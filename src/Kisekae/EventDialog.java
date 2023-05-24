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
import java.text.NumberFormat ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;

final class EventDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Class attributes

	private static StringBuffer sb = new StringBuffer() ;

	// Dialog attributes

   private JDialog me = null ;								// Reference to ourselves
	private FKissEvent event = null ;      		 		// The event object
	private FKissAction action = null ;				 		// The action object
   private Configuration config = null ;     	 		// The current config
   private Object value = null ;								// The parameter value
   private String name = null ;								// The parameter name
   private Object identifier = null ;						// The event action id
   private Vector cancelstack = null ;						// The internal cancel
   private int paramindex = 0 ;								// The parameter index

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
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEWPARAM = new JButton();
	private JButton ADDEVENT = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
   private JTree TREE = new JTree() ;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode() ;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
	private JLabel heading = new JLabel();
	private JLabel locationlabel = new JLabel();
	private JLabel sizelabel = new JLabel();
	private JLabel countlabel = new JLabel();
	private JLabel actionlabel = new JLabel();
	private JLabel totalactionlabel = new JLabel();
	private JLabel timelabel = new JLabel();
	private JLabel valuelabel = new JLabel();
	private JCheckBox actionenablelabel = new JCheckBox();
	private JCheckBox breakpointlabel = new JCheckBox();
	private JCheckBox statelabel = new JCheckBox();
	private JComboBox paramselect = new JComboBox();
	private Component component1;

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (statelabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return statelabel.getBackground() ; }
   };


	// Create specialized listeners for events.

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
         if (node == null) return ;

         // Invoke the appropriate event or action dialog.

			Object o = node.getUserObject();
         if (o instanceof FKissEvent && (o != event || action != null))
        	{
           	Object o1 = event ;
            if (action != null) o1 = action ;
           	action = null ;
           	event = (FKissEvent) o ;
         }

         if (o instanceof FKissAction && o != action)
        	{
           	Object o1 = event ;
            if (action != null) o1 = action ;
           	action = (FKissAction) o ;
            event = action.getEvent() ;
         }

         setValues() ;
         showParameters() ;
         TreePath path = TREE.getSelectionPath()  ;
			TREE.expandPath(path) ;
			setViewRow(TREE.getRowForPath(path)) ;
   	}
   } ;


   // Listener to define the tree right click event handler.

   MouseListener treeBreakpoint = new MouseListener()
   {
      public void mouseReleased(MouseEvent e)
      {
         if (!e.isMetaDown()) return ;
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
        	if (event != null)
            breakpointlabel.setSelected(event.getBreakpoint()) ;
        	if (action != null)
            breakpointlabel.setSelected(action.getBreakpoint()) ;
         TREE.repaint() ;
      }

      public void mousePressed(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e) { }
   } ;


   ItemListener paramListener = new ItemListener()
   {
     	public void itemStateChanged(ItemEvent e)
      {
        	paramindex = paramselect.getSelectedIndex() ;
        	Object o = paramselect.getSelectedItem() ;
         setParameter(o) ;
      }
   } ;


	// Constructor

   public EventDialog(JDialog f, Object o, Configuration c)
   { super(f,null,false) ; init(o,c) ; }

	public EventDialog(JFrame f, Object o, Configuration c)
	{ super(f,null,false) ; init(o,c) ; }

   private void init(Object o, Configuration c)
   {
      me = this ;
      config = c ;
      cancelstack = new Vector() ;

      // Establist the action and event objects

     	if (o instanceof FKissEvent)
      	{ action = null; event = (FKissEvent) o ; }
     	if (o instanceof FKissAction)
      	{ action = (FKissAction) o ;  event = action.getEvent() ; }

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
      showEvents() ;
      showParameters() ;

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
		ADDEVENT.addActionListener(this) ;
		VIEWPARAM.addActionListener(this) ;
      TREE.addTreeSelectionListener(treeListener);
      TREE.addMouseListener(treeBreakpoint) ;
      paramselect.addItemListener(paramListener) ;
      actionenablelabel.addActionListener(this) ;
      breakpointlabel.addActionListener(this) ;
      statelabel.addActionListener(this) ;
		addWindowListener(this);
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		component1 = Box.createHorizontalStrut(90);
		panel1.setLayout(borderLayout1);
 		panel1.setPreferredSize(new Dimension(620, 410));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      OK.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyOKButton"));
		CANCEL.setText(Kisekae.getCaptions().getString("ReturnMessage"));
      CANCEL.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyCancelButton"));
      CANCEL.setEnabled(parent instanceof KissDialog);
		VIEWPARAM.setMaximumSize(new Dimension(100, 27));
		VIEWPARAM.setMinimumSize(new Dimension(100, 27));
		VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewObjectMessage"));
		ADDEVENT.setText(Kisekae.getCaptions().getString("EditEventsMessage"));
      ADDEVENT.setToolTipText(Kisekae.getCaptions().getString("ToolTipAddEventButton"));
      ADDEVENT.setEnabled(config != null);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
      TREE.setShowsRootHandles(true);
		paramselect.setMinimumSize(new Dimension(50, 24));
		paramselect.setPreferredSize(new Dimension(100, 24));

      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("BoundingBoxText")),eb2) ;
      Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
      Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
      Border cb5 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("ParametersBoxText")),eb2) ;
		gridLayout2.setColumns(1);
		gridLayout2.setRows(3);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(4);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(3);

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
		jPanel5.setLayout(gridLayout3);
		jPanel6.setBorder(cb4);
		jPanel6.setMinimumSize(new Dimension(132, 100));
		jPanel6.setPreferredSize(new Dimension(132, 100));
		jPanel6.setLayout(gridLayout4);
		jPanel7.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel7.setMinimumSize(new Dimension(100, 100));
		jPanel7.setPreferredSize(new Dimension(100, 100));
		jPanel8.setBorder(cb5);
		jPanel8.setLayout(gridBagLayout2);

      heading.setText(" ");
      heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		locationlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageLocation"));
		locationlabel.setText(Kisekae.getCaptions().getString("LocationText"));
		sizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectSize"));
		sizelabel.setText(Kisekae.getCaptions().getString("SizeText"));
		countlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipActivations"));
		countlabel.setText(Kisekae.getCaptions().getString("ActivationsText"));
		actionlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipActionCount"));
		actionlabel.setText(Kisekae.getCaptions().getString("ActionCountText"));
		totalactionlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipAverageActions"));
		totalactionlabel.setText(Kisekae.getCaptions().getString("AverageActionsText"));
		actionenablelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipSkipActions"));
		actionenablelabel.setText(Kisekae.getCaptions().getString("SkipActionsText"));
      actionenablelabel.setEnabled(false);
      actionenablelabel.setBackground(nonFading);
		breakpointlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventBreakpoint"));
		breakpointlabel.setText(Kisekae.getCaptions().getString("EventBreakpointText"));
      breakpointlabel.setBackground(nonFading);
		timelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipAverageTime"));
		timelabel.setText(Kisekae.getCaptions().getString("AverageTimeText"));
		statelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCollisionState"));
		statelabel.setText(Kisekae.getCaptions().getString("CollisionStateText"));
      statelabel.setEnabled(false);
      statelabel.setBackground(nonFading);
		valuelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipParameterValue"));
		valuelabel.setText(Kisekae.getCaptions().getString("ParameterValueText"));

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(locationlabel, null);
		jPanel4.add(sizelabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 10, 0));
      jPanel5.add(countlabel, null);
		jPanel5.add(timelabel, null);
		jPanel5.add(totalactionlabel, null);
		jPanel5.add(actionlabel, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 1), 0, 0));
      jPanel6.add(breakpointlabel, null);
		jPanel6.add(actionenablelabel, null);
		jPanel6.add(statelabel, null);
		jPanel1.add(jPanel7, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanel8, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel8.add(paramselect, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 8, 10), 0, 0));
		jPanel8.add(VIEWPARAM, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		jPanel8.add(valuelabel, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(component1, null);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(ADDEVENT, null);
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

      // The user cannot change state variable values.

      if (source instanceof JCheckBox)
      {
			JCheckBox cb = (JCheckBox) source ;
      	if (source == breakpointlabel)
         {
         	if (action != null) action.setBreakpoint(cb.isSelected()) ;
            else if (event != null) event.setBreakpoint(cb.isSelected()) ;
            repaint() ;
            return ;
         }
			cb.setSelected(!cb.isSelected()) ;
         return ;
      }

		// An OK closes this dialog and all parent dialogs.

		if (source == OK)
		{
      	close() ;
			return ;
		}

		// A CANCEL backs up our display within this event dialog if entries
      // exist on the cancel stack queue.  Otherwise, it closes this dialog
      // and makes and parent dialog visible.

		if (source == CANCEL)
		{
      	int i = cancelstack.size() ;
         if (i > 0)
         {
         	event = null ;
            action = null ;
         	Object o = cancelstack.lastElement() ;
            cancelstack.removeElement(o) ;
            if (o instanceof FKissAction)
            	{ action = (FKissAction) o ; event = action.getEvent() ; }
            if (o instanceof FKissEvent)
            	{ action = null ; event = (FKissEvent) o ; }
            setValues() ;
            showEvents() ;
            showParameters() ;
            return ;
         }

         // Close this dialog.

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
  			DefaultMutableTreeNode node = (TREE == null) ? null
            : (DefaultMutableTreeNode) TREE.getLastSelectedPathComponent() ;
  			Object o = (node != null) ? node.getUserObject() : null ;
         FKissFrame fk = FKissEvent.getBreakFrame() ;
         if (fk != null) 
            hide() ;
         else
         {
            fk = new FKissFrame(config,null,o) ;
            fk.setVisible(true) ;
         }
			return ;
		}

      // A View Alarm shows the alarm event in the event list.

      if (command.equals(Kisekae.getCaptions().getString("ViewAlarmMessage")))
      {
      	if (Kisekae.getCaptions().getString("ViewObjectMessage").equals(VIEWPARAM.getText())) value = identifier ;
        	Object cid = (config == null) ? null : config.getID() ;
         String s = (value == null) ? "" : value.toString().toUpperCase() ;
      	Alarm a = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,s) ;
         if (a == null && name != null)
            a = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,name.toUpperCase()) ;
         if (a == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         Vector events = a.getEvent("alarm") ;
         if (events == null || events.size() == 0) return ;
			Object o1 = event ;
			if (action != null) o1 = action ;
         event = (FKissEvent) events.elementAt(0) ;
         if (o1 != event) cancelstack.addElement(o1);
        	action = null ;
			setValues() ;
         showEvents() ;
         showParameters() ;
         return ;
		}

      // A View Label shows the label event in the event list.

      if (command.equals(Kisekae.getCaptions().getString("ViewLabelMessage")))
      {
         if (Kisekae.getCaptions().getString("ViewObjectMessage").equals(VIEWPARAM.getText())) value = identifier ;
        	Object cid = (config == null) ? null : config.getID() ;
         String s = (value == null) ? "" : value.toString().toUpperCase() ;
      	Module m = (Module) Module.getByKey(Module.getKeyTable(),cid,s) ;
         if (m == null && name != null)
            m = (Module) Module.getByKey(Module.getKeyTable(),cid,name.toUpperCase()) ;
         if (m == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         Vector events = m.getEvent("label") ;
         if (events == null || events.size() == 0) return ;
			Object o1 = event ;
			if (action != null) o1 = action ;
         event = (FKissEvent) events.elementAt(0) ;
         if (o1 == event) return ;
			cancelstack.addElement(o1);
        	action = null ;
			setValues() ;
         showEvents() ;
         showParameters() ;
         return ;
		}

		// A View Audio invokes the appropriate audio context dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewAudioMessage")))
		{
      	if (!(identifier instanceof String)) return ;
        	Audio a = (Audio) Audio.findAudio("\"" + identifier + "\"",config) ;
         if (a == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         AudioDialog ad = new AudioDialog(me,a,config) ;
         ad.show() ;
			return ;
		}

		// A View Video invokes the appropriate video context dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewVideoMessage")))
		{
      	if (!(identifier instanceof String)) return ;
        	Video v = (Video) Video.findCel("\"" + identifier + "\"",config,null) ;
         if (v == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         VideoDialog vd = new VideoDialog(me,v,config) ;
         vd.show() ;
			return ;
		}

		// A View Object invokes the appropriate group context dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewObjectMessage")))
		{
        	Object cid = (config == null) ? null : config.getID() ;
      	Group g = (Group) Group.getByKey(Group.getKeyTable(),cid,value) ;
         if (g == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         GroupDialog gd = new GroupDialog(me,g,getPageContext(),config) ;
         gd.show() ;
			return ;
		}

		// A View Image brings up a Cel dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewImageMessage")))
		{
      	if (!(value instanceof String)) return ;
        	Cel c = (Cel) Cel.findCel("\"" + value + "\"",config,null) ;
         if (c == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         CelDialog cd = new CelDialog(me,c,null,config) ;
         cd.show() ;
			return ;
		}

		// A View Page Set brings up a PageSet dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewPageSetMessage")))
		{
        	Object cid = (config == null) ? null : config.getID() ;
      	PageSet p = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,value) ;
         if (p == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         PageSetDialog pd = new PageSetDialog(me,p,config) ;
         pd.show() ;
			return ;
		}

      // A View Cel Group brings up a CelGroup dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewCelGroupMessage")))
      {
         Object cid = (config == null) ? null : config.getID() ;
         String s = (value == Kisekae.getCaptions().getString("UnknownValueText"))
             ? name : value.toString() ;
         s = (s != null) ? s.toUpperCase() : "" ;
         CelGroup cg = (CelGroup) CelGroup.getByKey(CelGroup.getKeyTable(),cid,s) ;
         if (cg == null)
         {
            showMessage(name + " " + Kisekae.getCaptions().getString("UnknownValueText")) ;
            return ;
         }
         CelGroupDialog cd = new CelGroupDialog(me,cg,config) ;
         cd.show() ;
         return ;
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


	// Method to set the dialog field values.  These values are set
   // according to the current event context.  Note that an event context
   // may not exist.

	void setValues()
	{
   	String title = "" ;
      Rectangle r = null ;
      Vector parameters = null ;
      double runtime = 0 ;
      long activations = 0 ;
      long totalactions = 0 ;
      int paramcount = 0 ;
      int actioncount = 0 ;

      // Set the values for an event item.

      if (event != null)
      {
			title = Kisekae.getCaptions().getString("EventDialogTitle") + " " + event.toStringComment(false) ;
			r = event.getBoundingBox() ;
      	parameters = event.getParameters() ;
         activations = event.getRunCount() ;
         totalactions = event.getTotalActionCount() ;
         runtime = event.getRunTime() ;
         runtime = (activations == 0) ? 0 : (runtime / activations) / 1000000000. ;
         totalactions = (activations == 0) ? 0 : (totalactions / activations) ;
         actioncount = event.getActionCount() ;
         actionlabel.setVisible(true);
         actionenablelabel.setVisible(true);
         statelabel.setVisible(true);
			actionlabel.setVisible(true);
         NumberFormat nf = NumberFormat.getInstance() ;
         nf.setMaximumFractionDigits(9) ;
         nf.setMinimumFractionDigits(9) ;
         nf.setMinimumIntegerDigits(1) ;
			countlabel.setText(Kisekae.getCaptions().getString("ActivationsText") + " " + activations) ;
			timelabel.setText(Kisekae.getCaptions().getString("AverageTimeText") + " " + nf.format(runtime)) ;
			totalactionlabel.setText(Kisekae.getCaptions().getString("AverageActionsText") + " " + totalactions) ;
         totalactionlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipAverageActions"));
			actionlabel.setText(Kisekae.getCaptions().getString("ActionCountText") + " " + actioncount) ;
			breakpointlabel.setText(Kisekae.getCaptions().getString("EventBreakpointText"));
      	breakpointlabel.setSelected(event.getBreakpoint()) ;
			breakpointlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventBreakpoint"));
			actionenablelabel.setText(Kisekae.getCaptions().getString("SkipActionsText"));
   		actionenablelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipSkipActions"));
         actionenablelabel.setSelected(event.getSkip(Thread.currentThread())) ;
			statelabel.setText(Kisekae.getCaptions().getString("CollisionStateText")) ;
   		statelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCollisionState"));
         statelabel.setSelected(event.getState()) ;

         // Special cases.

         if ("alarm".equals(event.getIdentifier()))
         {
           	Object cid = (config == null) ? null : config.getID() ;
            String s1 = event.getFirstParameter() ;
            if (s1 != null) s1 = s1.toUpperCase() ;
				Alarm a = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,s1) ;
            if (a != null)
            {
					statelabel.setText(Kisekae.getCaptions().getString("AlarmEnabledState")) ;
         		statelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmEnabled"));
               statelabel.setSelected(a.isEnabled()) ;
					actionenablelabel.setText(Kisekae.getCaptions().getString("AlarmStoppedState")) ;
         		actionenablelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmStopped"));
               actionenablelabel.setSelected(a.isStopped()) ;
            }
         }
      }

      // Set the values for an action item.

      if (action != null)
      {
      	title = Kisekae.getCaptions().getString("ActionDialogTitle") + " " + action.toStringComment(false,false) ;
			r = action.getBoundingBox() ;
      	parameters = action.getParameters() ;
      	paramcount = (parameters == null) ? 0 : parameters.size() ;
         actionlabel.setVisible(false);
         actionenablelabel.setVisible(false);
         statelabel.setVisible(false);
			actionlabel.setVisible(false);
         NumberFormat nf = NumberFormat.getInstance() ;
         nf.setMaximumFractionDigits(9) ;
         nf.setMinimumFractionDigits(9) ;
         nf.setMinimumIntegerDigits(1) ;
         activations = action.getRunCount() ;
         runtime = action.getRunTime() ;
         runtime = (activations == 0) ? 0 : (runtime / activations) / 1000000000. ;
			countlabel.setText(Kisekae.getCaptions().getString("ActivationsText") + " " + activations) ;
			timelabel.setText(Kisekae.getCaptions().getString("AverageTimeText") + " " + nf.format(runtime)) ;
			totalactionlabel.setText(Kisekae.getCaptions().getString("ParametersText") + " " + paramcount) ;
         totalactionlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipParameters"));
			breakpointlabel.setText(Kisekae.getCaptions().getString("ActionBreakpointText"));
      	breakpointlabel.setSelected(action.getBreakpoint()) ;
			breakpointlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipActionBreakpoint"));
      }

      // Create the common text entries.

      setTitle(title) ;
      if (r == null) r = new Rectangle() ;
		String s1 = Kisekae.getCaptions().getString("LocationText") + " (" + r.x + "," + r.y + ")" ;
		String s2 = Kisekae.getCaptions().getString("SizeText") + " (" + r.width + "," + r.height +")" ;
		locationlabel.setText(s1) ;
		sizelabel.setText(s2) ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // A function to construct the event list entries.

	private void showEvents()
   {
      heading.setText(Kisekae.getCaptions().getString("EventListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top.setUserObject(Kisekae.getCaptions().getString("EventsNode")) ;
		jScrollPane1.setViewportView(TREE);

		// Get the configuration event list from the event handler.

      if (config == null) return ;
      EventHandler handler = config.getEventHandler() ;
      if (handler == null) return ;
      Vector sorted = new Vector() ;
      Enumeration e = handler.getEvents() ;
      appendevents(sorted,e) ;

      // Sort the event list.

      Collections.sort(sorted) ;
      e = sorted.elements() ;
      TreeNode [] expandpath = null ;

		// Populate the tree with all defined events.

		while (e.hasMoreElements())
		{
        	FKissEvent evt = (FKissEvent) e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(evt) ;
			top.add(eventnode) ;
         if (evt == event) expandpath = eventnode.getPath() ;

         // Build the event action hierarchy

         Enumeration enum1 = evt.getActions() ;
         if (enum1 == null) continue ;
         while (enum1.hasMoreElements())
         {
           	FKissAction a = (FKissAction) enum1.nextElement() ;
            DefaultMutableTreeNode actionnode = new DefaultMutableTreeNode(a) ;
				eventnode.add(actionnode) ;
	         if (a == action) expandpath = actionnode.getPath() ;
			}
		}

      // Expand the tree.  Position to display the current event or action.

      if (expandpath == null)
			TREE.expandRow(0) ;
      else
		{
			// Scroll the selected node to the top of the view.

			TreePath path = new TreePath(expandpath) ;
			TREE.setSelectionPath(path) ;
			TREE.expandPath(path) ;
			setViewRow(TREE.getRowForPath(path)) ;
		}
   }


   // A function to construct the event list entries.

	private void showParameters()
   {
      Vector parameters = null ;
      if (event != null) parameters = event.getParameters() ;
      if (action != null) parameters = action.getParameters() ;
      if (paramselect.getItemCount() > 0)
      	paramselect.removeAllItems() ;
      if (parameters != null)
      {
      	for (int i = 0 ; i < parameters.size() ; i++)
         	paramselect.addItem(parameters.elementAt(i)) ;
      }

      // Set the parameter identifier to be the first parameter value.
      // This identifier is used for context Action commands.

      setParameter(paramselect.getItemAt(0)) ;
      identifier = value ;
	}


   // A function to set the parameter context display variables.

   private void setParameter(Object o)
   {
      name = null ;
   	value = null ;
   	Image previewimage = null ;
		valuelabel.setText(" ");
      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewObjectMessage"));
      VIEWPARAM.setToolTipText(null);
   	VIEWPARAM.setEnabled(false) ;
      if (!(o instanceof String)) return ;
      name = (String) o ;
      if (name.length() == 0) return ;

      // Get the parameter value.  The parameter could be a variable.

      if (config != null)
      {
        	Variable variable = config.getVariable() ;
         value = variable.getValue(name,null) ;
         if (value == null) value = Kisekae.getCaptions().getString("UnknownValueText") ;
   		valuelabel.setText(Kisekae.getCaptions().getString("ParameterValueText") + " " + value) ;
      }

      // Set the event action button text based upon the parameter context.

      if (action == null)
      {
//	      if ("alarm".equals(event.getIdentifier()))
//       {
//		      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
//		      VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
//       }
//	      else if ("label".equals(event.getIdentifier()))
//       {
//		      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewLabelMessage")) ;
//		      VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipLabelButton")) ;
//       }
	      if ("col".equals(event.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewPaletteMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteButton")) ;
         }
	      else if ("set".equals(event.getIdentifier()))
         {
		      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewPageSetMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSetButton")) ;
         }
      }

      // Action contexts are more numerous.

		if (action != null)
      {
	      if ("changecol".equals(action.getIdentifier()))
         {
		      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewPaletteMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteButton")) ;
         }
	      else if ("changeset".equals(action.getIdentifier()))
         {
		      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewPageSetMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSetButton")) ;
         }
	      else if ("timer".equals(action.getIdentifier()))
         {
            if (paramindex == 0)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("randomtimer".equals(action.getIdentifier()))
         {
            if (paramindex == 0)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("iffixed".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("ifmapped".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("ifmoved".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("ifnotfixed".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("ifnotmapped".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
	      else if ("ifnotmoved".equals(action.getIdentifier()))
         {
            if (paramindex == 1)
            {
               VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAlarmMessage")) ;
               VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAlarmButton")) ;
            }
         }
			else if ("music".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAudioMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAudioButton")) ;
         }
	      else if ("sound".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewAudioMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipAudioButton")) ;
         }
	      else if ("movie".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewVideoMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipVideoButton")) ;
         }
	      else if ("goto".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewLabelMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipLabelButton")) ;
         }
	      else if ("gosub".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewLabelMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipLabelButton")) ;
         }
	      else if ("gotorandom".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewLabelMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipLabelButton")) ;
         }
	      else if ("gosubrandom".equals(action.getIdentifier()))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewLabelMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipLabelButton")) ;
         }
      }

      // Group parameters are always identified with a leading #.

      if (name.charAt(0) == '#')
      {
	      VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewObjectMessage")) ;
         Group group = Group.findGroup(name,config,null) ;
         if (group != null)
         {
         	PageSet page = getPageContext() ;
            Integer p = (page == null) ? null : (Integer) page.getIdentifier() ;
            PanelFrame pf = getPanelContext() ;
            float sf = (pf != null) ? pf.getScaleFactor() : 1.0f ;
           	Object cid = (config == null) ? null : config.getID() ;
            previewimage = group.getImage(p,cid,sf) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipObjectButton")) ;
		   	VIEWPARAM.setEnabled(true) ;
         }
		}

      // CelGroup parameters are always identified with a leading !.

      if (name.charAt(0) == '!')
      {
         VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewCelGroupMessage")) ;
         CelGroup celgroup = CelGroup.findCelGroup(name,config,null) ;
         if (celgroup != null)
         {
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipCelGroupButton")) ;
            VIEWPARAM.setEnabled(true) ;
         }
      }

      // Cel parameters are always identified with a cel file extension.
      // Movie parameters are identified with a movie file extension.

      if (value instanceof String)
      {
			int n = ((String) value).lastIndexOf('.') ;
 			String extension = (n < 0) ? "" : ((String) value).substring(n).toLowerCase() ;
     		if (ArchiveFile.isImage(extension))
         {
	     		VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewImageMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageButton")) ;
            Cel cel = Cel.findCel(name,config,null) ;
            if (cel != null)
            {
	         	PageSet page = getPageContext() ;
	            Integer p = (page == null) ? null : (Integer) page.getIdentifier() ;
            	previewimage = (p == null) ? cel.getImage() : cel.getImage(p) ;
            }
         }
     		else if (ArchiveFile.isVideo(extension))
         {
            VIEWPARAM.setText(Kisekae.getCaptions().getString("ViewVideoMessage")) ;
            VIEWPARAM.setToolTipText(Kisekae.getCaptions().getString("ToolTipVideoButton")) ;
         }
      }

      // Enable the View button if we have a valid context established.

      if (!(Kisekae.getCaptions().getString("ViewObjectMessage").equals(VIEWPARAM.getText())))
	   	VIEWPARAM.setEnabled(true) ;

      // Set our preview pane image if such an image exists.

      ImagePreview preview = (ImagePreview) jPanel7 ;
		preview.setImage(previewimage) ;
      preview.drawImage() ;
   }

   // A function to set the viewport so that the specified row is visible.

	void setViewRow(int row)
   {
		if (TREE == null) return ;
		if (jScrollPane1 == null) return ;
		JViewport view = jScrollPane1.getViewport() ;
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
      repaint() ;
   }


   // A function to show a status message.

   private void showMessage(String s)
   {
		JOptionPane.showMessageDialog(me, s,
         Kisekae.getCaptions().getString("OptionsDialogInfoTitle"),
         JOptionPane.INFORMATION_MESSAGE) ;
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
      event = null ;
      action = null ;
      value = null ;
      identifier = null ;
      cancelstack = null ;
		((ImagePreview) jPanel7).setImage(null) ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		ADDEVENT.removeActionListener(this) ;
		VIEWPARAM.removeActionListener(this) ;
      TREE.removeTreeSelectionListener(treeListener);
      TREE.removeMouseListener(treeBreakpoint) ;
      paramselect.removeItemListener(paramListener) ;
      actionenablelabel.removeActionListener(this) ;
      breakpointlabel.removeActionListener(this) ;
      statelabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
   }
}







