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
* Cel class
*
* Purpose:
*
* This is an abstract class to encapsulate a cel object.  A cel object
* contains the cel image, a position, and a group association.  The cel
* knows how to draw itself.
*
* Cel objects use their file name as their access key.
*
* Concrete types of cel objects are KissCel, GifCel and JpgCel.  Each of
* these has a different graphics file format and input/output requirements.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.util.Arrays ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Iterator ;
import java.util.Enumeration ;
import javax.swing.JOptionPane ;
import java.awt.geom.AffineTransform ;
import javax.imageio.* ;
import javax.imageio.stream.* ;


abstract class Cel extends KissObject
{
   // Class attributes.  Sized for 1000 cel objects.

   static private Hashtable key = new Hashtable(1200,0.855f) ;
   static private Component component = new Component() { } ;

   // Cel image attributes inherited by cel instances

   protected Image image = null ;				// The cel image
   protected Image baseimage = null ;			// The cel base image
   protected Image scaledimage = null ;		// The cel scaled image
   protected Image filteredimage = null ;		// The cel filtered image
   protected Dimension size = null ;			// The cel dimensions
   protected Dimension scaledsize = null ;  	// The cel scaled dimensions
   protected Point offset = null ;				// The cel offset
   protected Point baseoffset = null ;			// The cel image offset
   protected Point initialoffset = null ;		// The cel offset from cnf
   protected Point adjustedoffset = null ;	// The cel adjusted offset
   protected ColorModel cm = null ;				// The cel color model
   protected ColorModel basecm = null ;		// The cel original color model
   protected float sf = 1.0f ;					// The cel scale factor
   protected int imagewidth = 0 ;				// The cel image width
   protected int imageheight = 0 ;				// The cel image height
   protected int imagescaledwidth = 0 ;		// The cel image width
   protected int imagescaledheight = 0 ;		// The cel image height

   // Cel object attributes inherited by cel instances

   protected Object pid = null ;					// The cel palette identifier
   protected Object initpid = null ;			// The cel initial palette identifier
   protected Object mpid = null ;			  	// The cel fixed palette group identifier
   protected Object initmpid = null ;			// The cel initial palette group identifier
   protected Palette palette = null ;			// The cel palette
   protected Point location = null ;			// The cel location
   protected Color transparentcolor = null ;	// The cel transparent color
   protected Color backgroundcolor = null ;	// The cel background color
   protected String attributes = null ;      // The cel component attributes
   protected int usedcolors = 0 ;  			   // The cel colors in use
   protected int multipalette = 0 ;  			// The cel multipalette in use
   protected int transparency = 0 ;				// The cel transparency
   protected int inittransparency = 0 ;		// The cel initial transparency
   protected int transparentindex = -1 ;	  			// The cel transparent index
   protected int background = -1 ;	  			// The cel background index
   protected int maxloop = 0 ;					// The cel maximum loop count
   protected int loop = 0 ;						// The cel animation loop count

   // Cel object attributes maintained by this class

   private Object group = null ;					// The cel object group
   private Vector pages = null ;					// The cel pages it is on
   private Rectangle box = null ;				// The cel bounding box
   private Rectangle allbox = null ;			// The cel ambiguous bounding box
   private Point placement = null ;			   // The cel movement offset
   private Integer flex = null ;				  	// The cel flex value
   private Integer initflex = null ;			// The cel initial flex value
   private Integer level = null ;				// The cel level identifier
   private Color selectcolor = null ;			// The cel selection box color
   private Color markcolor = null ;			   // The cel marked selection color
   private Color flickercolor = null ;			// The cel flicker selection color
   private Vector celgroups = null ;			// The cel groups list
   private Vector pageleadspace = null ;		// The page format leading space
   private int commentleadspace = 0 ;			// The comment leading space
   private int line = 0 ;							// The configuration file line

   // Cel state attributes inherited by cel instances

   protected boolean loaded = false ;			// If true, data has been loaded
   protected boolean visible = true ;			// If true, show cel
   protected boolean ghost = false ;			// If true, cel cannot be caught
   protected boolean input = false ;			// If true, cel accepts input
   protected boolean truecolor = false ;		// If true, cel is truecolor
   protected boolean copy = false ;				// If true, cel is a copy
   protected boolean scaled = false ;			// If true, cel is scaled
   protected boolean allpages = false ;		// If true, cel on all pages
   protected boolean animate = false ;       // If true, cel can animate
   protected boolean initghost = false ;		// The cel initial ghost tag
   protected boolean initvisible = true ;		// The cel initial visibility
   protected boolean initinput = false ;		// The cel initial input state
   protected boolean ambiguous = true ;		// If false, no ambiguous cel
   protected boolean importascel = false ;	// If true, converted to cel

   // Cel tag update attributes

   private boolean updatetranstag = false ;  // If true, write %t tag
   private boolean updateghosttag = false ;  // If true, write %g tag



   // Constructor

   public Cel()
   {
      init() ;
      selectcolor = Color.green ;
      markcolor = Color.cyan ;
      flickercolor = Color.red ;
      transparency = 255 ;
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
      if (cid == null) cid = new String("Unknown") ;
      Enumeration e = key.keys() ;
      while (e.hasMoreElements())
      {
         String hashkey = (String) e.nextElement() ;
         if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
      }
   }


   // Function to find a Cel object by name.  For duplicate or 
   // ambiguous cels, we return the first cel that we find on the
   // current page.

   static Cel findCel(String name, Configuration c, FKissEvent event)
   {
      if (c == null) return null ;
      if (name == null || name.length() == 0) return null ;

      // See if we have a literal name.

      if (name.charAt(0) == '\"')
      {
         int i = name.lastIndexOf('\"') ;
         if (i < 1) return null ;
         name = name.substring(1,i) ;
      }

      // Otherwise, look for a variable name.

      else
      {
         Object o = c.getVariable().getValue(name,event) ;
         if (!(o instanceof String)) return null ;
         name = o.toString() ;
      }

      // Find the cel.  Watch for directory separator differences and 
      // imported cels.  For ambiguous cels, return the first occurance
      // on the active page.

      File f = new File(c.getDirectory(),name) ;
      String fname = f.getPath().toUpperCase() ;
      Cel c1 = findPageCel(fname,c) ;
      if (c1 != null) return c1 ;
      c1 = findPageCel("Import " + name,c) ;
      if (c1 != null) return c1 ;
      c1 = findPageCel("Import " + fname,c) ;
      if (c1 != null) return c1 ;
      if (fname.indexOf('\\') >= 0)
      {
         fname = fname.replace('\\','/') ;
         return findPageCel(fname,c) ;
      }
      else if (fname.indexOf('/') >= 0)
      {
         fname = fname.replace('/','\\') ;
         return findPageCel(fname,c) ;
      }
      return null ;
   }


   // Function to locate the next duplicate Cel by name.  If we have
   // previously verified the specified cel we clear its ambiguous flag
   // and can respond immediately.

