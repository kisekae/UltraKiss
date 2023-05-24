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
* FileSave class
*
* Purpose:
*
* This class manages the file save dialog to provide the user with
* the ability to select the required directory and data set element
* to save the current configuration.
*
* This class saves files to the local file system.
*
*/


import java.awt.* ;
import java.awt.event.* ;
import java.net.URL ;
import java.io.* ;
import java.util.ResourceBundle;
import java.util.Locale;
import javax.swing.* ;
import javax.swing.filechooser.FileFilter ;


final class FileSave
{
	static final int save = 0 ;                  // Save (only updated)
	static final int saveset = 1 ;               // Save As  (everything)
	static final int saveexport = 2 ;
	static final int saveconvert = 3 ;
	private static String dirname = null ;       // Last accessed directory

   // I18N attributes

   private ResourceBundle captions = Kisekae.getCaptions() ;

	// File attributes

	private JFrame parent = null ;  				   // Parent frame
   private KissObject kiss = null ;  			   // KiSS object to save
   private Cel cel = null ;  			   			// Associated cel for palettes
	private String destination = null ;	 		   // Destination path name
	private String source = null ;				   // Source path name
	private String filename = null ;				   // File name
   private String directory = null ;	     	   // Directory name
	private ArchiveFile zip = null ;				   // Zip file object
	private ArchiveEntry ze = null ;				   // Zip entry object
   private ActionListener writelistener = null ; // Listener for write termination
	private int saveoption = 0 ;			   		// Save option

   // File Filters

   private String filefilter = null ;
   private FileFilter allfiles = null ;
   private FileFilter kissarchives = null ;
   private FileFilter palettefiles = null ;
   private FileFilter imagefiles = null ;
   private FileFilter mediafiles = null ;
   private FileFilter textfiles = null ;
   private FileFilter bmpfile = new SimpleFilter("BMP Image",".bmp");
   private FileFilter celfile = new SimpleFilter("CEL Image",".cel");
   private FileFilter giffile = new SimpleFilter("GIF Image",".gif");
   private FileFilter jpgfile = new SimpleFilter("JPG Image",".jpg");
   private FileFilter pngfile = new SimpleFilter("PNG Image",".png");
   private FileFilter ppmfile = new SimpleFilter("PPM Image",".ppm");
   private FileFilter jarfile = new SimpleFilter("JAR Archive",".jar");
   private FileFilter lzhfile = new SimpleFilter("LZH Archive",".lzh");
   private FileFilter zipfile = new SimpleFilter("ZIP Archive",".zip");
   private FileFilter gzipfile = new SimpleFilter("GZIP Archive",".gzip");
   private FileFilter kcffile = new SimpleFilter("KCF Palette",".kcf");
   private FileFilter palfile = new SimpleFilter("PAL Palette",".pal");
   private FileFilter cnffile = new SimpleFilter("CNF Text",".cnf");
   private FileFilter docfile = new SimpleFilter("DOC Text",".cnf");
   private FileFilter htmlfile = new SimpleFilter("HTML Text",".html");
   private FileFilter logfile = new SimpleFilter("LOG Text",".log");
   private FileFilter lstfile = new SimpleFilter("LST Text",".lst");
   private FileFilter m3ufile = new SimpleFilter("M3U Text",".m3u");
   private FileFilter rtffile = new SimpleFilter("RTF Text",".rtf");
   private FileFilter txtfile = new SimpleFilter("TXT Text",".txt");
   private FileFilter aufile = new SimpleFilter("AU Audio",".au");
   private FileFilter midifile = new SimpleFilter("MID Audio",".mid");
   private FileFilter mp3file = new SimpleFilter("MP3 Audio",".mp3");
   private FileFilter wavfile = new SimpleFilter("WAV Audio",".wav");
   private FileFilter avifile = new SimpleFilter("AVI Video",".avi");
   private FileFilter mpgfile = new SimpleFilter("MPG Video",".mpg");


	// Constructor to save a specific KiSS object.

   public FileSave(JFrame f, KissObject k)
   { init(f,k) ; }

	// Constructor to save a palette and associated cel.

   public FileSave(JFrame f, Palette p, Cel c)
   {
      cel = c ;
      init(f,((p != null) ? (KissObject) p : (KissObject) c)) ;
      if (cel == null || !cel.isUpdated()) return ;
      filename = cel.getName() ;
      File file = new File(directory,filename) ;
		destination = file.getPath() ;
		ze = cel.getZipEntry() ;
   }


