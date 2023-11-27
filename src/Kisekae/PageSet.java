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
* PageSet class
*
* Purpose:
*
* This class encapsulates a page set.  A page set contains the  
* object groups placed on the page, the group positions, and the
* default palette for the page.  
* 
* Page set objects use their identifier number as their access key.
* They also use their unique identifier as a permanent key so that
* they can be referenced if their identifier changes due to page
* insertions or deletions.
* 
*/

import java.awt.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Hashtable ;
import java.util.Collection ;
import java.util.Collections ;
import java.awt.Rectangle ;
import java.io.IOException ;
import java.io.OutputStream ;

final class PageSet extends KissObject
{
	// Class attributes.  Sized for 100 page set or scene objects.
	
	static private Hashtable key = new Hashtable(100,1f) ;
	
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
	
	// Page Set attributes
	
	private Integer multipalette = null ;	// Default multipalette
	private Vector positions = null ;		// Group positions in set
	private Vector groups = null ;			// All group objects on this page
   private boolean changed = false ;		// If true, page positions changed
   private boolean visible = true ;		   // If true, page is visible
	private int line = 0 ;						// The configuration file line
	
	
	// Constructor
	
	public PageSet()
	{ 
      setUniqueID(new Integer(this.hashCode())) ;
		multipalette = new Integer(0) ;
		positions = new Vector() ;
		groups = new Vector() ;
	}
	
	
	// Class methods
	// -------------
	
	static Hashtable getKeyTable() { return key ; }
	
	
	// Object state change methods
	// ---------------------------
	
	// Attach the group list to this set.  We take only those groups
	// from the group list that have cels visible on this page.
	
	void setGroups(Vector grouplist)
	{ 
		groups = new Vector() ;
		for (int i = 0 ; i < grouplist.size() ; i++)
		{
			Group group = (Group) grouplist.elementAt(i) ;
			if (!(group.isOnPage((Integer) getIdentifier()))) continue ;
			groups.addElement(group) ;
		}
      Collections.sort(groups) ;
	}
	
	// Set the context of all groups on this page.

	void setContext()
   {
		for (int i = 0 ; i < groups.size() ; i++)
		{
         Integer page = (Integer) getIdentifier() ;
			Group g = (Group) groups.elementAt(i) ;
			if (!(g.isOnPage(page))) continue ;
         g.setContext(page) ;
      }
	}


   // Remove a group from this page.  This also removes the group from
   // the active positions list.  [Note: Removal of the group from the
   // position list compromises undo operations when groups have been
   // cut from the page.]

   boolean removeGroup(Group g)
   {
   	if (groups == null) return false ;
      boolean b = groups.removeElement(g) ;
      Object o = g.getIdentifier() ;
      if (!(o instanceof Integer)) return b ;
		int index = ((Integer) o).intValue() ;
		if (index < 0 || index >= positions.size()) return b ;
//      positions.setElementAt(null,index) ;
      return b ;
   }


   // Add a group to this page.  The group list is sorted by group number.

   void addGroup(Group g)
   {
   	if (groups == null) groups = new Vector() ;
      groups.addElement(g) ;
      Collections.sort(groups) ;
   }

	// Set the default multipalette for this set.  This is the palette
	// number on the page set declaration in the configuration file.
	
	void setMultiPalette(Integer p)	{ multipalette = p ; }
	
	// Set the initial positions for the group objects.  Group positions
	// are referenced using the group number as an index into the 
	// position vector.
	
	void addPosition(Point position, int n)
	{
      if (n < 0) return ;
   	if (positions.size() <= n) positions.setSize(n+1) ;
		positions.setElementAt(position,n) ;
	}
	
	// The configuration file line number showing where this object was 
	// first declared is used for diagnostic output messages. 
	
	void setLine(int l) { if (line == 0) line = l ; }

   // Set the page changed flag, if positions have been adjusted.

   void setChanged(boolean b) { changed = b ; }

   // Set the page visibility flag.

   void setVisible(boolean b) { visible = b ; }

	// Set the initial multipalette for this page.

	void setInitialMultiPalette(Integer multipalette)
	{
		State sv = (State) State.getByKey(getID(),this,"initial") ;
		if (sv == null) return ;
		sv.variable[1] = multipalette ;
	}

	
	
	// Object state reference methods
	// ------------------------------

	// Return a specific group.

	Group getGroup(int i)
	{ return (i >= groups.size()) ? null : (Group) (groups.elementAt(i)) ; }

	// Return all the groups on this page.

	Vector getGroups() { return groups ; }

	// Return all the visible groups on this page.

	Vector getVisibleGroups() 
   { 
      Vector v = new Vector() ;
      for (int i = 0; i < groups.size() ; i++)
      {
         Group g = (Group) groups.elementAt(i) ;
         if (!g.isVisible()) continue ;
         v.addElement(g) ;
      }
      return v ; 
   }

