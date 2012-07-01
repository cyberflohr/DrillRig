package de.flohrit.drillrig.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.flohrit.drillrig.DrillServer;
import de.flohrit.drillrig.config.Configuration;

@Path("/config")
public class ConfigHandler {

	@GET
	@Path("read")
	@Produces("application/json")
	public Configuration read() {
		return DrillServer.getConfiguration();
	}
}
