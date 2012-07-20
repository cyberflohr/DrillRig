package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import net.schmizz.concurrent.Event;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.Connection;
import net.schmizz.sshj.connection.channel.SocketStreamCopyMonitor;
import net.schmizz.sshj.connection.channel.direct.AbstractDirectChannel;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder.Parameters;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import socks.ProxyMessage;
import socks.SocksProxyServer;
import socks.server.ServerAuthenticatorNone;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.IpFilter;

public 	class MyDynamicPortForwarder extends Thread implements PortForwarder, DisconnectListener {
	final static private Logger logger = LoggerFactory
			.getLogger(MyDynamicPortForwarder.class);
	
	private SSHClient client;
	private Forward forwardCfg;

	private ServerSocket serverSocket;

	public void close() {
		try {
			if (client != null) {
				client.close();
			}
			if (serverSocket != null) {
				serverSocket.close();
			}
			serverSocket=null;
			
		} catch (IOException e) {
			logger.error("Forcing socket close failed.");
		}
	}

	MyDynamicPortForwarder(SSHClient client,
			Forward forward) throws IOException {
		super("DForwarder-" + forward.getSPort());
		this.client = client;
		

		serverSocket = new java.net.ServerSocket(
				forward.getSPort(), 50,
				InetAddress.getByName("*".equals(forward
						.getSHost()) ? "0.0.0.0" : forward
						.getSHost()));

		this.forwardCfg = forward;
	}
	
	@Override
	public void run() {

		while (!isInterrupted()) {
			try {
				final Socket socket = serverSocket.accept();
				if (isBlockedIp((InetSocketAddress) socket
						.getRemoteSocketAddress())) {
					socket.close();
				} else {

					SocksProxyServer ps = new SocksProxyServer(
							new ServerAuthenticatorNone(), socket) {

						@Override
						protected void onConnect(ProxyMessage msg)
								throws IOException {

							// start ssh forwarding for socks request
							startForwarding(socket, msg.host, msg.port);
							try {
								super.onConnect(msg);
								synchronized (socket) {
									try {
										socket.wait();
									} catch (InterruptedException e) {
									}
								}
							} catch (SocketException sex) {
							}
						}

					};

					// start own thread for socks session
					(new Thread(ps)).start();
				}

			} catch (ConnectException e) { // i.e. connection refused on
											// remote end

			} catch (IOException e) {
				logger.warn(
						"Local listener for port {} terminated with IOException",
						new Object[] { serverSocket.getLocalPort(),
								e.getLocalizedMessage() });
				break;
			}
		}
	}

	synchronized void startForwarding(Socket socket, String host, int port) throws IOException {

		Parameters parameters = new LocalPortForwarder.Parameters(
				forwardCfg.getSHost(), forwardCfg.getSPort(),
				host, port);


		final DirectTCPIPChannel chan = new DirectTCPIPChannel(
				client.getConnection(), socket, parameters);
		chan.open();
		chan.start();
		client.getTransport().setDisconnectListener(this);
	}

	class DirectTCPIPChannel extends AbstractDirectChannel {

		protected final Socket socket;
		protected final Parameters parameters;

		DirectTCPIPChannel(Connection conn, Socket socket,
				Parameters parameters) {
			super(conn, "direct-tcpip");
			this.socket = socket;
			this.parameters = parameters;
		}

		@Override
		protected synchronized void sendClose() throws TransportException {
			super.sendClose();
			synchronized (socket) {
				socket.notify();
			}
		}

		protected void start() throws IOException {
			socket.setSendBufferSize(getLocalMaxPacketSize());
			socket.setReceiveBufferSize(getRemoteMaxPacketSize());
			final Event<IOException> soc2chan = new StreamCopier(
					socket.getInputStream(), getOutputStream()).bufSize(
					getRemoteMaxPacketSize()).spawnDaemon("soc2chan");
			final Event<IOException> chan2soc = new StreamCopier(
					getInputStream(), socket.getOutputStream()).bufSize(
					getLocalMaxPacketSize()).spawnDaemon("chan2soc");
			SocketStreamCopyMonitor.monitor(5, TimeUnit.SECONDS, soc2chan,
					chan2soc, this, socket);
		}

		@Override
		protected SSHPacket buildOpenReq() {
			return super.buildOpenReq()
					.putString(parameters.getRemoteHost())
					.putUInt32(parameters.getRemotePort())
					.putString(parameters.getLocalHost())
					.putUInt32(parameters.getLocalPort());
		}
	}

	@Override
	public boolean isActive() {
	
		return super.isAlive() && client != null && serverSocket != null && client.isAuthenticated() && client.isConnected();
	}
	
	@Override
	public void notifyDisconnect(DisconnectReason paramDisconnectReason) {
		interrupt();
		client=null;
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
		}
		serverSocket=null;
		logger.warn(
				"notifyDisconnect received {}", paramDisconnectReason);
	}
	
	private boolean isBlockedIp(InetSocketAddress socketAddress) {

		IpFilter filter = forwardCfg.getFilter();
		if (filter != null) {
			if (filter.isEnabled()) {
				
				for (String mask : filter.getMask()) {
					if (Pattern.compile(mask).matcher(socketAddress.getAddress().getHostAddress()).matches()) {
						return filter.isBlock();
					}
				}
				return !filter.isBlock();
			}
		}
		return false;
	}
}
