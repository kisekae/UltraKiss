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
* ImageFrame Class
*
* Purpose:
*
* This object is a generalized image editor.  It is used to edit
* various image files for use in the Kisekae application.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.geom.* ;
import java.awt.image.* ;
import java.awt.color.*;
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
import java.awt.image.BufferedImage;


final class ImageFrame extends KissFrame
	implements ActionListener, ItemListener, WindowListener, ClipboardOwner
{
	private final int TOP_BOTTOM = 1 ;
	private final int LEFT_RIGHT = 2 ;
	private final int ROTATELEFT90 = 3 ;
	private final int ROTATERIGHT90 = 4 ;
	private final int ROTATE180 = 5 ;
	private final int BLUR = 6 ;
	private final int SHARPEN = 7 ;
	private final int SOBELHORIZ = 8 ;
	private final int SOBELVERT = 9 ;
	private final int ROBERTSHORIZ = 10 ;
	private final int ROBERTSVERT = 11 ;
	private final int INVERT = 12 ;
	private final int BRIGHTNESS = 13 ;
	private final int CONTRAST = 14 ;
	private final int GAMMA = 15 ;
	private final int POSTERIZE = 16 ;
	private final int RED = 17 ;
	private final int GREEN = 18 ;
	private final int BLUE = 19 ;
	private final int EDGEDETECT = 20 ;
	private final int ALPHA = 21 ;

	private static String helpset = "Help/ImageEditor.hs" ;
	private static String helpsection = "imageeditor.index" ;
	private static String onlinehelp = "imageeditor/index.html" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;

	// Image frame interface objects

	private ImageFrame me = null ;					// Reference to ourselves
	private Cel cel = null ;							// Graphic cel loaded
	private Cel writecel = null ;						// Graphic cel written
	private Group group = null ;						// Graphic group loaded
	private Palette palette = null ;					// Palette being adjusted
	private Palette fwpalette = null ;				// Palette being written
	private Object configobject = null ;			// Configuration object
	private ArchiveEntry ze = null ;					// Zip entry for palette
	private Configuration config = null ;  		// Active configuration
   private PreviewPanel preview = null ;        // Color preview panel
	private Integer multipalette = null ; 			// Multipalette in use
	private Clipboard clipboard = null ;			// Our internal clipboard
   private String originalzepath = null ;			// Zip entry name before save
   private String originalcelname = null ;		// Cel name before save
	private String originalpalettename = null ;	// Palette name before save
	private Point originallocation = null ;	   // Cel original location
   private Object currentframe = null ;         // Current cel frame on display
   private Image baseimage = null ;             // Our base transform image
   private Image oldimage = null ;              // Our undo original image
   private Image entryimage = null ;            // Our undo original image
   private Image workimage = null ;             // Our work in progress image
   private Palette oldpalette = null ;          // Our undo original palette
   private BufferedImage image = null ;         // Our buffered transform image
   private String lastchange = null ;           // Our last edit change type
   private Point clipoffset = null ;            // Our selection clip (x,y)
   private RenderingHints hints = null ;        // Default rendering hints
   private Vector windows = null ;              // Active edit windows
   private WindowImageItem window = null ;      // Active window item
	private boolean changed = false ;				// True, palette has changed
	private boolean memorysource = false ;  		// True, palette is active
	private boolean updated = false ;				// True, memory is updated
   private boolean undoredo = false ;				// True, performing an undo
   private boolean entrystate = false ;			// Initial file update state
   private boolean animatestate = false ;			// Initial cel animation state

	// User interface objects

	private JPanel panel1 = new JPanel() ;  		// Main container panel
	private JPanel panel2 = new JPanel() ;			// Color changer and palette
	private JPanel panel3 = new JPanel() ; 		// Image editor main panel
	private JPanel panel4 = new JPanel() ; 		// unused
	private JPanel panel5 = new JPanel() ; 		// Preview panel
  	private JPanel panel6 = new JPanel() ; 		// Frame panel
	private JPanel northpanel = null ; 		      // Identifier north panel
	private JPanel southpanel = null ; 		      // Identifier south panel
	private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private Border eb2 = BorderFactory.createEmptyBorder(0,0,0,0) ;
	private Border eb3 = BorderFactory.createEmptyBorder(10,0,0,0) ;
	private Border eb4 = BorderFactory.createEmptyBorder(0,0,0,5) ;
   private Border cb2 = new CompoundBorder(eb4,new EtchedBorder()) ;
   private JList framelist = new JList() ;
   private StatusBar statusbar = null ;
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;
   private ImageColorPanel colorpanel = new ImageColorPanel(this) ;   // Our color adjust pane
   private ImageGeomPanel geompanel = new ImageGeomPanel(this) ;     // Our geometry adjust pane

	// Undo helpers

	private UndoManager undo = new UndoManager() ;
	private UndoAction undoAction ;					// Action for undo
	private RedoAction redoAction ;					// Action for redo

	// Menu items

   private JMenu fileMenu ;
   private JMenu editMenu ;
	private JMenu windowMenu ;
	private JMenu helpMenu ;
	private JMenu newmenu ;
	private JMenu newfile ;
	private JMenu colorMenu ;

	private JMenu coloradjust ;
	private JMenuItem brightness ;
	private JMenuItem contrast ;
	private JMenuItem gamma ;
	private JMenuItem red ;
	private JMenuItem green ;
	private JMenuItem blue ;
	private JMenuItem alpha ;

	private JMenu colorhistogram ;
	private JMenuItem countcolors ;
	private JMenuItem equalize ;
	private JMenuItem normalize ;
	private JMenu colorreduce ;
	private JMenuItem grayscale ;
	private JMenuItem colorcount ; 
	private JMenuItem errordiffusion ;

	private JMenu geometryMenu ;
	private JMenuItem mirror ;
	private JMenuItem flip ;
	private JMenuItem crop ;
	private JMenuItem rotatefree ;
	private JMenuItem rotateleft90 ;
	private JMenuItem rotateright90 ;
	private JMenuItem rotate180 ;
	private JMenuItem scale ;
	private JMenuItem shear ;

	private JMenu filterMenu ;
	private JMenuItem blur ;
	private JMenuItem sharpen ;
	private JMenuItem invert ;
	private JMenuItem posterize ;
	private JMenuItem open ;
	private JMenuItem close ;
	private JMenuItem save ;
	private JMenuItem saveas ;
	private JMenuItem cut ;
	private JMenuItem copy ;
	private JMenuItem paste ;
	private JMenuItem undoall ;
	private JMenuItem print ;
   private JMenuItem printpreview ;
   private JMenuItem pagesetup ;
   private JMenuItem properties ;
	private JMenuItem exit ;
	private JMenuItem help ;
	private JMenuItem logfile ;
	private JMenuItem about ;

	private JMenu viewMenu ;
	private JCheckBoxMenuItem statusbaritem ;
   
	private JMenu optionMenu ;
	private JCheckBoxMenuItem editwindow ;
	private JCheckBoxMenuItem framewindow ;
	private JCheckBoxMenuItem showbackground ;
	private JCheckBoxMenuItem showmarquee ;
	private JCheckBoxMenuItem showoverlay ;

   private JMenu rendering ;
   private JCheckBoxMenuItem renderquality ;
   private JCheckBoxMenuItem renderspeed ;
   private JCheckBoxMenuItem renderdefault ;

   private JMenu dithering ;
   private JCheckBoxMenuItem ditherdisable ;
   private JCheckBoxMenuItem ditherenable ;
   private JCheckBoxMenuItem ditherdefault ;

   private JMenu interpolation ;
	private JCheckBoxMenuItem nearneighbour ;
	private JCheckBoxMenuItem bilinear ;
	private JCheckBoxMenuItem bicubic ;

   private JMenu antialiasing ;
   private JCheckBoxMenuItem antialiasingon ;
   private JCheckBoxMenuItem antialiasingoff ;
   private JCheckBoxMenuItem antialiasingdefault ;

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

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the palette file is
   // saved.

   private ActionListener writelistener = null ;
	protected CallbackButton callback = new CallbackButton(this,"ImageFrame Callback") ;

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
      }
	} ;


	// Register for GIF frame selection events.

	ListSelectionListener gifFrameListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
		{
         if (cel == null) return ;
			Object o = framelist.getSelectedValue() ;
         if (o == currentframe) return ;
         currentframe = o ;
         int n = updateGifInterface(cel,o) ;
         cel.setFrame(n) ;
   		preview.redraw() ;
		}
	} ;

	// Register for group cel selection events.

	ListSelectionListener groupListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
		{
         if (group == null) return ;
			Object o = framelist.getSelectedValue() ;
         if (o == currentframe) return ;
         currentframe = o ;
         int n = updateGroupInterface(group,o) ;
         if (n < 0 || showoverlay.isSelected())
         {
            group.restoreState(config.getID(),"imageframe",true) ;
      		preview.redraw() ;
         }
         if (n >= 0)
         {
            group.setFrame(n) ;
     		   preview.redraw(!showoverlay.isSelected()) ;
         }
		}
	} ;


	// Constructor for zip file entries.  This constructor is used when we
   // wish to edit image files that exist in an archive file and are
   // not memory resident as part of the currently active configuration.

	public ImageFrame() { this(null) ; }

	public ImageFrame(ArchiveEntry ze, ActionListener al)
	{ this(ze) ; writelistener = al ; }

	public ImageFrame(ArchiveEntry ze)
	{
		super(Kisekae.getCaptions().getString("ImageEditorTitle")) ;
		me = this ;
		this.ze = ze ;
		config = null ;
		memorysource = false ;
		setIconImage(Kisekae.getIconImage()) ;

		// If we have a ZipEntry, open it and find the cel object.
		// We will load the cel object and any associated palette object.

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

		// Initialize to display this cel.

      windows = new Vector() ;
      window = new WindowImageItem(configobject,cel,palette) ;
      if (cel != null) windows.addElement(window) ;
      window.addItemListener(me) ;
		init() ;
	}


	// Constructor for configuration image entries.  This constructor is used
   // when we have an active configuration and wish to edit a memory copy of
	// an object.  This object can be a KiSS cel object.  We retain the initial
   // object update state in case we cancel any edits on exit.

	public ImageFrame(Configuration c, Object o, Integer mp)
   {
   	super(Kisekae.getCaptions().getString("ImageEditorTitle")) ;
   	me = this ;
		config = c ;
		configobject = o ;
		multipalette = mp ;
		memorysource = true ;

		// For cel configuration entries we must identify the associated
		// palette object.  The cel is cloned as we adjust the location
		// attributes and modify a copy of the cel. The cloned cel is placed
      // at the origin.

		if (o instanceof Cel)
		{
			cel = (Cel) o ;
			cel = (Cel) cel.clone() ;
			entrystate = cel.isUpdated() ;
         entryimage = cel.getImage() ;
         animatestate = cel.getAnimate() ;
			palette = cel.getPalette() ;
         cel.setGroup(group) ;
         cel.setAnimate(0) ;
      	originallocation = cel.getLocation() ;
         cel.setLocation(new Point(cel.getLocation())) ;
         cel.setOffset(new Point(cel.getOffset())) ;
         cel.setSize(new Dimension(cel.getSize())) ;
         cel.setVisibility(true) ;
			setAtOrigin(cel) ;
			group = new Group() ;
         group.setInternal(true) ;
         group.addElement(cel) ;
         group = null ;
		}

		// For group configuration entries we must layer the associated
		// cel objects.  We clone all cels in the complete group.

		if (o instanceof Group)
		{
         Group og = (Group) o ;
         Vector cels = og.getCels() ;
			group = new Group() ;
         group.setInternal(true) ;
         group.setIdentifier(og.getIdentifier()) ;
         group.setPlacement(og.getLocation().x,og.getLocation().y) ;
         group.drop() ;

         // Clone the group cels.

         if (cels != null)
         {
            Enumeration enum1 = cels.elements() ;
            while (enum1.hasMoreElements())
            {
               Cel cel = (Cel) enum1.nextElement() ;
               cel = (Cel) cel.clone() ;
               cel.setLocation(new Point(cel.getLocation())) ;
               cel.setOffset(new Point(cel.getOffset())) ;
               cel.setSize(new Dimension(cel.getSize())) ;
               group.addElement(cel) ;
            }
         }

         // Set the group at the origin.

         group.updateCelPlacement() ;
			setAtOrigin(group) ;
         group.saveState(config.getID(),"imageframe") ;
		}

      // Initialize.

      windows = new Vector() ;
      window = new WindowImageItem(configobject,cel,palette) ;
      if (cel != null) windows.addElement(window) ;
      window.addItemListener(me) ;
		init() ;
	}


   // Frame initialization.

	private void init()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		setTitle(Kisekae.getCaptions().getString("ImageEditorTitle")) ;
		setIconImage(Kisekae.getIconImage()) ;

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      	helper = new HelpLoader(this,helpset,helpsection) ;

		// Create our internal clipboard for color transfers.  The ImageFrame
		// object is the clipboard owner.

		clipboard = new Clipboard("Image Frame") ;

		// Set up the menu bar.

		JMenuBar mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
		String s = System.getProperty("java.version") ;
		int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
		Insets insets = new Insets(2,2,2,rm) ;
		fileMenu.setMargin(insets) ;
		if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
		open.addActionListener(this) ;
		fileMenu.add((close = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClose")))) ;
		close.addActionListener(this) ;
		fileMenu.add((save = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSave")))) ;
		save.addActionListener(this) ;
		save.setEnabled((cel != null && cel.getName() != null) && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((saveas = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAs")))) ;
		saveas.addActionListener(this) ;
		saveas.setEnabled(cel != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((pagesetup = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePageSetup")))) ;
		pagesetup.addActionListener(this) ;
      pagesetup.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((printpreview = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrintPreview")))) ;
		printpreview.addActionListener(this) ;
      printpreview.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((print = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrint")))) ;
		print.addActionListener(this) ;
      print.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((properties = new JMenuItem(Kisekae.getCaptions().getString("MenuFileProperties")))) ;
		properties.setEnabled(false) ;
		properties.addActionListener(this) ;
		fileMenu.addSeparator();
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuitImageEditor" : "MenuFileExitImageEditor" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;

		// Edit menu.

		editMenu = createEditMenu() ;
		editMenu.setMargin(insets) ;
		mb.add(editMenu) ;

		// Color menu.

		colorMenu = new JMenu("Color") ;
		colorMenu.setMargin(insets) ;
		if (!applemac) colorMenu.setMnemonic(KeyEvent.VK_C) ;
		mb.add(colorMenu);
		colorMenu.add((coloradjust = new JMenu("Adjust"))) ;
		coloradjust.add((brightness = new JMenuItem("Brightness..."))) ;
      brightness.addActionListener(this) ;
		coloradjust.add((contrast = new JMenuItem("Contrast..."))) ;
      contrast.addActionListener(this) ;
		coloradjust.add((gamma = new JMenuItem("Gamma..."))) ;
      gamma.addActionListener(this) ;
		coloradjust.add((red = new JMenuItem("Red..."))) ;
      red.addActionListener(this) ;
		coloradjust.add((green = new JMenuItem("Green..."))) ;
      green.addActionListener(this) ;
		coloradjust.add((blue = new JMenuItem("Blue..."))) ;
      blue.addActionListener(this) ;
		coloradjust.add((alpha = new JMenuItem("Alpha..."))) ;
      alpha.addActionListener(this) ;
		colorMenu.add((colorhistogram = new JMenu("Histogram"))) ;
		colorhistogram.setEnabled(false) ;
		colorhistogram.add((countcolors = new JMenuItem("Count Colors..."))) ;
      countcolors.addActionListener(this) ;
		colorhistogram.add((equalize = new JMenuItem("Equalize"))) ;
      equalize.addActionListener(this) ;
		colorhistogram.add((normalize = new JMenuItem("Normalize"))) ;
      normalize.addActionListener(this) ;
		colorMenu.add((colorreduce = new JMenu("Reduce"))) ;
      colorreduce.addActionListener(this) ;
		colorreduce.add((grayscale = new JMenuItem("Gray Scale..."))) ;
      grayscale.addActionListener(this) ;
		colorreduce.add((colorcount = new JMenuItem("Color Count"))) ;
      colorcount.addActionListener(this) ;
		colorreduce.add((errordiffusion = new JMenuItem("Error Diffusion"))) ;
      errordiffusion.setEnabled(false) ;
      errordiffusion.addActionListener(this) ;

		// Geometry menu.

		geometryMenu = new JMenu("Geometry") ;
		geometryMenu.setMargin(insets) ;
		if (!applemac) geometryMenu.setMnemonic(KeyEvent.VK_G) ;
		mb.add(geometryMenu);
		geometryMenu.add((flip = new JMenuItem("Flip"))) ;
      flip.addActionListener(this) ;
		geometryMenu.add((mirror = new JMenuItem("Mirror"))) ;
      mirror.addActionListener(this) ;
		geometryMenu.add((crop = new JMenuItem("Crop"))) ;
      crop.addActionListener(this) ;
		geometryMenu.add((rotateleft90 = new JMenuItem("Rotate Left"))) ;
      rotateleft90.addActionListener(this) ;
		geometryMenu.add((rotateright90 = new JMenuItem("Rotate Right"))) ;
      rotateright90.addActionListener(this) ;
//		geometryMenu.add((rotate180 = new JMenuItem("Rotate 180"))) ;
//    rotate180.addActionListener(this) ;
		geometryMenu.addSeparator() ;
		geometryMenu.add((rotatefree = new JMenuItem("Rotate..."))) ;
      rotatefree.addActionListener(this) ;
		geometryMenu.add((scale = new JMenuItem("Scale..."))) ;
      scale.addActionListener(this) ;
		geometryMenu.add((shear = new JMenuItem("Shear..."))) ;
      shear.addActionListener(this) ;

		// Filter menu.

		filterMenu = new JMenu("Filter") ;
		filterMenu.setMargin(insets) ;
		if (!applemac) filterMenu.setMnemonic(KeyEvent.VK_F) ;
		mb.add(filterMenu);
		filterMenu.add((blur = new JMenuItem("Blur"))) ;
      blur.addActionListener(this) ;
		filterMenu.add((sharpen = new JMenuItem("Sharpen"))) ;
      sharpen.addActionListener(this) ;
		filterMenu.addSeparator() ;
		filterMenu.add((invert = new JMenuItem("Invert"))) ;
      invert.addActionListener(this) ;
		filterMenu.add((posterize = new JMenuItem("Posterize"))) ;
      posterize.addActionListener(this) ;

		// View menu.

		viewMenu = new JMenu(Kisekae.getCaptions().getString("MenuView")) ;
		viewMenu.setMargin(insets) ;
		if (!applemac) viewMenu.setMnemonic(KeyEvent.VK_T) ;
      viewMenu.add((statusbaritem = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewStatusBar")))) ;
      statusbaritem.setState(true) ;
      statusbaritem.addItemListener(this) ;
      statusbaritem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0)) ;
		viewMenu.add((editwindow = new JCheckBoxMenuItem("Edit Window"))) ;
		editwindow.addItemListener(this) ;
		editwindow.setSelected(true) ;
		viewMenu.add((framewindow = new JCheckBoxMenuItem("Frame Window"))) ;
		framewindow.addItemListener(this) ;
		framewindow.setSelected(true) ;
		mb.add(viewMenu);

		// Options menu.

		optionMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
		optionMenu.setMargin(insets) ;
		if (!applemac) optionMenu.setMnemonic(KeyEvent.VK_O) ;
		mb.add(optionMenu);
		optionMenu.add((showbackground = new JCheckBoxMenuItem("Show Background"))) ;
		showbackground.addItemListener(this) ;
		showbackground.setSelected(false) ;
		optionMenu.add((showoverlay = new JCheckBoxMenuItem("Show Image Overlay"))) ;
		showoverlay.addItemListener(this) ;
		showoverlay.setSelected(false) ;
		optionMenu.add((showmarquee = new JCheckBoxMenuItem("Show Bounding Box"))) ;
		showmarquee.addItemListener(this) ;
		showmarquee.setSelected(false) ;
		optionMenu.addSeparator() ;
      optionMenu.add((antialiasing = new JMenu("Antialiasing"))) ;
		antialiasing.add((antialiasingon = new JCheckBoxMenuItem("On"))) ;
		antialiasingon.setSelected(false) ;
		antialiasing.add((antialiasingoff = new JCheckBoxMenuItem("Off"))) ;
		antialiasingoff.setSelected(false) ;
		antialiasing.add((antialiasingdefault = new JCheckBoxMenuItem("Default"))) ;
		antialiasingdefault.setSelected(true) ;
      ButtonGroup bg1 = new ButtonGroup() ;
      bg1.add(antialiasingon);
      bg1.add(antialiasingoff);
      bg1.add(antialiasingdefault);
      optionMenu.add((rendering = new JMenu("Rendering"))) ;
		rendering.add((renderquality = new JCheckBoxMenuItem("Quality"))) ;
		renderquality.setSelected(false) ;
		rendering.add((renderspeed = new JCheckBoxMenuItem("Speed"))) ;
		renderspeed.setSelected(false) ;
		rendering.add((renderdefault = new JCheckBoxMenuItem("Default"))) ;
		renderdefault.setSelected(true) ;
      ButtonGroup bg2 = new ButtonGroup() ;
      bg2.add(renderquality);
      bg2.add(renderspeed);
      bg2.add(renderdefault);
      optionMenu.add((dithering = new JMenu("Dithering"))) ;
		dithering.add((ditherdisable = new JCheckBoxMenuItem("Disable"))) ;
		ditherdisable.setSelected(false) ;
		dithering.add((ditherenable = new JCheckBoxMenuItem("Enable"))) ;
		ditherenable.setSelected(false) ;
		dithering.add((ditherdefault = new JCheckBoxMenuItem("Default"))) ;
		ditherdefault.setSelected(true) ;
      ButtonGroup bg3 = new ButtonGroup() ;
      bg3.add(ditherdisable);
      bg3.add(ditherenable);
      bg3.add(ditherdefault);
      optionMenu.add((interpolation = new JMenu("Interpolation"))) ;
		interpolation.add((nearneighbour = new JCheckBoxMenuItem("Nearest Neighbour"))) ;
		nearneighbour.addItemListener(this) ;
		nearneighbour.setSelected(false) ;
		interpolation.add((bilinear = new JCheckBoxMenuItem("Bilinear"))) ;
		bilinear.addItemListener(this) ;
		bilinear.setSelected(true) ;
		interpolation.add((bicubic = new JCheckBoxMenuItem("Bicubic"))) ;
		bicubic.addItemListener(this) ;
		bicubic.setSelected(false) ;
      ButtonGroup bg4 = new ButtonGroup() ;
      bg4.add(nearneighbour);
      bg4.add(bilinear);
      bg4.add(bicubic);

		// Window menu.

		windowMenu = createWindowMenu() ;
		windowMenu.setMargin(insets) ;
		mb.add(windowMenu) ;

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
		SAVE.setEnabled((cel != null && cel.getName() != null) && !Kisekae.isSecure() && !Kisekae.isExpired());
		toolbar.add(NEW, null);
		toolbar.add(OPEN, null);
		toolbar.add(CLOSE, null);
		toolbar.add(SAVE, null);
      
      // Create the status bar.
      
      statusbar = new StatusBar(this) ;
      statusbar.setStatusBar(true) ;

		// Create the edit operations.

		CUT = new JButton() ;
		iconfile = Kisekae.getResource("Images/cut" + ext) ;
		if (iconfile != null) CUT.setIcon(new ImageIcon(iconfile)) ;
		CUT.setMargin(new Insets(1,1,1,1)) ;
		CUT.setAlignmentY(0.5f) ;
		CUT.addActionListener(this) ;
		CUT.setToolTipText(Kisekae.getCaptions().getString("ToolTipCut"));
		COPY = new JButton() ;
		iconfile = Kisekae.getResource("Images/copy" + ext) ;
		if (iconfile != null) COPY.setIcon(new ImageIcon(iconfile)) ;
		COPY.setMargin(new Insets(1,1,1,1)) ;
		COPY.setAlignmentY(0.5f) ;
		COPY.addActionListener(this) ;
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

		// Create the preview image panel.

      preview = new PreviewPanel(this) ;
		Border rbb = BorderFactory.createRaisedBevelBorder() ;
		CompoundBorder cb1 = new CompoundBorder(eb3,rbb) ;
      preview.setStatusBar(statusbar) ;
		preview.setBorder(cb1) ;

      // Create the default page format.

      if (Kisekae.isPrintInstalled())
      {
			PrinterJob prn = null ;
			try { prn = PrinterJob.getPrinterJob() ; }
			catch (Exception e) { }
			pageformat = (prn != null) ? prn.defaultPage() : null ;
      }

		// Create the user interface.

		Container c = getContentPane() ;
		panel1 = new JPanel() ;
		panel1.setLayout(new BorderLayout()) ;
		panel1.setBorder(eb1) ;
		c.add(toolbar,BorderLayout.NORTH);
		c.add(panel1,BorderLayout.CENTER);
		c.add(statusbar,BorderLayout.SOUTH);
		panel2 = new JPanel() ;
		panel2.setLayout(new BoxLayout(panel2,BoxLayout.Y_AXIS)) ;
		panel2.add(Box.createVerticalGlue()) ;
		panel1.add(panel2,BorderLayout.WEST) ;
		panel3 = new JPanel() ;
		panel3.setLayout(new BorderLayout()) ;
		panel3.setBorder(eb2) ;
		panel5 = new JPanel() ;
		panel5.setLayout(new BorderLayout()) ;
		panel5.add(preview.getToolBar(),BorderLayout.NORTH) ;
		panel5.add(preview,BorderLayout.CENTER) ;
      panel3.add(panel5,BorderLayout.CENTER) ;
  		panel6.setLayout(new BorderLayout()) ;
      panel6.setBorder(cb2) ;
      panel3.add(panel6,BorderLayout.WEST) ;
		panel1.add(panel3,BorderLayout.CENTER) ;

		// Size the frame for the window space.

      super.open() ;
		doLayout() ;
		validate() ;
      if (helper != null) helper.setSize(getSize());

		// Set up the window event listeners.

		addWindowListener(this) ;
      addKeyListener(keyListener) ;
		setValues() ;
	}


	// A utility method to create the edit menu.

	private JMenu createEditMenu()
	{
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuEdit")) ;
		menu.setMnemonic(KeyEvent.VK_E) ;

		// Undo and redo are actions of our own creation.

		undoAction = new UndoAction() ;
		menu.add(undoAction) ;
		redoAction = new RedoAction() ;
		menu.add(redoAction) ;
		menu.add((undoall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUndoAll")))) ;
		undoall.setEnabled(false) ;
		undoall.addActionListener(this);
		menu.addSeparator() ;

		// These actions come from the default editor kit, but we renamed
		// them.  Get the ones we want and stick them in the menu.

		menu.add((cut = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCut")))) ;
		cut.setEnabled(false) ;
		cut.addActionListener(this);
		menu.add((copy = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCopy")))) ;
		copy.setEnabled(false) ;
		copy.addActionListener(this);
		menu.add((paste = new JMenuItem(Kisekae.getCaptions().getString("MenuEditPaste")))) ;
		paste.setEnabled(false) ;
		paste.addActionListener(this);
		return menu;
	}


	// A utility method to create the window menu.

   private JMenu createWindowMenu()
   {
      boolean applemac = OptionsDialog.getAppleMac() ;
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		if (!applemac) menu.setMnemonic(KeyEvent.VK_W) ;
      for (int i = 0 ; i < windows.size() ; i++)
      {
         JCheckBoxMenuItem item = (JCheckBoxMenuItem) windows.elementAt(i) ;
         item.removeItemListener(me) ;
         item.setSelected(item == window) ;
         item.addItemListener(me) ;
         menu.add(item) ;
      }
      return menu ;
   }


	// Method to set the dialog field values.  This method sets the frame
   // title and adjusts the menu item state to reflect the current context.

	private void setValues()
	{
		setTitle() ;
      editMenu.setEnabled(cel != null || group != null);
      colorMenu.setEnabled(cel != null || group != null);
      geometryMenu.setEnabled(cel != null || group != null);
      filterMenu.setEnabled(cel != null || group != null);
		saveas.setEnabled(cel != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		save.setEnabled(cel != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		SAVE.setEnabled(cel != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		properties.setEnabled(cel != null || group != null) ;
		close.setEnabled(cel != null) ;
		CLOSE.setEnabled(cel != null) ;

      // Create our rendering hints.

      Object antialiasinghint = (antialiasingon.isSelected()) ? RenderingHints.VALUE_ANTIALIAS_ON
         : ((antialiasingoff.isSelected()) ? RenderingHints.VALUE_ANTIALIAS_OFF
         : ((antialiasingdefault.isSelected()) ? RenderingHints.VALUE_ANTIALIAS_DEFAULT : null)) ;
      Object renderinghint = (renderquality.isSelected()) ? RenderingHints.VALUE_RENDER_QUALITY
         : ((renderspeed.isSelected()) ? RenderingHints.VALUE_RENDER_SPEED
         : ((renderdefault.isSelected()) ? RenderingHints.VALUE_RENDER_DEFAULT : null)) ;
      Object ditheringhint = (ditherdisable.isSelected()) ? RenderingHints.VALUE_DITHER_DISABLE
         : ((ditherenable.isSelected()) ? RenderingHints.VALUE_DITHER_ENABLE
         : ((ditherdefault.isSelected()) ? RenderingHints.VALUE_DITHER_DEFAULT : null)) ;
      Object interpolationhint = (bilinear.isSelected()) ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
         : ((bicubic.isSelected()) ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
         : ((nearneighbour.isSelected()) ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : null)) ;
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,antialiasinghint) ;
      hints.put(RenderingHints.KEY_RENDERING,renderinghint) ;
      hints.put(RenderingHints.KEY_DITHERING,ditheringhint) ;
      hints.put(RenderingHints.KEY_INTERPOLATION,interpolationhint) ;

      // Create our preview image.

      setVisible(true) ;
      preview.init(config,group,cel,palette,multipalette) ;
      baseimage = preview.getImage() ;
      workimage = baseimage ;
      oldimage = baseimage ;
      colorpanel.setImage(baseimage) ;
      colorpanel.setPreview(preview) ;
      panel6.removeAll() ;
      panel6.setVisible(false) ;
      validate() ;

      // Construct the group object edit panel.

      if (group != null)
      {
         Vector cels = group.getCels() ;
         Vector framelabels = new Vector() ;
         northpanel = new GroupGlobalPanel(this,config,group) ;
         panel6.add(northpanel,BorderLayout.NORTH) ;

         // Build the group cel image list.

         framelabels.addElement("Object #" + group.getIdentifier());
         for (int i = 0 ; i < cels.size() ; i++)
         {
            Cel cel = (Cel) cels.elementAt(i) ;
            framelabels.addElement(cel) ;
         }

         // Create a scrollable list.

         framelist = new JList(framelabels) ;
         framelist.setSelectedIndex(0) ;
         framelist.addListSelectionListener(groupListener) ;
         JScrollPane scrollpane = new JScrollPane(framelist) ;
         int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
         if (OptionsDialog.getAppleMac()) vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
         int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
         if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
         scrollpane.setVerticalScrollBarPolicy(vspolicy) ;
         scrollpane.setHorizontalScrollBarPolicy(hspolicy) ;			
         panel6.add(scrollpane,BorderLayout.CENTER) ;
         panel6.setVisible(framewindow.getState()) ;
         revalidate() ;
      }

      // Construct the GIF frame edit panel.

      else if (cel instanceof GifCel)
      {
         GifCel gif = (GifCel) cel ;
         Vector frames = gif.getFrames() ;
         northpanel = new GifGlobalPanel(this,config,gif) ;
         panel6.add(northpanel,BorderLayout.NORTH) ;
         Vector framelabels = new Vector() ;

         // Build the GIF image frame list.

         framelabels.addElement("Global Information Block");
         for (int i = 0 ; i < frames.size() ; i++)
         {
            GifFrame cf = (GifFrame) frames.elementAt(i) ;
            framelabels.addElement(cf) ;
         }

         // Create a scrollable list.

         framelist = new JList(framelabels) ;
         framelist.setSelectedIndex(0) ;
         framelist.addListSelectionListener(gifFrameListener) ;
         JScrollPane scrollpane = new JScrollPane(framelist) ;
         panel6.add(scrollpane,BorderLayout.CENTER) ;
         lastchange = "Color Adjust" ;
         southpanel = colorpanel ;
         colorpanel.setPane(3) ;
         panel6.add(southpanel,BorderLayout.SOUTH) ;
         panel6.setVisible(framewindow.getState()) ;
         revalidate() ;
      }

      // Construct the cel frame edit panel.

      else if (cel != null)
      {
         northpanel = new CelLocalPanel(this,config,cel) ;
         panel6.add(northpanel,BorderLayout.NORTH) ;
         lastchange = "Color Adjust" ;
         southpanel = colorpanel ;
         colorpanel.setPane(3) ;
         panel6.add(southpanel,BorderLayout.SOUTH) ;
         panel6.setVisible(framewindow.getState()) ;
         revalidate() ;
      }
	}


	// This method sets the frame title based upon the active cel object.

	private void setTitle()
	{
   	if (cel == null) return ;
		String s = Kisekae.getCaptions().getString("ImageEditorTitle") ;
		String name = (cel == null) ? null : cel.getName() ;
		if (name != null) s += " - " + name ; else name = "" ;
		ArchiveFile zip = (cel == null) ? null : cel.getZipFile() ;
		if (zip != null) s += " (" + zip.getFileName() + ")" ;
		setTitle(s) ;
   }


	// This method sets the view pane.

	void setViewPane(ColorFrame cf)
	{
   	if (cf == null) return ;
      Component cftoolbar = cf.getToolbar() ;
      Component cfeditpane = cf.getEditPane() ; 
      Container contents = getContentPane() ;
      contents.remove(toolbar) ;
      contents.add(cftoolbar,BorderLayout.NORTH) ; 
      panel3.remove(panel5) ;
      panel3.add(cfeditpane,BorderLayout.CENTER) ;
   }
   
   
   // Set the changed indicator for any palette type change.
   
   void setChanged(boolean b) { changed = b ; }


   // A utility function to establish a new cel and palette object given
   // an archive entry.  We return either a cel object or a palette object.

	private KissObject createCelPalette(FileOpen fd, ArchiveEntry ze)
   {
		ArchiveEntry pe = ze ;
      Cel newcel = null ;
		Palette newpalette = null ;
		if (ze == null) return null ;

		// Load the cel object.  The load will fail if we have a
      // palette type KiSS cel.

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
		newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
      if (newcel != null)
      {
			newcel.setLoader(null) ;
			newcel.load() ;
			newpalette = newcel.getPalette() ;
         setAtOrigin(newcel) ;
		}

		// If we are a cel file then identify the associated palette.

		if (".cel".equals(ze.getExtension()))
		{
      	if (newcel != null && newcel.isError())
         {
				String [] ext = ArchiveFile.getPaletteExt() ;
            String s = Kisekae.getCaptions().getString("SelectCelPalette") ;
            s += " - " + newcel.getName() ;
				pe = fd.showConfig(me,s,ext) ;
				if (pe == null)
            {
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            	return null ;
            }

				// Load the palette object.

				newpalette = new Palette(pe.getZipFile(),pe.getPath()) ;
				newpalette.setIdentifier(new Integer(-1)) ;
				newpalette.setLoader(me) ;
				newpalette.load() ;

				// Load the cel object.

				newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
            newcel.setPaletteID(newpalette.getIdentifier()) ;
            newcel.setBackgroundIndex(newpalette.getBackgroundIndex()) ;
            newcel.setTransparentIndex(newpalette.getTransparentIndex()) ;
            newcel.setColorsUsed(newpalette.getColorCount()) ;
	         newcel.setPalette(newpalette) ;
				newcel.load() ;
	         setAtOrigin(newcel) ;
			}
		}

		// Return the cel or palette.

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		if (newcel != null && newcel.isError()) return null ;
		if (newpalette != null && newpalette.isError()) return null ;
		if (newcel != null) return newcel ;
      return newpalette ;
   }


	// A utility function to place an object at the origin.

	private void setAtOrigin(Object o)
   {
   	if (o instanceof Cel)
      {
         Cel cel = (Cel) o ;
   		cel.setLocation(new Point(0,0)) ;
   		Point offset = cel.getOffset() ;
   		cel.setPlacement(new Point(-offset.x,-offset.y)) ;
      }

      if (o instanceof Group)
      {
         Group group = (Group) o ;
         Point p = group.getLocation() ;
         group.setPlacement(-p.x,-p.y) ;
         group.drop() ;
      }
   }


	// A utility method to initialize for a new image file.  This method is
	// invoked after we have opened and loaded a new image object or created
	// a new image object.

	private void initimage()
	{
      if (window == null) 
      {
         window = new WindowImageItem(configobject,cel,palette) ;
         if (cel != null) windows.addElement(window) ;
         window.addItemListener(me) ;
      }
      
      group = null ;
		config = null ;
		changed = false ;
      updated = false ;
		memorysource = false ;
		multipalette = new Integer(0) ;
		if (palette != null) palette.startEdits() ;
      if (preview != null) preview.stopAnimation() ;
      
      for (int i = windowMenu.getItemCount()-1 ; i >= 0 ; i--)
      {
         JMenuItem item = windowMenu.getItem(i) ;
         if (!(item instanceof WindowImageItem)) continue ;
         windowMenu.remove(item) ;
      }
      for (int i = 0 ; i < windows.size() ; i++)
      {
         JCheckBoxMenuItem item = (JCheckBoxMenuItem) windows.elementAt(i) ;
         item.removeItemListener(me) ;
         item.setSelected(item == window) ;
         item.addItemListener(me) ;
         windowMenu.add(item) ;
      }
         
      setValues() ;
		doLayout() ;
		validate() ;
	}


   // A function to set the base image for edit changes.

   void setBaseImage() { 
      baseimage = preview.getImage() ; 
   }


   // A function to restore the image for edit changes.

   void restoreImage() { preview.setImage(oldimage) ; }


   // A function to update the preview image.

   void updatePreview()
   {
      preview.redraw() ;
      baseimage = preview.getImage() ;
      workimage = baseimage ;
   }


	// Method to return a reference to our parent zip file object.

	ArchiveFile getZipFile() { return (ze != null) ? ze.getZipFile() : null ; }

	// Method to return a reference to our zip entry object.

	ArchiveEntry getZipEntry() { return ze ; }


   // A function to crop an image.

   private BufferedImage createCroppedImage(Image img)
   {
      Rectangle clip = null ;
      BufferedImage image = null ;
      BufferedImage source = null ;

      // Ensure our source is a buffered image.

      int w = (img != null) ? img.getWidth(null) : 0 ;
      int h = (img != null) ? img.getHeight(null) : 0 ;
      source = makeBufferedImage(img,w,h,BufferedImage.TYPE_INT_ARGB,0) ;
      
      if (statusbar != null) statusbar.showStatus("Performing image crop ...") ;
      clip = (preview != null) ? preview.getSelection() : null ;
      if (preview != null) preview.clearSelection() ;
      if (clip == null) return null ;
      int cw = Math.min(clip.width,w-clip.x) ;
      int ch = Math.min(clip.height,h-clip.y) ;
      image = source.getSubimage(clip.x,clip.y,cw,ch) ;
      clipoffset = new Point(clip.x,clip.y) ;
      return image ;
   }


   // A function to perform an image rotation.

   private BufferedImage createFlipTransform(Image img, int type)
   {
      BufferedImage image = null ;
      AffineTransform at = null ;
      int w = (img != null) ? img.getWidth(null) : 0 ;
      int h = (img != null) ? img.getHeight(null) : 0 ;

      // Create our flip rotation transform.

      switch (type)
      {
         case TOP_BOTTOM:
            at = new AffineTransform(new double[] {1.0,0.0,0.0,-1.0}) ;
            at.translate(0.0,-h) ;
            break ;

         case LEFT_RIGHT:
            at = new AffineTransform(new double[] {-1.0,0.0,0.0,1.0}) ;
            at.translate(-w,0.0) ;
            break ;

         case ROTATELEFT90:
            at = AffineTransform.getRotateInstance(-Math.PI/2.0,w/2.0,h/2.0) ;
            at.translate((h-w)/2.0,(h-w)/2.0) ;
            int temp = w ; w = h ; h = temp ;
            break ;

         case ROTATERIGHT90:
            at = AffineTransform.getRotateInstance(Math.PI/2.0,w/2.0,h/2.0) ;
            at.translate((w-h)/2.0,(w-h)/2.0) ;
            temp = w ; w = h ; h = temp ;
            break ;

         case ROTATE180:
            at = AffineTransform.getRotateInstance(Math.PI,w/2.0,h/2.0) ;
            break ;

         default:
            break ;
      }

      // Apply the transform.  Create a new transform image.

      if (at != null)
      {
         if (statusbar != null) statusbar.showStatus("Applying affine transform ...") ;
         image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
         Graphics2D imagegc = image.createGraphics() ;
         if (img != null) imagegc.drawImage(img,at,null) ;
         imagegc.dispose() ;
      }
      return image ;
   }


   // A function to perform an image convolution.

   private BufferedImage createConvolveTransform(Image img, int type)
   {
      BufferedImage source = null ;
      BufferedImage image = null ;
      Kernel kernel = null ;

      // Ensure our source is a buffered image.

      source = makeBufferedImage(img,BufferedImage.TYPE_INT_ARGB) ;

      // Create our convolution kernel.

      switch (type)
      {
         case BLUR:
            float blurMatrix[] = {0.0625f, 0.125f, 0.0625f,
                                0.125f, 0.25f, 0.125f,
                                0.0625f, 0.125f, 0.0625f} ;
            kernel = new Kernel(3,3,blurMatrix) ;
            break ;

         case SHARPEN:
            float sharpMatrix[] = {-1.0f, -1.0f, -1.0f,
                                -1.0f, 9.0f, -1.0f,
                                -1.0f, -1.0f, -1.0f} ;
            kernel = new Kernel(3,3,sharpMatrix) ;
            break ;

         case SOBELVERT:
            float sbvMatrix[] = {-1.0f, -2.0f, -1.0f,
                               0.0f,  0.0f,  0.0f,
                               1.0f,  2.0f,  1.0f} ;
            kernel = new Kernel (3,3,sbvMatrix) ;
            break ;

         case SOBELHORIZ:
            float sbhMatrix[] = {1.0f, -0.0f, -1.0f,
                              2.0f,  0.0f, -2.0f,
                              1.0f,  0.0f, -1.0f} ;
            kernel = new Kernel (3,3,sbhMatrix) ;
            break ;

         case ROBERTSHORIZ:
            float rbhMatrix[] = {0.0f, 0.0f, -1.0f,
                              0.0f,  1.0f, 0.0f,
                              0.0f,  0.0f, 0.0f} ;
            kernel = new Kernel (3,3,rbhMatrix) ;
            break ;

         case ROBERTSVERT:
            float rbvMatrix[] = {-1.0f, 0.0f, 0.0f,
                              0.0f,  1.0f, 0.0f,
                              0.0f,  0.0f, 0.0f} ;
            kernel = new Kernel (3,3,rbvMatrix) ;
            break ;

         case EDGEDETECT:
            float edgeMatrix[] = {0.0f, -1.0f, 0.0f,
                                -1.0f, 4.0f, -1.0f,
                                0.0f, -1.0f, 0.0f} ;
            kernel = new Kernel(3,3,edgeMatrix) ;
            break ;

         default:
            break ;
      }

      // Apply the transform.  Create a new transform image.

      if (kernel != null)
      {
         if (statusbar != null) statusbar.showStatus("Applying convolve transform ...") ;
         ConvolveOp op = new ConvolveOp(kernel,ConvolveOp.EDGE_NO_OP,hints) ;
         image = op.createCompatibleDestImage(source,source.getColorModel()) ;
         image = op.filter(source,image) ;
         changed = true ;
      }
      return image ;
   }


   // A function to perform a lookup transform on the specified image.

   private BufferedImage createLookupTransform(Image img, int type, double param)
   {
      final int samples = 256 ;
  		final int max = samples - 1 ;
  		final float mid = max / 2.0f ;
      BufferedImage image = null ;
      BufferedImage source = null ;
      byte [] lut = new byte[samples] ;
      byte [] ident = new byte[samples] ;
      byte [][] bandadjust = null ;

      // Ensure our source is a buffered image.

      source = makeBufferedImage(img,BufferedImage.TYPE_INT_ARGB) ;
      if (source == null) return null ;

      // Create our convolution kernel.

      switch (type)
      {
         case INVERT:
            for (int i = 0 ; i < samples ; i++)
               lut[i] = (byte) (samples - i) ;
            break ;


         case POSTERIZE:
            for (int i = 0 ; i < samples ; i++)
               lut[i] = (byte) (i - (i % 32)) ;
            break ;

         default:
            break ;
      }

      // Apply the transform.  Create a new transform image.

      if (statusbar != null) statusbar.showStatus("Applying lookup transform ...") ;
      ByteLookupTable blt = (bandadjust != null)
         ? new ByteLookupTable(0,bandadjust) : new ByteLookupTable(0,lut) ;
      LookupOp op = new LookupOp(blt,hints) ;
      image = op.filter(source,image) ;
      changed = true ;
      return image ;
   }


   // A function to convert to a gray color space.

   private BufferedImage grayColorConvert(Image img)
   {
      BufferedImage source = null ;
      BufferedImage image = null ;

      // Ensure our source is an RGB buffered image.

      int w = (img != null) ? img.getWidth(null) : 0 ;
      int h = (img != null) ? img.getHeight(null) : 0 ;
      source = makeBufferedImage(null,w,h,BufferedImage.TYPE_INT_RGB,0) ;
      if (source == null) return null ;
      Graphics gc = source.getGraphics() ;
      gc.drawImage(img,0,0,null) ;
      gc.dispose() ;

      // Convert to a gray color space.

      if (statusbar != null) statusbar.showStatus("Converting image to grayscale ...") ;
      ColorSpace graySpace = ColorSpace.getInstance(ColorSpace.CS_GRAY) ;
      ColorConvertOp op = new ColorConvertOp(graySpace,hints) ;
      image = op.createCompatibleDestImage(source,source.getColorModel()) ;
      image = op.filter(source,image) ;
      changed = true ;
      return image ;
   }


   // Utility function to ensure that an AWT image is ARGB or RGB.  If we
   // already have a buffered image then it is not changed.  If the copy
   // argument is non-zero an image copy is returned of the specified type.

   private BufferedImage makeBufferedImage(Image img)
   { return makeBufferedImage(img, BufferedImage.TYPE_INT_ARGB, 1) ; }

   private BufferedImage makeBufferedImage(Image img, int type)
   { return makeBufferedImage(img, type, 0) ; }
   
   private BufferedImage makeBufferedImage(Image img, int type, int copy)
   { 
      if (img == null) return null ;
      int w = img.getWidth(null) ;
      int h = img.getHeight(null) ;
      return makeBufferedImage(img, w, h, type, copy) ; 
   }
   
   private BufferedImage makeBufferedImage(Image img, int w, int h, int type, int copy)
   {
      if (w <= 0 || h <= 0) return null ;
      if (copy == 0 && img instanceof BufferedImage) return (BufferedImage) img ;
      BufferedImage image = new BufferedImage(w,h,type) ;

      // Set the background as transparent.
/*
		int [] rgbarray = new int[w*h] ;
		for (int i = 0 ; i < w*h ; i++) rgbarray[i] = 1 ;
		image.setRGB(0,0,w,h,rgbarray,0,w) ;
*/
      // Apply the required image.

      if (img == null) return image ;
      Graphics gc = image.getGraphics() ;
      gc.drawImage(img,0,0,null) ;
      gc.dispose() ;
      return image ;
   }


   // A function to dither an image to a reduced number of colors.

   void changeColorCount(int m, boolean quick)
   {
      if (cel == null) return ;
      if (m > 256 && cel instanceof GifCel) m = 256 ;
      if (m <= 0) return ;
      if (statusbar != null) statusbar.showStatus("Dithering image to " + m + " colors ...") ;
      Object [] o = cel.dither(m,quick) ;
      Image img = (Image) o[0] ;
      Palette p = (Palette) o[1] ;
      ColorModel cm = (ColorModel) o[2] ;

      // Create a new palette if we have dithered to 256 colors or less.

      if (p != null)
      {
         cel.setImage(img,cm) ;
         cel.setPalette(p) ;
         cel.setPaletteID(p.getIdentifier()) ;
         cel.setTransparentIndex(p.getTransparentIndex()) ;
         cel.setBackgroundIndex(p.getBackgroundIndex()) ;
         cel.setColorsUsed(p.getColorCount()) ;
         palette = p ;

         // Ensure that we have the requisite number of colors.

         int colors = Math.min(m,256) ;
         palette.setColorCount(colors) ;
      }

      // Return the new image.

      if (img instanceof BufferedImage) image = (BufferedImage) img ;
      if (northpanel instanceof CelLocalPanel)
         ((CelLocalPanel) northpanel).update(image) ;
      if (northpanel instanceof GifGlobalPanel)
         ((GifGlobalPanel) northpanel).update(image) ;
      if (northpanel instanceof GifLocalPanel)
         ((GifLocalPanel) northpanel).update(image) ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   void changeRotation(double n)
   {
      if (preview == null) return ;
      Image img = workimage ;
      if (img == null) return ;
      try
      {
         int w = img.getWidth(null) ;
         int h = img.getHeight(null) ;
         AffineTransform at = AffineTransform.getRotateInstance(n, 0, 0) ;
         Rectangle bounds = new Rectangle(w, h) ;
         Shape shape = at.createTransformedShape(bounds) ;
         bounds = shape.getBounds() ;
         image = new BufferedImage(bounds.width, bounds.height,
                                   BufferedImage.TYPE_INT_ARGB) ;
         Graphics2D imagegc = image.createGraphics() ;
         imagegc.addRenderingHints(hints) ;
         imagegc.translate( -bounds.x, -bounds.y) ;
         imagegc.drawImage(img, at, null) ;
         imagegc.dispose() ;
         preview.setShowState(false) ;
         preview.updateImage(image) ;
         preview.setShowState(true) ;
         updateFramePanel(image) ;
      }
      catch (Exception e)
      {
         PrintLn.println("ImageFrame: rotate exception " + e);
      }
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   void changeShear(double n)
   {
      if (preview == null) return ;
      Image img = workimage ;
      if (img == null) return ;
      try
      {
         int w = img.getWidth(null) ;
         int h = img.getHeight(null) ;
         AffineTransform at = AffineTransform.getShearInstance(n, n) ;
         Rectangle bounds = new Rectangle(w, h) ;
         Shape shape = at.createTransformedShape(bounds) ;
         bounds = shape.getBounds() ;
         image = new BufferedImage(bounds.width, bounds.height,
                                   BufferedImage.TYPE_INT_ARGB) ;
         Graphics2D imagegc = image.createGraphics() ;
         imagegc.addRenderingHints(hints) ;
         imagegc.translate( -bounds.x, -bounds.y) ;
         imagegc.drawImage(img, at, null) ;
         imagegc.dispose() ;
         preview.setShowState(false) ;
         preview.updateImage(image) ;
         preview.setShowState(true) ;
         updateFramePanel(image) ;
      }
      catch (Exception e)
      {
         PrintLn.println("ImageFrame: shear exception " + e);
      }
   }


   // Utility functions to change the image size relative to the existing
   // base image.  The transformation does not update the base image.

   void changeScale(double n)
   {
      if (n < 0.1) return ;
      if (preview == null) return ;
      Image img = workimage ;
      if (img == null) return ;
      
      if (n == 1.0)
      {
         baseimage = oldimage ;
         preview.setShowState(false) ;
         preview.updateImage(oldimage) ;
         preview.setShowState(true) ;
         updateFramePanel(oldimage) ;
         return ;
      }
      
      try
      {
         int w = img.getWidth(null) ;
         int h = img.getHeight(null) ;
         AffineTransform at = AffineTransform.getScaleInstance(n, n) ;
         Rectangle bounds = new Rectangle(w, h) ;
         Shape shape = at.createTransformedShape(bounds) ;
         bounds = shape.getBounds() ;
         image = new BufferedImage(bounds.width, bounds.height,
                                   BufferedImage.TYPE_INT_ARGB) ;
         Graphics2D imagegc = image.createGraphics() ;
         imagegc.addRenderingHints(hints) ;
         imagegc.translate( -bounds.x, -bounds.y) ;
         imagegc.drawImage(img, at, null) ;
         imagegc.dispose() ;
         preview.setShowState(false) ;
         preview.updateImage(image) ;
         preview.setShowState(true) ;
         updateFramePanel(image) ;
      }
      catch (Exception e)
      {
         PrintLn.println("ImageFrame: scale exception " + e);
      }
   }


	// We close the current cel and palette and any zip file.

	void closeimage()
	{
		if (cel != null) cel.setLoader(null) ;
		if (palette != null) palette.setLoader(null) ;
		if (undo != null)	undo.discardAllEdits() ;
      if (undoAction != null)	undoAction.updateUndoState() ;
      if (redoAction != null)	redoAction.updateRedoState() ;
      if (preview != null) preview.stopAnimation() ;
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

      if (windowMenu != null && windows != null)
      {
         windowMenu.remove(window) ;
         windows.remove(window) ;
         window = null ;

         int n = windows.size() ;
         if (n > 0)
         {
            window = (WindowImageItem) windows.elementAt(n-1) ;
            cel = window.cel ;
            palette = window.palette ;
            updated = window.changed ;
         }
      }
	}



	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
			// Open file request.

			if (source == open || source == OPEN)
			{
            Vector v = new Vector() ;
				String [] ext = ArchiveFile.getImageExt() ;
				for (int i = 0 ; i < ext.length ; i++) v.addElement(ext[i]) ;
				FileOpen fd = new FileOpen(this,Kisekae.getCaptions().getString("ImageListTitle"),v.toArray()) ;
            fd.setFileFilter("imagefiles") ;
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
                  s = s.substring(0,i1+1) + name.toUpperCase() + s.substring(j1) ;
               JOptionPane.showMessageDialog(me,
                  s + "\n" +
                  Kisekae.getCaptions().getString("SaveAsImageText"),
                  Kisekae.getCaptions().getString("FileOpenException"),
                  JOptionPane.ERROR_MESSAGE) ;
					fd.close() ;
					return ;
				}

            // Save any previously updated palette file.

				if (!closecheck(true)) { fd.close() ; return ; }

				// Initialize for this image object.

				KissObject kiss = createCelPalette(fd,zenew) ;
				fd.close() ;
				cel = (kiss instanceof Cel) ? (Cel) kiss : null ;
				palette = (kiss instanceof Palette) ? (Palette) kiss
					: (cel != null) ? cel.getPalette() : null ;
				if (cel == null) return ;
            ze = zenew ;
            if (window != null) window.setChanged(updated) ;
            window = null ;
				initimage() ;
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

			// Exit request.  Save the image first if necessary.

			if (source == exit)
			{
				if (!closecheck(true)) return ;
				closeimage() ;
            close(false) ;
				return ;
			}

			// Close request.  Save the image first if necessary.

			if (source == close || source == CLOSE)
			{
				if (!closecheck(true)) return ;
				closeimage() ;
				initimage() ;
				return ;
			}

			// Save or Save As request.  Save the element.

			if (source == save || source == saveas || source == SAVE)
			{
				originalzepath = (ze == null) ? null : ze.getPath() ;
				originalcelname = (cel == null) ? null : cel.getName() ;
				originalpalettename = (palette == null) ? null : palette.getName() ;
//            if (colorpanel.isChanged()) colorpanel.apply() ;
//            if (geompanel.isChanged()) geompanel.apply() ;
				saveimage((source == saveas)) ;
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

         // An Undo All request rolls back all edit changes.

         if (undoall == source)
         {
            if (!undo.canUndo()) return ;
            int n = JOptionPane.showConfirmDialog(null,
               Kisekae.getCaptions().getString("UndoAllConfirmText"),
               Kisekae.getCaptions().getString("MenuEditUndoAll"),
               JOptionPane.YES_NO_OPTION) ;
            if (n != JOptionPane.YES_OPTION) return ;

            // Undo everything.  Reset to initial state.

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            while (undo.canUndo()) undo.undo() ;
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            undo.discardAllEdits() ;
            undoAction.updateUndoState() ;
            redoAction.updateRedoState() ;
         }

         // A flip command.

         if (source == flip)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Flip" ;
            image = createFlipTransform(baseimage,TOP_BOTTOM) ;
            applyTransformedImage(image) ;
         }

         // A mirror command.

         if (source == mirror)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Mirror" ;
            image = createFlipTransform(baseimage,LEFT_RIGHT) ;
            applyTransformedImage(image) ;
         }

         // A rotate left 90 command.

         if (source == rotateleft90)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Rotate -90" ;
            image = createFlipTransform(baseimage,ROTATELEFT90) ;
            applyTransformedImage(image) ;
         }

         // A rotate right 90 command.

         if (source == rotateright90)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Rotate +90" ;
            image = createFlipTransform(baseimage,ROTATERIGHT90) ;
            applyTransformedImage(image) ;
         }

         // A rotate 180 command.

         if (source == rotate180)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Rotate 180" ;
            image = createFlipTransform(baseimage,ROTATE180) ;
            applyTransformedImage(image) ;
         }

         // A blur command.

         if (source == blur)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Blur" ;
            image = createConvolveTransform(baseimage,BLUR) ;
            applyTransformedImage(image) ;
         }

         // A sharpen command.

         if (source == sharpen)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Sharpen" ;
            image = createConvolveTransform(baseimage,SHARPEN) ;
            applyTransformedImage(image) ;
         }

         // An invert command.

         if (source == invert)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Invert" ;
            image = createLookupTransform(baseimage,INVERT,0) ;
            applyTransformedImage(image) ;
         }

         // A posterize command.

         if (source == posterize)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Posterize" ;
            image = createLookupTransform(baseimage,POSTERIZE,0) ;
            applyTransformedImage(image) ;
         }

         // A request to convert an image to gray scale.

         if (source == grayscale)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Gray Scale" ;
            image = grayColorConvert(baseimage) ;
            applyTransformedImage(image) ;
         }

			// Set Color Count request.  This dithers the image for the
         // specified number of colors.

			if (source == colorcount)
			{
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Count" ;
            int n = cel.getColorsUsed() ;
            if (palette != null) n = palette.getColorCount() ;
           	ColorSizePanel csd = new ColorSizePanel(this,n) ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = csd ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A brightness command.

         if (source == brightness)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(0) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A contrast command.

         if (source == contrast)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(1) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
       }

         // A gamma command.

         if (source == gamma)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(2) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A red command.

         if (source == red)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(3) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
        }

         // A green command.

         if (source == green)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(4) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A blue command.

         if (source == blue)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(5) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // An alpha command.

         if (source == alpha)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Color Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = colorpanel ;
            colorpanel.setImage(preview.getImage()) ;
            colorpanel.setPane(6) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A free rotate command.

         if (source == rotatefree)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Geometry Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = geompanel ;
            geompanel.setPane(0) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A shear command.

         if (source == shear)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Geometry Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = geompanel ;
            geompanel.setPane(1) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
        }

         // A scale command.

         if (source == scale)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Geometry Adjust" ;
            if (southpanel != null) panel6.remove(southpanel) ;
            southpanel = geompanel ;
            geompanel.setPane(2) ;
            panel6.add(southpanel,BorderLayout.SOUTH) ;
            validate() ;
            panel6.repaint() ;
         }

         // A crop command.

         if (source == crop)
         {
            oldimage = baseimage ;
            oldpalette = palette ;
            lastchange = "Crop" ;
            image = createCroppedImage(baseimage) ;
            applyTransformedImage(image) ;
         }


			// A Print request prints the current panel display.

         if (print == source)
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
						JOptionPane.showMessageDialog(null,
							"Printer exception.  Printing terminated." + "\n" + ex.toString(),
							"Printer Fault",  JOptionPane.ERROR_MESSAGE) ;
					}
				}
				return ;
			}

			// The Help About request brings up the About dialog window.

         if (about == source)
			{
				if (aboutdialog != null) aboutdialog.show() ;
				return ;
			}

			// A Print Preview request shows a preview frame.

         if (printpreview == source)
			{
			   ComponentPrintable cp = new ComponentPrintable(getContentPane()) ;
				int orientation = PageFormat.PORTRAIT ;
				if	(pageformat != null) orientation = pageformat.getOrientation() ;
				new PrintPreview(cp,orientation) ;
				return ;
			}

			// A Page Setup request establishes the print control page format.

         if (pagesetup == source)
			{
				PrinterJob pj = PrinterJob.getPrinterJob() ;
				pageformat = pj.pageDialog(getPageFormat()) ;
				return ;
			}

			// A Properties request shows the cel dialog.

			if (properties == source)
			{
            changed = false ;
         	if (cel != null) 
            {
               CelDialog cd = new CelDialog(this,cel,null,config) ;
               cd.callback.addActionListener(this) ;
               cd.show() ;
               return ;
            }
         	if (group != null) 
            {
               GroupDialog gd = new GroupDialog(this,group,null,config) ;
               gd.callback.addActionListener(this) ;
               gd.show() ;
               return ;
            }
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

			// An update request from the file save window has occured.
         // Once the file is written changes cannot be undone.  We
         // retain visibility of the active file and do not switch
         // to the saved file.

			if ("FileWriter Callback".equals(evt.getActionCommand()))
			{
				String newname = (writecel == null) ? null : writecel.getPath() ;
            
				if (originalzepath != null && ze != null)
					ze.setPath(originalzepath) ;
				if (originalcelname != null && writecel != null)
					writecel.setName(originalcelname) ;
				if (originalpalettename != null && fwpalette != null)
					fwpalette.setName(originalpalettename) ;

				// If we saved a cel file then save the associated palette.

				if (ArchiveFile.isCel(newname))
				{
   				savepalette(writecel,fwpalette,newname) ;
				}

				// If we saved a palette file then make sure that any preset 
            // encoding is dropped.

				if (writecel == null && fwpalette != null)
				{
   				fwpalette.setEncodeArrays(null,null,null) ;
				}

            changed = false ;
            updated = false ;
            if (cel != null) cel.setUpdated(false) ;
            writecel = null ;
            fwpalette = null ;
				return ;
			}

			// An update request from the Color Editor occured.  This can
         // happen from the properties dialogs or the GIF edit pane.  The
         // preview image will be updated.

			if ("ColorFrame Callback".equals(evt.getActionCommand()))
         {
            changed = true ;
            oldpalette = (Palette) palette.clone() ;
            oldpalette.undoEdits() ;
            oldimage = baseimage ;
            lastchange = "Color Edit" ;
            palette = (cel != null) ? cel.getPalette() : null ;
            Image img = (cel != null) ? cel.getImage() : null ;
            image = makeBufferedImage(img,BufferedImage.TYPE_INT_ARGB) ;
            createUndoColor(oldimage,image,oldpalette,palette,lastchange) ;
            updateInterface(cel,currentframe) ;
            updatePreview() ;
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
			PrintLn.println("ImageFrame: Out of memory.") ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			PrintLn.println("ImageFrame: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


   // A function to update the working image with the preview copy.

   void updateTransformedImage()
   {
      workimage = (BufferedImage) preview.getImage() ;
   }
   
   // A function to apply the working image to the reference object.
   // If permanent change an undoable edit is created.

   void applyTransformedImage()
   {  applyTransformedImage(workimage) ; }
   
   void applyTransformedImage(Image image)
   { applyTransformedImage(image,true) ; }
   
   private void applyTransformedImage(Image image, boolean perm)
   {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      if (statusbar != null) statusbar.showStatus("") ;
      if (image == null) return ;

      if (perm) baseimage = image ;
      workimage = image ;
      image = baseimage ;
      
      // If we have a cel, update the cel size and image.

      if (cel != null)
      {
         cel.setImage(image) ;
         if (clipoffset != null)
         {
            Point offset = cel.getOffset() ;
            offset.x += clipoffset.x ;
            offset.y += clipoffset.y ;
            cel.setOffset(offset) ;
            clipoffset = null ;
         }

         // If we have a palette, reconstruct the palette.
         // Image transforms may adjust the number of colors.

         oldpalette = palette ;
         palette = cel.getPalette() ;
         if (palette != null && changed)
         {
            Palette p = cel.makePalette(palette.getColorCount()) ;
            if (p != null)
            {
               palette = p ;
               cel.setPalette(palette) ;
               cel.setPaletteID(palette.getIdentifier()) ;
               cel.setTransparentIndex(palette.getTransparentIndex()) ;
               cel.setBackgroundIndex(palette.getBackgroundIndex()) ;
               cel.setColorsUsed(palette.getColorCount()) ;
            }
         }
      }

      // Update the preview image.

      if (preview != null)
      {
         preview.setPalette(palette) ;
         preview.setShowState(false) ;
         preview.setImage(image) ;
         preview.setShowState(true) ;
      }

      // Update the colorpanel image reference for the next edit.
      
      colorpanel.setImage(image);     

      // Update the frame panel to reflect any palette changes.

      if (!perm) return ;
      createUndoColor(oldimage,image,oldpalette,palette,lastchange) ;
      updateFramePanel(image) ;
      oldimage = workimage ;
      updated = true ;
   }



   // Capture the change for undo/redo processing.

   private void createUndoColor(Image oldimage, Image image, Palette oldpalette, Palette palette, String lastchange)
   {
      UndoableEdit ce = new UndoableColor(oldimage,image,oldpalette,palette,lastchange) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      undo.undoableEditHappened(evt) ;
      undoAction.updateUndoState() ;
      redoAction.updateRedoState() ;
   }


	// ItemListener interface.  The item state changed method is invoked
	// when checkbox menu items are selected.

	public void itemStateChanged(ItemEvent evt)
	{
		Object source = evt.getSource() ;

		// Request to show the preview image background color.  This removes
		// transparency from the preview image background.

		if (source == showbackground)
		{
			if (preview == null) return ;
			preview.redraw(multipalette) ;
			return ;
		}

		// Request to show a selection box around the current object.

		if (source == showmarquee)
		{
			if (preview == null) return ;
			preview.redraw(multipalette) ;
			return ;
		}

      // Turn the edit pane window on and off.  This also turns off
      // the image preview toolbar.

      if (source == editwindow)
      {
         if (preview == null) return ;
         panel5.setVisible(editwindow.getState()) ;
         return ;
      }

      // Turn the frame window on and off.

      if (source == framewindow)
      {
         if (panel6 == null) return ;
         panel6.setVisible(framewindow.getState()) ;
         return ;
      }

      // Turn the status bar on and off.

      if (source == statusbaritem)
      {
         if (statusbar == null) return ;
         statusbar.setVisible(statusbaritem.getState()) ;
         return ;
      }

      // Show a new image in the image window.

      if (source instanceof WindowImageItem)
      {
         if (window != null) window.changed = updated ;
         window = (WindowImageItem) source ;
         cel = window.cel ;
         palette = window.palette ;
         updated = window.changed ;
         initimage() ;
         return ;
      }
	}


	// Method to check for pending updates.  This method returns true if
	// the check is not cancelled.  This method will restore the palette
	// state to the initial multipalette if the contents are not saved.

	private boolean closecheck(boolean cancel)
	{
		boolean restorestate = changed ;
//      if (colorpanel.isChanged()) colorpanel.apply() ;
//      if (geompanel.isChanged()) geompanel.apply() ;
      if (cel != null && cel.isUpdated()) changed = true ;

		// Check for a file save if changes are pending.

		if (changed || updated)
      {
      	int opt = JOptionPane.YES_NO_OPTION ;
         if (cancel) opt = JOptionPane.YES_NO_CANCEL_OPTION ;

         // If we are editing an active file, check for a data set update.

			if (memorysource)
	      {
            String s = Kisekae.getCaptions().getString("UnknownValueText") ;
            if (cel != null) s = cel.getName() ;
            String s1 = Kisekae.getCaptions().getString("ApplyChangeFileText") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
            int i = JOptionPane.showConfirmDialog(null, s1,
               Kisekae.getCaptions().getString("ApplyChangeTitle"),
               opt, JOptionPane.QUESTION_MESSAGE) ;

	         // If we should apply the changes, update our panel frame using
	         // the contents of the current palette.

				if (i == JOptionPane.CANCEL_OPTION)
	            return false ;
				if (i == JOptionPane.YES_OPTION)
				{
            	int basetransparent = -1 ;
            	int baseloop = 0 ;
               Point baseoffset = null ;
               Point baseloc = null ;

            	// Transparent cel color changes or max loop count changes
               // may have been applied to a cloned cel.  We need to apply
               // the changes to the real cel and save a copy of the original
               // cel for undo.

               if (cel != null)
               {
                  if (configobject instanceof Cel)
                  {
                  	Cel c = (Cel) configobject ;
                     basetransparent = c.getTransparentIndex() ;
                     baseloop = c.getLoopLimit() ;
                     baseoffset = c.getOffset() ;
                     baseloc = originallocation ;
                     baseimage = c.getImage() ;
                     oldpalette = c.getPalette() ;
                     
                     // If our cel was palette type then it gets replaced with
                     // a truecolor image. Also, possible color changes and 
                     // geometry changes means that we may have to replace the 
                     // cel.  This is difficult as the image size and type may 
                     // have changed.  

                     c.setPalette(cel.getPalette()) ;
                     c.setPaletteID(cel.getPaletteID()) ;
                     c.setColorsUsed(cel.getColorsUsed()) ;
                     c.setBackgroundIndex(cel.getBackgroundIndex()) ;
                     c.setTransparentIndex(cel.getTransparentIndex()) ;
                     c.setOffset(cel.getOffset()) ;
                     c.setAdjustedOffset(cel.getAdjustedOffset()) ;
                     c.setLocation(originallocation) ;
                     c.changeTransparency(0) ;
                     c.setLoopLimit(cel.getLoopLimit()) ;
                     c.setImage(cel.getImage(),cel.getColorModel()) ;
                     c.setUpdated(true) ;
                     
                     // Rebuild the group if the cel size changed.
                     
                     Object o = c.getGroup() ;
                     if (o instanceof Group) 
                     {
                        Group g = (Group) o ;
                        g.updateBoundingBox() ;
                     }
                  }
               }

					// Perform the callback and capture the edit change.

               callback.setDataObject(configobject) ;
               callback.doClick() ;
					capturePanelEdit(configobject,basetransparent,baseloop,baseoffset,baseloc,baseimage,oldpalette) ;
					updated = true ;
               restorestate = false ;
				}
	      }

         // If we are editing a non-active file, check for a file save.

			else if (!Kisekae.isSecure())
			{
				String file = (ze == null) ? cel.getName() : ze.getPath() ;
				ArchiveFile zip = (ze == null) ? null : ze.getZipFile() ;
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
            int i = JOptionPane.showConfirmDialog(null, s1, s2,
               opt, JOptionPane.QUESTION_MESSAGE) ;

				// Save the text contents if necessary.

				if (i == JOptionPane.CANCEL_OPTION)
					return false ;
				if (i == JOptionPane.YES_OPTION)
            {
               saveimage(true) ;
               restorestate = false ;
            }
         }
		}

      // Terminate edits.

		if (palette != null)
         palette.stopEdits() ;
      if (configobject instanceof Cel)
         ((Cel) configobject).setAnimate(animatestate) ;
		ze = null ;
      changed = false ;
		return true ;
   }


   // This function constructs an undoable image change edit on the
   // PanelFrame.  This edit allows the panel frame to undo image
   // edits that are applied through registered event callbacks.
   // The fact that we have to construct the undoable edit within this
   // ImageFrame editor is an architectural problem as it now associates
   // the image editor to the panel frame.

   private void capturePanelEdit(Object editobject, int transparent, int loop, 
           Point offset, Point loc, Image img, Palette palette)
   {
      if (!memorysource) return ;
   	MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe == null) return ;
      PanelFrame panelframe = mainframe.getPanel() ;
      if (panelframe == null) return ;

      // Construct the undoable edit.

	   panelframe.createImageEdit(editobject,transparent,loop,offset,loc,img,palette) ;
      panelframe.showpage() ;
   }

   // Implementation of the menu item update of our state when we become
   // visible.  
   
   void updateRunState()
   {
      
      // Find the Window menu item separator.

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
	{ 
      closecheck(false) ; 
      close() ; 
   }


	// Clipboard owner interface functions.

	public void lostOwnership(Clipboard cb, Transferable contents)
	{
	}


	// Utility function to write the cel file contents.  If we are 
   // performing a Save As then we write to the local file system.
   // A Save replaces the current file, which can be in an archive.

	private void saveimage(boolean saveas)
	{
      Cel c = cel ;
		FileSave fs = null ;
		if (c == null) return ;
      if (c.getName() == null) saveas = true ;

      // We clone the cel because we destructively update its contents 
      // and we do not want to alter the original object.  For KissCels
      // the FileWriter will write the associate palette file when the
      // image file write returns.  
      
      writecel = (Cel) c.clone() ;
      fwpalette = palette ;
      ArchiveEntry cze = writecel.getZipEntry() ;
      if (cze != null) writecel.setZipEntry((ArchiveEntry) cze.clone()) ;
      if (saveas) writecel.setZipFile(null) ;
		writecel.setUpdated(true) ;
		fs = new FileSave(this,writecel) ;
		fs.addWriteListener(writelistener) ;
      fs.setFileFilter("Image") ;
		if (saveas) fs.show() ; else fs.save() ;
	}


	// Utility function to write the palette file contents.  The palette
   // is written with the same name as the image cel if we are performing
   // a Save As.

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

		// Set a KiSS .KCF extension for the new name.

		int i = newname.lastIndexOf(".") ;
		if (i < 0) i = newname.length() ;
		newname = newname.substring(0,i) + ".kcf" ;

		// Create a zip entry for the new name.  We clone the palette
      // because we destructively update its contents and we do not
      // want to alter the original object.

      palette = (Palette) palette.clone() ;
		ArchiveFile zip = cel.getZipFile() ;
      ArchiveEntry pze = palette.getZipEntry() ;
      if (zip == null || pze == null)
      {
         palette.setZipFile(null) ;
         if (zip == null && pze != null)
            palette.setZipEntry((ArchiveEntry) pze.clone()) ;
         if (zip instanceof DirFile || zip == null)
            palette.setZipEntry(new DirEntry(newname));
         if (zip instanceof PkzFile)
            palette.setZipEntry(new PkzEntry(newname));
         if (zip instanceof LhaFile)
            palette.setZipEntry(new LhaEntry(newname));
   		palette.setName(newname) ;
      }

		// Save the palette.

		palette.setUpdated(true) ;
		fs = new FileSave(this,palette) ;
		fs.save() ;
	}


   // A function to update the GIF frame panel.

   private int updateInterface(Object o, Object frame)
   {
      if (o instanceof GifCel) return updateGifInterface(o,frame) ;
      if (o instanceof Group) return updateGroupInterface(o,frame) ;
      if (o instanceof Cel) return updateCelInterface(o,frame) ;
      return -1 ;
   }


   // A function to update the GIF frame panel.

   private int updateGifInterface(Object cel, Object frame)
   {
      if (!(cel instanceof GifCel)) return -1 ;
      GifCel gif = (GifCel) cel ;
      Vector frames = gif.getFrames() ;
      int n = (frames == null) ? 0 : frames.indexOf(frame) ;

      // Update the frame as colors may have changed.

      baseimage = preview.getImage() ;
      applyTransformedImage(image,false) ;
      if (frame instanceof GifFrame) 
         ((GifFrame) frame).setImage(baseimage);
      else
         gif.setImage(baseimage);
      preview.init(config,group,gif,palette,multipalette) ;

      // Assign the correct north panel.

      if (northpanel != null)
         panel6.remove(northpanel) ;
      if (northpanel instanceof GifLocalPanel)
         ((GifLocalPanel) northpanel).hidePopup() ;
      if (northpanel instanceof GifGlobalPanel)
         ((GifGlobalPanel) northpanel).hidePopup() ;
      if (frame instanceof GifFrame)
      {
         northpanel = new GifLocalPanel(this,config,gif,(GifFrame) frame,n) ;
         ((GifLocalPanel) northpanel).update(image) ;
      }
      else
      {
         northpanel = new GifGlobalPanel(this,config,gif) ;
         ((GifGlobalPanel) northpanel).update(image) ;
      }
      panel6.add(northpanel,BorderLayout.NORTH) ;
      validate() ;
      return n ;
   }


   // A function to update the group frame panel.

   private int updateGroupInterface(Object group, Object frame)
   {
      if (!(group instanceof Group)) return -1 ;
      Group g = (Group) group ;
      Vector frames = g.getCels() ;
      int n = (frames == null) ? 0 : frames.indexOf(frame) ;

      // Assign the correct north panel.

      if (northpanel != null)
         panel6.remove(northpanel) ;
      if (frame instanceof Cel)
         northpanel = new GroupLocalPanel(this,config,g,(Cel) frame,n) ;
      else
         northpanel = new GroupGlobalPanel(this,config,g) ;
      panel6.add(northpanel,BorderLayout.NORTH) ;
      validate() ;
      return n ;
   }


   // A function to update the cel panel.

   private int updateCelInterface(Object cel, Object frame)
   {
      if (!(cel instanceof Cel)) return -1 ;
      Cel c = (Cel) cel ;

      // Assign the correct north panel.

      if (northpanel != null) panel6.remove(northpanel) ;
      northpanel = new CelLocalPanel(this,config,c) ;
      panel6.add(northpanel,BorderLayout.NORTH) ;
      validate() ;
      return 0 ;
   }


   // A function to update the frame panel with our new cel image.

   private void updateFramePanel(Image img)
   {
      if (img == null) return ;
      if (northpanel instanceof GifGlobalPanel)
         ((GifGlobalPanel) northpanel).update(img) ;
      else if (northpanel instanceof GifLocalPanel)
         ((GifLocalPanel) northpanel).update(img) ;
      else if (northpanel instanceof GroupGlobalPanel)
         ((GroupGlobalPanel) northpanel).update(img) ;
      else if (northpanel instanceof GroupLocalPanel)
         ((GroupLocalPanel) northpanel).update(img) ;
      else if (northpanel instanceof CelLocalPanel)
         ((CelLocalPanel) northpanel).update(img) ;
      validate() ;
   }

   
   // Fuctions to update our images after an undo/redo
   
   private void updateOldImage(Image img) 
   { oldimage = img ; image = (BufferedImage) oldimage ; }
   private void updateOldPalette(Palette p) 
   { oldpalette = p ; palette = oldpalette ; }
   

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


	// We close the frame after clean up.   We also restore our cel colors
	// to the multipalette being used in the panel frame as colors may have
	// changed during this edit session.

	void close(boolean b)
	{
      for (int i = windows.size()-1 ; i >= 0 ; i--)
      {
         window = (WindowImageItem) windows.elementAt(i) ;
         cel = window.cel ;
         configobject = window.kiss ;
         palette = window.palette ;
         changed = window.changed ;
         if (!closecheck(b)) return ;
         window.removeItemListener(me) ;
      }
      
      // Close this frame.
      
		super.close() ;
		if (cel != null) cel.setLoader(null) ;
		if (palette != null) palette.setLoader(null) ;
      if (preview != null) preview.stopAnimation() ;
      if (preview != null) preview.setImage(null) ;
      if (preview != null) preview.callback.removeActionListener(null) ;
      if (statusbar != null) statusbar.setStatusBar(false) ;
      callback.removeActionListener(null) ;
      if (northpanel instanceof CelLocalPanel)
         ((CelLocalPanel) northpanel).flush() ;
      if (southpanel instanceof ImageColorPanel)
         ((ImageColorPanel) southpanel).flush() ;
      flush() ;
      dispose() ;
   }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if some problem occurs while
   // disposing with the dialog window.

	private void flush()
	{
		me = null ;
		ze = null ;
		cel = null ;
		group = null ;
		palette = null ;
      preview = null ;
		config = null ;
		toolbar = null ;
		clipboard = null ;
      colorpanel = null ;
      geompanel = null ;
      configobject = null ;
      oldpalette = null ;
      northpanel = null ;
      southpanel = null ;
      statusbar = null ;
      windows = null ;
		if (undo != null)	undo.discardAllEdits() ;
		undo = null ;
      KissObject.setLoader(null) ;

      // Flush the dialog contents.

      setVisible(false) ;
		removeKeyListener(keyListener);
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
		Runtime.getRuntime().gc() ;
   }


	// Inner class to create an image preview panel that draws only those
	// cels associated with the specified palette.

	class PreviewPanel extends ImagePreview
	{
		private Cel cel = null ;							// Cel to preview
		private Group group = null ;						// Group to preview
		private Palette palette = null ;					// Palette to preview
      private Integer multipalette = null ;			// Multipalette to view
      private Vector cels = null ;						// Configuration cels
		private Vector celList = null ;    				// Cels using palette
      private Dimension panelArea = null ;   		// Our panel size
		private Image image = null ;    					// The preview image
		private float sf = 1.0f ;							// Preview scale factor
      private boolean show = false ; 					// True, show cel always

      // Interface objects

      private JToggleButton animate = null ;       // Toolbar animate button
      private JComponent animatesep = null ;       // Toolbar separator
      private Thread animatethread = null ;        // Image animation thread


   	// Register for animation preview action events.

   	ActionListener animateListener = new ActionListener()
      {
   		public void actionPerformed(ActionEvent e)
   		{
            if (animate.isSelected())
               startAnimation() ;
            else
               stopAnimation() ;
   		}
   	} ;


      // Constructor

		public PreviewPanel(ActionListener al)
      {
      	super() ;
         AbstractButton cb = getCallback() ;
         if (al != null && cb != null) cb.addActionListener(al) ;
         animate = new JToggleButton(Kisekae.getCaptions().getString("AnimatePreviewText")) ;
         animatesep = new JToolBar.Separator() ;
         animate.addActionListener(animateListener) ;
		}

      // Initialize the preview panel.

		void init(Configuration cfg, Group g, Cel c, Palette p, Integer mp)
		{
			cel = c ;
			group = g ;
			palette = p ;
			config = cfg ;
			multipalette = mp ;
			celList = new Vector() ;
			cels = new Vector() ;
			setImage(null) ;

			// Access the configuration information to customize the panel
			// for our specific group or cel image.  If we are showing
			// a specific cel, build the panel for that cel.

			if (cel != null)
			{
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

			if (group != null)
			{
				cels.addAll(group.getCels()) ;
            Rectangle box = group.getBoundingBox() ;
            if (box != null)
				{
					int w = box.width ;
					int h = box.height ;
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

			for (int i = cels.size()-1; i >= 0; i--)
			{
				c = (Cel) cels.elementAt(i) ;
				c.changePalette(multipalette) ;
				celList.addElement(c) ;
			}

			// Draw the scene.

			Graphics gc = image.getGraphics() ;
			draw(gc,celList,show,true) ;
         gc.dispose() ;

         // Request that we paint ourselves if we have at least one cel.

         updateToolBar() ;
			if (celList.size() > 0) setImage(image) ;
		}


		// Create the preview image buffer.

		private Image createimage(int width, int height)
   	{
   		if (width < 1) width = 1 ;
   		if (height < 1) height = 1 ;
   		if (width > 2000) width = 2000 ;
   		if (height > 2000) height = 2000 ;
         Image image = makeBufferedImage(null,width,height,BufferedImage.TYPE_INT_ARGB,0) ;
			return image ;
		}


      // Method to set our show status option.  This will draw a cel
      // even if it is not visible.

      void setShow(boolean b) { show = b ; }


      // Method to set our active palette.  This must be done if the cel
      // palette is replaced.

      void setPalette(Palette p) { palette = p ; }


   	// The draw method is used to paint the required set of cels on the
   	// screen.  This method clears the drawing area and then draws the
   	// cels over a static background.  Only visible cels are drawn
      // unless we have explicity requested that all cels be shown.

   	private void draw(Graphics g, Vector celList, boolean show, boolean clear)
   	{
   		if (celList == null || panelArea == null) return ;
         int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
			Rectangle box = new Rectangle(panelArea) ;

   		// Clear the image if we must draw everything.

         if (clear && image instanceof BufferedImage)
         {
            int iw = image.getWidth(null) ;
            int ih = image.getHeight(null) ;
            int [] rgbarray = new int[iw*ih] ;
   			for (int i = 0 ; i < iw*ih ; i++) rgbarray[i] = 0 ;
   			((BufferedImage) image).setRGB(0,0,iw,ih,rgbarray,0,iw) ;
         }

			// Set the background color to be the multipalette background
			// color.  Note, this can differ from the page set background
			// color as the pageset background is taken from palette file 0.
			// The background can be a transparent color.  If we show the
         // background we must paint it as an opaque color.

			Color c = (palette != null) ? palette.getBackgroundColor(mp) : null ;
			if (c == null) c = getBackground() ;
			if (showbackground.isSelected() && c != null)
			{
				g.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue())) ;
				if (clear) g.fillRect(box.x,box.y,box.width,box.height) ;
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
            if (cel == currentframe && showmarquee.isSelected())
               cel.drawSelected(g,sf,0,0,false,false) ;
				cel.setVisibility(visible) ;
   		}
   	}


		// Method to redraw the image for a new multipalette.  If a color
		// has changed the palette will have been invalidated and all cels
      // using the associated multipalette must be re-established.  This
      // routine can draw on top of the current image if the background
      // is not cleared.

		void redraw() { redraw(multipalette,true) ; }
		void redraw(Integer mp) { redraw(mp,true) ; }
		void redraw(boolean clear) { redraw(multipalette,clear) ; }
		void redraw(Integer mp, boolean clear)
		{
      	multipalette = mp ;
			if (celList == null) return ;

         // Update the cels to reflect the colors in the new multipalette.

			for (int i = 0 ; i < celList.size() ; i++)
			{
   			Cel cel = (Cel) celList.elementAt(i) ;
           	if (cel.getPalette() == palette)
            {
					cel.changePalette(multipalette) ;
            }
			}

         // Redraw the image and update the preview display.

      	if (image != null)
         {
				Graphics gc = image.getGraphics() ;
				draw(gc,celList,show,clear) ;
	         gc.dispose() ;
            setShowState(false) ;
	         updateImage(image) ;
            setShowState(true) ;
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
				// and the color is not transparent.  Note that pixels with
				// RGB (0,0,1) are converted to (0,0,0).

				int [] pixels = cel.getPixels(box) ;
				if (pixels == null) continue ;
				if (pixels.length == 0) continue ;
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
		// preview scale factor.  There may be rounding error.

		Point getPixel()
		{
			Point p = super.getPixel() ;
			Point q = new Point(p) ;
			p.x = (int) (p.x / sf + 1) ;
			p.y = (int) (p.y / sf + 1) ;
			return p ;
		}

      // If we are previewing an animated GIF, create an animation button
      // on the preview panel toolbar.

      private void updateToolBar()
      {
         JToolBar toolbar = super.getToolBar() ;
         toolbar.remove(animatesep) ;
         toolbar.remove(animate) ;
         if (cel != null && cel.getFrameCount() > 1)
         {
            toolbar.add(animatesep) ;
            toolbar.add(animate) ;
         }
      }

      // Start an image animation thread.

      private void startAnimation()
      {
         if (cel == null || cel.getFrameCount() < 1) return ;
         if (animatethread != null) return ;
         setShowState(false) ;
         animatethread = new Thread()
         {
            public void run()
            {
               try
               {
                  while (true)
                  {
                     int n = 100 ;
                     cel.setNextFrame() ;
                     updateImage(cel.getImage()) ;
                     if (cel instanceof GifCel)
                        n = ((GifCel) cel).getInterval() ;
                     if (n <= 0) n = 100 ;
                     sleep(n) ;
                  }
               }
               catch (InterruptedException e) { }
               cel.setFrame(0) ;
               updateImage(cel.getImage()) ;
            }
         } ;
         animatethread.start() ;
      }

      // Stop an image animation thread.

      void stopAnimation()
      {
         setShowState(true) ;
         animate.setSelected(false) ;
         if (animatethread == null) return ;
         animatethread.interrupt() ;
         animatethread = null ;
         if (framelist != null) framelist.setSelectedIndex(0) ;
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
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.undo(); }
			catch (CannotUndoException ex)
			{
				PrintLn.println("ImageFrame: Unable to undo edit") ;
				ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}

			// Reflect the change in the current palette panel.

			undoredo = true ;
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
				undoall.setEnabled(true) ;
            UNDO.setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName()) ;
			}
			else
			{
				setEnabled(false) ;
				undoall.setEnabled(false) ;
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
			me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
			try { undo.redo() ; }
			catch (CannotRedoException ex)
			{
				PrintLn.println("ImageFrame: Unable to redo edit") ;
				ex.printStackTrace() ;
            JOptionPane.showMessageDialog(null,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}

			// Reflect the change in the current palette panel.

			undoredo = true ;
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


   // Inner class to construct an undoable color count edit change.

   class UndoableColor extends AbstractUndoableEdit
   {
      private Image oldimage = null ;
      private Image newimage = null ;
      private Palette oldpalette = null ;
      private Palette newpalette = null ;
      private String editname = null ;

      // Constructor

      public UndoableColor(Image oldimage, Image newimage,
         Palette oldpalette, Palette newpalette, String name)
      {
         this.oldimage = oldimage ;
         this.newimage = newimage ;
         this.oldpalette = oldpalette ;
         this.newpalette = newpalette ;
         this.editname = name ;
     }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return editname ; }

      // Undo a color change.

      public void undo()
      {
         super.undo() ;
         if (cel != null)
         {
            cel.setImage(oldimage) ;
            cel.setPalette(oldpalette) ;
            cel.setPaletteID((oldpalette != null) ? oldpalette.getIdentifier() : null) ;
            cel.setTransparentIndex((oldpalette != null) ? oldpalette.getTransparentIndex() : -1) ;
            cel.setBackgroundIndex((oldpalette != null) ? oldpalette.getBackgroundIndex() : -1) ;
            cel.setColorsUsed((oldpalette != null) ? oldpalette.getColorCount() : 0) ;
            cel.changePalette(new Integer(0));
//            oldimage = cel.getImage() ;
         }   
         if (preview != null)
         {
            preview.setPalette(oldpalette) ;
            preview.setImage(oldimage) ;
         }
         colorpanel.setImage(oldimage);
         workimage = oldimage ;
         baseimage = oldimage ;
         updateOldImage(oldimage) ;
         updateOldPalette(oldpalette) ;
         updateFramePanel(oldimage) ;
         changed = false ;
      }

      // Redo a color change.

      public void redo()
      {
         super.redo() ;
         if (cel != null)
         {
            cel.setImage(newimage) ;
            cel.setPalette(newpalette) ;
            cel.setPaletteID((newpalette != null) ? newpalette.getIdentifier() : null) ;
            cel.setTransparentIndex((newpalette != null) ? newpalette.getTransparentIndex() : -1) ;
            cel.setBackgroundIndex((newpalette != null) ? newpalette.getBackgroundIndex() : -1) ;
            cel.setColorsUsed((newpalette != null) ? newpalette.getColorCount() : 0) ;
            cel.changePalette(new Integer(0));
//            newimage = cel.getImage() ;
         }
         if (preview != null)
         {
            preview.setPalette(newpalette) ;
            preview.setImage(newimage) ;
         }
         colorpanel.setImage(newimage);
         workimage = newimage ;
         baseimage = newimage ;
         updateOldImage(newimage) ;
         updateOldPalette(newpalette) ;
         updateFramePanel(newimage) ;
         changed = false ;
      }
   }
   


   // Inner class to define a window object for multiple image edits.

   class WindowImageItem extends JCheckBoxMenuItem
   {
      protected Cel cel ;
      protected Object kiss ;
      protected Palette palette ;
      protected boolean changed = false ;
      
      public WindowImageItem(Object o, Cel c, Palette p)
      {
         cel = c ;
         kiss = o ;
         palette = p ;
         setText(toString()) ;
      }
      
      public String toString()
      { 
         KissObject kiss = cel ;
         if (cel == null) kiss = group ;
         if (kiss == null) return "Unknown" ;
         return kiss.getName() ;
      }
      
      public void setChanged(boolean b) { changed = b ; }
      
   }
}



