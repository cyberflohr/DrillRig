package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Forward;

public 	class MyRemotePortForwarder implements PortForwarder, DisconnectListener {
	final static private Logger logger = LoggerFactory
			.getLogger(MyRemotePortForwarder.class);
	
	private SSHClient client;
	private Forward forwardCfg;
	private net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward remoteFwd;
	
	public void close() {
		try {
			if (isAlive()) {
				client.getRemotePortForwarder().cancel(remoteFwd);
				client.close();
			}
		} catch (IOException e) {
			logger.error("Forcing socket close failed.");
		}
	}

	MyRemotePortForwarder(SSHClient sshClient,
			Forward forward) throws IOException {

		this.client = sshClient;
		this.forwardCfg = forward;
		client.getTransport().setDisconnectListener(this);
	}
	
	public void start() {
		try {
			remoteFwd = client
			.getRemotePortForwarder()
			.bind(// where the server should listen
					"*".equals(forwardCfg.getSHost()) ?
					new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(
							forwardCfg.getSPort()) : new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(forwardCfg.getSHost(),
							forwardCfg.getSPort()),
							
					// what we do with incoming connections that are
					// forwarded to us
					new MySocketConnectListener(
							new InetSocketAddress(forwardCfg.getRHost(), forwardCfg.getRPort())));
		} catch (ConnectionException e) {
			logger.error("Starting remote port tunnel failed.", e);
		} catch (TransportException e) {
			logger.error("Starting remote port tunnel failed.", e);
		}
	}

	@Override
	public boolean isAlive() {
		return remoteFwd != null
				&& client.getRemotePortForwarder().getActiveForwards()
						.contains(remoteFwd); 
	}
	
	@Override
	public void notifyDisconnect(DisconnectReason paramDisconnectReason) {
		remoteFwd=null;
		logger.warn(
				"notifyDisconnect received {}", paramDisconnectReason);
	}
}