   // Initialization.

   private void init(JFrame f, KissObject k)
   {
   	parent = f ;
      kiss = k ;
      zip = (k != null) ? k.getZipFile() : null ;
		ze = (k != null) ? k.getZipEntry() : null ;
      if (kiss == null) return ;
      kissarchives = new SimpleFilter(captions.getString("KissArchiveFilter"),ArchiveFile.getKissExt());
      palettefiles = new SimpleFilter(captions.getString("PaletteFilter"),ArchiveFile.getPaletteExt());
      imagefiles = new SimpleFilter(captions.getString("ImageFilter"),ArchiveFile.getImageExt());
      mediafiles = new SimpleFilter(captions.getString("MediaFilter"),ArchiveFile.getMediaExt());
      textfiles = new SimpleFilter(captions.getString("TextFilter"),ArchiveFile.getTextExt());

      // The directory name is the fully qualified path name to the
      // directory in which the KiSS object or archive file resides.

		directory = (zip == null) ? kiss.getDirectory() : zip.getDirectoryName() ;
		if (directory == null) directory = "" ;

      // The source file name is a fully qualified path name to the archive
      // file for KiSS objects extracted from an archive.  For KiSS objects
      // extracted from a directory the source file name is a path name to
		// the actual file element in the directory.

		filename = (zip == null) ? kiss.getName() : zip.getName() ;
		if (ze instanceof DirEntry) filename = ze.getPath() ;
		if (filename == null) filename = "" ;
		source = filename ;

      // The destination file name is a fully qualified path name to the
      // specific KiSS object being saved.  This is the new name for the
      // KiSS object in the parent archive.

		String element = (ze == null) ? kiss.getPath() : ze.getPath() ;
		if (element == null) element = "" ;
		File fd = new File(element) ;
      String name = fd.getName() ;
		fd = new File(directory,name) ;
		destination = (zip instanceof DirFile) ? element : fd.getPath() ;
	}


	// Object utility methods
	// ----------------------

	// Show the file Save As dialog, which is modal.  If we are performing
   // a Save As from our main panel frame then we show the source file name
   // in our dialog, which is either the archive file name for configurations
   // loaded from an archive, or the configuration file element name for
   // configurations loaded from a directory.  If we are performing a Save As
	// from a text, palette, or graphics editor frame, then we show the KiSS
   // object element name in our dialog.

