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
* ArchiveFile class
*
* Purpose:
*
* This class is an abstract class for KiSS data archive files.  Extensions
* of this class define specific archive file types.  These may be ZIP files,
* LHA files, JAR files, or even uncompressed directory files.
*
* An archive file is an object that manages a file container.  This is either
* a compressed file or a file directory.  The container has a name and it
* contains many file elements.
*
* This object manages the state and contents of the file container.
*
*/


import java.util.*;
import java.io.*;


abstract class ArchiveFile
{
	protected String pathname = null ;			// Archive file path name
	protected String setname = null ;			// Archive file original name
   protected FileOpen fileopen = null ;		// FileOpen for this archive
	protected Vector contents = null ;			// File contents list
   protected MemFile memfile = null ;        // Memory file for URL input
   protected long size = 0 ;						// Total uncompressed bytes
   protected long compressedsize = 0 ;			// Total compressed bytes
	protected int connections = 0 ;				// Media file use count
	protected int opencount = 0 ;					// File open count
   protected Hashtable key = new Hashtable(200,0.855f) ; // Entry index


	// Object utility methods
	// ----------------------

	// Return the fully qualified archive file path name.

	String getName() { return pathname ; }

	// Return the original archive file set name.

	String getOriginalName() { return setname ; }

	// Set the fully qualified archive file path name.

	void setName(String name) { pathname = name ; }

	// Set the original archive file set name.

	void setOriginalName(String name) { setname = name ; }

	// Return the unqualified archive file name.  This is the element name
   // for archive files and the directory name for non-archive files.

	String getFileName()
   {
   	if (pathname == null) return null ;
   	if (isArchive())
      {
	      File f = new File(pathname) ;
	   	return f.getName() ;
      }
      return pathname ;
   }

	// Return the unqualified archive directory name.

	String getDirectoryName()
   {
   	if (pathname == null) return null ;
   	if (isArchive())
      {
	      File f = new File(pathname) ;
	   	return f.getParent() ;
      }
      return pathname ;
   }

	// Return the archive file path.

	String getPath() { return pathname ; }

	// Return the FileOpen object for this archive.

	FileOpen getFileOpen() { return fileopen ; }

	// Return the archive contents vector.

	Vector getContents() { return contents ; }

	// Return the memory file.

	MemFile getMemFile() { return memfile; }

	// Return the total uncompressed size of the file.

	long getSize() { return size ; }

	// Return the total compressed size of the file.

	long getCompressedSize() { return compressedsize ; }

	// Method to retrieve the archive update state.  All entries in the
	// archive are checked.

	boolean isUpdated()
	{
		if (contents == null) return false ;
		for (int i = 0 ; i < contents.size() ; i++)
		{
			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
			if (h.isUpdated())
         {
            return true ;
         }
		}
		return false ;
	}

	// Method to retrieve a list of the archive updated elements.

	Vector getUpdated()
	{
   	Vector v = new Vector() ;
		if (contents == null) return v ;
		for (int i = 0 ; i < contents.size() ; i++)
		{
			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
			if (h.isUpdated()) v.addElement(h) ;
		}
		return v ;
	}

	// Method to clear the archive update state.  All entries in the
	// archive are cleared.

	void clearUpdated()
	{
		if (contents == null) return ;
		for (int i = 0 ; i < contents.size() ; i++)
		{
			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
			h.setUpdated(false) ;
		}
	}

	// Method to set an archive entry update state.  The archive file
   // contents may not reference the active archive entry for a KiSS
   // object.  This function locates any inactive content entry and
   // sets its update state.

	void setUpdated(ArchiveEntry ze, boolean update)
   {
   	if (ze == null) return ;
		if (contents == null) return ;
		for (int i = 0 ; i < contents.size() ; i++)
		{
			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
         if (!h.getName().equalsIgnoreCase(ze.getName())) continue ;
			h.setUpdated(update) ;
         break ;
		}
	}

	// Set the FileOpen object for this archive.

	void setFileOpen(FileOpen f) { fileopen = f ; }

   // Method to determine if the archive is a compressed file.

	boolean isArchive() { return isArchive(pathname) ; }

   // A function to determine if the extension is for a compressed file.

