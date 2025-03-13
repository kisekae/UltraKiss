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
* PanelMenu class
*
* Purpose:
*
* This class encapsulates the menu bar for the panel frame.  This
* menu depends on implementation functions in the main menu for file
* and toolbar support.
*
*/

import java.awt.* ; 
import java.awt.event.* ;
import java.awt.print.* ;
import java.awt.dnd.* ;
import java.awt.datatransfer.* ;
import java.io.* ;
import java.util.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.undo.* ;
import javax.swing.ImageIcon;


final class PanelMenu extends KissMenu
   implements ActionListener
{
   private static final String LABEL = "label" ;
   private static final String BUTTON = "button" ;
   private static final String RADIOBUTTON = "radiobutton" ;
   private static final String TOGGLEBUTTON = "togglebutton" ;
   private static final String CHECKBOX = "checkbox" ;
   private static final String LIST = "list" ;
   private static final String COMBOBOX = "combobox" ;
   private static final String TEXTBOX = "textbox" ;
   private static final String TEXTAREA = "textarea" ;
   private static final String TEXTPANE = "textpane" ;
   private static final String TEXTFIELD = "textfield" ;
   private static final String PASSWORDFIELD = "passwordfield" ;
   private static final String MENUITEM = "menuitem" ;

   private MainMenu menu = null ;			// Reference to the main menu
   private boolean enableRestart = true ;	

   // MenuBar definitions

   private JMenu m[] = new JMenu[7];
   private JMenu newmenu = null ;
   private JMenu pagemenu = null ;
   private JMenu colormenu = null ;
   private JMenu datasetmenu = null ;
   private JMenu importimage = null ;
   private JMenu addcomponent = null ;

   // Menu item declarations

   private JMenuItem page[] = null ;
   private JMenuItem color[] = null ;
   private JMenuItem edit[] = null ;
   protected JMenuItem undoall = null ;
   protected JMenuItem cut = null ;
   protected JMenuItem copy = null ;
   protected JMenuItem paste = null ;
   protected JMenuItem pastenew = null ;
   protected JMenuItem ungroup = null ;
   protected JMenuItem regroup = null ;
   protected JMenuItem newgroup = null ;
   protected JMenuItem selectall = null ;
   protected JMenuItem selectallvisible = null ;
   protected JMenuItem unselectall = null ;
   protected JMenuItem selectfind = null ;
   protected JMenuItem addimage = null ;
   protected JMenuItem addimagegroup = null ;
   protected JMenuItem externalpaste = null ;
   protected JMenuItem addlabel = null ;
   protected JMenuItem addbutton = null ;
   protected JMenuItem addtogglebutton = null ;
   protected JMenuItem addradiobutton = null ;
   protected JMenuItem addcheckbox = null ;
   protected JMenuItem addlist = null ;
   protected JMenuItem addcombobox = null ;
   protected JMenuItem addtextbox = null ;
   protected JMenuItem addtextarea = null ;
   protected JMenuItem addtextpane = null ;
   protected JMenuItem addtextfield = null ;
   protected JMenuItem addpasswordfield = null ;
   protected JMenuItem addmenuitem = null ;
   protected JMenuItem layerimage = null ;
   protected JMenuItem editimage = null ;
   protected JMenuItem selectcnf = null ;
   protected JMenuItem importcnf = null ;
   protected JMenuItem appendcnf = null ;
   protected JMenuItem expand = null ;
   protected JMenuItem close = null ;
   protected JMenuItem save = null ;
   protected JMenuItem saveas = null ;
   protected JMenuItem saveasarchive = null ;
   protected JMenuItem saveasfiles = null ;
   protected JMenuItem savenew = null ;
   protected JMenuItem reset = null ;
   protected JMenuItem restart = null ;
   protected JMenuItem magnify = null ;
   protected JMenuItem reduce = null ;
   protected JMenuItem scale = null ;
   protected JMenuItem deletepage = null ;
   protected JMenuItem insertpage = null ;
   protected JMenuItem addpage = null ;
   protected JMenuItem writepage = null ;
   protected JMenuItem print = null ;
   protected JMenuItem preview = null ;
   protected JMenuItem pagesetup = null ;
   protected JMenuItem logfile = null ;
   protected JMenuItem cnffile = null ;
   protected JMenuItem objects = null ;
   protected JMenuItem archive = null ;
   protected JMenuItem activities = null ;
   protected JMenuItem debugger = null ;
   protected JMenuItem properties = null ;
   protected JMenuItem importpalette = null ;
   protected JMenuItem importaudio = null ;
   protected JMenuItem importvideo = null ;
   protected JMenuItem importother = null ;
   protected JMenuItem loadtext = null ;
   protected JMenuItem export = null ;

   // Edit text frame references

   private TextFrame tf = null ;					// The edit text frame
   private ColorFrame cf = null ;				// The palette edit frame
   private ImageFrame ef = null ;				// The image edit frame
   private InputStream is = null ;				// The file input stream
   private OutputStream os = null ;				// The file output stream

   // Undo helpers

   private UndoManager undo = new UndoManager() ;
   private UndoAction undoAction ;				 // Action for undo
   private RedoAction redoAction ;				 // Action for redo




   // Constructor

   public PanelMenu (MainFrame frame, MainMenu menu)
   {
      this.parent = frame ;
      this.menu = menu ;
      Configuration config = parent.getConfig() ;
      int npage = (config == null) ? 0 : config.getPageCount() ;
      page = new JMenuItem[npage] ;
      color = new JMenuItem[0] ;
      edit = new JMenuItem[3] ;
      int n = OptionsDialog.getUndoLimit() ;
      if (menu.getNoCopy()) OptionsDialog.setSecurityEnable(true) ;
      createMenu() ;
   }

   // Initialization.

   boolean createMenu()
   {
      // Update the existing menubar.  We remove all menu items
      // except the first, then rebuild the menubar from scratch.
      // We take this approach to stop the menubar from disappearing
      // from view, as would happen if we substituted a new menubar
      // object in the parent frame.

      mb = menu.getMenuBar() ;
      if (mb == null) return false ;
      for (int i = mb.getMenuCount() ; i > 1 ; i--) mb.remove(i-1) ;
      m[0] = mb.getMenu(0) ;
      Insets insets = m[0].getMargin() ;
      if (m[0].getItemCount() > 0) m[0].removeAll() ;

      // Create the File menu.

      boolean applemac = OptionsDialog.getAppleMac() ;
      int apple = (OptionsDialog.getAppleMac()) ? ActionEvent.SHIFT_MASK : 0 ;
//    m[0].add((newmenu = new JMenu(Kisekae.getCaptions().getString("MenuFileNew")))) ;
//    newmenu.add(menu.newkiss) ;
//    menu.newkiss.setText(Kisekae.getCaptions().getString("MenuFileNewCnf"));
//    if (!applemac) menu.newkiss.setMnemonic(KeyEvent.VK_N) ;
//    menu.newkiss.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelerator));
//    menu.newkiss.setEnabled(OptionsDialog.getEditEnable());
//    newmenu.add(menu.newpage) ;
//    menu.newpage.setText(Kisekae.getCaptions().getString("MenuFileNewPage"));
//    if (!applemac) menu.newpage.setMnemonic(KeyEvent.VK_P) ;
//    menu.newpage.setEnabled(OptionsDialog.getEditEnable());
      m[0].add(menu.open) ;
      m[0].add(menu.expand) ;
      m[0].add((selectcnf = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSelect")))) ;
      selectcnf.addActionListener(this) ;
      if (!applemac) selectcnf.setMnemonic(KeyEvent.VK_L) ;
      m[0].add((importcnf = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewCnf")))) ;
      importcnf.addActionListener(this) ;
      if (!applemac) importcnf.setMnemonic(KeyEvent.VK_N) ;
      appendcnf = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewCnf")) ;
      appendcnf.addActionListener(this) ;
      m[0].add((close = new JMenuItem(Kisekae.getCaptions().getString("MenuFileClose")))) ;
      close.addActionListener(this) ;
      if (!applemac) close.setMnemonic(KeyEvent.VK_C) ;
      close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, accelerator));
      m[0].addSeparator() ;
      m[0].add((save = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSave")))) ;
      save.addActionListener(this) ;
      if (!applemac) save.setMnemonic(KeyEvent.VK_S) ;
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelerator));
      save.setEnabled(false) ;
      m[0].add((saveas = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAs")))) ;
      saveas.addActionListener(this) ;
      saveas.setEnabled(!Kisekae.isSecure() && !Kisekae.isExpired() && !menu.getNoCopy()) ;
      if (!applemac) saveas.setMnemonic(KeyEvent.VK_A) ;
      m[0].add((saveasarchive = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAsArchive")))) ;
      saveasarchive.addActionListener(this) ;
      saveasarchive.setVisible(false) ;
      m[0].add((saveasfiles = new JMenuItem(Kisekae.getCaptions().getString("MenuFileSaveAsFiles")))) ;
      saveasfiles.addActionListener(this) ;
      saveasfiles.setVisible(false) ;
		m[0].add((savenew = new JMenuItem(Kisekae.getCaptions().getString("MenuFileWriteAs")))) ;
		savenew.addActionListener(this) ;
		savenew.setEnabled(OptionsDialog.getEditEnable() && !Kisekae.isSecure()) ;
      savenew.setVisible(false) ;
      m[0].addSeparator() ;
      m[0].add(menu.openurl) ;
      m[0].add(menu.openweb) ;
      m[0].add(menu.openportal) ;
      m[0].addSeparator() ;
      m[0].add((pagesetup = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePageSetup")))) ;
      pagesetup.addActionListener(this) ;
      if (!applemac) pagesetup.setMnemonic(KeyEvent.VK_U) ;
      pagesetup.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
      m[0].add((preview = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrintPreview")))) ;
      preview.addActionListener(this) ;
      if (!applemac) preview.setMnemonic(KeyEvent.VK_V) ;
      preview.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
      m[0].add((print = new JMenuItem(Kisekae.getCaptions().getString("MenuFilePrint")))) ;
      print.addActionListener(this) ;
      if (!applemac) print.setMnemonic(KeyEvent.VK_P) ;
      print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelerator));
      print.setEnabled(Kisekae.isPrintInstalled() && !Kisekae.isSecure() && !Kisekae.isExpired()) ;
      m[0].addSeparator() ;
      m[0].add((export = new JMenuItem(Kisekae.getCaptions().getString("MenuFileExport")))) ;
      export.addActionListener(this) ;
      export.setEnabled(!Kisekae.isSecure() && !Kisekae.isExpired()) ;
      m[0].addSeparator() ;
      m[0].add((properties = new JMenuItem(Kisekae.getCaptions().getString("MenuFileProperties")))) ;
      properties.addActionListener(this) ;
      properties.setEnabled(OptionsDialog.getEditEnable()) ;
      m[0].addSeparator() ;
      m[0].add(menu.exit) ;

      // Create the Edit menu.

      m[1] = new JMenu(Kisekae.getCaptions().getString("MenuEdit"));
      m[1].setMargin(insets) ;
      if (!applemac) m[1].setMnemonic(KeyEvent.VK_E);
      mb.add(m[1]);
      m[1].addActionListener(this) ;
      m[1].add((undoAction = new UndoAction())) ;
      m[1].add((redoAction = new RedoAction())) ;
      m[1].add((undoall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUndoAll")))) ;
      undoall.addActionListener(this) ;
      undoall.setEnabled(false) ;
      m[1].addSeparator() ;
      m[1].add((cut = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCut")))) ;
      cut.addActionListener(this) ;
      if (!applemac) cut.setMnemonic(KeyEvent.VK_T) ;
      cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelerator));
      cut.setEnabled(false) ;
      m[1].add((copy = new JMenuItem(Kisekae.getCaptions().getString("MenuEditCopy")))) ;
      copy.addActionListener(this) ;
      if (!applemac) copy.setMnemonic(KeyEvent.VK_C) ;
      copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelerator));
      copy.setEnabled(false) ;
      m[1].add((paste = new JMenuItem(Kisekae.getCaptions().getString("MenuEditPaste")))) ;
      paste.addActionListener(this) ;
      if (!applemac) paste.setMnemonic(KeyEvent.VK_P) ;
      paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, accelerator));
      paste.setEnabled(false) ;
      m[1].add((pastenew = new JMenuItem(Kisekae.getCaptions().getString("MenuEditPasteNew")))) ;
      pastenew.addActionListener(this) ;
      pastenew.setEnabled(false) ;
      m[1].addSeparator() ;
      m[1].add((ungroup = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUngroup")))) ;
      ungroup.addActionListener(this) ;
      ungroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, accelerator));
      ungroup.setEnabled(false) ;
      m[1].add((regroup = new JMenuItem(Kisekae.getCaptions().getString("MenuEditRegroup")))) ;
      regroup.addActionListener(this) ;
      regroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, accelerator+ActionEvent.SHIFT_MASK));
      regroup.setEnabled(false) ;
      m[1].add((newgroup = new JMenuItem(Kisekae.getCaptions().getString("MenuEditNewgroup")))) ;
      newgroup.addActionListener(this) ;
      newgroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, accelerator+ActionEvent.ALT_MASK));
      newgroup.setEnabled(false) ;
      m[1].addSeparator() ;
      m[1].add((selectall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectAll")))) ;
      selectall.addActionListener(this) ;
      if (!applemac) selectall.setMnemonic(KeyEvent.VK_A) ;
      selectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator));
      selectall.setEnabled(false) ;
      m[1].add((selectallvisible = new JMenuItem(Kisekae.getCaptions().getString("MenuEditSelectAllVisible")))) ;
      selectallvisible.addActionListener(this) ;
      selectallvisible.setEnabled(false) ;
      m[1].add((unselectall = new JMenuItem(Kisekae.getCaptions().getString("MenuEditUnselectAll")))) ;
      unselectall.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelerator+ActionEvent.SHIFT_MASK));
      unselectall.addActionListener(this) ;
      unselectall.setEnabled(false) ;
      if (!applemac) unselectall.setMnemonic(KeyEvent.VK_U) ;
      m[1].add((selectfind = new JMenuItem(Kisekae.getCaptions().getString("MenuEditFind")))) ;
      selectfind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, accelerator));
      selectfind.addActionListener(this) ;
      selectfind.setEnabled(false) ;
      if (!applemac) selectfind.setMnemonic(KeyEvent.VK_F) ;
      m[1].addSeparator() ;
      m[1].add((addcomponent = new JMenu(Kisekae.getCaptions().getString("MenuEditAddComponent")))) ;
      addcomponent.setEnabled(false) ;
      addcomponent.add((addlabel = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentLabel")))) ;
      addlabel.addActionListener(this) ;
      addcomponent.add((addbutton = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentButton")))) ;
      addbutton.addActionListener(this) ;
      addcomponent.addSeparator() ;
      addcomponent.add((addtogglebutton = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentToggleButton")))) ;
      addtogglebutton.addActionListener(this) ;
      addcomponent.add((addradiobutton = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentRadioButton")))) ;
      addradiobutton.addActionListener(this) ;
      addcomponent.add((addcheckbox = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentCheckBox")))) ;
      addcheckbox.addActionListener(this) ;
      addcomponent.addSeparator() ;
      addcomponent.add((addtextbox = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentTextBox")))) ;
      addtextbox.addActionListener(this) ;
      addcomponent.add((addtextarea = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentTextArea")))) ;
      addtextarea.addActionListener(this) ;
      addcomponent.add((addtextpane = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentTextPane")))) ;
      addtextpane.addActionListener(this) ;
      addcomponent.add((addtextfield = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentTextField")))) ;
      addtextfield.addActionListener(this) ;
      addcomponent.addSeparator() ;
      addcomponent.add((addlist = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentList")))) ;
      addlist.addActionListener(this) ;
      addcomponent.add((addcombobox = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentComboBox")))) ;
      addcombobox.addActionListener(this) ;
      addcomponent.add((addmenuitem = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentMenuItem")))) ;
      addmenuitem.addActionListener(this) ;
      addcomponent.add((addpasswordfield = new JMenuItem(Kisekae.getCaptions().getString("MenuComponentPasswordField")))) ;
      addpasswordfield.addActionListener(this) ;
      m[1].add((importimage = new JMenu(Kisekae.getCaptions().getString("MenuEditImportImage")))) ;
      importimage.setEnabled(false) ;
      importimage.add((externalpaste = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportPaste")))) ;
      externalpaste.addActionListener(this) ;
      externalpaste.setEnabled(true) ;
      importimage.add((addimagegroup = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportObject")))) ;
      addimagegroup.addActionListener(this) ;
      addimagegroup.setEnabled(true) ;
      importimage.add((addimage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportSelection")))) ;
      addimage.addActionListener(this) ;
      addimage.setEnabled(false) ;
      m[1].add((importother = new JMenu(Kisekae.getCaptions().getString("MenuEditImportOther")))) ;
      importother.setEnabled(false) ;
      importother.add((importpalette = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportPalette")))) ;
      importpalette.addActionListener(this) ;
      importpalette.setEnabled(false) ;
      importother.add((importaudio = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportAudio")))) ;
      importaudio.addActionListener(this) ;
      importaudio.setEnabled(false) ;
      importother.add((importvideo = new JMenuItem(Kisekae.getCaptions().getString("MenuEditImportVideo")))) ;
      importvideo.addActionListener(this) ;
      importvideo.setEnabled(false) ;
      m[1].addSeparator() ;
      m[1].add((layerimage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditAdjustLayering")))) ;
      layerimage.addActionListener(this) ;
      m[1].add((editimage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditEditObject")))) ;
      editimage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelerator));
      editimage.addActionListener(this) ;
      editimage.setEnabled(false) ;
      m[1].addSeparator() ;
      m[1].add((deletepage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditDeletePage")))) ;
      deletepage.addActionListener(this) ;
      deletepage.setEnabled(OptionsDialog.getEditEnable() && page.length > 0) ;
      m[1].add((insertpage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditInsertPage")))) ;
      insertpage.addActionListener(this) ;
      insertpage.setEnabled(OptionsDialog.getEditEnable() && page.length > 0) ;
      m[1].add((addpage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditAddPage")))) ;
      addpage.addActionListener(this) ;
      addpage.setEnabled(OptionsDialog.getEditEnable()) ;
      m[1].add((writepage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditWritePage")))) ;
      writepage.addActionListener(this) ;
      writepage.setEnabled(false) ;

      // Create the View menu.

      m[2] = new JMenu(Kisekae.getCaptions().getString("MenuView")) ;
      m[2].setMargin(insets) ;
      if (!applemac) m[2].setMnemonic(KeyEvent.VK_V);
      mb.add(m[2]) ;
      m[2].addActionListener(this) ;
      m[2].add(menu.toolbar) ;
      m[2].add(menu.statusbar) ;
      m[2].addSeparator() ;
      m[2].add((reset = new JMenuItem(Kisekae.getCaptions().getString("MenuViewReset")))) ;
      reset.addActionListener(this) ;
      if (!applemac) reset.setMnemonic(KeyEvent.VK_R) ;
      reset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7,0)) ;
      m[2].add((restart = new JMenuItem(Kisekae.getCaptions().getString("MenuViewRestart")))) ;
      restart.addActionListener(this) ;
      restart.setEnabled(enableRestart) ;
      if (!applemac) restart.setMnemonic(KeyEvent.VK_S) ;
      restart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8,0)) ;
      m[2].addSeparator() ;
      m[2].add((pagemenu = new JMenu(Kisekae.getCaptions().getString("MenuViewPageSet")))) ;
      for (int i = 0 ; i < page.length ; i++)
      {
         String s = "" + i ;
         pagemenu.add((page[i] = new JMenuItem(s))) ;
         page[i].addActionListener(this) ;
      }
      m[2].add((colormenu = new JMenu(Kisekae.getCaptions().getString("MenuViewColorSet")))) ;
      for (int i = 0 ; i < color.length ; i++)
      {
         String s = "" + i ;
         colormenu.add((color[i] = new JMenuItem(s))) ;
         color[i].addActionListener(this) ;
      }
      m[2].addSeparator() ;
      m[2].add((magnify = new JMenuItem(Kisekae.getCaptions().getString("MenuViewMagnify")))) ;
      magnify.addActionListener(this) ;
      if (!applemac) magnify.setMnemonic(KeyEvent.VK_N) ;
      if (!applemac) magnify.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0)) ;
      else magnify.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0)) ;
      m[2].add((reduce = new JMenuItem(Kisekae.getCaptions().getString("MenuViewReduce")))) ;
      reduce.addActionListener(this) ;
      if (!applemac) reduce.setMnemonic(KeyEvent.VK_U) ;
      if (!applemac) reduce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0)) ;
      else reduce.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,0)) ;
      m[2].add((scale = new JMenuItem(Kisekae.getCaptions().getString("MenuViewScale")))) ;
      scale.addActionListener(this) ;
      if (!applemac) scale.setMnemonic(KeyEvent.VK_E) ;
      scale.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6,0)) ;
      m[2].addSeparator() ;
      m[2].add(edit[0] = new JMenuItem(Kisekae.getCaptions().getString("MenuViewConfigFiles"))) ;
      if (!applemac) edit[0].setMnemonic(KeyEvent.VK_C) ;
      m[2].add(edit[1] = new JMenuItem(Kisekae.getCaptions().getString("MenuViewDocFiles"))) ;
      if (!applemac) edit[1].setMnemonic(KeyEvent.VK_D) ;
      m[2].add(edit[2] = new JMenuItem(Kisekae.getCaptions().getString("MenuViewPaletteFiles"))) ;
      if (!applemac) edit[2].setMnemonic(KeyEvent.VK_P) ;
      for (int i = 0 ; i < edit.length ; i++) edit[i].addActionListener(this) ;
      m[2].add((archive = new JMenuItem(Kisekae.getCaptions().getString("MenuViewArchiveFile")))) ;
      archive.addActionListener(this) ;
      if (!applemac) archive.setMnemonic(KeyEvent.VK_V) ;
      archive.setEnabled(false) ;
      m[2].addSeparator() ;
      m[2].add((cnffile = new JMenuItem(Kisekae.getCaptions().getString("MenuViewActiveCNF")))) ;
      cnffile.addActionListener(this) ;
      if (!applemac) cnffile.setMnemonic(KeyEvent.VK_A) ;
      if (!applemac) cnffile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0)) ;
      else cnffile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,0)) ;
      cnffile.setEnabled(OptionsDialog.getEditEnable() && !Kisekae.isSecure()) ;
      m[2].add((objects = new JMenuItem(Kisekae.getCaptions().getString("MenuViewActiveObjects")))) ;
      objects.addActionListener(this) ;
      if (!applemac) objects.setMnemonic(KeyEvent.VK_O) ;
      if (!applemac) objects.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0)) ;
      else objects.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0)) ;
      objects.setEnabled(OptionsDialog.getEditEnable()) ;
      m[2].add((loadtext = new JMenuItem(Kisekae.getCaptions().getString("MenuViewLoadText")))) ;
      loadtext.addActionListener(this) ;
      loadtext.setEnabled(false) ;

      // Create the Tools menu.

      m[3] = menu.toolsMenu ;
      m[3].setMargin(insets) ;
      mb.add(m[3]) ;
      m[3].add((debugger = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsFKissEditor")))) ;
      debugger.addActionListener(this) ;
      if (!applemac) debugger.setMnemonic(KeyEvent.VK_F) ;
      debugger.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, accelerator+ActionEvent.SHIFT_MASK));
      debugger.setEnabled(OptionsDialog.getEditEnable()) ;

      // Create the Options menu.

      m[4] = menu.optionsMenu ;
      m[4].setMargin(insets) ;
      mb.add(m[4]) ;
      activities = new JMenuItem(Kisekae.getCaptions().getString("MenuOptionsThreadManager")) ;
      m[4].insert(activities,2) ;
      activities.addActionListener(this) ;
      activities.setEnabled(OptionsDialog.getEditEnable()) ;

      // Create the Window menu.

      m[5] = menu.windowMenu ;
      m[5].setMargin(insets) ;
      mb.add(m[5]);

      // Create the Help menu.

      m[6] = menu.helpMenu ;
      m[6].setMargin(insets) ;
      mb.add(m[6]);

      // Set the menu item context according to the current configuration.

      Configuration config = parent.getConfig() ;
      if (config != null)
      {
         PageSet pageset = config.getPage(0) ;
         ArchiveFile zip = config.getZipFile() ;
         save.setEnabled(zip != null && zip.getDirectoryName() != null  && !Kisekae.isSecure() && !menu.getNoCopy()) ;
         archive.setEnabled(zip != null && zip.isArchive()) ;
         selectall.setEnabled(pageset != null && pageset.getGroupCount() > 0) ;
         selectallvisible.setEnabled(pageset != null && pageset.getGroupCount() > 0) ;
         selectfind.setEnabled(pageset != null && pageset.getGroupCount() > 0) ;
         importimage.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         importpalette.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         importaudio.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         importvideo.setEnabled(pageset != null && !Kisekae.isSecure() && Kisekae.isMediaInstalled()) ;
         importother.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         addcomponent.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         insertpage.setEnabled(pageset != null) ;
         deletepage.setEnabled(pageset != null) ;
         writepage.setEnabled(pageset != null && !Kisekae.isSecure()) ;
         edit[0].setEnabled(zip != null && zip.containsFileType(ArchiveFile.getConfigurationExt())) ;
         edit[1].setEnabled(zip != null && zip.containsFileType(ArchiveFile.getDocExt())) ;
         edit[2].setEnabled(zip != null && zip.containsFileType(ArchiveFile.getPaletteExt())) ;
      }

      // Validate the updates.

      parent.validate() ;
      return true;
   }


   // Method to update our menu state.  This method examines attributes
   // of our current panel frame and adjusts the menu item states
   // appropriately.

   void updateMenu()
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      PanelFrame panel = parent.getPanel() ;
      if (panel == null) return ;
      PageSet pageset = panel.getPage() ;

      // Adjust the number of page sets.

      int npage = config.getPageCount() ;
      pagemenu.setEnabled(npage > 0) ;
      if (npage != page.length)
      {
         pagemenu.removeAll() ;
         page = new JMenuItem[npage] ;
         for (int i = 0 ; i < page.length ; i++)
         {
            String s = "" + i ;
            pagemenu.add((page[i] = new JMenuItem(s))) ;
            page[i].addActionListener(this) ;
         }
      }

      // Calculate the number of color sets.  This is defined by the maximum
      // number of palette groups across all palette files.

      int n = 0 ;
      int ncolor = 0 ;
      Object cid = config.getID() ;
      while (true)
      {
         Palette p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,new Integer(n++)) ;
         if (p == null) break ;
         int multipalettes = p.getMultiPaletteCount() ;
         if (multipalettes > ncolor) ncolor = multipalettes ;
      }

      // Construct the color menu items.

      colormenu.setEnabled(ncolor > 0) ;
      if (ncolor != color.length)
      {
         colormenu.removeAll() ;
         color = new JMenuItem[ncolor] ;
         for (int i = 0 ; i < color.length ; i++)
         {
            String s = "" + i ;
            colormenu.add((color[i] = new JMenuItem(s))) ;
            color[i].addActionListener(this) ;
         }
      }

      // Determine if we have an importable image in the system clipboard.
      // This is valid on Java 1.4 only

      boolean imageinclip = false ;
      if (Kisekae.isVolatileImage() && !Kisekae.isSecure())
      {
         Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard() ;
         Transferable t = (clipboard != null) ? clipboard.getContents(clipboard) : null ;
         if (t != null) imageinclip = t.isDataFlavorSupported(DataFlavor.imageFlavor) ;
      }

      // Adjust the state of our edit menu.

      boolean b = OptionsDialog.getEditEnable() ;
      ArchiveFile zip = config.getZipFile() ;
      String directory = (zip != null) ? zip.getDirectoryName() : null ;
      save.setEnabled(directory != null && !Kisekae.isSecure() && !Kisekae.isExpired() && !menu.getNoCopy()) ;
      saveas.setEnabled(!Kisekae.isSecure() && !Kisekae.isExpired() && !menu.getNoCopy()) ;
      saveasarchive.setVisible(zip != null && zip instanceof DirFile && !Kisekae.isSecure()) ;
      saveasarchive.setEnabled(!config.isUpdated() && !Kisekae.isExpired() && !menu.getNoCopy()) ;
      saveasfiles.setVisible(zip != null && !(zip instanceof DirFile) && !Kisekae.isSecure()) ;
      saveasfiles.setEnabled(!config.isUpdated() && !config.hasIncludeFiles() && !Kisekae.isExpired() && !menu.getNoCopy()) ;
      undoall.setEnabled(b && undo.canUndo()) ;
      cut.setEnabled(b && panel.isEditOn()) ;
      copy.setEnabled(b && panel.isEditOn()) ;
      paste.setEnabled(b && panel.isClipOn()) ;
      pastenew.setEnabled(b && panel.isClipOn()) ;
      selectall.setEnabled(b && pageset != null && pageset.getGroupCount() > 0);
      selectallvisible.setEnabled(b && pageset != null && pageset.getGroupCount() > 0);
      selectfind.setEnabled(b && pageset != null && pageset.getGroupCount() > 0);
      unselectall.setEnabled(b && panel.isEditOn()) ;
      importimage.setEnabled(b && pageset != null  && !Kisekae.isSecure()) ;
      importpalette.setEnabled(b && pageset != null  && !Kisekae.isSecure()) ;
      importaudio.setEnabled(b && pageset != null  && !Kisekae.isSecure()) ;
      importvideo.setEnabled(b && pageset != null  && !Kisekae.isSecure() && Kisekae.isMediaInstalled()) ;
      importother.setEnabled(b && pageset != null  && !Kisekae.isSecure()) ;
      addcomponent.setEnabled(b && pageset != null  && !Kisekae.isSecure()) ;
      addimage.setEnabled(b && !panel.isEditOn() || panel.isUngrouped()) ;
      externalpaste.setEnabled(b && imageinclip);
      layerimage.setEnabled(b && config.getActiveCelCount() > 0) ;
      insertpage.setEnabled(b && pageset != null) ;
      deletepage.setEnabled(b && pageset != null) ;
      addpage.setEnabled(b) ;
      writepage.setEnabled(b && pageset != null && pageset.getGroupCount() > 0  && !Kisekae.isSecure()) ;
      ungroup.setEnabled(b && panel.isEditOn() && !panel.isUngrouped()) ;
      regroup.setEnabled(b && panel.isUngrouped()) ;
      newgroup.setEnabled(b && panel.isEditOn()) ;
      editimage.setEnabled(b && panel.isObjectSelected()) ;
      activities.setEnabled(b) ;
      debugger.setEnabled(b) ;
      edit[0].setEnabled(b && zip != null && zip.containsFileType(ArchiveFile.getConfigurationExt())) ;
      edit[1].setEnabled(zip != null && zip.containsFileType(ArchiveFile.getDocExt())) ;
      edit[2].setEnabled(b && zip != null && zip.containsFileType(ArchiveFile.getPaletteExt())) ;
      archive.setEnabled(b && zip != null && zip.isArchive() && !Kisekae.isSecure()) ;
      undoAction.updateUndoState() ;
      redoAction.updateRedoState() ;

      // Adjust the state of our file menu.

      menu.newkiss.setEnabled(b);
      properties.setEnabled(b);
		savenew.setEnabled(b && !Kisekae.isSecure()) ;

      // Adjust the state of our view menu.

      cnffile.setEnabled(b);
      objects.setEnabled(b);
      loadtext.setEnabled(parent.getLoadText() != null);

      // Adjust the state of our tools menu.
      
      menu.options.setEnabled(!OptionsDialog.getSecurityEnable()) ;
      menu.coloreditor.setEnabled(b) ;
      menu.imageeditor.setEnabled(b) ;
      menu.texteditor.setEnabled(b) ;
      menu.archive.setEnabled(b) ;
      
      // Adjust the state of our window menu.

      menu.tracefkiss.setEnabled(!OptionsDialog.getSecurityEnable());

      // Validate the updates.

      parent.validate() ;
   }

   // Implementation of required KissMenu method to return our current
   // FileOpen object.

   FileOpen getFileOpen() { return (menu == null) ? null : menu.getFileOpen() ; }
   
   // Implementation of the required FileOpen method to restore the menu
   // reference.  Restoration can occur if a configuration load fails.

   void setFileOpen(FileOpen f) { if (menu != null) menu.setFileOpen(f) ; }
   
   // Implementation of required KissMenu method to return our help menu.

   JMenu getHelpMenu() { return m[4] ; }

   // Implementation of the required menu item update.

   void update() { updateMenu() ; }

   // Return our undo manager.

   UndoManager getUndoManager() { return undo ; }

   // Return our undo action.

   Action getUndoAction() { return undoAction ; }

   // Return our redo action.

   Action getRedoAction() { return redoAction ; }

   // Enable or disable set restarts.  

   void setEnableRestart(boolean b) 
   { 
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { setEnableRestart(b) ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      enableRestart = b ; 
      restart.setEnabled(b) ;
   }



   // The action method is used to process control menu events.
   // This method is required as part of the ActionListener interface.

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource() ;
      parent.showStatus(null) ;

      try
      {
         // A View Reset reverts to the initial layout.

         if (reset == source)
         {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            parent.reset() ;
            return ;
         }

         // A View Restart reloads the data set.

         if (restart == source)
         {
            if (!enableRestart) return ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            parent.restart() ;
            return ;
         }

         // A magnify request scales the data set by 3/2.

         if (magnify == source) { eventMagnify(1) ; return ; }

         // A reduce request scales the data set by 2/3.

         if (reduce == source) { eventMagnify(-1) ; return ; }

         // A scale request sets the actual scale factor.

         if (scale == source)
         {
            PanelFrame pf = parent.getPanel() ;
            if (pf == null) return ;
            float sf = pf.getScaleFactor() ;
            String s = Kisekae.getCaptions().getString("ScaleText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + sf + s.substring(j1) ;
            s += "\n" + Kisekae.getCaptions().getString("ScaleText2") ;
            s = JOptionPane.showInputDialog(parent, s,
               Kisekae.getCaptions().getString("ScaleDialogTitle"),
               JOptionPane.QUESTION_MESSAGE) ;
            if (s == null) return ;
            try { sf = Float.parseFloat(s) ; }
            catch (NumberFormatException e)
            {
               parent.showStatus("NumberFormatException: Invalid scale factor.") ;
               return ;
            }
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            if (sf > 100) sf = 100 ;
            if (sf < 0.1) sf = 0.1f ;
            parent.resize(sf) ;
            return ;
         }

         // An object edit request invokes the image editor for the first
         // selection object.

         if (editimage == source)
         {
            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            PanelFrame pf = parent.getPanel() ;
            if (pf == null) return ;
            Vector v = pf.getSelection() ;
            if (v == null || v.size() == 0) return ;
            Integer mp = pf.getMultiPalette() ;
            ef = new ImageFrame(config,v.elementAt(0),mp) ;
            ef.callback.addActionListener(this) ;
            ef.show() ;
         }

         // Page requests add or delete a new page set to the current
         // configuration.  We pass the request to the main menu

         if (addpage == source) { menu.newpage.doClick() ; return ; }
         if (insertpage == source) { menu.insertpage.doClick() ; return ; }
         if (deletepage == source) { menu.deletepage.doClick() ; return ; }
         if (writepage == source) { menu.writepage.doClick() ; return ; }

         // A Select request is used to select a new configuration file
         // from a data set.  We revert to the currently loaded
         // configuration to establish the possible cnf files.
         //
         // An import request allows the selection of a new configuration
         // file from the local system.  

         if (selectcnf == source) { eventSelect() ; return ; }
         if (importcnf == source) { eventSelect(true) ; return ; }
         
         // An append request is used to display a selection of
         // CNF files found in INCLUDE archives.  These files, if
         // loaded, do not replace but extend the current CNF file.
         
         if (appendcnf == source) { eventAppend() ; return ; }

         // A Close request terminates the currently running configuration.

         if (close == source) { parent.closepanel() ; return ; }

         // A Save request updates the current archive file.

         if (save == source) { eventSave(1) ; return ; }

         // A Save As request saves the current archive file to a new file.

         if (saveas == source) { eventSave(0) ; return ; }

         // A Save As Archive request saves the current configuration set
         // as a new archive file.

         if (saveasarchive == source) { eventSave(2) ; return ; }

         // A Save As Files request saves the current configuration set
         // as a set of files in a directory.

         if (saveasfiles == source) { eventSave(3) ; return ; }

         // A Write New request writes the current configuration state to a new file.

			if (savenew == source) { eventWrite() ; return ; }

         // A Print request prints the current panel display.

         if (print == source) { eventPrint() ; return ; }

         // A Print Preview request shows a preview frame.

         if (preview == source) { eventPrintPreview() ; return ; }

         // A Page Setup request establishes the print control page format.

         if (pagesetup == source) { eventPageSetup() ; return ; }

         // A page display request initializes a new page in the
         // current configuration.

         for (int i = 0 ; i < page.length ; i++)
            if (source == page[i]) { eventPage(i) ; return ; }

         // A color display request uses a new palette for the
         // cels in the panel frame.

         for (int i = 0 ; i < color.length ; i++)
            if (source == color[i]) { eventColor(i) ; return ; }

         // Configuration files open a file edit window.

         if (source == edit[0])
         {
            String [] ext = new String[1] ;
            ext[0] = ".CNF" ;
            String title = Kisekae.getCaptions().getString("ConfigurationListTitle") ;
            eventTextEdit(title,ext,true) ;
            return ;
         }

         // Documentation files open a file edit window.

         if (source == edit[1])
         {
            String [] ext = ArchiveFile.getDocExt() ;
            String title = Kisekae.getCaptions().getString("DocumentationListTitle") ;
            eventTextEdit(title,ext,false) ;
            return ;
         }

         // Palette files open a color edit window.

         if (source == edit[2])
         {
            String [] ext = ArchiveFile.getPaletteExt() ;
            String title = Kisekae.getCaptions().getString("PaletteListTitle") ;
            eventColorEdit(title,ext) ;
            return ;
         }

         // An Objects request shows the Kisekae object context dialog.

         if (objects == source)
         {
            Configuration config = parent.getConfig() ;
            ObjectDialog od = new ObjectDialog(parent,config) ;
            od.show() ;
            return ;
         }

         // A Load Messages request shows the FileLoader text.

         if (loadtext == source)
         {
            JTextPane text = parent.getLoadText() ;
            if (text == null) return ;
            JScrollPane scroll = new JScrollPane(text) ;
            text.setPreferredSize(new Dimension(500,400)) ;
            text.setCaretPosition(0) ;
            String title = Kisekae.getCaptions().getString("FileLoaderText") ;
            JOptionPane.showMessageDialog(parent,scroll,title,JOptionPane.INFORMATION_MESSAGE) ;
            return ;
         }

         // An Active Configuration request shows the configuration file.

         if (cnffile == source)
         {
            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            
            // Watch for edits that have pasted images and updated the page. 
            
				Object cid = config.getID() ;
				Vector pages = config.getPages() ;
				for (int j = 0 ; j < pages.size() ; j++)
				{
					PageSet page = (PageSet) pages.elementAt(j) ;
					if (!page.isChanged()) continue ;
	            int n = page.updateInitialPositions(cid);
	            page.setChanged(false) ;
					if (n > 0 && OptionsDialog.getDebugControl())
						PrintLn.println("PanelMenu: Update page initial positions " + page) ;
            }
            
            // Write the current configuration to memory.
            
            byte [] configtext = new byte [0] ;
            try { configtext = config.write() ; }
            catch (IOException e)
            {
               parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               PrintLn.println("PanelMenu: view active configuration, " + e.toString()) ;
               return ;
            }
            
            config.setMemoryFile(configtext) ;
            is = config.getInputStream() ;
            tf = new TextFrame(config.getZipEntry(),is,true,true) ;
//            tf.showLineNumbers(true) ;
            tf.callback.addActionListener(this) ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            tf.setVisible(true) ;
            return ;
         }

         // An Archive request invokes the Zip Archive manager.
         // It is not possible to rename the active archive.

         if (archive == source)
         {
            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ZipManager zm = new ZipManager(config.getZipFile()) ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            zm.disableRenameDelete() ;
            zm.setVisible(true) ;
            return ;
         }

         // A debug request invokes the FKiSS Editor.

         if (debugger == source)
         {
            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) fk.reopen(config,null,null) ;
            else
            {
               fk = new FKissFrame(config) ;
               fk.setVisible(true) ;
            }
            return ;
         }

         // An update request from the text edit window has occured.
         // The configuration memory image file will be updated.

         if ("TextFrame Callback".equals(evt.getActionCommand()))
         {
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            TextObject text = (tf == null) ? null : tf.getTextObject() ;
            if (text == null) return ;
            try {	text.write(null,out,null) ; }
            catch (IOException e)
            {
               PrintLn.println("PanelMenu: I/O Exception: " + e.toString()) ;
               e.printStackTrace() ;
               return ;
            }
            finally
            {
               try { out.close() ; }
               catch (IOException e)
               {
                  PrintLn.println("PanelMenu: I/O Exception: " + e.toString()) ;
                  e.printStackTrace() ;
                  return ;
               }
            }

            // Update the configuration memory data and apply the changes.

            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            ArchiveEntry ze = text.getZipEntry() ;
            config.setMemoryFile(out.toByteArray(),ze) ;
            config.setUpdated(true) ;
            parent.setRestart(true) ;
            parent.init(config) ;
            return ;
         }

         // An update request from the palette edit window has occured.
         // The data set will be updated.

         if ("ColorFrame Callback".equals(evt.getActionCommand()))
         {
            Configuration config = parent.getConfig() ;
            if (config == null) return ;
            PanelFrame pf = parent.getPanel() ;
            if (pf == null) return ;
            PageSet page = pf.getPage() ;
            if (page == null) return ;
            pf.setBackground(config.getBorderColor()) ;
            parent.setBackground(config.getBorderColor()) ;
            pf.initcolor(page.getMultiPalette()) ;
            parent.updateToolBar() ;
            pf.showpage() ;
            return ;
         }

         // A realize request from a video cel import has occurred. Get
         // the relevant Video object and continue the import.

         if ("Video Prefetch Callback".equals(evt.getActionCommand()))
         {
            if (!(source instanceof CallbackButton)) return ;
            CallbackButton b = (CallbackButton) source ;
            Object o = b.getParentObject() ;
            if (!(o instanceof Video)) return ;
            parent.importvideo((Video) o) ;
            return ;
         }

         // A Threads request shows the state of the EventHandler and Timer
         // threads.

         if (activities == source)
         {
            new ThreadDialog(parent,parent.getConfig()).show() ;
            return ;
         }

         // An Export request shows the export page image dialog.

         if (export == source)
         {
            String title = Kisekae.getCaptions().getString("ExportDialogTitle") ;
            new ExportDialog(parent,title).show() ;
            return ;
         }

         // A Properties request shows the configuration property dialog.

         if (properties == source)
         {
            new ConfigDialog(parent,parent.getConfig()).show() ;
            return ;
         }

         // An Undo All request rolls back all edit changes.

         if (undoall == source)
         {
            if (!undo.canUndo()) return ;
            int n = JOptionPane.showConfirmDialog(parent,
               Kisekae.getCaptions().getString("UndoAllConfirmText"),
               Kisekae.getCaptions().getString("MenuEditUndoAll"),
               JOptionPane.YES_NO_OPTION) ;
            if (n != JOptionPane.YES_OPTION) return ;

            // Undo everything.  Reset to initial state.

            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            while (undo.canUndo()) undo.undo() ;
//            parent.reset(true) ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            undo.discardAllEdits() ;
            undoAction.updateUndoState() ;
            redoAction.updateRedoState() ;
            updateMenu() ;
         }

         // The following actions apply to the edit mode.  The actions
         // are passed to the parent main frame and then on to the panel
         // frame.  This allows the parent to process edit commands.

         if (source == cut) { eventCut() ; return ; }

         if (source == copy) { eventCopy() ; return ; }

         if (source == paste) { eventPaste() ; return ; }

         if (source == pastenew) { eventPasteNew() ; return ; }

         if (source == ungroup) { eventUngroup() ; return ; }

         if (source == regroup) { eventRegroup() ; return ; }

         if (source == newgroup) { eventNewgroup() ; return ; }

         if (source == selectall) { eventSelectAll(true) ; return ; }

         if (source == selectallvisible) { eventSelectAllVisible(true) ; return ; }

         if (source == unselectall) { eventSelectAll(false) ; return ; }

         if (source == selectfind) { eventSelectFind() ; return ; }

         if (source == addimage) { eventImportImage(false) ; return ; }

         if (source == addimagegroup) { eventImportImage(true) ; return ; }

         if (source == importpalette) { eventImportPalette() ; return ; }

         if (source == importaudio) { eventImportAudio() ; return ; }

         if (source == importvideo) { eventImportVideo() ; return ; }

         if (source == importother) { eventImportOther() ; return ; }

         if (source == externalpaste) { eventImportPaste() ; return ; }

         if (source == layerimage) { eventLayerImage() ; return ; }

         if (source == addbutton) { eventAddComponent(BUTTON) ; return ; }

         if (source == addtogglebutton) { eventAddComponent(TOGGLEBUTTON) ; return ; }

         if (source == addradiobutton) { eventAddComponent(RADIOBUTTON) ; return ; }

         if (source == addlabel) { eventAddComponent(LABEL) ; return ; }

         if (source == addlist) { eventAddComponent(LIST) ; return ; }

         if (source == addcombobox) { eventAddComponent(COMBOBOX) ; return ; }

         if (source == addcheckbox) { eventAddComponent(CHECKBOX) ; return ; }

         if (source == addtextbox) { eventAddComponent(TEXTBOX) ; return ; }

         if (source == addtextfield) { eventAddComponent(TEXTFIELD) ; return ; }

         if (source == addpasswordfield) { eventAddComponent(PASSWORDFIELD) ; return ; }

         if (source == addmenuitem) { eventAddComponent(MENUITEM) ; return ; }

         if (source == addtextarea) { eventAddComponent(TEXTAREA) ; return ; }

         if (source == addtextpane) { eventAddComponent(TEXTPANE) ; return ; }
      }

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.

      catch (OutOfMemoryError e)
      {
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         PrintLn.println("PanelMenu: Out of memory.") ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Watch for internal faults during action events.

      catch (Throwable e)
      {
         EventHandler.stopEventHandler() ;
         PrintLn.println("PanelMenu: Internal fault, action " + evt.getActionCommand()) ;
         e.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError") + ". " ;
         s += Kisekae.getCaptions().getString("KissSetClosed") ;
         s += "\n" + e.toString() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            e.printStackTrace(ps) ;
            os.close() ;
   			s += "\n" + "Host Operating System " + System.getProperty("os.name") ;
            s += "\n" + "Java Virtual Machine " + System.getProperty("java.version") ;
            s += "\n" + "UltraKiss build date: " + Kisekae.getBuildDate() ;
            FileReader is = new FileReader(f) ;
            LineNumberReader lr = new LineNumberReader(is) ;
            String s1 = lr.readLine() ;
            s1 = lr.readLine() ;
            int traceline = 0 ;
            while (s1 != null)
            {
               s += "\n" + s1.trim() ;
               s1 = lr.readLine() ;
               if (traceline++ > 10) break ;
            }
         }
         catch (EOFException eof) { }
         catch (Exception ex) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         parent.closeconfig() ;
      }
   }


   // Toolbar and Menu shared event action methods
   // --------------------------------------------

   void eventNew(int type) { menu.eventNew(type) ; }

   void eventOpen() { menu.eventOpen() ; }
   
   void eventClose() { parent.closepanel() ; }

   void eventPortal() { menu.eventPortal() ; }

   void eventCut() { parent.editCut() ; }

   void eventCopy() { parent.editCopy() ; }

   void eventPaste() { parent.editPaste() ; }

   void eventPasteNew() { parent.editPasteNew() ; }

   void eventUngroup() { parent.editUngroup() ; }

   void eventRegroup() { parent.editGroup() ; }

   void eventNewgroup() { parent.editNewgroup() ; }

   void eventSelectAll(boolean selectall) { parent.selectAll(selectall) ; }

   void eventSelectAllVisible(boolean selectall) { parent.selectAllVisible(selectall) ; }
   
   // Magnify or reduce the screen size.
   
   void eventMagnify(int n)
   {
      PanelFrame pf = parent.getPanel() ;
      if (pf == null) return ;
      float sf = pf.getScaleFactor() ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      float newsf = (n > 0) ? sf * 3/2 : sf * 2/3 ;
      if (newsf > 100) newsf = 100 ;
      if (newsf < 0.1) newsf = 0.1f ;
      if (Math.abs(newsf - 1.0) < 0.1) newsf = 1 ;
      parent.resize(newsf) ;
   }

   // Find an object in the configuration.

   void eventSelectFind() 
   { 
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      Vector v = new Vector() ;
      Vector v1 = config.getGroups() ;
      if (v1 != null) v.addAll(v1) ;
      v1 = config.getCels() ;
      if (v1 != null) v.addAll(v1) ;
      v1 = config.getCelGroups() ;
      if (v1 != null) v.addAll(v1) ;
      Object o = JOptionPane.showInputDialog(parent,
         Kisekae.getCaptions().getString("OptionsShowObjectSelection"),
         Kisekae.getCaptions().getString("FindMessage"),
         JOptionPane.INFORMATION_MESSAGE,
         null,
         v.toArray(),
         null) ;
      if (o == null) return ;
      v = new Vector() ;
      v.add(o) ;
      parent.selectFind(v) ; 
   }

   // Select or import a new configuration.  The no argument version shows a
   // standard EventDialog selection list for CNF files and allows for import
   // of a new CNF.  The importonly argument, if true, will only allow for a
   // new import of a CNF.

   void eventSelect() { eventSelect(false) ; }
   void eventSelect(boolean importonly)
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      FileOpen fd = config.getFileOpen() ;
      if (fd != null)
      {
         String [] ext = new String[1] ;
         ext[0] = ".CNF" ;
         fd.open() ;
         String title = Kisekae.getCaptions().getString("ConfigurationListTitle") ;
         boolean allowimport = false ;
         ArchiveEntry ze = fd.showConfig(parent,title,ext,true,null,allowimport,importonly) ;

         // If we selected an entry, update our menu fileopen object
         // to agree with the new entry that we will initialize.
         

         if (ze != null && !ze.isMemoryFile())
         {
            setFileOpen(fd) ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            parent.init() ;
         }

         // If we have a memory file, set the configuration as being from
         // new memory copy and initialize this configuration.  The set
         // will be restarted.

         else if (ze != null && ze.isMemoryFile())
         {
            fd.setZipEntry(ze) ;
            setFileOpen(fd) ;
            MemFile memfile = ze.getMemoryFile() ;
            config.setMemoryFile(memfile.getBuffer(),ze) ;
            config.setAppended(false) ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            parent.init() ;
         }
         
         else fd.close() ;
      }
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
   }

   // Appending a CNF selects a new configuration from an INCLUDE file.  This 
   // CNF expands the current CNF object and event declarations.  The original 
   // cels and groups and events are retained but the definitions from the 
   // selected CNF are added to the original CNF.  The CNF text is updated.
   //
   // This differs from adding an expansion set.  If a new expansion is added
   // to the existing configuration then the CNF is replaced with a new CNF
   // discovered in the expansion set.  If no new CNF exists in the expansion
   // set then the original CNF is used.
   
   void eventAppend() 
   { 
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      FileOpen fd = config.getFileOpen() ;
      if (fd == null) return ;
      Vector v = config.getIncludeFiles() ;
      if (v == null) return ;
            
      // If we triggered this expansion automatically on an viewer("menu","appendcnf") 
      // event or similar we may be restarting the expanded configuration.  This would 
      // be a loop.  
      
      if (config.isAppended()) 
      {
         Configuration ref = config.getReference() ;
         String s = (ref != null) ? ref.getName() : config.getName() ;
         PrintLn.println("Cycle detected, attempt to append \"" + s + "\", already done.") ;
         return ;
      }

      Vector files = new Vector() ;
      String originalpath = fd.getPath() ;
      for (int n = v.size()-1 ; n >= 0 ; n--)
      {
         Vector entries = config.searchInclude(v.elementAt(n)) ;
         if (entries != null && entries.size() > 0) 
         {
            files.addAll(entries) ;
            break ;
         }
      }
         
      // Show the identified .CNF elements for selection.
         
      String title = Kisekae.getCaptions().getString("ConfigurationListTitle") ;
      ArchiveEntry ze = fd.showConfig(parent, title, files) ;
      ArchiveFile zip = (ze != null) ? ze.getZipFile() : null ;

      if (ze == null)
      {
         PrintLn.println("No configuration selected to expand configuration \"" + config.getName() + "\"") ;
         parent.closeframe();
         return ;
      }
      
      // Our config is the original configuration.  The archive entry is the
      // configuration to append.  

      PrintLn.println("Expanding configuration \"" + config.getName() + "\" with \"" + ze.getName() + "\"") ;
      ArchiveFile preappendzip = config.getZipFile() ;
      ArchiveEntry preappendze = config.getZipEntry() ;
      String preappendlru = (preappendzip != null) ? preappendzip.getPath() : null ;
      String preappendpath = (preappendze != null) ? preappendze.getPath() : null ;
      String lruname = (preappendlru != null) ? preappendlru + File.pathSeparator : null ;
      if (parent != null) parent.setNewPreAppend(lruname,preappendpath) ;
      boolean b = config.appendInclude(ze) ;
      if (b)
      {
         setFileOpen(fd) ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.expand() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
      else
      {
         eventSelect(true) ;
      }
   }

   // Save the current data set being referenced by our main frame.
   // If the configuration has been updated it is written out without
   // adjusting initial positions, to save new pages and pasted groups.
   // If the configuration was changed initial positions are updated.

   void eventSave(int type)
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      
      // Show a warning if we are writing components as cels. This is 
      // non-recoverable as the CNF component attributes are lost when 
      // the component image is saved.
      
      if (OptionsDialog.getComponentCel() && config.getComponentCount() > 0)
      {
         String s = Kisekae.getCaptions().getString("SaveComponents") ;
         s += "\n" + Kisekae.getCaptions().getString("OptionsDialogChangedText3") ;
         int i = JOptionPane.showConfirmDialog(parent,s,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE) ;
         if (i != JOptionPane.YES_OPTION) return ;
      }
      
      // Show a warning if we are exporting cels in a different image 
      // format to ensure this is desired.  This is for a Save or Save As 
      // because a conversion to or from an archive does not convert images.
      
      if (OptionsDialog.getExportCel() && (type == 0 || type == 1))
      {
         String s = Kisekae.getCaptions().getString("ExportCelsOption") ; 
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1+1) + OptionsDialog.getExportType() + s.substring(j1) ;
         s += "\n" + Kisekae.getCaptions().getString("OptionsDialogChangedText3") ;
         int i = JOptionPane.showConfirmDialog(parent,s,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE) ;
         if (i != JOptionPane.YES_OPTION) return ;
      }

      // Save the state of the current page.

      PanelFrame panel = parent.getPanel() ;
      PageSet page = (panel == null) ? null : panel.getPage() ;
      if (page != null) page.saveState(config.getID(),"panelframe") ;

      // If the configuration has changed then update page locations.

      if (config.isChanged())
      {
         Object cid = config.getID() ;
         Vector pages = config.getPages() ;
         for (int j = 0 ; j < pages.size() ; j++)
         {
            page = (PageSet) pages.elementAt(j) ;
            if (!page.isChanged()) continue ;
            int n = page.updateInitialPositions(cid);
            page.setChanged(false) ;
            if (n > 0 && OptionsDialog.getDebugControl())
               PrintLn.println("Save: Update page initial positions " + page) ;
         }
      }

      // Retain any updated configuration in our current memory copy.
      // Note, our configuration needs to be rewritten if options have
      // changed or we are exporting components as images, as offsets
      // need to be reconstructed.

      if (config.isOptionChanged()) 
         config.setUpdated(true) ;
      if (config.getComponentCount() > 0 && OptionsDialog.getComponentCel()) 
         config.setUpdated(true) ;
      if (config.isUpdated())
      {
         try
         {
            byte [] b = config.write() ;
            config.setMemoryFile(b) ;
         }
         catch (IOException e)
         {
            PrintLn.println("PanelMenu: eventSave, " + e.getMessage()) ;
            return ;
         }
      }

      // Save all updated elements.  Make sure our zip file is closed,
      // otherwise the write can fail.

      ArchiveFile af = config.getZipFile() ;
      if (af.isOpen()) 
      {
         try { af.close() ; }
         catch (IOException e) { }       
      }
      
      if (type == 0)        // Save As request
      {
         FileSave fd = new FileSave(parent,config) ;
         fd.showall() ; 
      }
      if (type == 1)        // Save request
      {
         FileSave fd = new FileSave(parent,config) ;
         fd.saveall() ;
      }
      if (type == 2)        // Save As Archive
      {
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         ZipManager zm = new ZipManager(config) ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
      if (type == 3)        // Save As Files
      {
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         ZipManager zm = new ZipManager(config.getZipFile(),config) ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
   }

   // Write out the current configuration state.

   void eventWrite() { eventWrite(false) ; }
   void eventWrite(boolean saveset)
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      PanelFrame panel = parent.getPanel() ;
      PageSet page = (panel == null) ? null : panel.getPage() ;

      // Restore all page group positions in preparation for output.
      // Upon completion of the write all page group initial positions
      // should be set to the current positions for proper page resets.
      // We do this before the write for convenience.

      Vector pages = config.getPages() ;
      if (page != null) page.saveState(config.getID(),"panelframe") ;
      for (int i = 0 ; i < pages.size() ; i++)
      {
         page = (PageSet) pages.elementAt(i) ;
         page.restoreState(config.getID(),"panelframe") ;
         State sv = page.getState(config.getID(),"panelframe") ;
         page.setState(config.getID(),"initial",sv) ;
      }

      // Output the current configuration file.

      byte [] configtext = new byte [0] ;
      try { configtext = config.write() ; }
      catch (IOException e)
      {
         PrintLn.println("PanelMenu: eventWrite, " + e.getMessage()) ;
         return ;
      }

      // Update our configuration object for the write.

      config.setMemoryFile(configtext) ;
      config.setChanged(true) ;
      FileSave fd = new FileSave(parent,config) ;
      if (saveset) fd.showall() ; else fd.show() ;
   }

   // Print the current panel frame.

   void eventPrint()
   {
      if (parent == null) return ;
      PrinterJob pj = PrinterJob.getPrinterJob() ;
      PageFormat pf = parent.getPageFormat() ;
      PanelFrame panel = parent.getPanel() ;
      if (pf == null || panel == null) return ;
      pj.setPrintable(panel,pf) ;
      if (pj.printDialog())
      {
         panel.printSetup(pf) ;
         try { pj.print() ; }
         catch (PrinterException e)
         {
            PrintLn.println("PanelMenu: eventPrint, " + e.getMessage()) ;
         }
         finally
         {
            panel.printClose() ;
         }
      }
   }

   // Preview the print of the current panel frame.

   void eventPrintPreview()
   {
      if (parent == null) return ;
      PageFormat pf = parent.getPageFormat() ;
      PanelFrame panel = parent.getPanel() ;
      if (pf == null || panel == null) return ;
      int orientation = pf.getOrientation() ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      panel.printSetup(pf) ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      PrintPreview preview = new PrintPreview(panel,orientation) ;
   }

   // Setup the printer.

   void eventPageSetup()
   {
      PrinterJob pj = PrinterJob.getPrinterJob() ;
      parent.setPageFormat(pj.pageDialog(parent.getPageFormat())) ;
   }

   // View new page.

   void eventPage(int p)
   {
      if (EventHandler.getModal() != null) return ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      parent.initpage(p) ;
      parent.showpage() ;
   }

   // View new color.

   void eventColor(int c)
   {
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      parent.initcolor(new Integer(c)) ;
      parent.showpage() ;
   }

   // View Data Set configuration and documentation files.

   void eventTextEdit(String title, String [] ext, boolean showline)
   {
      FileOpen fileopen = null ;
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      
      if (!config.isAppended())
      {
         fileopen = config.getFileOpen() ;
      }
      
      if (config.isExpanded())
      {
         Object o = config.getExpandFiles().elementAt(0) ;
         if (o instanceof ArchiveFile) 
         {
            String s = ((ArchiveFile) o).getPath() ;
            if (s != null) s = s.replace(File.pathSeparatorChar,' ').trim() ;
            fileopen = new FileOpen(parent,s,"r") ;
         }
      }
      
      if (fileopen == null)
      {
         String preappendlru = parent.getPreAppendLru() ;
         String s = preappendlru ;
         if (s != null) s = s.replace(File.pathSeparatorChar,' ').trim() ;
         fileopen = new FileOpen(parent,s,"r") ;
      }
      
      // Search for the appropriate entries in our archive.
      // New configurations will not yet exist as files.

      fileopen.open() ;
      ArchiveEntry ze = fileopen.showConfig(parent,title,ext) ;
      if (ze == null) { fileopen.close() ; return ; }
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      tf = new TextFrame(ze) ;

      // Edit the file.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      fileopen.close() ;
      if (tf.isError()) { tf.close() ; return ; }
      tf.showLineNumbers(showline) ;
      tf.setVisible(true) ;
   }

   // View Data Set palette files.

   void eventColorEdit(String title, String [] ext)
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      FileOpen fileopen = config.getFileOpen() ;
      if (fileopen == null) return ;
      fileopen.open() ;
      ArchiveEntry ze = fileopen.showConfig(parent,title,ext) ;
      if (ze == null) { fileopen.close() ; return ; }
      String name = fileopen.getElement() ;
      if (name == null) { fileopen.close() ; return ; }
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;

      // Search for the palette object in our configuration.

      Palette palette = null ;
      Hashtable key = Palette.getKeyTable() ;
      Enumeration enum1 = key.elements() ;
      while (enum1.hasMoreElements())
      {
         Object o = enum1.nextElement() ;
         if (!(o instanceof Palette)) continue ;
         Palette p = (Palette) o ;
         if (name.equalsIgnoreCase(p.getPath()))
         {
            palette = p ;
            break ;
         }
      }

      // Show the palette edit dialog.

      fileopen.close() ;
      PanelFrame pf = parent.getPanel() ;
      PageSet page = (pf == null) ? null : pf.getPage() ;
      Integer mp = (page == null) ? new Integer(0) : page.getMultiPalette() ;
      cf = (palette == null) ? new ColorFrame(ze) : new ColorFrame(config,palette,mp) ;
      if (palette != null)	cf.callback.addActionListener(this) ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      cf.show() ;
   }

   // Insert new cel images.

   void eventImportImage(boolean newgroup)
   {
      String [] ext = ArchiveFile.getImageExt() ;
      String title = Kisekae.getCaptions().getString("ImageListTitle") ;
      FileOpen fd = new FileOpen(parent,title,ext) ;
      fd.setFileFilter("imagefiles") ;
      fd.setMultiple(true) ;
      fd.show() ;

      // Confirm the validity of all selected files.

      int n = 0 ;
      ArchiveEntry zenew = fd.getZipEntry(n) ;
      if (zenew == null) { fd.close() ; return ; }
      while (zenew != null)
      {
         zenew.setImported(true) ;
         if (!zenew.isImage())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            s += "\n" + Kisekae.getCaptions().getString("ImportFileText2") ;
            for (int i = 0 ; i < ext.length ; i++) s += ext[i] + " " ;
            JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("FileImportError"),
            JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         zenew = fd.getZipEntry(++n) ;
      }

      // Load all the new images.

      n = 0 ;
      zenew = fd.getZipEntry(n) ;
      while (zenew != null)
      {
         Cel cel = createCelPalette(fd,zenew) ;

         // Confirm that the cel loaded properly.

         if (cel == null || cel.isError())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }

         // Import the image into the selection.

         try
         {
            parent.importimage(cel,newgroup) ;
         }
         catch (Exception e)
         {
            JOptionPane.showMessageDialog(parent,
               e.getMessage(),
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         zenew = fd.getZipEntry(++n) ;
      }
      fd.close() ;
   }

   // Insert new palettes.

   void eventImportPalette()
   {
      String [] ext = ArchiveFile.getPaletteExt() ;
      String title = Kisekae.getCaptions().getString("PaletteListTitle") ;
      FileOpen fd = new FileOpen(parent,title,ext) ;
      fd.setFileFilter("palettefiles") ;
      fd.setMultiple(true) ;
      fd.show() ;

      // Confirm the validity of all selected files.

      int n = 0 ;
      ArchiveEntry zenew = fd.getZipEntry(n) ;
      if (zenew == null) { fd.close() ; return ; }
      while (zenew != null)
      {
         zenew.setImported(true) ;
         if (!zenew.isPalette())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            s += "\n" + Kisekae.getCaptions().getString("ImportFileText2") ;
            for (int i = 0 ; i < ext.length ; i++) s += ext[i] + " " ;
            JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("FileImportError"),
            JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         zenew = fd.getZipEntry(++n) ;
      }

      // Load all the new palettes.

      n = 0 ;
      zenew = fd.getZipEntry(n) ;
      while (zenew != null)
      {
         Palette palette = createPalette(fd,zenew) ;

         // Confirm that the cel loaded properly.

         if (palette == null || palette.isError())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         
         parent.importpalette(palette) ;
         zenew = fd.getZipEntry(++n) ;
      }
      fd.close() ;
   }

   // Insert audio and media files 

   void eventImportAudio()
   {
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      if (zip == null) return ;
      
      // Show the selection dialog.
      
      String [] ext = ArchiveFile.getMediaExt() ;
      String title = Kisekae.getCaptions().getString("OtherListTitle") ;
      FileOpen fd = new FileOpen(parent,title,ext) ;
      fd.setFileFilter("audiofiles") ;
      fd.setMultiple(true) ;
      fd.show() ;

      // Confirm the validity of all selected files.

      int n = 0 ;
      ArchiveEntry zenew = fd.getZipEntry(n) ;
      if (zenew == null) { fd.close() ; return ; }

      // Load all the new audio files.

      n = 0 ;
      zenew = fd.getZipEntry(n) ;
      while (zenew != null)
      {
         zenew.setImported(true) ;
         Audio audio = createAudio(fd,zenew) ;

         // Confirm that the audio file loaded properly.

         if (audio == null || audio.isError())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         
         parent.importaudio(audio) ;
         zenew = fd.getZipEntry(++n) ;
      }
      fd.close() ;
   }

   // Insert audio and media files 

   void eventImportVideo()
   {
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      if (zip == null) return ;
      
      // Show the selection dialog.
      
      String [] ext = ArchiveFile.getMediaExt() ;
      String title = Kisekae.getCaptions().getString("OtherListTitle") ;
      FileOpen fd = new FileOpen(parent,title,ext) ;
      fd.setFileFilter("videofiles") ;
      fd.setMultiple(true) ;
      fd.show() ;

      // Confirm the validity of all selected files.

      int n = 0 ;
      ArchiveEntry zenew = fd.getZipEntry(n) ;
      if (zenew == null) { fd.close() ; return ; }

      // Load all the new video files.

      n = 0 ;
      zenew = fd.getZipEntry(n) ;
      while (zenew != null)
      {
         zenew.setImported(true) ;
         Video video = createVideo(fd,zenew) ;

         // Confirm that the video file loaded properly.

         if (video == null || video.isError())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("ImportFileText1") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + name + s.substring(j1) ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         zenew = fd.getZipEntry(++n) ;
      }
      fd.close() ;
   }

   // Insert other files (text, ...).

   void eventImportOther()
   {
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      if (zip == null) return ;
      
      // Show the selection dialog.
      
      String title = Kisekae.getCaptions().getString("OtherListTitle") ;
      FileOpen fd = new FileOpen(parent,title) ;
      fd.setFileFilter("textfiles") ;
      fd.setMultiple(true) ;
      fd.show() ;
      
      importOther(fd,zip) ;
   }

   // Paste new cel images from the System clipboard.
   // This function is valid for Java 1.4 only.

   void eventImportPaste()
   {
      if (!Kisekae.isVolatileImage() || Kisekae.isSecure()) return ;
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard() ;
      if (clipboard == null) return ;
      Transferable t = clipboard.getContents(clipboard) ;
      if (t == null) return ;

      try
      {
         if (t.isDataFlavorSupported(DataFlavor.imageFlavor))
         {
            int n = config.getCelCount() ;
            String s = "import-" + (n+1) + ".cel" ;
            Cel cel = Cel.createCel(null,s) ;
            JLabel label = new JLabel() ;
            Object handler = new ImageSelection() ;
            ((TransferHandler) handler).importData(label,t) ;
            ImageIcon icon = (ImageIcon) label.getIcon() ;
            Image img = (icon != null) ? icon.getImage() : null ;
            cel.setImage(img,Palette.getDirectColorModel()) ;
            cel.setPalette(null) ;
            cel.setLoaded(true) ;
            parent.importimage(cel,true) ;
         }
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(parent,
            e.getMessage(),
            Kisekae.getCaptions().getString("PasteImportError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }
   }

   // Add a GUI component.  This constructs a JavaCel object.
   // Non-input components can be added as cels. This changes
   // the component name and ensures that the image representation
   // is written as a cal when the set is saved.

   void eventAddComponent(String type)
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      Vector v = config.getComponents() ;
      int n = JavaCel.getNextComponentNumber(type,config.getID()) ;
      String s = type + n ;
      String ext = type ;
      if (OptionsDialog.getImportComponent()) 
      {
         if ("label".equals(type)) ext = "cel" ;
         if ("button".equals(type)) ext = "cel" ;
         if ("textbox".equals(type)) ext = "cel" ;
      }
      JavaCel cel = new JavaCel(type,(s + "." + ext),config) ;
      cel.setSize(cel.getPreferredSize()) ;
      cel.setAttributes("text=\""+s+"\"") ;
      cel.setImported(true) ;
      v.addElement(cel) ;
      parent.addcomponent(cel) ;
   }

   // Adjust the cel layering order.  This constructs a drag and drop list
   // for manual adjustment of the cel order.

   void eventLayerImage()
   {
      Configuration config = parent.getConfig() ;
      if (config == null) return ;
      LayerDialog ld = new LayerDialog(parent,config) ;
      ld.show() ;
   }

   // Turn sound on and off.

   void eventSound(boolean b)
   {
      OptionsDialog.setSoundOn(b) ;
      if (!b) Audio.stop() ;
   }

   // Turn movies on and off.

   void eventMovie(boolean b)
   {
      OptionsDialog.setMovieOn(b) ;
      if (!b) Video.stop() ;
   }

   // A utility function to open a loaded file according to its context.
   // Configuration elements initialize the main frame.

   void openContext(FileOpen fd, ArchiveEntry ze)
   {
      if (menu == null) return ;
      menu.openContext(fd,ze);
   }


   // A utility function to establish a new cel and palette object given
   // an archive entry.  We return a cel object with an associated palette,
   // if the palette exists.

   private Cel createCelPalette(FileOpen fd, ArchiveEntry ze)
   {
      ArchiveEntry pe = ze ;
      Cel newcel = null ;
      Palette newpalette = null ;
      if (ze == null) return null ;
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      Object cid = (config != null) ? config.getID() : null ;

      // If we are not retaining relative path information, our
      // archive entry must be a simple element name.

      if (!OptionsDialog.getImportRelative() && zip != null && zip.isArchive())
         ze.setDirectory(null) ;

      // Load the cel object.  The load will fail if we have a
      // palette type KiSS cel.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
      if (newcel != null)
      {
         newcel.setLoader(null) ;
         newcel.load() ;
         newpalette = newcel.getPalette() ;
      }
      
      // If we are not a cel file and we should be importing images as cels,
      // construct a KissCel and copy the image data from the imported file. 
      // The imported palette is linked to the cel and renamed to be a KCF 
      // file.  Note that if this import is for a duplicate or ambiguous cel 
      // that exists in the configuration, then this new palette may not be 
      // an exact replica. Background and transparent colors may not be at 
      // palette index 0 as a KCF palette requires. The cel is corrected
      // to the proper KiSS form when it is saved.
      
      if (OptionsDialog.getImportCel())
      {
         if (newcel != null && !(".cel".equals(ze.getExtension())))
         {
            if (OptionsDialog.getDebugEdit())
               PrintLn.println("Edit: Import " + ze.getName() + " convert to Cel") ;

            // Create a cel file from the loaded KiSS image.               

            String name = newcel.getName() ;
            int n = name.lastIndexOf('.') ;
            if (n >= 0) name = name.substring(0,n) + ".cel" ;
            KissCel kisscel = new KissCel(zip,name,config) ;
            kisscel.setPalette(newpalette) ;
            kisscel.loadCopy(newcel) ;
            kisscel.setCopy(newcel.isCopy()) ;
            newcel = kisscel ;
            newcel.setUpdated(true) ;
            newcel.setImportedAsCel(true) ; 
            
            // Adjust the palette name so that the palette gets written
            // as a KCF file.
            
            if (newpalette != null)
            {
               newpalette.setInternal(false) ;
               newpalette.setUpdated(true) ;
               name = newpalette.getName() ;
               n = name.lastIndexOf('.') ;
               if (n >= 0) name = name.substring(0,n) + ".kcf" ;
               newpalette.setName(name) ;
               newpalette.setZipEntry(new DirEntry(null,name,zip)) ;
               newpalette.setImported(true) ;
            }
 
            // Adjust the archive entry name so this file gets written
            // as a CEL file.
 /*           
            name = ze.getName() ;
            n = name.lastIndexOf('.') ;
            if (n >= 0) name = name.substring(0,n) + ".cel" ;
            ze.setName(name) ;
  */
         }
      }

      // If we are a cel file then identify the associated palette.

      if (".cel".equals(ze.getExtension()))
      {
         if (newcel != null && newcel.isError())
         {
            String [] ext = ArchiveFile.getPaletteExt() ;
            String s = Kisekae.getCaptions().getString("SelectCelPalette") ;
            s += " - " + newcel.getName() ;
            pe = fd.showConfig(parent,s,ext) ;
            if (pe == null)
            {
               parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               return null ;
            }
            if (!OptionsDialog.getImportRelative() && zip != null && zip.isArchive())
               pe.setDirectory(null) ;

            // Load the palette object.  If this palette already exists in the 
            // configuration we will use the previous palette when we complete
            // the import.  If it does not exist we add it to the configuration.

            newpalette = new Palette(pe.getZipFile(),pe.getPath()) ;
            newpalette.setLoader(null) ;
            newpalette.load() ;

            // Load the cel object.

            newcel = Cel.createCel(ze.getZipFile(),ze.getPath()) ;
            newcel.setPalette(newpalette) ;
            newcel.load() ;
         }
      }

      // Set the cel and palette relative names.  These names are relative
      // to the configuration.

      if (OptionsDialog.getImportRelative())
      {
         String directory = (zip != null) ? zip.getDirectoryName() : null ;
         String celpath = (newcel != null) ? newcel.getPath() : null ;
         String palpath = (newpalette != null) ? newpalette.getPath() : null ;

         // Correct relative names.  Eliminate drive prefixes.  If we are
         // importing to a configuration and we are not relative to the
         // configuration directory then the relative name is the simple
         // element name.

         if (celpath != null)
         {
            if (directory != null && celpath.startsWith(directory))
               celpath = celpath.substring(directory.length()) ;
            else
               celpath = newcel.getName() ;
            int i = celpath.indexOf(':') ;
            if (i > 0) celpath = celpath.substring(i+1) ;
            if (celpath.startsWith(File.separator))
               celpath = celpath.substring(File.separator.length()) ;
            newcel.setRelativeName(celpath) ;
         }
         if (palpath != null)
         {
            if (directory != null && palpath.startsWith(directory))
               palpath = palpath.substring(directory.length()) ;
            else
               palpath = newpalette.getName() ;
            int i = palpath.indexOf(':') ;
            if (i > 0) palpath = palpath.substring(i+1) ;
            if (palpath.startsWith(File.separator))
               palpath = palpath.substring(File.separator.length()) ;
            newpalette.setRelativeName(palpath) ;
         }
      }
            
      // This cel will be a copy on restart if a prior entry exists in
      // the configuration.

      String name = (newcel != null) ? newcel.getRelativeName() : null ;
      if (name != null) name = name.toUpperCase() ;
  		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,name) ;
      newcel.setCopy(c != null) ;

      // Return the cel.  The cel palette attribute is set to the 
      // new palette.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      return newcel ;
   }

   
   // A utility function to establish a new palette object given
   // an archive entry.  

   private Palette createPalette(FileOpen fd, ArchiveEntry ze)
   {
      ArchiveEntry pe = ze ;
      Palette newpalette = null ;
      if (ze == null) return null ;
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      Object cid = (config != null) ? config.getID() : null ;

      // If we are not retaining relative path information, our
      // archive entry must be a simple element name.

      if (!OptionsDialog.getImportRelative() && zip != null && zip.isArchive())
         ze.setDirectory(null) ;

      // Load the palette object.

      newpalette = new Palette(pe.getZipFile(),pe.getPath()) ;
      newpalette.setIdentifier(new Integer(-1)) ;
      newpalette.setLoader(null) ;
      newpalette.load() ;

      // Set the palette relative names.  These names are relative
      // to the configuration.

      if (OptionsDialog.getImportRelative())
      {
         String directory = (zip != null) ? zip.getDirectoryName() : null ;
         String palpath = (newpalette != null) ? newpalette.getPath() : null ;

         // Correct relative names.  Eliminate drive prefixes.  If we are
         // importing to a configuration and we are not relative to the
         // configuration directory then the relative name is the simple
         // element name.

         if (palpath != null)
         {
            if (directory != null && palpath.startsWith(directory))
               palpath = palpath.substring(directory.length()) ;
            else
               palpath = newpalette.getName() ;
            int i = palpath.indexOf(':') ;
            if (i > 0) palpath = palpath.substring(i+1) ;
            if (palpath.startsWith(File.separator))
               palpath = palpath.substring(File.separator.length()) ;
            newpalette.setRelativeName(palpath) ;
         }
      }

      // Return the palette.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      return newpalette ;
   }

   
   // A utility function to establish a new audio object given
   // an archive entry.  

   private Audio createAudio(FileOpen fd, ArchiveEntry ze)
   {
      ArchiveEntry pe = ze ;
      Audio newaudio = null ;
      if (ze == null) return null ;
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      Object cid = (config != null) ? config.getID() : null ;

      // If we are not retaining relative path information, our
      // archive entry must be a simple element name.

      if (!OptionsDialog.getImportRelative() && zip != null && zip.isArchive())
         ze.setDirectory(null) ;

      // Load the audio object.

      if (ze.isAudioSound()) 
      {
         if (Kisekae.isWebswing())
            newaudio = new AudioWebswing(pe.getZipFile(),pe.getPath()) ;
         else
            newaudio = new AudioSound(pe.getZipFile(),pe.getPath()) ;
      }
      if (ze.isAudioMedia()) 
         newaudio = new AudioMedia(pe.getZipFile(),pe.getPath()) ;
      if (newaudio != null)
      {
         newaudio.setIdentifier(new Integer(-1)) ;
         newaudio.setLoader(null) ;
         newaudio.load() ;
      }

      // Set the audio object relative names.  These names are relative
      // to the configuration.

      if (OptionsDialog.getImportRelative())
      {
         String directory = (zip != null) ? zip.getDirectoryName() : null ;
         String audiopath = (newaudio != null) ? newaudio.getPath() : null ;

         // Correct relative names.  Eliminate drive prefixes.  If we are
         // importing to a configuration and we are not relative to the
         // configuration directory then the relative name is the simple
         // element name.

         if (audiopath != null)
         {
            if (directory != null && audiopath.startsWith(directory))
               audiopath = audiopath.substring(directory.length()) ;
            else
               audiopath = newaudio.getName() ;
            int i = audiopath.indexOf(':') ;
            if (i > 0) audiopath = audiopath.substring(i+1) ;
            if (audiopath.startsWith(File.separator))
               audiopath = audiopath.substring(File.separator.length()) ;
            newaudio.setRelativeName(audiopath) ;
         }
      }

      // Return the audio object.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      return newaudio ;
   }

   
   // A utility function to establish a new video object given
   // an archive entry.  

   private Video createVideo(FileOpen fd, ArchiveEntry ze)
   {
      ArchiveEntry pe = ze ;
      Video newvideo = null ;
      if (ze == null) return null ;
      Configuration config = parent.getConfig() ;
      ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
      Object cid = (config != null) ? config.getID() : null ;

      // If we are not retaining relative path information, our
      // archive entry must be a simple element name.

      if (!OptionsDialog.getImportRelative() && zip != null && zip.isArchive())
         ze.setDirectory(null) ;

      // Load the video object. A window is created and a callback action
      // occurs when the video is is intialized (prefetched).

      if (ze.isVideo()) newvideo = new Video(pe.getZipFile(),pe.getPath(),config) ;
      if (newvideo != null)
      {
         newvideo.addPrefetchCallbackListener(this) ;
         newvideo.setIdentifier(new Integer(-1)) ;
         newvideo.setShowControls(true) ;
         newvideo.showComponent(true) ;
         newvideo.setActivated(true) ;
         newvideo.setLoader(null) ;
         newvideo.load() ;
         newvideo.init() ;
      }

      // Set the video object relative names.  These names are relative
      // to the configuration.

      if (OptionsDialog.getImportRelative())
      {
         String directory = (zip != null) ? zip.getDirectoryName() : null ;
         String videopath = (newvideo != null) ? newvideo.getPath() : null ;

         // Correct relative names.  Eliminate drive prefixes.  If we are
         // importing to a configuration and we are not relative to the
         // configuration directory then the relative name is the simple
         // element name.

         if (videopath != null)
         {
            if (directory != null && videopath.startsWith(directory))
               videopath = videopath.substring(directory.length()) ;
            else
               videopath = newvideo.getName() ;
            int i = videopath.indexOf(':') ;
            if (i > 0) videopath = videopath.substring(i+1) ;
            if (videopath.startsWith(File.separator))
               videopath = videopath.substring(File.separator.length()) ;
            newvideo.setRelativeName(videopath) ;
         }
      }
            
      // This cel will be a copy on restart if a prior entry exists in
      // the configuration.

      String name = (newvideo != null) ? newvideo.getRelativeName() : null ;
      if (name != null) name = name.toUpperCase() ;
  		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,name) ;
      newvideo.setCopy(c != null) ;

      // Return the video object.

      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      return newvideo ;
   }
   
   
   // A utility function to confirm the validity of all selected arbitraty 
   // import files.  We construct a contents vector to copy all the new files 
   // selected in our file open descriptor to our configuration archive.

   private void importOther(FileOpen fd, ArchiveFile zip)
   {
      int n = 0 ;
      if (zip == null) return ;
      ArchiveEntry zenew = fd.getZipEntry(n) ;
      if (zenew == null) { fd.close() ; return ; }
      
      // If our destination is an archive file then the contents vector
      // must be preset to the current archive contents.
      
      Vector contents = new Vector() ;
      if (zip.isArchive()) 
      {
         try
         {
            if (!zip.isOpen()) zip.open() ;
            contents.addAll(zip.getContents()) ;
         }
         catch (IOException e) { }
      }
      
      // Verify that we are not trying to import an archive file.
      // Add new imported archive entries to our contents vector.
      
      while (zenew != null)
      {
         ArchiveFile zezip = zenew.getZipFile() ;
         if (zezip.isArchive())
         {
            String name = zenew.getName() ;
            String s = Kisekae.getCaptions().getString("SaveAsArchiveText") ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("FileImportError"),
               JOptionPane.ERROR_MESSAGE) ;
            fd.close() ;
            return ;
         }
         zenew.setImported(true) ;
         contents.add(zenew) ;
         zenew = fd.getZipEntry(++n) ;
      }

      // Perform the import. 
      
      parent.importother(contents,zip) ;
      fd.close() ;
   }


   // A function to update our menu state.
   // Determine if we have an importable image in the system clipboard.

   void updateRunState()
   {
      try
      {
         boolean imageinclip = false ;
         if (Kisekae.isVolatileImage() && !Kisekae.isSecure())
         {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard() ;
            Transferable t = (clipboard != null) ? clipboard.getContents(clipboard) : null ;
            if (t != null) imageinclip = t.isDataFlavorSupported(DataFlavor.imageFlavor) ;
         }
         externalpaste.setEnabled(imageinclip && OptionsDialog.getEditEnable()) ;
      }
      catch (Exception e)
      {
         PrintLn.printErr(e.toString()) ;
      }
      
      if (menu != null) menu.updateRunState() ;
   }


   // Inner class to support the undo operation.

   class UndoAction extends AbstractAction
   {
      public UndoAction()
      {
         super(Kisekae.getCaptions().getString("MenuEditUndo")) ;
         putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())) ;
         setEnabled(false) ;
      }

      public void actionPerformed(ActionEvent e)
      {
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         try { undo.undo(); }
         catch (CannotUndoException ex)
         {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            PrintLn.println("PanelMenu: Unable to undo edit") ;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("UndoErrorText") + "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
         }

         // Update the undo state.

         updateUndoState() ;
         redoAction.updateRedoState() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      protected void updateUndoState()
      {
         ToolBar toolbar = parent.getToolBar() ;
         if (undo.canUndo())
         {
            setEnabled(true) ;
            if (toolbar != null) toolbar.undo.setEnabled(true) ;
            putValue(Action.NAME, undo.getUndoPresentationName()) ;
         }
         else
         {
            setEnabled(false) ;
            if (toolbar != null) toolbar.undo.setEnabled(false) ;
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
         putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
         setEnabled(false) ;
      }

      public void actionPerformed(ActionEvent e)
      {
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         try { undo.redo() ; }
         catch (CannotRedoException ex)
         {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            PrintLn.println("PanelMenu: Unable to redo edit") ;
            ex.printStackTrace() ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("UndoErrorText") + "\n" + ex.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
         }

         // Update the redo state.

         updateRedoState() ;
         undoAction.updateUndoState() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      protected void updateRedoState()
      {
         ToolBar toolbar = parent.getToolBar() ;
         if (undo.canRedo())
         {
            setEnabled(true) ;
            if (toolbar != null) toolbar.redo.setEnabled(true) ;
            putValue(Action.NAME, undo.getRedoPresentationName()) ;
         }
         else
         {
            setEnabled(false) ;
            if (toolbar != null) toolbar.redo.setEnabled(false) ;
            putValue(Action.NAME, Kisekae.getCaptions().getString("MenuEditRedo")) ;
         }
      }
   }
}
