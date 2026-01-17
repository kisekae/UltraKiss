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
* TextObject class
*
* Purpose:
*
* This class is a KiSS object container for arbitrary text files.  It is
* used to encapsulate a JTextComponent as a KiSS object to facilitate file
* saves.
*
*/

import java.io.* ;
import java.net.* ;
import java.awt.* ;
import java.awt.print.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.text.rtf.* ;
import javax.swing.text.html.* ;
import javax.swing.plaf.* ;
import javax.swing.plaf.basic.* ;

final class TextObject extends KissObject
   implements Printable
{
	// Class attributes.  Sized for 85 objects.

	static private Hashtable key = new Hashtable(100,0.855f) ;

	// Text object attributes.

	private JTextComponent text = null ;	 	      // The text component
   private EditorKit kit = null ;				      // The editor kit
   private String mode = null ;                    // File mode

   // Print references.

   private PageFormat pageformat = null ;				// The current page format
   private StyledPrintView sprintview = null ;		// The styled print view
   private PlainPrintView pprintview = null ;		// The plain print view


	// Constructor

	public TextObject(JTextComponent t) { this(null,t,null) ; } 
   
	public TextObject(ArchiveEntry z, JTextComponent t, EditorKit k)
	{
		ze = z ;
      kit = k ;
		text = t ;
		if (ze == null) return ;
		file = ze.getPath() ;
		zip = ze.getZipFile() ;
	}


   // Constructor to open a named file in an archive.

   public TextObject(ArchiveFile z, String path, String rw)
   {
      zip = z ;
      kit = null ;
      file = path ;
      mode = rw ;
      ze = zip.getEntry(file) ;
	   text = new JTextArea() ;
      if (ArchiveFile.isRichText(file)) setEditorKit(new RTFEditorKit()) ;
      if (ArchiveFile.isHTMLText(file)) setEditorKit(new HTMLEditorKit()) ;
	}


	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// configuration identifier.

	static void clearTable(Object cid)
	{
      if (cid == null) cid = new String("Unknown") ;
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}


	// Function to find a Text object by name.

	static TextObject findTextObject(String name, Configuration c, FKissEvent event)
	{
      if (c == null) return null ;
		if (name == null || name.length() == 0) return null ;

		// See if we have a literal name.

		if (name.charAt(0) == '\"')
		{
			int i = name.lastIndexOf('\"') ;
			if (i < 1) return null ;
			name = name.substring(1,i) ;
		}

		// Otherwise, look for a variable name.

		else
      {
			Object o = c.getVariable().getValue(name,event) ;
			if (!(o instanceof String)) return null ;
			name = o.toString() ;
      }

      // Locate the file in our hash table.

      name = name.toUpperCase() ;
		TextObject c1 = (TextObject) TextObject.getByKey(TextObject.getKeyTable(),c.getID(),name) ;
      if (c1 != null) return c1 ;
      if (name.indexOf('\\') >= 0)
      {
         name = name.replace('\\','/') ;
   		return (TextObject) TextObject.getByKey(TextObject.getKeyTable(),c.getID(),name) ;
      }
      else if (name.indexOf('/') >= 0)
      {
         name = name.replace('/','\\') ;
   		return (TextObject) TextObject.getByKey(TextObject.getKeyTable(),c.getID(),name) ;
      }
		return null ;
	}


   // Method to write the text contents.  Text only documents are written
   // as plain text.  Styled documents are written as RTF or HTML formatted
   // text.

   int write(FileWriter fw, OutputStream out, String type) throws IOException
   {
   	if (out == null) return 0 ;
      if (text == null) return 0 ;

      // Get the document length.

     	Document doc = text.getDocument() ;
      int length = doc.getLength() ;

      // Write styled text if we have a known editor kit.

     	if (kit != null)
      {
         try { kit.write(out,doc,0,length); }
         catch (BadLocationException e)
         { throw new IOException(e.getMessage()) ; }
         if (fw != null) fw.updateProgress(length) ;
         return length ;
      }

      // Write plain text as the default.

		Writer w = new OutputStreamWriter(out) ;
     	text.write(w) ;
      if (fw != null) fw.updateProgress(length) ;
      return length ;
	}


   // Return a writable indicator.

	boolean isWritable() { return true ; }


   // Return a rich text indicator.

	boolean isStyledText() { return (text instanceof JTextPane) ; }


   // Return our document size.

	int getBytes()
   {
      if (text == null) return 0 ;
     	Document doc = text.getDocument() ;
      return (doc != null) ? doc.getLength() : 0 ;
   }

   // Return our zip entry size.

	long getSize()
   {
      if (ze == null) return 0 ;
      return ze.getSize() ;
   }


   // Return our text component.

	JTextComponent getTextComponent() { return text ; }


   // Get the text contents.

	String getText()
   {
      if (text == null) return "" ;
      try { return text.getText() ; }
      catch (Exception e)
      {
         PrintLn.println("TextObject: " + toString() + " " + e.toString());
         e.printStackTrace() ;
         return "" ;
      }
   }


   // Get the text body contents.

	String getBody()
   {
      if (text == null) return "" ;
      if (!ArchiveFile.isHTMLText(file)) return getText() ;
      String s = text.getText() ;
      int i = s.indexOf("<body>") ;
      if (i < 0) return s ;
      int j = s.indexOf("</body>") ;
      if (j < 0) j = s.length() ;
      return s.substring(i+6,j) ;
   }


   // Set the text contents.

	void setText(String s)
   {
      if (text == null) return ;
      try { text.setText(s) ; }
      catch (Exception e)
      {
         PrintLn.println("TextObject: setText " + e.toString()) ;
      }
   }

   // Find a particular element in a styled document.

   private Element findElement(Element parent, String name)
   {
      Element foundElement = null;
      Element thisElement = null;
      int count = parent.getElementCount();
      int i = 0;
      while (i < count && foundElement == null)
      {
         thisElement = parent.getElement(i);
         if(thisElement.getName().equalsIgnoreCase(name))
         {
            foundElement = thisElement;
         }
         else
         {
            foundElement = findElement(thisElement, name);
         }
         i++;
      }
      return foundElement;
   }

   // Return our editor kit.

	EditorKit getEditorKit() { return kit ; }

   // Return our document.

	Document getDocument() { return (text == null) ? null: text.getDocument() ; }

   // Return an input stream to read the document.

   InputStream getInputStream()
   {
      if (text == null) return null ;
      String s = text.getText() ;
      if (s == null) return null ;
      return new ByteArrayInputStream(s.getBytes()) ;
   }


   // Set the editor kit for cases where we change the document style.
   // A null kit implies we have a standard text document.

	void setEditorKit(EditorKit k)
   {
   	JTextPane styledtext = null ;
      JTextArea normaltext = null ;
      if (text == null) return ;

      // Convert to a styled document.

      if (kit == null && k != null)
      {
         Document doc = null ;
         StyleContext context = new StyleContext() ;
         if (k instanceof RTFEditorKit)
            doc = new DefaultStyledDocument(context) ;
         if (k instanceof HTMLEditorKit)
            doc = new HTMLDocument() ;
         if (doc != null)
         {
   	  		styledtext = new JTextPane() ;
            styledtext.setEditorKit(k) ;
            try { doc.insertString(0,text.getText(),new SimpleAttributeSet()) ; }
            catch (BadLocationException e)
            { PrintLn.println("TextObject: " + e.getMessage()) ; }
            styledtext.setDocument(doc) ;
         }
      }

      // Convert to a normal document.

      if (kit != null && k == null)
      {
      	String s = "" ;
	  		normaltext = new JTextArea() ;
         normaltext.setLineWrap(false) ;
         normaltext.setWrapStyleWord(false) ;
         normaltext.setTabSize(3) ;
         Document doc = text.getDocument() ;
      	if (doc != null)
         {
         	try { s = doc.getText(0,doc.getLength()) ; }
            catch (BadLocationException e) { }
         }
         normaltext.setText(s) ;
      }

      // Update our state.

      kit = k ;
      if (styledtext != null) text = styledtext ;
      if (normaltext != null) text = normaltext ;
   }


   // Set up to read the file contents.   Watch for errors.

   int read() throws IOException  { return read(zip,ze) ; }
   int read(ArchiveFile zip, ArchiveEntry ze) throws IOException
   {
      InputStream in = null ;
      String encoding = Kisekae.getLanguageEncoding() ;
		int bytes = (ze == null) ? 0 : (int) ze.getSize() ;
		if (OptionsDialog.getDebugLoad())
			PrintLn.println("TextObject: read " + file + " [" + bytes + " bytes]") ;
		if (ze != null)
			in = (zip == null) ? null : zip.getInputStream(ze) ;

		// Read the file contents.

		if (in != null)
		{
         InputStreamReader isr = new InputStreamReader(in,encoding) ;
   		text.read(isr,file) ;
		}
      else
      {
         bytes = -1 ;
  			if (OptionsDialog.getDebugLoad())
  				PrintLn.println("TextObject: unable to obtain input stream for " + file) ;
      }
      return bytes ;
   }


   // Print interface method.   This method knows how to render a page
   // for printing.  Note that we must call this method with an unprintable
   // page after printing to ensure that the print views are reset on
   // the next print request.

   public int print(Graphics g, PageFormat pageformat, int pageindex)
   {
      if (g == null || pageformat == null)
      {
			sprintview = null ;
         pprintview = null ;
   		return Printable.NO_SUCH_PAGE ;
      }

   	// Set the graphics context to the printable area.

      int x = (int) pageformat.getImageableX() ;
      int y = (int) pageformat.getImageableY() ;
      int iw = (int) pageformat.getImageableWidth() ;
      int ih = (int) pageformat.getImageableHeight() ;
      int width = (int) pageformat.getWidth() ;
      int height = (int) pageformat.getHeight() ;
      Graphics g2 = g.create(0,0,width,height) ;

      // Create our printview object used to render a page.  The printview
      // object knows how to paginate and render a page.

		try
		{
			if (sprintview == null && pprintview == null)
			{
         	if (kit != null)
            {
					BasicTextUI btui = (BasicTextUI) text.getUI() ;
					View root = btui.getRootView(text) ;
					Document doc = text.getDocument() ;
	            Element elem = doc.getDefaultRootElement() ;
					sprintview = new StyledPrintView(elem,root,x,y,iw,height) ;
            }
            else
            {
            	pprintview = new PlainPrintView(file,text,x,y,iw,height) ;
            }
			}

			// Render a page.

			boolean exists = false ;
         if (sprintview != null)
         	exists = sprintview.paintPage(g2,ih,pageindex) ;
         if (pprintview != null)
         	exists = pprintview.paintPage(g2,ih,pageindex) ;
         g2.dispose() ;
			Runtime.getRuntime().gc() ;
			if (exists) return Printable.PAGE_EXISTS ;
			sprintview = null ;
         pprintview = null ;
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("TextObject: Out of memory during print.") ;
		}

		// Watch for internal faults while printing.

		catch (Throwable e)
		{
			PrintLn.println("TextObject: Internal print fault.") ;
			e.printStackTrace() ;
		}
		return Printable.NO_SUCH_PAGE ;
	}


   // Editing operations on the text document
   // ---------------------------------------

   void append(Object o1)
   {
      if (o1 == null) return ;
      if (text == null) return ;
      String s = text.getText() ;
      String s1 = s + o1.toString() ;
      if (ArchiveFile.isHTMLText(file))
      {
         int i = s.indexOf("</body>") ;
         if (i >= 0) s1 = s.substring(0,i) + o1.toString() + s.substring(i) ;
      }
      try { text.setText(s1) ; }
      catch (Exception e)
      {
         PrintLn.println("TextObject: append " + e.toString()) ;
      }
   }

   int delete(Object o1)
   {
      if (o1 == null) return -1 ;
      String find = o1.toString() ;
      String doctext = text.getText() ;
      int i = doctext.indexOf(find) ;
      if (i < 0) return -1 ;
      int j = i + find.length() ;
      String newtext = doctext.substring(0,i) + doctext.substring(j) ;
      text.setText(newtext) ;
      return i ;
   }

   int replace(Object o1, Object o2)
   {
      if (o1 == null || o2 == null) return -1 ;
      String find = o1.toString() ;
      String replace = o2.toString() ;
      String doctext = text.getText() ;
      int i = doctext.indexOf(find) ;
      if (i < 0) return -1 ;
      int j = i + find.length() ;
      String newtext = doctext.substring(0,i) + replace + doctext.substring(j) ;
      text.setText(newtext) ;
      return i ;
   }

   int replaceall(Object o1, Object o2)
   {
      if (o1 == null || o2 == null) return -1 ;
      int start = 0 ;
      String find = o1.toString() ;
      String replace = o2.toString() ;
      String doctext = text.getText() ;
      int i = doctext.indexOf(find,start) ;
      if (i < 0) return -1 ;

      // Replace all occurances.

      int count = 0 ;
      StringBuffer sb = new StringBuffer() ;
      while (i >= 0)
      {
         int j = i + find.length() ;
         sb.append(doctext.substring(start,i)) ;
         sb.append(replace) ;
         start = i + find.length() ;
         i = doctext.indexOf(find,start) ;
         count++ ;
      }
      sb.append(doctext.substring(start)) ;
      text.setText(sb.toString()) ;
      return count ;
   }

   int find(Object o1)
   {
      if (o1 == null) return -1 ;
      String find = o1.toString() ;
      String doctext = text.getText() ;
      int i = doctext.indexOf(find) ;
      return i ;
   }

   int findLine(Object o1)
   {
      if (o1 == null) return -1 ;
      String find = o1.toString() ;
      String doctext = text.getText() ;
      int i = doctext.indexOf(find) ;
      if (i < 0) return -1 ;
      
      try
      {
         JTextArea ta = null ;
         if (text instanceof JTextArea)
            ta = (JTextArea) text ;
         if (text instanceof JTextPane)
            ta = new JTextArea(((JTextPane) text).getText()) ;
         i = ta.getLineOfOffset(i) ;
      }
      catch (Exception e) { i = -1 ; }
      return i ;
   }

   int getLine(Object o1)
   {
      if (!(o1 instanceof Integer)) return -1 ;
      int n = ((Integer) o1).intValue() ;
       
      try
      {
         JTextArea ta = null ;
         if (text instanceof JTextArea)
            ta = (JTextArea) text ;
         if (text instanceof JTextPane)
            ta = new JTextArea(((JTextPane) text).getText()) ;
         n = ta.getLineOfOffset(n) ;
      }
      catch (Exception e) { n = -1 ; }
      return n ;
   }


	// The toString method returns a string representation of this object.
	// This is the class name concatenated with the object identifier.

	public String toString()
	{ return super.toString() + " " + getName() ; }
}
