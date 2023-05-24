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
* MediaFileStream class
*
* Purpose:
*
* This class defines the IO stream used to read data for a media DataSource.
*
*/

import java.io.*;
import javax.media.* ;
import javax.media.protocol.* ;
import java.io.IOException;

final class MediaFileStream
	implements PullSourceStream, Seekable
{
	private InputStream datastream = null ;
	private ArchiveEntry ze = null ;
	private String name = null ;
	protected ContentDescriptor cd ;
	protected long length ;
	protected long index ;
	protected boolean started = false ;
	protected boolean debug = false ;

	// Constructor
	
	public MediaFileStream(ContentDescriptor cd, ArchiveEntry ze)
	{
		index = 0 ;
		this.cd = cd ;
		this.ze = ze ;
		length = ze.getSize() ;
		name = (ze != null) ? ze.getName() : null ;
		if (name == null) name = "(unknown)" ;
		debug = OptionsDialog.getDebugMedia() ;

		// Allocate the input stream.

		try { datastream = ze.getInputStream() ; }
		catch (IOException e)
		{
			System.out.println("MediaFileStream: Unable to allocate input stream for " + name) ;
		}

		if (datastream != null)
			if (debug) System.out.println("MediaFileStream: new datastream " + datastream
				+ " length " + length + " for " + name) ;
	}


	/*************************************************************************
	* Stream control
	*************************************************************************/

	public void start()
	{
   	started = true ;
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) System.out.println("MediaFileStream: start " + name) ;
	}

	public void stop()
	{
   	started = false ;
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) System.out.println("MediaFileStream: stop " + name) ;
	}

	public void close() throws IOException
	{
   	started = false ;
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) System.out.println("MediaFileStream: close datastream "
			 + datastream + " for " + name) ;
		if (datastream != null) datastream.close() ;
		datastream = null ;
      ze = null ;
      cd = null ;
	}


	/*************************************************************************
	* Controls interface
	*************************************************************************/

	public Object [] getControls() { return new Object[0] ; }

	public Object getControl(String controlType) { return null ; }


	/*************************************************************************
	* SourceStream interface
	*************************************************************************/

	public ContentDescriptor getContentDescriptor() { return cd ; }

	public long getContentLength() { return length ; }

	public boolean endOfStream() { return (index >= length) ; }


	/*************************************************************************
	* PullSourceStream interface (extends SourceStream)
	*************************************************************************/

	public int read(byte[] buffer, int offset, int nToRead) throws IOException
	{
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) System.out.println("MediaFileStream: read(" + nToRead + ")"
			+ " for " + name) ;
		if (nToRead == 0) return 0 ;
		if (endOfStream()) return -1 ;

		// Confirm that we have an open stream.

		if (datastream == null)
		{
			System.out.println("MediaFileStream: Unable to access media file " + name);
			throw new IOException("Media " + name + " null datastream") ;
		}

		// Read the requested data.

		long bytes = length - index ;
		if (nToRead < bytes) bytes = nToRead ;

		if (buffer != null)
			bytes = datastream.read(buffer,offset,(int) bytes) ;
		else
			bytes = datastream.skip(bytes) ;

		// Maintain our position in the file.

		index += bytes ;
		return (int) bytes ;
	}


	public boolean willReadBlock()
	{
		int n = 0 ;
		if (datastream == null) return false ;
		try {n = datastream.available() ; }
		catch (IOException e) { n = 0 ; }
		return (n == 0) ;
	}


	/*************************************************************************
	* Seekable interface
	*************************************************************************/

	public long seek(long where)
	{
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) System.out.println("MediaFileStream: seek(" + where + ")"
			+ " for " + name);
		if (where >= length) where = length ;
		if (where < 0) where = 0;
      long difference = where - index ;

		// Allocate a new input stream if seeking backwards.

		try
      {
	      if (difference < 0)
	      {
         	datastream.close() ;
         	datastream = ze.getInputStream() ;
            difference = where ;
         }
         while (difference > 0)
				difference -= datastream.skip(difference) ;
      }
		catch (IOException e)
		{
      	where = 0 ;
			System.out.println("MediaFileStream: Unable to allocate input stream for " + name) ;
		}

      index = where ;
		return index;
	}

	public long tell() { return index ; }

	public boolean isRandomAccess() { return true ; }
}
