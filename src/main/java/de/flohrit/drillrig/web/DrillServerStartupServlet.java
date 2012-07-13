package de.flohrit.drillrig.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import de.flohrit.drillrig.runtime.DrillServer;

public class DrillServerStartupServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		DrillServer.startService();
	}

	@Override
	public void destroy() {
		super.destroy();

		DrillServer.stopService();
	}
}
