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
* FKiSS Action class
*
* Purpose:
*
* This class represents an FKiSS action.  All FKiSS event actions are
* currently defined in this class.
*
* An action is a parameterized request to make a state change to an
* FKiSS object.  Action objects contain their implementation method.
* A series of actions are performed when an FKiSS event occurs.
*
*/

import java.io.* ;
import java.awt.* ;
import java.util.* ;
import java.net.* ;
import java.awt.print.* ;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.text.NumberFormat ;
import javax.swing.* ;
import javax.swing.text.* ;
import javax.swing.SwingUtilities ;
import javax.media.* ;


final class FKissAction extends KissObject
{
   private static Vector groupsetpool = new Vector() ;

   private String identifier = null ;		// Action name (map, unmap, ...)
   private Vector parameters = null ;		// Action parameter list
   private Object object = null ;			// Action object (cel, group, ...)
   private Configuration config = null ;	// Current configuration
   private Object cid = null ;				// Configuration object id
   private Rectangle box = null ;			// The action bounding box
   private FKissEvent event = null ;		// Parent event for this action
   private Variable variable = null ;		// Our variable storage
   private Alarm alarm = null ;		      // The action alarm for timers
   private String comment = null ;			// Action comment text
   private String message = null ;			// Notify box message
   private Object [] skip = null ;        // Skip actions data buffer
   private boolean breakpoint = false ;   // True if break on this action
   private boolean nobreakpoint = false ; // True if no break on this action
   private boolean currentbreak = false ; // Set if current breakpoint
   private boolean initializefor = true ;	// For statement to be initialized
   private boolean initializerepeat = true ;	// Repeat statement to be initialized
   private int code = -1 ;						// Code value for this action
   private int sequence = -1 ;            // Our action sequence number
   private int loopentry = -1 ;           // Sequence number of loop call
	private long starttime = 0 ;			   // Action start time
	private long runtime = 0 ;				   // Action cummulative run time
	private long invocations = 0 ;	 	   // Action run count
   private int indentlevel = 0 ;          // toString() leading space


   // Constructor

   public FKissAction(FKissEvent e, String id, Configuration c)
   {
      config = c ;
      event = e ;
      cid = c.getID() ;
      identifier = (id != null) ? id : null ;
      parameters = new Vector() ;
      variable = c.getVariable() ;
      code = EventHandler.getActionNameKey(id) ;
   }


   // Class methods for managing our pool of Group objects retained for
   // movement of attached groups.  These objects are allocated as
   // required and reused to minimize memory management overhead.

   static void clearGroupsetPool() { groupsetpool = new Vector() ; }

   static Group allocateGroupset()
   {
      if (groupsetpool.size() == 0) return new Group() ;
      Group g = (Group) groupsetpool.elementAt(0) ;
      groupsetpool.removeElement(g) ;
      return g ;
   }

   static void returnGroupset(Group g)
   {
      if (g == null) return ;
      g.init() ;
      groupsetpool.addElement(g) ;
   }


   // Object state change methods
   // ---------------------------

   // Set the object identifier.

   void setIdentifier(Object id)
   { identifier = (id != null) ? id.toString() : null ; }

   // Set the action parameter string vector

   void addParameters(Vector v) { parameters = v ; }

   // Set the action object reference.

   void setObject(Object o) { object = o ; }

   // Set the source code line number where this object was declared.

   void setLine(int line) { this.line = line ; }

   // Set the toString() leading space for if-else indented output.

   void setIndentLevel(int n) { indentlevel = n ; }   

   // Set the object comment text.

   void setComment(String s) { comment = s ; }

   // Set the action sequence line number for the event actions.

   void setSequence(int n) { sequence = n ; }

   // Set the sequence line number for any loop caller.

   void setLoopEntry(int n) { loopentry = n ; }

   // The action breakpoint, if set, is used to invoke a debug dialog when
   // this action is processed.

   void setBreakpoint(boolean b)
   {
      breakpoint = b ;
      nobreakpoint = (event != null) ? event.getNoBreakpoint() : false ;
   }

   // The no action breakpoint, if set, does not allow this action to
   // break normal processing.

   void setNoBreakpoint(boolean b) { nobreakpoint = b ; }

   // The current breakpoint, if set, indicates that this action is the
   // currently active breakpoint instruction.

   void setCurrentBreak(boolean b) { currentbreak = b ; }

   // Set the parent event for this action.

   void setEvent(FKissEvent e) { event = e ; }

   // Initialize for a new for statement invocation

   void initfor() { initializefor = true ; }


   // Object state reference methods
   // ------------------------------

   // Return the action object.

   Object getObject() { return object ; }

   // Return the object identifier.

   Object getIdentifier() { return identifier ; }

   // Return the object comment text.

   String getComment() { return comment ; }

   // Return the source code line number.

   int getLine() { return line ; }

   // Return the action code value.

   int getCode() { return code ; }

   // Return the action sequence number in the event.

   int getSequence() { return sequence ; }

   // Return the action bounding box.

   Rectangle getBoundingBox() { return box ; }

   // Return the object parameters.

   Vector getParameters() { return parameters ; }

   // Return the first object parameter.

   String getFirstParameter()
   { return (parameters.size() > 0) ? (String) parameters.elementAt(0) : null ; }

   // Return the second object parameter.

   String getSecondParameter()
   { return (parameters.size() > 1) ? (String) parameters.elementAt(1) : null ; }

   // Return the parent event for this action.

   FKissEvent getEvent() { return event ; }

   // Return the last alarm referenced on a timer command.

   Alarm getAlarm() { return alarm ; }

   // Return the action breakpoint switch.  If a breakpoint is set then
   // a debug dialog will be invoked when this action is processed.

   boolean getBreakpoint() { return breakpoint ; }

   // Return the no breakpoint switch.  If the no breakpoint switch is set
   // then this action cannot initiate a breakpoint interrupt.

   boolean getNoBreakpoint() { return nobreakpoint ; }

   // Return the current breakpoint indicator.  If we are breakpoint
   // processing and stopped at this command this value is true.

   boolean isCurrentBreak() { return currentbreak ; }

   // Return true if this action can schedule an alarm.  We test this 
   // if breakpointing and stepping into the action and the alarm is 
   // not scheduled (alarm = null)

   boolean isTimerAction() { return 
      (code == 4 || code == 7 || // iffixed
       code == 5 || code == 8 || // ifmapped
       code == 6 || code == 9 || // ifmoved
       code == 26) ; }



   // Object utility methods
   // ----------------------

   // Perform the action command.  We will establish a bounding box for
   // the area of change relative to the current panel context in which
   // this action occurs.  The activation thread helps us manage alarm
   // activation.

