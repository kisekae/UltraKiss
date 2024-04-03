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
* LhaEntry class
*
* Purpose:  
* 
* Objects of this class are LHA file elements.  Each file element has 
* a data header section followed by the file data contents.  The element
* header is read when this object is created so it is assumed that the
* LHA file is positioned correctly when this object is created.
*
*/

import java.io.*;
import java.util.*;


class LhaEntry extends ArchiveEntry
{
	static final public int LH0 = 0 ;      // Compression method code for -lh0-
	static final public int LH1 = 1 ;  		// Compression method code for -lh1-
	static final public int LH5 = 5 ;		// Compression method code for -lh5-
	static final public int LH6 = 6 ;		// Compression method code for -lh6-
	static final public int LH7 = 7 ;		// Compression method code for -lh7-
	static final public int LHD = 0 ;		// Compression method code for -lhd-

	private RandomAccessFile in = null ;	// Reference to the file
	private long filepointer = -1 ;			// Offset to this entry
	private long datapointer = -1 ;			// Offset to the data

	// LHA file header attributes.
	
	private int headersize ;					// Size of archived file header
	private int headersum ;						// One byte header checksum
	private int skipsize ;						// Compressed file size + ext. hdr
	private int origsize ;						// Uncompressed file size
	private int packsize ;						// Compressed file size
	private int next ;							// Size of next extension
	private int extsize ;						// Total extension header size
	private int time ;							// Original file date/time MSDOS
	private long datetime ;					  	// Generic time in msec from epoch
	private int attr ;							// MS-DOS file or dir attribute
	private int level ;							// Level identifier
	private int namelen ;						// Length of filename in bytes
	private int os ;								// Operating system identifier
	private int crc16 = -1 ;					// 16 bit CRC of uncompressed file
	private long crc32 = -1 ;				  	// 32 bit CRC of uncompressed file
	private String method = "" ;				// LHA compression method
	private String name = "" ;					// Original file name
	private String dir = "" ;					// Original directory name

	// Extended file header attributes.
	
	private String user = "" ;             // UNIX user name
	private String group = "" ;            // UNIX group name
	private int gid, uid, permission ;     // UNIX group, userid, permission
	private int msdosattr ;						// MSDOS attributes
	private int lastmodified ;             // Last modified UNIX time

	
	// Constructor.  This constructs a new LHA entry by reading the file
   // and decoding the next entry header.  The file is positioned after
   // the header for reading and decoding the compressed data.

	LhaEntry(RandomAccessFile in, ArchiveFile af) throws IOException
	{
		this.in = in;
      archive = af ;
	   filepointer = in.getFilePointer() ;
		readHeader();
	   datapointer = in.getFilePointer() ;
      filename = name ;
      dirname = dir ;
      pathname = getPath() ;
	}

  	// Constructor.  This constructs a new LHA entry from a memory file.

	LhaEntry(MemFile in, ArchiveFile af) throws IOException
	{
		memfile = in;
      archive = af ;
	   filepointer = memfile.getFilePointer() ;
		readHeader();
	   datapointer = memfile.getFilePointer() ;
      filename = name ;
      dirname = dir ;
      pathname = getPath() ;
	}


   // Constructor.  This constructs a new LHA entry from a given file name.

   LhaEntry(String s) 
   {
   	File f = new File(s) ;
   	name = f.getName() ;
      filename = name ;
      dir = f.getParent() ;
      dirname = dir ;
      pathname = f.getPath() ;
   }
	
	
	// Reads the LHA element header.
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
	
