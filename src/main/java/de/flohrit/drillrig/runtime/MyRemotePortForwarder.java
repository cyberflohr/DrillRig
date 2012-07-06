package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Forward;

public 	class MyRemotePortForwarder implements PortForwarder {
	final static private Logger logger = LoggerFactory
			.getLogger(MyRemotePortForwarder.class);
	
	private SSHClient client;
	private Forward forwardCfg;
	private net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward remoteFwd;
	
	public void close() {
		try {
			if (isAlive()) {
				if (false) client.getRemotePortForwarder().cancel(remoteFwd);
			}
		} catch (IOException e) {
			logger.error("Forcing socket close failed.");
		}
	}

	MyRemotePortForwarder(SSHClient sshClient,
			Forward forward) throws IOException {

		this.client = sshClient;
		this.forwardCfg = forward;
	}
	
	public void start() {
		try {
			remoteFwd = client
			.getRemotePortForwarder()
			.bind(// where the server should listen
					"*".equals(forwardCfg.getRHost()) ?
					new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(
							forwardCfg.getRPort()) : new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(forwardCfg.getRHost(),
							forwardCfg.getRPort()),
							
					// what we do with incoming connections that are
					// forwarded to us
					new MySocketConnectListener(
							new InetSocketAddress(forwardCfg.getSHost(), forwardCfg.getSPort())));
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
}
