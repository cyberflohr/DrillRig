package de.flohrit.drillrig.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.transport.DisconnectListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Connection;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.SshSession;
import de.flohrit.drillrig.services.ForwardStateInfo;

public class SshSessionMonitor extends Thread implements DisconnectListener {
	final static private Logger logger = LoggerFactory
			.getLogger(SshSessionMonitor.class);

	private Map<Forward, PortForwarder> portForwarders = new LinkedHashMap<Forward, PortForwarder>();
	private SshSession sshClientsCfg;

	public SshSessionMonitor(SshSession sshClientsCfg) throws IOException {

		super(sshClientsCfg.getName());
		setDaemon(true);
		this.sshClientsCfg = sshClientsCfg;

		if (sshClientsCfg.isEnabled()) {
			createPortForwardings(sshClientsCfg);
		}
	}

	@Override
	public synchronized void start() {
		if (sshClientsCfg.isEnabled()) {
			super.start();
		}
	}

	private void createPortForwardings(SshSession sshClientsCfg) {

		for (Forward forward : sshClientsCfg.getForward()) {

			createPortForwarding(forward);
		}
	}

	public void createPortForwarding(Forward forward) {
		PortForwarder fwd;

		try {
			SSHClient sshClient = createSshTransportSession(forward);
			if ("L".equals(forward.getType())) {
				fwd = new MyLocalPortForwarder(sshClient, forward);

			} else if ("D".equals(forward.getType())) {

				fwd = new MyDynamicPortForwarder(sshClient, forward);
				
			} else {

				fwd = new MyRemotePortForwarder(sshClient, forward);
			}

			portForwarders.put(forward, fwd);
			fwd.start();
		} catch (IOException e) {
			logger.error("Can't create port {} listener for interface {}",
					new Object[] { forward.getSPort(), e.toString() });
		}
	}

	private SSHClient createSshTransportSession(Forward forward) {
		SSHClient sshClient = new SSHClient();
		Connection maschineAccount = (Connection) forward.getConnection();
		try {
			logger.info("create ssh session for user {}@{}", new Object[] {
					maschineAccount.getUser(), maschineAccount.getHost() });
			sshClient.addHostKeyVerifier(new AutoKnownHostsVerifier(new File(
					"known_hosts")));

			sshClient.connect(maschineAccount.getHost(),
					maschineAccount.getPort());
			
			sshClient.authPassword(maschineAccount.getUser(),
					DrillServer.getEncDecorder().decrypt(maschineAccount.getPassword()));

			
		} catch (IOException e) {
			logger.error(
					"Authentication failed for user {}@{}: {}",
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
		synchronized (portForwarders) {
			logger.info("Disconnect event received, reason: {}",
					paramDisconnectReason);

			for (PortForwarder forward : portForwarders.values()) {
				forward.close();
			}
			portForwarders.clear();
		}
	}

	@Override
	public void run() {
		while (!interrupted()) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				logger.error("SshClientMonitor thread interrupted -> exiting now");
				break;
			}

			synchronized (portForwarders) {
/*
					try {
						sshClientSession.getTransport().write(
								new SSHPacket(Message.IGNORE));
					} catch (TransportException e) {
						logger.error("SshClientMonitor failed to send heartbeat message");
						try {
							sshClientSession.close();
						} catch (IOException e1) {
						}
					}
*/
					for (Forward forward : sshClientsCfg.getForward()) {
						PortForwarder myLocalPortForwarder = portForwarders
								.get(forward);
						if (myLocalPortForwarder == null
								|| !myLocalPortForwarder.isActive()) {
							createPortForwarding(forward);
						}
					}
			}
		}

		logger.info("Shuting down SshClientMonitor thread");
		synchronized (portForwarders) {
			for (PortForwarder myPortForwarder : portForwarders.values()) {
				myPortForwarder.close();
			}
			portForwarders.clear();
		}
		logger.info("Shuting down SshClientMonitor thread completed");
	}

	public List<ForwardStateInfo> getForwardStateInfos() {

		List<ForwardStateInfo> infos = new ArrayList<ForwardStateInfo>();
		synchronized (portForwarders) {
			for (Forward forward : sshClientsCfg.getForward()) {
				ForwardStateInfo info = new ForwardStateInfo();
				info.setId(forward.getId());

				PortForwarder myLocalPortForwarder = portForwarders
						.get(forward);
				if (!forward.isEnabled() || !sshClientsCfg.isEnabled()) {
					info.setState("deactivated");
				} else if (myLocalPortForwarder == null) {
					info.setState("stopped");
				} else if (myLocalPortForwarder.isActive()) {
					info.setState("running");
				} else {
					info.setState("retry");
				}

				infos.add(info);
			}
		}

		return infos;
	}
}
