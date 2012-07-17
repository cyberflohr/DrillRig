package de.flohrit.drillrig.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.SshSession;

public class SshSessionManager {

	final static private Logger logger = LoggerFactory
			.getLogger(SshSessionManager.class);
			
	private List<SshSession> sshClientsCfg;
	private List<SshSessionMonitor> sshClientMonitor = new ArrayList<SshSessionMonitor>();
	private State state;
	
	private enum State { ACTIVE, INACTIVE };
	
	public SshSessionManager(List<SshSession> sshClientsCfg) {

		this.state = State.INACTIVE;
		this.sshClientsCfg = sshClientsCfg;
	}

	synchronized public void start() {
		if (State.ACTIVE == state) {
			return;
		}
		
		sshClientMonitor.clear();
		for (SshSession client : sshClientsCfg) {

			try {
				SshSessionMonitor mon = new SshSessionMonitor(client);
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
			Iterator<SshSessionMonitor> iter = sshClientMonitor.iterator();
			while (iter.hasNext()) {
				SshSessionMonitor mon = iter.next();
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
	
	synchronized public List<SshSessionMonitor> getSshClientMonitors() {
		return sshClientMonitor;
	}
}
