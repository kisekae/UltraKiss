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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.SwingUtilities ;
import java.net.* ;
import java.io.* ;

final class JarLoader extends UrlLoader
{

	// Constructor to load a JAR in a secure environment.

	public JarLoader(KissFrame frame, String url)
	{
		super(frame,url) ;
	}


	// The loader code.  This code runs as a separate thread.  It will
	// initialize a new Configuration object, load all the necessary
	// files, and finally initialize the main frame window to display
	// the panel frame.  This thread runs at low priority.

	public void run()
	{
		if (activeloader != null) activeloader.stopload() ;
		activeloader = this ;
		LOAD.setEnabled(false);
		thread = Thread.currentThread() ;
		thread.setName("JarLoader-" + loadcount++) ;
		thread.setPriority(Thread.MIN_PRIORITY) ;
		time = System.currentTimeMillis() ;
		threadname = thread.getName() ;
		if (OptionsDialog.getDebugControl())
			PrintLn.println("JAR loader " + threadname + " active.") ;

		// Initialization.  We are given a KiSS configuration file name
		// and we construct the window panel environment to properly
		// display this data set.  This method is called when we need to
		// initialize for a new data set.

		try
		{
			int b = 0 ;
         String s = Kisekae.getCaptions().getString("JARLoadingText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + urlname + s.substring(j1+1) ;
         setTitle(s) ;

			// Get the connection to the JAR.

         openurl = Kisekae.getResource(urlname) ;
			if (openurl == null) throw new MalformedURLException("JAR resource not found: " + urlname) ;
			PrintLn.println("Open JAR " + openurl.toExternalForm()) ;

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

			// Set up the window.  If we are running in the sandbox we
			// cannot create a file.  Load to memory.

			try
			{
				f = File.createTempFile("UltraKiss-"+file,extension) ;
				pathname = f.getPath() ;
				if (OptionsDialog.getDebugLoad())
					PrintLn.println("Open JAR temp file " + pathname) ;
			}
			catch (SecurityException e)
			{
				pathname = null ;
				if (OptionsDialog.getDebugLoad())
					PrintLn.println("Open JAR memory file") ;
			}

			// Open the URL session.

			bytes = 0 ;
         showStatus(Kisekae.getCaptions().getString("OpenConnectionStatus")) ;
			if (icon != null) ErrorMsg.setIcon(icon);
			URLConnection c = openurl.openConnection() ;
         // HTTP Response code 403 for URL
//         c.addRequestProperty("User-Agent", "UltraKiss");

			// Open the stream and read the data.

			ErrorMsg.setIcon(null);
			int size = c.getContentLength() ;
			initProgress(size) ;
			s = (size  / 1024) + "K" ;
         String s1 = Kisekae.getCaptions().getString("TransferText") ;
         i1 = s1.indexOf('[') ;
         j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
         showMsg(s1) ;
			InputStream is = c.getInputStream();
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
			while (!stop && (b = is.read()) >= 0)
			{
				bytes++ ;
				os.write(b) ;
				updateProgress(1) ;
			}

			// Close the connection.

         showStatus(Kisekae.getCaptions().getString("CloseConnectionStatus")) ;
			is.close() ;
			os.close() ;

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
				PrintLn.println("Open JAR data transfer bytes " + bytes) ;

		}

		catch (OutOfMemoryError e)
		{
			fatal = true ;
			pathname = null ;
			memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("LowMemoryFault")) ;
			PrintLn.println("JarLoader: Out of memory.") ;
		}

		catch (MalformedURLException e)
		{
			fatal = true ;
			pathname = null ;
			memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("InvalidURLError") + " " + urlname) ;
		}

		// Catch security exceptions.

		catch (SecurityException e)
		{
			fatal = true ;
			pathname = null ;
			memfile = null ;
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
         showMsg(Kisekae.getCaptions().getString("SecurityException")) ;
         PrintLn.println("KiSS file open exception, " + e.getMessage()) ;
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
         showStatus(Kisekae.getCaptions().getString("LoadTerminatedStatus")) ;
			showMsg(e.toString()) ;
			if (!stop)
			{
				PrintLn.println("JarLoader: " + threadname + " exception " + e) ;
				e.printStackTrace() ;
			}
		}

		// Show the load status to the user.

		if (!fatal)
		{
			time = System.currentTimeMillis() - time ;
			int n = bytes / 1024 ;
         String s = Kisekae.getCaptions().getString("LoadBytesText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + n + s.substring(j1+1) ;
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
		// currently has active focus.

		if (!stop) activeloader = null ;
		if (fatal && !Kisekae.isBatch()) return ;
		if (active || Kisekae.isBatch())
		{
         
         // Run the callback on the EDT thread.
         
         Runnable runner = new Runnable()
         { public void run() { callback.doClick() ; close() ; } } ;
         javax.swing.SwingUtilities.invokeLater(runner) ;        
			return ;
		}

		// Signal completion to the user.  Bring load window into focus.

		LOAD.setEnabled(true) ;
		validate() ;
		requestFocus() ;
	}
}
