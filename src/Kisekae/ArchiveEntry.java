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
* ArchiveEntry class
*
* Purpose:
*
* This is an abstract class for KiSS archive element types.  Each element
* in a KiSS data set is described by an archive entry.  The archive entry
* defines the element characteristics such as compression type, file location
* and so on.  Every archive entry is related back to an archive file.
*
* An archive entry is an object that manages one file element inside an archive
* file.  The archive file is either a compressed file or a file directory.
*
* This object manages the state and contents of the file element.
*
*/


import java.util.*;
import java.io.*;


abstract class ArchiveEntry
	implements Cloneable, Comparable
{
	protected String filename = null ;			// Entry file name (unqualified)
	protected String dirname = null ;			// Entry directory name
	protected String pathname = null ;			// Entry path name (qualified)
	protected ArchiveFile archive = null ;		// Parent archive file object
	protected MemFile memfile = null ;        // Memory file for this archive entry
	protected Object userobject = null ;		// Optional user object
   protected Vector updated = null ;			// True if file has changed
   protected boolean copy = false ;				// True if Kiss object is a copy
   private boolean savedir = false ;			// True if save extra file info
   private boolean savereldir = false ;	  	// True if save relative info
   private boolean checkdate = false ;	  		// True if not overwrite new
   private boolean imported = false ;	  		// True if imported image
   private boolean writing = false ;         // True if FileWriter writing

   // Constructor
   
   public ArchiveEntry()
   {
      updated = new Vector() ;
   }

	// Object utility methods
	// ----------------------

	// Return the requested element name.

	public String getName() { return filename ; }

	// Return the requested path name.

	public String getPath() { return pathname ; }

   // Returns the full path name of the file.
   
	public String getPathName() { return pathname ; }

	// Return the requested path name.

	public String getDirectory() { return dirname ; }

	// Return the element extension.

	public String getExtension()
	{
		int i = filename.lastIndexOf(".") ;
		String extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;
		return extension ;
	}

	// Set the archive element name.

	void setName(String name)
   {
		filename = convertSeparator(name) ;
      if (dirname == null) return ;
      File f = new File(dirname,filename) ;
      pathname = f.getPath() ;
   }


	// Set the archive element path name.

	void setPath(String path)
   {
   	pathname = convertSeparator(path) ;
      if (path == null) return ;
      File f = new File(path) ;
      filename = f.getName() ;
      dirname = f.getParent() ;
   }


	// Set the archive element directory name.

	void setDirectory(String dir)
   {
   	dirname = convertSeparator(dir) ;
      if (filename == null) return ;
      File f = new File(dirname,filename) ;
      pathname = f.getPath() ;
   }


   // A function to convert file separator characters.

   String convertSeparator(String s)
   {
      if (s == null) return null ;
      s = s.replace('/',File.separatorChar) ;
      s = s.replace('\\',File.separatorChar) ;
      return s ;
   }


	// Set if the associated KiSS object is a copy of another object.

	void setCopy(boolean b) { copy = b ; }


	// Set if we must save the absolute path when writing an archive file.

	void setSaveDir(boolean b) { savedir = b ; }


	// Set if we must save the relative path when writing an archive file.

	void setSaveRelDir(boolean b) { savereldir = b ; }


	// Set if we must check the file date and time when writing an archive file.

	void setCheckDate(boolean b) { checkdate = b ; }


	// Set if we have imported an image file.

	void setImported(boolean b) { imported = b ; }


	// Set the optional user object to be associated with this archive entry.

	void setUserObject(Object o) { userobject = o ; }


	// Set the FileWriter writing state.

	void setWriting(boolean b) { writing = b ; }


	// Return true if we must save the path when writing an archive file.

   boolean getSaveDir() { return (savedir || savereldir) ; }


	// Return true if we must save the relative path when writing an archive file.

   boolean getSaveRelDir() { return savereldir ; }


	// Return true if we must not overwrite new files when writing an archive file.

   boolean getCheckDate() { return checkdate ; }

   
   // Return the memory file.
   
   MemFile getMemoryFile() { return memfile ; }

   
   // Set the memory file.
   
   void setMemoryFile(MemFile mf) { memfile = mf ; }


	// Return true if we have imported this entry.

   boolean isImported() { return imported ; }


	// Return true if we have a writing state set.

   boolean isWriting() { return writing ; }


	// Return true if we have imported this entry.

   boolean isMemoryFile() { return memfile != null ; }


	// Return the FileOpen object for this archive element.

	FileOpen getFileOpen() { return (archive == null) ? null : archive.getFileOpen() ; }

   
	// Returns an input stream for reading the uncompressed contents of
	// the specified file entry.
   
	public InputStream getInputStream() throws IOException 
   {  
      if (isMemoryFile()) return memfile.getInputStream() ;
      return null ; 
   }

   
	// Returns an output stream for writing the uncompressed contents of
	// the specified file entry.
   
	public OutputStream getOutputStream() throws IOException { return null ; }


	// Return the size of the file.

	long getSize() { return -1 ; }

	// Return the compressed size of the file.

	long getCompressedSize() { return -1 ; }

	// Return the CRC32 for the uncompressed file.

	long getCrc32() { return -1 ; }

	// Return the time of the file creation.

	long getTime() { return -1 ; }

	// Set the time of the file creation.

	void setTime(long time) { return ; }

	// Return the compression method.

	int getMethod() { return -1 ; }

	// Set the compression method.

	void setMethod(int method) { return ; }

	// Set the parent archive file.

   void setZipFile(ArchiveFile af) { archive = af ; }

   // Return the parent archive file.

   ArchiveFile getZipFile() { return archive ; }

   // Return the user object associated with this archive entry.

   Object getUserObject() { return userobject ; }

   // Method to retrieve the update state. We keep this in a Vector
   // container so that there is a common update boolean for all 
   // cloned copies.

	boolean isUpdated() 
   { 
      if (updated.size() == 0) return false ;
      if (!(updated.elementAt(0) instanceof Boolean)) return false ;
      return ((Boolean) updated.elementAt(0)).booleanValue() ; 
   }

   // Method to set the update state.

	void setUpdated(boolean b) 
   { 
      updated.removeAllElements() ;
      updated.addElement(new Boolean(b)) ; 
   }

   // Method to determine if the element is an audio file.

   boolean isAudio() { return ArchiveFile.isAudio(filename) ; }

   // Method to determine if the element is an audio file.

	boolean isAudioSound() { return ArchiveFile.isAudioSound(filename) ; }

   // Method to determine if the element is an audio file.

	boolean isAudioMedia() { return ArchiveFile.isAudioMedia(filename) ; }

   // Method to determine if the element is a video file.

   boolean isVideo() { return ArchiveFile.isVideo(filename) ; }

   // Method to determine if the element is a video file.

   boolean isImage() { return ArchiveFile.isImage(filename) ; }

   // Method to determine if the element is a compressed file.

   boolean isArchive() { return ArchiveFile.isArchive(filename) ; }

   // Method to determine if the element is a directory file.

   boolean isDirectory() { return ArchiveFile.isDirectory(filename) ; }

   // Method to determine if the element is a configuration element.

   boolean isConfiguration() { return ArchiveFile.isConfiguration(filename) ; }

   // Method to determine if the element is a text element.

   boolean isText() { return ArchiveFile.isText(filename) ; }

   // Method to determine if the element is a component.

   boolean isComponent() { return ArchiveFile.isComponent(filename) ; }

   // Method to determine if the element is a rich text element.

   boolean isRichText() { return ArchiveFile.isRichText(filename) ; }

   // Method to determine if the element is an HTML text element.

   boolean isHTMLText() { return ArchiveFile.isHTMLText(filename) ; }

   // Method to determine if the element is a styled text element.

   boolean isStyledText() { return ArchiveFile.isStyledText(filename) ; }

   // Method to determine if the element is a list element.

   boolean isList() { return ArchiveFile.isList(filename) ; }

	// Method to determine if the element is a palette element.

	boolean isPalette() { return ArchiveFile.isPalette(filename) ; }

	// Method to determine if the element is a cel element.

	boolean isCel() { return ArchiveFile.isCel(filename) ; }

	// Method to determine if the element has a palette.

	boolean hasPalette() { return ArchiveFile.hasPalette(filename) ; }

	// Method to determine if the element is compressed.

	boolean isCompressed() { return false ; }

	// Shallow clone.  This creates a new object where all object references
   // are the same references as found in the original object.

   public Object clone()
   {
	   try { return super.clone() ; }
      catch (CloneNotSupportedException e)
      {
      	e.printStackTrace();
         return null ;
      }
   }


	// Required comparison method for the Comparable interface.  We compare
	// the toString representations of the objects.  This routine returns
	// 0 if the objects are lexographically equal, a number less than 0 if
	// the object is lexographically less than this object, and a number
	// greater than 0 if the object is lexographically greater than this
	// object.

	public int compareTo(Object o)
   {
		if (!(o instanceof ArchiveEntry)) return -1 ;
		String s1 = o.toString() ;
		String s2 = this.toString() ;
		if (s1 == null) return -1 ;
		return (-(s1.compareTo(s2))) ;
	}


	// The toString method returns a string representation of this object.
	// This is the name of the archive entry.

	public String toString()
	{ return (filename == null) ? "Unknown" : filename.toUpperCase() ; }
}


