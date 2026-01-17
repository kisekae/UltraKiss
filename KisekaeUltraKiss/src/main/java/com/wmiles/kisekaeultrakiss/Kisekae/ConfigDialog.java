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
* ConfigDialog class
*
* Purpose:
*
* This class defines an instance of the Kisekae configuration dialog.  This
* dialog shows the properties of a configuration object.  It is referenced
* through the Properties menu item for a loaded Kiss configuration.
*
* The playfield size and the background color can be adjusted through
* this dialog.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.io.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.* ;



final class ConfigDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

	private JDialog me = null ;						// Reference to ourselves
   private Configuration config = null ;  		// The current config id
	private TextFrame tf = null ;						// The text edit frame
	private int expansions = 0 ;			  			// Number of tree expansions

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
	private JPanel jPanel9 = new JPanel();
	private JPanel jPanel10 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton LISTBTN = new JButton();
	private JButton ADDEVENT = new JButton();
	private JButton VIEWOBJECTS = new JButton();
	private JButton CONFIGEDIT = new JButton();
   private JButton PanelButton = new JButton();
   private JButton BorderButton = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridLayout gridLayout5 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
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
	private JLabel groupcountlabel = new JLabel();
	private JLabel palettecountlabel = new JLabel();
	private JLabel celcountlabel = new JLabel();
	private JLabel variablecountlabel = new JLabel();
	private JLabel eventcountlabel = new JLabel();
	private JLabel panelsizelabel = new JLabel();
	private JLabel backgroundlabel = new JLabel();
	private JLabel pagecountlabel = new JLabel();
	private JLabel imagelabel = new JLabel();
	private JCheckBox writablelabel = new JCheckBox();
	private JCheckBox updatelabel = new JCheckBox();
	private JCheckBox copylabel = new JCheckBox();
	private JCheckBox visiblelabel = new JCheckBox();

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (visiblelabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return visiblelabel.getBackground() ; }
   };

	// Register for events.

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
					setVisible(false) ;
					ed.show() ;
				}
			}
		}
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
			VIEW.setEnabled(((o instanceof FKissEvent) || (o instanceof FKissAction)));
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

	public ConfigDialog(JDialog f, Configuration c)
	{ super(f,null,false) ; init(c) ; }

	public ConfigDialog(JFrame f, Configuration c)
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

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		ADDEVENT.addActionListener(this) ;
		LISTBTN.addActionListener(this) ;
		VIEWOBJECTS.addActionListener(this) ;
		CONFIGEDIT.addActionListener(this) ;
      PanelButton.addActionListener(this) ;
      BorderButton.addActionListener(this) ;
      TREE.addMouseListener(treeBreakpoint) ;
		TREE.addMouseListener(mouseTreeListener) ;
		TREE.addTreeSelectionListener(treeListener);
      TREE.addTreeExpansionListener(treeExpander);
      writablelabel.addActionListener(this) ;
      copylabel.addActionListener(this) ;
      visiblelabel.addActionListener(this) ;
      updatelabel.addActionListener(this) ;
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
      VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewEventButton"));
		VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage"));
		ADDEVENT.setText(Kisekae.getCaptions().getString("EditEventsMessage"));
      ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipAddEventButton"));
      ADDEVENT.setEnabled(config != null);
		LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage"));
      LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
		VIEWOBJECTS.setText(Kisekae.getCaptions().getString("ViewObjectsMessage"));
      VIEWOBJECTS.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectsButton"));
		CONFIGEDIT.setText(Kisekae.getCaptions().getString("ViewConfigMessage"));
      CONFIGEDIT.setToolTipText(Kisekae.getCaptions().getString("ToolTipConfigButton"));
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
		TREE.setShowsRootHandles(true);
      PanelButton.setMaximumSize(new Dimension(10, 10));
      PanelButton.setMinimumSize(new Dimension(10, 10));
      PanelButton.setPreferredSize(new Dimension(10, 10));
		PanelButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayfieldButton"));
      BorderButton.setPreferredSize(new Dimension(10, 10));
      BorderButton.setMinimumSize(new Dimension(10, 10));
      BorderButton.setMaximumSize(new Dimension(10, 10));
		BorderButton.setToolTipText(Kisekae.getCaptions().getString("ToolTipBorderButton"));

      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
		Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("ObjectsBoxText")),eb2) ;
		Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
		Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
		Border cb5 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("ConfigContextBoxText")),eb2) ;
		gridLayout2.setColumns(1);
		gridLayout2.setRows(4);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(4);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(4);
		gridLayout5.setColumns(1);
		gridLayout5.setRows(4);
      gridLayout5.setVgap(8);

 		jPanel1.setBorder(eb1);
		jPanel1.setLayout(gridBagLayout1);
		jPanel2.setBorder(eb1);
		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3);
		jPanel3.setBorder(eb1);
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
		jPanel7.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel7.setMinimumSize(new Dimension(100, 100));
		jPanel7.setPreferredSize(new Dimension(100, 100));
		jPanel7.setLayout(borderLayout4);
		jPanel8.setBorder(cb5);
		jPanel8.setMinimumSize(new Dimension(100, 70));
		jPanel8.setPreferredSize(new Dimension(100, 70));
		jPanel8.setLayout(gridBagLayout2);
		jPanel9.setLayout(gridLayout3);
		jPanel10.setLayout(gridLayout5);

      heading.setText(" ");
		heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		groupcountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectNumber"));
		groupcountlabel.setText(Kisekae.getCaptions().getString("ObjectsText"));
		palettecountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteNumber"));
		palettecountlabel.setText(Kisekae.getCaptions().getString("PalettesText"));
		celcountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageNumber"));
		celcountlabel.setText(Kisekae.getCaptions().getString("ImagesText"));
		copylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopyState"));
		copylabel.setText(Kisekae.getCaptions().getString("CopyStateText"));
      copylabel.setEnabled(false);
      copylabel.setBackground(nonFading);
		writablelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipWriteState"));
      writablelabel.setText(Kisekae.getCaptions().getString("WritableStateText"));
      writablelabel.setEnabled(false);
      writablelabel.setBackground(nonFading);
		updatelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipUpdateState"));
		updatelabel.setText(Kisekae.getCaptions().getString("UpdatedStateText"));
      updatelabel.setEnabled(false);
      updatelabel.setBackground(nonFading);
		eventcountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventNumber"));
		eventcountlabel.setText(Kisekae.getCaptions().getString("EventCountText"));
		panelsizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayfieldSize"));
		panelsizelabel.setText(Kisekae.getCaptions().getString("PlayfieldSizeText"));
		backgroundlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipBorderColor"));
		backgroundlabel.setText(Kisekae.getCaptions().getString("BorderColorText"));
		variablecountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVariableNumber"));
		variablecountlabel.setText(Kisekae.getCaptions().getString("VariableCountText"));
		visiblelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleState"));
		visiblelabel.setText(Kisekae.getCaptions().getString("VisibleStateText"));
      visiblelabel.setEnabled(false);
      visiblelabel.setBackground(nonFading);
		pagecountlabel.setText(Kisekae.getCaptions().getString("PageSetsText"));
		pagecountlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSetNumber"));
		imagelabel.setHorizontalAlignment(SwingConstants.CENTER);
		imagelabel.setHorizontalTextPosition(SwingConstants.CENTER);


      getContentPane().add(panel1);
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
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(celcountlabel, null);
		jPanel4.add(groupcountlabel, null);
		jPanel4.add(palettecountlabel, null);
		jPanel4.add(pagecountlabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
      jPanel5.add(jPanel9,BorderLayout.WEST) ;
		jPanel9.add(panelsizelabel, null);
		jPanel9.add(backgroundlabel, null);
		jPanel9.add(eventcountlabel, null);
		jPanel9.add(variablecountlabel, null);
      jPanel5.add(jPanel10,BorderLayout.EAST) ;
      jPanel10.add(PanelButton, null);
      jPanel10.add(BorderButton, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel6.add(visiblelabel, null);
		jPanel6.add(writablelabel, null);
		jPanel6.add(updatelabel, null);
		jPanel6.add(copylabel, null);
		jPanel1.add(jPanel7, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel7.add(imagelabel, BorderLayout.CENTER);
		jPanel1.add(jPanel8, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel8.add(VIEWOBJECTS, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
		jPanel8.add(CONFIGEDIT, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
	}



	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

      // The user cannot change state variable values.

      try
      {
         if (source instanceof JCheckBox)
         {
   			JCheckBox cb = (JCheckBox) source ;
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
         // create new events.

			if (source == ADDEVENT)
			{
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) 
            {
//               if (parent != fk) 
//                  fk.reopen(config,null,null) ;
//               else
                  hide() ;
            }
            else
            {
               fk = new FKissFrame(config) ;
               fk.setVisible(true) ;
            }
				return ;
			}

   		// A List Events updates the tree to display the group events.

         if (command.equals(Kisekae.getCaptions().getString("ListEventsMessage")))
   		{
            VIEW.setEnabled(false) ;
            VIEW.setVisible(true) ;
   			LISTBTN.setText(Kisekae.getCaptions().getString("ListTextMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListTextButton"));
            expansions = 0 ;
            setValues() ;
   			return ;
   		}

   		// A List Text updates the list table to display the text values.

         if (command.equals(Kisekae.getCaptions().getString("ListTextMessage")))
   		{
            VIEW.setVisible(false) ;
            VIEW.setEnabled(false) ;
            LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
            setValues() ;
   			return ;
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

   		// A Data Objects request brings up the ObjectDialog dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewObjectsMessage")))
   		{
   			ObjectDialog od = new ObjectDialog(me,config) ;
   			od.show() ;
   			return ;
   		}

   		// The panel button changes the configuration size.

   		if (PanelButton == source)
   		{
            JDialog pb = new PanelSizeDialog(me) ;
            Point p = PanelButton.getLocationOnScreen() ;
            pb.setLocation(p.x,p.y) ;
            pb.setVisible(true) ;
   			return ;
   		}

   		// The border button changes the border color.

   		if (BorderButton == source)
   		{
            JDialog pb = new BorderColorDialog(me) ;
            Point p = BorderButton.getLocationOnScreen() ;
            pb.setVisible(true) ;
   			return ;
   		}

   		// An Active Configuration request brings up the Text Editor.

         if (command.equals(Kisekae.getCaptions().getString("ViewConfigMessage")))
   		{
   			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
   			byte [] configtext = new byte [0] ;
   			try { configtext = config.write() ; } catch (IOException ex) { }
   			InputStream is = new ByteArrayInputStream(configtext) ;
   			tf = new TextFrame(config.getZipEntry(),is,true,true) ;
//   			tf.showLineNumbers(true) ;
   			tf.callback.addActionListener(this) ;
   			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   			tf.setVisible(true) ;
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
   				PrintLn.println("I/O Exception: " + e.toString()) ;
   				e.printStackTrace() ;
   			}
   			finally
   			{
   				try { if (out != null) out.close() ; }
   				catch (IOException e)
   				{
   					PrintLn.println("I/O Exception: " + e.toString()) ;
   					e.printStackTrace() ;
   				}
   			}

   			// Update the configuration memory data and apply the changes.
   			// This creates a new configuration object and invalidates
   			// our current configuration.

   			if (config != null)
   			{
   				ArchiveEntry ze = text.getZipEntry() ;
   				config.setMemoryFile(out.toByteArray(),ze) ;
               MainFrame mf = Kisekae.getMainFrame() ;
   				if (mf != null) mf.init(config) ;
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
         Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			PrintLn.println("ConfigDialog: Out of memory.") ;
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
			PrintLn.println("ConfigDialog: Internal fault, action " + evt.getActionCommand()) ;
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


	// Method to set the dialog field values.

	void setValues()
	{
      String title = Kisekae.getCaptions().getString("ConfigurationDialogTitle") + " " + config.getIdentifier() ;
		if (config.getName() != null) title += " " + config.getName() ;
      if (config.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
		setTitle(title) ;

      // Set field values.

		EventHandler handler = config.getEventHandler() ;
      Dimension size = config.getSize() ;
		int n = (handler == null) ? 0 : handler.getEventCount() ;
      int w = (size == null) ? 0 : size.width ;
      int h = (size == null) ? 0 : size.height ;
		String s1 = Kisekae.getCaptions().getString("ObjectsText") + " " + config.getGroupCount() ;
		String s2 = Kisekae.getCaptions().getString("ImagesText") + " " + config.getCelCount() ;
		String s3 = Kisekae.getCaptions().getString("PalettesText") + " " + config.getPaletteCount() ;
		String s4 = Kisekae.getCaptions().getString("PageSetsText") + " " + config.getPageCount() ;
		String s5 = Kisekae.getCaptions().getString("EventCountText") + " " + n ;
		String s6 = Kisekae.getCaptions().getString("VariableCountText") + " " + config.getVariableCount() ;
		String s7 = Kisekae.getCaptions().getString("BorderColorText") + " " + config.getBorder() ;
		String s8 = Kisekae.getCaptions().getString("PlayfieldSizeText") + " " + "(" + w + "," + h + ")" ;
		groupcountlabel.setText(s1) ;
		celcountlabel.setText(s2) ;
		palettecountlabel.setText(s3) ;
		pagecountlabel.setText(s4) ;
		eventcountlabel.setText(s5) ;
		variablecountlabel.setText(s6) ;
		backgroundlabel.setText(s7) ;
		panelsizelabel.setText(s8) ;
		visiblelabel.setSelected(config.isVisible()) ;
		writablelabel.setSelected(config.isWritable()) ;
		updatelabel.setSelected(config.isUpdated()) ;
		copylabel.setSelected(config.isCopy()) ;
		imagelabel.setIcon(Kisekae.getImageIcon()) ;
      BorderButton.setBackground(config.getBorderColor()) ;

      // Set the user interface state where necessary.

      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListEventsMessage")))
      {
	      LISTBTN.setEnabled(n > 0);
      	VIEW.setVisible(false) ;
         VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage"));
			showText() ;
		}
		if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListTextMessage")))
      {
      	LISTBTN.setEnabled(true) ;
         showEvents() ;
      }

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
	}


   // A function to construct the cel text entries.

	private void showText()
   {
		heading.setText(Kisekae.getCaptions().getString("CharacteristicsHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
		jScrollPane1.getViewport().add(LIST, null);
      listmodel.removeAllElements();
		String fn = null ;
		Object o = config.getZipFile() ;
      if (o instanceof PkzFile) fn = ((PkzFile) o).getName() ;
      if (o instanceof LhaFile) fn = ((LhaFile) o).getName() ;
      if (o instanceof DirFile) fn = ((DirFile) o).getName() ;
      if (fn == null) fn = Kisekae.getCaptions().getString("UnknownValueText") ;
      String fntype = Kisekae.getCaptions().getString("ArchiveText") ;
      if (o instanceof DirFile) fntype = Kisekae.getCaptions().getString("DirectoryText") ;
		listmodel.addElement(Kisekae.getCaptions().getString("NameText") + " " + config.getName()) ;
		listmodel.addElement(fntype + " " + fn) ;
		listmodel.addElement(Kisekae.getCaptions().getString("BytesText") + " " + config.getBytes()) ;

      // Add the configuration lead comment text.

      Vector comment = config.removeSection(config.getLeadComment()) ;
      Enumeration enum1 = comment.elements() ;
      while (enum1.hasMoreElements())
         listmodel.addElement(enum1.nextElement());
	}


   // A function to construct the event list entries.

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

		// Get the configuration event list from the event handler.

		DefaultMutableTreeNode node = top ;
		EventHandler handler = config.getEventHandler() ;
		if (handler != null)
      {
	      Enumeration e = handler.getEvents() ;
         Vector sorted = new Vector() ;
			appendevents(sorted,e) ;

			// Sort the event list.

			Collections.sort(sorted) ;
         e = sorted.elements() ;

			// Populate the tree with all defined events.

			while (e.hasMoreElements())
			{
 	        	FKissEvent evt = (FKissEvent) e.nextElement() ;
            DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
            Enumeration enum1 = evt.getActions() ;
            if (enum1 == null) continue ;
				top.add(eventnode) ;

            // Build the event action hierarchy

            while (enum1.hasMoreElements())
				{
					FKissAction a = (FKissAction) enum1.nextElement() ;
					DefaultMutableTreeNode actionnode = new DefaultMutableTreeNode(a) ;
					eventnode.add(actionnode) ;
				}
         }
		}

		// Expand the tree.

      TREE.expandRow(0);
	}


   // We overload the KissDialog close method.

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
		config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		LISTBTN.removeActionListener(this) ;
		ADDEVENT.removeActionListener(this) ;
		VIEWOBJECTS.removeActionListener(this) ;
		CONFIGEDIT.removeActionListener(this) ;
		TREE.removeMouseListener(mouseTreeListener) ;
      TREE.removeTreeSelectionListener(treeListener);
      TREE.removeTreeExpansionListener(treeExpander) ;
      TREE.removeMouseListener(treeBreakpoint) ;
      writablelabel.removeActionListener(this) ;
      copylabel.removeActionListener(this) ;
      updatelabel.removeActionListener(this) ;
      visiblelabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

		top = null ;
		TREE = null ;
   }


	// The toString method returns a string representation of this object.

   public String toString()
   {
		return Kisekae.getCaptions().getString("PropertiesMessage") ;
   }



   // Inner classes for input dialogs.


   // Dialog class to set the configuration size.

   class PanelSizeDialog extends JDialog
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
   	private JTextField field1 = new JTextField(10);
   	private JTextField field2 = new JTextField(10);
   	private JLabel label1 = new JLabel();
   	private JLabel label2 = new JLabel();
   	private JLabel label3 = new JLabel();
   	private JButton OK = new JButton();
   	private JButton CANCEL = new JButton();


   	// Constructor

   	public PanelSizeDialog(JDialog frame)
   	{
   		// Call the base class constructor to set up our frame.

   		super(frame);
   		parent = frame ;
         setTitle(Kisekae.getCaptions().getString("AdjustPlayfieldSizeTitle")) ;
   		setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

   		// Initialize the user interface.

   		try { jbInit(); pack(); }
   		catch(Exception ex)
   		{ ex.printStackTrace(); }

   		// Put the text into the fields.

         Dimension size = config.getSize() ;
         int w = (size == null) ? 0 : size.width ;
         int h = (size == null) ? 0 : size.height ;
   		field1.setText("" + w) ;
   		field2.setText("" + h) ;

         // Add action listeners.

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
         label1.setText(Kisekae.getCaptions().getString("WidthText"));
         label2.setText(Kisekae.getCaptions().getString("HeightText"));

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
   		panel3.add(label2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
   		panel3.add(field2, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
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

   		// An OK adjusts the configuration size.

         if (source == OK)
   		{
            Dimension size = config.getSize() ;
            int w = (size == null) ? 0 : size.width ;
            int h = (size == null) ? 0 : size.height ;
            int initwidth = w ;
            int initheight = h ;
            try
            {
               w = Integer.parseInt(field1.getText()) ;
               h = Integer.parseInt(field2.getText()) ;
            }
            catch (NumberFormatException e)
            {
               panel4.setVisible(true);
               label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               pack() ;
               return ;
            }

            // Validate the input value.

            if ((w < 0 || w > 2000) || (h < 0 || h > 2000))
            {
               panel4.setVisible(true) ;
               label3.setText(Kisekae.getCaptions().getString("ParameterInvalidText"));
               CANCEL.requestFocus() ;
               pack() ;
               return ;
            }

            // Change the size of the configuration.

            Dimension d = new Dimension(w,h) ;
            if (panel != null)
            	panel.setConfigSize(d) ;
            else
            	config.setSize(d) ;

            // Update our display values.

            config.setUpdated(true) ;
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


   // Dialog class to choose a color for the playfield border.

   class BorderColorDialog extends JDialog
   	implements ActionListener, WindowListener
   {
   	private JDialog parent = null ;
      private JColorChooser chooser = null ;
      private ColorMenu cm = null ;
      private JPopupMenu popup = null ;

      // User interface objects

   	private JPanel panel1 = new JPanel();
   	private JPanel panel2 = new JPanel();
   	private JPanel panel3 = new JPanel();
   	private JPanel panel4 = new JPanel();
   	private JPanel panel5 = new JPanel();
   	private BorderLayout borderLayout1 = new BorderLayout();
   	private BorderLayout borderLayout2 = new BorderLayout();
   	private JLabel label1 = new JLabel();
   	private JLabel label2 = new JLabel();
   	private JButton OK = new JButton();
   	private JButton CANCEL = new JButton();
   	private JButton ColorChooser = new JButton();


   	// Constructor

   	public BorderColorDialog(JDialog frame)
   	{
   		// Call the base class constructor to set up our frame.

   		super(frame);
   		parent = frame ;
         setTitle(Kisekae.getCaptions().getString("SelectBorderColorTitle")) ;
   		setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

   		// Initialize the user interface.

   		try { jbInit(); pack(); }
   		catch(Exception ex)
   		{ ex.printStackTrace(); }

   		// If we have a palette, populate the color menu.  Border color
         // indexes are always relative to palette file 0.

         Palette palette = config.getPalette(0) ;
         cm = buildColorMenu() ;

         // Build the dialog.  If we have a color menu then show it
         // and provide an option to use the color chooser.  If we
         // do not have a palette then show the color chooser.

         if (cm != null)
         {
            popup = cm.getPopupMenu() ;
            popup.setVisible(true) ;
            panel3.add(popup,BorderLayout.CENTER) ;
            cm.setSelectedIndex(config.getBorder()) ;
            chooser = null ;
         }
         else
         {
            chooser = new JColorChooser() ;
            chooser.setPreviewPanel(new JPanel()) ;
            ColorChooser.setEnabled(false) ;
            panel3.add(chooser,BorderLayout.CENTER) ;
            chooser.setColor(config.getBorderColor());
            popup = null ;
         }

         // Add action listeners.

         doLayout() ;
         center() ;
   		OK.addActionListener(this) ;
   		CANCEL.addActionListener(this) ;
         ColorChooser.addActionListener(this) ;
         addWindowListener(this) ;
   	}

      // User interface initialization.

   	private void jbInit() throws Exception
   	{
   		panel1.setLayout(borderLayout1);
   		panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));
   		panel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
   		panel3.setLayout(borderLayout2);
   		panel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

   		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
   		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
   		ColorChooser.setText(Kisekae.getCaptions().getString("ColorChooserMessage"));

   		getContentPane().add(panel1);
   		panel1.add(panel2, BorderLayout.SOUTH);
         panel2.add(Box.createGlue()) ;
         panel2.add(ColorChooser, null);
         panel2.add(Box.createGlue()) ;
         panel2.add(OK, null);
         panel2.add(Box.createGlue()) ;
         panel2.add(CANCEL, null);
   		panel2.add(Box.createGlue()) ;
   		panel1.add(panel3, BorderLayout.CENTER);

    		// Set the default button for an enter key.

    		JRootPane rootpane = getRootPane()  ;
    		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   	}


   	// The action method is used to process control events.

   	public void actionPerformed(ActionEvent evt)
   	{
         Object source = evt.getSource() ;
         String command = evt.getActionCommand() ;

   		// An OK adjusts the border color.

         if (source == OK)
   		{
            MainFrame mainframe = Kisekae.getMainFrame() ;
            if (mainframe == null) { close() ; return ; }
            PanelFrame panel = mainframe.getPanel() ;
            if (panel == null) { close() ; return ; }

            // Adjust the color depending on index or RGB type.

            if (popup != null)
            {
               int n = cm.getColorIndex() ;
               if (n < 0) { close() ; return ; }
               int index = config.getBorder() ;
               if (n == index) { close() ; return ; }
               panel.setBorderIndex(n) ;
            }
            else if (chooser != null)
            {
               Color c = chooser.getColor() ;
               if (c == null) { close() ; return ; }
               int rgb = c.getRGB() & 0xFFFFFF ;
               panel.setBorderRgb(rgb) ;
            }
            else { close() ; return ; }

            // Update our parent display.

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

   		// A Color Chooser updates the dialog display.

         if (command.equals(Kisekae.getCaptions().getString("ColorChooserMessage")))
   		{
            if (popup != null) popup.setVisible(false) ;
            ColorChooser.setText(Kisekae.getCaptions().getString("PaletteMessage")) ;
            chooser = new JColorChooser() ;
            chooser.setPreviewPanel(new JPanel()) ;
            panel3.removeAll() ;
            panel3.add(chooser,BorderLayout.CENTER) ;
            chooser.setColor(config.getBorderColor());
            popup = null ;
            invalidate() ;
            doLayout() ;
            center() ;
            return ;
         }

   		// A Palette request updates the dialog display.

         if (command.equals(Kisekae.getCaptions().getString("PaletteMessage")))
   		{
            ColorChooser.setText(Kisekae.getCaptions().getString("ColorChooserMessage")) ;
            cm = buildColorMenu() ;
            if (cm == null) return ;
            panel3.removeAll() ;
            popup = cm.getPopupMenu() ;
            popup.setVisible(true) ;
            panel3.add(popup,BorderLayout.CENTER) ;
            cm.setSelectedIndex(config.getBorder()) ;
            chooser = null ;
            invalidate() ;
            doLayout() ;
            center() ;
            return ;
         }
   	}

      // Close the dialog.

      private void close() 
      { 
         if (popup != null) popup.setVisible(false) ;
         if (cm != null) cm.setVisible(false) ;
         popup = null ;
         cm = null ;
         tf = null ;
         dispose() ; 
      }


      // A utility function to center our dialog window.

      private void center()
      {
         Dimension s = getPreferredSize() ;
         if (s.width < 350) s.width = 350 ;
         setSize(s) ;
     		Dimension d = Kisekae.getScreenSize() ;
     		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
     		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
     		setLocation(x,y) ;
         validate() ;

         // The popup ColorMenu is a menu item.  It must be positioned
         // where the window is, otherwise it shows at (0,0).
         
         if (popup != null)
         {
            Component c = panel3.getComponent(0) ;
            int x1 = c.getX() ;
            int y1 = c.getY() ;
            int w1 = c.getWidth() ;
            int h1 = c.getHeight() ;
            Point p = SwingUtilities.convertPoint(panel3,x1,y1,null) ;
            popup.setLocation(p.x+x,p.y+y) ;
            popup.setSize(w1,h1) ;
         }
      }
      

      // A utility function to create the color menu object.  A new object
      // is required when we swap from the ColorChooser and our palette.
      
      private ColorMenu buildColorMenu()
      { 
         cm = null ;
         if (config == null) return null ;
         Palette palette = config.getPalette(0) ;
         if (palette != null)
         {
   			int cols = 16 ;
   			int colors = palette.getColorCount() ;
			   int rows = colors / cols + ((colors % cols == 0) ? 0 : 1) ;
            if (colors < cols) cols = colors ;
   			if (rows == 0 && cols == 0) { rows = 1 ; cols = 1 ; }

   			// Set the grid layout.

            cm = new ColorMenu(palette.getName(),rows,cols) ;
            for (int i = 0 ; i < colors ; i++)
               cm.setMenuColor(i,palette.getColor(0,i)) ;
         }
         return cm ;
      }

      
   	// Window Events

   	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
   	public void windowClosed(WindowEvent evt) { close() ; }
   	public void windowIconified(WindowEvent evt) { }
   	public void windowDeiconified(WindowEvent evt) { }
   	public void windowActivated(WindowEvent evt) { }
   	public void windowDeactivated(WindowEvent evt) { }
   	public void windowClosing(WindowEvent evt) { }
   }
}
