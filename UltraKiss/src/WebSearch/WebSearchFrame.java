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
* WebSearchFrame class
*
* Purpose:
*
* This class defines a simple web browser for searching and downloading sets
* from a host server.
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

import java.io.* ;
import java.net.* ;
import java.awt.* ;
import java.util.Vector ;
import java.util.ResourceBundle ;
import java.beans.PropertyChangeListener ;
import java.beans.PropertyChangeEvent ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.text.* ;
import javax.swing.text.html.* ;
import Kisekae.Kisekae ;
import Kisekae.MainFrame ; 
import Kisekae.MainMenu ;
import Kisekae.OptionsDialog ;
import Kisekae.KissFrame ;
import Kisekae.HelpLoader ; 
import Kisekae.AboutBox ; 
import Kisekae.LogFile ; 
import Kisekae.BrowserControl ; 
import Kisekae.WebFrame ;
import Kisekae.FileOpen ;
import Kisekae.KissPreferences ;
import Kisekae.PrintLn ;


final public class WebSearchFrame extends KissFrame
        implements WindowListener, ActionListener
{
	private static String helpset = "Help/WebSearch.hs" ;
	private static String helpsection = "websearch.index" ;
	private static String onlinehelp = "websearch/index.html" ;
	private static String instructions = "HTML/WebSearch.html" ;
   private Timer timer = null ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;
   
   // Dialog attributes

   private WebSearchFrame me = null ;					// Reference to ourselves
   private MainFrame parent = null ;               // Our parent frame
   private ThreadGroup group = null ;              // Search thread group
   private Scheduler scheduler = null ;            // Search thread scheduler
   private StatusBar statusbar = null ;            // Window status bar
   private WaitDialog waitbox = null ;             // Waiting dialog 
   private String baselocation = null ;            // Location of base site
   private String absoluteformname = null ;        // Location of generated HTML
   private URL pageurl = null ;                    // URL of base page
   private boolean stop = false ;                  // If true, stop processing
   private boolean activated = false ;             // If true, search was activated
   private boolean terminating = false ;           // If true, terminating
   private int initdivider = 0 ;                   // Initial split divider
   
   // User interface objects.

   private JPanel panel1 = new JPanel() ;
   private JPanel panel2 = new JPanel() ;
   private JPanel panel3 = new JPanel() ;
   private JPanel panel4 = new JPanel() ;
   private JSplitPane split1 = new JSplitPane() ;
   private JTextField enterurl = new JTextField() ;
   private JTextPane tracearea = new JTextPane() ;
   private JEditorPane editor = new JEditorPane() ;
   private EditorKit editorkit = null ;
	private JButton activebtn = new JButton() ;
	private JTextField address = new JTextField() ;
   private JScrollPane scrollpane1 = new JScrollPane() ;
   private JScrollPane scrollpane2 = new JScrollPane() ;
   private BorderLayout borderLayout1 = new BorderLayout();
   private BorderLayout borderLayout2 = new BorderLayout();
   private BorderLayout borderLayout3 = new BorderLayout();
   private BorderLayout borderLayout4 = new BorderLayout();

   // Menu items

   private JMenu fileMenu ;
   private JMenu optionMenu ;
   private JMenu helpMenu ;
   private JMenuItem open ;
   private JMenuItem exit ;
   private JMenuItem help ;
   private JMenuItem cancel ;
   private JMenuItem about ;
   private JMenuItem options ;
   private JMenuItem logfile ;
   private Insets insets = null ;

   // State flags

   protected boolean getlinkactive = false ;
   protected boolean vallinkactive = false ;
   protected boolean buildformactive = false ;
   
   // Text attributes

   private int errorpos = -1 ;            // Error position
   private static SimpleAttributeSet errorset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet warnset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet normalset = new SimpleAttributeSet() ;
   private static SimpleAttributeSet goodset = new SimpleAttributeSet() ;
   
   static
   {
      StyleConstants.setForeground(errorset,Color.red) ;
      StyleConstants.setForeground(warnset,Color.blue) ;
      StyleConstants.setForeground(goodset,Color.gray) ;
   }

   // Callback button for URL validation.

   protected JButton urlcallback = new JButton("WebFrame URL Callback") ;
   protected JButton valcallback = new JButton("WebFrame Val Callback") ;
   protected JButton bldcallback = new JButton("WebFrame Bld Callback") ;
   protected JButton idxcallback = new JButton("WebFrame Idx Callback") ;

	// Register for events.

	PropertyChangeListener editorListener = new PropertyChangeListener()
   {
		public void propertyChange(PropertyChangeEvent e)
      {
         // A page change sets the split pane divider to display the 
         // page when loaded. A document change essentially hides the 
         // page to view the traces, unless the user adjusted the divider.
         
         if ("page".equals(e.getPropertyName()))
         {
            split1.setDividerLocation(0.9) ;
            initdivider = split1.getDividerLocation() ;
         }
         if ("document".equals(e.getPropertyName()))
         {
            if (split1.getDividerLocation() == initdivider)
            {
               split1.setDividerLocation(0) ;
               initdivider = -1 ;
            }
         }
      }
	} ;

   // Constructor

   public WebSearchFrame(MainFrame parent)
   {
      super("WebSearch") ;
      this.parent = parent ;
      init() ;
      super.open() ;
      setVisible(true) ;
      if (helper != null) helper.setSize(getSize());
      if (parent != null) parent.setVisible(false) ;
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
   }


   // Frame initialization.

   private void init()
   {
      // Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;
 
		// Active button

		activebtn = new JButton() ;
		URL iconfile = getClass().getClassLoader().getResource("Images/web1.gif") ;
		if (iconfile != null) activebtn.setIcon(new ImageIcon(iconfile)) ;
		activebtn.setMargin(new Insets(1,1,1,1)) ;
		activebtn.setBorder(BorderFactory.createEmptyBorder(0,5,0,5)) ;
		activebtn.setToolTipText("Searching") ;
      activebtn.setRequestFocusEnabled(false) ;
		activebtn.setAlignmentY(0.5f) ;
		iconfile = getClass().getClassLoader().getResource("Images/web1.gif") ;
		if (iconfile != null) activebtn.setIcon(new ImageIcon(iconfile)) ;
		iconfile = getClass().getClassLoader().getResource("Images/web2.gif") ;
		if (iconfile != null) activebtn.setDisabledIcon(new ImageIcon(iconfile)) ;
      activebtn.setEnabled(false) ;
      address.setText("Address:") ;
      address.setMargin(new Insets(0,0,0,5)) ;
      address.setEnabled(false) ;
      
      // Status Bar
      
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;
     
     // Construct the user interface.

      try { jbInit() ; }
      catch(Exception ex)
      { 
         PrintLn.println("WebSearchFrame: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace() ; 
      }

      // Set up the menu bar.

      JMenuBar mb = new JMenuBar() ;
      fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
      String jv = System.getProperty("java.version") ;
      int rm = (jv.indexOf("1.2") == 0) ? 2 : 26 ;
      insets = new Insets(2,2,2,rm) ;
      fileMenu.setMnemonic(KeyEvent.VK_F) ;
      fileMenu.setMargin(insets) ;
      fileMenu.setMnemonic(KeyEvent.VK_F) ;
      fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
      open.setMnemonic(KeyEvent.VK_O) ;
      open.addActionListener(this) ;
      fileMenu.add((cancel = new JMenuItem(Kisekae.getCaptions().getString("CancelMessage")))) ;
      cancel.setMnemonic(KeyEvent.VK_C) ;
      cancel.addActionListener(this) ;
      fileMenu.addSeparator() ;
      fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString("MenuFileExit")))) ;
      exit.setMnemonic(KeyEvent.VK_X) ;
      exit.addActionListener(this) ;
      mb.add(fileMenu) ;

      optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
      optionMenu.setMnemonic(KeyEvent.VK_O) ;
      optionMenu.setMargin(insets) ;
      optionMenu.add((options = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsOptions")))) ;
      options.setMnemonic(KeyEvent.VK_C) ;
      options.addActionListener(this) ;
      mb.add(optionMenu) ;

		// Create the Help menu and About dialog.

      aboutdialog = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
		helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
		helpMenu.setMargin(insets) ;
		helpMenu.setMnemonic(KeyEvent.VK_H) ;
		mb.add(helpMenu);
		helpMenu.add((help = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpContents")))) ;
		help.setMnemonic(KeyEvent.VK_C) ;
		help.setEnabled(helper != null && helper.isLoaded()) ; 
      if (helper != null) helper.addActionListener(help) ;
      if (!Kisekae.isHelpInstalled()) help.addActionListener(this) ;
      if (!Kisekae.isHelpInstalled()) help.setEnabled(true) ; 
      MainFrame mf = Kisekae.getMainFrame() ;
      MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;  
      if (menu != null)
      {
         helpMenu.add((logfile = new JMenuItem(Kisekae.getCaptions().getString("MenuViewLogFile")))) ;
         logfile.setEnabled(LogFile.isOpen()) ;
         logfile.addActionListener(menu) ;
         logfile.setMnemonic(KeyEvent.VK_L) ;
      }
		helpMenu.addSeparator() ;
		helpMenu.add((about = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpAbout")))) ;
		about.setMnemonic(KeyEvent.VK_A) ;
		about.addActionListener(this);

      setJMenuBar(mb) ;
      setValues() ;

      // Set listeners.

      editor.setEditable(false);
      editor.addPropertyChangeListener(editorListener) ;
      enterurl.addActionListener(this);
      urlcallback.addActionListener(this);
      valcallback.addActionListener(this);
      bldcallback.addActionListener(this);
      idxcallback.addActionListener(this);
      addWindowListener(this) ;
  }


   // User interface initialization.

   void jbInit() throws Exception
   {
      panel1 = new JPanel() ;
      panel1.setLayout(borderLayout1) ;
      panel3 = new JPanel() ;
      panel3.setLayout(borderLayout3) ;
      panel3.add(enterurl,BorderLayout.CENTER) ;
      panel3.add(activebtn,BorderLayout.EAST) ;
      panel3.add(address,BorderLayout.WEST) ;
      panel1.add(panel3,BorderLayout.NORTH) ;
      enterurl.setToolTipText("Enter the URL to search.  Use http:// or https:// or file:// protocol.");
      editorkit = editor.getEditorKit() ;
      scrollpane1 = new JScrollPane(editor) ;
      panel1.add(scrollpane1,BorderLayout.CENTER) ;
      panel2 = new JPanel() ;
      panel2.setLayout(borderLayout2) ;
      tracearea = new JTextPane() ;
      scrollpane2 = new JScrollPane(tracearea) ;
      panel2.add(scrollpane2,BorderLayout.CENTER) ;
      split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,panel1,panel2) ;
      panel4 = new JPanel() ;
      panel4.setLayout(borderLayout4) ;
      panel4.add(split1,BorderLayout.CENTER) ;
      panel4.add(statusbar,BorderLayout.SOUTH) ;
      getContentPane().add(panel4) ;
   }


   // Method to set the dialog field values.

   private void setValues()
   {
      String title = "WebSearch Tool" ;
      setTitle(title) ;
      enterurl.setText("") ;
      try
      {
         URL url = getClass().getClassLoader().getResource(instructions) ;
         editor.setPage(url) ;
      }
      catch (Exception e) 
      { 
         PrintLn.println("WebSearch: " + e) ;
      }
   }


   // Set the WebEditorPane page to view.

   public void setPage(String location)
   {
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         InputStream is = getStream(new URL(location)) ;
         editor.setEditorKit(null) ;
         editor.read(is,null) ;
         baselocation = pageurl.toExternalForm() ;
         enterurl.setText(baselocation) ;
         activebtn.setEnabled(true) ;

         // Initiate the parse.

         addTrace("Begin WebSearch",1) ;
         PrintLn.println("WebSearch: begins for " + location) ;
         group = new ThreadGroup("SearchThreads") ;
         Scheduler.reset() ;
         GetLinks.reset() ;
         ValidateLinks.reset() ;
         BuildForm.reset() ;
         scheduler = new Scheduler() ;
         scheduler.startScheduler() ;
         addTrace("Begin URL scan",1) ;
         GetLinks gl = new GetLinks(this,location) ;
         Thread thread = new Thread(group,gl) ;
         thread.start() ;
      }
      catch (Exception e)
      {
         String s = e.toString() + "\n" + location ;
         JOptionPane.showMessageDialog(this,s,"Invalid URL",JOptionPane.ERROR_MESSAGE) ;
         activated = false ;
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
   }
   
   
   // Set the status bar state.
   
   public void showStatus(String s)
   {
      if (statusbar == null) return ;
      statusbar.showStatus(s) ;
   }


   protected InputStream getStream(URL page) throws IOException
   {
      URLConnection conn = page.openConnection() ;
      if (conn instanceof HttpURLConnection)
      {
         HttpURLConnection hconn = (HttpURLConnection) conn;
         // HTTP Response code 403 for URL
//         hconn.addRequestProperty("User-Agent", "UltraKiss");
         hconn.setInstanceFollowRedirects(false);
         int response = hconn.getResponseCode();
         boolean redirect = (response >= 300 && response <= 399);

         /*
         * In the case of a redirect, we want to actually change the URL
         * that was input to the new, redirected URL
         */

         if (redirect)
         {
            String loc = conn.getHeaderField("Location");
            if (loc.startsWith("http", 0)) {
               page = new URL(loc);
            } else {
               page = new URL(page, loc);
            }
            return getStream(page);
         }
      }

      pageurl = page ;
      InputStream in = conn.getInputStream();
      return in;
   }

   // Method to return the site base location name.

   String getBaseLocation() { return baselocation ; }

   // Method to identify if search is local or remote.

   public boolean isLocalSearch() 
   { 
      if (baselocation == null) return true ; 
      String s = baselocation.toLowerCase() ;
      if (s.startsWith("file:")) return true ;
      return false ;
   }

   // Method to add a trace entry to our trace area.

   void addTrace(String s) { addTrace(s,0) ; }
   void addTrace(String s, int type) 
   { 
      if (type == 0) appendText(s + "\n",normalset) ; 
      else if (type == 1) appendText(s + "\n",warnset) ; 
      else if (type == 2) appendText(s + "\n",errorset) ; 
      else if (type == 3) appendText(s + "\n",goodset) ; 
      else appendText(s + "\n") ; 
   }

   // A function to convert file separator characters.

   private String convertSeparator(String s)
   {
      if (s == null) return null ;
      s = s.replace('/',File.separatorChar) ;
      s = s.replace('\\',File.separatorChar) ;
      return s ;
   }

   
   // A method to show the file chooser dialog to select a directory.
   
   private void showFileOpen()
   {
      FileOpen fo = new FileOpen(this) ;
      fo.show("Search Directory",null,true) ;
      String s = fo.getPath() ;
      if (s == null) return ;
      File f = new File(s) ;
      if (f.isFile()) s = f.getParent() ;
      if (!(s.endsWith(File.separator))) s += File.separator ;
      if (!s.startsWith(File.separator)) s = File.separator + s ;
      s = "file://" + s.replace('\\','/') ;
      enterurl.setText(s) ;
 
      // Signal a textfield input event.
      
      ActionEvent ae = new ActionEvent(enterurl,ActionEvent.ACTION_PERFORMED,s) ;
      actionPerformed(ae) ;
   }
   
   // Method to append text to a styled document.  This allows us to set 
   // text attributes in a TextPane.
   
   private void appendText(String s) { appendText(s,null) ; }
   private void appendText(String s, AttributeSet a)
   {
      try
      {
         if (a == null) a = normalset ;
         StyledDocument doc = tracearea.getStyledDocument() ;
         Position pos = doc.getEndPosition() ;
         int offset = pos.getOffset() - 1 ;
         if (offset < 0) offset = 0 ;
         tracearea.setCaretPosition(offset) ;
         doc.insertString(offset,s,a) ;
      }
      catch (BadLocationException e) { }
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

         // Open request.

         if (source == open)
         {
            if (activated) return ;
            showFileOpen() ;
            return ;
         }

         // Cancel request.

         if (source == cancel)
         {
            stop = true ;
            GetLinks.stopsearch() ;
            ValidateLinks.stopsearch() ;
            BuildForm.stopsearch() ;
            Scheduler.stopScheduler() ;
            activebtn.setEnabled(false) ;
            scheduler = null ;
            showStatus("Search cancelled") ;
            waitbox = new WaitDialog(this,"Search interrupted, please wait ...") ;
            waitbox.setVisible(true) ;
            return ;
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
            OptionsDialog op = new OptionsDialog(this,11) ;
            op.setVisible(true) ;
            return ;
         }

         // The URL text entry causes a switch to a new page.

         if (source == enterurl)
         {
            if (activated) return ;
            GetLinks.stopsearch() ;
            ValidateLinks.stopsearch() ;
            BuildForm.stopsearch() ;
            Scheduler.stopScheduler() ;
            stop = false ;
            activated = true ;
            String s = evt.getActionCommand() ;
            s = resolveHex(s) ;
            setPage(s) ;
         }

         // A timer request from a WindowClosing activity.  If the timer
         // expires then the close activity may have failed.

         if (source instanceof Timer)
         {
            if (!activated) return ;
            int i = JOptionPane.showConfirmDialog(this,
               "Search has not terminated.  Force termination?",
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.YES_NO_OPTION) ;
            if (i == JOptionPane.YES_OPTION)
               close() ;
            else
               ((Timer) source).start();
         }

         // The callback when URL's are determined.

         if ("WebFrame URL Callback".equals(evt.getActionCommand()))
         {
            if (waitbox != null) { waitbox.close() ; waitbox = null ; }
            if (terminating) { close() ; return ; }
            Vector v = GetLinks.getArchives() ;
            if (v.size() > 0 && !stop)
            {
               addTrace("Begin Archive Validation",1) ;
               ValidateLinks vl = new ValidateLinks(this,v) ;
               Thread thread = new Thread(vl) ;
               thread.start() ;
            }
            else
               exitsearch("No archive files located") ;
         }

         // The callback when validation is finished.

         if ("WebFrame Val Callback".equals(evt.getActionCommand()))
         {
            if (waitbox != null) { waitbox.close() ; waitbox = null ; }
            if (terminating) { close() ; return ; }
            Vector v = ValidateLinks.getSetState() ;
            if (v.size() > 0)
            {
               addTrace("Begin HTML Form Generation",1) ;
               BuildForm bf = new BuildForm(this,v) ;
               Thread thread = new Thread(bf) ;
               thread.start() ;
            }
            else
               exitsearch("No KiSS archive files found") ;
         }

         // The callback when the HTML form is written.
         // Change the file extension to HTML.

         if ("WebFrame Bld Callback".equals(evt.getActionCommand()))
         {
            if (waitbox != null) { waitbox.close() ; waitbox = null ; }
            if (terminating) { close() ; return ; }
            String s2 = null ;
            String s1 = getBaseLocation() ;
            String formname = BuildForm.getFormName() ;
            String formtitle = GetLinks.getTitle() ;
            if (formtitle.length() == 0) formtitle = s1 ;
            if (s1.startsWith("http://")) s1 = s1.substring(7) ;
            if (s1.startsWith("https://")) s1 = s1.substring(8) ;
            if (s1.startsWith("file:///")) s1 = s1.substring(11) ;
            if (s1.startsWith("file://")) s1 = s1.substring(10) ;
            if (s1.startsWith("file:/")) s1 = s1.substring(9) ;
            s1 = s1.replace('/','.') ;
            if (s1.endsWith(".")) s1 = s1.substring(0,s1.length()-1) ;
            if (s1.endsWith(".html") || s1.endsWith(".htm"))
               s1 = s1.substring(0,s1.lastIndexOf('.')) ;
            s2 = s1 + ".html";
            s1 += ".txt" ;
               
            try
            {
               String directory = OptionsDialog.getHtmlDirectory() ;
               directory = convertSeparator(directory) ;
               if (!directory.endsWith(File.separator)) directory += File.separator ;
               File f1 = new File(directory,s1) ;
               File f2 = new File(directory,s2) ;
               if (f2.exists()) f2.delete() ;
               f1.renameTo(f2) ;
               if (OptionsDialog.getDebugSearch())
                  PrintLn.println("WebSearch: rename " + f1.getName() + " to " + f2.getPath()) ;
               formname = f2.getName() ;
               absoluteformname = f2.getAbsolutePath() ;
            }
            catch (Exception e)
            {
               PrintLn.println("WebSearch: rename " + s1 + " to " + s2 + " failed.") ;
            }
               
            addTrace("Begin Index Form Generation",1) ;
            BuildIndex bi = new BuildIndex(this,formtitle,formname,BuildForm.getFormSize()) ;
            bi.parse() ;
            bi.buildform() ;
         }

         // The callback when the HTML index is written.

         if ("WebFrame Idx Callback".equals(evt.getActionCommand()))
         {
            if (waitbox != null) { waitbox.close() ; waitbox = null ; }
            if (terminating) { close() ; return ; }
            String s1 = OptionsDialog.getKissIndex() ;
            s1 = convertSeparator(s1) ;
            File f2 = new File(s1) ;
            s1 = f2.getName() ;
            if (s1.endsWith(".html") || s1.endsWith(".htm"))
            {
               s1 = s1.substring(0,s1.lastIndexOf('.')) ;
               s1 += ".txt" ;
            }
            try
            {
               String directory = f2.getParent() ;
               File f1 = new File(directory,s1) ;
               if (f2.exists()) f2.delete() ;
               f1.renameTo(f2) ;
               if (OptionsDialog.getDebugSearch())
                  PrintLn.println("WebSearch: rename " + f1.getName() + " to " + f2.getPath()) ;
            }
            catch (Exception e)
            {
               PrintLn.println("WebSearch: rename " + s1 + " to " + f2.getPath() + " failed.") ;
            }
               
            showStatus("") ;
            activated = false ;
            activebtn.setEnabled(false) ;
            PrintLn.println("WebSearch: ends") ;
            addTrace("End WebSearch",1) ;
            Scheduler.stopScheduler() ;
            scheduler = null ;
               
            // Activate a Portal.  When this search form is closed
            // a new invocation of the Kisekae program is started.
            // The web portal is opened in a new thread after a 2
            // second delay.
            
            if (absoluteformname != null)
            {
               int i = JOptionPane.showConfirmDialog(this,
                  "Launch the UltraKiss Portal?",
                  "Search Complete",
                  JOptionPane.YES_NO_OPTION) ;
               if (i == JOptionPane.YES_OPTION)
               {
                  Runnable showportal = new Runnable()
                  {
                     public void run()
                     {
                        try { Thread.sleep(1000) ; }
                        catch (InterruptedException e) { }
                        
               			Runnable awt = new Runnable()
               			{ 
                           public void run() 
                           { 
                              String s = absoluteformname ;
                              if (!s.startsWith(File.separator)) s = File.separator + s ;
                              s = ("file://"+s).replace('\\','/') ;
                              MainFrame mf = Kisekae.getMainFrame() ;
                              WebFrame.setCurrentWeb(s) ;
                              WebFrame wf = new WebFrame(mf) ; 
                              wf.setVisible(true) ;
                              wf.toFront() ;
                           } 
                        } ;
               			SwingUtilities.invokeLater(awt) ;
                     }
                  } ;
                  new Thread(showportal).start() ;
                  close() ;
               }
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
         PrintLn.println("WebSearchFrame: Out of memory.") ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            "Insufficiant memory.  Action not completed.",
            "Low Memory Fault", JOptionPane.ERROR_MESSAGE) ;
      }

      // Watch for internal faults during action events.

      catch (Throwable e)
      {
         PrintLn.println("WebFrame: Internal fault, action " + evt.getActionCommand()) ;
         e.printStackTrace() ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            "Internal fault.  Action not completed." + "\n" + e.toString(),
            "Internal Fault", JOptionPane.ERROR_MESSAGE) ;
      }
   }


   // Window Events

   public void windowOpened(WindowEvent evt) { }
   public void windowClosed(WindowEvent evt) { }
   public void windowIconified(WindowEvent evt) { }
   public void windowDeiconified(WindowEvent evt) { }
   public void windowActivated(WindowEvent evt) { }
   public void windowDeactivated(WindowEvent evt) { }
   public void windowClosing(WindowEvent evt) { close() ; }



   // Close this main window frame.  This terminates the Search.
   // We need to interrupt any validation load in progress.  

   public void close()
   {
      if (!activated || terminating)
      {
         if (timer != null) timer.stop() ;
         statusbar.setStatusBar(false) ;
         activated = false ;
		   super.close() ;
         dispose() ;
         Kisekae.getKisekae().close() ;
         return ;
      }
        
      // If we are running, cancel execution.  This can take some time.
       
      stop = true ;
      terminating = true ;
      showStatus("Terminating ...") ;
      GetLinks.stopsearch() ;
      ValidateLinks.stopsearch() ;
      BuildForm.stopsearch() ;
      Scheduler.stopScheduler() ;
      activebtn.setEnabled(false) ;
      scheduler = null ;

      timer = new Timer(5000,this) ;
      timer.setRepeats(false) ;
      timer.start() ;
      waitbox = new WaitDialog(this,"Search terminating, please wait ...") ;
      waitbox.setVisible(true) ;
   }
     
     
   // Function to exit the search.
     
   private void exitsearch(String s) 
   {
      addTrace(s,3) ;
      showStatus("") ;
      activated = false ;
      activebtn.setEnabled(false) ;
      PrintLn.println("WebSearch: ends") ;
      addTrace("End WebSearch",1) ;
      Scheduler.stopScheduler() ;
      scheduler = null ;
   }
     
     
  // Function to exit the search.
     
  private String resolveHex(String s) 
  {
     if (s == null) return null ;
     int n = s.indexOf('%') ;
     while (n >= 0)
     {
        if (s.substring(n).startsWith("%7")) 
           s = s.substring(0,n) + "~" + s.substring(n+2) ;
        if (s.substring(n).startsWith("%20")) 
           s = s.substring(0,n) + " " + s.substring(n+3) ;
     }
     return s ;
   }
     
     
   // Inner class for a Status Bar
     
   /**
   * StatusBar Class
   *
   * Purpose:
   *
   * This object encapsulates the main program status bar.  It is a
   * panel that resides in the main frame window.
   *
   */

   class StatusBar extends JPanel
   {
   	boolean statusBarOn = false ;             // Status bar view state
      private Thread thread = null ;            // Periodic mem update
      private int period = 5000 ;               // Update period

      // Status bar attributes

      private Frame parent = null ;				   // The parent frame

      // User interface components

      private BorderLayout borderLayout1 = new BorderLayout();
      private JLabel statuslabel = new JLabel();
      private JLabel memlabel = new JLabel();


      // Constructor

      public StatusBar(Frame parent)
      {
         this.parent = parent ;
         try { jbInit() ; }
         catch(Exception e)
         { 
            PrintLn.println("WebSearchFrame: jbInit StatusBar constructor " + e.toString()) ;
            e.printStackTrace() ; 
         }
      }


      // User interface initialization.

      private void jbInit() throws Exception
      {
         this.setLayout(borderLayout1);
         statuslabel.setBorder(BorderFactory.createLoweredBevelBorder());
         memlabel.setBorder(BorderFactory.createLoweredBevelBorder());
         memlabel.setText("Mem:       0K");
         memlabel.addMouseListener(new java.awt.event.MouseAdapter()
         {
            public void mouseClicked(MouseEvent e)
            { memlabel_mouseClicked(e); }
         });
         this.add(statuslabel, BorderLayout.CENTER);
         this.add(memlabel, BorderLayout.EAST);
      }


      // Method to display a status message.

      void showStatus(String s)
      {
         if (!statusBarOn) return ;
         statuslabel.setText(new String(s)) ;
         repaint() ;
      }


      // Method to update our memory display.

      private int showMem()
      {
         if (!statusBarOn) return -1 ;
         long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
         memlabel.setText("Mem: " + (int) (mem/1024) + "K") ;
         repaint() ;
         return (int) (mem/1024) ;
      }


      // Method to set the status bar on.

      void setStatusBar(boolean state)
      {
         statusBarOn = state ;

         // If the bar is on, start the memory indicator update thread.

         if (state)
         {
            thread = new Thread()
            {
               public void run()
               {
                  Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                  while (true)
                  {
                     try
                     {
                        if (showMem() < 0 && thread != null) thread.interrupt() ;
                        sleep(period) ;
                     }
                     catch (InterruptedException e) { break ; }
                  }
               }
            } ;
            thread.start() ;
         }

         // If the bar is off, stop the memory bar update thread.

         if (!state)
         {
            if (thread != null) thread.interrupt() ;
            thread = null ;
         }
      }

      // Update memory if user clicks on memory label.   Note that this
      // event can be held if a garbage collection is currently in progress.

      void memlabel_mouseClicked(MouseEvent e) { showMem() ; }
   }
  
  
   // Inner Class to define a wait dialog
  
   class WaitDialog extends JDialog
   {
      private String message ;
      private Frame parent ;
     
      public WaitDialog(Frame owner,String s)
      {
         super(owner) ;
         message = s ;
         parent = owner ;
         init() ;
      }
     
      private void init() 
      {
         JPanel panel1 = new JPanel() ;
         JLabel label1 = new JLabel(message) ;
         panel1.setLayout(new BorderLayout()) ;
         panel1.add(label1,BorderLayout.CENTER) ;
         panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         getContentPane().add(panel1) ;
         pack() ;

         // Center the frame in the panel space.

         Dimension s = getSize() ;
         Dimension d = parent.getSize() ;
         int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
         int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
         setLocation(x,y) ;
      }
      
      void close()
      {
         dispose() ;
      }
   }
}
