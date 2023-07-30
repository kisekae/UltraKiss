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
import java.awt.image.* ;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.* ;
import javax.swing.text.rtf.* ;
import javax.swing.text.html.* ;
import java.io.* ;
import java.net.URL ;
import java.util.zip.* ;
import java.util.jar.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.ResourceBundle ;

/**
* FileWriter class
*
* Purpose:
*
* This class provides a window to show file writing progress in an
* independent thread.  It is quite complicated and poorly designed.
* It handles archive to archive writing, directory to directory, and
* related combinations.  It will write a single file or a complete
* configuration with all elements.
*
* Be careful if changing the code.
*
*/

final class FileWriter extends KissFrame
	implements WindowListener, ActionListener, Runnable
{
	private static final int SAVE = 0 ;
	private static final int COPY = 1 ;
	private static final int EXTRACT = 2 ;
	private static final int ADD = 3 ;
	private static final int DELETE = 4 ;
	private static final int EXPORT = 5 ;
	private static final int CONVERT = 6 ;

   // I18N attributes

   private ResourceBundle captions = Kisekae.getCaptions() ;

   private JFrame parent = null ;         // Our parent window
	private Thread thread = null ;			// The writer thread
	private KissObject kiss = null ;			// Our KiSS object to save
	private ArchiveFile kisszip = null ;	// Our open kiss archive object
	private ArchiveFile sourcezip = null ;	// Our open source archive objext
	private FileOpen fileopen = null ;		// The source fileopen object
	private Vector contents = null ;   		// The configuration contents
	private File fd = null ;   				// The new destination file
	private int mode = SAVE ;					// The invoke mode

	private String source = null ; 			// Full path name of source
	private String destination = null ; 	// Full path name of destination
	private String directory = null ; 		// Destination directory path
	private String filename = null ; 		// Destination file name

	// Status indicators

	private boolean interrupted = false ;	// True if write is interrupted
	private boolean error = false ;			// True if error on write
	private boolean alternate = false ;		// True if alternate name on write
	private boolean saveset = false ;		// True if save all elements
	private boolean saveconfig = false ;	// True if saving a configuration
	private boolean updated = false ;		// True if kiss object was updated
	private int progress = 0 ;					// Progress bar value
	private int maxprogress = 0 ;				// Progress maximum value

	// Control definitions

	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel1a = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JLabel Status = new JLabel();
	private JLabel FileName = new JLabel();
	private JLabel Directory = new JLabel();
	private JLabel ErrorText = new JLabel();
	private JProgressBar Progress = new JProgressBar();
	private JButton CANCEL = new JButton();

	// Our update callback button that other components can attach
	// listeners to.

	protected CallbackButton callback = new CallbackButton(this,"FileWriter Callback") ;


	// Constructor to copy a source archive file to a destination file.
	// A copy retains the extra file directory information.

	public FileWriter(JFrame f, ArchiveFile zip, String dest)
	{
      parent = f ;
		mode = COPY ;
		sourcezip = zip ;
		contents = zip.getContents() ;
		for (int i = 0 ; i < contents.size() ; i++)
			((ArchiveEntry) contents.elementAt(i)).setSaveDir(true) ;
		init(null,zip.getName(),dest,false) ;
	}


	// Constructor to extract the selected content files from an archive.

	public FileWriter(JFrame f, ArchiveFile zip, String dest, Vector c)
	{
      parent = f ;
		mode = EXTRACT ;
		sourcezip = zip ;
		contents = c ;
		init(null,zip.getName(),dest,true) ;
	}


	// Constructor to add selected content files to an archive.   The
	// contents vector must contain all element additions as the archive
	// file is recreated from scratch.

	public FileWriter(JFrame f, ArchiveFile zip, Vector c, boolean add)
	{
      parent = f ;
		mode = (add) ? ADD : DELETE ;
		sourcezip = zip ;
		contents = c ;
		init(null,zip.getName(),zip.getName(),true) ;
	}


	// Constructor to write a specified set of cels.   The contents vector must 
   // contain all cels in a writable form.  The files are written to the 
   // specified archive file, which can be a directory.
   
	public FileWriter(JFrame f, ArchiveFile zip, Vector c)
	{
      parent = f ;
		mode = SAVE ;
		sourcezip = zip ;
		contents = new Vector() ;
      if (c != null)
      {
         for (int i = 0 ; i < c.size() ; i++)
         {
            Object o = c.elementAt(i) ;
            if (o instanceof ArchiveEntry) 
               contents.addElement(o) ;
            else if (o instanceof KissObject)
            {
               KissObject ko = (KissObject) o ;
               ArchiveEntry ae = ko.getZipEntry() ;
               contents.addElement(new ContentEntry(ko,ae,false)) ;
            }
         }
      }
		init(null,zip.getName(),zip.getName(),true) ;
	}


	// Constructor to write all elements of a KiSS object to the destination.
	// If we are exporting our KiSS object we flag the file write mode.

	public FileWriter(JFrame f, KissObject k, String src, String dest, int option)
	{
      parent = f ;
		mode = SAVE ;
		if (option == 1) mode = COPY ;
		if (option == 2) mode = EXPORT ;
		if (option == 3) mode = CONVERT ;
		boolean ss = (option == 0) || (option == 1) || 
         (ArchiveFile.isConfiguration(src) && option == 2) ;
		init(k,src,dest,ss) ;
	}


	// Initialization, common to all constructors.  We need not have a KiSS
	// object but we must have a valid source file and destination file.
	// A KiSS object without an archive entry or archive file is assumed
	// to be a directory file object.

	private void init(KissObject k, String src, String dest, boolean ss)
	{
		kiss = k ;
		source = src ;
		destination = dest ;
		saveset = ss ;

		// Split the requested destination into a directory, file name, and
		// extension.

		try
		{
			File fs = (source == null) ? null : new File(source) ;
			File fd = (destination == null) ? null : new File(destination) ;
         if (fd != null)
         {
            directory = (fd.isDirectory()) ? destination : fd.getParent() ;
            filename = (fd.isDirectory()) ? "" : fd.getName() ;
         }

			// Establish our source KiSS object zipfile and fileopen object.

			kisszip = (kiss == null) ? null : kiss.getZipFile() ;
			if (kisszip != null) fileopen = kisszip.getFileOpen() ;
			if (fileopen == null && sourcezip != null) fileopen = sourcezip.getFileOpen() ;
			if (fileopen == null && sourcezip == null) sourcezip = kisszip ;
			if (kiss != null) updated = kiss.isUpdated() ;

			// If we are saving a text file we may be converting between normal
			// text and styled text.  We must set the appropriate translation.

			if (kiss instanceof TextObject)
			{
				if (ArchiveFile.isRichText(destination))
					((TextObject) kiss).setEditorKit(new RTFEditorKit()) ;
				if (ArchiveFile.isHTMLText(destination))
					((TextObject) kiss).setEditorKit(new HTMLEditorKit()) ;
				if (ArchiveFile.isText(destination))
					((TextObject) kiss).setEditorKit(null) ;
			}

			// Determine if our true destination is an archive file.  Our original
			// destination can be an archive file if we are saving an existing
			// data set.  It will be an element file if we are saving a specific
			// KiSS object.  In the latter case our final destination will be the
			// parent zip file in which the KiSS object resides.

			if (!ArchiveFile.isArchive(filename))
			{
				if (kisszip != null && kisszip.isArchive())
				{
					destination = kisszip.getName() ;
					directory = kisszip.getDirectoryName() ;
					if (dest != null && dest.startsWith(directory))
					{
						filename = dest.substring(directory.length()) ;
						if (filename.startsWith(File.separator))
							filename = filename.substring(File.separator.length()) ;
					}
				}
			}

			// Set the frame characteristics.

			try { jbInit() ; }
			catch(Exception e)
			{ e.printStackTrace() ; }

			// Center the frame in the screen space.

			setSize(400,200) ;
         center(this,parent) ;
			setIconImage(Kisekae.getIconImage()) ;
			setTitle(captions.getString("FileWriterTitle")) ;

			// Set the frame title depending on our mode.

			switch (mode)
			{
			case SAVE:
            String s1 = captions.getString("FileWriterTitle1") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (sourcezip == null && (i1 >= 0 && j1 > i1))
               s1 = s1.substring(0,i1+1) + dest + s1.substring(j1) ;
            else if (sourcezip != null && (i1 >= 0 && j1 > i1))
               s1 = s1.substring(0,i1+1) + fs.getName() + s1.substring(j1) ;
            else if (kisszip != null && (i1 >= 0 && j1 > i1))
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
				setTitle(s1) ;
				break ;

			case COPY:
            s1 = captions.getString("FileWriterTitle2") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fd != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			case EXTRACT:
            s1 = captions.getString("FileWriterTitle3") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fs != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fs.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			case ADD:
            s1 = captions.getString("FileWriterTitle4") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fd != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			case DELETE:
            s1 = captions.getString("FileWriterTitle5") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fd != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			case EXPORT:
            s1 = captions.getString("FileWriterTitle6") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			case CONVERT:
            s1 = captions.getString("FileWriterTitle7") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fd != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fd.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;

			default:
            s1 = captions.getString("FileWriterTitle1") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (fs != null && i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1+1) + fs.getName() + s1.substring(j1) ;
            setTitle(s1) ;
				break ;
			}

			// Setup to catch window events in this frame.

			addWindowListener(this) ;
			CANCEL.addActionListener(this) ;
         boolean b = OptionsDialog.getShowLoad() ;
         KissFrame f = Kisekae.getBatchFrame() ;
         boolean b1 = (f instanceof WebSearch.WebSearchFrame)
            ? !((WebSearch.WebSearchFrame) f).isLocalSearch() : true ;
         if (OptionsDialog.getUseDefaultWS()) b = b1 ;
         if (!Kisekae.isBatch() || b) setVisible(true) ;
		}

		// Catch security exceptions.

		catch (SecurityException e)
		{
			System.out.println("KiSS file write exception, " + e.getMessage()) ;
			JOptionPane.showMessageDialog(null,
            captions.getString("SecurityException") + "\n" +
				captions.getString("FileOpenSecurityMessage1"),
				captions.getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


	// User interface initialization.

	private void jbInit() throws Exception
	{
		Status.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
		Status.setPreferredSize(new Dimension(150, 20));
		FileName.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
		FileName.setPreferredSize(new Dimension(250, 20));
		FileName.setHorizontalAlignment(SwingConstants.CENTER);
		Progress.setPreferredSize(new Dimension(400, 16));
		CANCEL.setText(captions.getString("CancelMessage"));
		Status.setText("Initializing ...") ;
		Directory.setText(" ");

		this.getContentPane().setLayout(borderLayout1);
		jPanel1.setLayout(borderLayout2);
		jPanel1a.setLayout(borderLayout3);
		jPanel1a.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel2.setLayout(gridLayout1);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		gridLayout1.setColumns(1);
		gridLayout1.setRows(2);

		this.getContentPane().add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel1a, BorderLayout.NORTH);
		jPanel1a.add(Status, BorderLayout.WEST);
		jPanel1a.add(FileName, BorderLayout.EAST);
		jPanel1a.add(Progress, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(Directory, null);
		jPanel2.add(ErrorText, null);
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(Box.createGlue()) ;
		jPanel3.add(CANCEL, null);
		jPanel3.add(Box.createGlue()) ;
	}


	// Method to intialize the progress bar display.

	void initProgress(int max)
	{
		progress = 0 ;
		maxprogress = (max < 0) ? 0 : max ;
		Progress.setValue(0) ;
		Progress.setMaximum(maxprogress) ;
		ErrorText.setText("Bytes: " + maxprogress) ;
      notifyWebSearch("Save Progress: 0%") ;
	}


	// Method to update the progress bar display.

	void updateProgress(int x)
	{
		progress += x ;
		Progress.setValue(progress) ;
		if (maxprogress < progress) maxprogress = progress ;
		int complete = (int) (((float) progress) / ((float) maxprogress) * 100) ;
		int fieldsize = 20 ;
		KissDialog.format("clear","",0,65) ;
		KissDialog.format("left","Bytes: " + maxprogress,0,20) ;
		KissDialog.format("left","Completed: " + progress,20,20) ;
		KissDialog.format("left","(" + complete + "%)",45,20) ;
		ErrorText.setText(KissDialog.format(" ","",0,65)) ;
      notifyWebSearch("Save Progress: " + complete + "%") ;
	}


	// Method to interrupt any active write thread.

	void interrupt() { interrupted = true ; }


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt)
	{
		System.out.println("Write cancelled ...") ;
		Status.setText("Save cancelled.") ;
      if (Kisekae.isBatch()) callback.doClick() ;
		interrupted = true ;
		flush() ;
		dispose() ;
	}


	// Action Events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		// A Cancel request stops the writer thread.

		if (source == CANCEL)
		{
			System.out.println("Write cancelled ...") ;
			Status.setText("Save cancelled.") ;
         if (Kisekae.isBatch()) callback.doClick() ;
			interrupted = true ;
			flush() ;
			dispose() ;
		}
	}


	// The writer code.  This code runs as a separate thread.
	// This thread runs at low priority.  The source zipfile and
	// output destination must be established before this thread
	// is initiated.

	public void run()
	{
		if (filename == null) error = true ;
		if (!ArchiveFile.isArchive(filename) && directory == null) error = true ;
		if (error) ErrorText.setText("Internal fault, attempt to save unnamed file.") ;
      Vector originalcontents = new Vector() ;
		thread = Thread.currentThread() ;
  		thread.setName("FileWriter") ;
  		thread.setPriority(Thread.MIN_PRIORITY) ;
		toFront() ;

      // Confirm file save is allowed.

      if (Kisekae.isRestricted())
      {
			System.out.println("FileWriter: Product license has expired.") ;
      	ErrorText.setText("Product license has expired.  File output is restricted.") ;
         error = true ;
      }

      // Trace save operation.

      if (!error)
      {
         if (ArchiveFile.isArchive(filename))
            System.out.println("Save archive " + destination) ;
         else if (!ArchiveFile.isArchive(destination))
         {
            File f = new File(directory,filename) ;
            if (f.isDirectory())
               System.out.println("Extract " + source + " to directory " + f.getPath()) ; 
            else
               System.out.println("Save file " + f.getPath()) ;
         }
         else
            System.out.println("Save file " + filename + " to archive " + destination) ;

         // Determine the destination file extension.  This is used to establish
         // the output file type.

         int i = destination.lastIndexOf(".") ;
         String extension = (i < 0) ? "" : destination.substring(i).toLowerCase() ;

         // Create a temporary file for the new output if we are writing to
         // an archive file and the destination file equals the source file.

         try
         {
				Status.setText("Opening source file ...") ;
            if (ArchiveFile.isArchive(extension) && destination.equalsIgnoreCase(source))
            {
               fd = createTemporaryFile(directory) ;
               if (fd != null) destination = fd.getPath() ;
            }

            // Open the source file for input.  We must reference the newly
            // opened archive file to obtain correct file directory contents.
            // We construct a list of the writeable archive entries of our kiss
            // object so that we can reference the object memory values when we
            // write to the output file.

				if (kisszip != null)
				{
					if (fileopen != null)
				   {
                  String s = fileopen.getFile() ;
                  FileName.setText((s == null) ? "" : "File: " + fileopen.getFile()) ;
                  fileopen.open() ;
						kisszip = fileopen.getZipFile() ;
				   }
				   else
						FileName.setText("File: " + kisszip.getName()) ;
				}

				if (sourcezip != null)
					FileName.setText("File: " + sourcezip.getName()) ;

				// Establish the archive entries to be processed. We need to
            // retain our original contents as the contents vector can
            // be modified during write processing.

				if (contents == null)
					contents = getContents(kiss,kisszip) ;
            originalcontents = (Vector) contents.clone() ;

   			// The file extension determines the output file type.  Compressed
   			// files are files with a ".zip", ".jar", or ".lzh" extension.
   			// Directory files are all other files.

				Status.setText("Writing files ...") ;
				Directory.setText("To: " + directory) ;
				if (ArchiveFile.isArchive(extension))
					writearchive(kisszip,destination,extension) ;
				else if (ArchiveFile.isDirectory(extension))
					writearchive(kisszip,directory,extension) ;

   			// Rename the new output file as the original source file.
   			// Create a source backup file for safety.

   			if (fileopen != null) fileopen.close() ;
   			fd = closeTemporaryFile(fd) ;
   			if (fd != null) destination = fd.getPath() ;
            if (error) 
               Status.setText("Save terminated.") ;
            else
            {
               Status.setText("Save complete.") ;
               CANCEL.setEnabled(false) ;
            }
   		}

   		// Watch for internal thread termination faults.

   		catch (Throwable e)
   		{
   			error = true ;
   			Status.setText("Write fault.") ;
   			ErrorText.setText("Error: " + e.toString()) ;
   			e.printStackTrace() ;
   		}
      }

		// Dispose with our window if we had a successful save.
		// Clear the update state for all content entries.

		if (!error)
		{
   		for (int i = 0 ; i < originalcontents.size() ; i++)
			{
            KissObject k = null ;
				Object o = originalcontents.elementAt(i) ;
				if (o instanceof ContentEntry) 
            {
               ContentEntry ce = (ContentEntry) o ;
               k = ce.kiss ;
               if (k != null) 
               {
                  k.setUpdated(false) ;
                  ArchiveEntry ze = k.getZipEntry() ;
                  if (ze != null) ze.setWriting(false) ;
               }
            }
            else if (o instanceof ArchiveEntry)
            {
               ArchiveEntry ze = (ArchiveEntry) o ;
               ze.setUpdated(false) ;
            }
   		}

         if (saveset)
         {
            ArchiveFile zip = (kiss != null) ? kiss.getZipFile() : null ;
            if (zip != null) zip.clearUpdated() ;
         }

			// If we saved a file under a new name then update the zip entry
			// file name.  If we saved the file to a new archive file then update
			// the archive name and the fileopen path name. Component cel names
         // are not changed, nor are imported cels that have been saved in
         // CEL format.

			if (kiss != null && mode == SAVE)
			{
            boolean b = (kiss instanceof Cel) && kiss.isImported() ;
            if (!(b && ((Cel) kiss).isImportedAsCel()))
            {
               ArchiveFile zip = kiss.getZipFile() ;
               ArchiveEntry ze = kiss.getZipEntry() ;
               FileOpen fileopen = (zip == null) ? null : zip.getFileOpen() ;
               if (ArchiveFile.isDirectory(destination))
               {
                  if (!(kiss instanceof JavaCel)) kiss.setName(destination) ;
                  if (ze != null) ze.setPath(destination) ;
                  if (fileopen != null) fileopen.setPath(destination) ;
                  if (ze != null && zip != null) zip.setName(ze.getDirectory()) ;
                  try 
                  { 
                     if (ze != null) ze.setZipFile(new DirFile(fileopen,ze.getPath())) ; 
                  }
                  catch (IOException e) 
                  {
                     System.out.println("FileWriter: new DirFile Archive " + e) ;
                  }
               }
               if (ArchiveFile.isArchive(destination))
               {
                  if (!ArchiveFile.isArchive(filename)) 
                     if (!(kiss instanceof JavaCel)) kiss.setName(filename) ;
                  if (zip != null) zip.setName(destination) ;
                  if (fileopen != null) fileopen.setPath(destination) ;
                  if (!ArchiveFile.isArchive(filename))
                  if (ze != null) ze.setPath(filename) ;
               }
            }
			}

         System.out.println("Write complete to " + destination) ;
			Status.setText("Save complete.") ;


			// Perform any callbacks that are required.

         CANCEL.setEnabled(false) ;
			try { thread.sleep(1000); }
			catch (InterruptedException e) { }
			flush() ;
			dispose() ;
			callback.doClick() ;
		}
	}


	// Method to write the contents of an archive file.   This routine takes
	// all elements in the input archive file and writes them to the output
	// file defined by the pathname.  The extension is used to establish the
	// output file type.

	private void writearchive(ArchiveFile zipfile, String pathname, String extension)
	{
		String element = null ;   					// output element name
      String newname = null ;                // output new element name
      String type = null ;                   // output extension
		Enumeration enum1 = null ;
		byte buffer [] = new byte[10240] ;
		OutputStream out = null ;
		InputStream in = null ;
		DirEntry de = null ;

		// Open the new output file.  Archive files are written as one
		// complete unit.  Directory files write each element individually
		// to the destination directory.

		try
		{
			if (".zip".equals(extension))
			{
				FileOutputStream stream = new FileOutputStream(pathname) ;
				out = new ZipOutputStream(stream) ;
			}
			if (".gzip".equals(extension))
			{
				FileOutputStream stream = new FileOutputStream(pathname) ;
				out = new GZIPOutputStream(stream) ;
			}
			if (".jar".equals(extension))
			{
				FileOutputStream stream = new FileOutputStream(pathname) ;
				out = new JarOutputStream(stream,new Manifest()) ;
			}
			if (".lzh".equals(extension))
			{
				LhaFile lha = new LhaFile(fileopen,pathname,"rw") ;
				out = lha.getOutputStream(null) ;
			}

			// If a zipfile was specified then we are saving a KiSS data set.
			// Obtain an enumeration of all the output file elements. The source
			// file type is set by the zipfile and the destination file type is
			// determined by the extension.

			if (zipfile != null)
			{
				// Archive to archive.  We select all the archive elements
				// and add any new element that must be appended through a
				// Save As request.  Archive file entries are unique by name.
            // Archive copies retain path information.

				if (zipfile.isArchive() && ArchiveFile.isArchive(extension))
				{
					Vector v = new Vector(zipfile.getContents()) ;
         		for (int i = 0 ; i < v.size() ; i++)
         			((ArchiveEntry) v.elementAt(i)).setSaveDir(true) ;
					if (ArchiveFile.isArchive(filename))
					{
                  // Saving a configuration set to an archive
               	for (int i = 0 ; i < contents.size() ; i++)
                  {
                     if (!(contents.elementAt(i) instanceof ContentEntry)) continue ;
                  	ContentEntry o = (ContentEntry) contents.elementAt(i) ;
                     ArchiveEntry ce = (o != null) ? o.ze : null ;
                     if (ce == null) continue ;
                     ArchiveEntry zipentry = zipfile.getEntry(ce.getPath()) ;
                     if (zipentry == null) v.addElement(ce) ;

                     // A contents entry duplicates an entry in the zip file.
                     // Substitute our content entry for the zip file archive
                     // entry and remove the contents vector entry.

                     else
                     {
                        int n = v.indexOf(zipentry) ;
                        if (ce.isUpdated())
                        {
                           if (n < 0)
                              v.addElement(o) ;
                           else
                              v.setElementAt(o,n) ;
                           contents.setElementAt(null,i);
                        }
                     }
                  }
					}
					else
					{
                  // Saving a specific file to an archive
						if (contents.size() > 0 && contents.elementAt(0) instanceof ContentEntry)
						{
							ContentEntry o = (ContentEntry) contents.elementAt(0) ;
                     String cepathname = o.ze.pathname ;
                     ArchiveEntry zipentry = zipfile.getEntry(cepathname) ;
							if (zipentry == null)
                     {
                        if (o != null) v.addElement(o.ze) ;
                     }

                     // A contents entry duplicates an entry in the zip file.
                     // Substitute our content entry for the zip file archive
                     // entry and remove the contents vector entry.

                     else
                     {
                        int n = v.indexOf(zipentry) ;
                        if (o != null)
                        {
                           o.ze.setSaveDir(zipentry.getSaveDir());
                           o.ze.setSaveRelDir(zipentry.getSaveRelDir());
                           v.setElementAt(o,n) ;
                           contents.setElementAt(null,0) ;
                        }
                     }
						}
					}
					enum1 = v.elements() ;
					initProgress((int) getContentSize(v)) ;
				}

				// Non-archive to non-archive.  We select only the specified
				// file object unless we were saving all modified elements of
				// a configuration in a directory.

				else if (!zipfile.isArchive() && !ArchiveFile.isArchive(extension))
				{
					Vector v = new Vector() ;
					if (contents != null)
					{
						for (int i = 0 ; i < contents.size() ; i++)
						{
                     if (!(contents.elementAt(i) instanceof ContentEntry)) continue ;
							ContentEntry o = (ContentEntry) contents.elementAt(i) ;
							if (o != null) v.addElement(o.ze) ;
							if (!saveset) break ;
						}
					}
					enum1 = v.elements() ;
					initProgress((int) getContentSize(v)) ;
				}
			}

			// If a KiSS zipfile was not specified then we are extracting or
			// adding a new collection of file elements to the destination file.
			// The contents collection must contain all necessary ArchiveEntries
			// to be processed.

			if (zipfile == null)
			{
				// Non-archive to archive.  This mode is used to add new
				// elements to an archive file.

				if (ArchiveFile.isArchive(extension))
				{
					enum1 = contents.elements() ;
					initProgress((int) getContentSize(contents)) ;
				}

				// Archive to non-archive.  This mode is used to extract
				// elements from an archive file.

				else if (!ArchiveFile.isArchive(extension))
				{
					enum1 = contents.elements() ;
					initProgress((int) getContentSize(contents)) ;
				}
			}


			// Write each data set element.  For each element obtain an input
			// stream, then write the data to the output file.  For loaded
			// KiSS objects our enumeration entries will be ArchiveEntries,
			// but this will not be the case for new objects that are created
			// dynamically.

			while (enum1 != null && enum1.hasMoreElements())
			{
				int bytes = 0 ;
				element = null ;
				KissObject ko = null ;
				ArchiveEntry next = null ;
				Object o = enum1.nextElement() ;
				if (o == null) continue ;
            boolean export = false ;

				// Determine the element name by the enumeration type.
            // The KiSS object contains the original name.  The next
            // archive entry contains the new name.

				if (o instanceof ArchiveEntry)
				{
               ko = null ;
					next = (ArchiveEntry) o ;
					if (next == null) continue ;
					element = next.getPath() ;
               int n = findKissObject(element,contents) ;
               if (n >= 0) 
               {
                  Object content = contents.elementAt(n) ;
                  if (content instanceof ContentEntry)
                  {
                     ko = ((ContentEntry) content).kiss ;
                     export = ((ContentEntry) content).export ;
                  }
               }
				}
				if (o instanceof ContentEntry)
				{
					ko = ((ContentEntry) o).kiss ;
               export = ((ContentEntry) o).export ;
               ArchiveEntry ce = ((ContentEntry) o).ze ;
					if (ko == null || ce == null) continue ;
					next = new DirEntry(ko.getDirectory(),ce.getName(),null) ;
               ((DirEntry) next).setFileSize(ce.getSize()) ;
					if (ko.getName() == null) next.setPath(destination) ;
					element = next.getPath() ;
				}

				// If the output file element is a writable KiSS object in our
				// configuration then we will write the KiSS object contents to
				// the output file.  Otherwise, we establish an input stream to
				// copy the archive source to the new output file.

				if (ko != null && !ko.isWritable())
					if (!(ko instanceof JavaCel) || saveset) ko = null ;
				if (OptionsDialog.getSaveSourceOn())
					if (ko != null && !(next.isUpdated() || ko.isUpdated() || export)) ko = null ;
				if (ko == null && zipfile != null)
					in = zipfile.getInputStream(next) ;
				if (ko == null && zipfile == null)
					in = next.getInputStream() ;

				// If we have a KiSS object and it has been updated then
				// we should adjust the archive entry time.

				if (ko != null && ko.isUpdated())
					next.setTime(ko.lastModified()) ;

				// Verify that we found a valid file.

				if (ko == null && in == null)
		  		{
               int i = JOptionPane.NO_OPTION ;
					System.out.println("Save: Unable to read file " + element) ;
               if (!Kisekae.isBatch())
                  i = JOptionPane.showConfirmDialog(this,
                     captions.getString("FileReadError") +
                     "\n" + element + "\n" +
                     captions.getString("ContinueMessage") + "?",
                     captions.getString("OptionsDialogWarningTitle"),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.WARNING_MESSAGE) ;
  					if (i == JOptionPane.YES_OPTION) continue ;
					Status.setText("Save aborted.") ;
					throw new KissException("Save cancelled") ;
				}

				// Create the output archive element.  The new element name
				// will retain directory path information if our active element
				// has the save directory flag set or we are explicitly saving
				// a configuration element and the element was not imported.

  				if (!next.getSaveDir() && !saveconfig) element = next.getName() ;
  				if (next.isImported()) element = next.getName() ;
				newname = new String(element) ;
            int j = newname.lastIndexOf('.') ;
            if (j > 0) type = newname.substring(j) ;

				// Convert the element name to a relative directory name.

				if (next.getSaveRelDir() || saveconfig)
				{
  					File f = new File(pathname) ;
					String pathdir = f.getParent() ;
               if (pathdir == null) pathdir = "" ;
					if (next instanceof DirEntry && ArchiveFile.isDirectory(extension))
               {
                  String nextdir = next.getDirectory() ;
                  if (nextdir != null && newname.startsWith(nextdir))
						   newname = newname.substring(nextdir.length()) ;
					   else
                     pathdir = pathname ;
               }
					if (newname.startsWith(pathdir))
						newname = newname.substring(pathdir.length()) ;
					if (newname.startsWith(File.separator))
						newname = newname.substring(File.separator.length()) ;
				}

				// Remove the drive prefix if it exists.

				if (next.getSaveDir() || saveconfig)
				{
					int i = newname.indexOf(':') ;
					if (i > 0) newname = newname.substring(i+1) ;
				}
            
            // Ensure trailing slash if zip directory entry (size = 0)
            // Zip files require this.
            
            if (next.getSize() == 0) {
                if (!newname.endsWith(File.separator))
                    newname += File.separator ;
            }

				// Create the new output element entry.  For archive files
				// files the entries are written directly within the archive.
				// For directory files we create a new file in the destination
				// directory.  Note that zip and jar files do not yet store
				// uncompressed entries as we require a CRC-32 value for the
				// uncompressed data file.  ** Updated Apr 11 2023 for zip copy
            // when archive has a directory path **

            if (".zip".equals(extension))
            {
               ZipEntry ze = new ZipEntry(newname) ;
               ze.setTime(next.getTime()) ;
               ze.setMethod((next.isCompressed()) ? ze.DEFLATED : ze.STORED) ;
               if (next instanceof DirEntry) ze.setMethod(ze.DEFLATED);
               if (ze.getMethod() == ze.STORED) {
                   ze.setCrc(next.getCrc32()) ;
                   ze.setSize(next.getSize()) ;
               }
               ((ZipOutputStream) out).putNextEntry(ze) ;
            }

            if (".gzip".equals(extension))
            {
               ZipEntry ze = new ZipEntry(newname) ;
               ze.setTime(next.getTime()) ;
               ze.setMethod((next.isCompressed()) ? ze.DEFLATED : ze.STORED) ;
               if (next instanceof DirEntry) ze.setMethod(ze.DEFLATED);
               if (ze.getMethod() == ze.STORED) {
                   ze.setCrc(next.getCrc32()) ;
                   ze.setSize(next.getSize()) ;
               }
               ((ZipOutputStream) out).putNextEntry(ze) ;
            }
            
            if (".jar".equals(extension))
            {
               ZipEntry ze = new ZipEntry(newname) ;
               ze.setTime(next.getTime()) ;
               ze.setMethod((next.isCompressed()) ? ze.DEFLATED : ze.STORED) ;
               if (next instanceof DirEntry) ze.setMethod(ze.DEFLATED);
               if (ze.getMethod() == ze.STORED) {
                   ze.setCrc(next.getCrc32()) ;
                   ze.setSize(next.getSize()) ;
               }
               ((ZipOutputStream) out).putNextEntry(ze) ;
            }

            if (".lzh".equals(extension))
            {
               LhaEntry le = new LhaEntry(newname) ;
               le.setTime(next.getTime()) ;
               le.setMethod((next.isCompressed()) ? le.LH5 : le.LH0) ;
               if (next.getSize() == 0) le.setMethod(le.LH0); 
               ((LhaOutputStream) out).putNextEntry(le) ;
            }

				// Create the output file.  This may create new directories.

				if (ArchiveFile.isDirectory(extension))
				{
					File newfile = new File(pathname,newname) ;
					if (newfile.exists())
               {
               	// We are about to replace a file in a directory.
                  // If we are checking creation dates, verify that
                  // we are not overwriting a newer file.

						long ta = next.getTime() ;
						long tb = newfile.lastModified() ;
						if (!next.getCheckDate() || !(ta < tb))
                  {
	                  // Create a temporary file.

							source = newfile.getPath() ;
							fd = createTemporaryFile(pathname) ;
							if (error || fd == null)
	                    	throw new KissException("Temp file creation error") ;
							out = new FileOutputStream(fd.getPath()) ;
                  }
                  else
                  {
                  	// Do not write this file as it is too old.

			            updateProgress((int) next.getSize()) ;
                     in.close() ;
                     in = null ;
                     continue ;
                  }
               }

               // File does not exist.  We can write it directly.

               else
               {
						if (next.getSaveDir() || saveconfig)
                  {
                  	File newdirectory = new File(newfile.getParent()) ;
                     newdirectory.mkdirs() ;
						}
						de = new DirEntry(pathname,newname,null) ;
						try { out = de.getOutputStream() ; }
                  catch (FileNotFoundException e)
						{
                     int i = JOptionPane.NO_OPTION ;
				      	ErrorText.setText("Error: Unable to create " + de.getPath()) ;
				  	      String s = "Write exception, unable to create file " + de.getPath() ;
				         System.out.println(s);
                     if (!Kisekae.isBatch()) 
                        i = JOptionPane.showConfirmDialog(this,
                           captions.getString("FileWriteError") +
                           "\n" + element + "\n" +
                           captions.getString("ContinueMessage") + "?",
                           captions.getString("OptionsDialogWarningTitle"),
                           JOptionPane.YES_NO_OPTION,
                           JOptionPane.WARNING_MESSAGE) ;
                     ko = null ;
			            if (in != null) { in.close() ;  in = null ; }
                     if (i == JOptionPane.NO_OPTION) { error = true ; break ; }
                     ErrorText.setText("") ;
                  }
               }
            }

            // Read from the input stream and write to the output stream.
            // For objects in our configuration we write from their memory
            // contents.  For objects with an input stream we copy the
            // input to the output.

				long size = next.getSize() ;
				FileName.setText("File: " + element + " [" + size + " bytes]") ;
				if (ko != null) 
               bytes = (export) ? ko.export(this,out,type) : ko.write(this,out,type) ;
				if (in != null) 
               bytes = copy(this,in,out,buffer) ;

            // Close the input stream.

				if (in != null) in.close() ;
				in = null ;

  				// Close the output stream if we are writing to a directory.
            // Update the last modified time if changed.

				if (ArchiveFile.isDirectory(extension))
            {
					if (out != null) { out.close() ;  out.flush() ; }
					fd = closeTemporaryFile(fd) ;
					long time = next.getTime() ;
               long now = System.currentTimeMillis() ;
               if (next.isUpdated()) time = now ;
					if (fd != null) fd.setLastModified(time) ;
					if (de != null) de.setTime(time) ;
					out = null ;
					fd = null ;
            }

            // Yield the processor before writing another element.

				thread.yield() ;
				if (interrupted) throw new KissException("Write interrupted") ;
				if (OptionsDialog.getDebugLoad())
            {
            	String s = "Save: " + element + " [" + bytes + " bytes]" ;
               if (ko != null) s += " (" + ko.getClass().getName() + ")";
            	System.out.println(s) ;
            }
         }
      }

      // Watch for KiSS exceptions.  These are thrown for planned early
      // termination.  We will delete any temporary output files that we
      // are writing to.

      catch (KissException ex)
      {
      	error = true ;
      	ErrorText.setText("Error: " + ex.getMessage()) ;
         try
         {
	     		if (out != null) { out.close() ; out.flush() ; }
		   	if (fd != null) fd.delete() ;
            out = null ;
            fd = null ;
         }
         catch (IOException e) { }
      }

		// Watch for internal write exceptions.

  		catch (Throwable ex)
  		{
         error = true ;
			ErrorText.setText("Error: " + ex.toString()) ;
			String s = "Write exception, write file " + ((element == null) ? "" : element) ;
			System.out.println(s) ;
			ex.printStackTrace() ;
         if (!Kisekae.isBatch()) JOptionPane.showMessageDialog(this,
            captions.getString("FileWriteError") +
            "\n" + element + "\n" + ex.toString(),
            captions.getString("FileSaveException"),
            JOptionPane.ERROR_MESSAGE) ;
  		}

      // Clean up on termination.

      finally
      {
      	try
			{
				Status.setText("Closing ...") ;
				FileName.setText("File: " + pathname) ;
				if (in != null) in.close() ;
       		if (out != null) { out.close() ; out.flush() ; }
            in = null ;
            out = null ;
         }
         catch (IOException e)
	  		{
	         error = true ;
	      	ErrorText.setText("Error: " + e.toString()) ;
				String s = "Write exception, close file " + ((element == null) ? "" : element) ;
	         System.out.println(s);
            if (!Kisekae.isBatch()) JOptionPane.showMessageDialog(this,
               captions.getString("FileWriteError") +
               "\n" + element + "\n" + e.toString(),
               captions.getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
	  		}
      }
   }


   // A function to copy an input stream to an output stream.

	private int copy(FileWriter fw, InputStream in, OutputStream out, byte [] buffer)
      throws IOException
   {
      int bytes = 0 ;
      if (out instanceof LhaOutputStream)
          ((LhaOutputStream) out).setFileWriter(fw) ;

		while (true)
      {
        	int n = in.read(buffer,0,buffer.length) ;
			if (n <= 0) break ;
			bytes += n ;
         out.write(buffer,0,n);
         if (!(out instanceof LhaOutputStream)) updateProgress(n) ;
      }
      
		return bytes ;
   }


   // Function to reference a KiSS object and construct a list of content
   // entries that relate to every writable object found within the source
   // archive.

	private Vector getContents(KissObject kiss, ArchiveFile zip)
   {
   	Vector v = new Vector() ;
		if (kiss == null) return v ;
		ArchiveEntry ze = kiss.getZipEntry() ;

		// The KiSS object must be associated with its new output file name
		// so that it is created properly when written to the destination
		// output file.  We create a cloned archive entry with the new file
		// name for internal use.  The actual archive entry for the KiSS
		// object is updated only when the file is written. Objects created
		// internally such as palette objects do not have archive entries.

		if (!ArchiveFile.isArchive(filename))
		{
			String s = (ze != null) ? ze.getName() : kiss.getName() ;
			if (s == null || !s.equalsIgnoreCase(filename) || kiss.isInternal()) 
			{
				if (ze == null) ze = new DirEntry(null,s,null) ;
				else ze = (ArchiveEntry) ze.clone() ;
				ze.setName(filename) ;
				ze.setUpdated(true) ;
			}
		}

      // Add the KiSS object zip entry to our contents vector.

      if (ze instanceof DirEntry && kisszip != null && kisszip.isArchive()) ze.setMethod(1) ;
      ContentEntry ce = new ContentEntry(kiss,ze,mode == EXPORT) ;
  		v.add(ce) ;

      // If we are not saving a data set then there will only be one entry
      // in our contents vector.  This entry is the KiSS object being
      // saved.

      if (!saveset) return v ;

      // Configuration objects are collections of cels, palettes, and
      // sound files.  We add the zip entries for all these objects to
      // our contents vector so that their contents can be referenced
      // when the configuration is saved.  We also note that we are
      // saving a configuration set as directory paths are required.

      if (kiss instanceof Configuration)
      {
			saveconfig = true ;
         ArchiveFile opened = null ;
	      Configuration c = (Configuration) kiss ;
         ArchiveFile czip = (c != null) ? c.getZipFile() : null ;
         String directory = (czip != null) ? czip.getDirectoryName() : null ;

	      // Add the cel object entries to the contents vector.  We do not
         // add component or video cels here as they are handled below.
         // On a File Save only updated elements are written unless cels
         // are being exported to a different format.

	      int i = 0 ;
	      Vector cels = c.getCels() ;
	      while (cels != null && i < cels.size())
	      {
				Cel cel = (Cel) cels.elementAt(i++) ;
				if (cel.isError()) continue ;
				if (cel.isInternal()) continue ;
            if (cel instanceof JavaCel) continue ;
            if (cel instanceof Video) continue ;
            if (!OptionsDialog.getExportCel() && !cel.isUpdated())
            {
               if (mode == SAVE) continue ;
               if (mode == COPY && zip != null && zip.isDirectory() && 
                  directory != null && directory.equals(zip.getDirectoryName())) continue ;
            }
            
            // Create a name for this cel if necessary.
            
				ze = (zip == null) ? null : zip.getEntry(cel.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,cel.getName(),null) ;
               cel.setName(ze.getPath()) ;
            }
            
            // Code to export all cels to PNG, JPG, BMP, or CEL images during  
            // a Save or a Save As.

            boolean exportcel = false ;
            if (OptionsDialog.getExportCel())
            {
               try
               {
                  Object o = OptionsDialog.getExportType() ;
                  if (o != null) 
                  {
                     String s = o.toString().toUpperCase() ;
                     String type = "." + s.toLowerCase() ;
                     exportcel = true ;
                     if (OptionsDialog.getDebugLoad())
                        System.out.println("FileWriter: saveset export " + cel + " to " + s);
                    
                     // Load the cel if necessary.
                    
                     if (!cel.isLoaded()) 
                     {
                        if (opened != null && opened != czip)
                           opened.close() ;
                        if (czip != null && !czip.isOpen()) 
                        { 
                           czip.open() ; 
                           opened = czip ; 
                        }
                        cel.load() ;
                     }
                    
                     // Change the cel type for writing. Flag the cel as an
                     // exported cel to ensure that is is not unloaded until
                     // such time as the configuration is reloaded.
                    
                     s = cel.getPath() ;
                     int i1 = s.lastIndexOf('.') ;
                     if (i1 > 0) s = s.substring(0,i1) + type ;
                     cel.setName(s) ;
                     cel.setUpdated(true) ;
                     cel.setExported(true) ;
                     cel.setRelativeName(null) ;
                     ze = cel.getZipEntry() ;
                     s = ze.getPath() ;
                     i1 = s.lastIndexOf('.') ;
                     if (i1 > 0) s = s.substring(0,i1) + type ;
                     ze.setPath(s) ;
                  }
               }
               catch (IOException e) 
               { 
                  System.out.println("FileWriter: prepare export cel, " + e) ;
               }
            }
            
            // Establish the output archive entry.
            
				if (cel.isCopy()) continue ;
				if (cel.isUpdated()) ze = cel.getZipEntry() ;
				if ((ze == null && cel.getZipEntry() instanceof DirEntry) ||
                (ze == null && cel.getZipEntry() == null))
              	ze = new DirEntry(c.getDirectory(),cel.getName(),zip) ;
            if (ze == null) continue ;
            
            // If our cel was imported and we are converting to CEL format
            // on write, we need to update our archive entry to reflect the
            // new output type.
            
            if (cel.isImported() && cel.isImportedAsCel())
            {
               String name = ze.getName() ;
               int n = name.lastIndexOf('.') ;
               if (n >= 0) name = name.substring(0,n) + ".cel" ;
               ze = (ArchiveEntry) ze.clone() ;
               ze.setName(name) ;
            }

            // Establish relative names.

            String path = cel.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            
            if (ze instanceof DirEntry && kisszip != null && kisszip.isArchive()) ze.setMethod(1) ;
				ce = new ContentEntry(cel,ze,(mode == EXPORT || exportcel)) ;
				v.add(ce) ;
			}

         // If we had opened any archives to load a cel for export, close
         // the archive.
         
         if (opened != null)
         {
            try { opened.close() ; }
            catch (IOException e)
            { System.out.println("FileWriter: load cel for export, " + e) ; }
         }
 
	      // Add the palette object entries to the contents vector.

	      i = 0 ;
	      Vector palettes = c.getPalettes() ;
	      while (palettes != null && i < palettes.size())
	      {
				Palette palette = (Palette) palettes.elementAt(i++) ;
	         if (palette.isCopy()) continue ;
            if (!palette.isWritable()) continue ;
            if (mode == SAVE && !palette.isUpdated()) continue ;
            if (mode == COPY && zip != null && zip.isDirectory() && directory != null &&
               directory.equals(zip.getDirectoryName()) && !palette.isUpdated()) continue ;
				ze = (zip == null) ? null : zip.getEntry(palette.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,palette.getName(),null) ;
               palette.setName(ze.getPath()) ;
            }
				if (palette.isUpdated()) ze = palette.getZipEntry() ;
				if (ze == null && palette.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),palette.getName(),zip) ;
				if (ze == null && palette.getZipEntry() == null)
	           	ze = new DirEntry(zip.getDirectoryName(),palette.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = palette.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            
            if (ze instanceof DirEntry && kisszip != null && kisszip.isArchive()) ze.setMethod(1) ;
				ce = new ContentEntry(palette,ze,(mode == EXPORT)) ;
				v.add(ce) ;
			}

			// Add the audio object entries to the contents vector.

	      i = 0 ;
	      Vector sounds = c.getSounds() ;
	      while (sounds != null && i < sounds.size())
	      {
				Audio audio = (Audio) sounds.elementAt(i++) ;
	         if (audio.isCopy()) continue ;
            if (!audio.isWritable()) continue ;
            if (mode == SAVE && !audio.isUpdated()) continue ;
            if (mode == COPY && zip != null && zip.isDirectory() && directory != null &&
               directory.equals(zip.getDirectoryName()) && !audio.isUpdated()) continue ;
				ze = (zip == null) ? null : zip.getEntry(audio.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,audio.getName(),null) ;
               audio.setName(ze.getPath()) ;
            }
				if (audio.isUpdated()) ze = audio.getZipEntry() ;
				if (ze == null && audio.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),audio.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = audio.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            
            if (ze instanceof DirEntry && kisszip != null && kisszip.isArchive()) ze.setMethod(1) ;
				ce = new ContentEntry(audio,ze,(mode == EXPORT)) ;
				v.add(ce) ;
			}

			// Add the video object entries to the contents vector.

	      i = 0 ;
			Vector movies = c.getMovies() ;
			while (movies != null && i < movies.size())
	      {
				Video video = (Video) movies.elementAt(i++) ;
				if (video.isCopy()) continue ;
				if (!video.isWritable()) continue ;
            if (mode == SAVE && !video.isUpdated()) continue ;
            if (mode == COPY && zip != null && zip.isDirectory() && directory != null &&
               directory.equals(zip.getDirectoryName()) && !video.isUpdated()) continue ;
				ze = (zip == null) ? null : zip.getEntry(video.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,video.getName(),null) ;
               video.setName(ze.getPath()) ;
            }
				if (video.isUpdated()) ze = video.getZipEntry() ;
				if (ze == null && video.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),video.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = video.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            
            if (ze instanceof DirEntry && kisszip != null && kisszip.isArchive()) ze.setMethod(1) ;
				ce = new ContentEntry(video,ze,(mode == EXPORT)) ;
				v.add(ce) ;
			}

	      // Add the component cel object entries to the contents vector.
         // These are written as truecolor CEL images if the appropriate 
         // option is set. Otherwise they are never written as they are
         // recreated on set load from the CNF entries.

	      i = 0 ;
	      Vector comps = c.getComponents() ;
	      while (comps != null && i < comps.size())
	      {
				JavaCel cel = (JavaCel) comps.elementAt(i++) ;
				if (cel.isCopy()) continue ;
				if (!cel.isWritable()) continue ;
            if (mode == SAVE && !OptionsDialog.getComponentCel()) continue ;
            if (mode == COPY && zip != null && zip.isDirectory() && directory != null &&
               directory.equals(zip.getDirectoryName()) && !OptionsDialog.getComponentCel()) continue ;
            String name = cel.getName() ;
            int n = name.lastIndexOf('.') ;
            if (n >= 0) name = name.substring(0,n) + ".cel" ;
            
            // We can only write components if they have an image.
            // Input components such as lists and text areas are
            // not writable.
            
            cel.createImage() ;
            if (cel.getImage() == null) continue ;

      		// The KiSS object must be associated with its new output file name
      		// so that it is created properly when written to the destination
      		// output file.  
            
  				ze = new DirEntry(null,name,null) ;
  				ze.setName(name) ;
            ze.setWriting(true) ;
  				ze.setUpdated(true) ;
            cel.setZipEntry(ze) ;
            ce = new ContentEntry(cel,ze,(mode == EXPORT)) ;
				v.add(ce) ;
         }
		}

      // Return the list of writable zip entry objects.  Make sure that
      // any CNF element is the last one in the vector as the CNF is
      // generated on output.  When cels are exported to a new type BMP  
      // images need transparency %c values and this transparency is 
      // identified when the original image is written.

      if (kiss instanceof Configuration && v.size() > 0)
      {
         Object o = v.elementAt(0) ;
         v.remove(0) ;
         v.addElement(o) ;
      }
      return v ;
   }


   // A function to determine if a file exists as a known writable
   // object within our container contents list.  This function returns
   // the index into the contents vector that can be used to establish 
   // the object data.

   private int findKissObject(String filename, Vector contents)
   {
      int i ;
      ContentEntry ce = null ;
   	if (filename == null || contents == null) return -1 ;

      // Confirm that the file is referenced in our Kiss object contents.
      // The filename identifies the file that is to be written.  If this
      // file is not part of our contents we return -1, otherwise we
      // return the index to the Kiss object associated with this file.

     	for (i = 0 ; i < contents.size() ; i++)
      {
      	Object o = contents.elementAt(i) ;
      	if (o instanceof ContentEntry) ce = (ContentEntry) o ;
        	ArchiveEntry ze = (ce == null) ? null : ce.ze ;
         String s = (ze == null) ? "" : ze.getPath() ;
         if (filename.equalsIgnoreCase(s)) break ;
         ce = null ;
      }

      if (ce == null) i = -1 ;
      return i ;
   }


   // Function to compute the total size of all zip entries in the
   // extraction contents vector.  Two types of contents vectors are
   // possible.  The first is a vector of ArchiveEntries from a zip file.
   // The second is a vector of ContentEntries constructed in this class.

   private long getContentSize(Vector contents)
   {
   	long size = 0 ;
   	if (contents == null) return size ;
      for (int i = 0 ; i < contents.size() ; i++)
      {
      	Object o = contents.elementAt(i) ;
         if (o instanceof ArchiveEntry)
	         size += ((ArchiveEntry) o).getSize() ;
         else if (o instanceof ContentEntry)
         {
         	ArchiveEntry ze = ((ContentEntry) o).ze ;
            KissObject kiss = ((ContentEntry) o).kiss ;
            if (ze != null) size += ze.getSize() ;
            else if (kiss != null) size += kiss.getBytes() ;
         }
      }
      return size ;
   }


   // A function to create a temporary file for the data output.  This
   // function returns a File object for a new temporary file created in
   // the specified directory.

   private File createTemporaryFile(String directory)
   {
      File fd = null ;

     	try
      {
      	if (directory == null)
         	fd = File.createTempFile("Kisekae",null) ;
         else
				fd = File.createTempFile("Kisekae",null,new File(directory)) ;
	      if (OptionsDialog.getDebugLoad())
            System.out.println("Save: Create temporary file " + fd.getPath());
         fd.deleteOnExit() ;
      }
      catch (IOException ex)
      {
         error = true ;
         String s = (fd != null) ? fd.getPath() : "" ;
      	ErrorText.setText("Error: " + ex.toString()) ;
         JOptionPane.showMessageDialog(this,
            captions.getString("FileWriteError") +
            "\n" + s + "\n" + ex.toString(),
            captions.getString("FileSaveException"),
            JOptionPane.ERROR_MESSAGE) ;
  		   ex.printStackTrace() ;
      }
      return fd ;
   }


	// A function to rename the temporary file to our new output file.  This
   // function first renames the source file to establish a backup, then
	// renames the temporary file to be the new output file, then optionally
	// deletes the backup file.  The function returns a file object
	// that references the final output file.
	//
	// Video files throw an error when creating a backup file because the data
	// is not cached, the video is initialized, and the source file is in use.

	private File closeTemporaryFile(File fd)
	{
      if (fd == null || error) return null ;
		if (OptionsDialog.getDebugLoad())
			System.out.println("Save: Close output file " + fd.getPath()) ;

		// Create an alternate output file name.  This is used if we cannot
		// cannot rename the saved file to the original source name.  The
      // alternate name extends the file extension.

		int i = source.lastIndexOf(".") ;
		String extension = (i < 0) ? ".new" : source.substring(i).toLowerCase() ;
		File fs = new File(source) ;
		File fsb = new File(source + ".bak") ;
		File fsalt = new File(source + extension) ;
		File fsoriginal = fs ;

      // Delete any previous backup file.

		if (fsb.exists())
      {
      	fsb.delete() ;
	      if (OptionsDialog.getDebugLoad())
				System.out.println("Save: Delete old backup file " + fsb.getPath()) ;
      }

      // Create a source backup file if the source file exists.  The file
      // may not exist if we are creating a new archive file.

      if (fs.exists())
      {
	      if (!fs.renameTo(fsb))
	      {
				alternate = true ;
				String s = "Save: Unable to create backup file " + fsb.getName() ;
	      	ErrorText.setText(s) ;
				System.out.println(s) ;
				System.out.println("Save: File will be saved as " + fsalt.getName()) ;
            String s1 = captions.getString("FileCreateErrorText1") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + fsb.getName() + s1.substring(j1+1) ;
            String s2 = captions.getString("FileCreateErrorText2") ;
            i1 = s2.indexOf('[') ;
            j1 = s2.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s2 = s2.substring(0,i1) + fs.getName() + s2.substring(j1+1) ;
            String s3 = captions.getString("FileCreateErrorText3") ;
            i1 = s3.indexOf('[') ;
            j1 = s3.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s3 = s3.substring(0,i1) + fsalt.getName() + s3.substring(j1+1) ;
  				JOptionPane.showMessageDialog(this,
  					s1 + "\n" + s2 + "\n" + s3,
  					captions.getString("FileSaveException"),
               JOptionPane.INFORMATION_MESSAGE) ;
				fs = fsalt ;
				fsalt.delete() ;
			}
			if (OptionsDialog.getDebugLoad()  && !alternate)
			{
				System.out.println("Save: New backup file name is " + fsb.getPath()) ;
         }
      }

      // Rename the temporary file to be the new output file.

		if (!fd.renameTo(fs))
      {
         error = true ;
			String s = "Save: Unable to rename file "
           	+ fd.getName() + " to " + fs.getName() ;
      	ErrorText.setText(s) ;
  	      System.out.println(s) ;
         String s1 = captions.getString("FileCreateErrorText4") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + fd.getName() + s1.substring(j1+1) ;
         String s2 = captions.getString("FileCreateErrorText2") ;
         i1 = s2.indexOf('[') ;
         j1 = s2.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s2 = s2.substring(0,i1) + fs.getName() + s2.substring(j1+1) ;
  			JOptionPane.showMessageDialog(this,
  	         s1 + "\n" + s2,
            captions.getString("FileSaveException"),
            JOptionPane.ERROR_MESSAGE) ;
			if (!alternate) fsb.renameTo(fs) ;
         return null ;
		}

		// Trace the name of the output file.

		if (OptionsDialog.getDebugLoad())
			System.out.println("Save: New output file is " + fs.getPath()) ;

      // Delete the source backup file.

      if (!OptionsDialog.getBackupFileOn())
      {
			if (fsb.exists() && !alternate)
         {
         	fsb.delete() ;
		      if (OptionsDialog.getDebugLoad())
		         System.out.println("Save: Delete backup file " + fsb.getPath()) ;
         }
		}
		return fsoriginal ;
   }

   // We release references to some of our critical objects.

   private void flush()
   {
   	kiss = null ;
      fileopen = null ;
      kisszip = null ;
      sourcezip = null ;
      setVisible(false) ;
      CANCEL.removeActionListener(this) ;
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


   // Inner class to define a content vector entry object.

   class ContentEntry
   {
   	KissObject kiss = null ;      // The object to write
      ArchiveEntry ze = null ;      // The output file name and type
      boolean export = false ;      // if true, export instead of write

      ContentEntry(KissObject k, ArchiveEntry e, boolean b)
      {
         export = b ;
         kiss = k ;
         ze = e ;
      }
   }
}
