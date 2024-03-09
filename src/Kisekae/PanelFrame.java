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
* PanelFrame Class
*
* Purpose:
*
* This object encapsulates the drawing panel that represents a data
* set window.  It paints the data set panel and it contains helper methods
* to both draw and update the screen.  This module supports full undo edit
* recovery for all changes to a configuration data set.  It also handles all
* keyboard input and key events for the set.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.geom.Rectangle2D ;
import java.awt.font.FontRenderContext ;
import java.util.* ;
import java.util.Enumeration ;
import java.awt.event.* ;
import java.awt.print.* ;
import java.awt.datatransfer.* ;
import java.net.URL ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.undo.* ;


final class PanelFrame extends JPanel
   implements MouseListener, MouseMotionListener, KeyListener, Printable, ClipboardOwner
{
   private final int SELECT = 0 ;
   private final int CUT = 1 ;
   private final int COPY = 2 ;
   private final int PASTE = 3 ;
   private final int MOVE = 4 ;
   private final int ADDPAGE = 5 ;
   private final int REMOVEPAGE = 6 ;
   private final int PALETTE = 7 ;
   private final int RESET = 8 ;
   private final int WRITEPAGE = 9 ;
   private final int IMPORT = 10 ;
   private final int UNGROUP = 11 ;
   private final int GROUP = 12 ;
   private final int NEWGROUP = 13 ;
   private final int IMPORTCUT = 14 ;
   private final int IMPORTPASTE = 15 ;
   private final int SIZE = 16 ;
   private final int SCALE = 17 ;
   private final int COMPONENT = 18 ;
   private final int IMPORTPALETTE = 19 ;
   private final int IMPORTPALETTECUT = 20 ;
   private final int IMPORTPALETTEADD = 21 ;
   private final int PASTENEW = 22 ;
   private final int SIZECEL = 23 ;
   private final int ATTRIBUTES = 24 ;
   private final int SELECTSET = 25 ;

   private MainFrame parent = null ;		// Reference to our parent frame
   private PanelFrame panel = null ;		// Reference to ourselves
   private ScaleDialog sd = null ;			// Reference to our scaling window
   private PopupMenu popup = null ;			// Reference to our context menu
   private Dimension panelSize = null ;	// The panel dimensions
   private Rectangle panelArea = null ;   // The panel area rectangle
   private Point windowOffset = null ;    // The panel window offset
   private Dimension windowSize = null ;  // The panel window size

   private Configuration config = null ;	// The configuration object
   private Vector cels = null ;				// The set of cels in the data set
   private Vector groups = null ;			// The set of groups in the data set
   private PageSet page = null ;				// The active page set to display
   private Palette palette = null ;			// The default color palette
   private Color background = null ;		// The panel background color
   private Color border = null ;				// The frame background color
   private Color darkborder = null ;		// The dark border color
   private Color lightborder = null ;		// The light border color
   private Vector evt = null ;				// The events to process

   private Image fullImage = null ;			// The image to paint
   private Image baseImage = null ;			// The image, excluding drag cels
   private Graphics fullgc = null ;			// Graphics context for full image
   private Graphics basegc = null ;			// Graphics context for base image
   private Vector printimage = null ;     // The set of printable page images
   private Rectangle imageArea = null ;   // The area of our image buffers

   private int x, y ;							// Panel centering coordinates
   private int celNum ;							// Our selected cel number
   private int groupNum ;						// The selected cel group number
   private int posX, posY ;					// The mouse drag event coordinates
   private int editX, editY ;					// The mouse edit drag coordinates
   private int mouseX, mouseY ;				// The real-time mouse coordinates
   private int moveX, moveY ;				   // The mouse moved event coordinates
   private int originalflex ;					// Original flex on mouse down
   private int newflex ;                  // Updated flex on mouse down
   private float sf = 1.0f ;					// Screen image scaling factor
   private float oldsf = 1.0f ;				// Screen image prior scaling factor
   private float newsf = 1.0f ;				// Screen image new scale factor
   private Object dragobject = null ;     // The current object being dragged

   private boolean flexdrop = false ;		// True if dropped a sticky group
   private boolean movable = false ;		// True if group can be moved
   private boolean sticky = false ;			// True if group is sticky
   private boolean fixed = false ;			// True if group is fixed
   private boolean locked = false ;			// True if group is fixed locked
   private boolean recovery = false ; 		// True if attempting error recovery
   private boolean enabledrag = true ; 	// True if mouse drag is possible
   private boolean redrawimage = false ; 	// True if cel list has changed
   private boolean clearscreen = false ; 	// True if screen must clear
   private boolean flicker = false ;      // True if selection boxes flicker
   private boolean mousedown = false ; 	// True if mouse pressed
   private boolean metadown = false ;     // True if mouse meta pressed

   private Cel cel = null ;					// A reference to the selected cel
   private Group group = null ;				// A reference to the selected group
   private Group groupset = null ;			// A reference to a selection set
   private Group pko = null ;			      // A reference for group collisions
   private Cel lastcel = null ;				// The last selected cel
   private Group lastgroup = null ;			// The last selected group
   private int [] celList = null ;			// The list of cels on this page
   private int [] baseList = null ;			// The list of cels excluding group
   private Rectangle box = null ;			// The mouse drawing bounding box
   private Rectangle priorbox = null ;		// The previous drag bounding box
   private Rectangle scrollbox = null ;	// The scrollable object bounding box
   private Rectangle restrictbox = null ;	// The restriction drag bounding box
   private Point flexvalue = null ;			// Our current group sticky value
   private Point location = null ;			// Our initial object location
   private Hashtable eventstate = null ;  // Our event collision state table
   private Object [] collide = null ;     // The cels that collide

   private Cursor defaultcursor = null ;	// The default cursor
   private Cursor presscursor = null ;		// The object selection cursor
   private Cursor dragcursor = null ;		// The object movement cursor
   private Cursor selectcursor = null ;	// The object selection cursor
   private Cursor waitcursor = null ;	   // The object selection cursor
   private Cursor east = null ;           // The east resize cursor
   private Cursor west = null ;           // The west resize cursor
   private Cursor north = null ;          // The north resize cursor
   private Cursor south = null ;          // The south resize cursor
   private Cursor northeast = null ;      // The northeast resize cursor
   private Cursor northwest = null ;      // The northwest resize cursor
   private Cursor southeast = null ;      // The southeast resize cursor
   private Cursor southwest = null ;      // The southwest resize cursor

   private boolean editmode = false ;		// True if selecting objects
   private boolean undoredo = false ;		// True if performing undo/redo
   private Rectangle selectbox = null ;	// The object edit selection box
   private PanelEdit selection = null ;	// The set of selected objects
   private Dimension celbasesize = null ;	// The cel size on mouse down
   private Point celbaselocation = null ;	// The cel location on mouse down
   private Clipboard clipboard = null ;	// Our internal clipboard
   private UndoManager undo = null ;   	// Our undo manager

   private Cel incel = null ;					// A reference to the mouseover cel
   private Group ingroup = null ;			// A reference to the mouseover group

   private char keychar = 0 ; 				// The last key event char
   private int keycode = 0 ; 					// The last key event code
   private String keystring = null ;		// The last key string
   private String keytextline = null ;		// The set of last keys pressed to CR
   private String keymodifier = null ;		// The name of the last key modifiers
   private String multikey = null ;		   // The last set of multiple keypress
   private boolean newkeytext = true ;	   // If true, starts a new keystroke string
   private boolean newmultikey = true ;	// If true, starts a new multikey string


   // Constructor

   public PanelFrame (MainFrame mainframe)
   {
      this.panel = this ;
      this.parent = mainframe ;
      setLayout(null) ;

      // Allocate default resources.

      panelSize = new Dimension(448,320) ;
      panelArea = new Rectangle(panelSize) ;
      windowSize = new Dimension(panelSize) ;
      windowOffset = new Point(0,0) ;

      // Create our internal clipboard for object transfers.  The PanelFrame
      // object is the clipboard owner.

      popup = new PopupMenu() ;
      clipboard = new Clipboard("Panel Frame") ;

      // Set up to catch mouse and key events in this panel.

      addMouseListener(this) ;
      addMouseMotionListener(this) ;
      addKeyListener(this) ;
   }


   // Object state reference methods
   // ------------------------------

   // Method to return the panel size.  This is the drawing area
   // excluding any insets and is the actual, scaled panel size.

   Dimension getPanelSize() { return panelSize ; }


   // Method to return the window rectangle.  This is a subset of the
   // panel frame and is the actual, scaled window size.

   Rectangle getWindow() { return new Rectangle(windowOffset,windowSize) ; }


   // Return the preferred size for this panel frame.  This is
   // the scaled panel size plus its border, plus one more pixel
   // on each side.  This method is required for proper scrolling.

   public Dimension getPreferredSize()
   {
      int h = panelSize.height + 2 + 2 ;
      int w = panelSize.width + 2 + 2 ;
      return new Dimension(w,h) ;
   }


   // Method to return the active page set on display.

   PageSet getPage() { return page ; }


   // Method to return the active palette in use.

   Palette getPalette() { return palette ; }


   // Method to get the active multipalette for the current page.

   Integer getMultiPalette()
   { return (page == null) ? null : page.getMultiPalette() ; } ;


   // Method to return the current scaling factor in use.

   float getScaleFactor() { return sf ; }


   // Method to return the current selection set.

   PanelEdit getSelection() { return selection ; }


   // Method to return the current clipboard.

   Clipboard getClipboard() { return clipboard ; }


   // Method to return the current object being dragged vy the mouse.

   Object getDragObject() { return dragobject ; }


   // Method to return the current mouse coordinates.

   int getMouseX() { return mouseX ; }
   int getMouseY() { return mouseY ; }


   // Method to return the last key typed.

   int getKeyCode() { return keycode ; }
   String getKeyString() { return keytextline ; }
   String getKeyModifier() { return keymodifier ; }
   String getKeyCombination() { return multikey ; }
   String getActiveKeyChar() { return (newmultikey) ? "" : "" + keychar ; }
   String getActiveKeyCombination() { return (newmultikey) ? "" : multikey ; }


   // Method to return the last key character. This is upper case unless
   // the keyboard case option is set. All control characters except for 
   // newline are returned as null strings.

   String getKeyChar() 
   {
      String s = "" + keychar ;
      if (!Character.isDefined(keychar)) s = "" ;
      if (Character.isISOControl(keychar) && keychar != '\n') s = "" ;
      if (!OptionsDialog.getKeyCase()) s = s.toUpperCase() ;
      return s ; 
   }


   // Method to return the current group being acted upon.
   // This is the group set primary group if we have attached groups.

   Group getGroup()
   {
      if (group == null) return null ;
      Group g = group.getPrimaryGroup() ;
      return (g != null) ? g : group ;
   }


   // Method to return the last group being acted upon.

   Group getLastGroup() { return lastgroup ; }


   // Method to return the current cel being acted upon.

   Cel getCel() { return cel ; }


   // Method to return the last cel being acted upon.

   Cel getLastCel() { return lastcel ; }


   // Method to return a reference to the full panel image on display.

   Image getImage() { return fullImage ; }
   Image getBaseImage() { return baseImage ; }


   // Method to get our parent MainFrame object.

   MainFrame getMainFrame() { return parent ; }


   // Method to return our current edit mode state.

   boolean isEditOn() { return (selection != null) ; }
   boolean isClipOn() { return (clipboard.getContents(this) != null) ; }


   // Method to determine if a single group object is selected.

   boolean isGroupSelected()
   {
      if (isUngrouped()) return false ;
      if (groupset == null) return false ;
      if (groupset.getGroupCount() != 1) return false ;
      return true ;
   }


   // Method to determine if a single group or cel object is selected.

   boolean isObjectSelected()
   {
      if (groupset == null) return false ;
      if (groupset.getGroupCount() != 1) return false ;
      return true ;
   }


   // Method to determine if we are ungrouped.

   boolean isUngrouped()
   {
      if (selection == null) return false ;
      if (selection.size() == 0) return false ;
      if (selection.elementAt(0) instanceof Group) return false ;
      return true ;
   }


   // Method to determine if a specific cel or group is selected.  A cel in
   // a selected group is selected.

   boolean isSelected(KissObject kiss)
   {
      if (selection == null) return false ;
      if (selection.size() == 0) return false ;
      if (selection.contains(kiss)) return true ;
      if (!(kiss instanceof Cel)) return false ;
      Object o = ((Cel) kiss).getGroup() ;
      if (!(o instanceof Group)) return false ;
      return selection.contains(o) ;
   }



   // Object state change methods
   // ---------------------------

   // Method to set the panel location.  This is the (x,y) point
   // where the panel is centered for drawing in its parent window
   // frame, adjusted by the current window size.

   void setOffset(int x, int y)
   {
      this.x = x + ((panelSize.width - windowSize.width) / 2) ;
      this.y = y + ((panelSize.height - windowSize.height) / 2) ;
   }


   // Method to set our background color to a palette index value.
   // This is an undoable edit.

   public void setBorderIndex(int n)
   {
      if (config == null) return ;
      boolean rgb = config.isBorderRgb() ;
      int oldborder = config.getBorder() ;
      config.setBorderIndex(n) ;
      config.setUpdated(true) ;
      Color c = config.getBorderColor() ;
      parent.setBackground(c) ;
      setBackground(c) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoableColorEdit ce = new UndoableColorEdit(rgb,oldborder,false,n) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our interface.

      parent.updateMenu() ;
      showpage() ;
   }


   // Method to set our background color to an RGB value.
   // This is an undoable edit.

   public void setBorderRgb(int n)
   {
      if (config == null) return ;
      boolean rgb = config.isBorderRgb() ;
      int oldborder = config.getBorder() ;
      config.setBorderRgb(n) ;
      config.setUpdated(true) ;
      Color c = config.getBorderColor() ;
      parent.setBackground(c) ;
      setBackground(c) ;

      // Adjust the playfield background if there is no palette.

      palette = config.getPalette(0) ;
      if (palette == null) background = c ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoableColorEdit ce = new UndoableColorEdit(rgb,oldborder,true,n) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our interface.

      parent.updateMenu() ;
      showpage() ;
   }


   // Method to set the configuration size and adjust our panel to fit.
   // No adjustment is made if the requested size equals the current
   // size.

   void setConfigSize(Dimension d2)
   {
      if (d2 == null) return ;
      if (config == null) return ;
      Dimension d1 = config.getSize() ;
      if ((d1.width == d2.width) && (d1.height == d2.height)) return ;

      // Perform the configuration size update.  

      try
      {
         config.setSize(d2) ;
         resizepanel(d2) ;
         if (!undoredo) createSizeChangeEdit(d1,d2) ;
         changesize(d2.width,d2.height) ;
      }

      // Watch for memory or internal errors due to an invalid size.

      catch (OutOfMemoryError e)
      {
         System.out.println("PanelFrame: Out of memory on size change " + d2) ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
         config.setSize(d1) ;
      }
      catch (Exception e)
      {
         System.out.println("PanelFrame: Size change exception " + e.toString()) ;
         e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + e.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         config.setSize(d1) ;
      }
   }


   // Method to signal that the complete image needs to be redrawn on
   // a redraw request.  This can occur if layering adjustments are made.

   void setRedraw(boolean b) { redrawimage = b ; }

   
   // Method to return our panel offset.  This is the (x,y) point
   // where the panel is centered for drawing in its parent window
   // frame, adjusted by the current window size.

   Point getOffset() { return new Point(x,y) ; }


   // Method to enable mouse dragging.

   void setEnableDrag(boolean b) { enabledrag = b ; }


   // Method to signal that a setfix action has changed the mouse
   // selected object flex value. 

   void setFixChanged() { originalflex = -1 ; }


   // Method to cut the selected objects from the current page.  This copies
   // the specified transferable object to the clipboard and then deletes the
   // objects from the current page set.  The transferable object must be a
   // PanelEdit selection set which is a vector of the objects currently
   // selected.  Items cut from an unmarked selection include the complete
   // selection set.  After the cut the new selection set is empty.  Items
   // cut from a marked selection set include only the marked items.  After
   // the cut the selection set is the unmarked items.

   void editCut(Transferable t) { editCut(t,false) ; }
   void editCut(Transferable t, boolean internal)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      boolean marked = edit.isMarked() ;
      boolean pasted = edit.isPasted() ;
      if (!undoredo) editCopy(t) ;
      suspendEvents() ;

      // Initialize our new selection set.  Cut objects are removed
      // from our current selection set.

      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      if (selection != null) newselection.addAll(selection) ;

      // Remove the selection objects from the current page set.  Note
      // that our selection set may not have current object references
      // if undo and redo edits have occurred.  We must establish the
      // correct object based upon the selection object identifier.

      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         if (marked && !edit.isMarked(i)) continue ;
         if (pasted && !edit.isPasted(i)) continue ;
         if (OptionsDialog.getDebugEdit())
            System.out.println("Edit: cut begin for element " + kiss + " on page " + page) ;
         newselection.remove(kiss) ;

         // Cuts of group objects occur for grouped items.

         if (kiss instanceof Group)
         {
            Group g = (Group) kiss ;
            Object id = g.getIdentifier() ;
            g = (Group) Group.getByKey(Group.getKeyTable(),config.getID(),id) ;
            if (g == null) continue ;

            // If the group is removed from the page it will no longer be
            // displayed.  However, cels must remain associated with this
            // page as this is the source page required for undo processing.
            // Without the cel page association we do not know which cels
            // to activate if the group is restored.

            page.removeGroup(g) ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: cut remove group " + g + " on page " + page) ;

            // We must determine if the group is active on any other page.

            boolean groupinuse = false ;
            Vector pages = config.getPages() ;
            for (int j = 0 ; j < pages.size() ; j++)
            {
               PageSet ps = (PageSet) pages.elementAt(j) ;
               if (!ps.contains(g)) continue ;
               groupinuse = true ;
               break ;
            }

            // All cels that are cut must be marked as internal cels if the
            // group they are associated with is no longer active.  This flags
            // the cels and they will not be written to the configuration file
            // if the configuration is saved.  We must also stop any video cels
            // playing in the cut group.  Cut cels can no longer appear on
            // all pages.

            Vector groupcels = g.getCels() ;
            for (int j = 0 ; j < groupcels.size() ; j++)
            {
               Object o = groupcels.elementAt(j) ;
               if (!(o instanceof Cel)) continue ;
               Cel c = (Cel) o ;
               if (!groupinuse) c.setInternal(true) ;
               if (!undoredo) c.setUndoAllPages(c.isOnAllPage()) ;
               c.setAllPages(false) ;
               if (!(o instanceof Video)) continue ;
               Video video = (Video) o ;
               video.stop(video) ;
            }

            // If the group is no longer in use remove it from the
            // configuration group list.  We retain it in the group
            // hash table so that it can be located on an edit undo.

            if (!groupinuse)
            {
               Vector groups = config.getGroups() ;
               groups.remove(g) ;
            }
         }

         // Cuts of cel objects occur for ungrouped items.

         if (kiss instanceof Cel)
         {
            Cel c = (Cel) kiss ;
            Object id = c.getIdentifier() ;
            c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),id) ;
            if (c == null) continue ;
            if (c.isError()) continue ;

            // Cels that are cut have this page removed from their page list.
            // If the parent group is no longer contained on the page it is
            // removed from the page set.  If the cel is not on any pages
            // it becomes an internal cel. 

            Object o = c.getGroup() ;
            Vector pages = c.getPages() ;
            Integer pid = (Integer) page.getIdentifier() ;
            pages.removeElement(pid) ;
            if (!undoredo) c.setUndoAllPages(c.isOnAllPage()) ;
            c.setAllPages(false) ;
            if (pages.size() == 0) c.setInternal(true) ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: cut " + c + " remove from page " + pid) ;
            if (c instanceof JavaCel) 
               ((JavaCel) c).showComponent(false) ;
            if (c instanceof Video)
            {
               Video video = (Video) c ;
               video.stop(video) ;
            }

            // Establish the parent group context.  Remove the group from the
            // page if it is not on this page.

            if (o instanceof Group)
            {
               Group g = (Group) o ;
               g.rebuildBoundingBox() ;
               g.eliminateOffset() ;
               g.setContext(pid) ;
               if (!g.isOnPage(pid))
               {
                  page.removeGroup(g) ;
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: cut remove group " + g + " on page " + page) ;
               }
               else
               {

                  // Compute the relative placement offset necessary to move the
                  // group to the required position.

                  Point grouplocation = g.getLocation() ;
                  Rectangle box = g.getBoundingBox() ;
                  Point offset = g.getOffset() ;
                  int x = (grouplocation.x - (box.x-offset.x)) ;
                  int y = (grouplocation.y - (box.y-offset.y)) ;
                  g.setPlacement(x,y) ;
                  g.drop() ;
               }
            }
         }
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(CUT,edit,selection,newselection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // If we have a new selection create a new group set.  The group set is
      // the collection of selected objects that can be picked up and moved
      // as a complete unit during mouse events.

      groupset = null ;
      selection = null ;
      if (newselection.size() > 0)
      {
         groupset = new Group() ;
         selection = newselection ;
         for (int i = 0 ; i < selection.size() ; i++)
            groupset.addElement((KissObject) selection.elementAt(i),false) ;
      }
      int pageset = ((Integer) page.getIdentifier()).intValue() ;
      celList = createCelList(pageset) ;
      baseList = new int [celList.length] ;
      for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
      page.setChanged(true) ;
      config.setUpdated(true) ;

      // If this is not an internal cut, redraw the page.

      if (internal) return ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      showpage() ;
   }


   // Method to copy the specified objects to the clipboard.  The set retained
   // in the clipboard is a cloned copy of the selection set so that the
   // selection can be modified without upsetting the clipboard contents.

   void editCopy(Transferable t)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (clipboard == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      edit.setPageUniqueID(page.getUniqueID()) ;
      Transferable t2 = clipboard.getContents(this) ;
      PanelEdit edit2 = (t2 instanceof PanelEdit) ? (PanelEdit) t2 : null ;
      clipboard.setContents((PanelEdit) edit.clone(),this) ;

      // Update the page group position state.  This ensures that the
      // objects when pasted reflect their positions at the time of copy.

      page.saveState(config.getID(),"panelframe") ;
      page.restoreState(config.getID(),"panelframe") ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(COPY,edit,edit2,edit) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Show a diagnostic trace.

      if (OptionsDialog.getDebugEdit())
      {
         for (int i = 0 ; i < edit.size() ; i++)
            System.out.println("Edit: copy " + edit.elementAt(i) + " on page " + page) ;
      }

      // Update our menu state.

      parent.updateMenu() ;
   }


   // Method to paste the specified objects to the current page.  This takes
   // the objects from the selection set and adds them to the current page.
   // New group and cel objects will be created if pasted objects duplicate
   // objects currently on the page.  Objects will be visible if they were
   // visible when selected. A new selection set is created that refers to
   // the pasted objects.

   void editPaste(Transferable t) { editPaste(t,false,false) ; }
   void editPaste(Transferable t, boolean internal) { editPaste(t,internal,false) ; }
   void editPaste(Transferable t, boolean internal, boolean pastenew)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (cels == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      boolean marked = edit.isMarked() ;
      Object cid = config.getID() ;
      Object sourcepage = edit.getPageUniqueID() ;
      Object currentpage = page.getIdentifier() ;
      PageSet editpage = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,sourcepage) ;
      if (editpage == null) return ;
      sourcepage = editpage.getIdentifier() ;
      suspendEvents() ;

      // Initialize our new selection set.  Pasted cel objects are added
      // to any ungrouped selection.  Pasted group objects create a new
      // selection set.

      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      if (isUngrouped() && edit.size() > 0 && edit.elementAt(0) instanceof Cel)
      {
          newselection.addAll(selection) ;
          newselection.clearPasted() ;
      }

      // Add the selection objects to the current page set.  We must
      // ensure that only the cels that were visible on the source page
      // are visible on the current page.

      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         if (marked && !edit.isMarked(i)) continue ;
         if (OptionsDialog.getDebugEdit())
            System.out.println("Edit: paste begin for element " + kiss + " on page " + page) ;

         // Pastes of group objects.

         if (kiss instanceof Group)
         {
            // Ensure that the selection group still exists.  It may
            // have been deleted.

            Group g = (Group) kiss ;
            Integer gid = (Integer) g.getIdentifier() ;
            g = (Group) Group.getByKey(Group.getKeyTable(),cid,gid) ;
            if (g == null) continue ;

            // Construct a new group object if our current page already
            // contains the group or we are pasting a temporary group or
            // performing a pastenew.

            if ((pastenew || page.contains(g) || gid.intValue() < 0) && !OptionsDialog.getPagesAreScenes())
            {
               Vector groupcels = g.getCels() ;
               Point location = editpage.getGroupPosition(gid) ;
               if (location == null) location = kiss.getLocation() ;
               Integer groupnumber = new Integer(config.getMaxGroupNumber()+1) ;
               g = new Group(groupnumber) ;
               gid = groupnumber ;
               Vector groups = config.getGroups() ;
               groups.addElement(g) ;
               g.setInternal(true) ;
               g.setUpdated(true) ;
               g.setPasted(true) ;
               g.setIdentifier(groupnumber) ;
               g.setKey(g.getKeyTable(),config.getID(),groupnumber) ;
               if (OptionsDialog.getDebugEdit())
                  System.out.println("Edit: paste create " + g + " on page " + page) ;

               // Construct new cels for the group.  The only cels created
               // are those that exist on the selection source page.  Cels
               // are usually located at the parent group location, however
               // this may not be the case if we are creating a new group.

               for (int j = 0 ; j < groupcels.size() ; j++)
               {
                  Cel c = (Cel) groupcels.elementAt(j) ;
                  Point cellocation = c.getLocation() ;
                  int x = cellocation.x - location.x ;
                  int y = cellocation.y - location.y ;
                  Cel c1 = (Cel) c.clone() ;
                  Integer celnumber = new Integer(config.getMaxCelNumber()) ;
                  config.setMaxCelNumber(new Integer(config.getMaxCelNumber()+1)) ;
                  Vector p = c.getPages() ;
                  Vector pages = (p == null) ? null : new Vector() ;
                  pages.add(currentpage) ;
                  c1.setIdentifier(celnumber) ;
                  c1.setPages(pages) ;
                  c1.setLocation(new Point(x,y)) ;
                  c1.setOffset(new Point(c.getOffset())) ;
                  c1.setAdjustedOffset(c.getAdjustedOffsetPoint()) ;
                  c1.setPlacement(new Point(0,0)) ;
                  c1.setLevel(c.getLevel()) ;
                  c1.setInternal(false) ;
                  c1.setCopy((c.isInternal()) ? c.isCopy() : true) ;
                  if (c1 instanceof JavaCel) c1.setCopy(false) ;
                  c1.setAllPages(c.getUndoAllPages());
                  
                  // If we are performing a Paste New then we need to ensure
                  // that new cels are created. We append a number to our cel
                  // name. Note that JavaCels are automatically created with
                  // unique names when cloned.
                  
                  if (pastenew && !(c1 instanceof JavaCel))
                  {
                     String extension = "" ;
                     String celname = c1.getName() ;
                     int n = celname.lastIndexOf('.') ;
                     if (n > 0) extension = celname.substring(n) ;
                     if (n > 0) celname = celname.substring(0,n) ;
                     n = celname.lastIndexOf('-') ;
                     if (n > 0)
                     {
                        try
                        {
                           Integer.parseInt(celname.substring(n+1)) ;
                           celname = celname.substring(0,n) ;
                        }
                        catch (NumberFormatException e) { }
                     }
                     celname += "-" + celnumber + extension ;
                     c1.setRelativeName(null) ;
                     c1.setName(celname) ;
                     c1.setUpdated(true) ;
                     c1.setCopy(false) ;
                     
                     ArchiveEntry ze = c1.getZipEntry() ;
                     if (ze != null)
                     {
                        ze = (ArchiveEntry) ze.clone() ;
                        ze.setName(celname) ;
                        c1.setZipEntry(ze) ;
                     }
                  }

                  // Attach the new cel to the group.  Insert the cel in the
                  // cel list.  The cel is placed before the source cel to
                  // ensure the cel overlay level is of higher priority than
                  // any other copy of the source cel.  This ensures that it
                  // can be selected by the mouse after the paste.

                  c1.setKey(c1.getKeyTable(),config.getID(),c1.getIdentifier()) ;
                  c1.setKey(c1.getKeyTable(),config.getID(),c1.getPath().toUpperCase()) ;
                  if (c1.isImported()) 
                     c1.setKey(c1.getKeyTable(),config.getID(),"Import "+c1.getName().toUpperCase()) ;
                  cels.addElement(c1) ;
                  g.addCel(c1) ;
                  c1.saveState(cid,"initial") ;
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: paste create " + c1 + " on page " + page) ;
               }

               // Position the new group.  Correct the group level as the
               // cels added did not have their correct levels established.

               if (location == null) location = new Point() ;
               g.eliminateOffset() ;
               g.setInitialOffset() ;
               g.setContext((Integer) currentpage);
               g.setPlacement(location.x,location.y) ;
               g.drop() ;
            }

            // Ensure that all cels in the group with visibility on our
            // source page retain visibility on our current page.

            else
            {
               Vector cels = g.getCels() ;
               for (int j = 0 ; j < cels.size() ; j++)
               {
                  Cel c = (Cel) cels.elementAt(j) ;
                  Vector pages = c.getPages() ;
                  if (pages == null) continue ;
                  Vector newpages = (Vector) pages.clone() ;
                  newpages.remove(currentpage) ;
                  if (pages.contains(sourcepage)) newpages.addElement(currentpage) ;
                  c.setPages(newpages) ;
                  c.setInternal(false) ;
               }
            }

            // Add the group to the page.  It is positioned to the same
            // location as currently set on the selection set edit page.

            g.setContext((Integer) page.getIdentifier()) ;
            page.addGroup(g) ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: paste add group " + g + " on page " + page) ;
            newselection.addElement(g) ;
            Integer newgid = (Integer) g.getIdentifier() ;
            Point grouplocation = editpage.getGroupPosition(newgid) ;
            if (grouplocation == null) grouplocation = g.getLocation() ;
            page.addPosition(grouplocation,newgid.intValue()) ;

            // Compute the relative placement offset necessary to move the
            // group to the required position.

            Rectangle box = g.getBoundingBox() ;
            Point offset = g.getOffset() ;
            int x = (grouplocation.x - (box.x-offset.x)) ;
            int y = (grouplocation.y - (box.y-offset.y)) ;
            g.setPlacement(x,y) ;
            g.drop() ;

            // Update the page initial state.  Initial positions for the
            // pasted groups will become the initial positions from the
            // selection edit source page.  If no initial position exists
            // then the initial position becomes the current position.

            grouplocation = editpage.getInitialGroupPosition(gid) ;
            if (grouplocation == null) grouplocation = g.getLocation() ;
            page.setInitialGroupPosition(newgid,grouplocation) ;
            
            // However, if we are using pages as scenes and pasting something 
            // cut from page 0 onto a new page then the initial page positions
            // for the pasted group should be the initial page positions
            // from the source page, page 0.
            
            if (OptionsDialog.getPagesAreScenes() && sourcepage.equals(new Integer(0)))
            {
               grouplocation = editpage.getInitialGroupPosition(newgid) ;
               page.setInitialGroupPosition(newgid,grouplocation) ;
            }

            // As this group will be in use add it to the configuration
            // group list if it does not exist.

            Vector groups = config.getGroups() ;
            if (!groups.contains(g)) groups.addElement(g) ;
         }

         // Pastes of cel objects occur for ungrouped items.

         if (kiss instanceof Cel)
         {
            Cel c = (Cel) kiss ;
            Object id = c.getIdentifier() ;
            c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),id) ;
            if (c == null) continue ;
            if (c.isError()) continue ;

            // Cels that are pasted have this page set added to their page
            // list.  The parent group is added to the page if it does not
            // exist.  If the cel is on a page it is not internal.  If the
            // cel already exists on the page then we are pasting a new
            // copy and a new cel object is cloned.

            Integer pid = (Integer) page.getIdentifier() ;
            Vector v = c.getPages() ;
            if (!v.contains(pid) && !pastenew)
            {
               v.addElement(pid) ;
               if (OptionsDialog.getDebugEdit())
                  System.out.println("Edit: paste add " + c + " to page " + page) ;
            }
            else
            {
               Cel c1 = (Cel) c.clone() ;
               Integer celnumber = new Integer(config.getMaxCelNumber()) ;
               config.setMaxCelNumber(new Integer(config.getMaxCelNumber()+1)) ;
               Vector pages = new Vector() ;
               Integer level = c.getLevel() ;
               c1.setIdentifier(celnumber) ;
               c1.setPages(pages) ;
               c1.setLocation(new Point(c.getLocation())) ;
               c1.setOffset(new Point(c.getOffset())) ;
               c1.setAdjustedOffset(c.getAdjustedOffsetPoint()) ;
               c1.setPlacement(new Point(0,0)) ;
               c1.setInternal(false) ;
               c1.setCopy((c.isInternal()) ? c.isCopy() : true) ;
               if (c1 instanceof JavaCel) c1.setCopy(false) ;
                  
               // If we are performing a Paste New then we need to ensure
               // that new cels are created. We append a number to our cel
               // name. Note that JavaCels are automatically created with
               // unique names when cloned.
                  
               if (pastenew && !(c1 instanceof JavaCel))
               {
                  String extension = "" ;
                  String celname = c1.getName() ;
                  int n = celname.lastIndexOf('.') ;
                  if (n > 0) extension = celname.substring(n) ;
                  if (n > 0) celname = celname.substring(0,n) ;
                  n = celname.lastIndexOf('-') ;
                  if (n > 0)
                  {
                     try
                     {
                        Integer.parseInt(celname.substring(n+1)) ;
                        celname = celname.substring(0,n) ;
                     }
                     catch (NumberFormatException e) { }
                  }
                  celname += "-" + celnumber + extension ;
                  c1.setRelativeName(null) ;
                  c1.setName(celname) ;
                  c1.setUpdated(true) ;
                  c1.setCopy(false) ;
                     
                  ArchiveEntry ze = c1.getZipEntry() ;
                  if (ze != null)
                  {
                     ze = (ArchiveEntry) ze.clone() ;
                     ze.setName(celname) ;
                     c1.setZipEntry(ze) ;
                  }
               }
               
               c1.setKey(c1.getKeyTable(),config.getID(),c1.getIdentifier()) ;
               c1.setKey(c1.getKeyTable(),config.getID(),c1.getPath().toUpperCase()) ;
               if (c1.isImported()) 
                  c1.setKey(c1.getKeyTable(),config.getID(),"Import "+c1.getName().toUpperCase()) ;
               cels.addElement(c1) ;
               c1.setLevel(c.getLevel()) ;
               pages.addElement(pid) ;
               c1.saveState(cid,"initial") ;
               if (OptionsDialog.getDebugEdit())
                  System.out.println("Edit: paste create " + c1 + " on page " + page) ;
               if (marked) newselection.setMarked(c,false) ;
               c = c1 ;
            }

            // Add the cel to the new selection set if necessary. Mark the
            // cel as pasted so an edit cut only removes the newly pasted
            // entries.

            c.setInternal(false) ;
            c.setPasted(true) ;
            c.setAllPages(c.getUndoAllPages());
            if (!newselection.contains(c)) newselection.addElement(c) ;
            if (marked) newselection.setMarked(c,true) ;
            newselection.setPasted(c,true) ;
            Object o = c.getGroup() ;

            // Add the cel to the parent group and rebuild the group
            // bounding box.

            if (o instanceof Group)
            {
               Group g = (Group) o ;
               g.addCel(c) ;
               g.rebuildBoundingBox() ;
               g.eliminateOffset() ;
               g.setUpdated(true) ;
               g.setContext(pid) ;
               if (!page.contains(g))
               {
                  page.addGroup(g) ;
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: paste add group " + g + " on page " + page) ;

                  // Position the group.
                  // Compute the relative placement offset necessary to move the
                  // group to the required position.

                  Point grouplocation = g.getLocation() ;
                  Rectangle box = g.getBoundingBox() ;
                  Point offset = g.getOffset() ;
                  int x = (grouplocation.x - (box.x-offset.x)) ;
                  int y = (grouplocation.y - (box.y-offset.y)) ;
                  g.setPlacement(x,y) ;
                  g.drop() ;

                  // Update the page initial state.  Initial positions for the
                  // pasted groups will become the initial positions from the
                  // selection edit source page.  If no initial position exists
                  // then the initial position becomes the current position.

                  Integer gid = (Integer) g.getIdentifier() ;
                  grouplocation = editpage.getInitialGroupPosition(gid) ;
                  if (grouplocation == null) grouplocation = g.getLocation() ;
                  page.setInitialGroupPosition(gid,grouplocation) ;
            
                  // However, if we are using pages as scenes and pasting something 
                  // cut from page 0 onto a new page then the initial page positions
                  // for the pasted group should be the initial page positions
                  // from the source page, page 0.
            
                  if (OptionsDialog.getPagesAreScenes() && sourcepage.equals(new Integer(0)))
                  {
                     grouplocation = editpage.getInitialGroupPosition(gid) ;
                     page.setInitialGroupPosition(gid,grouplocation) ;
                  }

                  // Update the location of the group on the page.
         
                  if (kiss instanceof Group)
                     g.updatePageSetLocation(page) ;
               }
            }
         }
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         int type = (pastenew) ? PASTENEW : PASTE ;
         UndoablePageEdit ce = new UndoablePageEdit(type,edit,selection,newselection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // If we have a new selection create a new group set.  The group set is
      // the collection of selected objects that can be picked up and moved
      // as a complete unit during mouse events.

      groupset = null ;
      selection = null ;
      if (newselection.size() > 0)
      {
         groupset = new Group() ;
         selection = newselection ;
         for (int i = 0 ; i < selection.size() ; i++)
            groupset.addElement((KissObject) selection.elementAt(i),false) ;
      }

      // Create a new cel list for this page.

      int pageset = ((Integer) page.getIdentifier()).intValue() ;
      celList = createCelList(pageset) ;
      baseList = new int [celList.length] ;
      for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
      page.setChanged(true) ;
      config.setUpdated(true) ;

      // If this is not an internal paste, redraw the page.

      if (internal) return ;
      loadCels(pageset,false) ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      showpage() ;
   }


   // Method to paste new objects to the current page.  This takes
   // the objects from the selection set and adds them to the current page.
   // New group and cel objects are created and new cel names are defined. 

   void editPasteNew(Transferable t) { editPasteNew(t,false) ; }
   void editPasteNew(Transferable t, boolean internal)
   {
      editPaste(t,internal,true) ;
   }


   // Method to ungroup the specified objects in the selection set.  This
   // takes all group objects and replaces them with their component cels.

   void editUngroup(Transferable t)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (cels == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      suspendEvents() ;

      // Identify the groups in the selection set.

      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID());
      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         if (kiss instanceof Group)
         {
            Group g = (Group) kiss ;
            Vector groupcels = g.getCels() ;

            // Add all cels that are visible on the current page.

            for (int j = 0 ; j < groupcels.size() ; j++)
            {
               Cel c = (Cel) groupcels.elementAt(j) ;
               if (!c.isVisible()) continue ;
               if (c.isOnPage((Integer) page.getIdentifier()))
                  newselection.addElement(c) ;
            }
         }
      }

      // Create a new cel list for this page and redraw the page.
      // Ungrouped objects in the selection set are Cels.

      selection = newselection ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(UNGROUP,selection,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      parent.updateMenu() ;
      showpage() ;
   }


   // Method to group the specified objects in the selection set.  This
   // takes all selected cel objects and places them into a new group.

   void editGroup(Transferable t)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (cels == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      suspendEvents() ;

      // Identify the groups in the selection set.

      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         if (kiss instanceof Cel)
         {
            Cel c = (Cel) kiss ;
            Object o = c.getGroup() ;
            if ((o instanceof Group) && (!newselection.contains(o)))
               newselection.addElement(o) ;
         }
      }
      
      // Rebuild all the group bounding boxes for each group.
      
      for (int i = 0 ; i < newselection.size() ; i++)
      {
         KissObject kiss = (KissObject) newselection.elementAt(i) ;
         if (kiss instanceof Group)
            ((Group) kiss).rebuildBoundingBox() ;
      }

      // Create a new cel list for this page and redraw the page.

      selection = newselection ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(GROUP,selection,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      parent.updateMenu() ;
      showpage() ;
   }


   // Method to group the specified objects in the selection set into one
   // new group object.  This takes all selected cel objects and places them
   // into a new group.

   void editNewgroup(Transferable t)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (cels == null) return ;
      if (!(t instanceof PanelEdit)) return ;
      PanelEdit edit = (PanelEdit) t ;
      suspendEvents() ;

      // Cut the current selection set from the page.  Retain the
      // current draw order as we need this when cels are regrouped.

      PanelEdit oldselection = selection ;
      Integer pid = (Integer) page.getIdentifier() ;
      int [] order = createCelList(pid.intValue()) ;
      boolean b = undoredo ;
      undoredo = true ;
      editCut(edit,true) ;
      undoredo = b ;

      // Create a new group object.  This is a temporary group used to
      // establish a pattern for the actual new group created during a paste.

      Integer gid = new Integer(-1) ;
      Group newgroup = new Group() ;
      newgroup.setInternal(true) ;
      newgroup.setUpdated(true) ;
      newgroup.setIdentifier(gid) ;
      newgroup.setKey(newgroup.getKeyTable(),config.getID(),gid) ;

      // Identify the current selection set bounding box and position
      // the group to this location.

      Rectangle box = null ;
      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         if (box == null) box = kiss.getBoundingBox() ;
         else box = box.union(kiss.getBoundingBox()) ;
      }
      newgroup.setPlacement(box.x,box.y) ;
      newgroup.drop() ;

      // Add all selected cels to the new group. 

      for (int i = 0 ; i < edit.size() ; i++)
      {
         KissObject kiss = (KissObject) edit.elementAt(i) ;
         newgroup.addElement(kiss) ;
      }
      
      // Order the cels as they appear in the current cel list.
      // This ensures that the new group cels maintain the same
      // relative draw sequence if cels have identical draw levels.

      Vector groupcels = newgroup.getCels() ;
      Collections.sort(groupcels,new CelListComparator(order)) ;
         
      // Relocate the group cels so that the adjusted offset is
      // relative to the current group.
         
      relocateGroupCels(newgroup) ;
      newgroup.rebuildBoundingBox() ;

      // Create a new selection for this new group.

      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      newselection.add(newgroup) ;

      // Paste the new selection into the page.

      b = undoredo ;
      undoredo = true ;
      editPaste(newselection,true) ;
      undoredo = b ;

      // Remove the temporary group registration.

      newgroup.removeKey(newgroup.getKeyTable(),config.getID(),gid) ;
      if (selection != null) selection.remove(newgroup) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(NEWGROUP,page,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      config.setUpdated(true) ;
      parent.updateMenu() ;
      showpage() ;
   }


   // Method to select all groups on the current page.

   void selectAll() { selectAll(null) ; }
   void selectAll(Vector v)
   {
      if (page == null) return ;
      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      if (v == null) newselection.addAll(page.getGroups()) ;
      else newselection.addAll(v) ;
      if (newselection.size() == 0) return ;
      selection = newselection ;

      // If we have a selection create a new group set.

      groupset = new Group() ;
      for (int i = 0 ; i < selection.size() ; i++)
         groupset.addElement((KissObject) selection.elementAt(i),false) ;

      // Disable input on selected components.

      setInputState(true,oldselection) ;
      setInputState(false,newselection) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(SELECT,selection,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      OptionsDialog.setTempEditEnable(true) ;
      parent.updateMenu() ;
      repaint() ;
   }


   // Method to select all visible cels on the current page.  We return only
   // the cels necessary to fully cover the screen.  

   void selectAllVisible() { selectAllVisible(null) ; }
   void selectAllVisible(Vector v)
   {
      if (page == null) return ;
      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      newselection.setPageUniqueID(page.getUniqueID()) ;
      if (v == null) newselection.addAll(page.getVisibleCels()) ;
      else newselection.addAll(v) ;
      if (newselection.size() == 0) return ;
      selection = newselection ;

      // If we have a selection create a new group set.

      groupset = new Group() ;
      Collections.sort(selection,new LevelComparator()) ;
      Rectangle r = new Rectangle() ;
      for (int i = 0 ; i < selection.size() ; i++)
      {
         Cel c = (Cel) selection.elementAt(i) ;
         groupset.addElement(c,false) ;
         r = r.union(c.getBoundingBox()) ;
         if (r.x <= 0 && r.y <= 0 && r.width >= panelSize.width && r.height >= panelSize.height)
            break ;
      }

      // Disable input on selected components.

      setInputState(true,oldselection) ;
      setInputState(false,newselection) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(SELECT,selection,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      OptionsDialog.setTempEditEnable(true) ;
      parent.updateMenu() ;
      repaint() ;
   }

   
   // Method to unselect all objects currently selected.

   void unselectAll()
   {
      if (page == null) return ;
      PanelEdit oldselection = selection ;
      selection = null ;
      groupset = null ;

      // Enable input on previously selected components.

      setInputState(true,oldselection) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(SELECT,selection,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      parent.updateMenu() ;
      parent.showStatus(null) ;
      repaint() ;
   }

   
   // Method to unselect a specific set of objects currently selected.

   void unselectSet(PanelEdit selectset)
   {
      if (selection == null) return ;
      PanelEdit oldselection = (PanelEdit) selection.clone() ;
      for (int i = 0 ; i < selectset.size() ; i++)
      {
         Object o = selectset.elementAt(i) ;
         if (!(o instanceof KissObject)) continue ;
         KissObject kiss = (KissObject) o ;
         if (groupset != null) groupset.removeElement(kiss) ;
         selection.remove(kiss) ;
      }
      if (groupset != null && groupset.getCelCount() == 0) groupset = null ;
      if (selection.size() == 0) selection = null ;

      // Enable input on previously selected components.

      setInputState(true,selectset) ;

      // Capture this edit for undo/redo processing.

      if (page != null && selection != null)
         selection.setPageUniqueID(page.getUniqueID()) ;
      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(SELECTSET,selectset,oldselection,selection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Update our menu state.

      parent.updateMenu() ;
      parent.showStatus(null) ;
      repaint() ;
   }



   // Method to create a new PageSet object with the specified page number.
   // This page is inserted into the page list with a default multipalette
   // of zero and no objects. The new page number is returned.

   int insertPage() { return insertPage(null,false) ; }
   int insertPage(boolean b) { return insertPage(null,b) ; }
   int insertPage(Integer p) { return insertPage(p,false) ; }
   int insertPage(Integer p, boolean internal)
   {
      if (config == null) return -1 ;
      Object cid = config.getID() ;
      Vector cels = config.getCels() ;
      Vector pages = config.getPages() ;
      int pagenumber = (p == null) ? pages.size() : p.intValue() ;
      if (pagenumber > pages.size()) return -1 ;
      PageSet newpage = new PageSet() ;
      newpage.setID(cid) ;
      newpage.setIdentifier(new Integer(pagenumber)) ;

      // All pages in the data set including this new page must be
      // removed from the PageSet key table.

      for (int i = pagenumber ; i < pages.size() ; i++)
      {
         PageSet page = (PageSet) pages.elementAt(i) ;
         PageSet.removeKey(PageSet.getKeyTable(),cid,page.getIdentifier()) ;
      }

      // All pages in the data set after this new page must have their
      // page association numbers adjusted.  All set() events associated
      // with the adjusted pages need to have their set number parameter 
      // adjusted.

      for (int i = 0 ; i < pages.size() ; i++)
      {
         PageSet page = (PageSet) pages.elementAt(i) ;
         Object o = page.getIdentifier() ;
         if (!(o instanceof Integer)) continue ;
         int id = ((Integer) o).intValue() ;
         if (id < pagenumber) continue ;
         page.setIdentifier(new Integer(id+1)) ;
         page.setKey(page.getKeyTable(),cid,page.getIdentifier()) ;
         Vector v = page.getEvent("set") ;
         if (v == null) continue ;
         
         // Adjust all page set() events
         
         for (int j = 0 ; j < v.size() ; j++)
         {
            FKissEvent event = (FKissEvent) v.elementAt(j) ;
            String s = event.getFirstParameter() ;
            try 
            { 
               int n = Integer.parseInt(s) ;
               if (n < pagenumber) continue ;
               event.setFirstParameter("" + (n+1)) ;
            }
            catch (NumberFormatException e) { }
         }
      }

      // All cels in the data set after this new page must have their page
      // association numbers adjusted.

      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel cel = (Cel) cels.elementAt(i) ;
         Vector v = cel.getPages() ;
         if (v == null) continue ;
         for (int j = 0 ; j < v.size() ; j++)
         {
            int id = ((Integer) v.elementAt(j)).intValue() ;
            if (id < pagenumber) continue ;
            v.setElementAt(new Integer(id+1),j) ;
         }
      }

      // Insert the new page.  Note that the page vector is kept ordered
      // by ascending page set number.  There is an implicit relation
      // between the pages index and the page set identifier.

      newpage.setKey(newpage.getKeyTable(),cid,newpage.getIdentifier()) ;
      newpage.setKey(newpage.getKeyTable(),cid,newpage.getUniqueID()) ;
      pages.insertElementAt(newpage,pagenumber) ;
      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: insert page " + newpage) ;

      // Capture this edit for undo/redo processing.

      if (!internal && !undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(ADDPAGE,page,pagenumber,null,null,null) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Return the new page number.

      config.setUpdated(true) ;
      return pagenumber ;
   }



   // Method to delete the PageSet object with the specified page number.
   // This page is removed from the page list.  The deleted page number is
   // returned.

   int deletePage() { return deletePage(null) ; }
   int deletePage(Integer p)
   {
      if (config == null) return -1 ;
      Object cid = config.getID() ;
      Vector cels = config.getCels() ;
      Vector pages = config.getPages() ;
      Vector groups = config.getGroups() ;
      int pagenumber = (p == null) ? pages.size() - 1 : p.intValue() ;
      if (pagenumber < 0 || pagenumber >= pages.size()) return -1 ;

      // Capture the current state of the deleted page.

      PageSet deletepage = (PageSet) pages.elementAt(pagenumber) ;
      deletepage.saveState(cid,"panelframe") ;
      deletepage.restoreState(cid,"panelframe") ;
      Integer multipalette = deletepage.getMultiPalette() ;
      Vector positions = deletepage.getPositions() ;
      Vector initpositions = deletepage.getInitialPositions() ;

      // All remaining pages in the data set including this deleted page must
      // be removed from the PageSet key table.

      for (int i = pagenumber ; i < pages.size() ; i++)
      {
         PageSet page = (PageSet) pages.elementAt(i) ;
         PageSet.removeKey(PageSet.getKeyTable(),cid,page.getIdentifier()) ;
      }

      // All pages in the data set after this deleted page must have their
      // page association numbers adjusted.

      for (int i = 0 ; i < pages.size() ; i++)
      {
         PageSet page = (PageSet) pages.elementAt(i) ;
         Object o = page.getIdentifier() ;
         if (!(o instanceof Integer)) continue ;
         int id = ((Integer) o).intValue() ;
         if (id <= pagenumber) continue ;
         page.setIdentifier(new Integer(id-1)) ;
         page.setKey(page.getKeyTable(),cid,page.getIdentifier()) ;
         Vector v = page.getEvent("set") ;
         if (v == null) continue ;
         
         // Adjust all page set() events
         
         for (int j = 0 ; j < v.size() ; j++)
         {
            FKissEvent event = (FKissEvent) v.elementAt(j) ;
            String s = event.getFirstParameter() ;
            try 
            { 
               int n = Integer.parseInt(s) ;
               if (n <= pagenumber) continue ;
               event.setFirstParameter("" + (n-1)) ;
            }
            catch (NumberFormatException e) { }
         }
      }

      // All cels in the data set after this deleted page must have their page
      // number associations adjusted.  We retain a list of cels previously
      // attached to the deleted page so that they can be restored in the
      // event that the page deletion is undone.

      PanelEdit deleteset = new PanelEdit() ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel cel = (Cel) cels.elementAt(i) ;
         Vector v = cel.getPages() ;
         if (v == null) continue ;
         Vector celpages = new Vector() ;

         // Construct the new cel page list and create the list of deleted
         // cel identifiers for all cels that are on this page.

         for (int j = 0 ; j < v.size() ; j++)
         {
            int id = ((Integer) v.elementAt(j)).intValue() ;
            if (id < pagenumber)
               celpages.addElement(v.elementAt(j)) ;
            else if (id == pagenumber)
            {
               Object o = cel.getGroup() ;
               if (o instanceof Group && deletepage.contains((Group) o))
                  deleteset.addElement(cel.getIdentifier()) ;
            }
            else
               celpages.addElement(new Integer(id-1)) ;
         }
         cel.setPages(celpages) ;
      }

      // Delete the page.  Note that the page vector is ordered by
      // ascending page set number.   There is an implicit relation
      // between the pages index and the page set identifier.

      pages.removeElementAt(pagenumber) ;
      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: delete page " + deletepage) ;

      // All groups on the deleted page must be examined.  If the group is
      // internal then it was created through a paste operation.  We must
      // determine if the group is active on any other page.

      boolean groupinuse = false ;
      Vector pagegroups = deletepage.getGroups() ;
      for (int i = 0 ; i < pagegroups.size() ; i++)
      {
         Group g = (Group) pagegroups.elementAt(i) ;
         if (!g.isInternal())
         {
            for (int j = 0 ; j < pages.size() ; j++)
            {
               PageSet ps = (PageSet) pages.elementAt(j) ;
               if (!ps.contains(g)) continue ;
               groupinuse = true ;
               break ;
            }
         }

         // We must stop any video cels playing in the deleted groups.
         // Furthermore, all cels that have been created through a paste
         // operation must be marked as internal cels if the group they
         // are associated with is no longer active.  This flags the
         // cels and they will not be written to the configuration file
         // if the configuration is saved.

         Vector groupcels = g.getCels() ;
         for (int j = 0 ; j < groupcels.size() ; j++)
         {
            Object o = groupcels.elementAt(j) ;
            if (!(o instanceof Cel)) continue ;
            Cel c = (Cel) o ;
            if (!groupinuse) c.setInternal(true) ;
            if (!(o instanceof Video)) continue ;
            Video video = (Video) o ;
            video.stop(video) ;
         }
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(REMOVEPAGE,
            page,pagenumber,deleteset,positions,initpositions,multipalette) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Return the new page number.  If we have deleted the current page
      // on display then a new page initialization is required.

      int n = (page != null) ? ((Integer) page.getIdentifier()).intValue() : -1 ;
      if (n == pagenumber) page = null ;
      n = config.getPageCount() ;
      if (pagenumber >= n) pagenumber = n - 1 ;
      config.setUpdated(true) ;
      return pagenumber ;
   }



   // Method to update the PageSet initial object positions for the specified
   // page.  The page number is returned.

   int writePage(Integer p)
   {
      if (p == null) return -1 ;
      if (config == null) return -1 ;
      Object cid = config.getID() ;
      Vector pages = config.getPages() ;
      int pagenumber = p.intValue() ;
      if (pagenumber < 0 || pagenumber >= pages.size()) return -1 ;

      // Capture the current state of the page.

      PageSet writepage = (PageSet) pages.elementAt(pagenumber) ;
      if (writepage == null) return -1 ;
      writepage.saveState(cid,"panelframe") ;
      writepage.restoreState(cid,"panelframe") ;
      State newstate = writepage.getState(cid,"panelframe") ;
      State oldstate = writepage.getState(cid,"initial") ;

      // Update the page initial state.

      writepage.setState(cid,"initial",newstate) ;
      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: write page " + writepage) ;

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(WRITEPAGE,writepage,oldstate,newstate) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Return the page number.

      writepage.setChanged(false) ;
      config.setUpdated(true) ;
      return pagenumber ;
   }


   // Method to create an undoable color edit from the ColorFrame editor.
   // This method is invoked for palette changes which may have image changes.
   // This method allows updates from the color editor to be undone.

   void createColorEdit(Object palette, Palette priorpalette, Object [] olddata, int oldmp,
      int oldcolors, int oldbackground, int oldtransparent, Object [] newdata,
      int newmp, int newcolors, int newbackground, int newtransparent,
      Cel cel, Cel priorcel, int transparent, int loop, Image img, ColorModel cm)
   {
      Image newimg = (cel != null) ? cel.getImage() : null ;
      ColorModel newcm = (cel != null) ? cel.getColorModel() : null ;
      UndoableColorEdit ce = new UndoableColorEdit(palette, priorpalette,
         olddata,oldmp,oldcolors,oldbackground,oldtransparent,
         newdata,newmp,newcolors,newbackground,newtransparent,
         cel,newimg,newcm,img,cm) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      
      // If we changed the background then update the border color.
      
      if (config != null)
      {
         setBackground(config.getBorderColor()) ;
         parent.setBackground(config.getBorderColor()) ;
      }

      // Redraw the page.

      Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
      initcolor(multipalette) ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      showpage() ;
   }


   // Method to create an undoable color edit from the ColorFrame editor.
   // This method is for truecolor cel transparency changes.

   void createColorEdit(Object editobject, int oldtransparent, int newtransparent)
   {
      UndoableColorEdit ce = new UndoableColorEdit(editobject,oldtransparent,newtransparent) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable image edit from the ImageFrame editor.
   // This method is for cel transparency changes and color changes and
   // dithering of truecolor images to palette images.

   void createImageEdit(Object editobject, int oldtransparent, int loop, Image img, Palette p)
   {
      UndoableImageEdit ce = new UndoableImageEdit(editobject,oldtransparent,loop,img,p) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
      
      // If we dithered to a KCF palette then we need to add the palette into
      // the configuration.  This will be removed on an undo.
      
      if (p != null) return ;
      if (config == null) return ;
      if (!(editobject instanceof Cel)) return ;
      Cel c = (Cel) editobject ;
      p = c.getPalette() ;
      if (p == null) return ;
      if (!ArchiveFile.isPalette(p.getName())) return ;
      Vector v = config.getPalettes() ;
      
      // Set the palette file keys and add it to the configuration.
      
      if (v != null) 
      {
         int n = v.size() ;
         v.addElement(p) ;
         Object pid = new Integer(n) ;
         c.setInitPaletteID(pid) ;
         c.setPaletteID(pid) ;
         p.setIdentifier(pid) ;
   		p.setKey(p.getKeyTable(),config.getID(),pid) ;
         String name = p.getRelativeName() ;
         if (name != null) p.setKey(p.getKeyTable(),config.getID(),name.toUpperCase()) ; 
         if (config.getPaletteCount() == 1)
         {
            setBackground(config.getBorderColor()) ;
            parent.setBackground(config.getBorderColor()) ;
         }
     }

      // Redraw the page.

      Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
      initcolor(multipalette) ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      showpage() ;
   }


   // Method to create an undoable edit for page set attribute changes.
   // This method is invoked from the pageset properties dialog.

   void createPageSetEdit(PageSet p, Integer oldmp, Integer newmp)
   {
      UndoablePageSetEdit ce = new UndoablePageSetEdit(p,oldmp,newmp) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable edit for playfield size changes.
   // This method is invoked from the configuration properties dialog.

   void createSizeChangeEdit(Dimension d1, Dimension d2)
   {
      UndoableSizeEdit ce = new UndoableSizeEdit(SIZE,d1,d2) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable edit for component attribute changes.
   // This method is invoked from the attribute dialog.

   void createAttributeEdit(Cel c, String oldattr, String newattr)
   {
      UndoableSizeEdit ce = new UndoableSizeEdit(ATTRIBUTES,c,oldattr,newattr) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable edit for transparency changes.
   // This method is invoked from the properties dialog.

   void createTransparencyEdit(KissObject o, int ot, int nt)
   {
      UndoableTransparencyEdit ce = new UndoableTransparencyEdit(o,ot,nt) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable edit for visibility changes.
   // This method is invoked from the properties dialog.

   void createVisibilityEdit(KissObject o, boolean ot, boolean nt)
   {
      UndoableVisibilityEdit ce = new UndoableVisibilityEdit(o,ot,nt) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to create an undoable edit for visibility changes.
   // This method is invoked from the properties dialog.

   void createGhostEdit(KissObject o, boolean ot, boolean nt)
   {
      UndoableGhostEdit ce = new UndoableGhostEdit(o,ot,nt) ;
      UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
      if (undo != null) undo.undoableEditHappened(evt) ;
      parent.updateMenu() ;
   }


   // Method to import a new cel image into the current page.   This method
   // creates an internal group for the cel and adds the new group and cel
   // to the active configuration.

   void importImage(Cel cel, boolean newgroup)
   {
      if (cel == null) return ;
      if (page == null) return ;
      if (config == null) return ;
      if (cels == null) return ;

      // Determine the group for this cel.  If we have a cel selection set
      // then the new imported cel will become a member of this cel selection
      // set and the selected group.  If we do not have a cel selection set
      // then a new group and selection set must be created for the imported
      // cel.

      Group g = null ;
      Object cid = config.getID() ;
      Object pageuniqueid = page.getUniqueID() ;
      ArchiveFile zip = config.getZipFile() ;
      PanelEdit edit = new PanelEdit() ;
      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      edit.setPageUniqueID(pageuniqueid) ;
      newselection.setPageUniqueID(pageuniqueid) ;

      // Add to the selection set if we are on the same
      // page and there is a current selection set.

      if (!newgroup)
      {
         if (selection != null && selection.size() > 0)
         {
            if (selection.getPageUniqueID() == pageuniqueid)
            {
               Object o = selection.firstElement() ;
               if (o instanceof Cel)
               {
                  newselection = (PanelEdit) selection.clone() ;
                  newselection.add(cel) ;
                  g = (Group) ((Cel) o).getGroup() ;
                  edit.addElement(cel) ;
               }
            }
         }
      }
      else if (selection != null && selection.getPageUniqueID() == pageuniqueid)
         newselection = (PanelEdit) selection.clone() ;

      // Create a new group object if necessary.

      if (g == null)
      {
         Integer groupnumber = new Integer(config.getMaxGroupNumber()+1) ;
         g = new Group(groupnumber) ;
         groups.addElement(g) ;
         g.setInternal(true) ;
         g.setUpdated(true) ;
         g.setIdentifier(groupnumber) ;
         g.setKey(g.getKeyTable(),cid,groupnumber) ;
         edit.addElement(g) ;
         newselection.add((newgroup) ? (Object) g : (Object) cel) ;
         newgroup = true ;
         showStatus("Object group " + g + " created for image cel " + cel) ;
         if (OptionsDialog.getDebugEdit())
            System.out.println("Edit: import create " + g) ;
      }

      // Add the cel to the configuration. The cel is placed at the front
      // of the cel list. We set keys based upon the path name (normal) and
      // the file name so that if we reload a new unsaved configuration we
      // can still find the imported cels.

      Integer celnumber = new Integer(config.getMaxCelNumber()) ;
      config.setMaxCelNumber(new Integer(config.getMaxCelNumber()+1)) ;
      Vector pages = new Vector() ;
      Vector movies = config.getMovies() ;
      pages.add(page.getIdentifier()) ;
      zip.addEntry(cel.getZipEntry()) ;
      cel.setID(cid) ;
      cel.setPages(pages) ;
      cel.setIdentifier(celnumber) ;
      cel.setLevel(new Integer(0));
      cel.setLocation(new Point(0,0)) ;
      cel.setPlacement(new Point(0,0)) ;
      cel.setInitTransparency(255) ;
      cel.setInternal(false) ;
      cel.setUpdated(true) ;
      cel.setKey(cel.getKeyTable(),cid,cel.getIdentifier()) ;
      cel.setKey(cel.getKeyTable(),cid,cel.getPath().toUpperCase()) ;
      cel.setKey(cel.getKeyTable(),cid,"Import "+cel.getName().toUpperCase()) ;
      cels.addElement(cel) ;
      if (cel instanceof Video && movies != null) movies.addElement(cel) ;
      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: import create " + cel + " on page " + page) ;

      // Add the new cel palette to the configuration if it does not exist.
      // If the cel palette duplicates a loaded palette in the configuration,
      // use the configuration palette and remove the imported palette from
      // the edit selection set. Set the configuration palette data to the
      // imported palette data.

      boolean paletteadded = false ;
      Palette p = cel.getPalette() ;
      if (p != null && !p.isInternal())
      {
         Vector palettes = config.getPalettes() ;
         String name = p.getName().toUpperCase() ;
         Palette p2 = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,name) ;
         if (p2 != null) 
         {
            Object [] o = p.getPaletteData() ;
            p2.setPaletteData(o) ;
            int n = p.getColorCount() ;
            p2.setColorCount(n) ;
            n = p.getBackgroundIndex() ;
            p2.setBackgroundIndex(n) ;
            n = p.getTransparentIndex() ;
            p2.setTransparentIndex(n) ;
            p2.setUpdated(true) ;
            p = p2 ;
         }
         else
         {
            zip.addEntry(p.getZipEntry()) ;
            p.setID(cid) ;
            p.setUpdated(true) ;
            p.setIdentifier(new Integer(palettes.size())) ;
            p.setKey(p.getKeyTable(),cid,p.getIdentifier()) ;
            p.setKey(p.getKeyTable(),cid,p.getPath().toUpperCase()) ;
            p.setKey(p.getKeyTable(),cid,p.getName().toUpperCase()) ;
            p.setKey(p.getKeyTable(),cid,"Import "+p.getName().toUpperCase()) ;
            edit.addElement(p) ;
            newselection.add(p) ;
            palettes.addElement(p) ;
            paletteadded = true ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: import create " + p) ;
         }
         cel.setPaletteID(p.getIdentifier()) ;
         cel.setInitPaletteID(p.getIdentifier()) ;
      }

      // Add the cel as the first cel in the group.

      g.addCel(cel,true) ;
      g.setContext((Integer) page.getIdentifier()) ;
      g.rebuildBoundingBox() ;
      if (!newgroup)
      {
         showStatus("Image cel " + cel + " added to object group " + g) ;
         if (OptionsDialog.getDebugEdit())
            System.out.println("Edit: import add " + cel + " to group " + g) ;
      }

      // Attach the group to the page.

      if (!page.contains(g))
      {
         page.addGroup(g) ;
         Integer newgid = (Integer) g.getIdentifier() ;
         Point grouplocation = g.getLocation() ;
         page.addPosition(grouplocation,newgid.intValue()) ;

         // Compute the relative placement offset necessary to move the
         // group to the required position.

         Rectangle box = g.getBoundingBox() ;
         Point offset = g.getOffset() ;
         int x = (grouplocation.x - (box.x-offset.x)) ;
         int y = (grouplocation.y - (box.y-offset.y)) ;
         g.setPlacement(x,y) ;
         g.drop() ;
         page.setInitialGroupPosition(newgid,grouplocation) ;
         if (OptionsDialog.getDebugEdit())
            System.out.println("Edit: import add " + g + " to page " + page) ;
      }

      // Add this group to any existing group set.

      if (groupset == null) groupset = new Group() ;
      groupset.addElement(g,false) ;
      selection = newselection ;

      // Ensure that cel offsets are adjusted for the group.

      if (!newgroup) relocateCel(cel) ;

      // Disable input on selected components.

      setInputState(false,newselection) ;

      // Capture this edit for undo/redo processing.  Undo of an image 
      // import cuts the new group from the page.  Redo of an import 
      // pastes the group back into the page.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(IMPORT,edit,oldselection,newselection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Create a new cel list for this page.

      int pageset = ((Integer) page.getIdentifier()).intValue() ;
      celList = createCelList(pageset) ;
      baseList = new int [celList.length] ;
      for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
      
      // Adjust the background color if we added the first palette.
      
      if (config.getPaletteCount() == 1 && paletteadded)
      {
         Palette p0 = config.getPalette(0) ;
         if (p0 != null) config.setImportBorderIndex(p0.getBackgroundIndex()) ; 
         parent.setBackground(config.getBorderColor()) ;
         setBackground(config.getBorderColor()) ;
         initcolor(new Integer(0)) ;
      }
      
      // Redraw the page.
      
      OptionsDialog.setTempEditEnable(true) ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      page.setChanged(true) ;
      config.setUpdated(true) ;
      showpage() ;
  }


   // Method to import a new palette into the current configuration.   

   void importPalette(Palette p)
   {
      if (config == null) return ;
      if (page == null) return ;
      boolean paletteadded = false ;
      Object cid = config.getID() ;
      Object pageuniqueid = page.getUniqueID() ;
      ArchiveFile zip = config.getZipFile() ;
      PanelEdit edit = new PanelEdit() ;
      PanelEdit oldselection = selection ;
      PanelEdit newselection = new PanelEdit() ;
      edit.setPageUniqueID(pageuniqueid) ;
      newselection.setPageUniqueID(pageuniqueid) ;

      // Add the new palette to the configuration if it does not exist.
      // If the cel palette duplicates a loaded palette in the configuration,
      // use the configuration palette.

      if (p != null && !p.isInternal())
      {
         Vector palettes = config.getPalettes() ;
         String name = p.getPath().toUpperCase() ;
         Palette p2 = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,name) ;
         if (p2 != null) p = p2 ;
         else
         {
            zip.addEntry(p.getZipEntry()) ;
            p.setID(cid) ;
            p.setUpdated(true) ;
            p.setIdentifier(new Integer(palettes.size())) ;
            p.setKey(p.getKeyTable(),cid,p.getIdentifier()) ;
            p.setKey(p.getKeyTable(),cid,p.getPath().toUpperCase()) ;
            p.setKey(p.getKeyTable(),cid,p.getName().toUpperCase()) ;
            p.setKey(p.getKeyTable(),cid,"Import "+p.getName().toUpperCase()) ;
            edit.addElement(p) ;
            palettes.addElement(p) ;
            paletteadded = true ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: import create " + p) ;
         }
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoablePageEdit ce = new UndoablePageEdit(IMPORTPALETTE,edit,oldselection,newselection) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Redraw the page.

      parent.updateMenu() ;
      parent.updateToolBar() ;
      config.setUpdated(true) ;
      if (config.getPaletteCount() == 1 && paletteadded)
      {
         setBackground(config.getBorderColor()) ;
         parent.setBackground(config.getBorderColor()) ;
         initcolor(new Integer(0)) ;
      }
      showpage() ;
   }


   // Method to import a new audio file into the current configuration. 
   // This import cannot be undone.

   void importAudio(Audio a)
   {
      if (config == null) return ;
      Object cid = config.getID() ;
      ArchiveFile zip = config.getZipFile() ;

      // Add the new audio file to the configuration if it does not exist.
      // If the audio file duplicates a loaded file in the configuration,
      // ignore this import.

      if (a != null && !a.isInternal())
      {
         Vector sounds = config.getSounds() ;
         String name = a.getPath().toUpperCase() ;
         Audio a2 = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,name) ;
         if (a2 == null) 
         {
            zip.addEntry(a.getZipEntry()) ;
            a.setID(cid) ;
            a.setUpdated(true) ;
            a.setIdentifier(new Integer(sounds.size())) ;
            a.setKey(a.getKeyTable(),cid,a.getPath().toUpperCase()) ;
            a.setKey(a.getKeyTable(),cid,a.getName().toUpperCase()) ;
            a.setKey(a.getKeyTable(),cid,"Import "+a.getName().toUpperCase()) ;
            sounds.addElement(a) ;
            if (OptionsDialog.getDebugEdit())
               System.out.println("Edit: import create " + a) ;
            JOptionPane.showMessageDialog(parent,a + " imported.") ;
         }
         else
            JOptionPane.showMessageDialog(parent,a + " already exists.") ;
      }
   }


   // Method to import an arbitrary file set into the current configuration.
   // This simply copies the file to the configuration archive.  This
   // import cannot be undone.

   void importOther(Vector contents, ArchiveFile zip)
   {
      if (config == null) return ;
      if (page == null) return ;
      Object cid = config.getID() ;
      
      // Use our FileWriter to copy the files.

		FileWriter w = new FileWriter(parent,zip,contents,true) ;
		Thread thread = new Thread(w) ;
      thread.start() ;
   }


   // Method to apply a cel layer update to the current cel list.   
   // This method accepts an initial and final list of object arrays 
   // that identify the cel and the requested layering level. The state
   // vectors contain two element object arrays which identify the cel
   // identifier and the integer layer number.

   void adjustLayer(Vector initialstate, Vector finalstate)
   {
      if (page == null) return ;
      if (config == null) return ;

      // Apply the final state level order to the cel list.

      for (int i = 0 ; i < finalstate.size() ; i++)
      {
         int n = 0 ;
         Object [] layeredit = (Object []) finalstate.elementAt(i) ;
         Object celID = layeredit[0] ;
         if (celID == null) continue ;
         try { n = Integer.parseInt(celID.toString()); }
         catch (NumberFormatException e) { continue ; }
         if (n < 0 || n > cels.size()) continue ;
         Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),new Integer(n)) ;
         if (c == null) continue ;
         c.setLevel((Integer) layeredit[1]) ;

         // Check for leading Cel Section comments.  If the first cel was
         // replaced in the order, a later cel can retain the UltraKiss
         // section heading.  This must be removed.

         Vector v = c.getLeadComment() ;
         if (v == null) continue ;
         n = v.indexOf(";[Cel Section]") ;
         if (n < 1) continue ;
         v.removeElementAt(n) ;
         Object o = v.elementAt(n-1) ;
         if (!" ".equals(o.toString())) continue ;
         v.removeElementAt(n-1) ;
      }

      // Capture this edit for undo/redo processing.  Undo of a level
      // change resets the original cel levels.

      if (!undoredo && initialstate != null)
      {
         UndoableLayerEdit ce = new UndoableLayerEdit(page,initialstate,finalstate) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Create a new celList for the current page and redraw the page.
      // All groups on the current page must have their context updated
      // as cel levels have changed.

      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: " + page + " image cel layering adjusted") ;
      page.setContext() ;
      Integer pid = (Integer) page.getIdentifier() ;
      if (groupset != null) groupset.setContext(pid) ;
      updateCelList() ;
      parent.updateMenu() ;
      parent.updateToolBar() ;
      showpage() ;
   }


   // Method to move selected objects forward or backward in the layering 
   // order. This method is activated when we are in edit mode and press the 
   // plus key. We construct layer adjustment lists and apply a layer
   // adjust undoable edit.

   void moveLevel(Vector selected, int direction)
   {
      if (page == null) return ;
      if (config == null) return ;
      if (selected == null) return ;
      
      Vector initialstate = new Vector() ;
      Vector finalstate = new Vector() ;

      // Invert the configuration cel list so that lower index duplicate level
      // cels appear at the top of the working list.

      Vector sortedcels = new Vector() ;
      Vector cels = (Vector) config.getCels() ;
      for (int i = cels.size()-1 ; i >= 0 ; i--)
      	sortedcels.addElement(cels.elementAt(i)) ;

      // Sort the working list by z-level.  The sort retains original order
      // for duplicate values.

      Collections.sort(sortedcels,new LevelComparator()) ;
      
      // Construct our initial state.
      
      for (int i = 0 ; i < sortedcels.size() ; i++)
      {
         Object [] layeredit = new Object [2] ;
         Cel c = (Cel) sortedcels.elementAt(i) ;
         if (c == null) continue ;
         layeredit[0] = c.getIdentifier() ;
         layeredit[1] = c.getLevel() ;
         initialstate.addElement(layeredit) ;
      }
      
      // Construct our final state. Modify the initial state levels
      // for each identified cel in the kiss object.
      
      for (int i = 0 ; i < initialstate.size() ; i++)
      {
         Object [] layeredit1 = (Object []) initialstate.elementAt(i) ;
         Object [] layeredit2 = new Object [2] ;
         layeredit2[0] = layeredit1[0] ;
         layeredit2[1] = layeredit1[1] ;
         finalstate.addElement(layeredit2) ;
         Object celID = layeredit1[0] ;
         if (celID == null) continue ;
         
         // Look for a match with our selected objects.
         
         boolean adjust = false ;
         for (int j = 0 ; j < selected.size() ; j++)
         {
            Object kiss = selected.elementAt(j) ;
            if (kiss instanceof Cel)
               if (celID.equals(((Cel) kiss).getIdentifier())) adjust = true ;
            if (kiss instanceof Group)
            {
               Vector groupcels = ((Group) kiss).getCels() ;
               for (int k = 0 ; k < groupcels.size() ; k++)
               {
                  Cel c = (Cel) groupcels.elementAt(k) ;
                  if (celID.equals(c.getIdentifier())) { adjust = true ; break ; }
               }
            }
         }
         
         // Construct the new level. Bound our layers to the limits of the
         // cel list.
         
         if (!adjust) continue ;
         Object level = layeredit2[1] ;
         if (!(level instanceof Integer)) continue ;
         int n1 = ((Integer) level).intValue() ;
         int n2 = n1 + direction ;
         if (n2 < 0) n2 = 0 ;
         layeredit2[1] = new Integer(n2) ;
     }
      
      // Apply the undoable layer adjustment.
      
      adjustLayer(initialstate,finalstate) ;
   }



   // Object utility methods
   // ----------------------

   // Initialization.  We are given a KiSS configuration object and
   // we must construct a panel frame environment to properly display
   // this KiSS set.

   void init(Configuration c)
   {
      config = c ;
      mousedown = false ;
      cels = new Vector() ;
      celList = new int [0] ;
      background = Color.black ;
      defaultcursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ;
      presscursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) ;
      dragcursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) ;
      selectcursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) ;
      waitcursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) ;
      east = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) ;
      west = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR) ;
      north = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) ;
      south = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR) ;
      northeast = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR) ;
      northwest = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR) ;
      southeast = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR) ;
      southwest = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR) ;

      // Allocate default size resources.

      panelSize = new Dimension(448,320) ;
      panelArea = new Rectangle(panelSize) ;
      windowSize = new Dimension(panelSize) ;
      windowOffset = new Point(0,0) ;

      // Access the configuration information to customize the parent
      // window for our specific KiSS set.

      if (config != null)
      {
         String s = parent.getTitle() ;
         String s1 = parent.getUserTitle() ;
         int i = s.indexOf('-') ;
         s = (i >= 0) ? s.substring(0,i) : s + " " ;
         String name = config.getName() ;
         if (name != null) s += "- " + " " + name ;
         if (s1 != null) s = s1 ;
         if (name != null) parent.setTitle(s) ;
         panelSize = new Dimension(config.getSize()) ;
         panelArea = new Rectangle(panelSize) ;
         windowSize = new Dimension(panelSize) ;
         sf = config.getScaleFactor() ;
         cels = config.getCels() ;
         groups = config.getGroups() ;
   		if (OptionsDialog.getDebugControl())
   			System.out.println("PanelFrame initialize frame for configuration \"" + name + "\" (" + config.getID() + ")") ;
      }

      // Allocate the panel frame image buffers.

      createbuffers(panelSize.width,panelSize.height) ;
      panelSize.width = fullImage.getWidth(null) ;
      panelSize.height = fullImage.getHeight(null) ;

      // Locate our undo manager for edits.

      undo = null ;
      KissMenu menu = parent.getMenu() ;
      if (menu instanceof PanelMenu)
         undo = ((PanelMenu) menu).getUndoManager() ;
   }


   // Create two image buffers.  The base image buffer is used
   // as a base upon which to draw the moving cel group.  The full
   // image buffer is where we actually draw the final scene.

   void createbuffers(int width, int height)
   {
      GraphicsConfiguration gc = parent.getGraphicsConfiguration() ;
      if (gc == null) return ;
      if (width < 1) width = 1 ;
      if (height < 1) height = 1 ;
      if (fullgc != null) fullgc.dispose() ;
      if (basegc != null) basegc.dispose() ;
      fullImage = gc.createCompatibleImage(width,height) ;
      baseImage = gc.createCompatibleImage(width,height) ;
      fullgc = fullImage.getGraphics() ;
      basegc = baseImage.getGraphics() ;
      imageArea = new Rectangle(0,0,width,height) ;
   }


   // Page initialization.  This function sets up the environment to
   // properly display the requested page set in our panel frame.
   // All audio and video stop playing on a page initialization.
   // Page set events will be fired every time the page is initialized.

   void initpage(int pageset)
   {
      if (config == null) return ;
      if (pageset < 0) page = null ;
      if (pageset >= config.getPageCount()) page = null ;
      Vector comps = config.getComponents() ;
      Vector sounds = config.getSounds() ;
      Vector movies = config.getMovies() ;

      // Clear our Java component visibility.  Only visible components on
      // this page should be added to the panel frame.

      if (comps != null)
      {
         for (int i = 0 ; i < comps.size() ; i++)
         {
            JavaCel c = (JavaCel) comps.elementAt(i) ;
            c.showComponent(false) ;
         }
      }

      // Clear our movie visibility.  Only visible components on
      // this page should be added to the panel frame.

      if (movies != null)
      {
         for (int i = 0 ; i < movies.size() ; i++)
         {
            Video v = (Video) movies.elementAt(i) ;
            v.showComponent(false) ;
         }
      }

      // If we switch to the current page fire page set events and resume
      // any suspended media player.

      if (page != null && config.getPage(pageset) == page)
      {
         if (OptionsDialog.getDebugControl())
            System.out.println("PanelFrame reactivate page " + pageset) ;

         // Create our current cel display list.

         celList = createCelList(pageset) ;
         baseList = new int [celList.length] ;
         for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;

         // Reset the group context for the page.

         Vector groups = page.getGroups() ;
         Integer pageid = new Integer(pageset) ;
         for (int i = 0 ; i < groups.size() ; i++)
         {
            Group g = (Group) groups.elementAt(i) ;
            g.setContext(pageid) ;
         }

         // Establish the current colors.

         Integer multipalette = page.getMultiPalette() ;
         initcolor(multipalette) ;

         // Set our Java component visibility.  Only visible components on
         // this page should be added to the panel frame.

         if (comps != null)
         {
            for (int i = 0 ; i < comps.size() ; i++)
            {
               JavaCel c = (JavaCel) comps.elementAt(i) ;
               c.showComponent(c.isOnPage(new Integer(pageset)));
            }
         }

         // Set our video cel visibility.  Only visible components on
         // this page should be added to the panel frame.

         if (movies != null)
         {
            for (int i = 0 ; i < movies.size() ; i++)
            {
               Video v = (Video) movies.elementAt(i) ;
               v.showComponent(v.isOnPage(new Integer(pageset)));
            }
         }

         // Enable the media player.

         MediaFrame mf = config.getMediaFrame() ;
         if (mf != null) mf.resume() ;

         // Fire any page set events.

         boolean isvisible = OptionsDialog.getPanelVisible() || isVisible() ;
         if (!undoredo && isvisible && isEnabled())
         {
            Vector v = page.getEvent("set") ;
            if (v != null)
               EventHandler.fireEvents(v,this,Thread.currentThread(),page) ;
            
            // Watch for generic set(*) events that apply to all pages
            
            else if (config != null)
            {
               EventHandler handler = config.getEventHandler() ;
               v = (handler != null) ? handler.getEvent("set") : null ;
               Enumeration enum1 = (v != null) ? v.elements() : null ;
               while (enum1 != null && enum1.hasMoreElements())
               {
                  FKissEvent evt = (FKissEvent) enum1.nextElement() ;
                  if (!"*".equals(evt.getFirstParameter())) continue ;
                  EventHandler.queueEvent(evt,Thread.currentThread(),page) ;
               }
            }
         }
         return ;
      }

      // We are switching to a new page.

      Cursor cursor = parent.getCursor() ;
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      suspendEvents() ;
      if (OptionsDialog.getDebugControl())
         System.out.println("PanelFrame initialize page " + pageset) ;

      // Stop any audio and video in progress if we have a prior page.

      if (page != null)
      {
         if (sounds != null)
         {
            for (int i = 0 ; i < sounds.size() ; i++)
            {
               if (OptionsDialog.getStopMusic()) { Audio.stop(config) ; break ; }
//             Audio.stop((Audio) sounds.elementAt(i)) ;
            }
         }

         // Always stop any video in progress.

         if (movies != null)
         {
            for (int i = 0 ; i < movies.size() ; i++)
            {
               Video.stop((Video) movies.elementAt(i)) ;
            }
         }
      }

      // We must save the object positions for the page set currently
      // on display and then restore any prior positions for the new
      // page being displayed.  If pages are scenes then page 0 is the
      // position of record for movement and restoring object positions.

      Object cid = config.getID() ;
      if (page != null) page.saveState(cid,"panelframe") ;
      page = (PageSet) PageSet.getByKey(PageSet.getKeyTable(),cid,new Integer(pageset)) ;
      if (page != null) page.restoreState(cid,"panelframe") ;

      // Unload all loaded cels that are not on our new page.  Selected cels
      // or updated cels are not unloaded.  Cels that are members of internal
      // object groups are not unloaded.  Cels that have copies are not
      // unloaded.  

      if (baseList != null && !OptionsDialog.getCacheImage())
      {
         if (OptionsDialog.getDebugLoad())
            System.out.println("PanelFrame unload cels not on page " + pageset) ;
         unloadCels(pageset) ;
      }

      // Create our cel display list.  Cels must be painted in reverse
      // order to their declaration as the declaration order represents
      // the cel overlay priority.

      Runtime.getRuntime().gc() ;
      celList = createCelList(pageset) ;
      baseList = new int [celList.length] ;
      for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;

      // Load all unloaded cel images that are on our new page.

      if (OptionsDialog.getDebugLoad())
         System.out.println("PanelFrame load unloaded cels on page " + pageset) ;
      loadCels(pageset) ;

      // Set the initial colors for this page.  These are the colors defined
      // by the multipalette established for the page set.

      Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
      initcolor(multipalette) ;

      // Remove the group cels from the base picture if the mouse is down
      // and we are dragging an object.  The celList and baseList contain
      // integer indexes into the Cel vector, thus we remove the actual
      // index into the cel vector from the list.

      if (group != null)
      {
         for (int i = 0; i < group.getCelCount(); i++)
         {
            Cel c = group.getCel(i) ;
            int n = cels.indexOf(c) ;
            for (int j = 0 ; j < baseList.length ; j++)
               if (baseList[j] == n) { baseList[j] = -1 ; break ; }
         }
      }

      // Initialize the page.  This moves the objects to their proper
      // positions and then fires any page set initialization events.

      if (page != null)
      {
         page.init() ;

         // Restore our group selection set if required.

         if (selection != null)
         {
            groupset = new Group() ;
            Integer pg = new Integer(pageset) ;
            for (int i = 0 ; i < selection.size() ; i++)
            {
               KissObject kiss = (KissObject) selection.elementAt(i) ;
               if (kiss instanceof Group)
               {
                  Group g = (Group) kiss ;
                  if (g.isOnPage(pg)) groupset.addElement(g,false) ;
               }
            }
         }

         // Fire page set events. A specific event is fired if specified,
         // otherwise the generic set(*) event is fired if it exists.

         boolean isvisible = OptionsDialog.getPanelVisible() || isVisible() ;
         if (!undoredo && isvisible && isEnabled())
         {
            Vector v = page.getEvent("set") ;
            if (v != null)
               EventHandler.fireEvents(v,this,Thread.currentThread(),page) ;
            else if (config != null)
            {
               EventHandler handler = config.getEventHandler() ;
               v = (handler != null) ? handler.getEvent("set") : null ;
               Enumeration enum1 = (v != null) ? v.elements() : null ;
               while (enum1 != null && enum1.hasMoreElements())
               {
                  FKissEvent evt = (FKissEvent) enum1.nextElement() ;
                  if (!"*".equals(evt.getFirstParameter())) continue ;
                  EventHandler.queueEvent(evt,Thread.currentThread(),page) ;
               }
            }
         }

         // Set our Java component visibility.  Only visible components on
         // this page should be added to the panel frame.

         if (comps != null)
         {
            for (int i = 0 ; i < comps.size() ; i++)
            {
               JavaCel c = (JavaCel) comps.elementAt(i) ;
               boolean b = c.isOnPage(new Integer(pageset)) ;
               c.showComponent(b);
            }
         }

         // Set our video cel visibility.  Only visible components on
         // this page should be added to the panel frame.

         if (movies != null)
         {
            for (int i = 0 ; i < movies.size() ; i++)
            {
               Video v = (Video) movies.elementAt(i) ;
               boolean b = v.isOnPage(new Integer(pageset)) ;
               v.showComponent(b);
            }
         }
      }

      // Update the toolbar to show the correct page selection.

      parent.updateMenu() ;
      parent.updateToolBar() ;
      parent.setCursor(cursor);
      parent.showStatus(null);
   }



   // Color initialization.  Set the cel colors to refer to the specified
   // multipalette.  The multipalette is an offset into the first palette
   // file, palette file zero.  It fires color set events.

   void initcolor(Integer multipalette)
   {
      if (config == null) return ;
      if (page == null || multipalette == null) return ;
      Integer priormultipalette = page.getMultiPalette() ;
      if (OptionsDialog.getDebugControl())
         System.out.println("PanelFrame initialize palette group " + multipalette) ;

      // Convert all cels to the new palette colors.  The multipalette
      // number is now an offset into the cel palette file.  Note that
      // the cel palette file may not be the same file as palette file
      // zero.  If this is the case and the actual cel palette file does
      // not contain multiple offsets, then the cel colors will not change.
      // Otherwise, we will change the cel colors to reference the
      // multipalette from the actual cel palette file.

      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel cel = (Cel) cels.elementAt(i) ;
         cel.changePalette(multipalette) ;
      }

      // Ensure that the required multipalette exists.  We calculate the
      // number of color sets.  This is defined by the maximum number of
      // palette groups across all palette files.

      int n = 0 ;
      int groups = 0 ;
      while (config != null)
      {
         Palette p1 = (Palette) Palette.getByKey(Palette.getKeyTable(),config.getID(),new Integer(n++)) ;
         if (p1 == null) break ;
         int multipalettes = p1.getMultiPaletteCount() ;
         if (multipalettes > groups) groups = multipalettes ;
      }

      // Check for a valid color set.

      palette = (config != null) ? config.getPalette(0) : null ;
      if (palette == null) background = getBackground() ;
      if (palette == null) border = null ;
      int mp = multipalette.intValue() ;
      if (mp >= groups) return ;

      // Set the current multipalette value in the active page set.  This is
      // where we reference the multipalette in use for the current page.

      page.setMultiPalette(multipalette) ;

      // Set the panel frame background color to be the palette file
      // zero multipalette transparent color.  We will establish a
      // default border color for the panel frame based upon the panel
      // background color.  For extreme brightness or darkness, we will
      // use constant color border lines around the panel frame.

      if (palette != null)
         background = palette.getColor(mp,palette.getBackgroundIndex()) ;
      border = getBackground() ;
      darkborder = border.darker() ;
      lightborder = border.brighter() ;
      int rgb = border.getRGB() ;
      int red = rgb & (255<<16) >> 16 ;
      int green = rgb & (255<<8) >> 8 ;
      int blue = rgb & (255) ;
      float hsb[] = Color.RGBtoHSB(red,green,blue,null) ;
      if (hsb[2] > 0.9) darkborder = lightborder = Color.darkGray ;
      if (hsb[2] < 0.1) darkborder = lightborder = Color.lightGray ;

      // Perform any palette initialization events.  All multipalette
      // events are linked to palette 0.  We will invoke the palette event
      // only for the palette that we are switching to.

      Vector v = (palette != null) ? palette.getEvent("col") : null ;
      if (!undoredo && isVisible() && isEnabled() && v != null)
      {
         boolean colfired = false ;
         boolean colgeneric = false ;
         for (int i = 0 ; i < v.size() ; i++)
         {
            int colorset ;
            FKissEvent evt = (FKissEvent) v.elementAt(i) ;
            String s = evt.getFirstParameter() ;
            if ("*".equals(evt.getFirstParameter()))
               { colgeneric = true ; continue ; }
            try { colorset = Integer.parseInt(s) ; }
            catch (NumberFormatException e) { continue ; }
            if (colorset == mp)
            {
               evt.fireEvent(this,Thread.currentThread(),palette) ;
               colfired = true ;
            }
         }

         // Fire generic col(*) events if required.

         if (!colfired & colgeneric)
         {
            for (int i = 0 ; i < v.size() ; i++)
            {
               FKissEvent evt = (FKissEvent) v.elementAt(i) ;
               if (!"*".equals(evt.getFirstParameter())) continue ;
               evt.fireEvent(this,Thread.currentThread(),palette) ;
            }
         }
      }

      // Update the toolbar to show the correct multipalette.

      parent.updateMenu() ;
      parent.updateToolBar() ;
   }


   // A function to create a new cel list for the specified page.  This is a
   // list of all cels that can be drawn on the page.  The returned cel list
   // is in z-level order from bottom cels to top cels. We return only cels
   // that are in groups on this page. Video cels are on top and appear last
   // in the list.  Duplicate level cels or pasted cels were inserted at the
   // end of the configuration cel list.

   private int [] createCelList(int pageset)
   {
      if (cels == null) return new int [0] ;
      Integer pg = new Integer(pageset) ;

      // Invert the configuration cel list so that lower index duplicate level
      // cels appear at the top of the working list.

      Vector sortedcels = (Vector) cels.clone() ;
      Collections.reverse(sortedcels) ;

      // Sort the working list by z-level.  The sort retains original order
      // for duplicate values.

      Collections.sort(sortedcels,new LevelComparator()) ;

      // Construct the page cel list.  Isolate video cels.

      Vector videoList = new Vector() ;
      Vector celList = new Vector(cels.size()) ;
      for (int i = sortedcels.size()-1 ; i >= 0 ; i--)
      {
         Cel c = (Cel) sortedcels.elementAt(i) ;
         if (c.isOnPage(pg))
         {
            Object o = c.getGroup() ;
            if (!(o instanceof Group)) continue ;
            if (!(page.contains((Group) o))) continue ;
            if (c instanceof Video)
               videoList.addElement(new Integer(cels.indexOf(c))) ;
            else
               celList.addElement(new Integer(cels.indexOf(c))) ;
         }
      }

      // Video cels are last in the list so they are drawn on top of all
      // normal cels, followed by all remaining cels.

      celList.addAll(videoList) ;
      int [] celnumbers = new int [celList.size()] ;
      for (int i = 0 ; i < celList.size() ; i++)
      {
         Object o = celList.elementAt(i) ;
         celnumbers[i] = (o instanceof Integer) ? ((Integer) o).intValue() : -1 ;
      }
      return celnumbers ;
   }


   // A function to update our cel list due to a layer adjustment change.
   // This update ensures that the current baselist with removed values
   // is not destroyed if we have selected a group for mouse movement.

   void updateCelList()
   {
      if (page == null) return ;
      Object id = page.getIdentifier() ;
      if (!(id instanceof Integer)) return ;

      // Create a new cel display list.  This is synchronized to ensure
      // that multiple events that clone or destroy cels do not conflict.

      synchronized (config)
      {
         int pageset = ((Integer) id).intValue() ;
         celList = createCelList(pageset) ;
         if (group != null) return ;
         baseList = new int [celList.length] ;
         for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
      }
   }


   // A function to load all cels on the specified page.  Cels may be
   // unloaded on a page change.  This function is typically called on
   // a page initialization. The usual requirement is to update the
   // group object as cels are loaded, but this is not required on a
   // restore for edit paste.

   private void loadCels(int pageset) { loadCels(pageset,true) ; }
   private void loadCels(int pageset, boolean update)
   {
      try
      {
         if (config == null) return ;
         ArchiveFile zip = config.getZipFile() ;
         boolean open = (zip != null) ? zip.isOpen() : true ;
         if (!open) zip.open() ;
         Vector groups = new Vector() ;
         String s = (OptionsDialog.getPagesAreScenes()) ? "scene" : "page" ;
         if (OptionsDialog.getDebugLoad())
            parent.showStatus("Load images on " + s + " " + pageset + " ...");
         Integer page = new Integer(pageset) ;

         for (int i = 0 ; i < baseList.length ; i++)
         {
            int index = baseList[i] ;
            if (index < 0 || index >= cels.size()) continue ;
            Cel c = (Cel) cels.elementAt(index) ;
            if (c.isLoaded()) continue ;
            if (!c.isOnSpecificPage(page)) continue ;
            Vector includefiles = config.getIncludeFiles() ;
            loadCel(c,pageset,includefiles) ;
            if (!update) continue ;
            Object o = c.getGroup() ;
            if (o instanceof Group)
            {
               if (!groups.contains(o)) groups.addElement(o) ;
               ((Group) o).updateBoundingBox() ;
            }            
         }

         // Rebuild the group offsets.  Cels may have been moved through 
         // alarms while unloaded.  We need to position the group when the
         // cels are established and we know the size.

         if (update)
         {
            for (int i = 0 ; i < groups.size() ; i++)
            {
               Group g = (Group) groups.elementAt(i) ;
               Point p = g.getLocation() ;
               g.updateOffset() ;

               // Get the current group location and compute the relative
               // placement offset necessary to move the group to the required
               // position.

               g.setContext(pageset) ;
               Rectangle box = g.getBoundingBox() ;
               Point offset = g.getOffset() ;
               Point location = new Point(box.x-offset.x,box.y-offset.y) ;

               // Apply the displacement offset.

               int dispX = p.x - location.x ;
               int dispY = p.y - location.y ;
               g.setPlacement(dispX,dispY) ;
               g.drop() ;
            }
         }

         // Rebuild the animated GIF list as new cels may now be loaded.

         GifTimer animator = config.getAnimator() ;
         if (animator != null)
         {
            animator.updateCels(config.getAnimatedCels()) ;
            animator.setPanelFrame(this) ;
         }
//       if (!open) zip.close() ;
      }
      catch (Exception e)
      {
         System.out.println("PanelFrame: initPage load cels, " + e) ;
         e.printStackTrace();
      }
   }

   // A function to unload all cels not on the specified page.  Cels may be
   // unloaded on a page change.  This function is typically called on
   // a page initialization when images are not cached. 
   
   private void unloadCels(int pageset)
   {
      try
      {
         Integer p = new Integer(pageset) ;
         Integer p0 = new Integer(0) ;
         for (int i = 0 ; i < baseList.length ; i++)
         {
            int index = baseList[i] ;
            if (index < 0 || index >= cels.size()) continue ;
            Cel c = (Cel) cels.elementAt(index) ;
            if (c instanceof JavaCel) continue ;
            if (!c.isLoaded()) continue ;
            if (c.isUpdated()) continue ;
            if (c.isImported()) continue ;
            if (c.isExported()) continue ;
            if (isSelected(c)) continue ;
            if (c.isOnAllPage()) continue ;
            if (c.isOnSpecificPage(p)) continue ;
            if (OptionsDialog.getPagesAreScenes() && c.isOnSpecificPage(p0)) continue ;
            if (OptionsDialog.getPagesAreScenes() && pageset == 0) continue ;
            if (c.getFrameCount() > 1) continue ;
            Object o = c.getGroup() ;
            if (o instanceof Group && ((Group) o).isInternal()) continue ;
            c.unload() ;
         }      
      }
      catch (Exception e)
      {
         System.out.println("PanelFrame: initPage unload cels, " + e) ;
      }
   }

   
   // A function to load a specific cel for the specified pageset.  
   // Cels may be unloaded on a page change. 

   private void loadCel(Cel c, int pageset, Vector includefiles)
   {
      if (c == null) return ;
      if (c.isLoaded()) return ;
      c.load(includefiles) ;
      if (OptionsDialog.getDebugLoad())
      {
         String s = "Load: (page " + pageset + ") " + c ;
         if (c.isCopy()) s += " [copy]" ;
         System.out.println(s) ;
      }
   }

   
   // A function to unload a specific cel for the specified pageset.  
   // Cels may be unloaded on a page change. 

   private void unloadCel(Cel c, int pageset)
   {
      if (c == null) return ;
      if (!c.isLoaded()) return ;
      if (c.isUpdated()) return ;
      if (c.isImported()) return ;
      if (isSelected(c)) return ;
      if (c.isOnAllPage()) return ;
      if (!c.isOnSpecificPage(new Integer(pageset))) return ;
      if (OptionsDialog.getPagesAreScenes() && c.isOnSpecificPage(new Integer(0))) return ;
      if (c.getFrameCount() > 1) return ;
      c.unload() ;
      if (OptionsDialog.getDebugLoad())
      {
         String s = "Unload: (page " + pageset + ") " + c ;
         if (c.isCopy()) s += " [copy]" ;
         System.out.println(s) ;
      }
   }


   // A reset request causes the page set to be re-established.  We
   // suspend any active timers and reset all cels to their initial
   // state.

   void reset() { reset(null,false) ; }
   void reset(State sv) { reset(sv,false) ; }
   void reset(boolean select) { reset(null,select) ; }
   void reset(State sv, boolean select)
   {
      if (page == null) return ;
      if (config == null) return ;
      Object cid = config.getID() ;
      if (sv == null) sv = page.getState(cid,"initial") ;
      Dimension configsize = config.getSize() ;
      int w = (int) (configsize.width * sf) ;
      int h = (int) (configsize.height * sf) ;
      configsize = new Dimension(w,h) ;

      // A reset can clear any selection set.
      
      if (select)
      {
         selection = null ;
         groupset = null ;
      }

      // Suspend any active timers and reset all cel animation loop counts
      // for visible cels on this page.  Also reset initial KCF files and
      // fixed palette groups possibly changed through setkcf() and setpal()
      // action commands.

      suspendEvents() ;
      Integer pg = (page != null) ? (Integer) page.getIdentifier() : null ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel cel = (Cel) cels.elementAt(i) ;
         if (cel.isOnPage(pg)) 
         {
            Object o = cel.getInitPaletteGroupID() ;
            cel.setPaletteID(cel.getInitPaletteID()) ;
            cel.setPaletteGroupID(o) ;
            if (o instanceof Integer) cel.changePalette((Integer) o) ;
            if (cel.isVisible()) cel.resetAnimation() ;
         }
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         if (page != null) page.saveState(cid,"panelframe") ;
         State current = (page != null) ? page.getState(cid,"panelframe") : null ;
         UndoablePageEdit ce = new UndoablePageEdit(RESET,page,current,sv) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
      }

      // Show a diagnostic trace.

      if (OptionsDialog.getDebugEdit())
         System.out.println("Edit: Reset " + page) ;

      // The reset request must perform all page initialization
      // events to ensure that objects are placed at their initial
      // positions.  We do not restore visibility because some
      // groups or cels may have been mapped or unmapped as a
      // result of previous events.

      page.setState(cid,"panelframe",sv) ;
      page.restoreState(cid,"panelframe") ;
      page.saveState(cid,"panelframe") ;
      Object o = page.getIdentifier() ;
      int p = (o instanceof Integer) ? ((Integer) o).intValue() : 0 ;
      page.init() ;

      // Reset any initial movement restrictions.

      for (int i = 0 ; i < groups.size() ; i++)
      {
         Group group = (Group) groups.elementAt(i) ;
         group.setRestrictX(group.getInitialRestrictX()) ;
         group.setRestrictY(group.getInitialRestrictY()) ;
      }

      // Re-establish our group set as the reset has changed locations.

      if (groupset != null) groupset.updateBoundingBox() ;

      // Initialize the page for viewing.

      initpage(p) ;
      parent.updateMenu() ;
      showpage() ;
   }



   // A changerelativesize request alters the panel frame dimensions by
   // a relative amount.  The viewport is centered in the new panel.

   void changerelativesize(int w, int h)
   {
      w += panelSize.width ;
      h += panelSize.height ;
      changesize(w,h) ;
   }



   // A changesize request alters the panel frame dimensions and centers
   // the new panel in the viewport.  It also adjusts the current window.
   // The width and height parameters are display dimensions.

   void changesize(int width, int height)
   {
      if (config == null) return ;
      suspendEvents() ;
      float sfx = ((float) width) / panelSize.width ;
      float sfy = ((float) height) / panelSize.height ;
      int ww = (int) (windowSize.width * sfx) ;
      int wh = (int) (windowSize.height * sfy) ;
      panelSize.width = width ;
      panelSize.height = height ;
      windowSize = new Dimension(ww,wh) ;

      // Revalidate the scroll pane to reset the scroll bars and
      // center the new panel on the screen.

      Object o = this.getParent() ;
      if (o instanceof JComponent) ((JComponent) o).revalidate() ;
      parent.centerpanel() ;
      parent.updateMenu() ;
      showpage() ;
   }



   // A changeviewport request moves the current window position to a
   // new location.  The argument values are relative offsets and bounded
   // to the panel size.

   void changeviewport(int x, int y)
   {
      windowOffset.x += x ;
      windowOffset.y += y ;
      if (windowOffset.x + windowSize.width > panelSize.width)
         windowOffset.x = panelSize.width - windowSize.width ;
      if (windowOffset.y + windowSize.height > panelSize.height)
         windowOffset.y = panelSize.height - windowSize.height ;
      if (windowOffset.x < 0) windowOffset.x = 0 ;
      if (windowOffset.y < 0) windowOffset.y = 0 ;
   }



   // A changewindow request sizes the viewing window by a relative
   // amount.  The viewing window is a subset of the panel frame.
   // The window cannot be made larger than the original panel size.
   // The width and height parameters are display dimensions.

   void changewindow(int w, int h)
   {
      int width ;
      int height ;
      if (OptionsDialog.getAbsoluteWindow())
      {
         width = w ;
         height = h ;         
      }
      else
      {
         width = w + windowSize.width;
         height = h + windowSize.height ;         
      }

      int xb = x - ((panelSize.width - windowSize.width) / 2) ;
      int yb = y - ((panelSize.height - windowSize.height) / 2) ;
      int maxwidth = panelSize.width ;
      int maxheight = panelSize.height ;
      if (width > maxwidth) width = maxwidth ;
      if (height > maxheight) height = maxheight ;
      if (width < 0) width = 0 ;
      if (height < 0) height = 0 ;
      windowSize.width = width ;
      windowSize.height = height ;
      changeviewport(-w/2,-h/2) ;
      setOffset(xb,yb) ;
      releaseMouse(true) ;
      clearscreen = true ;
      repaint() ;
   }



   // A resize request changes the scale factor for the data set.  This
   // can magnify or reduce the panel frame size.

   void resize(float sf)
   {
      if (config == null) return ;
      Dimension size = config.getSize() ;
      fitscreen((int) (size.width*sf), (int) (size.height*sf)) ;
   }

   
   
   // A resizepanel request changes the image buffer sizes to the 
   // specified size.  The set is redrawn in the buffers.

   void resizepanel(Dimension d)
   {
      createbuffers(d.width,d.height) ;
      panelSize = new Dimension(d) ;
      panelArea = new Rectangle(panelSize) ;
      windowSize = new Dimension(panelSize) ;
      panelSize.width = fullImage.getWidth(null) ;
      panelSize.height = fullImage.getHeight(null) ;
      baseList = new int [celList.length] ;
      for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
      box = new Rectangle(imageArea) ;
      draw(basegc,box,baseList,null) ;
      copy(fullgc,box,baseImage) ;
   }



   // Fit the panel frame to the specified screen size.  This starts an
   // independent activity to scale all the cel images.  When the activity
   // terminates a callback event occurs and the fitscreenreturn method
   // is invoked.  If the fit option is false we revert to normal size.
   // If the exact option is true we do not adjust scale factors near 1.

   void fitscreen(int x, int y) { fitscreen(true,x,y,undoredo) ; }
   void fitscreen(boolean fit, int x, int y) { fitscreen(fit,x,y,undoredo) ; }
   void fitscreen(boolean fit, int x, int y, boolean undoredo) 
   {
      if (config == null) return ;
      suspendEvents() ;

      // Compute the scaling factor required to fit the cel images
      // to the specified screen dimension.

      oldsf = sf ;
      Dimension size = config.getSize() ;
      float sx = ((float) x) / size.width ;
      float sy = ((float) y) / size.height ;
      newsf = Math.min(sx,sy) ;
      if (newsf < 0.01f) newsf = 0.01f ;
      if (!fit) newsf = 1.0f ;
      if (OptionsDialog.getDebugEdit() && newsf != 1.0f)
         System.out.println("Edit: Scale to fit screen by " + newsf) ;

      // We must scale input type JavaCel and Video components.  Scaling 
      // will resize the component and position it properly within the
      // panel frame.

      try
      {
         if (config != null)
         {
            Vector cels = config.getCels() ;
            for (int i = 0 ; i < cels.size() ; i++)
            {
               if (cels.elementAt(i) instanceof JavaCel)
                  ((Cel) cels.elementAt(i)).scaleImage(newsf) ;
               if (cels.elementAt(i) instanceof Video)
                  ((Video) cels.elementAt(i)).scaleImage(newsf) ;
            }
         }
      }

      // Watch for scaling faults.

      catch (KissException e)
      {
         System.out.println("PanelFrame: Scaling fault " + e.getMessage()) ;
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }

         // Show a message dialog.

         JOptionPane.showMessageDialog(parent, e.getMessage(),
            Kisekae.getCaptions().getString("ScalingFault"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }
      
      fitscreenreturn(undoredo) ;
   }


   // Callback from fitscreen scaling activity.  The callback accesses the
   // state from the last ScaleDialog object that was created through a
   // fitscreen call.

   void fitscreenreturn() { fitscreenreturn(false) ; }
   void fitscreenreturn(boolean undoredo)
   {
      if (config == null) return ;

      // Establish the new group context for the current page.

      try
      {
         if (page != null)
         {
            Vector groups = config.getGroups() ;
            Integer pg = (Integer) page.getIdentifier() ;
            for (int i = 0 ; i < groups.size() ; i++)
               ((Group) groups.elementAt(i)).setContext(pg) ;

            // Convert all cels to the page palette colors.

//            Integer multipalette = page.getMultiPalette() ;
//            for (int i = 0 ; i < cels.size() ; i++)
//               ((Cel) cels.elementAt(i)).changePalette(multipalette) ;
         }

         // Adjust the panel frame size to match the new scaled size.

         sf = newsf ;
         config.setScaleFactor(sf) ;
         Dimension size = config.getSize() ;
         JViewport jv = parent.getViewport() ;
         Rectangle view = jv.getViewRect() ;
         float dx = (view.x + (view.width * 0.5f)) / panelSize.width ;
         float dy = (view.y + (view.height * 0.5f)) / panelSize.height ;
         if (view.width > panelSize.width) dx = 0.5f ;
         if (view.height > panelSize.height) dy = 0.5f ;
         int sx = (int) (size.width * sf) ;
         int sy = (int) (size.height * sf) ;
         changesize(sx,sy) ;

         // Center the viewport on the view center.  The new panel size
         // has been set to the new scaled size.

         view = jv.getViewRect() ;
         int px = (int) ((panelSize.width * dx) - (view.width * 0.5f)) ;
         int py = (int) ((panelSize.height * dy) - (view.height * 0.5f)) ;
         if (px < 0) px = 0 ;
         if (py < 0) py = 0 ;
         jv.setViewPosition(new Point(px,py)) ;
      }

      // Watch for memory errors.  If we run out of memory try to revert to
      // an unscaled version of the images.  If this fails throw the exception
      // up to the next level.

      catch (OutOfMemoryError e)
      {
         fullImage = null ;
         baseImage = null ;
         if (recovery) throw(e) ;
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         System.out.println("PanelFrame: Image scaling.  Out of memory. ") ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
         recovery = true ;
         fitscreen(false,0,0) ;
         recovery = false ;
      }

      // Watch for general errors.  These can occur if we have improper
      // scaling factors.

      catch (Throwable e)
      {
         fullImage = null ;
         baseImage = null ;
         if (recovery) throw (new Error("Scaling Fault")) ;
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         System.out.println("PanelFrame: Internal scaling fault, factor = " + sf) ;
         System.out.println(e.toString()) ;
         JOptionPane.showMessageDialog(parent,
            e.getMessage(),
            Kisekae.getCaptions().getString("ScalingFault"),
            JOptionPane.ERROR_MESSAGE) ;
         recovery = true ;
         fitscreen(false,0,0) ;
         recovery = false ;
      }

      // Capture this edit for undo/redo processing.

      if (!undoredo)
      {
         UndoableSizeEdit ce = new UndoableSizeEdit(SCALE,oldsf,sf) ;
         UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
         if (undo != null) undo.undoableEditHappened(evt) ;
         parent.updateMenu() ;
      }
   }


   // The showpage method draws the page and activates any suspended
   // timer activities.   We update the window title to show the current
   // page number, color palette, and archive file in use.

   void showpage()
   {
      String s = parent.getTitle() ;
      String s1 = parent.getUserTitle() ;
      int i = s.indexOf(" (") ;
      s = (i >= 0) ? s.substring(0,i) : s ;
      i = s.indexOf('[') ;
      s = (i >= 0) ? s.substring(0,i) : s + " " ;
      if (page != null)
         s += "[" + page.getIdentifier() + "] [" + page.getMultiPalette() + "]" ;
      if (config != null)
      {
         ArchiveFile zip = config.getZipFile() ;
         if (zip != null)
         {
            String name = zip.getFileName() ;
            if (name != null) s += "  (" + name + ") " ;
            if (zip.isUpdated()) s += "[" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
         }
      }
      if (s1 != null) s = s1 ;
      parent.setTitle(s) ;

      // Draw the initial scene.  We draw every cel in the panel window
      // to ensure that the complete scene is painted.  We also request
      // that input focus be set to this component for keyboard events.

      draw(basegc,imageArea,celList,null) ;
      if (baseImage != null) fullgc.drawImage(baseImage,0,0,null) ;

      // Resume all suspended alarms.

      resumeEvents() ;
      parent.updateRunState() ;
//    parent.requestFocus() ;
      requestFocus() ;
      repaint() ;
   }


   // The showpopup method activates a contextual popup menu for the
   // specified component.

   void showpopup(Component c, int x, int y, Cel cel, Group group)
   {
      if (popup == null) return ;
      if (!OptionsDialog.getEditEnable()) return ;
      if (!OptionsDialog.getTempEditEnable()) return ;
      popup.show(c,x,y,cel,group,page) ;
   }


   // This is a method to temporarily suspend the animation events.
   // When the timer is activated any events that update the
   // display will call this panel's redraw method.  We should
   // suspend the timer if we are setting up a new page set.

   void suspendEvents()
   {
      if (config == null) return ;
      AlarmTimer timer = config.getTimer() ;
      if (timer != null) timer.suspendTimer() ;
      GifTimer animator = config.getAnimator() ;
      if (animator != null) animator.suspendTimer() ;
      EventHandler.suspendEventHandler() ;
   }


   // This is a method to resume the animation events.

   void resumeEvents()
   {
      if (config == null) return ;
      AlarmTimer timer = config.getTimer() ;
      if (timer != null) timer.resumeTimer() ;
      GifTimer animator = config.getAnimator() ;
      if (animator != null) animator.resumeTimer() ;
      EventHandler.resumeEventHandler();
   }



   // When we paint the screen, we want to draw only within the
   // bounding box for the cels that are being dragged.  This is a
   // smaller area to fill and can improve the graphic performance.
   // The paint method is synchronized to stop flicker when multiple
   // activities request simultaneous repaints.

   public synchronized void paintComponent (Graphics g)
   {
      boolean doflicker = false ;   // If true, flicker selection boxes
      final int space = 2 ;      	// size of line segment space
      final int segment = 4 ;			// size of line segment

      // Clear the screen if requested.  This is required if the
      // window size has changed.

      if (clearscreen)
      {
         Dimension d = getSize() ;
         g.setColor(getBackground()) ;
         g.fillRect(0,0,d.width,d.height) ;
         clearscreen = false ;
      }

      // The image to draw is the base image.  We draw relative to
      // base coordinate (0,0) and then position the complete full
      // image in the main display panel.  A moving group image is
      // reconstructed within its bounding box.  We build the image
      // at paint time so that we do not waste time constructing the
      // image during mouse event processing time.

      // With scaled images we have a one pixel drawing problem.
      // Expanding the box size appear to compensate for this.

      if (box != null)
      {
         Integer level = (group != null) ? group.getLevel() : null ;
         // Draw where the image once was into the full buffer.
//       copy(fullgc,priorbox,baseImage) ;
         // Draw where the image now is into the full buffer.
//       draw(fullgc,box,celList,level) ;
         
      // Don't know why this is required?  Problem with attachments and 
      // when a saucer with attached teacup is moved on a press() event the
      // image clips on painting?  2023/08/07
         copy(fullgc,new Rectangle(imageArea),baseImage) ;
         draw(fullgc,new Rectangle(imageArea),celList,level) ;

         // Retain the current box coordinates.

         if (priorbox != null)
         {
            priorbox.x = box.x ;
            priorbox.y = box.y ;
            priorbox.width = box.width ;
            priorbox.height = box.height ;
         }
      }

      // Draw the full scene within the repaint graphics clip area.
      // Draw only the portion of the image within the window area.

      super.paintComponent(g) ;
      int sx = windowOffset.x ;
      int sy = windowOffset.y ;
      int sw = windowSize.width ;
      int sh = windowSize.height ;

      // Java 1.4 volatile image high performance graphics.  It provides
      // video performance improvements.

      if (fullImage != null)
      {
         double windowsfx = ((double) sw) / panelSize.width ;
         double windowsfy = ((double) sh) / panelSize.height ;
         int iw1 = fullImage.getWidth(null) ;
         int ih1 = fullImage.getHeight(null) ;
         int iw = (int) (iw1 * windowsfx) ;
         int ih = (int) (ih1 * windowsfy) ;
         g.drawImage(fullImage,x,y,x+sw,y+sh,sx,sy,sx+iw,sy+ih,null) ;
      }

      // If we are editing, paint a selection box.

      if (selectbox != null && selectbox.width > 0 && selectbox.height > 0)
      {
         Color color = Color.red ;
         int rgb = color.getRGB() ;
         g.setColor(color) ;
         rgb = rgb ^ 0xFFFF ;
         color = new Color(rgb) ;
         g.setXORMode(color) ;
         sx = (int) ((selectbox.x-windowOffset.x) * sf) + x ;
         sy = (int) ((selectbox.y-windowOffset.y) * sf) + y ;
         sw = (int) (selectbox.width * sf) ;
         sh = (int) (selectbox.height * sf) ;
         int colsegments = (sw - space) / (segment + space) ;
         int rowsegments = (sh - space) / (segment + space) ;

         // Draw the horizontal lines.

         int x1 = sx ;
         int y1 = sy ;
         int x2 = sx + sw - 1 ;
         int y2 = sy + sh - 1 ;
         for (int i = 0 ; i < colsegments ; i++)
         {
            x2 = x1 + segment ;
            g.drawLine(x1,y1,x2,y1) ;
            g.drawLine(x1,y2,x2,y2) ;
            x1 += (segment + space) ;
         }
         x2 = sx + sw - 1 ;
         g.drawLine(x1,y1,x2,y1) ;
         g.drawLine(x1,y2,x2,y2) ;

         // Draw the vertical lines.

         x1 = sx ;
         y1 = sy ;
         x2 = sx + sw - 1 ;
         y2 = sy + sh - 1 ;
         for (int i = 0 ; i < rowsegments ; i++)
         {
            y2 = y1 + segment ;
            g.drawLine(x1,y1,x1,y2) ;
            g.drawLine(x2,y1,x2,y2) ;
            y1 += (segment + space) ;
         }
         y2 = sy + sh - 1 ;
         g.drawLine(x1,y1,x1,y2) ;
         g.drawLine(x2,y1,x2,y2) ;
         g.setPaintMode() ;

         // Retain the current drag box coordinates.

         if (priorbox != null)
         {
            priorbox.x = selectbox.x ;
            priorbox.y = selectbox.y ;
            priorbox.width = selectbox.width ;
            priorbox.height = selectbox.height ;
         }
      }

      // If we have a selection set for our current page then draw selection
      // boxes around each object on this page.  If any of our objects were
      // pasted then they will be flagged.  We use this to flicker our boxes
      // by performing a repaint and altering the mark state of the object.
      // The paste flag is cleared on first draw, therfore flicker is only
      // recognized on the initial paint operation.
      
      if (selection != null)
      {
         boolean hasmarked = selection.isMarked() ;
         Object o1 = selection.getPageUniqueID() ;
         Object o2 = (page != null) ? page.getUniqueID() : null ;
         if (o1 != null && o1.equals(o2))
         {
            Graphics gc = g.create() ;
            gc.translate(x,y) ;
            Rectangle gclip = gc.getClipBounds() ;
            Rectangle clip = new Rectangle(0,0,windowSize.width,windowSize.height) ;
            if (gclip != null) clip = clip.intersection(gclip) ;
            gc.setClip(clip) ;

            // Draw selection boxes within the panel clip area.  Show only
            // those objects that are on this page.

            for (int i = 0 ; i < selection.size() ; i++)
            {
               KissObject kiss = (KissObject) selection.elementAt(i) ;
               if (kiss == null) continue ;
               if (kiss instanceof Group && page != null)
                  if (!(page.contains((Group) kiss))) continue ;
               if (kiss instanceof Cel && page != null)
                  if (!(((Cel) kiss).isOnPage((Integer) page.getIdentifier()))) continue ;
               if (kiss.isPasted()) doflicker = true ;
               boolean marked = selection.isMarked(kiss) ;
               boolean flickerstate = flicker ;
               if (hasmarked && !marked) flickerstate = false ;
               kiss.drawSelected(gc,sf,windowOffset.x,windowOffset.y,marked,flickerstate) ;
            }
         }
      }

      // Draw borders around the current window in the panel frame.

      if (OptionsDialog.getShowBorder())
      {
         if (border != null)
         {
            int w = windowSize.width + x ;
            int h = windowSize.height + y ;
            g.setColor(darkborder) ;
            g.drawLine(x-1,y-1,w+1,y-1) ;
            g.drawLine(x-1,y-1,x-1,h+1) ;
            g.setColor(lightborder) ;
            g.drawLine(x-1,h+1,w+1,h+1) ;
            g.drawLine(w+1,y-1,w+1,h+1) ;
         }
      }
      
      // If we had a pasted selection and need to flicker our selection boxes,
      // start a repaint thread to perform the flicker.
      
      if (doflicker)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try 
               {
                  for (int i = 0 ; i < 12 ; i++)
                  {
                     sleep(150) ;
                     flicker = !flicker ;
                     repaint() ;
                  }
               }
               catch (Exception ex) { }
            }
         } ;
         thread.start() ;
      }
   }



   // The draw method is used to paint the required set of cels on the
   // screen.  This method clears the drawing area and then draws the
   // cels over a static background.  This method is synchronized
   // to prevent two different activities from drawing to the same
   // image at the same time.

   synchronized void draw(Graphics g, Rectangle box, int [] celList, Integer limit)
   {
      int cellevel = 0 ;
      if (celList == null || box == null) return ;
      int level = (limit == null) ? -1 : limit.intValue() ;
      Graphics2D g2 = (Graphics2D) g ;
      g2.setClip(new Rectangle(imageArea)) ;

      // If we are dragging an object then we have an alpha compositing
      // problem where the object transparent background can show beneath
      // another transparent image.  The problem goes away if we redraw the
      // scene from the bottom up.

      if (cel != null && level >= 0)
      {
         for (int i = 0 ; i < celList.length ; i++)
         {
            Cel drawcel ;
            int c = celList[i] ;
            try { drawcel = (Cel) cels.elementAt(c) ; }
            catch (ArrayIndexOutOfBoundsException e) { continue ; }
            Object o = drawcel.getLevel() ;
            if (!(o instanceof Integer)) continue ;
            int n = ((Integer) o).intValue() ;
            if (level >= 0 && n > level) continue ;
            if (drawcel.getTransparency() != 255) { level = -1 ; break ; }
         }
      }

      // Clear the screen, assuming that we must draw everything.
      // The group overlay level is used to limit the cels to be
      // drawn to only those which can overlay our group object.

      if (level < 0)
      {
         int bx = box.x ;
         int by = box.y ;
         int bw = box.width ;
         int bh = box.height ;
         g2.setColor(background) ;
         g2.fillRect(bx,by,bw,bh) ;
      }

      // We have something to draw.  Paint the complete screen.
      // The picture is built up one cel at a time from the lowest
      // priority cel to the highest.  We draw a cel only if it
      // can overlay the limit defined by our current group object.
      // This reduces unnecessary drawing overhead.

      for (int i = 0 ; i < celList.length ; i++)
      {
         Cel drawcel ;
         int c = celList[i] ;
         try { drawcel = (Cel) cels.elementAt(c) ; }
         catch (ArrayIndexOutOfBoundsException e) { continue ; }
         Object o = drawcel.getLevel() ;
         if (!(o instanceof Integer)) continue ;
         int n = ((Integer) o).intValue() ;
         if (level >= 0 && n > level) continue ;
         if (!drawcel.isDrawable(box)) continue ;
         
         // If drawing an unloaded cel when using pages as scenes then 
         // load all the cels in the new scene.  These cels are 
         // unloaded periodically by our SceneTimer activity that 
         // scans the cel list to free unused cel memory.
         
         if (OptionsDialog.getPagesAreScenes() && !drawcel.isLoaded())
         {
            Vector pages = drawcel.getPages() ;
            if (pages == null || pages.size() == 0) continue ;
            for (int j = 0 ; j < pages.size() ; j++)
            {
               o = pages.elementAt(j) ;
               if (!(o instanceof Integer)) continue ;
               Integer page = (Integer) o ;
               int p = page.intValue() ;
               if (OptionsDialog.getDebugLoad())
                  System.out.println("PanelFrame: unloaded cel is "+drawcel+", loading scene page "+p) ;
               SceneTimer.setPage(page) ;
               if (p > 0) loadCels(p,true) ;
               o = drawcel.getGroup() ;
               if (o instanceof Group)
               {
                  Rectangle r = ((Group) o).getBoundingBox() ;
                  box = box.union(r) ;
               }
            }
         }
         drawcel.draw(g2,box) ;
      }
   }



   // The redraw method is used when an alarm fires and a portion
   // of the screen need to be reconstructed.  The cels are drawn
   // to the base image then copied to the full image for display.

   synchronized void redraw(Rectangle box)
   {
      if (box == null) return ;

      // Watch for concurrent JavaCel draws that require paintComponent calls
      // while not on the AWT thread.  These can lead to hang conditions.

      if (!SwingUtilities.isEventDispatchThread())
      {
         boolean paint = false ;
         Vector comps = (config != null) ? config.getComponents() : null ;
         if (comps != null)
         {
            for (int i = 0 ; i < comps.size() ; i++)
            {
               if (((JavaCel) comps.elementAt(i)).mustPaint())
                  { paint = true ; break ; }
            }

            // Perform the redraw on the AWT thread if necessary.

            if (paint)
            {
               final Rectangle box1 = box ;
               Runnable runner = new Runnable()
               { public void run() { redraw(box1) ; } } ;
               javax.swing.SwingUtilities.invokeLater(runner) ;
               return ;
            }
         }
      }

      // Is a full redraw requested?

      if (redrawimage)
      {
         updateCelList() ;
         baseList = new int [celList.length] ;
         for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
         box = new Rectangle(imageArea) ;
         redrawimage = false ;
      }

      // Remove the group cels from the base picture if the mouse is down
      // and we are dragging an object.  The celList and baseList contain
      // integer indexes into the Cel vector, thus we remove the actual
      // index into the cel vector from the list.
/*
      if (mousedown && group != null && baseList != null)
      {
         for (int i = 0; i < group.getCelCount(); i++)
         {
            Cel c = group.getCel(i) ;
            int n = cels.indexOf(c) ;
            for (int j = 0 ; j < baseList.length ; j++)
               if (baseList[j] == n) { baseList[j] = -1 ; break ; }
         }
      }
*/
      // Draw within the supplied bounding box.

      draw(basegc,box,baseList,null) ;
      copy(fullgc,box,baseImage) ;
      int bx = (int) (box.x * sf) ;
      int by = (int) (box.y * sf) ;
      int bw = (int) Math.ceil(box.width * sf) + 1 ;
      int bh = (int) Math.ceil(box.height * sf) + 1 ;
      if (bx < 0) bx = 0 ;
      if (by < 0) by = 0 ;

      // Request a repaint.

      repaint(bx+x-windowOffset.x,by+y-windowOffset.y,bw,bh) ;
   }



   // The copy method copies a part of one image into another image.
   // This method is synchronized to prevent two different activities
   // from drawing to the same image at the same time.

   synchronized void copy(Graphics g, Rectangle box, Image image)
   {
      if (box == null) return ;
      if (image == null) return ;
      int x = box.x ;
      int y = box.y  ;
      int w = box.width ;
      int h = box.height ;
      if (x < 0) x = 0 ;
      if (y < 0) y = 0 ;
      
      // A workaround for Apple image distortion when cels exceed
      // the panel clip area.  Suspect this is due to a hardware
      // driver problem. Also suspect this fails if the window is
      // scaled and the solution is to use the imageArea and not 
      // the panelArea.  Note: this fix is required (Dec 15 2006)!
     
      if (OptionsDialog.getAppleMac())
      {
         if ((imageArea.width < x+w) || (imageArea.height < y+h))
         {
            int iw = w ; 
            int ih = h ;
            w = imageArea.width - x ;
            h = imageArea.height - y ;
            if (iw < w) w = iw ;
            if (ih < h) h = ih ;
         }
      }
      
      g.drawImage(image,x,y,x+w,y+h,x,y,x+w,y+h,null) ;
   }


   // The adjustViewport method scrolls the panel frame as required to
   // contain the specified area.  Note that the area is provided in
   // unscaled coordinates, whereas the viewport shows scaled images.

   private void adjustViewport(Rectangle sb)
   {
      if (sb == null) return ;
      JViewport jv = parent.getViewport() ;
      if (jv == null) return ;
      Rectangle view = jv.getViewRect() ;
      if (view == null) return ;
      sb = scaleRectangle(sb,sf) ;
      sb.x += x ;
      sb.y += y ;
      if (view.contains(sb)) return ;

      // Scroll the viewport.  Identify the delta change.  Constrain the
      // scrolling to the unscaled panel area including the border.

      Rectangle r = view.union(sb) ;
      Dimension d = new Dimension(panelSize) ;
      d.width += 4 ;
      d.height += 4 ;
      int dx = (r.x+r.width) - (view.x+view.width) + (r.x-view.x) ;
      int dy = (r.y+r.height) - (view.y+view.height) + (r.y-view.y) ;
      int x = view.x + dx ;
      int y = view.y + dy ;
      if (x+view.width > d.width) x = d.width - view.width ;
      if (y+view.height > d.height) y = d.height - view.height ;
      if (x < 0) x = 0 ;
      if (y < 0) y = 0 ;
      jv.setViewPosition(new Point(x,y)) ;
      repaint() ;
   }



   // Function to scale a rectangle.

   private Rectangle scaleRectangle(Rectangle r, float sf)
   {
      if (r == null) return r ;
      int x = (int) (r.x * sf) ;
      int y = (int) (r.y * sf) ;
      int w = (int) (r.width * sf) ;
      int h = (int) (r.height * sf) ;
      return new Rectangle(x,y,w,h) ;
   }



   // The getPixel function gets the top cel visible at the specified
   // coordinate point.  This function assumes that the cel list is
   // ordered by overlay priority so that the first cel found is the
   // top cel currently visible.  If we are over a video cel it is
   // always selected.

   private int getPixel(int x, int y)
   {
      // Find a cel in which our (x,y) point resides.  We scan the
      // cel list because this contains all the cels participating
      // on the current page. Transparent cel pixels are recognized.

      if (celList == null) return (-1) ;
      for (int i = celList.length-1 ; i >= 0 ; i--)
      {
         Cel c ;
         int celNumber = celList[i] ;
         try { c = (Cel) cels.elementAt(celNumber) ; }
         catch (ArrayIndexOutOfBoundsException e) { continue ; }
         if (!c.isVisible()) continue ;
         Rectangle r = c.getBoundingBox() ;
         int t = c.getAlpha(x-r.x,y-r.y) ;
         if (t < 0) continue ;
         int n = c.getTransparency() ;

         // Transparent pixels are not accepted if the pixel is the cel
         // transparent color.  GIF images appear to return the background
         // color if this the transparent pixel.  This is because the GIF
         // frame 0 buffered image is set to the background color in GifCel.

         if (t == 0 || (n == 0 && selection != null))
         {
            if (c.isTruecolor()) continue ;
            int rgb = c.getRGB(x-r.x,y-r.y) ;
            if (rgb < 0) continue ;
            Color tc = c.getTransparentColor() ;
            if (tc != null && rgb == (tc.getRGB() & 0xffffff)) continue ;
            if (c instanceof GifCel && tc != null)
            {
               Color bc = c.getBackgroundColor() ;
               if (bc != null && rgb == (bc.getRGB() & 0xffffff)) continue ;
            }
         }

         // Ghosted cels are recognized if they are edit selected.

         if (c.isGhosted())
         {
            boolean selected = false ;
            if (selection == null) continue ;
            for (int j = 0 ; j < selection.size() ; j++)
            {
               Object o = selection.elementAt(j) ;
               if (o instanceof Cel)
               {
                  if (o != c) continue ;
                  selected = true ;
               }
               if (o instanceof Group)
               {
                  Group g = (Group) o ;
                  Vector groupcels = g.getCels() ;
                  if (groupcels == null) continue ;
                  if (!groupcels.contains(c)) continue ;
                  selected = true ;
               }
               break ;
            }
            if (!selected) continue ;
         }
         return (celNumber) ;
      }
      return (-1);
   }



   // The getPixelBox function gets the top cel visible at the specified
   // coordinate point by checking for bounding box inclusion.  This function 
   // assumes that the cel list is  ordered by overlay priority so that the 
   // first cel found is the top cel currently visible.  If we are over a 
   // video cel it is always selected.

   private int getPixelBox(int x, int y)
   {
      // Find a cel in which our (x,y) point resides.  We scan the
      // cel list because this contains all the cels participating
      // on the current page. Transparent cel pixels are recognized.

      if (celList == null) return (-1) ;
      for (int i = celList.length-1 ; i >= 0 ; i--)
      {
         Cel c ;
         int celNumber = celList[i] ;
         try { c = (Cel) cels.elementAt(celNumber) ; }
         catch (ArrayIndexOutOfBoundsException e) { continue ; }
         if (!c.isVisible()) continue ;
         Rectangle r = c.getBoundingBox() ;
         if (!r.contains(x,y)) continue ;
         return (celNumber) ;
      }
      return (-1);
   }



   // The checkOverlap function is used to determine if two groups have
   // overlapping bounding boxes.  Groups overlap if the intersection
   // between any two cel bounding boxes in the groups is not empty.

   int checkOverlap(String s1, String s2)
   {
      collide = null ;
      KissObject g1 = findGroupOrCel(s1) ;
      KissObject g2 = findGroupOrCel(s2) ;
      Integer pid = (page == null) ? null : (Integer) page.getIdentifier() ;
      if (g1 == null || g2 == null || pid == null) return -1 ;
      if (!(g1.isVisible() && g2.isVisible())) return -1 ;
      if (!(g1.isOnPage(pid) && g2.isOnPage(pid))) return -1 ;
      if (!(g1.getBaseBoundingBox().intersects(g2.getBaseBoundingBox()))) return 0 ;

      // For all cels in each group, check if an overlap exists.
      // For cels to overlap they need not be visible but they must
      // be on this page.

      for (int i1 = 0 ; i1 < g1.getCelCount() ; i1++)
      {
         Cel c1 = g1.getCel(i1) ;
         if (c1 == null) continue ;
         for (int i2 = 0 ; i2 < g2.getCelCount() ; i2++)
         {
            Cel c2 = g2.getCel(i2) ;
            if (c2 == null) continue ;
            if (!(c1.isVisible() && c2.isVisible())) continue ;
            if (!(c1.isOnPage(pid) && c2.isOnPage(pid))) continue ;
            if (c1.getBaseBoundingBox().intersects(c2.getBaseBoundingBox()))
            {
               collide = new Object[2] ;
               collide[0] = c1 ;
               collide[1] = c2 ;
               return 1 ;
            }
         }
      }
      return 0 ;
   }



   // The checkTouch method is used to determine if two cels are touching.
   // Cels touch if they have non-transparent pixels that overlay one
   // another.  A cel can be found in more than one object, so we need
   // to check all possible variants within the moving group of interest.
   // Cels must be visible to touch.  The active press object is used
   // to differentiate the selected ambiguous cel.

   int checkTouch(String s1, String s2, Object g, FKissEvent event)
   {
      Object [] o ;
      Object [] pkt1 ;
      Object [] pkt2 ;
      collide = null ;
      if (s1 == null) return -1 ;
      if (s2 == null) return -1 ; 
      if (page == null) return -1 ;
      if (config == null) return -1 ;
      Integer pid = (Integer) page.getIdentifier() ;

      // Get the first cel.  Our parameter may be a CelGroup name.
      // For CelGroups our list will include all cels in the group.
      // Ambiguous cels are not checked for group or cel group parameters.

      o = (event != null) ? event.getCollision1() : null ;
      pkt1 = (o != null) ? o : getCelList(s1,config,new Object [2]) ;
      if (event != null) event.setCollision1(pkt1) ;
      Vector cellist1 = (Vector) pkt1[1] ;
      Integer celtype1 = (Integer) pkt1[0] ;
      if (cellist1 != null) 
      {
         Enumeration enum1 = cellist1.elements() ;
         while (enum1.hasMoreElements())
         {
            Cel c1 = (Cel) enum1.nextElement()  ;
            while (c1 != null)
            {

               // Identify the cel background and transparent colors.
               // Note that we convert pixels (0,0,0) to (0,0,1) so
               // fully black transparent settings are not 0, but 1.

               Color bc1 = c1.getBackgroundColor() ;
               Color tc1 = c1.getTransparentColor() ;
               int bcrgb1 = (bc1 != null) ? (bc1.getRGB() & 0xffffff) : -1 ;
               int tcrgb1 = (tc1 != null) ? (tc1.getRGB() & 0xffffff) : -1 ;
               if (tcrgb1 == 0) tcrgb1 = 1 ;

               // Check this cel against all possible occurances of the second cel.

               o = (event != null) ? event.getCollision2() : null ;
               pkt2 = (o != null) ? o : getCelList(s2,config,new Object [2] ) ;
               if (event != null) event.setCollision2(pkt2) ;
               Vector cellist2 = (Vector) pkt2[1] ;
               Integer celtype2 = (Integer) pkt2[0] ;
               if (cellist2 != null) 
               {
                  Enumeration enum2 = cellist2.elements() ;
                  while (enum2.hasMoreElements())
                  {
                     Cel c2 = (Cel) enum2.nextElement()  ;
                     while (c2 != null)
                     {
                        if (c1 == c2)
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }
                  
                        // Confirm that the cels are within the area of interest.
                        // Cels must be visible and on the current page.  For mouse
                        // pressed events we reference the object base location.

                        boolean caught1 = (g != null && (c1.getGroup().equals(g))) ;
                        boolean caught2 = (g != null && (c2.getGroup().equals(g))) ;
                        Rectangle r1 = (caught1) ? c1.getBaseBoundingBox() : c1.getBoundingBox() ;
                        Rectangle r2 = (caught2) ? c2.getBaseBoundingBox() : c2.getBoundingBox() ;
                        if (g != null && !(c1.getGroup().equals(g) || c2.getGroup().equals(g)))
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }
                        if (!(c1.isOnPage(pid) && c2.isOnPage(pid)))
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }
                        if (!(c1.isVisible() && c2.isVisible()))
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }
                        if (!(r1.intersects(r2)))
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }

                        // For these cels, check the pixels in the intersection area
                        // to see if an overlap exists.

                        Rectangle r = r1.intersection(r2) ;
                        int pixel1[] = c1.getPixels(r) ;
                        int pixel2[] = c2.getPixels(r) ;
                        if (pixel1 == null || pixel2 == null)
                        {
                           c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                           if (celtype2.intValue() != 1) c2 = null ;
                           continue ;
                        }

                        // Identify the second cel background and transparent colors.
                        // Note that we convert pixels (0,0,0) to (0,0,1) so
                        // fully black transparent settings are not 0, but 1.

                        Color bc2 = c2.getBackgroundColor() ;
                        Color tc2 = c2.getTransparentColor() ;
                        int bcrgb2 = (bc2 != null) ? (bc2.getRGB() & 0xffffff) : -1 ;
                        int tcrgb2 = (tc2 != null) ? (tc2.getRGB() & 0xffffff) : -1 ;
                        if (tcrgb2 == 0) tcrgb2 = 1 ;

                        // Scan the intersection area to find two overlapping
                        // non-transparent pixels.

                        for (int n = 0 ; n < pixel1.length ; n++)
                        {
                           if (n >= pixel2.length) break ;
                           int a1 = (pixel1[n] >> 24) & 255 ;
                           int a2 = (pixel2[n] >> 24) & 255 ;

                           // Transparent pixels are not really transparent
                           // unless they are the cel transparent color.

                           if (a1 == 0 && !c1.isTruecolor())
                           {
                              int rgb = pixel1[n] & 0xffffff ;
                              if (rgb == 0) rgb = 1 ;
                              if (!((rgb == bcrgb1) || (rgb == tcrgb1))) a1 = 1 ;
                           }
                           if (a2 == 0 && !c2.isTruecolor())
                           {
                              int rgb = pixel2[n] & 0xffffff ;
                              if (rgb == 0) rgb = 1 ;
                              if (!((rgb == bcrgb2) || (rgb == tcrgb2))) a2 = 1 ;
                           }
                           if (a1 > 0 && a2 > 0)
                           {
                              collide = new Object[2] ;
                              collide[0] = c1 ;
                              collide[1] = c2 ;
                              return 1 ;
                           }
                        }
                        c2 = Cel.findNextCel(c2,c2.getRelativeName(),config) ;
                        if (celtype2.intValue() != 1) c2 = null ;
                     }
                  }
               }
               c1 = Cel.findNextCel(c1,c1.getRelativeName(),config) ;
               if (celtype1.intValue() != 1) c1 = null ;
            }
         }
      }
      return 0 ;
   }



   // The checkClone method is used to correct event parameter strings
   // for cloned objects.  If an object was cloned it retains collision
   // events from the source object, however these events reference the
   // clone source object number in their parameter list.  This routine
   // constructs a new object string to identify the new cloned object.

   private String checkClone(Object g, String s1)
   {
      if (s1 == null) return s1 ;
      if (s1.length() == 0) return s1 ;
      if (!(s1.charAt(0) == '#')) return s1 ;
      if (!(g instanceof Group)) return s1 ;
      Object id = ((Group) g).getClone() ;
      if (!(id instanceof Integer)) return s1 ;
      if (!(s1.equals("#" + id.toString()))) return s1 ;
      id = ((Group) g).getIdentifier() ;
      if (!(id instanceof Integer)) return s1 ;
      return "#" + id.toString() ;
   }


   // Utility function to return a set of cels depending on the type of
   // an event parameter (group or cel group or cel).  This is used in the
   // checkTouch() function.  

   private Object [] getCelList(String s, Configuration c, Object [] pkt)
   {
      Vector v = null ;
      Integer type = new Integer(0) ;
      Cel c1 = Cel.findCel(s,c,null) ;
      if (c1 != null)
      {
         type = new Integer(1) ;
         v = new Vector() ;
         v.add(c1) ;
      }
      else
      {
         Group g = Group.findGroup(s,c,null) ;
         if (g != null)
         {
            type = new Integer(2) ;
            v = g.getAllCels() ;
         }
         else
         {
            CelGroup cg = CelGroup.findCelGroup(s,c,null) ;
            if (cg != null)
            {
               type = new Integer(3) ;
               v = cg.getCels() ;
            }
         }
      }
      pkt[0] = type ;
      pkt[1] = v ;
      return pkt ;
   }


   // Function to parse an event Group or Cel or Cel Group parameter.
   // These cannot be variables.

   private KissObject findGroupOrCel(String s)
   {
      Object o = Group.findGroup(s,config,null) ;
      if (o == null) o = Cel.findCel(s,config,null) ;
      if (o == null) o = CelGroup.findCelGroup(s,config,null) ;
      if (!(o instanceof KissObject)) return null ;
      return (KissObject) o ;
   }


   // Function to adjust a cel location offset due to a cel relocation.
   // Cels are relocated if cels are ungrouped and subsequently moved
   // to a new location.  A new offset is calculated as a displacement
   // from the group location.  The group is rebuilt and a group offset
   // of zero is established.  All cels in the group have their offsets
   // recalculated. This is a permanent update.

   private void relocateCel(Cel cel)
   {
      Object o = cel.getGroup() ;
      if (!(o instanceof Group)) return ;
      Group g = (Group) o ;

      // Relocate the object cel.

      Point cellocation = cel.getLocation() ;
      Point celoffset = cel.getOffset() ;
      Point grouplocation = g.getLocation() ;
      int x = (cellocation.x + celoffset.x) - grouplocation.x ;
      int y = (cellocation.y + celoffset.y) - grouplocation.y ;
      cel.setPlacement(g.getPlacementObject()) ;
      cel.setOffset(x,y) ;
      cel.setLocation(grouplocation) ;

      // Update the group bounding box.  This can change the group location.
      // We recompute a new group offset from its original location and
      // calculate the new relocated cel offset for all other cels in the group.

      g.updateBoundingBox() ;
      g.setOffset(new Point(0,0)) ;
      relocateGroupCels(g) ;

      // Rebuild the group bounding box to eliminate any group offset.
      // Indicate that the group has been updated due to an offset change.

      g.rebuildBoundingBox() ;
      g.eliminateOffset() ;
      g.setUpdated(true) ;
      
      // If our group offset was eliminated then the group location may
      // have changed. We must show a page change and a configuration 
      // change so that new initial locations are set for the group
      // on a restart or save.
      
      Point newlocation = g.getLocation() ;
      if (config != null) config.setUpdated(true) ;
      if (newlocation.x != grouplocation.x || newlocation.y != grouplocation.y)
      {
         if (!g.isInternal() && page != null)
            page.setInitialGroupPosition((Integer) g.getIdentifier(),newlocation) ;
      }
   }

   
   // Calculate the new relocated cel offset for all cels in the group.
   
   private void relocateGroupCels(Group g)
   {
      if (g == null) return ;
      Vector cels = g.getCels() ;
      if (cels == null) return ;
      Object cid = (config != null) ? config.getID() : null ;
      
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (c == null) continue ;
         Point cellocation = c.getLocation() ;
         Point celoffset = c.getOffset() ;
         Point grouplocation = g.getLocation() ;
         int x = (cellocation.x + celoffset.x) - grouplocation.x ;
         int y = (cellocation.y + celoffset.y) - grouplocation.y ;
         c.setOffset(x,y) ;
         c.setUpdated(true) ;
         c.setAdjustedOffset(new Point(x,y)) ;
         if (OptionsDialog.getWriteCelOffset() && c.isWriteableOffset())
            if (!c.hasDuplicateKey(c.getKeyTable(),cid,c.getPath().toUpperCase()))
               c.setBaseOffset(new Point(x,y)) ;
      }
   }


   // Function to set initial collision event states.  The collision state
   // is maintained in the event state table by event and event object.  
   // Every event has a collision state table as does the mouse up/down
   // activity. The event state table is used to track collisions across
   // FKiSS actions and the mouse up/down state table tracks collisions
   // for mouse activities. This function recursively sets states for all 
   // attached objects.  A state is not replaced if it has already been 
   // established.  The fire flag, if set, signals that this collision must 
   // fire regardless of object movement.  The visible flag, if set, indicates 
   // that the collision is a result of an object visibility change.

   void setCollisionState(Object o, Hashtable eventstate, boolean fire, boolean visible)
   {
      Vector evt = null ;
      if (!isVisible()) return ;
      if (eventstate == null) return ;
      if (!(o instanceof KissObject)) return ;
      KissObject g = (KissObject) o ;
      Point location = g.getLocation() ;

      // Save the collision state for all events associated with this object.

      if ((evt = g.getEvent("apart")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkTouch(s1,s2,g,event) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }
      
      if ((evt = g.getEvent("collide")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkTouch(s1,s2,g,event) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }

      if ((evt = g.getEvent("in")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkOverlap(s1,s2) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }

      if ((evt = g.getEvent("out")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkOverlap(s1,s2) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }

      if ((evt = g.getEvent("stillin")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkOverlap(s1,s2) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }

      if ((evt = g.getEvent("stillout")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String s1 = checkClone(g,event.getFirstParameter()) ;
            String s2 = checkClone(g,event.getSecondParameter()) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            Object [] collisionstate = (Object []) eventstate.get(key) ;
            int b = checkOverlap(s1,s2) ;
            if (b >= 0 && (collisionstate == null || OptionsDialog.getImmediateCollide()))
            {
               collisionstate = new Object[5] ;
               collisionstate[0] = g ;
               collisionstate[1] = (b != 0) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[2] = location ;
               collisionstate[3] = (fire) ? Boolean.TRUE : Boolean.FALSE ;
               collisionstate[4] = (visible) ? Boolean.TRUE : Boolean.FALSE ;
               eventstate.put(key,collisionstate) ;
            }
            else if (b >= 0 && fire)
               ((Object []) collisionstate)[3] = Boolean.TRUE ; 
         }
      }

      // Recurse for all attached children.

      if (!g.hasChildren()) return ;
      Vector children = g.getChildren() ;
      for (int i = 0 ; i < children.size() ; i++)
         setCollisionState(children.elementAt(i),eventstate,fire,visible) ;
   }


   // Function to fire any collision events that are required.  Initial
   // collide states are found in the eventstate table keyed on the event
   // and the event object.  This function recursively fires events on all
   // attached objects.  It also removes action fired events from the
   // mouse up/down eventstate table to eliminate duplicate event firings.

   Rectangle fireCollisionEvents(Object o, Hashtable eventstate, Thread thread, Rectangle box)
   {
      Vector evt = null ;
      if (!isVisible()) return box ;
      if (eventstate == null) return box ;
      if (!(o instanceof KissObject)) return box ;
      KissObject g = (KissObject) o ;
      
      // Check all collision events registered in our event state table.

      if ((evt = g.getEvent("apart")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [] && ((Object []) o).length > 1)
               o = ((Object []) o)[1] ;
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (((Boolean) o).booleanValue() && checkTouch(s1,s2,g,event) == 0)
               {
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      if ((evt = g.getEvent("collide")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [] && ((Object []) o).length > 1)
               o = ((Object []) o)[1] ;
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (!((Boolean) o).booleanValue() && checkTouch(s1,s2,g,event) == 1)
               {
                  event.setCollide(collide) ;
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      if ((evt = g.getEvent("in")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [] && ((Object []) o).length > 1)
               o = ((Object []) o)[1] ;
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (!((Boolean) o).booleanValue() && checkOverlap(s1,s2) == 1)
               {
                  event.setCollide(collide);
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      if ((evt = g.getEvent("stillin")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            boolean mapped = false ;
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [])
            {
               Boolean map = (Boolean) ((Object []) o)[4] ;
               mapped = map.booleanValue() ;
               o = ((Object []) o)[1] ;
            }
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (!mapped && checkOverlap(s1,s2) == 1)
               {
                  event.setCollide(collide) ;
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      if ((evt = g.getEvent("out")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [] && ((Object []) o).length > 1)
               o = ((Object []) o)[1] ;
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (((Boolean) o).booleanValue() && checkOverlap(s1,s2) == 0)
               {
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      if ((evt = g.getEvent("stillout")) != null)
      {
         for (int i = 0 ; i < evt.size() ; i++)
         {
            boolean mapped = false ;
            FKissEvent event = (FKissEvent) evt.elementAt(i) ;
            String key = "" + event.hashCode() + g.hashCode() ;
            o = eventstate.get(key) ;
            if (o == null) o = new Boolean(false) ;
            if (o instanceof Object [])
            {
               Boolean map = (Boolean) ((Object []) o)[4] ;
               mapped = map.booleanValue() ;
               o = ((Object []) o)[1] ;
            }
            if (o instanceof Boolean)
            {
               String s1 = checkClone(g,event.getFirstParameter()) ;
               String s2 = checkClone(g,event.getSecondParameter()) ;
               if (!mapped && checkOverlap(s1,s2) == 0)
               {
                  event.fireEvent(this,thread,g) ;
                  if (this.eventstate != null) this.eventstate.remove(key) ;
               }
               Rectangle ebb = event.getBoundingBox() ;
               if (box == null) box = ebb ;
               if (ebb != null) box = box.union(ebb) ;
               eventstate.remove(key) ;
            }
         }
      }

      // Recurse for all attached children.

      if (!g.hasChildren()) return box ;
      Vector children = g.getChildren() ;
      for (int i = 0 ; i < children.size() ; i++)
         box = fireCollisionEvents(children.elementAt(i),eventstate,thread,box) ;
      return box ;
   }


   // Function to set initial selection input state for a set of objects.
   // The input state applies to components that can accept user input.
   // These components cannot be manipulated through normal KiSS mouse
   // and keyboard commands.

   void setInputState(boolean state, KissObject o)
   {
      Vector v = new Vector() ;
      v.add(o) ;
      setInputState(state,v) ;
   }
   
   void setInputState(boolean state, Vector set)
   {
      if (set == null) return ;
      Integer pid = (Integer) page.getIdentifier() ;
      for (int i = 0 ; i < set.size() ; i++)
      {
         Object o = set.elementAt(i) ;
         if (o instanceof Cel)
         {
            Cel c = (Cel) o ;
            if (c.isOnPage(pid))
            {
               if (state) c.setInput(c.getInitInput()) ;
               else c.setInput(false) ;
            }
         }
         else if (o instanceof Group)
         {
            Vector cels = ((Group) o).getCels() ;
            for (int j = 0 ; j < cels.size() ; j++)
            {
               Cel c = (Cel) cels.elementAt(j) ;
               if (!c.isOnPage(pid)) continue ;
               if (state) c.setInput(c.getInitInput()) ;
               else c.setInput(false) ;
            }
         }
      }
   }


   // Method to show a status message in the main frame status bar.

   void showStatus(String s) { parent.showStatus(s) ; }


   // Method to show a trace message during mouse selection events.  This
   // tracks movement of mouse selected objects, but only if the option to 
   // show object selection is set as well as the debug option for control 
   // messages.

   void showTrace()
   {
      String trace = null ;
      if (group == null || cel == null) return ;
      if (!OptionsDialog.getDebugMouse()) return ;
      boolean selected = (selection != null &&
         (selection.contains(group) || selection.contains(cel)
          || (groupset != null && group == groupset))) ;
      trace = "Image " + celNum + " \"" + cel.getName() + "\"" ;
      Object o = group.getIdentifier() ;
      if (o != null) trace += ", Object " + o.toString() ;
      if (flexvalue != null && flexvalue.y > 0)
         trace += ", Flex " + flexvalue.y ;
      int transparency = 255 - cel.getTransparency() ;
      if (transparency < 0) transparency = 0 ;
      if (transparency > 255) transparency = 255 ;
      if (transparency != 0)
         trace += ", Transparency " + transparency ;
      Rectangle box = (selected)
         ? group.getBoundingBox() : cel.getBoundingBox() ;
      if (box != null)
      {
         trace += ", " + ((selected)
            ? "Object" + ((groupset != null && group == groupset) ? " Set" : "")
            : "Image") + " boundingbox [" ;
         trace += box.x + "," + box.y + "," + box.width + "," + box.height + "]" ;
      }
      showStatus(trace) ;
   }


   // Method to close our frame.

   void close()
   {
      String s = parent.getTitle() ;
      int i = s.indexOf(' ') ;
      if (i >= 0) s = s.substring(0,i) ;
      parent.setTitle(s.trim()) ;
		if (OptionsDialog.getDebugControl() && config != null)
  			System.out.println("PanelFrame close frame for configuration \"" + config.getName() + "\" (" + config.getID() + ")") ;

		// Flush all image data.  This cleans up our memory allocation.
/*
      if (cels != null)
      {
         for (i = 0 ; i < cels.size() ; i++)
         {
            Cel c = (Cel) cels.elementAt(i) ;
            c.unload() ;
         }
      }
*/      
      // Release resources.

      config = null ;
      cel = null ;
      group = null ;
      pko = null ;
      lastcel = null ;
      lastgroup = null ;
      page = null ;
      palette = null ;
      evt = null ;
      cels = null ;
      groups = null ;
      celList = null ;
      baseList = null ;
      incel = null ;
      ingroup = null ;
      groupset = null ;
      selection = null ;
      eventstate = null ;
      printimage = null ;
      if (fullgc != null) fullgc.dispose() ;
      if (basegc != null) basegc.dispose() ;
      if (fullImage != null) fullImage.flush() ;
      if (baseImage != null) baseImage.flush() ;

      // Empty the clipboard and clear the undo buffer.

      if (clipboard != null) clipboard.setContents(null,this) ;
      if (undo != null) undo.discardAllEdits() ;

      // Remove all added components.

      removeAll() ;

      // Remove listeners

      removeMouseListener(this) ;
      removeMouseMotionListener(this) ;
      removeKeyListener(this) ;
   }


   // Method to quit the current set.

   void quit() { parent.closepanel() ; }


   // Print setup method.  This method will construct a printable image for
   // each page in the KiSS set according to the specified page format.
   // The image snapshots are retained in the printimage list by page number.
   // Event handling is suspended while the snapshot is taken.

   public void printSetup(PageFormat pageformat)
   {
      if (config == null) return ;
      printimage = new Vector() ;
      PageSet currentpage = getPage() ;
      suspendEvents() ;
      setEnabled(false) ;

      // Create a print image for each page.

      for (int i = 0 ; i < config.getPageCount() ; i++)
      {
         PageSet page = config.getPage(i) ;
         if (page == null || !page.isVisible() || page.getGroupCount() == 0)
            { printimage.addElement(null) ;  continue ; }

         // Initialize the printable page.   This draws the page.

         initpage(i) ;
         draw(fullgc,imageArea,celList,null) ;

         // Compute the scale factor to fit the print page format.

         int dw = (int) (pageformat.getImageableWidth()) ;
         int dh = (int) (pageformat.getImageableHeight()) ;
         int sw = panelSize.width ;
         int sh = panelSize.height ;
         float scaleX = (sw > dw) ? (((float) sw) / dw) : 1.0f ;
         float scaleY = (sh > dh) ? (((float) sh) / dh) : 1.0f ;
         float scale = Math.max(scaleX,scaleY) ;
         int dsw = (int) (sw / scale) ;
         int dsh = (int) (sh / scale) ;

         // Create a scaled print image from the page image.

         GraphicsConfiguration gc = parent.getGraphicsConfiguration() ;
         Image pageimage = gc.createCompatibleImage(dsw,dsh) ;
         Graphics g = pageimage.getGraphics() ;
         if (fullImage != null) g.drawImage(fullImage,0,0,dsw,dsh,0,0,sw,sh,null) ;
         printimage.addElement(pageimage) ;
         g.dispose() ;
      }

      // Restore the panel frame and resume the event handler.

      Integer n = (Integer) currentpage.getIdentifier() ;
      int i = (n instanceof Integer) ? n.intValue() : 0 ;
      initpage(i) ;
      draw(fullgc,imageArea,celList,null) ;
      resumeEvents() ;
      setEnabled(true) ;
   }


   // Print interface method.  The panel image scaled to fit on the page
   // and centered if required.  Note that we return PAGE_EXISTS if
   // we have a page but it is not visible and rendered.

   public int print(Graphics g, PageFormat pageformat, int pageindex)
   {
      if (printimage == null) return Printable.NO_SUCH_PAGE ;
      if (pageindex < 0) return Printable.NO_SUCH_PAGE ;
      if (pageindex >= printimage.size()) return Printable.NO_SUCH_PAGE ;
      Object o = printimage.elementAt(pageindex) ;

      // Write the page header and footer.

      Font f = new Font(getFont().getName(),Font.PLAIN,9) ;
      FontRenderContext frc = new FontRenderContext(null,false,false) ;
      String name = (config != null) ? config.getName() : "" ;
      Rectangle2D r2 = f.getStringBounds(name,frc) ;
      Rectangle r = r2.getBounds() ;
      int pw = (int) pageformat.getWidth() ;
      int ih = (int) pageformat.getImageableHeight() ;
      int iy = (int) pageformat.getImageableY() ;
      int x = (pw - r.width) / 2 ;
      int y = iy ;
      g.setFont(f) ;
      g.setColor(Color.black) ;
      g.drawString(name,x,y+r.height) ;
      String s1 = Kisekae.getCaptions().getString("PageFooterText") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + (pageindex) + s1.substring(j1+1) ;
      r2 = f.getStringBounds(s1,frc) ;
      r = r2.getBounds() ;
      x = (pw - r.width) / 2 ;
      y = ih + iy - (r.height / 2) ;
      g.drawString(s1,x,y) ;

      // If there are no pages left we are done, otherwise we have a
      // blank page.

      if (!(o instanceof Image))
      {
         boolean another = false ;
         for (int i = pageindex+1 ; i < printimage.size() ; i++)
         {
            o = printimage.elementAt(i) ;
            if (!(o instanceof Image)) continue ;
            another = true ;
            break ;
         }
         if (another) return Printable.PAGE_EXISTS ;
         return Printable.NO_SUCH_PAGE ;
      }

      // Print the page image.

      Image img = (Image) o ;
      int dw = (int) (pageformat.getImageableWidth()) ;
      int dh = (int) (pageformat.getImageableHeight()) ;
      int dsw = img.getWidth(null) ;
      int dsh = img.getHeight(null) ;

      // Center the image on the page.

      int dx = (dw - dsw) / 2 + (int) pageformat.getImageableX() ;
      int dy = (dh - dsh) / 2 + (int) pageformat.getImageableY() ;
      g.drawImage(img,dx,dy,dx+dsw,dy+dsh,0,0,dsw,dsh,null) ;
      return Printable.PAGE_EXISTS ;
   }


   // Print close method.  This method will removes the print images
   // created for printing.

   public void printClose()
   {
      printimage = null ;
   }


   // Clipboard owner interface functions.

   public void lostOwnership(Clipboard cb, Transferable contents)
   {
      if (contents instanceof PanelEdit)
         ((PanelEdit) contents).removeAllElements() ;
   }


   // Reset the mouse press coordinates if we FKiSS moved a mouse object.
   // This positions the mouse in the middle of the object after the move 
   // so the mouse is not hovering in space.  But this causes the object to
   // jump on drag because we are not doing it correctly.  Also needs to 
   // use the visible bounding box.
   // Disabled in FKissAction 20230530.

   void resetDrag(KissObject kiss)
   {
      Group g = getGroup() ;
      if (g == null) return ;
      if (g != kiss) return ;
      Rectangle r = g.getBoundingBox() ;
      posX = (int) ((r.x + r.width/2) * sf + x) ;
      posY = (int) ((r.y + r.height/2) * sf + y) ;
   }



   // Event listeners for mouse up/down events.
   // -----------------------------------------

   // The action on a mouse down event is to determine which cel
   // is to be moved.  Transparent cels may be overlayed, so we
   // need to determine which one is visible and selected. Then
   // the list of base points for all grouped cels that must be
   // moved as a unit is then constructed.

   synchronized public void mousePressed(MouseEvent e)
   {
      if (mousedown) return ;
      if (config == null) return ;
      metadown = SwingUtilities.isRightMouseButton(e) ;
      mousedown = true ;
      enabledrag = true ;
      boolean selected = false ;
      restrictbox = null ;

      try
      {
         e.consume() ;
         requestFocus() ;
         Component source = (Component) e.getSource() ;
         Point p = SwingUtilities.convertPoint(source,e.getX(),e.getY(),this) ;
         int xmouse = p.x + windowOffset.x ;
         int ymouse = p.y + windowOffset.y ;

         // Keep track of the mouse down event coordinates for edit mode.

         editX = xmouse ;
         editY = ymouse ;

         // Identify the cel under the mouse.

         int px = (int) ((xmouse - x) / sf) ;
         int py = (int) ((ymouse - y) / sf) ;
         celNum = getPixel(px,py) ;

         // Determine the cel and group object of interest.

         Object o = null ;
         try { cel = (Cel) cels.elementAt(celNum) ; }
         catch (ArrayIndexOutOfBoundsException ex) { cel = null ; }
         if (cel != null) o = cel.getGroup() ;
         if (o instanceof Group) group = (Group) o ;
         if (cel != null) celbasesize = cel.getSize() ;
         if (cel != null) celbaselocation = cel.getLocation() ;
         
         // Determine if we are transitioning to edit mode.  If we are
         // editing then create a new selection box.  Edit mode is entered
         // through alt-clicks if we are using alt-key edits, or shift-clicks
         // if we are not using alt-key edits.

         boolean editswitch = (OptionsDialog.getAltEditDrag() && e.isAltDown())
             || (!OptionsDialog.getAltEditDrag() && e.isShiftDown()) ;
         if (editswitch && OptionsDialog.getTempEditEnable())
         {
            editmode = true ;
            selectbox = new Rectangle(px,py,0,0) ;
            priorbox = new Rectangle(selectbox) ;
            setCursor(selectcursor) ;
            repaint() ;
            return ;
         }
         
         // If we right clicked with no selection, unselect all.
         
         if (SwingUtilities.isRightMouseButton(e)&& OptionsDialog.getTempEditEnable())
         {
            if (cel == null && group == null && selection != null) 
            {
               unselectAll() ;
            }
         }

         // If we failed to select an object, ignore the mouse down event.

         if (cel == null || group == null) return ;

         // If we are editing and double clicked on a selected object then our
         // selection is not just the single object but is the selected set of
         // objects.  We can also multi-select if we press the shift key while
         // using alt-key edits, or the alt key if not using alt-key edits.

         selected = (selection != null &&
            (selection.contains(group) || selection.contains(cel))) ;
         boolean multiswitch = (OptionsDialog.getAltEditDrag() && e.isShiftDown())
            || (!OptionsDialog.getAltEditDrag() && e.isAltDown()) ;
         if (selected && (e.getClickCount() > 1 || multiswitch))
            group = groupset ;
         if (group == null) return ;

         // If right mouse button pressed, show a context dialog.
         // or a pop-up selection for choice.

         if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
         {
            if (OptionsDialog.getEditEnable() && OptionsDialog.getTempEditEnable())
               showpopup(e.getComponent(),e.getX(),e.getY(),cel,group) ;

            // This completes the mousePressed action.

            cel = null ;
            group = null ;
            return ;
         }

         // If we are not editing and our group has children create a
         // group set for drawing.  The selected group is used as the
         // primary group for event processing and state values. The 
         // hierarchy is flattened in that this groupset contains all
         // cels in the attachment tree and the group vector contains
         // all groups in the attachment tree.

         pko = group ;
         Point flex = pko.getFlex() ;
         boolean nostrongfix = !OptionsDialog.getDetachFix() ;
         nostrongfix = nostrongfix && (flex != null && flex.y > 0) ;
         if (!selected && group.isAttached() && !nostrongfix)
         {
            Group ag = group ;
            while (ag.getFlex() != null && ag.getFlex().y > 1 &&
                   ag.hasParent() && ag.isVisible())
               ag = (Group) ag.getParent() ;
            groupset = new Group() ;
            groupset.setIdentifier(ag.getIdentifier());
            groupset.setInternal(true) ;
            groupset.addElement(ag) ;
            groupset.setPrimaryGroup(group) ;
            groupset.setConstrain(ag.isConstrained()) ;
            groupset.setFlex(ag.getFlex()) ;
            group = groupset ;
            pko = ag ;
         }

         // If we are editing ungrouped cels then we will create a
         // temporary group object for the selected cel.

         if (selected && selection.contains(cel))
         {
            group = new Group() ;
            group.addElement(cel) ;
            group.rebuildBoundingBox() ;
            group.updateCelPlacement() ;
         }

         // Retain the original sticky flex value for this group.

         newflex = 0 ;
         originalflex = 0 ;
         Group primary = getGroup() ;
         Cel primarycel = cel ;
         if (primary == null) return ;
         location = primary.getLocation() ;
         flexvalue = primary.getFlex() ;
         flexdrop = primary.getUnfix() ;
         if (flexvalue != null) originalflex = flexvalue.y ;
         movable = sticky = fixed = locked = false ;
         
         // Decrement the fixed count. Establish the new movable state
         // for mouse down events.

         if (!selected)
         {
            if (primary != null && primary.isVisible())
            {
               if (flexvalue != null)
               {
                  if (OptionsDialog.getEarlyFix())
                     if (flexvalue.y < OptionsDialog.getMaxFlex())
                        flexvalue.y = flexvalue.y - 1 ;
                  if (flexvalue.y == 1) primary.setUnfix(true) ;
                  if (flexvalue.y < 0) flexvalue.y = 0 ;
                  if (flexvalue.y == 0) movable = true ;
                  if (!movable && flexvalue.y < OptionsDialog.getMaxFlex()) sticky = true ;
                  if (!movable && !sticky) fixed = true ;
                  if (fixed && flexvalue.y >= OptionsDialog.getMaxLock()) locked = true ;
                  newflex = flexvalue.y ;
               }
            }
         }

         // Remove the group cels from the base picture.  The celList and
         // baseList contain integer indexes into the Cel vector, thus we
         // remove the actual index into the cel vector from the list.

         for (int i = 0; i < group.getCelCount(); i++)
         {
            Cel c = group.getCel(i) ;
            int n = cels.indexOf(c) ;
            for (int j = 0 ; j < baseList.length ; j++)
               if (baseList[j] == n) { baseList[j] = -1 ; break ; }
         }

         // Draw the base image, which excludes the moving cels.
         // We only need to draw within our bounding box as the
         // base image should be complete.  We also establish the
         // prior group bounding box as this is used during image
         // painting to remove the moving group from the full scene
         // prior to drawing it at its new location.

         box = group.getBoundingBox() ;
         priorbox = new Rectangle(box) ;
         draw(basegc,box,baseList,null) ;

         // Keep track of the mouse down event coordinates.
         // Retain the visible part of the object for viewport scrolling.

         posX = xmouse ;
         posY = ymouse ;
         JViewport jv = parent.getViewport() ;
         if (jv != null)
         {
            Rectangle r = jv.getViewRect() ;
            r.x = (int) (r.x / sf) ;
            r.y = (int) (r.y / sf) ;
            r.width = (int) (r.width / sf) ;
            r.height = (int) (r.height / sf) ;
            scrollbox = box.intersection(r) ;
         }

         // Process mouse down events for this cel or group. Mouse
         // events only apply to the visible cel. If we are queuing
         // events collision states must be established now.

         if (!selected)
         {
            if (!OptionsDialog.getImmediateEvent())
            {
               eventstate = new Hashtable(10,1.0f) ;
               setCollisionState(pko,eventstate,false,false) ;
            }
            
            // Process mouse down events for this cel or group. Mouse
            // events only apply to the visible cel.
            
            if ((evt = primary.getEvent("press")) != null)
            {
               if (OptionsDialog.getImmediateEvent())
                  EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
               else
                  EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
            }
            
            if (!locked && ((evt = primary.getEvent("catch")) != null)
                  && (OptionsDialog.getCatchFixdrop() || movable))
            {
               if (OptionsDialog.getImmediateEvent())
                  EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
               else
                  EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
            }
            
            if (!movable && !locked && (evt = primary.getEvent("fixcatch")) != null)
            {
               if (OptionsDialog.getImmediateEvent())
                  EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
               else
                  EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
            }

            // Process mouse down events for this cel. JavaCel Components may not be 
            // enabled. This means that they should not respond to mouse events.
            
            if (primarycel.isEnabled())
            {
               if ((evt = primarycel.getEvent("press")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primarycel) ;
                  else
                     EventHandler.queueEvents(evt,Thread.currentThread(),primarycel) ;
               }
            
               if (!locked && ((evt = primarycel.getEvent("catch")) != null)
                     && (OptionsDialog.getCatchFixdrop() || movable))
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primarycel) ;
                  else
                     EventHandler.queueEvents(evt,Thread.currentThread(),primarycel) ;
               }
            
               if (!movable && !locked && (evt = primarycel.getEvent("fixcatch")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primarycel) ;
                  else
                     EventHandler.queueEvents(evt,Thread.currentThread(),primarycel) ;
               }
            }
            
            // Re-establish the movable state for the object. Immediate mouse
            // events may have altered the fix state. We decrement the new fix
            // state to apply the mouse down state change after modification.
         
            if ((originalflex < 0 && flexvalue != null) || !OptionsDialog.getEarlyFix())
            {
               originalflex = flexvalue.y ;
               movable = sticky = fixed = locked = false ;
               if (flexvalue.y < OptionsDialog.getMaxFlex())
                  flexvalue.y = flexvalue.y - 1 ;
               // The following appears to be a PlayFKiss bug where setfix()
               // on mouse fixed objects are ignored, except for setfix(#n,1).
               if (OptionsDialog.getImmediateUnfix())
                  if (newflex != 0) flexvalue.y = newflex ;
               if (flexvalue.y == 1) primary.setUnfix(true) ;
               if (flexvalue.y < 0) flexvalue.y = 0 ;
               if (flexvalue.y == 0) movable = true ;
               if (!movable && flexvalue.y < OptionsDialog.getMaxFlex()) sticky = true ;
               if (!movable && !sticky) fixed = true ;
               if (fixed && flexvalue.y >= OptionsDialog.getMaxLock()) locked = true ;
            }

            // Process unfix events on mouse down.  

            if (movable && originalflex == 1)
            {
               if ((evt = primary.getEvent("unfix")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                  else
                     EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
               }

               int i = 0 ;
               Cel c = null ;
               while ((c = primary.getCel(i++)) != null)
               {
                  boolean b = OptionsDialog.getVisibleUnfix() ;
                  if ((b && c.isVisible() ) || !b)
                  {
                     if ((evt = c.getEvent("unfix")) != null)
                     {
                        if (OptionsDialog.getImmediateEvent())
                           EventHandler.fireEvents(evt,this,Thread.currentThread(),c) ;
                        else
                            EventHandler.queueEvents(evt,Thread.currentThread(),c) ;
                     }
                  }
               }               
            }
                           
            // If we are not queuing events then we must establish object 
            // collision initial states at this time.  It has been confirmed 
            // that PlayFKiss does this after press events and unfix events 
            // are processed and objects are mapped. Note that collision events 
            // are attached to a group movement so we must check for cel 
            // collisions across the whole group. 

            if (OptionsDialog.getImmediateEvent())
            {
               eventstate = new Hashtable(10,1.0f) ;
               setCollisionState(pko,eventstate,false,false) ;
            }
         }

         // Cleanup the group flex attributes.

         flexdrop = false ;
         primary.setUnfix(false) ;
      }

      // Watch for stack overflow.

      catch (StackOverflowError ex)
      {
         System.out.println("PanelFrame: mouse pressed " + ex.toString()) ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("StackOverflowFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") +
            "\n" + ex.getMessage(),
            Kisekae.getCaptions().getString("StackOverflowFault"),
            JOptionPane.ERROR_MESSAGE) ;
         showpage() ;
      }

      // Watch for internal faults.

      catch (Throwable ex)
      {
         System.out.println("PanelFrame: Internal fault, mouse pressed.") ;
         ex.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError")
            + " - " + Kisekae.getCaptions().getString("ActionNotCompleted")
            + "\n" + ex.toString() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            ex.printStackTrace(ps) ;
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
         catch (Exception ex1) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Show the diagnostic trace.

      showTrace() ;
   }



   // On a mouse up event we correct the cel bounding boxes and
   // drop the selected group. Note that we can see sequential
   // mouse release events if the left and right mouse button
   // were pressed concurrently. We must ensure that we only
   // process the correct event for the recognized mouse press.

   synchronized public void mouseReleased(MouseEvent e)
   {
      if (!mousedown) return ;
      if (SwingUtilities.isRightMouseButton(e) != metadown) return ;
      if (config == null) return ;
      mousedown = false ;
      boolean snapback = false ;
      dragobject = null ;
      
      try
      {
         e.consume() ;
         Component source = (Component) e.getSource() ;
         Point p = SwingUtilities.convertPoint(source,e.getX(),e.getY(),this) ;
         int xmouse = p.x + windowOffset.x ;
         int ymouse = p.y + windowOffset.y ;

         // If we are editing then create our final selection set.
         // This contains all the groups in our selection box.

         if (editmode)
         {
            if (group != null) group.restoreCelPlacement() ;
            doSelection(e,selectbox,group,cel) ;
            if (selection != null) OptionsDialog.setTempEditEnable(true) ;

            // End our selection box.

            cel = null ;
            group = null ;
            selectbox = null ;
            setCursor(defaultcursor) ;
            parent.updateMenu();
            editmode = false ;
            repaint() ;
            return ;
         }

         // There is no need for right mouse clicks here.

         if (SwingUtilities.isRightMouseButton(e)) 
         {
            cel = null ;
            group = null ;
            return ;
         }

         // Keep track of the mouse up event coordinates.

         posX = xmouse ;
         posY = ymouse ;
         boolean selected = (selection != null && (group == groupset
            || selection.contains(group) || selection.contains(cel))) ;

         // If we dropped a sticky group, snap it back to the start.

         Point moveflex = (group != null) ? group.getFlex() : null ;
         if (!selected && moveflex != null && moveflex.y > 0)
            if (group != null)
            {
               p = new Point(group.getPlacement()) ;
               group.setPlacement(0,0,false) ;
               if (p.x != 0 || p.y != 0) snapback = true ;
            }

         // Correct the group bounding box to reflect our new position.
         // Group set objects may have moved between mouse down and mouse up
         // so we need to correct our bounding box.

         if (group != null)
         {
            Point placement = group.getPlacement() ;
            group.drop() ;
            if (group == groupset)
               group.updateBoundingBox() ;
            if (selected && groupset != null)
               groupset.updateBoundingBox() ;
            
            box = group.getBoundingBox() ;
            if (celList != null)
            {
               baseList = new int [celList.length] ;
               for (int i = 0 ; i < celList.length ; i++) baseList[i] = celList[i] ;
            }

            // Update the location of the group on the page.
/*    causes reset to fail on second time.  Not sure why this was added for pagesasscenes?     
            group.updatePageSetLocation(page) ;
*/
            // Capture this edit for undo/redo processing.

            if (selected && (placement.x != 0 || placement.y != 0))
            {
               KissObject kiss = group ;
               if (selected && selection.contains(cel)) kiss = cel ;
               UndoablePageEdit ce = new UndoablePageEdit(MOVE,page,kiss,placement) ;
               UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
               if (undo != null) undo.undoableEditHappened(evt) ;
               parent.updateMenu();
            }
         }

         // If we are changing the size of a component, create an undoable
         // size edit.

         Cursor cursor = source.getCursor() ;
         if (selected && !cursor.equals(dragcursor) && cel != null)
         {
            Dimension s = cel.getSize() ;
            if (s.width != celbasesize.width || s.height != celbasesize.height)
            {
               if (ArchiveFile.isImage(cel.getName())) cel.scaleImage(s) ;
               UndoableSizeEdit ce = new UndoableSizeEdit(SIZECEL,cel,celbasesize,s) ;
               UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
               if (undo != null) undo.undoableEditHappened(evt) ;
               parent.updateMenu();
            }
         }

         // Update our base image to include the moving cels.  We
         // must also update our full paint image to correctly show
         // the final location of the moving group.

         if (box != null)
         {
            if (sf != 1.0) box = new Rectangle(imageArea) ;
            Integer level = (group != null) ? group.getLevel() : null ;
            copy(fullgc,priorbox,baseImage) ;
            draw(basegc,box,celList,level) ;
            copy(fullgc,box,baseImage) ;
         }
         
         // If we have any restricted placements from dragging these must
         // be cleared.

         if (restrictbox != null && pko != null)
         {
            pko.clearRestrictedPlacement() ;
            pko.setPlacement(0,0,false) ;
            pko.rebuildMoveRestrictions() ;
         }
         
         // If we have any restricted glued objects we must update our parent
         // restrictions, otherwise the parent will not honour restrictions.

         if (pko != null && pko.isGlued() && pko.hasParent())
         {
            pko.rebuildMoveRestrictions() ;
         }

         // If we are editing ungrouped cels then we must destroy our
         // temporary group object for the identified cel.

         if (selected && selection.contains(cel))
         {
            relocateCel(cel) ;
            group = null ;
         }

         // Check for group overlap or collision events.  These event types
         // are processed before release or drop events because the collision
         // action can change the object fix state and thus invalidate any
         // drop or release event.

         Point loc = null ;
         boolean moved = false ;
         Group primary = getGroup() ;
         if (primary != null) loc = primary.getLocation() ;
         if (loc != null && location != null && !moved)
            moved = (loc.x != location.x || loc.y != location.y) ;
         if (!selected && moved)
            fireCollisionEvents(pko,eventstate,Thread.currentThread(),null) ;

         // Establish the current fix state of our object if the flex
         // value changed through any collision setfix actions since mouse down.

         if (flexvalue != null)
         {
            movable = sticky = fixed = locked = false ;
            if (flexvalue.y == 0) movable = true ;
            if (!movable && flexvalue.y < OptionsDialog.getMaxFlex()) sticky = true ;
            if (!movable && !sticky) fixed = true ;
            if (fixed && flexvalue.y >= OptionsDialog.getMaxLock()) locked = true ;
         }

         // Process mouse up events for this cel or group. If the group
         // has already been dropped because we went beyond the flex limits
         // then drop and fixdrop events will have been processed.

         if (!selected)
         {
            if (primary != null)
            {
               if ((evt = primary.getEvent("release")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
               }
               if (!locked && !flexdrop && ((evt = primary.getEvent("drop")) != null)
                     && (OptionsDialog.getDropFixdrop() || movable))
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
               }
               if (!movable && !locked && !flexdrop && ((evt = primary.getEvent("fixdrop")) != null))
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
               }
            }
            
            if (cel != null && cel.isEnabled())
            {
               if ((evt = cel.getEvent("release")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),cel) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
               }
               if (!locked && !flexdrop && ((evt = cel.getEvent("drop")) != null)
                     && (OptionsDialog.getDropFixdrop() || movable))
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),cel) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
               }
               if (!movable && !locked && !flexdrop && (evt = cel.getEvent("fixdrop")) != null)
               {
                  if (OptionsDialog.getImmediateEvent())
                     EventHandler.fireEvents(evt,this,Thread.currentThread(),cel) ;
                  else
                    EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
               }
            }
         }

         // Clear our state variables.

         lastcel = cel ;
         lastgroup = primary ;
         restrictbox = null ;
         box = null ;
         cel = null ;
         group = null ;
         scrollbox = null ;
         eventstate = null ;
         pko = null ;

         // If we always repaint on a mouse release, events that were keyed on
         // a mouse press and adjusted object visibility will show the changes
         // even though the event may not be complete.  But if we don't repaint, 
         // an FKiSS move of an object under the mouse that detaches fails to
         // draw the detached object.

         repaint() ;
      }

      // Watch for stack overflow.

      catch (StackOverflowError ex)
      {
         System.out.println("PanelFrame: mouse released " + ex.toString()) ;
         JOptionPane.showMessageDialog(parent,
            Kisekae.getCaptions().getString("StackOverflowFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") +
            "\n" + ex.getMessage(),
            Kisekae.getCaptions().getString("StackOverflowFault"),
            JOptionPane.ERROR_MESSAGE) ;
         showpage() ;
      }

      // Watch for internal faults.

      catch (Throwable ex)
      {
         System.out.println("PanelFrame: Internal fault, mouse released.") ;
         ex.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError")
            + " - " + Kisekae.getCaptions().getString("ActionNotCompleted")
            + "\n" + ex.toString() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            ex.printStackTrace(ps) ;
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
         catch (Exception ex1) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Clear the diagnostic trace.

      if (OptionsDialog.getDebugMouse()) showStatus(null) ;
   }



   // Event listeners for mouse movement events.
   // ------------------------------------------

   // On a mouse drag event we must measure the drag displacement
   // from the mouse down location and move the cel base by this
   // amount.  All grouped cels are moved concurrently.

   synchronized public void mouseDragged(MouseEvent e)
   {
      if (config == null) return ;
      if (!enabledrag) return ;

      try
      {
         e.consume() ;
         Component source = (Component) e.getSource() ;
         Point p = SwingUtilities.convertPoint(source,e.getX(),e.getY(),this) ;
         int xmouse = p.x + windowOffset.x ;
         int ymouse = p.y + windowOffset.y ;
         int px = (int) ((xmouse - x) / sf) ;
         int py = (int) ((ymouse - y) / sf) ;

         // Retain the current real time mouse coordinates.

         moveX = e.getX() ;
         moveY = e.getY() ;
         mouseX = px ;
         mouseY = py ;

         // If we are dragging and we do not have an active cel or group
         // then transition to edit mode.

         if (!editmode && group == null && cel == null)
         {
            boolean editswitch = (OptionsDialog.getAltEditDrag() && e.isAltDown())
                || (!OptionsDialog.getAltEditDrag() && e.isShiftDown()) ;
            if (editswitch)
            {
               editmode = true ;
               px = (int) ((editX - x) / sf) ;
               py = (int) ((editY - y) / sf) ;
               selectbox = new Rectangle(px,py,0,0) ;
               priorbox = new Rectangle(selectbox) ;
               setCursor(selectcursor) ;
            }
         }

         // If we are editing then update our selection box.

         if (editmode)
         {
            if (selectbox != null)
            {
               px = (int) ((xmouse - x) / sf) ;
               py = (int) ((ymouse - y) / sf) ;
               selectbox.width = px - selectbox.x ;
               selectbox.height = py - selectbox.y ;
            }

            // Set the overall drawing area for a more efficient repaint.
            // The viewport is scrolled if we move outside the current
            // viewing area.

            Rectangle drawbox = priorbox ;
            if (priorbox == null) drawbox = selectbox ;
            else if (selectbox != null) drawbox = priorbox.union(selectbox) ;
            if (drawbox == null) return ;
            adjustViewport(drawbox) ;

            // Repaint the scene.

            int bx = (int) (drawbox.x * sf) + x ;
            int by = (int) (drawbox.y * sf) + y ;
            int bw = (int) Math.ceil(drawbox.width * sf) + 1 ;
            int bh = (int) Math.ceil(drawbox.height * sf) + 1 ;
            if (bx < 0) bx = 0 ;
            if (by < 0) by = 0 ;
            repaint(bx-windowOffset.x,by-windowOffset.y,bw,bh) ;
            return ;
         }

         // Calculate the object displacement from its base position.

         if (SwingUtilities.isRightMouseButton(e)) return ;
         dragobject = group ;
         if (group == null || flexdrop) return ;
         int dispX = (int) ((xmouse - posX) / sf) ;
         int dispY = (int) ((ymouse - posY) / sf) ;
         boolean selected = (selection != null &&
            (selection.contains(group) || selection.contains(cel))) ;

         // If we are changing the size of a component, adjust the
         // cel characteristics.

         Cursor cursor = source.getCursor() ;
         if (selected && !cursor.equals(dragcursor))
         {
            if (cel != null && group != null && celbasesize != null)
            {
               p = new Point(celbaselocation) ;
               Dimension d = new Dimension(celbasesize) ;
               if (!cursor.equals(south) && !cursor.equals(north))
               {
                  if (cursor.equals(east) || cursor.equals(northeast) || cursor.equals(southeast))
                     { d.width += dispX ; dispX = 0 ; if (d.width < 0) d.width = 0 ; }
                  if (cursor.equals(west) || cursor.equals(northwest) || cursor.equals(southwest))
                     { if (dispX > d.width) dispX = d.width ; p.x += dispX ; d.width -= dispX ; }
                  if (cursor.equals(east) || cursor.equals(west))
                     dispY = 0 ;
               }
               if (!cursor.equals(east) && !cursor.equals(west))
               {
                  if (cursor.equals(north) || cursor.equals(northeast) || cursor.equals(northwest))
                     { if (dispY > d.height) dispY = d.height ; p.y += dispY ; d.height -= dispY ; }
                  if (cursor.equals(south) || cursor.equals(southeast) || cursor.equals(southwest))
                     { d.height += dispY ; dispY = 0 ; if (d.height < 0) d.height = 0 ; }
                  if (cursor.equals(north) || cursor.equals(south))
                     dispX = 0 ;
               }
               cel.setSize(d) ;
               cel.setLocation(p) ;
               group.updateBoundingBox() ;
               dispX = dispY = 0 ;
            }
         }

         // If we moved an object that was attached to another object
         // and not glued tight, then detach it from its parent object
         // and fire any detached events. Sticky objects are not detached.
         // Travel up the attachment chain to locate a detachable object.

         Group primary = getGroup() ;
         Point flex = (primary != null) ? primary.getFlex() : null ;
         boolean nostrongfix = !OptionsDialog.getDetachFix() ;
         nostrongfix = nostrongfix && (flex != null && flex.y > 0) ;
         if (!selected && !nostrongfix)
         {
            Group detachable = primary ;
            while (detachable != null && detachable.hasParent())
            {
               flex = detachable.getFlex() ;
               if (detachable == primary) flex = flexvalue ;
               if (!detachable.isGlued() && flex != null && flex.y == 0)
               {
                  detachable.detach() ;
                  break ;
               }
               Object o = detachable.getParent() ;
               detachable = (o instanceof Group) ? (Group) o : null ;
            }
         }

         // Compute distance travelled and compare with the maximum
         // flex value.  If the distance exceeds the maximum allowed,
         // snap the object back to the start. We check the flex value
         // for the moving object as this is not always the primary object.

         Point moveflex = group.getFlex() ;
         boolean fixed = moveflex != null && moveflex.y > 0 ;
         if (!selected && fixed)
         {
            float distance = dispX*dispX + dispY*dispY;
            float f = (float) (flexvalue.x-flexvalue.y)/flexvalue.x;
            int flexlimit = OptionsDialog.getStickyFlex() ;
            float limit = f*f*flexlimit*flexlimit;
            if (distance > limit)
            {
               dispX = dispY = 0;
               group.setPlacement(0,0) ;

               // Adjust the flex value if it has not been changed through
               // a setfix() action since mouse down.  If it was changed
               // any unfix() events would have been fired.  Drop events
               // will be fired on mouse release.

               if (originalflex > 0)
               {
                  flexdrop = true ;

                  // If we drop the object, we should fire any drop events
                  // that may apply now, rather than at mouse up time.

                  if (primary != null)
                  {
                     if (!locked && ((evt = primary.getEvent("drop")) != null)
                           && (OptionsDialog.getDropFixdrop() || movable))
                     {
                        if (OptionsDialog.getImmediateEvent())
                           EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                        else
                           EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
                     }
                     if (!movable && !locked && ((evt = primary.getEvent("fixdrop")) != null))
                     {
                        if (OptionsDialog.getImmediateEvent())
                           EventHandler.fireEvents(evt,this,Thread.currentThread(),primary) ;
                        else
                           EventHandler.queueEvents(evt,Thread.currentThread(),primary) ;
                     }
                  }
                  
                  if (cel != null)
                  {
                     if (!locked && ((evt = cel.getEvent("drop")) != null)
                           && (OptionsDialog.getDropFixdrop() || movable))
                     {
                        if (OptionsDialog.getImmediateEvent())
                           EventHandler.fireEvents(evt,this,Thread.currentThread(),cel) ;
                        else
                          EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
                     }
                     if (!movable && !locked && (evt = cel.getEvent("fixdrop")) != null)
                     {
                        if (OptionsDialog.getImmediateEvent())
                           EventHandler.fireEvents(evt,this,Thread.currentThread(),cel) ;
                        else
                          EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
                     }
                  }
               }
            }
         }

         // Constrain the movement to fit within any restrictions.
         // This can detach child objects from the moving parent if
         // the child is not glued to the parent and is not fixed.

         Vector detached = null ;
         boolean detach = OptionsDialog.getDetachRestricted() ;
         if (!selected && pko != null)
         {
            while (true)
            {
               Point location = pko.getLocation() ;
               Point restrictx = pko.getRestrictX() ;
               Point restricty = pko.getRestrictY() ;
               int x = location.x + dispX ;
               int y = location.y + dispY ;

               // Restrict the movement offset if necessary.

               boolean isX1 = false ; boolean isX2 = false ;
               boolean isY1 = false ; boolean isY2 = false ;
               if (restrictx != null && restrictx.x <= restrictx.y)
               {
                  if (x < restrictx.x) { isX1 = true ; x = restrictx.x ; }
                  if (x > restrictx.y) { isX2 = true ; x = restrictx.y ; }
               }
               if (restricty != null && restricty.x <= restricty.y)
               {
                  if (y < restricty.x) { isY1 = true ; y = restricty.x ; }
                  if (y > restricty.y) { isY2 = true ; y = restricty.y ; }
               }

               // If we are restricting movement then detach any child object
               // that is detachable and causing the restriction.  If such an
               // object exist recompute the movement restriction and retain
               // the child drop location.

               if (isX1 || isX2 || isY1 || isY2)
               {
                  KissObject child = KissObject.getRestrictionObject(pko,isX1,isX2,isY1,isY2) ;
                  Point flex1 = (child instanceof Group) ? ((Group) child).getFlex() : null ;
                  boolean glued = (child != null) ? child.isGlued() : false ;
                  boolean detachable = detach && !glued && 
                     (!OptionsDialog.getDetachFix() || (flex1 == null || flex1.y == 0)) ;
                  
                  if (child instanceof Group && detachable)
                  {
                     if (detached == null) detached = new Vector() ;
                     Point primaryloc = pko.getLocation() ;
                     Point childloc = child.getLocation() ;
                     int dropx = x - (primaryloc.x - childloc.x) ;
                     int dropy = y - (primaryloc.y - childloc.y) ;
                     Point droplocation = new Point(dropx,dropy) ;
                     Object [] detachitem = new Object[2] ;
                     detachitem[0] = (Group) child ;
                     detachitem[1] = droplocation ;
                     detached.addElement(detachitem) ;
                     child.detach() ;
                     continue ;
                  }
                  
                  // If we are not detaching on exceeding restriction bounds
                  // then set a restricted placement on the restriction object.
                  // This will stop it from moving as we drag.
                  
                  else if (child != null)
                  {
                     child.setRestrictedPlacement(x-location.x,y-location.y) ;
                     KissObject parent = child.getParent() ;
                     if (parent != null) parent.updateMoveRestrictions(null) ;
                     Rectangle r1 = child.getAttachedBoundingBox() ;
                     if (restrictbox == null) restrictbox = r1 ;
                     else restrictbox = restrictbox.union(r1) ;
                  }
                  
                  // No restriction object but we are restricted. As we did
                  // not detach we need to confirm our initial restrictions
                  // to verify if we are really restricted.
                  
                  x = location.x + dispX ;
                  y = location.y + dispY ;
                  restrictx = pko.getInitialRestrictX() ;
                  restricty = pko.getInitialRestrictY() ;
                  if (restrictx != null && restrictx.x <= restrictx.y)
                  {
                     if (x < restrictx.x) x = restrictx.x ; 
                     if (x > restrictx.y) x = restrictx.y ; 
                  }
                  if (restricty != null && restricty.x <= restricty.y)
                  {
                     if (y < restricty.x) y = restricty.x ; 
                     if (y > restricty.y) y = restricty.y ; 
                  }

                  // Adjust the final displacement offset if our top level
                  // object is restricted.

                  dispX = x - location.x ;
                  dispY = y - location.y ;
               }
               break ;
            }
         }

         // If we detached objects due to restriction moves remove
         // these objects from the movement set and drop them at their
         // detach point.  The base image will need to be corrected
         // and collision events may need to be fired.

         if (detached != null)
         {
            for (int i = 0 ; i < detached.size() ; i++)
            {
               Object [] detacheditem = (Object []) detached.elementAt(i) ;
               Group ko = (Group) detacheditem[0] ;
               Point droplocation = (Point) detacheditem[1] ;
               Point location = ko.getLocation() ;
               int x = droplocation.x - location.x ;
               int y = droplocation.y - location.y ;
               Group g = new Group() ;
               g.addElement(ko) ;
               g.setPlacement(x,y) ;
               g.drop() ;

               // Remove the dropped object from our moving group and add
               // the dropped object back into the set of base elements.
               // Compute the drawing box for the dropped cels.

               Rectangle drawbox = null ;
               group.removeElement(ko) ;
               Vector v = ko.getAllCels() ;
               for (i = 0; i < v.size() ; i++)
               {
                  Cel c = (Cel) v.elementAt(i) ;
                  int n = cels.indexOf(c) ;
                  if (n < 0) continue ;
                  if (drawbox == null) drawbox = c.getBoundingBox() ;
                  drawbox = drawbox.union(c.getBoundingBox()) ;
                  for (int j = 0 ; j < celList.length ; j++)
                     if (celList[j] == n) { baseList[j] = n ; break ; }
               }

               // Draw the base image to add the dropped cels.
               // We only need to draw within our bounding box as the
               // base image should be complete.

               draw(basegc,drawbox,baseList,null) ;
            }
         }

         // Constrain the group movement to fit inside the panel.

         if (group.isConstrained() || OptionsDialog.getConstrainMoves())
         {
            Point offset = group.getOffset() ;
            Point location = group.getLocation() ;
            Dimension groupsize = group.getSize() ;
            
            // Are we constraining to the visible area?
            
            if (OptionsDialog.getConstrainVisible() || restrictbox != null)
            {
               Object o = (page != null) ? page.getIdentifier() : null ;
               Integer n = (o instanceof Integer) ? (Integer) o : null ;
               Rectangle r = group.getVisibleBoundingBox(n) ;
               location = new Point(location.x+r.x,location.y+r.y) ;
               groupsize = new Dimension(r.width,r.height) ; 
            }
            
            // Restrict to the panel size if we can move this object.

            if (!selected && !fixed)
            {
               Dimension panelsize = new Dimension(getPanelSize()) ;
               panelsize.width = (int) (panelsize.width / sf) ;
               panelsize.height = (int) (panelsize.height / sf) ;
               if ((location.x + dispX) > (panelsize.width - groupsize.width))
                  dispX = panelsize.width - groupsize.width - location.x ;
               if ((location.y + dispY) > (panelsize.height - groupsize.height))
                  dispY = panelsize.height - groupsize.height - location.y ;
               if ((location.x - offset.x + dispX) < 0)
                  dispX = 0 - (location.x - offset.x) ;
               if ((location.y - offset.y + dispY) < 0)
                  dispY = 0 - (location.y - offset.y) ;
            }
         }

         // Constrain video movement to fit inside the window.

         if (cel instanceof Video)
         {
            Point offset = group.getOffset() ;
            Point location = group.getLocation() ;
            Dimension groupsize = group.getSize() ;
            Dimension windowsize = new Dimension(getPanelSize()) ;
            Point paneloffset = getOffset() ;
            paneloffset.x = (int) (paneloffset.x / sf) ;
            paneloffset.y = (int) (paneloffset.y / sf) ;
            windowsize.width = (int) (windowsize.width / sf) + paneloffset.x ;
            windowsize.height = (int) (windowsize.height / sf) + paneloffset.y ;
            
            if ((location.x + dispX) > (windowsize.width - groupsize.width))
               dispX = windowsize.width - groupsize.width - location.x ;
            if ((location.y + dispY) > (windowsize.height - groupsize.height))
               dispY = windowsize.height - groupsize.height - location.y ;
            if ((location.x + dispX) < -paneloffset.x)
               dispX = -paneloffset.x - location.x ;
            if ((location.y + dispY) < -paneloffset.y)
               dispY = -paneloffset.y - location.y ;
         }

         // Now move all cels in the group the requested distance.

         group.move(dispX,dispY) ;
         box = group.getBoundingBox() ;
         if (box == null) return ;

         // Adjust the scrollable viewport if necessary.  We move the
         // viewport by moving the scrollbox.  The scrollbox is the
         // portion of the selected object that was visible when the
         // mouse was pressed.

         if (scrollbox != null)
         {
            if (dispX != 0 || dispY != 0)
            {
               Rectangle sb = new Rectangle(scrollbox) ;
               sb.x += dispX ;
               sb.y += dispY ;
               if (OptionsDialog.getAutoScroll()) adjustViewport(sb) ;
            }
         }

         // Determine the overall drawing area.  Repaint the screen.

         if (restrictbox != null) box = box.union(restrictbox) ;
         Rectangle drawbox = (priorbox == null) ? box : priorbox.union(box) ;
         int bx = (int) (drawbox.x * sf) ;
         int by = (int) (drawbox.y * sf) ;
         int bw = (int) Math.ceil(drawbox.width * sf) + 1;
         int bh = (int) Math.ceil(drawbox.height * sf) + 1 ;
         if (bx < 0) bx = 0 ;
         if (by < 0) by = 0 ;
         // Full repaint required with attached objects when moved with FKiSS
         // See paintComponent() drag causes window paint artifacts
//       repaint(bx+x-windowOffset.x,by+y-windowOffset.y,bw,bh) ;
         repaint() ;
      }

      // Watch for internal faults.

      catch (Throwable ex)
      {
         System.out.println("PanelFrame: Internal fault, mouse dragged.") ;
         ex.printStackTrace() ;
         String s = Kisekae.getCaptions().getString("InternalError")
            + " - " + Kisekae.getCaptions().getString("ActionNotCompleted")
            + "\n" + ex.toString() ;
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;

         // Catch the stack trace.

         try
         {
            File f = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(f) ;
            PrintStream ps = new PrintStream(os) ;
            ex.printStackTrace(ps) ;
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
         catch (Exception ex1) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(parent, s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
      }

      // Show the diagnostic trace.

      showTrace() ;
   }


   // On a mouse movement we need to retain the real-time mouse
   // coordinates.  We also process mousein and mouseout events.

   public void mouseMoved(MouseEvent e)
   {
      if (config == null) return ;
      if (editmode) return ;
      if (e.getX() == moveX && e.getY() == moveY) return ;
      Component source = (Component) e.getSource() ;
      Cursor cursor = source.getCursor() ;
      if (cursor.equals(waitcursor)) return ;
      moveX = e.getX() ;
      moveY = e.getY() ;

      Point p = SwingUtilities.convertPoint(source,e.getX(),e.getY(),this) ;
      int xmouse = p.x + windowOffset.x ;
      int ymouse = p.y + windowOffset.y ;
      int px = (int) ((xmouse - x) / sf) ;
      int py = (int) ((ymouse - y) / sf) ;

      // Retain the current real time mouse coordinates.

      mouseX = px ;
      mouseY = py ;

      // See if this is a movable cel.  We show a drag cursor
      // if the object can be moved, a press cursor if the object
      // is fixed and has a mouse event attached to it, and a normal
      // cursor if we are not over an object.

      celNum = getPixel(px,py) ;
      if (celNum >= 0)
      {
         Cel cel = null ;
         try { cel = (Cel) cels.elementAt(celNum) ; }
         catch (ArrayIndexOutOfBoundsException ex) { cel = null ; }
         source.setCursor(defaultcursor) ;
         if (cel == null) return ;

         // Show size change cursors if we are over the boundary of
         // an ungrouped selected cel.

         if (isSelected(cel) && selection.contains(cel))
         {
            Rectangle r = cel.getBoundingBox() ;
            Point p1 = new Point(px-r.x,py-r.y) ;
            if (p1.x >= 0 && p1.x <= 2)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)) ;
            if (p1.y >= 0 && p1.y <= 2)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)) ;
            if (p1.x <= r.width-1 && p1.x >= r.width-3)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) ;
            if (p1.y <= r.height-1 && p1.y >= r.height-3)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)) ;
            if (p1.x >= 0 && p1.x <= 2 && p1.y >= 0 && p1.y <= 2)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)) ;
            if (p1.x <= r.width-1 && p1.x >= r.width-3 && p1.y >= 0 && p1.y <= 2)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)) ;
            if (p1.x >= 0 && p1.x <= 2 && p1.y <= r.height-1 && p1.y >= r.height-3)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR)) ;
            if (p1.x <= r.width-1 && p1.x >= r.width-3 && p1.y <= r.height-1 && p1.y >= r.height-3)
               source.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR)) ;
            Cursor c = source.getCursor() ;
            if (!(c.equals(defaultcursor))) return ;
         }

         // Determine if the object is movable.

         Object o = cel.getGroup() ;
         Group group = (o instanceof Group) ? (Group) o : null ;
         boolean selected = (selection != null &&
            (selection.contains(group) || selection.contains(cel))) ;
         if (group != null)
         {
            Group ag = group ;
            while (ag.getFlex() != null && ag.getFlex().y > 0 &&
                   ag.hasParent() && ag.isVisible())
               ag = (Group) ag.getParent() ;
            Point flexvalue = ag.getFlex() ;
            if (selected || flexvalue.y < OptionsDialog.getMaxFlex())
               source.setCursor(dragcursor) ;
            else if (group.hasActionEvent())
               source.setCursor(presscursor) ;

            // Process mouse events for this group.  Mousein events are
            // triggered once only when we first enter the group.  Mouseout
            // events are triggered if we change to a new group.  We can only
            // be in one group at a time.

            if (!selected)
            {
               if (ingroup != null && ingroup != group)
               {
                  Vector outevt = ingroup.getEvent("mouseout") ;
                  if (outevt != null)
                     EventHandler.queueEvents(outevt,Thread.currentThread(),ingroup) ;
               }
               if ((evt = group.getEvent("mousein")) != null)
               {
                  if (ingroup != group)
                     EventHandler.queueEvents(evt,Thread.currentThread(),group) ;
               }
               ingroup = group ;
            }
         }

         // Determine if the cel has an event.

         if (cel.hasActionEvent())
            source.setCursor(presscursor) ;

         // Process mouse events for this cel.  Mousein events are
         // triggered once only when we first enter the cel.  Mouseout
         // events are triggered if we change to a new cel.  We can only
         // be in one cel at a time.

         if (!selected)
         {
            if (incel != null && incel != cel)
            {
               Vector outevt = incel.getEvent("mouseout") ;
               if (outevt != null)
                  EventHandler.queueEvents(outevt,Thread.currentThread(),incel) ;
            }
            if ((evt = cel.getEvent("mousein")) != null)
            {
               if (incel != cel)
                  EventHandler.queueEvents(evt,Thread.currentThread(),cel) ;
            }
            incel = cel ;
         }
         return ;
      }

      // No special cases or not over an object.  If we were in a group
      // or cel, trigger any mouseout events that are required.

      if (ingroup != null)
      {
         Vector outevt = ingroup.getEvent("mouseout") ;
         if (outevt != null)
            EventHandler.queueEvents(outevt,Thread.currentThread(),ingroup) ;
      }
      if (incel != null)
      {
         Vector outevt = incel.getEvent("mouseout") ;
         if (outevt != null)
            EventHandler.queueEvents(outevt,Thread.currentThread(),incel) ;
      }

      // Set a normal cursor.

      incel = null ;
      ingroup = null ;
      source.setCursor(defaultcursor) ;
   }

   public void mouseEntered(MouseEvent e) { }
   public void mouseExited(MouseEvent e) { }
   public void mouseClicked(MouseEvent e) { }


   // A utility function to release mouse control.  The optional
   // boolean parameter, if true, forces a mouse release regardless
   // of state.

   void releaseMouse() { releaseMouse(false) ; }
   void releaseMouse(boolean b)
   {
      if (!b && !mousedown) return ;
      if (!b && OptionsDialog.getDragMove()) return ;
      mouseReleased(new MouseEvent(this,0,0,0,moveX,moveY,0,false)) ;
      setCursor(defaultcursor) ;
  }



   // Event listeners for keyboard events.
   // ------------------------------------

   // Keyboard events are delivered to the component with active focus.
   // The panel frame consumes all events that are registered for the
   // data set.

   public void keyTyped(KeyEvent e)
   {
      boolean b = fireKeyEvent(e,"keytype") ;
      if (b) e.consume() ;
   }

   public void keyReleased(KeyEvent e)
   {
      boolean b = fireKeyEvent(e,"keyrelease") ;
      if (b) e.consume() ;
   }

   public void keyPressed(KeyEvent e)
   {
      boolean b = fireKeyEvent(e,"keypress") ;
      if (!b) b = doKeyControl(e) ;
      if (b) e.consume() ;
   }


   // A utility function to determine if the key event is a registered
   // data set event.  If so, then queue the event for processing.

   private synchronized boolean fireKeyEvent(KeyEvent e, String type)
   {
      char c = e.getKeyChar() ;
      int n = e.getKeyCode() ;
      String s = e.getKeyText(n) ;
      keymodifier = e.getKeyModifiersText(e.getModifiers()) ;
      boolean b = (n == KeyEvent.VK_ENTER) || (n == KeyEvent.VK_SPACE) ;
      b = b || (n == KeyEvent.VK_BACK_SPACE) || (n == KeyEvent.VK_DELETE) ;
      b = b || (n == KeyEvent.VK_TAB) || (n == KeyEvent.VK_INSERT) ;
      b = b || (n == KeyEvent.VK_HOME) || (n == KeyEvent.VK_END) ; 
      b = b || (n == KeyEvent.VK_PAGE_DOWN) || (n == KeyEvent.VK_PAGE_UP) ; 
      b = b || (n == KeyEvent.VK_ESCAPE) || (n == KeyEvent.VK_SCROLL_LOCK) ; 
      b = b || (n == KeyEvent.VK_PAUSE) || (n == KeyEvent.VK_PRINTSCREEN) ; 
      
      // Retain key types for keypressed events only.

      if ("keypress".equals(type))
      {
         keychar = c ;
         keycode = n ;
         keystring = s ;
         if (newkeytext) keytextline = "" ;
         newkeytext = (keycode == KeyEvent.VK_ENTER) ;
         if (newmultikey) multikey = "" ;
         int i = multikey.indexOf(s) ;
         if (i < 0) multikey += s ;
         newmultikey = false ;
         if (!b && Character.isDefined(c)) s = "" + c ;
      }

      // Retain the modifiers and keyboard line for keytyped events.
      // Keytyped events are fired upon release of the key.

      if ("keytype".equals(type))
      {
         keymodifier = e.getKeyModifiersText(e.getModifiers()) ;
         if (keycode == KeyEvent.VK_BACK_SPACE)
         {
            int n1 = keytextline.length() ;
            if (n1 > 0) keytextline = keytextline.substring(0,n1-1) ;
         }
         if (!Character.isISOControl(c)) 
         {
            if (!b && Character.isDefined(c))
            {
               keytextline += c ;
               s = "" + c ;
            }
            else if (keycode == KeyEvent.VK_SPACE)
            {
               keytextline += c ;
            }
         }
         else
            s = keystring ;
      }

      // Indicate multikey input is finished on a key release event.

      if ("keyrelease".equals(type))
      {
         newmultikey = true ;
         if (!b && Character.isDefined(c)) s = "" + c ;
      }

      // Do we have any registered events for this event type?

      EventHandler handler = (config == null) ? null : config.getEventHandler() ;
      Vector evt = (handler == null) ? null : handler.getEvent(type) ;
      if (evt == null) return false ;

      // Find the actual event for the key code.  A zero length string event
      // parameter is a registered event to capture any keystroke.

      Vector keyevents = new Vector() ;
      for (int i = 0 ; i < evt.size() ; i++)
      {
         FKissEvent event = (FKissEvent) evt.elementAt(i) ;
         String v = event.getFirstParameter() ;
         v = Variable.getStringLiteralValue(v) ;
         if ("\\\"".equals(v)) v = "\"" ;
         if ("\\\'".equals(v)) v = "\'" ;
         if ("\\\\".equals(v)) v = "\\" ;
         if (v == null) continue ;

         // Adjust for case sensitive comparisons.  Keypress and keyrelease
         // are not case sensitive.  Keytype can be depending on options.

         if (!OptionsDialog.getKeyCase())
         {
            v = v.toUpperCase() ;
            s = s.toUpperCase() ;
         }
         if ("keypress".equals(type) || "keyrelease".equals(type))
         {
            v = v.toUpperCase() ;
            s = s.toUpperCase() ;
         }

         // Recognize the event if we have the correct key.

         v = v.trim() ;
         s = s.trim() ;
         if (v.length() == 0) keyevents.addElement(event) ;
         else if (v.equals(s)) keyevents.addElement(event) ;
      }

      // Run the events asynchronously.

      if (keyevents.size() == 0) return false ;
      EventHandler.queueEvents(keyevents,Thread.currentThread(),null) ;
      return true ;
   }


   // A utility function to determine if the key event is a page or
   // color control event.  Page Up, Page Down, Home and End change
   // pages. Alt versions change color sets.  Del and Ins cut and paste
   // the current selection set.  Plus and minus keys adjust selected
   // object layering.

   private boolean doKeyControl(KeyEvent e)
   {
      boolean applemac = OptionsDialog.getAppleMac() ;
      if (config == null) return false;

      // Ensure that we have a key of interest.  If the data set handled
      // the key then page control key processing will not occur.

      switch (e.getKeyCode())
      {
         case KeyEvent.VK_PAGE_DOWN:
         case KeyEvent.VK_PAGE_UP:
         case KeyEvent.VK_HOME:
         case KeyEvent.VK_END:
         case KeyEvent.VK_DELETE:
         case KeyEvent.VK_INSERT:
            break ;
         case KeyEvent.VK_ADD:                     // Numpad +
         case KeyEvent.VK_SUBTRACT:                // Numpad -
            if (!applemac) break ;
            if (e.isShiftDown()) break ;
         case KeyEvent.VK_ESCAPE:                  // Show menu bar
            if (parent != null && parent.getMenu() == null)
            {
               if (parent.getPanelMenu() != null) parent.setMenu(parent.getPanelMenu()) ;
               else parent.setMenu(parent.getMainMenu()) ; 
               OptionsDialog.setInitMenubar(true);
            }                  
         default:
            return false ;
      }

      // Determine our context.

      Integer pg = null ;
      PageSet page = getPage() ;
      Palette palette = getPalette() ;
      Integer mp = getMultiPalette() ;
      if (page != null) pg = (Integer) page.getIdentifier() ;
      int pages = (config == null) ? 0 : config.getPageCount() ;
      int palettes = (config == null) ? 0 : config.getPaletteGroupCount() ;

      switch (e.getKeyCode())
      {
         // Page Down key shows the next page set or color set.

         case KeyEvent.VK_PAGE_DOWN:
            if (!e.isAltDown())
            {
               if (pg == null) return false ;
               if (pages <= 1) return false ;
               int start = pg.intValue() ;
               int p = start + 1 ;
               if (p >= pages) p = 0 ;
               while (p != start)
               {
                  if (p >= pages) p = 0 ;
                  PageSet next = config.getPage(p) ;
                  if (next.getGroupCount() > 0 && next.isVisible()) break ;
                  p++ ;
               }
               if (p != start)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initpage(p) ;
               }
            }
            else
            {
               if (mp == null) return false ;
               if (palettes <= 1) return false ;
               int start = mp.intValue() ;
               int p = start + 1 ;
               if (p >= palettes) p = 0 ;
               while (p != start)
               {
                  if (p >= palettes) p = 0 ;
                  if (palette.isVisible(p)) break ;
                  p++ ;
               }
               if (p != start)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initcolor(new Integer(p));
               }
            }
            break ;

         // Page Up key shows the prior page set or color set.

         case KeyEvent.VK_PAGE_UP:
            if (!e.isAltDown())
            {
               if (pg == null) return false ;
               if (pages <= 1) return false ;
               int start = pg.intValue() ;
               int p = start - 1 ;
               while (p != start)
               {
                  if (p < 0) p = pages - 1 ;
                  if (p < 0) { p = start ; break ; }
                  PageSet next = config.getPage(p) ;
                  if (next.getGroupCount() > 0 && next.isVisible()) break ;
                  p-- ;
               }
               if (p != start)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initpage(p) ;
               }
            }
            else
            {
               if (mp == null) return false ;
               if (palettes <= 1) return false ;
               int start = mp.intValue() ;
               int p = start - 1 ;
               while (p != start)
               {
                  if (p < 0) p = palettes - 1 ;
                  if (palette.isVisible(p)) break ;
                  p-- ;
               }
               if (p != start)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initcolor(new Integer(p));
               }
            }
            break ;

         // Home key shows the first page set or color set.

         case KeyEvent.VK_HOME:
            if (!e.isAltDown())
            {
               if (pg == null) return false ;
               if (pages <= 1) return false ;
               int p = 0 ;
               while (p < pages)
               {
                  PageSet next = config.getPage(p) ;
                  if (next.getGroupCount() > 0 && next.isVisible()) break ;
                  p++ ;
               }
               if (p < pages)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initpage(p) ;
               }
            }
            else
            {
               if (mp == null) return false ;
               if (palettes <= 1) return false ;
               int p = 0 ;
               while (p < palettes)
               {
                  if (palette.isVisible(p)) break ;
                  p++ ;
               }
               if (p < palettes)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initcolor(new Integer(p));
               }
            }
            break ;

         // End key shows the last page set or color set.

         case KeyEvent.VK_END:
            if (!e.isAltDown())
            {
               if (pg == null) return false ;
               if (pages <= 1) return false ;
               int p = pages - 1 ;
               while (p >= 0)
               {
                  PageSet next = config.getPage(p) ;
                  if (next.getGroupCount() > 0 && next.isVisible()) break ;
                  p-- ;
               }
               if (p >= 0)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initpage(p) ;
               }
            }
            else
            {
               if (mp == null) return false ;
               if (palettes <= 1) return false ;
               int p = palettes - 1 ;
               while (p >= 0)
               {
                  if (palette.isVisible(p)) break ;
                  p-- ;
               }
               if (p >= 0)
               {
                  setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
                  initcolor(new Integer(p));
               }
            }
            break ;

         // Del key cuts the selection set.

         case KeyEvent.VK_DELETE:
            editCut(selection) ;
            break ;

         // Ins key pastes the clipboard selection set.

         case KeyEvent.VK_INSERT:
            if (clipboard != null) editPaste(clipboard.getContents(this)) ;
            break ;

         // Numpad Plus keys move selected objects forward.

         case KeyEvent.VK_ADD:
            if (e.isShiftDown() || !applemac) 
               moveLevel(selection,+1) ;
            break ;

         // Numpad Minus keys move selected objects backward.

         case KeyEvent.VK_SUBTRACT:
            if (e.isShiftDown() || !applemac) 
               moveLevel(selection,-1) ;
            break ;
      }

      // Show the new page or color set.

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      showpage() ;
      return true ;
   }
   
   
   // This is a utility method to select objects within the set.  It is
   // typically invoked through a mouse select operation or through
   // context pop-up selection.  It creates an undoable selection.
   
   private void doSelection(AWTEvent e, Rectangle selectbox, Group group, Cel cel)
   {
      if (selectbox == null) return ;

      // Construct our selection set as specified by our selection box.

      boolean ungrouped = false ;
      PanelEdit oldselection = null ;
      PanelEdit selectset = new PanelEdit() ;
      if (selection != null)
      {
         oldselection = (PanelEdit) selection.clone() ;
         if (selection.size() > 0 && (selection.elementAt(0) instanceof Cel))
            ungrouped = true ;
      }

      // If we are not ungrouped then pick up all groups in the box
      // even if they are fully covered by another group.

      if (!ungrouped)
      {
         if (groups != null && page != null)
         {
            for (int i = 0 ; i < groups.size() ; i++)
            {
               Group g = (Group) groups.elementAt(i) ;
               if (!selectbox.contains(g.getBoundingBox())) continue ;
               if (!g.isOnPage((Integer) page.getIdentifier())) continue ;
               if (!g.isVisible()) continue ;
               selectset.addElement(g);
            }
         }

         // If our selection set is empty but we clicked on a group
         // and did not drag the mouse then select this group.

         if (selectset.size() == 0 && group != null)
            if (selectbox.width <=1 && selectbox.height <= 1)
               selectset.addElement(group) ;
      }

      // If we are ungrouped select only those cels from the
      // ungrouped set that are in the box.

      else
      {
         if (selection != null)
         {
            for (int i = 0 ; i < selection.size() ; i++)
            {
               Object o = selection.elementAt(i) ;
               if (!(o instanceof Cel)) continue ;
               Cel c = (Cel) o ;
               if (!selectbox.contains(c.getBoundingBox())) continue ;
               selectset.addElement(c);
            }
         }

         // If our selection set is empty but we clicked on a selected
         // cel and did not drag the mouse then select this cel.

         if (selectset.size() == 0 && cel != null)
            if (selectbox.width <=1 && selectbox.height <= 1)
               if (selection.contains(cel))
                  selectset.addElement(cel) ;
      }

      // Establish our event control key states.

      boolean ctrldown = (keycode == KeyEvent.VK_CONTROL && !newmultikey) ;
//      boolean ctrldown = false ;
//      if (e instanceof MouseEvent) 
//         ctrldown = ((MouseEvent) e).isControlDown() ;
//      else if (e instanceof ActionEvent) 
//         ctrldown = (((ActionEvent) e).getModifiers() & ActionEvent.CTRL_MASK) != 0 ;
      boolean metadown = (keycode == KeyEvent.VK_META && !newmultikey) ;
//      boolean metadown = false ;
//      if (e instanceof MouseEvent) 
//      {
//         metadown = ((MouseEvent) e).isMetaDown() ;
//      }
//      else if (e instanceof ActionEvent) 
//      {
//         metadown = (((ActionEvent) e).getModifiers() & ActionEvent.META_MASK) != 0 ;
//      }
      
      // If we are ungrouped but did not select anything in out ungrouped set,
      // then this is a selection of a new group object. Set up to accept this
      // as a new selection.
      
      if (ungrouped && selectset.size() == 0)
      {
         selection = null ;
         ungrouped = false ;
         
         if (groups != null && page != null)
         {
            for (int i = 0 ; i < groups.size() ; i++)
            {
               Group g = (Group) groups.elementAt(i) ;
               if (!selectbox.contains(g.getBoundingBox())) continue ;
               if (!g.isOnPage((Integer) page.getIdentifier())) continue ;
               if (!g.isVisible()) continue ;
               selectset.addElement(g);
            }
         }

         // If our selection set is empty but we clicked on a group
         // and did not drag the mouse then select this group.

         if (selectset.size() == 0 && group != null)
            if (selectbox.width <=1 && selectbox.height <= 1)
               selectset.addElement(group) ;
      }
      
      if (OptionsDialog.getDebugEdit())
      {
         System.out.println("Edit: selection environment " + " (ctrl)=" + ctrldown + " (meta)=" + metadown + " ungrouped=" + ungrouped) ;
         for (int i = 0 ; i < selectset.size() ; i++)
            System.out.println("Edit: selection set contains element " + selectset.elementAt(i)) ;
      }
      
      // If the control key was down on a left mouse press add the
      // selection set to our current selection. If the control key
      // was down on a right mouse press remove the selection set
      // from our current selection.

      if (ctrldown && selection != null)
      {
         if (!ungrouped)
         {
            if (metadown)
            {
               for (int i = 0 ; i < selectset.size() ; i++)
               {
                  selection.remove(selectset.elementAt(i)) ;
               }
            }
            else {
               for (int i = 0 ; i < selectset.size() ; i++)
                  if (!selection.contains(selectset.elementAt(i)))
                     selection.addElement(selectset.elementAt(i)) ;
            }
         }

         // Ungrouped selections adjust the selection mark.

         else
         {
            if (metadown)
            {
               for (int i = 0 ; i < selectset.size() ; i++)
                  selection.setMarked(selectset.elementAt(i),false) ;
            }
            else
            {
               for (int i = 0 ; i < selectset.size() ; i++)
                  selection.setMarked(selectset.elementAt(i),true) ;
            }
         }
      }

      // Otherwise accept the select set as our selection.

      else if (selectbox.width >= 0 && selectbox.height >= 0)
      {
         if (!ungrouped)
         {
            if (!metadown)
            {
               selection = (selectset.size() > 0) ? selectset : null ;
            }
            else if (selection != null)
            {
               for (int i = 0 ; i < selectset.size() ; i++)
                  selection.remove(selectset.elementAt(i)) ;
            }
         }

         // Ungrouped selections adjust the selection mark.

         else if (selection != null)
         {
            if (!metadown)
            {
               for (int i = 0 ; i < selection.size() ; i++)
                  selection.setMarked(i,false) ;
               for (int i = 0 ; i < selectset.size() ; i++)
                  selection.setMarked(selectset.elementAt(i),true) ;
            }
            else
            {
               for (int i = 0 ; i < selectset.size() ; i++)
                  selection.setMarked(selectset.elementAt(i),false) ;
            }
         }
      }

      // If we did not select anything drop out of edit mode.

      if (selection == null || selection.size() == 0 )
         { selection = null ; groupset = null ; }

      // If we have a selection create a new group set.

      if (selection != null)
      {
         groupset = new Group() ;
         for (int i = 0 ; i < selection.size() ; i++)
         {
            Object o = selection.elementAt(i) ;
            if (o instanceof Cel) o = ((Cel) o).getGroup() ;
            if (o instanceof KissObject)
               groupset.addElement((KissObject) o,false) ;
         }
      }

      // Ensure all selected objects are disabled from input and
      // all unselected objects are enabled for input.

      setInputState(true,oldselection) ;
      setInputState(false,selection) ;

      // Capture this edit for undo/redo processing.

      if (page != null && selection != null)
         selection.setPageUniqueID(page.getUniqueID()) ;
      if (!undoredo)
      {
         if (selection == null || !selection.equals(oldselection))
         {
            UndoablePageEdit ce = new UndoablePageEdit(SELECT,selection,oldselection,selection) ;
            UndoableEditEvent evt = new UndoableEditEvent(this,ce) ;
            if (undo != null) undo.undoableEditHappened(evt) ;
         }
      }
   }
   
   
   // This is a utility method to force a reload of a specific cel
   // This is required if we update a cel image through an Image Edit.
   
   void reloadCel(Cel cel)
   {
      if (cel == null) return ;
      String name = cel.getName() ;
      if (name != null) name = name.toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),name) ;
      if (c == null) 
      {
         name = "Import" + name ;
         c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),name) ;
      }

      if (c != null)
      {
         boolean copy = c.isCopy() ;
         c.loadCopy(cel) ;
         c.setCopy(copy) ;
         Object o = c.getGroup() ;
         if (o instanceof Group) 
         {
            Group g = (Group) o ;
            g.updateBoundingBox() ;
         }
      }
   }
   
   
   // This is a utility method to force a reload of all copies of ambiguous
   // cels. This is required if we update a cel image through an Image Edit.
   
   void reloadAmbiguousCels(Cel cel)
   {
      if (cel == null) return ;
      String name = cel.getName() ;
      if (name != null) name = name.toUpperCase() ;
      Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),name) ;
      
      while (c != null)
      {
         if (c != cel)
         {
            boolean copy = c.isCopy() ;
            c.loadCopy(cel) ;
            c.setCopy(copy) ;
            Object o = c.getGroup() ;
            if (o instanceof Group) 
            {
               Group g = (Group) o ;
               g.updateBoundingBox() ;
            }
         }
         
         // Pick up the next ambiguous cel.  If we had imported ambiguity,
         // try the imported cels.
         
         c = (Cel) c.getNextByKey(Cel.getKeyTable(),config.getID(),name) ;
         if (c == null) 
         {
            name = "Import" + name ;
            c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),name) ;
         }
     }
   }



   // Inner Classes
   // -------------

   // Inner class to create a popup dialog for the context popup.

   public class PopupMenu extends JPopupMenu
      implements ActionListener, PopupMenuListener
   {
      private Cel c = null ;
      private Group g = null ;
      private PageSet p = null ;
      private Integer mp = null ;

      private JMenu celmenu = new JMenu(Kisekae.getCaptions().getString("ImageContextText")) ;
      private JMenu groupmenu = new JMenu(Kisekae.getCaptions().getString("ObjectContextText")) ;
      private JMenuItem attributemenu = new JMenuItem(Kisekae.getCaptions().getString("AttributeContextText")) ;
      private JMenuItem viewcel = new JMenuItem(Kisekae.getCaptions().getString("PropertiesMessage")) ;
      private JMenuItem viewgroup = new JMenuItem(Kisekae.getCaptions().getString("PropertiesMessage")) ;
      private JMenuItem editpalette = new JMenuItem(Kisekae.getCaptions().getString("EditPaletteMessage")) ;
      private JMenuItem editcel = new JMenuItem(Kisekae.getCaptions().getString("EditImageMessage")) ;
      private JMenuItem editgroup = new JMenuItem(Kisekae.getCaptions().getString("EditImageMessage")) ;
      private JMenuItem celevent = new JMenuItem(Kisekae.getCaptions().getString("EditEventsMessage")) ;
      private JMenuItem groupevent = new JMenuItem(Kisekae.getCaptions().getString("EditEventsMessage")) ;
      private JMenuItem celeventwiz = new JMenuItem(Kisekae.getCaptions().getString("EventWizardMessage")) ;
      private JMenuItem groupeventwiz = new JMenuItem(Kisekae.getCaptions().getString("EventWizardMessage")) ;
      private JMenuItem selectobject = new JMenuItem(Kisekae.getCaptions().getString("SelectContextText")) ;
      private JMenuItem unselectobject = new JMenuItem(Kisekae.getCaptions().getString("UnselectMessage")) ;
      private JMenuItem ungroupobject = new JMenuItem(Kisekae.getCaptions().getString("UngroupContextText")) ;
      private JMenuItem regroupobject = new JMenuItem(Kisekae.getCaptions().getString("RegroupContextText")) ;
      private JMenuItem forwardobject = new JMenuItem(Kisekae.getCaptions().getString("ForwardObjectText")) ;
      private JMenuItem backwardobject = new JMenuItem(Kisekae.getCaptions().getString("BackwardObjectText")) ;

      // Constructor

      public PopupMenu()
      {
         add(selectobject) ;
         add(unselectobject) ;
         add(ungroupobject) ;
         add(regroupobject) ;
         add(forwardobject) ;
         add(backwardobject) ;
         add(attributemenu) ;
         add(celmenu) ;
         celmenu.add(viewcel) ;
         celmenu.add(editpalette) ;
         celmenu.add(editcel) ;
         celmenu.add(celevent) ;
         celmenu.add(celeventwiz) ;
         add(groupmenu) ;
         groupmenu.add(viewgroup) ;
         groupmenu.add(editgroup) ;
         groupmenu.add(groupevent) ;
         groupmenu.add(groupeventwiz) ;
         
         selectobject.addActionListener(this);
         unselectobject.addActionListener(this);
         ungroupobject.addActionListener(this);
         regroupobject.addActionListener(this);
         forwardobject.addActionListener(this);
         backwardobject.addActionListener(this);
         viewcel.addActionListener(this);
         viewgroup.addActionListener(this);
         editpalette.addActionListener(this);
         editcel.addActionListener(this);
         editgroup.addActionListener(this);
         celevent.addActionListener(this);
         groupevent.addActionListener(this);
         celeventwiz.addActionListener(this);
         groupeventwiz.addActionListener(this);
         attributemenu.addActionListener(this);
         addPopupMenuListener(this);
      }

      // We overload the show function to set our action object names.

      public void show(Component invoker, int x, int y, Cel cel, Group group, PageSet page)
      {
         c = cel ;
         g = group ;
         p = page ;
         mp = getMultiPalette() ;
         viewcel.setEnabled(c != null) ;
         viewgroup.setEnabled(g != null) ;
         editpalette.setVisible(c != null && !c.isTruecolor()) ;
         editcel.setEnabled(c != null && !(c instanceof Video)) ;
         editgroup.setEnabled(g != null && !(c instanceof Video)) ;
         editgroup.setVisible(false) ;
         if (c != null) 
         {
            celevent.setVisible(c.getEventCount() > 0) ;
            celevent.setText(Kisekae.getCaptions().getString("EditEventsMessage") 
               + " (" + c.getEventCount() + ")") ;
         }
         if (g != null) 
         {
            groupevent.setVisible(g.getEventCount() > 0) ;
            groupevent.setText(Kisekae.getCaptions().getString("EditEventsMessage") 
               + " (" + g.getEventCount() + ")") ;
         }
         celeventwiz.setEnabled(c != null) ;
         groupeventwiz.setEnabled(g != null) ;
         attributemenu.setVisible(c instanceof JavaCel || c instanceof Video) ;
         boolean b = selection != null && (selection.contains(g) || selection.contains(c)) ;
         ungroupobject.setVisible(b && !selection.contains(c)) ;
         regroupobject.setVisible(b && selection.contains(c)) ;
         forwardobject.setVisible(b) ;
         backwardobject.setVisible(b) ;
         unselectobject.setVisible(b && !selection.contains(c)) ; 
         selectobject.setVisible(!(unselectobject.isVisible())) ; 
         
         // Set attribute menuitem name.  Convert first character to upper case.
         
         if (c instanceof JavaCel)
         {
            String s1 = ((JavaCel) c).getType() ;
            String s2 = Kisekae.getCaptions().getString("AttributeContextText") ;
            if (s1 != null && s1.length() > 0)
            {
               String s3 = s1.substring(0,1).toUpperCase() ;
               s1 = s3 + s1.substring(1) ;
               s2 = s1 + " " + s2 ;
            }
            attributemenu.setText(s2) ;
         }
         
         if (c instanceof Video) 
         {
            x = c.getSize().width ;
            String s1 = "Video" ;
            String s2 = Kisekae.getCaptions().getString("AttributeContextText") ;
            s2 = s1 + " " + s2 ;
            attributemenu.setText(s2) ;
         }
         
         pack() ;
         super.show(invoker,x,y) ;
      }


      // The action method is used to process control menu events.
      // This method is required as part of the ActionListener interface.

      public void actionPerformed(ActionEvent e)
      {
         Object source = e.getSource() ;

         if (source == selectobject)
         {
            if (g == null) return ;
            doSelection(e,new Rectangle(),g,c) ;
            parent.updateMenu();
            parent.repaint() ;
         }
         
         if (source == unselectobject)
         {
            if (g == null&& c == null) return ;
            PanelEdit edit = new PanelEdit() ;
            if (selection != null && selection.contains(g)) edit.add(g) ;
            if (selection != null && selection.contains(c)) edit.add(c) ;
            unselectSet(edit) ;
            parent.updateMenu();
            parent.repaint() ;
         }
         
         if (source == ungroupobject)
         {
            if (g == null) return ;
            PanelEdit edit = new PanelEdit() ;
            edit.add(g) ;
            editUngroup(edit) ;
         }
         
         if (source == regroupobject)
         {
            if (c == null) return ;
            PanelEdit edit = new PanelEdit() ;
            edit.add(c) ;
            editGroup(edit) ;
         }
         
         if (source == forwardobject)
         {
            moveLevel(selection,-1) ;
         }
         
         if (source == backwardobject)
         {
            moveLevel(selection,+1) ;
         }

         if (source == viewgroup)
         {
            if (g == null) return ;
            GroupDialog gd = new GroupDialog(parent,g,p,config) ;
            gd.show() ;
         }

         if (source == viewcel)
         {
            if (c == null) return ;
            CelDialog cd = new CelDialog(parent,c,g,config) ;
            cd.show() ;
         }

         if (source == editpalette)
         {
            if (c == null) return ;
            KissObject p = c.getPalette() ;
            Object o = (p == null) ? c : p ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ColorFrame cf = new ColorFrame(config,o,mp) ;
            cf.callback.addActionListener(this);
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            cf.show() ;
         }

         if (source == editcel)
         {
            if (c == null) return ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ImageFrame imf = new ImageFrame(config,c,mp) ;
            imf.callback.addActionListener(this);
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            imf.show() ;
         }

         if (source == editgroup)
         {
            if (g == null) return ;
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            ImageFrame imf = new ImageFrame(config,g,mp) ;
            imf.callback.addActionListener(this);
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
            imf.show() ;
         }

         if (source == celevent)
         {
            if (c == null) return ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) fk.reopen(config,c,null) ;
            else
            {
               fk = new FKissFrame(config,c,null) ;
               fk.setVisible(true) ;
            }
            return ;
         }

         if (source == groupevent)
         {
            if (g == null) return ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) fk.reopen(config,g,null) ;
            else
            {
               fk = new FKissFrame(config,g,null) ;
               fk.setVisible(true) ;
            }
            return ;
         }

         if (source == celeventwiz)
         {
            if (c == null) return ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) fk.reopen(config,c,null,parent) ;
            else
            {
               fk = new FKissFrame(config,c,null,parent) ;
            }
            return ;
         }

         if (source == groupeventwiz)
         {
            if (g == null) return ;
            FKissFrame fk = FKissEvent.getBreakFrame() ;
            if (fk != null) fk.reopen(config,g,null,parent) ;
            else
            {
               fk = new FKissFrame(config,g,null,parent) ;
            }
            return ;
         }

         if (source == attributemenu)
         {
            if (c == null) return ;
            AttributeDialog ad = new AttributeDialog(parent,c,config) ;
            ad.show() ;
         }

         // An update request from the palette edit window has occured.
         // The data set will be updated.

         if ("ColorFrame Callback".equals(e.getActionCommand()))
         {
            Object o = e.getSource() ;
            if (o instanceof CallbackButton)
            {
               o = ((CallbackButton) o).getDataObject() ;
               if (o instanceof Cel)
               {
                  c = (Cel) o ;
                  reloadCel(c) ;
               }
            }
            if (page == null) return ;
            initcolor(page.getMultiPalette()) ;
            parent.updateToolBar() ;
            showpage() ;
            return ;
         }

         // An update request from the image edit window has occured.
         // The data set will be updated.

         if ("ImageFrame Callback".equals(e.getActionCommand()))
         {
            reloadAmbiguousCels(c) ;
            if (page == null) return ;
            parent.updateToolBar() ;
            showpage() ;
            return ;
         }
      }
      

      // PopupMenu Events

      public void popupMenuCanceled(PopupMenuEvent evt) { }
      public void popupMenuWillBecomeVisible(PopupMenuEvent evt) { }
      public void popupMenuWillBecomeInvisible(PopupMenuEvent evt)
      {
         Runnable callback = new Runnable()
         { public void run() { flush() ; }	} ;
         javax.swing.SwingUtilities.invokeLater(callback) ;
      }


      // Utility function to release critical resources.

      private void flush()
      {
         c = null ;
         g = null ;
         p = null ;
         mp = null ;
      }
   }


   // Inner class to construct a transferable panel edit object.  This object
   // is placed on the clipboard for cut, copy and paste operations.  It is
   // an actual vector of selection objects and contains attributes that
   // identify the selection context.

   class PanelEdit extends Vector
      implements Transferable
   {
      private String df = DataFlavor.javaSerializedObjectMimeType ;
      private Object pagesetid = null ;

      // Constructor

      public PanelEdit() { super() ; }
      public PanelEdit(Collection c) { super(c) ; }

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
            System.out.println("PanelFrame: PanelEdit DataFlavor fault, " + df) ;
            System.out.println(e.getMessage()) ;
         }
         return flavors ;
      }

      // Transferable interface method to check if we support our data flavor.

      public boolean isDataFlavorSupported(DataFlavor flavor)
      { return flavor.isMimeTypeEqual(df) ; }


      // Method to set the page set identifier for this selection set.
      // This attribute helps identify the required visibility of cels
      // and groups when this selection set is pasted into a new page.

      void setPageUniqueID(Object o) { pagesetid = o ; }
      Object getPageUniqueID() { return pagesetid ; }


      // Overload the vector accessor functions.  We attach a selection mark
      // modifier to each element and store the modifiers as the latter elements
      // of an array packet.

      public Object elementAt(int i)
      {
         Object o = super.elementAt(i) ;
         if (!(o instanceof Object [])) return o ;
         return ((Object []) o)[0] ;
      }

      public boolean remove(Object o)
      {
         if (o == null) return false ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            super.remove(i) ;
            return true ;
         }
         return false ;
      }

      public boolean contains(Object o)
      {
         if (o == null) return false ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            return true ;
         }
         return false ;
      }

      // Define functions to maintain the selection mark for
      // an object.

      void setMarked(Object o, boolean b)
      {
         if (o == null) return ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            {
               Object [] newpkt = new Object [3] ;
               newpkt[0] = o ;
               newpkt[1] = new Boolean(b) ;
               newpkt[2] = new Boolean(isPasted(i)) ;
               setElementAt(newpkt,i) ;
               return ;
            }
         }
      }

      void setMarked(int i, boolean b)
      {
         if (i < 0 || i >= size()) return ;
         Object o = elementAt(i) ;
         Object [] newpkt = new Object [3] ;
         newpkt[0] = o ;
         newpkt[1] = new Boolean(b) ;
         newpkt[2] = new Boolean(isPasted(i)) ;
         setElementAt(newpkt,i) ;
         return ;
      }

      // Function to determine if a specific object is marked.

      boolean isMarked(Object o)
      {
         if (o == null) return false ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) return false ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 2) return false ;
            if (!(pkt[1] instanceof Boolean)) return false ;
            return ((Boolean) pkt[1]).booleanValue() ;
         }
         return false ;
      }

      // Function to determine if a specific object by index is marked.

      boolean isMarked(int i)
      {
         if (i < 0 || i >= size()) return false ;
         Object o1 = super.elementAt(i) ;
         if (!(o1 instanceof Object [])) return false ;
         Object [] pkt = (Object []) o1 ;
         if (pkt.length < 2) return false ;
         if (!(pkt[1] instanceof Boolean)) return false ;
         return ((Boolean) pkt[1]).booleanValue() ;
      }

      // Function to determine if any object is marked.

      boolean isMarked()
      {
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) continue ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 2) continue ;
            if (!(pkt[1] instanceof Boolean)) continue ;
            if (((Boolean) pkt[1]).booleanValue()) return true ;
         }
         return false ;
      }

      // Function to clear all marked indicators.

      void clearMarked()
      {
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) continue ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 2) continue ;
            pkt[1] = null ;
         }
      }

      // Define functions to maintain the pasted mark for
      // an object.

      void setPasted(Object o, boolean b)
      {
         if (o == null) return ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            {
               Object [] newpkt = new Object [3] ;
               newpkt[0] = o ;
               newpkt[1] = new Boolean(isMarked(i)) ;
               newpkt[2] = new Boolean(b) ;
               setElementAt(newpkt,i) ;
               return ;
            }
         }
      }

      void setPasted(int i, boolean b)
      {
         if (i < 0 || i >= size()) return ;
         Object o = elementAt(i) ;
         Object [] newpkt = new Object [3] ;
         newpkt[0] = o ;
         newpkt[1] = new Boolean(isMarked(i)) ;
         newpkt[2] = new Boolean(b) ;
         setElementAt(newpkt,i) ;
         return ;
      }

      // Function to determine if a specific object is pasted.

      boolean isPasted(Object o)
      {
         if (o == null) return false ;
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = elementAt(i) ;
            if (!(o.equals(o1))) continue ;
            o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) return false ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 3) return false ;
            if (!(pkt[2] instanceof Boolean)) return false ;
            return ((Boolean) pkt[2]).booleanValue() ;
         }
         return false ;
      }

      // Function to determine if a specific object by index is pasted.

      boolean isPasted(int i)
      {
         if (i < 0 || i >= size()) return false ;
         Object o1 = super.elementAt(i) ;
         if (!(o1 instanceof Object [])) return false ;
         Object [] pkt = (Object []) o1 ;
         if (pkt.length < 3) return false ;
         if (!(pkt[2] instanceof Boolean)) return false ;
         return ((Boolean) pkt[2]).booleanValue() ;
      }

      // Function to determine if any object is pasted.

      boolean isPasted()
      {
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) continue ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 3) continue ;
            if (!(pkt[2] instanceof Boolean)) continue ;
            if (((Boolean) pkt[2]).booleanValue()) return true ;
         }
         return false ;
      }

      // Function to clear all pasted indicators.

      void clearPasted()
      {
         for (int i = 0 ; i < size() ; i++)
         {
            Object o1 = super.elementAt(i) ;
            if (!(o1 instanceof Object [])) continue ;
            Object [] pkt = (Object []) o1 ;
            if (pkt.length < 3) continue ;
            pkt[2] = null ;
         }
      }
   }


   // Inner class to construct an undoable edit operation.  We copy
   // our edit data to ensure we retain consistency.

   class UndoablePageEdit extends AbstractUndoableEdit
   {
      private PanelEdit edit = null ;
      private PanelEdit oldselection = null ;
      private PanelEdit newselection = null ;
      private PageSet page = null ;
      private KissObject kiss = null ;
      private Point displacement = null ;
      private Integer pagenumber = null ;
      private Vector positions = null ;
      private Vector initpositions = null ;
      private Integer multipalette = null ;
      private State initialstate = null ;
      private State finalstate = null ;
      private int type = 0 ;


      // Constructor for page updates given a page number

      public UndoablePageEdit(int t, PageSet p, int n, PanelEdit e, Vector pos, Integer mp)
      {
         type = t ;
         page = p ;
         pagenumber = new Integer(n) ;
         edit = e ;
         positions = pos ;
         multipalette = mp ;
      }


      // Constructor for page deletions given a page number

      public UndoablePageEdit(int t, PageSet p, int n, PanelEdit e, Vector pos,
                              Vector initpos, Integer mp)
      {
         type = t ;
         page = p ;
         pagenumber = new Integer(n) ;
         edit = e ;
         positions = pos ;
         initpositions = initpos ;
         multipalette = mp ;
      }

      // Constructor for edit type updates

      public UndoablePageEdit(int t, PanelEdit e1, PanelEdit e2, PanelEdit e3)
      {
         type = t ;
         if (e1 != null) edit = (PanelEdit) e1.clone() ;
         if (e2 != null) oldselection = (PanelEdit) e2.clone() ;
         if (e3 != null) newselection = (PanelEdit) e3.clone() ;
      }

      // Constructor for movement type updates

      public UndoablePageEdit(int t, PageSet p, KissObject k, Point d)
      {
         type = t ;
         page = p ;
         kiss = k ;
         displacement = d ;
      }

      // Constructor for reset type updates

      public UndoablePageEdit(int t, PageSet p, State s1, State s2)
      {
         type = t ;
         page = p ;
         initialstate = s1 ;
         finalstate = s2 ;
      }

      // Constructor for new group type updates

      public UndoablePageEdit(int t, PageSet p, PanelEdit e1, PanelEdit e2)
      {
         type = t ;
         page = p ;
         if (e1 != null) oldselection = (PanelEdit) e1.clone() ;
         if (e2 != null) newselection = (PanelEdit) e2.clone() ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      {
         if (type == CUT)
            return Kisekae.getCaptions().getString("UndoCutName") ;
         if (type == COPY)
            return Kisekae.getCaptions().getString("UndoCopyName") ;
         if (type == PASTE)
            return Kisekae.getCaptions().getString("UndoPasteName") ;
         if (type == PASTENEW)
            return Kisekae.getCaptions().getString("UndoPasteNewName") ;
         if (type == SELECT)
            return Kisekae.getCaptions().getString("UndoSelectName") ;
         if (type == SELECTSET)
            return Kisekae.getCaptions().getString("UndoSelectName") ;
         if (type == MOVE)
            return Kisekae.getCaptions().getString("UndoMoveName") ;
         if (type == ADDPAGE)
            return Kisekae.getCaptions().getString("UndoNewPageName") ;
         if (type == REMOVEPAGE)
            return Kisekae.getCaptions().getString("UndoDeletePageName") ;
         if (type == RESET)
            return Kisekae.getCaptions().getString("UndoResetName") ;
         if (type == WRITEPAGE)
            return Kisekae.getCaptions().getString("UndoWritePageName") ;
         if (type == IMPORT)
            return Kisekae.getCaptions().getString("UndoImportName") ;
         if (type == IMPORTPALETTE)
            return Kisekae.getCaptions().getString("UndoImportName")  
              + " " + Kisekae.getCaptions().getString("PaletteText") ;
         if (type == UNGROUP)
            return Kisekae.getCaptions().getString("UndoUngroupName") ;
         if (type == GROUP)
            return Kisekae.getCaptions().getString("UndoRegroupName") ;
         if (type == NEWGROUP)
            return Kisekae.getCaptions().getString("UndoNewGroupName") ;
         return "" ;
      }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         if (config == null) return ;
         undoredo = true ;
         try
         {
            if (type == CUT) updatePage(PASTE,edit,newselection,oldselection) ;
            if (type == COPY) updatePage(COPY,edit,newselection,oldselection) ;
            if (type == PASTE) updatePage(CUT,newselection,newselection,oldselection) ;
            if (type == PASTENEW) updatePage(CUT,newselection,newselection,oldselection) ;
            if (type == SELECT) updatePage(SELECT,edit,newselection,oldselection) ;
            if (type == SELECTSET) updatePage(SELECT,edit,newselection,oldselection) ;
            if (type == MOVE) updatePage(MOVE,page,kiss,-displacement.x,-displacement.y) ;
            if (type == ADDPAGE) updatePage(REMOVEPAGE,page,pagenumber,edit,positions,initpositions,multipalette) ;
            if (type == REMOVEPAGE) updatePage(ADDPAGE,page,pagenumber,edit,positions,initpositions,multipalette) ;
            if (type == RESET) updatePage(RESET,page,initialstate) ;
            if (type == WRITEPAGE) updatePage(WRITEPAGE,page,initialstate) ;
            if (type == IMPORT) updatePage(IMPORTCUT,edit,newselection,oldselection) ;
            if (type == IMPORTPALETTE) updatePage(IMPORTPALETTECUT,edit,newselection,oldselection) ;
            if (type == UNGROUP) updatePage(SELECT,edit,newselection,oldselection) ;
            if (type == GROUP) updatePage(SELECT,edit,newselection,oldselection) ;
            if (type == NEWGROUP) updatePage(NEWGROUP,page,newselection,oldselection) ;
         }
         catch (OutOfMemoryError e)
         {
            System.out.println("PanelFrame: Out of memory performing undo, type " + type) ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
               JOptionPane.ERROR_MESSAGE) ;
         }
         catch (Exception e)
         {
            System.out.println("PanelFrame: Undo exception " + e.toString()) ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + e.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
            e.printStackTrace() ;
         }
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         if (config == null) return ;
         undoredo = true ;
            
         try
         {
            if (type == CUT) updatePage(CUT,edit,oldselection,newselection) ;
            if (type == COPY) updatePage(COPY,edit,oldselection,newselection) ;
            if (type == PASTE) updatePage(PASTE,edit,oldselection,newselection) ;
            if (type == PASTENEW) updatePage(PASTENEW,edit,oldselection,newselection) ;
            if (type == SELECT) updatePage(SELECT,edit,oldselection,newselection) ;
            if (type == SELECTSET) updatePage(SELECT,edit,oldselection,newselection) ;
            if (type == MOVE) updatePage(MOVE,page,kiss,displacement.x,displacement.y) ;
            if (type == ADDPAGE) updatePage(ADDPAGE,page,pagenumber,edit,positions,initpositions,multipalette) ;
            if (type == REMOVEPAGE) updatePage(REMOVEPAGE,page,pagenumber,edit,positions,initpositions,multipalette) ;
            if (type == RESET) updatePage(RESET,page,finalstate) ;
            if (type == WRITEPAGE) updatePage(WRITEPAGE,page,finalstate) ;
            if (type == IMPORT) updatePage(IMPORTPASTE,edit,oldselection,newselection) ;
            if (type == IMPORTPALETTE) updatePage(IMPORTPALETTEADD,edit,oldselection,newselection) ;
            if (type == UNGROUP) updatePage(SELECT,edit,oldselection,newselection) ;
            if (type == GROUP) updatePage(SELECT,edit,oldselection,newselection) ;
            if (type == NEWGROUP) updatePage(NEWGROUP,page,oldselection,newselection) ;
         }
         catch (OutOfMemoryError e)
         {
            System.out.println("PanelFrame: Out of memory performing redo, type " + type) ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
               JOptionPane.ERROR_MESSAGE) ;
         }
         catch (Exception e)
         {
            System.out.println("PanelFrame: Redo exception " + e.toString()) ;
            JOptionPane.showMessageDialog(parent,
               Kisekae.getCaptions().getString("EditUndoError") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted") +
               "\n" + e.toString(),
               Kisekae.getCaptions().getString("EditUndoError"),
               JOptionPane.ERROR_MESSAGE) ;
            e.printStackTrace() ;
         }
         undoredo = false ;
      }

      // Utility function to apply an edit of the specified type to the
      // selected page.  These are CUT, COPY, PASTE, IMPORT and SELECT edits.

      private void updatePage(int type, PanelEdit e, PanelEdit os, PanelEdit ns)
      {
         boolean fallthrough = false ;
         
         switch (type)
         {
         case COPY:
            if (clipboard == null) break ;
            PanelEdit clip = (ns == null) ? null : (PanelEdit) ns.clone() ;
            clipboard.setContents(clip,panel) ;
            parent.updateMenu() ;
            repaint() ;
            break ;

         case CUT:
         case PASTE:
         case PASTENEW:

            // Establish the page to which the update applies.  Cuts apply
            // to the source selection set page.  Pastes are made to the
            // final selection set page.

            PanelEdit edit = (type == CUT) ? os : ns ;
            PageSet p = (edit == null) ? null : (PageSet) PageSet.getByKey(
               PageSet.getKeyTable(),config.getID(),edit.getPageUniqueID()) ;
            if (p != null && !p.equals(panel.getPage()))
               initpage(((Integer) p.getIdentifier()).intValue()) ;

            // Apply the edit to the proper page.  Paste operations may
            // create a new set of objects.  We retain this as our new
            // selection set so that the operation can be undone.

            if (type == CUT) editCut(e) ;
            if (type == PASTE) editPaste(e) ;
            if (type == PASTENEW) editPasteNew(e) ;
            ns = selection ;
            newselection = (ns != null) ? (PanelEdit) ns.clone() : null ;
            fallthrough = true ;

            // We fall through to re-establish the original edit set.

         case SELECT:

            // Establish the page to which the selection applies.

            edit = ns ;
            p = (edit == null) ? null : (PageSet) PageSet.getByKey(
               PageSet.getKeyTable(),config.getID(),edit.getPageUniqueID()) ;
            if (p != null && !p.equals(panel.getPage()))
               initpage(((Integer) p.getIdentifier()).intValue()) ;

            // Apply the selection set

            selection = ns ;
            if (selection != null)
            {
               groupset = new Group() ;
               for (int i = 0 ; i < selection.size() ; i++)
               {
                  Group g = null ;
                  Object o = selection.elementAt(i) ;
                  if (o instanceof Cel) o = ((Cel) o).getGroup() ;
                  if (o instanceof Group) g = (Group) o ;
                  if (g != null) groupset.addElement(g,false) ;
               }
            }
            else
               groupset = null ;

            // Ensure all selected objects are disabled from input and
            // all unselected objects are enabled for input.  One difficulty
            // is that if a component object was pasted on two different pages
            // and the paste is undone the cel is removed from one page, yet it
            // retains the second page in its page association list.  If input 
            // state is restored this causes the object to be added back into 
            // the panel frame yet it was supposedly cut.  However, by not
            // restoring input state on the old selection set we leave the
            // original page object as non-responsive.

            if (!fallthrough)
            {
               setInputState(true,os) ;
               setInputState(false,ns) ;
            }

            // Update the user interface.

            parent.updateMenu() ;
            parent.updateToolBar() ;
            showpage() ;
            break ;

         case IMPORTCUT:
            Vector cels = config.getCels() ;
            Vector palettes = config.getPalettes() ;
            updatePage(CUT,e,os,ns) ;
            for (int i = 0 ; i < e.size() ; i++)
            {
               KissObject kiss = (KissObject) e.elementAt(i) ;
               if (kiss instanceof Group)
               {
                  int j = 0 ;
                  Cel c = null ;
                  while ((c = ((Group) kiss).getCel(j++)) != null)
                  {
                     if (OptionsDialog.getDebugEdit())
                        System.out.println("Edit: import remove cel " + c) ;
                     cels.remove(c) ;
                     if (c instanceof JavaCel) 
                        ((JavaCel) c).showComponent(false) ;
                     if (selection != null) selection.remove(c) ;
                  }
               }
               if (kiss instanceof Cel)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import remove cel " + kiss) ;
                  cels.remove(kiss) ;
                  if (kiss instanceof JavaCel) 
                     ((JavaCel) kiss).showComponent(false) ;

                  // If we cut a cel the parent group has its bounding box
                  // recomputed, however this is relative to the group offset.
                  // This cut cel must be removed from the group and the
                  // bounding box recalculated.

                  Object o = ((Cel) kiss).getGroup() ;
                  Group g = (o instanceof Group) ? (Group) o : null ;
                  if (g != null) g.removeCel((Cel) kiss) ;
               }
               if (kiss instanceof Palette)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import remove palette " + kiss) ;
                  palettes.remove(kiss) ;
                  Object cid = config.getID() ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getIdentifier(),kiss) ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getPath().toUpperCase(),kiss) ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getName().toUpperCase(),kiss) ;
               }
            }
            
            if (palettes.size() == 0)
            {
               background = config.getBorderColor() ;
               setBackground(background) ;
               parent.setBackground(background) ;
               border = null ;
               darkborder = null ;
               lightborder = null ;
               showpage() ;
            }
            break ;

         case IMPORTPASTE:
            cels = config.getCels() ;
            palettes = config.getPalettes() ;
            for (int i = 0 ; i < e.size() ; i++)
            {
               KissObject kiss = (KissObject) e.elementAt(i) ;
               if (kiss instanceof Palette)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import restore palette " + kiss) ;
                  palettes.add(kiss) ;
                  Object cid = config.getID() ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getIdentifier()) ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getPath().toUpperCase()) ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getName().toUpperCase()) ;
               }
               if (kiss instanceof Cel)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import restore cel " + kiss) ;
                  cels.add(kiss) ;
                  if (kiss instanceof JavaCel) 
                     ((JavaCel) kiss).showComponent(true) ;

                  // The undone import cel must be added back to the group and
                  // the bounding box recalculated.

                  Object o = ((Cel) kiss).getGroup() ;
                  Group g = (o instanceof Group) ? (Group) o : null ;
                  if (g != null)
                  {
                     g.addCel((Cel) kiss) ;
                     g.rebuildBoundingBox() ;
                  }
               }
               if (kiss instanceof Group)
               {
                  int j = 0 ;
                  Cel c = null ;
                  while ((c = ((Group) kiss).getCel(j++)) != null)
                  {
                     if (OptionsDialog.getDebugEdit())
                        System.out.println("Edit: import restore cel " + c) ;
                     cels.add(c) ;
                     if (c instanceof JavaCel) 
                        ((JavaCel) c).showComponent(true) ;
                  }
               }
            }
            updatePage(PASTE,e,os,ns) ;
            
            if (palettes.size() == 1)
            {
               setBackground(config.getBorderColor()) ;
               parent.setBackground(config.getBorderColor()) ;
               initcolor(new Integer(0)) ;
               showpage() ;
            }
            break ;

         case IMPORTPALETTECUT:
            palettes = config.getPalettes() ;
            for (int i = 0 ; i < e.size() ; i++)
            {
               KissObject kiss = (KissObject) e.elementAt(i) ;
               if (kiss instanceof Palette)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import remove palette " + kiss) ;
                  palettes.remove(kiss) ;
                  Object cid = config.getID() ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getIdentifier(),kiss) ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getPath().toUpperCase(),kiss) ;
                  kiss.removeObject(Palette.getKeyTable(),cid,kiss.getName().toUpperCase(),kiss) ;
               }
            }
            
            if (palettes.size() == 0)
            {
               background = Color.black ;
               setBackground(background) ;
               parent.setBackground(config.getBorderColor()) ;
               border = null ;
               darkborder = null ;
               lightborder = null ;
               showpage() ;
            }

            // Update the user interface.

            selection = os ;
            parent.updateMenu() ;
            parent.updateToolBar() ;
            showpage() ;
            break ;

         case IMPORTPALETTEADD:
            palettes = config.getPalettes() ;
            for (int i = 0 ; i < e.size() ; i++)
            {
               KissObject kiss = (KissObject) e.elementAt(i) ;
               if (kiss instanceof Palette)
               {
                  if (OptionsDialog.getDebugEdit())
                     System.out.println("Edit: import restore palette " + kiss) ;
                  palettes.add(kiss) ;
                  Object cid = config.getID() ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getIdentifier()) ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getPath().toUpperCase()) ;
                  kiss.setKey(Palette.getKeyTable(),cid,kiss.getName().toUpperCase()) ;
               }
            }
            
            if (palettes.size() == 1)
            {
               setBackground(config.getBorderColor()) ;
               parent.setBackground(config.getBorderColor()) ;
               initcolor(new Integer(0)) ;
               showpage() ;
            }

            // Update the user interface.

            selection = ns ;
            parent.updateMenu() ;
            parent.updateToolBar() ;
            showpage() ;
            break ;
         }
      }

      // Utility function to apply a move edit to the selected page.  We
      // will show the page on which the movement activity originally occured.

      private void updatePage(int type, PageSet page, KissObject kiss, int x, int y)
      {
         if (type != MOVE) return ;
         if (kiss == null) return ;
         if (page == null) return ;

         // Switch to the page to which the event applies.

         if (!page.equals(panel.getPage()))
            initpage(((Integer) page.getIdentifier()).intValue()) ;

         // If this is an undo/redo of an ungrouped cel object then
         // we are adjusting the placement of relocated cels.

         if (kiss instanceof Cel) 
            ((Cel) kiss).setPlacement(new Point()) ;

         // Move the object.

         kiss.setPlacement(x,y) ;
         kiss.drop() ;

         // If this is an undo/redo of an ungrouped cel object then
         // we are adjusting the placement of relocated cels.

         if (kiss instanceof Cel) 
            relocateCel((Cel) kiss) ;

         // Update the location of the group on the page.
         
         if (kiss instanceof Group)
            ((Group) kiss).updatePageSetLocation(page) ;
         
         // If this was a JavaCel component then eliminate the group offset.
         
//         if (kiss instanceof JavaCel)
//         {
//            Object o = ((JavaCel) kiss).getGroup() ;
//            if (o instanceof Group) ((Group) o).eliminateOffset() ;
//         }

         // If the object was part of our groupset then we need to
         // correct the groupset bounding box for shift drags.

         if (groupset != null) groupset.updateBoundingBox() ;
         showStatus(null) ;
         showpage() ;
      }

      // Utility function to undo reset operations to the selected page.  We
      // adjust the page positions to the required initial or final state.

      private void updatePage(int type, PageSet page, State sv)
      {
         if (page == null) return ;

         // Switch to the page to which the event applies.

         if (!page.equals(panel.getPage()))
            initpage(((Integer) page.getIdentifier()).intValue()) ;

         // Adjust the page set positions.

         if (type == WRITEPAGE) page.setState(config.getID(),"initial",sv) ;
         if (type == RESET) reset(sv) ;

         // Show a feedback confirmation to the user.

         if (type == WRITEPAGE)
         {
            String s = Kisekae.getCaptions().getString("MainFrameWriteText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1) s = s.substring(0,i1+1) + page.getIdentifier() + s.substring(j1) ;
            JOptionPane.showMessageDialog(parent, s,
               Kisekae.getCaptions().getString("MainFrameWritePage"),
               JOptionPane.INFORMATION_MESSAGE) ;
         }
      }

      // Utility function to undo new group operations to the selected page.
      // We adjust the page positions to the required initial or final state.

      private void updatePage(int type, PageSet page, PanelEdit e1, PanelEdit e2)
      {
         if (page == null) return ;
         if (type != NEWGROUP) return ;

         // Switch to the page to which the event applies.

         if (!page.equals(panel.getPage()))
            initpage(((Integer) page.getIdentifier()).intValue()) ;

         // Undo the operation by cutting the original new group paste.
         // Paste the old original groups back into the set.

         editCut(e1,true) ;
         editPaste(e2,true) ;
         
         // Relocate the group cels so that the adjusted offset is
         // relative to the current group.
         
         for (int i = 0 ; i < e2.size() ; i++)
         {
            Object o = e2.elementAt(i) ;
            if (!(o instanceof Group)) continue ;
            Group g = (Group) o ;
            relocateGroupCels(g) ;
            g.rebuildBoundingBox() ;
         }

         // Update the user interface.

         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
      }

      // Utility function to add or remove pages.  If pages are added we will
      // show the new, blank page.  If pages are removed we will show the
      // page from which the request was initiated.

      private void updatePage(int type, PageSet page, Integer pagenumber,
         PanelEdit edit, Vector positions, Vector initpositions, Integer multipalette)
      {
         int p = 0 ;
         if (page == null) return ;
         if (config == null) return ;
         if (type == ADDPAGE)
            p = panel.insertPage(pagenumber) ;
         if (type == REMOVEPAGE)
         {
            p = panel.deletePage(pagenumber) ;
            Object o = page.getIdentifier() ;
            if (o instanceof Integer) p = ((Integer) o).intValue() ;
         }

         // If we are undoing a page deletion then we need to restore the
         // objects that were originally visible on the page.  The undo edit
         // set should contain a list of cel identifiers that were dropped
         // from the page.  We must restore these and paste the relevant
         // groups back into the page.  The recovered page initial state is
         // set to reference the object positions at the time the page was
         // deleted.

         PanelEdit groupset = new PanelEdit() ;
         groupset.setPageUniqueID(page.getUniqueID());
         if (type == ADDPAGE && edit != null)
         {
            for (int i = 0 ; i < edit.size() ; i++)
            {
               Object o = edit.elementAt(i) ;
               if (!(o instanceof Integer)) continue ;
               Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),o) ;
               if (c == null) continue ;
               Object g = c.getGroup() ;
               Vector pages = c.getPages() ;
               if (pages == null || !(g instanceof Group)) continue ;
               if (!pages.contains(pagenumber))	pages.addElement(pagenumber) ;
               if (!groupset.contains(g)) groupset.addElement(g) ;
            }
         }

         // Switch to the new empty page.

         initpage(p) ;

         // Paste in any objects that are required for recovery.  Reset the
         // page to its state when the deletion was performed, then set the
         // original page initial positions.

         if (type == ADDPAGE)
         {
            panel.editPaste(groupset) ;
            loadCels(p) ;
            if (panel.page != null)
               panel.page.setState(config.getID(),"initial",positions,multipalette) ;
            selection = null ;
            groupset = null ;
            reset() ;
            if (panel.page != null)
               panel.page.setState(config.getID(),"initial",initpositions,multipalette) ;
         }

         // Update the user interface.

         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
         showStatus(null) ;
      }
   }


   // Inner class to construct an undoable color edit operation.  These edits
   // occur when we invoke the ColorFrame utility to alter colors and the
   // panel frame is updated on return.  We copy our edit data to ensure we
   // retain consistency.

   class UndoableColorEdit extends AbstractUndoableEdit
   {
      private Object editobject = null ;
      private Object [] olddata = null ;
      private Object [] newdata = null ;
      private Cel cel = null ;
      private Palette newpalette = null ;
      private Palette oldpalette = null ;
      private Image oldimage = null ;
      private Image newimage = null ;
      private ColorModel oldcm = null ;
      private ColorModel newcm = null ;
      private boolean oldrgb = false ;
      private boolean newrgb = false ;
      private int oldcolors = 0 ;
      private int newcolors = 0 ;
      private int oldmp = 0 ;
      private int newmp = 0 ;
      private int oldbackground = 0 ;
      private int newbackground = 0 ;
      private int oldtransparent = 0 ;
      private int newtransparent = 0 ;
      private int type = 0 ;


      // Constructor for palette color updates

      public UndoableColorEdit(Object editobject, Palette oldpalette,
         Object [] od, int oldmp, int oldcolors, int oldbackground, int oldtransparent,
         Object [] nd, int newmp, int newcolors, int newbackground, int newtransparent,
         Cel cel, Image newimage, ColorModel newcm, Image oldimage, ColorModel oldcm)
      {
         type = 0 ;
         this.editobject = editobject ;
         this.oldpalette = oldpalette ;
         if (editobject instanceof Palette) newpalette = (Palette) editobject ;
         this.cel = cel ;
         
         this.olddata = new Object [od.length] ;
         for (int i = 0 ; i < od.length ; i++)
         {
            Object o = od[i] ;
            if (o == null) continue ;
            byte [] b = (byte []) o ;
            byte [] copy = new byte [b.length] ;
            for (int j = 0 ; j < b.length ; j++) copy[j] = b[j] ;
            this.olddata[i] = copy ;
         }
         this.oldmp = oldmp ;
         this.oldcolors = oldcolors ;
         this.oldbackground = oldbackground ;
         this.oldtransparent = oldtransparent ;
         this.oldimage = oldimage ;
         this.oldcm = oldcm ;

         this.newdata = new Object [nd.length] ;
         for (int i = 0 ; i < nd.length ; i++)
         {
            Object o = nd[i] ;
            if (o == null) continue ;
            byte [] b = (byte []) o ;
            byte [] copy = new byte [b.length] ;
            for (int j = 0 ; j < b.length ; j++) copy[j] = b[j] ;
            this.newdata[i] = copy ;
         }
         this.newmp = newmp ;
         this.newcolors = newcolors ;
         this.newbackground = newbackground ;
         this.newtransparent = newtransparent ;
         this.newimage = newimage ;
         this.newcm = newcm ;
      }


      // Constructor for cel transparency color updates

      public UndoableColorEdit(Object editobject, int oldtransparent, int newtransparent)
      {
         type = 1 ;
         this.editobject = editobject ;
         this.oldtransparent = oldtransparent ;
         this.newtransparent = newtransparent ;
      }


      // Constructor for panel frame background color changes

      public UndoableColorEdit(boolean oldrgb, int oldbackground,
         boolean newrgb, int newbackground)
      {
         type = 2 ;
         this.oldbackground = oldbackground ;
         this.newbackground = newbackground ;
         this.oldrgb = oldrgb ;
         this.newrgb = newrgb ;
      }

      
      // Return the undo/redo menu name

      public String getPresentationName()
      {
         if (type == 0)
            return Kisekae.getCaptions().getString("UndoColorEditName") ;
         if (type == 1)
            return Kisekae.getCaptions().getString("UndoColorEditName") ;
         if (type == 2)
            return Kisekae.getCaptions().getString("UndoBackgroundName") ;
         return Kisekae.getCaptions().getString("UndoColorEditName") ;
      }

      
      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (type == 0)
            updatePalette(editobject,olddata,oldmp,oldcolors,oldbackground,oldtransparent,cel,oldpalette,oldimage,oldcm) ;
         if (type == 1)
            updateCelTransparency(editobject,oldtransparent) ;
         if (type == 2)
            updateBackground(oldrgb,oldbackground) ;
         undoredo = false ;
      }

      
      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (type == 0)
            updatePalette(editobject,newdata,newmp,newcolors,newbackground,newtransparent,cel,newpalette,newimage,newcm) ;
         if (type == 1)
            updateCelTransparency(editobject,newtransparent) ;
         if (type == 2)
            updateBackground(newrgb,newbackground) ;
         undoredo = false ;
      }

      
      // Utility function to apply a color update to a palette and
      // recreate the panel frame with the new colors.

      private void updatePalette(Object editobject, Object [] data, int mp, int colors,
         int background, int transparent, Cel c, Palette p, Image img, ColorModel cm)
      {
         if (config == null) return ;
         if (!(editobject instanceof Palette)) return ;
         Palette editpalette = (Palette) editobject ;

         // Update the palette.  We copy the data arrays as the colors can 
         // get changed in a subsequent palette edit and this destroys the
         // retained colors in this undoable edit.

         byte [] a = (byte []) data[0] ;
         byte [] r = (byte []) data[1] ;
         byte [] g = (byte []) data[2] ;
         byte [] b = (byte []) data[3] ;
         byte [] a1 = (a == null) ? null : new byte[a.length] ;
         byte [] r1 = (r == null) ? null : new byte[r.length] ;
         byte [] g1 = (g == null) ? null : new byte[g.length] ;
         byte [] b1 = (b == null) ? null : new byte[b.length] ;
         int len = (r == null) ? 0 : r.length ;
         for (int i = 0 ; i < len ; i++)
         {
            if (i < a.length) a1[i] = a[i] ;
            if (i < r.length) r1[i] = r[i] ;
            if (i < g.length) g1[i] = g[i] ;
            if (i < b.length) b1[i] = b[i] ;
         }
         editpalette.setPalette(a1,r1,g1,b1,mp,colors,background,transparent) ;

         // Update the cel because the palette changes required dithering.

         if (c != null && img != null && cm != null) 
         {
            c.setImage(img,cm) ;
            reloadCel(c) ;
         }

         // Palette updates may have changed the transparent or background
         // color index.  All cels that reference this palette must refer
         // to the reverted palette index values.

         for (int i = 0 ; i < cels.size() ; i++)
         {
            Cel cel = (Cel) cels.elementAt(i) ;
            Palette celpalette = cel.getPalette() ;
            if (celpalette == null || celpalette != editpalette) continue ;
            int trans = cel.getTransparentIndex() ;
            cel.setTransparentIndex(transparent) ;
            cel.setBackgroundIndex(background) ;
            if (trans != transparent) cel.changeTransparency(0) ;
         }

         // Redraw the page.  Note that the background color may have changed.

         Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
         setBackground(config.getBorderColor()) ;
         parent.setBackground(config.getBorderColor()) ;
         initcolor(multipalette) ;
         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
      }

      
      // Utility function to apply a transparency update to a cel.

      void updateCelTransparency(Object editobject, int transparent)
      {
         if (config == null) return ;
         if (!(editobject instanceof Cel)) return ;
         Cel c = (Cel) editobject ;
         c.setTransparentIndex(transparent) ;
         c.changeTransparency(0) ;

         // Redraw the page.

         Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
         initcolor(multipalette) ;
         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
      }

      
      // Utility function to apply a background color change.

      private void updateBackground(boolean rgb, int n)
      {
         if (config == null) return ;
         if (rgb) setBorderRgb(n) ; else setBorderIndex(n) ;
         Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
         if (multipalette == null) 
            background = getBackground() ;
         else
            initcolor(multipalette) ;
         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
      }
   }


   // Inner class to construct an undoable image edit operation.  These edits
   // occur when we invoke the ImageFrame utility to alter images and the
   // panel frame is updated on return.  We copy our edit data to ensure we
   // retain consistency.
   //
   // Note:  This is incomplete.  

   class UndoableImageEdit extends AbstractUndoableEdit
   {
      private Object editobject = null ;
      private Image oldimage = null ;
      private Image newimage = null ;
      private Palette oldpalette = null ;
      private Palette newpalette = null ;
      private int oldtransparent = 0 ;
      private int newtransparent = 0 ;
      private int oldloop = 0 ;
      private int newloop = 0 ;
      private int type = 0 ;


      // Constructor for image updates

      public UndoableImageEdit(Object editobject, int oldtransparent, int loop, Image img, Palette p)
      {
         type = 1 ;
         this.editobject = editobject ;
         if (editobject instanceof Cel)
         {
            Cel c = (Cel) editobject ;
            this.oldtransparent = oldtransparent ;
            this.newtransparent = c.getTransparentIndex() ;
            this.oldimage = img ;
            this.newimage = c.getImage() ;
            this.oldpalette = p ;
            this.newpalette = c.getPalette() ;
            this.oldloop = loop ;
            this.newloop = c.getLoopCount() ;
         }
      }

      
      // Return the undo/redo menu name

      public String getPresentationName()
      {
         return Kisekae.getCaptions().getString("UndoImageEditName") ;
      }

      
      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (!(editobject instanceof Cel)) return ;
         Cel c = (Cel) editobject ;
         c.setLoopCount(oldloop) ;
         c.setImage(oldimage) ;
         c.setPalette(oldpalette) ;
         c.setPaletteID((oldpalette == null) ? null : oldpalette.getIdentifier()) ;
         Object o = c.getGroup() ;
         if (o instanceof Group) 
         {
            Group g = (Group) o ;
            g.updateBoundingBox() ;
         }
      
         // If we dithered to a KCF palette then we need to remove the palette 
         // from the configuration on an undo.

         if (oldpalette == null && newpalette != null)
         {
            if (ArchiveFile.isPalette(newpalette.getName())) 
            {
               Vector v = config.getPalettes() ;
               if (v != null) 
               {
                  v.removeElement(newpalette) ;
                  c.setPaletteID(null) ;
                  c.setInitPaletteID(null) ;
                  Object pid = newpalette.getIdentifier() ;
                  newpalette.setIdentifier(new Integer(-1)) ;
                  newpalette.removeKey(newpalette.getKeyTable(),config.getID(),pid) ;
                  if (config.getPaletteCount() == 0)
                  {
                     setBackground(config.getBorderColor()) ;
                     parent.setBackground(config.getBorderColor()) ;
                  }
               }
            }
         }
         
         // Now, for all ambiguous cels, reload the cel image.
         
         reloadCel(c) ;
         reloadAmbiguousCels(c) ;
         updateCelTransparency(editobject,oldtransparent) ;
         undoredo = false ;
      }

      
      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (!(editobject instanceof Cel)) return ;
         Cel c = (Cel) editobject ;
         c.setLoopCount(newloop) ;
         c.setImage(newimage) ;
         c.setPalette(newpalette) ;
         c.setPaletteID((newpalette == null) ? null : newpalette.getIdentifier()) ;
         Object o = c.getGroup() ;
         if (o instanceof Group) 
         {
            Group g = (Group) o ;
            g.updateBoundingBox() ;
         }
      
         // If we dithered to a KCF palette then we need to add the palette into
         // the configuration on a redo.

         if (oldpalette == null && newpalette != null)
         {
            if (ArchiveFile.isPalette(newpalette.getName())) 
            {
               Vector v = config.getPalettes() ;
               if (v != null) 
               {
                  int n = v.size() ;
                  v.addElement(newpalette) ;
                  Object pid = new Integer(n) ;
                  c.setPaletteID(pid) ;
                  c.setInitPaletteID(pid) ;
                  newpalette.setIdentifier(pid) ;
                  newpalette.setKey(newpalette.getKeyTable(),config.getID(),pid) ;
                  if (config.getPaletteCount() == 1)
                  {
                     setBackground(config.getBorderColor()) ;
                     parent.setBackground(config.getBorderColor()) ;
                  }
               }
            }
         }
         
         // Now, for all ambiguous cels, reload the cel image.
         
         reloadCel(c) ;
         reloadAmbiguousCels(c) ;
         updateCelTransparency(editobject,newtransparent) ;
         undoredo = false ;
      }

      
      // Utility function to apply a transparency update to a cel.

      void updateCelTransparency(Object editobject, int transparent)
      {
         if (config == null) return ;
         if (!(editobject instanceof Cel)) return ;
         Cel c = (Cel) editobject ;
         c.setTransparentIndex(transparent) ;
         c.changeTransparency(0) ;

         // Redraw the page.

         Integer multipalette = (page != null) ? page.getMultiPalette() : null ;
         initcolor(multipalette) ;
         parent.updateMenu() ;
         parent.updateToolBar() ;
         showpage() ;
      }
   }


   // Inner class to construct an undoable cel level change.
   // These edits occur if we adjust the cel levels.

   class UndoableLayerEdit extends AbstractUndoableEdit
   {
      private PageSet page = null ;
      private Vector oldlevels = null ;
      private Vector newlevels = null ;


      // Constructor for cel layer adjustment changes

      public UndoableLayerEdit(PageSet p, Vector v1, Vector v2)
      {
         page = p ;
         oldlevels = v1 ;
         newlevels = v2 ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoLayerName") ;  }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;

         // Switch to the page to which the event applies.

         if (!page.equals(panel.getPage()))
            initpage(((Integer) page.getIdentifier()).intValue()) ;

         // Perform the level change.

         adjustLayer(newlevels,oldlevels) ;
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;

         // Switch to the page to which the event applies.

         if (!page.equals(panel.getPage()))
            initpage(((Integer) page.getIdentifier()).intValue()) ;

         // Perform the level change.

         adjustLayer(oldlevels,newlevels) ;
         undoredo = false ;
      }
   }


   // Inner class to construct an undoable cel level change.
   // These edits occur if we adjust the cel levels.

   class UndoablePageSetEdit extends AbstractUndoableEdit
   {
      private PageSet page = null ;
      private Integer oldmp = null ;
      private Integer newmp = null ;


      // Constructor for cel layer adjustment changes

      public UndoablePageSetEdit(PageSet p, Integer mp1, Integer mp2)
      {
         page = p ;
         oldmp = mp1 ;
         newmp = mp2 ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      {
         Object o = (page != null) ? page.getIdentifier() : null ;
         String s = (o instanceof Integer) ? o.toString() : "" ;
         String s1 = Kisekae.getCaptions().getString("UndoPagePaletteName") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1+1) + s + s1.substring(j1) ;
         return s1 ;
      }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;

         // Perform the level change.

         adjustPalette(page,oldmp) ;
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;

         // Perform the level change.

         adjustPalette(page,newmp) ;
         undoredo = false ;
      }

      // Utility function to perform the palette change.

      private void adjustPalette(PageSet p, Integer mp)
      {
         if (p == null || mp == null) return ;
         p.setInitialMultiPalette(mp) ;
         p.setMultiPalette(mp) ;
         PageSet ps = getPage() ;
         if (ps == p)
         {
            initcolor(mp);
            showpage() ;
            showStatus(null) ;
         }
      }
   }


   // Inner class to construct an undoable panel frame scaling operation.
   // These edits occur if we scale the configuration.

   class UndoableSizeEdit extends AbstractUndoableEdit
   {
      private int type = 0 ;
      private float oldsf = 1.0f ;
      private float newsf = 1.0f ;
      private Dimension oldsize = null ;
      private Dimension newsize = null ;
      private Cel cel = null ;
      private String oldattr = null ;
      private String newattr = null ;


      // Constructor for panel frame scaling updates

      public UndoableSizeEdit(int type, float oldsf, float newsf)
      {
         this.type = type ;
         this.oldsf = oldsf ;
         this.newsf = newsf ;
      }

      // Constructor for configuration size updates

      public UndoableSizeEdit(int t, Dimension d1, Dimension d2)
      {
         type = t ;
         oldsize = d1 ;
         newsize = d2 ;
      }

      // Constructor for a cel size change

      public UndoableSizeEdit(int t, Cel c, Dimension d1, Dimension d2)
      {
         type = t ;
         cel = c ;
         oldsize = new Dimension(d1) ;
         newsize = new Dimension(d2) ;
      }

      // Constructor for a cel attribute change

      public UndoableSizeEdit(int t, Cel c, String s1, String s2)
      {
         type = t ;
         cel = c ;
         oldattr = new String((s1 != null) ? s1 : "") ;
         newattr = new String((s2 != null) ? s2 : "") ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      {
         if (type == SIZE)
            return Kisekae.getCaptions().getString("UndoSizeName") ;
         if (type == SCALE)
            return Kisekae.getCaptions().getString("UndoScaleName") ;
         if (type == SIZECEL)
            return Kisekae.getCaptions().getString("UndoSizeName") ;
         if (type == ATTRIBUTES)
            return Kisekae.getCaptions().getString("UndoAttributesName") ;
         return "" ;
      }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (type == SCALE) resize(oldsf) ;
         if (type == SIZE) setConfigSize(oldsize) ;
         if (type == SIZECEL) 
         { 
            cel.setSize(oldsize) ; 
            if (ArchiveFile.isImage(cel.getName())) 
            {
               try { cel.scaleImage(oldsize) ; }
               catch (KissException e) { }
            }
            Object o = cel.getGroup() ;
            if (o instanceof Group) ((Group) o).updateBoundingBox() ;
            showpage() ; 
         }
         if (type == ATTRIBUTES) 
         { 
            cel.resetAttributes() ;
            cel.setAttributes(oldattr) ; 
            showpage() ; 
         }
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (type == SCALE) resize(newsf) ;
         if (type == SIZE) setConfigSize(newsize) ;
         if (type == SIZECEL) 
         { 
            cel.setSize(newsize) ; 
            if (ArchiveFile.isImage(cel.getName())) 
            {
               try { cel.scaleImage(newsize) ; }
               catch (KissException e) { }
            }
            Object o = cel.getGroup() ;
            if (o instanceof Group) ((Group) o).updateBoundingBox() ;
            showpage() ; 
         }
         if (type == ATTRIBUTES) 
         { 
            cel.resetAttributes() ;
            cel.setAttributes(newattr) ; 
            showpage() ; 
         }
         undoredo = false ;
      }
   }


   // Inner class to construct an undoable object transparency adjustment.
   // These edits occur if we change transparency through a property dialog.

   class UndoableTransparencyEdit extends AbstractUndoableEdit
   {
      private int type = 0 ;
      private int oldtrans = 0 ;
      private int newtrans = 0 ;
      private KissObject object = null ;


      // Constructor for transparency updates

      public UndoableTransparencyEdit(KissObject o, int oldtrans, int newtrans)
      {
         this.object = o ;
         this.oldtrans = oldtrans ;
         this.newtrans = newtrans ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoTransparencyName") + " " + object.getName() ; }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            int relative = oldtrans - newtrans ;
            if (relative != 0)
            {
               group.changeTransparency(relative) ;
               Vector cels = group.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
                  Cel cel = (Cel) cels.elementAt(i) ;
                  int n = cel.getTransparency() ;
                  cel.setInitTransparency(n) ;
               }
            }
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            int relative = oldtrans - newtrans ;
            if (relative != 0)
            {
               celgroup.changeTransparency(relative) ;
               Vector cels = celgroup.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
                  Cel cel = (Cel) cels.elementAt(i) ;
                  int n = cel.getTransparency() ;
                  cel.setInitTransparency(n) ;
               }
            }
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setInitTransparency(oldtrans) ;
            cel.setTransparency(oldtrans) ;
            cel.changeTransparency(0) ;
         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            int relative = newtrans - oldtrans ;
            if (relative != 0)
            {
               group.changeTransparency(relative) ;
               Vector cels = group.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
                  Cel cel = (Cel) cels.elementAt(i) ;
                  int n = cel.getTransparency() ;
                  cel.setInitTransparency(n) ;
               }
            }
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            int relative = newtrans - oldtrans ;
            if (relative != 0)
            {
               celgroup.changeTransparency(relative) ;
               Vector cels = celgroup.getCels() ;
               for (int i = 0 ; i < cels.size() ; i++)
               {
                  Cel cel = (Cel) cels.elementAt(i) ;
                  int n = cel.getTransparency() ;
                  cel.setInitTransparency(n) ;
               }
            }
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setInitTransparency(newtrans) ;
            cel.setTransparency(newtrans) ;
            cel.changeTransparency(0) ;
         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }
   }


   // Inner class to construct an undoable object visibility adjustment.
   // These edits occur if we change visibility through a property dialog.

   class UndoableVisibilityEdit extends AbstractUndoableEdit
   {
      private int type = 0 ;
      private boolean oldvisibility ;
      private boolean newvisibility ;
      private KissObject object = null ;


      // Constructor for transparency updates

      public UndoableVisibilityEdit(KissObject o, boolean oldvisibility, boolean newvisibility)
      {
         this.object = o ;
         this.oldvisibility = oldvisibility ;
         this.newvisibility = newvisibility ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoVisibilityName") + " " + object.getName() ; }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            group.setVisible(oldvisibility) ;
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            celgroup.setVisible(oldvisibility) ;
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setVisibility(oldvisibility);
         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            group.setVisible(newvisibility) ;
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            celgroup.setVisible(newvisibility) ;
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setVisibility(newvisibility) ;
         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }
   }


   // Inner class to construct an undoable object visibility adjustment.
   // These edits occur if we change visibility through a property dialog.

   class UndoableGhostEdit extends AbstractUndoableEdit
   {
      private int type = 0 ;
      private boolean oldvisibility ;
      private boolean newvisibility ;
      private KissObject object = null ;


      // Constructor for transparency updates

      public UndoableGhostEdit(KissObject o, boolean oldvisibility, boolean newvisibility)
      {
         this.object = o ;
         this.oldvisibility = oldvisibility ;
         this.newvisibility = newvisibility ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoGhostName") + " " + object.getName() ; }

      // Undo a change.

      public void undo()
      {
         super.undo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            group.setGhost(oldvisibility) ;
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            celgroup.setGhost(oldvisibility) ;
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setGhost(oldvisibility);

         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }

      // Redo a change.

      public void redo()
      {
         super.redo() ;
         undoredo = true ;
         if (object instanceof Group)
         {
            Group group = (Group) object ;
            group.setGhost(newvisibility) ;
         }
         if (object instanceof CelGroup)
         {
            CelGroup celgroup = (CelGroup) object ;
            celgroup.setGhost(newvisibility) ;
         }
         if (object instanceof Cel)
         {
            Cel cel = (Cel) object ;
            cel.setGhost(oldvisibility) ;
         }
         showpage() ;
         showStatus(null) ;
         undoredo = false ;
      }
   }
   
   
   // A comparison function to order cels in the
   // same sequence as the cel list.
   
   class CelListComparator implements Comparator
   {
      private boolean ascending ;
      private int [] order ;

      public CelListComparator() { this(true,celList) ; }
      public CelListComparator(int [] order) { this(true,order) ; }
      public CelListComparator(boolean ascending, int [] order)
      {
         this.ascending = ascending ;
         this.order = order ;
      }

      public int compare(Object o1, Object o2)
      {
         int n1 = -1 ;
         int n2 = -1 ;
         Vector cels = config.getCels() ;
         if (cels == null) return 0 ;
         if (order == null) return 0 ;
         
         for (int i = 0 ; i < order.length ; i++)
         {
            Cel drawcel ;
            int c = order[i] ;
            try { drawcel = (Cel) cels.elementAt(c) ; }
            catch (ArrayIndexOutOfBoundsException e) { continue ; }
            if (o1 == drawcel) n1 = i ;
            if (o2 == drawcel) n2 = i ;
            if (n1 >= 0 && n2 >= 0) break ;
         }

         int result = (n1 > n2) ? 1 : -1 ;
         if (n1 == n2) result = 0 ;
         if (!ascending) result = -result ;
         return result ;
      }
   }
}
