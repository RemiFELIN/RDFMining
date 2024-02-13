package com.i3s.app.rdfminer.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * RDFminer-core server
 * @author Rémi Felin
 */
public class RDFminerServer {

    private static final String JERSEY_PACKAGES = "jersey.config.server.provider.packages";

    private static final String RDFMINER_WS_PATH = "com.i3s.app.rdfminer.server.ws";

    /**
     * Jetty server configurations and launcher
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // init server
        // Integer.parseInt(System.getenv("RDFMINER_CORE_PORT")
        int port = 8080;
        //
        Server server = new Server(port);
        //
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        //
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter(JERSEY_PACKAGES, RDFMINER_WS_PATH);
        //
        server.setHandler(context);
        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }

}
