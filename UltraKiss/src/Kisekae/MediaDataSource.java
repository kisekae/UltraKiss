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
* MediaDataStore class
*
* Purpose:
*
* This class defines a custom media DataSource to send media data to a media
* Player.   The data feed can be from a cached byte array or an archive file
* input stream, which are types of a MediaStream.
*
*/

import java.io.*;
import javax.media.*;
import javax.media.protocol.*;

final class MediaDataSource extends PullDataSource
	implements Positionable
{
	private byte [] data = null ;
	private long length = 0 ;
   private boolean debug = false ;
	private String name = null ;
	private ContentDescriptor cd = null ;
	private InputStream is = null ;
	private PullSourceStream[] pullStreams = null ;
	private PullSourceStream sourceStream = null ;
	private ArchiveEntry ze = null ;

	// Constructor
   
	public MediaDataSource(Object cd, byte [] data, long length, String name)
	{
		super() ;
		this.data = data ;
		this.length = length ;
		this.name = name ;
		this.cd = (cd instanceof ContentDescriptor) ? (ContentDescriptor) cd : null ;
		sourceStream = new MediaByteStream(this.cd,data,length,name) ;
		pullStreams = new PullSourceStream[1] ;
		pullStreams[0] = sourceStream;
		if (name == null) name = "(unknown)" ;
		debug = OptionsDialog.getDebugMedia() ;
	}

	public MediaDataSource(Object cd, ArchiveEntry ze)
	{
		super() ;
		this.ze = ze ;
		this.cd = (cd instanceof ContentDescriptor) ? (ContentDescriptor) cd : null ;
		if (ze != null)
		{
			try { is = ze.getInputStream() ; }
			catch (IOException e) { }
			sourceStream = new MediaFileStream(this.cd,ze) ;
      }
		pullStreams = new PullSourceStream[1] ;
		pullStreams[0] = sourceStream;
		name = (ze != null) ? ze.getName() : null ;
		if (name == null) name = "(unknown)" ;
		debug = OptionsDialog.getDebugMedia() ;
	}


	/*************************************************************************
	* Controls Methods
	*************************************************************************/

	public Object[] getControls()
	{
		if (sourceStream == null) return new Object[0] ;
		return sourceStream.getControls() ;
	}

	public Object getControl(String controlType)
	{
		if (sourceStream == null) return null ;
		return sourceStream.getControl(null) ;
	}


	/*************************************************************************
	* Duration interface
	*************************************************************************/

	public Time getDuration() { return Duration.DURATION_UNKNOWN ; }


	/*************************************************************************
	* DataSource Methods
	*************************************************************************/
    
	public String getContentType() 
	{
		if (cd == null) return ContentDescriptor.CONTENT_UNKNOWN ;
		return cd.getContentType() ;
	}

	public void connect()
	{
   	if (sourceStream == null)
      {
      	PrintLn.println("MediaDataSource: connection attempt on disconnected source stream.") ;
         return ;
      }
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) PrintLn.println("MediaDataSource: connect() " + name) ;
		ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;
	}

	public void disconnect()
   {
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) PrintLn.println("MediaDataSource: disconnect() " + name) ;
		ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;
		try
		{
			if (sourceStream instanceof MediaByteStream)
				((MediaByteStream) sourceStream).close() ;
			else if (sourceStream instanceof InputSourceStream)
				((InputSourceStream) sourceStream).close() ;
		}
		catch (IOException e) { }

      // Release storage.

      if (pullStreams != null && pullStreams.length > 0) pullStreams[0] = null ;
		sourceStream = null ;
		pullStreams = null ;
      data = null ;
      cd = null ;
      ze = null ;
   }

	public void start() 
	{
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) PrintLn.println("MediaDataSource: start() " + name) ;
		if (sourceStream instanceof MediaByteStream)
			((MediaByteStream) sourceStream).start() ;
		else if (sourceStream instanceof MediaFileStream)
			((MediaFileStream) sourceStream).start() ;
	}

	public void stop() throws IOException 
	{
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) PrintLn.println("MediaDataSource: stop() " + name) ;
		if (sourceStream instanceof MediaByteStream)
			((MediaByteStream) sourceStream).stop() ;
		else if (sourceStream instanceof MediaFileStream)
			((MediaFileStream) sourceStream).stop() ;
	}


	/*************************************************************************
	* PullDataSource Methods
	*************************************************************************/
    
	public PullSourceStream[] getStreams() { return pullStreams ; }


	/*************************************************************************
	* Positionable interface
	*************************************************************************/

	public Time setPosition(Time where, int rounding) 
	{
		debug = OptionsDialog.getDebugMedia() ;
		if (debug) PrintLn.println("MediaDataSource: setPosition("
			+ where.getSeconds() + ")" + " for " + name) ;
		return where ;
	}

	public boolean isRandomAccess() { return false ; }
}
