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
* ValidateLinks class
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
import javax.swing.text.*;
import javax.swing.text.html.*;
import Kisekae.Kisekae ;
import Kisekae.OptionsDialog ;
import Kisekae.PrintLn ;


class ValidateLinks implements Runnable, ActionListener
{
   private static int count = 0 ;                  // Count of invokations
   private static int bytes = 0 ;                  // Total bytes loaded
   private static int activecount = 0 ;            // Active threads
   private static boolean stop = false ;           // Signal to stop thread
   private static Vector setstate = new Vector() ; // State result returned
   private boolean savedataset = false ;           // True if save in progress
   private boolean savethumbnail = false ;         // True if save thumbnail
   private String setname = null ;                 // Actual kiss set name
   private String writename = null ;               // Set name as written
   private String baselocation = null ;            // Web site location
   private String thumbname = null ;               // Actual thumbnail name
   private String statestring = null ;             // Set state information
   private Vector archives = new Vector() ;        // Archives to test
   private WebSearchFrame webframe = null ;        // Our parent frame
   private String location = null ;                // Current archive name
   private URL parseurl = null ;                   // Current archive URL
   private Kisekae kisekae = null ;                // The Kisekae loader

   private static Object queue = new Object() ;    // Our synchronized wait


   // Constructor.  The input vector contains all the archives isolated
   // consolidated through the GetLinks threads.  The validated state
   // information for the archives is saved in our setstate vector.

   public ValidateLinks(WebSearchFrame web, Vector v)
   {
      webframe = web ;
      archives = v ;
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
      activecount++ ;
      Thread thread = Thread.currentThread() ;
      thread.setName("ValidateLinks-" + count) ;
      if (OptionsDialog.getDebugSearch())
         PrintLn.println(thread.getName() + " started.") ;
      webframe.vallinkactive = true ;
      kisekae = Kisekae.getKisekae() ;
      kisekae.setCallback(this) ;

      // Process each entry.

      try
      {
         for (int i = 0 ; i < archives.size() ; i++)
         {
            count++ ;
            location = (String) archives.elementAt(i) ;
            if (validate(location))
            {
               synchronized (queue)
               {
                  try { queue.wait() ; }
                  catch (InterruptedException e) { stop = true ; }
               }
            }
         }
      }
      catch (Throwable e) { }
      
      activecount-- ;
      if (activecount <= 0)
      {
         String s = (bytes  / 1024) + "K" ;
         webframe.addTrace("End Archive Validation. Pages: " + count + ". Bytes: " + s,1) ;
         webframe.vallinkactive = false ;
         activecount = 0 ;
         
         // Run the callback on the EDT thread.
         
         Runnable runner = new Runnable()
         { public void run() { webframe.valcallback.doClick() ; } } ;
         javax.swing.SwingUtilities.invokeLater(runner) ;        
      }
      
      kisekae.removeCallback(this) ;
      if (OptionsDialog.getDebugSearch())
         PrintLn.println(thread.getName() + " ends.") ;
  }



   // A function to invoke UltraKiss to load and validate the archive.

   private boolean validate(String s)
   {
      if (stop) return false ;
      try
      {
         if (OptionsDialog.getDebugSearch())
            PrintLn.println("ValidateLinks: begin validation for " + s) ;
         kisekae.init(s,OptionsDialog.getDownloadSize(),webframe) ;
         return true ;
      }
      catch (Throwable e)
      {
         Thread thread = Thread.currentThread() ;
         PrintLn.println(thread.getName() + " " + e) ;
         e.printStackTrace();
         return false ;
      }
   }



   // A function to save an archive state.  The state information is
   // returned in a packet of the following form:
   //
   // [0] String  setname
   // [1] Long    compressed size (bytes)
   // [2] Integer cnf entry count
   // [3] Integer palettes
   // [4] Integer cels
   // [5] Integer audio
   // [6] Integer video
   // [7] Integer fkiss level
   // [8] Boolean ckiss
   // [9] Boolean epalette

