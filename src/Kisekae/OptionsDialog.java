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
import java.awt.event.*;
import java.io.* ;
import java.util.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import javax.swing.*;
import javax.swing.event.* ;
import javax.swing.border.*;


final public class OptionsDialog extends KissDialog
	implements ActionListener, WindowListener, ListSelectionListener, ChangeListener
{
   private static String properties = "UltraKiss.properties" ;
   
   protected OptionsDialog me = null ;             // Ourselves
	private boolean suspendactions = false ;			// Suspend action events
   private static boolean writeheader = true ;     // Options write
   private static boolean initialized = false ;    // Properties read
   private static boolean compatapply = false ;    // True if compat change
   private static boolean compatperm = false ;     // True if compat permanent
   private static boolean languagerestart = false ;// True if language change
   private static boolean iconrestart = false ;    // True if icon change
   private static boolean splashrestart = false ;  // True if splash change
   private static Vector backsetnames = new Vector() ; // Back splash set names
   private boolean languageapply = false ;         // True if apply language
   private boolean userdirapply = false ;          // True if user dir change
   private boolean icondirapply = false ;          // True if icon dir change
   private boolean splashdirapply = false ;        // True if splash dir change
   private Locale newlocale = null ;               // New locale to set
   private Object newencoding = null ;             // New encoding to set

   // Option values.  These are static as only one set of options
   // exist for the complete program.

	private static boolean sound = true ;
	private static boolean movie = true ;
	private static boolean event = true ;
	private static boolean animate = true ;
	private static boolean timer = true ;
	private static boolean editenable = true ;
	private static boolean tempeditenable = false ;
	private static boolean securityenable = false ;
	private static boolean initedit = true ;
	private static boolean debugmouse = false ;
	private static boolean debugcontrol = false ;
	private static boolean debugload = false ;
	private static boolean debugedit = false ;
	private static boolean debugimage = false ;
	private static boolean debugevent = false ;
	private static boolean debugaction = false ;
	private static boolean debugvariable = false ;
	private static boolean debugfkiss = false ;
	private static boolean debugsound = false ;
	private static boolean debugmovie = false ;
	private static boolean debugmedia = false ;
	private static boolean debugsearch = false ;
	private static boolean debugdisabled = false ;
	private static boolean debugcomponent = false ;
	private static boolean javasound = true ;
	private static boolean systemlf = false ;
	private static boolean javalf = true ;
	private static boolean backup = false ;
 	private static boolean savesource = false ;
 	private static boolean showdlprompt = true ;
 	private static boolean showtips = true ;
 	private static boolean loadclose = true ;
	private static boolean cacheaudio = true ;
 	private static boolean cachevideo = false ;
	private static boolean cacheimage = true ;
	private static boolean suspendmedia = true ;
	private static boolean longsoundmedia = false ;
	private static boolean adjustmediavolume = false ;
	private static boolean stopmusic = false ;
	private static boolean soundsingle = false ;
	private static boolean scaletofit = false ;
	private static boolean sizetofit = false ;
	private static boolean retainwindowsize = false ;
	private static boolean maximizewindow = false ;
	private static boolean randomsplash = true ;
	private static boolean showborder = true ;
	private static boolean inittoolbar = true ;
	private static boolean tbtools = true ;			
	private static boolean tbcolors = true ;		
	private static boolean tbpages = true ;		
	private static boolean tbedits = true ;      
	private static boolean tbcompat = true ;		
	private static boolean initstatusbar = true ;
	private static boolean initmenubar = true ;
	private static boolean importcel = false ;
	private static boolean exportcel = false ;
	private static boolean componentcel = false ;
	private static boolean importcomponent = false ;
	private static boolean constrainmoves = false ;
	private static boolean constrainvisible = true ;
	private static boolean constrainfkiss = true ;
	private static boolean autoscroll = true ;
	private static boolean dragmove = false ;
	private static boolean releasemove = true ;
	private static boolean dropfixdrop = true ;
	private static boolean catchfixdrop = true ;
	private static boolean visibleunfix = true ;
	private static boolean earlyfix = false ;
	private static boolean detachrestricted = true ;
	private static boolean detachmove = true ;
	private static boolean detachfix = true ;
	private static boolean invertghost = false ;
	private static boolean transparentgroup = true ;
	private static boolean mapcollide = false ;
	private static boolean movexycollide = false ;
	private static boolean automedialoop = false ;
	private static boolean autofullscreen = false ;
	private static boolean mediaminimize = false ;
	private static boolean mediacenter = true ;
	private static boolean mediamusicresume = false ;
	private static boolean keepaspect = true ;
	private static boolean keycase = false ;
	private static boolean variablecase = false ;
	private static boolean expandevents = false ;
	private static boolean enableshell = false ;
	private static boolean xyoffsets = false ;
	private static boolean absolutewindow = false ;
	private static boolean panelvisible = false ;
	private static boolean mouseinoutbox = false ;
	private static boolean contextmap = true ;
	private static boolean mapcount = true ;
	private static boolean allambiguous = false ;
	private static boolean alteditdrag = true ;
	private static boolean shifteditdrag = false ;
	private static boolean importrelative = false ;
	private static boolean writeceloffset = true ;
	private static boolean showbreakpointend = true ;
	private static boolean showstepintoend = true ;
	private static boolean writecomment = true ;
	private static boolean eventpause = true ;
	private static boolean actionpause = false ;
	private static boolean disableall = false ;
	private static boolean retainkey = true ;
	private static boolean strictsyntax = true ;
	private static boolean showundefs = false ;
	private static boolean autoendif = false ;
	private static boolean immediatecollide = true ;
	private static boolean immediateunfix = false ;
	private static boolean immediateevent = false ;
	private static boolean applemac = false ;
	private static boolean linux = false ;
   private static boolean playfkiss = false ;
   private static boolean directkiss = false ;
   private static boolean gnomekiss = false ;
   private static boolean kissld = false ;
   private static boolean defaultplayfkiss = false ;
   private static boolean acceptcnferrors = false ;
   private static boolean pagesarescenes = false ;
   private static boolean multipleevents = true ;
   private static String eventqueues = "1" ;
   private static String timerperiod = "10" ;
   private static String gifperiod = "100" ;
   private static String sceneperiod = "1000" ;
   private static String audioperiod = "30000" ;
   private static String stickyflex = "10" ;
   private static String maxflex = "100" ;
   private static String maxlock = "32767" ;
   private static String maxpageset = "10" ;
   private static String maxcolorset = "10" ;
   private static String jpegquality = "0.9" ;
   private static String undolimit = "100" ;
   private static String commentcol = "60" ;
   private static String indentspace = "1" ;
   private static String maxlrufiles = "4" ;
   private static String longduration = "10" ;
   private static String mediavolume = "0.5" ;
   private static Object language = "English" ;
   private static Object encoding = "Cp1252" ;
   private static Object exporttype = "CEL" ;
   private static Object browser = "" ; 
	private static String kissweb = Kisekae.getKissWeb() ;
   private static String userdir =  Kisekae.getBaseDir() ;
   private static String website = Kisekae.getWebSite() ;
   private static String splashdir = Kisekae.getSplashDir() ; 
   private static String icondir = Kisekae.getIconDir() ; 
   private static String onlinehelp = "HelpFiles/product/" ;
   private static Integer iconnumber = new Integer(0) ;
   private static Integer splashsetnumber = new Integer(0) ;
   private static Vector lrufiles = null ;

   // WebSearch Option values.

	private static boolean savearchive = true ;
	private static boolean saveimage = true ;
	private static boolean saveaszip = true ;
	private static boolean showload = false ;
	private static boolean usedefaultws = true ;
	private static boolean clearmaster = false ;
   private static String htmldirectory = "WebSearch/HTML/" ;
   private static String datadirectory = "WebSearch/Data/" ;
   private static String imagedirectory = "WebSearch/HTML/Images/" ;
   private static String kissindex = "WebSearch/HTML/index.html" ;
   private static String thumbwidth = "50" ;
   private static String thumbheight = "50" ;
   private static String downloadsize = "1024" ;
   private static String thumbpage = "0" ;

   // Initial option values.

	private static boolean initsound = sound ;
	private static boolean initmovie = movie ;
	private static boolean initevent = event ;
	private static boolean initanimate = animate ;
	private static boolean inittimer = timer ;
	private static boolean initeditenable = editenable ;
	private static boolean initsecurityenable = securityenable ;
	private static boolean initinitedit = initedit ;
	private static boolean initdebugmouse = debugmouse ;
	private static boolean initdebugcontrol = debugcontrol ;
	private static boolean initdebugload = debugload ;
	private static boolean initdebugedit = debugedit ;
	private static boolean initdebugimage = debugimage ;
	private static boolean initdebugevent = debugevent ;
	private static boolean initdebugaction = debugaction ;
	private static boolean initdebugvariable = debugvariable ;
	private static boolean initdebugfkiss = debugfkiss ;
	private static boolean initdebugsound = debugsound ;
	private static boolean initdebugmovie = debugmovie ;
	private static boolean initdebugmedia = debugmedia ;
	private static boolean initdebugsearch = debugsearch ;
	private static boolean initdebugdisabled = debugdisabled ;
	private static boolean initdebugcomponent = debugcomponent ;
	private static boolean initjavasound = javasound ;
	private static boolean initsystemlf = systemlf ;
	private static boolean initjavalf = javalf ;
	private static boolean initbackup = backup ;
 	private static boolean initsavesource = savesource ;
 	private static boolean initshowdlprompt = showdlprompt ;
 	private static boolean initshowtips = showtips ;
 	private static boolean initloadclose = loadclose ;
	private static boolean initcacheaudio = cacheaudio ;
 	private static boolean initcachevideo = cachevideo ;
	private static boolean initcacheimage = cacheimage ;
	private static boolean initsuspendmedia = suspendmedia ;
	private static boolean initlongsoundmedia = longsoundmedia ;
	private static boolean initadjustmediavolume = adjustmediavolume ;
	private static boolean initstopmusic = stopmusic ;
	private static boolean initsoundsingle = soundsingle ;
	private static boolean initscaletofit = scaletofit ;
	private static boolean initsizetofit = sizetofit ;
	private static boolean initretainwindowsize = retainwindowsize ;
	private static boolean initmaximizewindow = maximizewindow ;
	private static boolean initrandomsplash = randomsplash ;
	private static boolean initshowborder = showborder ;
	private static boolean initinittoolbar = inittoolbar ;
	private static boolean inittbtools = tbtools ;			
	private static boolean inittbcolors = tbcolors ;		
	private static boolean inittbpages = tbpages ;		
	private static boolean inittbedits = tbedits ;      
	private static boolean inittbcompat = tbcompat ;		
	private static boolean initinitstatusbar = initstatusbar ;
	private static boolean initinitmenubar = initmenubar ;
	private static boolean initimportcel = importcel ;
	private static boolean initexportcel = exportcel ;
	private static boolean initcomponentcel = componentcel ;
	private static boolean initimportcomponent = importcomponent ;
	private static boolean initconstrainmoves = constrainmoves ;
	private static boolean initconstrainvisible = constrainvisible ;
	private static boolean initconstrainfkiss = constrainfkiss ;
	private static boolean initautoscroll = autoscroll ;
	private static boolean initdragmove = dragmove ;
	private static boolean initreleasemove = releasemove ;
	private static boolean initdropfixdrop = dropfixdrop ;
	private static boolean initcatchfixdrop = catchfixdrop ;
	private static boolean initvisibleunfix = visibleunfix ;
	private static boolean initearlyfix = earlyfix ;
	private static boolean initdetachrestricted = detachrestricted ;
	private static boolean initdetachmove = detachmove ;
	private static boolean initdetachfix = detachfix ;
	private static boolean initinvertghost = invertghost ;
	private static boolean inittransparentgroup = transparentgroup ;
	private static boolean initmapcollide = mapcollide ;
	private static boolean initmovexycollide = movexycollide ;
	private static boolean initautomedialoop = automedialoop ;
	private static boolean initautofullscreen = autofullscreen ;
	private static boolean initmediaminimize = mediaminimize ;
	private static boolean initmediacenter = mediacenter ;
	private static boolean initmediamusicresume = mediamusicresume ;
	private static boolean initkeepaspect = keepaspect ;
	private static boolean initkeycase = keycase ;
	private static boolean initvariablecase = variablecase ;
	private static boolean initexpandevents = expandevents ;
	private static boolean initenableshell = enableshell ;
	private static boolean initxyoffsets = xyoffsets ;
	private static boolean initabsolutewindow = absolutewindow ;
	private static boolean initpanelvisible = panelvisible ;
	private static boolean initmouseinoutbox = mouseinoutbox ;
	private static boolean initcontextmap = contextmap ;
	private static boolean initmapcount = mapcount ;
	private static boolean initallambiguous = allambiguous ;
	private static boolean initalteditdrag = alteditdrag ;
	private static boolean initshifteditdrag = shifteditdrag ;
	private static boolean initimportrelative = importrelative ;
	private static boolean initwriteceloffset = writeceloffset ;
	private static boolean initshowbreakpointend = showbreakpointend ;
	private static boolean initshowstepintoend = showstepintoend ;
	private static boolean initwritecomment = writecomment ;
	private static boolean initeventpause = eventpause ;
	private static boolean initactionpause = actionpause ;
	private static boolean initdisableall = disableall ;
	private static boolean initretainkey = retainkey ;
	private static boolean initstrictsyntax = strictsyntax ;
	private static boolean initshowundefs = showundefs ;
	private static boolean initautoendif = autoendif ;
	private static boolean initimmediatecollide = immediatecollide ;
	private static boolean initimmediateunfix = immediateunfix ;
	private static boolean initimmediateevent = immediateevent ;
	private static boolean initapplemac = applemac ;
	private static boolean initlinux = linux ;
   private static boolean initplayfkiss = playfkiss ;
   private static boolean initdefaultplayfkiss = defaultplayfkiss ;
   private static boolean initacceptcnferrors = acceptcnferrors ;
   private static boolean initpagesarescenes = pagesarescenes ;
   private static boolean initmultipleevents = multipleevents ;
   private static boolean initdirectkiss = directkiss ;
   private static boolean initgnomekiss = gnomekiss ;
   private static boolean initkissld = kissld ;
	private static String initeventqueues = new String(eventqueues) ;
   private static String inittimerperiod = new String(timerperiod) ;
   private static String initgifperiod = new String(gifperiod) ;
   private static String initsceneperiod = new String(sceneperiod) ;
   private static String initaudioperiod = new String(audioperiod) ;
   private static String initstickyflex = new String(stickyflex) ;
   private static String initmaxflex = new String(maxflex) ;
   private static String initmaxlock = new String(maxlock) ;
   private static String initmaxpageset = new String(maxpageset) ;
   private static String initmaxcolorset = new String(maxcolorset) ;
   private static String initmaxlrufiles = new String(maxlrufiles) ;
   private static String initlongduration = new String(longduration) ;
   private static String initmediavolume = new String(mediavolume) ;
   private static String initjpegquality = new String(jpegquality) ;
   private static String initundolimit = new String(undolimit) ;
   private static String initcommentcol = new String(commentcol) ;
   private static String initindentspace = new String(indentspace) ;
   private static String initkissweb = new String(kissweb) ;
   private static String inituserdir = new String(userdir) ;
   private static String initwebsite = new String(website) ;
   private static String initsplashdir = new String(splashdir) ;
   private static String initicondir = new String(icondir) ;
   private static String initonlinehelp = new String(onlinehelp) ;
   private static Object initlanguage = language ;
   private static Object initencoding = encoding ;
   private static Object initexporttype= exporttype ;
   private static Object initbrowser= browser ;

   // Supported language settings and file encodings.

   private static String [] languages = new String []
   { "English", "Japanese", "French", "German", "Dutch", "Italian", "Spanish" } ;
   private static String [] encodings = new String []
   { "Cp1252", "ISO8859_1", "MS932", "SJIS", "EUC_JP", "UTF-8", "UTF-16" } ;
   private Object[] languageitems = {
      new ComboItem("Dutch",true),
      new ComboItem("English",true),
      new ComboItem("French",true),
      new ComboItem("German",false),
      new ComboItem("Italian",false),
      new ComboItem("Japanese",true),
      new ComboItem("Spanish",true)
    };
   private Object[] browsers = {
      "", "netscape", "mozilla", "firefox", 
      "konqueror", "opera", "epiphany", 
      "iexplore.exe", "netscape.exe", "firefox.exe",
      "Internet Explorer.app", "Safari.app", "Firefox.app"
    };

   // Supported export file image types.

   public static String [] exporttypes = new String []
   { "BMP", "CEL", "GIF", "JPG", "PNG", "PBM", "PPM", "PGM" } ;

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JTabbedPane jTabbedPane1 = new JTabbedPane();
	private JTabbedPane jTabbedPane2 = new JTabbedPane();
	private FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEADING,0,0);
	private FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER);
	private FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEADING,0,0);
	private BorderLayout borderLayout3 = new BorderLayout();
   private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout6 = new BorderLayout();
	private BorderLayout borderLayout7 = new BorderLayout();
	private BorderLayout borderLayout8 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridLayout gridLayout5 = new GridLayout();
	private GridLayout gridLayout6 = new GridLayout();
	private GridLayout gridLayout7 = new GridLayout();
	private GridLayout gridLayout8 = new GridLayout();
	private GridLayout gridLayout9 = new GridLayout();
	private GridLayout gridLayout10 = new GridLayout();
	private GridLayout gridLayout11 = new GridLayout();
	private GridLayout gridLayout12 = new GridLayout();
	private GridLayout gridLayout13 = new GridLayout();
	private GridLayout gridLayout14 = new GridLayout();
	private GridLayout gridLayout15 = new GridLayout();
	private GridLayout gridLayout16 = new GridLayout();
	private GridLayout gridLayout17 = new GridLayout();
	private GridLayout gridLayout18 = new GridLayout();
	private GridLayout gridLayout19 = new GridLayout();
	private GridLayout gridLayout20 = new GridLayout();
	private GridLayout gridLayout21 = new GridLayout();
	private GridLayout gridLayout22 = new GridLayout();
	private GridLayout gridLayout23 = new GridLayout();
	private GridLayout gridLayout24 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
   private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private GridBagLayout gridBagLayout5 = new GridBagLayout();
	private GridBagLayout gridBagLayout6 = new GridBagLayout();
	private GridBagLayout gridBagLayout7 = new GridBagLayout();
	private GridBagLayout gridBagLayout8 = new GridBagLayout();
	private GridBagLayout gridBagLayout9 = new GridBagLayout();
	private GridBagLayout gridBagLayout10 = new GridBagLayout();
	private GridBagLayout gridBagLayout11 = new GridBagLayout();
	private GridBagLayout gridBagLayout12 = new GridBagLayout();
	private GridBagLayout gridBagLayout13 = new GridBagLayout();
	private GridBagLayout gridBagLayout14 = new GridBagLayout();
	private GridBagLayout gridBagLayout15 = new GridBagLayout();
	private GridBagLayout gridBagLayout16 = new GridBagLayout();
	private GridBagLayout gridBagLayout17 = new GridBagLayout();
	private GridBagLayout gridBagLayout18 = new GridBagLayout();
	private GridBagLayout gridBagLayout19 = new GridBagLayout();
	private GridBagLayout gridBagLayout20 = new GridBagLayout();
	private GridBagLayout gridBagLayout21 = new GridBagLayout();
	private GridBagLayout gridBagLayout22 = new GridBagLayout();
	private GridBagLayout gridBagLayout23 = new GridBagLayout();
	private GridBagLayout gridBagLayout24 = new GridBagLayout();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
   private JPanel jPanel5a = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new JPanel();
	private JPanel jPanel8 = new JPanel();
	private JPanel jPanel9 = new JPanel();
   private JPanel jPanel10 = new JPanel();
	private JPanel jPanel11 = new JPanel();
   private JPanel jPanel12 = new JPanel();
	private JPanel jPanel13 = new JPanel();
	private JPanel jPanel14 = new JPanel();
	private JPanel jPanel15 = new JPanel();
	private JPanel jPanel16 = new JPanel();
	private JPanel jPanel17 = new JPanel();
	private JPanel jPanel18 = new JPanel();
	private JPanel jPanel19 = new JPanel();
	private JPanel jPanel20 = new JPanel();
	private JPanel jPanel21 = new JPanel();
	private JPanel jPanel22 = new JPanel();
	private JPanel jPanel23 = new JPanel();
	private JPanel jPanel24 = new JPanel();
	private JPanel jPanel25 = new JPanel();
	private JPanel jPanel26 = new JPanel();
	private JPanel jPanel27 = new JPanel();
	private JPanel jPanel28 = new JPanel();
	private JPanel jPanel29 = new JPanel();
	private JPanel jPanel30 = new JPanel();
	private JPanel jPanel31 = new JPanel();
	private JPanel jPanel32 = new JPanel();
	private JPanel jPanel33 = new JPanel();
	private JPanel jPanel34 = new JPanel();
	private JPanel jPanel35 = new JPanel();
	private JPanel jPanel36 = new JPanel();
	private JPanel jPanel37 = new JPanel();
	private JPanel jPanel38 = new JPanel();
	private JPanel jPanel39 = new JPanel();
	private JPanel jPanel40 = new JPanel();
	private JPanel jPanel41 = new JPanel();
	private JPanel jPanel42 = new JPanel();
	private JPanel jPanel43 = new JPanel();
	private JPanel jPanel44 = new JPanel();
	private JPanel jPanel45 = new JPanel();
	private JPanel jPanel46 = new JPanel();
	private JPanel jPanel47 = new JPanel();
	private JPanel jPanel48 = new JPanel();
	private JPanel jPanel49 = new JPanel();
	private JPanel jPanel50 = new JPanel();
	private JPanel jPanel51 = new JPanel();
	private JPanel jPanel52 = new JPanel();
	private JPanel jPanel110 = new JPanel();
	private JPanel jPanel111 = new JPanel();
	private JPanel jPanel112 = new JPanel();
	private JPanel jPanel113 = new JPanel();
	private JPanel jPanel114 = new JPanel();
	private JButton CANCEL = new JButton();
	private JButton APPLY = new JButton();
	private JButton RESET = new JButton();
	private JButton SAVE = new JButton();
	private JButton OK = new JButton();
	private JButton LogEdit = new JButton();
	private JButton LogClear = new JButton();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JLabel jLabel6 = new JLabel();
	private JLabel jLabel7 = new JLabel();
	private JLabel jLabel8 = new JLabel();
	private JLabel jLabel9 = new JLabel();
	private JLabel jLabel10 = new JLabel();
	private JLabel jLabel11 = new JLabel();
	private JLabel jLabel12 = new JLabel();
   private JLabel jLabel13 = new JLabel();
   private JLabel jLabel14 = new JLabel();
   private JLabel jLabel15 = new JLabel();
   private JLabel jLabel16 = new JLabel();
   private JLabel jLabel17 = new JLabel();
   private JLabel jLabel18 = new JLabel();
   private JLabel jLabel19 = new JLabel();
   private JLabel jLabel20 = new JLabel();
   private JLabel jLabel21 = new JLabel();
	private JLabel jLabel22 = new JLabel();
	private JLabel jLabel23 = new JLabel();
   private JLabel SplashSetName = new JLabel();
	private JCheckBox MouseDebug = new JCheckBox();
	private JCheckBox ControlDebug = new JCheckBox();
	private JCheckBox LoadDebug = new JCheckBox();
	private JCheckBox EditDebug = new JCheckBox();
	private JCheckBox ImageDebug = new JCheckBox();
	private JCheckBox EventDebug = new JCheckBox();
	private JCheckBox ActionDebug = new JCheckBox();
	private JCheckBox VariableDebug = new JCheckBox();
	private JCheckBox FKissDebug = new JCheckBox();
	private JCheckBox SoundDebug = new JCheckBox();
	private JCheckBox MovieDebug = new JCheckBox();
	private JCheckBox MediaDebug = new JCheckBox();
	private JCheckBox SearchDebug = new JCheckBox();
	private JCheckBox DisabledDebug = new JCheckBox();
	private JCheckBox ComponentDebug = new JCheckBox();
	private JCheckBox JavaSoundDebug = new JCheckBox();
	private JCheckBox SystemLF = new JCheckBox();
	private JCheckBox JavaLF = new JCheckBox();
	private JCheckBox AppleMac = new JCheckBox();
	private JCheckBox Backup = new JCheckBox();
	private JCheckBox SaveSource = new JCheckBox();
	private JCheckBox ShowDLPrompt = new JCheckBox();
	private JCheckBox ShowTips = new JCheckBox();
	private JCheckBox LoadClose = new JCheckBox();
	private JCheckBox SoundOption = new JCheckBox();
	private JCheckBox MovieOption = new JCheckBox();
	private JCheckBox EventOption = new JCheckBox();
	private JCheckBox AnimateOption = new JCheckBox();
	private JCheckBox TimerOption = new JCheckBox();
	private JCheckBox ScaleToFit = new JCheckBox();
	private JCheckBox SizeToFit = new JCheckBox();
	private JCheckBox RetainWindowSize = new JCheckBox();
	private JCheckBox MaximizeWindow = new JCheckBox();
	private JCheckBox RandomSplash = new JCheckBox();
	private JCheckBox ShowBorder = new JCheckBox();
	private JCheckBox InitToolBar = new JCheckBox();
	private JCheckBox TbTools = new JCheckBox() ;			
	private JCheckBox TbColors = new JCheckBox() ;		
	private JCheckBox TbPages = new JCheckBox() ;		
	private JCheckBox TbEdits = new JCheckBox() ;      
	private JCheckBox TbCompat = new JCheckBox() ;		
	private JCheckBox InitStatusBar = new JCheckBox();
	private JCheckBox InitMenuBar = new JCheckBox();
	private JCheckBox ImportCel = new JCheckBox();
	private JCheckBox ExportCel = new JCheckBox();
	private JCheckBox ComponentCel = new JCheckBox();
	private JCheckBox ImportComponent = new JCheckBox();
	private JCheckBox CacheImage = new JCheckBox();
	private JCheckBox CacheAudio = new JCheckBox();
	private JCheckBox CacheAudio1 = new JCheckBox();
	private JCheckBox CacheVideo = new JCheckBox();
	private JCheckBox CacheVideo1 = new JCheckBox();
	private JCheckBox ConstrainMoves = new JCheckBox();
	private JCheckBox ConstrainVisible = new JCheckBox();
	private JCheckBox ConstrainFKiss = new JCheckBox();
	private JCheckBox AutoScroll = new JCheckBox();
	private JCheckBox DragMove = new JCheckBox();
	private JCheckBox ReleaseMove = new JCheckBox();
	private JCheckBox DropFixdrop = new JCheckBox();
	private JCheckBox CatchFixdrop = new JCheckBox();
	private JCheckBox VisibleUnfix = new JCheckBox();
	private JCheckBox EarlyFix = new JCheckBox();
	private JCheckBox DetachRestricted = new JCheckBox();
	private JCheckBox DetachMove = new JCheckBox();
	private JCheckBox DetachFix = new JCheckBox();
	private JCheckBox InvertGhost = new JCheckBox();
	private JCheckBox TransparentGroup = new JCheckBox();
	private JCheckBox MapCollide = new JCheckBox();
	private JCheckBox MoveXYCollide = new JCheckBox();
	private JCheckBox RetainKey = new JCheckBox();
	private JCheckBox StrictSyntax = new JCheckBox();
	private JCheckBox ShowUndefs = new JCheckBox();
	private JCheckBox AutoEndif = new JCheckBox();
	private JCheckBox ImmediateCollide = new JCheckBox();
	private JCheckBox ImmediateUnfix = new JCheckBox();
	private JCheckBox ImmediateEvent = new JCheckBox();
	private JCheckBox SuspendMedia = new JCheckBox();
	private JCheckBox LongSoundMedia = new JCheckBox();
	private JCheckBox AdjustMediaVolume = new JCheckBox();
	private JCheckBox StopMusic = new JCheckBox();
	private JCheckBox SoundSingle = new JCheckBox();
	private JCheckBox AutoMediaLoop = new JCheckBox();
	private JCheckBox AutoFullScreen = new JCheckBox();
	private JCheckBox MediaMinimize = new JCheckBox();
	private JCheckBox MediaCenter = new JCheckBox();
	private JCheckBox MediaMusicResume = new JCheckBox();
	private JCheckBox KeepAspect = new JCheckBox();
	private JCheckBox KeyCase = new JCheckBox();
	private JCheckBox VariableCase = new JCheckBox();
	private JCheckBox ExpandEvents = new JCheckBox();
	private JCheckBox EnableShell = new JCheckBox();
	private JCheckBox XYOffsets = new JCheckBox();
	private JCheckBox AbsoluteWindow = new JCheckBox();
	private JCheckBox PanelVisible = new JCheckBox();
	private JCheckBox MouseInOutBox = new JCheckBox();
	private JCheckBox ContextMap = new JCheckBox();
	private JCheckBox MapCount = new JCheckBox();
	private JCheckBox AllAmbiguous = new JCheckBox();
	private JCheckBox EditEnable = new JCheckBox();
	private JCheckBox SecurityEnable = new JCheckBox();
	private JCheckBox InitEdit = new JCheckBox();
	private JCheckBox DefaultPlayFKiss = new JCheckBox();
	private JCheckBox AcceptCnfErrors = new JCheckBox();
	private JCheckBox PagesAreScenes = new JCheckBox();
	private JCheckBox MultipleEvents = new JCheckBox();
	private JRadioButton AltEditDrag = new JRadioButton();
	private JRadioButton ShiftEditDrag = new JRadioButton();
	private JCheckBox ImportRelative = new JCheckBox();
	private JCheckBox WriteCelOffset = new JCheckBox();
	private JCheckBox ShowBreakPointEnd = new JCheckBox();
	private JCheckBox ShowStepIntoEnd = new JCheckBox();
	private JCheckBox WriteComment = new JCheckBox();
	private JRadioButton EventPause = new JRadioButton();
	private JRadioButton ActionPause = new JRadioButton();
	private JCheckBox DisableAll = new JCheckBox();
	private JComboBox LogFileBox = new JComboBox();
   private JComboBox LanguageBox = new JComboBox(languageitems) ;
   private JComboBox EncodingBox = new JComboBox(encodings) ;
   private JComboBox ExportBox = new JComboBox(exporttypes) ;
   private JComboBox BrowserBox = new JComboBox(browsers) ;
	private JTextField TimerPeriod = new JTextField();
	private JTextField GifPeriod = new JTextField();
	private JTextField ScenePeriod = new JTextField();
	private JTextField AudioPeriod = new JTextField();
	private JTextField EventQueues = new JTextField();
	private JTextField MaxFlex = new JTextField();
	private JTextField MaxLock = new JTextField();
	private JTextField StickyFlex = new JTextField();
	private JTextField MaxPageSet = new JTextField();
	private JTextField MaxColorSet = new JTextField();
	private JTextField UndoLimit = new JTextField();
	private JTextField CommentCol = new JTextField();
	private JTextField IndentSpace = new JTextField();
	private JTextField MaxLruFiles = new JTextField();
	private JTextField LongDuration = new JTextField();
	private JTextField MediaVolume = new JTextField();
	private JTextField KissWeb = new JTextField();
	private JTextField UserDirectory = new JTextField();
	private JTextField WebSite = new JTextField();
	private JTextField OnlineHelp = new JTextField();
	private JTextField SplashDirectory = new JTextField();
	private JTextField IconDirectory = new JTextField();
	private JList IconList = new JList();
	private JList SplashList = new JList();
   private JScrollPane IconScrollPane = new JScrollPane(IconList) ;
   private JScrollPane SplashScrollPane = new JScrollPane(SplashList) ;
   private JButton SplashBtn = new JButton() ;
   private JButton IconBtn = new JButton() ;
   private JButton HelpBtn = new JButton() ;
   private JButton UserBtn = new JButton() ;
   private JButton PortalBtn = new JButton() ;
   private JButton ClearLruBtn = new JButton() ;
   private JToggleButton PlayFKissBtn = new JToggleButton() ;
   private JToggleButton DirectKissBtn = new JToggleButton() ;
   private JToggleButton GnomeKissBtn = new JToggleButton() ;
   private JToggleButton KissLDBtn = new JToggleButton() ;

   // WebSearch User interface objects.

	private JPanel panel1ws = new JPanel();
   private FlowLayout flowLayout1ws = new FlowLayout() ;
   private GridLayout gridLayout1ws = new GridLayout() ;
	private GridBagLayout gridBagLayout1ws = new GridBagLayout();
	private GridBagLayout gridBagLayout2ws = new GridBagLayout();
   private GridBagLayout gridBagLayout3ws = new GridBagLayout();
	private JPanel jPanel1ws = new JPanel();
	private JPanel jPanel2ws = new JPanel();
	private JPanel jPanel3ws = new JPanel();
	private JPanel jPanel4ws = new JPanel();
	private JLabel jLabel1ws = new JLabel();
	private JLabel jLabel2ws = new JLabel();
   private JLabel jLabel3ws = new JLabel();
   private JLabel jLabel4ws = new JLabel();
   private JLabel jLabel5ws = new JLabel();
   private JLabel jLabel6ws = new JLabel();
	private JLabel jLabel7ws = new JLabel();
	private JLabel jLabel8ws = new JLabel();
	private JLabel jLabel9ws = new JLabel();
	private JLabel jLabel10ws = new JLabel();
	private JCheckBox UseDefaultWS = new JCheckBox();
	private JCheckBox ClearMaster = new JCheckBox();
	private JCheckBox SaveArchive = new JCheckBox();
	private JCheckBox SaveAsZip = new JCheckBox();
	private JCheckBox SaveImage = new JCheckBox();
	private JCheckBox ShowLoad = new JCheckBox();
	private JTextField ImageDirectory = new JTextField();
	private JTextField HtmlDirectory = new JTextField();
   private JTextField DataDirectory = new JTextField();
	private JTextField KissIndex = new JTextField();
	private JTextField DownloadSize = new JTextField();
	private JTextField ThumbWidth = new JTextField();
	private JTextField ThumbHeight = new JTextField();
	private JTextField ThumbPage = new JTextField();



	// Constructor

	public OptionsDialog()
	{
		this(null,Kisekae.getCaptions().getString("OptionsDialogTitle"),false) ;
	}
   
	public OptionsDialog(JFrame frame)
	{
		this(frame,Kisekae.getCaptions().getString("OptionsDialogTitle"),false) ;
	}
   
	public OptionsDialog(JFrame frame,int tab)
	{
		this(frame,Kisekae.getCaptions().getString("OptionsDialogTitle"),false) ;
      jTabbedPane1.setSelectedIndex(tab) ;
	}

	public OptionsDialog(JFrame frame, String title, boolean modal)
	{
		super(frame, title, modal) ;
      me = this ;

		// Construct the user interface.

		try { jbInit(); pack() ; }
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
      
      // Read initial properties.
      
      loadPropertyOptions() ;
      
      // Set the available icons.  Index from 1.
      
      DefaultListModel model1 = new DefaultListModel() ;
      IconList.setModel(model1) ;
      IconList.setVisibleRowCount(1) ;
      String s2 = getIconDir() ;
      for (int n = 1 ; ; n++)
      {
         try
         {
            int m = s2.indexOf('.') ;
            if (m < 0) break ;
            String s1 = s2.substring(0,m) + n + "-32" + s2.substring(m) ;
            URL iconurl =  Kisekae.getResource(s1) ;
            if (iconurl == null) break ;
            ImageIcon icon = new ImageIcon(iconurl) ;
            model1.addElement(icon) ;
         }
         catch (Exception e) { break ; }
      }
      if (model1.size() == 0) 
      {
         icondir = "" ;
         jPanel32.setVisible(false) ;
      }
      
      // Set the available splash options.  Index from 1.
      
      DefaultListModel model2 = new DefaultListModel() ;
      SplashList.setModel(model2) ;
      SplashList.setVisibleRowCount(1) ;
      s2 = getSplashDir() ;
      
      String directory = "" ;
      String extension = "" ;
      try
      {
         int m = s2.lastIndexOf('.') ;
         extension = (m > 0) ? s2.substring(m) : ".jpg" ;
         int n = s2.lastIndexOf('/') ;
         if (n < 0) n = s2.lastIndexOf('\\') ;
         if (n < 0) n = s2.length() ;
         directory = s2.substring(0,n) ;  // directory
      }
      catch (IndexOutOfBoundsException e) { }
      
      try
      {
         URL reference = Kisekae.getResource(directory+"/index.txt") ;
         if (reference != null)
         {
            int i = 0 ;
            InputStream input = reference.openStream();
            Scanner scanner = new Scanner(input) ;
            while (scanner.hasNextLine())
            {
               String line = scanner.nextLine() ;
               if (line.isEmpty()) continue ;
               if (line.startsWith(";")) continue ;
               line = line.trim() ;
               String s3 = directory + "/" + line + "-icon" + extension ;
               URL iconurl =  Kisekae.getResource(s3) ;
               if (iconurl != null) 
               {
                  ImageIcon icon = new ImageIcon(iconurl) ;
                  model2.addElement(icon) ;
                  backsetnames.addElement(line) ;
                  i = i + 1 ;
               }
            }
         }
      }
      catch (Exception e) { }
      
      // The SplashSetNumber is the definitive set to show.  
      // If the SplashDir is not correct set it to the correct 
      // directory and back image set value.
      
      int backset = getSplashSetNumber() ;
      String s1 = getSplashSetName(backset-1) ;
      SplashSetName.setText(s1) ;
      if ("".equals(s1)) 
         setSplashDir(Kisekae.getSplashDir()) ;
      else
      {
         s1 = Kisekae.getSplashDir() + s1 + ".jpg" ;
         if (!s1.equals(s2)) setSplashDir(s1) ;
      }
      
      if (model2.size() == 0) 
      {
         splashdir = "" ;
         jPanel50.setVisible(false) ;
         jPanel52.setVisible(false) ;
      }

		// Set initial option values.

	   ButtonGroup bglf = new ButtonGroup() ;
      bglf.add(SystemLF) ;
      bglf.add(JavaLF) ;
	   ButtonGroup bgfkpause = new ButtonGroup() ;
      bgfkpause.add(EventPause) ;
      bgfkpause.add(ActionPause) ;
	   ButtonGroup bgeditdrag = new ButtonGroup() ;
      bgeditdrag.add(AltEditDrag) ;
      bgeditdrag.add(ShiftEditDrag) ;
      savesource = initsavesource = !Kisekae.inApplet() ;
		setControls() ;

		// Center the dialog frame in the screen.

      pack() ;
 		center(this) ;
      IconList.ensureIndexIsVisible(getIconNumber()-1) ;
      SplashList.ensureIndexIsVisible(getSplashSetNumber()-1) ;

		// Establish event listeners

		addWindowListener(this);
		OK.addActionListener(this);
		APPLY.addActionListener(this);
		RESET.addActionListener(this);
		SAVE.addActionListener(this);
		CANCEL.addActionListener(this);
      LogEdit.addActionListener(this) ;
      LogClear.addActionListener(this) ;
      PlayFKissBtn.addActionListener(this) ;
      DirectKissBtn.addActionListener(this) ;
      GnomeKissBtn.addActionListener(this) ;
      KissLDBtn.addActionListener(this) ;
      jTabbedPane1.addChangeListener(this) ;
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
		Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
		Border eb2 = BorderFactory.createEmptyBorder(10,10,10,10) ;
		Border eb3 = BorderFactory.createEmptyBorder(0,0,0,0) ;
      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb3 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb4 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb5 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border tb1 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("UserInterfaceBoxText")),eb1) ;
      Border tb2 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("FileAccessBoxText")),eb1) ;
      Border tb3 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("EventsBoxText")),eb1) ;
      Border tb4 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("TimersBoxText")),eb1) ;
      Border tb5 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("FlexLimitsBoxText")),eb1) ;
      Border tb6 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("ToolbarLimitsBoxText")),eb1) ;
      Border tb7 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("LanguageBoxText")),eb1) ;
      Border tb8 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("InitStateBoxText")),eb1) ;
      Border tb9 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("MemoryCacheBoxText")),eb1) ;
      Border tb10 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("MovementBoxText")),eb1) ;
      Border tb11 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("VariablesBoxText")),eb1) ;
      Border tb12 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("MediaPlayerBoxText")),eb1) ;
      Border tb13 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("MusicControlBoxText")),eb1) ;
      Border tb14 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("EditControlBoxText")),eb1) ;
      Border tb15 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("EditLimitsBoxText")),eb1) ;
      Border tb16 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("ControlSettingsBoxText")),eb1) ;
      Border tb17 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("FKissEditorBoxText")),eb1) ;
      Border tb18 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WebSiteBoxText")),eb1) ;
      Border tb19 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("DirectoryBoxText")),eb1) ;
      Border tb20 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("IconBoxText")),eb1) ;
      Border tb21 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("LruFileBoxText")),eb1) ;
      Border tb22 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WindowBoxText")),eb1) ;
      Border tb23 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("ImportBoxText")),eb1) ;
      Border tb24 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("PresetsBoxText")),eb1) ;
      Border tb25 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("ExportBoxText")),eb1) ;
      Border tb26 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("ToolbarBoxText")),eb1) ;
      Border tb27 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("BrowserBoxText")),eb1) ;
      Border tb28 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("SplashBoxText")),eb1) ;

      jTabbedPane1.setBorder(eb2);

		gridLayout1.setColumns(1);
		gridLayout1.setRows(4);
		gridLayout2.setColumns(1);
		gridLayout2.setRows(3);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(3);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(4);
		gridLayout5.setColumns(2);
		gridLayout5.setRows(8);
		gridLayout6.setRows(3);
		gridLayout6.setColumns(1);
		gridLayout7.setColumns(1);
		gridLayout7.setRows(3);
		gridLayout8.setColumns(3);
		gridLayout8.setRows(3);
		gridLayout9.setColumns(2);
		gridLayout9.setRows(3);
		gridLayout10.setColumns(1);
		gridLayout10.setRows(10);
		gridLayout11.setColumns(1);
		gridLayout11.setRows(3);
		gridLayout12.setColumns(1);
		gridLayout12.setRows(2);
		gridLayout13.setColumns(1);
		gridLayout13.setRows(1);
		gridLayout14.setColumns(2);
		gridLayout14.setRows(2);
		gridLayout15.setColumns(1);
		gridLayout15.setRows(3);
		gridLayout16.setColumns(1);
		gridLayout16.setRows(5);
		gridLayout17.setColumns(1);
		gridLayout17.setRows(3);
		gridLayout18.setColumns(1);
		gridLayout18.setRows(3);
		gridLayout19.setColumns(1);
		gridLayout19.setRows(3);
		gridLayout20.setColumns(1);
		gridLayout20.setRows(10);
		gridLayout21.setColumns(1);
		gridLayout21.setRows(11);
		gridLayout22.setColumns(1);
		gridLayout22.setRows(10);
		gridLayout23.setColumns(1);
		gridLayout23.setRows(5);
		gridLayout24.setColumns(1);
		gridLayout24.setRows(2);
      
		panel1.setLayout(gridBagLayout4);
		jPanel1.setBorder(eb1);
		jPanel1.setLayout(gridBagLayout5);
		jPanel2.setBorder(eb1);
		jPanel2.setLayout(gridBagLayout1);
		jPanel3.setBorder(eb1);
		jPanel3.setLayout(borderLayout3);
		jPanel4.setBorder(eb1);
		jPanel4.setLayout(new BoxLayout(jPanel4,BoxLayout.X_AXIS));
		jPanel5.setBorder(cb1);
		jPanel5.setLayout(gridLayout5);
      jPanel5a.setLayout(borderLayout4);
		jPanel6.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
		jPanel6.setLayout(borderLayout6);
		jPanel7.setBorder(tb2);
		jPanel7.setLayout(gridLayout1);
		jPanel8.setBorder(tb1);
		jPanel8.setLayout(gridLayout2);
		jPanel9.setBorder(BorderFactory.createEmptyBorder(10,20,10,0));
		jPanel9.setLayout(new BoxLayout(jPanel9,BoxLayout.Y_AXIS));
      jPanel10.setBorder(tb4);
		jPanel10.setLayout(gridBagLayout2);
      jPanel11.setBorder(tb3);
		jPanel11.setLayout(gridBagLayout3);
		jPanel12.setLayout(gridBagLayout6);
		jPanel13.setBorder(tb5);
		jPanel13.setLayout(gridBagLayout7);
		jPanel14.setLayout(gridBagLayout8);
		jPanel14.setBorder(tb6);
		jPanel15.setLayout(gridLayout3);
		jPanel15.setBorder(tb7);
		jPanel16.setBorder(tb8);
		jPanel16.setLayout(gridLayout4);
		jPanel17.setLayout(gridLayout6);
		jPanel17.setBorder(tb9);
		jPanel18.setLayout(gridLayout7);
		jPanel18.setBorder(tb10);
		jPanel19.setBorder(eb1);
		jPanel19.setLayout(gridBagLayout9);
		jPanel20.setBorder(eb1);
		jPanel20.setLayout(gridBagLayout11);
		jPanel21.setBorder(tb14);
		jPanel21.setLayout(gridBagLayout12);
		jPanel22.setBorder(tb15);
		jPanel22.setLayout(gridBagLayout13);
		jPanel23.setLayout(gridBagLayout14);
		jPanel24.setLayout(gridLayout10);
		jPanel24.setBorder(eb1);
		jPanel25.setBorder(tb16);
		jPanel25.setLayout(gridBagLayout15);
  		jPanel26.setBorder(cb2);
		jPanel26.setLayout(gridLayout11);
		jPanel27.setBorder(cb3);
		jPanel27.setLayout(gridLayout12);
		jPanel28.setBorder(tb18);
		jPanel28.setLayout(gridLayout13);
 		jPanel29.setLayout(new BoxLayout(jPanel29,BoxLayout.X_AXIS));
		jPanel110.setBorder(tb12);
		jPanel110.setLayout(gridLayout8);
		jPanel111.setLayout(gridBagLayout10);
		jPanel111.setBorder(tb11);
		jPanel112.setBorder(tb13);
		jPanel112.setLayout(gridLayout9);
		jPanel113.setLayout(flowLayout1);
		jPanel114.setLayout(flowLayout3);
		jPanel30.setLayout(gridBagLayout16);
		jPanel30.setBorder(eb1);
		jPanel31.setBorder(tb19);
		jPanel31.setLayout(gridBagLayout17);
		jPanel32.setBorder(tb20);
		jPanel32.setLayout(borderLayout7);
		jPanel33.setBorder(tb21);
		jPanel33.setLayout(gridBagLayout18);
		jPanel34.setBorder(cb5);
		jPanel34.setLayout(gridLayout15);
		jPanel36.setBorder(eb1);
		jPanel36.setLayout(gridBagLayout19);
		jPanel37.setBorder(tb22);
		jPanel37.setLayout(gridLayout16);
		jPanel38.setBorder(eb1);
		jPanel38.setLayout(gridBagLayout20);
		jPanel39.setBorder(tb23);
		jPanel39.setLayout(gridLayout17);
		jPanel40.setBorder(tb24);
		jPanel40.setLayout(gridBagLayout21);
		jPanel41.setBorder(tb25);
		jPanel41.setLayout(gridLayout19);
		jPanel42.setBorder(eb1);
		jPanel42.setLayout(gridLayout20);
		jPanel43.setBorder(eb1);
		jPanel43.setLayout(gridLayout21);
		jPanel44.setBorder(eb1);
		jPanel44.setLayout(gridLayout22);
		jPanel45.setLayout(new BoxLayout(jPanel45,BoxLayout.X_AXIS));
		jPanel46.setBorder(eb1);
		jPanel46.setLayout(gridBagLayout22);
		jPanel47.setBorder(tb26);
		jPanel47.setLayout(gridLayout23);
		jPanel48.setBorder(tb27);
		jPanel48.setLayout(gridBagLayout23);
		jPanel49.setBorder(eb1);
		jPanel49.setLayout(gridBagLayout24);
		jPanel50.setBorder(tb28);
		jPanel50.setLayout(borderLayout8);
		jPanel51.setLayout(flowLayout2);
		jPanel52.setLayout(gridLayout24);

		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		APPLY.setText(Kisekae.getCaptions().getString("ApplyMessage"));
		RESET.setText(Kisekae.getCaptions().getString("ResetMessage"));
		SAVE.setText(Kisekae.getCaptions().getString("SaveMessage"));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      PlayFKissBtn.setText("PlayFKiss") ;
      DirectKissBtn.setText("DirectKiss") ;
      GnomeKissBtn.setText("GnomeKiss") ;
      KissLDBtn.setText("KissLD") ;
		SoundOption.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundOption"));
		SoundOption.setText(Kisekae.getCaptions().getString("OptionsEnableSounds"));
		SoundOption.setSelected(sound);
		MovieOption.setToolTipText(Kisekae.getCaptions().getString("ToolTipMovieOption"));
		MovieOption.setText(Kisekae.getCaptions().getString("OptionsEnableMovies"));
		MovieOption.setSelected(movie);
		EventOption.setText(Kisekae.getCaptions().getString("OptionsEnableEvents"));
      EventOption.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventOption"));
		EventOption.setSelected(event);
		MouseDebug.setText(Kisekae.getCaptions().getString("OptionsShowObjectSelection"));
      MouseDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipMouseDebug"));
		MouseDebug.setSelected(debugmouse);
		ControlDebug.setText(Kisekae.getCaptions().getString("OptionsLogControlMessages"));
      ControlDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipControlDebug"));
		ControlDebug.setSelected(debugcontrol);
		LoadDebug.setText(Kisekae.getCaptions().getString("OptionsLogLoadMessages"));
      LoadDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipLoadDebug"));
		LoadDebug.setSelected(debugload);
		EditDebug.setText(Kisekae.getCaptions().getString("OptionsLogEditMessages"));
      EditDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditDebug"));
		EditDebug.setSelected(debugedit);
		ImageDebug.setText(Kisekae.getCaptions().getString("OptionsLogImageTransforms"));
      ImageDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageDebug"));
		ImageDebug.setSelected(debugimage);
		EventDebug.setText(Kisekae.getCaptions().getString("OptionsLogEventCommands"));
      EventDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventDebug"));
		EventDebug.setSelected(debugevent);
		ActionDebug.setText(Kisekae.getCaptions().getString("OptionsLogActionCommands"));
      ActionDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipActionDebug"));
		ActionDebug.setSelected(debugaction);
      VariableDebug.setText(Kisekae.getCaptions().getString("OptionsLogVariableAssignments"));
		VariableDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipVariableDebug"));
		VariableDebug.setSelected(debugvariable);
		FKissDebug.setText(Kisekae.getCaptions().getString("OptionsLogFKissBreakpoints"));
		FKissDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipFKissDebug"));
		FKissDebug.setSelected(debugfkiss);
		SoundDebug.setText(Kisekae.getCaptions().getString("OptionsLogAudioPlaybackEvents"));
		SoundDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundDebug"));
		SoundDebug.setSelected(debugsound);
		MovieDebug.setText(Kisekae.getCaptions().getString("OptionsLogVideoPlaybackEvents"));
		MovieDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipMovieDebug"));
		MovieDebug.setSelected(debugmovie);
		MediaDebug.setText(Kisekae.getCaptions().getString("OptionsLogMediaStreamEvents"));
		MediaDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipMediaDebug"));
		MediaDebug.setSelected(debugmedia);
		SearchDebug.setText(Kisekae.getCaptions().getString("OptionsLogSearchDebug"));
		SearchDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipSearchDebug"));
		SearchDebug.setSelected(debugsearch);
		DisabledDebug.setText(Kisekae.getCaptions().getString("OptionsLogDisabledDebug"));
		DisabledDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipDisabledDebug"));
		DisabledDebug.setSelected(debugdisabled);
		ComponentDebug.setText(Kisekae.getCaptions().getString("OptionsLogComponentDebug"));
		ComponentDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipComponentDebug"));
		ComponentDebug.setSelected(debugcomponent);
		jLabel2.setPreferredSize(new Dimension(60, 17));
		jLabel2.setText(Kisekae.getCaptions().getString("OptionsDialogLogFileText"));
		jLabel2.setToolTipText(Kisekae.getCaptions().getString("ToolTipLogFile"));
		LogFileBox.setEditable(false);
		LogFileBox.addItem(LogFile.getLogFileName());
		LogEdit.setText(Kisekae.getCaptions().getString("LogEditMessage"));
      LogEdit.setToolTipText(Kisekae.getCaptions().getString("ToolTipLogEdit"));
		LogEdit.setEnabled(LogFile.isOpen());
		LogClear.setText(Kisekae.getCaptions().getString("LogClearMessage"));
      LogClear.setToolTipText(Kisekae.getCaptions().getString("ToolTipLogClear"));
		LogClear.setEnabled(LogFile.isOpen());
		jLabel3.setText(Kisekae.getCaptions().getString("TimerPeriodText"));
		jLabel3.setToolTipText(Kisekae.getCaptions().getString("ToolTipTimerPeriod"));
		jLabel8.setText(Kisekae.getCaptions().getString("AnimationPeriodText"));
		jLabel8.setToolTipText(Kisekae.getCaptions().getString("ToolTipAnimationPeriod"));
		jLabel22.setText(Kisekae.getCaptions().getString("ScenePeriodText"));
		jLabel22.setToolTipText(Kisekae.getCaptions().getString("ToolTipScenePeriod"));
		jLabel23.setText(Kisekae.getCaptions().getString("AudioPeriodText"));
		jLabel23.setToolTipText(Kisekae.getCaptions().getString("ToolTipAudioPeriod"));
		jLabel4.setText(Kisekae.getCaptions().getString("EventHandlersText"));
		jLabel4.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventHandlers"));
		SystemLF.setText(Kisekae.getCaptions().getString("OptionsSystemLF"));
		SystemLF.setToolTipText(Kisekae.getCaptions().getString("ToolTipSystemLF"));
		SystemLF.setSelected(systemlf);
		JavaLF.setText(Kisekae.getCaptions().getString("OptionsJavaLF"));
		JavaLF.setToolTipText(Kisekae.getCaptions().getString("ToolTipJavaLF"));
		JavaLF.setSelected(javalf);
		AppleMac.setText(Kisekae.getCaptions().getString("OptionsAppleMac"));
		AppleMac.setToolTipText(Kisekae.getCaptions().getString("ToolTipAppleLF"));
		AppleMac.addActionListener(this);
		Backup.setText(Kisekae.getCaptions().getString("OptionsFileBackup"));
		Backup.setToolTipText(Kisekae.getCaptions().getString("ToolTipBackupFile"));
		Backup.setSelected(backup);
		SaveSource.setText(Kisekae.getCaptions().getString("OptionsSaveSource"));
		SaveSource.setToolTipText(Kisekae.getCaptions().getString("ToolTipSaveSource"));
		SaveSource.setSelected(savesource);
		ShowDLPrompt.setText(Kisekae.getCaptions().getString("OptionsShowDownloadPrompt"));
		ShowDLPrompt.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowDLPrompt"));
		ShowDLPrompt.setSelected(showdlprompt);
		ShowDLPrompt.addActionListener(this);
		ShowTips.setText(Kisekae.getCaptions().getString("OptionsShowTips"));
		ShowTips.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowTips"));
		ShowTips.setSelected(showtips);
		ShowTips.addActionListener(this);
		LoadClose.setText(Kisekae.getCaptions().getString("OptionsLoadClose"));
		LoadClose.setToolTipText(Kisekae.getCaptions().getString("ToolTipLoadClose"));
		LoadClose.setSelected(loadclose);
		TimerPeriod.setPreferredSize(new Dimension(40, 21));
		TimerPeriod.setHorizontalAlignment(SwingConstants.RIGHT);
		TimerPeriod.setText(timerperiod);
		GifPeriod.setPreferredSize(new Dimension(40, 21));
		GifPeriod.setHorizontalAlignment(SwingConstants.RIGHT);
		GifPeriod.setText(gifperiod);
		ScenePeriod.setPreferredSize(new Dimension(40, 21));
		ScenePeriod.setHorizontalAlignment(SwingConstants.RIGHT);
		ScenePeriod.setText(sceneperiod);
		AudioPeriod.setPreferredSize(new Dimension(40, 21));
		AudioPeriod.setHorizontalAlignment(SwingConstants.RIGHT);
		AudioPeriod.setText(audioperiod);
		EventQueues.setPreferredSize(new Dimension(40, 21));
		EventQueues.setHorizontalAlignment(SwingConstants.RIGHT);
		EventQueues.setText(eventqueues);
		TimerOption.setText(Kisekae.getCaptions().getString("OptionsEnableTimer"));
		TimerOption.setToolTipText(Kisekae.getCaptions().getString("ToolTipTimerOption"));
		TimerOption.setSelected(timer);
		TimerOption.addActionListener(this);
		jLabel1.setText(Kisekae.getCaptions().getString("FixedLockValueText"));
		jLabel1.setToolTipText(Kisekae.getCaptions().getString("ToolTipFixedLock"));
		MaxFlex.setPreferredSize(new Dimension(50, 21));
		MaxFlex.setHorizontalAlignment(SwingConstants.RIGHT);
		MaxFlex.setText(maxflex);
		jLabel12.setText(Kisekae.getCaptions().getString("MaximumLockValueText"));
		jLabel12.setToolTipText(Kisekae.getCaptions().getString("ToolTipMaxLock"));
		MaxLock.setPreferredSize(new Dimension(50, 21));
		MaxLock.setHorizontalAlignment(SwingConstants.RIGHT);
		MaxLock.setText(maxlock);
		jLabel5.setText(Kisekae.getCaptions().getString("StickyFlexAreaText"));
		jLabel5.setToolTipText(Kisekae.getCaptions().getString("ToolTipStickyLimit"));
		StickyFlex.setPreferredSize(new Dimension(50, 21));
		StickyFlex.setHorizontalAlignment(SwingConstants.RIGHT);
		StickyFlex.setText(stickyflex);
		jLabel6.setText(Kisekae.getCaptions().getString("PageSetsText"));
		jLabel6.setToolTipText(Kisekae.getCaptions().getString("ToolTipPageSetLimit"));
		MaxPageSet.setPreferredSize(new Dimension(30, 21));
		MaxPageSet.setHorizontalAlignment(SwingConstants.RIGHT);
		MaxPageSet.setText(maxpageset);
		jLabel7.setText(Kisekae.getCaptions().getString("ColorSetsText"));
		jLabel7.setToolTipText(Kisekae.getCaptions().getString("ToolTipColorSetLimit"));
		MaxColorSet.setPreferredSize(new Dimension(30, 21));
		MaxColorSet.setHorizontalAlignment(SwingConstants.RIGHT);
		MaxColorSet.setText(maxcolorset);
		MaxLruFiles.setPreferredSize(new Dimension(30, 21));
		MaxLruFiles.setHorizontalAlignment(SwingConstants.RIGHT);
		MaxLruFiles.setText(maxlrufiles);
		LongDuration.setPreferredSize(new Dimension(30, 21));
		LongDuration.setHorizontalAlignment(SwingConstants.RIGHT);
		LongDuration.setText(longduration);
		MediaVolume.setPreferredSize(new Dimension(30, 21));
		MediaVolume.setHorizontalAlignment(SwingConstants.RIGHT);
		MediaVolume.setText(mediavolume);
		AnimateOption.setText(Kisekae.getCaptions().getString("OptionsEnableAnimation"));
		AnimateOption.setToolTipText(Kisekae.getCaptions().getString("ToolTipAnimateOption"));
		AnimateOption.setSelected(animate);
		ScaleToFit.setText(Kisekae.getCaptions().getString("OptionsScaleToFit"));
		ScaleToFit.setToolTipText(Kisekae.getCaptions().getString("ToolTipScaleToFit"));
		ScaleToFit.setSelected(scaletofit);
		ScaleToFit.addActionListener(this);
		SizeToFit.setText(Kisekae.getCaptions().getString("OptionsSizeToFit"));
		SizeToFit.setToolTipText(Kisekae.getCaptions().getString("ToolTipSizeToFit"));
		SizeToFit.setSelected(sizetofit);
		SizeToFit.addActionListener(this);
		RetainWindowSize.setText(Kisekae.getCaptions().getString("OptionsRetainWindowSize"));
		RetainWindowSize.setToolTipText(Kisekae.getCaptions().getString("ToolTipRetainWindowSize"));
		RetainWindowSize.setSelected(retainwindowsize);
		MaximizeWindow.setText(Kisekae.getCaptions().getString("OptionsMaximizeWindow"));
		MaximizeWindow.setToolTipText(Kisekae.getCaptions().getString("ToolTipMaximizeWindow"));
		MaximizeWindow.setSelected(maximizewindow);
		RandomSplash.setText(Kisekae.getCaptions().getString("OptionsRandomSplash"));
		RandomSplash.setToolTipText(Kisekae.getCaptions().getString("ToolTipRandomSplash"));
		RandomSplash.setSelected(randomsplash);
		ShowBorder.setText(Kisekae.getCaptions().getString("OptionsShowBorder"));
		ShowBorder.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowBorder"));
		ShowBorder.setSelected(showborder);
		InitToolBar.setText(Kisekae.getCaptions().getString("MenuViewToolbar"));
		InitToolBar.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		InitToolBar.setSelected(inittoolbar);
		TbTools.setText(Kisekae.getCaptions().getString("MenuOptionsToolbarTools"));
		TbTools.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		TbTools.setSelected(tbtools);
		TbTools.addActionListener(this);
		TbColors.setText(Kisekae.getCaptions().getString("MenuOptionsToolbarColors"));
		TbColors.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		TbColors.setSelected(tbcolors);
		TbColors.addActionListener(this);
		TbPages.setText(Kisekae.getCaptions().getString("MenuOptionsToolbarPages"));
		TbPages.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		TbPages.setSelected(tbpages);
		TbPages.addActionListener(this);
		TbEdits.setText(Kisekae.getCaptions().getString("MenuOptionsToolbarEdits"));
		TbEdits.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		TbEdits.setSelected(tbedits);
		TbEdits.addActionListener(this);
		TbCompat.setText(Kisekae.getCaptions().getString("MenuOptionsToolbarCompatibility"));
		TbCompat.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowToolbar"));
		TbCompat.setSelected(tbcompat);
		TbCompat.addActionListener(this);
      LanguageBox.setToolTipText(Kisekae.getCaptions().getString("ToolTipSetLanguage"));
