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
* Configuration class
*
* Purpose:
*
* This class represents a configuration object.  A configuration
* contains the attributes of a KiSS data set and it is used to
* define the image cels, color palettes, and the action event model.
*
* Configuration objects use their configuration identifier as their access
* key.
*/


import java.io.* ;
import java.awt.* ;
import java.util.StringTokenizer ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.NoSuchElementException ;
import java.util.Comparator ;
import java.util.Collection ;
import java.util.Collections ;
import java.util.Hashtable ;
import java.util.Date ;
import java.util.Properties ;
import javax.swing.Timer ;


final class Configuration extends KissObject
{
	// Class attributes

	private static int count = 0 ;			// Count of class instances
	private static long createtime = 0 ;	// Activation time
	private static Hashtable key = new Hashtable(3,1.0f) ;
   private static Hashtable propertypool = new Hashtable() ;
   private static String [] sectionheadings =
   { ";[Screen Section]", ";[Palette Section]", ";[Cel Section]",
     ";[Page Set Section]", ";[FKiSS Section]", ";[Option Section]" } ;

	// Configuration file attributes

	private InputStream is = null ;			// The file input stream
	private BufferedReader f = null;			// The buffered input stream
	private String lastline = null ;			// The last line read
	private String priorline = null ;  		// The previous to last line read
	private byte b[] = null ;					// Memory file image
	private int line = 0 ;						// File line number
	private int bytes = 0 ;						// File size in size
	private int memory = -1 ;					// Memory size in bytes
   private int loadbytes = 0 ;				// Total bytes loaded
   private float sf = 1.0f ;              // Initial scaling factor

	// Configuration objects

	private Dimension screen = null ;		// Dimension of the current screen
   private Vector includefiles = null ;   // Set of expansion base files
	private Vector palettes = null ;			// Set of palettes
	private Vector cels = null ;				// Set of cels
	private Vector pages = null ;				// Set of pages
	private Vector groups = null ;			// Set of groups
	private Vector alarms = null ;			// Set of alarms
	private Vector labels = null ;			// Set of labels
	private Vector sounds = null ;			// Set of audio files
	private Vector movies = null ;			// Set of video files
	private Vector comps = null ;			   // Set of component cels
	private Vector frames = null ;  			// Set of animated cels
	private Vector celgroups = null ;		// Set of cel groups
	private Variable variable = null ;		// Variable storage
	private AlarmTimer timer = null ;		// Primary alarm timer
   private GifTimer animator = null ;		// Primary cel animator
	private EventHandler handler = null ;	// Primary event handler
	private MediaFrame mediaframe = null ;	// Primary media player
   private String properties = null ;     // Doll properties pool name
   private Hashtable variables = null ;   // FKiSS variables declared

	// Configuration item unique identifiers

	private int border = 0 ;					// Border color palette index
	private int importborder = -1 ;			// Imported palette border index
	private int celCount = 0 ;					// Cel identifier
	private int paletteCount = 0 ;			// Palette idetifier
	private int pageCount = 0 ;				// Page identifier
	private int audioCount = 0 ;				// Audio identifier
	private int videoCount = 0 ;				// Video identifier
   private int fkisslevel = 0 ;           // FKiSS specification level
   private int maxgroups = 0 ;            // Max group number defined
   private int maxcels = 0 ;              // Max number of cels created
   private int maxcelnumber = 0 ;         // Max cel number defined

   // Configuration parse lead comments

   private Vector leadingtext = null ;		// Lead text working storage
   private Vector trailingtext = null ;	// Trailing text working storage
   private Vector leadmemory = null ;		// Lead text for memory specification
   private Vector leadsize = null ;			// Lead text for size specification
   private Vector leadborder = null ;		// Lead text for border specification
   private Vector leadaction = null ;		// Lead text for action specification
   private Vector trailtoken = null ;		// Trailing text for next token read
   private String trailmemory = "" ;		// Trailing comment for memory spec
   private String trailborder = "" ;		// Trailing comment for border spec
   private String trailscreen = "" ;		// Trailing comment for screen spec
   private int [] celtabstops = null ;		// Cel format field positions

	// State attributes

	private boolean reread = false ;			// True if line is read again
	private boolean eventsection = false ;	// True if reading events
	private boolean newline = false ;		// True if read new line
   private boolean activated = false ;		// True if configuration is active
	private boolean copy = false ;			// True if file is a reload copy
	private boolean changed = false ;		// True if object structure changed
   private boolean rgbborder = false ;    // True if border is rgb value
   private boolean restartable = true ;   // True if configuration can restart
   private boolean optionchange = false ; // True if options have changed
   private boolean ckiss = false ;        // True if any cel is truecolor
   private boolean epalette = false ;     // True if palette file exceeds 256
   private boolean ultrakiss = false ;    // True if reading UltraKiss config
   private boolean directkiss = false ;   // True if DirectKiss syntax is found
   private boolean playfkiss = false ;    // True if PlayFKiss syntax is found


	// Constructor

	public Configuration()
	{
		count = count + 1 ;
		setID("CNF" + count) ;
      setUniqueID(new Integer(this.hashCode())) ;
		setIdentifier("" + count) ;

		// Establish all internal configuration tables.  These entries
		// are built during configuration initialization.

		cels = new Vector() ;
		groups = new Vector() ;
		celgroups = new Vector() ;
		palettes = new Vector() ;
		sounds = new Vector() ;
		movies = new Vector() ;
		comps = new Vector() ;
		pages = new Vector() ;
		alarms = new Vector() ;
		frames = new Vector() ;
		labels = new Vector() ;
		variable = new Variable() ;
      variables = new Hashtable() ;

		// Set the defaults.

		screen = new Dimension(800,600) ;
      leadingtext = new Vector() ;
      trailingtext = new Vector() ;
      leadmemory = new Vector() ;
      leadborder = new Vector() ;
      leadaction = new Vector() ;
      trailtoken = new Vector() ;
      leadsize = new Vector() ;
	}


	// Configuration object attribute methods
	// --------------------------------------

	// Method to return the complete list of cels.

	Vector getCels() { return cels ; }

	// Method to return the complete list of groups.

	Vector getGroups() { return groups ; }

	// Method to return the complete list of alarms.

	Vector getAlarms() { return alarms ; }

	// Method to return the complete list of audio files.

	Vector getSounds() { return sounds ; }

	// Method to return the complete list of video files.

	Vector getMovies() { return movies ; }

	// Method to return the complete list of Java component cels.

	Vector getComponents() { return comps ; }
   
	// Method to return the complete list of page sets.

	Vector getPages() { return pages ; }

	// Method to return the complete list of cel groups.

	Vector getCelGroups() { return celgroups ; }

	// Method to return the complete list of palette files.

	Vector getPalettes() { return palettes ; }

	// Method to return the complete list of animated files.

	Vector getAnimatedCels() { return frames ; }

	// Method to return the list of INCLUDE files.

	Vector getIncludeFiles() { return includefiles ; }

	// Method to return our variable object.

	Variable getVariable() { return variable ; }

	// Method to return our current property object name.

	String getPool() { return properties ; }

	// Method to set our current property object name.

	void setPool(String s) { properties = s ; }
   
	// Method to return our activation time.

	static long getTimestamp() { return createtime ; }


	// Configuration data set attribute methods
	// ----------------------------------------

	// Method to return the current screen dimensions.  We default to (800,600).

	Dimension getSize()
   { return (screen == null) ? new Dimension(800,600) : new Dimension(screen) ; }

	// Set the configuration dimensions.

	void setSize(Dimension d) { screen = d ; }

	// Return the configuration file size.

	int getBytes() { return (b == null) ? bytes : b.length ; }

	// Return the number of groups in this configuration.

	int getGroupCount() { return groups.size() ; }

	// Return the number of cels in this configuration.

	int getCelCount() { return cels.size() ; }

	// Return the FKiSS support level.

	int getFKissLevel() { return fkisslevel ; }

	// Return the number of non-internal cels in this configuration.

	int getActiveCelCount()
   {
   	int n = 0 ;
      for (int i = 0 ; i < cels.size() ; i++)
      	if (!((Cel) cels.elementAt(i)).isInternal()) n++ ;
   	return n ;
   }

	// Return the number of groups in this configuration.

	int getCelGroupCount() { return celgroups.size() ; }

	// Return the number of palettes in this configuration.

	int getPaletteCount() { return palettes.size() ; }

	// Return the number of pages in this configuration.

	int getPageCount() { return pages.size() ; }

	// Return the number of sounds in this configuration.

	int getSoundCount() { return sounds.size() ; }

	// Return the number of movies in this configuration.

	int getMovieCount() { return movies.size() ; }

	// Return the number of pages in this configuration.

	int getVariableCount() { return variable.getSize() ; }

	// Return the number of components in this configuration.

	int getComponentCount() { return comps.size() ; }

	// Return the maximum palette group number referenced in this 
   // configuration. This is the size of the largest palette file.

	int getPaletteGroupCount()
   {
      int ncolors = 0 ;
      for (int i = 0 ; i < palettes.size() ; i++)
      {
   		Palette p1 = (Palette) palettes.elementAt(i) ;
         int multipalettes = p1.getMultiPaletteCount() ;
         if (multipalettes > ncolors) ncolors = multipalettes ;
      }
      return ncolors ;
   }

	// Return the maximum group number referenced in this configuration.
   // This number includes groups possibly created and removed.

	int getMaxGroupNumber()
   {
   	int maxgroups = 0 ;
      Enumeration enum1 = Group.getKeyTable().elements() ;
      while (enum1.hasMoreElements())
      {
         Object o = enum1.nextElement() ;
         if (!(o instanceof Group)) continue ;
      	Group g = (Group) o ;
      	int n = ((Integer) g.getIdentifier()).intValue() ;
         if (n > maxgroups) maxgroups = n ;
      }
      return maxgroups ;
   }

	// Return an unused group number referenced in this configuration.
   // This number is a new group number that is larger than all groups
   // initially defined in the configuration.  The Group key table is
   // probed to find an available key value.

	int getAvailableGroupNumber()
   {
      int n = maxgroups ;
      for ( ; ; )
      {
         Group g = (Group) Group.getByKey(Group.getKeyTable(),cid,new Integer(++n)) ;
         if (g == null) break ;
      }
      return n ;
   }

   // Functions to adjust the maximum group number used to assign new group
   // numbers.  This number increases as groups are cloned and decreases
   // as they are destroyed.

   void incrementMaxGroups() { maxgroups++ ; }
   void decrementMaxGroups() { maxgroups-- ; }

	// Return the maximum cel number referenced in this configuration.

	int getMaxCelNumber() { return maxcelnumber ; }

	// Set the maximum cel number referenced in this configuration.
   // This number is calculated by scanning the current cel table
   // and includes cels possibly created and removed.

   void setMaxCelNumber()
   {
   	maxcelnumber = 0 ;
      Enumeration enum1 = Cel.getKeyTable().elements() ;
      while (enum1.hasMoreElements())
      {
         Object o = enum1.nextElement() ;
         if (o instanceof Cel)
         {
         	Cel c = (Cel) o ;
         	int n = ((Integer) c.getIdentifier()).intValue() ;
            if (n > maxcelnumber) maxcelnumber = n ;
         }
         if (o instanceof Vector)
         {
            Enumeration enum2 = ((Vector) o).elements() ;
            while (enum2.hasMoreElements())
            {
               Object o2 = enum2.nextElement() ;
               if (!(o2 instanceof Cel)) continue ;
            	Cel c = (Cel) o2 ;
            	int n = ((Integer) c.getIdentifier()).intValue() ;
               if (n > maxcelnumber) maxcelnumber = n ;
            }
         }
      }
   }

	// Set the maximum cel number referenced in this configuration
   // given a new cel number.  If this number exceeds the known maximum
   // we use this as our maximum value, otherwise we recompute a new value.

   void setMaxCelNumber(Integer n)
   {
      if (n == null) return ;
      int n1 = n.intValue() ;
      if (n1 > maxcelnumber)
         maxcelnumber = n1 ;
      else
         setMaxCelNumber() ;
   }

	// Return an unused cel number referenced in this configuration.
   // This number is a new cel number that is larger than all cels
   // initially defined in the configuration.  The Cel key table is
   // probed to find an available key value.

