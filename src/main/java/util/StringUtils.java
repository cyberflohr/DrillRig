package util;

import java.util.UUID;

public class StringUtils {

	public static String createUUID() {
		return "ID" + UUID.randomUUID().toString().replace('-','_');
	}
}
