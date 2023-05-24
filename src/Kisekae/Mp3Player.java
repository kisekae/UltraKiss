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
 
/*
 * 11/19/04		1.0 moved to LGPL.
 * 29/01/00		Initial version. mdm@techie.com
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */


import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
	
/**
 * The <code>Player</code> class implements a simple player for playback
 * of an MPEG audio stream. 
 * 
 * @author	Mat McGowan
 * @since	0.0.8
 */

// REVIEW: the audio device should not be opened until the
// first MPEG audio frame has been decoded. 
public class Mp3Player extends Thread
{
   private Mp3Clip audiosound = null ;    // parent clip object
	private Bitstream bitstream;           // The MPEG audio bitstream. 
	private Header header;                 // The audio frame header. 
	private Decoder decoder;               // The MPEG audio decoder.
	private AudioDevice audio;             // The AudioDevice the samples are written to.
	private boolean closed = false;        // Has the player been closed?
	private boolean complete = false;      // Has the player played back all frames from the stream?
	private int lastPosition = 0;          // The position in milliseconds
	private int frame = 0;                 // The current frame number. 
   
   // MP3 file characteristics
   
	private int framesize = 0;             // The current frame size.
	private int totalframes = 0;           // The total frames in the file.
	private int totalduration = 0;         // The total file milliseconds.
   private int nLayer ;                   // number of layers
   private int nSFIndex ;                 // sample frequency index
   private int nMode ;                    // header mode
   private int nVersion ;                 // header version
   private int nChannels ;                // number of channels
   private int FrameSize ;                // frame size
   private int nFrequency ;               // sample frequency
   private float FrameRate ;              // frame rate
   private int BitRate ;                  // sample bit rate
   
	
	/**
	 * Creates a new <code>Player</code> instance. 
	 */
	public Mp3Player(Mp3Clip a, InputStream stream) throws JavaLayerException
	{
		this(a, stream, null);	
	}
	
	public Mp3Player(Mp3Clip a, InputStream stream, AudioDevice device) throws JavaLayerException
	{
      audiosound = a ;
		bitstream = new Bitstream(stream);		
		decoder = new Decoder();
				
		if (device!=null)
		{		
			audio = device;
		}
		else
		{			
			FactoryRegistry r = FactoryRegistry.systemRegistry();
			audio = r.createAudioDevice();
		}
      
      header = bitstream.readFrame();
      getMp3Info(header,audiosound.getBytes()) ;
		audio.open(decoder);
	}
   
   
   // Thread to read frames and send to audio device.
	
	public void run() 
	{
      try { play(Integer.MAX_VALUE); }
      catch (Exception e) 
      {
         System.out.println("Mp3Player: exception, " + e) ;
      }
      audiosound.stopThread() ;
	}
	
	/**
	 * Plays a number of MPEG audio frames. 
	 * 
	 * @param frames	The number of frames to play. 
	 * @return	true if the last frame was played, or false if there are
	 *			more frames. 
	 */
	public boolean play(int frames) throws JavaLayerException
	{
		boolean ret = true;

      try
      {
         while (frames-- > 0 && ret)
         {
            if (audiosound.terminate()) 
               throw new InterruptedException() ;
            ret = decodeFrame();	
            frame++ ;
         }
      }
      catch (InterruptedException e ) 
      {
          if (OptionsDialog.getDebugMedia()) 
              System.out.println("Mp3Player: interrupted playback for " + audiosound.getName() + " at " + getPosition()) ;
      }
		
		// last frame, ensure all data flushed to the audio device. 
      
      if (!ret)
      {
         AudioDevice out = audio;
         if (out!=null)
         {				
            out.flush();
            synchronized (this)
            {
               complete = (!closed);
               close();
            }				
         }
      }
		return ret;
	}
		
	/**
	 * Cloases this player. Any audio currently playing is stopped immediately. 
	 */
	public synchronized void close()
	{		
		AudioDevice out = audio;
		if (out!=null)
		{ 
			closed = true;
			audio = null;	
         framesize = 0 ;
			// this may fail, so ensure object state is set up before
			// calling this method. 
			lastPosition = out.getPosition();
			out.close();
			try
			{
				bitstream.close();
			}
			catch (BitstreamException ex)
			{
			}
		}
	}
	
	/**
	 * Returns the completed status of this player.
	 * 
	 * @return	true if all available MPEG audio frames have been
	 *			decoded, or false otherwise. 
	 */
	public synchronized boolean isComplete()
	{
		return complete;	
	}
	
