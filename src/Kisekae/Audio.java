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
* This class is an abstract class for KiSS audio files.  Extensions
* of this class define specific audio file types.  These may be Java Sound
* files or Java Media Framework files.
*
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.util.Hashtable ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.net.URL ;
import javax.swing.JButton ;
import javax.sound.sampled.* ;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Collections;
import java.util.Comparator;


abstract class Audio extends KissObject
{
	// Class attributes.  Sized for 256 audio objects.
   // Reentrant lock for players changes by concurrent stop/play activties

	static private Hashtable key = new Hashtable(300,0.855f) ;
	static protected Vector players = new Vector() ;
   static protected final Lock lock = new ReentrantLock();

	// Audio attributes

	protected Audio me = null ;					// Reference to ourselves
	protected byte b[] = null ;					// The audio file data
	protected int line = 0 ;						// The configuration file line
	protected int bytes = 0 ;						// The file size in bytes
	protected int repeatcount = 0 ;				// The repetition count
   protected String copyright = null ;       // Midi file copyright
   protected String type = null ;            // Audio play type (sound, music)
   protected AudioFormat audiofmt = null ;   // Audio format from inputstream
   protected int framesize = 0 ;             // Audio frame size from format
   protected float framerate = 0 ;           // Audio frame rate from format
   protected int playcount = 0 ;             // Number of play requests
   protected String includename = null ;     // Name of include file used
   protected InputStream is = null ;         // Audio input data stream
   protected long starttime = 0 ;            // Time when playback started
   protected long stoptime = 0 ;             // Time when playback stopped
   protected long opentime = 0 ;             // Time when the audio is opened
   protected long closetime = 0 ;            // Time when the audio is closed

	// State attributes

	protected boolean loaded = false ;			// True if data has been loaded
	protected boolean frominclude = false ;	// True if data from include file
	protected boolean opened = false ;			// True if sound has been opened
	protected boolean realized = false ;		// True if player is realized
	protected boolean copy = false ;				// True if file is a copy
	protected boolean cache = false ;			// True if load media to memory
	protected boolean started = false ;			// True if player ever started
	protected boolean repeat = false ;			// True if audio repeats
	protected boolean restart = false ;			// True if restart play after stop
	protected boolean hascallback = false ;	// True if callback listener set
	protected boolean format = false ;	      // True if unknown media format
   protected boolean converted = false ;     // True if converted to JMF
   protected boolean background = false ;    // True if media player background
   protected boolean stopping = false ;      // True if audio is being stopped

	// Our end of media callback button that other components can attach
	// listeners to.

	protected CallbackButton callback = null ;


	// Object utility methods
	// ----------------------


	// Initialize.

	void init(ArchiveFile zip, String file, Configuration ref)
	{
		setZipFile(zip) ;
		this.file = convertSeparator(file) ;
		this.ref = ref ;

		// Decode the audio content based upon the file type.

		int n = file.lastIndexOf('.') ;
		extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
      callback = new CallbackButton(this,"Audio Callback") ;
	}


	// A utility function to fire any callback listeners.  Listeners are
	// registered internally for any function that needs to know when a
	// sound file stops.  The media player uses this to begin the next
	// file in a playlist.  We also use this to queue any media stop events.

	void doCallback()
	{
		Vector v = me.getEvent("mediastop") ;
		if (!isInternal() && started)
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
		if (!hascallback) return ;
		if (callback == null) return ;
		callback.doClick() ;
	}


	// A utility function to fire any audio start listeners.  We use this to
	// queue any media start events.

	void doCallstart()
	{
		Vector v = me.getEvent("mediastart") ;
		if (!isInternal() && !started)
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
	}


	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// file.

	static void clearTable() 
   { 
      players = new Vector() ;
      key.clear() ; 
   }
	static void clearTable(Object cid)
	{
      players = new Vector() ;
		if (cid == null) cid = new String("Unknown") ;
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}


	// Function to find an Audio object by parameter name.

	static Audio findAudio(String name, Configuration c)
	{
		if (name == null || name.length() == 0) return null ;

		// See if we have a literal name.

		if (name.charAt(0) == '\"')
		{
			int i = name.lastIndexOf('\"') ;
			if (i < 1) return null ;
			name = name.substring(1,i) ;
		}

		// Look for a variable name.

		else
		{
			Object o = c.getVariable().getValue(name,null) ;
			if (!(o instanceof String)) return null ;
			name = o.toString() ;
		}

		// Find the audio file.

		File f = new File(c.getDirectory(),name) ;
		name = f.getPath().toUpperCase() ;
		return (Audio) Audio.getByKey(Audio.getKeyTable(),c.getID(),name) ;
	}


