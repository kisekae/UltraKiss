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
* CelGroup class
*
* Purpose:
*
* This is a class to manage a cel group list.  Cel groups are an FKiSS 4
* extension that allows cel groups to be used in FKiSS events or actions
* anywhere that a single cel can be used.  The FKiSS event or action will
* apply to all cels in the group.
*
* Cel groups have frames that are used by the @setframe and @letframe
* action.
*
* Cel groups are KissObjects that have many cels.  Many of the standard
* KissObject methods are overloaded.
*
*/


import java.io.* ;
import java.awt.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;


class CelGroup extends KissObject
{
	// Class attributes.  Sized for 100 cel group objects.

	static private Hashtable key = new Hashtable(120,0.8f) ;

	// Cel Group attributes

	private Vector framelist = null ;			// The group frames
	private Vector cellist = null ;				// The cels in the group
	private Integer currentframe = null ;		// The current frame number
	private int transparency = 255 ;			   // The group relative transparency
   private boolean allframes = false ;       // If true, all frames are valid
   private boolean animate = false ;         // True, object can animate
   private int framedelay = 0 ;				   // Time delay until next frame
   private long frametime = 0 ;				   // Time elapsed for next frame



	// Constructor

	public CelGroup(String s)
	{
		setIdentifier(s.toUpperCase()) ;
		framelist = new Vector() ;
		cellist = new Vector() ;
		currentframe = new Integer(-1) ;
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


	// Function to find a CelGroup object by name.

	static CelGroup findCelGroup(String s, Configuration c, FKissEvent event)
	{
      if (c == null) return null ;
		if (s == null || s.length() <= 1) return null ;

		// Check for a literal or variable preceeded by '!'.   Numeric variables
      // default to a literal cel group name.

		if (s.charAt(0) == '!')
		{
			Object o = c.getVariable().getValue(s.substring(1),event) ;
         if (o instanceof Integer) o = null ;
         if (o instanceof Long) o = null ;
         if (o instanceof Double) o = null ;
         if (o == null) o = s.toUpperCase() ;
         else o = new String("!" + o.toString().toUpperCase()) ;
			return (CelGroup) CelGroup.getByKey(CelGroup.getKeyTable(),c.getID(),o) ;
		}

		// Check for a variable not preceeded by '!'.

		Object o = c.getVariable().getValue(s,event) ;
		if (o == null) return null ;
		return (CelGroup) CelGroup.getByKey(CelGroup.getKeyTable(),c.getID(),o) ;
	}

	// Return the cel group frame list.

	Vector getCelGroupFrames() { return framelist ; }

	// Return the cel group cel list.

	Vector getCels() { return cellist ; }

	// Return all the group objects in this cel group.

	Vector getGroups() 
   {
      Vector grouplist = new Vector() ;
      if (cellist == null) return grouplist ;
      for (int i = 0 ; i < cellist.size() ; i++)
      {
         Cel c = (Cel) cellist.elementAt(i) ;
         Object o = c.getGroup() ;
         if (!(o instanceof Group)) continue ;
         grouplist.addElement(o) ;
      }
      return grouplist ;
   }
   
	// Method to return the cel group name.

	String getName()
   {
      Object o = getIdentifier() ;
   	if (o == null) return null ;
      return o.toString() ;
   }

	// Return the celgroup size.

	int getCelCount() { return cellist.size() ; }

	// Return the current frame number.

	int getFrame() { return currentframe.intValue() ; }
	int getFrame(Cel c) { return cellist.indexOf(c) ; }

	// A method to return the frame count.

	int getFrameCount() { return framelist.size() ; }

	// A method to add a new frame number to this cel group.

	void addFrame(Integer frame)
	{ 
      if (!containsFrame(frame)) framelist.add(frame) ; 
   }

	// A method to add a set of frame numbers to this cel group.

	void addFrame(Vector v)
   {
      if (v == null) return ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (o instanceof Integer)
            addFrame((Integer) o) ;
      }
   }

	// A method to add a new cel to this cel group.

	void addCel(Cel cel) { cellist.add(cel) ; }

	// A method to return a cel from this cel group.

	Cel getCel(int i)
   {
   	if (cellist == null) return null ;
      if (i < 0 || i >= cellist.size()) return null ;
      Object o = cellist.elementAt(i) ;
      if (!(o instanceof Cel)) return null ;
      return (Cel) o ;
   }

	// Get the palette identifier object for all cels in the group.  If the
   // cels have different palettes, this method returns null.

	Object getPaletteID()
	{
      Object pid = null ;
   	if (cellist == null) return null ;
		for (int i = 0 ; i < cellist.size() ; i++)
      {
         Object o = cellist.elementAt(i) ;
         if (!(o instanceof Cel)) return null ;
			o = ((Cel) o).getPaletteID() ;
         if (pid == null) pid = o ;
         if (o == null) return null ;
         if (!(o.equals(pid))) return null ;
      }
      return pid ;
	}

	// Determine if this cel group contains a specific frame.

