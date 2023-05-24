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
* ListAutoScroll class
*
* Purpose:
*
* This class extends JList to implement drag and drop auto scrolling.
*
*/

import java.awt.* ;
import java.awt.dnd.* ;
import java.util.* ;
import javax.swing.* ;


public class ListAutoScroll extends JList
    implements Autoscroll
{
   private int margin = 12 ;


	// Constructor

	public ListAutoScroll() { super() ; }


	//	Invoked when the cursor is in the scroll zone.

	public void autoscroll(Point p)
   {
		int realrow = locationToIndex(p) ;
      Rectangle ob = getBounds() ;

      // Decide if the row is at the top of the screen or the bottom.
      // We do this to make the previous or next row visible as
      // appropriate.  If we are at the absolute top or bottom
      // we return the first or last row respectively.

      realrow = (p.y + ob.y <= margin ?
      	realrow < 1 ? 0 : realrow - 1 :
         realrow < getModel().getSize() - 1 ? realrow + 1 : realrow) ;
      ensureIndexIsVisible(realrow) ;
   }

	// Calculte the insets for the JList, not the viewport the JList
   // is in.

	public Insets getAutoscrollInsets()
   {
   	Rectangle ob = getBounds() ;
      Rectangle ib = getParent().getBounds() ;
      return new Insets(
      	ib.y - ob.y + margin,
         ib.x - ob.x + margin,
         ob.height - ib.height - ib.y + ob.y + margin,
         ob.width - ib.width - ib.x + ob.x + margin) ;
	}
}
