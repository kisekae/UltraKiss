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
* ToolBar Class
*
* Purpose:
*
* This object encapsulates the main program tool bar.  It is a
* panel that resides in the main frame window.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.ResourceBundle ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.swing.* ;
import javax.swing.border.*;


final class ToolBar extends JPanel
	implements ActionListener
{
	// Class attributes

	static boolean toolBarOn = false ;			// Tool bar view state
	private static final int NEWFILE = 0 ;
	private static final int NEWPAGE = 1 ;

   // Toolbar attributes

	private MainFrame parent = null ;			// The parent frame
	private PanelFrame panel = null ;			// The panel frame
	private JToolBar toolbar = null ;			// The toolbar object
	private KissMenu menu = null ;				// The parent menu

	protected PageButton page[] = null ;		// The page buttons
	protected ColorButton color[] = null ;		// The color buttons
	protected JButton newfile = null ;			// The new file button
	protected JButton newpage = null ;			// The new page button
	protected JButton open = null ;				// The file open button
	protected JButton close = null ;				// The file close button
	protected JButton openurl = null ;			// The open URL button
	protected JButton openweb = null ;			// The open web button
	protected JButton openportal = null ;			// The open kiss button
	protected JButton select = null ;			// The select button
	protected JButton save = null ;				// The file save button
	protected JButton cut = null ;				// The edit cut button
	protected JButton copy = null ;				// The edit copy button
	protected JButton paste = null ;				// The edit paste button
	protected JButton undo = null ;				// The edit undo button
	protected JButton redo = null ;				// The edit redo button
	protected JButton magnify = null ;			// The screen magnify button
	protected JButton reduce = null ;			// The screen reduce button
	protected JButton textedit = null ;			// The text editor tool
	protected JButton coloredit = null ;		// The color editor tool
	protected JButton imageedit = null ;		// The image editor tool
	protected JButton zipedit = null ;        // The archive editor tool
	protected JButton mediaedit = null ;		// The media editor tool
	protected JButton fkissedit = null ;		// The fkiss editor tool
   protected JToggleButton pause = null ;		// The FKiSS pause button
   protected JToggleButton runstate = null ;	// The FKiSS run state indicator
	protected JToggleButton audio = null ;		// The sound button
	protected JToggleButton video = null ;		// The movie button
	protected JToggleButton directkiss = null ; // DirectKiss compatibility 
	protected JToggleButton playfkiss = null ; // PlayFKiss compatibility 
	protected JToggleButton gnomekiss = null ; // GnomeKiss compatibility 
	protected JToggleButton kissld = null ;	 // KissLD compatibility 
	protected JToggleButton editenable = null ;	// The enable edits button
	protected JButton pageset = null ;			// The page right button
	protected JButton colorset = null ;			// The color right button
   private ImageIcon runonicon = null ;      // Run state normal on icon
   private ImageIcon runpauseicon = null ;   // Run state will pause icon
   private ImageIcon runstopicon = null ;    // Run state stopped icon

	private int pagestart = 0 ;					// First page button
	private int pagestop = 0 ;						// Last page button
	private int colorstart = 0 ;					// First color button
	private int colorstop = 0 ;					// Last color button
   private int pageselected = 0 ;				// Selected page button
   private int colorselected = 0 ;				// Selected color button
	private int npages = 0 ;						// Number of page sets
	private int ncolors = 0 ;						// Number of color sets
	private int nbuttons = 0 ;						// Number of buttons in toolbar
	private int nseparators = 0 ;					// Number of separators in toolbar

	// Create a specialized listener for context events.

	MouseListener mouseContextListener = new MouseAdapter()
	{
		public void mousePressed(MouseEvent evt)
		{
			Object source = evt.getSource() ;
			Configuration config = parent.getConfig() ;
			if (config == null) return ;
         if (EventHandler.isModal())
         {
            Toolkit.getDefaultToolkit().beep() ;
            return ;
         }

			// Watch for page buttons.

			try
			{
				for (int i = 0 ; i < page.length ; i++)
				{
					if (source == page[i])
					{
						PageSet page = (PageSet) PageSet.getByKey(
							PageSet.getKeyTable(),config.getID(),Integer.valueOf(i)) ;
						if (page == null) return ;

                  // Right mouse buttons show the page set dialog.

						if (SwingUtilities.isRightMouseButton(evt) && OptionsDialog.getEditEnable())
						{
							Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
							PageSetDialog pd = new PageSetDialog(parent,page,config) ;
							Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
							pd.show() ;
						}

                  // Normal mouse buttons show the page if it is visible.

						else
                  {
                     if (!page.isVisible()) return ;
                  	selectPage(i) ;
							menu.eventPage(i) ;
                  }
						return ;
					}
				}

				// Watch for color buttons.

				for (int i = 0 ; i < color.length ; i++)
				{
					if (source == color[i])
					{
						Palette palette = (Palette) Palette.getByKey(
							Palette.getKeyTable(),config.getID(),Integer.valueOf(0)) ;
						if (palette == null) return ;

                  // Right mouse buttons show the color set dialog.
                  // Alt right mouse buttons show the color edit tool.

						if (SwingUtilities.isRightMouseButton(evt) && OptionsDialog.getEditEnable())
						{
							Window cd = null ;
							Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
							if (!evt.isAltDown())
								cd = new PaletteDialog(parent,palette,Integer.valueOf(i),config) ;
							else
							{
								cd = new ColorFrame(config,palette,Integer.valueOf(i)) ;
								if (menu instanceof ActionListener)
									((ColorFrame) cd).callback.addActionListener((ActionListener) menu) ;
							}
							Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
							if (cd != null) cd.show() ;
						}

                  // Normal left buttons switch to the multipalette group.

						else
                  {
   						if (!color[i].isEnabled()) return ;
                     if (!palette.isVisible(i)) return ;
                  	selectColor(i) ;
							menu.eventColor(i) ;
                  }
						return ;
					}
				}

				// Watch for page shift context changes.

				if (source == pageset)
				{
					int y = evt.getY() ;
					int h = pageset.getSize().height ;

					// Show prior buttons.

					if (y < h/2)
					{
						int maxps = OptionsDialog.getMaxPageSet() ;
						int start = pagestart - maxps ;
						if (start < 0) start = (maxps > 0) ? (npages / maxps) * maxps : 0 ;
						if (start >= npages) start -= maxps ;
						if (start < 0) start = 0 ;
						updateButtons(parent.getConfig(),start,colorstart) ;
						return ;
					}

					// Show next buttons.

					else
					{
						int start = pagestart + OptionsDialog.getMaxPageSet() ;
						if (start >= npages) start = 0 ;
						if (start < 0) start = 0 ;
						if (start < npages)
							updateButtons(parent.getConfig(),start,colorstart) ;
						return ;
					}
				}

				// Watch for color shift context changes.

				if (source == colorset)
				{
					int y = evt.getY() ;
					int h = colorset.getSize().height ;

					// Show prior buttons.

					if (y < h/2)
					{
						int maxcs = OptionsDialog.getMaxColorSet() ;
						int start = colorstart - maxcs ;
						if (start < 0) start = (maxcs > 0) ? (ncolors / maxcs) * maxcs : 0 ;
						if (start >= ncolors) start -= maxcs ;
						if (start < 0) start = 0 ;
						updateButtons(parent.getConfig(),pagestart,start) ;
						return ;
					}

					// Show next buttons.

					else
					{
						int start = colorstart + OptionsDialog.getMaxColorSet() ;
						if (start >= ncolors) start = 0 ;
						if (start < 0) start = 0 ;
						if (start < ncolors)
							updateButtons(parent.getConfig(),pagestart,start) ;
						return ;
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
				PrintLn.println("ToolBar: Out of memory.") ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
					Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
					JOptionPane.ERROR_MESSAGE) ;
			}
		}
	} ;


	// Constructor

	public ToolBar(MainFrame f)
	{
		parent = f ;
		setLayout(new BorderLayout()) ;
		Dimension d = parent.getSize() ;
		Insets insets = parent.getInsets() ;

		// Create the toolbar.

		toolbar = new JToolBar() ;
		toolbar.setFloatable(false) ;
      if (OptionsDialog.getAppleMac()) 
         toolbar.setBackground(Color.LIGHT_GRAY) ;
		createButtons() ;
		updateButtons(parent.getConfig(),0,0) ;
		add(toolbar,BorderLayout.CENTER) ;
	}

   
	// Create the tool bar buttons.

	private void createButtons()
	{
		JButton button = null ;
		URL iconfile = null ;
		page = null ;
		color = null ;
      String ext = ".gif" ;
      if (OptionsDialog.getAppleMac()) ext = ".png" ;

		// New File button

		newfile = new JButton() ;
		iconfile = Kisekae.getResource("Images/new" + ext) ;
		if (iconfile != null) newfile.setIcon(new ImageIcon(iconfile)) ;
		newfile.setMargin(new Insets(1,1,1,1)) ;
		newfile.setToolTipText(Kisekae.getCaptions().getString("ToolTipNewSet")) ;
		newfile.setAlignmentY(0.5f) ;
		newfile.addActionListener(this) ;
      newfile.setEnabled(OptionsDialog.getEditEnable());

		// New Page button

		newpage = new JButton() ;
		iconfile = Kisekae.getResource("Images/leaf" + ext) ;
		if (iconfile != null) newpage.setIcon(new ImageIcon(iconfile)) ;
		newpage.setMargin(new Insets(1,1,1,1)) ;
		newpage.setToolTipText(Kisekae.getCaptions().getString("ToolTipNewPage")) ;
		newpage.setAlignmentY(0.5f) ;
		newpage.addActionListener(this) ;
      newpage.setEnabled(OptionsDialog.getEditEnable());

		// Open button

		open = new JButton() ;
		iconfile = Kisekae.getResource("Images/open" + ext) ;
		if (iconfile != null) open.setIcon(new ImageIcon(iconfile)) ;
		open.setMargin(new Insets(1,1,1,1)) ;
		open.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpenFile")) ;
		open.setAlignmentY(0.5f) ;
		open.addActionListener(this) ;

		// Close button

		close = new JButton() ;
		iconfile = Kisekae.getResource("Images/close" + ext) ;
		if (iconfile != null) close.setIcon(new ImageIcon(iconfile)) ;
		close.setMargin(new Insets(1,1,1,1)) ;
		close.setToolTipText(Kisekae.getCaptions().getString("ToolTipCloseFile")) ;
		close.setAlignmentY(0.5f) ;
		close.addActionListener(this) ;

		// Open URL button

      boolean b = Kisekae.isExpired() || (Kisekae.isSecure()) && !Kisekae.inApplet() ;
		openurl = new JButton() ;
		iconfile = Kisekae.getResource("Images/sphere" + ext) ;
		if (iconfile != null) openurl.setIcon(new ImageIcon(iconfile)) ;
		openurl.setMargin(new Insets(1,1,1,1)) ;
		openurl.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpenURL")) ;
		openurl.setAlignmentY(0.5f) ;
		openurl.addActionListener(this) ;
      openurl.setEnabled(true);

		// Open Web button

		openweb = new JButton() ;
		iconfile = Kisekae.getResource("Images/msbrowser" + ext) ;
      if (BrowserControl.isUnixPlatform())
   		iconfile = Kisekae.getResource("Images/nsbrowser" + ext) ;
      if (BrowserControl.isWindowsPlatform())
   		iconfile = Kisekae.getResource("Images/msbrowser" + ext) ;
      if (BrowserControl.isApplePlatform())
   		iconfile = Kisekae.getResource("Images/macbrowser" + ext) ;
  		if (iconfile != null) openweb.setIcon(new ImageIcon(iconfile)) ;
		openweb.setMargin(new Insets(1,1,1,1)) ;
		openweb.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpenWeb")) ;
		openweb.setAlignmentY(0.5f) ;
		openweb.addActionListener(this) ;
      openweb.setEnabled(!Kisekae.isSecure());

		// Open KiSS button

		openportal = new JButton() ;
		iconfile = Kisekae.getResource("Images/ksbrowser" + ext) ;
		if (iconfile != null) openportal.setIcon(new ImageIcon(iconfile)) ;
		openportal.setMargin(new Insets(1,1,1,1)) ;
		openportal.setToolTipText(Kisekae.getCaptions().getString("ToolTipOpenPortal")) ;
		openportal.setAlignmentY(0.5f) ;
		openportal.addActionListener(this) ;
      openportal.setEnabled(true);

		// Select Configuration button

		select = new JButton() ;
		iconfile = Kisekae.getResource("Images/arrow" + ext) ;
		if (iconfile != null) select.setIcon(new ImageIcon(iconfile)) ;
		select.setMargin(new Insets(1,1,1,1)) ;
		select.setToolTipText(Kisekae.getCaptions().getString("ToolTipSelectCNF")) ;
		select.setAlignmentY(0.5f) ;
		select.addActionListener(this) ;

		// Save button

		save = new JButton() ;
		iconfile = Kisekae.getResource("Images/save" + ext) ;
		if (iconfile != null) save.setIcon(new ImageIcon(iconfile)) ;
		save.setMargin(new Insets(1,1,1,1)) ;
		save.setToolTipText(Kisekae.getCaptions().getString("ToolTipSaveFile"));
		save.setAlignmentY(0.5f) ;
		save.addActionListener(this) ;

		// Cut button

		cut = new JButton() ;
		iconfile = Kisekae.getResource("Images/cut" + ext) ;
		if (iconfile != null) cut.setIcon(new ImageIcon(iconfile)) ;
		cut.setMargin(new Insets(1,1,1,1)) ;
		cut.setToolTipText(Kisekae.getCaptions().getString("ToolTipCut"));
		cut.setAlignmentY(0.5f) ;
		cut.addActionListener(this) ;
      cut.setEnabled(OptionsDialog.getEditEnable());

		// Copy button

		copy = new JButton() ;
		iconfile = Kisekae.getResource("Images/copy" + ext) ;
		if (iconfile != null) copy.setIcon(new ImageIcon(iconfile)) ;
		copy.setMargin(new Insets(1,1,1,1)) ;
		copy.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopy"));
		copy.setAlignmentY(0.5f) ;
		copy.addActionListener(this) ;
      copy.setEnabled(OptionsDialog.getEditEnable());

		// Paste button

		paste = new JButton() ;
		iconfile = Kisekae.getResource("Images/paste" + ext) ;
		if (iconfile != null) paste.setIcon(new ImageIcon(iconfile)) ;
		paste.setMargin(new Insets(1,1,1,1)) ;
		paste.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaste"));
		paste.setAlignmentY(0.5f) ;
		paste.addActionListener(this) ;
      paste.setEnabled(OptionsDialog.getEditEnable());

		// Undo button

		undo = new JButton() ;
		iconfile = Kisekae.getResource("Images/undo" + ext) ;
		if (iconfile != null) undo.setIcon(new ImageIcon(iconfile)) ;
		undo.setMargin(new Insets(1,1,1,1)) ;
		undo.setToolTipText(Kisekae.getCaptions().getString("ToolTipUndo"));
		undo.setAlignmentY(0.5f) ;
		undo.addActionListener(this) ;

		// Redo button

		redo = new JButton() ;
		iconfile = Kisekae.getResource("Images/redo" + ext) ;
		if (iconfile != null) redo.setIcon(new ImageIcon(iconfile)) ;
		redo.setMargin(new Insets(1,1,1,1)) ;
		redo.setToolTipText(Kisekae.getCaptions().getString("ToolTipRedo"));
		redo.setAlignmentY(0.5f) ;
		redo.addActionListener(this) ;

      // Create the run pause button.

      pause = new JToggleButton() ;
      iconfile = Kisekae.getResource("Images/pause" + ext) ;
      if (iconfile != null) pause.setIcon(new ImageIcon(iconfile)) ;
      pause.setMargin(new Insets(1,1,1,1)) ;
      pause.setAlignmentY(0.5f) ;
      pause.addActionListener(this) ;

		// Page shift right button

		pageset = new JButton() ;
		iconfile = Kisekae.getResource("Images/leftright" + ext) ;
		if (iconfile != null) pageset.setIcon(new ImageIcon(iconfile)) ;
		pageset.setMargin(new Insets(1,0,1,0)) ;
		pageset.setToolTipText(Kisekae.getCaptions().getString("ToolTipNextPage"));
		pageset.setAlignmentY(0.5f) ;
		pageset.addMouseListener(mouseContextListener) ;

		// Color shift right button

		colorset = new JButton() ;
		iconfile = Kisekae.getResource("Images/leftright" + ext) ;
		if (iconfile != null) colorset.setIcon(new ImageIcon(iconfile)) ;
		colorset.setMargin(new Insets(1,0,1,0)) ;
		colorset.setToolTipText(Kisekae.getCaptions().getString("ToolTipNextColor"));
		colorset.setAlignmentY(0.5f) ;
		colorset.addMouseListener(mouseContextListener) ;

		// Sound button

		audio = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/sound" + ext) ;
		if (iconfile != null) audio.setIcon(new ImageIcon(iconfile)) ;
		audio.setAlignmentY(0.5f) ;
		audio.addActionListener(this) ;

		// Movie button

		video = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/movie" + ext) ;
		if (iconfile != null) video.setIcon(new ImageIcon(iconfile)) ;
		video.setAlignmentY(0.5f) ;
		video.addActionListener(this) ;

		// Edit enable button

		editenable = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/editenable" + ext) ;
		if (iconfile != null) editenable.setIcon(new ImageIcon(iconfile)) ;
		editenable.setAlignmentY(0.5f) ;
		editenable.addActionListener(this) ;

		// PlayFKiss button

		playfkiss = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/playfkiss" + ext) ;
		if (iconfile != null) playfkiss.setIcon(new ImageIcon(iconfile)) ;
		playfkiss.setAlignmentY(0.5f) ;
		playfkiss.addActionListener(this) ;

		// DirectKiss button

		directkiss = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/directkiss" + ext) ;
		if (iconfile != null) directkiss.setIcon(new ImageIcon(iconfile)) ;
		directkiss.setAlignmentY(0.5f) ;
		directkiss.addActionListener(this) ;

		// GnomeKiss button

		gnomekiss = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/gnomekiss" + ext) ;
		if (iconfile != null) gnomekiss.setIcon(new ImageIcon(iconfile)) ;
		gnomekiss.setAlignmentY(0.5f) ;
		gnomekiss.addActionListener(this) ;

		// KissLD button

		kissld = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/kissld" + ext) ;
		if (iconfile != null) kissld.setIcon(new ImageIcon(iconfile)) ;
		kissld.setAlignmentY(0.5f) ;
		kissld.addActionListener(this) ;

		// Text editor tool button

		textedit = new JButton() ;
		iconfile = Kisekae.getResource("Images/cnfedit2" + ext) ;
		if (iconfile != null) textedit.setIcon(new ImageIcon(iconfile)) ;
		textedit.setMargin(new Insets(1,1,1,1)) ;
		textedit.setAlignmentY(0.5f) ;
		textedit.setToolTipText(Kisekae.getCaptions().getString("TextEditorTitle")) ;
		textedit.addActionListener(this) ;

		// Color editor tool button

		coloredit = new JButton() ;
		iconfile = Kisekae.getResource("Images/clredit2" + ext) ;
		if (iconfile != null) coloredit.setIcon(new ImageIcon(iconfile)) ;
		coloredit.setMargin(new Insets(1,1,1,1)) ;
		coloredit.setAlignmentY(0.5f) ;
		coloredit.setToolTipText(Kisekae.getCaptions().getString("ColorEditorTitle")) ;
		coloredit.addActionListener(this) ;

		// Image editor tool button

		imageedit = new JButton() ;
		iconfile = Kisekae.getResource("Images/imgedit2" + ext) ;
		if (iconfile != null) imageedit.setIcon(new ImageIcon(iconfile)) ;
		imageedit.setMargin(new Insets(1,1,1,1)) ;
		imageedit.setAlignmentY(0.5f) ;
		imageedit.setToolTipText(Kisekae.getCaptions().getString("ImageEditorTitle")) ;
		imageedit.addActionListener(this) ;

		// FKiSS editor tool button

		fkissedit = new JButton() ;
		iconfile = Kisekae.getResource("Images/fkedit2" + ext) ;
		if (iconfile != null) fkissedit.setIcon(new ImageIcon(iconfile)) ;
		fkissedit.setMargin(new Insets(1,1,1,1)) ;
		fkissedit.setAlignmentY(0.5f) ;
		fkissedit.setToolTipText(Kisekae.getCaptions().getString("FKissEditorTitle")) ;
		fkissedit.addActionListener(this) ;

		// Archive editor tool button

		zipedit = new JButton() ;
		iconfile = Kisekae.getResource("Images/zip" + ext) ;
		if (iconfile != null) zipedit.setIcon(new ImageIcon(iconfile)) ;
		zipedit.setMargin(new Insets(1,1,1,1)) ;
		zipedit.setAlignmentY(0.5f) ;
		zipedit.setToolTipText(Kisekae.getCaptions().getString("ArchiveManagerTitle")) ;
		zipedit.addActionListener(this) ;

		// Media editor tool button

		mediaedit = new JButton() ;
		iconfile = Kisekae.getResource("Images/note" + ext) ;
		if (iconfile != null) mediaedit.setIcon(new ImageIcon(iconfile)) ;
		mediaedit.setMargin(new Insets(1,1,1,1)) ;
		mediaedit.setAlignmentY(0.5f) ;
		mediaedit.setToolTipText(Kisekae.getCaptions().getString("MediaPlayerTitle")) ;
		mediaedit.addActionListener(this) ;

		// Magnify editor tool button

		magnify = new JButton() ;
		iconfile = Kisekae.getResource("Images/magnifyplus" + ext) ;
		if (iconfile != null) magnify.setIcon(new ImageIcon(iconfile)) ;
		magnify.setMargin(new Insets(1,1,1,1)) ;
		magnify.setAlignmentY(0.5f) ;
		magnify.setToolTipText(Kisekae.getCaptions().getString("MenuViewMagnify")) ;
		magnify.addActionListener(this) ;

		// Reduce editor tool button

		reduce = new JButton() ;
		iconfile = Kisekae.getResource("Images/magnifyminus" + ext) ;
		if (iconfile != null) reduce.setIcon(new ImageIcon(iconfile)) ;
		reduce.setMargin(new Insets(1,1,1,1)) ;
		reduce.setAlignmentY(0.5f) ;
		reduce.setToolTipText(Kisekae.getCaptions().getString("MenuViewReduce")) ;
		reduce.addActionListener(this) ;

      // Create the run state indicator.

      runstate = new JToggleButton() ;
      runstate.setMargin(new Insets(1,1,1,1)) ;
      runstate.setRequestFocusEnabled(false) ;
      runstate.setAlignmentY(0.5f) ;
      runstate.addActionListener(this) ;
      iconfile = Kisekae.getResource("Images/greenball" + ext) ;
      runonicon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
      if (iconfile != null) runstate.setIcon(runonicon) ;
      iconfile = Kisekae.getResource("Images/yellowball" + ext) ;
      runpauseicon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
      iconfile = Kisekae.getResource("Images/redball" + ext) ;
      runstopicon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
      if (iconfile != null) runstate.setDisabledIcon(new ImageIcon(iconfile)) ;
	}


	// Method to enable the page and color set buttons based upon the
	// state of our current configuration.  We remove all page and color
	// buttons and rebuild the toolbar to reflect our current state.
	// New button arrays are constructed for the configuration only call,
	// otherwise the existing button arrays are shown from the page
	// start and color start indexes.  This must run on the AWT thread.

	void updateButtons(Configuration c) { updateButtons(c,-1,-1) ; }
	void updateButtons(final Configuration c1, final int pstart, final int cstart)
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { updateButtons(c1,pstart,cstart) ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

		nbuttons = 0 ;
		nseparators = 0 ;
		if (toolbar == null) return ;
      MainMenu mm = (parent != null) ? parent.getMainMenu() : null ;
      Configuration c = c1 ;
      if (c != null && c.isClosed()) c = null ;
      
		for (int i = toolbar.getComponentCount()-1 ; i >= 0 ; i--)
		{
			Component comp = toolbar.getComponentAtIndex(i) ;
			toolbar.remove(comp) ;
		}

		// Add the common buttons.

      if (OptionsDialog.getEditEnable())
      {
   		if (c == null)
         {
   			toolbar.add(newfile) ;
    		   nbuttons += 1 ;
         }
//   		else
//   			toolbar.add(newpage) ;
      }
		toolbar.add(open) ;
      toolbar.add(openportal) ;
		nbuttons += 2 ;

      // If no configuration show the URL and browser buttons.
      // Also show the compatibility buttons.

      if (c == null)
      {
   		toolbar.addSeparator() ;
         toolbar.add(openurl) ;
         toolbar.add(openweb) ;
         nbuttons += 2 ;
   		nseparators += 1 ;
         
         if (parent != null)
         {
            if (mm != null && mm.viewtbcompat.isSelected())
            {
         		toolbar.addSeparator() ;
               toolbar.add(playfkiss) ;
               toolbar.add(directkiss) ;
               toolbar.add(gnomekiss) ;
//             toolbar.add(kissld) ;
               nbuttons += 3 ;
               nseparators += 1 ;
            }
            if (mm != null && mm.viewtbtools.isSelected())
            {
         		toolbar.addSeparator() ;
               toolbar.add(textedit) ;
               toolbar.add(coloredit) ;
               toolbar.add(imageedit) ;
               toolbar.add(zipedit) ;
               toolbar.add(mediaedit) ;
               toolbar.add(fkissedit) ;
               nbuttons += 6 ;
               nseparators += 1 ;
            }
         }
      }

		// Add the edit buttons.

      if (c != null)
      {
   		toolbar.add(select) ;
         toolbar.add(close) ;
  		   nbuttons += 2 ;
         
         if (OptionsDialog.getEditEnable())
         {
      		toolbar.add(save) ;
            nbuttons += 1 ;
         }
         
         if (parent != null)
         {
            if (mm != null && mm.viewtbcompat.isSelected())
            {
         		toolbar.addSeparator() ;
               toolbar.add(playfkiss) ;
               toolbar.add(directkiss) ;
               toolbar.add(gnomekiss) ;
//             toolbar.add(kissld) ;
               nbuttons += 3 ;
               nseparators += 1 ;
            }
            if (mm != null && mm.viewtbtools.isSelected())
            {
               if (!OptionsDialog.getSecurityEnable())
               {
                  toolbar.addSeparator() ;
                  toolbar.add(textedit) ;
                  toolbar.add(coloredit) ;
                  toolbar.add(imageedit) ;
                  toolbar.add(zipedit) ;
                  toolbar.add(mediaedit) ;
                  toolbar.add(fkissedit) ;
                  nbuttons += 6 ;
                  nseparators += 1 ;
               }
            }
         }

         if (OptionsDialog.getEditEnable())
         {
            if (mm != null && mm.viewtbedits.isSelected())
            {
               toolbar.addSeparator() ;
               toolbar.add(cut) ;
               toolbar.add(copy) ;
               toolbar.add(paste) ;
               toolbar.addSeparator() ;
               toolbar.add(undo) ;
               toolbar.add(redo) ;
               toolbar.addSeparator() ;
               toolbar.add(magnify) ;
               toolbar.add(reduce) ;
               toolbar.addSeparator() ;
               toolbar.add(pause) ;
               toolbar.add(editenable) ;
               nbuttons += 9 ;
               nseparators += 4 ;
            }
         }
      }

		// Initialize for the new page buttons.

		npages = (c == null) ? 0 : c.getPageCount() ;
		int max = OptionsDialog.getMaxPageSet() ;
		if (page == null || c == null) page = new PageButton[npages] ;
		if (page.length < npages) page = new PageButton[npages] ;
		if (pstart < 0) page = new PageButton[npages] ;
		pagestart = (pstart < 0) ? 0 : pstart ;

		// Iterate from the start button number to the maximum allowed.

		for (int i = pagestart ; i < npages ; i++)
		{
			if (i == pagestart + max) pagestop = i - 1 ;
			if (i - pagestart == max)	break ;

			// Create a button.

			if (page[i] == null)
			{
				page[i] = new PageButton(i) ;
            String s = Kisekae.getCaptions().getString("ToolTipPage") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1) s = s.substring(0,i1+1) + i + s.substring(j1) ;
				page[i].setToolTipText(s) ;
				page[i].setSize(open.getSize()) ;
				page[i].setPreferredSize(open.getPreferredSize()) ;
				page[i].setMaximumSize(open.getMaximumSize()) ;
				page[i].setAlignmentY(0.5f) ;
				page[i].addMouseListener(mouseContextListener) ;
				PageSet p = (c == null) ? null : c.getPage(i) ;
				page[i].setEnabled((p == null) ? false : (p.getGroupCount() > 0)) ;
            if (p != null && !p.isVisible()) page[i].setEnabled(false) ;
			}

			// Add the button to the toolbar.

         if (mm != null && mm.viewtbpages.isSelected())
         {
            if (i == pagestart)
            {
               toolbar.addSeparator() ;
               nseparators++ ;
            }

            toolbar.add(page[i]) ;
            nbuttons++ ;
         }
		}

		// Insert the shift button if necessary.

		if (npages > max && max > 0)
		{
         if (mm != null && mm.viewtbpages.isSelected())
         {
            toolbar.add(pageset) ;
            nbuttons++ ;
         }
		}

		// Initialize for the new color buttons.  We calculate the number of
      // color sets.  This is defined by the maximum number of palette groups
      // across all palette files.

      int n = 0 ;
      ncolors = 0 ;
      Palette p = null ;
      while (c != null)
      {
   		Palette p1 = (Palette) Palette.getByKey(Palette.getKeyTable(),c.getID(),Integer.valueOf(n++)) ;
         if (p1 == null) break ;
         if (n == 1) p = p1 ;
         int multipalettes = p1.getMultiPaletteCount() ;
         if (multipalettes > ncolors) ncolors = multipalettes ;
      }

      // Create color buttons.

		max = OptionsDialog.getMaxColorSet() ;
		if (color == null || c == null) color = new ColorButton[ncolors] ;
		if (color.length < ncolors) color = new ColorButton[ncolors] ;
		if (cstart < 0) color = new ColorButton[ncolors] ;
		colorstart = (cstart < 0) ? 0 : cstart ;

		// Iterate from the start button number to the maximum allowed.

		for (int i = colorstart ; i < ncolors ; i++)
		{
			if (i == colorstart + max) colorstop = i - 1 ;
			if (i - colorstart == max)	break ;

			// Create a button.

			if (color[i] == null)
			{
				color[i] = new ColorButton(i) ;
            String s = Kisekae.getCaptions().getString("ToolTipColor") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1) s = s.substring(0,i1+1) + i + s.substring(j1) ;
				color[i].setToolTipText(s) ;
				color[i].setSize(open.getSize()) ;
				color[i].setPreferredSize(open.getPreferredSize()) ;
				color[i].setMaximumSize(open.getMaximumSize()) ;
				color[i].setAlignmentY(0.5f) ;
				color[i].addMouseListener(mouseContextListener) ;
				color[i].setColor((p == null) ? null : p.getColor(i,0));
				color[i].setEnabled((p == null) ? false : (i < ncolors)) ;
            if (p != null && !p.isVisible(i)) color[i].setEnabled(false) ;
			}

			// Add the button to the toolbar.

         if (mm != null && mm.viewtbcolors.isSelected())
         {
            if (i == colorstart)
            {
               toolbar.addSeparator() ;
               nseparators++ ;
            }
            toolbar.add(color[i]) ;
            nbuttons++ ;
         }
      }

		// Insert the shift button if necessary.

 		if (ncolors > max && max > 0)
      {
         if (mm != null && mm.viewtbcolors.isSelected())
         {
            toolbar.add(colorset) ;
            nbuttons++ ;
         }
      }
      
      // Set our edit enable state.
      
		editenable.setSelected(OptionsDialog.getTempEditEnable()) ;
		editenable.setPreferredSize(open.getPreferredSize()) ;
		if (editenable.isSelected())
			editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOn"));
		else
			editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOff"));
      
      // Set our tool availability state.
      
		fkissedit.setVisible(c != null) ;
      
      // Set our compatibility state.
      
		playfkiss.setSelected(OptionsDialog.getPlayFKissCompatibility()) ;
		playfkiss.setPreferredSize(open.getPreferredSize()) ;
		if (playfkiss.isSelected())
			playfkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayFKissOn"));
		else
			playfkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayFKissOff"));
      
		directkiss.setSelected(OptionsDialog.getDirectKissCompatibility()) ;
		directkiss.setPreferredSize(open.getPreferredSize()) ;
		if (directkiss.isSelected())
			directkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipDirectKissOn"));
		else
			directkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipDirectKissOff"));
      
		gnomekiss.setSelected(OptionsDialog.getGnomeKissCompatibility()) ;
		gnomekiss.setPreferredSize(open.getPreferredSize()) ;
		if (gnomekiss.isSelected())
			gnomekiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipGnomeKissOn"));
		else
			gnomekiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipGnomeKissOff"));
      
		kissld.setSelected(OptionsDialog.getKissLDCompatibility()) ;
		kissld.setPreferredSize(open.getPreferredSize()) ;
		if (kissld.isSelected())
			kissld.setToolTipText(Kisekae.getCaptions().getString("ToolTipKissLDOn"));
		else
			kissld.setToolTipText(Kisekae.getCaptions().getString("ToolTipKissLDOff"));
      
      // Set our breakpoint enable state.
      
		pause.setSelected(FKissEvent.getBreakPause()) ;
		pause.setPreferredSize(open.getPreferredSize()) ;
		if (pause.isSelected())
			pause.setToolTipText(Kisekae.getCaptions().getString("ToolTipBreakpointOn"));
		else
			pause.setToolTipText(Kisekae.getCaptions().getString("ToolTipBreakpointOff"));
      
      // Set our runstate indicator
      
      runstate.setSelected(OptionsDialog.getFKissOn()) ;

		// Update and add the sound and movie control buttons.
		// Action the button state.

		audio.setSelected(OptionsDialog.getSoundOn()) ;
		audio.setPreferredSize(open.getPreferredSize()) ;
		if (audio.isSelected())
			audio.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundOn"));
		else
			audio.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundOff"));
		video.setSelected(OptionsDialog.getMovieOn()) ;
		video.setPreferredSize(open.getPreferredSize()) ;
		if (video.isSelected())
			video.setToolTipText(Kisekae.getCaptions().getString("ToolTipMoviesOn"));
		else
			video.setToolTipText(Kisekae.getCaptions().getString("ToolTipMoviesOff"));
      
		toolbar.addSeparator() ;
		toolbar.add(Box.createGlue()) ;
		toolbar.add(audio, null) ;
		toolbar.add(video, null) ;
      nbuttons += 2 ;
  		nseparators += 1 ;
      if (mm != null && mm.viewtbedits.isSelected())
      {
         if (c != null) toolbar.add(runstate,null) ;
   		nbuttons += 1 ;
      }

		// Update the file buttons.

		update() ;
		validate() ;
		repaint() ;
	}



	// Utility method to adjust our toolbar button states based upon the
	// current menu and panel context.

	void update()
	{
   	boolean b1 = false ;
   	boolean b2 = false ;
   	boolean b3 = false ;
   	boolean b4 = false ;
   	boolean b5 = false ;
   	boolean b6 = false ;
      PageSet pageset = null ;
      Integer multipalette = null ;

		if (parent == null) return ;
		menu = parent.getMenu() ;
      if (menu instanceof UserMenu) menu = parent.getPanelMenu() ;
		panel = parent.getPanel() ;
      if (panel != null)
      {
			b3 = panel.isEditOn() && OptionsDialog.getEditEnable() ;
			b4 = panel.isClipOn() && OptionsDialog.getEditEnable() ;
         pageset = panel.getPage() ;
         multipalette = panel.getMultiPalette() ;
      }

      if (menu instanceof PanelMenu)
      {
         b1 = true ;
         b2 = true ;
			Configuration c = parent.getConfig() ;
	      ArchiveFile zip = (c != null) ? c.getZipFile() : null ;
	      String directory = (zip != null) ? zip.getDirectoryName() : null ;
			b2 = (directory != null) ;
			PanelMenu panelmenu = (PanelMenu) menu ;
			Action undoaction = panelmenu.getUndoAction() ;
			Action redoaction = panelmenu.getRedoAction() ;
			if (undoaction != null) b5 = undoaction.isEnabled() ;
			if (redoaction != null) b6 = redoaction.isEnabled() ;
		}

		select.setEnabled(b1) ;
		save.setEnabled(b2 && !Kisekae.isExpired()) ;
		cut.setEnabled(b3) ;
		copy.setEnabled(b3) ;
		paste.setEnabled(b4) ;
		undo.setEnabled(b5) ;
		redo.setEnabled(b6) ;
      newfile.setEnabled(OptionsDialog.getEditEnable()) ;
      newpage.setEnabled(OptionsDialog.getEditEnable()) ;

		// Update our page set buttons.

		Configuration c = parent.getConfig() ;
      Integer pageid = (pageset == null) ? null : (Integer) pageset.getIdentifier() ;
      selectPage((pageid == null) ? -1 : pageid.intValue()) ;
      if (page != null)
      {
         for (int i = 0 ; i < page.length ; i++)
         {
            if (page[i] == null) continue ;
            PageSet p = (c == null) ? null : c.getPage(i) ;
            page[i].setEnabled((p == null) ? false : (p.getGroupCount() > 0)) ;
            if (p != null && !p.isVisible()) page[i].setEnabled(false) ;
         }
      }

		// Update our color set buttons.

      selectColor((multipalette == null) ? -1 : multipalette.intValue()) ;
		Palette p = (c == null) ? null :
			(Palette) Palette.getByKey(Palette.getKeyTable(),c.getID(),Integer.valueOf(0)) ;
      if (color != null)
      {
         for (int i = 0 ; i < color.length ; i++)
         {
            if (color[i] == null) continue ;
            color[i].setEnabled((p != null) ? p.isVisible(i) : true) ;
         }
      }
      
      // Set our edit enable state.
      
		editenable.setSelected(OptionsDialog.getTempEditEnable()) ;
		if (editenable.isSelected())
			editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOn"));
		else
			editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOff"));
      
      // Set the tools enable state

		textedit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
		coloredit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
		imageedit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
		zipedit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
		mediaedit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
		fkissedit.setEnabled(!OptionsDialog.getSecurityEnable()) ;
      
      // Set our compatibility state.
      
      updateCompatibilityState() ;
      
      // Update our run state indicator.
      
      updateRunState() ;
	}


   // Update our operational run state indicator.

   void updateRunState()
   {
      if (parent == null) return ;
      boolean active = EventHandler.isActive() ;
      Configuration config = parent.getConfig() ;
      EventHandler handler = (config != null) ? config.getEventHandler() : null ;
      int eventcount = (handler != null) ? handler.getEventCount() : 0 ;
      pause.setEnabled(eventcount > 0 && OptionsDialog.getEditEnable()) ;
      pause.setSelected(FKissEvent.getBreakPause());
		if (pause.isSelected())
			pause.setToolTipText(Kisekae.getCaptions().getString("ToolTipBreakpointOn"));
		else
			pause.setToolTipText(Kisekae.getCaptions().getString("ToolTipBreakpointOff"));
      
      ImageIcon runicon = (FKissEvent.getBreakPause()) ? runpauseicon : runonicon ;
      if (active && runicon != null) runstate.setIcon(runicon) ;
      else if (runstopicon != null) runstate.setIcon(runstopicon) ;
      String s = (config != null) ? config.getName() : "" ;
      String s1 = Kisekae.getCaptions().getString("RunStatePausedText") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
      String s2 = Kisekae.getCaptions().getString("RunStateBreakText") ;
      i1 = s2.indexOf('[') ;
      j1 = s2.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s2 = s2.substring(0,i1) + s + s2.substring(j1+1) ;
      String s3 = Kisekae.getCaptions().getString("RunStateActiveText") ;
      i1 = s3.indexOf('[') ;
      j1 = s3.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s3 = s3.substring(0,i1) + s + s3.substring(j1+1) ;
      runstate.setToolTipText((!active) ? s1
         : ((FKissEvent.getBreakPause()) ? s2 : s3)) ;
      if (eventcount == 0)
      {
         runstate.setIcon(runonicon) ;
         runstate.setToolTipText(s3) ;
      }
      repaint() ;
   }


   // Update our compatibility state indicators.

   void updateCompatibilityState()
   {
		playfkiss.setSelected(OptionsDialog.getPlayFKissCompatibility()) ;
      if (playfkiss.isSelected())
			playfkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayFKissOn"));
      else
			playfkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipPlayFKissOff"));
      
		directkiss.setSelected(OptionsDialog.getDirectKissCompatibility()) ;
      if (directkiss.isSelected())
			directkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipDirectKissOn"));
      else
			directkiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipDirectKissOff"));
      
		gnomekiss.setSelected(OptionsDialog.getGnomeKissCompatibility()) ;
      if (gnomekiss.isSelected())
			gnomekiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipGnomeKissOn"));
      else
			gnomekiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipGnomeKissOff"));
      
		kissld.setSelected(OptionsDialog.getKissLDCompatibility()) ;
      if (kissld.isSelected())
			kissld.setToolTipText(Kisekae.getCaptions().getString("ToolTipKissLDOn"));
      else
			kissld.setToolTipText(Kisekae.getCaptions().getString("ToolTipKissLDOff"));
   }



   // Utility function to select a page button.  This unselects the current
   // (pressed) button and selects (presses) the new button.  The button
   // states are adjusted acordingly.


   private void selectPage(int n)
   {
   	if (page == null) return ;
      PageButton onbutton = null ;
   	PageButton offbutton = null ;

      if (pageselected >= 0 && pageselected < page.length)
	     	offbutton = page[pageselected] ;
      if (offbutton != null) offbutton.setSelected(false) ;
      if (n < 0 || n >= page.length) return ;
      onbutton = page[n] ;
      if (onbutton != null) onbutton.setSelected(true) ;
      pageselected = n ;
   }



   // Utility function to select a color button.  This unselects the current
   // (pressed) button and selects (presses) the new button.  The button
   // states are adjusted acordingly.

   private void selectColor(int n)
   {
   	if (color == null) return ;

      ColorButton onbutton = null ;
   	ColorButton offbutton = null ;

      if (colorselected >= 0 && colorselected < color.length)
	     	offbutton = color[colorselected] ;
      if (offbutton != null) offbutton.setSelected(false) ;
      if (n < 0 || n >= color.length) return ;
      onbutton = color[n] ;
      if (onbutton != null) onbutton.setSelected(true) ;
      colorselected = n ;
   }



	// The action method is used to process control menu events.
	// This method is required as part of the ActionListener interface.
	// The implementation for all event definitions can be found in the
	// main menu object.

	public void actionPerformed(ActionEvent evt)
	{
		if (parent == null) return ;
		menu = parent.getMenu() ;
      if (menu == null && !OptionsDialog.getInitMenubar())
         menu = (parent.getPanel() != null) ? parent.getPanelMenu() : parent.getMainMenu() ;
      if (menu instanceof UserMenu) 
         menu = parent.getPanelMenu() ;
		if (menu == null) return ;
		Object source = evt.getSource() ;
      parent.showStatus(null) ;

		try
		{
			// A new configuration request.

			if (source == newfile)
         	{ menu.eventNew(NEWFILE) ; return ; }

			// A new page request.

			if (source == newpage)
         	{ menu.eventNew(NEWPAGE) ; return ; }

			// A file open request.

			if (source == open)
            { menu.eventOpen() ; return ; }

			// A file close request.

			if (source == close)
            { menu.eventClose() ; return ; }

			// An open URL request.

			if (source == openurl)
         	{ menu.eventUrl() ; return ; }

			// An open web browser request.

			if (source == openweb)
         	{ menu.eventWeb() ; return ; }

			// An open KiSS browser request.

			if (source == openportal)
         	{ menu.eventPortal() ; return ; }

			// A configuration select request.

			if (source == select)
         	{ menu.eventSelect() ; return ; }

			// A file save request.

			if (source == save)
         	{ menu.eventSave(1) ; return ; }

			// An edit cut request.

			if (source == cut)
				{ menu.eventCut() ; return ; }

			// An edit copy request.

			if (source == copy)
				{ menu.eventCopy() ; return ; }

			// An edit paste request.

			if (source == paste)
				{ menu.eventPaste() ; return ; }

			// A magnify screen request.

			if (source == magnify)
				{ menu.eventMagnify(1) ; return ; } 

			// A reduce screen request.

			if (source == reduce)
				{ menu.eventMagnify(-1) ; return ; } 

			// An edit undo request.

			if (source == undo)
			{
         	if (!(menu instanceof PanelMenu)) return ;
            Action undoAction = ((PanelMenu) menu).getUndoAction() ;
            if (undoAction == null) return ;
         	undoAction.actionPerformed(evt) ;
            return ;
         }

			// An edit redo request.

			if (source == redo)
			{
         	if (!(menu instanceof PanelMenu)) return ;
            Action redoAction = ((PanelMenu) menu).getRedoAction() ;
            if (redoAction == null) return ;
         	redoAction.actionPerformed(evt) ;
            return ;
         }

         // An FKiSS pause request.

         if (source == pause)
         {
            FKissEvent.setBreakPause(FKissEvent.getBreakFrame(),pause.isSelected()) ;
            update() ;
            return ;
         }

         // An edit enable/disable request.

         if (source == editenable)
         {
            OptionsDialog.setTempEditEnable(editenable.isSelected()) ;
         	if (editenable.isSelected())
					editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOn"));
            else
					editenable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditEnableOff"));
            return ;
         }

         // A PlayFKiss compatibility request.

         if (source == playfkiss)
         {
            Configuration config = null ;
            if (parent != null) config = parent.getConfig() ;
            boolean b = (config == null) ;
            OptionsDialog.setPlayFKissCompatibility(playfkiss.isSelected(),b) ;
            OptionsDialog.setCompatibilityControls(playfkiss.isSelected()) ;
            updateCompatibilityState() ;
                  
            // Configuration is not restartable if these options change.
       
            if (config != null) 
            {
               config.setOptionsChanged(true) ;
               config.setRestartable(false) ;
               String s = Kisekae.getCaptions().getString("OptionsDialogRestartText2") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1) 
                   s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
               JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("OptionsDialogRestartText1") + "\n" + s,
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.INFORMATION_MESSAGE) ;
               parent.restart();
            }
            return ;
         }

         // A DirectKiss compatibility request.

         if (source == directkiss)
         {
            Configuration config = null ;
            if (parent != null) config = parent.getConfig() ;
            boolean b = (config == null) ;
            OptionsDialog.setDirectKissCompatibility(directkiss.isSelected(),b) ;
            OptionsDialog.setCompatibilityControls(directkiss.isSelected()) ;
            updateCompatibilityState() ;
            return ;
         }

         // A GnomeKiss compatibility request.

         if (source == gnomekiss)
         {
            Configuration config = null ;
            if (parent != null) config = parent.getConfig() ;
            boolean b = (config == null) ;
            OptionsDialog.setGnomeKissCompatibility(gnomekiss.isSelected(),b) ;
            OptionsDialog.setCompatibilityControls(gnomekiss.isSelected()) ;
            updateCompatibilityState() ;
            return ;
         }

         // A GnomeKiss compatibility request.

         if (source == kissld)
         {
            Configuration config = null ;
            if (parent != null) config = parent.getConfig() ;
            boolean b = (config == null) ;
            OptionsDialog.setKissLDCompatibility(kissld.isSelected(),b) ;
            OptionsDialog.setCompatibilityControls(kissld.isSelected()) ;
            updateCompatibilityState() ;
            return ;
         }

         // A run state enable/disable request.

         if (source == runstate)
         {
            OptionsDialog.setFKissOn(runstate.isSelected()) ;
         	if (runstate.isSelected())
					EventHandler.resumeEventHandler(true) ;
            else
					EventHandler.suspendEventHandler(true) ;
            updateRunState() ;
            return ;
         }

			// A sound on/off request.

			if (source == audio)
         {
         	if (audio.isSelected())
					audio.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundOn"));
            else
					audio.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundOff"));
         	menu.eventSound(audio.isSelected()) ;
            return ;
         }

			// A movie on/off request.

			if (source == video)
         {
         	if (video.isSelected())
					video.setToolTipText(Kisekae.getCaptions().getString("ToolTipMoviesOn"));
            else
					video.setToolTipText(Kisekae.getCaptions().getString("ToolTipMoviesOff"));
         	menu.eventMovie(video.isSelected()) ;
            return ;
         }

			// A text editor request.

			if (source == textedit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            KissFrame kf = new TextFrame() ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

			// A color editor request.

			if (source == coloredit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            KissFrame kf = new ColorFrame() ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

			// An image editor request.

			if (source == imageedit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            KissFrame kf = new ImageFrame() ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

			// An fkiss editor request.

			if (source == fkissedit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            Configuration config = parent.getConfig() ;
            KissFrame kf = new FKissFrame(config) ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

			// A media player request.

			if (source == mediaedit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            KissFrame kf = new MediaFrame() ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

			// An archive manager request.

			if (source == zipedit)
         {
   			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            KissFrame kf = new ZipManager() ;
            kf.setVisible(true) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
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
			PrintLn.println("ToolBar: Out of memory.") ;
			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

      // Watch for internal faults.  Close the configuration.

		catch (Throwable e)
		{
         EventHandler.stopEventHandler() ;
	      PrintLn.println("ToolBar: Internal fault, action " + evt.getActionCommand()) ;
         e.printStackTrace() ;
			Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("InternalError") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
			parent.closeconfig() ;
		}
	}


	// Inner class to create a color button.

	class ColorButton extends JButton
	{
		private int id = 0 ;
		private int w = 0 ;
		private int h = 0 ;
		private Color color = null ;
		private boolean enabled = true ;
		private boolean selected = false ;

		public ColorButton(int n)
		{
			id = n ;
			h = 3 ;
			w = (n < 10) ? 3 : 2 ;
			color = SystemColor.inactiveCaptionBorder ;
			setBackground(color) ;
			setBorder(createBorder(color,w,h)) ;
         setFont(getFont().deriveFont(Font.PLAIN,10)) ;
		}


		public void setEnabled(boolean b)
		{
         super.setEnabled(b) ;
			enabled = b ;
			if (enabled)
				setBorder(createBorder(color,w,h)) ;
			else
				setBorder(createBorder(SystemColor.inactiveCaptionBorder,w,h)) ;
		}


		public boolean isEnabled() { return enabled ; }


		public void setSelected(boolean b)
		{
			selected = b ;
			if (enabled)
				setBorder(createBorder(color,w,h)) ;
			else
				setBorder(createBorder(SystemColor.inactiveCaptionBorder,w,h)) ;
		}


		public boolean isSelected() { return selected ; }


		public void setColor(Color c)
		{
			color = (c == null) ? SystemColor.inactiveCaptionBorder : c ;
			if (enabled) setBorder(createBorder(color,w,h)) ;
		}


		public void paintComponent(Graphics g)
		{
			super.paintComponent(g) ;
			Dimension size = getSize() ;
			Insets insets = getInsets() ;
			int x = insets.left ;
			int y = insets.top ;
			int w = size.width - insets.left - insets.right ;
			int h = size.height - insets.top - insets.bottom ;
			g.setColor((isEnabled()) ? color : SystemColor.inactiveCaptionBorder) ;
			g.fillRect(x,y,w,h) ;
			String s = "" + id ;
			FontMetrics fm = g.getFontMetrics() ;
         Color textcolor = Color.black ;
   		int rgb = color.getRGB() ;
   		int red = rgb & (255<<16) >> 16 ;
   		int green = rgb & (255<<8) >> 8 ;
   		int blue = rgb & (255) ;
   		float hsb[] = Color.RGBtoHSB(red,green,blue,null) ;
   		if (hsb[2] < 0.5) textcolor = Color.white ;
			g.setColor((isEnabled()) ? textcolor : SystemColor.controlShadow) ;
			int sw = fm.stringWidth(s) ;
			int sh = fm.getMaxAscent() ;
			g.drawString(s,x+(w-sw)/2,y+((h-sh)/2)+sh-2) ;
		}


		private Border createBorder(Color c, int w, int h)
		{
         Color cs = SystemColor.controlShadow ;
			if (c == null) c = SystemColor.inactiveCaptionBorder ;
      	int n = (selected) ? BevelBorder.LOWERED : BevelBorder.RAISED ;
			Border bb1 = BorderFactory.createBevelBorder(n,cs.brighter(),cs.darker()) ;
			Border lb1 = BorderFactory.createLineBorder(c) ;
			Border eb1 = BorderFactory.createEmptyBorder(h,w,h,w) ;
			Border cb1 = BorderFactory.createCompoundBorder(bb1,eb1) ;
			Border cb2 = BorderFactory.createCompoundBorder(cb1,lb1) ;
			return cb2 ;
		}
	}


	// Inner class to create a page button.

	class PageButton extends JButton
	{
		private int id = 0 ;
		private int w = 0 ;
		private int h = 0 ;
		private boolean enabled = true ;
		private boolean selected = false ;

		public PageButton(int n)
		{
			id = n ;
			h = 3 ;
			w = (n < 10) ? 3 : 2 ;
			setBackground(SystemColor.inactiveCaptionBorder) ;
			setBorder(createBorder(SystemColor.controlShadow,w,h)) ;
         setFont(getFont().deriveFont(Font.PLAIN,10)) ;
		}


		public void setEnabled(boolean b)
		{
         super.setEnabled(b) ;
			enabled = b ;
			if (enabled)
				setBorder(createBorder(Color.black,w,h)) ;
			else
				setBorder(createBorder(SystemColor.controlShadow,w,h)) ;
		}

		public boolean isEnabled() { return enabled ; }


		public void setSelected(boolean b)
		{
			selected = b ;
			if (enabled)
				setBorder(createBorder(Color.black,w,h)) ;
			else
				setBorder(createBorder(SystemColor.controlShadow,w,h)) ;
		}

		public boolean isSelected() { return selected ; }


		public void paintComponent(Graphics g)
		{
			super.paintComponent(g) ;
			Dimension size = getSize() ;
			Insets insets = getInsets() ;
			int x = insets.left ;
			int y = insets.top ;
			int w = size.width - insets.left - insets.right ;
			int h = size.height - insets.top - insets.bottom ;
			g.setColor((isEnabled()) ? Color.white : SystemColor.inactiveCaptionBorder) ;
			g.fillRect(x,y,w,h) ;
			String s = "" + id ;
			FontMetrics fm = g.getFontMetrics() ;
			g.setColor((isEnabled()) ? Color.black : SystemColor.controlShadow) ;
			int sw = fm.stringWidth(s) ;
			int sh = fm.getMaxAscent() ;
			g.drawString(s,x+(w-sw)/2,y+((h-sh)/2)+sh-2) ;
		}


		private Border createBorder(Color c,int w,int h)
		{
         Color cs = SystemColor.controlShadow ;
      	int n = (selected) ? BevelBorder.LOWERED : BevelBorder.RAISED ;
			Border bb1 = BorderFactory.createBevelBorder(n,cs.brighter(),cs.darker()) ;
			Border lb1 = BorderFactory.createLineBorder(c) ;
			Border eb1 = BorderFactory.createEmptyBorder(h,w,h,w) ;
			Border cb1 = BorderFactory.createCompoundBorder(bb1,eb1) ;
			Border cb2 = BorderFactory.createCompoundBorder(cb1,lb1) ;
			return cb2 ;
		}
	}
}



