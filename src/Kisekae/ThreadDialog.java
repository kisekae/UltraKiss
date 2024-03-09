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
import javax.swing.*;
import javax.swing.event.* ;
import javax.swing.border.*;
import javax.swing.tree.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;


final class ThreadDialog extends KissDialog
	implements ActionListener, WindowListener
{
   private JDialog me = null ;						// Reference to ourselves
	private Configuration config = null ;        // Current configuration
	private String item = null ;                 // Selected item in list
   private int itemindex = -1 ;						// Selected item index
   private boolean pollqueues = false ;			// If true, poll queue state

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
   private JPanel jPanel3 = new JPanel();
	private FlowLayout flowLayout1 = new FlowLayout();
   private GridLayout gridLayout1 = new GridLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
   private JLabel jLabel2 = new JLabel();
   private JLabel jLabel3 = new JLabel();
	private JButton OK = new JButton();
	private JButton SUSPEND = new JButton();
	private JButton VIEW = new JButton();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList LIST = new JList();
   private JTree TREE = null ;
 	private DefaultListModel listmodel = new DefaultListModel() ;
	private DefaultMutableTreeNode top = new DefaultMutableTreeNode() ;

	// Create specialized listeners for events.

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
	         if (o instanceof Cel)
	         {
					CelDialog cd = new CelDialog(me,(Cel) o,null,config) ;
					cd.show() ;
	         }
	         if (o instanceof Alarm)
	         {
            	Alarm a = (Alarm) o ;
		         Vector events = a.getEvent("alarm") ;
		         if (events == null || events.size() == 0) return ;
					EventDialog ed = new EventDialog(me,events.elementAt(0),config) ;
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
        	VIEW.setEnabled(true);
         if (o instanceof FKissEvent) VIEW.setText("View Event") ;
         else if (o instanceof FKissAction) VIEW.setText("View Action") ;
         else if (o instanceof Cel) VIEW.setText("View Cel") ;
         else if (o instanceof Alarm) VIEW.setText("View Alarm") ;
         else
         {
         	VIEW.setEnabled(false) ;
         	VIEW.setText("View Object") ;
			}
      }
	} ;

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
        	Object o = LIST.getSelectedValue() ;
			int index = LIST.getSelectedIndex() ;
			if (o != null && index >= 0)
         {
				item = o.toString() ;
				itemindex = index ;
				String s = (item.length() > 30) ? item.substring(20,29).trim() : "" ;
         	SUSPEND.setText((Kisekae.getCaptions().getString("AlarmSuspendState").equals(s))
               ? Kisekae.getCaptions().getString("ResumeMessage")
               : Kisekae.getCaptions().getString("SuspendMessage")) ;
				SUSPEND.setEnabled(true);
	      	VIEW.setEnabled(Kisekae.getCaptions().getString("AlarmSuspendState").equals(s));
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

   // Periodic display update thread.  The update must be run under the AWT
   // thread as the setValues method adjusts the LIST layout.

   Thread thread = new Thread()
   {
     	Runnable awt = new Runnable()
      { public void run() { setValues() ; repaint() ; } } ;

   	public void run()
      {
      	while (true)
         {
	      	try
	         {
            	if (pollqueues) SwingUtilities.invokeLater(awt) ;
 	            sleep(1000) ;
	         }
	         catch (InterruptedException e) { break ; }
         }
      }
   } ;


	// Constructor

	public ThreadDialog(JFrame frame, Configuration c)
	{
 		super(frame,Kisekae.getCaptions().getString("ThreadDialogTitle"),false);
      config = c ;
      me = this ;

      // Initialize the user interface.

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

 		center(this) ;

      // Set the dialog values.

      thread.start() ;
      setValues() ;

      // Register the event handlers.

		OK.addActionListener(this);
		SUSPEND.addActionListener(this);
		VIEW.addActionListener(this);
      LIST.addListSelectionListener(listListener) ;
		TREE.addMouseListener(treeBreakpoint) ;
		TREE.addMouseListener(mouseTreeListener) ;
      TREE.addTreeSelectionListener(treeListener) ;
 		addWindowListener(this);
	}

   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		panel1.setPreferredSize(new Dimension(400,300));
		jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel2.setLayout(borderLayout2);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jLabel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
      jPanel3.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
      gridLayout1.setRows(2);
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
		SUSPEND.setEnabled(false);
		SUSPEND.setToolTipText(Kisekae.getCaptions().getString("ToolTipSuspend"));
		SUSPEND.setText(Kisekae.getCaptions().getString("SuspendMessage"));
		VIEW.setEnabled(false);
      VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewQueue"));
		VIEW.setText(Kisekae.getCaptions().getString("ViewQueueMessage"));
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
      TREE.setShowsRootHandles(true);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.SOUTH);
      jPanel1.add(Box.createGlue()) ;
		jPanel1.add(SUSPEND, null);
      jPanel1.add(Box.createGlue()) ;
		jPanel1.add(VIEW, null);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(OK, null);
      jPanel1.add(Box.createGlue()) ;
		panel1.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jScrollPane1, BorderLayout.CENTER);
      panel1.add(jPanel3, BorderLayout.NORTH);
      jPanel3.add(jLabel2, null);
      jPanel3.add(jLabel3, null);
		jScrollPane1.getViewport().add(LIST, null);

  		// Set the default button for an enter key.

  		JRootPane rootpane = getRootPane()  ;
  		rootpane.setDefaultButton(OK) ;
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

		// An OK closes the frame

		if (source == OK)
      {
			close() ;
         return ;
      }

      // A Suspend temporarily stops the selected thread.
      // This is a manual suspension.  Threads remain suspended
      // until they are manually resumed.

      if (command.equals(Kisekae.getCaptions().getString("SuspendMessage")))
      {
         if (item == null) return ;
         if (item.startsWith("AlarmTimer") && config != null)
         {
         	AlarmTimer timer = config.getTimer() ;
            if (timer != null) timer.suspendTimer(true) ;
         }
         else if (item.startsWith("GifTimer") && config != null)
         {
         	GifTimer animator = config.getAnimator() ;
            if (animator != null) animator.suspendTimer(true) ;
         }
         else if (item.startsWith("SceneTimer") && config != null)
         {
         	SceneTimer scenetimer = config.getSceneTimer() ;
            if (scenetimer != null) scenetimer.suspendTimer(true) ;
         }
         else if (item.startsWith("AudioTimer") && config != null)
         {
         	AudioTimer closetimer = config.getAudioTimer() ;
            if (closetimer != null) closetimer.suspendTimer(true) ;
         }
         else if (item.startsWith("EventHandler"))
         {
            EventHandler.suspendEventHandler(true) ;
         }
     		SUSPEND.setText(Kisekae.getCaptions().getString("ResumeMessage")) ;
         return ;
      }

      // A Resume restarts a suspended thread.  This is a manual resume.

      if (command.equals(Kisekae.getCaptions().getString("ResumeMessage")))
      {
         if (item == null) return ;
         if (item.startsWith("AlarmTimer") && config != null)
         {
 	        	AlarmTimer timer = config.getTimer() ;
            if (timer != null) timer.resumeTimer(true) ;
         }
         else if (item.startsWith("GifTimer") && config != null)
         {
         	GifTimer animator = config.getAnimator() ;
            if (animator != null) animator.resumeTimer(true) ;
         }
         else if (item.startsWith("SceneTimer") && config != null)
         {
         	SceneTimer scenetimer = config.getSceneTimer() ;
            if (scenetimer != null) scenetimer.resumeTimer(true) ;
         }
         else if (item.startsWith("AudioTimer") && config != null)
         {
         	AudioTimer closetimer = config.getAudioTimer() ;
            if (closetimer != null) closetimer.resumeTimer(true) ;
         }
         else if (item.startsWith("EventHandler"))
         {
            EventHandler.resumeEventHandler(true) ;
         }
     		SUSPEND.setText(Kisekae.getCaptions().getString("SuspendMessage")) ;
         return ;
      }

      // A List Queues displays the queue list.

      if (command.equals(Kisekae.getCaptions().getString("ListQueuesMessage")))
      {
      	setValues() ;
         SUSPEND.setText(Kisekae.getCaptions().getString("ResumeMessage")) ;
         VIEW.setText(Kisekae.getCaptions().getString("ViewQueueMessage")) ;
         VIEW.setEnabled(true) ;
         pollqueues = true ;
      }

      // A View Queue displays the suspended queue contents.

      if (command.equals(Kisekae.getCaptions().getString("ViewQueueMessage")))
      {
      	pollqueues = false ;
      	showQueueContents() ;
         SUSPEND.setText(Kisekae.getCaptions().getString("ListQueuesMessage")) ;
         VIEW.setText(Kisekae.getCaptions().getString("ViewObjectMessage")) ;
         VIEW.setEnabled(false) ;
      }

      // A View Event or View Action displays the event dialog.

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

      // A View Cel displays the cel dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewCelMessage")))
      {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
			if (node == null) return;

         // Invoke the appropriate dialog.

			Object o = node.getUserObject();
         if (o instanceof Cel)
         {
				CelDialog cd = new CelDialog(me,(Cel) o,null,config) ;
				cd.show() ;
         }
         return ;
		}

      // A View Alarm displays the alarm dialog.

      if (command.equals(Kisekae.getCaptions().getString("ViewAlarmMessage")))
      {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				TREE.getLastSelectedPathComponent();
			if (node == null) return;

         // Invoke the appropriate dialog.

			Object o = node.getUserObject();
         if (o instanceof Alarm)
         {
           	Alarm a = (Alarm) o ;
	         Vector events = a.getEvent("alarm") ;
	         if (events == null || events.size() == 0) return ;
				EventDialog ed = new EventDialog(me,events.elementAt(0),config) ;
				ed.show() ;
         }
         return ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { OK.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


   // Function to set the dialog values.

   void setValues()
   {
      String s1 = Kisekae.getCaptions().getString("TimerPeriodMsgText") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + OptionsDialog.getTimerPeriod() + s1.substring(j1+1) ;
      jLabel2.setText(s1) ;
      s1 = Kisekae.getCaptions().getString("AnimationPeriodMsgText") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + OptionsDialog.getGifPeriod() + s1.substring(j1+1) ;
      jLabel3.setText(s1) ;

      // Set the user interface state where necessary.

      if (SUSPEND.getText().equals(Kisekae.getCaptions().getString("ListQueuesMessage")))
      {
      	pollqueues = false ;
         showQueueContents() ;
      }
		else
      {
      	pollqueues = true ;
      	showQueues() ;
      }
   }


   // A function to construct the queue list entries.

   private void showQueues()
   {
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
		jScrollPane1.getViewport().add(LIST, null);
      listmodel.removeAllElements();
      int len = 50 ;
      format("clear","",0,len) ;
      format("left",Kisekae.getCaptions().getString("ThreadNameText"),0,19) ;
      format("left",Kisekae.getCaptions().getString("ThreadStateText"),20,10) ;
      format("center",Kisekae.getCaptions().getString("ThreadQueuedText"),31,8) ;
      format("center",Kisekae.getCaptions().getString("ThreadProcessedText"),40,9) ;
      String s = format(" ","",0,len) ;
      JLabel columns = new JLabel(s) ;
      columns.setFont(LIST.getFont());
		jScrollPane1.setColumnHeaderView(columns);

		// Populate the display list with the AlarmTimer thread statistics.

      if (config != null)
      {
      	GifTimer animator = config.getAnimator() ;
         if (animator != null)
         {
	         String queuesize = "" + animator.getQueueSize() ;
            String fired = "" + animator.getCount() ;
				format("clear","",0,len) ;
				format("left",animator.getName(),0,19) ;
				format("left",animator.getTimerState(),20,10) ;
				format("right",queuesize,31,5) ;
            format("right",fired,40,9) ;
				s = format(" ","",0,len) ;
				listmodel.addElement(s) ;
         }

         // Do the same for the scene memory timer.

      	SceneTimer scenetimer = config.getSceneTimer() ;
         if (scenetimer != null)
         {
	         String queuesize = "" + scenetimer.getQueueSize() ;
            String fired = "" + scenetimer.getCount() ;
				format("clear","",0,len) ;
				format("left",scenetimer.getName(),0,19) ;
				format("left",scenetimer.getTimerState(),20,10) ;
				format("right",queuesize,31,5) ;
            format("right",fired,40,9) ;
				s = format(" ","",0,len) ;
				listmodel.addElement(s) ;
         }

         // Do the same for the scene memory timer.

      	AudioTimer closetimer = config.getAudioTimer() ;
         if (closetimer != null)
         {
	         String queuesize = "" + closetimer.getQueueSize() ;
            String fired = "" + closetimer.getCount() ;
				format("clear","",0,len) ;
				format("left",closetimer.getName(),0,19) ;
				format("left",closetimer.getTimerState(),20,10) ;
				format("right",queuesize,31,5) ;
            format("right",fired,40,9) ;
				s = format(" ","",0,len) ;
				listmodel.addElement(s) ;
         }

         // Do the same for the alarm timer.

      	AlarmTimer timer = config.getTimer() ;
         if (timer != null)
         {
	         String queuesize = "" + timer.getQueueSize() ;
            String fired = "" + timer.getCount() ;
				format("clear","",0,len) ;
				format("left",timer.getName(),0,19) ;
				format("left",timer.getAlarmState(),20,10) ;
				format("right",queuesize,31,5) ;
            format("right",fired,40,9) ;
				s = format(" ","",0,len) ;
				listmodel.addElement(s) ;
         }
		}

		// Populate the display list with the EventHandler thread statistics.

      Vector handlers = EventHandler.getThreads() ;
      if (handlers != null)
      {
      	for (int i = 0 ; i < handlers.size() ; i++)
         {
         	EventHandler handler = (EventHandler) handlers.elementAt(i) ;
	         String queuesize = "" + handler.getQueueSize() ;
            String fired = "" + handler.getCount() ;
				format("clear","",0,len) ;
				format("left",handler.getName(),0,19) ;
				format("left",handler.getState(),20,10) ;
				format("right",queuesize,31,5) ;
            format("right",fired,40,9) ;
				s = format(" ","",0,len) ;
				listmodel.addElement(s) ;
         }
      }

      // Set the list selection entry.

		if (item != null && itemindex >= 0)
			LIST.setSelectedIndex(itemindex) ;
		else
		{
			LIST.clearSelection() ;
			item = null ;
			itemindex = -1 ;
		}
   }


   // A function to construct the queue contents entries.

   private void showQueueContents()
   {
   	if (item == null) return ;

		// Obtain an enumeration of the queue contents.

      Enumeration e = null ;
      if (config != null)
      {
	      if (item.startsWith("AlarmTimer"))
	       	e = config.getTimer().getQueue() ;
	      else if (item.startsWith("GifTimer"))
	       	e = config.getAnimator().getQueue() ;
      }
      if (item.startsWith("EventHandler"))
         e = EventHandler.getQueue() ;
      if (e == null) return ;

		// Populate the display list with the queue contents.

      Vector sorted = new Vector() ;
      while (e.hasMoreElements())
      {
      	Object o = e.nextElement() ;
         if (o instanceof KissObject)
         	sorted.addElement(o);
         else if (o instanceof Object [])
         	sorted.addElement(((Object []) o)[0]);
      }

      // Sort the event list.

      Collections.sort(sorted) ;
      e = sorted.elements() ;

      // Construct the tree view.

      jLabel2.setText("") ;
      jLabel3.setText(Kisekae.getCaptions().getString("QueuedObjectsText")) ;
      int i = item.indexOf(' ') ;
      top.setUserObject((i < 0) ? item : item.substring(0,i)) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
		jScrollPane1.getViewport().add(TREE, null);
		jScrollPane1.setColumnHeaderView(null);
      VIEW.setEnabled(false) ;

 		// Populate the tree with all queued objects.

		while (e.hasMoreElements())
		{
        	Object o = e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(o) ;
         if (o instanceof FKissEvent)
         {
         	FKissEvent evt = (FKissEvent) o ;
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
         else
         	top.add(eventnode) ;
 		}

      // Expand the tree.

      TREE.expandRow(0);
	}

   // We overload the KissDialog close method to release our local references.

   void close()
   {
   	thread.interrupt() ;
   	flush() ;
      super.close() ;
   }

   // We release references to some of our critical objects.

   private void flush()
   {
      me = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		SUSPEND.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
      LIST.removeListSelectionListener(listListener) ;
		TREE.removeMouseListener(treeBreakpoint) ;
		TREE.removeMouseListener(mouseTreeListener) ;
      TREE.removeTreeSelectionListener(treeListener) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;

      top = null ;
      TREE = null ;
   }
}