	boolean containsFrame(Integer frame)
   {
      if (allframes) return true ;
      return framelist.contains(frame) ;
   }

	// Determine if this cel group contains a specific cel.

	boolean containsCel(Cel c)
   {
      return cellist.contains(c) ;
   }

	// Set the current frame number.   All cels in this group with the required
	// frame are made visible and all cels in this group not in this frame are
	// set to nonvisible.

	void setFrame(int f)
	{
		currentframe = new Integer(f) ;
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel cel = (Cel) cellist.elementAt(i) ;
			Vector v = cel.getCelGroups() ;
			if (v == null) continue ;

			// Find this cel group in the set of groups for the cel.

			CelGroup cg = null ;
			for (int j = 0 ; j < v.size() ; j++)
			{
				cg = (CelGroup) v.elementAt(j) ;
				if (cg.getIdentifier().equals(getIdentifier())) break ;
            cg = null ;
			}

			// Set the cel visibility if the cel is in the frame.

         if (cg == null) continue ;
         boolean b = cg.containsFrame(currentframe) ;
			cel.setVisible(b) ;
		}
	}

	// Set the indicator that the cel is on all frames.

	void setAllFrames(boolean b) { allframes = b ; }

	// Return an indication if the group has any cel on the specified page.

	boolean isOnPage(Integer p)
	{
		for (int i = 0 ; i < cellist.size() ; i++)
			if (((Cel) cellist.elementAt(i)).isOnPage(p)) return true ;
		return false ;
	}



	// Overloaded standard KissObject methods for all cels in a group.
	// -----------------------------------------------------------------

	void addEvent(FKissEvent e)
	{
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			c.addEvent(e) ;
		}
	}

	// Return a consolidated bounding box for all cels in the group.

	Rectangle getBoundingBox()
	{
		Rectangle box = null ;
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			if (box == null)
				box = c.getBoundingBox() ;
			else
				box = box.union(c.getBoundingBox()) ;
		}
		return box ;
	}

	// Return a base bounding box for all cels in the group.

	Rectangle getBaseBoundingBox() { return getBoundingBox() ; }



	// Overloaded FKiSS action methods for all cels in a group.
	// -----------------------------------------------------------------

	void altVisible()
	{
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			c.altVisible() ;
		}
	}

	void setVisible(boolean b)
	{
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			c.setVisible(b) ;
		}
	}

	void setGhost(boolean b)
	{
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			c.setGhost(b) ;
		}
	}

	// Set the animation flag for this cel group. An animated cel group
   // shows all cels for a constant frame display time.

	void setAnimate(int n)
	{
      animate = (n > 0) ;
      setInterval(n) ;
      setTime(0) ;
      if (!animate) return ;
      
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
      
      // Add this cel group to the animation queue.
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

   // Animation specific methods.

   boolean getAnimate() { return animate ; }
   
   int getInterval() { return framedelay ; }

   void setInterval(int t) { framedelay = t ; }

   long getTime() { return frametime ; }

   void setTime(long t) { frametime = t ; }
   
   // Cel group transparency is -1 if not all cels in the group have
   // the same transparency.  Otherwise, it is the common transparency
   // of all cels in the group.

	int getTransparency()
	{
		int transparent = -1 ;
      if (OptionsDialog.getTransparentGroup()) return transparency ; 
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			int t = c.getTransparency() ;
			if (transparent < 0) transparent = t ;
			if (t != transparent) return -1 ;
		}
		return transparent ;
	}

   // Changes to transparency are relative.

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
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			c.changeTransparency(t,bound,ambiguous) ;
		}
	}

	// Set the palette for all cels in the group.

	void changePaletteID(Integer n)
	{
		for (int i = 0 ; i < cellist.size() ; i++)
			((Cel) cellist.elementAt(i)).changePaletteID(n) ;
	}

   // Method to fix the cel multipalette group.  This is done through 
   // the cel CNF configuration or through a setpal() command.  A cel
   // with a fixed palette group does not participate in color set changes
   // to a different palette group.

   void fixPaletteGroup(Integer n) 
   { 
		for (int i = 0 ; i < cellist.size() ; i++)
			((Cel) cellist.elementAt(i)).fixPaletteGroup(n) ;
   }

   // A cel group is visible if any cel in the group is visible.

	boolean isVisible()
	{
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
			if (c.isVisible()) return true ;
		}
		return false ;
	}

   // Get the visible cel count on the specified page.

	int getVisibleCelCount(Integer page)
	{
      int n = 0 ;
		for (int i = 0 ; i < cellist.size() ; i++)
		{
			Cel c = (Cel) cellist.elementAt(i) ;
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

	// KiSS object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }


   // The toString method returns a string representation of this object.
	// CelGroup names are variables so they can be referenced in FKiSS events
	// and actions. The variable value is the CelGroup object.  The toString
	// method returns the variable name.

	public String toString()
	{
		Object o = getIdentifier() ;
		if (o == null) o = new String("") ;
		return o.toString() ;
	}
}