   static Cel findNextCel(Cel cel, String name, Configuration c)
   {
      if (c == null) return null ;
      if (!cel.ambiguous) return null ;
      
      // See if we have a literal name.

      if (name.charAt(0) == '\"')
      {
         int i = name.lastIndexOf('\"') ;
         if (i < 1) return null ;
         name = name.substring(1,i) ;
      }

      // Find the cel.  Watch for directory separator differences and 
      // imported cels.

      File f = null ;
      name = name.toUpperCase() ;
      String s = c.getDirectory() ;
      if (s != null) s = s.toUpperCase() ;
      if (s != null && name.startsWith(s)) f = new File(name) ;
      else f = new File(c.getDirectory(),name) ;
      Cel c1 = (Cel) cel.getNextByKey(Cel.getKeyTable(),c.getID(),"Import "+name) ;
      if (c1 != null) return c1 ;
      name = (f != null) ? f.getPath().toUpperCase() : null ;
      c1 = (Cel) cel.getNextByKey(Cel.getKeyTable(),c.getID(),name) ;
      if (c1 != null) return c1 ;
      c1 = (Cel) cel.getNextByKey(Cel.getKeyTable(),c.getID(),"Import "+name) ;
      if (c1 != null) return c1 ;
      if (name.indexOf('\\') >= 0)
      {
         name = name.replace('\\','/') ;
         c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),c.getID(),name) ;
      }
      else if (name.indexOf('/') >= 0)
      {
         name = name.replace('/','\\') ;
         c1 = (Cel) Cel.getByKey(Cel.getKeyTable(),c.getID(),name) ;
      }
      
      if (c1 == null) cel.ambiguous = false ;
      return c1 ;
   }


   // Function to locate the next duplicate Cel by name.  We use this
   // to apply FKiSS actions (map, unmap, ...) to the first visible 
   // ambiguous cel if such cels exist. The Context Mapping option
   // must be set to activate this disambiguation.

   static private Cel findPageCel(String name, Configuration c)
   {
      if (c == null) return null ;
      Cel cel = (Cel) Cel.getByKey(Cel.getKeyTable(),c.getID(),name) ;
      if (cel == null) return null ;
      if (!OptionsDialog.getContextMap()) return cel ;
      if (!cel.hasDuplicateKey(Cel.getKeyTable(),c.getID(),name)) return cel ;
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf == null) return cel ;
      PanelFrame pf = mf.getPanel() ;
      if (pf == null) return cel ;
      PageSet page = pf.getPage() ;
      if (page == null) return cel ;
      Integer pid = (Integer) page.getIdentifier() ;
      if (pid == null) return cel ;
      Cel c1 = cel ;
      
      do
      {
         if (c1.isOnPage(pid)) return c1 ;
         c1 = findNextCel(c1,name,c) ;
      }
      while (c1 != null) ;
      return cel ;
   }



   // Object state change methods
   // ---------------------------

   // Set the cel group.  This is an Integer object before groups
   // are created.  It is a Group object once the group exists.

   void setGroup(Object o) { group = o ; }

   // Set the cel placement value.  This is (0,0) before groups are
   // created, then later becomes a reference to the group placement
   // term.  The placement is a relative offset from the cel's
   // current location and it is used to calculate the actual cel
   // location during mouse dragging.  When the cel is dropped, its
   // location is updated to reflect its new position.

   void setPlacement(Point p)
   { if (p != null) placement = p ; else placement = new Point() ; }

   void setPlacement(int x, int y)
   { placement.x = x ; placement.y = y ; }

   // Set the cel offset.  This is the relative point from the logical
   // location of the group where the cel is drawn.

   void setOffset(Point p) { offset = p ; }
   void setOffset(int x, int y) { offset.x = x ; offset.y = y ; }

   // Set the cel base offset.  This is the initial offset when the cel
   // is loaded if the cel contains offset values.

   void setBaseOffset(Point p) { baseoffset = p ; }

   // Set the cel adjusted offset.  This is the offset value if it is changed
   // through edit commands.

   void setAdjustedOffset(Point p) { adjustedoffset = p ; }

   // Set the cel initial offset.  This is the offset specification from the
   // cel configuration line.

   void setInitialOffset(int x, int y) { initialoffset.x = x ; initialoffset.y = y ; }
   void setInitialOffset(Point p) 
   { 
      if (p == null) p = new Point(0,0) ;
      initialoffset.x = p.x ; 
      initialoffset.y = p.y ;
   }

   // Set the cel visible flag.  If the cel becomes visible then
   // its group should become visible, too.  If this is the last cel
   // in the group to become invisible then the group becomes invisible,
   // too.  Collision events require visible group objects.

   void setVisible(boolean b)
   {
      visible = b ;
      Enumeration e = getEvents() ;
      while (e != null & e.hasMoreElements())
      {
         Vector v = (Vector) e.nextElement() ;
         if (v == null) continue ;
         for (int i = 0 ; i < v.size() ; i++)
            ((FKissEvent) v.elementAt(i)).setVisible(b,getName()) ;
      }
      if (!(group instanceof Group)) return ;
      if (visible)
         ((Group) group).setVisibility(true) ;
      else
      {
         Vector cels = ((Group) group).getCels() ;
         if (cels == null) return ;
         for (int i = 0 ; i < cels.size() ; i++)
            if (((Cel) cels.elementAt(i)).isVisible()) return ;
         ((Group) group).setVisibility(false) ;
      }
   }

   // Set the cel visible flag for all ambiguous cels

   void setVisible(boolean b, boolean all, KissObject primary)
   {
      setVisible(b) ;
       if (!all) return ;
      setAmbiguous("visible",new Boolean(b),primary) ;
   }

   // Invert the cel visible flag for all ambiguous cels

   void setAltVisible(boolean b, boolean all, KissObject primary)
   {
      setVisible(b) ;
      if (!all) return ;
      setAmbiguous("altvisible",new Boolean(b),primary) ;
   }

   // Method to set the cel visibility without updating the group visibility.
   // We use this if we need to make the cel temporarily visible for drawing.

   void setVisibility(boolean b) { visible = b ; }

   // Invert the cel visibility.

   void altVisible() { setVisible(!visible) ; }
   void altVisible(boolean all, Cel primary) { setAltVisible(!visible,all,primary) ; }

   // Set the cel ghost flag.

   void setGhost(boolean b) { ghost = b ; }
   void setGhost(boolean b, boolean all, KissObject primary) 
   {
      setGhost(b) ;
      if (!all) return ;
      setAmbiguous("ghost",new Boolean(b),primary) ;
   }

   // Set the cel input flag.

   void setInput(boolean b) { input = b ; }

   // Set the flex value.

   void setFlex(Integer f) { flex = f ; }

   // Set the initial flex value.

   void setInitFlex(Integer f) { initflex = f ; }

   // Set the draw level value.

   void setLevel(Integer n) { level = n ; }

   // Set the cel palette object.

   void setPalette(Palette p)
   {
      palette = p ;
      truecolor = (palette == null) ;
   }

   // Set the cel palette identifier value.

   void setPaletteID(Object p) { pid = p ; }

   // Set the cel fixed multipalette identifier value.

   void setPaletteGroupID(Object p) { mpid = p ; }

   // Set the cel pages vector.  This vector contains Integer PageSet
   // identifier values.

   void setPages(Vector v) { pages = v ; }

   // Set the indicator that the cel is on all pages.

   void setAllPages(boolean b) { allpages = b ; }

   // Set the page format leading space count.  This value is used during
   // formatted output of the colon page section to retain user spacing.

   void setPageLeadingSpace(Vector v) { pageleadspace = v ; }

   // Set the comment format leading space count.  This value is used during
   // formatted output of the colon page section to retain user spacing.

   void setCommentLeadingSpace(int n) { commentleadspace = n ; }

   // Set the cel component attributes.

   void setAttributes(String s) { attributes = s ; }
   void setInitAttributes(String s) { attributes = s ; }
   void resetAttributes() { }

   // Set the cel transparency value.  O is transparent, 255 is opaque.

   void setTransparency(int t) { transparency = t ; }
   void setTransparency(int t, boolean all, KissObject primary) 
   {
      setTransparency(t) ;
      if (!all) return ;
      setAmbiguous("transparency",new Integer(t),primary) ;
   }

   // Set the cel initial transparency value.  This is set on load or if
   // it is explicitly changed through the CelDialog function.

   void setInitTransparency(int t) { inittransparency = t ; }

   // Set the cel requested transparent color for cel types that do not
   // maintain transparency.  This is an index for cels with palettes or
   // an RGB value for truecolor cels.

   void setTransparentIndex(int c)
   {
      transparentindex = c ;
      Palette palette = getPalette() ;
      if (c < 0)
         transparentcolor = null ;
      else if (palette == null)
         transparentcolor = new Color(transparentindex) ;
      else
         transparentcolor = palette.getColor(multipalette,transparentindex) ;
   }

   // Set the cel requested background color.  This is an index for cels
   // with palettes or an RGB value for truecolor cels.

   void setBackgroundIndex(int c)
   {
      background = c ;
      Palette palette = getPalette() ;
      if (c < 0)
         backgroundcolor = null ;
      else if (palette == null)
         backgroundcolor = new Color(background) ;
      else
         backgroundcolor = palette.getColor(multipalette,background) ;
   }

   // Set the cel location.  This is biased by the offset.

   void setLocation(Point p) { location = new Point(p.x+offset.x,p.y+offset.y) ; }

   // The configuration file line number showing where this object was
   // first declared.  This is used for diagnostic output messages.

   void setLine(int l) { if (line == 0) line = l ; }

   // Set the cel frame image.  For ambiguous cels this maps the specified
   // cel in the ambiguity order and unmaps all others.

   void setFrame(int n)
   {
      int i = 0 ;
      String s = getPath() ;
      if (s != null) s = s.toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,s) ;
      while (c != null)
      {
         if (i == n) c.setVisible(true) ; else c.setVisible(false) ;
         c = (Cel) c.getNextByKey(Cel.getKeyTable(),cid,s) ;
         i++ ;
      }
      return ;
   }

   // Set the count of colors used in the cel.

   void setColorsUsed(int n) { usedcolors = n ; }

   // Set the cel copy indicator.

   void setCopy(boolean b) { copy = b ; }

   // Set the cel loop count.

   void setLoopCount(int n) { loop = n ; }

   // Set the cel loop count.

   void setLoopLimit(int n) { maxloop = n ; }

   // Set the cel animate flag.

   void setAnimate(int n) { setAnimate(n != 0) ; }

   // Set the cel animate flag.

   void setAnimate(boolean b)
   {
      animate = b ;
      if (b) setLoopCount(0) ;
   }
   
   void setAnimate(int n, boolean all, KissObject primary) 
   {
      setAnimate(n) ;
      if (!all) return ;
      setAmbiguous("animate",new Integer(n), primary) ;
   }

   // Set the cel background color.

   void setBackgroundColor(Color c) { backgroundcolor = c ; }

   // Set the cel transparent color.

   void setTransparentColor(Color c) { transparentcolor = c ; }

   // Set the cel size.  This is set to the image size on image load,
   // but the size can be set manually for video cels.

   void setSize(Dimension d)
   {
      if (scaled) scaledsize = d ; else size = d ;
      if (group instanceof Group)
         ((Group) group).updateBoundingBox(this) ;
   }

   // Set the image.  This updates the base image and the cel size.

   void setImage(Image img)
   {
      image = img ;
      baseimage = img ;
      filteredimage = null ;
      scaledimage = null ;
      if (this instanceof JavaCel) return ;
      Dimension d = new Dimension(0,0) ;
      if (img != null)
      {
         d.width = img.getWidth(null) ;
         d.height = img.getHeight(null) ;
         if (d.width < 0) d.width = 0 ;
         if (d.height < 0) d.height = 0 ;
      }
      imagewidth = d.width ;
      imageheight = d.height ;
      setSize(d) ;
   }

   // Set the image.  This updates the base image and the color model.

   void setImage(Image img, ColorModel newcm)
   {
      setImage(img) ;
      cm = basecm = newcm ;
   }

   // Set the cel loaded state.  This is required if we create a cel image.

   void setLoaded(boolean b) { loaded = b ; }

   // Set the cel imported as cel state.  This is required for cel write names.

   void setImportedAsCel(boolean b) { importascel = b ; }

   // Set the cel selection color.

   void setSelectColor(Color c) { selectcolor = c ; }

   // Set the cel marked selection color.

   void setMarkColor(Color c) { markcolor = c ; }

   // Set the cel flicker selection color.

   void setFlickerColor(Color c) { flickercolor = c ; }

   // Set the cel groups list.

   void setCelGroups(Vector v) { celgroups = v ; }

   // Set the cel initial transparency update flag.

   void setInitTransUpdated(boolean b) { updatetranstag = b ; }

   // Set the cel initial ghost update flag.

   void setInitGhostUpdated(boolean b) { updateghosttag = b ; }

   // Set the cel initial ghost value.  This is set on load or if
   // it is explicitly changed through the CelDialog function.

   void setInitGhost(boolean b) { initghost = b ; }

   // Set the cel initial visibility flag.

   void setInitVisible(boolean b) { initvisible = b ; }

   // Set the cel initial palette ID.

   void setInitPaletteID(Object o) { initpid = o ; }

   // Set the cel initial palette ID.

   void setInitPaletteGroupID(Object o) { initmpid = o ; }

   // Initialize the cel.  We need to create a new bounding box for
   // cloned cels.

   void init() 
   { 
      setUniqueID(new Integer(this.hashCode())) ;
      location = (location == null) ? new Point(0,0) : new Point(location) ;
      offset = (offset == null) ? new Point(0,0) : new Point(offset) ;
      initialoffset = (initialoffset == null) ? new Point(0,0) : new Point(initialoffset) ;
      baseoffset = (baseoffset == null) ? new Point(0,0) : new Point(baseoffset) ;
      placement = (placement == null) ? new Point(0,0) : new Point(placement) ;
      size = (size == null) ? new Dimension(0,0) : new Dimension(size) ;
      scaledsize = (scaledsize == null) ? new Dimension(0,0) : new Dimension(scaledsize) ;
      box = (box == null) ? new Rectangle(0,0) : new Rectangle(box) ; 
   }

   // A common function to set property values for all ambiguous cels.  To
   // access the consolidated bounding box, use the getAllBoundingBox() method.
   // To access all ambiguous cels we cycle through the list until we reach 
   // our first or primary cel object.

   private void setAmbiguous(String property, Object o, KissObject primary)
   {
      allbox = null ;
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf == null) return ; 
      Configuration config = mf.getConfig() ;
      if (config == null) return ;
      Cel c = findNextCel(this,getName(),config) ;
      if (c == null) c = (Cel) getByKey(getKeyTable(),getID(),getName().toUpperCase()) ;
      if (c == null) return ;
      
      // If we are using page context to disambiguate cels then do not apply
      // the attribute change if the cel is not on the current page.
      
      if (OptionsDialog.getContextMap() && c != primary)
      {
         PanelFrame pf = mf.getPanel() ;
         PageSet pageset = (pf != null) ? pf.getPage() : null ;
         Integer n1 = (pageset != null) ? (Integer) pageset.getIdentifier() : null ;
         Object o1 = c.getGroup() ;
         Group g = (o1 instanceof Group) ? (Group) o1 : null ;
         Integer n2 = (g != null) ? g.getContext() : null ;
         if (n1 != null && !(n1.equals(n2))) 
         {
            c.setAmbiguous(property,o,primary) ;
            return ;
         }
      }

      // If we have an ambiguous cel, apply property and recurse.
      
      if (c != primary) 
      {
         if ("visible".equals(property)) 
           c.setVisible(((Boolean) o).booleanValue(),true,primary) ;
         else if ("altvisible".equals(property)) 
            c.setVisible(!c.isVisible(),true,primary) ;
         else if ("ghost".equals(property)) 
            c.setGhost(((Boolean) o).booleanValue(),true,primary) ;
         else if ("transparency".equals(property)) 
            c.setTransparency(((Integer) o).intValue(),true,primary) ;
         else if ("animate".equals(property)) 
            c.setAnimate(((Integer) o).intValue(),true,primary) ;
      }

      // Recursion exit
      
      Rectangle bb = getBoundingBox() ;
      allbox = bb.union(c.getAllBoundingBox()) ;
   }



   // Object state reference methods
   // ------------------------------

   // Return the cel file size.

   int getBytes() { return 0 ; }

   // Return the cel pixel bit size.

   int getPixelBits() { return 0 ; }

   // Return the cel group object.

   Object getGroup()	{ return group ; }

   // Return the set of cel pages.

   Vector getPages()	{ return pages ; }

   // Return the maximum page number defined for this cel.

   int getMaxPage()
   {
      int maxpage = -1 ;
      if (pages == null) return maxpage ;
      for (int i = 0 ; i < pages.size() ; i++)
      {
         Integer n = (Integer) pages.elementAt(i) ;
         if (n.intValue() > maxpage) maxpage = n.intValue() ;
      }
      return maxpage ;
   }

   // Return the comment format leading space count.

   int getCommentLeadingSpace() { return commentleadspace ; }

   // Return the cel component attributes.

   String getAttributes() 
   { 
      if (attributes == null) return null ;
      return new String(attributes) ; 
   }

   // Return a reference to the current cel image.

   Image getImage()
   {
      if (filteredimage != null) return filteredimage ;
      if (scaled) return scaledimage ;
      return image ;
   }

   // Return a reference to the current cel image on the specified page.
   // We change the cel colors to match the requested page set default palette
   // and then restore the cel to its current colors.

   Image getImage(Integer page)
   {
      int mp = getMultiPalette() ;
      PageSet pageset = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,page) ;
      Integer newmultipalette = (pageset == null) ? null : pageset.getMultiPalette() ;
      changePalette(newmultipalette) ;
      Image img = getImage() ;
      changePalette(new Integer(mp)) ;
      return img ;
   }
   
   // Return the known image width and height
   
   int getImageWidth() { return (scaled) ? imagescaledwidth : imagewidth ; }
   int getImageHeight() { return (scaled) ? imagescaledheight : imageheight ; }

   // Return a reference to the cel base image.

   Image getBaseImage()
   { if (baseimage != null) return baseimage ; else return image ; }

   // Return a reference to the cel scaled image.

   Image getScaledImage() { return scaledimage ; }

   // Return the cel color model.

   ColorModel getColorModel() { return cm ; }

   // Return the base cel color model.

   ColorModel getBaseColorModel() { return basecm ; }

   // Return the current cel image size.

   Dimension getSize() { return (scaled) ? scaledsize : size ; }

   // Return the cel base image size.

   Dimension getBaseSize() { return size ; }

   // Return the cel scaled image size.

   Dimension getScaledSize() { return scaledsize ; }

   // Return the cel offset.

   Point getOffset() { return offset ; }

   // Return the cel initial offset from the configuration.

   Point getInitialOffset() { return initialoffset ; }

   // Return the cel base offset. This is the offset read from the image.

   Point getBaseOffset() { return baseoffset ; }

   // Return the cel movement offset. This is the displacement when dragging.

   Point getPlacement() { return placement ; }

   // Return the cel adjusted offset. This reflects any edit adjustments.

   Point getAdjustedOffset() 
   { 
      if (adjustedoffset != null) return adjustedoffset ;
      return new Point(initialoffset.x+baseoffset.x,initialoffset.y+baseoffset.y) ;
   }

   // Return a new instance of the cel adjusted offset point.

   Point getAdjustedOffsetPoint() 
   { 
      if (adjustedoffset == null) return null ;
      return new Point(adjustedoffset) ;
   }

   // Return the cel flex value.

   Integer getFlex() { return flex ; }

   // Return the cel initial flex value.

   Integer getInitFlex() { return initflex ; }

   // Return the cel initial input state.

   boolean getInitInput() { return initinput ; }

   // Return the cel initial palette ID.

   Object getInitPaletteID() { return initpid ; }

   // Return the cel initial fixed multipalette.

   Object getInitPaletteGroupID() { return initmpid ; }

   // Return the cel drawing level.

   Integer getLevel() { return (level == null) ? new Integer(0) : level ; }

   // Return the cel location.  This is biased by the offset.

   Point getLocation() { return new Point(location.x-offset.x,location.y-offset.y) ; }

   // Return the configuration file source line number.

   int getLine() { return line ; }

   // Return the cel multipalette in use.

   int getMultiPalette() { return multipalette ; }

   // Return the cel transparency value.

   int getTransparency() { return transparency ; }

   // Return the cel initial loaded transparency value.

   int getInitTransparency() { return inittransparency ; }

   // Get the cel requested transparent color for cel types that do not
   // maintain transparency.  This is an index for cels with palettes or
   // an RGB value for truecolor cels.

   int getTransparentIndex() { return transparentindex ; }

   // Return the cel transparent color.

   Color getTransparentColor() { return transparentcolor ; }

   // Return the cel palette object.

   Palette getPalette() { return palette ; }

   // Return the cel palette identifier object.

   Object getPaletteID() { return pid ; }

   // Return the cel palette group identifier object.

   Object getPaletteGroupID() { return mpid ; }

   // Return the cel animate flag.

   boolean getAnimate() { return animate ; }

   // Return the number of frames in the cel.  For ambiguous cels this
   // is the number of duplicate cels.

   int getFrameCount()
   {
      int i = 0 ;
      String s = getPath() ;
      if (s != null) s = s.toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,s) ;
      while (c != null)
      {
         i++ ;
         c = (Cel) c.getNextByKey(Cel.getKeyTable(),cid,s) ;
      }
      return i ;
   }

   // Return the current frame number in the cel.  For ambiguous cels this
   // is the sequence number for the current ambiguity.  Frame numbers
   // begin from 0.

   int getFrame() { return getFrame(this) ; }
   int getFrame(Cel cel)
   {
      int i = 0 ;
      String s = getPath() ;
      if (s != null) s = s.toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,s) ;
      while (c != null)
      {
         if (c == cel) break ;
         c = (Cel) c.getNextByKey(Cel.getKeyTable(),cid,s) ;
         i++ ;
      }
      if (c == null) return -1 ;
      return i ;
   }

   // Return the count of the colors used in the cel.

   int getColorsUsed() { return usedcolors ; }

   // Return the set of frames in the cel.

   Vector getFrames() { return null ; }

   // A method to return this cel.

   Cel getCel(int i) { return (i == 0) ? this : null ; }

   // Return the cel size.

   int getCelCount() { return 1 ; }

   // Return the number of cel groups this cel participates in.

   int getCelGroupCount() { return (celgroups == null) ? 0 : celgroups.size() ; }

   // Return the cel groups list.

   Vector getCelGroups() { return celgroups ; }

   // A method to return a celgroup from this cel group.

   CelGroup getCelGroup(int i)
   {
      if (celgroups == null) return null ;
      if (i < 0 || i >= celgroups.size()) return null ;
      Object o = celgroups.elementAt(i) ;
      if (!(o instanceof CelGroup)) return null ;
      return (CelGroup) o ;
   }

   // Get the cel background color index.

   int getBackgroundIndex()
   {
      if (palette == null) return background ;
      return palette.getBackgroundIndex() ;
   }

   // Return the cel background color.

   Color getBackgroundColor() { return backgroundcolor ; }

   // Return the maximum animation loop count.

   int getLoopLimit() { return maxloop ; }

   // Return the actual animation loop count.

   int getLoopCount() { return loop ; }

   // Return the cel image scaling factor.

   float getScaleFactor() { return sf ; }

   // Return the cel initial loaded transparency value.

   boolean getInitGhost() { return initghost ; }

   // Return the cel initial visibility value.

   boolean getInitVisible() { return initvisible ; }

   // Return the cel bounding box.

   Rectangle getBoundingBox()
   {
      box.x = location.x + placement.x ;
      box.y = location.y + placement.y ;
      if (scaled)
      {
         box.width = scaledsize.width ;
         box.height = scaledsize.height ;
      }
      else
      {
         box.width = size.width ;
         box.height = size.height ;
      }
      return box ;
   }

   // Return the consolidated ambiguous cel bounding box. This is
   // established for property changes that apply to all ambiguous
   // cels that require a redraw.

   Rectangle getAllBoundingBox()
   {
      if (allbox == null) return getBoundingBox() ;
      if (!OptionsDialog.getAllAmbiguous()) return getBoundingBox() ;
      return allbox ;
   }

   // Return the cel base bounding box.  This is the bounding box and excludes
   // any moving placement offset.

   Rectangle getBaseBoundingBox()
   {
      Rectangle r = getBoundingBox() ;
      r.x = location.x ;
      r.y = location.y ;
      return r ;
   }

   // Get the visible cel bounding box. It is relative to location (0,0).

	Rectangle getVisibleBoundingBox(Integer page)
   {
		Rectangle r = new Rectangle(offset) ;
      if (!visible) return r ;
      if (!isOnPage(page)) return r ;
      Dimension s = getSize() ;
      r.width = s.width ;
      r.height = s.height ;
      return r ;
  }

   // Return the initial object location on the specified page.

   Point getInitialLocation(PageSet page)
   {
      Point p = new Point(0,0) ;
      if (page == null) return p ;
      if (!(group instanceof Group)) return p ;
      Object identifier = ((Group) group).getIdentifier() ;
      if (!(identifier instanceof Integer)) return p ;
      Point location = page.getInitialGroupPosition((Integer) identifier) ;
      if (location == null) return p ;
      return location ;
   }
   
   // Get the visible cel count on the specified page.

	int getVisibleCelCount(Integer page)
	{
      if (OptionsDialog.getMapCount())
      {
         if (isOnPage(page) && isVisible()) return 1 ;
      }
      else
      {
         if (isVisible()) return 1 ;
      }
      return 0 ;
	}

	// Return the requested event. This is a list of all events by identifier.
   // Collision events are returned only when all parameters are visible.

	Vector getEvent(Object o) 
   { 
      Vector cv = null ;
      Vector gv = super.getEvent(o) ; 
      if (gv == null) return null ;
		if (!("apart".equals(o) || "collide".equals(o))) return gv ;
      
      // Collision events are only valid if visible.
      
      for (int i = 0 ; i < gv.size() ; i++)
      {
         FKissEvent evt = (FKissEvent) gv.elementAt(i) ;
         if (!evt.isVisible()) continue ;
         if (cv == null) cv = new Vector() ;
         cv.add(evt) ;
      }
      return cv ;
   }

	// Method to return the name of the file as written.  Cels can be
   // encoded and written in a different format. For ambiguous cels, 
   // we return the primary cel name.
   
	String getWriteName() 
   { 
      if (!copy) 
      {
         String name = super.getWriteName() ;
         if (this instanceof Video) return name ;
         if (isImported() && isImportedAsCel())
         {
            int n = name.lastIndexOf('.') ;
            if (n >= 0) name = name.substring(0,n) + ".cel" ;
         }
         return name ; 
      }
      
      // Ambiguous cel
      
      String path = getPath().toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,path) ;
      while (c != null)
      {
         if (!c.isCopy()) return c.getWriteName() ;
         c = (Cel) c.getNextByKey(c.getKeyTable(),cid,path) ;
      }
      return getName() ;
   }

   // Return an indication if the cel is on the specified page.

   boolean isOnPage(Integer p)
   {
      if (error) return false ;
      if (p == null) return false ;
      if (pages == null) return true ;
      return pages.contains(p) ;
   }

   // Return an indication if the cel is on all pages.

   boolean isOnAllPage() { return allpages ; }

   // Return an indication if the cel is visible.

   boolean isVisible() { return (error) ? false : visible ; }

   // Return an indication if the cel can be caught.

   boolean isGhosted() { return (error) ? false : ghost ; }

   // Return an indication if the cel is enabled.

   boolean isEnabled() { return true ; }

   // Return the cel truecolor indicator.

   boolean isTruecolor() { return truecolor ; }

   // Return the cel image copy indicator.

   boolean isCopy() { return copy ; }

   // Return an indication if the data has been loaded.

   boolean isLoaded() { return loaded ; }

   // Return an indication if the image was imported as cel.

   boolean isImportedAsCel() { return importascel ; }

   // Return the cel input state.

   boolean isInput() { return input ; }

   // Return the default writable offset state.

   boolean isWriteableOffset() { return false ; }

   // Return a writable file indicator.

   boolean isWritable() { return (!error && !isInternal() && isLoaded()) ; }

   // Return an indicator if the cel is scaled.

   boolean isScaled() { return scaled ; }

   // Return an indicator if the cel has a local palette.

   boolean isLocalPalette() { return true ; }

   // Return an indicator if the cel initial transparency has changed.

   boolean isInitTransUpdated() { return updatetranstag ; }

   // Return an indicator if the cel initial ghost tag has been changed.

   boolean isInitGhostUpdated() { return updateghosttag ; }

   // Return an indicator if the cel has a palette.

   boolean hasPalette() { return (pid != null) ; }

   // Return an indicator if the cel image contains a translucent pixel.

   boolean isTranslucent()
   {
      int rgb = 0 ;
      int w = size.width ;
      int h = size.height ;
      int pixels[] = new int[w*h] ;
      PixelGrabber pg = new PixelGrabber(image,0,0,w,h,pixels,0,w) ;
      try { pg.grabPixels() ; }
      catch (InterruptedException e) { return false ; }
      for (int i = 0 ; i < w*h ; i++)
      {
         int a = pixels[i] >> 24 ;
         if (a > 0 && a < 255) return true ;
      }
      return false ;
   }
   
   // Return an indicator if the cel image is buffered.

   boolean isBufferedImage()
   {
      if (image == null) return false ;
      return (image instanceof BufferedImage) ;
   }
   



   // Object utility methods
   // ----------------------

   // Move the cel to a new position.  The placement is a relative
   // offset from the cel's current position.

   void move(int x, int y)
   {
      placement.x = x ;
      placement.y = y ;
   }

   // Drop the cel in a new location.

   void drop()
   {
      location.x += placement.x ;
      location.y += placement.y ;
   }

   // Reset the cel to its initial state.  This restores cel visibility,
   // ghost state, transparency state, visibility state and z-order level.
   // If we were imported then we have not been saved and we must retain
   // a loaded state.

   void reset()
   {
      super.reset() ;
      loop = 0 ;
      visible = true ;
      animate = false ;
      loaded = isImported() || isUpdated() ;
      ghost = initghost ;
      setVisible(initvisible) ;
      setLevel((Integer) getIdentifier()) ;
      setPaletteGroupID(getInitPaletteGroupID()) ;
      if (pid != initpid && initpid instanceof Integer)
      {
         changePaletteID((Integer) initpid) ;
      }
      if (transparency != inittransparency)
      {
         transparency = inittransparency ;
         changeTransparency(0) ;
      }
   }

   // Reset the cel to animation state.

   void resetAnimation() { loop = 0 ; }

   // Save the cel state.  The current state is given a name
   // for later reference when it needs to be restored.

   void saveState(Object cid, Object state)
   {
      State sv = new State(cid,this,state,9) ;
      sv.variable[0] = new Point(location) ;
      sv.variable[1] = new Boolean(visible) ;
      sv.variable[2] = new Integer((flex == null) ? -1 : flex.intValue()) ;
      sv.variable[3] = new Integer(transparency) ;
      sv.variable[4] = new Boolean(ghost) ;
      sv.variable[5] = new Point(offset) ;
      sv.variable[6] = new Integer((level == null) ? 0 : level.intValue()) ;
      sv.variable[7] = new Integer((pid instanceof Integer) ? ((Integer) pid).intValue() : -1 ) ;
      sv.variable[8] = new Integer((mpid instanceof Integer) ? ((Integer) mpid).intValue() : -1 ) ;
   }

   // Restore the required cel state.  Note: If we do not restore the
   // cel visibility then events that map and unmap cels in one page
   // set will be reflected in a new page set.

   void restoreState(Object cid, Object state) { restoreState(cid,state,false) ; }
   void restoreState(Object cid, Object state, boolean restorevisibility)
   {
      State sv = (State) State.getByKey(cid,this,state) ;
      if (sv != null)
      {
         location.x = ((Point) sv.variable[0]).x ;
         location.y = ((Point) sv.variable[0]).y ;
         flex = ((Integer) sv.variable[2]) ;
         offset = (Point) sv.variable[5] ;
         level = ((Integer) sv.variable[6]) ;
         if (flex.intValue() < 0) flex = null ;
         
         // Restore visibility state
         
         if (restorevisibility)
         {
            visible = ((Boolean) sv.variable[1]).booleanValue() ;
            ghost = ((Boolean) sv.variable[4]).booleanValue() ;
         }
         
         // Restore palette file and fixed palette group
         
         int p = ((Integer) sv.variable[7]).intValue() ;
         int mp = ((Integer) sv.variable[8]).intValue() ;
         if (!sv.variable[7].equals(pid) || !sv.variable[8].equals(mpid))
         {
            pid = (p >= 0) ? sv.variable[7] : null ; 
            mpid = (mp >= 0) ? sv.variable[8] : null ; 
            changePalette((mpid instanceof Integer) ? (Integer) mpid : new Integer(multipalette)) ;
         }
         
         // Restore transparency
         
         int t = ((Integer) sv.variable[3]).intValue() ;
         if (t != transparency)
         {
            transparency = t ;
            changeTransparency(0) ;
         }
      }
      else if ("initial".equals(state))
      {
         location.x = offset.x ;
         location.y = offset.y ;
         offset.x = 0 ;
         offset.y = 0 ;
      }
   }


   // Return an output image encoder depending on the file type.

   ImageEncoder getEncoder(FileWriter fw, OutputStream out, String type) throws IOException
   {
      boolean b = false ;
      if (type == null) type = extension ;
      type = type.toLowerCase() ;
      Point celoffset = getBaseOffset() ;
      if (".cel".equals(type)) b = KissCel.getWriteableOffset() ;
      else if (".bmp".equals(type)) b = BmpCel.getWriteableOffset() ;
      else if (".gif".equals(type)) b = GifCel.getWriteableOffset() ;
      else if (".jpg".equals(type)) b = JpgCel.getWriteableOffset() ;
      else if (".png".equals(type)) b = PngCel.getWriteableOffset() ;
      else if (".ppm".equals(type)) b = PpmCel.getWriteableOffset() ;
      else if (".pbm".equals(type)) b = PpmCel.getWriteableOffset() ;
      else if (".pgm".equals(type)) b = PpmCel.getWriteableOffset() ;
      if (OptionsDialog.getWriteCelOffset() && b)
         if (!hasDuplicateKey(getKeyTable(),cid,getPath().toUpperCase()))
            celoffset = getAdjustedOffset() ;

      // If the image has changed we must apply current colors and transparency.
      // Cel size changes encode the scaled image.

      Image img = getBaseImage() ;
      if (img == null) return null ;
      if (scaled && sf == 1.0f) img = getScaledImage() ;
      if (img == null) return null ;

      // Return the appropriate encoder.

      if (".bmp".equals(type))
         return new BmpEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".gif".equals(type))
         return new GifEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".jpg".equals(type))
         return new JpgEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".png".equals(type))
         return new PngEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".cel".equals(type))
         return new KissEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".ppm".equals(type))
         return new PpmEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".pbm".equals(type))
         return new PpmEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      if (".pgm".equals(type))
         return new PpmEncoder(fw,img,getPalette(),getMultiPalette(),
            celoffset,getTransparentColor(),getBackgroundColor(),
            getTransparency(),out) ;
      return null ;
   }


   // Return a new cel object depending on the file type.

   static Cel createCel(ArchiveFile zip, String name)
   { return createCel(zip,name,null) ; }

   static Cel createCel(ArchiveFile zip, String name, Configuration ref)
   {
      int i = (name == null) ? -1 : name.lastIndexOf('.') ;
      if (i < 0) return null ;
      String ext = name.substring(i).toLowerCase() ;
      if (".bmp".equals(ext)) return new BmpCel(zip,name,ref) ;
      if (".gif".equals(ext)) return new GifCel(zip,name,ref) ;
      if (".jpg".equals(ext)) return new JpgCel(zip,name,ref) ;
      if (".png".equals(ext)) return new PngCel(zip,name,ref) ;
      if (".cel".equals(ext)) return new KissCel(zip,name,ref) ;
      if (".ppm".equals(ext)) return new PpmCel(zip,name,ref) ;
      if (".pbm".equals(ext)) return new PpmCel(zip,name,ref) ;
      if (".pgm".equals(ext)) return new PpmCel(zip,name,ref) ;
      if (".mpg".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".mpv".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".avi".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".mov".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".viv".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".swf".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }
      if (".spl".equals(ext))
         { if (Kisekae.isMediaInstalled()) return new Video(zip,name,ref) ; }

      // Components.

      if (ArchiveFile.isComponent(name)) return new JavaCel(ext.substring(1),name,ref) ;
      return null ;
   }


   // A function to format a cel declaration.  Internal cels are not written.
   // The cel name is relative to the configuration directory.  Cel page
   // associations are written only if the group is contained on the page.
   // This function will use tabstop settings from the configuration to build
   // constant sized fields if such settings exist.  Otherwise, fields are
   // set to their actual size and delimited with tab characters.  A tab
   // character is always placed at the end of the formatted string.

   String formatCel(Configuration config)
   {
      Cel cel = this ;
      if (cel.isInternal()) return null ;
      Group g = (Group) cel.getGroup() ;
      if (g == null) return null ;
      Integer flex = cel.getFlex() ;
      String name = cel.getWriteName() ;
      Object pid = cel.getInitPaletteID() ;
      Object mpid = cel.getInitPaletteGroupID() ;
      Vector pages = cel.getPages() ;
      int transparency = cel.getTransparency() ;
      boolean ghost = cel.isGhosted() ;
      Vector celgroups = cel.getCelGroups() ;
      int celgroupcount = (celgroups == null) ? 0 : celgroups.size() ;
      Palette p = (Palette) Palette.getByKey(Palette.getKeyTable(),getID(),pid) ;
      int id = (pid instanceof Integer) ? ((Integer) pid).intValue() : 0 ;
      if (p == null || p.isInternal()) id = -1 ;

      // The cel name is relative to the configuration directory name.
      // Configurations loaded from an archive do not have a directory.
      // Files loaded from an archive have relative names set by the
      // archive path field.  Imported files have their relative name
      // set to the configuration relative name when the cel is created.

      String path = convertSeparator(cel.getPath()) ;
      String relativename = name ;
      String directory = (config == null) ? null : config.getDirectory() ;
      if (directory != null)
      {
         if (path.startsWith(directory))
            relativename = path.substring(directory.length()) ;
         if (relativename.startsWith(File.separator))
            relativename = relativename.substring(File.separator.length()) ;
      }
      
      // If we are writing a component cel then our cel name may have changed.
      // This can happen if we were importing components as CEL files.
      
      if (cel instanceof JavaCel) relativename = cel.getWriteName() ;
      if (relativename.indexOf(' ') > 0) relativename = "\"" + relativename + "\"" ;

      // Compose and write the output line.  Tab stops delimit fields.
      // Fields are space filled to the size specified in the tabstops array.

      int [] tabstops = (config == null) ? null : config.getCelTabStops() ;
      StringBuffer sb = new StringBuffer("#") ;
      Object o = g.getIdentifier() ;
      if (o == null) return null ;
      sb.append(o.toString()) ;
      if (initflex != null) sb.append("." + initflex.toString()) ;
      sb.append(' ') ;
      if (tabstops != null && tabstops[0] > 0)
         for (int i = sb.length() ; i < tabstops[0] ; i++) sb.append(' ') ;

      // Cel relative name field.

      sb.append("\t") ;
      sb.append(relativename) ;
      sb.append(' ') ;
      if (tabstops != null && tabstops[1] > 0)
         for (int i = sb.length() ; i < tabstops[1] ; i++) sb.append(' ') ;

      // Cel palette field.

      sb.append("\t") ;
      if (id >= 0)
      {
         sb.append("*") ;
         sb.append(id) ;
         if (mpid instanceof Integer)
         {
            sb.append(".") ;
            sb.append(mpid) ;
         }
      }
      sb.append(' ') ;
      if (tabstops != null && tabstops[2] > 0)
         for (int i = sb.length() ; i < tabstops[2] ; i++) sb.append(' ') ;

      // Cel pages are written as found in the page list, except
      // for the case where the configuration has been updated and
      // the cel group no longer exists on the page.  If the cel is
      // defined to be on all pages the colon indicator is not written.

      sb.append("\t") ;
      if (pages != null && !allpages)
      {
         sb.append(":") ;
         for (int i = 0 ; i < pages.size() ; i++)
         {
            o = pages.elementAt(i) ;
            if (!(o instanceof Integer)) continue ;
            int n = ((Integer) o).intValue() ;
            if (config.isUpdated())
            {
               PageSet page = config.getPage(n) ;
               if (page == null || !page.contains(g)) continue ;
            }

            // Insert page number lead spaces to retain format compatibility
            // with the original input file.  The spacing was retained when
            // the configuration was read.  The default lead spacing is one.

            try
            {
               Object o2 = (pageleadspace != null) ? pageleadspace.elementAt(n) : null ;
               n = (o2 instanceof Integer) ? ((Integer) o2).intValue() : -1 ;
               if (n < 0 || (i > 0 && n == 0)) n = 1 ;
            }
            catch (ArrayIndexOutOfBoundsException e) { n = 1 ; }
            for (int j = 0 ; j < n ; j++) sb.append(' ') ;
            sb.append(o) ;
         }
      }
      sb.append(' ') ;
      if (tabstops != null && tabstops[3] > 0)
         for (int i = sb.length() ; i < tabstops[3] ; i++) sb.append(' ') ;

      // Final field terminator.

      sb.append("\t") ;
      return sb.toString() ;
   }

   
   // A function to build a cel comment string.  This function writes the
   // existing cel transparency, size and offset settings for cels that
   // do not maintain their own values as part of the cel file.

   String buildComment()
   {
      String comment = getComment() ;
      StringBuffer newcomment = new StringBuffer(";") ;
      
      // Erase all string literals in the comment.

      StringBuffer sb = new StringBuffer(comment) ;
      boolean inliteral = comment.startsWith("\"") ;
      for (int i = 1 ; i < sb.length() ; i++)
      {
         if (sb.charAt(i) == '"' && sb.charAt(i-1) != '\\') 
            inliteral = !inliteral ;
         else if (inliteral && sb.charAt(i) != '\\') 
            sb.replace(i,i+1," ") ;
      }

      // %t transparency specification.

      comment = sb.toString() ;
      int i = comment.indexOf("%t") ;
      int n = getInitTransparency() ;
      if (n < 0) n = 0 ;
      if (n > 255) n = 255 ;
      if ((i >= 0 || isInitTransUpdated()) && n != 255)
      	newcomment.append("%t" + (255-n) + " ") ;

      // %g ghost specification.

      i = comment.indexOf("%g") ;
      boolean b = getInitGhost() ;
      if ((i >= 0 || isInitGhostUpdated()) && b)
      	newcomment.append("%g" + " ") ;

      // %u unmap specification.

      i = comment.indexOf("%u") ;
      if (i >= 0)
      	newcomment.append("%u" + " ") ;

      // %c transparent color specification. This color must be specified
      // for GIF87, BMP, JPG, and PPM type cels.

      i = comment.indexOf("%c") ;
      Object o = OptionsDialog.getExportType() ;
      if (!isExported()) o = null ;
      if (this instanceof BmpCel) i = 0 ;
      if (this instanceof JpgCel) i = 0 ;
      if (this instanceof PpmCel) i = 0 ;
      if (this instanceof GifCel && "87".equals(((GifCel) this).getVersion())) i = 0 ;
      if ("BMP".equals(o)) i = 0 ;
      if ("JPG".equals(o)) i = 0 ;
      if ("PPM".equals(o)) i = 0 ;
      if ("PGM".equals(o)) i = 0 ;
      if ("PBM".equals(o)) i = 0 ;
      n = getTransparentIndex() ;
      Color transparentcolor = getTransparentColor() ;
      int rgb = (transparentcolor != null) ? transparentcolor.getRGB() : n ;
      rgb = rgb & 0x00ffffff ;
      if (i >= 0 && n >= 0)
      	newcomment.append("%c" + rgb + " ") ;

      // %size[w,h] specification.

      i = comment.indexOf("%size[") ;
      Dimension d = getBaseSize() ;
      if ((i >= 0 || this instanceof JavaCel || this instanceof Video) && d != null)
      	newcomment.append("%size[" + d.width + "," + d.height + "] ") ;

      // %offset[x,y] specification.

      i = comment.indexOf("%offset[") ;
      Point p1 = getAdjustedOffset() ;
      Point p2 = getBaseOffset() ;
      b = isWriteableOffset() ;
      
      // Offsets for exported cels are the current adjusted offset. Our export
      // type will determine if offsets should be represented on the cel line.
      
      o = OptionsDialog.getExportType() ;
      if (isExported() && o != null)
      {
         String type = o.toString().toLowerCase() ;
         if ("cel".equals(type)) b = KissCel.getWriteableOffset() ;
         else if ("bmp".equals(type)) b = BmpCel.getWriteableOffset() ;
         else if ("gif".equals(type)) b = GifCel.getWriteableOffset() ;
         else if ("jpg".equals(type)) b = JpgCel.getWriteableOffset() ;
         else if ("png".equals(type)) b = PngCel.getWriteableOffset() ;
         else if ("ppm".equals(type)) b = PpmCel.getWriteableOffset() ;
         else if ("pbm".equals(type)) b = PpmCel.getWriteableOffset() ;
         else if ("pgm".equals(type)) b = PpmCel.getWriteableOffset() ;
         if (!hasDuplicateKey(Cel.getKeyTable(),cid,getPath().toUpperCase())) 
            p2 = new Point(0,0) ;
      }
      
      // Show the offset if it cannot be written in the file and the cel
      // is not duplicated. 
      
      if (p1.x != p2.x || p1.y != p2.y)
         if (!(OptionsDialog.getWriteCelOffset() && b &&
               !hasDuplicateKey(Cel.getKeyTable(),cid,getPath().toUpperCase()))) 
         {
            if (OptionsDialog.getXYOffsets()) 
         	   newcomment.append("%x" + (p1.x-p2.x) + " %y" + (p1.y-p2.y) + " ") ;
            else
         	   newcomment.append("%offset[" + (p1.x-p2.x) + "," + (p1.y-p2.y) + "] ") ;
         }

      // %attributes[a1,a2,...] specification.

      i = comment.indexOf("%attributes[") ;
      String s = getAttributes() ;
      if ((i >= 0 && s != null) || s != null)
      	newcomment.append("%attributes[" + s + "] ") ;

      // !group specification. Ensure a space exists between the group name
      // and any frame set specification as some viewers (DirectKiss) 
      // require this.

      i = comment.indexOf("!") ;
      if (i >= 0)
      {
         String s1 = comment.substring(i) ;
         int j = s1.indexOf(':') ;
         if (j > 0 && s1.charAt(j-1) != ' ')
            s1 = s1.substring(0,j) + " " + s1.substring(j) ;
      	newcomment.append(s1) ;
         return newcomment.toString() ;
      }

      // Add the base comment and lead spacing.

      n = getCommentLeadingSpace() ;
      for (i = 0 ; i < n ; i++) newcomment.append(' ') ;
      newcomment.append(getBaseComment()) ;
      return newcomment.toString() ;
   }



   // Abstract cel methods.
   // ----------------------

   // Load the cel file.  This method creates an input stream to
   // read the cel pixels from the compressed zip file.

   void load() { load(null) ; }
   abstract void load(Vector includefiles) ;
   abstract void loadCopy(Cel c) ;

   // Unload the cel file.  This releases our image allocation.

   void unload()
   {
      copy = false ;
      loaded = false ;
      scaled = false ;
      if (image != null) image.flush();
      if (baseimage != null) baseimage.flush();
      if (scaledimage != null) scaledimage.flush();
      image = baseimage = scaledimage = null ;
      if (OptionsDialog.getDebugLoad())
         System.out.println("Unload: " + toString());
   }


   // Method to write our file contents to the specified output stream.

   int write(FileWriter fw, OutputStream out) throws IOException
   { return write(fw,out,extension) ; }

   abstract int write(FileWriter fw, OutputStream out, String type) throws IOException ;

   // Draw the cel at its current position, constrained by the
   // defined bounding box.  We draw the cel only if is is visible
   // and intersects our drawing area.

   abstract void draw(Graphics g, Rectangle box) ;




   // Object graphics methods
   // -----------------------

   // Method to add a filter to the cel image for multipalette changes.
   // The default behaviour is to do nothing.  This method should be
   // overridden for cels that can change their color palette.

   void changePalette(Integer newpalette) { return ; }


   // Method to change the cel transparency.  The default behaviour is
   // to do nothing.  This method should be overridden for cels that
   // can change their transparency.

   void changeTransparency(int t, boolean bounded, boolean ambiguous) { return ; }


   // Method to fix the cel multipalette group.  This is done through 
   // the cel CNF configuration or through a setpal() command.  A cel
   // with a fixed palette group does not participate in color set changes
   // to a different palette group.

   void fixPaletteGroup(Integer n) 
   { 
      setPaletteGroupID(n) ;
      changePalette(n) ; 
   }


   // Function to return the current pixel transparency at the
   // specified point.  This function returns -1 if the point is
   // outside the cel or a cel color model is not defined.

   int getAlpha(int x, int y)
   {
      if (cm == null) return -1 ;
      Image img = getImage() ;
      if (img == null) return -1 ;
      Dimension s = getSize() ;
      if (x < 0 || x >= s.width) return -1 ;
      if (y < 0 || y >= s.height) return -1 ;
      float sf1 = (scaled) ? sf : 1.0f ;

      // Get the image pixel.

      x = (int) (x * sf1) ;
      y = (int) (y * sf1) ;
      int pixels[] = new int[1] ;
      PixelGrabber pg = new PixelGrabber(img,x,y,1,1,pixels,0,s.width) ;
      try { pg.grabPixels() ; }
      catch (InterruptedException e) { return -1 ; }
      return ((pixels[0] >> 24) & 255) ;
   }


   // Function to return the current pixel at the specified point.  This
   // function returns -1 if the point is outside the cel or a cel color
   // model is not defined.  This returns only the RGB value.

   int getRGB(int x, int y)
   {
      if (cm == null) return -1 ;
      Image img = getImage() ;
      if (img == null) return -1 ;
      Dimension s = getSize() ;
      if (x < 0 || x >= s.width) return -1 ;
      if (y < 0 || y >= s.height) return -1 ;
      float sf1 = (scaled) ? sf : 1.0f ;

      // Get the image pixel. Transparent colors of 1 are adjusted to 0.

      int rgb = 0 ;
      x = (int) (x * sf1) ;
      y = (int) (y * sf1) ;
      int pixels[] = new int[1] ;
       PixelGrabber pg = new PixelGrabber(img,x,y,1,1,pixels,0,s.width) ;
        try { pg.grabPixels() ; }
        catch (InterruptedException e) { return -1 ; }
      rgb = pixels[0] & 0xffffff ;
      if (rgb == 1) rgb = 0 ;
      return rgb ;
   }


   // Function to return the current pixels in the specified area.
   // This function returns null if the rectangle area is outside
   // the cel bounding box.

   int [] getPixels(Rectangle r)
   {
      if (cm == null) return null ;
      Image img = getImage() ;
      if (img == null) return null ;
      Dimension s = getSize() ;
      int x = r.x - box.x ;
      int y = r.y - box.y ;
      if (x < 0 || y < 0) return null ;
      if (x >= box.width || y >= box.height) return null ;
      float sf1 = (scaled) ? sf : 1.0f ;

      // Apply the scaling factor.  We will return at least one pixel.

      x = (int) (x * sf1) ;
      y = (int) (y * sf1) ;
      int w = (int) (r.width * sf1) ;
      int h = (int) (r.height * sf1) ;
      if (w < 1) w = 1 ;
      if (h < 1) h = 1 ;

      // Extract the pixels.

      int pixels[] = new int[w*h] ;
      PixelGrabber pg = new PixelGrabber(img,x,y,w,h,pixels,0,w) ;
      try { pg.grabPixels() ; }
      catch (InterruptedException e) { return null ; }
      return pixels ;
   }


   // Method to generate a palette from the cel image.

   Palette makePalette() { return makePalette(256) ; }
   Palette makePalette(int maxcolors)
   {
      Palette palette = null ;
      IntHashtable colorHash = new IntHashtable() ;
      int transparentIndex = -1 ;
      int backgroundIndex = -1 ;
      int transparentRgb = -1 ;
      int index = 0 ;

      // We must construct a palette for this image and convert pixel
      // colors into color indexes.

      try
      {
         byte [] a = new byte[0] ;
         byte [] r = new byte[0] ;
         byte [] g = new byte[0] ;
         byte [] b = new byte[0] ;
         int colors = 0 ;

         // If the image is indexed, construct the palette from the color model.

         Image img = getImage() ;
         if (img == null) return null ;
         if (img instanceof BufferedImage)
         {
            BufferedImage bi = (BufferedImage) img ;
            ColorModel cm = bi.getColorModel() ;
            if (cm instanceof IndexColorModel)
            {
               IndexColorModel icm = (IndexColorModel) cm ;
               transparentIndex = icm.getTransparentPixel() ;
               int n = icm.getMapSize() ;
               a = new byte[n] ;
               r = new byte[n] ;
               g = new byte[n] ;
               b = new byte[n] ;
               icm.getAlphas(a);
               icm.getReds(r);
               icm.getGreens(g);
               icm.getBlues(b);
               colors = n ;
            }
         }

         // If the image is not indexed, grab the image pixels.  Our image is
         // ARGB and the palette that we construct will not relate to the image
         // data sample representation.

         if (r.length == 0)
         {
            int h = img.getHeight(null) ;
            int w = img.getWidth(null) ;
            if (h < 0 || w < 0) return null ;
            int [] rgbPixels = new int[h * w] ;
            PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, rgbPixels, 0, w) ;
            try { pg.grabPixels() ; }
            catch (InterruptedException e) {}
            if ( (pg.getStatus() & ImageObserver.ABORT) != 0) return null ;

            // Scan the image data to translate the image pixel colors into
            // color palette indexes.  Image colors that do not exist in the
            // palette are added to the palette.

            for (int row = 0 ; row < h ; ++row) 
            {
               for (int col = 0 ; col < w ; ++col) 
               {
                  int rgb = rgbPixels[row * w + col] ;
                  boolean isTransparent = ( (rgb >>> 24) == 0) ;
                  if (isTransparent) 
                  {
                     if (transparentIndex < 0) 
                     {
                        // First transparent color; remember it.
                        transparentIndex = index ;
                        transparentRgb = rgb ;
                     }
                     else if (rgb != transparentRgb) {
                        // A second transparent color; replace it with
                        // the first one.
                        rgbPixels[row * w + col] = rgb = transparentRgb ;
                     }
                  }
                  else
                     rgb |= 0xFF000000 ;

                     // We have a pixel.  Find the color in our hash table.  If it
                     // does not exist, create a new color entry in the hash table.
                     // If it does exist, count the number of times this color is used.
                     // Note that pixels with color (0,0,1) are actually color (0,0,0).

                  if ( (rgb & 0xffffff) == 1) rgb-- ;
                  EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb) ;
                  if (item == null) 
                  {
                     if (index >= maxcolors) 
                     {
                        index = maxcolors ;
                        row = h ;
                        col = w ;
                        break ;
                     }

                     // Remember this color.

                     item = new EncoderHashitem(rgb, 1, index, isTransparent) ;
                     colorHash.put(rgb, item) ;
                     ++index ;
                  }
                  else
                     ++item.count ;
               }
            }

            // Create the new palette colors.

            colors = index ;
            index = 0 ;
            rgbPixels = null ;
            a = new byte[colors] ;
            r = new byte[colors] ;
            g = new byte[colors] ;
            b = new byte[colors] ;
            Enumeration enum1 = colorHash.keys() ;
            while (enum1.hasMoreElements()) {
               Integer rgbkey = (Integer) enum1.nextElement() ;
               int rgb = rgbkey.intValue() ;
               a[index] = (byte) ( (rgb >> 24) & 0xff) ;
               r[index] = (byte) ( (rgb >> 16) & 0xff) ;
               g[index] = (byte) ( (rgb >> 8) & 0xff) ;
               b[index] = (byte) ( (rgb) & 0xff) ;
               index++ ;
            }
         }

         // Create a new palette object.

         palette = new Palette(zip,file) ;
         int [] back = new int[1] ;
         int [] trans = new int[1] ;
         back[0] = backgroundIndex ;
         trans[0] = transparentIndex ;
         palette.setPalette(a,r,g,b,1,colors,back,trans) ;
         palette.setIdentifier(new Integer(-1));
      }

      // Watch for encoding errors.

      catch (Exception e)
      {
         System.out.println("Cel: makepalette exception " + e) ;
         return null ;
      }

      // Return the result.

      return palette ;
   }



   // Function to dither a cel to a specified number of colors.  This function
   // returns a new cel image and a new palette. The objects returned are the
   // image, palette, and color model.

   Object [] dither(int m)
   { return dither(m,false) ; }

   Object [] dither(int m, boolean quick)
   {
      ColorModel cm = null ;
      Palette palette = null ;
      Image image = getImage() ;
      if (image == null) return null ;

      int w = image.getWidth(null) ;
      int h = image.getHeight(null) ;
      if (w <= 0 || h <= 0) return null ;

      // Get the image pixels.

      int pix[] = new int[w * h] ;
      PixelGrabber pg = new PixelGrabber(image,0,0,w,h,pix,0,w);
      try { pg.grabPixels() ; }
      catch (InterruptedException e) { }
      if ((pg.getStatus() & ImageObserver.ABORT) != 0) return null ;

      // Reduce the image colors.

      int pixels[][] = new int[w][h];
      for (int x = w; x-- > 0; )
         for (int y = h; y-- > 0; )
            pixels[x][y] = pix[y * w + x];
      Color c = getTransparentColor() ;
      int transcolor = (c != null) ? (c.getRGB() & 0xffffff) : getNonImageColor(pix) ;
      int transindex = getTransparentIndex() ;
      int reduced[] = Quantize.quantizeImage(pixels,m,transcolor,transindex,quick) ;

      // Create a new palette if we have dithered to 256 colors or less.  If this 
      // is a KiSS cel then we create a non-internal KCF palette, otherwise the  
      // palette is an internal palette with the same name as the cel.

      if (reduced.length <= 256)
      {
         palette = new Palette() ;
         palette.setInternal(true) ;
         palette.setName(getName()) ;
         palette.setZipFile(getZipFile());
         palette.setIdentifier(new Integer(-1)) ;
         palette.setPalette(reduced) ;
         if ((reduced[0] & 0xFF000000) == 0) transparentindex = 0 ;
         if (transparentindex >= 0)
         {
            palette.setTransparentIndex(0) ;
            transparentcolor = palette.getTransparentColor(0) ;
         }
         
         // Correct palette name if cel.  If we are assuming a KCF type for 
         // the palette then the cel needed to dither such that the first
         // color was transparent.
         
         if (ArchiveFile.isCel(getName()))
         {
            String name = palette.getName() ;
            int n = name.lastIndexOf('.') ;
            String s = (n < 0) ? name : name.substring(0,n) ;
            s = s + ".kcf" ;
            palette.setName(s) ;
            palette.setInternal(false) ;
         }
      }

      // Construct the cel image.  If we did not dither to a palette we create
      // a basic ARGB image by picking the pixel colors up from the reduced
      // palette based upon their (x,y) palette indexes in the pixel array.

      if (palette == null)
      {
         for (int y = 0 ; y < h ; y++ )
            for (int x = 0 ; x < w ; x++ )
                pix[y * w + x] = reduced[pixels[x][y]] ;
         cm = Palette.getDirectColorModel() ;
         image = component.createImage(new MemoryImageSource(w,h,cm,pix,0,w)) ;
      }

      // If we do have a palette we create an INDEXED buffered image. We pick
      // up the RGB values from the reduced palette based upon their (x,y) 
      // palette indexes in the pixel array.

      else
      {
         cm = palette.createColorModel(getTransparency(),0) ;
         if (!(cm instanceof IndexColorModel)) return null ;
         IndexColorModel icm = (IndexColorModel) cm ;
         BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_BYTE_INDEXED,icm) ;
         for (int y = 0 ; y < h ; y++ )
            for (int x = 0 ; x < w ; x++ )
                bi.setRGB(x,y,reduced[pixels[x][y]]) ;
         image = bi ;
      }

      // Return the results.

      Object [] o = new Object[3] ;
      o[0] = image ;
      o[1] = palette ;
      o[2] = cm ;
      return o ;
   }


   // A function to return a color that is not in the pixel array.
   // We search the picture and return the most different color.

   private int getNonImageColor(int [] pix)
   {
      int [] pix1 = new int[pix.length] ;
      System.arraycopy(pix,0,pix1,0,pix.length);

      // Remove any alpha transparencies from the data.

      for(int i = 0 ; i < pix1.length ; i++)
         pix1[i] = pix1[i] & 0xFFFFFF ;
      Arrays.sort(pix1) ;

      // Find the largest difference.

      int loc = 0 ;
      int dif = 0 ;
      for (int i = 1 ; i < pix1.length ; i++)
      {
         int d = pix1[i] - pix1[i-1] ;
         if (d > dif) { dif = d ; loc = i - 1 ; }
      }

      // Compute the mid difference with a non-zero alpha.

      int n = ((pix1[loc+1]-pix1[loc]) / 2) + pix1[loc] ;
      return (n | 0xFF000000) ;
   }



   // Function to scale the base image.  A zero scaling factor frees all
   // scaled image allocations.

   void scaleImage(float scale)
      throws KissException
   {
      if (image == null) return ;
      scaled = (scale != 1.0f) ;

      // Request to eliminate scaling?

      if (scale == 0.0f || !scaled)
      {
         imagescaledwidth = 0 ;
         imagescaledheight = 0 ;
         scaledimage = null ;
         scaled = false ;
         sf = 1.0f ;

         // Apply current colors and transparency.

         if (cm != basecm || transparency != 255 || isInitTransUpdated())
         {
            ImageProducer base = image.getSource() ;
            ImageProducer ip = new FilteredImageSource(base,
               new PaletteFilter(cm,basecm,transparency,transparentcolor));
            filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            MediaTracker tracker = new MediaTracker(component) ;
            tracker.addImage(filteredimage,0) ;
            try { tracker.waitForAll(500) ; }
            catch (InterruptedException e) { }
         }
      }

      // If we have a scaled image at this size, use it.

      if (!scaled) return ;
      float delta = scale - sf ;
      if (delta < 0) delta = -delta ;
      if (scaledimage != null && delta < 0.01f) return ;
      filteredimage = null ;

      // Determine the new scaled image size.

      int w = size.width ;
      int h = size.height ;
      int sw = (int) Math.ceil(w * scale) ;
      int sh = (int) Math.ceil(h * scale) ;

      // Scale the image.  Watch for errors.

      try
      {
//       scaledimage = image.getScaledInstance(sw,sh,Image.SCALE_REPLICATE) ;
         scaledimage = createResizedCopy(image,sw,sh,true) ;
         imagescaledwidth = sw ;
         imagescaledheight = sh ;

         // Establish the new scaled image size.  Note that this is not the
         // size of the scaled image.  It is the adjusted base size of the
         // image that results from the scaling and should be equal or close
         // to the original image size.

         sf = scale ;
         sw = (int) (sw / sf) ;
         sh = (int) (sh / sf) ;
         scaledsize = new Dimension(sw,sh) ;

         // Apply current colors and transparency.

         if (cm != basecm || transparency != 255 || isInitTransUpdated())
         {
            ImageProducer base = scaledimage.getSource() ;
            ImageProducer ip = new FilteredImageSource(base,
               new PaletteFilter(cm,basecm,transparency,transparentcolor));
            filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            MediaTracker tracker = new MediaTracker(component) ;
            tracker.addImage(filteredimage,0) ;
            try { tracker.waitForAll(500) ; }
            catch (InterruptedException e) { }
         }
      }
      catch (OutOfMemoryError e)
      {
         throw new KissException("Scale image, out of memory, cel " + file) ;
      }
   }



   // Function to scale the base image to a specific width and height.  
   // Scaled image allocations are released if the specified dimension
   // is equivalent to the original image size. 

   void scaleImage(Dimension d)
      throws KissException
   {
      if (image == null) return ;
      filteredimage = null ;
      int sw = d.width ;
      int sh = d.height ;
      int w = image.getWidth(null) ;
      int h = image.getHeight(null) ;

      // Request to eliminate scaling?

      if (sw == w && sh == h)
      {
         imagescaledwidth = 0 ;
         imagescaledheight = 0 ;
         scaledimage = null ;
         scaled = false ;
         sf = 1.0f ;

         // Apply current colors and transparency.

         if (cm != basecm || transparency != 255 || isInitTransUpdated())
         {
            ImageProducer base = image.getSource() ;
            ImageProducer ip = new FilteredImageSource(base,
               new PaletteFilter(cm,basecm,transparency,transparentcolor));
            filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            MediaTracker tracker = new MediaTracker(component) ;
            tracker.addImage(filteredimage,0) ;
            try { tracker.waitForAll(500) ; }
            catch (InterruptedException e) { }
         }
         return ;
      }

      // Scale the image.  Watch for errors.

      try
      {
//       scaledimage = image.getScaledInstance(sw,sh,Image.SCALE_REPLICATE) ;
         scaledimage = createResizedCopy(image,sw,sh,true) ;
         imagescaledwidth = sw ;
         imagescaledheight = sh ;

         // Establish the new scaled image size. A scaled image with a scale
         // factor of 1 indicates that we have performed a dimension scale.
         // This is used during encoding to reference the scaled image. 

         sf = 1.0f ;
         scaled = true ;
         scaledsize = new Dimension(sw,sh) ;

         // Apply current colors and transparency.

         if (cm != basecm || transparency != 255 || isInitTransUpdated())
         {
            ImageProducer base = scaledimage.getSource() ;
            ImageProducer ip = new FilteredImageSource(base,
               new PaletteFilter(cm,basecm,transparency,transparentcolor));
            filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            MediaTracker tracker = new MediaTracker(component) ;
            tracker.addImage(filteredimage,0) ;
            try { tracker.waitForAll(500) ; }
            catch (InterruptedException e) { }
         }
      }
      catch (OutOfMemoryError e)
      {
         throw new KissException("Scale image, out of memory, cel " + file) ;
      }
   }
   
   // Create a BufferedImage of the desired size and draw the original 
   // image into it, scaling on the fly. Note that depending on whether 
   // your original image is opaque or non-opaque (that is, if it's 
   // translucent or transparent), you may need to create an image with 
   // an alpha channel.

   BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
   {
       int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
       BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
       Graphics2D g = scaledBI.createGraphics();
       if (preserveAlpha) 
       {
           g.setComposite(AlphaComposite.Src);
       }
       g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
       g.dispose();
       return scaledBI;
   }


   // Method to draw a selection box around our image.  This box highlights
   // a selected cel for cut, copy, or paste operations.  Marked cels are
   // those that have been selected from within an ungrouped oject and are
   // displayed in a darker color.

   void drawSelected(Graphics g, float sf, int wx, int wy, boolean marked, boolean flicker)
   {
      final int space = 2 ;      					// size of line segment space
      final int segment = 4 ;							// size of line segment

      setPasted(false) ;
      Rectangle selectbox = getBoundingBox() ;
      if (selectbox == null) return ;
      if (selectbox.width <= 0) return ;
      if (selectbox.height <= 0) return ;
      int rgb = selectcolor.getRGB() ;
      g.setColor(selectcolor) ;
      rgb = rgb ^ 0xFFFF ;
      Color color = new Color(rgb) ;
//    g.setXORMode(color) ;
      if (marked) g.setColor(markcolor);
      if (flicker) g.setColor(flickercolor);
      int colsegments = ((int) (selectbox.width * sf) - space) / (segment + space) ;
      int rowsegments = ((int) (selectbox.height * sf) - space) / (segment + space) ;

      // Draw the horizontal lines.

      int x1 = (int) ((selectbox.x-wx) * sf) ;
      int y1 = (int) ((selectbox.y-wy) * sf) ;
      int x2 = (int) Math.ceil(((selectbox.x-wx) + selectbox.width - 1) * sf) ;
      int y2 = (int) Math.ceil(((selectbox.y-wy) + selectbox.height - 1) * sf) ;
      for (int i = 0 ; i < colsegments ; i++)
      {
         x2 = x1 + segment ;
         g.drawLine(x1,y1,x2,y1) ;
         g.drawLine(x1,y2,x2,y2) ;
         x1 += (segment + space) ;
      }
      x2 = (int) Math.ceil(((selectbox.x-wx) + selectbox.width - 1) * sf) ;
      g.drawLine(x1,y1,x2,y1) ;
      g.drawLine(x1,y2,x2,y2) ;

      // Draw the vertical lines.

      x1 = (int) ((selectbox.x-wx) * sf) ;
      y1 = (int) ((selectbox.y-wy) * sf) ;
      x2 = (int) Math.ceil(((selectbox.x-wx) + selectbox.width - 1) * sf) ;
      y2 = (int) Math.ceil(((selectbox.y-wy) + selectbox.height - 1) * sf) ;
      for (int i = 0 ; i < rowsegments ; i++)
      {
         y2 = y1 + segment ;
         g.drawLine(x1,y1,x1,y2) ;
         g.drawLine(x2,y1,x2,y2) ;
         y1 += (segment + space) ;
      }
      y2 = (int) Math.ceil(((selectbox.y-wy) + selectbox.height - 1) * sf) ;
      g.drawLine(x1,y1,x1,y2) ;
      g.drawLine(x2,y1,x2,y2) ;
//		g.setPaintMode() ;
   }


   // The toString method returns a string representation of this object.
   // This is the class name concatenated with the object identifier.

   public String toString()
   { return super.toString() + " " + getName() ; }

   
	// Cel clone.  This creates a new object where object references
   // are new duplicates of the references found in the original object.

   public Object clone()
   {
       Cel c = (Cel) super.clone() ;
       c.init() ;
       return c ;
   }
}
