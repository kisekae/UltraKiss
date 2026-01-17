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
* WebHTMLEditor class
*
* Purpose:
*
* This class extends the HTMLEditorKit to define a minimum synchronous load
* priority for image load threads.  HTML pages that contain many images can
* impact performance during loading.
*
*/

import java.util.Hashtable;
import javax.swing.text.* ;
import javax.swing.text.html.* ;


final class WebHTMLEditor extends HTMLEditorKit
{

   private static Hashtable imagecache = new Hashtable() ; // Image cache
   
/*
   The JEditorPane.setPage() does an asynchronous loading of HTML
   data. If you try to iterate on the doc before the parsing is
   completed you only get the tags for the empty doc.
   You can force a syncrhonous loading by deriving a class from
   HTMLEditorKit as follow:

   public class SyncHTMLEditor extends HTMLEditorKit {
   public Document createDefaultDocument() {
   AbstractDocument doc=(AbstractDocument)
   super.createDefaultDocument();
   doc.setAsynchronousLoadPriority(-1);
   }
   }

   and use is in JEditorPane:

   //JEditorPane jpane
   ...
   jpane.setEditorKit(new SyncHTMLEditor());
   ...
   jpane.setPage("http://java.sun.com/");
   */

   public Document createDefaultDocument()
   {
      HTMLDocument doc = (HTMLDocument) super.createDefaultDocument() ;
      int priority = Thread.NORM_PRIORITY ;
      doc.setAsynchronousLoadPriority(priority) ;
      doc.setTokenThreshold(1000) ;
      doc.putProperty("imageCache",imagecache);
//      doc.setAsynchronousLoadPriority(-1);
      return doc ;
   }
   
   public ViewFactory getViewFactory ()
   {
      return new WebViewFactory ();
   }
   
   // Specific methods to manage cache
      
   public Hashtable getImageCache() { return imagecache ; }
   
   public void clearCache() { imagecache.clear() ; }
}
