package de.flohrit.drillrig;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Configuration;
import de.flohrit.drillrig.config.Forward;
import de.flohrit.drillrig.config.MachineAccount;
import de.flohrit.drillrig.config.SshClient;
import de.flohrit.drillrig.runtime.AutoKnownHostsVerifier;
import de.flohrit.drillrig.runtime.SshClientManager;

public class DrillServer {
	final static private Logger logger = LoggerFactory
			.getLogger(AutoKnownHostsVerifier.class);
	private static SshClientManager sshClientManager;
	private static Configuration configuration;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(22);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
				"hostkey.ser"));

		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

			@Override
			public boolean authenticate(String paramString1,
					String paramString2, ServerSession paramServerSession) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		sshd.setShellFactory(new ProcessShellFactory(
				new String[] { "cmd.exe" }, EnumSet.of(
						ProcessShellFactory.TtyOptions.Echo,
						ProcessShellFactory.TtyOptions.ICrNl,
						ProcessShellFactory.TtyOptions.ONlCr)));

		try {
			sshd.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
*/
		readConfiguration();
	startSshClients();
		
		Server server = new Server(8080);
		
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
        webapp.setWar("webapp");
        
        HashLoginService loginService = new HashLoginService();
        loginService.putUser("root", Credential.getCredential("test"), new String[] {"user"});
        webapp.getSecurityHandler().setLoginService(loginService);
        
        server.setHandler(webapp);
        
        try {
			server.start();
	        server.join();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	public static void startSshClients() {
		logger.info("Starting ssh client sessions");
		sshClientManager = new SshClientManager(configuration.getSshClient());
		sshClientManager.start();
	}
	
	public static void stopSshClients() {
		sshClientManager.stop();
	}

	/**
	 * Read server configuration file.
	 * @return Configuration object.
	 */
	public static void readConfiguration() {
		try {
			File configFile = new File("config.xml");
			if (configFile.isFile()) {
				JAXBContext jc = JAXBContext
						.newInstance("de.flohrit.drillrig.config");
	
				Unmarshaller um = jc.createUnmarshaller();
				configuration = (Configuration) um
						.unmarshal(configFile);
			} else {
				configuration = new Configuration();
				writeConfiguration(configuration);
			}

		} catch (JAXBException e3) {
			logger.error("Error reading configuration file config.xml");
		}
	}

	/**
	 * Write configuration file config.xml
	 * @param configuration to write 
	 */
	public static void writeConfiguration(Configuration configuration) {
		try {
			JAXBContext jc = JAXBContext
					.newInstance("de.flohrit.drillrig.config");
			Marshaller m = jc.createMarshaller();
			SshClient sshClient = new SshClient();
			MachineAccount mAccount = new MachineAccount();
			mAccount.setHost("cgn-sam-01");
			mAccount.setUser("xflohrw");
			mAccount.setPassword("xflohrw123");
			sshClient.setMachineAccount(mAccount);

			Forward fwd = new Forward();
			fwd.setDescription("svn tunnel");
			fwd.setSHost("*");
			fwd.setSPort(13690);
			fwd.setRHost("cgn-sam-01");
			fwd.setRPort(3690);
			sshClient.getForward().add(fwd);

			configuration.getSshClient().add(sshClient);

			m.setProperty("jaxb.formatted.output", true);
			FileOutputStream config = new FileOutputStream("config.xml");
			m.marshal(configuration, config);
			config.close();

		} catch (Exception e) {
			logger.error("Error writing configuration file config.xml", e);
		}
	}
}
