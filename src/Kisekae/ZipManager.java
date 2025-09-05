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
* ZipManager Class
*
* Purpose:
*
* This object is a generalized compressed file manager.  It is used to view
* and manipulate the contents of ZIP, LZH and JAR archive files.
*
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Collections ;
import java.util.zip.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.* ;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.* ;


public class ZipManager extends KissFrame
	implements ActionListener, WindowListener, FilenameFilter
{
	// Class attributes

	private static String helpset = "Help/ArchiveManager.hs" ;
	private static String helpsection = "archivemanager.index" ;
	private static String onlinehelp = "archivemanager/index.html" ;
	private static String dirname = null ;				// Last accessed directory
   private static URL zipFileURL = null ;				// URL of open file
   private static final int UNSORTED = 0 ;
   private static final int NAMESORT = 1 ;
   private static final int TIMESORT = 2 ;
   private static final int SIZESORT = 3 ;
	private static final int RATIOSORT = 4 ;
   private static final int PACKEDSORT = 5 ;
   private static final int PATHSORT = 6 ;

	// File attributes

	private VariableTableData tabledata = null ;		// The table data object
	private String filename = null ;						// File name
	private String elementname = null ;					// Element name
	private String pathname = null ;						// Full path name
	private String extension = null ;					// File extension
	private ArchiveFile zip = null ;						// ZIP file object
	private ArchiveEntry ze = null ;			 			// ZIP file entry
   private FileOpen fileopen = null ;					// Current fileopen object
   private int sortcolumn = 0 ; 					  		// The column to sort
	private boolean ascending = true ;			  		// The sort direction
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

   // File writer callback variables

   private FileWriter filewriter = null ;				// Current filewriter object
   private String savecallback = null ;				// Method to call on file save
   private boolean callreturn = false ; 				// True if callback processed

   // Help set objects

	private HelpLoader helper = null ;
   private AboutBox aboutdialog = null ;

	// Menu items

   private JMenu fileMenu ;
   private JMenu actionMenu ;
   private JMenu optionsMenu ;
   private JMenu windowMenu ;
   private JMenu helpMenu ;
	private JMenuItem newfile ;
	private JMenuItem openfile ;
	private JMenuItem openurl ;
	private JMenuItem closefile ;
	private JMenuItem copyfile ;
	private JMenuItem renamefile ;
	private JMenuItem deletefile ;
	private JMenuItem addaction ;
   private JMenuItem deleteaction ;
   private JMenuItem extractaction ;
   private JMenuItem viewaction ;
   private JMenuItem selectaction ;
   private JMenuItem unselectaction ;
   private JMenuItem findaction ;
   private JCheckBoxMenuItem sortnosort ;
   private JCheckBoxMenuItem sortname ;
   private JCheckBoxMenuItem sortsize ;
   private JCheckBoxMenuItem sorttime ;
   private JCheckBoxMenuItem sortratio ;
   private JCheckBoxMenuItem sortpacked ;
   private JCheckBoxMenuItem sortpath ;
   private JCheckBoxMenuItem viewastext ;
   private ButtonGroup sortgroup ;
	private JMenuItem exit ;
	private JMenuItem help ;
	private JMenuItem logfile ;
	private JMenuItem about ;
   private Insets insets = null ;

	// User interface objects

	private JToolBar jToolBar1 = new JToolBar();
	private JScrollPane scroll = new JScrollPane();
	private JTable TABLE = new JTable();
	private JButton NEW = new JButton();
	private JButton OPEN = new JButton();
	private JButton CLOSE = new JButton();
	private JButton FIND = new JButton();
	private JButton ADD = new JButton();
	private JButton EXTRACT = new JButton();
	private JButton DELETE = new JButton();
	private JButton VIEW = new JButton();
	private JPanel jPanel1 = new JPanel();
	private JLabel statuslabel = new JLabel();
	private JLabel runlabel = new JLabel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JLabel heading = new JLabel();
   private Border eb1 = BorderFactory.createEmptyBorder(10,0,10,0) ;
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout5 = new BorderLayout();

	// Create specialized listeners for events.

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
      	deleteaction.setEnabled(true) ;
      	extractaction.setEnabled(true) ;
      	viewaction.setEnabled(true) ;
      	unselectaction.setEnabled(true) ;
      	findaction.setEnabled(true) ;
      	CLOSE.setEnabled(true) ;
      	EXTRACT.setEnabled(true) ;
      	DELETE.setEnabled(true) ;
      	VIEW.setEnabled(true) ;
      	FIND.setEnabled(true) ;
      }
	} ;

	MouseListener mouseListListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
         	if (tabledata == null) return ;
		     	int row = TABLE.getSelectedRow() ;
	         Object o = tabledata.getValueAt(row,0) ;
            if (!(o instanceof String)) return ;
	         String filename = (String) o ;
				o = tabledata.getValueAt(row,5) ;
	         String dir = (o instanceof String) ? o.toString() : "" ;
            if ("".equals(dir)) dir = null ;
	         File f = new File(dir,filename) ;
	         viewElement(f.getPath()) ;
			}
		}
	} ;

   MouseListener columnListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
      	if (TABLE == null) return ;
         if (tabledata == null) return ;
        	TableColumnModel columnmodel = TABLE.getColumnModel() ;
         int columnindex = columnmodel.getColumnIndexAtX(e.getX()) ;
         int modelindex = columnmodel.getColumn(columnindex).getModelIndex() + 1 ;

         if (modelindex <= 0) return ;
         if (sortcolumn == modelindex)
         	ascending = !ascending ;
         else
           	sortcolumn = modelindex ;

         setList() ;
      }
   } ;

	KeyListener keyListener = new KeyListener()
   {
      public void keyReleased(KeyEvent e) { }
      public void keyTyped(KeyEvent e) { }
		public void keyPressed(KeyEvent e)
      {
      	if (e.getKeyChar() != KeyEvent.VK_DELETE) return ;
         deleteElements() ;
      }
	} ;


   // Constructor

	public ZipManager() 
   { 
		super(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
      init() ;
   }


   // Constructor to open a named archive file.

	public ZipManager(ArchiveFile zip)
	{
		super(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
      init() ;
      if (zip == null) return ;
      if (!zip.isArchive()) return ;
      openArchive(zip.getName()) ;
      closeArchiveFile() ;
   }


   // Constructor to create a new archive preset to the CNF files.

	public ZipManager(KissObject kiss)
	{
		super(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
      init() ;
      createArchive() ;
      if (zip == null) return ;

      // Establish the compression method and all CNF contents.
      
      int method = 0 ;
      String name = zip.getFileName() ;
		int n = (name != null) ? name.lastIndexOf('.') : -1 ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".zip".equals(ext)) method = ZipEntry.DEFLATED ;
		if (".jar".equals(ext)) method = ZipEntry.DEFLATED ;
		if (".lzh".equals(ext)) method = LhaEntry.LH5 ;
      
      // Open the configuration zip file for reading.
      
      ArchiveFile configzip = null ;
      Vector contents = new Vector() ;
      if (kiss instanceof Configuration)
         configzip = ((Configuration) kiss).getZipFile() ;
      if (configzip != null)
      {
         try
         {
            boolean zipopen = configzip.isOpen() ;
            if (!zipopen) configzip.open() ; 
            contents = getContents(kiss,configzip,method) ;
         }
         catch (IOException e) { }
      }
      
      // Write the files to the archive.

      setBusy() ;
      savecallback = "add" ;
      filewriter = new FileWriter(this,zip,contents,true) ;
		filewriter.callback.addActionListener(this) ;
		Thread thread = new Thread(filewriter) ;
      thread.start() ;
  }


   // Constructor to open a CNF in an archive for extract to a directory.

	public ZipManager(ArchiveFile zip, KissObject kiss)
	{
		super(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
      init() ;
      if (zip == null) return ;
      if (!zip.isArchive()) return ;
      openArchive(zip.getName()) ;
      closeArchiveFile() ;

      // Establish the compression method and all CNF contents.
      
      int method = 0 ;
      String name = zip.getFileName() ;
		int n = (name != null) ? name.lastIndexOf('.') : -1 ;
		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
		if (".zip".equals(ext)) method = ZipEntry.STORED ;
		if (".jar".equals(ext)) method = ZipEntry.STORED ;
		if (".lzh".equals(ext)) method = LhaEntry.LH0 ;
      
      // Open the configuration zip file for reading.
      
      Vector contents = new Vector() ;
      try
      {
         boolean zipopen = zip.isOpen() ;
         if (!zipopen) zip.open() ; 
         contents = getContents(kiss,zip,method) ;
         if (!zipopen) zip.close() ;
      }
      catch (IOException e) { }
      
      // Select all the CNF items in the archive.
      
      ListSelectionModel selectionmodel = TABLE.getSelectionModel() ;
      VariableTableData model = (VariableTableData) TABLE.getModel() ;
      for (int i = 0 ; i < contents.size() ; i++)
      {
         ArchiveEntry ze = (ArchiveEntry) contents.elementAt(i) ;
         String elementname = ze.getPath() ;
         int row = model.findRow(elementname) ;
         if (row < 0) continue ;
         if (selectionmodel != null) 
            selectionmodel.addSelectionInterval(row,row) ;
      }
      TABLE.setSelectionModel(selectionmodel) ;
      
      // Show the dialog to extract the selected elements.
      
      extractArchive() ;
    }


   // Frame initialization.

   private void init()
   {
      aboutdialog = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
     	setIconImage(Kisekae.getIconImage()) ;
      boolean applemac = OptionsDialog.getAppleMac() ;

      // Construct the user interface.

		try { jbInit() ; pack() ; }
		catch(Exception ex)
		{ 
         PrintLn.println("ZipManager: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace(); 
      }

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;

		// Set up the menu bar and create the File menu.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
      if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		String s = System.getProperty("java.version") ;
		int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
		insets = new Insets(2,2,2,rm) ;
      fileMenu.setMargin(insets) ;
		fileMenu.add((newfile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNew")))) ;
      if (!applemac)newfile.setMnemonic(KeyEvent.VK_N) ;
		newfile.addActionListener(this) ;
      newfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelerator));
		fileMenu.add((openfile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
      if (!applemac) openfile.setMnemonic(KeyEvent.VK_O) ;
		openfile.addActionListener(this) ;
      openfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelerator));
		fileMenu.add((openurl = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpenURL")))) ;
		openurl.addActionListener(this) ;
      openurl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelerator+ActionEvent.SHIFT_MASK));
      openurl.setMnemonic(KeyEvent.VK_R) ;
      openurl.setEnabled(false) ;
      if (!applemac) openurl.setVisible(false) ;  // remove this function for now
		fileMenu.add((closefile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClose")))) ;
      if (!applemac) closefile.setMnemonic(KeyEvent.VK_C) ;
		closefile.addActionListener(this) ;
      closefile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, accelerator));
      closefile.setEnabled(false) ;
		fileMenu.addSeparator() ;
		fileMenu.add((copyfile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileCopy")))) ;
      if (!applemac) copyfile.setMnemonic(KeyEvent.VK_P) ;
		copyfile.addActionListener(this) ;
      copyfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelerator));
		copyfile.setEnabled(false) ;
		fileMenu.add((renamefile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileRename")))) ;
      if (!applemac) renamefile.setMnemonic(KeyEvent.VK_R) ;
		renamefile.addActionListener(this) ;
      renamefile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelerator));
		renamefile.setEnabled(false) ;
		fileMenu.add((deletefile = new JMenuItem(Kisekae.getCaptions().getString("MenuFileDelete")))) ;
      if (!applemac) deletefile.setMnemonic(KeyEvent.VK_D) ;
		deletefile.addActionListener(this) ;
		deletefile.setEnabled(false) ;
      deletefile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelerator));
		fileMenu.addSeparator() ;
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuitArchive" : "MenuFileExitArchive" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
      if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;

		// Create the Edit menu.

		actionMenu = new JMenu(Kisekae.getCaptions().getString("MenuEdit")) ;
      if (!applemac) actionMenu.setMnemonic(KeyEvent.VK_E);
      actionMenu.setMargin(insets) ;
		actionMenu.add((addaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditAdd")))) ;
      if (!applemac) addaction.setMnemonic(KeyEvent.VK_A) ;
		addaction.addActionListener(this) ;
      addaction.setEnabled(false) ;
		addaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0)) ;
		actionMenu.add((extractaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditExtract")))) ;
      if (!applemac) extractaction.setMnemonic(KeyEvent.VK_E) ;
		extractaction.addActionListener(this) ;
      extractaction.setEnabled(false) ;
		extractaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0)) ;
		actionMenu.add((deleteaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditDelete")))) ;
      if (!applemac) deleteaction.setMnemonic(KeyEvent.VK_D) ;
		deleteaction.addActionListener(this) ;
      deleteaction.setEnabled(false) ;
		deleteaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0)) ;
		actionMenu.add((viewaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditView")))) ;
      if (!applemac) viewaction.setMnemonic(KeyEvent.VK_V) ;
		viewaction.addActionListener(this) ;
      viewaction.setEnabled(false) ;
		viewaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0)) ;
		actionMenu.addSeparator() ;
		actionMenu.add((findaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditFind")))) ;
      if (!applemac) findaction.setMnemonic(KeyEvent.VK_F) ;
		findaction.addActionListener(this) ;
     	findaction.setEnabled(false) ;
		findaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0)) ;
		actionMenu.add((selectaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectAll")))) ;
      if (!applemac) selectaction.setMnemonic(KeyEvent.VK_L) ;
		selectaction.addActionListener(this) ;
      selectaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator));
     	selectaction.setEnabled(false) ;
		actionMenu.add((unselectaction = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUnselectAll")))) ;
      if (!applemac) unselectaction.setMnemonic(KeyEvent.VK_U) ;
		unselectaction.addActionListener(this) ;
      unselectaction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator+ActionEvent.SHIFT_MASK));
     	unselectaction.setEnabled(false);
		mb.add(actionMenu) ;

		// Create the Options menu.

		optionsMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
      if (!applemac) optionsMenu.setMnemonic(KeyEvent.VK_O);
      optionsMenu.setMargin(insets) ;
		JMenu sortMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptionsSort")) ;
		optionsMenu.add(sortMenu) ;
      if (!applemac) sortMenu.setMnemonic(KeyEvent.VK_S) ;
		sortMenu.add((sortnosort = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveUnsorted")))) ;
      if (!applemac) sortnosort.setMnemonic(KeyEvent.VK_U) ;
		sortnosort.addActionListener(this) ;
		sortMenu.add((sortname = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldName")))) ;
      if (!applemac) sortname.setMnemonic(KeyEvent.VK_N) ;
		sortname.addActionListener(this) ;
		sortMenu.add((sorttime = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldDate")))) ;
      if (!applemac) sorttime.setMnemonic(KeyEvent.VK_T) ;
		sorttime.addActionListener(this) ;
		sortMenu.add((sortsize = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldSize")))) ;
      if (!applemac) sortsize.setMnemonic(KeyEvent.VK_S) ;
		sortsize.addActionListener(this) ;
		sortMenu.add((sortratio = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldRatio")))) ;
      if (!applemac) sortratio.setMnemonic(KeyEvent.VK_R) ;
		sortratio.addActionListener(this) ;
		sortMenu.add((sortpacked = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldPacked")))) ;
      if (!applemac) sortpacked.setMnemonic(KeyEvent.VK_A) ;
		sortpacked.addActionListener(this) ;
		sortMenu.add((sortpath = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("ArchiveFieldPath")))) ;
      if (!applemac) sortpath.setMnemonic(KeyEvent.VK_P) ;
		sortpath.addActionListener(this) ;
	   sortgroup = new ButtonGroup() ;
      sortgroup.add(sortnosort) ;
      sortgroup.add(sortname) ;
      sortgroup.add(sorttime) ;
      sortgroup.add(sortsize) ;
      sortgroup.add(sortratio) ;
      sortgroup.add(sortpacked) ;
      sortgroup.add(sortpath) ;
		optionsMenu.addSeparator() ;
		optionsMenu.add((viewastext = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewAsText")))) ;
      if (!applemac) viewastext.setMnemonic(KeyEvent.VK_T) ;
		viewastext.addActionListener(this) ;
		mb.add(optionsMenu) ;

		// Create the Window menu.

		windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		windowMenu.setMargin(insets) ;
      if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
		mb.add(windowMenu) ;

		// Create the Help menu.

		helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
      if (!applemac) helpMenu.setMnemonic(KeyEvent.VK_H);
      helpMenu.setMargin(insets) ;
		mb.add(helpMenu) ;
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
         logfile.setEnabled(LogFile.isOpen()) ;
         logfile.addActionListener(menu) ;
         if (!applemac) logfile.setMnemonic(KeyEvent.VK_L) ;
         logfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, accelerator+ActionEvent.SHIFT_MASK));
      }
		helpMenu.addSeparator() ;
		helpMenu.add((about = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpAbout")))) ;
      if (!applemac) about.setMnemonic(KeyEvent.VK_A) ;
		about.addActionListener(this) ;
		setJMenuBar(mb) ;

      // Create the TABLE.

   	tabledata = new VariableTableData() ;
		TABLE.setAutoCreateColumnsFromModel(false) ;
      TABLE.setShowHorizontalLines(false) ;
      TABLE.setShowVerticalLines(false) ;
      TABLE.setShowGrid(false) ;
		TABLE.setModel(tabledata) ;

		// Add new table columns.

		for (int i = 0 ; i < tabledata.columns.length ; i++)
		{
      	DefaultTableCellRenderer r = new DefaultTableCellRenderer() ;
         r.setHorizontalAlignment(tabledata.columns[i].alignment) ;
         TableColumn column = new TableColumn(i,tabledata.columns[i].width,r,null) ;
         TABLE.addColumn(column) ;
      }

      JTableHeader header = TABLE.getTableHeader() ;
		header.setUpdateTableInRealTime(true) ;
		header.addMouseListener(columnListener) ;

      // Set the frame size and miscellaneous initial items.

      clearBusy() ;
      statuslabel.setText(Kisekae.getCopyright());
      sortcolumn = UNSORTED ;
      sortnosort.setState(true) ;
      super.open() ;
      if (helper != null) helper.setSize(getSize());

		// Register for events.

		NEW.addActionListener(this) ;
		OPEN.addActionListener(this) ;
		CLOSE.addActionListener(this) ;
		FIND.addActionListener(this) ;
		ADD.addActionListener(this) ;
		EXTRACT.addActionListener(this) ;
		DELETE.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		TABLE.addMouseListener(mouseListListener) ;
      TABLE.addKeyListener(keyListener) ;
      ListSelectionModel m = TABLE.getSelectionModel() ;
		m.addListSelectionListener(listListener) ;
		addWindowListener(this) ;
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
      String ext = ".gif" ;
      if (OptionsDialog.getAppleMac()) ext = ".png" ;
		URL iconfile = null ;
		NEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipNew"));
		NEW.setAlignmentY(0.5f) ;
		NEW.setMaximumSize(new Dimension(110, 27));
		NEW.setText(Kisekae.getCaptions().getString("NewMessage"));
		iconfile = Kisekae.getResource("Images/new" + ext) ;
		if (iconfile != null) NEW.setIcon(new ImageIcon(iconfile)) ;
		OPEN.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpen"));
		OPEN.setAlignmentY(0.5f) ;
		OPEN.setMaximumSize(new Dimension(110, 27));
		OPEN.setText(Kisekae.getCaptions().getString("OpenMessage"));
		iconfile = Kisekae.getResource("Images/open" + ext) ;
		if (iconfile != null) OPEN.setIcon(new ImageIcon(iconfile)) ;
		CLOSE.setEnabled(false);
		CLOSE.setAlignmentY(0.5f) ;
		CLOSE.setMaximumSize(new Dimension(110, 27));
		CLOSE.setToolTipText(Kisekae.getCaptions().getString("ToolTipClose"));
		CLOSE.setText(Kisekae.getCaptions().getString("CloseMessage"));
		iconfile = Kisekae.getResource("Images/close" + ext) ;
		if (iconfile != null) CLOSE.setIcon(new ImageIcon(iconfile)) ;
		FIND.setEnabled(false);
		FIND.setAlignmentY(0.5f) ;
		FIND.setMaximumSize(new Dimension(110, 27));
		FIND.setToolTipText(Kisekae.getCaptions().getString("ToolTipFind"));
		FIND.setText(Kisekae.getCaptions().getString("FindMessage"));
		iconfile = Kisekae.getResource("Images/find" + ext) ;
		if (iconfile != null) FIND.setIcon(new ImageIcon(iconfile)) ;
		ADD.setEnabled(false);
		ADD.setAlignmentY(0.5f) ;
		ADD.setMaximumSize(new Dimension(110, 27));
		ADD.setToolTipText(Kisekae.getCaptions().getString("ToolTipAdd"));
		ADD.setText(Kisekae.getCaptions().getString("AddMessage"));
		iconfile = Kisekae.getResource("Images/add" + ext) ;
		if (iconfile != null) ADD.setIcon(new ImageIcon(iconfile)) ;
		EXTRACT.setEnabled(false);
		EXTRACT.setAlignmentY(0.5f) ;
		EXTRACT.setMaximumSize(new Dimension(110, 27));
		EXTRACT.setToolTipText(Kisekae.getCaptions().getString("ToolTipExtract"));
		EXTRACT.setText(Kisekae.getCaptions().getString("ExtractMessage"));
		iconfile = Kisekae.getResource("Images/extract" + ext) ;
		if (iconfile != null) EXTRACT.setIcon(new ImageIcon(iconfile)) ;
		DELETE.setEnabled(false);
		DELETE.setAlignmentY(0.5f) ;
		DELETE.setMaximumSize(new Dimension(110, 27));
		DELETE.setToolTipText(Kisekae.getCaptions().getString("ToolTipDelete"));
		DELETE.setText(Kisekae.getCaptions().getString("DeleteMessage"));
		iconfile = Kisekae.getResource("Images/delete" + ext) ;
		if (iconfile != null) DELETE.setIcon(new ImageIcon(iconfile)) ;
		VIEW.setEnabled(false);
		VIEW.setAlignmentY(0.5f) ;
		VIEW.setMaximumSize(new Dimension(110, 27));
		VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipView"));
		VIEW.setText(Kisekae.getCaptions().getString("ViewMessage"));
		iconfile = Kisekae.getResource("Images/view" + ext) ;
		if (iconfile != null) VIEW.setIcon(new ImageIcon(iconfile)) ;

      heading.setBorder(eb1);
		statuslabel.setText(Kisekae.getCaptions().getString("StatusLabelText"));
		jPanel1.setLayout(borderLayout1);
		jPanel2.setLayout(borderLayout2);
		jPanel3.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel3.setLayout(borderLayout3);
		jPanel4.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanel4.setLayout(borderLayout4);
		jPanel5.setLayout(borderLayout5);
		jToolBar1.setFloatable(false);

		this.getContentPane().add(jToolBar1, BorderLayout.NORTH);
		jToolBar1.add(NEW, null);
		jToolBar1.add(OPEN, null);
		jToolBar1.add(CLOSE, null);
		jToolBar1.add(FIND, null);
		jToolBar1.add(ADD, null);
		jToolBar1.add(EXTRACT, null);
		jToolBar1.add(DELETE, null);
		jToolBar1.add(VIEW, null);

		this.getContentPane().add(jPanel1, BorderLayout.SOUTH) ;
		jPanel1.add(jPanel3, BorderLayout.CENTER) ;
		jPanel3.add(statuslabel, BorderLayout.CENTER) ;
		jPanel1.add(jPanel4, BorderLayout.EAST) ;
		jPanel4.add(runlabel, BorderLayout.CENTER) ;

		this.getContentPane().add(jPanel2, BorderLayout.CENTER) ;
      int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
      int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
      scroll.setVerticalScrollBarPolicy(vspolicy) ;
      scroll.setHorizontalScrollBarPolicy(hspolicy) ;			
		jPanel2.add(scroll, BorderLayout.CENTER) ;
		scroll.getViewport().add(TABLE, null) ;
      jPanel2.add(heading,BorderLayout.NORTH) ;
	}


   // Method to create a new archive.

	private void createArchive() { createArchive(null) ; }
   private void createArchive(String path)
	{
   	pathname = selectArchive(path,
         Kisekae.getCaptions().getString("CreateArchiveTitle"),
         Kisekae.getCaptions().getString("NewMessage")) ;
      if (pathname == null) return ;

      // Check to see if the file already exists.  If it does, delete it.
      // The deletion was confirmed in selectArchive call.

      File fsb = new File(pathname) ;
      if (fsb.exists() && !fsb.delete()) 
      {
         JOptionPane.showMessageDialog(this,
         	pathname + "\n" +
            Kisekae.getCaptions().getString("FileDeleteError"),
            Kisekae.getCaptions().getString("DeleteArchiveTitle"),
            JOptionPane.ERROR_MESSAGE);
         return ;
      }

      // Update our frame title and menu actions.

      closeArchive() ;
      String s = getTitle() ;
      if (filename != null) s += " - " + filename ;
		setTitle(s) ;
      s = Kisekae.getCaptions().getString("NewArchiveTitle") ;
      statuslabel.setText(s + " " + filename.toUpperCase());
      addaction.setEnabled(true);
      closefile.setEnabled(true) ;
      deletefile.setEnabled(true) ;
      copyfile.setEnabled(true) ;
      renamefile.setEnabled(true) ;
     	deleteaction.setEnabled(false);
     	extractaction.setEnabled(false);
     	viewaction.setEnabled(false);
     	selectaction.setEnabled(false);
     	unselectaction.setEnabled(false);
     	findaction.setEnabled(false) ;
      CLOSE.setEnabled(true) ;
      ADD.setEnabled(true);
      EXTRACT.setEnabled(false) ;
      DELETE.setEnabled(false) ;
      VIEW.setEnabled(false) ;
     	FIND.setEnabled(false) ;

      // Create the new archive.  We construct a FileOpen object to
      // maintain consistency with our file writer routine.

      try
      {
         s = Kisekae.getCaptions().getString("CreateArchiveTitle") ;
         statuslabel.setText(s + " " + filename.toUpperCase());
         fileopen = new FileOpen(this,pathname,"rw") ;
         String name = pathname ;
   		int n = (name != null) ? name.lastIndexOf('.') : -1 ;
   		String ext = (n < 0) ? "" : name.substring(n).toLowerCase() ;
   		if (".zip".equals(ext)) 
            zip = new PkzFile(fileopen,pathname,"rw") ;
         else if (".lzh".equals(ext)) 
            zip = new LhaFile(fileopen,pathname,"rw") ;
         else 
            zip = new DirFile(fileopen,pathname) ;
      }
      catch (IOException e)
		{
         PrintLn.println("ZipManager: Archive file creation exception, " + e.getMessage()) ;
			JOptionPane.showMessageDialog(this, e.getMessage(),
            Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
			return ;
		}

      if (!(Kisekae.isWebswing()))
      {
         JOptionPane.showMessageDialog(this,
          	fsb.getName() + "\n" +
            Kisekae.getCaptions().getString("NewArchiveCreated"),
            Kisekae.getCaptions().getString("NewArchiveTitle"),
            JOptionPane.INFORMATION_MESSAGE);
      }
	}



   // Method to open an existing archive.  This method opens the file,
   // reads the directory information and populates the archive element
   // list component.

   private void openArchive() { openArchive(null) ; }
	private void openArchive(String path)
	{
      clearBusy() ;
   	ArchiveFile newzip = null ;
   	pathname = selectArchive(path,
         Kisekae.getCaptions().getString("OpenArchiveTitle"),null) ;
      if (pathname == null) return ;

      // Check to see if the file already exists.

      File fsb = new File(pathname) ;
      if (!fsb.exists())
      {
			int i = JOptionPane.showConfirmDialog(this,
            filename.toUpperCase() + "\n" +
            Kisekae.getCaptions().getString("CreateFileText"),
            Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) ;
         if (i == JOptionPane.YES_OPTION)
         	createArchive(pathname) ;
		   return ;
      }

		// Close any open archive.

      int currentsort = sortcolumn ;
      closeArchive() ;

		// Open the archive file.

      try
      {
         String s = Kisekae.getCaptions().getString("OpenArchiveTitle") ;
         statuslabel.setText(s + " " + filename.toUpperCase());
         fileopen = new FileOpen(this,pathname,"r") ;
         fileopen.setFileFilter("kissarchives") ;
         fileopen.open() ;
         newzip = fileopen.getZipFile() ;
			if (newzip == null)
         	throw new IOException(Kisekae.getCaptions().getString("FileReadError")) ;
			if (!newzip.isArchive())
         	throw new IOException(Kisekae.getCaptions().getString("SaveAsArchiveText")) ;
         setOpen() ;
      }

		// Catch file error exceptions.

		catch (IOException e)
		{
         PrintLn.println("ZipManager: Open " + filename + " " + e.getMessage()) ;
			JOptionPane.showMessageDialog(this, filename + "\n" + e.getMessage(),
            Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
			closeArchive() ;
			return ;
		}

      // Update our frame title and menu actions.

      String s = getTitle() ;
      if (pathname != null) s += " [" + pathname + "]";
		setTitle(s) ;
      addaction.setEnabled(true);
      closefile.setEnabled(true) ;
      deletefile.setEnabled(true) ;
      copyfile.setEnabled(true) ;
      renamefile.setEnabled(true) ;
     	deleteaction.setEnabled(false);
     	extractaction.setEnabled(true);
     	viewaction.setEnabled(false);
     	selectaction.setEnabled(true);
     	findaction.setEnabled(true) ;
      CLOSE.setEnabled(true) ;
      ADD.setEnabled(true);
      EXTRACT.setEnabled(true) ;
      DELETE.setEnabled(true) ;
      VIEW.setEnabled(false) ;
     	FIND.setEnabled(true) ;

      // Show the archive file characteristics.

      zip = newzip ;
      s = Kisekae.getCaptions().getString("OpenArchiveTitle") ;
      s += " " + filename.toUpperCase() ;
      Vector contents = zip.getContents() ;
      int n = (contents != null) ? contents.size() : 0 ;
      s += " (" + n +")" ;
      long zs = zip.getSize() / 1024 ;
      long cs = zip.getCompressedSize() / 1024 ;
      int factor = (int) (((float) (zs-cs) / (float) zs) * 100) ;
      s += "  " + Kisekae.getCaptions().getString("NoCompressionText") ;
      s += " " + zs + "K" ;
      s += "  " + Kisekae.getCaptions().getString("NormalCompressionText") ;
      s += " " + cs + "K" ;
      s += "  [" + factor + "%]" ;
		statuslabel.setText(s) ;
      sortcolumn = currentsort ;
      setList() ;
	}


   // Method to open a URL archive.  This method downloads the file then
   // opens it.

   private void openURLArchive()
	{
   }


   // Method to close an archive.

	private void closeArchive()
	{
		closeArchiveFile() ;
      fileopen = null ;

      // Clear references.

		ze = null ;
      zip = null ;
      statuslabel.setText(Kisekae.getCopyright());
      clearBusy() ;

      // Update our frame title and menu actions.

		setTitle(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
      addaction.setEnabled(false);
      closefile.setEnabled(false) ;
      deletefile.setEnabled(false) ;
      copyfile.setEnabled(false) ;
      renamefile.setEnabled(false) ;
     	deleteaction.setEnabled(false);
     	extractaction.setEnabled(false);
     	viewaction.setEnabled(false);
     	selectaction.setEnabled(false);
     	unselectaction.setEnabled(false);
     	findaction.setEnabled(false) ;
      CLOSE.setEnabled(false) ;
      ADD.setEnabled(false) ;
      EXTRACT.setEnabled(false) ;
      DELETE.setEnabled(false) ;
      VIEW.setEnabled(false) ;
     	FIND.setEnabled(false) ;

      // Erase our table.

      sortcolumn = 0 ;
      setList() ;
   }


   // Method to close an archive file to release any locks.  The ZipManager
   // should not keep locks up on a file unless it is actively working with
   // the file.

	private void closeArchiveFile()
	{
		if (fileopen != null) fileopen.close() ;
   }


   // Method to show a file dialog to select an archive file.  This method
   // returns the path name of the selected file.

	private String selectArchive(String path, String title, String approve)
	{
   	FileOpen fd = null ;
      String name = null ;
      
      // If we are creating a new archive in Webswing, create a temporary
      // file.  The Webswing Save As dialog will upload the file to the
      // client with a user specified name.
      
      if (Kisekae.isWebswing() && path == null && approve != null)
      {
         try
         {
            File f = File.createTempFile("UltraKiss-Archive", ".zip") ;
            path = f.getPath() ;
            f.deleteOnExit() ;
         }
         catch (Exception e)
         {
            PrintLn.printErr("ZipManager: " + e);
            return null ;
         }
      }

      // If we have a path already, use it.

   	if (path != null)
      {
      	pathname = path ;
         File f = new File(pathname) ;
         filename = f.getName() ;
         dirname = f.getParent() ;
			int i = filename.lastIndexOf(".") ;
			extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;
         return pathname ;
      }

      // We do not have a known path to an archive file.  Show a file
      // dialog to choose a file.

      try
      {
			fd = new FileOpen(this,title) ;
         fd.setFileFilter("archives");
			fd.show(title,approve) ;

	      // Check to see if a valid archive type was selected.

	      name = fd.getFile() ;
			if (name != null)
	      {
		      if (!ArchiveFile.isArchive(name))
		      {
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
      }

      // Catch security exceptions.

      catch (SecurityException e)
      {
         PrintLn.println("ZipManager: Archive file open exception, " + e.getMessage()) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

		// Get the selected file name.

		if (name == null) return null ;
		dirname = fd.getDirectory() ;
		filename = fd.getFile() ;
      File f = new File(dirname,filename) ;
		pathname = f.getPath() ;
		int i = filename.lastIndexOf(".") ;
		extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;

      // If file exists prompt for approval to overwrite.
      
      if (approve == null) return pathname ;
      if (!f.exists()) return pathname ;
      String [] msg = new String [] { pathname, 
         Kisekae.getCaptions().getString("ReplaceFileText") } ;
      i = JOptionPane.showConfirmDialog(this, msg,
         Kisekae.getCaptions().getString("CopyArchiveTitle"),
         JOptionPane.YES_NO_OPTION) ;
      if (i == JOptionPane.YES_OPTION) return pathname ;
      return null ;     
	}


	// Method to extract files from an archive.  The archive manager busy
	// flag is set during extract and cleared on the file writer callback.

	private void extractArchive()
	{
      if (zip == null) return ;
      Vector extractnames = new Vector() ;
     	int [] rows = TABLE.getSelectedRows() ;
      int n = (rows == null) ? 0 : rows.length ;

      // Identify the relative names of the selected extract items.

      for (int i = 0 ; i < n ; i++)
      {
      	int row = rows[i] ;
			Object o = tabledata.getValueAt(row,0) ;
			if (!(o instanceof String)) continue ;
         String filename = (String) o ;
         o = tabledata.getValueAt(row,5) ;
         String dir = (o instanceof String) ? o.toString() : "" ;
         if ("".equals(dir)) dir = null ;
         File f = new File(dir,filename) ;
         extractnames.add(f.getPath()) ;
      }

      // Extract the elements.

      savecallback = "extract" ;
      ZipExtract ae = new ZipExtract(this,zip,extractnames) ;
      ae.setVisible(true) ;
	}

   private void extractArchiveCallback()
   {
      clearBusy() ;
   }


   // Method to add files to an archive.   The updated archive is re-opened
   // when the addition callback is fired to update the content list.

	private void addArchive()
	{
      if (zip == null) return ;
		setBusy() ;
      ZipAdd ae = new ZipAdd(this,zip) ;
      savecallback = "add" ;
      ae.setVisible(true);
	}

   private void addArchiveCallback()
   {
      if (zip == null) return ;
      openArchive(zip.getName()) ;
      closeArchiveFile() ;
      clearBusy() ;
   }


   // Method to copy an archive file.  The new copy is opened when the
   // copy callback is fired to update the content list.

   private void copyArchive()
   {
   	if (zip == null) return ;
      pathname = selectArchive(null,
         Kisekae.getCaptions().getString("CopyArchiveTitle"),
         Kisekae.getCaptions().getString("CopyAsMessage")) ;
      if (pathname == null) return ;
      if (pathname.equalsIgnoreCase(zip.getName())) return ;

		// Open the zip file.

		FileOpen fileopen = zip.getFileOpen() ;
		fileopen.open() ;
		if (fileopen.getZipFile() == null) return ;

		// Write the new file.

		filewriter = new FileWriter(this,fileopen.getZipFile(),pathname) ;
		filewriter.callback.addActionListener((ActionListener) this);
		Thread thread = new Thread(filewriter) ;
		setBusy() ;
		savecallback = "copy" ;
		thread.start() ;
	}

   private void copyArchiveCallback()
   {
   	if (zip == null) return ;
      openArchive(pathname) ;
      closeArchiveFile() ;
      clearBusy() ;
   }


   // Method to rename an archive file.  This is a copy followed by a delete.
   // The deletion is performed when the copy callback is fired.

   private void renameArchive()
   {
   	if (zip == null) return ;
      pathname = selectArchive(null,
         Kisekae.getCaptions().getString("RenameArchiveTitle"),
         Kisekae.getCaptions().getString("RenameAsMessage")) ;
      if (pathname == null) return ;
      if (pathname.equalsIgnoreCase(zip.getName())) return ;

		// Open the zip file.

		FileOpen fileopen = zip.getFileOpen() ;
		fileopen.open() ;
		if (fileopen.getZipFile() == null) return ;

		// Write the new file.

		filewriter = new FileWriter(this,fileopen.getZipFile(),pathname) ;
		filewriter.callback.addActionListener((ActionListener) this);
		Thread thread = new Thread(filewriter) ;
		setBusy() ;
		savecallback = "rename" ;
		thread.start() ;
	}

   private void renameArchiveCallback()
   {
   	if (zip == null) return ;
      String source = zip.getName() ;
      if (source == null) return ;
      File fsb = new File(source) ;
      closeArchive() ;
      fsb.delete() ;
      openArchive(pathname) ;
      closeArchiveFile() ;
      clearBusy() ;
   }


   // Method to delete an archive file.

   private void deleteArchive()
   {
   	if (zip == null) return ;
      String source = zip.getName() ;
      if (source == null) return ;

      // Show a confirmation dialog.

		int i = JOptionPane.showConfirmDialog(this,
        	source + "\n" +
         Kisekae.getCaptions().getString("DeleteFileText"),
         Kisekae.getCaptions().getString("DeleteArchiveTitle"),
         JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) ;
		if (i != JOptionPane.YES_OPTION) return ;

		// Delete the file if confirmed.

      File fsb = new File(source) ;
      closeArchive() ;
      if (fsb.delete()) return ;

      // Error on delete.

      JOptionPane.showMessageDialog(this,
      	source + "\n" +
         Kisekae.getCaptions().getString("FileDeleteError"),
         Kisekae.getCaptions().getString("DeleteArchiveTitle"),
         JOptionPane.ERROR_MESSAGE);
      return ;
   }


   // Method to delete selected archive elements.  The elements are
   // identified by their index position in the element list.

   private void deleteElements()
   {
   	if (zip == null) return ;
      int [] elements = TABLE.getSelectedRows() ;
      if (elements == null) return ;
		if (elements.length == 0) return ;

		// Open the zip file.

		Vector contents = new Vector() ;
		FileOpen fileopen = zip.getFileOpen() ;
      if (fileopen == null) 
      {
         fileopen = new FileOpen(frame,zip.pathname,"r") ;
         fileopen.setFileFilter("kissarchives") ;
         fileopen.setZipFile(zip) ;
      }
      else if (fileopen == null) return ;
		fileopen.open() ;
		if (fileopen.getZipFile() == null) return ;

		// Remove the selected elements from the zip file contents list.
      // The remaining archive elements are the ones written to the file.

		contents.addAll(fileopen.getZipFile().getContents()) ;
      for (int i = 0 ; i < elements.length ; i++)
      {
      	int j ;
      	int row = elements[i] ;
         Object o = tabledata.getValueAt(row,0) ;
         if (!(o instanceof String)) continue ;
         for (j = 0 ; j < contents.size() ; j++)
         {
				ArchiveEntry ae = (ArchiveEntry) contents.elementAt(j) ;
				if (o.toString().equalsIgnoreCase(ae.toString())) break ;
         }
			if (j < contents.size()) contents.removeElementAt(j) ;
      }

		// Confirm that we are not deleting all elements from a zip file.

      if (contents.size() == 0)
      {
			int i = JOptionPane.showConfirmDialog(this,
            Kisekae.getCaptions().getString("DeleteAllText"),
            Kisekae.getCaptions().getString("DeleteArchiveTitle"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) ;
			if (i != JOptionPane.YES_OPTION)
				{ fileopen.close() ; return ; }

			// Delete the file if confirmed.

	      String source = zip.getName() ;
	      if (source == null) return ;
	      File fsb = new File(source) ;
	      closeArchive() ;
	      if (fsb.delete()) return ;

	      // Error on delete.

			JOptionPane.showMessageDialog(this,
            source + "\n" +
            Kisekae.getCaptions().getString("FileDeleteError"),
            Kisekae.getCaptions().getString("DeleteArchiveTitle"),
            JOptionPane.ERROR_MESSAGE);
			return ;
      }

      // Confirm element deletion request.

		int i = JOptionPane.showConfirmDialog(this,
        	pathname + "\n" +
         Kisekae.getCaptions().getString("DeleteElementText"),
         Kisekae.getCaptions().getString("DeleteElementTitle"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) ;
		if (i != JOptionPane.YES_OPTION)
			{ fileopen.close() ; return ; }

      // Set the save directory flag on all content elements to retain
      // directory path name information.

		for (i = 0 ; i < contents.size() ; i++)
			((ArchiveEntry) contents.elementAt(i)).setSaveDir(true) ;

		// Perform the element deletion.

		filewriter = new FileWriter(this,fileopen.getZipFile(),contents,false) ;
		filewriter.callback.addActionListener((ActionListener) this);
		Thread thread = new Thread(filewriter) ;
		setBusy() ;
		savecallback = "delete" ;
		thread.start() ;
	}

   private void deleteElementsCallback()
   {
   	if (zip == null) return ;
      openArchive(pathname) ;
      closeArchiveFile() ;
      clearBusy() ;
   }


   // Method to View an archive element according to its type.
   // The file is opened in preparation for viewing.

   private void viewElement(String element)
   {
      if (fileopen == null) return ;
      fileopen.open(element) ;
      ze = fileopen.getZipEntry() ;
      if (ze == null) return ;
		savecallback = "view" ;

		// Text elements invoke the text editor.

      if (viewastext.isSelected() || ze.isText() || ze.isRichText())
      {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      	TextFrame tf = new TextFrame(ze,this) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         tf.setVisible(true) ;
         return ;
      }

      // Palette elements invoke the color editor.

      if (ze.isPalette())
      {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      	ColorFrame cf = new ColorFrame(ze,this) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         cf.setVisible(true) ;
         return ;
      }

		// Audio and Video elements invoke the media player.

      if (ze.isAudio() || ze.isVideo())
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			MediaFrame mf = new MediaFrame(ze) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         mf.setMinimized(false) ;
         mf.setVisible(true) ;
         return ;
      }

		// Image elements invoke the image viewer.

      if (ze.isImage())
      {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			ImageFrame mf = new ImageFrame(ze,this) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         mf.setVisible(true) ;
         return ;
      }

      // Unknown elements are a problem.

		int i = JOptionPane.showConfirmDialog(this,
        	element + "\n" +
         Kisekae.getCaptions().getString("MenuViewAsText") + "?",
         Kisekae.getCaptions().getString("ViewFileTitle"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) ;

		// Invoke the text viewer if requested.

		if (i == JOptionPane.YES_OPTION)
      {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      	TextFrame tf = new TextFrame(ze,this) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         tf.setVisible(true) ;
         return ;
      }

      // Close the archive file to release locks.

      fileopen.close() ;
   }

   private void viewElementCallback()
   {
   	if (fileopen == null) return ;
      openArchive(pathname) ;
      closeArchiveFile() ;
   }


   // A function to setup the contents of the archive element list.

   private void setList()
   {
   	if (TABLE == null) return ;
      if (tabledata == null) return ;
   	tabledata.updateTableData(zip) ;

      // Update the table column names to identify the sort column.

     	TableColumnModel columnmodel = TABLE.getColumnModel() ;
      for (int i = 0 ; i < tabledata.columns.length ; i++)
      {
        	TableColumn column = columnmodel.getColumn(i) ;
         column.setHeaderValue(tabledata.getColumnName(column.getModelIndex())) ;
      }

      // Update the sort menu items.

      if (sortcolumn == 0) sortnosort.setSelected(true) ;
      if (sortcolumn == 1) sortname.setSelected(true) ;
      if (sortcolumn == 2) sorttime.setSelected(true) ;
      if (sortcolumn == 3) sortsize.setSelected(true) ;
      if (sortcolumn == 4) sortratio.setSelected(true) ;
      if (sortcolumn == 5) sortpacked.setSelected(true) ;
      if (sortcolumn == 6) sortpath.setSelected(true) ;

      // Clear selection items.

     	TABLE.clearSelection() ;
      VIEW.setEnabled(false) ;
      unselectaction.setEnabled(false) ;
      deleteaction.setEnabled(false) ;
      viewaction.setEnabled(false) ;

      // Update the table data.

      TABLE.getTableHeader().repaint() ;
      Vector v = tabledata.getData() ;
      Collections.sort(v,new SortComparator(sortcolumn,ascending)) ;
      TABLE.tableChanged(new TableModelEvent(tabledata)) ;
      TABLE.repaint() ;
   }


   // Function to reference a KiSS configuration object and construct a list of 
   // content entries that relate to every writable object found within the source
   // archive.  All entries are set to the specified compression method.

	private Vector getContents(KissObject kiss, ArchiveFile zip, int method)
   {
   	Vector v = new Vector() ;
		if (kiss == null) return v ;
		ArchiveEntry ze = kiss.getZipEntry() ;
      if (ze == null) return v ;

      // Add the KiSS object zip entry to our contents vector.

      ze = (ArchiveEntry) ze.clone() ;
      ze.setMethod(method) ;
  		v.add(ze) ;

      // Configuration objects are collections of cels, palettes, and
      // sound files.  We add the zip entries for all these objects to
      // our contents vector so that their contents can be referenced
      // when the configuration is saved.  We also note that we are
      // saving a configuration set as directory paths are required.

      if (kiss instanceof Configuration)
      {
	      Configuration c = (Configuration) kiss ;
         ArchiveFile czip = (c != null) ? c.getZipFile() : null ;
         String directory = (czip != null) ? czip.getDirectoryName() : null ;

	      // Add the cel object entries to the contents vector.

	      int i = 0 ;
	      Vector cels = c.getCels() ;
	      while (cels != null && i < cels.size())
	      {
				Cel cel = (Cel) cels.elementAt(i++) ;
				if (cel.isError()) continue ;
				if (cel.isCopy()) continue ;
            if (cel instanceof JavaCel) continue ;
            if (cel instanceof Video) continue ;

            // Load the cel if necessary.

            if (!cel.isLoaded())
            {
               try { cel.load() ; }
               catch (Exception e) { }
            }

            // Determine the cel archive entry.

				if (!cel.isWritable()) continue ;
				ze = (zip == null) ? null : zip.getEntry(cel.getPath(),true) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,cel.getName(),null) ;
               cel.setName(ze.getPath()) ;
            }

				if (cel.isUpdated()) ze = cel.getZipEntry() ;
				if ((ze == null && cel.getZipEntry() instanceof DirEntry) ||
                (ze == null && cel.getZipEntry() == null))
              	ze = new DirEntry(c.getDirectory(),cel.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = cel.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            ze.setMethod(method) ;
            
            // Add the kiss object to the content vector if the object
            // has been updated or a memory file exists. 
            
            if (ze.isUpdated() || ze.getMemoryFile() != null)
               v.add(cel) ;
            else           
               v.add(ze) ;
			}

	      // Add the palette object entries to the contents vector.

	      i = 0 ;
	      Vector palettes = c.getPalettes() ;
	      while (palettes != null && i < palettes.size())
	      {
				Palette palette = (Palette) palettes.elementAt(i++) ;
	         if (palette.isCopy()) continue ;
            if (!palette.isWritable()) continue ;
				ze = (zip == null) ? null : zip.getEntry(palette.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,palette.getName(),null) ;
               palette.setName(ze.getPath()) ;
            }
				if (palette.isUpdated()) ze = palette.getZipEntry() ;
				if (ze == null && palette.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),palette.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = palette.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            ze.setMethod(method) ;
            
            // Add the kiss object to the content vector if the object
            // has been updated or a memory file exists. 
            
            if (ze.isUpdated() || ze.getMemoryFile() != null)
               v.add(palette) ;
            else           
               v.add(ze) ;
			}

			// Add the audio object entries to the contents vector.

	      i = 0 ;
	      Vector sounds = c.getSounds() ;
	      while (sounds != null && i < sounds.size())
	      {
				Audio audio = (Audio) sounds.elementAt(i++) ;
	         if (audio.isCopy()) continue ;
            if (!audio.isWritable()) continue ;
				ze = (zip == null) ? null : zip.getEntry(audio.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,audio.getName(),null) ;
               audio.setName(ze.getPath()) ;
            }
				if (audio.isUpdated()) ze = audio.getZipEntry() ;
				if (ze == null && audio.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),audio.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = audio.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            ze.setMethod(method) ;
            
            // Add the kiss object to the content vector if the object
            // has been updated or a memory file exists. 
            
            if (ze.isUpdated() || ze.getMemoryFile() != null)
               v.add(audio) ;
            else           
               v.add(ze) ;
			}

			// Add the video object entries to the contents vector.

	      i = 0 ;
			Vector movies = c.getMovies() ;
			while (movies != null && i < movies.size())
	      {
				Video video = (Video) movies.elementAt(i++) ;
				if (video.isCopy()) continue ;
				if (!video.isWritable()) continue ;
				ze = (zip == null) ? null : zip.getEntry(video.getPath()) ;
            if (zip == null)
            {
            	ze = new DirEntry(directory,video.getName(),null) ;
               video.setName(ze.getPath()) ;
            }
				if (video.isUpdated()) ze = video.getZipEntry() ;
				if (ze == null && video.getZipEntry() instanceof DirEntry)
	           	ze = new DirEntry(zip.getDirectoryName(),video.getName(),zip) ;
            if (ze == null) continue ;

            // Relative names.

            String path = video.getPath() ;
            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            if (directory != null)
            {
      	      if (path.startsWith(directory))
      	      	relativename = path.substring(directory.length()) ;
      	      if (relativename.startsWith(File.separator))
      	      	relativename = relativename.substring(File.separator.length()) ;
               if (ze instanceof DirEntry) ze.setDirectory(directory) ;
               ze.setName(relativename) ;
            }
            ze.setMethod(method) ;
            
            // Add the kiss object to the content vector if the object
            // has been updated or a memory file exists. 
            
            if (ze.isUpdated() || ze.getMemoryFile() != null)
               v.add(video) ;
            else           
               v.add(ze) ;
			}

			// Add the other text object entries to the contents vector.
         // These are DirEntries as they were selected for import.

	      i = 0 ;
	      Vector otherfiles = c.getOtherFiles();
	      while (otherfiles != null && i < otherfiles.size())
	      {
				Object o = otherfiles.elementAt(i++) ;
	         if (!(o instanceof DirEntry)) continue ;
            ze = (DirEntry) o ;

            // Relative names.

            String relativename = ze.getName() ;
            ze = (ArchiveEntry) ze.clone() ;
            ze.setMethod(method) ;
            v.add(ze) ;
			}
		}

      // Return the list of writable zip entry objects.

      return v ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
 	 	Object source = evt.getSource() ;

		// An update request from the file writer window has occured.

      try
      {
         if ("FileWriter Callback".equals(evt.getActionCommand()))
         {
            // Process the callback request as per our return indicator.

            if (savecallback == null) return ;
            callreturn = true ;
            if ("copy".equals(savecallback)) copyArchiveCallback() ;
            else if ("rename".equals(savecallback)) renameArchiveCallback() ;
            else if ("extract".equals(savecallback)) extractArchiveCallback() ;
            else if ("add".equals(savecallback)) addArchiveCallback() ;
            else if ("delete".equals(savecallback)) deleteElementsCallback() ;
            else if ("view".equals(savecallback)) viewElementCallback() ;

            // Multiple file saves, or view callbacks can occur from the
            // TextFrame object.

            if (!("view".equals(savecallback))) savecallback = null ;
            callreturn = false ;
         }

         // Exit request.

         if (source == exit)
         {
            if (checkActive(true)) return ;
            closeArchive() ;
            close() ;
            return ;
         }

         // New Archive request.

         if (source == newfile || source == NEW)
         { createArchive() ; return ; }

         // Open Archive request.

         if (source == openfile || source == OPEN)
         { openArchive() ; closeArchiveFile() ; return ; }

         // Open URL Archive request.

         if (source == openurl)
         { openURLArchive() ; return ; }

         // Close Archive request.

         if (source == closefile || source == CLOSE)
         { closeArchive() ; return ; }

         // Extract element request.

         if (source == extractaction || source == EXTRACT)
         { extractArchive() ; return ; }

         // Add element request.

         if (source == addaction || source == ADD)
         { addArchive() ; return ; }

         // View element request.

         if (source == viewaction || source == VIEW)
         {
            int row = TABLE.getSelectedRow() ;
            Object o = tabledata.getValueAt(row,0) ;
            if (!(o instanceof String)) return ;
            String filename = (String) o ;
            o = tabledata.getValueAt(row,5) ;
            String dir = (o instanceof String) ? o.toString() : "" ;
            if ("".equals(dir)) dir = null ;
            File f = new File(dir,filename) ;
            viewElement(f.getPath()) ;
            return ;
         }

         // Copy archive request.

         if (source == copyfile)
         { copyArchive() ;  return ; }

         // Rename archive request.

         if (source == renamefile)
         { renameArchive() ; return ; }

         // Delete archive request.

         if (source == deletefile)
         { deleteArchive() ; return ; }

         // Delete archive element request.

         if (source == deleteaction || source == DELETE)
         { deleteElements() ; return ; }

         // Find an element in the archive list.

         if (source == findaction || source == FIND)
         { new ZipFindDialog(this) ; return ; }

         // Select all elements request.

         if (source == selectaction)
         {
            if (zip == null) return ;
            ListSelectionModel m = TABLE.getSelectionModel() ;
            if (m == null) return ;
            m.setSelectionInterval(0,TABLE.getRowCount()-1) ;
            return ;
         }

         // Unselect all request.

         if (source == unselectaction)
         {
            if (zip == null) return ;
            TABLE.clearSelection() ;
            VIEW.setEnabled(false) ;
            unselectaction.setEnabled(false) ;
            deleteaction.setEnabled(false) ;
            viewaction.setEnabled(false) ;
            return ;
         }

         // Sort requests.

         if (source == sortnosort) { sortcolumn = 0 ; setList() ; return ; }
         if (source == sortname)   { sortcolumn = 1 ; setList() ; return ; }
         if (source == sorttime)   { sortcolumn = 2 ; setList() ; return ; }
         if (source == sortsize)   { sortcolumn = 3 ; setList() ; return ; }
         if (source == sortratio)  { sortcolumn = 4 ; setList() ; return ; }
         if (source == sortpacked) { sortcolumn = 5 ; setList() ; return ; }
         if (source == sortpath)   { sortcolumn = 6 ; setList() ; return ; }
         
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

         if (about == source)
         { aboutdialog.show() ; return ; }
      }
      
		// Watch for internal faults during action events.

		catch (Throwable ex)
		{
			PrintLn.println("ZipManager: Internal fault, action " + evt.getActionCommand()) ;
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
	public void windowClosing(WindowEvent evt)
   { checkActive(false) ; closeArchive() ; close() ; }


	// Function to terminate any active threads on close.
   // This function returns true if threads are left running.
   // A cancel option is provided if the cancel argument is true.

   boolean checkActive(boolean cancel)
	{
		int i = 0 ;
      if (filewriter == null) return false ;
      String s = Kisekae.getCaptions().getString("ZipStatusBusy") ;
   	if (!s.equals(runlabel.getText())) return false ;

		// We appear to have an active thread.  Show a confirm dialog
		// to solicit approval if we can cancel the operation.

		if (cancel)
		{
			i = JOptionPane.showConfirmDialog(this,
				Kisekae.getCaptions().getString("ArchiveTerminateText"),
            Kisekae.getCaptions().getString("ArchiveManagerTitle"),
            JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE) ;
		}
		else
			i = JOptionPane.OK_OPTION ;

		// Interrupt the file writer if required.

		if (i == JOptionPane.CANCEL_OPTION) return true ;
		if (i == JOptionPane.OK_OPTION)
		{
			filewriter.interrupt() ;
			try { Thread.currentThread().sleep(1000) ; }
			catch (InterruptedException e) { }
		}
      return false ;
   }

   // A means for extract and add to identify active filewriter objects.

	void setFileWriter(FileWriter fw) { filewriter = fw ; }

   // Return the archive table object for use by the Find dialog.

	JTable getTable() { return TABLE ; }

   // Return the archive filename.

	String getFilename() { return filename ; }

   // Return the archive filename.

	ArchiveFile getZipFile() { return zip ; }

   // Return the archive entry.

	ArchiveEntry getZipEntry() { return ze ; }

   // Set the Archive Manager state.

   void setBusy() { runlabel.setText(Kisekae.getCaptions().getString("ZipStatusBusy")) ; }
   void clearBusy() { runlabel.setText(Kisekae.getCaptions().getString("ZipStatusReady")) ; }
   void setOpen() { runlabel.setText(Kisekae.getCaptions().getString("ZipStatusOpen")) ; }
   boolean isBusy() { return runlabel.getText().equals(Kisekae.getCaptions().getString("ZipStatusBusy")) ; }
   boolean isError() { return (filewriter != null) ? filewriter.isError() : false ; }

   // Disable Rename archive so as to not destroy original file when loaded.
   
   public void disableRenameDelete()
   {
      renamefile.setEnabled(false) ;
      deletefile.setEnabled(false) ;
      DELETE.setEnabled(false);
   }
   
	// Method to close our frame.

	public void close() 
   { 
      super.close() ; 
      flush() ; 
      dispose() ;  
   }

   // Method to clear variable storage.

   private void flush()
   {
   	ze = null ;
   	zip = null ;
      fileopen = null ;
      tabledata = null ;
      filewriter = null ;
   }

   // A function to set the viewport so that the specified row is visible.
   // The viewport is not changed if the row is currently visible.

   void setViewRow(int row)
   {
      if (TABLE == null) return ;
   	if (scroll == null) return ;
      JViewport view = scroll.getViewport() ;
      Rectangle r = view.getViewRect() ;
      int inc = TABLE.getScrollableUnitIncrement(r,SwingConstants.VERTICAL,1) ;
      int ypos = row * inc ;
      if (r.y < ypos && r.y+r.height-inc > ypos) return ;
      view.setViewPosition(new Point(r.x,ypos)) ;
      repaint() ;
   }


	// Filename Filter interface to show only the relevent files.
	// Note: This doesn't appear to work under Microsoft Windows.

	public boolean accept(File dir, String name)
	{
   	if (name == null) return false ;
   	String s = name.toLowerCase() ;
		if (s.endsWith(".zip")) return true ;
		if (s.endsWith(".gzip")) return true ;
		if (s.endsWith(".jar")) return true ;
		if (s.endsWith(".lzh")) return true ;
      return false ;
	}


   // Inner classes to define the table data characteristics.

   class VariableData
   {
   	public String name ;
      public String date ;
      public Long size ;
      public Integer ratio ;
      public Long packed ;
      public String dir ;

      public VariableData(String name, String date, long size, int ratio, long packed, String dir)
      {
      	this.name = name ;
         this.date = date ;
         this.size = new Long(size) ;
         this.ratio = new Integer(ratio) ;
         this.packed = new Long(packed) ;
         this.dir = dir ;
      }
   }


   class ColumnData
   {
   	protected String title ;
      protected int width ;
      protected int alignment ;

      public ColumnData(String title, int width, int alignment)
      {
      	this.title = title ;
         this.width = width ;
         this.alignment = alignment ;
      }
   }


   class VariableTableData extends AbstractTableModel
   {
   	final public ColumnData columns[] = {
      	new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldName"),60,JLabel.LEFT),
         new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldDate"),80,JLabel.LEFT),
         new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldSize"),30,JLabel.RIGHT),
         new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldRatio"),30,JLabel.RIGHT),
         new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldPacked"),30,JLabel.RIGHT),
         new ColumnData(Kisekae.getCaptions().getString("ArchiveFieldPath"),130,JLabel.LEFT) } ;

      private Vector vector = null ;
      private ArchiveFile zip = null ;

      public VariableTableData() { this(null) ; }

      public VariableTableData(ArchiveFile zip)
      {
      	this.zip = zip ;
      	vector = new Vector() ;
         setDefaultData() ;
      }

      private void setDefaultData()
      {
         if (zip == null) return ;

	      Vector contents = zip.getContents() ;
         if (contents != null) 
         {
         	vector.removeAllElements() ;
   			Enumeration enum1 = contents.elements() ;
   	      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a") ;

   	      // Add the sorted file elements to the list.

   	      while (enum1 != null && enum1.hasMoreElements())
   	      {
   	      	ze = (ArchiveEntry) enum1.nextElement() ;
   				Date d = new Date(ze.getTime()) ;
   	         long size = ze.getSize() ;
   	         long packed = ze.getCompressedSize() ;
   	         int ratio = (size == 0) ? 0 :
   	         	(int) (((float) (size-packed) / (float) size) * 100) ;
   	         String dir = ze.getPath() ;
   	         if (dir != null)
   	         {
   		         File f = new File(dir) ;
   		         dir = f.getParent() ;
   	         }
               if (dir == null) dir = "" ;
               vector.addElement(new VariableData(ze.getName(),
               	sdf.format(d), size, ratio, packed, dir)) ;
      		}
         }

         Collections.sort(vector,new SortComparator(sortcolumn,ascending)) ;
		}

      private void updateTableData(ArchiveFile zip)
      {
      	this.zip = zip ;
         if (zip == null) vector = new Vector() ;
         setDefaultData() ;
      }

      public int getRowCount() { return (vector == null) ? 0 : vector.size() ; }

      public int getColumnCount() { return columns.length ; }

      public String getColumnName(int c)
      {
      	String s = columns[c].title ;
         if (c == sortcolumn-1)
         	s += (ascending) ? " >>" : " <<" ;
      	return s ;
      }

      public boolean isCellEditable(int row, int col) { return false ; }

      public Object getValueAt(int row, int col)
      {
         if (vector == null) return null ;
      	if (row < 0 || row >= getRowCount()) return null ;
         VariableData rowvalue = (VariableData) vector.elementAt(row) ;
         switch (col)
         {
         	case 0: return rowvalue.name ;
            case 1: return rowvalue.date ;
            case 2: return rowvalue.size ;
            case 3: return rowvalue.ratio ;
            case 4: return rowvalue.packed ;
            case 5: return rowvalue.dir ;
         }
         return null ;
      }

      public String getTitle()
      {
         return Kisekae.getCaptions().getString("ArchiveElementsTitle") ;
      }

      public Vector getData() { return vector ; }
      
      // Find the row for the specified element name
      
      public int findRow(String filename)
      {
         if (vector == null) return -1 ;
         if (filename == null) return -1 ;
         for (int row = 0 ; row < vector.size() ; row++)
         {
            String name = (String) getValueAt(row,0) ;
            String dir = (String) getValueAt(row,5) ;
            if (dir == "") dir = null ;
            String path = (new File(dir,name)).getPath() ;
            if (filename.equalsIgnoreCase(path)) return row ;
         }
         return -1 ;
      }
   }


   class SortComparator implements Comparator
   {
   	private int column ;
      private boolean ascending ;

      public SortComparator(int column, boolean ascending)
      {
      	this.column = column ;
         this.ascending = ascending ;
      }

      public int compare(Object o1, Object o2)
      {
      	if (!(o1 instanceof VariableData)) return 0 ;
         if (!(o2 instanceof VariableData)) return 0 ;
         VariableData vd1 = (VariableData) o1 ;
         VariableData vd2 = (VariableData) o2 ;
         int result = 0 ;

         switch (column)
         {
         	case 1:
            	result = vd1.name.compareTo(vd2.name) ;
               break ;

            case 2:
            	result = vd1.date.compareTo(vd2.date) ;
               break ;

            case 3:
            	result = vd1.size.compareTo(vd2.size) ;
               break ;

            case 4:
            	result = vd1.ratio.compareTo(vd2.ratio) ;
               break ;

            case 5:
            	result = vd1.packed.compareTo(vd2.packed) ;
               break ;

            case 6:
            	result = vd1.dir.compareTo(vd2.dir) ;
               break ;
         }

         if (!ascending) result = -result ;
         return result ;
      }
   }
}


