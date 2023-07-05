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
* Group class
*
* Purpose:
*
* This class encapsulates a group of cel objects or group objects.  A group is
* a single unit of associated cels or groups that can be moved as one unit.
*
* Group objects use their group identifier number as their access key.
*
*/


import java.awt.* ;
import java.util.* ;
import java.awt.image.* ;
import java.io.IOException ;
import java.io.OutputStream ;

final class Group extends KissObject
{
	// Class attributes.  Sized for 512 group objects.

	static private Hashtable key = new Hashtable(600,0.855f) ;

	// Group attributes

	private Vector cels = null ;				// The set of cels in the group
	private Vector groups = null ;		  	// The set of groups in the group
	private Group primary = null ;		  	// The primary group in the set
	private Rectangle basebox = null ;		// The base group bounding box
	private Rectangle box = null ;			// The moving group bounding box
	private Point placement = null ;			// The group movement offset
	private Point offset = null ;				// The group location offset
	private Point initoffset = null ;		// The group initial offset
	private Point flex = null ;				// The group flex value
   private Color selectcolor = null ;		// The cel selection box color
	private Integer level = null ;			// The lowest cel z-level
	private Integer currentframe = null ;	// The current frame number
   private Integer pagecontext = null ;	// The last pagecontext set

   // State attributes

	private boolean visible = true ;			// The group visibility
	private boolean unfix = false ;			// True when flex value goes to 0
   private boolean constrain = false ;		// True if force within panel
   private boolean restricted = false ;	// True if placement is restricted
   private boolean animate = false ;      // True, object can animate
	private int transparency = 255 ;			// The group relative transparency
   private int framedelay = 0 ;				// Time delay until next frame
   private int frametime = 0 ;				// Time elapsed for next frame


	// Constructor

	public Group() { this(null) ; }
	public Group(Integer id)
	{
      setUniqueID(new Integer(this.hashCode())) ;
		cels = new Vector() ;
		groups = new Vector() ;
		placement = new Point(0,0) ;
		offset = new Point(0,0) ;
		initoffset = new Point(0,0) ;
		flex = new Point(1,0) ;
		level = new Integer(0) ;
      currentframe = new Integer(0) ;
      selectcolor = Color.blue ;
	}


   // Cloned object initialization.

   void init()
	{
      setUniqueID(new Integer(this.hashCode())) ;
		cels = new Vector() ;
		groups = new Vector() ;
		placement = new Point(0,0) ;
		offset = new Point(0,0) ;
		initoffset = new Point(0,0) ;
		flex = new Point(1,0) ;
		level = new Integer(0) ;
      currentframe = new Integer(0) ;
      basebox = null ;
      box = null ;
	}



	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// file.

	static void clearTable(Object cid)
	{
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}

	// Function to find a Group object by parameter name.   We accept
	// object integer numbers preceeded by a #, or variables preceeded by
	// a # that evaluate to integer group numbers, or variables that
	// evaluate to a string preceeded by a #, or variables that evaluate
	// to an integer group number not preceeded by #.

	static Group findGroup(String s, Configuration c, FKissEvent event)
	{
		Integer g ;
      if (c == null) return null ;
		if (s == null || s.length() < 1) return null ;

      // Literals or variables preceeded by '!' cannot be used as
      // group identifiers.

      if (s.charAt(0) == '!') return null ;

		// Check for a literal or variable preceeded by #.

		if (s.charAt(0) == '#')
		{
			try { g = new Integer(s.substring(1)) ; }
			catch(NumberFormatException e)
			{
				Object o = c.getVariable().getValue(s.substring(1),event) ;
				if (o instanceof Integer)
               g = (Integer) o ;
            else if (o instanceof Long)
               g = new Integer(((Long) o).intValue()) ;
            else if (o instanceof Double)
               g = new Integer(((Double) o).intValue()) ;
            else if (o != null)
            {
      			try { g = new Integer(o.toString().trim()) ; }
      			catch(NumberFormatException e1) { return null ; }
            }
            else return null ;
			}
			return (Group) Group.getByKey(Group.getKeyTable(),c.getID(),g) ;
		}

		// Check for a variable not preceeded by #.  Numeric literals fail,
      // which means that we cannot refer to a group simply by number.

		else
		{
			if (s.length() > 0 && Character.isDigit(s.charAt(0))) return null ;
			Object o = c.getVariable().getValue(s,event) ;
			if (o != null)
			{
				s = o.toString().trim() ;
				if (s.length() > 0 && s.charAt(0)  == '#')
					return findGroup(s,c,event) ;
				else
				{
					try { g = new Integer(s) ; }
					catch(NumberFormatException e)  { return null ; }
					return (Group) Group.getByKey(Group.getKeyTable(),c.getID(),g) ;
				}
			}
		}
		return null ;
	}



	// Object state change methods
	// ---------------------------

	// Add a cel to the group.  The cel may not be located at the origin
   // of the group.  In this case the cel offset is set as an offset from
   // the actual group location.  Cels are not added if they already exist.