	public void show()
	{
		if (parent == null) parent = new JFrame() ;
      if ("".equals(directory)) directory = dirname ;
      if (directory == null) directory = FileOpen.getDirectory() ;
      if (directory == null) directory = "" ;

      // Use a platform dependent dialog if necessary.  These do not allow
      // for filename filters or multiple selections.

      FileDialog chooser = null ;
      JFileChooser jchooser = null ;
      if (OptionsDialog.getSystemLF())
      {
		   chooser = new FileDialog(parent) ;
         chooser.setMode(FileDialog.SAVE) ;
   		chooser.setDirectory(directory) ;
      }
      else
      {
		   jchooser = new JFileChooser() ;
         jchooser.setLocale(Kisekae.getCurrentLocale()) ;
         jchooser.setDialogType(JFileChooser.SAVE_DIALOG) ;
         jchooser.setCurrentDirectory(new File(directory)) ;
         allfiles = jchooser.getAcceptAllFileFilter() ;
         if (filefilter == null)
         {
            jchooser.addChoosableFileFilter(kissarchives) ;
            jchooser.addChoosableFileFilter(palettefiles) ;
            jchooser.addChoosableFileFilter(imagefiles) ;
            jchooser.addChoosableFileFilter(mediafiles) ;
            jchooser.addChoosableFileFilter(textfiles) ;
         }
         else
         {
            if ("Image".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(bmpfile) ;
               jchooser.addChoosableFileFilter(celfile) ;
               jchooser.addChoosableFileFilter(giffile) ;
               jchooser.addChoosableFileFilter(jpgfile) ;
               jchooser.addChoosableFileFilter(pngfile) ;
               jchooser.addChoosableFileFilter(ppmfile) ;
            }
            else if ("Archive".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(jarfile) ;
               jchooser.addChoosableFileFilter(lzhfile) ;
               jchooser.addChoosableFileFilter(zipfile) ;
            }
            else if ("Palette".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(kcffile) ;
               jchooser.addChoosableFileFilter(palfile) ;
            }
            else if ("Text".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(cnffile) ;
               jchooser.addChoosableFileFilter(docfile) ;
               jchooser.addChoosableFileFilter(htmlfile) ;
               jchooser.addChoosableFileFilter(logfile) ;
               jchooser.addChoosableFileFilter(lstfile) ;
               jchooser.addChoosableFileFilter(m3ufile) ;
               jchooser.addChoosableFileFilter(rtffile) ;
               jchooser.addChoosableFileFilter(txtfile) ;
            }
            else if ("Audio".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(aufile) ;
               jchooser.addChoosableFileFilter(midifile) ;
               jchooser.addChoosableFileFilter(mp3file) ;
               jchooser.addChoosableFileFilter(wavfile) ;
            }
            else if ("Video".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(avifile) ;
               jchooser.addChoosableFileFilter(mpgfile) ;
            }
            else if ("ColorEditor".equalsIgnoreCase(filefilter))
            {
               jchooser.addChoosableFileFilter(bmpfile) ;
               jchooser.addChoosableFileFilter(celfile) ;
               jchooser.addChoosableFileFilter(giffile) ;
               jchooser.addChoosableFileFilter(jpgfile) ;
               jchooser.addChoosableFileFilter(pngfile) ;
               jchooser.addChoosableFileFilter(ppmfile) ;
               jchooser.addChoosableFileFilter(kcffile) ;
               jchooser.addChoosableFileFilter(palfile) ;
            }
         }
      }

		// If we are saving a configuration set from our main panel frame
      // we show the archive file name.

		if (saveoption == saveset)
		{
			String element = (ze == null) ? kiss.getName() : ze.getName() ;
			String zipname = (zip == null) ? null : zip.getFileName() ;
			if (zipname == null || zip.isDirectory())
         {
				if (chooser != null) chooser.setTitle(captions.getString("SaveConfigurationTitle")) ;
				if (jchooser != null) jchooser.setDialogTitle(captions.getString("SaveConfigurationTitle")) ;
         }
			else
			{
				if (chooser != null) chooser.setTitle(captions.getString("SaveKissSetTitle")) ;
				if (jchooser != null) jchooser.setDialogTitle(captions.getString("SaveKissSetTitle")) ;
				element = zipname ;
			}
			if (chooser != null) chooser.setFile(element) ;
			if (jchooser != null) jchooser.setSelectedFile(new File(element)) ;
		}

		// If we are exporting a KiSS object we set the appropriate export
		// extension to the element entry name.

		else if (saveoption == saveexport)
		{
			String name = (ze == null) ? kiss.getName() : ze.getName() ;
			if (name == null) name = "" ;
			int n = name.lastIndexOf(".") ;
			String extension = (n < 0) ? "" : name.substring(n) ;
			if (n > 0 && kiss instanceof Palette) name = name.substring(0,n) + ".pal" ;
			if (chooser != null) chooser.setFile(name) ;
			if (jchooser != null) jchooser.setSelectedFile(new File(name)) ;
			String zipname = (zip == null) ? null : zip.getFileName() ;
			if (zipname == null || zip.isDirectory())
         {
				if (chooser != null) chooser.setTitle(captions.getString("ExportElementTitle")) ;
				if (jchooser != null) jchooser.setDialogTitle(captions.getString("ExportElementTitle")) ;
         }
			else
         {
            String s = captions.getString("ExportToArchiveTitle") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + zipname + s.substring(j1) ;
				if (chooser != null) chooser.setTitle(s) ;
				if (jchooser != null) jchooser.setDialogTitle(s) ;
         }
		}

		// Otherwise we use the element entry name.

		else
      {
			if (ze != null)
         {
   			if (chooser != null) chooser.setFile(ze.getName()) ;
   			if (jchooser != null) jchooser.setSelectedFile(new File(ze.getName())) ;
         }
         else if (filename != null)
         {
            if (chooser != null) chooser.setFile(filename) ;
            if (jchooser != null) jchooser.setSelectedFile(new File(filename)) ;
         }
			String zipname = (zip == null) ? null : zip.getFileName() ;
			if (zipname == null || zip.isDirectory())
         {
				if (chooser != null) chooser.setTitle(captions.getString("SaveElementTitle")) ;
				if (jchooser != null) jchooser.setDialogTitle(captions.getString("SaveElementTitle")) ;
         }
			else
         {
            String s = captions.getString("SaveToArchiveTitle") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + zipname + s.substring(j1) ;
				if (chooser != null) chooser.setTitle(s) ;
				if (jchooser != null) jchooser.setDialogTitle(s) ;
         }
		}

      // Set the initial file name filter.

      if (jchooser != null)
      {
         if (filefilter != null)
         {
            File f = jchooser.getSelectedFile() ;
            String name = (f != null) ? f.getName() : "" ;
            int i = name.indexOf('.') ;
            String ext = (i > 0) ? name.substring(i) : "" ;
            ext = ext.toLowerCase() ;
            if (".bmp".equals(ext)) jchooser.setFileFilter(bmpfile) ;
            else if (".cel".equals(ext)) jchooser.setFileFilter(celfile) ;
            else if (".gif".equals(ext)) jchooser.setFileFilter(giffile) ;
            else if (".png".equals(ext)) jchooser.setFileFilter(pngfile) ;
            else if (".jpg".equals(ext)) jchooser.setFileFilter(jpgfile) ;
            else if (".ppm".equals(ext)) jchooser.setFileFilter(ppmfile) ;
            else if (".kcf".equals(ext)) jchooser.setFileFilter(kcffile) ;
            else if (".pal".equals(ext)) jchooser.setFileFilter(palfile) ;
            else if (".jar".equals(ext)) jchooser.setFileFilter(jarfile) ;
            else if (".lzh".equals(ext)) jchooser.setFileFilter(lzhfile) ;
            else if (".zip".equals(ext)) jchooser.setFileFilter(zipfile) ;
            else if (".gzip".equals(ext)) jchooser.setFileFilter(gzipfile) ;
            else if (".cnf".equals(ext)) jchooser.setFileFilter(cnffile) ;
            else if (".doc".equals(ext)) jchooser.setFileFilter(docfile) ;
            else if (".txt".equals(ext)) jchooser.setFileFilter(txtfile) ;
            else if (".log".equals(ext)) jchooser.setFileFilter(logfile) ;
            else if (".lst".equals(ext)) jchooser.setFileFilter(lstfile) ;
            else if (".m3u".equals(ext)) jchooser.setFileFilter(m3ufile) ;
            else if (".html".equals(ext)) jchooser.setFileFilter(htmlfile) ;
            else if (".au".equals(ext)) jchooser.setFileFilter(aufile) ;
            else if (".mid".equals(ext)) jchooser.setFileFilter(midifile) ;
            else if (".midi".equals(ext)) jchooser.setFileFilter(midifile) ;
            else if (".mp3".equals(ext)) jchooser.setFileFilter(mp3file) ;
            else if (".avi".equals(ext)) jchooser.setFileFilter(avifile) ;
            else if (".mpg".equals(ext)) jchooser.setFileFilter(mpgfile) ;
            else jchooser.setFileFilter(allfiles) ;
         }
         else
            jchooser.setFileFilter(allfiles) ;
      }

      // Show the file save dialog.

      File f = null ;
      int jreturn = 0 ;
		if (chooser != null) chooser.show() ;
		if (jchooser != null) jreturn = jchooser.showSaveDialog(parent) ;

		// Get the selected file name and destination type extension.

		if (chooser != null && chooser.getFile() == null) return ;
		if (jchooser != null && jreturn != JFileChooser.APPROVE_OPTION) return ;
      if (chooser != null)
      {
 			directory = chooser.getDirectory() ;
 			filename = chooser.getFile() ;
 			f = new File(directory,filename) ;
         dirname = directory ;
         chooser.dispose() ;
      }
      if (jchooser != null)
      {
         f = jchooser.getSelectedFile() ;
         directory = f.getParent() ;
         filename = f.getName() ;
         dirname = directory ;
      }
		destination = f.getPath() ;
      
      // Prompt if we are overwriting a file.
      
      if (f.exists())
      {
         String s = captions.getString("FileSaveReplaceFile") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1+1) + filename + s.substring(j1) ;
         int i = JOptionPane.showConfirmDialog(parent, s,
						captions.getString("ReplaceFileText"),
                  JOptionPane.WARNING_MESSAGE) ;
         if (i == JOptionPane.CANCEL_OPTION) return;
      }

