package de.flohrit.drillrig.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.flohrit.drillrig.DrillServer;

@Path("/runtime")
public class RuntimeHandler {
	
	@GET
	@Path("read")
	@Produces("application/json")
	public boolean stop() {
		DrillServer.stopSshClients();
		return true;
	}	

	@GET
	@Path("read")
	@Produces("application/json")
	public boolean start() {
		DrillServer.startSshClients();
		return true;
	}	
}
