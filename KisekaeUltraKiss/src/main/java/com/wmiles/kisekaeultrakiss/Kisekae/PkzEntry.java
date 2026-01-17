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
* PkzEntry class
*
* Purpose:  
* 
* Objects of this class are ZIP file elements.
*
*/

import java.io.*;
import java.util.zip.* ;


final class PkzEntry extends ArchiveEntry
{
   private ZipFile zip = null ;           // Actual zip file
   private ZipEntry ze = null ;           // Actual zip entry

	// Constructor
	
	public PkzEntry(ZipFile zipfile, ZipEntry zipentry, ArchiveFile af)
	{
      zip = zipfile ;
      ze = zipentry ;
      archive = af ;
      if (ze != null)
      {
      	File f = new File(ze.getName()) ;
      	filename = f.getName() ;
         dirname = f.getParent() ;
         pathname = f.getPath() ;
         if (dirname == null && af != null) 
         {
//            dirname = af.getDirectoryName() ;
            pathname = af.getPath() ;
         }
         if (dirname == null) dirname = "" ;
         else if (!dirname.endsWith(File.separator)) dirname += File.separator ;
      }
	}

  	// Constructor.  This constructs a new Zip entry from a memory file.

	PkzEntry(MemFile in, ZipEntry zipentry, ArchiveFile af)
	{
		memfile = in;
      ze = zipentry ;
      archive = af ;
      if (ze != null)
      {
      	File f = new File(ze.getName()) ;
      	filename = f.getName() ;
         dirname = f.getParent() ;
         pathname = f.getPath() ;
         if (dirname == null) dirname = "" ;
         else if (!dirname.endsWith(File.separator)) dirname += File.separator ;
      }
	}

	public PkzEntry(String name)
	{
		zip = null ;
		ze = null ;
		archive = null ;
		File f = new File(name) ;
		filename = f.getName() ;
		dirname = f.getParent() ;
		pathname = f.getPath() ;
	}

	// Return an input stream to read this element in uncompressed form.
   // If the PkzFile was closed then on a reopen a new zipfile is created.
   // This PkzEntry will have a reference to the old zipfile that was closed.

	public InputStream getInputStream() throws IOException
	{
      if (ze == null) return null ;
		if (archive == null) return null ;
      if (memfile == null && !archive.isOpen()) return null ;
      ZipFile zf = (archive instanceof PkzFile) ? ((PkzFile) archive).getZipFile() : null ;
      if (zf != zip) zip = zf ;
		if (zip != null)
      {
         try 
         {
            InputStream is = zip.getInputStream(ze) ;
            if (is == null) return null ;
            return new BufferedInputStream(is,4096) ;
         }
         catch (IllegalStateException e)
         {
            throw new IOException(e.getMessage()) ;
         }
      }

      // If we are using a memory file then we scan the file to find
      // the appropriate stream position.  There should be a way to
      // skip directly to the element position.

      if (memfile != null)
      {
         ZipInputStream is = new ZipInputStream(memfile.getInputStream()) ;
         while (true)
         {
            ZipEntry zipentry = is.getNextEntry() ;
            if (zipentry == null) break ;
         	File f = new File(zipentry.getName()) ;
            if (pathname.equals(f.getPath())) return is ;
            is.closeEntry() ;
         }
      }
      return null ;
   }

	// Return the size of the file.
	
	long getSize()
   {
      if (ze == null) return -1 ;
//    if (copy) return 0 ;
      return ze.getSize() ;
   }

	// Return the compressed size of the file.

	long getCompressedSize()
	{
		if (ze == null) return -1 ;
		return ze.getCompressedSize() ;
	}

	// Return the CRC-32 for the uncompressed file.

	long getCrc32()
	{
		if (ze == null) return -1 ;
		return ze.getCrc() ;
	}

	// Return the time of file creation.
	
	long getTime()
   {
		if (ze == null) return -1 ;
      return ze.getTime() ;
   }

	// Set the time of the file creation.

	void setTime(long time)
   {
      if (ze == null) return ;
      ze.setTime(time);
   }

	// Return the compression method.
	
	int getMethod()
   {
      if (ze == null) return -1 ;
      return ze.getMethod() ;
   }

	// Set the compression method.

	void setMethod(int method)
   {
      if (ze == null) return ;
      ze.setMethod(method);
   }

	// Method to determine if the element is compressed.

	boolean isCompressed()
	{
		int m = getMethod() ;
		if (m >= 0) return (m == ZipEntry.DEFLATED) ;
		return false ;
	}

	// Set the required output element name.

	void setName(String name)
   {
		ZipEntry newze = new ZipEntry(name) ;
		newze.setComment(ze.getComment()) ;
      newze.setExtra(ze.getExtra()) ;
      newze.setMethod(ze.getMethod()) ;
      newze.setTime(ze.getTime()) ;
      ze = newze ;
   	super.setName(name) ;
   }

   // Returns the full path name of the file.  If the directory is
   // the home UNIX '/' character then simply return the file name.

	public String getPath() 
	{
      String s1 = convertSeparator(dirname) ;
      String s2 = convertSeparator(filename) ;
     	File f = new File(s1,s2) ;
      String path = f.getPath() ;
      if (path == null) return filename ;
      s1 = File.separator + s2 ;
      if (path.equalsIgnoreCase(s1)) return s2 ;
		return path ;
	}

   // Returns the full path name of the file.
   
	public String getPathName() 
	{
		return pathname ;
	}
}
