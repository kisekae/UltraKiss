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
* BuildForm class
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
import Kisekae.Kisekae ;
import Kisekae.OptionsDialog ;
import Kisekae.PrintLn ;


class BuildForm implements Runnable, ActionListener
{
   private static int count = 0 ;                  // Count of invokations
   private static int bytes = 0 ;                  // Total bytes loaded
   private static boolean stop = false ;           // Signal to stop thread
   private static Vector state = new Vector() ;    // URL's to generate
   private static String formname = null ;         // Actual HTML name
   private String baselocation = null ;            // Web site location
   private WebSearchFrame webframe = null ;        // Our parent frame
   private Kisekae kisekae = null ;                // The Kisekae loader

   private static Object queue = new Object() ;    // Our synchronized wait


   // Constructor.  The input vector contains the extended state
   // information for every validate archive. 

   public BuildForm(WebSearchFrame web, Vector v)
   {
      webframe = web ;
      state = v ;
      baselocation = webframe.getBaseLocation() ;
      if (baselocation.startsWith("http://"))
         baselocation = baselocation.substring(7) ;
      if (baselocation.startsWith("https://"))
         baselocation = baselocation.substring(8) ;
      if (baselocation.startsWith("file:///"))
         baselocation = baselocation.substring(11) ;
      if (baselocation.startsWith("file://"))
         baselocation = baselocation.substring(10) ;
      if (baselocation.startsWith("file:/"))
         baselocation = baselocation.substring(9) ;
      baselocation = baselocation.replace('/','.') ;
      if (baselocation.endsWith("."))
         baselocation = baselocation.substring(0,baselocation.length()-1) ;
   }



   // Thread interface.

   public void run()
   {
      Thread thread = Thread.currentThread() ;
      thread.setName("BuildForm-" + count) ;
      webframe.buildformactive = true ;
      if (OptionsDialog.getDebugSearch())
         PrintLn.println(thread.getName() + " started.") ;
      kisekae = Kisekae.getKisekae() ;

      // Process each entry.

      count++ ;
      if (buildform())
      {
         synchronized (queue)
         {
            try { queue.wait() ; }
            catch (InterruptedException e) { stop = true ; }
         }
      }

      // Continue with the Index update.

      webframe.addTrace("End HTML Form generation.",1) ;
      webframe.buildformactive = false ;
      if (OptionsDialog.getDebugSearch())
         PrintLn.println(thread.getName() + " ends.") ;
         
      // Run the callback on the EDT thread.
         
      Runnable runner = new Runnable()
      { public void run() { webframe.bldcallback.doClick() ; } } ;
      javax.swing.SwingUtilities.invokeLater(runner) ;        
   }


