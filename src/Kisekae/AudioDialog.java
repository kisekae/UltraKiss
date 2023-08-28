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
* AudioDialog class
*
* Purpose:
*
* This class defines an instance of the Kisekae audio dialog.  This dialog
* shows all attributes of an Audio object.  It is an instance of a KissDialog.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.* ;
import javax.media.* ;
import javax.media.protocol.* ;
import javax.sound.sampled.* ;
import javax.sound.midi.* ;



final class AudioDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;								// Reference to ourselves
   private Configuration config = null ;  		 		// The current config id
	private Audio audio = null ;    			  			 	// The audio object
   private Timer timer = null ;                       // The progress timer
   private AudioListener alisten = null ;             // Audio listener assigned
   private ClipListener clisten = null ;              // Clip listener assigned
   private SequencerListener slisten = null ;         // Sequencer listener assigned
   private boolean played = false ;                   // True if play pressed

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
	private JButton PLAYBTN = new JButton();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
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
	private JLabel offsetlabel = new JLabel();
	private JLabel sizelabel = new JLabel();
	private JLabel statelabel = new JLabel();
	private JLabel eventslabel = new JLabel();
	private JLabel namelabel = new JLabel();
	private JLabel imagelabel = new JLabel();
	private JCheckBox realizedlabel = new JCheckBox();
 	private JCheckBox repeatlabel = new JCheckBox();
	private JCheckBox copylabel = new JCheckBox();
	private JCheckBox loadedlabel = new JCheckBox();
   private JProgressBar progress = new JProgressBar() ;

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (copylabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return copylabel.getBackground() ; }
   };

	// Register for events.

	MouseListener mouseTreeListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
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
			VIEW.setEnabled(true);
		}
	} ;

   
	// Create an action listener for timer scheduling.  This updates the
   // progress bar for active media playback.
   
   ActionListener timerTask = new ActionListener()
   {
      public void actionPerformed(ActionEvent evt)
      {
   		if (!(audio instanceof AudioSound)) return ;
         AudioSound sound = (AudioSound) audio ;
         Object player = sound.getPlayer() ;
         if (player instanceof Clip)
         {
            Clip clip = (Clip) player ;
            int framelength = clip.getFrameLength() ;
            int position = clip.getFramePosition() ;
            if (framelength <= 0) return ;
            int percent = (int) ((position * 1000) / framelength) ;
            progress.setValue(percent) ;
         }
         if (player instanceof Sequencer)
         {
            Sequencer clip = (Sequencer) player ;
            long framelength = clip.getMicrosecondLength() ;
            long position = clip.getMicrosecondPosition() ;
            if (framelength <= 0) return ;
            int percent = (int) ((position * 1000) / framelength) ;
            progress.setValue(percent) ;
         }
         
         // Update state values
         
         String state = audio.getState() ;
         String playtime = "" + audio.getPosition() ;
         String mediatime = "" + audio.getDuration() ;
         String latenttime = "" + audio.getLatency() ;
         boolean playeractive = Kisekae.getCaptions().getString("MediaStartedState").equals(state) ;
   		String s1 = Kisekae.getCaptions().getString("PositionText") + " " + playtime ;
         String s2 = Kisekae.getCaptions().getString("LatencyText") + " " + latenttime ;
         String s3 = Kisekae.getCaptions().getString("DurationText") + " " + mediatime ;
         String s4 = Kisekae.getCaptions().getString("PlayerText") + " " + state ;
         locationlabel.setText(s1) ;
         offsetlabel.setText(s2) ;
         sizelabel.setText(s3) ;
         statelabel.setText(s4) ;
       }
   } ;


	// Constructor

   public AudioDialog(JDialog f, Audio a, Configuration c)
   { super(f,null,false) ; init(a,c) ; }

	public AudioDialog(JFrame f, Audio a, Configuration c)
	{ super(f,null,false) ; init(a,c) ; }

   private void init(Audio a, Configuration c)
   {
		me = this ;
		audio = a ;
		config = c ;
      String title = Kisekae.getCaptions().getString("AudioDialogTitle") ;
      if (a.getIdentifier() != null) title += " " + a.getIdentifier() ;
      if (a.getName() != null) title += " " + a.getName() ;
      if (a.isInternal()) title += " [" + Kisekae.getCaptions().getString("InternalStateText") + "]" ;
      if (a.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
      setTitle(title) ;

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
      
      // Set the current media values.  Activate the progress update if active.
      
		setValues() ;
      if (Kisekae.getCaptions().getString("MediaStartedState").equals(audio.getState()))
      {
         if (timer != null) timer.stop() ;
         timer = new Timer(300,timerTask) ;
         timer.start() ;
      }

		// Add the control panel if our media player is realized.

		if (audio instanceof AudioMedia)
		{
			if (((AudioMedia) audio).isRealized())
			{
				jPanel8.removeAll() ;
				Component comp = ((AudioMedia) audio).getControlPanelComponent() ;
            if (comp != null)
					jPanel8.add(comp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
						,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0)) ;
			}
		}

		// Center the frame in the panel space.

		doLayout() ;
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
		PLAYBTN.addActionListener(this) ;
		TREE.addMouseListener(mouseTreeListener) ;
      TREE.addTreeSelectionListener(treeListener);
      realizedlabel.addActionListener(this) ;
      copylabel.addActionListener(this) ;
      repeatlabel.addActionListener(this) ;
      loadedlabel.addActionListener(this) ;
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
      LISTBTN.setText(Kisekae.getCaptions().getString("ListEventsMessage"));
      LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListEventsButton"));
		PLAYBTN.setText(Kisekae.getCaptions().getString("MediaPlayMessage"));
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TREE = new noExpandTree(top);
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TREE.setCellRenderer(new FKissCellRenderer());
		TREE.setShowsRootHandles(true);

		Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("MediaBoxText")),eb2) ;
		Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
      Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
		Border cb5 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("PlayerBoxText")),eb2) ;
		gridLayout2.setColumns(1);
		gridLayout2.setRows(3);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(3);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(4);

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
		jPanel5.setLayout(gridLayout3);
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

      heading.setText(" ");
		heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		locationlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPosition"));
		locationlabel.setText(Kisekae.getCaptions().getString("PositionText"));
		offsetlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipLatency"));
		offsetlabel.setText(Kisekae.getCaptions().getString("LatencyText"));
		sizelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipDuration"));
		sizelabel.setText(Kisekae.getCaptions().getString("DurationText"));
		copylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopyState"));
		copylabel.setText(Kisekae.getCaptions().getString("CopyStateText"));
      copylabel.setEnabled(false);
      copylabel.setBackground(nonFading);
		realizedlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipRealizedState"));
		realizedlabel.setText(Kisekae.getCaptions().getString("RealizedStateText"));
      realizedlabel.setEnabled(false);
      realizedlabel.setBackground(nonFading);
		repeatlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipRepeatState"));
		repeatlabel.setText(Kisekae.getCaptions().getString("RepeatStateText"));
      repeatlabel.setBackground(nonFading);
		eventslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventNumber"));
		eventslabel.setText(Kisekae.getCaptions().getString("EventCountText"));
		namelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipName"));
		namelabel.setText(Kisekae.getCaptions().getString("NameText"));
		statelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayer"));
		statelabel.setText(Kisekae.getCaptions().getString("PlayerText"));
		loadedlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipLoadedState"));
		loadedlabel.setText(Kisekae.getCaptions().getString("LoadedStateText"));
      loadedlabel.setEnabled(false);
      loadedlabel.setBackground(nonFading);
		imagelabel.setHorizontalAlignment(SwingConstants.CENTER);
		imagelabel.setHorizontalTextPosition(SwingConstants.CENTER);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(sizelabel, null);
		jPanel4.add(locationlabel, null);
		jPanel4.add(offsetlabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel5.add(eventslabel, null);
		jPanel5.add(statelabel, null);
		jPanel5.add(namelabel, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel6.add(loadedlabel, null);
		jPanel6.add(realizedlabel, null);
		jPanel6.add(repeatlabel, null);
		jPanel6.add(copylabel, null);
		jPanel1.add(jPanel7, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel7.add(imagelabel, BorderLayout.CENTER);
		jPanel1.add(jPanel8, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel8.add(PLAYBTN, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
		jPanel8.add(progress, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 10), 0, 0));
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

      // The user cannot change state variable values.

      if (source instanceof JCheckBox)
      {
			JCheckBox cb = (JCheckBox) source ;
         if (source == repeatlabel)
         {
         	int n = (cb.isSelected()) ? -1 : 0 ;
         	if (audio != null) audio.setRepeat(n) ;
            return ;
         }
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
   			if (audio != null && played) audio.stop(audio) ;
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

		// A Play request starts the media file.

      if (command.equals(Kisekae.getCaptions().getString("MediaPlayMessage")))
		{
      	PLAYBTN.setText(Kisekae.getCaptions().getString("MediaStopMessage")) ;
			if (audio == null) return ;
         played = true ;
         audio.open() ;
 
         // The open request may fail.  For audio sound files we can
         // try and recover using Java Media Framework.  The new AudioMedia
         // object is a load copy of the original object and it replaces the
         // original AudioSound object.

         if (audio.isError() && audio instanceof AudioSound && Kisekae.isMediaInstalled())
         {
            Object cid = (config != null) ? config.getID() : null ;
            Object o = audio.getByKey(audio.getKeyTable(),cid,audio.getPath().toUpperCase()) ;

            // Have we replaced this object previously?

            if (o == audio)
            {
               KissObject kiss = new AudioMedia(audio.getZipFile(),audio.getPath()) ;
               Audio a1 = (Audio) kiss ;
               a1.setIdentifier(audio.getIdentifier()) ;
               a1.setRelativeName(audio.getRelativeName()) ;
               a1.setRepeat(audio.getRepeat()) ;
               a1.setType(audio.getType()) ;
               a1.setID(audio.getID()) ;
               a1.load() ;
               audio.removeKey(audio.getKeyTable(),cid,audio.getPath().toUpperCase()) ;
               a1.setKey(a1.getKeyTable(),cid,a1.getPath().toUpperCase()) ;
               Enumeration enum1 = audio.getEvents() ;
               while (enum1 != null && enum1.hasMoreElements())
                  a1.addEvent((Vector) enum1.nextElement()) ;
               audio.close() ;
               Vector sounds = (config != null) ? config.getSounds() : null ;
               int n = (sounds != null) ? sounds.indexOf(audio) : -1 ;
               if (n >= 0) sounds.setElementAt(a1,n) ;
               audio = a1 ;
               audio.init() ;
            }
            else if (o instanceof Audio)
            {
               audio = (Audio) o ;
               audio.init() ;
            }
         }

         // Listen for events on our java sound player.

   		if (audio instanceof AudioSound)
   		{
   			if (((AudioSound) audio).isSequencer())
            {
   				audio.removeListener(slisten) ;
               slisten = new SequencerListener() ;
   				audio.addListener(slisten) ;
            }
   			if (((AudioSound) audio).isClip())
            {
               audio.removeListener(clisten) ;
               clisten = new ClipListener() ;
   				audio.addListener(clisten) ;
            }
         }
   		if (audio instanceof AudioMedia)
   		{
   			audio.removeListener(alisten) ;
            alisten = new AudioListener() ;
   			audio.addListener(alisten) ;
         }

         // Play audio.

         audio.play() ;
         setValues() ;

         // Activate the progress bar.

         progress.setMinimum(0) ;
         progress.setMaximum(1000) ;
         progress.setValue(0) ;
         if (timer != null) timer.stop() ;
         timer = new Timer(300,timerTask) ;
         timer.start() ;
			return ;
		}

		// A Stop request stops the media file.

      if (command.equals(Kisekae.getCaptions().getString("MediaStopMessage")))
		{
      	PLAYBTN.setText(Kisekae.getCaptions().getString("MediaPlayMessage")) ;
			if (audio != null) 
         {
         	audio.setRepeat(0) ;
            audio.stop(audio) ;
         }
      
         // Terminate any progress timing.
      
         if (timer != null) timer.stop() ;
         timer = null ;
			return ;
		}

		// A List Events updates the tree to display the group events.

      if (command.equals(Kisekae.getCaptions().getString("ListEventsMessage")))
		{
         VIEW.setEnabled(false) ;
         VIEW.setVisible(true) ;
         LISTBTN.setText(Kisekae.getCaptions().getString("ListTextMessage")) ;
         LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListTextButton"));
         setValues() ;
			return ;
		}

		// A List Audio updates the list table to display the audio values.

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
      if (!javax.swing.SwingUtilities.isEventDispatchThread())
      {
      	Runnable runner = new Runnable()
			{ public void run() { setValues() ; } } ;
   		javax.swing.SwingUtilities.invokeLater(runner) ;
      }

   	if (audio == null) return ;
		String state = audio.getState() ;
		String playtime = "" + audio.getPosition() ;
		String mediatime = "" + audio.getDuration() ;
		String latenttime = "" + audio.getLatency() ;
		boolean playeractive = Kisekae.getCaptions().getString("MediaStartedState").equals(state) ;
		int events = audio.getEventCount() ;

		// Create the text entries.

		String s1 = Kisekae.getCaptions().getString("PositionText") + " " + playtime ;
		String s2 = Kisekae.getCaptions().getString("LatencyText") + " " + latenttime ;
		String s3 = Kisekae.getCaptions().getString("DurationText") + " " + mediatime ;
		String s4 = Kisekae.getCaptions().getString("PlayerText") + " " + state ;
		String s5 = Kisekae.getCaptions().getString("EventCountText") + " " + events ;
		String s6 = Kisekae.getCaptions().getString("NameText") + " " + audio.getPath() ;
		locationlabel.setText(s1) ;
		offsetlabel.setText(s2) ;
		sizelabel.setText(s3) ;
		statelabel.setText(s4) ;
		eventslabel.setText(s5) ;
		namelabel.setText(s6) ;
      loadedlabel.setSelected(audio.isLoaded()) ;
      realizedlabel.setSelected(audio.isRealized()) ;
      repeatlabel.setSelected(audio.isRepeating()) ;
      copylabel.setSelected(audio.isCopy()) ;
		imagelabel.setIcon(Kisekae.getImageIcon()) ;
      progress.setMinimum(0) ;
      progress.setMaximum(1000) ;
      progress.setValue(0) ;

      // Examine the error state.

      if (audio.isError())
      {
         loadedlabel.setText(Kisekae.getCaptions().getString("ErrorStateText")) ;
         loadedlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipErrorState"));
         loadedlabel.setSelected(true) ;
      }

      // Set the user interface state where necessary.

      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListEventsMessage")))
      {
      	VIEW.setVisible(false) ;
         VIEW.setText(Kisekae.getCaptions().getString("ViewEventMessage"));
      	LISTBTN.setEnabled(events > 0) ;
         showAudio() ;
      }
      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListTextMessage")))
      {
      	LISTBTN.setEnabled(true) ;
         showEvents() ;
		}
      if (playeractive) PLAYBTN.setText(Kisekae.getCaptions().getString("MediaStopMessage")) ;
      else PLAYBTN.setText(Kisekae.getCaptions().getString("MediaPlayMessage")) ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
      repaint() ;
	}


   // A function to construct the cel text entries.

   private void showAudio()
   {
   	if (audio == null) return ;
      heading.setText(Kisekae.getCaptions().getString("CharacteristicsHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
		jScrollPane1.getViewport().add(LIST, null);
      listmodel.removeAllElements();
		String fn = Kisekae.getCaptions().getString("UnknownValueText") ;
		Object o = audio.getZipFile() ;
      if (o instanceof PkzFile) fn = ((PkzFile) o).getName() ;
      if (o instanceof LhaFile) fn = ((LhaFile) o).getName() ;
      if (o instanceof DirFile) fn = ((DirFile) o).getName() ;
      if (fn == null) fn = Kisekae.getCaptions().getString("UnknownValueText") ;
      String fntype = Kisekae.getCaptions().getString("ArchiveText") ;
      if (o instanceof DirFile) fntype = Kisekae.getCaptions().getString("DirectoryText") ;
		String content = audio.getContentType() ;
      String copyright = audio.getCopyright() ;
      if (copyright == null) copyright = Kisekae.getCaptions().getString("UnknownValueText") ;
      Object contentdesc = audio.getContentDescriptor() ;
      if (contentdesc == null) contentdesc = Kisekae.getCaptions().getString("UnknownValueText") ;
      String audiotype = audio.getType() ;
      if (audiotype == null) audiotype = Kisekae.getCaptions().getString("UnknownValueText") ;
		listmodel.addElement(Kisekae.getCaptions().getString("NameText") + " " + audio.getName()) ;
		listmodel.addElement(fntype + " " + fn) ;
		listmodel.addElement(Kisekae.getCaptions().getString("BytesText") + " " + audio.getBytes()) ;
      listmodel.addElement(Kisekae.getCaptions().getString("ContentText") + " " + content) ;
      listmodel.addElement(Kisekae.getCaptions().getString("AudioTypeText") + " " + audiotype) ;
      listmodel.addElement(Kisekae.getCaptions().getString("CopyrightText") + " " + copyright) ;
      listmodel.addElement(Kisekae.getCaptions().getString("ContentDescriptorText") + " " + contentdesc.toString()) ;
	}


   // A function to construct the event list entries.

   private void showEvents()
   {
   	if (audio == null) return ;
      heading.setText(Kisekae.getCaptions().getString("EventListHeadingText")) ;
   	Component c = jScrollPane1.getViewport().getView() ;
      if (c != null) jScrollPane1.getViewport().remove(c) ;
      top.removeAllChildren() ;
      TreeModel treemodel = TREE.getModel() ;
      ((DefaultTreeModel) treemodel).reload() ;
      top.setUserObject(Kisekae.getCaptions().getString("EventsNode")) ;
		jScrollPane1.getViewport().add(TREE, null);

		// Populate the display list with the audio event vector contents.

      Vector sorted = new Vector() ;
      Enumeration e = audio.getEvents() ;
      appendevents(sorted,e) ;

      // Sort the event list.

      Collections.sort(sorted) ;
      e = sorted.elements() ;

 		// Populate the tree with all defined events.

		while (e.hasMoreElements())
		{
        	FKissEvent event = (FKissEvent) e.nextElement() ;
         DefaultMutableTreeNode eventnode = new DefaultMutableTreeNode(event) ;
         Enumeration enum1 = event.getActions() ;
         if (enum1 == null) continue ;
			top.add(eventnode) ;

         // Build the event action hierarchy

         while (enum1.hasMoreElements())
         {
          	FKissAction action = (FKissAction) enum1.nextElement() ;
            DefaultMutableTreeNode actionnode = new DefaultMutableTreeNode(action) ;
				eventnode.add(actionnode) ;
         }
		}

      // Expand the tree.

      TREE.expandRow(0);
	}


   // We overload the KissDialog close method.

   void close()
   {
		if (audio != null && played) audio.stop(audio) ;
      if (timer != null) timer.stop() ;
      timer = null ;
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
      audio = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		LISTBTN.removeActionListener(this) ;
		PLAYBTN.removeActionListener(this) ;
		TREE.removeMouseListener(mouseTreeListener) ;
      TREE.removeTreeSelectionListener(treeListener);
      realizedlabel.removeActionListener(this) ;
      copylabel.removeActionListener(this) ;
      repeatlabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
		top = null ;
      TREE = null ;
   }


	// Inner class to catch Controller events.

	class AudioListener implements ControllerListener
	{
		public void controllerUpdate(ControllerEvent ce)
		{
			setValues() ;
			if (ce instanceof RealizeCompleteEvent)
			{
				jPanel8.removeAll() ;
				Object player = (audio != null) ? audio.getPlayer() : null ;
				if (player == null) return ;
				Component comp = ((Player) player).getControlPanelComponent() ;
            if (comp != null)
					jPanel8.add(comp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
						,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0)) ;
				me.addNotify() ;
				me.validate() ;
				me.repaint() ;
			}
			else if (ce instanceof EndOfMediaEvent)
			{
				if (audio != null && !audio.isRepeating() && OptionsDialog.getAutoMediaLoop())
            	audio.play() ;
			}
		}
	}

	// Inner class to catch sound clip events.

	class ClipListener implements LineListener
	{
		public void update(LineEvent event)
		{
			if (event.getType() == LineEvent.Type.STOP)
			{
				if (audio != null)
            {
               if (audio.isRepeating() || (audio.getBackground() && OptionsDialog.getAutoMediaLoop()))
               	audio.play() ;
            }
				else setValues() ;
			}

			else if (event.getType() == LineEvent.Type.START)
			{
				setValues() ;
			}
		}
	}

	// Inner class to catch sound sequencer events.

	class SequencerListener implements MetaEventListener
	{
		public void meta(MetaMessage message)
		{
			if (message.getType() == 47)
			{  // 47 is end of track
				if (audio != null)
            {
               if (audio.isRepeating() || (audio.getBackground() && OptionsDialog.getAutoMediaLoop()))
               	audio.play() ;
            }
				else setValues() ;
			}
		}
	}
}