	int getAvailableCelNumber()
   {
      int n = maxcels ;
      for ( ; ; )
      {
         Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,new Integer(++n)) ;
         if (c == null) break ;
      }
      return n ;
   }

	// Return the initial configuration scaling factor.

	float getScaleFactor() { return sf ; }

	// Set the current configuration scaling factor.

	void setScaleFactor(float f) { sf = f ; }

	// Method to return a specific page set object.

	PageSet getPage(int p)
	{
   	if (p < 0) return null ;
   	return (p < pages.size()) ? (PageSet) pages.elementAt(p) : null ;
   }

	// Method to return a specific palette object.

	Palette getPalette(int p)
	{
   	if (p < 0) return null ;
   	return (p < palettes.size()) ? (Palette) palettes.elementAt(p) : null ;
   }

	// Method to return the configuration border color.  This color
	// is taken from the first palette defined in the first
	// configuration file.  If no palette exists, then we have a
	// truetype data set and the border value is a direct RGB color.
   // If the first configuration palette was imported then the importborder
   // index is used as the background index of the imported palette.

	Color getBorderColor()
	{
		Palette p = getPalette(0) ;
		if (p == null || rgbborder) 
      {
         if (!OptionsDialog.getPlayFKissCompatibility())
            return new Color(border) ;
         int r = (((border % 1024) / 32) * 36) % 256 ;
         int g = (((border % 32) / 4) * 36) % 256 ;
         int b = (((border % 4)) * 85) % 256 ;
         return new Color(r,g,b) ;
      }
      if (importborder >= 0) return (p.getColor(0,importborder)) ;
		return (p.getColor(0,border)) ;
	}

	// Method to return the configuration border color index.

	int getBorder() { return (importborder >= 0) ? importborder : border ; }

	// Method to set the configuration border color index.

	void setBorderIndex(int n) { border = n ; importborder = -1 ; rgbborder = false ; }

	// Method to set the configuration rgb border color.

	void setBorderRgb(int n) { border = n ; importborder = -1 ; rgbborder = true ; }

	// Method to set the configuration imported palette border index.

	void setImportBorderIndex(int n) { importborder = n ; rgbborder = false ; }


	// Configuration environment attribute methods
	// -------------------------------------------

	// Method to return our timer reference.

	AlarmTimer getTimer() { return timer ; }

	// Method to return our cel animator reference.

	GifTimer getAnimator() { return animator ; }

	// Method to return a reference to our primary event handler.

	EventHandler getEventHandler() { return handler ; }

	// Method to return a reference to our primary media player.

	MediaFrame getMediaFrame() { return mediaframe ; }

	// Return the total bytes loaded for this configuration.

	int getLoadBytes() { return loadbytes + bytes ; }

	// Return the cel format field sizes.

	int [] getCelTabStops() { return celtabstops ; }

	// Method to return a reference to our memory file image.

	byte [] getMemoryFile() { return b ; }

	// Method to set our cel animator reference.

	void setAnimator(GifTimer t) { animator = t ; }

	// Method to set a reference to our primary event handler.

	void setEventHandler(EventHandler h) { handler = h ; }

	// Method to update our memory file image and associated zip entry.

	void setMemoryFile(byte [] buffer) { setMemoryFile(buffer,null) ; }
	void setMemoryFile(byte [] buffer, ArchiveEntry ze)
   {
   	b = buffer ;
      setLastModified(System.currentTimeMillis()) ;
      if (ze != null)
      {
	      this.ze = ze ;
	      this.zip = ze.getZipFile() ;
			file = ze.getPath() ;
      }
   }

	// Method to set our edit change indicator.

	void setChanged(boolean b)
	{
		changed = b ;
		if (ze != null) ze.setUpdated(true) ;
	}

	// Method to set our option change indicator.

	void setOptionsChanged(boolean b) { optionchange = b ; }

	// Set the object update state.  This also sets the update state of
	// the associated archive entry and the possibly outdated entry
   // in the archive file contents.

	void setUpdated(boolean b)
	{
		super.setUpdated(b) ;
		if (!b) changed = false ;
      if (b) restartable = false ;
	}

	// Set the configuration restartable flag.  A configuration restart
   // will load from memory and not reparse the configuration text.

	void setRestartable(boolean b) { restartable = b ; }

	// Method to set a reference to our primary media player.

	void setMediaFrame(MediaFrame f) { mediaframe = f ; }

   // Method to retrieve our configuration FileOpen object.

   FileOpen getFileOpen() { return (zip == null) ? null : zip.getFileOpen() ; }

	// Return a writable file indicator.

	boolean isWritable() { return true ; }

	// Return our UltraKiss indicator.

	boolean isUltraKiss() { return ultrakiss ; }

	// Return our Cherry KiSS indicator.

	boolean isCherryKiss() { return ckiss ; }

	// Return our Enhanced palette indicator.

	boolean isEnhancedPalette() { return epalette ; }

	// Return a restartable indicator.

	boolean isRestartable() { return restartable ; }

	// Return our border RGB indicator.

	boolean isBorderRgb() { return rgbborder ; }

	// Return our edit change indicator.

	boolean isChanged()
   {
   	if (changed) return true ;
      if (pages == null) return false ;
      for (int i = 0 ; i < pages.size() ; i++)
      	if (((PageSet) pages.elementAt(i)).isChanged()) return true ;
      return false ;
   }

	// Return our option change indicator.

	boolean isOptionChanged() { return optionchange ; }

	// Return the configuration reload copy indicator.

	boolean isCopy() { return copy ; }

	// Return the active configuration indicator.

	boolean isVisible() { return activated ; }

	// Return an indicator if the configuration uses INCLUDE files.

	boolean hasIncludeFiles() { return (includefiles != null && includefiles.size() > 0) ; }
   
   // Method to add unreferenced sound files to our configuration.  This method 
   // is used on configuration restarts to retain newly imported audio objects
   // across configuration reloads.
   
   void addSounds(Vector v)
   {
      boolean addentry ;
      if (v == null) return ;
      
      for (int i = 0 ; i < v.size() ; i++)
      {
         addentry = true ;
         Object o1 = v.elementAt(i) ;
         if (!(o1 instanceof Audio)) continue ;
         Audio a1 = (Audio) o1 ;
         if (sounds != null)
         {
            for (int j = 0 ; j < sounds.size() ; j++)
            {
               Object o2 = sounds.elementAt(j) ;
               if (!(o2 instanceof Audio)) continue ;
               Audio a2 = (Audio) o2 ;
               if (a1.getName().equals(a2.getName())) 
                  { addentry = false; break ; }
            }
         }

         // Set the key indexes for the new configuration.
         
         if (!addentry) continue ;
			a1.setID(cid) ;
			a1.setIdentifier(new Integer(audioCount++)) ;
			a1.setKey(a1.getKeyTable(),cid,a1.getPath().toUpperCase()) ;
			a1.setKey(a1.getKeyTable(),cid,a1.getName().toUpperCase()) ;
			a1.setKey(a1.getKeyTable(),cid,file.toUpperCase()) ;
         if (a1.isImported()) 
            a1.setKey(a1.getKeyTable(),cid,"Import "+a1.getName().toUpperCase()) ;

         // Ensure the contents are loaded.  The restarted configuration
         // may have unloaded contents when the CNF was closed.
         
			if (!a1.isLoaded()) 
         {
            try
            {
               ArchiveFile zip = a1.getZipFile() ;
               if (zip != null && !zip.isOpen()) zip.open() ;
               a1.load() ;
            }
            catch (IOException e) { break ;}
         }
         if (sounds == null) sounds = new Vector() ;
         sounds.addElement(a1) ;
      }
   }
   
   // Method to add unreferenced movie files to our configuration.  This method 
   // is used on configuration restarts to retain newly imported video objects
   // across configuration reloads.
   
   void addMovies(Vector v)
   {
      boolean addentry ;
      if (v == null) return ;
      
      for (int i = 0 ; i < v.size() ; i++)
      {
         addentry = true ;
         Object o1 = v.elementAt(i) ;
         if (!(o1 instanceof Video)) continue ;
         Video v1 = (Video) o1 ;
         if (movies != null)
         {
            for (int j = 0 ; j < movies.size() ; j++)
            {
               Object o2 = movies.elementAt(j) ;
               if (!(o2 instanceof Video)) continue ;
               Video v2 = (Video) o2 ;
               if (v1.getName().equals(v2.getName())) 
                  { addentry = false; break ; }
            }
         }

         // Set the key indexes for the new configuration.
         
         if (!addentry) continue ;
         if (movies == null) movies = new Vector() ;
			v1.setID(cid) ;
			v1.setIdentifier(new Integer(videoCount++)) ;
			v1.setKey(v1.getKeyTable(),cid,v1.getPath().toUpperCase()) ;
			v1.setKey(v1.getKeyTable(),cid,v1.getName().toUpperCase()) ;
			v1.setKey(v1.getKeyTable(),cid,file.toUpperCase()) ;
         if (v1.isImported()) 
            v1.setKey(v1.getKeyTable(),cid,"Import "+v1.getName().toUpperCase()) ;

         // Ensure the contents are loaded.  The restarted configuration
         // may have unloaded contents when the CNF was closed.
         
			if (!v1.isLoaded()) 
         {
            try
            {
               ArchiveFile zip = v1.getZipFile() ;
               if (zip != null && !zip.isOpen()) zip.open() ;
               v1.load() ;
            }
            catch (IOException e) { break ;}
         }
         movies.addElement(v1) ;
      }
   }

	// Method to write our file contents to the specified output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
		if (error) return -1 ;
		if (b == null || isUpdated())
		{
			byte [] configtext = write() ;
			if (configtext != null) out.write(configtext) ;
			int length = (configtext == null) ? 0 : configtext.length ;
         if (fw != null) fw.updateProgress(length) ;
			return length ;
		}
		out.write(b) ;
      if (fw != null) fw.updateProgress(b.length) ;
		return b.length ;
	}

   // Method to return an input stream to access our data buffer.

   InputStream getInputStream()
	{
		if (error) return null ;
		if (b == null) { try { write() ; } catch (IOException e) { } }
      if (b != null) return new ByteArrayInputStream(b) ;
      return null ;
   }


	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// configuration identifier.

	static void clearTable(Object cid)
	{
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}



	// Object creation utility methods
	// -------------------------------

	// Method to open a new configuration file entry in a zip file.
	// The reference configuration is used to access the contents of
	// previously loaded files for configuration reloads or expansions.

	void open(ArchiveFile zip, ArchiveEntry ze, Configuration r) throws Exception
	{
		error = false ;
		ref = r ;

		try
		{
			setZipFile(zip) ;
			setZipEntry(ze) ;

			if (zip != null && ze != null)
			{
				file =  ze.getPath() ;
				bytes = (int) ze.getSize() ;
				is = zip.getInputStream(ze) ;
			}
			if (is == null)
				throw new IOException("No input stream for " + file) ;

			// Read the entire file contents into memory.

			System.out.println("Open configuration " + file) ;
			int n = 0, len = 0 ;
			b = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0)
         {
            if (loader instanceof FileLoader && ((FileLoader) loader).stop) break ;
            n += len ;
         }
			is.close() ;

			// Open the memory copy of the configuration file.

         InputStreamReader isr = null ;
         String encoding = Kisekae.getLanguageEncoding() ;
			is = new ByteArrayInputStream(b) ;
         if (encoding == null) isr = new InputStreamReader(is) ;
         else isr = new InputStreamReader(is,encoding) ;
			f = new BufferedReader(isr) ;
		}

		// Catch file errors.

		catch (Exception e)
		{
			error = true ;
			showFile("Open Exception, " + file) ;
			throw e ;
		}
	}


	// Method to open a configuration file entry by reloading the data from
   // an active configuration.  We reopen the archive file so that we can
   // load new elements that may be named in the reference configuration.
   // If we have a new configuration then retain the reference zip entry.

	void openref(Configuration r) throws Exception
	{
		ref = r ;
		FileOpen fileopen = ref.getFileOpen() ;
      if (fileopen == null) return ;
      zip = ref.getZipFile() ;
     	ze = ref.getZipEntry() ;
      if (zip != null && zip.isOpen()) zip.close() ;
      fileopen.open((ze != null) ? ze.getPath() : null) ;

      // Get the reference configuration fileopen zip file and zip entry. On
      // new data sets these will be null as no valid fileopen object exists.

      ArchiveFile newzip = fileopen.getZipFile() ;
		ArchiveEntry newze = fileopen.getZipEntry() ;
      if (newzip == null && newze == null)
      {
         newzip = ref.getZipFile() ;
      	newze = ref.getZipEntry() ;
      }
      
      // On a restart we created a new zip file.  The old one should be
      // flushed to discard the old content entries.
      
      if (zip != null) zip.flush() ;
      zip = newzip ;
      ze = newze ;

      // Set the new reloaded configuration object state attributes to be
      // the same as the reference configuration.  The new configuration
      // is restartable.

      setLastModified(ref.lastModified()) ;
		setUpdated(ref.isUpdated()) ;
		setInternal(ref.isInternal()) ;
      setOptionsChanged(ref.isOptionChanged()) ;
      setRestartable(ref.isRestartable()) ;
		file = ref.getPath() ;
		bytes = ref.getBytes() ;
		System.out.println("Open configuration " + file) ;

		// Open the memory copy of our reference configuration file.

		try
		{
			b = ref.getMemoryFile() ;
			if (b != null)
			{
				is = new ByteArrayInputStream(b) ;
				InputStreamReader isr = new InputStreamReader(is) ;
				f = new BufferedReader(isr) ;
			}
		}

		// Catch file errors.

		catch (Exception e)
		{
      	b = null ;
         f = null ;
			is = null ;
			error = true ;
			showFile("Open Exception, " + file) ;
			throw e ;
		}
	}


	// Method to read and parse the configuration file.
	// This method populates the palettes, cels and page set vectors.
	// It also sets the screen dimension size.

	void read() throws Exception
	{
		StringTokenizer st = null ;
      boolean showtitle = true ;
      boolean leadsection = true ;
      boolean celgroupcheck = false ;
      KissObject lastparsed = null ;
      int maxpageref = 0 ;

		if (error) return ;
		if (f == null) return ;
		if (loader != null) loader.initProgress(bytes) ;
      leadingtext = new Vector() ;
      trailingtext = new Vector() ;
      leadaction = new Vector() ;
      trailtoken = new Vector() ;
		reread = false ;
		newline = false ;
      ultrakiss = false ;
      directkiss = false ;
      playfkiss = false ;
      eventsection = false ;
		line = 0 ;
		border = 0 ;
      importborder = -1 ;

		// Read the configuration file.  We read one line at a time and
		// decode it.  There are times when the parse will fail and we
		// must re-read the last line.

		try
		{
			while (true)
			{
				newline = true ;
				String s = (reread) ? lastline : f.readLine() ;
				if (s == null) break ;
            if (loader instanceof FileLoader && ((FileLoader) loader).stop) break ;
				if (!reread && loader != null) loader.updateProgress(s.length()+2) ;
            if (trailingtext.size() == 0)
               trailingtext.addElement(s) ;
            if (trailingtext.lastElement() != s)
               trailingtext.addElement(s) ;

            // Establish our line.

				reread = false ;
				lastline = s ;
				line = line + 1 ;
				s = trim(s) ;
				if (s.length() == 0)
            {
            	leadingtext.addElement(new String(" ")) ;
               continue ;
            }

				// Parse the configuration statements.

				switch (s.charAt(0))
				{
					// Comment or FKiSS specifications.

				case ';':
					if (s.length() <= 1 || s.charAt(1) != '@')
               {
               	leadingtext.addElement(s) ;
                  String s1 = (s.length() > 1) ? trim(s.substring(1)) : "" ;

                  // Identify UltraKiss configuration files.

                  if (line < 10)
                  {
                     if (s1.startsWith("Kisekae UltraKiss"))
                        ultrakiss = true ;
                  
                     if (s1.indexOf("This CNF file was written by Direct KiSS") >= 0)
                        directkiss = true ;
                  
                     if (s1.indexOf("This .cnf file was written by PlayFKiSS") >= 0)
                        playfkiss = true ;
                  
                     String s1lc = s1.toLowerCase() ;
                     if (s1lc.indexOf("the owl") >= 0)
                        playfkiss = true ;
                     if (s1lc.indexOf("the scarecrow") >= 0)
                        playfkiss = true ;
                     if (s1lc.indexOf("living pictures") >= 0)
                        playfkiss = true ;
                     if (s1lc.indexOf("s a m o d i v a") >= 0)
                        playfkiss = true ;
                  }

                  // Parse UltraKiss options.

                  if (s1.startsWith("[Option Section]"))
                  {
                     showtitle = false ;
                     parseOptions(s,f) ;
                     reread = true ;
                  }

                  // Parse INCLUDE statements.

                  if (s1.startsWith("INCLUDE"))
                  {
                     s1 = s1.substring("INCLUDE".length()) ;
                     char c = (s1.length() > 0) ? s1.charAt(0) : 0 ;
                     if (Character.isWhitespace(c))
                        parseIncludeFiles(s1) ;
                  }

                  // Parse HINT statements.

                  if (s1.startsWith("HINT"))
                  {
                     s1 = s1.substring("HINT".length()) ;
                     char c = (s1.length() > 0) ? s1.charAt(0) : 0 ;
                     if (Character.isWhitespace(c))
                        parseHint(s1) ;
                  }

                  // Parse Cel Group specifiers.

                  if (lastparsed instanceof Cel && celgroupcheck)
                  {
                     Vector v = ((Cel) lastparsed).getCelGroups() ;
                     celgroupcheck = parseCelGroup(s1,v) ;
                  }

                  // Show initial comment text in the loader.

               	if (showtitle && loader != null)
                  {
                  	if (s1.startsWith("[Screen Section]"))
                     	showtitle = false ;
                     else
                     	loader.showText(s) ;
                  }
                  continue ;
               }

               // Try and parse an FKiSS event.

					FKissEvent event = parseFKiss(s,f,lastparsed) ;
					if (event != null)
               {
	            	if (leadsection)
		               addLeadComment(leadingtext) ;
	               else
		               event.addLeadComment(leadingtext) ;
						if (handler == null) handler = new EventHandler() ;
            		if (!"EventHandler".equals(event.getIdentifier()))
                  	handler.addEvent(event) ;
                  trailingtext.removeAllElements() ;
                  lastparsed = event ;
               }
					break ;

					// Memory size specification (ignored).

				case '=':
            	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
               	leadmemory.addAll(leadingtext) ;
					st = new StringTokenizer(s," \t=") ;
					try {	memory = Integer.parseInt(st.nextToken()) ; }
               catch (NoSuchElementException e)
               { memory = -1 ; }
					catch (Exception e)
					{ showError("Non-numeric memory specification.") ; }
               trailmemory = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
					trailingtext.removeAllElements() ;
					break ;

					// Border color pixel code.

				case '[':
               int n1, n2, n3 ;
               n1 = n2 = n3 = -1 ;
             	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
               	leadborder.addAll(leadingtext) ;

               // Parse the text.  An index border is one number less than 256.
               // An RGB border is 3 numbers delimited by commas or spaces, or
               // a single number greater than or equal to 256.

					st = new StringTokenizer(s," \t[,") ;
					try {	n1 = Integer.parseInt(st.nextToken()) ; }
               catch (Exception e) { n1 = -1 ; }
               trailborder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
					st = new StringTokenizer(trailborder," ,") ;
               if (n1 >= 0)
               {
   					try {	n2 = Integer.parseInt(st.nextToken()) ; }
                  catch (Exception e) { n2 = -1 ; }
               }
               if (n2 >= 0)
               {
   					try {	n3 = Integer.parseInt(st.nextToken()) ; }
                  catch (Exception e) { n3 = -1 ; }
                  if (n3 >= 0) trailborder = (st.hasMoreTokens())
                     ? st.nextToken("") : "" ;
               }

               // Set the border value.

               border = n1 ;
               rgbborder = (n1 >= 0 && n2 >= 0 && n3 >= 0) ;
               if (rgbborder) border = ((n1&0xFF) << 16) + ((n2&0xFF) << 8) + (n3&0xFF) ;
               if (n1 > 255 & !rgbborder) rgbborder = true ;
               if (border < 0) border = 0 ;
					trailingtext.removeAllElements() ;
					break ;

					// Screen size specifications.

				case '(':
            	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
               	leadsize.addAll(leadingtext) ;
					st = new StringTokenizer(s," \t,()") ;
					try
					{
						screen.width = Integer.parseInt(st.nextToken()) ;
						screen.height = Integer.parseInt(st.nextToken()) ;
					}
					catch (Exception e)
					{ showError("Non-numeric screen size specification.") ; }
               trailscreen = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
               int i = trailscreen.indexOf(')') ;
               if (i >= 0) trailscreen = trailscreen.substring(i+1) ;
					trailingtext.removeAllElements() ;
					break ;

					// Palette file identifiers.

				case '%':
					Palette palette = parsePalette(s) ;
               if (palette == null) break ;
					palette.setIdentifier(new Integer(paletteCount++)) ;
					palette.setKey(palette.getKeyTable(),cid,palette.getIdentifier()) ;
					palette.setKey(palette.getKeyTable(),cid,palette.getPath().toUpperCase()) ;
					palette.setKey(palette.getKeyTable(),cid,palette.getName().toUpperCase()) ;
               if (palette.isImported()) 
                  palette.setKey(palette.getKeyTable(),cid,"Import "+palette.getName().toUpperCase()) ;
            	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
	               palette.addLeadComment(leadingtext) ;
					palette.setLine(line) ;
					palettes.addElement(palette) ;
					trailingtext.removeAllElements() ;
               lastparsed = palette ;
					break ;

					// Cel file specifications.

				case '#':
					Cel cel = parseCel(s) ;
               if (cel == null) break ;
					cel.setIdentifier(new Integer(celCount++)) ;
					cel.setKey(cel.getKeyTable(),cid,cel.getIdentifier()) ;
					cel.setKey(cel.getKeyTable(),cid,cel.getPath().toUpperCase()) ;
					cel.setKey(cel.getKeyTable(),cid,cel.getName().toUpperCase()) ;
               if (cel.isImported()) 
                  cel.setKey(cel.getKeyTable(),cid,"Import "+cel.getName().toUpperCase()) ;
               cel.setLevel((Integer) cel.getIdentifier()) ;
            	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
 	               cel.addLeadComment(leadingtext) ;
					cel.setLine(line) ;
					cels.addElement(cel) ;
					trailingtext.removeAllElements() ;
               lastparsed = cel ;
               celgroupcheck = true ;
               int maxcelpage = cel.getMaxPage() ;
               if (maxcelpage > maxpageref) maxpageref = maxcelpage ;
					break ;

					// Page set declarations.

				case '$':
					PageSet page = parsePage(s,f) ;
					if (page == null) break ;
					page.setIdentifier(new Integer(pageCount++)) ;
					page.setKey(page.getKeyTable(),cid,page.getIdentifier()) ;
					page.setKey(page.getKeyTable(),cid,page.getUniqueID()) ;
            	if (leadsection)
	               addLeadComment(leadingtext) ;
               else
	               page.addLeadComment(leadingtext) ;
					page.setLine(line) ;
					pages.addElement(page) ;
					trailingtext.removeAllElements() ;
               trailingtext.addAll(trailtoken) ;
               lastparsed = page ;
					break ;

					// Unknown command.

				default:
					showError("Unknown configuration command: " + s) ;
               if (lastparsed != null) lastparsed.addTrailComment(trailingtext) ;
					trailingtext.removeAllElements() ;
					break ;
				}

            // We have processed a command and all leading comments.

            showtitle = false ;
            leadsection = false ;
            leadingtext = new Vector() ;
            leadingtext.addAll(leadaction) ;
            leadaction = new Vector() ;
            trailtoken = new Vector() ;
			}
		}
		catch (Exception e)
		{
			error = true ;
         showError("Configuration read " + file + ", " + e.getMessage()) ;
			showFile("Read Exception, " + file) ;
			throw e ;
		}

		// Close the file on termination and clean up our memory allocation.

		finally
		{
			try
         {
         	if (f != null) f.close() ;
            f = null ;
            is = null ;
         }
			catch (IOException e)
			{
         	b = null ;
            f = null ;
            is = null ;
				error = true ;
				showFile("Configuration I/O Exception, " + file) ;
				throw e ;
			}
		}

		// Clean up the data.  Cels that did not specify page associations must
		// be visible on all initial pages.  Page sets must be created up to
      // the maximum page number referenced in the cel page associations.

		addTrailComment(trailingtext) ;
      for (int i = pages.size() ; i <= maxpageref ; i++)
      {
         PageSet page = new PageSet() ;
   		page.setID(cid) ;
         page.setIdentifier(new Integer(pageCount++)) ;
         page.setKey(page.getKeyTable(),cid,page.getIdentifier()) ;
         page.setKey(page.getKeyTable(),cid,page.getUniqueID()) ;
         page.setLine(line) ;
         pages.addElement(page) ;
      }
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			Vector celpages = c.getPages() ;
			if (celpages != null) continue ;
			celpages = new Vector() ;
			for (int j = 0 ; j < pages.size() ; j++)
				celpages.addElement(new Integer(j));
			c.setPages(celpages) ;
         c.setAllPages(true) ;
		}

      // If we have a reference configuration we must retain old event
      // definitions unless they were redefined.  Expansion sets may not
      // always define all prior events.

      EventHandler refhandler = (ref != null) ? ref.getEventHandler() : null ;
      if (refhandler != null && OptionsDialog.getExpandEvents())
      {
         if (handler == null) handler = new EventHandler() ;
         Enumeration enum1 = refhandler.getEvents() ;
         while (enum1 != null && enum1.hasMoreElements())
            handler.addNamedEvent((Vector) enum1.nextElement()) ;
      }
      if (refhandler != null) refhandler.flush() ;

      // If we use include files we must cache images as we can reference
      // cels that are not in the base archive file.

      if (includefiles != null && includefiles.size() > 0)
      {
         OptionsDialog.setCacheImage(true) ;
         setRestartable(false) ;
      }
	}


	// Method to load all files in the configuration.  Palette files must be
   // loaded first, followed by the cel files and media files.

	void load()
	{
		if (error) return ;

		// Load the palette files first.

		if (loader != null)
			loader.initProgress(palettes.size()+cels.size()+sounds.size()+movies.size()) ;
		int i = 0 ;
		while (palettes != null && i < palettes.size())
		{
         if (loader instanceof FileLoader && ((FileLoader) loader).stop) break ;
			Palette p = (Palette) palettes.elementAt(i++) ;
			p.load(includefiles) ;
			loadbytes += p.getBytes() ;
			if (loader != null) loader.updateProgress(1) ;
         int mp = p.getMultiPaletteCount() ;
         int colors = p.getColorCount() ;
         if (mp*colors > 256) epalette = true ;
         if (i == 1 && p.isImported()) importborder = p.getBackgroundIndex() ;
		}

      // Identify the start page.  We begin with the first page that is visible
      // and has objects defined on it.

     	i = 0 ;
     	for (i = 0 ; i < pages.size() ; i++)
      {
        	PageSet pageset = (PageSet) pages.elementAt(i) ;
        	if (pageset.isVisible() && pageset.getGroupCount() > 0) break ;
      }
      if (i >= pages.size()) i = 0 ;
      Integer startpage = new Integer(i) ;

		// Load cel files.  They require palettes for correct ColorModels.
      // Imported or updated cels may not yet have been saved.

		i = 0 ;
		while (cels != null && i < cels.size())
		{
         boolean load = false ;
         if (loader instanceof FileLoader && ((FileLoader) loader).stop) break ;
			Cel c = (Cel) cels.elementAt(i++) ;
         
         // If we have a reference to an imported or updated cel, then load.
         
         if (ref != null)
         {
            String name = c.getRelativeName() ;
            if (name != null) name = name.toUpperCase() ;
            Cel c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),name) ;
            if (c1 == null) c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),"Import "+name) ;
            if (c1 != null && (c1.isImported() || c1.isUpdated())) load = true ; 
         }
         
         // If our cel is on the start page or we are caching images, then load.
         
         if (c.isOnPage(startpage) || OptionsDialog.getCacheImage()) load = true ; 
         if (c instanceof Video) load = true ; 
         if (load) c.load(includefiles) ;
         
         // Retain the progress state.
         
         loadbytes += c.getBytes() ;
         if (c.isTruecolor()) ckiss = true ;
			if (loader != null) loader.updateProgress(1) ;
		}

		// Load the audio files.   Video files are cels and were loaded
      // with the cels.

		i = 0 ;
		while (sounds != null && i < sounds.size())
		{
         if (loader instanceof FileLoader && ((FileLoader) loader).stop) break ;
			Audio a = (Audio) sounds.elementAt(i++) ;
			a.load(includefiles) ;
         loadbytes += a.getBytes() ;
			if (loader != null) loader.updateProgress(1) ;
		}

      // Close any INCLUDE files that were opened.

      if (includefiles != null)
      {
         for (i = 0 ; i < includefiles.size() ; i++)
         {
            Object o = includefiles.elementAt(i) ;
            if (!(o instanceof ArchiveFile)) continue ;
            try { ((ArchiveFile) o).close() ; }
            catch (IOException e) { }
         }
      }
	}


	// Method to scale the cel images to fit within the screen area.

	void scale(int x, int y) throws Exception
	{
		if (error) return ;
		if (loader != null)
			loader.initProgress(cels.size()) ;

      // Compute the scaling factor required to fit the cel images
      // to the specified dimension.

      float sx = ((float) x) / screen.width ;
      float sy = ((float) y) / screen.height ;
      sf = Math.min(sx,sy) ;
	}


	// Method to initialize the configuration data set.  This creates
	// the groups and the page sets.  This method establishes the
	// event triggers for the appropriate cel and group objects.
	// This also creates the configuration celgroup list that links
	// all cels together that are in the same group name.

	void init()
	{
		if (error) return ;
      maxcels = cels.size() ;
      maxcelnumber = maxcels ;

		// After reading the parameters the cel object initially
		// holds a reference to an Integer group number.  When cels
		// are grouped each cel will contain a reference to its group
		// object.

		for (int i = 0; i < cels.size(); i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			Object o = c.getGroup() ;
			if (!(o instanceof Integer)) continue;

			// Get the group number for this cel.  Track the maximum used
         // group number to create new group identifiers if required.

			Integer g = (Integer) o ;
			int groupNum = g.intValue() ;
         if (groupNum > maxgroups) maxgroups = groupNum ;
			Group group = new Group(g) ;
			group.setIdentifier(g) ;
			group.setKey(group.getKeyTable(),cid,g) ;

			// Find all other cels in this group.  Build the
			// group object and have the cel refer to it.

			for (int j = i; j < cels.size(); j++)
			{
				c = (Cel) cels.elementAt(j) ;
				o = c.getGroup() ;
				if (!(o instanceof Integer)) continue;

				// Cel is not yet placed in a group.  Add the cel to this
				// group if it belongs here.

				g = (Integer) o;
				if (g.intValue() != groupNum) continue;
				c.setLocation(group.getLocation());
				group.addCel(c);
			}

			// Add this group to the list of groups.

         group.setInitialOffset() ;
			groups.addElement(group) ;
		}

      // Sort the groups so that they will be displayed in group number
      // sequence.

      Collections.sort(groups) ;

		// If no page sets were specified in the configuration file, then
		// default page sets must be created for all pages referenced
		// by the set of cels.  In truth, page sets must be created for
      // any cel that referenced a page for which no $ line existed.

		Vector pageset = new Vector() ;
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			Vector v = c.getPages() ;
			if (v == null) continue ;
			for (int j = 0 ; j < v.size() ; j++)
			{
				if (pageset.contains(v.elementAt(j))) continue ;
				pageset.addElement(v.elementAt(j)) ;
			}
		}

		// Create any necessary page sets.

		for (int i = 0 ; i < pageset.size() ; i++)
		{
			Integer p = (Integer) pageset.elementAt(i) ;
         if (PageSet.getByKey(PageSet.getKeyTable(),cid,p) == null)
         {
				PageSet page = new PageSet() ;
				page.setMultiPalette(new Integer(0)) ;
				page.setID(cid) ;
				page.setIdentifier(p) ;
				page.setKey(page.getKeyTable(),cid,p) ;
				page.setKey(page.getKeyTable(),cid,page.getUniqueID()) ;
				pages.addElement(page) ;
         }
		}

      // Sort the pages so that they will be displayed in page number
      // sequence.

      Collections.sort(pages) ;

		// Associate the required groups with each page set.  When a set
		// is activated it will be initialized.  Set 0 is initialized
		// by default when a new configuration is loaded.

		for (int i = 0 ; i < pages.size() ; i++)
		{
			PageSet p = (PageSet) pages.elementAt(i) ;
			p.setGroups(groups) ;
		}

		// Initialize any audio objects that might exist.

		for (int i = 0 ; i < sounds.size() ; i++)
		{
			Audio a = (Audio) sounds.elementAt(i) ;
			a.init() ;
		}

		// Initialize any video objects that might exist.

		for (int i = 0 ; i < movies.size() ; i++)
		{
         if (Kisekae.isMediaInstalled())
         {
   			Video v = (Video) movies.elementAt(i) ;
   			v.init() ;
         }
		}

      // Establish the list of animated cels and the list of component
      // cels.  The component list and the animated cel list is referenced
      // on each page change to manage the visual display. We also set
      // up the component name maximum counts so that new components
      // can be uniquely created.

		for (int i = 0 ; i < cels.size() ; i++)
      {
      	Cel c = (Cel) cels.elementAt(i) ;
         JavaCel.setComponentNumber(c.getName(),cid) ;
         if (c instanceof GifCel) frames.addElement(c) ;
         if (c instanceof JavaCel) comps.addElement(c) ;
		}

		// Create the celgroup table.  FKiSS 4 introduced the notion of a
		// cel group or collection of cels that could be referenced by
		// name during FKiSS event or action processing.

		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			Vector celgrouplist = c.getCelGroups() ;
			if (celgrouplist == null) continue ;

			// Identify all celgroups associated with this cel.  If the group
			// is new add a new celgroup to the configuration celgroup list.
			// If it is known to the configuration, add the cel to the
			// existing configuration celgroup.

			for (int j = 0 ; j < celgrouplist.size() ; j++)
			{
				CelGroup cg1 = (CelGroup) celgrouplist.elementAt(j) ;
				Object o = cg1.getIdentifier() ;
				String name = (o != null) ? o.toString() : "" ;
				CelGroup cg2 = (CelGroup) CelGroup.getByKey(CelGroup.getKeyTable(),cid,name) ;
				if (cg2 == null)
				{
					cg2 = new CelGroup(name) ;
               cg2.setID(cid) ;
					cg2.setKey(cg2.getKeyTable(),cid,cg2.getIdentifier()) ;
					celgroups.add(cg2) ;
				}
            
            // Add the cel to the configuration celgroup. Also add all the
            // celgroup frame specifications to the configuration celgroup
            // so that we know how many frames are defined.
            
				cg2.addCel(c) ;
            cg2.addFrame(cg1.getCelGroupFrames()) ;
			}
		}

		// Set up the objects for all event actions.  When the event
		// specifications were read the objects were not created.
		// We must now decode the action parameters and attach the
		// event actions to the appropriate object.

		Enumeration events = (handler == null) ? null : handler.getEvents() ;
		while (events != null && events.hasMoreElements())
		{
			Vector v = (Vector) events.nextElement() ;
			if (v == null) continue ;
			for (int i = 0 ; i < v.size() ; i++)
			{
				FKissEvent event = (FKissEvent) v.elementAt(i) ;
            attachEvent(event) ;
         }
		}
      
      // Perform syntax validation.

      if (OptionsDialog.getStrictSyntax())
         validateFKissSyntax() ;
	}


	// Most events apply to an object.  When the user does something such as
   // click an object or switch to a new page, the event actions for the object
   // must be processed.  We attach the event to the action object so that
   // events can be recognized when the object is referenced.

   void attachEvent(FKissEvent event) { attachEvent(event,false) ; }
   void attachEvent(FKissEvent event, boolean detach)
   {
      if (event == null) return ;
		String name = (String) event.getIdentifier() ;
      if (name == null) return ;
		String target = event.getFirstParameter() ;
		if (target != null) target = target.toUpperCase() ;

		// Events that apply to a cel or group or cel group.  We catch
		// some parameter syntax errors here.

		try
		{
			if ("press".equals(name) || "catch".equals(name) ||
				"fixcatch".equals(name) || "release".equals(name) ||
				"drop".equals(name) || "fixdrop".equals(name) ||
				"mousein".equals(name) || "mouseout".equals(name) ||
            "unfix".equals(name))
			{
				Object o = findGroupOrCel(target) ;
				if (o instanceof KissObject)
               if (!detach)
                  ((KissObject) o).addEvent(event) ;
               else
                  ((KissObject) o).removeEvent(event) ;

				// Add this event to all cel duplicates.

				if (o instanceof Cel)
				{
					Cel c = (Cel) o ;
					while ((c = Cel.findNextCel(c,target,this)) != null)
               {
                  if (!detach)
                     c.addEvent(event) ;
                  else
                     c.removeEvent(event) ;
               }
				}
			}

			// Events that apply only to a group.  The FKiSS 4 specification
			// suggests that unfix events apply to cels, too.

			if ("detached".equals(name))
			{
				Group g = (Group) Group.findGroup(target,this,null) ;
				if (g != null)
            {
               if (!detach)
                  g.addEvent(event) ;
               else
                  g.removeEvent(event) ;
            }
			}

			// Collision events that apply to a group or cel.  These events
			// are symmetric. For cels, the event is attached to the group object
			// to capture the cel collisions during group moves.

			if ("in".equals(name) || "out".equals(name) ||
				 "stillin".equals(name) || "stillout".equals(name))
			{
            boolean processed = false ;
				Group g = (Group) Group.findGroup(target,this,null) ;
				if (g != null)
            {
               processed = true ;
               if (!detach)
                  g.addEvent(event) ;
               else
                  g.removeEvent(event) ;
            }
            else
            {
               Cel c = (Cel) Cel.findCel(target,this,null) ;
					while (c != null)
					{
						Object o = c.getGroup() ;
						if (o instanceof Group)
                  {
                     processed = true ;
                     if (!detach)
                        ((Group) o).addEvent(event) ;
                     else
                        ((Group) o).removeEvent(event) ;
                  }
						c = Cel.findNextCel(c,c.getName(),this) ;
					}
            }

            if (!processed)
            {
               CelGroup cg1 = CelGroup.findCelGroup(target,this,null) ;
               if (cg1 != null)
               {
                  Vector group = cg1.getGroups() ;
                  for (int i = 0 ; i < groups.size() ; i++)
                  {
                     Group g1 = (Group) groups.elementAt(i) ; 
                     if (!detach)
                        g1.addEvent(event) ;
                     else
                        g1.removeEvent(event) ;
                  }
               }
            }

				// Do the symmetric part.

            processed = false ;
				target = event.getSecondParameter() ;
				if (target != null) target = target.toUpperCase() ;
				g = (Group) Group.findGroup(target,this,null) ;
				if (g != null)
            {
               processed = true ;
               if (!detach)
                  g.addEvent(event) ;
               else
                  g.removeEvent(event) ;
            }
            else
            {
               Cel c = (Cel) Cel.findCel(target,this,null) ;
					while (c != null)
					{
						Object o = c.getGroup() ;
						if (o instanceof Group)
                  {
                     processed = true ;
                     if (!detach)
                        ((Group) o).addEvent(event) ;
                     else
                        ((Group) o).removeEvent(event) ;
                  }
						c = Cel.findNextCel(c,c.getName(),this) ;
					}
            }
            
            if (!processed)
            {
               CelGroup cg2 = CelGroup.findCelGroup(target,this,null) ;
               if (cg2 != null)
               {
                  Vector group = cg2.getGroups() ;
                  for (int i = 0 ; i < groups.size() ; i++)
                  {
                     Group g1 = (Group) groups.elementAt(i) ; 
                     if (!detach)
                        g1.addEvent(event) ;
                     else
                        g1.removeEvent(event) ;
                  }
               }
            }
			}

			// Collision events that apply to a cel or cel group.
         // These events are symmetric.  They are attached to the group object
			// to capture cel collisions during group moves.

			if ("apart".equals(name) || "collide".equals(name))
			{
				Vector cellist = new Vector() ;
				Cel c = (Cel) Cel.findCel(target,this,null) ;
            if (c != null)
            {
               while (c != null)
               {
                  cellist.add(c) ;
                 	c = (Cel) Cel.findNextCel(c,target,this) ;
               }
            }
				else
				{
					Object o = CelGroup.findCelGroup(target,this,null) ;
					if (o instanceof CelGroup)
						cellist = ((CelGroup) o).getCels() ;
               else
               {
                  o = Group.findGroup(target,this,null) ;
					   if (o instanceof Group)
						   cellist = ((Group) o).getCels() ;
               }
				}

				// We have a list of cels in the named cel group.  For each
				// cel attach the event.  

				Enumeration enum1 = cellist.elements() ;
				while (enum1.hasMoreElements())
				{
					Object o = enum1.nextElement()  ;
					if (!(o instanceof Cel)) continue ;
					c = (Cel) o ;
/*					o = c.getGroup() ;
					if (o instanceof Group)
               {
                  if (!detach)
                     ((Group) o).addEvent(event) ;
                  else
                     ((Group) o).removeEvent(event) ;
					}
 */
               if (!detach)
                  c.addEvent(event) ;
               else
                  c.removeEvent(event) ;
				}

				// Do the symmetric part.

				cellist = new Vector() ;
				target = event.getSecondParameter() ;
				if (target != null) target = target.toUpperCase() ;
				c = (Cel) Cel.findCel(target,this,null) ;
            if (c != null)
            {
               while (c != null)
               {
                  cellist.add(c) ;
                 	c = (Cel) Cel.findNextCel(c,target,this) ;
               }
            }
				else
				{
					Object o = CelGroup.findCelGroup(target,this,null) ;
					if (o instanceof CelGroup)
						cellist = ((CelGroup) o).getCels() ;
               else
               {
                  o = Group.findGroup(target,this,null) ;
					   if (o instanceof Group)
						   cellist = ((Group) o).getCels() ;
               }
				}

				// We have a list of cels in the named cel group.
				// For each cel attach the event.

				enum1 = cellist.elements() ;
				while (enum1.hasMoreElements())
				{
					Object o = enum1.nextElement()  ;
					if (!(o instanceof Cel)) continue ;
					c = (Cel) o ;
/*					o = c.getGroup() ;
   				if (o instanceof Group)
               {
                  if (!detach)
                     ((Group) o).addEvent(event) ;
                  else
                     ((Group) o).removeEvent(event) ;
					}
 */
               if (!detach)
                  c.addEvent(event) ;
               else
                  c.removeEvent(event) ;
				}
			}

			// Events that apply to a page set.  Page sets are changed
			// when the user explicitly selects a new page to view, or
			// if a 'changeset' event action command is invoked.  In
			// either case, the PageSet object will process its own
			// events when the set is initialized.

			if ("set".equals(name))
			{
            Integer n = null ;
            if (target != null && !"*".equals(target))
               n = Integer.decode(target) ;
				PageSet p = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,n) ;
				if (p != null)
            {
               if (!detach)
                  p.addEvent(event) ;
               else
                  p.removeEvent(event) ;
            }
			}

			// Events that apply to a palette change.  Palettes can be
			// changed by the user, either through an explicit selection
			// or if a 'changecol' event action command is invoked.  These
			// are multipalette requests and are attached to palette file zero.

			if ("col".equals(name))
			{
				Palette p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,new Integer(0)) ;
				if (p != null)
            {
               if (!detach)
                  p.addEvent(event) ;
               else
                  p.removeEvent(event) ;
            }
			}

			// Alarm events are invoked by timer commands.  When an alarm
			// expires the event actions must be performed.

			if ("alarm".equals(name))
			{
				Alarm a = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,target) ;
				if (a != null)
            {
               if (!detach)
                  a.addEvent(event) ;
               else
                  a.removeEvent(event) ;
            }
			}

			// Label events are invoked with goto or gosub commands.
			// When a label module is entered the event actions must
			// be performed.

			if ("label".equals(name))
			{
				Module m = (Module) Module.getByKey(Module.getKeyTable(),cid,target) ;
				if (m != null)
            {
               if (!detach)
                  m.addEvent(event) ;
               else
                  m.removeEvent(event) ;
            }
			}

			// Media start events are invoked when media file starts playing.

			if ("mediastart".equals(name))
			{
           	String s1 = Variable.getStringLiteralValue(target) ;
		      File f = new File(getDirectory(),s1) ;
		      s1 = f.getPath().toUpperCase() ;
				KissObject k = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;
				if (k != null)
            {
               if (!detach)
                  k.addEvent(event) ;
               else
                  k.removeEvent(event) ;
            }
            else if (Kisekae.isMediaInstalled())
            {
					k = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,s1) ;
					if (k instanceof Video)
               {
                  if (!detach)
                     k.addEvent(event) ;
                  else
                     k.removeEvent(event) ;
               }
            }
			}

			// Media stop events are invoked when media file stops playing.

			if ("mediastop".equals(name))
			{
           	String s1 = Variable.getStringLiteralValue(target) ;
		      File f = new File(getDirectory(),s1) ;
		      s1 = f.getPath().toUpperCase() ;
				KissObject k = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;
				if (k != null)
            {
               if (!detach)
                  k.addEvent(event) ;
               else
                  k.removeEvent(event) ;
            }
            else if (Kisekae.isMediaInstalled())
            {
					k = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,s1) ;
					if (k instanceof Video)
               {
                  if (!detach)
                     k.addEvent(event) ;
                  else
                     k.removeEvent(event) ;
               }
            }
			}
		}
		catch (NumberFormatException e)
		{
			line = event.getLine() ;
			showError("FKiSS " + name + "(" + target + ") " + e.toString()) ;
		}

		// Now, for all actions in this event, we must identify the
		// action object.  When the event occurs, actions apply to
		// specific objects such as cels or alarms.  By attaching
		// the identified object to the action at initialization
		// time we can simplify the action implementation code.

      if (detach) return ;
		Enumeration actions = event.getActions() ;
		while (actions != null && actions.hasMoreElements())
		{
			FKissAction a = (FKissAction) actions.nextElement() ;
			String action = (String) a.getIdentifier() ;
			target = a.getFirstParameter() ;
			if (target == null) continue ;
			target = target.toUpperCase() ;

			// Decode parameter based upon the action type.  We catch
			// some parameter syntax errors here.

			try
			{
				if ("map".equals(action) || "unmap".equals(action) ||
					 "altmap".equals(action) || "move".equals(action) ||
					 "transparent".equals(action) || "ghost".equals(action) ||
					 "ghosted".equals(action))
					a.setObject(findGroupOrCel(target,true)) ;

				if ("movebyx".equals(action) || "movebyy".equals(action) ||
					 "moverandx".equals(action) || "moverandy".equals(action) ||
					 "moveto".equals(action) || "movetorand".equals(action))
					a.setObject(findGroupOrCel(target,true)) ;

				if ("iffixed".equals(action) || "ifnotfixed".equals(action) ||
					 "ifmoved".equals(action) || "ifnotmoved".equals(action))
					a.setObject(findGroupOrCel(target,true)) ;

				if ("ifmapped".equals(action) || "ifnotmapped".equals(action))
					a.setObject(findGroupOrCel(target,true)) ;

				if ("setfix".equals(action))
					a.setObject(findGroupOrCel(target,true)) ;

				if ("changecol".equals(action))
					a.setObject(Palette.getByKey(Palette.getKeyTable(),cid,new Integer(0))) ;

				if ("changeset".equals(action))
					a.setObject(PageSet.getByKey(PageSet.getKeyTable(),cid,target)) ;

            // Duplicate PlayFKiss bug that schedules Alarm(201) for Alarm(0201)
            if (!OptionsDialog.getPlayFKissCompatibility())
            {
   				if ("timer".equals(action) || "randomtimer".equals(action))
   					a.setObject(Alarm.getByKey(Alarm.getKeyTable(),cid,target)) ;
            }

				if ("goto".equals(action) || "gosub".equals(action) ||
					 "gotorandom".equals(action) || "gosubrandom".equals(action))
					a.setObject(Module.getByKey(Module.getKeyTable(),cid,target)) ;

				if ("movie".equals(action))
					a.setObject(Cel.findCel(target,this,null)) ;

				if ("sound".equals(action) || "music".equals(action) || "mediaplayer".equals(action))
					a.setObject(Audio.findAudio(target,this)) ;
			}
			catch (NumberFormatException e)
			{
				line = a.getLine() ;
				showError("FKiSS " + action + ": " + e.toString()) ;
			}
		}
   }


	// Most events apply to an object.  When we edit or update an event
   // we need to detach the old event from all objects.

   void detachEvent(FKissEvent event)
   {
      if (event == null) return ;
      attachEvent(event,true) ;
   }
   
   
   
   // Method to perform a syntax validation specific to the viewer
   // type. This method must be invoked after the configuration
   // is parsed and all objects and cels are created. Syntax validation
   // primarily ensures that FKiSS arguments and parameters exist amd
   // are of the correct type for the viewer.
   
   void validateFKissSyntax()
   {
		Enumeration events = (handler == null) ? null : handler.getEvents() ;
		while (events != null && events.hasMoreElements())
		{
			Vector v = (Vector) events.nextElement() ;
			if (v == null) continue ;
			for (int i = 0 ; i < v.size() ; i++)
			{
            // Validate the event parameters.
               
				FKissEvent event = (FKissEvent) v.elementAt(i) ;
            Vector params = event.getParameters() ;
            validateParameters(event,params) ;
               
            // Validate all the event action parameters.
               
            Enumeration actions =  event.getActions() ;
            if (actions == null) continue ;
            while (actions.hasMoreElements())
            {
               FKissAction action = (FKissAction) actions.nextElement() ;
               params = action.getParameters() ;
               validateParameters(action,params) ;
            }
         }
		}
      
      // Verify that all declared variables have been referenced.
      
      Enumeration references = variables.keys() ;
      while (references.hasMoreElements())
      {
         Object key = references.nextElement() ;
         Object o = variables.get(key) ;
         if (!(o instanceof Object [])) continue ;
         Object [] keyvalue = (Object []) o ;
         int line = ((Integer) keyvalue[0]).intValue() ;
         KissObject kiss = (KissObject) keyvalue[1] ;
         String name = (kiss != null) ? kiss.getName() : "" ;
         showWarning("[Line " + line + "] " + name 
            + " variable " + key + " is declared but never referenced") ;
      }
   }


	// This is the external entry to validate FKiSS event commands.  
   // This method is used to validate individual FKiSS event text
   // modified through the FKiSS Editor.

	void validateFKissSyntax(KissFrame parent, Vector events)
	{
      String compatibility = OptionsDialog.getCompatibilityMode() ;
      if (compatibility == null) return ;
   }


	// This method sets a bit field according to the recognized type
   // of the parameter.  Bit 0 is object, bit 1 is cel name, 
   // bit 2 is variable, bit 3 is number, bit 4 is string, 
   // bit 5 is celgroup, bit 6 is name, bit 7 is variable declaration,
   // bit 8 is generic '*', bit 9 is object literal.

	private int getParameterType(String s)
	{
      int b = 0 ;
      if (s == null) return 0 ;
      if (s.length() == 0) return 0 ;
      char c0 = s.charAt(0) ;
      char c1 = (s.length() > 1) ? s.charAt(1) : ' ' ;
      if (c0 == '#') b |= 513 ;                        // #nnn or #variable
      else if (Character.isDigit(c0)) b |= 8 ;         // number
      else if (c0 == '-') b |= 8 ;                     // negative number
      else if (c0 == '+') b |= 8 ;                     // positive number
      else if (c0 == '"') b |= 16 ;                    // string literal
      else if (c0 == '\'') b |= 16 ;                   // char literal
      else if (c0 == '!') b |= 32 ;                    // celgroup
      else if (c0 == '$') b |= 4 ;                     // indirect variable
      else if (c0 == '@') b |= 4 ;                     // local variable
      else if (c0 == '*') b |= 256 ;                   // generic value type
      else if (Character.isLetter(c0)) b |= (64+4) ;   // variable or name
      // Celnames are also strings.
      if (c0 == '"' && s.indexOf('.') > 0) b |= 2 ;
      // Numbers are also objects.
//    if (Character.isDigit(c0)) b |= 1 ;
      // Signed numbers should have digits.
      if ((c0 == '-' || c0 == '+') && !Character.isDigit(c1)) b &= 0xFFF7 ;
      // Objects should have digits unless we allow #variable.
      if (OptionsDialog.getCompatibilityMode() != null)
         if (c0 == '#' && !Character.isDigit(c1)) b &= 0xFDFE ;
      return b ;
   }


	// This method validates a parameter list. The KiSS object should be 
   // an event or action object as this is used to determine the valid
   // compatibility settings.

	private void validateParameters(KissObject kiss, Vector params)
	{
      if (kiss == null) return ;
      if (params == null) return ;
      
      for (int n = 0 ; n < params.size() ; n++)
      {
         int valid = 0 ;
         int code = -1 ;
         int mandatory = -1 ;
         int line = kiss.getLine() ;
         String name = kiss.getName() ;
         Object id = kiss.getIdentifier() ;
         String s = (id != null) ? id.toString() : "" ;
         
         if (kiss instanceof FKissEvent) 
         {
            code = EventHandler.getEventNameKey(s) ;
            valid = EventHandler.getEventParamType(code,n) ;
            mandatory = EventHandler.getMandatoryEventParams(code) ;
         }
         if (kiss instanceof FKissAction) 
         {
            code = EventHandler.getActionNameKey(s) ;
            valid = EventHandler.getActionParamType(code,n) ;
            mandatory = EventHandler.getMandatoryActionParams(code) ;
         }
         
         if (code < 0) break ;
         if (valid < 0) break ;
               
         // Bit 0 is object, bit 1 is cel name, bit 2 is variable, 
         // bit 3 is number, bit 4 is string, bit 5 is celgroup,
         // bit 6 is name, bit 7 is variable declaration, bit 8
         // is generic '*' value, bit 9 is object literal.

         String p = (String) params.elementAt(n) ;
         int type = getParameterType(p) ;
         if (valid == 0)
         {
            showWarning("[Line " + line + "] " + name 
               + " parameter " + (n+1) + " should not be specified.",p) ;
            continue ;
         }

         if (n == params.size()-1 && mandatory > n+1)
         {
            showWarning("[Line " + line + "] " + name 
               + " requires additional parameters.",name) ;
            continue ;
         }
         
         if ((valid & type) == 0)
         {
            String s1 = stringParameterType(valid) ;
            showWarning("[Line " + line + "] " + name 
               + " parameter " + (n+1) + " should be " + s1 + " type.",p) ;
            continue ;
         }
         
         // Verify that the object, cel, or celgroup exists. Watch for
         // object or cel group specifications that are variables.
               
         if ((valid & type & 547) != 0)
         {
            Object o = findGroupOrCel(p) ;
            if (o == null)
            {
               boolean fault = true ;
               char c0 = (p.length() > 0) ? p.charAt(0) : ' ' ;
               char c1 = (p.length() > 1) ? p.charAt(1) : ' ' ;
               String key = (p.length() > 1) ? p.substring(1) : "" ;
               if (!OptionsDialog.getVariableCase()) key = key.toUpperCase() ;
               if (c0 == '#' && c1 == '@') fault = false ;
               if (c0 == '#' && c1 == '$') fault = false ;
               if (c0 == '#' && Character.isLetter(c1)) fault = false ;
               if (c0 == '!' && variables.get(key) != null) fault = false ;
               
               if (fault && OptionsDialog.getShowUndefs())
               {
                  String s1 = stringParameterType(valid & type) + "." ;
                  showWarning("[Line " + line + "] " + name 
                     + " parameter " + (n+1) + " does not exist as " + s1,p) ;
                  continue ;
               }
               else
                  type |= 4 ;
            }
         }
               
         // Verify that the variables have been declared. Watch for
         // object or cel group specifications that are variables.
         // Also watch for alarm and label names.  Also watch for
         // local variables that have event name prefixes.
               
         if ((type & 68) != 0)
         {
            Object o = null ;
            String key = p ;
            if (!OptionsDialog.getVariableCase()) key = key.toUpperCase() ;
            if (key.startsWith("#") || key.startsWith("!")) 
               key = key.substring(1) ;
            
            // For local variables we either have the specification of
            // a label return, or a label local variable prefixed with
            // the label name.
            
            if (key.startsWith("@") && kiss instanceof FKissAction) 
            {
               o = variables.get(key) ;
               if (o == null)
               {
                  FKissEvent parent = ((FKissAction) kiss).getEvent() ;
                  String prefix = parent.getFirstParameter() ;
                  if (prefix == null) prefix = parent.getName() ;
                  if (!OptionsDialog.getVariableCase()) prefix = prefix.toUpperCase() ;
                  if (parent != null) key = prefix + "-" + key ;
               }
            }
            
            // Get the variable declaration.
            
            if (o == null) o = variables.get(key) ;
            
            // If not a variable, it may be a name. A name on an FKiSS event
            // is a declaration and not an error.
            
            if (o == null)
            {
               if ((type & 64) != 0)
               {
                  if (!(kiss instanceof FKissAction)) 
                     o = kiss ;
                  else if (code == 4 || code == 5 || code == 6 || code == 7 
                     || code == 8 || code ==9 || code == 26 || code == 106)
                     o = Alarm.getByKey(Alarm.getKeyTable(),cid,key) ;  
                  else if (code == 31 || code == 32 || code == 33 || code == 34
                     || code == 149) 
                     o = Module.getByKey(Module.getKeyTable(),cid,key) ; 
               }
               
               // If we still can't find it, show the warning.
               
               if (o == null && OptionsDialog.getShowUndefs())
               {
                  String s1 = stringParameterType(type) + "." ;
                  showWarning("[Line " + line + "] " + name 
                     + " parameter " + (n+1) + " is not a defined " + s1,p) ;
                  continue ;
               }
            }
            else 
               variables.put(key,new Integer(0)) ;
         }
      }
   }


	// This method returns a string according to the recognized type
   // of the parameter.  Bit 0 is object, bit 1 is cel name, 
   // bit 2 is variable, bit 3 is number, bit 4 is string, 
   // bit 5 is celgroup, bit 6 is name, bit 7 is variable
   // declaration, bit 8 is generic '*', bit 9 is object literal.

	private String stringParameterType(int n)
	{
      StringBuffer sb = new StringBuffer("") ;
      if ((n & 1) != 0) sb.append("object, ") ;
      if ((n & 2) != 0) sb.append("celname, ") ;
      if ((n & 4) != 0) sb.append("variable, ") ;
      if ((n & 8) != 0) sb.append("number, ") ;
      if ((n & 16) != 0) sb.append("string, ") ;
      if ((n & 32) != 0) sb.append("celgroup, ") ;
      if ((n & 64) != 0) sb.append("name, ") ;
      if ((n & 256) != 0) sb.append("'*', ") ;
      if ((n & 512) != 0) sb.append("#object, ") ;
      if (sb.length() == 0) return "" ;
      sb.delete(sb.length()-2,sb.length()) ;
      n = sb.lastIndexOf(",") ;
      if (n > 0) sb.replace(n,n+1," or") ;
      return sb.toString() ;
   }


	// Method to activate a configuration.  This initiates any necessary
	// event activities, establishes the variable set, and saves the initial
   // states for cels, groups, and pages.

	void activate(PanelFrame panel)
	{
  		createtime = System.currentTimeMillis() ;
		if (OptionsDialog.getDebugControl())
			System.out.println("Configuration " + file + " begin activation.") ;

		// Retain the initial states for all cels, groups, and pages.

		for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
			c.saveState(cid,"initial") ;
         if (c instanceof JavaCel) ((JavaCel) c).setPanel(panel) ;
         if (c instanceof Video) ((Video) c).setPanel(panel) ;
         c.setInput(c.getInitInput()) ;
      }
		for (int i = 0 ; i < groups.size() ; i++)
			((Group) groups.elementAt(i)).saveState(cid,"initial") ;
		for (int i = 0 ; i < pages.size() ; i++)
			((PageSet) pages.elementAt(i)).saveState(cid,"initial") ;

      // Initialize the panel frame for startup.  We begin with the first
      // page that is visible and has objects defined on it.  The start
      // page can be changed during initialize and begin events.

      if (panel != null)
      {
      	int n = 0 ;
      	for (n = 0 ; n < pages.size() ; n++)
         {
         	PageSet pageset = (PageSet) pages.elementAt(n) ;
         	if (pageset.isVisible() && pageset.getGroupCount() > 0) break ;
         }
         if (n >= pages.size()) n = 0 ;
      	panel.initpage(n) ;
      }

      // Ensure all FKiSS alarms are enabled.

      if (alarms != null)
      {
         for (int i = 0 ; i < alarms.size() ; i++)
            ((Alarm) alarms.elementAt(i)).enableAlarm() ;
      }

		// If we have FKiSS alarms, start the alarm timer.

		timer = new AlarmTimer() ;
		timer.startTimer(new Vector()) ;
      timer.suspendTimer(true) ;

		// If we have animated cels, start the GIF timer.

		animator = new GifTimer() ;
		animator.startTimer(cels) ;
      animator.suspendTimer(true) ;
      animator.setEnabled(true) ;
		animator.setPanelFrame(panel) ;

		// If we have FKiSS events, start the event handler threads.

		if (handler == null) handler = new EventHandler() ;
		handler.startEventHandler() ;
		for (int i = 1 ; i < OptionsDialog.getEventQueues() ; i++)
			(new EventHandler()).startEventHandler() ;
      handler.suspendEventHandler(true) ;
		handler.setPanelFrame(panel) ;

		// Perform any data set initialization events.  These events are
      // fired directly and are not queued for later processing.

		variable.clear() ;
      activated = true ;
      if (handler != null)
      {
			Vector v = handler.getEvent("initialize") ;
			EventHandler.fireEvents(v,panel,Thread.currentThread(),null) ;

			// Process all version events up to and including version 6.
			//  Version 1 -> FKiSS
			//  Version 2 -> FKiSS 2
			//  Version 3 -> FKiSS 2.1
			//  Version 4 -> FKiSS 3
			//  Version 5 -> FKiSS 4
			//  Version 6 -> FKiSS 5 (Kisekae UltraKiss)

         if (handler == null) 
            throw new OutOfMemoryError() ;
			v = handler.getEvent("version") ;
			if (v != null)
			{
				for (int i = 0 ; i < v.size() ; i++)
				{
					FKissEvent event = (FKissEvent) v.elementAt(i) ;
					String s = event.getFirstParameter() ;
					if (s != null)
					{
						try
	               {
                     // Version temp change for test
							if (Integer.parseInt(s) <= 6)  
                     {
	                  	event.fireEvent(panel,Thread.currentThread(),null) ;
                     }
	               }
						catch (NumberFormatException e) { }
					}
				}
         }
		}

		// Activate any video objects that might exist.

		for (int i = 0 ; i < movies.size() ; i++)
		{
         if (Kisekae.isMediaInstalled())
         {
   			Video v = (Video) movies.elementAt(i) ;
   			v.setActivated(true) ;
         }
		}

      // Activate initial options.

      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.updateMenuOptions() ;

		if (OptionsDialog.getDebugControl())
			System.out.println("Configuration " + file + " activated.") ;
	}


	// The close method is called when we are about to terminate
	// the current data set.  This method performs any termination
	// actions that are required.

   void close() { close(false,true) ; }
	void close(boolean restart, boolean resetoptions)
	{
		String s = (file == null) ? "" : file ;
		if (OptionsDialog.getDebugControl())
			System.out.println("Configuration " + s + " begin close.") ;

      // Ensure that we can have no breakpoint restarts.

      EventHandler.setModal(null) ;
      FKissEvent.setBreakPause(null,false) ;

		// Shut down the timer and the event handler.

      if (activated)
      {
         stopTimer() ;
			if (animator != null) animator.stopTimer() ;
			EventHandler.stopEventHandler() ;
         EventHandler.clearEventQueue() ;
	      EventHandler.setPanelFrame(null);
	      GifTimer.setPanelFrame(null);
         activated = false ;
      }

		// Flush all image data.  This cleans up our memory allocation
		// and seems to stop odd things from happening.  Note: this
      // causes GIF images to not display on a restart.
/*
		if (cels != null)
		{
			for (int i = 0 ; i < cels.size() ; i++)
			{
				Cel c = (Cel) cels.elementAt(i) ;
				if (c.isCopy()) continue ;
				Image img = c.getImage() ;
				if (img != null) img.flush() ;
			}
		}
*/
      // Terminate any configuration mediaplayer that is active.

      if (mediaframe != null) mediaframe.stop() ;
      mediaframe = null ;
		timer = null ;
      animator = null ;

      // If restarting from memory we must re-establish initial object
      // states.  Internal objects must be removed from the configuration
      // and cels must have their original lock values set.  All object
      // movement restrictions are cleared.

      if (restart)
      {
         sf = 1.0f ;
         Audio.stop() ;
         if (Kisekae.isMediaInstalled()) Video.stop() ;

         // Remove any internal objects.

         cels = removeInternal(cels) ;
         celCount = cels.size() ;
         groups = removeInternal(groups) ;
         palettes = removeInternal(palettes) ;
         paletteCount = palettes.size() ;
         pages = removeInternal(pages) ;
         pageCount = pages.size() ;
         celgroups = removeInternal(celgroups) ;

         // Reset object position, visibility, lock values and transparencies.
         // We also clear all object attachments and movement restrictions.

         for (int i = 0 ; i < groups.size() ; i++)
         {
            Group group = (Group) groups.elementAt(i) ;
            group.restoreState(cid,"initial",true) ;
            group.reset() ;
         }

         // Remove all page set panelframe states.

         for (int i = 0 ; i < pages.size() ; i++)
         {
            PageSet page = (PageSet) pages.elementAt(i) ;
            page.removeState(cid,"panelframe") ;
            page.restoreState(cid,"initial") ;
         }

         // Reset all alarms.

         for (int i = 0 ; i < alarms.size() ; i++)
         {
            Alarm alarm = (Alarm) alarms.elementAt(i) ;
            alarm.init() ;
         }

         // Reset all components.

         for (int i = 0 ; i < comps.size() ; i++)
         {
            JavaCel c = (JavaCel) comps.elementAt(i) ;
            c.reset() ;
         }

         // Reset all movies.

         for (int i = 0 ; i < movies.size() ; i++)
         {
            Video v = (Video) movies.elementAt(i) ;
            v.reset() ;
         }

         // Cancel any valuepool settings.
         
         propertypool.clear() ;
			System.out.println("Restart configuration " + s) ;
         return ;
      }

		// Terminate any audio in progress.

		for (int i = 0 ; i < sounds.size() ; i++)
		{
			Audio a = (Audio) sounds.elementAt(i) ;
			a.close() ;
		}

		// Terminate any video in progress.

		for (int i = 0 ; i < movies.size() ; i++)
		{
         if (Kisekae.isMediaInstalled())
         {
   			Video v = (Video) movies.elementAt(i) ;
   			v.close() ;
         }
		}
      
      // Save any valuepool property settings.
      
      saveValuepoolProperties() ;
      propertypool.clear() ;

		// Release our current object storage.

		cels.removeAllElements() ;
		palettes.removeAllElements() ;
		sounds.removeAllElements() ;
		movies.removeAllElements() ;
		comps.removeAllElements() ;
		pages.removeAllElements() ;
		groups.removeAllElements() ;
		alarms.removeAllElements() ;
		frames.removeAllElements() ;
		labels.removeAllElements() ;
		celgroups.removeAllElements() ;
      variables.clear() ;
		variable.clear() ;
		handler = null ;
		screen = null ;
      ref = null ;
      b = null ;

		// Clear all static index tables.

		Configuration.clearTable(cid) ;
		Cel.clearTable(cid) ;
		Group.clearTable(cid) ;
		CelGroup.clearTable(cid) ;
		Palette.clearTable(cid) ;
		PageSet.clearTable(cid) ;
		Alarm.clearTable(cid) ;
		Module.clearTable(cid) ;
		State.clearTable(cid) ;
		Audio.clearTable(cid) ;
		JavaCel.clearTable(cid) ;
      TextObject.clearTable(cid) ;
      if (Kisekae.isMediaInstalled()) Video.clearTable(cid) ;
      FKissAction.clearGroupsetPool() ;

		// Reset any configuration variables.

		celCount = 0 ;
		pageCount = 0 ;
		paletteCount = 0 ;
		audioCount = 0 ;
		videoCount = 0 ;

      // Reset initial options.  If we are not resetting options then the
      // configuration may be reused.  We retain the zip file reference on 
      // reuse so that cels and other objects can be reloaded.

		if (resetoptions)
		{
         MainFrame mf = Kisekae.getMainFrame() ;
         OptionsDialog options = (mf != null) ? mf.getOptionsDialog() : null ;
         if (options != null) options.resetOptions() ;
         if (mf != null) mf.updateMenuOptions() ;

   		// Close the parent archive file.

   		if (zip != null)
   		{
   			try { 
               zip.close() ; 
               zip.flush() ;
            }
   			catch (IOException e) { }
   		}
         zip = null ;
         ze = null ;
      }

		// Invoke the garbage collector.

		System.out.println("Close configuration " + s) ;
		Runtime.getRuntime().gc() ;
	}
   
   
   // A function to stop the timer.  
   
   private void stopTimer()
   {
		if (timer != null) 
      { 
         timer.stopTimer() ; 
         timer.resetQueue() ; 
      }
      if (!OptionsDialog.getTimerOn())
      {
         if (alarms != null)
         {
            for (int i = 0; i < alarms.size(); i++)
            {
               Alarm a = (Alarm) alarms.elementAt(i) ;
               Timer t = a.getTimer() ;
               if (t != null) t.stop() ;
            }
         }
      }
      
   }


   // A function to remove internal objects from a configuration.
   // Internal objects are cels, groups, palettes and other items
   // that have been created through edit operations and not saved.

   private Vector removeInternal(Vector list)
   {
      if (list == null) return null ;
      Vector v = new Vector() ;
      for (int i = 0 ; i < list.size() ; i++)
      {
         Object o = list.elementAt(i) ;
         if (!(o instanceof KissObject)) continue ;
         KissObject ko = (KissObject) o ;
         if (ko.isInternal())
         {
            if (ko instanceof Cel)
            {
               Cel.removeObject(Cel.getKeyTable(),cid,ko.getIdentifier(),ko) ;
					Cel.removeObject(Cel.getKeyTable(),cid,ko.getPath().toUpperCase(),ko) ;
            }
            if (ko instanceof Group)
            {
               Group.removeObject(Group.getKeyTable(),cid,ko.getIdentifier(),ko) ;
            }
            if (ko instanceof CelGroup)
            {
               CelGroup.removeObject(CelGroup.getKeyTable(),cid,ko.getIdentifier(),ko) ;
            }
            if (ko instanceof Palette)
            {
               Palette.removeObject(Palette.getKeyTable(),cid,ko.getIdentifier(),ko) ;
					Palette.removeObject(Palette.getKeyTable(),cid,ko.getPath().toUpperCase(),ko) ;
            }
            if (ko instanceof PageSet)
            {
               PageSet.removeObject(PageSet.getKeyTable(),cid,ko.getIdentifier(),ko) ;
					PageSet.removeObject(PageSet.getKeyTable(),cid,ko.getUniqueID(),ko) ;
            }
            continue ;
         }
         else
            v.addElement(ko) ;
      }
      return v ;
   }


	// The write method updates the configuration memory copy to
   // show the current state of the configuration objects.  Page
	// set declarations will be rewritten to reflect the initial
	// group positions.  The new byte array is returned.

	byte [] write() throws IOException
	{
		if (error) return null ;
		celtabstops = new int [4] ;
		celtabstops[0] = -1 ;
		celtabstops[1] = -1 ;
		celtabstops[2] = -1 ;
		celtabstops[3] = -1 ;

		// Open the output stream.

   	ByteArrayOutputStream out = new ByteArrayOutputStream() ;
      String s = "; Kisekae UltraKiss configuration file created on " + (new Date()).toString() ;
      writeLine(out,s) ;

      // Sort our cels by z-level and our events by source line number.

      Vector sortedcels = new Vector() ;
      Vector sortedevents = new Vector() ;
      if (cels != null)
      {
         for (int i = cels.size()-1 ; i >= 0 ; i--)
            sortedcels.addElement(cels.elementAt(i)) ;
      }
      if (handler != null)
      {
      	Enumeration enum1 = handler.getEvents() ;
         while (enum1.hasMoreElements())
         {
         	Object o = enum1.nextElement() ;
            if (o instanceof FKissEvent)
            	sortedevents.addElement(o) ;
            if (o instanceof Vector)
            	sortedevents.addAll((Vector) o) ;
         }
      }
      Collections.sort(sortedcels, new LevelComparator()) ;
      Collections.sort(sortedevents, new EventComparator()) ;

      // Run through the cels to figure out the maximum field sizes for
      // output formatting.  Fields are delimited by tabs.

      for (int i = 0 ; i < sortedcels.size() ; i++)
      {
      	Cel c = (Cel) sortedcels.elementAt(i) ;
         s = c.formatCel(this) ;
         if (s != null)
         {
            int t0 = s.indexOf('\t') ;
            int t1 = s.indexOf('\t',t0+1) ;
            int t2 = s.indexOf('\t',t1+1) ;
            int t3 = s.indexOf('\t',t2+1) ;
            if (t0 < 0) t0 = s.length() ;
            if (t1 < 0) t1 = s.length() ;
            if (t2 < 0) t2 = s.length() ;
            if (t3 < 0) t3 = s.length() ;
            celtabstops[0] = Math.max(celtabstops[0],t0) ;
            celtabstops[1] = Math.max(celtabstops[1],t1-t0) ;
            celtabstops[2] = Math.max(celtabstops[2],t2-t1) ;
            celtabstops[3] = Math.max(celtabstops[3],t3-t2) ;
         }
      }
      celtabstops[0] += 1 ;
      celtabstops[1] += celtabstops[0] + 1 ;
      celtabstops[2] += celtabstops[1] + 1 ;
      celtabstops[3] += celtabstops[2] + 1 ;

      // Write the configuration file the Kisekae UltraKiss way.
      // We write the screen section, palette section, cel section,
      // page set section, and finally the FKiSS section.

      Vector leadtext = removeSection(getLeadComment()) ;
      Enumeration enum1 = leadtext.elements() ;
      while (enum1.hasMoreElements())
      {
      	s = (String) enum1.nextElement() ;
         if (s.startsWith("; Kisekae UltraKiss")) continue ;
         if (s.startsWith("; FKiSS code compatibility")) continue ;
      	writeLine(out,s) ;
      }
      
      // Write the actual FKiSS level.
      
      if (fkisslevel != 0)
         writeLine(out,"; FKiSS code compatibility level is " + fkisslevel) ;
      
      // Write the set specific options.
      
      OptionsDialog.writeOptions(out) ;
     	writeLine(out," ") ;
      writeLine(out,sectionheadings[0]) ;

      // The memory specification.

      if (memory >= 0)
      {
			leadtext = removeSection(leadmemory) ;
	      enum1 = leadtext.elements() ;
	      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
         StringBuffer sb = new StringBuffer("=" + memory) ;
         int n = OptionsDialog.getCommentCol() ;
         if (n < 0) n = sb.length() + 1 ;
         if (n <= sb.length()) n = sb.length() + 1 ;
         while (sb.length() < n) sb.append(' ') ;
   		if (trailmemory != null) sb.append(trailmemory.trim()) ;
	      s = sb.toString() ;
	      writeLine(out,s) ;
      }

      // The border color specification.

      if (border >= 0)
      {
			leadtext = removeSection(leadborder) ;
	      enum1 = leadtext.elements() ;
	      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
         StringBuffer sb = new StringBuffer() ;
         if (rgbborder && !OptionsDialog.getPlayFKissCompatibility())
	         sb.append("[" + ((border&0xFF0000) >> 16) + "," + ((border&0xFF00) >> 8) + "," + (border & 0xFF)) ;
         else
            sb.append("[" + border) ;
         int n = OptionsDialog.getCommentCol() ;
         if (n < 0) n = sb.length() + 1 ;
         if (n <= sb.length()) n = sb.length() + 1 ;
         while (sb.length() < n) sb.append(' ') ;
   		if (trailborder != null) sb.append(trailborder.trim()) ;
	      s = sb.toString() ;
	      writeLine(out,s) ;
      }

      // The screen size specification.

      if (screen != null)
      {
			leadtext = removeSection(leadsize) ;
	      enum1 = leadtext.elements() ;
	      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
         StringBuffer sb = new StringBuffer() ;
			sb.append("(" + screen.width + "," + screen.height + ")") ;
         int n = OptionsDialog.getCommentCol() ;
         if (n < 0) n = sb.length() + 1 ;
         if (n <= sb.length()) n = sb.length() + 1 ;
         while (sb.length() < n) sb.append(' ') ;
   		if (trailscreen != null) sb.append(trailscreen.trim()) ;
	      s = sb.toString() ;
	      writeLine(out,s) ;
      }

      // The palette specifications.

      if (palettes.size() > 0)
      {
      	Palette p = (Palette) palettes.elementAt(0) ;
			leadtext = removeSection(p.getLeadComment()) ;
	      enum1 = leadtext.elements() ;
	      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
        	writeLine(out," ") ;
         writeLine(out,sectionheadings[1]) ;
	      for (int i = 0 ; i < palettes.size() ; i++)
				writePalette(out,(Palette) palettes.elementAt(i),(i != 0)) ;
      }

      // The cel specifications.

      if (cels.size() > 0)
      {
      	Cel c = (Cel) sortedcels.elementAt(0) ;
			leadtext = removeSection(c.getLeadComment()) ;
	      enum1 = leadtext.elements() ;
	      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
        	writeLine(out," ") ;
         writeLine(out,sectionheadings[2]) ;
	      for (int i = 0 ; i < sortedcels.size() ; i++)
				writeCel(out,(Cel) sortedcels.elementAt(i),(i != 0)) ;
      }

      // The page set specifications.

      if (pages.size() > 0)
      {
      	PageSet p = (PageSet) pages.elementAt(0) ;
			leadtext = removeSection(p.getLeadComment()) ;
	      enum1 = leadtext.elements() ;
         while (enum1.hasMoreElements())
         {
         	s = (String) enum1.nextElement() ;
            if (s.startsWith("; Kisekae UltraKiss")) continue ;
            if (s.startsWith("; Page ")) continue ;
         	writeLine(out,s) ;
         }
        	writeLine(out," ") ;
         writeLine(out,sectionheadings[3]) ;
	      for (int i = 0 ; i < pages.size() ; i++)
				writePageSet(out,(PageSet) pages.elementAt(i),(i != 0)) ;
      }

      // The FKiSS event specifications.

      if (sortedevents.size() > 0)
      {
      	Object o = sortedevents.elementAt(0) ;
         if (o instanceof FKissEvent)
         {
				FKissEvent event = (FKissEvent) o ;
				leadtext = removeSection(event.getLeadComment()) ;
		      enum1 = leadtext.elements() ;
		      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
         }
        	writeLine(out," ") ;
         writeLine(out,sectionheadings[4]) ;
        	writeLine(out,";@EventHandler") ;
        	writeLine(out," ") ;
			for (int i = 0 ; i < sortedevents.size() ; i++)
	      {
	      	o = sortedevents.elementAt(i) ;
	         if (!(o instanceof FKissEvent)) continue ;
				writeEvent(out,(FKissEvent) o,(i != 0)) ;
         }
      }

      // We write the trailing comments which are all lines in the
      // configuration file after the last successfully parsed line.

      enum1 = getTrailComment().elements() ;
      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;

		// Close the output stream and return a new configuration memory buffer.

		if (out != null) { out.close() ;  }
		return out.toByteArray() ;
   }


	// A function to write a line to our output stream.

	private void writeLine(OutputStream out, String s)
		throws IOException
	{
      if (s == null || out == null) return ;
      String encoding = Kisekae.getLanguageEncoding() ;
		String ls = System.getProperty("line.separator") ;
		if (ls == null) ls = "\n" ;
		out.write((encoding != null) ? s.getBytes(encoding) : s.getBytes()) ;
		out.write((encoding != null) ? ls.getBytes(encoding) : ls.getBytes()) ;
	}


	// A function to format and write a palette declaration.  Internal
   // palettes are not written.

	private void writePalette(OutputStream out, Palette palette, boolean write)
		throws IOException
	{
      if (out == null) return ;
      if (palette == null) return ;
   	if (palette.isInternal()) return ;
      String directory = getDirectory() ;
		String name = palette.getName() ;
      String path = palette.getPath() ;
		String comment = palette.getComment() ;
      String relativename = palette.getRelativeName() ;
      if (directory != null)
      {
	      if (path.startsWith(directory))
	      	relativename = path.substring(directory.length()) ;
	      if (relativename.startsWith(File.separator))
	      	relativename = relativename.substring(File.separator.length()) ;
      }
      if (relativename.indexOf(' ') > 0) relativename = "\"" + relativename + "\"" ;
      StringBuffer sb = new StringBuffer() ;
		sb.append("%" + relativename) ;
      int n = OptionsDialog.getCommentCol() ;
      if (n < 0) n = sb.length() + 1 ;
      if (n <= sb.length()) n = sb.length() + 1 ;
      while (sb.length() < n) sb.append(' ') ;
   	if (comment != null) sb.append(comment.trim()) ;
	   String s = sb.toString() ;
		Vector leadtext = removeSection(palette.getLeadComment()) ;
      Enumeration enum1 = leadtext.elements() ;
      while (enum1.hasMoreElements() && write) writeLine(out,(String) enum1.nextElement()) ;
		writeLine(out,s) ;
      enum1 = palette.getTrailComment().elements() ;
      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
   }


	// A function to format and write a cel declaration.  Internal cels
   // are not written.

	private void writeCel(OutputStream out, Cel cel, boolean write)
		throws IOException
	{
      if (out == null) return ;
      if (cel == null) return ;
   	String s = cel.formatCel(this) ;
      if (s == null) return ;

      // Cels with no group, or cels related to a group that does not
      // contain this cel are not written.

      Object o = cel.getGroup() ;
      if (!(o instanceof Group)) return ;
      Vector v = ((Group) o).getCels() ;
      if (v == null) return ;
      if (!v.contains(cel)) return ;

		// Write the comment.  This includes the Cel extension commands.
		// Note the Cel extensions must start immediately after the semicolon.

      s = s.replace('\t',' ') ;
      StringBuffer sb = new StringBuffer(s) ;
      String newcomment = cel.buildComment() ;
      int n = OptionsDialog.getCommentCol() ;
      if (n < 0) n = sb.length() + 1 ;
      if (n <= sb.length()) n = sb.length() + 1 ;
      while (sb.length() < n) sb.append(' ') ;
		if (newcomment != null) sb.append(newcomment) ;
		Vector leadtext = removeSection(cel.getLeadComment()) ;
      Enumeration enum1 = leadtext.elements() ;
      while (enum1.hasMoreElements() && write) writeLine(out,(String) enum1.nextElement()) ;
		writeLine(out,sb.toString()) ;
      enum1 = cel.getTrailComment().elements() ;
      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
	}


	// A function to format and write a pageset declaration.  Current group
	// positions are written if requested, otherwise the initial positions
	// are written.  Internal page sets are not written.

	private void writePageSet(OutputStream out, PageSet page, boolean write)
   	throws IOException
   {
      if (out == null) return ;
      if (page == null) return ;
   	if (page.isInternal()) return ;

      // Write position entries for all groups in the configuration.
      // Groups not on this page will have an '*' written for their
      // position, as will cloned groups.

      Object [] positions = new Object[getMaxGroupNumber()+1] ;
      for (int i = 0 ; i < page.getGroupCount() ; i++)
      {
			Group g = page.getGroup(i) ;
         if (g.isCloned()) continue ;
			Integer group = (Integer) g.getIdentifier() ;
			int id = group.intValue() ;
         if (id >= positions.length) continue ;
			Point p = page.getInitialGroupPosition(group) ;
			positions[id] = p ;
      }

      // Output the heading lines.

		Vector leadtext = removeSection(page.getLeadComment()) ;
      Enumeration enum1 = leadtext.elements() ;
      while (enum1.hasMoreElements())
      {
      	String s = (String) enum1.nextElement() ;
         if (s.startsWith("; Page ")) continue ;
      	if (write) writeLine(out,s) ;
      }
   	String s = "; Page " + page.getIdentifier() ;
      writeLine(out,s) ;

      // Output the page lines.

		Integer mp = page.getInitialMultiPalette() ;
		s = "$" + mp ;
      for (int i = 0 ; i < positions.length ; i++)
      {
         if (s.length() > 80)
         {
         	writeLine(out,s) ;
            s = "  " ;
         }
      	if (positions[i] == null)
         	s += " *" ;
         else if (positions[i] instanceof Point)
         {
         	Point p = (Point) positions[i] ;
         	s += " " + p.x + "," + p.y  ;
         }
      }
      writeLine(out,s) ;
      enum1 = page.getTrailComment().elements() ;
      while (enum1.hasMoreElements()) writeLine(out,(String) enum1.nextElement()) ;
   }


	// A function to format and write an event declaration.

	void writeEvent(OutputStream out, FKissEvent event, boolean write)
		throws IOException
	{
      if (out == null) return ;
      if (event == null) return ;
   	int nestlevel = 1 ;
      int priorline = 0 ;
      String comment = null ;
      String trailtext = null ;
      StringBuffer sb = new StringBuffer(";@") ;
      int line = event.getLine() ;
      priorline = line ;
      if ("EventHandler".equals(event.getIdentifier())) return ;

      // Compose the event line.

		Vector leadtext = removeSection(event.getLeadComment()) ;
      Enumeration enum1 = leadtext.elements() ;
      while (enum1.hasMoreElements() && write) writeLine(out,(String) enum1.nextElement()) ;
      sb.append(event.getName()) ;
      comment = event.getComment() ;

      // Write all action statements for this event.  This function
      // tracks the if-else-endif nesting level and formats the output
      // line appropriately.

      Enumeration actions = event.getActions() ;
      while (actions != null && actions.hasMoreElements())
      {
      	FKissAction a = (FKissAction) actions.nextElement() ;
         trailtext = a.getFirstTrailComment() ;
         int actionline = a.getLine() ;

         // If new line, close prior line.

         if (actionline != line)
         {
            if (trailtext != null)
            {
               sb.append(' ') ;
               sb.append(trailtext) ;
            }
            else
            {
            	int n = OptionsDialog.getCommentCol() ;
               if (n < 0) n = sb.length() + 1 ;
               if (n <= sb.length()) n = sb.length() + 1 ;
               while (sb.length() < n) sb.append(' ') ;
            	if (comment != null) sb.append("; " + comment) ;
               else if (OptionsDialog.getWriteComment()) sb.append(";") ;
            }

            // Replace null characters with spaces.

            for (int i = 0 ; i < sb.length() ; i++)
              	if (sb.charAt(i) == 0) sb.setCharAt(i,' ') ;
         	writeLine(out,sb.toString()) ;
            sb = new StringBuffer(";@") ;
            line = actionline ;
         }

         // Write the action statement.

         String s = (String) a.getIdentifier() ;
         if ("else".equals(s) || "endif".equals(s) ||
             "elseifequal".equals(s) || "elseifnotequal".equals(s) ||
             "elseifgreaterthan".equals(s) || "elseiflessthan".equals(s) ||
             "next".equals(s) || "endwhile".equals(s)) nestlevel-- ;
	      enum1 = a.getLeadComment().elements() ;
      	while (enum1.hasMoreElements()) 
            writeLine(out,(String) enum1.nextElement()) ;
         int n = OptionsDialog.getIndentSpace() ;
         if (n < 1) n = 1 ;
         if (nestlevel < 1) nestlevel = 1 ;
         int multiplier = (line == priorline) ? 1 : nestlevel ;
         for (int i = 0 ; i < n*multiplier ; i++) sb.append(' ') ;
         if ("ifequal".equals(s) || "ifnotequal".equals(s) ||
             "ifgreaterthan".equals(s) || "iflessthan".equals(s) ||
             "else".equals(s) ||
             "elseifequal".equals(s) || "elseifnotequal".equals(s) ||
             "elseifgreaterthan".equals(s) || "elseiflessthan".equals(s) ||
             "for".equals(s) || "while".equals(s)) nestlevel++ ;
			sb.append(a.getName()) ;
         comment = a.getComment() ;
         priorline = line ;
      }

      // Write the final line.

      if (trailtext != null)
      {
         sb.append(' ') ;
         sb.append(trailtext) ;
      }
      else
      {
        	int n = OptionsDialog.getCommentCol() ;
         if (n < 0) n = sb.length() + 1 ;
         if (n <= sb.length()) n = sb.length() + 1 ;
         while (sb.length() < n) sb.append(' ') ;
        	if (comment != null) sb.append("; " + comment) ;
         else if (OptionsDialog.getWriteComment()) sb.append(";") ;
      }

      // Replace null characters with spaces.

      for (int i = 0 ; i < sb.length() ; i++)
	     	if (sb.charAt(i) == 0) sb.setCharAt(i,' ') ;
     	writeLine(out,sb.toString()) ;
      enum1 = event.getTrailComment().elements() ;
      while (enum1.hasMoreElements()) 
         writeLine(out,(String) enum1.nextElement()) ;
	}


   // A function to remove a section heading with leading blank line
   // from a leading text vector.  This function always returns a
   // vector object.

   static Vector removeSection(Vector v)
   {
   	int n = 0 ;
   	Vector result = new Vector() ;
   	if (v == null) return result ;
      if (v.size() <= 1) return v ;
      String s1 = (String) v.elementAt(n++) ;
      String s2 = (String) v.elementAt(n++) ;

      // Scan the text vector.  If we have a blank line followed by any
      // section heading, drop the heading and check the remaining text.

      while (n <= v.size()+1)
      {
         boolean loop = false ;
         if (s1.trim().length() == 0)
         {
            for (int i = 0 ; i < sectionheadings.length ; i++)
            {
               if (s2.startsWith(sectionheadings[i]))
               {
                  s1 = (n < v.size()) ? (String) v.elementAt(n) : "" ;
                  n++ ;
                  s2 = (n < v.size()) ? (String) v.elementAt(n) : "" ;
                  n++ ;
                  loop = true ;
                  break ;
               }
            }
            if (loop) continue ;
         }

         // Retain this non-heading line.

         if (n > v.size()+1) break ;
      	result.addElement(s1) ;
         s1 = s2 ;
         s2 = (n < v.size()) ? (String) v.elementAt(n) : "" ;
         if (n++ > v.size()) break ;
      }
      return result ;
   }


	// Configuration element parsing methods
	// -------------------------------------

	// Method to parse the CEL declaration line. The format is:
	// #<group number>[.<flex value>]
   //  <cel filename>
   //  [*palette number[.multipaltette]]
	//	 [:<page> <page> ... ]
	//  [; [%t <transp>] [%g] [%u] [%c <color>] [%size[w,h]] [%offset[x,y]]
   //  [[!<group>] [:<frame> ... ] ... ]]]
	//
	// %t <transp> sets the initial transparency of the cel
	// %g makes the cel a ghost cel
	// %c <color> defines a cel transparent color
	// %offset [x,y] defines the offset for the cel
	// %size [w,h] defines the width and height for the cel
	// <!group> makes the cel being defined part of the specified group
	// <:<frame> makes the cel part of the specific frame in the group

	private Cel parseCel(String s)
	{
		StringTokenizer st = new StringTokenizer(s," \t") ;

		// First token is group and flex value.  Format: #group.flex
      // There can be space between the '#' and the group number.

      int groupnum = 0 ;
		String token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
      if ("#".equals(token)) token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
		int i = token.indexOf(".") ;
		if (i < 0) i = token.length() ;

      // Parse the group value.

      int n = (token.length() > 0 && token.charAt(0) == '#') ? 1 : 0 ;
		String value = token.substring(n,i) ;
		try {	groupnum = Integer.parseInt(value) ; }
		catch (NumberFormatException e)
		{ showError("Non-numeric group value " + value,value) ; }
		Integer group = new Integer(groupnum) ;

		// Now to parse the optional flex value.

      String file = null ;
		Integer flex = null ;
		if (i < token.length())
		{
			value = token.substring(i+1,token.length()) ;
			if (value.length() > 0)
			{
				try {	flex = new Integer(value) ; }
				catch (NumberFormatException e)
				{ showError("Non-numeric fix value " + value,value) ; }
			}
         else
         {
            file = "." ;
            i = token.length() ;
         }
		}

		// Next token is the cel image file name.  Format: name.cel
      // Unless the flex value .flex was preceeded by white space.

      if (file == null)
		   file = (st.hasMoreTokens()) ? st.nextToken() : " " ;
		if (file.length() > 0 && file.charAt(0) == '.' && i == token.length())
      {
			value = file.substring(1,file.length()) ;
         file = null ;

         // We should have the flex value, unless the period was both
         // preceeded and terminated by white space.

         if (value.length() == 0)
         {
   			file = (st.hasMoreTokens()) ? st.nextToken() : " " ;
				try {	flex = new Integer(file) ; value = file ; file = null ; }
				catch (NumberFormatException e) { }
         }

         // Parse the value for a numeric flex entry.

			if (value.length() > 0)
			{
				try {	flex = new Integer(value) ; }
				catch (NumberFormatException e)
				{ showError("Non-numeric lock value " + value,value) ; }
			}

         // Establish the file name.

         if (file == null)
			   file = (st.hasMoreTokens()) ? st.nextToken() : " " ;
      }

      // Now we have the cel image file name.  It may be immediately
      // followed by a semicolon or a colon or an asterisk.  We need to
      // watch for names enclosed in string quotes.

		while (!(file.equals(" ")) && file.length() > 1 && file.charAt(0) == '\"')
      {
			if (file.charAt(file.length()-1) == '\"')
         {
				file = file.substring(1,file.length()-1) ;
            break ;
         }
         else
            file += (st.hasMoreTokens()) ? (" " + st.nextToken()) : "\"" ;
      }

      // Watch for next field terminating ':' or ';' without leading space.

		int j1 = file.indexOf(';') ;
		int j2 = file.indexOf(':') ;
      if (j1 < 0 ) j1 = j2 ;
      if (j1 > 0 && j2 > 0) j1 = Math.min(j1,j2) ;
		int j3 = file.indexOf('*') ;
      if (j1 < 0 ) j1 = j3 ;
      if (j1 > 0 && j3 > 0) j1 = Math.min(j1,j3) ;
      if (j1 > 0)
		{
         token = file.substring(j1) ;
			file = file.substring(0,j1) ;
		}
      else
			token = (st.hasMoreTokens()) ? st.nextToken() : " " ;

		// Check the palette specifier for this cel.  These begin with '*'
		// and can be followed immediately by the page declaration ':'.
		// Palette specifiers default to palette 0 when not specified.
		// Truecolor cels will ignore any palette specifier declaration.

		Integer pid = null ;
		Integer multipalette = null ;
		if (token.charAt(0) == '*')
		{
			i = token.indexOf(':') ;
			int j = token.indexOf(".") ;
         if (j > 0) i = j ;
			if (i < 0) i = token.length() ;
			value = token.substring(1,i) ;
			token = token.substring(i) ;
			if (value.length() > 0)
			{
				try { pid = new Integer(value) ; }
				catch (NumberFormatException e)
				{ showError("Non-numeric palette value " + value,value) ; }
			}

         // Watch for palette numbers preceeded by space.

         else
         {
				String pn = (st.hasMoreTokens()) ? st.nextToken() : " " ;
   			i = pn.indexOf(':') ;
   			j = pn.indexOf(".") ;
            if (j > 0) i = j ;
			   if (i < 0) i = pn.length() ;
			   token = pn.substring(i) ;
			   value = pn.substring(0,i) ;
				try { pid = new Integer(value) ; }
				catch (NumberFormatException e) { token = pn ; }
         }

         // Get the next token if required.

			if (token.length() == 0)
				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;

         // Check for the optional multipalette specifier.

         if (token.charAt(0) == '.')
         {
				i = token.indexOf(':') ;
				if (i < 0) i = token.length() ;
				value = token.substring(1,i) ;
				token = token.substring(i) ;
				if (value.length() > 0)
				{
					try { multipalette = new Integer(value) ; }
					catch (NumberFormatException e)
					{ showError("Non-numeric palette group value " + value,value) ; }
				}
				if (token.length() == 0)
					token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
         }
		}

		// Check the page declarations for this cel.  These are optional
		// and if the colon sentinal is not specified then the cel is
		// visible on all pages. If the colon sentinal is used, then
		// the cel is visible only on the pages specified, which may
		// be none.

		int page = 0 ;
      int leadingspace = 0 ;
		Vector pages = null ;
      String remainder = null ;
      Vector pageleadingspace = new Vector() ;
		if (token.charAt(0) == ':')
		{
			pages = new Vector() ;

			// Determine the leading spacing to the first page number.

         if (token.length() == 1)
         {
            remainder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
   			st = new StringTokenizer(remainder," \t") ;
            if (remainder.length() > 0)
   	         while (remainder.charAt(leadingspace) == ' '
                  || remainder.charAt(leadingspace) == '\t') leadingspace++ ;
         }

			// The first declaration may come immediately after the colon.
			// The page declaration may also be followed immediately by a
			// comment semicolon.

         int j = 0 ;
			if (token.length() > 1)
			{
				value = token.substring(1) ;
				try
				{
					j = value.indexOf(';') ;
					if (j > 0)
					{
						token = value.substring(j) ;
						value = value.substring(0,j) ;
					}
					if (j != 0)
					{
						page = Integer.parseInt(value) ;
					}
				}
				catch (NumberFormatException e)
				{ showError("Non-numeric page set value " + value,value) ; }

            // Watch for 0123456789 type page entries on older KiSS sets.
            // If we see these parse the individual digits as page numbers.

            if (value.length() > 1 && OptionsDialog.getMaxPageSet() <= 10)
            {
               for (int digit = 0 ; digit < value.length() ; digit++)
               {
   					try { page = Integer.parseInt(value.substring(digit,digit+1)) ; }
   					catch (NumberFormatException e) { break ; }
   					pages.addElement(new Integer(page)) ;
               }
            }
            else if (j != 0)
               pages.addElement(new Integer(page)) ;

            // Set this page leading space count and calculate the space
            // to the next page entry.

		      int size = pageleadingspace.size() ;
		      if (page >= size) pageleadingspace.setSize(page+1) ;
		   	pageleadingspace.setElementAt(new Integer(0),page) ;
            remainder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
   			st = new StringTokenizer(remainder," \t") ;
            if (remainder.length() > 0)
   	         while (remainder.charAt(leadingspace) == ' '
                  || remainder.charAt(leadingspace) == '\t') leadingspace++ ;
			}

			// Process all remaining page declarations.

         j = 0 ;
			if (token.length() == 0) token = " " ;
			if (token.charAt(0) != ';')
				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
			while (token.charAt(0) != ' ')
			{
         	value = token ;
				j = value.indexOf(';') ;
            if (j > 0)
				{
               token = value.substring(j) ;
					value = value.substring(0,j) ;
				}
				if (j != 0)
				{
					try { page = Integer.parseInt(value) ; }
					catch (NumberFormatException e) { break ; }
				}

            // Watch for 0123456789 type page entries on older KiSS sets.
            // If we see these parse the individual digits as page numbers.

            if (value.length() > 1 && (!ultrakiss || OptionsDialog.getCompatibilityMode() != null))
            {
               for (int digit = 0 ; digit < value.length() ; digit++)
               {
   					try { page = Integer.parseInt(value.substring(digit,digit+1)) ; }
   					catch (NumberFormatException e) { break ; }
   					pages.addElement(new Integer(page)) ;
               }
            }
            else if (j != 0)
               pages.addElement(new Integer(page)) ;

            // Retain the leading space count for this page entry.
            // These values are used to format the cel line when the
            // configuration is written.

				if (token.length() > 0 && token.charAt(0) == ';') break ;
		      int size = pageleadingspace.size() ;
		      if (page >= size) pageleadingspace.setSize(page+1) ;
		   	pageleadingspace.setElementAt(new Integer(leadingspace),page) ;

            // Compute the next page number lead spacing.

	         leadingspace = 0 ;
	         remainder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
				st = new StringTokenizer(remainder," \t") ;
	         if (remainder.length() > 0)
   	         while (remainder.charAt(leadingspace) == ' '
                  || remainder.charAt(leadingspace) == '\t') leadingspace++ ;
				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
			}
		}

		// Parse the comment text.  The token contain the next input token.
		//  [; [%t <transp>] [%g] [%u] [%c <color>] [%i] [%size[w,h]] [%offset[x,y]]
      //  [%attribute[a1,a2,...]] [[!group] [:<frame> ... ] ... ]]]
		// No spaces are allowed between the semicolon and the start of the
		// FKiSS extension commands.

		String comment = "" ;
		String basecomment = "" ;
      String attributes = null ;
		boolean ghost = false ;
      boolean visible = true ;
		int transparency = 255 ;
		int transparent = -1 ;
		int celwidth = 0 ;
		int celheight = 0 ;
		int celoffsetx = 0 ;
		int celoffsety = 0 ;
		Vector celgroups = new Vector() ;

		// Comments begin with a semicolon.  Retain the leading space count
      // for the comment entry.  This value is used to format the cel line
      // when the configuration is written.

      leadingspace = 0 ;
		if (token.charAt(0) == ';')
		{
			token = (token.substring(1)).trim() ;
         if (token.length() == 0)
         {
	         remainder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
				st = new StringTokenizer(remainder," \t") ;
	         if (remainder.length() > 0)
		         while (remainder.charAt(leadingspace) == ' ') leadingspace++ ;
				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
         }

         // Process tags in any sequence.
         
         while (token.length() > 0 && !" ".equals(token))
         {
            
   			// See if we have a transparency specification.  Syntax is %t

   			if (token.startsWith("%t"))
   			{
   				comment += token + " " ;
   				value = token.substring(2) ;
   				if (value.length() > 0)
   				{
   					i = value.indexOf(';') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { transparency = 255 - Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
   					{ showError("Non-numeric transparency value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have a ghost specification.  Syntax is %g

   			if (token.startsWith("%g"))
   			{
   				ghost = true ;
   				comment += token + " " ;
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have an unmap specification.  Syntax is %u

   			if (token.startsWith("%u"))
   			{
   				visible = false ;
   				comment += token + " " ;
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have a transparent color specification.  Syntax is %c
            // This is used to specify a transparent color for cels that do not
            // maintain transparency.

   			if (token.startsWith("%c"))
   			{
   				comment += token + " " ;
   				value = token.substring(2) ;
   				if (value.length() > 0)
   				{
   					i = value.indexOf(';') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { transparent = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
   					{ showError("Non-numeric transparent color value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have a cel size declaration.  Syntax is %size[w,h]
            // This is used to specify a cel size for cels that do not
            // maintain size information.

   			if (token.startsWith("%size["))
   			{
   				comment += token + " " ;
   				value = token.substring(6) ;
   				if (value.length() > 0)
   				{
   					String sizestring = value ;
   					i = value.indexOf(',') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { celwidth = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
   					{ showError("Non-numeric cel width value " + value,value) ; }
   					int j = sizestring.indexOf(']') ;
   					if (i > 0 && j > i+1) value = sizestring.substring(i+1,j) ;
   					try { celheight = Integer.parseInt(value) ; }
     					catch (NumberFormatException e)
   					{ showError("Non-numeric cel height value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have an offset specification.  Syntax is %offset[x,y]
            // This is used to specify a cel offset for cels that do not
            // maintain offset information.

   			if (token.startsWith("%offset["))
   			{
   				comment += token + " " ;
   				value = token.substring(8) ;
   				if (value.length() > 0)
   				{
   					String sizestring = value ;
   					i = value.indexOf(',') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { celoffsetx = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
   					{ showError("Non-numeric cel offset x value " + value,value) ; }
   					int j = sizestring.indexOf(']') ;
   					if (i > 0 && j > i+1) value = sizestring.substring(i+1,j) ;
   					try { celoffsety = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
     					{ showError("Non-numeric cel offset y value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have an alternate offset specification.  Syntax is %x
            // or %y. This is used to specify a specific offset for the cel.

   			if (token.startsWith("%x"))
   			{
               directkiss = true ;
   				comment += token + " " ;
   				value = token.substring(2) ;
   				if (value.length() > 0)
   				{
   					i = value.indexOf(';') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { celoffsetx = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
   					{ showError("Non-numeric cel offset x value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}
         
   			if (token.startsWith("%y"))
   			{
               directkiss = true ;
   				comment += token + " " ;
   				value = token.substring(2) ;
   				if (value.length() > 0)
   				{
   					i = value.indexOf(';') ;
   					if (i > 0) value = value.substring(0,i) ;
   					try { celoffsety = Integer.parseInt(value) ; }
   					catch (NumberFormatException e)
     					{ showError("Non-numeric cel offset y value " + value,value) ; }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have attribute specifications for a component.
            // Syntax is %attribute[a1,a2,...]
            // This is used to specify various display attributes for
            // Java component cels.

   			if (token.startsWith("%attributes["))
   			{
   				comment += token ;
   				value = token.substring(12) ;
   				if (value.length() > 1)
   				{
   					i = value.indexOf(']') ;
   					if (i > 0) 
                  {
                     attributes = value.substring(0,i) ;
                     comment += " " ;
                  }
                  else
                  {
         	         remainder = (st.hasMoreTokens()) ? st.nextToken("") : "" ;
      					i = remainder.lastIndexOf(']') ;
                     if (i < 0) i = remainder.length() ;
      					attributes = value + remainder.substring(0,i) ;
                     comment += remainder.substring(0,i) ;
                     comment += (i < remainder.length()) ? "] " : "" ;
                     remainder = (i < remainder.length()) ? remainder.substring(i+1) : "" ;
         				st = new StringTokenizer(remainder," \t") ;
                  }
   				}
   				token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
               continue ;
   			}

   			// See if we have a cel group specification.  Format is:
   			// [!<group> [:<frame> ... ] ... ]

   			while (token.startsWith("!"))
   			{
               if (fkisslevel < 4) fkisslevel = 4 ;
               remainder = (st.hasMoreTokens()) ? st.nextToken(";") : "" ;
               parseCelGroup(token + remainder,celgroups) ;
               comment += token + remainder ;
               comment = comment.trim() ;
               token = "" ;
            }
            
            // If we get here we had an unknown attribute. 
            
            break ;
         }

			// Pick up the remaining comment text if it exists.  Remove the
			// leading semicolon.
         
			String endcomment = "" ;
			try { endcomment = st.nextToken("\n") ; }
			catch (NoSuchElementException e) { endcomment = "" ; }
			comment = trim(comment + token + endcomment) ;
         basecomment = trim(token + endcomment) ;
			if (comment.length() > 0 && comment.charAt(0) == ';')
				comment = comment.substring(1).trim() ;
			if (comment.length() == 0) comment = null ;
			if (basecomment.length() > 0 && basecomment.charAt(0) == ';')
				basecomment = basecomment.substring(1).trim() ;
			if (basecomment.length() == 0) basecomment = null ;
		}

		// Determine the full name for the new cel entity.

		Cel c = null ;
		n = file.lastIndexOf('.') ;
		String ext = (n < 0) ? "" : file.substring(n).toLowerCase() ;
		File f = new File(this.getDirectory(),file) ;
      String newname = f.getPath() ;

		// If we are reloading a configuration and we had imported components 
      // which were converted to cels, then we need to reconstruct the component.

      if (ref != null) 
      {
         String name = f.getName() ;
         name = (name != null) ? name.toUpperCase() : "" ;
         Cel c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),"Import "+name) ;
         if (c1 instanceof JavaCel) 
         {
            String type = ((JavaCel) c1).getType() ;
            c = new JavaCel(type,newname,this) ;
         }
      }
      
		// Create a new cel entity. 

      if (c == null)
      {
         if (ArchiveFile.isComponent(newname))
            c = Cel.createCel(zip,newname,this) ;
         else
            c = Cel.createCel(zip,newname,ref) ;
      }
		if (c == null) 
         showError("Cel " + file + " has an unknown file extension.",ext) ;
      else
         if (!ArchiveFile.isCel(newname)) fkisslevel = 5 ;

      // Retain movie cels in the movies list and set the movie cel size.

      if (Kisekae.isMediaInstalled())
      {
         if (c instanceof Video)
         {
   	     	if (!movies.contains(c)) movies.addElement(c) ;
            c.setSize(new Dimension(celwidth,celheight)) ;
         }
      }

      // Set the size for Java components.

      if (c instanceof JavaCel)
         c.setSize(new Dimension(celwidth,celheight)) ;

      // Set the cel attributes.

		if (c != null)
		{
			c.setID(cid) ;
			c.setGroup(group) ;
			c.setCelGroups(celgroups) ;
			c.setPages(pages) ;
    		c.setPaletteID(pid) ;
			c.setInitPaletteID(pid) ;
         c.setPaletteGroupID(multipalette) ;
         c.setInitPaletteGroupID(multipalette) ;
			c.setFlex(flex) ;
			c.setInitFlex(flex) ;
			c.setTransparency(transparency) ;
			c.setInitTransparency(transparency) ;
         c.setTransparentIndex(transparent) ;
         c.setOffset(celoffsetx,celoffsety) ;
         c.setInitialOffset(celoffsetx,celoffsety) ;
			c.setGhost(ghost) ;
         c.setVisible(visible) ;
         c.setInitVisible(visible) ;
         c.setInitGhost(ghost) ;
			c.setComment(comment) ;
         c.setBaseComment(basecomment) ;
         c.setRelativeName(file) ;
         c.setPageLeadingSpace(pageleadingspace) ;
         c.setCommentLeadingSpace(leadingspace) ;
         c.setInitAttributes(attributes) ;
		}
      
      // We have this nasty problem, in that if a cel was imported and we 
      // reload the configuration before it is saved, and we are not caching 
      // images, then we can drop the image from cache on the configuration 
      // reload.  We need to retain the original archive entry as an imported 
      // image.
      
      if (ref == null) return c ;
      String name = (c != null) ? c.getName() : null ;
      name = (name != null) ? name.toUpperCase() : "" ;
      Cel c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),"Import "+name) ;
      if (c1 == null) return c ;
      if (c == null) return c ;
      c.setZipEntry(c1.getZipEntry()) ;
      c.setImportedAsCel(c1.isImportedAsCel()) ;
      c.setImported(c1.isImported()) ;
      c.setUpdated(c1.isUpdated()) ;
		return (c) ;
	}


   // Method to parse a cel frame group declaration.
	// Format is: [!<group> [:<frame> ... ] ... ]

   private boolean parseCelGroup(String s, Vector celgroups)
   {
      if (s == null) return false ;
      if (celgroups == null) return false ;
      StringTokenizer st = new StringTokenizer(s," \t") ;
      String token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
      boolean havecelgroup = false ;

		// See if we have a cel group specification.  Format is:
		// [!<group> [:<frame> ... ] ... ]

		while (token.startsWith("!"))
		{
         havecelgroup = true ;
         String framelist = "" ;
   		int celgroupframe = 0 ;
         int k = token.indexOf(':') ;
         if (k > 0)
         {
            framelist = token.substring(k) ;
            token = token.substring(0,k) ;
         }

         // Create a new cel group.

			CelGroup celgroup = new CelGroup(token) ;
			celgroups.add(celgroup) ;
         if (framelist.length() == 0)
			   token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
         else
            token = framelist ;

			// Parse the cel group frame list.  The first declaration may
			// come immediately after the colon.

			if (token.length() > 0 && token.charAt(0) == ':')
			{
				if (token.length() > 1)
				{
					String value = token.substring(1) ;
					try
					{
						int j = value.indexOf(';') ;
						if (j > 0)
						{
							token = value.substring(j) ;
							value = value.substring(0,j) ;
						}
						if (j != 0)
						{
							celgroupframe = Integer.parseInt(value) ;
							celgroup.addFrame(new Integer(celgroupframe)) ;
						}
					}
					catch (NumberFormatException e)
					{ showError("Non-numeric cel group frame value " + value,value) ; }
				}

				// Process all remaining frame declarations.

				if (token.length() == 0) token = " " ;
				if (token.charAt(0) != '!')
					token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
				while (token.charAt(0) != ' ')
				{
					String value = token ;
					try { celgroupframe = Integer.parseInt(value) ; }
					catch (NumberFormatException e) { break ; }
					celgroup.addFrame(new Integer(celgroupframe)) ;
					if (token.length() > 0 && token.charAt(0) == '!') break ;
					token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
				}
			}

         // No colon specifier means the cel is on all frames.

         else celgroup.setAllFrames(true) ;
		}

      // Return whether we found a cel group or not.
      
      return havecelgroup ;
   }


	// Method to parse a palette file declaration.

	private Palette parsePalette(String s)
	{
		StringTokenizer st = new StringTokenizer(s.substring(1)," \t") ;

		// First token is the palette file name.  Format: %file
      // Watch for file names delimited by quotes.

		String file = (st.hasMoreTokens()) ? st.nextToken() : " " ;
		while (!(file.equals(" ")) && file.length() > 1 && file.charAt(0) == '\"')
      {
			if (file.charAt(file.length()-1) == '\"')
         {
				file = file.substring(1,file.length()-1) ;
            break ;
         }
         else
            file += (st.hasMoreTokens()) ? (" " + st.nextToken()) : "\"" ;
      }

      // Remaining tokens are the comment.

		String comment = (st.hasMoreTokens()) ? st.nextToken("\n") : null ;
      File f = new File(this.getDirectory(),file) ;
		Palette palette = new Palette(zip,f.getPath(),ref) ;
		palette.setID(cid) ;
      palette.setComment(comment) ;
		palette.setRelativeName(file) ;
		return (palette) ;
	}


	// Method to parse expansion set INCLUDE files.  Each token is an INCLUDE
   // file name.  These are path specifiers to a compressed archive file or
   // a configuration file.  File names must be delimited with quotes if they
   // contain spaces.

	private void parseIncludeFiles(String s)
	{
      if (s == null) return ;
      if (zip == null) return ;
      s = trim(s) ;
		StringTokenizer st = new StringTokenizer(s," \t,") ;
      if (includefiles == null) includefiles = new Vector() ;

      // Extract file names.

      while (st.hasMoreTokens())
      {
         String file = "" ;
         if (s.length() > 0 && s.charAt(0) == '"')
         {
            int i = s.indexOf('"',1) ;
            if (i > 0)
            {
               file = s.substring(1,i) ;
               s = trim(s.substring(i+1)) ;
            }
            else
            {
               showError("INCLUDE file name missing terminating quote " + s) ;
               break ;
            }
            st = new StringTokenizer(s,",") ;
            if (st.hasMoreTokens())
            {
              s = st.nextToken() ;
              s = trim(s) ;
            }
            st = new StringTokenizer(s," \t,") ;
         }
         else
   		   file = st.nextToken() ;
         File f = new File(file) ;
         if (!f.isAbsolute()) f = new File(zip.getDirectoryName(),file) ;
         includefiles.add(f) ;
      }
	}


	// Method to parse the HINTS.  Each line specifies a new
   // option value.

	private void parseHint(String s)
	{
      StringTokenizer st = new StringTokenizer(s," ") ;
      try
      {
         String option = "" ;
         String hint = st.nextToken() ;
         String value = st.nextToken() ;
         if (hint != null) hint = hint.toLowerCase() ;
         
         if ("stack".equals(hint)) 
            OptionsDialog.setOption("",value) ;
         else if ("maxfix".equals(hint)) 
            OptionsDialog.setOption("maxflex",value) ;
         else if ("bounds".equals(hint))
         {
            String b1 = "", b2 = "", b3 = "" ;
            if ("all".equalsIgnoreCase(value)) { b1= "true" ; b2 = "true" ; b3 = "false" ; }
            if ("fkiss".equalsIgnoreCase(value)) { b1= "false" ; b2 = "true" ; b3 = "false" ; }
            if ("visible".equalsIgnoreCase(value)) { b1= "true" ; b2 = "true" ; b3 = "true" ; }
            if ("limited".equalsIgnoreCase(value)) { b1= "true" ; b2 = "false" ; b3 = "false" ; }
            if ("none".equalsIgnoreCase(value)) { b1= "false" ; b2 = "false" ; b3 = "false" ; }
            if (b1.length() > 0) OptionsDialog.setOption("constrainmoves",b1) ;
            if (b2.length() > 0) OptionsDialog.setOption("constrainfkiss",b2) ;
            if (b3.length() > 0) OptionsDialog.setOption("constrainvisible",b3) ;
         }
      }
      catch (NoSuchElementException e)
      {
         showError("Invalid HINT: " + s) ;
      }
   }


	// Method to parse the [Option Section].  Each line specifies a new
   // option value.

	private void parseOptions(String s, BufferedReader f)
		throws IOException
	{
      if (!";[Option Section]".equals(s)) return ;

      // Remove option section heading statements.

      Object last = (trailingtext != null) ? trailingtext.lastElement() : null ;
      if (last != null && s.equals(last))
      {
         trailingtext.removeElement(last) ;
         last = trailingtext.lastElement() ;
         if (" ".equals(last)) trailingtext.removeElement(last) ;
      }

      // Read and parse options.  We terminate the option parse on a blank
      // line or a non-comment line.

      while (true)
      {
         s = f.readLine() ;
         if (s == null) break ;
         lastline = s ;
         line = line + 1 ;
         if (s.length() == 0) break ;
         if (s.charAt(0) != ';') break ;
         s = s.substring(1) ;
         s = trim(s) ;

         // We have a comment line that should be an option specifier.

         StringTokenizer st = new StringTokenizer(s," ") ;
         try
         {
            String option = st.nextToken() ;
            String equal = st.nextToken() ;
            String value = st.nextToken() ;
            if (!"=".equals(equal)) continue ;
            OptionsDialog.setOption(option,value) ;
         }
         catch (NoSuchElementException e)
         {
            showError("Invalid UltraKiss option line: " + s) ;
            continue ;
         }
      }
   }


	// Method to parse the page positions for the cel groups.  The
	// format is:  $n x,y x,y ... x,y   where 'n' represents a
	// multi-palette number from 0 to 9, and x,y represents the
	// location for each group object.  Note that the pair x,y may
	// be specified as '*', which indicates that the group is to be
	// placed at the origin.

	private PageSet parsePage(String s, BufferedReader f)
		throws IOException
	{
		String token = null ;
      trailtoken = new Vector() ;
      if (s == null) return null ;

		// Create a new page set and store the multipalette number that
		// must be used for displaying this page set.  If none is specified
      // the multipalette defaults to 0.

		PageSet p = new PageSet() ;
		p.setID(cid) ;
      int i = s.indexOf(' ') ;
      String value = (i >= 1) ? s.substring(1,i) : "0" ;
		try
		{
			Integer multipalette = new Integer(value) ;
			p.setMultiPalette(multipalette) ;
		}
		catch (NumberFormatException e)
		{ showError("Non-numeric page set multipalette value, " + value,value) ; }

		// Parse all group object positions on this page.  Position pairs
		// can span multiple lines, so we use a read-ahead routine to
		// access the next token.  If we see a syntax error we ignore
		// the remainder of the line and continue processing on the next
		// line.

		int groupnumber = 0 ;
      if (i < 0) i = s.length() ;
		String pagelist = s.substring(i) ;
		while ((s = nextToken(pagelist,f)) != null)
		{
			StringTokenizer st = new StringTokenizer(s," ,") ;
			value = (st.hasMoreTokens()) ? st.nextToken() : " " ;

			// Look for comments at the end of the line.  Comments at
			// the beginning of the line can be FKiSS commands.  We will
			// catch FKiSS comments as recoverable syntax errors when
			// we parse the first position pair integer value.

			if (value.charAt(0) == ';')
			{
				if (!(newline && value.startsWith(";@")))
				{
					pagelist = " " ;
					continue ;
				}
			}

			// Positions '*' indicates that the group does not exist
			// on this page.

			if (value.charAt(0) == '*')
			{
            trailtoken = new Vector() ;
				p.addPosition(null,groupnumber++) ;
				pagelist = (st.hasMoreTokens()) ? st.nextToken("\n") : " " ;
				continue ;
			}

			// A coordinate pair must be specified on one line.  If we
			// cannot decode a number and we are looking at the first
			// token on the line then this is a recoverable syntax error.
			// We assume we have a new command line.

			int x, y ;
			try { x = Integer.parseInt(value) ; }
			catch (NumberFormatException e)
			{
				if (newline)
				{
					reread = true ;
					line = line - 1 ;
					break ;
				}
				showError("Non-numeric page set coordinate, " + value,value) ;
				pagelist = " " ;
				continue ;
			}

			// Parse the second number of the pair.  We may have a
			// comment immediately following the digit.

			value = (st.hasMoreTokens()) ? st.nextToken() : " " ;
			if (value.indexOf(';') >= 0)
			{
				value = value.substring(0,value.indexOf(';')) ;
				while (st.hasMoreTokens()) st.nextToken("\n") ;
			}
			try { y = Integer.parseInt(value) ; }
			catch (NumberFormatException e)
			{
				showError("Non-numeric page set coordinate, " + value,value) ;
				pagelist = " " ;
				continue ;
			}

			// Position to process the rest of the line.

         trailtoken = new Vector() ;
			p.addPosition(new Point(x,y),groupnumber++) ;
			pagelist = (st.hasMoreTokens()) ? st.nextToken("\n") : " " ;
		}

      // Done.  Clear any possible leading lines constructed through
      // the nextToken call.

      leadaction = new Vector() ;
		return p ;
	}


	// Method to parse FKiSS event commands.  This module is designed
	// to construct an FKiSS event object.  The event object contains a
	// list of event actions that must be performed when the event occurs.

	private FKissEvent parseFKiss(String s, BufferedReader f, KissObject lastparsed)
		throws IOException
	{
      String input = s ;
		if (s.startsWith(";@") && s.length() > 2)
			s = trim(s.substring(2)) ;
      if (s.length() == 0) return null ;
		if (s.charAt(0) == ';') return null ;
		StringTokenizer st = new StringTokenizer(s," \t(;") ;
		String token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
		if (token.charAt(0) == ' ') return null ;

      // Check for a recognized event name.  If we don't have one this is a
      // syntax error which terminates the event parse.  If we have an action 
      // name and a previous event was being parsed then this action applies 
      // to the previous event.

		int eventcode = EventHandler.getEventNameKey(token.toLowerCase()) ;
		if (eventcode < 0 && eventsection)
		{
			int actioncode = EventHandler.getActionNameKey(token) ;
			if (actioncode < 0 || !(lastparsed instanceof FKissEvent)) 
         {
            showError("FKiSS unknown event: " + token,token) ;
            leadaction.addAll(leadingtext) ;
            leadaction.addElement(input) ;
            return null ;
         }
         showError("FKiSS action " + token + " attached to previous event " + lastparsed,token) ;
         FKissEvent event = (FKissEvent) lastparsed ;
         Vector actions = event.getActionList() ;
         s = parseEventActions(event,actions,s,f,lastparsed) ;
			return event ;
		}

		// Determine if we are in the event handler section.

		if ("EventHandler".equals(token)) eventsection = true ;
		if (!eventsection) return null ;
      if (fkisslevel < EventHandler.getEventFKissLevel(eventcode))
         fkisslevel = EventHandler.getEventFKissLevel(eventcode) ;

		// If we are in the event handler section and performing syntax
      // checks, identify the compatibility mode.  Do not assume to default 
      // to PlayFKiss if we are restarting as we may have turned off the
      // initial default mode.

      MainFrame mf = Kisekae.getMainFrame() ;
		if ("EventHandler".equals(token))
      {
         String compatibility = OptionsDialog.getCompatibilityMode() ;
         if (OptionsDialog.getStrictSyntax() && compatibility != null)
         {
           	loader.showText(" ") ;
           	loader.showText(compatibility + " compatibility on.") ;
           	System.out.println(compatibility + " compatibility on.") ;
         }
         else if (directkiss && compatibility == null)
         {
           	loader.showText(" ") ;
           	loader.showText("DirectKiss compatibility set.") ;
           	System.out.println("DirectKiss compatibility set.") ;
            OptionsDialog.setDirectKissCompatibility("true") ;
         }
         else if (playfkiss && compatibility == null)
         {
           	loader.showText(" ") ;
           	loader.showText("PlayFKiss compatibility set.") ;
           	System.out.println("PlayFKiss compatibility set.") ;
            OptionsDialog.setPlayFKissCompatibility("true") ;
         }
         else if (!ultrakiss && compatibility == null && 
            !mf.isRestart() && OptionsDialog.getDefaultPlayFKiss())
         {
            loader.showText(" ") ;
            loader.showText("PlayFKiss compatibility assumed.") ;
            System.out.println("PlayFKiss compatibility assumed.") ;
            OptionsDialog.setPlayFKissCompatibility("true") ;
         }
      }
      

		// Decode the event definition.  Retain the event parameters
		// and attach the action objects to the event.  We also construct
      // the loop iteration entries.  These are records to action
      // commands that loop.

		FKissEvent event = new FKissEvent(token,this) ;
		event.setLine(line) ;
		Vector parameters = new Vector() ;
		Vector actions = new Vector() ;
      Vector iterations = new Vector() ;
		s = s.substring(s.indexOf(token) + token.length()) ;
		s = parseParameters(parameters,s,f) ;

      // Check for a parameter parse error.

      if (s == null)
      {
         leadaction.addAll(leadingtext) ;
         leadaction.addElement(input) ;
         return null ;
      }
         
      // On a valid parameter parse, check for variable declarations.
      // We retain the names of variables and their last declaration
      // line number for later syntax checking when the configuration 
      // is initialized.
         
      for (int n = 0 ; n < parameters.size() ; n++)
      {
         String param = (String) parameters.elementAt(n) ;
         int type = EventHandler.getEventParamType(eventcode,n) ;
         if ((type & 128) == 0) continue ;
         type = getParameterType(param) ;

         // Declarations for local parameters passed on an event call.
         // Local variables are prefixed with the event name to uniquely 
         // identify them. Return values do not have a prefix.
            
         if ((type & 4) != 0) 
         {
            if (param.charAt(0) != '@') continue ;
            if (param.equals(parameters.elementAt(0))) continue ;
            if (!OptionsDialog.getVariableCase()) param = param.toUpperCase() ;
            Object [] keyvalue = new Object [2] ;
            keyvalue[0] = new Integer(line) ;
            keyvalue[1] = event ;
            String prefix = (String) parameters.elementAt(0) ;
            if (!OptionsDialog.getVariableCase()) prefix = prefix.toUpperCase() ;
            if (!param.equals("@"+prefix)) param = prefix + "-" + param ;
            variables.put(param,keyvalue) ;
         }
      }

		// Check for event comments.

		s = trim(s) ;
		if (s.length() > 0 && s.charAt(0) == ';')
		{
			event.setComment(trim(s.substring(1))) ;
			s = "" ;
		}

		// Parse all event actions.

		event.addParameters(parameters) ;
		s = parseEventActions(event,actions,s,f,lastparsed) ;
		event.addActions(actions) ;

		// Create the alarm objects for reference by timer actions.
      // Alarms must be unique by name.

		if ("alarm".equals(event.getIdentifier()))
		{
         String id = event.getFirstParameter() ;
         if (id != null)
         {
            id = id.toUpperCase() ;
            Alarm a = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,id) ;
            if (a == null)
            {
   			   a = new Alarm(cid,event.getFirstParameter()) ;
               a.setLine(event.getLine());
   			   alarms.addElement(a) ;
            }
            event.setParent(a) ;
         }
		}

		// Create the label objects for reference by goto or gosub actions.
      // Labels must be unique by name. We also create a local variable
      // declaration for the return value variable.

		if ("label".equals(event.getIdentifier()))
		{
         String id = event.getFirstParameter() ;
         if (id != null)
         {
            id = id.toUpperCase() ;
            variables.put("@"+id,event) ;
            Module m = (Module) Module.getByKey(Module.getKeyTable(),cid,id) ;
            if (m == null)
            {
			      m = new Module(cid,event.getFirstParameter(),event.getParameters()) ;
			      labels.addElement(m) ;
            }
         }
		}

		return event ;
	}


	// This is the external entry to parse FKiSS event commands.  This
   // method is used to prepare and parse individual FKiSS event text
   // modified through the FKiSS Editor.  We return a list of events
   // as the input text may contain more than one event declaration.

	Vector parseFKiss(KissFrame parent, byte [] b)
		throws IOException
	{
      if (b == null) return null ;
      FKissEvent event = null ;
      loader = parent ;
      eventsection = true ;
      Vector events = new Vector() ;
      leadingtext = new Vector() ;
      trailingtext = new Vector() ;
      leadaction = new Vector() ;
      trailtoken = new Vector() ;
		reread = false ;
		newline = false ;
		line = 0 ;

      ByteArrayInputStream in = new ByteArrayInputStream(b) ;
      BufferedReader f = new BufferedReader(new InputStreamReader(in)) ;

		// Read the event file.  We read one line at a time and
		// decode it.  There are times when the parse will fail and we
		// must re-read the last line.

		try
		{
			while (true)
			{
				newline = true ;
				String s = (reread) ? lastline : f.readLine() ;
				if (s == null) break ;

            // Establish our line.

				reread = false ;
				lastline = s ;
				line = line + 1 ;
				s = trim(s) ;
				if (s.length() == 0)
            {
            	leadingtext.addElement(new String(" ")) ;
               continue ;
            }

				// Parse the configuration statements.

				switch (s.charAt(0))
				{
					// Comment or FKiSS specifications.

				case ';':
					if (s.length() <= 1 || s.charAt(1) != '@')
               {
               	leadingtext.addElement(s) ;
                  continue ;
               }

               // Try and parse an FKiSS event.  The parse terminates
               // on the first error.  We terminate the syntax scan, too.

					event = parseFKiss(s,f,null) ;
					if (event == null) return null ;
               event.addLeadComment(leadingtext) ;
               event.addTrailComment(leadaction) ;
               events.addElement(event) ;
               leadaction.removeAllElements() ;
               leadingtext.removeAllElements() ;
					break ;

					// Unknown command.  Ignore if batch processing.

				default:
               if (!Kisekae.isBatch())
               {
   					showError("Unknown configuration command " + s) ;
   					return null ;
               }
				}
			}
		}
		catch (Exception e)
		{
         showError("FKiSS parse " + e.getMessage()) ;
         return null ;
		}

		// Close the file on termination and clean up our memory allocation.

		finally
		{
			try { if (f != null) f.close() ; }
			catch (IOException e)
			{
				showFile("FKiSS parse " + e.getMessage()) ;
				throw e ;
			}
		}

      return events ;
	}


	// Function to parse the event actions.  We consume tokens from
	// the input stream and parse all known action commands.  This
	// function constructs a vector of event actions and returns when
	// an unknown action command is read.  We also match interation
   // commands while/endwhile and for/next pairs within the action list.

	private String parseEventActions(FKissEvent parent, Vector v, String s, BufferedReader f, KissObject lastparsed)
		throws IOException
	{
		FKissAction a = null ;                 // Action object
      String name = null ;                   // Action identifier
      int ifcount = 0 ;                      // Count of if statements seen
      int endifcount = 0 ;                   // Count of endif statements seen
      int endifgenerated = 0 ;               // Count of total endif generated
      int ifnestlevel = 0 ;                  // Nested if level (FK5 if nesting)
      int indentlevel = 0 ;                  // Indentation level for output
      int lastparsedline = 0 ;               // Last parsed action line number
      boolean ifsequence = false ;           // True if processing an if statement
      boolean inifsequence = false ;         // True if within an if statement
      boolean wrapper = false ;              // True if event is never() wrapper
      
      String compatibility = OptionsDialog.getCompatibilityMode() ;
      boolean strictsyntax = OptionsDialog.getStrictSyntax() ;
      boolean autoendif = OptionsDialog.getAutoEndif() ;
      
      // Determine if our parent event is a wrapper to hide syntax errors for
      // unknown events.
      
      if (parent != null)
      {
         if ("never".equals(parent.getID())) wrapper = true ;
         if ("nothing".equals(parent.getID())) wrapper = true ;
         if ("unknown".equals(parent.getID())) wrapper = true ;
      }
      
      // Loop over all actions until we fail to find a token.
      
		while (true)
		{
			// Read the next event action command.

			int lastline = line ;
         String input = s ;
			if ((s = nextToken(s,f)) == null) break ;
			if (s.startsWith(";@") && s.length() > 2)
				s = trim(s.substring(2)) ;
			if (s.length() > 0 && s.charAt(0) == ';')
			{
				if (a != null && lastline == line)
            	a.setComment(trim(s.substring(1))) ;
            else
               trailcomment.addElement(s) ;
				s = "" ;
			}
			if (s.length() == 0) continue ;
         trailcomment.removeAllElements() ;

			// Parse the action command.  See if it is a valid action.

			StringTokenizer st = new StringTokenizer(s," \t(") ;
			String token = (st.hasMoreTokens()) ? st.nextToken() : " " ;
         name = token ;
			int actioncode = EventHandler.getActionNameKey(token) ;
         if (actioncode < 0 && (!strictsyntax || compatibility != null))
         {
            String s1 = EventHandler.findPartialActionName(token) ;
            actioncode = EventHandler.getActionNameKey(s1) ;
            if (EventHandler.getEventNameKey(token) >= 0) actioncode = -1 ;
            if (actioncode >= 0)
            {
               if (!wrapper)
               {
         			showWarning("Line [" + line + "] Invalid action name "
                     + token + " corrected to " + s1) ;
               }
               name = s1 ;
            }
         }

   		// Did we find an action? If not, re-read the line if this was the
         // first token as it could be an event name.

			if (actioncode < 0)
			{
				if (line != lastline)
				{
               if (leadaction.size() > 0)
               	leadaction.remove(leadaction.lastElement()) ;
					reread = true ;
					line = line - 1 ;
					break ;
				}

				// If this is not an event name, skip it, along with
            // any parameters it might have.

				if (EventHandler.getEventNameKey(token) < 0)
				{
               String badaction = token ;
   				if (line == lastline && a != null) a.addTrailComment(s) ;
      			String s1 = s.substring(token.length()) ;
               if (s1.startsWith("("))
               {
                  int n = s1.indexOf(')') ;
                  if (n > 0) 
                  {
                     n += token.length() + 1 ;
                     badaction = s.substring(0,n) ;
                     s = s.substring(n) ;
                  }
               }
               else s = s1 ;
               if (!wrapper)
                  showError("FKiSS unknown action: " + badaction,badaction) ;
					continue ;
				}

				// Otherwise, process the event.  This is a recursive call
				// and the recursion will terminate when we find an invalid
				// action or an FKiSS line that starts with an event name.

				FKissEvent event = parseFKiss(s,f,lastparsed) ;
				if (event != null)
            {
            	event.addLeadComment(leadaction) ;
               leadaction = new Vector() ;
            	if (handler == null) handler = new EventHandler() ;
               handler.addEvent(event) ;
            }
				break ;
			}

         // Now, for our new action, create a new event action object.
         // Add the action object to our resulting action vector.

         String unparsed = s ;
         FKissAction prioraction = a ;
			a = new FKissAction(parent,name,this) ;
			Vector parameters = new Vector() ;
			s = s.substring(token.length()) ;
			s = parseParameters(parameters,s,f) ;
         if (leadaction.size() > 0)
            leadaction.remove(leadaction.lastElement()) ;
         if (fkisslevel < EventHandler.getActionFKissLevel(actioncode))
            fkisslevel = EventHandler.getActionFKissLevel(actioncode) ;

         // On an invalid parameter parse, reject the action.
			// Re-read the line if this was the first token.

			if (s == null)
			{
            s = unparsed ;
				if (line != lastline)
				{
					reread = true ;
					line = line - 1 ;
					break ;
				}
  				if (line == lastline && prioraction != null)
               prioraction.addTrailComment(s) ;
				break ;
			}
         
         // On a valid parameter parse, check for variable declarations.
         // We retain the names of variables and their last declaration
         // line number for later syntax checking when the configuration 
         // is initialized.
         
         for (int n = 0 ; n < parameters.size() ; n++)
         {
            String param = (String) parameters.elementAt(n) ;
            int type = EventHandler.getActionParamType(actioncode,n) ;
            if ((type & 128) == 0) continue ;
            type = getParameterType(param) ;

            // Variable declarations.  let(N,1) and so on.  Local variables
            // are prefixed with the event first parameter, which should be
            // the label name, to uniquely identify them.
            
            if ((type & 4) != 0) 
            {
               if (!OptionsDialog.getVariableCase()) param = param.toUpperCase() ;
               Object [] keyvalue = new Object [2] ;
               keyvalue[0] = new Integer(line) ;
               keyvalue[1] = a ;
               if (param.charAt(0) == '@' && parent != null)
               {
                  String prefix = parent.getFirstParameter() ;
                  if (prefix == null) prefix = parent.getName() ;
                  if (!OptionsDialog.getVariableCase()) prefix = prefix.toUpperCase() ;
                  if (!param.equals("@"+prefix)) param = prefix + "-" + param ;
               }
               variables.put(param,keyvalue) ;
            }
         }

         // Check for valid else actions.

         if (strictsyntax)
         {
            if (EventHandler.isElseEndAction(a) && !inifsequence)
            {
               String s1 = a.getName() ;
               showError(s1 + " does not have a matching if statement,",s1) ;
            }
         }

         // Establish our processing state for auto endif() generation.

         ifsequence = EventHandler.isIfAction(a) ;
         if (EventHandler.isEndIfAction(a))
         {
            if (ifnestlevel > 0) ifnestlevel-- ;
            if (ifnestlevel == 0) inifsequence = false ;
         }

         // Auto endif() generation assumes that each preceeding if() is
         // terminated.  If the immediate preceeding if() was not terminated
         // we generate an endif() statement in the middle of the line.

         if (ifsequence && inifsequence && autoendif)
         {
            FKissAction a1 = new FKissAction(parent,"endif",this) ;
            a1.setLine(lastline) ;
            a1.setInternal(true) ;
            a1.setSequence(v.size()) ;
            a1.addParameters(new Vector()) ;
            v.addElement(a1) ;
            endifgenerated++ ;
            if (ifnestlevel > 0) ifnestlevel-- ;
         }

         // Retain our current if statement process counts.

         if (EventHandler.isEndIfAction(a)) endifcount++ ;
         if (ifsequence)
         {
            ifcount++ ;
            ifnestlevel++ ;
            inifsequence = true ;
            if (ifnestlevel > 1) fkisslevel = 5 ;
         }       

         // The action is valid.

         String s1 = (String) a.getIdentifier() ;
         if ("else".equals(s1) || "endif".equals(s1) ||
             "elseifequal".equals(s1) || "elseifnotequal".equals(s1) ||
             "elseifgreaterthan".equals(s1) || "elseiflessthan".equals(s1) ||
             "next".equals(s1) || "endwhile".equals(s1)) indentlevel-- ;
         if (indentlevel < 0) indentlevel = 0 ;

         lastparsedline = line ;
        	a.addLeadComment(leadaction) ;
         leadaction = new Vector() ;
			a.setLine(line) ;
         a.setIndentLevel(indentlevel);
         a.setSequence(v.size()) ;
			a.addParameters(parameters) ;
			v.addElement(a) ;
         
         if ("ifequal".equals(s1) || "ifnotequal".equals(s1) ||
             "ifgreaterthan".equals(s1) || "iflessthan".equals(s1) ||
             "else".equals(s1) ||
             "elseifequal".equals(s1) || "elseifnotequal".equals(s1) ||
             "elseifgreaterthan".equals(s1) || "elseiflessthan".equals(s1) ||
             "for".equals(s1) || "while".equals(s1)) indentlevel++ ;

			// Create the audio objects.  These objects must be unique by name.

			if ("sound".equals(name) || "music".equals(name) || "mediaplayer".equals(name))
			{
				String file = a.getFirstParameter() ;
				if (file != null && file.charAt(0) == '\"')
				{
					int i = file.lastIndexOf('\"') ;
					if (i > 0)
					{
						file = file.substring(1,i) ;
						createAudio(zip,file) ;
					}
				}
			}
		}

      // On exit we may be missing a final endif().  If the immediate
      // preceeding if() was not terminated we generate an endif() statement
      // as the last line.

      if (inifsequence && autoendif)
      {
  			FKissAction a1 = new FKissAction(parent,"endif",this) ;
         a1.setLine(lastparsedline) ;
         a1.setInternal(true) ;
         a1.setSequence(v.size()) ;
         a1.addParameters(new Vector()) ;
         v.addElement(a1) ;
         endifgenerated++ ;
      }

      // Do a sanity check on if-endif matches.  These must agree.

      if (ifcount == endifcount)
      {
         if (endifgenerated > 0)
         {
            removeGeneratedStatements(v) ;
            endifgenerated = 0 ;
         }
      }

      // If we have a mismatch this may have been auto corrected.

      else if (ifcount == (endifcount + endifgenerated) && autoendif)
      {
         endifcount += endifgenerated ;
         for (int i = 0 ; i < v.size() ; i++)
         {
            String actionname = null ;
            FKissAction act = (FKissAction) v.elementAt(i) ;
            if (act.isInternal())
            {
               if (i > 0) actionname = ((FKissAction) v.elementAt(i-1)).getName() ;
               showWarning("[Line " + act.getLine() + "] " + act.getName()
                  + " statement generated" + ((actionname != null)
                  ? " after " + actionname : "")) ;
            }
         }
      }

      // Unable to recover.

      if (ifcount != endifcount)
      {
        int eventline = 0 ;
         String eventname = null ;
         if (parent != null) eventname = parent.getName() ;
         if (parent != null) eventline = parent.getLine() ;
         showWarning("[Line " + eventline + "] FKiSS if-endif mismatch"
            + ((eventname != null) ? " in " + eventname : "")) ;
         if (endifgenerated > 0)
         {
            removeGeneratedStatements(v) ;
            endifgenerated = 0 ;
         }
      }

      // Construct the loop statement return points.

      buildLoopReturn(v) ;
      return s ;
	}


	// Function to parse a parameter list.  This function adds string
	// elements to a parameter vector.  It returns the string with the
   // parameter list removed.

	private String parseParameters(Vector v, String s, BufferedReader f)
		throws IOException
	{
		int i = 0 ;
		String token ;

		// Parameter list is optional, but if it exists it must be all
		// on one line.  Leading spaces are removed.

		if (s != null) s = trim(s) ;
		if (s == null || s.length() == 0) return s ;
		if ((s = nextToken(s,f)) == null) return s ;
		if (s.length() == 0 || s.charAt(0) != '(') return s ;

		// Determine the end of the parameter list.

		i = findTerminator(s,')') ;
		if (i < 0)
		{
  			showError("FKiSS invalid parameter list, " + s,s) ;
			return null ;
		}

		// Extract parameters from the list.  List items are delimited by
      // commas.  Watch for commas in string literals. Note that string
      // parameters can be delimited by pairs of single or double quotes.

		String value = s.substring(1,i) ;
		value = trim(value).trim() ;
		s = s.substring(i+1) ;
		while (value.length() > 0)
		{
      	int fromindex = 0 ;
         if (value.charAt(0) == '"' && value.length() > 1)
         {
         	int endstring = value.indexOf('"',1) ;
            if (endstring > 0) fromindex = endstring ;
         }
         
         // PlayFKiss delimits parameter lists on commas, periods, and other 
         // non-alphanumeric characters. The regular expression splits on 
         // anything that is a comma or a period.
         
         if (OptionsDialog.getPlayFKissCompatibility())
         {
            String valuefrom = value.substring(fromindex) ;
            String [] parts = valuefrom.split("[\\,\\.]",2) ;
            i = (parts.length == 1) ? -1 : parts[0].length() + fromindex ;
         }
         else
            i = value.indexOf(',',fromindex) ;
         
			if (i < 0)
			{
				token = value ;
				value = "" ;
			}
			else
			{
				token = value.substring(0,i) ;
				value = value.substring(i+1) ;
			}

			// If the first parameter character is a digit ensure that the
         // parameter is a valid number. Also check negative numbers.
         // Also determine if we are using local variables.

			String param = trim(token).trim() ;
         if (param.length() > 0 && param.charAt(0) == '@') fkisslevel = 5 ;
         if ((param.length() > 0 && Character.isDigit(param.charAt(0))) ||
            (param.length() > 1 && param.charAt(0) == '-' && Character.isDigit(param.charAt(1))))
         {
				try { Long.parseLong(param) ; }
            catch (NumberFormatException e1)
            { 
               try { Double.parseDouble(param) ; }
               catch (NumberFormatException e2)
               {	
                  showError("FKiSS invalid number format: " + param,param) ; 
               }
            }
         }

         // Add the parameter text to the vector.

			v.addElement(new String(trim(token).trim())) ;
			value = trim(value).trim() ;
		}
		return s ;
	}


	// Function to create a new audio media object for the given file.
	// Objects are created if they do not already exist.  We will create
	// one audio object for a blank file name.  A request to play an
	// unnamed file is interpreted as a request to stop the current
	// audio output.

	private void createAudio(ArchiveFile zip, String file)
	{
		if (file == null) return ;
		file = trim(file) ;
		Audio audio = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,file.toUpperCase()) ;
		if (audio != null) return ;

		// Set our references.

      File f = new File(this.getDirectory(),file) ;
		if (ArchiveFile.isAudioSound(file))
			audio = new AudioSound(zip,f.getPath(),ref) ;
		if (ArchiveFile.isAudioMedia(file))
         if (Kisekae.isMediaInstalled())
   			audio = new AudioMedia(zip,f.getPath(),ref) ;
		if (audio != null)
		{
			audio.setID(cid) ;
			audio.setIdentifier(new Integer(audioCount++)) ;
			audio.setKey(audio.getKeyTable(),cid,audio.getPath().toUpperCase()) ;
			audio.setKey(audio.getKeyTable(),cid,audio.getName().toUpperCase()) ;
			audio.setKey(audio.getKeyTable(),cid,file.toUpperCase()) ;
         if (audio.isImported()) 
            audio.setKey(audio.getKeyTable(),cid,"Import "+audio.getName().toUpperCase()) ;
			audio.setLine(line) ;
         audio.setRelativeName(file) ;
			sounds.addElement(audio) ;
		}
      else if (!Kisekae.isMediaInstalled() || !ArchiveFile.isList(file))
         showError(file + " is not a recognized audio file type.",file) ;
	}


   // Function to remove internally generated FKiSS statements from an action
   // list.  The list is resequenced to ensure that sequence numbers are
   // consecutive.

   private void removeGeneratedStatements(Vector v)
   {
      if (v == null) return ;
      Vector newlist = new Vector() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         FKissAction a = (FKissAction) v.elementAt(i) ;
         if (a.isInternal()) continue ;
         a.setSequence(newlist.size()) ;
         newlist.addElement(a) ;
      }

      // Resequence the list.

      v.removeAllElements() ;
      v.addAll(newlist) ;
   }


   // Function to scan the action list and construct the loop return points.
   // We create the iteration entries if an action is a loop command.  The
   // iteration entries are used to retain loop and return action command
   // indexes, or addresses, for process control during event handling.

   private void buildLoopReturn(Vector v)
   {
      if (v == null) return ;
      Vector iterations = new Vector() ;

      // Scan the action list and construct a list of loop actions.

      for (int j = 0 ; j < v.size() ; j++)
      {
         Object o = v.elementAt(j) ;
         if (!(o instanceof FKissAction)) continue ;
         FKissAction a = (FKissAction) o ;

         // While and For statements create iteration entries.

         if (EventHandler.isIterationAction(a))
         {
            Object [] iteration = new Object[3] ;
            iteration[0] = a ;
            iteration[1] = new Integer(a.getSequence()) ;
            iteration[2] = null ;
            iterations.addElement(iteration) ;
         }

         // Identify the loop return action if this is a loop terminator
         // command.  We scan our iteration entries backwards seeking the
         // last matching loop command with the correct index parameter.

         if (EventHandler.isEndIterationAction(a))
         {
            boolean found = false ;
         	for (int i = iterations.size()-1 ; i >= 0 ; i--)
            {
            	Object o1 [] = (Object []) iterations.elementAt(i) ;
               if (o1 == null) continue ;
               FKissAction loopaction = (FKissAction) o1[0] ;
               if ("endwhile".equals(a.getIdentifier()))
               	if (!("while".equals(loopaction.getIdentifier()))) continue ;
               if ("next".equals(a.getIdentifier()))
	               if (!("for".equals(loopaction.getIdentifier()))) continue ;

               // We have a matching loop command.  Check for identifier
               // equivalence and if they match set the loop end statement
               // return point.  This is the action list sequence number.

               String s1 = a.getFirstParameter() ;
               String s2 = loopaction.getFirstParameter() ;
               if (s1 == null || s2 == null) continue ;
               if (!s1.equals(s2)) continue ;
               Integer sequence = (Integer) o1[1] ;
               if (sequence != null) a.setLoopEntry(sequence.intValue()) ;
               found = true ;
               break ;
            }

            // If we failed to find the matching loop entry signal an error.

            if (!found && OptionsDialog.getStrictSyntax())
            {
               showWarning("[Line " + a.getLine() + "] " + a.getName()
                  + " does not have a matching loop statement.") ;
            }
         }
      }
   }



	// Miscellaneous utility functions
	// -------------------------------

	// Function to return tokens from text that crosses multiple lines.
	// We return the string with leading spaces removed, and when
	// necessary we read a new line.

	private String nextToken(String s, BufferedReader f)
		throws IOException
	{
		s = trim(s) ;
		newline = false ;
		while (s.length() == 0)
		{
			newline = true ;
			s = (reread) ? lastline : f.readLine() ;
			if (s == null) break ;
         if (!reread) leadaction.addElement(s) ;
         if (!reread) trailtoken.addElement(s) ;
			if (!reread && loader != null) loader.updateProgress(s.length()+2) ;
			reread = false ;
			lastline = s ;
			line = line + 1 ;
			s = trim(s) ;
		}
		return s ;
	}


	// Function to trim leading spaces from a string.  This includes standard
   // whitespace and all ISO control characters and null characters.

	static String trim(String s)
	{
      if (s == null) return "" ;
		int i = 0 ;
      int length = s.length() ;
		while (i < length)
      {
      	char c = s.charAt(i) ;
         if (Character.isSpaceChar(c)) i++ ;
         else if (Character.isWhitespace(c)) i++ ;
         else if (Character.isISOControl(c)) i++ ;
         else break ;
      }
      if (i >= length) return "" ;
		return s.substring(i) ;
	}


	// Function to find a terminating character in a string.  This function
   // is used to find the first character outside of a string literal.
   // String quotes within a string must be escaped with a backslash.
   // Backslashes within a string must be escaped with a backslash.

	private int findTerminator(String s, char c)
	{
		boolean literal = false ;

		for (int i = 0 ; i < s.length() ; i++)
		{
			if (s.charAt(i) == '"')
         {
				if (i == 0) literal = !literal ;
            else if (s.charAt(i-1) != '\\') literal = !literal ;
            else if (findEscapeCount(s,i-1) % 2 == 0) literal = !literal ;
         }
			if (!literal && s.charAt(i) == c) return i ;
		}
		return -1 ;
	}


	// Function to count consecutive backslashes in a string.  This function
   // is used to determine if a character is escaped.

	private int findEscapeCount(String s, int n)
	{
      int count = 0 ;
      while (n > 0)
      {
         if (s.charAt(n) != '\\') break ;
         count++ ;
         n-- ;
      }
      return count ;
   }


	// Function to locate a Group or Cel by name.  This function will return
	// a group object, a cel object, or a CelGroup object if something is
	// found.  If we are searching for unique cels and duplicate cels exist
   // then we return null.

	private Object findGroupOrCel(String s) { return findGroupOrCel(s,false) ; }
	private Object findGroupOrCel(String s, boolean unique)
	{
      if (s == null) return null ;
		Object o = Group.findGroup(s,this,null) ;
      if (o != null) return o ;
		o = Cel.findCel(s,this,null) ;
      if (o instanceof Cel)
      {
         if (!unique) return o ;

         // See if we have a literal name.
      
         if (s.charAt(0) == '\"')
         {
            int i = s.lastIndexOf('\"') ;
            if (i < 1) return null ;
            s = s.substring(1,i) ;
         }
         
         // If we are duplicated, return non-unique.
         
         if (Cel.hasDuplicateKey(Cel.getKeyTable(),cid,s)) return null ;
         return o ;
      }
		o = CelGroup.findCelGroup(s,this,null) ;
		return o ;
	}
   
   // Function to locate a properties pool.  This function returns
   // a new proerties object if the correct one cannot be loaded.
   
   static Properties findValuepoolProperties(String s, Configuration config)
   {
      Properties p = new Properties() ;
      if (config == null) return p ;
      ArchiveFile zip = config.getZipFile() ;
      if (zip == null) return p ;
      
      // Private pool names are of the form 'cnfname.properties', unless we
      // are loaded from an archive file in which case the names are
      // 'archivename.cnfname.properties'.  This ensures that similar
      // cnf names are differentiated between archives. 
      
      if (s == null || s.length() == 0) 
      {
         if (zip.isArchive()) 
            s = zip.getFileName() + "." + config.getName() ;
         else
            s = config.getName() ;
      }
      if (s == null || s.length() == 0) return p ;
      String poolname = s + ".properties" ;

      // Try to access the valuepool from our default directory.
      
      try
      {
         Object o = propertypool.get(s) ;
         if (o instanceof Properties)
            return (Properties) o ;
         if (o != null) return null ;
         
         File f = new File((zip.isArchive()) ? zip.getName() : config.getPath()) ;
         String filedir = f.getParent() ;
         if (filedir != null)
         {
            f = new File(filedir,poolname) ;
            FileInputStream in = new FileInputStream(f) ;
            p.load(in) ;
            in.close() ;
         }
         else
         {
            if (OptionsDialog.getDebugControl())
               System.out.println("Kisekae: Unable to load properties " + poolname 
               + ", configuration path is " + config.getPath()) ;
         }
      }
      catch (Exception e)
      {  
         // If the read failed and the configuration was loaded from an archive, 
         // then check the archive for the initial properties file.  In all cases
         // the output valuepools will be written to a directory and not the archive.
         // This means that any subsequent load of the configuration will reference
         // the updated properties unless the property files were deleted.

         if (zip.isArchive())
         {
            try
            {
               // Our private value pool names as stored in a directory are of the 
               // form 'archivename.cnfname.properties'.  However, in the archive 
               // itself the private pool name should be simply 'cnfname.properties'
               
               String s1 = poolname ;
               String s2 = zip.getFileName() ;
               if (s1.startsWith(s2))
                  s1 = s1.substring(s2.length()+1) ;
               
               boolean b = zip.isOpen() ;
               if (!b) zip.open() ;
               ArchiveEntry ze = zip.getEntry(s1) ;
               InputStream is = ze.getInputStream() ;
               p.load(is) ;
               is.close() ;
               if (!b) zip.close() ;
            }
            catch (Exception ex)
            {  
               if (OptionsDialog.getDebugControl())
                  System.out.println("Kisekae: Unable to load archive properties " + poolname) ;
            }
         }
         else
         {
            if (OptionsDialog.getDebugControl())
               System.out.println("Kisekae: Unable to load properties " + poolname) ;
         }
      }
      
      propertypool.put(s,p) ;
      return p ;
   }
   
      
   // Save any valuepool property settings.
   
   void saveValuepoolProperties()
   {
      Enumeration keys = propertypool.keys() ;
      while (keys.hasMoreElements())
      {
         Object key = keys.nextElement() ;
         Object o = propertypool.get(key) ;
         if (!(o instanceof Properties)) continue ;
         Properties p = (Properties) o ;
         
         // Create the output stream and write the properties collection.
         // If the properties set is empty then delete any previous file.
             
         try
         {
            ArchiveFile zip = getZipFile() ;
            File f = new File((zip.isArchive()) ? zip.getName() : getPath()) ;
            String filedir = f.getParent() ;
            if (filedir != null)
            {
               f = new File(filedir,key.toString() + ".properties") ;
               if (p.size() > 0)
               {
                  OutputStream os = new FileOutputStream(f) ;
                  p.store(os,"UltraKiss Valuepool " + key.toString()) ;
                  os.close() ;
               }
               else
               {
                  f.delete() ;
               }
            }
         }
         catch (IOException e)
         {
            System.out.println("Configuration: write valuepool " + key.toString() + " " + e);
         }
      }
   }
   


	// Function to display a syntax error message.

	private void showError(String s) { showError(s,null) ; }
	private void showError(String s, String highlite)
	{
   	errormessage = s ;
      String linetext = Kisekae.getCaptions().getString("LineText") ;
		if (loader != null && !Kisekae.isBatch())
			loader.showError("[" + linetext + " " + line + "] " + s,highlite) ;
		else
			System.out.println("[" + linetext + " " + line + "] " + s) ;
	}


	// Function to display a syntax warning message.

	private void showWarning(String s) { showWarning(s,null) ; }
	private void showWarning(String s, String highlite)
	{
   	errormessage = s ;
		if (loader != null)
			loader.showWarning(s,highlite) ;
		else
			System.out.println(s) ;
	}


	// Function to return a status message to the file loader.

	private void showStatus(String s)
	{
		if (loader != null)
			loader.showStatus(s) ;
		else
			System.out.println(s) ;
	}


	// Function to return a file message to the file loader.

	private void showFile(String s)
	{
		if (loader != null)
			loader.showFile(s) ;
		else
			System.out.println(s) ;
	}


   // Inner class to define a function to compare events by their name.

   class EventComparator
   	implements Comparator
   {
   	public int compare(Object o1, Object o2)
      {
      	if (!(o1 instanceof FKissEvent)) return 0 ;
         if (!(o2 instanceof FKissEvent)) return 0 ;
         Integer s1 = new Integer(((FKissEvent) o1).getLine()) ;
         Integer s2 = new Integer(((FKissEvent) o2).getLine()) ;
         return (s1.compareTo(s2)) ;
      }
   }
}