   Object [] doAction(PanelFrame panel, Thread thread, Thread currentthread, Hashtable eventstate)
   {
      int i, n1, n2, n3, n4 ;
      long l1, l2, l3, l4 ;
      double d1, d2, d3, d4 ;
      String s, s1, s2, s3, s4 ;
      Object o, o1, o2, o3, o4 ;

      // We need to declare local storage variables as the doAction method
      // can be called recursively.

      this.box = null ;					 // The action bounding box
      Vector evt = null ;				 // The kiss object event list
      Rectangle box = null ;			 // The action bounding box
      Thread activator = null ;      // The thread that activates alarms
      String exception = null ;      // The action exception command
      Object exceptionitem = null ;  // The action exception object
      long actionsprocessed = 0 ;    // The action statements processed
      
      // Declare initialized repeat() variable storage.
      
      int repeatlimit = 0 ;
      int repeatcount = 0 ;
      String repeatvbl = "" ;
      boolean directkiss = OptionsDialog.getDirectKissCompatibility() ;
		starttime = System.currentTimeMillis() ; // Java 1.5

      // Process event actions unless we are skipping code due to
      // a logical condition failure or loop termination.

      if (event.getSkip(currentthread))
      {
         String context = event.getSkipContext(currentthread) ;
         if (context != null)
         {
            if (context.startsWith("if"))  		// return if not else or endif
            {
               if (EventHandler.isIfAction(this))
                  event.setIfLevel(event.getIfLevel() + 1) ;
               if (!EventHandler.isElseEndAction(this))
               {
               return null ;
               }
            }
            else if (context.startsWith("while"))	// return if not endwhile
            {
               if (!(code == 65)) return null ;
            }
            else if (context.startsWith("for"))		// return if not next
            {
               if (!(code == 73)) return null ;
            }
         }
      }
      
      // Write the event trace to the log file. Note, event, action, 
      // and variable traces set as 'nobreakpoint' in the FKiSS editor 
      // have a sentinal '*' inserted in the text string. These trace 
      // lines are not forwarded to the trace dialog although they can 
      // appear in the log file if the DebugDisabled option is set.
      
      if (OptionsDialog.getDebugAction() && (!getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
      {
         String bp = (nobreakpoint) ? "*" : " " ;
         System.out.println(" >"+bp+"[" + currentthread.getName() + "] Event action " + toString()) ;
      }

      // Determine the class of the action object.  Action commands expect
      // specific objects of type cel, palette, alarm, and so on.

      KissObject kiss = (object instanceof KissObject) ? (KissObject) object : null ;
      activator = thread ;
      alarm = null ;

      // Determine the class of the event object.  If the event is an
      // alarm we must reference the alarm and obtain its activation
      // flag and propagate it through any associated timer actions.

      if ("alarm".equals(event.getIdentifier()))
      {
         o = evaluateParam((String) event.getFirstParameter()) ;
         if (o instanceof String) o = ((String) o).toUpperCase() ;
         Alarm alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,o) ;
         activator = (alarm != null) ? alarm.getActivator() : null ;
         if (activator == null) activator = thread ;
         
         // If the alarm has been stopped while in the eventhandler queue
         // we must ignore the actions.
         
         if (alarm != null && alarm.isStopped() && sequence == 0) 
         {
            Object [] result = new Object[4] ;
            result[0] = null ;
            result[1] = "terminate" ;
            result[2] = exceptionitem ;
            result[3] = new Long(0) ;
            return result ;
         }
      }
            
      // Feature to play sound() background sound playback through mediaplayer.
      // Useful for PlayFKiss compatibility which implements single sound only.

      if (code == 25 && OptionsDialog.getLongSoundMedia())
      {
         o1 = variable.getValue((String) parameters.elementAt(0),event) ;
         if (!"".equals(o1)) 
         {
            // Identify the action object.
            if (o1 instanceof String)
            {
               s1 = ((String) o1).toUpperCase() ;
               kiss = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;
               if (kiss instanceof Audio) 
               {
                  Audio a = (Audio) kiss ;
                  int n = a.getDuration() ;
                  int m = OptionsDialog.getLongDuration() ;
                  if (n >= m && m > 0) 
                  {
                     code = 93 ;
                     a.setBackground(true) ;
                     if (OptionsDialog.getDebugSound())
                        System.out.println("FKissAction: " + getName() + " converted to MediaPlayer, duration " + n + " seconds");
                  }
               }
            }
         }
               
         // Stop the media player if requested.  

         MediaFrame mf = (config == null) ? null : config.getMediaFrame() ;
         if (mf != null && ("".equals(o1) || "silence.wav".equals(o1)))
         {
            Runnable runner = new Runnable()
            { public void run() { mf.closeMedia() ; } } ;
            Thread runthread = new Thread(runner) ;
            runthread.start() ;
         }
      }
     
      // Watch for syntax errors in the parameter specifications.
      // These errors are caught at execution time and are ignored.

      try
      {
         switch (code)
         {

         // Invert the display of a cel or group.

         case 0:		// "altmap"
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            o = (kiss instanceof Cel) ? ((Cel) kiss).getGroup() : kiss ;
            if (panel != null && OptionsDialog.getMapCollide())
               panel.setCollisionState(o,eventstate,true,true) ;
            kiss.altVisible(OptionsDialog.getAllAmbiguous(),kiss) ; 
            box = kiss.getAllBoundingBox() ;
            if (panel != null && OptionsDialog.getMapCollide())
               if (OptionsDialog.getImmediateCollide())
                  box = panel.fireCollisionEvents(o,eventstate,thread,box) ;
            break ;


         // Change the current palette.

         case 1:		// "changecol"
            if (panel == null) break ;
            if (parameters.size() < 1) break ;
            int color = (int) variable.getIntValue((String) parameters.elementAt(0),event) ;
            if (config != null && color >= config.getPaletteGroupCount()) break ;
            panel.initcolor(new Integer(color)) ;
            panel.showpage() ;
            box = new Rectangle(panel.getSize()) ;
            break ;


         // Change the page set.

         case 2:		// "changeset"
            if (panel == null) break ;
            if (parameters.size() < 1) break ;
            int page = (int) variable.getIntValue((String) parameters.elementAt(0),event) ;
            if (config != null && page >= config.getPageCount()) break ;
            PageSet pageset = panel.getPage() ;
            Integer pid = (pageset != null) ? (Integer) pageset.getIdentifier() : null ;
            if (pid != null && pid.intValue() != page) panel.releaseMouse() ;
            panel.initpage(page) ;
            panel.showpage() ;
            box = new Rectangle(panel.getSize()) ;
            break ;


         // Show a message dialog box.

         case 3:		// "debug"
         case 20:		// "notify"
         case 66:		// "showstatus"
            message = "" ;
            Image img = null ;
            if (parameters.size() > 0)
            {
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
               if (kiss instanceof Cel)
                  img = ((Cel) kiss).getImage() ;
               else
               {
                  for (i = 0 ; i < parameters.size() ; i++)
                  {
                     o = variable.getValue((String) parameters.elementAt(i),event) ;
                     if (o != null) message += o.toString() ;
                  }
               }
            }

            // Show a message in the status bar.  If there is no message
            // text parameter then we show the default copyright text.

            if (code == 66)
            {
               if (panel != null && !OptionsDialog.getDebugMouse())
               {
                  if (parameters.size() == 0)
                     panel.showStatus(null) ;
                  else
                     panel.showStatus(message) ;
               }
               break ;
            }

            // Show a debug message in the AWT event handler thread.

            if (code == 3)
            {
               final String msg = new String(message) ;
               Runnable notify = new Runnable()
               {
                  public void run()
                  {
                     MainFrame mfm = Kisekae.getMainFrame() ;
                     if (mfm != null) mfm.debugFKiss(msg) ;
                  }
               } ;
               if (!Kisekae.isBatch())
                  javax.swing.SwingUtilities.invokeLater(notify);
               break ;
            }

            // Show the notify box in the AWT event handler thread.

            if (panel == null) break ;
            final Image image = img ;
            final String notifymsg = new String(message) ;
            Runnable notify = new Runnable()
            {
               public void run()
               {
                  NotifyDialog nd = new NotifyDialog(Kisekae.getMainFrame(),
                     "Notify",notifymsg,image,false) ;
                  nd.setVisible(true) ;
                  if (panel != null) panel.releaseMouse(true) ;
               }
            } ;
            if (!Kisekae.isBatch())
               javax.swing.SwingUtilities.invokeLater(notify);
            break ;


         // Set a timer if a group or cel is fixed or not. A cel is
         // fixed if its parent group is fixed.

         case 4:		// "iffixed"
         case 7:		// "ifnotfixed"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if(kiss instanceof Cel) 
            {
               o = ((Cel) kiss).getGroup() ;
               if (!(o instanceof Group)) break ;
               kiss = (Group) o ;
            }
            if (!(kiss instanceof Group)) break ;
            Point flex = ((Group) kiss).getFlex() ;
            if (flex == null) flex = new Point(0,0) ;
            if (code == 4 && flex.y == 0) break ;
            if (code == 7 && flex.y != 0) break ;
            String alarmid = evaluateParam((String) parameters.elementAt(1)) ;
            if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
            alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            int delay = variable.getIntValue((String) parameters.elementAt(2),event) ;
            if (alarm != null) alarm.setInterval(delay,activator) ;
            event.setAlarmEnable(alarm) ;
            setAlarmArguments(alarm,parameters,3) ;
            if (alarm != null) alarm.setSource(getName() + " in " + event.getName());
            break ;


         // Set a timer if a cel is mapped or not.

         case 5:		// "ifmapped"
         case 8:		// "ifnotmapped"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            boolean visible = kiss.isVisible() ;
            if (code == 5 && !visible) break ;
            if (code == 8 && visible) break ;
            alarmid = evaluateParam((String) parameters.elementAt(1)) ;
            if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
            alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            delay = variable.getIntValue((String) parameters.elementAt(2),event) ;
            if (alarm != null) alarm.setInterval(delay,activator) ;
            event.setAlarmEnable(alarm) ;
            setAlarmArguments(alarm,parameters,3) ;
            if (alarm != null) alarm.setSource(getName() + " in " + event.getName());
            break ;


         // Set a timer if a cel or group has moved or not.

         case 6:		// "ifmoved"
         case 9:		// "ifnotmoved"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Group || kiss instanceof Cel)) break ;
            PageSet ps = (panel != null) ? panel.getPage() : null ;
            Point svbox = kiss.getInitialLocation(ps) ;
            Point location = kiss.getLocation() ;
            if (code == 6 && (location.x == svbox.x && location.y == svbox.y)) break ;
            if (code == 9 && !(location.x == svbox.x && location.y == svbox.y)) break ;
            alarmid = evaluateParam((String) parameters.elementAt(1)) ;
            if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
            alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            delay = variable.getIntValue((String) parameters.elementAt(2),event) ;
            if (alarm != null) alarm.setInterval(delay,activator) ;
            event.setAlarmEnable(alarm) ;
            setAlarmArguments(alarm,parameters,3) ;
            if (alarm != null) alarm.setSource(getName() + " in " + event.getName());
            break ;


         // Show a cel or group.

         case 10:		// "map"
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            o = (kiss instanceof Cel) ? ((Cel) kiss).getGroup() : kiss ;
            if (panel != null && OptionsDialog.getMapCollide())
               panel.setCollisionState(o,eventstate,true,true) ;
            kiss.setVisible(true,OptionsDialog.getAllAmbiguous(),kiss) ;
            box = kiss.getAllBoundingBox() ;
            if (panel != null && OptionsDialog.getMapCollide())
               if (OptionsDialog.getImmediateCollide())
                  box = panel.fireCollisionEvents(o,eventstate,thread,box) ;
            break ;


         // Movement X restriction.  Note that this code is position sensitive.
         // The restriction can cause a move.  This code will fall though to
         // the move section if necessary.

         case 104:		// "restrictx(object,minx,maxx)"
            if (code == 104)
            {
               if (parameters.size() < 3) break ;
               if (kiss == null)
                  kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
               if (!(kiss instanceof Group)) break ;
               Group group = (Group) kiss ;
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
               n2 = variable.getIntValue((String) parameters.elementAt(2),event) ;
               group.setRestrictX(new Point(n1,n2)) ;

               // Move the group if necessary.  If we must move then it is the
               // parent object that is moved to retain a consistent attachment.

               Point restrictx = group.getRestrictX() ;
               if (restrictx == null) break ;
               location = group.getLocation() ;
               if (location == null) break ;
               if (location.x >= restrictx.x && location.x <= restrictx.y) break ;

               // If our restriction is unsatisfiable because of an attached
               // child restriction we detach all restricted children.

               if (OptionsDialog.getDetachRestricted() && restrictx.x > restrictx.y)
               {
                  Vector children = group.getChildren() ;
                  if (children != null)
                  {
                     for (i = 0 ; i < children.size() ; i++)
                     {
                        KissObject child = (KissObject) children.elementAt(i) ;
                        if (child.getRestrictX() != null) child.detach() ;
                     }
                  }
               }

               // If the group has a parent then movement of the group also
               // moves the parent if the group is fixed.  Restrictions will
               // be for the combined object as set above.  However, if this
               // group is mobile then it must move without its parent.  It
               // will be detached if not fixed and not glued or the restriction
               // is unsatisfiable.  Movement restrictions are adjusted as a
               // result of the detach.

               if (group.hasParent())
               {
                  boolean unsatisfiable = false ;
                  KissObject parent = group.getParent() ;
                  while (parent != null)
                  {
                     Point rx = parent.getRestrictX() ;
                     if (rx == null) break ;
                     if (rx.x > rx.y) { unsatisfiable = true ; break ; }
                     parent = parent.getParent() ;
                  }
                  if (group.getFlex().y == 0 || unsatisfiable)
                  {
                     if (!group.isGlued() || unsatisfiable) group.detach() ;
                  }
               }

               // Now find the parent object that must move.

               while (kiss.getParent() != null) kiss = kiss.getParent() ;
            }


         // Movement Y restriction.  Note that this code is position sensitive.
         // The restriction can cause a move.  This code will fall though to
         // the move section if necessary.

         case 105:		// "restricty(object,miny,maxy)"
            if (code == 105)
            {
               if (parameters.size() < 3) break ;
               if (kiss == null)
                  kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
               if (!(kiss instanceof Group)) break ;
               Group group = (Group) kiss ;
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
               n2 = variable.getIntValue((String) parameters.elementAt(2),event) ;
               group.setRestrictY(new Point(n1,n2)) ;

               // Move the group if necessary.

               Point restricty = group.getRestrictY() ;
               if (restricty == null) break ;
               location = group.getLocation() ;
               if (location == null) break ;
               if (location.y >= restricty.x && location.y <= restricty.y) break ;

               // If our restriction is unsatisfiable because of an attached
               // child restriction we detach all restricted children.

               if (OptionsDialog.getDetachRestricted() && restricty.x > restricty.y)
               {
                  Vector children = group.getChildren() ;
                  if (children != null)
                  {
                     for (i = 0 ; i < children.size() ; i++)
                     {
                        KissObject child = (KissObject) children.elementAt(i) ;
                        if (child.getRestrictY() != null) child.detach() ;
                     }
                  }
               }

               // If the group has a parent then movement of the group also
               // moves the parent if the group is fixed.  Restrictions will
               // be for the combined object as set above.  However, if this
               // group is mobile then it must move without its parent.  It
               // will be detached if not fixed and not glued or the
               // restriction is unsatisfiable.  Movement restrictions are
               // adjusted as a result of the detach.

               if (group.hasParent())
               {
                  boolean unsatisfiable = false ;
                  KissObject parent = group.getParent() ;
                  while (parent != null)
                  {
                     Point ry = parent.getRestrictY() ;
                     if (ry == null) break ;
                     if (ry.x > ry.y) { unsatisfiable = true ; break ; }
                     parent = parent.getParent() ;
                  }
                  if (group.getFlex().y == 0 || unsatisfiable)
                  {
                     if (!group.isGlued() || unsatisfiable) group.detach() ;
                  }
               }

               // Now find the parent object that must move.

               while (kiss.getParent() != null) kiss = kiss.getParent() ;
            }


         // Move a cel or group by either a relative offset (move) or
         // to an absolute location (moveto).  We can also move a
         // group by a random offset in either the x or y direction,
         // or to a completely random location.

         case 11:		// "move"
         case 12:		// "movebyx"
         case 13:		// "movebyy"
         case 14:		// "moverandx"
         case 15:		// "moverandy"
         case 16:		// "moveto"
         case 17:		// "movetorand"
            if (panel == null) break ;
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;

            // Initialize.

            int x = 0 ;
            int y = 0 ;
            Object g = null ;
            Group groupset = null ;
            KissObject primary = kiss ;

            // Verify if we are FKiSS moving the currently selected object
            // through an external request such as an alarm activation. If
            // so, ignore the move as the user may be dragging the object.
            // Maximum fixed objects can always be moved with FKiSS commands.
            //
            // In PlayFKiss compatibility alarm moves on mouse controlled
            // objects are prohibited but moves through press() events
            // are allowed.

            g = panel.getGroup() ;
            if (g != null && !OptionsDialog.getReleaseMove())
//          if ((g != null && !OptionsDialog.getReleaseMove()) || 
//             (g != null && OptionsDialog.getPlayFKissCompatibility() && 
//              "alarm".equals(event.getIdentifier())))     
            {
               int gflex = (kiss instanceof Group) ? ((Group) kiss).getFlex().y : 0 ;
               if (gflex < OptionsDialog.getMaxLock())
               {
                  if ((kiss instanceof Group) && (g == kiss)) break ;
                  if ((kiss instanceof Cel) && (g == ((Cel) kiss).getGroup())) break ;
               }
            }

            // If we are moving the currently selected object due to move
            // coded between a mouse down and mouse up sequence, then we
            // must ignore subsequent user drags.

            if (g != null && !OptionsDialog.getDragMove())
            {
               if (((kiss instanceof Group) && (g == kiss)) ||
                     ((kiss instanceof Cel) && (g == ((Cel) kiss).getGroup())))
                  panel.setEnableDrag(false) ;
            }

            // If we are moving an attached group then create a group set for
            // the consolidated object.  If the group is fixed then we actually
            // move the parent.  This is a strong attachment where the fixed 
            // chain is moved.  Groups with attachment 1 are not considered
            // fixed for strong attachments.

            if (kiss instanceof Group && kiss.isAttached())
            {
               Group ag = (Group) kiss ;
               while (ag.getFlex() != null && ag.getFlex().y > 1 &&
                      ag.hasParent() && ag.isVisible())
                  ag = (Group) ag.getParent() ;
               groupset = allocateGroupset() ;
               groupset.addElement(ag) ;
               groupset.setPrimaryGroup((Group) kiss) ;
               groupset.setConstrain(ag.isConstrained()) ;
               groupset.setFlex(ag.getFlex()) ;
               kiss = groupset ;
               primary = ag ;
            }

            // The object to which the move displacement applies is the
            // primary group object.  The actual movement applies to the
            // active KiSS object.

            KissObject pko = kiss ;
            if (kiss instanceof Group)
            {
               Group p1 = ((Group) kiss).getPrimaryGroup() ;
               if (p1 != null) pko = p1 ;
            }

            // Random absolute moves do not take any parameters.

            Rectangle r = new Rectangle(pko.getBoundingBox()) ;
            if (code == 17 /* movetorand */)
            {
               x = Math.round((float) (Math.random() * panel.getPanelSize().width-r.width)) ;
               y = Math.round((float) (Math.random() * panel.getPanelSize().height-r.height)) ;
               x = x - r.x + pko.getOffset().x ;
               y = y - r.y + pko.getOffset().y ;
            }

            // All other moves take two parameters.

            else
            {
               if (parameters.size() < 3) break ;
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
               n2 = variable.getIntValue((String) parameters.elementAt(2),event) ;

               // Movebyx and movebyy are relative offsets to a second object.
               // We must pick up the second object's base location.

               if (code == 12 || code == 13)
               {
                  KissObject ko = findGroupOrCel((String) parameters.elementAt(1),event) ;
                  if (!(ko instanceof Cel || ko instanceof Group))
                  {
                     o = variable.getValue((String) parameters.elementAt(1),event) ;
                     throw new Exception("movebyx or movebyy object "
                        + (String) parameters.elementAt(1) + ", value " + o
                        + " not Cel or Group.") ;
                  }
                  x = (ko.getBoundingBox().x-ko.getOffset().x) - (r.x-pko.getOffset().x) ;
                  y = (ko.getBoundingBox().y-ko.getOffset().y) - (r.y-pko.getOffset().y) ;
               }

               // Moverandx and moverandy set random offsets to the current location.

               if (code == 14 || code == 15)
                  x = y = Math.round((float) (Math.random() * (n2 - n1))) + n1 ;

               // Moveto is an absolute movement and must include any offsets.
               // Cel moves are offset changes, so cel absolute movement must be
               // relative to the original cel base+initial offset.

               if (code == 16)
               {
                  Point offset = new Point(0,0) ;
                  if (pko instanceof Cel) 
                  {
                     Point baseoffset = ((Cel) pko).getBaseOffset() ;
                     Point initialoffset = ((Cel) pko).getInitialOffset() ;
                     offset.x += baseoffset.x + initialoffset.x ;
                     offset.y += baseoffset.y + initialoffset.y ;
                  }
                  else
                     offset = pko.getOffset() ;
                  x = n1 - r.x + offset.x ;
                  y = n2 - r.y + offset.y ;
               }

               // Compute the actual movement offsets.

               if (code == 11 /* move */) { x = n1 ; y = n2 ; }
               if (code == 12 /* movebyx */) { x += n2 ; y = 0 ; }
               if (code == 13 /* movebyy */) { y += n2 ; x = 0 ; }
               if (code == 14 /* moverandx */) { y = 0 ; }
               if (code == 15 /* moverandy */) { x = 0 ; }

               // Movement may be initiated through restriction commands.

               if (code == 104 /* restrictx */) { x = 0 ; y = 0 ; }
               if (code == 105 /* restricty */) { x = 0 ; y = 0 ; }
            }

            // Establish any object collision initial states.  Movement
            // events only occur if we move a visible or mapped object.
            // Events apply to the primary kiss object and any children.
            // We always establish collision states for every movement
            // action.  Only the first state is retained in the event 
            // that objects are moved more than once in the same event.
            // Events can be fired at the end of this action or at the
            // end of the event.  

            if (primary.isVisible())
            {
               g = (primary instanceof Cel) ? ((Cel) primary).getGroup()
                  : ((primary instanceof Group) ? (Group) primary : null) ;
               panel.setCollisionState(g,eventstate,false,false) ;
            }

            // If we are moving a cel we must relocate the cel in its
            // parent object.  This can reposition and resize the object.

            boolean celrelocated = false ;
            if (kiss instanceof Cel)
            {
               Cel cel = (Cel) kiss ;
               Point cellocation = cel.getLocation() ;
               if (OptionsDialog.getConstrainFKiss())
               {
                  Integer pn = null ;
                  PageSet p = panel.getPage() ;
                  float sf = panel.getScaleFactor() ;
                  if (p != null) pn = (Integer) p.getIdentifier() ;
                  Dimension d = new Dimension(panel.getPanelSize()) ;
                  d.width = (int) (d.width / sf) ;
                  d.height = (int) (d.height / sf) ;
               
                  // Obtain the bounded rectangle.  

                  r = new Rectangle(kiss.getBoundingBox()) ;
                  if (OptionsDialog.getConstrainVisible())
                  {
                     Point loc = kiss.getLocation() ;
                     r = kiss.getVisibleBoundingBox(pn) ;
                     r.x += loc.x ;
                     r.y += loc.y ;
                  }
               
                  // Move commands with a noconstrain option are not limited.

                  if (parameters.size() <= 3)
                  {
                     Point m = constrain(x,y,r,d,kiss.getRestrictX(),kiss.getRestrictY()) ;
                     x = m.x ;
                     y = m.y ;
                  }
               }
               cellocation.x += x ;
               cellocation.y += y ;
               cel.setLocation(cellocation) ;
               relocateCel(cel) ;
               celrelocated = (x != 0 || y != 0) ;
               o = cel.getGroup() ;
               x = y = 0 ;

               // Restrictions now apply to the parent group.

               if (o instanceof Group)
               {
                  kiss = (Group) o ;
                  Group p1 = ((Group) kiss).getPrimaryGroup() ;
                  if (p1 != null) pko = p1 ;
               }
            }

            // For mouse selected objects, if we are dragging, apply the
            // current drag displacement to the FKiSS move. This is to
            // stop positioning conflicts between the mouse drag and the 
            // FKiSS move.
            
            g = panel.getGroup() ;
            if ((g instanceof Group) && (g == kiss))
            {
               Point p = ((Group) g).getPlacement() ;
               if (p != null) { x += p.x ; y += p.y ; }
            }

            // If we moved an object that was attached to another object
            // and not glued tight, then detach it from its parent object
            // and fire any detached events. Sticky objects are not detached.
            // Travel up the attachment chain to locate a detachable object.

            if (OptionsDialog.getDetachMove())
            {
               Group detachable = null ;
               if (primary instanceof Group) detachable = (Group) primary ;
               while (detachable != null && detachable.hasParent())
               {
                  Point dflex = detachable.getFlex() ;
                  if (!detachable.isGlued() && dflex != null && dflex.y == 0)
                  {
                     detachable.detach() ;
                     break ;
                  }
                  o1 = detachable.getParent() ;
                  detachable = (o1 instanceof Group) ? (Group) o1 : null ;
               }
            }

            // Constrain any object movement to meet object restrictions.
            // For groups with children a consolidated object was constructed
            // for attached object movement.  The restrictions for the
            // consolidated object apply to the primary group location.
            // This can detach child objects from the moving parent if
            // the child is not glued to the parent and is not fixed.

            Vector detached = null ;
            if (kiss == groupset) pko = primary ;
            boolean detach = OptionsDialog.getDetachRestricted() ;
            if (pko instanceof Group)
            {
               while (true)
               {
                  Point r1 = pko.getLocation() ;
                  Point restrictx = pko.getRestrictX() ;
                  Point restricty = pko.getRestrictY() ;
                  int x1 = r1.x + x ;
                  int y1 = r1.y + y ;

                  // Restrict the movement offset if necessary.

                  boolean isX1 = false ; boolean isX2 = false ;
                  boolean isY1 = false ; boolean isY2 = false ;
                  if (restrictx != null && restrictx.x <= restrictx.y)
                  {
                     if (x1 < restrictx.x) { isX1 = true ; x1 = restrictx.x ; }
                     if (x1 > restrictx.y) { isX2 = true ; x1 = restrictx.y ; }
                  }
                  if (restricty != null && restricty.x <= restricty.y)
                  {
                     if (y1 < restricty.x) { isY1 = true ; y1 = restricty.x ; }
                     if (y1 > restricty.y) { isY2 = true ; y1 = restricty.y ; }
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
                        int dropx = x1 - (primaryloc.x - childloc.x) ;
                        int dropy = y1 - (primaryloc.y - childloc.y) ;
                        Point droplocation = new Point(dropx,dropy) ;
                        Object [] detachitem = new Object[2] ;
                        detachitem[0] = (Group) child ;
                        detachitem[1] = droplocation ;
                        detached.addElement(detachitem) ;
                        child.detach() ;
                        continue ;
                     }
                  
                     // If we are not detaching on exceeding restriction bounds
                     // then ensure that our restriction applies to the top
                     // level moving object.
                  
                     else if (child != null)
                     {
                        child.setRestrictedPlacement(x1-r1.x,y1-r1.y) ;
                        KissObject parent = child.getParent() ;
                        if (parent != null) parent.updateMoveRestrictions(null) ;
                        x1 = r1.x + x ;
                        y1 = r1.y + y ;
                     }
                  
                     // No restriction object but we are restricted. As we did
                     // not detach we need to confirm our initial restrictions
                     // to verify if we are really restricted.
                  
                     x1 = r1.x + x ;
                     y1 = r1.y + y ;
                     restrictx = pko.getInitialRestrictX() ;
                     restricty = pko.getInitialRestrictY() ;
                     if (restrictx != null && restrictx.x <= restrictx.y)
                     {
                        if (x1 < restrictx.x) x1 = restrictx.x ; 
                        if (x1 > restrictx.y) x1 = restrictx.y ; 
                     }
                     if (restricty != null && restricty.x <= restricty.y)
                     {
                        if (y1 < restricty.x) y1 = restricty.x ; 
                        if (y1 > restricty.y) y1 = restricty.y ; 
                     }

                     // Adjust the final displacement offset if our top level
                     // object is restricted.

                     x = x1 - r1.x ;
                     y = y1 - r1.y ;
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
               for (i = 0 ; i < detached.size() ; i++)
               {
                  Object [] detacheditem = (Object []) detached.elementAt(i) ;
                  Group ko = (Group) detacheditem[0] ;
                  Point droplocation = (Point) detacheditem[1] ;
                  Point kolocation = ko.getLocation() ;
                  int kox = droplocation.x - kolocation.x ;
                  int koy = droplocation.y - kolocation.y ;
                  Group kog = new Group() ;
                  kog.addElement(ko) ;
                  kog.setPlacement(kox,koy) ;
                  kog.drop() ;

                  // Remove the dropped object from our group set.

                  ((Group) kiss).removeElement(ko) ;
               }
            }

            // Constrain the object movement to the unscaled panel size.
            // Cels have already been moved.  The contained object may
            // have movement restrictions set.

            Point m = new Point(x,y) ;
            if (!celrelocated) r = new Rectangle(kiss.getBoundingBox()) ;
            if (OptionsDialog.getConstrainFKiss() && !celrelocated)
            {
               Integer pn = null ;
               PageSet p = panel.getPage() ;
               float sf = panel.getScaleFactor() ;
               if (p != null) pn = (Integer) p.getIdentifier() ;
               Dimension d = new Dimension(panel.getPanelSize()) ;
               d.width = (int) (d.width / sf) ;
               d.height = (int) (d.height / sf) ;
               
               // Obtain the bounded rectangle.  

               r = new Rectangle(kiss.getBoundingBox()) ;
               if (OptionsDialog.getConstrainVisible())
               {
                  Point loc = kiss.getLocation() ;
                  r = kiss.getVisibleBoundingBox(pn) ;
                  r.x += loc.x ;
                  r.y += loc.y ;
               }
               
               // Move commands with a noconstrain option are not limited.
               // Movement of unmapped objects is also not limited.

               int noconstrain = 0 ;
               if (parameters.size() > 3)
                  noconstrain = variable.getIntValue((String) parameters.elementAt(3),event) ;
               if (noconstrain == 0 && kiss.isVisible())
                  m = constrain(x,y,r,d,kiss.getRestrictX(),kiss.getRestrictY()) ;
            }

            // Move the object.

            kiss.setPlacement(m.x,m.y) ;
            kiss.drop() ;
//          panel.resetDrag(kiss) ;
         
            // If we have any restricted placements these must be cleared.

            if (!OptionsDialog.getDetachRestricted() && primary != null)
            {
               primary.clearRestrictedPlacement() ;
               primary.setPlacement(0,0,false) ;
               primary.rebuildMoveRestrictions() ;
            }

            // Compute the consolidated bounding box.  It is not necessarily
            // bounded to the panel frame area.

            KissObject parent = primary.getParent() ;
            kiss.updateMoveRestrictions(kiss.getChildren()) ;
            if (parent != null) parent.updateMoveRestrictions(parent.getChildren()) ;
            Rectangle kb = kiss.getBoundingBox() ;
            box = r.union(kiss.getBoundingBox()) ;
            
            // Fire any object overlap or collision events if the object has
            // been moved.  We cannot queue these events as they might be
            // recursive.  Note that immediate collisions are not fired for
            // movebyx and movebyy actions unless collision events are enabled.

            boolean moved = (x != 0 || y != 0 || celrelocated) ;
            if (!OptionsDialog.getMoveXYCollide())
               if (code == 12 || code == 13) moved = false ;
            if (primary.isVisible() && panel.isVisible() && moved)
            {
               g = (primary instanceof Cel) ? ((Cel) primary).getGroup()
                  : (primary instanceof Group) ? (Group) primary : null ;
               if (OptionsDialog.getImmediateCollide())
                  box = panel.fireCollisionEvents(g,eventstate,thread,box) ;
            }

            // Release any allocated groupset object.

            returnGroupset(groupset) ;
            break ;


         // Play a sound file.   Format: sound(name,repeatcount)

         case 18:		// "music"
         case 25:		// "sound"
            if (parameters.size() < 1) break ;
            String audiotype = null ;
            if (code == 18) audiotype = "music" ;
            if (code == 25) audiotype = "sound" ;

            // Determine if this is a stop request.

            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if ("".equals(o1)) { Audio.stop(config,audiotype) ;  break ; }
            if (OptionsDialog.getSoundSingle() && !OptionsDialog.getLongSoundMedia())
            {
         		if (OptionsDialog.getDebugSound())
         			System.out.println("FKissAction: SoundSingle about to play " + o1 + " stopping any sound") ;
               if ("sound".equals(audiotype)) Audio.stop(config,audiotype) ;
            }

            // Identify the action object.

            if (kiss == null)
            {
               o1 = variable.getValue((String) parameters.elementAt(0),event) ;
               if (o1 instanceof String)
               {
                  s1 = ((String) o1).toUpperCase() ;
                  kiss = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;
               }
            }
            if (!(kiss instanceof Audio)) break ;
            Audio a = (Audio) kiss ;

            // Suspend the media player if it is active and we are starting
            // a music file.

            MediaFrame mf = (config == null) ? null : config.getMediaFrame() ;
            if (mf != null && code == 18)
            {
               if (OptionsDialog.getSuspendMedia())
               {
                  mf.suspend() ;
                  if (OptionsDialog.getMediaMusicResume())
                     a.addCallbackListener(mf) ;
               }
            }

            // Set the repeat count.  If the repeat count is specified and 
            // is zero then this is a request to stop the named sound.
            // Otherwise the play request is performed in a separate thread 
            // (AudioSound) so as to not restrict event processing.

            n1 = 0 ;
            if (parameters.size() > 1)
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            a.setRepeat(n1) ;
            a.setType(audiotype) ;
            if (parameters.size() > 1 && n1 == 0)
            {
               Audio.stop(a) ;
               break ;
            }
            
            // If single sound and stopping and currently playing this sound 
            // then set the indicator to wait for the stop to complete.
            
            if (OptionsDialog.getSoundSingle() && !OptionsDialog.getLongSoundMedia())
               if (a.isStarted()) a.setStopping(true) ;
            a.play() ;
            break ;


         // Do nothing.

         case 19:		// "nop"
            break ;


         // Terminate the program execution.  We invoke quit commands on the
         // AWT thread so that the EventHandler shutdown is not dependent
         // on this thread.

         case 21:		// "quit"
            Runnable quitter = new Runnable()
            {
               public void run()
               {
                  MainFrame mfm = Kisekae.getMainFrame() ;
                  PanelFrame panel = (mfm != null) ? mfm.getPanel() : null ;
                  if (panel != null) panel.quit() ;
               }
            } ;
            javax.swing.SwingUtilities.invokeLater(quitter);
            break ;


         // Activate a timer with a random delay value.

         case 22:		// "randomtimer"
            if (parameters.size() < 3) break ;
            if (kiss == null)
            {
               alarmid = evaluateParam((String) parameters.elementAt(0)) ;
               if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
               kiss = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            }
            if (!(kiss instanceof Alarm)) break ;
            alarm = (Alarm) kiss ;
            int min = variable.getIntValue((String) parameters.elementAt(1),event) ;
            int variance = variable.getIntValue((String) parameters.elementAt(2),event) ;
            delay = min + Math.round((float) (Math.random() * variance)) ;
            alarm.setInterval(delay,activator) ;
            event.setAlarmEnable(kiss) ;
            setAlarmArguments(alarm,parameters,3) ;
            if (alarm != null) alarm.setSource(getName() + " in " + event.getName());
            break ;


         // Manually set the flex value for a group.  Fire an unfix event if
         // we unfix the group. Note that we update the flex values in place
         // as we can have concurrent mouse events examining these values.
         // If the fix value is set to a new, different value through this
         // action no concurrent mouse events will change the set value.

         case 23:		// "setfix"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            
            if (kiss instanceof Group)
            {
               Group group = (Group) kiss ;
               flex = group.getFlex() ;
               int fix = variable.getIntValue((String) parameters.elementAt(1),event) ;
               int oldflex = flex.y ;
               flex.x = fix + 1 ;
               flex.y = fix ;
               if (flex.x <= 0) flex.x = 1 ;
               if (flex.y < 0) flex.y = 0 ;

               // Fire unfix events on the state change. Note, it appears
               // that the group does not need to be visible for unfix events.

               boolean b = OptionsDialog.getVisibleUnfix() ;
               if (fix == 0 && oldflex > 0 && panel != null && panel.isVisible())
               {
                  if ((b && group.isVisible() ) || !b)
                  {
                     if (((evt = group.getEvent("unfix")) != null))
                     {
                        if (OptionsDialog.getImmediateUnfix())
                           EventHandler.fireEvents(evt,panel,currentthread,group) ;
                        else
                           EventHandler.queueEvents(evt,currentthread,group) ;
                     }
                  }

                  // Fire unfix events for any cels in this group.  Note, it
                  // appears that cels do not need to be visible for unfix events.

                  i = 0 ;
                  Cel c = null ;
                  group.setUnfix(false) ;
                  while ((c = group.getCel(i++)) != null)
                  {
                     if ((b && c.isVisible() ) || !b)
                     {
                        if ((evt = c.getEvent("unfix")) != null)
                        {
                           if (OptionsDialog.getImmediateUnfix())
                              EventHandler.fireEvents(evt,panel,currentthread,c) ;
                           else
                              EventHandler.queueEvents(evt,currentthread,c) ;
                        }
                     }
                  }
               }
               
               // If we have set the fix value to 1 of our current mouse 
               // selected object, then we need to set up for an unfix
               // event the next time it is selected.
               
               if (panel != null && panel.getGroup() == group)
               {
                  if (flex.y == 1) group.setUnfix(true) ;
                  panel.setFixChanged() ;
                  panel.releaseMouse() ;
               }
            }
            
            if (kiss instanceof Cel)
            {
               Cel c = (Cel) kiss ;
               Integer flexnum = c.getFlex() ;
               if (flexnum == null) flexnum = new Integer(0) ;
               int fix = variable.getIntValue((String) parameters.elementAt(1),event) ;
               int oldflex = flexnum.intValue() ;
               flexnum = new Integer(fix) ;
               c.setFlex(flexnum) ;
               
               // It is the group object that is sticky, not the cel.  It is
               // not clear whether the group should become sticky if we set
               // a fix value on a cel.
               
               Object cg = c.getGroup() ;
               if (cg instanceof Group) ((Group) cg).updateFlex() ;

               // Fire unfix events on the state change.

               boolean b = OptionsDialog.getVisibleUnfix() ;
               if (fix == 0 && oldflex > 0 && panel != null && panel.isVisible())
               {
                  if ((b && c.isVisible() ) || !b)
                  {
                     if (((evt = c.getEvent("unfix")) != null))
                        if (OptionsDialog.getImmediateUnfix())
                           EventHandler.fireEvents(evt,panel,currentthread,c) ;
                        else
                           EventHandler.queueEvents(evt,currentthread,c) ;
                  }
               }
            }
            break ;


         // Run an operating system command.  shell(command,exitcode)

         case 24:		// "shell"
            if (parameters.size() < 1) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            s1 = s1.toLowerCase() ;
            try
            {
               if (s1.startsWith("http:") || s1.startsWith("https:") || s1.startsWith("file:") || s1.startsWith("mailto:"))
               {
                  BrowserControl browser = new BrowserControl() ;
                  browser.displayURL(s1) ;
               }
               if (OptionsDialog.getEnableShell())
               {
                  Process p = Runtime.getRuntime().exec((String) parameters.elementAt(0)) ;
                  if (parameters.size() > 1) 
                  {
                     n1 = (p != null) ? p.waitFor() : -1 ;
                     variable.setIntValue((String) parameters.elementAt(1),n1,event) ;
                  }
               }
            }
            catch (IOException e)
            {
               s = "Exception, Event action: " + toString() ;
               System.out.println("Line [" + line + "] " + s) ;
               System.out.println(e) ;
            }
            break ;


         // Activate a timer with a specified delay value.

         case 26:		// "timer"
            if (parameters.size() < 2) break ;
            if (kiss == null)
            {
               alarmid = evaluateParam((String) parameters.elementAt(0)) ;
               if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
               kiss = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            }
            if (!(kiss instanceof Alarm)) break ;
            alarm = (Alarm) kiss ;
            delay = variable.getIntValue((String) parameters.elementAt(1),event) ;
/*            
            // Kludge for NoGodsLand timing problems
            s = config.getName() ;
            if (s.contains("NoGodsLand"))
            {
               o = alarm.getIdentifier() ;
               // problem in Official Meeting Hall with alarm(4057) running before alarm(128)
               if ("4057".equals(o) && "alarm(4042)".equals(event.getName())) 
                  delay = delay + 200 ;
               // problem in Hidden Hermitage with alarm(1001) running before alarm(257)
               if ("257".equals(o) && "alarm(2887)".equals(event.getName())) 
                  delay = delay + 200 ;
            }
*/            
            alarm.setInterval(delay,activator) ;
            event.setAlarmEnable(kiss) ;
            setAlarmArguments(alarm,parameters,2) ;
            alarm.setSource(getName() + " in " + event.getName());
            break ;


         // Set the transparency value for a cel or group.  The
         // transparency value is a relative change to the current
         // object transparency.  Bound changes are restricted so that
         // the object transparency is between 0 and 255.  Unbound changes
         // can set object transparency outside the limits.

         case 27:		// "transparent"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            int t = variable.getIntValue((String) parameters.elementAt(1),event) ;
            boolean bound = true ;
            if (parameters.size() > 2)
               bound = (variable.getIntValue((String) parameters.elementAt(2),event) != 0) ;
            kiss.changeTransparency(t,bound,(kiss instanceof Cel)) ;
            box = kiss.getAllBoundingBox() ;
            break ;


         // Hide a cel or group.

         case 28:		// "unmap"
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            o = (kiss instanceof Cel) ? ((Cel) kiss).getGroup() : kiss ;
            if (panel != null && OptionsDialog.getMapCollide())
               panel.setCollisionState(o,eventstate,true,true) ;
            kiss.setVisible(false,OptionsDialog.getAllAmbiguous(),kiss) ;
            box = kiss.getAllBoundingBox() ;
            if (panel != null && OptionsDialog.getMapCollide())
               if (OptionsDialog.getImmediateCollide())
                  box = panel.fireCollisionEvents(o,eventstate,thread,box) ;
            break ;


         // Change the current viewport position by a relative amount.

         case 29:		// "viewport"
            if (panel == null) break ;
            if (parameters.size() < 2) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
            n2 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            panel.changeviewport(n1,n2) ;
            box = new Rectangle(panel.getPanelSize()) ;
            break ;


         // Change the current window size by a relative amount.

         case 30:		// "windowsize"
            if (panel == null) break ;
            if (parameters.size() < 2) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
            n2 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            panel.changewindow(n1,n2) ;
            box = new Rectangle(panel.getPanelSize()) ;
            break ;
            
         // Repeat a gosub 'n' times setting variable 'v' to the repeat count.
         // Note that this code falls through to process the gosub.
            
         case 149: // "repeat(label,n,v)"
            if (parameters.size() < 2) break ;
            String s0 = evaluateParam((String) parameters.elementAt(0)) ;
            repeatlimit = variable.getIntValue((String) parameters.elementAt(1),event) ;
            if (parameters.size() < 3) repeatvbl = "@" + s0 ;
            else repeatvbl = (String) parameters.elementAt(2) ;
            repeatcount = variable.getIntValue(repeatvbl,event) ;

         // Random choice between two label events.  Note, this
         // code falls through to call the label event.

         case 33:		// "gotorandom"
         case 34:		// "gosubrandom"
            if (code != 149)
            {
               if (parameters.size() < 3) break ;
               n1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
               o2 = evaluateParam((String) parameters.elementAt(1)) ;
               if (o2 instanceof String) o2 = ((String) o2).toUpperCase() ;
               o3 = evaluateParam((String) parameters.elementAt(2)) ;
               if (o3 instanceof String) o3 = ((String) o3).toUpperCase() ;
               float percent = (float) (n1 / 100.) ;
               if (Math.random() < percent)
                  kiss = (Module) Module.getByKey(Module.getKeyTable(),cid,o2) ;
               else
                  kiss = (Module) Module.getByKey(Module.getKeyTable(),cid,o3) ;
            }

         // Call a label event.  For literal label numbers the label module will
         // have already been established.  For variables, we must identify
         // the correct label module.

         case 31:		// "goto"
         case 32:		// "gosub"
            if (parameters.size() < 1) break ;
            if (kiss == null)
            {
               String labelid = evaluateParam((String) parameters.elementAt(0)) ;
               if (labelid instanceof String) labelid = ((String) labelid).toUpperCase() ;
               kiss = (Module) Module.getByKey(Module.getKeyTable(),cid,labelid) ;
            }
            if (!(kiss instanceof Module)) break ;

            // Label events should be unique so we access the first event
            // to identify the required label event.

            Vector labelevt = kiss.getEvent("label") ;
            o = (labelevt != null && labelevt.size() > 0) ? labelevt.elementAt(0) : null ;
            if (o instanceof FKissEvent)
            {
               FKissFrame fk = event.getBreakFrame() ;
               FKissEvent invoker = (fk != null) ? fk.getInvoker() : null ;
               if (invoker != null && invoker.isBreakEnter())
                 ((FKissEvent) o).setNoBreakpoint(false);

               // Establish any label arguments.  These are provided on the gosub or goto
               // or goto statement and referenced as local parameters by the label event 
               // code.  For gosubrandom or gotorandom statements parameters should be  
               // consistent across each potential label module

               n1 = n2 = 1 ;
               if (code == 33 || code == 34) n1 = 3 ;
               FKissEvent labelevent = (FKissEvent) o ;
               Vector arguments = labelevent.getParameters() ;
               for (i = n1 ; i < parameters.size() ; i++)
               {
                  o1 = variable.getValue((String) parameters.elementAt(i),event) ;
                  if (n2 < arguments.size())
                  {
                     s = (String) arguments.elementAt(n2++) ;
                     if (!s.startsWith("@")) s = "@" + s ;
                     if (!(code == 31 || code == 33))
                        labelevent.setDepth(labelevent.getDepth()+1) ;
                     variable.setValue(s,o1,labelevent) ;
                     if (!(code == 31 || code == 33))
                        labelevent.setDepth(labelevent.getDepth()-1) ;
                  }
               }
               
               // The goto action is processed sequentially.  The event
               // firing routine will switch focus to this new label event.
               // We must transfer any pending alarm activations to this
               // label event so they do not get lost.

               if (code == 31 || code == 33)
               {
                  Vector alarmlist = event.getAlarmList() ;
                  ((FKissEvent) o).setAlarmEnable(alarmlist) ;
                  exception = "goto" ;
                  exceptionitem = o ;
                  break ;
               }

               // The gosub action causes the label event to be processed
               // recursively.  The display is not updated until the main event
               // is complete.  We propagate breakpoint states into the label
               // event if we are performing a breakpoint subroutine entry.

               Object [] breakstate = null ;
               fk = event.getBreakFrame() ;
               invoker = (fk != null) ? fk.getInvoker() : null ;
               int iflevel = event.getIfLevel() ;
               int elseiflevel = event.getElseIfLevel() ;
               Object skipactions = event.getSkipActions(currentthread) ;
               if (invoker != null && invoker.isBreakEnter())
               {
                  breakstate = invoker.getBreakState() ;
                  invoker.setBreakState(null) ;
                  labelevent.setBreakState(breakstate) ;
               }
               
               // Indicate if this event is the target of a repeat() action.
               // If it is then exitloop() actions are valid.
               
               if (code == 149) 
               {
                  labelevent.setRepeating(true) ;
            
                  // Initialize or increment.  Set the iteration variable value.

                  if (initializerepeat) 
                  { 
                     repeatcount = 1 ; 
                     labelevent.setRepeatLimit(repeatlimit) ; 
                     initializerepeat = false ; 
                  }
                  else  
                  {
                     repeatlimit = labelevent.getRepeatLimit() ; 
                     repeatcount += 1 ;
                  }
                  if (!directkiss)
                     variable.setIntValue(repeatvbl,repeatcount,event) ;
                  if (repeatcount > repeatlimit) 
                  { 
                     initializerepeat = true ; 
                     break ; 
                  }
                  if (directkiss)
                     variable.setIntValue(repeatvbl,repeatcount,event) ;
                  exception = "repeat" ;
               }

               // Process the gosub event.  Restore our break and skip state
               // on return.

               labelevent.fireEvent(panel,thread,null) ;
               labelevent.setRepeating(false) ;
               event.setIfLevel(iflevel) ;
               event.setElseIfLevel(elseiflevel) ;
               event.setSkipActions(currentthread,skipactions) ;
               if (breakstate != null)
               {
                  breakstate = labelevent.getBreakState() ;
                  labelevent.setBreakState(null) ;
                  if (invoker != null) invoker.setBreakState(breakstate) ;
               }

               // Establish the label return value as a local variable
               // for this event.

               o = labelevent.getReturnValue() ;
               arguments = labelevent.getParameters() ;
               if (o != null && arguments != null && arguments.size() > 0)
               {
                  s = '@' + (String) arguments.elementAt(0) ;
                  variable.setValue(s,o,event) ;
               }

               // Ensure that any new event collision states are propagated
               // to this event.

               Hashtable labelcollide = labelevent.getCollisionState() ;
               Enumeration enum1 = labelcollide.keys() ;
               while (enum1 != null && enum1.hasMoreElements())
               {
                  Object key = enum1.nextElement() ;
                  Object value = labelcollide.get(key) ;
                  Object collisionstate = eventstate.get(key) ;
                  if (collisionstate == null)
                     eventstate.put(key,value) ;
                  else if (collisionstate instanceof Object [])
                  {
                     Boolean mapped = (Boolean) ((Object []) collisionstate)[4] ;
                     Boolean mapstate = (Boolean) ((Object []) value)[4] ;
                     if (!mapstate.booleanValue() & mapped.booleanValue())
                        ((Object []) collisionstate)[4] = new Boolean(false) ;
                  }
               }
               
               // Cancel any repeat exception if we terminated with an exitloop.
               
               if ("exitloop".equals(labelevent.getEventException()))
               {
                  exception = null ;
                  initializerepeat = true ;
               }

               // Return the label event bounding box and ensure that any
               // label alarm enable requests are attached to this event.

               box = labelevent.getBoundingBox() ;
               actionsprocessed = labelevent.getActionsProcessed() ;
               Vector alarmlist = labelevent.getAlarmList() ;
               event.setAlarmEnable(alarmlist) ;

               if (OptionsDialog.getDebugAction() && (!getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
               {
                   String bp = (nobreakpoint) ? "*" : " " ;
                   System.out.println(" >"+bp+"gosub(" + kiss.getIdentifier() + ") returns") ;
               }
            }
            break ;

         // Exitevent command.

         case 35:		// "exitevent"
            exception = "exitevent" ;
            break ;

         // Variable assignment command.

         case 36:		// "let"
            if (parameters.size() < 1) break ;
            o = (parameters.size() < 2) ? null : variable.getValue((String) parameters.elementAt(1),event) ;
            variable.setValue((String) parameters.elementAt(0),o,event) ;
            break;

         // Addition command.

         case 37:		// "add"
            if (parameters.size() < 3) break ;
            n1 = variable.getConvertType((String) parameters.elementAt(1),event) ;
            n2 = variable.getConvertType((String) parameters.elementAt(2),event) ;
            if (n1 == 3 || n2 == 3)
            {
               d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
               d2 = variable.getDoubleValue((String) parameters.elementAt(2),event) ;
               variable.setDoubleValue((String) parameters.elementAt(0),d1+d2,event) ;
               break;
            }
            l1 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(2),event) ;
            variable.setLongValue((String) parameters.elementAt(0),l1+l2,event) ;
            break;

         // Subtraction command.

         case 38:		// "sub"
            if (parameters.size() < 3) break ;
            n1 = variable.getConvertType((String) parameters.elementAt(1),event) ;
            n2 = variable.getConvertType((String) parameters.elementAt(2),event) ;
            if (n1 == 3 || n2 == 3)
            {
               d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
               d2 = variable.getDoubleValue((String) parameters.elementAt(2),event) ;
               variable.setDoubleValue((String) parameters.elementAt(0),d1-d2,event) ;
               break;
            }
            l1 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(2),event) ;
            variable.setLongValue((String) parameters.elementAt(0),l1-l2,event) ;
            break;

         // Multiplication command.

         case 39:		// "mul"
            if (parameters.size() < 3) break ;
            n1 = variable.getConvertType((String) parameters.elementAt(1),event) ;
            n2 = variable.getConvertType((String) parameters.elementAt(2),event) ;
            if (n1 == 3 || n2 == 3)
            {
               d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
               d2 = variable.getDoubleValue((String) parameters.elementAt(2),event) ;
               variable.setDoubleValue((String) parameters.elementAt(0),d1*d2,event) ;
               break;
            }
            l1 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(2),event) ;
            variable.setLongValue((String) parameters.elementAt(0),l1*l2,event) ;
            break;

         // Integer division command.

         case 40:		// "div"
            if (parameters.size() < 3) break ;
            n1 = variable.getConvertType((String) parameters.elementAt(1),event) ;
            n2 = variable.getConvertType((String) parameters.elementAt(2),event) ;
            if (n1 == 3 || n2 == 3)
            {
               d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
               d2 = variable.getDoubleValue((String) parameters.elementAt(2),event) ;
               variable.setDoubleValue((String) parameters.elementAt(0),d1/d2,event) ;
               break;
            }
            l1 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(2),event) ;
            variable.setLongValue((String) parameters.elementAt(0),l1/l2,event) ;
            break;

         // Division remainder command.

         case 41:		// "mod"
            if (parameters.size() < 3) break ;
            l1 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(2),event) ;
            variable.setLongValue((String) parameters.elementAt(0),l1%l2,event) ;
            break;

         // Random number from A to B command.

         case 42:		// "random"
            if (parameters.size() < 3) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            n2 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            n3 = Math.round((float) (Math.random() * (n2-n1))) + n1 ;
            variable.setIntValue((String) parameters.elementAt(0),n3,event) ;
            break;

         // Determine the x-coordinate of an object.  

         case 43:		// "letobjectx"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            n1 = kiss.getLocation().x ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            if (panel == null || !(kiss instanceof Group)) break ;
            
            // If the object happens to be the one dragged by the mouse 
            // return the bounding box coordinate corrected for the offset.
           
            o1 = panel.getDragObject() ;
            if (!(o1 == kiss)) break ;
            Rectangle box1 = kiss.getBoundingBox() ;
            Point offset = kiss.getOffset() ;
            n1 = box1.x - offset.x ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the y-coordinate of an object.

         case 44:		// "letobjecty"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            n1 = kiss.getLocation().y ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            if (panel == null || !(kiss instanceof Group)) break ;
            
            // If the object happens to be the one dragged by the mouse 
            // return the bounding box coordinate corrected for the offset.
            
            o1 = panel.getDragObject() ;
            if (!(o1 == kiss)) break ;
            box1 = kiss.getBoundingBox() ;
            offset = kiss.getOffset() ;
            n1 = box1.y - offset.y ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the flex value of a group or cel.

         case 45:		// "letfix"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            n1 = 0 ;
            if (kiss instanceof Group) 
               n1 = ((Group) kiss).getFlex().y ;
            if (kiss instanceof Cel) 
            { 
               Integer celflex = ((Cel) kiss).getFlex() ;
               if (celflex == null) celflex = new Integer(0) ;
               n1 = celflex.intValue() ;
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the visibility of a cel or cel group.  The cel group
         // visibility returns the number of visible cels on the current page.

         case 46:		// "letmapped"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            if (panel == null)
               n1 = kiss.isVisible() ? 1 : 0 ;
            else
            {
               pageset = panel.getPage() ;
               pid = (pageset != null) ? (Integer) pageset.getIdentifier() : null ;
               n1 = kiss.getVisibleCelCount(pid) ;
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the current page set.

         case 47:		// "letset"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            PageSet p = panel.getPage() ;
            if (p == null) break ;
            n1 = ((Integer) p.getIdentifier()).intValue() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the current multipalette in use.
         // Syntax: letpal(Variable,[celname])

         case 48:		// "letpal"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            if (parameters.size() > 1)
            {
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
               if (!(kiss instanceof Cel)) break ;
               n1 = ((Cel) kiss).getMultiPalette() ;
            }
            else
            {
               Integer multipalette = panel.getMultiPalette() ;
               if (multipalette == null) break ;
               n1 = multipalette.intValue() ;
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the current mouse x-coordinate.

         case 49:		// "letmousex"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            n1 = panel.getMouseX() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the current mouse y-coordinate.

         case 50:		// "letmousey"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            n1 = panel.getMouseY() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the currently selected or last group object.  If the
         // optional second argument is specified then we check and return
         // the collision group object it is of the required type.

         case 51:		// "letcatch"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            if (parameters.size() < 2)
            {
               Group group = panel.getGroup() ;
               if (group == null) group = panel.getLastGroup() ;
               if (group == null) break ;
               n1 = ((Integer) group.getIdentifier()).intValue() ;
               variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
               break;
            }

            // Check for a collision object of the required type.  This version
            // of letcatch() returns a value only when collision objects are
            // set in the event.

            o = null ;
            variable.setIntValue((String) parameters.elementAt(0),-1,event) ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            Object [] collide = event.getCollide() ;
            if (collide == null) break ;
            Cel c1 = (Cel) collide[0] ;
            Cel c2 = (Cel) collide[1] ;
            if (kiss.containsCel(c1)) o = c1.getGroup() ;
            else if (kiss.containsCel(c2)) o = c2.getGroup() ;
            if (o instanceof Group)
            {
               Group group = (Group) o ;
               n1 = ((Integer) group.getIdentifier()).intValue() ;
               variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            }
            break ;

         // Determine if two objects touch.

         case 52:		// "letcollide"
            if (parameters.size() < 3) break ;
            if (panel == null) break ;
            s1 = (String) parameters.elementAt(1) ;
            s2 = (String) parameters.elementAt(2) ;
            n1 = (panel.checkTouch(s1,s2,null,null) == 1) ? 1 : 0 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine if two objects are inside one another.

         case 53:		// "letinside"
            if (parameters.size() < 3) break ;
            if (panel == null) break ;
            s1 = (String) parameters.elementAt(1) ;
            s2 = (String) parameters.elementAt(2) ;
            n1 = (panel.checkOverlap(s1,s2) == 1) ? 1 : 0 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine a cel's transparency.

         case 54:		// "lettransparent"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            n1 = kiss.getTransparency() ;
            if (n1 >= 0) n1 = 255 - n1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // If equal.

         case 55:		// "ifequal"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null && o2 instanceof Integer) o1 = new Integer(0) ;
            if (o2 == null && o1 instanceof Integer) o2 = new Integer(0) ;
            if (o1 == null && o2 instanceof Long) o1 = new Long(0) ;
            if (o2 == null && o1 instanceof Long) o2 = new Long(0) ;
            if (o1 == null && o2 instanceof Double) o1 = new Double(0) ;
            if (o2 == null && o1 instanceof Double) o2 = new Double(0) ;
            if (o1 == null && o2 instanceof String) o1 = "" ;
            if (o2 == null && o1 instanceof String) o2 = "" ;
            o1 = (o1 != null) ? o1.toString() : "" ;
            o2 = (o2 != null) ? o2.toString() : "" ;
            setSkipActions((!o1.equals(o2)),identifier,currentthread) ;
            event.setIfLevel(event.getIfLevel() + 1);
            event.setElseIfLevel(0) ;
            break;

         // If not equal.

         case 56:		// "ifnotequal"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null && o2 instanceof Integer) o1 = new Integer(0) ;
            if (o2 == null && o1 instanceof Integer) o2 = new Integer(0) ;
            if (o1 == null && o2 instanceof Long) o1 = new Long(0) ;
            if (o2 == null && o1 instanceof Long) o2 = new Long(0) ;
            if (o1 == null && o2 instanceof Double) o1 = new Double(0) ;
            if (o2 == null && o1 instanceof Double) o2 = new Double(0) ;
            if (o1 == null && o2 instanceof String) o1 = "" ;
            if (o2 == null && o1 instanceof String) o2 = "" ;
            o1 = (o1 != null) ? o1.toString() : "" ;
            o2 = (o2 != null) ? o2.toString() : "" ;
            setSkipActions((o1.equals(o2)),identifier,currentthread) ;
            event.setIfLevel(event.getIfLevel() + 1);
            event.setElseIfLevel(0) ;
            break;

         // If greater than.

         case 57:		// "ifgreaterthan"
            if (parameters.size() < 2) break ;
            l1 = variable.getLongValue((String) parameters.elementAt(0),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            setSkipActions((l1 <= l2),identifier,currentthread) ;
            event.setIfLevel(event.getIfLevel() + 1);
            event.setElseIfLevel(0) ;
            break;

         // If less than.

         case 58:		// "iflessthan"
            if (parameters.size() < 2) break ;
            l1 = variable.getLongValue((String) parameters.elementAt(0),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            setSkipActions((l1 >= l2),identifier,currentthread) ;
            event.setIfLevel(event.getIfLevel() + 1);
            event.setElseIfLevel(0) ;
            break;

         // Else part.

         case 59:		// "else"
            n1 = event.getIfLevel() - 1 ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            boolean b = !event.getSkip(currentthread) ;
            if (event.getElseIfLevel() > 0) b = true ;
            setSkipActions(b,event.getSkipContext(currentthread),n1,currentthread) ;
            break;

         // Endif sentinal.

         case 60:		// "endif"
            n1 = event.getIfLevel() - 1 ;
            event.setIfLevel(n1) ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            event.setElseIfLevel(0) ;
            setSkipActions(false,event.getSkipContext(currentthread),currentthread) ;
            break;

         // Set the cel ghost flag.

         case 61:		// "ghost"
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            n1 = 1 ;
            if (parameters.size() > 1)
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            if (OptionsDialog.getInvertGhost()) 
               kiss.setGhost(n1 == 0,OptionsDialog.getAllAmbiguous(),kiss) ;
            else
               kiss.setGhost(n1 != 0,OptionsDialog.getAllAmbiguous(),kiss) ;
            box = kiss.getAllBoundingBox() ;
            break ;

         // Determine the defined width of an object.

         case 62:		// "letobjectw"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            n1 = kiss.getBoundingBox().width ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the visible width of an object.

         case 77:		// "letwidth"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            p = (panel != null) ? panel.getPage() : null ;
            o = (p != null) ? p.getIdentifier() : null ;
            pid = (o instanceof Integer) ? (Integer) o : null ;
            Dimension d = ((Group) kiss).getVisibleSize(pid) ;
            variable.setIntValue((String) parameters.elementAt(0),d.width,event) ;
            break;

         // Determine the defined height of an object.

         case 63:		// "letobjecth"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            n1 = kiss.getBoundingBox().height ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the visible height of an object.

         case 78:		// "letheight"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            p = (panel != null) ? panel.getPage() : null ;
            o = (p != null) ? p.getIdentifier() : null ;
            pid = (o instanceof Integer) ? (Integer) o : null ;
            d = ((Group) kiss).getVisibleSize(pid) ;
            variable.setIntValue((String) parameters.elementAt(0),d.height,event) ;
            break;

         // Determine the initial x ordinate of an object on the current page.

         case 75:		// "letinitx"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            p = (panel != null) ? panel.getPage() : null ;
            n1 = kiss.getInitialLocation(p).x ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Determine the initial y ordinate of an object on the current page.

         case 76:		// "letinity"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Cel || kiss instanceof Group)) break ;
            p = (panel != null) ? panel.getPage() : null ;
            n1 = kiss.getInitialLocation(p).y ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // While (operand[[,operand2,condition]). Execute code only if not false.
         // If the optional second operand and condition is specified then the
         // two operands are compared according to the specified condition.
         // Note that our skip context must include the iteration variable.

         case 64:		// "while(operand[,operand2,condition)"
            if (parameters.size() < 1) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            String context = identifier + " " + (String) parameters.elementAt(0) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            if (s1.length() == 0) s1 = "0" ;
            if (parameters.size() == 1)
            {
               s1 = (o1 != null) ? o1.toString() : "" ;
               if (s1.length() == 0) s1 = "0" ;
               setSkipActions(("0".equals(s1) || "true".equalsIgnoreCase(s1)),context,currentthread) ;
               break ;
            }
            
            // Perform comparison
            
            n1 = 0 ;
            s3 = "" ;
            b = true ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            s2 = (o2 != null) ? o2.toString() : "" ;
            if (parameters.size() > 2)
            {
               o3 = variable.getValue((String) parameters.elementAt(2),event) ;
               s3 = (o3 != null) ? o3.toString() : "" ;
            }
               
            // Watch for type conversions.

            if (o1 instanceof String || o2 instanceof String) 
               n1 = s1.compareTo(s2) ;
            else if (o1 instanceof Double || o2 instanceof Double)
            {
               d1 = variable.getDoubleValue((String) parameters.elementAt(0),event) ;
               d2 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
               n1 = (d1 < d2) ? -1 : (d1 == d2) ? 0 : 1 ;
            }
            else if (o1 instanceof Long || o2 instanceof Long)
            {
               l1 = variable.getLongValue((String) parameters.elementAt(0),event) ;
               l2 = variable.getLongValue((String) parameters.elementAt(1),event) ;
               n1 = (l1 < l2) ? -1 : (l1 == l2) ? 0 : 1 ;
            }
            else if (o1 instanceof Integer || o2 instanceof Integer)
            {
               l1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
               l2 = variable.getIntValue((String) parameters.elementAt(1),event) ;
               n1 = (l1 < l2) ? -1 : (l1 == l2) ? 0 : 1 ;
            }
               
            // Comparisons.  
            
            if ("".equals(s3) && n1 == 0) b = false ;
            else if ("=".equals(s3) && n1 == 0) b = false ;
            else if (">".equals(s3) && n1 > 0) b = false ;
            else if ("<".equals(s3) && n1 < 0) b = false ;
            else if (">=".equals(s3) && n1 >= 0) b = false ;
            else if ("<=".equals(s3) && n1 <= 0) b = false ;
            else if ("<>".equals(s3) && n1 != 0) b = false ;
            else if (s3 == "!=" && n1 != 0) b = false ;

            setSkipActions(b,context,currentthread) ;
            break;

         // EndWhile (Variable).  Repeat the loop until we skip.

         case 65:		// "endwhile(Variable)"
            if (!event.getSkip(currentthread))
            {
               exception = "loop" ;
               exceptionitem = new Integer(loopentry) ;
               break ;
            }

            // Skipping.  Check this endwhile statement for the correct 
            // variable context. If we match, stop skipping.

            s = (String) parameters.elementAt(0) ;
            context = event.getSkipContext(currentthread) ;
            if (context == null || s == null) break ;
            int n = context.indexOf(' ') ;
            context = context.substring(n+1) ;
            if (OptionsDialog.getVariableCase() && s.equals(context))
               setSkipActions(false,event.getSkipContext(currentthread),currentthread) ;
            else if (!OptionsDialog.getVariableCase() && s.equalsIgnoreCase(context))
               setSkipActions(false,event.getSkipContext(currentthread),currentthread) ;
            break;

         // Determine the currently selected or last cel object or the name of
         // a specified frame in a cel group or object group.

         case 67:		// "letcel"
            Cel cel = null ;
            if (parameters.size() < 1) break ;
            
            // Syntax: letcel(Variable)
            // This version returns the cel name of the last pressed cel.
            
            if (parameters.size() == 1) 
            {
               if (panel == null) break ;
               cel = panel.getCel() ;
               if (cel == null) cel = panel.getLastCel() ;
            }
            
            // Syntax: letcel(Variable,[group,[frame]])
            // This version returns the cel name of the specified group frame.
            // If a frame number is not provided the name of the first mapped
            // cel is returned.
            
            if (parameters.size() > 1) 
            {
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
               if (kiss == null) break ;
               n1 = kiss.getFrame() ;
               if (parameters.size() > 2)
                  n1 = variable.getIntValue((String) parameters.elementAt(2),event) ;
               cel = kiss.getCel(n1) ;
            }
            
            // Return the cel relative name.
            
            if (cel == null) break ;
            s1 = cel.getRelativeName() ;
            if (s1 != null) variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Determine a cel or object comment text.

         case 68:		// "letcomment"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            s1 = kiss.getBaseComment() ;
            variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Concatenate multiple strings.

         case 69:		// "concat(Result,S1,S2,...)"
            if (parameters.size() < 3) break ;
            StringBuffer sb = new StringBuffer("") ;
            for (i = 1 ; i < parameters.size() ; i++)
            {
               o = variable.getValue((String) parameters.elementAt(i),event) ;
               if (o != null) sb.append(o) ;
            }
            variable.setValue((String) parameters.elementAt(0),sb.toString(),event) ;
            break;

         // Substring a string.  If the End parameter is not specified
         // or invalid the remainder of the string is returned.

         case 70:		// "substr(S,SourceString,Start,End)"
            if (parameters.size() < 3) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            n2 = -1 ;
            if (parameters.size() > 3)
               n2 = variable.getIntValue((String) parameters.elementAt(3),event) ;
            s1 = o1.toString() ;
            if (n1 < 0 || n1 > s1.length()) n1 = s1.length() ;
            if (n2 < n1 || n2 > s1.length()) n2 = s1.length() ;
            variable.setValue((String) parameters.elementAt(0),s1.substring(n1,n2),event) ;
            break;

         // Animate a cel.  If no cel name is specified, set the timer
         // enable state to control all animation.

         case 71:		// "animate"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            if ("".equals(o1)) { GifTimer.setEnabled(n1 != 0) ;  break ; }
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            kiss.setAnimate(n1,OptionsDialog.getAllAmbiguous(),kiss) ;
            break;

         // For (variable, start, end, increment)

         case 72:		// "for(variable, start, end, increment)"
            if (parameters.size() < 3) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
            n2 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            n3 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            n4 = (parameters.size() < 4) ? ((n3 >= n2) ? 1 : -1)
               : variable.getIntValue((String) parameters.elementAt(3),event) ;

            // Initialize or increment.

            if (initializefor) { n1 = n2 ; initializefor = false ; }
            else  n1 += n4 ;

            // Our skip context must include the iteration variable.

            if ((n1 > n3 && n4 > 0) || (n1 < n3 && n4 < 0))
            {
               context = identifier + " " + (String) parameters.elementAt(0) ;
               setSkipActions(true,context,currentthread) ;
               initializefor = true ;
            }

            // Set the iteration variable value.

            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Next (variable)  Repeat the loop until we skip.

         case 73:		// "next(variable)"
            if (parameters.size() < 1) break ;
            if (!event.getSkip(currentthread))
            {
               exception = "loop" ;
               exceptionitem = new Integer(loopentry) ;
               break ;
            }

            // Skipping.  Check this next statement for the correct for
            // variable context. If we match, stop skipping.

            s = (String) parameters.elementAt(0) ;
            context = event.getSkipContext(currentthread) ;
            if (context == null || s == null) break ;
            n = context.indexOf(' ') ;
            context = context.substring(n+1) ;
            if (OptionsDialog.getVariableCase() && s.equals(context))
               setSkipActions(false,event.getSkipContext(currentthread),currentthread) ;
            else if (!OptionsDialog.getVariableCase() && s.equalsIgnoreCase(context))
               setSkipActions(false,event.getSkipContext(currentthread),currentthread) ;
            break;

         // Play a movie file.   Format:  movie(name,repeat)

         case 74:		// "movie"
            if (!Kisekae.isMediaInstalled())
            {
               s = toString() + " requires Java Media Framework" ;
               if (panel != null) panel.showStatus(s) ;
               break ;
            }
            if (parameters.size() < 1) break ;

            // Are we stopping all movies?

            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if ("".equals(o1)) { Video.stop(config) ;  break ; }

            // Identify the action object.

            kiss = Cel.findCel((String) parameters.elementAt(0),config,event) ;
            do
            {
               if (!(kiss instanceof Video)) break ;
               Video v = (Video) kiss ;

               // Set the repeat count.

               n1 = 0 ;
               if (parameters.size() > 1)
                 n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
               v.setRepeat(n1) ;
               v.play() ;
               kiss = Cel.findNextCel(v,v.getName(),config) ;
            }
            while (kiss != null && OptionsDialog.getAllAmbiguous()) ;
            break ;

         // Determine the cel group frame setting.

         case 79:		// "letframe"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            if (parameters.size() == 2 || panel == null)
               n1 = kiss.getFrame() ;
            else
               n1 = kiss.getFrame(panel.getCel()) ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the cel group frame. 
            
         case 80:		// "setframe(object,n)"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            kiss.setFrame(n1) ;
            box = kiss.getBoundingBox() ;
            break;

         // Attach an object to another object.

         case 81:		// "attach"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Group)) break ;
            parent = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(parent instanceof Group)) break ;
            parent.attach(kiss) ;
            kiss.setGlue(false) ;
            break;

         // Detach an object from another object.

         case 82:		// "detach"
            if (parameters.size() < 1) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Group)) break ;
            kiss.detach() ;
            break;

         // Glue an object to another object so that it is not detached on move.

         case 83:		// "glue"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Group)) break ;
            parent = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(parent instanceof Group)) break ;
            parent.attach(kiss) ;
            kiss.setGlue(true) ;
            break;

         // Returns the lowest numbered object attached in the children.

         case 84:		// "letchild"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            KissObject child = kiss.getFirstChild() ;
            Object attachid = (child == null) ? null : child.getIdentifier() ;
            n1 = (attachid instanceof Integer) ? ((Integer) attachid).intValue() : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Returns the number of the parent object.

         case 85:		// "letparent"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            parent = kiss.getParent() ;
            attachid = (parent == null) ? null : parent.getIdentifier() ;
            n1 = (attachid instanceof Integer) ? ((Integer) attachid).intValue() : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Returns the number of the next child object.

         case 86:		// "letsibling"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            child = kiss.getNextChild() ;
            attachid = (child == null) ? null : child.getIdentifier() ;
            n1 = (attachid instanceof Integer) ? ((Integer) attachid).intValue() : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Search a list for the last key typed and return its index position.
         // Note that this command will only recognize a key while the key is
         // pressed.

         case 87:		// "letkey"
            if (parameters.size() < 2) break ;
            if (panel == null) break ;
            s1 = panel.getActiveKeyChar() ;
            if (!OptionsDialog.getRetainKey()) s1 = panel.getKeyChar() ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s2 = (o1 == null) ? "" : o1.toString() ;
            s1 = translateKey(s1) ;
            s2 = translateKey(s2) ;
            n1 = s2.indexOf(s1) + 1 ;
            if (s1.length() == 0) n1 = 0 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // Search a list for the last key combination typed and return its
         // index position.  Note that this command will only recognize a key
         // while the key is pressed.

         case 88:		// "letkeymap"
            if (parameters.size() < 2) break ;
            if (panel == null) break ;
            s1 = panel.getActiveKeyCombination() ;
            if (!OptionsDialog.getRetainKey()) s1 = panel.getKeyCombination() ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s2 = (o1 == null) ? "" : o1.toString() ;
            s1 = translateKey(s1) ;
            s2 = translateKey(s2) ;
            n1 = 0 ;
            for (i = 0 ; i < s1.length() ; i++)
            {
               int j = s2.indexOf(s1.charAt(i)) ;
               if (j >= 0) n1 += (1 << j) ;
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // Returns the last typed character.

         case 89:		// "letkeychar"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            variable.setValue((String) parameters.elementAt(0),panel.getKeyChar(),event) ;
            break ;

         // Returns the last typed virtual character code.

         case 90:		// "letkeycode"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            variable.setIntValue((String) parameters.elementAt(0),panel.getKeyCode(),event) ;
            break ;

         // Returns the last typed modifier string.

         case 91:		// "letkeymodifier"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            variable.setValue((String) parameters.elementAt(0),panel.getKeyModifier(),event) ;
            break ;

         // Returns the last typed string.

         case 92:		// "letkeystring"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            variable.setValue((String) parameters.elementAt(0),panel.getKeyString(),event) ;
            break ;


         // Start the media player.   Format: mediaplayer(playlist,repeat,minimize)

         case 93:		// "mediaplayer"
            if (!Kisekae.isMediaInstalled())
            {
               s = toString() + " requires Java Media Framework" ;
               if (panel != null) panel.showStatus(s) ;
               break ;
            }
            if (parameters.size() < 1) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if (!(o1 instanceof String)) break ;
            s1 = ((String) o1).toUpperCase() ;
            mf = (config == null) ? null : config.getMediaFrame() ;
            audiotype = "media" ;

            // Stop the media player if requested.  This will dispose of
            // the configuration media player if it was created during this
            // session.  If not, then this command will terminate all audio.

            if ("".equals(s1))
            {
               if (mf != null)
               {
                  MediaFrame mf1 = mf ;
                  Runnable runner = new Runnable()
                  { public void run() { mf1.stop() ; } } ;
                  Thread runthread = new Thread(runner) ;
                  runthread.start() ;
                  if (config != null) config.setMediaFrame(null) ;
               }
               else
                  Audio.stop(config,audiotype) ;
               break ;
            }

            // Identify the action object.

            if (kiss == null)
               kiss = (Audio) Audio.getByKey(Audio.getKeyTable(),cid,s1) ;

            // Determine the playlist zip entry if no action object.

            ArchiveEntry ze = null ;
            if (kiss == null)
            {
               ArchiveFile zip = (config == null) ? null : config.getZipFile() ;
               if (zip == null) break ;
               if (zip instanceof DirFile)
               {
                  String directory = zip.getDirectoryName() ;
                  if (directory != null)
                  {
                     File file = new File(directory,s1) ;
                     s1 = file.getPath() ;
                  }
               }
               ze = zip.getEntry(s1) ;
               if (ze == null) break ;
               if (!(ze.isAudio() || ze.isVideo() || ze.isList())) break ;
            }

            // Get the repeat and minimize options.

            n1 = 0 ;     // repeat
            n2 = 1 ;     // minimized
            n3 = 0 ;     // x position
            n4 = 0 ;     // y position
            if (parameters.size() > 1)
               n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            if (parameters.size() > 2)
               n2 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            if (parameters.size() > 3)
               n3 = variable.getIntValue((String) parameters.elementAt(3),event) ;
            if (parameters.size() > 4)
               n4 = variable.getIntValue((String) parameters.elementAt(4),event) ;
            
            // If we are a background sound transitioned to the MediaPlayer
            // and the MediaPlayer option id set to repeat sounds, then set 
            // the repeat value.
            
            if (OptionsDialog.getAutoMediaLoop() && parameters.size() == 1)
               if (kiss instanceof Audio && ((Audio) kiss).getBackground()) n1 = -1 ;
            
            // If we have a repeat of 0 and a specified file then stop that sound.
            
            if (parameters.size() > 1 && n1 == 0 && kiss instanceof Audio)
            {
               Audio.stop(config,((Audio) kiss),audiotype) ;
               break ;
            }
           
            // Create a new configuration specific media player if none
            // exists.  If one has already been created then use it to
            // play the new media playlist or file.

            if (mf == null) mf = new MediaFrame() ;
            if (config != null) config.setMediaFrame(mf) ;
            mf.setRepeat(n1) ;
            mf.setMinimized(n2 != 0,n3,n4) ;
            mf.setInternal(true) ;
            if (kiss == null)
               mf.play(ze) ;
            else
            {
               if (kiss instanceof Audio) ((Audio) kiss).setType(audiotype) ;
               mf.play(kiss) ;
            }

            // Show the media player if not minimized.

            if (n2 == 0)
            {
               mf.setVisible(true) ;
               MainFrame mainframe = Kisekae.getMainFrame() ;
               if (mainframe != null) mainframe.toFront() ;
            }
            else
            {
               MainMenu mainmenu = null ;
               MainFrame mainframe = Kisekae.getMainFrame() ;
               if (mainframe != null) mainmenu = mainframe.getMainMenu() ;
               if (mainmenu != null) mainmenu.updateRunState() ;                   
            }
            break ;

         // Elseifequal compatibility.

         case 94:		// "elseifequal"
            n1 = event.getIfLevel() - 1 ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            b = !event.getSkip(currentthread) ;
            if (event.getElseIfLevel() > 0) b = true ;
            setSkipActions(b,event.getSkipContext(currentthread),n1,currentthread) ;
            if (b) { event.setElseIfLevel(event.getElseIfLevel() + 1) ; break ; }
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null && o2 instanceof Integer) o1 = new Integer(0) ;
            if (o2 == null && o1 instanceof Integer) o2 = new Integer(0) ;
            if (o1 == null && o2 instanceof Long) o1 = new Long(0) ;
            if (o2 == null && o1 instanceof Long) o2 = new Long(0) ;
            if (o1 == null && o2 instanceof Double) o1 = new Double(0) ;
            if (o2 == null && o1 instanceof Double) o2 = new Double(0) ;
            if (o1 == null && o2 instanceof String) o1 = "" ;
            if (o2 == null && o1 instanceof String) o2 = "" ;
            o1 = (o1 != null) ? o1.toString() : "" ;
            o2 = (o2 != null) ? o2.toString() : "" ;
            setSkipActions((!o1.equals(o2)),event.getSkipContext(currentthread),event.getIfLevel()-1,currentthread) ;
            if (o1.equals(o2)) event.setElseIfLevel(event.getElseIfLevel() + 1) ;
            break;

         // Elseifnotequal compatibility.

         case 95:		// "elseifnotequal"
            n1 = event.getIfLevel() - 1 ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            b = !event.getSkip(currentthread) ;
            if (event.getElseIfLevel() > 0) b = true ;
            setSkipActions(b,event.getSkipContext(currentthread),n1,currentthread) ;
            if (b) { event.setElseIfLevel(event.getElseIfLevel() + 1) ; break ; }
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null && o2 instanceof Integer) o1 = new Integer(0) ;
            if (o2 == null && o1 instanceof Integer) o2 = new Integer(0) ;
            if (o1 == null && o2 instanceof Long) o1 = new Long(0) ;
            if (o2 == null && o1 instanceof Long) o2 = new Long(0) ;
            if (o1 == null && o2 instanceof Double) o1 = new Double(0) ;
            if (o2 == null && o1 instanceof Double) o2 = new Double(0) ;
            if (o1 == null && o2 instanceof String) o1 = "" ;
            if (o2 == null && o1 instanceof String) o2 = "" ;
            o1 = (o1 != null) ? o1.toString() : "" ;
            o2 = (o2 != null) ? o2.toString() : "" ;
            setSkipActions((o1.equals(o2)),event.getSkipContext(currentthread),event.getIfLevel()-1,currentthread) ;
            if (!(o1.equals(o2))) event.setElseIfLevel(event.getElseIfLevel() + 1) ;
            break;

         // Elseifgreaterthan compatibility.

         case 96:		// "elseifgreaterthan"
            n1 = event.getIfLevel() - 1 ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            b = !event.getSkip(currentthread) ;
            if (event.getElseIfLevel() > 0) b = true ;
            setSkipActions(b,event.getSkipContext(currentthread),n1,currentthread) ;
            if (b) { event.setElseIfLevel(event.getElseIfLevel() + 1) ; break ; }
            if (parameters.size() < 2) break ;
            l1 = variable.getLongValue((String) parameters.elementAt(0),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            setSkipActions((l1 <= l2),event.getSkipContext(currentthread),event.getIfLevel()-1,currentthread) ;
            if (!(l1 <= l2)) event.setElseIfLevel(event.getElseIfLevel() + 1) ;
            break;

         // Elseiflessthan compatibility.

         case 97:		// "elseiflessthan"
            n1 = event.getIfLevel() - 1 ;
            if (event.getSkipLevel(currentthread) < n1) break ;
            b = !event.getSkip(currentthread) ;
            if (event.getElseIfLevel() > 0) b = true ;
            setSkipActions(b,event.getSkipContext(currentthread),n1,currentthread) ;
            if (b) { event.setElseIfLevel(event.getElseIfLevel() + 1) ; break ; }
            if (parameters.size() < 2) break ;
            l1 = variable.getLongValue((String) parameters.elementAt(0),event) ;
            l2 = variable.getLongValue((String) parameters.elementAt(1),event) ;
            setSkipActions((l1 >= l2),event.getSkipContext(currentthread),event.getIfLevel()-1,currentthread) ;
            if (!(l1 >= l2)) event.setElseIfLevel(event.getElseIfLevel() + 1) ;
            break;

         // Clone an object.

         case 98:		// "clone"
            Group newgroup = null ;
            Integer groupnumber = null ;
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            if (config == null) break ;

            // Perform the new group allocation under a synchronization lock.
            // This prevents simultaneous updates or references from other
            // concurrent activities.

            synchronized (config)
            {
               groupnumber = new Integer(config.getAvailableGroupNumber()) ;
               newgroup = (Group) ((Group) kiss).clone() ;
               newgroup.setClone(kiss) ;
               newgroup.setInternal(true) ;
               newgroup.setIdentifier(groupnumber) ;
               newgroup.setKey(newgroup.getKeyTable(),config.getID(),groupnumber) ;
               Vector groups = config.getGroups() ;
               groups.addElement(newgroup) ;
               config.incrementMaxGroups() ;

               // Initialize the cloned object.

               Vector cels = config.getCels() ;
               Vector clonedcels = newgroup.getCels() ;
               newgroup.init() ;
               location = ((Group) kiss).getLocation() ;
               newgroup.setPlacement(location.x,location.y) ;
               newgroup.drop() ;

               // Clone duplicate cels.

               Enumeration enum1 = clonedcels.elements() ;
               while (enum1.hasMoreElements())
               {
                  Cel c = (Cel) enum1.nextElement() ;
                  n = cels.indexOf(c) ;
                  Cel newcel = (Cel) c.clone() ;
                  newcel.setClone(c) ;
                  Integer celnumber = new Integer(config.getMaxCelNumber()) ;
                  config.setMaxCelNumber(new Integer(config.getMaxCelNumber()+1)) ;
                  offset = newcel.getOffset() ;
                  offset = (offset == null) ? new Point(0,0) : new Point(offset) ;
                  cels.insertElementAt(newcel,n) ;
                  newcel.setIdentifier(celnumber) ;
                  newcel.setInternal(true) ;
                  newcel.setLocation(newcel.getLocation()) ;
                  newcel.setOffset(offset) ;
                  newcel.setKey(newcel.getKeyTable(),config.getID(),celnumber) ;
                  newgroup.addCel(newcel) ;
               }

               // Add this new group to all appropriate pages.

               newgroup.setVisible(false) ;
               newgroup.setInitialOffset() ;
               for (i = 0 ; i < config.getPageCount() ; i++)
               {
                  if (newgroup.isOnPage(new Integer(i)))
                  {
                     pageset = config.getPage(i) ;
                     pageset.addGroup(newgroup) ;
                  }
               }
            }

            // Set the variable to the new group number.

            n1 = (groupnumber != null) ? groupnumber.intValue() : 0 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            if (panel != null && panel.isVisible())
            {
               if (!("set".equals(event.getIdentifier()) ||
                     "version".equals(event.getIdentifier()) ||
                     "begin".equals(event.getIdentifier())))
               {
                  panel.updateCelList() ;
               }
            }
            box = newgroup.getBoundingBox() ;
            break;

         // Destroy a cloned object.

         case 99:		// "destroy"
            if (parameters.size() < 1) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof Group)) break ;
            Group group = (Group) kiss ;
            o1 = kiss.getClone() ;
            if (o1 == null) break ;
            if (config == null) break ;

            // Remove this group from all appropriate pages.

            synchronized (config)
            {
               for (i = 0 ; i < config.getPageCount() ; i++)
               {
                  if (group.isOnPage(new Integer(i)))
                  {
                     pageset = config.getPage(i) ;
                     pageset.removeGroup(group) ;
                  }
               }

               // Remove all cel objects from the configuration.

               Vector cels = group.getCels() ;
               Vector configcels = config.getCels() ;
               Enumeration enum1 = cels.elements() ;
               while (enum1.hasMoreElements())
               {
                  Cel c = (Cel) enum1.nextElement() ;
                  configcels.removeElement(c) ;
                  c.removeKey(c.getKeyTable(),config.getID(),c.getIdentifier()) ;
                  c.removeKey(c.getKeyTable(),config.getID(),c.getPath()) ;
               }

               // Remove the group from the configuration.

               Vector groups = config.getGroups() ;
               groups.removeElement(group) ;
               group.removeKey(group.getKeyTable(),config.getID(),group.getIdentifier()) ;
               config.decrementMaxGroups() ;
            }

            if (panel != null && panel.isVisible())
            {
               if (!("set".equals(event.getIdentifier()) ||
                     "version".equals(event.getIdentifier()) ||
                     "begin".equals(event.getIdentifier())))
               {
                  panel.updateCelList() ;
               }
            }
            box = kiss.getBoundingBox() ;
            break;

         // Determine the object draw level.

         case 100:		// "letlevel(Variable,Object)"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            o = kiss.getLevel() ;
            n1 = (o instanceof Integer) ? ((Integer) o).intValue() : 0 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the object draw level.  This must force a full redraw on
         // event completion as the layering order has changed.

         case 101:		// "setlevel(Object,Variable)"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            kiss.setLevel(new Integer(n1)) ;
            box = kiss.getBoundingBox() ;
            if (panel != null) panel.setRedraw(true) ;
            break;

         // Returns the index of a substring in a string.

         case 102:		// "indexof(N,String,Substring,[Case])"
            if (parameters.size() < 3) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            o2 = variable.getValue((String) parameters.elementAt(2),event) ;
            if (o1 == null || o2 == null) break ;
            
            n3 = 1 ;
            s1 = o1.toString() ;
            s2 = o2.toString() ;
            if (parameters.size() > 3)
               n3 = variable.getIntValue((String) parameters.elementAt(3),event) ;
            String st1 = (n3 == 0) ? s1.toUpperCase() : s1 ;
            String st2 = (n3 == 0) ? s2.toUpperCase() : s2 ;
            variable.setIntValue((String) parameters.elementAt(0),st1.indexOf(st2),event) ;
            break;

         // Replaces a substring in a string.  Substring1 in String is replaced
         // with Substring2 and the result is returned in Result.

         case 103:		// "replacestr(Result,String,Substring1,Substring2[,Count,Case])"
            if (parameters.size() < 4) break ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            o3 = variable.getValue((String) parameters.elementAt(2),event) ;
            o4 = variable.getValue((String) parameters.elementAt(3),event) ;
            if (o2 == null || o3 == null || o4 == null) break ;

            // Initialize.  Watch for case sensitive comparisons.
            
            n1 = 0 ;
            n2 = 1 ;
            n3 = 1 ;
            s2 = o2.toString() ;
            s3 = o3.toString() ;
            s4 = o4.toString() ;
            if (parameters.size() > 4)
               n2 = variable.getIntValue((String) parameters.elementAt(4),event) ;
            if (parameters.size() > 5)
               n3 = variable.getIntValue((String) parameters.elementAt(5),event) ;
            st1 = (n3 == 0) ? s2.toUpperCase() : s2 ;
            st2 = (n3 == 0) ? s3.toUpperCase() : s3 ;
            s1 = s2 ;
 
            // Repeat for count.  Negative counts replace all.
            
            while (n2 != 0)
            {
               n1 = st1.indexOf(st2,n1) ;
               if (n1 < 0) break ;
               s1 = s2.substring(0,n1) + s4 + s2.substring(n1+s3.length()) ;
               s2 = s1 ;
               st1 = (n3 == 0) ? s2.toUpperCase() : s2 ;
               n1 += s4.length() ; 
               n2-- ;
            }

            // Set result.  No find returns unchanged result string.
            
            variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Return the time left on a timer before expiry.  If the alarm is
         // running or will begin on the next cycle we return 0.  If the
         // alarm has not been scheduled we return -1.

         case 106:	// "lettimer"
            if (parameters.size() < 2) break ;
            if (kiss == null)
            {
               alarmid = evaluateParam((String) parameters.elementAt(1)) ;
               if (alarmid instanceof String) alarmid = ((String) alarmid).toUpperCase() ;
               kiss = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,alarmid) ;
            }
            if (!(kiss instanceof Alarm)) break ;
            alarm = (Alarm) kiss ;
            delay = alarm.getInterval() ;
            n1 = (delay > 0) ? delay - ((int) alarm.getTime()) : 0 ;
            if (n1 < OptionsDialog.getTimerPeriod()) n1 = 0 ;
            if (delay < 0 && alarm.getTime() == 0) n1 = -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // Set the multipalette for a cel, group, or cel group.

