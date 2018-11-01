/**
 * 
 */
package org.bluelamar.wsruler.svr;

import org.bluelamar.wsruler.*;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * @author mark
 *
 */
public class WsSvrContainer {

	WsSvrHandler handler;
    
	/**
	 * 
	 */
	public WsSvrContainer(WsSvrHandler handler) {
		this.handler = handler;
	}
	
	public void run() {
        try {
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMaxThreads(16);

            Server server = new Server(8080);
            //Server server = new Server(threadPool);
            ServletContextHandler handler = new ServletContextHandler();
            handler.setContextPath("");
            //ResourceConfig config = new ResourceConfig(WsSvrResources.class).register(new WsSvrBinder());
            ResourceConfig config = new WsSvr();
            handler.addServlet(new ServletHolder(new ServletContainer(config)), "/*");
            server.setHandler(handler);
            
            //HttpConfiguration httpConfig = new HttpConfiguration();
         
            server.start();
            server.join();
        } catch (Exception e) {
            System.err.println("*** " + e);
        }
	}

}