//    LanguageBox.setSelectedItem(language);
//    LanguageBox.addActionListener(this);
      LanguageBox.addActionListener(new ComboListener(LanguageBox));
      LanguageBox.setRenderer(new ComboRenderer());
      jLabel13.setText(Kisekae.getCaptions().getString("EncodingText"));
      jLabel13.setToolTipText(Kisekae.getCaptions().getString("ToolTipSetEncoding"));
      EncodingBox.setSelectedItem(encoding);
      EncodingBox.addActionListener(this);
      ExportBox.setSelectedItem(exporttype);
      ExportBox.addActionListener(this);
      jLabel21.setText(Kisekae.getCaptions().getString("PreferredBrowserText"));
      jLabel21.setToolTipText(Kisekae.getCaptions().getString("ToolTipSetBrowser"));
      BrowserBox.setSelectedItem(browser);
      BrowserBox.addActionListener(this);
      BrowserBox.setEditable(true) ;
		EventPause.setText(Kisekae.getCaptions().getString("MenuOptionsEventPause"));
		EventPause.setToolTipText(Kisekae.getCaptions().getString("ToolTipEventBreakpoint"));
		EventPause.setSelected(eventpause);
		ActionPause.setText(Kisekae.getCaptions().getString("MenuOptionsActionPause"));
		ActionPause.setToolTipText(Kisekae.getCaptions().getString("ToolTipActionBreakpoint"));
		ActionPause.setSelected(actionpause);
		DisableAll.setText(Kisekae.getCaptions().getString("MenuOptionsDisableAll"));
		DisableAll.setToolTipText(Kisekae.getCaptions().getString("ToolTipDisableAll"));
		DisableAll.setSelected(disableall);
		CacheAudio.setText(Kisekae.getCaptions().getString("OptionsCacheAudio"));
		CacheAudio.setToolTipText(Kisekae.getCaptions().getString("ToolTipCacheAudio"));
		CacheAudio.addActionListener(this);
		CacheAudio.setSelected(cacheaudio);
		CacheAudio1.setText(Kisekae.getCaptions().getString("OptionsCacheAudio"));
		CacheAudio1.setToolTipText(Kisekae.getCaptions().getString("ToolTipCacheAudio"));
		CacheAudio1.addActionListener(this);
		CacheAudio1.setSelected(cacheaudio);
		CacheVideo.setText(Kisekae.getCaptions().getString("OptionsCacheVideo"));
		CacheVideo.setToolTipText(Kisekae.getCaptions().getString("ToolTipCacheVideo"));
		CacheVideo.addActionListener(this);
		CacheVideo.setSelected(cachevideo);
		CacheVideo1.setText(Kisekae.getCaptions().getString("OptionsCacheVideo"));
      CacheVideo1.setToolTipText(Kisekae.getCaptions().getString("ToolTipCacheVideo"));
		CacheVideo1.addActionListener(this);
		CacheVideo1.setSelected(cachevideo);
		CacheImage.setText(Kisekae.getCaptions().getString("OptionsCacheImages"));
		CacheImage.setToolTipText(Kisekae.getCaptions().getString("ToolTipCacheImages"));
		CacheImage.setSelected(cacheimage);
		CacheImage.addActionListener(this);
		JavaSoundDebug.setText(Kisekae.getCaptions().getString("OptionsUseJavaSound"));
		JavaSoundDebug.setToolTipText(Kisekae.getCaptions().getString("ToolTipUseJavaSound"));
		JavaSoundDebug.setSelected(javasound);
		JavaSoundDebug.setEnabled(Kisekae.isMediaInstalled());
		JavaSoundDebug.addActionListener(this);
		InitStatusBar.setText(Kisekae.getCaptions().getString("MenuViewStatusBar"));
      InitStatusBar.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowStatusBar"));
		InitStatusBar.setSelected(initstatusbar);
		InitMenuBar.setText(Kisekae.getCaptions().getString("MenuViewMenuBar"));
      InitMenuBar.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowMenuBar"));
		InitMenuBar.setSelected(initmenubar);
		ImportCel.setText(Kisekae.getCaptions().getString("MenuViewImportCel"));
      ImportCel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImportCel"));
		ImportCel.setSelected(importcel);
		ExportCel.setText(Kisekae.getCaptions().getString("MenuViewExportCel"));
      ExportCel.setToolTipText(Kisekae.getCaptions().getString("ToolTipExportCel"));
		ExportCel.setSelected(exportcel);
		ExportCel.addActionListener(this);
		ComponentCel.setText(Kisekae.getCaptions().getString("MenuViewComponentCel"));
      ComponentCel.setToolTipText(Kisekae.getCaptions().getString("ToolTipComponentCel"));
		ComponentCel.setSelected(componentcel);
		ImportComponent.setText(Kisekae.getCaptions().getString("MenuViewImportComponent"));
      ImportComponent.setToolTipText(Kisekae.getCaptions().getString("ToolTipImportComponent"));
		ImportComponent.setSelected(importcomponent);
		AutoScroll.setText(Kisekae.getCaptions().getString("OptionsAutoScroll"));
      AutoScroll.setToolTipText(Kisekae.getCaptions().getString("ToolTipAutoScroll"));
		AutoScroll.setSelected(autoscroll);
      ReleaseMove.setText(Kisekae.getCaptions().getString("OptionsReleaseMove"));
		ReleaseMove.setToolTipText(Kisekae.getCaptions().getString("ToolTipFKissMove"));
		ReleaseMove.setSelected(releasemove);
		DragMove.setText(Kisekae.getCaptions().getString("OptionsDragMove"));
      DragMove.setToolTipText(Kisekae.getCaptions().getString("ToolTipDragMove"));
		DragMove.setSelected(dragmove);
		DropFixdrop.setText(Kisekae.getCaptions().getString("OptionsDropFixDrop"));
      DropFixdrop.setToolTipText(Kisekae.getCaptions().getString("ToolTipDropFixDrop"));
		DropFixdrop.setSelected(dropfixdrop);
		CatchFixdrop.setText(Kisekae.getCaptions().getString("OptionsCatchFixDrop"));
      CatchFixdrop.setToolTipText(Kisekae.getCaptions().getString("ToolTipCatchFixDrop"));
		CatchFixdrop.setSelected(catchfixdrop);
		VisibleUnfix.setText(Kisekae.getCaptions().getString("OptionsVisibleUnfix"));
      VisibleUnfix.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleUnfix"));
		VisibleUnfix.setSelected(visibleunfix);
		EarlyFix.setText(Kisekae.getCaptions().getString("OptionsEarlyFix"));
      EarlyFix.setToolTipText(Kisekae.getCaptions().getString("ToolTipEarlyFix"));
		EarlyFix.setSelected(earlyfix);
		DetachRestricted.setText(Kisekae.getCaptions().getString("OptionsDetachRestricted"));
      DetachRestricted.setToolTipText(Kisekae.getCaptions().getString("ToolTipDetachRestricted"));
		DetachRestricted.setSelected(detachrestricted);
		DetachMove.setText(Kisekae.getCaptions().getString("OptionsDetachMove"));
      DetachMove.setToolTipText(Kisekae.getCaptions().getString("ToolTipDetachMove"));
		DetachMove.setSelected(detachmove);
		DetachFix.setText(Kisekae.getCaptions().getString("OptionsDetachFix"));
      DetachFix.setToolTipText(Kisekae.getCaptions().getString("ToolTipDetachFix"));
		DetachFix.setSelected(detachfix);
		InvertGhost.setText(Kisekae.getCaptions().getString("OptionsInvertGhost"));
      InvertGhost.setToolTipText(Kisekae.getCaptions().getString("ToolTipInvertGhost"));
		InvertGhost.setSelected(invertghost);
		InvertGhost.addActionListener(this);
		TransparentGroup.setText(Kisekae.getCaptions().getString("OptionsTransparentGroup"));
      TransparentGroup.setToolTipText(Kisekae.getCaptions().getString("ToolTipTransparentGroup"));
		TransparentGroup.setSelected(transparentgroup);
		TransparentGroup.addActionListener(this);
		MapCollide.setText(Kisekae.getCaptions().getString("OptionsMapCollide"));
      MapCollide.setToolTipText(Kisekae.getCaptions().getString("ToolTipMapCollide"));
		MapCollide.setSelected(mapcollide);
		MoveXYCollide.setText(Kisekae.getCaptions().getString("OptionsMoveXYCollide"));
      MoveXYCollide.setToolTipText(Kisekae.getCaptions().getString("ToolTipMoveXYCollide"));
		MoveXYCollide.setSelected(movexycollide);
		RetainKey.setText(Kisekae.getCaptions().getString("OptionsRetainKey"));
      RetainKey.setToolTipText(Kisekae.getCaptions().getString("ToolTipRetainKey"));
		RetainKey.setSelected(retainkey);
		StrictSyntax.setText(Kisekae.getCaptions().getString("OptionsStrictSyntax"));
      StrictSyntax.setToolTipText(Kisekae.getCaptions().getString("ToolTipStrictSyntax"));
		StrictSyntax.setSelected(strictsyntax);
		ShowUndefs.setText(Kisekae.getCaptions().getString("OptionsShowUndefs"));
      ShowUndefs.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowUndefs"));
		ShowUndefs.setSelected(showundefs);
		AutoEndif.setText(Kisekae.getCaptions().getString("OptionsAutoEndif"));
      AutoEndif.setToolTipText(Kisekae.getCaptions().getString("ToolTipAutoEndif"));
		AutoEndif.setSelected(autoendif);
		ImmediateCollide.setText(Kisekae.getCaptions().getString("OptionsImmediateCollide"));
      ImmediateCollide.setToolTipText(Kisekae.getCaptions().getString("ToolTipImmediateCollide"));
		ImmediateCollide.setSelected(immediatecollide);
		ImmediateUnfix.setText(Kisekae.getCaptions().getString("OptionsImmediateUnfix"));
      ImmediateUnfix.setToolTipText(Kisekae.getCaptions().getString("ToolTipImmediateUnfix"));
		ImmediateUnfix.setSelected(immediateunfix);
		ImmediateEvent.setText(Kisekae.getCaptions().getString("OptionsImmediateEvent"));
      ImmediateEvent.setToolTipText(Kisekae.getCaptions().getString("ToolTipImmediateEvent"));
		ImmediateEvent.setSelected(immediateevent);
		ConstrainMoves.setText(Kisekae.getCaptions().getString("OptionsLimitMouseMoves"));
		ConstrainMoves.setToolTipText(Kisekae.getCaptions().getString("ToolTipLimitMouseMoves"));
		ConstrainMoves.setSelected(constrainmoves);
		ConstrainVisible.setText(Kisekae.getCaptions().getString("OptionsLimitVisibleMoves"));
		ConstrainVisible.setToolTipText(Kisekae.getCaptions().getString("ToolTipLimitVisibleMoves"));
		ConstrainVisible.setSelected(constrainvisible);
		ConstrainFKiss.setText(Kisekae.getCaptions().getString("OptionsLimitFKissMoves"));
		ConstrainFKiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipLimitFKissMoves"));
		ConstrainFKiss.setSelected(constrainfkiss);
		SuspendMedia.setText(Kisekae.getCaptions().getString("OptionsSuspendMedia"));
		SuspendMedia.setToolTipText(Kisekae.getCaptions().getString("ToolTipSuspendMedia"));
		SuspendMedia.setSelected(suspendmedia);
		LongSoundMedia.setText(Kisekae.getCaptions().getString("OptionsLongSoundMedia"));
		LongSoundMedia.setToolTipText(Kisekae.getCaptions().getString("ToolTipLongSoundMedia"));
		LongSoundMedia.setSelected(longsoundmedia);
		AdjustMediaVolume.setText(Kisekae.getCaptions().getString("OptionsAdjustMediaVolume"));
		AdjustMediaVolume.setToolTipText(Kisekae.getCaptions().getString("ToolTipAdjustMediaVolume"));
		AdjustMediaVolume.setSelected(adjustmediavolume);
		StopMusic.setText(Kisekae.getCaptions().getString("OptionsStopMusic"));
		StopMusic.setToolTipText(Kisekae.getCaptions().getString("ToolTipStopMusic"));
		StopMusic.setSelected(stopmusic);
		SoundSingle.setText(Kisekae.getCaptions().getString("OptionsSoundSingle"));
		SoundSingle.setToolTipText(Kisekae.getCaptions().getString("ToolTipSoundSingle"));
		SoundSingle.setSelected(soundsingle);
		AutoMediaLoop.setText(Kisekae.getCaptions().getString("OptionsLoopPlayback"));
		AutoMediaLoop.setToolTipText(Kisekae.getCaptions().getString("ToolTipLoopPlayback"));
		AutoMediaLoop.setSelected(automedialoop);
		AutoFullScreen.setText(Kisekae.getCaptions().getString("OptionsFullScreenVideo"));
		AutoFullScreen.setToolTipText(Kisekae.getCaptions().getString("ToolTipFullScreenVideo"));
		AutoFullScreen.setSelected(autofullscreen);
		MediaMinimize.setText(Kisekae.getCaptions().getString("OptionsMinimizeAudio"));
		MediaMinimize.setToolTipText(Kisekae.getCaptions().getString("ToolTipMinimizeAudio"));
		MediaMinimize.setSelected(mediaminimize);
		MediaCenter.setText(Kisekae.getCaptions().getString("OptionsCenterFrame"));
		MediaCenter.setToolTipText(Kisekae.getCaptions().getString("ToolTipCenterFrame"));
		MediaCenter.setSelected(mediacenter);
		MediaMusicResume.setText(Kisekae.getCaptions().getString("OptionsResumeMedia"));
		MediaMusicResume.setToolTipText(Kisekae.getCaptions().getString("ToolTipResumeMedia"));
		MediaMusicResume.setSelected(mediamusicresume);
		KeepAspect.setText(Kisekae.getCaptions().getString("OptionsRetainAspectRatio"));
		KeepAspect.setToolTipText(Kisekae.getCaptions().getString("ToolTipRetainAspectRatio"));
		KeepAspect.setSelected(keepaspect);
		KeyCase.setText(Kisekae.getCaptions().getString("OptionsKeyCase"));
		KeyCase.setToolTipText(Kisekae.getCaptions().getString("ToolTipKeyCase"));
		KeyCase.setSelected(keycase);
		VariableCase.setText(Kisekae.getCaptions().getString("OptionsVariableCase"));
		VariableCase.setToolTipText(Kisekae.getCaptions().getString("ToolTipVariableCase"));
		VariableCase.setSelected(variablecase);
		ExpandEvents.setText(Kisekae.getCaptions().getString("OptionsExpansionAdd"));
		ExpandEvents.setToolTipText(Kisekae.getCaptions().getString("ToolTipExpansionAdd"));
		ExpandEvents.setSelected(expandevents);
		EnableShell.setText(Kisekae.getCaptions().getString("OptionsEnableShell"));
		EnableShell.setToolTipText(Kisekae.getCaptions().getString("ToolTipEnableShell"));
		EnableShell.setSelected(enableshell);
		XYOffsets.setText(Kisekae.getCaptions().getString("OptionsXYOffsets"));
		XYOffsets.setToolTipText(Kisekae.getCaptions().getString("ToolTipXYOffsets"));
		XYOffsets.setSelected(xyoffsets);
		AbsoluteWindow.setText(Kisekae.getCaptions().getString("OptionsAbsoluteWindow"));
		AbsoluteWindow.setToolTipText(Kisekae.getCaptions().getString("ToolTipAbsoluteWindow"));
		AbsoluteWindow.setSelected(absolutewindow);
		PanelVisible.setText(Kisekae.getCaptions().getString("OptionsPanelVisible"));
		PanelVisible.setToolTipText(Kisekae.getCaptions().getString("ToolTipPanelVisible"));
		PanelVisible.setSelected(panelvisible);
		MouseInOutBox.setText(Kisekae.getCaptions().getString("OptionsMouseInOutBox"));
		MouseInOutBox.setToolTipText(Kisekae.getCaptions().getString("ToolTipMouseInOutBox"));
		MouseInOutBox.setSelected(mouseinoutbox);
		ContextMap.setText(Kisekae.getCaptions().getString("OptionsContextMap"));
		ContextMap.setToolTipText(Kisekae.getCaptions().getString("ToolTipContextMap"));
		ContextMap.setSelected(contextmap);
		MapCount.setText(Kisekae.getCaptions().getString("OptionsMapCount"));
		MapCount.setToolTipText(Kisekae.getCaptions().getString("ToolTipMapCount"));
		MapCount.setSelected(mapcount);
		AllAmbiguous.setText(Kisekae.getCaptions().getString("OptionsAllAmbiguous"));
		AllAmbiguous.setToolTipText(Kisekae.getCaptions().getString("ToolTipAllAmbiguous"));
		AllAmbiguous.setSelected(allambiguous);
		jLabel9.setText(Kisekae.getCaptions().getString("UndoLimitText"));
		jLabel9.setToolTipText(Kisekae.getCaptions().getString("ToolTipUndoLimit"));
		UndoLimit.setPreferredSize(new Dimension(30, 21));
		UndoLimit.setMinimumSize(new Dimension(30, 21));
		UndoLimit.setHorizontalAlignment(SwingConstants.RIGHT);
		UndoLimit.setText(undolimit);
		UndoLimit.addActionListener(this);
		EditEnable.setText(Kisekae.getCaptions().getString("OptionsEnableEdits"));
		EditEnable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEnableEdits"));
		EditEnable.setSelected(editenable);
		SecurityEnable.setText(Kisekae.getCaptions().getString("OptionsEnableSecurity"));
		SecurityEnable.setToolTipText(Kisekae.getCaptions().getString("ToolTipEnableSecurity"));
		SecurityEnable.setSelected(securityenable);
		InitEdit.setText(Kisekae.getCaptions().getString("OptionsInitEdit"));
		InitEdit.setToolTipText(Kisekae.getCaptions().getString("ToolTipInitEdit"));
		InitEdit.setSelected(initedit);
		AltEditDrag.setText(Kisekae.getCaptions().getString("OptionsAltEditKey"));
		AltEditDrag.setToolTipText(Kisekae.getCaptions().getString("ToolTipAltEditKey"));
		AltEditDrag.setSelected(alteditdrag);
		ShiftEditDrag.setText(Kisekae.getCaptions().getString("OptionsShiftEditKey"));
		ShiftEditDrag.setToolTipText(Kisekae.getCaptions().getString("ToolTipShiftEditKey"));
		ShiftEditDrag.setSelected(shifteditdrag);
		ImportRelative.setText(Kisekae.getCaptions().getString("OptionsImportRelative"));
		ImportRelative.setToolTipText(Kisekae.getCaptions().getString("ToolTipImportRelative"));
		ImportRelative.setSelected(importrelative);
		WriteCelOffset.setText(Kisekae.getCaptions().getString("OptionsWriteCelOffsets"));
		WriteCelOffset.setToolTipText(Kisekae.getCaptions().getString("ToolTipWriteCelOffsets"));
		WriteCelOffset.setSelected(writeceloffset);
		ShowBreakPointEnd.setText(Kisekae.getCaptions().getString("OptionsShowBreakPointEnd"));
		ShowBreakPointEnd.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowBreakPointEnd"));
		ShowBreakPointEnd.setSelected(showbreakpointend);
		ShowStepIntoEnd.setText(Kisekae.getCaptions().getString("OptionsShowStepIntoEnd"));
		ShowStepIntoEnd.setToolTipText(Kisekae.getCaptions().getString("ToolTipShowStepIntoEnd"));
		ShowStepIntoEnd.setSelected(showstepintoend);
		WriteComment.setText(Kisekae.getCaptions().getString("OptionsWriteComment"));
		WriteComment.setToolTipText(Kisekae.getCaptions().getString("ToolTipWriteComment"));
		WriteComment.setSelected(writecomment);
		jLabel10.setText(Kisekae.getCaptions().getString("CNFCommentText"));
		jLabel10.setToolTipText(Kisekae.getCaptions().getString("ToolTipCNFComment"));
		CommentCol.setPreferredSize(new Dimension(30, 21));
		CommentCol.setMinimumSize(new Dimension(30, 21));
		CommentCol.setHorizontalAlignment(SwingConstants.RIGHT);
		CommentCol.setText(commentcol);
		CommentCol.addActionListener(this);
		jLabel11.setText(Kisekae.getCaptions().getString("FKissIndentText"));
		jLabel11.setToolTipText(Kisekae.getCaptions().getString("ToolTipFKissIndent"));
		IndentSpace.setPreferredSize(new Dimension(30, 21));
		IndentSpace.setMinimumSize(new Dimension(30, 21));
		IndentSpace.setHorizontalAlignment(SwingConstants.RIGHT);
		IndentSpace.setText(indentspace);
		IndentSpace.addActionListener(this);
		jLabel14.setText(Kisekae.getCaptions().getString("KissWebText"));
		jLabel15.setText(Kisekae.getCaptions().getString("UserDirectoryText"));
		jLabel16.setText(Kisekae.getCaptions().getString("WebSiteText"));
		jLabel17.setText(Kisekae.getCaptions().getString("OnlineHelpText"));
		jLabel18.setText(Kisekae.getCaptions().getString("SplashDirectoryText"));
		jLabel19.setText(Kisekae.getCaptions().getString("IconDirectoryText"));
		KissWeb.setText(kissweb);
		jLabel14.setToolTipText(Kisekae.getCaptions().getString("ToolTipKissWeb"));
		KissWeb.addActionListener(this);
		UserDirectory.setText(userdir);
		jLabel15.setToolTipText(Kisekae.getCaptions().getString("ToolTipUserDir"));
		UserDirectory.addActionListener(this);
		WebSite.setText(website);
		jLabel16.setToolTipText(Kisekae.getCaptions().getString("ToolTipWebSite"));
 		WebSite.addActionListener(this);
		OnlineHelp.setText(onlinehelp);
		jLabel17.setToolTipText(Kisekae.getCaptions().getString("ToolTipOnlineHelp"));
		OnlineHelp.addActionListener(this);
		SplashDirectory.setText(splashdir);
		jLabel18.setToolTipText(Kisekae.getCaptions().getString("ToolTipSplashDir"));
		SplashDirectory.addActionListener(this);
      SplashList.addListSelectionListener(this);
		IconDirectory.setText(icondir);
		jLabel19.setToolTipText(Kisekae.getCaptions().getString("ToolTipIconDir"));
		IconDirectory.addActionListener(this);
      IconList.addListSelectionListener(this);
		URL iconfile = Kisekae.getResource("Images/folder.gif") ;
      Icon folderIcon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
      SplashBtn.setIcon(folderIcon) ;
      SplashBtn.setBorder(eb3) ;
		SplashBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		SplashBtn.addActionListener(this);
      IconBtn.setIcon(folderIcon) ;
      IconBtn.setBorder(eb3) ;
		IconBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		IconBtn.addActionListener(this);
      UserBtn.setIcon(folderIcon) ;
      UserBtn.setBorder(eb3) ;
		UserBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		UserBtn.addActionListener(this);
      HelpBtn.setIcon(folderIcon) ;
      HelpBtn.setBorder(eb3) ;
		HelpBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		HelpBtn.addActionListener(this);
      PortalBtn.setIcon(folderIcon) ;
      PortalBtn.setBorder(eb3) ;
		PortalBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		PortalBtn.addActionListener(this);
		jLabel20.setText(Kisekae.getCaptions().getString("MaxLruFilesText"));
      ClearLruBtn.setText(Kisekae.getCaptions().getString("LruClearMessage")) ;
		ClearLruBtn.addActionListener(this);
		DefaultPlayFKiss.setText(Kisekae.getCaptions().getString("OptionsDefaultPlayFKiss"));
      DefaultPlayFKiss.setToolTipText(Kisekae.getCaptions().getString("ToolTipDefaultPlayFKiss"));
		DefaultPlayFKiss.setSelected(defaultplayfkiss);
		AcceptCnfErrors.setText(Kisekae.getCaptions().getString("OptionsAcceptCnfErrors"));
      AcceptCnfErrors.setToolTipText(Kisekae.getCaptions().getString("ToolTipAcceptCnfErrors"));
		AcceptCnfErrors.setSelected(acceptcnferrors);
		PagesAreScenes.setText(Kisekae.getCaptions().getString("OptionsPagesAreScenes"));
      PagesAreScenes.setToolTipText(Kisekae.getCaptions().getString("ToolTipPagesAreScenes"));
		PagesAreScenes.setSelected(pagesarescenes);
		MultipleEvents.setText(Kisekae.getCaptions().getString("OptionsMultipleEvents"));
      MultipleEvents.setToolTipText(Kisekae.getCaptions().getString("ToolTipMultipleEvents"));
		MultipleEvents.setSelected(multipleevents);
      
		getContentPane().add(panel1);
		panel1.add(jTabbedPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

      // The General tab pane.

		jTabbedPane1.add(jPanel1, Kisekae.getCaptions().getString("GeneralTabText"));
		jPanel1.add(jPanel7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel7.add(Backup, null);
		jPanel7.add(SaveSource, null);
		jPanel7.add(LoadClose, null);
		jPanel7.add(ShowDLPrompt, null);
		jPanel1.add(jPanel8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel8.add(SystemLF, null);
		jPanel8.add(JavaLF, null);
		jPanel8.add(AppleMac, null);
		jPanel1.add(jPanel15, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
      jPanel15.add(LanguageBox, null);
      jPanel15.add(jLabel13, null);
      jPanel15.add(EncodingBox, null);
		jPanel1.add(jPanel16, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel16.add(InitMenuBar, null);
		jPanel16.add(InitToolBar, null);
		jPanel16.add(InitStatusBar, null);
		jPanel16.add(ShowTips, null);
		jPanel1.add(jPanel17, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel17.add(CacheImage, null);
		jPanel17.add(CacheAudio, null);
		jPanel17.add(CacheVideo, null);
		jPanel1.add(jPanel18, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel18.add(AutoScroll, null);
		jPanel18.add(ConstrainMoves, null);
		jPanel18.add(ConstrainFKiss, null);

      // The KiSS tab pane.

		jTabbedPane1.add(jPanel12, Kisekae.getCaptions().getString("KissTabText"));
		jPanel12.add(jPanel13, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel13.add(jLabel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel13.add(MaxFlex, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel13.add(jLabel12, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel13.add(MaxLock, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel13.add(jLabel5, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel13.add(StickyFlex, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel12.add(jPanel14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 10), 0, 0));
		jPanel14.add(MaxPageSet, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel14.add(jLabel6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel14.add(jLabel7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel14.add(MaxColorSet, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel12.add(jPanel33, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
		jPanel33.add(jLabel20, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel33.add(MaxLruFiles, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel33.add(ClearLruBtn, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));

      // The FKiSS tab pane.

		jTabbedPane1.add(jPanel2, Kisekae.getCaptions().getString("FKissTabText"));
      jPanel2.add(jPanel10, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
      jPanel10.add(EventQueues, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel10.add(jLabel4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
      jPanel10.add(TimerPeriod, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel10.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
      jPanel10.add(GifPeriod, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel10.add(jLabel8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
      jPanel10.add(ScenePeriod, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel10.add(jLabel22, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
      jPanel10.add(AudioPeriod, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel10.add(jLabel23, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
      jPanel2.add(jPanel11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      jPanel11.add(TimerOption, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel11.add(EventOption, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel11.add(SoundOption, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel11.add(MovieOption, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel11.add(AnimateOption, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(jPanel111, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
		jPanel111.add(KeyCase, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel111.add(VariableCase, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

      // The UKiSS custom tab pane.

		jTabbedPane1.add(jPanel30, Kisekae.getCaptions().getString("CustomTabText"));
		jPanel30.add(jPanel31, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel31.add(jLabel16, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(jLabel17, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(jLabel14, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(jLabel15, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(jLabel18, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(jLabel19, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel31.add(KissWeb, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel31.add(PortalBtn, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel31.add(UserDirectory, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel31.add(UserBtn, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel31.add(WebSite, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel31.add(OnlineHelp, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//		jPanel31.add(HelpBtn, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel31.add(SplashDirectory, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//		jPanel31.add(SplashBtn, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel31.add(IconDirectory, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//		jPanel31.add(IconBtn, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
//            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel30.add(jPanel51, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel51.add(jPanel32, null);
      jPanel32.add(IconScrollPane,BorderLayout.NORTH) ;
		jPanel51.add(jPanel50, null);
      jPanel50.add(SplashScrollPane,BorderLayout.NORTH) ;
		jPanel51.add(jPanel52, null);
		jPanel52.add(SplashSetName, null) ;
		jPanel52.add(RandomSplash, null) ;

      // The Compatibility tab pane.

		jTabbedPane1.add(jPanel23, Kisekae.getCaptions().getString("CompatibilityTabText"));
		jPanel23.add(jTabbedPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel23.add(jPanel24, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jTabbedPane2.add(jPanel24, Kisekae.getCaptions().getString("ControlSettingsBoxText"));
      jPanel24.setLayout(new BoxLayout(jPanel24,BoxLayout.Y_AXIS)) ;
		jPanel24.add(ImmediateEvent, null);
		jPanel24.add(ImmediateCollide, null);
		jPanel24.add(MapCollide, null);
		jPanel24.add(MoveXYCollide, null);
		jPanel24.add(DropFixdrop, null);
		jPanel24.add(CatchFixdrop, null);
		jPanel24.add(VisibleUnfix, null);
		jPanel24.add(EarlyFix, null);
		jPanel24.add(DetachRestricted, null);
		jPanel24.add(DetachMove, null);
		jPanel24.add(DetachFix, null);
		jPanel24.add(MultipleEvents, null);
		jTabbedPane2.add(jPanel42, Kisekae.getCaptions().getString("InterfaceSettingsBoxText"));
		jPanel42.add(ContextMap, null);
		jPanel42.add(MapCount, null);
		jPanel42.add(AllAmbiguous, null);
		jPanel42.add(ReleaseMove, null);
		jPanel42.add(DragMove, null);
		jPanel42.add(ConstrainVisible, null);
		jPanel42.add(RetainKey, null);
		jTabbedPane2.add(jPanel44, Kisekae.getCaptions().getString("ViewerSettingsBoxText"));
		jPanel44.add(XYOffsets, null);
		jPanel44.add(AbsoluteWindow, null);
		jPanel44.add(MouseInOutBox, null);
		jPanel44.add(PanelVisible, null);
		jPanel44.add(TransparentGroup, null);
		jPanel44.add(InvertGhost, null);
		jPanel44.add(ImmediateUnfix, null);
      jPanel44.add(SoundSingle, null);
		jTabbedPane2.add(jPanel43, Kisekae.getCaptions().getString("MiscellaneousSettingsBoxText"));
		jPanel43.add(EnableShell, null);
		jPanel43.add(StrictSyntax, null);
		jPanel43.add(ShowUndefs, null);
		jPanel43.add(ExpandEvents, null);
		jPanel43.add(AutoEndif, null);
		jPanel43.add(DefaultPlayFKiss, null);
		jPanel43.add(AcceptCnfErrors, null);
		jPanel43.add(PagesAreScenes, null);

		jPanel23.add(jPanel40, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel40.add(PlayFKissBtn, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		jPanel40.add(DirectKissBtn, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		jPanel40.add(GnomeKissBtn, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
		jPanel40.add(KissLDBtn, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
      JTextArea notes = new JTextArea();
      notes.setText("Presets are not fully functional") ;
      notes.setBackground(Color.lightGray) ;
      notes.setBorder(new CompoundBorder(new LineBorder(Color.black),new EmptyBorder(0,5,0,5))) ;
      notes.setWrapStyleWord(true) ;
      notes.setLineWrap(true) ;
      notes.setEditable(false) ;
      notes.setSize(100,50) ;
		jPanel40.add(notes, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 10, 10, 10), 0, 0));

      // The Import tab pane.

		jTabbedPane1.add(jPanel49, Kisekae.getCaptions().getString("ImportTabText"));
		jPanel49.add(jPanel39, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel39.add(ImportRelative, null) ;
		jPanel39.add(ImportComponent, null) ;
  		jPanel39.add(ImportCel, null) ;

      // The Export tab pane.

		jTabbedPane1.add(jPanel46, Kisekae.getCaptions().getString("ExportTabText"));
		jPanel46.add(jPanel41, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
  		jPanel41.add(WriteCelOffset, null) ;
  		jPanel41.add(ComponentCel, null) ;
  		jPanel45.add(ExportCel, null) ;
  		jPanel45.add(ExportBox, null) ;
  		jPanel41.add(jPanel45, null) ;

      // The Window tab pane.

		jTabbedPane1.add(jPanel36, Kisekae.getCaptions().getString("WindowTabText"));
		jPanel36.add(jPanel37, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel37.add(SizeToFit, null);
		jPanel37.add(ScaleToFit, null);
		jPanel37.add(RetainWindowSize, null);
		jPanel37.add(MaximizeWindow, null);
		jPanel37.add(ShowBorder, null);
		jPanel36.add(jPanel47, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		jPanel47.add(TbTools, null);
		jPanel47.add(TbColors, null);
		jPanel47.add(TbPages, null);
		jPanel47.add(TbEdits, null);
		jPanel47.add(TbCompat, null);

      // The Media tab pane.

		jTabbedPane1.add(jPanel19, Kisekae.getCaptions().getString("MediaTabText"));
		jPanel19.add(jPanel110, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel110.add(JavaSoundDebug, null);
		jPanel110.add(MediaMinimize, null);
		jPanel110.add(AutoMediaLoop, null);
		jPanel110.add(CacheAudio1, null);
		jPanel110.add(AutoFullScreen, null);
		jPanel110.add(KeepAspect, null);
		jPanel110.add(CacheVideo1, null);
		jPanel110.add(MediaCenter, null);
		jPanel19.add(jPanel112, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
		jPanel112.add(StopMusic, null);
		jPanel112.add(SuspendMedia, null);
		jPanel112.add(MediaMusicResume, null);
      jPanel113.add(LongSoundMedia,null) ;
		jPanel113.add(LongDuration, null);
      jPanel112.add(jPanel113,null);
      jPanel114.add(AdjustMediaVolume,null) ;
		jPanel114.add(MediaVolume, null);
      jPanel112.add(jPanel114,null);

      // The Edit tab pane.

		jTabbedPane1.add(jPanel20, Kisekae.getCaptions().getString("EditTabText"));
		jPanel20.add(jPanel21, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel21.add(EditEnable, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		jPanel21.add(SecurityEnable, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		jPanel21.add(InitEdit, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		jPanel21.add(AltEditDrag, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		jPanel21.add(ShiftEditDrag, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
		jPanel20.add(jPanel22, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 10), 0, 0));
		jPanel22.add(jLabel9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel22.add(UndoLimit, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel22.add(jLabel10, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel22.add(CommentCol, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel22.add(jLabel11, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel22.add(IndentSpace, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel20.add(jPanel25, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
		jPanel25.add(jPanel29, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		jPanel27.add(EventPause,null) ;
		jPanel27.add(ActionPause,null) ;
		jPanel27.add(DisableAll,null) ;
		jPanel34.add(ShowBreakPointEnd,null) ;
		jPanel34.add(ShowStepIntoEnd,null) ;
		jPanel34.add(WriteComment,null) ;
      jPanel29.add(jPanel27,null) ;
      jPanel29.add(Box.createGlue()) ;
      jPanel29.add(jPanel34,null) ;

      // The Debug tab pane.

		jTabbedPane1.add(jPanel3, Kisekae.getCaptions().getString("DebugTabText"));
      jPanel3.add(jPanel5a, BorderLayout.CENTER);
		jPanel5a.add(jPanel5, BorderLayout.CENTER);
		jPanel5.add(MouseDebug, null);
		jPanel5.add(EventDebug, null);
		jPanel5.add(ControlDebug, null);
		jPanel5.add(ActionDebug, null);
		jPanel5.add(LoadDebug, null);
		jPanel5.add(VariableDebug, null);
		jPanel5.add(EditDebug, null);
		jPanel5.add(SoundDebug, null);
		jPanel5.add(ImageDebug, null);
		jPanel5.add(MovieDebug, null);
		jPanel5.add(FKissDebug, null);
		jPanel5.add(MediaDebug, null);
		jPanel5.add(SearchDebug, null);
		jPanel5.add(DisabledDebug, null);
		jPanel5.add(ComponentDebug, null);
		jPanel5a.add(jPanel6, BorderLayout.NORTH);
		jPanel6.add(jLabel2, BorderLayout.WEST);
		jPanel6.add(LogFileBox, BorderLayout.CENTER);
		jPanel3.add(jPanel9, BorderLayout.EAST);
      jPanel9.add(Box.createGlue()) ;
		jPanel9.add(LogEdit, null);
      jPanel9.add(Box.createGlue()) ;
		jPanel9.add(LogClear, null);
      jPanel9.add(Box.createGlue()) ;

      // The standard buttons at the bottom of the dialog.

		panel1.add(jPanel4, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 230, 0));
      jPanel4.add(Box.createGlue()) ;
		jPanel4.add(OK, null);
      jPanel4.add(Box.createGlue()) ;
//		jPanel4.add(APPLY, null);
//    jPanel4.add(Box.createGlue()) ;
		jPanel4.add(RESET, null);
      jPanel4.add(Box.createGlue()) ;
		jPanel4.add(SAVE, null);
      jPanel4.add(Box.createGlue()) ;
      jPanel4.add(CANCEL, null);
      jPanel4.add(Box.createGlue()) ;
      
      JRootPane rootpane = getRootPane()  ;
      rootpane.setDefaultButton(OK) ;
 
      
      // The WebSearch extensions
      // ------------------------
      
		jTabbedPane1.add(panel1ws, Kisekae.getCaptions().getString("SearchTabText"));

      Border eb1ws = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WebSearchGeneralOptions")),eb1) ;
      Border eb2ws = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WebSearchDirectoriesOptions")),eb1) ;
      Border eb3ws = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WebSearchThumbnailsOptions")),eb1) ;
      Border eb4ws = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("WebSearchLimitsOptions")),eb1) ;

		panel1ws.setLayout(gridBagLayout1ws);
		jPanel1ws.setBorder(eb1ws);
		jPanel1ws.setLayout(gridLayout1ws);
		jPanel2ws.setBorder(eb2ws);
      jPanel2ws.setLayout(gridBagLayout2ws);
		jPanel3ws.setBorder(eb3ws);
      jPanel3ws.setLayout(gridBagLayout3ws);
		jPanel4ws.setBorder(eb4ws);
      jPanel4ws.setLayout(flowLayout1ws);
      gridLayout1ws.setRows(6);
      gridLayout1ws.setColumns(1);
      
		UseDefaultWS.setToolTipText("Use different search options for online versus local searches");
		UseDefaultWS.setText("Use Default Search Options");
		UseDefaultWS.setSelected(usedefaultws) ;
		UseDefaultWS.addChangeListener(this) ;
		ClearMaster.setToolTipText("Creates a new master search index page");
		ClearMaster.setText("Clear master index");
		ClearMaster.setSelected(clearmaster) ;
		SaveArchive.setToolTipText("Saves validated archive files to local storage");
		SaveArchive.setText("Save Validated Sets");
		SaveArchive.setSelected(savearchive);
		SaveArchive.setEnabled(!UseDefaultWS.isSelected());
		SaveAsZip.setToolTipText("Saves validated archive files as ZIP files");
		SaveAsZip.setText("Write in Zip format");
		SaveAsZip.setSelected(saveaszip);
		SaveAsZip.setEnabled(!UseDefaultWS.isSelected());
		SaveImage.setToolTipText("Generates and saves a thumbnail image of each KiSS set");
		SaveImage.setText("Save Thumbnail Images");
		SaveImage.setSelected(saveimage);
		SaveImage.setEnabled(!UseDefaultWS.isSelected());
		ShowLoad.setToolTipText("Shows the load window during set validation");
		ShowLoad.setText("Show Load Dialog");
		ShowLoad.setSelected(showload);
		ShowLoad.setEnabled(!UseDefaultWS.isSelected());
		ImageDirectory.setPreferredSize(new Dimension(200, 21));
		ImageDirectory.setMinimumSize(new Dimension(200, 21));
		ImageDirectory.setText(imagedirectory);
		HtmlDirectory.setPreferredSize(new Dimension(200, 21));
		HtmlDirectory.setMinimumSize(new Dimension(200, 21));
		HtmlDirectory.setText(htmldirectory);
      DataDirectory.setText(datadirectory);
      DataDirectory.setPreferredSize(new Dimension(200, 21));
		DataDirectory.setMinimumSize(new Dimension(200, 21));
		KissIndex.setPreferredSize(new Dimension(200, 21));
		KissIndex.setMinimumSize(new Dimension(200, 21));
		KissIndex.setText(kissindex);
		DownloadSize.setPreferredSize(new Dimension(50, 21));
		DownloadSize.setMinimumSize(new Dimension(50, 21));
		DownloadSize.setText(downloadsize);
		ThumbWidth.setPreferredSize(new Dimension(50, 21));
		ThumbWidth.setMinimumSize(new Dimension(50, 21));
		ThumbWidth.setText(thumbwidth);
		ThumbHeight.setPreferredSize(new Dimension(50, 21));
		ThumbHeight.setMinimumSize(new Dimension(50, 21));
		ThumbHeight.setText(thumbheight);
		ThumbPage.setPreferredSize(new Dimension(50, 21));
		ThumbPage.setMinimumSize(new Dimension(50, 21));
		ThumbPage.setText(thumbpage);

      jLabel3ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel3ws.setText("Image Directory:");
		jLabel3ws.setToolTipText("The local folder for thumbnail images");
      jLabel4ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel4ws.setText("HTML Directory:");
		jLabel4ws.setToolTipText("The local folder for generated HTML files");
      jLabel5ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel5ws.setText("Index File:");
		jLabel5ws.setToolTipText("The local HTML index file.  This is a table to the generated HTML files");
      jLabel6ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel6ws.setText("Data Directory:");
		jLabel6ws.setToolTipText("The local folder for saving downloaded archive files");
      jLabel7ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel7ws.setText("Maximum Archive Size (KB):");
		jLabel7ws.setToolTipText("The maximum size of an archive file for download transfer");
      jLabel8ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel8ws.setText("Thumbnail Width:");
		jLabel8ws.setToolTipText("The width, in pixels, of a generated thumbnail image");
      jLabel9ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel9ws.setText("Thumbnail Height:");
		jLabel9ws.setToolTipText("The height, in pixels, of a generated thumbnail image");
      jLabel10ws.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel10ws.setText("Thumbnail Page:");
		jLabel10ws.setToolTipText("The preferred page set used for the thumbnail image");

      // The General options.

		jPanel1ws.add(UseDefaultWS, null);
		jPanel1ws.add(SaveArchive, null);
		jPanel1ws.add(SaveAsZip, null);
		jPanel1ws.add(SaveImage, null);
		jPanel1ws.add(ShowLoad, null);
		jPanel1ws.add(ClearMaster, null);
      panel1ws.add(jPanel1ws, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

      // The Limits options.

		jPanel4ws.add(jLabel7ws, null);
		jPanel4ws.add(DownloadSize, null);
      panel1ws.add(jPanel4ws, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));

      // The Directory options.

      jPanel2ws.add(jLabel6ws, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel2ws.add(DataDirectory, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel2ws.add(jLabel4ws, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      jPanel2ws.add(HtmlDirectory, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
      jPanel2ws.add(jLabel3ws, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      jPanel2ws.add(ImageDirectory, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
      jPanel2ws.add(jLabel5ws, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
      jPanel2ws.add(KissIndex, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 5), 0, 0));
      panel1ws.add(jPanel2ws, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));

      // The Thumbnail tab pane.

		jPanel3ws.add(jLabel8ws, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel3ws.add(ThumbWidth, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 18), 0, 0));
		jPanel3ws.add(jLabel9ws, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel3ws.add(ThumbHeight, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel3ws.add(jLabel10ws, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jPanel3ws.add(ThumbPage, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      panel1ws.add(jPanel3ws, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 0, 0), 0, 0));

      // End of WebSearch extensions
      // ---------------------------
	}


	// Methods to retrieve current option values.

	static public boolean getSoundOn() { return sound ; }
	static public boolean getMovieOn() { return movie ; }
	static public boolean getTimerOn() { return timer ; }
	static public boolean getFKissOn() { return event ; }
	static public boolean getEditEnable() { return editenable && !securityenable ; }
	static public boolean getTempEditEnable() { return editenable && !securityenable && tempeditenable; }
	static public boolean getSecurityEnable() { return securityenable ; }
	static public boolean getInitEdit() { return initedit ; }
	static public boolean getAnimateOn() { return animate ; }
	static public boolean getDebugMouse() { return debugmouse ; }
	static public boolean getDebugControl() { return debugcontrol ; }
	static public boolean getDebugLoad() { return debugload ; }
	static public boolean getDebugEdit() { return debugedit ; }
	static public boolean getDebugImage() { return debugimage ; }
	static public boolean getDebugEvent() { return debugevent ; }
	static public boolean getDebugAction() { return debugaction ; }
	static public boolean getDebugVariable() { return debugvariable ; }
	static public boolean getDebugFKiss() { return debugfkiss ; }
	static public boolean getDebugSound() { return debugsound ; }
	static public boolean getDebugMovie() { return debugmovie ; }
	static public boolean getDebugMedia() { return debugmedia ; }
	static public boolean getDebugSearch() { return debugsearch ; }
	static public boolean getDebugDisabled() { return debugdisabled ; }
	static public boolean getDebugComponent() { return debugcomponent ; }
	static public boolean getJavaSound() { return javasound ; }
	static public boolean getBackupFileOn() { return backup ; }
	static public boolean getSaveSourceOn() { return savesource ; }
	static public boolean getShowDLPrompt() { return showdlprompt ; }
	static public boolean getShowTips() { return showtips ; }
	static public boolean getLoadCloseOn() { return loadclose ; }
	static public boolean getCacheAudio() { return cacheaudio ; }
	static public boolean getCacheVideo() { return cachevideo ; }
	static public boolean getCacheImage() { return cacheimage ; }
	static public boolean getScaleToFit() { return scaletofit ; }
	static public boolean getSizeToFit() { return sizetofit ; }
	static public boolean getRetainWindowSize() { return retainwindowsize ; }
	static public boolean getMaximizeWindow() { return maximizewindow ; }
	static public boolean getRandomSplash() { return randomsplash ; }
	static public boolean getShowBorder() { return showborder ; }
	static public boolean getInitToolbar() { return inittoolbar ; }
	static public boolean getTbTools() { return tbtools ; }
	static public boolean getTbColors() { return tbcolors ; }
	static public boolean getTbPages() { return tbpages ; }
	static public boolean getTbEdits() { return tbedits ; }
	static public boolean getTbCompat() { return tbcompat ; }
	static public boolean getInitStatusbar() { return initstatusbar ; }
	static public boolean getInitMenubar() { return initmenubar ; }
	static public boolean getImportCel() { return importcel ; }
	static public boolean getExportCel() { return exportcel ; }
	static public boolean getComponentCel() { return componentcel ; }
	static public boolean getImportComponent() { return importcomponent ; }
	static public boolean getConstrainMoves() { return constrainmoves ; }
	static public boolean getConstrainVisible() { return constrainvisible ; }
	static public boolean getConstrainFKiss() { return constrainfkiss ; }
	static public boolean getAutoScroll() { return autoscroll ; }
	static public boolean getDragMove() { return dragmove ; }
	static public boolean getReleaseMove() { return releasemove ; }
	static public boolean getDropFixdrop() { return dropfixdrop ; }
	static public boolean getCatchFixdrop() { return catchfixdrop ; }
	static public boolean getVisibleUnfix() { return visibleunfix ; }
	static public boolean getEarlyFix() { return earlyfix ; }
	static public boolean getDetachRestricted() { return detachrestricted ; }
	static public boolean getDetachMove() { return detachmove ; }
	static public boolean getDetachFix() { return detachfix ; }
	static public boolean getInvertGhost() { return invertghost ; }
	static public boolean getTransparentGroup() { return transparentgroup ; }
	static public boolean getMapCollide() { return mapcollide ; }
	static public boolean getMoveXYCollide() { return movexycollide ; }
	static public boolean getRetainKey() { return retainkey ; }
	static public boolean getStrictSyntax() { return strictsyntax ; }
	static public boolean getShowUndefs() { return showundefs ; }
	static public boolean getAutoEndif() { return autoendif ; }
	static public boolean getImmediateCollide() { return immediatecollide ; }
	static public boolean getImmediateUnfix() { return immediateunfix ; }
	static public boolean getImmediateEvent() { return immediateevent ; }
	static public boolean getAutoMediaLoop() { return automedialoop ; }
	static public boolean getAutoFullScreen() { return autofullscreen ; }
	static public boolean getSuspendMedia() { return suspendmedia ; }
	static public boolean getLongSoundMedia() { return longsoundmedia ; }
	static public boolean getAdjustMediaVolume() { return adjustmediavolume ; }
	static public boolean getStopMusic() { return stopmusic ; }
	static public boolean getSoundSingle() { return soundsingle ; }
	static public boolean getMediaMinimize() { return mediaminimize ; }
	static public boolean getMediaCenter() { return mediacenter ; }
	static public boolean getMediaMusicResume() { return mediamusicresume ; }
	static public boolean getKeepAspect() { return keepaspect ; }
	static public boolean getKeyCase() { return keycase ; }
	static public boolean getVariableCase() { return variablecase ; }
	static public boolean getExpandEvents() { return expandevents ; }
	static public boolean getEnableShell() { return enableshell ; }
	static public boolean getXYOffsets() { return xyoffsets ; }
	static public boolean getAbsoluteWindow() { return absolutewindow ; }
	static public boolean getPanelVisible() { return panelvisible ; }
	static public boolean getMouseInOutBox() { return mouseinoutbox ; }
	static public boolean getContextMap() { return contextmap ; }
	static public boolean getMapCount() { return mapcount ; }
	static public boolean getAllAmbiguous() { return allambiguous ; }
	static public boolean getAltEditDrag() { return alteditdrag ; }
	static public boolean getShiftEditDrag() { return shifteditdrag ; }
	static public boolean getImportRelative() { return importrelative ; }
	static public boolean getWriteCelOffset() { return writeceloffset ; }
	static public boolean getShowBreakPointEnd() { return showbreakpointend ; }
	static public boolean getShowStepIntoEnd() { return showstepintoend ; }
	static public boolean getWriteComment() { return writecomment ; }
	static public boolean getEventPause() { return eventpause ; }
	static public boolean getActionPause() { return actionpause ; }
	static public boolean getDisableAll() { return disableall ; }
	static public boolean getSystemLF() { return systemlf ; }
	static public boolean getAppleMac() { return applemac ; }
	static public boolean getLinux() { return linux ; }
   static public boolean getDefaultPlayFKiss() { return defaultplayfkiss ; }
   static public boolean getAcceptCnfErrors() { return acceptcnferrors ; }
   static public boolean getPagesAreScenes() { return pagesarescenes ; }
   static public boolean getMultipleEvents() { return multipleevents ; }

   static public boolean getPlayFKissCompatibility() { return playfkiss ; }
   static public boolean getDirectKissCompatibility() { return directkiss ; }
   static public boolean getGnomeKissCompatibility() { return gnomekiss ; }
   static public boolean getKissLDCompatibility() { return kissld ; }
    
 
   static public int getMaxLruFiles() 
   {
      if (maxlrufiles == null) return 0 ;
      try { return (Integer.parseInt(maxlrufiles)) ; }
      catch (Exception e) { }
      return 0 ;
   }
   
   static public int getLongDuration() 
   {
      if (longduration == null) return 0 ;
      try { return (Integer.parseInt(longduration)) ; }
      catch (Exception e) { }
      return 0 ;
   }
   
   static public float getMediaVolume() 
   {
      if (mediavolume == null) return 0 ;
      try { return (Float.parseFloat(mediavolume)) ; }
      catch (Exception e) { }
      return 1.0f ;
   }

	static public int getEventQueues()
   {
      if (eventqueues == null) return -1 ;
      try { return (Integer.parseInt(eventqueues)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getTimerPeriod()
   {
      if (timerperiod == null) return -1 ;
      try { return (Integer.parseInt(timerperiod)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getGifPeriod()
   {
      if (gifperiod == null) return -1 ;
      try { return (Integer.parseInt(gifperiod)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getScenePeriod()
   {
      if (sceneperiod == null) return -1 ;
      try { return (Integer.parseInt(sceneperiod)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getAudioPeriod()
   {
      if (audioperiod == null) return -1 ;
      try { return (Integer.parseInt(audioperiod)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getMaxFlex()
   {
      if (maxflex == null) return -1 ;
      try { return (Integer.parseInt(maxflex)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getMaxLock()
   {
      if (maxlock == null) return getMaxFlex() ;
      try { return (Integer.parseInt(maxlock)) ; }
      catch (Exception e) { }
      return getMaxFlex() ;
   }

	static public int getStickyFlex()
   {
      if (stickyflex == null) return -1 ;
      try { return (Integer.parseInt(stickyflex)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getMaxColorSet()
   {
      if (maxcolorset == null) return -1 ;
      try { return (Integer.parseInt(maxcolorset)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getMaxPageSet()
   {
      if (maxpageset == null) return -1 ;
      try { return (Integer.parseInt(maxpageset)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public float getJPEGQuality()
   {
      if (jpegquality == null) return 0.8f ;
      try { return (Float.parseFloat(jpegquality)) ; }
      catch (Exception e) { }
      return 0.8f ;
   }

	static public int getUndoLimit()
   {
      if (undolimit == null) return -1 ;
      try { return (Integer.parseInt(undolimit)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getCommentCol()
   {
      if (commentcol == null) return -1 ;
      try { return (Integer.parseInt(commentcol)) ; }
      catch (Exception e) { }
      return -1 ;
   }

	static public int getIndentSpace()
   {
      if (indentspace == null) return -1 ;
      try { return (Integer.parseInt(indentspace)) ; }
      catch (Exception e) { }
      return -1 ;
   }
   
	static public int getIconNumber()
   {
      if (iconnumber == null) return 0 ;
      return iconnumber.intValue() ;
   }
   
	static public int getSplashSetNumber()
   {
      if (splashsetnumber == null) return 0 ;
      return splashsetnumber.intValue() ;
   }
   
	static public String getSplashSetName(int i)
   {
      if (i < 0) return "" ;
      if (i >= backsetnames.size()) return "" ;
      return (String) backsetnames.elementAt(i) ;
   }
   
   static public String getCompatibilityMode()
   {
      if (playfkiss) return "PlayFKiss" ;
      else if (directkiss) return "DirectKiss" ;
      else if (gnomekiss) return "GnomeKiss" ;
      else if (kissld) return "KissLD" ;
      return null ;
   }
   
	static public String getKissWeb() { return kissweb ; }
	static public String getUserDir() { return userdir ; }
	static public String getWebSite() { return website ; }
	static public String getOnlineHelp() { return onlinehelp ; }
	static public String getSplashDir() { return splashdir ; }
	static public String getIconDir() { return icondir ; }
   static public Object getLanguage() { return language ; }
   static public Object getEncoding() { return encoding ; }
   static public Object getExportType() { return exporttype ; }
   static public Object getBrowser() { return browser ; }
   static public Object getInitLanguage() { return initlanguage ; }
   static public Object getInitEncoding() { return initencoding ; }
   static public Object getInitExportType() { return initexporttype ; }
   static public Object getInitBrowser() { return initbrowser ; }
   static public boolean getInitSecurityEnable() { return initsecurityenable ; }
   static public Vector getLruFiles() { return lrufiles ; }

   
   
   
   // WebSearch extensions
   // ------------------------------------------

	// Methods to retrieve current option values.

	static public boolean getUseDefaultWS() { return usedefaultws ; }
	static public boolean getClearMaster() { return clearmaster ; }
	static public boolean getSaveArchive() { return savearchive ; }
	static public boolean getSaveAsZip() { return saveaszip ; }
	static public boolean getSaveImage() { return saveimage ; }
	static public boolean getShowLoad() { return showload ; }
	static public String getImageDirectory() { return imagedirectory ; }
	static public String getHtmlDirectory() { return htmldirectory ; }
	static public String getDataDirectory() { return datadirectory ; }
	static public String getKissIndex() { return kissindex ; }

	static public int getThumbnailWidth()
   {
      try { return (Integer.parseInt(thumbwidth)) ; }
      catch (Exception e) { }
      return 50 ;
   }

	static public int getThumbnailHeight()
   {
      try { return (Integer.parseInt(thumbheight)) ; }
      catch (Exception e) { }
      return 50 ;
   }

	static public int getDownloadSize()
   {
      try { return (Integer.parseInt(downloadsize)) ; }
      catch (Exception e) { }
      return 1024 ;
   }

	static public int getThumbPage()
   {
      try { return (Integer.parseInt(thumbpage)) ; }
      catch (Exception e) { }
      return 0 ;
   }

   // End of WebSearch extensions
   // -----------------------------------------
   

	// Methods to programmatically set option values.

	static public void setSoundOn(boolean b) { sound = b ; }
	static public void setMovieOn(boolean b) { movie = b ; }
	static public void setTimerOn(boolean b) { timer = b ; }
	static public void setFKissOn(boolean b) { event = b ; }
	static public void setEditEnable(boolean b) { editenable = b ; }
	static public void setTempEditEnable(boolean b) { tempeditenable = b ; }
	static public void setSecurityEnable(boolean b) { securityenable = b ; }
	static public void setInitEdit(boolean b) { initedit = b ; }
	static public void setAnimateOn(boolean b) { animate = b ; }
	static public void setDebugMouse(boolean b) { debugmouse = b ; }
	static public void setDebugControl(boolean b) { debugcontrol = b ; }
	static public void setDebugLoad(boolean b) { debugload = b ; }
	static public void setDebugEdit(boolean b) { debugedit = b ; }
	static public void setDebugImage(boolean b) { debugimage = b ; }
	static public void setDebugEvent(boolean b) { debugevent = b ; }
	static public void setDebugAction(boolean b) { debugaction = b ; }
	static public void setDebugVariable(boolean b) { debugvariable = b ; }
	static public void setDebugFKiss(boolean b) { debugfkiss = b ; }
	static public void setDebugSound(boolean b) { debugsound = b ; }
	static public void setDebugMovie(boolean b) { debugmovie = b ; }
	static public void setDebugMedia(boolean b) { debugmedia = b ; }
	static public void setDebugSearch(boolean b) { debugsearch = b ; }
	static public void setDebugDisabled(boolean b) { debugdisabled = b ; }
	static public void setDebugComponent(boolean b) { debugcomponent = b ; }
	static public void setJavaSound(boolean b) { javasound = b ; }
	static public void setSaveSourceOn(boolean b) { savesource = b ; }
	static public void setShowDLPrompt(boolean b) { showdlprompt = b ; }
	static public void setShowTips(boolean b) { showtips = b ; }
	static public void setLoadCloseOn(boolean b) { loadclose = b ; }
	static public void setCacheAudio(boolean b) { cacheaudio = b ; }
	static public void setCacheVideo(boolean b) { cachevideo = b ; }
	static public void setCacheImage(boolean b) { cacheimage = b ; }
	static public void setScaleToFit(boolean b) { scaletofit = b ; }
	static public void setSizeToFit(boolean b) { sizetofit = b ; }
	static public void setRetainWindowSize(boolean b) { retainwindowsize = b ; }
	static public void setMaximizeWindow(boolean b) { maximizewindow = b ; }
	static public void setRandomSplash(boolean b) { randomsplash = b ; }
	static public void setShowBorder(boolean b) { showborder = b ; }
	static public void setInitToolbar(boolean b) { inittoolbar = b ; }
	static public void setTbTools(boolean b) { tbtools = b ; }
	static public void setTbColors(boolean b) { tbcolors = b ; }
	static public void setTbPages(boolean b) { tbpages = b ; }
	static public void setTbEdits(boolean b) { tbedits = b ; }
	static public void setTbCompat(boolean b) { tbcompat = b ; }
	static public void setInitStatusbar(boolean b) { initstatusbar = b ; }
	static public void setInitMenubar(boolean b) { initmenubar = b ; }
	static public void setImportCel(boolean b) { importcel = b ; }
	static public void setExportCel(boolean b) { exportcel = b ; }
	static public void setComponentCel(boolean b) { componentcel = b ; }
	static public void setImportComponent(boolean b) { importcomponent = b ; }
	static public void setConstrainMoves(boolean b) { constrainmoves = b ; }
	static public void setConstrainVisible(boolean b) { constrainvisible = b ; }
	static public void setConstrainFKiss(boolean b) { constrainfkiss = b ; }
	static public void setAutoScroll(boolean b) { autoscroll = b ; }
	static public void setDragMove(boolean b) { dragmove = b ; }
	static public void setReleaseMove(boolean b) { releasemove = b ; }
	static public void setDropFixdrop(boolean b) { dropfixdrop = b ; }
	static public void setCatchFixdrop(boolean b) { catchfixdrop = b ; }
	static public void setVisibleUnfix(boolean b) { visibleunfix = b ; }
	static public void setEarlyFix(boolean b) { earlyfix = b ; }
	static public void setDetachRestricted(boolean b) { detachrestricted = b ; }
	static public void setDetachMove(boolean b) { detachmove = b ; }
	static public void setDetachFix(boolean b) { detachfix = b ; }
	static public void setInvertGhost(boolean b) { invertghost = b ; }
	static public void setTransparentGroup(boolean b) { transparentgroup = b ; }
	static public void setMapCollide(boolean b) { mapcollide = b ; }
	static public void setMoveXYCollide(boolean b) { movexycollide = b ; }
	static public void setRetainKey(boolean b) { retainkey = b ; }
	static public void setStrictSyntax(boolean b) { strictsyntax = b ; }
	static public void setShowUndefs(boolean b) { showundefs = b ; }
	static public void setAutoEndif(boolean b) { autoendif = b ; }
	static public void setImmediateCollide(boolean b) { immediatecollide = b ; }
	static public void setImmediateUnfix(boolean b) { immediateunfix = b ; }
	static public void setImmediateEvent(boolean b) { immediateevent = b ; }
	static public void setAutoMediaLoop(boolean b) { automedialoop = b ; }
	static public void setAutoFullScreen(boolean b) { autofullscreen = b ; }
	static public void setSuspendMedia(boolean b) { suspendmedia = b ; }
	static public void setLongSoundMedia(boolean b) { longsoundmedia = b ; }
	static public void setAdjustMediaVolume(boolean b) { adjustmediavolume = b ; }
	static public void setStopMusic(boolean b) { stopmusic = b ; }
	static public void setSoundSingle(boolean b) { soundsingle = b ; }
	static public void setMediaMinimize(boolean b) { mediaminimize = b ; }
	static public void setMediaCenter(boolean b) { mediacenter = b ; }
	static public void setMediaMusicResume(boolean b) { mediamusicresume = b ; }
	static public void setKeepAspect(boolean b) { keepaspect = b ; }
	static public void setKeyCase(boolean b) { keycase = b ; }
	static public void setExpandEvents(boolean b) { expandevents = b ; }
	static public void setEnableShell(boolean b) { enableshell = b ; }
	static public void setXYOffsets(boolean b) { xyoffsets = b ; }
	static public void setAbsoluteWindow(boolean b) { absolutewindow = b ; }
	static public void setPanelVisible(boolean b) { panelvisible = b ; }
	static public void setMouseInOutBox(boolean b) { mouseinoutbox = b ; }
	static public void setContextMap(boolean b) { contextmap = b ; }
	static public void setMapCount(boolean b) { mapcount = b ; }
	static public void setAllAmbiguous(boolean b) { allambiguous = b ; }
	static public void setAltEditDrag(boolean b) { alteditdrag = b ; }
	static public void setShiftEditDrag(boolean b) { shifteditdrag = b ; }
	static public void setImportRelative(boolean b) { importrelative = b ; }
	static public void setWriteCelOffset(boolean b) { writeceloffset = b ; }
	static public void setShowBreakPointEnd(boolean b) { showbreakpointend = b ; }
	static public void setShowStepIntoEnd(boolean b) { showstepintoend = b ; }
	static public void setWriteComment(boolean b) { writecomment = b ; }
	static public void setEventPause(boolean b) { eventpause = b ; }
	static public void setActionPause(boolean b) { actionpause = b ; }
	static public void setDisableAll(boolean b) { disableall = b ; }
	static public void setSystemLF(boolean b) { systemlf = b ; }
	static public void setJavaLF(boolean b) { javalf = b ; }
	static public void setBackupFileOn(boolean b) { backup = b ; }
	static public void setVariableCase(boolean b) { variablecase = b ; }
   static public void setLruFiles(Vector v) { lrufiles = v ; }
	static public void setAppleMac(boolean b) { applemac = b ; }
	static public void setLinux(boolean b) { linux = b ; }
   static public void setDefaultPlayFKiss(boolean b) { defaultplayfkiss = b ; }
   static public void setAcceptCnfErrors(boolean b) { acceptcnferrors = b ; }
   static public void setPagesAreScenes(boolean b) { pagesarescenes = b ; }
   static public void setMultipleEvents(boolean b) { multipleevents = b ; }

   static public void setLruFile(String s) 
   { 
      if (lrufiles == null) lrufiles = new Vector() ;
      if (lrufiles.contains(s)) lrufiles.remove(s) ;
      lrufiles.insertElementAt(s,0) ;
   }
   
   static public void removeLruFile(String s) 
   { 
      if (lrufiles == null) return ;
      lrufiles.remove(s) ;
   }

	static public void setEventQueues(String s)
   { eventqueues = Variable.getStringLiteralValue(s) ; }

	static public void setTimerPeriod(String s)
   { timerperiod = Variable.getStringLiteralValue(s) ; }

	static public void setGifPeriod(String s)
   { gifperiod = Variable.getStringLiteralValue(s) ; }

	static public void setScenePeriod(String s)
   { sceneperiod = Variable.getStringLiteralValue(s) ; }

	static public void setAudioPeriod(String s)
   { audioperiod = Variable.getStringLiteralValue(s) ; }

	static public void setMaxFlex(String s)
   { maxflex = Variable.getStringLiteralValue(s) ; }

	static public void setMaxLock(String s)
   { maxlock = Variable.getStringLiteralValue(s) ; }

	static public void setStickyFlex(String s)
   { stickyflex = Variable.getStringLiteralValue(s) ; }

	static public void setMaxColorSet(String s)
   { maxcolorset = Variable.getStringLiteralValue(s) ; }

	static public void setMaxPageSet(String s)
   { maxpageset = Variable.getStringLiteralValue(s) ; }

	static public void setJpegQuality(String s)
   { jpegquality = Variable.getStringLiteralValue(s) ; }

	static public void setUndoLimit(String s)
   { undolimit = Variable.getStringLiteralValue(s) ; }

	static public void setCommentCol(String s)
   { commentcol = Variable.getStringLiteralValue(s) ; }

	static public void setIndentSpace(String s)
   { indentspace = Variable.getStringLiteralValue(s) ; }

	static public void setMaxLruFiles(String s)
   { maxlrufiles = Variable.getStringLiteralValue(s) ; }

	static public void setLongDuration(String s)
   { longduration = Variable.getStringLiteralValue(s) ; }

   static public void setMediaVolume(String s)
   { mediavolume = Variable.getStringLiteralValue(s) ; }

	static public void setUserDir(String s)
   { userdir = Variable.getStringLiteralValue(s) ; }

 	static public void setOnlineHelp(String s)
   { onlinehelp = Variable.getStringLiteralValue(s) ; }

 	static public void setWebSite(String s)
   { website = Variable.getStringLiteralValue(s) ; }

	static public void setSplashDir(String s)
   { splashdir = Variable.getStringLiteralValue(s) ; }
   
	static public void setSplashSetNumber(int n)
   { splashsetnumber = new Integer(n) ; }
   
	static public void setIconDir(String s)
   { icondir = Variable.getStringLiteralValue(s) ; }

	static public void setIconNumber(int n)
   { iconnumber = new Integer(n) ; }

	static public void setPlayFKissCompatibility(String s)
   { setPlayFKissCompatibility("true".equalsIgnoreCase(Variable.getStringLiteralValue(s)),false) ; }
	static public void setPlayFKissCompatibility(boolean b) { setPlayFKissCompatibility(b,true) ; }
	static public void setPlayFKissCompatibility(boolean b, boolean permanent)
   { 
      if (directkiss) setDirectKissCompatibility(false) ;
      if (gnomekiss) setGnomeKissCompatibility(false) ;
      if (kissld) setKissLDCompatibility(false) ;
      playfkiss = b ; 
      compatperm = permanent ;
      if (permanent) initplayfkiss = b ; 
      if (b) setPlayFKissOptions() ; else clearCompatibilityOptions() ;
   }

	static public void setDirectKissCompatibility(String s)
   { setDirectKissCompatibility("true".equalsIgnoreCase(Variable.getStringLiteralValue(s)),false) ; }
	static public void setDirectKissCompatibility(boolean b) { setDirectKissCompatibility(b,true) ; }
	static public void setDirectKissCompatibility(boolean b, boolean permanent)
   { 
      if (playfkiss) setPlayFKissCompatibility(false) ;
      if (gnomekiss) setGnomeKissCompatibility(false) ;
      if (kissld) setKissLDCompatibility(false) ;
      directkiss  = b ; 
      compatperm = permanent ;
      if (permanent) initdirectkiss = b ; 
      if (b) setDirectKissOptions() ; else clearCompatibilityOptions() ;
   }

	static public void setGnomeKissCompatibility(String s)
   { setGnomeKissCompatibility("true".equalsIgnoreCase(Variable.getStringLiteralValue(s)),false) ; }
	static public void setGnomeKissCompatibility(boolean b) { setGnomeKissCompatibility(b,true) ; }
	static public void setGnomeKissCompatibility(boolean b, boolean permanent)
   { 
      if (playfkiss) setPlayFKissCompatibility(false) ;
      if (directkiss) setDirectKissCompatibility(false) ;
      if (kissld) setKissLDCompatibility(false) ;
      gnomekiss = b ; 
      compatperm = permanent ;
      if (permanent) initgnomekiss = b ; 
      if (b) setGnomeKissOptions() ; else clearCompatibilityOptions() ;
   }

	static public void setKissLDCompatibility(String s)
   { setKissLDCompatibility("true".equalsIgnoreCase(Variable.getStringLiteralValue(s)),false) ; }
	static public void setKissLDCompatibility(boolean b) { setKissLDCompatibility(b,true) ; }
	static public void setKissLDCompatibility(boolean b, boolean permanent)
   { 
      if (playfkiss) setPlayFKissCompatibility(false) ;
      if (directkiss) setDirectKissCompatibility(false) ;
      if (gnomekiss) setGnomeKissCompatibility(false) ;
      kissld = b ; 
      compatperm = permanent ;
      if (permanent) initkissld = b ; 
      if (b) setKissLDOptions() ; else clearCompatibilityOptions() ;
   }

	static public void setKissWeb(String s)
   { 
      kissweb = Variable.getStringLiteralValue(s) ; 
      WebFrame.setCurrentWeb(null) ;
   }

	static public void setInitKissWeb(String s)
   { 
      initkissweb = s ; 
   }

   static public void setLanguage(String s)
   {
      language = Variable.getStringLiteralValue(s) ;
      if (language == null) return ;
      if (language.equals(Kisekae.getLanguage())) return ;
      Kisekae.setLanguage(Kisekae.getLanguageCode(language)) ;
   }

   static public void setEncoding(String s)
   {
      encoding = Variable.getStringLiteralValue(s) ;
      if (encoding == null) return ;
      if (encoding.equals(Kisekae.getLanguageEncoding())) return ;
      Kisekae.setLanguageEncoding(encoding) ;
   }

   static public void setExportType(String s)
   { exporttype = Variable.getStringLiteralValue(s) ; }

   static public void setBrowser(String s)
   { browser = Variable.getStringLiteralValue(s) ; }
   
   
   // WebSearch extensions
   // ---------------------------
   
	// Methods to programmatically set option values.

	static public void setUseDefaultWS(boolean b) { usedefaultws = b ; }
	static public void setClearMaster(boolean b) { clearmaster = b ; }
	static public void setSavedArchive(boolean b) { savearchive = b ; }
	static public void setSavedAsZip(boolean b) { saveaszip = b ; }
	static public void setSaveImage(boolean b) { saveimage = b ; }
	static public void setShowLoad(boolean b) { showload = b ; }
   static public void setThumbnailWidth(String s) { thumbwidth = s ; }
   static public void setThumbnailHeight(String s) { thumbheight = s ; }
   static public void setThumbPage(String s) { thumbpage = s ; }
   static public void setDownloadSize(String s) { downloadsize = s ; }

   // End of WebSearch extensions
   // ---------------------------


   
   // A function to dynamically set program options.

   static public void setOption(String option, String value)
   {
      boolean b = false ;
      if ("1".equals(value)) b = true ;
      if ("true".equalsIgnoreCase(value)) b = true ;
      if ("sound".equalsIgnoreCase(option)) setSoundOn(b) ;
      else if ("movie".equalsIgnoreCase(option)) setMovieOn(b) ;
      else if ("event".equalsIgnoreCase(option)) setFKissOn(b) ;
      else if ("animate".equalsIgnoreCase(option)) setAnimateOn(b) ;
      else if ("timer".equalsIgnoreCase(option)) setTimerOn(b) ;
      else if ("editenable".equalsIgnoreCase(option)) setEditEnable(b) ;
      else if ("securityenable".equalsIgnoreCase(option)) setSecurityEnable(b) ;
      else if ("initedit".equalsIgnoreCase(option)) setInitEdit(b) ;
      else if ("debugmouse".equalsIgnoreCase(option)) setDebugMouse(b) ;
      else if ("debugcontrol".equalsIgnoreCase(option)) setDebugControl(b) ;
      else if ("debugload".equalsIgnoreCase(option)) setDebugLoad(b) ;
      else if ("debugedit".equalsIgnoreCase(option)) setDebugEdit(b) ;
      else if ("debugimage".equalsIgnoreCase(option)) setDebugImage(b) ;
      else if ("debugevent".equalsIgnoreCase(option)) setDebugEvent(b) ;
      else if ("debugaction".equalsIgnoreCase(option)) setDebugAction(b) ;
      else if ("debugvariable".equalsIgnoreCase(option)) setDebugVariable(b) ;
      else if ("debugfkiss".equalsIgnoreCase(option)) setDebugFKiss(b) ;
      else if ("debugsound".equalsIgnoreCase(option)) setDebugSound(b) ;
      else if ("debugmovie".equalsIgnoreCase(option)) setDebugMovie(b) ;
      else if ("debugmedia".equalsIgnoreCase(option)) setDebugMedia(b) ;
      else if ("debugsearch".equalsIgnoreCase(option)) setDebugSearch(b) ;
      else if ("debugdisabled".equalsIgnoreCase(option)) setDebugDisabled(b) ;
      else if ("debugcomponent".equalsIgnoreCase(option)) setDebugComponent(b) ;
      else if ("javasound".equalsIgnoreCase(option)) setJavaSound(b) ;
      else if ("systemlf".equalsIgnoreCase(option)) setSystemLF(b) ;
      else if ("javalf".equalsIgnoreCase(option)) setJavaLF(b) ;
      else if ("applemac".equalsIgnoreCase(option)) setAppleMac(b) ;
      else if ("linux".equalsIgnoreCase(option)) setLinux(b) ;
      else if ("backup".equalsIgnoreCase(option)) setBackupFileOn(b) ;
      else if ("savesource".equalsIgnoreCase(option)) setSaveSourceOn(b) ;
      else if ("showdlprompt".equalsIgnoreCase(option)) setShowDLPrompt(b) ;
      else if ("showtips".equalsIgnoreCase(option)) setShowTips(b) ;
      else if ("loadclose".equalsIgnoreCase(option)) setLoadCloseOn(b) ;
      else if ("cacheaudio".equalsIgnoreCase(option)) setCacheAudio(b) ;
      else if ("cachevideo".equalsIgnoreCase(option)) setCacheVideo(b) ;
      else if ("cacheimage".equalsIgnoreCase(option)) setCacheImage(b) ;
      else if ("suspendmedia".equalsIgnoreCase(option)) setSuspendMedia(b) ;
      else if ("longsoundmedia".equalsIgnoreCase(option)) setLongSoundMedia(b) ;
      else if ("adjustmediavolume".equalsIgnoreCase(option)) setAdjustMediaVolume(b) ;
      else if ("stopmusic".equalsIgnoreCase(option)) setStopMusic(b) ;
      else if ("soundsingle".equalsIgnoreCase(option)) setSoundSingle(b) ;
      else if ("scaletofit".equalsIgnoreCase(option)) setScaleToFit(b) ;
      else if ("sizetofit".equalsIgnoreCase(option)) setSizeToFit(b) ;
      else if ("retainwindowsize".equalsIgnoreCase(option)) setRetainWindowSize(b) ;
      else if ("maximizewindow".equalsIgnoreCase(option)) setMaximizeWindow(b) ;
      else if ("randomsplash".equalsIgnoreCase(option)) setRandomSplash(b) ;
      else if ("showborder".equalsIgnoreCase(option)) setShowBorder(b) ;
      else if ("inittoolbar".equalsIgnoreCase(option)) setInitToolbar(b) ;
      else if ("tbtools".equalsIgnoreCase(option)) setTbTools(b) ;
      else if ("tbcolors".equalsIgnoreCase(option)) setTbColors(b) ;
      else if ("tbpages".equalsIgnoreCase(option)) setTbPages(b) ;
      else if ("tbedits".equalsIgnoreCase(option)) setTbEdits(b) ;
      else if ("tbcompat".equalsIgnoreCase(option)) setTbCompat(b) ;
      else if ("initstatusbar".equalsIgnoreCase(option)) setInitStatusbar(b) ;
      else if ("initmenubar".equalsIgnoreCase(option)) setInitMenubar(b) ;
      else if ("importcel".equalsIgnoreCase(option)) setImportCel(b) ;
      else if ("exportcel".equalsIgnoreCase(option)) setExportCel(b) ;
      else if ("componentcel".equalsIgnoreCase(option)) setComponentCel(b) ;
      else if ("importcomponent".equalsIgnoreCase(option)) setImportComponent(b) ;
      else if ("constrainmoves".equalsIgnoreCase(option)) setConstrainMoves(b) ;
      else if ("constrainvisible".equalsIgnoreCase(option)) setConstrainVisible(b) ;
      else if ("constrainfkiss".equalsIgnoreCase(option)) setConstrainFKiss(b) ;
      else if ("autoscroll".equalsIgnoreCase(option)) setAutoScroll(b) ;
      else if ("dragmove".equalsIgnoreCase(option)) setDragMove(b) ;
      else if ("releasemove".equalsIgnoreCase(option)) setReleaseMove(b) ;
      else if ("dropfixdrop".equalsIgnoreCase(option)) setDropFixdrop(b) ;
      else if ("catchfixdrop".equalsIgnoreCase(option)) setCatchFixdrop(b) ;
      else if ("visibleunfix".equalsIgnoreCase(option)) setVisibleUnfix(b) ;
      else if ("earlyfix".equalsIgnoreCase(option)) setEarlyFix(b) ;
      else if ("detachrestricted".equalsIgnoreCase(option)) setDetachRestricted(b) ;
      else if ("detachmove".equalsIgnoreCase(option)) setDetachMove(b) ;
      else if ("detachfix".equalsIgnoreCase(option)) setDetachFix(b) ;
      else if ("invertghost".equalsIgnoreCase(option)) setInvertGhost(b) ;
      else if ("transparentgroup".equalsIgnoreCase(option)) setTransparentGroup(b) ;
      else if ("mapcollide".equalsIgnoreCase(option)) setMapCollide(b) ;
      else if ("movexycollide".equalsIgnoreCase(option)) setMoveXYCollide(b) ;
      else if ("automedialoop".equalsIgnoreCase(option)) setAutoMediaLoop(b) ;
      else if ("autofullscreen".equalsIgnoreCase(option)) setAutoFullScreen(b) ;
      else if ("mediaminimize".equalsIgnoreCase(option)) setMediaMinimize(b) ;
      else if ("mediacenter".equalsIgnoreCase(option)) setMediaCenter(b) ;
      else if ("mediamusicresume".equalsIgnoreCase(option)) setMediaMusicResume(b) ;
      else if ("keepaspect".equalsIgnoreCase(option)) setKeepAspect(b) ;
      else if ("keycase".equalsIgnoreCase(option)) setKeyCase(b) ;
      else if ("variablecase".equalsIgnoreCase(option)) setVariableCase(b) ;
      else if ("expandevents".equalsIgnoreCase(option)) setExpandEvents(b) ;
      else if ("enableshell".equalsIgnoreCase(option)) setEnableShell(b) ;
      else if ("xyoffsets".equalsIgnoreCase(option)) setXYOffsets(b) ;
      else if ("absolutewindow".equalsIgnoreCase(option)) setAbsoluteWindow(b) ;
      else if ("panelvisible".equalsIgnoreCase(option)) setPanelVisible(b) ;
      else if ("mouseinoutbox".equalsIgnoreCase(option)) setMouseInOutBox(b) ;
      else if ("contextmap".equalsIgnoreCase(option)) setContextMap(b) ;
      else if ("mapcount".equalsIgnoreCase(option)) setMapCount(b) ;
      else if ("allambiguous".equalsIgnoreCase(option)) setAllAmbiguous(b) ;
      else if ("alteditdrag".equalsIgnoreCase(option)) setAltEditDrag(b) ;
      else if ("shifteditdrag".equalsIgnoreCase(option)) setShiftEditDrag(b) ;
      else if ("importrelative".equalsIgnoreCase(option)) setImportRelative(b) ;
      else if ("writeceloffset".equalsIgnoreCase(option)) setWriteCelOffset(b) ;
      else if ("showbreakpointend".equalsIgnoreCase(option)) setShowBreakPointEnd(b) ;
      else if ("showstepintoend".equalsIgnoreCase(option)) setShowStepIntoEnd(b) ;
      else if ("writecomment".equalsIgnoreCase(option)) setWriteComment(b) ;
      else if ("eventpause".equalsIgnoreCase(option)) setEventPause(b) ;
      else if ("actionpause".equalsIgnoreCase(option)) setActionPause(b) ;
      else if ("disableall".equalsIgnoreCase(option)) setDisableAll(b) ;
      else if ("retainkey".equalsIgnoreCase(option)) setRetainKey(b) ;
      else if ("strictsyntax".equalsIgnoreCase(option)) setStrictSyntax(b) ;
      else if ("showundefs".equalsIgnoreCase(option)) setShowUndefs(b) ;
      else if ("autoendif".equalsIgnoreCase(option)) setAutoEndif(b) ;
      else if ("immediatecollide".equalsIgnoreCase(option)) setImmediateCollide(b) ;
      else if ("immediateunfix".equalsIgnoreCase(option)) setImmediateUnfix(b) ;
      else if ("immediateevent".equalsIgnoreCase(option)) setImmediateEvent(b) ;
      else if ("eventqueues".equalsIgnoreCase(option)) setEventQueues(value) ;
      else if ("timerperiod".equalsIgnoreCase(option)) setTimerPeriod(value) ;
      else if ("gifperiod".equalsIgnoreCase(option)) setGifPeriod(value) ;
      else if ("sceneperiod".equalsIgnoreCase(option)) setScenePeriod(value) ;
      else if ("audioperiod".equalsIgnoreCase(option)) setAudioPeriod(value) ;
      else if ("stickyflex".equalsIgnoreCase(option)) setStickyFlex(value) ;
      else if ("maxflex".equalsIgnoreCase(option)) setMaxFlex(value) ;
      else if ("maxlock".equalsIgnoreCase(option)) setMaxLock(value) ;
      else if ("maxpageset".equalsIgnoreCase(option)) setMaxPageSet(value) ;
      else if ("maxcolorset".equalsIgnoreCase(option)) setMaxColorSet(value) ;
      else if ("jpegquality".equalsIgnoreCase(option)) setJpegQuality(value) ;
      else if ("undolimit".equalsIgnoreCase(option)) setUndoLimit(value) ;
      else if ("commentcol".equalsIgnoreCase(option)) setCommentCol(value) ;
      else if ("indentspace".equalsIgnoreCase(option)) setIndentSpace(value) ;
      else if ("maxlrufiles".equalsIgnoreCase(option)) setMaxLruFiles(value) ;
      else if ("longduration".equalsIgnoreCase(option)) setLongDuration(value) ;
      else if ("mediavolume".equalsIgnoreCase(option)) setMediaVolume(value) ;
      else if ("kissweb".equalsIgnoreCase(option)) setKissWeb(value) ;
      else if ("userdir".equalsIgnoreCase(option)) setUserDir(value) ;
      else if ("onlinehelp".equalsIgnoreCase(option)) setOnlineHelp(value) ;
      else if ("website".equalsIgnoreCase(option)) setWebSite(value) ;
      else if ("splashdir".equalsIgnoreCase(option)) setSplashDir(value) ;
      else if ("icondir".equalsIgnoreCase(option)) setIconDir(value) ;
      else if ("language".equalsIgnoreCase(option)) setLanguage(value) ;
      else if ("encoding".equalsIgnoreCase(option)) setEncoding(value) ;
      else if ("exporttype".equalsIgnoreCase(option)) setExportType(value) ;
      else if ("browser".equalsIgnoreCase(option)) setBrowser(value) ;
      else if ("playfkiss".equalsIgnoreCase(option)) setPlayFKissCompatibility(value) ;
      else if ("directkiss".equalsIgnoreCase(option)) setDirectKissCompatibility(value) ;
      else if ("gnomekiss".equalsIgnoreCase(option)) setGnomeKissCompatibility(value) ;
      else if ("kissld".equalsIgnoreCase(option)) setKissLDCompatibility(value) ;
      else if ("defaultplayfkiss".equalsIgnoreCase(option)) setDefaultPlayFKiss(b) ;
      else if ("acceptcnferrors".equalsIgnoreCase(option)) setAcceptCnfErrors(b) ;
      else if ("pagesarescenes".equalsIgnoreCase(option)) setPagesAreScenes(b) ;
      else if ("multipleevents".equalsIgnoreCase(option)) setMultipleEvents(b) ;
   }


   
   // A function to dynamically set program options.

   static public String getOption(String option)
   {
      String s = "" ;
      if ("sound".equalsIgnoreCase(option)) s += getSoundOn() ;
      else if ("movie".equalsIgnoreCase(option)) s += getMovieOn() ;
      else if ("event".equalsIgnoreCase(option)) s += getFKissOn() ;
      else if ("animate".equalsIgnoreCase(option)) s += getAnimateOn() ;
      else if ("timer".equalsIgnoreCase(option)) s += getTimerOn() ;
      else if ("editenable".equalsIgnoreCase(option)) s += getEditEnable() ;
      else if ("securityenable".equalsIgnoreCase(option)) s += getSecurityEnable() ;
      else if ("initedit".equalsIgnoreCase(option)) s += getInitEdit() ;
      else if ("debugmouse".equalsIgnoreCase(option)) s += getDebugMouse() ;
      else if ("debugcontrol".equalsIgnoreCase(option)) s += getDebugControl() ;
      else if ("debugload".equalsIgnoreCase(option)) s += getDebugLoad() ;
      else if ("debugedit".equalsIgnoreCase(option)) s += getDebugEdit() ;
      else if ("debugimage".equalsIgnoreCase(option)) s += getDebugImage() ;
      else if ("debugevent".equalsIgnoreCase(option)) s += getDebugEvent() ;
      else if ("debugaction".equalsIgnoreCase(option)) s += getDebugAction() ;
      else if ("debugvariable".equalsIgnoreCase(option)) s += getDebugVariable() ;
      else if ("debugfkiss".equalsIgnoreCase(option)) s += getDebugFKiss() ;
      else if ("debugsound".equalsIgnoreCase(option)) s += getDebugSound() ;
      else if ("debugmovie".equalsIgnoreCase(option)) s += getDebugMovie() ;
      else if ("debugmedia".equalsIgnoreCase(option)) s += getDebugMedia() ;
      else if ("debugsearch".equalsIgnoreCase(option)) s += getDebugSearch() ;
      else if ("debugdisabled".equalsIgnoreCase(option)) s += getDebugDisabled() ;
      else if ("debugcomponent".equalsIgnoreCase(option)) s += getDebugComponent() ;
      else if ("javasound".equalsIgnoreCase(option)) s += getJavaSound() ;
      else if ("systemlf".equalsIgnoreCase(option)) s += getSystemLF() ;
      else if ("applemac".equalsIgnoreCase(option)) s += getAppleMac() ;
      else if ("linux".equalsIgnoreCase(option)) s += getLinux() ;
      else if ("backup".equalsIgnoreCase(option)) s += getBackupFileOn() ;
      else if ("savesource".equalsIgnoreCase(option)) s += getSaveSourceOn() ;
      else if ("showdlprompt".equalsIgnoreCase(option)) s += getShowDLPrompt() ;
      else if ("showtips".equalsIgnoreCase(option)) s += getShowTips() ;
      else if ("loadclose".equalsIgnoreCase(option)) s += getLoadCloseOn() ;
      else if ("cacheaudio".equalsIgnoreCase(option)) s += getCacheAudio() ;
      else if ("cachevideo".equalsIgnoreCase(option)) s += getCacheVideo() ;
      else if ("cacheimage".equalsIgnoreCase(option)) s += getCacheImage() ;
      else if ("suspendmedia".equalsIgnoreCase(option)) s += getSuspendMedia() ;
      else if ("longsoundmedia".equalsIgnoreCase(option)) s += getLongSoundMedia() ;
      else if ("adjustmediavolume".equalsIgnoreCase(option)) s += getAdjustMediaVolume() ;
      else if ("stopmusic".equalsIgnoreCase(option)) s += getStopMusic() ;
      else if ("soundsingle".equalsIgnoreCase(option)) s += getSoundSingle() ;
      else if ("scaletofit".equalsIgnoreCase(option)) s += getScaleToFit() ;
      else if ("sizetofit".equalsIgnoreCase(option)) s += getSizeToFit() ;
      else if ("retainwindowsize".equalsIgnoreCase(option)) s += getRetainWindowSize() ;
      else if ("maximizewindow".equalsIgnoreCase(option)) s += getMaximizeWindow() ;
      else if ("randomsplash".equalsIgnoreCase(option)) s += getRandomSplash() ;
      else if ("showborder".equalsIgnoreCase(option)) s += getShowBorder() ;
      else if ("inittoolbar".equalsIgnoreCase(option)) s += getInitToolbar() ;
      else if ("tbtools".equalsIgnoreCase(option)) s += getTbTools() ;
      else if ("tbcolors".equalsIgnoreCase(option)) s += getTbColors() ;
      else if ("tbedits".equalsIgnoreCase(option)) s += getTbEdits() ;
      else if ("tbcompat".equalsIgnoreCase(option)) s += getTbCompat() ;
      else if ("initstatusbar".equalsIgnoreCase(option)) s += getInitStatusbar() ;
      else if ("initmenubar".equalsIgnoreCase(option)) s += getInitMenubar() ;
      else if ("importcel".equalsIgnoreCase(option)) s += getImportCel() ;
      else if ("exportcel".equalsIgnoreCase(option)) s += getExportCel() ;
      else if ("componentcel".equalsIgnoreCase(option)) s += getComponentCel() ;
      else if ("importcomponent".equalsIgnoreCase(option)) s += getImportComponent() ;
      else if ("constrainmoves".equalsIgnoreCase(option)) s += getConstrainMoves() ;
      else if ("constrainvisible".equalsIgnoreCase(option)) s += getConstrainVisible() ;
      else if ("constrainfkiss".equalsIgnoreCase(option)) s += getConstrainFKiss() ;
      else if ("autoscroll".equalsIgnoreCase(option)) s += getAutoScroll() ;
      else if ("dragmove".equalsIgnoreCase(option)) s += getDragMove() ;
      else if ("releasemove".equalsIgnoreCase(option)) s += getReleaseMove() ;
      else if ("dropfixdrop".equalsIgnoreCase(option)) s += getDropFixdrop() ;
      else if ("catchfixdrop".equalsIgnoreCase(option)) s += getCatchFixdrop() ;
      else if ("visibleunfix".equalsIgnoreCase(option)) s += getVisibleUnfix() ;
      else if ("earlyfix".equalsIgnoreCase(option)) s += getEarlyFix() ;
      else if ("detachrestricted".equalsIgnoreCase(option)) s += getDetachRestricted() ;
      else if ("detachmove".equalsIgnoreCase(option)) s += getDetachMove() ;
      else if ("detachfix".equalsIgnoreCase(option)) s += getDetachFix() ;
      else if ("invertghost".equalsIgnoreCase(option)) s += getInvertGhost() ;
      else if ("transparentgroup".equalsIgnoreCase(option)) s += getTransparentGroup() ;
      else if ("mapcollide".equalsIgnoreCase(option)) s += getMapCollide() ;
      else if ("movexycollide".equalsIgnoreCase(option)) s += getMoveXYCollide() ;
      else if ("automedialoop".equalsIgnoreCase(option)) s += getAutoMediaLoop() ;
      else if ("autofullscreen".equalsIgnoreCase(option)) s += getAutoFullScreen() ;
      else if ("mediaminimize".equalsIgnoreCase(option)) s += getMediaMinimize() ;
      else if ("mediacenter".equalsIgnoreCase(option)) s += getMediaCenter() ;
      else if ("mediamusicresume".equalsIgnoreCase(option)) s += getMediaMusicResume() ;
      else if ("keepaspect".equalsIgnoreCase(option)) s += getKeepAspect() ;
      else if ("keycase".equalsIgnoreCase(option)) s += getKeyCase() ;
      else if ("variablecase".equalsIgnoreCase(option)) s += getVariableCase() ;
      else if ("expandevents".equalsIgnoreCase(option)) s += getExpandEvents() ;
      else if ("enableshell".equalsIgnoreCase(option)) s += getEnableShell() ;
      else if ("xyoffsets".equalsIgnoreCase(option)) s += getXYOffsets() ;
      else if ("absolutewindow".equalsIgnoreCase(option)) s += getAbsoluteWindow() ;
      else if ("panelvisible".equalsIgnoreCase(option)) s += getPanelVisible() ;
      else if ("mouseinoutbox".equalsIgnoreCase(option)) s += getMouseInOutBox() ;
      else if ("contextmap".equalsIgnoreCase(option)) s += getContextMap() ;
      else if ("mapcount".equalsIgnoreCase(option)) s += getMapCount() ;
      else if ("allambiguous".equalsIgnoreCase(option)) s += getAllAmbiguous() ;
      else if ("alteditdrag".equalsIgnoreCase(option)) s += getAltEditDrag() ;
      else if ("shifteditdrag".equalsIgnoreCase(option)) s += getShiftEditDrag() ;
      else if ("importrelative".equalsIgnoreCase(option)) s += getImportRelative() ;
      else if ("writeceloffset".equalsIgnoreCase(option)) s += getWriteCelOffset() ;
      else if ("showbreakpointend".equalsIgnoreCase(option)) s += getShowBreakPointEnd() ;
      else if ("showstepintoend".equalsIgnoreCase(option)) s += getShowStepIntoEnd() ;
      else if ("writecomment".equalsIgnoreCase(option)) s += getWriteComment() ;
      else if ("eventpause".equalsIgnoreCase(option)) s += getEventPause() ;
      else if ("actionpause".equalsIgnoreCase(option)) s += getActionPause() ;
      else if ("disableall".equalsIgnoreCase(option)) s += getDisableAll() ;
      else if ("retainkey".equalsIgnoreCase(option)) s += getRetainKey() ;
      else if ("strictsyntax".equalsIgnoreCase(option)) s += getStrictSyntax() ;
      else if ("showundefs".equalsIgnoreCase(option)) s += getShowUndefs() ;
      else if ("autoendif".equalsIgnoreCase(option)) s += getAutoEndif() ;
      else if ("immediatecollide".equalsIgnoreCase(option)) s += getImmediateCollide() ;
      else if ("immediateunfix".equalsIgnoreCase(option)) s += getImmediateUnfix() ;
      else if ("immediateevent".equalsIgnoreCase(option)) s += getImmediateEvent() ;
      else if ("eventqueues".equalsIgnoreCase(option)) s += getEventQueues() ;
      else if ("timerperiod".equalsIgnoreCase(option)) s += getTimerPeriod() ;
      else if ("gifperiod".equalsIgnoreCase(option)) s += getGifPeriod() ;
      else if ("sceneperiod".equalsIgnoreCase(option)) s += getScenePeriod() ;
      else if ("audioperiod".equalsIgnoreCase(option)) s += getAudioPeriod() ;
      else if ("stickyflex".equalsIgnoreCase(option)) s += getStickyFlex() ;
      else if ("maxflex".equalsIgnoreCase(option)) s += getMaxFlex() ;
      else if ("maxlock".equalsIgnoreCase(option)) s += getMaxLock() ;
      else if ("maxpageset".equalsIgnoreCase(option)) s += getMaxPageSet() ;
      else if ("maxcolorset".equalsIgnoreCase(option)) s += getMaxColorSet() ;
//    else if ("jpegquality".equalsIgnoreCase(option)) s += getJpegQuality() ;
      else if ("undolimit".equalsIgnoreCase(option)) s += getUndoLimit() ;
      else if ("commentcol".equalsIgnoreCase(option)) s += getCommentCol() ;
      else if ("indentspace".equalsIgnoreCase(option)) s += getIndentSpace() ;
      else if ("maxlrufiles".equalsIgnoreCase(option)) s += getMaxLruFiles() ;
      else if ("longduration".equalsIgnoreCase(option)) s += getLongDuration() ;
      else if ("mediavolume".equalsIgnoreCase(option)) s += getMediaVolume() ;
      else if ("kissweb".equalsIgnoreCase(option)) s += getKissWeb() ;
      else if ("userdir".equalsIgnoreCase(option)) s += getUserDir() ;
      else if ("onlinehelp".equalsIgnoreCase(option)) s += getOnlineHelp() ;
      else if ("website".equalsIgnoreCase(option)) s += getWebSite() ;
      else if ("splashdir".equalsIgnoreCase(option)) s += getSplashDir() ;
      else if ("icondir".equalsIgnoreCase(option)) s += getIconDir() ;
      else if ("language".equalsIgnoreCase(option)) s += getLanguage() ;
      else if ("encoding".equalsIgnoreCase(option)) s += getEncoding() ;
      else if ("exporttype".equalsIgnoreCase(option)) s += getExportType() ;
      else if ("browser".equalsIgnoreCase(option)) s += getBrowser() ;
      else if ("playfkiss".equalsIgnoreCase(option)) s += getPlayFKissCompatibility() ;
      else if ("directkiss".equalsIgnoreCase(option)) s += getDirectKissCompatibility() ;
      else if ("gnomekiss".equalsIgnoreCase(option)) s += getGnomeKissCompatibility() ;
      else if ("kissld".equalsIgnoreCase(option)) s += getKissLDCompatibility() ;
      else if ("multipleevents".equalsIgnoreCase(option)) s += getMultipleEvents() ;
      return s ;
   }


	// Set controls to current values when window is on display.

	public void show()
	{
		setControls() ;
		super.show() ;
	}


	// Helper functions.

	public void setControls()
	{
		SoundOption.setSelected(sound) ;
		MovieOption.setSelected(movie) ;
		TimerOption.setSelected(timer) ;
		EventOption.setSelected(event) ;
		AnimateOption.setSelected(animate) ;
		EditEnable.setSelected(editenable) ;
		SecurityEnable.setSelected(securityenable) ;
		InitEdit.setSelected(initedit) ;
		MouseDebug.setSelected(debugmouse) ;
		ControlDebug.setSelected(debugcontrol) ;
		LoadDebug.setSelected(debugload) ;
		EditDebug.setSelected(debugedit) ;
		ImageDebug.setSelected(debugimage) ;
		EventDebug.setSelected(debugevent) ;
		ActionDebug.setSelected(debugaction) ;
		VariableDebug.setSelected(debugvariable) ;
		FKissDebug.setSelected(debugfkiss) ;
		SoundDebug.setSelected(debugsound) ;
		MovieDebug.setSelected(debugmovie) ;
		MediaDebug.setSelected(debugmedia) ;
		SearchDebug.setSelected(debugsearch) ;
		DisabledDebug.setSelected(debugdisabled) ;
		ComponentDebug.setSelected(debugcomponent) ;
		JavaSoundDebug.setSelected(javasound) ;
      SystemLF.setSelected(systemlf);
      JavaLF.setSelected(javalf);
      AppleMac.setSelected(applemac);
      PlayFKissBtn.setSelected(playfkiss);
      DirectKissBtn.setSelected(directkiss);
      GnomeKissBtn.setSelected(gnomekiss);
      KissLDBtn.setSelected(kissld);
      DefaultPlayFKiss.setSelected(defaultplayfkiss);
      AcceptCnfErrors.setSelected(acceptcnferrors);
      PagesAreScenes.setSelected(pagesarescenes);
      MultipleEvents.setSelected(multipleevents);
      Backup.setSelected(backup);
      SaveSource.setSelected(savesource);
      ShowDLPrompt.setSelected(showdlprompt);
      ShowTips.setSelected(showtips);
      LoadClose.setSelected(loadclose);
      EventQueues.setText(eventqueues);
      TimerPeriod.setText(timerperiod);
      GifPeriod.setText(gifperiod);
      ScenePeriod.setText(sceneperiod);
      AudioPeriod.setText(audioperiod);
      StickyFlex.setText(stickyflex);
      MaxFlex.setText(maxflex);
      MaxLock.setText(maxlock);
      MaxPageSet.setText(maxpageset);
      MaxColorSet.setText(maxcolorset);
      MaxLruFiles.setText(maxlrufiles);
      LongDuration.setText(longduration);
      MediaVolume.setText(mediavolume);
		CacheAudio.setSelected(cacheaudio);
		CacheAudio1.setSelected(cacheaudio);
		CacheVideo.setSelected(cachevideo);
		CacheVideo1.setSelected(cachevideo);
		CacheImage.setSelected(cacheimage);
		ScaleToFit.setSelected(scaletofit);
		SizeToFit.setSelected(sizetofit);
		RetainWindowSize.setSelected(retainwindowsize);
		MaximizeWindow.setSelected(maximizewindow);
		RandomSplash.setSelected(randomsplash);
		ShowBorder.setSelected(showborder);
		InitToolBar.setSelected(inittoolbar);
		TbTools.setSelected(tbtools);
		TbColors.setSelected(tbcolors);
		TbPages.setSelected(tbpages);
		TbEdits.setSelected(tbedits);
		TbCompat.setSelected(tbcompat);
		InitStatusBar.setSelected(initstatusbar);
		InitMenuBar.setSelected(initmenubar);
		ImportCel.setSelected(importcel);
		ExportCel.setSelected(exportcel);
		ComponentCel.setSelected(componentcel);
		ImportComponent.setSelected(importcomponent);
		ConstrainMoves.setSelected(constrainmoves);
		ConstrainVisible.setSelected(constrainvisible);
		ConstrainFKiss.setSelected(constrainfkiss);
		AutoScroll.setSelected(autoscroll);
		ReleaseMove.setSelected(releasemove);
		DragMove.setSelected(dragmove);
		DropFixdrop.setSelected(dropfixdrop);
		CatchFixdrop.setSelected(catchfixdrop);
		VisibleUnfix.setSelected(visibleunfix);
		EarlyFix.setSelected(earlyfix);
		DetachRestricted.setSelected(detachrestricted);
		DetachMove.setSelected(detachmove);
		DetachFix.setSelected(detachfix);
		InvertGhost.setSelected(invertghost);
		TransparentGroup.setSelected(transparentgroup);
		MapCollide.setSelected(mapcollide);
		MoveXYCollide.setSelected(movexycollide);
		RetainKey.setSelected(retainkey);
		StrictSyntax.setSelected(strictsyntax);
		ShowUndefs.setSelected(showundefs);
		AutoEndif.setSelected(autoendif);
		ImmediateCollide.setSelected(immediatecollide);
		ImmediateUnfix.setSelected(immediateunfix);
		ImmediateEvent.setSelected(immediateevent);
		AutoMediaLoop.setSelected(automedialoop);
		AutoFullScreen.setSelected(autofullscreen);
		SuspendMedia.setSelected(suspendmedia);
		LongSoundMedia.setSelected(longsoundmedia);
		AdjustMediaVolume.setSelected(adjustmediavolume);
		StopMusic.setSelected(stopmusic);
		SoundSingle.setSelected(soundsingle);
		MediaMinimize.setSelected(mediaminimize);
		MediaCenter.setSelected(mediacenter);
		MediaMusicResume.setSelected(mediamusicresume);
		KeepAspect.setSelected(keepaspect);
		KeyCase.setSelected(keycase);
		VariableCase.setSelected(variablecase);
		ExpandEvents.setSelected(expandevents);
		EnableShell.setSelected(enableshell);
		XYOffsets.setSelected(xyoffsets);
		AbsoluteWindow.setSelected(absolutewindow);
		PanelVisible.setSelected(panelvisible);
		MouseInOutBox.setSelected(mouseinoutbox);
		ContextMap.setSelected(contextmap);
		MapCount.setSelected(mapcount);
		AllAmbiguous.setSelected(allambiguous);
		AltEditDrag.setSelected(alteditdrag);
		ShiftEditDrag.setSelected(shifteditdrag);
		ImportRelative.setSelected(importrelative);
		WriteCelOffset.setSelected(writeceloffset);
		ShowBreakPointEnd.setSelected(showbreakpointend);
		ShowStepIntoEnd.setSelected(showstepintoend);
		WriteComment.setSelected(writecomment);
      CommentCol.setText(commentcol);
      IndentSpace.setText(indentspace);
      KissWeb.setText(kissweb);
      UserDirectory.setText(userdir);
      OnlineHelp.setText(onlinehelp);
      WebSite.setText(website);
      SplashDirectory.setText(splashdir);
      IconDirectory.setText(icondir);
		EventPause.setSelected(eventpause);
		ActionPause.setSelected(actionpause);
		DisableAll.setSelected(disableall);
      EncodingBox.setSelectedItem(encoding);
      ExportBox.setSelectedItem(exporttype);
      BrowserBox.setSelectedItem(browser);
      
//    Turn off look and feel changes if Apple
      if (applemac)
      {
         JavaLF.setEnabled(false) ;
         SystemLF.setEnabled(false) ;
         JavaLF.setSelected(false) ;
         SystemLF.setSelected(true) ;
         javalf = false ;
         systemlf = true ;
      }
      AppleMac.setEnabled(applemac) ;
 
//    Turn off alt drag if Linux
      if (linux)
      {
         AltEditDrag.setSelected(false) ;
         ShiftEditDrag.setSelected(true) ;
         alteditdrag = false ;
         shifteditdrag = true ;
      }
    
//    LanguageBox.setSelectedItem(language);
      ComboBoxModel m = LanguageBox.getModel() ;
      if (m != null)
      {
         for (int i = 0 ; i < m.getSize() ; i++)
         {
            Object o = m.getElementAt(i) ;
            if (o == null) continue ;
            if (o.toString().equals(language)) 
            {
               m.setSelectedItem(o) ;
               break ;
            }
         }
      }
      
//    Ensure SplashList shows current splash selection (after reset)
      if (splashsetnumber instanceof Integer)
      {
         int n = ((Integer) splashsetnumber).intValue() - 1;
         if (n < 0) n = 0 ;
         SplashList.ensureIndexIsVisible(n) ;
      }
      
//    Disable SAVE function if we have a loaded configuration
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      SAVE.setEnabled(config == null);

      
      // WebSearch extensions
      // -----------------------
      
      UseDefaultWS.setSelected(usedefaultws) ;
      ClearMaster.setSelected(clearmaster) ;
		SaveArchive.setSelected(savearchive) ;
		SaveAsZip.setSelected(saveaszip) ;
		SaveImage.setSelected(saveimage) ;
		ShowLoad.setSelected(showload) ;
      ImageDirectory.setText(imagedirectory);
      HtmlDirectory.setText(htmldirectory);
      DataDirectory.setText(datadirectory);
      ImageDirectory.setText(imagedirectory);
      KissIndex.setText(kissindex);
      ThumbWidth.setText(thumbwidth) ;
      ThumbHeight.setText(thumbheight) ;
      ThumbPage.setText(thumbpage) ;
      DownloadSize.setText(downloadsize) ;

      // End of WebSearch extensions
      // --------------------------------
      
	}

   public void setSplashListIndex()
   {
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { setSplashListIndex() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      
      if (!(splashsetnumber instanceof Integer)) return ;
      int n = ((Integer) splashsetnumber).intValue() - 1;
      if (n < 0) n = 0 ;
      SplashList.ensureIndexIsVisible(n) ;
   }

	private void setOptions()
	{
		sound = SoundOption.isSelected() ;
		movie = MovieOption.isSelected() ;
		timer = TimerOption.isSelected() ;
		event = EventOption.isSelected() ;
		animate = AnimateOption.isSelected() ;
		editenable = EditEnable.isSelected() ;
		securityenable = SecurityEnable.isSelected() ;
		initedit = InitEdit.isSelected() ;
		debugmouse = MouseDebug.isSelected() ;
		debugcontrol = ControlDebug.isSelected() ;
		debugload = LoadDebug.isSelected() ;
		debugedit = EditDebug.isSelected() ;
		debugimage = ImageDebug.isSelected() ;
		debugevent = EventDebug.isSelected() ;
		debugaction = ActionDebug.isSelected() ;
		debugvariable = VariableDebug.isSelected() ;
		debugfkiss = FKissDebug.isSelected() ;
		debugsound = SoundDebug.isSelected() ;
		debugmovie = MovieDebug.isSelected() ;
		debugmedia = MediaDebug.isSelected() ;
		debugsearch = SearchDebug.isSelected() ;
		debugdisabled = DisabledDebug.isSelected() ;
		debugcomponent = ComponentDebug.isSelected() ;
		javasound = JavaSoundDebug.isSelected() ;
      systemlf = SystemLF.isSelected() ;
      javalf = JavaLF.isSelected() ;
      applemac = AppleMac.isSelected() ;
      playfkiss = PlayFKissBtn.isSelected() ;
      directkiss = DirectKissBtn.isSelected() ;
      gnomekiss = GnomeKissBtn.isSelected() ;
      kissld = KissLDBtn.isSelected() ;
      defaultplayfkiss = DefaultPlayFKiss.isSelected() ;
      acceptcnferrors = AcceptCnfErrors.isSelected() ;
      pagesarescenes = PagesAreScenes.isSelected() ;
      multipleevents = MultipleEvents.isSelected() ;
      backup = Backup.isSelected() ;
      savesource = SaveSource.isSelected() ;
      showdlprompt = ShowDLPrompt.isSelected() ;
      showtips = ShowTips.isSelected() ;
      loadclose = LoadClose.isSelected() ;
		eventqueues = EventQueues.getText() ;
      timerperiod = TimerPeriod.getText() ;
      gifperiod = GifPeriod.getText() ;
      sceneperiod = ScenePeriod.getText() ;
      audioperiod = AudioPeriod.getText() ;
      stickyflex = StickyFlex.getText() ;
      maxflex = MaxFlex.getText() ;
      maxlock = MaxLock.getText() ;
      maxpageset = MaxPageSet.getText() ;
      maxcolorset = MaxColorSet.getText() ;
      maxlrufiles = MaxLruFiles.getText() ;
      longduration = LongDuration.getText() ;
      mediavolume = MediaVolume.getText() ;
		cacheaudio = CacheAudio.isSelected() ;
		cachevideo = CacheVideo.isSelected() ;
		cacheimage = CacheImage.isSelected() ;
		scaletofit = ScaleToFit.isSelected() ;
		sizetofit = SizeToFit.isSelected() ;
		retainwindowsize = RetainWindowSize.isSelected() ;
		maximizewindow = MaximizeWindow.isSelected() ;
		randomsplash = RandomSplash.isSelected() ;
		showborder = ShowBorder.isSelected() ;
		inittoolbar = InitToolBar.isSelected() ;
		tbtools = TbTools.isSelected() ;
		tbcolors = TbColors.isSelected() ;
		tbpages = TbPages.isSelected() ;
		tbedits = TbEdits.isSelected() ;
		tbcompat = TbCompat.isSelected() ;
		initstatusbar = InitStatusBar.isSelected() ;
		initmenubar = InitMenuBar.isSelected() ;
		importcel = ImportCel.isSelected() ;
		exportcel = ExportCel.isSelected() ;
		componentcel = ComponentCel.isSelected() ;
		importcomponent = ImportComponent.isSelected() ;
		constrainmoves = ConstrainMoves.isSelected() ;
		constrainvisible = ConstrainVisible.isSelected() ;
		constrainfkiss = ConstrainFKiss.isSelected() ;
		autoscroll = AutoScroll.isSelected() ;
		dragmove = DragMove.isSelected() ;
		releasemove = ReleaseMove.isSelected() ;
		dropfixdrop = DropFixdrop.isSelected() ;
		catchfixdrop = CatchFixdrop.isSelected() ;
		visibleunfix = VisibleUnfix.isSelected() ;
		earlyfix = EarlyFix.isSelected() ;
		detachrestricted = DetachRestricted.isSelected() ;
		detachmove = DetachMove.isSelected() ;
		detachfix = DetachFix.isSelected() ;
		invertghost = InvertGhost.isSelected() ;
		transparentgroup = TransparentGroup.isSelected() ;
		mapcollide = MapCollide.isSelected() ;
		movexycollide = MoveXYCollide.isSelected() ;
		retainkey = RetainKey.isSelected() ;
		strictsyntax = StrictSyntax.isSelected() ;
		showundefs = ShowUndefs.isSelected() ;
		autoendif = AutoEndif.isSelected() ;
		immediatecollide = ImmediateCollide.isSelected() ;
		immediateunfix = ImmediateUnfix.isSelected() ;
		immediateevent = ImmediateEvent.isSelected() ;
		automedialoop = AutoMediaLoop.isSelected() ;
		autofullscreen = AutoFullScreen.isSelected() ;
		longsoundmedia = LongSoundMedia.isSelected() ;
		adjustmediavolume = AdjustMediaVolume.isSelected() ;
		stopmusic = StopMusic.isSelected() ;
		soundsingle = SoundSingle.isSelected() ;
		mediaminimize = MediaMinimize.isSelected() ;
		mediacenter = MediaCenter.isSelected() ;
		mediamusicresume = MediaMusicResume.isSelected() ;
		keepaspect = KeepAspect.isSelected() ;
		keycase = KeyCase.isSelected() ;
		variablecase = VariableCase.isSelected() ;
		expandevents = ExpandEvents.isSelected() ;
		enableshell = EnableShell.isSelected() ;
		xyoffsets = XYOffsets.isSelected() ;
		absolutewindow = AbsoluteWindow.isSelected() ;
		panelvisible = PanelVisible.isSelected() ;
		mouseinoutbox = MouseInOutBox.isSelected() ;
		contextmap = ContextMap.isSelected() ;
		mapcount = MapCount.isSelected() ;
		allambiguous = AllAmbiguous.isSelected() ;
		alteditdrag = AltEditDrag.isSelected() ;
		shifteditdrag = ShiftEditDrag.isSelected() ;
		importrelative = ImportRelative.isSelected() ;
		writeceloffset = WriteCelOffset.isSelected() ;
		showbreakpointend = ShowBreakPointEnd.isSelected() ;
		showstepintoend = ShowStepIntoEnd.isSelected() ;
		writecomment = WriteComment.isSelected() ;
		commentcol = CommentCol.getText() ;
		indentspace = IndentSpace.getText() ;
		kissweb = KissWeb.getText() ;
		userdir = UserDirectory.getText() ;
		onlinehelp = OnlineHelp.getText() ;
		website = WebSite.getText() ;
		splashdir = SplashDirectory.getText() ;
		icondir = IconDirectory.getText() ;
		eventpause = EventPause.isSelected() ;
		actionpause = ActionPause.isSelected() ;
		disableall = DisableAll.isSelected() ;
      encoding = EncodingBox.getSelectedItem() ;
      exporttype = ExportBox.getSelectedItem() ;
      browser = BrowserBox.getSelectedItem() ;
      language = LanguageBox.getSelectedItem() ;
      if (language != null) language = language.toString() ;

      // Set initial options if we do not have an active configuration.

      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      if (config == null) setInitOptions() ;

		// Turn off all media immediately.

		if (!sound) Audio.stop() ;
		if (!movie) Video.stop() ;

		// Set the animation state.

		FKissEvent.setEnabled(event) ;

      // Set the timer enable.

      AlarmTimer.setEnabled(timer) ;
      GifTimer.setEnabled(animate) ;

      // Reset our WebFrame history if we set a new KissWeb URL.

      if (!kissweb.equals(initkissweb)) 
      {
         WebFrame.reset() ;
         WebFrame.setCurrentWeb(null) ;
      }

      // Change our default directory if required.

      if (!userdir.equals(Kisekae.getBaseDir())) 
      {
         setUserDir(Kisekae.setBase(userdir)) ;
         UserDirectory.setText(userdir);
      }

      // Change our background splash image if required.

      if (!splashdir.equals(initsplashdir)) 
      {
         if (mf != null) mf.setNewSplashPane(true) ;
         initsplashdir = splashdir ;
         splashrestart = true ;
      }

      // Change our background icon image if required.

      if (!icondir.equals(initicondir)) 
      {
         if (mf != null) mf.setNewSplashPane(true) ;
         initicondir = icondir ;
      }
      
      // Set our security option.
      
      Kisekae.setSecure(securityenable) ;
      initsecurityenable = securityenable ;
      
      
      // WebSearch extensions
      // ----------------------
      
   	usedefaultws = UseDefaultWS.isSelected() ;
   	clearmaster = ClearMaster.isSelected() ;
		savearchive = SaveArchive.isSelected() ;
		saveaszip = SaveAsZip.isSelected() ;
		saveimage = SaveImage.isSelected() ;
		showload = ShowLoad.isSelected() ;
		imagedirectory = ImageDirectory.getText() ;
		htmldirectory = HtmlDirectory.getText() ;
		datadirectory = DataDirectory.getText() ;
		kissindex = KissIndex.getText() ;
      thumbwidth = ThumbWidth.getText() ;
      thumbheight = ThumbHeight.getText() ;
      thumbpage = ThumbPage.getText() ;
      downloadsize = DownloadSize.getText() ;

      // End of WebSearch extensions
      // ------------------------------
      
	}

   // A function to reset the options to the initial state.

	void resetOptions()
	{
//		sound = initsound ;
//		movie = initmovie ;
//    systemlf = initsystemlf ;
//    javalf = initjavalf ;
//		kissweb = initkissweb ;
//		userdir = inituserdir ;
//		onlinehelp = initonlinehelp ;
//		website = initwebsite ;
		splashdir = initsplashdir ;
//		iocndir = initicondir ;
//    language = initlanguage ;
//    encoding = initencoding ;
//    exporttype = initexporttype ;
//    browser = initbrowser ;
//		applemac = initapplemac ;
//		linux = initlinux ;
//		playfkiss = initplayfkiss ;
//		directkiss = initdirectkiss ;
//		gnomekiss = initgnomekiss ;
//		kissld = initkissld ;
//    showdlprompt = initshowdlprompt ;
//    showtips = initshowtips ;
		defaultplayfkiss = initdefaultplayfkiss ;
		acceptcnferrors = initacceptcnferrors ;
		pagesarescenes = initpagesarescenes ;
		multipleevents = initmultipleevents ;
		timer = inittimer ;
		event = initevent ;
		animate = initanimate ;
      editenable = initeditenable ;
      securityenable = initsecurityenable ;
      initedit = initinitedit ;
		debugmouse = initdebugmouse ;
		debugcontrol = initdebugcontrol ;
		debugload = initdebugload ;
		debugedit = initdebugedit ;
		debugimage = initdebugimage ;
		debugevent = initdebugevent ;
		debugaction = initdebugaction ;
		debugvariable = initdebugvariable ;
		debugfkiss = initdebugfkiss ;
		debugsound = initdebugsound ;
		debugmovie = initdebugmovie ;
		debugmedia = initdebugmedia ;
		debugsearch = initdebugsearch ;
		debugdisabled = initdebugdisabled ;
		debugcomponent = initdebugcomponent ;
		javasound = initjavasound ;
//    backup = initbackup ;
//    savesource = initsavesource ;
//    loadclose = initloadclose ;
		eventqueues = initeventqueues ;
      timerperiod = inittimerperiod ;
      gifperiod = initgifperiod ;
      sceneperiod = initsceneperiod ;
      audioperiod = initaudioperiod ;
      stickyflex = initstickyflex ;
      maxflex = initmaxflex ;
      maxlock = initmaxlock ;
      maxpageset = initmaxpageset ;
      maxcolorset = initmaxcolorset ;
      maxlrufiles = initmaxlrufiles ;
      longduration = initlongduration ;
      mediavolume = initmediavolume ;
		cacheaudio = initcacheaudio ;
		cachevideo = initcachevideo ;
		cacheimage = initcacheimage ;
//		scaletofit = initscaletofit ;
//		sizetofit = initsizetofit ;
		retainwindowsize = initretainwindowsize ;
		maximizewindow = initmaximizewindow ;
		randomsplash = initrandomsplash ;
		showborder = initshowborder ;
		inittoolbar = initinittoolbar ;
//		tbtools = inittbtools ;
//		tbcolors = inittbcolors ;
//		tbpages = inittbpages ;
//		tbedits = inittbedits ;
//		tbcompat = inittbcompat ;
		initstatusbar = initinitstatusbar ;
		initmenubar = initinitmenubar ;
		importcel = initimportcel ;
		exportcel = initexportcel ;
		componentcel = initcomponentcel ;
		importcomponent = initimportcomponent ;
		constrainmoves = initconstrainmoves ;
		constrainvisible = initconstrainvisible ;
		constrainfkiss = initconstrainfkiss ;
		autoscroll = initautoscroll ;
		dragmove = initdragmove ;
		releasemove = initreleasemove ;
		dropfixdrop = initdropfixdrop ;
		catchfixdrop = initcatchfixdrop ;
		visibleunfix = initvisibleunfix ;
		earlyfix = initearlyfix ;
		detachrestricted = initdetachrestricted ;
		detachmove = initdetachmove ;
		detachfix = initdetachfix ;
		invertghost = initinvertghost ;
		transparentgroup = inittransparentgroup ;
		mapcollide = initmapcollide ;
		movexycollide = initmovexycollide ;
		retainkey = initretainkey ;
		strictsyntax = initstrictsyntax ;
		showundefs = initshowundefs ;
		autoendif = initautoendif ;
		immediatecollide = initimmediatecollide ;
		immediateunfix = initimmediateunfix ;
		immediateevent = initimmediateevent ;
		automedialoop = initautomedialoop ;
		autofullscreen = initautofullscreen ;
		suspendmedia = initsuspendmedia ;
		longsoundmedia = initlongsoundmedia ;
		adjustmediavolume = initadjustmediavolume ;
		stopmusic = initstopmusic ;
		soundsingle = initsoundsingle ;
		mediaminimize = initmediaminimize ;
		mediacenter = initmediacenter ;
		mediamusicresume = initmediamusicresume ;
		keepaspect = initkeepaspect ;
		keycase = initkeycase ;
		variablecase = initvariablecase ;
		expandevents = initexpandevents ;
		enableshell = initenableshell ;
		xyoffsets = initxyoffsets ;
		absolutewindow = initabsolutewindow ;
		panelvisible = initpanelvisible ;
		mouseinoutbox = initmouseinoutbox ;
		contextmap = initcontextmap ;
		mapcount = initmapcount ;
		allambiguous = initallambiguous ;
		alteditdrag = initalteditdrag ;
		shifteditdrag = initshifteditdrag ;
		importrelative = initimportrelative ;
		writeceloffset = initwriteceloffset ;
//		writecomment = initwritecomment ;
		commentcol = initcommentcol ;
		indentspace = initindentspace ;
//		showbreakpointend = initshowbreakpointend ;
//		showstepintoend = initshowstepintoend ;
//		eventpause = initeventpause ;
//		actionpause = initactionpause ;
//		disableall = initdisableall ;
      
      // Reset specified compatibility options.
      
      if (initplayfkiss) setPlayFKissCompatibility(true) ;
      else if (initdirectkiss) setDirectKissCompatibility(true) ;
      else if (initgnomekiss) setGnomeKissCompatibility(true) ;
      else if (initkissld) setKissLDCompatibility(true) ;
      else if (compatapply) clearCompatibilityOptions() ;
      if (compatapply) setInitOptions() ;
      compatapply = false ;
      setControls() ;
   }

   // Set initial options.   These are set for every option change if we do
   // not have an active configuration.

   static void setInitOptions()
   {
      initsound = sound ;
	   initmovie = movie ;
	   inittimer = timer ;
	   initevent = event ;
	   initanimate = animate ;
      initeditenable = editenable ;
      initsecurityenable = securityenable ;
      initinitedit = initedit ;
	   initdebugmouse = debugmouse ;
	   initdebugcontrol = debugcontrol ;
	   initdebugload = debugload ;
	   initdebugedit = debugedit ;
	   initdebugimage = debugimage ;
	   initdebugevent = debugevent ;
	   initdebugaction = debugaction ;
	   initdebugvariable = debugvariable ;
	   initdebugfkiss = debugfkiss ;
	   initdebugsound = debugsound ;
	   initdebugmovie = debugmovie ;
	   initdebugmedia = debugmedia ;
	   initdebugsearch = debugsearch ;
	   initdebugdisabled = debugdisabled ;
	   initdebugcomponent = debugcomponent ;
	   initjavasound = javasound ;
      initsystemlf = systemlf ;
      initjavalf = javalf ;
      initbackup = backup ;
      initsavesource = savesource ;
      initshowdlprompt = showdlprompt ;
      initshowtips = showtips ;
      initloadclose = loadclose ;
	   initeventqueues = new String(eventqueues) ;
      inittimerperiod = new String(timerperiod) ;
      initgifperiod = new String(gifperiod) ;
      initsceneperiod = new String(sceneperiod) ;
      initaudioperiod = new String(audioperiod) ;
      initstickyflex = new String(stickyflex) ;
      initmaxflex = new String(maxflex) ;
      initmaxlock = new String(maxlock) ;
      initmaxpageset = new String(maxpageset) ;
      initmaxcolorset = new String(maxcolorset) ;
      initmaxlrufiles = new String(maxlrufiles) ;
      initlongduration = new String(longduration) ;
      initmediavolume = new String(mediavolume) ;
	   initcacheaudio = cacheaudio ;
	   initcachevideo = cachevideo ;
	   initcacheimage = cacheimage ;
	   initscaletofit = scaletofit ;
	   initsizetofit = sizetofit ;
	   initretainwindowsize = retainwindowsize ;
	   initmaximizewindow = maximizewindow ;
	   initrandomsplash = randomsplash ;
	   initshowborder = showborder ;
	   initinittoolbar = inittoolbar ;
	   inittbtools = tbtools ;
	   inittbcolors = tbcolors ;
	   inittbpages = tbpages ;
	   inittbedits = tbedits ;
	   inittbcompat = tbcompat ;
	   initinitstatusbar = initstatusbar ;
	   initinitmenubar = initmenubar ;
	   initimportcel = importcel ;
	   initexportcel = exportcel ;
	   initcomponentcel = componentcel ;
	   initimportcomponent = importcomponent ;
	   initconstrainmoves = constrainmoves ;
	   initconstrainvisible = constrainvisible ;
	   initconstrainfkiss = constrainfkiss ;
	   initautoscroll = autoscroll ;
	   initdragmove = dragmove ;
	   initreleasemove = releasemove ;
	   initdropfixdrop = dropfixdrop ;
	   initcatchfixdrop = catchfixdrop ;
	   initvisibleunfix = visibleunfix ;
	   initearlyfix = earlyfix ;
	   initdetachrestricted = detachrestricted ;
	   initdetachmove = detachmove ;
	   initdetachfix = detachfix ;
	   initinvertghost = invertghost ;
	   inittransparentgroup = transparentgroup ;
	   initmapcollide = mapcollide ;
	   initmovexycollide = movexycollide ;
	   initretainkey = retainkey ;
	   initstrictsyntax = strictsyntax ;
	   initshowundefs = showundefs ;
	   initautoendif = autoendif ;
	   initimmediatecollide = immediatecollide ;
	   initimmediateunfix = immediateunfix ;
	   initimmediateevent = immediateevent ;
	   initautomedialoop = automedialoop ;
	   initautofullscreen = autofullscreen ;
	   initsuspendmedia = suspendmedia ;
	   initlongsoundmedia = longsoundmedia ;
	   initadjustmediavolume = adjustmediavolume ;
	   initstopmusic = stopmusic ;
	   initsoundsingle = soundsingle ;
	   initmediaminimize = mediaminimize ;
	   initmediacenter = mediacenter ;
	   initmediamusicresume = mediamusicresume ;
	   initkeepaspect = keepaspect ;
	   initkeycase = keycase ;
	   initvariablecase = variablecase ;
	   initexpandevents = expandevents ;
	   initenableshell = enableshell ;
	   initxyoffsets = xyoffsets ;
	   initabsolutewindow = absolutewindow ;
	   initpanelvisible = panelvisible ;
	   initmouseinoutbox = mouseinoutbox ;
	   initcontextmap = contextmap ;
	   initmapcount = mapcount ;
	   initallambiguous = allambiguous ;
	   initalteditdrag = alteditdrag ;
	   initshifteditdrag = shifteditdrag ;
	   initimportrelative = importrelative ;
	   initwriteceloffset = writeceloffset ;
	   initshowbreakpointend = showbreakpointend ;
	   initshowstepintoend = showstepintoend ;
	   initwritecomment = writecomment ;
	   initcommentcol = new String(commentcol) ;
	   initindentspace = new String(indentspace) ;
	   initeventpause = eventpause ;
	   initactionpause = actionpause ;
	   initdisableall = disableall ;
      initlanguage = language ;
      initencoding = encoding ;
      initexporttype = exporttype ;
      initbrowser = browser ;
      initapplemac = applemac ;
      initlinux = linux ;
      initplayfkiss = playfkiss ;
      initdirectkiss = directkiss ;
      initgnomekiss = gnomekiss ;
      initkissld = kissld ;
      tempeditenable = initedit ;
      initdefaultplayfkiss = defaultplayfkiss ;
      initacceptcnferrors = acceptcnferrors ;
      initpagesarescenes = pagesarescenes ;
      initmultipleevents = multipleevents ;
	   initsplashdir = new String(splashdir) ;
   }
   
   // Set initial factory option values.
   
   static void setFactoryOptions()
   {
      sound = true ;
      movie = true ;
      event = true ;
      animate = true ;
      timer = true ;
      editenable = true ;
      securityenable = false ;
      initedit = true ;
      debugmouse = false ;
      debugcontrol = false ;
      debugload = false ;
      debugedit = false ;
      debugimage = false ;
      debugevent = false ;
      debugaction = false ;
      debugvariable = false ;
      debugfkiss = false ;
      debugsound = false ;
      debugmovie = false ;
      debugmedia = false ;
      debugsearch = false ;
      debugdisabled = false ;
      debugcomponent = false ;
      javasound = true ;
      systemlf = false ;
      javalf = true ;
      backup = false ;
      savesource = false ;
      showdlprompt = true ;
      showtips = true ;
      loadclose = true ;
      cacheaudio = true ;
      cachevideo = false ;
      cacheimage = true ;
      suspendmedia = true ;
      longsoundmedia = false ;
      adjustmediavolume = false ;
      stopmusic = false ;
      soundsingle = false;
      scaletofit = false ;
      sizetofit = false ;
      retainwindowsize = true ;
      maximizewindow = false ;
      randomsplash = true ;
      showborder = true ;
      inittoolbar = true ;
      tbtools = true ;
      tbcolors = true ;
      tbpages = true ;
      tbedits = true ;
      tbcompat = true ;
      initstatusbar = true ;
      initmenubar = true ;
      importcel = false ;
      exportcel = false ;
      componentcel = false ;
      importcomponent = false ;
      constrainmoves = false ;
      constrainvisible = true ;
      constrainfkiss = true ;
      autoscroll = true ;
      dragmove = false ;
      releasemove = true ;
      dropfixdrop = true ;
      catchfixdrop = true ;
      visibleunfix = true ;
      earlyfix = false ;
      detachrestricted = true ;
      detachmove = true ;
      detachfix = true ;
      invertghost = false ;
      transparentgroup = true ;
      mapcollide = false ;
      movexycollide = false ;
      automedialoop = false ;
      autofullscreen = false ;
      mediaminimize = false ;
      mediacenter = true ;
      mediamusicresume = false ;
      keepaspect = true ;
      keycase = false ;
      variablecase = false ;
      expandevents = false ;
      enableshell = false ;
      xyoffsets = false ;
      absolutewindow = false ;
      panelvisible = false ;
      mouseinoutbox = false ;
      contextmap = true ;
      mapcount = true ;
      allambiguous = false ;
      alteditdrag = true ;
      shifteditdrag = false ;
      importrelative = false ;
      writeceloffset = true ;
      showbreakpointend = true ;
      showstepintoend = true ;
      writecomment = true ;
      eventpause = true ;
      actionpause = false ;
      disableall = false ;
      retainkey = true ;
      strictsyntax = true ;
      showundefs = false ;
      autoendif = false ;
      immediatecollide = true ;
      immediateunfix = false ;
      immediateevent = false ;
      playfkiss = false ;
      directkiss = false ;
      gnomekiss = false ;
      kissld = false ;
      defaultplayfkiss = false ;
      acceptcnferrors = false ;
      pagesarescenes = false ;
      multipleevents = true ;
      compatapply = false ;
      eventqueues = "1" ;
      timerperiod = "10" ;
      gifperiod = "100" ;
      sceneperiod = "1000" ;
      audioperiod = "30000" ;
      stickyflex = "10" ;
      maxflex = "100" ;
      maxlock = "32767" ;
      maxpageset = "10" ;
      maxcolorset = "10" ;
      maxlrufiles = "4" ;
      longduration = "10" ;
      mediavolume = "0.5" ;
      jpegquality = "0.9" ;
      undolimit = "100" ;
      commentcol = "60" ;
      indentspace = "1" ;
      language = "English" ;
      encoding = "Cp1252" ;
      exporttype = "CEL" ;
      browser = "" ;
      kissweb = Kisekae.getKissWeb() ;
      userdir = Kisekae.getBaseDir() ;
      website = Kisekae.getWebSite() ; ;
      splashdir = Kisekae.getFactorySplashDir() ; 
      splashsetnumber = new Integer(0) ;
      icondir = Kisekae.getIconDir() ; 
      iconnumber = new Integer(0) ;
      onlinehelp = "HelpFiles/product/" ;
      splashrestart = true ;
      
      // Clear lru menu files.
      
      lrufiles = null ;
      MainFrame mf = Kisekae.getMainFrame() ;
      MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
      if (menu != null) menu.clearLruFiles() ;
   }
   
   // Check options for any change.   
   
   boolean changedOptions()
   {
//    if (initplayfkiss != PlayFKissBtn.isSelected()) return true ;
//    if (initdirectkiss != DirectKissBtn.isSelected()) return true ;
//    if (initgnomekiss != GnomeKissBtn.isSelected()) return true ;
//    if (initkissld != KissLDBtn.isSelected()) return true ;
//	   if (inittimer != TimerOption.isSelected()) return true ;
//	   if (initevent != EventOption.isSelected()) return true ;
//	   if (initanimate != AnimateOption.isSelected()) return true ;
//    if (initinitedit != InitEdit.isSelected()) return true ;
      if (initdefaultplayfkiss != DefaultPlayFKiss.isSelected()) return true ;
      if (initacceptcnferrors != AcceptCnfErrors.isSelected()) return true ;
      if (initpagesarescenes != PagesAreScenes.isSelected()) return true ;
      if (initmultipleevents != MultipleEvents.isSelected()) return true ;
      if (initeditenable != EditEnable.isSelected()) return true ;
      if (initsecurityenable != SecurityEnable.isSelected()) return true ;
	   if (initdebugmouse != MouseDebug.isSelected()) return true ;
	   if (initdebugcontrol != ControlDebug.isSelected()) return true ;
	   if (initdebugload != LoadDebug.isSelected()) return true ;
	   if (initdebugedit != EditDebug.isSelected()) return true ;
	   if (initdebugimage != ImageDebug.isSelected()) return true ;
	   if (initdebugevent != EventDebug.isSelected()) return true ;
	   if (initdebugaction != ActionDebug.isSelected()) return true ;
	   if (initdebugvariable != VariableDebug.isSelected()) return true ;
	   if (initdebugfkiss != FKissDebug.isSelected()) return true ;
	   if (initdebugsound != SoundDebug.isSelected()) return true ;
	   if (initdebugmovie != MovieDebug.isSelected()) return true ;
	   if (initdebugmedia != MediaDebug.isSelected()) return true ;
	   if (initdebugsearch != SearchDebug.isSelected()) return true ;
	   if (initdebugdisabled != DisabledDebug.isSelected()) return true ;
	   if (initdebugcomponent != ComponentDebug.isSelected()) return true ;
	   if (initjavasound != JavaSoundDebug.isSelected()) return true ;
//    if (initbackup != Backup.isSelected()) return true ;
//    if (initsavesource != SaveSource.isSelected()) return true ;
//    if (initshowdlprompt != ShowDLPrompt.isSelected()) return true ;
//    if (initshowtips != ShowTips.isSelected()) return true ;
//    if (initloadclose != LoadClose.isSelected()) return true ;
	   if (!(initeventqueues.equals(EventQueues.getText()))) return true ;
      if (!(inittimerperiod.equals(TimerPeriod.getText()))) return true ;
      if (!(initgifperiod.equals(GifPeriod.getText()))) return true ;
      if (!(initsceneperiod.equals(ScenePeriod.getText()))) return true ;
      if (!(initaudioperiod.equals(AudioPeriod.getText()))) return true ;
      if (!(initstickyflex.equals(StickyFlex.getText()))) return true ;
      if (!(initmaxflex.equals(MaxFlex.getText()))) return true ;
      if (!(initmaxlock.equals(MaxLock.getText()))) return true ;
      if (!(initmaxpageset.equals(MaxPageSet.getText()))) return true ;
      if (!(initmaxcolorset.equals(MaxColorSet.getText()))) return true ;
//    if (!(initmaxlrufiles.equals(MaxLruFiles.getText()))) return true ;
      if (!(initlongduration.equals(LongDuration.getText()))) return true ;
      if (!(initmediavolume.equals(MediaVolume.getText()))) return true ;
//	   if (initcacheaudio != CacheAudio.isSelected()) return true ;
//	   if (initcachevideo != CacheVideo.isSelected()) return true ;
//	   if (initcacheimage != CacheImage.isSelected()) return true ;
//	   if (initscaletofit != ScaleToFit.isSelected()) return true ;
//	   if (initsizetofit != SizeToFit.isSelected()) return true ;
//	   if (initretainwindowsize != RetainWindowSize.isSelected()) return true ;
//	   if (initmaximizewindow != MaximizeWindow.isSelected()) return true ;
//	   if (initrandomsplash != RandomSplash.isSelected()) return true ;
//	   if (initshowborder != ShowBorder.isSelected()) return true ;
//	   if (initinittoolbar != InitToolBar.isSelected()) return true ;
//	   if (inittbtools != TbTools.isSelected()) return true ;
//	   if (inittbcolors != TbColors.isSelected()) return true ;
//	   if (inittbpages != TbPages.isSelected()) return true ;
//	   if (inittbedits != TbEdits.isSelected()) return true ;
//	   if (inittbcompat != TbCompat.isSelected()) return true ;
//	   if (initinitstatusbar != InitStatusBar.isSelected()) return true ;
//	   if (initinitmenubar != InitMenuBar.isSelected()) return true ;
//	   if (initimportcel != ImportCel.isSelected()) return true ;
//	   if (initexportcel != ExportCel.isSelected()) return true ;
//	   if (initcomponentcel != ComponentCel.isSelected()) return true ;
//	   if (initimportcomponent != ImportComponent.isSelected()) return true ;
//	   if (initexpandevents != ExpandEvents.isSelected()) return true ;
	   if (initconstrainmoves != ConstrainMoves.isSelected()) return true ;
	   if (initconstrainvisible != ConstrainVisible.isSelected()) return true ;
	   if (initconstrainfkiss != ConstrainFKiss.isSelected()) return true ;
	   if (initautoscroll != AutoScroll.isSelected()) return true ;
	   if (initdragmove != DragMove.isSelected()) return true ;
	   if (initreleasemove != ReleaseMove.isSelected()) return true ;
	   if (initdropfixdrop != DropFixdrop.isSelected()) return true ;
	   if (initcatchfixdrop != CatchFixdrop.isSelected()) return true ;
	   if (initvisibleunfix != VisibleUnfix.isSelected()) return true ;
	   if (initearlyfix != EarlyFix.isSelected()) return true ;
	   if (initdetachrestricted != DetachRestricted.isSelected()) return true ;
	   if (initdetachmove != DetachMove.isSelected()) return true ;
	   if (initdetachfix != DetachFix.isSelected()) return true ;
	   if (initinvertghost != InvertGhost.isSelected()) return true ;
	   if (inittransparentgroup != TransparentGroup.isSelected()) return true ;
	   if (initmapcollide != MapCollide.isSelected()) return true ;
	   if (initmovexycollide != MoveXYCollide.isSelected()) return true ;
	   if (initretainkey != RetainKey.isSelected()) return true ;
	   if (initstrictsyntax != StrictSyntax.isSelected()) return true ;
	   if (initshowundefs != ShowUndefs.isSelected()) return true ;
	   if (initautoendif != AutoEndif.isSelected()) return true ;
	   if (initimmediatecollide != ImmediateCollide.isSelected()) return true ;
	   if (initimmediateunfix != ImmediateUnfix.isSelected()) return true ;
	   if (initimmediateevent != ImmediateEvent.isSelected()) return true ;
	   if (initenableshell != EnableShell.isSelected()) return true ;
	   if (initxyoffsets != XYOffsets.isSelected()) return true ;
	   if (initabsolutewindow != AbsoluteWindow.isSelected()) return true ;
	   if (initpanelvisible != PanelVisible.isSelected()) return true ;
	   if (initmouseinoutbox != MouseInOutBox.isSelected()) return true ;
	   if (initcontextmap != ContextMap.isSelected()) return true ;
	   if (initmapcount != MapCount.isSelected()) return true ;
	   if (initallambiguous != AllAmbiguous.isSelected()) return true ;
	   if (initkeepaspect != KeepAspect.isSelected()) return true ;
	   if (initkeycase != KeyCase.isSelected()) return true ;
	   if (initvariablecase != VariableCase.isSelected()) return true ;
//	   if (initautomedialoop != AutoMediaLoop.isSelected()) return true ;
//	   if (initautofullscreen != AutoFullScreen.isSelected()) return true ;
//	   if (initsuspendmedia != SuspendMedia.isSelected()) return true ;
//	   if (initlongsoundmedia != LongSoundMedia.isSelected()) return true ;
//	   if (initadjustmediavolume != AdjustMediaVolume.isSelected()) return true ;
//	   if (initstopmusic != StopMusic.isSelected()) return true ;
//	   if (initsoundsingle != SoundSingle.isSelected()) return true ;
//	   if (initmediaminimize != MediaMinimize.isSelected()) return true ;
//	   if (initmediacenter != MediaCenter.isSelected()) return true ;
//	   if (initmediamusicresume != MediaMusicResume.isSelected()) return true ;
//	   if (initalteditdrag != AltEditDrag.isSelected()) return true ;
//	   if (initshifteditdrag != ShiftEditDrag.isSelected()) return true ;
//	   if (initimportrelative != ImportRelative.isSelected()) return true ;
//	   if (initwriteceloffset != WriteCelOffset.isSelected()) return true ;
//	   if (initshowbreakpointend != ShowBreakPointEnd.isSelected()) return true ;
//	   if (initshowstepintoend != ShowStepIntoEnd.isSelected()) return true ;
//	   if (initwritecomment != WriteComment.isSelected()) return true ;
//	   if (!(initcommentcol.equals(CommentCol.getText()))) return true ;
//	   if (!(initindentspace.equals(IndentSpace.getText()))) return true ;
//	   if (initeventpause != EventPause.isSelected()) return true ;
//	   if (initactionpause != ActionPause.isSelected()) return true ;
//	   if (initdisableall != DisableAll.isSelected()) return true ;
      return false ;
   }
   
   // Check current options for any change.   
   
   boolean changedCurrentOptions()
   {
//    if (playfkiss != PlayFKissBtn.isSelected()) return true ;
//    if (directkiss != DirectKissBtn.isSelected()) return true ;
//    if (gnomekiss != GnomeKissBtn.isSelected()) return true ;
//    if (kissld != KissLDBtn.isSelected()) return true ;
//	   if (timer != TimerOption.isSelected()) return true ;
//	   if (event != EventOption.isSelected()) return true ;
//	   if (animate != AnimateOption.isSelected()) return true ;
//    if (initedit != InitEdit.isSelected()) return true ;
      if (defaultplayfkiss != DefaultPlayFKiss.isSelected()) return true ;
      if (acceptcnferrors != AcceptCnfErrors.isSelected()) return true ;
      if (pagesarescenes != PagesAreScenes.isSelected()) return true ;
      if (multipleevents != MultipleEvents.isSelected()) return true ;
      if (editenable != EditEnable.isSelected()) return true ;
      if (securityenable != SecurityEnable.isSelected()) return true ;
	   if (debugmouse != MouseDebug.isSelected()) return true ;
	   if (debugcontrol != ControlDebug.isSelected()) return true ;
	   if (debugload != LoadDebug.isSelected()) return true ;
	   if (debugedit != EditDebug.isSelected()) return true ;
	   if (debugimage != ImageDebug.isSelected()) return true ;
	   if (debugevent != EventDebug.isSelected()) return true ;
	   if (debugaction != ActionDebug.isSelected()) return true ;
	   if (debugvariable != VariableDebug.isSelected()) return true ;
	   if (debugfkiss != FKissDebug.isSelected()) return true ;
	   if (debugsound != SoundDebug.isSelected()) return true ;
	   if (debugmovie != MovieDebug.isSelected()) return true ;
	   if (debugmedia != MediaDebug.isSelected()) return true ;
	   if (debugsearch != SearchDebug.isSelected()) return true ;
	   if (debugdisabled != DisabledDebug.isSelected()) return true ;
	   if (debugcomponent != ComponentDebug.isSelected()) return true ;
	   if (javasound != JavaSoundDebug.isSelected()) return true ;
//    if (backup != Backup.isSelected()) return true ;
//    if (savesource != SaveSource.isSelected()) return true ;
//    if (showdlprompt != ShowDLPrompt.isSelected()) return true ;
//    if (showtips != ShowTips.isSelected()) return true ;
//    if (loadclose != LoadClose.isSelected()) return true ;
	   if (!(eventqueues.equals(EventQueues.getText()))) return true ;
      if (!(timerperiod.equals(TimerPeriod.getText()))) return true ;
      if (!(gifperiod.equals(GifPeriod.getText()))) return true ;
      if (!(sceneperiod.equals(ScenePeriod.getText()))) return true ;
      if (!(audioperiod.equals(AudioPeriod.getText()))) return true ;
      if (!(stickyflex.equals(StickyFlex.getText()))) return true ;
      if (!(maxflex.equals(MaxFlex.getText()))) return true ;
      if (!(maxlock.equals(MaxLock.getText()))) return true ;
      if (!(maxpageset.equals(MaxPageSet.getText()))) return true ;
      if (!(maxcolorset.equals(MaxColorSet.getText()))) return true ;
//    if (!(maxlrufiles.equals(MaxLruFiles.getText()))) return true ;
      if (!(longduration.equals(LongDuration.getText()))) return true ;
      if (!(mediavolume.equals(MediaVolume.getText()))) return true ;
//	   if (cacheaudio != CacheAudio.isSelected()) return true ;
//	   if (cachevideo != CacheVideo.isSelected()) return true ;
//	   if (cacheimage != CacheImage.isSelected()) return true ;
//	   if (scaletofit != ScaleToFit.isSelected()) return true ;
//	   if (sizetofit != SizeToFit.isSelected()) return true ;
//	   if (retainwindowsize != RetainWindowSize.isSelected()) return true ;
//	   if (maximizewindow != MaximizeWindow.isSelected()) return true ;
//	   if (randomsplash != RandomSplash.isSelected()) return true ;
//	   if (showborder != ShowBorder.isSelected()) return true ;
//	   if (inittoolbar != InitToolBar.isSelected()) return true ;
//	   if (tbtools != TbTools.isSelected()) return true ;
//	   if (tbcolors != TbColors.isSelected()) return true ;
//	   if (tbpages != TbPages.isSelected()) return true ;
//	   if (tbedits != TbEdits.isSelected()) return true ;
//	   if (tbcompat != TbCompat.isSelected()) return true ;
//	   if (initstatusbar != InitStatusBar.isSelected()) return true ;
//	   if (initmenubar != InitMenuBar.isSelected()) return true ;
//	   if (importcel != ImportCel.isSelected()) return true ;
//	   if (exportcel != ExportCel.isSelected()) return true ;
//	   if (componentcel != ComponentCel.isSelected()) return true ;
//	   if (importcomponent != ImportComponent.isSelected()) return true ;
//	   if (expandevents != ExpandEvents.isSelected()) return true ;
	   if (constrainmoves != ConstrainMoves.isSelected()) return true ;
	   if (constrainvisible != ConstrainVisible.isSelected()) return true ;
	   if (constrainfkiss != ConstrainFKiss.isSelected()) return true ;
	   if (autoscroll != AutoScroll.isSelected()) return true ;
	   if (dragmove != DragMove.isSelected()) return true ;
	   if (releasemove != ReleaseMove.isSelected()) return true ;
	   if (dropfixdrop != DropFixdrop.isSelected()) return true ;
	   if (catchfixdrop != CatchFixdrop.isSelected()) return true ;
	   if (visibleunfix != VisibleUnfix.isSelected()) return true ;
	   if (earlyfix != EarlyFix.isSelected()) return true ;
	   if (detachrestricted != DetachRestricted.isSelected()) return true ;
	   if (detachmove != DetachMove.isSelected()) return true ;
	   if (detachfix != DetachFix.isSelected()) return true ;
	   if (invertghost != InvertGhost.isSelected()) return true ;
	   if (transparentgroup != TransparentGroup.isSelected()) return true ;
	   if (mapcollide != MapCollide.isSelected()) return true ;
	   if (movexycollide != MoveXYCollide.isSelected()) return true ;
	   if (retainkey != RetainKey.isSelected()) return true ;
	   if (strictsyntax != StrictSyntax.isSelected()) return true ;
	   if (showundefs != ShowUndefs.isSelected()) return true ;
	   if (autoendif != AutoEndif.isSelected()) return true ;
	   if (immediatecollide != ImmediateCollide.isSelected()) return true ;
	   if (immediateunfix != ImmediateUnfix.isSelected()) return true ;
	   if (immediateevent != ImmediateEvent.isSelected()) return true ;
	   if (enableshell != EnableShell.isSelected()) return true ;
	   if (xyoffsets != XYOffsets.isSelected()) return true ;
	   if (absolutewindow != AbsoluteWindow.isSelected()) return true ;
	   if (panelvisible != PanelVisible.isSelected()) return true ;
	   if (mouseinoutbox != MouseInOutBox.isSelected()) return true ;
	   if (contextmap != ContextMap.isSelected()) return true ;
	   if (mapcount != MapCount.isSelected()) return true ;
	   if (allambiguous != AllAmbiguous.isSelected()) return true ;
	   if (keepaspect != KeepAspect.isSelected()) return true ;
	   if (keycase != KeyCase.isSelected()) return true ;
	   if (variablecase != VariableCase.isSelected()) return true ;
//	   if (automedialoop != AutoMediaLoop.isSelected()) return true ;
//	   if (autofullscreen != AutoFullScreen.isSelected()) return true ;
//	   if (suspendmedia != SuspendMedia.isSelected()) return true ;
//	   if (longsoundmedia != LongSoundMedia.isSelected()) return true ;
//	   if (adjustmediavolume != AdjustMediaVolume.isSelected()) return true ;
//	   if (stopmusic != StopMusic.isSelected()) return true ;
//	   if (soundsingle != SoundSingle.isSelected()) return true ;
//	   if (mediaminimize != MediaMinimize.isSelected()) return true ;
//	   if (mediacenter != MediaCenter.isSelected()) return true ;
//	   if (mediamusicresume != MediaMusicResume.isSelected()) return true ;
//	   if (alteditdrag != AltEditDrag.isSelected()) return true ;
//	   if (shifteditdrag != ShiftEditDrag.isSelected()) return true ;
//	   if (importrelative != ImportRelative.isSelected()) return true ;
//	   if (writeceloffset != WriteCelOffset.isSelected()) return true ;
//	   if (showbreakpointend != ShowBreakPointEnd.isSelected()) return true ;
//	   if (showstepintoend != ShowStepIntoEnd.isSelected()) return true ;
//	   if (writecomment != WriteComment.isSelected()) return true ;
//	   if (!(commentcol.equals(CommentCol.getText()))) return true ;
//	   if (!(indentspace.equals(IndentSpace.getText()))) return true ;
//	   if (eventpause != EventPause.isSelected()) return true ;
//	   if (actionpause != ActionPause.isSelected()) return true ;
//	   if (disableall != DisableAll.isSelected()) return true ;
      return false ;
   }


   // Set options from property values.   

   static void setPropertyOptions(Properties p)
   {
//    sound = toBoolean1(p.getProperty("sound"),sound) ;
//	   movie = toBoolean1(p.getProperty("movie"),movie) ;
//	   timer = toBoolean1(p.getProperty("timer"),timer) ;
//	   event = toBoolean1(p.getProperty("event"),event) ;
//	   animate = toBoolean1(p.getProperty("animate"),animate) ;
//    securityenable = toBoolean1(p.getProperty("securityenable"),securityenable) ;
      initedit = toBoolean1(p.getProperty("initedit"),editenable) ;
      editenable = toBoolean1(p.getProperty("editenable"),editenable) ;
	   javasound = toBoolean1(p.getProperty("javasound"),javasound) ;
      backup =  toBoolean1(p.getProperty("backup"),backup) ;
      savesource =  toBoolean1(p.getProperty("savesource"),savesource) ;
      showdlprompt =  toBoolean1(p.getProperty("showdlprompt"),showdlprompt) ;
      showtips =  toBoolean1(p.getProperty("showtips"),showtips) ;
      loadclose =  toBoolean1(p.getProperty("loadclose"),loadclose) ;
	   eventqueues = toString1(p.getProperty("eventqueues"),eventqueues) ;
      timerperiod = toString1(p.getProperty("timerperiod"),timerperiod) ;
      gifperiod = toString1(p.getProperty("gifperiod"),gifperiod) ;
      sceneperiod = toString1(p.getProperty("sceneperiod"),sceneperiod) ;
      audioperiod = toString1(p.getProperty("audioperiod"),audioperiod) ;
      stickyflex = toString1(p.getProperty("stickyflex"),stickyflex) ;
      maxflex = toString1(p.getProperty("maxflex"),maxflex) ;
      maxlock = toString1(p.getProperty("maxlock"),maxlock) ;
      maxpageset = toString1(p.getProperty("maxpageset"),maxpageset) ;
      maxcolorset = toString1(p.getProperty("maxcolorset"),maxcolorset) ;
      maxlrufiles = toString1(p.getProperty("maxlrufiles"),maxlrufiles) ;
      longduration = toString1(p.getProperty("longduration"),longduration) ;
      mediavolume = toString1(p.getProperty("mediavolume"),mediavolume) ;
	   cacheaudio = toBoolean1(p.getProperty("cacheaudio"),cacheaudio) ;
	   cachevideo = toBoolean1(p.getProperty("cachevideo"),cachevideo) ;
	   cacheimage = toBoolean1(p.getProperty("cacheimage"),cacheimage) ;
	   scaletofit = toBoolean1(p.getProperty("scaletofit"),scaletofit) ;
	   sizetofit = toBoolean1(p.getProperty("sizetofit"),sizetofit) ;
	   retainwindowsize = toBoolean1(p.getProperty("retainwindowsize"),retainwindowsize) ;
	   maximizewindow = toBoolean1(p.getProperty("maximizewindow"),maximizewindow) ;
	   randomsplash = toBoolean1(p.getProperty("randomsplash"),randomsplash) ;
	   showborder = toBoolean1(p.getProperty("showborder"),showborder) ;
	   inittoolbar = toBoolean1(p.getProperty("inittoolbar"),inittoolbar) ;
	   tbtools = toBoolean1(p.getProperty("tbtools"),tbtools) ;
	   tbcolors = toBoolean1(p.getProperty("tbcolors"),tbcolors) ;
	   tbpages = toBoolean1(p.getProperty("tbpages"),tbpages) ;
	   tbedits = toBoolean1(p.getProperty("tbedits"),tbedits) ;
	   tbcompat = toBoolean1(p.getProperty("tbcompat"),tbcompat) ;
	   initstatusbar = toBoolean1(p.getProperty("initstatusbar"),initstatusbar) ;
	   initmenubar = toBoolean1(p.getProperty("initmenubar"),initmenubar) ;
	   importcel = toBoolean1(p.getProperty("importcel"),importcel) ;
	   exportcel = toBoolean1(p.getProperty("exportcel"),exportcel) ;
	   componentcel = toBoolean1(p.getProperty("componentcel"),componentcel) ;
	   importcomponent = toBoolean1(p.getProperty("importcomponent"),importcomponent) ;
	   constrainmoves = toBoolean1(p.getProperty("constrainmoves"),constrainmoves) ;
	   constrainvisible = toBoolean1(p.getProperty("constrainvisible"),constrainvisible) ;
	   constrainfkiss = toBoolean1(p.getProperty("constrainfkiss"),constrainfkiss) ;
	   autoscroll = toBoolean1(p.getProperty("autoscroll"),autoscroll) ;
	   dragmove = toBoolean1(p.getProperty("dragmove"),dragmove) ;
	   releasemove = toBoolean1(p.getProperty("releasemove"),releasemove) ;
	   dropfixdrop = toBoolean1(p.getProperty("dropfixdrop"),dropfixdrop) ;
	   catchfixdrop = toBoolean1(p.getProperty("catchfixdrop"),catchfixdrop) ;
	   visibleunfix = toBoolean1(p.getProperty("visibleunfix"),visibleunfix) ;
	   earlyfix = toBoolean1(p.getProperty("earlyfix"),earlyfix) ;
	   detachrestricted = toBoolean1(p.getProperty("detachrestricted"),detachrestricted) ;
	   detachmove = toBoolean1(p.getProperty("detachmove"),detachmove) ;
	   detachfix = toBoolean1(p.getProperty("detachfix"),detachfix) ;
	   invertghost = toBoolean1(p.getProperty("invertghost"),invertghost) ;
	   transparentgroup = toBoolean1(p.getProperty("transparentgroup"),transparentgroup) ;
	   mapcollide = toBoolean1(p.getProperty("mapcollide"),mapcollide) ;
	   movexycollide = toBoolean1(p.getProperty("movexycollide"),movexycollide) ;
	   retainkey = toBoolean1(p.getProperty("retainkey"),retainkey) ;
	   strictsyntax = toBoolean1(p.getProperty("strictsyntax"),strictsyntax) ;
	   showundefs = toBoolean1(p.getProperty("showundefs"),showundefs) ;
	   autoendif = toBoolean1(p.getProperty("autoendif"),autoendif) ;
	   immediatecollide = toBoolean1(p.getProperty("immediatecollide"),immediatecollide) ;
	   immediateunfix = toBoolean1(p.getProperty("immediateunfix"),immediateunfix) ;
	   immediateevent = toBoolean1(p.getProperty("immediateevent"),immediateevent) ;
	   automedialoop = toBoolean1(p.getProperty("automedialoop"),automedialoop) ;
	   autofullscreen = toBoolean1(p.getProperty("autofullscreen"),autofullscreen) ;
	   suspendmedia = toBoolean1(p.getProperty("suspendmedia"),suspendmedia) ;
	   longsoundmedia = toBoolean1(p.getProperty("longsoundmedia"),longsoundmedia) ;
	   adjustmediavolume = toBoolean1(p.getProperty("adjustmediavolume"),adjustmediavolume) ;
	   stopmusic = toBoolean1(p.getProperty("stopmusic"),stopmusic) ;
	   soundsingle = toBoolean1(p.getProperty("soundsingle"),soundsingle) ;
	   mediaminimize = toBoolean1(p.getProperty("mediaminimize"),mediaminimize) ;
	   mediacenter = toBoolean1(p.getProperty("mediacenter"),mediacenter) ;
	   mediamusicresume = toBoolean1(p.getProperty("mediamusicresume"),mediamusicresume) ;
	   keepaspect = toBoolean1(p.getProperty("keepaspect"),keepaspect) ;
	   keycase = toBoolean1(p.getProperty("keycase"),keycase) ;
	   variablecase = toBoolean1(p.getProperty("variablecase"),variablecase) ;
	   expandevents = toBoolean1(p.getProperty("expandevents"),expandevents) ;
	   enableshell = toBoolean1(p.getProperty("enableshell"),enableshell) ;
	   xyoffsets = toBoolean1(p.getProperty("xyoffsets"),xyoffsets) ;
	   absolutewindow = toBoolean1(p.getProperty("absolutewindow"),absolutewindow) ;
	   panelvisible = toBoolean1(p.getProperty("panelvisible"),panelvisible) ;
	   mouseinoutbox = toBoolean1(p.getProperty("mouseinoutbox"),mouseinoutbox) ;
	   contextmap = toBoolean1(p.getProperty("contextmap"),contextmap) ;
	   mapcount = toBoolean1(p.getProperty("mapcount"),mapcount) ;
	   allambiguous = toBoolean1(p.getProperty("allambiguous"),allambiguous) ;
	   playfkiss = toBoolean1(p.getProperty("playfkiss"),playfkiss) ;
	   defaultplayfkiss = toBoolean1(p.getProperty("defaultplayfkiss"),defaultplayfkiss) ;
	   acceptcnferrors = toBoolean1(p.getProperty("acceptcnferrors"),acceptcnferrors) ;
	   pagesarescenes = toBoolean1(p.getProperty("pagesarescenes"),pagesarescenes) ;
	   multipleevents = toBoolean1(p.getProperty("multipleevents"),multipleevents) ;
	   directkiss = toBoolean1(p.getProperty("directkiss"),directkiss) ;
	   gnomekiss = toBoolean1(p.getProperty("gnomekiss"),gnomekiss) ;
	   kissld = toBoolean1(p.getProperty("kissld"),kissld) ;
	   alteditdrag = toBoolean1(p.getProperty("alteditdrag"),alteditdrag) ;
	   shifteditdrag = toBoolean1(p.getProperty("shifteditdrag"),shifteditdrag) ;
	   importrelative = toBoolean1(p.getProperty("importrelative"),importrelative) ;
	   writeceloffset = toBoolean1(p.getProperty("writeceloffset"),writeceloffset) ;
	   showbreakpointend = toBoolean1(p.getProperty("showbreakpointend"),showbreakpointend) ;
	   showstepintoend = toBoolean1(p.getProperty("showstepintoend"),showstepintoend) ;
	   writecomment = toBoolean1(p.getProperty("writecomment"),writecomment) ;
	   commentcol = toString1(p.getProperty("commentcol"),commentcol) ;
	   indentspace = toString1(p.getProperty("indentspace"),indentspace) ;
	   eventpause = toBoolean1(p.getProperty("eventpause"),eventpause) ;
	   actionpause = toBoolean1(p.getProperty("actionpause"),actionpause) ;
	   disableall = toBoolean1(p.getProperty("disableall"),disableall) ;
      kissweb = toString1(p.getProperty("kissweb"),kissweb) ;
      userdir = toString1(p.getProperty("userdir"),userdir) ;
      website = toString1(p.getProperty("website"),website) ;
      splashdir = toString1(p.getProperty("splashdir"),splashdir) ;
      icondir = toString1(p.getProperty("icondir"),icondir) ;
	   iconnumber = new Integer(toInteger1(p.getProperty("iconnumber"),iconnumber)) ;
	   splashsetnumber = new Integer(toInteger1(p.getProperty("splashsetnumber"),splashsetnumber)) ;
      setLanguage(toString1(p.getProperty("language"),language)) ;
      setEncoding(toString1(p.getProperty("encoding"),encoding)) ;
      setExportType(toString1(p.getProperty("exporttype"),exporttype)) ;
      setBrowser(toString1(p.getProperty("browser"),browser)) ;
      
      boolean systemlf1 = toBoolean1(p.getProperty("systemlf"),systemlf) ;
      boolean javalf1 =  toBoolean1(p.getProperty("javalf"),javalf) ;
      
      // Set Look and Feel
      
      try
      {
         if (systemlf && javalf1)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()) ;
         if (javalf && systemlf1)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
      }
      catch (Exception e) { }
      systemlf = systemlf1 ;
      javalf = javalf1 ;

      // Watch for uninitialized radio buttons.
      
      if (!alteditdrag && !shifteditdrag) alteditdrag = true ;
      if (!eventpause && !actionpause) eventpause = true ;
 
      // Restore the last used file list.

      for (int i = 0 ; ; i++)
      {
         String s = p.getProperty("lrufile"+i) ;
         if (s == null) break ;
         if (lrufiles == null) lrufiles = new Vector() ;
         lrufiles.add(s) ;
      }
      
      // Set compatibility options.
      
      if (playfkiss) setPlayFKissOptions() ;
      if (directkiss) setDirectKissOptions() ;
      if (gnomekiss) setGnomeKissOptions() ;
      if (kissld) setKissLDOptions() ;
   }

   // Set properties from option values.    

   static void putPropertyOptions(Properties p)
   {
      p.put("sound",toString2(sound)) ;
      p.put("movie",toString2(movie)) ;
      p.put("timer",toString2(timer)) ;
      p.put("event",toString2(event)) ;
      p.put("animate",toString2(animate)) ;
      p.put("editenable",toString2(editenable)) ;
      p.put("securityenable",toString2(securityenable)) ;
      p.put("initedit",toString2(initedit)) ;
      p.put("debugmouse",toString2(debugmouse)) ;
      p.put("debugcontrol",toString2(debugcontrol)) ;
      p.put("debugload",toString2(debugload)) ;
      p.put("debugedit",toString2(debugedit)) ;
      p.put("debugimage",toString2(debugimage)) ;
      p.put("debugevent",toString2(debugevent)) ;
      p.put("debugaction",toString2(debugaction)) ;
      p.put("debugvariable",toString2(debugvariable)) ;
      p.put("debugfkiss",toString2(debugfkiss)) ;
      p.put("debugsound",toString2(debugsound)) ;
      p.put("debugmovie",toString2(debugmovie)) ;
      p.put("debugmedia",toString2(debugmedia)) ;
      p.put("debugsearch",toString2(debugsearch)) ;
      p.put("debugdisabled",toString2(debugdisabled)) ;
      p.put("debugcomponent",toString2(debugcomponent)) ;
      p.put("javasound",toString2(javasound)) ;
      p.put("systemlf",toString2(systemlf)) ;
      p.put("javalf",toString2(javalf)) ;
      p.put("backup",toString2(backup)) ;
      p.put("savesource",toString2(savesource)) ;
      p.put("showdlprompt",toString2(showdlprompt)) ;
      p.put("showtips",toString2(showtips)) ;
      p.put("loadclose",toString2(loadclose)) ;
      p.put("eventqueues",toString2(eventqueues)) ;
      p.put("timerperiod",toString2(timerperiod)) ;
      p.put("gifperiod",toString2(gifperiod)) ;
      p.put("sceneperiod",toString2(sceneperiod)) ;
      p.put("audioperiod",toString2(audioperiod)) ;
      p.put("stickyflex",toString2(stickyflex)) ;
      p.put("maxflex",toString2(maxflex)) ;
      p.put("maxlock",toString2(maxlock)) ;
      p.put("maxpageset",toString2(maxpageset)) ;
      p.put("maxcolorset",toString2(maxcolorset)) ;
      p.put("maxlrufiles",toString2(maxlrufiles)) ;
      p.put("longduration",toString2(longduration)) ;
      p.put("mediavolume",toString2(mediavolume)) ;
      p.put("cacheaudio",toString2(cacheaudio)) ;
      p.put("cachevideo",toString2(cachevideo)) ;
      p.put("cacheimage",toString2(cacheimage)) ;
      p.put("scaletofit",toString2(scaletofit)) ;
      p.put("sizetofit",toString2(sizetofit)) ;
      p.put("retainwindowsize",toString2(retainwindowsize)) ;
      p.put("maximizewindow",toString2(maximizewindow)) ;
      p.put("randomsplash",toString2(randomsplash)) ;
      p.put("showborder",toString2(showborder)) ;
      p.put("inittoolbar",toString2(inittoolbar)) ;
      p.put("tbtools",toString2(tbtools)) ;
      p.put("tbcolors",toString2(tbcolors)) ;
      p.put("tbpages",toString2(tbpages)) ;
      p.put("tbedits",toString2(tbedits)) ;
      p.put("tbcompat",toString2(tbcompat)) ;
      p.put("initstatusbar",toString2(initstatusbar)) ;
      p.put("initmenubar",toString2(initmenubar)) ;
      p.put("importcel",toString2(importcel)) ;
      p.put("exportcel",toString2(exportcel)) ;
      p.put("componentcel",toString2(componentcel)) ;
      p.put("importcomponent",toString2(importcomponent)) ;
      p.put("constrainmoves",toString2(constrainmoves)) ;
      p.put("constrainvisible",toString2(constrainvisible)) ;
      p.put("constrainfkiss",toString2(constrainfkiss)) ;
      p.put("autoscroll",toString2(autoscroll)) ;
      p.put("dragmove",toString2(dragmove)) ;
      p.put("releasemove",toString2(releasemove)) ;
      p.put("dropfixdrop",toString2(dropfixdrop)) ;
      p.put("catchfixdrop",toString2(catchfixdrop)) ;
      p.put("visibleunfix",toString2(visibleunfix)) ;
      p.put("earlyfix",toString2(earlyfix)) ;
      p.put("detachrestricted",toString2(detachrestricted)) ;
      p.put("detachmove",toString2(detachmove)) ;
      p.put("detachfix",toString2(detachfix)) ;
      p.put("invertghost",toString2(invertghost)) ;
      p.put("transparentgroup",toString2(transparentgroup)) ;
      p.put("mapcollide",toString2(mapcollide)) ;
      p.put("movexycollide",toString2(movexycollide)) ;
      p.put("retainkey",toString2(retainkey)) ;
      p.put("strictsyntax",toString2(strictsyntax)) ;
      p.put("showundefs",toString2(showundefs)) ;
      p.put("autoendif",toString2(autoendif)) ;
      p.put("immediatecollide",toString2(immediatecollide)) ;
      p.put("immediateunfix",toString2(immediateunfix)) ;
      p.put("immediateevent",toString2(immediateevent)) ;
      p.put("automedialoop",toString2(automedialoop)) ;
      p.put("autofullscreen",toString2(autofullscreen)) ;
      p.put("suspendmedia",toString2(suspendmedia)) ;
      p.put("longsoundmedia",toString2(longsoundmedia)) ;
      p.put("adjustmediavolume",toString2(adjustmediavolume)) ;
      p.put("stopmusic",toString2(stopmusic)) ;
      p.put("soundsingle",toString2(soundsingle)) ;
      p.put("mediaminimize",toString2(mediaminimize)) ;
      p.put("mediacenter",toString2(mediacenter)) ;
      p.put("mediamusicresume",toString2(mediamusicresume)) ;
      p.put("keepaspect",toString2(keepaspect)) ;
      p.put("keycase",toString2(keycase)) ;
      p.put("variablecase",toString2(variablecase)) ;
      p.put("expandevents",toString2(expandevents)) ;
      p.put("enableshell",toString2(enableshell)) ;
      p.put("xyoffsets",toString2(xyoffsets)) ;
      p.put("absolutewindow",toString2(absolutewindow)) ;
      p.put("panelvisible",toString2(panelvisible)) ;
      p.put("mouseinoutbox",toString2(mouseinoutbox)) ;
      p.put("contextmap",toString2(contextmap)) ;
      p.put("mapcount",toString2(mapcount)) ;
      p.put("allambiguous",toString2(allambiguous)) ;
      p.put("alteditdrag",toString2(alteditdrag)) ;
      p.put("shifteditdrag",toString2(shifteditdrag)) ;
      p.put("importrelative",toString2(importrelative)) ;
      p.put("writeceloffset",toString2(writeceloffset)) ;
      p.put("showbreakpointend",toString2(showbreakpointend)) ;
      p.put("showstepintoend",toString2(showstepintoend)) ;
      p.put("writecomment",toString2(writecomment)) ;
      p.put("commentcol",toString2(commentcol)) ;
      p.put("indentspace",toString2(indentspace)) ;
      p.put("eventpause",toString2(eventpause)) ;
      p.put("actionpause",toString2(actionpause)) ;
      p.put("disableall",toString2(disableall)) ;
      p.put("language",toString2(language)) ;
      p.put("encoding",toString2(encoding)) ;
      p.put("exporttype",toString2(exporttype)) ;
      p.put("browser",toString2(browser)) ;
      p.put("applemac",toString2(applemac)) ;
      p.put("linux",toString2(linux)) ;
      p.put("playfkiss",toString2(playfkiss)) ;
      p.put("defaultplayfkiss",toString2(defaultplayfkiss)) ;
      p.put("acceptcnferrors",toString2(acceptcnferrors)) ;
      p.put("pagesarescenes",toString2(pagesarescenes)) ;
      p.put("multipleevents",toString2(multipleevents)) ;
      p.put("directkiss",toString2(directkiss)) ;
      p.put("gnomekiss",toString2(gnomekiss)) ;
      p.put("kissld",toString2(kissld)) ;
      p.put("kissweb",toString2(kissweb)) ;
      p.put("userdir",toString2(userdir)) ;
      p.put("website",toString2(website)) ;
      p.put("splashdir",toString2(splashdir)) ;
      p.put("splashsetnumber",(""+getSplashSetNumber())) ;
      p.put("icondir",toString2(icondir)) ;
      p.put("iconnumber",(""+getIconNumber())) ;
     
      if (lrufiles != null)
      {
         for (int i = 0 ; i < lrufiles.size() ; i++)
         {
            if (i >= getMaxLruFiles()) break ;
            String s = (String) lrufiles.elementAt(i) ;
            if (s != null) p.put("lrufile"+i,s) ;
         }
      }
   }

   // Set properties from option values.   

   static void putFinalPropertyOptions(Properties p)
   {
      p.put("kissweb",toString2(kissweb)) ;
      p.put("userdir",toString2(userdir)) ;
      p.put("website",toString2(website)) ;
      p.put("splashdir",toString2(splashdir)) ;
      p.put("icondir",toString2(icondir)) ;
      p.put("iconnumber",(""+getIconNumber())) ;
      p.put("splashsetnumber",(""+getSplashSetNumber())) ;
      
      if (lrufiles != null)
      {
         for (int i = 0 ; i < lrufiles.size() ; i++)
         {
            if (i >= getMaxLruFiles()) break ;
            String s = (String) lrufiles.elementAt(i) ;
            p.put("lrufile"+i,s) ;
         }
      }
   }

      
   // Read initial properties.
      
   public static void loadPropertyOptions()
   {
      if (!initialized)
      {
         try
         {
            FileInputStream in = null ;
            Properties p = new Properties() ;
            try
            {
               in = new FileInputStream(properties) ;
               p.load(in) ;
            }
            catch (FileNotFoundException ex1)
            {
               try
               {
                  URL url = Kisekae.getLoadBase() ;
                  String userdir = url.getPath() ; 
                  File f = new File(userdir,properties) ;
                  in = new FileInputStream(f.getAbsolutePath()) ;
                  p.load(in) ;
               }
               catch (Exception ex2)
               {
                  String userdir = System.getProperty("user.home") ;
                  File f = new File(userdir,properties) ;
                  in = new FileInputStream(f.getAbsolutePath()) ;
                  p.load(in) ;
               }
            }
            in.close() ;
            setPropertyOptions(p) ;
            setInitOptions() ;
         }
         catch (SecurityException e) 
         { 
            System.err.println("OptionsDialog: Security exception accessing properties, " + e.getMessage()) ;
         }
         catch (Exception e)
         {
            if (!(e instanceof FileNotFoundException))
            {
               System.err.println("OptionsDialog: loadPropertyOptions failure, " + e.getMessage()) ;
            }
         }
         finally
         {
            initialized = true ;
         }
      }
   }
   

   // Hide the option dialog.
   
	private void closeWindow()
   {
      newlocale = null ;
      newencoding = null ;
      languageapply = false ;
      languagerestart = false ;
      suspendactions = false ;
      IconList.clearSelection() ;
      SplashList.clearSelection() ;
      setVisible(false) ;
 }
   
   
   // Convert 'true' or 'True' to boolean
   
   private static boolean toBoolean1(String s, boolean b)
   { 
      if (s == null || s.length() == 0) return b ;
      char c = s.charAt(0) ;
      return (c == 't' || c == 'T') ;
   }
   
   
   // Convert property strings.
   
   private static String toString1(String s, Object s1)
   { 
      if (s == null || s.length() == 0) return s1.toString() ;
      return s ;
   }
   
   
   // Convert property integers.
   
   private static String toInteger1(String s, Object s1)
   { 
      if (s == null || s.length() == 0) return s1.toString() ;
      return s ;
   }
   
   
   // Convert boolean to 'true' or 'false' 
   
   private static String toString2(boolean b)
   { return (b) ? "true" : "false" ; }
   
   
   // Convert object to string
   
   private static String toString2(Object o)
   { return (o != null) ? o.toString() : "" ; }
   


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosing(WindowEvent evt) { closeWindow() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
  	public void windowActivated(WindowEvent evt) { setControls() ; }
	public void windowDeactivated(WindowEvent evt) { }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;
		if (suspendactions) return ;

      // A View request from the debug panel opens the log file.

      try
      {
         if (source == LogEdit)
    		{
   			InputStream is = null ;
            ArchiveFile dir = null ;
            File fl = new File(LogFile.getLogFileName()) ;
   			try { is = new FileInputStream(fl) ; }
   			catch (FileNotFoundException e) { is = null ; }
   			if (is != null)
   			{
   				try { LogFile.flushcontents() ; }
   				catch (IOException e) { }
               String directory = fl.getParent() ;
               try { dir = new DirFile(null,directory) ; }
               catch (IOException e) { dir = null ; }
               ArchiveEntry ze = new DirEntry(directory,fl.getName(),dir) ;
   				TextFrame tf = new TextFrame(ze,is,false) ;
   				tf.setVisible(true) ;
   			}
           return ;
   		}

         // A Clear request from the debug panel erases the log file.

         if (source == LogClear)
         {
            String s = Kisekae.getCaptions().getString("OptionsDialogLogEraseText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1+1) + LogFile.getLogFileName() + s.substring(j1) ;
            int i = JOptionPane.showConfirmDialog(this, s,
               Kisekae.getCaptions().getString("OptionsDialogClearLogTitle"),
               JOptionPane.OK_CANCEL_OPTION,
               JOptionPane.QUESTION_MESSAGE) ;
            if (i == JOptionPane.OK_OPTION)
            {
   	      	LogFile.clearcontents() ;
               s = Kisekae.getCaptions().getString("OptionsDialogLogErasedText") ;
               i1 = s.indexOf('[') ;
               j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + LogFile.getLogFileName() + s.substring(j1) ;
   				JOptionPane.showMessageDialog(getParentFrame(), s,
                  Kisekae.getCaptions().getString("OptionsDialogClearLogTitle"),
                  JOptionPane.INFORMATION_MESSAGE) ;
   	         System.out.println(Kisekae.getCopyright()) ;
   			}
   			return ;
   		}

         // A directory button opens a file chooser.

         if (source == PortalBtn)
         {
            JFileChooser fc = new JFileChooser() ;
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY) ;
            if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
            {
               File f = fc.getSelectedFile() ;
               String s = (f != null) ? f.getPath() : null ;
               if (s == null) return ;
               s = "file:/" + s.replace('\\', '/') ;
               setKissWeb(s) ;
               KissWeb.setText(s) ;
               KissWeb.setCaretPosition(0) ;
            }
            return ;
         }

         // A directory button opens a file chooser.

         if (source == UserBtn)
         {
            JFileChooser fc = new JFileChooser() ;
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
            if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
            {
               File f = fc.getSelectedFile() ;
               String s = (f != null) ? f.getPath() : null ;
               s = Kisekae.setBase(s) ;
               UserDirectory.setText(s) ;
               UserDirectory.setCaretPosition(0) ;
            }
            return ;
         }
         
         if (source == HelpBtn)
         {
            JFileChooser fc = new JFileChooser() ;
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
            if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
            {
               File f = fc.getSelectedFile() ;
               String s = (f != null) ? f.getPath() : null ;
               OnlineHelp.setText((f != null) ? f.getPath() : "") ;
               OnlineHelp.setCaretPosition(0) ;
            }
            return ;
         }
         
         if (source == IconBtn)
         {
            JFileChooser fc = new JFileChooser() ;
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
            if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
            {
               File f = fc.getSelectedFile() ;
               String s = (f != null) ? f.getPath() : null ;
               Kisekae.setIconDir(s) ;
               IconDirectory.setText((f != null) ? f.getPath() : "") ;
               IconDirectory.setCaretPosition(0) ;
               iconrestart = true ;
            }
            return ;
         }
         
         if (source == SplashBtn)
         {
            JFileChooser fc = new JFileChooser() ;
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
            if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
            {
               File f = fc.getSelectedFile() ;
               String s = (f != null) ? f.getPath() : null ;
               Kisekae.setSplashDir(s) ;
               SplashDirectory.setText((f != null) ? f.getPath() : "") ;
               SplashDirectory.setCaretPosition(0) ;
               splashrestart = true ;
            }
            return ;
         }
         
         if (source == SplashDirectory)
         {
            splashrestart = true ;            
         }
         
         if (source == ClearLruBtn)
         {
            setLruFiles(null) ;
            MainFrame mf = Kisekae.getMainFrame() ;
            MainMenu menu = (mf != null) ? mf.getMainMenu() : null ;
            if (menu != null) menu.eraseLruFiles() ;
            String s = Kisekae.getCaptions().getString("OptionsDialogLruClearText") ;
  				JOptionPane.showMessageDialog(getParentFrame(), s,
               Kisekae.getCaptions().getString("OptionsDialogLruClearTitle"),
               JOptionPane.INFORMATION_MESSAGE) ;
            return ;
         }
         
         if (source == PlayFKissBtn)
         {
            setPlayFKissCompatibility(PlayFKissBtn.isSelected(),false) ;
            setCompatibilityControls(PlayFKissBtn.isSelected()) ;
         }
         
         if (source == DirectKissBtn)
         {
            setDirectKissCompatibility(DirectKissBtn.isSelected(),false) ;
            setCompatibilityControls(DirectKissBtn.isSelected()) ;
         }
         
         if (source == GnomeKissBtn)
         {
            setGnomeKissCompatibility(GnomeKissBtn.isSelected(),false) ;
            setCompatibilityControls(GnomeKissBtn.isSelected()) ;
         }
         
         if (source == KissLDBtn)
         {
            setKissLDCompatibility(KissLDBtn.isSelected(),false) ;
            setCompatibilityControls(KissLDBtn.isSelected()) ;
         }
 
   		// Actions on duplicated checkboxes must be propagated to their peers.

   		suspendactions = true ;
   		if (source == CacheAudio) CacheAudio1.setSelected(CacheAudio.isSelected()) ;
   		else if (source == CacheAudio1) CacheAudio.setSelected(CacheAudio1.isSelected()) ;
   		else if (source == CacheVideo) CacheVideo1.setSelected(CacheVideo.isSelected()) ;
   		else if (source == CacheVideo1) CacheVideo.setSelected(CacheVideo1.isSelected()) ;
   		suspendactions = false ;

         // Some options immediately update the menu state.

         if (source == TbTools || source == TbColors ||
             source == TbPages || source == TbEdits ||
             source == TbCompat)
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            MainMenu mm = (mf != null) ? mf.getMainMenu() : null ;
            if (mm == null) return ;
            tbtools = TbTools.isSelected() ;
            tbcolors = TbColors.isSelected() ;
            tbpages = TbPages.isSelected() ;
            tbedits = TbEdits.isSelected() ;
            tbcompat = TbCompat.isSelected() ;
            if (source == TbTools) mm.viewtbtools.setSelected(tbtools) ;
            if (source == TbPages) mm.viewtbpages.setSelected(tbpages) ;
            if (source == TbEdits) mm.viewtbedits.setSelected(tbedits) ;
            if (source == TbColors) mm.viewtbcolors.setSelected(tbcolors) ;
            if (source == TbCompat) mm.viewtbcompat.setSelected(tbcompat) ;
            return ;
         }

         // Language selection sets the default locale and file encoding.

         if (source == LanguageBox)
         {
            Object o = LanguageBox.getSelectedItem() ;
            if (o != null && !o.toString().equals(language))
            {
               languagerestart = true ;
               languageapply = true ;
               if ("English".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = Locale.ENGLISH ;
               }
               else if ("Japanese".equals(o.toString()))
               {
                  newencoding = "SJIS" ;
                  newlocale = Locale.JAPANESE ;
               }
               else if ("French".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = Locale.FRENCH ;
               }
               else if ("German".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = Locale.GERMAN ;
               }
               else if ("Dutch".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = new Locale("nl") ;
               }
               else if ("Italian".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = Locale.ITALIAN ;
               }
               else if ("Spanish".equals(o.toString()))
               {
                  newencoding = "Cp1252" ;
                  newlocale = new Locale("es") ;
               }
            }
            else
               newlocale = null ;
         }

         if (source == EncodingBox)
         {
            Object o = EncodingBox.getSelectedItem() ;
            if (o != null && !o.equals(encoding))
            {
               languagerestart = true ;
               languageapply = true ;
               newencoding = o ;
            }
            else
               newencoding = null ;
         }

         // Export selection sets the exporttype.

         if (source == ExportBox)
         {
            Object o = ExportBox.getSelectedItem() ;
            if (o != null) exporttype = o.toString() ;
         }

         // Browser selection sets the exporttype.

         if (source == BrowserBox)
         {
            Object o = BrowserBox.getSelectedItem() ;
            if (o != null) browser = o.toString() ;
            BrowserControl.reset() ;
         }

         // Certain options require a set reload.

         if (source == CacheAudio || source == CacheVideo ||
             source == CacheAudio1 || source == CacheVideo1 ||
             source == TimerOption ||
             source == SizeToFit || source == ScaleToFit ||
             source == JavaSoundDebug || source == AppleMac ||
             source == PlayFKissBtn || source == DirectKissBtn ||
             source == GnomeKissBtn || source == KissLDBtn)
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            Configuration config = (mf != null) ? mf.getConfig() : null ;
            if (config != null)
            {
               String s = Kisekae.getCaptions().getString("OptionsDialogReloadText2") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
      			int i = JOptionPane.showConfirmDialog(this,
                  Kisekae.getCaptions().getString("OptionsDialogReloadText1") + "\n" + s,
                  Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.INFORMATION_MESSAGE) ;
               if (i == JOptionPane.YES_OPTION)
               {
                  config.setOptionsChanged(true) ;
                  config.setRestartable(false) ;
                  setOptions() ;
                  mf.restart() ;
                  closeWindow() ;
                  return ;
               }
            }
   		}

         // Certain options require a set restart.

         if (source == InvertGhost || source == CacheImage ||
             !EventQueues.getText().equals(eventqueues) ||
             !TimerPeriod.getText().equals(timerperiod) ||
             !GifPeriod.getText().equals(gifperiod) ||
             !ScenePeriod.getText().equals(sceneperiod) ||
             !AudioPeriod.getText().equals(audioperiod) ||
             languagerestart)
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            Configuration config = (mf != null) ? mf.getConfig() : null ;
            if (config != null)
            {
               String s = Kisekae.getCaptions().getString("OptionsDialogRestartText2") ;
                  int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
               int i = JOptionPane.showConfirmDialog(this,
                  Kisekae.getCaptions().getString("OptionsDialogRestartText1") + "\n" + s,
                  Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.INFORMATION_MESSAGE) ;
               if (i == JOptionPane.YES_OPTION)
               {
                  if (languagerestart)
                  {
                     languagerestart = false ;
                     setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     Kisekae.setCurrentLocale(newlocale) ;
                     Kisekae.setLanguageEncoding(newencoding) ;
                     EncodingBox.setSelectedItem(newencoding) ;
                     setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                  }
                  config.setOptionsChanged(true) ;
                  config.setRestartable(false) ;
                  setOptions() ;
                  mf.restart() ;
                  closeWindow() ;
                  return ;
               }
               else if (languagerestart)
               {
                  languagerestart = false ;
                  EncodingBox.setSelectedItem(encoding) ;
//                LanguageBox.setSelectedItem(language);
                  ComboBoxModel m = LanguageBox.getModel() ;
                  if (m != null)
                  {
                     for (int i2 = 0 ; i2 < m.getSize() ; i2++)
                     {
                        Object o = m.getElementAt(i2) ;
                        if (o == null) continue ;
                        if (o.toString().equals(language)) 
                        {
                           m.setSelectedItem(o) ;
                           break ;
                        }
                     }
                  }
               }
            }
            else
            {
               if (languagerestart)
               {
                  languagerestart = false ;
                  EncodingBox.setSelectedItem(newencoding) ;
               }
            }
   		}
         
   		// An OK closes the dialog and retains the current option settings.

         if (source == OK)
   		{
         	boolean b = apply(this) ;
   			if (b) closeWindow() ;
   			return ;
   		}

   		// An Apply sets the options but does not close the dialog.

         if (source == APPLY)
   		{
         	apply(this) ;
   			return ;
         }

   		// A Reset sets the factory options but does not close the dialog.

         if (source == RESET)
   		{
            int i = JOptionPane.showConfirmDialog(this,
               Kisekae.getCaptions().getString("OptionsDialogResetText") + "\n",
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.YES_NO_OPTION,
               JOptionPane.INFORMATION_MESSAGE) ;
            if (i == JOptionPane.YES_OPTION)
            {
               IconList.clearSelection() ;
               SplashList.clearSelection() ;
               setFactoryOptions() ;
               setInitOptions() ;
               setControls() ;
               CANCEL.setEnabled(false);
         	   apply(this) ;
            }
   			return ;
         }

         // A Cancel closes the dialog and restores the option controls.

         if (source == CANCEL)
         {
            setControls() ;
            closeWindow() ;
            return ;
         }

         // A Save writes the properties.

         if (source == SAVE)
         {
            apply() ;
            if (savePropertiesOptions())
            {
               JOptionPane.showMessageDialog(getParentFrame(),
                  Kisekae.getCaptions().getString("OptionsDialogSavePropertiesText1")
                  + ((properties != null) ? ("\n" + properties) : ""),
                  Kisekae.getCaptions().getString("OptionsDialogInfoTitle"),
                  JOptionPane.INFORMATION_MESSAGE) ;
            }
            return ;
         }
   	}

      // Catch security exceptions.

      catch (SecurityException e)
      {
         System.out.println("OptionDialog security exception, " + e.toString()) ;
         JOptionPane.showMessageDialog(getParentFrame(),
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
            Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Catch property save exceptions.

      catch (IOException e)
      {
         System.out.println("OptionDialog exception, " + e.toString()) ;
         JOptionPane.showMessageDialog(getParentFrame(),
            Kisekae.getCaptions().getString("FileWriteError") + "\n" + e.toString(),
            Kisekae.getCaptions().getString("FileSaveException"),
            JOptionPane.ERROR_MESSAGE) ;
      }
   }
   
   
   // Watch for Icon image selections.
   
   public void valueChanged(ListSelectionEvent evt)
   {
		Object source = evt.getSource() ;
      if (source instanceof JList)
         if (((JList) source).getSelectedValue() == null) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;

      if (source == IconList)
      {
         if (config == null)
         {
            iconrestart = true ;
            return ;
         }
         
         String s = Kisekae.getCaptions().getString("OptionsDialogRestartText2") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
         int i = JOptionPane.showConfirmDialog(this,
            Kisekae.getCaptions().getString("OptionsDialogRestartText1") + "\n" + s,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE) ;
         
         if (i == JOptionPane.YES_OPTION)
         {
            setOptions() ;
            setIconImage() ;
            closeWindow() ;
            Kisekae.restart() ;
         }
         else
         {
            ((JList) source).clearSelection() ;
         }
         return ;
      }

      if (source == SplashList)
      {
         int n = SplashList.getSelectedIndex() ;
         if (n < 0) return ;
         n = n + 1 ;
         splashsetnumber = new Integer(n) ;

         // Establish Splash option.  Format is path/setname.ext
         
         String name = getSplashSetName(n-1) ;
         SplashSetName.setText(name) ;
         if ("".equals(name)) return ;
         String s2 = getSplashDir() ;      
         int m = s2.lastIndexOf('.') ;
         String extension = (m > 0) ? s2.substring(m) : ".jpg" ;
         n = s2.lastIndexOf('/') ;
         if (n < 0) n = s2.lastIndexOf('\\') ;
         if (n < 0) n = s2.length() ;
         String directory = s2.substring(0,n) ;  
         s2 = directory + "/" + name + extension ;
         
         // Confirm the change
                          
         String s = Kisekae.getCaptions().getString("OptionsDialogRestartText2") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1 && config != null)
            s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
         if (i1 >= 0 && j1 > i1 && config == null)
            s = s.substring(0,i1) + s.substring(j1+1) ;
         int i = JOptionPane.showConfirmDialog(this,
            Kisekae.getCaptions().getString("OptionsDialogRestartText1") + "\n" + s,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE) ;
         
         if (i == JOptionPane.YES_OPTION)
         {
            setSplashDir(s2) ;
            splashrestart = true ;
            closeWindow() ;
            Kisekae.restart() ;
         }
         else
         {
            ((JList) source).clearSelection() ;
         }
         return ;
      }
   }
   
   
   // Watch for tab pane changes.  We left justify out textfields.
   
   public void stateChanged(ChangeEvent evt)
   {
 		Object source = evt.getSource() ;
      
      if (source == jTabbedPane1)
      {
   		KissWeb.setCaretPosition(0);
         UserDirectory.setCaretPosition(0);
         WebSite.setCaretPosition(0);
         OnlineHelp.setCaretPosition(0);
         SplashDirectory.setCaretPosition(0);
         IconDirectory.setCaretPosition(0);
      }
      
      if (source == UseDefaultWS)
      {
         SaveArchive.setEnabled(!UseDefaultWS.isSelected());
   		SaveAsZip.setEnabled(!UseDefaultWS.isSelected());
   		SaveImage.setEnabled(!UseDefaultWS.isSelected());
   		ShowLoad.setEnabled(!UseDefaultWS.isSelected());
      }
    }
   
   
   // Set a new Icon image.  The first time the program runs we can use an
   // animated icon named Icons/iconN.gif. 
   
   private void setIconImage()
   {
      try
      {
         Object o = IconList.getSelectedValue() ;
         if (o instanceof ImageIcon)
         {
            int n = IconList.getSelectedIndex() ;
            if (n < 0) return ;
            n = n + 1 ;
            iconnumber = new Integer(n) ;
            Kisekae.setImageIcon((ImageIcon) o) ;
            
            int waitcount = 0 ;
            String s2 = getIconDir() ;
            int m = s2.indexOf('.') ;
            String s1 = s2.substring(0,m) + n + "-16" + s2.substring(m) ;
            URL iconurl =  Kisekae.getResource(s1) ;
            ImageIcon icon16 = new ImageIcon(iconurl) ;
            while (icon16.getImageLoadStatus() == MediaTracker.LOADING) 
            {
               if (waitcount++ > 3) break ;
               Thread.currentThread().sleep(200) ;
            }
            if (icon16.getImageLoadStatus() == MediaTracker.COMPLETE)
               Kisekae.setImageIcon16(icon16) ;
            
            waitcount = 0 ;
            s1 = s2.substring(0,m) + n + s2.substring(m) ;
            iconurl =  Kisekae.getResource(s1) ;
            ImageIcon icon = new ImageIcon(iconurl) ;
            while (icon.getImageLoadStatus() == MediaTracker.LOADING) 
            {
               if (waitcount++ > 3) break ;
               Thread.currentThread().sleep(200) ;
            }
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE)
               Kisekae.setImageIcon(icon) ;
         }
      }
      catch (Exception e) { }
   }
   

   // A function to apply the current option settings.

   public boolean apply() { return apply(null) ; }
   public boolean apply(Object o)
   {
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      ToolBar toolbar = (mf != null) ? mf.getToolBar() : null ;
      KissMenu menu = (mf != null) ? mf.getMenu() : null ;
      
      if (o != null && config != null && changedCurrentOptions())
      {
         String s = Kisekae.getCaptions().getString("OptionsDialogChangedText1") ;
         int i1 = s.indexOf('[') ;
         int j1 = s.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s = s.substring(0,i1+1) + config.getName() + s.substring(j1) ;
         String s1 = "\n" ;
         String compat = getCompatibilityMode() ;
         if (compat != null)
         {
            s1 += Kisekae.getCaptions().getString("OptionsDialogChangedText4") ;
            i1 = s1.indexOf('[') ;
            j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + getCompatibilityMode() + s1.substring(j1+1) ;
            s1 += "\n" ;
         }
         int i = JOptionPane.showConfirmDialog(this, s + s1 +
            Kisekae.getCaptions().getString("OptionsDialogChangedText3"),
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE) ;
         if (i != JOptionPane.YES_OPTION) return false ;
      }

      // Apply the language setting.
      
     	try
      {
         if (languageapply)
         {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Kisekae.setCurrentLocale(newlocale) ;
            Kisekae.setLanguageEncoding(newencoding) ;
            language = LanguageBox.getSelectedItem() ;
            if (language != null) language = language.toString() ;
            encoding = EncodingBox.getSelectedItem() ;
            if (config != null)
            {
               initlanguage = language ;
               initencoding = encoding ;
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
      }
      catch (Exception e) 
      { 
         System.err.println("OptionsDialog: apply language error, " + e.getMessage()) ;
      }
         
      // Apply look and feel changes
         
     	try
      {
         if (!applemac)
         {
            if (systemlf && !SystemLF.isSelected())
            {
               Kisekae.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()) ;
               SwingUtilities.updateComponentTreeUI(this);
               SwingUtilities.updateComponentTreeUI(Kisekae.getMainFrame());
            }
            if (javalf && !JavaLF.isSelected())
            {
               Kisekae.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
               SwingUtilities.updateComponentTreeUI(this);
               SwingUtilities.updateComponentTreeUI(Kisekae.getMainFrame());
            }
         }
      }
      catch (Exception e) 
      { 
         System.err.println("OptionsDialog: apply look and feel error, " + e.getMessage()) ;
      }
   
      // Update the toolbar state and the configuration option changed state.

		setOptions() ;
      if (config != null) config.setOptionsChanged(true) ;
      if (toolbar != null) toolbar.updateButtons(config,0,0) ;
      if (toolbar != null) toolbar.update() ;
      if (mf != null) mf.updateMenuOptions() ;
      if (menu != null) menu.update() ;
      
      if (mf != null)
      {
         if (!getInitMenubar())
            mf.setMenu(null);
         else
         {
            if (mf.getMenu() == null)
            {
               if (mf.getPanelMenu() != null) mf.setMenu(mf.getPanelMenu()) ;
               else mf.setMenu(mf.getMainMenu()) ; 
            }                  
         }
      }
      
      // Watch for icon changes.
      
      if (iconrestart)
      {
         setIconImage() ;
         closeWindow() ;
         Kisekae.restart() ;
         return true ;
      }
      
      // Watch for splash pane changes.
      
      if (splashrestart)
      {
         if (mf != null) 
         {
            mf.setNewSplashPane(true) ;
         }
         return true ;
      }
      
      // Lastly, repaint the screen.
      
      PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
      if (pf != null) pf.repaint() ;
      return true ;
   }
   
   
   // A function to clear all Compatibility options.  This restores the 
   // values to the factory defaults.
   
   static void clearCompatibilityOptions()
   {
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      if (config != null) config.setOptionsChanged(true) ;
      compatapply = true ;
      playfkiss = false ;
      directkiss = false ;
      gnomekiss = false ;
      kissld = false ;
      setDragMove(false);
      setReleaseMove(true);
      setDropFixdrop(true);
      setCatchFixdrop(true);
      setVisibleUnfix(true);
      setEarlyFix(false);
      setDetachRestricted(true);
      setDetachMove(true);
      setDetachFix(true);
      setRetainKey(true);
      setStrictSyntax(true);
      setAutoEndif(false);
      setInvertGhost(false);
      setTransparentGroup(true);
      setMapCollide(false);
      setMoveXYCollide(false);
      setImmediateCollide(true);
      setImmediateUnfix(false);
      setImmediateEvent(false);
      setExpandEvents(false);
      setEnableShell(false);
      setXYOffsets(false);
      setAbsoluteWindow(false);
      setPanelVisible(false);
      setMouseInOutBox(false);
      setContextMap(true);
      setMapCount(true);
      setAllAmbiguous(false);
      setConstrainMoves(false) ;
      setDragMove(false) ;
      setConstrainVisible(true) ;
      setImportCel(false) ;
      setExportCel(false) ;
      setImportComponent(false) ;
      setComponentCel(false) ;
      setWriteCelOffset(true) ;
      setSoundSingle(false) ;
      setMultipleEvents(true) ;
      setMaxLock("32767") ;
      setMaxPageSet("10") ;
      setTimerPeriod("10") ;
   }
   
   
   // A function to set compatibility controls. This is valid if we have
   // an options dialog instance on display.
   
   static void setCompatibilityControls(boolean b)
   {
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null)
      {
         OptionsDialog options = mf.getOptionsDialog() ;
         if (options != null && options.isVisible()) 
         {
            if (playfkiss) options.setPlayFKissControls(b) ;
            else if (directkiss) options.setDirectKissControls(b) ;
            else if (gnomekiss) options.setGnomeKissControls(b) ;
            else if (kissld) options.setKissLDControls(b) ;
            else if (!b) options.setPlayFKissControls(false) ;
         }
      }
   }
   
   
   // A function to set PlayFKiss controls.
   
   void setPlayFKissControls(boolean b)
   {
      DirectKissBtn.setSelected(false) ;
      GnomeKissBtn.setSelected(false) ;
      KissLDBtn.setSelected(false) ;
      if (PlayFKissBtn.isSelected() != b) PlayFKissBtn.setSelected(b) ;
      DragMove.setSelected((b) ? false : false);
      ReleaseMove.setSelected((b) ? true : true);
      DropFixdrop.setSelected((b) ? false : true);
      CatchFixdrop.setSelected((b) ? true : true);
      VisibleUnfix.setSelected((b) ? false : true);
      EarlyFix.setSelected((b) ? true : false);
      DetachRestricted.setSelected((b) ? false : true);
      DetachMove.setSelected((b) ? false : true);
      DetachFix.setSelected((b) ? false : true);
      RetainKey.setSelected((b) ? false : true);
      StrictSyntax.setSelected((b) ? true : true);
      AutoEndif.setSelected((b) ? true : false);
      InvertGhost.setSelected((b) ? true : false);
      TransparentGroup.setSelected((b) ? false : false);
      MapCollide.setSelected((b) ? false : false);
      MoveXYCollide.setSelected((b) ? true : false);
      ImmediateCollide.setSelected((b) ? true : true);
      ImmediateUnfix.setSelected((b) ? true : false);
      ImmediateEvent.setSelected((b) ? true : false);
      ExpandEvents.setSelected((b) ? false : false);
      EnableShell.setSelected((b) ? true : false);
      XYOffsets.setSelected((b) ? false : false);
      AbsoluteWindow.setSelected((b) ? false : false);
      PanelVisible.setSelected((b) ? false : false);
      MouseInOutBox.setSelected((b) ? false : false);
      ContextMap.setSelected((b) ? false : true);
      MapCount.setSelected((b) ? false : true);
      AllAmbiguous.setSelected((b) ? false : false);
      ConstrainMoves.setSelected((b) ? true : false) ;
      ConstrainVisible.setSelected((b) ? true : true) ;
      ImportCel.setSelected((b) ? true : false) ;
      ExportCel.setSelected((b) ? false : false) ;
      ImportComponent.setSelected((b) ? false : false) ;
//    ComponentCel.setSelected((b) ? true : false) ;
      WriteCelOffset.setSelected((b) ? true : true) ;
      SoundSingle.setSelected((b) ? true : false) ;
      MultipleEvents.setSelected((b) ? false : true) ;
      TimerPeriod.setText((b) ? "60" : "10") ;
  }
   
   
   // A function to set PlayFKiss options.
   
   static void setPlayFKissOptions()
   {
      compatapply = true ;
      setDragMove(false);
      setReleaseMove(true);
      setDropFixdrop(false);
      setCatchFixdrop(true);
      setVisibleUnfix(false);
      setEarlyFix(true);
      setDetachRestricted(false);
      setDetachMove(false);
      setDetachFix(false);
      setRetainKey(false);
      setStrictSyntax(true);
      setAutoEndif(true);
      setInvertGhost(true);
      setTransparentGroup(false);
      setMapCollide(false);
      setMoveXYCollide(true);
      setImmediateCollide(true);
      setImmediateUnfix(true);
      setImmediateEvent(true);
      setExpandEvents(false);
      setEnableShell(true);
      setXYOffsets(true);
      setAbsoluteWindow(false);
      setPanelVisible(false);
      setMouseInOutBox(false);
      setContextMap(false);
      setMapCount(false);
      setAllAmbiguous(false);
      setConstrainMoves(true) ;
      setDragMove(true) ;
      setConstrainVisible(true) ;
      setImportCel(true) ;
      setExportCel(false) ;
      setImportComponent(false) ;
//    setComponentCel(true) ;
      setWriteCelOffset(true) ;
      setSoundSingle(true) ;
      setMultipleEvents(false) ;
      setMaxLock("32768") ;
      setMaxPageSet("10") ;
      MainFrame mf = Kisekae.getMainFrame() ;
      if (!mf.isRestart()) setTimerPeriod("60") ;
 }
   
   
   // A function to set DirectKiss controls.
   
   void setDirectKissControls(boolean b)
   {
      PlayFKissBtn.setSelected(false) ;
      GnomeKissBtn.setSelected(false) ;
      KissLDBtn.setSelected(false) ;
      if (DirectKissBtn.isSelected() != b) DirectKissBtn.setSelected(b) ;
      DragMove.setSelected((b) ? false : false);
      ReleaseMove.setSelected((b) ? true : true);
      DropFixdrop.setSelected((b) ? false : true);
      CatchFixdrop.setSelected((b) ? true : true);
      VisibleUnfix.setSelected((b) ? false : true);
      EarlyFix.setSelected((b) ? true : false);
      DetachRestricted.setSelected((b) ? false : true);
      DetachMove.setSelected((b) ? false : true);
      DetachFix.setSelected((b) ? false : true);
      RetainKey.setSelected((b) ? false : true);
      StrictSyntax.setSelected((b) ? true : true);
      AutoEndif.setSelected((b) ? true : false);
      InvertGhost.setSelected((b) ? false : false);
      TransparentGroup.setSelected((b) ? false : false);
      MapCollide.setSelected((b) ? false : false);
      MoveXYCollide.setSelected((b) ? true : false);
      ImmediateCollide.setSelected((b) ? true : true);
      ImmediateUnfix.setSelected((b) ? false : false);
      ImmediateEvent.setSelected((b) ? true : false);
      ExpandEvents.setSelected((b) ? false : false);
      EnableShell.setSelected((b) ? false : false);
      XYOffsets.setSelected((b) ? true : false);
      AbsoluteWindow.setSelected((b) ? true : false);
      PanelVisible.setSelected((b) ? true : false);
      MouseInOutBox.setSelected((b) ? true : false);
      ContextMap.setSelected((b) ? false : true);
      MapCount.setSelected((b) ? false : true);
      AllAmbiguous.setSelected((b) ? true : false);
      ConstrainMoves.setSelected((b) ? false : false) ;
      DragMove.setSelected((b) ? false : false) ;
      ConstrainVisible.setSelected((b) ? false : true) ;
      ImportCel.setSelected((b) ? true : false) ;
      ExportCel.setSelected((b) ? false : false) ;
      ImportComponent.setSelected((b) ? false : false) ;
      ComponentCel.setSelected((b) ? true : false) ;
      WriteCelOffset.setSelected((b) ? true : true) ;
      MultipleEvents.setSelected((b) ? false : true) ;
 }
   
   
   // A function to set DirectKiss options.
   
   static void setDirectKissOptions()
   {
      compatapply = true ;
      setDragMove(false);
      setReleaseMove(true);
      setDropFixdrop(false);
      setCatchFixdrop(true);
      setVisibleUnfix(false);
      setEarlyFix(true);
      setDetachRestricted(false);
      setDetachMove(false);
      setDetachFix(false);
      setRetainKey(false);
      setStrictSyntax(true);
      setAutoEndif(true);
      setInvertGhost(false);
      setTransparentGroup(false);
      setMapCollide(false);
      setMoveXYCollide(true);
      setImmediateCollide(true);
      setImmediateUnfix(false);
      setImmediateEvent(true);
      setExpandEvents(false);
      setEnableShell(true);
      setXYOffsets(true);
      setAbsoluteWindow(true);
      setPanelVisible(true);
      setMouseInOutBox(true);
      setContextMap(false);
      setMapCount(false);
      setAllAmbiguous(true);
      setConstrainMoves(true) ;
      setDragMove(false) ;
      setConstrainVisible(true) ;
      setImportCel(true) ;
      setExportCel(false) ;
      setImportComponent(false) ;
      setComponentCel(true) ;
      setWriteCelOffset(true) ;
      setMultipleEvents(false) ;
      setMaxLock("32767") ;
      setMaxPageSet("10") ;
//    setTimerPeriod("60") ;
   }
   
   
   // A function to set GnomeKiss controls.
   
   void setGnomeKissControls(boolean b)
   {
      PlayFKissBtn.setSelected(false) ;
      DirectKissBtn.setSelected(false) ;
      KissLDBtn.setSelected(false) ;
      if (GnomeKissBtn.isSelected() != b) GnomeKissBtn.setSelected(b) ;
      DragMove.setSelected((b) ? false : false);
      ReleaseMove.setSelected((b) ? true : true);
      DropFixdrop.setSelected((b) ? true : true);
      CatchFixdrop.setSelected((b) ? true : true);
      VisibleUnfix.setSelected((b) ? false : true);
      EarlyFix.setSelected((b) ? false : false);
      DetachRestricted.setSelected((b) ? false : true);
      DetachMove.setSelected((b) ? false : true);
      DetachFix.setSelected((b) ? false : true);
      RetainKey.setSelected((b) ? true : true);
      StrictSyntax.setSelected((b) ? true : true);
      AutoEndif.setSelected((b) ? true : false);
      InvertGhost.setSelected((b) ? false : false);
      TransparentGroup.setSelected((b) ? false : false);
      MapCollide.setSelected((b) ? false : false);
      MoveXYCollide.setSelected((b) ? true : false);
      ImmediateCollide.setSelected((b) ? true : true);
      ImmediateUnfix.setSelected((b) ? false : false);
      ImmediateEvent.setSelected((b) ? false : false);
      ExpandEvents.setSelected((b) ? false : false);
      EnableShell.setSelected((b) ? false : false);
      XYOffsets.setSelected((b) ? false : false);
      AbsoluteWindow.setSelected((b) ? false : false);
      PanelVisible.setSelected((b) ? false : false);
      MouseInOutBox.setSelected((b) ? false : false);
      ContextMap.setSelected((b) ? true : true);
      MapCount.setSelected((b) ? false : true);
      AllAmbiguous.setSelected((b) ? false : false);
      ConstrainMoves.setSelected((b) ? true : false) ;
      DragMove.setSelected((b) ? false : false) ;
      ConstrainVisible.setSelected((b) ? false : true) ;
      ImportCel.setSelected((b) ? true : false) ;
      ExportCel.setSelected((b) ? false : false) ;
      ImportComponent.setSelected((b) ? false : false) ;
      ComponentCel.setSelected((b) ? true : false) ;
      WriteCelOffset.setSelected((b) ? true : true) ;
      MultipleEvents.setSelected((b) ? false : true) ;
 }
   
   
   // A function to set GnomeKiss options.
   
   static void setGnomeKissOptions()
   {
      compatapply = true ;
      setDragMove(false);
      setReleaseMove(true);
      setDropFixdrop(true);
      setCatchFixdrop(true);
      setVisibleUnfix(false);
      setEarlyFix(false);
      setDetachRestricted(false);
      setDetachMove(false);
      setDetachFix(false);
      setRetainKey(true);
      setStrictSyntax(true);
      setAutoEndif(true);
      setInvertGhost(false);
      setTransparentGroup(false);
      setMapCollide(false);
      setMoveXYCollide(true);
      setImmediateCollide(true);
      setImmediateUnfix(false);
      setImmediateEvent(true);
      setExpandEvents(false);
      setEnableShell(false);
      setXYOffsets(false);
      setAbsoluteWindow(false);
      setPanelVisible(false);
      setMouseInOutBox(false);
      setContextMap(true);
      setMapCount(false);
      setAllAmbiguous(false);
      setConstrainMoves(true) ;
      setDragMove(false) ;
      setConstrainVisible(false) ;
      setImportCel(true) ;
      setExportCel(false) ;
      setImportComponent(false) ;
      setComponentCel(true) ;
      setWriteCelOffset(true) ;
      setMultipleEvents(false) ;
      setMaxLock("32767") ;
      setMaxPageSet("10") ;
//    setTimerPeriod("60") ;
   }
   
   
   // A function to set KissLD controls.
   
   void setKissLDControls(boolean b)
   {
      PlayFKissBtn.setSelected(false) ;
      DirectKissBtn.setSelected(false) ;
      GnomeKissBtn.setSelected(false) ;
      if (KissLDBtn.isSelected() != b) KissLDBtn.setSelected(b) ;
      DragMove.setSelected((b) ? false : false);
      ReleaseMove.setSelected((b) ? true : true);
      DropFixdrop.setSelected((b) ? true : true);
      CatchFixdrop.setSelected((b) ? true : true);
      VisibleUnfix.setSelected((b) ? true : true);
      EarlyFix.setSelected((b) ? false : false);
      DetachRestricted.setSelected((b) ? false : true);
      DetachMove.setSelected((b) ? false : true);
      DetachFix.setSelected((b) ? false : true);
      RetainKey.setSelected((b) ? true : true);
      StrictSyntax.setSelected((b) ? true : true);
      AutoEndif.setSelected((b) ? true : false);
      InvertGhost.setSelected((b) ? false : false);
      TransparentGroup.setSelected((b) ? false : false);
      MapCollide.setSelected((b) ? false : false);
      MoveXYCollide.setSelected((b) ? true : false);
      ImmediateCollide.setSelected((b) ? true : true);
      ImmediateUnfix.setSelected((b) ? false : false);
      ImmediateEvent.setSelected((b) ? false : false);
      ExpandEvents.setSelected((b) ? false : false);
      EnableShell.setSelected((b) ? false : false);
      XYOffsets.setSelected((b) ? false : false);
      AbsoluteWindow.setSelected((b) ? false : false);
      PanelVisible.setSelected((b) ? false : false);
      MouseInOutBox.setSelected((b) ? false : false);
      ContextMap.setSelected((b) ? true : true);
      MapCount.setSelected((b) ? false : true);
      AllAmbiguous.setSelected((b) ? false : false);
      ConstrainMoves.setSelected((b) ? false : false) ;
      DragMove.setSelected((b) ? false : false) ;
      ConstrainVisible.setSelected((b) ? true : true) ;
      ImportCel.setSelected((b) ? true : false) ;
      ExportCel.setSelected((b) ? false : false) ;
      ImportComponent.setSelected((b) ? false : false) ;
      ComponentCel.setSelected((b) ? true : false) ;
      WriteCelOffset.setSelected((b) ? true : true) ;
      MultipleEvents.setSelected((b) ? false : true) ;
  }
   
   
   // A function to set KissLD options.
   
   static void setKissLDOptions()
   {
      compatapply = true ;
      setDragMove(false);
      setReleaseMove(true);
      setDropFixdrop(true);
      setCatchFixdrop(true);
      setVisibleUnfix(true);
      setEarlyFix(false);
      setDetachRestricted(false);
      setDetachMove(false);
      setDetachFix(false);
      setRetainKey(true);
      setStrictSyntax(true);
      setAutoEndif(true);
      setInvertGhost(false);
      setTransparentGroup(false);
      setMapCollide(false);
      setMoveXYCollide(true);
      setImmediateCollide(true);
      setImmediateUnfix(false);
      setImmediateEvent(false);
      setExpandEvents(false);
      setEnableShell(false);
      setXYOffsets(false);
      setAbsoluteWindow(false);
      setPanelVisible(false);
      setMouseInOutBox(false);
      setContextMap(true);
      setMapCount(false);
      setAllAmbiguous(false);
      setConstrainMoves(false) ;
      setDragMove(false) ;
      setConstrainVisible(true) ;
      setImportCel(true) ;
      setExportCel(false) ;
      setImportComponent(false) ;
      setComponentCel(true) ;
      setWriteCelOffset(true) ;
      setMultipleEvents(false) ;
      setMaxLock("32767") ;
      setMaxPageSet("10") ;
//    setTimerPeriod("60") ;
  }
   
   
   // A function to save the current property values.
   
   static boolean savePropertiesOptions() throws IOException
   {
      if (Kisekae.isSecure()) return false ;
      Properties p = new Properties() ;
      putPropertyOptions(p) ;
      return savePropertiesOptions(p) ;
   }
   
   static boolean savePropertiesOptions(Properties p) throws IOException
   {
      MainFrame mf = Kisekae.getMainFrame() ;
      try
      {
         if (Kisekae.isSecure()) return false ;
         if (p == null) return false ;
         FileOutputStream out = null ;
         String pathname ;
         
         try
         {
            File f = new File(properties) ;
            pathname = f.getAbsolutePath() ;
            out = new FileOutputStream(pathname) ;
            p.store(out,"UltraKiss Properties") ;
         }
         catch (Exception ex1)
         {
            try
            {
               URL url = Kisekae.getLoadBase() ;
               String userdir = url.getPath() ; 
               File f = new File(userdir,properties) ;
               pathname = f.getAbsolutePath() ;
               out = new FileOutputStream(pathname) ;
               p.store(out,"UltraKiss Properties") ;
            }
            catch (Exception ex2)
            {
               String userdir = System.getProperty("user.home") ;
               File f = new File(userdir,properties) ;
               pathname = f.getAbsolutePath() ;
               out = new FileOutputStream(pathname) ;
               p.store(out,"UltraKiss Properties") ;
            }
         }
         out.close() ;
         if (mf != null && properties != null) 
            mf.showStatus("UltraKiss Properties saved to " + properties) ;
         return true ;
      }
      catch (Exception e)
      {
         String s = "Unable to save UltraKiss Properties, " + e.getMessage() ;
         System.err.println(s) ;
         if (mf != null) mf.showStatus(s) ;
      }
      return false ;
   }
   
   
   // A function to save the persistent property values.  Options are 
   // restored to the persistent values then the final properties 
   // are written.
   
   static void saveFinalProperties() 
   {
      try
      {
         if (Kisekae.isSecure()) return ;
         loadPropertyOptions() ;
         if (playfkiss) clearCompatibilityOptions() ;
         if (directkiss) clearCompatibilityOptions() ;
         if (gnomekiss) clearCompatibilityOptions() ;
         if (kissld) clearCompatibilityOptions() ;
         Properties p = new Properties() ;
         putPropertyOptions(p) ;
         putFinalPropertyOptions(p) ;
         savePropertiesOptions(p) ;
      }
      catch (IOException e) { }
   }

   
   // A function to format and write option values that differ from
   // the initial factory option settings.  These are set specific options
   // that are saved in the configuration file.

   static void writeOptions(OutputStream out) throws IOException
   {
      writeheader = true ;
      boolean b = getCompatibilityMode() != null ;
//	   if (debugload != initdebugload) writeLine(out,"; debugload = " + debugload) ;
//	   if (debugedit != initdebugedit) writeLine(out,"; debugedit = " + debugedit) ;
//	   if (debugimage != initdebugimage) writeLine(out,"; debugimage = " + debugimage) ;
//	   if (debugevent != initdebugevent) writeLine(out,"; debugevent = " + debugevent) ;
//	   if (debugaction != initdebugaction) writeLine(out,"; debugaction = " + debugaction) ;
//	   if (debugvariable != initdebugvariable) writeLine(out,"; debugvariable = " + debugvariable) ;
//	   if (debugfkiss != initdebugfkiss) writeLine(out,"; debugfkiss = " + debugfkiss) ;
//	   if (debugsound != initdebugsound) writeLine(out,"; debugsound = " + debugsound) ;
//	   if (debugmovie != initdebugmovie) writeLine(out,"; debugmovie = " + debugmovie) ;
//	   if (debugmedia != initdebugmedia) writeLine(out,"; debugmedia = " + debugmedia) ;
//	   if (debugsearch != initdebugsearch) writeLine(out,"; debugsearch = " + debugsearch) ;
//	   if (debugdisabled != initdebugdisabled) writeLine(out,"; debugdisabled = " + debugdisabled) ;
//	   if (debugcomponent != initdebugcomponent) writeLine(out,"; debugcomponent = " + debugcomponent) ;
//	   if (debugmouse != initdebugmouse) writeLine(out,"; debugmouse = " + debugmouse) ;
//	   if (systemlf != initsystemlf) writeLine(out,"; systemlf = " + systemlf) ;
//	   if (javalf != initjavalf) writeLine(out,"; javalf = " + javalf) ;
//	   if (mediaminimize != initmediaminimize) writeLine(out,"; mediaminimize = " + mediaminimize) ;
//	   if (!kissweb.equals(initkissweb)) writeLine(out,"; kissweb = \"" + kissweb + "\"") ;
//	   if (!userdir.equals(inituserdir)) writeLine(out,"; userdir = \"" + userdir + "\"") ;
//	   if (!onlinehelp.equals(initonlinehelp)) writeLine(out,"; onlinehelp = \"" + onlinehelp + "\"") ;
//	   if (!website.equals(initwebsite)) writeLine(out,"; website = \"" + website + "\"") ;
	   if (!splashdir.equals(initsplashdir)) writeLine(out,"; splashdir = \"" + splashdir + "\"") ;
//	   if (!icondir.equals(initicondir)) writeLine(out,"; icondir = \"" + icondir + "\"") ;
//    if (applemac != initapplemac) writeLine(out,"; applemac = \"" + applemac + "\"") ;
//    if (linux != initlinux) writeLine(out,"; linux = \"" + linux + "\"") ;
      if (playfkiss) writeLine(out,"; playfkiss = \"" + playfkiss + "\"") ;
      else if (directkiss) writeLine(out,"; directkiss = \"" + directkiss + "\"") ;
      else if (gnomekiss) writeLine(out,"; gnomekiss = \"" + gnomekiss + "\"") ;
      else if (kissld) writeLine(out,"; kissld = \"" + kissld + "\"") ;
//    if (defaultplayfkiss) writeLine(out,"; defaultplayfkiss = " + defaultplayfkiss) ;
      if (acceptcnferrors != initacceptcnferrors) writeLine(out,"; acceptcnferrors = " + acceptcnferrors) ;
      if (pagesarescenes != initpagesarescenes) writeLine(out,"; pagesarescenes = " + pagesarescenes) ;
      if (multipleevents != initmultipleevents && !b) writeLine(out,"; multipleevents = " + multipleevents) ;
//	   if (backup != initbackup) writeLine(out,"; backup = " + backup) ;
//	   if (savesource != initsavesource) writeLine(out,"; savesource = " + savesource) ;
//	   if (loadclose != initloadclose) writeLine(out,"; loadclose = " + loadclose) ;
//	   if (sound != initsound) writeLine(out,"; sound = " + sound) ;
//	   if (movie != initmovie) writeLine(out,"; movie = " + movie) ;
//	   if (event != initevent) writeLine(out,"; event = " + event) ;
//	   if (animate != initanimate) writeLine(out,"; animate = " + animate) ;
//	   if (timer != inittimer) writeLine(out,"; timer = " + timer) ;
	   if (editenable != initeditenable) writeLine(out,"; editenable = " + editenable) ;
	   if (securityenable != initsecurityenable) writeLine(out,"; securityenable = " + securityenable) ;
//	   if (initedit != initinitedit) writeLine(out,"; initedit = " + initedit) ;
      if (javasound != initjavasound) writeLine(out,"; javasound = " + javasound) ;
	   if (cacheaudio != initcacheaudio) writeLine(out,"; cacheaudio = " + cacheaudio) ;
	   if (cachevideo != initcachevideo) writeLine(out,"; cachevideo = " + cachevideo) ;
	   if (cacheimage != initcacheimage) writeLine(out,"; cacheimage = " + cacheimage) ;
	   if (suspendmedia != initsuspendmedia) writeLine(out,"; suspendmedia = " + suspendmedia) ;
	   if (stopmusic != initstopmusic) writeLine(out,"; stopmusic = " + stopmusic) ;
	   if (soundsingle != initsoundsingle && !b) writeLine(out,"; soundsingle = " + soundsingle) ;
	   if (longsoundmedia != initlongsoundmedia) writeLine(out,"; longsoundmedia = " + longsoundmedia) ;
	   if (adjustmediavolume != initadjustmediavolume) writeLine(out,"; adjustmediavolume = " + adjustmediavolume) ;
	   if (scaletofit != initscaletofit) writeLine(out,"; scaletofit = " + scaletofit) ;
	   if (sizetofit != initsizetofit) writeLine(out,"; sizetofit = " + sizetofit) ;
	   if (retainwindowsize != initretainwindowsize) writeLine(out,"; retainwindowsize = " + retainwindowsize) ;
	   if (maximizewindow != initmaximizewindow) writeLine(out,"; maximizewindow = " + maximizewindow) ;
	   if (randomsplash != initrandomsplash) writeLine(out,"; randomsplash = " + randomsplash) ;
	   if (showborder != initshowborder) writeLine(out,"; showborder = " + showborder) ;
	   if (inittoolbar != initinittoolbar) writeLine(out,"; inittoolbar = " + inittoolbar) ;
//	   if (tbtools != inittbtools) writeLine(out,"; tbtools = " + tbtools) ;
//	   if (tbcolors != inittbcolors) writeLine(out,"; tbcolors = " + tbcolors) ;
//	   if (tbpages != inittbpages) writeLine(out,"; tbpages = " + tbpages) ;
//	   if (tbedits != inittbedits) writeLine(out,"; tbedits = " + tbedits) ;
//	   if (tbcompat != inittbcompat) writeLine(out,"; tbcompat = " + tbcompat) ;
	   if (initstatusbar != initinitstatusbar) writeLine(out,"; initstatusbar = " + initstatusbar) ;
	   if (initmenubar != initinitmenubar) writeLine(out,"; initmenubar = " + initmenubar) ;
	   if (constrainmoves != initconstrainmoves && !b) writeLine(out,"; constrainmoves = " + constrainmoves) ;
	   if (constrainvisible != initconstrainvisible && !b) writeLine(out,"; constrainvisible = " + constrainvisible) ;
	   if (constrainfkiss != initconstrainfkiss) writeLine(out,"; constrainfkiss = " + constrainfkiss) ;
	   if (autoscroll != initautoscroll) writeLine(out,"; autoscroll = " + autoscroll) ;
	   if (dragmove != initdragmove && !b) writeLine(out,"; dragmove = " + dragmove) ;
	   if (releasemove != initreleasemove && !b) writeLine(out,"; releasemove = " + releasemove) ;
	   if (dropfixdrop != initdropfixdrop && !b) writeLine(out,"; dropfixdrop = " + dropfixdrop) ;
	   if (catchfixdrop != initcatchfixdrop && !b) writeLine(out,"; catchfixdrop = " + catchfixdrop) ;
	   if (visibleunfix != initvisibleunfix && !b) writeLine(out,"; visibleunfix = " + visibleunfix) ;
	   if (earlyfix != initearlyfix && !b) writeLine(out,"; earlyfix = " + earlyfix) ;
	   if (detachrestricted != initdetachrestricted && !b) writeLine(out,"; detachrestricted = " + detachrestricted) ;
	   if (detachmove != initdetachmove && !b) writeLine(out,"; detachmove = " + detachmove) ;
	   if (detachfix != initdetachfix && !b) writeLine(out,"; detachfix = " + detachfix) ;
	   if (invertghost != initinvertghost && !b) writeLine(out,"; invertghost = " + invertghost) ;
	   if (transparentgroup != inittransparentgroup && !b) writeLine(out,"; transparentgroup = " + transparentgroup) ;
	   if (mapcollide != initmapcollide && !b) writeLine(out,"; mapcollide = " + mapcollide) ;
	   if (movexycollide != initmovexycollide && !b) writeLine(out,"; movexycollide = " + movexycollide) ;
	   if (automedialoop != initautomedialoop) writeLine(out,"; automedialoop = " + automedialoop) ;
	   if (autofullscreen != initautofullscreen) writeLine(out,"; autofullscreen = " + autofullscreen) ;
	   if (mediacenter != initmediacenter) writeLine(out,"; mediacenter = " + mediacenter) ;
	   if (mediamusicresume != initmediamusicresume) writeLine(out,"; mediamusicresume = " + mediamusicresume) ;
	   if (keepaspect != initkeepaspect) writeLine(out,"; keepaspect = " + keepaspect) ;
	   if (keycase != initkeycase) writeLine(out,"; keycase = " + keycase) ;
	   if (variablecase != initvariablecase) writeLine(out,"; variablecase = " + variablecase) ;
//	   if (importcel != initimportcel && !b) writeLine(out,"; importcel = " + importcel) ;
//	   if (exportcel != initexportcel && !b) writeLine(out,"; exportcel = " + exportcel) ;
//	   if (componentcel != initcomponentcel && !b) writeLine(out,"; componentcel = " + componentcel) ;
//	   if (importcomponent != initimportcomponent && !b) writeLine(out,"; importcomponent = " + importcomponent) ;
//	   if (expandevents != initexpandevents && !b) writeLine(out,"; expandevents = " + expandevents) ;
//	   if (enableshell != initenableshell && !b) writeLine(out,"; enableshell = " + enableshell) ;
//	   if (xyoffsets != initxyoffsets && !b) writeLine(out,"; xyoffsets = " + xyoffsets) ;
//	   if (absolutewindow != initabsolutewindow && !b) writeLine(out,"; absolutewindow = " + absolutewindow) ;
//	   if (panelvisible != initpanelvisible && !b) writeLine(out,"; panelvisible = " + panelvisible) ;
//	   if (mouseinoutbox != initmouseinoutbox && !b) writeLine(out,"; mouseinoutbox = " + mouseinoutbox) ;
//	   if (alteditdrag != initalteditdrag) writeLine(out,"; alteditdrag = " + alteditdrag) ;
//	   if (shifteditdrag != initshifteditdrag) writeLine(out,"; shifteditdrag = " + shifteditdrag) ;
//	   if (importrelative != initimportrelative) writeLine(out,"; importrelative = " + importrelative) ;
//	   if (writeceloffset != initwriteceloffset) writeLine(out,"; writeceloffset = " + writeceloffset) ;
//	   if (showbreakpointend != initshowbreakpointend) writeLine(out,"; showbreakpointend = " + showbreakpointend) ;
//	   if (showstepintoend != initshowstepintoend) writeLine(out,"; showstepintoend = " + showstepintoend) ;
//	   if (writecomment != initwritecomment) writeLine(out,"; writecomment = " + writecomment) ;
//	   if (eventpause != initeventpause) writeLine(out,"; eventpause = " + eventpause) ;
//	   if (actionpause != initactionpause) writeLine(out,"; actionpause = " + actionpause) ;
//	   if (disableall != initdisableall) writeLine(out,"; disableall = " + disableall) ;
//	   if (retainkey != initretainkey && !b) writeLine(out,"; retainkey = " + retainkey) ;
//	   if (strictsyntax != initstrictsyntax && !b) writeLine(out,"; strictsyntax = " + strictsyntax) ;
//	   if (showundefs != initshowundefs && !b) writeLine(out,"; showundefs = " + showundefs) ;
//	   if (autoendif != initautoendif && !b) writeLine(out,"; autoendif = " + autoendif) ;
	   if (contextmap != initcontextmap && !b) writeLine(out,"; contextmap = " + contextmap) ;
	   if (mapcount != initmapcount && !b) writeLine(out,"; mapcount = " + mapcount) ;
	   if (allambiguous != initallambiguous && !b) writeLine(out,"; allambiguous = " + allambiguous) ;
	   if (immediatecollide != initimmediatecollide && !b) writeLine(out,"; immediatecollide = " + immediatecollide) ;
	   if (immediateunfix != initimmediateunfix && !b) writeLine(out,"; immediateunfix = " + immediateunfix) ;
	   if (immediateevent != initimmediateevent && !b) writeLine(out,"; immediateevent = " + immediateevent) ;
	   if (!eventqueues.equals(initeventqueues)) writeLine(out,"; eventqueues = \"" + eventqueues + "\"") ;
	   if (!timerperiod.equals(inittimerperiod) && !b) writeLine(out,"; timerperiod = \"" + timerperiod + "\"") ;
	   if (!gifperiod.equals(initgifperiod)) writeLine(out,"; gifperiod = \"" + gifperiod + "\"") ;
	   if (!sceneperiod.equals(initsceneperiod)) writeLine(out,"; sceneperiod = \"" + sceneperiod + "\"") ;
	   if (!audioperiod.equals(initaudioperiod)) writeLine(out,"; audioperiod = \"" + audioperiod + "\"") ;
	   if (!stickyflex.equals(initstickyflex)) writeLine(out,"; stickyflex = \"" + stickyflex + "\"") ;
	   if (!maxflex.equals(initmaxflex)) writeLine(out,"; maxflex = \"" + maxflex + "\"") ;
	   if (!maxlock.equals(initmaxlock) && !b) writeLine(out,"; maxlock = \"" + maxlock + "\"") ;
	   if (!maxpageset.equals(initmaxpageset) && !b) writeLine(out,"; maxpageset = \"" + maxpageset + "\"") ;
	   if (!maxcolorset.equals(initmaxcolorset)) writeLine(out,"; maxcolorset = \"" + maxcolorset + "\"") ;
//	   if (!maxlrufiles.equals(initmaxlrufiles)) writeLine(out,"; maxlrufiles = \"" + maxlrufiles + "\"") ;
	   if (!longduration.equals(initlongduration)) writeLine(out,"; longduration = \"" + longduration + "\"") ;
	   if (!mediavolume.equals(initmediavolume)) writeLine(out,"; mediavolume = \"" + mediavolume + "\"") ;
//	   if (!jpegquality.equals(initjpegquality)) writeLine(out,"; jpegquality = \"" + jpegquality + "\"") ;
//	   if (!undolimit.equals(initundolimit)) writeLine(out,"; undolimit = \"" + undolimit + "\"") ;
//	   if (!commentcol.equals(initcommentcol)) writeLine(out,"; commentcol = \"" + commentcol + "\"") ;
//	   if (!indentspace.equals(initindentspace)) writeLine(out,"; indentspace = \"" + indentspace + "\"") ;
      if (!language.equals(initlanguage)) writeLine(out,"; language = \"" + language + "\"") ;
      if (!encoding.equals(initencoding)) writeLine(out,"; encoding = \"" + encoding + "\"") ;
//    if (!exporttype.equals(initexporttype)) writeLine(out,"; exporttype = \"" + exporttype + "\"") ;
//    if (!browser.equals(initbrowser)) writeLine(out,"; browser = \"" + browser + "\"") ;

      // Allow some option changes in PlayFKiss compatibility mode

      if (playfkiss)
      {
         if (!timerperiod.equals("60") && b) writeLine(out,"; timerperiod = \"" + timerperiod + "\"") ;
         if (releasemove != true && b) writeLine(out,"; releasemove = " + releasemove) ;  
         if (strictsyntax != true && b) writeLine(out,"; strictsyntax = " + strictsyntax) ;  
         if (multipleevents != false && b) writeLine(out,"; multipleevents = " + multipleevents) ;
      }
   }

   
	// A function to write a line to our output stream.

	private static void writeLine(OutputStream out, String s)
		throws IOException
	{
      String encoding = Kisekae.getLanguageEncoding() ;
		String ls = System.getProperty("line.separator") ;
		if (ls == null) ls = "\n" ;

      // Write the section header.

      if (writeheader)
      {
         out.write(" ".getBytes(encoding)) ;
         out.write(ls.getBytes(encoding)) ;
         out.write(";[Option Section]".getBytes(encoding)) ;
         out.write(ls.getBytes(encoding)) ;
         writeheader = false ;
      }
		out.write(s.getBytes(encoding)) ;
		out.write(ls.getBytes(encoding)) ;
	}
   
   void setValues() { }   
   
   
   
   // Inner class to render a combo box with disabled items.
   
   class ComboRenderer extends JLabel implements ListCellRenderer 
   {
      public ComboRenderer() 
      {
         setOpaque(true);
         setBorder(new EmptyBorder(1, 1, 1, 1));
      }

      public Component getListCellRendererComponent( JList list, 
          Object value, int index, boolean isSelected, boolean cellHasFocus) 
      {
         if (isSelected) 
         {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }	
         if (!((value instanceof ComboItem) && ((ComboItem) value).isEnabled())) 
         {
            setBackground(list.getBackground());
            setForeground(UIManager.getColor("Label.disabledForeground"));
         }
         setFont(list.getFont());
         setText((value == null) ? "" : value.toString());
         return this;
      }  
   }
   
   
   // Inner class to manage selections on a combo box with disabled items.
   
   class ComboListener implements ActionListener 
   {
      JComboBox combo;
      Object currentItem;
    
      ComboListener(JComboBox combo) 
      {
         this.combo  = combo;
         combo.setSelectedIndex(0);
         currentItem = combo.getSelectedItem();
      }
    
      public void actionPerformed(ActionEvent e) 
      {
         Object tempItem = combo.getSelectedItem();
         if (!(tempItem instanceof ComboItem))
         {
            currentItem = tempItem;
            if (me != null) me.actionPerformed(e) ;
         }
         else if (!((ComboItem)tempItem).isEnabled()) 
            combo.setSelectedItem(currentItem);
         else 
         {
            currentItem = tempItem;
            if (me != null) me.actionPerformed(e) ;
         }
      }
   }
   
   
   // Inner class to define a combo box item that can be disabled.
   
   class ComboItem
   {
      Object  obj;
      boolean isEnable;
    
      ComboItem(Object obj, boolean isEnable) 
      {
         this.obj      = obj;
         this.isEnable = isEnable;
      }
    
      ComboItem(Object obj) { this(obj, true); }
    
      public boolean isEnabled() { return isEnable; }
    
      public void setEnabled(boolean isEnable) { this.isEnable = isEnable; }
    
      public String toString() { return obj.toString(); }
   }
}   
   
   // Inner class to define a scrollable panel for our compatibility options.
   
   class ScrollablePanel extends JPanel implements Scrollable
   {
      private int maxUnitIncrement = 1;
      
      public ScrollablePanel()
      {
         Dimension d = new JCheckBox().getPreferredSize() ;
         if (d != null) maxUnitIncrement = d.height ;
         if (maxUnitIncrement == 0) maxUnitIncrement = 1 ;
      }
      
      
      public Dimension getPreferredScrollableViewportSize() 
      {
         return getPreferredSize();
      }
      
      public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) 
      {
            return visibleRect.height - maxUnitIncrement;
      }
      
      public boolean getScrollableTracksViewportHeight() { return false; }
      
      public boolean getScrollableTracksViewportWidth() { return false; }
      
      public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) 
      {
            return maxUnitIncrement;
      }
   }