	void addCel(Cel c) { addCel(c,false) ; }
	void addCel(Cel c, boolean first)
	{
   	if (c == null) return ;
      if (cels.contains(c)) return ;
		if (first) cels.insertElementAt(c,0) ; else cels.addElement(c) ;
		c.setGroup(this) ;
		c.setPlacement(placement) ;
      Point location = getLocation() ;

      // Set the group comment to be the first known cel name.

      if (comment == null) comment = c.getName() ;

		// Update the group bounding box.

		if (basebox == null)
			basebox = new Rectangle(c.getBoundingBox()) ;
		else
			basebox = basebox.union(c.getBoundingBox()) ;

		// Establish our moving bounding box and group location offset.
		// The location offset is the (x,y) point where the group is
		// initially located.  This is not always at the origin.

		box = new Rectangle(basebox) ;
		offset.x = -(location.x - box.x) ;
		offset.y = -(location.y - box.y) ;

      // Update the cel offset to be relative from the group location.

     Point cellocation = c.getLocation() ;
     Point celoffset = c.getOffset() ;
     int x = -(location.x - cellocation.x) + celoffset.x ;
     int y = -(location.y - cellocation.y) + celoffset.y ;
     c.setOffset(x,y) ;

      // Group movement can be constrained to the panel frame.  By default,
      // groups that contain video cels are constrained because the movie
      // window sits on top of the panel frame.

//    if (c instanceof Video) constrain = true ;

		// Update the group flex value.  This is set to the greatest
		// flex value across all cels in the group.

		int f = (c.getFlex() == null) ? 0 : c.getFlex().intValue() ;
		if (f > flex.y) flex = new Point(f+1,f) ;

		// Retain the draw level of the largest numbered cel in the
		// group.  This identifier represents the minimum overlay
		// level that needs to be considered when the group is drawn.
		// All cels with lower numbers can conceivably overlay this
		// group and all cels with higher numbers cannot.

		Integer id = c.getLevel() ;
		if (id != null && (id.intValue() > level.intValue())) level = id ;
	}


   // Function to add a group or cel to this object.  Groups of groups
   // represent selection set objects or movement objects for groups with
   // attached children. This function adds either groups or cels to this
   // group object.  Group children are added by default.  Objects are not
   // added if they are currently a member of the set.

	void addElement(KissObject kiss) { addElement(kiss,true) ; }
	void addElement(KissObject kiss, boolean withchild)
   {
		if (kiss == null) return ;

		// Add a Group object.  This adds the group and all its cels to this
      // object.

      if (kiss instanceof Group)
      {
			Group g = (Group) kiss ;
			if (!groups.contains(g))
			{
				groups.addElement(g) ;
				Vector groupcels = g.getCels() ;
				cels.addAll(groupcels) ;
			}
		}

		// Add a Cel object.  Note that the cel object is not changed.
      // Movement of this group must perform an updateCelPlacement()
      // and restoreCelPlacement() or drop() to correctly move group cels.

      if (kiss instanceof Cel)
      {
      	Cel c = (Cel) kiss ;
			if (!cels.contains(c)) cels.addElement(c) ;
      }

		// Update the group bounding box.

		if (basebox == null)
			basebox = new Rectangle(kiss.getBoundingBox()) ;
		else
			basebox = basebox.union(kiss.getBoundingBox()) ;

		// Establish our moving bounding box and group location offset.
		// The location offset is calculated as the difference between
      // the drawing bounding box and the object location box.

		box = new Rectangle(basebox) ;
      Rectangle bb = getLocationBox().union(kiss.getLocationBox()) ;
		offset.x = box.x - bb.x ;
		offset.y = box.y - bb.y ;

		// Retain the draw level of the all groups in this group.

		Integer id = kiss.getLevel() ;
		if (id != null && (id.intValue() > level.intValue())) level = id ;

      // Update any movement restrictions.  This will set restrictions on
      // this object as the minimum restrictions necessary to satisfy the
      // attached object.

      updateMoveRestrictions(kiss.getLocation(),kiss.getRestrictX(),kiss.getRestrictY()) ;

   	// If our group has children, add them, too.  This is a recursive call.

      if (kiss instanceof Group)
      {
			Group g = (Group) kiss ;
   		if (withchild && g.hasChildren())
			{
				Vector children = g.getChildren() ;
				for (int i = 0 ; i < children.size() ; i++)
					addElement((KissObject) children.elementAt(i),true) ;
   		}
      }
   }


	// Remove a cel from the group.  We retain the cel association to
   // this group object so that undo/redo will work for imported cels.
   // We reconstructs the group bounding box after the cel is removed.

	void removeCel(Cel c)
	{
   	if (c == null) return ;
		if (!cels.removeElement(c)) return ;
      c.setPlacement(null) ;
      rebuildBoundingBox() ;
   }


	// Remove an object from the group.

	void removeElement(KissObject kiss)
	{
   	if (kiss == null) return ;
      if (kiss instanceof Cel)
         removeCel((Cel) kiss) ;
      if (kiss instanceof Group)
      {
			Group g = (Group) kiss ;
			groups.remove(g) ;
			Vector groupcels = g.getCels() ;
         if (groupcels != null) cels.removeAll(groupcels) ;

         // If our group has children, remove them, too.

   		if (g.hasChildren())
			{
				Vector children = g.getChildren() ;
				for (int i = 0 ; i < children.size() ; i++)
					removeElement((KissObject) children.elementAt(i)) ;
   		}
      }
   }


   // Set the primary group in a set of groups.

   void setPrimaryGroup(Group g) { primary = g ; }