	// Object state reference methods
	// ------------------------------

	// Method to return the audio data.

	byte [] getAudioData() { return b ; }

	// Method to return the audio file size.

	int getBytes() { return bytes ; }

	// Method to return the audio player object.

	Object getPlayer() { return null ; }

	// Method to return the content descriptor.

	Object getContentDescriptor() { return null ; }

	// Method to return the content type.

	String getContentType() { return "unknown" ; }

	// Method to return the control component.

	Component getControlPanelComponent() { return null ; }

	// Method to return the visual component.

	Component getVisualComponent() { return null ; }

	// Return the object state.

	String getState() { return Kisekae.getCaptions().getString("UnknownValueText") ; }

	// Return the audio type.

	String getType() { return type ; }

	// Return the object position.

	int getPosition() { return 0 ; }

	// Return the object duration.

	int getDuration() { return 0 ; }

	// Return the background media player indicator.

	boolean getBackground() { return background ; }

	// Return the object latency.

	int getLatency() { return 0 ; }

	// Return the repeat count.

	int getRepeat() { return repeatcount ; }

	// Return the playback start time.

	long getStartTime() { return starttime ; }

	// Set the playback start time.

	void setStartTime(long n) { starttime = n ; }

	// Return the playback stop time.

	long getStopTime() { return stoptime ; }

	// Set the playback stop time.

	void setStopTime(long n) { stoptime = n ; }

	// Return the object open time.

	long getOpenTime() { return opentime ; }

   // Set the time when the audio object is opened.

	void setOpenTime(long n) { opentime = n ; }

	// Return the object close time.

	long getCloseTime() { return closetime ; }

   // Set the time when the audio object is closed.

	void setCloseTime(long n) { closetime = n ; }

	// Return the copyright text.

	String getCopyright() { return copyright ; }

	// Return the name of the include file used to load data.

	String getIncludeFileName() { return includename ; }

	// The configuration file line number showing where this object was
	// first declared is used for diagnostic output messages.

	void setLine(int l) { if (line == 0) line = l ; }

	// Set the configuration ID for this audio file.

	void setID(Object id) { cid = id ; }

	// Set the repeat flag for this audio file.

	void setRepeat(int n) { repeatcount = n ; repeat = (repeatcount != 0) ; }

	// Set the audio type.

	void setType(String s) { type = s ; }

   // Set the loaded state.  

   void setLoaded(boolean b) { loaded = b ; }

   // Indicate if loaded from an include file.  

   void setFromInclude(boolean b) { frominclude = b ; }

	// Set the copy indicator.

	void setCopy(boolean b) { copy = b ; }

	// Set the stopping indicator.

	void setStopping(boolean b) { stopping = b ; }

	// Set the background media player indicator.

	void setBackground(boolean b) { background = b ; }

	// Set the media stop callback listener.

	void addCallbackListener(ActionListener l)
   {
   	callback.addActionListener(l) ;
      hascallback = true ;
   }

	// Return the audio image copy indicator.

	boolean isCopy() { return copy ; }

	// Return an indication if the audio object is realized.

	boolean isRealized() { return realized ; }

	// Return an indication if the audio object is cached.

	boolean isCached() { return (b != null && b.length > 0) ; }

	// Return an indication if the sound has been opened.

	boolean isOpen() { return opened ; }

	// Return an indication if the data has been loaded.

	boolean isLoaded() { return (loaded || copy) ; }

   // Return an indication if data was loaded from an include file.

   boolean isFromInclude() { return frominclude ; }

	// Return an indication if this sound is being stopped.

	boolean isStopping() { return stopping ; }

	// Return an indication if this sound is playing.

	boolean isStarted() { return started ; }

	// Return an indication if the audio repeats.

	boolean isRepeating() { return (repeatcount != 0) ; }

   // Return an indication if the audio loops.

	boolean isLooping() { return (repeatcount < 0) ; }

	// Return an indication if the medium is Java sound.

	boolean isJavaSound() { return false ; }

	// Return an indication if the medium is JMF sound.

	boolean isJavaMedia() { return false ; }

	// Return a writable file indicator.

	boolean isWritable() { return (!("".equals(getRelativeName()))) ; }