	void readHeader()  throws IOException
	{
		headersize = readByte() ;
		if (headersize <= 0)
			throw new EOFException("Zero sized LHA file") ;
		headersum = readByte();
		for (int i = 0 ; i < 5 ; i++)
			method += (char) readByte() ;

		// Check if this is a valid LHA file.  We used to return an IO
      // exception, but apparently some LHA encoders leave junk at
      // the end of the file and do not terminate it properly.

		if (method.charAt(0) != '-')
			throw new EOFException("Invalid LHA file signature") ;

		// Continue reading the remainder of the header.
			
		extsize = 0 ;
		skipsize = littleEndian(readInt()) ;
		origsize = littleEndian(readInt()) ;
		time = littleEndian(readInt()) ;
		attr = readByte();
		level = readByte();
		
		// Interpret the header fields based upon the header level.
		
		switch (level)
		{
		case 0:
			packsize = skipsize ;
			datetime = toGeneric(time) ;
			namelen = readByte() ;
			for (int i = 0 ; i < namelen ; i++)
				name += (char) readByte() ;
			crc16 = littleEndian(readShort()) ;
			next = 0 ;
			break ;
			
		case 1:
			datetime = toGeneric(time) ;
			namelen = readByte() ;
			for (int i = 0 ; i < namelen ; i++)
				name += (char) readByte() ;
			crc16 = littleEndian(readShort()) ;
			os = (char) readByte() ;
			next = littleEndian(readShort()) ;
			break ;
			
		case 2:
         datetime = toGeneric(time) ;
			packsize = skipsize ;
			crc16 = littleEndian(readShort()) ;
			os = (char) readByte() ;
			next = littleEndian(readShort()) ;
			break ;
			
		default:
			throw new IOException("Invalid LHA file header") ;
		}

		// Read the extended headers if they exist.
		// Level 0 files do not have header extensions.  

		if (level > 0)
		{
			// Read extended headers.  The first byte of an extended
			// header identifies the type of header, and the last two
			// bytes of the header identify whether or not more headers
			// are defined.  The Huffman compression engine will use the
			// extended header filename if both exist in level 1 archives.
			
			while (next != 0) 
			{
				extsize += next;
				int exttype = readByte();
				next -= 3;
				next = readExt(exttype, next);
				for (int j = next; j > 0; j--) readByte();
				next = littleEndian(readShort()) ;
			}
		}

		// Correct the compressed data block size for level 1 files.
		
		if (level == 1)
			packsize = skipsize - extsize ;
		
		// Position the file so the next read occurs immediately after 
		// the header area.  
		
		if (level == 0)
			seek(filepointer + headersize + 2) ;
		if (level == 1)
			seek(filepointer + headersize + extsize + 2) ;
		if (level == 2)
			seek(filepointer + headersize) ;
	}
	
	
	// Reads a header extension.
	//
	// Extension Format (Type 1 and Type 2 headers)
	//   1 ext-type
	//   . ........
	//   2 next-header size
	// ---------------------
	
	int readExt(int exttype, int size)  throws IOException 
	{
		switch (exttype) 
		{
		case 0:		// crc of header  
			break ;
		case 1:		// file name 
			name = "" ;
			for (; size > 0; size--) name += (char) readByte() ;
			break ;
		case 2:		// directory name 
			dir = "";
			for (; size > 0; size--)
         {
         	char c = (char) readByte() ;
            if (c == '\uFFFF') c = File.separatorChar ;
         	dir += c ;
         }
			break ;
		case 0x3f:	// Uncompressed file comment
			break ;
		case 0x40:	// MSDOS attr
			msdosattr = littleEndian(readShort()) ; size -= 2 ;
			break ;
		case 0x50:	// UNIX permission
			permission = littleEndian(readShort()) ; size -= 2 ;
			break ;
		case 0x51:	// UNIX gid uid
			gid = littleEndian(readShort()) ;
			uid = littleEndian(readShort()) ; size -= 4 ;
			break ;
		case 0x52:	// UNIX group name
			group = "";
			for (; size > 0; size--) group += (char) readByte() ;
			break ;
		case 0x53:	// UNIX user name
			user = "";
			for (; size > 0; size--) user += (char) readByte() ;
			break ;
		case 0x54:	// UNIX modified time
			lastmodified = littleEndian(readInt()) ; size -= 4 ;
			break ;
		}
		return size ;
	}
	
	
	// Returns an input stream to read the compressed file data from this
   // file entry.  The file is positioned appropriately for reading to
   // begin.
	
	public InputStream getCompressedInputStream() throws IOException
	{
		if (in == null && memfile == null) return null ;
		seek(datapointer) ;
      if (in != null) return new LhaInputStream(in,packsize) ;
      if (memfile != null) return new MemInputStream(memfile,packsize) ;
      return null ;
	}

	// Return an input stream to read this element in uncompressed form.

	public InputStream getInputStream() throws IOException
   {
		if (archive == null || !archive.isOpen()) return null ;
      return archive.getInputStream(this) ;
   }

	
	// Skips to the next entry in the file.
	
	void skip() throws IOException { skipBytes(packsize) ; }
	
	
	// Sets the uncompressed file CRC.

	void setCrc16(int crc) { crc16 = crc ; }
	void setCrc32(long crc) { crc32 = crc ; }

	// Return the CRC for the uncompressed file.

	int  getCrc16() { return crc16 ; }
	long getCrc32() { return crc32 ; }


