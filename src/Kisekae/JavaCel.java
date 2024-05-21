package Kisekae ;

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
* JavaCel class
*
* Purpose:
*
* This class encapsulates a Java object.  These objects can be used as cels.
* The object knows how to draw itself.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;
import java.net.MalformedURLException;
import java.util.* ;
import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import java.net.URL ;

final class JavaCel extends Cel 
	implements HyperlinkListener
{
	// Class attributes.  Sized for 16 component types.

	static private Hashtable componentnumber = new Hashtable(20,0.855f) ;
   
   private JavaCel me = null ;                  // Ourselves
   private String type = null ;                 // Component type
	private String currentattr = null ;          // Current attributes
	private String currentweb = null ;           // Current web for textpane
   private PanelFrame panel = null ;            // Panel for redraws
   private JComponent component = null ;        // Java component
   private JComponent showcomponent = null ;    // Actual component
   private JTextComponent text = null ;         // Text component
   private JEditorPane pane = null ;            // HTML text component
   private JList list = null ;                  // List component
   private JComboBox combobox = null ;          // ComboBox component
   private JScrollPane scroll = null ;          // Scroll pane
   private JPanel borderpanel = null ;          // Border panel
   private Insets insets = null ;               // Border insets
   private Color bordercolor = null ;           // Border color
   private Configuration ref = null ;           // Load reference
   private String borderstyle = "" ;            // Border style
   private String bordertitle = "" ;            // Border title
   private String bevelstyle = "" ;             // Border bevel style
   private String inittext = null ;             // Initial text
   private Font initfont = null ;               // Initial font
   private Color initbackground = null ;        // Initial background
   private Color initforeground = null ;        // Initial foreground 
   private int next = 0 ;                       // Next selection index
	private int border = 0 ;			  	         // The component border size
   
   private boolean show = true ;                // If true, component can show
   private boolean imported = false ;           // If true, component was added
   private boolean readonly = false ;           // If true, component is read
   private boolean flushed = false ;            // If true, component is flushed


   // Listeners for events on the component

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
         Vector v = getEvent("press") ;
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
         v = getEvent("release") ;
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
      }
	} ;

	ItemListener itemListener = new ItemListener()
   {
		public void itemStateChanged(ItemEvent e)
      {
         Vector v = getEvent("press") ;
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
         v = getEvent("release") ;
			EventHandler.queueEvents(v,Thread.currentThread(),this) ;
      }
	} ;

	ActionListener actionListener = new ActionListener()
   {
		public void actionPerformed(ActionEvent e)
      {
         Object o = getGroup() ;
         if (o instanceof Group)
         {
            Vector v = ((Group) o).getEvent("press") ;
            EventHandler.queueEvents(v,Thread.currentThread(),this) ;
            v = ((Group) o).getEvent("release") ;
            EventHandler.queueEvents(v,Thread.currentThread(),this) ;
         }
         Vector v = getEvent("press") ;
         EventHandler.queueEvents(v,Thread.currentThread(),this) ;
         v = getEvent("release") ;
         EventHandler.queueEvents(v,Thread.currentThread(),this) ;
         MainFrame mf = Kisekae.getMainFrame() ;
         PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
         if (pf != null) pf.requestFocus() ;
      }
	} ;

	MouseListener mouseListener = new MouseListener()
   {
		public void mousePressed(MouseEvent e)
      {
         if (panel == null) return ;

         // If right mouse button pressed, show a context dialog.

         if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
         {
            Object o = me.getGroup() ;
            Group g = (o instanceof Group) ? (Group) o : null ;
            panel.showpopup(e.getComponent(),e.getX(),e.getY(),me,g) ;
            return ;
         }
      }
      public void mouseReleased(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
	} ;


	// Hyperlink event listener.  This is for TextPane components.
   // We process LZH, ZIP and JAR file downloads with our UrlLoader. 
   // Normal hyperlinks switch to the required page.

  	public void hyperlinkUpdate(HyperlinkEvent evt)
  	{
      if (currentweb == null) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
      WebFrame webframe = (menu != null) ? menu.getWebFrame() : null ;
         
      if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) 
      {
         URL evturl = evt.getURL() ;
         String description = (evturl != null) ? evturl.toExternalForm() : "" ;
         if (mf != null) 
         {
            mf.showStatus(description) ;
            mf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ;
         }
         return ;
      }
      
      if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) 
      {
         if (mf != null) 
         {
            mf.showStatus(null) ;
            mf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         }
         return ;
      }
            
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
      {
         URL evturl = evt.getURL() ;
         if (evturl == null) return ;       
         String description = evt.getDescription() ;
         
         // If the URL could not be decoded, parse the description.
         
         try
         {
            if (description.startsWith("file:") || description.startsWith("jar:"))
               evturl = new URL(description) ;
            URL context = new URL(currentweb) ;
            if (context.toString().startsWith("http:"))
               evturl = new URL(context,description) ;
            else if (context.toString().startsWith("https:"))
               evturl = new URL(context,description) ;
            else if (context.toString().startsWith("file:"))
               evturl = new URL("file:" + description) ;
            else if (context.toString().startsWith("jar:"))
               evturl = new URL(context,description) ;
         }
         catch (MalformedURLException e) { evturl = null ; }         
         if (evturl == null) return ;

         // If the link is to an archive, load it.

         if (webframe == null) 
         {
            webframe = new WebFrame(mf,currentweb) ;
            if (menu != null) menu.setWebFrame(webframe) ;
         }
         String urlname = evturl.toExternalForm() ;
         System.out.println("JavaCel: " + getName() + " hyperlink to " + urlname) ;
         if (ArchiveFile.isArchive(urlname))
            { webframe.loadArchive(evturl,true) ; return ; }

         // If the link is to an image file prompt for download.

         if (ArchiveFile.isImage(urlname))
            { webframe.loadArchive(evturl,true) ; return ; }

         // If the link is to a media file prompt for download.

         if (ArchiveFile.isAudio(urlname))
            { webframe.loadArchive(evturl,true) ; return ; }

         // If our current web is on our local file system and we link to
         // a remote HTML page, then we adjust our current web site to point
         // to the new remote web.

         String s = currentweb ;
         if (s == null) s = "" ;
         int n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         if ((s.startsWith("file:") || s.startsWith("jar:")) && 
              (urlname.startsWith("http:") || urlname.startsWith("https:")))
         {
            currentweb = urlname ;
            s = currentweb ;
            n = s.lastIndexOf('/') ;
            if (n > 0) s = s.substring(0,n) ;
            webframe.setHosted(s);
         }
      
         // Not all internal links on remote URL's start with www.  If it is 
         // missing we correct the URL.
      
         boolean inframe = false ;
         String authority = evturl.getAuthority() ;
         n = (authority != null) ? s.indexOf(authority) : -1 ;
         if (n >= 0) 
         {
            inframe = true ;
            int m = urlname.indexOf(authority) ;
            urlname = s.substring(0,n) + urlname.substring(m) ;
         }

         // If the link is to a page on the current web site, or if we are
         // linking to a local file, we show the hyperlink in this frame.

         if (urlname.startsWith("file:")) inframe = true ;
         if (urlname.startsWith("jar:")) inframe = true ;
         if (inframe)
         {
            Vector v = webframe.getHistory() ;
            for (int i = v.size()-1 ; i > webframe.getHistoryLocation() ; i--)
               v.removeElementAt(i) ;
            webframe.setHistoryLocation(v.size()) ;

            // If our URL is to a file, we need to determine if this is to
            // a directory or a specific file.

            if (urlname.startsWith("file:") && !urlname.endsWith("/"))
            {
               String s1 = evturl.getPath() ;
               File f = new File(s1) ;
               if (f.isDirectory()) urlname += File.separator ;
            }

            // Add this URL to our history list and show in the browser.

            v.addElement(urlname) ;
            webframe.setPage(urlname,this) ;
            return ;
         }

         // If the link is outside our current web site we show it in a browser
         // window.

         BrowserControl.displayURL(urlname) ;
      }
   }


	// Constructor

	public JavaCel(String type, String file, Configuration ref)
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { JavaCel1(type,file,ref) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         JavaCel1(type,file,ref) ;
   }

	private void JavaCel1(String type, String file, Configuration ref)
	{
		this.type = type ;
		this.file = convertSeparator(file) ;
      this.ref = ref ;
      me = this ;

      // Duplicate referenced components are copies of the first component.

      load() ;
      if (component != null) return ;
      insets = new Insets(0,0,0,0) ;
      borderpanel = new BorderPanel() ;
      createComponent(type) ;
      setComponentNumber(file,ref.getID()) ;
      if (initbackground == null)
         initbackground = component.getBackground() ;
      if (initforeground == null)
         initforeground = component.getForeground() ;
      if (initfont == null)
         initfont = component.getFont() ;
      
      // Attach our mouse listener to the actual component.

      Component c = component ;
      if (c instanceof JPanel)
      {
         try { c = ((JPanel) c).getComponent(0) ; }
         catch (ArrayIndexOutOfBoundsException e) { }
      }
      if (c instanceof JScrollPane)
      {
         JViewport view = ((JScrollPane) c).getViewport() ;
         if (view != null) c = view.getView() ;
      }
      if (c != null) c.addMouseListener(mouseListener);
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: constructor for "+getName()+" result=\""+c+"\""+" on thread "+Thread.currentThread());               
	}


   // Create a new component.

   void createComponent(String type)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { createComponent1(type) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         createComponent1(type) ;      
   }
      
   private void createComponent1(String type)
   {
      if ("label".equals(type))
      {
         component = new JLabel() ;
         MainFrame mf = Kisekae.getMainFrame() ;
         PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
         Color c = (pf != null) ? pf.getBackground() : Color.black ;
         int rgb = (c.getRGB() ^ 0xFFFFFF) ;
         rgb = (rgb ^ 0) | 0xFF000000 ;
         ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER) ;
         ((JLabel) component).setForeground(new Color(rgb)) ;
         ((JLabel) component).setText("") ;
         initinput = false ;
      }

      if ("textbox".equals(type))
      {
         text = new JTextArea() ;
         text.setText("") ;
         component = text ;
         initinput = false ;
      }

      if ("textarea".equals(type))
      {
         text = new JTextArea() ;
         text.setText("") ;
         initbackground = text.getBackground() ;
         initforeground = text.getForeground() ;
         initfont = text.getFont() ;
         scroll = new JScrollPane(text) ;
         component = scroll ;
         initinput = true ;
      }

      if ("textpane".equals(type))
      {
         pane = new JEditorPane() ;
         pane.setContentType("text/html");
         pane.setEditorKit(new WebHTMLEditor());
         pane.addHyperlinkListener(this);
         pane.setText("") ;
         initbackground = pane.getBackground() ;
         initforeground = pane.getForeground() ;
         initfont = pane.getFont() ;
         scroll = new JScrollPane(pane) ;
         component = scroll ;
         initinput = true ;
      }

      if ("textfield".equals(type))
      {
         text = new JTextField() ;
         text.setText("") ;
         component = text ;
         initinput = true ;
      }

      if ("passwordfield".equals(type))
      {
         text = new JPasswordField() ;
         text.setText("") ;
         component = text ;
         initinput = true ;
      }

      if ("button".equals(type))
      {
         component = new JButton() ;
         ((JButton) component).setText("") ;
         initinput = false ;
      }

      if ("togglebutton".equals(type))
      {
         component = new JToggleButton() ;
         ((JToggleButton) component).setText("") ;
         ((JToggleButton) component).addActionListener(actionListener);
         initinput = true ;
      }

      if ("checkbox".equals(type))
      {
         component = new JCheckBox() ;
         ((JCheckBox) component).setText("") ;
         ((JCheckBox) component).addActionListener(actionListener);
         initinput = true ;
      }

      if ("radiobutton".equals(type))
      {
         component = new JRadioButton() ;
         ((JRadioButton) component).setText("") ;
         ((JRadioButton) component).addActionListener(actionListener);
         initinput = true ;
      }

      if ("list".equals(type))
      {
         DefaultListModel dlm = new DefaultListModel() ;
         list = new JList() ;
         list.setModel(dlm) ;
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.addListSelectionListener(listListener);
         initbackground = list.getBackground() ;
         initforeground = list.getForeground() ;
         initfont = list.getFont() ;
         scroll = new JScrollPane(list) ;
         component = scroll ;
         initinput = true ;
      }

      if ("combobox".equals(type))
      {
         DefaultComboBoxModel dcbm = new DefaultComboBoxModel() ;
         combobox = new JComboBox() ;
         combobox.setModel(dcbm) ;
         combobox.addItemListener(itemListener);
         combobox.setEditable(true) ;
         component = combobox ;
         initinput = true ;
      }

      if ("menuitem".equals(type))
      {
         component = new JMenuItem() ;
         ((JMenuItem) component).setText("") ;
         ((JMenuItem) component).addActionListener(actionListener);
         initinput = false ;
      }

      if ("checkboxmenuitem".equals(type))
      {
         component = new JCheckBoxMenuItem() ;
         ((JMenuItem) component).setText("") ;
         ((JMenuItem) component).addActionListener(actionListener);
         initinput = true ;
      }

      if ("menuseparator".equals(type))
      {
         component = new JLabel("menuSeparator") ;
         initinput = false ;
      }
      
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: createComponent for "+getName()+" result=\""+type+"\""+" on thread "+Thread.currentThread());               
	}


   // Return the next unique component numer of the specified type
   // defined in the configuration. We use this number to ensure 
   // that unique component names are defined. If our type is not
   // yet defined we return 1.

   static int getNextComponentNumber(String type, Object cid)
   {
      Object o = null ;
      if (cid == null) return 0 ;
      if (type == null) return 0 ;
      String key = cid + "-" + type ;
      o = componentnumber.get(key.toUpperCase()) ;
      if (!(o instanceof Integer)) return 1 ;
      return ((Integer) o).intValue() + 1 ;
   }
 
   
   // Set our last used component number. Our component file name should
   // be of the form nameN.ext for the specified type. We need to isolate N.
   
   static void setComponentNumber(String file, Object cid)
   {
      int i = 0 ;
      if (cid == null) return ;
      if (file == null) return ;
      int n = file.lastIndexOf('.') ;
      if (n < 0) return ;
      
      // Isolate the digits.
      
      StringBuffer N = new StringBuffer("") ;
      for (i = n-1 ; i >= 0 ; i--)
      {
         char c = file.charAt(i) ;
         if (!Character.isDigit(c)) break ;
         N.append(c) ;
      }
      N.reverse() ;
      if (N.length() == 0) return ;
      Integer max = new Integer(N.toString()) ;
      
      // Determine if we have a valid type name.

      String type = file.substring(0,i+1) ;
      if (!ArchiveFile.isComponent("."+type)) return ;
      
      // Save N as the new maximum component number.

      String key = cid + "-" + type ;
      Object o = componentnumber.get(key.toUpperCase()) ;
      if (o == null || ((Integer) o).intValue() < max.intValue())
         componentnumber.put(key.toUpperCase(),max) ;
   }

   
	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// file.

	static void clearTable(Object cid)
	{
		Enumeration e = componentnumber.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) componentnumber.remove(hashkey) ;
		}
   }


   // Reset the initial text state of the component.  Also reset any button
   // selection states.

   void reset()
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { reset1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         reset1() ;
   }

   private void reset1()
   {
      if ("menuseparator".equals(type)) return ;
      removeAll() ;
      setEditable(true) ;
      setAttributes(attributes) ;
      setText(inittext) ;
      
      if (component instanceof AbstractButton)
         ((AbstractButton) component).setSelected(false) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: reset for "+getName()+" on thread "+Thread.currentThread());               
   }


   // Reset the initial attribute state of the component.

   void resetAttributes()
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { resetAttributes1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         resetAttributes1() ;
   }

   private void resetAttributes1()
   {
      if (showcomponent != null) 
      {
         boolean showing = show ;
         showComponent(false) ;
         showcomponent = null ;
         showComponent(showing) ;
      }
      String s = "-wrap,-align,-read,-vsb,-hsb,-bc,-fc,-fontname," ;
      s += "-fontsize,-fontstyle,-multiple,-disabled,-margin" ;
      s += "-borderstyle,-insets,-border,-bdc" ;
      setAttributes(s) ;
      setAttributes(attributes) ;
      currentattr = null ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: resetAttributes for "+getName()+" on thread "+Thread.currentThread());               
   }


   // Method to determine if the component must be painted.

   boolean mustPaint() { return (!input && image == null) ; }

   
   // Return a writable file indicator. For input components this is false
   // unless we are read only. Scrollable components are not writable.
   // Otherwise, if we are exporting components as cels or had imported 
   // a component as a cel it is true.

   boolean isWritable() 
   { 
      if (input && !readonly) return false ;
      if (component instanceof JScrollPane) return false ;
      if (OptionsDialog.getComponentCel()) return true ; 
      if (OptionsDialog.getImportComponent() && isImported()) return true ; 
      return false ;
   }

   
   // Return the writable offset state. For components this is false unless
   // we are exporting components as cels or have imported our component as
   // a cel.

   static boolean getWriteableOffset() { return false ; }
   boolean isWriteableOffset() 
   { 
      if (ze == null) return false ;
      if (!ze.isWriting()) return false ;
      if (!OptionsDialog.getComponentCel() && ze.isComponent()) return false ;
      
      // If we are exporting our component as an image type, then
      // our writable offset state depends on the type of image
      // we are supposed to be.
      
      String s = ze.getName() ;
      if (s == null) return false ;
      s = s.toLowerCase() ;
      if (s.endsWith(".cel")) return KissCel.getWriteableOffset() ;
      else if (s.endsWith(".gif")) return GifCel.getWriteableOffset() ;
      else if (s.endsWith(".jpg")) return JpgCel.getWriteableOffset() ;
      else if (s.endsWith(".bmp")) return BmpCel.getWriteableOffset() ;
      else if (s.endsWith(".png")) return PngCel.getWriteableOffset() ;
      else if (s.endsWith(".pgm")) return PpmCel.getWriteableOffset() ;
      else if (s.endsWith(".pbm")) return PpmCel.getWriteableOffset() ;
      else if (s.endsWith(".ppm")) return PpmCel.getWriteableOffset() ;
      return false ;
   }



   // Component state changes
   // -----------------------

	// Abstract method implementations.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
      int bytes = 0 ;
      if (image == null) createImage() ;
      ImageEncoder encoder = getEncoder(fw,out,type) ;
      if (encoder == null)
      	throw new IOException("unable to encode component cel as " + type) ;
      if (!(encoder instanceof GifEncoder))
      {
   		encoder.encode() ;
         bytes = encoder.getBytesWritten() ;
         return bytes ;
      }
      
      try { encoder.encode() ; }
      catch (IOException e)
      {
         if ("Too many colors for a GIF image".equals(e.getMessage()))
         {
            Object [] reduced = this.dither(256) ;
            Image img = (Image) reduced[0] ;
            encoder = new GifEncoder(fw,img,null,0,null,
               transparentcolor,backgroundcolor,transparency,out) ;
            encoder.encode() ;
         }
         else throw e ;
      }
      bytes += encoder.getBytesWritten() ;
      return bytes ;
	}


	void load(Vector includefiles)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { load1(includefiles) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         load1(includefiles) ;
   }
   
	private void load1(Vector includefiles)
	{
      String name = getRelativeName() ;
      if (name != null) name = name.toUpperCase() ;

		// Access an existing component if it already exists.

      String copyname = name ;
      Object cid = (ref != null) ? ref.getID() : null ;
		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,copyname) ;
      if (c == null)
      {
         copyname = getPath().toUpperCase() ;
         c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,copyname) ;
      }

      // Watch for duplicate entries that may not all be loaded.

      if (c != null && !c.isLoaded() && Cel.hasDuplicateKey(Cel.getKeyTable(),cid,copyname))
         while (c != null && !c.isLoaded()) c = (Cel) c.getNextByKey(Cel.getKeyTable(),cid,copyname) ;

      // If we found a loaded copy, use it.

		if (c != null && c.isLoaded() && c instanceof JavaCel)
      {
      	Object [] o = ((JavaCel) c).getComponent() ;
      	component = (JComponent) o[0] ;
         text = (JTextComponent) o[1] ;
         list = (JList) o[2] ;
         combobox = (JComboBox) o[3] ;
         scroll = (JScrollPane) o[4] ;
         initinput = ((Boolean) o[5]).booleanValue() ;
//       image = (Image) o[6] ;
      }

      // Components are truecolor.

      cm = basecm = Palette.getDirectColorModel() ;
      truecolor = true ;
      loaded = true ;
      
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: load for "+getName()+" result=\""+c+"\""+" on thread "+Thread.currentThread());               
   }


   // Components are never copied.

	void loadCopy(Cel c) { }

   // Components are never unloaded as they may be reused.

	void unload() { }


   // Create an image of this component.  We do not appear to be able to paint
   // the component unless we are on the AWT thread.   Therefore we maintain
   // an image representation for non-input and read only components.

   void createImage()
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { createImage1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         createImage1() ;
   }

   private void createImage1()
   {
      if (input && !readonly) return ;
      if (component == null) return ;

      Component comp = component ;
      if (showcomponent != null) comp = showcomponent ;
      component.setSize(getSize()) ;
     	Dimension d = comp.getSize() ;
      int iw = d.width ;
      int ih = d.height ;
      if (iw == 0 || ih == 0) return ;

      // Obtain a graphics context for our current image.

		image = new BufferedImage(iw,ih,BufferedImage.TYPE_INT_ARGB) ;
      cm = Palette.getDirectColorModel() ;
      Graphics g = image.getGraphics() ;

      // Clear the image background.  The cleared image is transparent.
      // Note that ARGB values of 0 are not truly transparent (Java bug?).

      Color c = Color.black ;
      int rgb = c.getRGB() & 0xFFFFFF ;
      if (rgb == 0) rgb = 1 ;
      int n = iw * ih ;
		int [] rgbarray = new int[n] ;
   	for (int i = 0 ; i < n ; i++) 
         rgbarray[i] = rgb ;
		((BufferedImage) image).setRGB(0,0,iw,ih,rgbarray,0,iw) ;
      
      // If we are a label and have specified a background color then
      // we use it.
      
      if (component instanceof JLabel)
      {
         int i = -1 ;
         if (currentattr != null) i = currentattr.indexOf("bc=") ;
         char c1 = (i > 0) ? currentattr.charAt(i-1) : '-' ;
         boolean onoff = (c1 == '-') ? false : true ;
         if (onoff)
         {
            c = ((JLabel) component).getBackground() ;
            if (c != null) rgb = c.getRGB() ;
            for (i = 0 ; i < n ; i++) 
               rgbarray[i] = rgb ;
            ((BufferedImage) image).setRGB(0,0,iw,ih,rgbarray,0,iw) ;
         }
      }

      // Paint the component into the image. There has got to be a better
      // way to fix the paint problem for JScrollPane components.

      baseimage = image ;
      comp.paint(g) ;
  		g.dispose() ;
      
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: createImage for "+getName()+" result=\""+image+"\""+" on thread "+Thread.currentThread());               
   }
   
         
   // Function to setup the border panel.  Note, there is only one panel
   // per component. We reuse this panel for border changes as this is
   // the component added to the panel frame and must be referencable
   // during addComponent and removeComponent.
   
   private void createBorder(Component c) 
   { createBorder(c,borderpanel) ; }
   private void createBorder(Component c, JPanel p)
   {
      if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { createBorder1(c,p) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         createBorder1(c,p) ;
   }
   
   private void createBorder1(Component c, JPanel p)
   {
      Border b = null ;
      if (p == null) { showcomponent = null ; return ; }
      p.removeAll() ;
      p.setBorder(null) ;
      p.setLayout(new BorderLayout()) ;
      Color color = Color.black ;
      if (bordercolor != null) color = bordercolor ;
      int bevel = BevelBorder.LOWERED ;
      if ("raised".equals(bevelstyle)) bevel = BevelBorder.RAISED ;
      if ("line".equals(borderstyle))
         b = new LineBorder(color,border) ;
      else if ("bevel".equals(borderstyle))
         b = new BevelBorder(bevel) ;
      else if ("etched".equals(borderstyle))
         b = new EtchedBorder() ;
      else if ("matte".equals(borderstyle))
         b = new MatteBorder(insets,color) ;
      else if ("titled".equals(borderstyle))
      {
         b = new TitledBorder(bordertitle) ;
         ((TitledBorder) b).setTitleColor(color) ;
      }
      else 
         b = new LineBorder(color,border) ;

      // Add insets if specified.
         
      if (b != null && insets != null)
      {
         EmptyBorder eb = new EmptyBorder(insets) ;
         CompoundBorder cb = new CompoundBorder(b,eb) ;
         p.setBorder(cb) ;
      }
      else if (b != null)
         p.setBorder(b) ;

      // Add our component to the border panel.
      
      if (c != null)
      {
         c.setSize(getSize()) ;
         p.add(c,BorderLayout.CENTER) ;
         p.setLocation(c.getLocation()) ;
         p.setSize(getSize()) ;
      }
      showcomponent = p ;
   }


   // Invalidate all copies of this component image.

   void invalidateImage()
   {
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),getID(),getPath().toUpperCase()) ;
      while (c != null)
      {
         if (c instanceof JavaCel) c.setImage(null) ;
         c = (Cel) c.getNextByKey(Cel.getKeyTable(),getID(),getPath().toUpperCase()) ;
      }
   }


	// Set the component size.

	void setSize(Dimension d)
   {
      final Dimension param = d ;
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setSize1(d) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setSize1(d) ;    
   }
 
   // Perform the component update.
      
   private synchronized void setSize1(Dimension d)
   {
      invalidateImage() ;
      super.setSize(d) ;
      if (component != null) component.setSize(d) ;
      if (text != null) text.setSize(d) ;
      if (list != null) list.setSize(d) ;
      if (borderpanel != null) borderpanel.setSize(d) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setSize for "+getName()+" result=\""+d+"\""+" on thread "+Thread.currentThread());               
   }


	// Set the component drawable parent. If we are a label without a
   // foreground color, establish our contrasting color.

	void setPanel(PanelFrame p) 
   { 
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setPanel1(p) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setPanel1(p) ;    
   }
   
	private void setPanel1(PanelFrame p) 
   { 
      panel = p ; 
      if (!(component instanceof JLabel)) return ;
      if (currentattr != null && currentattr.indexOf("fc") >= 0) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
      Color c = (pf != null) ? pf.getBackground() : Color.black ;
      int rgb = (c.getRGB() ^ 0xFFFFFF) ;
      rgb = (rgb ^ 0) | 0xFF000000 ;
      ((JLabel) component).setForeground(new Color(rgb)) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setPanel for "+getName()+" on thread "+Thread.currentThread());               
   }


	// Set the component visible flag.

	void setVisible(boolean b)
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setVisible1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setVisible1(b) ;    
   }

	private void setVisible1(boolean b)
	{
      super.setVisible(b) ;
      showComponent(show) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setVisible for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
	}


	// Set the cel imported flag.

	void setImported(boolean b) { imported = b ; }


	// Get the cel imported flag.
   
   boolean isImported() { return imported ; }


	// Get the cel enabled flag.
   
   boolean isEnabled() { return getEnabled() != 0 ; }
   

   // Method to set the cel visibility without updating the group visibility.
   // We use this if we need to make the cel temporarily visible for drawing.

   void setVisibility(boolean b)
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setVisibility1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setVisibility1(b) ;    
   }

   private void setVisibility1(boolean b)
	{
      super.setVisibility(b) ;
      if (component == null) return ;
      component.setVisible(b) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setVisibility for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
	}


   // Set the component show flag.  Hidden components are removed from the
   // panel frame.

   void showComponent(boolean b)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { showComponent1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         showComponent1(b) ;    
   }

   private void showComponent1(boolean b)
   {
      show = b ;
      if (isVisible() && show && input) addComponent() ;
      else removeComponent() ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: showComponent for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to set text components editable.

   void setEditable(boolean b)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setEditable1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setEditable1(b) ;    
   }

   private void setEditable1(boolean b)
   {
      readonly = !b ;
      if (text != null) text.setEditable(b) ;
      if (pane != null) pane.setEditable(b) ;
      if (combobox != null) combobox.setEditable(b) ;
      if (component != null) component.setEnabled(b) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setEditable for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
   }


	// Set the component input flag.  Input components are added to the
   // panel frame.  Non-input components are drawn as normal cels.
   // Component input state can be changed dynamically.  Non-input
   // components can be picked up with the mouse and moved.  Input
   // components trap mouse events and cannot be moved.

	void setInput(boolean b)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setInput1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setInput1(b) ;    
   }

	private void setInput1(boolean b)
   {
      invalidateImage() ;
      super.setInput(b) ;
      if (b) addComponent() ; else removeComponent() ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setInput for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
   }


   // Set the initial component attributes.

   void setInitAttributes(String s)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setInitAttributes1(s) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setInitAttributes1(s) ;    
   }

   private void setInitAttributes1(String s)
   {
      attributes = s ;
      setAttributes(s) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setInitAttributes for "+getName()+" result=\""+s+"\""+" on thread "+Thread.currentThread());               
   }


	// Set the component attributes.  This will update the component image.
   // Temporary attribute changes through FKiSS do not update the current
   // attributes.

	void setAttributes(String s) { setAttributes(s,false) ; }
	void setAttributes(String s, boolean temp)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setAttributes1(s,temp) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setAttributes1(s,temp) ;    
   }
   
	private void setAttributes1(String s, boolean temp)
   {
      if (s == null) return ;
      String s1 = eraseLiterals(s) ;
		if (!temp) currentattr = eraseNullAttributes(s) ;
      invalidateImage() ;
      int i = 0 ;

      // Parse and apply attributes.

      if ((i = s1.indexOf("wrap")) >= 0)         // enable wordwrap
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (text instanceof JTextArea)
         {
            ((JTextArea) text).setLineWrap(onoff) ;
            ((JTextArea) text).setWrapStyleWord(onoff) ;
         }
         if (text instanceof JTextArea)
         {
            ((JTextArea) text).setLineWrap(onoff) ;
            ((JTextArea) text).setWrapStyleWord(onoff) ;
         }
         if (list != null)
         {
            Dimension d = component.getSize() ;
            ListCellRenderer renderer = new JavaText(d) ;
            list.setCellRenderer(renderer);
         }
      }

      if ((i = s1.indexOf("align")) >= 0)        // text alignment
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         
         if (component instanceof JLabel)
         {
            if ("left".equals(s2))
               ((JLabel) component).setHorizontalAlignment(SwingConstants.LEFT) ;
            if ("right".equals(s2))
               ((JLabel) component).setHorizontalAlignment(SwingConstants.RIGHT) ;
            if ("center".equals(s2))
               ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER) ;
            if (!onoff)
               ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER) ;
         }
         if (text instanceof JTextField)
         {
            if ("left".equals(s2))
               ((JTextField) text).setHorizontalAlignment(SwingConstants.LEFT) ;
            if ("right".equals(s2))
               ((JTextField) text).setHorizontalAlignment(SwingConstants.RIGHT) ;
            if ("center".equals(s2))
               ((JTextField) text).setHorizontalAlignment(SwingConstants.CENTER) ;
            if (!onoff)
               ((JTextField) text).setHorizontalAlignment(SwingConstants.LEFT) ;
         }
         if (text instanceof JTextArea)
         {
            if ("left".equals(s2))
               ((JTextArea) text).setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT) ;
            if ("right".equals(s2))
               ((JTextArea) text).setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT) ;
            if ("center".equals(s2))
               ((JTextArea) text).setComponentOrientation(ComponentOrientation.UNKNOWN) ;
            if (!onoff)
               ((JTextArea) text).setComponentOrientation(ComponentOrientation.UNKNOWN) ;
         }
         if (pane instanceof JEditorPane)
         {
            if ("left".equals(s2))
               ((JEditorPane) pane).setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT) ;
            if ("right".equals(s2))
               ((JEditorPane) pane).setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT) ;
            if ("center".equals(s2))
               ((JEditorPane) pane).setComponentOrientation(ComponentOrientation.UNKNOWN) ;
            if (!onoff)
               ((JEditorPane) pane).setComponentOrientation(ComponentOrientation.UNKNOWN) ;
         }
      }

      if ((i = s1.indexOf("noscroll")) >= 0)     // disable scrolling
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (scroll == component) removeComponent() ;
         if (text != null) component = text ;
         if (pane != null) component = pane ;
         if (list != null) component = list ;
         scroll = null ;
      }

      if ((i = s1.indexOf("read")) >= 0)         // enable read only
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         setEditable(!onoff) ;
   //      setInput((!onoff) ? initinput : false) ;
      }

      if ((i = s1.indexOf("vsb")) >= 0)          // enable vertical scroll bars
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (scroll != null)
         {
            if (onoff)
               scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS) ;
            else
               scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER) ;
         }
      }

      if ((i = s1.indexOf("hsb")) >= 0)          // enable horizontal scroll bars
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (scroll != null)
         {
            if (onoff)
               scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) ;
            else
               scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) ;
         }
      }

      if ((i = s1.indexOf("bc")) >= 0)           // set background color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            Color c1 = component.getBackground() ;
            if (c1 == null) c1 = Color.black ;
            int rgb = c1.getRGB() ;
            try { rgb = Integer.parseInt(s2) ; }
            catch (NumberFormatException e) { }
            Color bc = new Color(rgb) ;
            if (!onoff) bc = initbackground ;
            component.setBackground(bc);
            if (text != null) text.setBackground(bc);
            if (list != null) list.setBackground(bc);
         }
      }

      if ((i = s1.indexOf("fc")) >= 0)           // set foreground color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            Color c1 = component.getForeground() ;
            if (c1 == null) c1 = Color.white ;
            int rgb = c1.getRGB() ;
            try { rgb = Integer.parseInt(s2) ; }
            catch (NumberFormatException e) { }
            Color fc = new Color(rgb) ;
            if (!onoff) fc = initforeground ;
            component.setForeground(fc);
            if (text != null) text.setForeground(fc);
            if (list != null) list.setForeground(fc);
         }
      }

      if ((i = s1.indexOf("trans")) >= 0)      // set transparent background
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? true : false ;
         component.setOpaque(onoff); 
         if (component instanceof JButton)
            ((JButton) component).setContentAreaFilled(false);
      }
      
      if ((i = s1.indexOf("fontname")) >= 0)    // set the font
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            Font f = component.getFont() ;
            if (f == null) return ;
            int size = f.getSize() ;
            int style = f.getStyle() ;
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            f = new Font(s2,style,size) ;
            if (!onoff) f = initfont ;
            component.setFont(f);
            if (text != null) text.setFont(f);
            if (list != null) list.setFont(f);
         }
      }

      if ((i = s1.indexOf("fontsize")) >= 0)    // set the font size
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            Font f = component.getFont() ;
            if (!onoff) f = initfont ;
            if (f == null) return ;
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            float n = f.getSize2D() ;
            try { n = Float.parseFloat(s2) ; }
            catch (NumberFormatException e) { }
            f = f.deriveFont(n) ;
            component.setFont(f);
            if (text != null) text.setFont(f);
            if (list != null) list.setFont(f);
         }
      }

      if ((i = s1.indexOf("fontstyle")) >= 0)    // set the font attributes
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            Font f = component.getFont() ;
            if (!onoff) f = initfont ;
            if (f == null) return ;
            String s2 = s.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;
            int n = f.getStyle() ;
            if ("bold".equals(s2)) n = Font.BOLD ;
            if ("italic".equals(s2)) n = Font.ITALIC ;
            if ("plain".equals(s2)) n = Font.PLAIN ;
            f = f.deriveFont(n) ;
            component.setFont(f);
            if (text != null) text.setFont(f);
            if (list != null) list.setFont(f);
         }
      }

      if ((i = s1.indexOf("multiple")) >= 0)    // set multiple selections
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (list != null)
         {
            if (onoff)
               list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            else
               list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         }
      }

      if ((i = s1.indexOf("disabled")) >= 0)    // set disabled state
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         setEnabled(!onoff) ;
      }

      if ((i = s1.indexOf("selected")) >= 0)    // set selected state
      {
         setSelected(new Integer(1)) ;
      }

      if ((i = s1.indexOf("text")) >= 0)        // set component text
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component != null)
         {
            String s2 = s.substring(i) ;
            int j = findTextEnd(s2) ;
            if (j > 0 && s2.charAt(j) == '\"') j++ ;
            if (j < 0) j = s2.length() ;
            int k = s2.indexOf('=') ;
            if (k+1 > j) j = k + 1 ;
            s2 = s2.substring(k+1,j) ;
            s2 = Variable.getStringLiteralValue(s2) ;

            // Convert newline literals in the text.

            int i1 = 0 ;
            int j1 = s2.indexOf("\\n",i1) ;
            StringBuffer sb = new StringBuffer("") ;
            while (j1 >= 0)
            {
               sb.append(s2.substring(i1,j1)) ;
               sb.append('\n') ;
               i1 = j1 + 2 ;
               j1 = s2.indexOf("\\n",i1) ;
            }
            sb.append(s2.substring(i1)) ;
            s2 = sb.toString() ;

            // Set the text.

            if (!onoff) s2 = (inittext != null) ? inittext : "" ;
            if (component instanceof JLabel) ((JLabel) component).setText(s2) ;
            if (component instanceof AbstractButton) ((AbstractButton) component).setText(s2) ;
            if (component instanceof JMenuItem) ((JMenuItem) component).setText(s2) ;
            if (text != null) text.setText(s2) ;
            if (pane != null) pane.setText(s2) ;
            if (inittext == null) inittext = s2 ;
         }
      }

      if ((i = s1.indexOf("nomargin")) >= 0)    // set no margin
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (component instanceof JButton)
         {
            if (onoff)
               ((JButton) component).setMargin(new Insets(0,0,0,0)) ;
            else
               ((JButton) component).setMargin(null) ;
         }
      }

      if ((i = s1.indexOf("insets")) >= 0)    // set insets
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf("\",") ;
         if (j > 0 && s2.charAt(j) == '\"') j++ ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         StringTokenizer st = new StringTokenizer(s2,", ") ;
         int top = 0 ;
         int left = 0 ;
         int bottom = 0 ;
         int right = 0 ;
         if (st.hasMoreTokens()) top = parseInset(st.nextToken()) ;
         if (st.hasMoreTokens()) left = parseInset(st.nextToken()) ;
         if (st.hasMoreTokens()) bottom = parseInset(st.nextToken()) ;
         if (st.hasMoreTokens()) right = parseInset(st.nextToken()) ;
         insets = new Insets(top,left,bottom,right) ;
         if (text instanceof JTextArea)
         {
            ((JTextArea) text).setMargin(insets) ;
         }
         if (pane instanceof JEditorPane)
         {
            ((JEditorPane) pane).setMargin(insets) ;
         }
      }

      if ((i = s1.indexOf("borderstyle")) >= 0)        // set the border style
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         boolean showing = show ;
         showComponent(false) ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         borderstyle = Variable.getStringLiteralValue(s2) ;
         if (!onoff) borderstyle = "" ;
         
         // If no style, cancel any prior border.
         
         if ("".equals(borderstyle))
         {
            if (showcomponent != null)
            {
               component.setLocation(showcomponent.getLocation()) ;
               component.setSize(getSize()) ;
            }
            showcomponent = null ;
         }
         else
            createBorder(component) ;
         showComponent(showing) ;
      }

      if ((i = s1.indexOf("border")) >= 0)        // set the border size
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         boolean showing = show ;
         showComponent(false) ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try { border = Integer.parseInt(s2) ; }
         catch (NumberFormatException e) { }
         if (border < 0) border = 0 ;
         if (!onoff) border = 0 ;
         
         // If no border size, cancel any prior border.
         
         if (border == 0)
         {
            if (showcomponent != null)
            {
               component.setLocation(showcomponent.getLocation()) ;
               component.setSize(getSize()) ;
            }
            showcomponent = null ;
         }
         else if ("line".equals(borderstyle) || "".equals(borderstyle))
            createBorder(component) ;
         showComponent(showing) ;
      }

      if ((i = s1.indexOf("bordertitle")) >= 0)        // set the border title
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         boolean showing = show ;
         showComponent(false) ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         bordertitle = Variable.getStringLiteralValue(s2) ;
         if (!onoff) bordertitle = "" ;
         if ("titled".equals(borderstyle))
            createBorder(component) ;
         showComponent(showing) ;
      }

      if ((i = s1.indexOf("bevelstyle")) >= 0)        // set the border bevel
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         boolean showing = show ;
         showComponent(false) ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         bevelstyle = Variable.getStringLiteralValue(s2) ;
         if (!onoff) bevelstyle = "" ;
         if ("bevel".equals(borderstyle))
            createBorder(component) ;
         showComponent(showing) ;
      }

      if ((i = s1.indexOf("bdc")) >= 0)           // set border color rgb
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         boolean showing = show ;
         showComponent(false) ;
         String s2 = s.substring(i) ;
         int j = s2.indexOf(',') ;
         if (j < 0) j = s2.length() ;
         int k = s2.indexOf('=') ;
         if (k+1 > j) j = k + 1 ;
         s2 = s2.substring(k+1,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
         try 
         { 
            int rgb = Integer.parseInt(s2) ; 
            bordercolor = new Color(rgb) ;
            if (!onoff) bordercolor = null ;
         }
         catch (NumberFormatException e) { }

         // Show the border component.
         
         createBorder(component) ;
         showComponent(showing) ;
      }
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setAttributes for "+getName()+" result=\""+s+"\""+" on thread "+Thread.currentThread());               
   }

   
   // Function to parse an integer.
   
   private int parseInset(String s)
   {
      int n = 0 ;
      try { n = Integer.parseInt(s) ; }
      catch (NumberFormatException e) { }
      return n ;
   }
   
      
  // Function to erase all string literals in the comment.
   
   private String eraseLiterals(String s)
   {
      if (s == null) return null ;
      s = s.toLowerCase() ;
      StringBuffer sb = new StringBuffer(s) ;
      boolean inliteral = s.startsWith("\"") ;
      for (int i = 1 ; i < sb.length() ; i++)
      {
         if (sb.charAt(i) == '"' && sb.charAt(i-1) != '\\') 
            inliteral = !inliteral ;
         else if (inliteral && sb.charAt(i) != '\\') 
            sb.replace(i,i+1," ") ;
      }
      return sb.toString() ; 
   }
   
      
   // Function to determine the end of a Text literal specification.  The text
   // string is delimited by double quotes and followed by a comma.  However, 
   // the Text literal can also contain quoted strings inside the literal that
   // are also followed by a comma.  So, we need to ensure that it is the first
   // quote that is being terminated.
   
   private int findTextEnd(String s)
   {
      if (s == null) return -1 ;
      int j = s.indexOf("\",") ;
      
      // Is this the true end? 

      while (j >= 0)
      {
         int quotecount = 0 ;
         for (int i = 0 ; i <= j ; i++)
            if (s.charAt(i) == '\"') quotecount++ ;
         if (quotecount % 2 == 0) return j ;
         j = s.indexOf("\",",j+1) ;
      }
      return j ;
   }
   
      
  // Function to erase all attribute negation entries.
   
   private String eraseNullAttributes(String s)
   {
      if (s == null) return null ;
      boolean innegation = false ;
      String s1 = eraseLiterals(s) ;
      StringBuffer sb = new StringBuffer() ;
      for (int i = 0 ; i < s1.length() ; i++)
      {
         if (s1.charAt(i) == '-') innegation = true ;
         if (!innegation) sb.append(s.charAt(i)) ;
         if (s1.charAt(i) == ',') innegation = false ;
      }
		s = sb.toString() ;
		if (s.endsWith(",")) s = s.substring(0,s.length()-1) ;
		return s ;
   }


   // Method to enable the component.

   void setEnabled(boolean b)
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setEnabled1(b) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setEnabled1(b) ;    
   }

   private void setEnabled1(boolean b)
   {
      if (component == null) return ;
      component.setEnabled(b) ;
      invalidateImage() ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setEnabled for "+getName()+" result=\""+b+"\""+" on thread "+Thread.currentThread());               
   }


   // Return the component objects.

   Object [] getComponent()
   {
      Object [] o = new Object[7] ;
      o[0] = component ;
      o[1] = (text != null) ? text : pane ;
      o[2] = list ;
      o[3] = combobox ;
      o[4] = scroll ;
      o[5] = new Boolean(initinput) ;
      o[6] = image ;
      return o ;
   }


   // Return the component type.

   String getType() { return type ; }


   // Return the relative name which will be of type CEL if we are 
   // writeable. Non-input components will be written as a truecolor  
   // CEL if the ComponentCel option has been set.

   String getWriteName() 
   { 
      String name = super.getRelativeName() ;
      if (!isWritable()) return name ;
      if (ze == null) return name ;
      if (!ze.isWriting()) return name ;
      int n = name.lastIndexOf('.') ;
      if (n >= 0) name = name.substring(0,n) + ".cel" ;
      return name ;
   }


	// Return the current attributes. If we are writing
   // as a CEL file then we have no attributes.

	String getAttributes()
	{
      if (ze != null && ze.isWriting()) return null ;
		if (currentattr == null) return super.getAttributes() ;
		return new String(currentattr) ;
	}


	// Return the current attributes. If we are writing
   // as a CEL file then we have no attributes.

	String getAttribute(String s)
	{
      if (s == null) return "" ;
      String attributes = getAttributes() ;
      if (attributes == null) return "" ;
      String s1 = eraseLiterals(attributes) ;
      s = Variable.getStringLiteralValue(s.toLowerCase()) ;
      int n1 = s1.indexOf(s) ;
      if (n1 < 0) return "" ;
      n1 += s.length() ;
      if (n1 < attributes.length() && attributes.charAt(n1) == '=') n1++ ;
      int n2 = s1.indexOf(',',n1) ;
      if (n2 < 0) n2 = s1.length() ;
      if (n2 == n1) return "true" ; 
      String s2 = attributes.substring(n1,n2) ;
      s2 = Variable.getStringLiteralValue(s2) ;
      return s2 ;
	}
   

   // Return the cel base image size.  If we are writing
   // as a CEL file then our size is defined by the image.

   Dimension getBaseSize() 
   { 
      if (ze != null && ze.isWriting()) return null ;
      return super.getBaseSize() ; 
   }
   

   // Return the cel preferred size.  This is the initial size
   // for the cel when it is created.

   Dimension getPreferredSize() 
   { 
      Dimension d = new Dimension(100,50) ;
      Font f = component.getFont() ;
      if (f == null) return d ;
      FontMetrics fm = component.getFontMetrics(f) ;
      if ("checkbox".equals(type)) d.height = fm.getHeight() + 10 ;
      if ("textfield".equals(type)) d.height = fm.getHeight() + 10 ;
      if ("passwordfield".equals(type)) d.height = fm.getHeight() + 10 ;
      if ("combobox".equals(type)) d.height = fm.getHeight() + 10 ;
      return d ;
   }



   // Component accessors and modifiers
   // ---------------------------------

   // Method to set the component text.  This will update the component image.
   // Non-editable and scrollable TextArea components have the caret postion
   // set to the end of text, otherwise the text is displayed from the start.
   //
   // In many cases these must run on the AWT thread as they modify a Swing
   // component.  If we switch to the AWT thread we suspend the invoking
   // thread until the AWT thread finishes.  

   void setText(String s)
   {
      final String param = s ;
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setText1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setText1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void setText1(String s)
   {
      invalidateImage() ;
      if (s == null) s = "" ;
      if (text != null)
         text.setText(s) ;
      else if (component instanceof JLabel)
         ((JLabel) component).setText(s) ;
      else if (component instanceof AbstractButton)
         ((AbstractButton) component).setText(s) ;
      else if (pane instanceof JEditorPane)
         setPage(s) ;
      if (text instanceof JTextArea && scroll != null)
         if (!((JTextArea) text).isEditable())
            ((JTextArea) text).setCaretPosition(s.length()) ;
      if (OptionsDialog.getDebugComponent())
          System.out.println("JavaCel: setText for "+getName()+" result=\""+s+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the component text.

   synchronized String getText()
   {
      if (component instanceof JPasswordField)
         return new String(((JPasswordField) component).getPassword()) ;
      if (component instanceof JLabel)
         return ((JLabel) component).getText() ;
      if (component instanceof AbstractButton)
         return ((AbstractButton) component).getText() ;
      if (text != null)
         return text.getText() ;
      return "" ;
   }

   void setPage(String s)
   {
      final String param = s ;
		if (!SwingUtilities.isEventDispatchThread())
      {
   		Runnable runner = new Runnable()
   		{ public void run() { setPage1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setPage1(param) ;    
   }

 
   // Perform the component update
      
   private synchronized void setPage1(String s)
   {
      invalidateImage() ;
      if (s == null) return ;
      URL url = null ;
      
      try { url = new URL(s) ; }
      catch (Exception e)
      { url = Kisekae.class.getClassLoader().getResource(s) ; }
      
      if (url == null && ref != null)
      {
         try
         {
            ArchiveFile af = ref.getZipFile() ;
            String dir = af.getDirectoryName() ;
            File f = new File(dir,s) ;
     			URL codebase = Kisekae.getBase() ;
   			if (codebase == null) codebase = new URL("file","",-1,"") ;
     			String protocol = codebase.getProtocol() ;
     			String host = codebase.getHost() ;
     			int port = codebase.getPort() ;
     			url = new URL(protocol,host,port,f.getPath()) ;
         }
         catch (Exception e) { return ; }           
      }
      
      try 
      {
         if (pane != null)
         {
            Document doc = pane.getDocument();
            doc.putProperty(Document.StreamDescriptionProperty, null);
            currentweb = (url != null) ? url.toExternalForm() : null ;
            if (url != null) pane.setPage(url) ;
         }
      }
      catch (Exception e)
      {
         System.out.println("Error Loading URL " + url + " Message: " + e.toString());         
      }
   }

   // Method to set the current list selection.  For a list that supports
   // multiple selections the new value is added to the selection set.

   void setSelectedValue(Object value)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { setSelectedValue1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setSelectedValue1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void setSelectedValue1(Object value)
   {
      if (list != null)
      {
         list.removeListSelectionListener(listListener) ;
         int i = list.getSelectionMode() ;
         if (i != ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            list.setSelectedValue(value,true) ;
         else
         {
            int [] selected = list.getSelectedIndices() ;
            int len = selected.length ;
            list.setSelectedValue(value,false) ;
            i = list.getSelectedIndex() ;
            if (i >= 0)
            {
               int [] newselection = new int[len+1] ;
               System.arraycopy(selected,0,newselection,0,len);
               newselection[len] = i ;
               list.setSelectedIndices(newselection) ;
               if (len == 0) list.ensureIndexIsVisible(i) ;
            }
         }
         list.addListSelectionListener(listListener) ;
      }
      if (combobox != null)
      {
         combobox.setSelectedItem(value) ;
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setSelectedValue for "+getName()+" result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the list or combo box selection.

   synchronized String getSelectedValue()
   {
      if (list != null)
      {
         Object o = list.getSelectedValue() ;
         if (o != null) return o.toString() ;
      }
      if (combobox != null)
      {
         Object o = combobox.getSelectedItem() ;
         if (o != null) return o.toString() ;
      }
      return "" ;
   }


   // Method to select a specific entry in the list.  An invalid index
   // clears any existing list selection.  For a list that supports multiple
   // selections the new value is added to the selection set.

   void setSelectedIndex(int n)
   {
      final int param = n ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { setSelectedIndex1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setSelectedIndex1(n) ;    
   }
 
   // Perform the component update
      
   private synchronized void setSelectedIndex1(int n)
   {
      if (list != null)
      {
         list.removeListSelectionListener(listListener);
         if (n < 0 || n >= list.getModel().getSize())
         {
            list.clearSelection() ;
            next = 0 ;
         }
         else
         {
            int i = list.getSelectionMode() ;
            if (i != ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            {
               list.setSelectedIndex(n) ;
               list.ensureIndexIsVisible(n) ;
            }
            else
            {
               int [] selected = list.getSelectedIndices() ;
               int len = selected.length ;
               int [] newselection = new int[len+1] ;
               System.arraycopy(selected,0,newselection,0,len);
               newselection[len] = n ;
               list.setSelectedIndices(newselection) ;
               if (len == 0) list.ensureIndexIsVisible(n) ;
            }
         }
         list.addListSelectionListener(listListener);
      }
      if (combobox != null)
      {
         if (n < -1 || n >= combobox.getModel().getSize()) return ;
         combobox.setSelectedIndex(n) ;
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setSelectedIndex for "+getName()+" result=\""+n+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the list or combobox selection.

   synchronized int getSelectedIndex()
   {
      if (list != null) return list.getSelectedIndex() ;
      if (combobox != null) return combobox.getSelectedIndex() ;
      return -1 ;
   }


   // Method to get the next list selection for multiple selections.
   // Once we return a no-selection indicator we restart from the beginning.

   synchronized int getNextSelectedIndex()
   {
      if (list != null)
      {
         int [] n = list.getSelectedIndices() ;
         int selected = (next < n.length) ? n[next] : -1 ;
         next++ ;
         if (selected < 0) next = 0 ;
         return selected ;
      }
      if (combobox != null) return combobox.getSelectedIndex() ;
      return -1 ;
   }


   // Method to set the combobox selection.

   void setSelectedItem(Object value)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { setSelectedItem1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setSelectedItem1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void setSelectedItem1(Object value)
   {
      if (combobox != null) 
         combobox.setSelectedItem(value) ;
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setSelectedItem for "+getName()+" result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the combobox selection.

   synchronized String getSelectedItem()
   {
      if (combobox == null) return "" ;
      Object o = combobox.getSelectedItem() ;
      if (o != null) return o.toString() ;
      return "" ;
   }


   // Method to set the selected state of the component.

   void setSelected(Object value)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
         Runnable runner = new Runnable()
			{ public void run() { setSelected1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setSelected1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void setSelected1(Object value)
   {
      boolean b = false ;
      if (value instanceof String && value.toString().equalsIgnoreCase("true")) b = true ;
      if (value instanceof String && value.toString().equalsIgnoreCase("1")) b = true ;
      if (value instanceof Integer && ((Integer) value).intValue() != 0) b = true ;
      if (component instanceof AbstractButton)
        ((AbstractButton) component).setSelected(b) ;
      if (list != null && list.getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
      {
         ListModel model = list.getModel() ;
         if (model instanceof DefaultListModel)
         {
            DefaultListModel dlm = (DefaultListModel) model ;
            int n = dlm.getSize() ;
            if (b) list.setSelectionInterval(0,n-1) ; else list.clearSelection() ;
         }
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setSelected for "+getName()+" result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the selected state of the component.

   synchronized int getSelected()
   {
      if (!(component instanceof AbstractButton)) return -1 ;
      if (((AbstractButton) component).isSelected()) return 1 ;
      return 0 ;
   }


   // Method to get the enable state of the component.

   synchronized int getEnabled()
   {
      if (component == null) return -1 ;
      if (!component.isEnabled()) return 0 ;
      return 1 ;
   }


   // Method to set a value at a specific list point.  New entries can
   // be added to the list one at a time.  Entries begin at index 0.
   // This can update the component image.

   void setValueAt(Object value, int n)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { setValueAt1(param,n) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setValueAt1(param,n) ;    
   }
 
   // Perform the component update
      
   private synchronized void setValueAt1(Object value, int n)
   {
      invalidateImage() ;
      if (list != null)
      {
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return ;
         DefaultListModel dlm = (DefaultListModel) model ;
         if (n < 0 || n > dlm.getSize()) return ;
         if (n == dlm.getSize()) dlm.setSize(n+1) ;
         dlm.setElementAt(value,n) ;
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setValueAt for "+getName()+" result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get a value at a specific list index.

   synchronized String getValueAt(int n)
   {
      if (list != null)
      {
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return "" ;
         DefaultListModel dlm = (DefaultListModel) model ;
         if (n < 0 || n >= dlm.getSize()) return "" ;
         Object o = dlm.getElementAt(n) ;
         if (o == null) return "" ;
         return o.toString() ;
      }
      return "" ;
   }


   // Method to add an item to a combo box or list.  New entries can
   // be added to the list one at a time.  Entries begin at index 0.
   // This can update the component image.

   void addItem(Object value)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { addItem1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         addItem1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void addItem1(Object value)
   {
      invalidateImage() ;
      if (combobox != null) combobox.addItem(value) ;
      if (list != null)
      {
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return ;
         DefaultListModel dlm = (DefaultListModel) model ;
         dlm.addElement(value) ;
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: addItem for "+getName()+"  result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to remove an item from a combo box.  This can update the
   // component image.

   void removeItem(Object value)
   {
      final Object param = value ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { removeItem1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         removeItem1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void removeItem1(Object value)
   {
      invalidateImage() ;
      if (combobox != null) combobox.removeItem(value) ;
      if (list != null)
      {
         list.removeListSelectionListener(listListener);
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return ;
         DefaultListModel dlm = (DefaultListModel) model ;
         dlm.removeElement(value) ;
         list.addListSelectionListener(listListener);
      }
      setSelectedIndex(-1) ;
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: removeItem for "+getName()+" result=\""+value+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to get the index of an item in a list.

   synchronized int getIndexOf(Object value)
   {
      if (list != null)
      {
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return -1 ;
         DefaultListModel dlm = (DefaultListModel) model ;
         return dlm.indexOf(value) ;
      }
      if (combobox != null)
      {
         ComboBoxModel model = combobox.getModel() ;
         if (!(model instanceof DefaultComboBoxModel)) return -1 ;
         DefaultComboBoxModel dcbm = (DefaultComboBoxModel) model ;
         return dcbm.getIndexOf(value) ;
      }
      return -1 ;
   }


   // Method to remove all elements from a list or combo box.  This can
   // update the component image.

   
   void removeAll()
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { removeAll1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         removeAll1() ;    
   }
 
   // Perform the component update
      
   private synchronized void removeAll1()
   {
      invalidateImage() ;
      if (combobox != null) combobox.removeAllItems() ;
      if (list != null)
      {
         next = 0 ;
         list.removeListSelectionListener(listListener);
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return ;
         DefaultListModel dlm = (DefaultListModel) model ;
         dlm.removeAllElements() ;
         list.addListSelectionListener(listListener);
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: removeAll for "+getName()+"  result=\""+"DONE"+"\""+" on thread "+Thread.currentThread());               
   }


   // Method to return the size of a list or combo box.

   synchronized int getItemCount()
   {
      if (list != null)
      {
         ListModel model = list.getModel() ;
         if (!(model instanceof DefaultListModel)) return 0 ;
         DefaultListModel dlm = (DefaultListModel) model ;
         return dlm.getSize() ;
      }
      
      if (combobox != null) return combobox.getItemCount() ;
      return -1 ;
   }


   // Method to set a button icon. This can update the component image.

   void setIcon(Object img)
   {
      final Object param = img ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { setIcon1(param) ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         setIcon1(param) ;    
   }
 
   // Perform the component update
      
   private synchronized void setIcon1(Object img)
   {
      invalidateImage() ;
      if ((img instanceof Image) && (component instanceof JButton))
      {
         ImageIcon icon = new ImageIcon((Image) img) ;
         ((JButton) component).setIcon(icon) ;
      }
      if (OptionsDialog.getDebugComponent())
         System.out.println("JavaCel: setIcon for "+getName()+" result=\""+"DONE"+"\""+" on thread "+Thread.currentThread());               
   }



   // Cel implementation methods
   // --------------------------

	// Function to return the current pixel transparency at the
	// specified point.  This function returns -1 if the point is
	// outside the cel.

	int getAlpha(int x, int y)
   {
      Dimension s = getSize() ;
		if (x < 0 || x >= s.width) return -1 ;
		if (y < 0 || y >= s.height) return -1 ;
      if (input && !readonly) return 255 ;
      if (panel != null && panel.isSelected(this)) return 255 ;
      int alpha = super.getAlpha(x,y) ;
      return (alpha >= 0) ? alpha : 255 ;
   }


	// Function to return the current pixel at the specified point.  This
   // function returns -1 if the point is outside the cel. This returns
   // only the RGB value.

	int getRGB(int x, int y)
	{
      Dimension s = getSize() ;
		if (x < 0 || x >= s.width) return -1 ;
		if (y < 0 || y >= s.height) return -1 ;
      return 0 ;
   }


   // Function to add the component to our panel frame.  This function must
   // run on the AWT event thread.  MenuItem components are not added to
   // the panel frame.

   private void addComponent()
   {
      if (!input) return ;
      if (panel == null) return ;
      if (component == null) return ;
      if (component instanceof JMenuItem) return ;
      if (component instanceof JCheckBoxMenuItem) return ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { addComponent1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         addComponent1() ;
   }

      // Add our component to the panel frame if it is not present.

   private void addComponent1()
   {
      boolean member = false ;
      Component [] components = panel.getComponents() ;
      if (components != null)
      {
         for (int i = 0 ; i < components.length ; i++)
         {
            if (components[i].equals(component)) { member = true ; break ; }
            if (components[i].equals(showcomponent)) { member = true ; break ; }
         }
      }

      // We initially position the component at the group location as all cel
      // offsets are relative to the group.

      Object o = getGroup() ;
      if (!member && (o instanceof Group))
      {
         Component c = component ;
         if (showcomponent != null) c = showcomponent ;
         c.setVisible(false) ;
         Point offset = getOffset() ;
         Point location = ((Group) o).getLocation() ;
         setLocation(location) ;
         location.x += offset.x ;
         location.y += offset.y ;
         panel.add(c) ;
         Point disp = panel.getOffset() ;
         c.setLocation(location.x+disp.x,location.y+disp.y) ;
         c.setVisible(true) ;
      }
   }


   // Function to remove the component from the panel frame.  This function
   // must run on the AWT event thread.

   private void removeComponent()
   {
      if (panel == null) return ;
      if (component == null) return ;
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { removeComponent1() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      else
         removeComponent1() ;
   }
      
      // Remove the component.
      
   private void removeComponent1()
   {
      Component c = component ;
      if (showcomponent != null) c = showcomponent ;
      panel.remove(c) ;
   }


	// Object graphics methods
	// -----------------------

	// Draw the cel at its current position, constrained by the
	// defined bounding box.  We draw the cel only if is is visible
	// and intersects our drawing area.  MenuItem components are not
   // drawn.

	void draw(final Graphics g, final Rectangle box)
	{
		if (!visible) return ;
      if (panel == null) return ;
      if (component instanceof JMenuItem) return ;
      if (component instanceof JCheckBoxMenuItem) return ;
		Rectangle celBox = getBoundingBox() ;
		Rectangle r = (box == null) ? celBox : box.intersection(celBox) ;
		if (r.width < 0 || r.height < 0) return ;
      float scale = (scaled) ? sf : 1.0f ;
      component.setSize(getSize()) ;

		// The cel intersects our drawing box.  Position to screen coordinates
      // if the cel has been scaled.

      int x = (int) (r.x * scale) ;
      int y = (int) (r.y * scale) ;
      int w = (int) Math.ceil(r.width * scale) ;
      int h = (int) Math.ceil(r.height * scale) ;
      int cx = (int) (celBox.x * scale) ;
      int cy = (int) (celBox.y * scale) ;

      // Draw the component.  Input components are added to the panel
      // to capture input events.  Non-input components are drawn as
      // standard cels.  We have observed hang conditions invoking the
      // component.paint() method while not on the AWT thread.

      if (input)
      {
         Point offset = panel.getOffset() ;
         if (showcomponent != null) 
            showcomponent.setLocation(offset.x+cx,offset.y+cy) ; 
         else
            component.setLocation(offset.x+cx,offset.y+cy) ;
         return ;
      }

      if (g == null) return ;
      if (image == null) createImage() ;
  		Graphics gc = g.create(x,y,w,h) ;
  		gc.translate(-x,-y) ;
      Image img = getImage() ;
      
      // Set the transparency.

      if (transparency < 255) 
      {
         float t = transparency / 255.0f ;
         if (t > 1) t = 1 ; else if (t < 0) t = 0 ;
         AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,t) ;
         if (gc instanceof Graphics2D) ((Graphics2D) gc).setComposite(ac) ;
      }
  		if (img != null) gc.drawImage(img,cx,cy,null) ;
      
      // Scrolling components do not paint. In this case we identify the
      // item with its name.

      if ((component instanceof JScrollPane) ||
          (component instanceof JComboBox))
      {
         String s = getName() ;
         int i = s.lastIndexOf('.') ;
         if (i > 0) s = s.substring(0,i) ;
         FontMetrics metrics = gc.getFontMetrics();
         int width = metrics.stringWidth(s);
         int height = metrics.getHeight();
         if (!(width > w || height > h))
         {
            gc.setColor(Color.black) ;
// disable drawing name as it sometimes always shows when selected
            gc.drawString(s,cx+(w/2)-(width/2),cy+(h/2)+(height/2));
         }
      }
  		gc.dispose() ;
	}


	// Method to change the cel transparency.  The change is relative
	// to the current transparency.  We create a new color model for
	// palette cels.  The relative change uses the KiSS model of transparency.
   // Positive values make the object more transparent.  Negative values make
   // the object more opaque.

	void changeTransparency(int t, boolean bounded, boolean ambiguous)
	{
		if (error) return ;

      // Limit transparency between 0 and 255 if this is a bounded change.

      int adjust = t ;
      if (bounded)
      {
         if (transparency < 0) transparency = 0 ;
         if (transparency > 255) transparency = 255 ;
   		int n1 = 255 - transparency ;
         int n2 = n1 + t ;
         if (n2 < 0) n2 = 0 ;
         if (n2 > 255) n2 = 255 ;
         adjust = n2 - n1 ;
      }

		// Update the cel transparency value.

		int kisstransparency = 255 - transparency ;
      kisstransparency += adjust ;
		setTransparency(255 - kisstransparency,(OptionsDialog.getAllAmbiguous() && ambiguous),this) ;
		if (image == null) return ;

		// Identify the image to filter.
/*
		filteredimage = null ;
      Image img = getScaledImage() ;
      if (img == null) img = getBaseImage() ;
      if (img == null) return ;

		// Palette cels require a new color model.  Truecolor cels use the
      // same color model with a new transparency.

		if (!truecolor)
		{
			Palette p = getPalette() ;
			if (p == null) return ;
			cm = p.createColorModel(transparency,multipalette) ;
      }

		// Construct an image filter.
      
		PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor) ;
		ImageProducer ip = new FilteredImageSource(img.getSource(),pf) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
      MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
      tracker.addImage(filteredimage,0) ;
      try { tracker.waitForAll(500) ; }
      catch (InterruptedException e) { }
/*
		// This is approximately twice as fast.

      int w = img.getWidth(null) ;
      int h = img.getHeight(null) ;
      BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC,transparency/255.0f) ;
      Graphics2D gc = (Graphics2D) bi.getGraphics() ;
      gc.setComposite(ac) ;
      gc.drawImage(img,0,0,null) ;
      filteredimage = bi ;
*/
	}


   // Function to scale the component.

   void scaleImage(float scale)
   	throws KissException
   {
      if (component instanceof JMenuItem) return ;
      if (component instanceof JCheckBoxMenuItem) return ;
      if ("menuseparator".equals(type)) return ;
      if (!input) return ;

      // Input component scaling.

      scaled = (scale != 1.0f) ;
      if (scale == 0.0f || !scaled)
      {
			scaled = false ;
			sf = 1.0f ;
      }

      // If we are not scaled, restore the component size.

      if (component == null) return ;
      if (!scaled) component.setSize(size) ;

      // Determine the new scaled size.

      else
      {
         int w = size.width ;
         int h = size.height ;
         int sw = (int) Math.ceil(w * scale) ;
         int sh = (int) Math.ceil(h * scale) ;

         // Scale the component.  Watch for errors.

         try
         {
            component.setSize(new Dimension(sw,sh)) ;
   	      sf = scale ;
   	      sw = (int) (sw / sf) ;
   	      sh = (int) (sh / sf) ;
   	      scaledsize = new Dimension(sw,sh) ;
         }
         catch (OutOfMemoryError e)
         {
         	throw new KissException("Scale component, out of memory, cel " + file) ;
         }
      }
   }



   // Function to scale the base image to a specific width and height.  

   void scaleImage(Dimension d)
      throws KissException
   {
      if (image == null) createImage() ;
      super.scaleImage(d) ;
   }
   
   

	// Duplicate clone.  This creates a brand new component object where 
   // attributes are the same as found in the original object. New components
   // are created as they must remain distinct from the original source.

   public Object clone()
   {
      if (ref == null) return null ;
      Vector v = ref.getComponents() ;
      int n = getNextComponentNumber(type,ref.getID()) ;
      String s = type + n ;
      String name = this.getName() ;
      n = (name != null) ? name.lastIndexOf('.') : -1 ;
      String ext = (n > 0) ? name.substring(n+1) : type ;
      JavaCel cel = new JavaCel(type,(s + "." + ext),ref) ;
      cel.setPanel(panel) ;
      cel.setID(this.getID()) ;
      cel.setText(this.getText()) ;
      cel.setSize(this.getSize()) ;
      cel.setGroup(this.getGroup()) ;
      cel.setOffset(this.getOffset()) ;
      cel.setInitialOffset(this.getInitialOffset()) ;
      cel.setAdjustedOffset(this.getAdjustedOffsetPoint()) ;
      cel.setLocation(this.getLocation()) ;
      cel.setAttributes(this.getAttributes()) ;
      cel.setImported(this.isImported()) ;
      cel.setUpdated(true) ;
      v.addElement(cel) ;
      return cel ;
   }
   
   
   // Flush the JavaCel to get rid of all objects.
   
   void flush()
   {
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { flush() ; } } ;
         try { javax.swing.SwingUtilities.invokeAndWait(runner) ; }
         catch (InterruptedException e) { }
         catch (Exception e) { e.printStackTrace(); }
      }
      removeComponent() ;
      if (list != null) list.removeListSelectionListener(listListener);
      if (combobox != null) combobox.removeItemListener(itemListener);

      component = null ;
      showcomponent = null ;   
      text = null ;         // Text component
      pane = null ;         // HTML text component
      list = null ;         // List component
      combobox = null ;     // ComboBox component
      scroll = null ;       // Scroll pane
      borderpanel = null ;  // Border panel
      insets = null ;       // Border insets
      ref = null ;          // Load reference
      panel = null ;
      flushed = true ;
   }
   
   
   // An inner class to draw a border around the component.
   
   class BorderPanel extends JPanel
   {
      public BorderPanel() { setOpaque(false) ; }
      
      public void paint(Graphics g)
      {
         Insets insets = getInsets() ;
         int w = getWidth() - insets.right ;
         int h = getHeight() - insets.top ;
         Graphics g2 = g.create(insets.right,insets.top,w,h) ; 
         if (!input)
            component.paint(g) ;
         else
            component.paint(g2) ;
         g2.dispose() ;
         
         // Titled borders require a grahics clip area. They don't really
         // work very well as our borders actually overlay the component.
         
         g2 = g.create(0,0,getWidth(),getHeight()) ; 
         super.paintBorder(g2) ;
         g2.dispose() ;
      }
   }
}
