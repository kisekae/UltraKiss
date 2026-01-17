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
* BuildIndex class
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


class BuildIndex implements ActionListener
{
   private static boolean stop = false ;           // Signal to stop thread
   private String formname = null ;                // Actual site name
   private String formurl = null ;                 // Catalogue URL string
   private String formcount = "0" ;                // Count of entries on form
   private String baselocation = null ;            // Web site location
   private Vector entries = new Vector() ;         // URL's to generate
   private ListEntry entry = null ;                // URL and site name entry
   private WebSearchFrame webframe = null ;        // Our parent frame
   private Kisekae kisekae = null ;                // The Kisekae loader
   private boolean gettitle = false ;
   private boolean getsize = false ;



   // Our HTML parser callback methods.

   HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback()
   {
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
                  PrintLn.println("BuildIndex: add old entry " + entry + " "
                     + entry.sitename + " " + entry.entrycount) ;
            }
            entry = new ListEntry() ;
            Object o = a.getAttribute(HTML.Attribute.HREF) ;
            if (o == null) entry = null ; else entry.urlname = o.toString().trim() ;
         }
         // TD ref
         if (t == HTML.Tag.TD && a != null)
         {
            getsize = true ;
         }
      }

      public void handleEndTag(HTML.Tag t, int pos)
      {
         gettitle = false ;
         getsize = false ;
      }

      public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) { }
      public void flush() throws BadLocationException { }
      public void handleComment(char[] data, int pos) { }
      public void handleError(String errorMsg, int pos) { }
      public void handleEndOfLineString(String eol) { }
   } ;


   // Constructor

   public BuildIndex(WebSearchFrame web, String name, String url, int n)
   {
      webframe = web ;
      formname = name ;
      formurl = url ;
      formcount = Integer.valueOf(n).toString() ;
      kisekae = Kisekae.getKisekae() ;
   }



   // A function to perform the parse of the input KiSS index.

   void parse()
   {
      try
      {
         // Create a reader and parse the HTML content.  The original
         // file is renamed to have a ".bak" extension.

         int n = 0 ;
         String s = OptionsDialog.getKissIndex() + ".bak" ;
         s = convertSeparator(s) ;
         
         try
         {
            String s1 = OptionsDialog.getKissIndex() ;
            s1 = convertSeparator(s1) ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: parse started. " + s1) ;
            File f1 = new File(s1) ;
            File f2 = new File(s) ;
            if (f2.exists()) f2.delete() ;
            f1.renameTo(f2) ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: rename " + f1.getName() + " to " + f2.getPath()) ;
            if (!OptionsDialog.getClearMaster())
            {
               Reader rd = getReader(s) ;
               if (rd == null) return ;
               new ParserDelegator().parse(rd, callback, true);
            }
         }
         catch (FileNotFoundException e) 
         { 
            PrintLn.println("BuildIndex: rename " + OptionsDialog.getKissIndex() + " to " + s + " failed.") ;
            PrintLn.println(e.toString()) ;
         }

         // Add or replace the new entry to our list.

         if (entry != null)
         {
            entries.add(entry) ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: add old entry " + entry + " '"
                  + entry.sitename + "' " + entry.entrycount) ;
         }
         entry = new ListEntry() ;
         entry.urlname = formurl ;
         entry.sitename = formname ;
         entry.entrycount = formcount ;
         for (n = 0 ; n < entries.size() ; n++)
            if (formurl.equals(((ListEntry) entries.elementAt(n)).urlname)) break ;
         if (n < entries.size())
         {
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: replace entry " + n
                  + " with " + entry + " '" + entry.sitename + "' " + entry.entrycount) ;
            entries.setElementAt(entry,n) ;
         }
         else
         {
            entries.add(entry) ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: add new entry " + entry + " '"
                  + entry.sitename + "' " + entry.entrycount) ;
         }

         // Sort by site name.

         Collections.sort(entries) ;
         if (OptionsDialog.getDebugSearch())
            PrintLn.println("BuildIndex: parse ends. Entries = " + entries.size()) ;
      }

      // Watch for exceptions.

      catch (Exception e)
      {
         PrintLn.println(Thread.currentThread().getName() + " " + e) ;
         e.printStackTrace() ;
      }
   }


   boolean buildform()
   {
      try
      {
         if (OptionsDialog.getDebugSearch())
            PrintLn.println("BuildIndex: buildform started.") ;
         
         // Open the HTML page.

         JTextArea text = new JTextArea() ;
         String title = "UltraKiss Search Index" ;
         URL url = getClass().getClassLoader().getResource("Images/ultrakiss.gif") ;
         String s = (url != null) ? url.toString() : null ;

         text.append(newline("<html>")) ;
         text.append(newline("<head>")) ;
         text.append(newline("<title>"+title+"</title>")) ;
         text.append(newline("</head>")) ;
         text.append(newline("<body>")) ;

         if (s != null)
         {
            text.append(newline("<p align=\"center\">")) ;
            text.append(newline("<img src=\""+s+"\" border=\"0\">")) ;
            text.append(newline("</p>")) ;
         }
         
         text.append(newline("<div align\"center\">")) ;
         text.append(newline("<center>")) ;
         text.append(newline("<table border=\"0\" cellpadding=\"5\">")) ;
         text.append(newline("<tr>")) ;
         text.append(newline("<td><b>Site</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Entries</b></td>")) ;
         text.append(newline("</tr>")) ;

         // Build the table.

         for (int i = 0 ; i < entries.size() ; i++)
         {
            ListEntry entry = (ListEntry) entries.elementAt(i) ;
            String urlstring = entry.urlname ;
            String sitename = entry.sitename ;
            String count = entry.entrycount ;
            if (sitename == null || urlstring == null) continue ;
            String title2 = sitename ;
            if (title2.startsWith("file:///")) title2 = title2.substring(8) ;
            if (title2.startsWith("file:/")) title2 = title2.substring(6) ;
            text.append(newline("<tr>")) ;
            text.append(newline(" <td><a href=\""+urlstring+"\">"+title2+"</a></td>")) ;
            text.append(newline(" <td align=\"center\">"+count+"</td>")) ;
            text.append(newline("</tr>")) ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildIndex: index for " + urlstring + " to " + title2) ;
         }

         // Close the HTML page.

         Calendar date = Calendar.getInstance() ;
         String datestring = DateFormat.getDateInstance().format(date.getTime()) ;
         text.append(newline("</table>")) ;
         text.append(newline("</center>")) ;
         text.append(newline("</div>")) ;
         text.append(newline("<p>&nbsp;</p>")) ;
         text.append("<p align=\"center\"><font size=\"2\">Generated by WebSearch V1.0a<br>") ;
         text.append(newline(datestring+"</font></p>")) ;
         text.append(newline("<p>&nbsp;</p>")) ;
         text.append(newline("</body>")) ;
         text.append(newline("</html>")) ;

         // Save the text object.

         formname = OptionsDialog.getKissIndex() ;
         File f1 = new File(formname) ;
         String directory = f1.getParent() ;
         formname = f1.getName() ;
         if (formname.endsWith(".html") || formname.endsWith(".htm"))
         {
            formname = formname.substring(0,formname.lastIndexOf('.')) ;
            formname += ".txt" ;
         }
         File f = new File(directory) ;
         f.mkdirs() ;
         if (f1.exists()) f1.delete() ;
         boolean savehtmlform = f1.createNewFile() ;
         if (savehtmlform) kisekae.saveText(this,directory,formname,text) ;
         if (OptionsDialog.getDebugSearch())
            PrintLn.println("BuildIndex: buildform ends. " + f1.getPath()) ; 
         return savehtmlform ;
      }
      catch (Exception e)
      {
         PrintLn.println("BuildIndex: " + e) ;
         e.printStackTrace() ;
         return false ;
      }
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



   // Our action listener to recognize Kisekae load completion events and file
   // save events.

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource() ;

      // File saves are sequenced.

      if (e.getActionCommand().equals("FileWriter Callback"))
      {
         try
         {
            String s = "Index Form " + formname + " saved" ;
            PrintLn.println("BuildIndex: " + s) ;
            webframe.addTrace(s) ;
         }

         // If there is an error recognize the exception and continue.

         catch (Throwable ex)
         {
            Thread thread = Thread.currentThread() ;
            PrintLn.println(thread.getName() + " " + ex) ;
            ex.printStackTrace() ;
         }

         finally
         {
            webframe.addTrace("End Index Form generation.",1) ;
            webframe.idxcallback.doClick() ;
         }
      }
   }


   // Inner class for list entries.

   class ListEntry implements Comparable
   {
      protected String urlname = null ;
      protected String sitename = null ;
      protected String entrycount = "0" ;

      public String toString() { return (urlname != null) ? urlname : "" ; }


      // Required comparison method for the Comparable interface.

      public int compareTo(Object o)
      {
         if (!(o instanceof ListEntry)) return -1 ;
         String s = ((ListEntry) o).sitename ;
         if (s == null) return -1 ;
         if (sitename == null) return -1 ;
         return (sitename.compareTo(s)) ;
      }
   }
}
