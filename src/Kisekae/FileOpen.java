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
* FileOpen class
*
* Purpose:
*
* This class manages the file open dialog to provide the user with
* the ability to select the required element from a compressed data
* file or an uncompressed file directory.
*
* A FileOpen object manages the connection to a data set archive file.
*
*/


import java.awt.* ;
import java.util.Enumeration ;
import java.util.StringTokenizer ;
import java.util.Vector ;
import java.util.ResourceBundle;
import java.util.Locale;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.* ;
import javax.swing.* ;
import javax.swing.filechooser.FileFilter ;
import java.text.MessageFormat;


final public class FileOpen
{
	// Class attributes

	private static String dirname = null ;		// Last accessed directory
   private static URL zipFileURL = null ;		// URL of open file

   // I18N attributes

   private ResourceBundle captions = Kisekae.getCaptions() ;

	// File attributes

	private JFrame parent = null ;				// Parent frame
	private Vector entries = null ;				// List of .cnf files
	private String filename = null ;				// Entered file name
	private String elementname = null ;			// Selected element name
	private MemFile cnfmemfile = null ;       // Selected CNF memory file
	private String pathname = null ;				// Full path name
	private String extension = null ;			// File extension
	private String mode = null ;					// File creation mode (rw)
	private String title = null ;					// Selection dialog title
	private Object [] ext = null ;				// Selection extensions
   private File [] files = null ;            // Multiple files selected
   private File fileobject = null ;          // Selected file
	private ArchiveFile zip = null ;				// ZIP file object
	private ArchiveEntry ze = null ;			 	// ZIP file entry
	private ArchiveEntry selected = null ;		// ZIP file entry selected
   private URL sourceURL = null ;		      // URL of original source
   private int entrycount = 0 ;              // Number of entries
   private boolean multiple = false ;        // Multiple selection allowed
   private boolean dirs = false ;            // Directories only

   // File Filters

   private String filefilter = null ;
   private FileFilter allfiles = null ;
   private FileFilter archives = null ;
   private FileFilter kissarchives = null ;
   private FileFilter palettefiles = null ;
   private FileFilter imagefiles = null ;
   private FileFilter mediafiles = null ;
   private FileFilter audiofiles = null ;
   private FileFilter videofiles = null ;
   private FileFilter textfiles = null ;


	// Constructor

	public FileOpen(JFrame frame)
	{ this(frame,null,"r") ; }

	public FileOpen(JFrame frame, String title)
	{ this(frame,null,"r") ; this.title = title ; }

	public FileOpen(JFrame frame, String title, Object [] ext)
	{ this(frame,null,"r") ; this.title = title ; this.ext = ext ; }

   public FileOpen(JFrame frame, String path, String rw)
	{
      mode = rw ;
		parent = frame ;
		pathname = path ;
		title = captions.getString("ConfigurationListTitle") ;
      archives = new SimpleFilter(captions.getString("ArchiveFilter"),ArchiveFile.getArchiveExt());
      kissarchives = new SimpleFilter(captions.getString("KissArchiveFilter"),ArchiveFile.getKissExt());
      palettefiles = new SimpleFilter(captions.getString("PaletteFilter"),ArchiveFile.getPaletteExt());
      imagefiles = new SimpleFilter(captions.getString("ImageFilter"),ArchiveFile.getImageExt());
      mediafiles = new SimpleFilter(captions.getString("MediaFilter"),ArchiveFile.getMediaListExt());
      audiofiles = new SimpleFilter(captions.getString("MediaFilter"),ArchiveFile.getAudioExt());
      videofiles = new SimpleFilter(captions.getString("MediaFilter"),ArchiveFile.getVideoExt());
      textfiles = new SimpleFilter(captions.getString("TextFilter"),ArchiveFile.getTextExt());
      ext = new String [] { ".CNF" } ;
	}


	// Object state reference methods
	// ------------------------------

	// Return the zip file object reference.

	ArchiveFile getZipFile() { return zip ; }

