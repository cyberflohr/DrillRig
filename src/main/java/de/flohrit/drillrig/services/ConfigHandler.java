package de.flohrit.drillrig.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.flohrit.drillrig.DrillServer;
import de.flohrit.drillrig.config.Configuration;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.SshClient;

@Path("/config")
public class ConfigHandler {

	@GET
	@Path("read")
	@Produces(MediaType.APPLICATION_JSON)
	public Configuration read() {
		return DrillServer.getConfiguration();
	}
	
	@POST
	@Path("tunnel/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addTunnel(ForwardActionRequest forwardReq) {
		Configuration cfg = DrillServer.getConfiguration();
		
		for (SshClient sshClient : cfg.getSshClient()) {
			if (sshClient.getId().equals(forwardReq.getSSHClientId())) {
				
				Forward fwd = new Forward();
				fwd.setDescription(forwardReq.getDescription());
				fwd.setEnabled(forwardReq.isEnabled());
				fwd.setSHost(forwardReq.getSHost());
				fwd.setSPort(forwardReq.getRPort());
				fwd.setRHost(forwardReq.getRHost());
				fwd.setRPort(forwardReq.getRPort());
				fwd.setType(forwardReq.getType());
				sshClient.getForward().add(fwd);

				ServiceResponse sr = new ServiceResponse();
				sr.setCode("OK");
				sr.setMsg("Tunnel added to SSH Client.");
				return sr;
			}
		}
		
		ServiceResponse sr = new ServiceResponse();
		sr.setCode("NOK");
		sr.setMsg("SSH Client not found.");
		return sr;
	}
	
}
