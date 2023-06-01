package sd2223.trab1.servers.soap;


import jakarta.xml.ws.Endpoint;
import sd2223.trab1.discovery.Discovery;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapUsersServer {

	public static final int PORT = 8081;
	public static final String SERVICE_NAME = "users";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

	public static void main(String[] args) throws Exception {
		String domain = args[0];
		
//		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		Log.setLevel(Level.INFO);

		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		Endpoint.publish(serverURI, new SoapUsersWebService(domain));

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));

		Discovery discovery = Discovery.getInstance();
		discovery.announce(domain, SERVICE_NAME, serverURI);
	}
}
