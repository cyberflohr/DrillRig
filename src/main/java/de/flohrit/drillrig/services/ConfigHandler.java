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

import de.flohrit.drillrig.config.Configuration;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.Connection;
import de.flohrit.drillrig.config.SshSession;
import de.flohrit.drillrig.runtime.DrillServer;
import de.flohrit.drillrig.util.StringUtils;

@Path("/config")
public class ConfigHandler {

	@GET
	@Path("save")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse save(@Context HttpServletRequest req) {

		Configuration cfg = getEditConfiguration(req);
		DrillServer.writeConfiguration(cfg);
		DrillServer.restartService();

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
	public Configuration read(@Context HttpServletRequest req,
			@QueryParam("edit") boolean editCfg) {
		return editCfg ? getEditConfiguration(req) : DrillServer
				.getConfiguration();
	}

	private Configuration getEditConfiguration(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		Configuration cfg = (Configuration) session
				.getAttribute("editConfiguration");
		if (cfg == null) {
			cfg = (Configuration) DrillServer.getConfiguration().clone();

			session.setAttribute("editConfiguration", cfg);
		}

		return cfg;
	}

	/**
	 * Add new forward to configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param forwardReq
	 *            forward data
	 * @return service response.
	 */
	@POST
	@Path("forward/add/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addForward(@Context HttpServletRequest req,
			@PathParam("id") String sessionId, Forward forwardReq) {
		Configuration cfg = getEditConfiguration(req);

		for (SshSession sshClient : cfg.getSshSession()) {
			if (sshClient.getId().equals(sessionId)) {

				Connection mAccount = getConnectionById((String) forwardReq.getConnection(), cfg);
				if (mAccount != null) {
				
						Forward fwd = new Forward();
						fwd.setConnection(mAccount);

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

				return ServiceUtils.createNOKResponse("SSH connection not found");
			}
		}

		return ServiceUtils.createNOKResponse("SSH client not found");
	}

	private Connection getConnectionById(String connectionId,
			Configuration cfg) {
		
		for (Connection mAccount : cfg.getConnection()) {
			if (mAccount.getId().equals(connectionId)) {
				return mAccount;
			}
		}
		return null;
	}

	/**
	 * Delete forward from configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            forward id to delete
	 * @return service response.
	 */
	@DELETE
	@Path("forward/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteForward(@Context HttpServletRequest req,
			@PathParam("id") String id) {

		Configuration cfg = getEditConfiguration(req);
		for (SshSession sshClient : cfg.getSshSession()) {
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
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            forward id to change
	 * @return service response.
	 */
	@POST
	@Path("forward/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse updateForward(@Context HttpServletRequest req,
			@PathParam("id") String id, Forward forwardReq) {

		Configuration cfg = getEditConfiguration(req);
		for (SshSession sshClient : cfg.getSshSession()) {
			Iterator<Forward> iter = sshClient.getForward().iterator();
			while (iter.hasNext()) {
				Forward fwd = iter.next();
				if (fwd.getId().equals(id)) {

					Connection mAccount = getConnectionById((String) forwardReq.getConnection(), cfg);
					if (mAccount != null) {
						fwd.setDescription(forwardReq.getDescription());
						fwd.setEnabled(forwardReq.isEnabled());
						fwd.setType(forwardReq.getType());
						fwd.setRHost(forwardReq.getRHost());
						fwd.setSHost(forwardReq.getSHost());
						fwd.setRPort(forwardReq.getRPort());
						fwd.setSPort(forwardReq.getSPort());
						fwd.setConnection(mAccount);
						fwd.setFilter(forwardReq.getFilter());
						
						return ServiceUtils.createOKResponse("Forward changed");
					} 

					return ServiceUtils.createNOKResponse("Connection not found");
				}
			}
		}
		return ServiceUtils.createOKResponse("Forward not found: " + id);
	}

	/**
	 * Add new connection to configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param connectionCfg
	 *            forward data
	 * @return service response.
	 */
	@POST
	@Path("connection/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addConnection(@Context HttpServletRequest req,
			Connection connectionCfg) {
		Configuration cfg = getEditConfiguration(req);

		connectionCfg.setId(StringUtils.createUUID());
		connectionCfg.setPassword(DrillServer.getEncDecorder().encrypt(
				connectionCfg.getPassword()));
		cfg.getConnection().add(connectionCfg);

		return ServiceUtils.createOKResponse("Connection added");
	}

	/**
	 * Delete connection from configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            forward id to delete
	 * @return service response.
	 */
	@DELETE
	@Path("connection/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteConnection(@Context HttpServletRequest req,
			@PathParam("id") String id) {

		Configuration cfg = getEditConfiguration(req);
		for (SshSession sshClient : cfg.getSshSession()) {
			for (Forward fwd : sshClient.getForward()) {
				if (((Connection) fwd.getConnection()).getId().equals(
						id)) {
					return ServiceUtils
							.createNOKResponse("Connection is in use by session "
									+ sshClient.getName());
				}
			}
		}
		Iterator<Connection> iter = cfg.getConnection().iterator();
		while (iter.hasNext()) {
			Connection connection = iter.next();
			if (connection.getId().equals(id)) {
				iter.remove();
				return ServiceUtils.createOKResponse("Connection deleted");
			}
		}

		return ServiceUtils.createOKResponse("Connection not found: " + id);
	}

	/**
	 * Change connection configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            forward id to change
	 * @return service response.
	 */
	@POST
	@Path("connection/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse updateConnection(@Context HttpServletRequest req,
			@PathParam("id") String id, Connection forwardReq) {

		Configuration cfg = getEditConfiguration(req);
		for (Connection account : cfg.getConnection()) {
			if (account.getId().equals(id)) {

				account.setName(forwardReq.getName());
				account.setHost(forwardReq.getHost());
				account.setPort(forwardReq.getPort());
				account.setUser(forwardReq.getUser());
				account.setPassword(forwardReq.getPassword().endsWith("==") ? forwardReq
						.getPassword() : DrillServer.getEncDecorder().encrypt(
						forwardReq.getPassword()));
				account.setProxy(forwardReq.getProxy());
				
				return ServiceUtils.createOKResponse("Connection changed");
			}
		}
		return ServiceUtils
				.createOKResponse("Connection not found: " + id);
	}

	/**
	 * Add new session to configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param forwardReq
	 *            forward data
	 * @return service response.
	 */
	@POST
	@Path("session/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse addSession(@Context HttpServletRequest req,
			SshSession sessionCfg) {
		Configuration cfg = getEditConfiguration(req);

		sessionCfg.setId(StringUtils.createUUID());
		cfg.getSshSession().add(sessionCfg);

		return ServiceUtils.createOKResponse("SSH client session added");
	}

	/**
	 * Delete session from configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            session id to delete
	 * @return service response.
	 */
	@DELETE
	@Path("session/delete/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse deleteSession(@Context HttpServletRequest req,
			@PathParam("id") String id) {

		Configuration cfg = getEditConfiguration(req);
		Iterator<SshSession> iter = cfg.getSshSession().iterator();
		while (iter.hasNext()) {
			SshSession sshClient = iter.next();
			if (sshClient.getId().equals(id)) {
				iter.remove();
				return ServiceUtils.createOKResponse("Session '"
						+ sshClient.getName() + "' deleted");
			}
		}

		return ServiceUtils.createNOKResponse("Session not found: " + id);
	}

	/**
	 * Change session configuration.
	 * 
	 * @param req
	 *            HTTP request
	 * @param id
	 *            session id to change
	 * @return service response.
	 */
	@POST
	@Path("session/update/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ServiceResponse updateSession(@Context HttpServletRequest req,
			@PathParam("id") String id, SshSession newSessionCfg) {

		Configuration cfg = getEditConfiguration(req);
		for (SshSession sessionCfg : cfg.getSshSession()) {
			if (sessionCfg.getId().equals(id)) {

				sessionCfg.setName(newSessionCfg.getName());
				sessionCfg.setEnabled(newSessionCfg.isEnabled());
				sessionCfg.setDescription(newSessionCfg.getDescription());

				return ServiceUtils
						.createOKResponse("Session configuration changed");
			}
		}
		return ServiceUtils.createOKResponse("Session not found: " + id);
	}

}