   private void savestate()
   {
      try
      {
         String directory = OptionsDialog.getDataDirectory() ;
         directory = convertSeparator(directory) ;
         if (!directory.endsWith(File.separator)) directory += File.separator ;
         directory += baselocation + File.separator ;
         Object [] state = kisekae.getState() ;
         if (state == null) return ;
         setname = (String) state[0] ;
         Long size = (Long) state[1] ;
         Integer entrycount = (Integer) state[2] ;
         Integer palettes = (Integer) state[3] ;
         Integer cels = (Integer) state[4] ;
         Integer audio = (Integer) state[5] ;
         Integer video = (Integer) state[6] ;
         Integer fkiss = (Integer) state[7] ;
         Boolean ckiss = (Boolean) state[8] ;
         Boolean epalette = (Boolean) state[9] ;
         statestring = location + "\t" + setname + "\t" + size + "\t"
            + entrycount + "\t" + palettes + "\t" + cels  + "\t"
            + audio + "\t" + video + "\t" + fkiss + "\t"
            + ((ckiss.booleanValue()) ? "Yes" : "No") + "\t"
            + ((epalette.booleanValue()) ? "Yes" : "No") ;

         // Save the loaded KiSS set.  If we are using default options
         // then remote downloads are saved as zip format files.  

         try
         {
            savedataset = false ;
            File f = new File(directory) ;
            f.mkdirs() ;
            writename = setname ;
            
            boolean b = OptionsDialog.getSaveArchive() ;
            if (OptionsDialog.getUseDefaultWS())
               b = !webframe.isLocalSearch() ;
            if (!b) return ;
            b = OptionsDialog.getSaveAsZip() ;
            if (OptionsDialog.getUseDefaultWS())
               b = !webframe.isLocalSearch() ;
            if (b && !writename.endsWith("zip")) writename += ".zip" ;
            
            f = new File(f,writename) ;
            if (f.exists()) f.delete() ;
            savedataset = f.createNewFile() ;
            String s = f.getAbsolutePath() ;
            if (!s.startsWith(File.separator)) s = File.separator + s ;
            s = convertSeparator("file://"+s) ;
            if (savedataset) statestring = s + "\t" + setname + "\t" 
               + size + "\t" + entrycount + "\t" + palettes + "\t" 
               + cels  + "\t" + audio + "\t" + video + "\t" + fkiss + "\t"
               + ((ckiss.booleanValue()) ? "Yes" : "No") + "\t"
               + ((epalette.booleanValue()) ? "Yes" : "No") ;
         }
         catch (IOException ioex)
         {
            savedataset = false ;
            PrintLn.println(ioex.toString()) ;
            webframe.addTrace("IOException, unable to write "+directory,2) ;
            webframe.addTrace("Ensure "+OptionsDialog.getDataDirectory()+" is write enabled.",2) ;
         }
         if (savedataset)
            kisekae.saveSet(this,directory,writename);
      }
      catch (Throwable e)
      {
         Thread thread = Thread.currentThread() ;
         PrintLn.println(thread.getName() + " " + e) ;
         e.printStackTrace();
      }
   }


   // A function to save a thumbnail image.

   private void saveimage()
   {
      try
      {
         String directory = OptionsDialog.getImageDirectory() ;
         directory = convertSeparator(directory) ;
         if (!directory.endsWith(File.separator)) directory += File.separator ;
         directory += baselocation + File.separator ;
         int i = setname.lastIndexOf('.') ;
         thumbname = (i < 0) ? setname : setname.substring(0,i) ;
         thumbname += ".gif" ;

         try 
         { 
            savethumbnail = false ;
            if (!OptionsDialog.getSaveImage()) return ;
            File f = new File(directory) ;
            f.mkdirs() ;
            f = new File(f,thumbname) ;
            if (f.exists()) f.delete() ;
            if (OptionsDialog.getDebugSearch())
               PrintLn.println("ValidateLinks: createNewFile " + f.getPath()) ;
            savethumbnail = f.createNewFile() ; 
         }
         catch (IOException ioex)
         {
            PrintLn.println(ioex.toString()) ;
            webframe.addTrace("IOException, unable to write "+directory,2) ;
            webframe.addTrace("Ensure "+OptionsDialog.getImageDirectory()+" is write enabled.",2) ;
         }
         if (savethumbnail)
            kisekae.saveThumbnail(this,directory,thumbname,
               OptionsDialog.getThumbnailWidth(),OptionsDialog.getThumbnailHeight());
      }

      catch (Throwable e)
      {
         Thread thread = Thread.currentThread() ;
         PrintLn.println(thread.getName() + " " + e) ;
         e.printStackTrace();
      }
   }


