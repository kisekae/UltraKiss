package com.wmiles.kisekaeultrakiss.WebSearch ;

// Title:        Kisekae UltraKiss
// Version:      2.0
// Copyright:    Copyright (c) 2002-2005
// Company:      WSM Information Systems Inc.
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
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
 *
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  This copyright notice and this permission notice shall be included in      %
%  all copies or substantial portions of UltraKiss.                           %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  WSM Information Systems be liable for any claim, damages or other          %
%  liability, whether in an action of contract, tort or otherwise, arising    %
%  from, out of or in connection with UltraKiss or the use of UltraKiss       %
%                                                                             %
%  WSM Information Systems Inc.                                               %
%  144 Oakmount Rd. S.W.                                                      %
%  Calgary, Alberta                                                           %
%  Canada  T2V 4X4                                                            %
%                                                                             %
%  http://www.kisekaeworld.com                                                %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*/




/**
* CountEntries class
*
* Purpose:
*
* This class is part of a simple web browser for searching and downloading 
* KiSS sets from a host server.
*
* This is a five step process.
*
* Step 1.  Given a URL, obtain the HTML page.  Parse the page to identify 
* all links (GetLinks).  Save links to archive files for later processing.
* For every link to new HTML page, spawn a new GetLinks thread to parse the
* new page.  This effectively scans the web site.  When all links in the 
* site hierarchy have been processed begin Step 2.
*
* Step 2. For each archive file identified through the GetLinks process, load 
* the file in UltraKiss (ValidateLinks).  This load can be for a remote file
* on the web site.  If the file loads this validates it as a KiSS archive.  
* The UltraKiss load is a batch process.  No user interaction is
* required or permitted.  
*
* Step 3. With the file loaded, a thumbnail image can be produced from the
* set initial UltraKiss screen image.  The set can also be saved to local
* storage.  Set statistics can be accumulated.  This information is captured
* for each set.
*
* Step 4. When all sets are validated an HTML index page to the set URLs is
* constructed (BuildForm).  This is a table of all validated sets, by name,
* with thumbnail images and links to the set archive file.  These links can 
* be to remote URLs or to local files.  The HTML index is written to the 
* directory specified in the UltraKiss Search options and named according
* to the site URL or top level directory originally specified in Step 1.
*
* Step 5. The new HTML index is now added to the consolidated index maintained
* for all scanned hosts (BuildIndex).  Each primary site URL or top level
* directory is one line item in the main search index that links to the HTML
* index page built in Step 4.   
*
*/

import java.io.*;
import java.net.*;
import java.util.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.* ;
import java.text.DateFormat ;
import com.wmiles.kisekaeultrakiss.Kisekae.Kisekae ;
import com.wmiles.kisekaeultrakiss.Kisekae.OptionsDialog ;
import com.wmiles.kisekaeultrakiss.Kisekae.PrintLn ;


class CountEntries 
{
   private static String kissindex = null ;        // Master or subindex name
   private WebSearchFrame webframe = null ;        // Parent frame
   private String formname = null ;                // Actual site name
   private Vector entries = new Vector() ;         // URL's to generate
   private ListEntry entry = null ;                // URL and site name entry
   private int totalentries = 0 ;
   private boolean gettitle = false ;
   private boolean getsize = false ;
   private boolean getstart = false ;



   // Our HTML parser callback methods.

   HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback()
   {
      private int tdCounter = 0 ;
      
      public void handleText(char[] data, int pos)
      {
         if (data == null) return ;
         if (entry == null) return ;

         if (gettitle)
         {
            String title = new String(data) ;
            entry.sitename = title.trim() ;
         }

         if (getsize)
         {
            int n = 0 ;
            String size = new String(data) ;
            entry.entrycount = size.trim() ;
         }
         
         if (getstart)
         {
            String start = new String(data) ;
            entry.startname = start.trim() ;
         }
      }

      public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
      {
         // Anchor href
         if (t == HTML.Tag.A && a != null)
         {
            gettitle = true ;
            if (entry != null)
            {
               entries.add(entry) ;
               if (OptionsDialog.getDebugSearch())
                  PrintLn.println("CountEntries: add old entry " + entry + " "
                     + entry.sitename + " " + entry.entrycount) ;
            }
            entry = new ListEntry() ;
            Object o = a.getAttribute(HTML.Attribute.HREF) ;
            if (o == null) entry = null ; else entry.urlname = o.toString().trim() ;
         }
         // TD ref
         if (t == HTML.Tag.TD && a != null)
         {
            tdCounter++ ;
            if (tdCounter == 2)
               getsize = true ;
            if (tdCounter == 3)
               getstart = true ;
         }
      }

      public void handleEndTag(HTML.Tag t, int pos)
      {
         gettitle = false ;
         if (tdCounter == 2)
           getsize = false ;
         if (tdCounter == 3)
           getstart = false ;
         if (t == HTML.Tag.TR) 
            tdCounter = 0; // Reset for new row
      }

      public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { }
      public void flush() throws BadLocationException { }
      public void handleComment(char[] data, int pos) { }
      public void handleError(String errorMsg, int pos) { }
      public void handleEndOfLineString(String eol) { }
   } ;


