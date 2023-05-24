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
* MemFile class
*
* Purpose:
*
* This class simulates a random access file for LHA type file processing.
* Files of this type are created for URL files loaded from the web if we
* are running in a secure environment.
* 
*/

import java.io.* ;

final class MemFile
{
   private String filename = null ;
   private byte [] buffer = null ;
   private long offset = 0 ;


	// Constructor

   public MemFile() { }

   public MemFile(String name, byte [] mem)
	{
		filename = name ;
      buffer = mem ;
	}

   // Return an input stream to read this memory file.

   InputStream getInputStream()
   { return new ByteArrayInputStream(buffer) ; }

   // When we close a memory file we reset our offset pointer.

   public void close() { offset = 0 ; }

   public long getFilePointer() { return offset ; }

   public long length() { return (buffer == null) ? -1 : buffer.length ; }

   public int read() throws IOException
   {
      if (buffer == null) throw new IOException("no memory buffer") ;
      if (offset >= buffer.length) return -1 ;
      return (int) (buffer[(int) offset++] & 0xff) ;
   }

   public byte readByte() throws IOException
   {
      if (buffer == null) throw new IOException("no memory buffer") ;
      if (offset >= buffer.length) return -1 ;
      return (byte) (buffer[(int) offset++] & 0xff) ;
   }

   public short readShort() throws IOException
   {
      if (buffer == null) throw new IOException("no memory buffer") ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n1 = (buffer[(int) offset++]) & 0xff ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n2 = (buffer[(int) offset++]) & 0xff ;
      return (short) ((n1 << 8) + n2) ;
   }

   public int readInt() throws IOException
   {
      if (buffer == null) throw new IOException("no memory buffer") ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n1 = (buffer[(int) offset++]) & 0xff ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n2 = (buffer[(int) offset++]) & 0xff ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n3 = (buffer[(int) offset++]) & 0xff ;
      if (offset >= buffer.length) throw new EOFException() ;
      int n4 = (buffer[(int) offset++]) & 0xff ;
      return ((n1 << 24) + (n2 << 16) + (n3 << 8) + n4) ;
   }

   public void seek(long pos) throws IOException
   {
      offset = pos ;
      if (offset < 0) throw new IOException("memory file invalid file position") ;
   }

   public int skipBytes(int n) throws IOException
   {
      long oldoffset = offset ;
      offset += n ;
      if (offset < 0) throw new IOException("memory file invalid file position") ;
      if (offset > buffer.length) offset = buffer.length ;
      return (int) (offset - oldoffset) ;
   }

   public String getFileName() { return filename ; }
}