   // Set the group bounding box size and level based upon the specified page
   // set context.  If no page is specified the context is set for all cels.

	void setContext(Integer page)
   {
      pagecontext = page ;
		level = new Integer(0) ;
      currentframe = new Integer(0) ;
		Rectangle r = new Rectangle(offset.x,offset.y,0,0) ;

		// Calculate the group bounding box.  This box is relative to a base
		// location of (0,0) adjusted by the group offset.

		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			if (page != null && !c.isOnPage(page)) continue ;
			Dimension d = c.getSize() ;
			Point p = c.getOffset() ;
			r = r.union(new Rectangle(p,d)) ;

			// Retain the draw level of the largest numbered cel in the
			// group.  This identifier represents the minimum overlay
			// level that needs to be considered when the group is drawn.
			// All cels with lower numbers can conceivably overlay this
			// group and all cels with higher numbers cannot.

			Integer id = c.getLevel() ;
			if (id != null && (id.intValue() > level.intValue())) level = id ;
		}

  		// Establish our new bounding box size.

		if (box != null)
		{
			box.width = r.width ;
			box.height = r.height ;
      }
		if (basebox != null)
		{
			basebox.width = r.width ;
			basebox.height = r.height ;
		}
	}


   // Get the group visible size.  This is used for FKiSS actions getheight()
   // and getwidth().

	Dimension getVisibleSize(Integer page)
   {
		Rectangle r = getVisibleBoundingBox(page) ;
      return new Dimension(r.width,r.height) ;
	}

   
   // Get the visible cel count on the specified page.

	int getVisibleCelCount(Integer page)
	{
      int n = 0 ;
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
         if (OptionsDialog.getMapCount())
         {
            if (c.isOnPage(page) && c.isVisible()) n++ ;
         }
         else
         {
            if (c.isVisible()) n++ ;
         }
		}
		return n ;
	}


	// Set the group location relative to where it currently is.  Note
	// that all cels in the group have a reference to this placement
	// point for group movements.  Thus, to change the group location,
	// we must change the actual point values and drop the group.
   // Placement changes are ignored if the group has restricted
   // placements set.

	void setPlacement(int x, int y) { setPlacement(x,y,true) ; }
	void setPlacement(int x, int y, boolean restrict)
	{
      if (!restrict || !restricted)
      {
   		placement.x = x ;
   		placement.y = y ;
      }
      else
      {
         Rectangle r = getBaseBoundingBox() ;
         int x1 = x + r.x ;
         int y1 = y + r.y ;
         boolean inx = true ;
         boolean iny = true ;
         if (restrictx != null && restrictx.x <= restrictx.y)
            if (x1 < restrictx.x || x1 > restrictx.y) inx = false ;
         if (restricty != null && restricty.x <= restricty.y)
            if (y1 < restricty.x || y1 > restricty.y) iny = false ;
         
         // Set the placement value to the current offset if within
         // bounds, otherwise set the offset to the limit.
         
         if (inx) placement.x = x ;
         else if (x1 < restrictx.x) placement.x = restrictx.x - r.x ;
         else if (x1 > restrictx.y) placement.x = restrictx.y - r.x ;
         if (iny) placement.y = y ;
         else if (y1 < restricty.x) placement.y = restricty.x - r.y ;
         else if (y1 > restricty.y) placement.y = restricty.y - r.y ;
      }

      // Set the placement value for all contained groups.

      for (int i = 0 ; i < groups.size() ; i++)
      {
         Group g = (Group) groups.elementAt(i) ;
         g.setPlacement(x,y,restrict) ;
      }
	}


   // This variant of the setPlacement method sets a new placement object
   // for the group and all contained cels.

   void setPlacement(Point p)
   {
      placement = p ;
      updateCelPlacement() ;
      setPlacement(p.x,p.y,false) ;
   }

	// Set our placement to the specifed values and indicate that these are
   // restricted placements. The restriction indicator is propagated to all 
   // children in the attachment hierarchy,

   void setRestrictedPlacement(int x, int y)
	{
      setPlacement(x,y) ;
      restricted = true ;

      // Set the restricted placement for all children.

      Vector children = getChildren() ;
      if (children == null) return ;
      for (int i = 0 ; i < children.size() ; i++)
         ((KissObject) children.elementAt(i)).setRestrictedPlacement(x,y) ;
	}

	// Clear the indicator that the group has restricted placement set.
   // This indicator is propagated to all children in the attachment hierarchy,

	void clearRestrictedPlacement()
   {
      restricted = false ;
      
      // Clear the restricted placement on all children.

      Vector children = getChildren() ;
      if (children == null) return ;
      for (int i = 0 ; i < children.size() ; i++)
         ((KissObject) children.elementAt(i)).clearRestrictedPlacement() ;
   }

	// Obtain the indicator that drag movement offset was constrained by 
   // restrictions. If this is true then standard drag movement placement
   // cannot be set.

	boolean hasRestrictedPlacement() { return restricted ; } 


   // If cels were added to this group through the addElement method
   // they may not be fully associated with this object.  Movements
   // require a link to this placement point.  The non-association can
   // happen for groups created to manage selections.  If we are dragging
   // a selection group we must temporarily update the cel placement
   // and restore the old association when the group is dropped.

   void updateCelPlacement()
   {
      if (cels == null) return ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         Object o = c.getGroup() ;
         if (o == this) continue ;
         c.setPlacement(placement) ;
      }
   }

   // If cels were added to this group through the addElement method
   // they may not be fully associated with this object.  This method
   // will restore any cel placements if they have been updated.

   void restoreCelPlacement()
   {
      if (cels == null) return ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         Object o = c.getGroup() ;
         if (o == this) continue ;
         if (!(o instanceof Group)) continue ;
         c.setPlacement(((Group) o).getPlacementObject()) ;
      }
   }

	// Set the group offset.  Under normal conditions this is the minimum
   // offset over all cels in the group.  However, if we are editing and
   // repositioning a cel movement cannot be constrained by the cel offset.

	void setOffset(Point p) { offset = p ; }

	// Set the initial group offset.  This must be set the first time a group
   // object is created after all cels are added.

	void setInitialOffset() { initoffset = new Point(offset) ; }

	// Set all group cels visible or invisible.

	void setVisible(boolean b)
	{
		visible = b ;
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).setVisible(b) ;
	}

	// Set the group visibility without changing the cel visibility.

	void setVisibility(boolean b) { visible = b ; }

	// Set the group movement constraint.

	void setConstrain(boolean b) { constrain = b ; }

   // Set the group selection color.

   void setSelectColor(Color c) { selectcolor = c ; }

	// Set all group cels to be ghosted or not.  Ghosted cels cannot be
   // selected by the user.  Transparent groups that are ghosted act like
   // filters over the actual drawing scene.

	void setGhost(boolean b)
	{
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).setGhost(b) ;
	}

	// Set the animation flag for this object. An animated object
   // shows all cels for a constant frame display time. For backwards
   // compatibiity, an animate(group,0) stops all group cels from 
   // animating and an animate(group,1) starts all group cels.

	void setAnimate(int n)
	{
      animate = (n > 0) ;
      setInterval(n) ;
      setTime(0) ;

      // Animation periods of 0 and 1 are global settings to turn on or
      // off animation of all contained cels in this group.
      
      if (n <= 1)
      {
         for (int i = 0 ; i < cels.size() ; i++)
         {
            Cel cel = (Cel) cels.elementAt(i) ;
            cel.setAnimate(n) ;
         }
         setInterval(0) ;
         return ;
      }
      
      // Get the animation timer.

      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf == null) return ;
      Configuration config = mf.getConfig() ;
      if (config == null) return ;
      GifTimer timer = config.getAnimator() ;
      if (timer == null) return ;
      if (timer.isQueued(this)) return ;
      Vector v = new Vector() ;
      v.addElement(this) ;
      
      // Add this group to the animation queue.
      // If we do not have an active animation timer
      // then create a new one and start it up.

      boolean active = timer.isActive() ;
      boolean suspended = timer.isSuspended() ;
      if (!active && !suspended) 
      {
         timer = new GifTimer() ;
         timer.setEnabled(true) ;
   		timer.setPanelFrame(mf.getPanel()) ;
   		timer.startTimer(v) ;
         config.setAnimator(timer) ;
      }
      else
      {
         if (!suspended) timer.suspendTimer() ;
         timer.updateCels(v,false) ;
         if (!suspended) timer.resumeTimer() ;
      }
	}

	// Set the current frame number.  The requested cel in this group within
	// the last page context is made visible and all other cels are set
   // to be invisible.

	void setFrame(int f)
	{
      int framecount = 0 ;
      int frameindex = -1 ;
      if (cels == null) return ;
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel cel = (Cel) cels.elementAt(i) ;
			if (pagecontext != null && !cel.isOnPage(pagecontext)) continue ;
			cel.setVisibility(false) ;
         if (framecount == f) frameindex = i ;
         framecount++ ;
      }

      // Set the current frame visible.

      visible = false ;
      if (frameindex >= 0) 
      {
         Cel cel = (Cel) cels.elementAt(frameindex) ;
         currentframe = new Integer(f) ;
         cel.setVisibility(true) ;
         visible = true ;
      }
	}

   // Animation specific methods.

   boolean getAnimate() { return animate ; }
   
   int getInterval() { return framedelay ; }

   void setInterval(int t) { framedelay = t ; }

   long getTime() { return frametime ; }

   void setTime(int t) { frametime = t ; }

	// Set the transparency for all cels in the group.  This is a
   // relative change.  Limit change so that the object transparency
   // is within 0-255.

	void changeTransparency(int t, boolean bound, boolean ambiguous)
	{
      int adjust = t ;
      if (bound)
      {
         if (transparency < 0) transparency = 0 ;
         if (transparency > 255) transparency = 255 ;
   		int n1 = 255 - transparency ;
         int n2 = n1 + t ;
         if (n2 < 0) n2 = 0 ;
         if (n2 > 255) n2 = 255 ;
         adjust = n2 - n1 ;
      }
      int kisstransparency = 255 - transparency ;
      kisstransparency += adjust ;
      transparency = 255 - kisstransparency ;
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).changeTransparency(t,bound,ambiguous) ;
	}

	// Set the palette file for all cels in the group.

	void changePaletteID(Integer n)
	{
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).changePaletteID(n) ;
	}

	// Set the group visibility for all cels in the group.

	void changeVisibility(Integer n)
	{
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).changePaletteID(n) ;
	}


   // Method to fix the cel multipalette group.  This is done through 
   // the cel CNF configuration or through a setpal() command.  A cel
   // with a fixed palette group does not participate in color set changes
   // to a different palette group.

   void fixPaletteGroup(Integer n) 
   { 
		for (int i = 0 ; i < cels.size() ; i++)
			((Cel) cels.elementAt(i)).fixPaletteGroup(n) ;
   }

	// Invert the visibility of all group cels.

	void altVisible()
	{
		visible = false ;
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			c.altVisible() ;
			if (c.isVisible()) visible = true ;
		}
	}

	// Set the indicator that the group flex value changed to zero.

	void setUnfix(boolean b) { unfix = b ; }

	// Set the flex value for the group.

	void setFlex(Point p) { flex = p ; }

	// Set the group transparency.

	void setTransparency(int t) { transparency = t ; }

	// Update the group bounding box for a new cel or group size.  This
	// method can increase the size but not decrease it.

	void updateBoundingBox(KissObject kiss)
	{
		if (basebox == null)
			basebox = new Rectangle(kiss.getBoundingBox()) ;
		else
			basebox = basebox.union(kiss.getBoundingBox()) ;
	}

	// Update the group bounding box for a new group size.  If we are a group
	// of groups then we calculate our new size from the contained groups.
	// If not, then calculate the new size from the contained cels.

	void updateBoundingBox()
	{
		Rectangle r = null ;
		if (groups.size() > 0)
		{
			for (int i = 0 ; i < groups.size() ; i++)
			{
				Group g = (Group) groups.elementAt(i) ;
				if (r == null) r = new Rectangle(g.getBoundingBox()) ;
				else r = r.union(g.getBoundingBox()) ;
			}
		}
		else
		{
			for (int i = 0 ; i < cels.size() ; i++)
			{
				Cel c = (Cel) cels.elementAt(i) ;
            if (c.isInternal()) continue ;
				if (r == null) r = new Rectangle(c.getBoundingBox()) ;
				else r = r.union(c.getBoundingBox()) ;
			}
		}
		basebox = r ;
	}

   // Function to rebuild the group bounding box from all the cels.  We
   // assume the group is located at the origin and rebuild the bounding box.
   // All cels have their offsets corrected to reflect their relative position
   // within the group object.  If the group has had all cels removed we
   // retain the original bounding box location.

   void rebuildBoundingBox()
   {
      Point location = (basebox != null)
         ? new Point(basebox.x,basebox.y) : new Point(0,0) ;
      updateBoundingBox() ;
      Point updlocation = (basebox != null)
         ? new Point(basebox.x,basebox.y) : new Point(0,0) ;

      // Set the offset.

      if (basebox != null)
      {
         Point p1 = getLocation() ;
         Point p = new Point(updlocation.x-p1.x,updlocation.y-p1.y) ;
         offset.x = p.x ;
         offset.y = p.y ;
      }
      else
      {
         basebox = new Rectangle(location) ;
         offset.x = initoffset.x ;
         offset.y = initoffset.y ;
      }

      // Update the cel offset to be relative to the original group location.

      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (c.isInternal()) continue ;
         Point cellocation = c.getLocation() ;
         Point celoffset = c.getOffset() ;
         int x = -(updlocation.x - cellocation.x) + celoffset.x ;
         int y = -(updlocation.y - cellocation.y) + celoffset.y ;
         c.setOffset(x+offset.x,y+offset.y) ;
      }
   }

	// Update the group offset given that a cel may now be loaded.
   // This update resets the group to (0,0) as first initialized.

   void updateOffset()
   {
      basebox = null ;
      Point location = getLocation() ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (!c.isLoaded()) continue ;
         if (c.isInternal()) continue ;
         c.setLocation(new Point(0,0)) ;

   		if (basebox == null)
	   		basebox = new Rectangle(c.getBoundingBox()) ;
		   else
			   basebox = basebox.union(c.getBoundingBox()) ;

   		// Establish our moving bounding box and group location offset.
   		// The location offset is the (x,y) point where the group is
   		// initially located.  This is not always at the origin.

   		box = new Rectangle(basebox) ;
   		offset.x = -(location.x - box.x) ;
   		offset.y = -(location.y - box.y) ;
      }
   }

   // Adjust the group offset given that the group may not be properly
   // positioned according to the required page locations.  All cels in
   // the group have their offsets corrected.

   void adjustOffset(Point p)
   {
      setOffset(new Point(offset.x+p.x,offset.y+p.y)) ;
      Point location = getLocation() ;

      // Update the cel offset to be relative to the new group location.

      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (c.isInternal()) continue ;
         Point cellocation = c.getLocation() ;
         Point celoffset = c.getOffset() ;
         int x = -(location.x - cellocation.x) + celoffset.x ;
         int y = -(location.y - cellocation.y) + celoffset.y ;
         c.setOffset(x+offset.x,y+offset.y) ;
      }
   }

   // Eliminate the group offset.

   void eliminateOffset()
   {
      adjustOffset(new Point(-offset.x,-offset.y)) ;
      rebuildBoundingBox() ;
   }
  
	// Update the group lock value.  This function must be performed whenever
   // a cel flex value is changed through user input.  The group flex value
   // is set to the greater of the current flex value and the flex value of
   // all cels in the group.

	void updateFlex()
	{
   	int cf = (flex == null) ? 0 : flex.y ;
		flex = new Point(cf+1,cf) ;
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
         if (c.isInternal()) continue ;
   		int f = (c.getFlex() == null) ? 0 : c.getFlex().intValue() ;
   		if (f > flex.y) flex = new Point(f+1,f) ;
      }
   }
   
   // Update the object draw level.

   void setLevel(Integer n)
   {
      if (n == null) return ;
      if (cels == null) return ;
      int m = getLevel().intValue() ;
      int diff = n.intValue() - m ;
      level = n ;

      // Update the levels of all contained cels.

      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         Integer cellevel = c.getLevel() ;
         if (cellevel == null) continue ;
         m = cellevel.intValue() + diff ;
         c.setLevel(new Integer(m)) ;
      }
   }



	// Object state reference methods
	// ------------------------------

	// Return the specified cel.

	Cel getCel(int i)
	{ return (i >= cels.size()) ? null : (Cel) (cels.elementAt(i)) ; }

	// Return the group cels.

	Vector getCels() { return cels ; }

	// Return all the group cels in this object and all attached objects.

	Vector getAllCels()
   {
      Vector v = new Vector() ;
      if (cels != null) v.addAll(cels) ;
      if (!hasChildren()) return v ;
      Vector children = getChildren() ;
      for (int i = 0 ; i < children.size() ; i++)
         v.addAll(((Group) children.elementAt(i)).getAllCels()) ;
      return v ;
   }

	// Return the group of groups.

	Vector getGroups() { return groups ; }

	// Return the primary group in the set of groups.

	Group getPrimaryGroup() { return primary ; }

	// Return the group size.

	int getCelCount() { return cels.size() ; }

	// Return the last page context set.

	Integer getContext() { return pagecontext ; }

	// Return the group count.

	int getGroupCount() { return groups.size() ; }

	// Return the current frame number.

	int getFrame() { return currentframe.intValue() ; }
	int getFrame(Cel c) { return cels.indexOf(c) ; }

	// A method to return the frame count.

	int getFrameCount() { return getCelCount() ; }

	// Return the group flex value.

	Point getFlex() { return flex ; }

	// Return the indicator monitoring if the flex value changed to zero.

	boolean getUnfix() { return unfix ; }

	// Return the group relative transparency adjustment.  If we are treating
   // group transparency similar to cel group transparency then the collection
   // transparency is -1 if not all cels in the group have the same transparency.  
   // Otherwise, it is the common transparency all cels in the group.

	int getTransparency()
	{
 		int transparent = -1 ;
      if (OptionsDialog.getTransparentGroup()) return transparency ; 
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			int t = c.getTransparency() ;
			if (transparent < 0) transparent = t ;
			if (t != transparent) return -1 ;
		}
		return transparent ;
	}

	// Return the group location.  This is biased by the group offset.
   // The box changes with mouse movement, the basebox changes on group drop.

	Point getLocation()
	{
   	if (basebox == null) return new Point(0,0) ;
      return new Point(basebox.x-offset.x,basebox.y-offset.y) ;
   }
   
	// Method to return the object name.

	String getName()
   {
      Object o = getIdentifier() ;
   	if (!(o instanceof Integer)) return null ;
      return "#" + o.toString() ;
   }

	// Return the group size.

	Dimension getSize()
   {
   	if (basebox == null) return new Dimension() ;
   	return new Dimension(basebox.width,basebox.height) ;
   }

	// Return the group location offset.

	Point getOffset() { return offset ; }

	// Return the group placement value.

	Point getPlacement() { return  new Point(placement) ; }
	Point getPlacementObject() { return placement ; }

	// Return the cel overlay level for drawing this group.

	Integer getLevel() { return (level != null) ? level : new Integer(0) ; }

	// Return the group bounding box.  This bounding box is computed
	// during mouse movements so we update the box variable in place,
	// rather than create and return a new Rectangle.

	Rectangle getBoundingBox()
	{
   	if (basebox == null) return new Rectangle() ;
      if (box == null) box = new Rectangle(basebox) ;
		box.x = basebox.x + placement.x ;
		box.y = basebox.y + placement.y ;
		box.width = basebox.width ;
		box.height = basebox.height ;
		return box ;
	}


   // Get the visible group bounding box.  This is calculated for all visible
   // cels on the specified page. It is relative to a group at (0,0).

	Rectangle getVisibleBoundingBox(Integer page)
   {
		Rectangle r = null ;
      Point grouploc = getLocation() ;
      
		for (int i = 0 ; i < cels.size() ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			if (page != null && !c.isOnPage(page)) continue ;
         if (!c.isVisible()) continue ;
         if (c.isInternal()) continue ;
			Dimension d = c.getSize() ;
         Point celloc = c.getLocation() ;
         Point celoffset = c.getOffset() ;
         celloc.x += celoffset.x ;
         celloc.y += celoffset.y ;
 			Point p = new Point(celloc.x-grouploc.x,celloc.y-grouploc.y) ;
         if (r == null) r = new Rectangle(p,d) ;
			else r = r.union(new Rectangle(p,d)) ;
		}
      
      if (r != null) return r ;
      return new Rectangle(getOffset()) ;
	}

	// Return the group base bounding box.  This is the group bounding box
   // but excludes any moving placement offset.

	Rectangle getBaseBoundingBox()
	{
   	if (basebox == null) return new Rectangle() ;
      return basebox ;
	}

	// Return the object bounding box inclusive of all attached children.

	Rectangle getAttachedBoundingBox() 
   {
      Rectangle r = getBoundingBox() ;
      Vector children = getChildren() ;
      if (children == null) return r ;
      for (int i = 0 ; i < children.size() ; i++)
      {
         Object o = children.elementAt(i) ;
         r = r.union(((KissObject) o).getBoundingBox()) ;
      }
      return r ;
   }

   // Return the consolidated ambiguous cel bounding box. This is
   // established for property changes that apply to all ambiguous
   // cels that require a redraw.

   Rectangle getAllBoundingBox()
   {
      Rectangle r = getBoundingBox() ;
      if (!OptionsDialog.getAllAmbiguous()) return r ;
      Vector v = getAllCels() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         r = r.union(((KissObject) o).getAllBoundingBox()) ;
      }
      return r ;
   }

	// Return the initial object location on the specified page.

	Point getInitialLocation(PageSet page)
	{
		Point p = new Point(0,0) ;
      if (page == null) return p ;
      Object identifier = getIdentifier() ;
      if (!(identifier instanceof Integer)) return p ;
      Point location = page.getInitialGroupPosition((Integer) identifier) ;
      if (location == null) return p ;
      return location ;
	}

	// Get the palette identifier object for all cels in the group.  If the
   // cels have different palettes, this method returns null.

	Object getPaletteID()
	{
      Object pid = null ;
		for (int i = 0 ; i < cels.size() ; i++)
      {
			Object o = ((Cel) cels.elementAt(i)).getPaletteID() ;
         if (pid == null) pid = o ;
         if (o == null) return null ;
         if (!(o.equals(pid))) return null ;
      }
      return pid ;
	}

	// Return the requested event. This is a list of all events by identifier.
   // Collision events are returned only for visible cels in the group.

	Vector getEvent(Object o) 
   { 
      Vector cv = new Vector() ;
      Vector gv = super.getEvent(o) ; 
      if (cels == null) return gv ;
		if (!("apart".equals(o) || "collide".equals(o))) return gv ;
      
      // Group events should include cel collisions so that they are recognized
      // during group moves.
      
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (!c.isVisible()) continue ;
         Vector v = c.getEvent(o) ;
         if (v == null) continue ;
         cv.addAll(v) ;
      }
      if (gv != null) cv.addAll(gv) ;
      if (cv.isEmpty()) return null ;
      return cv ;
   }

	// Determine if this group contains a specific cel.

	boolean containsCel(Cel c)
   {
      return cels.contains(c) ;
   }

	// Return the group visibility.

	boolean isVisible() {return visible ; }

	// Return the group mobility.  If constrained, the group cannot be
   // moved outside of the panel boundary.

	boolean isConstrained() {return constrain ; }

	// Return an indication if the group has any cel on the specified page.

	boolean isOnPage(Integer p)
	{
		for (int i = 0 ; i < cels.size() ; i++)
			if (((Cel) cels.elementAt(i)).isOnPage(p)) return true ;
		return false ;
	}

   // Return an image for the group as it appears on the specified page.
   // This image is constructed by compositing all cels within the group.
	// The image is scaled according to the specified scaling factor.

   Image getImage(Integer page, Object cid, float sf)
   {
      Rectangle r = new Rectangle() ;
      for (int i = 0 ; ; i++)
      {
         Cel cel = getCel(i) ;
         if (cel == null) break ;
         if (!cel.isOnPage(page)) continue ;
         if (!cel.isVisible()) continue ;
         if (cel.isInternal()) continue ;
         Dimension d = cel.getSize() ;
         Point p = cel.getOffset() ;
         if (r == null)
            r = new Rectangle(p,d) ;
         else
  				r = r.union(new Rectangle(p,d)) ;
      }

      // Create an image for the group bounding box.  This box is relative
      // to base coordinate (0,0).   The image is at the scaled size.

      int w = (int) (r.width * sf) ;
      int h = (int) (r.height * sf) ;
      if (w <= 0 || h <= 0) return null ;
   	Image img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
      Graphics g = img.getGraphics() ;
      Rectangle box = getLocationBox() ;
      int x = (int) (box.x * sf) ;
      int y = (int) (box.y * sf) ;
      g.translate(-x,-y) ;

      // Paint the complete group.  We change cel colors to match the selected
      // page set colors if required.

      int celsdrawn = 0 ;
      Vector cellist = (Vector) cels.clone() ;
      Collections.sort(cellist,new LevelComparator(true));
		PageSet pageset = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,page) ;
		Integer newmultipalette = (pageset == null) ? null : pageset.getMultiPalette() ;
      int newmp = (newmultipalette == null) ? 0 : newmultipalette.intValue() ;
		for (int i = cellist.size()-1 ; i >= 0 ; i--)
		{
			Object o = cellist.elementAt(i) ;
			if (!(o instanceof Cel)) break ;
         Cel cel = (Cel) o ;
         if (cel instanceof Video) continue ;
         if (!cel.isLoaded()) continue ;
			if (cel.isOnPage(page))
			{
            cel = (Cel) cel.clone() ;
            cel.setLocation(new Point(box.x,box.y)) ;
				int mp = cel.getMultiPalette() ;
            if (mp != newmp) cel.changePalette(newmultipalette);
				cel.draw(g,null) ;
            celsdrawn++ ;
			}
		}

      // Return the group image.

      g.dispose() ;
      if (celsdrawn == 0) return null ;
      return img ;
   }



	// Object utility methods
	// ----------------------

	// Move the group.

	void move(int x, int y) { setPlacement(x,y) ; }


	// Drop all cels in the group.  The drop is constrained to ensure the
   // object movement does not exceed any restricted values.

	void drop()
	{
   	if (groups.size() > 0)
      {
			for (int i = 0 ; i < groups.size() ; i++)
         {
            Group pko = (Group) groups.elementAt(i) ;
				pko.drop() ;
         }
      }
      
      // For groups that do not have subgroups, drop the cels.
      
      else
      {
			for (int i = 0 ; i < cels.size() ; i++)
         {
            Cel c = (Cel) cels.elementAt(i) ;
				c.drop() ;
            Object o = c.getGroup() ;
            if (o == this) continue ;
            if (!(o instanceof Group)) continue ;
            c.setPlacement(((Group) o).getPlacementObject()) ;
         }
      }

		// Update the group bounding box.

      if (basebox != null)
      {
			basebox.x = basebox.x + placement.x ;
			basebox.y = basebox.y + placement.y ;
      }
      else
         basebox = new Rectangle(placement.x,placement.y,0,0) ;

      // Clear the placement directive.

		setPlacement(0,0) ;
	}

   // A function to reset this object to its initial state.

   void reset()
   {
      super.reset() ;
      visible = true ;
      transparency = 255 ;

      // Reset all cels.  This establishes the images.

		for (int i = 0 ; i < cels.size() ; i++)
         ((Cel) cels.elementAt(i)).reset() ;
   }


   // Draw the group.  This draws all cels in the group constrained by
   // the group bounding box.

   void draw(Graphics g, Rectangle box)
   {
		for (int i = cels.size()-1 ; i >= 0 ; i--)
			((Cel) cels.elementAt(i)).draw(g,box) ;
   }


   // Method to draw a selection box around our group.  This box highlights
   // a selected group for cut, copy, or paste operations.  Marked groups
   // are not used.
   
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
//		g.setXORMode(color) ;
      if (marked) g.setColor(selectcolor.darker().darker());
      if (flicker) g.setColor(Color.red);
		int colsegments = ((int) (selectbox.width * sf) - space) / (segment + space) ;
		int rowsegments = ((int) (selectbox.height * sf) - space) / (segment + space) ;
      
      // If the group contains a Java cel component, we may have to draw the  
      // group cels to get the component to properly display.
      
		for (int i = cels.size()-1 ; i >= 0 ; i--)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (!c.isOnPage(pagecontext)) continue ;
         if (c instanceof JavaCel) c.draw(g,selectbox) ;
         if (c instanceof Video) c.draw(g,selectbox) ;
      }

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
		g.setPaintMode() ;
	}


	// Save the group state.  All cels are saved and the current state
	// is given a name for later reference when it must be restored.

	void saveState(Object cid, Object state)
	{
		for (int i = 0 ; i < cels.size() ; i++ )
			((Cel) cels.elementAt(i)).saveState(cid,state) ;

		// Save the group values.

		State sv = new State(cid,this,state,4) ;
      int x = (basebox == null) ? 0 : basebox.x ;
      int y = (basebox == null) ? 0 : basebox.y ;
		sv.variable[0] = new Point(x,y) ;
		sv.variable[1] = new Boolean(visible) ;
		sv.variable[2] = new Point(flex) ;
		sv.variable[3] = new Point(offset) ;
	}

	// Restore the group to its required state.  All cels are restored,
	// the group flex value is restored, and the group is positioned at
	// its restored location.

	void restoreState(Object cid, Object state) { restoreState(cid,state,false) ; }
	void restoreState(Object cid, Object state, boolean restorevisibility)
	{
		for (int i = 0 ; i < cels.size() ; i++ )
			((Cel) cels.elementAt(i)).restoreState(cid,state,restorevisibility) ;

		// Reset the group values.

		State sv = (State) State.getByKey(cid,this,state) ;
		if (sv != null)
		{
      	if (basebox != null)
         {
				basebox.x = ((Point) sv.variable[0]).x ;
				basebox.y = ((Point) sv.variable[0]).y ;
         }
         if (restorevisibility)
            visible = ((Boolean) sv.variable[1]).booleanValue() ;
			flex.x = ((Point) sv.variable[2]).x ;
			flex.y = ((Point) sv.variable[2]).y ;
         offset = (Point) sv.variable[3] ;
		}
      else if ("initial".equals(state))
      {
         offset.x = basebox.x ;
         offset.y = basebox.y ;
         basebox.x = basebox.y = 0 ;
      }

		// Update the group non-event driven attributes.

		unfix = false ;
		setPlacement(0,0) ;
	}


	// KiSS object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }


   // The toString method returns a string representation of this object.
   // This is the class name concatenated with the name of the first
   // cel in the group.

   public String toString()
   {
   	String s = (cels.size() > 0) ? ((Cel) cels.elementAt(0)).getName() : "" ;
      if (s.length() > 0) return super.toString() + " " + s ;
      return super.toString() ;
   }
}

