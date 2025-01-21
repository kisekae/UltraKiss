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


import java.awt.event.* ;
import java.io.InputStream;
import java.io.IOException;
import javax.sound.sampled.* ;
import java.util.Vector ;

public class Mp3Clip implements Clip
{
   private Audio audiosound ;
   private InputStream stream = null ;
   private Vector listeners = new Vector() ;
   private Mp3Player player = null ;
   private int frame = 0 ;                      // Media frame 
   private boolean stop = false ;               // True if requested player stop
   private boolean alive = false ;              // True if player active

    /** Creates a new instance of Mp3Clip */
   public Mp3Clip(Audio a, InputStream stream) 
   {
      this.stream = stream ;
      this.audiosound = a ;
   }
    
   public InputStream getInputStream() { return stream ; }
     
   public int getBytes() { return (audiosound != null) ? audiosound.getBytes() : 0 ; }
 
   public Object getContentDescriptor() { return (player != null) ? player.getContentDescriptor() : null ; } 

   public void addLineListener(LineListener listener) { listeners.addElement(listener) ; }
    
   public void removeLineListener(LineListener listener) { listeners.removeElement(listener) ; }
    
   public int available() { return 0 ; }

   public String getName() { return audiosound.getName() ; }
   
   public boolean terminate() { return stop ; }
    
   public void drain() { close() ; }
    
   public void flush() { close() ; }
    
   public int getBufferSize() { return 0 ; }
    
   public Control getControl(javax.sound.sampled.Control.Type control) { return null ; }
    
   public Control[] getControls() { return null ; }
    
   public AudioFormat getFormat() { return null ; }
     
   public int getFrameLength() { return (player != null) ? player.getFrameLength() : 0 ; }
    
   public int getFramePosition() 
   { 
      if (player != null && player.isComplete()) return getFrameLength() ;
      return (player != null) ? player.getFramePosition() : 0 ; 
   }
    
   public long getLongFramePosition() { return (player != null) ? player.getFramePosition() : 0 ; }
    
   public float getLevel() { return 0 ; }
    
   public javax.sound.sampled.Line.Info getLineInfo() { return null ; }
    
   public long getMicrosecondLength() { return (player != null) ? player.getMicrosecondLength() * 1000 : 0 ;  }
    
   public long getMicrosecondPosition() 
   {
      if (player != null && player.isComplete()) return getMicrosecondLength() ;
      return (player != null) ? player.getPosition() * 1000 : 0 ;
   }
    
   public boolean isActive() { return (player != null) ? !player.isComplete() && alive : false ; }
    
   public boolean isControlSupported(javax.sound.sampled.Control.Type control) { return false ; }
    
   public boolean isOpen() { return (player != null) ? !player.isClosed() : false ; }
    
   public boolean isRunning() { return alive ; }
   
   public void loop(int count) { }
    
   public void open(AudioFormat fmt) throws LineUnavailableException { open() ; }
        
   public void open(AudioInputStream stream) throws LineUnavailableException, IOException 
   { this.stream = stream ; open() ; }
    
   public void open(AudioFormat format, byte[] data, int offset, int bufferSize) throws LineUnavailableException 
   { open() ; }
    
   public void setFramePosition(int frames) { if (player != null && player.isComplete()) setMicrosecondPosition(0) ; }
    
   public void setLoopPoints(int start, int end) { }
    
   // Shut the player down and set it for another start request.  
   
   public synchronized void setMicrosecondPosition(long microseconds) 
   { 
      try 
      { 
         Vector ln = listeners ;
         listeners = null ;  // disable listener events
         stop() ;
         close() ;
//         if (stream != null) stream.close() ;
         stream = audiosound.getInputStream() ;
         listeners = ln ;   // enable listener events
     }
     catch (Exception e) 
     { 
        PrintLn.println("Mp3Clip: " + audiosound + " setMicrosecondPosition("+microseconds+") input stream exception, " + e) ;
     }
  }
    
   public void open() throws LineUnavailableException 
   {
      if (!Kisekae.isMP3Installed()) return ;
      if (listeners == null) return ;

      frame = 0 ;
      for (int i = 0 ; i < listeners.size() ; i++)
        ((LineListener) listeners.elementAt(i)).update(new LineEvent(this,LineEvent.Type.OPEN,0)) ; 
   }
    
   // Close the player.  This forces a player stop.
   
   public void close() 
   { 
      if (!Kisekae.isMP3Installed()) return ;
      if (player != null) player.close() ;
      player = null ;
      frame = 0 ;
      if (listeners == null) return ;
      for (int i = 0 ; i < listeners.size() ; i++)
         ((LineListener) listeners.elementAt(i)).update(new LineEvent(this,LineEvent.Type.CLOSE,0)) ; 
   }
    
   // Start the player and begin decoding the sound stream.
   
   public void start() 
   { 
      if (!Kisekae.isMP3Installed()) return ;
      stop = false ;
        
      // Create a thread to play the sound.

      try
      {
         player = new Mp3Player(this,stream) ; 
         player.setFramePosition(frame) ;
         player.start() ;
         alive = true ;
      }
      catch (Exception e) 
      {
         PrintLn.println("Mp3Clip: unable to start player, " + e) ;
      }
        
      // Signal player has started.

      if (listeners == null) return ;
      for (int i = 0 ; i < listeners.size() ; i++)
         ((LineListener) listeners.elementAt(i)).update(new LineEvent(this,LineEvent.Type.START,0)) ; 
   }
    
   // Stop the player immediately.
   
   public void stop() 
   {
      if (!Kisekae.isMP3Installed()) return ;
      stop = true ;
      try 
      {
         while (player != null && player.isAlive()) 
            Thread.currentThread().sleep(10) ;
      }
      catch (InterruptedException e) { }
      frame = (player != null) ? player.getFramePosition() : 0 ;
      alive = false ;
   }
    
   // Callback from the player when it stops.  
   
   public synchronized void stopThread() 
   { 
      if (!Kisekae.isMP3Installed()) return ;
      stop = true ;
      alive = false ;
      frame = (player != null) ? player.getFramePosition() : 0 ;
      if (listeners == null) return ;
      for (int i = 0 ; i < listeners.size() ; i++)
         ((LineListener) listeners.elementAt(i)).update(new LineEvent(this,LineEvent.Type.STOP,0)) ; 
   }
   
   
   // toString method to return content descriptor.
   
   public String toString() 
   { 
      Object o = getContentDescriptor() ;
      if (o == null) return "MP3 clip" ;
      return o.toString() ; 
   }
}