         case 107:	// "setpal(cel/object/celgroup,multipalette)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            kiss.fixPaletteGroup(new Integer(n1)) ;
            box = kiss.getBoundingBox() ;
            break ;

         // Determine the ordinal palette number (kcf number) for a cel, group,
         // or cel group.

         case 108:	// "letkcf(variable,cel/object/celgroup)"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (kiss == null) break ;
            o = kiss.getPaletteID() ;
            n1 = (o instanceof Integer) ? ((Integer) o).intValue() : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the ordinal palette (kcf number) for a cel, group, or cel group.

         case 109:	// "setkcf(cel/object/celgroup,palette)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (kiss == null) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            kiss.changePaletteID(new Integer(n1)) ;
            box = kiss.getBoundingBox() ;
            break ;

         // Apply a viewer specific command.

         case 110:	// "viewer(command,args,...)"
            if (parameters.size() < 1) break ;
            if (panel == null) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            o2 = (parameters.size() >= 2) ? variable.getValue((String) parameters.elementAt(1),event) : null ;
            o3 = (parameters.size() >= 3) ? variable.getValue((String) parameters.elementAt(2),event) : null ;
            final String vs1 = (o1 != null) ? o1.toString() : "" ;
            final String vs2 = (o2 != null) ? o2.toString() : "" ;
            final String vs3 = (o3 != null) ? o3.toString() : "" ;
            