   private boolean buildform()
   {
      try
      {
         if (state == null) throw new Exception("BuildForm: no state information") ;

         // Open the HTML page.

         JTextArea text = new JTextArea() ;
         String s = webframe.getBaseLocation() ;
         String title = GetLinks.getTitle() ;
         String title2 = title ;
         if (title2.length() == 0) title2 = s ;
         if (title2.startsWith("file:///")) title2 = title2.substring(8) ;
         if (title2.startsWith("file:/")) title2 = title2.substring(6) ;
         text.append(newline("<html>")) ;
         text.append(newline("<head>")) ;
         text.append(newline("<title>"+title2+"</title>")) ;
         text.append(newline("</head>")) ;
         text.append(newline("<body>")) ;

         if (title.length() > 0)
            text.append(newline("<h1 align=\"center\"><font color=\"#800000\">"+title+"</font></h1>")) ;
         text.append(newline("<p align=\"center\"><a href=\""+s+"\">"+title2+"</a></p>")) ;
         text.append(newline("<p align=\"center\">&nbsp;</p>")) ;
         text.append(newline("<div align=\"center\">")) ;
         text.append(newline("<center>")) ;
         text.append(newline("<table border=\"1\" cellpadding=\"5\" bgcolor=\"#FDFFE8\">")) ;
         text.append(newline("<tr>")) ;
         text.append(newline("<td align=\"center\"><b>Image</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Set Name</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Size</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>CNFs</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Cels</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>KCFs</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Audio</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Video</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>FKiSS</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>CKiSS</b></td>")) ;
         text.append(newline("<td align=\"center\"><b>Enhanced</b></td>")) ;
         text.append(newline("</tr>")) ;

         // Build the table.

         Collections.sort(state) ;
         for (int i = 0 ; i < state.size() ; i++)
         {
            int setsize = 0 ;
            StringTokenizer st = new StringTokenizer((String) state.elementAt(i),"\t") ;
            String location = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildForm: processing " + location) ;
            String name = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String size = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String entrycount = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String palettes = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String cels = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String audio = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String video = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String fkiss = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String ckiss = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String epal = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String image = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            String descr = (st.hasMoreTokens()) ? st.nextToken() : "" ;
            try { setsize = Integer.parseInt(size) ; }
            catch (NumberFormatException e) { }
            text.append(newline("<tr>")) ;
            text.append("<td align=\"center\" rowspan=\"2\">") ;
            text.append("<img border\"0\" src=\""+image+"\" width=\"50\" height=\"50\">") ;
            text.append(newline("</td>"));
            text.append(newline("<td align=\"center\"><a href=\""+location+"\">"+name+"</td>")) ;
            text.append(newline("<td align=\"center\">"+(setsize/1024)+" KB"+"</td>")) ;
            text.append(newline("<td align=\"center\">"+entrycount+"</td>")) ;
            text.append(newline("<td align=\"center\">"+cels+"</td>")) ;
            text.append(newline("<td align=\"center\">"+palettes+"</td>")) ;
            text.append(newline("<td align=\"center\">"+audio+"</td>")) ;
            text.append(newline("<td align=\"center\">"+video+"</td>")) ;
            text.append(newline("<td align=\"center\">"+fkiss+"</td>")) ;
            text.append(newline("<td align=\"center\">"+ckiss+"</td>")) ;
            text.append(newline("<td align=\"center\">"+epal+"</td>")) ;
            text.append(newline("</tr>")) ;
            text.append(newline("<tr>")) ;
            text.append(newline("<td colspan=\"10\">"+descr+"</td>")) ;
            text.append(newline("</tr>")) ;
         }

         // Close the HTML page.

         Calendar date = Calendar.getInstance() ;
         String datestring = DateFormat.getDateInstance().format(date.getTime()) ;
         text.append(newline("</table>")) ;
         text.append(newline("</center>")) ;
         text.append(newline("</div>")) ;
         text.append(newline("<p>&nbsp;</p>")) ;
//       text.append(newline("<p align=\"center\"><a href=\"index.html\" target=\"_top\">Return</a></p>")) ;
//       text.append(newline("<p align=\"center\">&nbsp;</p>")) ;
         text.append("<p align=\"center\"><font size=\"2\">Generated by WebSearch V1.1<br>") ;
         text.append(newline(datestring+"</font></p>")) ;
         text.append(newline("<p>&nbsp;</p>")) ;
         text.append(newline("</body>")) ;
         text.append(newline("</html>")) ;

         // Save the generated text object as a text file.  We rename it to an
         // HTML file on the write callback.

         formname = baselocation ;
         String directory = OptionsDialog.getHtmlDirectory() ;
         directory = convertSeparator(directory) ;
         if (!directory.endsWith(File.separator)) directory += File.separator ;
         s = formname.toLowerCase() ;
         if (s.endsWith(".html") || s.endsWith(".htm"))
            formname = formname.substring(0,formname.lastIndexOf('.')) ;
         formname += ".txt" ;
         try
         {
            File f = new File(directory) ;
            f.mkdirs() ;
            f = new File(f,formname) ;
            if (f.exists()) f.delete() ;
            boolean savehtmlform = f.createNewFile() ;
            if (savehtmlform) kisekae.saveText(this,directory,formname,text) ;
            return savehtmlform ;
         }
         catch (IOException ioex)
         {
            PrintLn.println(ioex.toString()) ;
            webframe.addTrace("IOException, unable to write "+directory,2) ;
            webframe.addTrace("Ensure "+OptionsDialog.getHtmlDirectory()+" is write enabled.",2) ;
            return false ;
         }
      }
      catch (Exception e)
      {
         PrintLn.println("BuildForm: " + e) ;
         e.printStackTrace() ;
         return false ;
      }
   }


   // A function to return a newline terminated string.

   private String newline(String s) { return (s+"\n") ; }


   // A function to return the form name.

   public static String getFormName() { return formname ; }


   // A function to get the form size.

   public static int getFormSize() { return state.size() ; }


   // A method to shut us down.

   static void stopsearch()
   {
      stop = true ;
      synchronized (queue) { queue.notify() ; }
   }

   // A method to reset our static counts.

   static void reset()
   {
      count = 0 ; bytes = 0 ;
      stop = false ;
   }


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
            String s = "HTML Form " + formname + " saved" ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("BuildForm: " + s) ;
            webframe.addTrace(s) ;
            synchronized (queue) { queue.notify() ; }
            return ;
         }

         // If there is an error recognize the exception and continue.

         catch (Throwable ex)
         {
            Thread thread = Thread.currentThread() ;
            PrintLn.println(thread.getName() + " " + ex) ;
            ex.printStackTrace() ;
            synchronized (queue) { queue.notify() ; }
         }
      }
   }
}