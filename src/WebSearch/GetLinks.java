package WebSearch ;

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
* GetLinks class
*
* Purpose:
*
* This class is part of a simple web browser for searching and downloading 
* KiSS sets from a host server.
*
* This is a four step process.
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
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import Kisekae.OptionsDialog ;


class GetLinks implements Runnable
{
   private static int count = 0 ;
   private static int bytes = 0 ;
   private static int activecount = 0 ;
   private static boolean stop = false ;
   private static boolean scheduled = false ;
   private static Vector archives = new Vector() ;
   private static String title = null ;
   private boolean catchtitle = false ;
   private WebSearchFrame webframe = null ;
   private String location = null ;
   private URL parseurl = null ;
   private Vector v = new Vector() ;



   // Our HTML parser callback methods.

   HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback()
   {
      public void handleText(char[] data, int pos)
      {
         if (!catchtitle) return ;
         if (title != null) return ;
         title = new String(data) ;
         webframe.addTrace("Site Title: " + title) ;
      }

      public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
      {
//       System.out.println(Thread.currentThread().getName() + " [" + location + "] HTML start tag " + t) ;
         // Anchor href
         if (t == HTML.Tag.A && a != null)
            v.add(a.getAttribute(HTML.Attribute.HREF)) ;
         // Form action
         if (t == HTML.Tag.FORM && a != null)
            v.add(a.getAttribute(HTML.Attribute.ACTION)) ;
         // Table href
         if (t == HTML.Tag.TD && a != null)
            v.add(a.getAttribute(HTML.Attribute.HREF)) ;
         if (t == HTML.Tag.TR && a != null)
            v.add(a.getAttribute(HTML.Attribute.HREF)) ;
         // Title description
         if (t == HTML.Tag.TITLE && a != null)
            catchtitle = true ;
      }

      public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
      {
         // Frame source
         if (t == HTML.Tag.FRAME && a != null)
            { v.add(a.getAttribute(HTML.Attribute.SRC)) ; }
         // IFrame source
         if ("IFRAME".equals(t.toString().toUpperCase()) && a != null)
            { v.add(a.getAttribute(HTML.Attribute.SRC)) ; }
         // Area href
         if (t == HTML.Tag.AREA && a != null)
            v.add(a.getAttribute(HTML.Attribute.HREF)) ;
      }

      public void handleEndTag(HTML.Tag t, int pos)
      { catchtitle = false ; }

      public void flush() throws BadLocationException { }
      public void handleComment(char[] data, int pos) { }
      public void handleError(String errorMsg, int pos) { }
      public void handleEndOfLineString(String eol) { }
   } ;


   // Constructor.  The input string contains a directory that needs
   // to be scanned by this GetLink thread.  The isolated zip and lzh
   // archives are saved in our archives vector.

   public GetLinks(WebSearchFrame web, String s)
   {
      webframe = web ;
      s = s.replace('\\','/') ;
      if (s.startsWith("file:") && !s.endsWith("/")) s += "/" ;
      location = s ;
      try { parseurl = new URL(s) ; }
      catch (MalformedURLException e)
      {
         System.out.println("GetLinks: invalid URL " + s);
      }
   }



   // Thread interface.

   public void run()
   {
      count++ ;
      activecount++ ;
      Thread thread = Thread.currentThread() ;
      thread.setName("GetLinks-" + count) ;
      if (OptionsDialog.getDebugSearch())
   		System.out.println(thread.getName() + " started.") ;
      webframe.showStatus("GetLinks-" + count + " searching " + location) ;
      webframe.getlinkactive = true ;
      if (!stop) parse() ;
      checkend() ;
      if (OptionsDialog.getDebugSearch())
    		System.out.println(thread.getName() + " ends.") ;
  }

   
   // A function to schedule the next step when all activities end.
   
   synchronized void checkend()
   {
      activecount-- ;
      if (scheduled) return ;
      if (Scheduler.getQueueSize() == 0 && activecount == 0)
      {
         scheduled = true ;
         String s = (bytes  / 1024) + "K" ;
         webframe.addTrace("End URL Scan. Pages accessed: " + count + " Bytes downloaded: " + s,1) ;
         webframe.getlinkactive = false ;
         webframe.urlcallback.doClick() ;
      }
   }


   // A function to perform the parse.