            // Most viewer commands have to run on the AWT thread. 
            // Some can be processed immediately.
            
            if ("setoption".equalsIgnoreCase(vs1))
            {
               OptionsDialog.setOption(vs2,vs3) ;
               MainFrame mfm = Kisekae.getMainFrame() ;
               OptionsDialog opt = (mfm != null) ? mfm.getOptionsDialog() : null ;
               if (opt != null)
               {
                  opt.setControls() ;
                  opt.apply() ;
                  if (panel.isVisible())
                     panel.requestFocus();
               }
               break ;
            }
            if ("getoption".equalsIgnoreCase(vs1))
            {
               s = OptionsDialog.getOption(vs2) ;
               if ("true".equals(s)) s = "1" ;
               if ("false".equals(s)) s = "0" ;
               variable.setValue((String) parameters.elementAt(2),s,event) ;
               break ;
            }

            // Invoke specialized viewer commands on the AWT thread.

            Runnable runner = new Runnable()
            {
               public void run()
               {
                  MainFrame mf = Kisekae.getMainFrame() ;
                  Configuration config = (mf != null) ? mf.getConfig() : null ;
                  PanelFrame panel = (mf != null) ? mf.getPanel() : null ;
                  KissMenu menu = (mf != null) ? mf.getMenu() : null ;
                  MainMenu mm = (mf != null) ? mf.getMainMenu() : null ;
                  PanelMenu pm = (mf != null) ? mf.getPanelMenu() : null ;
                  PageSet pageset = (panel != null) ? panel.getPage() : null ;
                  Object cid = (config != null) ? config.getID() : null ;

                  if ("menu".equalsIgnoreCase(vs1))
                  {
                     if (pm != null && mm != null)
                     {
                        if ("reset".equalsIgnoreCase(vs2)) pm.reset.doClick() ;
                        if ("restart".equalsIgnoreCase(vs2)) pm.restart.doClick() ;
                        if ("magnify".equalsIgnoreCase(vs2)) pm.magnify.doClick() ;
                        if ("reduce".equalsIgnoreCase(vs2)) pm.reduce.doClick() ;
                        if ("sizetofit".equalsIgnoreCase(vs2)) mm.fitpanel.doClick() ;
                        if ("scaletofit".equalsIgnoreCase(vs2)) mm.fitscreen.doClick() ;
                        if ("logfile".equalsIgnoreCase(vs2)) mm.logfile.doClick() ;
                        if ("statusbar".equalsIgnoreCase(vs2)) mm.statusbar.doClick() ;
                        if ("toolbar".equalsIgnoreCase(vs2)) mm.toolbar.doClick() ;
                        if ("openweb".equalsIgnoreCase(vs2)) mm.openweb.doClick() ;
                        if ("submitbug".equalsIgnoreCase(vs2)) mm.bugreport.doClick() ;
                        if ("openkiss".equalsIgnoreCase(vs2)) mm.openkiss.doClick() ;
                        if ("activecnf".equalsIgnoreCase(vs2)) pm.cnffile.doClick() ;
                     }
                  }
                  else if ("reset".equalsIgnoreCase(vs1))
                  {
                     if (panel != null) panel.reset() ;
                  }
                  else if ("restart".equalsIgnoreCase(vs1))
                  {
                     if (mf != null) mf.restart() ;
                  }
                  else if ("select".equalsIgnoreCase(vs1))
                  {
                     if (menu == null) return ;
                     menu.eventSelect() ;
                  }
                  else if ("open".equalsIgnoreCase(vs1))
                  {
                     if (menu == null) return ;
                     if (config == null) return ;
                     FileOpen fd = config.getFileOpen() ;
                     if (fd == null) return ;
                     fd.open() ;
                     ArchiveFile zip = fd.getZipFile() ;
                     String name = vs2 ;

                     // Find the object.

                     if (zip instanceof DirFile)
                     {
                        File f = new File(config.getDirectory(),vs2) ;
                        name = f.getPath() ;
                     }

                     // Open the file for this entry.

                     fd.open(name) ;
                     ArchiveEntry ze = fd.getZipEntry() ;
                     if (ze == null) return ;
                     menu.openContext(fd,ze) ;
                  }
                  else if ("view".equalsIgnoreCase(vs1))
                  {
                     TextObject text = TextObject.findTextObject("\""+vs2+"\"",config,null) ;
                     if (text == null) return ;
                     InputStream is = text.getInputStream() ;
                     final TextFrame tf = new TextFrame(null,is,false,false,text.getName()) ;
                     // Seems necessary to resolve Java 1.4 flicker problem
                     SwingUtilities.invokeLater(new Runnable()
                     { public void run() { tf.setVisible(true); } } );
                  }
                  else if ("print".equalsIgnoreCase(vs1))
                  {
                     int orientation = PageFormat.PORTRAIT ;
                     TextObject text = TextObject.findTextObject("\""+vs2+"\"",config,null) ;
                     if (text == null) return ;
                     if (text == null) return ;
                     new PrintPreview(text,orientation) ;
                  }
                  else if ("exit".equalsIgnoreCase(vs1))
                  {
                     if (mm != null) mm.exit.doClick() ;
                  }
                  else if ("setmenu".equalsIgnoreCase(vs1))
                  {
                     if (mf == null) return ;
                     UserMenu usermenu = new UserMenu(mf) ;
                     for (int i = 1 ; i < parameters.size() ; )
                     {
                        Object o1 = variable.getValue((String) parameters.elementAt(i),event) ;
                        if (!(o1 instanceof String)) continue ;
                        String s = (String) o1 ;
                        i = i + 1 ;
                        if (i < parameters.size())
                        {
                           Object o2 = findGroupOrCel((String) parameters.elementAt(i),event) ;
                           if (!(o2 instanceof Group)) continue ;
                           usermenu.addMenu(s,(Group) o2) ;
                           i = i + 1 ;
                        }
                     }
                     mf.setMenu(usermenu);
                  }
                  else if ("restoremenu".equalsIgnoreCase(vs1))
                  {
                     if (mf == null) return ;
                     mf.setMenu((pm != null) ? pm : mm);
                  }
                  else if ("seticon".equalsIgnoreCase(vs1))
                  {
                     Object o2 = findGroupOrCel((String) parameters.elementAt(1),event) ;
                     if (o2 instanceof Cel)
                     {
                        Image image = ((Cel) o2).getImage() ;
                        if (mf != null && image != null)
                           mf.setIconImage(image) ;
                     }
                  }
                  else if ("settitle".equalsIgnoreCase(vs1))
                  {
                     if (mf != null) mf.setUserTitle(vs2) ;
                  }
               }
            } ;
            javax.swing.SwingUtilities.invokeLater(runner);
            break ;

