package com.wmiles.kisekaeultrakiss.Kisekae ;

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
* ListDragSource class
*
* Purpose:
*
* This class implements drag and drop operations on a JList.  This is the
* source JList event handling module.  The selected item is recognized
* and removed from the list on successful completion of a drag and drop
* operation.
*
*/

import java.awt.* ;
import java.awt.dnd.* ;
import java.awt.datatransfer.* ;
import java.awt.event.* ;
import java.io.* ;
import java.util.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.DefaultListModel ;


public class ListDragSource
    implements DragSourceListener, DragGestureListener, ListSelectionListener
{
	private DragSource source = null ;
   private DragGestureRecognizer recognizer = null ;
   private Object selectobject = null ;
   private JList sourcelist = null ;
   private boolean enabledrag = false ;

   // Drag Callback

	protected CallbackButton callback = new CallbackButton(this,"Drag Callback") ;


	// Constructor

	public ListDragSource(JList list, int actions)
   {
   	sourcelist = list ;
		source = new DragSource() ;
		recognizer = source.createDefaultDragGestureRecognizer(list,actions,this) ;
      sourcelist.addListSelectionListener(this) ;
	}

	// Drag Event Handlers

	public void dragGestureRecognized(DragGestureEvent event)
   {
		if (selectobject instanceof Transferable && enabledrag)
      {
			event.startDrag(DragSource.DefaultMoveDrop,(Transferable) selectobject,this) ;
		}
	}

	public void dragEnter(DragSourceDragEvent event) { }

	public void dragExit(DragSourceEvent event) { }

	public void dragOver(DragSourceDragEvent event) { }

	public void dropActionChanged(DragSourceDragEvent event) { }

   public void dragDropEnd(DragSourceDropEvent event)
   {
   	if (event.getDropSuccess())
      {
      	// Only remove the element if the drop was successful.
			(( DefaultListModel) sourcelist.getModel()).removeElement(selectobject) ;
         callback.doClick() ;
      }
   }

   // A utility method to return and clear our selection object.

   public Object getSelection() { return selectobject ; }
   public void clearSelection()
   {
    	sourcelist.removeListSelectionListener(this) ;
      sourcelist.clearSelection() ;
      sourcelist.addListSelectionListener(this) ;
   	selectobject = null ;
   }


   // ListSelectionListener interface methods.  The selected value can change
   // concurrently with the drag operation commencing.  We need to ensure that
   // the original selection is the item dragged.

   public void valueChanged(ListSelectionEvent e)
   {
 		if (sourcelist == null) return ;
      Object o = sourcelist.getSelectedValue() ;
      if (selectobject != null && sourcelist.getValueIsAdjusting())
      {
      	sourcelist.removeListSelectionListener(this) ;
         sourcelist.setSelectedValue(selectobject,false) ;
	      sourcelist.addListSelectionListener(this) ;
      }
		selectobject = sourcelist.getSelectedValue() ;
      enabledrag = (o == selectobject) ;
	}
}
