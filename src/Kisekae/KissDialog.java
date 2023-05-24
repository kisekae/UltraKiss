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
* KissDialog class
*
* Purpose:
*
* This class is an abstract class for all KiSS dialogs.  It maintains
* the dialog hierarchy and page context variables for each dialog.
*
* KiSS dialogs that display cels, groups, palettes and page sets extend
* this class.  Each object must provide specific implementations for
* the common methods.
*
*/

import java.awt.* ;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.Vector ;
import java.util.Enumeration ;

abstract class KissDialog extends JDialog
{
	// Class attributes

	private static StringBuffer sb = new StringBuffer() ;

   // Object attributes

	protected Object parent = null ;
   private PageSet pagecontext = null ;
   private PanelFrame panelcontext = null ;
   private Object objectcontext = null ;

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the dialog is closed.

	protected CallbackButton callback = new CallbackButton(this,"KissDialog Callback") ;

	// Constructor

   public KissDialog(JDialog f, String s, boolean b)
   { super(f,s,b) ; parent = f ; }

   public KissDialog(JFrame f, String s, boolean b)
   { super(f,s,b) ; parent = f ; }


   // Close method to dispose of this dialog and all parent dialogs.
   // The parent JFrame is disposed if it is not a KissFrame.

   void close()
   {
      pagecontext = null ;
      panelcontext = null ;
      objectcontext = null ;
      callback.doClick() ;
      dispose() ;
   	if (parent instanceof KissDialog)
      	((KissDialog) parent).close() ;
   	else if (parent instanceof JFrame)
         if (parent instanceof KissFrame)
         	((KissFrame) parent).setVisible(true) ;
         else
         	((JFrame) parent).dispose() ;
      callback.removeActionListener(null) ;
      parent = null ;
   }


	// Method to get the parent frame of this dialog.

	JFrame getParentFrame()
	{
		if (parent instanceof JFrame)
			return (JFrame) parent ;
		if (parent instanceof KissDialog)
			return ((KissDialog) parent).getParentFrame() ;
		return null ;
	}


	// Method to return the size of the parent frame of this dialog
   // if it is a KissFrame, otherwise the screen size.

	Dimension getParentSize()
	{
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
      JFrame f = getParentFrame() ;
      if (f instanceof KissFrame) d = f.getSize() ;
		return d ;
	}

	// Method to return the size of the parent frame of this dialog
   // if it is a KissFrame, otherwise the screen size.

	Point getParentLocation()
	{
      JFrame f = getParentFrame() ;
      if (f == null) return new Point(0,0) ;
      return f.getLocation() ;
	}

	// Method to center a window within our parent.  We
   // ensure that the window does not exceed the screen
   // limits.
   
   void center(Window w)
   {
      Dimension s = w.getSize() ;
      Dimension d = getParentSize() ;
      Dimension ss = Toolkit.getDefaultToolkit().getScreenSize() ;
		Point p = getParentLocation() ;
		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
      int cx = p.x + x ;
      int cy = p.y + y ;
      if ((cx+s.width) > ss.width) cx = ss.width - (cx+s.width) ;
      if ((cy+s.height) > ss.height) cy = ss.height - (cy+s.height) ;
      if (cx < 0) cx = 0 ;
      if (cy < 0) cy = 0 ;
		w.setLocation(cx,cy) ;
   }


   // Method to traverse the owned window chain to retrieve the
   // lowest level visible dialog.

   KissDialog getVisibleDialog()
   {
   	if (isVisible()) return this ;
      Window [] windows = getOwnedWindows() ;
      if (windows == null) return null ;
     	for (int i = 0 ; i < windows.length ; i++)
      {
        	if (windows[i] instanceof KissDialog)
         {
         	KissDialog kd = (KissDialog) windows[i] ;
         	kd = kd.getVisibleDialog() ;
            if (kd != null) return kd ;
         }
      }
      return null ;
   }


   // Method to set the active context for dialog display.

   void setPageContext(PageSet p) { pagecontext = p ; }
   void setPanelContext(PanelFrame p) { panelcontext = p ; }
   void setObjectContext(Object o) { objectcontext = o ; }