         // Determine the length of a string.

         case 111:		// "strlen(N,String)"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 == null) ? "" : o1.toString() ;
            n1 = s1.length() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Get the text value of a component.

         case 112:		// "getText(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            s1 = ((JavaCel) kiss).getText() ;
            variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Set the text value of a component.

         case 113:		// "setText(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            ((JavaCel) kiss).setText(s1) ;
            if (kiss.isVisible()) box = kiss.getBoundingBox() ;
            break;

         // Get the text value of a component.

         case 114:		// "getSelected(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = ((JavaCel) kiss).getSelected() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the selection state of a component.

         case 115:		// "setSelected(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            ((JavaCel) kiss).setSelected(o1) ;
            break;

         // Get the value of a list item at a specific index.

         case 116:		// "getValueAt(vbl,component,index)"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            s1 = ((JavaCel) kiss).getValueAt(n1) ;
            variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Set the value of a list item.

         case 117:		// "setValueAt(component,index,vbl)"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(2),event) ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            ((JavaCel) kiss).setValueAt(s1,n1) ;
            break;

         // Set the value of a combo box item.

         case 118:		// "addItem(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            ((JavaCel) kiss).addItem(s1) ;
            break;

         // Remove an item from a combo box.

         case 119:		// "removeItem(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            ((JavaCel) kiss).removeItem(s1) ;
            break;

