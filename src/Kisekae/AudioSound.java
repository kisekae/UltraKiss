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
* Audio class
*
* Purpose:
*
* This class encapsulates an audio object.  Audio objects are accessed
* through FKiSS sound and music events.
*
* Audio objects use their file name as their access key.
*
* The intent is as follows:
*
* 1. Read an audio file into a memory buffer.
*
* 2. Realize the player.  The object is now initialized.  It does not play.
*    It waits in a realized state for a play request.
*
* 3. A play request prefetches and starts the player.  The play request can
*    come at any time.  The possible states are:
*
* 3.0 (Expected state) Init function complete, player waiting in a realized
*     state, and play is requested.
*		=> initiate prefetch.  On prefetch complete, start the player.
*
* 3.1 (Slow realize state) Init function complete, player in process of
*     realizing due to initialization, and play is requested.
*		=> Set callback switch so that when realize is complete, play is initiated.
*
* 3.2 (Slow prefetch state) Init function complete, player is prefetching, and
*     another play is requested.
*		=> Ignore the play request as the player will start when prefetch completes.
*
* 3.3 (Busy state) Init function complete, player is started, and another play
*     is requested.
*		=> restart the player with setMediaTime(0) and prefetch.
*
*/


import java.io.* ;
import java.awt.event.* ;
import java.util.Hashtable ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.net.URL ;
import javax.swing.* ;
import javax.sound.sampled.* ;

import javax.sound.midi.* ;


final class AudioSound extends Audio
{
	private Object cd = null;					// The audio content descriptor
	private MediaDataSource ds = null ;		// The audio data source
	private Object currentsound ; 			// Sequencer, player or clip
   private Object listener = null ;			// Last listener added
   private Object metalistener = null ;	// Internal sequencer listener
   private Object linelistener = null ;	// Internal line listener

	// Constructor for when we do not have a configuration.

	public AudioSound(ArchiveFile zip, String file)
	{
		me = this ;
		setUniqueID(new Integer(this.hashCode())) ;
		init(zip,file,null) ;
	}

	// Constructor

	public AudioSound(ArchiveFile zip, String file, Configuration ref)
	{
		me = this ;
		init(zip,file,ref) ;
	}


	// Object state reference methods
	// ------------------------------

	// Method to return the medi sequencer.

	Object getPlayer() { return (error) ? null : currentsound ; }

	// Method to return the content descriptor.

	Object getContentDescriptor() { return (error) ? null : cd ; }

   // Method to return the last listener object added.


   Object getListener() { return listener ; }


	// Method to return the content type.

	String getContentType()
	{
		String s = (String) getContentType(getName()) ;
		if (s == null) return super.getContentType() ;
		return s ;
	}

	// Sequencer type.

	boolean isSequencer() { return (currentsound instanceof Sequencer) ; }

	// Clip type.

	boolean isClip() { return (currentsound instanceof Clip) ; }

	// Return the object state.

	String getState()
	{
		String state = super.getState() ;
		if (isSequencer())
		{
			Sequencer p = (Sequencer) currentsound ;
			if (p.isOpen())
            state = Kisekae.getCaptions().getString("MediaPrefetchedState") ;
			if (p.isRecording())
            state = Kisekae.getCaptions().getString("MediaRecordingState") ;
			if (p.isRunning())
            state = Kisekae.getCaptions().getString("MediaStartedState") ;
		}
		if (isClip())
		{
			Clip p = (Clip) currentsound ;
			if (p.isOpen())
            state = Kisekae.getCaptions().getString("MediaPrefetchedState") ;
			if (p.isRunning())
            state = Kisekae.getCaptions().getString("MediaStartedState") ;
		}
		return state ;
	}

	// Return the object position.

	int getPosition()
	{
		int position = super.getPosition() ;
		if (isSequencer())
		{
			Sequencer p = (Sequencer) currentsound ;
			position = (int) (p.getMicrosecondPosition() / 1000000.0) ;
		}
		if (isClip())
		{
			Clip p = (Clip) currentsound ;
			position = (int) (p.getMicrosecondPosition() / 1000000.0) ;
		}
		return position ;
	}

	// Return the object duration.

