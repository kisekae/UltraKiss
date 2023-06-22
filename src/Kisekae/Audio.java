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


abstract class Audio extends KissObject
{
	// Class attributes.  Sized for 256 audio objects.

	static private Hashtable key = new Hashtable(300,0.855f) ;
	static protected Vector players = new Vector() ;

	// Audio attributes

	protected Audio me = null ;					// Reference to ourselves
	protected byte b[] = null ;					// The audio file data
	protected int line = 0 ;						// The configuration file line
	protected int bytes = 0 ;						// The file size in bytes
	protected int repeatcount = 0 ;				// The repetition count
   protected String copyright = null ;       // Midi file copyright
   protected String type = null ;            // Audio play type (sound, music)

	// State attributes

	protected boolean loaded = false ;			// True if data has been loaded
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

	static void clearTable() { key.clear() ; }
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

	// Return the object latency.

	int getLatency() { return 0 ; }

	// Returnt the repeat count.

	int getRepeat() { return repeatcount ; }

	// Return the copyright text.

	String getCopyright() { return copyright ; }

	// The configuration file line number showing where this object was
	// first declared is used for diagnostic output messages.

	void setLine(int l) { if (line == 0) line = l ; }

	// Set the configuration ID for this audio file.

	void setID(Object id) { cid = id ; }

	// Set the repeat flag for this audio file.

	void setRepeat(int n) { repeatcount = n ; repeat = (repeatcount != 0) ; }

	// Set the audio type.

	void setType(String s) { type = s ; }

	// Set the copy indicator.

	void setCopy(boolean b) { copy = b ; }

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

	// Return an indication if the data has been loaded.

	boolean isLoaded() { return (loaded || copy) ; }

	// Return an indication if the audio repeats.

	boolean isRepeating() { return (repeatcount != 0) ; }

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
		if (b != null) out.write(b) ;
      if (fw != null) fw.updateProgress(b.length) ;
		return b.length ;
	}
   
   // Method to return an input stream for this audio object
   
   InputStream getInputStream()
   {
      try
      {
         if (!cache) return (ze != null) ? ze.getInputStream() : null ;
         if (b != null) return new ByteArrayInputStream(b) ;
      }
      catch (IOException e) { }
      return null ;
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
	         	loadCopy(a) ;
               copy = false ;
               zip.addEntry(ze) ;
               zip.setUpdated(ze,a.isUpdated()) ;
	            return ;
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
						loadCopy(a) ;
                  copy = false ;
						return ;
					}

      			// Load an unloaded copy if it exists in the reference file.

              	ArchiveFile refzip = ref.getZipFile() ;
               if (refzip != null && !refzip.isOpen()) refzip.open() ;
               ze = (refzip != null) ? refzip.getEntry(getPath()) : null ;
				}
			}

         // If we have not yet found the file, check the INCLUDE list.

         String includename = null ;
         if (ze == null)
         {
            ze = searchIncludeList(includefiles,name) ;
            if (ze != null)
            {
               zip = ze.getZipFile() ;
               includename = (zip != null) ? zip.getFileName() : null ;
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
               ((includename != null) ? (" (" + includename + ")") : "")) ;
         }

			// Create the file input stream.

			if (!cache) { loaded = true ; return ; }
			is = (zip == null) ? null : zip.getInputStream(ze) ;
			if (is == null) throw new IOException("file not found") ;

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
      loaded = true ;
		if (loader != null)
      {
         String s = Kisekae.getCaptions().getString("FileNameText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + file + s.substring(j1+1) ;
         loader.showFile(s + " [" + bytes + " bytes]") ;
      }
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


	// The generic audio stop method.

	static void stop() { stop(null,null,null) ; }
	static void stop(Audio a) { stop(null,a,null) ; }
	static void stop(Configuration c) { stop(c,null,null) ; }
	static void stop(Configuration c, String t) { stop(c,null,t) ; }
	static void stop(Configuration c, Audio a, String t)
	{
		Vector p = (Vector) players.clone() ;
		for (int i = p.size()-1 ; i >= 0 ; i--)
		{
			Audio audio = (Audio) p.elementAt(i) ;
			if (a != null && a != audio) continue ;
			if (audio instanceof AudioMedia)
				((AudioMedia) audio).stop(c,a,t) ;
			if (audio instanceof AudioSound)
				((AudioSound) audio).stop(c,a,t) ;
		}
	}


	// Function to display a syntax error message.

	void showError(String s)
	{
   	errormessage = s ;
      MainFrame frame = Kisekae.getMainFrame() ;
		if (line > 0) s = "[Line " + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
      else frame.showStatus(errormessage) ;
	}


	// The toString method returns a string representation of this object.
	// This is the class name concatenated with the object identifier.

	public String toString()
	{ return super.toString() + " " + getName() ; }
}