   // Constructor

   public CountEntries(WebSearchFrame web, String name)
   {
      webframe = web ;
      formname = name ;
   }



   // A function to perform the parse of the input KiSS index.

   public int parse()
   {
      try
      {
         // Create a reader and parse the HTML content.  

         kissindex = formname ;          
         String s = kissindex ;
         s = convertSeparator(s) ;

         try
         {

            // We usually add this new scan as a line item in the master index
            // unless we clearing the master.  However we always create a new 
            // submaster index for remote scans for the remote site or for
            // local directory scans that exceed one batch.
 
            Reader rd = getReader(s) ;
            if (rd == null) return 0 ;
            new ParserDelegator().parse(rd, callback, true);                 
         }
         catch (FileNotFoundException e) 
         { 
            PrintLn.println("CountEntries: exception " + e.toString()) ;
            return 0 ;
         }

         // Build the table.  Note, the last entry in the table has not yet been
         // added into the entries list by the parser.

         totalentries = 0 ;
         if (entry != null) totalentries = Integer.parseInt(entry.entrycount) ;
         for (int i = 0 ; i < entries.size() ; i++)
         {
            ListEntry entry = (ListEntry) entries.elementAt(i) ;
            String urlstring = entry.urlname ;
            String sitename = entry.sitename ;
            String count = entry.entrycount ;
            String start = entry.startname ;
            
            int n = Integer.parseInt(count) ;
            totalentries += n ;
         }
      }
      catch (Exception e)
      {
         PrintLn.println("CountEntries: " + e) ;
         e.printStackTrace() ;
         return 0 ;
      }
      
      return totalentries ;
   }


   // Returns a reader on the HTML data. If 'uri' begins
   // with "http:" or "https:", it's treated as a URL; otherwise,
   // it's assumed to be a local filename.

   static Reader getReader(String uri) throws IOException
   {
      if (uri.startsWith("http:") || uri.startsWith("https:"))
      {
         // Retrieve from Internet.
         URLConnection conn = new URL(uri).openConnection();
         // HTTP Response code 403 for URL
//         conn.addRequestProperty("User-Agent", "UltraKiss");
         long bytes = conn.getContentLength() ;
         return new InputStreamReader(conn.getInputStream());
      }
      return new FileReader(uri) ;
   }

   static String getKissIndex() { return kissindex ; }

   // A function to return a newline terminated string.

   private String newline(String s) { return (s+"\n") ; }


   // A function to convert file separator characters.
   // If we have a URL all separators are '/'.

   private String convertSeparator(String s)
   {
      if (s == null) return null ;
      s = s.replace('/',File.separatorChar) ;
      s = s.replace('\\',File.separatorChar) ;
      String s1 = s.toLowerCase() ;
      if (s1.startsWith("http:")) s = s.replace('\\','/') ;
      if (s1.startsWith("https:")) s = s.replace('\\','/') ;
      return s ;
   }


   // Inner class for list entries.

   class ListEntry implements Comparable
   {
      protected String urlname = null ;
      protected String sitename = null ;
      protected String entrycount = "0" ;
      protected String startname = "" ;

      public String toString() { return (urlname != null) ? urlname : "" ; }


      // Required comparison method for the Comparable interface.

      public int compareTo(Object o)
      {
         if (!(o instanceof ListEntry)) return -1 ;
         String s = ((ListEntry) o).urlname ;
         if (s == null) return -1 ;
         if (urlname == null) return -1 ;
         return (urlname.compareTo(s)) ;
      }
   }
}
