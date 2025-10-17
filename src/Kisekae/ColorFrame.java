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
* ColorFrame Class
*
* Purpose:
*
* This object is a generalized color editor.  It is used to edit
* KiSS palette files and other palettes created through loading
* GIF or BMP or PNG images.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import java.awt.print.* ;
import java.awt.datatransfer.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Collection ;
import java.util.Collections ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.swing.* ;
import javax.swing.colorchooser.* ;
import javax.swing.border.* ;
import javax.swing.event.* ;
import javax.swing.text.* ;
import javax.swing.undo.* ;


final class ColorFrame extends KissFrame
	implements ActionListener, ItemListener, WindowListener, ComponentListener, ClipboardOwner
{
	private static String helpset = "Help/ColorEditor.hs" ;
	private static String helpsection = "coloreditor.index" ;
	private static String onlinehelp = "coloreditor/index.html" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;

	// Color frame interface objects

	private ColorFrame me = null ;					// Reference to ourselves
	private Cel cel = null ;							// Graphic cel loaded
	private Cel writecel = null ;						// Graphic cel written
	private Cel basecel = null ;						// Graphic cel updated
	private Palette palette = null ;					// Palette being adjusted
	private Palette writepalette = null ;			// Palette being written
	private Object configobject = null ;			// Configuration object
	private Cel newcel = null ;						// New cel loaded
	private Palette newpalette = null ;				// New palette loaded
	private ArchiveEntry ze = null ;					// Zip entry for palette
	private Configuration config = null ;  		// Active configuration
	private JColorChooser cc = null ;				// Color chooser component
   private PalettePanel pp = null ;					// Palette panel component
   private PreviewPanel preview = null ;        // Color preview panel
   private ImageColorPanel icp = null ;         // Color adjust panel
	private Integer multipalette = null ; 			// Multipalette in use
	private Integer initmultipalette = null ; 	// Initial multipalette
	private Hashtable actions = null ;			   // The edit actions
   private Clipboard clipboard = null ;			// Our internal clipboard
   private String originalzepath = null ;			// Zip entry name before save
   private String originalcelname = null ;		// Cel name before save
   private String originalpalettename = null ;	// Palette name before save
   private SelectListener hueListener = null ;	// Hue button selection
   private SelectListener satListener = null ;	// Saturation button selection
   private SelectListener briListener = null ;	// Brightness button selection
   private SelectListener redListener = null ;	// Red button selection
   private SelectListener grnListener = null ;	// Green button selection
   private SelectListener bluListener = null ;	// Blue button selection
   private int frame = 0 ;								// Current cel frame
   private int initframe = 0 ;						// Initial cel frame to show
   private int initsensitivity = 10 ;				// Initial hsb-rgb sensitivity
	private boolean changed = false ;				// True, palette has changed
	private boolean memorysource = false ;  		// True, palette is active
	private boolean updated = false ;				// True, memory is updated
   private boolean undoredo = false ;				// True, performing an undo
   private boolean entrystate = false ;			// Initial file update state
   private boolean saveasrequest = false ;      // True if Save As
   private boolean framesel = false ;           // True, frame selection active
   
   // Dithering objects to retain state for undo
   
   private Image oldimage = null ;
   private Image newimage = null ;
   private Palette oldpalette = null ;
   private Palette adjpalette = null ;
   private Object [] olddata = null ;
   private Object [] newdata = null ;
  	private int oldbackground = 0 ;
  	private int newbackground = 0 ;
  	private int oldtransparent = 0 ;
  	private int newtransparent = 0 ;
 	private int oldmp = 0 ;
 	private int newmp = 0 ;

	// User interface objects

	private JPanel panel1 = new JPanel() ;  		// Main container panel
	private JPanel panel2 = new JPanel() ;			// Color changer and palette
	private JPanel panel3 = new JPanel() ; 		// Preview panel
	private JPanel panel4 = new JPanel() ; 		// Palette selection control
	private JPanel panel5 = new JPanel() ; 		// Preview panel
	private JPanel panel6 = new JPanel() ; 		// Slider panel
	private JPanel panel7 = new JPanel() ; 		// Selection and slider
   private JLabel palettelabel = new JLabel() ;
   private JLabel framelabel = new JLabel() ;
   private JLabel multilabel = new JLabel() ;
	private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private Border eb2 = BorderFactory.createEmptyBorder(0,20,0,0) ;
	private Border eb3 = BorderFactory.createEmptyBorder(10,0,0,0) ;
	private Border eb4 = BorderFactory.createEmptyBorder(0,0,10,0) ;
	private Border eb5 = BorderFactory.createEmptyBorder(0,0,20,0) ;
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

	// Undo helpers

	private UndoManager undo = new UndoManager() ;
	private UndoAction undoAction ;					// Action for undo
	private RedoAction redoAction ;					// Action for redo

	// Menu items

   private JMenu fileMenu ;
   private JMenu editMenu ;
	private JMenu optionMenu ;
	private JMenu windowMenu ;
	private JMenu helpMenu ;
	private JMenu newmenu ;
	private JMenu newfile ;
   private JMenu properties ;
	private JMenuItem newfile16 ;
	private JMenuItem newfile256 ;
	private JMenuItem newmulti ;
	private JMenuItem open ;
	private JMenuItem close ;
	private JMenuItem update ;
	private JMenuItem save ;
	private JMenuItem saveas ;
	private JMenuItem importp ;
	private JMenuItem exportp ;
	private JMenuItem mergep ;
	private JMenuItem cut ;
	private JMenuItem copy ;
	private JMenuItem paste ;
	private JMenuItem selectall ;
	private JMenuItem selectclip ;
	private JMenuItem selecthue ;
	private JMenuItem selectrgb ;
	private JMenuItem setsize ;
	private ColorMenu settransparent ;
	private ColorMenu setbackground ;
	private JMenuItem deletemp ;
	private JMenuItem insertmp ;
	private JMenuItem addmp ;
	private JMenuItem reload ;
	private JMenuItem print ;
   private JMenuItem printpreview ;
   private JMenuItem pagesetup ;
   private JMenuItem paletteproperties ;
   private JMenuItem celproperties ;
	private JMenuItem exit ;
	private JMenuItem help ;
	private JMenuItem about ;
	private JMenuItem logfile ;
	private JCheckBoxMenuItem ppwindow ;
	private JCheckBoxMenuItem ccwindow ;
	private JCheckBoxMenuItem icpwindow ;
	private JCheckBoxMenuItem previewwindow ;
	private JCheckBoxMenuItem selectwindow ;
	private JCheckBoxMenuItem showbackground ;
	private JCheckBoxMenuItem relativechange ;
	private JCheckBoxMenuItem hsblinked ;
	private JCheckBoxMenuItem rgblinked ;
   private JSlider hsbslider ;
   private JSlider rgbslider ;

	// Toolbar interface objects

	private JToolBar toolbar = null ;
	private JButton NEW = null ;
	private JButton OPEN = null ;
	private JButton CLOSE = null ;				
	private JButton SAVE = null ;
	private JButton CUT = null ;
	private JButton COPY = null ;
	private JButton PASTE = null ;
	private JButton UNDO = null ;
	private JButton REDO = null ;
	private JComboBox PALETTES = null ;
	private JComboBox MULTIPALETTES = null ;
	private JComboBox FRAMES = null ;
	private JLabel COLORTEXT = null ;
	private JLabel HSBTEXT = null ;
	private JLabel RGBTEXT = null ;
	private JPanel COLORPANEL = null ;
	private JButton HUE = null ;
	private JButton SATURATION = null ;
	private JButton BRIGHTNESS = null ;
	private JButton RED = null ;
	private JButton GREEN = null ;
	private JButton BLUE = null ;
   private JButton activeHSB = null ;
   private JButton activeRGB = null ;

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the palette file is
   // saved.

   private ActionListener writelistener = null ;
	protected CallbackButton callback = new CallbackButton(this,"ColorFrame Callback") ;

   // Print references.

   private PageFormat pageformat = null ;		// The current page format


	// Create specialized listeners for events.

	KeyListener keyListener = new KeyListener()
   {
      public void keyReleased(KeyEvent e) { }
      public void keyTyped(KeyEvent e) { }
		public void keyPressed(KeyEvent e)
      {
      	int n = 0 ;
         int mp = 0 ;

			// Ensure that we have a key of interest.

			switch (e.getKeyCode())
			{
				case KeyEvent.VK_PAGE_DOWN:
				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_HOME:
				case KeyEvent.VK_END:
					break ;
				default:
					return ;
			}

			// Identify the context.  Normal PG-UP and PG-DN select the next
			// image frame.  Alt-PG-UP and Alt-PG-DN select the next multipalette
			// entry.

      	if (!e.isAltDown())
         {
	      	if (cel == null) return ;
	         n = cel.getFrameCount() ;
	         if (n == 0) return ;
         }
         else
         {
            if (palette == null) return ;
         	if (multipalette == null) return ;
            n = palette.getMultiPaletteCount() ;
         	mp = multipalette.intValue() ;
         }

      	switch (e.getKeyCode())
         {
         // Page Down key shows the next cel frame or multipalette.

         case KeyEvent.VK_PAGE_DOWN:
         	if (!e.isAltDown())
	            { if (++frame >= n) frame = 0 ; }
            else
            	{ if (++mp >= n) mp = 0 ; }
            break ;

         // Page Up key shows the prior cel frame or multipalette.

         case KeyEvent.VK_PAGE_UP:
         	if (!e.isAltDown())
	            { if (--frame < 0) frame = n - 1 ; }
            else
            	{ if (--mp < 0) mp = n - 1 ; }
            break ;

         // Home key shows the first cel frame or multipalette.

         case KeyEvent.VK_HOME:
         	if (!e.isAltDown())
	         	frame = 0 ;
            else
            	mp = 0 ;
            break ;

         // End key shows the last cel frame or multipalette.

         case KeyEvent.VK_END:
         	if (!e.isAltDown())
	         	frame = n - 1 ;
            else
            	mp = n - 1 ;
            break ;
         }

         // Initialize for the new frame or multipalette.  This triggers
         // the associated FRAME or MULTIPALETTE listener.

         if (!e.isAltDown())
         {
         	if (FRAMES.getItemCount() > frame)
            	FRAMES.setSelectedIndex(frame) ;
         }
         else
           	MULTIPALETTES.setSelectedItem(new Integer(mp)) ;
      }
	} ;


	// Register for multipalette selection events.  This listener initializes
	// for new multipalette selections by updating the palette panel and
	// redrawing the preview image for the new multipalette colors.

	ItemListener multiListener = new ItemListener()
   {
		public void itemStateChanged(ItemEvent e)
		{
			Object o = MULTIPALETTES.getSelectedItem() ;
         if (o == null) return ;
			Integer n = (Integer) o ;
			if (n.equals(multipalette)) return ;
         if (icp != null && icp.isChanged()) icp.apply() ;
			multipalette = n ;
         updateInterface(multipalette) ;
		}
	} ;


	// Register for image selection events.  This listener initializes
	// for new image selections and updates the preview panel and establishes
	// the frame palette.  Frame objects can be strings for new GIF frames,
   // archive entries for new cels, or actual cel objects.

	ItemListener frameListener = new ItemListener()
   {
		public void itemStateChanged(ItemEvent e)
		{
			Object o = FRAMES.getSelectedItem() ;
         if (o == null) return ;
         if (framesel) return ;
         framesel = true ;

         // GIF animated frames or All Cels selection.

         if (o instanceof String)
         {
				int n ;
         	String s = (String) o ;
				if (s.startsWith("Frame "))
				{
					s = s.substring(6) ;
					try { n = Integer.parseInt(s) ; }
					catch (NumberFormatException ex) { n = -1 ; }
					if (n < 0) return ;
					if (cel == null) return ;

					// Update the user interface and the preview panel.   We retain
					// the same selection set if we are using a global palette.

					frame = n ;
					boolean lp = cel.isLocalPalette() ;
					cel.setFrame(frame) ;
					palette = cel.getPalette() ;
					boolean newselect = (cel.isLocalPalette() || lp) ;
					pp.init(palette,multipalette,newselect) ;
					preview.redraw(multipalette) ;
					if (!changed) palette.startEdits() ;
				}
				else
				{
					cel = null ;
               updateInterface(multipalette) ;
					preview.init(palette,cel,multipalette) ;
               icp.setImage(preview.getImage()) ;
				}

				validate() ;
            framesel = false ;
				return ;
			}

			// Archive entry cels.  These must be loaded as we do not have an
			// active configuration.  They use the current palette.

         if (o instanceof ArchiveEntry)
         {
         	ArchiveEntry ze = (ArchiveEntry) o ;
				FileOpen fd = (ze == null) ? null : ze.getFileOpen() ;
				if (fd != null) { fd.open(ze.getPath()) ; ze = fd.getZipEntry() ; }
            Integer pid = (palette != null) ? (Integer) palette.getIdentifier() : null ;
				cel = new KissCel(ze.getZipFile(),ze.getPath(),null) ;
            cel.setPaletteID(pid) ;
				cel.setLoader(me);
				cel.load() ;
				if (fd != null) fd.close() ;
            setAtOrigin(cel) ;
            palette = cel.getPalette() ;
            updateInterface(multipalette) ;
				preview.init(palette,cel,multipalette) ;
            icp.setImage(preview.getImage()) ;
				setTitle() ;
				validate() ;
            framesel = false ;
            return ;
         }

			// Configuration cels.  These are already loaded as part of an
			// active configuration.   They use the current palette unless
         // the cel has a specific palette group.

         if (o instanceof Cel)
         {
				basecel = (Cel) o ;
				cel = (Cel) basecel.clone() ;
				setAtOrigin(cel) ;
            if (cel.getPaletteGroupID() instanceof Integer)
            	multipalette = (Integer) cel.getPaletteGroupID() ;
            updateInterface(multipalette) ;
 				preview.init(palette,cel,multipalette) ;
            icp.setImage(preview.getImage()) ;
 				setTitle() ;
 				validate() ;
            framesel = false ;
				return ;
         }
		}
	} ;


	// Register for palette selection events.  This listener selects new
	// palette files.  We reference palettes defined in the active configuration
	// if we were initiated with a configuration object.  We will load a new
	// palette file if we were initiated through a file load.

	ItemListener fileListener = new ItemListener()
	{
		public void itemStateChanged(ItemEvent e)
		{
			Object o = PALETTES.getSelectedItem() ;
         if (o == null || o == palette) return ;
         String s1 = o.toString() ;
         String s2 = (palette != null) ? palette.getName() : "" ;
         if (s1.equalsIgnoreCase(s2)) return ;
			ArchiveEntry newze = ze ;

			// Our palette selection box is populated with palette or cel
			// objects if we were invoked with an active configuration.

			if (o instanceof Palette)
			{
				cel = null ;
				newpalette = (Palette) o ;
				if (newpalette == palette) return ;
				if (!closecheck(false)) return ;
			}

			else if (o instanceof Cel)
			{
				newcel = (Cel) o ;
				newpalette = newcel.getPalette() ;
				if (newpalette == palette) return ;
				if (!closecheck(false)) return ;
			}

			// Our palette selection box is populated with archive entries
			// if we loaded an archive file.  We could see selection entries
         // for KCF files and GIF or BMP images.

			else if (o instanceof ArchiveEntry)
			{
				if (!closecheck(false)) return ;
				memorysource = false ;
				newze = (ArchiveEntry) o ;
				if (newze == ze) return ;
				FileOpen fd = (newze == null) ? null : newze.getFileOpen() ;
				if (fd != null) { fd.open(newze.getPath()) ; newze = fd.getZipEntry() ; }
				KissObject kiss = createCelPalette(fd,newze) ;
            newcel = (kiss instanceof Cel) ? (Cel) kiss : cel ;
				newpalette = (kiss instanceof Palette) ? (Palette) kiss
            	: (newcel != cel) ? newcel.getPalette() : null ;
				if (fd != null) fd.close() ;
			}
			else return ;

			// Switch attention to the selected palette.

         changed = false ;
			ze = newze ;
			cel = newcel ;
			palette = newpalette ;
         if (cel != null) cel.setPalette(palette) ;

			// Retain the same multipalette context if possible.

         if (palette != null)
         {
	         palette.startEdits() ;
				if (palette.getMultiPaletteCount() < multipalette.intValue()+1)
					multipalette = new Integer(0) ;
         }

         // Initialize.

			initmultipalette = multipalette ;
   		if (preview != null) preview.init(palette,cel,multipalette) ;
			updateInterface(multipalette) ;
         icp.setImage(preview.getImage()) ;
		}
   } ;


	// Register for slider change events.  
   
	ChangeListener hsbSliderListener = new ChangeListener()
   {
		public void stateChanged(ChangeEvent e)
		{
         JSlider source = (JSlider) e.getSource() ;
         if (source.getValueIsAdjusting()) return ;
         int sensitivity = source.getValue() ;
         if (hsblinked.isSelected())
         {
            hueListener.setSensitivity(sensitivity) ;
            satListener.setSensitivity(sensitivity) ;
            briListener.setSensitivity(sensitivity) ;
         }
         else
         {
            if (activeHSB == HUE) hueListener.setSensitivity(sensitivity) ;
            if (activeHSB == SATURATION) satListener.setSensitivity(sensitivity) ;
            if (activeHSB == BRIGHTNESS) briListener.setSensitivity(sensitivity) ;
         }
			pp.selectHSB() ;
		}
	} ;
   
	ChangeListener rgbSliderListener = new ChangeListener()
   {
		public void stateChanged(ChangeEvent e)
		{
         JSlider source = (JSlider) e.getSource() ;
         if (source.getValueIsAdjusting()) return ;         
         int sensitivity = source.getValue() ;
         if (rgblinked.isSelected())
         {
            redListener.setSensitivity(sensitivity) ;
            grnListener.setSensitivity(sensitivity) ;
            bluListener.setSensitivity(sensitivity) ;
         }
         else
         {
            if (activeRGB == RED) redListener.setSensitivity(sensitivity) ;
            if (activeRGB == GREEN) grnListener.setSensitivity(sensitivity) ;
            if (activeRGB == BLUE) bluListener.setSensitivity(sensitivity) ;
         }
			pp.selectRGB() ;
		}
	} ;


	// Constructor for zip file entries.  This constructor is used when we
   // wish to edit element files that exist in an archive file and are
   // not memory resident as part of the currently active configuration.
   // These files may be KiSS palette files or cels.

	public ColorFrame() { this(null) ; }

	public ColorFrame(ArchiveEntry ze, ActionListener al)
	{ this(ze) ; writelistener = al ; }

	public ColorFrame(ArchiveEntry ze)
	{
		super(Kisekae.getCaptions().getString("ColorEditorTitle")) ;
		me = this ;
		this.ze = ze ;
		config = null ;
		multipalette = null ;
		memorysource = false ;

		// If we have a ZipEntry, open it and find the palette object.
      // This will be a KiSS palette or a cel that uses a palette.  We
      // will load the palette object and any associated cel object.

		if (ze != null)
      {
			FileOpen fd = ze.getFileOpen() ;
			if (fd != null) { fd.open(ze.getPath()) ; ze = fd.getZipEntry() ; }
			KissObject kiss = createCelPalette(fd,ze) ;
			cel = (kiss instanceof Cel) ? (Cel) kiss : null ;
			palette = (kiss instanceof Palette) ? (Palette) kiss
            	: (cel != null) ? cel.getPalette() : null ;
			if (fd != null) fd.close() ;
      }
      
      // If we do not have anything default to a 256 color palette.
      
      else
      {
				palette = createpalette(256) ;
      }

		// Initialize.

		init() ;
	}


	// Constructor for configuration palette entries.  This constructor is used
	// when we have an active configuration and wish to edit a memory copy of
	// an object.  This object can be a KiSS palette or a cel object that has
	// an internal palette.  We retain the initial object update state in case
	// we cancel any palette edits on exit.

	public ColorFrame(Configuration c, Object o, Integer mp) { this(c,o,mp,0) ; }
	public ColorFrame(Configuration c, Object o, Integer mp, int celframe)
	{
   	super(Kisekae.getCaptions().getString("ColorEditorTitle")) ;
   	me = this ;
		config = c ;
		multipalette = mp ;
		initmultipalette = mp ;
      initframe = celframe ;
		memorysource = true ;
      configobject = o ;

      // Configuration entries are of palette type or cel type.  For palettes,
      // we set our palette object to the entry and initialize.

		if (o instanceof Palette)
		{
			palette = (Palette) o ;
			entrystate = palette.isUpdated() ;
		}

      // For cel configuration entries, we must identify the associated
      // palette object.  For cels with internal palettes any updates to
		// to the palette are updates to the cel.  The cel is cloned as we
		// adjust the cloned attributes.  The cel is placed at the origin.

		else if (o instanceof Cel)
		{
			basecel = (Cel) o ;
			cel = (Cel) basecel.clone() ;
			setAtOrigin(cel) ;
			palette = cel.getPalette() ;

         // Default to the specified multipalette if the cel
         // is associated with a palette group.

         if (cel.getPaletteGroupID() instanceof Integer)
         	multipalette = (Integer) cel.getPaletteGroupID() ;

         // Retain the object edit update state for restoration on exit.

			if (palette != null)
         {
				if (palette.isInternal())
					entrystate = cel.isUpdated() ;
				else
					entrystate = palette.isUpdated() ;
         }
		}

      // Initialize.

      init() ;
	}


   // Frame initialization.

	private void init()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		setTitle(Kisekae.getCaptions().getString("ColorEditorTitle")) ;
		setIconImage(Kisekae.getIconImage()) ;

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;

		// Create our internal clipboard for color transfers.  The ColorFrame
		// object is the clipboard owner.

		clipboard = new Clipboard("Color Frame") ;

		// Set up the menu bar.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
		if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		String s = System.getProperty("java.version") ;
		int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
		Insets insets = new Insets(2,2,2,rm) ;
		fileMenu.setMargin(insets) ;
		fileMenu.add((newmenu = new JMenu(Kisekae.getCaptions().getString("MenuFileNew")))) ;
		if (!applemac) newmenu.setMnemonic(KeyEvent.VK_N) ;
		newmenu.add((newfile = new JMenu(Kisekae.getCaptions().getString("MenuFileNewPalette")))) ;
		newfile.add((newfile16 = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewPalette16")))) ;
		newfile16.addActionListener(this) ;
		newfile.add((newfile256 = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewPalette256")))) ;
		newfile256.addActionListener(this) ;
		newmenu.add((newmulti = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewPaletteGroup")))) ;
		newmulti.setEnabled(palette != null && !palette.isInternal()) ;
		newmulti.addActionListener(this) ;
		fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
		if (!applemac) open.setMnemonic(KeyEvent.VK_O) ;
		open.addActionListener(this) ;
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelerator));
		fileMenu.add((close = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClose")))) ;
		if (!applemac) close.setMnemonic(KeyEvent.VK_C) ;
		close.addActionListener(this) ;
      close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, accelerator));
		close.setEnabled(false) ;
		fileMenu.add((save = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSave")))) ;
		if (!applemac) save.setMnemonic(KeyEvent.VK_S) ;
		save.addActionListener(this) ;
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelerator));
		save.setEnabled((palette != null || cel != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((saveas = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAs")))) ;
		if (!applemac) saveas.setMnemonic(KeyEvent.VK_A) ;
		saveas.addActionListener(this) ;
		saveas.setEnabled((palette != null || cel != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((importp = new JMenuItem(Kisekae.getCaptions().getString("MenuFileImport")))) ;
		importp.addActionListener(this) ;
		importp.setEnabled(!Kisekae.isSecure()) ;
      importp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, accelerator));
		fileMenu.add((exportp = new JMenuItem(Kisekae.getCaptions().getString("MenuFileExport")))) ;
		exportp.addActionListener(this) ;
		exportp.setEnabled(!Kisekae.isSecure()) ;
      exportp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelerator));
		fileMenu.add((mergep = new JMenuItem(Kisekae.getCaptions().getString("MenuFileMerge")))) ;
		mergep.addActionListener(this) ;
		mergep.setEnabled(!Kisekae.isSecure()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((pagesetup = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePageSetup")))) ;
		if (!applemac) pagesetup.setMnemonic(KeyEvent.VK_U) ;
		pagesetup.addActionListener(this) ;
      pagesetup.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((printpreview = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrintPreview")))) ;
		if (!applemac) printpreview.setMnemonic(KeyEvent.VK_V) ;
		printpreview.addActionListener(this) ;
      printpreview.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((print = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrint")))) ;
		if (!applemac) print.setMnemonic(KeyEvent.VK_P) ;
		print.addActionListener(this) ;
      print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelerator));
      print.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((properties = new JMenu(Kisekae.getCaptions().getString("MenuFileProperties")))) ;
      properties.add((paletteproperties = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePropertiesPalette")))) ;
		paletteproperties.addActionListener(this) ;
      properties.add((celproperties = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePropertiesImage")))) ;
		celproperties.addActionListener(this) ;
		properties.setEnabled(false) ;
		fileMenu.addSeparator();
//		fileMenu.add((update = new JMenuItem("Update"))) ;
		update = new JMenuItem("Update") ;
//    update.addActionListener(this) ;
		update.setEnabled(false) ;
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuitColorEditor" : "MenuFileExitColorEditor" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
		if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;
      
      // Edit menu.
      
		editMenu = createEditMenu() ;
		editMenu.setMargin(insets) ;
		mb.add(editMenu) ;
      
      // Option menu.

		optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
		if (!applemac) optionMenu.setMnemonic(KeyEvent.VK_O) ;
		optionMenu.setMargin(insets) ;
		mb.add(optionMenu);
		optionMenu.add((showbackground = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsBackground")))) ;
		showbackground.addItemListener(this) ;
		showbackground.setSelected(false) ;
		optionMenu.add((relativechange = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsRelative")))) ;
		relativechange.addItemListener(this) ;
		relativechange.setSelected(true) ;
		optionMenu.addSeparator() ;
		optionMenu.add((hsblinked = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsLinkHSB")))) ;
		hsblinked.addItemListener(this) ;
		hsblinked.setSelected(true) ;
		optionMenu.add((rgblinked = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsLinkRGB")))) ;
		rgblinked.addItemListener(this) ;
		rgblinked.setSelected(true) ;
      
      // Window menu.

		windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
		windowMenu.setMargin(insets) ;
		mb.add(windowMenu);
		windowMenu.add((ppwindow = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuWindowPalette")))) ;
		ppwindow.addItemListener(this) ;
		ppwindow.setSelected(true) ;
		windowMenu.add((ccwindow = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuWindowColor")))) ;
		ccwindow.addItemListener(this) ;
		ccwindow.setSelected(true) ;
		windowMenu.add((icpwindow = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuWindowTint")))) ;
		icpwindow.addItemListener(this) ;
		icpwindow.setSelected(true) ;
		windowMenu.add((previewwindow = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuWindowPreview")))) ;
		previewwindow.addItemListener(this) ;
		previewwindow.setSelected(true) ;
		windowMenu.add((selectwindow = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuWindowSelection")))) ;
		selectwindow.addItemListener(this) ;
		selectwindow.setSelected(true) ;

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
		NEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipNew")) ;
		OPEN = new JButton() ;
		iconfile = Kisekae.getResource("Images/open" + ext) ;
		if (iconfile != null) OPEN.setIcon(new ImageIcon(iconfile)) ;
		OPEN.setMargin(new Insets(1,1,1,1)) ;
		OPEN.setAlignmentY(0.5f) ;
		OPEN.addActionListener(this) ;
		OPEN.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpen"));
		CLOSE = new JButton() ;
		iconfile = Kisekae.getResource("Images/close" + ext) ;
		if (iconfile != null) CLOSE.setIcon(new ImageIcon(iconfile)) ;
		CLOSE.setMargin(new Insets(1,1,1,1)) ;
		CLOSE.setAlignmentY(0.5f) ;
		CLOSE.addActionListener(this) ;
		CLOSE.setToolTipText(Kisekae.getCaptions().getString("ToolTipCloseFile")) ;
		SAVE = new JButton() ;
		iconfile = Kisekae.getResource("Images/save" + ext) ;
		if (iconfile != null) SAVE.setIcon(new ImageIcon(iconfile)) ;
		SAVE.setMargin(new Insets(1,1,1,1)) ;
		SAVE.setAlignmentY(0.5f) ;
		SAVE.addActionListener(this) ;
		SAVE.setToolTipText(Kisekae.getCaptions().getString("ToolTipSave"));
		SAVE.setEnabled((palette != null && palette.getName() != null) && !Kisekae.isSecure() && !Kisekae.isExpired());
		toolbar.add(NEW, null);
		toolbar.add(OPEN, null);
		toolbar.add(CLOSE, null);
		toolbar.add(SAVE, null);

		// Create the edit operations.

		CUT = new JButton() ;
		iconfile = Kisekae.getResource("Images/cut" + ext) ;
		if (iconfile != null) CUT.setIcon(new ImageIcon(iconfile)) ;
		CUT.setMargin(new Insets(1,1,1,1)) ;
		CUT.setAlignmentY(0.5f) ;
		CUT.addActionListener(this) ;
		CUT.setEnabled(false) ;
		CUT.setToolTipText(Kisekae.getCaptions().getString("ToolTipCut"));
		COPY = new JButton() ;
		iconfile = Kisekae.getResource("Images/copy" + ext) ;
		if (iconfile != null) COPY.setIcon(new ImageIcon(iconfile)) ;
		COPY.setMargin(new Insets(1,1,1,1)) ;
		COPY.setAlignmentY(0.5f) ;
		COPY.addActionListener(this) ;
		COPY.setEnabled(false) ;
		COPY.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopy"));
		PASTE = new JButton() ;
		iconfile = Kisekae.getResource("Images/paste" + ext) ;
		if (iconfile != null) PASTE.setIcon(new ImageIcon(iconfile)) ;
		PASTE.setMargin(new Insets(1,1,1,1)) ;
		PASTE.setAlignmentY(0.5f) ;
		PASTE.addActionListener(this) ;
		PASTE.setEnabled(false) ;
		PASTE.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaste"));
		toolbar.addSeparator() ;
		toolbar.add(CUT, null);
		toolbar.add(COPY, null);
		toolbar.add(PASTE, null);
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

		// Add our hue selection controls.

		HUE = new JButton() ;
      hueListener = new SelectListener(HUE,"HSB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/hue" + ext) ;
		if (iconfile != null) HUE.setIcon(new ImageIcon(iconfile)) ;
		HUE.setMargin(new Insets(1,1,1,1)) ;
		HUE.setAlignmentY(0.5f) ;
		HUE.addMouseListener(hueListener) ;
      String s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + hueListener.getSensitivity() + s1.substring(j1+1) ;
		HUE.setToolTipText(s1);
		toolbar.addSeparator() ;
		toolbar.add(HUE, null) ;

		// Add our saturation selection controls.

		SATURATION = new JButton() ;
      satListener = new SelectListener(SATURATION,"HSB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/sat" + ext) ;
		if (iconfile != null) SATURATION.setIcon(new ImageIcon(iconfile)) ;
		SATURATION.setMargin(new Insets(1,1,1,1)) ;
		SATURATION.setAlignmentY(0.5f) ;
		SATURATION.addMouseListener(satListener) ;
      s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + satListener.getSensitivity() + s1.substring(j1+1) ;
		SATURATION.setToolTipText(s1);
		toolbar.add(SATURATION, null) ;

		// Add our brightness selection controls.

		BRIGHTNESS = new JButton() ;
      briListener = new SelectListener(BRIGHTNESS,"HSB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/bri" + ext) ;
		if (iconfile != null) BRIGHTNESS.setIcon(new ImageIcon(iconfile)) ;
		BRIGHTNESS.setMargin(new Insets(1,1,1,1)) ;
		BRIGHTNESS.setAlignmentY(0.5f) ;
		BRIGHTNESS.addMouseListener(briListener) ;
      s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + briListener.getSensitivity() + s1.substring(j1+1) ;
		BRIGHTNESS.setToolTipText(s1);
		toolbar.add(BRIGHTNESS, null) ;

		// Add our hue selection controls.

		RED = new JButton() ;
      redListener = new SelectListener(RED,"RGB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/redsel" + ext) ;
		if (iconfile != null) RED.setIcon(new ImageIcon(iconfile)) ;
		RED.setMargin(new Insets(1,1,1,1)) ;
		RED.setAlignmentY(0.5f) ;
		RED.addMouseListener(redListener) ;
      s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + redListener.getSensitivity() + s1.substring(j1+1) ;
		RED.setToolTipText(s1);
		toolbar.addSeparator() ;
		toolbar.add(RED, null) ;

		// Add our saturation selection controls.

		GREEN = new JButton() ;
      grnListener = new SelectListener(GREEN,"RGB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/grnsel" + ext) ;
		if (iconfile != null) GREEN.setIcon(new ImageIcon(iconfile)) ;
		GREEN.setMargin(new Insets(1,1,1,1)) ;
		GREEN.setAlignmentY(0.5f) ;
		GREEN.addMouseListener(grnListener) ;
      s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + grnListener.getSensitivity() + s1.substring(j1+1) ;
		GREEN.setToolTipText(s1);
		toolbar.add(GREEN, null) ;

		// Add our brightness selection controls.

		BLUE = new JButton() ;
      bluListener = new SelectListener(BLUE,"RGB",initsensitivity) ;
		iconfile = Kisekae.getResource("Images/blusel" + ext) ;
		if (iconfile != null) BLUE.setIcon(new ImageIcon(iconfile)) ;
		BLUE.setMargin(new Insets(1,1,1,1)) ;
		BLUE.setAlignmentY(0.5f) ;
		BLUE.addMouseListener(bluListener) ;
      s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
      i1 = s1.indexOf('[') ;
      j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + bluListener.getSensitivity() + s1.substring(j1+1) ;
		BLUE.setToolTipText(s1);
		toolbar.add(BLUE, null) ;

		// Create a image popdown selection box.  The contents are established
		// when we set the display values.

		FRAMES = new JComboBox() ;
		FRAMES.setAlignmentY(0.5f) ;
		FRAMES.setEditable(false) ;
		FRAMES.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageSelection"));
		FRAMES.setPreferredSize(new Dimension(200, 21));
		FRAMES.addItemListener(frameListener) ;
      framelabel.setText(Kisekae.getCaptions().getString("ImageText"));

		// Create the palettes popdown selection box.  This contains all objects
      // in the configuration that are palettes or have internal palettes.
      // The contents are established when we set the display values.

		PALETTES = new JComboBox() ;
		PALETTES.setAlignmentY(0.5f) ;
		PALETTES.setEditable(false) ;
		PALETTES.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteSelection"));
		PALETTES.setPreferredSize(new Dimension(200, 21));
		PALETTES.addItemListener(fileListener) ;
      palettelabel.setText(Kisekae.getCaptions().getString("PaletteText"));

		// Create the multipalette selection box.  The contents are established
		// when we set the display values.

		MULTIPALETTES = new JComboBox() ;
		MULTIPALETTES.setAlignmentY(0.5f) ;
		MULTIPALETTES.setEditable(false) ;
		MULTIPALETTES.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteGroupSelection"));
		MULTIPALETTES.setPreferredSize(new Dimension(200, 21));
		MULTIPALETTES.addItemListener(multiListener) ;
      multilabel.setText(Kisekae.getCaptions().getString("PaletteGroupText"));

		// Create the color fields.

		COLORTEXT = new JLabel(Kisekae.getCaptions().getString("ColorsText")) ;
		toolbar.addSeparator() ;
		toolbar.add(COLORTEXT, null) ;
		COLORPANEL = new JPanel() ;
		COLORPANEL.setPreferredSize(new Dimension(20,20)) ;
		COLORPANEL.setMaximumSize(COLORPANEL.getPreferredSize()) ;
		COLORPANEL.setBorder(BorderFactory.createRaisedBevelBorder()) ;
		COLORPANEL.setAlignmentY(0.5f) ;
		toolbar.addSeparator() ;
		toolbar.add(COLORPANEL, null) ;
		HSBTEXT = new JLabel("HSB (0,0,0)") ;
		RGBTEXT = new JLabel("RGB (0,0,0)") ;
		toolbar.addSeparator() ;
		toolbar.add(HSBTEXT, null) ;
		toolbar.addSeparator() ;
		toolbar.add(RGBTEXT, null) ;

		// Create the default page format.

      if (Kisekae.isPrintInstalled())
      {
			PrinterJob prn = null ;
			try { prn = PrinterJob.getPrinterJob() ; }
			catch (Exception e) { }
			pageformat = (prn != null) ? prn.defaultPage() : null ;
      }

		// Create the palette edit panel.

		int n = (palette == null) ? 0 : palette.getMultiPaletteCount() - 1 ;
		if (multipalette == null)
			multipalette = new Integer(0) ;
		if (multipalette.intValue() > n)
			multipalette = new Integer(0) ;
		pp = new PalettePanel(palette,multipalette) ;
      pp.setBorder(eb3) ;

		// Create the preview image panel.

		preview = new PreviewPanel(this) ;
		Border rbb = BorderFactory.createRaisedBevelBorder() ;
		CompoundBorder cb1 = new CompoundBorder(eb3,rbb) ;
		preview.setBorder(cb1) ;

		// Create the color transform panel.

		icp = new ImageColorPanel(this) ;
      icp.setPreview(preview) ;

		// Create the color chooser panel.

		n = (palette == null) ? 0 : palette.getColorCount() ;
		cc = new JColorChooser() ;
		cc.setPreviewPanel(new JPanel()) ;
		Dimension ccsize = cc.getPreferredSize() ;
		cc.setMaximumSize(ccsize);
		cc.getSelectionModel().addChangeListener(pp) ;
		Dimension ppsize = pp.getPreferredSize() ;
		ppsize.width = ccsize.width ;
		pp.setMaximumSize(ppsize);
		undoredo = true ;
		pp.setColor(0) ;
		undoredo = false ;
      
      // Create the HSB and RGB sliders.
      
      hsbslider = new JSlider(JSlider.HORIZONTAL,0,100,initsensitivity) ;
      rgbslider = new JSlider(JSlider.HORIZONTAL,0,100,initsensitivity) ;
      hsbslider.addChangeListener(hsbSliderListener) ;
      rgbslider.addChangeListener(rgbSliderListener) ;
      hsbslider.setMajorTickSpacing(20) ;
      hsbslider.setMinorTickSpacing(5) ;
      hsbslider.setPaintTicks(true) ;
      hsbslider.setPaintLabels(true) ;
      rgbslider.setMajorTickSpacing(20) ;
      rgbslider.setMinorTickSpacing(5) ;
      rgbslider.setPaintTicks(true) ;
      rgbslider.setPaintLabels(true) ;

		// Create the user interface.

		Container c = getContentPane() ;
		panel1 = new JPanel() ;
		panel1.setLayout(new BorderLayout()) ;
		panel1.setBorder(eb1) ;
		c.add(toolbar,BorderLayout.NORTH);
		c.add(panel1,BorderLayout.CENTER);
		panel2 = new JPanel() ;
		panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS)) ;
		panel2.add(Box.createVerticalGlue()) ;
		panel2.add(cc,null) ;
		panel2.add(Box.createVerticalGlue()) ;
		panel2.add(pp,null) ;
		panel2.add(Box.createVerticalGlue()) ;
		panel2.add(icp,null) ;
		panel2.add(Box.createVerticalGlue()) ;
		panel1.add(panel2,BorderLayout.WEST) ;
		panel3 = new JPanel() ;
		panel3.setLayout(new BorderLayout()) ;
		panel3.setBorder(eb2) ;
		panel7 = new JPanel() ;
		panel7.setLayout(new GridBagLayout()) ;
		panel7.setBorder(eb4) ;
		panel7.add(palettelabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		panel7.add(PALETTES, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel7.add(framelabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		panel7.add(FRAMES, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel7.add(multilabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
		panel7.add(MULTIPALETTES, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel6.setLayout(new GridBagLayout()) ;
		panel6.add(new JLabel("HSB"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel6.add(hsbslider, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel6.add(new JLabel("RGB"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel6.add(rgbslider, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel4.setLayout(new BorderLayout()) ;
		panel4.setBorder(eb5) ;
		panel4.add(panel6,BorderLayout.WEST) ; 
		panel4.add(panel7,BorderLayout.CENTER) ; 
		panel3.add(panel4,BorderLayout.NORTH) ;
      panel5 = new JPanel() ;
		panel5.setLayout(new BorderLayout()) ;
		panel5.add(preview.getToolBar(),BorderLayout.NORTH) ;
		panel5.add(preview,BorderLayout.CENTER) ;
      panel3.add(panel5,BorderLayout.CENTER) ;
		panel1.add(panel3,BorderLayout.CENTER) ;

		// Size the frame for the window space.

      super.open() ;
		doLayout() ;
		validate() ;
      if (helper != null) helper.setSize(getSize());

		// Set up the window event listeners.

		addWindowListener(this) ;
		addComponentListener(this) ;
      addKeyListener(keyListener) ;
		setValues() ;
		preview.init(palette,cel,multipalette) ;
      icp.setImage(preview.getImage()) ;
		if (palette != null)	palette.startEdits() ;
	}


	// A utility method to create the edit menu.

	private JMenu createEditMenu()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuEdit")) ;
		if (!applemac) menu.setMnemonic(KeyEvent.VK_E) ;

		// Undo and redo are actions of our own creation.

		undoAction = new UndoAction() ;
		JMenuItem undo = menu.add(undoAction) ;
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelerator));
      if (!applemac) undo.setMnemonic(KeyEvent.VK_U) ;
		redoAction = new RedoAction() ;
		JMenuItem redo = menu.add(redoAction) ;
      redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelerator));
      if (!applemac) redo.setMnemonic(KeyEvent.VK_R) ;
		menu.add((reload = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUndoAll")))) ;
		reload.setEnabled(false) ;
		reload.addActionListener(this);
		menu.addSeparator() ;

		// These actions come from the default editor kit, but we renamed
		// them.  Get the ones we want and stick them in the menu.

		menu.add((cut = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCut")))) ;
		if (!applemac) cut.setMnemonic(KeyEvent.VK_T) ;
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelerator));
		cut.addActionListener(this);
		menu.add((copy = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCopy")))) ;
		if (!applemac) copy.setMnemonic(KeyEvent.VK_C) ;
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelerator));
		copy.addActionListener(this);
		menu.add((paste = new JMenuItem(Kisekae.getCaptions().getString("MenuEditPaste")))) ;
		paste.setEnabled(false) ;
		if (!applemac) paste.setMnemonic(KeyEvent.VK_P) ;
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, accelerator));
		paste.addActionListener(this);
		menu.addSeparator() ;
		menu.add((selectall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectAll")))) ;
		if (!applemac) selectall.setMnemonic(KeyEvent.VK_L) ;
		selectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator));
		selectall.addActionListener(this) ;
		menu.add((selectclip = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectCopy")))) ;
		selectclip.setEnabled(false) ;
		selectclip.addActionListener(this) ;
		menu.add((selecthue = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectHSB")))) ;
		selecthue.addActionListener(this) ;
		menu.add((selectrgb = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectRGB")))) ;
		selectrgb.addActionListener(this) ;
		menu.addSeparator() ;
		menu.add((setsize = new JMenuItem(Kisekae.getCaptions().getString("MenuEditColorCount")))) ;
		setsize.addActionListener(this) ;
		menu.add((settransparent = new ColorMenu(Kisekae.getCaptions().getString("MenuEditTransparent")))) ;
		settransparent.addActionListener(this) ;
		menu.add((setbackground = new ColorMenu(Kisekae.getCaptions().getString("MenuEditBackground")))) ;
		setbackground.addActionListener(this) ;
		menu.addSeparator() ;
		menu.add((deletemp = new JMenuItem(Kisekae.getCaptions().getString("MenuEditDeletePaletteGroup")))) ;
		if (!applemac) deletemp.setMnemonic(KeyEvent.VK_DELETE) ;
		deletemp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, accelerator));
		deletemp.addActionListener(this) ;
		menu.add((insertmp = new JMenuItem(Kisekae.getCaptions().getString("MenuEditInsertPaletteGroup")))) ;
		if (!applemac) insertmp.setMnemonic(KeyEvent.VK_INSERT) ;
		insertmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, accelerator));
		insertmp.addActionListener(this) ;
		menu.add((addmp = new JMenuItem(Kisekae.getCaptions().getString("MenuEditAddPaletteGroup")))) ;
		if (!applemac) addmp.setMnemonic(KeyEvent.VK_END) ;
		addmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, ActionEvent.SHIFT_MASK+accelerator));
		addmp.addActionListener(this) ;
		return menu;
	}


	// Method to set the dialog field values.  This method sets the frame
   // title, populates the MULTIPALETTES selection box, and adjusts the
   // menu item state to reflect the current context.  It also initializes
   // the preview panel.

	private void setValues()
	{
		boolean includedefault = false ;		// If true, include all cels option

		// Update the user interface.  Loaded files (those with names and
		// not newly created) can be saved.  Palette files can create
		// new multipalette entries.

		setTitle() ;
		update.setEnabled(false) ;
      importp.setEnabled(palette != null && !Kisekae.isSecure()) ;
      exportp.setEnabled(palette != null && !Kisekae.isSecure()) ;
      mergep.setEnabled(palette != null && !Kisekae.isSecure()) ;
      properties.setEnabled(palette != null || cel != null) ;
      paletteproperties.setEnabled(palette != null) ;
      celproperties.setEnabled(cel != null) ;
		close.setEnabled(palette != null || cel != null) ;
		CLOSE.setEnabled(palette != null || cel != null) ;
		saveas.setEnabled((palette != null || cel != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		save.setEnabled((palette != null || cel != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		SAVE.setEnabled((palette != null || cel != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		newmulti.setEnabled(palette != null && !palette.isInternal()) ;
		deletemp.setEnabled(palette != null && !palette.isInternal()) ;
		insertmp.setEnabled(palette != null && !palette.isInternal()) ;
		addmp.setEnabled(palette != null && !palette.isInternal()) ;
		cut.setEnabled(pp != null && pp.hasSelection()) ;
		CUT.setEnabled(pp != null && pp.hasSelection()) ;
		copy.setEnabled(pp != null && pp.hasSelection()) ;
		COPY.setEnabled(pp != null && pp.hasSelection()) ;
		selectall.setEnabled(palette != null) ;
		selecthue.setEnabled(palette != null) ;
		selectrgb.setEnabled(palette != null) ;
		setsize.setEnabled(palette != null || cel != null) ;
		settransparent.setEnabled(palette != null || cel != null) ;
		setbackground.setEnabled(palette != null || cel != null) ;
		reload.setEnabled(palette != null && undo.canUndo() && changed) ;
		int n = (palette == null) ? 0 : palette.getColorCount() ;
      String s1 = Kisekae.getCaptions().getString("ColorsText") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + n + s1.substring(j1+1) ;
		COLORTEXT.setText(s1) ;
      ppwindow.setState(palette != null) ;
      icp.setEnabled(cel != null) ;
      showLabels() ;

		// Populate the frames popdown selection box with the possible
		// options for this palette.  GIF cels show animation frames in
		// the frame popdown list.  KiSS palettes show cels in the list.

		frame = 0 ;
		Vector v = new Vector() ;
      Object selectobject = null ;
		FRAMES.removeItemListener(frameListener) ;

      // If our display object is a KiSS palette and we have an active
		// configuration, show only those cels referencing the palette
      // in the FRAMES selection list.  Internal cels are not listed.

      if (palette != null)
      {
         if (config != null)
			{
				includedefault = !palette.isInternal() ;
				Vector cels = config.getCels() ;
				for (int i = cels.size()-1 ; i >= 0 ; i--)
				{
					Cel c = (Cel) cels.elementAt(i) ;
               if (c.isInternal()) continue ;
					Object pid = c.getPaletteID() ;
					if (pid != null && pid.equals(palette.getIdentifier()))
               {
		         	v.addElement(c) ;
						if (cel == null) continue ;
	               if (c.getName().equals(cel.getName()))
                  {
                  	Object mpid1 = c.getPaletteGroupID() ;
                     Object mpid2 = cel.getPaletteGroupID() ;
                     if (mpid1 == null || mpid2 == null || mpid1.equals(mpid2))
		                  selectobject = c ;
                  }
               }
            }
            
            // If we didn't find a cel but we have one with an internal palette,
            // then we should use it.
            
            if (v.size() == 0 && cel != null)
            {
               v.addElement(cel) ;
               selectobject = cel ;
            }

				// If our palette is internal and we have an actual cel
				// then default to this cel for display purposes.  The cel
				// is cloned as we adjust its fixed placement attributes.

//				if (cel == null && palette.isInternal() && v.size() > 0)
				if (cel == null && v.size() == 1)
            {
            	cel = (Cel) v.elementAt(0) ;
					selectobject = cel ;
               basecel = cel ;
					cel = (Cel) basecel.clone() ;
					setAtOrigin(cel) ;
               setsize.setEnabled(true) ;
               icp.setEnabled(true) ;
            }
         }

	      // If our display object is a KiSS palette and we do not have
	      // an active configuration, show all cels of the proper type
         // in the FRAMES selection list.

         else
         {
				ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;
				if (zip != null)
				{
					Enumeration enum1 = zip.entries() ;
					while (enum1 != null && enum1.hasMoreElements())
					{
						ArchiveEntry ae = (ArchiveEntry) enum1.nextElement() ;
						if (!palette.isInternal() && ae.isCel()) v.addElement(ae) ;
                  if (cel == null) continue ;
                  if (cel.getPath().equals(ae.getPath()))
                  {
	                  if (palette.isInternal()) v.addElement(ae) ;
                  	selectobject = ae ;
                  }
               }
				}

            // If we have a palette without a zip file show just the cel.

            else if (cel != null)
            {
            	v.addElement(cel) ;
               selectobject = cel ;
            }
			}
      }

      // If our display object is a cel without a palette show the cel
		// name in the FRAMES selection list.

      else
      {
         if (cel != null)
         {
	      	v.addElement(cel) ;
	         selectobject = cel ;
         }
      }

      // If our display object is a GIF cel, show the GIF frames in the
      // FRAMES selection list.

		if (cel instanceof GifCel)
		{
         if (cel.getFrameCount() > 1)
         {
	      	v.removeAllElements() ;
				for (int i = 0 ; i < cel.getFrameCount() ; i++)
            {
               s1 = Kisekae.getCaptions().getString("ImageFrameText") ;
               i1 = s1.indexOf('[') ;
               j1 = s1.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s1 = s1.substring(0,i1) + i + s1.substring(j1+1) ;
               v.addElement(s1) ;
            }
            if (initframe < 0 || initframe >= v.size()) initframe = 0 ;
	         if (initframe < v.size()) selectobject = v.elementAt(initframe) ;
            cel.setFrame(initframe) ;
            palette = cel.getPalette() ;
            frame = initframe ;
         }
		}

		if (!(cel instanceof GifCel)) Collections.sort(v) ;
		if (includedefault) 
      {
         Object o = Kisekae.getCaptions().getString("ImageAllCelsText") ;
         v.insertElementAt(o,0) ;
         if (selectobject == null) selectobject = o ;
      }
      FRAMES.setModel(new DefaultComboBoxModel(v)) ;
     	FRAMES.setSelectedItem(selectobject);
		FRAMES.addItemListener(frameListener) ;

		// Populate the palette file popdown selection box with the possible
		// palette files from our configuration or archive file.  The entries
      // will contain all KiSS palette objects plus all cel objects that use
      // internal palettes.

		v = new Vector() ;
		selectobject = null ;
		PALETTES.removeItemListener(fileListener) ;
		if (config != null)
		{
			selectobject = palette ;
			v = config.getPalettes() ;
         Vector cels = config.getCels() ;
			if (v != null) v = (Vector) v.clone() ; else v = new Vector() ;
         for (int i = 0 ; i < cels.size() ; i++)
         {
         	Cel c = (Cel) cels.elementAt(i) ;
				if (c == null) continue ;
				if (c.isCopy()) continue ;
            Palette p = c.getPalette() ;
            if (p == null) continue ;
            if (p.isInternal()) v.add(p) ;
         }
         if (selectobject != null && !v.contains(selectobject))
            v.addElement(selectobject) ;
		}
		else
		{
			ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;
			if (zip != null)
			{
				Enumeration enum1 = zip.entries() ;
				while (enum1 != null && enum1.hasMoreElements())
				{
					ArchiveEntry ae = (ArchiveEntry) enum1.nextElement() ;
					if (ae.hasPalette()) v.addElement(ae) ;
               if (palette != null && (palette.getPath().equals(ae.getPath())))
						selectobject = ae ;
				}
			}

         // If we have a palette without a zip file show just the palette.

         else if (palette != null)
         {
           	v.addElement(palette) ;
            selectobject = palette ;
         }
		}
      Collections.sort(v) ;
      PALETTES.setModel(new DefaultComboBoxModel(v));
		PALETTES.setSelectedItem(selectobject) ;
		PALETTES.addItemListener(fileListener) ;

		// Populate the multipalette popdown selection box with the possible
		// options for this palette.

      n = (palette == null) ? 0 : palette.getMultiPaletteCount() ;
		MULTIPALETTES.removeItemListener(multiListener) ;
		if (MULTIPALETTES.getItemCount() > 0) MULTIPALETTES.removeAllItems() ;
		for (int i = 0 ; i < n ; i++) MULTIPALETTES.addItem(new Integer(i));
		if (multipalette.intValue() > n-1) multipalette = new Integer(0) ;
		MULTIPALETTES.setSelectedItem(multipalette) ;
		MULTIPALETTES.addItemListener(multiListener) ;

		// Update the user interface and the preview panel.

		updateColorMenu(multipalette) ;
		panel2.setVisible((palette != null || cel != null) && ccwindow.getState()) ;
		panel4.setVisible(selectwindow.getState()) ;
//		preview.init(palette,cel,multipalette) ;
	}


	// This method sets the frame title based upon the active palette
   // and cel object.

	private void setTitle()
	{
		String s = Kisekae.getCaptions().getString("ColorEditorTitle") ;
		String name = (palette == null) ? null : palette.getName() ;
		if (name != null) s += " - " + name ; else name = "" ;
      String celname = (cel == null) ? name : cel.getName() ;
      if (!(name.equalsIgnoreCase(celname))) s += " [" + celname + "]" ;
		ArchiveFile zip = (palette == null) ? null : palette.getZipFile() ;
		if (zip == null && cel != null) zip = cel.getZipFile() ;
		if (zip != null) s += " (" + zip.getFileName() + ")" ;
		setTitle(s) ;
	}


	// This method sets the user selection labels visible if we have sufficient
   // space in the panel.

	private void showLabels()
	{
      boolean showlabels = true ;
      Dimension d = panel4.getSize() ;
      Insets insets = panel4.getInsets() ;
      d.width -= (insets.left + insets.right) ;
      Dimension ls1 = palettelabel.getPreferredSize() ;
      Dimension ls2 = framelabel.getPreferredSize() ;
      Dimension ls3 = multilabel.getPreferredSize() ;
      int labelwidth = Math.max(ls1.width,Math.max(ls2.width,ls3.width)) ;
      Dimension fs1 = PALETTES.getPreferredSize() ;
      Dimension fs2 = FRAMES.getPreferredSize() ;
      Dimension fs3 = MULTIPALETTES.getPreferredSize() ;
      int fieldwidth = Math.max(fs1.width,Math.max(fs2.width,fs3.width)) ;
      if (labelwidth + fieldwidth > d.width) showlabels = false ;
      palettelabel.setVisible(showlabels) ;
      framelabel.setVisible(showlabels) ;
      multilabel.setVisible(showlabels) ;
   }



	// A utility function to set the active color.

	void setActiveColor(Color c)
	{
		if (pp == null) return ;
		pp.setColor(c) ;
	}



	// A utility function to set the current palette object.

	void setPalette(Palette p) { palette = p ; }

   

	// Method to return a reference to our parent zip file object.

	ArchiveFile getZipFile() { return (ze != null) ? ze.getZipFile() : null ; }

	// Method to return a reference to our zip entry object.

	ArchiveEntry getZipEntry() { return ze ; }



	// A utility method to return a new palette object of the specified size.
	// This palette contains opaque colors and includes white and black.
	// The first 8 colors are greyscale.  The next 8 colors are standard
	// colors. The remaining colors span the spectrum.

	private Palette createpalette(int n)
	{
		Palette p = new Palette() ;
      byte [] a = new byte[n] ;
		byte [] r = new byte[n] ;
		byte [] g = new byte[n] ;
		byte [] b = new byte[n] ;

		// Span the color spectrum for the colors.

		for (int i = 0 ; i < n ; i++)
		{
			Color c = ColorMenu.getDefaultColor(i,n) ;
			a[i] = (byte) 0xff ;
			r[i] = (byte) c.getRed() ;
			g[i] = (byte) c.getGreen() ;
			b[i] = (byte) c.getBlue() ;
		}

		// Update the palette object.

		p.setPalette(a,r,g,b) ;
		p.setUpdated(true) ;
      entrystate = false ;
		return p ;
	}


	// A utility method to initialize for a new palette file.  This method is
   // invoked after we have opened and loaded a new palette object or created
   // a new palette object.  It populates the PALETTE selection box and
   // initializes for a new palette context.

	private void initpalette()
	{
		config = null ;
		changed = false ;
		memorysource = false ;
		multipalette = new Integer(0) ;
		initmultipalette = multipalette ;
		if (palette != null) palette.startEdits() ;
		updateInterface(multipalette) ;
      if (preview != null) preview.init(palette,cel,multipalette) ;
      icp.setImage(preview.getImage()) ;
      icp.reset() ;
	}


   // A utility function to establish a new cel and palette object given
   // an archive entry.  We return either a cel object or a palette object.

	private KissObject createCelPalette(FileOpen fd, ArchiveEntry ze)
   {
		ArchiveEntry pe = ze ;
		Palette newpalette = null ;
		if (ze == null) return null ;

		// Load the cel object.  The load will fail if we have a
      // palette type KiSS cel.

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
		newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
      if (newcel != null)
      {
			newcel.load() ;
         setAtOrigin(newcel) ;
			newpalette = newcel.getPalette() ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         if (newcel.isError())
         {
	         newcel = null ;
	         newpalette = null ;
         }
         else
            return newcel ;
		}


		// If we are a cel file then identify the associated palette.

		if (".cel".equals(ze.getExtension()) && fd != null)
		{
			String [] ext = ArchiveFile.getPaletteExt() ;
			pe = fd.showConfig(me,Kisekae.getCaptions().getString("SelectCelPalette"),ext) ;
			if (pe == null)
         {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         	return null ;
         }
		}

		// Load the palette object.

		if (pe != null && ArchiveFile.isPalette(pe.getExtension()))
		{
			newpalette = new Palette(pe.getZipFile(),pe.getPath()) ;
			newpalette.setIdentifier(new Integer(-1)) ;
			newpalette.clearTable(null) ;
			newpalette.setKey(Palette.getKeyTable(),null,newpalette.getIdentifier()) ;
			newpalette.setLoader(me) ;
			newpalette.load() ;
			newpalette.setLoader(null) ;
		}

		// Load the cel object.

		Cel newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
      if (newcel != null)
      {
			newcel.setLoader(me) ;
         newcel.setPalette(newpalette) ;
			newcel.load() ;
         setAtOrigin(newcel) ;
			newpalette = newcel.getPalette() ;
			newcel.setLoader(null) ;
		}

		// Return the cel or palette.

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		if (newcel != null && newcel.isError()) return null ;
		if (newpalette != null && newpalette.isError()) return null ;
		if (newcel != null) return newcel ;
      return newpalette ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
			// New 16 or 256 color palette file request.  Because this is a
         // new file we signify that its contents have changed so that a
         // prompt to save the file will be issued on close.

			if (source == newfile16 || source == newfile256 || source == NEW)
			{
				if (!closecheck(true)) return ;
				int n = (source == newfile256) ? 256 : 16 ;
				closepalette() ;
				palette = createpalette(n) ;
				initpalette() ;
				changed = true ;
				return ;
			}

			// New multipalette file request.   This creates a new multipalette
			// in the current palette.  It uses the default multipalette for the
         // new multipalette colors.

			if (source == newmulti)
			{
				if (palette == null) return ;
				int n = palette.getColorCount() ;
				Object [] olddata = palette.getPaletteData() ;
				int oldmp = palette.getMultiPaletteCount() ;
				int oldbackground = palette.getBackgroundIndex() ;
				int oldtransparent = palette.getTransparentIndex() ;
				Palette p = createpalette(n) ;
				Object [] o = p.getMultiPaletteData(new Integer(0)) ;
				multipalette = new Integer(palette.getMultiPaletteCount()) ;
				palette.addMultiPalette((byte []) o[0], (byte []) o[1], (byte []) o[2], (byte []) o[3]) ;
				changed = true ;

            // Save the edit for undo processing.

            UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
					palette,multipalette,olddata,oldmp,n,oldbackground,oldtransparent,
					palette.getPaletteData(),palette.getMultiPaletteCount(),
					palette.getColorCount(),palette.getBackgroundIndex(),
					palette.getTransparentIndex()) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Update the user interface and the preview panel.

				if (memorysource) update.setEnabled(true) ;
            updateInterface(multipalette) ;
				return ;
			}

			// Open file request.

			if (source == open || source == OPEN)
			{
            Vector v = new Vector() ;
				String [] cext = ArchiveFile.getImageExt() ;
            String [] pext = ArchiveFile.getPaletteExt() ;
            for (int i = 0 ; i < cext.length ; i++) v.addElement(cext[i]) ;
            for (int i = 0 ; i < pext.length ; i++) v.addElement(pext[i]) ;
				FileOpen fd = new FileOpen(this,Kisekae.getCaptions().getString("PaletteListTitle"),v.toArray()) ;
				fd.show() ;
				ArchiveEntry zenew = fd.getZipEntry() ;
				if (zenew == null) { fd.close() ; return ; }

				// Confirm that we selected a valid file.

            String s = zenew.getExtension().toUpperCase() ;
            if (!(v.contains(s)))
				{
					String name = zenew.getName() ;
               s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + name + s.substring(j1) ;
               s += "\n" + Kisekae.getCaptions().getString("SaveAsPaletteImageText") ;
               JOptionPane.showMessageDialog(this, s,
                  Kisekae.getCaptions().getString("FileOpenException"),
                  JOptionPane.ERROR_MESSAGE) ;
					fd.close() ;
					return ;
				}

            // Save any previously updated palette file.

				if (!closecheck(true)) { fd.close() ; return ; }

				// Initialize for this palette.

				KissObject kiss = createCelPalette(fd,zenew) ;
				fd.close() ;
				closepalette() ;
            newcel = (kiss instanceof Cel) ? (Cel) kiss : null ;
				newpalette = (kiss instanceof Palette) ? (Palette) kiss
            	: (newcel != null) ? newcel.getPalette() : null ;
            ze = zenew ;
				cel = newcel ;
				palette = newpalette ;
				initpalette() ;
				return ;
			}

			// Multipalette add/insert/delete request.   This modifies the
         // multipalettes in the current palette.  It inserts black colors
         // for new multipalette colors.

			if (source == insertmp || source == deletemp || source == addmp)
			{
				if (palette == null) return ;
				int n = palette.getColorCount() ;
            Integer oldmultipalette = new Integer(multipalette.intValue()) ;
				Object [] olddata = palette.getPaletteData() ;
				int oldmp = palette.getMultiPaletteCount() ;
				int oldbackground = palette.getBackgroundIndex() ;
				int oldtransparent = palette.getTransparentIndex() ;
            if (source == addmp)
            {
            	multipalette = new Integer(palette.getMultiPaletteCount()) ;
					palette.addMultiPalette(multipalette) ;
            }
            if (source == insertmp)
					palette.addMultiPalette(multipalette) ;
				if (source == deletemp)
				{
					palette.deleteMultiPalette(multipalette) ;
					int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
					int mpcount = palette.getMultiPaletteCount() ;
					if (mp >= mpcount) mp = mpcount - 1 ;
					if (mp < 0) mp = 0 ;
					multipalette = new Integer(mp) ;
				}
				changed = true ;

            // Save the edit for undo processing.

            UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
					palette,oldmultipalette,olddata,oldmp,n,oldbackground,oldtransparent,
					palette.getPaletteData(),palette.getMultiPaletteCount(),
					palette.getColorCount(),palette.getBackgroundIndex(),
					palette.getTransparentIndex()) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Update the user interface and the preview panel.

				if (memorysource) update.setEnabled(true) ;
            updateInterface(multipalette) ;
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

			// Exit request.  Save the document first if necessary.

			if (source == exit)
			{
				if (!closecheck(true)) return ;
				close() ;
				return ;
			}

			// Close request.  Save the document first if necessary.

			if (source == close || source == CLOSE)
			{
				if (!closecheck(true)) return ;
				closepalette() ;
				palette = null ;
				initpalette() ;
				return ;
			}

			// Save or Save As request.  Save the element.  If the palette is
         // internal then a cel object is saved, otherwise we save a KiSS
         // palette.

			if (source == save || source == saveas || source == SAVE)
			{
            if (icp.isChanged()) icp.apply() ;
         	originalpalettename = (palette == null) ? null : palette.getName() ;
            originalcelname = (cel == null) ? null : cel.getName() ;
            originalzepath = (ze == null) ? null : ze.getPath() ;
				savepalette((source == saveas)) ;
				return ;
			}

			// Cut color request.

			if (source == cut || source == CUT)
			{
				if (pp == null) return ;
				Vector selected = pp.getSelection() ;
				ColorEdit selectcopy = new ColorEdit(selected) ;
				clipboard.setContents(selectcopy,this) ;
				paste.setEnabled(true) ;
				PASTE.setEnabled(true) ;
				selectclip.setEnabled(true) ;

				// Set all selected colors to black (0,0,0).

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				Vector black = new Vector() ;
				for (int i = 0 ; i < selected.size() ; i++)
					black.addElement(Color.black) ;
				pp.setSelectedColors(black) ;
            updateColorMenu(multipalette) ;
				preview.redraw(multipalette) ;
            icp.setImage(preview.getImage()) ;
            icp.reset() ;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				return ;
			}

			// Copy color request.

			if (source == copy || source == COPY)
			{
				if (pp == null) return ;
				Vector selected = pp.getSelection() ;
				ColorEdit selectcopy = new ColorEdit(selected) ;
				clipboard.setContents(selectcopy,this) ;
				paste.setEnabled(true) ;
				PASTE.setEnabled(true) ;
				selectclip.setEnabled(true) ;
				return ;
			}

			// Paste color request.

			if (source == paste || source == PASTE)
			{
				if (pp == null) return ;
				Vector selected = pp.getSelection() ;
				Transferable t = clipboard.getContents(this) ;
				if (!(t instanceof ColorEdit)) return ;
				Vector selectcopy = (Vector) t ;

				// Check for copy/paste size agreement.

				if (selected.size() != selectcopy.size())
				{
               String s1 = Kisekae.getCaptions().getString("PasteSizeText2") ;
               int i1 = s1.indexOf('[') ;
               int j1 = s1.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s1 = s1.substring(0,i1+1) + selectcopy.size() + s1.substring(j1) ;
               i1 = s1.indexOf('[') ;
               j1 = s1.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s1 = s1.substring(0,i1+1) + selected.size() + s1.substring(j1) ;
					int i = JOptionPane.showConfirmDialog(this,
						Kisekae.getCaptions().getString("PasteSizeText1") + "\n" + s1,
						Kisekae.getCaptions().getString("EditError"),
                  JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) ;
					if (i != JOptionPane.YES_OPTION) return ;
				}

				// Update the currently selected colors.  The copied colors
            // are sequentially copied to the selected colors.  If the
            // selection size exceeds the copy size then multiple iterations
            // of the copy are constructed.

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				Vector colors = new Vector() ;
            while (colors.size() < selected.size())
            {
   				for (int i = 0 ; i < selectcopy.size() ; i++)
               {
               	Object [] o = (Object []) selectcopy.elementAt(i) ;
                  if (o[0] instanceof Color) colors.addElement(o[0]) ;
                  if (colors.size() > selected.size()) break ;
   				}
            }

				// Update the preview panel.

				pp.setSelectedColors(colors) ;
            updateColorMenu(multipalette) ;
				preview.redraw(multipalette) ;
            icp.setImage(preview.getImage()) ;
            icp.reset() ;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				return ;
			}

			// Undo request.

			if (source == UNDO)
			{
				if (undoAction == null) return ;
				undoAction.actionPerformed(evt) ;
				return ;
			}

			// Redo request.

			if (source == REDO)
			{
				if (redoAction == null) return ;
				redoAction.actionPerformed(evt) ;
				return ;
			}

			// Revert Palette request.

			if (source == reload)
			{
				if (palette == null) return ;
				int i = JOptionPane.showConfirmDialog(this,
               Kisekae.getCaptions().getString("UndoAllConfirmText"),
               Kisekae.getCaptions().getString("UndoAllDialogTitle"),
              	JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.WARNING_MESSAGE) ;
            if (i == JOptionPane.CANCEL_OPTION) return ;

				// Reload palette.

            undoredo = true ;
            palette.undoEdits() ;
				changed = false ;
				undo.discardAllEdits() ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
            undoredo = false ;

				// Update our palette panel and preview pane.

            multipalette = new Integer(0) ;
            updateInterface(multipalette) ;
            return ;
         }

			// Select All color request.

			if (source == selectall)
			{
				if (pp == null) return ;
				pp.selectAll() ;
				return ;
			}

			// Select Hue color request.  This selects all colors from the
			// palette that are near the color of the active color.

			if (source == selecthue)
			{
				if (pp == null) return ;
				pp.selectHSB() ;
				return ;
			}

			// Select RGB color request.  This selects all colors from the
			// palette that are near the color of the active color.

			if (source == selectrgb)
			{
				if (pp == null) return ;
				pp.selectRGB() ;
				return ;
			}

			// Select Copy color request.  This selects the color buttons
         // as saved in the clipboard.

			if (source == selectclip)
			{
				if (pp == null) return ;
				Transferable t = clipboard.getContents(this) ;
				if (!(t instanceof ColorEdit)) return ;
				Vector selectcopy = (Vector) t ;

				// Select the colors identified by the clipboard index.

				for (int i = 0 ; i < selectcopy.size() ; i++)
            {
            	Object [] o = (Object []) selectcopy.elementAt(i) ;
               if (o[1] instanceof Integer)
               	pp.setColor(((Integer) o[1]).intValue(),(i != 0)) ;
            }
				return ;
			}

			// Set Color Count request.  This dithers the image for the
         // specified number of colors.

			if (source == setsize)
			{
            int n = 0 ;
            ColorSizeDialog csd = null ;
            
            // Show the color size dialog.

         	if (cel != null)
            {
               n = cel.getColorsUsed() ;
               if (palette != null) n = palette.getColorCount() ;
            	csd = new ColorSizeDialog(this,n) ;
            }
         	else if (palette != null)
            {
               n = palette.getColorCount() ;
            	csd = new ColorSizeDialog(this,n) ;
            }
            if (csd == null) return ;
            int m = csd.getColorSize() ;
            if (m > 256 && cel instanceof GifCel) m = 256 ;
            if (m <= 0) return ;
            
            // Dither the image.
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            setColorCount(m) ;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

            // Save the edit for undo processing.

            UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
               cel,palette,multipalette,
               oldimage,oldpalette,olddata,oldmp,n,oldbackground,oldtransparent,
               newimage,adjpalette,newdata,newmp,m,newbackground,newtransparent) ;
            UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
            undo.undoableEditHappened(uee) ;
            undoAction.updateUndoState() ;
            redoAction.updateRedoState() ;
            
				// Update our palette panel and preview pane.  If we had dithered
            // the all cels image then we retain our current palette, otherwise
            // we will have a valid cel and should retain the adjusted palette
            // as a cel specific palette for a subsequent save.

            if (preview != null) preview.init(palette,cel,multipalette) ;
            updateInterface(multipalette) ;
            return ;
         }

			// A settransparent request sets the palette transparent color.

			if (source == settransparent)
			{
            int oldtransparent = -1 ;
            int newtransparent = -1 ;

				if (palette != null)
				{
					oldtransparent = palette.getTransparentIndex() ;
					newtransparent = settransparent.getColorIndex() ;
					palette.setTransparentIndex(newtransparent) ;
				}

				if (cel != null && cel.isTruecolor())
				{
            	oldtransparent = cel.getTransparentIndex() ;
					Color c = settransparent.getColor() ;
					newtransparent = (c != null) ? c.getRGB() & 0x00ffffff : -1 ;
            }

            if (cel != null)
            {
					cel.setTransparentIndex(newtransparent) ;
					cel.changeTransparency(0) ;
					cel.setUpdated(true) ;
            }

				// Save the edit for undo processing.

				UndoableEdit ce = new UndoableCel(evt.getActionCommand(),
					cel,palette,multipalette,oldtransparent,newtransparent) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Update our palette panel and preview pane.

				changed = true ;
				if (memorysource) update.setEnabled(true) ;
				preview.redraw(multipalette) ;
            icp.setImage(preview.getImage()) ;
            icp.reset() ;
				return ;
			}

			// A setbackground request sets the palette background color.

			if (source == setbackground)
			{
				if (palette == null) return ;
				int n = palette.getColorCount() ;
				Object [] olddata = palette.getPaletteData() ;
				int oldmp = palette.getMultiPaletteCount() ;
				int oldbackground = palette.getBackgroundIndex() ;
				int oldtransparent = palette.getTransparentIndex() ;
				int index = setbackground.getColorIndex() ;
				palette.setBackgroundIndex(index) ;
				if (palette.isInternal() && cel != null) cel.setUpdated(true) ;

            // Save the edit for undo processing.

            UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
					palette,multipalette,olddata,oldmp,n,oldbackground,oldtransparent,
					palette.getPaletteData(),palette.getMultiPaletteCount(),
					palette.getColorCount(),palette.getBackgroundIndex(),
					palette.getTransparentIndex()) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Update our palette panel and preview pane.

				changed = true ;
				if (memorysource) update.setEnabled(true) ;
				preview.redraw(multipalette) ;
            icp.setImage(preview.getImage()) ;
            icp.reset() ;
				return ;
			}

			// An export request writes the palette object as a text file.

			if (source == exportp)
			{
				if (palette == null) return ;
				FileSave fs = new FileSave(this,palette) ;
				fs.showexport() ;
				return ;
			}

			// An import request invokes a file open dialog for .PAL files
         // and imports the definition to the current palette.

			if (source == importp)
			{
				if (palette == null) return ;
				String [] ext = ArchiveFile.getPaletteExt() ;
				FileOpen fdnew = new FileOpen(this,Kisekae.getCaptions().getString("PaletteListTitle"),ext) ;
				fdnew.show() ;
				ArchiveEntry ze = fdnew.getZipEntry() ;
				if (ze == null) { fdnew.close() ; return ; }

				// Confirm that we selected a valid file.

            String s = ze.getExtension().toUpperCase() ;
				if (!ze.isPalette())
				{
					String name = ze.getName() ;
               s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
               JOptionPane.showMessageDialog(this,
                  s + "\n" +
                  Kisekae.getCaptions().getString("SaveAsPaletteText"),
                  Kisekae.getCaptions().getString("FileOpenException"),
                  JOptionPane.ERROR_MESSAGE) ;
               fdnew.close() ;
					return ;
				}

				// Import the text.

				int n = palette.getColorCount() ;
				Object [] olddata = palette.getPaletteData() ;
				int oldmp = palette.getMultiPaletteCount() ;
				int oldbackground = palette.getBackgroundIndex() ;
				int oldtransparent = palette.getTransparentIndex() ;
				palette.setLoader(me) ;
				palette.importPalette(ze) ;
				palette.invalidate() ;
				palette.setUpdated(true) ;
            palette.setLoader(null) ;
				fdnew.close() ;

				// Check for errors and restore the palette if necessary.

				if (palette.isError())
				{
					byte [] alpha = (byte []) olddata[0] ;
					byte [] red = (byte []) olddata[1] ;
					byte [] green = (byte []) olddata[2] ;
					byte [] blue = (byte []) olddata[3] ;
					palette.setPalette(alpha,red,green,blue,oldmp,n,oldbackground,oldtransparent) ;
					return ;
				}

				// Create an undoable edit.

				UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
					palette,multipalette,olddata,oldmp,n,oldbackground,oldtransparent,
					palette.getPaletteData(),palette.getMultiPaletteCount(),
					palette.getColorCount(),palette.getBackgroundIndex(),
					palette.getTransparentIndex()) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Show the new palette.

				changed = true ;
            updateInterface(multipalette) ;

            // Acknowledge the import.

            s = Kisekae.getCaptions().getString("PaletteImportText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + palette.getMultiPaletteCount() + s.substring(j1) ;
       		JOptionPane.showMessageDialog(this, s,
               Kisekae.getCaptions().getString("PaletteImportTitle"),
            	JOptionPane.INFORMATION_MESSAGE) ;
				return ;
			}

			// A merge request invokes a file open dialog to identify the
         // palette to merge, loads the palette, and adds the definition
         // to the current palette.

			if (source == mergep)
			{
				if (palette == null) return ;
				String [] ext = ArchiveFile.getPaletteExt() ;
				FileOpen fdnew = new FileOpen(this,Kisekae.getCaptions().getString("PaletteListTitle"),ext) ;
				fdnew.show() ;
				ArchiveEntry ze = fdnew.getZipEntry() ;
				if (ze == null) { fdnew.close() ; return ; }

				// Confirm that we selected a palette file.

            String s = ze.getExtension().toUpperCase() ;
				if (!ze.isPalette())
				{
					String name = ze.getName() ;
               s = Kisekae.getCaptions().getString("InvalidFileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
               JOptionPane.showMessageDialog(this,
                  s + "\n" +
                  Kisekae.getCaptions().getString("SaveAsPaletteText"),
                  Kisekae.getCaptions().getString("FileOpenException"),
                  JOptionPane.ERROR_MESSAGE) ;
					fdnew.close() ;
					return ;
				}

				// Open the new file.  Load the merge palette.

				Palette p = new Palette(ze.getZipFile(),ze.getPath()) ;
				p.load() ;
				fdnew.close() ;

            // Merge the palettes.

				int n = palette.getColorCount() ;
				Object [] olddata = palette.getPaletteData() ;
				int oldmp = palette.getMultiPaletteCount() ;
				int oldbackground = palette.getBackgroundIndex() ;
				int oldtransparent = palette.getTransparentIndex() ;
            palette.merge(p) ;

				// Create an undoable edit.

				UndoableEdit ce = new UndoablePalette(evt.getActionCommand(),
					palette,multipalette,olddata,oldmp,n,oldbackground,oldtransparent,
					palette.getPaletteData(),palette.getMultiPaletteCount(),
					palette.getColorCount(),palette.getBackgroundIndex(),
					palette.getTransparentIndex()) ;
				UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(uee) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;

				// Show the new palette.

				changed = true ;
            updateInterface(multipalette) ;

            // Acknowledge the merge.

            s = Kisekae.getCaptions().getString("PaletteMergeText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + palette.getMultiPaletteCount() + s.substring(j1) ;
            JOptionPane.showMessageDialog(this, s,
               Kisekae.getCaptions().getString("PaletteImportTitle"),
               JOptionPane.INFORMATION_MESSAGE) ;
				return ;
			}

			// An update request from the file save window has occured.
         // Once the file is written changes cannot be undone.  We
         // retain visibility of the active file and do not switch
         // to the saved file.

			if ("FileWriter Callback".equals(evt.getActionCommand()))
			{
				String newname = (writecel != null) ? writecel.getPath() : null ;
            
            if (originalzepath != null && ze != null)
            	ze.setPath(originalzepath) ;
            if (originalcelname != null && writecel != null)
            	writecel.setName(originalcelname) ;
            if (originalpalettename != null && writepalette != null)
            	writepalette.setName(originalpalettename) ;

				// If we saved any file as a Cel then save the
            // associated palette under its original name.
            // Save the palette to the cel directory.

  				if (ArchiveFile.isCel(newname))
            {
               if (!saveasrequest && writepalette != null)
               {
                  File f = new File(newname) ;
                  String s = writepalette.getName() ;
                  String dir = f.getParent() ;
                  f = new File(dir,s) ;
                  newname = f.getPath() ;
               }
               savepalette(writecel,writepalette,newname) ;
            }

				// If we saved a palette file then make sure that any preset 
            // encoding is dropped.

				if (writecel == null && writepalette != null)
				{
   				writepalette.setEncodeArrays(null,null,null) ;
				}

            // If we are loaded from an active configuration, update.

            if (!saveasrequest)
            {
               if (memorysource)
                  source = update ;
               else
                  changed = false ;
            }
            
            writecel = null ;
         }

			// Update request.  Applies the document to the current configuration.
         // We establish a palette edit checkpoint by restarting edits.  The
         // undo list is retained so that prior edits can be undone.

			if (source == update)
			{
				if (changed)
            {
            	int basetransparent = -1 ;

            	// Transparent cel color changes were applied to
               // a cloned cel.  We need to apply the change to the real cel.

               if (cel != null)
               {
               	int transparent = cel.getTransparentIndex() ;
						if (configobject instanceof Cel)
                  {
                  	Cel c = (Cel) configobject ;
                     basetransparent = c.getTransparentIndex() ;
                     c.setTransparentIndex(transparent) ;
                     c.changeTransparency(0) ;
                  }
               }
            	callback.doClick() ;
//               capturePanelEdit(configobject,basetransparent) ;
               update.setEnabled(false) ;
               toFront() ;
            }

				updated = true ;
            changed = false ;
            if (palette != null) palette.startEdits() ;
            if (palette != null) palette.setUpdated(false) ;
            if (cel != null) cel.setUpdated(false) ;
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

			// A color has been picked up from the ImagePreview panel.  If the
			// control key was down then the new color will be added to our
			// current selection list.  If the right mouse button was pressed
			// then the color will be removed from our current selection list.
			// If we are editing a specific cel we adjust the preview absolute
         // pixel location to the relative cel location.

			if ("ImagePreview Callback".equals(evt.getActionCommand()))
			{
         	if (pp == null) return ;
				if (preview == null) return ;
				Point p = preview.getPixel() ;
				if (cel != null)
				{
					Rectangle box = cel.getBoundingBox() ;
					p.x += box.x ;
					p.y += box.y ;
				}
				Color c = preview.getColor(p.x,p.y) ;
				pp.setColor(c,preview.isControlDown(),preview.isMetaDown()) ;
				if (palette == null) updateColorMenu(null) ;
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
						PrintLn.printErr("Printing error: " + ex.toString()) ;
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

			// A Palette properties request shows the palette dialog.

			if (paletteproperties == source)
			{
            if (palette == null) return ;
				PaletteDialog pd = new PaletteDialog(this,palette,null,config) ;
				pd.show() ;
            return ;
         }

			// A Cel properties request shows the cel dialog.

			if (celproperties == source)
			{
            if (cel == null) return ;
   			CelDialog cd = new CelDialog(this,cel,null,config) ;
            cd.callback.addActionListener(this) ;
				cd.show() ;
            return ;
         }

			// Cel properties can adjust the image transparency.

			if ("KissDialog Callback".equals(evt.getActionCommand()))
			{
            if (cel != null && cel.isUpdated()) changed = true ;
				if (preview == null) return ;
            preview.redraw(multipalette) ;
         }
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("ColorFrame: Out of memory.") ;
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
			PrintLn.println("ColorFrame: Internal fault, action " + evt.getActionCommand()) ;
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

		// Turn the palette panel window on and off.

		if (source == ppwindow)
		{
			if (pp == null) return ;
			pp.setVisible(ppwindow.getState()) ;
			return ;
		}

		// Turn the color chooser window on and off.

		if (source == ccwindow)
		{
			if (cc == null) return ;
			cc.setVisible(ccwindow.getState()) ;
			return ;
		}

		// Turn the color transform window on and off.

		if (source == icpwindow)
		{
			if (icp == null) return ;
			icp.setVisible(icpwindow.getState()) ;
			return ;
		}

		// Turn the preview pane window on and off.  This also turns off
		// the image preview toolbar.

		if (source == previewwindow)
		{
			if (preview == null) return ;
			panel5.setVisible(previewwindow.getState()) ;
			return ;
		}

		// Turn the selection window on and off.  

		if (source == selectwindow)
		{
			panel4.setVisible(selectwindow.getState()) ;
			return ;
		}

		// Request to show the preview image background color.  This removes
		// transparency from the preview image background.

		if (source == showbackground)
		{
			if (preview == null) return ;
			preview.redraw(multipalette) ;
			return ;
		}
	}


   // Method to check for pending updates.  This method returns true if
	// the check is not cancelled.  This method will restore the palette
	// state to the initial multipalette if the contents are not saved.

	private boolean closecheck(boolean cancel)
	{
      if (icp.isChanged()) icp.apply() ;
		boolean restorestate = changed ;

		// Check for a file save if changes are pending.

		if (changed)
      {
      	int opt = JOptionPane.YES_NO_OPTION ;
         if (cancel) opt = JOptionPane.YES_NO_CANCEL_OPTION ;

         // If we are editing an active file, check for a data set update.

			if (memorysource)
	      {
            String s = Kisekae.getCaptions().getString("UnknownValueText") ;
            if (palette != null) s = palette.getName() ;
            if (cel != null) s = cel.getName() ;
            String s1 = Kisekae.getCaptions().getString("ApplyChangeFileText") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
            int i = JOptionPane.showConfirmDialog(this, s1,
               Kisekae.getCaptions().getString("ApplyChangeTitle"),
               opt, JOptionPane.QUESTION_MESSAGE) ;

	         // If we should apply the changes, update our panel frame using
	         // the contents of the current palette.

				if (i == JOptionPane.CANCEL_OPTION)
	            return false ;
				if (i == JOptionPane.YES_OPTION)
				{
               int baseloop = (basecel == null) ? 0 : basecel.getLoopCount() ; ;
            	int basetransparent = (basecel == null) ? -1 : basecel.getTransparentIndex() ;
            	Point baseoffset = (basecel == null) ? null : basecel.getOffset() ;
            	Point baselocation = (basecel == null) ? null : basecel.getLocation() ;
               Image baseimage = (basecel == null) ? null : basecel.getImage() ;
               ColorModel basecm = (basecel == null) ? null : basecel.getColorModel() ;
               Palette oldpalette = (basecel == null) ? palette : basecel.getPalette() ; 
               
            	// Transparent cel color changes may have been applied to
					// a cloned cel.  We need to apply the change to the real cel.

               if (cel != null && basecel != cel)
               {
                  basecel.setImage(cel.getImage()) ;
                  basecel.setPalette(cel.getPalette()) ;
                  basecel.setTransparentIndex(cel.getTransparentIndex()) ;
                  basecel.changeTransparency(0) ;
                  cel = basecel ;
               }

					// Perform the callback and capture the edit change.  We set the 
               // data object on the callback to the cel being edited as tint
               // changes can adjust the image, and this requires a cel reload.

               callback.setDataObject(cel) ;
					callback.doClick() ;
  					capturePanelEdit(configobject,basetransparent,baseloop,baseoffset,baselocation,baseimage,basecm,oldpalette,cel) ;
					updated = true ;
               restorestate = false ;
				}
	      }

         // If we are editing a non-active file, check for a file save.

			else if (!Kisekae.isSecure())
			{
				String file = null ;
				if (ze == null && palette != null) file = palette.getName() ;
				if (ze != null) file = ze.getPath() ;
				ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;

				// If we have a palette, ask the user.

				if (palette != null)
				{
              String s1 = Kisekae.getCaptions().getString("SaveChangeText") ;
              String s2 = Kisekae.getCaptions().getString("SaveUntitledTitle") ;
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
              int i = JOptionPane.showConfirmDialog(this, s1, s2,
                 opt, JOptionPane.QUESTION_MESSAGE) ;

					// Save the text contents if necessary.

					if (i == JOptionPane.CANCEL_OPTION)
						return false ;
					if (i == JOptionPane.YES_OPTION)
					{
						savepalette(true) ;
						restorestate = false ;
					}
				}
			}
		}

      // Terminate edits.

		if (restorestate) cancelEdits(initmultipalette) ;
		if (palette != null) palette.stopEdits() ;
		undo.discardAllEdits() ;
		undoAction.updateUndoState() ;
		redoAction.updateRedoState() ;
		ze = null ;
      changed = false ;
		return true ;
   }


   // This function constructs an undoable color change edit on the
   // PanelFrame.  This edit allows the panel frame to undo palette
   // color changes that are applied through registered event callbacks.
   // The fact that we have to construct the undoable edit within this
   // ColorFrame editor is an architectural problem as it now associates
   // the color editor to the panel frame.

   private void capturePanelEdit(Object editobject, int transparent, int loop,
     Point baseoffset, Point baselocation, Image img, ColorModel cm, Palette oldpalette, Cel oldcel)
   {
      Object [] basedata = null ;
      int basecolors = 0 ;
      int basemp = 0 ;
      int basebackground = -1 ;
      int basetransparent = -1 ;
      Object [] currentdata = null ;
      int currentcolors = 0 ;
      int currentmp = 0 ;
      int currentbackground = -1 ;
      int currenttransparent = -1 ;
      
      if (!memorysource) return ;
   	MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe == null) return ;
      PanelFrame panelframe = mainframe.getPanel() ;
      if (panelframe == null) return ;

      // Identify the before and after state for palette changes.

   	if (palette != null)
      {
	      basedata = palette.getEditPaletteData() ;
	      basecolors = palette.getEditColorCount() ;
	      basemp = palette.getEditMultiPaletteCount() ;
	      basebackground = palette.getEditBackgroundIndex() ;
	      basetransparent = palette.getEditTransparentIndex() ;
	      currentdata = palette.getPaletteData() ;
	      currentcolors = palette.getColorCount() ;
	      currentmp = palette.getMultiPaletteCount() ;
	      currentbackground = palette.getBackgroundIndex() ;
	      currenttransparent = palette.getTransparentIndex() ;
      }

	   // Construct the undoable edit.  Thic color edit is for palette color
      // changes where the palette data arrays were changed, or for image
      // tint changes where a cel image was updated and the palette was
      // generated through a dithering operation.

      if (palette != null && oldpalette != null)
      {
	      panelframe.createColorEdit(palette, oldpalette,
            basedata,basemp,basecolors,basebackground,basetransparent,
            currentdata,currentmp,currentcolors,currentbackground,currenttransparent,
            cel,oldcel,transparent,loop,img,cm) ; 
      }

      // Identify the before and after state for cel changes.  This edit happens
      // for truecolor cels that do not have a palette.

   	else if (editobject instanceof Cel)
      {
         panelframe.createImageEdit(editobject,transparent,loop,baseoffset,baselocation,img,oldpalette) ;
      }
   }

   
   // Implementation of the menu item update of our state when we become
   // visible.  
   
   void updateRunState()
   {
      int i = 0 ;
      for (i = 0 ; i < windowMenu.getItemCount() ; i++)
      {
         Object o = windowMenu.getItem(i) ;
         if (o instanceof JSeparator) break ;
         if (o == null) break ;
      }
      
      // Remove all prior window entries
      
      for (int j = windowMenu.getItemCount()-1 ; j >= i ; j--)
         windowMenu.remove(j) ;

      // Add new window entries

      int n = 0 ;
      Vector v = KissFrame.getWindowFrames() ;
      for (i = 0 ; i < v.size() ; i++)
      {
         KissFrame w = (KissFrame) v.elementAt(i) ;
         String s = w.getTitle() ;
         if (n == 0) windowMenu.addSeparator() ;
         JMenuItem mi = new JMenuItem(++n + ". " + s) ;
         mi.addActionListener(this) ;
         windowMenu.add(mi) ;
      }
   }


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { updateRunState() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt)
	{ closecheck(false) ; close() ; }


	// Component Events

	public void componentMoved(ComponentEvent evt) { }
	public void componentResized(ComponentEvent evt) { showLabels() ; }
	public void componentShown(ComponentEvent evt) { }
	public void componentHidden(ComponentEvent evt) { }


	// Clipboard owner interface functions.

	public void lostOwnership(Clipboard cb, Transferable contents)
	{
		if (contents instanceof ColorEdit)
			((ColorEdit) contents).removeAllElements() ;
	}


	// Utility function to write the active object.  If we have an internal
   // palette that was derived from a cel object then we write the cel object.
   // If we do not have a palette then we write the cel.  If we are 
   // performing a Save As then we write to the local file system.
   // A Save replaces the current file, which can be in an archive.

	private void savepalette(boolean saveas)
	{
		FileSave fs = null ;
      saveasrequest = saveas ;
      writecel = (cel != null) ? (Cel) cel.clone() : null ;
      ArchiveEntry cze = (writecel != null) ? writecel.getZipEntry() : null ;
      if (cze != null) writecel.setZipEntry((ArchiveEntry) cze.clone()) ;
      writepalette = (palette != null) ? (Palette) palette.clone() : null ;
      ArchiveEntry pze = (writepalette != null) ? writepalette.getZipEntry() : null ;
      if (pze != null) writepalette.setZipEntry((ArchiveEntry) pze.clone()) ;
      if (writecel != null) writecel.setPalette(writepalette) ;
      
		if (writepalette != null)
      {
			writepalette.setUpdated(true) ;
			if (writepalette.getName() == null) saveas = true ;
         if (writecel != null)
         {
            if (saveas) writecel.setZipFile(null) ;
            writecel.changePalette(multipalette) ;
            writecel.setUpdated(true) ;
         }
      }
 
      if (saveas && writepalette != null) writepalette.setZipFile(null) ;
      fs = new FileSave(this,writepalette,writecel) ;
		fs.addWriteListener(writelistener) ;
      fs.setFileFilter("ColorEditor") ;
		if (saveas) fs.show() ; else fs.save() ;
	}


	// Utility function to write the palette file contents.
   // This function writes a palette object.

	private void savepalette(Cel cel, Palette palette, String newname)
	{
		FileSave fs = null ;
		if (cel == null) return ;
		if (palette == null) return ;
		if (newname == null) return ;

      // Images with internal palettes can use more than 256 colors.  Such a
      // condition would occur if we saved an image in cel form and the image
      // did not use transparency.  In this case the image would be encoded
      // as a Cherry Kiss cel and we should not write a palette.

      if (palette.isInternal() && palette.getUsedColors() > 256) return ;

		// Set a KiSS .kcf extension for the new name.

		int i = newname.indexOf(".") ;
		if (i < 0) i = newname.length() ;
		newname = newname.substring(0,i) + ".kcf" ;

		// Create a zip entry for the new name.  We clone the palette
      // because we destructively update its name and we do not
      // want to alter the original object.

      writepalette = (Palette) palette.clone() ;
      ArchiveEntry pze = (writepalette != null) ? writepalette.getZipEntry() : null ;
      if (pze != null) writepalette.setZipEntry((ArchiveEntry) pze.clone()) ;
		ArchiveFile zip = cel.getZipFile() ;
      if (zip == null)
         writepalette.setZipFile(null) ;
		if (zip instanceof DirFile || zip == null)
			writepalette.setZipEntry(new DirEntry(newname));
		if (zip instanceof PkzFile)
			writepalette.setZipEntry(new PkzEntry(newname));
		if (zip instanceof LhaFile)
			writepalette.setZipEntry(new LhaEntry(newname));

		// Save the palette.

      saveasrequest = false ;
		writepalette.setName(newname) ;
		writepalette.setUpdated(true) ;
		fs = new FileSave(this,writepalette) ;
		fs.save() ;
	}


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
   

	// Utility functions to return our panels.

	Component getToolbar() { return toolbar ; } 
   Component getEditPane() 
   { 
		panel4.setVisible(false) ;
		panel5.setVisible(true) ;
		cc.setVisible(true) ;
		pp.setVisible(true) ;
      return panel1 ; 
   }



	// A utility function to place a new cel at the origin.

	private void setAtOrigin(Cel cel)
   {
   	if (cel == null) return ;
		cel.setLocation(new Point(0,0)) ;
		Point offset = cel.getOffset() ;
		cel.setPlacement(new Point(-offset.x,-offset.y)) ;
   }


	// A utility function to apply an image color transformation.

   void applyTransformedImage(Image image)
   {
      int n = 0 ;
      if (cel == null) return ;
      changed = true ;

      // If we have a cel, update the cel size and image.  It is not possible
      // to apply tints to palettes without an image.  This is because the
      // image is dithered to obtain a palette and this rebuilds the image
      // indexes into the new palette.

      Image oldimage1 = cel.getImage() ;
      cel.setImage(image) ;
      cel.setUpdated(true) ;

      // If we have a palette, reconstruct the palette and image.
      // Image transforms may adjust the number of colors.

      if (cel.getPalette() != null)
      {
         n = cel.getPalette().getColorCount() ;
         setColorCount(n) ;
         oldimage = oldimage1 ;
 
         // Save the edit for undo processing.

         UndoableEdit ce = new UndoablePalette("Color Adjust",
            cel,palette,multipalette,
            oldimage,oldpalette,olddata,oldmp,n,oldbackground,oldtransparent,
            newimage,adjpalette,newdata,newmp,n,newbackground,newtransparent) ;
         UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
         undo.undoableEditHappened(uee) ;
         undoAction.updateUndoState() ;
         redoAction.updateRedoState() ;
      }
      else
      {
         UndoableEdit ce = new UndoableImage("Color Adjust2",cel,oldimage1,image) ;
         UndoableEditEvent uee = new UndoableEditEvent(this,ce) ;
         undo.undoableEditHappened(uee) ;
         undoAction.updateUndoState() ;
         redoAction.updateRedoState() ;
      }

      // Update the preview image.

      if (preview != null) preview.init(palette,cel,multipalette) ;
      updateInterface(multipalette) ;
   }
   
   
   // A utility function to dither the image for a new color count.  Note that
   // a new palette and a new image is created by dithering.  It is not 
   // possible to simply replace the palette as color indexes in the image 
   // have all changed.
   
   private void setColorCount(int count)
   {
      int n = 0 ;
      if (icp.isChanged()) icp.apply() ;

      // Retain the old palette information.  We heed to create copied
      // values as the data in the palette can change.

      if (palette != null)
      {
         olddata = palette.getPaletteData() ;
         int n0 = ((byte []) olddata[0]).length ;
         byte [] oldalpha = new byte [n0] ;
         byte [] oldred = new byte [n0] ;
         byte [] oldgreen = new byte [n0] ;
         byte [] oldblue = new byte [n0] ;
   		oldmp = palette.getMultiPaletteCount() ;
   		oldbackground = palette.getBackgroundIndex() ;
   		oldtransparent = palette.getTransparentIndex() ;
         for (int i = 0 ; i < n0 ; i++)
         {
            oldalpha[i] = ((byte []) olddata[0])[i] ;
            oldred[i] = ((byte []) olddata[1])[i] ;
            oldgreen[i] = ((byte []) olddata[2])[i] ;
            oldblue[i] = ((byte []) olddata[3])[i] ;
         }
         olddata[0] = oldalpha ;
         olddata[1] = oldred ;
         olddata[2] = oldgreen ;
         olddata[3] = oldblue ;
      }

      // Dither the image to the new color count. This will update the
      // cel image and palette and possibly change the cel truecolor
      // state.

      if (cel != null)
      {
         oldimage = cel.getImage() ;
         oldpalette = cel.getPalette() ;
      
         // Peform the dithering.
      
         Object [] o = cel.dither(count,false) ;
         if (o == null) return ;
         newimage = (Image) o[0] ;
         adjpalette = (Palette) o[1] ;
         ColorModel newcm = (ColorModel) o[2] ;
      
         // Apply the dithered image and palette.
      
         cel.setImage(newimage,newcm) ;
         cel.setUpdated(true) ;

         // Dithering may have reduced colors to a palette image.

         if (palette == null && adjpalette != null)
         {
            palette = adjpalette ;
            oldbackground = cel.getBackgroundIndex() ;
            oldtransparent = cel.getTransparentIndex() ;
            cel.setPalette(adjpalette) ;
            cel.setPaletteID(adjpalette.getIdentifier()) ;
         }
         else if (count > 256)
         {
            palette = adjpalette = null ;
            cel.setPalette(null) ;
            cel.setPaletteID(null) ;
         }
      }

      // Update the palette.

      if (palette != null)
      {
         changed = true ;

         // Apply any new palette settings initiated from cel dithering.
         // The transparent color is now at palette index 0.  This replaces
         // the current palette data arrays.

         if (adjpalette != null)
         {
            Object [] o = adjpalette.getPaletteData() ;
            Color c = (cel != null) ? cel.getTransparentColor() : null ;
            int ti = (c != null) ? 0 : -1 ;
            byte [] a = (byte []) o[0] ;
            byte [] r = (byte []) o[1] ;
            byte [] g = (byte []) o[2] ;
            byte [] b = (byte []) o[3] ;
            int mp = (multipalette != null) ? multipalette.intValue() : 0 ;
            palette.setMultiPalette(a,r,g,b,mp) ;
            palette.setTransparentIndex(ti) ;
            if (cel != null) cel.setTransparentIndex(ti) ;
         }

         // Ensure that we have the requisite number of colors.

         int colors = Math.min(count,256) ;
     	   palette.setColorCount(colors) ;

         // Retain the new palette data for undo processing.

  			newdata = palette.getPaletteData() ;
         newmp = palette.getMultiPaletteCount() ;
  		   newbackground = palette.getBackgroundIndex() ;
  			newtransparent = palette.getTransparentIndex() ;
      }
   }


	// A utility function to update the color selection menus to agree with
	// the current palette.

	private void updateColorMenu(Integer multipalette)
	{
		if (pp == null) return ;
		int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
		Dimension d = pp.getDimension() ;

		// Resize the transparency and background color selection menus.

		if (settransparent != null)
			settransparent.setDimension(d) ;
		if (setbackground != null)
			setbackground.setDimension(d) ;

		// Populate the transparency and background color selection menus.

		if (palette != null)
		{
			for (int i = 0 ; i < d.height*d.width ; i++)
			{
				Color c = palette.getColor(mp,i) ;
				if (settransparent != null) settransparent.setMenuColor(i,c) ;
				if (setbackground != null) setbackground.setMenuColor(i,c) ;
			}
			if (settransparent != null)
				settransparent.setSelectedIndex(palette.getTransparentIndex()) ;
			if (setbackground != null)
				setbackground.setSelectedIndex(palette.getBackgroundIndex()) ;
		}

		// If we do not have a palette the color menu shows the active color.

		else
		{
			Color c = pp.getActiveColor() ;
			if (setbackground != null) setbackground.setMenuColor(0,c) ;
			if (settransparent != null)
			{
				settransparent.setMenuColor(0,c) ;
				if (cel != null && c.equals(cel.getTransparentColor()))
					settransparent.setSelectedIndex(0) ;
			}
		}
	}


   // A utility function to update our user interface items for the
   // current palette object and specified multipalette.

	private void updateInterface(Integer multipalette)
   {
		pp.init(palette,multipalette) ;
		preview.redraw(multipalette) ;
		updateColorMenu(multipalette) ;
      icp.setImage(preview.getImage()) ;
      icp.reset() ;
		setValues() ;
		doLayout() ;
		validate() ;
      repaint() ;
   }


	// Utility function to cancel all edit changes and reset the cel
	// colors to the specified multipalette.

	private void cancelEdits(Integer multipalette)
	{
		if (palette == null) return ;
		palette.undoEdits() ;
		changed = false ;
		update.setEnabled(false) ;
		if (!updated)
		{
			save.setEnabled(false) ;
			SAVE.setEnabled(false) ;
			if (cel == null)
				palette.setUpdated(entrystate) ;
			else
				cel.setUpdated(entrystate) ;
		}
		if (multipalette != null)
		{
			pp.reset(multipalette,0) ;
			preview.redraw(multipalette) ;
		}
	}


	// We close the frame after clean up.   We also restore our cel colors
	// to the multipalette being used in the panel frame as colors may have
	// changed during this edit session.

	public void close()
	{
		super.close() ;
		if (cel != null) cel.setLoader(null) ;
		if (palette != null) palette.setLoader(null) ;
		if (undo != null)	undo.discardAllEdits() ;
		if (preview != null) preview.setImage(null) ;
		if (config != null)
		{
			MainFrame main = Kisekae.getMainFrame() ;
			PanelFrame panel = (main == null) ? null : main.getPanel() ;
			Integer mp = (panel == null) ? null : panel.getMultiPalette() ;
			preview.redraw(mp) ;
		}
      callback.removeActionListener(null) ;
		flush() ;
		dispose() ;
	}


	// We close the current cel and palette and any zip file.

	void closepalette()
	{
		if (cel != null) cel.setLoader(null) ;
		if (palette != null) palette.setLoader(null) ;
		if (undo != null)	undo.discardAllEdits() ;
		if (preview != null) preview.setImage(null) ;
		if (ze != null)
		{
			ArchiveFile zip = ze.getZipFile() ;
			try { zip.close() ; }
			catch (IOException e) { }
		}
		cel = null ;
		palette = null ;
		ze = null ;
	}


	// We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if some problem occurs while
   // disposing with the dialog window.

	private void flush()
	{
		me = null ;
		cc = null ;
		pp = null ;
		ze = null ;
		cel = null ;
		palette = null ;
		config = null ;
		toolbar = null ;
		actions = null ;
		clipboard = null ;
		if (undo != null)	undo.discardAllEdits() ;
		undo = null ;
		if (preview != null) preview.setImage(null) ;
      if (icp != null) icp.flush() ;
		preview = null ;
      icp = null ;
      KissObject.setLoader(null) ;
      
      // Flush dithering retention
      
      oldimage = null ;
      newimage = null ;
      oldpalette = null ;
      adjpalette = null ;
      olddata = null ;
      newdata = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		FRAMES.removeItemListener(frameListener) ;
      FRAMES.setModel(new DefaultComboBoxModel()) ;
		PALETTES.removeItemListener(fileListener) ;
      PALETTES.setModel(new DefaultComboBoxModel()) ;
		MULTIPALETTES.removeItemListener(multiListener) ;
      MULTIPALETTES.setModel(new DefaultComboBoxModel()) ;
		removeKeyListener(keyListener);
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
		Runtime.getRuntime().gc() ;
   }


	// Inner class to create a palette grid panel to choose a color.
   // This class maintains the color selection list for all selected colors.

	class PalettePanel extends JPanel
		implements ChangeListener, ActionListener
	{
		private Border unselectedBorder;
		private Border selectedBorder;
		private Border activeBorder;

		private PaletteButton btn[] = null ;	 	// Palette color buttons
		private PaletteButton active = null ;  	// Active color button
		private Vector selected = null ;          // Selected button list
      private Integer multipalette = null ;		// Multipalette on display
      private Timer changetimer = null ;			// Timer to post color changes
      private Color changecolor = null ;			// New color from color chooser
      private String indextext = "" ;           // Language term for 'Index'
      private int colors = 0 ;						// Colors in palette
		private int rows = 0 ;							// Grid rows
		private int cols = 0 ;							// Grid columns

		// Constructor

		public PalettePanel(Palette p, Integer multipalette)
		{
			unselectedBorder = new JButton().getBorder() ;
			selectedBorder = new CompoundBorder(
				new MatteBorder(2,2,2,2,Color.red),
				new MatteBorder(1,1,1,1,getBackground())) ;
			activeBorder = new CompoundBorder(
				new MatteBorder(2, 2, 2, 2, Color.blue),
				new MatteBorder(1, 1, 1, 1, getBackground())) ;
			BevelBorder bb = new BevelBorder(BevelBorder.RAISED);
			setBorder(new CompoundBorder(bb,new EmptyBorder(4,4,4,4))) ;
         selected = new Vector() ;
         init(p,multipalette,false) ;
      }

      // Initialization.  If the newselect option is true the current
      // button selection is cleared, otherwise the selection set is
		// reapplied to the new palette buttons.

		void init(Palette palette, Integer multipalette)
      { init(palette,multipalette,true) ; }

		void init(Palette palette, Integer multipalette, boolean newselect)
		{
			cols = 16 ;
			colors = (palette == null) ? 0 : palette.getColorCount() ;
         indextext = Kisekae.getCaptions().getString("ColorIndexText") ;
			rows = colors / cols + ((colors % cols == 0) ? 0 : 1) ;
         if (colors < cols) cols = colors ;
			this.multipalette = multipalette ;
			int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
			if (palette != null && mp >= palette.getMultiPaletteCount())
				mp = palette.getMultiPaletteCount() - 1 ;
			if (mp < 0) mp = 0 ;

			// Set the grid layout and remove any prior components.

			if (getComponentCount() > 0) removeAll() ;
			if (rows == 0 && cols == 0) { rows = 1 ; cols = 1 ; }
         setLayout(new GridLayout(rows,cols,4,4)) ;
			Vector v = getSelection() ;
			clearSelection() ;

         // Establish the button array for this palette.  Every multipalette
			// within the palette should have the same size.

			btn = new PaletteButton[colors] ;
			for (int i = 0 ; i < btn.length ; i++)
			{
				Color c = palette.getColor(mp,i) ;
				btn[i] = new PaletteButton(c,i) ;
				if (mp < palette.getMultiPaletteCount()) add(btn[i]) ;
			}

         // Set the initial active color.  The setColor function updates
         // the color changer and the color menu interface to match the
         // current active palette color index.

			undoredo = true ;
			if (newselect)
         	setColor(-1) ;
         else
         {
         	for (int i = 0 ; i < v.size() ; i++)
            {
            	Object [] o = (Object []) v.elementAt(i) ;
               if (!(o[1] instanceof Integer)) continue ;
               int index = ((Integer) o[1]).intValue() ;
               if (index > btn.length) continue ;
               btn[index].setSelected(true) ;
               selected.addElement(btn[index]);
            }
            setColor(getActiveIndex(),true) ;
			}
			undoredo = false ;
		}

		// Method to return the active color index.

		int getActiveIndex() { return (active == null) ? -1 : active.getIndex() ; }

		// Method to return the active color.

		Color getActiveColor() { return (active == null) ? Color.black : active.getColor() ; }

		// Method to return the grid dimensions.

		Dimension getDimension() { return new Dimension(cols,rows) ; }

		// Method to determine if there is a valid selection.

		boolean hasSelection() { return (selected.size() > 0) ; }

		// Method to return a new vector of the selected colors.  The returned
		// contents are not PaletteButtons as exist in the selection vector
		// but are PaletteButton signature objects.  A signature object is a
		// 2-dimension array containing a Color and a button index.

		Vector getSelection()
		{
			Vector v = new Vector() ;
         if (palette != null)
         {
				for (int i = 0 ; i < selected.size() ; i++)
				{
					PaletteButton b = (PaletteButton) selected.elementAt(i) ;
					if (b != null) v.addElement(b.getSignature());
				}
         }
         else
         {
         	if (active != null) v.addElement(active.getSignature()) ;
         }
			return v ;
		}

		// Method to clear the selected colors.  This method erases the
		// selection vector and unselects all currently selected objects.

		void clearSelection()
		{
      	if (active != null) active.setSelected(false) ;
         for (int i = 0 ; i < selected.size() ; i++)
         	((PaletteButton) selected.elementAt(i)).setSelected(false) ;
			selected.removeAllElements() ;
			active = null ;

         // Update our menu state.

     		cut.setEnabled(false) ;
     		CUT.setEnabled(false) ;
     		copy.setEnabled(false) ;
     		COPY.setEnabled(false) ;
     		paste.setEnabled(false) ;
     		PASTE.setEnabled(false) ;
		}

		// Method to set the selection vector.  This method rebuilds the vector
		// given a set of PaletteButton signature objects.  It does not change
		// the button colors.

		void setSelection(Vector v)
		{
			if (v == null) v = new Vector() ;
			for (int i = 0 ; i < selected.size() ; i++)
				((PaletteButton) selected.elementAt(i)).setSelected(false) ;
			selected = new Vector() ;
			for (int i = 0 ; i < v.size() ; i++)
			{
				Object [] signature = (Object []) v.elementAt(i) ;
				int index = ((Integer) signature[1]).intValue() ;
            if (index < 0) continue ;
				if (index >= btn.length) continue ;
				if (index >= 0) selected.addElement(btn[index]) ;
			}
			for (int i = 0 ; i < selected.size() ; i++)
				((PaletteButton) selected.elementAt(i)).setSelected(true) ;
		}

		// Method to set the selected colors to the required colors.  This
		// method applies a vector of colors to the currently selected palette
		// buttons.  It does not change the set of buttons currently selected.

		void setSelectedColors(Vector colors)
		{
			if (colors == null) return ;
			if (palette == null) return ;
			CompoundEdit ce = new CompoundEdit() ;
			for (int i = 0 ; i < selected.size() ; i++)
			{
				if (i >= colors.size()) break ;
				PaletteButton b = (PaletteButton) selected.elementAt(i) ;
				Object o = colors.elementAt(i) ;
				if (o instanceof Color)
				{
					Color c = b.getBackground() ;
					Color c1 = (Color) o ;
					b.changeColor(c1) ;
					palette.setColor(multipalette,b.getIndex(),c1) ;
					palette.setLastModified(System.currentTimeMillis()) ;
					if (palette.isInternal() && cel != null) cel.setUpdated(true) ;
					ce.addEdit(new UndoableColor(multipalette,b.getIndex(),c1,c)) ;
				}
			}

			// Update the color panel.  Do not fire color change events.

			updateColorInterface(active,true) ;
			if (memorysource) update.setEnabled(true) ;
			save.setEnabled(palette != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
			SAVE.setEnabled(palette != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
			changed = true ;
			ce.end() ;

			// Capture the change for undo/redo processing.

			UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
			undo.undoableEditHappened(evt) ;
			undoAction.updateUndoState() ;
			redoAction.updateRedoState() ;
		}


		// Method to select a color from the palette panel color buttons
		// and make this the active color in the color chooser.  If the
		// append flag is true the new selection is added to the selected
		// color list.  If the remove flag is true the button is removed
      // from the selection list.  This method will not add a button to
      // the list if it is currently selected.

		void setColor(int index)  { setColor(index,false,false) ; }
		void setColor(int index, boolean a) { setColor(index,a,false) ; }
		void setColor(int index, boolean append, boolean remove)
		{
			if (btn == null) return ;
			int oldindex = getActiveIndex() ;
			Vector oldselection = getSelection() ;

         // Clear prior selected buttons if we are not adding to the selection.

         if (!append) clearSelection() ;

			// Select this button if we are adding to the selection.
         // Remove the button if we are removing it from the selection.

			PaletteButton button = active ;
         if (index >= 0 && index < btn.length) button = btn[index] ;
			if (button != null)
         {
         	if (remove) selected.removeElement(button) ;
				else if (!selected.contains(button)) selected.addElement(button) ;
         	button.setSelected(!remove) ;
         }

			// Make this button the active button.  We set the color chooser
         // color.  This will initiate a color chooser state changed event.
         // We disable the color changer event updates.

         if (remove && (index == getActiveIndex() || index < 0)) button = null ;
         if (!remove || button == null) updateColorInterface(button,true) ;

			// Capture the change for undo/redo processing.

			if (!undoredo)
			{
				UndoableEdit ce = new UndoableSelect(oldselection,getSelection(),
					oldindex,getActiveIndex()) ;
				UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(evt) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
			}
		}


		// Method to find a color in the palette panel color buttons
		// and make this the active color in the color chooser.   If the
		// append flag is true the new selection is added to the selected
		// color list.  This method will not add a button to the list if
		// it is currently selected.

		void setColor(Color c) { setColor(c,false,false) ; }
		void setColor(Color c, boolean a) { setColor(c,a,false) ; }
		void setColor(Color c, boolean append, boolean remove)
		{
         if (c == null) return ;
			if (btn == null) return ;
			int rgb = c.getRGB() & 0xffffff ;
			int oldindex = getActiveIndex() ;
			Vector oldselection = getSelection() ;
			PaletteButton first = null ;

         // If we are not adding to the selection set, clear prior selected
         // buttons.

         if (!append) clearSelection() ;

         // Search for buttons of the correct color.

         for (int index = 0 ; index < btn.length ; index++)
         {
         	PaletteButton button = btn[index] ;
         	if (rgb != (button.getRGB() & 0xffffff)) continue ;
            if (first == null) first = button ;

            // Add or remove this button to the selection.

				if (button != null)
	         {
	         	if (remove) selected.removeElement(button) ;
					else if (!selected.contains(button)) selected.addElement(button) ;
	         	button.setSelected(!remove) ;
	         }
         }

			// Update our color interface.  The first palette button found
			// becomes the new active color.  If we failed to find a color
			// then the active color becomes the search color.

         if (first == null) first = new PaletteButton(c,-1) ;
         if (!remove) updateColorInterface(first,true) ;

			// Capture the change for undo/redo processing.

			if (!undoredo)
			{
				UndoableEdit ce = new UndoableSelect(oldselection,getSelection(),
            	oldindex,getActiveIndex()) ;
				UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(evt) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
			}
		}


		// Method to update our color interface for a new active button.   We
      // can optionally disable the color changer updates to retain previously
      // selected colors without change.

		void updateColorInterface(PaletteButton btn, boolean enable)
		{
			if (cc == null) return ;
			if (btn == null)
			{
				Color c = (palette == null && cel != null)
					? cel.getTransparentColor() : null ;
				if (c == null) c = Color.black ;
				btn = new PaletteButton(c,-1) ;
			}
			boolean temp = undoredo ;
			undoredo = enable ;
			active = btn ;
			cc.setColor(getActiveColor()) ;
			updateColorInterface(getActiveColor(),getActiveIndex()) ;
			undoredo = temp ;
		}


		// Method to update our color interface for a new active index.

		void updateColorInterface(int index, boolean enable)
		{
      	if (btn == null) updateColorInterface(null,enable) ;
         else if (index < 0) updateColorInterface(active,enable) ;
         else if (index < btn.length) updateColorInterface(btn[index],enable) ;
     }


		// Method to update our color interface graphic items for a new color.

		void updateColorInterface(Color c, int index)
		{
      	if (c == null) return ;
			int r = c.getRed() ;
			int g = c.getGreen() ;
			int b = c.getBlue() ;
			float [] hsb = Color.RGBtoHSB(r,g,b,null) ;
			int h = (int) (hsb[0] * 360) ;
			int s = (int) (hsb[1] * 100) ;
			int v = (int) (hsb[2] * 100) ;
			COLORPANEL.setBackground(c) ;
			COLORPANEL.setToolTipText(indextext
            + " " + index + " RGB ["+r+","+g+","+b+"]") ;
			HSBTEXT.setText("HSB ("+h+","+s+","+v+")");
			RGBTEXT.setText("RGB ("+r+","+g+","+b+")");
		}


		// Method to reset our button colors to the current palette colors.
      // This function resets the palette display to show the colors from
      // the specified multipalette.

		void reset(Integer multipalette, int index)
      {
			if (btn == null) return ;
			if (palette == null) return ;
			int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
			for (int i = 0 ; i < btn.length ; i++)
			{
				Color c = palette.getColor(mp,i) ;
				btn[i].changeColor(c) ;
			}
			setColor(index) ;
		}


		// Method to select all buttons within a range.

      void selectAll() { selectAll(0,Integer.MAX_VALUE) ; }
		void selectAll(int start, int stop)
		{
			if (btn == null) return ;

         // Clear any existing selection.

			Vector oldselection = getSelection() ;
			int oldindex = getActiveIndex() ;
			for (int i = 0 ; i < selected.size() ; i++)
				((PaletteButton) selected.elementAt(i)).setSelected(false) ;
			selected.removeAllElements() ;

         // Select buttons within the specified range.

         if (start > stop) { int temp = start ; start = stop ; stop = temp ; }
         if (start < 0) start = 0 ;
         if (stop >= btn.length) stop = btn.length - 1;
			for (int i = start ; i <= stop ; i++)
			{
				selected.addElement(btn[i]) ;
				btn[i].setSelected(true) ;
			}

			// Capture the change for undo/redo processing.

			if (!undoredo)
			{
				UndoableEdit ce = new UndoableSelect(oldselection,getSelection(),
            	oldindex,getActiveIndex()) ;
				UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(evt) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
			}
		}


		// Method to select all buttons near the same hue as the active color.
		// This will include the active color as a trivial case.

		void selectHSB()
		{
			if (btn == null) return ;
			if (active == null) return ;
         boolean he = HUE.isEnabled() ;
         boolean se = SATURATION.isEnabled() ;
         boolean be = BRIGHTNESS.isEnabled() ;
         if (!(he || se || be)) return ;

         // Clear any existing selection.

			Vector oldselection = getSelection() ;
			int oldindex = getActiveIndex() ;
			for (int i = 0 ; i < selected.size() ; i++)
				((PaletteButton) selected.elementAt(i)).setSelected(false) ;
			selected.removeAllElements() ;

         // Get the components of the active color.

			Color c = active.getBackground() ;
			int r = c.getRed() ;
			int g = c.getGreen() ;
			int b = c.getBlue() ;
			float hsb[] = Color.RGBtoHSB(r,g,b,null) ;
			float h = hsb[0] ;
			float s = hsb[1] ;
			float v = hsb[2] ;

			// Select all palette colors within the sensitivity range of the
         // hue value.  Hue is a value from 0 to 1.  This value measures
         // rotation around a 360 degree color circle.  Saturation and
         // brightness range from 0 to 100.

			for (int i = 0 ; i < btn.length ; i++)
			{
				c = btn[i].getBackground() ;
				hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),hsb) ;
            float diffhue = hsb[0] - h ;
            float diffsat = hsb[1] - s ;
            float diffbri = hsb[2] - v ;
            if (diffhue < 0) diffhue = -diffhue ;
            if (diffsat < 0) diffsat = -diffsat ;
            if (diffbri < 0) diffbri = -diffbri ;
            float huesensitivity = (hueListener.getSensitivity() / 100.0f) ;
            float satsensitivity = (satListener.getSensitivity() / 100.0f) ;
            float brisensitivity = (briListener.getSensitivity() / 100.0f) ;
				if (((he && diffhue <= huesensitivity) || !he)
            	&& ((se && diffsat <= satsensitivity) || !se)
               && ((be && diffbri <= brisensitivity) || !be))
				{
					selected.addElement(btn[i]) ;
					btn[i].setSelected(true) ;
				}
			}

			// Capture the change for undo/redo processing.

			if (!undoredo)
			{
				UndoableEdit ce = new UndoableSelect(oldselection,getSelection(),
            	oldindex,getActiveIndex()) ;
				UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(evt) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
			}
		}


		// Method to select all buttons near the same RGB as the active color.
		// This will include the active color as a trivial case.

		void selectRGB()
		{
			if (btn == null) return ;
			if (active == null) return ;
         boolean re = RED.isEnabled() ;
         boolean ge = GREEN.isEnabled() ;
         boolean be = BLUE.isEnabled() ;
         if (!(re || ge || be)) return ;

         // Clear any existing selection.

			Vector oldselection = getSelection() ;
			int oldindex = getActiveIndex() ;
			for (int i = 0 ; i < selected.size() ; i++)
				((PaletteButton) selected.elementAt(i)).setSelected(false) ;
			selected.removeAllElements() ;

         // Get the components of the active color.

			Color c = active.getBackground() ;
			int r = c.getRed() ;
			int g = c.getGreen() ;
			int b = c.getBlue() ;

			// Select all palette colors within the sensitivity range of the
         // RGB value.  RGB is a value from 0 to 255.

			for (int i = 0 ; i < btn.length ; i++)
			{
				c = btn[i].getBackground() ;
            int diffred = c.getRed() - r ;
            int diffgrn = c.getGreen() - g ;
            int diffblu = c.getBlue() - b ;
            if (diffred < 0) diffred = -diffred ;
            if (diffgrn < 0) diffgrn = -diffgrn ;
            if (diffblu < 0) diffblu = -diffblu ;
            int redsensitivity = (int) (redListener.getSensitivity() * 2.55f) ;
            int grnsensitivity = (int) (grnListener.getSensitivity() * 2.55f) ;
            int blusensitivity = (int) (bluListener.getSensitivity() * 2.55f) ;
				if (((re && diffred <= redsensitivity) || !re)
            	&& ((ge && diffgrn <= grnsensitivity) || !ge)
               && ((be && diffblu <= blusensitivity) || !be))
				{
					selected.addElement(btn[i]) ;
					btn[i].setSelected(true) ;
				}
			}

			// Capture the change for undo/redo processing.

			if (!undoredo)
			{
				UndoableEdit ce = new UndoableSelect(oldselection,getSelection(),
            	oldindex,getActiveIndex()) ;
				UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
				undo.undoableEditHappened(evt) ;
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
			}
		}


		// ChangeListener interface.  This picks up a color change from the
		// colorchanger and ensures that the selected color selection buttons
		// in the palette panel track the new color.  Color change events are
		// ignored if we are undo or redo processing.

		public void stateChanged(ChangeEvent e)
		{
			if (undoredo) return ;
			if (active == null) return ;
			changecolor = cc.getColor() ;
			Color c2 = active.getBackground() ;
			if (changecolor.equals(c2)) return ;

         // The color changer fires multiple change events as we move our
         // mouse within the HSB panel or type new RGB values.  We do not
         // want to capture all these events.  Color change events are
         // posted so that we only capture the last event.

         if (changetimer == null)
         {
				changetimer = new Timer(500,this) ;
            changetimer.setRepeats(false) ;
         }
         if (changetimer.isRunning())
         	changetimer.restart() ;
         else
         	changetimer.start() ;
      }


		// ActionListener interface.  This applies a color change posted by
      // the changetimer and ensures that the selected color selection
      // buttons in the palette panel track the new color.

      public void actionPerformed(ActionEvent e)
      {
         if (active == null) return ;
			if (changecolor == null) return ;
         boolean he = HUE.isEnabled() ;
         boolean se = SATURATION.isEnabled() ;
         boolean be = BRIGHTNESS.isEnabled() ;
         Color c1 = changecolor ;
			Color c2 = getActiveColor() ;
			CompoundEdit ce = new CompoundEdit() ;

         // Determine the type of change.  We apply a relative change to
         // all selected colors based upon hue, saturation, and brightness.

         float [] hsb1 = Color.RGBtoHSB(c1.getRed(),c1.getGreen(),c1.getBlue(),null) ;
         float [] hsb2 = Color.RGBtoHSB(c2.getRed(),c2.getGreen(),c2.getBlue(),null) ;
         float h = hsb1[0] - hsb2[0] ;
         float s = hsb1[1] - hsb2[1] ;
         float v = hsb1[2] - hsb2[2] ;

         // Apply the new color to all selected palette buttons.  Hue change
         // is cyclical over the 360 degree color circle.  Saturation and
         // brightness are bounded between 0 and 100%.

			for (int i = 0 ; i < selected.size() ; i++)
         {
         	Object o = selected.elementAt(i) ;
            if (!(o instanceof PaletteButton)) continue ;
         	PaletteButton btn = (PaletteButton) o ;
	         Color c = btn.getColor() ;
            if (c == null) continue ;

            // Convert the button color to HSB values and apply the color
				// differential change to the selected button if relative
            // changes are enabled.

	         hsb1 = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),hsb1) ;
            if (he) hsb1[0] += h ;
				if (se) hsb1[1] += s ;
				if (be) hsb1[2] += v ;
				if (hsb1[0] > 1) hsb1[0] -= 1 ;
				if (hsb1[0] < 0) hsb1[0] += 1 ;
				if (hsb1[1] > 1) hsb1[1] = 1 ;
				if (hsb1[1] < 0) hsb1[1] = 0 ;
				if (hsb1[2] > 1) hsb1[2] = 1 ;
				if (hsb1[2] < 0) hsb1[2] = 0 ;
            if (relativechange.isSelected())
					c1 = new Color(Color.HSBtoRGB(hsb1[0],hsb1[1],hsb1[2])) ;
				btn.changeColor(c1) ;

				// Change the palette and flag the object as being updated.

				palette.setColor(multipalette,btn.getIndex(),c1) ;
				palette.setLastModified(System.currentTimeMillis()) ;
				if (palette.isInternal() && cel != null) cel.setUpdated(true) ;
				ce.addEdit(new UndoableColor(multipalette,btn.getIndex(),c1,c)) ;
			}

         // Update the user interface.

			updateColorInterface(changecolor,active.getIndex()) ;
			if (memorysource) update.setEnabled(true) ;
			save.setEnabled(palette != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
			SAVE.setEnabled(palette != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
			changed = true ;
			ce.end() ;

			// Post the change event.

			UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
			undo.undoableEditHappened(evt);
			undoAction.updateUndoState() ;
			redoAction.updateRedoState() ;

			// Reflect the change in the current preview panel.  The palette
         // was invalidated when the color was changed.  All cels referencing
         // this multipalette need to be re-established.

			updateColorMenu(multipalette) ;
         if (preview != null) preview.redraw(multipalette) ;
		}


		// Inner class to define a palette color button.

		class PaletteButton extends JButton
			implements MouseListener
		{
         private int index = 0 ;
			private Color color = Color.black ;
			private boolean select = false ;
			private Dimension minsize = new Dimension(11,7) ;
			private Dimension preferredsize = new Dimension(16,11) ;

			public PaletteButton(Color c, int i)
			{
				super() ;
            index = i ;
            changeColor(c) ;
				addMouseListener(this) ;
			}

         // Return the button signature.  This is a color and index pair.

         public Object [] getSignature()
         {
         	Object o[] = new Object[2] ;
            o[0] = new Color(color.getRGB()) ;
            o[1] = new Integer(index) ;
            return o ;
         }

			// Set our selection state.

			public void setSelected(boolean b)
			{
				select = b ;
				setBorder((select) ? selectedBorder : unselectedBorder) ;

            // Update our menu state.

      		cut.setEnabled(hasSelection()) ;
      		CUT.setEnabled(hasSelection()) ;
      		copy.setEnabled(hasSelection()) ;
      		COPY.setEnabled(hasSelection()) ;
      		paste.setEnabled(hasSelection() && clipboard.getContents(me) != null) ;
      		PASTE.setEnabled(hasSelection() && clipboard.getContents(me) != null) ;
			}

			// Return the minimum size of this button.

			public Dimension getMinimumSize() { return minsize ; }
			public Dimension getPreferredSize() { return preferredsize ; }

			// Return our palette index.

			int getIndex() { return index ; }

			// Return our button color.

			Color getColor() { return color ; }

			// Return our button RGB value.

			int getRGB() { return color.getRGB() ; }

			// Change the button color.

			public void changeColor(Color c)
			{
				if (c == null) return ;
            color = c ;
				setBackground(c) ;
				int r = c.getRed() ;
				int g = c.getGreen() ;
				int b = c.getBlue() ;
				setToolTipText(indextext
               + " " + index + " RGB ["+r+","+g+","+b+"]") ;
            repaint() ;
			}


			// MouseListener interface.  We catch mouse release events to
			// set the appropriate color for editing.

			public void mouseReleased(MouseEvent e) { }
			public void mouseClicked(MouseEvent e) { }

			public void mouseEntered(MouseEvent e)
			{ setBorder(activeBorder); }

			public void mouseExited(MouseEvent e)
			{ setBorder(select ? selectedBorder : unselectedBorder); }

			public void mousePressed(MouseEvent e)
         {
         	if (SwingUtilities.isRightMouseButton(e))
            {
            	setColor(index,true,true) ;
            	return ;
            }
         	if (e.isShiftDown())
            {
            	int activeindex = getActiveIndex() ;
               selectAll(activeindex,index) ;
               return ;
            }
				setColor(index,e.isControlDown(),false) ;
         }
		}
	}


   // Inner class to create a palette preview panel that draws only those
   // cels associated with this palette.

	class PreviewPanel extends ImagePreview
   {
   	private Cel cel = null ;							// Cel to preview
		private Palette palette = null ;					// Palette to preview
      private Integer multipalette = null ;			// Multipalette to view
      private Vector cels = null ;						// Configuration cels
		private Vector celList = null ;    				// Cels using palette
      private Vector loadedcels = null ;				// Cels loaded for preview
      private Dimension panelArea = null ;   		// Our panel size
		private float sf = 1.0f ;							// Preview scale factor
      private boolean show = false ; 					// True, show cel always


      // Constructor

		public PreviewPanel(ActionListener al)
      {
      	super() ;
         AbstractButton cb = getCallback() ;
         if (al != null && cb != null) cb.addActionListener(al) ;
		}

      // Initialize the preview panel.

      void init(Palette p, Cel c, Integer mp)
      {
      	cel = c ;
         palette = p ;
			multipalette = mp ;
			show = false ;
			celList = new Vector() ;
			cels = new Vector() ;
			setImage(null) ;

			// Access the configuration information to customize the panel
			// for our specific data set or cel image.  If we are showing
			// a specific cel, build the panel for that cel.  If we are
			// showing all cels in the configuration, build the panel
			// for the configuration.

			if (cel == null)
         {
				if (config != null)
				{
					panelArea = new Dimension(config.getSize()) ;
					sf = config.getScaleFactor() ;
					panelArea.width = (int) (panelArea.width * sf) ;
		         panelArea.height = (int) (panelArea.height * sf) ;
					cels = config.getCels() ;
	   		}
         }
         else
			{
				show = true ;
				cels.addElement(cel) ;
				Image celimage = cel.getImage() ;
				sf = cel.getScaleFactor() ;
            if (celimage != null)
            {
					int w = celimage.getWidth(null) ;
					int h = celimage.getHeight(null) ;
					panelArea = new Dimension(w,h) ;
            }
			}

   		// Allocate the image buffer.

			if (panelArea == null) panelArea = new Dimension(448,320) ;
			image = createimage(panelArea.width,panelArea.height) ;
			panelArea.width = image.getWidth(null) ;
			panelArea.height = image.getHeight(null) ;

			// Create our cel display list.  Cels must be painted in reverse
			// order to their declaration as the declaration order represents
			// the cel overlay priority.  We are only interested in cels
			// that use our palette.  The cels are set to the multipalette
			// colors in use.

         ArchiveFile zip = null ;
         boolean zipopened = false ;
			for (int i = cels.size()-1; i >= 0; i--)
			{
            boolean loaded = false ;
				c = (Cel) cels.elementAt(i) ;
            
            // Load the cel if required.
            
            if (!c.isLoaded())
            {
               try
               {
                  zip = c.getZipFile() ;
                  if (zip != null && !zip.isOpen())
                  {
                     zipopened = true ;
                     zip.open() ;
                  }
                  c.load() ;
                  loaded = true ;
               }
               catch (Exception e) { }
            }

            // Accept this cel if it references our palette.
            
            if (palette == c.getPalette() || show)
				{
					c.changePalette(multipalette) ;
					celList.addElement(c) ;
               if (loaded)
               {
                  if (loadedcels == null) loadedcels = new Vector() ;
                  loadedcels.add(c) ;
               }
				}
            else if (loaded) c.unload() ;
			}

         // Close any opened zip file.

         if (zipopened)
         {
            try { zip.close(); }
            catch (IOException e) { }
         }

			// Draw the scene.

			Graphics gc = image.getGraphics() ;
			draw(gc,celList,show) ;
         gc.dispose() ;

         // Request that we paint ourselves if we have at least one cel.

			if (celList.size() > 0) 
         {
            setImage(image) ;
         }
		}


		// Create the image buffer.

		private Image createimage(int width, int height)
   	{
   		if (width < 1) width = 1 ;
   		if (height < 1) height = 1 ;
   		if (width > 2000) width = 2000 ;
   		if (height > 2000) height = 2000 ;
//			Image image = me.createImage(width,height) ;
         BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB) ;
			return image ;
		}


      // Set the preview image.  This overrides the ImagePreview method
      // so that we can unload preview cels that may have been loaded.

      void setImage(Image img)
      {
         if (img == null && loadedcels != null)
         {
            for (int i = 0 ; i < loadedcels.size() ; i++)
               ((Cel) loadedcels.elementAt(i)).unload() ;
            loadedcels = null ;
         }
         super.setImage(img) ;
      }


      // Method to set our active palette.  This must be done if the cel
      // palette is replaced.

      void setPalette(Palette p) { palette = p ; }


   	// The draw method is used to paint the required set of cels on the
   	// screen.  This method clears the drawing area and then draws the
   	// cels over a static background.

   	private void draw(Graphics g, Vector celList, boolean show)
   	{
   		// Clear the screen as we must draw everything.

   		if (celList == null || panelArea == null) return ;
         int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
			Rectangle box = new Rectangle(panelArea) ;
         Color c = getBackground() ;

         // We should have BufferedImages so set the background fully 
         // transpareny.  
         
         if (g instanceof Graphics2D)
         {
            Graphics2D g2D = (Graphics2D) g ;
            g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g2D.fill(box);
            g2D.setComposite(AlphaComposite.Src);
         }
         else
         {
            g.setColor(c) ;
            g.fillRect(box.x,box.y,box.width,box.height) ;
         }

			// Set the background color to be the multipalette background
         // color.  Note, this can differ from the page set background
			// color as the pageset background is taken from palette file 0.
			// The background can be a transparent color.  If we show the
         // background we must paint it as an opaque color.

			c = (palette == null) ? null : palette.getBackgroundColor(mp) ;
			if (c == null) c = getBackground() ;
			if (showbackground.isSelected() && c != null)
			{
				g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue())) ;
				g.fillRect(box.x,box.y,box.width,box.height) ;
			}

			// Our preview panel area is at a scaled size.  Cel drawing
			// requires an unscaled bounding box.

			box.x = (int) (box.x / sf) ;
			box.y = (int) (box.y / sf) ;
			box.width = (int) (box.width / sf) ;
			box.height = (int) (box.height / sf) ;

			// The picture is built up one cel at a time from the lowest
			// priority cel to the highest.  Only visible cels are drawn
			// unless we have explicity requested that all cels be shown.

   		for (int i = 0; i < celList.size(); i++)
   		{
   			Cel cel = (Cel) celList.elementAt(i) ;
				boolean visible = cel.isVisible() ;
				if (show) cel.setVisibility(true) ;
				cel.draw(g,box) ;
				cel.setVisibility(visible) ;
   		}
   	}


		// Method to redraw the image for a new multipalette.  If a color
		// has changed the palette will have been invalidated and all cels
      // using the associated multipalette must be re-established.

		void redraw(Integer mp)
		{
      	multipalette = mp ;
         int n = (celList == null) ? -1 : celList.size() ;
			if (celList == null) return ;

         // Update the cels to reflect the colors in the new multipalette.

			for (int i = 0 ; i < celList.size() ; i++)
			{
   			Cel cel = (Cel) celList.elementAt(i) ;
           	if (cel.getPalette() == palette)
					cel.changePalette(multipalette) ;
			}

         // Redraw the image and update the preview display.

      	if (image != null) 
         {
				Graphics gc = image.getGraphics() ; 
				draw(gc,celList,show) ;
	         gc.dispose() ;
	         drawImage() ;
         }
      }


		// Method to return the color of the preview image at a specific point.
		// This method will return the actual color from the cel drawn at the
		// point location.  We do not use the preview image.  This image was
		// drawn from the cels and alpha compositing of transparent colors can
		// yeild a non-palette color.

		Color getColor(int x, int y)
		{
			if (celList == null) return null ;
			Rectangle box = new Rectangle(x,y,1,1) ;

         // Find the top cel that contains the point.

         for (int i = celList.size()-1 ; i >= 0 ; i--)
         {
         	Cel cel = (Cel) celList.elementAt(i) ;
				if (!(show || cel.isVisible())) continue ;

				// We find a cel if our point intersects the cel bounding box
				// and the color is not transparent.

				int [] pixels = cel.getPixels(box) ;
				if (pixels == null) continue ;
				if (pixels.length == 0) continue ;

            // Extract the pixel color components.  Note that pixels with
				// RGB (0,0,1) are converted to (0,0,0).

				int a = (pixels[0] & 0xff000000) >> 24 ;
				int r = (pixels[0] & 0xff0000) >> 16 ;
            int g = (pixels[0] & 0xff00) >> 8 ;
            int b = (pixels[0] & 0xff) ;
				if (a == 0) continue ;
            if (r == 0 && g == 0 && b == 1) b = 0 ;
	         Color c = new Color(r,g,b) ;
				return c ;
         }
         return null ;
		}

		// Method to return the pixel selected.  This is adjusted for the
		// preview scale factor.  There are problems with pixel color pickup
      // if the cel has been scaled.

		Point getPixel()
		{
			Point p = super.getPixel() ;
			p.x = Math.round(p.x / sf) ;
			p.y = Math.round(p.y / sf) ;
			return p ;
		}
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
			if (pp == null) return ;
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.undo(); }
			catch (CannotUndoException ex)
			{
				PrintLn.println("ColorFrame: Unable to undo edit") ;
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
			Vector selected = pp.getSelection() ;
			int active = pp.getActiveIndex() ;
			pp.reset(multipalette,active) ;

			// Establish our original selection buttons.

			for (int i = 0 ; i < selected.size() ; i++)
			{
				Object [] o = (Object []) selected.elementAt(i) ;
				if (o[1] instanceof Integer)
					pp.setColor(((Integer) o[1]).intValue(),(i > 0)) ;
			}

			// Reflect the change in the current preview pane.

			pp.updateColorInterface(active,undoredo) ;
			if (preview != null) preview.redraw(multipalette) ;

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
				reload.setEnabled(palette != null) ;
            UNDO.setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName()) ;
			}
			else
			{
				setEnabled(false) ;
				reload.setEnabled(false) ;
				UNDO.setEnabled(false);
				putValue(Action.NAME, Kisekae.getCaptions().getString("MenuEditUndo")) ;
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
			if (pp == null) return ;
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.redo() ; }
			catch (CannotRedoException ex)
			{
				PrintLn.println("ColorFrame: Unable to redo edit") ;
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
			Vector selected = pp.getSelection() ;
			int active = pp.getActiveIndex() ;
			pp.reset(multipalette,active) ;

			// Establish our original selection buttons.

			for (int i = 0 ; i < selected.size() ; i++)
			{
				Object [] o = (Object []) selected.elementAt(i) ;
				if (o[1] instanceof Integer)
					pp.setColor(((Integer) o[1]).intValue(),(i > 0)) ;
			}

			// Reflect the change in the current preview pane.

			pp.updateColorInterface(active,undoredo) ;
			if (preview != null) preview.redraw(multipalette) ;

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


	// Inner class to construct an undoable palette color change.

	class UndoableColor extends AbstractUndoableEdit
	{
		private Integer multipalette = null ;
		private Color newcolor = null ;
		private Color oldcolor = null ;
		private int index = -1 ;

		// Constructor

      public UndoableColor(Integer mp, int index, Color c1, Color c2)
      {
         this.index = index ;
         this.newcolor = c1 ;
         this.oldcolor = c2 ;
         this.multipalette = mp ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoColorEditName") ; }

      // Undo a color change.

      public void undo()
      {
			super.undo() ;
			updateColor(multipalette,index,oldcolor) ;
		}

      // Redo a color change.

      public void redo()
      {
			super.redo() ;
			updateColor(multipalette,index,newcolor) ;
		}

      // Utility function to update a palette color for the appropriate
      // multipalette in use.

      private void updateColor(Integer multipalette, int index, Color c)
      {
         // Update the multipalette panel display and the actual palette data.

         this.multipalette = multipalette ;
         if (multipalette == null) multipalette = new Integer(0) ;
			MULTIPALETTES.setSelectedItem(multipalette) ;
			if (palette != null) palette.setColor(multipalette,index,c) ;
         changed = true ;
		}
   }


	// Inner class to construct an undoable color frame color selection.

	class UndoableSelect extends AbstractUndoableEdit
	{
		private int oldindex = -1 ;
		private int newindex = -1 ;
		private Vector newselection = null ;
		private Vector oldselection = null ;

		// Constructor

		public UndoableSelect(Vector v1, Vector v2, int oldindex, int newindex)
		{
			this.oldindex = oldindex ;
			this.newindex = newindex ;
			oldselection = v1 ;
			newselection = v2 ;
		}

      // Return the undo/redo menu name

      public String getPresentationName()
		{ return Kisekae.getCaptions().getString("UndoColorSelectName") ; }

		// Undo a color selection.

      public void undo()
      {
			super.undo() ;
			updateColorSelection(oldselection,oldindex) ;
		}

		// Redo a color selection.

      public void redo()
      {
			super.redo() ;
			updateColorSelection(newselection,newindex) ;
		}

      // Utility function to update a palette color for the appropriate
      // multipalette in use.

		private void updateColorSelection(Vector v, int active)
		{
			if (pp == null) return ;
			undoredo = true ;
			pp.setSelection(v) ;
//			pp.updateColorInterface(active,undoredo) ;
         pp.setColor(active,true,(active < 0));
			undoredo = false ;
		}
   }


   // Inner class to construct an undoable palette data edit operation.  We
   // copy our data arrays to ensure we retain consistency.

   class UndoablePalette extends AbstractUndoableEdit
   {
   	private String name = null ;
		private Cel cel = null ;
		private Palette palette = null ;
      private Integer multipalette = null ;
      private Image oldimage = null ;
      private Image newimage = null ;
      private Palette oldpalette = null ;
      private Palette newpalette = null ;
		private Object [] olddata = null ;
		private Object [] newdata = null ;
		private int oldcolors = 0 ;
		private int newcolors = 0 ;
		private int oldmp = 0 ;
		private int newmp = 0 ;
		private int oldbackground = 0 ;
		private int newbackground = 0 ;
		private int oldtransparent = 0 ;
		private int newtransparent = 0 ;


		// Constructor for palette color updates

		public UndoablePalette(String name, Palette p, Integer multipalette,
      	Object [] olddata, int oldmp, int oldcolors, int oldbackground, int oldtransparent,
			Object [] newdata, int newmp, int newcolors, int newbackground, int newtransparent)
      {
      	this.name = name ;
			this.palette = p ;
         this.multipalette = multipalette ;
         this.olddata = olddata ;
         this.oldmp = oldmp ;
			this.oldcolors = oldcolors ;
			this.oldbackground = oldbackground ;
			this.oldtransparent = oldtransparent ;
         this.newdata = newdata ;
         this.newmp = newmp ;
         this.newcolors = newcolors ;
			this.newbackground = newbackground ;
			this.newtransparent = newtransparent ;

         if (olddata != null)
         {
            for (int i = 0 ; i < olddata.length ; i++)
            {
            	Object o = olddata[i] ;
               if (o == null) continue ;
            	byte [] b = (byte []) o ;
               byte [] copy = new byte [b.length] ;
               for (int j = 0 ; j < b.length ; j++) copy[j] = b[j] ;
            	olddata[i] = copy ;
            }
         }

         if (newdata != null)
         {
            for (int i = 0 ; i < newdata.length ; i++)
            {
            	Object o = newdata[i] ;
               if (o == null) continue ;
            	byte [] b = (byte []) o ;
               byte [] copy = new byte [b.length] ;
               for (int j = 0 ; j < b.length ; j++) copy[j] = b[j] ;
            	newdata[i] = copy ;
            }
         }
		}

      // Constructor for palette and cel size changes.

		public UndoablePalette(String name, Cel c, Palette p, Integer multipalette,
         Image oldimage, Palette oldpalette, Object [] olddata, int oldmp, int oldcolors, int oldbackground, int oldtransparent, 
         Image newimage, Palette newpalette, Object [] newdata, int newmp, int newcolors, int newbackground, int newtransparent)
      {
         this(name,p,multipalette,
            olddata,oldmp,oldcolors,oldbackground,oldtransparent,
			   newdata,newmp,newcolors,newbackground,newtransparent) ;
         this.cel = c ;
         this.oldimage = oldimage ;
         this.newimage = newimage ;
         this.oldpalette = oldpalette ;
         this.newpalette = newpalette ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return name ;  }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         undoredo = true ;
			updatePalette(cel,palette,multipalette,oldimage,oldpalette,olddata,
            oldmp,oldcolors,oldbackground,oldtransparent) ;
         undoredo = false ;
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         undoredo = true ;
			updatePalette(cel,palette,multipalette,newimage,newpalette,newdata,
            newmp,newcolors,newbackground,newtransparent) ;
         undoredo = false ;
		}

      // Utility function to apply a color update to a palette.

		private void updatePalette(Cel c, Palette p, Integer oldmultipalette,
      	Image image, Palette palette, Object [] data, int mp, int colors,
         int background, int transparent)
		{

         // Cel changes result from image dithering.
         // Ensure the cel tracks the changed palette transparent color.

         if (c != null)
         {
            c.setImage(image) ;
            c.setPalette(palette) ;
            c.setTransparentIndex(transparent) ;
            setPalette(palette) ;
         }

         // Palette changes result from palette updates.

         if (data != null && p != null)
         {
            int m = Math.min(colors,256) ;
            byte [] a = (byte []) data[0] ;
            byte [] r = (byte []) data[1] ;
            byte [] g = (byte []) data[2] ;
            byte [] b = (byte []) data[3] ;
            p.setPalette(a,r,g,b,mp,m,background,transparent) ;
         }

         // Adjust our display interface.

         multipalette = oldmultipalette ;
         if (preview != null) preview.init(palette,c,multipalette) ;
         updateInterface(multipalette) ;
      }
	}


   // Inner class to construct an undoable cel transparency operation.

   class UndoableCel extends AbstractUndoableEdit
   {
   	private String name = null ;
		private Cel cel = null ;
		private Palette palette = null ;
      private Integer multipalette = null ;
		private int oldtransparent = 0 ;
		private int newtransparent = 0 ;


		// Constructor for cel transparency updates

		public UndoableCel(String name, Cel c, Palette p, Integer multipalette,
      	int oldtransparent, int newtransparent)
      {
      	this.name = name ;
         this.cel = c ;
         this.palette = p ;
         this.multipalette = multipalette ;
			this.oldtransparent = oldtransparent ;
			this.newtransparent = newtransparent ;
		}

      // Return the undo/redo menu name

      public String getPresentationName()
      { return name ;  }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         undoredo = true ;
			updateCel(cel,palette,multipalette,oldtransparent) ;
         undoredo = false ;
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         undoredo = true ;
			updateCel(cel,palette,multipalette,newtransparent) ;
         undoredo = false ;
		}

      // Utility function to apply a transparency update to a cel.

		private void updateCel(Cel c, Palette p, Integer multipalette, int transparent)
		{
         if (p != null)
         {
            p.setTransparentIndex(transparent);
         }
         if (c != null)
         {
            c.setTransparentIndex(transparent) ;
            c.changeTransparency(0) ;
         }
         updateInterface(multipalette) ;
      }
	}


   // Inner class to construct an undoable cel transparency operation.

   class UndoableImage extends AbstractUndoableEdit
   {
   	private String name = null ;
		private Cel cel = null ;
		private Image oldimage = null ;
		private Image newimage = null ;


		// Constructor for cel transparency updates

		public UndoableImage(String name, Cel c, Image oldimg, Image newimg)
      {
      	this.name = name ;
         this.cel = c ;
         this.oldimage = oldimg ;
         this.newimage = newimg ;
		}

      // Return the undo/redo menu name

      public String getPresentationName()
      { return name ;  }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         undoredo = true ;
			updateImage(cel,oldimage) ;
         undoredo = false ;
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         undoredo = true ;
			updateImage(cel,newimage) ;
         undoredo = false ;
		}

      // Utility function to apply a transparency update to a cel.

		private void updateImage(Cel c, Image image)
		{
         if (c != null)
         {
            c.setImage(image) ;
 
            // Adjust our display interface.

            preview.setShowState(false) ;
            preview.updateImage(image) ;
            preview.setShowState(true) ;
            icp.setImage(image) ;
            icp.reset() ;
        }
      }
	}


	// Inner class to construct a transferable color edit object.  This object
	// is placed on the clipboard for cut, copy and paste operations.  It is
	// an actual vector of copied PaletteButtons.

   class ColorEdit extends Vector
   	implements Transferable
	{
		private String df = DataFlavor.javaSerializedObjectMimeType ;

		// Constructor

		public ColorEdit() { super() ; }
		public ColorEdit(Collection c) { super(c) ; }

		// Transferable interface method to return the transfer data.   We use
		// a Java serialized object type.

		public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
      {
			if (!flavor.isMimeTypeEqual(df))
         	throw new UnsupportedFlavorException(flavor) ;
			return this ;
      }

		// Transferable interface method to return our transfer data flavor.

		public DataFlavor[] getTransferDataFlavors()
		{
			DataFlavor flavors[] = new DataFlavor[1] ;

			try { flavors[0] = new DataFlavor(df) ; }
			catch (ClassNotFoundException e)
			{
				PrintLn.println("ColorFrame: DataFlavor " + df) ;
				PrintLn.println(e.getMessage()) ;
			}
			return flavors ;
		}

      // Transferable interface method to check if we support our data flavor.

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{ return flavor.isMimeTypeEqual(df) ; }
	}


   // Inner class to construct a mouse listener for HSB and RBG selection
   // buttons.

   class SelectListener extends MouseAdapter
   {
      private Timer timer = null ;
      private JButton button = null ;
      private String classname = null ;
   	private int interval = 0 ;
      private int sensitivity = 0 ;

      // Constructor

      public SelectListener(JButton source, String name, int initial)
      {
      	button = source ;
         classname = name ;
         sensitivity = initial ;
      }


      // Attribute methods.

      int getSensitivity() { return sensitivity ; }
      void setSensitivity(int n)
      {
      	sensitivity = n ;
         String s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + sensitivity + s1.substring(j1+1) ;
			button.setToolTipText(s1) ;
      }


      // Utility function to update linked sensitivties.

      private void updateSensitivity()
      {
         if ("HSB".equals(classname))
         {
            hsbslider.setValue(sensitivity) ;
            if (hsblinked.isSelected())
            {
               hueListener.setSensitivity(sensitivity) ;
               satListener.setSensitivity(sensitivity) ;
               briListener.setSensitivity(sensitivity) ;
            }
         }
         if ("RGB".equals(classname))
         {
            rgbslider.setValue(sensitivity) ;
            if (rgblinked.isSelected())
            {
               redListener.setSensitivity(sensitivity) ;
               grnListener.setSensitivity(sensitivity) ;
               bluListener.setSensitivity(sensitivity) ;
            }
         }
      }


		// Window to display our button tooltip text.  This window tracks
		// selection sensitivity changes.

      JWindow window = new JWindow()
      {
      	public void paint(Graphics g)
         {
         	if (button == null) return ;
         	Dimension d = getSize() ;
         	String text = button.getToolTipText() ;
            if (text == null) return ;
            FontMetrics fm = getFontMetrics(getFont()) ;
            g.setColor(Color.white) ;
            g.fillRect(0,0,d.width,d.height) ;
            g.setColor(Color.black) ;
            g.drawRect(0,0,d.width-1,d.height-1) ;
            g.drawString(text,3,fm.getAscent()+1) ;
         }
      } ;

      // Timer action event to update the increment interval.

      ActionListener buttonAction = new ActionListener()
      {
      	public void actionPerformed(ActionEvent e)
         {
         	sensitivity += interval ;
            if (sensitivity < 0) sensitivity = 0 ;
            if (sensitivity > 100) sensitivity = 100 ;
            String s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + sensitivity + s1.substring(j1+1) ;
				button.setToolTipText(s1) ;
            updateSensitivity() ;
            window.repaint() ;
         }
      } ;

		public void mousePressed(MouseEvent e)
		{
			Object source = e.getSource() ;
			if (source != button) return ;

         // On a right mouse button down, enable or disable the button.

         if (SwingUtilities.isRightMouseButton(e))
         	button.setEnabled(!button.isEnabled()) ;

         // Process only if enabled and left button press.

         if (SwingUtilities.isRightMouseButton(e)) return ;
         if (!button.isEnabled()) return ;

         // Get mouse down position.  If we are in the upper right third of
         // the button we expand the selection sensitivity.  If we are
         // in the lower right third of the button we reduce the selection
         // sensitivity.

			int x = e.getX() ;
			int y = e.getY() ;
			int w = button.getSize().width ;
			int h = button.getSize().height ;
			int px = button.getLocation().x ;
			int py = button.getLocation().y ;
			if (x < 2*w/3) return ;

         // Establish our increment interval and perform an initial
         // update.  Start a timer to continuously update the increment
         // until the mouse is released.

         interval = (y < h/2) ? 2 : -2 ;
        	sensitivity += interval ;
         if (sensitivity < 0) sensitivity = 0 ;
         if (sensitivity > 100) sensitivity = 100 ;
         String s1 = Kisekae.getCaptions().getString("ToolTipSensitivity") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + sensitivity + s1.substring(j1+1) ;
			button.setToolTipText(s1) ;
         if (timer == null) timer = new Timer(350,buttonAction) ;
         if (!timer.isRunning()) timer.start() ;
         updateSensitivity() ;

         // Show the increment tooltip window.

         window.setSize(150,20) ;
         window.setLocation(px,py+h+50) ;
         window.setVisible(true) ;
         return ;
		}

		public void mouseReleased(MouseEvent e)
		{
			Object source = e.getSource() ;
			if (source != button) return ;
			if ("HSB".equals(classname)) activeHSB = button ;
			if ("RGB".equals(classname)) activeRGB = button ;

         // On a mouse release, if we have a selection timer running
         // then stop the selection interval updates.

         if (timer != null && timer.isRunning())
         {
         	timer.stop() ;
            timer = null ;
			}

         // Process only if enabled and left button release.

         if (!SwingUtilities.isLeftMouseButton(e)) return ;
         if (!button.isEnabled()) return ;

         // Initiate a selection action.

         window.setVisible(false) ;
			if (pp == null) return ;
			if ("HSB".equals(classname)) pp.selectHSB() ;
			if ("RGB".equals(classname)) pp.selectRGB() ;
         return ;
		}
   }
}
