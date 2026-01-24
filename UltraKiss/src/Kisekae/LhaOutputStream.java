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
* LhaOutputStream class
*
* Purpose:
*
* This class manages the writing to an LHA compressed file.  It contains
* methods to open and close the file and create new entries within the file.
*
* The writes to the file are staged to a byte array and finally encoded to
* the LHA random access file when the file stream is closed.
*
*/

import java.io.*;
import java.util.*;
import java.util.zip.ZipException ;

final class LhaOutputStream extends ByteArrayOutputStream
{
	private RandomAccessFile aout = null ;
   private ByteArrayOutputStream header = null ;
   private LhaFileOutputStream lhaout = null ;
   private FileWriter fw = null ;
   private LhaEntry le = null ;

   // Variables to track writing an LHA file element entry.

   private long filepointer = 0 ;
   private long endpointer = 0 ;
	private int byteswritten = 0 ;
	private int headercrclocation = 0 ;
   private int headersize = 0 ;
   private int packsize = 0 ;
   private int origsize = 0 ;
   private int crc = 0 ;


	// Constructor

	public LhaOutputStream(LhaFile out)
	{
		aout = out.getRandomAccessFile() ;
      lhaout = new LhaFileOutputStream() ;
	}
   
   
   // Set the FileWriter reference for write tracking display.
   // Note that our tracking is based upon uncompressed file size
   // so we track consumption of input bytes rather than writes
   // of output bytes.
   
   void setFileWriter(FileWriter f) { fw = f ; }


   // Method to create a new header entry for the LHA file.  When the
   // data contents for the element are written to this ByteArrayOutputStream
   // object and this object is closed, then we perform the LHA encoding.

   void putNextEntry(LhaEntry le) throws IOException
   {
   	if (le == null) return ;
   	closeEntry() ;
      this.le = le ;
      header = new ByteArrayOutputStream() ;
      writeHeader(header) ;
   }


   // Overloaded method to close this LhaOutputStream.  We write any
   // pending LHA entry data, then a file terminating null byte, and
   // close the LHA random access file.

   public void close() throws IOException
   {
   	if (aout == null) return ;
      closeEntry() ;
      Putbyte((byte) 0, lhaout) ;
      lhaout.close() ;
      super.close() ;
      aout.close() ;
      aout = null ;
   }


   // A method to close our currently active LHA file entry.  We extract
   // the buffered data, write the LHA header information to the LHA
   // file, encode the buffered data to the LHA file, then finally
   // update the header information to reflect the compressed data.

   private void closeEntry() throws IOException
   {
   	Lhhuf lhhuf = null ;
      Lzhuf lzhuf = null ;
      InputStream in = null ;

   	if (header == null || le == null) return ;
      filepointer = lhaout.getFilePointer() ;

      // Write the header.

      byteswritten = 0 ;
      byte [] h = header.toByteArray() ;
      headersize = h.length ;
      lhaout.write(h) ;

      // Compress the data.

      byte [] data = this.toByteArray() ;
      origsize = data.length ;
		switch (le.getMethod())
		{
		case 0:
	      lhaout.write(data) ;
         crc = LhaCrc16.calcCRC(data) ;
			if (fw != null) fw.updateProgress(data.length) ;
         break ;

		case 1:
	      in = new ByteArrayInputStream(data) ;
	      lzhuf = new Lzhuf() ;
         lzhuf.setFileWriter(fw) ;
	      lzhuf.encode(in,lhaout) ;
         break ;

		case 5:
		case 6:
		case 7:
	      in = new ByteArrayInputStream(data) ;
	      lhhuf = new Lhhuf(origsize,le.getMethod()) ;
         lhhuf.setFileWriter(fw) ;
	      lhhuf.encode(in,lhaout) ;
         crc = lhhuf.CRC() ;
         break ;
         
		case 99:
	      lhaout.write(data) ;
         crc = LhaCrc16.calcCRC(data) ;
			if (fw != null) fw.updateProgress(data.length) ;
         break ;

		default:
			throw new ZipException("LHA unimplemented method: " + le.getMethodText());
		}

      // Update the header.

      if (in != null) in.close() ;
      endpointer = lhaout.getFilePointer() ;
      packsize = (int) (endpointer - (filepointer + headersize)) ;
      updateHeader(h) ;
      lhaout.seek(filepointer) ;
      lhaout.write(h);

      // Position for the next element.

      header.close() ;
      this.reset() ;
      lhaout.seek(endpointer) ;
      header = null ;
   }


   // Function to write a string to the output stream.

   private void Putstring(String str, OutputStream outs) throws IOException
   {
      byte[] buf = str.getBytes();
      outs.write(buf);
      byteswritten += buf.length ;
   }

   // Function to write out four bytes to the LHA file in little endian mode.

   private void Putword(int w, OutputStream outs) throws IOException
	{
      Putbyte((byte) ( w & 0xff ), outs);
      Putbyte((byte) ( ( w >> 8 ) & 0xff ), outs);
      Putbyte((byte) ( ( w >> 16 ) & 0xff ), outs);
      Putbyte((byte) ( ( w >> 24 ) & 0xff ), outs);
   }

   // Function to write out two bytes to the LHA file in little endian mode.

   private void Putshort(int w, OutputStream outs) throws IOException
	{
      Putbyte((byte) ( w & 0xff ), outs);
      Putbyte((byte) ( ( w >> 8 ) & 0xff ), outs);
   }

