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



import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.io.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;

final class ObjectDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;					  	// Reference to ourselves
	private FKissEvent event = null ;      	  	// The event object
	private FKissAction action = null ;			  	// The action object
   private Configuration config = null ;       	// The current config
   private Object value = null ;					  	// The parameter value
   private Object identifier = null ;			  	// The event action id
	private TextFrame tf = null ;						// The text edit frame
	private int expansions = 0 ;			  			// Number of tree expansions

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton FIND = new JButton();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private FlowLayout flowLayout1 = new FlowLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
   private JTree TREE = new JTree() ;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode() ;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,5,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(15,0,0,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private JLabel heading = new JLabel();
	private JLabel configlabel = new JLabel();
	private JLabel filelabel = new JLabel();
	private JLabel bytelabel = new JLabel();
	private Component component1;

	// Create specialized listeners for events.

	MouseListener mouseTreeListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent() ;
				if (node == null) return;
				Object o = node.getUserObject() ;
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
	         	node.getParent() ;
	         Object p = (parent == null) ? null : parent.getUserObject() ;
	         String section = (p == null) ? null : p.toString() ;
	         if (section == null) section = "" ;
            KissDialog kd = null ;

            // Identify the appropriate event or action dialog.

            if (o instanceof Audio)
					kd = new AudioDialog(me,(Audio) o,config) ;
            else if (o instanceof Video && section.startsWith(Kisekae.getCaptions().getString("VideoNode")))
					kd = new VideoDialog(me,(Video) o,config) ;
            else if (o instanceof Cel)
					kd = new CelDialog(me,(Cel) o,null,config) ;
            else if (o instanceof CelGroup)
					kd = new CelGroupDialog(me,(CelGroup) o,config) ;
            else if (o instanceof FKissEvent || o instanceof FKissAction)
					kd = new EventDialog(me,o,config) ;
            else if (o instanceof Group)
					kd = new GroupDialog(me,(Group) o,getPageContext(),config) ;
            else if (o instanceof PageSet)
					kd = new PageSetDialog(me,(PageSet) o,config) ;
            else if (o instanceof Palette)
					kd = new PaletteDialog(me,(Palette) o,null,config) ;
            else if (o instanceof Variable)
					kd = new VariableDialog(me,config) ;
            else if (o instanceof KissDialog)
					kd = new ConfigDialog(me,config) ;

				// Configuration elements open the text editor.  We write the
				// configuration memory copy to ensure that it is current.

				if (Kisekae.getCaptions().getString("ConfigurationNode").equals(o) && config != null)
				{
					Configuration c = config ;
					try { c.write() ; } catch (IOException ex) { }
					InputStream is = c.getInputStream() ;
					tf = new TextFrame(c.getZipEntry(),is,true,true) ;
//					tf.showLineNumbers(true) ;
					tf.callback.addActionListener((ActionListener) me) ;
					tf.setVisible(true) ;
				}

				// Show the context dialog.

				if (kd != null)
            {
					kd.show() ;
            }
				return ;
			}
		}
	} ;

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
			if (node == null) return ;
         Object o = node.getUserObject();
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
         	node.getParent() ;
         Object p = (parent == null) ? null : parent.getUserObject() ;
         String section = (p == null) ? null : p.toString() ;
         if (section == null) section = "" ;

         // Set the view button text.

         if (o instanceof FKissEvent)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewEventButton"));
         }
         else if (o instanceof FKissAction)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewActionMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewEventButton"));
         }
         else if (o instanceof Audio)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewAudioMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewAudioButton"));
         }
         else if (o instanceof Video && section.startsWith(Kisekae.getCaptions().getString("VideoNode")))
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewVideoMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewVideoButton"));
         }
			else if (o instanceof Cel)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewImageButton"));
         }
			else if (o instanceof Group)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewObjectButton"));
         }
			else if (o instanceof CelGroup)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewCelGroupMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewCelGroupButton"));
         }
			else if (o instanceof PageSet)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewPageSetMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewPageSetButton"));
         }
         else if (o instanceof Palette)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewPaletteMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewPaletteButton"));
         }
			else if (o instanceof Variable)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewVariableMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewVariableButton"));
         }
			else if (o instanceof KissDialog)
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewPropertiesMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewConfigurationButton"));
         }
			else if (Kisekae.getCaptions().getString("ConfigurationNode").equals(o))
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewFileMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipConfigButton"));
         }
			else 
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewMessage")) ;
            VIEW.setToolTipText(null);
         }
        	VIEW.setEnabled(!(Kisekae.getCaptions().getString("ViewMessage").equals(VIEW.getText()))) ;
		
         JRootPane rootpane = getRootPane()  ;
         if (VIEW.isEnabled()) rootpane.setDefaultButton(VIEW) ;
         else rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
      }
	} ;

	TreeExpansionListener treeExpander = new TreeExpansionListener()
   {
		public void treeExpanded(TreeExpansionEvent e)
		{ 
         ++expansions ; 
         CANCEL.setEnabled(parent instanceof KissDialog || expansions > 0) ; 
      }
		public void treeCollapsed(TreeExpansionEvent e)
		{ 
         --expansions ; 
         CANCEL.setEnabled(parent instanceof KissDialog || expansions > 0) ; 
      }
	} ;


   // Listener to define the tree right click event handler.

   MouseListener treeBreakpoint = new MouseListener()
   {
      public void mouseReleased(MouseEvent e)
      {
         if (!SwingUtilities.isRightMouseButton(e)) return ;
         int x = e.getX() ;
         int y = e.getY() ;
         TreePath path = TREE.getPathForLocation(x,y) ;
         if (path == null) return ;
         Object o = path.getLastPathComponent() ;
         if (!(o instanceof DefaultMutableTreeNode)) return ;
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) o ;
         o = node.getUserObject() ;
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
         TREE.repaint() ;
      }

      public void mousePressed(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
   } ;


	// Constructor

   public ObjectDialog(JDialog f, Configuration c)
   { super(f,null,false) ; init(c) ; }

	public ObjectDialog(JFrame f, Configuration c)
	{ super(f,null,false) ; init(c) ; }

   private void init(Configuration c)
   {
		me = this ;
		config = c ;

      // Construct the user interface.

		try { jbInit(); pack(); }
      catch(Exception ex)
      {
         ex.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + ex.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }
      setValues() ;

		// Center the frame in the panel space.

      Dimension d1 = getParentSize() ;
      Dimension d2 = getSize() ;
      if (d1.height > d2.height) d2.height = d1.height ;
      if (d1.width > d2.width) d2.width = d1.width ;
      setSize(d2) ;
 		center(this) ;

      // Attach a listener for MainFrame initialization completion events.
      // We may request a new initialization through a configuration text
      // file change.

		MainFrame mf = Kisekae.getMainFrame() ;
		if (mf != null) mf.callback.addActionListener(this) ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		FIND.addActionListener(this) ;
		TREE.addMouseListener(treeBreakpoint) ;
		TREE.addMouseListener(mouseTreeListener) ;
      TREE.addTreeSelectionListener(treeListener);
      TREE.addTreeExpansionListener(treeExpander);
		addWindowListener(this);
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		component1 = Box.createHorizontalStrut(90);
		panel1.setLayout(borderLayout1);
 		panel1.setBorder(eb1);
		panel1.setPreferredSize(new Dimension(620, 410));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      OK.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyOKButton"));
		CANCEL.setText(Kisekae.getCaptions().getString("ReturnMessage"));
      CANCEL.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyCancelButton"));
      CANCEL.setEnabled(parent instanceof KissDialog);
		VIEW.setEnabled(false);
		VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage"));
		FIND.setEnabled(true);
		FIND.setText(Kisekae.getCaptions().getString("FindMessage"));
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
      TREE.setShowsRootHandles(true);

      Border cb1 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("ConfigurationBoxText")),eb2) ;
		gridLayout1.setColumns(1);
		gridLayout1.setRows(3);

 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3) ;
		jPanel4.setLayout(borderLayout4) ;
      jPanel5.setLayout(flowLayout1) ;
      jPanel5.setBorder(eb4) ;

      heading.setText(Kisekae.getCaptions().getString("ObjectHeadingText"));
      heading.setBorder(BorderFactory.createEmptyBorder(20,0,5,0));
		heading.setHorizontalAlignment(SwingConstants.CENTER);

		jPanel1.setBorder(cb1);
		jPanel1.setLayout(gridLayout1);
		configlabel.setText(Kisekae.getCaptions().getString("NameText"));
		filelabel.setText(Kisekae.getCaptions().getString("FileText"));
		bytelabel.setText(Kisekae.getCaptions().getString("BytesText"));
		jPanel2.setBorder(eb3);

		getContentPane().add(panel1);
		panel1.add(jPanel4, BorderLayout.NORTH);
		jPanel1.add(configlabel, null);
		jPanel1.add(filelabel, null);
		jPanel1.add(bytelabel, null);
		jPanel4.add(jPanel1, BorderLayout.CENTER);
		jPanel5.add(FIND, null);
		jPanel4.add(jPanel5, BorderLayout.EAST);
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(component1, null);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(VIEW, null);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(heading, BorderLayout.NORTH);
		jPanel3.add(jScrollPane1, BorderLayout.CENTER);
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

		try
		{
			// An OK closes this dialog and all parent dialogs.

			if (source == OK)
			{
				close() ;
				return ;
			}

			// A CANCEL with an unexpanded tree is equivalent to an OK.

			if (source == CANCEL)
			{
				if (expansions > 0)
				{
					TreeModel treemodel = TREE.getModel() ;
					((DefaultTreeModel) treemodel).reload() ;
					TREE.expandRow(0) ;
					expansions = 0 ;
					VIEW.setEnabled(false) ;
					VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage")) ;
               CANCEL.setEnabled(parent instanceof KissDialog) ;
               TREE.requestFocus() ;
               JRootPane rootpane = getRootPane()  ;
               rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
				}
				else if (parent instanceof KissDialog)
				{
            	((KissDialog) parent).setValues() ;
               callback.doClick();
					flush() ;
					dispose() ;
					parent = null ;
					getOwner().setVisible(true) ;
				}
				else
					close() ;
				return ;
			}
         
			// A FIND request opens a dialog to search the tree for a node.

			if (source == FIND)
			{
				ObjectFindDialog find = new ObjectFindDialog(me,TREE) ;
            find.show() ;
				return ;
			}

			// A View Event initializes the dialog for the selected Event.

			if (command.equals(Kisekae.getCaptions().getString("ViewEventMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				EventDialog ed = new EventDialog(me,o,config) ;
				ed.show() ;
				return ;
			}

			// A View Action brings up the appropriate Action dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewActionMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				EventDialog ed = new EventDialog(me,o,config) ;
				ed.show() ;
				return ;
			}

			// A View Audio invokes the appropriate audio context dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewAudioMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Audio)) return ;
				AudioDialog ad = new AudioDialog(me,(Audio) o,config) ;
				ad.show() ;
				return ;
			}

			// A View Video invokes the appropriate video context dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewVideoMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Video)) return ;
				VideoDialog vd = new VideoDialog(me,(Video) o,config) ;
				vd.show() ;
				return ;
			}

			// A View Object invokes the appropriate group context dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewObjectMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Group)) return ;
				GroupDialog gd = new GroupDialog(me,(Group) o,getPageContext(),config) ;
				gd.show() ;
				return ;
			}

			// A View Image brings up a Cel dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewImageMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Cel)) return ;
				Cel c = (Cel) o ;
				o = c.getGroup() ;
				Group g = (o instanceof Group) ? (Group) o : null ;
				Vector pages = c.getPages() ;
				if (pages != null && pages.size() > 0)
				{
					Integer page = (Integer) pages.elementAt(0) ;
					PageSet p = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),config.getID(),page) ;
					setPageContext(p);
				}
				CelDialog cd = new CelDialog(me,c,g,config) ;
				cd.show() ;
				return ;
			}

			// A View CelGroup invokes the appropriate celgroup context dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewCelGroupMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof CelGroup)) return ;
				CelGroupDialog cgd = new CelGroupDialog(me,(CelGroup) o,config) ;
				cgd.show() ;
				return ;
			}

			// A View Page Set brings up a PageSet dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewPageSetMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof PageSet)) return ;
				PageSetDialog pd = new PageSetDialog(me,(PageSet) o,config) ;
				pd.show() ;
				return ;
			}

			// A View Palette brings up a PageSet dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewPaletteMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Palette)) return ;
				PaletteDialog pd = new PaletteDialog(me,(Palette) o,null,config) ;
				pd.show() ;
				return ;
			}

			// A View Variables brings up a Variable dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewVariableMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof Variable)) return ;
				VariableDialog vd = new VariableDialog(me,config) ;
				vd.show() ;
				return ;
			}

			// A View File brings up the Configuration edit dialog.
			// A wait cursor is established as a file load can take time.
			// We write the configuration memory file to ensure that it is
         // current.

         if (command.equals(Kisekae.getCaptions().getString("ViewFileMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(Kisekae.getCaptions().getString("ConfigurationNode").equals(o))) return ;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				Configuration c = config ;
				byte [] configtext = new byte [0] ;
				try { configtext = c.write() ; } catch (IOException ex) { }
				InputStream is = new ByteArrayInputStream(configtext) ;
				tf = new TextFrame(c.getZipEntry(),is,true,true) ;
//				tf.showLineNumbers(true) ;
				tf.callback.addActionListener(this) ;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				tf.setVisible(true) ;
				return ;
			}

			// A View Properties brings up the configuration properties dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewPropertiesMessage")))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;
				Object o = node.getUserObject();
				if (!(o instanceof KissDialog)) return ;
				KissDialog kd = new ConfigDialog(me,config) ;
				kd.show() ;
				return ;
			}

			// An update request from the text edit window has occured.
			// The configuration memory image file will be updated.

			if ("TextFrame Callback".equals(evt.getActionCommand()))
			{
				if (tf == null) return ;
				ByteArrayOutputStream out = new ByteArrayOutputStream() ;
				TextObject text = tf.getTextObject() ;
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

				// Update the configuration memory data and apply the changes.
				// This creates a new configuration object and can invalidate
				// our current configuration.

				ArchiveEntry ze = text.getZipEntry() ;
				if (config != null && ze != null)
				{
					config.setMemoryFile(out.toByteArray(),ze) ;
					MainFrame mf = Kisekae.getMainFrame() ;
					if (mf != null) mf.init(config) ;
				}
			}

			// A new mainframe configuration has been loaded resulting from
			// a configuration file update.  Our display must be updated.

			if ("MainFrame Callback".equals(evt.getActionCommand()))
			{
         	if (me == null) return ;
				config = Kisekae.getMainFrame().getConfig() ;
				setValues() ;
			}
		}

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("ObjectDialog: Out of memory.") ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults.

		catch (Throwable e)
		{
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("ObjectDialog: Internal fault, action " + evt.getActionCommand()) ;
         e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + e.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { TREE.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { TREE.requestFocus() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }



	// Method to create the dialog object selection tree.  The top level
   // objects such as events, cels, groups, and so on can be expanded
   // to individual object references.  These references, when selected,
   // invoke their appropriate object display dialog.

	void setValues()
	{
   	if (config == null) return ;
      if (expansions > 0) return ;
		String configname = config.getName() ;
		if (configname == null) configname = Kisekae.getCaptions().getString("NewConfigurationText") ;
		String title = Kisekae.getCaptions().getString("ObjectDialogTitle") + " " + configname ;
		setTitle(title) ;

      // Show the configuration file attributes.

      String fn = null ;
      Object o = config.getZipFile() ;
      if (o instanceof PkzFile) fn = ((PkzFile) o).getName() ;
      if (o instanceof LhaFile) fn = ((LhaFile) o).getName() ;
		if (o instanceof DirFile) fn = ((DirFile) o).getName() ;
		if (fn == null) fn = Kisekae.getCaptions().getString("UnknownValueText") ;
		configlabel.setText(Kisekae.getCaptions().getString("NameText") + " " + configname) ;
		filelabel.setText(Kisekae.getCaptions().getString("FileText") + " " + fn) ;
		bytelabel.setText(Kisekae.getCaptions().getString("BytesText") + " " + config.getBytes()) ;

      // Construct the tree selection control.

   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top.setUserObject(Kisekae.getCaptions().getString("ConfigurationNode") + " " + config.getIdentifier()) ;
		jScrollPane1.getViewport().add(TREE, null);

		// Build the configuration object selection hierarchy.

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(configname) ;
		node.add(new DefaultMutableTreeNode(new ConfigDialog(this,config))) ;
		node.add(new DefaultMutableTreeNode(Kisekae.getCaptions().getString("ConfigurationNode"))) ;
		top.add(node) ;

      // Build the audio object selection hierarchy.

      Vector sounds = config.getSounds() ;
      int n = (sounds == null) ? 0 : sounds.size() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("AudioNode") + " [" + n + "]") ;
      if (sounds != null)
      {
      	for (int i = 0 ; i < sounds.size() ; i++)
				node.add(new DefaultMutableTreeNode(sounds.elementAt(i))) ;
         if (n > 0) top.add(node) ;
      }

      // Build the video object selection hierarchy.

      Vector movies = config.getMovies() ;
      n = (movies == null) ? 0 : movies.size() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("VideoNode") + " [" + n + "]") ;
      if (movies != null)
      {
      	for (int i = 0 ; i < movies.size() ; i++)
				node.add(new DefaultMutableTreeNode(movies.elementAt(i))) ;
         if (n > 0) top.add(node) ;
      }

      // Build the cel object selection hierarchy.  Cels that are internal
      // are not added.

      Vector cels = config.getCels() ;
		node = new DefaultMutableTreeNode() ;
      if (cels != null)
      {
         n = 0 ;
      	for (int i = 0 ; i < cels.size() ; i++)
         {
            Cel cel = (Cel) cels.elementAt(i) ;
            if (cel.isInternal()) continue ;
         	node.add(new DefaultMutableTreeNode(cel)) ;
            n++ ;
         }
         node.setUserObject(Kisekae.getCaptions().getString("ImagesNode") + " [" + n + "]");
         if (n > 0) top.add(node) ;
      }

      // Build the celgroup object selection hierarchy.

      Vector celgroups = config.getCelGroups() ;
      n = (celgroups == null) ? 0 : celgroups.size() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("CelGroupsNode") + " [" + n + "]") ;
      if (celgroups != null)
      {
      	for (int i = 0 ; i < celgroups.size() ; i++)
				node.add(new DefaultMutableTreeNode(celgroups.elementAt(i))) ;
			if (n > 0) top.add(node) ;
		}

		// Get the configuration event list from the event handler.

      EventHandler handler = config.getEventHandler() ;
      n = (handler == null) ? 0 : handler.getEventCount() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("EventsNode") + " [" + n + "]") ;
      if (handler != null)
      {
	      Enumeration e = handler.getEvents() ;
         Vector sorted = new Vector() ;

	      // Sort the list by event name.

         while (e.hasMoreElements())
         {
				Vector events = (Vector) e.nextElement() ;
	         for (int i = 0 ; i < events.size() ; i++)
				{
            	o = events.elementAt(i) ;
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
         if (exists) top.add(node);
		}

      // Build the group object selection hierarchy.   Groups without
      // cels are not added.

      Vector groups = config.getGroups() ;
		node = new DefaultMutableTreeNode() ;
      if (groups != null)
      {
         n = 0 ;
      	for (int i = 0 ; i < groups.size() ; i++)
         {
            Group group = (Group) groups.elementAt(i) ;
            if (group.getCelCount() == 0) continue ;
				node.add(new DefaultMutableTreeNode(group)) ;
            n++ ;
         }
         node.setUserObject(Kisekae.getCaptions().getString("ObjectsNode") + " [" + n + "]") ;
			if (n > 0) top.add(node) ;
		}

      // Build the page set object selection hierarchy.

      Vector pages = config.getPages() ;
      n = (pages == null) ? 0 : pages.size() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("PageSetsNode") + " [" + n + "]") ;
      if (pages != null)
      {
      	for (int i = 0 ; i < pages.size() ; i++)
         	node.add(new DefaultMutableTreeNode(pages.elementAt(i))) ;
         if (n > 0) top.add(node) ;
      }

      // Build the palette object selection hierarchy.

      Vector palettes = config.getPalettes() ;
      n = (palettes == null) ? 0 : palettes.size() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("PalettesNode") + " [" + n + "]") ;
      if (palettes != null)
      {
      	for (int i = 0 ; i < palettes.size() ; i++)
         	node.add(new DefaultMutableTreeNode(palettes.elementAt(i))) ;
         if (n > 0) top.add(node) ;
      }

      // Build the variable object selection hierarchy.

      Variable variable = config.getVariable() ;
      n = (variable == null) ? 0 : variable.getSize() ;
		node = new DefaultMutableTreeNode(Kisekae.getCaptions().getString("VariablesNode") + " [" + n + "]") ;
      if (variable != null && n > 0)
      {
        	node.add(new DefaultMutableTreeNode(variable)) ;
	      n = (handler == null) ? 0 : handler.getEventCount() ;
         if (n > 0) top.add(node) ;
      }

      // Expand the top level of the tree.

		TREE.expandRow(0) ;
      expansions = 0 ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
	}

   // We overload the KissDialog close method.

   void close()
   {
		JFrame f = getParentFrame() ;
		if (f instanceof MainFrame)
		{
			MainFrame mf = (MainFrame) f ;
			mf.callback.removeActionListener(this) ;
		}
      flush() ;
      super.close() ;
   }

   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
      event = null ;
      action = null ;
      value = null ;
      identifier = null ;
      config = null ;

      // Remove any MainFrame attached listeners.

		MainFrame mf = Kisekae.getMainFrame() ;
		if (mf != null) mf.callback.removeActionListener(this) ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		TREE.removeMouseListener(treeBreakpoint) ;
		TREE.removeMouseListener(mouseTreeListener) ;
      TREE.removeTreeSelectionListener(treeListener) ;
      TREE.removeTreeExpansionListener(treeExpander) ;
		removeWindowListener(this) ;
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
   }
}

