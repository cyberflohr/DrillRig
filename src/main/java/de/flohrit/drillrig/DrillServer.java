package de.flohrit.drillrig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flohrit.drillrig.config.Configuration;
import de.flohrit.drillrig.config.Security;
import de.flohrit.drillrig.config.User;
import de.flohrit.drillrig.runtime.AutoKnownHostsVerifier;
import de.flohrit.drillrig.runtime.SshClientManager;
import de.flohrit.drillrig.util.StringEncDecoder;
import de.flohrit.drillrig.util.StringUtils;

public class DrillServer {
	final static private Logger logger = LoggerFactory
			.getLogger(AutoKnownHostsVerifier.class);

	private static SshClientManager sshClientManager;
	private static Configuration configuration;
	private static StringEncDecoder encDecoder;
	private static HashLoginService loginService;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * SshServer sshd = SshServer.setUpDefaultServer(); sshd.setPort(22);
		 * sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
		 * "hostkey.ser"));
		 * 
		 * sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
		 * 
		 * @Override public boolean authenticate(String paramString1, String
		 * paramString2, ServerSession paramServerSession) { // TODO
		 * Auto-generated method stub return true; } });
		 * sshd.setShellFactory(new ProcessShellFactory( new String[] {
		 * "cmd.exe" }, EnumSet.of( ProcessShellFactory.TtyOptions.Echo,
		 * ProcessShellFactory.TtyOptions.ICrNl,
		 * ProcessShellFactory.TtyOptions.ONlCr)));
		 * 
		 * try { sshd.start(); } catch (IOException e) { e.printStackTrace(); }
		 */

		reloadServer();
		// writeConfiguration(configuration);

		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		if (new File("webapp").isDirectory()) {
			webapp.setWar("webapp");
		} else {
			File tmpdir = new File(System.getProperty("java.io.tmpdir") + "/drillrig");
			if (tmpdir.isDirectory()) {
				deleteDirectory(tmpdir.getAbsoluteFile()); 
				tmpdir.mkdir();
			}
			
			webapp.setTempDirectory(tmpdir);
			webapp.setWar("rsrc:internal.war");
			webapp.setCopyWebDir(true);
			webapp.setCopyWebInf(true);
			webapp.setParentLoaderPriority(true);
		}

		webapp.getSecurityHandler().setLoginService(getLoginService());
		server.setHandler(webapp);

		try {
			server.start();
			server.join();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }	

	public static String getAnonymizedString(String val, int readable) {
		if (val.length() - readable <= 0) {
			return val;
		}

		byte[] bytes = val.getBytes();
		Arrays.fill(bytes, 0, val.length() - readable, "*".getBytes()[0]);
		return new String(bytes);
	}

	public static SshClientManager getSshClientManager() {
		return sshClientManager;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Read server configuration file.
	 * 
	 * @return Configuration object.
	 */
	public static void readConfiguration() {
		try {
			File configFile = new File("config.xml");
			if (configFile.isFile()) {
				JAXBContext jc = JAXBContext
						.newInstance("de.flohrit.drillrig.config");

				Unmarshaller um = jc.createUnmarshaller();

				Configuration newConfiguration = (Configuration) um
						.unmarshal(configFile);
				configuration = newConfiguration;

			} else {
				configuration = new Configuration();

				GregorianCalendar gcal = new GregorianCalendar();
				XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(gcal);

				configuration.setCreationDate(xgcal);
				configuration.setModificationDate(xgcal);
				configuration.setVersion("0.1");
				configuration.setId(StringUtils.createUUID());

				User user = new User();
				user.setName("admin");
				user.setEmail("admin@localhost");
				user.setEnabled(true);
				user.setPassword("");
				user.getRole().add("admin");
				user.getRole().add("user");

				configuration.setSecurity(new Security());
				configuration.getSecurity().getUser().add(user);

				writeConfiguration(configuration);
			}

		} catch (JAXBException e3) {
			logger.error("Error reading configuration file config.xml", e3);
		} catch (DatatypeConfigurationException e) {
			logger.error("XMLGregorianCalendar not found", e);
		}
	}

	/**
	 * 
	 */
	public static void reloadServer() {

		if (sshClientManager != null) {
			sshClientManager.stop();
		}

		readConfiguration();

		sshClientManager = new SshClientManager(configuration.getSshClient());
		sshClientManager.start();
	}

	/**
	 * Write configuration file config.xml
	 * 
	 * @param configuration
	 *            to write
	 */
	public static void writeConfiguration(Configuration configuration) {
		File configFile = new File("config.xml");
		File backFile = new File("config.xml.tmp");
		FileOutputStream config = null;

		try {
			JAXBContext jc = JAXBContext
					.newInstance("de.flohrit.drillrig.config");
			Marshaller m = jc.createMarshaller();

			m.setProperty("jaxb.formatted.output", true);

			config = new FileOutputStream(backFile);
			m.marshal(configuration, config);
			config.close();

			if (!configFile.exists() || configFile.delete()) {
				backFile.renameTo(configFile);
			}

		} catch (Exception e) {
			logger.error("Error writing configuration file config.xml", e);
		} finally {
			if (config != null) {
				try {
					config.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	public static StringEncDecoder getEncDecorder() {
		if (encDecoder == null) {
			encDecoder = new StringEncDecoder(getConfiguration());
		}

		return encDecoder;
	}

	public static HashLoginService getLoginService() {
		if (loginService == null) {
			loginService = new HashLoginService() {

				@Override
				public UserIdentity login(String username, Object credentials) {
					return super.login(
							username,
							"".equals(credentials) ? credentials
									: getEncDecorder().encrypt(
											(String) credentials));
				}

			};
			for (User user : getConfiguration().getSecurity().getUser()) {
				if (user.isEnabled()) {
					loginService.putUser(user.getName(),
							Credential.getCredential(user.getPassword()),
							(String[]) user.getRole().toArray(new String[0]));
				}
			}
		}

		return loginService;
	}
}
