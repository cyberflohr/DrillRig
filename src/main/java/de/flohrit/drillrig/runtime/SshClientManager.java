package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.SshClient;

public class SshClientManager {

	final static private Logger logger = LoggerFactory
			.getLogger(SshClientManager.class);
			
	private List<SshClient> sshClientsCfg;
	private List<SshClientMonitor> sshClientMonitor = new ArrayList<SshClientMonitor>();
	
	public SshClientManager(List<SshClient> sshClientsCfg) {

		this.sshClientsCfg = sshClientsCfg;
	}

	public void start() {
		for (SshClient client : sshClientsCfg) {
	
			try {
				SshClientMonitor mon = new SshClientMonitor(client);
				mon.start();
				sshClientMonitor.add(mon);
			} catch (IOException e) {
				logger.error("Failed to start monitor for client: {}", client);
			}
		}
	}

	public void stop() {
		// notify thread termination
		for (SshClientMonitor mon : sshClientMonitor) {
			mon.interrupt();
		}

		while (!sshClientMonitor.isEmpty()) {
			Iterator<SshClientMonitor> iter = sshClientMonitor.iterator();
			while (iter.hasNext()) {
				if (!iter.next().isAlive()) {
					iter.remove();
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
		}
	}
}
