package de.flohrit.drillrig.runtime;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.MachineAccount;
import de.flohrit.drillrig.config.SshClient;

public class SshClientMonitor extends Thread implements DisconnectListener {
	final static private Logger logger = LoggerFactory
			.getLogger(SshClientMonitor.class);

	
	private Map<Forward,MyLocalPortForwarder> portForwarders = new LinkedHashMap<Forward,MyLocalPortForwarder>();
	private SSHClient sshClient;
	private SshClient sshClientsCfg;
	
	public SshClientMonitor(SshClient sshClientsCfg) throws IOException {
	
		super(sshClientsCfg.getName());
		setDaemon(true);
		this.sshClientsCfg = sshClientsCfg;

		// create ssh session
		sshClient = createSshTransportSession(sshClientsCfg);
	}

	private void createPortForwardings(SSHClient sshClient, SshClient sshClientsCfg) {
		
		for (Forward forward : sshClientsCfg.getForward()) {

			createPortForwarding(sshClient, forward);		

		}
	}

	public void createPortForwarding(SSHClient sshClient, Forward forward) {
		if ("L".equals(forward.getType())) {
			MyLocalPortForwarder fwd;
			try {
				fwd = new MyLocalPortForwarder(sshClient, forward);

				portForwarders.put(forward, fwd);
				fwd.start();
			} catch (IOException e) {
				logger.error(
						"Can't create local port {} listener for interface {}",
						new Object[] { forward.getSPort(), e.toString() });
			}

		} else {

			/*
			 * We make _server_ listen on port 8080, which forwards all
			 * connections to us as a channel, and we further forward all such
			 * channels to google.com:80
			 */
			try {
				sshClient
						.getRemotePortForwarder()
						.bind(
						// where the server should listen
								"*".equals(forward.getRHost()) ?
						new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(
								forward.getRPort()) : new net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward(forward.getRHost(),
										forward.getRPort()),
						// what we do with incoming connections that are
						// forwarded to us
								new MySocketConnectListener(
										new InetSocketAddress(forward.getSHost(), forward.getSPort())));
				
				portForwarders.put(forward, null);
			} catch (ConnectionException e) {
				logger.error(
						"Can't remote port {} listener for interface {}", forward.getRPort());
			} catch (TransportException e) {
				logger.error(
						"Can't remote port {} listener for interface {}", forward.getRPort());
			}

		}
	}

	private SSHClient createSshTransportSession(SshClient sshClientsCfg) {
		SSHClient sshClient = new SSHClient();
		MachineAccount maschineAccount = sshClientsCfg.getMachineAccount();
		try {
			logger.info("create ssh session for user {}@{}", new Object[] { maschineAccount.getUser(),
							maschineAccount.getHost() });
			sshClient.addHostKeyVerifier(new AutoKnownHostsVerifier(
					new File("known_hosts")));

			sshClient.connect(maschineAccount.getHost(),
					maschineAccount.getPort());
			sshClient.authPassword(maschineAccount.getUser(),
					maschineAccount.getPassword());
					
			sshClient.getTransport().setDisconnectListener(this);
					
			createPortForwardings(sshClient, sshClientsCfg);
		} catch (IOException e) {
			logger.error("Authentication failed for user {}@{}: {}",
					new Object[] { maschineAccount.getUser(),
							maschineAccount.getHost(), e });
			try {
				sshClient.close();
			} catch (IOException e1) {
			}
			return null;
		}
		return sshClient;
	}


	@Override
	public void notifyDisconnect(DisconnectReason paramDisconnectReason) {
		synchronized(portForwarders) {
			logger.info("Disconnect event received, reason: {}", paramDisconnectReason);
			for (MyLocalPortForwarder forward : portForwarders.values()) {
				if (forward!=null) {
					forward.close();
				}
			}
			portForwarders.clear();
			sshClient=null;
		}
	}
	
	@Override
	public void run() {
		while (!isInterrupted()) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error("SshClientMonitor thread interrupted -> exiting now");
			}
			
			if (sshClient==null) {
				sshClient = createSshTransportSession(sshClientsCfg);
			} else {
				try {
					sshClient.getTransport().write(new SSHPacket(Message.IGNORE));
				} catch (TransportException e) {
					logger.error("SshClientMonitor failed to send heartbeat message");
				}

				synchronized(portForwarders) {
					for (Forward forward : sshClientsCfg.getForward()) {
						if ("L".equals(forward.getType())) {
							MyLocalPortForwarder myLocalPortForwarder = portForwarders.get(forward);
							if (myLocalPortForwarder == null) {
								createPortForwarding(sshClient, forward);
							} else if (!myLocalPortForwarder.isAlive()) {
								createPortForwarding(sshClient, forward);
							}
						} else {
							if (!portForwarders.containsKey(forward)) {
								createPortForwarding(sshClient, forward);
							}
						}
					}
				}										
			}
		}
		
		logger.info("Shuting down SshClientMonitor thread");
		try {
			for (MyLocalPortForwarder myLocalPortForwarder : portForwarders.values()) {
				myLocalPortForwarder.close();
			}
			portForwarders.clear();
			sshClient.close();
		} catch (IOException e) {
		}
		logger.info("Shuting down SshClientMonitor thread completed");
	}		
}

