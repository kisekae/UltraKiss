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
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Vector ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.text.* ;
import javax.swing.text.html.* ;
import javax.jnlp.* ;


final public class WebFrame extends KissFrame
	implements WindowListener, ActionListener, HyperlinkListener
{
	// Dialog attributes

   private static Vector history = new Vector() ;  // URL History list
   private static int currentlocation = 0 ;        // Current history index
   private static String currentweb = null ;       // Last valid web

   private WebFrame me = null ;						   // Reference to ourselves
   private MainFrame parent = null ;               // Reference to mainframe
   private UrlLoader urlloader = null ;            // Reference to loader
   private String location = null ;                // Requested location
   private Thread runner = null ;                  // Our connection thread
   private Vector localhistory = null ;            // Local URL History list
   private int locallocation = 0 ;                 // Local history index
   private boolean connecting = false ;            // If true, connecting
   private boolean nocopy = false ;                // If true, no save of file

	// HelpSet interface objects.

	private static String helpset = "Help/KissBrowser.hs" ;
	private static String helpsection = "kissbrowser.index" ;
	private static String onlinehelp = "kissbrowser/index.html" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;
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
      setHistoryLocation(history.size()-1) ;
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;
      editor.setEditorKit(new WebHTMLEditor());
      runner = new WebConnect() ;
      init() ;
   }
   
   // Constructor for a specified home page.  This constructor
   // does not establish a current web.
   
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
         System.out.println("WebFrame: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace() ; 
      }

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;

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
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuit" : "MenuFileExit" ;
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
      setPage((String) v.lastElement()) ;
		addWindowListener(this) ;
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


   // Set the JEditorPane page to view.   The connection is performed in a
   // separate thread.

   public void setPage(String location)
   {
      if (runner == null || runner.isAlive()) return ;
      runner = new WebConnect() ;
      this.location = location ;
      runner.start() ;
   }


   // Set a nocopy indicator that was referenced on the URL for download.
   // Pass this to the mainmenu when we load the URL file to disable menu 
   // Save and Save As functions.

   public void setNoCopy(boolean b) { nocopy = b ; }


   // Menu items for browser forward and back control.

   void back()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() - 1 ;
      if (i < 0 || i >= v.size()) 
      {
         me.toBack() ;
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
      if ((s.startsWith("http:") || s.startsWith("https:")) && (location.startsWith("file:") || location.startsWith("jar:")))
      {
         currentweb = location ;
         s = currentweb ;
         n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         editor.setHosted(s);
      }
      setPage(location) ;
   }

   void forward()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() + 1 ;
      if (i < 0 || i >= v.size()) return ;
      location = (String) v.elementAt(i) ;
      setHistoryLocation(i) ;

      // If our current web is on a local file system and we link forward to
      // a remote host, then we adjust our current web site to point to the
      // to the remote host.

      String s = currentweb ;
      if (s == null) s = "" ;
      int n = s.lastIndexOf('/') ;
      if (n > 0) s = s.substring(0,n) ;
      if ((location.startsWith("http:") || location.startsWith("https:")) && (s.startsWith("file:") || s.startsWith("jar:")))
      {
         currentweb = location ;
         s = currentweb ;
         n = s.lastIndexOf('/') ;
         if (n > 0) s = s.substring(0,n) ;
         editor.setHosted(s);
      }
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
      }
      catch (Exception e) { } ;
   }

   
   void refresh()
   {
      Vector v = getHistory() ;
      int i = getHistoryLocation() ;
      if (i < 0 || i >= v.size()) return ;
      location = (String) v.elementAt(i) ;
      setHistoryLocation(i) ;
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
      	Runnable runner = new Runnable()
			{ public void run() { update() ; } } ;
   		javax.swing.SwingUtilities.invokeLater(runner) ;
      }
      
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
   
   
   // Translate between permanent and local history
   
   private int getHistoryLocation()
   {
      if (localhistory != null) return locallocation ;
      return currentlocation ;
   }
   
   private void setHistoryLocation(int n)
   {
      if (localhistory != null) locallocation = n ;
      currentlocation = n ;
   }
   
   private Vector getHistory()
   {
      if (localhistory != null) return localhistory ;
      return history ;
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
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            browser.displayURL(helpurl+onlinehelp);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
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
            if (ArchiveFile.isArchive(s)) { loadArchive(url) ; return ; }
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
                  String s = Kisekae.getCaptions().getString("SaveDownloadFileText") ;
                  pathname = selectArchive(s, filename, FileDialog.SAVE) ;
                  if (pathname == null) return ;
                  FileWriter filewriter = new FileWriter(this,fdnew.getZipFile(), pathname) ;
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
                     System.out.println("WebFrame: JNLP FileSaveService is not available.");
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

   			ArchiveEntry ze = fdnew.showConfig(me) ;
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

            close() ;
            MainFrame mf = Kisekae.getMainFrame() ;
            MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
   	      if (menu != null)
            {
               menu.setNoCopy(nocopy);
               menu.openContext(fdnew,ze) ;
            }
            else
               fdnew.close() ;
         }
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			System.out.println("WebFrame: Out of memory.") ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable ex)
		{
			System.out.println("WebFrame: Internal fault, action " + evt.getActionCommand()) ;
			ex.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError") + " - " ;
         s += Kisekae.getCaptions().getString("ActionNotCompleted") ;
         s += "\n" + ex.toString() ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

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
            String s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
            JOptionPane.showMessageDialog(this,
               s + "\n" +
               Kisekae.getCaptions().getString("SaveAsArchiveText"),
               Kisekae.getCaptions().getString("FileSaveException"),
               JOptionPane.ERROR_MESSAGE) ;
				name = null ;
			}
      }

      // Catch security exceptions.

      catch (SecurityException e)
      {
         System.out.println("WebFrame: Archive file open exception, " + e.getMessage()) ;
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
         return ;
      }
      
      if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) 
      {
         statusbar.showStatus("") ;
         return ;
      }
      
      if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
      {
         statusbar.showStatus("") ;
         URL evturl = evt.getURL() ;
         String description = evt.getDescription() ;
         if (OptionsDialog.getDebugControl())
            System.out.println("WebFrame: hyperlink to " + description) ;


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

         // If the link is to an archive, load it.

         if (evturl == null) return ;
         String urlname = evturl.toExternalForm() ;
         if (ArchiveFile.isArchive(urlname))
            { loadArchive(evturl) ; return ; }

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
            editor.setHosted(s);
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
//         if (urlname.startsWith(s)) inframe = true ;
//         if (!urlname.startsWith(s)) inframe = true ;
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

   private void loadArchive(URL url)
   {
      boolean open = true ;
      String s = url.toExternalForm() ;
      if (!(s.startsWith("file:") || s.startsWith("jar:")))
      {
         if (WebDloadDialog.showDialog())
         {
            WebDloadDialog dd = new WebDloadDialog(this,url) ;
            dd.setVisible(true) ;
            open = dd.open.isSelected() ;
            if (dd.cancel) return ;
         }
      }

      // Determine the download type.

      String urlname = url.toExternalForm() ;
      urlloader = new UrlLoader(this,urlname) ;
      urlloader.callback.addActionListener(this) ;
      urlloader.setConnectionID(Kisekae.getConnectionID());
      urlloader.setAction(open ? "open" : "save") ;
 		Thread loadthread = new Thread(urlloader) ;
      loadthread.start() ;
      close() ;
      return ;
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
         if (mm != null) mm.clearWebFrame() ;
      }
      
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
      if (statusbar != null) statusbar.setStatusBar(false) ;
      statusbar = null ;
		me = null ;
		setVisible(false) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
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
            me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            String s = Kisekae.getCaptions().getString("ConnectingToStatus") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + location + s.substring(j1+1) ;
            parent.showStatus(s) ;
            activebtn.setEnabled(true) ;
            editor.setPage(location);
            enterurl.setText(location) ;
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
         	Runnable runner = new Runnable()
    			{ public void run() { setEndConnection() ; } } ;
        		javax.swing.SwingUtilities.invokeLater(runner) ;
         }
         me.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         parent.showStatus(null);
         connecting = false ;
         activebtn.setEnabled(false) ;
      }
   }
}