   static boolean isArchive(String name)
   {
   	if (name == null) return false ;
      name = name.replaceFirst("[\\#\\?].*$","") ;  // no query or ref
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".gzip".equals(ext)) return true ;
		if (".zip".equals(ext)) return true ;
		if (".jar".equals(ext)) return true ;
		if (".lzh".equals(ext)) return true ;
		return false ;
	}

   // A function to determine if the extension is for a configuration file.

   static boolean isConfiguration(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
   	return (".cnf".equals(ext)) ;
   }

	// A function to determine if the extension is for a palette file.

	static boolean isPalette(String name)
	{
		if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".kcf".equals(ext)) return true ;
		if (".pal".equals(ext)) return true ;
		return false ;
	}

	// A function to determine if the extension is for a cel file.

	static boolean isCel(String name)
	{
		if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".cel".equals(ext)) return true ;
		return false ;
	}

	// A function to determine if the extension is for a component.

	static boolean isComponent(String name)
	{
		if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
      if (".label".equals(ext)) return true ;
      if (".textbox".equals(ext)) return true ;
      if (".textarea".equals(ext)) return true ;
      if (".textpane".equals(ext)) return true ;
      if (".textfield".equals(ext)) return true ;
      if (".passwordfield".equals(ext)) return true ;
      if (".button".equals(ext)) return true ;
      if (".togglebutton".equals(ext)) return true ;
      if (".checkbox".equals(ext)) return true ;
      if (".radiobutton".equals(ext)) return true ;
      if (".combobox".equals(ext)) return true ;
      if (".list".equals(ext)) return true ;
      if (".menuitem".equals(ext)) return true ;
      if (".menuseparator".equals(ext)) return true ;
      if (".checkboxmenuitem".equals(ext)) return true ;
		return false ;
	}

	// A function to determine if the extension is for an image file.

	static boolean isImage(String name)
	{
		if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".cel".equals(ext)) return true ;
		if (".gif".equals(ext)) return true ;
		if (".jpg".equals(ext)) return true ;
      if (".png".equals(ext)) return true ;
		if (".bmp".equals(ext)) return true ;
		if (".ppm".equals(ext)) return true ;
		if (".pbm".equals(ext)) return true ;
		if (".pgm".equals(ext)) return true ;
		return false ;
	}

   // A function to determine if the extension is for a text element.

   static boolean isText(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
   	if (".txt".equals(ext)) return true ;
      if (".cnf".equals(ext)) return true ;
      if (".log".equals(ext)) return true ;
   	if (".doc".equals(ext)) return true ;
		if (".pal".equals(ext)) return true ;
		return false ;
   }

   // A function to determine if the extension is for a rich text element.

   static boolean isRichText(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
      if (".rtf".equals(ext)) return true ;
      return false ;
   }

   // A function to determine if the extension is for an HTML text element.

   static boolean isHTMLText(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".htm".equals(ext)) return true ;
		if (".html".equals(ext)) return true ;
      return false ;
   }

   // A function to determine if the extension is for styled text.

   static boolean isStyledText(String name)
   {
   	if (name == null) return false ;
      return (isRichText(name) || isHTMLText(name)) ;
   }

   // A function to determine if the extension is for a playlist element.

   static boolean isList(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
      if (".lst".equals(ext)) return true ;
      if (".m3u".equals(ext)) return true ;
		return false ;
   }

   // A function to determine if the extension is for an audio element.

   static boolean isAudio(String name)
	{
		if (name == null) return false ;
		if (isAudioSound(name)) return true ;
		return (isAudioMedia(name)) ;
	}

   // A function to determine if the extension is for a Java Sound audio element.

	static boolean isAudioSound(String name)
   {
		if (name == null) return false ;
      if (!OptionsDialog.getJavaSound()) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if ("".equals(name)) return true ;
      if (!OptionsDialog.getJavaSound()) return false ;
      if (".mid".equals(ext)) return true ;
      if (".midi".equals(ext)) return true ;
		if (".rmf".equals(ext)) return true ;
		if (".au".equals(ext)) return true ;
      if (".wav".equals(ext)) return true ;
		if (".aif".equals(ext)) return true ;
		if (".aiff".equals(ext)) return true ;
		if (".mp3".equals(ext)) return true ;
		return false ;
   }

   // A function to determine if the extension is for a Java Sound audio element.

	static boolean isMP3Sound(String name)
   {
		if (name == null) return false ;
      if (!OptionsDialog.getJavaSound()) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".mp3".equals(ext)) return true ;
      return false ;
   }

	// A function to determine if the extension is for a JMF audio element.

	static boolean isAudioMedia(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
      if (!Kisekae.isMediaInstalled()) return false ;
//		if (".mp3".equals(ext)) return true ;  // JMF implementation
      if (OptionsDialog.getJavaSound()) return false ;
		if (".mp3".equals(ext)) return true ;
		if (".au".equals(ext)) return true ;
      if (".wav".equals(ext)) return true ;
		if (".aif".equals(ext)) return true ;
		if (".aiff".equals(ext)) return true ;
      if (".mid".equals(ext)) return true ;
      if (".midi".equals(ext)) return true ;
		if (".rmf".equals(ext)) return true ;
		if ("".equals(name)) return true ;
		return false ;
   }

   // A function to determine if the extension is for a video element.

   static boolean isVideo(String name)
   {
   	if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
      if (".avi".equals(ext)) return true ;
      if (".mpg".equals(ext)) return true ;
      if (".mpeg".equals(ext)) return true ;
      if (".mpv".equals(ext)) return true ;
      if (".mov".equals(ext)) return true ;
      if (".viv".equals(ext)) return true ;
      if (".swf".equals(ext)) return true ;
      if (".spl".equals(ext)) return true ;
      return false ;
   }

	// Method to determine if the archive is a directory file.

	boolean isDirectory() { return isDirectory(pathname) ; }

   // A function to determine if the extension is for a directory.

   static boolean isDirectory(String name)
   {
		if (name == null) return false ;
   	return (!isArchive(name)) ;
	}

	// A function to determine if the extension has a palette.

	static boolean hasPalette(String name)
	{
		if (name == null) return false ;
		int n = name.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".kcf".equals(ext)) return true ;
		if (".pal".equals(ext)) return true ;
		if (".gif".equals(ext)) return true ;
		if (".bmp".equals(ext)) return true ;
      if (".png".equals(ext)) return true ;
		return false ;
	}

   // A function to return the known text file extensions.

   static String [] getTextExt()
   {
		String [] ext = new String [] { ".TXT", ".DOC", ".RTF", ".LOG", ".CNF",
      	".PAL", ".LST", ".M3U", ".HTM", ".HTML" } ;
      return ext ;
   }

   // A function to return the known text file extensions.

   static String [] getDocExt()
   {
      String [] ext = new String [] { ".TXT", ".DOC", ".RTF",
         ".LST", ".M3U", ".HTM", ".HTML" } ;
      return ext ;
   }

   // A function to return the known media playlist file extensions.

   static String [] getListExt()
   {
		String [] ext = new String [] { ".LST", ".M3U" } ;
      return ext ;
   }

   // A function to return the known palette file extensions.

   static String [] getPaletteExt()
   {
		String [] ext = new String [] { ".KCF", ".PAL" } ;
      return ext ;
   }

	// A function to return the known archive file extensions.

   static String [] getArchiveExt()
   {
		String [] ext = new String [] { ".LZH", ".ZIP", ".GZIP", ".JAR" } ;
      return ext ;
	}

	// A function to return the known KiSS archive file extensions.

   static String [] getKissExt()
   {
		String [] ext = new String [] { ".CNF", ".LZH", ".ZIP" } ;
      return ext ;
	}

	// A function to return the configuration extension.

   static String [] getConfigurationExt()
   {
		String [] ext = new String [] { ".CNF" } ;
      return ext ;
	}

	// A function to return the known media file extensions.

	static String [] getMediaExt()
   {
		String [] ext = new String [] { ".AU", ".WAV", ".MID", ".MIDI", ".MP3",
			".AIFF", ".RMF", ".AVI", ".MPG", ".MPEG", ".MOV", ".VIV" } ;
		return ext ;
   }

	// A function to return the known media file extensions.

	static String [] getMediaListExt()
   {
		String [] ext = new String [] { ".AU", ".WAV", ".MID", ".MIDI", ".MP3",
			".AIFF", ".RMF", ".AVI", ".MPG", ".MPEG", ".MOV", ".VIV", ".LST", ".M3U" } ;
		return ext ;
   }

	// A function to return the known audio file extensions.

	static String [] getAudioExt()
   {
		String [] ext = new String [] { ".AU", ".WAV", ".MID", ".MIDI", ".MP3",
			".AIFF", ".RMF"} ;
		return ext ;
   }

	// A function to return the known video file extensions.

	static String [] getVideoExt()
   {
		String [] ext = new String [] { ".AVI", ".MPG", ".MPEG", ".MOV", ".VIV" } ;
		return ext ;
   }

	// A function to return the known image file extensions.

	static String [] getImageExt()
   {
		String [] ext = new String [] { ".CEL", ".GIF", ".JPG", ".BMP", ".PPM",
			".PGM", ".PBM", ".PNG" } ;
		return ext ;
   }


	// A function to increment/decrement the file use count.   The connection
	// count determines whether the file can be closed on a close request.

	void connect() { connections++ ; }
	void disconnect() { connections-- ; }


	// Returns the archive file entry for the specified name, or null.
	// The archive file entry represents one element in the archive file
	// or the file directory.  We can search on the filename only in case
   // the archive file had directory entries and the pathname search fails.

	ArchiveEntry getEntry(String pathname) { return getEntry(pathname,false) ; }
	ArchiveEntry getEntry(String pathname ,boolean nameonly)
	{
		if (pathname == null) return null ;
      if (contents == null) return null ;

      // This code was inserted to fix duplicate entries being saved to
      // an lzh file if an image was inserted, saved, cut, saved, inserted
      // again, and saved.  The endsWith logic fails if cels have similar
      // names as exist in the FK4testset.

//		String path = pathname.toUpperCase() ;
//		for (int i = 0 ; i < contents.size() ; i++)
//		{
//			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
//			String archive = h.getPath() ;
//			if (archive == null) continue ;
//			archive = archive.toUpperCase() ;
//			if (path.endsWith(archive)) return (h) ;
//		}

      // Watch for Unix and Microsoft file separators.

      String s = pathname ;
      s = s.replace('/',File.separatorChar) ;
      s = s.replace('\\',File.separatorChar) ;

      // Find the file in the archive contents.  If the entry exists in our
      // hash table, return it.  Otherwise search the contents vector.  
      // Our archive entry can have path information.  An expansion set may not.
      // We allow for a search on filename only.

      Object o = key.get(s.toLowerCase()) ;
      if (o instanceof ArchiveEntry) return (ArchiveEntry) o ;
 		for (int i = 0 ; i < contents.size() ; i++)
 		{
 			ArchiveEntry h = (ArchiveEntry) contents.elementAt(i) ;
         String path = h.getPath() ;
         if (nameonly) 
         {
            int n = path.lastIndexOf(File.separatorChar) ;
            if (n >= 0) path = path.substring(n+1) ;
            n = s.lastIndexOf(File.separatorChar) ;
            if (n >= 0) s = s.substring(n+1) ;
         }
			if (s.equalsIgnoreCase(path)) return (h) ;
		}

      // If we were searching in an archive file, return a no find result.

		if (!(this instanceof DirFile)) return null ;

		// We may be looking for a file in a subdirectory.  Directory archives
		// have entries for top level files only.  If a subdirectory file exists
      // we can use it.  Ignore case on filenames.

      File f = new File(s) ;
		if (!f.exists() && f.getParent() != null) 
      {
         int i = 0 ;
         File file = new File(f.getParent()) ;
         File [] files = file.listFiles() ;
         if (files == null) return null ;
         for (i = 0 ; i < files.length ; i++)
         {
            File f1 = files[i] ;
            if (f1.isDirectory()) continue ;
            String s1 = f1.getPath() ;
            if (s1.equalsIgnoreCase(s)) 
            {
               f = f1 ;
               break ;
            }
         }
         if (i >= files.length) return null ;
      }
      
      // If no parent directory then not found.  If we searched in a 
      // parent directory then we must have found it at some index.
      
      if (f.getParent() == null) return null ;
		DirEntry h = new DirEntry(f.getParent(),f.getName(),this);
		contents.addElement(h) ;
      return h ;
	}



	// Returns the archive file entry for the specified directory and name.

	ArchiveEntry getEntry(String directory, String name)
	{
   	if (directory == null || name == null) return null ;
      File f = new File(directory,name) ;
      ArchiveEntry ae = getEntry(f.getPath()) ;
      return ae ;
   }


	// Returns a list of all entries of the required type, or null.
	// The archive file entry represents one element in the archive file
	// or the file directory. ArchiveEntry elements are returned.

	Vector getEntryType(String extension) 
	{
		if (extension == null) return null ;
      if (contents == null) return null ;
      Vector files = new Vector() ;
      
      Enumeration e = entries() ;
      while (e.hasMoreElements())
      {
         Object o = e.nextElement() ;
         String s = o.toString() ;
         int n = s.lastIndexOf('.') ;
         if (n < 0) continue ;
         String ext = s.substring(n) ;
         if (!extension.equalsIgnoreCase(ext)) continue ;
         files.add(o) ;
      }
      return files ;
   }


	// Returns an enumeration of the archive file entries.  The enumeration
	// can be used to sequentially scan all elements of the directory.

	Enumeration entries()
	{ return (contents == null) ? null : contents.elements() ; }


	// Determines if the archive contains a specified file type.
   // For directory archives we need to verify that the file
   // actually exists.

	boolean containsFileType(String [] ext)
   {
      Enumeration enum1 = entries() ;
      if (enum1 == null || ext == null) return false ;
      while(enum1.hasMoreElements())
      {
         Object o = enum1.nextElement() ;
         if (!(o instanceof ArchiveEntry)) continue ;
         ArchiveEntry ze = (ArchiveEntry) o ;
         String s = o.toString() ;
         int n = s.indexOf('.') ;
         if (n < 0) continue ;
         
         // Check for existence of entry.
         
         for (int i = 0 ; i < ext.length ; i++)
         {
            if (s.substring(n).equalsIgnoreCase(ext[i])) 
            {
               if (!(ze instanceof DirEntry)) return true ;
               try
               {
                  File f = new File(ze.getPath()) ;
                  if (f.exists()) return true ;
               }
               catch (Exception e) { }
            }
         }
      }
      return false ;
   }


   // This function adds an entry to the archive file contents.  For
   // directory files the archive entry directory name is set to match
   // this archive file directory name as this entry can be written as
   // a new element in this archive file.  Entries are added only if they
   // do not currently exist.  Added entries are retained in the hash key
   // table for getEntry search optimization.

   void addEntry(ArchiveEntry ze)
   {
   	if (ze == null) return ;
   	if (contents == null) init() ;
      String s = ze.getPath() ;
      if (s == null) return ;
      if (contents.contains(ze)) return ;
      Object o = key.get(s.toLowerCase()) ;
      if (o != null) return ;
      if (this instanceof DirFile) ze.setDirectory(getDirectoryName()) ;
      contents.addElement(ze) ;
      key.put(s.toLowerCase(),ze) ;
   }


	// Utility function to sort the contents vector entries.

	void sortEntries() { if (contents != null) Collections.sort(contents) ; }


   // Initialize the archive contents.

   void init()
   {
      contents = new Vector() ;
      key = new Hashtable(200,0.855f) ;
   }


	// Abstract class methods
	// ----------------------


	// Returns an input stream for reading the uncompressed contents of
	// the specified file entry.

	abstract InputStream getInputStream(ArchiveEntry e) throws IOException ;

	// Returns an output stream for writing the uncompressed contents of
	// the specified file entry.

	abstract OutputStream getOutputStream(ArchiveEntry e) throws IOException ;

	// Close the archive file contents.

	abstract void close() throws IOException ;

   // Flush the archive file contents.

	abstract void flush() ;

	// Open the archive file contents.

	abstract void open() throws IOException ;

   // Return the file open state.

   abstract boolean isOpen() ;


   // The toString method returns a string representation of this object.
   // This is the name of the archive file path.

   public String toString()
	{ return (pathname == null) ? "Unknown" : pathname ; }
}

