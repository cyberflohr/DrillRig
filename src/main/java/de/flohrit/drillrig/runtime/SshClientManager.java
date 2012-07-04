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
	private State state;
	
	private enum State { ACTIVE, INACTIVE };
	
	public SshClientManager(List<SshClient> sshClientsCfg) {

		this.state = State.INACTIVE;
		this.sshClientsCfg = sshClientsCfg;
	}

	synchronized public void start() {
		if (State.ACTIVE == state) {
			return;
		}
		
		sshClientMonitor.clear();
		for (SshClient client : sshClientsCfg) {

			try {
				SshClientMonitor mon = new SshClientMonitor(client);
				mon.start();
				sshClientMonitor.add(mon);
			} catch (IOException e) {
				logger.error("Failed to start monitor for client: {}", client);
			}
		}
		this.state = State.ACTIVE;
	}

	synchronized public void stop() {

		if (State.INACTIVE == state) {
			return;
		}

		boolean doPoll=true;
		while (doPoll) {
			
			doPoll=false;
			Iterator<SshClientMonitor> iter = sshClientMonitor.iterator();
			while (iter.hasNext()) {
				SshClientMonitor mon = iter.next();
				mon.interrupt();
				if (mon.isAlive()) {
					doPoll=true;
				}
			}
			try {
				if (doPoll) {
					Thread.sleep(5000);
				}
			} catch (InterruptedException e) {
			}
		}
		this.state = State.INACTIVE;
	}
	
	synchronized public List<SshClientMonitor> getSshClientMonitors() {
		return sshClientMonitor;
	}
}