	// Return all the visible cels on this page.  

	Vector getVisibleCels() 
   { 
      Vector v = new Vector() ;
      for (int i = 0; i < groups.size() ; i++)
      {
         Group g = (Group) groups.elementAt(i) ;
         Vector cels = g.getCels() ;
         for (int j = 0; j < cels.size() ; j++)
         {
            Cel c = (Cel) cels.elementAt(j) ;
            if (!c.isVisible()) continue ;
            v.add(c) ;
         }
      }
      return v ; 
   }

	// Return all the fully visible cels on this page.  Transparent or partly
   // transparent cels are not fully visible.

	Vector getFullyVisibleCels() 
   { 
      Vector v = new Vector() ;
      for (int i = 0; i < groups.size() ; i++)
      {
         Group g = (Group) groups.elementAt(i) ;
         Vector cels = g.getCels() ;
         for (int j = 0; j < cels.size() ; j++)
         {
            Cel c = (Cel) cels.elementAt(j) ;
            if (!c.isVisible()) continue ;
            if (c.getTransparency() > 0) continue ;
            v.add(c) ;
         }
      }
      return v ; 
   }

   // Return the number of groups on this page.

	int getGroupCount() { return groups.size() ; }

	// Return the group position on this page.

	Point getGroupPosition(Integer group)
	{
		if (group == null) return null ;
		int g = group.intValue() ;
		if (g < 0 || g >= positions.size()) return null ;
      Object o = positions.elementAt(g) ;
      if (!(o instanceof Point)) return null ;
		return new Point((Point) o) ;
	}

	// Return the initial group position on this page.

	Point getInitialGroupPosition(Integer group)
	{
		if (group == null) return null ;
		String state = "initial" ;
		State sv = (State) State.getByKey(getID(),this,state) ;
		if (sv == null) return getGroupPosition(group) ;
		Object o = sv.variable[0] ;
		if (!(o instanceof Vector)) return getGroupPosition(group) ;
		Vector initpositions = (Vector) o ;
		int g = group.intValue() ;
		if (g < 0 || g >= initpositions.size()) return getGroupPosition(group) ;
      o = initpositions.elementAt(g) ;
      if (!(o instanceof Point)) return null ;
		return new Point((Point) o) ;
	}

	// Set the group position on this page.

	void setGroupPosition(Integer group, Point p)
	{
		if (group == null) return ;
		int g = group.intValue() ;
      if (g < 0) return ;
		if (g >= positions.size()) positions.setSize(g+1) ;
		positions.setElementAt(p,g) ;
	}

	// Set the initial group position on this page.  If there is no initial
	// state one will be created.

	void setInitialGroupPosition(Integer group, Point p)
	{
		if (group == null) return ;
		String state = "initial" ;
		State sv = (State) State.getByKey(getID(),this,state) ;
		if (sv == null)
		{
			sv = new State(getID(),this,state,2) ;
			sv.variable[0] = new Vector() ;
			sv.variable[1] = new Integer(0) ;
		}
		Object o = sv.variable[0] ;
		if (!(o instanceof Vector)) return ;
		Vector initpositions = (Vector) o ;
		int g = group.intValue() ;
      if (g < 0) return ;
		if (g >= initpositions.size()) initpositions.setSize(g+1) ;
		initpositions.setElementAt(p,g) ;
		return ;
	}

	// Determine if the specified group is on this page.

	boolean contains(Group g) { return groups.contains(g) ; }

	// Return the page default palette.

	Integer getMultiPalette() { return multipalette ; }

	// Return the initial multipalette for this page.

	Integer getInitialMultiPalette()
	{
		State sv = (State) State.getByKey(getID(),this,"initial") ;
		if (sv == null) return getMultiPalette() ;
		Object o = sv.variable[1] ;
		if (!(o instanceof Integer)) return getMultiPalette() ;
		return (Integer) o ;
	}

	// Return the page object positions.

	Vector getPositions() { return positions ; }

	// Return the initial page object positions.

	Vector getInitialPositions()
	{
		String state = "initial" ;
		State sv = (State) State.getByKey(getID(),this,state) ;
		if (sv == null) return getPositions() ;
		Object o = sv.variable[0] ;
		if (!(o instanceof Vector)) return getPositions() ;
		return (Vector) o ;
	}

   // Return our page changed indicator.

   boolean isChanged() { return changed ; }

   // Return our page visibility indicator.

   boolean isVisible() { return visible ; }



	// Utility methods
	// ---------------

	// Initialize the set.  Groups must be moved to their starting 
	// positions on this page and initialized to the required context.

