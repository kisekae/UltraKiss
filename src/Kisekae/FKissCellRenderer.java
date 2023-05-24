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
* FKissCellRenderer class
*
* Purpose:
*
* This class renders an FKiSS event or action object placed in a tree.
* The object color is changed to show breakpoint states and selection
* states.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import javax.swing.* ;
import javax.swing.tree.* ;


final class FKissCellRenderer extends JLabel
	implements TreeCellRenderer
{
	private Color textSelectionColor = null ;
   private Color textNonSelectionColor = null ;
   private Color textDisabledColor = null ;
   private Color bkSelectionColor = null ;
   private Color bkNonSelectionColor = null ;
   private Color borderSelectionColor = null ;
   private boolean selected = false ;

   // Default constructor

   public FKissCellRenderer()
   {
   	super() ;
      textSelectionColor = UIManager.getColor("Tree.selectionForeground") ;
		textNonSelectionColor = UIManager.getColor("Tree.textForeground") ;
		textDisabledColor = Color.lightGray ;
		bkSelectionColor = UIManager.getColor("Tree.selectionBackground") ;
		bkNonSelectionColor = UIManager.getColor("Tree.textBackground") ;
		borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor") ;
      setOpaque(false) ;
   }


   // TreeCellRenderer interface

   public Component getTreeCellRendererComponent(JTree tree,
   	Object value, boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasfocus)
   {
   	selected = sel ;
      setBackground((sel) ? bkSelectionColor : bkNonSelectionColor) ;
      setForeground((sel) ? textSelectionColor : textNonSelectionColor) ;
   	DefaultMutableTreeNode node = (DefaultMutableTreeNode) value ;
      Object o = (node != null) ? node.getUserObject() : null ;
		if (o != null) setText(o.toString()) ;

      // Set color depending on the object state.   Breakpoint events
      // are shown in red.  Current breakpoints are shown brighter.

      if (o instanceof FKissEvent)
      {
         FKissEvent evt = (FKissEvent) o ;
      	if (evt.getBreakpoint())
         {
         	if (selected)
            {
            	setBackground(Color.red) ;
            	setForeground(textSelectionColor) ;
            }
            else
            {
            	setBackground(bkNonSelectionColor) ;
            	setForeground(Color.red) ;
            }
         }
      	else if (evt.getNoBreakpoint())
         {
           	setBackground(bkNonSelectionColor) ;
           	setForeground(textDisabledColor) ;
         }
      	if (evt.isCurrentBreak())
         {
           	setForeground(Color.blue) ;
         	if (selected)
               setBackground(getBackground().brighter().brighter()) ;
         }
      }
      if (o instanceof FKissAction)
      {
         FKissAction act = (FKissAction) o ;
      	if (act.getBreakpoint())
         {
         	if (selected)
            {
            	setBackground(Color.red) ;
            	setForeground(textSelectionColor) ;
            }
            else
            {
            	setBackground(bkNonSelectionColor) ;
            	setForeground(Color.red) ;
            }
         }
      	else if (act.getNoBreakpoint())
         {
           	setBackground(bkNonSelectionColor) ;
           	setForeground(textDisabledColor) ;
         }
      	if (act.isCurrentBreak())
         {
           	setForeground(Color.blue) ;
         	if (selected)
               setBackground(getBackground().brighter().brighter()) ;
         }
      }
      return this ;
   }

   // Paint background color.

   public void paintComponent(Graphics g)
   {
   	Color background = getBackground() ;
      g.setColor(background) ;
      g.fillRect(0,0,getWidth()-1,getHeight()-1) ;

      if (selected)
      {
      	g.setColor(borderSelectionColor) ;
         g.drawRect(0,0,getWidth()-1,getHeight()-1) ;
      }

      super.paintComponent(g) ;
   }

	// Get the default height.

	public int getHeight()
	{
		Font f = this.getFont() ;
		if (f == null) return 0 ;
		FontMetrics fm = this.getFontMetrics(f) ;
		int h = fm.getHeight() ;
		return h ;
	}
}

