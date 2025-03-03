package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      4.0  (January 27, 2025)
// Copyright:    Copyright (c) 2002-2025
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
* WebViewFactory class
*
* Purpose:
*
* This class extends the HTMLEditorKit to define a minimum synchronous load
* priority for image load threads.  HTML pages that contain many images can
* impact performance during loading.
*
*/

import javax.swing.text.* ;
import javax.swing.text.html.* ;

/**
 *
 * @author William
 */
public class WebViewFactory extends HTMLEditorKit.HTMLFactory
{
   @Override
   public View create (Element elem)
   {
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) 
      {
         HTML.Tag kind = (HTML.Tag) o;
         if (kind == HTML.Tag.IMG)
            return new WebImageView(elem);
      }
      
      View view = super.create (elem);
//      if (view instanceof ImageView)
//         ((ImageView) view).setLoadsSynchronously (true);
      return view;
   }
}
