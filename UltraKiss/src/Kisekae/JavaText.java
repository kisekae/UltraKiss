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
* JavaText class
*
* Purpose:
*
* This class is a list cel renderer for JavaCel components.  Text components
* can be word-wrapped in a list entry.
*
*/


import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.event.* ;

final class JavaText extends JTextArea
   implements ListCellRenderer
{
   private Dimension size = null ;

   public JavaText(Dimension d)
   {
      d.width -= 20 ;                                 // remove scroll bar
      size = d ;                                      // retain size (Java 1.4)
      this.setSize(d) ;
      this.setLineWrap(true) ;
      this.setWrapStyleWord(true) ;
      this.setBorder(BorderFactory.createEtchedBorder());
   }

   // ListCellRenderer interface methods.

   public Component getListCellRendererComponent(JList list,
   	Object value, int index, boolean isSelected, boolean cellHasFocus)
   {
   	setText((value != null) ? value.toString() : "" ) ;
      setFont(list.getFont()) ;
   	setBackground(isSelected ? list.getSelectionBackground() : list.getBackground()) ;
      setForeground(isSelected ? list.getSelectionForeground() : list.getForeground()) ;
      return this ;
   }

   // Return the preferred size for list box sizing.  Java 1.4 requires that
   // we re-establish the size and recompute this.

   public Dimension getPreferredSize()
   {
      this.setSize(size) ;
      super.doLayout() ;
      return super.getPreferredSize() ;
   }
}
