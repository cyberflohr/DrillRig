package de.flohrit.drillrig.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

import de.flohrit.drillrig.config.Configuration;

public class StringEncDecoder {

	private SecretKeySpec secret;

	public StringEncDecoder(Configuration cfg) {
		String secretStr = cfg.getId().substring(cfg.getId().length()-16);
		 secret = new SecretKeySpec(secretStr.getBytes(), "AES");
	}
	
	public String encrypt(String input) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			cipher.init(Cipher.ENCRYPT_MODE, secret);

			return new String(Base64.encode(cipher.doFinal(input.getBytes())));

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String decrypt(String input) {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			cipher.init(Cipher.DECRYPT_MODE, secret);

	        byte[] decrypted = cipher.doFinal(Base64.decode(input));  
	        return  new String(decrypted);
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
