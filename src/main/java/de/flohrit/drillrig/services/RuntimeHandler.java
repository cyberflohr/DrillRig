package de.flohrit.drillrig.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.flohrit.drillrig.DrillServer;

@Path("/runtime")
public class RuntimeHandler {
	
	@GET
	@Path("stop")
	@Produces("application/json")
	public void stop() {
		DrillServer.stopSshClients();
		Object x=null;
		x.toString();
	}	

	@GET
	@Path("start")
	@Produces("application/json")
	public void start() {
		DrillServer.startSshClients();
	}	
}