   // Function to write out a byte to the LHA file.

   void Putbyte(byte b, OutputStream outs) throws IOException
   { outs.write(b) ; byteswritten++ ; }


   // Function to construct an LHA file element header.   We write level 2
   // headers and then update the values once the data set is written.
	//
	// Header Formats:
	//  (integers are little-endian; i.e. low-order bytes come first)
	//
	// ---------------------------------------------------------------
	//   level-0              level-1              level-2
	// ---------------------------------------------------------------
	// 1 header size        1 header size        2 total header size
	// 1 header sum         1 header sum                                   
	// 5 method ID          5 method ID          5 method ID
	// 4 packed size        4 skip size          4 packed size          
	// 4 original size      4 original size      4 original size
	// 2 time (MS-DOS)      2 time (MS-DOS)      4 time (UNIX style)    
	// 2 date (MS-DOS)      2 date (MS-DOS)
	// 1 MS-DOS attribute   1 fixed (0x20)       1 reserved (now 0x20)  
	// 1 level (0x00)       1 level (0x01)       1 level (0x02)
	// 1 name length        1 name length                                  
	// ? pathname           ? filename
	// 2 file crc           2 file crc           2 file crc             
	// . ........           1 OS ID ('M')        1 OS ID ('M')
	//                      . ........
	//                      2 next header size   2 next header size
	// ***************************************************************
	// 24 + ?               27 + ?               26
	// ---------------------------------------------------------------
	//
	// Extension Format (Type 1 and Type 2 headers)
	//   1 ext-type
	//   . ........
	//   2 next-header size
	// ---------------------


	private void writeHeader(OutputStream out) throws IOException
	{
   	// Total header size.  Set to zero and we fix this later.
		Putshort(0, out) ;

      // Method id.  Set to the element compression method (-lh5- or -lh0-).
      Putstring(le.getMethodText(), out) ;

      // The packed size.  Set to zero and we fix this later.
		Putword(0, out) ;

      // The original file size.  Set this to zero and fix it later.
      Putword(0, out) ;

      // The element creation date-time.
      Putword(LhaEntry.toMsdos(le.getTime()), out) ;

      // Reserved.
      Putbyte((byte) 0, out) ;

      // Header level (2)
      Putbyte((byte) 2, out) ;

      // File CRC value.  We set this to zero and fix it later.
      Putshort(0, out) ;

      // The OS level.  M is MSDOS.  U is UNIX.  m is MAC.
      Putbyte((byte) 'M', out) ;

   	// First extended header.  CRC of the header.  Extension type is 0.
      Putshort(5, out) ;
		Putbyte((byte) 0, out) ;
		headercrclocation = byteswritten ;
		Putshort(0, out) ;

   	// Next extended header.  Element name.  Extension type is 1.
      String name = le.getName() ;
		Putshort(name.length() + 3, out);
		Putbyte((byte) 1, out);
      Putstring(name, out) ;

   	// Next extended header.  Directory name.  Extension type is 2.
      String dir = le.getPath() ;
		if (dir != null)
		{
      	if (dir.endsWith(name))
         	dir = dir.substring(0,dir.length()-name.length()) ;
			Putshort(dir.length() + 3, out);
			Putbyte((byte) 2, out) ;
			Putstring(dir, out) ;
		}

      // Not sure why we end our extended headers this way.
		Putshort(0, out);
	}


	// Method to correct our header fields after the data has been written.

	private void updateHeader(byte [] h)
   	throws IOException
	{
   	// Set the correct header size.
      h[0] = (byte) (headersize & 0xff) ;
      h[1] = (byte) ((headersize >> 8) & 0xff) ;

      // Set the correct compressed file size.
      h[7] = (byte) (packsize & 0xff) ;
      h[8] = (byte) ((packsize >> 8) & 0xff) ;
      h[9] = (byte) ((packsize >> 16) & 0xff) ;
      h[10] = (byte) ((packsize >> 24) & 0xff) ;

      // Set the correct original file size.
      h[11] = (byte) (origsize & 0xff) ;
      h[12] = (byte) ((origsize >> 8) & 0xff) ;
      h[13] = (byte) ((origsize >> 16) & 0xff) ;
      h[14] = (byte) ((origsize >> 24) & 0xff) ;

      // Set the correct compressed data CRC.
      h[21] = (byte) (crc & 0xff) ;
      h[22] = (byte) ((crc >> 8) & 0xff) ;

      // Set the correct header CRC.
		crc = LhaCrc16.calcCRC(h);
		h[headercrclocation] = (byte) (crc & 0xff) ;
		h[headercrclocation+1] = (byte) ((crc >> 8) & 0xff) ;
	}


	// Inner class to create a specialized output stream to write to a
	// random access file.

	class LhaFileOutputStream extends OutputStream
   {
	   // Required methods for an output stream.

		public void write(int ch) throws IOException { aout.write(ch) ; }

		public void write(byte b[]) throws IOException { aout.write(b) ; }

		public void close() throws IOException { aout.close() ; }


	   // Supplementary methods for a random access file.

		void seek(long p) throws IOException { aout.seek(p) ; }

		long length() throws IOException { return aout.length() ; }

		long getFilePointer() throws IOException { return aout.getFilePointer() ; }
   }
}