      // Check that the destination file is compatible with the source archive.

		if (saveoption == saveset)
		{
			if (zip != null)
			{
				if (zip.isArchive() && !ArchiveFile.isArchive(filename)
					|| !zip.isArchive() && ArchiveFile.isArchive(filename))
				{
               String s = captions.getString("InvalidFileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + filename + s.substring(j1) ;
					if (zip.isArchive())
						s += "\n" + captions.getString("SaveAsArchiveText") ;
					else
						s += "\n" + captions.getString("SaveAsElementText") ;
					JOptionPane.showMessageDialog(parent, s,
						captions.getString("FileSaveException"),
                  JOptionPane.ERROR_MESSAGE) ;
					return ;
				}
			}
		}
		else
		{
			if (ArchiveFile.isArchive(filename))
			{
            String s = captions.getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + filename + s.substring(j1) ;
				s += "\n" + captions.getString("SaveAsElementText") ;
				JOptionPane.showMessageDialog(parent, s,
               captions.getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				return ;
			}
		}

      // If we are saving a palette and there is no associated cel then the
      // output element had better be a palette type of file.  If we have
      // a cel then we need a palette or image type.

		if (kiss instanceof Palette)
		{
	      if (cel == null && !ArchiveFile.isPalette(destination))
			{
            String s = captions.getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + filename + s.substring(j1) ;
				s += "\n" + captions.getString("SaveAsPaletteText") ;
				JOptionPane.showMessageDialog(parent, s,
               captions.getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				return ;
			}
	      if (cel != null && !(ArchiveFile.isPalette(destination)
           	|| ArchiveFile.isImage(destination)))
			{
            String s = captions.getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + filename + s.substring(j1) ;
				s += "\n" + captions.getString("SaveAsPaletteImageText") ;
				JOptionPane.showMessageDialog(parent, s,
               captions.getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				return ;
			}
      }

      // If we are saving a cel then the output element had better be an
      // image or palette type of file.

		if (kiss instanceof Cel)
		{
	      if (!(ArchiveFile.isPalette(destination) || ArchiveFile.isImage(destination)))
			{
            String s = captions.getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + filename + s.substring(j1) ;
            s += "\n" + captions.getString("SaveAsPaletteImageText") ;
				JOptionPane.showMessageDialog(parent, s,
               captions.getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				return ;
			}
      }

      // Perform the file IO in a worker thread to do the writes
      // in a background mode.

      save() ;
   }


	// Method to show a Save As dialog, but indicate that all modified
	// files in the data set should be written to the output archive.

	public void showall() { saveoption = saveset ; show() ; }


	// Method to show a Save As dialog, but indicate that files in the data set
	// should be exported to the output archive.

	public void showexport() { saveoption = saveexport ; show() ; }


	// Method to invoke a direct file conversion without going through
   // a dialog.

	public void saveconvert() { saveoption = saveconvert ; save() ; }


   // Method to add a new ActionListener to be notified on writes.

   public void addWriteListener(ActionListener al) { writelistener = al ; }


   // Set the default file filter.

   void setFileFilter(String s) { filefilter = s ; }


   // Method to set a specific destination without going through a dialog.

   public void setDestination(String dir, String name)
   {
      directory = dir ;
      filename = name ;
		File f = new File(directory,filename) ;
		destination = f.getPath() ;
   }


   // The save method initiates the file output to the named destination
   // file.  The source object is identified through the frame passed
   // when this object was created.  The output file is identified
   // by the destination path established when the file dialog was closed.

	void save()
   {
   	if (destination == null) return ;
      if (cel != null && ArchiveFile.isImage(destination)) kiss = cel ;
		FileWriter w = new FileWriter(parent,kiss,source,destination,saveoption) ;
		if (parent instanceof ActionListener)
			w.callback.addActionListener((ActionListener) parent) ;
		if (writelistener != null)
      	w.callback.addActionListener(writelistener) ;
		Thread thread = new Thread(w) ;
      thread.start() ;
   }


   // Method to initiate file output, but indicate that all modified files
   // in the data set should be written to the output archive.  For the
   // FileWriter to save new elements to an archive the destination must
   // be an archive file name.

	void saveall()
   {
   	saveoption = save ;
		String element = (ze == null) ? kiss.getName() : ze.getName() ;
		String zipname = (zip == null) ? null : zip.getFileName() ;
		if (!(zipname == null || zip.isDirectory())) element = zipname ;
		File f = new File(directory,element) ;
		destination = f.getPath() ;
      save() ;
   }
}
