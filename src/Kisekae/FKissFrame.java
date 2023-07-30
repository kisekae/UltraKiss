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
* FKissFrame Class
*
* Purpose:
*
* This object is a visual debugger for FKiSS code.  It is used to edit
* and trace FKiSS code executing in the UltraKiss application.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import java.awt.print.* ;
import java.awt.datatransfer.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.util.* ;
import java.util.prefs.* ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.event.* ;
import javax.swing.undo.* ;
import javax.swing.tree.* ;
import javax.swing.table.* ;


final class FKissFrame extends KissFrame
	implements ActionListener, ItemListener, WindowListener, ClipboardOwner
{
	private static String helpset = "Help/FKissEditor.hs" ;
	private static String helpsection = "fkisseditor.index" ;
	private static String refset = "Help/Reference2.hs" ;
	private static String refsection = "reference2.index" ;
	private static String onlinehelp = "fkisseditor/index.html" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;
	private HelpLoader helper2 = null ;

	// Debug frame interface objects

	private FKissFrame me = null ;					// Reference to ourselves
	private FKissEvent event = null ;			   // Active event object
	private FKissAction action = null ;			   // Active action object
	private KissObject kiss = null ;			      // Active KiSS object
	private FKissEvent invoker = null ;			   // Event for callbacks
	private Configuration config = null ;  		// Active configuration
	private TextFrame tf = null ;						// The text edit frame
	private EventWizard ne = null ;				   // The event wizard dialog
	private Vector errormsgs = null ;				// The event parse errors
   private ImagePreview preview = null ;        // Our panel preview pane
   private Dimension previewsize = null ;       // Size of preview pane
   private DefaultMutableTreeNode selectednode = null ; // Selected TREE node
   private Object expandobject = null ;         // Tree object to position
   private ImageIcon runonicon = null ;         // Run state normal on icon
   private ImageIcon runpauseicon = null ;      // Run state will pause icon
   private boolean changed = false ;				// True, event has changed
	private boolean updated = false ;				// True, memory is updated
   private boolean undoredo = false ;				// True, performing an undo

   // Variable table

	private Variable variable = null ;    		  	// The variable object
	private VariableTableData tabledata = null ;	// The table data object
   private int sortcolumn = 0 ; 					  	// The column to sort
	private boolean ascending = true ;			  	// The sort direction

	// User interface objects

	private JPanel panel1 = new JPanel() ;
	private JPanel panel2 = new JPanel() ;
	private JPanel panel3 = new JPanel() ;
	private JPanel panel4 = new JPanel() ;
   private JButton PROPERTIES = new JButton() ;
   private JLabel label1 = new JLabel() ;
	private JSplitPane leftside = new JSplitPane();
	private JSplitPane mainsplit = new JSplitPane();
	private JScrollPane treescroll = new JScrollPane();
	private JScrollPane vblscroll = new JScrollPane();
	private JTable TABLE = new JTable();
   private JTree TREE = new JTree() ;
	private DefaultMutableTreeNode top = null ;
	private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private Border eb2 = BorderFactory.createEmptyBorder(5,5,5,5) ;
	private Border cb1 = new CompoundBorder(eb2,BorderFactory.createRaisedBevelBorder()) ;
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

	// Undo helpers

	private UndoManager undo = new UndoManager() ;
	private UndoAction undoAction ;					// Action for undo
	private RedoAction redoAction ;					// Action for redo
   private TreeFindDialog findDialog = null ;   // Find dialog

	// Menu items

   private JMenu fileMenu ;
   private JMenu editMenu ;
	private JMenu runMenu ;
	private JMenu optionMenu ;
	private JMenu windowMenu ;
	private JMenu helpMenu ;
	private JMenu newmenu ;
	private JMenu newfile ;
	private JMenuItem newevent ;
	private JMenuItem editevent ;
	private JMenuItem deleteevent ;
	private JMenuItem editconfig ;
   private JMenuItem eventwiz ;
	private JMenuItem cut ;
	private JMenuItem copy ;
	private JMenuItem paste ;
	private JMenuItem undoall ;
	private JMenuItem find ;
	private JMenuItem print ;
   private JMenuItem printpreview ;
   private JMenuItem pagesetup ;
   private JMenuItem properties ;
	private JMenuItem exit ;
	private JMenuItem help ;
	private JMenuItem refhelp ;
	private JMenuItem about ;
	private JMenuItem execute ;
	private JMenuItem stepinto ;
	private JMenuItem runtosel ;
	private JMenuItem runtoend ;
	private JMenuItem runtocall ;
	private JMenuItem setbreak ;
	private JMenuItem clearbreak ;
	private JMenuItem setnobreak ;
	private JMenuItem clearnobreak ;
	private JMenuItem clearall ;
	private JMenuItem showall ;
	private JMenuItem restart ;
	private JMenuItem pause ;
	private JMenuItem resume ;
	private JCheckBoxMenuItem eventpause ;
	private JCheckBoxMenuItem actionpause ;
	private JCheckBoxMenuItem disableall ;

	// Toolbar interface objects

	private JToolBar toolbar = null ;
	private JButton NEW = null ;
	private JButton EDIT = null ;
	private JButton CUT = null ;
	private JButton COPY = null ;
	private JButton PASTE = null ;
	private JButton UNDO = null ;
	private JButton REDO = null ;
	private JButton PAUSE = null ;
	private JButton RESUME = null ;
	private JButton EXECUTE = null ;
	private JButton STEPINTO = null ;
	private JButton RUNTOSEL = null ;
	private JButton RUNTOEND = null ;
	private JButton RUNTOCALL = null ;
	private JButton SETBREAK = null ;
	private JButton CLEARBREAK = null ;
	private JButton SETNOBREAK = null ;
	private JButton CLEARNOBREAK = null ;
	private JButton CLEARALL = null ;
	private JButton RUNSTATE = null ;
	private JButton SHOWALL = null ;

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the palette file is
   // saved.

	protected CallbackButton callback = new CallbackButton(this,"FKissFrame Callback") ;

   // Print references.

   private PageFormat pageformat = null ;		// The current page format


	// Register for events.

   MouseListener columnListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
      	if (TABLE == null) return ;
         if (tabledata == null) return ;
        	TableColumnModel columnmodel = TABLE.getColumnModel() ;
         int columnindex = columnmodel.getColumnIndexAtX(e.getX()) ;
         int modelindex = columnmodel.getColumn(columnindex).getModelIndex() ;

         if (modelindex < 0) return ;
         if (sortcolumn == modelindex)
         	ascending = !ascending ;
         else
           	sortcolumn = modelindex ;

         for (int i = 0 ; i < tabledata.columns.length ; i++)
         {
           	TableColumn column = columnmodel.getColumn(i) ;
            column.setHeaderValue(tabledata.getColumnName(column.getModelIndex())) ;
         }

         TABLE.getTableHeader().repaint() ;

         Vector v = tabledata.getData() ;
         Collections.sort(v,new SortComparator(modelindex,ascending)) ;
         TABLE.tableChanged(new TableModelEvent(tabledata)) ;
         TABLE.repaint() ;
      }
   } ;


	// Create specialized listeners for events.

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
		public void valueChanged(TreeSelectionEvent e)
		{
         PROPERTIES.setToolTipText(Kisekae.getCaptions().getString("ToolTipConfigurationButton"));
			selectednode = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
         if (selectednode == null) return ;
         Object o = selectednode.getUserObject() ;
         if ((o instanceof FKissEvent) || (o instanceof FKissAction))
            PROPERTIES.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewEventButton"));
         setValues() ;
   	}
   } ;


   // Listener to define the tree right click event handler.

   MouseListener treeBreakpoint = new MouseListener()
   {
      public void mouseReleased(MouseEvent e)
      {
         int x = e.getX() ;
         int y = e.getY() ;
         TreePath path = TREE.getPathForLocation(x,y) ;
         if (path == null) return ;
         Object o = path.getLastPathComponent() ;
         if (!(o instanceof DefaultMutableTreeNode)) return ;
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) o ;
         o = node.getUserObject() ;
         
         // Right button clicks set and clear breakpoints.
         
         if (SwingUtilities.isRightMouseButton(e)) 
         {
            if (o instanceof FKissEvent)
            {
               FKissEvent event = (FKissEvent) o ;
               event.setBreakpoint(!event.getBreakpoint());
            }
            else if (o instanceof FKissAction)
            {
               FKissAction action = (FKissAction) o ;
               action.setBreakpoint(!action.getBreakpoint());
            }

            // Update our menu items if we adjusted the selected entry.

            if (selectednode != null)
            {
               o = selectednode.getUserObject() ;
               if (o instanceof FKissEvent)
               {
                  FKissEvent evt = (FKissEvent) o ;
                  clearbreak.setEnabled(evt.getBreakpoint()) ;
                  CLEARBREAK.setEnabled(evt.getBreakpoint()) ;
                  setbreak.setEnabled(!clearbreak.isEnabled()) ;
                  SETBREAK.setEnabled(!clearbreak.isEnabled()) ;
                  clearnobreak.setEnabled(false) ;
                  CLEARNOBREAK.setEnabled(false) ;
                  setnobreak.setEnabled(true) ;
                  SETNOBREAK.setEnabled(true) ;
               }
               else if (o instanceof FKissAction)
               {
                  FKissAction act = (FKissAction) o ;
                  clearbreak.setEnabled(act.getBreakpoint()) ;
                  CLEARBREAK.setEnabled(act.getBreakpoint()) ;
                  setbreak.setEnabled(!clearbreak.isEnabled()) ;
                  SETBREAK.setEnabled(!clearbreak.isEnabled()) ;
                  clearnobreak.setEnabled(false) ;
                  CLEARNOBREAK.setEnabled(false) ;
                  setnobreak.setEnabled(true) ;
                  SETNOBREAK.setEnabled(true) ;
               }
            }
            clearall.setEnabled(isBreakpointSet()) ;
            CLEARALL.setEnabled(clearall.isEnabled()) ;
            TREE.repaint() ;
            return ;
         }
         
         if (SwingUtilities.isMiddleMouseButton(e))
         {
            if (o instanceof FKissEvent)
            {
               FKissEvent event = (FKissEvent) o ;
               event.setNoBreakpoint(!event.getNoBreakpoint());
            }
            else if (o instanceof FKissAction)
            {
               FKissAction action = (FKissAction) o ;
               FKissEvent event = action.getEvent() ;
               event.setNoBreakpoint(!event.getNoBreakpoint());
            }
            else if (o instanceof String)
            {
               setDisableAll(OptionsDialog.getDisableAll(),node) ;
               OptionsDialog.setDisableAll(!OptionsDialog.getDisableAll()) ;
            }

            // Update our menu items if we adjusted the selected entry.

            if (selectednode != null)
            {
               o = selectednode.getUserObject() ;
               if (o instanceof FKissEvent)
               {
                  FKissEvent evt = (FKissEvent) o ;
                  clearbreak.setEnabled(false) ;
                  CLEARBREAK.setEnabled(false) ;
                  setbreak.setEnabled(false) ;
                  SETBREAK.setEnabled(false) ;
                  clearnobreak.setEnabled(evt.getNoBreakpoint()) ;
                  CLEARNOBREAK.setEnabled(evt.getNoBreakpoint()) ;
                  setnobreak.setEnabled(!clearnobreak.isEnabled()) ;
                  SETNOBREAK.setEnabled(!clearnobreak.isEnabled()) ;
               }
               else if (o instanceof FKissAction)
               {
                  FKissAction act = (FKissAction) o ;
                  clearbreak.setEnabled(false) ;
                  CLEARBREAK.setEnabled(false) ;
                  setbreak.setEnabled(false) ;
                  SETBREAK.setEnabled(false) ;
                  clearnobreak.setEnabled(act.getBreakpoint()) ;
                  CLEARNOBREAK.setEnabled(act.getBreakpoint()) ;
                  setnobreak.setEnabled(!clearnobreak.isEnabled()) ;
                  SETNOBREAK.setEnabled(!clearnobreak.isEnabled()) ;
               }
            }
            clearall.setEnabled(isBreakpointSet()) ;
            CLEARALL.setEnabled(clearall.isEnabled()) ;
            TREE.repaint() ;
            return ;
         }
         
         // Ctrl clicks show the event editor.
         
         if (e.isControlDown())
			{
            editEvent(o) ;
   			return ;
   		}
      }

      // Double click on a tree action node image brings up the EventDialog.
      // Tree event nodes expand on double click.

		public void mouseClicked(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e)) return ;
        	if (e.getClickCount() == 2)
         {
				DefaultMutableTreeNode node = null ;
            
            Object o = e.getSource() ;            
            if (o == TREE)
               node = (DefaultMutableTreeNode) TREE.getLastSelectedPathComponent();
				if (node == null) return;

            // Invoke the appropriate event or action dialog.

				o = node.getUserObject();
            if (o instanceof FKissAction)
            {
					EventDialog ed = new EventDialog(me,o,config) ;
					ed.show() ;
            }
			}
		}

      public void mousePressed(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
   } ;


	// Default constructor for manual invokations.  This constructor is used
   // when we wish to examine and edit the complete configuration FKiSS code.

	public FKissFrame(Configuration c) { this(c,null,null) ; }


	// Constructor for KiSS object invokations.  This constructor is used
   // when we wish to examine and edit the FKiSS configuration for an object.

	public FKissFrame(Configuration c, KissObject k, Object o)
   {
		super(Kisekae.getCaptions().getString("FKissEditorTitle")) ;
		me = this ;
		config = c ;
      kiss = k ;
      event = null ;
      action = null ;
      expandobject = o ;

      // Initialize the frame.

		init() ;

      // Register for MainFrame callbacks that signal a configuration
      // update.

      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.callback.addActionListener(this) ;
	}


	// Constructor for KiSS object invokations with Wizard.  This constructor is 
   // used to invoke the event wizard when we wish to edit the FKiSS events
   // for an object. The wizard parent frame can be specified.

	public FKissFrame(Configuration c, KissObject k, Object o, JFrame wizard)
   {
      this(c,k,o) ;
      if (wizard == null) return ;
      EventWizard ne = new EventWizard(wizard,c,k,false) ;
      ne.callback.addActionListener(this) ;
      ne.show() ;
	}


	// Constructor for breakpoint events.  This constructor is used
   // when we have a breakpoint recognized for a specific object.

	public FKissFrame(FKissEvent caller, Configuration c, Object o)
   {
		super(Kisekae.getCaptions().getString("FKissEditorTitle")) ;
   	me = this ;
		config = c ;
      invoker = caller ;
      expandobject = o ;

      // Establish the breakpoint event and action.

      if (o instanceof FKissEvent)
      {
         action = null ;
         event = (FKissEvent) o ;
      }
      if (o instanceof FKissAction)
      {
         action = (FKissAction) o ;
         event = action.getEvent() ;
      }
      if (OptionsDialog.getDebugFKiss())
      {
         System.out.println("FKissFrame: new breakpoint event  " + event) ;
         System.out.println("FKissFrame: new breakpoint action " + action) ;
      }

      // Initialize the frame.

		init() ;

      // Register for MainFrame callbacks that signal a configuration
      // update.

      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.callback.addActionListener(this) ;
	}


   // Frame initialization.

	private void init()
	{
		setTitle(Kisekae.getCaptions().getString("FKissEditorTitle")) ;
		setIconImage(Kisekae.getIconImage()) ;
      variable = (config != null) ? config.getVariable() : null ;
      boolean applemac = OptionsDialog.getAppleMac() ;

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      {
      	helper = new HelpLoader(this,helpset,helpsection) ;
      	helper2 = new HelpLoader(this,refset,refsection) ;
      }

		// Set up the menu bar.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
      if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		String s = System.getProperty("java.version") ;
		int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
		Insets insets = new Insets(2,2,2,rm) ;
		fileMenu.setMargin(insets) ;
		fileMenu.add((eventwiz = new JMenuItem(Kisekae.getCaptions().getString("EventWizardMessage")))) ;
      if (!applemac) eventwiz.setMnemonic(KeyEvent.VK_W) ;
      eventwiz.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, accelerator));
		eventwiz.addActionListener(this) ;
		fileMenu.add((newevent = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewEvent")))) ;
      if (!applemac) newevent.setMnemonic(KeyEvent.VK_N) ;
      newevent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelerator));
		newevent.addActionListener(this) ;
		fileMenu.add((editevent = new JMenuItem(Kisekae.getCaptions().getString("MenuFileEditEvent")))) ;
      if (!applemac) editevent.setMnemonic(KeyEvent.VK_E) ;
      editevent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelerator));
		editevent.addActionListener(this) ;
		editevent.setEnabled(false) ;
		fileMenu.add((deleteevent = new JMenuItem(Kisekae.getCaptions().getString("MenuFileDeleteEvent")))) ;
      if (!applemac) deleteevent.setMnemonic(KeyEvent.VK_D) ;
      deleteevent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelerator));
		deleteevent.addActionListener(this) ;
		deleteevent.setEnabled(false) ;
		fileMenu.addSeparator() ;
		fileMenu.add((editconfig = new JMenuItem(Kisekae.getCaptions().getString("MenuFileConfig")))) ;
		editconfig.addActionListener(this) ;
		editconfig.setEnabled(false) ;
		fileMenu.addSeparator() ;
		fileMenu.add((pagesetup = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePageSetup")))) ;
		pagesetup.addActionListener(this) ;
      if (!applemac) pagesetup.setMnemonic(KeyEvent.VK_U) ;
      pagesetup.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure()) ;
		fileMenu.add((printpreview = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrintPreview")))) ;
		printpreview.addActionListener(this) ;
      if (!applemac) printpreview.setMnemonic(KeyEvent.VK_V) ;
      printpreview.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure()) ;
		fileMenu.add((print = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrint")))) ;
		print.addActionListener(this) ;
      if (!applemac) print.setMnemonic(KeyEvent.VK_P) ;
      print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelerator));
      print.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((properties = new JMenuItem(Kisekae.getCaptions().getString("MenuFileProperties")))) ;
		properties.addActionListener(this) ;
		fileMenu.addSeparator();
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileFKissEditQuit" : "MenuFileFKissEditExit" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;
      
		editMenu = createEditMenu() ;
		editMenu.setMargin(insets) ;
      if (!applemac) editMenu.setMnemonic(KeyEvent.VK_E) ;
		mb.add(editMenu) ;

		runMenu = new JMenu(Kisekae.getCaptions().getString("MenuRun")) ;
		runMenu.setMargin(insets) ;
      if (!applemac) runMenu.setMnemonic(KeyEvent.VK_R) ;
		mb.add(runMenu);
		runMenu.add((execute = new JMenuItem(Kisekae.getCaptions().getString("MenuRunExecute")))) ;
      if (!applemac) execute.setMnemonic(KeyEvent.VK_E) ;
		execute.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0)) ;
		execute.addActionListener(this) ;
		execute.setEnabled(false) ;
		runMenu.add((stepinto = new JMenuItem(Kisekae.getCaptions().getString("MenuRunTraceInto")))) ;
      if (!applemac) stepinto.setMnemonic(KeyEvent.VK_T) ;
		stepinto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0)) ;
		stepinto.addActionListener(this) ;
		stepinto.setEnabled(false) ;
		runMenu.add((runtosel = new JMenuItem(Kisekae.getCaptions().getString("MenuRunToSelection")))) ;
      if (!applemac) runtosel.setMnemonic(KeyEvent.VK_S) ;
		runtosel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0)) ;
		runtosel.addActionListener(this) ;
		runtosel.setEnabled(false) ;
		runMenu.add((runtoend = new JMenuItem(Kisekae.getCaptions().getString("MenuRunToReturn")))) ;
      if (!applemac) runtoend.setMnemonic(KeyEvent.VK_R) ;
		runtoend.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0)) ;
		runtoend.addActionListener(this) ;
		runtoend.setEnabled(false) ;
		runMenu.add((runtocall = new JMenuItem(Kisekae.getCaptions().getString("MenuRunToCall")))) ;
      if (!applemac) runtoend.setMnemonic(KeyEvent.VK_C) ;
		runtocall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0)) ;
		runtocall.addActionListener(this) ;
		runtocall.setEnabled(false) ;
		runMenu.addSeparator() ;
		runMenu.add((setbreak = new JMenuItem(Kisekae.getCaptions().getString("MenuRunSetBreak")))) ;
		setbreak.addActionListener(this) ;
		setbreak.setEnabled(false) ;
		runMenu.add((clearbreak = new JMenuItem(Kisekae.getCaptions().getString("MenuRunClearBreak")))) ;
		clearbreak.addActionListener(this) ;
		clearbreak.setEnabled(false) ;
		runMenu.add((setnobreak = new JMenuItem(Kisekae.getCaptions().getString("MenuRunDisableBreak")))) ;
		setnobreak.addActionListener(this) ;
		setnobreak.setEnabled(false) ;
		runMenu.add((clearnobreak = new JMenuItem(Kisekae.getCaptions().getString("MenuRunEnableBreak")))) ;
		clearnobreak.addActionListener(this) ;
		clearnobreak.setEnabled(false) ;
		runMenu.add((clearall = new JMenuItem(Kisekae.getCaptions().getString("MenuRunClearAllBreak")))) ;
		clearall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0)) ;
		clearall.addActionListener(this) ;
		clearall.setEnabled(false) ;
		runMenu.addSeparator() ;
		runMenu.add((pause = new JMenuItem(Kisekae.getCaptions().getString("MenuRunPause")))) ;
		pause.addActionListener(this) ;
		pause.setEnabled(false) ;
		runMenu.add((resume = new JMenuItem(Kisekae.getCaptions().getString("MenuRunResume")))) ;
		resume.addActionListener(this) ;
		resume.setEnabled(false) ;
		runMenu.add((restart = new JMenuItem(Kisekae.getCaptions().getString("MenuRunRestart")))) ;
		restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8,0)) ;
		restart.addActionListener(this) ;

		optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
		optionMenu.setMargin(insets) ;
      if (!applemac) optionMenu.setMnemonic(KeyEvent.VK_O) ;
		mb.add(optionMenu);
		optionMenu.add((eventpause = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsEventPause")))) ;
		eventpause.addItemListener(this) ;
      if (!applemac) eventpause.setMnemonic(KeyEvent.VK_E) ;
		eventpause.setSelected(OptionsDialog.getEventPause()) ;
		optionMenu.add((actionpause = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsActionPause")))) ;
		actionpause.addItemListener(this) ;
      if (!applemac) actionpause.setMnemonic(KeyEvent.VK_A) ;
		actionpause.setSelected(OptionsDialog.getActionPause()) ;
      ButtonGroup bgfkpause = new ButtonGroup() ;
      bgfkpause.add(eventpause) ;
      bgfkpause.add(actionpause) ;
		optionMenu.add((disableall = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsDisableAll")))) ;
		disableall.addItemListener(this) ;
		disableall.setSelected(OptionsDialog.getDisableAll()) ;
      
		windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		windowMenu.setMargin(insets) ;
      if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
		mb.add(windowMenu) ;
      
		// Create the Help menu and About dialog.

      aboutdialog = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
		helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
      if (!applemac) helpMenu.setMnemonic(KeyEvent.VK_H) ;
		helpMenu.setMargin(insets) ;
		mb.add(helpMenu);
//		helpMenu.add((help = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpContents")))) ;
//    if (!applemac) help.setMnemonic(KeyEvent.VK_C) ;
//    help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0)) ;
//		help.setEnabled(helper != null && helper.isLoaded()) ;
//    if (helper != null) helper.addActionListener(help) ;
//    if (!Kisekae.isHelpInstalled()) help.addActionListener(this) ;
//    if (!Kisekae.isHelpInstalled()) help.setEnabled(true) ;
		helpMenu.add((refhelp = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpFKiss")))) ;
		refhelp.setEnabled(helper2 != null && helper2.isLoaded()) ;
      if (!applemac) refhelp.setMnemonic(KeyEvent.VK_F) ;
      if (helper2 != null) helper2.addActionListener(refhelp) ;
		helpMenu.addSeparator() ;
		helpMenu.add((about = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpAbout")))) ;
		about.addActionListener(this);
      if (!applemac) about.setMnemonic(KeyEvent.VK_A) ;
		setJMenuBar(mb) ;

		// Create the tool bar.

      String ext = ".gif" ;
      if (OptionsDialog.getAppleMac()) ext = ".png" ;
		URL iconfile = null ;
		toolbar = new JToolBar() ;
		toolbar.setFloatable(false) ;
		NEW = new JButton() ;
		iconfile = Kisekae.getResource("Images/new" + ext) ;
		if (iconfile != null) NEW.setIcon(new ImageIcon(iconfile)) ;
		NEW.setMargin(new Insets(1,1,1,1)) ;
		NEW.setAlignmentY(0.5f) ;
		NEW.addActionListener(this) ;
		NEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipNewEvent")) ;
		EDIT = new JButton() ;
		iconfile = Kisekae.getResource("Images/view" + ext) ;
		if (iconfile != null) EDIT.setIcon(new ImageIcon(iconfile)) ;
		EDIT.setMargin(new Insets(1,1,1,1)) ;
		EDIT.setAlignmentY(0.5f) ;
		EDIT.addActionListener(this) ;
		EDIT.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEvent"));
		EDIT.setEnabled(false);
		toolbar.add(NEW, null);
		toolbar.add(EDIT, null);

		// Create the undo and redo operations.

		UNDO = new JButton() ;
		iconfile = Kisekae.getResource("Images/undo" + ext) ;
		if (iconfile != null) UNDO.setIcon(new ImageIcon(iconfile)) ;
		UNDO.setMargin(new Insets(1,1,1,1)) ;
		UNDO.setAlignmentY(0.5f) ;
		UNDO.addActionListener(this) ;
		UNDO.setEnabled(false) ;
		UNDO.setToolTipText(Kisekae.getCaptions().getString("ToolTipUndo"));
		REDO = new JButton() ;
		iconfile = Kisekae.getResource("Images/redo" + ext) ;
		if (iconfile != null) REDO.setIcon(new ImageIcon(iconfile)) ;
		REDO.setMargin(new Insets(1,1,1,1)) ;
		REDO.setAlignmentY(0.5f) ;
		REDO.addActionListener(this) ;
		REDO.setEnabled(false) ;
		REDO.setToolTipText(Kisekae.getCaptions().getString("ToolTipRedo"));
		toolbar.addSeparator() ;
		toolbar.add(UNDO, null);
		toolbar.add(REDO, null);

		// Create the run operations.

		EXECUTE = new JButton() ;
		iconfile = Kisekae.getResource("Images/execute" + ext) ;
		if (iconfile != null) EXECUTE.setIcon(new ImageIcon(iconfile)) ;
		EXECUTE.setMargin(new Insets(1,1,1,1)) ;
		EXECUTE.setAlignmentY(0.5f) ;
		EXECUTE.addActionListener(this) ;
		EXECUTE.setEnabled(false) ;
		EXECUTE.setToolTipText(Kisekae.getCaptions().getString("ToolTipExecute"));
		STEPINTO = new JButton() ;
		iconfile = Kisekae.getResource("Images/stepinto" + ext) ;
		if (iconfile != null) STEPINTO.setIcon(new ImageIcon(iconfile)) ;
		STEPINTO.setMargin(new Insets(1,1,1,1)) ;
		STEPINTO.setAlignmentY(0.5f) ;
		STEPINTO.addActionListener(this) ;
		STEPINTO.setEnabled(false) ;
		STEPINTO.setToolTipText(Kisekae.getCaptions().getString("ToolTipStepInto"));
		RUNTOSEL = new JButton() ;
		iconfile = Kisekae.getResource("Images/runtosel" + ext) ;
		if (iconfile != null) RUNTOSEL.setIcon(new ImageIcon(iconfile)) ;
		RUNTOSEL.setMargin(new Insets(1,1,1,1)) ;
		RUNTOSEL.setAlignmentY(0.5f) ;
		RUNTOSEL.addActionListener(this) ;
		RUNTOSEL.setEnabled(false) ;
		RUNTOSEL.setToolTipText(Kisekae.getCaptions().getString("ToolTipRunTo"));
		RUNTOEND = new JButton() ;
		iconfile = Kisekae.getResource("Images/runtoend" + ext) ;
		if (iconfile != null) RUNTOEND.setIcon(new ImageIcon(iconfile)) ;
		RUNTOEND.setMargin(new Insets(1,1,1,1)) ;
		RUNTOEND.setAlignmentY(0.5f) ;
		RUNTOEND.addActionListener(this) ;
		RUNTOEND.setEnabled(false) ;
		RUNTOEND.setToolTipText(Kisekae.getCaptions().getString("ToolTipRunToEnd"));
		RUNTOCALL = new JButton() ;
		iconfile = Kisekae.getResource("Images/runtocall" + ext) ;
		if (iconfile != null) RUNTOCALL.setIcon(new ImageIcon(iconfile)) ;
		RUNTOCALL.setMargin(new Insets(1,1,1,1)) ;
		RUNTOCALL.setAlignmentY(0.5f) ;
		RUNTOCALL.addActionListener(this) ;
		RUNTOCALL.setEnabled(false) ;
		RUNTOCALL.setToolTipText(Kisekae.getCaptions().getString("ToolTipRunToCall"));
		toolbar.addSeparator() ;
		toolbar.add(EXECUTE,null);
		toolbar.add(STEPINTO,null);
		toolbar.add(RUNTOSEL,null);
		toolbar.add(RUNTOEND,null);
		toolbar.add(RUNTOCALL,null);
		SETBREAK = new JButton() ;
		iconfile = Kisekae.getResource("Images/setbreak" + ext) ;
		if (iconfile != null) SETBREAK.setIcon(new ImageIcon(iconfile)) ;
		SETBREAK.setMargin(new Insets(1,1,1,1)) ;
		SETBREAK.setAlignmentY(0.5f) ;
		SETBREAK.addActionListener(this) ;
		SETBREAK.setEnabled(false) ;
		SETBREAK.setToolTipText(Kisekae.getCaptions().getString("ToolTipSetBreak"));
		CLEARBREAK = new JButton() ;
		iconfile = Kisekae.getResource("Images/clrbreak" + ext) ;
		if (iconfile != null) CLEARBREAK.setIcon(new ImageIcon(iconfile)) ;
		CLEARBREAK.setMargin(new Insets(1,1,1,1)) ;
		CLEARBREAK.setAlignmentY(0.5f) ;
		CLEARBREAK.addActionListener(this) ;
		CLEARBREAK.setEnabled(false) ;
		CLEARBREAK.setToolTipText(Kisekae.getCaptions().getString("ToolTipClearBreak"));
		SETNOBREAK = new JButton() ;
		iconfile = Kisekae.getResource("Images/setnobreak" + ext) ;
		if (iconfile != null) SETNOBREAK.setIcon(new ImageIcon(iconfile)) ;
		SETNOBREAK.setMargin(new Insets(1,1,1,1)) ;
		SETNOBREAK.setAlignmentY(0.5f) ;
		SETNOBREAK.addActionListener(this) ;
		SETNOBREAK.setEnabled(false) ;
		SETNOBREAK.setToolTipText(Kisekae.getCaptions().getString("ToolTipDisableBreak"));
		CLEARNOBREAK = new JButton() ;
		iconfile = Kisekae.getResource("Images/clrnobreak" + ext) ;
		if (iconfile != null) CLEARNOBREAK.setIcon(new ImageIcon(iconfile)) ;
		CLEARNOBREAK.setMargin(new Insets(1,1,1,1)) ;
		CLEARNOBREAK.setAlignmentY(0.5f) ;
		CLEARNOBREAK.addActionListener(this) ;
		CLEARNOBREAK.setEnabled(false) ;
		CLEARNOBREAK.setToolTipText(Kisekae.getCaptions().getString("ToolTipEnableBreak"));
		CLEARALL = new JButton() ;
		iconfile = Kisekae.getResource("Images/clearall" + ext) ;
		if (iconfile != null) CLEARALL.setIcon(new ImageIcon(iconfile)) ;
		CLEARALL.setMargin(new Insets(1,1,1,1)) ;
		CLEARALL.setAlignmentY(0.5f) ;
		CLEARALL.addActionListener(this) ;
		CLEARALL.setEnabled(false) ;
		CLEARALL.setToolTipText(Kisekae.getCaptions().getString("ToolTipClearAllBreak"));
		toolbar.addSeparator() ;
		toolbar.add(SETBREAK,null);
		toolbar.add(CLEARBREAK,null);
		toolbar.add(SETNOBREAK,null);
		toolbar.add(CLEARNOBREAK,null);
		toolbar.add(CLEARALL,null);

		// Create the Show All Event button.

		SHOWALL = new JButton() ;
      SHOWALL.setText(Kisekae.getCaptions().getString("MenuEditShowAll")) ;
//		iconfile = Kisekae.getResource("Images/pause" + ext) ;
//		if (iconfile != null) SHOWALL.setIcon(new ImageIcon(iconfile)) ;
		SHOWALL.setMargin(new Insets(1,1,1,1)) ;
		SHOWALL.setAlignmentY(0.5f) ;
		SHOWALL.addActionListener(this) ;
		SHOWALL.setEnabled(kiss != null && kiss.getEventCount() > 0) ;
		SHOWALL.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowAll"));
		toolbar.addSeparator() ;
		toolbar.add(SHOWALL,null);
      
      // Create the pause and resume buttons.
      
		PAUSE = new JButton() ;
		iconfile = Kisekae.getResource("Images/pause" + ext) ;
		if (iconfile != null) PAUSE.setIcon(new ImageIcon(iconfile)) ;
		PAUSE.setMargin(new Insets(1,1,1,1)) ;
		PAUSE.setAlignmentY(0.5f) ;
		PAUSE.addActionListener(this) ;
		PAUSE.setEnabled(false) ;
		PAUSE.setToolTipText(Kisekae.getCaptions().getString("ToolTipPause"));
		RESUME = new JButton() ;
		iconfile = Kisekae.getResource("Images/resume" + ext) ;
		if (iconfile != null) RESUME.setIcon(new ImageIcon(iconfile)) ;
		RESUME.setMargin(new Insets(1,1,1,1)) ;
		RESUME.setAlignmentY(0.5f) ;
		RESUME.addActionListener(this) ;
		RESUME.setEnabled(false) ;
		RESUME.setToolTipText(Kisekae.getCaptions().getString("ToolTipResume"));
		toolbar.addSeparator() ;
		toolbar.add(PAUSE, null);
		toolbar.add(RESUME, null);

      // Create the run state indicator.

      RUNSTATE = new JButton() ;
		RUNSTATE.setMargin(new Insets(1,1,1,1)) ;
      RUNSTATE.setRequestFocusEnabled(false) ;
		RUNSTATE.setAlignmentY(0.5f) ;
		RUNSTATE.addActionListener(this) ;
		iconfile = Kisekae.getResource("Images/greenball" + ext) ;
      runonicon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
		if (iconfile != null) RUNSTATE.setIcon(runonicon) ;
		iconfile = Kisekae.getResource("Images/yellowball" + ext) ;
      runpauseicon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
		iconfile = Kisekae.getResource("Images/redball" + ext) ;
		if (iconfile != null) RUNSTATE.setDisabledIcon(new ImageIcon(iconfile)) ;
		toolbar.add(Box.createGlue()) ;
      toolbar.add(RUNSTATE,null) ;

		// Create the default page format.

      if (Kisekae.isPrintInstalled())
      {
			PrinterJob prn = null ;
			try { prn = PrinterJob.getPrinterJob() ; }
			catch (Exception e) { }
			pageformat = (prn != null) ? prn.defaultPage() : null ;
      }

      // Set up the interface split panes.

      leftside = new JSplitPane();
      mainsplit = new JSplitPane();
      treescroll = new JScrollPane();
      vblscroll = new JScrollPane();
      leftside.setOrientation(JSplitPane.VERTICAL_SPLIT) ;
      mainsplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT) ;

      // Create the event tree.  This tree displays tool tips to show
      // the node variable values.  Literal values are not displayed.

      top = new DefaultMutableTreeNode() ;
		TREE = new JTree(top)
      {
         public String getToolTipText(MouseEvent e)
         {
            Vector params = null ;
            if (e == null) return null ;
            if (TREE == null) return null ;
            if (variable == null) return null ;
            TreePath path = TREE.getPathForLocation(e.getX(),e.getY()) ;
            if (path == null) return null ;
            Object o = path.getLastPathComponent() ;
            if (!(o instanceof DefaultMutableTreeNode)) return null ;
            o = ((DefaultMutableTreeNode) o).getUserObject() ;
            if (o instanceof FKissEvent) params = ((FKissEvent) o).getParameters() ;
            if (o instanceof FKissAction) params = ((FKissAction) o).getParameters() ;
            if (params == null) return null ;

            // Remove duplicate parameter names.

            Object source = o ;
            Vector unique = new Vector() ;
            for (int i = 0 ; i < params.size() ; i++)
            {
               o = params.elementAt(i) ;
               if (!(o instanceof String)) continue ;
               if (!(unique.contains(o))) unique.addElement(o) ;
            }

            // Get the parameter values.  The parameter could be a variable.
            // Tooltip variable text is restricted to 25 characters.

            StringBuffer sb = new StringBuffer() ;
            for (int i = 0 ; i < unique.size() ; i++)
            {
               String name = (String) unique.elementAt(i) ;
               o = findGroupOrCel(name) ;
               if (o == null) o = variable.getValue(name,null) ;
               String pv = (o == null)
                   ? Kisekae.getCaptions().getString("UnknownValueText")
                   : o.toString() ;
               if (pv.length() > 25) pv = pv.substring(0,25) + "..." ;
         		sb.append(name) ;
               sb.append("=") ;
               sb.append(pv) ;
               sb.append(" ") ;
            }
            
            // Get the source invoker description for alarm events
            
            if (source instanceof FKissEvent)
            {
               source = ((FKissEvent) source).getParentObject() ;
               if (source instanceof Alarm)
               {
                  source = ((Alarm) source).getSource() ;
                  sb.append("source=" + source) ;
               }
            }
            return sb.toString() ;
         }
      } ;
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer()) ;
      TREE.setShowsRootHandles(true) ;
      ToolTipManager.sharedInstance().registerComponent(TREE) ;
		treescroll.getViewport().add(TREE,null) ;
      buildEventTree(config,kiss) ;
      setDisableAll(disableall.isSelected(),top) ;

      // Create the variable table.

      TABLE = new JTable() ;
      variable = (config != null) ? config.getVariable() : null ;
   	tabledata = new VariableTableData(variable) ;
		TABLE.setAutoCreateColumnsFromModel(false) ;
      TABLE.setCellSelectionEnabled(true) ;
      TABLE.setRowSelectionAllowed(true) ;
      TABLE.setColumnSelectionAllowed(true) ;
		TABLE.setModel(tabledata) ;

		// Add new table columns.  The variable value column can be edited.

      TableCellEditor editor ;
		for (int i = 0 ; i < 2 ; i++)
		{
      	DefaultTableCellRenderer r = new DefaultTableCellRenderer() ;
         r.setHorizontalAlignment(tabledata.columns[i].alignment) ;
         editor = (i == 1) ? new DefaultCellEditor(new JTextField()) : null ;
         TableColumn column = new TableColumn(i,tabledata.columns[i].width,r,editor) ;
         TABLE.addColumn(column) ;
      }

      // Create the object properties panel.

      label1 = new JLabel() ;
      panel3 = new JPanel() ;
      panel4 = new JPanel() ;
      Object id = (config != null) ? config.getIdentifier() : null ;
      String name = (id != null) ? id.toString() : "" ;
      label1.setText((config == null) ? name : config.getName()) ;
      label1.setHorizontalAlignment(JLabel.CENTER) ;
      panel3.setLayout(new BorderLayout()) ;
      panel3.add(label1,BorderLayout.NORTH) ;
		panel4.setBorder(eb1);
 		panel4.setLayout(new BoxLayout(panel4,BoxLayout.X_AXIS));
      panel4.add(Box.createGlue()) ;
      PROPERTIES = new JButton(Kisekae.getCaptions().getString("PropertiesMessage")) ;
      PROPERTIES.setToolTipText(Kisekae.getCaptions().getString("ToolTipConfigurationButton"));
      panel4.add(PROPERTIES,null) ;
      panel4.add(Box.createGlue()) ;
      panel3.add(panel4,BorderLayout.SOUTH) ;
      panel3.add(vblscroll,BorderLayout.CENTER) ;

		// Create the user interface.

		Container c = getContentPane() ;
		panel1 = new JPanel() ;
		panel1.setLayout(new BorderLayout()) ;
		panel1.setBorder(eb1) ;
		c.add(toolbar,BorderLayout.NORTH);
		c.add(panel1,BorderLayout.CENTER);
      panel1.add(mainsplit,BorderLayout.CENTER) ;
      mainsplit.setLeftComponent(leftside) ;
		mainsplit.setRightComponent(treescroll);
      leftside.setBottomComponent(panel3);
      preview = new ImagePreview() ;
      panel2 = new JPanel() ;
		panel2.setLayout(new BorderLayout()) ;
      panel2.setBorder(cb1) ;
      panel2.add(preview,BorderLayout.CENTER) ;
      leftside.setTopComponent(panel2);
		vblscroll.getViewport().add(TABLE,null);

		// Size the frame for the window space.

      super.open() ;
		doLayout() ;
		validate() ;
      if (helper != null) helper.setSize(getSize());
      if (helper2 != null) helper2.setSize(getSize());

		// Set up the split pane sizes.

      mainsplit.setDividerLocation(0.33) ;
      leftside.setDividerLocation(0.33) ;
      Dimension d1 = leftside.getSize() ;
      Dimension d2 = mainsplit.getSize() ;
      insets = panel2.getInsets() ;
      int w = (int) (d2.width * 0.33) - mainsplit.getDividerSize() ;
      int h = (int) (d1.height * 0.33) - leftside.getDividerSize() ;
      w -= (insets.left + insets.right) ;
      h -= (insets.top + insets.bottom) ;
      previewsize = new Dimension(w,h) ;
      preview.setSize(previewsize) ;
      preview.setPreferredSize(previewsize) ;
      preview.setShowState(false) ;
      JTableHeader header = TABLE.getTableHeader() ;
		header.setUpdateTableInRealTime(true) ;
		header.addMouseListener(columnListener) ;
      updateFrame() ;
		setValues() ;

      // Add required listeners.

		RUNSTATE.setBorder(BorderFactory.createEmptyBorder(0,5,0,5)) ;
      TREE.addTreeSelectionListener(treeListener) ;
      TREE.addMouseListener(treeBreakpoint) ;
      PROPERTIES.addActionListener(this) ;
		addWindowListener(this) ;
	}


	// A utility method to create the edit menu.

	private JMenu createEditMenu()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuEdit")) ;

		// Undo and redo are actions of our own creation.

		undoAction = new UndoAction() ;
		JMenuItem undo = menu.add(undoAction) ;
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelerator));
      if (!applemac) undo.setMnemonic(KeyEvent.VK_U) ;
		redoAction = new RedoAction() ;
		JMenuItem redo = menu.add(redoAction) ;
      redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelerator));
      if (!applemac) redo.setMnemonic(KeyEvent.VK_R) ;
		menu.add((undoall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUndoAll")))) ;
		undoall.setEnabled(false) ;
		undoall.addActionListener(this);
      menu.addSeparator() ;
		menu.add((find = new JMenuItem(Kisekae.getCaptions().getString("MenuEditFind")))) ;
		find.addActionListener(this);      
		menu.add((showall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditShowAll")))) ;
		showall.addActionListener(this);    
      showall.setEnabled(kiss != null && kiss.getEventCount() > 0) ;
		return menu;
	}


	// Method to set the dialog field values.  This method sets the frame
   // title and adjusts the menu item state to reflect the current context.

	private void setValues()
	{
		setTitle() ;
      editconfig.setEnabled(config != null) ;
  		editevent.setEnabled(false) ;
  		deleteevent.setEnabled(false) ;
  		EDIT.setEnabled(false) ;
      clearall.setEnabled(isBreakpointSet()) ;
      CLEARALL.setEnabled(clearall.isEnabled()) ;
      setbreak.setEnabled(false) ;
      SETBREAK.setEnabled(false) ;
      clearbreak.setEnabled(false) ;
      CLEARBREAK.setEnabled(false) ;
      setnobreak.setEnabled(false) ;
      SETNOBREAK.setEnabled(false) ;
      clearnobreak.setEnabled(false) ;
      CLEARNOBREAK.setEnabled(false) ;

      // Set our operational run state indicators.
      // Ensure this runs on the AWT thread.

      Runnable setOperationalState = new Runnable()
			{ public void run() {updateRunState() ; }	} ;
      javax.swing.SwingUtilities.invokeLater(setOperationalState);


      // Enable the debug menu options if we are breakpointed.

      runtosel.setEnabled(false) ;
      RUNTOSEL.setEnabled(false) ;
      execute.setEnabled(event != null) ;
      EXECUTE.setEnabled(event != null) ;
      stepinto.setEnabled(event != null) ;
      STEPINTO.setEnabled(event != null) ;
      runtoend.setEnabled(event != null) ;
      RUNTOEND.setEnabled(event != null) ;
      runtocall.setEnabled(event != null) ;
      RUNTOCALL.setEnabled(event != null) ;

      // Update the active object name.

      if (selectednode != null)
      {
         Object o = selectednode.getUserObject() ;
         if (o instanceof FKissEvent)
         {
            FKissEvent evt = (FKissEvent) o ;
            label1.setText(evt.getName()) ;
      		editevent.setEnabled(true) ;
      		deleteevent.setEnabled(true) ;
      		EDIT.setEnabled(true) ;
            clearbreak.setEnabled(evt.getBreakpoint()) ;
            CLEARBREAK.setEnabled(evt.getBreakpoint()) ;
            setbreak.setEnabled(!clearbreak.isEnabled()) ;
            SETBREAK.setEnabled(!clearbreak.isEnabled()) ;
            clearnobreak.setEnabled(evt.getNoBreakpoint()) ;
            CLEARNOBREAK.setEnabled(evt.getNoBreakpoint()) ;
            setnobreak.setEnabled(!clearnobreak.isEnabled()) ;
            SETNOBREAK.setEnabled(!clearnobreak.isEnabled()) ;
         }
         else if (o instanceof FKissAction)
         {
            FKissAction act = (FKissAction) o ;
            label1.setText(act.getName()) ;
      		editevent.setEnabled(true) ;
      		deleteevent.setEnabled(true) ;
      		EDIT.setEnabled(true) ;
            clearbreak.setEnabled(act.getBreakpoint()) ;
            CLEARBREAK.setEnabled(act.getBreakpoint()) ;
            setbreak.setEnabled(!clearbreak.isEnabled()) ;
            SETBREAK.setEnabled(!clearbreak.isEnabled()) ;
            clearnobreak.setEnabled(act.getNoBreakpoint()) ;
            CLEARNOBREAK.setEnabled(act.getNoBreakpoint()) ;
            setnobreak.setEnabled(!clearnobreak.isEnabled()) ;
            SETNOBREAK.setEnabled(!clearnobreak.isEnabled()) ;
            runtosel.setEnabled(event != null && o != action) ;
            RUNTOSEL.setEnabled(event != null && o != action) ;
         }
         else
         {
            label1.setText(config.getName()) ;
            editevent.setEnabled(false) ;
            deleteevent.setEnabled(false) ;
            EDIT.setEnabled(false) ;
         }
      }
	}


	// This method sets the frame title based upon the active object.
   // If an event is specified we are breakpointed, otherwise this is
   // an editing session.

	private void setTitle()
	{
   	if (config == null) return ;
      String s = null ;
		if (event != null) s = event.getName() ;
      if (action != null) s += "  " + action.getName() ;
      if (s != null) s = Kisekae.getCaptions().getString("BreakpointTitle") + " - " + s ;
      else if (kiss != null) s = Kisekae.getCaptions().getString("FKissEditorTitle") + " - " + kiss ;
      else s = Kisekae.getCaptions().getString("FKissEditorTitle") ;
		setTitle(s) ;
   }


   // Determine our operational run state.

   private void updateRunState()
   {
      boolean active = EventHandler.isActive() ;
      EventHandler handler = (config != null) ? config.getEventHandler() : null ;
      int eventcount = (handler != null) ? handler.getEventCount() : 0 ;
      pause.setEnabled(eventcount > 0) ;
      PAUSE.setEnabled(eventcount > 0) ;
      resume.setEnabled(eventcount > 0) ;
      RESUME.setEnabled(eventcount > 0) ;
      ImageIcon runicon = (FKissEvent.getBreakPause()) ? runpauseicon : runonicon ;
      if (active && runicon != null) RUNSTATE.setIcon(runicon) ;
      RUNSTATE.setEnabled(active) ;
      String s = (config != null) ? config.getName() : "" ;
      String s1 = Kisekae.getCaptions().getString("RunStatePausedText") ;
      String s2 = Kisekae.getCaptions().getString("RunStateBreakText") ;
      String s3 = Kisekae.getCaptions().getString("RunStateActiveText") ;
      s1 = messageEdit(s1,s,1) ;
      s2 = messageEdit(s2,s,1) ;
      s3 = messageEdit(s3,s,1) ;
      RUNSTATE.setToolTipText((!active) ? s1
         : ((FKissEvent.getBreakPause()) ? s2 : s3)) ;
      if (eventcount == 0)
      {
         RUNSTATE.setIcon(runonicon) ;
         RUNSTATE.setToolTipText(s3) ;
      }

      // Implementation of the menu item update of our state when we become
      // visible.  
         
      for (int j = windowMenu.getItemCount()-1 ; j >= 0 ; j--)
         windowMenu.remove(j) ;

      // Add new dialog entries

      int n = 0 ;
      Vector v = KissFrame.getWindowFrames() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         KissFrame w = (KissFrame) v.elementAt(i) ;
         String sw = w.getTitle() ;
         JMenuItem mi = new JMenuItem(++n + ". " + sw) ;
         mi.addActionListener(this) ;
         windowMenu.add(mi) ;
      }
      repaint() ;
   }


   // A function to set the event tree so that the specified row is visible.
   // The event tree is set to show on row 3 of the display.

	private void setViewRow(int row)
   {
		if (TREE == null) return ;
		if (treescroll == null) return ;
		JViewport view = treescroll.getViewport() ;
		Rectangle r = view.getViewRect() ;
		int h = TREE.getRowHeight() ;
		if (h <= 0)
		{
			FKissCellRenderer cr = (FKissCellRenderer) TREE.getCellRenderer() ;
			h = cr.getHeight() ;
		}
      row -= 2 ;
      if (row < 0) row = 0 ;
		int ypos = row * h ;
		view.setViewPosition(new Point(r.x,ypos)) ;
   }


   // Set all tree events to the disabled status as defined in the
   // main OptionsDialog settings.
   
   private void setDisableAll(boolean disable, DefaultMutableTreeNode top)
   {
      if (top == null) return;
      Enumeration e = top.depthFirstEnumeration() ;
      while (e.hasMoreElements())
      {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement() ;
			Object o = node.getUserObject() ;
         if (!(o instanceof FKissEvent)) continue ;
         ((FKissEvent) o).setNoBreakpoint(disable);
      }
   }


   // A function to update the frame as an action event has been processed.

   private void updateFrame()
   {
      MainFrame mainframe = Kisekae.getMainFrame() ;
      PanelFrame panel = (mainframe != null) ? mainframe.getPanel() : null ;
      Dimension d = (panel != null) ? panel.getSize() : null ;
      updateFrame((d != null) ? new Rectangle(d) : null) ;
   }

	private void updateFrame(Rectangle box)
   {
      // Update the variable data.

      if (tabledata != null) tabledata.setDefaultData() ;
      if (TABLE != null)
      {
         TABLE.revalidate() ;
         TABLE.repaint() ;
      }

      // Update the preview pane if we have a reason.

      if (box != null)
      {
         MainFrame mainframe = Kisekae.getMainFrame() ;
         PanelFrame panel = (mainframe != null) ? mainframe.getPanel() : null ;
         Image image = (panel != null) ? panel.getImage() : null ;
         Color bg = (panel != null) ? panel.getBackground() : null ;
         if (bg != null) preview.setBackground(bg) ;
         preview.updateImage(image) ;
      }
   }


   // A function to construct an event tree from the current configuration.

   private void buildEventTree(Configuration config, KissObject kiss)
   {
      int n = 0 ;
      if (config == null) return ;
      EventHandler handler = config.getEventHandler() ;
      if (handler == null) return ;
      Enumeration e = null ;
      Vector sorted = new Vector() ;
  		DefaultMutableTreeNode node = null ;
      
      // Show the event tree in context.  If a KissObject is specified then
      // only show the events for this object.  By default all events are shown.
      
      if (kiss != null)
      {
         top.setUserObject(kiss.toString()) ;
         n = kiss.getEventCount() ;
     		node = new DefaultMutableTreeNode("Events  [" + n + "]") ;
         e = kiss.getEvents() ;
      }
      
      // If no elements are found show all configuration events
      
      if (n == 0)
      {
         Object id = config.getIdentifier() ;
         String name = (id != null) ? id.toString() : "" ;
         String s1 = Kisekae.getCaptions().getString("ConfigurationTitle") ;
         s1 = messageEdit(s1,name,1) ;
         top.setUserObject(s1) ;
         n = handler.getEventCount() ;
         node = new DefaultMutableTreeNode("Events  [" + n + "]") ;
         e = handler.getEvents() ;
      }

      // Sort the list by event name.

      while (e != null && e.hasMoreElements())
      {
			Vector events = (Vector) e.nextElement() ;
         for (int i = 0 ; i < events.size() ; i++)
			{
           	Object o = events.elementAt(i) ;
            if (o instanceof FKissEvent) sorted.addElement(o) ;
         }
      }
      Collections.sort(sorted) ;
      e = sorted.elements() ;

   	// Populate the tree with all defined events.

      boolean exists = false ;
		while (e.hasMoreElements())
   	{
        	FKissEvent evt = (FKissEvent) e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
			node.add(eventnode) ;
         exists = true ;

         // Build the event action hierarchy

         Enumeration enum1 = evt.getActions() ;
         if (enum1 == null) continue ;
         while (enum1.hasMoreElements())
   		{
				FKissAction a = (FKissAction) enum1.nextElement() ;
            DefaultMutableTreeNode actionnode = new DefaultMutableTreeNode(a) ;
				eventnode.add(actionnode) ;
			}
      }

   	// Set the tree up to display this node hierarchy.
      
      top.removeAllChildren() ;
      if (exists) top.add(node);
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
		TREE.expandRow(0) ;
		TREE.expandRow(1) ;
      TREE.repaint() ;
   }


   // A function to position the event tree to the specified object.
   // We search the tree to find the current event or action entry.
   // Note that we find the tree entry with the identical object reference.
   // This function returns the row number of the found entry, or -1.

   private int expandTree(Object o)
   {
      int row = -1 ;
      Object o1 = null ;
      if (o == null) return row ;
      if (top == null) return row ;
      Enumeration enum1 = top.breadthFirstEnumeration() ;
      while (enum1.hasMoreElements())
      {
         o1 = enum1.nextElement() ;
         if (!(o1 instanceof DefaultMutableTreeNode)) continue ;
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) o1 ;
         Object nodevalue = node.getUserObject() ;
         if (nodevalue == o) break ;
         o1 = null ;
      }

      // If we found the node expand the tree and select this node.

      if (o1 instanceof DefaultMutableTreeNode)
      {
         TreeNode [] nodes = ((DefaultMutableTreeNode) o1).getPath() ;
         TreePath path = new TreePath(nodes) ;
         TREE.scrollPathToVisible(path) ;
         row = TREE.getRowForPath(path) ;
         TREE.expandRow(row) ;
         TREE.setSelectionRow(row) ;
      }
      else
      {
     		TREE.expandRow(0) ;
     		TREE.expandRow(1) ;
      }
      return row ;
   }


   // A function to traverse the tree on display to set or clear the disable 
   // indicator for all events.  This function returns the number of events
   // processed.

   private int traverseTree(boolean nobreakpoint)
   {
      int row = -1 ;
      Object o1 = null ;
      if (top == null) return row ;
      Enumeration enum1 = top.breadthFirstEnumeration() ;
      while (enum1.hasMoreElements())
      {
         o1 = enum1.nextElement() ;
         if (!(o1 instanceof DefaultMutableTreeNode)) continue ;
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) o1 ;
         Object nodevalue = node.getUserObject() ;
         
         if (nodevalue == null) continue ;
         if (!(nodevalue instanceof FKissEvent)) continue ;
         FKissEvent evt = (FKissEvent) nodevalue ;
         evt.setNoBreakpoint(nobreakpoint);
         row++ ;
      }
      return row ;
   }

   
   // Invoke our text editor on the specified event object
   
   private void editEvent(Object o)
	{
      FKissEvent selectedevent = null ;
      if (o instanceof FKissEvent)
         selectedevent = (FKissEvent) o ;
      if (o instanceof FKissAction)
         selectedevent = ((FKissAction) o).getEvent() ;
      if (selectedevent == null) return ;

      // Format and write the event.

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
     	ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		try { config.writeEvent(out,selectedevent,true) ; }
      catch (IOException ex) { }

      // Invoke the text editor on the formatted output.

		byte [] eventtext = out.toByteArray() ;
		InputStream is = new ByteArrayInputStream(eventtext) ;
      DirEntry ze = new DirEntry(selectedevent.getName()) ;
      ze.setUserObject(selectedevent) ;
		tf = new TextFrame(ze,is,true,true) ;
//		tf.showLineNumbers(true) ;
		tf.callback.addActionListener(this) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      tf.setFKissHelp(true) ;
		tf.setVisible(true) ;
   }
   

   // A function to determine if any breakpoint is set.
   // Search the tree to examine all event or action entries.

	private boolean isBreakpointSet()
   {
      Enumeration enum1 = top.breadthFirstEnumeration() ;
      while (enum1.hasMoreElements())
      {
         Object node = enum1.nextElement() ;
         if (!(node instanceof DefaultMutableTreeNode)) continue ;
         DefaultMutableTreeNode n = (DefaultMutableTreeNode) node ;
         node = n.getUserObject() ;
         if (node instanceof FKissEvent)
            { if (((FKissEvent) node).getBreakpoint()) return true ; }
         else if (node instanceof FKissAction)
            { if (((FKissAction) node).getBreakpoint()) return true ; }
      }
      return false ;
   }


   // Entry for establishing a new breakpoint object.

	void doBreakpoint(FKissEvent caller, Object o, Rectangle box, boolean end)
   {
      invoker = caller ;
      FKissEvent lastevent = event ;
      FKissAction lastaction = action ;
      if (lastevent != null) lastevent.setCurrentBreak(false) ;
      if (lastaction != null) lastaction.setCurrentBreak(false) ;

      // Establish our current breakpoint object state.

      if (o instanceof FKissEvent)
      {
         action = null ;
         event = (FKissEvent) o ;
         event.setCurrentBreak(true) ;
      }
      else if (o instanceof FKissAction)
      {
         action = (FKissAction) o ;
         event = action.getEvent() ;
         action.setCurrentBreak(true) ;
      }
      if (OptionsDialog.getDebugFKiss())
      {
         System.out.println("FKissFrame: breakpoint event  " + event) ;
         System.out.println("FKissFrame: breakpoint action " + action) ;
      }

      // Search the tree to find the current event or action entry.
      // Note that we find the tree entry with the identical object reference.

      int row = expandTree(o) ;
      if (lastevent != event) setViewRow(row) ;
      TREE.repaint() ;

      // Update the display values.

      if (action == null)
         updateFrame() ;
      else
         updateFrame(box) ;
      setValues() ;
      validate() ;
      setVisible(true) ;
      toFront() ;

      // Terminate the breakpoint activities and this frame if this is the
      // end of the debugging event.  We close in a separate thread so that
      // this routine exits and allows the FKissEvent to enter wait mode.

      if (end)
      {
			Runnable runner = new Runnable()
			{
            public void run()
            {
               JCheckBox doNotShow = new JCheckBox(Kisekae.getCaptions().getString("DoNotShowAgain")) ;
               JCheckBox pausenext = new JCheckBox(Kisekae.getCaptions().getString("PauseNextEvent")) ;
               if (OptionsDialog.getShowBreakPointEnd())
               {
                  String msg = Kisekae.getCaptions().getString("BreakpointTerminatedText") ;
                  Object [] msgContent = {msg, pausenext, doNotShow} ;
                  JOptionPane.showMessageDialog(me,
                     msgContent, Kisekae.getCaptions().getString("FKissEditorTitle"),
                     JOptionPane.INFORMATION_MESSAGE) ;
                  if (doNotShow.isSelected())
                     OptionsDialog.setShowBreakPointEnd(false);
               }
               execute.setEnabled(false) ;
               EXECUTE.setEnabled(false) ;
               stepinto.setEnabled(false) ;
               STEPINTO.setEnabled(false) ;
               runtosel.setEnabled(false) ;
               RUNTOSEL.setEnabled(false) ;
               runtoend.setEnabled(false) ;
               RUNTOEND.setEnabled(false) ;
               runtocall.setEnabled(false) ;
               RUNTOCALL.setEnabled(false) ;
               
               // Resume processing

               if (pause.isSelected() || PAUSE.isSelected() || pausenext.isSelected())
                  FKissEvent.setBreakPause(me,true) ;
               if (event != null) event.setCurrentBreak(false) ;
               if (action != null) action.setCurrentBreak(false) ;
               TREE.setSelectionPath(null) ;
               if (invoker != null) invoker.resumeProcessing() ;
               selectednode = null ;
               event = null ;
               action = null ;
               setValues() ;

               // Bring the main frame to the front if we terminated 

               Runnable bringtofront = new Runnable()
               {
                  public void run()
                  {
                     MainFrame mf = Kisekae.getMainFrame() ;
                     if (mf != null) mf.toFront() ; 
                  }
               } ;
               javax.swing.SwingUtilities.invokeLater(bringtofront) ;
            }
         } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
		}
	}


   // Entry for re-opening this frame from a manual invocation. Event wizard display
   // uses the FKissEditor as the dialog parent frame.

	void reopen(Configuration c, KissObject o, Object select) { reopen(c,o,select,null) ; }
	void reopen(Configuration c, KissObject o, Object select, JFrame wizard)
   {
      config = c ;
      kiss = o ;
      expandobject = select ;
      setValues() ;
      updateFrame() ;
      toFront() ;
      if (wizard == null) return ;
      ne = new EventWizard(this,c,o,false) ;
   	ne.callback.addActionListener(this) ;
      ne.setPrimaryObject(o) ;
      ne.show() ;
   }



	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
			// Event Wizard request.

			if (source == eventwiz)
			{
            if (config == null) return ;
            EventWizard ne = new EventWizard(this,config,null,false) ;
   			ne.callback.addActionListener(this) ;
            ne.show() ;
   			return ;
   		}
         
			// New event request.

			if (source == newevent || source == NEW)
			{
            if (config == null) return ;
   			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            DirEntry ze = new DirEntry("New Event") ;
            ze.setUserObject(new FKissEvent(null,config)) ;
   			tf = new TextFrame(ze,null,true,true) ;
   			tf.callback.addActionListener(this) ;
   			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            tf.setFKissHelp(true) ;
   			tf.setVisible(true) ;
   			return ;
   		}

			// Edit event request.  We extract the event text using the
         // configuration formatted output routines.

			if (source == editevent || source == EDIT)
			{
            if (config == null) return ;
            if (selectednode == null) return ;
            editEvent(selectednode.getUserObject()) ;
   			return ;
   		}

			// Delete event request.

			if (source == deleteevent)
			{
            if (config == null) return ;
            if (selectednode == null) return ;
            Object o = selectednode.getUserObject() ;
            FKissEvent old = null ;
            if (o instanceof FKissEvent)
               old = (FKissEvent) o ;
            if (o instanceof FKissAction)
               old = ((FKissAction) o).getEvent() ;
            if (old == null) return ;

            // Request confirmation.

            String s1 = Kisekae.getCaptions().getString("DeleteEventText") ;
            s1 = messageEdit(s1,old.toString()) ;
            int i = JOptionPane.showConfirmDialog(me, s1,
               Kisekae.getCaptions().getString("DeleteEventTitle"),
               JOptionPane.YES_NO_OPTION) ;
            if (i != JOptionPane.YES_OPTION) return ;

            // Create an undoable edit before the change is applied.

            FKissEvent e = new FKissEvent(null,config) ;
            UndoableFKissEdit ce = new UndoableFKissEdit(old,e) ;
            UndoableEditEvent ue = new UndoableEditEvent(this,ce) ;
            if (undo != null) undo.undoableEditHappened(ue) ;
            undoAction.updateUndoState() ;
            redoAction.updateRedoState() ;

            // Remove the event definition.

            Object [] params = new Object[6] ;
            params[0] = e.getIdentifier() ;
            params[1] = e.getParameters() ;
            params[2] = e.getActionList() ;
            params[3] = e.getLeadComment() ;
            params[4] = e.getTrailComment() ;
            params[5] = e.getComment() ;
            updateEvent(old,params) ;
            config.setUpdated(true) ;
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

			// A Find request invokes the find/replace dialog.

			if (source == find)
			{
				if (findDialog == null)
					findDialog = new TreeFindDialog(this,-1) ;
				findDialog.show() ;
				return ;
			}

			// Exit request.

			if (source == exit)
			{
            close() ;
				return ;
			}

			// A properties request.  Show the selected tree object properties.

			if (source == properties || source == PROPERTIES)
			{
            if (TREE == null) return ;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node != null)
            {
				   Object o = node.getUserObject() ;
               if (o instanceof FKissEvent || o instanceof FKissAction)
               {
				      EventDialog ed = new EventDialog(me,o,config) ;
				      ed.show() ;
				      return ;
               }
            }

            // Not an event or action.  Show the configuration properties.

				KissDialog kd = new ConfigDialog(me,config) ;
				kd.show() ;
				return ;
			}

   		// An Edit Configuration request brings up the Text Editor.

   		if (source == editconfig)
   		{
            if (config == null) return ;
            ArchiveEntry ze = config.getZipEntry() ;
            if (ze == null) return ;
   			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
   			byte [] configtext = new byte [0] ;
   			try { configtext = config.write() ; }
            catch (IOException ex) { return ; }
   			InputStream is = new ByteArrayInputStream(configtext) ;
            ze.setUserObject(config) ;
   			tf = new TextFrame(ze,is,true,true) ;
   			tf.callback.addActionListener(this) ;
   			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   			tf.setVisible(true) ;
   			return ;
   		}

   		// A Clear Breakpoint request clears the selected breakpoint.

   		if (source == clearbreak || source == CLEARBREAK)
   		{
            if (TREE == null) return ;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node != null)
            {
				   Object o = node.getUserObject() ;
               if (o instanceof FKissEvent)
               {
                  ((FKissEvent) o).setBreakpoint(false) ;
                  Enumeration enum1 = ((FKissEvent) o).getActions() ;
                  while (enum1 != null && enum1.hasMoreElements())
                  {
                     o = enum1.nextElement() ;
                     if (!(o instanceof FKissAction)) continue ;
                     FKissAction act = (FKissAction) o ;
                     act.setBreakpoint(act.getBreakpoint() && !act.getNoBreakpoint()) ;
                  }
               }
               else if (o instanceof FKissAction)
                  ((FKissAction) o).setBreakpoint(false) ;
               clearbreak.setEnabled(false) ;
               CLEARBREAK.setEnabled(false) ;
               setbreak.setEnabled(true) ;
               SETBREAK.setEnabled(true) ;
               clearnobreak.setEnabled(false) ;
               CLEARNOBREAK.setEnabled(false) ;
               setnobreak.setEnabled(true) ;
               SETNOBREAK.setEnabled(true) ;
               clearall.setEnabled(isBreakpointSet()) ;
               CLEARALL.setEnabled(clearall.isEnabled()) ;
               TREE.repaint() ;
            }
            return ;
   		}

   		// A Set Breakpoint request sets the selected breakpoint.
         // This can immediately issue a breakpoint call if the program
         // is running.

   		if (source == setbreak || source == SETBREAK)
   		{
            if (TREE == null) return ;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node != null)
            {
				   Object o = node.getUserObject() ;
               if (o instanceof FKissEvent)
               {
                  ((FKissEvent) o).setBreakpoint(true) ;
                  Enumeration enum1 = ((FKissEvent) o).getActions() ;
                  while (enum1 != null && enum1.hasMoreElements())
                  {
                     o = enum1.nextElement() ;
                     if (!(o instanceof FKissAction)) continue ;
                     FKissAction act = (FKissAction) o ;
                     act.setBreakpoint(act.getBreakpoint() && !act.getNoBreakpoint()) ;
                  }
               }
               else if (o instanceof FKissAction)
                  ((FKissAction) o).setBreakpoint(true) ;
               clearbreak.setEnabled(true) ;
               CLEARBREAK.setEnabled(true) ;
               setbreak.setEnabled(false) ;
               SETBREAK.setEnabled(false) ;
               clearnobreak.setEnabled(false) ;
               CLEARNOBREAK.setEnabled(false) ;
               setnobreak.setEnabled(true) ;
               SETNOBREAK.setEnabled(true) ;
               clearall.setEnabled(isBreakpointSet()) ;
               CLEARALL.setEnabled(clearall.isEnabled()) ;
               TREE.repaint() ;
            }
            return ;
   		}
 
   		// A Clear All Breakpoint request clears all breakpoints.

   		if (source == clearall || source == CLEARALL)
   		{
            if (TREE == null) return ;
            Enumeration enum1 = top.breadthFirstEnumeration() ;
            while (enum1.hasMoreElements())
            {
               Object node = enum1.nextElement() ;
               if (!(node instanceof DefaultMutableTreeNode)) continue ;
               DefaultMutableTreeNode n = (DefaultMutableTreeNode) node ;
               node = n.getUserObject() ;
               if (node instanceof FKissEvent)
                  ((FKissEvent) node).setBreakpoint(false) ;
               else if (node instanceof FKissAction)
                  ((FKissAction) node).setBreakpoint(false) ;
            }
            clearall.setEnabled(false) ;
            CLEARALL.setEnabled(false) ;
            clearbreak.setEnabled(false) ;
            CLEARBREAK.setEnabled(false) ;
            setbreak.setEnabled(selectednode != null) ;
            SETBREAK.setEnabled(selectednode != null) ;
            TREE.repaint() ;
            return ;
         }

   		// A Disable Breakpoint sets a no breakpoint state for the
         // selected event.

   		if (source == setnobreak || source == SETNOBREAK)
   		{
            if (TREE == null) return ;
            if (selectednode == null) return ;
            FKissEvent event = null ;
            Object o = selectednode.getUserObject() ;
            if (o instanceof FKissEvent) event = (FKissEvent) o ;
            else if (o instanceof FKissAction) event = ((FKissAction) o).getEvent() ;
            if (event != null) event.setNoBreakpoint(true) ;
            setValues() ;
            TREE.repaint() ;
         }

   		// An Enable Breakpoint clears a no breakpoint state for the
         // selected event.

   		if (source == clearnobreak || source == CLEARNOBREAK)
   		{
            if (TREE == null) return ;
            if (selectednode == null) return ;
            FKissEvent event = null ;
            Object o = selectednode.getUserObject() ;
            if (o instanceof FKissEvent) event = (FKissEvent) o ;
            else if (o instanceof FKissAction) event = ((FKissAction) o).getEvent() ;
            if (event != null) event.setNoBreakpoint(false) ;
            setValues() ;
            TREE.repaint() ;
         }

			// An execute request executes the next action command within the
         // current event.

			if (execute == source || EXECUTE == source)
			{
            if (invoker != null) invoker.stepProcessing() ;
				return ;
			}

			// A step into request executes the next action command and if this
         // is a gosub command the FKissEvent routine will allow a break at
         // the next action statement irrespective of the parent event.  A step
         // into a timer command sets an interim breakpoint for the referenced
         // alarm and automatically runs to termination of the current event.

			if (stepinto == source || STEPINTO == source)
			{
            String alarmid = EventHandler.getTimerAlarmParam(action) ;
            if (alarmid != null)
            {
   				alarmid = evaluateParam(alarmid) ;
               Object cid = (config != null) ? config.getID() : null ;
     				Alarm alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;

               if (OptionsDialog.getShowStepIntoEnd())
               {
                  JCheckBox doNotShow = new JCheckBox(Kisekae.getCaptions().getString("DoNotShowAgain")) ;
                  String msg1 = Kisekae.getCaptions().getString("StepIntoAlarmMessage1") ;
                  String msg2 = Kisekae.getCaptions().getString("StepIntoAlarmMessage2") ;
                  String msg3 = Kisekae.getCaptions().getString("StepIntoAlarmMessage3") ;
                  String msg4 = Kisekae.getCaptions().getString("StepIntoAlarmMessage4") ;
                  String msg5 = Kisekae.getCaptions().getString("StepIntoAlarmMessage5") ;
                  msg1 = messageEdit(msg1,alarm.toString()) ;
                  msg2 = messageEdit(msg2,event.getName()) ;
                  msg3 = messageEdit(msg3,alarm.toString()) ;
                  Object [] msgContent = {msg1, msg2, msg3, msg4, msg5, doNotShow} ;
                  int i = JOptionPane.showConfirmDialog(me, msgContent,
                     Kisekae.getCaptions().getString("StepIntoAlarmTitle"),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.WARNING_MESSAGE) ;
                  if (doNotShow.isSelected())
                     OptionsDialog.setShowStepIntoEnd(false);
                  if (i != JOptionPane.YES_OPTION) 
                  {
                     if (invoker != null) invoker.stepProcessing() ;
                     return ;
                  }
               }
               
					Vector v = (alarm != null) ? alarm.getEvent("alarm") : null ;
               Object a = (v != null && v.size() > 0) ? v.elementAt(0) : null ;
               if (a instanceof FKissEvent && invoker != null)
               {
                  ((FKissEvent) a).setNoBreakpoint(false) ;
                  ((FKissEvent) a).setInterimBreakpoint(me,true) ;
                  if (event != null) event.setCurrentBreak(false) ;
                  if (action != null) action.setCurrentBreak(false) ;
                  invoker.timerProcessing() ;
                  return ;
               }
            }

            // Normal gosub processing.

            if (invoker != null) invoker.enterProcessing() ;
				return ;
			}

			// A run to cursor request executes until the selected action command.

			if (runtosel == source || RUNTOSEL == source)
			{
            if (TREE == null) return ;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return ;
			   Object o = node.getUserObject() ;
            if (o instanceof FKissAction)
               if (invoker != null) invoker.cursorProcessing((FKissAction) o) ;
				return ;
			}

			// A run to end request executes until the end of the code module.

			if (runtoend == source || RUNTOEND == source)
			{
            if (invoker != null) invoker.returnProcessing() ;
				return ;
			}

			// A run to call request returns to the next statement in the calling module.

			if (runtocall == source || RUNTOCALL == source)
			{
            if (invoker != null) invoker.returnProcessing() ;
				return ;
			}

			// A Restart reloads the data set and closes this frame.
         // The close is performed on a MainFrame callback when the
         // set is reloaded.

			if (restart == source)
			{
            MainFrame mf = Kisekae.getMainFrame() ;
            if (mf == null) return ;
            FKissEvent.setBreakPause(null,false) ;
            EventHandler.stopEventHandler() ;
			   setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            event = null ;
            mf.restart() ;
				return ;
			}

			// A Pause request resumes processing but signals that this frame
         // should be entered, if visible, on the next instruction.

			if (pause == source || PAUSE == source)
			{
            FKissEvent.setBreakPause(me,true) ;
            if (event != null) event.setCurrentBreak(false) ;
            if (action != null) action.setCurrentBreak(false) ;
            TREE.setSelectionPath(null) ;
            if (invoker != null) invoker.resumeProcessing() ;
            selectednode = null ;
            event = null ;
            action = null ;
            setValues() ;
            MainFrame mf = Kisekae.getMainFrame() ;
            if (mf != null) mf.toFront() ; else toBack() ;
				return ;
			}

			// A Resume request activates the program.

			if (resume == source || RESUME == source)
			{
            FKissEvent.setBreakPause(me,false) ;
            if (event != null) event.setCurrentBreak(false) ;
            if (action != null) action.setCurrentBreak(false) ;
            TREE.setSelectionPath(null) ;
            if (invoker != null) invoker.resumeProcessing() ;
            selectednode = null ;
            event = null ;
            action = null ;
            setValues() ;
            MainFrame mf = Kisekae.getMainFrame() ;
            if (mf != null) mf.toFront() ; else toBack() ;
				return ;
			}

			// A RUNSTATE click alters the program run state.  This can be
         // used to cancel a pause request.

			if (RUNSTATE == source)
			{
            if (EventHandler.isActive() && FKissEvent.getBreakPause())
               FKissEvent.setBreakPause(FKissEvent.getBreakFrame(),false) ;
            setValues() ;
				return ;
			}

			// A Show All Event request.  This is valid if we have contextually
         // filtered the event tree to show only the object or image events.

			if (source == showall || source == SHOWALL)
			{
            if (config == null) return ;
            if (top == null) return ;
            String s1 = (kiss != null) ? kiss.toString() : "" ;
            String s2 = top.toString() ;
            String s3 = Kisekae.getCaptions().getString("MenuEditShowAll") ;
            String s4 = Kisekae.getCaptions().getString("MenuEditShowCel") ;
            KissObject ko = (s2.indexOf(s1) >= 0) ? null : kiss ;
            if (ko == null && kiss != null && kiss.getName() != null)
            {
               s4 = messageEdit(s4,kiss.getName()) ;
               SHOWALL.setText(s4) ;
               showall.setText(s4) ;
            }
            else
            {
               SHOWALL.setText(s3) ;
               showall.setText(s3) ;
            }
            buildEventTree(config,ko) ;
            TREE.repaint() ;
     			return ;
   		}

			// An Undo All request.

			if (source == undoall)
			{
				int i = JOptionPane.showConfirmDialog(this,
            	Kisekae.getCaptions().getString("UndoAllConfirmText"),
               Kisekae.getCaptions().getString("UndoAllDialogTitle"),
              	JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) ;
            if (i == JOptionPane.CANCEL_OPTION) return ;
            undoredo = true ;
            while (undo.canUndo()) undo.undo() ;
            undo.discardAllEdits() ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
            undoredo = false ;
            return ;
         }


			// A Print request prints the current panel display.

         if (source == print)
			{
				PrinterJob pj = PrinterJob.getPrinterJob() ;
				ComponentPrintable cp = new ComponentPrintable(getContentPane()) ;
				pj.setPrintable(cp,pageformat) ;
				if (pj.printDialog())
				{
					try { pj.print() ; }
					catch (PrinterException ex)
					{
						System.err.println("Printing error: " + ex.toString()) ;
						ex.printStackTrace() ;
                  JOptionPane.showMessageDialog(this,
                     Kisekae.getCaptions().getString("PrinterError") + " - " +
                     Kisekae.getCaptions().getString("PrintingTerminated")
                     + "\n" + ex.toString(),
                     Kisekae.getCaptions().getString("PrinterError"),
                     JOptionPane.ERROR_MESSAGE) ;
					}
				}
				return ;
			}

			// The Help About request brings up the About dialog window.

         if (source == about)
			{
				if (aboutdialog != null) aboutdialog.show() ;
				return ;
			}

			// A Print Preview request shows a preview frame.

         if (source == printpreview)
			{
			   ComponentPrintable cp = new ComponentPrintable(getContentPane()) ;
				int orientation = PageFormat.PORTRAIT ;
				if	(pageformat != null) orientation = pageformat.getOrientation() ;
				new PrintPreview(cp,orientation) ;
				return ;
			}

			// A Page Setup request establishes the print control page format.

         if (source == pagesetup)
			{
				PrinterJob pj = PrinterJob.getPrinterJob() ;
				pageformat = pj.pageDialog(getPageFormat()) ;
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

   		// An update request from the event wizard dialog has occured.
   		// The configuration memory image file will be updated.
         // Note that the MainFrame update will fire a callback
         // that will terminate this frame.

   		if ("EventWizard Callback".equals(evt.getActionCommand()))
   		{
            Object o = evt.getSource() ;
            if (!(o instanceof CallbackButton)) return ;
            o = ((CallbackButton) o).getParentObject() ;
            if (!(o instanceof EventWizard)) return ;
            ne = (EventWizard) o ;
            TextObject text = ne.getTextObject() ;
            if (text == null) return ;
            ArchiveEntry ze = text.getZipEntry() ;
            if (ze == null) return ;
            applyNewTextEvent(text,config) ;
            return ;
         }

   		// An update request from the text edit window has occured.
   		// The configuration memory image file will be updated.
         // Note that the MainFrame update will fire a callback
         // that will terminate this frame.

   		if ("TextFrame Callback".equals(evt.getActionCommand()))
   		{
            Object o = evt.getSource() ;
            if (!(o instanceof CallbackButton)) return ;
            o = ((CallbackButton) o).getParentObject() ;
            if (!(o instanceof TextFrame)) return ;
            tf = (TextFrame) o ;
            TextObject text = tf.getTextObject() ;
            applyNewTextEvent(text,config) ;
            return ;
         }

   		// An update request from the MainFrame window has occured.
   		// The configuration has been updated and this frame must exit.

   		if ("MainFrame Callback".equals(evt.getActionCommand()))
   		{
			   setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            invoker = null ;
            close() ;
         }
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			System.out.println("FKissFrame: Out of memory.") ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			EventHandler.stopEventHandler() ;
			System.out.println("FKissFrame: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


	// ItemListener interface.  The item state changed method is invoked
	// when checkbox menu items are selected.

	public void itemStateChanged(ItemEvent evt)
	{
		Object source = evt.getSource() ;
      if (source == eventpause)
         OptionsDialog.setEventPause(eventpause.isSelected());
      if (source == actionpause)
         OptionsDialog.setActionPause(actionpause.isSelected());
      if (source == disableall)
      {
         OptionsDialog.setDisableAll(disableall.isSelected());
         int n = traverseTree(disableall.isSelected()) ;
         TREE.repaint() ;
      }
	}



	// Window Events

	public void windowOpened(WindowEvent evt)
   {
      FKissEvent.setBreakPause(me,FKissEvent.getBreakPause()) ;
      if (tabledata != null) tabledata.setDefaultData() ;
      if (TABLE != null)
      {
         TABLE.revalidate() ;
         TABLE.repaint() ;
      }
      int row = expandTree(expandobject) ;
      setViewRow(row) ;
      TREE.requestFocus() ;
   }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt)
   { updateRunState() ; updateFrame() ; TREE.requestFocus() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// Clipboard owner interface functions.

	public void lostOwnership(Clipboard cb, Transferable contents)
	{
	}


	// Function to evaluate a KiSS object key parameter.  If the parameter
	// is a variable then this function returns the variable value, otherwise
	// it returns the actual parameter.

	private String evaluateParam(String s)
	{
      if (variable == null) return s ;
		Object o = variable.getValue(s,null) ;
		String value = (o != null) ? o.toString() : s ;
		if (value != null) value = value.toUpperCase() ;
		return value ;
	}
   
   
   // Function to apply a new event text to the configuration.
   
   void applyNewTextEvent(TextObject text, Configuration config) throws IOException
   {
      ArchiveEntry ze = (text != null) ? text.getZipEntry() : null ;
      Object userobject = (ze != null) ? ze.getUserObject() : null ;

      // Capture the edited text in a byte array.

		ByteArrayOutputStream out = new ByteArrayOutputStream() ;
		try {	if (text != null) text.write(null,out,null) ; }
		catch (IOException e)
		{
			System.out.println("I/O Exception: " + e.toString()) ;
			e.printStackTrace() ;
		}
		finally
		{
			try { if (out != null) out.close() ; }
			catch (IOException e)
			{
				System.out.println("I/O Exception: " + e.toString()) ;
				e.printStackTrace() ;
			}
		}

		// If our user object is a configuration then update the
      // configuration memory data and apply the changes.
		// This immediately invalidates our current configuration.

		if (config != null && userobject instanceof Configuration)
		{
			config.setMemoryFile(out.toByteArray(),ze) ;
			MainFrame mf = Kisekae.getMainFrame() ;
			if (mf != null) mf.init(config) ;
		}

   	// If our user object is an event then update the old event
      // object state.  This update will invalidate our current
      // configuration for any restarts, but not otherwise.

		if (config != null && userobject instanceof FKissEvent)
		{
         errormsgs = null ;
         FKissEvent old = (FKissEvent) userobject ;
         Vector v = config.parseFKiss(this,out.toByteArray()) ;
         config.setLoader(null) ;
         
         // If we had any parse errors show the event code in a
         // text frame for editing. Parse errors are those messages
         // that begin with the form "[Line nnn]".

         if (errormsgs != null)
         {
            InputStream is = text.getInputStream() ;
            final TextFrame tf2 = new TextFrame(ze,is,true,true) ;
            if (tf2.setErrorLine(errormsgs) > 0)
            {
               setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               String s1 = Kisekae.getCaptions().getString("SyntaxErrorMsg") ;
               s1 = messageEdit(s1,old.toString()) ;
               Object [] msg = new Object[] {s1, errormsgs.toArray()} ;
               int i = JOptionPane.showConfirmDialog(me,msg,
                  Kisekae.getCaptions().getString("SyntaxErrorTitle"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.ERROR_MESSAGE) ;
               
               // Close our current edit frame and show the frame with errors.
               
               if (i == JOptionPane.YES_OPTION)
               {
                  if (tf != null) tf.close() ;
                  tf2.setChanged() ; 
                  tf2.callback.addActionListener(this) ;
                  Runnable runner = new Runnable()
                  { 
                     public void run() 
                     { 
                        tf2.setVisible(true) ; 
                        tf2.toFront() ;
                     } 
                  } ;
                  javax.swing.SwingUtilities.invokeLater(runner) ;
                  return ;
               }
            }
         }

         // Apply each event that was successfully parsed.  Multiple
         // new events each require a new event object. Note that these
         // updates are undoable within the FKiSS Editor.

         if (v == null) return ;
         for (int i = 0 ; i < v.size() ; i++)
         {
            FKissEvent e = (FKissEvent) v.elementAt(i) ;
            UndoableFKissEdit ce = new UndoableFKissEdit(old,e) ;
            UndoableEditEvent ue = new UndoableEditEvent(this,ce) ;
            if (undo != null) undo.undoableEditHappened(ue) ;
        		undoAction.updateUndoState() ;
      		redoAction.updateRedoState() ;
            Object [] params = new Object[6] ;
            params[0] = e.getIdentifier() ;
            params[1] = e.getParameters() ;
            params[2] = e.getActionList() ;
            params[3] = e.getLeadComment() ;
            params[4] = e.getTrailComment() ;
            params[5] = e.getComment() ;
            updateEvent(old,params) ;
            config.setUpdated(true) ;
            old = new FKissEvent(null,config) ;
         }
      }
   }

   // Function to update an event object for a new action set.  The event
   // object for new events will not have an identifier.  The update parameter
   // is a structure containing the new event identifier, parameter list,
   // action list, leading comments, trailing comments, and the event comment.
   // The event object is added to the event handler if it does not currently
   // exist.

   private void updateEvent(FKissEvent e, Object [] params)
   {
      if (e == null || params == null) return ;
      Object id = (params.length > 0) ? params[0] : null ;
      Object o = (params.length > 1) ? params[1] : null ;
      Vector eventparams = (o instanceof Vector) ? (Vector) o : null ;
      o = (params.length > 2) ? params[2] : null ;
      Vector actions = (o instanceof Vector) ? (Vector) o : null ;
      o = (params.length > 3) ? params[3] : null ;
      Vector leadcomment = (o instanceof Vector) ? (Vector) o : null ;
      o = (params.length > 4) ? params[4] : null ;
      Vector trailcomment = (o instanceof Vector) ? (Vector) o : null ;
      o = (params.length > 5) ? params[5] : null ;
      String eventcomment = (o instanceof String) ? (String) o : null ;

      // Detach the event from the configuration and remove it from the
      // event handler.

      EventHandler handler = null ;
      if (config != null)
      {
         handler = config.getEventHandler() ;
         config.detachEvent(e) ;
         if (handler != null) handler.removeEvent(e) ;
      }

      // Update the event object.

      Object identifier = e.getIdentifier() ;
      Vector v = e.getLeadComment() ;
      if (v != null) v.removeAllElements() ;
      v = e.getTrailComment() ;
      if (v != null) v.removeAllElements() ;
      e.setIdentifier(id) ;
      e.addParameters(eventparams) ;
      e.addActions(actions) ;
      e.addLeadComment(leadcomment) ;
      e.addTrailComment(trailcomment) ;
      e.setComment(eventcomment) ;

      // Update the action list to reference the event object.

      if (actions != null)
      {
         for (int i = 0 ; i < actions.size() ; i++)
         {
            FKissAction a = (FKissAction) actions.elementAt(i) ;
            a.setEvent(e) ;
         }
      }

      // Add the event to the event handler and attach it to the configuration.

      if (config != null)
      {
         if (handler == null)
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            PanelFrame panel = (mf != null) ? mf.getPanel() : null ;
            handler = new EventHandler() ;
     	      handler.setPanelFrame(panel) ;
            config.setEventHandler(handler) ;
            handler.startEventHandler() ;
         }

         // Add the event to the event handler if it does not already exist.

         handler.addEvent(e) ;
         config.attachEvent(e) ;
      }

      // Recreate the event tree.

      buildEventTree(config,null) ;
      int row = expandTree(e) ;
      if (identifier == null) setViewRow(row) ;

      // Show a debug trace.

      if (OptionsDialog.getDebugFKiss())
      {
         if (identifier == null)
            System.out.println("FKissFrame: add new event " + e) ;
         else if (e.getIdentifier() == null)
            System.out.println("FKissFrame: delete event " + identifier) ;
         else
            System.out.println("FKissFrame: update event " + e) ;
      }
   }


	// Method to capture error messages from an FKiSS event parse.
   // This is the loader error routine called from the Configuration 
   // parse function.

	void showError(String s) { showError(s,null) ; }
	void showError(String s, String highlite) 
	{
      if (errormsgs == null) errormsgs = new Vector() ;
      errormsgs.addElement(s) ;
   }

	void showVarning(String s) { showWarning(s,null) ; }
	void showWarning(String s, String highlite) 
	{
      if (errormsgs == null) errormsgs = new Vector() ;
      errormsgs.addElement(s) ;
   }

   // Function to parse an event Group or Cel or Cel Group parameter.
   // These cannot be variables.

   private KissObject findGroupOrCel(String s)
   {
      Object o = Group.findGroup(s,config,null) ;
      if (o == null) o = Cel.findCel(s,config,null) ;
      if (o == null) o = CelGroup.findCelGroup(s,config,null) ;
      if (!(o instanceof KissObject)) return null ;
      return (KissObject) o ;
   }



	// A utility function to return our event invoker.

	FKissEvent getInvoker() { return invoker ; }



	// A utility function to return our current page format for printing.

	private PageFormat getPageFormat()
   {
      if (pageformat == null)
      {
         PrinterJob pj = PrinterJob.getPrinterJob() ;
         pageformat = pj.defaultPage() ;
      }
      return pageformat ;
   }


	// We close the frame after clean up.

	public void close()
	{
      FKissEvent.setBreakPause(null,false) ;
      if (event != null) event.setCurrentBreak(false) ;
      if (action != null) action.setCurrentBreak(false) ;

      // If there are any interim breakpoints remaining for this frame
      // they must be cleared.

      EventHandler handler = (config != null) ? config.getEventHandler() : null ;
      Enumeration enum1 = (handler != null) ? handler.getEvents() : null ;
      while (enum1 != null && enum1.hasMoreElements())
      {
			Vector events = (Vector) enum1.nextElement() ;
         for (int i = 0 ; i < events.size() ; i++)
			{
           	Object o = events.elementAt(i) ;
            if (o instanceof FKissEvent)
            {
               FKissEvent e = (FKissEvent) o ;
               if (e.getInterimBreakpoint(me)) e.setInterimBreakpoint(null,false) ;
            }
         }
      }

      // Resume normal processing.

      if (invoker != null) invoker.resumeProcessing() ;
      callback.removeActionListener(null) ;
		super.close() ;
      flush() ;
      dispose() ;
   }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if some problem occurs while
   // disposing with the dialog window.

	private void flush()
	{
		me = null ;
      tf = null ;
		config = null ;
      event = null ;
      action = null ;
      invoker = null ;
      preview = null ;
      selectednode = null ;
		toolbar = null ;
      tabledata = null ;
      variable = null ;
		if (undo != null) undo.discardAllEdits() ;
		undo = null ;

      // Remove any MainFrame callbacks.

      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.callback.removeActionListener(this) ;

      // Flush the dialog contents.

      setVisible(false) ;
		TREE.removeMouseListener(treeBreakpoint) ;
      TREE.removeTreeSelectionListener(treeListener) ;
      ToolTipManager.sharedInstance().unregisterComponent(TREE) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
      TABLE = null ;
		Runtime.getRuntime().gc() ;
   }

   // A utility function to perform a literal dialog message edit.
  
   private String messageEdit(String msg, String sub)
   { return messageEdit(msg,sub,0) ; }
   
   private String messageEdit(String msg, String sub, int n)
   {
      if (msg == null || sub == null) return msg ;
      int i1 = msg.indexOf('[') ;
      int j1 = msg.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         msg = msg.substring(0,i1+1-n) + sub + msg.substring(j1+n) ;
      return msg ;
   }
   

	// Inner class to support the undo operation.

	class UndoAction extends AbstractAction
	{
		public UndoAction()
		{
			super(Kisekae.getCaptions().getString("MenuEditUndo")) ;
			setEnabled(false) ;
		}

		public void actionPerformed(ActionEvent e)
		{
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.undo(); }
			catch (CannotUndoException ex)
			{
				System.out.println("FKissFrame: Unable to undo edit") ;
				ex.printStackTrace();
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}

			// Reflect the change in the current palette panel.

			undoredo = true ;

			// Update the undo state.

			updateUndoState() ;
			redoAction.updateRedoState() ;
			undoredo = false ;
			me.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		}

		protected void updateUndoState()
		{
			if (undo.canUndo())
			{
				setEnabled(true) ;
				undoall.setEnabled(true) ;
            UNDO.setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName()) ;
			}
			else
			{
				setEnabled(false) ;
				undoall.setEnabled(false) ;
				UNDO.setEnabled(false);
				putValue(Action.NAME, Kisekae.getCaptions().getString("MenuEditUndo")) ;
			}
		}
	}


	// Inner class to construct an undoable FKiSS event edit.

	class UndoableFKissEdit extends AbstractUndoableEdit
   {
      private FKissEvent oldevent = null ;
      private FKissEvent newevent = null ;
      private Object [] oldparams = null ;
      private Object [] newparams = null ;

		// Constructor

		public UndoableFKissEdit(FKissEvent e1, FKissEvent e2)
      {
      	oldevent = e1 ;
         newevent = e2 ;
         oldparams = new Object[6] ;
         Object o = e1.getIdentifier() ;
         oldparams[0] = (o != null) ? new String(o.toString()) : null ;
         o = e1.getParameters() ;
         oldparams[1] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e1.getActionList() ;
         oldparams[2] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e1.getLeadComment() ;
         oldparams[3] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e1.getTrailComment() ;
         oldparams[4] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e1.getComment() ;
         oldparams[5] = (o instanceof String) ? new String(o.toString()) : null ;
         newparams = new Object[6] ;
         o = e2.getIdentifier() ;
         newparams[0] = (o != null) ? new String(o.toString()) : null ;
         o = e2.getParameters() ;
         newparams[1] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e2.getActionList() ;
         newparams[2] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e2.getLeadComment() ;
         newparams[3] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e2.getTrailComment() ;
         newparams[4] = (o instanceof Vector) ? ((Vector) o).clone() : null ;
         o = e2.getComment() ;
         newparams[5] = (o instanceof String) ? new String(o.toString()) : null ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      {
         String name = (oldevent != null) ? oldevent.getName() : null ;
         String s = (name != null) ? name.toString() : "" ;
         String s1 = Kisekae.getCaptions().getString("UndoFKissEditName") ;
         s1 = messageEdit(s1,s) ;
         return s1 ;
      }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         undoredo = true ;
         updateEvent(oldevent,oldparams) ;
         undoredo = false ;
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         undoredo = true ;
         updateEvent(oldevent,newparams) ;
         undoredo = false ;
		}
	}


	// Inner class to construct an undoable FKiSS event edit.

	class UndoableVariableEdit extends AbstractUndoableEdit
   {
      private Object name = null ;
      private Object oldvalue = null ;
      private Object newvalue = null ;

		// Constructor

		public UndoableVariableEdit(Object o, Object oldval, Object newval)
      {
      	name = o ;
         oldvalue = oldval ;
         newvalue = newval ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      {
         String s = (name != null) ? name.toString() : "" ;
         String s1 = Kisekae.getCaptions().getString("UndoVariableEditName") ;
         s1 = messageEdit(s1,s) ;
         return s1 ;
      }

		// Undo a change.

      public void undo()
      {
         if (variable == null) return ;
			super.undo() ;
         undoredo = true ;
         variable.setValue(name,oldvalue,null) ;
         try
         {
            int n = Integer.parseInt(oldvalue.toString()) ;
            variable.setIntValue(name.toString(),n,null) ;
         }
         catch (NumberFormatException e)
         {
            variable.setValue(name,oldvalue.toString(),null) ;
         }
         undoredo = false ;

         // Update the variable data.

         if (tabledata != null) tabledata.setDefaultData() ;
         if (TABLE != null)
         {
            TABLE.revalidate() ;
            TABLE.repaint() ;
         }
		}

		// Redo a change.

      public void redo()
      {
         if (variable == null) return ;
			super.redo() ;
         undoredo = true ;
         try
         {
            int n = Integer.parseInt(newvalue.toString()) ;
            variable.setIntValue(name.toString(),n,null) ;
         }
         catch (NumberFormatException e)
         {
            variable.setValue(name,newvalue.toString(),null) ;
         }
         undoredo = false ;

         // Update the variable data.

         if (tabledata != null) tabledata.setDefaultData() ;
         if (TABLE != null)
         {
            TABLE.revalidate() ;
            TABLE.repaint() ;
         }
		}
	}



	// Inner class to support the redo operation.

	class RedoAction extends AbstractAction
	{
		public RedoAction()
		{
			super(Kisekae.getCaptions().getString("MenuEditRedo")) ;
			setEnabled(false) ;
		}

		public void actionPerformed(ActionEvent e)
		{
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.redo() ; }
			catch (CannotRedoException ex)
			{
				System.out.println("FKissFrame: Unable to redo edit") ;
				ex.printStackTrace() ;
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}

			// Reflect the change in the current palette panel.

			undoredo = true ;

			// Update the redo state.

			updateRedoState() ;
			undoAction.updateUndoState() ;
			undoredo = false ;
			me.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		}

		protected void updateRedoState()
		{
			if (undo.canRedo())
			{
				setEnabled(true) ;
            REDO.setEnabled(true) ;
				putValue(Action.NAME, undo.getRedoPresentationName()) ;
			}
			else
			{
				setEnabled(false) ;
            REDO.setEnabled(false) ;
				putValue(Action.NAME, Kisekae.getCaptions().getString("MenuEditRedo")) ;
			}
		}
	}


   // Inner classes to define the variable table data characteristics.

   class VariableData
   {
   	public String name ;
      public String value ;

      public VariableData(String name, String value)
      {
      	this.name = name ;
         this.value = value ;
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
      	new ColumnData(Kisekae.getCaptions().getString("VariableNameText"),100,JLabel.LEFT),
         new ColumnData(Kisekae.getCaptions().getString("VariableValueText"),150,JLabel.LEFT) } ;

      private Vector vector = null ;
      private Variable variable = null ;

      public VariableTableData(Variable variable)
      {
      	this.variable = variable ;
      	vector = new Vector() ;
         setDefaultData() ;
      }

      public void setDefaultData()
      {
      	vector.removeAllElements() ;
         if (variable == null) return ;

         Enumeration enum1 = variable.getVariables() ;
         while (enum1.hasMoreElements())
         {
         	Object key = enum1.nextElement() ;
            if (!(key instanceof String)) continue ;
            Object value = variable.getValue((String) key,null) ;
            String keyvalue = (value == null)
                ? Kisekae.getCaptions().getString("UnknownValueText")
                : value.toString() ;
            vector.addElement(new VariableData((String) key, keyvalue)) ;
         }

         // Add local variables.

         Hashtable local = (event != null) ? event.getVariableTable() : null ;
         if (local != null)
         {
            enum1 = local.keys() ;
            while (enum1.hasMoreElements())
            {
            	Object key = enum1.nextElement() ;
               if (!(key instanceof String)) continue ;
               Object value = variable.getValue((String) key,event) ;
               String keyvalue = (value == null)
                   ? Kisekae.getCaptions().getString("UnknownValueText")
                   : value.toString() ;
               vector.addElement(new VariableData((String) key, keyvalue)) ;
            }
         }

         Collections.sort(vector,new SortComparator(sortcolumn,ascending)) ;
		}

      public int getRowCount() { return (vector == null) ? 0 : vector.size() ; }

      public int getColumnCount() { return columns.length ; }

      public String getColumnName(int c)
      {
      	String s = columns[c].title ;
//       if (c == sortcolumn)
//         	s += (ascending) ? " >>" : " <<" ;
      	return s ;
      }

      public boolean isCellEditable(int row, int col) { return (col == 1) ; }

      public Object getValueAt(int row, int col)
      {
      	if (row < 0 || row > getRowCount()) return " " ;
         VariableData rowvalue = (VariableData) vector.elementAt(row) ;
         switch (col)
         {
         	case 0: return rowvalue.name ;
            case 1: return rowvalue.value ;
         }
         return " " ;
      }

      public String getTitle() { return Kisekae.getCaptions().getString("VariableValuesText") ; }

      public Vector getData() { return vector ; }

      // The setValueAt method is called every time an edit is made to
      // any cell data.

      public void setValueAt(Object value, int row, int col)
      {
         if (col != 1) return ;
         if (value == null) return ;
         if (row < 0 || row >= getRowCount()) return ;
         Object o = vector.elementAt(row) ;
         if (!(o instanceof VariableData)) return ;
         VariableData vd = (VariableData) o ;
         Object oldvalue = vd.value ;
         vd.value = value.toString() ;
         if (variable == null) return ;
         try
         {
            int n = Integer.parseInt(vd.value) ;
            variable.setIntValue(vd.name,n,null) ;
         }
         catch (NumberFormatException e)
         {
            variable.setValue(vd.name,vd.value,null) ;
         }

         // Create an undoable variable edit.

     		UndoableVariableEdit ce = new UndoableVariableEdit(vd.name,oldvalue,value) ;
     		UndoableEditEvent ue = new UndoableEditEvent(this,ce) ;
     		if (undo != null) undo.undoableEditHappened(ue) ;
			undoAction.updateUndoState() ;
			redoAction.updateRedoState() ;

         // Show a debug trace.

         if (OptionsDialog.getDebugFKiss())
            System.out.println("FKissFrame: change variable " + vd.name + " from " + oldvalue + " to " + value) ;
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
         	case 0:
            	result = vd1.name.compareTo(vd2.name) ;
               break ;

            case 1:
            	result = vd1.value.compareTo(vd2.value) ;
               break ;
         }

         if (!ascending) result = -result ;
         return result ;
      }
   }
}

