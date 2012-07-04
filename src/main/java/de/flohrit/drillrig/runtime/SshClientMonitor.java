package de.flohrit.drillrig.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.transport.DisconnectListener;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.MachineAccount;
import de.flohrit.drillrig.config.SshClient;
import de.flohrit.drillrig.services.ForwardStateInfo;

public class SshClientMonitor extends Thread implements DisconnectListener {
	final static private Logger logger = LoggerFactory
			.getLogger(SshClientMonitor.class);

	private Map<Forward, PortForwarder> portForwarders = new LinkedHashMap<Forward, PortForwarder>();
	private SSHClient sshClientSession;
	private SshClient sshClientsCfg;

	public SshClientMonitor(SshClient sshClientsCfg) throws IOException {

		super(sshClientsCfg.getName());
		setDaemon(true);
		this.sshClientsCfg = sshClientsCfg;

		// create ssh session
		sshClientSession = createSshTransportSession(sshClientsCfg);
	}

	private void createPortForwardings(SSHClient sshClient,
			SshClient sshClientsCfg) {

		for (Forward forward : sshClientsCfg.getForward()) {

			createPortForwarding(sshClient, forward);

		}
	}

	public void createPortForwarding(SSHClient sshClient, Forward forward) {
		PortForwarder fwd;
		if ("".equals(forward.getId())) {
			forward.setId("ID" + System.currentTimeMillis()
					+ String.valueOf(System.nanoTime()));
		}

		try {
			if ("L".equals(forward.getType())) {
				fwd = new MyLocalPortForwarder(sshClient, forward);

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

	private SSHClient createSshTransportSession(SshClient sshClientsCfg) {
		SSHClient sshClient = new SSHClient();
		MachineAccount maschineAccount = sshClientsCfg.getMachineAccount();
		try {
			logger.info("create ssh session for user {}@{}", new Object[] {
					maschineAccount.getUser(), maschineAccount.getHost() });
			sshClient.addHostKeyVerifier(new AutoKnownHostsVerifier(new File(
					"known_hosts")));

			sshClient.connect(maschineAccount.getHost(),
					maschineAccount.getPort());
			sshClient.authPassword(maschineAccount.getUser(),
					maschineAccount.getPassword());

			sshClient.getTransport().setDisconnectListener(this);

			createPortForwardings(sshClient, sshClientsCfg);
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
			sshClientSession = null;
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
				if (sshClientSession == null) {
					sshClientSession = createSshTransportSession(sshClientsCfg);
				} else {
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

					for (Forward forward : sshClientsCfg.getForward()) {
						PortForwarder myLocalPortForwarder = portForwarders
								.get(forward);
						if (myLocalPortForwarder == null
								|| !myLocalPortForwarder.isAlive()) {
							createPortForwarding(sshClientSession, forward);
						}
					}
				}
			}
		}

		logger.info("Shuting down SshClientMonitor thread");
		try {
			synchronized (portForwarders) {
				for (PortForwarder myPortForwarder : portForwarders.values()) {
					myPortForwarder.close();
				}
				portForwarders.clear();
			}
			if (sshClientSession != null) {
				sshClientSession.close();
				sshClientSession = null;
			}
		} catch (IOException e) {
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
				if (!forward.isEnabled()) {
					info.setState("deactivated");
				} else if (myLocalPortForwarder == null) {
					info.setState("stopped");
				} else if (myLocalPortForwarder.isAlive()) {
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
