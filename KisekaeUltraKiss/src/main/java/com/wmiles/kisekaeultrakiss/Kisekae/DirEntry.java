package com.wmiles.kisekaeultrakiss.Kisekae ;

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
* DirEntry class
*
* Purpose:  
* 
* Objects of this class are directory file elements.  
*
*/

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.CheckedInputStream;


final class DirEntry extends ArchiveEntry
{
	static final public int UNCOMPRESSED = 0 ;  // Compression method code
	static final public int COMPRESSED = 1 ;    // Compression method code

	private File file = null ;					// File object
   private int method = 0 ;					// Compression method
	private long crc32 = -1 ;				  	// 32 bit CRC of uncompressed file
   private long filesize = -1 ;           // uncompressed size of file
   private long lastmodified = 0 ;        // last modified time of file

	// Constructor

	public DirEntry(String directory, String name, ArchiveFile af)
	{
      archive = af ;
		filename = convertSeparator(name) ;
      dirname = convertSeparator(directory) ;
		if (filename == null) return ;
		if (directory == null) directory = "" ;
		file = new File(directory,filename) ;
      pathname = file.getPath() ;
	}

   // Constructor to encalsulate a memory file
   
	public DirEntry(MemFile mem)
	{ 
      archive = null ;
      dirname = null ;
      filename = null ;
		if (mem == null) return ;
		memfile = mem ;
      filename = memfile.getFileName() ;
      pathname = memfile.getFileName() ;      
   }

	public DirEntry(String name)
	{ this(null,name,null) ; }


	// Return an input stream for this file element.

	public InputStream getInputStream() throws IOException
	{
      if (memfile != null) return memfile.getInputStream() ;
   	InputStream is = null ;
      File streamsource = file ;
      if (isImported() && importpath != null) 
         streamsource = new File(importpath) ;
   	if (streamsource == null) return null ;
      try { is = new FileInputStream(streamsource) ; }
      catch ( FileNotFoundException e) { return null ; }
   	return new BufferedInputStream(is,4096) ;
   }


	// Return an output stream for this file element.

	public OutputStream getOutputStream() throws IOException
	{
   	if (file == null) return null ;
   	return new FileOutputStream(file) ;
   }

	
	// Return the size of the file.
	
	long getSize()
   {
      if (memfile != null) return memfile.getSize() ;
      if (file == null) return -1 ;
//    if (copy) return 0 ;
      if (filesize >= 0) return filesize ;
      return file.length() ;
   }

   // Return the memory file.
   
   MemFile getMemoryFile() { return memfile ; }
   
   // Return the compressed size of the file.

   long getCompressedSize() { return getSize() ; }

	// Return the specified compression method.
	
	int getMethod() { return method ; }
   
   // Return the calculated CRC32 value.
   
   long getCrc32() { return crc32 ; }

	// Set the required compression method.

	void setMethod(int m) {method = m ; }

	// Set the file size for edited memory type CNF files.

	void setFileSize(long n) {filesize = n ; }
   
   // Set the uncompressed CRC32 for the entry.
   
   void setCrc32(long crc) { crc32 = crc ; }

	// Return the time of the file creation.

	long getTime() 
   { 
      long t = (file == null) ? 0 : file.lastModified() ; 
      if (t == 0) t = lastmodified ;
      return t ;
   }

	// Set the time of the file creation.

	void setTime(long time) 
   { 
      lastmodified = time ;
      if (file != null) file.setLastModified(time) ; 
   }

	// Method to determine if the element is compressed.

	boolean isCompressed()
	{
		int m = getMethod() ;
		if (m != UNCOMPRESSED) return true ;
		return false ;
	}

	// Return the requested path name.  If our directory entry does not
   // exist as a file return the file name.  This can occur if we created
   // a DirEntry for a new configuration memory resident file.

	public String getPath()
   {
   	if (dirname == null) return filename ;
      return pathname ;
   }

	// Overload the ArchiveEntry setPath method to update our file object.

	void setPath(String path)
   {
   	super.setPath(path) ;
   	file = new File(pathname) ;
   }
   
 	// Calculate the CRC32 of the directory entry.

	long computeCRC32(byte[] buf) 
	{ 
      Checksum crc32 = new CRC32() ;
      crc32.update(buf, 0, buf.length) ;
      return crc32.getValue() ;
   }

}
