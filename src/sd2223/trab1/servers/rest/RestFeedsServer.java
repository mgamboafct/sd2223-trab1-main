package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.discovery.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

public class RestFeedsServer {

    private static Logger Log = Logger.getLogger(RestFeedsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8090;
    public static final String SERVICE = "feeds";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        String domain = args[0];
        long base = Long.parseLong(args[1]);
        Log.info("Creating REST feeds server with domain : " + domain);

        try {

            ResourceConfig config = new ResourceConfig();
            config.register(new RestFeedsResource(domain, base));
            // config.register(CustomLoggingFilter.class);

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            // More code can be executed here...
            Discovery discovery = Discovery.getInstance();
            discovery.announce(domain, SERVICE, serverURI);
        } catch (Exception e) {
            Log.severe(e.getMessage());
        }

    }


}
