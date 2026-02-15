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
* MainMenu class
*
* Purpose:
*
* This class encapsulates the menu bar for the main frame.
*
*/

import java.awt.* ;
import java.awt.event.* ; 
import java.io.* ;
import java.net.URL ;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Vector ;
import java.net.MalformedURLException ;
import java.applet.AppletContext ;
import javax.swing.* ;
import javax.swing.border.*;
import javax.jnlp.* ;


final public class MainMenu extends KissMenu
   implements ActionListener, ItemListener
{
   private FileOpen fd = null ;							// Our file open list
   private UrlLoader urlloader = null ;	 			// Our active url load
   private URL downloadurl = null ;                // The original download
   private WebFrame webframe = null ;              // Our active portal
   private boolean nocopy = false ;                // Our URL nocopy indicator
   private String webURL = null ;                  // Initial URL for portal
   private String openpath = null ;                // FileOpen path on open
   private static boolean filenew = false ;        // True if File-New 

   private static final int NEWFILE = 0 ;
   private static final int NEWPAGE = 1 ;
   private static final int NEWCONFIG = 2 ;
   private static final int INSERTPAGE = 3 ;
   private static final int DELETEPAGE = 4 ;
   private static final int WRITEPAGE = 5 ;

   // HelpSet declarations

   private static String helpset = "Help/Product.hs" ;
   private static String helpsection = "kisekae.index" ;
	private static String onlinehelp = "index.html" ;
	private static String tutorialset = "Help/Tutorials.hs" ;
	private static String tutorialsection = "tutorials.index" ;
	private static String refset = "Help/Reference2.hs" ;
	private static String refsection = "reference2.index" ;
   private static String portal = "HTML/WebAccess.html" ;
   private static String urlbug = "issues" ;
   private static String urltutorialfiles = "releases" ;
   private static HelpLoader helper = null ;
	private static HelpLoader helper2 = null ;
	private static HelpLoader helper3 = null ;
   protected static JMenuItem help = null ;
   protected static JMenuItem tutorial = null ;
   protected static JMenuItem tutorialfiles = null ;
   protected static JMenuItem refhelp = null ;
   private static Point windowlocation = null ;
   private static Dimension windowsize = null ;

   // MenuBar definitions

   protected JMenu fileMenu = null ;
   protected JMenu viewMenu = null ;
   protected JMenu toolsMenu = null ;
   protected JMenu optionsMenu = null ;
   protected JMenu windowMenu = null ;
   protected JMenu helpMenu = null ;
   protected JMenu toolbarMenu = null ;
   protected Insets insets = null ;

   // Menu item declarations

   protected JCheckBoxMenuItem toolbar = null ;
   protected JCheckBoxMenuItem statusbar = null ;
   protected JCheckBoxMenuItem fitscreen = null ;
   protected JCheckBoxMenuItem fitpanel = null ;
   protected JCheckBoxMenuItem showborder = null ;
   protected JCheckBoxMenuItem tracefkiss = null ;
   protected JCheckBoxMenuItem viewastext = null ;
   protected JCheckBoxMenuItem viewtbcompat = null ;
   protected JCheckBoxMenuItem viewtbtools = null ;
   protected JCheckBoxMenuItem viewtbedits = null ;
   protected JCheckBoxMenuItem viewtbpages = null ;
   protected JCheckBoxMenuItem viewtbcolors = null ;
   protected JMenuItem newkiss = null ;
   protected JMenuItem newpage = null ;
   protected JMenuItem insertpage = null ;
   protected JMenuItem deletepage = null ;
   protected JMenuItem writepage = null ;
   protected JMenuItem open = null ;
   protected JMenuItem expand = null ;
   protected JMenuItem openweb = null ;
   protected JMenuItem openportal = null ;
   protected JMenuItem openurl = null ;
   protected JMenuItem websearch = null ;
   protected JMenuItem readme = null ;
   protected JMenuItem exit = null ;
   protected JMenuItem register = null ;
   protected JMenuItem showtips = null ;
   protected JMenuItem rundemo = null ;
   protected JMenuItem bugreport = null ;
   protected JMenuItem clearcache = null ;
   protected JMenuItem about = null ;
   protected JMenuItem options = null ;
   protected JMenuItem memory = null ;
   protected JMenuItem archive = null ;
   protected JMenuItem texteditor = null ;
   protected JMenuItem coloreditor = null ;
   protected JMenuItem imageeditor = null ;
   protected JMenuItem mediaplayer = null ;
   protected JMenuItem logfile = null ;
   protected JMenuItem [] lru = null ;


   // Constructor

   public MainMenu (MainFrame frame)
   {
      parent = frame ;
      mb = new JMenuBar() ;
      fileMenu = new JMenu(Kisekae.getCaptions().getString("MenuFile")) ;
      if (!OptionsDialog.getAppleMac()) fileMenu.setMnemonic(KeyEvent.VK_F) ;
      mb.add(fileMenu) ;
      String s = System.getProperty("java.version") ;
      int rm = (s.indexOf("1.2") == 0) ? 2 : 26 ;
      insets = new Insets(2,2,2,rm) ;
      fileMenu.setMargin(insets) ;
      lru = new JMenuItem [OptionsDialog.getMaxLruFiles()] ;
      createMenu() ;
   }

   // Initialization.

   boolean createMenu()
   {
      boolean priorfitscreen = false ;
      boolean priorfitpanel = false ;
      boolean priorviewastext = false ;
      boolean priorshowborder = false ;
      boolean priortracefkiss = false ;
      boolean applemac = OptionsDialog.getAppleMac() ;
      
      // Update the existing menubar.  We remove all menu items
      // except the first, then rebuild the menubar from scratch.
      // We take this approach to stop the menubar from disappearing
      // from view as would happen if we substituted a new menubar
      // object in the parent frame.

      mb = getMenuBar() ;
      if (mb == null) return false ;
      for (int i = mb.getMenuCount() ; i > 1 ; i--) mb.remove(i-1) ;
      fileMenu = mb.getMenu(0) ;
      if (fileMenu.getItemCount() > 0) fileMenu.removeAll() ;

      // Find the HelpSet file and create the HelpSet broker.

      help = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpContents")) ;
      help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0)) ;
      if (!applemac) help.setMnemonic(KeyEvent.VK_C) ;
      help.setEnabled(false) ;
      tutorial = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpTutorials")) ;
      tutorial.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,accelerator+ActionEvent.SHIFT_MASK)) ;
      if (!applemac) tutorial.setMnemonic(KeyEvent.VK_T) ;
      tutorial.setEnabled(false) ;
		refhelp = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpFKiss")) ;
      if (!applemac) refhelp.setMnemonic(KeyEvent.VK_F) ;
      refhelp.setEnabled(false) ;
      if (Kisekae.isHelpInstalled())
      {
         helper3 = new HelpLoader(parent,refset,refsection) ;
         helper3.addActionListener(refhelp) ;
         refhelp.setEnabled(helper3.isLoaded()) ;
      	helper2 = new HelpLoader(parent,tutorialset,tutorialsection) ;
         helper2.setActionListener(this) ;
         tutorial.setEnabled(helper2.isLoaded()) ;
         tutorial.addActionListener(this) ;
         helper = new HelpLoader(parent,helpset,helpsection) ;
         if (helper.isLoaded()) helper.addActionListener(help) ;
         help.setEnabled(helper.isLoaded()) ;
      }
       if (!Kisekae.isHelpInstalled() || helper == null || !helper.isLoaded())
      {
         help.addActionListener(this) ;
      }
      
      // Establish the last recently used file history.
      
      Vector lrufiles = OptionsDialog.getLruFiles() ;
      if (lrufiles != null)
      {
         for (int i = 0 ; i < lru.length ; i++) lru[i] = null ;
         int maxlrufiles = OptionsDialog.getMaxLruFiles() ;
         int n = Math.min(lrufiles.size(),maxlrufiles) ;
         for (int i = 0 ; i < n ; i++)
         {
            String s = (String) lrufiles.elementAt(i) ;
            int m = s.indexOf(File.pathSeparatorChar) ;
            String path = (m < 0) ? "" : s.substring(0,m) ;
            String cnf = (m < 0) ? s : s.substring(m+1) ;
            File f1 = new File(path) ;
            File f2 = new File(cnf) ;
            String s1 = f1.getName() + " " + f2.getName() ;
            if (path.endsWith(File.separator)) s1 = f2.getName() ;
            while (s1.startsWith(" ")) s1 = s1.substring(1) ;
            LruMenuItem lruitem = new LruMenuItem(s1) ;
            lruitem.setLruName(s) ;
            lruitem.addActionListener(this) ;
            if (i < lru.length) lru[i] = lruitem ;
         }
      }

      // Create the File menu.

      fileMenu.add((newkiss = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNew")))) ;
      newkiss.addActionListener(this) ;
      if (!applemac) newkiss.setMnemonic(KeyEvent.VK_N) ;
      newkiss.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelerator));
      newkiss.setEnabled(OptionsDialog.getEditEnable());
      newpage = new JMenuItem(Kisekae.getCaptions().getString("MenuFileNewPage")) ;
      if (!applemac) newpage.setMnemonic(KeyEvent.VK_P) ;
      newpage.addActionListener(this) ;
      newpage.setEnabled(OptionsDialog.getEditEnable());
      insertpage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditInsertPage")) ;
      insertpage.addActionListener(this) ;
      insertpage.setEnabled(OptionsDialog.getEditEnable());
      deletepage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditDeletePage")) ;
      deletepage.addActionListener(this) ;
      deletepage.setEnabled(OptionsDialog.getEditEnable());
      writepage = new JMenuItem(Kisekae.getCaptions().getString("MenuEditWritePage")) ;
      writepage.addActionListener(this) ;
      writepage.setEnabled(OptionsDialog.getEditEnable() && !Kisekae.isSecure());
      fileMenu.add((open = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpen")))) ;
      open.addActionListener(this) ;
      if (!applemac) open.setMnemonic(KeyEvent.VK_O) ;
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelerator));
      expand = new JMenuItem(Kisekae.getCaptions().getString("MenuFileAdd")) ;
      expand.addActionListener(this) ;
      fileMenu.add((websearch = new JMenuItem(Kisekae.getCaptions().getString("MenuFileWebSearch")))) ;
      websearch.addActionListener(this) ;
      websearch.setEnabled(!Kisekae.isSecure() && Kisekae.isSearchInstalled() && !Kisekae.isExpired() && !Kisekae.isWebsocket());
      websearch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelerator));
      if (!applemac) websearch.setMnemonic(KeyEvent.VK_S) ;
      fileMenu.addSeparator() ;
      fileMenu.add((openurl = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpenURL")))) ;
      openurl.addActionListener(this) ;
      openurl.setEnabled(!Kisekae.isSecure()) ;
      openurl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, accelerator+ActionEvent.SHIFT_MASK));
      if (!applemac) openurl.setMnemonic(KeyEvent.VK_R) ;
      fileMenu.add((openweb = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpenWeb")))) ;
      openweb.addActionListener(this) ;
      openweb.setEnabled(!Kisekae.isSecure());
      openweb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, accelerator+ActionEvent.SHIFT_MASK));
      if (!applemac) openweb.setMnemonic(KeyEvent.VK_B) ;
      fileMenu.add((openportal = new JMenuItem(Kisekae.getCaptions().getString("MenuFileOpenPortal")))) ;
      openportal.addActionListener(this) ;
      openportal.setEnabled(true);
      openportal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, accelerator+ActionEvent.SHIFT_MASK));
      if (!applemac) openportal.setMnemonic(KeyEvent.VK_P) ;
      fileMenu.addSeparator() ;
      String mfe = (OptionsDialog.getAppleMac()) ? "MenuFileQuit" : "MenuFileExit" ;
		fileMenu.add((exit = new JMenuItem(Kisekae.getCaptions().getString(mfe)))) ;
      exit.addActionListener(this) ;
      if (!applemac) exit.setMnemonic(KeyEvent.VK_X) ;
      if (lru.length > 0 && lru[0] != null) fileMenu.addSeparator() ;
      for (int i = 0 ; i < lru.length ; i++) 
         if (lru[i] != null) fileMenu.add(lru[i]) ;

      // Create the View menu.

      viewMenu = new JMenu(Kisekae.getCaptions().getString("MenuView")) ;
      viewMenu.setMargin(insets) ;
      if (!applemac) viewMenu.setMnemonic(KeyEvent.VK_V) ;
      mb.add(viewMenu) ;
      viewMenu.add((toolbar = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewToolbar")))) ;
      toolbar.setState(ToolBar.toolBarOn) ;
      toolbar.addItemListener(this) ;
      toolbar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0)) ;
      if (!applemac) toolbar.setMnemonic(KeyEvent.VK_T) ;
      viewMenu.add((statusbar = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewStatusBar")))) ;
      StatusBar sb = parent.getStatusBar() ;
      if (sb != null) statusbar.setState(sb.getState()) ;
      statusbar.addItemListener(this) ;
      statusbar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0)) ;
      if (!applemac) statusbar.setMnemonic(KeyEvent.VK_S) ;
      priorviewastext = (viewastext != null) ? viewastext.getState() : false ;
      viewMenu.add((viewastext = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewAsText")))) ;
      viewastext.setState(priorviewastext) ;
      viewastext.setEnabled(!Kisekae.isSecure()) ;

      // Create the Tools menu

      toolsMenu = new JMenu(Kisekae.getCaptions().getString("MenuTools")) ;
      toolsMenu.setMargin(insets) ;
      if (!applemac) toolsMenu.setMnemonic(KeyEvent.VK_T) ;
      mb.add(toolsMenu) ;
      toolsMenu.add((texteditor = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsTextEditor")))) ;
      texteditor.addActionListener(this) ;
      texteditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, accelerator+ActionEvent.SHIFT_MASK));
      texteditor.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) texteditor.setMnemonic(KeyEvent.VK_T) ;
      toolsMenu.add((coloreditor = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsColorEditor")))) ;
      coloreditor.addActionListener(this) ;
      coloreditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelerator+ActionEvent.SHIFT_MASK));
      coloreditor.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) coloreditor.setMnemonic(KeyEvent.VK_C) ;
      toolsMenu.add((imageeditor = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsImageEditor")))) ;
      imageeditor.addActionListener(this) ;
      imageeditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, accelerator+ActionEvent.SHIFT_MASK));
      imageeditor.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) imageeditor.setMnemonic(KeyEvent.VK_I) ;
      toolsMenu.add((archive = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsArchiveManager")))) ;
      archive.addActionListener(this) ;
      archive.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, accelerator+ActionEvent.SHIFT_MASK));
      archive.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) archive.setMnemonic(KeyEvent.VK_Z) ;
      toolsMenu.add((mediaplayer = new JMenuItem(Kisekae.getCaptions().getString("MenuToolsMediaPlayer")))) ;
      mediaplayer.addActionListener(this) ;
      mediaplayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, accelerator+ActionEvent.SHIFT_MASK));
      mediaplayer.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) mediaplayer.setMnemonic(KeyEvent.VK_M) ;
      mediaplayer.setVisible(true) ;

      // Create the Options menu

      optionsMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptions")) ;
      optionsMenu.setMargin(insets) ;
      if (!applemac) optionsMenu.setMnemonic(KeyEvent.VK_O) ;
      mb.add(optionsMenu) ;
      optionsMenu.add((options = new JMenuItem(Kisekae.getCaptions().getString("MenuOptionsOptions")))) ;
      options.addActionListener(this) ;
      options.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelerator+ActionEvent.SHIFT_MASK));
      options.setEnabled(!Kisekae.isSecure()) ;
      if (!applemac) options.setMnemonic(KeyEvent.VK_O) ;
      optionsMenu.add((memory = new JMenuItem(Kisekae.getCaptions().getString("MenuOptionsMemory")))) ;
      memory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, accelerator));
      if (!applemac) memory.setMnemonic(KeyEvent.VK_M) ;
      memory.addActionListener(this) ;
      toolbarMenu = new JMenu(Kisekae.getCaptions().getString("MenuOptionsToolbarOptions")) ;
      optionsMenu.add(toolbarMenu) ;
      toolbarMenu.add((viewtbtools = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsToolbarTools")))) ;
      viewtbtools.setSelected(OptionsDialog.getTbTools()) ;
      viewtbtools.addItemListener(this) ;
      toolbarMenu.add((viewtbedits = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsToolbarEdits")))) ;
      viewtbedits.setSelected(OptionsDialog.getTbEdits()) ;
      viewtbedits.addItemListener(this) ;
      toolbarMenu.add((viewtbpages = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsToolbarPages")))) ;
      viewtbpages.setSelected(OptionsDialog.getTbPages()) ;
      viewtbpages.addItemListener(this) ;
      toolbarMenu.add((viewtbcolors = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsToolbarColors")))) ;
      viewtbcolors.setSelected(OptionsDialog.getTbColors()) ;
      viewtbcolors.addItemListener(this) ;
      toolbarMenu.add((viewtbcompat = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuOptionsToolbarCompatibility")))) ;
      viewtbcompat.setSelected(OptionsDialog.getTbCompat()) ;
      viewtbcompat.addItemListener(this) ;

      // Create the Window menu

      int apple = (OptionsDialog.getAppleMac()) ? ActionEvent.SHIFT_MASK : 0 ;
      windowMenu = new JMenu(Kisekae.getCaptions().getString("MenuWindow")) ;
      windowMenu.setMargin(insets) ;
      if (!applemac) windowMenu.setMnemonic(KeyEvent.VK_W) ;
      mb.add(windowMenu) ;
      priorfitpanel = (fitpanel != null) ? fitpanel.getState() : false ;
      windowMenu.add((fitpanel = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewSizeToFit")))) ;
      fitpanel.setState(priorfitpanel) ;
      fitpanel.addItemListener(this) ;
      if (!applemac) fitpanel.setMnemonic(KeyEvent.VK_F) ;
      fitpanel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,apple)) ;
      priorfitscreen = (fitscreen != null) ? fitscreen.getState() : false ;
      windowMenu.add((fitscreen = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewScaleToFit")))) ;
      fitscreen.setState(priorfitscreen) ;
      fitscreen.addItemListener(this) ;
      if (!applemac) fitscreen.setMnemonic(KeyEvent.VK_S) ;
      fitscreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10,apple)) ;
      fitscreen.setVisible(true) ;
      priorshowborder = (showborder != null) ? showborder.getState() : false ;
      windowMenu.add((showborder = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewShowBorder")))) ;
      showborder.setState(priorshowborder) ;
      showborder.addItemListener(this) ;
      if (!applemac) showborder.setMnemonic(KeyEvent.VK_B) ;
      priortracefkiss = (tracefkiss != null) ? tracefkiss.getState() : false ;
      windowMenu.add((tracefkiss = new JCheckBoxMenuItem(Kisekae.getCaptions().getString("MenuViewTraceFKiss")))) ;
      tracefkiss.setState(priortracefkiss) ;
      tracefkiss.setEnabled(!OptionsDialog.getSecurityEnable());
      tracefkiss.addItemListener(this) ;
      if (!applemac) tracefkiss.setMnemonic(KeyEvent.VK_T) ;

      // Create the Help menu

      helpMenu = new JMenu(Kisekae.getCaptions().getString("MenuHelp")) ;
      helpMenu.setMargin(insets) ;
      if (!applemac) helpMenu.setMnemonic(KeyEvent.VK_H) ;
      mb.add(helpMenu) ;
      helpMenu.add(help) ;
//    helpMenu.add(refhelp) ;
      helpMenu.add((readme = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpReadMe")))) ;
      if (!applemac) readme.setMnemonic(KeyEvent.VK_M) ;
      readme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.SHIFT_MASK));
      readme.addActionListener(this);
      readme.setVisible(Kisekae.getReadMeIndex() != null);
      helpMenu.addSeparator() ;
      helpMenu.add((showtips = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpTips")))) ;
      if (!applemac) showtips.setMnemonic(KeyEvent.VK_S) ;
      showtips.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.SHIFT_MASK));
      showtips.addActionListener(this);
      showtips.setEnabled(Kisekae.isTipsInstalled());
      helpMenu.add((rundemo = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpDemo")))) ;
      if (!applemac) rundemo.setMnemonic(KeyEvent.VK_D) ;
      rundemo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, accelerator+ActionEvent.SHIFT_MASK));
      rundemo.addActionListener(this);
      rundemo.setEnabled(Kisekae.getDemoIndex() != null) ;
      helpMenu.add(tutorial) ;
      helpMenu.add((tutorialfiles = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpTutorialFiles")))) ;
      tutorialfiles.addActionListener(this);
      tutorialfiles.setEnabled(tutorial.isEnabled());
      helpMenu.add((logfile = new JMenuItem(Kisekae.getCaptions().getString("MenuViewLogFile")))) ;
      logfile.setEnabled(LogFile.isOpen()) ;
      logfile.addActionListener(this) ;
      if (!applemac) logfile.setMnemonic(KeyEvent.VK_L) ;
      logfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, accelerator+ActionEvent.SHIFT_MASK));
      helpMenu.addSeparator() ;
//    helpMenu.add((register = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpRegister")))) ;
//    if (!applemac) register.setMnemonic(KeyEvent.VK_R) ;
//    register.addActionListener(this);
//    register.setEnabled(!Kisekae.isSecure());
//    helpMenu.addSeparator() ;
      helpMenu.add((bugreport = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpBugReport")))) ;
      bugreport.addActionListener(this);
      bugreport.setEnabled(!Kisekae.isSecure());
      helpMenu.add((clearcache = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpClearCache")))) ;
      clearcache.addActionListener(this);
      clearcache.setEnabled(!Kisekae.isSecure());
      helpMenu.add((about = new JMenuItem(Kisekae.getCaptions().getString("MenuHelpAbout")))) ;
      about.addActionListener(this);
      if (!applemac) about.setMnemonic(KeyEvent.VK_A) ;

      parent.validate() ;
      return true;
   }


   // Implementation of the required KissMenu abstract method.  The FileOpen
   // object contains the list of CNF file entries in the compressed file.
   // The FileOpen dialog is retained when the configuration is loaded so
   // that it can be referenced if a new configuration is selected from the
   // file.

   FileOpen getFileOpen() { return fd ; }

   // Implementation of the required KissMenu abstract method to restore the
   // fileopen reference.  Restoration must occur if a configuration load fails.

   void setFileOpen(FileOpen f) { fd = f ; }

   // Implementation of a method to establish the active UrlLoader object
   // for load complete callbacks.

   void setUrlLoader(UrlLoader loader) { urlloader = loader ; }
   
   // Set the original download URL (not the cache file)
   // the menu Save and Save As menu items.
   
   void setDownloadURL(URL url) { downloadurl = url ; }
   
   // Retain the nocopy indicator from a URL load. This is used to disable
   // the menu Save and Save As menu items.
   
   void setNoCopy(boolean b) { nocopy = b ; }
   
   // Return the nocopy indicator from a URL load. 

   boolean getNoCopy() { return nocopy ; }

   // Implementation of required KissMenu abstract method.

   JMenu getHelpMenu() { return helpMenu ; }
   
   // Set the initial URL for a launch of browser or portal.
   
   void setWebURL(String s) { webURL = s ; }
   
   // Get our active portal reference.
   
   WebFrame getWebFrame() { return webframe ; }
   
   // Set our active portal reference.
   
   void setWebFrame(WebFrame wf) { webframe = wf ; }
   
   // Get our last Open event FileOpen path.
   
   String getOpenPath() { return openpath ; }
   
   // Get our original not cached download URL from UrlLoader.
   
   URL getDownloadURL() { return downloadurl ; }
   
   // Set our last open event FileOpen path.
   
   void setOpenPath(String path) { openpath = path ; }
   
   // Close the Tutorial screen invoked through File-New.
   
   void closeTutorial() 
   { 
      if (!filenew) return ;
      filenew = false ;
      if (helper2 != null) helper2.close() ; 
   }

   
   // Implementation of the required menu item update.

   void update()
   {
      newkiss.setEnabled(OptionsDialog.getEditEnable());
      newpage.setEnabled(OptionsDialog.getEditEnable());
      insertpage.setEnabled(OptionsDialog.getEditEnable());
      deletepage.setEnabled(OptionsDialog.getEditEnable());
      writepage.setEnabled(OptionsDialog.getEditEnable() && !Kisekae.isSecure());
      tracefkiss.setEnabled(!OptionsDialog.getSecurityEnable());
   }

   // Implementation of the menu item update of our state when we become
   // visible.  
   
   void updateRunState()
   {
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { updateRunState() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      
      Window [] windows = parent.getOwnedWindows() ;
      if (windows == null) return ;
      
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

   
   // Determination if the tutorial help set is active.
   
   boolean isTutorialDisplayed()
   {
      return (helper2 != null && helper2.isVisible()) ;
   }
   
   // Determination if the main help set is active.
   
   boolean isHelplDisplayed()
   {
      return (helper != null && helper.isVisible()) ;
   }
   
   
   // Add a new last referenced file to the beginning of the LRU list. 
   // If this is a previously referenced item we reorder the list.
   // LRU files must end in a CNF extension.
   
   void setLruFile(String s)
   {
      if (s != null)
      {
         OptionsDialog.setLruFile(s) ;
         LruMenuItem lruitem = removeLruFile(s) ;
         if (lruitem == null)
         {
            int m = s.indexOf(File.pathSeparatorChar) ;
            String path = (m < 0) ? "" : s.substring(0,m) ;
            String cnf = (m < 0) ? s : s.substring(m+1) ;
            File f1 = new File(path) ;
            File f2 = new File(cnf) ;
            String s1 = f1.getName() + " " + f2.getName() ;
            if (path.endsWith(File.separator)) s1 = f2.getName() ;
            while (s1.startsWith(" ")) s1 = s1.substring(1) ;
            lruitem = new LruMenuItem(s1) ;
            lruitem.setLruName(s) ;
            lruitem.addActionListener(this) ;
         }
         for (int i = lru.length-1 ; i > 0 ; i--) lru[i] = lru[i-1] ;
         if (lru.length > 0) lru[0] = lruitem ;
      }
      
      // Remove all the old file LRU menu items from the menu.

      clearLruFiles() ;
       
      // Add the new file LRU menu items.
      
      if (lru.length > 0) fileMenu.addSeparator() ;
      for (int i = 0 ; i < lru.length ; i++)
         if (lru[i] != null) fileMenu.add(lru[i]) ;
 }
   
   
   // Remove all last referenced files from the LRU menu.  This method
   // assumes that the LRU files are listed at the end of the file menu.
   
   void clearLruFiles()
   {
      int n = 0 ;
      mb = getMenuBar() ;
      if (mb == null || mb.getMenuCount() == 0) return ;
      fileMenu = mb.getMenu(0) ;
      if (fileMenu == null) return ;
      
      for (n = fileMenu.getItemCount()-1 ; n > 0 ; n--)
      {
         boolean found = false ;
         Object item = fileMenu.getItem(n) ;
         for (int i = 0 ; i < lru.length ; i++)
            if (item == lru[i]) { fileMenu.remove(n) ; found = true ; break ; }
         if (!found) break ;
      }
      
      Object item = fileMenu.getItem(n) ;
      if (item instanceof JSeparator) fileMenu.remove(n) ;
      return ;
   }
   
   
   // Erase all LRU files.  
   
   void eraseLruFiles()
   {
      clearLruFiles() ;
      for (int i = 0 ; i < lru.length-1 ; i++) lru[i] = null ;
   }
   
   
   // Remove the specified entry from the LRU table.  This method returns
   // the removed menu item.  It returns null if the entry does not exist.
   
   private LruMenuItem removeLruFile(String s)
   {
      int i = findLruFile(s) ;
      if (i < 0) return null ;
      LruMenuItem item = (LruMenuItem) lru[i] ;
      for (int j = i ; j < lru.length-1 ; j++) lru[j] = lru[j+1] ;
      lru[lru.length-1] = null ;
      return item ;
   }
   
   
   // Find the specified entry in the LRU table.  This method returns
   // the index of the menu item.  It returns -1 if the entry does not exist.
   
   private int findLruFile(String s)
   {
      for (int i = 0 ; i < lru.length ; i++)
      {
         if (!(lru[i] instanceof LruMenuItem)) break ;
         LruMenuItem item = (LruMenuItem) lru[i] ;
         String s1 = item.getLruName() ;
         if (s1 != null && s1.equals(s)) return i ;
      }
      return -1 ;
   }



   // The action method is used to process control menu events.
   // This method is required as part of the ActionListener interface.

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource() ;
      parent.showStatus(null) ;

      // An Option request brings up the Option dialog window.

      try
      {
         if (options == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu options dialog request") ;
            parent.getOptionsDialog().show() ;
            return ;
         }

         // A Memory request shows the current memory use and presents an option
         // to reclaim memory by invoking the garbage collector.

         if (memory == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu - memory display request") ;
            long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
            int n = (int) (mem/1024) ;
            String s1 = Kisekae.getCaptions().getString("MemoryInUseText1") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + n + s1.substring(j1+1) ;
            int i = JOptionPane.showConfirmDialog(parent,
               s1 + "\n" + Kisekae.getCaptions().getString("MemoryInUseText2"),
               Kisekae.getCaptions().getString("MemoryUtilizationTitle"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE) ;

            // Invoke the garbage collector if requested.

            if (i == JOptionPane.YES_OPTION) Runtime.getRuntime().gc() ;
            return ;
         }

         // An Archive Manager request invokes the archive tool to create and
         // extract files from an archive.

         if (archive == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu archive manager request") ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ZipManager manager = new ZipManager() ;
            manager.setVisible(true) ;
            manager.toFront() ;
            return ;
         }

         // A Text Editor request invokes the text editor tool to create and
         // edit text files.

         if (texteditor == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu text editor request") ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            TextFrame editor = new TextFrame() ;
            editor.setVisible(true) ;
            editor.toFront() ;
            return ;
         }

         // A Color Editor request invokes the color editor tool to create and
         // edit palette files.

         if (coloreditor == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu color editor request") ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ColorFrame editor = new ColorFrame() ;
            editor.setVisible(true) ;
            editor.toFront() ; 
            return ;
         }

         // An Image Editor request invokes the image editor tool to create and
         // edit image files.

         if (imageeditor == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu image editor request") ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ImageFrame editor = new ImageFrame() ;
            editor.setVisible(true) ;
            editor.toFront() ;
            return ;
         }

         // A Media Player request invokes the media player tool to play audio
         // or video files.

         if (mediaplayer == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu media player request") ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            MediaFrame player = new MediaFrame() ;
            player.setMinimized(false) ;
            player.setVisible(true) ;
            player.toFront() ;
            return ;
         }

         // The Help About request brings up the About dialog window.

         if (about == source)
         {
            if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu about dialog request") ;
           parent.getAboutDialog().show() ;
            return ;
         }

         // The Register dialog enables setting of user and password.

         if (register == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu register request") ;
            String s = Kisekae.getCaptions().getString("RegisterTitle") ;
            RegisterDialog rd = new RegisterDialog(parent,s,true) ;
            rd.show() ;
            return ;
         }

         // A Help Contents request occurs only if the installed Help system is
         // not available.  In this case we attempt to reference online help
         // through the browser.  If in a secure mode we use our portal.

         if (help == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu help request") ;
            String helpurl = OptionsDialog.getWebSite() + OptionsDialog.getOnlineHelp() ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            if (Kisekae.isSecure())
            {
               WebFrame wf = new WebFrame(parent,helpurl+onlinehelp) ;
               wf.setVisible(true) ;
               wf.toFront() ;
            }
            else
            {
               BrowserControl browser = new BrowserControl() ;
               browser.displayURL(helpurl+onlinehelp);
            }
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            return ;
         }

         // A Tutorial request can occur only if the Help system is
         // installed.  We position the mainframe and help window so that
         // the tutorial can run beside the main window.

         if (tutorial == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu tutorial request") ;
            if (helper2 == null) return ;            
            Dimension screensize = Kisekae.getScreenSize() ;
            screensize.width = (int) (screensize.width*0.5f) ;
            screensize.height = (int) (screensize.height*0.95f) ;
            windowsize = parent.getSize() ;
            windowlocation = parent.getLocation() ;
            parent.setSize(screensize) ;
            parent.setLocation(0,0) ;
            helper2.setSize(screensize) ;
            helper2.setLocation(new Point(screensize.width,0)) ;
            helper2.setViewDisplayed(false) ;
            helper2.setDisplayed(true) ;
            tutorial.setEnabled(false) ;
            return ;
         }

         // A HelpWindowClosed request occurs if the Tutorial Help system 
         // exited.  We restore the mainframe window to its prior size and
         // location.

         if ("HelpWindowClosed".equals(evt.getActionCommand()))
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu help window close request") ;
            tutorial.setEnabled(helper2 != null && helper2.isLoaded()) ;
            if (helper2 == null) return ;
            if (windowsize == null) return ;
            if (windowlocation == null) return ;
            parent.setSize(windowsize) ;
            parent.setLocation(windowlocation) ;
            windowlocation = null ;
            windowsize = null ;
            return ;
         }
         
         // Window display commands are of the form 'nn. title'. If we have
         // one of these bring the window to the front.
         
         if (evt.getActionCommand().indexOf(". ") > 0)
         {
            String s = evt.getActionCommand() ;
            s = s.substring(s.indexOf(". ")+2) ;
            if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu window display request for " + s) ;
            Vector windows = KissFrame.getWindowFrames() ;
            for (int i = 0 ; i < windows.size() ; i++)
            {
               KissFrame f = (KissFrame) windows.elementAt(i) ;
               if (s.equals(f.getTitle()))
               {
                  if (f instanceof MediaFrame)
                     ((MediaFrame) f).setMinimized(false) ;
                  if ((f.getExtendedState() & Frame.ICONIFIED) != 0)
                     f.setState(Frame.NORMAL) ;
                  f.setVisible(true) ;
                  f.toFront() ;
                  break ;
               }
            }
            return ;
         }

         // A Show Tips request invokes the Tips UltraKiss application.

         if (showtips == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu show tips request") ;
            TipsBox tips = Kisekae.getTipsBox() ;
            if (tips == null) tips = new TipsBox(parent,Kisekae.getCaptions().getString("TipsBoxTitle"),false) ;
            tips.setModal(false) ;
            tips.show() ;
            return ;
         }

         // A Show Demo request invokes the Browser to run KiSS applications.

         if (rundemo == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu run demo request") ;
            String webstart = Kisekae.getDemoIndex() ;
            if (webstart == null) return ;
            URL demourl = Kisekae.getResource(webstart) ;
            if (demourl != null) webstart = demourl.toString() ;
            parent.showStatus("Connecting ...");
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            WebFrame.setCurrentWeb(webstart) ;
            WebFrame wf = new WebFrame(parent) ;
            parent.showStatus(null);
            wf.setVisible(true) ;
            wf.toFront() ;
            return ;
         }

         // A ReadMe request invokes the Browser to display our product 
         // documentation information.  If we are running in an applet
         // then we use our internal portal to access web pages from 
         // the applet jar.

         if (readme == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu readme request") ;
            String readmeindex = Kisekae.getReadMeIndex() ;
            if (readmeindex == null) return ;
            URL readme = Kisekae.getResource(readmeindex) ;
            if (readme == null) return ;
//          Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
//          BrowserControl.displayURL(readme.toExternalForm()) ;
//          Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            parent.showStatus("Connecting ...");
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            WebFrame wf = new WebFrame(parent,readme.toString()) ;
            parent.showStatus(null);
            wf.setVisible(true) ;
            wf.toFront() ;
         }

         // An Exit request closes our frame.  We process end events prior
         // to termination to enable an orderly KiSS application shutdown.

         if (exit == source)
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu exit request") ;
            parent.exit() ;
            return ;
         }

         // A New request establishes a completely new data set.
         // A New Configuration request establishes a new CNF in an archive.

         if (newkiss == source)
         {
            if (evt.getActionCommand().endsWith("..."))
               eventNew(NEWCONFIG) ;
            else
               eventNew(NEWFILE) ;
            return ;
         }

         // A New Page request establishes a new page set.

         if (newpage == source)
         { eventNew(NEWPAGE) ; return ; }

         // An Insert Page request establishes a new page set.

         if (insertpage == source)
         { eventNew(INSERTPAGE) ; return ; }

         // A Delete Page request removes a page set.

         if (deletepage == source)
         { eventNew(DELETEPAGE) ; return ; }

         // A Write Page request sets the page set initial positions.

         if (writepage == source)
         { eventNew(WRITEPAGE) ; return ; }

         // An Open request opens a dialog to select a new data set.

         if (open == source)
         { eventOpen() ; return ; }

         // An Expand request opens an expansion data set.

         if (expand == source)
         { eventExpand() ; return ; }

         // An Open Web request opens the default browser to access web files.

         if (openweb == source)
         { eventWeb() ; return ; }

         // An Open URL request opens a frame to access web files.

         if (openurl == source)
         { eventUrl() ; return ; }

         // An Open Portal request opens a frame to browse HTML 3.2 web files.

         if (openportal == source)
         { eventPortal() ; return ; }

         // A Search request opens a frame to browse for KiSS files.

         if (websearch == source)
         { eventSearchWeb() ; return ; }

         // A Bug Report request opens a web browser to submit bug entries.

         if (bugreport == source)
         { eventBug() ; return ; }

         // A Tutorial Files request opens a web browser to download the GitHub file.

         if (tutorialfiles == source)
         { eventTutorialFiles() ; return ; }

         // A Clear Cache request clears the download cache.

         if (clearcache == source)
         { eventClearCache() ; return ; }

         // A Log File request shows the current log file.

         if (logfile == source || 
            evt.getActionCommand() == Kisekae.getCaptions().getString("MenuViewLogFile"))
         {
           if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu view logfile request") ;
            InputStream is = null ;
            ArchiveFile dir = null ;
            File fl = new File(LogFile.getLogFileName()) ;
            try { is = new FileInputStream(fl) ; }
            catch (FileNotFoundException e) { is = null ; }

            // If log file exists, show a text edit frame.

            if (is != null)
            {
               try { LogFile.flushcontents() ; }
               catch (IOException e) { }
               String directory = fl.getParent() ;
               try { dir = new DirFile(null,directory) ; }
               catch (IOException e) { dir = null ; }
               ArchiveEntry ze = new DirEntry(directory,fl.getName(),dir) ;
               Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
               TextFrame tf = new TextFrame(ze,is,false) ;
               tf.setVisible(true) ;
               tf.toFront() ;
            }
            return ;
         }
         
         // Determine if the item is in the LRU list.  The item text provides
         // the full path to the archive and the CNF element name.
         
         if (source instanceof LruMenuItem)
         {
            String s = ((LruMenuItem) source).getLruName() ;
            if (OptionsDialog.getDebugControl())
               PrintLn.println("MainMenu open LRU request for " + s) ;
            int n = findLruFile(s) ;
            if (n >= 0)
            {
               String cnf = "" ;
               String archive = "" ;
               int i = s.lastIndexOf(File.pathSeparatorChar) ;
               if (i > 0) 
               {
                  cnf = s.substring(i+1) ;
                  archive = s.substring(0,i) ;
               }
               
               // Clear our URL NoCopy indicator for loads from the menu.
      
               setNoCopy(false) ;
               setOpenPath(archive) ;
               OptionsDialog.setSecurityEnable(OptionsDialog.getInitSecurityEnable());  
               
               // Load the LRU file.  If the load fails disable the 
               // entry on the menu list. 
               
      			if (ArchiveFile.isArchive(archive))
      				PrintLn.println("Open LRU archive " + archive) ;
               parent.loadfile(archive,cnf) ; 
               if (fd == null)
               {
                  OptionsDialog.removeLruFile(s) ;
                  ((LruMenuItem) source).setEnabled(false) ;
                  Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               }
            }
         }

         // Watch for URL load callbacks.  These signal completion of the
         // download of a URL file.

         if ("UrlLoader Callback".equals(evt.getActionCommand()))
         {
            if (urlloader == null) return ;
            String pathname = urlloader.getTempFileName() ;
            String urlname = urlloader.getUrlName() ;
            String setname = urlloader.getSetName() ;
            String cnfname = urlloader.getCnfName() ;
            downloadurl = urlloader.getURL() ;
            urlloader = null ;
            if (pathname == null)
            {
               Kisekae.setLoaded(false) ;
               return ;
            }

            // Create a FileOpen object for this temporary file.

            URL sourceURL = null ;
            try { sourceURL = new URL(urlname) ; }
            catch (MalformedURLException e) { sourceURL = null ; }
            FileOpen fdnew = new FileOpen(parent,pathname,"r") ;
            fdnew.setSourceURL(sourceURL) ;
            fdnew.open() ;
            ArchiveEntry ze = fdnew.showConfig(parent,cnfname) ;
            ArchiveFile zip = fdnew.getZipFile() ;
            if (zip != null) zip.setOriginalName(setname) ;
            if (ze == null)
            {
               fdnew.close() ;
               Kisekae.setLoaded(false) ;
               return ;
            }

            // Load the URL file.

            openContext(fdnew,ze) ;
         }
      }

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.

      catch (OutOfMemoryError e)
      {
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         PrintLn.println("MainMenu: Out of memory.") ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         Kisekae.setLoaded(false) ;
         if (!Kisekae.isBatch())
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
               JOptionPane.ERROR_MESSAGE) ;
      }

      // Watch for internal faults during action events.

      catch (Throwable e)
      {
         PrintLn.println("MainMenu: Internal fault, action " + evt.getActionCommand()) ;
         e.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError") + " - " ;
         s += Kisekae.getCaptions().getString("KissSetClosed") ;
         s += "\n" + e.toString() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            e.printStackTrace(ps) ;
            os.close() ;
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

         if (!Kisekae.isBatch())
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("InternalError"),
               JOptionPane.ERROR_MESSAGE) ;
         Kisekae.setLoaded(false) ;
         parent.closeconfig() ;
      }
   }



   // The item state changed method is invoked when checkbox menu
   // items are selected.  This method is required as part of the
   // ItemListener interface.

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource() ;

      // Turn the status bar on and off.

      if (source == statusbar)
      {
         parent.setStatusBar(statusbar.getState()) ;
      }

      // Turn the tool bar on and off.  Put up a wait cursor because
      // the initial toolbar image load can take some time.

      else if (source == toolbar)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.setToolBar(toolbar.getState()) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      else if (source == viewtbcompat)
      {
         OptionsDialog.setTbCompat(viewtbcompat.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      else if (source == viewtbtools)
      {
         OptionsDialog.setTbTools(viewtbtools.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      else if (source == viewtbedits)
      {
         OptionsDialog.setTbEdits(viewtbedits.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      else if (source == viewtbpages)
      {
         OptionsDialog.setTbPages(viewtbpages.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      else if (source == viewtbcolors)
      {
         OptionsDialog.setTbColors(viewtbcolors.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.updateToolBar() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      // Fit the panel image to the screen size.

      else if (source == fitscreen)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.fitScreen(fitscreen.getState(),false) ;
         OptionsDialog.setScaleToFit(fitscreen.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      // Fit the window size to the panel size.

      else if (source == fitpanel)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.fitPanel(fitpanel.getState()) ;
         OptionsDialog.setSizeToFit(fitpanel.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      // Show the window border.

      else if (source == showborder)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.showBorder(showborder.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      // Enable the FKiSS trace window.

      else if (source == tracefkiss)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.traceFKiss(tracefkiss.getState()) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }

      // Re-establish the parent frame.

      parent.validate() ;
      parent.centerpanel() ;
      PanelFrame panel = parent.getPanel() ;
      if (panel != null) panel.requestFocus() ;
   }


   // Toolbar and Menu shared event action methods
   // --------------------------------------------

   void eventNew(int type) 
   { 
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventNew, type " + type) ;
      parent.setNew(type) ;  
      
      // Show tutorial if File-New clicked.
      
      if (type == NEWFILE)
      {
         if (tutorial != null)
         {
            filenew = true ;
            if (helper2.isVisible()) return ;
            tutorial.doClick();
         }
      }
   }

   void eventOpen()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventOpen ") ;
      FileOpen fdnew = null ;
      ArchiveEntry ze = null ;

      if (!Kisekae.isSecure())
      {
         fdnew = new FileOpen(parent) ;
         fdnew.setFileFilter("kissarchives") ;
         fdnew.show() ;
         ze = fdnew.getZipEntry() ;
      }

      // If running in a secure mode, use JNLP and load to a memory file.
      // We can only open archive files.

      else
      {
         try
         {
            int b = 0 ;
            FileOpenService fos = (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
            FileContents fc = fos.openFileDialog(null,null) ;
            if (fc != null)
            {
               String name = fc.getName() ;
               InputStream is = fc.getInputStream() ;
               ByteArrayOutputStream os = new ByteArrayOutputStream() ;
               while ((b = is.read()) >= 0) { os.write(b) ; }
               MemFile memfile = new MemFile(name, os.toByteArray()) ;
               UrlLoader.setMemoryFile(memfile) ;
               fdnew = new FileOpen(parent,name,"r") ;
               fdnew.open() ;
               ze = fdnew.showConfig(parent) ;
            }
         }
         catch (Throwable e)
         {
            PrintLn.println("MainMenu: JNLP FileOpenService is not available.");
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("FileReadError")
               + "\n" + e.toString(),
               Kisekae.getCaptions().getString("FileOpenException"),
               JOptionPane.WARNING_MESSAGE) ;
           return ;
         }
      }
     
      // Clear our URL NoCopy indicator for loads from the menu.
      
      setNoCopy(false) ;
      downloadurl = null ;
      setOpenPath(fdnew.getPath()) ;      
      OptionsDialog.setSecurityEnable(OptionsDialog.getInitSecurityEnable());
      fd = fdnew ;
      if (ze == null && fdnew.getChoice() == JOptionPane.NO_OPTION) return ;
      openContext(fd, ze) ;
   }

   // An expansion set load extends and replaces the current data set.

   void eventExpand()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventExpand ") ;
      if (parent == null) return ;
      Configuration c = parent.getConfig() ;
      if (c == null) return ;
      
      FileOpen fdnew = new FileOpen(parent) ;
      fdnew.show(Kisekae.getCaptions().getString("OpenExpansionTitle")) ;
      ArchiveEntry ze = fdnew.getZipEntry() ;
      ArchiveFile zip = fdnew.getZipFile() ;
      
      // The added file is an expansion file.
     
      File f = fdnew.getFileObject() ;
      if (f == null) return ; 
      Vector expandfiles = new Vector() ;
      expandfiles.add(zip) ;
      c.setExpandFiles(expandfiles) ;
      PrintLn.println("Add expansion file " + zip.getFileName());
      
      // If there is a CNF in the expansion file use it.  If not, use the
      // current configuration file.  
      
      if (ze == null)
      {
         ze = c.getZipEntry() ;
         fdnew.setZipEntry(ze) ;
      }
      fd = fdnew ;

      // Configuration elements initialize the main frame.

      if (ze != null && ze.isConfiguration())
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.expand(true) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
   }

   // If running as an applet, get the Kisekae HTML page for our initial
   // display.  Otherwise use the default web browser to display pages.

   void eventWeb()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventWeb ") ;
      try
      {
         URL webpage = null ;
         String s = Kisekae.getLoadBase() + portal ;
         webpage = Kisekae.getResource(portal) ;
         if (webpage == null) webpage = new URL(s) ;
         if (webpage != null)
         {
//            AppletContext browser = Kisekae.getContext() ;
//            if (browser != null)
//               browser.showDocument(webpage,"_blank") ;
//            else
            {
               String kissweb = OptionsDialog.getWebSite() ;
               try
               {
                  if (Kisekae.isWebsocket())
                     BrowserControl.displayURL("") ;
                  else
                  {
                     Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                     BrowserControl.displayURL(kissweb) ;
                     Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
                  }
               }
               catch (Exception ex) { }
            }
         }
      }
      catch (Exception e) { }
   }

   // Invoke the browser to show the GitHub bug report page.

   void eventBug()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventBug ") ;
      try
      {
         URL webpage = null ;
         String s = Kisekae.getLoadBase() + urlbug ;
         webpage = Kisekae.getResource(urlbug) ;
         if (webpage == null) webpage = new URL(s) ;
         if (webpage != null)
         {
//            AppletContext browser = Kisekae.getContext() ;
//            if (browser != null)
//               browser.showDocument(webpage,"_blank") ;
//            else
            {
               String kissweb = OptionsDialog.getWebSite() ;
               try
               {
                  Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  BrowserControl.displayURL(kissweb + urlbug) ;
                  Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               }
               catch (Exception ex) { }
            }
         }
      }
      catch (Exception e) { }
   }

   // Invoke the browser to show the GitHub download page for tutorial files.

   void eventTutorialFiles()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventTutorialFiles ") ;
      try
      {
         URL webpage = null ;
         String s = Kisekae.getLoadBase() + urltutorialfiles ;
         webpage = Kisekae.getResource(urltutorialfiles) ;
         if (webpage == null) webpage = new URL(s) ;
         if (webpage != null)
         {
//           AppletContext browser = Kisekae.getContext() ;
//           if (browser != null)
//               browser.showDocument(webpage,"_blank") ;
//            else
            {
               String kissweb = OptionsDialog.getWebSite() ;
               try
               {
                  Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  BrowserControl.displayURL(kissweb + urltutorialfiles) ;
                  Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               }
               catch (Exception ex) { }
            }
         }
      }
      catch (Exception e) { }
   }
   
   // The Clear Cache command clears the download cache.
   
   void eventClearCache()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventClearCache ") ;
      if (parent == null) return ;
      OptionsDialog options = parent.getOptionsDialog() ;
      if (options == null) return ;
      ActionEvent event = new ActionEvent(options.getCacheBtn(),0,"Clear Cache") ;
      options.actionPerformed(event) ;
   }

   // The Open URL command loads a KiSS data set from a URL over the web.
   // We copy the URL file to a temporary file and then process it as per
   // its type. If the application or applet is running in the sandbox
   // then only LZH type KiSS sets can be processed as I/O is required
   // for other file types.

   void eventUrl()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventUrl ") ;
      URLEntryDialog ud = new URLEntryDialog(parent) ;
      String urlname = ud.getSelected() ;
      if (urlname == null || urlname.length() == 0) return ;
      urlloader = new UrlLoader(parent,urlname) ;
      urlloader.callback.addActionListener(this) ;
      Thread loadthread = new Thread(urlloader) ;
      loadthread.start() ;
   }

   // The Open Portal command shows the KiSS index page on the specified
   // web site.  We use a simple EditorPane object to traverse the web
   // directory to load a KiSS data set from a URL over the web as per
   // the eventURL method.  

   void eventPortal()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventPortal ") ;
      String website = null ;
      URL currentweb = null ;
      parent.showStatus("Connecting ...");
      Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      if ("".equals(webURL)) webURL = null ;
      try { currentweb = new URL(webURL) ; }
      catch (Exception e) { currentweb = null ; }
      if (currentweb != null) 
         website = currentweb.getProtocol() + "://" + currentweb.getHost() + "/" ;
      
      // If the portal is not running then launch the Portal.
      
      if (webframe == null)
      {
         if (webURL == null && website == null)
            webframe = new WebFrame(parent) ;
         else
            webframe = new WebFrame(parent,webURL,website) ;
         parent.showStatus(null);
         webframe.setVisible(true) ;
         webframe.toFront() ;
         return ;
      }
      
      // If the portal is running set the required page and bring to front.

      webframe.clearLocalHistory(webURL) ;
      webframe.setPage(webURL) ;
      Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      webframe.toFront() ;
   }

   // The Search command uses UltraKiss to search the local computer, or
   // a specified web site, for KiSS archives.  An index page complete
   // with thumbnails is built with links to the known KiSS archives.

   void eventSearchWeb()
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventSearchWeb ") ;
/*     String msg = "The Search function scans a file directory or a website\n"
         + "to construct a thumbnail index of all KiSS files found.\n"
         + "Your current UltraKiss session will be closed.\n\n"
         + "Continue?" ;
      int i = JOptionPane.showConfirmDialog(parent,msg,
         Kisekae.getCaptions().getString("SearchTabText"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE) ;
      if (i != JOptionPane.YES_OPTION) return ;
*/
      Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
      com.wmiles.kisekaeultrakiss.WebSearch.WebSearchFrame wf = new com.wmiles.kisekaeultrakiss.WebSearch.WebSearchFrame(parent) ;
      Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      parent.showStatus(null);
      wf.setVisible(true) ;
      wf.toFront() ;
   }


   // Turn sound on and off

   void eventSound(boolean b)
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventSound b " + b) ;
      OptionsDialog.setSoundOn(b) ;
      if (!b) Audio.stop() ;
   }

   // Turn movies on and off

   void eventMovie(boolean b)
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu eventSound b " + b) ;
      OptionsDialog.setMovieOn(b) ;
      if (!b) Video.stop() ;
   }


   // A utility function to open a loaded file according to its context.
   // Configuration elements initialize the main frame.
   // Archive file without CNF elements open the Archive Manager.

   void openContext(FileOpen fd, ArchiveEntry ze)
   {
      if (OptionsDialog.getDebugControl())
         PrintLn.println("MainMenu openContext ze = " + ze) ;
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { openContext(fd,ze) ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      setFileOpen(fd) ;
      if (ze == null) 
      {
         ArchiveFile zip = (fd == null) ? null : fd.getZipFile() ;
         if (zip != null && zip.isArchive())
         {
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ZipManager zm = new ZipManager(zip) ;
            Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            zm.setVisible(true) ;
            zm.toFront() ;
         }
         if (fd != null) fd.close() ; 
         return ;
      }
      
      // Configuration elements open a KiSS set.  Any existing set must be
      // closed.

      if (fd != null && !viewastext.isSelected() && ze.isConfiguration())
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         parent.setNewPreAppend(null,null) ;
         parent.closepanel() ;
         setFileOpen(fd) ;
         PrintLn.println("MainMenu: openContext initialize " + ze.getName());
         parent.init() ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         return ;
      }

      // Text elements invoke the text editor.

      if (viewastext.isSelected() || ze.isText() || ze.isStyledText())
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         TextFrame tf = new TextFrame(ze) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         tf.setVisible(true) ;
         tf.toFront() ;
         return ;
      }

      // Palette elements invoke the color editor.

      if (ze.isPalette())
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         ColorFrame cf = new ColorFrame(ze) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         cf.setVisible(true) ;
         cf.toFront() ;
         return ;
      }

      // Audio and Video elements invoke the media player.

      if ((ze.isAudio() || ze.isVideo() || ze.isList()))
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         MediaFrame mf = MediaFrame.getUniquePlayer() ;
         if (mf == null) 
            mf = new MediaFrame(ze,ze) ;
         else
            mf.play(ze) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         mf.setVisible(true) ;
         mf.toFront() ;
         return ;
      }

      // Image elements invoke the image editor.

      if (ze.isImage())
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         ImageFrame cf = new ImageFrame(ze) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         cf.setVisible(true) ;
         cf.toFront() ;
         return ;
      }

      // Unknown elements are a problem.

      int i = JOptionPane.showConfirmDialog(parent,
         ((fd != null) ? fd.getElement() : "") + "\n" +
         Kisekae.getCaptions().getString("MenuViewAsText")+ "?",
         Kisekae.getCaptions().getString("FileOpenException"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE) ;

      // Invoke the text viewer if requested.

      if (i == JOptionPane.YES_OPTION)
      {
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         TextFrame tf = new TextFrame(ze) ;
         Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         tf.setVisible(true) ;
         tf.toFront() ;
         return ;
      }

      // Cancel file open.

      if (fd != null) fd.close() ;
   }
}
