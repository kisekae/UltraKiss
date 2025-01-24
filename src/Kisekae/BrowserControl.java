package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.4  (May 11, 2023)
// Copyright:    Copyright (c) 2002-2023
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



/**
* BrowserControl class
*
* Purpose:
*
* This class contains platform dependent code to open the default web browser
* if we are running as an application.
*
*/


import java.awt.Desktop;
import java.io.* ;
import java.net.URL ;
import java.lang.reflect.Method ;
import javax.swing.JOptionPane ;


public class BrowserControl
{
   // Used to identify the windows platform.
   private static final String WIN_ID = "Windows";
   // Used to identify the apple platform.
   private static final String MAC_ID = "MAC";
   // The default system browser under windows.
   private static final String WIN_PATH = "rundll32";
   // The flag to display a url.
   private static final String WIN_HTML_FLAG = "url.dll,FileProtocolHandler";
   // The flag to display mail.
   private static final String WIN_MAIL_FLAG = "url.dll,MailToProtocolHandler";
   // The default browser under unix.
   private static final String UNIX_PATH = "netscape";
   // The flag to display a url.
   private static final String MOZILLA_PATH = "mozilla";
   // The flag to display a url.
   private static final String SAFARI_PATH = "safari";
   // The flag to display a url.
   private static final String FIREFOX_PATH = "firefox";
   // The flag to display a url.
   private static final String UNIX_FLAG = "-remote openURL";
   // Set if security prohibits access to system properties.
   private static boolean secure = false ;
   // Set to known exec command.
   private static String execcmd = null ;

   /**
   * Display a file in the system browser.  If you want to display a
   * file, you must include the absolute path name.
   *
   * @param url the file's url (the url must start with either "http://" or
   * "https://" or"file://").
   */

