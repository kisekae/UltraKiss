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
* Video class
*
* Purpose:
*
* This class encapsulates a video object.  Video objects are displayed
* in their own frame.
*
* Video objects use their file name as their access key.
*
* The intent is as follows:
*
* 1. Reference the video file through a URL.
*
* 2. Realize the player.  The object is now initialized.  It does not play.
*    It waits in a realized state for a play request.
*
* 3. A play request prefetches and starts the player.  The play request can
*    come at any time.  The possible states are:
*
* 3.0 (Expected state) Init function complete, player waiting in a realized
*     state, and play is requested.
*	=> initiate prefetch.  On prefetch complete, start the player.
*
* 3.1 (Slow realize state) Init function complete, player in process of
*     realizing due to initialization, and play is requested.
*	=> Set callback switch so that when realize is complete, play is initiated.
*
* 3.2 (Slow prefetch state) Init function complete, player is prefetching, and
*     another play is requested.
*	=> Ignore the play request as the player will start when prefetch completes.
*
* 3.3 (Busy state) Init function complete, player is started, and another play
*     is requested.
*	=> restart the player with setMediaTime(0) and prefetch.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.util.Hashtable ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.net.URL ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.media.* ;
import javax.media.protocol.* ;


final class Video extends Cel
   implements ActionListener
{
	// Class attributes.

	static private Vector players = new Vector() ;

	// Video cel attributes

	private Video me = null ;					// Reference to ourselves
   private Dimension initsize = null ;		// The initial cel size
	private byte b[] = null ;					// The video file data
	private int line = 0 ;						// The configuration file line
	private int bytes = 0 ;						// The file size in bytes
	private int repeatcount = 0 ;			  	// The repeat count
	private int border = 0 ;			  	   // The window border size
	private String currentattr = null ;    // Current attributes
   private Color background = null ;      // Initial background
   private Color foreground = null ;      // Initial foreground 
   private Color bordercolor = null ;     // Initial foreground 
   private JPanel panel1 = null ;         // Window panel

	// State attributes

	private boolean loaded = false ;			// True if data has been loaded
	private boolean frominclude = false ;	// True if data from include file
	private boolean initialized = false ;	// True if video is initialized
	private boolean activated = false ;    // True if video visibility active
	private boolean realized = false ;		// True if player is realized
	private boolean wasstarted = false ;	// True if player was started
	private boolean copy = false ;			// True if file is a copy
	private boolean cache = false ;			// True if load media to memory
	private boolean started = false ;		// True if player ever started
	private boolean repeat = false ;			// True if movie repeats
	private boolean restart = false ;		// True if restart after stop
	private boolean show = false ;			// True if window must be shown
   private boolean showcontrols = false ;	// True if controls shown in window

	// Media attributes

	private Player player = null ;			// The media player object
	private Component visual = null ; 		// The visual component
	private Component controls = null ; 	// The control component
	private ContentDescriptor cd = null ; 	// The media content descriptor
	private DataSource ds = null ;			// The video data source
   private Object listener = null ;			// Last listener added
   private PanelFrame panel = null ;      // Panel for redraws
	private JWindow window = null ;			// The video window
   private VideoListener vlistener = null ;	// The controller listener

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the palette file is
   // saved.

	private CallbackButton stopcallback = null ;
	private CallbackButton startcallback = null ;
	private CallbackButton prefetchcallback = null ;

   
	// Constructor for the case when we do need a window.

	public Video(ArchiveFile zip, String file, Configuration ref)
	{
		me = this ;
		setUniqueID(new Integer(this.hashCode())) ;
		init(zip,file,ref) ;
	}

	// Initialization method.

	private void init(ArchiveFile zip, String file, Configuration ref)
	{
      super.init() ;
		setZipFile(zip) ;
		this.file = convertSeparator(file) ;
		this.ref = ref ;
      initinput = true ;
      background = Color.black ;
      foreground = Color.black ;
      bordercolor = Color.black ;
      panel1 = new JPanel() ;
      panel1.setLayout(new BorderLayout()) ;

		// Decode the video content based upon the file type.

		int n = file.lastIndexOf('.') ;
		extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
      stopcallback = new CallbackButton(this,"Video Callback") ;
      startcallback = new CallbackButton(this,"Video Start Callback") ;
      prefetchcallback = new CallbackButton(this,"Video Prefetch Callback") ;
		Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));
	}


	// Object state reference methods
	// ------------------------------

	// Method to return the video data.

	byte [] getVideoData() { return b ; }

	// Method to return the video file size.

	int getBytes() { return bytes ; }

	// Method to return the player.

	Player getPlayer() { return (error) ? null : player ; }

	// Method to return the content descriptor.

	Object getContentDescriptor() { return (error) ? null : cd ; }

   // Method to return the last listener object added.

   Object getListener() { return listener ; }

	// Method to return the control visibility indicator.

	boolean getShowControls() { return showcontrols ; }

	// Method to return the content type.

	String getContentType()
	{ return (cd == null) ? "unknown" : cd.getContentType() ; }

	// Method to return the control component.

	Component getControlPanelComponent()
	{
		if (player == null) return null ;
		if (player.getState() < Player.Realized) return null ;
		return player.getControlPanelComponent() ;
	}

	// Method to return the visual component.

	Component getVisualComponent()
	{
		if (player == null) return null ;
		if (player.getState() < Player.Realized) return null ;
		return player.getVisualComponent() ;
	}

   // Return the player window frame.

	JWindow getWindow() { return window ; }

	// The configuration file line number showing where this object was
	// first declared is used for diagnostic output messages.

	void setLine(int l) { if (line == 0) line = l ; }

   // Set the cel loaded state.  

   void setLoaded(boolean b) { loaded = b ; }

   // Indicate if loaded from an include file.  

   void setFromInclude(boolean b) { frominclude = b ; }

	// Set the configuration ID for this video file.

	void setID(Object id) { cid = id ; }

	// Set the movie repeat flag.

	void setRepeat(int n) { repeatcount = n ; repeat = (n != 0) ; }

	// Set the restart after stop flag.

	void setRestart(boolean b) { restart = b ; }

	// Set the video activated flag.

	void setActivated(boolean b) { activated = b ; showComponent(show) ; }

	// Set the flag to show the control component in the window.

	void setShowControls(boolean b) { showcontrols = b ; }

	// Set the media stop callback listener.

	void addCallbackListener(ActionListener l)
   {
   	stopcallback.addActionListener(l) ;
   }

	// Set the media prefetch callback listener.

	void addPrefetchCallbackListener(ActionListener l)
   {
   	prefetchcallback.addActionListener(l) ;
   }

	// Return the video image copy indicator.

	boolean isCopy() { return copy ; }

	// Return an indication if the video object is realized.

	boolean isRealized() { return realized ; }

	// Return an indication if the video object is repeating.

	boolean isRepeating() { return (repeatcount != 0) ; }

   // Return an indication if the data has been loaded.

   boolean isLoaded() { return loaded ; }

   // Return an indication if data was loaded from an include file.

   boolean isFromInclude() { return frominclude ; }

	// Return an indication if the video has been activated.

	boolean isActivated() { return activated ; }

	// Return an indication if the video was started.

	boolean wasStarted() { return wasstarted ; }

	// Return a writable file indicator.

	boolean isWritable() { return (!("".equals(getRelativeName()))) ; }

	// Method to write our data file to an output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
		if (error) return -1 ;
      if (ze == null) return -1 ;
		InputStream	in = ze.getInputStream() ;
		byte buffer [] = new byte[10240] ;
      int bytes = 0 ;

		while (in != null)
      {
        	int n = in.read(buffer,0,buffer.length) ;
			if (n <= 0) break ;
			bytes += n ;
         out.write(buffer,0,n);
      }
		return bytes ;
	}



	// Movie state reference methods
	// ------------------------------

   // Set the initial video attributes.

   void setInitAttributes(String s)
   {
      attributes = s ;
      setAttributes(s) ;
   }


	// Set the video attributes.  This will update the component image.
   // Temporary attribute changes through FKiSS do not update the current
   // attributes.

	void setAttributes(String s) { setAttributes(s,false) ; }
	void setAttributes(String s, boolean temp)
   {
      if (s == null) return ;
      String s1 = eraseLiterals(s) ;
		if (!temp) currentattr = eraseNullAttributes(s) ;
      int i = 0 ;

      // Parse and apply attributes.

      if ((i = s1.indexOf("controls")) >= 0)         // enable controls
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (window != null && controls != null)
         {
            Dimension ws = window.getSize() ;
            Dimension cs = controls.getSize() ;
            if (onoff) 
            {
               panel1.add(controls,BorderLayout.SOUTH) ;
               if (!showcontrols) ws.height += cs.height ;
            }
            else
            {
               panel1.remove(controls) ;
               if (showcontrols) ws.height -= cs.height ;
            }
            window.setSize(ws) ;
            window.validate() ;
            setSize(ws) ;
         }
         showcontrols = onoff ;
      }
      
      if ((i = s1.indexOf("repeat")) >= 0)          // enable repeat
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         repeat = onoff ;
         repeatcount = (repeat) ? -1 : 0 ;
      }

      if ((i = s1.indexOf("bdc")) >= 0)           // set border color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s1.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try { bordercolor = new Color(Integer.parseInt(s2)) ; }
         catch (NumberFormatException e) { }
         if (!onoff) bordercolor = Color.black ;
      }

      if ((i = s1.indexOf("border")) >= 0)        // set the border size
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s1.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try { border = Integer.parseInt(s2) ; }
         catch (NumberFormatException e) { }
         if (!onoff) border = 0 ;
         if (border < 0) border = 0 ;
         if (border > 0)
            panel1.setBorder(new LineBorder(bordercolor,border)) ;
         else
            panel1.setBorder(null) ;
         if (window != null) window.validate() ;
      }

      if ((i = s1.indexOf("bc")) >= 0)           // set background color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s1.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try { background = new Color(Integer.parseInt(s2)) ; }
         catch (NumberFormatException e) { }
         if (!onoff) background = Color.black ;
      }

      if ((i = s1.indexOf("fc")) >= 0)           // set foreground color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s1.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try { foreground = new Color(Integer.parseInt(s2)) ; }
         catch (NumberFormatException e) { }
         if (!onoff) foreground = Color.black ;
      }

      if ((i = s1.indexOf("fontname")) >= 0)    // set the font
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (window != null)
         {
            Font f = window.getFont() ;
            if (f == null) return ;
            int size = f.getSize() ;
            int style = f.getStyle() ;
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            f = new Font(s2,style,size) ;
            if (!onoff) f = null ;
            window.setFont(f);
         }
      }

      if ((i = s1.indexOf("fontsize")) >= 0)    // set the font size
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (window != null)
         {
            Font f = window.getFont() ;
            if (f == null) return ;
            String s2 = s1.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            float n = f.getSize2D() ;
            try { n = Float.parseFloat(s2) ; }
            catch (NumberFormatException e) { }
            f = f.deriveFont(n) ;
            if (!onoff) f = null ;
            window.setFont(f);
         }
      }

      if ((i = s1.indexOf("fontstyle")) >= 0)    // set the font attributes
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (window != null)
         {
            Font f = window.getFont() ;
            if (f == null) return ;
            String s2 = s1.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            int n = f.getStyle() ;
            if ("bold".equals(s2)) n = Font.BOLD ;
            if ("italic".equals(s2)) n = Font.ITALIC ;
            if ("plain".equals(s2)) n = Font.PLAIN ;
            f = f.deriveFont(n) ;
            if (!onoff) f = null ;
            window.setFont(f);
         }
      }
   }
   
      
  // Function to erase all string literals in the comment.
   
   private String eraseLiterals(String s)
   {
      if (s == null) return null ;
      s = s.toLowerCase() ;
      StringBuffer sb = new StringBuffer(s) ;
      boolean inliteral = s.startsWith("\"") ;
      for (int i = 1 ; i < sb.length() ; i++)
      {
         if (sb.charAt(i) == '"' && sb.charAt(i-1) != '\\') 
            inliteral = !inliteral ;
         else if (inliteral && sb.charAt(i) != '\\') 
            sb.replace(i,i+1," ") ;
      }
      return sb.toString() ; 
   }
   
      
  // Function to erase all attribute negation entries.
   
   private String eraseNullAttributes(String s)
   {
      if (s == null) return null ;
      boolean innegation = false ;
      String s1 = eraseLiterals(s) ;
      StringBuffer sb = new StringBuffer() ;
      for (int i = 0 ; i < s1.length() ; i++)
      {
         if (s1.charAt(i) == '-') innegation = true ;
         if (!innegation) sb.append(s.charAt(i)) ;
         if (s1.charAt(i) == ',') innegation = false ;
      }
		s = sb.toString() ;
		if (s.endsWith(",")) s = s.substring(0,s.length()-1) ;
		return s ;
   }


	// Return the current attributes. 

	String getAttributes()
	{
		if (currentattr != null) return new String(currentattr) ;
      String s = super.getAttributes() ;
      if (s == null && showcontrols) s = "" ;
      if (s != null && showcontrols && s.indexOf("controls") < 0) 
      {
         s = "controls," + s ;
         if (s.endsWith(",")) s = s.substring(0,s.length()-1) ;
      }
      return s ; 
	}


	// Return the current attribute value. 

	String getAttribute(String s)
	{
      String attributes = getAttributes() ;
      if (attributes == null) return "" ;
      String s1 = eraseLiterals(attributes) ;
      if (repeat && s1.indexOf("repeat") < 0) s1 = "repeat," + s1 ;
      if (showcontrols && s1.indexOf("controls") < 0) s1 = "controls," + s1 ;
      s = Variable.getStringLiteralValue(s.toLowerCase()) ;
      int n1 = s1.indexOf(s) ;
      if (n1 < 0) return "" ;
      n1 += s.length() ;
      if (n1 < attributes.length() && attributes.charAt(n1) == '=') n1++ ;
      int n2 = s1.indexOf(',',n1) ;
      if (n2 < 0) n2 = s1.length() ;
      if (n2 == n1) return s ; 
      String s2 = attributes.substring(n1,n2) ;
      s2 = Variable.getStringLiteralValue(s2) ;
      return s2 ;
	}
   

	// Window visibility.

	void showComponent(boolean b) 
   { 
      show = b ;
      if (!activated) return ;
      if (isVisible() && show) 
      { 
         if (OptionsDialog.getDebugMovie() && window != null)
            PrintLn.println("Video: " + getName() + " Show component.") ;
         draw(null,null) ; 
         if (window != null) window.setVisible(true) ; 
         if (panel != null) panel.showpage() ;
         if (player != null && player.getState() == Player.Prefetched)
            if (wasstarted) player.start() ;
      }
      else
         if (window != null) window.setVisible(false) ;
   }


	// Set the movie visible flag.

	void setVisible(boolean b)
	{
      super.setVisible(b) ;
      showComponent(show) ;
	}


	// Set the component size.

	synchronized void setSize(Dimension d)
   {
      super.setSize(d) ;
      if (window != null) window.setSize(d) ;
   }


	// Set the component drawable parent.  Add mouse listeners to the visual 
   // component so that the panel can recognize KiSS events and drags.
   
	void setPanel(PanelFrame p) 
   { 
		if (panel != null && visual != null)
		{
			visual.removeMouseListener((MouseListener) panel) ;
			visual.removeMouseMotionListener((MouseMotionListener) panel) ;
      }
      panel = p ; 
		if (panel != null && visual != null)
		{
			visual.addMouseListener((MouseListener) panel) ;
			visual.addMouseMotionListener((MouseMotionListener) panel) ;
		}
   }


	// Set the movie input flag.  
   
	void setInput(boolean b)
   {
      super.setInput(b) ;
      showComponent(b) ; 
  }


   // Reset the initial state of the video.

   void reset()
   {
      stopmovie() ;
      showComponent(false) ; 
      if (player != null && player.getState() >= Player.Realized) 
         player.setMediaTime(new Time(0)) ;

		// Release critical resources.

      if (stopcallback != null) 
         stopcallback.removeActionListener(null) ;
      if (startcallback != null) 
         startcallback.removeActionListener(null) ;
      if (prefetchcallback != null) 
         prefetchcallback.removeActionListener(null) ;
		if (visual != null && panel != null)
		{
			visual.removeMouseListener((MouseListener) panel) ;
			visual.removeMouseMotionListener((MouseMotionListener) panel) ;
		}
      
      panel = null ;
      activated = false ;
   }


   // Reset the initial attribute state of the component.

   void resetAttributes()
   {
      String s = "-controls,-repeat,-border,-bc,-fc,-fontname," ;
      s += "-fontsize,-fontstyle,-bdc" ;
      setAttributes(s) ;
      setAttributes(attributes) ;
      currentattr = null ;
   }




	// Object loading methods
	// ----------------------

	// Method to read the video file.  By default, video files are not
   // loaded to memory.

	void load(Vector includefiles)
	{
   	cache = OptionsDialog.getCacheVideo() ;
		InputStream is = null ;
      String name = getRelativeName() ;
      if (name != null) name = name.toUpperCase() ;
      scaledimage = null ;
      filteredimage = null ;

		// Load the file if another copy of the video stream has not been
		// loaded.  If we have previously read the file use the prior
		// video stream.

		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,name) ;
		if (c instanceof Video && c.isLoaded()) 
      {
      	loadCopy(c) ;
			if (zip != null) ze = zip.getEntry(file) ;
         if (ze != null) ze.setCopy(copy) ;
         return ;
      }

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
				c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),name) ;
            if (c == null)
            {
               c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),"Import "+getName().toUpperCase()) ;
            }
				if (c instanceof Video && c.isLoaded())
	         {
	         	loadCopy(c) ;
               copy = false ;
               if (!c.isFromInclude()) zip.addEntry(ze) ;
               zip.setUpdated(ze,c.isUpdated()) ;
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

		// Read the new video file.

		try
		{
			if (zip != null) ze = zip.getEntry(file) ;

			// Load a reference copy if it exists.

			if (ze == null)
			{
				if (ref != null)
				{
               c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),name) ;
					if (c instanceof Video && c.isLoaded())
               {
               	loadCopy(c) ;
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
               ((includename != null) ? (" (" + includename + ")") : "")) ;
         }

			// Set the cel characteristics.

         Point initialoffset = getInitialOffset() ;
         offset.x = initialoffset.x ;
         offset.y = initialoffset.y ;
         truecolor = true ;

			// Create the file input stream.

			if (!cache) return ;
			is = (zip == null) ? null : zip.getInputStream(ze) ;
			if (is == null) throw new IOException("file not found") ;

			// Read the entire contents.

			int n = 0, len = 0 ;
			b = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;
		}

		// Watch for I/O errors

		catch (IOException e)
		{
			error = true ;
			showError("I/O Exception, Video " + file + ", " + e.getMessage()) ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; }
			catch (IOException e)
			{
         	b = null ;
				error = true ;
				showError("I/O Exception, Video " + file + ", " + e.getMessage()) ;
			}
         is = null ;
			loaded = true ;
		}
	}


	// Load a copy of the video data from the specified object.

	void loadCopy(Cel c)
	{
      if (!(c instanceof Video)) return ;
      Video a = (Video) c ;

		// Set the cel characteristics.

      sf = 1.0f ;
      copy = true ;
   	loaded = true ;
      scaled = false ;
      truecolor = true ;
      scaledsize = null ;
      Point initialoffset = getInitialOffset() ;
      baseoffset = c.getBaseOffset() ;
      offset.x = baseoffset.x + initialoffset.x ;
      offset.y = baseoffset.y + initialoffset.y ;
      ze = a.getZipEntry() ;
		b = a.getVideoData() ;
		bytes = a.getBytes() ;
      setLastModified(a.lastModified()) ;
      setUpdated(a.isUpdated()) ;
      setFromInclude(a.isFromInclude()) ;
      
      if (loader != null)
      {
         String s = Kisekae.getCaptions().getString("FileNameText") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1) + file + s.substring(j1+1) ;
         loader.showFile(s + " [" + bytes + " bytes] (copy)") ;
      }
	}



	// Video player initialization. 	Create a data source from the
	// file. Video objects are not usually retained in memory.

	void init()
	{
		started = false ;
      if (!loaded) return ;
      if (initialized) return ;
      initsize = getSize() ;
      if (!Kisekae.isMediaInstalled())
      {
         errormessage = "Video requires Java Media Framework to be installed." ;
         error = true ;
      }
		if (error || (cache && b == null)) return ;
		if ("".equals(getPath())) return ;

      if (!OptionsDialog.getCacheVideo() && zip != null) zip.connect() ;
		if (OptionsDialog.getDebugMovie())
      	PrintLn.println("Video: " + getName() + " Initialization request.") ;

		try
		{
			if (cache)
			{
				ds = new MediaDataSource(getContentType(file),b,b.length,getName()) ;
				ds.connect() ;
			}
			else
			{
				ds = new MediaDataSource(getContentType(file),ze) ;
				ds.connect() ;
			}
		}
		catch (Exception e)
		{
      	ds = null ;
			error = true ;
			showError("Unable to establish data source for " + file) ;
			if (!(e instanceof KissException)) 
            PrintLn.println(e.getMessage());
		}

		// Create an instance of a player for this data source.

		if (ds != null)
		{
			Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true)) ;
			try { player = Manager.createPlayer(ds) ; }
			catch (Throwable e)
			{
				ds = null ;
				error = true ;
				showError("Unable to create media player for " + file) ;
				if (!(e instanceof KissException)) 
               PrintLn.println(e.getMessage());
			}
		}

		// Prepare the player.

		if (!error)
		{
      	vlistener = new VideoListener() ;
			player.addControllerListener(vlistener) ;
         realize() ;
		}
	}



	// Video player open. 	Media sources are opened when initialized.

	void open() { }




   // Method to add a listener to our player object.

   void addListener(Object ce)
   {
   	listener = ce ;
		if (ce instanceof ControllerListener && player != null)
        	player.addControllerListener((ControllerListener) ce) ;
   }



   // Method to remove a listener from our player object.

   void removeListener(Object ce)
   {
		if (ce instanceof ControllerListener && player != null)
        	player.removeControllerListener((ControllerListener) ce) ;
	}



	// Method to play the video file.

	void play()
	{
   	if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { play() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

		try
		{
         if (!loaded) return ;
			if (player == null) return ;
	      if (!Kisekae.isMediaInstalled()) return ;
			if (!OptionsDialog.getMovieOn()) return ;
			if (error) throw new KissException("object in error") ;
			if (!players.contains(me)) players.addElement(me) ;

	      // Identify the player state for debug output.

         String state = "Unknown" ;
			int playerstate = player.getState() ;
         if (playerstate == Player.Prefetched) state = "Prefetched" ;
         if (playerstate == Player.Prefetching) state = "Prefetching" ;
         if (playerstate == Player.Realized) state = "Realized" ;
         if (playerstate == Player.Realizing) state = "Realizing" ;
         if (playerstate == Player.Started) state = "Started" ;
         if (playerstate == Player.Unrealized) state = "Unrealized" ;
      	if (OptionsDialog.getDebugMovie())
         	PrintLn.println("Video: " + getName() + " Play request, state " + state) ;

			// Start the player. If it is currently playing stop it and restart.
         // The stop request reinvokes this play() method.

			if (player != null)
			{
				if (playerstate >= Player.Started)
            {
            	restart = true ;
            	player.stop() ;
               return ;
            }

            // Player is realized or stopped set up to start at the beginning.
            // If we are running from an lzh archive this can be slow.

				if (playerstate >= Player.Realized)
				{
            	if (playerstate != Player.Started)
						player.setMediaTime(new Time(0)) ;
				}

            // If it is not realized, show the realizing status.

            else
            {
            	MainFrame main = Kisekae.getMainFrame() ;
               if (main != null) main.showStatus("Realizing " + getName()) ;
               addPrefetchCallbackListener(this) ;
            }

            // Player is stopped.  It can be in the unrealized or
            // prefetched state.  Start it from the beginning. The 
            // window will be made visible on a prefetch complete event.

				player.start() ;
			}
		}

		catch (Exception e)
		{
			showError("Video " + getName() + " start fault " + e.getMessage()) ;
			if (!(e instanceof KissException)) e.printStackTrace();
		}
	}


	// Method to realize the video file.  Action listeners attached to the 
   // callback button are fired when the realize event is complete and the 
   // video attributes are established.

	void realize()
	{
   	if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { realize() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

		try
		{
         if (!loaded) return ;
			if (player == null) return ;
	      if (!Kisekae.isMediaInstalled()) return ;
			if (error) throw new KissException("object in error") ;
			if (!players.contains(me)) players.addElement(me) ;

	      // Identify the player state for debug output.

         String state = "Unknown" ;
			int playerstate = player.getState() ;
         if (playerstate == Player.Prefetched) state = "Prefetched" ;
         if (playerstate == Player.Prefetching) state = "Prefetching" ;
         if (playerstate == Player.Realized) state = "Realized" ;
         if (playerstate == Player.Realizing) state = "Realizing" ;
         if (playerstate == Player.Started) state = "Started" ;
         if (playerstate == Player.Unrealized) state = "Unrealized" ;
      	if (OptionsDialog.getDebugMovie())
         	PrintLn.println("Video: " + getName() + " Realize request, state " + state) ;

			// Realize the player.
         
			if (player != null)
         {
            if (playerstate == Player.Unrealized)
            {
               player.prefetch() ;
            }
         }
		}

		catch (Exception e)
		{
			showError("Video " + getName() + " realize fault " + e.getMessage()) ;
			if (!(e instanceof KissException)) e.printStackTrace();
		}
	}


	// Static method to stop playing any active video files.
   // The no-argument constructor stops all files.

	static void stop() { stop(null,null) ; }
	static void stop(Video v) { stop(null,v) ; }
	static void stop(Configuration c) { stop(c,null) ; }
	static void stop(Configuration c, Video v)
	{
		Vector p = (Vector) players.clone() ;
		for (int i = p.size()-1 ; i >= 0 ; i--)
		{
			Video video = (Video) players.elementAt(i) ;
			if (v != null && video != v) continue ;

			// Check for configuration archive file agreement.  If the object
         // configuration reference equals the specified configuration
         // reference the the video object was established at configuration
         // load time.  This would exclude media player objects.

			if (c != null)
			{
				ArchiveFile configzip = c.getZipFile() ;
				ArchiveFile videozip = video.getZipFile() ;
				if (!(configzip == videozip)) continue ;
			}

			// Stop the player.

        	video.stopmovie() ;
		}
	}


   // Internal function to start the movie in the window frame.
   // This sets up the window but the movie is not started until
   // a play or realize request.

   private void startmovie() throws KissException
   {
   	if (player == null) return ;
		MainFrame frame = Kisekae.getMainFrame() ;
		visual = player.getVisualComponent() ;
		controls = player.getControlPanelComponent() ;
      if (visual == null) throw new KissException("No visual component") ;

      // Create the window. Show it if the video cel is visible and
      // it is supposed to be shown on the page.
      
		window = createWindow(visual,controls) ;
      if (frame != null) frame.showStatus(null) ;
     	if (OptionsDialog.getDebugMovie())
        	PrintLn.println("Video: " + getName() + " Movie window created") ;
   }


   // Internal function to stop the movie player.  Stability problems once 
   // occurred if the player is stopped with a player stop request.

   private void stopmovie()
   {
     	if (OptionsDialog.getDebugMovie())
      	PrintLn.println("Video: " + getName() + " Stop request.") ;
		try
		{
         if (window != null && started) 
            wasstarted = true ;
         if (player != null) 
            player.stop() ;
      }
		catch (Exception e)
		{
			PrintLn.println("Video: player stop fault.") ;
			if (!(e instanceof KissException)) e.printStackTrace();
		}
   }


   // A utility function to fire any callback listeners waiting on
   // a movie stop signal.

	void doStopCallback()
   {
		Vector v = me.getEvent("mediastop") ;
		if (!isInternal() && started)
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
   	if (stopcallback == null) return ;
		stopcallback.doClick() ;
   }


	// A utility function to fire any video start events.  We use this to
	// queue any media start events.

	void doStartCallback()
	{
		Vector v = me.getEvent("mediastart") ;
		if (!isInternal() && !started)
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
   	if (startcallback == null) return ;
		startcallback.doClick() ;
	}


   // A utility function to fire any callback listeners waiting on
   // a movie prefetch signal.

	void doPrefetchCallback()
   {
      if (!realized) return ;
   	if (prefetchcallback == null) return ;
		prefetchcallback.doClick() ;
      prefetchcallback.removeActionListener(null) ;
   }


	// Method to close our video player.  After a close the video cannot
	// be played without being initialized once again.

	void close()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { close() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
			return ;
		}

      if (player == null) return ;
		if (!Kisekae.isMediaInstalled()) return ;
      if (!OptionsDialog.getCacheVideo() && zip != null) zip.disconnect() ;

		// Look for our player in the active play list.  If we find it,
      // remove it and close the player down.

     	if (OptionsDialog.getDebugMovie())
      	PrintLn.println("Video: " + getName() + " Close request.") ;
		players.removeElement(me) ;
		closeWindow() ;

		// Close the player.  This has been known to hang the system on
      // the deallocate.

		try
		{
			if (vlistener != null) player.removeControllerListener(vlistener) ;
			int state = player.getState() ;
			if (!error && state >= Player.Realized)
			{
				player.stop() ;
				player.close() ;
//				player.deallocate() ;
			}
			if (ds != null) ds.disconnect() ;
		}
		catch (Exception e)
		{
			showError("Video " + getName() + " close fault.") ;
			if (!(e instanceof KissException)) e.printStackTrace();
		}

		// Release critical resources.

      if (stopcallback != null) 
         stopcallback.removeActionListener(null) ;
      if (startcallback != null) 
         startcallback.removeActionListener(null) ;
      if (prefetchcallback != null) 
         prefetchcallback.removeActionListener(null) ;
      
		b = null ;
		ds = null ;
      panel = null ;
		player = null ;
		window = null ;
		visual = null ;
      controls = null ;
		stopcallback = null ;
		startcallback = null ;
		prefetchcallback = null ;
      vlistener = null ;
      realized = false ;
      initialized = false ;
      activated = false ;
	}



	// Create a player window frame.  The visual component is a heavyweight
	// component.  We use the panel frame mouse event handlers to process
	// mouse events on the visual component.

	JWindow createWindow(Component visual, Component controls)
	{
		if (visual == null) return null ;
      if (Kisekae.isBatch()) return null ;

      // If the video cel size has not been set we use the preferred
		// visual component size.

      Dimension size = (initsize == null) ? getSize() : initsize ;
      if (size.width == 0 || size.height == 0)
      {
      	size = new Dimension(visual.getPreferredSize()) ;
         if (showcontrols && controls != null)
         {
            Dimension d = controls.getPreferredSize() ;
            size.height += d.height ;
         }
      }
      setSize(size) ;

		// Create the window.  The video visual component is centered in
      // the window frame.

      MainFrame frame = Kisekae.getMainFrame() ;
      window = new VideoWindow(frame,this) ;
      window.setSize(size.width,size.height) ;
      Point paneloffset = frame.getPanelOffset() ;
      Point offset = getOffset() ;
      Rectangle box = getBoundingBox() ;
      int x = paneloffset.x + offset.x + (int) (box.x * sf) ;
      int y = paneloffset.y + offset.y + (int) (box.y * sf) ;
      window.setLocation(x,y) ;
      window.getContentPane().removeAll() ;
      panel1 = new JPanel() ;
      panel1.setLayout(new BorderLayout()) ;
      panel1.setBorder(new LineBorder(foreground,border)) ;
      window.getContentPane().add(panel1,BorderLayout.CENTER) ;
      panel1.add(visual,BorderLayout.CENTER) ;
      if (showcontrols && controls != null)
         panel1.add(controls,BorderLayout.SOUTH) ;
      window.validate() ;

      // Add mouse listeners to the visual component so that the panel
      // can recognize KiSS events and drags.
      
      if (panel != null && visual != null)
      {
         visual.removeMouseListener((MouseListener) panel) ;
         visual.removeMouseMotionListener((MouseMotionListener) panel) ;
         visual.addMouseListener((MouseListener) panel) ;
         visual.addMouseMotionListener((MouseMotionListener) panel) ;
      }
      return window ;
   }



	// Close a player window frame.  This disposes of the window and
	// removes listeners from the media visual component.

	synchronized void closeWindow()
	{
		MainFrame frame = Kisekae.getMainFrame() ;
		if (visual != null && panel != null)
		{
			visual.removeMouseListener((MouseListener) panel) ;
			visual.removeMouseMotionListener((MouseMotionListener) panel) ;
		}

      // Dispose of the window.

		if (window == null) return ;
      window.setVisible(false) ;
		window.getContentPane().removeAll() ;
		window.dispose() ;
		window = null ;

		// Force a redraw of the panel frame to capture this cel object
		// change.

		if (initsize != null) setSize(initsize) ;
		if (frame != null) frame.validate() ;
	}



	// Restore the visual component in our window.

	void restoreVisual(Component visual)
	{
      if (window == null) return ;
      panel1 = new JPanel() ;
      panel1.setLayout(new BorderLayout()) ;
      panel1.setBorder(new LineBorder(foreground,border)) ;
		window.getContentPane().removeAll() ;
		window.getContentPane().add(panel1,BorderLayout.CENTER) ;
		panel1.add(visual,BorderLayout.CENTER) ;
      if (showcontrols && controls != null)
         panel1.add(controls,BorderLayout.SOUTH) ;
      window.validate() ;
   }

   

   // Method to draw this cel.  For movies, we change the window location
   // based upon the drag position.

   synchronized void draw(Graphics g, Rectangle box)
   {
      if (!isVisible()) return ;
      if (Kisekae.isBatch()) return ;
      if (window == null) return ;
      MainFrame frame = Kisekae.getMainFrame() ;
      if (frame == null) return ;
      box = getBoundingBox() ;
      Point offset = frame.getPanelOffset() ;
      if (offset == null || box == null) return ;
      int x = offset.x + (int) (box.x * sf) ;
      int y = offset.y + (int) (box.y * sf) ;
      window.setLocation(x,y) ;
      if (g == null) return ;
      if (input) return ;
      
      // If we are not in input mode draw a temporary representation
      // of the video cel.

      String s = toString() ;
      int w = box.width ;
      int h = box.height ;
      FontMetrics metrics = g.getFontMetrics();
      int width = metrics.stringWidth(s);
      int height = metrics.getHeight();
      if (width > w || height > h) return ;
      g.setColor(Color.lightGray) ;
      g.fillRect(box.x,box.y,w,h) ;
      g.setColor(Color.black) ;
      g.drawString(s,box.x+w/2-width/2,box.y+h/2-height/2);
   }


	// Function to return the current pixel transparency at the
	// specified point.  This function returns -1 if the point is
	// outside the video window, or 255 (opaque) if the point is
   // inside the window.

	int getAlpha(int x, int y)
	{
		Dimension s = getSize() ;
		if (x < 0 || x >= s.width) return -1 ;
		if (y < 0 || y >= s.height) return -1 ;
      return 255 ;
   }


	// Function to return the current pixel at the specified point.  This
   // function returns -1 if the point is outside the cel. This returns
   // only the RGB value.

	int getRGB(int x, int y)
	{
      Dimension s = getSize() ;
		if (x < 0 || x >= s.width) return -1 ;
		if (y < 0 || y >= s.height) return -1 ;
      return 0 ;
   }


   // Function to scale the base image.
	// A zero scaling factor frees all scaled image allocations.

   synchronized void scaleImage(float scale)
   	throws KissException
	{
   	sf = scale ;
      if (scale == 0) sf = 1.0f ;
      scaled = (sf != 1.0f) ;

      // Calculate the scaled size.

      Dimension size = getBaseSize() ;
      scaledsize = new Dimension(size) ;
      int width = (int) (size.width * sf) ;
		int height = (int) (size.height * sf) ;

		// Adjust the window size.

		if (window == null) return ;
		window.setSize(width,height) ;
      MainFrame frame = Kisekae.getMainFrame() ;
      Point offset = frame.getPanelOffset() ;
      Rectangle box = getBoundingBox() ;
      int x = offset.x + (int) (box.x * sf) ;
      int y = offset.y + (int) (box.y * sf) ;
      window.setLocation(x,y) ;
      window.validate() ;
   }


	// Method to determine the media content type.

	ContentDescriptor getContentType(String filename)
	{
		int i = filename.lastIndexOf(".");
		String ext = (i > 0) ? filename.substring(i+1).toLowerCase() : "" ;
		String ct = "" ;

		if (ext.equals("viv"))
			ct = "video/vivo" ;
		else if (ext.equals("avi"))
			ct = "video/x-msvideo" ;
		else if (ext.equals("mpg"))
			ct = "video/mpeg" ;
		else if (ext.equals("mpeg"))
			ct = "video/mpeg" ;
		else if (ext.equals("mpv"))
			ct = "video/mpeg" ;
		else if (ext.equals("mov"))
			ct = "video/quicktime" ;
		else if (ext.equals("swf"))
			ct = "application/x-shockwave-flash" ;
		else if (ext.equals("spl"))
			ct = "application/futuresplash" ;
		ct = ContentDescriptor.mimeTypeToPackageName(ct);
		cd = new ContentDescriptor(ct);
		return cd;
	}

   
	// Function to display a syntax error message.

	private void showError(String s)
	{
   	errormessage = s ;
      MainFrame frame = Kisekae.getMainFrame() ;
		if (line > 0) s = "Line [" + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
      else frame.showStatus(errormessage) ;
		PrintLn.println(s) ;
	}

   
   public void actionPerformed(ActionEvent evt) 
   {
      Object source = evt.getSource() ;

      if ("Video Prefetch Callback".equals(evt.getActionCommand()))
      {
         if (!(source instanceof CallbackButton)) return ;
         CallbackButton b = (CallbackButton) source ;
         Object o = b.getParentObject() ;
         if (!(o instanceof Video)) return ;
         showComponent(show) ;
         return ;
      }
   }   

	// Duplicate clone.  This creates a new object where attributes
   // are the same as found in the original object.

   public Object clone()
   {
      if (ref == null) return null ;
      Vector v = ref.getMovies() ;
      Video cel = (Video) super.clone() ;
      cel.setActivated(false) ;
      cel.setPanel(panel) ;
      cel.setID(this.getID()) ;
      cel.setSize(this.getSize()) ;
      cel.setGroup(this.getGroup()) ;
      cel.setAttributes(this.getAttributes()) ;
      cel.setImported(this.isImported()) ;
      cel.setActivated(this.isActivated()) ;
      cel.setUpdated(true) ;
      v.addElement(cel) ;
      return cel ;
   }

   
   
   // Inner class to listen for Controller events.

   class VideoListener implements ControllerListener
   {
		public void controllerUpdate(ControllerEvent ce)
		{
      	if (player == null) return ;

			// RealizeCompleteEvent occurs after a realize() call.  The cel
			// visual window is created.

			if (ce instanceof RealizeCompleteEvent)
			{
				realized = true ;
	      	if (OptionsDialog.getDebugMovie())
					PrintLn.println("Video: " + getName() + " RealizeCompleteEvent") ;
           	try { startmovie() ; }
            catch (Exception e)
				{
					showError("Video " + getPath() + " start fault " + e.getMessage()) ;
					if (!(e instanceof KissException)) e.printStackTrace();
				}
			}

			// PrefetchCompleteEvent is generated when the player has finished
			// prefetching enough data to fill its internal buffers and is ready
	      // to start playing.

			else if (ce instanceof PrefetchCompleteEvent)
			{
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " PrefetchCompleteEvent") ;
            doPrefetchCallback() ;
			}


			// EndOfMediaEvent occurs when the media file has played till the end.
			// The player is now in the stopped state.

			else if (ce instanceof EndOfMediaEvent)
			{
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " EndOfMediaEvent") ;

				// Start the player again if repeating.

	         if (repeat)
	         {
		      	if (OptionsDialog.getDebugMovie())
						PrintLn.println("Video: " + getName() + " Repeat " + repeatcount) ;
					if (repeatcount > 0) repeatcount-- ;
					repeat = (repeatcount != 0) ;
					player.setMediaTime(new Time(0)) ;
               player.start() ;
	            return ;
	         }
				doStopCallback() ;

            // Close the visual window at the end of media.  A new play
            // request will create a new window for a realized player.

            player.stop() ;
	      }

			// If at any point the Player encountered an error - possibly in the
	      // data stream and it could not recover from the error, it generates
	      // a ControllerErrorEvent

			else if (ce instanceof ControllerErrorEvent)
			{
				error = true ;
	        	PrintLn.println("Video: " + getName() + " ControllerErrorEvent") ;
				showError("Unable to play media file " + file) ;
			}

			// The ControllerClosedEvent occurs when a player is closed.

			else if (ce instanceof ControllerClosedEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " ControllerClosedEvent") ;
	      }

			// DurationUpdateEvent occurs when the player's duration changes or is
			// updated for the first time

			else if (ce instanceof DurationUpdateEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " DurationUpdateEvent") ;
	      }

			// Caching control.

			else if (ce instanceof CachingControlEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " CachingControlEvent") ;
	      }

			// Start event.

			else if (ce instanceof StartEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " StartEvent") ;
				doStartCallback() ;
				started = true ;
            wasstarted = false ;
			}

			// Stop event.

			else if (ce instanceof StopEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " StopEvent") ;

				// Start the player in a new thread as player initiation can take
	         // time.  This frees the Player thread.

	         if (restart)
	         {
            	restart = false ;
					if (OptionsDialog.getDebugMovie())
               	PrintLn.println("Video: " + getName() + " Restart") ;
					Runnable runner = new Runnable()
					{ public void run() { play() ; } } ;
					javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
				}
				doStopCallback() ;
				started = false ;
	      }

			// Set Media Time event.

			else if (ce instanceof MediaTimeSetEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " MediaTimeEvent") ;
			}

			// Transition to new state event.

			else if (ce instanceof TransitionEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " TransitionEvent") ;
			}

			// Change play rate event.

			else if (ce instanceof RateChangeEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " RateChangeEvent") ;
			}

			// Change Stop Time event.

			else if (ce instanceof StopTimeChangeEvent)
	      {
	      	if (OptionsDialog.getDebugMovie())
	         	PrintLn.println("Video: " + getName() + " StopTimeChangeEvent") ;
	      }
		}
   }
}



