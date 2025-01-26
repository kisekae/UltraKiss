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
* MainFrame Class
*
* Purpose:
*
* This object encapsulates the main program window frame.  It manages
* the main menu events and the panel windows for the application.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.awt.print.* ;
import java.awt.datatransfer.Clipboard ;
import java.util.Vector ;
import java.util.ResourceBundle ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Scanner ;
import java.util.prefs.* ;
import java.net.URL ;
import java.io.* ;
import java.nio.file.Paths;
import javax.swing.* ;
import javax.swing.undo.* ;


final public class MainFrame extends KissFrame
	implements ActionListener, WindowListener, KeyListener, ComponentListener
{
	private static final int NEWFILE = 0 ;
	private static final int NEWPAGE = 1 ;
	private static final int NEWCONFIG = 2 ;
	private static final int INSERTPAGE = 3 ;
	private static final int DELETEPAGE = 4 ;
	private static final int WRITEPAGE = 5 ;
	private static String splashImage = "Images/KisekaeSplash.png" ;
	private static String ultraImage = "Images/KisekaeUltra.png" ;
   private static int imagenum = -2 ;     // The current splash image
   private static int images = -1 ;       // The number of splash mages

	private MainFrame me = null ;			   // Reference to ourselves
	private Kisekae kisekae = null ;			// Reference to our main class
	private Dimension screenArea = null ;	// The dimensions of our screen
	private Dimension panelArea = null ;	// The panel frame dimensions
	private Dimension windowArea = null ;	// The main window dimensions
	private PageFormat pageformat = null ; // Our default page format
	private KissMenu menu = null ;			// Reference to menu controls
	private MainMenu mainmenu = null ;		// Reference to main menu controls
	private PanelMenu panelmenu = null ;	// Reference to panel menu controls
	private SplashPane splash = null ;		// Reference to our splash panel
	private PanelFrame panel = null ;		// Reference to our panel frame
	private AboutBox about = null ;        // Reference to our about dialog
	private OptionsDialog options = null ; // Reference to our program options
	private FKissTrace trace = null ;      // Reference to our trace panel
	private JScrollPane scrollpane = null ; // Reference to the scroll pane
	private StatusBar statusBar = null ;	// Reference to the status bar
	private ToolBar toolBar = null ;			// Reference to our tool bar
	private JPanel northpanel = null ;		// Reference to our toolbar panel
	private JPanel southpanel = null ;		// Reference to our statusbar panel
	private JTextPane loadtext = null ;		// Reference to our loader text
   private String title = null ;          // User frame title

	private Configuration config = null ;	// The configuration object
   private Object newconfigid = null ;    // The new configuration ID
   private FileLoader loader = null ;		// The file loader object
	private FileOpen fileopen = null ;		// The current file open object
   private Vector expandfiles = null ;    // Configuration expansion files
   private String preappendlru = null ;   // Reference for LRU list
   private String preappendpath = null ;  // Reference for LRU list
	private Color background = null ;		// The panel background color
	private int x, y ;							// Panel centering coordinates
   
   private Timer closetimer = null ;      // Timer for exit
   private Timer endtimer = null ;        // Timer for end event

	private String savecallback = null ;	// Method to call on file save
	private boolean callreturn = false ; 	// True if callback processed
	private boolean terminate = false ;		// True if frame is closing
	private boolean restart = false ;		// True if configuration is reloaded
	private boolean append = false ;       // True if configuration being appended
	private boolean firstback = true ;		// True if window not deactivated  
	private boolean expansion = false ;		// True if loading expansion set  
   private boolean silentclose = false ;  // True if viewer(open) doing close
   

	// Our file load callback button that other components can attach
	// listeners to for notification of configuration file updates.

	protected CallbackButton callback = new CallbackButton(this,"MainFrame Callback") ;

   
   // Constructor

	public MainFrame (Kisekae kiss) 
   { this(kiss,null,false) ; }
   
	public MainFrame (Kisekae kiss, final String file) 
   { this(kiss,file,false) ; }

	public MainFrame (Kisekae kiss, final String file, boolean restart)
	{
		super("UltraKiss") ;
		kisekae = kiss ;
      me = this ;
      
      // On a restart, retain the same background image.
      
      if (restart && imagenum > 0) imagenum-- ;

      // Set the frame characteristics.

      super.open() ;
      northpanel = new JPanel() ;
      southpanel = new JPanel() ;
      northpanel.setLayout(new BorderLayout()) ;
      southpanel.setLayout(new BorderLayout()) ;
		getContentPane().add(northpanel,BorderLayout.NORTH) ;
		getContentPane().add(southpanel,BorderLayout.SOUTH) ;
		setFont(new Font("Dialog",Font.PLAIN,11)) ;

		// Create our options dialog and about dialog.  This creates
      // the menu, toolbar, and status bar.

      updateUI() ;
      setNewSplashPane(true) ;
      try { if (OptionsDialog.getAppleMac()) new AboutHandler() ; }
      catch (Throwable e) { } 

		// Set up to catch window events in this frame.

      addKeyListener(this) ;
		addWindowListener(this) ;
		addComponentListener(this) ;
      if (Kisekae.isBatch()) setState(Frame.ICONIFIED) ;
 		setIconImage(Kisekae.getIconImage()) ;
		setVisible(true) ;
      setFocus() ;

      // Initiate a file load on a separate thread.  We have seen
      // intermittent execeptions with the fileopen reference being
      // destroyed during init() if this is run directly.

      Runnable runner = new Runnable()
      { public void run() 
        {
           try { Thread.currentThread().sleep(1000) ; }
           catch (Exception e) { }
           loadfile(file) ; 
        } 
      } ;
		SwingUtilities.invokeLater(runner) ;
   }


   // A function to initiate a load of a specific file or URL.

   void loadfile(String filename) { loadfile(filename,null) ; }
   void loadfile(String filename, String cnf)
   {
      // If our named file parameter is a URL, load from the web.  If the
      // load is successful a callback is fired to the MainMenu and this
      // opens the correct file context.  Configuration elements call our
      // init() routine.
       
      String file = filename ;
      if (file != null && cnf == null)
      {
         int n = file.indexOf(',') ;
         if (n > 0) 
         {
            cnf = file.substring(n+1).trim() ;
            cnf = Variable.getStringLiteralValue(cnf) ;
            file = file.substring(0,n) ;
         }
      }

      if (file != null) 
         file = Variable.getStringLiteralValue(file) ;
      if (file != null)
      {
         if (file.startsWith("http:") || file.startsWith("https:") || file.startsWith("file:") || file.startsWith("jar:"))
         {
            UrlLoader urlloader = new UrlLoader(this,filename) ;
            mainmenu.setUrlLoader(urlloader) ;
            urlloader.callback.addActionListener(mainmenu) ;
            Thread loadthread = new Thread(urlloader) ;
            loadthread.start() ;
            return ;
         }
      }

		// If our named file parameter is an archive file and we are secure,
		// load with a class loader.

		if (ArchiveFile.isArchive(file) && Kisekae.isSecure())
		{
			JarLoader urlloader = new JarLoader(this,file) ;
			mainmenu.setUrlLoader(urlloader) ;
			urlloader.callback.addActionListener(mainmenu) ;
			Thread loadthread = new Thread(urlloader) ;
			loadthread.start() ;
			return ;
		}

      // If our named file is the special keyword for the UltraKiss Browser,
      // start the browser.

      if ("browser".equalsIgnoreCase(file))
      {
         final WebFrame wf = new WebFrame(this) ;
         wf.setVisible(true) ;
			Runnable runner = new Runnable()
			{ public void run() { try { Thread.currentThread().sleep(500) ; } catch (Exception e) { } wf.toFront() ; } } ;
         Thread thread = new Thread(runner) ;
         thread.start() ;
         return ;
      }

      // If our named file parameter is a file, load from disk.

      URL codebase = Kisekae.getBase() ;
      if (codebase == null) return ;
      if (file == null || file.length() == 0) return ;
      File f = new File(file) ;
      if (!f.isAbsolute()) f = new File(codebase.getFile(),file) ;
		FileOpen fd = new FileOpen(this,f.getPath(),"r") ;

      // Archive files may show a configuration selection dialog.
      // Directory files are uniquely identified.

      ArchiveEntry ze = null ;
      if (ArchiveFile.isArchive(file))
      {
         fd.open() ;
   		ze = fd.showConfig(this,cnf) ;
      }
      else
      {
         fd.open(f.getPath(),cnf) ;
   		ze = fd.getZipEntry() ;
      }

      // If we have a selection, load it.

      if (ze == null) { fd.close() ; return ; }
      mainmenu.setFileOpen(fd) ;
      mainmenu.openContext(fd,ze) ;
	}



	// Initialization.  We reference the current fileopen object to identify
	// the selected data set and then construct the window panel environment
	// to properly display this data set.  This method is called when we need
	// to initialize for a new data set.  The file loader is run as a
	// separate thread to track the load progress.

	void init()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { init() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}

		if (!callreturn)
		{
			savecallback = "init" ;
			if (checksave()) return ;
		}

		// Close the configuration if loading a new config.

      KissMenu m = (menu instanceof UserMenu) ? panelmenu : menu ;
		fileopen = (m != null) ? m.getFileOpen() : null ;
		if (fileopen == null) return ;
		FileOpen temp = (FileOpen) fileopen.clone() ;
		if (OptionsDialog.getLoadCloseOn())
		{
			closeconfig() ;
			fileopen = temp ;
		}
      else
      {
         if (options != null) options.resetOptions() ;
         updateMenuOptions() ;
      }

		// Run the file load as a separate thread.

		fileopen.open() ;
		if (fileopen == null) fileopen = temp ;
      mainmenu.setFileOpen(fileopen) ;
		ArchiveFile zip = fileopen.getZipFile() ;
		ArchiveEntry ze = fileopen.getZipEntry() ;
		loader = new FileLoader(this,config,zip,ze) ;
		Thread loadthread = new Thread(loader) ;
		loadthread.start() ;
      config = null ;
	}


	// Initialization.  We are going to reload our current configuration.
	// This reloads the memory copy of the configuration file.  This copy
	// may have been edited.  The archive file is reopened at the time of
	// the configuration load so that new elements may be accessed.

	void init(Configuration config)
	{
      closeframe() ;
      silentclose = false ;
		fileopen = config.getFileOpen() ;
		if (fileopen == null) return ;
		loader = new FileLoader(this,config) ;
		Thread loadthread = new Thread(loader) ;
		loadthread.start() ;
	}


	// Expansion.  This method is called when we expand the current data set.
   // An expansion retains existing object references and event definitions
   // unless they are replaced within the expansion set.  The file loader is
   // run as a separate thread to track the load progress.  The expand switch
   // is true if this is an expansion from a user action rather than FKiSS.

	void expand()
   { expand(false) ; }
   
	void expand(boolean expand)
	{
		if (!callreturn)
		{
			savecallback = "expand" ;
			if (checksave()) return ;
		}

		// Run the file load as a separate thread.

      expansion = expand ;
      KissMenu m = (menu instanceof UserMenu) ? panelmenu : menu ;
		fileopen = (m != null) ? m.getFileOpen() : null ;
		if (fileopen == null) return ;
		fileopen.open(fileopen.getZipFile(),fileopen.getZipEntry()) ;
		ArchiveFile zip = fileopen.getZipFile() ;
		ArchiveEntry ze = fileopen.getZipEntry() ;
      if (zip != null && zip.getFileOpen() == null) 
         zip.setFileOpen(fileopen) ;
      
      if (expansion)
      {
         Vector v = config.getExpandFiles() ;
         if (v != null)
            expandfiles = v ;
      }
      
      // Appended configurations reload the current configuration.  Expanded
      // configurations load a new CNF.
      
      if (config.isAppended())
      {
         if (zip != null) config.setZipFile(zip) ;
         if (ze != null) config.setZipEntry(ze) ;
   		PrintLn.println("Expansion reloading appended configuration \"" + config.getName() + "\" (" + config.getID() + ")") ;
         loader = new FileLoader(this,config) ;
      }
      else  
      {
   		PrintLn.println("Expansion reloading new configuration \"" + config.getName() + "\" (" + config.getID() + ")") ;
         loader = new FileLoader(this,config,zip,ze) ;
      }
		Thread loadthread = new Thread(loader) ;
		loadthread.start() ;
	}


	// This is the callback method that the loader thread invokes
	// when it is finished loading files.  The file loader should
	// have started this callback under the AWT event thread as we
	// can potentially modify the user interface objects.

   void initframe()
   {
      silentclose = false ;
   	if (loader == null) return ;
      Configuration c = loader.getNewConfiguration() ;
      newconfigid = (c != null) ? c.getID() : null ;
      if (expansion) c.setExpandFiles(expandfiles) ;
   	initframe(c) ;
   }

	void initframe(Configuration c)
	{
		try
		{
         if (Kisekae.isBatch() && c != null && c.hasViewerAppend())
            c = null ;
         
			// If the load was cancelled restore our old fileopen object.
         // Search loads are cancelled if the cnf was to be appended.

			if (c == null)
			{
				showStatus("Load cancelled ...") ;
				fileopen = (config == null) ? null : config.getFileOpen() ;
            KissMenu m = (menu instanceof UserMenu) ? panelmenu : menu ;
				if (m != null) m.setFileOpen(fileopen) ;
		      KissObject.setLoader(null) ;
            Kisekae.setLoaded(false) ;
            closeframe(); // cancel on syntax error CNF reload
      		if (fileopen != null) fileopen.close() ;
            if (loader != null) 
            {
               loader.setNewConfiguration(null) ;
               loader.close() ;
            }
            config = null ;
            loader = null ;			
            return ;
			}
      
         // If APPEND files exist and we are expanding this configuration
         // we need to revert to the preappend configuration and then add
         // the expansion configuration.  

         Vector includefiles = c.getIncludeFiles() ;
         if (c.isAppended() && expansion && includefiles != null)
         {
            String cnf = "" ;
            String archive = "" ;
            String s = preappendlru + preappendpath ;
            int i = s.lastIndexOf(File.pathSeparatorChar) ;
            if (i > 0) 
            {
               cnf = s.substring(i+1) ;
               archive = s.substring(0,i) ;
            }

      		// Close the configuration if loading a new config.

     			closeconfig() ;
               
            // Load the LRU file.  When loaded we return to the init()
            // routine and will restart here.  Our includefiles which
            // includes the expansion file will initiate the load of
            // the expansion configuration.
            
    			if (ArchiveFile.isArchive(archive))
     				PrintLn.println("Open LRU archive " + archive) ;
            final String archive1 = archive ;
            final String cnf1 = cnf ;
   			Runnable awt = new Runnable()
   			{ public void run() { loadfile(archive1,cnf1) ; } } ;
   			SwingUtilities.invokeLater(awt) ;
            if (loader != null) loader.close() ;
            return ;
         }

         // If we are expanding this configuration apply the expansion CNF.
         
         if (expansion && expandfiles != null)
         {
            boolean b = false ;
            for (int i = expandfiles.size()-1 ; i >= 0 ; i--)
            {
               Vector entries = c.searchInclude(expandfiles.elementAt(i)) ;
               if (entries == null || entries.size() == 0) continue ;
               Object o = entries.firstElement() ;
               if (!(o instanceof ArchiveEntry)) continue ;
               ArchiveEntry ze = (ArchiveEntry) o ;
               PrintLn.println("Configuration element found: " + ze.getName());
               b = c.appendInclude(ze) ;
               break ;
            }
   
            // If we found an expansion file then process the updated configuration.
            // If no file was found continue with normal initialization.
            
            if (loader != null) loader.close() ;       
            if (b) 
            {
               expansion = false ;
               expandfiles = null ;
               c.setRestartable(true) ;
               init(c) ;
               return ; 
            }           
         }
                  
         // If APPEND files exist we must open the INCLUDE files and append   
         // any CNF elements found in these files to this configuration.   
         // The new cel declarations and event code become an addition to  
         // this configuration.  The first CNF element in the APPEND archive  
         // file is used.  Retain the original source references for the 
         // LRU list.
   
         if (c.hasAppendFiles() && !c.isAppended() && includefiles != null)
         {
            boolean b = false ;
            ArchiveFile zip = c.getZipFile() ;
            String zipname = (zip != null) ? zip.getName() : null ;
            if (zipname != null && !mainmenu.getNoCopy())
            {
               String lruname = zipname + File.pathSeparator ;
               if (preappendlru == null) preappendlru = lruname ;
               String s = c.getName() ;
               if (preappendpath == null) preappendpath = s ;
            }
   
            // Search each APPEND file for a CNF element.  If we find one
            // then append the text at the location of the APPEND statement.
            
            for (int i = includefiles.size()-1 ; i >= 0 ; i--)
            {
               Vector entries = c.searchInclude(includefiles.elementAt(i)) ;
               if (entries == null || entries.size() == 0) continue ;
               Object o = entries.firstElement() ;
               if (!(o instanceof ArchiveEntry)) continue ;
               ArchiveEntry ze = (ArchiveEntry) o ;
               PrintLn.println("Configuration element found: " + ze.getName());
               b = c.appendInclude(ze) ;
               if (!b) break ;
            }
   
            // If we found an APPEND file then process the updated configuration.
            // If no file was found continue with normal initialization.
            
            if (loader != null) loader.close() ;       
            if (b) 
            {
               c.setRestartable(true) ;
               init(c) ;
               return ; 
            }
         }

         // If this initialization is due to a restart request where we
         // actually loaded a new configuration, rather than restarting
         // from our existing CNF, then we can lose audio and video objects
         // that were not referenced in the CNF.  We need to retain these
         // objects across the configuration and restore them to the
         // new configuration.
 
         Vector sounds = null ;
         Vector movies = null ;
         if (restart && config != null)
         {
            sounds = config.getSounds() ;
            movies = config.getMovies() ;
            if (sounds != null) sounds = (Vector) sounds.clone() ;
            if (movies != null) movies = (Vector) movies.clone() ;
         }

			// Close the current configuration and activate the new one.
         // If we are reactivating the current configuration our new
         // configuration object will equal the old configuration object.
			// Inform any objects waiting on a callback of a successful load.

			closeconfig((c == config || restart),false) ;
			config = c ;
			showStatus("Initializing \"" + config.getName() + "\" (" + config.getID() + ")" + " ...") ;
   		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         if (loader != null) loadtext = loader.getLoadText() ;
         
         // Restore any retained audio and video files from the previous
         // configuration if we restarted our set.  
         
         if (restart && config != null)
         {
            config.addSounds(sounds) ; 
            config.addMovies(movies) ; 
         }

			// Create a scroll pane to manage the situation when the
			// panel frame size exceeds the window area.  Our panel
			// must be initialized before its size can be established.

         Insets insets = getInsets() ;
			windowArea = getSize() ;
			windowArea.width -= (insets.left + insets.right) ;
			windowArea.height -= (insets.top + insets.bottom) ;
			panel = new PanelFrame(this) ;
			panelmenu = new PanelMenu(this,mainmenu) ;
         menu = panelmenu ;
			panel.setSize(windowArea.width,windowArea.height) ;
         panel.init(config) ;
			scrollpane = new JScrollPane(panel) ;
			scrollpane.setBounds(insets.left,insets.top,windowArea.width,windowArea.height) ;
         int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
         if (OptionsDialog.getAppleMac()) vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
         int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
         if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
         scrollpane.setVerticalScrollBarPolicy(vspolicy) ;
         scrollpane.setHorizontalScrollBarPolicy(hspolicy) ;			
         if (splash != null) getContentPane().remove(splash) ;
			if (toolBar != null) toolBar.updateButtons(config) ;
			getContentPane().add(scrollpane,BorderLayout.CENTER) ;
			fitPanel(getFitPanel()) ;

			// Initialize the panel frame.

         Color bc = config.getBorderColor() ;
			getContentPane().setBackground(bc) ;
			panel.setBackground(bc) ;
         panel.setVisible(false) ;

         // Activate the configuration and notify any listeners
         // waiting on a successful load. Open an FKiSS trace
         // dialog if required.

         restart = false ;
			showStatus("Activating \"" + config.getName() + "\" (" + config.getID() + ")" + " ...") ;
			PrintLn.println("Activating \"" + config.getName() + "\" (" + config.getID() + ")") ;
         traceFKiss(mainmenu.tracefkiss.isSelected()) ;
			config.activate(panel) ;
			callback.doClick() ;
         
         // Add this configuration to the LRU list.  Downloaded files  
         // with a NoCopy reference are not added to the list.  We
         // retain original values if configurations are APPENDed 
         // as the LRU value needs to refer to the original load.

         ArchiveFile zip = config.getZipFile() ;
         String zipname = (zip != null) ? zip.getName() : null ;
         if (zipname != null && !mainmenu.getNoCopy() && !restart)
         {
            String lruname = zipname + File.pathSeparator ;
            if (preappendlru != null) lruname = preappendlru ;
            String s = config.getPath() ;
            if (preappendpath != null) s = preappendpath ;
            int i = s.lastIndexOf('.') ;
            String ext = (i < 0) ? "" : s.substring(i).toLowerCase() ;
            if (".cnf".equals(ext))       
               mainmenu.setLruFile(lruname + s) ;
         }
         else
            mainmenu.setLruFile(null) ;
         if (zip != null) zip.close() ;
               
			// Activate the animation threads.

         AlarmTimer timer = config.getTimer() ;
         timer.resumeTimer(true) ;
         GifTimer animator = config.getAnimator() ;
         animator.resumeTimer(true) ;
			EventHandler handler = config.getEventHandler() ;
         handler.resumeEventHandler(OptionsDialog.getFKissOn()) ;
         
         // Release control. We have seen cases where the begin event 
         // starts alarms and they do not get queued.

         Thread.currentThread().yield() ; 

			// Perform any events keyed on this set first becoming visible.

			showStatus("Beginning \"" + config.getName() + "\" (" + config.getID() + ")" + " ...") ;
         panel.setVisible(true) ;
			Vector v = handler.getEvent("begin") ;
			EventHandler.fireEvents(v,panel,Thread.currentThread(),null) ;

			// Open the first page.  This activates page set and color set events.

         PageSet p = panel.getPage() ;
         Object o = (p != null) ? p.getIdentifier() : null ;
         int n = (o instanceof Integer) ? ((Integer) o).intValue() : 0 ;
			panel.initpage(n) ;
         
         // Make the panel visible.

   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			panel.showpage() ;
			showStatus(null) ;
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.  Close the
		// configuration.

		catch (OutOfMemoryError e)
		{
			EventHandler.stopEventHandler() ;
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("MainFrame: Out of memory.") ;
         String s = Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
				Kisekae.getCaptions().getString("KissSetClosed") ;
   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.
         
         s = captureStackTrace(e,s) ;
			JOptionPane.showMessageDialog(this, s,
            Kisekae.getCaptions().getString("LowMemoryFault"),
				JOptionPane.ERROR_MESSAGE) ;
			savecallback = "closeconfig" ;
			if (!checksave(false)) closeconfig() ;
		}

      // Watch for stack overflow.

      catch (StackOverflowError e)
      {
			EventHandler.stopEventHandler() ;
			Runtime.getRuntime().gc() ;
			PrintLn.println("MainFrame: stack overflow.") ;
         String s = Kisekae.getCaptions().getString("StackOverflowFault") + " - " +
            Kisekae.getCaptions().getString("KissSetClosed") ;
   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.
         
         s = captureStackTrace(e,s) ;
			JOptionPane.showMessageDialog(this, s,
            Kisekae.getCaptions().getString("StackOverflowFault"),
            JOptionPane.ERROR_MESSAGE) ;
			savecallback = "closeconfig" ;
			if (!checksave(false)) closeconfig() ;
      }

		// Watch for internal faults.  Close the configuration.

		catch (Throwable e)
		{
			EventHandler.stopEventHandler() ;
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("MainFrame: Internal fault, " + e.toString()) ;
			e.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError") + " - " +
            Kisekae.getCaptions().getString("KissSetClosed") + "\n" + e.toString() ;
   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.
         
         s = captureStackTrace(e,s) ;
			JOptionPane.showMessageDialog(this, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
			savecallback = "closeconfig" ;
			if (!checksave(false)) closeconfig() ;
		}

		// Initialization is now complete.

		if (fileopen != null) fileopen.close() ;
      fitScreen(getFitScreen(),false) ;
		showStatus(null) ;
      Kisekae.setLoaded(config != null) ;
      if (loader != null)
      {
         loader.setNewConfiguration(null) ;
         loader.close() ;
      }
      KissObject.setLoader(null) ;
      loader = null ;
		Runtime.getRuntime().gc() ;
		try { Thread.currentThread().sleep(300) ; }
		catch (InterruptedException ex) { }
	}
   
   
   // Capture the stack trace.
   
   private String captureStackTrace(Throwable e, String s)
   {
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
      return s ;
   }


	// Method to close our current configuration.  This releases all
	// objects in the current configuration and removes their references
	// from the static hashtables so that they are available for garbage
	// collection.

 	void closeconfig() { closeconfig(false,true) ; }
 	void closeconfig(boolean restart, boolean resetoptions)
	{
		// Perform any events keyed on this configuration terminating.
		// 1.  If the data set is being closed through a 'close' request
		// then we are running under the AWT thread and screen repaints due
		// to cel maps will not be seen because the panel frame closes.
		// 2.  If we are terminating due to 'quit' action request then we
		// are running through the FKiSS event handler and screen paints
		// will be seen, however an event handler termination timeout will
		// occur because we will be interrupting ourselves when we close
		// the configuration.

		if (config != null)
		{
			showStatus("Closing \"" + config.getName() + "\" (" + config.getID() + ")" + " ...") ;
			EventHandler handler = config.getEventHandler() ;
			if (handler != null && !restart && handler.isActive())
			{
				Vector v = handler.getEvent("end") ;
				if (v != null && v.size() > 0)
				{
               endtimer = new Timer(10000,Kisekae.getMainFrame()) ;
               endtimer.setRepeats(false) ;
               endtimer.start() ;
					FKissEvent evt = (FKissEvent) v.elementAt(0) ;
               Thread endthread = new Thread() ;
               endthread.setName("endevent") ;
					evt.fireEvent(panel,endthread,null) ;
               do
               {
   					try { Thread.currentThread().sleep(2000) ; }
   					catch (InterruptedException e) { }
               }
               while (evt.getConfirmWait()) ;
               
               // If we have any alarms initiated by the end event,
               // wait for these alarms to terminate.
               
               while (true)
               {
                  boolean wait = false ;
                  if (config == null) break ;
                  AlarmTimer timer = config.getTimer() ;
                  if (timer == null) break ;
                  Enumeration alarms = timer.getQueue() ;
                  while (alarms.hasMoreElements())
                  {
                     Alarm a = (Alarm) alarms.nextElement() ;
                     if (a.getActivator() != endthread) continue ;
                     wait = true ;
                     break ;
                  }
                  if (!wait) break ;
   					try { Thread.currentThread().sleep(2000) ; }
   					catch (InterruptedException e) { }
               }
				}
			}

			// Close the configuration.  We may be doing a reload of an edited 
         // configuration where we have imported new audio and video files 
         // which have not yet been referenced. All unreferenced objects are
         // dropped on a non-restart close.

         if (endtimer != null) endtimer.stop() ;
			if (config != null) config.close(restart,resetoptions) ;
			if (config != null && !restart) config.flush() ;
         endtimer = null ;

         // Close any established trace window.
         
         if (trace != null)
         {
            traceFKiss(false) ;
            mainmenu.tracefkiss.setState(false) ;
         }
		}

		// Close our frame.

      title = null ;
      WebFrame.clearRedirect() ;
		closeframe() ;
      config = null ;
 		setIconImage(Kisekae.getIconImage()) ;
		showStatus(null) ;
		repaint() ;
	}


	// Method to close our current panel frame.  This establishes the
	// initial splash panel as the main window pane. Note that this
	// method must run under the AWT event handling thread as we are
	// modifying the user interface.

	void closeframe()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { closeframe() ; } } ;
         try { SwingUtilities.invokeAndWait(awt) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
			return ;
		}

      // Close all KissDialog windows that may be open.

		try
		{
			Window [] windows = getOwnedWindows() ;
			if (windows != null)
			{
				for (int i = 0 ; i < windows.length ; i++)
				{
					if (windows[i] instanceof KissDialog)
					{
						KissDialog kd = (KissDialog) windows[i] ;
						kd = kd.getVisibleDialog() ;
						if (kd != null) kd.close() ;
					}
				}
			}
		}
		catch (Exception e) { } 				// Java 1.2 bug 4252492

      // Notify any listeners waiting on configuration changes.

		callback.doClick() ;
      callback = new CallbackButton(this,"MainFrame Callback") ;

		// Close the panel frame.

		if (panel != null) panel.close() ;
		panel = null ;
      trace = null ;
   	fileopen = null ;
		setSplashPane() ;

		// Re-establish the main menu bar for the main frame.

      KissMenu m = (menu instanceof UserMenu) ? panelmenu : menu ;
		if (m != null) m.setFileOpen(null) ;
      setMenu(null) ;
      WebFrame wf = mainmenu.getWebFrame() ;
      if (wf != null) wf.close() ;
   	mainmenu.setFileOpen(null) ;
      mainmenu.setWebFrame(null) ;
		mainmenu.createMenu() ;
      panelmenu = null ;
      loadtext = null ;
		menu = mainmenu ;
      setTitle("UltraKiss") ;
      if (OptionsDialog.getInitMenubar())
      {
         setJMenuBar(menu.getMenuBar()) ;
         updateMenuOptions() ;
         menu.updateRunState() ;
      }
   	if (toolBar != null) toolBar.updateButtons(null,0,0) ;
      
      Hashtable ht = Configuration.getKeyTable() ;
      Enumeration e = ht.elements() ;
      while (e.hasMoreElements())
      {
         Object o = e.nextElement() ;
         if (!(o instanceof Configuration)) continue ;
         Configuration c = (Configuration) o ;
         if (c.isClosed())
         {
            c.flush() ;
         }
      }
      
		// Reclaim memory.

      validate() ;
		Runtime.getRuntime().gc() ;
      setFocus() ;
	}


	// Function to save the configuration if it has been modified.  If a save
	// is inititated or the operation is cancelled this function returns true,
	// otherwise it returns false.

	private boolean checksave() { return checksave(!terminate) ; }
	private boolean checksave(boolean canceloption)
	{
		// If any file in the data set has been modified and we are loading
		// a new configuration we should ask the user if the current
		// configuration should be saved.

      if (Kisekae.isExpired()) return false ;
      
		if (config != null)
		{
			ArchiveFile zip = config.getZipFile() ;
			ArchiveEntry ze = config.getZipEntry() ;
			Object [] options = null ;
			String yes = Kisekae.getCaptions().getString("YesMessage") ;
			String no = Kisekae.getCaptions().getString("NoMessage") ;
			String changes = Kisekae.getCaptions().getString("UpdateDialogViewChanges") ;
			String cancel = Kisekae.getCaptions().getString("CancelMessage") ;
			if (canceloption)
			{
				options = new Object [4] ;
				options[0] = yes ;
				options[1] = no ;
				options[2] = changes ;
				options[3] = cancel ;
			}
			else
			{
				options = new Object [3] ;
				options[0] = yes ;
				options[1] = no ;
				options[2] = changes ;
			}

			// Has anything changed?

         boolean changed = false ;
			if (zip != null && zip.isUpdated()) changed = true ;
         if (ze != null && ze.isUpdated()) changed = true ;
         Vector includefiles = config.getIncludeFiles() ;
         if (includefiles != null)
         {
            for (int i = 0 ; i < includefiles.size(); i++)
            {
               Object include = includefiles.elementAt(i) ;
               if (!(include instanceof ArchiveFile)) continue ;
               if (((ArchiveFile) include).isUpdated()) changed = true ;
            }
         }

			// Show the save dialog.
         
         if (changed)
			{
				while (!Kisekae.isSecure())
				{
					JOptionPane jop = new JOptionPane(
                  Kisekae.getCaptions().getString("UpdateDialogText"),
						JOptionPane.QUESTION_MESSAGE,
						0, Kisekae.getImageIcon(), options ) ;
               String s = Kisekae.getCaptions().getString("UpdateDialogTitle") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
					JDialog dialog = jop.createDialog(this,s) ;
					dialog.show() ;

					// Get the selection.  We repeat this is we view the changed
					// element list.

					Object selected = jop.getValue() ;
					if (selected == null && canceloption) selected = cancel ;
					if (selected == cancel)
					{
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
						savecallback = null ;
						callreturn = false ;
						return true ;
					}

					// Save the data set.  Check for a possible write.

					else if (selected == yes)
					{
						if (config.isChanged())
						{
                     Object cid = config.getID() ;
                  	Vector pages = config.getPages() ;
                     for (int i = 0 ; i < pages.size() ; i++)
                     {
                     	PageSet page = (PageSet) pages.elementAt(i) ;
                        if (!page.isChanged()) continue ;
								page.saveState(cid,"panelframe") ;
								page.restoreState(cid,"panelframe") ;
								State newstate = page.getState(cid,"panelframe") ;

								// Update the page initial state.

								page.setState(cid,"initial",newstate) ;
								if (OptionsDialog.getDebugControl())
									PrintLn.println("Save: Write Page " + page) ;
                     }
                  }
      
                  // Save this element.  Make sure our zip file is closed,
                  // otherwise the write can fail.

                  if (zip != null && zip.isOpen()) 
                  {
                     try { zip.close() ; }
                     catch (IOException e) { }       
                  }
      
						FileSave fs = new FileSave(this,config) ;
				      fs.showall() ;
                  changed = false ;
	               return true ;
					}

					// View changes and then repeat the save request.

	            else if (selected == changes)
	            {
	            	Vector v = (zip != null) ? zip.getUpdated() : new Vector() ;
//                  if (ze != null && !v.contains(ze)) v.addElement(ze) ;
                  if (v.size() == 0 && ze != null) v.addElement(ze) ;

                  // Isolate only unique elements.  Multiple zip entries
                  // can exist for elements that have been cut and pasted.

                  Vector v2 = new Vector() ;
                  for (int i = 0 ; i < v.size() ; i++)
                  {
                  	Object o = v.elementAt(i) ;
                     if (!v2.contains(o)) v2.addElement(o) ;
                  }

                  // Watch for include files with updated elements.

                  if (includefiles != null)
                  {
                     for (int i = 0 ; i < includefiles.size(); i++)
                     {
                        Object include = includefiles.elementAt(i) ;
                        if (!(include instanceof ArchiveFile)) continue ;
                        v = ((ArchiveFile) include).getUpdated() ;
                        if (v != null)
                        {
                           for (int j = 0 ; j < v.size() ; j++)
                           {
                              Object o = v.elementAt(j) ;
                              if (!v2.contains(o)) v2.addElement(o) ;
                           }
                        }
                     }
                  }

                  // Show the updated list.

	               final JList list = new JList(v2) ;
                  list.addMouseListener(new MouseAdapter() 
                  {
                     public void mouseClicked(MouseEvent e) 
                     {
                        if (e.getClickCount() == 2) 
                        {
                           try
                           {
                              int index = list.locationToIndex(e.getPoint()) ;
                              ListModel model = list.getModel() ;
                              Object o = model.getElementAt(index) ;
                              if (!(o instanceof ArchiveEntry)) return ;
                              // Want to show changed object ...
                           }
                           catch (Exception ex) { }
                        }
                     }
                  } ) ;
                  
	               JScrollPane scroll = new JScrollPane(list) ;
	               JOptionPane.showMessageDialog(this,	scroll,
                     Kisekae.getCaptions().getString("UpdateDialogViewTitle"),
                     JOptionPane.INFORMATION_MESSAGE) ;
	            }
               else
               	break ;
            }
         }
 		}

      // No save is required.  Cancel any callback links.

      savecallback = null ;
      callreturn = false ;
      return false ;
   }



	// The action method is used to process file save callback events and
   // fit to screen callback events.  This method is required as part of
   // the ActionListener interface.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		// An update request from the file save window has occured.

      try
      {
			if ("FileWriter Callback".equals(evt.getActionCommand()))
			{
	      	// Update our frame title to reflect the new file names.
				// Format is "Kisekae - configuration [page] [palette] (zipfile)
				// Menu items are updated to reflect the configuration state.

	         if (config != null)
	         {
            	config.setUpdated(false) ;
	         	String s = "UltraKiss - " + config.getName() + " " ;
			  		if (panel != null && panel.getPage() != null)
			  			s += "[" + panel.getPage().getIdentifier() + "] ["
	               	+ panel.getPage().getMultiPalette() + "]" ;
	            if (config.getZipFile() != null)
						s += "  (" + config.getZipFile().getFileName() + ")" ;
               if (title != null) s = title ;
					setTitle(s) ;
					updateMenu() ;
				}

	         // Process the callback request as per our return indicator.

	      	if (savecallback == null) return ;
	         callreturn = true ;
	      	if ("init".equals(savecallback)) init() ;
	         else if ("expand".equals(savecallback)) expand() ;
	         else if ("close".equals(savecallback)) close() ;
				else if ("closepanel".equals(savecallback)) closepanel() ;
				else if ("restart".equals(savecallback)) restart() ;
				else if ("setnew".equals(savecallback)) setNew(NEWFILE) ;
				else if ("closeconfig".equals(savecallback)) closeconfig() ;
				savecallback = null ;
	         callreturn = false ;
				return ;
	      }

			// A completion request from the panel scaling window has occured.

			if ("ImageScale Callback".equals(evt.getActionCommand()))
			{
				if (panel == null) return ;
				panel.fitscreenreturn() ;
			}

         // A timer request from a WindowClosing activity.  If the timer
         // expires then the close activity may have failed.

         if (source == closetimer)
         {
            int i = JOptionPane.showConfirmDialog(this,
               Kisekae.getCaptions().getString("MainFrameTerminateText"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.YES_NO_OPTION) ;
            if (i == JOptionPane.YES_OPTION)
               close() ;
            else
               ((Timer) source).start();
         }

         // A timer request from a end() event.  If the timer
         // expires then the end() event may have failed.

         if (source == endtimer)
         {
            int i = JOptionPane.showConfirmDialog(this,
               Kisekae.getCaptions().getString("EndEventTerminateText"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.YES_NO_OPTION) ;
            if (i == JOptionPane.YES_OPTION)
            {
      			if (config != null) config.close() ;
               KissObject.setLoader(null) ;
               config = null ;
               title = null ;
               closeframe() ;
               setIconImage(Kisekae.getIconImage()) ;
               showStatus(null) ;
               repaint() ;
            }
            else
               ((Timer) source).start();
         }
      }

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.  Close the
      // configuration.

		catch (OutOfMemoryError e)
		{
			EventHandler.stopEventHandler() ;
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			String s = evt.getActionCommand() ;
			PrintLn.println("MainFrame: Out of memory. " + s) ;
   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("KissSetClosed") + "\n" +
            s + " " + Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
			savecallback = "closeconfig" ;
			if (!checksave(false)) closeconfig() ;
		}
   }


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt)
   {
      if (toolBar != null) toolBar.updateRunState() ;
      if (trace != null) trace.updateRunState() ;
      if (menu != null) menu.updateRunState() ;
   }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { kisekae.showHomePage() ; }
	public void windowClosing(WindowEvent evt) { exit() ; }

   // Key Events
   
   public void keyReleased(KeyEvent e) 
   {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
      {
         if (getMenu() == null)
         {
            if (getPanelMenu() != null) setMenu(getPanelMenu()) ;
            else setMenu(getMainMenu()) ; 
            OptionsDialog.setInitMenubar(true);
         }                  
      }
   }
   public void keyPressed(KeyEvent e) { }
   public void keyTyped(KeyEvent e) { }
  
	// Component Events

	public void componentMoved(ComponentEvent evt) { }
	public void componentShown(ComponentEvent evt) { }
	public void componentHidden(ComponentEvent evt) { }
	public void componentResized(ComponentEvent evt) 
   {
      setDefaultSize(getSize()) ;
      centerpanel() ; 
   }


	// Display a status message in the status bar.

	void showStatus(String s)
	{
		if (s == null) s = Kisekae.getCopyright() ;
      if (OptionsDialog.getDebugControl())
      	PrintLn.println("Status " + s) ;
		if (statusBar != null) statusBar.showStatus(s) ;
	}

	// Show an exception error.

	void showError(Exception e) { kisekae.showError(e) ; }

	// Return a reference to our current menu bar.  This reference
	// is used to link the toolbar to the menu action definitions.

	KissMenu getMenu() { return menu ; }

	// Return a reference to our loader text.

	public JTextPane getLoadText() { return loadtext ; }

	// Return a reference to our file loader.

	public FileLoader getLoader() { return loader ; }

	// Return a reference to our main menu.

	public MainMenu getMainMenu() { return mainmenu ; }

   // Return a reference to our original panel menu.

	PanelMenu getPanelMenu() { return panelmenu ; }

	// Return a reference to our status bar object.

	StatusBar getStatusBar() { return statusBar ; }

	// Return a reference to our toolbar object.

	ToolBar getToolBar() { return toolBar ; }

	// Return a reference to our trac3 dialog.

	FKissTrace getTrace() { return trace ; }

	// Return a reference to our current configuration.

	Configuration getConfig() { return config ; }

	// Return a reference to our new configuration.

	Configuration getNewConfig() 
   { return (loader != null) ? loader.getNewConfiguration() : null ; }

	// Return the new configuration ID.

	Object getNewConfigID() { return newconfigid ; }

	// Return a reference to our current panel frame.

	PanelFrame getPanel() { return panel ; }

	// Return true if we are restarting from an initial load.

	boolean isRestart() { return restart ; }

	// Set the restart indicator if the configuration is restarted due to a
   // an edit in the TextEditor.

	void setRestart(boolean b) { restart = b ; }

	// Return a reference to our about dialog.

   AboutBox getAboutDialog() { return about ; }

	// Return a reference to our options dialog.

   OptionsDialog getOptionsDialog() { return options ; }

	// Return a reference to our user frame title.

	String getUserTitle() { return title ; }

	// Return a reference to our scrolling viewport.

   JViewport getViewport()
   {
   	if (scrollpane == null) return null ;
      return scrollpane.getViewport() ;
   }

	// Return a reference to our current page format for printing.

	PageFormat getPageFormat()
   {
      if (pageformat == null)
      {
         PrinterJob pj = PrinterJob.getPrinterJob() ;
         pageformat = pj.defaultPage() ;
      }
      return pageformat ;
   }

   // Return our viewable area dimensions.

   Dimension getArea()
   {
		Dimension area = getSize() ;
		Insets insets = getInsets() ;
		area.width -= (insets.left + insets.right) ;
		area.height -= (insets.top + insets.bottom) ;
     	Dimension d = southpanel.getSize() ;
     	area.height -= (d.height) ;
     	d = northpanel.getSize() ;
     	area.height -= (d.height) ;
      return area ;
   }

   // Get the panel centering offset relative to this frame.

   Point getPanelOffset()
   {
   	Insets insets = getInsets() ;
      if (scrollpane == null) return new Point(insets.left,insets.top) ;
      Point p = scrollpane.getLocationOnScreen() ;
      p.x += x + 2 ;
      p.y += y + 2 ;
      return p ;
   }


	// Turn the status bar on and off.  This event is initiated from
	// the menu commands and requires that the main panel display
	// be repositioned in the frame window.

	void setStatusBar(boolean state)
	{
		if (statusBar == null && state)
		{
			statusBar = new StatusBar(this) ;
			southpanel.add(statusBar,BorderLayout.SOUTH) ;
         statusBar.setStatusBar(state) ;
			showStatus(Kisekae.getCopyright()) ;
		}
		else if (statusBar != null && !state)
		{
         southpanel.remove(statusBar) ;
         statusBar.setStatusBar(state) ;
         statusBar = null ;
      }
		else if (statusBar != null && state)
		{
         statusBar.setStatusBar(state) ;
		}
	}


	// Turn the tool bar on and off.  This event is initiated from
	// the menu commands and requires that the main panel display
	// be repositioned in the frame window.

	void setToolBar(boolean state)
	{
		ToolBar.toolBarOn = state ;
		if (toolBar == null && state)
		{
			toolBar = new ToolBar(this) ;
			northpanel.add(toolBar,BorderLayout.NORTH) ;
		}
		else if (toolBar != null && !state)
		{
			northpanel.remove(toolBar) ;
			toolBar = null ;
		}
      OptionsDialog.setInitToolbar(state) ;
	}


	// Update the tool bar.  This method is called if we add new page sets
	// or color sets to the configuration.  The toolbar buttons begin on
	// their modular boundaries.

	void updateToolBar()
	{
		if (toolBar == null) return ;
		PageSet p = (panel != null) ? panel.getPage() : null ;
		int n = (p != null) ? ((Integer) p.getIdentifier()).intValue() : 0 ;
		int maxps = OptionsDialog.getMaxPageSet() ;
		n =(maxps > 0) ? (n / maxps) * maxps : 0 ;
		Integer mp = (panel != null) ? panel.getMultiPalette() : null ;
		int m = (mp != null) ? mp.intValue() : 0 ;
		int maxcs = OptionsDialog.getMaxColorSet() ;
		m = (maxcs > 0) ? (m / maxcs) * maxcs : 0 ;
		toolBar.updateButtons(config,n,m) ;
	}


   // Update the tool bar run state.  This method is called after a showpage()
   // to ensure that our run button reflects the current FKiSS break state.

   void updateRunState()
   {
      if (panel == null) return ;
      if (toolBar == null) return ;
      toolBar.updateRunState() ;
   }


   // Update the menu.

	void updateMenu()
   {
		if (menu != null) menu.update() ;
		if (toolBar != null) toolBar.update() ;
   }
   
   
   // Update the menu toolbar and statusbar options.  Also ensure that the
   // toolbar and statusbar reflect the menu state.
   
   void updateMenuOptions()
   {
      ToolBar tb = getToolBar() ;
      boolean b = (tb != null) ? tb.toolBarOn : false ;
      setToolBar(OptionsDialog.getInitToolbar()) ;
      if (OptionsDialog.getInitToolbar() != b)
         if (mainmenu != null) 
            mainmenu.toolbar.setState(OptionsDialog.getInitToolbar()) ;
      
      StatusBar sb = getStatusBar() ;
      b = (sb != null) ? sb.getState() : false ;
      setStatusBar(OptionsDialog.getInitStatusbar()) ;
      if (OptionsDialog.getInitStatusbar() != b)
         if (mainmenu != null) 
            mainmenu.statusbar.setState(OptionsDialog.getInitStatusbar()) ;
      
      if (mainmenu != null)
      {
         mainmenu.fitpanel.setSelected(OptionsDialog.getSizeToFit()) ;
         mainmenu.fitscreen.setSelected(OptionsDialog.getScaleToFit()) ;
         mainmenu.showborder.setSelected(OptionsDialog.getShowBorder()) ;
      }
      
      if (!OptionsDialog.getInitMenubar())
         setMenu(null);
      else
      {
         if (getMenu() == null)
         {
            if (getPanelMenu() != null) setMenu(getPanelMenu()) ;
            else setMenu(getMainMenu()) ; 
         }
      }
   }


   // Update our AboutBox and OptionsDialog objects.  This is required
   // to assign new language settings.

   void updateUI()
   {
      if (about != null) about.dispose() ;
      if (options != null) options.dispose() ;
      about = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
      options = new OptionsDialog(this,"UltraKiss Options",false) ;
      if (mainmenu != null) mainmenu.toolbar.setState(false) ;
      if (mainmenu != null) mainmenu.statusbar.setState(false) ;
      ToolBar.toolBarOn = false ;
      StatusBar sb = getStatusBar() ;
      if (sb != null) sb.setStatusBar(false) ;
      mainmenu = new MainMenu(this) ;
      mainmenu.toolbar.setState(options.getInitToolbar()) ;
      mainmenu.statusbar.setState(options.getInitStatusbar()) ;
      mainmenu.showborder.setState(options.getShowBorder()) ;
      setJMenuBar(mainmenu.getMenuBar()) ;
      setFitScreen(options.getScaleToFit()) ;
      setFitPanel(options.getSizeToFit()) ;
      menu = mainmenu ;
      if (!options.getInitMenubar()) setMenu(null) ;
   }


	// A setNew request creates either a new data set or a new page set
	// depending on the current context.  New data sets are invoked from
   // the main menu.  New page sets are invoked from the panel menu.

	void setNew(int type)
	{
   	boolean internal = false ;

		switch (type)
		{

      // New data sets require that we create a new configuration file
		// and a new page set.  Note that new files fall through to create
		// a new page.  New data set configurations are not associated with
		// a named file directory until they are saved.

		case NEWFILE:
			if (!callreturn)
			{
				savecallback = "setnew" ;
				if (checksave()) return ;
         }
			try
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				FileOpen fileopen = new FileOpen(this) ;
				String directory = fileopen.getDirectory() ;
				ArchiveFile zip = new DirFile(fileopen,null) ;
				Configuration c = new Configuration() ;
				String name = c.getID().toString() + ".cnf" ;
				ArchiveEntry ze = new DirEntry(null,name,zip) ;
				c.setZipFile(zip) ;
				c.setZipEntry(ze) ;
				c.setName(name) ;
				c.setUpdated(true) ;
				initframe(c) ;
            // Fall through to create page 0.
            internal = true ;
			}
			catch (IOException e)
         {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         	PrintLn.println("MainFrame: set new configuration IOexception") ;
            e.printStackTrace() ;
         }

		// New page sets add a new set to the current configuration.  The new
		// page is shown.  No objects will exist on this page.

		case NEWPAGE:
			if (panel == null) return ;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			int p = panel.insertPage(internal) ;
			panel.initpage(p) ;
			updateToolBar() ;
			if (menu != null) menu.update() ;
			showpage() ;
			break ;

		// New configurations within a data set can be created when a data set
      // is loaded.  We will create a new .CNF file in the data set and open
      // it for editing.  It is marked as changed to prompt for a file
		// save when the editor is terminated.

		case NEWCONFIG:
			ArchiveFile zip = config.getZipFile() ;
			ArchiveEntry ze = config.getZipEntry() ;
			Configuration c = new Configuration() ;
			String directory = System.getProperty("user.dir") + File.separator ;
			String name = c.getID().toString() + ".cnf" ;
			if (ze == null) ze = new DirEntry(directory,name,zip) ;
			else ze = (ArchiveEntry) ze.clone() ;
			ze.setPath(name);
			c.setZipFile(zip) ;
			c.setZipEntry(ze) ;
			c.setName(name) ;
         c.setSize(config.getSize()) ;
         byte [] b = new byte[0] ;
         try { b = c.write() ; }
         catch (IOException e) { }
			c.setMemoryFile(b) ;
//			c.setMemoryFile(config.getMemoryFile()) ;
			InputStream is = c.getInputStream() ;
			TextFrame tf = new TextFrame(ze,is,false,true) ;
//			tf.showLineNumbers(true) ;
			tf.setChanged() ;
			tf.show() ;
			break ;

		// Insert page sets add a new set to the current configuration using the
		// current page number.  The new page is shown.  No objects will exist on
		// this page.

		case INSERTPAGE:
			if (panel == null) return ;
			PageSet page = panel.getPage() ;
			if (page == null) return ;
			Object o = page.getIdentifier() ;
			if (!(o instanceof Integer)) return ;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			p = panel.insertPage((Integer) o) ;
			panel.initpage(p) ;
			updateToolBar() ;
			if (menu != null) menu.update() ;
			showpage() ;
			break ;

		// Delete page sets remove a page set from the current configuration
		// using the current page number.

		case DELETEPAGE:
			if (panel == null) return ;
			page = panel.getPage() ;
			if (page == null) return ;
			o = page.getIdentifier() ;
			if (!(o instanceof Integer)) return ;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			p = panel.deletePage((Integer) o) ;
			panel.initpage(p) ;
			updateToolBar() ;
			if (menu != null) menu.update() ;
			showpage() ;
			break ;

		// Write page sets updates the page set initial position state
		// using the current page number.

		case WRITEPAGE:
			if (panel == null) return ;
			page = panel.getPage() ;
			if (page == null) return ;
			o = page.getIdentifier() ;
			if (!(o instanceof Integer)) return ;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			p = panel.writePage((Integer) o) ;
			panel.initpage(p) ;
			updateToolBar() ;
			if (menu != null) menu.update() ;
			showpage() ;

			// Show a feedback confirmation to the user.

         String s = Kisekae.getCaptions().getString("MainFrameWriteText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1) s = s.substring(0,i1+1) + p + s.substring(j1) ;
			JOptionPane.showMessageDialog(this, s,
            Kisekae.getCaptions().getString("MainFrameWritePage"),
				JOptionPane.INFORMATION_MESSAGE) ;
			break ;
		}
	}


   // A fitScreen request scales the images to fit the screen
   // viewport dimensions.  This event is initiated from the menu
   // commands and will rescale the images and main panel display
	// to fit within in the frame window.  The viewport may show
   // scrollbars when the fitScreen is requested. When the image
   // is fit the viewport will not have scrollbars.

   void fitScreen(boolean fit) { fitScreen(fit,true) ; }
   void fitScreen(boolean fit, boolean undoable)
   {
      if (panel == null) { OptionsDialog.setScaleToFit(fit) ; return ; }
      if (scrollpane == null) { OptionsDialog.setScaleToFit(fit) ; return ; }
      if (fit != mainmenu.fitscreen.getState()) setFitScreen(fit) ;
      JViewport view = scrollpane.getViewport() ;
		Insets insets = scrollpane.getInsets() ;
		Dimension v = view.getSize() ;
      JScrollBar hsb = scrollpane.getHorizontalScrollBar() ;
      JScrollBar vsb = scrollpane.getVerticalScrollBar() ;
      if (hsb != null && hsb.isVisible()) v.height += hsb.getSize().height ;
      if (vsb != null && vsb.isVisible()) v.width += vsb.getSize().width ;
		int x = v.width - insets.left - insets.right ;
		int y = v.height - insets.top - insets.bottom ;
      panel.fitscreen(fit,x,y,!undoable,true) ;
	}


	// A fitPanel request resizes the window to fit the configuration
	// panel dimensions.  This event is initiated from the menu
	// commands and will calculate a new window size sufficient to
	// display the current configuration data set.

	void fitPanel(boolean fit)
	{
		if (panel == null) { OptionsDialog.setSizeToFit(fit) ; return ; }
		if (scrollpane == null) { OptionsDialog.setSizeToFit(fit) ; return ; }
      if (fit != mainmenu.fitpanel.getState()) setFitPanel(fit) ;
		Insets insets = getInsets() ;
		Dimension panelsize = panel.getPreferredSize() ;
		panelsize.width += (insets.left + insets.right) ;
		panelsize.height += (insets.top + insets.bottom) ;
		insets = scrollpane.getInsets() ;
		panelsize.width += (insets.left + insets.right) ;
		panelsize.height += (insets.top + insets.bottom) ;
		Dimension menusize = (menu != null) ? menu.getMenuBar().getPreferredSize() : new Dimension() ;
		panelsize.height += menusize.height ;
		Dimension toolbarsize = (toolBar != null) ? toolBar.getPreferredSize() : new Dimension() ;
		panelsize.height += toolbarsize.height ;
		Dimension statusbarsize = (statusBar != null) ? statusBar.getPreferredSize() : new Dimension() ;
		panelsize.height += statusbarsize.height ;
		if (panelsize.width < toolbarsize.width) panelsize.width = toolbarsize.width ;
     	Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize() ;
  		screensize.height = (int) (screensize.height*0.95f) ;
      if (!OptionsDialog.getMaximizeWindow() && !fit) screensize = getDefaultSize() ;
      if (OptionsDialog.getRetainWindowSize() && !fit) screensize = getDefaultSize() ;
      if (mainmenu != null && mainmenu.isTutorialDisplayed()) screensize = getSize() ;
		if (panelsize.width > screensize.width) panelsize.width = screensize.width ;
		if (panelsize.height > screensize.height) panelsize.height = screensize.height ;
		screenArea = (fit) ? panelsize : screensize ;
		setSize(screenArea) ;
		validate() ;
		centerpanel() ;

      // Ensure that we have equal border sizes around the panel.

      if (!fit) return ;
      Point offset = panel.getOffset() ;
      int n = Math.max(offset.x,offset.y) ;
      panelsize.width += (-offset.x + n) ;
      panelsize.height += (-offset.y + n) ;
		if (panelsize.width > screensize.width) panelsize.width = screensize.width ;
		if (panelsize.height > screensize.height) panelsize.height = screensize.height ;
		screenArea = panelsize ;
		setSize(screenArea) ;
		validate() ;
		centerpanel() ;
	}


	// A showBorder request updates the options to draw a border around the 
   // set when painted..

	void showBorder(boolean show)
	{
		OptionsDialog.setShowBorder(show) ; 
      if (show != mainmenu.showborder.getState()) setShowBorder(show) ;
      if (panel != null) panel.repaint() ;
   }


	// A traceFKiss request enables diagnostic FKiSS tracing and opens a
   // dialog window to display the trace on the screen.

	void traceFKiss(boolean show)
	{
		if (show && panel != null)
		{
         OptionsDialog.setDebugEvent(show) ; 
         OptionsDialog.setDebugAction(show) ; 
         OptionsDialog.setDebugVariable(show) ;
         if (trace == null)
         {
            trace = new FKissTrace(this) ;
            southpanel.add(trace,BorderLayout.NORTH) ;
         }
         else
            trace.setVisible(true) ;
		}
		else if (!show && trace != null)
		{
         OptionsDialog.setDebugEvent(show) ; 
         OptionsDialog.setDebugAction(show) ; 
         OptionsDialog.setDebugVariable(show) ;
         southpanel.remove(trace) ;
         trace = null ;
		}
   }


	// A debugFKiss request enables diagnostic display of debug() output.
   // It opens a dialog window to display the trace on the screen.  This
   // is the same trace window used for FKiSS tracing.

	void debugFKiss(String message)
	{
		if (panel != null)
		{
         if (trace == null)
         {
            trace = new FKissTrace(this) ;
            southpanel.add(trace,BorderLayout.NORTH) ;
         }
         else 
            trace.setVisible(true) ;
         validate() ;
         centerpanel() ;
		}
      PrintLn.println("debug: " + message) ;
   }


   // Return the current FitScreen menu setting.  This is referenced
   // during configuration load to determine if we must automatically
   // fit the data set to the screen size.

   boolean getFitScreen()
   { return OptionsDialog.getScaleToFit() ; }


	// Return the current FitPanel menu setting.

	boolean getFitPanel()
	{ return OptionsDialog.getSizeToFit() ; }


	// Set the FitScreen menu check box on or off.  This is used to set
	// the check box if we fail to fit to screen during a configuration
	// load through the scale dialog.

	void setFitScreen(boolean state)
	{
		if (mainmenu == null) return ;
		mainmenu.fitscreen.setState(state) ;
   }


	// Set the FitSize menu check box on or off.

	void setFitPanel(boolean state)
	{
		if (mainmenu == null) return ;
		mainmenu.fitpanel.setState(state) ;
	}


	// Set the append flag to show that the loaded configuration is to be
   // appended to the last loaded configuration.

	void setAppend(boolean b) { append = b ; }


	// Set the ShowBorder menu check box on or off.

	void setShowBorder(boolean state)
	{
		if (mainmenu == null) return ;
		mainmenu.showborder.setState(state) ;
	}


	// Set the menu object in use.   This routine should be called under the
   // AWT thread.

	void setMenu(final KissMenu m)
   {
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { setMenu(m) ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

      if (menu instanceof UserMenu) ((UserMenu) menu).clearMenu() ;
      JMenuBar mb = (m != null) ? m.getMenuBar() : null ;
      setJMenuBar(mb) ;
      validate() ;
      menu = m ;
	}


	// Turn the splash panel on.

	void setSplashPane()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { setSplashPane() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

		if (splash == null) splash = new SplashPane(this) ;
		if (scrollpane != null) getContentPane().remove(scrollpane) ;
		getContentPane().add(splash,BorderLayout.CENTER) ;
		setBackground(Color.black) ;
		scrollpane = null ;
	}


	// Create a new splash panel.

	void setNewSplashPane(boolean reset)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { setNewSplashPane(reset) ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

      if (reset) 
      {
         images = -1 ;
         imagenum = -1 ;
      }
		if (splash != null) getContentPane().remove(splash) ;
      splash = null ;
      if (scrollpane != null) return ;
      setSplashPane() ;
      validate() ;
      repaint() ;
 	}


   // Set the main frame cursor.  This overloads the default method
   // to ensure the panel frame maintains the same cursor state as
   // the main frame.  In general we want to restore the default cursor
   // at many places but if we are doing a silent close then we keep any
   // wait cursor showing until such time as the silent close is complete.
   // This happens when a new set is loaded, typically through a viewer(open) 
   // FKiSS action command.

   public void setCursor(Cursor c)
   {
      if (c.equals(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))) 
      {
         if (mainmenu != null)
         {
            FileOpen fd = mainmenu.getFileOpen() ;
            if (fd != null && fd.isSilent()) 
            {
               silentclose = true;
               return ;
            }
         }
         if (silentclose) 
            return ;
      }
      super.setCursor(c) ;
      if (panel == null) return ;
      panel.setCursor(c) ;
   }


   // Set the current print page format for printing.

   void setPageFormat(PageFormat p) { pageformat = p ; }


   // Set the user specified frame title.

   void setUserTitle(String s)
   {
      title = s ;
      if (title != null) setTitle(s) ;
   }
   
   
   // Set for a new load from the user menu.  This resets the LRU paths
   // from any prior load with APPEND directives.
   
   void setNewPreAppend(String lru, String path)
   {
      preappendlru = lru ;
      preappendpath = path ;      
   }

   String getPreAppendPath() { return preappendpath ; }   
   String getPreAppendLru() { return preappendlru ; }   


	// Panel frame functions.  These functions are invoked by menu
	// actions on this frame and must be passed through to the panel
	// frame object.  The menu action may put up a wait cursor.

	void reset() { reset(false) ; }
	void reset(boolean select)
	{
		if (panel != null) panel.reset(select) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

	void restart()
	{
      if (!EventHandler.isActive()) 
      {
   		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         return ;
      }
      
		if (config != null && (config.isUpdated() || config.isOptionChanged()))
		{
      	if (config.isChanged())
         {
				Object cid = config.getID() ;
				Vector pages = config.getPages() ;
            PageSet currentpage = (panel != null) ? panel.getPage() : null ;
            
				for (int j = 0 ; j < pages.size() ; j++)
				{
					PageSet page = (PageSet) pages.elementAt(j) ;
					if (!page.isChanged()) continue ;
               if (page == currentpage) page.saveState(cid,"panelframe") ;
	            int n = page.updateInitialPositions(cid);
	            page.setChanged(false) ;
					if (n > 0 && OptionsDialog.getDebugControl())
						PrintLn.println("Restart: Update page initial positions " + page) ;
            }
			}

         // Retain the updated configuration in memory.  We write cel
         // offsets to the configuration file so that they can be 
         // restored on the restart.

			try
         {
            boolean w = OptionsDialog.getWriteCelOffset() ;
            OptionsDialog.setWriteCelOffset(false) ;
         	byte [] b = config.write() ;
            config.setMemoryFile(b) ;
            OptionsDialog.setWriteCelOffset(w) ;
         }
			catch (IOException e)
			{
				PrintLn.println("MainFrame: restart, " + e.getMessage()) ;
				return ;
			}
		}

      // Close any established trace window.
         
      if (trace != null)
      {
         traceFKiss(false) ;
         mainmenu.tracefkiss.setState(false) ;
         trace = null ;
      }
      
      // Close any internal Media Player currently running
      
      if (config != null)
      {
         MediaFrame mf = config.getMediaFrame() ;
         if (mf != null) mf.stop() ;
      }

      // Initialize the current configuration.  If the configuration has
      // changed we must reload it, otherwise we simply re-initialize the
      // current values.

      restart = true ;
      if (config != null)
      {
         showStatus("Restart \"" + config.getName() + "\" (" + config.getID() + ")" + " ...") ;
   		PrintLn.println("Restart configuration \"" + config.getName() + "\" (" + config.getID() + ")") ;
         if (!config.isRestartable())
            init(config) ;
         else
            initframe(config) ;
      }
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

	void resize(float sf)
	{
		if (panel != null) panel.resize(sf) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

	void initpage(int p)
	{ if (panel != null) panel.initpage(p) ; }

	void initcolor(Integer p)
	{ if (panel != null) panel.initcolor(p) ; }

	void importimage(Cel cel, boolean newgroup)
	{
   	if (panel != null) panel.importImage(cel,newgroup) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void importpalette(Palette palette)
	{
   	if (panel != null) panel.importPalette(palette) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void importaudio(Audio audio)
	{
   	if (panel != null) panel.importAudio(audio) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void importvideo(Video video)
	{
      video.setPanel(panel) ;
   	if (panel != null) panel.importImage(video,false) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void importother(Vector contents, ArchiveFile zip)
	{
   	if (panel != null) panel.importOther(contents,zip) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void addcomponent(JavaCel cel)
	{
      cel.setPanel(panel) ;
   	if (panel != null) panel.importImage(cel,false) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

	void showpage()
	{
		if (panel != null) panel.showpage() ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

	void suspend()
	{
		if (panel != null) panel.suspendEvents() ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

	void resume()
	{
		if (panel != null) panel.resumeEvents() ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}

   void adjustLayer(Vector v1, Vector v2)
   { if (panel != null) panel.adjustLayer(v1,v2) ; }

   boolean fireKeyEvent(KeyEvent e, String type)
   {
   	if (config == null) return false ;
      EventHandler handler = config.getEventHandler() ;
      if (handler == null) return false ;
      char c = e.getKeyChar() ;
      String key = e.getKeyText(e.getKeyCode()) ;
      if (c != KeyEvent.CHAR_UNDEFINED) key = "" + c ;
      if (key == null) return false ;
		Vector evt = handler.getEvent(type) ;
      if (evt == null) return false ;

      // Find the events for the key code.

      Vector keyevents = new Vector() ;
      for (int i = 0 ; i < evt.size() ; i++)
      {
      	FKissEvent event = (FKissEvent) evt.elementAt(i) ;
         String v = event.getFirstParameter() ;
         v = Variable.getStringLiteralValue(v) ;
      	if (!OptionsDialog.getVariableCase() && key.equalsIgnoreCase(v))
           	keyevents.add(event) ;
      	if (OptionsDialog.getVariableCase() && key.equals(v))
           	keyevents.add(event) ;
      }

      // Run the events asynchronously.

      if (keyevents.size() == 0) return false ;
		EventHandler.queueEvents(keyevents,Thread.currentThread(),null) ;
      return true ;
   }

   void editCut()
   {
   	if (panel == null) return ;
      panel.editCut(panel.getSelection()) ;
   }

   void editCopy()
   {
   	if (panel == null) return ;
      panel.editCopy(panel.getSelection()) ;
   }

   void editPaste()
   {
   	if (panel == null) return ;
      Clipboard clipboard = panel.getClipboard() ;
      if (clipboard != null) panel.editPaste(clipboard.getContents(panel)) ;
   }

   void editPasteNew()
   {
   	if (panel == null) return ;
      Clipboard clipboard = panel.getClipboard() ;
      if (clipboard != null) panel.editPasteNew(clipboard.getContents(panel)) ;
   }

	void editUngroup()
   {
   	if (panel == null) return ;
		panel.editUngroup(panel.getSelection()) ;
   }

	void editGroup()
   {
   	if (panel == null) return ;
		panel.editGroup(panel.getSelection()) ;
   }

	void editNewgroup()
   {
   	if (panel == null) return ;
		panel.editNewgroup(panel.getSelection()) ;
   }

	void selectAll(boolean selectall)
   {
   	if (panel == null) return ;
		if (selectall) panel.selectAll() ; else panel.unselectAll() ;
   }

	void selectAllVisible(boolean selectall)
   {
   	if (panel == null) return ;
		if (selectall) panel.selectAllVisible() ; else panel.unselectAll() ;
   }

	void selectFind(Vector v)
   {
   	if (panel == null) return ;
		panel.selectAll(v) ;
   }

	void releaseMouse(boolean b)
   {
   	if (panel == null) return ;
		panel.releaseMouse(b) ;
   }


	// Put the panel frame at the center of the scroll pane.   If the
	// panel frame exceeds the size of the viewport then it is set at
	// the top or left of the viewport and scroll bars will show.

	void centerpanel()
	{
		if (scrollpane == null || panel == null) return ;
		Dimension s = panel.getPreferredSize() ;
		Dimension v = scrollpane.getViewport().getSize() ;
		Insets insets = scrollpane.getInsets() ;
		x = ((v.width - s.width) / 2) + insets.left ;
		y = ((v.height - s.height) / 2) + insets.top ;
		if (x < insets.left) x = insets.left ;
		if (y < insets.right) y = insets.right ;
		panel.setOffset(x,y) ;

      // We must reposition any active movie windows.

      if (config == null) return ;
      Vector movies = config.getMovies() ;
      if (movies != null)
      {
         for (int i = 0 ; i < movies.size() ; i++)
         {
         	Video video = (Video) movies.elementAt(i) ;
           	video.draw(null,null) ;
         }
      }

      // We must reposition any active components.

      Vector components = config.getComponents() ;
      if (components != null)
      {
         for (int i = 0 ; i < components.size() ; i++)
         {
         	Cel comp = (Cel) components.elementAt(i) ;
           	comp.draw(null,null) ;
         }
      }
	}


	// Close this main window frame.  This terminates the program.

	public void close()
	{
      try
      {
         if (!callreturn)
         {
            savecallback = "close" ;
            if (checksave()) return ;
         }

         // Terminate.

         if (options != null) options.resetOptions() ;
   		PrintLn.println("Program terminating.") ;
         OptionsDialog.saveFinalProperties() ; 
         if (kisekae.inApplet())
            LogFile.delete() ;
         else
      		LogFile.stop() ;
      }
      catch (Throwable e) { }
      
      if (kisekae.inApplet()) 
         dispose() ;
      else 
         System.exit(0) ;
	}


	// Close this main window frame on a Kisekae restart from scratch.

	public void dispose()
	{
		super.close() ;
		super.dispose() ;
   }


	// Remove references to objects.

	void flush()
	{
      removeKeyListener(this) ;
		removeWindowListener(this) ;
		removeComponentListener(this) ;
		about = null ;
		loader = null ;
      fileopen = null ;
      config = null ;
   }

   
	// Close the panel frame.  This also closes the configuration and the
   // main frame.

	void closepanel()
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { closepanel() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      
      if (!callreturn)
      {
	      savecallback = "closepanel" ;
	      if (checksave()) return ;
      }

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
		closeconfig() ;
      closeframe() ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      if (OptionsDialog.getRandomSplash()) setNewSplashPane(true) ;      
      if (scrollpane != null) scrollpane.setViewport(null) ;
      scrollpane = null ;
      panel = null ;
 		repaint() ;
   }


	// Exit the program.  An Exit request closes our frame.  
	// Perform any events keyed on this configuration terminating.
   // These events are processed in a separate thread to release
   // the AWT thread.  This enables timeout recognition in the event
   // the termination fails.
   
   public void exit()
   {
      terminate = true ;
		if (config != null)
		{
         Thread thread = new Thread()
         {
            public void run()
            {
               showStatus("Exit \"" + config.getName() + "\" ...") ;
               closetimer = new Timer(10000,Kisekae.getMainFrame()) ;
               EventHandler handler = config.getEventHandler() ;
               closetimer.setRepeats(false) ;
               closetimer.start() ;
               
               // Process all end() events.
               
               if (handler != null)
               {
                  Vector v = handler.getEvent("end") ;
                  if (v != null)
                  {
         				if (v != null && v.size() > 0)
         				{
         					FKissEvent evt = (FKissEvent) v.elementAt(0) ;
         					evt.fireEvent(panel,Thread.currentThread(),null) ;
                        do
                        {
            					try { Thread.currentThread().sleep(1000) ; }
            					catch (InterruptedException e) { }
                        }
                        while (evt.getConfirmWait()) ;
         				}
                  }
               }
               
               // Save any valuepools.
               
               config.saveValuepoolProperties() ;
               closetimer.stop() ;
               close() ;
            }
         } ;

         // Start the thread and exit.

         thread.start() ;
         return ;
      }
      else 
         close() ;
   }


	// Inner class to create a splash panel for the main frame.  This is the
   // window that is shown when a data set configuration has not been loaded.

	class SplashPane extends JPanel
      implements MouseListener
	{
		private Image splashimage = null ; 		// The program splash image
		private Image ultraimage = null ; 		// The program name image
		private Image backimage = null ; 	   // The program background image
		private URL splashImageURL = null ;		// The URL for the splash file
		private URL ultraImageURL = null ;		// The URL for the name file
		private URL backImageURL = null ;	   // The URL for the back file
		private String description = "" ;      // The description for the back file
      private boolean nosplash = false ;     // True if no paint logo
		private Dimension size = null ;			// The panel size

		// Constructor

		public SplashPane(MainFrame parent)
		{
			// Pick up the logo splash image.

			try
			{
            String s = OptionsDialog.getSplashDir() ;
      
            // The SplashSetNumber is the definitive set to show.  
            // If the SplashDir is not correct set it to the correct 
            // directory and back image set value.
      
            int backset = OptionsDialog.getSplashSetNumber() ;
            String s1 = OptionsDialog.getSplashSetName(backset-1) ;
            if ("".equals(s1)) 
               OptionsDialog.setSplashDir(Kisekae.getSplashDir()) ;
            else
            {
               s1 = Kisekae.getSplashDir() + s1 + ".jpg" ;
               if (!s1.equals(s)) OptionsDialog.setSplashDir(s1) ;
            }
            s = OptionsDialog.getSplashDir() ;
            
            // Get the next splash image.  

            try 
            { 
               int n = images ;  
               int m = s.indexOf('.') ;
               if (n < 0)
               {
                  for ( n = 1 ; ; n++ )
                  {
                     images++ ;
                     s1 = s.substring(0,m) + n + s.substring(m) ;
                     URL u = Kisekae.getResource(s1) ;
                     if (u == null) break ;
                  }
               }
               if (imagenum < 0 && OptionsDialog.getRandomSplash())
                  imagenum = (int) (Math.random()*(images)) ;
               else
                  imagenum = (imagenum + 1) % (images+1) ;
               if (imagenum == images) imagenum = 0 ;
               s = s.substring(0,m) + (imagenum+1) + s.substring(m) ;
               backImageURL = Kisekae.getResource(s) ;
               if (backImageURL != null)
                  backimage = Toolkit.getDefaultToolkit().getImage(backImageURL) ;
               
               // Look for image description

               description = "" ;
               s1 = s.substring(0,m) + "_reference.txt";
               URL reference = Kisekae.getResource(s1) ;
               if (reference != null && backimage != null)
               {
                  s1 = reference.getPath() ;
                  s1 = s1.replaceAll("^.*[\\/\\\\]", "/") ;
                  s1 = s1.substring(s1.lastIndexOf("/")+1) ;         
                  s1 = s1.replace("_reference.txt","") + (imagenum+1) ;
                  InputStream input = reference.openStream();
                  Scanner scanner = new Scanner(input) ;
                  while (scanner.hasNextLine())
                  {
                     nosplash = false ;
                     String line = scanner.nextLine() ;
                     if (line.isEmpty()) continue ;
                     if (line.startsWith(";")) continue ;
                     String [] parts = line.split("\\|") ;
                     if (parts.length > 1 && s1.equalsIgnoreCase(parts[0].trim()))
                        description = parts[1].trim() ;
                     if (parts.length > 2 && "nosplash".equalsIgnoreCase(parts[2].trim()))
                        nosplash = true ;
                     if (!description.isEmpty()) break ;
                  }
               }
            }
            catch (Exception e) { } 
            
            // Overlay the UltraKiss splash images.
            
            splashImageURL = Kisekae.getResource(splashImage) ;
		      if (splashImageURL != null)
   				splashimage = Toolkit.getDefaultToolkit().getImage(splashImageURL) ;
            ultraImageURL = Kisekae.getResource(ultraImage) ;
		      if (ultraImageURL != null)
   				ultraimage = Toolkit.getDefaultToolkit().getImage(ultraImageURL) ;
				MediaTracker mt = new MediaTracker(this) ;
				if (backimage != null) mt.addImage(backimage,0) ;
				if (splashimage != null) mt.addImage(splashimage,0) ;
	 			if (ultraimage != null) mt.addImage(ultraimage,0) ;
				mt.waitForAll(1000) ;
				if (mt.isErrorAny())
            	throw new KissException("MainFrame: error loading splash image.") ;
			}
			catch (Exception e)
         {
            if (!(e instanceof KissException)) PrintLn.println(e.getMessage()) ;
         }

         Dimension d = parent.getSize() ;
			Insets insets = parent.getInsets() ;
			int w = d.width - insets.left - insets.right ;
			int h = d.height - insets.top - insets.bottom ;
			size = new Dimension(w,h) ;
			setBackground(Color.black) ;
         setLayout(null) ;
			setSize(size) ;
         addMouseListener(this) ;
		}

		// The paint routine will show the program splash image if if exists.

		public void paintComponent (Graphics g)
		{
         String copyright = Kisekae.getCopyrightDate() ;
         String releaselevel = Kisekae.getReleaseLevel() ;
         g.setFont(new Font("SansSerif",Font.BOLD,12)) ;
         g.setColor(new Color(180,160,140)) ;
         FontMetrics fm = g.getFontMetrics() ;
			super.paintComponent(g) ;

			int w1 = (backimage != null) ? backimage.getWidth(null) : 0 ;
			int h1 = (backimage != null) ? backimage.getHeight(null) : 0 ;
			int x1 = (w1 < 0) ? 0 : ((getSize().width - w1) >> 1) ;
			int y1 = (h1 < 0) ? 0 : ((getSize().height - h1) >> 1) ;
			int w2 = (splashimage != null) ? splashimage.getWidth(null) : 0 ;
			int h2 = (splashimage != null) ? splashimage.getHeight(null) : 0 ;
			int x2 = (w2 < 0) ? 0 : ((getSize().width - w2) >> 1) ;
			int y2 = (h2 < 0) ? 0 : ((getSize().height - h2) >> 1) ;
			int w3 = (ultraimage != null) ? ultraimage.getWidth(null) : 0 ;
			int h3 = (ultraimage != null) ? ultraimage.getHeight(null) : 0 ;
			int x3 = (w3 < 0) ? 0 : ((getSize().width - w3) >> 1) ;
			int y3 = (h3 < 0) ? 0 : ((getSize().height - h3) >> 1) ;
         int w4 = fm.stringWidth(copyright) ;
         int h4 = fm.getAscent() ;
			int x4 = (w4 < 0) ? 0 : ((getSize().width - w4) >> 1) ;
			int y4 = (h4 < 0) ? 0 : ((getSize().height - 20) - h4) ;
         int w5 = fm.stringWidth(releaselevel) ;
         int h5 = fm.getAscent() ;
			int x5 = (w5 < 0) ? 0 : ((getSize().width - w5) >> 1) ;
			int y5 = (h5 < 0) ? 0 : ((getSize().height - 20) - (h4+h5+(h5/2))) ;
         int w6 = fm.stringWidth(description) ;
         int h6 = fm.getAscent() ;
			int x6 = (w6 < 0) ? 0 : ((getSize().width - w6) >> 1) ;
			int y6 = (h6 < 0) ? 0 : ((getSize().height - h6)) ;
         
			if (backimage != null)
				g.drawImage(backimage,x1,y1,this) ;
			if (splashimage != null && !nosplash)
				g.drawImage(splashimage,x2,y2,this) ;
			if (ultraimage != null && !nosplash)
				g.drawImage(ultraimage,x3,y3,this) ;
         if (copyright != null)
            g.drawString(copyright,x4,y4) ;
         if (releaselevel != null)
            g.drawString(releaselevel,x5,y5) ;
         if (!description.isEmpty())
         {
            g.setColor(new Color(180,160,140,128)) ;
            g.drawString(description,x6,y6) ;
         }
		}
      
      public void mouseClicked(MouseEvent e) 
      { if (e.getClickCount() > 1) setNewSplashPane(false) ; }
      
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) { }
      
      public boolean hasBackImage() { return backimage != null ; }
	}
}
