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
* ListEntry class
*
* Purpose:
*
* The ListEntry is a drag and drop list object.  It contains an object
* reference, a level number, and a string for display.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.dnd.* ;
import java.awt.datatransfer.* ;
import java.util.* ;
import javax.swing.* ;
import javax.swing.border.*;


public class ListEntry extends JLabel
	implements Transferable, ListCellRenderer
{
	private Object object = null ;
   private String text = null ;
   private Integer level = null ;
   private Border selectborder = null ;
   private Border unselectborder = null ;
   private boolean selected = false ;

   public static DataFlavor LISTENTRYFLAVOR =
   	new DataFlavor(JLabel.class, "ListEntry") ;
   private DataFlavor flavors[] = { LISTENTRYFLAVOR } ;


	// Constructor

   public ListEntry() { this(null,null,null) ; }
   public ListEntry(String s) { this(null,null,s) ; }
	public ListEntry(Object o, Integer n, String s)
   {
   	super() ;
      object = o ;
      level = n ;
      text = (s != null) ? s : "" ;
		selectborder = new ListBorder(Color.red) ;
      unselectborder = new EmptyBorder(0,0,0,0) ;
      setText(text) ;
      setOpaque(true);
   }


   // Selection methods for this object.

   public void setSelected(boolean b) { selected = b ; }
   public boolean isSelected() { return selected ; }

   // Return the associated object.

   public Object getObject() { return object ; }

   // Return the associated level.

   public Integer getLevel() { return level ; }

   // Set the associated level.  Update the ListEntry text field that
   // begins with the level value.

   public void setLevel(Integer n)
   {
      level = n ;
      if (text == null || text.length() < 4) return ;
      String s = (n != null) ? n.toString().trim() : "" ;
      StringBuffer sb = new StringBuffer() ;
      for (int i = 4 ; i > s.length() ; i--) sb.append(' ') ;
      sb.append(s) ;
      text = sb.toString() + text.substring(4) ;
   }

	// The toString method returns a string representation of this object.

	public String toString() { return text ; }

	// The setString method sets the string representation of this object.

	public void setString(String s) { text = s ; }


   // ListCellRenderer interface methods.

   public Component getListCellRendererComponent(JList list,
   	Object value, int index, boolean isSelected, boolean cellHasFocus)
   {
   	setText(value.toString()) ;
      setFont(list.getFont()) ;
   	setBackground(isSelected ? list.getSelectionBackground() : list.getBackground()) ;
      setForeground(isSelected ? list.getSelectionForeground() : list.getForeground()) ;
   	if (!(value instanceof ListEntry)) return this ;
      if (((ListEntry) value).isSelected())
      	setBorder(selectborder) ;
      else
      	setBorder(unselectborder) ;
      return this ;
   }


   // Transferable interface methods.

   public synchronized DataFlavor[] getTransferDataFlavors()
   { return flavors ; }

   public boolean isDataFlavorSupported(DataFlavor flavor)
   {	return (flavor.getRepresentationClass() == JLabel.class) ; }

   public synchronized Object getTransferData(DataFlavor flavor)
   	throws UnsupportedFlavorException, IOException
   {
   	if (isDataFlavorSupported(flavor))
      	return this ;
      else
      	throw new UnsupportedFlavorException(flavor) ;
   }


   // Inner class to define our selection border.

   class ListBorder extends AbstractBorder
   {
   	Color linecolor = null ;

      // Constructor

      public ListBorder() { this(Color.black) ; }
   	public ListBorder(Color c) { linecolor = c ; }

      // Paint method

      public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
      {
      	g.setColor(linecolor) ;
         g.drawLine(x,y,x+w,y) ;
      }
   }
}



