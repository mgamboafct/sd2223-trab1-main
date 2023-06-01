package sd2223.trab1.clients;

import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.clients.soap.SoapUsersClient;
import sd2223.trab1.discovery.Discovery;

import java.net.URI;

public class UsersClientFactory {

	private static final String REST = "/rest";
	private static final String SOAP = "/soap";
	public static final String SERVICE = "users";

	public static Users get(String domain) {
		Discovery discovery = Discovery.getInstance();
		URI serverURI = discovery.getUriOf(SERVICE, domain);
		var uriString = serverURI.toString();

		if (uriString.endsWith(REST))
			return new RestUsersClient(serverURI);
		else if (uriString.endsWith(SOAP))
			return new SoapUsersClient(serverURI);
		else
			throw new RuntimeException("Unknown service type..." + uriString);
	}
}
