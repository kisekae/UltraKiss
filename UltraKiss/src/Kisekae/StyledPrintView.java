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
* StyledPrintView Class
*
* Purpose:
*
* This object is used to render the contents of a styled document for
* printing.
*
*/


import java.awt.* ;
import javax.swing.text.* ;
import javax.swing.text.rtf.* ;


class StyledPrintView extends BoxView
{
  	protected int firstOnPage = 0 ;           // first child on page
   protected int lastOnPage = 0 ;            // last child on page
   protected int pageindex = -1 ;            // last page processed
   private int xoffset = 0 ;                 // page x margin
   private int yoffset = 0 ;                 // page y margin
   private int ybias = 0 ;                   // cummulative interelement bias


   public StyledPrintView(Element elem, View root, int x, int y, int w, int h)
   {
		super(elem,Y_AXIS) ;
		setParent(root) ;
      xoffset = x ;
      yoffset = y ;
		setSize(w,h) ;
		layout(w,h) ;
	}

   public boolean paintPage(Graphics g, int height, int pageIndex)
   {
      Rectangle rc = new Rectangle() ;

      // Initialize if page 0.

      if (pageIndex == 0 || pageIndex <= pageindex)
      {
         lastOnPage = 0 ;
         pageindex = -1 ;
         ybias = 0 ;
      }

      // Scan to find the page if we are not sequentially printing.

     	while (pageIndex > pageindex+1)
      {
        	firstOnPage = lastOnPage + 1 ;
			if (firstOnPage >= getViewCount()) return false ;
         int yMin = getOffset(Y_AXIS,firstOnPage) ;
         int yMax = yMin + height ;
         for (int k = firstOnPage ; k < getViewCount() ; k++)
         {
    			rc.x = getOffset(X_AXIS,k) + xoffset ;
            rc.y = getOffset(Y_AXIS,k) + yoffset ;
     			rc.width = getSpan(X_AXIS,k) ;
            rc.height = getSpan(Y_AXIS,k) ;
            if (rc.y+rc.height-ybias > yMax)
            {
               if (rc.height-ybias > height) ybias += height ;
               break ;
            }
            ybias = 0 ;
            lastOnPage = k ;
         }
			pageindex++ ;
      }

      // Print the page.  If we have a large element that spans more than
      // one page it is partitioned.  The ybias variable is the offset into
      // the element for next page printing.

     	firstOnPage = lastOnPage + 1 ;
   	if (firstOnPage >= getViewCount()) return false ;
		pageindex++ ;
      int yMin = getOffset(Y_AXIS,firstOnPage) ;
      int yMax = yMin + height ;
      for (int k = firstOnPage ; k < getViewCount() ; k++)
      {
			rc.x = getOffset(X_AXIS,k) + xoffset ;
         rc.y = getOffset(Y_AXIS,k) + yoffset ;
			rc.width = getSpan(X_AXIS,k) ;
         rc.height = getSpan(Y_AXIS,k) ;
         if (k != firstOnPage && rc.y+rc.height > yMax) break ;
         rc.y -= yMin ;
         if (ybias > 0) rc.y = yoffset - ybias ;
         if (rc.y+rc.height <= 3*yoffset)
         {
            ybias = 0 ;
            lastOnPage = k ;
            return false ;
         }
         Rectangle r = g.getClipBounds() ;
         if (ybias > 0) g = g.create(0,yoffset,r.width,r.height) ;
         g.setClip(r.x,r.y,r.width,height) ;
         paintChild(g,rc,k) ;
         if (rc.y+rc.height > yMax)
         {
            ybias += height ;
            return true ;
         }
         ybias = 0 ;
         lastOnPage = k ;
      }

      return true ;
   }
}
