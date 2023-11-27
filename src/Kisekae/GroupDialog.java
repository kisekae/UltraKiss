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
* GroupDialog class
*
* Purpose:
*
* This class defines an instance of the Kisekae object group dialog.  This dialog
* shows all attributes of a Group object.  It is an instance of a KissDialog.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.awt.image.* ;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Collections ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;


final class GroupDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;						// Reference to ourselves
	private Group group = null ;       				// The Group object
   private Configuration config = null ;			// Our current context
   private PageSet pageset = null ;					// The current pageset
   private Integer page = null ;	  					// The page context
	private Cel cel = null ;    	  					// A cel in the group
	private int expansions1 = 0 ;			  			// Number of tree expansions
	private int expansions2 = 0 ;			  			// Number of tree expansions
   private boolean [] loaded = null ;           // If true, cel[i] was loaded
   private Object listselection = null ;        // Selected list object
   private Vector alarms = null ;               // The alarms referenced

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new ImagePreview();
	private JPanel jPanel8 = new JPanel();
	private JPanel jPanel9 = new JPanel();
	private JPanel jPanel10 = new JPanel();
	private JPanel jPanel11 = new JPanel();
	private JPanel jPanel12 = new JPanel();
	private JPanel jPanel13 = new JPanel();
	private JPanel jPanel14 = new JPanel();
	private JPanel jPanel15 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton VIEWPAGE = new JButton();
	private JButton ADDEVENT = new JButton();
   private JButton LocationButton = new JButton();
   private JButton TransparencyButton = new JButton();
   private JButton LockButton = new JButton();
   private JButton DummyButton1 = new JButton();
   private JButton DummyButton2 = new JButton();
   private JButton DummyButton3 = new JButton();
   private JButton DummyButton4 = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridLayout gridLayout5 = new GridLayout();
	private GridLayout gridLayout6 = new GridLayout();
	private GridLayout gridLayout7 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout5 = new BorderLayout();
	private BorderLayout borderLayout6 = new BorderLayout();
	private BorderLayout borderLayout7 = new BorderLayout();
	private BorderLayout borderLayout8 = new BorderLayout();
	private JScrollPane jScrollPane11 = new JScrollPane();
	private JScrollPane jScrollPane12 = new JScrollPane();
	private JScrollPane jScrollPane13 = new JScrollPane();
	private JList LIST = null ;
   private JTree TREE1 = null ;
   private JTree TREE2 = null ;
 	private DefaultListModel listmodel = new DefaultListModel() ;
	private DefaultMutableTreeNode top1 = new DefaultMutableTreeNode() ;
	private DefaultMutableTreeNode top2 = new DefaultMutableTreeNode() ;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
   private Border eb5 = BorderFactory.createEmptyBorder(10,0,0,0) ;
	private JLabel heading1 = new JLabel();
	private JLabel heading2 = new JLabel();
	private JLabel heading3 = new JLabel();
	private JLabel locationlabel = new JLabel();
	private JLabel offsetlabel = new JLabel();
	private JLabel sizelabel = new JLabel();
	private JLabel flexlabel = new JLabel();
	private JLabel celslabel = new JLabel();
	private JLabel eventslabel = new JLabel();
	private JLabel transparencylabel = new JLabel();
	private JLabel pagelabel = new JLabel();
	private JLabel restrictxlabel = new JLabel();
	private JLabel restrictylabel = new JLabel();
	private JLabel parentlabel = new JLabel();
	private JLabel childlabel = new JLabel();
	private JCheckBox visiblelabel = new JCheckBox();
	private JCheckBox constrainlabel = new JCheckBox();
	private JCheckBox unfixlabel = new JCheckBox();
	private JComboBox pageselect = new JComboBox();

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (visiblelabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return visiblelabel.getBackground() ; }
   };

	// Create specialized listeners for events.
   // Double click on a cel image brings up the CelDialog.

	MouseListener mouseListListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e)) return ;
        	if (e.getClickCount() == 2)
         {
           	int item = LIST.getSelectedIndex() ;
            if (item < 0) return ;
		      for (int i = 0 ; ; i++)
		      {
					cel = group.getCel(i) ;
					if (cel == null) return ;
					if (!cel.isOnSpecificPage(page)) continue ;
               if (item-- <= 0) break ;
				}
				CelDialog cd = new CelDialog(me,cel,group,config) ;
				cd.show() ;
			}
		}
	} ;
   
   // Double click on a tree node image brings up the EventDialog.

	MouseListener mouseTreeListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
         if (!SwingUtilities.isLeftMouseButton(e)) return ;
        	if (e.getClickCount() == 2)
         {
				DefaultMutableTreeNode node = null ;
            
            Object o = e.getSource() ;            
            if (o == TREE1)
               node = (DefaultMutableTreeNode) TREE1.getLastSelectedPathComponent();
            if (o == TREE2)
               node = (DefaultMutableTreeNode) TREE2.getLastSelectedPathComponent();
				if (node == null) return;

            // Invoke the appropriate event or action dialog.

				o = node.getUserObject();
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
      { 
          if (e.getValueIsAdjusting())
          {
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewImageButton"));
            VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage"));
            VIEW.setEnabled(true) ; 
            TREE1.clearSelection() ;
            TREE2.clearSelection() ;
            
            ADDEVENT.setText(Kisekae.getCaptions().getString("EditImageMessage"));
            ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditImageButton"));
            ADDEVENT.setEnabled(true) ; 
            
            listselection = LIST.getSelectedValue();
          }
      }
	} ;

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = null ;
            
         Object o = e.getSource() ;            
         if (o == TREE1)
            node = (DefaultMutableTreeNode) TREE1.getLastSelectedPathComponent();
         if (o == TREE2)
            node = (DefaultMutableTreeNode) TREE2.getLastSelectedPathComponent();
   		if (node == null) return;

   		ADDEVENT.setText(Kisekae.getCaptions().getString("EditEventsMessage"));
         ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipAddEventButton"));
         ADDEVENT.setEnabled(true) ; 

			o = node.getUserObject();
         if (o instanceof FKissEvent) 
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewEventButton"));
         }
         if (o instanceof FKissAction) 
         {
            VIEW.setText(Kisekae.getCaptions().getString("ViewActionMessage")) ;
            VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewActionButton"));
         }
        	VIEW.setEnabled((o instanceof FKissEvent) || (o instanceof FKissAction));
         
         if (e.getSource() == TREE1) TREE2.clearSelection() ;
         if (e.getSource() == TREE2) TREE1.clearSelection() ;       
         LIST.clearSelection();
         listselection = null ;
      }
	} ;

   ItemListener pageListener = new ItemListener()
   {
     	public void itemStateChanged(ItemEvent e)
      {
        	page = (Integer) pageselect.getSelectedItem() ;
         Object cid = (config == null) ? null : config.getID() ;
         PageSet ps = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,page) ;
			((ImagePreview) jPanel7).setImage(getImage(page)) ;
         setPageContext(ps) ;
         setValues() ;
      }
   } ;

	TreeExpansionListener treeExpander1 = new TreeExpansionListener()
   {
		public void treeExpanded(TreeExpansionEvent e)
		{ ++expansions1 ; }
		public void treeCollapsed(TreeExpansionEvent e)
		{ --expansions1 ; }
	} ;

	TreeExpansionListener treeExpander2 = new TreeExpansionListener()
   {
		public void treeExpanded(TreeExpansionEvent e)
		{ ++expansions2 ; }
		public void treeCollapsed(TreeExpansionEvent e)
		{ --expansions2 ; }
	} ;
   
   // Listener to define the tree right click event handler.
   // A right click sets a breakpoint on the event or action.
   // A middle click sets or clears the breakpoint disable staus.

   MouseListener treeBreakpoint = new MouseListener()
   {
      public void mouseReleased(MouseEvent e)
      {
         int x = e.getX() ;
         int y = e.getY() ;
         
         TreePath path = null ;
         Object o = e.getSource() ;            
         if (o == TREE1)
            path = TREE1.getPathForLocation(x,y) ;
         if (o == TREE2)
            path = TREE2.getPathForLocation(x,y) ;
   		if (path == null) return;

         o = path.getLastPathComponent() ;
         if (!(o instanceof DefaultMutableTreeNode)) return ;
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) o ;
         o = node.getUserObject() ;
         
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
         }
         
         // Ctrl clicks show the event editor.
         
         if (e.isControlDown())
			{
            editEvent(o) ;
   			return ;
   		}

         TREE1.repaint() ;
         TREE2.repaint() ;
      }

      public void mousePressed(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
   } ;


	// Constructor

   public GroupDialog(JDialog f, Group g, PageSet p, Configuration c)
   { super(f,null,false) ; init(g,p,c) ; }

	public GroupDialog(JFrame f, Group g, PageSet p, Configuration c)
	{ super(f,null,false) ; init(g,p,c) ; }

   private void init(Group g, PageSet p, Configuration c)
   {
      loaded = new boolean [g.getCelCount()] ;
		page = (p == null) ? null : (Integer) p.getIdentifier() ;
      String title = Kisekae.getCaptions().getString("GroupDialogTitle") ;
      if (g.getIdentifier() != null) title += " " + g.getIdentifier() ;
      if (g.getName() != null) title += " " + g.getName() ;
      if (g.isInternal()) title += " [" + Kisekae.getCaptions().getString("InternalStateText") + "]" ;
      if (g.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
      setTitle(title) ;
		setPageContext(p) ;
      pageset = p ;
      me = this ;
      config = c ;
		group = g ;

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

      Dimension d1 = getParentSize() ;
      Dimension d2 = getSize() ;
      if (d1.height > d2.height) d2.height = d1.height ;
      if (d1.width > d2.width) d2.width = d1.width ;
      setSize(d2) ;
 		center(this) ;

		// Set the initial values according to the group page context.

		for (int i = 0 ; i < c.getPageCount() ; i++)
		{
      	Integer pg = new Integer(i) ;
			if (!g.isOnPage(pg)) continue ;
         pageselect.addItem(pg) ;
		}

      if (page != null) pageselect.setSelectedItem(page) ;
      page = (Integer) pageselect.getSelectedItem() ;
      setValues() ;
      validate() ;
		((ImagePreview) jPanel7).setImage(getImage(page)) ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		VIEWPAGE.addActionListener(this) ;
		ADDEVENT.addActionListener(this) ;
      LocationButton.addActionListener(this) ;
      TransparencyButton.addActionListener(this) ;
      LockButton.addActionListener(this) ;
		LIST.addMouseListener(mouseListListener) ;
      LIST.addListSelectionListener(listListener) ;
		TREE1.addMouseListener(treeBreakpoint) ;
		TREE1.addMouseListener(mouseTreeListener) ;
      TREE1.addTreeSelectionListener(treeListener);
      TREE1.addTreeExpansionListener(treeExpander1);
		TREE2.addMouseListener(treeBreakpoint) ;
		TREE2.addMouseListener(mouseTreeListener) ;
      TREE2.addTreeSelectionListener(treeListener);
      TREE2.addTreeExpansionListener(treeExpander2);
      pageselect.addItemListener(pageListener) ;
      visiblelabel.addActionListener(this) ;
      constrainlabel.addActionListener(this) ;
      unfixlabel.addActionListener(this) ;
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
      VIEWPAGE.setText(Kisekae.getCaptions().getString("ViewPageSetMessage"));
      VIEWPAGE.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSetButton"));
      ADDEVENT.setText(Kisekae.getCaptions().getString("EditImageMessage"));
      ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditImageButton"));
      ADDEVENT.setEnabled(false);
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE1 = new noExpandTree(top1);
		TREE1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE1.setCellRenderer(new FKissCellRenderer());
      TREE1.setShowsRootHandles(true);
		TREE2 = new noExpandTree(top2);
		TREE2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE2.setCellRenderer(new FKissCellRenderer());
      TREE2.setShowsRootHandles(true);
      LocationButton.setMaximumSize(new Dimension(10, 10));
      LocationButton.setMinimumSize(new Dimension(10, 10));
      LocationButton.setPreferredSize(new Dimension(10, 10));
      LocationButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageLocationButton"));
      TransparencyButton.setMaximumSize(new Dimension(10, 10));
      TransparencyButton.setMinimumSize(new Dimension(10, 10));
      TransparencyButton.setPreferredSize(new Dimension(10, 10));
      TransparencyButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipTransparency"));
      LockButton.setMaximumSize(new Dimension(10, 10));
      LockButton.setMinimumSize(new Dimension(10, 10));
      LockButton.setPreferredSize(new Dimension(10, 10));
      LockButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipLockValueButton"));
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
      DummyButton4.setMaximumSize(new Dimension(10, 10));
      DummyButton4.setMinimumSize(new Dimension(10, 10));
      DummyButton4.setPreferredSize(new Dimension(10, 10));
      DummyButton4.setVisible(false);

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
		gridLayout5.setRows(3);
      gridLayout5.setVgap(8);
		gridLayout6.setColumns(1);
		gridLayout6.setRows(4);
      gridLayout6.setVgap(8);
		gridLayout7.setColumns(3);
		gridLayout7.setRows(1);

 		jPanel1.setBorder(eb1);
 		jPanel1.setLayout(gridBagLayout1);
		jPanel2.setBorder(eb1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3);
		jPanel3.setBorder(eb4);
		jPanel4.setBorder(cb2);
		jPanel4.setMinimumSize(new Dimension(132, 100));
		jPanel4.setPreferredSize(new Dimension(132, 100));
		jPanel4.setLayout(borderLayout5);
		jPanel5.setBorder(cb3);
		jPanel5.setMinimumSize(new Dimension(132, 100));
		jPanel5.setPreferredSize(new Dimension(132, 100));
		jPanel5.setLayout(borderLayout6);
		jPanel6.setBorder(cb4);
		jPanel6.setMinimumSize(new Dimension(132, 100));
		jPanel6.setPreferredSize(new Dimension(132, 100));
		jPanel6.setLayout(gridLayout4);
		jPanel7.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel7.setMinimumSize(new Dimension(100, 100));
		jPanel7.setPreferredSize(new Dimension(100, 100));
		jPanel8.setBorder(cb5);
		jPanel8.setLayout(gridBagLayout2);
		jPanel9.setLayout(gridLayout2);
		jPanel10.setLayout(gridLayout5);
		jPanel11.setLayout(gridLayout3);
		jPanel12.setLayout(gridLayout6);
		jPanel13.setLayout(gridLayout7);
		jPanel13.setBorder(eb5);
		jPanel14.setLayout(borderLayout7);
		jPanel14.setBorder(eb4);
		jPanel15.setLayout(borderLayout8);
		jPanel15.setBorder(eb4);

      heading1.setText(" ");
      heading1.setBorder(eb3);
		heading1.setHorizontalAlignment(SwingConstants.CENTER);
      heading2.setText(" ");
      heading2.setBorder(eb3);
		heading2.setHorizontalAlignment(SwingConstants.CENTER);
      heading3.setText(" ");
      heading3.setBorder(eb3);
		heading3.setHorizontalAlignment(SwingConstants.CENTER);
      locationlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageLocation"));
      locationlabel.setText(Kisekae.getCaptions().getString("LocationText"));
      offsetlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageOffset"));
      offsetlabel.setText(Kisekae.getCaptions().getString("OffsetText"));
      sizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectSize"));
      sizelabel.setText(Kisekae.getCaptions().getString("SizeText"));
      flexlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipFlexValue"));
      flexlabel.setText(Kisekae.getCaptions().getString("LockValueText"));
      visiblelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleState"));
      visiblelabel.setText(Kisekae.getCaptions().getString("VisibleStateText"));
      visiblelabel.setBackground(nonFading);
		constrainlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipConstrained"));
		constrainlabel.setText(Kisekae.getCaptions().getString("ConstrainedText"));
      constrainlabel.setBackground(nonFading);
		unfixlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipUnfixTransition"));
		unfixlabel.setText(Kisekae.getCaptions().getString("UnfixTransitionText"));
      unfixlabel.setEnabled(false);
      unfixlabel.setBackground(nonFading);
		celslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageCount"));
		celslabel.setText(Kisekae.getCaptions().getString("ImageCountText"));
      eventslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventNumber"));
      eventslabel.setText(Kisekae.getCaptions().getString("EventCountText"));
      transparencylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipTransparency"));
      transparencylabel.setText(Kisekae.getCaptions().getString("TransparencyText"));
		pagelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSet"));
		pagelabel.setText(Kisekae.getCaptions().getString("PageSetText"));
		restrictxlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipXRestriction"));
		restrictxlabel.setText(Kisekae.getCaptions().getString("XRestrictionText"));
		restrictylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipYRestriction"));
		restrictylabel.setText(Kisekae.getCaptions().getString("YRestrictionText"));
		parentlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipParent"));
		parentlabel.setText(Kisekae.getCaptions().getString("ParentText"));
		childlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipChild"));
		childlabel.setText(Kisekae.getCaptions().getString("ChildText"));
		pageselect.setMinimumSize(new Dimension(50, 24));
		pageselect.setPreferredSize(new Dimension(50, 24));
		pageselect.setMaximumRowCount(OptionsDialog.getMaxPageSet());

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      jPanel4.add(jPanel9,BorderLayout.WEST) ;
		jPanel9.add(locationlabel, null);
		jPanel9.add(sizelabel, null);
		jPanel9.add(offsetlabel, null);
      jPanel4.add(jPanel10,BorderLayout.EAST) ;
      jPanel10.add(LocationButton, null);
      jPanel10.add(DummyButton1, null);
      jPanel10.add(DummyButton4, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 10, 0));
      jPanel5.add(jPanel11,BorderLayout.WEST) ;
		jPanel11.add(celslabel, null);
		jPanel11.add(eventslabel, null);
		jPanel11.add(transparencylabel, null);
		jPanel11.add(flexlabel, null);
      jPanel5.add(jPanel12,BorderLayout.EAST) ;
      jPanel12.add(DummyButton2, null);
      jPanel12.add(DummyButton3, null);
      jPanel12.add(TransparencyButton, null);
      jPanel12.add(LockButton, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 1), 0, 0));
		jPanel6.add(visiblelabel, null);
		jPanel6.add(constrainlabel, null);
		jPanel6.add(unfixlabel, null);
		jPanel1.add(jPanel7, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanel8, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel8.add(pagelabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		jPanel8.add(pageselect, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 8, 0), 0, 0));
		jPanel8.add(VIEWPAGE, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		jPanel8.add(restrictxlabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));
		jPanel8.add(restrictylabel, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		jPanel8.add(parentlabel, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));
		jPanel8.add(childlabel, new GridBagConstraints(2, 2, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
 		jPanel2.add(VIEW, null);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(ADDEVENT, null);
	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
      
      panel1.add(jPanel13, BorderLayout.CENTER);
		jPanel13.add(jPanel3);
		jPanel3.add(heading1, BorderLayout.NORTH);
		jPanel3.add(jScrollPane11, BorderLayout.CENTER);
		jPanel13.add(jPanel14);
		jPanel14.add(heading2, BorderLayout.NORTH);
		jPanel14.add(jScrollPane12, BorderLayout.CENTER);
		jPanel13.add(jPanel15);
		jPanel15.add(heading3, BorderLayout.NORTH);
		jPanel15.add(jScrollPane13, BorderLayout.CENTER);     
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
                  panel.createVisibilityEdit(group,group.isVisible(),cb.isSelected()) ;
               group.setVisible(cb.isSelected()) ;
               if (cb.isSelected()) updatePreview() ;
               if (panel != null) panel.showpage() ;
            }
            else if (cb == constrainlabel)
            {
               group.setConstrain(cb.isSelected()) ;
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

			// An ADDEVENT command invokes the FKiSS Editor to update or
         // create new events.  We reference the active tree with a selection.
         // This contextually shows the current selected object events.

         if (command.equals(Kisekae.getCaptions().getString("EditEventsMessage")))
			{
            int contextTree = 1 ;
   			DefaultMutableTreeNode node = (TREE1 == null) ? null
               : (DefaultMutableTreeNode) TREE1.getLastSelectedPathComponent() ;
   			Object o = (node != null) ? node.getUserObject() : null ;
            if (o == null)
            {
               contextTree = 2;
               node = (TREE2 == null) ? null
               : (DefaultMutableTreeNode) TREE2.getLastSelectedPathComponent() ;
               o = (node != null) ? node.getUserObject() : null ;              
            }
            
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) { hide() ; return ; }

            // If our tree selection was for an FKissAction, call the editor 
            // with the parent FKissEvent.  The action will be shown under
            // the event.
            
            KissObject kiss = null ;
            Object nodeEvent = o ;
            if (nodeEvent instanceof FKissAction)
               nodeEvent = ((FKissAction) nodeEvent).getEvent() ;
            if (nodeEvent instanceof FKissEvent) 
               kiss = ((FKissEvent) nodeEvent).getParentObject() ;

            // If our selection was on the event tree, call the editor with 
            // the property group object.  If our selection was on the alarm
            // tree, call the editor with the object alarms list.

            if (kiss == null && contextTree == 1) 
               kiss = group ;
            if (kiss == null && contextTree == 2) 
            {
               kiss = new Alarm(config.getID(),"") ;
               if (alarms != null)
               {
                  for (int i = 0; i < alarms.size(); i++)
                  {
                     Alarm alarm = (Alarm) alarms.elementAt(i) ;
                     kiss.addEvent(alarm.getEvent("alarm")) ;
                  }
               }
            }
            
            fk = new FKissFrame(config,kiss,o) ;
            fk.setVisible(true) ;
				return ;
			}
         
         if (command.equals(Kisekae.getCaptions().getString("EditImageMessage")))
			{
            Cel cel = getSelectedCel() ;
            if (cel == null) return ;
	         PageSet ps = getPageContext() ;
	         Integer multipalette = (ps != null) ? ps.getMultiPalette() : null ;
	         MainFrame mf = Kisekae.getMainFrame() ;
	         KissMenu menu = mf.getMenu() ;
				ImageFrame cf = new ImageFrame(config,cel,multipalette) ;
	         if (menu instanceof ActionListener)
	           	((ImageFrame) cf).callback.addActionListener((ActionListener) menu);
           	((ImageFrame) cf).callback.addActionListener(this);
	         if (cf != null) cf.show() ;
				return ;
			}

   		// A View Image brings up a Cel dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewImageMessage")))
   		{
           	int c ;
            Object o = LIST.getSelectedValue() ;
            if (!(o instanceof String)) return ;
            String [] s = ((String) o).trim().split(" ") ;
            try { c = Integer.parseInt(s[0]) ; }
            catch (NumberFormatException ex) { return ; }
            cel = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),c) ;	
				if (cel == null) return ;
 				CelDialog cd = new CelDialog(me,cel,group,config) ;
 				cd.show() ;
  				return ;
   		}

   		// A View Page Set invokes the appropriate pageset context dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewPageSetMessage")))
   		{
         	Object o = pageselect.getSelectedItem() ;
            if (o instanceof Integer)
            {
            	Object cid = (config == null) ? null : config.getID() ;
            	PageSet p = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,o) ;
               if (p == null) return ;
               PageSetDialog pd = new PageSetDialog(me,p,config) ;
               pd.show() ;
            }
   			return ;
   		}

         // A View Event or View Action brings up the event dialog.
         // We reference the active tree with a selection.

         if (command.equals(Kisekae.getCaptions().getString("ViewEventMessage")) ||
             command.equals(Kisekae.getCaptions().getString("ViewActionMessage")))
         {
   			DefaultMutableTreeNode node = (TREE1 == null) ? null
               : (DefaultMutableTreeNode) TREE1.getLastSelectedPathComponent() ;
   	       if (node == null)
            {
               node = (TREE2 == null) ? null
               : (DefaultMutableTreeNode) TREE2.getLastSelectedPathComponent() ;
            }
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

   		// The location button changes the cel location.

   		if (LocationButton == source)
   		{
            JDialog pb = new InputDialog(me,0) ;
            Point p = LocationButton.getLocationOnScreen() ;
            pb.setLocation(p.x,p.y) ;
            pb.show() ;
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

   		// The lock button changes the cel lock value.

   		if (LockButton == source)
   		{
            JDialog pb = new InputDialog(me,3) ;
            Point p = LockButton.getLocationOnScreen() ;
            pb.setLocation(p.x,p.y) ;
            pb.show() ;
   			return ;
   		}
         
         // If this is an ImageFrame Callback update our image.
         
         if (command.equals("ImageFrame Callback"))
   		{
            updatePreview() ;
            return ;
         }         
         
   		// An update request from the text edit window has occured.
   		// The configuration memory image file will be updated.

   		if ("TextFrame Callback".equals(evt.getActionCommand()))
   		{
            Object o = evt.getSource() ;
            if (!(o instanceof CallbackButton)) return ;
            o = ((CallbackButton) o).getParentObject() ;
            if (!(o instanceof TextFrame)) return ;
            TextFrame tf = (TextFrame) o ;
            TextObject text = tf.getTextObject() ;
            FKissFrame fk = new FKissFrame(config) ;
            fk.applyNewTextEvent(text,config);
            showEvents() ;
            showAlarms() ;
            repaint() ;
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
			System.out.println("GroupDialog: Out of memory.") ;
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
			System.out.println("GroupDialog: Internal fault, action " + evt.getActionCommand()) ;
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
	public void windowActivated(WindowEvent evt) { repaint() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// Method to set the dialog field values.  These values are set
   // according to the current page context.  Note that no changes
   // should be made to the actual object state as the property 
   // display needs to be non-destructive.

	void setValues()
	{
		Point flex = group.getFlex() ;
		Rectangle r = group.getBoundingBox() ;
		Point location = group.getLocation() ;
		Point offset = group.getOffset() ;
		Integer level = group.getLevel() ;
      int transparency = 255 - group.getTransparency() ;

      // Load the cels if necessary.

      for (int i = 0 ; ; i++)
      {
         cel = group.getCel(i) ;
         if (cel == null) break ;
         if (!cel.isOnPage(page)) continue ;
         if (!cel.isError() && !cel.isLoaded())
         {
            try
            {
               ArchiveFile zip = cel.getZipFile() ;
               if (zip != null)
               {
                  loaded[i] = true ;
                  boolean opened = !zip.isOpen() ;
                  if (opened) zip.open() ;
                  if (zip.isOpen() && config != null) 
                     cel.load(config.getIncludeFiles()) ;
                  if (opened) zip.close() ;
                  if (OptionsDialog.getDebugLoad())
                  {
                     String s = "Load: (object properties) " + cel ;
                     if (cel.isCopy()) s += " [copy]" ;
                     System.out.println(s) ;
                  }
               }
            }
            catch (Exception e) { }
         }
      }

      // Apply the appropriate pageset context if necessary.  Recompute the 
      // group bounding box, level and flex value based upon the specified 
      // page set context.

      if (pageset == null || (page != null && !page.equals(pageset.getIdentifier())))
      {
         r = null ;
         flex = new Point(1,0) ;
         level = new Integer(0) ;

         // Compute the new group bounding box.  This box is relative to base
         // coordinate (0,0).

         for (int i = 0 ; ; i++)
         {
            cel = group.getCel(i) ;
            if (cel == null) break ;
            if (!cel.isOnPage(page)) continue ;
	         Dimension d = cel.getSize() ;
	         Point p = cel.getOffset() ;
            if (r == null)
               r = new Rectangle(p,d) ;
            else
    				r = r.union(new Rectangle(p,d)) ;

            // Retain the identifier of the largest numbered cel in the
            // group.  This identifier represents the minimum overlay
            // level that needs to be considered when the group is drawn.
            // All cels with lower numbers can conceivably overlay this
            // group and all cels with higher numbers cannot.

            Integer id = (Integer) cel.getIdentifier() ;
            if (id.intValue() > level.intValue()) level = id ;
         }

         // Correct the bounding box location for this page.

         if (r == null) r = group.getBoundingBox() ;
      	Object cid = (config == null) ? null : config.getID() ;
   	   PageSet p = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,page) ;
			Point pagelocation = (p == null) ? null
				: p.getGroupPosition((Integer) group.getIdentifier()) ;
			if (pagelocation == null) pagelocation = new Point(0,0) ;
         location = new Point(pagelocation) ;
         offset = new Point(r.x,r.y) ;
			r.x = pagelocation.x ;
			r.y = pagelocation.y ;
      }

      // Calculate the overall group event count.  The cel count is page context
      // sensitive.

      int celcount = 0 ;
      int eventcount = group.getEventCount() ;
      for (int i = 0 ; ; i++)
      {
         cel = group.getCel(i) ;
         if (cel == null) break ;
         if (cel.isOnPage(page)) celcount++ ;
      }

      // Define our movement restriction text values.

      Point restrict = group.getRestrictX() ;
      String xrestrict = (restrict == null) ? Kisekae.getCaptions().getString("NoRestrictionText")
         : "(" + restrict.x + "," + restrict.y + ")" ;
      restrict = group.getRestrictY() ;
      String yrestrict = (restrict == null) ? Kisekae.getCaptions().getString("NoRestrictionText")
         : "(" + restrict.x + "," + restrict.y + ")" ;

      // Define our attached object text values.

      String parent = (group.hasParent())
         ? "#" + group.getParent().getIdentifier() : Kisekae.getCaptions().getString("NoAttachmentText") ;
      String children = Kisekae.getCaptions().getString("NoAttachmentText") ;
      if (group.hasChildren())
      {
         children = "" ;
         Vector v = group.getChildren() ;
         for (int i = 0 ; i < v.size() ; i++)
            children += "#" + ((KissObject) v.elementAt(i)).getIdentifier() + " " ;
      }

      // Create the text entries.

      String s9 = Kisekae.getCaptions().getString("LocationText")
          + "  (" + location.x + "," + location.y + ")" ;
      String s2 = Kisekae.getCaptions().getString("OffsetText")
          + "  (" + offset.x + "," + offset.y + ")" ;
      String s3 = Kisekae.getCaptions().getString("SizeText")
          + "  (" + r.width + "," + r.height +")" ;
      String s8 = Kisekae.getCaptions().getString("TransparencyText")
          + "  " + transparency ;
      String s5 = Kisekae.getCaptions().getString("LockValueText")
          + "  " + flex.y ;
      String s6 = Kisekae.getCaptions().getString("ImageCountText")
          + "  " + celcount ;
      String s7 = Kisekae.getCaptions().getString("EventCountText")
          + "  " + eventcount ;
      String s10 = Kisekae.getCaptions().getString("XRestrictionText")
          + "  " + xrestrict ;
      String s11 = Kisekae.getCaptions().getString("YRestrictionText")
          + "  " + yrestrict ;
      String s12 = Kisekae.getCaptions().getString("ParentText")
          + "  " + parent ;
      String s13 = Kisekae.getCaptions().getString("ChildText")
          + "  " + children ;
      if (flex.y == 0)
         s5 += "  (" + Kisekae.getCaptions().getString("MobileStateText")+ ")" ;
      if (flex.y > 0 && flex.y < OptionsDialog.getMaxFlex())
         s5 += "  (" + Kisekae.getCaptions().getString("StickyStateText")+ ")" ;
      if (flex.y >= OptionsDialog.getMaxFlex())
         s5 += "  (" + Kisekae.getCaptions().getString("FixedStateText")+ ")" ;
		locationlabel.setText(s9) ;
		offsetlabel.setText(s2) ;
		sizelabel.setText(s3) ;
		flexlabel.setText(s5) ;
		celslabel.setText(s6) ;
      eventslabel.setText(s7) ;
		transparencylabel.setText(s8) ;
      restrictxlabel.setText(s10) ;
      restrictylabel.setText(s11) ;
      parentlabel.setText(s12) ;
      childlabel.setText(s13) ;
      unfixlabel.setSelected(group.getUnfix()) ;
      visiblelabel.setSelected(group.isVisible()) ;
      constrainlabel.setSelected(group.isConstrained()) ;

      // Set the user interface state where necessary.

      showCels() ;
      showEvents() ;
      showAlarms() ;
      LIST.setSelectedValue(listselection, true);

      // Adjustment buttons require a configuration.

      LockButton.setEnabled(config != null) ;
      LocationButton.setEnabled(config != null) ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // A function to construct the cel list entries.

   private void showCels()
   {
      heading1.setText(Kisekae.getCaptions().getString("ImageListHeadingText")) ;
      heading1.setToolTipText(Kisekae.getCaptions().getString("ToolTipGroupImageListHeading"));
   	Component c = jScrollPane11.getViewport().getView() ;
      if (c != null) jScrollPane11.getViewport().remove(c) ;
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
		jScrollPane11.getViewport().add(LIST, null);
		jScrollPane11.setColumnHeaderView(columns);
      listmodel.removeAllElements();

		// Populate the display list with the cel vector contents.

		for (int i = 0 ; ; i++)
		{
			cel = group.getCel(i) ;
			if (cel == null) break ;
         if (!cel.isOnSpecificPage(page)) continue ;

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

   // A function to get the cel selected in the list.
   
   private Cel getSelectedCel()
   {
      int item = LIST.getSelectedIndex() ;
      if (item < 0) return null ;
   	for (int i = 0 ; ; i++)
   	{
         cel = group.getCel(i) ;
   		if (cel == null) return null ;
   		if (!cel.isOnPage(page)) continue ;
         if (item-- > 0) continue ;
			return cel ;
      }
   }


   // A function to construct the event tree entries.

   private void showEvents()
   {
      if (expansions1 > 0) return ;
      heading2.setText(Kisekae.getCaptions().getString("GroupEventListHeadingText")) ;
      heading2.setToolTipText(Kisekae.getCaptions().getString("ToolTipGroupEventListHeading"));
   	Component c = jScrollPane12.getViewport().getView() ;
      if (c != null) jScrollPane12.getViewport().remove(c) ;
      top1.removeAllChildren() ;
      TreeModel treemodel = TREE1.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top1.setUserObject(Kisekae.getCaptions().getString("EventsNode")) ;
		jScrollPane12.getViewport().add(TREE1, null);
		jScrollPane12.setColumnHeaderView(null);

		// Create the sorted group event vector contents.

      Vector sorted = new Vector() ;
      Enumeration e = group.getEvents() ;
      appendevents(sorted,e) ;
      Collections.sort(sorted) ;
      e = sorted.elements() ;

 		// Populate the tree with all defined events.

		while (e.hasMoreElements())
		{
        	FKissEvent evt = (FKissEvent) e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
			top1.add(eventnode) ;

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

      for (int i = 0; i < TREE1.getRowCount(); i++) { TREE1.expandRow(i); }
      TREE1.requestFocus() ;
	}   
   

   // A function to construct the alarm tree entries.  We find them by
   // examining timer enties in the object events.

   private void showAlarms()
   {
      if (expansions2 > 0) return ;
      heading3.setText(Kisekae.getCaptions().getString("GroupAlarmListHeadingText")) ;
      heading3.setToolTipText(Kisekae.getCaptions().getString("ToolTipGroupAlarmListHeading"));
   	Component c = jScrollPane13.getViewport().getView() ;
      if (c != null) jScrollPane13.getViewport().remove(c) ;
      top2.removeAllChildren() ;
      TreeModel treemodel = TREE2.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top2.setUserObject(Kisekae.getCaptions().getString("AlarmsNode")) ;
		jScrollPane13.getViewport().add(TREE2, null);
		jScrollPane13.setColumnHeaderView(null);

		// Create the sorted group Alarm event vector contents.  Get the events
      // for the group object.  For each event look for timer actions.  For
      // every timer action identify the alarm.  Then, recursively examine
      // the alarm to see if it has timer actions for other alarms.

      alarms = new Vector() ;
      Vector sorted = new Vector() ;
      Enumeration e = group.getEvents() ;
      appendevents(sorted,e) ;
      Collections.sort(sorted) ;
      e = sorted.elements() ;
		while (e.hasMoreElements())
		{
        	FKissEvent evt = (FKissEvent) e.nextElement() ;
         alarms = findAlarms(evt,alarms) ;
      }
      
 		// Populate the tree with all recognized Alarm events.

      Collections.sort(alarms) ;
		for (int i = 0; i < alarms.size(); i++)
		{
         Alarm alarm = (Alarm) alarms.elementAt(i) ;
         Vector v = alarm.getEvent("alarm") ;
         if (v == null) continue ;
         
         // We access the alarm event to get the FKissEvent and FKissAction
         
         for (int j = 0; j < v.size(); j++)
         {
           	FKissEvent evt = (FKissEvent) v.elementAt(j) ;
            DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
            top2.add(eventnode) ;

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
 		}

      // Expand the tree.

      for (int i = 0; i < TREE2.getRowCount(); i++) { TREE2.expandRow(i); }
      TREE2.requestFocus() ;
	}
 
   
   // Find all alarms identified by timer actions and others that invoke alarms.
   // We just list top level alarms and do not recurse for other alarms.
   
   private Vector findAlarms(FKissEvent evt, Vector alarms)
   {
      Enumeration enum1 = evt.getActions() ;
      if (enum1 == null) return alarms ;
      while (enum1.hasMoreElements())
      {
         String alarmname = null ;
			FKissAction a = (FKissAction) enum1.nextElement() ;
         String name = a.getName() ;
         if (name == null) continue ;
         if (name.startsWith("timer")) 
            alarmname = a.getFirstParameter() ;
         else if (name.startsWith("randomtimer")) 
            alarmname = a.getFirstParameter() ;         
         else if (name.startsWith("ifmapped"))
             alarmname = a.getSecondParameter() ;
         else if (name.startsWith("ifnotmapped"))
             alarmname = a.getSecondParameter() ;
         else if (name.startsWith("iffixed"))
             alarmname = a.getSecondParameter() ;
         else if (name.startsWith("ifnotfixed"))
             alarmname = a.getSecondParameter() ;
         else if (name.startsWith("ifmoved"))
             alarmname = a.getSecondParameter() ;
         else if (name.startsWith("ifnotmoved"))
             alarmname = a.getSecondParameter() ;
         if (alarmname == null) continue ;
         
         Alarm alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(), config.getID(), alarmname) ;
         if (alarm == null) continue ;
         if (alarms.contains(alarm)) continue ;
         alarms.addElement(alarm) ;
         Vector v = alarm.getEvent("alarm") ;
         if (v == null) continue ;
         
         // If we were to recurse ...
         
//         for (int j = 0; j < v.size(); j++)
//         {
//           	alarms = findAlarms((FKissEvent) v.elementAt(j),alarms) ;
//         }
      }
      return alarms ;
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
		TextFrame tf = new TextFrame(ze,is,true,true) ;
//		tf.showLineNumbers(true) ;
		tf.callback.addActionListener(this) ;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      tf.setFKissHelp(true) ;
		tf.setVisible(true) ;
   }


   private Image getImage(Integer page)
   {
   	if (group == null) return null ;
      PanelFrame pf = getPanelContext() ;
      float sf = (pf != null) ? pf.getScaleFactor() : 1.0f ;
		Object cid = (config == null) ? null :config.getID() ;
		return group.getImage(page,cid,sf) ;
   }


   // A function to update the preview image.   This function overloads
   // the KissDialog generic method.

   void updatePreview()
   {
     	if (group == null) return ;
		((ImagePreview) jPanel7).setImage(getImage(page)) ;
		if (parent instanceof KissDialog) ((KissDialog) parent).updatePreview() ;
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
      // Release any loaded images.
       
      for (int i = 0 ; i < loaded.length ; i++)
      {
          if (loaded[i]) 
          {
              Cel c = group.getCel(i) ;
              if (c != null) c.unload() ;
          }
      }

      // Clear references.
      
   	me = null ;
      cel = null ;
      group = null ;
      page = null ;
      pageset = null ;
      config = null ;
      ((ImagePreview) jPanel7).setImage(null) ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		VIEWPAGE.removeActionListener(this) ;
		ADDEVENT.removeActionListener(this) ;
		LIST.removeMouseListener(mouseListListener) ;
      LIST.removeListSelectionListener(listListener) ;
		TREE1.removeMouseListener(treeBreakpoint) ;
		TREE1.removeMouseListener(mouseTreeListener) ;
      TREE1.removeTreeSelectionListener(treeListener);
      TREE1.removeTreeExpansionListener(treeExpander1) ;
		TREE2.removeMouseListener(treeBreakpoint) ;
		TREE2.removeMouseListener(mouseTreeListener) ;
      TREE2.removeTreeSelectionListener(treeListener);
      TREE2.removeTreeExpansionListener(treeExpander2) ;
      pageselect.removeItemListener(pageListener) ;
      visiblelabel.removeActionListener(this) ;
      constrainlabel.removeActionListener(this) ;
      unfixlabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top1 = null ;
      TREE1 = null ;
      top2 = null ;
      TREE2 = null ;
   }



   // Inner classes for input dialogs.


   // Dialog class to set the group location and lock value.

   class InputDialog extends JDialog
   	implements ActionListener, WindowListener
   {
   	private final int LOCATION = 0 ;
   	private final int LOCKVALUE = 3 ;

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
         PageSet ps = getPageContext() ;
         if (type == LOCATION) setTitle(Kisekae.getCaptions().getString("LocationText")) ;
         if (type == LOCKVALUE) setTitle(Kisekae.getCaptions().getString("LockValueText")) ;
   		setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

   		// Initialize the user interface.

   		try { jbInit(); pack(); }
   		catch(Exception ex)
   		{ ex.printStackTrace(); }

   		// Put the text into the fields.

         if (type == LOCATION)
         {
            Point location = group.getLocation() ;
            Integer gid = (Integer) group.getIdentifier() ;
            if (ps != null && pageset != ps)
               location = ps.getGroupPosition(gid) ;
            int x = (location == null) ? 0 : location.x ;
            int y = (location == null) ? 0 : location.y ;
      		field1.setText("" + x) ;
      		field2.setText("" + y) ;
         }

         if (type == LOCKVALUE)
         {
            Point flex = group.getFlex() ;
            int n = (flex != null) ? flex.y : 0 ;
      		field1.setText("" + n) ;
            field2.setVisible(false);
         }

         // Add action listeners.

         pack() ;
         Dimension s = getSize() ;
         if (s.width < 200) { s.width = 200 ; setSize(s) ; }
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
         if (type == LOCATION) { label1.setText("x:"); label2.setText("y:"); }
         if (type == LOCKVALUE) { label1.setText(Kisekae.getCaptions().getString("LockValueText")); }

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
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
         if (type == LOCATION)
         {
      		panel3.add(label2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      		panel3.add(field2, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
               ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
         }
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

            // Change the cel values as required.

            try
            {
               // Location changes reposition the object group on the
               // contextual page.

               if (type == LOCATION)
               {
                  PageSet ps = getPageContext() ;
                  Integer gid = (Integer) group.getIdentifier() ;
                  if (ps != null) ps.setGroupPosition(gid,new Point(n1,n2)) ;
                  if (panel != null && pageset == ps)
                  {
                     Point p = group.getLocation() ;
                     group.setPlacement(n1-p.x,n2-p.y) ;
                     group.drop() ;
                    	panel.showpage() ;
                     panel.showStatus(null) ;
                  }
               }

               // Lock value changes adjust the object group flex value.

               if (type == LOCKVALUE)
               {
                  if (n1 < 0) n1 = 0 ;
                  group.setFlex(new Point(n1+1,n1)) ;
                  if (panel != null) panel.showStatus(null) ;
               }
            }
            catch (Exception e)
            {
      			System.out.println("GroupDialog: exception " + e.toString()) ;
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


   // Dialog class to set the image cel transparency.

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

         transparentSlider = new JSlider(-256,256,0);
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
         // KiSS transparency values.

         initialValue = 255 - group.getTransparency() ;
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
                  if (relative != 0) group.changeTransparency(relative) ;
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
                  if (relative != 0) group.changeTransparency(relative) ;
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
         transparentValueField.setMinimumSize(new Dimension(30,21)) ;
         transparentValueField.setHorizontalAlignment(SwingConstants.RIGHT) ;

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

            // Change the cel transparency as required.  The cels
            // have all been updated.

            try
            {
            	Vector cels = group.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
               	Cel cel = (Cel) cels.elementAt(i) ;
               	int n = cel.getTransparency() ;
	               cel.setInitTransparency(n) ;
	               cel.setInitTransUpdated(true) ;
               }
               if (config != null) config.setUpdated(true) ;
               updatePreview() ;
               
               // Capture the edit if we were invoked from the MainFrame.
               
               if ((getParentFrame() instanceof MainFrame) && panel != null)
               {
                  panel.createTransparencyEdit(group,initialValue,lastValue) ;
               	panel.showpage() ;
                  panel.showStatus(null) ;
               }
            }
            catch (Exception e)
            {
      			System.out.println("GroupDialog: exception " + e.toString()) ;
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
            group.changeTransparency(initialValue-lastValue) ;
            updatePreview() ;
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