	// Return the requested element name.

	public String getName() { return name ; }


   // Returns the full path name of the file.  If the directory is
   // the home UNIX '/' character then simply return the file name.

	public String getPath() 
	{
      String s1 = convertSeparator(dir) ;
      String s2 = convertSeparator(name) ;
     	File f = new File(s1,s2) ;
      String path = f.getPath() ;
      if (path == null) return s2 ;
      s1 = File.separator + s2 ;
      if (path.equalsIgnoreCase(s1)) return s2 ;
		return path ;
	}


	// Set the required output element name.

	void setName(String name)
   {
   	this.name = name ;
      super.setName(name) ;
   }



	// Set the required output path name.

	void setPath(String path)
   {
      dir = "" ;
   	name = "" ;
   	pathname = path ;
      if (path == null) return ;
     	File f = new File(path) ;
      name = f.getName() ;
      dir = f.getParent() ;
      if (dir == null) dir = "" ;
      filename = name ;
      dirname = dir ;
   }

	
	// Returns the uncompressed file size.
	
//	public long getSize() { return (copy) ? 0 : (long) origsize ; }
	public long getSize() { return (long) origsize ; }

	
	// Returns the compressed file size.

//	public long getCompressedSize() { return (copy) ? 0 : (long) packsize ; }
	public long getCompressedSize() { return (long) packsize ; }

	
	// Returns the file update or creation time.

	public long getTime() { return datetime ; }


	// Set the time of the file update or creation.

	public void setTime(long t) { datetime = t ; }


	// Returns the compression method.
	//
	//  -lh0-   no compression
	//  -lh1-   4k sliding dictionary(max 60 bytes) + dynamic Huffman
	//				+ fixed encoding of position
	//  -lh2-   8k sliding dictionary(max 256 bytes) + dynamic Huffman
	//  -lh3-   8k sliding dictionary(max 256 bytes) + static Huffman
	//  -lh4-   4k sliding dictionary(max 256 bytes) + static Huffman
	//				+ improved encoding of position and trees
	//  -lh5-   8k sliding dictionary(max 256 bytes) + static Huffman
	//				+ improved encoding of position and trees
	//	 -lzs-   2k sliding dictionary(max 17 bytes)
	//  -lz4-   no compression
	//  -lz5-   4k sliding dictionary(max 17 bytes)
	//  -lz6-   16k sliding dictionary
	//  -lz7-   32k sliding dictionary

	public int getMethod()
	{
		if ("-lh0-".equals(method)) return LH0 ;
		if ("-lh1-".equals(method)) return LH1 ;
		if ("-lh5-".equals(method)) return LH5 ;
		if ("-lh6-".equals(method)) return LH6 ;
		if ("-lh7-".equals(method)) return LH7 ;
		if ("-lhd-".equals(method)) return LHD ;
		return -1 ;
	}

	// Set the compression method.

	void setMethod(int m)
	{
		method = "" ;
		if (m == LH0) method = "-lh0-" ;
		if (m == LH1) method = "-lh1-" ;
		if (m == LH5) method = "-lh5-" ;
		if (m == LH6) method = "-lh6-" ;
		if (m == LH7) method = "-lh7-" ;
		if (m == LHD) method = "-lhd-" ;
		return ;
	}

	// Return the method text string.

	public String getMethodText() { return method ; }

	// Method to determine if the element is compressed.

	boolean isCompressed()
	{
		int m = getMethod() ;
		if (m >= 0) return (m != LH0) && (m != LHD);
		return false ;
	}

	
	// Little Endian input data conversions.  Little Endian is a method
	// where numeric terms write the least significant byte first in the
   // input stream.  LHA files are written in this form.
	
	private int littleEndian(int x)
	{
		int b0 = (x >> 24) & 255 ;
		int b1 = (x >> 16) & 255 ;
		int b2 = (x >> 8) & 255 ;
		int b3 = (x >> 0) & 255 ;
		return (int) ((b0 << 0) + (b1 << 8) + (b2 << 16) + (b3 << 24)) ;
	}
	
	private short littleEndian(short x)
	{
		int b0 = (x >> 8) & 255 ;
		int b1 = (x >> 0) & 255 ;
		return (short) ((b0 << 0) + (b1 << 8));
	}

	/* Msdos stamp format:   */
	/* 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16		*/
	/* |<------- year ---->|<- month ->|<--- day --->|		*/
	/* 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1  0		*/
	/* |<-- hour --->|<---- minute ---->|<-second*2->|		*/


