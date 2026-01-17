// Title:        Kisekae UltraKiss
// Version:      5.0 (December 25, 2025)
// Copyright:    Copyright (c) 2002-2025
// Author:       William Miles
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

/*
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  This copyright notice and this permission notice shall be included in      %
%  all copies or substantial portions of UltraKiss.                           %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  William Miles be liable for any claim, damages or other liability,         %
%  whether in an action of contract, tort or otherwise, arising from, out of  %
%  or in connection with Kisekae UltraKiss or the use of UltraKiss.           %
%                                                                             %
%  William Miles                                                              %
%  144 Oakmount Rd. S.W.                                                      %
%  Calgary, Alberta                                                           %
%  Canada  T2V 4X4                                                            %
%                                                                             %
%  w.miles@wmiles.com                                                         %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*/

package com.wmiles.kisekaeultrakiss.WebSocket;

import com.wmiles.kisekaeultrakiss.Kisekae.Kisekae;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeHandler;

/**
 *
 * @author william
 */
public class JettyServer extends Thread
{  
   final JettyWebSocketEndpoint wsendpoint = new JettyWebSocketEndpoint() ;
   int port ;
   boolean ssl ;
	private long createtime = 0 ;			   // server activation time

   public JettyServer(int port, boolean ssl)
   {
      this.port = port ;
      this.ssl = ssl ;
   }
   
   public void run() 
   {
      Server server = null ;
      String endpoint = "/ultrakiss" ;
   	createtime = System.currentTimeMillis() ;
      
      Date dateInUTC = new Date() ;
      String mountainTimeZoneId = "America/Denver";
      SimpleDateFormat sdfMountain = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
      sdfMountain.setTimeZone(TimeZone.getTimeZone(mountainTimeZoneId));
      String dateInMountainTime = sdfMountain.format(dateInUTC);
      
      System.out.println("JettyServer thread starts, " + dateInMountainTime) ;
      System.out.println("JettyServer port = " + port + " ssl = " + ssl) ;
      Thread me = Thread.currentThread() ;
		me.setPriority(Thread.NORM_PRIORITY) ;
		me.setName("Jetty") ;

      try 
      {
         // Create a Server with a ServerConnector listening on port 49152-49155.
        
         if (ssl)
         {
            server = new Server() ;
         
            // 1. Configure the SslContextFactory
            // The "keystore.p12" certificate file would typically reside in the
            // same directory from which this Java program jar file is run.
            // The default directory is System.getProperty("user.dir") ; 
            String keystore = System.getProperty("user.dir") ;
            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(keystore+"/keystore.p12");
            sslContextFactory.setKeyStorePassword("kisekaeultrakiss");
            sslContextFactory.setKeyManagerPassword("kisekaeultrakiss"); // Often the same as keystore password
            sslContextFactory.setKeyStoreType("PKCS12"); // Set the keystore type if not default

            // 2. Configure HttpConfiguration for secure requests
            HttpConfiguration httpsConfig = new HttpConfiguration();
            httpsConfig.addCustomizer(new SecureRequestCustomizer()); // Adds TLS info to request attributes

            // 3. Create the secure connector
            ServerConnector sslConnector = new ServerConnector(server,
               new SslConnectionFactory(sslContextFactory, "http/1.1"),
               new HttpConnectionFactory(httpsConfig));
            sslConnector.setPort(port); // Standard port for HTTPS/WSS
            server.addConnector(sslConnector);        
            System.out.println("JettyServer new secure server created on port " + port) ;
         }
         else
         {
            server = new Server(port);
            System.out.println("JettyServer new server created on port " + port) ;
         }         

         // Create a ContextHandler with the given context path.
         ContextHandler contextHandler = new ContextHandler("/");
         System.out.println("JettyServer new contextHandler created") ;
         server.setHandler(contextHandler);

         // Create a WebSocketUpgradeHandler that implicitly creates a ServerWebSocketContainer.
         WebSocketUpgradeHandler webSocketHandler = WebSocketUpgradeHandler.from(server, contextHandler, container ->
         {
            // Configure the ServerWebSocketContainer.
            container.setMaxTextMessageSize(128 * 1024);

            // Map a request URI to a WebSocket endpoint, for example using a regexp.
            container.addMapping(endpoint, (rq, rs, cb) -> wsendpoint);
         });
         contextHandler.setHandler(webSocketHandler);
         System.out.println("JettyServer new webSocketHandler created for endpoint \"" + endpoint + "\"") ;

         // Starting the Server will start the ContextHandler and the WebSocketUpgradeHandler,
         // which would run the configuration of the ServerWebSocketContainer.
         server.start();  
         String s = server.getServerInfo() ;
         System.out.println("JettyServer server started, " + s) ;
      } 
      catch (Exception e) 
      {
      	Runnable runner = new Runnable()
			{ 
            public void run() 
            { 
               JOptionPane.showMessageDialog(null, e.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE); 
            } 
         } ;
   		javax.swing.SwingUtilities.invokeLater(runner) ;  
			e.printStackTrace();
         server = null ;
		}
   } 
   
   public JettyWebSocketEndpoint getEndpoint() { return wsendpoint ; }
   
   public long getCreateTime() { return createtime ; }
}
