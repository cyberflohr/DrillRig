package de.flohrit.drillrig.services;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.flohrit.drillrig.runtime.DrillServer;
import de.flohrit.drillrig.runtime.SshSessionMonitor;

@Path("/runtime")
public class RuntimeHandler {
	
	@GET
	@Path("stop")
	@Produces("application/json")
	public void stop() {
		DrillServer.getSshClientManager().stop();
	}	

	@GET
	@Path("start")
	@Produces("application/json")
	public void start() {
		DrillServer.getSshClientManager().start();
	}	
	
	@GET
	@Path("/monitor/forwards")
	@Produces("application/json")
	public List<ForwardStateInfo> getForwardStateInfo() {
		
		
		List<ForwardStateInfo> fwdInfos = new ArrayList<ForwardStateInfo>();
		for (SshSessionMonitor monitor : DrillServer.getSshClientManager().getSshClientMonitors()) {
			fwdInfos.addAll(monitor.getForwardStateInfos());
		}
		
		return fwdInfos;
	}	
}