   // Method to retrieve the active pageset context.

   PageSet getPageContext()
   {
   	if (!(parent instanceof KissDialog))
      {
      	if (pagecontext == null)
         {
	      	PanelFrame pf = getPanelContext() ;
            pagecontext = (pf == null) ? null : pf.getPage() ;
         }
         return pagecontext ;
      }
   	if (pagecontext != null) return pagecontext ;
      return ((KissDialog) parent).getPageContext() ;
   }


   // Method to retrieve the active panelframe context.

   PanelFrame getPanelContext()
   {
   	if (!(parent instanceof KissDialog))
      {
         if (panelcontext == null)
         {
	      	MainFrame mf = Kisekae.getMainFrame() ;
	         panelcontext = (mf == null) ? null : mf.getPanel() ;
         }
         return panelcontext ;
      }
   	if (panelcontext != null) return panelcontext ;
      return ((KissDialog) parent).getPanelContext() ;
   }


   // Method to retrieve the active object context.

   Object getObjectContext()
   {
   	if (!(parent instanceof KissDialog))
         return objectcontext ;
   	if (objectcontext != null)
         return objectcontext ;
      return ((KissDialog) parent).getObjectContext() ;
   }


   // Method to update the dialog preview image.   This method should be
   // overloaded for any dialog that can perform preview image updates.

   void updatePreview() { }


   // Abstract methods that all objects of this type must implement.

   abstract void setValues() ;


   // A function to append FKissEvent objects to a list.  This function is
   // used by many dialog objects that inherit from this class.

   static protected void appendevents(Vector list, Enumeration e)
   {
   	if (e == null) return ;
      while (e.hasMoreElements())
      {
			Vector events = (Vector) e.nextElement() ;
         for (int i = 0 ; i < events.size() ; i++)
			{
           	Object o = events.elementAt(i) ;
            if (o instanceof FKissEvent) list.addElement(o) ;
         }
      }
   }


	// A function to format a text string.  Command values are "clear",
   // "right", "left" and "center".  A destination string of the requested
   // length is constructed.  The source string is accessed from the starting
   // index and characters are extracted from the source string and stored
   // into the destination string according to the required format.
   // The destination string is returned.

	static protected String format(String command, Object text, int start, int length)
	{
		int i, j ;

      if (text == null) return sb.toString() ;
      String value = text.toString() ;

		// Set string to have the initial value.

      if ("clear".equals(command))
      {
         sb.setLength(length) ;
         j = start ;
         for (i = 0 ; i < value.length() ; i++)
         {
            if (j >= sb.length()) break ;
            sb.setCharAt(j++, value.charAt(i)) ;
         }
         for (j = i ; j < sb.length() ; j++)
            sb.setCharAt(j, ' ') ;
      }

		// Right align the value in the string.

      else if ("right".equals(command))
      {
			j = value.length() - 1 ;
			if (start+length <= sb.length())
         {
   			for (i = start+length-1 ; i >= start ; i--)
   			{
               if (j < 0) break ;
               sb.setCharAt(i, value.charAt(j--)) ;
            }
			}
      }

		// Left align the value in the string.

      else if ("left".equals(command))
      {
			j = 0 ;
			for (i = start ; i < start+length ; i++)
			{
				if (j >= value.length()) break ;
				if (i >= sb.length()) break ;
				sb.setCharAt(i,value.charAt(j++)) ;
			}
		}

      // Center the value in the string.

      else if ("center".equals(command))
      {
         j = value.length() - 1 ;
         int off = (length - value.length()) / 2 ;
         if (start+length <= sb.length())
         {
            for (i = start+length-1 ; i >= start ; i--)
            {
               if (j < 0) break ;
               sb.setCharAt(i-off, value.charAt(j--)) ;
            }
         }
      }

		// Return the result string.

		return sb.toString() ;
	}


   // Inner class to extend JTree to turn off double click node expansion.

   class noExpandTree extends JTree
   {
   	public noExpandTree() { super() ;  toggleClickCount = 0 ; }
      public noExpandTree(TreeNode n) { super(n) ; toggleClickCount = 0 ; }
   }
}