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
* FileLoader Class
*
* Purpose:
*
* This class is a frame used to display load messages and errors
* as a KiSS set is established. Configuration errors and warnings
* are displayed. If errors exist an option to edit the configuration
* file is provided.
*
*/

import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.SwingUtilities ;
import javax.swing.text.* ;
import java.util.Vector ;
import java.util.Collections ;
import java.util.Comparator ;
import java.net.* ;
import java.io.* ;

final class FileLoader extends KissFrame
	implements WindowListener, ActionListener, Runnable
{
	private static int count = 0 ;			// Count of class instances
	private MainFrame parent = null ;		// The parent frame
	private FileLoader me = null ;		   // Reference to ourselves
	private Thread thread = null ;			// The loader thread
	private ArchiveFile zip = null ;			// The zip file object
	private ArchiveEntry ze = null ;			// The zip entry object
	private Configuration config = null	;	// The new configuration
	private Configuration reference = null	;	// The reference configuration
	private Configuration newconfig = null	;	// The created configuration
   private String filename = null ;       // The archive being loaded
   private Vector errormsgs = null ;      // The configuration error messages
   protected boolean stop = false ;       // If true, loader stops

	// Status indicators

	private boolean active = true ;			// True if frame has focus
	private boolean reload = false ;			// True if reload configuration
	private boolean expansion = false ;		// True if loading expansion set
   private boolean interrupted = false ;	// True if load is interrupted
   private boolean fatal = false ;			// True if fatal error
	private int errors = 0 ;					// Count of showError calls
	private int warnings = 0 ;					// Count of showWarning calls
	private int faults = 0 ;					// Errors plus Warnings
	private int progress = 0 ;					// Progress bar value
	private long time = 0 ;						// Duration of load
   
   // Text attributes

   private int errorpos = -1 ;            // Error position
   private static SimpleAttributeSet errorset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet warnset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet normalset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet showset = new SimpleAttributeSet() ;
   
   static
   {
      StyleConstants.setForeground(errorset,Color.red) ;
      StyleConstants.setForeground(warnset,Color.gray) ;
      StyleConstants.setForeground(showset,new Color(128,0,0)) ;
   }

	// Edit text frame references

	private TextFrame tf = null ;				// The edit text frame
	private InputStream is = null ;			// The file input stream
	private OutputStream os = null ;			// The file output stream

   // Control definitions

	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private JPanel jPanel1 = new JPanel();
 	private JPanel jPanel1a = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JLabel Status = new JLabel();
	private JLabel FileName = new JLabel();
	private JProgressBar Progress = new JProgressBar();
	private JLabel jLabel1 = new JLabel();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JTextPane TextWindow = new JTextPane();
	private JButton CANCEL = new JButton();
	private JButton EDIT = new JButton();
	private JButton PLAY = new JButton();


	// Constructor to reload the current configuration.

	public FileLoader(MainFrame frame, Configuration c)
	{
		this(frame,c,c.getZipFile(),c.getZipEntry()) ;
		reload = true ;
	}

	// Constructor to load an expansion configuration.

	public FileLoader(MainFrame frame, Configuration c, ArchiveFile zip, ArchiveEntry ze)
	{
		this(frame,zip,ze) ;
		config = c ;
      if (config != null) 
      {
         expansion = true ;
         if (OptionsDialog.getDebugControl())
            System.out.println("Loading expansion configuration " + config) ;
      }
	}

	// Constructor to load a new configuration.

	public FileLoader(MainFrame frame, ArchiveFile zip, ArchiveEntry ze)
	{
      me = this ;
		parent = frame ;
		this.zip = zip ;
		this.ze = ze ;
      setIconImage(parent.getIconImage());

		// Set the frame characteristics.

		try { jbInit(); }
		catch(Exception e)
		{ e.printStackTrace(); }

		// Center the frame in the screen space.

 		setSize(550,350) ;
 		center(this,parent) ;

		// Scroll to last line always.

      DefaultCaret caret = (DefaultCaret) TextWindow.getCaret(); 
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);       

		// Setup to catch window events in this frame.

		addWindowListener(this) ;
      CANCEL.addActionListener(this) ;
      EDIT.addActionListener(this) ;
      PLAY.addActionListener(this) ;
      boolean b = OptionsDialog.getShowLoad() ;
      KissFrame f = Kisekae.getBatchFrame() ;
      boolean b1 = (f instanceof WebSearch.WebSearchFrame)
         ? !((WebSearch.WebSearchFrame) f).isLocalSearch() : true ;
      if (OptionsDialog.getUseDefaultWS()) b = b1 ;
      if (!Kisekae.isBatch() || b) setVisible(true) ;
	}

   // User interface initialization.

	private void jbInit() throws Exception
	{
		Status.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
		Status.setPreferredSize(new Dimension(200, 20));
		FileName.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
		FileName.setPreferredSize(new Dimension(300, 20));
		FileName.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText(Kisekae.getCaptions().getString("FileLoaderText"));
      jLabel1.setVisible(false);
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		EDIT.setVisible(false);
		EDIT.setText(Kisekae.getCaptions().getString("EditMessage"));
		PLAY.setEnabled(false);
		PLAY.setText(Kisekae.getCaptions().getString("OkMessage"));
		Progress.setPreferredSize(new Dimension(300, 16));

		jPanel1.setLayout(borderLayout2);
 		jPanel1a.setLayout(borderLayout3);
		jPanel1a.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 		jPanel2.setLayout(borderLayout4);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		this.getContentPane().setLayout(borderLayout1);
		TextWindow.setEditable(false);
		this.getContentPane().add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel1a, BorderLayout.NORTH);
		jPanel1a.add(Status, BorderLayout.WEST);
		jPanel1a.add(FileName, BorderLayout.EAST);
		jPanel1a.add(Progress, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jLabel1, BorderLayout.NORTH);
		jPanel2.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(TextWindow, null);
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(PLAY, null);
      jPanel3.add(Box.createGlue()) ;
		jPanel3.add(EDIT, null);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(CANCEL, null);
      jPanel3.add(Box.createGlue()) ;
	}


	// The loader code.  This code runs as a separate thread.  It will
	// initialize a new Configuration object, load all the necessary
	// files, and finally initialize the main frame window to display
	// the panel frame.  This thread runs at low priority.

	public void run()
	{
		count = count + 1 ;
		thread = Thread.currentThread() ;
		thread.setName("File Loader " + count) ;
		thread.setPriority(Thread.MIN_PRIORITY) ;
		time = System.currentTimeMillis() ;
		errors = warnings = 0 ;
      errormsgs = null ;
      jLabel1.setVisible(false);
		EDIT.setVisible(false);
		PLAY.setEnabled(false);
		TextWindow.setText("") ;
		TextWindow.setCaretPosition(0);
		if (OptionsDialog.getDebugControl())
			System.out.println("Configuration loader " + thread.getName() + " active.") ;
      
      // Bring the load window to the front on the AWT thread
      
      Runnable tofront = new Runnable()
		{ 
         public void run() 
         { 
            me.toFront() ;
            me.requestFocus();
         }	
      } ;
		javax.swing.SwingUtilities.invokeLater(tofront) ;

		// Initialization.  We are given a KiSS configuration file name
		// and we construct the window panel environment to properly
		// display this data set.  This method is called when we need to
		// initialize for a new data set.

		try
		{
			if (ze != null)
			{
				// Determine the data set and configuration file names.

				String file = (zip == null) ? "" : zip.getName() ;
				String name = (ze == null) ? "" : ze.getPath() ;
				if (zip instanceof DirFile) file = name ;
            filename = file ;

				// Set up the window.

				showStatus(Kisekae.getCaptions().getString("OpenConfigStatus")) ;
            String s = Kisekae.getCaptions().getString("LoadingText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + name + s.substring(j1+1) ;
				setTitle(s) ;
            s = Kisekae.getCaptions().getString("KissSetText") ;
            i1 = s.indexOf('[') ;
            j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + file + s.substring(j1+1) ;
				showText(s) ;
            s = Kisekae.getCaptions().getString("FileNameText") ;
            i1 = s.indexOf('[') ;
            j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + name + s.substring(j1+1) ;
				showFile(s) ;

				// Open the data set configuration.  We always create a new
            // configuration object, but if the configuration was edited
            // we must reload the current configuration for reprocessing.
            // If the configuration is an expansion set we will also need
            // our current configuration as the base reference.

				reference = config ;
				config = new Configuration() ;
            config.setName(file) ;
				config.setKey(config.getKeyTable(),config.getID(),name.toUpperCase()) ;
				if (reload)
					config.openref(reference) ;
				else
					config.open(zip,ze,reference) ;

				// Read the configuration file.

            if (stop) thread.interrupt() ;
            MainFrame mf = Kisekae.getMainFrame() ;
            OptionsDialog options = mf.getOptionsDialog() ;
            if (!reload && !expansion && options != null) 
               options.resetOptions() ;
				config.setLoader(this) ;
				showStatus(Kisekae.getCaptions().getString("ReadConfigStatus")) ;
				config.read() ;
            
            // If we have a reference configuration that used INCLUDE files
            // then the reference include file archives should be used in the 
            // current configuration.  This ensures that the same archive entry 
            // objects are used for cels when loaded and fixes a memory leak.
            
            if (stop) thread.interrupt() ;
            if (reference != null)
            {
               Vector includefiles = reference.getIncludeFiles() ;
               config.setIncludeFiles(includefiles) ;              
            }

            // If we loaded from a URL and our configuration referenced
            // INCLUDE files then we need to download the included files
            // to set up for proper loading.  The list of included files
            // is corrected to reference the downloaded file objects.

            if (stop) thread.interrupt() ;
            FileOpen fo = config.getFileOpen() ;
            Vector includefiles = config.getIncludeFiles() ;
            URL sourceURL = (fo != null) ? fo.getSourceURL() : null ;

            // Download each include file.  We track the download in
            // this FileLoader window to enable window closure if the
            // load is cancelled or aborted.

            if (includefiles != null)
            {
     				showStatus(Kisekae.getCaptions().getString("LoadIncludeStatus")) ;
               for (int i = 0 ; i < includefiles.size() ; i++)
               {                  
                  Object o = includefiles.elementAt(i) ;
                  if (o instanceof File && sourceURL == null) continue ;
                  if (!(o instanceof URL || o instanceof File)) continue ;
                  s = Kisekae.getCaptions().getString("FileNameText") ;
                  i1 = s.indexOf('[') ;
                  j1 = s.indexOf(']') ;
                  if (i1 >= 0 && j1 > i1)
                     s = s.substring(0,i1) + o.toString() + s.substring(j1+1) ;
      				showFile(s) ;
        			   if (o instanceof File) 
                     o = new URL(sourceURL,((File) o).getName()) ;
                  o = download((URL) o) ;
                  includefiles.setElementAt(o,i) ;
               }
            }

				// Load the cel images.

            if (stop) thread.interrupt() ;
				showStatus(Kisekae.getCaptions().getString("LoadKissStatus")) ;
				config.load() ;

				// Set up the cel groups and the page sets.

            if (stop) thread.interrupt() ;
				showStatus(Kisekae.getCaptions().getString("InitializeStatus")) ;
				FileName.setText("") ;
				config.init() ;

				// Finished.

				showStatus(Kisekae.getCaptions().getString("LoadCompleteStatus")) ;
            s = Kisekae.getCaptions().getString("FinishedText") ;
            i1 = s.indexOf('[') ;
            j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + name + s.substring(j1+1) ;
				setTitle(s) ;
				config.setLoader(null) ;
            
            // If we had a reference configuration release all objects.
            // This prevents a memory leak from a load-edit-load cycle.
            
            if (reference != null && !expansion) 
            {
               reference.close(false,false) ;
               reference.flush() ;
            }
            newconfig = config ;
			}

         // Show the load status to the user.

         if (stop) thread.interrupt() ;
  			time = System.currentTimeMillis() - time ;
         String s = Kisekae.getCaptions().getString("LoadTimeText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + (time/1000+1) + s.substring(j1+1) ;
    		showText(s) ;
     		if (config != null)
     		{
     			int n = config.getLoadBytes() / 1024 ;
            s = Kisekae.getCaptions().getString("LoadBytesText") ;
            i1 = s.indexOf('[') ;
            j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + n + s.substring(j1+1) ;
     			showText(s) ;
     		}

     		// Continue with the main panel frame initialization.  This
     		// is automatic if the load was error free and this frame
     		// currently has active focus.  Note that we must run the
     		// callback function under the AWT eventhandler thread to
     		// eliminate potential thread problems.

         faults = errors ;
         if (OptionsDialog.getStrictSyntax()) faults += warnings ;
    		if (faults == 0) showText(Kisekae.getCaptions().getString("NoErrorsText")) ;
     		if (faults > 0) showText(errors + " Errors, " + warnings + " Warnings") ;
     		if (active && (faults == 0 || OptionsDialog.getAcceptCnfErrors() || Kisekae.isBatch()))
     		{
            try { thread.sleep(1000) ; }
            catch (InterruptedException e) { }
     			Runnable callback = new Runnable()
     			{ public void run() { parent.initframe() ; }	} ;
     			javax.swing.SwingUtilities.invokeLater(callback) ;
     		}

     		// Signal completion to the user.  Bring load window into focus.
         // Do this on the AWT thread.  We were receiving NullPointerException
         // on javax.swing.text.FlowView$FlowStrategy.layoutRow(FlowView.java:546)

         else
         {
            Runnable complete = new Runnable()
            { public void run() { signalComplete() ; } } ;
            javax.swing.SwingUtilities.invokeLater(complete) ;
         }
		}
      catch (InterruptedException e) { interrupted = true ; }

		catch (OutOfMemoryError e)
		{
      	fatal = true ;
			showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showError(Kisekae.getCaptions().getString("LowMemoryFault")) ;
			if (config != null) config.close() ;
			config = null ;
		}

      catch (MalformedURLException e)
      {
      	fatal = true ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
			showError(e.toString()) ;
			if (config != null) config.close() ;
			config = null ;
      }

      catch (ConnectException e)
      {
      	fatal = true ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
			showError(e.toString()) ;
			if (config != null) config.close() ;
			config = null ;
      }

		catch (Throwable e)
		{
      	fatal = true ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
			showError(e.toString()) ;
         e.printStackTrace() ;
			if (config != null) config.close() ;
			config = null ;
		}

		if (OptionsDialog.getDebugControl())
         System.out.println("Configuration loader " + thread.getName() + " terminates.") ;
	}
   
   
   private void signalComplete()
   {
		jLabel1.setVisible(true);
		if (faults != 0 && !fatal) EDIT.setVisible(true) ;
   	if (config != null) PLAY.setEnabled(true) ;
	   KissObject.setLoader(null) ;
	   validate() ;
      requestFocus() ;     
   }


   // A function to download an INCLUDE file from the web.  We return the
   // File or MemFile object created as a result of the download.

   private Object download(URL url) throws IOException
   {
      String pathname = null ;
      MemFile memfile = null ;
      String file = url.getFile() ;
      File f = new File(file) ;
      file = f.getName() ;
		int n = (file == null) ? -1 : file.lastIndexOf('.') ;
		String extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
      if (n > 0) file = file.substring(0,n) ;
      showText("Download INCLUDE file " + url) ;
      System.out.println("Download INCLUDE file " + url.toExternalForm()) ;

      // Determine if the file exists in our download cache directory.
      // Check based on filename and last date modified.  Natural sort
      // is ascending so search backwards to get latest date if duplicate
      // cached files exist.

      boolean incache = false ;
      String cachepath = Kisekae.getCachePath() ;
      File directory = (cachepath != null) ? new File(cachepath) : null ;
      if (!OptionsDialog.getCacheInclude()) directory = null ;
      if (directory != null)
      {
         Vector v = new Vector() ;
         File [] files = directory.listFiles() ;
         if (files == null) 
         {
            files = new File [0] ;
            directory = null ;
         }
         
         for (int i = 0 ; i < files.length ; i++)
         {
            long date = files[i].lastModified() ;
            String name = files[i].getName() ;
            v.add(name+"\t"+date) ;
         }
         
         Collections.sort(v, new Comparator(){
            public int compare(Object a, Object b)
            {
               String [] parts1 = ((String) a).split("\t") ;
               String [] parts2 = ((String) b).split("\t") ;
               if (parts1.length > 1 && parts2.length > 1)
                  return (parts1[1].compareTo(parts2[1])) ;
               return 0 ;              
            }
         }) ;
          
         for (int i = v.size()-1 ; i >= 0 ; i--)
         {
            if (((String) v.elementAt(i)).startsWith("UltraKiss-"+file))
            {
               String key = (String) v.elementAt(i) ;
               String [] parts = key.split("\t") ;
               f = new File(cachepath+parts[0]) ;
               pathname = f.getPath() ;
               incache = true ;
               System.out.println("URL File " + pathname + " located in cache.") ;
               break ;
            }
         }
      }

   	// Setup the result object.  If we are running in the sandbox we
      // cannot create a file.  Load to memory.

      if (!incache)
      {
         try
         {
            f = File.createTempFile("UltraKiss-"+file,extension,directory) ;
            if (!OptionsDialog.getCacheInclude()) f.deleteOnExit() ;
            pathname = f.getPath() ;
            System.out.println("Create URL file " + pathname + " in cache.") ;
            String s = Kisekae.getCaptions().getString("OpenURLFile") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + f.getName() + s.substring(j1+1) ;
    	      showText(s) ;
         }
         catch (Exception e)
         {
            pathname = null ;
            System.out.println("Create URL memory file, cache exception " + e.getMessage()) ;
     	      showText(Kisekae.getCaptions().getString("OpenMemoryFile")) ;
         }
      }

      // Open the URL session.

      int bytes = 0 ;
		showStatus(Kisekae.getCaptions().getString("OpenConnectionStatus")) ;
      URLConnection c = url.openConnection() ;
      // HTTP Response code 403 for URL
//      c.addRequestProperty("User-Agent", "UltraKiss");      

      // Authorize the session if necessary.

      InputStream is = null ;
      OutputStream os = null ;
      String connid = Kisekae.getConnectionID() ;
      if (connid != null)
         c.setRequestProperty("Authorization", "Basic " + connid);
      int completed = 0 ;
      int size = c.getContentLength() ;
      initProgress(size) ;
      
      // Open the stream and read the data.  Read the stream if not cached  
      // or the content size differs from cached file size.
      
      if (!incache || size != f.length())
      {
         String s = (size / 1024) + "K" ;
         String s1 = Kisekae.getCaptions().getString("TransferText") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
         showText(s1) ;
         is = c.getInputStream();
         if (pathname != null)
            os = new FileOutputStream(f) ;
         else
            os = new ByteArrayOutputStream(size) ;

         // Read the data.

         int b = 0 ;
         s1 = Kisekae.getCaptions().getString("ReadDataStatus") ;
         i1 = s1.indexOf('[') ;
         j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1+1) + s + s1.substring(j1) ;
      	showStatus(s1) ;

         while (!stop && (b = is.read()) >= 0)
         {
           	bytes++ ;
            completed++ ;
            os.write(b) ;
            if (completed >= 2048)
            {
               completed -= 2048 ;
               updateProgress(2048) ;
            }
         }
      }

      // Close the connection.

      updateProgress(completed) ;
		showStatus(Kisekae.getCaptions().getString("CloseConnectionStatus")) ;
      if (is != null) is.close() ;
      if (os != null) os.close() ;

		// Finished.

   	showText(Kisekae.getCaptions().getString("TransferCompleteStatus")) ;
      if (OptionsDialog.getDebugLoad())
         System.out.println("Open URL data transfer bytes " + bytes) ;

      // Create a memory byte array if we are in a secure environment.

      if (os instanceof ByteArrayOutputStream)
      {
         ByteArrayOutputStream bos = (ByteArrayOutputStream) os ;
         memfile = new MemFile(url.getFile(),bos.toByteArray()) ;
         os = null ;
      }

      // Return the appropriate file object.

      if (memfile != null) return memfile ;
      return f ;
   }



	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { active = true ; }
	public void windowDeactivated(WindowEvent evt) { active = false ; }
	public void windowClosing(WindowEvent evt)
	{
   	System.out.println("Load cancelled ...") ;
      stop = true ;
		if (config != null) config.close() ;
      Kisekae.setLoaded(false) ;
		close() ;
	}


	// Action Events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;


		// A Play request initializes the main panel frame.  The callback is
      // being performed under the AWT thread.

		if (source == PLAY)
		{
   		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			parent.initframe(config) ;
			close() ;
         return ;
		}


		// A Cancel request stops the loader thread.  Our callback reverts
      // to the previously loaded configuration.

		if (source == CANCEL)
		{
         stop = true ;
	   	System.out.println("Load cancelled ...") ;
			if (config != null) config.close() ;
			parent.initframe(null) ;
         Kisekae.setLoaded(false) ;
			close() ;
         return ;
		}


		// An Edit request brings up a configuration text edit window.
		// When the edit window is closed and if the text was updated,
		// we must capture the event for post-update processing.  We
		// do this by attaching an action listener the to text editor
		// callback button.

		if (source == EDIT)
		{
      	if (ze == null) return ;
         if (reload && config == null) config = reference ;

        	// If the configuration exists, edit its memory contents.

			if (config != null)
         {
				is = config.getInputStream() ;
				tf = new TextFrame(ze,is,true,true) ;
         }

        	// Edit the file contents as we did not successfully create a
         // configuration object.  We may need to open the file.

         else
         {
            if (zip == null) return ;
            if (!zip.isOpen())
            {
               try
               {
                  zip.open() ;
                  ze = zip.getEntry(ze.getPath()) ;
                  if (ze == null) { zip.close() ; return ; }
               }
               catch (IOException e) { return ; }
            }
            tf = new TextFrame(ze,null,true,true) ;
         }

         // Highlite any syntax error lines in the text edit window.

         tf.setErrorLine(-1) ;
         if (errormsgs != null)
         {
            for (int i = 0 ; i < errormsgs.size() ; i++)
            {
               String s = (String) errormsgs.elementAt(i) ;
               tf.setErrorLine(s) ;
            }
         }

         // Show the file text edit window.

			tf.callback.addActionListener(this) ;
			tf.setVisible(true) ;
         return ;
		}


		// An update request from the text edit window has occured.
		// The configuration memory image file will be updated.  The
      // edit window will be closed and the revised configuration
      // will be processed.

		if ("TextFrame Callback".equals(evt.getActionCommand()))
		{
         if (tf == null) return ;
         TextObject text = tf.getTextObject() ;
			tf.dispose() ;
         tf = null ;

         // Access the updated configuration text.  If we have an actual
         // configuration object we apply the new text to the memory
         // resident configuration.  If we do not have a configuration
         // object we must save the configuration text and re-read the
         // file.

         if (config != null)
         {
				ByteArrayOutputStream out = new ByteArrayOutputStream() ;
				try {	if (text != null) text.write(null,out,null) ; }
				catch (IOException e)
				{
	            System.out.println("I/O Exception: " + e.toString()) ;
	            e.printStackTrace() ;
               return ;
	         }
				finally
				{
					try { if (out != null) out.close() ; }
					catch (IOException e)
					{
	               System.out.println("I/O Exception: " + e.toString()) ;
	               e.printStackTrace() ;
                  return ;
	            }
				}
            
            // Close the loaded configuration as we are going to reload it.
            // This frees cel memory and fixes a reload memory leak.  Note,
            // we can lose references to imported images if we release cels.
            
//          config.close() ;
//          config.close(true,true) ;
            config.setRestartable(false) ;

	         // Open our zip file as it can be necessary for the reload. On
            // new configurations we do not have a zip name yet.

	        	try
	         {
               if (zip != null && zip.getName() != null)
               {
                  if (!zip.isOpen()) zip.open() ;
                  ze = zip.getEntry(ze.getPath()) ;
              		if (ze == null) { zip.close() ; return ; }
               }
	         }
	         catch (IOException e) { return ; }

	         // Update the configuration memory data and apply the changes.

				config.setMemoryFile(out.toByteArray(),ze) ;
            if (text != null && text.isUpdated())
            	if (ze != null) ze.setUpdated(true) ;

         	// Reload the memory copy of the configuration.

				toFront() ;
				reload = true ;
				new Thread(this).start() ;
            return ;
         }

         // A configuration object does not exist.  We should close our
         // current zip file.

         if (zip == null) return ;
         try { zip.close() ; }
         catch (IOException e) { return ; }

         // Save the text contents if required.  When the save is complete
         // the FileWriter callback will reload the configuration file.

         if (text.isUpdated())
         {
		      FileSave fs = new FileSave(this,text) ;
				fs.save() ;
            return ;
         }

         // We can do the reload now as the file has been saved.  Open our
         // zip file in preparation for the reload.

        	try
         {
           	zip.open() ;
            ze = zip.getEntry(ze.getPath()) ;
            if (ze == null) { zip.close() ; return ; }
         }
         catch (IOException e) { return ; }
			toFront() ;
			reload = false ;
			new Thread(this).start() ;
		}



		// An update request from the file writer window has occured.
		// We have finished saving our configuration file.

		if ("FileWriter Callback".equals(evt.getActionCommand()))
		{
        	try
         {
           	zip.open() ;
            ze = zip.getEntry(ze.getPath()) ;
            if (ze == null) { zip.close() ; return ; }
         }
         catch (IOException e) { return ; }
			toFront() ;
			reload = false ;
			new Thread(this).start() ;
      }
	}



	// Methods to update the frame component display fields.
	// -----------------------------------------------------

	void showStatus(String s)
	{ 
      Status.setText(s) ; 
      notifyWebSearch(s) ;
   }

	void showFile(String s)
	{
		FileName.setText(s) ;
		if (OptionsDialog.getDebugLoad()) System.out.println("Load: " + s) ;
 	}

	void showText(String s)
	{
      appendText(s + "\n") ;
		if (OptionsDialog.getDebugLoad()) System.out.println("Load: " + s) ;
	}

	void showError(String s) { showError(s,null) ; }
	void showError(String s, String highlite)
	{
		errors = errors + 1 ;
      if (errors == 101) showText("More than 100 errors, errors are suppressed.") ;
      if (errors >= 101) return ;
      StyledDocument doc = TextWindow.getStyledDocument() ;
      Position pos = doc.getEndPosition() ;
      if (errorpos < 0) errorpos = pos.getOffset() - 1 ;
      
      // Maintain this error message in our error list. Any message that begins
      // with the text [Line nnn] is a configuration syntax error and can be
      // highighted in the text editor to facilitate edit changes.
      
      if (errormsgs == null) errormsgs = new Vector() ;
      errormsgs.addElement(s) ;
      
      // Highlight the specified text in the message.
      
      if (highlite == null)
         appendText(s + "\n",showset) ;
      else
      {
         int i = findHighlite(s,highlite) ;
         if (i >= 0)
         {
            String s1 = s.substring(0,i) ;
            String s2 = s.substring(i+highlite.length()) ;
            appendText(s1,showset) ;
            appendText(highlite,errorset) ;
            appendText(s2 + "\n",showset) ;
         }
         else
            appendText(s + "\n",showset) ;
      }
		System.out.println("Load: " + s) ;
	}

	void showWarning(String s) { showWarning(s,null) ; }
	void showWarning(String s, String highlite)
	{
      warnings = warnings + 1 ;
      if (warnings == 51) showText("More than 50 warnings, warnings are suppressed.") ;
      if (warnings >= 51) return ;
      StyledDocument doc = TextWindow.getStyledDocument() ;
      Position pos = doc.getEndPosition() ;
      if (errorpos < 0 && OptionsDialog.getStrictSyntax()) 
         errorpos = pos.getOffset() - 1 ;
      
      // Maintain this error message in our error list. Any message that begins
      // with the text [Line nnn] is a configuration syntax error and can be
      // highighted in the text editor to facilitate edit changes.
      
      if (errormsgs == null) errormsgs = new Vector() ;
      errormsgs.addElement(s) ;
      
      // Highlight the specified text in the message.
      
      if (highlite == null)
         appendText(s + "\n",warnset) ;
      else
      {
         int i = findHighlite(s,highlite) ;
         if (i >= 0)
         {
            String s1 = s.substring(0,i) ;
            String s2 = s.substring(i+highlite.length()) ;
            appendText(s1,warnset) ;
            appendText(highlite,errorset) ;
            appendText(s2 + "\n",warnset) ;
         }
         else
            appendText(s + "\n",warnset) ;
      }
		System.out.println("Load: " + s) ;
	}
   
   // Find a highlight in a string. This method assumes numbers
   // are parameters and searches for delimited numeric values.
   
   private int findHighlite(String s, String h)
   {
      if (s == null || h == null) return -1 ;
      try { Integer.parseInt(h) ; }
      catch (NumberFormatException e)
      { return s.indexOf(h) ; }
      s = s.replace(' ', ',') ;
      int n = s.indexOf(","+h+",") ;
      if (n > 0) return n+1 ;
      n = s.indexOf(","+h+")") ;
      if (n > 0) return n+1 ;
      n = s.indexOf("("+h+",") ;
      if (n > 0) return n+1 ;
      n = s.indexOf("("+h+")") ;
      if (n > 0) return n+1 ;
      return s.indexOf(n) ;
  }
   
   // Method to append text to a styled document.  This allows us to set 
   // text attributes in a TextPane.
   
   private void appendText(String s) { appendText(s,null) ; }
   private void appendText(String s, AttributeSet a)
   {
      try
      {
         if (a == null) a = normalset ;
         StyledDocument doc = TextWindow.getStyledDocument() ;
         Position pos = doc.getEndPosition() ;
         int offset = pos.getOffset() - 1 ;
         if (offset < 0) offset = 0 ;
         doc.insertString(offset,s,a) ;
      }
      catch (BadLocationException e) { }
   }

	// Method to intialize the progress bar display.

	void initProgress(int max)
	{
		progress = 0 ;
		Progress.setValue(0) ;
		Progress.setMaximum(max) ;
      notifyWebSearch("Progress: 0%") ;
	}

	// Method to update the progress bar display.

	void updateProgress(int x)
	{
		progress += x ;
		Progress.setValue(progress) ;
      int max = Progress.getMaximum() ;
      int percent = (int) ((x*100.0)/max) ;
      notifyWebSearch("Progress: " + percent + "%") ;
	}

   // Method to return our new configuration.

   Configuration getNewConfiguration() { return newconfig ; }
   void setNewConfiguration(Configuration c) { newconfig = c ; }
   
   // Method to return our text object.
   
   JTextPane getLoadText() { return TextWindow ; }

   
	// Method to close this frame.

	public void close()
	{
      super.close() ;
		flush() ;
		dispose() ;
	}

   // We release references to some of our critical objects.

	private void flush()
   {
      me = null ;
      ze = null ;
      zip = null ;
   	config = null ;
      setVisible(false) ;
      KissObject.setLoader(null) ;
      CANCEL.removeActionListener(this) ;
      EDIT.removeActionListener(this) ;
      PLAY.removeActionListener(this) ;
		removeWindowListener(this) ;
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   // Notify WebSearch.  This sends a load progress message to any batch
   // initialization frame.
   
   private void notifyWebSearch(String s)
   {
      if (s == null) return ;
      KissFrame f = Kisekae.getBatchFrame() ;
      if (f == null) return ;
      if (f instanceof WebSearch.WebSearchFrame)
         ((WebSearch.WebSearchFrame) f).showStatus(s) ;
   }
}