   void init()
	{
      MainFrame mf = Kisekae.getMainFrame() ;
      PanelFrame panel = (mf != null) ? mf.getPanel() : null ;
      Hashtable eventstate = new Hashtable(10,1.0f) ;

      // Process all groups on this page.  Retain the list of objects that
      // are moving and their collision state so that collision events can
      // be fired after all objects are placed.

      Vector moved = new Vector() ;
		for (int i = 0 ; i < groups.size() ; i++)
		{
         Integer page = (Integer) getIdentifier() ;
			Group g = (Group) groups.elementAt(i) ;
			if (!(g.isOnPage(page))) continue ;

         // Identify the initial object collision state.

         if (panel != null) panel.setCollisionState(g,eventstate,true,true) ;

			// Identify the required group position on this page.  If
			// a group is not within the set then the group will be
			// placed at the origin.

			int id = ((Integer) g.getIdentifier()).intValue() ;
			Point p = (id < positions.size()) ?
				(Point) positions.elementAt(id) : null ;
			if (p == null) p = new Point(0,0) ;

			// Get the current group location and compute the relative
			// placement offset necessary to move the group to the required
			// position.

         g.setContext(page) ;
			Rectangle box = g.getBoundingBox() ;
			Point offset = g.getOffset() ;
			Point location = new Point(box.x-offset.x,box.y-offset.y) ;

         // Apply the displacement offset.

         int dispX = p.x - location.x ;
         int dispY = p.y - location.y ;
			g.setPlacement(dispX,dispY) ;
			g.drop() ;

         // Remember any collision events if the object moved.

         if (dispX != 0 || dispY != 0) moved.add(g) ;
		}

      // We now check for restrictions.  Any object that has a restriction
      // must be moved to fit within the restriction limits.  Attached objects
      // with restrictions are moved with the parent restricted group.

		for (int i = 0 ; i < groups.size() ; i++)
		{
         Integer page = (Integer) getIdentifier() ;
			Group g = (Group) groups.elementAt(i) ;
			if (!(g.isOnPage(page))) continue ;
         if (g.hasParent()) continue ;
         Point restrictx = g.getRestrictX() ;
         Point restricty = g.getRestrictY() ;
         if (restrictx == null && restricty == null) continue ;

         // We have a restricted object.  Construct a consolidated
         // object that contains all children.

         Group groupset = new Group() ;
         groupset.setPrimaryGroup(g) ;
         groupset.setInternal(true) ;
         groupset.addElement(g) ;
			Point location = groupset.getLocation() ;
         int x = location.x ;
         int y = location.y ;

         // Restrict the movement offset if necessary.

         if (restrictx != null && restrictx.x <= restrictx.y)
         {
            if (x < restrictx.x) x = restrictx.x ;
            if (x > restrictx.y) x = restrictx.y ;
         }
         if (restricty != null && restricty.x <= restricty.y)
         {
            if (y < restricty.x) y = restricty.x ;
            if (y > restricty.y) y = restricty.y ;
         }

         // Apply the displacement offset.

         int dispX = x - location.x ;
         int dispY = y - location.y ;
			groupset.setPlacement(dispX,dispY) ;
			groupset.drop() ;

         // Remember any collision events if the object moved.

         if (dispX != 0 || dispY != 0)
            if (!moved.contains(g)) moved.add(g) ;
		}

      // Fire any collision events.

      if (OptionsDialog.getCompatibilityMode() == null)
      {
         if (panel != null && moved.size() > 0)
         {
            for (int i = 0 ; i < moved.size() ; i++)
            {
               Object o = moved.elementAt(i) ;
               panel.fireCollisionEvents(o,eventstate,Thread.currentThread(),null) ;
            }
         }
      }
	}


	// Save the page set state.  This requires that we compute the 
	// current positions of all groups and construct a new page set
	// position list.  
	
	void saveState(Object cid, Object state)
	{
		State sv = new State(cid,this,state,2) ;
		sv.variable[0] = (Vector) positions.clone() ;
		sv.variable[1] = new Integer(multipalette.intValue()) ;
		if ("initial".equals(state.toString())) return ;

		// Compute the group positions.  If this is a new page initial
		// positions may not be defined for all groups.  We create new
		// position entries where necessary.

		for (int i = 0 ; i < groups.size() ; i++)
		{
			Group g = (Group) groups.elementAt(i) ;
			int id = ((Integer) g.getIdentifier()).intValue() ;
			Rectangle r = g.getBoundingBox() ;
			Point p = g.getOffset() ;
			Point q = new Point(r.x-p.x,r.y-p.y) ;
			try { ((Vector) sv.variable[0]).setElementAt(q,id) ; }
			catch (ArrayIndexOutOfBoundsException e)
			{
				((Vector) sv.variable[0]).setSize(id+1) ;
				((Vector) sv.variable[0]).setElementAt(q,id) ;
			}
		}
	}
	
	
	// Restore the page set state.  For a complete restoration we must
	// restore the position of all groups.   If pages are scenes then page 0 
   // is the position of record for all object movement and restoring 
   // of object positions.
	
