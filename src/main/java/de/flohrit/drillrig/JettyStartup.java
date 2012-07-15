package de.flohrit.drillrig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyStartup {
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

		//reloadServer();
		// writeConfiguration(configuration);

		Server server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/");
		if (new File("webapp").isDirectory()) {
			webapp.setWar("webapp/");
		} else {
			File tmpdir = new File(System.getProperty("java.io.tmpdir")
					+ "/drillrig");
			if (tmpdir.isDirectory()) {
				deleteDirectory(tmpdir.getAbsoluteFile());
			}
			tmpdir.mkdir();

			InputStream warRes = JettyStartup.class
					.getResourceAsStream("/drillrig-1.0.war");
			try {
				File warFile = new File(tmpdir.getAbsoluteFile()
						+ "/drillrig-1.0.war");
				copyFile(warRes, warFile);
				webapp.setWar(warFile.getAbsolutePath());
				// extractJar(warFile.getAbsolutePath(),
				// tmpdir.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			webapp.setTempDirectory(tmpdir);
			webapp.setCopyWebDir(true);
			webapp.setCopyWebInf(true);
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
	public static HashLoginService getLoginService() {
		if (loginService == null) {
			loginService = new HashLoginService() {

				@Override
				public UserIdentity login(String username, Object credentials) {
					return super.login(
							username,username);
											
				}

			};
			loginService.putUser("admin", Credential.getCredential("admin"), new String[] { "user"});
			/*
			for (User user : getConfiguration().getSecurity().getUser()) {
				if (user.isEnabled()) {
					loginService.putUser(user.getName(),
							Credential.getCredential(user.getPassword()),
							(String[]) user.getRole().toArray(new String[0]));
				}*/
			
		}

		return loginService;
	}
	static public void copyFile(InputStream is, File dest) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is, 8192);
		BufferedOutputStream  bos = new BufferedOutputStream(new FileOutputStream(dest), 8192);
		while (bis.available() > 0) { // write contents of 'is' to 'fos'
			bos.write(bis.read());
		}
		bos.close();
		is.close();
		bis.close();
	}

	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	static public void extractJar(String jarFile, String destDir)
			throws IOException {
		JarFile jar = new JarFile(jarFile);
		Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			JarEntry file = (JarEntry) entries.nextElement();
			File f = new File(destDir + File.separator + file.getName());
			if (file.isDirectory()) { // if its a directory, create it
				f.mkdir();
				continue;
			}
			InputStream is = jar.getInputStream(file); // get the input stream
			copyFile(is, f);
		}
	}
}
