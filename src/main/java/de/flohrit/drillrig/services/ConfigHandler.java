package de.flohrit.drillrig.services;

import java.util.Iterator;

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
import de.flohrit.drillrig.config.MachineAccount;
import de.flohrit.drillrig.config.SshClient;
import de.flohrit.drillrig.util.StringUtils;

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

	/**
	 * Add new forward to configuration.
	 * @param req HTTP request 
	 * @param forwardReq forward data
	 * @return service response.
	 */
	@POST
	@Path("forward/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addForward(@Context HttpServletRequest req,ForwardActionRequest forwardReq) {
		Configuration cfg = getEditConfiguration(req);
		
		for (SshClient sshClient : cfg.getSshClient()) {
			if (sshClient.getId().equals(forwardReq.getSSHClientId())) {
				
				Forward fwd = new Forward();
				fwd.setId(StringUtils.createUUID());
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

	/**
	 * Delete forward from configuration.
	 * @param req HTTP request 
	 * @param id forward id to delete
	 * @return service response.
	 */
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
	
	/**
	 * Change forward configuration.
	 * @param req HTTP request 
	 * @param id forward id to change
	 * @return service response.
	 */
	@POST
	@Path("forward/update/{id}")
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
	
	/**
	 * Add new machine to configuration.
	 * @param req HTTP request 
	 * @param machineCfg forward data
	 * @return service response.
	 */
	@POST
	@Path("machine/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addMachine(@Context HttpServletRequest req,MachineAccount machineCfg) {
		Configuration cfg = getEditConfiguration(req);
		
		machineCfg.setId(StringUtils.createUUID());
		machineCfg.setPassword(DrillServer.getEncDecorder().encrypt(
				machineCfg.getPassword()));
		cfg.getMachineAccount().add(machineCfg);
		
		return ServiceUtils.createOKResponse("Machine account added");
	}

	/**
	 * Delete machine from configuration.
	 * @param req HTTP request 
	 * @param id forward id to delete
	 * @return service response.
	 */
	@DELETE
	@Path("machine/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteMachine(@Context HttpServletRequest req, @PathParam("id") String id) {
		
		Configuration cfg = getEditConfiguration(req);
		for (SshClient sshClient : cfg.getSshClient()) {
			if (((MachineAccount)sshClient.getMachineAccount()).getId().equals(id)) {
				return ServiceUtils.createNOKResponse("Machine is in use by session " + sshClient.getName());
			}
		}
		Iterator<MachineAccount> iter = cfg.getMachineAccount().iterator();
		while (iter.hasNext()) {
			MachineAccount machine = iter.next();
			if (machine.getId().equals(id)) {
				iter.remove();
				return ServiceUtils.createOKResponse("Machine account deleted");
			}
		}
		
		return ServiceUtils.createOKResponse("Machine not found: " + id);
	}
	
	/**
	 * Change machine configuration.
	 * @param req HTTP request 
	 * @param id forward id to change
	 * @return service response.
	 */
	@POST
	@Path("machine/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse changeMachine(@Context HttpServletRequest req, @PathParam("id") String id, MachineAccount forwardReq) {
		
		Configuration cfg = getEditConfiguration(req);
		for (MachineAccount account : cfg.getMachineAccount()) {
				if (account.getId().equals(id)) {

					account.setName(forwardReq.getName());
					account.setHost(forwardReq.getHost());
					account.setPort(forwardReq.getPort());
					account.setUser(forwardReq.getUser());
					account.setPassword(forwardReq.getPassword().endsWith("==") ? forwardReq
						.getPassword() : DrillServer.getEncDecorder().encrypt(
						forwardReq.getPassword()));

					return ServiceUtils.createOKResponse("Machine account changed");
				}
		}
		return ServiceUtils.createOKResponse("Machine account not found: " + id);
	}
	
	/**
	 * Add new session to configuration.
	 * @param req HTTP request 
	 * @param forwardReq forward data
	 * @return service response.
	 */
	@POST
	@Path("session/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addSession(@Context HttpServletRequest req, SshClient sessionCfg) {
		Configuration cfg = getEditConfiguration(req);
		
		sessionCfg.setId(StringUtils.createUUID());
		for (MachineAccount mAccount : cfg.getMachineAccount()) {
			if (mAccount.getId().equals(sessionCfg.getMachineAccount())) {
				sessionCfg.setMachineAccount(mAccount);
				cfg.getSshClient().add(sessionCfg);
				
				return ServiceUtils.createOKResponse("SSH client session added");
			}
		}
		
		return ServiceUtils.createNOKResponse("Machine account not found: " + sessionCfg.getMachineAccount());
	}
	
	/**
	 * Delete session from configuration.
	 * @param req HTTP request 
	 * @param id session id to delete
	 * @return service response.
	 */
	@DELETE
	@Path("session/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteSession(@Context HttpServletRequest req, @PathParam("id") String id) {
		
		Configuration cfg = getEditConfiguration(req);
		Iterator<SshClient> iter = cfg.getSshClient().iterator();
		while (iter.hasNext()) {
			SshClient sshClient = iter.next();
			if (sshClient.getId().equals(id)) {
				iter.remove();
				return ServiceUtils.createOKResponse("Session '" + sshClient.getName() + "' deleted");
			}
		}
		
		return ServiceUtils.createNOKResponse("Session not found: " + id);
	}
	
	/**
	 * Change session configuration.
	 * @param req HTTP request 
	 * @param id session id to change
	 * @return service response.
	 */
	@POST
	@Path("session/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse changeSession(@Context HttpServletRequest req, @PathParam("id") String id, SshClient newSessionCfg) {
		
		Configuration cfg = getEditConfiguration(req);
		for (SshClient sessionCfg : cfg.getSshClient()) {
				if (sessionCfg.getId().equals(id)) {

					for (MachineAccount machine : cfg.getMachineAccount()) {
						if (machine.getId().equals(newSessionCfg.getMachineAccount())) {
							
							sessionCfg.setName(newSessionCfg.getName());
							sessionCfg.setEnabled(newSessionCfg.isEnabled());
							sessionCfg.setDescription(newSessionCfg.getDescription());
							sessionCfg.setMachineAccount(machine);

							return ServiceUtils.createOKResponse("Session configuration changed");
						}
					}
					
					return ServiceUtils.createNOKResponse("Maschine configuration not found changed");
				}
		}
		return ServiceUtils.createOKResponse("Session not found: " + id);
	}
	
}
