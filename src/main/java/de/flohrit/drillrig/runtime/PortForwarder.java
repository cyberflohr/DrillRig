package de.flohrit.drillrig.runtime;

public interface PortForwarder {

	public void close();
	public boolean isAlive();
	public void start();
}
