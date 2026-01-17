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

package com.wmiles.servlet;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author william
 */
public class UltraKissServlet extends HttpServlet 
{
//   protected String host = "168.231.73.121" ;
   protected String host = "127.0.0.1" ;
   protected int maxsessions = 3 ;
   protected int port = 49152 ;
   protected boolean ssl = false ;
   protected int freeport ;

   /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
    * methods.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      
      int exitCode = StartUltraKiss(response) ;
      if (exitCode != 0) {
         response.setContentType("text/plain");
         return ;
      }

      String s = JettyLauncher.getHost() ;
      host = (s == null) ? "127.0.0.1"  : s ; 
      String ssltext = (ssl) ? "(SSL)" : "" ;
      System.out.println("Servlet now responding with HTML to client browser.");
      System.out.println("Host for UltraKiss client websocket is " + host) ;
      System.out.println("Port for UltraKiss client websocket is " + freeport) ;
      System.out.println("SSL for UltraKiss client websocket is " + ssl) ;
      response.reset() ;
      response.setContentType("text/html;charset=UTF-8");
      try (PrintWriter out = response.getWriter()) {
         out.println("<!DOCTYPE html>");
         out.println("<html>");
         out.println("<head>");
         out.println("<title>UltraKiss Servlet "+ssltext+"</title>");
         out.println("<style>");
         out.println("body {");
         out.println("margin: 0;");
         out.println("overflow: hidden; /* Prevent scrollbars */");
         out.println("}");
         out.println("canvas {");
         out.println("display: block; /* Remove extra space below canvas */");
         out.println("}");
         out.println("</style>");
         out.println("</head>");
         out.println("<body>");         
         out.println("<div><div id='custom-alert' style='display: none; padding: 15px; background-color: white; color: black; border: 2px solid red; position: absolute; width: fit-content; left: 0; right: 0; margin-inline: auto; top: 30%; z-index: 1000;'>");
         out.println("<span id='alert-message'></span>");
         out.println("</div></div>");                  
         out.println("<input type='file' id='fileInput' style='display: none;' />");
         out.println("<canvas id='myCanvas'></canvas>");
         out.println("<script src='client.js' host='"+host+"' port='"+freeport+"' ssl='"+ssl+"'></script>");
         out.println("<script src='WebAudioFontPlayer.js'></script>");
         out.println("<script src='MIDIPlayer.js'></script>");
         out.println("<script src='MIDIFile.js'></script>");
         out.println("</body>");
         out.println("</html>");
      }
   }
   
   protected int StartUltraKiss(HttpServletResponse response) 
           throws IOException 
   {
      int exitCode ;
      
      String scriptPath = System.getProperty("user.dir") + "/startUltraKiss.sh"; 
      String pidPath = System.getProperty("user.dir") + "/pid.txt"; 
      System.out.println("StartUltraKiss, pid path = " + pidPath);
      System.out.println("StartUltraKiss, script path = " + scriptPath);
      
      try 
      {
         // Wait for existing invocations of UltraKiss to close on a browser 
         // refresh.  This frees up the port for reuse.
         try { Thread.currentThread().sleep(3000) ; }
         catch (InterruptedException e) { }
         
         int activeprocesses = cleanPidFile(pidPath) ;
         if (activeprocesses >= maxsessions || freeport < port)
         {            
            response.getWriter().println("Too many sessions.  Maximum number of concurrent sessions is " + maxsessions + ".") ;            
            return -1 ;
         }
         
         int display = freeport % 100 ;
         System.out.println("StartUltraKiss, port = " + freeport + " on display " + display);
         
         // Define the command and arguments as a list of strings
         // Use the absolute path to the script
         String sslvalue = (ssl) ? "true" : "false" ;
         ProcessBuilder processBuilder = new ProcessBuilder(scriptPath, String.valueOf(display), String.valueOf(freeport), sslvalue);
            
         // Optional: Set the working directory for the script
         // processBuilder.directory(new File("/opt/scripts/"));
            
         // Redirect standard error to standard output
         processBuilder.redirectErrorStream(true); 
         // Redirect output to a file.  Xvfb can confuse stream output.
         File file = File.createTempFile("UltraKiss-", ".out") ;
         processBuilder.redirectOutput(file) ;
            
         // Start the process
         Process process = processBuilder.start();
                       
         // Wait for the process to complete and get the exit code
         exitCode = process.waitFor();

         // Capture and read the output from the script.
         String line;
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
         StringBuilder output = new StringBuilder();
         while ((line = reader.readLine()) != null) {
             output.append(line).append("\n");
             System.out.println(line);
         }
         reader.close();
         
         // Write the output to the servlet response.
         response.getWriter().println("StartUltraKiss script executed with exit code: " + exitCode);
         response.getWriter().println(output.toString());

      } 
      catch (IOException | InterruptedException e) 
      {
         response.getWriter().println("Error executing script: " + e.getMessage());
         e.printStackTrace();
         exitCode = -1 ;
      }    
      return exitCode ;
   }
   
   private int cleanPidFile(String filename)
   {
      freeport = 0 ;
      int activeprocesses = 0 ;
      Integer[] ports = new Integer[maxsessions] ;
      for (int i = 0; i < maxsessions; i++) ports[i] = port+i ;
      List<Integer> list = new ArrayList<>(Arrays.asList(ports)) ;
      
      System.out.println(" ");
      System.out.println("Servlet cleaning PID file...");
      File f = new File(filename) ;
      if (!f.exists()) 
      {
         System.out.println("Servlet system process PID file does not exist. " + f.getPath());
         System.out.println("Servlet active UltraKiss processes = " + activeprocesses);
         freeport = (list.size() > 0) ? list.get(0) : 0 ;
         System.out.println("Servlet free port = " + freeport);
         return activeprocesses ;
      }
      System.out.println("Servlet system process PID file exists. " + f.getPath());
         
      String content = "";
      BufferedReader reader = null;
      java.io.FileWriter writer = null;
      try      
      {
         Long PID = 0l ;
         int port ;
         String line ;
         reader = new BufferedReader(new FileReader(f));
           
         //Reading all the lines of input text file into oldContent
         while ((line = reader.readLine()) != null) 
         { 
            String [] tokens = line.split(" ") ;
            try { PID = Long.valueOf(tokens[0]) ; }
            catch (NumberFormatException e) { continue ; }
            if (!isProcessRunning(PID))
            {
               System.out.println("Servlet UltraKiss process PID " + tokens[0] + " removed as a running process.");
               continue ;
            }
            else
            {
               System.out.println("Servlet UltraKiss process PID " + tokens[0] + " is active as a running process.");               
            }
            content = content + line + System.lineSeparator();
            activeprocesses++ ;
            
            try { port = Integer.parseInt(tokens[1]) ; }
            catch (NumberFormatException e) { continue ; }
            list.remove(Integer.valueOf(port)) ;
         }

         if (content.isEmpty())
         {
            f.delete() ;
            System.out.println("Servlet PID file deleted.");
         }
         else
         {
            writer = new java.io.FileWriter(f);
            writer.write(content);     
         }
         
         // first available open port
         freeport = (list.size() > 0) ? list.get(0) : 0 ;
         System.out.println("Servlet free port = " + freeport);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         try { if (reader != null) reader.close() ; } catch (IOException e) { }
         try { if (writer != null) writer.close() ; } catch (IOException e) { }
      }  
      
      return activeprocesses ;
   }    
   
   public static boolean isProcessRunning(long pid) 
   {
      Optional<ProcessHandle> handle = ProcessHandle.of(pid);
      if (handle.isPresent()) 
      {
         // Check if the process is still alive
         return handle.get().isAlive();
      }
      return false;
   }
      

   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   /**
    * Handles the HTTP <code>GET</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      ssl = request.isSecure() ;
      processRequest(request, response);
   }

   /**
    * Handles the HTTP <code>POST</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
      processRequest(request, response);
   }

   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   @Override
   public String getServletInfo() {
      return "Short description";
   }// </editor-fold>

}