	int getDuration()
	{
		int duration = super.getDuration() ;
		if (isSequencer())
		{
			Sequencer p = (Sequencer) currentsound ;
			duration = (int) (p.getMicrosecondLength() / 1000000.0) ;
		}
		if (isClip())
		{
			Clip p = (Clip) currentsound ;
			duration = (int) (p.getMicrosecondLength() / 1000000.0) ;
		}
      if (duration == 0 && (framesize == 0 || framerate == 0))
      {
        	InputStream is = getInputStream() ;
         if (is != null) 
         {
            try 
            {
               AudioInputStream ais = AudioSystem.getAudioInputStream(is) ;
               audiofmt = (ais != null) ? ais.getFormat() : null ;
               framesize = (audiofmt != null) ? audiofmt.getFrameSize() : 0 ;
               framerate = (audiofmt != null) ? audiofmt.getFrameRate() : 0 ;
            }
            catch (Exception e) { }   
         }      
      }
      if (duration == 0 && framesize > 0 && framerate > 0)
      {
         duration = (int) (getBytes() / (framesize * framerate)) ;         
      }
		return duration ;
	}



	// Object loading methods
	// ----------------------


	// Audio player initialization.

	void init()
	{
		started = false ;
		if (error || (cache && b == null)) return ;
		if ("".equals(getPath())) return ;
      if (!OptionsDialog.getCacheAudio() && zip != null) zip.connect() ;
		if (OptionsDialog.getDebugSound())
			System.out.println("AudioSound: " + getName() + " Initialization request.") ;
   }



	// Audio player open. 	Create a sound data source from the memory array.

