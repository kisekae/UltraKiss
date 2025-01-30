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
* TextFrame Class
*
* Purpose:
*
* This object is a general text file editor.  It is used to edit
* configuration files and other text files.  This editor supports
* normal text files and rich text files.
*
* Normal text files can change text fonts but cannot adjust text attributes
* such as bold and italic.  Rich text files can adjust text attributes and
* color but cannot display line numbers.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.event.* ;
import java.awt.print.* ;
import java.awt.font.* ;
import java.awt.geom.Rectangle2D ;
import java.util.Hashtable ;
import java.util.Collections ;
import java.util.Enumeration ;
import java.util.Vector ;
import java.util.Locale ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.text.* ;
import javax.swing.text.rtf.* ;
import javax.swing.text.html.* ;
import javax.swing.undo.* ;
import javax.swing.plaf.* ;
import javax.swing.plaf.basic.* ;
import java.net.URL ;
import java.net.MalformedURLException ;


final class TextFrame extends KissFrame
	implements UndoableEditListener, DocumentListener, ActionListener,
   	ItemListener, WindowListener, Printable
{
	private static final int EDIT = 0 ;  		// Edit text mode
	private static final int INPUT = 1 ;     	// Input text mode
	private static final int INSERT = 0 ;		// Insert text
	private static final int OVERTYPE = 1 ;  	// Overtype text
   private static final int FINDTAB = 0 ;		// Find dialog
   private static final int REPLACETAB = 1 ;	// Replace dialog

	private static String helpset = "Help/TextEditor.hs" ;
	private static String helpsection = "texteditor.index" ;
	private static String onlinehelp = "texteditor/index.html" ;
	private static String refset = "Help/Reference2.hs" ;
	private static String refsection = "reference2.index" ;
   private AboutBox aboutdialog = null ;
	private HelpLoader helper = null ;
	private HelpLoader helper2 = null ;

	// Editor interface objects

	private TextFrame me = null ;					// Reference to ourselves
	private JTextComponent text = null ;	 	// The text component
   private Document doc = null ;	 				// The text document
   private EditorKit kit = null ;	 			// The RTF styled editor
   private StyleContext context = null ;	 	// The RTF style
   private TextObject textobject = null ;		// The KiSS object wrapper
	private TextPanel scrollpanel = null ;		// The scroll panel
	private JScrollPane scrollpane = null ;	// The scroll pane
	private Hashtable actions = null ;			// The edit actions
	private Hashtable errormsgs = null ;		// The text line messages
	private String fontname = "" ;				// The name of the font
	private int fontsize = 0 ; 					// The size of the font
	private int selstart = -1 ; 					// The selection start
	private int selfinish = -1 ;              // The selection finish
	private int caret = 0 ;							// The caret position
   private int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;

	// Status attributes

	private int editmode = INPUT ;		  		// The editor mode
	private int insertmode = INSERT ;	  		// The text insert mode
	private boolean showline = false ;			// True, show line numbers
	private boolean changed = false ;			// True, document has changed
	private boolean error = false ;				// True, document is in error
	private boolean skipupdate = false ;		// True, do not update toolbar
	private boolean fkisshelp = false ;		   // True, show fkiss tooltips

	// File objects

   private FileOpen fileopen = null ;			// The current directory
	private ArchiveFile zip = null ;				// The zip file
	private ArchiveEntry ze = null ;				// The zip file entry
	private ArchiveEntry originalze = null ; 	// The original zip file entry
	private String file = null ;					// The file being edited
	private String extension = null ;			// The file extension being edited
	private String type = null ;					// The type of file being edited
	private String originalfile = null ;		// The original file name
	private String directory = null ;			// The file directory
	private InputStream in = null ;				// The data input stream

	// Undo helpers and edit helpers.

	private UndoManager undo = new UndoManager() ;
	private UndoAction undoAction = null ;	  	// Action for undo
	private RedoAction redoAction = null ;	  	// Action for redo
   private TextFindDialog findDialog = null ;  // Find dialog

	// Menu items

   private JMenuBar mb ;
   private JMenu fileMenu ;
   private JMenu editMenu ;
   private JMenu formatMenu ;
   private JMenu windowMenu ;
   private JMenu helpMenu ;
   private JMenuItem undoall = null ;
	private JMenuItem update ;
	private JMenuItem newdoc ;
	private JMenuItem open ;
	private JMenuItem save ;
	private JMenuItem saveas ;
	private JMenuItem print ;
   private JMenuItem printpreview ;
   private JMenuItem pagesetup ;
	private JMenuItem properties ;
	private JMenuItem exit ;
	private JMenuItem cut ;
	private JMenuItem copy ;
	private JMenuItem paste ;
	private JMenuItem selectall ;
	private JMenuItem help ;
	private JMenuItem logfile ;
	private JMenuItem refhelp ;
	private JMenuItem about ;
	private JMenuItem find ;
	private JMenuItem replace ;
	private ColorMenu foreground ;
	private ColorMenu background ;
	private JCheckBoxMenuItem boldformat ;
	private JCheckBoxMenuItem italicformat ;
	private JCheckBoxMenuItem underlineformat ;
	private JCheckBoxMenuItem leftjustify ;
	private JCheckBoxMenuItem centerjustify ;
	private JCheckBoxMenuItem rightjustify ;
	private JCheckBoxMenuItem justify ;
	private JCheckBoxMenuItem linenum ;
	private JCheckBoxMenuItem wordwrap ;
	private JCheckBoxMenuItem richtext ;
   private Insets insets = null ;

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
	private JButton FIND = null ;
	private JComboBox FONTS = null ;
   private JComboBox FONTSIZE = null ;
	private JToggleButton BOLD = null ;
	private JToggleButton ITALIC = null ;
	private JToggleButton UNDERLINE = null ;
	private JToggleButton LEFT = null ;
	private JToggleButton RIGHT = null ;
	private JToggleButton CENTER = null ;
	private JToggleButton JUSTIFY = null ;
   private JTextField EDITMODE = null ;
   private JTextField INSERTMODE = null ;


	// Our update callback button that other components can attach
	// listeners to.

	protected CallbackButton callback = new CallbackButton(this,"TextFrame Callback") ;
   private ActionListener writelistener = null ;
   private boolean memorysource = false ;

   // Print references.

   private PageFormat pageformat = null ;				// The current page format
   private StyledPrintView sprintview = null ;		// The styled print view
   private PlainPrintView pprintview = null ;		// The plain print view


	// Create specialized listeners for events.

	ActionListener boldlistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
      	if (boldformat.getState() != BOLD.isSelected())
         {
         	boldformat.setState(BOLD.isSelected());
            return ;
         }

      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setBold(attr, BOLD.isSelected()) ;
				setAttributeSet(attr) ;
				setFocus() ;
         }
         else
         {
         	int style = Font.PLAIN ;
            if (BOLD.isSelected()) style |= Font.BOLD ;
            if (ITALIC.isSelected()) style |= Font.ITALIC ;
         	Font font = text.getFont() ;
            if (font == null) return ;
            font = font.deriveFont(style) ;
            text.setFont(font) ;
            repaint() ;
         }
		}
	} ;

	ActionListener italiclistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
      	if (italicformat.getState() != ITALIC.isSelected())
         {
         	italicformat.setState(ITALIC.isSelected());
            return ;
         }

      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setItalic(attr, ITALIC.isSelected()) ;
				setAttributeSet(attr) ;
				setFocus() ;
         }
         else
         {
         	int style = Font.PLAIN ;
            if (BOLD.isSelected()) style |= Font.BOLD ;
            if (ITALIC.isSelected()) style |= Font.ITALIC ;
         	Font font = text.getFont() ;
            if (font == null) return ;
            font = font.deriveFont(style) ;
            text.setFont(font) ;
            repaint() ;
         }
		}
	} ;

	ActionListener underlinelistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
      	if (underlineformat.getState() != UNDERLINE.isSelected())
         {
         	underlineformat.setState(UNDERLINE.isSelected());
            return ;
         }

      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setUnderline(attr, UNDERLINE.isSelected()) ;
				setAttributeSet(attr) ;
				setFocus() ;
         }
		}
	} ;

	ActionListener leftjustifylistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
         int style = StyleConstants.ALIGN_LEFT ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setAlignment(attr, style) ;
				setAttributeSet(attr,true) ;
				setFocus() ;
         }
		}
	} ;

	ActionListener centerjustifylistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
         int style = StyleConstants.ALIGN_CENTER ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setAlignment(attr, style) ;
				setAttributeSet(attr,true) ;
				setFocus() ;
         }
		}
	} ;

	ActionListener rightjustifylistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
         int style = StyleConstants.ALIGN_RIGHT ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setAlignment(attr, style) ;
				setAttributeSet(attr,true) ;
				setFocus() ;
         }
		}
	} ;

	ActionListener justifylistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
         int style = StyleConstants.ALIGN_JUSTIFIED ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setAlignment(attr, style) ;
				setAttributeSet(attr,true) ;
				setFocus() ;
         }
		}
	} ;

	ActionListener fontlistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			fontname = FONTS.getSelectedItem().toString() ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setFontFamily(attr, fontname);
				setAttributeSet(attr) ;
				setFocus() ;
         }
         else
         {
         	int style = Font.PLAIN ;
            if (BOLD.isSelected()) style |= Font.BOLD ;
            if (ITALIC.isSelected()) style |= Font.ITALIC ;
            if (fontsize == 0) fontsize = 12 ;
         	Font font = new Font(fontname,style,fontsize) ;
            text.setFont(font) ;
            repaint() ;
         }
		}
	} ;

	ActionListener fontsizelistener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			int size = 0;
			try { size = Integer.parseInt(FONTSIZE.getSelectedItem().toString()); }
			catch (NumberFormatException ex) { return ; }
			fontsize = size ;
      	if (kit != null)
         {
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setFontSize(attr, fontsize) ;
				setAttributeSet(attr) ;
				setFocus() ;
         }
         else
         {
         	int style = Font.PLAIN ;
            if (BOLD.isSelected()) style |= Font.BOLD ;
            if (ITALIC.isSelected()) style |= Font.ITALIC ;
            if (fontname == null) fontname = "Monospaced" ;
         	Font font = new Font(fontname,style,fontsize) ;
            text.setFont(font) ;
            repaint() ;
         }
		}
	} ;

	CaretListener caretlistener = new CaretListener()
	{
		public void caretUpdate(CaretEvent e)
		{
			int pos = e.getDot() ;
			if (!((pos == caret) || (pos == caret+1)))
			{
				editmode = EDIT ;
				EDITMODE.setText((editmode == EDIT) ? "Edit" : "Input") ;
			}
			if (editmode == EDIT) showAttributes(pos) ;
			caret = pos ;
		}
	} ;

	KeyListener keylistener = new KeyListener()
	{
		public void keyTyped(KeyEvent e) { }
		public void keyReleased(KeyEvent e) { }
		public void keyPressed(KeyEvent e)
      {
			if (e.getKeyCode() == KeyEvent.VK_INSERT)
			{
				insertmode = (insertmode + 1) & 0x1 ;
				INSERTMODE.setText((insertmode == INSERT) ? "INS" : "OVR") ;
			}
			else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				editmode = (editmode + 1) & 0x1 ;
				EDITMODE.setText((editmode == EDIT) ? "Edit" : "Input") ;
			}
			else if (Character.isDefined(e.getKeyChar()))
			{
				editmode = INPUT ;
				EDITMODE.setText((editmode == EDIT) ? "Edit" : "Input") ;
			}
			else if (!Character.isISOControl(e.getKeyChar()))
			{
				editmode = EDIT ;
				EDITMODE.setText((editmode == EDIT) ? "Edit" : "Input") ;
			}
		}
	} ;

	FocusListener focuslistener = new FocusListener()
	{
		public void focusGained(FocusEvent e)
		{
			if (text == null) return ;
	      Document doc = text.getDocument() ;
	      int length = (doc == null) ? 0 : doc.getLength() ;
         if (selstart > length) selstart = length ;
         if (selfinish > length) selfinish = length ;

         // Update the selection.

			if (selstart >= 0 && selfinish >= 0)
			{
				if (text.getCaretPosition() == selstart)
				{
					text.setCaretPosition(selfinish) ;
					text.moveCaretPosition(selstart) ;
				}
				else
					text.select(selstart,selfinish) ;
			}

         // Update the edit attribute.

         if (editmode == EDIT) showAttributes(selstart) ;
		}

		public void focusLost(FocusEvent e)
		{
			if (text == null) return ;
			selstart = text.getSelectionStart() ;
			selfinish = text.getSelectionEnd() ;
		}
	} ;

	MenuListener foregroundmenu = new MenuListener()
	{
		public void menuDeselected(MenuEvent e) { }
		public void menuCanceled(MenuEvent e) { }
		public void menuSelected(MenuEvent e)
		{
			if (!(doc instanceof DefaultStyledDocument)) return ;
			DefaultStyledDocument dsd = (DefaultStyledDocument) doc ;
			int p = text.getCaretPosition() ;
			AttributeSet a = dsd.getCharacterElement(p).getAttributes() ;
			Color c = StyleConstants.getForeground(a) ;
			foreground.setSelectedColor(c) ;
		}
	} ;

	MenuListener backgroundmenu = new MenuListener()
	{
		public void menuDeselected(MenuEvent e) { }
		public void menuCanceled(MenuEvent e) { }
		public void menuSelected(MenuEvent e)
		{
			if (!(doc instanceof DefaultStyledDocument)) return ;
			DefaultStyledDocument dsd = (DefaultStyledDocument) doc ;
			int p = text.getCaretPosition() ;
			AttributeSet a = dsd.getCharacterElement(p).getAttributes() ;
			Color c = StyleConstants.getBackground(a) ;
			background.setSelectedColor(c) ;
		}
	} ;



	// Constructor for zip file entries.  This constructor is used when we
	// wish to edit text documents that exist in an archive file and are
	// not memory resident as part of the currently active configuration.
   // Such elements can be external documentation files, configuration files,
   // and palette files.

	public TextFrame() { this(null) ; }

	public TextFrame(ArchiveEntry ze)
	{ this(ze,null,false) ; }

	public TextFrame(ArchiveEntry ze, ActionListener al)
	{ this(ze,null,false) ; writelistener = al ; }

	// Constructor for zip file entries that are memory resident as part of
   // the currently active configuration.  These files may have an input
   // stream and will be flagged as updatable.

	public TextFrame(ArchiveEntry ze, InputStream in, boolean updateable)
	{ this(ze,in,updateable,false) ; }
   
	public TextFrame(ArchiveEntry ze, InputStream in, boolean updateable, boolean showline)
	{ this(ze,in,updateable,showline,null) ; }

	public TextFrame(ArchiveEntry ze, InputStream in, boolean updateable, boolean showline, String type)
	{
   	super(Kisekae.getCaptions().getString("TextEditorTitle")) ;
		me = this ;
		this.in = in ;
		this.ze = ze ;
		this.zip = (ze == null) ? null : ze.getZipFile() ;
      this.showline = showline ;
		file = (ze == null) ? null : ze.getPath() ;
      int n = (file == null) ? -1 : file.lastIndexOf('.') ;
      extension = (file == null || n < 0) ? null : file.substring(n) ;
      if (".cnf".equals(extension)) this.showline = true ;
      this.type = (type != null) ? type : file ;
		directory = (zip == null) ? null : zip.getDirectoryName() ;
		memorysource = updateable ;
		originalfile = file ;
      originalze = ze ;
		init() ;
	}


	// Method to return a reference to our parent zip file object.

	ArchiveFile getZipFile() { return zip ; }

	// Method to return a reference to our zip entry object.

	ArchiveEntry getZipEntry() { return ze ; }

	// Method to return a reference to our Kiss text object.

	TextObject getTextObject() { return textobject ; }

	// Method to return a reference to our document.

	Document getDocument() { return (text == null) ? null : text.getDocument() ; }

	// Method to return a reference to our text component.

	JTextComponent getTextComponent() { return text ; }

	// Method to return our error state.

	boolean isError() { return error ; }


	// Initialize the edit frame.

	private void init()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		String s = Kisekae.getCaptions().getString("TextEditorTitle") ;
		if (file != null)
		{
			File f = new File(file) ;
			s += " - " + f.getName() ;
			if (zip != null && zip.getFileName() != null)
				s += " (" + zip.getFileName() + ")" ;
		}
		setTitle(s) ;
		setIconImage(Kisekae.getIconImage()) ;
		if (OptionsDialog.getDebugControl())
      	PrintLn.println("TextFrame active " + s) ;

		// Find the HelpSet file and create the HelpSet broker.

		if (Kisekae.isHelpInstalled())
      {
      	helper2 = new HelpLoader(this,refset,refsection) ;
      	helper = new HelpLoader(this,helpset,helpsection) ;
      }

		// Create the document.

		error = createDocument(type) ;
      if (error) return ;
		createActionTable(text) ;

		// Set up the menu bar.

		mb = new JMenuBar() ;
		fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
      if (!applemac) fileMenu.setMnemonic(KeyEvent.VK_F) ;
		s = System.getProperty("java.version") ;
		int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
		insets = new Insets(2,2,2,rm) ;
      fileMenu.setMargin(insets) ;
		fileMenu.add((newdoc = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNew")))) ;
      if (!applemac) newdoc.setMnemonic(KeyEvent.VK_N) ;
		newdoc.addActionListener(this) ;
      newdoc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelerator));
		newdoc.setEnabled(!Kisekae.isSecure()) ;
		fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
		open.addActionListener(this) ;
      if (!applemac) open.setMnemonic(KeyEvent.VK_O) ;
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelerator));
		fileMenu.add((save = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSave")))) ;
		save.addActionListener(this) ;
      if (!applemac) save.setMnemonic(KeyEvent.VK_S) ;
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelerator));
		save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((saveas = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAs")))) ;
		saveas.addActionListener(this) ;
      if (!applemac) saveas.setMnemonic(KeyEvent.VK_A) ;
		saveas.setEnabled(!Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator();
		fileMenu.add((wordwrap = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFileWordWrap")))) ;
		wordwrap.addItemListener(this) ;
      wordwrap.setSelected(!(".cnf".equals(extension)));
		fileMenu.add((linenum = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFileLineNumber")))) ;
		linenum.setState(showline) ;
		linenum.addItemListener(this) ;
      linenum.setEnabled(!ArchiveFile.isStyledText(type)) ;
		fileMenu.add((richtext = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFileStyledText")))) ;
		richtext.setState(ArchiveFile.isStyledText(type)) ;
		richtext.addItemListener(this) ;
		fileMenu.addSeparator() ;
		fileMenu.add((pagesetup = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePageSetup")))) ;
		pagesetup.addActionListener(this) ;
      if (!applemac) pagesetup.setMnemonic(KeyEvent.VK_U) ;
      pagesetup.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((printpreview = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrintPreview")))) ;
		printpreview.addActionListener(this) ;
      printpreview.setMnemonic(KeyEvent.VK_V) ;
      printpreview.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.add((print = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrint")))) ;
		print.addActionListener(this) ;
      if (!applemac) print.setMnemonic(KeyEvent.VK_P) ;
      print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelerator));
      print.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		fileMenu.addSeparator() ;
		fileMenu.add((properties = new JMenuItem(Kisekae.getCaptions().getString("MenuFileProperties")))) ;
		properties.setEnabled(false) ;
		properties.addActionListener(this) ;
		fileMenu.addSeparator();
//		fileMenu.add((update = new JMenuItem("Update"))) ;
		update = new JMenuItem("Update") ;
//		update.addActionListener(this) ;
		update.setEnabled(false) ;
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileTextQuit" : "MenuFileTextExit" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
      if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
		exit.addActionListener(this) ;
		mb.add(fileMenu) ;
      
		editMenu = createEditMenu() ;
      if (!applemac) editMenu.setMnemonic(KeyEvent.VK_E) ;
		editMenu.setMargin(insets) ;
		mb.add(editMenu) ;
		formatMenu = createFormatMenu() ;
      if (!applemac) formatMenu.setMnemonic(KeyEvent.VK_O) ;
      formatMenu.setMargin(insets) ;
		mb.add(formatMenu) ;
      
		windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
		windowMenu.setMargin(insets) ;
      if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
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
      if (OptionsDialog.getEditEnable())
      {
//   		helpMenu.add((refhelp = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpFKiss")))) ;
//       if (!applemac) refhelp.setMnemonic(KeyEvent.VK_R) ;
//       refhelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,ActionEvent.SHIFT_MASK)) ;
      }
      if (Kisekae.isHelpInstalled())
      {
         if (help != null)
         {
            if (helper != null) helper.addActionListener(help) ;
            help.setEnabled(helper != null && helper.isLoaded()) ;
         }
         if (refhelp != null)
         {
            if (helper2 != null) helper2.addActionListener(refhelp) ;
            refhelp.setEnabled(helper2 != null && helper2.isLoaded()) ;
         }
      }
      if (!Kisekae.isHelpInstalled())
      {
         help.addActionListener(this) ;
      }
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
      if (!applemac) about.setMnemonic(KeyEvent.VK_A) ;
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
		SAVE.setEnabled(!Kisekae.isSecure() && !Kisekae.isExpired());
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
		Action a = getActionByName("Cut") ;
		if (a != null) CUT.addActionListener(a) ;
      CUT.addActionListener(this);
		CUT.setToolTipText(Kisekae.getCaptions().getString("ToolTipCut"));
		COPY = new JButton() ;
		iconfile = Kisekae.getResource("Images/copy" + ext) ;
		if (iconfile != null) COPY.setIcon(new ImageIcon(iconfile)) ;
		COPY.setMargin(new Insets(1,1,1,1)) ;
		COPY.setAlignmentY(0.5f) ;
		a = getActionByName("Copy") ;
		if (a != null) COPY.addActionListener(a) ;
      COPY.addActionListener(this) ;
		COPY.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopy"));
		PASTE = new JButton() ;
		iconfile = Kisekae.getResource("Images/paste" + ext) ;
		if (iconfile != null) PASTE.setIcon(new ImageIcon(iconfile)) ;
		PASTE.setMargin(new Insets(1,1,1,1)) ;
		PASTE.setAlignmentY(0.5f) ;
		a = getActionByName("Paste") ;
		if (a != null) PASTE.addActionListener(a) ;
      PASTE.addActionListener(this) ;
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

		// Create the find button.

		FIND = new JButton() ;
		iconfile = Kisekae.getResource("Images/find" + ext) ;
		if (iconfile != null) FIND.setIcon(new ImageIcon(iconfile)) ;
		FIND.setMargin(new Insets(1,1,1,1)) ;
		FIND.setAlignmentY(0.5f) ;
		FIND.addActionListener(this) ;
		FIND.setToolTipText(Kisekae.getCaptions().getString("ToolTipFind"));
		toolbar.addSeparator() ;
		toolbar.add(FIND, null);

		// Create the font names selection box.

      String [] fontnames = { "Dialog", "Serif", "SansSerif", "Monospaced" } ;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
      try { fontnames = ge.getAvailableFontFamilyNames() ; }
      catch (Throwable e) { } 
		FONTS = (fontnames == null) ? new JComboBox() : new JComboBox(fontnames) ;
		FONTS.setMaximumSize(FONTS.getPreferredSize()) ;
		FONTS.setAlignmentY(0.5f) ;
		FONTS.setEditable(true) ;
		FONTS.addActionListener(fontlistener) ;
		toolbar.addSeparator() ;
		toolbar.add(FONTS, null);

		// Create the font size selection box.

		String [] fontsizes = new String []
			{"8", "9", "10", "11", "12", "14", "16", "18", "20",
			 "22", "24", "26", "28", "36", "48", "72"} ;
		FONTSIZE = new JComboBox(fontsizes) ;
		FONTSIZE.setMaximumSize(FONTSIZE.getPreferredSize()) ;
		FONTSIZE.setAlignmentY(0.5f) ;
		FONTSIZE.setEditable(true) ;
		FONTSIZE.addActionListener(fontsizelistener) ;
		toolbar.addSeparator() ;
		toolbar.add(FONTSIZE, null) ;

		// Create the style toggle buttons.

		BOLD = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/bold" + ext) ;
		if (iconfile != null) BOLD.setIcon(new ImageIcon(iconfile)) ;
		BOLD.setMargin(new Insets(1,1,1,1)) ;
		BOLD.setAlignmentY(0.5f) ;
		BOLD.addActionListener(boldlistener) ;
		BOLD.setToolTipText(Kisekae.getCaptions().getString("ToolTipBold"));
		BOLD.setEnabled(ArchiveFile.isStyledText(type)) ;
		ITALIC = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/italic" + ext) ;
		if (iconfile != null) ITALIC.setIcon(new ImageIcon(iconfile)) ;
		ITALIC.setMargin(new Insets(1,1,1,1)) ;
		ITALIC.setAlignmentY(0.5f) ;
		ITALIC.addActionListener(italiclistener) ;
		ITALIC.setToolTipText(Kisekae.getCaptions().getString("ToolTipItalic"));
		ITALIC.setEnabled(ArchiveFile.isStyledText(type)) ;
		UNDERLINE = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/underline" + ext) ;
		if (iconfile != null) UNDERLINE.setIcon(new ImageIcon(iconfile)) ;
		UNDERLINE.setMargin(new Insets(1,1,1,1)) ;
		UNDERLINE.setAlignmentY(0.5f) ;
		UNDERLINE.addActionListener(underlinelistener) ;
		UNDERLINE.setToolTipText(Kisekae.getCaptions().getString("ToolTipUnderline"));
		UNDERLINE.setEnabled(ArchiveFile.isStyledText(type)) ;
		toolbar.addSeparator() ;
		toolbar.add(BOLD, null) ;
		toolbar.add(ITALIC, null) ;
		toolbar.add(UNDERLINE, null) ;

		// Create the text justification buttons.

		LEFT = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/leftjust" + ext) ;
		if (iconfile != null) LEFT.setIcon(new ImageIcon(iconfile)) ;
		LEFT.setMargin(new Insets(1,1,1,1)) ;
		LEFT.setAlignmentY(0.5f) ;
		LEFT.addActionListener(leftjustifylistener) ;
		LEFT.setToolTipText(Kisekae.getCaptions().getString("ToolTipLeft"));
		LEFT.setEnabled(ArchiveFile.isStyledText(type)) ;
		CENTER = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/centerjust" + ext) ;
		if (iconfile != null) CENTER.setIcon(new ImageIcon(iconfile)) ;
		CENTER.setMargin(new Insets(1,1,1,1)) ;
		CENTER.setAlignmentY(0.5f) ;
		CENTER.addActionListener(centerjustifylistener) ;
		CENTER.setToolTipText(Kisekae.getCaptions().getString("ToolTipCenter"));
		CENTER.setEnabled(ArchiveFile.isStyledText(type)) ;
		RIGHT = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/rightjust" + ext) ;
		if (iconfile != null) RIGHT.setIcon(new ImageIcon(iconfile)) ;
		RIGHT.setMargin(new Insets(1,1,1,1)) ;
		RIGHT.setAlignmentY(0.5f) ;
		RIGHT.addActionListener(rightjustifylistener) ;
		RIGHT.setToolTipText(Kisekae.getCaptions().getString("ToolTipRight"));
		RIGHT.setEnabled(ArchiveFile.isStyledText(type)) ;
		JUSTIFY = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/justify" + ext) ;
		if (iconfile != null) JUSTIFY.setIcon(new ImageIcon(iconfile)) ;
		JUSTIFY.setMargin(new Insets(1,1,1,1)) ;
		JUSTIFY.setAlignmentY(0.5f) ;
		JUSTIFY.addActionListener(justifylistener) ;
		JUSTIFY.setToolTipText(Kisekae.getCaptions().getString("ToolTipJustify"));
		JUSTIFY.setEnabled(ArchiveFile.isStyledText(type)) ;
      ButtonGroup bg = new ButtonGroup() ;
      bg.add(LEFT);
      bg.add(CENTER);
      bg.add(RIGHT);
      bg.add(JUSTIFY);
		toolbar.addSeparator() ;
		toolbar.add(LEFT, null) ;
		toolbar.add(CENTER, null) ;
		toolbar.add(RIGHT, null) ;
		toolbar.add(JUSTIFY, null) ;

		// Create the edit mode indicators.

		Dimension d = new Dimension(40,21) ;
		EDITMODE = new JTextField((editmode == EDIT) ? "Edit" : "Input") ;
		EDITMODE.setPreferredSize(d) ;
		EDITMODE.setMaximumSize(d) ;
		EDITMODE.setHorizontalAlignment(SwingConstants.CENTER);
		EDITMODE.setEditable(false);
		INSERTMODE = new JTextField((insertmode == INSERT) ? "INS" : "OVR") ;
		INSERTMODE.setPreferredSize(d) ;
		INSERTMODE.setMaximumSize(d) ;
		INSERTMODE.setHorizontalAlignment(SwingConstants.CENTER);
		INSERTMODE.setEditable(false);
		toolbar.addSeparator() ;
		toolbar.add(Box.createGlue()) ;
		toolbar.add(EDITMODE, null) ;
		toolbar.add(INSERTMODE, null) ;

		// Create the default page format.

      if (Kisekae.isPrintInstalled())
      {
			PrinterJob prn = null ;
			try { prn = PrinterJob.getPrinterJob() ; }
			catch (Exception e) { }
			pageformat = (prn != null) ? prn.defaultPage() : null ;
      }

		// Create the window frame.

		Container c = getContentPane() ;
		d = Toolkit.getDefaultToolkit().getScreenSize() ;
		d = new Dimension((int) (d.width*1.00),(int) (d.height*0.95)) ;
		scrollpanel = new TextPanel(text) ;
		scrollpane = new JScrollPane(scrollpanel) ;
		scrollpane.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
      scrollpane.getViewport().setBackground(text.getBackground());
      int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
      vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
      int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
      scrollpane.setVerticalScrollBarPolicy(vspolicy) ;
      scrollpane.setHorizontalScrollBarPolicy(hspolicy) ;			
		c.add(toolbar,BorderLayout.NORTH) ;
		c.add(scrollpane,BorderLayout.CENTER) ;

      // The following is a kludge to fix a scrolling repaint problem.
      // Line numbers are not always drawn properly on scrolls.

      JScrollBar vsb = scrollpane.getVerticalScrollBar() ;
		MouseListener ml = new MouseAdapter()
	   { public void mouseReleased(MouseEvent e) { scrollpane.repaint(); } } ;
		vsb.addMouseListener(ml);

		// Layout the frame.

      super.open() ;
		doLayout() ;
      if (helper != null) helper.setSize(getSize());
      if (helper2 != null) helper2.setSize(getSize());

      // Enable the Input Method Framework for Asian text encodings.
      // This framework allows multikey character composition.
      // On Windows systems it is invoked with Alt-`

      Object encoding = OptionsDialog.getEncoding() ;
      s = (encoding != null) ? encoding.toString() : "" ;
      Locale locale = Kisekae.getCurrentLocale() ;
      if ("SJIS".equals(s) || "MS932".equals(s) || "EUC_JP".equals(s) ||
          (locale != null && "ja".equals(locale.getLanguage())))
         enableInputMethods(true) ;

		// Set up the window event listeners.

      scrollpanel.setLineWrap(wordwrap.getState()) ;
		addWindowListener(this) ;
		showAttributes(0) ;
      changed = false ;
	}


	// Method to create a new document and text object.  This is where we
	// set up the text edit panel.  We use plain text for simple text files
	// and rich text for styled documents.

	private boolean createDocument(String type)
	{
		boolean error = false ;

		// Initialize for a new document.

		fontname = "" ;							// The name of the font
		fontsize = 0 ; 							// The size of the font
		selstart = -1 ; 							// The selection start
		selfinish = -1 ;              		// The selection finish
		caret = 0 ;									// The caret position
		editmode = INPUT ;		  				// The editor mode
		insertmode = INSERT ;	  				// The text insert mode

		// Create the text object.  This is either a rich text object
		// or a plain text object, depending on the file name.

		try
		{
			if (ArchiveFile.isRichText(type))
			{
				text = new JTextPane() ;
				kit = new RTFEditorKit() ;
				context = new StyleContext() ;
				doc = new DefaultStyledDocument(context) ;
				((JTextPane) text).setEditorKit(kit) ;
				((JTextPane) text).setDocument(doc);
			}
			else if (ArchiveFile.isHTMLText(type))
			{
				text = new JTextPane() ;
				kit = new HTMLEditorKit() ;
				context = new StyleContext() ;
				doc = new HTMLDocument() ;
				((JTextPane) text).setEditorKit(kit) ;
				((JTextPane) text).setDocument(doc);
			}
			else
			{
         	kit = null ;
            doc = null ;
            context = null ;
				text = new JTextArea() 
            {
               public String getToolTipText(MouseEvent e)
               {
                  // If errors are set, tooltips show the error message.
               
                  if (errormsgs != null) 
                  {
                     try 
                     { 
                        Point pos = e.getPoint() ;
                        int start = getLineOfOffset(viewToModel(pos)) ; 
                        return (String) errormsgs.get(new Integer(start+1)) ;
                     }
                     catch (BadLocationException ble) { return null ; }
                  }
                  
                  // If we are showing FKiSS signature help then identify the
                  // event or action name within the FKiSS statement.
               
                  if (fkisshelp)
                  {
                     try 
                     { 
                        Point pos = e.getPoint() ;
                        int index = viewToModel(pos) ;
                        int start = Utilities.getPreviousWord(text,index) ;
                        int end = Utilities.getWordEnd(text,index) ;
                        int line = getLineOfOffset(index) ; 
                        int linestart = getLineStartOffset(line) ;
                        int lineend = getLineEndOffset(line) ;
                        String s = text.getText(linestart, lineend-linestart) ;
                        int i = s.indexOf(";@") ;
                        if (i < 0) i = start ;
                        int j = s.indexOf(";",i+1) ;
                        if (j < 0) j = end ;
                        if ((start-linestart) > i && (end-linestart) <= j)
                        {
                           String word = getText(start,end-start) ; 
                           return EventHandler.findSignature(word) ; 
                        }
                     }
                     catch (BadLocationException ble) { return null ; }
                  }
                  return null ;
              }
            } ;
            ToolTipManager.sharedInstance().registerComponent(text) ;
				((JTextArea) text).setLineWrap(false) ;
				((JTextArea) text).setWrapStyleWord(true) ;
				((JTextArea) text).setTabSize(3) ;
			}

			// Set up to read the file contents.   Watch for errors.

			int bytes = (ze == null) ? 0 : (int) ze.getSize() ;
			if (OptionsDialog.getDebugLoad())
				PrintLn.println("TextFrame: read " + file + " [" + bytes + " bytes]") ;
			if (in == null && ze != null)
				in = (zip == null) ? null : zip.getInputStream(ze) ;

			// Read the file contents.

			if (in != null)
			{
				if (kit == null)
            {
               InputStreamReader isr = null ;
               String encoding = Kisekae.getLanguageEncoding() ;
               if (encoding != null)
                  isr = new InputStreamReader(in,encoding) ;
               else
                  isr = new InputStreamReader(in) ;
					text.read(isr,file) ;
            }
				else if (kit instanceof RTFEditorKit)
				{
					doc = new DefaultStyledDocument(context) ;
					try { kit.read(in,doc,0); }
					catch (BadLocationException e)
					{ throw new IOException(e.getMessage()) ; }
					text.setDocument(doc);
				}
				else if (kit instanceof HTMLEditorKit)
				{
					doc = new HTMLDocument() ;
               doc.putProperty("IgnoreCharsetDirective", new Boolean(true));
               try { kit.read(in,doc,0); }
					catch (BadLocationException e)
					{ throw new IOException(e.getMessage()) ; }
					text.setDocument(doc);
				}
			}
         else
   			if (OptionsDialog.getDebugLoad())
   				PrintLn.println("TextFrame: unable to obtain input stream for " + file) ;

			// Wrap our text object in a KiSS TextObject.

			text.setBorder(new LineNumberBorder(Color.gray)) ;
			text.setCaretPosition(caret) ;
         text.setEnabled(!Kisekae.isSecure());
         text.setDisabledTextColor(Color.BLACK) ;
         textobject = new TextObject(ze,text,kit) ;
			FileOpen fd = (ze == null) ? null : ze.getFileOpen() ;
			if (fd != null) fd.close() ;

			// Set up the document event listeners.

			Document document = text.getDocument() ;
			document.addUndoableEditListener(this) ;
			document.addDocumentListener(this) ;
			text.addCaretListener(caretlistener) ;
			text.addFocusListener(focuslistener) ;
			text.addKeyListener(keylistener) ;
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			error = true ;
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("TextFrame: Out of memory.") ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") + "\n" +
            Kisekae.getCaptions().getString("FileReadError"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for security faults.

      catch (SecurityException e)
      {
			error = true ;
			PrintLn.println("TextFrame: Security exception. " + e.toString()) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

		// Watch for I/O errors.

		catch (IOException e)
		{
			error = true ;
			PrintLn.println("TextFrame: Initialization fault.") ;
			e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("FileOpenException") + "\n" +
            Kisekae.getCaptions().getString("FileReadError") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Close the file on termination.

		finally
		{
      	try { if (in != null) in.close() ; }
			catch (IOException e) { error = true ; }
		}
		return error ;
	}


	// A utility method to create the edit menu.

	private JMenu createEditMenu()
	{
      boolean applemac = OptionsDialog.getAppleMac() ;
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuEdit")) ;

		// Undo and redo are actions of our own creation.

		undoAction = new UndoAction() ;
		JMenuItem undo = menu.add(undoAction) ;
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelerator));
      if (!applemac) undo.setMnemonic(KeyEvent.VK_U) ;
		redoAction = new RedoAction() ;
		JMenuItem redo = menu.add(redoAction) ;
      redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, accelerator));
      if (!applemac) redo.setMnemonic(KeyEvent.VK_R) ;
      menu.add((undoall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUndoAll")))) ;
      undoall.addActionListener(this) ;
      undoall.setEnabled(false) ;
		menu.addSeparator() ;

		// These actions come from the default editor kit, but we rename
		// them.  Get the ones we want and add them to the menu.

		menu.add((cut = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCut")))) ;
		Action a = getActionByName("Cut") ;
		if (a != null) cut.addActionListener(a) ;
      cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelerator));
      if (!applemac) cut.setMnemonic(KeyEvent.VK_T) ;
      cut.addActionListener(this);
		cut.setEnabled(!Kisekae.isSecure()) ;
		menu.add((copy = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCopy")))) ;
		a = getActionByName("Copy") ;
		if (a != null) copy.addActionListener(a) ;
      copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelerator));
      if (!applemac) copy.setMnemonic(KeyEvent.VK_C) ;
      copy.addActionListener(this);
		copy.setEnabled(!Kisekae.isSecure()) ;
		menu.add((paste = new JMenuItem(Kisekae.getCaptions().getString("MenuEditPaste")))) ;
		a = getActionByName("Paste") ;
		if (a != null) paste.addActionListener(a) ;
      paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, accelerator));
      if (!applemac) paste.setMnemonic(KeyEvent.VK_P) ;
      paste.addActionListener(this);
		paste.setEnabled(!Kisekae.isSecure()) ;
		menu.addSeparator() ;
		menu.add((find = new JMenuItem(Kisekae.getCaptions().getString("MenuEditFind")))) ;
		find.addActionListener(this) ;
      if (!applemac) find.setMnemonic(KeyEvent.VK_F) ;
      find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, accelerator));
		find.setEnabled(!Kisekae.isSecure()) ;
		menu.add((replace = new JMenuItem(Kisekae.getCaptions().getString("MenuEditReplace")))) ;
		replace.addActionListener(this) ;
      if (!applemac) replace.setMnemonic(KeyEvent.VK_E) ;
      replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, accelerator));
		replace.setEnabled(!Kisekae.isSecure()) ;
		menu.addSeparator() ;
		menu.add((selectall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectAll")))) ;
		a = getActionByName("Select All") ;
      selectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator));
		selectall.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) selectall.setMnemonic(KeyEvent.VK_A) ;
		if (a != null) selectall.addActionListener(a) ;
		return menu;
	}


	// A utility method to create the format menu.

	private JMenu createFormatMenu()
	{
		JMenu menu = new JMenu(Kisekae.getCaptions().getString("MenuFormat")) ;
		menu.add((boldformat = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatBold")))) ;
		boldformat.addItemListener(this) ;
		boldformat.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((italicformat = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatItalic")))) ;
		italicformat.addItemListener(this) ;
		italicformat.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((underlineformat = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatUnderline")))) ;
		underlineformat.addItemListener(this) ;
		underlineformat.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.addSeparator() ;
		menu.add((leftjustify = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatLeftJustify")))) ;
//		leftjustify.addItemListener(this) ;
		leftjustify.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((centerjustify = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatCenter")))) ;
//		centerjustify.addItemListener(this) ;
		centerjustify.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((rightjustify = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatRightJustify")))) ;
//		rightjustify.addItemListener(this) ;
		rightjustify.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((justify = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuFormatJustify")))) ;
//		justify.addItemListener(this) ;
		justify.setEnabled(ArchiveFile.isStyledText(type)) ;
      ButtonGroup bg = new ButtonGroup() ;
      bg.add(leftjustify);
      bg.add(centerjustify);
      bg.add(rightjustify);
      bg.add(justify);
		menu.addSeparator() ;
		menu.add((foreground = new ColorMenu(Kisekae.getCaptions().getString("MenuFormatForeground")))) ;
		foreground.setSelectedColor(text.getForeground()) ;
		foreground.addMenuListener(foregroundmenu) ;
		foreground.addActionListener(this) ;
		foreground.setEnabled(ArchiveFile.isStyledText(type)) ;
		menu.add((background = new ColorMenu(Kisekae.getCaptions().getString("MenuFormatBackground")))) ;
		background.setSelectedColor(text.getBackground()) ;
		background.addMenuListener(backgroundmenu) ;
		background.addActionListener(this) ;
		background.setEnabled(ArchiveFile.isStyledText(type)) ;
		return menu ;
	}


	// Method to update the user interface for a new text document.

	private void updateInterface(TextObject text)
	{
		Container c = getContentPane() ;
		Dimension d = getSize() ;
		c.remove(scrollpane) ;
		JTextComponent textcomponent = text.getTextComponent() ;
		textcomponent.setBorder(new LineNumberBorder(Color.gray)) ;
		scrollpanel = new TextPanel(textcomponent) ;
		scrollpane = new JScrollPane(scrollpanel);
		scrollpane.getViewport().putClientProperty("EnableWindowBlit",Boolean.TRUE);
      scrollpane.getViewport().setBackground(textcomponent.getBackground());
      int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
      vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
      int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
      scrollpane.setVerticalScrollBarPolicy(vspolicy) ;
      scrollpane.setHorizontalScrollBarPolicy(hspolicy) ;			
		c.add(scrollpane,BorderLayout.CENTER) ;
		Insets insets = this.getInsets() ;
		d.width -= (insets.left + insets.right) ;
		d.height -= (insets.top + insets.bottom) ;
		insets = scrollpane.getInsets() ;
		d.width -= (insets.left + insets.right) ;
		d.height -= (insets.top + insets.bottom) ;
		scrollpanel.setSize(d) ;

		// Set the menu interface.

		foreground.setEnabled(text.isStyledText()) ;
		background.setEnabled(text.isStyledText()) ;
		boldformat.setEnabled(text.isStyledText()) ;
		italicformat.setEnabled(text.isStyledText()) ;
		underlineformat.setEnabled(text.isStyledText()) ;
		leftjustify.setEnabled(text.isStyledText()) ;
		centerjustify.setEnabled(text.isStyledText()) ;
		rightjustify.setEnabled(text.isStyledText()) ;
		justify.setEnabled(text.isStyledText()) ;
		linenum.setEnabled(!text.isStyledText()) ;
		BOLD.setEnabled(text.isStyledText()) ;
		ITALIC.setEnabled(text.isStyledText()) ;
		UNDERLINE.setEnabled(text.isStyledText()) ;
		LEFT.setEnabled(text.isStyledText()) ;
		CENTER.setEnabled(text.isStyledText()) ;
		RIGHT.setEnabled(text.isStyledText()) ;
		JUSTIFY.setEnabled(text.isStyledText()) ;
      richtext.setState(text.isStyledText()) ;

      // Show the text component attributes.
		// Assign input focus to the text component.

      showAttributes(0) ;
      scrollpanel.setLineWrap(wordwrap.getState()) ;
		validate() ;
		textcomponent.requestFocus() ;
	}


	// The following two methods allow us to find an action provided by
	// the editor kit by its name.

	private void createActionTable(JTextComponent textComponent)
	{
		actions = new Hashtable() ;
		if (error || textComponent == null) return ;
		Action[] actionsArray = textComponent.getActions() ;
		if (actionsArray == null) return ;

		// Get the required editor actions and store in our hashtable.

		for (int i = 0; i < actionsArray.length; i++)
		{
			Action a = actionsArray[i] ;
			String s = (String) a.getValue(Action.NAME) ;
			if (s.equals(DefaultEditorKit.cutAction))
				a.putValue(Action.NAME,"Cut") ;
			if (s.equals(DefaultEditorKit.copyAction))
				a.putValue(Action.NAME,"Copy") ;
			if (s.equals(DefaultEditorKit.pasteAction))
				a.putValue(Action.NAME,"Paste") ;
			if (s.equals(DefaultEditorKit.selectAllAction))
				a.putValue(Action.NAME,"Select All") ;
			actions.put(a.getValue(Action.NAME),a) ;
		}
	}

	private Action getActionByName(String name)
	{
		if (actions == null) return null ;
		return (Action) actions.get(name) ;
	}


	// The showAttributes method sets the toolbar state according to
   // the font properties at the given caret position.

	private void showAttributes(int p)
	{
		// If we have a plain document simply set the font attributes to
		// match the text object font.

		if (text instanceof JTextArea)
		{
			Font font = text.getFont() ;
			if (font == null) return ;
			fontname = font.getName() ;
			FONTS.setSelectedItem(fontname) ;
			fontsize = font.getSize() ;
			FONTSIZE.setSelectedItem(Integer.toString(fontsize));
			return ;
		}

		// If we have a styled document then we must get the document
		// attributes at the current position.

      AttributeSet a = null ;
  		RTFEditorKit rtfkit = null ;
  		HTMLEditorKit htmlkit = null ;
		if (kit instanceof StyledEditorKit)
      {
   		if (!(doc instanceof DefaultStyledDocument)) return ;
   		if (kit instanceof RTFEditorKit) rtfkit = (RTFEditorKit) kit ;
   		if (kit instanceof HTMLEditorKit) htmlkit = (HTMLEditorKit) kit ;
   		DefaultStyledDocument dfd = (DefaultStyledDocument) doc ;
   		Element elem = dfd.getCharacterElement(p) ;
   		a = (elem == null) ? null : elem.getAttributes();
      }

		// Display the current font.  This triggers the fontlistener which
		// sets selection attributes, but this is ignored as we skip updates.

  		if (a == null) return ;
		skipupdate = true ;
		String name = StyleConstants.getFontFamily(a);
		if (!fontname.equals(name))
		{
			fontname = name;
			FONTS.setSelectedItem(name);
		}

		// Display the current font size.  This triggers the fontlistener which
		// sets selection attributes, but this is ignored as we skip updates.

		int size = StyleConstants.getFontSize(a);
		if (fontsize != size)
		{
			fontsize = size;
			FONTSIZE.setSelectedItem(Integer.toString(fontsize));
		}

		// Display the current bold, italic, and underline state.

		boolean bold = StyleConstants.isBold(a);
		if (bold != BOLD.isSelected()) BOLD.setSelected(bold);
		if (bold != boldformat.isSelected()) boldformat.setSelected(bold);
		boolean italic = StyleConstants.isItalic(a);
		if (italic != ITALIC.isSelected()) ITALIC.setSelected(italic);
		if (italic != italicformat.isSelected()) italicformat.setSelected(italic);
		boolean underline = StyleConstants.isUnderline(a);
		if (underline != UNDERLINE.isSelected()) UNDERLINE.setSelected(underline);
		if (underline != underlineformat.isSelected()) underlineformat.setSelected(underline);

		// Display the text justification state.

		int style = StyleConstants.getAlignment(a);
		if (style == StyleConstants.ALIGN_LEFT)
         { LEFT.setSelected(true) ; leftjustify.setSelected(true) ; }
		if (style == StyleConstants.ALIGN_CENTER)
         { CENTER.setSelected(true) ; centerjustify.setSelected(true) ; }
		if (style == StyleConstants.ALIGN_RIGHT)
         { RIGHT.setSelected(true) ; rightjustify.setSelected(true) ; }
		if (style == StyleConstants.ALIGN_JUSTIFIED)
         { JUSTIFY.setSelected(true) ; justify.setSelected(true) ; }

		// Set the input attributes to match the current attributes.

		MutableAttributeSet inputAttributes = null ;
      if (rtfkit != null) inputAttributes = rtfkit.getInputAttributes();
      if (htmlkit != null) inputAttributes = htmlkit.getInputAttributes();
		if (inputAttributes != null) inputAttributes.addAttributes(a);
		skipupdate = false ;
	}


   // The setAttributeSet method assigns a given set of attributes to the
	// currently selected text.  We skip the update if we are currently
   // updating the toolbar state.

	private void setAttributeSet(AttributeSet attr)
   { setAttributeSet(attr,false) ; }

	private void setAttributeSet(AttributeSet attr, boolean paragraph)
	{
		if (kit instanceof RTFEditorKit)
      {
   		if (!(doc instanceof DefaultStyledDocument)) return ;
   		if (skipupdate) return ;

   		// Get the document attributes and selection bounds.

   		DefaultStyledDocument styleddoc = (DefaultStyledDocument) doc ;
   		RTFEditorKit rtfkit = (RTFEditorKit) kit ;
   		int start = text.getSelectionStart() ;
   		int finish = text.getSelectionEnd() ;

   		// If the text frame is not in focus used saved selection bounds.

   		if (!text.hasFocus())
   		{
   			start = selstart;
   			finish = selfinish;
   		}

   		// If something is selected apply the specified attributes, otherwise
   		// set the attributes for future input.

         if (paragraph)
         {
   			styleddoc.setParagraphAttributes(start,(finish-start),attr,false);
         }
   		else if (start != finish)
   		{
   			styleddoc.setCharacterAttributes(start,(finish-start),attr,false);
   		}
   		else
   		{
   			MutableAttributeSet inputAttributes = rtfkit.getInputAttributes();
   			inputAttributes.addAttributes(attr);
   		}
      }

		if (kit instanceof HTMLEditorKit)
      {
   		if (!(doc instanceof HTMLDocument)) return ;
   		if (skipupdate) return ;

   		// Get the document attributes and selection bounds.

   		HTMLDocument htmldoc = (HTMLDocument) doc ;
   		HTMLEditorKit htmlkit = (HTMLEditorKit) kit ;
   		int start = text.getSelectionStart() ;
   		int finish = text.getSelectionEnd() ;

   		// If the text frame is not in focus used saved selection bounds.

   		if (!text.hasFocus())
   		{
   			start = selstart;
   			finish = selfinish;
   		}

   		// If something is selected apply the specified attributes, otherwise
   		// set the attributes for future input.

         if (paragraph)
         {
   			htmldoc.setParagraphAttributes(start,(finish-start),attr,false);
         }
   		else if (start != finish)
   		{
   			htmldoc.setCharacterAttributes(start,(finish-start),attr,false);
   		}
   		else
   		{
   			MutableAttributeSet inputAttributes = htmlkit.getInputAttributes();
   			inputAttributes.addAttributes(attr);
   		}
      }
	}


	// A utility method to set the initial line number state.  This method can
   // be called prior to the frame being set visible.  If line numbers are
   // shown then wordwrap is off by default.

	void showLineNumbers(boolean b)
	{
   	if (error) return ;
      if (!linenum.isEnabled()) return ;
		linenum.setState(b) ;
      wordwrap.setState(!b) ;
      if (scrollpanel != null) 
      {
         scrollpanel.setLineWrap(wordwrap.getState()) ;
         scrollpanel.revalidate();
      }
		showline = b ;
	}


	// A utility method to set an error message line number. These line numbers 
   // are displayed in red when line numbers are visible. The method parameter
   // is an error message string of the form "[Line nnn] ...". The error text
   // is displayed as a ToolTip when the mouse hovers over the line.  This
   // method returns the number of error messages having the correct form.

	int setErrorLine(String s)
	{
      if (s == null) return 0 ;
      int i = s.indexOf('[') ;
      if (i < 0) return 0 ;
      int j = s.indexOf(']') ;
      if (j <= i+6) return 0 ;
      String s1 = s.substring(i+6,j) ;
      try { i = Integer.parseInt(s1) ; }
      catch (NumberFormatException e) { return 0 ; }
      if (errormsgs == null) errormsgs = new Hashtable() ;
      Integer key = new Integer(i) ;
      if (!errormsgs.containsKey(key)) errormsgs.put(key,s) ;
      return setErrorLine(i) ;
	}

	int setErrorLine(int n)
	{
      if (n < 0) errormsgs = null ;
      if (text == null) return 0 ;
      Border b = text.getBorder() ;
      if (!(b instanceof LineNumberBorder)) return 0 ;
      ((LineNumberBorder) b).setErrorLine(n) ;
      return 1 ;
   }

	int setErrorLine(Vector v)
	{
      int errors = 0 ;
      if (v == null) return 0 ;
      for (int i = 0 ; i < v.size() ; i++)
         errors += setErrorLine((String) v.elementAt(i)) ;
      return errors ;
   }


	// A utility method to enable FKiSS tooltip signature help. If true then 
   // command signatures are displayed as a ToolTip when the mouse hovers over 
   // the action or event name. 

	void setFKissHelp(boolean b)
	{
      fkisshelp = b ;
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


	// A utility method to adjust the text selection point in our document.

	void setSelection(Point location, boolean up)
	{
		if (location == null) return ;
		setSelection(location.x,location.y,up) ;
	}

	void setSelection(int start, int end, boolean up)
	{
   	if (start < 0 || end < 0) return ;
		selstart = start ;
		selfinish = end ;
		if (up)
		{
      	text.setCaretPosition(end) ;
         text.moveCaretPosition(start) ;
      }
      else
      	text.select(start,end) ;
   }


	// Undoable edit listener implementation.

	public void undoableEditHappened(UndoableEditEvent e)
	{
		undo.addEdit(e.getEdit()) ;
		undoAction.updateUndoState() ;
		redoAction.updateRedoState() ;
      undoall.setEnabled(undo.canUndo()) ;
      textobject.setUpdated(true) ;
      setChanged() ;
	}

	// Document change listener interface.

	public void insertUpdate(DocumentEvent e)
   {
      Document doc = getDocument() ;
      if (doc == null) return ;
      if (e.getChange(doc.getDefaultRootElement()) != null) setChanged() ;
   }
   
	public void changedUpdate(DocumentEvent e)
   {
      Document doc = getDocument() ;
      if (doc == null) return ;
      if (e.getChange(doc.getDefaultRootElement()) != null) setChanged() ;
   }
   
	public void removeUpdate(DocumentEvent e)
   {
      Document doc = getDocument() ;
      if (doc == null) return ;
      if (e.getChange(doc.getDefaultRootElement()) != null) setChanged() ;
   }


	// Function to indicate that the document has been changed.

	void setChanged()
   {
   	save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
   	SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
      update.setEnabled(memorysource) ;
      if (textobject != null)
	      textobject.setLastModified(System.currentTimeMillis()) ;
      changed = true ;
	}

	// Function to set the text component focus.

	void setFocus()
	{
		if (text == null) return ;
		text.requestFocus() ;
	}
   
   void setEditable(boolean b)
   {
      if (text != null) text.setEditable(b) ;
   }


	// Action event listener.  We only process menu item actions.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
			// Exit request.  Save the document first if necessary.

			if (source == exit || source == CLOSE)
			{
				if (!closecheck(true)) return ;
				close() ;
				return ;
			}

			// New document request.  Save the document first if necessary.

			if (source == newdoc || source == NEW)
			{
				if (!closecheck(true)) return ;
				in = null ;
				ze = null ;
				zip = null ;
				fileopen = null ;
				file = null ;
				directory = null ;
				type = ".txt" ;
				if (richtext.isSelected()) type = ".rtf" ;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				error = createDocument(type) ;
				createActionTable(text) ;
				if (!error) updateInterface(textobject) ;

				// Initialize state variables.

				setTitle(Kisekae.getCaptions().getString("TextEditorTitle")) ;
				save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				changed = false ;
				return ;
			}

			// Open file request.

			if (source == open || source == OPEN)
			{
				String [] ext = ArchiveFile.getTextExt() ;
				FileOpen fdnew = new FileOpen(this,
               Kisekae.getCaptions().getString("TextListTitle"), ext) ;
            fdnew.setFileFilter("textfiles") ;
				fdnew.show() ;
				ArchiveEntry zenew = fdnew.getZipEntry() ;
				if (zenew == null) { fdnew.close() ; return ; }
				if (!closecheck(true)) return ;

				// Open the file.

				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				fileopen = fdnew ;
				zip = fileopen.getZipFile() ;
				ze = zenew ;
				file = (ze == null) ? null : ze.getPath() ;
            type = file ;
				directory = (zip == null) ? null : zip.getDirectoryName() ;
				try { in = ze.getInputStream() ; }
				catch (IOException ex) { in = null ; }
				error = createDocument(type) ;
				createActionTable(text) ;
				if (!error) updateInterface(textobject) ;

				// Update our frame title to reflect the new text file name.
				// Format is "Text Editor - filename (directory)"

				String s = Kisekae.getCaptions().getString("TextEditorTitle") ;
				if (file != null && file.length() > 0)
				{
					File f = new File(file) ;
					s += " - " + f.getName() ;
					if (directory != null) s += " (" + directory + ")" ;
				}
				setTitle(s) ;

				// Initialize state variables.

				changed = false ;
            memorysource = false ;
				save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				return ;
			}

			// Save or Save As request.  Save the document.

			if (source == save || source == saveas || source == SAVE)
			{
				savetext((source == saveas)) ;
				return ;
			}

         // Edit requests needs to re-establish focus. (Java 1.4)

         if (source == cut || source == CUT) { selfinish = selstart ; setFocus() ; }
         if (source == copy || source == COPY) setFocus() ;
         if (source == paste || source == PASTE) setFocus() ;
         
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

			if ("FileWriter Callback".equals(evt.getActionCommand()))
			{
				String newname = (ze != null) ? ze.getPath()
					: ((textobject != null) ? textobject.getPath() : null) ;

				// If we converted to or from styled text then our user interface
				// must be updated.  If we saved the text element under a new name
				// and we were originally a memorysource element, then we are no
				// longer a memorysource and the facility to update the configuration
				// must be disabled.

				if (newname != null && !newname.equalsIgnoreCase(file))
				{
               memorysource = false ;
               update.setEnabled(false) ;
					if (textobject != null)
					{
						JTextComponent newtext = textobject.getTextComponent() ;
						EditorKit newkit = textobject.getEditorKit() ;

						// If out text object changed, adjust the interface.

						if (newtext != text)
						{
							kit = newkit ;
							text = newtext ;
							doc = text.getDocument() ;
							doc.addUndoableEditListener(this) ;
							doc.addDocumentListener(this) ;
							text.addCaretListener(caretlistener) ;
							text.addFocusListener(focuslistener) ;
							text.addKeyListener(keylistener) ;
							updateInterface(textobject) ;
						}
					}

					// Update our fileopen object to reference the new element.

					fileopen = (ze == null)
						? new FileOpen(this,newname,"r") : ze.getFileOpen() ;
					if (fileopen != null)
					{
						fileopen.open() ;
						zip = fileopen.getZipFile() ;
						ze = (zip != null) ? zip.getEntry(newname) : null ;
						textobject = new TextObject(ze,text,kit) ;
						fileopen.close() ;
					}
				}
            
				// Update our frame title to reflect the new text file name.
				// Format is "Text Editor - filename (directory)"
            
				String s = Kisekae.getCaptions().getString("TextEditorTitle") ;
				if (newname != null && newname.length() > 0)
				{
					File f = new File(newname) ;
					s += " - " + f.getName() ;
					directory = (zip == null) ? f.getParent() : zip.getFileName() ;
					if (directory != null) s += " (" + directory + ")" ;
				}
				setTitle(s) ;

		      // If we have saved an active configuration element,
		      // update the memory copy.  This ensures that the changes
            // are not lost if the data set is saved.  We also terminate.

		      if (memorysource)
		      {
					callback.doClick() ;
					update.setEnabled(false) ;
               close() ;
		      }

				// Retain our new file name.

				file = newname ;
				save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
				changed = false ;
				return ;
			}

			// Update request.  Applies the document to the current configuration.

			if (source == update)
			{
				if (changed) callback.doClick() ;
				update.setEnabled(false) ;
				undo.discardAllEdits();
				undoAction.updateUndoState() ;
				redoAction.updateRedoState() ;
				return ;
			}

   		// A Help Contents request occurs only if the installed Help system is
         // not available.  In this case we attempt to reference online help
         // through Kisekae World.

   		if (source == help)
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

			// A Print request prints the current panel display.

			if (source == print)
			{
				PrinterJob pj = PrinterJob.getPrinterJob() ;
				pj.setPrintable(this,pageformat) ;
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

			// A Print Preview request shows a preview frame.

			if (source == printpreview)
			{
				int orientation = PageFormat.PORTRAIT ;
				if	(pageformat != null) orientation = pageformat.getOrientation() ;
				new PrintPreview(this,orientation) ;
				return ;
			}

			// A Page Setup request establishes the print control page format.

			if (source == pagesetup)
			{
				PrinterJob pj = PrinterJob.getPrinterJob() ;
				pageformat = pj.pageDialog(getPageFormat()) ;
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
            int n = JOptionPane.showConfirmDialog(me,
               Kisekae.getCaptions().getString("UndoAllConfirmText"),
               Kisekae.getCaptions().getString("MenuEditUndoAll"),
               JOptionPane.YES_NO_OPTION) ;
            if (n != JOptionPane.YES_OPTION) return ;

            // Undo everything.  Reset to initial state.

            me.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            while (undo.canUndo()) undo.undo() ;
            me.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            undo.discardAllEdits() ;
            undoAction.updateUndoState() ;
            redoAction.updateRedoState() ;
            undoall.setEnabled(undo.canUndo());
         }

			// A Find request invokes the find/replace dialog.

			if (source == find || source == FIND || source == replace)
			{
				int tab = FINDTAB ;
				if (source == replace) tab = REPLACETAB ;
				if (findDialog == null)
					findDialog = new TextFindDialog(this,tab) ;
				else
					findDialog.setSelectedIndex(tab) ;
				findDialog.show() ;
				return ;
			}

			// A Foreground request sets the text foreground color.

			if (source == foreground)
			{
         	if (kit == null) return ;
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setForeground(attr,foreground.getColor()) ;
				setAttributeSet(attr) ;
				return ;
			}

			// A Background request sets the text background color.

			if (source == background)
			{
         	if (kit == null) return ;
				MutableAttributeSet attr = new SimpleAttributeSet() ;
				StyleConstants.setBackground(attr,background.getColor()) ;
				setAttributeSet(attr) ;
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
			PrintLn.println("TextFrame: Out of memory.") ;
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
			PrintLn.println("TextFrame: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Return focus to the text object.

		finally { setFocus() ; }
	}


	// ItemListener interface.  The item state changed method is invoked
	// when checkbox menu items are selected.

	public void itemStateChanged(ItemEvent evt)
	{
		Object source = evt.getSource() ;

		// Turn the line number panel on and off.

		if (source == linenum)
		{
      	showline = linenum.getState() ;
			repaint() ;
         return ;
		}

		// Wordwrap request.  Alter the word wrap policy.

		if (source == wordwrap)
		{
      	if (scrollpanel == null) return ;
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         scrollpanel.setLineWrap(wordwrap.getState()) ;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         scrollpanel.revalidate();
         return ;
		}

      // Bold menu checkbox request.

      if (source == boldformat)
      {
      	if (BOLD.isSelected() == boldformat.getState())
	      	BOLD.setSelected(!boldformat.getState()) ;
         BOLD.doClick() ;
         return ;
      }

      // Convert to and from rich text.

      if (source == richtext)
      {
      	if (textobject == null) return ;
      	if (richtext.isSelected())
         {
				if (textobject.isStyledText()) return ;
				textobject.setEditorKit(new RTFEditorKit()) ;
         }
         else
         {
				if (!textobject.isStyledText()) return ;
				int i = JOptionPane.showConfirmDialog(this,
               Kisekae.getCaptions().getString("FormatChangeText"),
               Kisekae.getCaptions().getString("FormatChangeTitle"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE) ;
            if (i == JOptionPane.YES_OPTION)
            {
					textobject.setEditorKit(null) ;
			      undo.discardAllEdits();
					undoAction.updateUndoState() ;
					redoAction.updateRedoState() ;
            }
            else
               richtext.setSelected(true) ;
			}

			// Establish the new text variables.

			text = textobject.getTextComponent() ;
         text.setEnabled(!Kisekae.isSecure());
         text.setDisabledTextColor(Color.BLACK) ;
			kit = textobject.getEditorKit() ;
			doc = textobject.getDocument() ;
			doc.addUndoableEditListener(this) ;
			doc.addDocumentListener(this) ;
			text.addCaretListener(caretlistener) ;
			text.addFocusListener(focuslistener) ;
			text.addKeyListener(keylistener) ;
			updateInterface(textobject) ;
      }

      // Italic menu checkbox request.

      if (source == italicformat)
      {
      	if (ITALIC.isSelected() == italicformat.getState())
	      	ITALIC.setSelected(!italicformat.getState()) ;
         ITALIC.doClick() ;
      }

      // Underline menu checkbox request.

      if (source == underlineformat)
      {
      	if (UNDERLINE.isSelected() == underlineformat.getState())
	      	UNDERLINE.setSelected(!underlineformat.getState()) ;
         UNDERLINE.doClick() ;
      }

      // Text justification checkbox request.

      if (source == leftjustify)
      {
      	if (LEFT.isSelected() == leftjustify.getState())
	      	LEFT.setSelected(!leftjustify.getState()) ;
         LEFT.doClick() ;
      }

      if (source == centerjustify)
      {
      	if (CENTER.isSelected() == centerjustify.getState())
	      	CENTER.setSelected(!centerjustify.getState()) ;
         CENTER.doClick() ;
      }

      if (source == rightjustify)
      {
      	if (RIGHT.isSelected() == rightjustify.getState())
	      	RIGHT.setSelected(!rightjustify.getState()) ;
         RIGHT.doClick() ;
      }

      if (source == justify)
      {
      	if (JUSTIFY.isSelected() == justify.getState())
	      	JUSTIFY.setSelected(!justify.getState()) ;
         JUSTIFY.doClick() ;
      }
	}

   // Implementation of the menu item update of our state when we become
   // visible.  We remove all prior entries and rebuild the Window menu. 
   
   void updateRunState()
   {
      for (int j = windowMenu.getItemCount()-1 ; j >= 0 ; j--)
         windowMenu.remove(j) ;

      // Add new dialog entries

      int n = 0 ;
      Vector v = KissFrame.getWindowFrames() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         KissFrame w = (KissFrame) v.elementAt(i) ;
         String s = w.getTitle() ;
         JMenuItem mi = new JMenuItem(++n + ". " + s) ;
         mi.addActionListener(this) ;
         windowMenu.add(mi) ;
      }
   }


	// Window Events

  	public void windowOpened(WindowEvent evt) { setFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { updateRunState() ; }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { closecheck(false) ; close() ; }


	// A utility function to write the text file contents.  If this TextFrame
   // object was constructed from a zip file element then we need to update
   // the zip file source.  If this TextFrame object was constructed from
   // a known input and output stream then we simply write to the memory
   // output stream.  If we are performing a Save As then we write to the 
   // local file system.  A Save replaces the current file, which can be in 
   // an archive.


	private void savetext(boolean saveas)
	{
		if (textobject == null) return ;
      if (saveas) 
      { 
         textobject.setZipFile(null) ; 
         ArchiveEntry zenew = new DirEntry("text.txt") ;
         if (ze != null) zenew = (ArchiveEntry) ze.clone() ;
         textobject.setZipEntry(zenew) ;
         ze = null ; 
      }
      
      // Save this element.  Make sure our zip file is closed,
      // otherwise the write can fail.

      if (zip != null && zip.isOpen()) 
      {
         try { zip.close() ; }
         catch (IOException e) { }       
      }
      
   	textobject.setUpdated(true);
      FileSave fs = new FileSave(this,textobject) ;
      fs.addWriteListener(writelistener) ;
      fs.setFileFilter("Text");
		if (saveas) fs.show() ; else fs.save() ;
	}



   // Method to check for pending updates.  This method returns true if
   // the dialog should be closed.

	private boolean closecheck(boolean cancel)
	{
		int opt = JOptionPane.YES_NO_OPTION ;
		if (cancel) opt = JOptionPane.YES_NO_CANCEL_OPTION ;
		if (update.isEnabled())
		{
			int i = JOptionPane.showConfirmDialog(this,
            Kisekae.getCaptions().getString("ApplyChangeText"),
            Kisekae.getCaptions().getString("ApplyChangeTitle"),
				opt, JOptionPane.QUESTION_MESSAGE) ;

			// If we should apply the changes, create a new memory copy of
			// the element.  If we do not apply the changes we must restore
			// the original archive entry name as it may have changed
			// as a result of a file save.

			if (i == JOptionPane.CANCEL_OPTION)
				return false ;
			if (i == JOptionPane.YES_OPTION)
				callback.doClick() ;
			else
				if (originalze != null) originalze.setPath(originalfile) ;
		}

		// If we are editing a non-active file, check for a file save.

		else if (saveas.isEnabled() && changed && !Kisekae.isSecure())
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

			// Save the text contents if necessary.  Note, with an inactive file
         // there is no SaveAs if we came from an archive file.

         boolean b = zip.isArchive() ;
			if (i == JOptionPane.CANCEL_OPTION)
				return false ;
			if (i == JOptionPane.YES_OPTION)
            savetext(!b) ;
		}

		// Dispose of updates.

      if (undo != null) 
      {
         undo.discardAllEdits();
   		undoAction.updateUndoState() ;
      	redoAction.updateRedoState() ;
      }
		return true ;
	}


   // We close the frame after clean up.

	public void close()
	{
		if (OptionsDialog.getDebugControl())
      	PrintLn.println("TextFrame terminates") ;
      callback.removeActionListener(null) ;
		super.close() ;
      flush() ;
      dispose() ;
   }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
	// objects when the data set is closed, even if some problem occurs while
	// disposing with the dialog window.

	private void flush()
   {
		setVisible(false) ;
		if (text != null)
		{
			text.removeCaretListener(caretlistener) ;
			text.removeFocusListener(focuslistener) ;
			text.removeKeyListener(keylistener) ;
      }

		if (doc != null)
		{
   		doc.removeUndoableEditListener(this) ;
			doc.removeDocumentListener(this) ;
		}

      // Release some important references.

		doc = null ;
		kit = null ;
		text = null ;
		textobject = null ;
      pageformat = null ;
      sprintview = null ;
      pprintview = null ;
      undoAction = null ;
      redoAction = null ;

      // Remove some action listeners.

		if (NEW != null) NEW.removeActionListener(this) ;
		if (OPEN != null) OPEN.removeActionListener(this) ;
		if (CLOSE != null) CLOSE.removeActionListener(this) ;
		if (SAVE != null) SAVE.removeActionListener(this) ;
		if (FIND != null) FIND.removeActionListener(this) ;
		if (CUT != null) CUT.removeActionListener(this) ;
		if (COPY != null) COPY.removeActionListener(this) ;
		if (PASTE != null) PASTE.removeActionListener(this) ;
		if (UNDO != null) UNDO.removeActionListener(this) ;
		if (REDO != null) REDO.removeActionListener(this) ;
		if (FONTS != null) FONTS.removeActionListener(fontlistener) ;
		if (FONTSIZE != null) FONTSIZE.removeActionListener(fontsizelistener) ;
		if (BOLD != null) BOLD.removeActionListener(boldlistener) ;
		if (ITALIC != null) ITALIC.removeActionListener(italiclistener) ;
		if (UNDERLINE != null) UNDERLINE.removeActionListener(underlinelistener) ;
		if (LEFT != null) LEFT.removeActionListener(leftjustifylistener) ;
		if (CENTER != null) CENTER.removeActionListener(centerjustifylistener) ;
		if (RIGHT != null) RIGHT.removeActionListener(rightjustifylistener) ;
		if (JUSTIFY != null) JUSTIFY.removeActionListener(justifylistener) ;

      // Clear the menu action table.

      if (mb != null) mb.removeAll() ;
      if (undo != null) undo.discardAllEdits();
		Action a = getActionByName("Cut") ;
		if (a != null) CUT.removeActionListener(a) ;
		a = getActionByName("Copy") ;
		if (a != null) COPY.removeActionListener(a) ;
		a = getActionByName("Paste") ;
		if (a != null) PASTE.removeActionListener(a) ;
      if (actions != null) actions.clear() ;
      actions = null ;
      undo = null ;
      mb = null ;

      // Clear the window references.

		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
		Runtime.getRuntime().gc() ;
   }


   // Print interface method.   This method calls the text object to render
   // a page for printing.

   public int print(Graphics g, PageFormat pageformat, int pageindex)
   {
      if (textobject == null) return Printable.NO_SUCH_PAGE ;
      return textobject.print(g,pageformat,pageindex);
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
			try { undo.undo(); }
			catch (CannotUndoException ex)
			{
				PrintLn.println("TextFrame: Unable to undo edit") ;
				ex.printStackTrace();
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}
			updateUndoState() ;
			redoAction.updateRedoState() ;
			save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
	   	SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
		}

		protected void updateUndoState()
		{
			if (undo.canUndo())
			{
				setEnabled(true) ;
				UNDO.setEnabled(true) ;
            undoall.setEnabled(true) ;
				putValue(Action.NAME, undo.getUndoPresentationName()) ;
			}
			else
			{
				setEnabled(false) ;
				UNDO.setEnabled(false) ;
            undoall.setEnabled(false) ;
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
			try { undo.redo() ; }
			catch (CannotRedoException ex)
			{
				PrintLn.println("TextFrame: Unable to redo edit") ;
				ex.printStackTrace() ;
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
			}
			updateRedoState() ;
			undoAction.updateUndoState() ;
			save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
	   	SAVE.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
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


	// Inner class to override the JTextPane class to stop word wrap.

	class TextPanel extends JPanel implements Scrollable
	{
		private JTextComponent text = null ;
      private Dimension size = null ;
      private boolean linewrap = false ;

		// Constructor

		TextPanel(JTextComponent text)
		{
         this.text = text ;
	      setLayout(new BorderLayout());
			add(text,BorderLayout.CENTER) ;
      }

		// Scrollable interface methods.

		public boolean getScrollableTracksViewportWidth()
//		{  return text.getScrollableTracksViewportWidth() ; }
		{  return linewrap ; }

		public boolean getScrollableTracksViewportHeight()
		{  return text.getScrollableTracksViewportHeight() ; }

		public int getScrollableUnitIncrement(Rectangle r, int o, int d)
		{  return text.getScrollableUnitIncrement(r,o,d) ; }

		public int getScrollableBlockIncrement(Rectangle r, int o, int d)
		{  return text.getScrollableBlockIncrement(r,o,d) ; }

		public Dimension getPreferredScrollableViewportSize()
		{  return text.getPreferredScrollableViewportSize() ;  }

      // Set the linewrap attribute.

      void setLineWrap(boolean b)
      {
      	linewrap = b ;
      	if (!(text instanceof JTextArea)) return ;
         ((JTextArea) text).setLineWrap(b) ;
      }

      // Get the preferred size.  

      public Dimension getPreferredSize()
		{
         return getPreferredScrollableViewportSize() ;
      }

      // Set the panel size.

      public void setSize(Dimension d)
      {
      	super.setSize(d) ;
      	size = d ;
      }
	}


	// Inner class to create a border that displays line numbers.
	// @author Sandip Chitale (schitale@selectica.com)

	class LineNumberBorder extends AbstractBorder
   {
		private Color numberColor;
		private Vector errorlines = null ;
		private Integer errorline = null ;

      // Constructor

		public LineNumberBorder() { }
		public LineNumberBorder(Color lineNumberColor)
      { 
         numberColor = lineNumberColor; 
      }

		/**
		* Paints the border for the specified component with the specified
		* position and size.
		* @param c the component for which this border is being painted
		* @param g the paint graphics
		* @param x the x position of the painted border
		* @param y the y position of the painted border
		* @param width the width of the painted border
		* @param height the height of the painted border
		*/

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
		{
         if (!showline) return ;
			if (!(c instanceof JTextArea)) return ;
			JTextArea ta = (JTextArea) c ;
         Rectangle view = new Rectangle(x,y,width,height) ;

         // Determine the scrollpane view rectangle.

         if (scrollpane != null)
         {
            JViewport viewport = scrollpane.getViewport() ;
            view = viewport.getViewRect() ;
         }

         // Set line number color.

			Color oldColor = g.getColor();
			if (numberColor == null) numberColor = c.getForeground();
			g.setColor(numberColor);
			g.translate(x,y);

			// Mask the left margin area.

			Graphics cg = g.create();
			Insets insets = getBorderInsets(c);
			cg.setClip(0,insets.top,insets.left,height-insets.top-insets.bottom);
			Font f = ta.getFont();
			FontMetrics fm = cg.getFontMetrics(f);

         // Determine the viewport line limits in the text document.

         int start = 0 ;
         int end = ta.getLineCount() ;
         Point pos = new Point(view.x,view.y) ;
         try { start = ta.getLineOfOffset(ta.viewToModel(pos)) ; }
         catch (BadLocationException ble) { } ;
         pos.y += view.height ;
         try { end = ta.getLineOfOffset(ta.viewToModel(pos)) ; }
         catch (BadLocationException ble) { } 

         // Establish our first error line number greater than or
         // equal to the start line number.  Note that the start value
         // is one less than the first line on display.

         errorline = null ;
         int nexterror = 0 ;
         if (errorlines != null) 
         {
            nexterror = Collections.binarySearch(errorlines,new Integer(start+1)) ;
            if (nexterror < 0) nexterror = -(nexterror+1) ;
            if (nexterror < errorlines.size()) 
               errorline = (Integer) errorlines.elementAt(nexterror) ;
         }

         // Create the border in the view rectangle.

			for (int i = start ; i < end+1 ; i++)
         {
				try
            {
					Rectangle r = ta.modelToView(ta.getLineStartOffset(i));
					int lx = insets.left - fm.stringWidth("W" + (i+1));
					int ly = r.y + fm.getLeading() + fm.getMaxAscent();
               if (errorline != null && i+1 == errorline.intValue()) 
               {
                  errorline = null ;
                  cg.setColor(Color.red);
   					cg.drawString("" + (i+1), lx, ly);
                  cg.setColor(numberColor);
                  if (++nexterror < errorlines.size())
                     errorline = (Integer) errorlines.elementAt(nexterror) ;
               }
               else
   					cg.drawString("" + (i+1), lx, ly);
				}
            catch (BadLocationException ble) { }
			}
			cg.dispose();
			g.setColor(oldColor);
		}

		/**
		* Returns the insets of the border.
		* @param c the component for which this border insets value applies
		*/

		public Insets getBorderInsets(Component c)
		{
			if (!showline) return new Insets(0,5,0,5);
			if (!(c instanceof JTextArea)) return new Insets(0,5,0,5);
			FontMetrics fm = c.getFontMetrics(c.getFont());
			int margin = fm.stringWidth("WWWWWW");
			return new Insets(0,margin,0,5);
		}

		/**
		* Returns whether or not the border is opaque. If the border
		* is opaque, it is responsible for filling in it's own
		* background when painting.
		*/

		public boolean isBorderOpaque() { return false; }

		/**
		* Establish an error line number highlighted in red. A negative number
      * removes all error line numbers.
		*/

		public void setErrorLine(int n) 
      {
         if (n < 0)
         {
            errorlines = null ;
            return ;
         }

         if (errorlines == null) errorlines = new Vector() ;
         Integer key = new Integer(n) ;
         if (errorlines.contains(key)) return ;
         errorlines.addElement(key) ; 
         Collections.sort(errorlines) ;
      }
      
      public Vector getErrorLines()
      {
         return errorlines ;
      }
   }
}
