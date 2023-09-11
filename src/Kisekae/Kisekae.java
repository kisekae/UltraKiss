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
 * @(#)Kisekae.java	96/09/12
 *
 * Copyright (c) William Miles.  All Rights Reserved.
 *
 */


import java.awt.* ;
import java.awt.event.* ;
import java.awt.print.* ; 
import java.awt.image.* ;
import java.applet.* ;
import java.io.* ;
import java.net.Authenticator ;
import java.util.* ;
import java.text.DateFormat ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.swing.* ;
import javax.swing.text.* ;


/**                     Kisekae UltraKiss Version 3.4
 *
 * This applet drags transparent images in an applet window.  The
 * images are cels which are sequenced in overlay order, so that one
 * cel may overlay another.
 *
 */


public class Kisekae extends Applet
   implements Runnable, ActionListener
{
   // Security variables

   private static String copyright = 
      "Kisekae UltraKiss V3.6.1 (c) 2002-2023 William Miles" ;
   private static Object authorize = null ;        // Seigen module
   private static Calendar warningdate = null ;    // Secure warning
   private static Calendar expiredate = null ;     // Licence expire
   private static Calendar restrictdate = null ;   // Actual restriction
   private static Calendar currentdate = null ;    // Today's date
   private static Calendar builddate = null ;      // Build date

   // Run time variables

   private static String website = "https://github.com/kisekae/UltraKiss/" ; // Web site
   private static String splashname = "Images/splash.gif" ; // Splash image
   private static String webstart = "HTML/KissWeb.html" ; // Portal page
   private static String splashdir = "Splash/hubble.jpg" ; // Splash dir
   private static String demoindex = "Demo/index.html" ; // Demo index file
   private static String readmeindex = "ReadMe/index.html" ; // ReadMe index
   private static String icondir = "Icons/icon.gif" ; // Icon dir
   private static String icondir1 = "Images/icon.gif" ; // Default icon
   private static String tipsapp = "Tips/tip.html" ; // Tips dir
   private static String tipsindex = "Tips/index.txt" ; // Tips index
   private static String packagename = "Kisekae" ;	// Name of our package
   private static String encoding = null ;         // The language encoding
   private static Locale locale = null ;           // The current locale
   private static ResourceBundle captions = null ; // The language resources
   private static Locale defaultlocale = null ;    // The default locale
   private static URL codebase = null ;				// The start directory base
   private static URL loadcodebase = null ;			// The applet code base
   private static URL iconImageURL = null ;			// The product icon URL
   private static URL iconImage16URL = null ;		// The product small icon URL
   private static URL splashImageURL = null ;		// The product splash URL
   private static AppletContext context = null ;	// The applet context
   private static MainFrame mainframe = null ;		// Our main window frame
   private static Kisekae kisekae = null ;		   // Our own reference 
   private static JWindow splashwindow = null ;		// Our load splash window
   private static KissFrame batchframe = null ;	   // Our batch load frame
   private static ImageIcon icon = null ;				// Our product icon
   private static TipsBox tips = null ;			   // Our Tips system
   private static ImageIcon icon16 = null ;			// Our product small icon
   private static ImageIcon splash = null ;			// Our product splash icon
   private static Image iconimage = null ;	 		// Our product icon image
   private static Image iconimage16 = null ;	 		// Our product small icon 
   private static Image splashimage = null ;	 		// Our product splash image
   private static String file = null ;             // Our command line file arg
   private static String directory = null ;        // Our command line dir arg
   private static String language = "" ;           // Our command line lang arg
   private static String kissweb = "http://" ;     // Our command line web arg
   private static int maxdownload = 1024 ;         // Maximum download size KB

   // State variables

   private static boolean helpinstalled = true ;	// True if help installed
   private static boolean mediainstalled = true ;	// True if media installed
   private static boolean jaiinstalled = true ;    // True if media installed
   private static boolean searchinstalled = true ; // True if search installed
   private static boolean volatileimages = true ;  // True if Java 1.4 installed
   private static boolean printinstalled = true ;	// True if printer installed
   private static boolean jlayerinstalled = true ;	// True if mp3 installed
   private static boolean registered = false ;		// True if registered
   private static boolean inapplet = true ;			// True if run as applet
   private static boolean secure = false ;		   // True if in sandbox
   private static boolean debug = false ;		      // True if debug required
   private static boolean batch = false ;          // True if run batch mode
   private static boolean accept = false ;         // True if cnf errors OK
   private static boolean loaded = false ;         // True if set was loaded
   private static boolean manualexpire = false ;   // True if set is expired
   private static boolean tipsinstalled = true ;   // True if tips system
   private boolean suspended = false ;					// True if applet suspended
   private boolean error = false ;						// True if error occured

   // Environment variables

   private Toolkit toolkit = null ;						// Our environment toolkit
   private Thread engine = null ;						// Our thread and groups
   private String statusMsg = null ;					// The current status message

   // Our callback button to signal configuration load completion.

   private static JButton callback = null ;        // Our batch callback


   // Constructor

   public Kisekae()
   {
      authorize() ;
      kisekae = this ;

      // Open the log file.

      LogFile.start() ;
      builddate = Calendar.getInstance() ;
      builddate.set(2023,9-1,10) ;
      
      // Restore the properties.
      
      defaultlocale = Locale.getDefault() ;
      OptionsDialog.loadPropertyOptions() ;
      directory = OptionsDialog.getUserDir() ;

      // Establish our default codebase and file encoding.

      initCodeBase() ;
      initEncoding() ;
      callback = new JButton("Kisekae Callback") ;
   }


   // The init method is called by the browser when the applet
   // is loaded and before the browser calls the start method.
   // We need to read and decode the applet parameters whenever
   // the applet is initialized.

   public void init()
   {
      ClassLoader cl = getClass().getClassLoader() ;
//    ClassLoader cl = Thread.currentThread().getContextClassLoader() ; // Trusted Library
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
      
      // Check for Apple system.
      
      try
      {
         String s = System.getProperty("os.name") ;
         s = s.toLowerCase() ;
         if (s.indexOf("mac") >= 0)
         {
            OptionsDialog.setAppleMac(true) ;
            try 
            { 
               String jver = System.getProperty("java.version");
               System.setProperty(jver.startsWith("1.3")?
                  "com.apple.macos.useScreenMenuBar"
                  : "apple.laf.useScreenMenuBar", "true");
               System.out.println("Apple system useScreenMenuBar set") ;
            }
            catch (Exception ex) 
            { 
               System.err.println("Kisekae: Unable to set Apple system useScreenMenuBar") ;
            }
         }
         if (s.indexOf("linux") >= 0)
         {
            OptionsDialog.setLinux(true) ; 
         }
      }
      catch (Throwable e) 
      { 
         System.err.println("Kisekae: Unable to determine host OS") ;
      }

      // Set the user interface to come up in the required Look and Feel.

      if (!OptionsDialog.getAppleMac())
         if (OptionsDialog.getSystemLF())
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         else
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

      // Get the load splash image.  Display the splash pane only
      // if we are performing a new initialization.

      if (mainframe == null)
      {
         try
         {
            splashImageURL = getResource(splashname) ;
            if (splashImageURL != null)
            {
               splash = new ImageIcon(splashImageURL) ;
               if (splash.getImageLoadStatus() == MediaTracker.COMPLETE)
                  splashimage = splash.getImage() ;
               else
                  throw new Exception("splash image load status " + splash.getImageLoadStatus()) ;
            }

            // Create a splash panel.

            if (splashimage != null)
            {
               int w = splashimage.getWidth(null) ;
               int h = splashimage.getHeight(null) ;
               int x = ((d.width - w) / 2) ;
               int y = ((d.height - h) / 2) ;
               splashwindow = new JWindow() ;
               JPanel panel = new JPanel() ;
               panel.setLayout(new BorderLayout()) ;
               JLabel splashlabel = new JLabel(splash) ;
               panel.add(splashlabel,BorderLayout.CENTER) ;
               JProgressBar progress = new JProgressBar() ;
               progress.setIndeterminate(true) ;
               progress.setBorderPainted(true) ;
               panel.add(progress,BorderLayout.SOUTH) ;
               splashwindow.getContentPane().add(panel) ;
               splashwindow.setSize(w,h) ;
               splashwindow.setLocation(x,y) ;
               splashwindow.setVisible(true) ;
            }
         }
         catch (Throwable e)
         {
            String s = ((splashImageURL != null) ? splashImageURL.toExternalForm() : "") ;
            System.err.println("Kisekae: Error loading splash image " + s) ;
         }
      }

      // Set the initial language font.

      try
      {
         Locale locale = getCurrentLocale() ;
         setLanguageFont(locale) ;
      }
      catch (Throwable e)
      {
         System.err.println("Kisekae: Error setting language font for locale " + locale.toString()) ;
      }

      // Determine if UltraKiss is registered.

      String registration = getRegistration() ;
      registered = !("".equals(registration)) ;
      if (!isLicensed())
      {
         String s = "\nThe evaluation period expires on "
            + DateFormat.getDateInstance().format(expiredate.getTime()) ;
         if (isExpired()) s = "" ;
         if (isRestricted()) s = "\nThe evaluation period has expired."
            + "\nPlease obtain a new product release." ;
         if (isRestricted())
         {
            JOptionPane.showMessageDialog(null,
                "This is an evaluation release of Kisekae UltraKiss."
                + s + "\n" + website,
                "Warning", JOptionPane.ERROR_MESSAGE) ;
         }
      }

      // Get the product icon image.  There is a small icon (16x16) and a large
      // icon (80x80).  We use the small icon for the frame icons and the large 
      // icon for display.

      try
      {
         String s = OptionsDialog.getIconDir() ;
         int n = OptionsDialog.getIconNumber() ;
         int m = s.indexOf('.') ;
         if (m > 0) 
         {
            String s1 = s.substring(0,m) + n + s.substring(m) ;
            iconImageURL = getResource(s1) ;
            s1 = s.substring(0,m) + n + "-32" + s.substring(m) ;
            if (iconImageURL == null)
               iconImageURL = getResource(s1) ;
            if (iconImageURL == null)
               iconImageURL = getResource(icondir1) ;
            if (iconImageURL != null)
               icon = new ImageIcon(iconImageURL) ;
         }
      }
      catch (Throwable e)
      {
         String s = ((iconImageURL != null) ? iconImageURL.toExternalForm() : "") ;
         System.err.println("Kisekae: Error loading icon " + s + " " + e.getMessage()) ;
      }

      // Get the small product icon image.  

      try
      {
         String s = OptionsDialog.getIconDir() ;
         int n = OptionsDialog.getIconNumber() ;
         int m = s.indexOf('.') ;
         if (m > 0)
         {
            String s1 = s.substring(0,m) + n + "-16" + s.substring(m) ;
            iconImage16URL = getResource(s1) ;
         }
         if (iconImage16URL == null)
         {
            s = icondir1 ;
            m = s.indexOf('.') ;
            if (m > 0)
            {
               String s1 = s.substring(0,m) + "-16" + s.substring(m) ;
               iconImage16URL = getResource(s1) ;
            }
         }
         if (iconImage16URL != null)
         {
            icon16 = new ImageIcon(iconImage16URL) ;
            if (icon16.getImageLoadStatus() == MediaTracker.COMPLETE)
               iconimage16 = icon16.getImage() ;
         }
      }
      catch (Throwable e)
      {
         String s = ((iconImage16URL != null) ? iconImage16URL.toExternalForm() : "") ;
         System.err.println("Kisekae: Error loading icon " + s + " " + e.getMessage()) ;
      }

      // Determine if Java 1.4 hardware image acceleration is available.

      try { cl.loadClass("java.awt.image.VolatileImage") ; }
      catch (Throwable e)
      {
         volatileimages = false ;
         System.err.println("Java Volatile Images are not available.") ;
      }

      // Determine if the Web Search extensions are available.

      try 
      { 
          cl.loadClass("WebSearch.WebSearchFrame") ; 
          Package websearch = Package.getPackage("WebSearch") ;
          if (websearch == null) 
          {
              searchinstalled = false ;
              System.err.println("Web Search package not found.") ;
          }
      }
      catch (Throwable e)
      {
         searchinstalled = false ;
         System.err.println("Web Search extensions are not installed.") ;
      }

      // Determine if the Help extension package is available.

      try 
      { 
          cl.loadClass("javax.help.HelpSet") ; 
          Package help = Package.getPackage("javax.help") ;
          if (help == null) 
          {
              helpinstalled = false ;
              System.err.println("Java Help package not found.") ;
          }
      }
      catch (Throwable e)
      {
         helpinstalled = false ;
         System.err.println("Java Help extensions are not installed.") ;
      }

      // Determine if the MP3 extension package is available.

      try 
      { 
          cl.loadClass("javazoom.jl.decoder.Decoder") ; 
          Package jlayer = Package.getPackage("javazoom.jl.decoder") ;
          if (jlayer == null) 
          {
              jlayerinstalled = false ;
              System.err.println("JLayer MP3 package not found.") ;
          }
      }
      catch (Throwable e)
      {
         jlayerinstalled = false ;
         System.err.println("JLayer MP3 extensions are not installed.") ;
      }

      // Determine if the Advanced Imaging extension package is available.

      try 
      { 
          cl.loadClass("javax.media.jai.JAI") ; 
          Package imaging = Package.getPackage("javax.media.jai") ;
          if (imaging == null) 
          {
              jaiinstalled = false ;
//            System.err.println("Java Advanced Imaging package not found.") ;
          }
      }
      catch (Throwable e)
      {
         jaiinstalled = false ;
//       System.err.println("Java Advanced Imaging extensions are not installed.") ;
      }

      // Determine if the Media Framework extension package is available.

      try 
      { 
          cl.loadClass("javax.media.Manager") ; 
          Package media = Package.getPackage("javax.media") ;
          if (media == null) 
          {
              mediainstalled = false ;
              System.err.println("Java Media Framework package not found.") ;
          }
      }
      catch (Throwable e)
      {
         mediainstalled = false ;
         System.err.println("Java Media Framework extensions are not installed.") ;
      }

      // Determine the Portal Web start page.

      try
      {
         URL weburl = getResource(webstart) ;
         if (weburl != null) kissweb = weburl.toString() ;
         else throw new Exception() ;
         OptionsDialog.setKissWeb(kissweb) ;
         OptionsDialog.setInitKissWeb(kissweb) ;
      }
      catch (Throwable e)
      {
         System.err.println("Application web portal is not available.") ;
      }

      // Determine if the background splash image exists.

      try
      {
         int m = splashdir.indexOf('.') ;
         String s1 = splashdir.substring(0,m) + "1" + splashdir.substring(m) ;
         URL backImage = getResource(s1) ;
         if (backImage == null) splashdir = null ;
      }
      catch (Throwable e)
      {
         splashdir = null ;
      }
      finally
      {
//         OptionsDialog.setSplashDir(splashdir) ;
      }
      
      
      // Determine if the Tips system is available.

      try
      {
         String s = Kisekae.getTipsApp() ;
         int m = s.indexOf('.') ;
         s = s.substring(0,m) + "0" + s.substring(m) ;
         URL tips = getResource(s) ;
         if (tips == null && tipsinstalled)
            throw new Exception("unable to establish Tips resource " + s) ;
      }
      catch (Throwable e)
      {
         tipsinstalled = false ;
         System.err.println("Tips system is not available.") ;
      }

      // Determine if the demo system is available.

      try
      {
         URL demo = (demoindex != null) ? getResource(demoindex) : null ;
         if (demo == null && demoindex != null) 
            throw new Exception("unable to establish Demo resource " + demoindex) ;
      }
      catch (Throwable e)
      {
         demoindex = null ;
         System.err.println("Demonstration system is not available.") ;
      }

      // Determine if the ReadMe system is available.

      try
      {
         URL readme = (readmeindex != null) ? getResource(readmeindex) : null ;
         if (readme == null && readmeindex != null)
            throw new Exception("unable to establish ReadMe resource " + readmeindex) ;
      }
      catch (Throwable e)
      {
         readmeindex = null ;
         System.err.println("UltraKiss ReadMe files are not available.") ;
      }

      // Determine if printing is possible.  This can hang trying to reach
      // the print system.

      try
      {
         if (inapplet && secure) printinstalled = false ;
         if (!inapplet && secure) printinstalled = false ;
         if (printinstalled && !OptionsDialog.getAppleMac() && !inapplet)
         {
            JobAttributes jobAttr = new JobAttributes();
            jobAttr.setDestination(JobAttributes.DestinationType.PRINTER);
            jobAttr.setDialog(JobAttributes.DialogType.NONE);// no dialog
            Toolkit kit = Toolkit.getDefaultToolkit() ;
            PrintJob job = kit.getPrintJob(new Frame(),"printer",jobAttr,null);
            if (job == null)
               throw new Exception("unable to establish print capabilities") ;
         }
      }
      catch (Throwable e)
      {
         printinstalled = false ;
         System.err.println("Kisekae: Java system printer is not available.") ;
      }

      // Show a dialog if we are in debug mode.

      String s = "Kisekae UltraKiss program build date: " + getBuildDate() + "\n" ;
      System.out.print(s) ;
      if (debug && (!helpinstalled || !mediainstalled || !jaiinstalled ||
          !printinstalled || !volatileimages || !jlayerinstalled))
      {
         if (!helpinstalled)
            s += "Java Help product extensions are not installed.\n" ;
         if (!mediainstalled)
            s += "Java Media Framework product extensions are not installed.\n" ;
         if (!jlayerinstalled)
            s += "JLayer MP3 extensions are not installed.\n" ;
         if (!jaiinstalled)
            s += "Java Advanced Imaging product extensions are not installed.\n" ;
         if (!volatileimages)
            s += "Java 1.4 Volatile Images are not available.\n" ;
         if (!printinstalled)
            s += "Java System Printer is not available.\n" ;
         JOptionPane.showMessageDialog(null, s,
            "Warning", JOptionPane.ERROR_MESSAGE) ;
      }

      // Initialize the control objects for the program.

      if (!OptionsDialog.getJavaSound())
         OptionsDialog.setJavaSound(!mediainstalled) ;
      if (!volatileimages)
         OptionsDialog.setShowTips(false) ;
      toolkit = Toolkit.getDefaultToolkit() ;
      statusMsg = getAppletInfo() ;
      showStatus(statusMsg) ;

      // Initialize the specific applet variables.  Determine the initial
      // file parameter.  parameters are:
      //
      // file; either a URL or path name of an initial set to load
      // directory: the name of the default directory for file open
      // web: the URL of the home page for the UltraKiss Portal
      // website: the URL of the home page for online help
      // splash: the directory for the background splash images

      if (inapplet)
      {
         codebase = getCodeBase() ;
         loadcodebase = getCodeBase() ;
         file = getParameter("file") ;
         directory = getParameter("directory") ;
         
         s = getParameter("web") ;
         if (s != null) 
         { 
            try
            {
               URL testURL = new URL(s) ;
               URL loadURL = getLoadBase() ;
               String testpath = testURL.getPath() ;
               String loadpath = loadURL.getPath() ;
               String protocol = testURL.getProtocol() ;
               int i = testpath.indexOf(':') ;
               int j = loadpath.indexOf(':') ;
                  
               // Apply Windows drive specifier if missing
                  
               if (j > 0 && i < 0 && "file".equals(protocol.toLowerCase())) 
               {
                  testpath = loadpath.substring(0,j+1) + testpath ;
                  s = protocol + ":" + testpath ;
               }
                  
               kissweb = s ; 
               OptionsDialog.setKissWeb(s) ; 
            }
            catch (MalformedURLException e) 
            { 
               System.out.println("Invalid URL for application website: " + s) ;
            }
         }
         
         s = getParameter("website") ;
         if (s != null) 
         { 
            try
            {
               website = s ;
               URL testURL = new URL(s) ;
               System.out.println("System website is " + s) ;
            }
            catch (MalformedURLException e) 
            {
               if (s.trim().length() > 0)
                  System.out.println("Invalid URL for system website: " + s) ;
            }
         }
         
         s = getParameter("splash") ;
         if (s != null) { splashdir = s ; OptionsDialog.setSplashDir(s) ; }
         System.out.println("This application is running as an applet.") ;
         if (secure)
            System.out.println("This applet is running in a secure environment.") ;
         if (file != null && file.length() > 0)
            System.out.println("Application start file is " + file) ;
         if (kissweb != null)
            System.out.println("Application web site is " + kissweb) ;
         language = getParameter("language") ;
         if (language != null) setLanguage(language) ;

         // Define the browser applet screen area.

         if (mainframe == null)
         {
            Button run = new Button("Run") ;
            this.add(run,"Center") ;

            // Register for applet mouse events

            addMouseListener(new MouseEventHandler()) ;
            run.addActionListener(this) ;

            // Fire up the program.

            suspended = false ;
            mainframe = new MainFrame(this,file) ;
            if (splashwindow != null)
            {
               splashwindow.setVisible(false) ;
               splashwindow.dispose() ;
               splashwindow = null ;
               splashimage = null ;
               splash = null ;
            }
            mainframe.toFront() ;
         
            // Show tips if first time.
         
            if (OptionsDialog.getShowTips() && file == null && tipsinstalled)
            {
               tips = new TipsBox(mainframe,Kisekae.getCaptions().getString("TipsBoxTitle"),true) ;
               tips.show() ;
            }
         }
      }
   }


   // Authorize this execution.

   private void authorize()
   {
      try
      {
         authorize = new Seigen.Seigen() ;
         copyright = ((Seigen.Seigen) authorize).getCopyright() ;
         warningdate = ((Seigen.Seigen) authorize).getWarningDate() ;
         expiredate = ((Seigen.Seigen) authorize).getExpireDate() ;
         restrictdate = ((Seigen.Seigen) authorize).getRestrictDate() ;
         currentdate = Calendar.getInstance() ;

         // Create and register an Authenticator.

         Authenticator.setDefault((Authenticator) authorize);
      }

      catch (Throwable e)
      {
         warningdate = Calendar.getInstance() ;
         expiredate = Calendar.getInstance() ;
         restrictdate = Calendar.getInstance() ;
         currentdate = Calendar.getInstance() ;
         
         // Don't restrict on licence if running as secure applet
         
         warningdate.set(2099,Calendar.DECEMBER,31) ;
         expiredate.set(2099,Calendar.DECEMBER,31) ;
         restrictdate.set(2099,Calendar.DECEMBER,31) ;
      }
   }



   // This version of the init method is used to initiate a silent execution
   // of UltraKiss to verify the load of a specified file.  This method loads
   // the first configuration found in the file.  This interface is used by
   // the WebSearch tool.  This should not be invoked on the AWT thread.

   public void init(String file, int maxsize, KissFrame f)
   {
      batch = true ;
      batchframe = f ;
      maxdownload = maxsize ;
      registered = false ;
      helpinstalled = false ;
      jaiinstalled = false ;
      volatileimages = false ;

      // Activate the load.  Close any previously loaded set to
      // reduce the load memory requirements.

      if (mainframe == null)
         mainframe = new MainFrame(this,file) ;
      else
      {
         mainframe.closeframe() ;
         try {Thread.currentThread().sleep(300) ; }
         catch (InterruptedException e) { } 
         mainframe.loadfile(file) ;
      }
   }



   // This closes a silent execution of UltraKiss and reinitializes our state.  
   // This interface is used by the WebSearch tool.
   
   public void close()
   {
      batch = false ;
      batchframe = null ;
      if (mainframe != null)
         mainframe.closeconfig() ;
      init() ;
   }



   // Determine the default language encoding for file translation.
   // This also establishes if we are running in the secure sandbox.

   private static void initEncoding()
   {
      String initlocale = "" ;
      String initencoding = "" ;

      try
      {
         if (locale != null) initlocale = locale.getDisplayLanguage() ;
         if (encoding != null) initencoding = encoding ;
         if (locale == null) locale = Locale.getDefault() ;
         if (encoding == null) encoding = System.getProperty("file.encoding") ;
         try 
         { captions = ResourceBundle.getBundle("Kisekae/Messages",locale); }
         catch (Exception ex)
         { captions = ResourceBundle.getBundle("Messages",locale); }

         // Ensure that our OptionsDialog tracks this language setting.

         String s0 = locale.getLanguage() ;
         String s1 = getLanguageName(s0) ;
         OptionsDialog.setLanguage(s1);
         OptionsDialog.setEncoding(encoding) ;
      }
      catch (SecurityException e)
      {
         secure = true ;
         System.out.println("Kisekae: Security exception. Unable to access system properties.") ;
         System.out.println(e.getMessage()) ;
      }
      finally
      {
         if (captions == null)
         {
            try 
            { captions = ResourceBundle.getBundle("Kisekae/Messages"); }
            catch (Exception ex)
            { captions = ResourceBundle.getBundle("Messages"); }
         }
         String localename = (locale != null)
             ? locale.getDisplayLanguage() : "unknown" ;
         String encodingname = (encoding != null)
             ? encoding : "unknown" ;
         if (!initlocale.equals(localename) || !initencoding.equals(encoding))
         {
            System.out.println("Default language is " + localename
               + " with file encoding " + encodingname) ;
         }
      }
   }



   // Initialize the default code base.  This also determines if we are
   // running in an applet context.

   private void initCodeBase()
   {
      String initcbname = "" ;

      try
      {
         try { context = getAppletContext() ; }
         catch (Exception e) { context = null ; }
         initcbname = (codebase == null) ? "" : codebase.toString() ;

         directory = setBase(directory) ;
         if (!inapplet || context == null)
         {
            String userdir = System.getProperty("user.dir") ;
            if (!userdir.endsWith(File.separator))
               userdir += File.separator ;
            loadcodebase = new URL("file","",-1,userdir) ;
         }
      }

      // Watch for exceptions.

      catch (SecurityException e)
      {
         secure = true ;
         System.out.println("Kisekae: Security exception. Unable to obtain codebase.") ;
         System.out.println(e.toString()) ;
      }

      catch (MalformedURLException e)
      {
         System.out.println("Kisekae: Codebase directory is invalid. " + directory) ;
         System.out.println(e.toString()) ;
      }

      finally
      {
         String cbname = (codebase == null) ? "unknown" : codebase.toString() ;
         if (!cbname.equals(initcbname)) System.out.println("Application codebase is " + cbname) ;
      }
   }



   // Return references to the necessary program attributes.

   public static Kisekae getKisekae() { return kisekae ; }
   public static MainFrame getMainFrame() { return mainframe ; }
   public static KissFrame getBatchFrame() { return batchframe ; }
   public static boolean inApplet() { return inapplet ; }
   public static boolean isSecure() { return secure ; }
   public static boolean isBatch() { return batch ; }
   public static boolean isAccept() { return accept ; }
   public static boolean isLoaded() { return loaded ; }
   public static boolean isHelpInstalled() { return helpinstalled ; }
   public static boolean isMediaInstalled() { return mediainstalled ; }
   public static boolean isMP3Installed() { return jlayerinstalled ; }
   public static boolean isSearchInstalled() { return searchinstalled ; }
   public static boolean isPrintInstalled() { return printinstalled ; }
   public static boolean isVolatileImage() { return volatileimages ; }
   public static boolean isTipsInstalled() { return tipsinstalled ; }
   public static boolean isLicensed() { return currentdate.before(warningdate) ; }
   public static boolean isExpired() { return !currentdate.before(expiredate) || manualexpire ; }
   public static boolean isRestricted() { return currentdate.after(restrictdate) ; }
   public static ResourceBundle getCaptions() { return captions ; }
   static AppletContext getContext() { return context ; }
   static TipsBox getTipsBox() { return tips ; }
   static URL getBase() { return codebase ; }
   static URL getLoadBase() { return loadcodebase ; }
   static String getCopyright() { return copyright ; }
   static String getPackageName() { return packagename ; }
   static String getLanguage() { return (language != null) ? language : "English" ; }
   static String getLanguageEncoding() { return (encoding != null) ? encoding : "UTF-8" ; }
   static Locale getCurrentLocale() { return locale ; }
   static int getMaxDownload() { return maxdownload ; }
   static String getKissWeb() { return kissweb ; }
   static String getWebSite() { return website ; }
   static String getSplashDir() { return splashdir ; }
   static String getIconDir() { return icondir ; }
   static String getTipsApp() { return tipsapp ; }
   static String getTipsIndex() { return tipsindex ; }
   static String getDemoIndex() { return demoindex ; }
   static String getReadMeIndex() { return readmeindex ; }

   static String getFactorySplashDir() { return "Splash/hubble.jpg" ; }
   
   static ImageIcon getImageIcon() 
   { return icon ; }
   static Image getIconImage() 
   { return (iconimage16 != null) ? iconimage16 : iconimage ; }

   static String getBaseDir() 
   { return (codebase != null) ? codebase.getPath() : "" ; }
    static String getUser()
   { return (authorize != null) ? ((Seigen.Seigen) authorize).getUser() : "" ; }
   static String getPassword()
   { return (authorize != null) ? ((Seigen.Seigen) authorize).getPassword() : "" ; }
   static String getConnectionID()
   { return (authorize != null) ? ((Seigen.Seigen) authorize).getConnectionID() : "" ; }
   static String getRegistration()
   { return (authorize != null) ? ((Seigen.Seigen) authorize).getRegistration() : "" ; }
   static String getExpireDate()
   { return DateFormat.getDateInstance().format(expiredate.getTime()) ; }
   static String getBuildDate()
   { return DateFormat.getDateInstance().format(builddate.getTime()) ; }
   static String getUnencodedUser()
   { return (authorize != null) ? ((Seigen.Seigen) authorize).getUnencodedUser() : "" ; }

   static void setUser(String s)
   { if (authorize != null) ((Seigen.Seigen) authorize).setUser(s) ; }
   static void setPassword(String s)
   { if (authorize != null) ((Seigen.Seigen) authorize).setPassword(s) ; }

   static String getLanguageCode(Object o)
   {
      if (o == null) return "" ;
      String l = o.toString() ;
      if ("English".equalsIgnoreCase(l)) return "en" ;
      if ("Japanese".equalsIgnoreCase(l)) return "ja" ;
      if ("French".equalsIgnoreCase(l)) return "fr" ;
      if ("German".equalsIgnoreCase(l)) return "de" ;
      if ("Dutch".equalsIgnoreCase(l)) return "nl" ;
      if ("Italian".equalsIgnoreCase(l)) return "it" ;
      if ("Spanish".equalsIgnoreCase(l)) return "es" ;
      return "" ;
   }
   
   static String getCopyrightDate()
   {
      String s = getCopyright() ;
      if (s == null) return "" ;
      int n = s.indexOf("(c)") ;
      return (n < 0) ? s : s.substring(n) ;
   }
   
   static String getReleaseLevel()
   {
      String s = getCopyright() ;
      if (s == null) return "" ;
      int n = s.indexOf("(c)") ;
      return (n <= 0) ? s : s.substring(0,n-1) ;
   }



   // Set references to the necessary language locale.

   static void setCurrentLocale(Locale l)
   {
      Object initlanguage = OptionsDialog.getLanguage() ;
      try
      {
         if (l != null)
         {
            locale = l ;
            Locale.setDefault(locale) ;
            try 
            { captions = ResourceBundle.getBundle("Kisekae/Messages",locale); }
            catch (Exception ex)
            { captions = ResourceBundle.getBundle("Messages",locale); }
            // This seems to set language translation for Swing components.
            try { UIManager.setLookAndFeel(UIManager.getLookAndFeel()) ; }
            catch (UnsupportedLookAndFeelException ex) { }
            // This applies the new fonts to preallocated dialogs.
            setLanguageFont(locale) ;
            if (mainframe != null) mainframe.updateUI() ;

            // Ensure that our OptionsDialog tracks this language setting.

            String s0 = locale.getLanguage() ;
            String s1 = getLanguageName(s0) ;
            OptionsDialog.setLanguage(s1);
         }
      }
      catch (SecurityException e)
      {
         System.out.println("Kisekae: Security exception. Unable to set language locale.") ;
         System.out.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(null,
            captions.getString("SecurityException") + "\n" +
            captions.getString("LocaleNotAvailable"),
            captions.getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }
      finally
      {
         if (encoding != null && locale != null)
            if (!initlanguage.equals(locale.getLanguage()))
            {
               String s0 = locale.getLanguage() ;
               String s1 = getLanguageName(s0) ;
               if (s1 == null) s1 = s0 ;
               System.out.println("Language is " + s1 + " with file encoding " + encoding) ;
            }
      }
   }


   // Set references to the necessary language encoding.  The encoding string
   // is used during file I/O operations to specify the file encoding.

   static void setLanguageEncoding(Object o)
   {
      Object initencoding = OptionsDialog.getEncoding() ;
      try
      {
         if (o != null) encoding = o.toString() ;
         OptionsDialog.setEncoding(encoding) ;
      }
      catch (SecurityException e)
      {
         System.out.println("Kisekae: Security exception. Unable to set default file encoding.") ;
         System.out.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(null,
            captions.getString("SecurityException") + "\n" +
            captions.getString("LocaleNotAvailable"),
            captions.getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }
      finally
      {
         if (encoding != null && !encoding.equals(initencoding) && locale != null)
         {
            String s0 = locale.getLanguage() ;
            String s1 = getLanguageName(s0) ;
            if (s1 == null) s1 = s0 ;
            System.out.println("Language is " + s1 + " with file encoding " + encoding) ;
         }
      }
   }


   // Set font references to the necessary language encoding.  This allows
   // different fonts to be used that are different from those specified
   // in the font.properties file.  We default TextArea and List components
   // to a monospaced font.  This sets the initial font for the Text Editor.

   static void setLanguageFont(Locale l)
   {
      if (l == null) return ;
      String s0 = l.getLanguage() ;
      Locale dl = defaultlocale ;
      if (dl == null) dl = Locale.getDefault() ;
      String s1 = dl.getLanguage() ;
      Font f = null ;
      Font ms = null ;

      // For languages equivalent to our default locale we set all UI fonts to
      // the current L&F default fonts except for TextArea and List which are
      // set to a Monospaced font.

      if (s0.equals(s1))
      {
         ms = new Font("Monospaced", Font.PLAIN, 12) ;
         UIDefaults uid = UIManager.getLookAndFeelDefaults() ;
         UIManager.put("List.font", ms) ;
         UIManager.put("TextArea.font", ms) ;
         UIManager.put("TextField.font", uid.getFont("TextField.font")) ;
         UIManager.put("Label.font", uid.getFont("Label.font")) ;
         UIManager.put("Panel.font", uid.getFont("Panel.font")) ;
         UIManager.put("Button.font", uid.getFont("Button.font")) ;
         UIManager.put("Menu.font", uid.getFont("Menu.font")) ;
         UIManager.put("MenuItem.font", uid.getFont("MenuItem.font")) ;
         UIManager.put("CheckBoxMenuItem.font",uid.getFont("CheckBoxMenuItem.font")) ;
         UIManager.put("Table.font", uid.getFont("Table.font")) ;
         UIManager.put("TableHeader.font", uid.getFont("TableHeader.font")) ;
         UIManager.put("ComboBox.font", uid.getFont("ComboBox.font")) ;
         UIManager.put("CheckBox.font", uid.getFont("CheckBox.font")) ;
         UIManager.put("RadioButton.font", uid.getFont("CheckBox.font")) ;
         UIManager.put("ToolTip.font", uid.getFont("ToolTip.font")) ;
         UIManager.put("TabbedPane.font", uid.getFont("TabbedPane.font")) ;
         UIManager.put("TitledBorder.font", uid.getFont("TitledBorder.font")) ;
         return ;
      }

      // If our default locale is not Japanese assign a Japanese font.

      if ("ja".equalsIgnoreCase(s0))
      {
         char dou = '\u50cd' ;
         f = new Font("MS Gothic",Font.PLAIN,12) ;
         ms = new Font("MS Minto",Font.PLAIN,12) ;
         if (OptionsDialog.getAppleMac())
            ms = f = new Font("Osaka",Font.PLAIN,12) ;

         // Search for an appropriate Japanese font. This search is slow.

         if (f == null || !f.canDisplay(dou))
         {
            String [] fonts = { "Dialog", "Serif", "SansSerif", "Monospaced" } ;
            GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
            try { fonts = g.getAvailableFontFamilyNames(l) ; }
            catch (Throwable e) { } 
            for (int i = 0 ; i < fonts.length ; i++)
            {
               f = new Font(fonts[i], Font.PLAIN, 12) ;
               if (f.canDisplay(dou)) break ;
               f = null ;
            }
         }

         String s2 = getLanguageName(s0) ;
         if (s2 == null) s2 = s0 ;
         if (f != null)
            System.out.println("Language is " + s2 + " with font " + f.getName()) ;
         else
         {
            System.out.println("Language is " + s2 + " with no font installed.") ;
            return ;
         }

         // Assign the Japanese font.

         if (ms == null || !ms.canDisplay(dou)) ms = f ;
         UIManager.put("List.font", ms) ;
         UIManager.put("TextArea.font", ms) ;
         UIManager.put("Label.font", f) ;
         UIManager.put("Panel.font", f) ;
         UIManager.put("Button.font", f) ;
         UIManager.put("Menu.font", f) ;
         UIManager.put("MenuItem.font", f) ;
         UIManager.put("CheckBoxMenuItem.font", f) ;
         UIManager.put("TextField.font", f) ;
         UIManager.put("Table.font", f) ;
         UIManager.put("TableHeader.font", f) ;
         UIManager.put("ComboBox.font", f) ;
         UIManager.put("CheckBox.font", f) ;
         UIManager.put("RadioButton.font", f) ;
         UIManager.put("ToolTip.font", f) ;
         UIManager.put("TabbedPane.font", f) ;
         UIManager.put("TitledBorder.font", f) ;
         return ;
      }

      // If our default locale is not European assign a European font.

      if ("en".equalsIgnoreCase(s0))
      {
         Font fe12 = new Font("Times Roman",Font.PLAIN,12) ;
         Font fems12 = new Font("Courier",Font.PLAIN,12) ;
         String s2 = getLanguageName(s0) ;
         if (s2 == null) s2 = s0 ;

         if (fe12 != null && fems12 != null)
            System.out.println("Language is " + s2 + " with font " + fe12.getName()) ;
         else
         {
            System.out.println("Language is " + s2 + " with no font installed.") ;
            return ;
         }

         // Assign the European font.

         UIManager.put("List.font", fems12) ;
         UIManager.put("TextArea.font", fems12) ;
         UIManager.put("Label.font", fe12) ;
         UIManager.put("Panel.font", fe12) ;
         UIManager.put("Button.font", fe12) ;
         UIManager.put("Menu.font", fe12) ;
         UIManager.put("MenuItem.font", fe12) ;
         UIManager.put("CheckBoxMenuItem.font", fe12) ;
         UIManager.put("TextField.font", fe12) ;
         UIManager.put("Table.font", fe12) ;
         UIManager.put("TableHeader.font", fe12) ;
         UIManager.put("ComboBox.font", fe12) ;
         UIManager.put("CheckBox.font", fe12) ;
         UIManager.put("RadioButton.font", fe12) ;
         UIManager.put("ToolTip.font", fe12) ;
         UIManager.put("TabbedPane.font", fe12) ;
         UIManager.put("TitledBorder.font", fe12) ;
         return ;
      }
   }


   // Set the start language.  This is currently based upon the first
   // character of the language code.

   static void setLanguage(String l)
   {
      if (secure) return ;
      if (l == null || l.length() == 0) return ;
      language = getLanguageName(l) ;

      if ("en".equals(l))
      {
         setCurrentLocale(Locale.ENGLISH) ;
         setLanguageEncoding("Cp1252") ;
      }

      else if ("ja".equals(l))
      {
         setCurrentLocale(Locale.JAPANESE) ;
         setLanguageEncoding("SJIS") ;
      }

      else if ("fr".equals(l))
      {
         setCurrentLocale(Locale.FRENCH) ;
         setLanguageEncoding("Cp1252") ;
      }

      else if ("de".equals(l))
      {
         setCurrentLocale(Locale.GERMAN) ;
         setLanguageEncoding("Cp1252") ;
      }

      else if ("nl".equals(l))
      {
         setCurrentLocale(new Locale("nl")) ;
         setLanguageEncoding("Cp1252") ;
      }

      else if ("it".equals(l))
      {
         setCurrentLocale(Locale.ITALIAN) ;
         setLanguageEncoding("Cp1252") ;
      }

      else if ("es".equals(l))
      {
         setCurrentLocale(new Locale("es")) ;
         setLanguageEncoding("Cp1252") ;
      }

      else return ;

      // Set the OptionsDialog initial field values.

      OptionsDialog.setLanguage(language) ;
      OptionsDialog.setEncoding(encoding) ;
      OptionsDialog.setInitOptions() ;
   }
   
   
   static void setLookAndFeel(String lookAndFeelName)
   {
      try
      {
         //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         UIManager.setLookAndFeel(lookAndFeelName) ;
         int i = lookAndFeelName.lastIndexOf('.') ;
         String s = (i < 0) ? lookAndFeelName : lookAndFeelName.substring(i+1) ;
         System.out.println("Look & Feel installed successfully, " + s);
      }
      catch (Exception e)
      {
         System.err.println("Kisekae: could not install Look & Feel " + lookAndFeelName + ", " + e.getMessage());
      }
   }
   
   
   // Get the language name given the language code.
   
   static String getLanguageName(String l)
   {
      if (l == null) return null ;
      String s = l.toLowerCase() ;
      if ("en".equals(s)) return "English" ;
      else if ("ja".equals(s)) return "Japanese" ;
      else if ("fr".equals(s)) return "French" ;
      else if ("de".equals(s)) return "German" ;
      else if ("nl".equals(s)) return "Dutch" ;
      else if ("it".equals(s)) return "Italian" ;
      else if ("es".equals(s)) return "Spanish" ;
      else return null ;
   }
   
   // Java 1.5.0_16 jar: url bug workaround
   
   public static URL getResource(String name) 
   {
      try
      {
         if (name == null) return null ;
         URL url = Kisekae.class.getClassLoader().getResource(name);
         if (url != null && "jar".equalsIgnoreCase(url.getProtocol()) && url.toExternalForm().indexOf("!/") == -1) 
         {
            String urlString = "jar:" + Kisekae.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm() + "!/" + name;
            if (!urlString.equals(url.toExternalForm())) 
               url = new URL(urlString);        //if new URL string is different, instantiate it
         }
         return url;
      }
      catch (Exception e)
      {
         System.out.println("Kisekae: unable to obtain resource: " + name) ;
         e.printStackTrace() ;
      }
      return null ;
   }


   
   // Set the default code base.
   
   static String setBase(String directory)
   {
      if (inapplet) 
         return (codebase != null) ? codebase.getPath() : "" ;
 
      try
      {
         String userdir = System.getProperty("user.dir") ;
         if (!userdir.endsWith(File.separator)) userdir += File.separator ;
         if (directory == null) directory = userdir ;
         else
         {
            String s = directory.toLowerCase() ;
            if (s.startsWith("file:")) 
               directory = directory.substring(5) ;
            File f = new File(directory) ;
            directory = (f.exists()) ? f.getAbsolutePath() : userdir ;
         }
         if (!directory.endsWith(File.separator))
            directory += File.separator ;
      
         // Set the directory base.
      
         codebase = new URL("file","",-1,directory) ; 
         OptionsDialog.setUserDir(codebase.getPath()) ;
      }
      catch (Exception e) 
      {
         directory = "" ;
         if (codebase != null) directory = codebase.getPath() ;
      }
      return directory ;
   }
   
   
   // Set the icon and icon image.
   
   static void setImageIcon(ImageIcon icn)
   {
      icon = icn ;
      if (icon != null) iconimage = icon.getImage() ;
      if (OptionsDialog.getDebugControl())
         System.out.println("Kisekae: new Icon Image set.") ;
   }
   
   static void setImageIcon16(ImageIcon icn)
   {
      icon16 = icn ;
      if (icon16 != null) iconimage16 = icon16.getImage() ;
   }
 
   static void setIconDir(String s) 
   { 
      icondir = s ; 
      OptionsDialog.setIconDir(s) ;
   }
   
   static void setSplashDir(String s) 
   { 
      splashdir = s ; 
      OptionsDialog.setSplashDir(s) ;
   }
   
   static void setSecure(boolean b) 
   {
      manualexpire = b ; 
   }
   
   static void setAccept(boolean b) 
   {
      accept = b ; 
   }



   // The following methods are defined for batch loads of KiSS sets.
   // ---------------------------------------------------------------

   // Set our set loaded state.   This fires a callback request if a callback
   // was registered.

   static void setLoaded(boolean b)
   {
      loaded = b ;
      callback.doClick() ;
   }

   // Register a callback action listener.  This is used for batch loads
   // to signal load completion.

   public static void setCallback(ActionListener al)
   {
      callback.addActionListener(al);
   }
   
   public static void removeCallback(ActionListener al)
   {
      callback.removeActionListener(al);
   }


   // A method to return the loaded archive file state information.
   // This interface is used by the WebSearch tool.
   
   public Object [] getState()
   {
      if (!loaded) return null ;
      Configuration config = mainframe.getConfig() ;
      if (config == null) return null ;
      ArchiveFile zip = config.getZipFile() ;
      if (zip == null) return null ;
      FileOpen fo = config.getFileOpen() ;
      int entrycount = (fo != null) ? fo.getEntryCount() : 0 ;
      Object [] packet = new Object[10] ;
      boolean opened = false ;

      // Identify the zip archive name.

      String name = zip.getOriginalName() ;
      if (name == null) name = zip.getName() ;
      if (name != null)
      {
         File f = new File(name) ;
         name = f.getName() ;
      }

      // Establish the CNF state.  Return this state if we loaded from
      // a directory file or there was only one configuration.

      packet[0] = new String(name) ;
      packet[1] = new Long(zip.getCompressedSize()) ;
      packet[2] = new Integer(entrycount) ;
      packet[3] = new Integer(config.getPaletteCount()) ;
      packet[4] = new Integer(config.getCelCount()) ;
      packet[5] = new Integer(config.getSoundCount()) ;
      packet[6] = new Integer(config.getMovieCount()) ;
      packet[7] = new Integer(config.getFKissLevel()) ;
      packet[8] = new Boolean(config.isCherryKiss()) ;
      packet[9] = new Boolean(config.isEnhancedPalette()) ;
      if (zip instanceof DirFile) return packet ;
      if (entrycount == 1) return packet ;

      // If we have multiple configurations examine the archive state.
      // Open the archive and scan the contents to determine the maximum
      // state values.

      try
      {
         int sounds = 0 ;
         int movies = 0 ;
         int images = 0 ;
         int palettes = 0 ;
         int configurations = 0 ;
         int fkisslevel = config.getFKissLevel() ;
         if (!zip.isOpen()) { zip.open() ; opened = true ; }
         Vector contents = zip.getContents() ;
         if (contents == null) return packet ;
         for (int i = 0 ; i < contents.size() ; i++)
         {
            ArchiveEntry ae = (ArchiveEntry) contents.elementAt(i) ;
            if (ae.isAudio()) sounds++ ;
            else if (ae.isImage()) images++ ;
            else if (ae.isPalette()) palettes++ ;
            else if (ae.isVideo()) movies++ ;
            else if (ae.isConfiguration()) configurations++ ;

            // For configurations, we read and parse the file to determine
            // the maximum FKiSS level.

            if (ae.isConfiguration())
            {
               InputStream is = ae.getInputStream() ;
               if (is == null) throw new Exception("null input stream") ;
               ByteArrayOutputStream os = new ByteArrayOutputStream() ;

               // Read and parse the data.

               int b = 0 ;
               while ((b = is.read()) >= 0) { os.write(b) ; }
               is.close() ;  os.close() ;
               config = new Configuration() ;
               config.parseFKiss(mainframe,os.toByteArray()) ;

               // Retain the maximum FKiSS level.

               int n = (config != null) ? config.getFKissLevel() : 0 ;
               if (n > fkisslevel) fkisslevel = n ;
            }
         }

         // Establish the new packet contents.

         packet[2] = new Integer(configurations) ;
         packet[3] = new Integer(palettes) ;
         packet[4] = new Integer(images) ;
         packet[5] = new Integer(sounds) ;
         packet[6] = new Integer(movies) ;
         packet[7] = new Integer(fkisslevel) ;
      }
      catch (Exception e)
      {
         System.out.println("Kisekae: getState " + e.toString()) ;
         e.printStackTrace() ;
      }
      finally
      {
         if (opened)
         {
            try { zip.close() ; }
            catch (IOException e)
            {
               System.out.println("Kisekae: getState " + e.toString()) ;
               e.printStackTrace() ;
            }
         }
      }
      return packet ;
   }


   // A method to write the loaded set to a file.  This interface is used by
   // the WebSearch tool.

   public void saveSet(ActionListener listener, String directory, String name)
   {
      try
      {
         if (!loaded) return ;
         Configuration config = mainframe.getConfig() ;
         if (config == null) return ;
         FileSave fs = new FileSave(mainframe,config) ;
         fs.setDestination(directory,name) ;
         fs.addWriteListener(listener) ;
         fs.save() ;
      }

      // Watch for exceptions.  If an exception occurs simulate a FileWriter
      // callback signal.

      catch (Exception e)
      {
         System.out.println("Kisekae: saveSet " + e.toString()) ;
         e.printStackTrace() ;
         ActionEvent ae = new ActionEvent(this,0,"FileWriter Callback") ;
         listener.actionPerformed(ae) ;
      }
   }


   // A method to generate a thumbnail image of the currently loaded set.
   // Images are saved in JPG format (by default) at the specified size.
   // This interface is used by the WebSearch tool.
   
   public void saveThumbnail(ActionListener listener, String directory, String name, int w, int h)
   {
      if (!loaded) return ;
      Configuration config = mainframe.getConfig() ;
      if (config == null) return ;
      PanelFrame panel = mainframe.getPanel() ;
      if (panel == null) return ;
      Image image = panel.getImage() ;
      if (image == null) return ;

      // Create a scaled image at the current aspect ratio.

      try
      {
         int iw = image.getWidth(null) ;
         int ih = image.getHeight(null) ;
         float sfw = ((float) w) / ((float) iw) ;
         float sfh = ((float) h) / ((float) ih) ;
         float sf = Math.min(sfw,sfh) ;
         int sw = (int) (sf * iw) ;
         int sh = (int) (sf * ih) ;
         Image scaledimage = image.getScaledInstance(sw,sh,Image.SCALE_AREA_AVERAGING) ;

         // Create a thumbnail image sized as required.

         BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
         Graphics2D g2 = bi.createGraphics() ;
         Color background = panel.getBackground() ;
         g2.setColor(background) ;
         g2.fillRect(0,0,w,h) ;
         int x = (w - sw) / 2 ;
         int y = (h - sh) / 2 ;
         g2.drawImage(scaledimage,x,y,sw,sh,null) ;
         g2.dispose() ;

         // Create a truetype cel so the image can be saved.  

         DirFile zip = new DirFile(null,directory) ;
         DirEntry ze = new DirEntry(directory,name,zip) ;
         int i = name.lastIndexOf('.') ;
         String ext = (i < 0) ? ".jpg" : name.substring(i).toLowerCase() ;

         Cel cel = new JpgCel(zip,name,null) ;
         cel.setZipEntry(ze) ;
         cel.setImage(bi) ;
         
         // Note that we need to dither to 256 colors for GIF.
         
         if (".gif".equals(ext))
         {
            Object [] o = cel.dither(255) ;
            cel.setImage((Image) o[0]) ;
            cel.setPalette((Palette) o[1]) ;
         }

         // Save our image.

         cel.setUpdated(true) ;
         cel.setLoaded(true) ;
         FileSave fs = new FileSave(mainframe,cel) ;
         fs.setDestination(directory,name) ;
         fs.addWriteListener(listener) ;
         fs.save() ;
      }

      // Watch for exceptions.  If an exception occurs simulate a FileWriter
      // callback signal.

      catch (Exception e)
      {
         System.out.println("Kisekae: saveThumbnail " + e.toString()) ;
         e.printStackTrace() ;
         ActionEvent ae = new ActionEvent(this,0,"FileWriter Callback") ;
         listener.actionPerformed(ae) ;
      }
   }


   // A method to save a generic text file.
   // This interface is used by the WebSearch tool.

   public void saveText(ActionListener listener, String directory, String name, Object o)
   {
      try
      {
         if (!(o instanceof JTextComponent))
            throw new KissException("invalid text component") ;
         DirFile zip = new DirFile(null,directory) ;
         DirEntry ze = new DirEntry(directory,name,zip) ;
         TextObject text = new TextObject(ze,(JTextComponent) o, null) ;

         // Save our text file.

         text.setUpdated(true) ;
         FileSave fs = new FileSave(mainframe,text) ;
         fs.setDestination(directory,name) ;
         fs.addWriteListener(listener) ;
         fs.save() ;
      }

      // Watch for exceptions.  If an exception occurs simulate a FileWriter
      // callback signal.

      catch (Exception e)
      {
         System.out.println("Kisekae: saveTextFile " + e.toString()) ;
         e.printStackTrace() ;
         ActionEvent ae = new ActionEvent(this,0,"FileWriter Callback") ;
         listener.actionPerformed(ae) ;
      }
   }

   // Retrieve KiSS set documentation files.  We return the first 100
   // alphanumeric bytes.  This interface is used by the WebSearch tool.

   public String getDescription()
   {
      if (!loaded) return null ;
      Configuration config = mainframe.getConfig() ;
      if (config == null) return null ;
      FileOpen fileopen = config.getFileOpen() ;
      if (fileopen == null) return null ;
      fileopen.open() ;

      // Search for the appropriate entries in our archive.

      String [] ext = new String[2] ;
      ext[0] = ".TXT" ;
      ext[1] = ".DOC" ;
      ArchiveEntry ze = fileopen.showConfig(mainframe,"",ext) ;
      if (ze == null) { fileopen.close() ; return null ; }

      // Scan input characters.  All sequences of whitespace are compressed
      // to only one space character.

      try
      {
         int b ;
         boolean ws = false ;
         StringBuffer sb = new StringBuffer() ;
         InputStream is = ze.getInputStream() ;
         if (is == null) { fileopen.close() ; return null ; }
         InputStreamReader isr = new InputStreamReader(is,encoding) ;
         while ((b = isr.read()) >= 0)
         {
            char c = (char) b ;
            if (Character.isWhitespace(c)) { ws = true ; continue ; }
            if (!Character.isLetterOrDigit(c)) continue ;
            if (ws) sb.append(' ') ;
            sb.append(c) ;
            if (sb.length() >= 100) break ;
            ws = false ;
         }

         // Return the result.

         fileopen.close();
         return sb.toString().trim() ;
      }

      // Watch for IO errors.

      catch (IOException e)
      {
         System.out.println("Kisekae: getDescription " + e.toString()) ;
         fileopen.close() ;
      }
      return null ;
   }


   // The following functions are necessary for Applet control.
   // ---------------------------------------------------------

   // These include the update and paint routines, applet information
   // and status message display.

   void trace(String s)
   {
      showStatus(s) ;
      try { Thread.sleep(1000) ;	}
      catch (Exception e) { }
   }


   // String Error display on status line.

   void showError(String s)
   {
      statusMsg = "Error: " + s ;
      trace(statusMsg) ;
      error = true ;
      throw new Error(statusMsg) ;
   }


   // Exception Error display on status line.

   void showError( Exception e )
   { if (!error)	showError("Exception: " + e) ; }


   // Applet Information.

   public String getAppletInfo() { return copyright ; }


   // Browser display of our home page.

   public static void showHomePage() 
   { 
      if (inApplet()) 
      {
         try 
         {
            URL homepage = new URL(website) ;
            AppletContext context = kisekae.getAppletContext() ;
            context.showDocument(homepage) ;
         }
         catch (Exception e) { } 
      }
   }
     

   // The Parameter access function is used to read Applet parameters.
   // If a parameter file has been specified, then it is accessed
   // first to determine the parameter value, otherwise we get
   // the parameters from the HTML page.

   public String getParameter(String s)
   {
      if (inapplet)
         return (super.getParameter(s)) ;
      else
         return null ;
   }


   // Override the showStatus method for application execution.

   public void showStatus(String s)
   {
      if (inapplet)
         super.showStatus(s) ;
      else
         System.out.println(s) ;
   }


   // Graphics Methods to update the Applet panel area.

   public void update (Graphics g) { paint(g) ; }


   // When we paint the applet panel, all we do is set the background.

   public void paint (Graphics g)
   {
      g.setColor(Color.white) ;
      g.fillRect(0,0,getSize().width,getSize().height) ;
      return ;
   }


   // Start the applet.  This method is called by the browser when
   // the window is entered.  We need to create a new thread and
   // execute the applet.  The start method of the thread calls the
   // run method for the object (us).

   public void start()
   {
      if (engine == null)
      {
         engine = new Thread(this) ;
         engine.start() ;
      }
      if (mainframe != null && suspended)
      {
         System.out.println("Kisekae: resumed.") ;
         mainframe.resume() ;
         suspended = false ;
      }
      showStatus(statusMsg) ;
   }


   // Destroy the applet.  This method is called by the browser or applet
   // viewer to inform this applet that it is being reclaimed and that it
   // should destroy any resources that it has allocated. The stop method
   // will always be called before destroy.

   public void destroy()
   {
      try
      {
         if (engine != null && engine.isAlive()) engine.interrupt() ;
         if (mainframe != null)
         {
            EventHandler.stopEventHandler() ;
            System.out.println("Kisekae: destroyed.") ;
            try { Thread.currentThread().sleep(2000) ; }
            catch (InterruptedException e) { }
            System.out.println("Kisekae: disposed.") ;
            mainframe.dispose() ;
         }
      }
      catch (Throwable e)
      {
         System.out.println("Kisekae: destroy applet exception.") ;
         System.out.println(e.toString()) ;
      }

      // Free storage allocations.

      suspended = false ;
      mainframe = null ;
      engine = null ;
   }


   // Stop the applet.  This method is called by the browser when we
   // leave the window.  We want to stop executing when we are not the
   // current window.  We suspend any main frame execution threads.

   public void stop()
   {
      if (mainframe != null)
      {
         System.out.println("Kisekae: suspended.") ;
         mainframe.suspend() ;
         Audio.stop() ;
         if (mediainstalled) Video.stop() ;
         suspended = true ;
      }
   }


   // The run method is called when our animation thread is started.
   // This happens when the applet page is displayed or is active.
   // We know that the applet parameters have been read and decoded.

   public void run()
   {
      Thread me = Thread.currentThread();
      me.setPriority(Thread.MIN_PRIORITY);

      if (error)
      {
         showStatus(statusMsg);
         return;
      }

      repaint();
   }


   // Event listeners for mouse events

   public class MouseEventHandler extends MouseAdapter
   {
      // When the mouse enters the Applet, show that it is active.

      public void mouseEntered(MouseEvent evt)
      { showStatus(statusMsg); repaint() ; }

      // When the mouse leaves the Applet, clear the status line.

      public void mouseExited(MouseEvent evt)
      { showStatus("");	repaint() ; }
   }


   // The action method is used to process applet events.

   public void actionPerformed(ActionEvent evt)
   {
      // The Run button opens the panel frame.

      if ("Run".equals(evt.getActionCommand()))
      { mainframe = new MainFrame(this) ; }
   }


   // A method to restart our program from scratch.

   public static void restart()
   {
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { restart() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      
      String file = null ;
      if (mainframe != null)
      {
         Configuration config = mainframe.getConfig() ;
         if (config != null)
         {
            ArchiveFile zip = config.getZipFile() ;
            if (zip != null) file = zip.getName() ;
            config.close() ;
         }
         mainframe.closeframe() ;
      }
      
      MainFrame mainframe1 = mainframe ;
      System.out.println("Application restarted.") ;
      mainframe = new MainFrame(kisekae,file,true) ;
      if (mainframe1 != null) mainframe1.dispose() ;
   }
  
   
   // Set global uncaught exception handler for Event Dispatch Thread.
   // We can occasionally see a NullPointerException in Swing code.
   // The exception writes a full stack trace which shows in the log file.
   // The EDT is automatically restarted in this case so no visible problem.
   
   public static void setupGlobalExceptionHandling() 
   {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() 
      {
        @Override
        public void uncaughtException(Thread t, Throwable e) 
        {
            String message = e.getMessage();
            if (message == null || message.length() == 0) 
               message = "Java Runtime Environment: " + e.getClass();
            if (t != null) message += " on thread " + t.getName() ;
            System.out.println("Kisekae: Uncaught Exception " + message);
            e.printStackTrace();
        }
      });
   }

   
   // Application Main program.
   // -------------------------

   // Main program to run this applet as an application.
   // java -jar UltraKiss.jar [file] [basedir] [kissweb] [language] 
   //    [splashdir] [website]

   public static void main(String[] args)
   {
      inapplet = false ;
      kisekae = new Kisekae() ;
      if (args.length > 0) file = Configuration.trim(args[0]) ;
      if (args.length > 1) directory = Configuration.trim(args[1]) ;
      if (args.length > 3) language = Configuration.trim(args[3]) ;

      if (directory != null) kisekae.initCodeBase() ;
      if (secure)
         System.out.println("This application is running in a secure environment.") ;
      if (file != null && file.length() > 0)
         System.out.println("Application start file is " + file) ;
      if (language != null && language.length() > 0)
         setLanguage(language) ;
      
      setupGlobalExceptionHandling() ;
      
      // Initialize the application frame.

      try
      {
         kisekae.init() ;
         if (args.length > 2)
         {
            String s = Configuration.trim(args[2]) ;
            if (s.length() > 0)
            {
               try
               {
                  URL testURL = new URL(s) ;
                  URL loadURL = getLoadBase() ;
                  String testpath = testURL.getPath() ;
                  String loadpath = loadURL.getPath() ;
                  String protocol = testURL.getProtocol() ;
                  int i = testpath.indexOf(':') ;
                  int j = loadpath.indexOf(':') ;
                  
                  // Apply Windows drive specifier if missing
                  
                  if (j > 0 && i < 0 && "file".equals(protocol.toLowerCase())) 
                  {
                     testpath = loadpath.substring(0,j+1) + testpath ;
                     s = protocol + ":" + testpath ;
                  }
                  
                  kissweb = s ;
                  OptionsDialog.setKissWeb(kissweb) ;
                  System.out.println("Application website is " + kissweb) ;
               }
               catch (MalformedURLException e) 
               { 
                  System.out.println("Invalid URL for application website: " + s) ;
               }
            }
         }
         if (args.length > 4)
         {
            String s = Configuration.trim(args[4]) ;
            if (s.length() > 0)
            {
               splashdir = s ;
               OptionsDialog.setSplashDir(s) ;
               System.out.println("Application splash image is " + s) ;
            }
         }
         if (args.length > 5)
         {
            String s = Configuration.trim(args[4]) ;
            if (s.length() > 0)
            {
               try
               {
                  website = s ;
                  URL testURL = new URL(s) ;
                  System.out.println("System website is " + s) ;
               }
               catch (MalformedURLException e) 
               { 
                  if (s.trim().length() > 0)
                     System.out.println("Invalid URL for system website: " + s) ;
               }
            }
         }

         // Fire up the program.

         mainframe = new MainFrame(kisekae,file) ;
         if (splashwindow != null)
         {
            splashwindow.setVisible(false) ;
            splashwindow.dispose() ;
            splashwindow = null ;
            splashimage = null ;
            splash = null ;
         }
         mainframe.toFront() ;
         
         // Show tips if first time.
         
         if (OptionsDialog.getShowTips() && file == null && tipsinstalled)
         {
            tips = new TipsBox(mainframe,Kisekae.getCaptions().getString("TipsBoxTitle"),true) ;
            tips.show() ;
         }
      }

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.

      catch (OutOfMemoryError e)
      {
         EventHandler.stopEventHandler() ;
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         System.out.println("Kisekae: Out of memory.") ;
         e.printStackTrace() ;
         if (captions != null)
         {
            JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
                captions.getString("LowMemoryFault") + " - " +
                captions.getString("ActionNotCompleted"),
                captions.getString("LowMemoryFault"),
                JOptionPane.ERROR_MESSAGE) ;
         }
         else
         {
            JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
                "LowMemoryFault" + " - " +
                "ActionNotCompleted",
                "LowMemoryFault",
                JOptionPane.ERROR_MESSAGE) ;
         }
      }

      // Watch for internal faults.

      catch (Throwable e)
      {
         EventHandler.stopEventHandler() ;
         System.out.println("Kisekae: Internal fault.") ;
         e.printStackTrace() ;
         String s = "InternalError" + " - " ;
         s += "ExecutionTerminates" ;
         if (captions != null)
         {
            s = captions.getString("InternalError") + " - " ;
            s += captions.getString("ExecutionTerminates") ;
         }
         s += "\n" + e.toString() ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            e.printStackTrace(ps) ;
            os.close() ;
   			s += "\n" + "Host Operating System " + System.getProperty("os.name") ;
            s += "\n" + "Java Virtual Machine " + System.getProperty("java.version") ;
            s += "\n" + "UltraKiss build date: " + Kisekae.getBuildDate() ;
            FileReader is = new FileReader(f) ;
            LineNumberReader lr = new LineNumberReader(is) ;
            String s1 = lr.readLine() ;
            s1 = lr.readLine() ;
            int traceline = 0 ;
            while (s1 != null)
            {
               s += "\n" + s1.trim() ;
               s1 = lr.readLine() ;
               if (traceline++ > 10) break ;
            }
         }
         catch (EOFException eof) { }
         catch (Exception ex) { s += "\n" + "Stack trace unavailable." ; }

         if (captions != null)
         {
            JOptionPane.showMessageDialog(Kisekae.getMainFrame(), s,
                captions.getString("InternalError"),
                JOptionPane.ERROR_MESSAGE) ;
         }
         else
         {
            JOptionPane.showMessageDialog(Kisekae.getMainFrame(), s,
                "InternalError",
                JOptionPane.ERROR_MESSAGE) ;
         }
         System.exit(-1) ;
      }
   }
}

