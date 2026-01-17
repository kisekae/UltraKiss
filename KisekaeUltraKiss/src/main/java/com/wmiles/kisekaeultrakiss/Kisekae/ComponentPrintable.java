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
* ComponentPrintable class
*
* Purpose:
*
* This class wraps an existing Component and implements the Printable
* interface.  When asked to print, this class simply translates the
* graphics origin to the imagable area of the page and renders the
* component.  Double buffering is disabled to have the Swing component
* draw directly to the printer to take full advantage of the printer
* resolution.
*
*/


import java.awt.* ;
import java.awt.print.* ;
import java.awt.geom.AffineTransform ;
import javax.swing.* ;


public class ComponentPrintable
   implements Printable
{
   private Component c ;


   // Constructor

   public ComponentPrintable(Component component)
   { c = component ; }


   // Method to print a component.

   public int print(Graphics g, PageFormat pageformat, int pageindex)
   {
   	try
      {
      	if (c == null) return NO_SUCH_PAGE ;
	      if (pageindex > 0) return NO_SUCH_PAGE ;
	      if (pageformat == null) return NO_SUCH_PAGE ;
         
         int width = (int) pageformat.getWidth() ;
         int height = (int) pageformat.getHeight() ;
         Graphics2D g2 = (Graphics2D) g.create(0,0,width,height) ;
			boolean buffered = disableDoubleBuffering(c) ;
         int x = (int) pageformat.getImageableX() ;
         int y = (int) pageformat.getImageableY() ;
         width = (int) pageformat.getImageableWidth() ;
         height = (int) pageformat.getImageableHeight() ;

         // Scale the component to fit on the page.

         Dimension d = c.getSize() ;
         float sx = ((float) width) / d.width ;
         float sy = ((float) height) / d.height ;
         float sf = Math.min(sx,sy) ;
	      GraphicsConfiguration gc = g2.getDeviceConfiguration();
	      g2.setTransform(gc.getDefaultTransform());
         AffineTransform scale = AffineTransform.getScaleInstance(sf,sf) ;
	      g2.transform(scale);
	      int dx = (width - ((int) (d.width * sf))) / 2 ;
	      int dy = (height - ((int) (d.height * sf))) / 2 ;
         g2.translate(x+dx,y+dy) ;

         // Draw the component.

			c.paint(g2) ;
         g2.dispose() ;
			restoreDoubleBuffering(c,buffered) ;
	      return PAGE_EXISTS ;
      }
      catch (Exception e)
      {
      	PrintLn.println("Print Component exception: " + e.getMessage()) ;
         e.printStackTrace() ;
			JOptionPane.showMessageDialog(null,
           	"Print error.  Printing terminated." + "\n" + e.toString(),
           	"Print Fault",  JOptionPane.ERROR_MESSAGE) ;
      	return NO_SUCH_PAGE ;
      }
   }

   // Function to disable double buffering on this component.

   private boolean disableDoubleBuffering(Component c)
   {
      if (!(c instanceof JComponent)) return false ;
      JComponent jc = (JComponent) c ;
      boolean buffered = jc.isDoubleBuffered() ;
      jc.setDoubleBuffered(false) ;
      return buffered ;
   }

   // Function to restore double buffering on the component.

   private void restoreDoubleBuffering(Component c, boolean buffered)
   {
      if (c instanceof JComponent)
         ((JComponent) c).setDoubleBuffered(buffered) ;
   }
}