         // Get the index of a selected list item.

         case 120:		// "getSelectedIndex(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = ((JavaCel) kiss).getSelectedIndex() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the index of a selected list item.

         case 121:		// "setSelectedIndex(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            ((JavaCel) kiss).setSelectedIndex(n1) ;
            break;

         // Get the value of a selected list item.

         case 122:		// "getSelectedValue(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            s1 = ((JavaCel) kiss).getSelectedValue() ;
            variable.setValue((String) parameters.elementAt(0),s1,event) ;
            break;

         // Set the value of a selected list item.

         case 123:		// "setSelectedValue(component,vbl)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            ((JavaCel) kiss).setSelectedValue(s1) ;
            break;

         // Get the index of a list item.

         case 126:		// "getIndexOf(vbl,component,value)"
            if (parameters.size() < 3) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            o1 = variable.getValue((String) parameters.elementAt(2),event) ;
            n1 = (o1 != null) ? ((JavaCel) kiss).getIndexOf(o1.toString()) : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set a list global adjustment state.

         case 127:		// "removeAll(component)"
            if (parameters.size() < 1) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            ((JavaCel) kiss).removeAll() ;
            break;

         // Get the number of items in a list.

         case 128:		// "getItemCount(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = ((JavaCel) kiss).getItemCount() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Get the enable state of a component.

         case 129:		// "getEnabled(integer,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = ((JavaCel) kiss).getEnabled() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the enable state of a component.

         case 130:		// "setEnabled(component,integer)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(1),event) ;
            ((JavaCel) kiss).setEnabled(n1 != 0) ;
            if (kiss.isVisible()) box = kiss.getBoundingBox() ;
            break;

         // Get the next selected index of a multiple selection list component.

         case 131:		// "getNextSelectedIndex(vbl,component)"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel)) break ;
            n1 = ((JavaCel) kiss).getNextSelectedIndex() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Set the attributes of a component. The string parameter must
         // be a valid attribute list as specified on the cel definition.
         // Attribute changes are temporary by default.

         case 132:		// "setAttributes(component,string[,temporary])"
            if (parameters.size() < 2) break ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            if (!(kiss instanceof JavaCel || kiss instanceof Video)) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (!(o1 instanceof String)) break ;
            
            boolean temp = true ;
            if (parameters.size() > 2) 
               temp = (variable.getIntValue((String) parameters.elementAt(2),event) == 0) ;
            if (kiss instanceof JavaCel)
               ((JavaCel) kiss).setAttributes(o1.toString(),temp) ;
            if (kiss instanceof Video)
               ((Video) kiss).setAttributes(o1.toString(),temp) ;
            if (kiss.isVisible()) box = kiss.getBoundingBox() ;
            break;

         // Get the attributes of a component. The variable is set to the
         // attribute string for the component, or the attribute value if
         // the optional string is specified.