   void parse()
   {
      try
      {
         // Create a reader and parse the HTML content.  Every identified
         // archive is added to the static archives vector.  A new GetLinks
         // thread is started for every file directory or link directory.

         if (OptionsDialog.getDebugSearch())
            System.out.println(Thread.currentThread().getName() + " search " + location);
         Reader rd = getReader(location);
         if (rd == null) return ;
         new ParserDelegator().parse(rd, callback, true);

         // Correct the URLs for relative entries.  Vector v is built by the
         // ParserDelegator callback routines from HREF and other tags parsed
         // from the document reader.  Vector v1 is the corrected URL entries.
         // For file directories vector v is empty as nothing was parsed with
         // the HTML ParserDelegator.

         Vector v1 = new Vector() ;
         for (int i = 0 ; i < v.size() ; i++)
         {
            Object o = v.elementAt(i) ;
            if (o == null) continue ;
            String s = ((String) o).toLowerCase() ;

            // FTP links are ignored.

            if (s.startsWith("ftp:")) continue ;

            // Fully specified URLs that are not to this site and do
            // not point to archive files are ignored.

            if (s.startsWith("http:") || s.startsWith("https:"))
            {
               if (!(s.endsWith(".zip") || s.endsWith(".lzh")))
               {
                  String s2 = webframe.getBaseLocation().toLowerCase() ;
                  if (!s.startsWith(s2)) continue ;
               }
            }
            
            // Links with parameters (?) are ignored.
            
            if (s.indexOf('?') > 0) continue ;

            // Relative links are converted to full URLs without bookmarks.

            if (s.endsWith(".html") || s.endsWith(".htm")
               || s.endsWith(".shtml") || s.endsWith(".shtm")
               || s.endsWith("/")
               || s.endsWith(".zip") || s.endsWith(".lzh"))
            {
               s = o.toString() ;
               int j = s.lastIndexOf('#') ;
               if (j > 0) s = s.substring(0,j) ;
               URL url = new URL(parseurl,s) ;
               v1.addElement(url.toString()) ;
            }
         }

         // List the isolated URLs for examination.  This adds HTML parsed 
         // entries to the archives vector.  

         Thread thread = Thread.currentThread() ;
         ThreadGroup group = thread.getThreadGroup() ;
         for (int i = 0 ; i < v1.size() ; i++)
         {
            Object o = v1.elementAt(i) ;
            String s = ((String) o).toLowerCase() ;
            if (s.endsWith(".zip") || s.endsWith(".lzh"))
            {
               if (archives.contains(o.toString())) continue ;
               if (OptionsDialog.getDebugSearch())
                  System.out.println(thread.getName() + " Archive found: " + o) ;
               webframe.addTrace("Archive: " + o) ;
               archives.addElement(o.toString()) ;
               continue ;
            }

            // HTML anchors or sub-directories cause a recursive search.

            if (s.endsWith(".html") || s.endsWith(".htm")
               || s.endsWith(".shtml") || s.endsWith(".shtm")
               || (s.endsWith("/") && s.startsWith(location.toLowerCase())))
            {
               GetLinks gl = new GetLinks(webframe,o.toString()) ;
               Scheduler.queueEvent(gl,group) ;
            }
         }

         // If we are working on our local file system, check for directories.
         // File directories cause a recursive search.  File archives are 
         // added to the archives vector.

         if (location.toLowerCase().startsWith("file:"))
         {
            URL url = new URL(location) ;
            String path = url.getPath() ;
            File f = new File(path) ;
            File [] files = f.listFiles() ;
            if (files != null)
            {
               for (int i = 0 ; i < files.length ; i++)
               {
                  File f2 = files[i] ;
                  if (f2.isDirectory()) 
                  {
                     String s = f2.getAbsolutePath() ;
                     if (!s.startsWith(File.separator)) s = File.separator + s ;
                     s = "file://" + s.replace('\\','/') ;
                     GetLinks gl = new GetLinks(webframe,s) ;
                     Scheduler.queueEvent(gl,group) ;
                  }
                  if (f2.isFile())
                  {
                     String s = f2.getName() ;
                     String s2 = s.toLowerCase() ;
                     if (s2.endsWith(".zip") || s2.endsWith(".lzh"))
                     {
                        s = f2.getAbsolutePath() ;
                        if (archives.contains(s)) continue ;
                        if (OptionsDialog.getDebugSearch())
                           System.out.println(thread.getName() + " Archive found: " + s) ;
                        webframe.addTrace("Archive: " + s) ;
                        if (!s.startsWith(File.separator)) s = File.separator + s ;
                        s = "file://" + s.replace('\\','/') ;
                        archives.addElement(s) ;
                     }
                  }
               }
            }
         }
      }

      // Watch for exceptions.

      catch (Exception e)
      {
         System.out.println(Thread.currentThread().getName() + " " + e) ;
         e.printStackTrace() ;
      }
   }


   // Returns a reader on the HTML data. If 'uri' begins  with "http:" or 
   // "https": or "file:" it's treated as a URL; otherwise, it's assumed to be a local 
   // filename.

   static Reader getReader(String uri) throws IOException
   {
      try
      {
         if (uri.startsWith("http:") || uri.startsWith("https:") || uri.startsWith("file:"))
         {
            // Retrieve from Internet.
            URL sourceurl = new URL(uri) ;
            URLConnection conn = sourceurl.openConnection();
            // HTTP Response code 403 for URL
//            conn.addRequestProperty("User-Agent", "UltraKiss");
            InputStream in = conn.getInputStream() ;
            bytes += conn.getContentLength() ;
            return new InputStreamReader(in);
         }
         return new FileReader(uri) ;
      }
      catch (Exception e)
      {
         if (e instanceof FileNotFoundException) return null ;
         System.out.println("GetLinks: uri '" + uri + "' " + e);
      }
      return null ;
   }


   // A method to shut us down.

   static void stopsearch() { stop = true ; }

   // A method to return our URL string.

   String getLocation() { return location ; }

   // A method to return our URL string.

   static String getTitle() { return (title != null) ? title : "" ; }

   // A method to return our archives.

   static Vector getArchives() { return archives ; }

   // A method to reset our static counts.

   static void reset()
   {
      count = 0 ; bytes = 0 ;
      archives = new Vector() ;
      title = null ;
      stop = false ;
      scheduled = false ;
   }
}