   // A method to shut us down.

   static void stopsearch()
   {
      stop = true ;
//      synchronized (queue) { queue.notify() ; }
      count = 0 ;                  // Count of invokations
      bytes = 0 ;                  // Total bytes loaded
      activecount = 0 ;            // Active threads
      setstate = new Vector() ;    // State result returned
   }

   // A method to return our result.

   static Vector getSetState() { return setstate ; }

   // A method to reset our static counts.

   static void reset()
   {
      count = 0 ; bytes = 0 ;
      setstate = new Vector() ; 
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
      if (s1.startsWith("file:")) s = s.replace('\\','/') ;
      return s ;
   }

   

   // Our action listener to recognize Kisekae load completion events and file
   // save events.

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource() ;

      // Kisekae callbacks occur after an UltraKiss load completion.  

      if (e.getActionCommand().equals("Kisekae Callback"))
      {
         if (!stop) 
         {
            boolean b = kisekae.isLoaded() ;
            webframe.addTrace("Validate " + location + " " + b,(b) ? 3 : 2) ;
            if (kisekae.isLoaded())
            {
               savestate() ;
               if (savedataset && !stop) return ;
               saveimage() ;
               if (savethumbnail && !stop) return ;
            }
         }
         synchronized (queue) { queue.notify() ; }
         return ;
      }

      // File saves are sequenced.  First is the data set save, then the
      // thumbnail image save.

      if (e.getActionCommand().equals("FileWriter Callback"))
      {
         try
         {
            // If our data set was saved, save the thumbnail image.

            if (savedataset)
            {
               savedataset = false ;
               String s2 = OptionsDialog.getDataDirectory() ;
               s2 = convertSeparator(s2) ;
               String s = "Archive " + setname + " saved to " + s2 ;
               if (OptionsDialog.getDebugSearch())
                  PrintLn.println("ValidateLinks: " + s) ;
               webframe.addTrace(s) ;
               if (OptionsDialog.getSaveImage()) saveimage() ;
               if (savethumbnail && !stop) return ;
            }

            // If our thumbnail image was saved, retain the HTML state.

            if (savethumbnail)
            {
               savethumbnail = false ;
               String s2 = OptionsDialog.getImageDirectory() ;
               s2 = convertSeparator(s2) ;
               String s = "Thumbnail image " + thumbname + " saved to " + s2 ;
               if (OptionsDialog.getDebugSearch())
                  PrintLn.println("ValidateLinks: " + s) ;
               webframe.addTrace(s) ;
               
               // Get the directories
               
               String s1 = OptionsDialog.getHtmlDirectory() ;
               s1 = convertSeparator(s1) ;
               if (!s1.endsWith(File.separator)) s1 += File.separator ;
               if (!s2.endsWith(File.separator)) s2 += File.separator ;
               if (s2.startsWith(s1)) s2 = s2.substring(s1.length()) ;
               
               // Append the image and description attributes to the state.
               
               statestring += "\t" + s2 + baselocation + "/" + thumbname ;
               statestring = convertSeparator(statestring) ;
               String descr = kisekae.getDescription() ;
               if (descr == null) descr = "No description." ;  else descr += " ..." ;
               statestring += "\t" + descr ;
               setstate.addElement(statestring) ;
            }

            // Continue with the next queue entry.

            synchronized (queue) { queue.notify() ; }
            return ;
         }

         // If there is an error recognize the exception and continue.

         catch (Throwable ex)
         {
            Thread thread = Thread.currentThread() ;
            PrintLn.println(thread.getName() + " " + ex) ;
            ex.printStackTrace() ;
            savedataset = false ;
            savethumbnail = false ;
            synchronized (queue) { queue.notify() ; }
         }
      }
   }
}