         case 157:		// "getAttributes(vbl,component[,string])"
            if (parameters.size() < 2) break ;
            s1 = "" ;
            if (kiss == null)
               kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof JavaCel || kiss instanceof Video)) break ;
            
            if (parameters.size() < 3) 
            {
               if (kiss instanceof JavaCel)
                  s1 = ((JavaCel) kiss).getAttributes() ;
               if (kiss instanceof Video)
                  s1 = ((Video) kiss).getAttributes() ;
               variable.setValue((String) parameters.elementAt(0),s1,event) ;
            }
            else
            {
               if (kiss instanceof JavaCel)
                  s1 = ((JavaCel) kiss).getAttribute((String) parameters.elementAt(2)) ;
               if (kiss instanceof Video)
                  s1 = ((Video) kiss).getAttribute((String) parameters.elementAt(2)) ;
               variable.setValue((String) parameters.elementAt(0),s1,event) ;
            }
            break;

         // Open an external file. If the file is opened for read only mode
         // then a TextObject is created that relates to the configuration
         // archive file.  This can be an archive or a directory.  If the file
         // is opened for write mode then a DirFile object is created and the
         // TextObject will be written to a local directory.  This defaults to
         // the directory from which the configuration was loaded.  If this is
         // a read-only directory, or the configuration was loaded from the
         // network then the Kisekae program base directory is used.  If the
         // file is opened for read/write mode then the file contents will
         // be read firstly from the directory copy and if this fails then
         // an archive copy will be referenced.

         case 133:		// "open(status,file,mode,[decode])"
            try
            {
               if (parameters.size() < 3) break ;
               o1 = variable.getValue((String) parameters.elementAt(1),event) ;
               s1 = (o1 instanceof String) ? (String) o1 : null ;
               o2 = variable.getValue((String) parameters.elementAt(2),event) ;
               s2 = (o2 instanceof String) ? (String) o2 : "r" ;
               if (s1 == null) break ;
               String refname = s1 ;

               // Establish the output file name.   Watch for temporary files
               // and memory files as these types of archives must be redirected
               // to the default base directory.

               URL base = Kisekae.getBase() ;
               String dir = (base != null) ? base.getPath() : File.pathSeparator ;
               ArchiveFile zip = (config != null) ? config.getZipFile() : null ;
               boolean read = (s2.indexOf("r") >= 0) ;
               boolean write = (s2.indexOf("w") >= 0) ;
               if (!read && !write) read = true ;
               if (zip == null || (!(zip instanceof DirFile) && write))
               {
                  b = true ;
                  FileOpen fo = new FileOpen(null) ;
                  if (zip == null) b = false ;
                  if (b && zip.getMemFile() != null) b = false ;
                  if (b && zip.getName().indexOf("UltraKiss-") >= 0) b = false ;
                  if (b) dir = zip.getDirectoryName() ;
                  zip = new DirFile(fo,dir) ;
               }

               // Establish the text object name.  This is a full path
               // for directory access or a simple relative name for
               // archive access.  If the file cannot be written, default
               // to the base directory.

               if (zip instanceof DirFile)
               {
                  dir = zip.getDirectoryName() ;
                  File f = new File(dir) ;
                  if (write && !f.canWrite())
                  {
                     try { dir = System.getProperty("user.home") ; }
                     catch (SecurityException e) { }
                     f = new File(dir) ;
                  }
                  if (write && !f.canWrite() && base != null)
                  {
                     dir = base.getPath() ;
                  }
                  f = new File(dir,s1) ;
                  s1 = f.getPath() ;
                  if (OptionsDialog.getDebugLoad())
                     System.out.println("FKiSS open file " + s1) ;
               }
               else
               {
                  if (OptionsDialog.getDebugLoad())
                     System.out.println("FKiSS open archive entry " + s1) ;
               }

               // If the Text Object does not exist, create it.  If it does
               // exist then we are opening a file that has not been closed.

               TextObject text = TextObject.findTextObject("\""+refname+"\"",config,null) ;
               if (text == null)
               {
                  boolean open = zip.isOpen() ;
                  if (!open) zip.open() ;
                  text = new TextObject(zip,s1,s2) ;
                  text.setKey(text.getKeyTable(),cid,refname.toUpperCase()) ;
                  n1 = (read) ? text.read() : 0 ;

                  // If the read failed and we are open for read and the
                  // configuration was loaded from an archive, we must check
                  // the archive for the source file.

                  zip = (config != null) ? config.getZipFile() : null ;
                  if (n1 < 0 && read && (zip instanceof ArchiveFile))
                  {
                     if (!zip.isOpen()) zip.open() ;

                     // The name referenced on the open command is relative to
                     // any configuration directory.

//                     if (config != null)
//                     {
//                        String cnfdir = config.getDirectory() ;
//                        File f = new File(cnfdir,s1) ;
//                        refname = f.getPath() ;
//                     }
                     ze = zip.getEntry(refname) ;
                     n1 = text.read(zip,ze) ;
                  }
               }

               // If we opened an existing Text Object for write mode
               // then erase the contents.  Read/Write mode retains the
               // contents.

               else
               {
                  if (write && !read) text.setText("") ;
                  n1 = text.getBytes() ;
               }

               // Decode the bytes if necessary.

               if (parameters.size() >= 4)
               {
                  n3 = variable.getIntValue((String) parameters.elementAt(3),event) ;
                  if (n3 != 0)
                  {
                     s = text.getText() ;
                     try { s = new String(new sun.misc.BASE64Decoder().decodeBuffer(s)) ; }
                     catch (SecurityException e) { }
                     text.setText(s) ;
                  }
               }
            }
            catch (IOException e) { n1 = -1 ; }

            // Return the file size.

            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // Read from an external file.

         case 134:		// "read(status,file,string,line)"
            if (parameters.size() < 4) break ;
            variable.setIntValue((String) parameters.elementAt(0),-1,event) ;
            s3 = (String) parameters.elementAt(1) ;
            TextObject text = TextObject.findTextObject(s3,config,null) ;
            if (text == null) break ;

            // Read the line from the text object.  Styled text objects are
            // converted to a TextArea after which the line is referenced.

            try
            {
               JTextArea ta = null ;
               JTextComponent textcomp = text.getTextComponent() ;
               if (textcomp instanceof JTextArea)
                  ta = (JTextArea) textcomp ;
               if (textcomp instanceof JTextPane)
                  ta = new JTextArea(((JTextPane) textcomp).getText()) ;
               n2 = variable.getIntValue((String) parameters.elementAt(3),event) ;
               int start = ta.getLineStartOffset(n2) ;
               int end = ta.getLineEndOffset(n2) ;
               n1 = end - start ;
               s2 = ta.getText(start,n1) ;
               n2 = s2.length() ;
            }
            catch (Exception e) { n1 = -1 ; s2 = "" ; }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            variable.setValue((String) parameters.elementAt(2),s2,event) ;
            break;

         // Write to an external file.

         case 135:		// "write(status,file,string)"
            if (parameters.size() < 3) break ;
            variable.setIntValue((String) parameters.elementAt(0),-1,event) ;
            s2 = (String) parameters.elementAt(1) ;
            text = TextObject.findTextObject(s2,config,null) ;
            if (text == null) break ;

            // Write a line to the text object.

            o1 = variable.getValue((String) parameters.elementAt(2),event) ;
            if (o1 == null) break ;
            s1 = o1.toString() ;
            text.append(s1) ;
            variable.setIntValue((String) parameters.elementAt(0),s1.length(),event) ;
            break;

         // Close an external file.

         case 136:		// "close(status,file,[commit,encode])"
            if (parameters.size() < 2) break ;
            variable.setIntValue((String) parameters.elementAt(0),-1,event) ;
            s2 = (String) parameters.elementAt(1) ;
            text = TextObject.findTextObject(s2,config,null) ;
            if (text == null) break ;
            o2 = variable.getValue(s2,event) ;
            String refname = (o2 instanceof String) ? (String) o2 : null ;

            // Write the file if we must commit.  The write assumes a standard
            // directory type file.  Text is encoded if necessary.

            n1 = 0 ;
            if (parameters.size() >= 3)
            {
               if (parameters.size() >= 4)
               {
                  n = variable.getIntValue((String) parameters.elementAt(3),event) ;
                  if (n != 0)
                  {
                     s = text.getText() ;
                     try { s = new sun.misc.BASE64Encoder().encode(s.getBytes()) ; }
                     catch (SecurityException e) { }
                     text.setText(s) ;
                  }
               }

               // Perform the write.  Create directories if necessary.

               try
               {
                  File f = new File(text.getPath()) ;
                  String filedir = f.getParent() ;
                  if (filedir != null)
                  {
                     File f1 = new File(filedir) ;
                     if (!f1.exists()) f1.mkdirs() ;
                  }
                  OutputStream os = new FileOutputStream(f) ;
                  n1 = text.write(null,os,".txt") ;
                  if (OptionsDialog.getDebugLoad())
                     System.out.println("FKiSS write file " + text.getPath() + " bytes " + n1);
               }
               catch (IOException e)
               {
                  n1 = -1 ;
                  System.out.println("FKissAction: write " + text.getPath() + " " + e);
               }
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            TextObject.removeKey(TextObject.getKeyTable(),cid,refname.toUpperCase());
            break;

         // Edit a file.

         case 137:		// "edit(status,file,command,arg1,arg2,...)"
            if (parameters.size() < 3) break ;
            variable.setIntValue((String) parameters.elementAt(0),-1,event) ;
            s1 = (String) parameters.elementAt(1) ;
            text = TextObject.findTextObject(s1,config,null) ;
            if (text == null) break ;
            o2 = variable.getValue((String) parameters.elementAt(2),event) ;
            s2 = (o2 instanceof String) ? (String) o2 : "" ;
            n1 = -1 ;

            // Edit the text file object.

            if ("append".equalsIgnoreCase(s2))
            {
               for (i = 3 ; i < parameters.size() ; i++)
               {
                  o3 = variable.getValue((String) parameters.elementAt(i),event) ;
                  s1 = o3.toString() ;
                  text.append(s1) ;
              }
               n1 = text.getBytes() ;
           }
            else if ("replace".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 5) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               o4 = variable.getValue((String) parameters.elementAt(4),event) ;
               n1 = text.replace(o3,o4) ;
            }
            else if ("replaceall".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 5) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               o4 = variable.getValue((String) parameters.elementAt(4),event) ;
               n1 = text.replaceall(o3,o4) ;
            }
            else if ("delete".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               n1 = text.delete(o3) ;
            }
            else if ("gettext".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               s1 = text.getText() ;
               variable.setValue((String) parameters.elementAt(3),s1,event) ;
               n1 = s1.length() ;
            }
            else if ("settext".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               if (o3 instanceof String) text.setText(o3.toString()) ;
               n1 = text.getBytes() ;
            }
            else if ("getbody".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               s1 = text.getBody() ;
               variable.setValue((String) parameters.elementAt(3),s1,event) ;
               n1 = s1.length() ;
            }
            else if ("find".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               n1 = text.find(o3) ;
            }
            else if ("findline".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               n1 = text.findLine(o3) ;
            }
            else if ("getline".equalsIgnoreCase(s2))
            {
               if (parameters.size() < 4) break ;
               o3 = variable.getValue((String) parameters.elementAt(3),event) ;
               n1 = text.getLine(o3) ;
            }

            // Return the document size.

            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;

         // A generic environment command.

         case 138:	// "environment(vbl,command,[v1,v2,...])"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString() : "" ;
            s = null ;  n = 0 ; o = null ;

            try
            {
               if ("getdate".equalsIgnoreCase(s1))
               {
                  Calendar date = Calendar.getInstance() ;
                  if (parameters.size() > 2)
                  {
                     o1 = variable.getValue((String) parameters.elementAt(2),event) ;
                     if (o1 instanceof Calendar) date = (Calendar) o1 ;
                  }
                  s = DateFormat.getDateInstance().format(date.getTime()) ;
               }
               if ("gettime".equalsIgnoreCase(s1))
               {
                  Calendar date = Calendar.getInstance() ;
                  if (parameters.size() > 2)
                  {
                     o1 = variable.getValue((String) parameters.elementAt(2),event) ;
                     if (o1 instanceof Calendar) date = (Calendar) o1 ;
                  }
                  s = new SimpleDateFormat("HH:mm:ss").format(date.getTime()) ;
               }
               else if ("getuser".equalsIgnoreCase(s1))
               {
                  s = Kisekae.getUser() ;
                  try { s = new String(new sun.misc.BASE64Decoder().decodeBuffer(s)) ; }
                  catch (SecurityException e) { }
               }
               else if ("gettotalmemory".equalsIgnoreCase(s1))
               {
                  n = (int) Runtime.getRuntime().totalMemory() ;
               }
               else if ("getfreememory".equalsIgnoreCase(s1))
               {
                  n = (int) Runtime.getRuntime().freeMemory() ;
               }
               else if ("reclaimmemory".equalsIgnoreCase(s1))
               {
                  Runtime.getRuntime().gc() ;
               }

               // The calendar function can return an internal Calendar object.

               else if ("calendar".equalsIgnoreCase(s1) && parameters.size() > 2)
               {
                  o2 = variable.getValue((String) parameters.elementAt(2),event) ;
                  s2 = (o2 != null) ? o2.toString() : "" ;
                  Calendar date = Calendar.getInstance() ;
                  if (parameters.size() > 3)
                  {
                     o1 = variable.getValue((String) parameters.elementAt(3),event) ;
                     if (o1 instanceof Calendar) date = (Calendar) o1 ;
                  }

                  // Return a Calendar instance.  This can subsequently be
                  // returned on subsequent calls for FKiSS date modification.

                  if ("getinstance".equalsIgnoreCase(s2))
                  {
                     if (parameters.size() > 5)
                     {
                        n1 = variable.getIntValue((String) parameters.elementAt(3),event) ;
                        n2 = variable.getIntValue((String) parameters.elementAt(4),event) ;
                        n3 = variable.getIntValue((String) parameters.elementAt(5),event) ;
                        date.set(n1,n2,n3) ;
                     }
                     o = date ;
                  }

                  // Return the numeric calendar values.

                  if ("getyear".equalsIgnoreCase(s2))
                     n = date.get(Calendar.YEAR) ;
                  if ("getmonth".equalsIgnoreCase(s2))
                     n = date.get(Calendar.MONTH) ;
                  if ("getdate".equalsIgnoreCase(s2))
                     n = date.get(Calendar.DAY_OF_MONTH) ;
                  if ("getdayofweek".equalsIgnoreCase(s2))
                     n = date.get(Calendar.DAY_OF_WEEK) ;
                  if ("getdaysinmonth".equalsIgnoreCase(s2))
                     n = date.getActualMaximum(Calendar.DAY_OF_MONTH) ;

                  // Return the string calendar values.

                  if ("year".equalsIgnoreCase(s2))
                     s = new SimpleDateFormat("yyyy").format(date.getTime()) ;
                  if ("month".equalsIgnoreCase(s2))
                     s = new SimpleDateFormat("MMMM").format(date.getTime()) ;
                  if ("date".equalsIgnoreCase(s2))
                     s = new SimpleDateFormat("dd").format(date.getTime()) ;
                  if ("dayofweek".equalsIgnoreCase(s2))
                     s = new SimpleDateFormat("EEEE").format(date.getTime()) ;

                  // Set the calendar values.

                  if ("setyear".equalsIgnoreCase(s2) && parameters.size() > 4)
                  {
                     n = variable.getIntValue((String) parameters.elementAt(4),event) ;
                     date.set(Calendar.YEAR,n) ;
                     o = date ;
                  }
                  if ("setmonth".equalsIgnoreCase(s2) && parameters.size() > 4)
                  {
                     n = variable.getIntValue((String) parameters.elementAt(4),event) ;
                     date.set(Calendar.MONTH,n) ;
                     o = date ;
                  }
                  if ("setdate".equalsIgnoreCase(s2) && parameters.size() > 4)
                  {
                     n = variable.getIntValue((String) parameters.elementAt(4),event) ;
                     date.set(Calendar.DAY_OF_MONTH,n) ;
                     o = date ;
                  }
               }
            }
            catch (SecurityException e) { }

            // The environment command can return a string, an integer, or
            // an internal object.

            if (o != null)
               variable.setValue((String) parameters.elementAt(0),o,event) ;
            else if (s != null)
               variable.setValue((String) parameters.elementAt(0),s,event) ;
            else
               variable.setIntValue((String) parameters.elementAt(0),n,event) ;
            break ;

         // Show a confirm dialog.  This is similar to a Notify command
         // except that FKiSS execution stops and the confirmation state
         // is returned in a variable.

         case 139:	// "confirm(vbl,string1,string2,...)"
            if (parameters.size() < 2) break ;
            if (panel == null) break ;
            if (Kisekae.isBatch()) break ;

            message = "" ;
            if (variable != null)
            {
               for (i = 1 ; i < parameters.size() ; i++)
               {
                  o = variable.getValue((String) parameters.elementAt(i),event) ;
                  if (o != null) message += o.toString() ;
               }
            }

            // Construct the notify box.

            event.setConfirmWait(true) ;
            final String confirmmsg = new String(message) ;
            final Object confirmlock = new Object() ;
            final NotifyDialog cd = new NotifyDialog(Kisekae.getMainFrame(),
               "Confirm",confirmmsg,null,true) ;

            // Show the notify box in the AWT event handler thread.  This event
            // thread is suspended until the confirm dialog is acknowledged.

            if (!SwingUtilities.isEventDispatchThread())
            {
               Runnable confirm = new Runnable()
               {
                  public void run()
                  {
                     cd.setVisible(true) ;
                     synchronized (confirmlock)
                     {
                        confirmlock.notifyAll() ;
                     }
                  }
               } ;
               javax.swing.SwingUtilities.invokeLater(confirm) ;
               synchronized (confirmlock)
               {
                  confirmlock.wait() ;
               }
            }
            else
            {
               cd.setVisible(true) ;
            }

            // Set the return value and continue.

            n = cd.getConfirmValue() ;
            variable.setIntValue((String) parameters.elementAt(0),n,event);
            event.setConfirmWait(false) ;
            break ;

         // Set or clear modal event processing.  If a modal object is set
         // events are only accepted from the modal source.

         case 140:	// "setmodal([vbl])"
            kiss = null ;
            if (parameters.size() > 0)
               kiss = findGroupOrCel((String) parameters.elementAt(0),event) ;
            EventHandler.setModal(kiss);
            break ;

         // Return our modal object. This is an integer if we have a modal
         // group object, or the cel name if we have a modal cel object.
         // If we are not modal we return -1.

         case 141:	// "letmodal(variable)"
            if (parameters.size() < 1) break ;
            s = (String) parameters.elementAt(0) ;
            o = EventHandler.getModal() ;
            if (o instanceof Group) o = ((Group) o).getIdentifier() ;
            if (o instanceof Cel) o = ((Cel) o).getRelativeName() ;
            if (o instanceof Integer)
               variable.setIntValue(s,((Integer) o).intValue(),event) ;
            else if (o instanceof String)
               variable.setValue(s,o,event) ;
            else
               variable.setIntValue(s,-1,event) ;
            break ;

         // Fire an existing event.

         case 142:	// "event(eventname,param1,param2,...)"
            if (config == null) break ;
            if (parameters.size() == 0) break ;
            EventHandler handler = config.getEventHandler() ;
            if (handler == null) break ;
            o1 = evaluateParam((String) parameters.elementAt(0)) ;
            Vector events = handler.getEvent(o1) ;
            if (events == null) break ;

            // Find all required events.  The event signature, excluding local
            // variables, must match the parameters on this action statement.

            for (i = 0 ; i < events.size() ; i++)
            {
               boolean signaturematch = true ;
               FKissEvent event = (FKissEvent) events.elementAt(i) ;
               Vector eventparameters = event.getParameters() ;
               if (eventparameters == null) continue ;
               if (parameters.size()-1 > eventparameters.size()) continue ;
               for (int j = 0 ; j < eventparameters.size() ; j++)
               {
                  s = (String) eventparameters.elementAt(j) ;
                  s = Variable.getStringLiteralValue(s) ;
                  if (s.startsWith("@")) break ;
                  o = evaluateParam((String) parameters.elementAt(j+1)) ;
                  if (!(o instanceof String))
                     { signaturematch = false ; break ; }
                  if (!s.equalsIgnoreCase(o.toString()))
                     { signaturematch = false ; break ; }
               }
               if (signaturematch)
                  EventHandler.queueEvent(event,Thread.currentThread(),this);
            }
            break ;

         // Paint the current screen.

         case 143:	// "paint([delay])"
            if (panel == null) break ;
            n1 = (parameters.size() == 0) ? 0 :
               variable.getIntValue((String) parameters.elementAt(0),event) ;
            panel.setRedraw(true) ;
            panel.redraw(new Rectangle()) ;
            if (n1 == 0) break ;
            try { Thread.currentThread().sleep(n1) ; }
            catch (InterruptedException e) { }
            break ;

         // Wait for a synchronization lock.

         case 144:	// "wait(variable[,delay])"
            if (parameters.size() < 1) break ;
            Object lock = variable.getValue((String) parameters.elementAt(0),event) ;
            if (lock == null) break ;
            n1 = (parameters.size() < 2) ? 0 :
               variable.getIntValue((String) parameters.elementAt(1),event) ;
            try
            {
               synchronized (lock)
               {
                  if (n1 > 0)
                     lock.wait(n1) ;
                  else
                     lock.wait() ;
               }
            }
            catch (InterruptedException e) { }
            break ;

         // Notify any queued activities.

         case 145:	// "signal(variable)"
            if (parameters.size() < 1) break ;
            lock = variable.getValue((String) parameters.elementAt(0),event) ;
            if (lock == null) break ;
            synchronized (lock) { lock.notifyAll() ; }
            break ;

         // Sleep for a bit.

         case 146:	// "sleep(delay)"
            if (parameters.size() < 1) break ;
            n1 = variable.getIntValue((String) parameters.elementAt(0),event) ;
            try { Thread.currentThread().sleep(n1) ; }
            catch (InterruptedException e) { }
            break ;

         // Force a mouse release.

         case 147:	// "mouseRelease()"
            if (panel == null) break ;
            panel.releaseMouse(true) ;
            break ;

         // Return the square root of a number. A floating point value is set
         // only if the number is floating point.

         case 148:	// "sqrt()"
            if (parameters.size() < 2) break ;
            n1 = variable.getConvertType((String) parameters.elementAt(1),event) ;
            d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
            d2 = Math.sqrt(d1) ;
            if (n1 == 3)
               variable.setDoubleValue((String) parameters.elementAt(0),d2,event) ;
            else
               variable.setIntValue((String) parameters.elementAt(0),(int) d2,event) ;
            break ;
            
         // Establish a default property pool.

         case 150:	// "valuepool(poolname)"
            if (parameters.size() < 1) break ;
            if (config == null) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if (o1 == null) break ;
            config.setPool(o1.toString()) ;
            break ;
            
         // Load a value from a variable pool.

         case 151:	// "loadvalue(variable,poolname.property)"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            if (o1 == null) break ;
            o2 = getPoolValue(o1.toString()) ;
            variable.setValue((String) parameters.elementAt(0),o2,event) ;
            break ;
            
         // Save a value in a variable pool.

         case 152:	// "savevalue(poolname.property,value)"
            if (parameters.size() < 2) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if (o1 == null) break ;
            o2 = variable.getValue((String) parameters.elementAt(1),event) ;
            setPoolValue(o1.toString(),o2) ;
            break ;
            
         // Delete a value from a variable pool.

         case 153:	// "deletevalue(property)"
            if (parameters.size() < 1) break ;
            o1 = variable.getValue((String) parameters.elementAt(0),event) ;
            if (o1 == null) break ;
            setPoolValue(o1.toString(),null) ;
            break ;

         // Exitloop command.

         case 154:	// "exitloop"
            if (event.isRepeating()) exception = "exitloop" ;
            break ;

         // letmaxpage command.

         case 155:	// "letmaxpage(variable)"
            if (parameters.size() < 1) break ;
            if (config == null) break ;
            n1 = config.getPageCount() ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // letmaxcolor command.

         case 156:	// "letmaxcolor(variable)"
            if (parameters.size() < 1) break ;
            n1 = 0 ;
            n2 = 0 ;
            while (config != null)
            {
               Palette p1 = (Palette) Palette.getByKey(Palette.getKeyTable(),config.getID(),new Integer(n2++)) ;
               if (p1 == null) break ;
               int multipalettes = p1.getMultiPaletteCount() ;
               if (multipalettes > n1) n1 = multipalettes ;
            }
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // Return the math function of a number. A floating point value is set
         // only if the result is floating point.

         case 158:	// "math(variable,function,operand1,operand2,...)"
            if (parameters.size() < 2) break ;
            double d0 = 0 ; d1 = 0 ; d2 = 0 ;
            o1 = variable.getValue((String) parameters.elementAt(1),event) ;
            s1 = (o1 != null) ? o1.toString().toLowerCase() : "" ;
            n1 = variable.getType((String) parameters.elementAt(2),event) ;
            if (parameters.size() > 2) 
               d1 = variable.getDoubleValue((String) parameters.elementAt(2),event) ;
            if (parameters.size() > 3) 
               d2 = variable.getDoubleValue((String) parameters.elementAt(3),event) ;
            if ("abs".equals(s1)) d0 = Math.abs(d1) ;
            else if ("acos".equals(s1)) d0 = Math.acos(d1) ;
            else if ("asin".equals(s1)) d0 = Math.asin(d1) ;
            else if ("atan".equals(s1)) d0 = Math.atan(d1) ;
            else if ("ceil".equals(s1)) d0 = Math.ceil(d1) ;
            else if ("cos".equals(s1)) d0 = Math.cos(d1) ;
            else if ("exp".equals(s1)) d0 = Math.exp(d1) ;
            else if ("floor".equals(s1)) d0 = Math.floor(d1) ;
            else if ("log".equals(s1)) d0 = Math.log(d1) ;
            else if ("max".equals(s1)) d0 = Math.max(d1,d2) ;
            else if ("min".equals(s1)) d0 = Math.min(d1,d2) ;
            else if ("pow".equals(s1)) d0 = Math.pow(d1,d2) ;
            else if ("random".equals(s1)) d0 = Math.random() ;
            else if ("rint".equals(s1)) d0 = Math.rint(d1) ;
            else if ("round".equals(s1)) d0 = Math.round(d1) ;
            else if ("sin".equals(s1)) d0 = Math.sin(d1) ;
            else if ("sqrt".equals(s1)) d0 = Math.sqrt(d1) ;
            else if ("tan".equals(s1)) d0 = Math.tan(d1) ;
            else if ("todegrees".equals(s1)) d0 = Math.toDegrees(d1) ;
            else if ("toradians".equals(s1)) d0 = Math.toRadians(d1) ;
            else break ;
            n1 = (int) d0 ;
            if (n1 != d0)
               variable.setDoubleValue((String) parameters.elementAt(0),d0,event) ;
            else
               variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break ;

         // Format a decimal number command.

         case 159:	// "format(S,Number,maxfraction,minfraction,maxint,minint)"
            if (parameters.size() < 2) break ;
            n1 = n2 = n3 = n4 = -1 ;
            if (parameters.size() > 2) 
               n1 = variable.getIntValue((String) parameters.elementAt(2),event) ;
            if (parameters.size() > 3) 
               n2 = variable.getIntValue((String) parameters.elementAt(3),event) ;
            if (parameters.size() > 4) 
               n3 = variable.getIntValue((String) parameters.elementAt(4),event) ;
            if (parameters.size() > 5) 
               n4 = variable.getIntValue((String) parameters.elementAt(5),event) ;
            d1 = variable.getDoubleValue((String) parameters.elementAt(1),event) ;
            NumberFormat nf = NumberFormat.getInstance() ;
            nf.setGroupingUsed(false) ;
            if (n1 >= 0) nf.setMaximumFractionDigits(n1) ;
            if (n2 >= 0) nf.setMinimumFractionDigits(n2) ;
            if (n3 >= 0) nf.setMaximumIntegerDigits(n3) ;
            if (n4 >= 0) nf.setMinimumIntegerDigits(n4) ;
            o = nf.format(d1) ;
            variable.setValue((String) parameters.elementAt(0),o,event) ;
            break ;
            

         // letcloned command.

         case 160:	// "letcloned(variable,object)"
            if (parameters.size() < 2) break ;
            kiss = findGroupOrCel((String) parameters.elementAt(1),event) ;
            if (!(kiss instanceof Group)) break ;
            o1 = kiss.getClone() ;
            o2 = (o1 instanceof Group) ? ((Group) o1).getIdentifier() : null ;
            n1 = (o2 instanceof Integer) ? ((Integer) o2).intValue() : -1 ;
            variable.setIntValue((String) parameters.elementAt(0),n1,event) ;
            break;
         }
      }

      // Catch execution time exceptions.  Show the error message in
      // the panel frame status area.

      catch (ArithmeticException e)
      {
         exception = "overflow" ;
      }

      catch (StackOverflowError e)
      {
         long depth = event.getDepth() ;
         s = "Event action stack overflow at depth " + depth + "\nLine [" + line + "] " + toString() ;
         if (e.getMessage() == null) e = new StackOverflowError(s) ;
         throw e ;
      }

      catch (SecurityException e)
      {
         s = "Event action security exception " + toString() ;
         System.out.println("Line [" + line + "] " + s) ;
         if (panel != null) panel.showStatus(s) ;
      }

      catch (Exception e)
      {
         s = "Exception, Event action: " + toString() ;
         System.out.println("Line [" + line + "] " + s) ;
         if (panel != null) panel.showStatus(s) ;
         e.printStackTrace() ;
      }

      // Return a null bounding box as the default.  We also return our
      // loop exception code.

      this.box = box ;
      Object [] result = new Object[4] ;
      result[0] = box ;
      result[1] = exception ;
      result[2] = exceptionitem ;
      result[3] = new Long(actionsprocessed + 1) ;
      runtime += (System.currentTimeMillis() - starttime) ;  // Java 1.5
      invocations++ ;
