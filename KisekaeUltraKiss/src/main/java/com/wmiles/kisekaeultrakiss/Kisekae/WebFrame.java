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
* WebFrame class
*
* Purpose:
*
* This class defines a simple web browser for searching and downloading sets
* from a host server.  We use a JEditorPane to capture hyperlink events.
*
*/

import java.io.* ;
import java.net.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.text.* ;
import javax.jnlp.* ;
import java.beans.PropertyChangeListener ;
import java.beans.PropertyChangeEvent ;


final public class WebFrame extends KissFrame
	implements WindowListener, ActionListener, HyperlinkListener, PropertyChangeListener
{
	// Dialog attributes

   private static Vector history = new Vector() ;  // URL History list
   private static int currentlocation = 0 ;        // Current history index
   private static String currentweb = null ;       // Last valid web
   private static Hashtable urlmap = new Hashtable(20) ; // Redirect URL to page set

   private WebFrame me = null ;						   // Reference to ourselves
   private MainFrame parent = null ;               // Reference to mainframe
   private UrlLoader urlloader = null ;            // Reference to loader
   private String location = null ;                // Requested location
   private Thread runner = null ;                  // Our connection thread
   private Vector localhistory = null ;            // Local URL History list
   private URL loadarchive = null ;                // Wait notify dialog
   private NotifyDialog nd = null ;                // Wait notify dialog
   private Hashtable cancel = new Hashtable() ;    // Cancel load 
   private int locallocation = 0 ;                 // Local history index
   private boolean connecting = false ;            // If true, connecting
   private boolean nocopy = false ;                // If true, no save of file
   private long setpagetime = 0 ;                  // Time when setPage() called
   private long hyperlinktime = 0 ;                // Time when hyperlink pressed
   private long pageloadtime = 0 ;                 // Time to load page 

	// HelpSet interface objects.

   private static String helpset = "Help/Product.hs" ;
   private static String helpsection = "kisekae.index" ;
	private static String onlinehelp = "kissbrowser/index.html" ;
	private static String portalset = "Help/KissBrowser.hs" ;
	private static String portalsection = "kissbrowser.index" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;
	private HelpLoader helper2 = null ;
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

	// User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
   private JPanel panel3 = new JPanel();
	private JToolBar toolbar = null ;
	private JTextField enterurl = new JTextField();
	private WebEditor editor = new WebEditor();
   private EditorKit editorkit = null ;
	private JButton backbtn = new JButton() ;
	private JButton forwardbtn = new JButton() ;
	private JButton searchbtn = new JButton() ;
	private JButton remotebtn = new JButton() ;
	private JButton activebtn = new JButton() ;
   private JScrollPane scrollpane = new JScrollPane() ;
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();
   private StatusBar statusbar = null ;

	// Menu items

	private JMenu fileMenu ;
   private JMenu optionMenu ;
   private JMenu windowMenu ;
	private JMenu helpMenu ;
	private JMenuItem exit ;
	private JMenuItem help ;
   private JMenuItem portalhelp ;
	private JMenuItem logfile ;
	private JMenuItem about ;
   private JMenuItem options ;
   private JMenuItem open ;
   private JMenuItem search ;
   private JMenuItem remote ;
	private JMenuItem back ;
	private JMenuItem forward ;
	private JMenuItem clearhistory ;
	private JMenuItem refresh ;
   private JMenuItem showsource ;
   private JMenuItem register ;
	private Insets insets = null ;


	// Constructor

   public WebFrame(MainFrame main)
	{
      me = this ;
      parent = main ;
      if (history.size() == 0)
         history.addElement(currentweb) ;
      setHistoryLocation(currentlocation) ;
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;
      editor.setEditorKit(new WebHTMLEditor());
      editor.addPropertyChangeListener("page", this);
      runner = new WebConnect() ;
      init() ;
   }
   
   // Constructor for a specified home page.  This constructor
   // does not establish a current web.  A local history is used
   // for this WebFrame object.  This history does not persist if the 
   // frame is closed.  
   
   public WebFrame(MainFrame main, String homepage)
	{
      me = this ;
      parent = main ;
      if (homepage == null) homepage = OptionsDialog.getKissWeb() ;
      localhistory = new Vector() ;
      localhistory.addElement(homepage) ;
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;
      editor.setEditorKit(new WebHTMLEditor());
      editor.addPropertyChangeListener("page", this);
      runner = new WebConnect() ;
      init() ;
   }
   
   // Constructor for a specified home page with a current web.
   // If website links go outside our current web site we show it 
   // in a browser window.
   
   public WebFrame(MainFrame main, String homepage, String web)
	{
      me = this ;
      parent = main ;
      setCurrentWeb(web) ;
      if (homepage == null) homepage = OptionsDialog.getKissWeb() ;
      localhistory = new Vector() ;
      localhistory.addElement(homepage) ;
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;
      editor.setEditorKit(new WebHTMLEditor());
      runner = new WebConnect() ;
      init() ;
   }


   // Frame initialization.

	private void init()
	{
		URL iconfile = null ;
		setIconImage(Kisekae.getIconImage()) ;
      boolean applemac = OptionsDialog.getAppleMac() ;
      String ext = ".gif" ;
      if (OptionsDialog.getAppleMac()) ext = ".png" ;

		// Back button

		backbtn = new JButton() ;
		iconfile = Kisekae.getResource("Images/back" + ext) ;
		if (iconfile != null) backbtn.setIcon(new ImageIcon(iconfile)) ;
		backbtn.setMargin(new Insets(1,1,1,1)) ;
      backbtn.setText(Kisekae.getCaptions().getString("BackMessage"));
		backbtn.setToolTipText(Kisekae.getCaptions().getString("ToolTipBack")) ;
		backbtn.setAlignmentY(0.5f) ;
		backbtn.addActionListener(this) ;
      backbtn.setEnabled(false);

		// Forward button

		forwardbtn = new JButton() ;
		iconfile = Kisekae.getResource("Images/forward" + ext) ;
		if (iconfile != null) forwardbtn.setIcon(new ImageIcon(iconfile)) ;
		forwardbtn.setMargin(new Insets(1,1,1,1)) ;
      forwardbtn.setText(Kisekae.getCaptions().getString("ForwardMessage"));
		forwardbtn.setToolTipText(Kisekae.getCaptions().getString("ToolTipForward")) ;
		forwardbtn.setAlignmentY(0.5f) ;
		forwardbtn.addActionListener(this) ;
      forwardbtn.setEnabled(false);

		// Search button

		searchbtn = new JButton() ;
		iconfile = Kisekae.getResource("Images/folder" + ext) ;
		if (iconfile != null) searchbtn.setIcon(new ImageIcon(iconfile)) ;
		searchbtn.setMargin(new Insets(1,1,1,1)) ;
      searchbtn.setText(Kisekae.getCaptions().getString("SearchMessage"));
		searchbtn.setToolTipText(Kisekae.getCaptions().getString("ToolTipSearch")) ;
		searchbtn.setAlignmentY(0.5f) ;
		searchbtn.addActionListener(this) ;
      searchbtn.setEnabled(false);

		// Remote button

		remotebtn = new JButton() ;
		iconfile = Kisekae.getResource("Images/folder" + ext) ;
		if (iconfile != null) remotebtn.setIcon(new ImageIcon(iconfile)) ;
		remotebtn.setMargin(new Insets(1,1,1,1)) ;
      remotebtn.setText(Kisekae.getCaptions().getString("RemoteMessage"));
		remotebtn.setToolTipText(Kisekae.getCaptions().getString("ToolTipRemote")) ;
		remotebtn.setAlignmentY(0.5f) ;
		remotebtn.addActionListener(this) ;
      remotebtn.setEnabled(false);

		// Active button

		activebtn = new JButton() ;
		iconfile = Kisekae.getResource("Images/web1" + ext) ;
		if (iconfile != null) activebtn.setIcon(new ImageIcon(iconfile)) ;
		activebtn.setMargin(new Insets(1,1,1,1)) ;
		activebtn.setBorder(BorderFactory.createEmptyBorder(0,5,0,5)) ;
		activebtn.setToolTipText(Kisekae.getCaptions().getString("ConnectingMessage")) ;
      activebtn.setRequestFocusEnabled(false) ;
		activebtn.setAlignmentY(0.5f) ;
		iconfile = Kisekae.getResource("Images/web1" + ext) ;
		if (iconfile != null) activebtn.setIcon(new ImageIcon(iconfile)) ;
		iconfile = Kisekae.getResource("Images/web2" + ext) ;
		if (iconfile != null) activebtn.setDisabledIcon(new ImageIcon(iconfile)) ;
      activebtn.setEnabled(false) ;

		// Construct the user interface.

		try { jbInit() ; pack() ; }
		catch(Exception ex)
		{ 
         PrintLn.println("WebFrame: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace() ; 
      }

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      {
      	helper = new HelpLoader(this,helpset,helpsection) ;
      	helper2 = new HelpLoader(this,portalset,portalsection) ;
      }

		// Set up the menu bar.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
		String jv = System.getProperty("java.version") ;
		int rm = (jv.indexOf("1.2") == 0) ? 2 : 26 ;
		insets = new Insets(2,2,2,rm) ;
		fileMenu.setMargin(insets) ;
		if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
      fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
      if (!applemac) open.setMnemonic(KeyEvent.VK_O) ;
      open.addActionListener(this) ;
      fileMenu.add((search = new JMenuItem(Kisekae.getCaptions().getString("MenuFileIndex")))) ;
      if (!applemac) search.setMnemonic(KeyEvent.VK_L) ;
      search.setEnabled(false) ;
      search.addActionListener(this) ;
      fileMenu.add((remote = new JMenuItem(Kisekae.getCaptions().getString("MenuFileRemote")))) ;
      if (!applemac) remote.setMnemonic(KeyEvent.VK_R) ;
      remote.setEnabled(false) ;
      remote.addActionListener(this) ;
      fileMenu.addSeparator();
		fileMenu.add((back = new JMenuItem(Kisekae.getCaptions().getString("MenuFileBack")))) ;
		if (!applemac) back.setMnemonic(KeyEvent.VK_B) ;
      back.setEnabled(false) ;
		back.addActionListener(this) ;
		fileMenu.add((forward = new JMenuItem(Kisekae.getCaptions().getString("MenuFileForward")))) ;
		if (!applemac) forward.setMnemonic(KeyEvent.VK_F) ;
      forward.setEnabled(false);
		forward.addActionListener(this) ;
		fileMenu.add((refresh = new JMenuItem(Kisekae.getCaptions().getString("MenuFileRefresh")))) ;
		if (!applemac) refresh.setMnemonic(KeyEvent.VK_R) ;
		refresh.addActionListener(this) ;
      fileMenu.addSeparator();
		fileMenu.add((showsource = new JMenuItem(Kisekae.getCaptions().getString("MenuFileViewSource")))) ;
		if (!applemac) showsource.setMnemonic(KeyEvent.VK_V) ;
		showsource.addActionListener(this) ;
		fileMenu.add((clearhistory = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClearHistory")))) ;
      clearhistory.setEnabled(false) ;
		clearhistory.addActionListener(this) ;
      fileMenu.addSeparator();
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuitPortal" : "MenuFileExitPortal" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
		if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;

      optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
      if (!applemac) optionMenu.setMnemonic(KeyEvent.VK_O) ;
      optionMenu.setMargin(insets) ;
      optionMenu.add((options = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsOptions")))) ;
      if (!applemac) options.setMnemonic(KeyEvent.VK_C) ;
      options.addActionListener(this) ;
      mb.add(optionMenu) ;
      
		windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		windowMenu.setMargin(insets) ;
      if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
		mb.add(windowMenu) ;

		// Create the Help menu and About dialog.

      aboutdialog = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
		helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
		helpMenu.setMargin(insets) ;
		if (!applemac) helpMenu.setMnemonic(KeyEvent.VK_H) ;
		mb.add(helpMenu);
      helpMenu.add((help = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpContents")))) ;
		if (!applemac) help.setMnemonic(KeyEvent.VK_C) ;
      help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0)) ;
		help.setEnabled(helper != null && helper.isLoaded()) ;
      if (helper != null) helper.addActionListener(help) ;
      portalhelp = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpContents")) ;
//      helpMenu.add(portalhelp) ;
		portalhelp.setEnabled(helper2 != null && helper2.isLoaded()) ;
      if (helper2 != null) helper2.addActionListener(portalhelp) ;
      if (!Kisekae.isHelpInstalled()) help.addActionListener(this) ;
      if (!Kisekae.isHelpInstalled()) help.setEnabled(true) ;
      MainFrame mf = Kisekae.getMainFrame() ;
      MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
      if (menu != null)
      {
         helpMenu.add((logfile = new JMenuItem(Kisekae.getCaptions().getString("MenuViewLogFile")))) ;
         if (!applemac) logfile.setEnabled(LogFile.isOpen()) ;
         logfile.addActionListener(menu) ;
         logfile.setMnemonic(KeyEvent.VK_L) ;
         logfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, accelerator+ActionEvent.SHIFT_MASK));
      }
		helpMenu.addSeparator() ;
//    helpMenu.add((register = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpRegister")))) ;
//    if (!applemac) register.setMnemonic(KeyEvent.VK_R) ;
//    register.addActionListener(this);
//    register.setEnabled(!Kisekae.isSecure());
//    helpMenu.addSeparator() ;
      helpMenu.add((about = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpAbout")))) ;
		if (!applemac) about.setMnemonic(KeyEvent.VK_A) ;
		about.addActionListener(this);
		setJMenuBar(mb) ;

		// Ensure the back and forward buttons are the same size.

      Dimension d1 = backbtn.getPreferredSize() ;
      Dimension d2 = forwardbtn.getPreferredSize() ;
      Dimension d3 = searchbtn.getPreferredSize() ;
      Dimension d4 = remotebtn.getPreferredSize() ;
      int w = Math.max(d1.width,d2.width) ;
      w = Math.max(w,d3.width) ;
      w = Math.max(w,d4.width) ;
      d1.width = Math.max(w,80) ;
      backbtn.setPreferredSize(d1) ;
      forwardbtn.setPreferredSize(d1) ;
      searchbtn.setPreferredSize(d1) ;
      remotebtn.setPreferredSize(d1) ;

		// Size the frame for the window space.

      super.open() ;
		doLayout() ;
		validate() ;
      setValues() ;
      if (helper != null) helper.setSize(getSize());
      if (helper2 != null) helper2.setSize(getSize());

      // Set listeners.  If we have a current web set the editor hosted site.

      String s = null ;
      if (currentweb != null)
      {
         s = currentweb.replace('\\','/') ;
         int n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
      }
      
      editor.setHosted(s) ;
      editor.setEditable(false);
      editor.addHyperlinkListener(this) ;
      enterurl.addActionListener(this) ;
      Vector v = getHistory() ;
//      setPage((String) v.lastElement()) ;
      int i = getHistoryLocation() ;
      if (i < 0 || i >= v.size()) i = v.size() - 1 ;
      s = (currentweb == null) ? OptionsDialog.getKissWeb() : s ;
      String s1 = (i >= 0) ? (String) v.elementAt(i) : s ;
      setPage(s1) ;
		addWindowListener(this) ;
      Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
      gridLayout1.setColumns(1);
      gridLayout1.setRows(2);
		panel1 = new JPanel() ;
		panel1.setLayout(borderLayout1) ;
      enterurl.setText(OptionsDialog.getKissWeb()) ;
		panel2 = new JPanel() ;
		panel2.setLayout(gridLayout1) ;
      panel3 = new JPanel() ;
		panel3.setLayout(borderLayout2) ;
		toolbar = new JToolBar() ;
		toolbar.setFloatable(false) ;
  		toolbar.add(backbtn) ;
      toolbar.addSeparator() ;
 		toolbar.add(forwardbtn) ;
      toolbar.addSeparator() ;
// 	  toolbar.add(searchbtn) ;
//      toolbar.addSeparator() ;
// 	  toolbar.add(remotebtn) ;
		toolbar.add(Box.createGlue()) ;
		toolbar.add(activebtn) ;
		panel3.add(toolbar,BorderLayout.CENTER) ;
      panel2.add(panel3,null) ;
      panel2.add(enterurl,null) ;
      panel1.add(panel2,BorderLayout.NORTH) ;
      editorkit = editor.getEditorKit() ;
      scrollpane = new JScrollPane(editor) ;
      int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
      int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
      scrollpane.setVerticalScrollBarPolicy(vspolicy) ;
      scrollpane.setHorizontalScrollBarPolicy(hspolicy) ;			
      panel1.add(scrollpane,BorderLayout.CENTER) ;
      panel1.add(statusbar,BorderLayout.SOUTH) ;
		getContentPane().add(panel1) ;
	}


	// Method to set the dialog field values.

	private void setValues()
	{
		String title = Kisekae.getCaptions().getString("WebBrowserTitle") ;
		setTitle(title) ;
      try
      {
         String formname = OptionsDialog.getKissIndex() ;
         File f1 = new File(formname) ;
         search.setEnabled(f1.exists()) ;
         remote.setEnabled(OptionsDialog.getKissWeb() != null) ;
         searchbtn.setEnabled(search.isEnabled()) ;
         remotebtn.setEnabled(remote.isEnabled()) ;
      }
      catch (Exception e) { }
	}

	// Method to set the current website.  The portal, if on the current
   // website, processes the HTML.  Otherwise the link is passed to the
   // default browser.

	public static void setCurrentWeb(String s) 
   { 
      if (currentweb == null || history == null)
      {
         history = new Vector() ;
         currentweb = OptionsDialog.getKissWeb() ;
         history.addElement(currentweb) ;
      }
      currentweb = s ; 
      if (currentweb != null && history != null) 
         if (history.size() == 0 || !history.lastElement().equals(currentweb))
            history.addElement(currentweb) ;
   }

	// Method to set a redirect to map a URL to a set of FKiSS events.  
   // The portal, if it sees the URL, will fire the events.  This will
   // typically be a set() event to change to a new page set.

	public static void redirect(String url, Vector events) 
   {
      if (url == null || events == null) return ;
      urlmap.put(url.toLowerCase(),events) ; 
   }
   
   // Method to clear the static redirect table.  This is required 
   // on a new set load.  All history elements that refer to redirects
   // are removed.
   
   public static void clearRedirect() 
   { 
      if (urlmap.isEmpty()) return ;
      Vector newhistory = new Vector() ;
      for (int i = 0 ; i < history.size() ; i++)
      {
         try
         {
            String s = (String) history.elementAt(i) ;
            URL evturl = new URL(s) ;
            String s1 = evturl.toExternalForm() ;
            s1 = s1.toLowerCase() ;
            Enumeration e = urlmap.keys() ;
            while (e.hasMoreElements())
            {
               Object o = e.nextElement() ;
               String s2 = o.toString() ;
               if (s2.startsWith(s1))
               {
                  s1 = null ;
                  break ;
               }
            }
             if (s1 != null) newhistory.add(s) ;
         }
         catch (MalformedURLException e) { }
      }
      history.clear() ;
      history.addAll(newhistory) ;
      currentlocation = history.size()-1 ;
      urlmap.clear(); 
   }


   // Set the JEditorPane page to view.   The connection is performed in a
   // separate thread.  Also look for a redirection to an event, typically 
   // a label() event.
 
   public void setPage(String location)
   { setPage(location,null) ; }
   
   public void setPage(String location, JavaCel cel)
   {        
      try
      {
         if (location == null) return ;
         URL evturl = new URL(location) ;
         String s = evturl.toExternalForm() ;
         Object o = urlmap.get(s.toLowerCase()) ;
         
         // Redirect exist?
         
         if (o instanceof Vector)
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
            if (pf != null)
            {
               if (OptionsDialog.getDebugControl())
                  PrintLn.println("WebFrame: redirect " + s + " to event \"" + ((Vector) o).elementAt(0) + "\"") ;
               setVisible(false) ;
               EventHandler.fireEvents((Vector) o,pf,Thread.currentThread(),s) ;
               fireWindowClose() ;
            }
            return ;
         }
      }
      catch (MalformedURLException e) { }
      this.location = location ;

      // Load page.
   
      if (cel != null)
      {
         cel.setPage(location) ;
         return ;
      }
      
      if (runner == null || runner.isAlive()) return ;
      Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      activebtn.setEnabled(true) ;
      enterurl.setText(location) ;
      runner = new WebConnect() ;
      runner.start() ;
//    runner.run() ;
   }


   // Set a nocopy indicator that was referenced on the URL for download.
   // Pass this to the mainmenu when we load the URL file to disable menu 
   // Save and Save As functions.

   public void setNoCopy(boolean b) { nocopy = b ; }


   // Menu items for browser forward and back control.  If we are at the 
   // beginning of the history list and cannot go back any further, simply
   // send the portal window to the back.  

   void back()
   {
      boolean b = (location.contains("#currentweb")) ;
      
      Vector v = getHistory() ;
      int i = getHistoryLocation() - 1 ;
      if (i < 0 || i >= v.size()) 
      {
         close() ;
         return ;
      }
      location = (String) v.elementAt(i) ;
      setHistoryLocation(i) ;        

      // If our current web is on a remote host and we link back to our
      // local file system, then we adjust our current web site to point
      // to the local file.

      String s = currentweb ;
      if (s == null) s = "" ;
      int n = s.lastIndexOf('/') ;
      if (n > 0) s = s.substring(0,n) ;
      if (b || ((s.startsWith("http:") || s.startsWith("https:")) && 
          (location.startsWith("file:") || location.startsWith("jar:"))))
      {
         currentweb = location ;
         s = currentweb ;
         n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         editor.setHosted(s);
      }
      Document doc = editor.getDocument();
      doc.putProperty(Document.StreamDescriptionProperty, null);
      setPage(location) ;
   }

   void forward()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() + 1 ;
      if (i < 0 || i >= v.size()) return ;
      location = (String) v.elementAt(i) ;
      setHistoryLocation(i) ;
      
      boolean b = (location.contains("#currentweb")) ;

      // If our current web is on a local file system and we link forward to
      // a remote host, then we adjust our current web site to point to the
      // to the remote host.

      String s = currentweb ;
      if (s == null) s = "" ;
      int n = s.lastIndexOf('/') ;
      if (n > 0) s = s.substring(0,n) ;
      if (b || ((location.startsWith("http:") || location.startsWith("https:")) && 
          (s.startsWith("file:") || s.startsWith("jar:"))))
      {
         currentweb = location ;
         s = currentweb ;
         n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         editor.setHosted(s);
      }
      Document doc = editor.getDocument();
      doc.putProperty(Document.StreamDescriptionProperty, null);
      setPage(location) ;
   }

   
   // A method to show the file chooser dialog to select a page.
   
   private void showFileOpen()
   {
      FileOpen fo = new FileOpen(this) ;
      fo.show("Open Portal Index") ;
      String s = fo.getPath() ;
      if (s == null) return ;
      if (!s.startsWith(File.separator)) s = File.separator + s ;
      s = "file://" + s.replace('\\','/') ;
      enterurl.setText(s) ;
 
      // Signal a textfield input event.
      
      ActionEvent ae = new ActionEvent(enterurl,ActionEvent.ACTION_PERFORMED,s) ;
      actionPerformed(ae) ;
   }

   
   // A method to show the search index result.
   
   private void showSearchIndex()
   {
      try 
      { 
         String formname = OptionsDialog.getKissIndex() ;
         File f1 = new File(formname) ;
         if (!f1.exists()) return ;
         URL formurl = f1.toURL() ; 
         String s = formurl.toExternalForm() ;
         enterurl.setText(s) ;
 
         // Signal a textfield input event.
      
         ActionEvent ae = new ActionEvent(enterurl,ActionEvent.ACTION_PERFORMED,s) ;
         actionPerformed(ae) ;
      }
      catch (Exception e) { } ;
   }

   
   // A method to show the remote index result.
   
   private void showRemoteIndex()
   {
      try 
      { 
         String s = OptionsDialog.getKissWeb() ;
         enterurl.setText(s) ;
 
         // Signal a textfield input event.
      
         ActionEvent ae = new ActionEvent(enterurl,ActionEvent.ACTION_PERFORMED,s) ;
         actionPerformed(ae) ;
         
         // Reset the history.
         
         setCurrentWeb(s) ;
      }
      catch (Exception e) { } ;
   }

   
   // A reload of the page must clear the document stream description.
   
   void refresh()
   {
      statusbar.showStatus("") ;
      Vector v = getHistory() ;
      int i = getHistoryLocation() ;
      if (i < 0 || i >= v.size()) return ;
      location = (String) v.elementAt(i) ;
      setHistoryLocation(i) ;
      Document doc = editor.getDocument();
      doc.putProperty(Document.StreamDescriptionProperty, null);
      setPage(location) ;
   }

   
   // Utility function to clear our history trace.

   static void reset()
   {
      history = new Vector() ;
      currentlocation = 0 ;
   }


   // A utility method to update any associated menu or button states.

   private void update()
   {
      if (!javax.swing.SwingUtilities.isEventDispatchThread())
      {
      	Runnable awt = new Runnable()
			{ public void run() { update() ; } } ;
   		javax.swing.SwingUtilities.invokeLater(awt) ;
      }
      
      activebtn.setEnabled(false) ;
      back.setEnabled(getBack() != null) ;
      backbtn.setEnabled(back.isEnabled()) ;
      forward.setEnabled(getForward() != null) ;
      forwardbtn.setEnabled(forward.isEnabled()) ;
      clearhistory.setEnabled(back.isEnabled() || forward.isEnabled()) ;
      String formname = OptionsDialog.getKissIndex() ;
      try
      {
         File f1 = new File(formname) ;
         search.setEnabled(f1.exists()) ; 
         remote.setEnabled(OptionsDialog.getKissWeb() != null) ; 
         searchbtn.setEnabled(search.isEnabled()) ; 
         remotebtn.setEnabled(remote.isEnabled()) ;
      }
      catch (Exception e) { }
   }

   // Utility functions to determine if back or forward entries exist.
   // A back entry always exists.  At the end it will hide the Portal
   // and show the main frame.

   String getBack()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() - 1 ;
      if (i < 0 || i >= v.size()) return new String("Hide") ;
      return (String) v.elementAt(i) ;
   }

   String getForward()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() + 1 ;
      if (i < 0 || i >= v.size()) return null ;
      return (String) v.elementAt(i) ;
   }


   // Get our current web base.
   
   static String getCurrentWeb() { return currentweb ; }
   
   // Translate between permanent and local history
   
   int getHistoryLocation()
   {
      if (localhistory != null) return locallocation ;
      return currentlocation ;
   }
   
   void setHistoryLocation(int n)
   {
      if (localhistory != null) locallocation = n ;
      currentlocation = n ;
   }
   
   Vector getHistory()
   {
      if (localhistory != null) return localhistory ;
      return history ;
   }
   
   // Clear the local history.
   
   void clearLocalHistory(String homepage)
   {
      if (localhistory != null) localhistory = new Vector() ;
      if (homepage == null) 
         homepage = OptionsDialog.getKissWeb() ;
      if (localhistory != null) 
         localhistory.addElement(homepage) ;
      setHistoryLocation(0) ;
   }
   
   // Set our hosted website.
   
   void setHosted(String s) { editor.setHosted(s); }
   
   // Get the archive load cancel indicator.
   
   boolean getCancel(String url) 
   {  
      Object o = cancel.get(url) ;
      if (!(o instanceof Boolean)) return false ;
      return ((Boolean) o).booleanValue() ; 
   }
   

	// Action event listener.  We only process menu item actions.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;
		try
		{
			// Exit request.

			if (source == exit)
			{
				close() ;
				return ;
			}

			// Back and Forward and Refresh requests.

			if (source == back || source == backbtn) back() ;
			if (source == forward || source == forwardbtn) forward() ;
			if (source == refresh) refresh() ;

         // Open request.

         if (source == open)
         {
             showFileOpen() ;
             return ;
         }

         // Search index request.

         if (source == search || source == searchbtn)
         {
             showSearchIndex() ;
             return ;
         }

         // Remote index request.

         if (source == remote || source == remotebtn)
         {
             showRemoteIndex() ;
             return ;
         }

         // Show the HTML source.

         if (source == showsource)
         {
            String s = editor.getText() ;
            JTextArea text = new JTextArea(s) ;
            JScrollPane scroll = new JScrollPane(text) ;
            JDialog d = new JDialog(me,location) ;
            d.getContentPane().add(scroll) ;
            d.setSize(getSize());
            d.show();
         }

         // Clear the history source.

         if (source == clearhistory)
         {
            reset() ;
            update() ;
            Vector v = getHistory() ;
            v.addElement(location) ;
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("ClearHistoryText"),
               Kisekae.getCaptions().getString("WebBrowserTitle"),
               JOptionPane.INFORMATION_MESSAGE) ;
         }

   		// A Help Contents request occurs only if the installed Help system is
         // not available.  In this case we attempt to reference online help
         // through Kisekae World.

   		if (help == source)
   		{
            BrowserControl browser = new BrowserControl() ;
            String helpurl = OptionsDialog.getWebSite() + OptionsDialog.getOnlineHelp() ;
            Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            browser.displayURL(helpurl+onlinehelp);
            Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   			return ;
   		}

			// The Help About request brings up the About dialog window.

			if (source == about)
			{
				if (aboutdialog != null) aboutdialog.show() ;
				return ;
			}

         // Options request.  Open Search tab.

         if (source == options)
         {
            OptionsDialog op = new OptionsDialog(this,3) ;
            op.setVisible(true) ;
            return ;
         }

         // The Register dialog enables setting of user and password.
         // Also, we can change our current web host site.

         if (register == source)
         {
            String s = Kisekae.getCaptions().getString("RegisterTitle") ;
            RegisterDialog rd = new RegisterDialog(this,s,true) ;
            rd.show() ;
            
            // Process a change to the current web site.
            
            if (currentweb == null)
            {
               currentweb = OptionsDialog.getKissWeb() ;
               if (currentweb == null) return ;
               reset() ;
               update() ;
               Vector v = getHistory() ;
               v.addElement(currentweb) ;
               s = currentweb.replace('\\','/') ;
               int n = s.lastIndexOf('/') ;
               if (n > 0) s = s.substring(0,n) ;
               editor.setHosted(s) ;
               setPage((String) v.lastElement()) ;
            }
            return ;
         }
         
         // Window display commands are of the form 'nn. title'. If we have
         // one of these bring the window to the front.
         
         if (evt.getActionCommand().indexOf(". ") > 0)
         {
            String s = evt.getActionCommand() ;
            s = s.substring(s.indexOf(". ")+2) ;
            Vector windows = KissFrame.getWindowFrames() ;
            for (int i = 0 ; i < windows.size() ; i++)
            {
               KissFrame f = (KissFrame) windows.elementAt(i) ;
               if (s.equals(f.getTitle()))
               {
                  if (f.getState() == Frame.ICONIFIED) 
                     f.setState(Frame.NORMAL) ;
                  f.toFront() ;
                  break ;
               }
            }
            return ;
         }

         // The URL text entry causes a switch to a new page.

         if (source == enterurl)
         {
            URL url = null ;
            String s = evt.getActionCommand() ;
            try { url = new URL(s) ; }
            catch (MalformedURLException e) { return ; }
            if (ArchiveFile.isArchive(s)) { loadArchive(url,true) ; return ; }
            String s1 = currentweb ;
            if (s1 == null) s1 = "" ;
            int n = s1.lastIndexOf('/') ;
            if (n > 0) s1 = s1.substring(0,n) ;

            // If the link is outside our current web site we show it in a
            // browser window.

//          if (!s.startsWith(s1) && !s.startsWith("file:"))
//          {
//             BrowserControl.displayURL(s) ;
//             return ;
//          }

            // If our URL is to a file, we need to determine if this is to
            // a directory or a specific file.

            if (s.startsWith("file:") && !s.endsWith("/"))
            {
               String s2 = url.getPath() ;
               File f = new File(s2) ;
               if (f.isDirectory()) s += File.separator ;
            }

            // Add the entry to our history list and show the URL.

            Vector v = getHistory() ;
            Object o = (v.size() > 0) ? v.lastElement() : null ;
            if (!s.equals(o))
            {
               setHistoryLocation(v.size()) ;
               v.addElement(s) ;
            }
            setPage(s) ;
            return ;
         }

         // Watch for URL load callbacks.  These signal completion of the
         // download of a URL file.

   		if ("UrlLoader Callback".equals(evt.getActionCommand()))
   		{
         	if (urlloader == null) return ;
         	String pathname = urlloader.getTempFileName() ;
            URL sourceURL = urlloader.getURL() ;
            String action = urlloader.getAction() ;
            MemFile mem = urlloader.getMemoryFile() ;
            urlloader = null ;
            if (pathname == null) return ;
            if (sourceURL == null) return ;

            // Create a FileOpen object for this temporary file.

            File f = new File(sourceURL.getFile()) ;
            String filename = f.getName() ;
    			FileOpen fdnew = new FileOpen(me,pathname,"r") ;
            fdnew.setSourceURL(sourceURL) ;
            fdnew.open() ;

            // Determine the load action state.  If it is unknown we
            // default to an UltraKiss open state.

            if ("save".equals(action))
            {
               if (!Kisekae.isSecure())
               {
                  FileWriter filewriter = null ;
                  String s = Kisekae.getCaptions().getString("SaveDownloadFileText") ;
                  pathname = selectArchive(s, filename, FileDialog.SAVE) ;
                  if (pathname == null) return ;
      
                  // Prompt if we are overwriting a file.

                  f = new File(pathname) ;
                  if (f.exists())
                  {
                     s = Kisekae.getCaptions().getString("FileSaveReplaceFile") ;
                     int i1 = s.indexOf('[') ;
                     int j1 = s.indexOf(']') ;
                     if (i1 >= 0 && j1 > i1)
                        s = s.substring(0,i1+1) + filename + s.substring(j1) ;
                     int i = JOptionPane.showConfirmDialog(parent, s,
            				Kisekae.getCaptions().getString("ReplaceFileText"),
                        JOptionPane.WARNING_MESSAGE) ;
                     if (i == JOptionPane.CANCEL_OPTION) return;
                  }
                  
                  // Write downloaded archive file if archive
                  if (ArchiveFile.isArchive(pathname))
                     filewriter = new FileWriter(this,fdnew.getZipFile(),pathname) ;
                  else
                  {
                     // Write downloaded image file or other if downloaded
                     Vector v = new Vector() ;
                     ArchiveEntry ze = fdnew.getZipEntry() ;
                     v.addElement(ze) ;
                     filewriter = new FileWriter(this,fdnew.getZipFile(),pathname,v) ;  
                  }
                  filewriter.callback.addActionListener( (ActionListener)this) ;
                  Thread thread = new Thread(filewriter) ;
                  thread.start() ;
                  return ;
               }

               // For secure environments, use JNLP for the write.

               else if (mem != null)
               {
                  try
                  {
                     InputStream fc = mem.getInputStream() ;
                     FileSaveService fss = (FileSaveService) ServiceManager.lookup
                         ("javax.jnlp.FileSaveService") ;
                     FileContents fc2 = fss.saveFileDialog(null,null,fc,mem.getFileName()) ;
                     if (fc2 == null) throw new Exception() ;
                     String s = Kisekae.getCaptions().getString("FileSavedText") ;
                     int i1 = s.indexOf('[') ;
                     int j1 = s.indexOf(']') ;
                     if (i1 >= 0 && j1 > i1)
                        s = s.substring(0,i1) + fc2.getName() + s.substring(j1+1) ;
                     JOptionPane.showMessageDialog(me, s,
                        Kisekae.getCaptions().getString("FileWriterTitle"),
                        JOptionPane.INFORMATION_MESSAGE) ;
                  }
                  catch (Exception e)
                  {
                     PrintLn.println("WebFrame: JNLP FileSaveService is not available.");
                     JOptionPane.showMessageDialog(me,
                        Kisekae.getCaptions().getString("FileWriteError")
                        + "\n" + e.toString(),
                        Kisekae.getCaptions().getString("FileSaveException"),
                        JOptionPane.WARNING_MESSAGE) ;
                  }
                  return ;
               }
            }

            // If no configuration element exists in the file we check
            // for an LZH element in the archive and unpack it.

            ArchiveEntry ze = fdnew.getZipEntry() ;
   			if (ze == null) ze = fdnew.showConfig(me) ;
   	      if (ze == null)
   	      {
               ze = fdnew.findEntry(".lzh") ;
               if (ze != null)
               {
                  if (fdnew.unpack(ze)) ze = fdnew.showConfig(me) ;
               }
               if (ze == null)
               {
      	      	fdnew.close() ;
      	         return ;
      	      }
   	      }

            // Load the URL file.  Close this frame.
            // Retain the sourceURL for basing future FKiSS open commands in CNF.

            close() ;
            urlloader = null ;
            MainFrame mf = Kisekae.getMainFrame() ;
            MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
   	      if (menu != null)
            {
               menu.setNoCopy(nocopy);
               if (ze.isConfiguration()) menu.setDownloadURL(sourceURL) ;
               menu.openContext(fdnew,ze) ;
            }
            else
               fdnew.close() ;
            return ;
         }
         
         // Notify dialog cancel request if archive load takes too long.
         
   		if ("NotifyDialog Cancel".equals(evt.getActionCommand()))
   		{
            if (OptionsDialog.getDebugControl())
               PrintLn.println("WebFrame: NotifyDialog Cancel, urlloader=" + urlloader) ;
            if (urlloader != null) urlloader.setInterrupted(true) ;
            String urlname = (loadarchive != null) ? loadarchive.toExternalForm() : null;
            if (urlname != null)
            {
               cancel.put(urlname, Boolean.valueOf(true)) ;
               if (OptionsDialog.getDebugControl())
                  PrintLn.println("WebFrame: interrupt archive load " + urlname) ;
            }
         }
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("WebFrame: Out of memory.") ;
			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable ex)
		{
			PrintLn.println("WebFrame: Internal fault, action " + evt.getActionCommand()) ;
			ex.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError") + " - " ;
         s += Kisekae.getCaptions().getString("ActionNotCompleted") ;
         s += "\n" + ex.toString() ;
			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            ex.printStackTrace(ps) ;
            os.close() ;
            FileReader is = new FileReader(f) ;
            LineNumberReader lr = new LineNumberReader(is) ;
            String s1 = lr.readLine() ;
            s1 = lr.readLine() ;
            int traceline = 0 ;
            while (s1 != null)
            {
               s += "\n" + s1.trim() ;
               s1 = lr.readLine() ;
               if (traceline++ > 10) break ;
            }
         }
         catch (EOFException eof) { }
         catch (Exception ex1) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(this, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}

   // This event is fired by the JEditorPane when the document is loaded.  
   
   public void propertyChange(PropertyChangeEvent e)
	{
   	Runnable awt = new Runnable()
		{ 
         public void run() 
         { 
            pageloadtime = System.currentTimeMillis() - Configuration.getTimestamp() ;
            long loadtime = pageloadtime - setpagetime ;
            String s = "Time to load: " + loadtime + " ms" ;
            if (editor != null) statusbar.showStatus(s) ;
            if (OptionsDialog.getDebugControl())
               PrintLn.println("WebFrame: Page loaded, location " + location + ", time to load = " + loadtime + " ms");
         } 
      } ;
		javax.swing.SwingUtilities.invokeLater(awt) ;
  		if (editor != null) Kisekae.setCursor(editor,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
  		Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      update() ;
	}


   // Method to show a file dialog to select an archive file.  This method
   // returns the path name of the selected file.

	private String selectArchive(String title, String fname, int mode)
	{
      String name = null ;
      String pathname = null ;
      String filename = null ;
      String dirname = null ;
      String extension = null ;
      FileDialog chooser = null ;
      JFileChooser jchooser = null ;

      // We do not have a known path to an archive file.  Show a file
      // dialog to choose a file.

      try
      {
         while (name == null)
         {
            if (OptionsDialog.getSystemLF())
            {
               chooser = new FileDialog(this) ;
               chooser.setTitle(title) ;
               chooser.setMode(FileDialog.SAVE) ;
            }
            else
            {
               jchooser = new JFileChooser() ;
               jchooser.setLocale(Kisekae.getCurrentLocale()) ;
               jchooser.setDialogType(JFileChooser.SAVE_DIALOG) ;
            }

            // Establish the current directory.

   			URL codebase = Kisekae.getBase() ;
            if (codebase == null) throw new SecurityException("unknown codebase") ;
   			String directory = codebase.getFile() ;
   			if (directory == null) directory = System.getProperty("user.dir") ;
            if (chooser != null)
            {
               if (directory != null)
                  chooser.setDirectory(directory) ;
               if (dirname != null)
                  chooser.setDirectory(dirname) ;
               if (fname != null)
                  chooser.setFile(fname) ;
               chooser.show() ;
               name = chooser.getFile() ;
            }
            if (jchooser != null)
            {
               if (directory != null)
                  jchooser.setCurrentDirectory(new File(directory)) ;
               if (dirname != null)
                  jchooser.setCurrentDirectory(new File(dirname)) ;
               if (fname != null)
                  jchooser.setSelectedFile(new File(fname)) ;
               int i = jchooser.showSaveDialog(this) ;
               if (i == JFileChooser.APPROVE_OPTION)
               {
                  File f = jchooser.getSelectedFile() ;
                  if (f != null) name = f.getName() ;
               }
            }

   	      // Check to see if a valid archive type was selected.

            if (name == null) break ;
  		      if (ArchiveFile.isArchive(name)) break ;
  		      if (ArchiveFile.isImage(name)) break ;
  		      if (ArchiveFile.isAudio(name)) break ;
            String s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
            JOptionPane.showMessageDialog(this,
               s + "\n" +
               Kisekae.getCaptions().getString("InvalidTypeText"),
               Kisekae.getCaptions().getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				name = null ;
			}
      }

      // Catch security exceptions.

      catch (SecurityException e)
      {
         PrintLn.println("WebFrame: Archive file open exception, " + e.getMessage()) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

		// Get the selected file name.

      File f = null ;
      if (chooser != null)
      {
         if (name == null) { chooser.dispose() ; return null ; }
         dirname = chooser.getDirectory() ;
         filename = chooser.getFile() ;
         f = new File(dirname,filename) ;
         chooser.dispose() ;
      }
      if (jchooser != null)
      {
         if (name == null) return null ;
         f = jchooser.getSelectedFile() ;
      }

      // Establish the full path name.

      if (f == null) return null ;
		pathname = f.getPath() ;
      dirname = f.getParent() ;
      filename = f.getName() ;
		int i = filename.lastIndexOf(".") ;
		extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;
      return pathname ;
	}


	// Hyperlink event listener.  We process LZH, ZIP and JAR file downloads
   // with our UrlLoader. Normal hyperlinks switch to the required page.

	public void hyperlinkUpdate(HyperlinkEvent evt)
	{
      if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) 
      {
         URL evturl = evt.getURL() ;
         String description = (evturl != null) ? evturl.toExternalForm() : "" ;
         statusbar.showStatus(description) ;
         Kisekae.setCursor(me,Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ;
         return ;
      }
      
      if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) 
      {
         Kisekae.setCursor(me,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         statusbar.showStatus("") ;
         return ;
      }
      
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
      {
         hyperlinktime = System.currentTimeMillis() - Configuration.getTimestamp() ;
         statusbar.showStatus("") ;
         URL evturl = evt.getURL() ;
         String description = evt.getDescription() ;
         if (OptionsDialog.getDebugControl())
            PrintLn.println("WebFrame: hyperlink to " + description) ;


         // If the URL could not be decoded, parse the description.

         try
         {
            if (description.startsWith("file:") || description.startsWith("jar:"))
               evturl = new URL(description) ;
            if (evturl == null)
            {
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
         }
         catch (MalformedURLException e) { evturl = null ; }         
         if (evturl == null) return ;
         
         // If the link is to an archive, load it.

         String urlname = evturl.toExternalForm() ;
         if (ArchiveFile.isArchive(urlname))
            { loadArchive(evturl,true) ; return ; }

         // If the link is to an image file prompt for download.

         if (ArchiveFile.isImage(urlname))
            { loadArchive(evturl,true) ; return ; }

         // If the link is to a media file prompt for download.

         if (ArchiveFile.isAudio(urlname))
            { loadArchive(evturl,true) ; return ; }

         // If our current web is on our local file system and we link to
         // a remote HTML page, or if our url has a fragment "#currentweb" 
         // then adjust the current website to point to the new remote web.

         boolean inframe = false ;
         String s = currentweb ;
         if (s == null) s = "" ;
         int n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         if ((s.startsWith("file:") || s.startsWith("jar:")) && 
                 (urlname.startsWith("http:") || urlname.startsWith("https:"))
               || (urlname.toLowerCase().contains("#currentweb")))
         {
            currentweb = urlname ;
            s = currentweb ;
            n = s.lastIndexOf('/') ;
            if (n > 0) s = s.substring(0,n) ;
            editor.setHosted(s);
         }
                        
         // Not all internal links on remote URL's start with www.  If it is 
         // missing we correct the URL.
      
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
         if (urlname.startsWith(s)) inframe = true ;
         if (inframe)
         {
            Vector v = getHistory() ;
            for (int i = v.size()-1 ; i > getHistoryLocation() ; i--)
               v.removeElementAt(i) ;
            setHistoryLocation(v.size()) ;

            // If our URL is to a file, we need to determine if this is to
            // a directory or a specific file.

            if (urlname.startsWith("file:") && !urlname.endsWith("/"))
            {
               String s1 = evturl.getPath() ;
               File f = new File(s1) ;
               if (f.isDirectory()) urlname += File.separator ;
            }

            // Capture any url links to main menu operations.
            
            if (urlname.contains("file:") && urlname.contains("helpbugreport"))
            {
            	Runnable awt = new Runnable()
               { 
                  public void run() 
                  {
                     MainFrame mainframe = Kisekae.getMainFrame() ;
                     if (mainframe == null) return ;
                     MainMenu mm = mainframe.getMainMenu() ;
                     mm.bugreport.doClick() ;
                  } 
               } ;
        			SwingUtilities.invokeLater(awt) ;
               return ;
            }                    
            
            // Add this URL to our history list and show in the browser.

            v.addElement(urlname) ;
            setPage(urlname) ;
            return ;
         }

         // If the link is outside our current web site we show it in a browser
         // window.

         BrowserControl.displayURL(urlname) ;
         return ;
      }
   }


   // A function to load an archive file.  Download prompts are not
   // shown for local files.
   //
   // If the download is requested a non-modal wait dialog can be shown.
   // This dialog confirms that the connection request may take some time.
   // The wait dialog allows the request to be cancelled if it has not started.
   //
   // The actual download proceeds in a new thread to release the AWT thread.
   // The wait dialog, being non-modal, did not properly construct the screen
   // if it was created on the current AWT thread.
   
   void loadArchive(URL url, boolean showrun)
   {
      if (url == null) return ;
      String s = url.toExternalForm() ;
      cancel.remove(s) ;
      loadarchive = url ;
      boolean open = true ;
      if (!(s.startsWith("file:") || s.startsWith("jar:")))
      {
         if (WebDloadDialog.showDialog() && !Kisekae.isWebsocket())
         {
            WebDloadDialog dd = new WebDloadDialog(this,url,showrun) ;
            dd.setVisible(true) ;
            open = dd.open.isSelected() ;
            if (dd.cancel) return ;
         }
      }
      
      // Open the wait dialog on a new ATW thread.  The modeless dialog
      // does not draw otherwise.  The worker also has to be on a new thread.

      Runnable runner = new Runnable()
      { public void run() 
        { showWaitDialog("Wait!\nRemote access can be slow.\n\n") ; } 
      } ;
		SwingUtilities.invokeLater(runner) ;
      
      LoadArchive loadarchive = new LoadArchive(this,url,open) ;
      new Thread(loadarchive).start() ;
   }

   // Implementation of the menu item update of our state when we become
   // visible.  We remove all prior entries and rebuild the Window menu. 
   
   void updateRunState()
   {
      for (int j = windowMenu.getItemCount()-1 ; j >= 0 ; j--)
         windowMenu.remove(j) ;

      // Add new dialog entries

      int n = 0 ;
      Vector v = KissFrame.getWindowFrames() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         KissFrame w = (KissFrame) v.elementAt(i) ;
         String s = w.getTitle() ;
         JMenuItem mi = new JMenuItem(++n + ". " + s) ;
         mi.addActionListener(this) ;
         windowMenu.add(mi) ;
      }
   }

   // Force fire a window closing event to shut down the Portal after a 
   // redirect back to the main frame.
   
   private void fireWindowClose()
   {
      Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
         new WindowEvent(me, WindowEvent.WINDOW_CLOSING));      
   }

	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { updateRunState() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// We overload the KissDialog close method to shut the media files down
	// when the dialog frame is closed.

	public void close()
	{
      if (parent != null)         
      {
         MainMenu mm = parent.getMainMenu() ;
         if (mm != null) mm.setWebFrame(null) ;
      }
      
      if (nd != null) nd.close() ; 
      if (editorkit instanceof WebHTMLEditor) 
         ((WebHTMLEditor) editorkit).clearCache() ;
      
      super.close() ;
		flush() ;
      dispose() ;
	}


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
	// objects when the data set is closed, even if a problem occurs while
	// disposing the dialog window.

   private void flush()
   {
//      history = new Vector() ;         // URL History list
//      currentlocation = 0 ;            // Current history index
//      currentweb = null ;              // Last valid web
      runner = null ;
      if (statusbar != null) statusbar.setStatusBar(false) ;
      statusbar = null ;
      toolbar = null ;
      editor = null ;
      editorkit = null ;
      aboutdialog = null ;
      helper = null ;
      helper2 = null ;
      parent = null ;
		me = null ;
		setVisible(false) ;
		setJMenuBar(null) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
	}

   
   // This function shows a non-modal dialog to advise that the user wait
   // for a URL download connection.  We have seen where this wait can be
   // up to 10 seconds before the download starts.  As the dialog is 
   // modeless is can be used to cancel the download request at any time.
   //
   // A one second delay is programmed before the dialog becomes visible.
   // Not all download requests show the lengthy delay.
   // 
   // If the download proceeds in UrlLoader this dialog is closed with a 
   // call to closeWaitDialog().
   
   private void showWaitDialog(String text)
   {
      nd = new NotifyDialog(me,"Notify",text,null,2,false) ;
     	Runnable awt = new Runnable()
		{ 
         public void run() 
         { 
            while (true)
            {
               try { Thread.sleep(1000) ; }
               catch (InterruptedException e) { break ; }
               if (nd == null) break ;
               if (!nd.isVisible())
               {
                  nd.setVisible(true) ;
                  continue ;
               } 
               String s = nd.getText() ;
               s += ". " ;
               nd.setText(s) ;
            }
         } 
      } ;
  		Thread t = new Thread(awt) ;
      t.start() ;
   }

 
   void closeWaitDialog()
   {      
      if (nd != null) nd.close() ; 
      nd = null ;
   }

   // Inner class to define a connection thread activity.  This is an
   // independent thread to load our first web page.  By loading the
   // page in a thread we release the AWT thread sooner without waiting
   // to see if the first web page is available.

   class WebConnect extends Thread
   {
      public void run()
      {
         try
         {
            Thread thread = Thread.currentThread() ;
            thread.setPriority(Thread.MIN_PRIORITY) ;
            if (location == null) return ;
            connecting = true ;
            location = location.replace('\\','/') ;
            Kisekae.setCursor(me,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            String s = Kisekae.getCaptions().getString("ConnectingToStatus") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + location + s.substring(j1+1) ;
            if (parent != null) parent.showStatus(s) ;
            if (OptionsDialog.getDebugControl())
               PrintLn.println("WebFrame: setPage " + location) ;
            setpagetime = System.currentTimeMillis() - Configuration.getTimestamp() ;
            editor.setPage(location);
            if (location.equals(OptionsDialog.getKissWeb()))
               currentweb = location ;
            update() ;
         }
         catch (Exception e)
         {
            String s = e.toString() ;
            if (s != null && s.indexOf("security") > 0)
            {
               JOptionPane.showMessageDialog(me,
                  Kisekae.getCaptions().getString("InaccessibleSite") + "\n" + location ,
                  Kisekae.getCaptions().getString("SecurityException"),
                  JOptionPane.ERROR_MESSAGE) ;
            }
            else
            {
               s = e.toString() + "\n" + location ;
               JOptionPane.showMessageDialog(me,s,
                  Kisekae.getCaptions().getString("InvalidURLError"),
                  JOptionPane.ERROR_MESSAGE) ;
            }
            Vector v = getHistory() ;
            v.remove(location) ;
            setHistoryLocation(v.size() - 1) ;
            update() ;
         }
         
         // End of connection.  Set display indicators.
         
         setEndConnection() ;            
      }
         
      void setEndConnection() 
      {
         if (!javax.swing.SwingUtilities.isEventDispatchThread())
         {
         	Runnable awt = new Runnable()
    			{ public void run() { setEndConnection() ; } } ;
        		javax.swing.SwingUtilities.invokeLater(awt) ;
         }
         if (me != null)
            Kisekae.setCursor(me,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         if (parent != null) parent.showStatus(null);
         connecting = false ;
      }
   }
 
   // This class initiates a new URL archive file download.  The download is
   // started on this thread to release the AWT thread.  A non-modal dialog
   // can be shown to advise of connection delays and the AWT thread does not
   // want to be delayed.
   
   private class LoadArchive implements Runnable
   {
      private WebFrame parent ;
      private URL url ;
      private boolean open ;
      
      public LoadArchive(WebFrame parent, URL url, boolean open) 
      {
         this.parent = parent ;
         this.url = url ;
         this.open = open ;
      }

      // Determine the download type.
      
      public void run()
      {
         if (url == null) return ;
         String urlname = url.toExternalForm() ;
         Object o = cancel.get(urlname) ;
         if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) return ;
         if (OptionsDialog.getDebugControl())
            PrintLn.println("WebFrame: LoadArchive " + urlname) ;
         urlloader = new UrlLoader(parent,urlname) ;
         urlloader.callback.addActionListener(parent) ;
         urlloader.setConnectionID(Kisekae.getConnectionID());
         urlloader.setAction(open ? "open" : "save") ;
    		Thread loadthread = new Thread(urlloader) ;
         loadthread.start() ;
      }
   }
}
