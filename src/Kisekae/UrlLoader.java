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


import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.SwingUtilities ;
import java.net.* ;
import java.io.* ;
import java.util.Collections;
import java.util.Comparator;


class UrlLoader extends KissFrame
	implements WindowListener, ActionListener, Runnable
{
	private static String iconname = "Images/connecting.gif" ;
   static MemFile memfile = null ;        // The memory file contents
	static UrlLoader activeloader = null ;	// The active loader
   static int loadcount = 0 ;             // Count of invokations

	protected KissFrame parent = null ;		// The parent frame
	protected Thread thread = null ;			// The loader thread
   protected String urlname = null ;		// The name of the url
	protected URL openurl = null ;   		// The URL to open
	protected String pathname = null ; 		// The temp file pathname
	protected String extension = null ; 	// The temp file extension
   protected String connid = null ;       // The session userid
   protected String threadname = "" ;     // The load thread name
   protected String setname = null ;      // The original set name
   protected String cnfname = null ;      // The original CNF name
   protected String action = null ;       // The loader action state


	// Status indicators

	protected boolean active = true ;		// True if frame has focus
	protected boolean reload = false ;		// True if reload configuration
   protected boolean interrupted = false ; // True if load is interrupted
   protected boolean fatal = false ;		// True if fatal error
   protected boolean stop = false ;			// True if load must stop
   protected int bytes = 0 ;					// Number of bytes loaded
	protected int progress = 0 ;				// Progress bar value
	protected long time = 0 ;					// Duration of load

	// Stream references

	protected InputStream is = null ;			// The file input stream
	protected OutputStream os = null ;			// The file output stream

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
	protected JLabel ErrorMsg = new JLabel();
	private JProgressBar Progress = new JProgressBar();
	private JButton CANCEL = new JButton();
	protected JButton LOAD = new JButton();
   protected ImageIcon icon = null ;

	// Our update callback button that other components can attach
	// listeners to.

	protected CallbackButton callback = new CallbackButton(this,"UrlLoader Callback") ;


	// Constructor to load a URL.

	public UrlLoader(KissFrame frame, String url)
	{
		parent = frame ;

      if (url != null)
      {
         int n = url.indexOf(',') ;
         if (n > 0) 
         {
            cnfname = url.substring(n+1).trim() ;
            cnfname = Variable.getStringLiteralValue(cnfname) ;
            url = url.substring(0,n) ;
         }
         url = Variable.getStringLiteralValue(url) ;      
      }
   
      urlname = url.replaceFirst("[\\#\\?].*$","") ;  // no query or ref
      setIconImage(parent.getIconImage());

		// Set the frame characteristics.

		try { jbInit(); }
		catch(Exception e)
		{
         System.out.println("UrlLoader: jbInit constructor " + e.toString()) ;
         e.printStackTrace(); 
      }

      // Get the connection icon image.

      try
      {
 		 	URL iconImageURL = Kisekae.getResource(iconname) ;
         if (iconImageURL != null)
	 	     	icon = new ImageIcon(iconImageURL) ;
      }
		catch (Exception e) { }

		// Center the frame in the screen space.

 		setSize(550,200) ;
 		center(this,parent) ;

		// Setup to catch window events in this frame.

		addWindowListener(this) ;
      CANCEL.addActionListener(this) ;
      LOAD.addActionListener(this) ;
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
      ErrorMsg.setPreferredSize(new Dimension(500, 20));
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		LOAD.setText(Kisekae.getCaptions().getString("LoadMessage"));
		Progress.setPreferredSize(new Dimension(300, 16));

		jPanel1.setLayout(borderLayout2);
 		jPanel1a.setLayout(borderLayout3);
		jPanel1a.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 		jPanel2.setLayout(borderLayout4);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		this.getContentPane().setLayout(borderLayout1);
		this.getContentPane().add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel1a, BorderLayout.NORTH);
		jPanel1a.add(Status, BorderLayout.WEST);
		jPanel1a.add(FileName, BorderLayout.EAST);
		jPanel1a.add(Progress, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
      jPanel2.add(ErrorMsg, BorderLayout.CENTER) ;
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(LOAD, null);
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
      boolean incache = false ;
		if (activeloader != null) activeloader.stopload() ;
      activeloader = this ;
		LOAD.setEnabled(false);
		thread = Thread.currentThread() ;
		thread.setName("UrlLoader-" + loadcount++) ;
		time = System.currentTimeMillis() ;
      threadname = thread.getName() ;
		if (OptionsDialog.getDebugControl())
			System.out.println("URL loader " + threadname + " active.") ;
		thread.setPriority(Thread.MIN_PRIORITY) ;
      try { thread.sleep(500) ; } catch (Exception e) { }
		toFront() ;
      requestFocus();

		// Initialization.  We are given a KiSS configuration file name
		// and we construct the window panel environment to properly
		// display this data set.  This method is called when we need to
		// initialize for a new data set.

      try
      {
         int b = 0 ;
         String s = Kisekae.getCaptions().getString("URLLoadingText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + urlname + s.substring(j1+1) ;
         setTitle(s) ;
   		openurl = new URL(urlname) ;
         System.out.println("Download file " + openurl.toExternalForm()) ;

         // Establish a temporary file of the correct type.

         String file = openurl.getFile() ;
         File f = new File(file) ;
         file = f.getName() ;
			int n = (file == null) ? -1 : file.lastIndexOf('.') ;
			extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
         if (n > 0) file = file.substring(0,n) ;
         setname = file + extension ;
         s = Kisekae.getCaptions().getString("FileNameText") ;
         i1 = s.indexOf('[') ;
         j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + setname + s.substring(j1+1) ;
         showFile(s) ;
      
         // Determine if the file exists in our download cache directory.
         // Check based on filename and last date modified.  Natural sort
         // is ascending so search backwards to get latest date.

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

			// Set up the window.  If we are running in the sandbox we
         // cannot create a file.  Load to memory.  Otherwise cache
         // the file in the cache directory.

         if (!incache)
         {
            try
            {  
               f = File.createTempFile("UltraKiss-"+file,extension,directory) ;
               if (!OptionsDialog.getCacheInclude()) f.deleteOnExit() ;
               pathname = f.getPath() ;
               System.out.println("Create URL file " + pathname + " in cache.") ;
            }
            catch (Exception e)
            {
               pathname = null ;
               System.out.println("Create URL memory file, cache exception " + e.getMessage()) ;
            }
         }
            

         // Open the URL session.

         bytes = 0 ;
         InputStream is = null ;
         OutputStream os = null ;
         showStatus(Kisekae.getCaptions().getString("OpenConnectionStatus")) ;
         if (icon != null) ErrorMsg.setIcon(icon);
         URLConnection c = openurl.openConnection() ;
         // HTTP Response code 403 for URL
//         c.addRequestProperty("User-Agent", "UltraKiss");

         // Authorize the session if necessary.

         if (connid != null)
            c.setRequestProperty("Authorization", "Basic " + connid);
         ErrorMsg.setIcon(null);
         int size = c.getContentLength() ;
         initProgress(size) ;
      
         // Open the stream and read the data.  Read stream if not cached or 
         // the content size differs from cached file size.
      
         if (!incache || size != f.length())
         {
            s = (size  / 1024) + "K" ;
            String s1 = Kisekae.getCaptions().getString("TransferText") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
            showMsg(s1) ;
            int maxdownload = Kisekae.getMaxDownload() ;
            if (Kisekae.isBatch() && size > maxdownload*1024)
               throw new KissException("Batch URL size (" + size + ") exceeds " + maxdownload + " KB") ;
            is = c.getInputStream();
            if (pathname != null)
               os = new FileOutputStream(f) ;
            else
               os = new ByteArrayOutputStream(size) ;

            // Read the data.

            s1 = Kisekae.getCaptions().getString("ReadDataStatus") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + s + s1.substring(j1) ;
            showStatus(s1) ;

            byte[] buffer = new byte[2048];
            while (!stop && (n = is.read(buffer)) >= 0)
            {
            	bytes = bytes + n ;
               os.write(buffer,0,n) ;
               updateProgress(n) ;
            }
         }

         // Close the connection.

         updateProgress((incache) ? size : 0) ;
         showStatus(Kisekae.getCaptions().getString("CloseConnectionStatus")) ;
         if (is != null) is.close() ;
         if (os != null) os.close() ;

			// Finished.

         s = Kisekae.getCaptions().getString("FinishedText") ;
         i1 = s.indexOf('[') ;
         j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + file + s.substring(j1+1) ;
         setTitle(s) ;
         if (stop) throw new Exception("Load " + openurl.toExternalForm() + " stopped") ;
         showStatus(Kisekae.getCaptions().getString("TransferCompleteStatus")) ;
         if (OptionsDialog.getDebugLoad())
	         System.out.println("Open URL data transfer bytes " + bytes) ;
      }

		catch (OutOfMemoryError e)
		{
      	fatal = true ;
         pathname = null ;
         memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("LowMemoryFault")) ;
			System.out.println("UrlLoader: Out of memory.") ;
		}

      catch (MalformedURLException e)
      {
      	fatal = true ;
         pathname = null ;
         memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("InvalidURL") + " " + urlname) ;
      }

      // Catch security exceptions.

      catch (SecurityException e)
      {
      	fatal = true ;
         pathname = null ;
         memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("SecurityException")) ;
         System.out.println("KiSS file open exception, " + e.getMessage()) ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

		catch (Throwable e)
		{
      	fatal = true ;
         pathname = null ;
         memfile = null ;
         String msg = Kisekae.getCaptions().getString("LoadTerminatedStatus") ;
         if (e instanceof FileNotFoundException)
            msg += " " + Kisekae.getCaptions().getString("FileNotFound") ;
  			showStatus(msg) ;
         showMsg(e.toString()) ;
         if (!stop) 
         {
            System.out.println("UrlLoader: " + threadname + " exception " + e) ;
            if (!(e instanceof FileNotFoundException))
            {
               System.out.println("UrlLoader: " + threadname + " URL " + urlname) ;
               if (!(e instanceof KissException)) e.printStackTrace() ;
            }
         }
		}

		// Show the load status to the user.

      try
      {
   		if (!fatal)
         {
      		time = System.currentTimeMillis() - time ;
      		int n = bytes / 1024 ;
            int i1 = 0 ;
            int j1 = 0 ;
            String s = "" ;
            if (incache)
            {
               s = Kisekae.getCaptions().getString("LoadBytesCacheText") ; 
               i1 = s.indexOf('[') ;
               j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1) + urlname + s.substring(j1+1) ;
            }
            else 
            {
               s = Kisekae.getCaptions().getString("LoadBytesText") ;
               i1 = s.indexOf('[') ;
               j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                 s = s.substring(0,i1) + n + s.substring(j1+1) ;               
            }
            showMsg(s) ;
            try {thread.sleep(1000) ; }
            catch (InterruptedException e) { }

            // Create a memory byte array if we are in a secure environment.

            if (os instanceof ByteArrayOutputStream)
            {
               ByteArrayOutputStream bos = (ByteArrayOutputStream) os ;
               memfile = new MemFile(openurl.getFile(),bos.toByteArray()) ;
               os = null ;
            }
         }

   		// Continue with the main panel frame initialization.  This
   		// is automatic if the load was error free and this frame
   		// currently has active focus.  Run the callback on the AWT
         // thread for thread safety.

         if (!stop) activeloader = null ;
         if (fatal && !Kisekae.isBatch()) return ;
   		if (active || Kisekae.isBatch())
   		{
            Runnable awt = new Runnable()
            { public void run() { callback.doClick() ; close() ; } } ;
            SwingUtilities.invokeLater(awt) ;
            return ;
      	}

   		// Signal completion to the user.  Bring load window into focus.

   		LOAD.setEnabled(true) ;
         validate() ;
   		requestFocus() ;
      }
      
      catch (Exception e)
      {
         fatal = true ;
         System.out.println("UrlLoader: " + threadname + " exception " + e) ;
         System.out.println("UrlLoader: " + threadname + " URL " + urlname) ;
         e.printStackTrace() ;
         return ;
      }
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
      String s = (openurl != null) ? openurl.toExternalForm() + " " : "" ;
   	System.out.println("URL Load " + s + "cancelled ...") ;
		if (activeloader == this) activeloader.stopload() ;
      if (Kisekae.isBatch()) callback.doClick();
      pathname = null ;
      memfile = null ;
		close() ;
	}


	// Action Events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		// A Cancel request stops the loader thread.

		if (source == CANCEL)
		{
	      pathname = null ;
         memfile = null ;
         String s = (openurl != null) ? openurl.toExternalForm() + " " : "" ;
      	System.out.println("URL Load " + s + "cancelled ...") ;
   		if (activeloader == this) activeloader.stopload() ;
         if (Kisekae.isBatch()) callback.doClick();
			close() ;
         return ;
		}

		// A Load request signals any callback requests.

		if (source == LOAD)
		{
      	callback.doClick() ;
			close() ;
         return ;
		}
	}



	// Methods to update the frame component display fields.
	// -----------------------------------------------------

	void showStatus(String s)
	{ 
      if (s == null) return ;
      Status.setText(s) ;
      notifyWebSearch(s) ;
   }

	void showMsg(String s)
	{ 
      if (s == null) return ;
      ErrorMsg.setText(s) ; 
      notifyWebSearch(s) ;
   }

	void showFile(String s)
	{
      if (s == null) return ;
      s = s.replaceFirst("[\\#\\?].*$","") ;  // remove query and ref from url
		FileName.setText(s) ;
		if (OptionsDialog.getDebugLoad()) System.out.println("URL Load: " + s) ;
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

   // Method to return our new temp file name.

   String getTempFileName()
   {
      if (fatal) return null ;
      if (pathname != null) return pathname ;
      return urlname ;
   }

   // Method to return our URL name.

   String getUrlName() { return urlname ; }

   // Method to return our URL.

   URL getURL() { return openurl ; }

   // Method to return our original set name.

   String getSetName() { return setname ; }
   
   // Method to return our optional original cnf name.

   String getCnfName() { return cnfname ; }

   // Method to set our authorized user and password.

   void setConnectionID(String s) { connid = s ; }

   // Method to set an interrupt flag.

   void stopload() { stop = true ; }

   // Method to set an action state for callback review.

   void setAction(String s) { action = s ; }

   // Method to retrieve the action state.

   String getAction() { return action ; }

   // Method to return our memory file array.

   static MemFile getMemoryFile() { return memfile ; }
   static void setMemoryFile(MemFile m) { memfile = m ; }
   static void closeMemoryFile() { memfile = null ; }

	// Method to close this frame.

	public void close()
	{
		super.close() ;
      try { is.close() ; }
      catch (Exception e) {}
      try { os.close() ; }
      catch (Exception e) {}
      callback.removeActionListener(null) ;
      flush() ;
		dispose() ;
	}

   // We release references to some of our critical objects.

	private void flush()
   {
      thread = null ;
      setVisible(false) ;
      CANCEL.removeActionListener(this) ;
      LOAD.removeActionListener(this) ;
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