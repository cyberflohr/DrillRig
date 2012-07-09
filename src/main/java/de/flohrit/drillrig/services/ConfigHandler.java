package de.flohrit.drillrig.services;

import java.util.Iterator;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.flohrit.drillrig.DrillServer;
import de.flohrit.drillrig.config.Configuration;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.SshClient;

@Path("/config")
public class ConfigHandler {
	
	@GET
	@Path("save")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse save(@Context HttpServletRequest req) {

		Configuration cfg = getEditConfiguration(req);
		DrillServer.writeConfiguration(cfg);
		DrillServer.reloadServer();
		
		ServiceResponse sr = new ServiceResponse();
		ServiceStatus ss = new ServiceStatus();
		ss.setCode("OK");
		ss.getMsg().add("New configuration loaded and server restarted.");
		
		sr.setServiceStatus(ss);
		
		return sr;
	}

	@GET
	@Path("read")
	@Produces(MediaType.APPLICATION_JSON)
	public Configuration read(@Context HttpServletRequest req, @QueryParam("edit") boolean editCfg) {
		return editCfg ? getEditConfiguration(req) : DrillServer.getConfiguration();
	}
	
	private Configuration getEditConfiguration(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		Configuration cfg = (Configuration) session.getAttribute("editConfiguration");
		if (cfg == null) {
			cfg =  (Configuration) DrillServer.getConfiguration().clone();
			
			session.setAttribute("editConfiguration", cfg);
		}
		
		return cfg;
	}

	@POST
	@Path("forward/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addForward(@Context HttpServletRequest req,ForwardActionRequest forwardReq) {
		Configuration cfg = getEditConfiguration(req);
		
		for (SshClient sshClient : cfg.getSshClient()) {
			if (sshClient.getId().equals(forwardReq.getSSHClientId())) {
				
				Forward fwd = new Forward();
				fwd.setId(createUUID());
				fwd.setDescription(forwardReq.getDescription());
				fwd.setEnabled(forwardReq.isEnabled());
				fwd.setSHost(forwardReq.getSHost());
				fwd.setSPort(forwardReq.getSPort());
				fwd.setRHost(forwardReq.getRHost());
				fwd.setRPort(forwardReq.getRPort());
				fwd.setType(forwardReq.getType());
				sshClient.getForward().add(fwd);

				return ServiceUtils.createOKResponse("SSH forward added");
			}
		}
		
		return ServiceUtils.createNOKResponse("SSH client not found");
	}

	@DELETE
	@Path("forward/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteForward(@Context HttpServletRequest req, @PathParam("id") String id) {
		
		Configuration cfg = getEditConfiguration(req);
		for (SshClient sshClient : cfg.getSshClient()) {
			Iterator<Forward> iter = sshClient.getForward().iterator();
			while (iter.hasNext()) {
				Forward fwd = iter.next();
				if (fwd.getId().equals(id)) {
					iter.remove();
					return ServiceUtils.createOKResponse("Forward removed");
				}
			}
		}
		return ServiceUtils.createOKResponse("Forward not found: " + id);
	}
	
	@POST
	@Path("forward/change/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse changeForward(@Context HttpServletRequest req, @PathParam("id") String id, ForwardActionRequest forwardReq) {
		
		Configuration cfg = getEditConfiguration(req);
		for (SshClient sshClient : cfg.getSshClient()) {
			Iterator<Forward> iter = sshClient.getForward().iterator();
			while (iter.hasNext()) {
				Forward fwd = iter.next();
				if (fwd.getId().equals(id)) {

					fwd.setDescription(forwardReq.getDescription());
					fwd.setEnabled(forwardReq.isEnabled());
					fwd.setType(forwardReq.getType());
					fwd.setRHost(forwardReq.getRHost());
					fwd.setSHost(forwardReq.getSHost());
					fwd.setRPort(forwardReq.getRPort());
					fwd.setSPort(forwardReq.getSPort());

					return ServiceUtils.createOKResponse("Forward changed");
				}
			}
		}
		return ServiceUtils.createOKResponse("Forward not found: " + id);
	}

	private String createUUID() {
		return "ID" + UUID.randomUUID().toString().replace('-','_');
	}
}
