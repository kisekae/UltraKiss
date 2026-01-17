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
* PlainPrintView Class
*
* Purpose:
*
* This object is used to render the contents of a plain document for
* printing.
*
*/


import java.awt.* ;
import java.awt.font.* ;
import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.text.rtf.* ;
import java.awt.geom.Rectangle2D ;



class PlainPrintView extends JComponent
{
  	private JTextArea text = null ;     // Text to print
   private String name = null ;        // Name of text file
   private Font font = null ;          // Font for print display
   private int fontsize = 0 ;          // Font point size
 	private int firstOnPage = 0 ;       // First line on page
   private int lastOnPage = -1 ;       // Last line on page
   private int pageindex = -1 ;        // Current page number
   private int xoffset = 0 ;           // Imageable x offset
   private int yoffset = 0 ;           // Imageable y offset
   private int width = 0 ;             // Page width
   private int height = 0 ;            // Page height

   // Constructor

   public PlainPrintView(String file, JTextComponent t, int x, int y, int w, int h)
   {
      if (t instanceof JTextArea) text = (JTextArea) t ;
      font = (text == null) ? null : text.getFont() ;
   	fontsize = (font == null) ? 0 : font.getSize() ;
      fontsize = 9 ;
      name = file ;
      xoffset = x ;
      yoffset = y ;
      yoffset += (fontsize * 3 / 2) ;
      width = w ;
      height = h ;
		setSize(w,h) ;
	}

   // Method to paint all lines on the specified page.  This function
   // returns true if the page was rendered, false otherwise.  The height
   // parameter is the page imageable height.

   public boolean paintPage(Graphics g, int height, int page)
   {
      boolean headerwritten = false ;
      boolean trailerwritten = false ;

     	if (text == null) return false ;
      Graphics2D g2 = (Graphics2D) g ;
      if (page <= pageindex) pageindex = -1 ;
      if (pageindex < 0) lastOnPage = -1 ;
     	firstOnPage = lastOnPage + 1 ;
		if (firstOnPage > text.getLineCount()) return false ;
      Font f = new Font(font.getName(),font.getStyle(),fontsize) ;
      FontRenderContext frc = new FontRenderContext(null,false,false) ;
      int y = fontsize ;

      // Read lines on required page.

      try
      {
         while (page > pageindex)
         {
            for (int k = firstOnPage ; k <= text.getLineCount() ; k++)
            {
               if ((y + 2*(fontsize * 3 / 2)) > height+yoffset) break ;
               lastOnPage = k ;
               if (page > pageindex + 1)
               {
                  if (k == firstOnPage) y = yoffset + 2*(fontsize * 3 / 2) ;
                  else y += (fontsize * 3 / 2) ;
                  continue ;
               }

               // Paint the line.

               g2.setFont(f) ;
               g2.setPaint(Color.black) ;
              	String line = readline(k) ;

               // Write the header line if necessary.

               if (!headerwritten)
               {
                  Rectangle2D r2 = f.getStringBounds(name,frc) ;
                  Rectangle r = r2.getBounds() ;
                  int x = (width - r.width) / 2 ;
                  y = yoffset ;
                  g2.drawString(name,x,y) ;
                  y += (fontsize * 3 / 2) ;
                  y += (fontsize * 3 / 2) ;
                  headerwritten = true ;
               }

               // Write the text line.

               if (line.length() > 0) g2.drawString(line,xoffset,y) ;
               y += (fontsize * 3 / 2) ;
            }

            // Setup for next page.

            pageindex++ ;
            y = fontsize ;
           	firstOnPage = lastOnPage + 1 ;

            // Write page footer if this is the required page.

            if (page == pageindex)
            {
               String s1 = Kisekae.getCaptions().getString("PageFooterText") ;
               int i1 = s1.indexOf('[') ;
               int j1 = s1.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s1 = s1.substring(0,i1) + (page+1) + s1.substring(j1+1) ;
               Rectangle2D r2 = f.getStringBounds(s1,frc) ;
               Rectangle r = r2.getBounds() ;
               int x = (width - r.width) / 2 ;
               int y1 = height + yoffset - (fontsize * 2) ;
               g2.drawString(s1,x,y1) ;
               return true ;
            }
         }
      }
      catch (Exception e) { }
      return false ;
   }

   // Function to return a line of text from the text area.  The line
   // does not include the terminating newline character.

   private String readline(int line)
   {
      int end = 0 ;
     	int start = 0 ;
      String s = null ;
      try { start = text.getLineStartOffset(line) ; }
      catch (BadLocationException e) { return "" ; }
      try { end = text.getLineEndOffset(line) ; }
      catch (BadLocationException e) { return "" ; }
      try { s = text.getText(start,end-start) ; }
      catch (BadLocationException e) { return "" ; }
      int n = s.length() - 1 ;
     	return (n > 0) ? s.substring(0,n) : "" ;
   }
}
