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
* MediaFrame class
*
* Purpose:
*
* This class defines a media player window frame used to play audio or video
* media files that are loaded as independent files.  The frame contains the
* visual component and the control component.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.StringTokenizer ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;
import javax.swing.SwingUtilities ;
import javax.media.* ;
import javax.media.protocol.* ;

import java.net.URL ;
import javax.sound.sampled.* ;
import javax.sound.midi.* ;



final class MediaFrame extends KissFrame
	implements WindowListener, ActionListener, ItemListener, MenuListener
{
   private static MediaFrame unique = null ;       // Reference to unique player
   
	// Dialog attributes

   private MediaFrame me = null ;						// Reference to ourselves
   private MediaLoad loader = null ;               // Reference to our loader
	private KissObject ko = null ;						// The abstract media
   private ArchiveEntry playze = null ;				// The playlist entry
	private Audio audio = null ;    			  	  	 	// The audio object
	private Video video = null ;					  		// The video object
	private Object currentmedia = null ;				// The media player object
   private Object audiolistener = null ;           // The audio listener for object
   private Object medialistener = null ;           // The media listener for object
	private FileOpen fd = null ;							// The fileopen object
	private Component visual = null ;					// The visual component
	private Component control = null ;					// The control component
	private Component status = null ;					// The label component
   private Vector playlist = null ;						// The playlist entries
   private String playlistname = null ;            // The playlist name
   private Timer timer = null ;                    // The progress timer
   private float volumeadjust = 1.0f ;             // The volume adjustment
   private int playindex = 0 ;							// The current play index
	private int repeatcount = 0 ;				         // The repetition count
   private boolean internal = false ;              // True if created by action

   private boolean realized = false ;					// If true, media realized
	private boolean error = false ;						// If true, media error
	private boolean updated = false ;					// If true, playlist updated

	// HelpSet interface objects.

	private static String helpset = "Help/MediaPlayer.hs" ;
	private static String helpsection = "mediaplayer.index" ;
	private static String onlinehelp = "mediaplayer/index.html" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;

	// User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel panel3 = new JPanel();
	private JPanel panel4 = new JPanel();
	private JPanel panel5 = new JPanel();
	private JLabel statelabel = new JLabel();
	private JLabel imagelabel = new JLabel();
	private JLabel copyrightlabel = new JLabel();
   private JList list1 = new JList() ;
   private JButton play = new JButton() ;
   private JButton reset = new JButton() ;
   private JProgressBar progress = new JProgressBar() ;
   private JScrollPane scrollpane = new JScrollPane() ;
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout5 = new BorderLayout();
   private Border eb1 = BorderFactory.createEmptyBorder(10,0,10,0) ;
   private Border eb2 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb3 = BorderFactory.createEmptyBorder(10,0,10,0) ;
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

	// Menu items

	private JMenu fileMenu ;
	private JMenu optionMenu ;
	private JMenu windowMenu ;
	private JMenu helpMenu ;
	private JMenuItem open ;
	private JMenuItem select ;
	private JMenuItem queue ;
	private JMenuItem delete ;
	private JMenuItem save ;
	private JMenuItem saveas ;
	private JMenuItem close ;
	private JMenuItem exit ;
	private JMenuItem help ;
	private JMenuItem logfile ;
	private JMenuItem about ;
	private JCheckBoxMenuItem loopcontrol ;
	private JCheckBoxMenuItem minimize ;
	private JCheckBoxMenuItem fullscreen ;
	private JCheckBoxMenuItem centerframe ;
	private JCheckBoxMenuItem aspectratio ;
   private JCheckBoxMenuItem volume ;
   private Insets insets = null ;


   // Listeners

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
			if (list1 == null) return ;
         playindex = list1.getSelectedIndex() ;
			playNext() ;
		}
	} ;

   
	// Create an action listener for timer scheduling.  This updates the
   // progress bar for active media playback.
   
   ActionListener timerTask = new ActionListener()
   {
      public void actionPerformed(ActionEvent evt)
      {
         if (currentmedia == null) return ;
         if (currentmedia instanceof Clip)
         {
            Clip clip = (Clip) currentmedia ;
            int framelength = clip.getFrameLength() ;
            int position = clip.getFramePosition() ;
            if (framelength <= 0) return ;
            int percent = (int) ((position * 1000) / framelength) ;
            progress.setValue(percent) ;
            progress.setToolTipText(position + " : " + framelength) ;
         }
         if (currentmedia instanceof Sequencer)
         {
            Sequencer clip = (Sequencer) currentmedia ;
            long framelength = clip.getMicrosecondLength() ;
            long position = clip.getMicrosecondPosition() ;
            if (framelength <= 0) return ;
            int percent = (int) ((position * 1000) / framelength) ;
            progress.setValue(percent) ;
            progress.setToolTipText(position + " : " + framelength) ;
         }
      }
   } ;


	// Constructor

   public MediaFrame()
	{ me = this ; init() ; }

   public MediaFrame(Audio a)
   { me = this ; ko = a ; init() ; play() ; }

	public MediaFrame(Video v)
	{ me = this ; ko = v ; init() ; play() ; }

   public MediaFrame(ArchiveEntry ze)
	{ me = this ; init() ; play(ze) ; }
  
   // Constructor for a unique Media Player.  Attempts to create a 
   // new Media Player should first examine if a previous unique
   // player exists through a getUniquePlayer() call and reuse this 
   // object if it exists.  
   
   public MediaFrame(ArchiveEntry ze, Object source)
   {
		me = this ;
      if (source != null && unique == null) unique = me ;
		init() ;
      play(ze) ;
	}


   // Frame initialization.

	private void init()
	{
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setIconImage(Kisekae.getIconImage()) ;
		JPopupMenu.setDefaultLightWeightPopupEnabled(false) ;
      boolean applemac = OptionsDialog.getAppleMac() ;

		// Construct the user interface.

		try { jbInit() ; pack() ; }
		catch(Exception ex)
		{ ex.printStackTrace() ; }

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;

		// Set up the menu bar.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
		String jv = System.getProperty("java.version") ;
		int rm = (jv.indexOf("1.2") == 0) ? 2 : 26 ;
		insets = new Insets(2,2,2,rm) ;
		fileMenu.setMargin(insets) ;
		if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
		open.setMnemonic(KeyEvent.VK_O) ;
		open.addActionListener(this) ;
		fileMenu.add((select = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSelect")))) ;
		if (!applemac) select.setMnemonic(KeyEvent.VK_L) ;
		select.addActionListener(this) ;
		fileMenu.add((close = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClose")))) ;
		close.setMnemonic(KeyEvent.VK_C) ;
		close.addActionListener(this) ;
		fileMenu.add((save = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSave")))) ;
		if (!applemac) save.setMnemonic(KeyEvent.VK_S) ;
		save.setEnabled(false) ;
		save.addActionListener(this) ;
		fileMenu.add((saveas = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAs")))) ;
		if (!applemac) saveas.setMnemonic(KeyEvent.VK_A) ;
		saveas.addActionListener(this) ;
      saveas.setEnabled(!Kisekae.isSecure());
		fileMenu.addSeparator();
		fileMenu.add((queue = new JMenuItem(Kisekae.getCaptions().getString("MenuFileQueue")))) ;
		if (!applemac) queue.setMnemonic(KeyEvent.VK_Q) ;
		queue.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0));
		queue.addActionListener(this) ;
		fileMenu.add((delete = new JMenuItem(Kisekae.getCaptions().getString("MenuFileDelete")))) ;
      delete.setEnabled(false) ;
		if (!applemac) delete.setMnemonic(KeyEvent.VK_D) ;
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
		delete.addActionListener(this) ;
		fileMenu.addSeparator();
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString("MenuFileExit")))) ;
		if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;
      
		optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
		optionMenu.setMargin(insets) ;
		if (!applemac) optionMenu.setMnemonic(KeyEvent.VK_O) ;
      optionMenu.addMenuListener(this) ;
		optionMenu.add((centerframe = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsCenterFrame")))) ;
		centerframe.setState(OptionsDialog.getMediaCenter());
		centerframe.addItemListener(this) ;
		optionMenu.add((loopcontrol = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsLoopPlayback")))) ;
		loopcontrol.setState(OptionsDialog.getAutoMediaLoop());
		loopcontrol.addItemListener(this) ;
		optionMenu.add((minimize = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsMinimizeAudio")))) ;
		minimize.setState(OptionsDialog.getMediaMinimize());
		minimize.addItemListener(this) ;
		optionMenu.add((fullscreen = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsFullScreenVideo")))) ;
		fullscreen.setState(OptionsDialog.getAutoFullScreen());
		fullscreen.addItemListener(this) ;
		optionMenu.add((aspectratio = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsRetainAspectRatio")))) ;
		aspectratio.setState(OptionsDialog.getKeepAspect());
		aspectratio.addItemListener(this) ;
      optionMenu.add((volume = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("OptionsMediaVolume")))) ;
      volume.setState(OptionsDialog.getAdjustMediaVolume());
      volumeadjust = OptionsDialog.getMediaVolume() ;
      volume.addItemListener(this) ;
      mb.add(optionMenu) ;

		// Create the Help menu and About dialog.

      aboutdialog = new AboutBox(this,Kisekae.getCaptions().getString("AboutBoxTitle"),true) ;
		helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
		helpMenu.setMargin(insets) ;
		if (!applemac) helpMenu.setMnemonic(KeyEvent.VK_H) ;
		mb.add(helpMenu);
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
		about.addActionListener(this);
		setJMenuBar(mb) ;

		// Initially center the frame in the panel space.

      super.open() ;
      Dimension s = getPreferredSize() ;
      s.width = Math.max(s.width,400) ;
		setSize(s) ;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
		d = new Dimension((int) (d.width*1.00),(int) (d.height*0.95)) ;
      if (helper != null) helper.setSize(d);
		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
		setLocation(x,y) ;
		setValues() ;

      // Set listeners.

      play.addActionListener(this) ;
      reset.addActionListener(this) ;
		addWindowListener(this) ;
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1 = new JPanel() ;
		panel1.setLayout(borderLayout1) ;
		panel4 = new JPanel() ;
		panel4.setLayout(borderLayout4) ;
      panel4.setBorder(eb2) ;
		panel5.setLayout(borderLayout5) ;
      panel5.setBorder(eb2) ;
      statelabel.setBorder(BorderFactory.createEmptyBorder(10,0,10,0)) ;
      statelabel.setPreferredSize(new Dimension(100,25)) ;
      statelabel.setHorizontalAlignment(SwingConstants.CENTER) ;
   	statelabel.setText(Kisekae.getCaptions().getString("MediaInactiveState")) ;
      panel5.add(statelabel,BorderLayout.NORTH) ;
      panel5.add(progress,BorderLayout.SOUTH) ;
      panel1.add(panel5, BorderLayout.SOUTH) ;
		imagelabel.setHorizontalAlignment(SwingConstants.CENTER) ;
		imagelabel.setHorizontalTextPosition(SwingConstants.RIGHT) ;
		imagelabel.setIconTextGap(10) ;
      imagelabel.setBorder(eb1) ;
		imagelabel.setIcon(Kisekae.getImageIcon()) ;
      imagelabel.setText("UltraKiss " + Kisekae.getCaptions().getString("MediaPlayerTitle")) ;
		panel4.add(imagelabel, BorderLayout.CENTER) ;
      copyrightlabel.setText(Kisekae.getCopyright()) ;
      copyrightlabel.setHorizontalAlignment(SwingConstants.CENTER) ;
//		panel4.add(copyrightlabel, BorderLayout.SOUTH) ;
		panel1.add(panel4, BorderLayout.CENTER) ;
		getContentPane().add(panel1) ;
	}


	// Action event listener.  We only process menu item actions.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

		try
		{
			// Exit request.

			if (source == exit)
			{
				stop() ;
				return ;
			}

			// Open file request.

			if (source == open)
			{
            Vector v = new Vector() ;
				String [] ext = ArchiveFile.getMediaListExt() ;
				for (int i = 0 ; i < ext.length ; i++) v.addElement(ext[i]) ;
				FileOpen fdnew = new FileOpen(this,Kisekae.getCaptions().getString("MediaFileList"),ext) ;
            fdnew.setFileFilter("mediafiles") ;
            fdnew.setMultiple(true) ;
				fdnew.show() ;
            
            int n = 0 ;
				ArchiveEntry ze = fdnew.getZipEntry(n) ;
				if (ze == null) { fdnew.close() ; return ; }

				// Confirm that we selected a valid file.

            String s = ze.getExtension().toUpperCase() ;
            if (!(v.contains(s)))
				{
					String name = ze.getName() ;
               s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
               JOptionPane.showMessageDialog(this,
                  s + "\n" +
                  Kisekae.getCaptions().getString("SaveAsMediaText"),
                  Kisekae.getCaptions().getString("FileOpenException"),
                  JOptionPane.ERROR_MESSAGE) ;
					fdnew.close() ;
					return ;
				}

            // Save any previously updated file.  Start the playback.

         	if (!closecheck(true)) return ;
            play(ze) ;
            
		      // Construct a playlist if we have multiple selections.

            ArchiveEntry ze1 = ze ;
            ze = fdnew.getZipEntry(++n) ;
            if (ze != null && playlist == null)
            {
               playlist = new Vector() ;
               playlist.addElement(ze1) ;
               playindex = 0 ;
            }
            
		      // Queue any multiple selections.  

            while (ze != null)
            {
               if (!(ze.isAudio() || ze.isVideo() || ze.isList()))
               {
                  s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
                  int i1 = s.indexOf('[') ;
                  int j1 = s.indexOf(']') ;
                  if (i1 >= 0 && j1 > i1)
                     s = s.substring(0,i1+1) + ze.getName() + s.substring(j1) ;
                  JOptionPane.showMessageDialog(this,
                     s + "\n" +
                     Kisekae.getCaptions().getString("SaveAsMediaText"),
                     Kisekae.getCaptions().getString("FileOpenException"),
                     JOptionPane.ERROR_MESSAGE) ;
                  return ;
               }

               // Add the next file to the playlist.

               if (ze.isList())
                  playlist.addAll(parselist(ze)) ;
               else 
                  playlist.addElement(ze) ;
               ze = fdnew.getZipEntry(++n) ;
            }

            // Construct a new scrollable list.

            list1 = new JList(playlist) ;
            scrollpane = new JScrollPane(list1) ;
            updateLayout() ;
            setValues() ;
				return ;
			}

			// Select file request.

			if (source == select)
			{
         	if (fd == null) return ;
	         fd.open() ;
				String [] ext = ArchiveFile.getMediaExt() ;
				ArchiveEntry ze = fd.showConfig(this,Kisekae.getCaptions().getString("MediaFileList"),ext) ;
				if (ze == null) { fd.close() ; return ; }
         	if (!closecheck(true)) return ;
            play(ze) ;
				return ;
			}

			// Queue file request.

			if (source == queue)
			{
				String [] ext = ArchiveFile.getMediaListExt() ;
				FileOpen fdnew = new FileOpen(this,Kisekae.getCaptions().getString("MediaFileList"),ext) ;
            fdnew.setMultiple(true) ;
				fdnew.show() ;
            
            int n = 0 ;
				ArchiveEntry ze = fdnew.getZipEntry(n) ;
				if (ze == null) { fdnew.close() ; return ; }

		      // If we are queuing a file then the it had better be a media
		      // file type.

            while (ze != null)
            {
               if (!(ze.isAudio() || ze.isVideo() || ze.isList()))
               {
                  String s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
                  int i1 = s.indexOf('[') ;
                  int j1 = s.indexOf(']') ;
                  if (i1 >= 0 && j1 > i1)
                     s = s.substring(0,i1+1) + ze.getName() + s.substring(j1) ;
                  JOptionPane.showMessageDialog(this,
                     s + "\n" +
                     Kisekae.getCaptions().getString("SaveAsMediaText"),
                     Kisekae.getCaptions().getString("FileOpenException"),
                     JOptionPane.ERROR_MESSAGE) ;
                  return ;
               }

               // We have a new file.  If there is a playlist add it to the end.
               // If there is no list, construct one.

               if (playlist == null) 
               {
                  playlist = new Vector() ;
                  if (ko != null)
                  {
                     ArchiveEntry kz = ko.getZipEntry() ;
                     if (kz != null) playlist.addElement(kz) ;
                  }
                  playindex = 0 ;
               }
               if (ze.isList())
                  playlist.addAll(parselist(ze)) ;
               else 
                  playlist.addElement(ze) ;
               ze = fdnew.getZipEntry(++n) ;
            }

            // Construct a new scrollable list.

            updated = true ;
            list1 = new JList(playlist) ;
            scrollpane = new JScrollPane(list1) ;
            if (ko == null)
               playNext() ;
            else
            {
               updateLayout() ;
               setValues() ;
            }
            return ;
         }

         // A delete request removes the current entry from the playlist.

         if (source == delete)
	      {
	      	if (playlist == null) return ;
	      	if (playindex < 0 || playindex >= playlist.size()) return ;
	         playlist.removeElementAt(playindex) ;
	         updated = true ;
	         playNext() ;
	      }

			// Save playlist file request.

			if (source == save || source == saveas)
			{
         	saveplaylist(source == saveas) ;
            return ;
         }

			// Close Media request.


			if (source == close)
			{
         	if (!closecheck(true)) return ;
            playlist = null ;
				closeMedia() ;
				if (fd != null) fd.close() ;
				updateLayout() ;
            setValues() ;
				return ;
			}

   		// A Help Contents request occurs only if the installed Help system is
         // not available.  In this case we attempt to reference online help
         // through Kisekae World.

   		if (source== help)
   		{
            BrowserControl browser = new BrowserControl() ;
            String helpurl = OptionsDialog.getWebSite() + OptionsDialog.getOnlineHelp() ;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            browser.displayURL(helpurl+onlinehelp);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   			return ;
   		}

			// The Help About request brings up the About dialog window.

			if (source == about)
			{
				if (aboutdialog != null) aboutdialog.show() ;
				return ;
			}
         
         // Window display commands are of the form 'nn. title'. If we have
         // one of these bring the window to the front.
         
         if (command.indexOf(". ") > 0)
         {
            String s = command ;
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

			// The Play button starts the audio file.

			if (command.equals(Kisekae.getCaptions().getString("MediaPlayMessage")))
			{
				resume() ;
				return ;
			}

			// The Stop button stops the audio file.

			if (command.equals(Kisekae.getCaptions().getString("MediaStopMessage")))
			{
				suspend() ;
				return ;
			}

			// The Reset button starts the audio file from the beginning.  This
         // button should only be visible when stopped.

			if (command.equals(Kisekae.getCaptions().getString("ResetMessage")))
			{
				restart() ;
				return ;
			}

         // If we see an Audio callback, resume playback.  Audio callbacks
         // occur when a music file stops.

         if ("Audio Callback".equals(evt.getActionCommand()))
         {
         	resume() ;
            return ;
         }

			// An update request from the file save window has occured.
         // Once the file is written changes cannot be undone.

			if ("FileWriter Callback".equals(evt.getActionCommand()))
			{
            updated = false ;
				return ;
			}

         if ("MediaLoad Callback".equals(evt.getActionCommand()))
         {
            if (loader == null) return ;
            ko = loader.getKissObject() ;
            error = (ko != null) ? ko.isError() : true ;
            loader = null ;
				play() ;
         }
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("MediaFrame: Out of memory.") ;
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
			PrintLn.println("MediaFrame: Internal fault, action " + evt.getActionCommand()) ;
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
	// when checkbox menu items are selected.  This is only for MediaPlayer
   // invoked manually and not through internal mediaplayer() statements.

	public void itemStateChanged(ItemEvent evt)
	{
//      if (isInternal()) return ;
		Object source = evt.getSource() ;

		// Turn loop control on and off.

		if (source == loopcontrol)
		{
         OptionsDialog.setAutoMediaLoop(loopcontrol.getState()) ;
			return ;
		}

		if (source == fullscreen)
		{
			OptionsDialog.setAutoFullScreen(fullscreen.getState()) ;
			return ;
		}

		if (source == minimize)
		{
         boolean b = minimize.getState() ;
			OptionsDialog.setMediaMinimize(b) ;
         setMinimized(b) ;
         setVisible(!b) ;
			return ;
		}

		if (source == centerframe)
		{
			OptionsDialog.setMediaCenter(centerframe.getState()) ;
			return ;
		}

		if (source == aspectratio)
		{
			OptionsDialog.setKeepAspect(aspectratio.getState()) ;
			return ;
		}

      if (source == volume)
		{
         if (volume.getState())
         { 
            String s = JOptionPane.showInputDialog(Kisekae.getCaptions().getString("MediaVolumeAdjustment"),
                 OptionsDialog.getMediaVolume()) ;
            if (s != null)
            {
               try { volumeadjust = Float.valueOf(s) ; }
               catch (NumberFormatException e) { }
               if (volumeadjust < 0) volumeadjust = 0 ;
               if (volumeadjust > 1.0f) volumeadjust = 1.0f ;
               resume() ;
            }
         }
         return ;
      }
	}


	// MenuSelected interface.  Menu selection events are captured.

	public void menuDeselected(MenuEvent evt) { }
	public void menuCanceled(MenuEvent evt) { }
	public void menuSelected(MenuEvent evt)
	{
		Object source = evt.getSource() ;

		// An Options Menu request must update the current checkbox state.

		if (source == optionMenu)
		{
        	loopcontrol.setState(OptionsDialog.getAutoMediaLoop()) ;
         fullscreen.setState(OptionsDialog.getAutoFullScreen()) ;
         minimize.setState(OptionsDialog.getMediaMinimize()) ;
         centerframe.setState(OptionsDialog.getMediaCenter()) ;
         aspectratio.setState(OptionsDialog.getKeepAspect()) ;
         return ;
      }
   }

   // Implementation of the menu item update of our state when we become
   // visible.  We remove all prior entries and rebuild the Window menu. 
  
   void updateRunState()
   {
      MainMenu mainmenu = null ;
      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe != null) mainmenu = mainframe.getMainMenu() ;
      if (mainmenu != null) mainmenu.updateRunState() ;                   
   }



	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { updateRunState() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { stop() ; }


	// Method to set the dialog field values.

	private void setValues()
	{
		String title = Kisekae.getCaptions().getString("MediaPlayerTitle") ;
		if (audio != null) title += " - " + audio.toString() ;
		if (video != null) title += " - " + video.toString() ;
		setTitle(title) ;
      select.setEnabled(fd != null) ;
      saveas.setEnabled(playlist != null && !Kisekae.isSecure()) ;
      close.setEnabled(ko != null || playlist != null) ;
      delete.setEnabled(playlist != null) ;
	}


   // Method to return the name of the media object.

   public String getName()
	{
		if (audio != null) return audio.getName() ;
		if (video != null) return video.getName() ;
		return Kisekae.getCaptions().getString("UnknownValueText") ;
   }


   // Method to return the unique media player if such exists.

   static public MediaFrame getUniquePlayer() { return unique ; }
   

   // Method to set minimized state.

   public void setMinimized(boolean b) { minimize.setState(b) ; }
   public void setMinimized(boolean b, int x, int y)
   {
      minimize.setState(b) ;
      setLocation(x,y) ;
   }


   // Method to set repeat loop state.

   public void setRepeat(boolean b)
   {
      loopcontrol.setState(b) ;
      repeatcount = (b) ? -1 : 0 ;
      OptionsDialog.setAutoMediaLoop(loopcontrol.getState()) ;
   }
	void setRepeat(int n)
   {
      repeatcount = n ;
      loopcontrol.setState(repeatcount != 0) ;
      OptionsDialog.setAutoMediaLoop(loopcontrol.getState()) ;
   }


	// Return true if the object is internally generated through FKissAction.

	void setInternal(boolean b) { internal = b ; }
   
	boolean isInternal() { return internal ; }

   
   // Method to set a minimized visible state if required.

   public void setVisible(boolean b)
   {
   	doLayout() ;
   	int state = (minimize.getState()) ? Frame.ICONIFIED : Frame.NORMAL ;
   	setState(state) ;
      super.setVisible(b) ;
   }


	// Method to play a KiSS media file.

	void play()
	{
		if (ko instanceof Audio) audio = (Audio) ko ;
		if (ko instanceof Video) video = (Video) ko ;
      if (OptionsDialog.getDebugSound() && ko != null)
	      PrintLn.println("MediaPlayer: play " + ko) ;
      
      // If we are loading from a previous play() request then interrupt 
      // the load and continue with this request.
      
      if (loader != null && loader.isActive())
      {
         loader.interrupt() ;
         loader = null ;
      }

      // Check for errors.

   	if (error)
      {
      	String s = (ko != null) ? ko.getErrorMessage() : null ;
         if (s == null) s = Kisekae.getCaptions().getString("UnrecognizableMedia") ;
         statelabel.setText(s) ;
         String s1 = (ko != null) ? ko.toString() : "" ;
			JOptionPane.showMessageDialog(me, s + "\n" + s1,
            Kisekae.getCaptions().getString("MediaPlayerFault"),
         	JOptionPane.ERROR_MESSAGE) ;
         closeMedia() ;
         updateLayout() ;
         setValues() ;
         fireMediaStop() ;
      	return ;
      }

		// Open the media file.  For audio sound files the open may fail.
      // We can try and recover using Java Media Framework.

		if (video != null) video.open() ;
		if (audio != null)
      {
         audio.open() ;
         
         if (audio.isError() && Kisekae.isMediaInstalled())
         {
            if (audio instanceof AudioSound)
            {
               if (audio.isInternal())
               {
                  ko = new AudioMedia(audio.getZipFile(),audio.getPath()) ;
                  loadMedia() ;
                  return ;
               }
               else
               {
                  ko = new AudioMedia(audio.getZipFile(),audio.getPath()) ;
      				Audio a1 = (Audio) ko ;
         			a1.setIdentifier(audio.getIdentifier()) ;
                  a1.setRelativeName(audio.getRelativeName()) ;
//                a1.setRepeat(audio.getRepeat()) ;
                  a1.setID(audio.getID()) ;
                  a1.setType("media") ;
           			a1.load() ;
                  MainFrame mf = Kisekae.getMainFrame() ;
                  Configuration config = (mf != null) ? mf.getConfig() : null ;
                  Object cid = (config != null) ? config.getID() : null ;
         			a1.setKey(a1.getKeyTable(),cid,a1.getPath().toUpperCase()) ;
                  Enumeration enum1 = audio.getEvents() ;
                  while (enum1 != null && enum1.hasMoreElements())
                     a1.addEvent((Vector) enum1.nextElement()) ;
              	   a1.init() ;
                  audio = a1 ;
               }
            }
         }
         
         // If error, clear state
         
         if (audio.isError())
         {
            currentmedia = null ;
            updateLayout() ;
            setValues() ;
            return ;
         }
      }

      // Get the media player.

		if (audio != null && OptionsDialog.getSoundOn())
      	currentmedia = audio.getPlayer() ;
		if (video != null && OptionsDialog.getMovieOn())
			currentmedia = video.getPlayer() ;
      
      // Add listeners.

     	if (audio != null && currentmedia instanceof Sequencer)
      {
         audiolistener = new SequencerListener() ;
        	audio.addListener(audiolistener) ;
      }
     	if (audio != null && currentmedia instanceof Clip)
      {
         audiolistener = new ClipListener() ;
        	audio.addListener(audiolistener) ;
      }
      if (Kisekae.isMediaInstalled())
      {
         medialistener = new MediaListener() ;
        	if (audio != null && currentmedia instanceof Player)
           	audio.addListener(medialistener) ;
         else if (video != null && currentmedia instanceof Player)
           	video.addListener(medialistener) ;
         else 
            medialistener = null ;
      }

		// Adjust the visual layout.

		if (currentmedia != null)
		{
      	if (Kisekae.isMediaInstalled() && currentmedia instanceof Player)
         {
         	Player player = (Player) currentmedia ;
				int state = player.getState() ;
				realized = (state >= Player.Realized) ;
            if (realized)
            {
               GainControl gc = player.getGainControl() ;
               if (gc != null && volume.isSelected()) 
                  gc.setLevel(20f * (float) Math.log10(volumeadjust)) ;
            }
         }
      	if (Kisekae.isMediaInstalled() && currentmedia instanceof Clip)
         {
         	Clip clip = (Clip) currentmedia ;
            FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN) ;
            if (fc != null && volume.isSelected())
               fc.setValue(20f * (float) Math.log10(volumeadjust)) ;
         }
      }
		updateLayout() ;
      setValues() ;
      updateRunState() ;
      toFront() ;

		// Start playing the media file.

		if (audio != null) audio.play() ;
		if (video != null) video.play() ;

		// Activate the progress bar.

      progress.setMinimum(0) ;
      progress.setMaximum(1000) ;
      progress.setValue(0) ;
      if (timer != null) 
         timer.start() ;
      else 
      {
         timer = new Timer(300,timerTask) ;
         timer.start() ;
      }
	}

   // Method to play a specific KiSS object.

   void play(KissObject kiss)
   {
		if (kiss == null) return ;
      closeMedia() ;
      ko = kiss ;
		if (ko instanceof Audio) Audio.stop((Audio) ko) ;
		if (ko instanceof Video) Video.stop((Video) ko) ;
      ko.setRepeat(repeatcount);
      play() ;
   }


   // A method to play the next media file in the play list.

	void playNext()
   {
   	if (playlist == null) return ;
		if (playindex >= playlist.size() && OptionsDialog.getAutoMediaLoop())
      {
			playindex = 0 ;
         if (repeatcount > 0) repeatcount-- ;
         if (repeatcount == 0) loopcontrol.setState(false) ;
      }

      // Close current media file.  Exit the playlist if we are finished.

		closeMedia() ;
		if (playindex >= playlist.size())
      {
         updateLayout() ;
			setValues() ;
         progress.setValue(0) ;
         fireMediaStop() ;
  			return ;
      }

      // Determine the type of media file to play.

      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      Object cid = (config != null) ? config.getID() : null ;
		ArchiveEntry ze = (ArchiveEntry) playlist.elementAt(playindex) ;
      String s1 = (ze != null) ? ze.getPath() : null ;
      if (s1 != null) s1 = s1.toUpperCase() ;
      KissObject kiss = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;
      if (kiss != null)
      {
         ko = kiss ;
   		setValues() ;
         play() ;
         return ;
      }

      // Not an active file.  Load and play.

		if (ze.isAudioSound())
      {
         if (Kisekae.isWebswing())
            ko = new AudioWebswing(ze.getZipFile(),ze.getPath()) ;
         else
            ko = new AudioSound(ze.getZipFile(),ze.getPath()) ;
      }
		if (ze.isAudioMedia())
         ko = new AudioMedia(ze.getZipFile(),ze.getPath()) ;
		if (ze.isVideo())
         ko = new Video(ze.getZipFile(),ze.getPath(),null) ;
		if (ko != null) ko.setInternal(true) ;
		setValues() ;
		loadMedia() ;
   }


   // Method to play an arbitrary archive entry.

   void play(ArchiveEntry ze)
   {
		if (ze == null) return ;
      closeMedia() ;
     	save.setEnabled(false) ;
		fd = ze.getFileOpen() ;
		if (fd != null) { fd.open(ze.getPath()) ; ze = fd.getZipEntry() ; }
      if (ze == null) return ;
      
		if (ze.isAudioSound())
      {
         if (Kisekae.isWebswing())
            ko = new AudioWebswing(ze.getZipFile(),ze.getPath()) ;
         else
            ko = new AudioSound(ze.getZipFile(),ze.getPath()) ;
      }
		if (ze.isAudioMedia())
         ko = new AudioMedia(ze.getZipFile(),ze.getPath()) ;
		if (ze.isVideo())
         ko = new Video(ze.getZipFile(),ze.getPath(),null) ;
      if (ze.isList()) 
         playlist = parselist(ze) ;
      if (ko != null)
      {
      	playlist = null ;
         ko.setRepeat(repeatcount);
         ko.setInternal(true) ;
		}

      // Close any current media and start the new media.

		if (playlist == null)
      {
         playlistname = null ;
      	loadMedia() ;
         return ;
      }

      // Start a playlist.

      playze = ze ;
     	save.setEnabled(!Kisekae.isSecure()) ;
		list1 = new JList(playlist) ;
		scrollpane = new JScrollPane(list1) ;
      playindex = 0 ;
     	playNext() ;

      // Find any playlist mediastart() events.

      playlistname = ze.getName() ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      EventHandler handler = (config != null) ? config.getEventHandler() : null ;
      Vector v = (handler != null) ? handler.getEvent("mediastart") : null ;

      // Fire the event if it is for this playlist.

      Enumeration enum1= (v != null) ? v.elements() : null ;
      while (enum1!= null && enum1.hasMoreElements())
      {
         FKissEvent evt = (FKissEvent) enum1.nextElement() ;
         String p1 = evt.getFirstParameter() ;
         p1 = Variable.getStringLiteralValue(p1) ;
         if (p1 != null && p1.equals(playlistname))
            EventHandler.queueEvent(evt,Thread.currentThread(),this) ;
      }
   }


   // A method to parse a text file playlist and return a vector
   // of playable ArchiveEntries.

   private Vector parselist(ArchiveEntry ze)
   {
   	int errors = 0 ;
   	Vector list = new Vector() ;
   	if (ze == null) return list ;
      ArchiveFile zip = ze.getZipFile() ;
      if (zip == null) return list ;

      // Read the playlist text file.

      try
      {
         ArchiveEntry play ;
      	InputStream is = ze.getInputStream() ;
         if (is == null) throw new IOException("Playlist null input stream") ;
			InputStreamReader isr = new InputStreamReader(is) ;
			BufferedReader f = new BufferedReader(isr) ;

         // Decode each line.  Playlist entries should be one per line
         // and reference the relative path name of the media file
         // in the current archive.

         while (true)
         {
         	play = null ;
            if (errors >= 10) break ;
				String s = f.readLine() ;
				if (s == null) break ;
				s = Configuration.trim(s) ;
            StringTokenizer st = new StringTokenizer(s,"\r\t\n") ;
            if (!st.hasMoreTokens()) continue ;
            String token = st.nextToken() ;

            // Find this element name in the archive file.

	      	if (zip instanceof DirFile)
	         {
	         	String directory = zip.getDirectoryName() ;
	            if (directory != null)
	            {
						File file = new File(directory,token) ;
		            token = file.getPath() ;
						play = zip.getEntry(token) ;
	            }
	         }
	         else
		      	play = zip.getEntry(token) ;

            // If no find, show error.

            if (play != null)
	            list.addElement(play) ;
            else
            {
            	errors++ ;
               s = "MediaPlayer: Playlist element not found, " + token ;
               if (OptionsDialog.getDebugMedia()) PrintLn.println(s) ;
               s = Kisekae.getCaptions().getString("FileOpenFileOpenMessage1") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + token + s.substring(j1) ;
			   	statelabel.setText(s) ;
				}
         }
      }

      // Watch for IO Exceptions

      catch (IOException e)
      {
      	PrintLn.println("MediaPlayer: playlist exception, " + ze.getName()) ;
         PrintLn.println(e.getMessage()) ;
         JOptionPane.showMessageDialog(me, ze.getName() + "\n" + e.getMessage(),
            Kisekae.getCaptions().getString("MediaPlayerFault"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Return the playlist.

      return list ;
   }


   // Fire any mediastop events for playlist files.

   private void fireMediaStop()
   {
      if (playlistname == null) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      EventHandler handler = (config != null) ? config.getEventHandler() : null ;
      Vector v = (handler != null) ? handler.getEvent("mediastop") : null ;

      // Fire the event if it is for this playlist.

      Enumeration enum1 = (v != null) ? v.elements() : null ;
      while (enum1 != null && enum1.hasMoreElements())
      {
         FKissEvent evt = (FKissEvent) enum1.nextElement() ;
         String p1 = evt.getFirstParameter() ;
         p1 = Variable.getStringLiteralValue(p1) ;
         if (p1 != null && p1.equals(playlistname))
            EventHandler.queueEvent(evt,Thread.currentThread(),this) ;
      }
   }


	// We overload the KissDialog close method to shut the media files down
	// when the dialog frame is closed.

	public void close()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { close() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}

      if (OptionsDialog.getDebugControl())
         PrintLn.println("Media Player " + me.getTitle() + " is closed.");       
      closecheck(false) ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      if (config != null) config.setMediaFrame(null) ;
      if (unique == me) unique = null ;
      play.removeActionListener(this);
      reset.removeActionListener(this);
		list1.removeListSelectionListener(listListener);
      if (timer != null) 
      {
         timer.removeActionListener(timerTask) ;
         timer.stop() ;
         timer = null ;
      }
		removeWindowListener(this);
      setVisible(false) ;
		super.close() ;
		flush() ;
      frame.dispose() ;
	}


   // Method to stop and dispose of the media player object.

   public void stop()
   {
      suspend() ;
      closeMedia() ;
      fireMediaStop() ;
      close() ;
   }


   // Method to suspend the media player.

   public void suspend()
   {
   	Object player = null ;
		if (audio != null) player = audio.getPlayer() ;
		if (video != null) player = video.getPlayer() ;
		if (Kisekae.isMediaInstalled() && player instanceof Player)
      {
      	((Player) player).stop() ;
      }
		if (player instanceof Sequencer)
      {
      	((Sequencer) player).stop() ;
         updateStatus() ;
         if (audio != null)
         {
            audio.doCallback() ;
            audio.started = false ;
            if (timer != null) timer.stop() ;
         }
      }
		if (player instanceof Clip)
      {
      	((Clip) player).stop() ;
         if (timer != null) timer.stop() ;
      }
   }


   // Method to resume the media player.  We reset the playback if we
   // have less than 2% of the stream remaining to play.

   public void resume()
   {
   	Object player = null ;
		if (audio != null) 
      {
         player = audio.getPlayer() ;
         if (!audio.isOpen())
         {
            audio.open() ;
            player = audio.getPlayer() ;
            currentmedia = player ;
      
            // Add listeners.

           	if (audio != null && player instanceof Sequencer)
            {
               audiolistener = new SequencerListener() ;
              	audio.addListener(audiolistener) ;
            }
          	if (audio != null && player instanceof Clip)
            {
               audiolistener = new ClipListener() ;
              	audio.addListener(audiolistener) ;
            }
            if (Kisekae.isMediaInstalled())
            {
               medialistener = new MediaListener() ;
              	if (audio != null && player instanceof Player)
                 	audio.addListener(medialistener) ;
               else if (video != null && player instanceof Player)
                 	video.addListener(medialistener) ;
               else 
                  medialistener = null ;
            }
         }
      }
		if (video != null) 
      {
         player = video.getPlayer() ;
      }
		if (Kisekae.isMediaInstalled() && player instanceof Player)
		{
      	Player p = (Player) player ;
         GainControl gc = p.getGainControl() ;
         if (gc != null && volume.isSelected()) 
            gc.setLevel(20f * (float) Math.log10(volumeadjust)) ;
			int state = p.getState() ;
			if (state != Player.Started) p.start() ;
		}
		if (player instanceof Sequencer)
		{
      	Sequencer p = (Sequencer) player ;
         long duration = p.getMicrosecondLength() ;
         long diff = Math.abs(p.getMicrosecondPosition() - duration) ;
         if (diff <= (int) (duration * 0.02))
         {
            p.setMicrosecondPosition(0) ;
            progress.setValue(0) ;
         }
			p.start() ;
         updateStatus() ;  // required as no media start event for sequencer
         if (timer != null) timer.start() ;
		}
		if (player instanceof Clip)
		{
      	Clip p = (Clip) player ;
         long duration = p.getFrameLength() ;
         long diff = Math.abs(p.getFramePosition() - duration) ;
         if (diff <= (int) (duration * 0.02))
         {
            p.setFramePosition(0) ;
            progress.setValue(0) ;
         }
         FloatControl fc = (FloatControl) p.getControl(FloatControl.Type.MASTER_GAIN) ;
         if (fc != null && volume.isSelected())
            fc.setValue(20f * (float) Math.log10(volumeadjust)) ;
			p.start() ;
         if (timer != null) timer.start() ;
		}
   }


   // Method to restart the media player from the beginning.

   public void restart()
   {
   	Object player = null ;
		if (audio != null) player = audio.getPlayer() ;
		if (video != null) player = video.getPlayer() ;
		if (Kisekae.isMediaInstalled() && player instanceof Player)
		{
      	Player p = (Player) player ;
			int state = p.getState() ;
			if (state != Player.Started) p.start() ;
		}
		if (player instanceof Sequencer)
		{
      	Sequencer p = (Sequencer) player ;
         p.setMicrosecondPosition(0) ;
			p.start() ;
         updateStatus() ;  // required as no media start event for sequencer
         if (timer != null) timer.start() ;
		}
		if (player instanceof Clip)
		{
      	Clip p = (Clip) player ;
         p.setMicrosecondPosition(0) ;
			p.start() ;
         if (timer != null) timer.start() ;
		}
   }


	// Utility method to update the user interface.  This must be done in the
	// AWT thread.  We adjust the frame layout depending on the state.   Media
   // players show the visual control and control component if the player is
   // realized.  Audio media shows the highlighted playlist and control
   // component if a playlist exists.  Only the control component is shown
   // for a single media play request.

	void updateLayout()
	{
		panel1.removeAll() ;
      if (Kisekae.isMediaInstalled() && currentmedia instanceof Player)
      {
        	Player player = (Player) currentmedia ;
			if (realized)
			{
				visual = player.getVisualComponent() ;
				control = player.getControlPanelComponent() ;
				panel2 = new JPanel() ;
				panel2.setLayout(new BorderLayout()) ;
            panel2.setBorder(eb2) ;
				panel3 = new JPanel() ;
				panel3.setLayout(new BorderLayout()) ;
            if (control != null) panel3.add(control,BorderLayout.CENTER) ;
				if (visual != null)
            {
              	panel2.add(visual,BorderLayout.CENTER) ;
					panel2.add(panel3,BorderLayout.SOUTH) ;
            }
            else
            {
					if (playlist != null)
               {
                 	panel2.add(scrollpane,BorderLayout.CENTER) ;
						list1.removeListSelectionListener(listListener) ;
						list1.setSelectedIndex(playindex) ;
                  list1.ensureIndexIsVisible(playindex) ;
						list1.addListSelectionListener(listListener) ;
						panel2.add(panel3,BorderLayout.SOUTH) ;
               }
               else
               {
     					panel2.add(panel3,BorderLayout.CENTER) ;
               }
            }
				panel1.add(panel2,BorderLayout.CENTER) ;
			}
        	else
         {
           	if (playlist != null)
            {
              	panel2.add(scrollpane,BorderLayout.CENTER) ;
               list1.removeListSelectionListener(listListener) ;
               list1.setSelectedIndex(playindex) ;
               list1.ensureIndexIsVisible(playindex) ;
               list1.addListSelectionListener(listListener) ;
            }
            else
            {
					panel2.add(panel4,BorderLayout.CENTER) ;
            }
            panel1.add(panel2,BorderLayout.CENTER) ;
         }
      }

      // Sound clips show a Play or Stop button to control the sound.
      // The status component is retained to monitor state changes.

      if (currentmedia instanceof Clip || currentmedia instanceof Sequencer)
      {
			panel2 = new JPanel() ;
			panel2.setLayout(new BorderLayout()) ;
         panel2.setBorder(eb2) ;
         panel3 = new JPanel() ;
			panel3.setLayout(new FlowLayout()) ;
         play.setText(Kisekae.getCaptions().getString("MediaPlayMessage")) ;
         play.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaPlay")) ;
         reset.setText(Kisekae.getCaptions().getString("ResetMessage")) ;
         reset.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaReset")) ;
         panel3.add(play) ;
         panel3.add(reset) ;
         reset.setVisible(false) ;
        	if (playlist != null)
         {
           	panel2.add(scrollpane,BorderLayout.CENTER) ;
            list1.removeListSelectionListener(listListener) ;
            list1.setSelectedIndex(playindex) ;
            list1.ensureIndexIsVisible(playindex) ;
            list1.addListSelectionListener(listListener) ;
				panel2.add(panel3,BorderLayout.SOUTH) ;
         }
         else
         {
				panel2.add(panel3,BorderLayout.CENTER) ;
         }
			panel1.add(panel2, BorderLayout.CENTER) ;
      }

      // If no media file is active we show either the playlist or
      // the logo.

      if (currentmedia == null)
      {
			panel2 = new JPanel() ;
			panel2.setLayout(new BorderLayout()) ;
         panel2.setBorder(eb2) ;
        	if (playlist != null)
         {
           	panel2.add(scrollpane,BorderLayout.CENTER) ;
            list1.removeListSelectionListener(listListener);
            list1.setSelectedIndex(playindex) ;
            list1.ensureIndexIsVisible(playindex) ;
            list1.addListSelectionListener(listListener);
         }
         else
         {
				panel2.add(panel4, BorderLayout.CENTER);
         }
			panel1.add(panel2, BorderLayout.CENTER) ;
      }

      panel1.add(panel5,BorderLayout.SOUTH) ;

		// Determine the preferred size.  The frame is centered if
      // necessary.

		doLayout() ;
           
      Dimension s = getPreferredSize() ;
      if (!(ko instanceof Video)) s.width = Math.max(s.width,400) ;
		setSize(s) ;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
      if (OptionsDialog.getMediaCenter()) setLocation(x,y) ;

		// Maximize video playback if required.

		if (fullscreen.getState() && ko instanceof Video)
		{
			d = new Dimension((int) (d.width*1.00),(int) (d.height*0.95)) ;
			setSize(d) ;
			setLocation(0,0) ;
		}

      // Layout the frame.

		validate() ;
      if (playlist != null) list1.requestFocus() ;
		repaint() ;
	}


   // Method to update our status.

	void updateStatus()
   {
      String state = null ;
      if (Kisekae.isMediaInstalled() && currentmedia instanceof Player)
      {
      	Player player = (Player) currentmedia ;
         state = Kisekae.getCaptions().getString("MediaInactiveState") ;
			int playerstate = player.getState() ;
			if (playerstate == Player.Prefetched)
            state = Kisekae.getCaptions().getString("MediaPrefetchedState") ;
	      if (playerstate == Player.Prefetching)
            state = Kisekae.getCaptions().getString("MediaPrefetchingState") ;
	      if (playerstate == Player.Realized)
            state = Kisekae.getCaptions().getString("MediaRealizedState") ;
	      if (playerstate == Player.Realizing)
            state = Kisekae.getCaptions().getString("MediaRealizingState") ;
	      if (playerstate == Player.Started)
            state = Kisekae.getCaptions().getString("MediaStartedState") ;
	      if (playerstate == Player.Unrealized)
            state = Kisekae.getCaptions().getString("MediaUnrealizedState") ;
      }

      // Determine the sequencer attributes.  Allow a position reset unless
      // we are at the end of the sequence.

		if (currentmedia instanceof Sequencer)
      {
         play.setText(Kisekae.getCaptions().getString("MediaPlayMessage")) ;
         play.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaPlay")) ;
         state = Kisekae.getCaptions().getString("MediaInactiveState") ;
      	Sequencer p = (Sequencer) currentmedia ;
			if (p.isOpen())
            state = Kisekae.getCaptions().getString("MediaPrefetchedState") ;
			if (p.isRecording())
            state = Kisekae.getCaptions().getString("MediaRecordingState") ;
			if (p.isRunning())
            state = Kisekae.getCaptions().getString("MediaStartedState") ;
			if (p.isRunning())
         {
            play.setText(Kisekae.getCaptions().getString("MediaStopMessage")) ;
            play.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaStop")) ;
         }
         long duration = p.getMicrosecondLength() ;
         long diff = Math.abs(p.getMicrosecondPosition() - duration) ;
         if (diff > (int) (duration * 0.02)) reset.setVisible(!p.isRunning()) ;
		}

      // Determine the sound clip attributes.  Allow a position reset unless
      // we are at the end of the clip.

		if (currentmedia instanceof Clip)
      {
         play.setText(Kisekae.getCaptions().getString("MediaPlayMessage")) ;
         play.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaPlay")) ;
         state = Kisekae.getCaptions().getString("MediaInactiveState") ;
      	Clip p = (Clip) currentmedia ;
			if (p.isOpen())
            state = Kisekae.getCaptions().getString("MediaPrefetchedState") ;
			if (p.isActive())
            state = Kisekae.getCaptions().getString("MediaActiveState") ;
			if (p.isRunning())
            state = Kisekae.getCaptions().getString("MediaStartedState") ;
			if (p.isRunning())
         {
            play.setText(Kisekae.getCaptions().getString("MediaStopMessage")) ;
            play.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaStop")) ;
         }
         long duration = p.getMicrosecondLength() ;
         long diff = Math.abs(p.getMicrosecondPosition() - duration) ;
         if (diff > (int) (duration * 0.02)) 
             reset.setVisible(!p.isRunning()) ;
		}

      if (currentmedia == null)
         state = Kisekae.getCaptions().getString("MediaInactiveState") ;
      if (state != null)
         statelabel.setText(state) ;
	}


	// Method to load a media object.   The load is performed asynchronously.
	// The media playback is started after being loaded.

	void loadMedia()
	{
   	statelabel.setText(Kisekae.getCaptions().getString("MediaInitializingState")) ;
      loader = new MediaLoad(this,ko) ;
      loader.callback.addActionListener(this) ;
      loader.start() ;
   }


	// Method to close a media object.   This removes all references to the
	// media object from this frame.

	void closeMedia()
	{
   	statelabel.setText(Kisekae.getCaptions().getString("MediaInactiveState")) ;
		list1.removeListSelectionListener(listListener);
		if (audio != null)
      {
         audio.setLoader(null) ;
      	audio.removeListener(audiolistener) ;
      	audio.removeListener(medialistener) ;
         audio.stop(null,audio,null) ;
      }
		if (video != null)
      {
         video.setLoader(null) ;
      	video.removeListener(medialistener) ;
         video.stop() ;
      }
      
      // Terminate any progress timing.
      
      if (timer != null) timer.stop() ;

      // Release references.

		ko = null ;
		audio = null ;
		video = null ;
		visual = null ;
		control = null ;
      loader = null ;
		currentmedia = null ;
      audiolistener = null ;
      medialistener = null ;
		realized = false ;
		Runtime.getRuntime().gc() ;
	}


   // Method to check for pending updates.  This method returns true if
	// the check is not cancelled.

	private boolean closecheck(boolean cancel)
	{
   	if (!updated) return true ;
     	int opt = JOptionPane.YES_NO_OPTION ;
      if (cancel) opt = JOptionPane.YES_NO_CANCEL_OPTION ;

      // As we are editing a non-active file, check for a file save.

      String s1 = Kisekae.getCaptions().getString("SaveChangeText") ;
      String s2 = Kisekae.getCaptions().getString("SaveUntitledTitle") ;
      String file = (playze != null) ? playze.getPath() : null ;
      ArchiveFile zip = (playze != null) ? playze.getZipFile() : null ;
      if (file != null)
      {
         File f = new File(file) ;
         s1 = Kisekae.getCaptions().getString("SaveChangeToText") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + f.getName() + s1.substring(j1+1) ;
         if (zip != null && zip.isArchive())
         {
            s2 = Kisekae.getCaptions().getString("SaveArchiveTitle") ;
            i1 = s2.indexOf('[') ;
            j1 = s2.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s2 = s2.substring(0,i1) + zip.getFileName() + s2.substring(j1+1) ;
         }
         else
         {
            s2 = Kisekae.getCaptions().getString("SaveFileTitle") ;
            i1 = s2.indexOf('[') ;
            j1 = s2.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s2 = s2.substring(0,i1) + f.getName() + s2.substring(j1+1) ;
         }
      }

		// If we have a playlist, ask the user.

		if (playlist != null && !Kisekae.isSecure())
		{
         int i = JOptionPane.showConfirmDialog(this, s1, s2,
            opt, JOptionPane.QUESTION_MESSAGE) ;

			// Save the text contents if necessary.

			if (i == JOptionPane.CANCEL_OPTION)
				return false ;
			if (i == JOptionPane.YES_OPTION)
				saveplaylist(true) ;
		}

      // Done.

      updated = false ;
		return true ;
   }


	// Utility function to write the playlist file contents.

	private void saveplaylist(boolean saveas)
	{
		FileSave fs = null ;
		if (playlist == null) return ;
      JTextArea doc = new JTextArea() ;
      Enumeration enum1 = playlist.elements() ;

      // Build the playlist text element.

      while (enum1.hasMoreElements())
      {
      	Object o = enum1.nextElement() ;
         if (!(o instanceof ArchiveEntry)) continue ;
         ArchiveEntry ae = (ArchiveEntry) o ;
      	doc.append(ae.getName() + "\n") ;
      }

      // Convert the element to a KiSS text object and save it.

      TextObject text = new TextObject(playze,doc,null) ;
		if (text.getName() == null) saveas = true ;
		text.setUpdated(true) ;
		fs = new FileSave(this,text) ;
		if (saveas) fs.show() ; else fs.save() ;
	}

   // Loader function to show the file loaded.

	void showFile(String s)
	{
		if (OptionsDialog.getDebugLoad())
			PrintLn.println("Load: " + s) ;
	}


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
	// objects when the data set is closed, even if a problem occurs while
	// disposing the dialog window.

   private void flush()
   {
		me = null ;
		closeMedia() ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      FileOpen fileopen = (config != null) ? config.getFileOpen() : null ;
		if (fd != null && fd != fileopen) fd.close() ;
      aboutdialog = null ;
      helper = null ;
      playze = null ;
      playlist = null ;
      playlistname = null ;
      timer = null ;
      timerTask = null ;
      list1 = null ;
      listListener = null ;
		setVisible(false) ;
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
	}


   // Inner class to listen for events on our Player object.

   class MediaListener implements ControllerListener
   {
		// Handle Controller/Player events for this media player.

		public void controllerUpdate(ControllerEvent ce)
		{
			// RealizeCompleteEvent occurs after a realize() call.

			if (ce instanceof RealizeCompleteEvent)
			{
	      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
					PrintLn.println("MediaPlayer: " + getName() + " RealizeCompleteEvent") ;
				realized = true ;
				Runnable callback = new Runnable()
				{ public void run() { updateLayout() ; } } ;
				javax.swing.SwingUtilities.invokeLater(callback) ;
				return ;
			}

			// If at any point the Player encountered an error - possibly in the
	      // data stream and it could not recover from the error, it generates
	      // a ControllerErrorEvent.

			if (ce instanceof ControllerErrorEvent)
			{
	      	error = true ;
				PrintLn.println("MediaPlayer: " + getName() + " ControllerErrorEvent") ;
				PrintLn.println(ce.toString()) ;
				statelabel.setText("Media file " + getName() + " " + ce.toString()) ;
				return ;
			}


			// EndOfMediaEvent occurs when the media file has played till the end.
			// The player is now in the stopped state.

			if (ce instanceof EndOfMediaEvent)
			{
	      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
					PrintLn.println("MediaPlayer: " + getName() + " EndOfMediaEvent") ;

            // If we have a playlist start the next item in the list.

            if (playlist != null)
				{
					playindex++ ;
					Runnable runner = new Runnable()
					{ public void run() { playNext() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
           }

				// Start the player in a new thread as player initiation can take
	         // time.  This frees the Player thread.

		      else if (OptionsDialog.getAutoMediaLoop())
				{
					if (audio != null && audio.isRepeating()) return ;
					if (video != null && video.isRepeating()) return ;
		      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
                  PrintLn.println("MediaPlayer: " + getName() + " Repeat count = " + repeatcount) ;
               if (repeatcount > 0) repeatcount-- ;
               if (repeatcount == 0) loopcontrol.setState(false) ;
					Runnable runner = new Runnable()
					{ public void run() { play() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
            }

            // Otherwise stop and update our status

            else
					updateStatus() ;
				return ;
         }

         // Transition events occur when our player state changes.
         // Update our status.

			if (ce instanceof TransitionEvent)
	      {
	      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
	         	PrintLn.println("MediaPlayer: " + getName() + " TransitionEvent") ;
				updateStatus() ;
				return ;
	      }
      }
	}

	// Inner class to catch sound clip events.

	class ClipListener implements LineListener
	{
		public void update(LineEvent event)
      {
			// Stop events occurs when the media file has played till the end.

			if (event.getType() == LineEvent.Type.STOP)
			{
				if (OptionsDialog.getDebugSound())
					PrintLn.println("MediaPlayer: " + getName() + " ClipStopEvent") ;

            // Determine if we completed the current media file.

            boolean complete = false ;
            if (currentmedia instanceof Clip)
            {
               Clip p = (Clip) currentmedia ;
               long duration = p.getMicrosecondLength() ;
               long diff = Math.abs(p.getMicrosecondPosition() - duration) ;
               if (diff <= (int) (duration * 0.02)) complete = true ;
            }
            else
               complete = true ;

            // If we have a playlist start the next item in the list.

            if (playlist != null && complete)
            {
            	playindex++ ;
					Runnable runner = new Runnable()
					{ public void run() { playNext() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
           }

				// Start the player in a new thread as player initiation can take
	         // time.  This frees the Player thread.

		      else if (OptionsDialog.getAutoMediaLoop() && complete)
				{
					if (audio != null && audio.isLooping()) return ;
		      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
                  PrintLn.println("MediaPlayer: " + getName() + " Repeat count = " + repeatcount) ;
               if (audio != null) audio.stop(audio) ;
               if (repeatcount > 0) repeatcount-- ;
               if (repeatcount == 0) return ;
					Runnable runner = new Runnable()
					{ public void run() { play() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
            }

            // Otherwise stop and update our status

            else
            {
               if (audio != null) audio.stop(audio) ;
            	updateStatus() ;
            }
			}

			// Start events update our status.

			else if (event.getType() == LineEvent.Type.START)
			{
				if (OptionsDialog.getDebugSound())
					PrintLn.println("MediaPlayer: " + getName() + " ClipStartEvent") ;
            updateStatus() ;
			}
      }
   }


	// Inner class to catch sound sequencer events.  Unfortunately, there is no known
   // way to identify sequencer start events as meta messages may not exist in the
   // sequencer file.  End-of-track events are manditory.

	class SequencerListener implements MetaEventListener
	{
   	private boolean started = false ;

		public void meta(MetaMessage message)
      {
			if (message.getType() == 47)
         {  // 47 is end of track (manditory)
         	started = false ;
				if (OptionsDialog.getDebugSound())
					PrintLn.println("MediaPlayer: " + getName() + " MidiEndTrackEvent " + message.getType()) ;

            // Determine if we completed the current media file.
            // End of track is always true.

            boolean complete = true ;

            // If we have a playlist start the next item in the list.

            if (playlist != null && complete)
            {
            	playindex++ ;
					Runnable runner = new Runnable()
					{ public void run() { playNext() ; } } ;
   				javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
           }

				// Start the player in a new thread as player initiation can take
	         // time.  This frees the Player thread.

		      else if (OptionsDialog.getAutoMediaLoop() && complete)
				{
					if (audio != null && audio.isRepeating()) return ;
		      	if (OptionsDialog.getDebugSound() || OptionsDialog.getDebugMovie())
                  PrintLn.println("MediaPlayer: " + getName() + " Repeat count = " + repeatcount) ;
               if (repeatcount > 0) repeatcount-- ;
               if (repeatcount == 0) loopcontrol.setState(false) ;
               if (audio != null) audio.stop(audio) ;
               if (repeatcount > 0)
               {
         			Runnable runner = new Runnable()
         			{ public void run() { play() ; } } ;
         			javax.swing.SwingUtilities.invokeLater(runner) ;
               }
               return ;
            }

            // Otherwise stop and update our status

            else
            {
               if (audio != null) audio.stop(audio) ;
            	updateStatus() ;
            }
			}

	      // Any other meta event will signal a start event.   We use only
         // the first meta event to recognize that the stream is started.

			else
	      {  // 00 is start of track (optional)
	        	// 02 is copyright info (optional)
	         // 03 is sequence or track name (optional)
	         // 04 is instrument name (optional)
	         if (started) return ;
				if (OptionsDialog.getDebugSound())
					PrintLn.println("MediaPlayer: " + getName() + " MidiStartTrackEvent " + message.getType()) ;
	         updateStatus() ;
	         started = true ;
         }
      }
   }
}