	// Method to write our data file to an output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
		if (error) return -1 ;
      if (b == null) 
      {
         InputStream is = getInputStream() ;
         if (is == null) return 0 ;
         bytes = 0 ;
   		byte buffer [] = new byte[10240] ;
   		while (true)
         {
           	int n = is.read(buffer,0,buffer.length) ;
   			if (n <= 0) break ;
   			bytes += n ;
            out.write(buffer,0,n);
            if (fw != null) fw.updateProgress(n) ;
         }
         return bytes ;
      }
      out.write(b) ;
      if (fw != null) fw.updateProgress(b.length) ;
      return b.length ;
	}
   
   // Method to return an input stream for this audio object.
   // If we have already established an input stream then return it.
   
   InputStream getInputStream()
   {
      InputStream input = null ;
      try
      {
         if (!cache) 
            input = (ze != null) ? ze.getInputStream() : null ;
         if (b != null) 
            input = new ByteArrayInputStream(b) ;
      }
      catch (IOException e) { input = null ; }
      return input ;
   }
   
   // Method to get last sound played.
   
   static String getLastAudio()
   {
      if (players == null) return null ;
      if (players.size() == 0) return null ;
      Vector v = (Vector) players.clone() ;
      Collections.sort(v, new Comparator() 
      {
         public int compare(Object o1, Object o2)
         {
            if (!(o1 instanceof Audio)) return 0 ;
            if (!(o2 instanceof Audio)) return 0 ;
            if (((Audio) o1).getStartTime() > ((Audio) o2).getStartTime()) return -1 ;
            if (((Audio) o1).getStartTime() < ((Audio) o2).getStartTime()) return 1 ;
            return 0 ;
         }
      }) ;
      Object o = v.elementAt(0) ;
      if (!(o instanceof Audio)) return null ;
      Audio a = (Audio) o ;
      return (a.getWriteName()) ;
   }



	// Object loading methods
	// ----------------------

	// Method to read the audio file.

	void load(Vector includefiles)
	{
		InputStream is = null ;
		cache = OptionsDialog.getCacheAudio() ;
      String name = getRelativeName() ;
      if (name != null) name = name.toUpperCase() ;

		// Load the file if another copy of the audio stream has not been
		// loaded.  If we have previously read the file use the prior
		// audio stream.

		Audio a = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,name) ;
		if (a != null && a.isLoaded()) loadCopy(a) ;
		if (isLoaded()) { copy = true ; return ; }
         
		// Load a reference copy if we are accessing the same zip file as our
      // reference configuration.  On new data sets we may not yet have known
      // paths to the files.

		if (ref != null && zip != null)
		{
      	ArchiveFile refzip = ref.getZipFile() ;
         String refpath = (refzip != null) ? refzip.getName() : null ;
         String zippath = zip.getName() ;
         if ((refpath == null && zippath == null) ||
         	(refpath != null && zippath != null && refpath.equals(zippath)))
         {
				a = (Audio) Audio.getByKey(Audio.getKeyTable(),ref.getID(),name) ;
				if (a != null && a.isLoaded())
	         {
               if ((cache && a.getAudioData() != null) || (!cache && a.getAudioData() == null))
               {
                  loadCopy(a) ;
                  copy = false ;
                  if (!a.isFromInclude()) zip.addEntry(ze) ;
                  zip.setUpdated(ze,a.isUpdated()) ;
                  return ;
               }
            }
         }
		}

		// Do not actually load unnamed files.  These files are used as a
		// signal to stop the player.

		if ("".equals(name))
		{
			loaded = true ;
			return ;
		}

		// Read the new audio file.

		try
		{
			if (zip != null) ze = zip.getEntry(file) ;

			// Load a reference copy if it exists.

			if (ze == null)
			{
				if (ref != null)
				{
					a = (Audio) Audio.getByKey(Audio.getKeyTable(),ref.getID(),name) ;
					if (a != null && a.isLoaded())
					{
                  if ((cache && a.getAudioData() != null) || (!cache && a.getAudioData() == null))
                  {
   						loadCopy(a) ;
                     copy = false ;
   						return ;
                  }
					}

      			// Load an unloaded copy if it exists in the reference file.

              	ArchiveFile refzip = ref.getZipFile() ;
               if (refzip != null && !refzip.isOpen()) refzip.open() ;
               ze = (refzip != null) ? refzip.getEntry(getPath()) : null ;
				}
			}

         // If we have not yet found the file, check the INCLUDE list.

         includename = null ;
         if (ze == null)
         {
            ze = searchIncludeList(includefiles,name) ;
            if (ze != null)
            {
               zip = ze.getZipFile() ;
               includename = (zip != null) ? zip.getFileName() : null ;
               setFromInclude(true) ;
            }
         }

			// Determine the uncompressed file size.

			bytes = (ze == null) ? 0 : (int) ze.getSize() ;
			if (loader != null)
         {
            String s = Kisekae.getCaptions().getString("FileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + file + s.substring(j1+1) ;
            loader.showFile(s + " [" + bytes + " bytes]" +
               ((includename != null) ? (" (" + includename + ")") : "") + 
               ((cache) ? " (cached)" : "")) ;
         }

			// Create the file input stream.

			is = (zip == null) ? null : zip.getInputStream(ze) ;
			if (is == null) throw new IOException("file not found") ;
			if (!cache) { loaded = false ; return ; }

			// Read the entire contents.

			int n = 0, len = 0 ;
			b = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;
			loaded = true ;
		}

		// Watch for I/O errors

		catch (IOException e)
		{
			error = true ;
			showError("I/O Exception, Audio " + file + ", " + e.getMessage()) ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; }
			catch (IOException e)
			{
				b = null ;
				error = true ;
				showError("I/O Exception, Audio " + file + ", " + e.getMessage()) ;
			}
			is = null ;
		}
	}


	// Load a copy of the audio data from the specified object.

	private void loadCopy(Audio a)
	{
		if (a == null) return ;
//		error = a.isError() ;
//		loaded = !error ;
//		if (error) return ;
		ze = a.getZipEntry() ;
		b = a.getAudioData() ;
		bytes = a.getBytes() ;
		setLastModified(a.lastModified()) ;
      setFromInclude(a.isFromInclude()) ;
      loaded = true ;
		if (loader != null)
      {
         String s = Kisekae.getCaptions().getString("FileNameText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + file + s.substring(j1+1) ;
         loader.showFile(s + " [" + bytes + " bytes]" + "(copy)") ;
      }
	}

   
   // Close the audio input stream.
   
   void closeInputStream() throws IOException
   {
      if (is == null) return ;
      is.close() ;
   }
   
   // Release critical resources.  We must retain the zip and ze entries
   // from the KissObject to re-open the audio object.

   void flush()
   {
      me = null ;
      is = null ;
      b = null ;
   }


	// Abstract methods
	// ----------------

	abstract void init() ;
	abstract void open() ;
	abstract void play() ;
	abstract void close() ;
	abstract void addListener(Object o) ;
   abstract void removeListener(Object o) ;
   abstract Object getListener() ;
	abstract Object getContentType(String filename) ;


	// The generic audio stop method.  The static stop methods in AudioSound and
   // AudioMedia loop throught the players list to stop all required sounds
   // based on the configuration, audio object, and type parameters.

	static void stop() { stop(null,null,null) ; }
	static void stop(Audio a) { stop(null,a,null) ; }
	static void stop(Configuration c) { stop(c,null,null) ; }
	static void stop(Configuration c, String t) { stop(c,null,t) ; }
	static void stop(Configuration c, Audio a, String t)
	{
      boolean stoppedmedia = false ;
      boolean stoppedsound = false ;
      lock.lock() ;
      try
      {
         Vector p = (Vector) players.clone() ;
         long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
   		for (int i = p.size()-1 ; i >= 0 ; i--)
   		{
    			Audio audio = (Audio) p.elementAt(i) ;
   			if (a != null && a != audio) continue ;
   			if (audio instanceof AudioMedia)
            {
         		if (OptionsDialog.getDebugSound())
         		   System.out.println("[" + time + "] Audio: about to stop media " + ((AudioMedia) audio).getName()) ;
   				if (!stoppedmedia) ((AudioMedia) audio).stop(c,a,t) ;
               stoppedmedia = true ;
            }
   			if (audio instanceof AudioSound)
            {
         		if (OptionsDialog.getDebugSound())
         		   System.out.println("[" + time + "] Audio: about to stop sound " + ((AudioSound) audio).getName()) ;
   				if (!stoppedsound) ((AudioSound) audio).stop(c,a,t) ;
                stoppedsound = true ;
            }
   		}
   		if (OptionsDialog.getDebugSound() && !(stoppedsound || stoppedmedia))
   		   System.out.println("[" + time + "] Audio: no sounds stopped.") ;
      }
      finally { lock.unlock() ; }
	}


	// Function to display a syntax error message.

	void showError(String s)
	{
   	errormessage = s ;
		if (line > 0) s = "[Line " + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
      else 
      {
         MainFrame frame = Kisekae.getMainFrame() ;
         frame.showStatus(errormessage) ;
         System.out.println(errormessage) ;
      }
	}


	// The toString method returns a string representation of this object.
	// This is the class name concatenated with the object identifier.

	public String toString()
	{ return super.toString() + " " + getName() ; }
}


