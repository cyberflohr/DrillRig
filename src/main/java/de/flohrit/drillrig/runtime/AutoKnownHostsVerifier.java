package de.flohrit.drillrig.runtime;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoKnownHostsVerifier extends OpenSSHKnownHosts {
	final static private Logger logger = LoggerFactory
			.getLogger(AutoKnownHostsVerifier.class);

	public AutoKnownHostsVerifier(File khFile)
			throws IOException {

		super(khFile);
	}

	protected boolean hostKeyUnverifiableAction(String hostname, PublicKey key) {
		KeyType type = KeyType.fromKey(key);
		logger.info(
				"The authenticity of host {} can't be established. {} key fingerprint is {}",
				new Object[] { hostname, type,
						SecurityUtils.getFingerprint(key) });

		try {
			entries().add(
					new OpenSSHKnownHosts.SimpleEntry(null, hostname, KeyType
							.fromKey(key), key));
			write();
			logger.info("Permanently added '{}' ({}) to the list of known host",
					new Object[] { hostname, type });
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	protected boolean hostKeyChangedAction(OpenSSHKnownHosts.HostEntry entry,
			String hostname, PublicKey key) {
		KeyType type = KeyType.fromKey(key);
		String fp = SecurityUtils.getFingerprint(key);
		String path = getFile().getAbsolutePath();
		logger.warn(
				"REMOTE HOST IDENTIFICATION HAS CHANGED! The fingerprint for the {} key sent by the remote host is {}. Please add correct host key in {} to get rid of this message.",
				new Object[] { type, fp, path });

		return false;
	}

}