	/**
	 * Returns the closed status of this player.
	 * 
	 * @return	true if player has been closed, or false otherwise. 
	 */
	public synchronized boolean isClosed()
	{
		return closed;	
	}
	
	/**
	 * Returns the description of this stream.
	 */
	public String getContentDescriptor()
	{
		String s = "MP3 version=" + nVersion ;	
      s +=  " layers=" + nLayer ;
      s +=  " channels=" + nChannels ;
      s +=  " samplefreq=" + nFrequency ;
      s +=  " bitrate=" + BitRate ;
      s +=  " framerate=" + FrameRate ;
      return s ;
	}
	
	/**
	 * Returns the frame size of this stream.
	 * 
	 * @return frame size in bytes if known, or 0 otherwise. 
	 */
	public int getFrameSize()
	{
		return framesize;	
	}
	
	/**
	 * Returns the frame length of this stream.
	 * 
	 * @return total number of frames, or 0 if unknown. 
	 */
	public int getFrameLength()
	{
		return totalframes;	
	}
	
	/**
	 * Returns the curren frame of this stream.
	 * 
	 * @return frame processed since the player has been started, or 0 otherwise. 
	 */
	public int getFramePosition()
	{
		return frame;	
	}
	
	/**
	 * Sets the curren frame of this stream.
	 */
	public void setFramePosition(int n)
	{
		frame = n ;	
	}
	
	/**
	 * Sets the curren frame of this stream.
	 */
	public int getMicrosecondLength()
	{
		return totalduration ;	
	}
				
	/**
	 * Retrieves the position in milliseconds of the current audio
	 * sample being played. This method delegates to the <code>
	 * AudioDevice</code> that is used by this player to sound
	 * the decoded audio samples. 
	 */
	public int getPosition()
	{
		int position = lastPosition;
		
		AudioDevice out = audio;		
		if (out!=null)
		{
			position = out.getPosition();	
		}
		return position;
	}		
	
	/**
	 * Decodes a single frame.
	 * 
	 * @return true if there are no more frames to decode, false otherwise.
	 */
	protected boolean decodeFrame() throws JavaLayerException
	{		
		try
		{
			AudioDevice out = audio;
			if (out == null) return false;

			// read frames unless first header already read
			Header h = (header != null) ? header : bitstream.readFrame();	
			if (h == null) return false;
         header = null ;
				
			// sample buffer set when decoder constructed
			SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitstream);
         if (framesize == 0) framesize = output.getBufferLength() ;
																																					
			synchronized (this)
			{
				out = audio;
				if (out != null)
				{		
               int n = output.getBufferLength() ;
               if (OptionsDialog.getDebugMedia()) 
                  System.out.println("Mp3Player: write(" + n + ") for " + audiosound.getName()) ;
					out.write(output.getBuffer(), 0, n);
				}				
			}
																			
			bitstream.closeFrame();
		}		
		catch (RuntimeException ex)
		{
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}
/*
		catch (IOException ex)
		{
			System.out.println("exception decoding audio frame: "+ex);
			return false;	
		}
		catch (BitstreamException bitex)
		{
			System.out.println("exception decoding audio frame: "+bitex);
			return false;	
		}
		catch (DecoderException decex)
		{
			System.out.println("exception decoding audio frame: "+decex);
			return false;				
		}
*/		
		return true;
	}

	public void getMp3Info(Header header, int mediaLength)
      throws JavaLayerException
   {
      if (header != null) 
      {
         // nVersion = 0 => MPEG2-LSF (Including MPEG2.5), nVersion = 1 => MPEG1
         nVersion = header.version();
      
         // nLayer = 1,2,3
         nLayer = header.layer();
         nSFIndex = header.sample_frequency();
         nMode = header.mode();
         nChannels = nMode == 3 ? 1 : 2;
         
         FrameSize = header.calculate_framesize();
         if (FrameSize < 0) throw new JavaLayerException("Invalid FrameSize : "+FrameSize);
         framesize = FrameSize ;
      
         nFrequency = header.frequency();
         FrameRate = (float) ((1.0/(header.ms_per_frame()))*1000.0);
         if (FrameRate < 0) throw new JavaLayerException("Invalid FrameRate : "+FrameRate);
      
         if (mediaLength >= 0) 
            totalframes = header.max_number_of_frames(mediaLength);
         BitRate = Header.bitrates[nVersion][nLayer-1][header.bitrate_index()];
         if (mediaLength >= 0) 
            totalduration = Math.round(header.total_ms(mediaLength));
      }
   }
}
