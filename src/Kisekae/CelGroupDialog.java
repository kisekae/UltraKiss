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
import java.awt.image.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;

final class CelGroupDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;						// Reference to ourselves
	private CelGroup celgroup = null ;       		// The CelGroup object
   private Configuration config = null ;			// Our current context
	private Cel cel = null ;    	  					// A cel in the group
	private int expansions = 0 ;			  			// Number of tree expansions

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel9 = new JPanel();
	private JPanel jPanel10 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton LISTBTN = new JButton();
   private JButton TransparencyButton = new JButton();
   private JButton DummyButton1 = new JButton();
   private JButton DummyButton2 = new JButton();
   private JButton DummyButton3 = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridLayout gridLayout5 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout5 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList LIST = null ;
   private JTree TREE = null ;
 	private DefaultListModel listmodel = new DefaultListModel() ;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode() ;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
	private JLabel heading = new JLabel();
	private JLabel locationlabel = new JLabel();
	private JLabel offsetlabel = new JLabel();
	private JLabel sizelabel = new JLabel();
	private JLabel celslabel = new JLabel();
	private JLabel eventslabel = new JLabel();
	private JLabel framelabel = new JLabel();
	private JLabel transparencylabel = new JLabel();
	private JCheckBox visiblelabel = new JCheckBox();

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (visiblelabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return visiblelabel.getBackground() ; }
   };

	// Create specialized listeners for events.

	MouseListener mouseListListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
           	int item = LIST.getSelectedIndex() ;
            if (item < 0) return ;
		      for (int i = 0 ; ; i++)
		      {
					cel = celgroup.getCel(i) ;
					if (cel == null) return ;
               if (item-- == 0) break ;
				}
				CelDialog cd = new CelDialog(me,cel,null,config) ;
				cd.show() ;
			}
		}
	} ;

	MouseListener mouseTreeListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
         if (SwingUtilities.isRightMouseButton(e)) return ;
        	if (e.getClickCount() == 2)
         {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					TREE.getLastSelectedPathComponent();
				if (node == null) return;

            // Invoke the appropriate event or action dialog.

				Object o = node.getUserObject();
            if (o instanceof FKissEvent || o instanceof FKissAction)
            {
					EventDialog ed = new EventDialog(me,o,config) ;
					ed.show() ;
            }
			}
		}
	} ;

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      { VIEW.setEnabled(true); }
	} ;

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
			if (node == null) return;
			Object o = node.getUserObject();
         if (o instanceof FKissEvent) VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage")) ;
         if (o instanceof FKissAction) VIEW.setText(Kisekae.getCaptions().getString("ViewActionMessage")) ;
        	VIEW.setEnabled((o instanceof FKissEvent) || (o instanceof FKissAction));
      }
	} ;

	TreeExpansionListener treeExpander = new TreeExpansionListener()
   {
		public void treeExpanded(TreeExpansionEvent e)
		{ ++expansions ; }
		public void treeCollapsed(TreeExpansionEvent e)
		{ --expansions ; }
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

   public CelGroupDialog(JDialog f, CelGroup g, Configuration c)
   { super(f,null,false) ; init(g,c) ; }

	public CelGroupDialog(JFrame f, CelGroup g, Configuration c)
	{ super(f,null,false) ; init(g,c) ; }

   private void init(CelGroup g, Configuration c)
   {
      String title = Kisekae.getCaptions().getString("CelGroupDialogTitle") ;
      if (g.getIdentifier() != null) title += " " + g.getIdentifier() ;
      if (g.getName() != null) title += " " + g.getName() ;
      if (g.isInternal()) title += " [" + Kisekae.getCaptions().getString("InternalStateText") + "]" ;
      if (g.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
      setTitle(title) ;
      me = this ;
      config = c ;
		celgroup = g ;

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

		// Center the frame in the panel space.

      setValues() ;
		doLayout() ;
      Dimension d1 = getParentSize() ;
      Dimension d2 = getSize() ;
      if (d1.height > d2.height) d2.height = d1.height ;
      if (d1.width > d2.width) d2.width = d1.width ;
      setSize(d2) ;
      center(this) ;
      validate() ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		LISTBTN.addActionListener(this) ;
      TransparencyButton.addActionListener(this) ;
		LIST.addMouseListener(mouseListListener) ;
      LIST.addListSelectionListener(listListener) ;
		TREE.addMouseListener(treeBreakpoint) ;
		TREE.addMouseListener(mouseTreeListener) ;
      TREE.addTreeSelectionListener(treeListener);
      TREE.addTreeExpansionListener(treeExpander);
      visiblelabel.addActionListener(this) ;
		addWindowListener(this);
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
 		panel1.setPreferredSize(new Dimension(620, 410));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      OK.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyOKButton"));
      CANCEL.setText(Kisekae.getCaptions().getString("ReturnMessage"));
      CANCEL.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyCancelButton"));
      CANCEL.setEnabled(parent instanceof KissDialog);
		VIEW.setEnabled(false);
      VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewImageButton"));
      VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage"));
      LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage"));
      LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setShowsRootHandles(true);
      TransparencyButton.setMaximumSize(new Dimension(10, 10));
      TransparencyButton.setMinimumSize(new Dimension(10, 10));
      TransparencyButton.setPreferredSize(new Dimension(10, 10));
		TransparencyButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipTransparency"));
      DummyButton1.setMaximumSize(new Dimension(10, 10));
      DummyButton1.setMinimumSize(new Dimension(10, 10));
      DummyButton1.setPreferredSize(new Dimension(10, 10));
      DummyButton1.setVisible(false);
      DummyButton2.setMaximumSize(new Dimension(10, 10));
      DummyButton2.setMinimumSize(new Dimension(10, 10));
      DummyButton2.setPreferredSize(new Dimension(10, 10));
      DummyButton2.setVisible(false);
      DummyButton3.setMaximumSize(new Dimension(10, 10));
      DummyButton3.setMinimumSize(new Dimension(10, 10));
      DummyButton3.setPreferredSize(new Dimension(10, 10));
      DummyButton3.setVisible(false);

      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("BoundingBoxText")),eb2) ;
      Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
      Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
      Border cb5 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("ObjectContextBoxText")),eb2) ;
		gridLayout2.setColumns(1);
		gridLayout2.setRows(3);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(4);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(3);
		gridLayout5.setColumns(1);
		gridLayout5.setRows(4);

 		jPanel1.setBorder(eb1);
 		jPanel1.setLayout(gridBagLayout1);
		jPanel2.setBorder(eb1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3);
		jPanel3.setBorder(eb4);
		jPanel4.setBorder(cb2);
		jPanel4.setMinimumSize(new Dimension(132, 100));
		jPanel4.setPreferredSize(new Dimension(132, 100));
		jPanel4.setLayout(gridLayout2);
		jPanel5.setBorder(cb3);
		jPanel5.setMinimumSize(new Dimension(132, 100));
		jPanel5.setPreferredSize(new Dimension(132, 100));
		jPanel5.setLayout(borderLayout5);
		jPanel6.setBorder(cb4);
		jPanel6.setMinimumSize(new Dimension(132, 100));
		jPanel6.setPreferredSize(new Dimension(132, 100));
		jPanel6.setLayout(gridLayout4);
		jPanel9.setLayout(gridLayout3);
		jPanel10.setLayout(gridLayout5);

      heading.setText(" ");
      heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		locationlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageLocation"));
		locationlabel.setText(Kisekae.getCaptions().getString("LocationText"));
		offsetlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageOffset"));
		offsetlabel.setText(Kisekae.getCaptions().getString("OffsetText"));
		sizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectSize"));
		sizelabel.setText(Kisekae.getCaptions().getString("SizeText"));
		visiblelabel.setText(Kisekae.getCaptions().getString("VisibleStateText"));
		visiblelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleState"));
      visiblelabel.setBackground(nonFading);
		celslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageCount"));
		celslabel.setText(Kisekae.getCaptions().getString("ImageCountText"));
		eventslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventNumber"));
		eventslabel.setText(Kisekae.getCaptions().getString("EventCountText"));
		framelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCurrentFrame"));
		framelabel.setText(Kisekae.getCaptions().getString("CurrentFrameText"));
		transparencylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipTransparency"));
      transparencylabel.setText(Kisekae.getCaptions().getString("TransparencyText"));

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(locationlabel, null);
		jPanel4.add(sizelabel, null);
		jPanel4.add(offsetlabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 10, 0));
      jPanel5.add(jPanel9,BorderLayout.WEST) ;
		jPanel9.add(celslabel, null);
		jPanel9.add(eventslabel, null);
		jPanel9.add(framelabel, null);
		jPanel9.add(transparencylabel, null);
      jPanel5.add(jPanel10,BorderLayout.EAST) ;
      jPanel10.add(DummyButton1, null);
      jPanel10.add(DummyButton2, null);
      jPanel10.add(DummyButton3, null);
      jPanel10.add(TransparencyButton, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 1), 0, 0));
		jPanel6.add(visiblelabel, null);
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(LISTBTN, null);
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
      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe == null) { close() ; return ; }
      PanelFrame panel = mainframe.getPanel() ;

      // The user cannot change state variable values.

      try
      {
         if (source instanceof JCheckBox)
         {
   			JCheckBox cb = (JCheckBox) source ;
            if (cb == visiblelabel)
            {
               // Capture the edit if we were invoked from the MainFrame.
               if ((getParentFrame() instanceof MainFrame) && panel != null)
                  panel.createVisibilityEdit(celgroup,celgroup.isVisible(),cb.isSelected()) ;
               celgroup.setVisible(cb.isSelected()) ;
               if (panel != null) panel.showpage() ;
            }
            else
   				cb.setSelected(!cb.isSelected()) ;
            return ;
         }

   		// An OK closes the frame.

   		if (source == OK)
   		{
         	close() ;
   			return ;
   		}

   		// A CANCEL closes only this dialog makes the parent visible.

   		if (source == CANCEL)
   		{
            if (parent instanceof KissDialog)
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

     		// The transparency button changes the cel transparency.

     		if (TransparencyButton == source)
     		{
            JDialog pb = new TransparentDialog(me) ;
            Point p = TransparencyButton.getLocationOnScreen() ;
            pb.show() ;
     			return ;
     		}

   		// A List Events updates the list table to display the group events.

         if (command.equals(Kisekae.getCaptions().getString("ListEventsMessage")))
   		{
         	VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage")) ;
            VIEW.setEnabled(false) ;
            LISTBTN.setText(Kisekae.getCaptions().getString("ListImagesMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListImagesButton"));
            expansions = 0 ;
            setValues() ;
   			return ;
   		}

   		// A List Cels updates the list table to display the group cels.

         if (command.equals(Kisekae.getCaptions().getString("ListImagesMessage")))
   		{
         	VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage"));
            VIEW.setEnabled(false) ;
            LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage"));
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
            setValues() ;
   			return ;
   		}

   		// A View Cel brings up a Cel dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewImageMessage")))
   		{
           	int item = LIST.getSelectedIndex() ;
            if (item < 0) return ;
   	      for (int i = 0 ; ; i++)
   	      {
   				cel = celgroup.getCel(i) ;
   				if (cel == null) return ;
               if (item-- > 0) continue ;
   				CelDialog cd = new CelDialog(me,cel,null,config) ;
   				cd.show() ;
   				return ;
            }
   		}

         // A View Event or View Action brings up the event dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewEventMessage")) ||
             command.equals(Kisekae.getCaptions().getString("ViewActionMessage")))
         {
   			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
   				TREE.getLastSelectedPathComponent();
   			if (node == null) return;

            // Invoke the appropriate event or action dialog.

   			Object o = node.getUserObject();
            if (o instanceof FKissEvent || o instanceof FKissAction)
            {
   				EventDialog ed = new EventDialog(me,o,config) ;
   				ed.show() ;
           }
            return ;
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
			System.out.println("CelGroupDialog: Out of memory.") ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("CelGroupDialog: Internal fault, action " + evt.getActionCommand()) ;
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

	public void windowOpened(WindowEvent evt)
   { if (CANCEL.isEnabled()) CANCEL.requestFocus() ; else OK.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt)
   { if (TREE != null) TREE.requestFocus() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// Method to set the dialog field values.  These values are set
   // according to the current context.

	void setValues()
	{
		Rectangle r = celgroup.getBoundingBox() ;
		Point location = celgroup.getLocation() ;
		Point offset = celgroup.getOffset() ;

      // Calculate the overall group event count as the group events plus the
      // potential cel events.

      int celcount = celgroup.getCelCount() ;
      int eventcount = celgroup.getEventCount() ;
      int transparency = celgroup.getTransparency() ;
      for (int i = 0 ; ; i++)
      {
         cel = celgroup.getCel(i) ;
         if (cel == null) break ;
         eventcount += cel.getEventCount() ;
      }

      // Create the text entries.

      String s9 = Kisekae.getCaptions().getString("LocationText")
          + "  (" + location.x + "," + location.y + ")" ;
      String s2 = Kisekae.getCaptions().getString("OffsetText")
          + "  (" + offset.x + "," + offset.y + ")" ;
      String s3 = Kisekae.getCaptions().getString("SizeText")
          + "  (" + r.width + "," + r.height +")" ;
      String s4 = Kisekae.getCaptions().getString("TransparencyText")
          + "  " + (255 - transparency) ;
      String s5 = Kisekae.getCaptions().getString("CurrentFrameText")
          + "  " + celgroup.getFrame() ;
      String s6 = Kisekae.getCaptions().getString("ImageCountText")
          + "  " + celcount ;
      String s7 = Kisekae.getCaptions().getString("EventCountText")
          + "  " + eventcount ;
		locationlabel.setText(s9) ;
		offsetlabel.setText(s2) ;
		sizelabel.setText(s3) ;
		celslabel.setText(s6) ;
      eventslabel.setText(s7) ;
      framelabel.setText(s5) ;
		transparencylabel.setText(s4) ;
      visiblelabel.setSelected(celgroup.isVisible()) ;

      // Set the user interface state where necessary.

      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListEventsMessage")))
      {
      	LISTBTN.setEnabled(eventcount > 0) ;
         showCels() ;
      }
      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListImagesMessage")))
      {
      	LISTBTN.setEnabled(celcount > 0) ;
         showEvents() ;
      }

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // A function to construct the cel list entries.

   private void showCels()
   {
      heading.setText(Kisekae.getCaptions().getString("ImageListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      int len = 60 ;
      format("clear","",0,len) ;
      format("center",Kisekae.getCaptions().getString("CelImageIDText"),0,5) ;
      format("center",Kisekae.getCaptions().getString("CelImageNameText"),6,15) ;
      format("left",Kisekae.getCaptions().getString("CelVisibleText"),22,9) ;
      format("center",Kisekae.getCaptions().getString("CelEventsText"),32,6) ;
      format("left",Kisekae.getCaptions().getString("CelCommentText"),39,20) ;
      String s = format(" ","",0,len) ;
      JLabel columns = new JLabel(s) ;
      columns.setFont(LIST.getFont());
		jScrollPane1.getViewport().add(LIST, null);
		jScrollPane1.setColumnHeaderView(columns);
      listmodel.removeAllElements();

		// Populate the display list with the cel vector contents.

		for (int i = 0 ; ; i++)
		{
			cel = celgroup.getCel(i) ;
			if (cel == null) break ;

			// Put cel in list.

			Rectangle r = cel.getBoundingBox() ;
         String celEventCount = "" + cel.getEventCount() ;
         String comment = cel.getComment() ;
         boolean visible = cel.isVisible() ;
         String visibletext = (visible)
             ? Kisekae.getCaptions().getString("BooleanTrueText")
             : Kisekae.getCaptions().getString("BooleanFalseText") ;
         len = 39 + comment.length() ;
			format("clear","",0,len) ;
			format("right",(cel.getIdentifier()).toString(),0,4) ;
			format("left",cel.getName(),6,15) ;
			format("left",visibletext,22,5) ;
         format("right",celEventCount,32,3) ;
			format("left",comment,39,comment.length()) ;
			s = format(" ","",0,len) ;
			listmodel.addElement(s) ;
		}
	}


   // A function to construct the event tree entries.

   private void showEvents()
   {
      if (expansions > 0) return ;
      heading.setText(Kisekae.getCaptions().getString("EventListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top.setUserObject(Kisekae.getCaptions().getString("EventsNode")) ;
		jScrollPane1.getViewport().add(TREE, null);
		jScrollPane1.setColumnHeaderView(null);

		// Populate the tree with the group event vector contents.

      Vector sorted = new Vector() ;
      Enumeration e = celgroup.getEvents() ;
      appendevents(sorted,e) ;

		// Populate the display list with the cel event vector contents.

      for (int n = 0 ; ; n++)
      {
         cel = celgroup.getCel(n) ;
         if (cel == null) break ;
         e = cel.getEvents() ;
         appendevents(sorted,e) ;
		}

      // Sort the event list.

      Collections.sort(sorted) ;
      e = sorted.elements() ;

 		// Populate the tree with all defined events.

		while (e.hasMoreElements())
		{
        	FKissEvent evt = (FKissEvent) e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
			top.add(eventnode) ;

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

      // Expand the tree.

      TREE.expandRow(0) ;
      TREE.requestFocus() ;
	}

   // We overload the KissDialog close method to release our preview image.

   void close()
   {
   	flush() ;
      super.close() ;
   }

   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing with this dialog window.

   private void flush()
   {
   	me = null ;
      cel = null ;
      celgroup = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		LISTBTN.removeActionListener(this) ;
		LIST.removeMouseListener(mouseListListener) ;
      LIST.removeListSelectionListener(listListener) ;
		TREE.removeMouseListener(treeBreakpoint) ;
		TREE.removeMouseListener(mouseTreeListener) ;
      TREE.removeTreeSelectionListener(treeListener);
      TREE.removeTreeExpansionListener(treeExpander) ;
      visiblelabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
   }



   // Inner classes

   // Dialog class to set the cel group transparency.

   class TransparentDialog extends JDialog
   	implements ActionListener, WindowListener
   {
   	private JDialog parent = null ;

      // User interface objects

   	private JPanel panel1 = new JPanel();
   	private JPanel panel2 = new JPanel();
   	private JPanel panel3 = new JPanel();
   	private JPanel panel4 = new JPanel();
   	private BorderLayout borderLayout1 = new BorderLayout();
   	private GridBagLayout gridBagLayout1 = new GridBagLayout();
      private JTextField transparentValueField = new JTextField(3) ;
      private JSlider transparentSlider;
   	private JLabel label2 = new JLabel();
   	private JLabel label3 = new JLabel();
   	private JButton OK = new JButton();
   	private JButton CANCEL = new JButton();
      private int transparentValue = 0 ;
      private int initialValue = 0 ;
      private int lastValue = 0 ;


   	// Constructor

   	public TransparentDialog(JDialog frame)
   	{
   		// Call the base class constructor to set up our frame.

   		super(frame);
   		parent = frame ;
         setTitle(Kisekae.getCaptions().getString("TransparencyText")) ;
   		setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

         // Set up our control components.

         transparentSlider = new JSlider(0,256,0);
         transparentSlider.setMajorTickSpacing(64);
         transparentSlider.setMinorTickSpacing(16);
         transparentSlider.setExtent(1);
         transparentSlider.setPaintTicks(true);
         transparentSlider.setPaintLabels(true);

   		// Initialize the user interface.

   		try { jbInit(); pack(); }
   		catch(Exception ex)
   		{ ex.printStackTrace(); }

   		// Put the text into the fields.  The user interface works with
         // KiSS transparency values. Cel groups have a transparency value
         // of -1 if not all cels in the group are of the same transparency.

         int cgtransparency = celgroup.getTransparency() ;
         if (cgtransparency < 0) cgtransparency = 255 ;
         initialValue = 255 - cgtransparency ;
         lastValue = initialValue ;
         int transparency = initialValue ;
     		transparentValueField.setText("" + transparency) ;
         transparentSlider.setValue(transparency);

   		// Center the dialog in the panel space.

         doLayout() ;
   		Dimension s = getSize() ;
         if (s.width < 500) s.width = 500 ;
         setSize(s) ;
         center(this) ;

         // Add control action listeners.

         transparentValueField.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               try
               {
                  String str = transparentValueField.getText();
                  transparentValue = (Integer.valueOf(str)).intValue();
                  int relative = transparentValue - lastValue ;
                  if (relative != 0) celgroup.changeTransparency(relative) ;
                  lastValue = transparentValue ;
                  transparentSlider.setValue(transparentValue);
                  updatePreview() ;
               }
               catch (Exception e1) { }
            }
         } ) ;

         transparentSlider.addChangeListener(new ChangeListener()
         {
            public void stateChanged(ChangeEvent e)
            {
               int sliderValue = transparentSlider.getValue();
               transparentValueField.setText(Integer.toString(sliderValue));
               if (!transparentSlider.getValueIsAdjusting())
               {
                  int relative = sliderValue - lastValue ;
                  if (relative != 0) celgroup.changeTransparency(relative) ;
                  lastValue = sliderValue ;
                  updatePreview() ;
               }
            }
         } ) ;

         // Add action listeners.

   		OK.addActionListener(this);
   		CANCEL.addActionListener(this);
   		addWindowListener(this) ;
   	}

      // User interface initialization.

   	private void jbInit() throws Exception
   	{
   		panel1.setLayout(borderLayout1);
   		panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));
   		panel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
   		panel3.setLayout(gridBagLayout1);
   		panel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
   		panel4.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         panel4.setVisible(false);
   		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
   		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
   		getContentPane().add(panel1);
   		panel1.add(panel2, BorderLayout.SOUTH);
         panel2.add(Box.createGlue()) ;
         panel2.add(OK, null);
   		panel2.add(Box.createGlue()) ;
         panel2.add(CANCEL, null);
   		panel2.add(Box.createGlue()) ;
   		panel1.add(panel3, BorderLayout.CENTER);
   		panel3.add(transparentValueField, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
   		panel3.add(transparentSlider, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
   		panel1.add(panel4, BorderLayout.NORTH);
         panel4.add(label3, null) ;
   	}


   	// The action method is used to process control events.

   	public void actionPerformed(ActionEvent evt)
   	{
         Object source = evt.getSource() ;
         MainFrame mainframe = Kisekae.getMainFrame() ;
         if (mainframe == null) { close() ; return ; }
         PanelFrame panel = mainframe.getPanel() ;

   		// An OK adjusts the cel state.

         try
         {
            if (source == OK)
      		{
               int n1 = 0 ;

               // Pick up the input values.

               try { n1 = Integer.parseInt(transparentValueField.getText()) ; }
               catch (NumberFormatException e)
               {
                  panel4.setVisible(true);
                  label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
                  CANCEL.requestFocus();
                  pack() ;
                  return ;
               }

               // Check for validity.

               if (n1 < 0 || n1 > 255)
               {
                  panel4.setVisible(true);
                  label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
                  CANCEL.requestFocus();
                  pack() ;
                  return ;
               }

               // Change the cel transparency as required.  The cels
               // have all been updated.

            	Vector cels = celgroup.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
               	Cel cel = (Cel) cels.elementAt(i) ;
               	int n = cel.getTransparency() ;
	               cel.setInitTransparency(n) ;
	               cel.setInitTransUpdated(true) ;
               }
               if (config != null) config.setUpdated(true) ;
               updatePreview() ;
               if (panel != null)
               {
                  panel.createTransparencyEdit(celgroup,initialValue,lastValue) ;
               	panel.showpage() ;
                  panel.showStatus(null) ;
               }

               // Update our display values.

               setValues() ;
               close() ;
               return ;
            }

      		// A Cancel does nothing.

            if (source == CANCEL)
      		{
               celgroup.changeTransparency(initialValue-lastValue) ;
               updatePreview() ;
               close() ;
               return ;
            }
         }

         // Watch for errors.

         catch (Exception e)
         {
            System.out.println("CelGroupDialog: exception " + e.toString()) ;
            e.printStackTrace() ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("InternalError") +
               "\n" + e.toString() + "\n" +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("InternalError"),
               JOptionPane.ERROR_MESSAGE) ;
         }
   	}

      // Close the dialog.

      private void close() { dispose() ; }


   	// Window Events

   	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
   	public void windowClosed(WindowEvent evt) { }
   	public void windowIconified(WindowEvent evt) { }
   	public void windowDeiconified(WindowEvent evt) { }
   	public void windowActivated(WindowEvent evt) { }
   	public void windowDeactivated(WindowEvent evt) { }
   	public void windowClosing(WindowEvent evt) { close() ; }
   }
}