//      Thread.currentThread().yield() ;
      return result ;
   }


   // Constrain the movement to the panel frame boundaries, given
   // that Rectangle r defines our current location and bounding box
   // size, and the pair (x,y) are the move offsets.  We return a
   // new Point with movement offsets set to fit within the frame
   // boundaries. 

   private Point constrain(int x, int y, Rectangle r, Dimension s, Point rx, Point ry)
   {
      if ((x + r.x + r.width) > s.width)
         x = s.width - r.x - r.width ;
      if ((x + r.x) < 0)
         x = 0 - r.x ;
      if ((y + r.y + r.height) > s.height)
         y = s.height - r.y - r.height ;
      if ((y + r.y) < 0)
         y = 0 - r.y ;
      return new Point(x,y) ;
   }


   // Function to parse an event Group or Cel or Cel Group parameter.

   private KissObject findGroupOrCel(String s, FKissEvent event)
   {
      Object o = Group.findGroup(s,config,event) ;
      if (o == null) o = Cel.findCel(s,config,event) ;
      if (o == null) o = CelGroup.findCelGroup(s,config,event) ;
      if (o == null) o = variable.getValue(s.toUpperCase(),event) ;
      if (!(o instanceof KissObject)) return null ;
      return (KissObject) o ;
   }


   // Function to adjust a cel location offset due to a cel relocation.
   // Cels are relocated if cels are moved to a new location.  The new
   // offset is calculated as a displacement from the group location.

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
      // We recompute a new group offset from its original location.

      g.updateBoundingBox() ;
      Rectangle r = g.getBoundingBox() ;
      x = -(grouplocation.x - r.x) ;
      y = -(grouplocation.y - r.y) ;
      g.setOffset(new Point(x,y)) ;

      // Calculate the new relocated cel offset for all other cels in the group.

      Vector cels = g.getCels() ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (c == null) continue ;
         cellocation = c.getLocation() ;
         celoffset = c.getOffset() ;
         grouplocation = g.getLocation() ;
         x = (cellocation.x + celoffset.x) - grouplocation.x ;
         y = (cellocation.y + celoffset.y) - grouplocation.y ;
         c.setOffset(x,y) ;
      }

      // Rebuild the group bounding box to eliminate any group offset.

      g.rebuildBoundingBox() ;
      g.eliminateOffset() ;
   }


   // Function to set the event skip actions state.  We pass a reusable
   // buffer to the event to maximize run time performance.

   private void setSkipActions(boolean b, String context, Thread t)
   { setSkipActions(b,context,event.getIfLevel(),t) ; }

   private void setSkipActions(boolean b, String context, int level, Thread t)
   {
      if (skip == null) skip = new Object[3] ;
      skip[0] = new Boolean(b) ;
      skip[1] = context ;
      skip[2] = new Integer(level) ;
      event.setSkipActions(t,skip) ;
   }


   // Function to evaluate a KiSS object key parameter.  If the parameter
   // is a variable then this function returns the variable value, otherwise
   // it returns the actual parameter.

   private String evaluateParam(String s)
   {
      if (variable == null) return s ;
      Object o = variable.getValue(s,event) ;
      String value = (o != null) ? o.toString() : s ;
      return value ;
   }


   // Establish any alarm arguments.  These are provided on
   // the timer statement and referenced as local parameters
   // by the alarm event code. We have to be careful and
   // retain argument values before setting parameters.
   // Names might match and we could modify values.

   private void setAlarmArguments(Alarm alarm, Vector parameters, int n)
   {
      if (alarm == null) return ;
      if (parameters == null) return ;
      Vector alarms = alarm.getEvent("alarm") ;
      Enumeration enum1 = (alarms != null) ? alarms.elements() : null ;
      while (enum1 != null && enum1.hasMoreElements())
      {
         Vector argvalues = new Vector() ;
         FKissEvent alarmevent = (FKissEvent) enum1.nextElement() ;
         Vector arguments = alarmevent.getParameters() ;
         for (int i = n ; i < parameters.size() ; i++)
         {
            Object o = variable.getValue((String) parameters.elementAt(i),event) ;
            argvalues.addElement(o) ;
         }
         for (int i = n ; i < parameters.size() ; i++)
         {
            if (i-(n-1) < arguments.size())
            {
               String s = (String) arguments.elementAt(i-(n-1)) ;
               if (!s.startsWith("@")) s = "@" + s ;
               alarmevent.setDepth(alarmevent.getDepth()+1) ;
               variable.setValue(s,argvalues.elementAt(i-n),alarmevent) ;
               alarmevent.setDepth(alarmevent.getDepth()-1) ;
            }
         }
      }
   }


   // Function to translate cursor control key names into special characters.

   private String translateKey(String s)
   {
      char c1 = 0xF0 ;
      char c2 = 0xF1 ;
      char c3 = 0xF2 ;
      char c4 = 0xF3 ;

      if (s == null) return "" ;
      s = s.toUpperCase() ;
      int i = s.indexOf("UP") ;
      if (i >= 0) s = s.substring(0,i) + c1 + s.substring(i+2) ;
      i = s.indexOf("DOWN") ;
      if (i >= 0) s = s.substring(0,i) + c2 + s.substring(i+4) ;
      i = s.indexOf("LEFT") ;
      if (i >= 0) s = s.substring(0,i) + c3 + s.substring(i+4) ;
      i = s.indexOf("RIGHT") ;
      if (i >= 0) s = s.substring(0,i) + c4 + s.substring(i+5) ;
      return s ;
   }
   
   
   // Function to get a variable pool value.
   
   private String getPoolValue(String pool)
   {
      if (pool == null) return "";
      if (config == null) return "" ;
      pool = pool.trim() ;
      int n = pool.indexOf('.') ;
      String variable = pool ;
      String poolname = config.getPool() ;
      if (n >= 0) 
      {
         poolname = pool.substring(0,n) ;
         variable = pool.substring(n+1) ;
      }
      Properties p = config.findValuepoolProperties(poolname,config) ;
      if (p == null) return "" ;
      if (!OptionsDialog.getVariableCase()) variable = variable.toUpperCase() ;
      return p.getProperty(variable,"") ;
   }
   
   
   // Function to set a variable pool value.
   
   private void setPoolValue(String pool, Object v)
   {
      if (pool == null) return;
      if (config == null) return ;
      pool = pool.trim() ;
      int n = pool.indexOf('.') ;
      String variable = pool ;
      String poolname = config.getPool() ;
      if (n >= 0) 
      {
         poolname = pool.substring(0,n) ;
         variable = pool.substring(n+1) ;
      }
      Properties p = config.findValuepoolProperties(poolname,config) ;
      if (p == null) return ;
      if (!OptionsDialog.getVariableCase()) variable = variable.toUpperCase() ;
      if (v == null) 
         p.remove(variable) ;
      else
         p.setProperty(variable,v.toString()) ;
      return ;
   }
 
	// Return the event cummulative execution time.

	long getRunTime() { return runtime ; }

	// Return the event execution count.

	long getRunCount() { return invocations ; }
  

   // Function to write the action command.

   int write(FileWriter fw, OutputStream out, String type) throws IOException
   {
      String s = toString() ;
      byte [] b = s.getBytes() ;
      out.write(b) ;
      if (fw != null) fw.updateProgress(b.length) ;
      return b.length ;
   }


   // The getName method returns a string representation of this event.
   // This is the action name and parameter list.

   String getName() { return toStringComment(false,false) ; }


   // The toString method returns a string representation of this action.
   // This is the action name, parameter list, and comment string.

   public String toString() { return toStringComment(true,true) ; }


   // The toStringComment method builds a string representation of this
   // action, optionally including the comment text.  Leading spaces
   // may exist to show event indentation level output.

   String toStringComment(boolean showcomment, boolean indent)
   {
      if (identifier == null) return "" ;
      StringBuffer sb = new StringBuffer() ;
      if (indent) 
         for (int i = 0 ; i < indentlevel ; i++) sb.append(' ') ;
      sb.append(identifier) ;
      sb.append('(') ;
      if (parameters != null)
      {
         for (int i = 0 ; i < parameters.size() ; i++)
         {
         	sb.append(translateName(parameters.elementAt(i))) ;
            if (i < parameters.size()-1) sb.append(',') ;
         }
      }
      sb.append(')') ;
      if (showcomment && comment != null) sb.append(" ; " + comment) ;
      return sb.toString() ;
   }
   
   
   // Method to translate component literal names for the case when
   // components were renamed.
   
   private Object translateName(Object o)
   {
      if (config == null) return o ;
      if (!(o instanceof String)) return o ;
      String s = o.toString() ;
      if (s.length() == 0) return s ;
      
      // See if we have a literal name.

      String s1 = s ;
      boolean literal = false ;
      if (s.charAt(0) == '\"')
      {
         int i = s1.lastIndexOf('\"') ;
         if (i < 1) return s1 ;
         s1 = s1.substring(1,i) ;
         literal = true ;
      }
      
      // Convert the name to the write name.

      if (!(ArchiveFile.isComponent(s1) || ArchiveFile.isImage(s1))) return s ;
      Cel cel = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),s1.toUpperCase()) ;
      if (cel == null) return s ;
      if (!cel.isWritable()) return s ;
      s1 = cel.getWriteName() ;
      if (literal) s1 = "\"" + s1 + "\"" ;
      return s1 ;
   }
}
