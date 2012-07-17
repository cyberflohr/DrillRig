package de.flohrit.drillrig.runtime;

public interface PortForwarder {

	public void close();
	public boolean isActive();
	public void start();
}