	void restoreState(Object cid, Object state)
   { restoreState(cid,state,false) ; }

	void restoreState(Object cid, Object state, boolean restorevisibility)
	{
      Integer id = (Integer) this.getIdentifier() ;
		State sv = (State) State.getByKey(cid,this,state) ;
      PageSet p0 = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,new Integer(0)) ;
      State sv0 = (State) State.getByKey(cid,p0,state) ;
      if (OptionsDialog.getPagesAreScenes() && "panelframe".equals(state)) sv = sv0 ;
		if (sv != null)
		{
			positions = (sv.variable[0] instanceof Vector)
				? (Vector) sv.variable[0] : new Vector() ;
			multipalette = (sv.variable[1] instanceof Integer)
				? (Integer) sv.variable[1] : new Integer(0) ;
		}
		
		// Now restore the groups on this page.  This restores all 
		// states except visibility.
		
		for (int i = 0 ; i < positions.size() ; i++)
		{
			if (positions.elementAt(i) == null) continue ;
			Group g = (Group) Group.getByKey(Group.getKeyTable(),cid,new Integer(i)) ;
         if (g == null) continue ;
         if (OptionsDialog.getPagesAreScenes() && id.intValue() != 0 && !g.isOnSpecificPage(id)) continue ;
			g.restoreState(cid,state,restorevisibility) ;
		}
	}
	

	// Set the page set state.  This function is used to predefine the state
	// variables in preparation for a state restoration.  It is used to
	// initialize object positions in the event that a page set is restored.

	void setState(Object cid, Object state, State sv)
	{
		if (sv == null || sv.getSize() < 2) return ;
		setState(cid,state,sv.variable[0],sv.variable[1]) ;
	}

	void setState(Object cid, Object state, Object positions, Object mp)
	{
		State sv = new State(cid,this,state,2) ;
		if (!(positions instanceof Vector)) positions = new Vector() ;
		if (!(mp instanceof Integer)) mp = new Integer(0) ;
		sv.variable[0] = positions ;
		sv.variable[1] = mp ;
		return ;
	}

	// Return a requested page set state.  This function is used to retrieve
	// the page set state object.

	State getState(Object cid, Object state)
	{
		State sv = (State) State.getByKey(cid,this,state) ;
		return sv ;
	}

	// Clear a requested page set state.  This function is used to remove
	// state objects from the state table.

	void removeState(Object cid, Object state)
	{
		State.removeByKey(cid,this,state) ;
	}

   // Assign initial positions for all group objects on this page that are
   // internal groups and not cloned.  These groups were newly created.
   // This function returns the number of updates performed.  Note, initial
   // positions for pages are not updated if we are using pages as scenes.  

   int updateInitialPositions(Object cid)
   {
   	int updates = 0 ;
		State initstate = getState(cid,"initial") ;
      if (initstate == null) return -1 ;
      if (!(initstate.variable[0] instanceof Vector)) return -1 ;
      if (OptionsDialog.getPagesAreScenes()) return -1 ;
      Vector ip = (Vector) initstate.variable[0] ;

		// Compute the group positions.  If this is a new page initial
		// positions may not be defined for all groups.  We create new
		// position entries only if such entries do not already exist
      // or the group has been pasted.

		for (int i = 0 ; i < groups.size() ; i++)
		{
			Group g = (Group) groups.elementAt(i) ;
         if (g.isCloned()) continue ;
         if (!g.isInternal() && !g.isUpdated()) continue ;
			int id = ((Integer) g.getIdentifier()).intValue() ;
			Rectangle r = g.getBoundingBox() ;
			Point p = g.getOffset() ;
			Point q = new Point(r.x-p.x,r.y-p.y) ;
         
         // Determine the group location on the panel frame. Imported
         // objects that have been moved should have initial positions
         // set to their current location.
         
         State sv = getState(cid,"panelframe") ;
         if (sv != null)
         {
            positions = (sv.variable[0] instanceof Vector)
               ? (Vector) sv.variable[0] : new Vector() ;
            if (id < positions.size() && !g.isUpdated()) 
               q = (Point) positions.elementAt(id) ;
         }
         
         // Set the initial position.
         
			try { ip.setElementAt(q,id) ; }
			catch (ArrayIndexOutOfBoundsException e)
			{
				ip.setSize(id+1) ;
				ip.setElementAt(q,id) ;
			}
         updates++ ;
		}

      // Update if we had changes.

      if (updates > 0) setState(cid,"initial",initstate) ;
      return updates ;
   }


	// KiSS object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }
}
