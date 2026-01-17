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



import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.util.HashSet ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;

final class PageSetDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;								// Reference to ourselves
	private Group group = null ;       				 		// The Group object
   private Configuration config = null ;					// Our current context
   private PageSet pageset = null ;					 		// The current pageset
   private Integer page = null ;	  					 		// The page context
   private Cel cel = null ;									// A cel reference

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new JPanel();
	private JPanel jPanel8 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton LISTBTN = new JButton();
	private JButton ADDEVENT = new JButton();
   private JButton PaletteButton = new JButton();
   private JButton DummyButton1 = new JButton();
   private JButton DummyButton2 = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridLayout gridLayout5 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
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
	private JLabel sizelabel = new JLabel();
	private JLabel palettelabel = new JLabel();
	private JLabel groupslabel = new JLabel();
	private JLabel eventslabel = new JLabel();
	private JCheckBox visiblelabel = new JCheckBox();
	private JCheckBox activelabel = new JCheckBox();

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
           	int g ;
            Object o = LIST.getSelectedValue() ;
            if (!(o instanceof String)) return ;
            String [] s = ((String) o).trim().split(" ") ;
            try { g = Integer.parseInt(s[0]) ; }
            catch (NumberFormatException ex) { return ; }
            group = (Group) Group.getByKey(Group.getKeyTable(),config.getID(),g) ;	
				if (group == null) return ;
				GroupDialog gd = new GroupDialog(me,group,pageset,config) ;
				gd.show() ;
         }
		}
	} ;

	MouseListener mouseTreeListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e)) return ;
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


	// Constructor

   public PageSetDialog(JDialog f,PageSet p, Configuration c)
   { super(f,null,false) ; init(p,c) ; }

	public PageSetDialog(JFrame f, PageSet p, Configuration c)
	{ super(f,null,false) ; init(p,c) ; }

   private void init(PageSet p, Configuration c)
   {
		page = (Integer) p.getIdentifier() ;
      String title = Kisekae.getCaptions().getString("PageSetDialogTitle") ;
      if (p.getIdentifier() != null) title += " " + p.getIdentifier() ;
      if (p.isInternal()) title += " [" + Kisekae.getCaptions().getString("InternalStateText") + "]" ;
      if (p.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
      setTitle(title) ;
      setPageContext(p) ;
      pageset = p ;
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

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		LISTBTN.addActionListener(this) ;
		ADDEVENT.addActionListener(this) ;
      PaletteButton.addActionListener(this) ;
		LIST.addMouseListener(mouseListListener) ;
      LIST.addListSelectionListener(listListener) ;
		TREE.addMouseListener(mouseTreeListener) ;
		TREE.addMouseListener(treeBreakpoint) ;
      TREE.addTreeSelectionListener(treeListener);
      visiblelabel.addActionListener(this) ;
      activelabel.addActionListener(this) ;
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
      VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewObjectButton"));
		VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage"));
		LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage"));
      LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
		ADDEVENT.setText(Kisekae.getCaptions().getString("EditEventsMessage"));
      ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipAddEventButton"));
      ADDEVENT.setEnabled(config != null);
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
      TREE.setShowsRootHandles(true);
      PaletteButton.setMaximumSize(new Dimension(10, 10));
      PaletteButton.setMinimumSize(new Dimension(10, 10));
      PaletteButton.setPreferredSize(new Dimension(10, 10));
		PaletteButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteGroupButton"));
      DummyButton1.setMaximumSize(new Dimension(10, 10));
      DummyButton1.setMinimumSize(new Dimension(10, 10));
      DummyButton1.setPreferredSize(new Dimension(10, 10));
      DummyButton1.setVisible(false);
      DummyButton2.setMaximumSize(new Dimension(10, 10));
      DummyButton2.setMinimumSize(new Dimension(10, 10));
      DummyButton2.setPreferredSize(new Dimension(10, 10));
      DummyButton2.setVisible(false);

      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("BoundingBoxText")),eb2) ;
      Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
      Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
		gridLayout2.setColumns(1);
		gridLayout2.setRows(3);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(3);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(3);
		gridLayout5.setColumns(1);
		gridLayout5.setRows(3);
      gridLayout5.setVgap(8);

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
		jPanel5.setLayout(borderLayout4);
		jPanel6.setBorder(cb4);
		jPanel6.setMinimumSize(new Dimension(132, 100));
		jPanel6.setPreferredSize(new Dimension(132, 100));
		jPanel6.setLayout(gridLayout4);
		jPanel7.setLayout(gridLayout3);
		jPanel8.setLayout(gridLayout5);

      heading.setText(" ");
      heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
      locationlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageLocation"));
      locationlabel.setText(Kisekae.getCaptions().getString("LocationText"));
      sizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectSize"));
      sizelabel.setText(Kisekae.getCaptions().getString("SizeText"));
      palettelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteGroup"));
      palettelabel.setText(Kisekae.getCaptions().getString("PaletteGroupText"));
      visiblelabel.setText(Kisekae.getCaptions().getString("VisibleStateText"));
      visiblelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleState"));
      visiblelabel.setBackground(nonFading);
		activelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipActiveState"));
		activelabel.setText(Kisekae.getCaptions().getString("ActiveStateText"));
      activelabel.setEnabled(false);
      activelabel.setBackground(nonFading);
		groupslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectCount"));
		groupslabel.setText(Kisekae.getCaptions().getString("ObjectCountText"));
      eventslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventNumber"));
      eventslabel.setText(Kisekae.getCaptions().getString("EventCountText"));

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(locationlabel, null);
		jPanel4.add(sizelabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 10, 0));
      jPanel5.add(jPanel7,BorderLayout.WEST) ;
		jPanel7.add(groupslabel, null);
		jPanel7.add(eventslabel, null);
		jPanel7.add(palettelabel, null);
      jPanel5.add(jPanel8,BorderLayout.EAST) ;
      jPanel8.add(DummyButton1, null);
      jPanel8.add(DummyButton2, null);
      jPanel8.add(PaletteButton, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 1), 0, 0));
		jPanel6.add(visiblelabel, null);
  		jPanel6.add(activelabel, null);
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(LISTBTN, null);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(ADDEVENT, null);
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

      // The user cannot change state variable values.

      try
      {
         if (source instanceof JCheckBox) {
            JCheckBox cb = (JCheckBox) source ;
            if (cb == visiblelabel) {
               pageset.setVisible(cb.isSelected()) ;
               mainframe.updateMenu() ;
            }
            else
               cb.setSelected(!cb.isSelected()) ;
            return ;
         }

         // An OK closes the frame.

         if (source == OK) {
            close() ;
            return ;
         }

         // A CANCEL closes only this dialog makes the parent visible.

         if (source == CANCEL) {
            if (parent instanceof KissDialog) {
               ( (KissDialog) parent).setValues() ;
               callback.doClick() ;
               flush() ;
               dispose() ;
               parent = null ;
               getOwner().setVisible(true) ;
            }
            else
               close() ;
            return ;
         }

         // An ADDEVENT command invokes the FKiSS Editor to update or
         // create new events.

         if (source == ADDEVENT) {
            DefaultMutableTreeNode node = (TREE == null) ? null
                : (DefaultMutableTreeNode) TREE.getLastSelectedPathComponent() ;
            Object o = (node != null) ? node.getUserObject() : null ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null)
               hide() ;
            else {
               fk = new FKissFrame(config, pageset, o) ;
               fk.setVisible(true) ;
            }
            return ;
         }

         // A List Events updates the list table to display the group events.

         if (command.equals(Kisekae.getCaptions().getString("ListEventsMessage"))) {
            VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage")) ;
            VIEW.setEnabled(false) ;
            LISTBTN.setText(Kisekae.getCaptions().getString("ListObjectsMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListObjectsButton"));
            setValues() ;
            return ;
         }

         // A List Cels updates the list table to display the group cels.

         if (command.equals(Kisekae.getCaptions().getString("ListObjectsMessage"))) {
            VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage")) ;
            VIEW.setEnabled(false) ;
            LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
            setValues() ;
            return ;
         }

         // A View Group brings up a Group dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewObjectMessage"))) {
           	int g ;
            Object o = LIST.getSelectedValue() ;
            if (!(o instanceof String)) return ;
            String [] s = ((String) o).trim().split(" ") ;
            try { g = Integer.parseInt(s[0]) ; }
            catch (NumberFormatException ex) { return ; }
            group = (Group) Group.getByKey(Group.getKeyTable(),config.getID(),g) ;	
				if (group == null) return ;
            GroupDialog gd = new GroupDialog(me, group, pageset, config) ;
            gd.show() ;
            return ;
         }

         // A View Event or View Action brings up the event dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewEventMessage")) ||
             command.equals(Kisekae.getCaptions().getString("ViewActionMessage"))) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                TREE.getLastSelectedPathComponent() ;
            if (node == null)
               return ;

            // Invoke the appropriate event or action dialog.

            Object o = node.getUserObject() ;
            if (o instanceof FKissEvent || o instanceof FKissAction) {
               EventDialog ed = new EventDialog(me, o, config) ;
               ed.show() ;
            }
            return ;
         }

         // The palette button changes the page set palette group.

         if (PaletteButton == source) {
            JDialog pb = new InputDialog(me, 0) ;
            Point p = PaletteButton.getLocationOnScreen() ;
            pb.setLocation(p.x, p.y) ;
            pb.show() ;
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
         Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         PrintLn.println("PageSetDialog: Out of memory.") ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Watch for internal faults during action events.

      catch (Throwable e)
      {
         Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         PrintLn.println("PageSetDialog: Internal fault, action " + evt.getActionCommand()) ;
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
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// Method to set the dialog field values.  These values are set
   // according to the current page context.

	void setValues()
	{
      // Calculate the overall page set event count as the page events plus
      // the potential group and cel events.

      int groupcount = 0 ;
      int eventcount = pageset.getEventCount() ;
      Dimension d = (config != null) ? config.getSize() : new Dimension() ;
      for (int i = 0 ; ; i++)
      {
         group = pageset.getGroup(i) ;
         if (group == null) break ;
         if (!group.isOnSpecificPage(page)) continue ;
         groupcount++ ;
         eventcount += group.getEventCount() ;
         for (int j = 0 ; ; j++)
         {
	         cel = group.getCel(j) ;
	         if (cel == null) break ;
	         if (!cel.isOnSpecificPage(page)) continue ;
         	eventcount += cel.getEventCount() ;
         }
      }

      // Create the text entries.

      String s1 = Kisekae.getCaptions().getString("LocationText")
          + "  (" + "0" + "," + "0" + ")" ;
      String s3 = Kisekae.getCaptions().getString("SizeText")
          + "  (" + d.width + "," + d.height +")" ;
      String s5 = Kisekae.getCaptions().getString("PaletteGroupText")
          + "  " + pageset.getMultiPalette() ;
      String s6 = Kisekae.getCaptions().getString("ObjectCountText")
          + "  " + groupcount ;
      String s7 = Kisekae.getCaptions().getString("EventCountText")
          + "  " + eventcount ;
		locationlabel.setText(s1) ;
		sizelabel.setText(s3) ;
		palettelabel.setText(s5) ;
		groupslabel.setText(s6) ;
      eventslabel.setText(s7) ;

      // See if this is the active page set.

      PanelFrame pf = getPanelContext() ;
      PageSet ps = (pf != null) ? pf.getPage() : null ;
      activelabel.setSelected(ps == pageset) ;
      visiblelabel.setSelected(pageset.isVisible()) ;

      // Set the user interface state where necessary.

      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListEventsMessage")))
      {
      	LISTBTN.setEnabled(eventcount > 0) ;
         showGroups() ;
      }
      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListObjectsMessage")))
      {
      	LISTBTN.setEnabled(groupcount > 0) ;
         showEvents() ;
      }

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // A function to construct the cel list entries.

   private void showGroups()
   {
      heading.setText(Kisekae.getCaptions().getString("ObjectListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      int len = 60 ;
      format("clear","",0,len) ;
      format("left",Kisekae.getCaptions().getString("ObjectIDText"),0,6) ;
      format("left",Kisekae.getCaptions().getString("ObjectPositionText"),8,12) ;
      format("left",Kisekae.getCaptions().getString("ObjectVisibleText"),21,9) ;
      format("left",Kisekae.getCaptions().getString("ObjectEventsText"),31,6) ;
      format("left",Kisekae.getCaptions().getString("ObjectPrimaryImageText"),39,20) ;
      String s = format(" ","",0,len) ;
      JLabel columns = new JLabel(s) ;
      columns.setFont(LIST.getFont());
		jScrollPane1.getViewport().add(LIST, null);
		jScrollPane1.setColumnHeaderView(columns);
      listmodel.removeAllElements();

		// Populate the display list with the group vector contents.  These
      // groups are specific to the requested page.

		for (int i = 0 ; ; i++)
		{
			group = pageset.getGroup(i) ;
			if (group == null) 
            break ;
         Object o = group.getIdentifier() ;
         int n = ((Integer) o).intValue() ;
         if (!group.isOnSpecificPage(page)) 
            continue ;

			// Put the group in the list.  Identify the group characteristics.

			Point pos = pageset.getGroupPosition((Integer) group.getIdentifier()) ;
         if (pos == null) pos = new Point() ;
         int eventcount = group.getEventCount() ;
         for (int j = 0 ; ; j++)
         {
	         cel = group.getCel(j) ;
	         if (cel == null) break ;
	         if (!cel.isOnSpecificPage(page)) continue ;
         	eventcount += cel.getEventCount() ;
         }
         String groupEventCount = "" + eventcount ;
         boolean visible = group.isVisible() ;
         String comment = group.getComment() ;
         String visibletext = (visible)
             ? Kisekae.getCaptions().getString("BooleanTrueText")
             : Kisekae.getCaptions().getString("BooleanFalseText") ;

         // Compose the output line.

         len = 39 + comment.length() ;
			format("clear","",0,len) ;
			format("right",(group.getIdentifier()).toString(),0,5) ;
			format("left","(" + pos.x + "," + pos.y + ")",8,12) ;
         format("left",visibletext,21,9) ;
         format("right",groupEventCount,32,3) ;
			format("left",comment,39,comment.length()) ;
			s = format(" ","",0,len) ;
			listmodel.addElement(s) ;
		}
	}


   // A function to construct the event tree entries.  The events shown 
   // are specific to the groups and cels on the requested page.

   private void showEvents()
   {
      heading.setText(Kisekae.getCaptions().getString("EventListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top.setUserObject(Kisekae.getCaptions().getString("EventsNode")) ;
		jScrollPane1.getViewport().add(TREE, null);
		jScrollPane1.setColumnHeaderView(null);

		// Populate the tree with the pageset event vector contents.

      Vector sorted = new Vector() ;
      Enumeration e = pageset.getEvents() ;
      appendevents(sorted,e) ;

		// Populate the tree with the group event vector contents.

      for (int n = 0 ; n < pageset.getGroupCount() ; n++)
      {
      	group = pageset.getGroup(n) ;
			if (group == null) break ;
         if (!group.isOnSpecificPage(page)) continue ;
	      e = group.getEvents() ;
         appendevents(sorted,e) ;

			// Populate the tree with the group cel event vector contents.

	      for (int j = 0 ; ; j++)
	      {
	         cel = group.getCel(j) ;
	         if (cel == null) break ;
	         if (!cel.isOnSpecificPage(page)) continue ;
	         e = cel.getEvents() ;
            appendevents(sorted,e) ;
         }
		}
      
      // Collision events (in, out, collide, apart, ...) are duplicated in
      // the sorted event list.  Remove duplicates.
      
      HashSet h = new HashSet(sorted) ;
      sorted.clear() ;
      sorted.addAll(h) ;

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

      TREE.expandRow(0);
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
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
      group = null ;
      cel = null ;
      pageset = null ;
      page = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		LISTBTN.removeActionListener(this) ;
		ADDEVENT.removeActionListener(this) ;
      PaletteButton.removeActionListener(this) ;
		LIST.removeMouseListener(mouseListListener) ;
      LIST.removeListSelectionListener(listListener) ;
		TREE.removeMouseListener(mouseTreeListener) ;
		TREE.removeMouseListener(treeBreakpoint) ;
      TREE.removeTreeSelectionListener(treeListener);
      visiblelabel.removeActionListener(this) ;
      activelabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
   }



   // Inner classes for input dialogs.


   // Dialog class to set the configuration size.

   class InputDialog extends JDialog
   	implements ActionListener, WindowListener
   {
   	private final int PALETTE = 0 ;

   	private JDialog parent = null ;
      private int type = 0 ;

      // User interface objects

   	private JPanel panel1 = new JPanel();
   	private JPanel panel2 = new JPanel();
   	private JPanel panel3 = new JPanel();
   	private JPanel panel4 = new JPanel();
   	private BorderLayout borderLayout1 = new BorderLayout();
   	private GridBagLayout gridBagLayout1 = new GridBagLayout();
   	private JTextField field1 = new JTextField(6);
   	private JTextField field2 = new JTextField(6);
   	private JCheckBox initialOption = new JCheckBox();
   	private JLabel label1 = new JLabel();
   	private JLabel label2 = new JLabel();
   	private JLabel label3 = new JLabel();
   	private JButton OK = new JButton();
   	private JButton CANCEL = new JButton();


   	// Constructor

   	public InputDialog(JDialog frame, int type)
   	{
   		// Call the base class constructor to set up our frame.

   		super(frame);
   		parent = frame ;
         this.type = type ;
         if (type == PALETTE) setTitle(Kisekae.getCaptions().getString("PaletteGroupText")) ;
   		setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

   		// Initialize the user interface.

   		try { jbInit(); pack(); }
   		catch(Exception ex)
   		{ ex.printStackTrace(); }

   		// Put the text into the fields.

         if (type == PALETTE)
         {
            Integer multipalette = pageset.getMultiPalette() ;
            int mp = (multipalette instanceof Integer)
            	? multipalette.intValue() : 0 ;
      		field1.setText("" + mp) ;
            field2.setVisible(false) ;
         }

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
         if (type == PALETTE) label1.setText(Kisekae.getCaptions().getString("PaletteGroupText")) ;

   		getContentPane().add(panel1);
   		panel1.add(panel2, BorderLayout.SOUTH);
         panel2.add(Box.createGlue()) ;
         panel2.add(OK, null);
         panel2.add(Box.createHorizontalStrut(8)) ;
         panel2.add(CANCEL, null);
   		panel2.add(Box.createGlue()) ;
   		panel1.add(panel3, BorderLayout.CENTER);
   		panel3.add(label1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
   		panel3.add(field1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
   		panel1.add(panel4, BorderLayout.NORTH);
         panel4.add(label3, null) ;

    		// Set the default button for an enter key.

    		JRootPane rootpane = getRootPane()  ;
    		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   	}


   	// The action method is used to process control events.

   	public void actionPerformed(ActionEvent evt)
   	{
         Object source = evt.getSource() ;
         MainFrame mainframe = Kisekae.getMainFrame() ;
         if (mainframe == null) { close() ; return ; }
         PanelFrame panel = mainframe.getPanel() ;

   		// An OK adjusts the cel state.

         if (source == OK)
   		{
            int n1 = 0 ;
            int n2 = 0 ;

            // Pick up the input values.

            try
            {
               if (field1.isVisible())
                  n1 = Integer.parseInt(field1.getText()) ;
               if (field2.isVisible())
                  n2 = Integer.parseInt(field2.getText()) ;
            }
            catch (NumberFormatException e)
            {
               panel4.setVisible(true);
               label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               CANCEL.requestFocus();
               pack() ;
               return ;
            }

            // Validate the input value. We calculate the number of
            // color sets.  This is defined by the maximum number of palette groups
            // across all palette files.

            int n = 0 ;
            int ncolors = 0 ;
            Palette p = null ;
            while (config != null)
            {
               Palette p1 = (Palette) Palette.getByKey(Palette.getKeyTable(),config.getID(),Integer.valueOf(n++)) ;
               if (p1 == null) break ;
               if (n == 1) p = p1 ;
               int multipalettes = p1.getMultiPaletteCount() ;
               if (multipalettes > ncolors) ncolors = multipalettes ;
            }

            if (n1 < 0 || n1 >= ncolors)
            {
               panel4.setVisible(true) ;
               if (ncolors == 0)
                  label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               else if (n1 < 0)
                  label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               else
                  label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               CANCEL.requestFocus() ;
               pack() ;
               return ;
            }

            // Change the page palette group as required.

            try
            {
               if (type == PALETTE)
               {
               	Integer oldmp = pageset.getInitialMultiPalette() ;
               	Integer newmp = Integer.valueOf(n1) ;
               	pageset.setInitialMultiPalette(newmp) ;
               	pageset.setMultiPalette(newmp) ;
                  if (config != null) config.setUpdated(true) ;
                  if (panel != null)
                  {
                  	panel.createPageSetEdit(pageset,oldmp,newmp);
					      PageSet ps = panel.getPage() ;
                     if (ps == pageset)
                     {
	                  	panel.initcolor(newmp);
	                  	panel.showpage() ;
	                     panel.showStatus(null) ;
                     }
                  }
               }
            }
            catch (Exception e)
            {
      			PrintLn.println("PageSetDialog: exception " + e.toString()) ;
               e.printStackTrace() ;
               JOptionPane.showMessageDialog(parent,
                  Kisekae.getCaptions().getString("InternalError") +
                  "\n" + e.toString() + "\n" +
                  Kisekae.getCaptions().getString("ActionNotCompleted"),
                  Kisekae.getCaptions().getString("InternalError"),
                  JOptionPane.ERROR_MESSAGE) ;
            }

            // Update our display values.

            setValues() ;
            close() ;
            return ;
         }

   		// A Cancel does nothing.

         if (source == CANCEL)
   		{
            close() ;
            return ;
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