	void open()
	{
		if (error || (cache && b == null)) return ;
		if ("".equals(getPath())) return ;
		if (!OptionsDialog.getJavaSound()) return ;
		if (OptionsDialog.getDebugSound())
			System.out.println("AudioSound: " + getName() + " Open request.") ;

		// Midi files do not play properly using JMF when the application is
		// loaded from a jar file.  We use Java Sound for playback if the Java
		// Sound option is set.

   	try
   	{
         if (opened)
         {
            if (currentsound instanceof Clip)
            {
               Clip clip = (Clip) currentsound ;
               clip.setMicrosecondPosition(0) ;
               return ;
            }
            if (currentsound instanceof Sequencer)
            {
               Sequencer sequencer = (Sequencer) currentsound ;
               sequencer.setMicrosecondPosition(0) ;
               return ;
            }
         }
         
         // Actually open the sound file. Get an input stream and create
         // a player.
         
        	InputStream is = getInputStream() ;
         if (is == null) return ;
         try
         {
            if (!ArchiveFile.isMP3Sound(getName()))
                currentsound = AudioSystem.getAudioInputStream(is) ;
            else if (ArchiveFile.isMP3Sound(getName()) && Kisekae.isMP3Installed())
                currentsound = is ;     
            else
               throw new Exception("unknown sound type") ;
            if (OptionsDialog.getDebugSound())
               System.out.println("AudioSound: " + getName() + " is a recognized stream") ;
         }
         catch (Exception ex)
   		{  // load midi & rmf as inputstreams for now.
            if (OptionsDialog.getDebugSound())
            {
 					System.out.println("AudioSound: " + getName() + " " + ex) ;
 					System.out.println("AudioSound: " + getName() + " attempt recovery, assuming sequencer type") ;
            }
	         currentsound = new BufferedInputStream(is,4096) ;
         }

         // Decode the sound type.  WAV and AU and such.

			if (currentsound instanceof AudioInputStream)
         {
				try
            {
					AudioInputStream stream = (AudioInputStream) currentsound;
					AudioFormat format = stream.getFormat() ;
               framesize = format.getFrameSize() ;
               framerate = format.getFrameRate() ;

					// we can't yet open the device for ALAW/ULAW playback,
					// convert ALAW/ULAW to PCM

					if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
						(format.getEncoding() == AudioFormat.Encoding.ALAW)) 
					{
						AudioFormat tmp = new AudioFormat(
   					AudioFormat.Encoding.PCM_SIGNED,
						format.getSampleRate(),
						format.getSampleSizeInBits() * 2,
						format.getChannels(),
						format.getFrameSize() * 2,
						format.getFrameRate(),
						true);
                  stream = AudioSystem.getAudioInputStream(tmp, stream);
                  format = tmp;
               }

               // Get the data line information.

					DataLine.Info info = new DataLine.Info(
					   Clip.class, stream.getFormat(),
					   ((int) stream.getFrameLength() * format.getFrameSize())) ;

				   // Create a sound clip.

				   Clip clip = (Clip) AudioSystem.getLine(info) ;
               linelistener = new ClipListener() ;
				   clip.addLineListener((LineListener) linelistener);
				   clip.open(stream);
				   currentsound = clip;
				   if (OptionsDialog.getDebugSound())
					   System.out.println("AudioSound: " + getName() + " Sound clip opened") ;
               opened = true ;
               return ;
				}
            catch (Exception ex)
            {
              	error = true ;
					currentsound = null ;
					showError("AudioSound: " + getName() + " Sound clip stream exception") ;
					ex.printStackTrace() ;
				}
			}

         // We do not have a known Java sound.  Try MIDI or RMF.  If we
			// fail on this and JMF is installed, try to allocate a JMF player.
         // The JMF recovery must occur by the caller (FKissAction) as this 
         // AudioSound object will be set with an error state.  

			else if (currentsound instanceof BufferedInputStream)
         {
				try
            {
					Sequencer sequencer = MidiSystem.getSequencer() ;
					sequencer.open() ;
					sequencer.setSequence((BufferedInputStream) currentsound) ;
					if (OptionsDialog.getDebugSound())
						System.out.println("AudioSound: " + getName() + " Sequencer opened") ;

               currentsound = sequencer ;
               metalistener = new SequencerListener() ;
					sequencer.addMetaEventListener((MetaEventListener) metalistener) ;
               opened = true ;
					return ;
				}

				catch (Exception e)
				{
		        	error = true ;
               format = true ;
					currentsound = null;
              	if (!Kisekae.isMediaInstalled())
               {
        				showError("AudioSound: " + getName() + " not a recognized audio format") ;
               }
               else
               {
  						if (OptionsDialog.getDebugSound())
                  {
                     System.out.println("AudioSound: " + getName() + " " + e) ;
  							System.out.println("AudioSound: " + getName() + " attempt JMF recovery") ;
                  }
               }
				}
         }

         // Decode the sound type.  MP3 files.

         else if (currentsound instanceof InputStream)
         {
				try
            {
					InputStream stream = (InputStream) currentsound;
				   Mp3Clip clip = new Mp3Clip(this,stream) ;
               linelistener = new ClipListener() ;
			      clip.addLineListener((LineListener) linelistener);
               clip.open() ;  
               currentsound = clip ;
				   if (OptionsDialog.getDebugSound())
					   System.out.println("AudioSound: " + getName() + " MP3 player opened") ;
               opened = true ;
               return ;
				}
            catch (Exception ex)
            {
              	error = true ;
					currentsound = null ;
					showError("AudioSound: " + getName() + " Sound clip stream exception, " + ex) ;
				}
			}

		}
      catch (Exception e)
      {
        	error = true ;
			currentsound = null;
         showError("AudioSound: " + getName() + " Sound stream exception") ;
        	e.printStackTrace() ;
      }
	}



	// Method to add a listener to our player object.

	void addListener(Object ce)
	{
   	listener = ce ;
		if (ce instanceof MetaEventListener && currentsound instanceof Sequencer)
        	((Sequencer) currentsound).addMetaEventListener((MetaEventListener) ce) ;
		if (ce instanceof LineListener && currentsound instanceof Clip)
       	((Clip) currentsound).addLineListener((LineListener) ce) ;
   }



   // Method to remove a listener from our player object.

   void removeListener(Object ce)
   {
		if (ce instanceof MetaEventListener && currentsound instanceof Sequencer)
        	((Sequencer) currentsound).removeMetaEventListener((MetaEventListener) ce) ;
		if (ce instanceof LineListener && currentsound instanceof Clip)
       	((Clip) currentsound).removeLineListener((LineListener) ce) ;
	}



	// Method to play the audio file.   This method starts the audio from
	// from the beginning because open() request resets the audio position.
   // For JMF players, if the player is currently running we stop it.  
   // This routine can be reinvoked when the stop event is recognized for
   // repeated playback.
   
   // Note: Audio did not always play with Java 1.5 when we reset the media 
   // position so we reopen our audio sound to force a new initialization.
   // Sept 2008 - a close and open can take an excessive time for animation.

	void play()
	{
      if (!loaded) return ;
		if (!OptionsDialog.getSoundOn()) return ;
		if (OptionsDialog.getDebugSound())
			System.out.println("AudioSound: " + getName() + " Play request.") ;

		try
		{
			if (format) throw new KissException("unknown media format") ;
			if (error) throw new KissException("audio object in error") ;
			if (!players.contains(me)) players.addElement(me) ;
         
         // Open the audio file.  This will reset the audio position if the
         // file has been previously opened, or establish the appropriate 
         // clip or sequencer if unopened.
         
         open() ;

	      // Midi files may not play properly if a soundbank is not installed.
         // We use Java Sound playback.  The sequencer events for start and
         // stop do not fire when the position is changed so we explicitly
         // check for mediaplayer callback events.

         if (currentsound instanceof Sequencer)
         {
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " Sequencer started") ;
				Sequencer sequencer = (Sequencer) currentsound ;
            doCallback() ;
            started = false ;
         	sequencer.start() ;
            doCallstart() ;
            if (sequencer.isRunning()) started = true ;
            return ;
         }

         // Java sound can be standard media, too.  In this case resetting the
         // media position appears to fire stop and start events.

			if (currentsound instanceof Clip)
         {
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " Sound clip started") ;
				Clip clip = (Clip) currentsound ;
				clip.start() ;
            if (clip instanceof Mp3Clip)
               cd = ((Mp3Clip) clip).getContentDescriptor() ;
            else
            {
               AudioFormat fmt = clip.getFormat() ;
               if (fmt != null) cd = fmt.toString() ;
            }
            return ;
			}
		}

		catch (Exception e)
		{
			showError("Audio " + getName() + " start fault " + e.getMessage()) ;
         System.out.println("AudioSound: " + getName() + " start fault " + e.getMessage()) ;
			if (!(e instanceof KissException)) e.printStackTrace();
		}
	}


	// Static method to stop playing any active audio files.  If this
	// method is called with no parameter all audio files are stopped.
	// If called with a configuration then all audio files associated
	// with the configuration are stopped.  If called with an audio object
	// then only that audio object is stopped.  The type parameter filters
   // by sound, music, or mediaplayer classes.

	static void stop(Configuration c, Audio a, String type)
	{
		for (int i = players.size()-1 ; i >= 0 ; i--)
		{
			Audio audio = (Audio) players.elementAt(i) ;
			if (a != null && a != audio) continue ;
         if (!(audio instanceof AudioSound)) continue ;

			// Check for configuration archive file agreement.  If the object
         // configuration reference equals the specified configuration
         // reference the the audio object was established at configuration
         // load time.  This would exclude media player objects.

			if (c != null)
			{
				ArchiveFile configzip = c.getZipFile() ;
				ArchiveFile audiozip = audio.getZipFile() ;
				if (!(configzip == audiozip)) continue ;
			}

         // Check for type agreement.

         if (type != null)
         {
            String audiotype = audio.getType() ;
            if (audiotype != null && !type.equals(audiotype)) continue ;
         }

			// Get the player and/or midi sequencer.

			if (OptionsDialog.getDebugSound())
				System.out.println("AudioSound: " + audio.getName() + " Stop request.") ;
         Object currentsound = audio.getPlayer() ;

			// Shut the sequencer down.  We have to do a manual callback request
         // as the sequencer event listener does not recognize stop events.

			if (currentsound instanceof Sequencer)
			{
				try
				{
					if (OptionsDialog.getDebugSound())
						System.out.println("AudioSound: " + audio.getName() + " Sequencer stopped") ;
					Sequencer sequencer = (Sequencer) currentsound ;
               if (sequencer.isRunning())
               {
                  sequencer.stop() ;
                  audio.doCallback() ;
                  audio.started = false ;
               }
            }
				catch (Exception e)
				{
					System.out.println("Audio sequencer stop fault.");
					if (!(e instanceof KissException)) e.printStackTrace();
				}
         }

			// Shut the sound clip down.  The sound clip event listener will
         // fire any callback events when the clip stops.

			if (currentsound instanceof Clip)
			{
				try
				{
					if (OptionsDialog.getDebugSound())
						System.out.println("AudioSound: " + audio.getName() + " Sound clip stopped") ;
					Clip clip = (Clip) currentsound;
	         	if (clip.isRunning()) clip.stop() ;
            }
				catch (Exception e)
				{
					System.out.println("Audio clip stop fault.");
					if (!(e instanceof KissException)) e.printStackTrace();
				}
         }

			// Remove the player from our active list.

         audio.setRepeat(0) ;
         audio.setType(null) ;
			players.remove(audio) ;
		}
	}


	// Method to close down our audio player.

	void close()
   {
      closeaudio() ;
      flush() ;
   }
   
	private void closeaudio()
	{
      opened = false ;
      if (!OptionsDialog.getCacheAudio() && zip != null)
      {
      	System.out.println("AudioSound: " + getName() + " call zip.disconnect()") ;
         zip.disconnect() ;
      }
//		if (currentsound == null) return ;

		// Look for our player in the active play list.  If we find it,
		// remove it and close the player down.

		if (OptionsDialog.getDebugSound())
      	System.out.println("AudioSound: " + getName() + " Close request.") ;
		players.removeElement(me) ;
      removeListener(listener) ;
      removeListener(metalistener) ;
      removeListener(linelistener) ;

		// Close the player.

		try
		{
			if (currentsound instanceof Sequencer)
			{
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " Sequencer closed") ;
            Sequencer sequencer = (Sequencer) currentsound ;
				if (sequencer.isRunning()) sequencer.stop() ;
				if (sequencer.isOpen()) sequencer.close() ;
			}

			if (currentsound instanceof Clip)
			{
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " Sound clip closed") ;
				Clip clip = (Clip) currentsound ;
         	if (clip.isRunning()) clip.stop() ;
				if (clip.isOpen()) clip.close() ;
         }
		}
		catch (Exception e)
		{
			showError("Audio " + getPath() + " close fault.") ;
			if (!(e instanceof KissException)) e.printStackTrace();
	   }
      
      currentsound = null ;
      metalistener = null ;
      linelistener = null ;
      listener = null ;
   }

   
   // Release critical resources.

   void flush()
   {
		b = null ;
		ds = null ;
		loaded = false ;
      opened = false ;
      hascallback = false ;
      callback = null ;
      listener = null ;
      metalistener = null ;
      linelistener = null ;
      currentsound = null ;
	}


	// Method to determine the media content type.

	Object getContentType(String filename)
	{
		int i = filename.lastIndexOf(".");
		String ext = (i > 0) ? filename.substring(i+1).toLowerCase() : "" ;
		String ct = "" ;
		if (ext.equals("mid") || ext.equals("midi"))
			ct = "audio/midi" ;
		else if (ext.equals("rmf"))
			ct = "audio/rmf" ;
		else if (ext.equals("gsm"))
			ct = "audio/x_gsm" ;
		else if (ext.equals("aiff") || ext.equals("aif"))
			ct = "audio/x_aiff" ;
		else if (ext.equals("mp2"))
			ct = "audio/x-mpegaudio" ;
		else if (ext.equals("mp3"))
			ct = "audio/mpeg" ;
		else if (ext.equals("wav"))
			ct = "audio/x-wav" ;
		else if (ext.equals("au"))
			ct = "audio/basic" ;
		return ct ;
	}

	// Inner class to catch sound clip events.

	class ClipListener implements LineListener
	{
		public void update(LineEvent event)
		{
			// Stop events occurs when the media file has played till the end.

			if (event.getType() == LineEvent.Type.STOP)
			{
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " ClipStopEvent") ;

				// Start the player in a new thread as player initiation can take
				// time.  This frees the Player thread.

				if (repeat)
				{
					if (OptionsDialog.getDebugSound())
               	System.out.println("AudioSound: " + getName() + " Repeat count = " + repeatcount) ;
					if (repeatcount > 0) repeatcount-- ;
					repeat = (repeatcount != 0) ;
					Runnable runner = new Runnable()
					{ public void run() { play() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
				}
				else
            {
					doCallback() ;
            	started = false ;
            }
			}


			// The Close Event occurs when a clip is closed.

			else if (event.getType() == LineEvent.Type.CLOSE)
			{
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " ClipCloseEvent") ;
			}

			else if (event.getType() == LineEvent.Type.START)
			{
				doCallstart() ;
				started = true ;
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " ClipStartEvent") ;
			}

			else if (event.getType() == LineEvent.Type.OPEN)
			{
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " ClipOpenEvent") ;
	      }
      }
   }


	// Inner class to catch sound sequencer events.

	class SequencerListener implements MetaEventListener
	{
		public void meta(MetaMessage message)
      {
			if (message.getType() == 47)
         {  // 47 is end of track
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " MidiEndTrackEvent " + message.getType()) ;

				// Start the player in a new thread as player initiation can take
				// time.  This frees the Player thread.

				if (repeat)
				{
					if (OptionsDialog.getDebugSound())
               	System.out.println("AudioSound: " + getName() + " Repeat count = " + repeatcount) ;
					if (repeatcount > 0) repeatcount-- ;
					repeat = (repeatcount != 0) ;
					Runnable runner = new Runnable()
					{ public void run() { play() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
				}
				else
            {
					doCallback() ;
            	started = false ;
            }
			}

	      // Any other meta event will signal a start event.

			else
	      {  // 00 is start of track (optional)
	        	// 02 is copyright info (optional)
	         // 03 is sequence or track name (optional)
	         // 04 is instrument name (optional)
            if (message.getType() == 2)
               copyright = new String(message.getData()) ;
	         if (started) return ;
				if (OptionsDialog.getDebugSound())
					System.out.println("AudioSound: " + getName() + " MidiStartTrackEvent " + message.getType()) ;
				doCallstart() ;
	         started = true ;
         }
      }
   }
}