	// Return the selected zip file entry.

	ArchiveEntry getZipEntry() { return ze ; }

	// Return the specified zip file entry for multiple selections.

	ArchiveEntry getZipEntry(int n)
   {
      if (n == 0) return getZipEntry() ;
      if (zip == null) return null ;
      if (files == null) return null ;
      if (n < 0 || n >= files.length) return null ;
      File f = files[n] ;
		return zip.getEntry(f.getPath()) ;
   }

	// Return the last accessed directory

	public static String getDirectory() { return dirname ; }

	// Return the file name for the container file.

	public String getFile() { return filename ; }

	// Return the file object for the container file.

	public File getFileObject() { return fileobject ; }

	// Return the selected file element from the container file.

	public String getElement() { return elementname ; }

	// Return the selected file element extension.

	public String getExtension() { return extension ; }

	// Return the full path name for the container file.

	public String getPath() { return pathname ; }

	// Return the URL for the zip file.

	URL getZipFileURL() { return zipFileURL ; }

	// Return the URL for the original file source.

	URL getSourceURL() { return sourceURL ; }

	// Return the number of available entries.

	int getEntryCount() { return entrycount ; }

   // Function to set a new pathname for this fileopen object.

	public void setPath(String path) { pathname = path ; close() ; }

	// Function to set the URL for the original file source.

	void setSourceURL(URL u) { sourceURL = u ; }

	// Function to set the archive file for a new source entry.

	void setZipFile(ArchiveFile af) { zip = af ; }

	// Function to set the archive entry for a new source entry.

	void setZipEntry(ArchiveEntry ae) 
   { 
      ze = ae ; 
      filename = (ze != null) ? ze.getName() : null ;
      pathname = (ze != null) ? ze.getPath() : null ;
      zip = (ze != null) ? ze.getZipFile() : null ;
   }


	// Object utility methods
	// ----------------------

	// Show the file open dialog, which is modal.

