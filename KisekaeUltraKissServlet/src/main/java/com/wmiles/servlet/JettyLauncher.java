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

//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//


// This is the main program to listen on ports 8080 and 8443 for HTTP/1 
// connection requests from a client browser.  We use a Jetty servlet
// that responds to the connection.  The servlet page (UltraKissServlet)
// containd the HTML and Javascript code to deliver the canvas object 
// to maintan the UltraKiss screen in the browser.
//
// This listener program runs in the background as a server thread.  
//
// It manages connections.  We allow for up to 3 simultaneous instances
// of UltraKiss.  Each instance communicates over a web socket on ports
// 49152, 49153, and 49154.
//
// SSL connections require a certificate.  We use the Let's Encrypt certificate
// on the server.


package com.wmiles.servlet;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class JettyLauncher
{
   static URL baseResource = null ;
   static String host = "127.0.0.1" ;
   static String port = "8080" ;
   static String sslport = "8443" ;
   static String keystore = "" ;
   // The "keystore.p12" certificate file would typically reside in the
   // same directory from which this Java program jar file is run.
   // The default directory is System.getProperty("user.dir") ; 

   public static void main(String[] args) throws Exception
   {
      int httpport, httpsport ;
      Server server = new Server();
      if (args.length > 0) host = args[0]; else host = "127.0.0.1" ;
      if (args.length > 1) port = args[1]; else port = "8080" ;
      if (args.length > 2) sslport = args[2]; else sslport = "8443" ;
      if (args.length > 3) keystore = args[3]; else keystore = System.getProperty("user.dir") ;
      try { httpport = Integer.parseInt(port) ; }
      catch (NumberFormatException e) { httpport = 8080 ; }
      try { httpsport = Integer.parseInt(sslport) ; }
      catch (NumberFormatException e) { httpsport = 8443 ; }
      System.out.println("JettyLauncher: server created.");
      System.out.println("JettyLauncher: host (argument 1) is " + host);
      System.out.println("JettyLauncher: port (argument 2) is " + httpport);
      System.out.println("JettyLauncher: ssl port (argument 3) is " + httpsport);
      System.out.println("JettyLauncher: keystore (argument 4) is " + keystore);

      // Setup for SSL
      
      // 1. Configure the HTTP part (optional, typically for redirection)
      HttpConfiguration http = new HttpConfiguration();
      ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(http));
      httpConnector.setPort(httpport);
      server.addConnector(httpConnector);

      // 2. Configure the HTTPS part
      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer(new SecureRequestCustomizer());

      SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
      sslContextFactory.setKeyStorePath(keystore+"/keystore.p12");
      sslContextFactory.setKeyStorePassword("kisekaeultrakiss");
      sslContextFactory.setKeyManagerPassword("kisekaeultrakiss"); // Often the same as keystore password
      sslContextFactory.setKeyStoreType("PKCS12"); // Set the keystore type if not default

      // 3. Create the SSL Connector      
      ConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "http/1.1");
      ConnectionFactory httpConnectionFactory = new HttpConnectionFactory(https);

      ServerConnector httpsConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
      httpsConnector.setPort(httpsport);
      server.addConnector(httpsConnector);

      // 4. Figure out what path to serve content from
      ClassLoader cl = JettyLauncher.class.getClassLoader();
      // We look for a file, as ClassLoader.getResource() is not
      // designed to look for directories (we resolve the directory later)
      baseResource = cl.getResource("static-root/hello.html");
      System.out.println("JettyLauncher: base resource URL for static-root/hello.html is:\n" + baseResource);
      if (baseResource == null)
         throw new RuntimeException("JettyLauncher: Unable to find resource directory.");

      // Resolve file to directory
      URI webRootUri = baseResource.toURI().resolve("./").normalize();

      // 5. Setup the basic application "context" for this application at "/"
      // This is also known as the handler tree (in jetty speak)
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      context.setBaseResource(ResourceFactory.of(context).newResource(webRootUri));
      server.setHandler(context);
      ServletHolder servletHolder = new ServletHolder(new UltraKissServlet());
      context.addServlet(servletHolder,"/ultrakiss");

      // Lastly, the default servlet for root content (always needed, to satisfy servlet spec)
      // It is important that this is last.
      ServletHolder holderDef = new ServletHolder("default", org.eclipse.jetty.ee10.servlet.DefaultServlet.class);
      holderDef.setInitParameter("dirAllowed", "false");
      context.addServlet(holderDef, "/");

      server.start();
      server.join();
   }
    
   public static String getBaseResourceDirectory()
   {
      if (baseResource == null) return null ;
      File f = new File(baseResource.getPath()) ;
      return f.getParent() ;
   }
   
   public static String getHost() { return host ; }
}