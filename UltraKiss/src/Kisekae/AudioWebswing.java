package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      4.0  (January 7, 2025)
// Copyright:    Copyright (c) 2002-2025
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
* AudioWebswing class
*
* Purpose:
*
* This class extends the AudioSound class to provide support for sound
* while running under webswing.  
* 
*/


import static Kisekae.Audio.lock;
import static Kisekae.Audio.players;
import java.io.* ;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.* ;


final class AudioWebswing extends AudioSound
{
	private Object cd = null;					// The audio content descriptor
   private final Object waithold = new Object() ; 

  
	// Constructor for when we do not have a configuration.

	public AudioWebswing(ArchiveFile zip, String file)
	{
      super(zip,file) ;
	}

	// Constructor

	public AudioWebswing(ArchiveFile zip, String file, Configuration ref)
	{
      super(zip,file,ref) ;
	} 
   
   
	// Audio player open. 	Create a sound data source from the memory array.
   // For webswing we prepare a clip
   

	void open()
	{
		if (error || (cache && b == null)) return ;
		if ("".equals(getPath())) return ;
		if (!OptionsDialog.getJavaSound()) return ;
      long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
		if (OptionsDialog.getDebugSound())
			PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Open request.") ;

		// Midi files do not play properly using JMF when the application is
		// loaded from a jar file.  We use Java Sound for playback if the Java
		// Sound option is set.

   	try
   	{
         
         // Actually open the sound file. Get an input stream and create
         // a player.
         
         InputStream is = getInputStream() ;
         if (cache) is.reset() ;
         
         Clip clip = prepareClip() ;
         currentsound = clip ;
         if (clip == null) return ;
         linelistener = new ClipListener() ;
		   clip.addLineListener((LineListener) linelistener);
         
         setOpenTime(time) ;
         setCloseTime(0) ;
       	is = getInputStream() ;
         if (is == null) return ;
         
         time = System.currentTimeMillis() - Configuration.getTimestamp() ;
 		   if (OptionsDialog.getDebugSound())
			   PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Sound clip opened (webswing)") ;
         loaded = true ;
		}
      catch (Exception e)
      {
        	error = true ;
			currentsound = null;
         PrintLn.println("AudioWebswing: " + getName() + " Sound stream exception, " + e.toString())  ;
      }
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
      if (!loaded && OptionsDialog.getCacheAudio()) return ;
		if (!OptionsDialog.getSoundOn()) return ;

      // Run the sound playback activity in a separate thread so as to not
      // interfere with the event handler FKissAction processing.  

      playcount++ ;
      Runnable runner = new Runnable()
      { public void run() { play1() ; } } ;
      Thread runthread = new Thread(runner) ;
      runthread.setName("AudioWebswing play " + getName());
      runthread.start() ;
   }
   
	void play1()
   {
      long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
		if (OptionsDialog.getDebugSound())
		   PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Play request.") ;

      setStartTime(time) ;      
      setStopTime(0) ;    
      
      // Wait for completion if we are already stopping.
      
      if (this.isStopping()) 
      {
         synchronized(waithold) 
         {            
            try 
            { 
               time = System.currentTimeMillis() - Configuration.getTimestamp() ;
         		if (OptionsDialog.getDebugSound())
         		   PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Waiting for stop to complete") ;
               waithold.wait(500) ; 
            }
            catch (InterruptedException e) { }  
            setStopping(false) ;
            time = System.currentTimeMillis() - Configuration.getTimestamp() ;
       		if (OptionsDialog.getDebugSound())
       		   PrintLn.println("[" + time + "] AudioWebswing: " + getName()  + " [" + playcount + "]" + " Resuming play request") ;
         }
      }

      // Now open the audio and start playing the sound.
      
      lock.lock() ;
      try
		{
			if (format) throw new KissException("unknown media format") ;
			if (error) throw new KissException("audio object in error") ;
			if (!players.contains(me)) 
         {
       		if (OptionsDialog.getDebugSound())
       		   PrintLn.println("[" + time + "] AudioWebswing: " + getName()  + " adding myself to players") ;
            players.addElement(me) ;
         }
         
         // Open the audio file.  This will reset the audio position if the
         // file has been previously opened, or establish the appropriate 
         // clip or sequencer if unopened.
         
         open() ;
			if (error) throw new KissException("audio object in error on open") ;

         // Java sound can be standard media, too.  In this case resetting the
         // media position appears to fire stop and start events.

			if (currentsound instanceof Clip)
         {
            time = System.currentTimeMillis() - Configuration.getTimestamp() ;
				if (OptionsDialog.getDebugSound())
					PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Clip started") ;
				Clip clip = (Clip) currentsound ;
            started = false ;
            if (isLooping() || (getBackground() && OptionsDialog.getAutoMediaLoop()))
            {
               clip.loop(Clip.LOOP_CONTINUOUSLY);
               time = System.currentTimeMillis() - Configuration.getTimestamp() ;
               if (OptionsDialog.getDebugSound())
                  PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " Clip is looping.") ;
            }
            
				clip.start() ;
            AudioFormat fmt = clip.getFormat() ;
            if (fmt != null) cd = fmt.toString() ;
			}
		}

		catch (Exception e)
		{
 			showError("AudioWebswing: " + getName() + " start fault " + e.getMessage()) ;
         e.printStackTrace();
		}
      finally { lock.unlock() ; }
	}
   
   // Webswing interface.  Clip must be closed when stopped otherwise 
   // line resources will not be released.
   
   private Clip prepareClip()
   {
      try
      {
         InputStream is = getInputStream() ;
         if (is == null) throw new KissException("null input stream") ;
         AudioInputStream ais = AudioSystem.getAudioInputStream(is) ;
         Clip clip = AudioSystem.getClip() ;
         currentsound = clip ;
// causes webswing to hang?
// Linux reports NoLineAvailableException at times if line not closed?
/*
         clip.addLineListener(event -> {
            if (LineEvent.Type.STOP.equals(event.getType())) 
            {
               long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
               if (OptionsDialog.getDebugSound())
                  PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " [" + playcount + "]" + " prepareClip close on stop event.") ;
               event.getLine().close();
            }
         });
*/
         clip.open(ais) ;
         return clip ;
      }
      catch (Exception e)
      {
         long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
         PrintLn.println("[" + time + "] AudioWebswing: " + getName() + " prepareClip, " + e.toString()) ;   
      }
      return null ;
   }
}