	public void show() { show(title) ; }
   public void show(String dialogtitle) { show(dialogtitle, null) ; }
   public void show(String dialogtitle, String approve) { show(dialogtitle, approve, false) ; }
   public void show(String dialogtitle, String approve, boolean dirs)
	{
		Enumeration enum1 = null ;
		Frame dialogframe = (parent != null) ? parent : new Frame() ;
		URL codebase = Kisekae.getBase() ;

      // Use a platform dependent dialog if necessary.  These do not allow
      // for filename filters or multiple selections.

      FileDialog fd = null ;
      JFileChooser jfd = null ;
      if (OptionsDialog.getSystemLF())
		   fd = new FileDialog(dialogframe,dialogtitle) ;
      else
      {
		   jfd = new JFileChooser() ;
         jfd.setLocale(Kisekae.getCurrentLocale()) ;
         jfd.setDialogTitle(dialogtitle) ;
         jfd.setApproveButtonText(approve);
         allfiles = jfd.getAcceptAllFileFilter() ;
         if (multiple) jfd.setMultiSelectionEnabled(true) ;
         if (dirs) jfd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
         
         // Define the file filters. 
         
         if (parent instanceof MainFrame)
            jfd.addChoosableFileFilter(kissarchives) ;
         jfd.addChoosableFileFilter(archives) ;
         if (!(parent instanceof ZipManager))
         {
            jfd.addChoosableFileFilter(palettefiles) ;
            jfd.addChoosableFileFilter(imagefiles) ;
            jfd.addChoosableFileFilter(mediafiles) ;
            jfd.addChoosableFileFilter(textfiles) ;
         }
         
         // Set the file filter.  
         
         if (filefilter == null) jfd.setFileFilter(allfiles) ;
         else if ("archives".equals(filefilter)) jfd.setFileFilter(archives) ;
         else if ("kissarchives".equals(filefilter)) jfd.setFileFilter(kissarchives) ;
         else if ("palettefiles".equals(filefilter)) jfd.setFileFilter(palettefiles) ;
         else if ("imagefiles".equals(filefilter)) jfd.setFileFilter(imagefiles) ;
         else if ("mediafiles".equals(filefilter)) jfd.setFileFilter(mediafiles) ;
         else if ("audiofiles".equals(filefilter)) jfd.setFileFilter(audiofiles) ;
         else if ("videofiles".equals(filefilter)) jfd.setFileFilter(videofiles) ;
         else if ("textfiles".equals(filefilter)) jfd.setFileFilter(textfiles) ;
      }

      try
      {
         if (codebase == null) throw new SecurityException("unknown codebase") ;
			String directory = codebase.getFile() ;
			if (directory != null && fd != null) fd.setDirectory(directory) ;
			if (directory != null && jfd != null) jfd.setCurrentDirectory(new File(directory)) ;
			if (dirname != null && fd != null) fd.setDirectory(dirname) ;
			if (dirname != null && jfd != null) jfd.setCurrentDirectory(new File(dirname)) ;

			// Center the dialog in the screen space.  The dialog is always set
			// to location (0,0) of the parent frame.  We center the dialog only
			// if a parent frame was not specified.

         int fdoption = JFileChooser.CANCEL_OPTION ;
			Dimension ds = new Dimension(500,400) ;
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
			int x = (ds.width < d.width) ? (d.width - ds.width) / 2 : 0 ;
			int y = (ds.height < d.height) ? (d.height - ds.height) / 2 : 0 ;
			if (parent == null) dialogframe.setLocation(x, y) ;
			if (fd != null) fd.show() ;
			if (jfd != null) fdoption = jfd.showOpenDialog(dialogframe) ;

			// See if we have a file to open.

			if (fd != null && fd.getFile() == null)
	      {
	      	fd.dispose() ;
	         return ;
	      }
			if ((jfd != null && jfd.getSelectedFile() == null) ||
             (jfd != null && fdoption != JFileChooser.APPROVE_OPTION))
	      {
	      	jfd.setVisible(false) ;
	         return ;
	      }

	      // Close any previously open file.

			close() ;
	      zip = null ;
			ze = null ;

			// Get the selected file name and directory.

         File f = null ;
         if (fd != null)
         {
   			dirname = fd.getDirectory() ;
   			filename = fd.getFile() ;
   			f = new File(dirname,filename) ;
         }
         if (jfd != null)
         {
            f = jfd.getSelectedFile() ;
            if (multiple) files = jfd.getSelectedFiles() ;
            dirname = f.getParent() ;
            filename = f.getName() ;
         }

         // Identify the file characteristics.  

         pathname = f.getPath() ;
			int i = filename.lastIndexOf(".") ;
			extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;
			if (fd != null) fd.dispose() ;
         if (jfd != null) jfd.setVisible(false) ;
         if (!f.isFile()) return ;
         if ("archives".equals(filefilter)) return ;
         
         ze = validateFile(pathname,filename,dirname,extension) ;
         fileobject = f ;
     }

      // Catch security exceptions.

      catch (SecurityException e)
      {
         System.out.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(parent,
            captions.getString("SecurityException") + "\n" +
            captions.getString("FileOpenSecurityMessage1"),
         	captions.getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
         if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			close() ;
         return ;
      }

		// Catch file error exceptions.

		catch (Exception e)
		{
         System.out.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(parent, e.toString(),
         	captions.getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
         e.printStackTrace() ;
         if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			close() ;
			return ;
		}
   }

	// Open the file and search for any configuration elements.  This is the 
   // processing after a file is chosen on an Open request or specified on
   // a specific viewer FKiSS request.
         
   public ArchiveEntry validateFile(String pathname, String filename, String dirname, String extension)
   {
		URL codebase = Kisekae.getBase() ;
      ArchiveEntry lzhentry = null ;
      ArchiveEntry lstentry = null ;

      if (this.pathname == null) this.pathname = pathname ;
      if (this.filename == null) this.filename = filename ;
      if (this.extension == null) this.extension = extension ;

      try
      {
			String protocol = codebase.getProtocol() ;
			String host = codebase.getHost() ;
			int port = codebase.getPort() ;
			zipFileURL = new URL(protocol,host,port,pathname) ;
			if (ArchiveFile.isArchive(filename) || ArchiveFile.isConfiguration(filename))
				System.out.println("Open archive " + zipFileURL.toExternalForm()) ;
         else
 				System.out.println("Open " + zipFileURL.toExternalForm()) ;

			// Regretfully, we must treat each file type separately as Java's
			// zip file class does not appear designed for subclassing.

         if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			if (".zip".equals(extension))
         	zip = new PkzFile(this,zipFileURL.getFile()) ;
         else if (".gzip".equals(extension))
         	zip = new PkzFile(this,zipFileURL.getFile()) ;
			else if (".jar".equals(extension))
         	zip = new PkzFile(this,zipFileURL.getFile()) ;
			else if (".lzh".equals(extension))
         	zip = new LhaFile(this,zipFileURL.getFile()) ;
			else if (".cnf".equals(extension))
         	zip = new DirFile(this,dirname) ;
         if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

			// Confirm that we selected a configuration file or archive file.
	      // If not, accept the actual selection.

	      if (zip == null)
	      {
	        	zip = new DirFile(this,dirname) ;
				ze = zip.getEntry(pathname) ;
				elementname = (ze == null) ? filename : ze.getPath() ;
				return ze ;
	      }

			// Put the data set required entries into a list.

         entrycount = 0 ;
			entries = new Vector() ;
         Enumeration enum1 = (zip == null) ? null : zip.entries() ;
			while (enum1 != null && enum1.hasMoreElements())
			{
				Object o = enum1.nextElement() ;
				String name = ((ArchiveEntry) o).getPath() ;
				String s = name.toUpperCase() ;
				int i = s.lastIndexOf('.') ;
				if (i < 0) continue ;
				String ss = s.substring(i) ;

            // Remember the first LZH element in the archive.

            if (".lzh".equalsIgnoreCase(ss))
               if (lzhentry == null) lzhentry = (ArchiveEntry) o ;

            // Remember the first LST element in the archive.

            if (".lst".equalsIgnoreCase(ss))
               if (lstentry == null) lstentry = (ArchiveEntry) o ;

            // Check for the requisite elements.

				for (i = 0 ; i < ext.length ; i++)
            {
					if (ss.equals(ext[i].toString()))
               {
               	entries.addElement(s) ;
                  entrycount++ ;
                  break ;
               }
            }
			}
		}

      // Catch security exceptions.

      catch (SecurityException e)
      {
         System.out.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(parent,
            captions.getString("SecurityException") + "\n" +
            captions.getString("FileOpenSecurityMessage1"),
         	captions.getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
         if (parent != null)
         {
            if (parent instanceof MainFrame)
               ((MainFrame) parent).releaseMouse(true) ;
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         }
			close() ;
         return null ;
      }

		// Catch file error exceptions.

		catch (IOException e)
		{
         System.out.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(parent, e.toString(),
         	captions.getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
         if (parent != null)
         {
            if (parent instanceof MainFrame)
               ((MainFrame) parent).releaseMouse(true) ;
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         }
			close() ;
			return null ;
		}

		// Catch file error exceptions.

		catch (Exception e)
		{
         System.out.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(parent, e.toString(),
         	captions.getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
         e.printStackTrace() ;
         if (parent != null)
         {
            if (parent instanceof MainFrame)
               ((MainFrame) parent).releaseMouse(true) ;
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         }
			close() ;
			return null ;
		}

      // If we failed to find a configuration entry and we had opened an
      // archive with a contained LZH file, then we should unpack the
      // archive and extract the configuration from the LHA file.

   	StringTokenizer st = new StringTokenizer(title," ") ;
      String type = (st.hasMoreElements()) ? (String) st.nextElement() : "suitable" ;
      StringTokenizer st1 = new StringTokenizer(captions.getString("ConfigurationListTitle")," ") ;
      String cnf = (st1.hasMoreElements()) ? (String) st1.nextElement() : "" ;
      if (entries.size() == 0 && cnf.equalsIgnoreCase(type) && lzhentry != null)
      {
         if (unpack(lzhentry))
         {
            showConfig(parent) ;
            return ze ;
         }
      }

		// Check that we actually found a required entry.

		if (entries.size() == 0)
		{
			String s = "File " + filename + " " + type + " element not found." ;
         System.out.println(s) ;
//       s = captions.getString("FileOpenFileOpenMessage1") ;
//       int i1 = s.indexOf('[') ;
//       int j1 = s.indexOf(']') ;
//       if (i1 >= 0 && j1 > i1)
//          s = s.substring(0,i1+1) + filename + "] CNF" + s.substring(j1+1) ;
//			JOptionPane.showMessageDialog(parent, s,
//         	captions.getString("FileOpenException"),
//          JOptionPane.ERROR_MESSAGE) ;
//       close() ;
         ze = null ;
			return ze ;
		}

		// If we selected an element from a directory, use it.

		if (zip instanceof DirFile)
		{
			ze = zip.getEntry(pathname) ;
			elementname = (ze == null) ? filename : ze.getPath() ;
			return ze ;
		}

		// If there was only one requested element in the data set
      // and it is a configuration element, use it.

		if (entries.size() == 1)
		{
			elementname = (String) entries.elementAt(0) ;
         if (ArchiveFile.isConfiguration(elementname))
         {
				if (zip != null) ze = zip.getEntry(elementname) ;
				return ze ;
         }
		}

		// If we are the MediaPlayer and saw a LST entry in the archive, use it.

		if ((parent instanceof MediaFrame) && lstentry != null)
		{
         if (zip != null) ze = lstentry ;
         return ze ;
      }

		// If there is more than one requested element in the data set,
		// show a selection dialog and use whatever is chosen.  If no
      // selection is made close the file.

		ze = showConfig(parent,title,entries) ;
      if (ze == null) close() ;
      return ze ;
	}


	// Method to display a configuration selection dialog.  This method
	// returns the archive entry object associated with the selected file
	// chosen from the list of name entries.  If there is only one entry 
   // it is automatically returned unless the 'single' parameter is true.

	ArchiveEntry showConfig(JFrame parent, String title, Vector entries)
   { return showConfigAllowImport(parent,title,entries,false,false,false) ; }
   
	ArchiveEntry showConfig(JFrame parent, String title, Vector entries, boolean single)
   { return showConfigAllowImport(parent,title,entries,single,false,false) ; }

	ArchiveEntry showConfigAllowImport(JFrame parent, String title, Vector entries, boolean single, boolean allowimport, boolean importonly)
	{
      if (entries == null) return null ;
      if ((entries.size() == 1 && !single) || (Kisekae.isBatch() && entries.size() > 0))
      {
         ze = null ;
         Object entry = entries.elementAt(0) ;
         if (entry instanceof ArchiveEntry)
         {
            ze = (ArchiveEntry) entry ;
            zip = ze.getZipFile() ;
            elementname = ze.getName() ;
         }
         else if (entry instanceof String)
            elementname = (String) entry ;
         else
            elementname = null ;
      }
      else if (entries.size() >= 1 || importonly)
      {
         // Have the user select the file element.

    		ElementDialog cl = new ElementDialog(parent,title,entries) ;
         cl.setAllowImport(allowimport) ;
         cl.setImportOnly(importonly) ;
    		cl.setVisible(true) ;
   		elementname = cl.getSelectedItem() ;
         cnfmemfile = cl.getSelectedFile() ;
         selected = cl.getSelectedEntry() ;
    		cl.dispose() ;
      }

		// Access the selected file element and return the archive entry.
      // If we are working with directory files then our selected element
      // name must be made absolute as it is simply an element name.  If we
      // are working with archive files then the selected element name will
      // include the directories relative to the configuration file.  If we
      // were selecting from archive entries then we are importing from an
      // INCLUDE file and expanding our configuration.

      if (selected != null) return selected ;
      
		if (zip != null && elementname != null)
      {
      	if (zip instanceof DirFile)
         {
         	String directory = zip.getDirectoryName() ;
            if (directory != null)
            {
					File f = new File(directory,elementname) ;
	            elementname = f.getPath() ;
					ze = zip.getEntry(elementname) ;
            }
         }
         else if (ze == null)
	      	ze = zip.getEntry(elementname) ;
			if (ze != null) 
            ze.setZipFile(zip) ;
         else
            close() ;         
         return ze ;
      }
      
      // If we imported a new CNF file we will have a MemFile returned.
      
      if (elementname == null && cnfmemfile != null)
      {
         DirEntry newcnf = new DirEntry(cnfmemfile) ;
         newcnf.setZipFile(zip) ;
         return newcnf ;
      }
		return null ;
	}


	// Method to display a configuration selection dialog.  This method
	// returns the file entry object after constructing a list of the
	// required name entries from the specified set of file extensions.
   
	ArchiveEntry showConfig(JFrame parent, String title, Object [] ext)
   { return showConfig(parent,title,ext,false,null) ; }
   
	ArchiveEntry showConfig(JFrame parent, String title, Object [] ext, String cnf)
   { return showConfig(parent,title,ext,false,cnf) ; }
   
	ArchiveEntry showConfig(JFrame parent, String title, Object [] ext, boolean single)
   { return showConfig(parent,title,ext,single,null) ; }

   ArchiveEntry showConfig(JFrame parent, String title, Object [] ext, boolean single, Object entry)
   { return showConfig(parent,title,ext,single,null,false,false) ; }

	ArchiveEntry showConfig(JFrame parent, String title, Object [] ext, boolean single, Object entry, boolean allowimport, boolean importonly)
	{
      entrycount = 0 ;
      ArchiveEntry lzhentry = null ;
		Vector entries = new Vector() ;
		Enumeration enum1 = null ;
      String name = null ;

		if (zip != null) enum1 = zip.entries() ;

		// Put the required data set entries into a list.  If our entry object
      // is a specific CNF name and it exists then only this single entry is 
      // used. 

		while (enum1 != null && enum1.hasMoreElements())
		{
			Object o = enum1.nextElement() ;
         if (zip instanceof DirFile)
				name = ((ArchiveEntry) o).getName() ;
         else
				name = ((ArchiveEntry) o).getPath() ;
			String s = name.toUpperCase() ;
			int i = s.lastIndexOf('.') ;
			if (i < 0) continue ;
			String ss = s.substring(i) ;

         // Remember the first LZH element in the archive.

         if (".lzh".equalsIgnoreCase(ss))
            if (lzhentry == null) lzhentry = (ArchiveEntry) o ;

         // Is the element of the required type?

			for (i = 0 ; i < ext.length ; i++)
         {
				if (ss.equals(ext[i].toString()))
            {
            	entries.addElement(s) ;
               entrycount++ ;
               break ;
            }
         }
         
         // Is this the specific entry requested?  If so, use only the
         // specific entry.
         
         if (entry instanceof String)
            if (((String) entry).equalsIgnoreCase(name)) 
            {
               entries = new Vector() ;
               entries.addElement(name) ;
               entrycount = 1 ;
               break ;
            }
		}

      // If we failed to find a configuration entry and we had opened an
      // archive with a contained LZH file, then we should unpack the archive
      // and extract the configuration from the LHA file.

      StringTokenizer st = new StringTokenizer(title," ") ;
      String type = (st.hasMoreElements()) ? (String) st.nextElement() : "suitable" ;
      StringTokenizer st1 = new StringTokenizer(captions.getString("ConfigurationListTitle")," ") ;
      String cnf = (st1.hasMoreElements()) ? (String) st1.nextElement() : "" ;
      if (entries.size() == 0 && cnf.equalsIgnoreCase(type) && lzhentry != null)
      {
         if (unpack(lzhentry))
         {
            return showConfig(parent) ;
         }
      }

		// Show the selection dialog.

      if (entry instanceof String && entrycount == 1)
         if (entries.elementAt(0).equals(entry)) single = false ;
      if (entries.size() == 0 && !importonly) return null ;
		return showConfigAllowImport(parent,title,entries,single,allowimport,importonly) ;
	}


	// A convenience method to display the configuration selection dialog
   // using the default title and extensions, with possibly a specific CNF.

	ArchiveEntry showConfig(JFrame parent)
	{ return showConfig(parent,title,ext) ; }
   
  	ArchiveEntry showConfig(JFrame parent, String cnf)
	{ return showConfig(parent,title,ext,cnf) ; }



   // A method to find an entry in the open file.

   ArchiveEntry findEntry(String ext)
   {
      if (zip == null) return null ;
      ArchiveEntry ae = null ;
      Enumeration enum1 = zip.entries() ;
   	while (enum1 != null && enum1.hasMoreElements())
		{
			Object o = enum1.nextElement() ;
			String name = ((ArchiveEntry) o).getPath() ;
			String s = name.toUpperCase() ;
			int i = s.lastIndexOf('.') ;
 			if (i < 0) continue ;
			String ss = s.substring(i) ;

         // Check for the requisite element.

  			if (ss.equalsIgnoreCase(ext))
         {
           	ae = (ArchiveEntry) o ;
            break ;
         }
   	}
      return ae ;
   }


   // A method to extract an archive from an archive file.

   boolean unpack(ArchiveEntry lzhentry)
   {
      if (zip == null) return false ;
      try
      {
         if (OptionsDialog.getDebugLoad())
  	         System.out.println("Extracting " + lzhentry + " from " + zip) ;
         InputStream is = lzhentry.getInputStream() ;
         ByteArrayOutputStream os = new ByteArrayOutputStream() ;

         // Extract the LZH file from the archive file.

         int b = 0 ;
         if (is != null)
         {
            while ((b = is.read()) >= 0) { os.write(b) ; }
            is.close() ;
            os.close() ;

            // Construct a MemFile object for the extracted file.

            MemFile memfile = UrlLoader.getMemoryFile() ;
            if (memfile != null) memfile.close() ;
            memfile = new MemFile(lzhentry.getName(),os.toByteArray()) ;
            UrlLoader.setMemoryFile(memfile) ;
            pathname = lzhentry.getName() ;
            open() ;
         }
      }
      catch (IOException e)
      {
         System.out.println("FileOpen: unpack exception " + e) ;
         return false ;
      }
      return true ;
   }


   // Method to open the archive file defined by our pathname.  This will
	// establish new archive entries for the file.  This constructor establishes
	// an archive entry for the specified element.

   void open(String name)
	{ elementname = name ; ze = null ; open() ; }
   
   void open(String path, String cnf)
   {
      if (cnf != null)
         open(cnf) ;
      else
         open(path) ;
   }


   // Method to open the archive file defined by an archive entry.  This will
	// establish new archive entries for the file.  This constructor establishes
	// an archive entry for the specified element.

   void open(ArchiveFile af, ArchiveEntry ae)
	{ 
      if (af == null || ae == null) open() ; 
      ze = ae ;
      zip = af ;
   }


	// The no-argument method opens the file.  Our pathname can be a
   // standard file path or a URL specification.

   void open()
   {
      URL newFileURL = null ;

		try
		{
			if (pathname == null) return ;
         try { newFileURL = new URL(pathname) ; }
         catch (MalformedURLException e)
         {
   			URL codebase = Kisekae.getBase() ;
				if (codebase == null) codebase = new URL("file","",-1,"") ;
   			String protocol = codebase.getProtocol() ;
   			String host = codebase.getHost() ;
   			int port = codebase.getPort() ;
   			newFileURL = new URL(protocol,host,port,pathname) ;
         }
			int i = pathname.lastIndexOf(".") ;
			extension = (i < 0) ? "" : pathname.substring(i).toLowerCase() ;

         // Confirm that we are opening a file that is not already open.
         // If a file is open and it is the same file that this fileopen
         // object applies to, then simply access any required zip element.

         if (zipFileURL != null && zip != null)
         {
	         if (newFileURL.equals(zipFileURL))
            {
		         if (elementname != null)
               {
                  if (!zip.isOpen()) zip.open() ;
						ze = zip.getEntry(elementname,true) ;
               }
					return ;
            }
         	close() ;
         }

			// Establish the fileopen URL file.

			if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			zipFileURL = newFileURL ;
			String urlfile = zipFileURL.getFile() ;
			File f = new File(urlfile) ;

			// Open the URL file.  Note that an open() request does not adjust
			// the current dirname for the active directory on the host system.

			if (".zip".equals(extension))
				zip = new PkzFile(this,urlfile,mode) ;
         else if (".gzip".equals(extension))
				zip = new PkzFile(this,urlfile,mode) ;
			else if (".jar".equals(extension))
				zip = new PkzFile(this,urlfile,mode) ;
			else if (".lzh".equals(extension))
				zip = new LhaFile(this,urlfile,mode) ;
			else
				zip = new DirFile(this,(f.isDirectory()) ? urlfile : f.getParent()) ;

      	// Locate the required archive entry in the file.  Search on name only.

         if (ze != null)
  				elementname = ze.getPath() ;
         if (elementname != null)
				ze = zip.getEntry(elementname,true) ;
			else if (zip instanceof DirFile && !f.isDirectory())
			{
           	elementname = f.getPath() ;
            ze = zip.getEntry(elementname) ;
			}
      }

		// Catch file error exceptions.

		catch (Exception e)
		{
         System.out.println("KiSS file open exception, " + e.toString()) ;
         String msg = e.getMessage() ;
         if (msg.contains("zip END header")) msg += "\nCheck download protocol, use https" ;
         if (!Kisekae.isBatch())
   			JOptionPane.showMessageDialog(parent, msg,
            	captions.getString("FileOpenException"),
               JOptionPane.ERROR_MESSAGE) ;
         if (!(e instanceof IOException)) 
            e.printStackTrace() ;
         close() ;
      }
      
      finally
      {
         if (parent != null)
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		}
   }


	// Method to close the zip file and erase all record of the file content
   // entries.

	void close()
	{
   	if (zipFileURL == null) zip = null ;
		try { if (zip != null) zip.close() ; }
		catch (IOException e)
		{
			System.out.println("KiSS file close exception, " + e.toString()) ;
         if (!Kisekae.isBatch())
   			JOptionPane.showMessageDialog(parent, e.toString(),
   				captions.getString("FileCloseException"),
               JOptionPane.ERROR_MESSAGE) ;
		}

		// Clear references.

//      parent = null ;
      allfiles = null ;
      archives = null ;
      kissarchives = null ;
      palettefiles = null ;
      imagefiles = null ;
      mediafiles = null ;
      audiofiles = null ;
      videofiles = null ;
      textfiles = null ;
      elementname = null ;
      zipFileURL = null ;
      files = null ;
      entries = null ;
      zip = null ;
      ze = null ;
	}


   // Enable multiple selections.

   void setMultiple(boolean b) { multiple = b ; }


   // Set the default file filter.

   void setFileFilter(String s) { filefilter = s ; }


   // The toString method returns a string representation of this object.
	// This is the zip URL if the object is open, otherwise the pathname.

   public String toString()
	{
		String s = "Unopen, path " + ((pathname == null) ? "unknown" : pathname) ;
		if (zipFileURL != null) s = "Open " + zipFileURL.toExternalForm() ;
		return s ;
   }
}