	// Given a time in milliseconds sinch the epoch, convert the
	// time to a GMT date in Msdos format.

	static int toMsdos(long t)
	{
		int year, month, day, hour, min, sec;
 		int val = 0 ;

		// Convert the time to a GMT time.

		if (t == 0) return 0 ;
		Calendar c = Calendar.getInstance() ;
		c.setTime(new Date(t)) ;

      // Isolate the year, month, day, hours, minutes, seconds.

		year = c.get(Calendar.YEAR) - 1980 ;
		month = c.get(Calendar.MONTH) + 1 ;
		day = c.get(Calendar.DAY_OF_MONTH) ;
		hour = c.get(Calendar.HOUR_OF_DAY) ;
		min = c.get(Calendar.MINUTE) ;
		sec = c.get(Calendar.SECOND) / 2 ;

     // Encode the Msdos format.

		val = (year<<25) | (month<<21) | (day<<16) | (hour<<11) | (min<<5) | sec ;
		return val  ;
	}


	// Given a GMT time and date in Msdos format, convert it to milliseconds.

	static long toGeneric(long time)
	{
		int year, month, day, hour, min, sec ;
		long longtime ;

		int t = (int) (time & 0xffffffff) ;
		if (t == 0)	return 0 ;

      // Isolate year, month, day, hours, minutes, seconds.

		year = ((t >> 25) & 0x7f) + 1980 ;
		month = ((t >> 21) & 0x0f) ;		// 1..12 means Jan..Dec
		day = ((t >> 16) & 0x1f) ;			// 1..31 means 1st,...31st
		hour = ((t >> 11) & 0x1f) ;
		min = ((t >> 5) & 0x3f) ;
		sec = (t & 0x1f) * 2 ;

      // Check for valid time and date.

		if (month > 12 || month < 1) return 0 ;
		if (day > 31 || day < 1) return 0 ;
		if (hour >= 24) return 0 ;
		if (min >= 60) return 0 ;
		if (sec > 60) return 0 ;

		// Calculate time since the JAVA epoch

		Calendar calendar = Calendar.getInstance() ;
		calendar.set(year,month-1,day,hour,min,sec) ;
		Date d = calendar.getTime() ;
		longtime = d.getTime() ;
		return (longtime) ;
	}

   // Generic routines to read from a random access file or a memory file.

   byte readByte() throws IOException
   {
      if (in != null) return in.readByte() ;
      if (memfile != null) return memfile.readByte() ;
      return -1 ;
   }

   int readInt() throws IOException
   {
      if (in != null) return in.readInt() ;
      if (memfile != null) return memfile.readInt() ;
      return -1 ;
   }

   short readShort() throws IOException
   {
      if (in != null) return in.readShort() ;
      if (memfile != null) return memfile.readShort() ;
      return -1 ;
   }

   void seek(long pos) throws IOException
   {
      if (in != null) in.seek(pos) ;
      if (memfile != null) memfile.seek(pos) ;
   }

   int skipBytes(int n) throws IOException
   {
      if (in != null) return in.skipBytes(n) ;
      if (memfile != null) return memfile.skipBytes(n) ;
      return -1 ;
   }


   // An inner class to construct an input stream to read from an LHA random
   // access file entry of a known length.

   class LhaInputStream extends InputStream
   {
   	int p;
   	int packsize;
   	int ch ;
   	RandomAccessFile in;

   	// Constructor

   	LhaInputStream(RandomAccessFile in, int size)
   	{
   		this.in = in;
   		packsize = size;
   	}

   	// Read a character.  If we have reached the maximum length of the
      // entry we return an end-of-file indicator.

   	public int read() throws IOException
   	{
   		if (p >= packsize) return -1 ;
   		p++;
   		ch = in.read() ;
   		return ch ;
   	}
   }


   // An inner class to construct an input stream to read from a memory
   // file entry of a known length.

   class MemInputStream extends InputStream
   {
   	int p;
   	int packsize;
   	int ch ;
   	MemFile in;

   	// Constructor

   	MemInputStream(MemFile in, int size)
   	{
   		this.in = in;
   		packsize = size;
   	}

   	// Read a character.  If we have reached the maximum length of the
      // entry we return an end-of-file indicator.

   	public int read() throws IOException
   	{
   		if (p >= packsize) return -1 ;
   		p++;
   		ch = in.read() ;
   		return ch ;
   	}
   }
}

