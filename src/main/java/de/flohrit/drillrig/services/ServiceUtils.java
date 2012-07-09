package de.flohrit.drillrig.services;

public class ServiceUtils {
	
	public static ServiceResponse createOKResponse(String msg) {
		ServiceResponse sr = new ServiceResponse();
		ServiceStatus ss = new ServiceStatus();
		ss.setCode("OK");
		if (msg != null) {
			ss.getMsg().add(msg);
		}
		sr.setServiceStatus(ss);
		return sr;
	}

	public static ServiceResponse createNOKResponse(String msg) {
		ServiceResponse sr = new ServiceResponse();
		ServiceStatus ss = new ServiceStatus();
		ss.setCode("NOK");
		if (msg != null) {
			ss.getMsg().add(msg);
		}
		sr.setServiceStatus(ss);
		return sr;
	}
}
