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
* ListDropTarget class
*
* Purpose:
*
* This class implements drag and drop operations on a JList.  This is the
* target JList event handling module.  ListEntry objects that are recognized
* as potential drop targets are selected for highlights during mouse movement.
* The last selected list item is added to the list on successful competion
* of the drag and drop operation.
*
*/

import java.awt.* ;
import java.awt.dnd.* ;
import java.awt.datatransfer.* ;
import java.awt.event.* ;
import java.io.*;
import java.util.* ;
import javax.swing.* ;
import javax.swing.undo.* ;
import javax.swing.event.* ;
import javax.swing.DefaultListModel ;


public class ListDropTarget
    implements DropTargetListener
{
	private DropTarget target = null ;
   private ListDragSource source = null ;
   private JList targetlist = null ;
   private UndoManager undomanager = null ;
   private int lastselected = -1 ;


	// Constructor

	public ListDropTarget(JList list, ListDragSource source, UndoManager undo)
   {
		targetlist = list ;
      this.source = source ;
      undomanager = undo ;
      target = new DropTarget(list,this) ;
	}

	// Drop Event Handlers.

	public void dragEnter(DropTargetDragEvent event)
   {
   	Object o = null ;
		int n = targetlist.locationToIndex(event.getLocation()) ;
      ListModel model = targetlist.getModel() ;
      try { o = model.getElementAt(n) ;  }
      catch (ArrayIndexOutOfBoundsException e) { }
      if (o instanceof ListEntry)
      	((ListEntry) o).setSelected(true) ;
      lastselected = n ;
      targetlist.repaint() ;
   }

	public void dragExit(DropTargetEvent event)
   {
   	Object o = null ;
      ListModel model = targetlist.getModel() ;
      try { o = model.getElementAt(lastselected) ;  }
      catch (ArrayIndexOutOfBoundsException e) { }
      if (o instanceof ListEntry)
      	((ListEntry) o).setSelected(false) ;
      lastselected = -1 ;
      targetlist.repaint() ;
   }

	public void dragOver(DropTargetDragEvent event)
   {
   	Object o1 = null ;
   	Object o2 = null ;
		int n = targetlist.locationToIndex(event.getLocation()) ;
      ListModel model = targetlist.getModel() ;
      try { o1 = model.getElementAt(n) ;  }
      catch (ArrayIndexOutOfBoundsException e) { }
      try { o2 = model.getElementAt(lastselected) ;  }
      catch (ArrayIndexOutOfBoundsException e) { }
      if (o2 instanceof ListEntry)
      	((ListEntry) o2).setSelected(false) ;
      if (o1 instanceof ListEntry)
      	((ListEntry) o1).setSelected(true) ;
      lastselected = n ;
      targetlist.repaint() ;
   }

	public void dropActionChanged(DropTargetDragEvent event ) { }

	public void drop(DropTargetDropEvent event)
   {
		try
      {
			Transferable transferable = event.getTransferable() ;
         DataFlavor[] flavors = transferable.getTransferDataFlavors() ;
        	DefaultListModel model = (DefaultListModel) targetlist.getModel() ;
         for (int i = 0 ; i < flavors.length ; i++)
         {
         	if (transferable.isDataFlavorSupported(flavors[i]))
	         {
					event.acceptDrop(DnDConstants.ACTION_MOVE) ;
					Object o = transferable.getTransferData(flavors[i]) ;
               int n1 = targetlist.getSelectedIndex() ;
               Object draglocator = (n1 == 0) ? null : model.getElementAt(n1-1) ;
               int n2 = targetlist.locationToIndex(event.getLocation()) ;
					model.add(n2,o) ;
               targetlist.clearSelection() ;
					event.dropComplete(true) ;

               // Ensure that no elements remain selected in the list.

               Enumeration enum1 = model.elements() ;
               while (enum1.hasMoreElements())
               {
                  Object entry = enum1.nextElement() ;
                  if (entry instanceof ListEntry) ((ListEntry) entry).setSelected(false) ;
               }

               // Create an undo object.

               Object droplocator = (n2 == 0) ? null : model.getElementAt(n2-1) ;
               Integer newlevel = (droplocator instanceof ListEntry)
                  ? ((ListEntry) droplocator).getLevel() : null ;
               if (newlevel == null) newlevel = Integer.valueOf(-1) ;
               newlevel = Integer.valueOf(newlevel.intValue() + 1) ;
               UndoableDropEdit de = new UndoableDropEdit(o,draglocator,droplocator,newlevel) ;
					UndoableEditEvent evt = new UndoableEditEvent(this,de) ;
					if (undomanager != null) undomanager.undoableEditHappened(evt) ;

               // Set the level of the dropped item.

               if (o instanceof ListEntry)
                  ((ListEntry) o).setLevel(newlevel) ;
               return ;
				}
         }
			event.rejectDrop();
		}
		catch (Exception exception)
      {
			PrintLn.printErr("ListDropTarget: Exception " + exception.getMessage());
			exception.printStackTrace();
			event.rejectDrop();
		}
	}


	// Inner class to construct an undoable edit operation.
	// These edits occur if we accept a drop operation.

	class UndoableDropEdit extends AbstractUndoableEdit
   {
      private Object dropobject = null ;
      private Object draglocator = null ;
      private Object droplocator = null ;
      private Integer olddroplevel = null ;
      private Integer newdroplevel = null ;


		// Constructor for cel layer adjustment changes

		public UndoableDropEdit(Object dropobject, Object drag, Object drop, Integer n)
      {
      	this.dropobject = dropobject ;
         this.draglocator = drag ;
         this.droplocator = drop ;
         this.newdroplevel = n ;
         if (dropobject instanceof ListEntry)
            olddroplevel = ((ListEntry) dropobject).getLevel() ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoLayerName") ;  }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         try
         {
         	DefaultListModel model = (DefaultListModel) targetlist.getModel() ;
            for (int i = 0 ; i < model.size() ; i++)
            {
               if (!model.elementAt(i).toString().equals(dropobject.toString()))
                  continue ;
               model.removeElementAt(i) ;
               break ;
            }
            int n = (draglocator == null) ? -1 : model.indexOf(draglocator) ;
         	model.add(n+1,dropobject) ;
            source.clearSelection() ;
            targetlist.setSelectedValue(dropobject,true) ;
            if (dropobject instanceof ListEntry)
               ((ListEntry) dropobject).setLevel(olddroplevel) ;
         }
         catch (ArrayIndexOutOfBoundsException e) { }
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         try
         {
         	DefaultListModel model = (DefaultListModel) targetlist.getModel() ;
            for (int i = 0 ; i < model.size() ; i++)
            {
               if (!model.elementAt(i).toString().equals(dropobject.toString()))
                  continue ;
               model.removeElementAt(i) ;
               break ;
            }
            int n = (droplocator == null) ? -1 : model.indexOf(droplocator) ;
         	model.add(n+1,dropobject) ;
            source.clearSelection() ;
            targetlist.setSelectedValue(dropobject,true) ;
            source.clearSelection() ;
            if (dropobject instanceof ListEntry)
               ((ListEntry) dropobject).setLevel(newdroplevel) ;
         }
         catch (ArrayIndexOutOfBoundsException e) { }
		}
	}
}