   public static void displayURL(String url)
   {
      secure = false ;
      String cmd = null ;
      Process p = null ;
      int exitCode = -1 ;
      boolean apple = isApplePlatform() ;
      if (secure) return ;
      boolean windows = isWindowsPlatform() ;
      if (secure) return ;
      boolean unix = isUnixPlatform() ;
      if (secure) return ;

      try
      {
         if (Kisekae.isWebswing())
         {
            try
            {
               String s = url.trim() ;
               if (s.length() == 0) url = "https://www.google.com" ;
               URL testURL = new URL(url) ;
               Desktop.getDesktop().browse(testURL.toURI()); // roll your own link launcher or use Desktop if J6+
               return ;
            }
            catch (Exception ex) 
            { 
               throw new KissException("Unable to launch browser in Webswing, "+ ex.getMessage()) ;
            }
         }
         
         // Apply known exec() command.
         
         if (execcmd != null)
         {
            exitCode = applyCommand(execcmd + url) ;
            if (exitCode >= 0) return ;
         }

         // Apple system

         if (apple)
         {
            Object browser = OptionsDialog.getBrowser() ;
            if (browser != null && !"".equals(browser.toString()))
            {
               if (OptionsDialog.getDebugControl())
                  PrintLn.println("BrowserControl: apple cmd = [open,-a,"+browser.toString()+"," + url + "]");
               String[] commandLine3 = { "open", "-a", browser.toString(), url };
               try { p = Runtime.getRuntime().exec(commandLine3); }
               catch (Throwable x)
               {
                  if (OptionsDialog.getDebugControl())
                      PrintLn.println("BrowserControl: error starting browser, " + x) ;
                  p = null ;
               }
               exitCode = (p != null) ? p.waitFor() : -1 ;
            }
            
            if (exitCode != 0)
            {
               try
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.println("BrowserControl: apple command FileManager.OpenURL()") ;
                  Class fileMgr = Class.forName("com.apple.eio.FileManager") ;
                  Method openURL = fileMgr.getDeclaredMethod("OpenURL",new Class[] {String.class}) ;
                  openURL.invoke(null,new Object[] {url}) ;
                  exitCode = 0 ;
               }
               catch (Throwable e1)
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.println("BrowserControl: error starting browser, " + e1) ;
               }
            }
            
            if (exitCode != 0)
            {
               try
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.println("BrowserControl: apple command MRJFileUtils.OpenURL()") ;
                  Class fileMgr = Class.forName("com.apple.mrj.MRJFileUtils") ;
                  Method openURL = fileMgr.getDeclaredMethod("OpenURL",new Class[] {String.class}) ;
                  openURL.invoke(null,new Object[] {url}) ;
                  exitCode = 0 ;
               }
               catch (Throwable e2)
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.println("BrowserControl: error starting browser, " + e2) ;
               }
            }
               
            if (exitCode != 0)
            {
               if (OptionsDialog.getDebugControl())
                  PrintLn.printErr("BrowserControl: apple cmd = [open,-a,Safari.app," + url + "]");
               String[] commandLine1 = { "open", "-a", "Safari.app", url };
               try { p = Runtime.getRuntime().exec(commandLine1); }
               catch (Throwable x)
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.printErr("BrowserControl: error starting browser, " + x) ;
                  p = null ;
               }
               exitCode = (p != null) ? p.waitFor() : -1 ;
            }
                  
            if (exitCode != 0)
            {
               if (OptionsDialog.getDebugControl())
                  PrintLn.printErr("BrowserControl: apple cmd = [open,-a,Firefox.app," + url + "]");
               String[] commandLine2 = { "open", "-a", "Firefox.app", url };
               try { p = Runtime.getRuntime().exec(commandLine2); }
               catch (Throwable x)
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.printErr("BrowserControl: error starting browser, " + x) ;
                  p = null ;
               }
               exitCode = (p != null) ? p.waitFor() : -1 ;
            }
                  
            if (exitCode != 0)
            {
               if (OptionsDialog.getDebugControl())
                  PrintLn.printErr("BrowserControl: apple cmd = [open,-a,Internet Explorer.app," + url + "]");
               String[] commandLine3 = { "open", "-a", "Internet Explorer.app", url };
               try { p = Runtime.getRuntime().exec(commandLine3); }
               catch (Throwable x)
               {
                  if (OptionsDialog.getDebugControl())
                     PrintLn.printErr("BrowserControl: error starting browser, " + x) ;
                  p = null ;
               }
               exitCode = (p != null) ? p.waitFor() : -1 ;
            }
                  
            if (exitCode != 0)
            {
               // Command failed, start up the browser
               // cmd = 'safari http://www.javaworld.com'
               cmd = SAFARI_PATH + " " + url ;
               if (OptionsDialog.getDebugControl())
                  PrintLn.printErr("BrowserControl: apple cmd = " + cmd);
               exitCode = applyCommand(cmd) ;
            }
         }
            
         // Windows system

         else if (windows)
         {
            String s1 = "iexplore.exe" ;
            Object o = OptionsDialog.getBrowser() ;
            if (o != null && "".equals(o.toString())) o = null ;
            if (o != null) s1 = o.toString() ;

            // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
            // cmd = 'cmd.exe /c start http://...'
            if (url.length() == 0) url = s1 ;
            if (url.startsWith("file:")) 
            {
               String s = url.substring(5) ;
               int i = s.indexOf(':') ;
               if (i > 0) url = "file:///" + s.substring(i-1) ;
            }
            cmd = WIN_PATH + " " + WIN_HTML_FLAG + " " + url ;
            int i = url.indexOf("//") ;
            if (i > 0) i = url.indexOf("/",i) ;
            if (i > 0)
            {
               if (Kisekae.inApplet() || o != null)
                  cmd = "cmd.exe /c start " + s1 + " " + url ;
               else
                  cmd = "cmd.exe /c start " + url ;
            }
            if (url.startsWith("mailto:"))
               cmd = WIN_PATH + " " + WIN_MAIL_FLAG + " " + url ;
            exitCode = applyCommand(cmd) ;

            // Look for 16 bit Windows systems.
            
            String os = System.getProperty("os.name");
            boolean oldwin = (os.indexOf("95") > 0) ;
            oldwin |= (os.indexOf("98") > 0) ;
            if (exitCode != 0 && oldwin)
            {
               cmd = "command.com /c start " + url ;
               exitCode = applyCommand(cmd) ;
            }
         }

         // Unix system

         else if (unix)
         {
            String s1 = null ;
            Object o = OptionsDialog.getBrowser() ;
            if (o != null && "".equals(o.toString())) o = null ;
            if (o != null) s1 = o.toString() ;
            
            // Under Unix, Netscape has to be running for the "-remote"
            // command to work.  So, we try sending the command and
            // check for an exit value.  If the exit command is 0,
            // it worked, otherwise we need to start the browser.
            // cmd = 'netscape -remote openURL(http://www.javaworld.com)'

            if (s1 == null || s1.equals(UNIX_PATH))
            {
               cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
               exitCode = applyCommand(cmd) ;
            
               if (exitCode != 0)
               {
                  // Command failed, start up the browser
                  // cmd = 'netscape http://www.javaworld.com'
                  cmd = UNIX_PATH + " " + url ;
                  exitCode = applyCommand(cmd) ;
               }
            }
            
            if (exitCode != 0 && s1 != null)
            {
               // Command failed, start up the preferred browser
               cmd = s1 + " " + url ;
               exitCode = applyCommand(cmd) ;
            }
               
            if (exitCode != 0)
            {
               // Command failed, start up the browser
               // cmd = 'mozilla http://www.javaworld.com'
               cmd = MOZILLA_PATH + " " + url ;
               exitCode = applyCommand(cmd) ;
            }
               
            if (exitCode != 0)
            {
               // Command failed, start up the browser
               // cmd = 'firefox http://www.javaworld.com'
               cmd = FIREFOX_PATH + " " + url ;
               exitCode = applyCommand(cmd) ;
            }
         }
            
         // Unknown system

         else 
         {
            PrintLn.println("BrowserControl: unknown OS " + System.getProperty("os.name"));
         }
         
         // All done.  If it worked, retain the working command.
         
         if (exitCode == 0) 
         {
            if (cmd.endsWith(url))
               execcmd = cmd.substring(0,cmd.indexOf(url)) ;
         }
      }

      // Watch for an exception.

      catch (SecurityException e)
      {
         secure = true ;
         PrintLn.println("BrowserControl: Security exception. Unable to open browser.") ;
         PrintLn.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("BrowserNotAvailable"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }
      catch(Exception x)
      {
         // couldn't exec browser
         PrintLn.println("BrowserControl: Exception " + x);
         if (OptionsDialog.getDebugControl()) x.printStackTrace() ;
      }
   }

   /**
   * Try to determine whether this application is running under Windows
   * or some other platform by examining the "os.name" property.
   *
   * @return true if this application is running under a Windows OS
   */

   public static boolean isWindowsPlatform()
   {
      try
      {
         String os = System.getProperty("os.name");
         if (os != null) os = os.toLowerCase() ;
         String id = WIN_ID.toLowerCase() ;
         if (os != null && os.startsWith(id))
            return true;
         else
            return false;
      }
      catch (SecurityException e)
      {
         secure = true ;
         PrintLn.println("BrowserControl: Security exception. Unable to access system properties.") ;
         PrintLn.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("BrowserNotAvailable"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
         return false ;
      }
   }

   /**
   * Try to determine whether this application is running under Apple
   * or some other platform by examining the "os.name" property.
   *
   * @return true if this application is running under an Apple OS
   */

   public static boolean isApplePlatform()
   {
      try
      {
         if (OptionsDialog.getAppleMac()) return true ;
         String os = System.getProperty("os.name");
         if (os != null) os = os.toLowerCase() ;
         String id = MAC_ID.toLowerCase() ;
         if (os != null && os.startsWith(id))
            return true;
         else
            return false;
      }
      catch (SecurityException e)
      {
         secure = true ;
         PrintLn.println("BrowserControl: Security exception. Unable to access system properties.") ;
         PrintLn.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("BrowserNotAvailable"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
         return false ;
      }
   }
   
   /**
   * Try to determine whether this application is running under Unix.
   * This is a default situation.
   *
   * @return true if this application is running under UNIX
   */

   public static boolean isUnixPlatform()
   {
      return (!isWindowsPlatform() && !isApplePlatform()) ;
   }

   
   private static int applyCommand(String cmd)
   {
      Process p = null ;
      
      if (OptionsDialog.getDebugControl())
         PrintLn.println("BrowserControl: command " + cmd) ;
      try { p = Runtime.getRuntime().exec(cmd) ; }
      catch (IOException x)
      {
         if (OptionsDialog.getDebugControl())
            PrintLn.println("BrowserControl: error starting browser, " + x) ;
         p = null ;
      }
      
      // Wait for exit code -- if it's 0, command worked,
      
      try { return ((p != null) ? p.waitFor() : -1) ; }
      catch (InterruptedException e) { return -1 ; }
   }

   
   public static void reset()
   {
      execcmd = null ;
   }
}
