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
* DirFile class
*
* Purpose:
*
* This class manages an uncompressed directory for a KiSS data set.
* It contains methods to open and close the file and to return an
* enumeration of the files contained in the directory.
*
*/

import java.io.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.NoSuchElementException ;

final class DirFile extends ArchiveFile
{
	// Directory attributes.

	private File file = null ;					// File object for directory
   private String name = null ;           // File or directory path name
   private String directory = null ;      // Directory name
   private boolean single = false ;       // if true then only one file


	// Constructor.  This method examines a file directory and
	// constructs a vector of elements contained within the directory.
   // We can construct a DirFile for only a single file entry.

	public DirFile(FileOpen fileopen, String name) throws IOException
	{ this(fileopen, name, false) ; }
   
	public DirFile(FileOpen fileopen, String name, boolean single) throws IOException
	{
      this.name = name ;
		this.fileopen = fileopen ;
      this.single = single ;
		pathname = name ;
      directory = name ;

      // A DirFile object can be created with a file name or directory name.
      // If it is a directory ensure our pathname is terminated properly.

      if (pathname != null)
      {
         File f = new File(pathname) ;
         if (!f.isDirectory())
            directory = f.getParent() ;
         else if (!pathname.endsWith(File.separator))
            pathname += File.separator ;
      }
		open() ;
   }



	// ArchiveFile abstract method implementations
	// -------------------------------------------


   // Implementation of the abstract method to open the directory.
   // When directory files are first opened the contents are established,
   // If they are re-opened the existing contents are retained and any
   // new files in the directory are added to the contents.

   public void open() throws IOException
   {
      if (contents == null) init() ;
      if (directory == null) return ;
		file = new File(directory) ;
		File [] files = file.listFiles() ;
		if (files == null) return ;
		if (OptionsDialog.getDebugControl())
      	PrintLn.println("Open DirFile file " + pathname + ", Open count " + ++opencount) ;

		// Construct an index of the elements in the file directory.

      open = true ;
      if (single) 
      {
         files = new File[1] ;
         files[0] = new File(pathname) ;
      } 
      
		for (int i = 0 ; i < files.length ; i++)
		{
      	File f = files[i] ;
         if (f.isDirectory()) continue ;
         String s = f.getPath() ;
         if (s == null) continue ;
         if (key.get(s.toLowerCase()) != null) continue ;
			DirEntry h = new DirEntry(directory,f.getName(),this);
         addEntry(h) ;
         size += h.getSize() ;
         compressedsize += h.getCompressedSize() ;
		}
	}


	// Returns an input stream for reading the uncompressed contents of
	// the specified directory file entry.

	public InputStream getInputStream(ArchiveEntry de) throws IOException
	{
   	if (!isOpen()) return null ;
      if (!(de instanceof DirEntry)) return null ;
      return (de.getInputStream()) ;
   }


	// Returns an output stream for writing the uncompressed contents of
	// the specified directory file entry.

	public OutputStream getOutputStream(ArchiveEntry de) throws IOException
	{
      if (!(de instanceof DirEntry)) return null ;
      return (de.getOutputStream()) ;
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
      if (directory != null) return directory ;
      return pathname ;
   }


   // Required method to close a directory file.

	public void close()
	{
      if (opencount <= 0) return ;
		if (OptionsDialog.getDebugControl() && pathname != null)
      	PrintLn.println("Close DirFile file " + pathname + ", Open count " + --opencount) ;
		file = null ;
      open = false ;
	}


	// Release the directory contents reference.

	void flush() 
	{
      file = null ;
      opencount = 0 ;
      contents = null ;
	}


   // Returns the file open state.

   public boolean isOpen() { return (file != null) ; }
}
