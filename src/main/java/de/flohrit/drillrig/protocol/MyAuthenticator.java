package de.flohrit.drillrig.protocol;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {

		//getRequestingHost():
			
		return new PasswordAuthentication("", "".toCharArray());
	}
}
