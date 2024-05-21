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
* PkzFile class
*
* Purpose:
*
* This class manages ZIP files for a KiSS data set.  It contains methods
* to open and close the file and to return an enumeration of the files
* contained in the directory.
*
*/

import java.io.* ;
import java.util.zip.* ;
import java.util.jar.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.NoSuchElementException ;

final class PkzFile extends ArchiveFile
{
	// Directory attributes.

	private ZipFile zipfile = null ;			   // Zip File object for the file


	// Constructor.  This method examines the zip file directory and
	// constructs a vector of elements contained within the file.

	public PkzFile(FileOpen fileopen, String name) throws IOException
	{ this(fileopen,name,"r") ; }


	// Constructor.  This method opens a new zip file from a memory file.

   public PkzFile(FileOpen fileopen, String name, MemFile mem) throws IOException
   {
      memfile = mem ;
   	this.fileopen = fileopen ;
		pathname = name ;
		contents = new Vector() ;
		open() ;
	}


	// Constructor.  This method opens a new zip file in the specified mode.

   public PkzFile(FileOpen fileopen, String name, String mode) throws IOException
   {
   	this.fileopen = fileopen ;
		pathname = name ;
      init() ;
		if ("rw".equals(mode)) return ;
		open() ;
	}


	// Return the zip file.

	ZipFile getZipFile() { return zipfile; }



	// ArchiveFile abstract method implementations
	// -------------------------------------------


   // Implementation of the abstract method to open the archive file.

   public void open() throws IOException
   {
      if (contents == null) init() ;
      if (pathname == null) return ;
		if (OptionsDialog.getDebugControl())
      	System.out.println("Open PkzFile file " + pathname + ", Open count " + ++opencount) ;

      // Construct the zip file object.

		int n = pathname.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : pathname.substring(n).toLowerCase() ;
      try
      {
         if (".gzip".equals(ext)) zipfile = new ZipFile(pathname) ;
         if (".zip".equals(ext)) zipfile = new ZipFile(pathname) ;
         if (".jar".equals(ext)) zipfile = new JarFile(pathname) ;
      }
      catch (Exception e)
      {
         if (memfile == null) memfile = UrlLoader.getMemoryFile() ;
         if (memfile == null) throw(new IOException(e.toString())) ;
         System.out.println("PkzFile: Open memory file " + memfile.getFileName()) ;
      }

		// Construct an index of the elements in the file.
      // Flush the old contents as we have created a new zip file.

      open = true ;
		if (zipfile != null)
      {
         flush() ;
   		Enumeration enum1 = zipfile.entries() ;
   		while (enum1 != null && enum1.hasMoreElements())
   		{
   			Object o = enum1.nextElement() ;
            PkzEntry ze = new PkzEntry(zipfile,(ZipEntry) o,this) ;
            addEntry(ze) ;
            size += ze.getSize() ;
            compressedsize += ze.getCompressedSize() ;
   		}
      }

      // For secure environments we may have loaded a URL.

      else if (memfile != null)
      {
         try
         {
            ZipInputStream is = new ZipInputStream(memfile.getInputStream()) ;
            while (true)
            {
               ZipEntry zipentry = is.getNextEntry() ;
               if (zipentry == null) break ;
               PkzEntry ze = new PkzEntry(memfile,zipentry,this) ;
      			contents.addElement(ze) ;
               size += ze.getSize() ;
               compressedsize += ze.getCompressedSize() ;
               is.closeEntry() ;
            }
         }
         catch (ZipException e) { }
         catch (IOException e)
         {
         	try { close() ; }
            catch (IOException ex) { }
         	if (contents == null || contents.size() == 0) throw e ;
            PkzEntry h = (PkzEntry) contents.lastElement() ;
            String s1 = "Zip file last successful element: " + h.getName() ;
            String s = e.getMessage() ;
            if (s != null) throw new IOException(s + "\n" + s1) ;
            throw new IOException(s1) ;
         }
      }
	}


	// Returns an input stream for reading the uncompressed contents of
	// the specified zip file entry.

	public InputStream getInputStream (ArchiveEntry ze) throws IOException
	{
   	if (!isOpen()) return null ;
      if (!(ze instanceof PkzEntry)) return null ;
      return ze.getInputStream() ;
   }

	// Returns an output stream for writing the compressed contents of
	// the specified zip file entry.

	public OutputStream getOutputStream (ArchiveEntry ze) throws IOException
	{ return null ; }


	// Close the zip file only if no media connections exist.

	public void close() throws IOException
	{
		if (connections > 0) return ;
		if (OptionsDialog.getDebugControl() && pathname != null)
      	System.out.println("Close PkzFile file " + pathname + ", Open count " + --opencount) ;
		if (zipfile != null) zipfile.close() ;
		if (memfile != null) memfile.close() ;
		zipfile = null ;
      memfile = null ;
//      contents = null ;
//      fileopen = null ;
      open = false ;
	}


	// Release the zip contents reference.

	void flush() 
	{
      if (contents != null) contents.clear() ;
      if (key != null) key.clear() ;
	}


   // Returns the file open state.

   public boolean isOpen()
   {
      if (zipfile != null) return true ;
      return (memfile != null) ;
   }
}