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
* FKiSS Event class
*
* Purpose:
*
* This class represents an FKiSS event.  An event object is a storage
* container to hold a series of action commands that must occur when a
* recognized event happens.
*
* When an event is fired all actions associated with the event are
* performed.  The KiSS specification does not define a sequence for
* the action order.  We perform actions sequentially, in the order
* in which they were defined.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.Rectangle ;
import java.awt.Graphics ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.util.NoSuchElementException ;
import javax.swing.SwingUtilities ;

final class FKissEvent extends KissObject
	implements Comparable
{
   private static Hashtable debugframe = new Hashtable() ; // Active debugging frame
   private static FKissFrame breakframe = null ;  // Debug frame for pause callback
	private static boolean enabled = true ;		  // True if event processing enabled
	private static boolean breakpause = false ;	  // True if auto pause execution

   // Breakpoint attributes

   private boolean breakenabled = false ; // True if breakpoint process enabled
   private boolean stepbreak = false ;    // True if breakpoint single step
   private boolean enterbreak = false ;   // True if breakpoint step into
   private boolean returnbreak = false ;  // True if breakpoint on return
   private boolean timerbreak = false ;   // True if breakpoint step into timer
   private Object runtobreak = null ;     // Action statemt to breakpoint on
   private FKissEvent breakevent = null ; // Retains current event being traced
   private Object topbreak = null ;       // The top level event being traced
   private Object queue = new Object() ;  // Breakpoint synchronization object
	private boolean breakpoint = false ; 	// True if breakpoint on this event
	private boolean interimbreak = false ; // True if interim breakpoint set
	private boolean nobreakpoint = false ; // True if not to break on this event
	private boolean currentbreak = false ; // Set if this is current breakpoint

	// Event attributes

	private String identifier = null ;	   // Event identifier
	private Vector parameters = null ;	   // Event parameters
	private Vector visibility = null ;	   // Parameter visibility
	private Vector actions = null ;		   // Action list for the event
	private Vector alarmlist = null ;	   // Alarms keyed during event actions
   private Vector objectfired = null ;    // Object fired during collisions
   private KissObject parent = null ;     // Parent object to this event
   private Configuration config = null ;  // Configuration object
	private Object cid = null ;			   // Configuration object id
	private Rectangle box = null ;		   // Event drawing bounding box
	private FKissAction action = null ;    // Current action being processed
   private Variable variable = null ;		// Our variable storage
	private String comment = null ;			// Event comment text
	private String exception = null ;		// Event exception text
   private Thread currentthread = null ;  // Current execution thread
   private Object returnvalue = null ;    // Event return value
   private Object [] collide = null ;     // Actual collision objects
   private Object [] collision1 = null ;  // Collision cel packet 1
   private Object [] collision2 = null ;  // Collision cel packet 2
   private Thread firethread = null ;     // Thread from fireEvent call
   private Object firesource = null ;     // Source from fireEvent call
	private boolean state = false ;		   // Action state for the event
	private boolean terminate = false ; 	// Execution termination flag
	private boolean temporary = false ; 	// Set if temporary variables exist
	private boolean confirmwait = false ;  // Set if confirm dialog shown
	private boolean repeating = false ;    // Set if event is repeat() target
	private boolean busy = false ;         // Set if event is being fired
	private long createtime = 0 ;			   // Configuration activation time
	private long starttime = 0 ;			   // Event start time
	private long runtime = 0 ;				   // Event cummulative run time
	private long invocations = 0 ;	 	   // Event run count
	private long depth = 0 ;	 	         // Event recursion depth
	private long totalactions = 0 ;	 	   // Total action statements processed
	private long actionsprocessed = 0 ;	 	// Action statements processed
	private int repeatlimit = 0 ;	   	   // Count for repeat() statement
	private int iflevel = 0 ;				   // Current nest level for skip
	private int elseiflevel = 0 ;				// Current elseif nest level for skip

	// Event thread table for tracking the action skip state by thread
   // Event state table for maintaining event action collision states
   // Variable table for maintaining local event variables

	private Hashtable key = new Hashtable(100,0.8f) ;
	private Hashtable eventstate = new Hashtable(100,0.8f) ;
   private Hashtable variabledepth = new Hashtable(100,0.8f) ;



	// Constructor

	public FKissEvent(String id, Configuration c)
	{
      config = c ;
		cid = c.getID() ;
		identifier = id ;
		alarmlist = new Vector() ;
      parameters = new Vector() ;
      visibility = new Vector() ;
      objectfired = new Vector() ;
      variable = c.getVariable() ;
	}
   
   
   // Initialization of the event object for a restart.
   
   void init()
   {
      alarmlist = new Vector() ;
      terminate = false ;
   }


	// Object state change methods
	// ---------------------------

	// Set the object identifier.

	void setIdentifier(Object id)
   { identifier = (id != null) ? id.toString() : null ; }

	void addParameters(Vector v) 
   { 
      parameters = (v != null) ? v : new Vector() ;
      visibility = new Vector() ;
      for (int i = 0 ; i < parameters.size() ; i++)
         visibility.addElement(Boolean.TRUE) ;
   }

	// The action vector holds all the action objects for this event.

	void addActions(Vector v) { actions = v ; }

	// Events can be enabled or disabled.  This is a global variable for the
	// FKiSS event class so if any event is disabled no event will fire.

	static void setEnabled(boolean b) { enabled = b ; }

	// The event state is used to save initial object collision states
	// between mouse down and mouse up events.

	void setState(boolean b) { state = b ; }

	// The event breakpoint, if set, is used to invoke the FKiSS Editor when
	// this event is fired.

	void setBreakpoint(boolean b)
   {
      breakpoint = b ;
      nobreakpoint = false ;
      interimbreak = false ;

      // Ensure that all event actions can breakpoint.

      if (actions == null) return ;
      for (int i = 0 ; i < actions.size() ; i++)
      {
         FKissAction a = (FKissAction) actions.elementAt(i) ;
         a.setNoBreakpoint(false) ;
      }
   }

	// An interim breakpoint, if set, is used to invoke the FKiSS Editor when
	// this event is fired.  Interim breakpoints are cleared on first use.

	void setInterimBreakpoint(FKissFrame df, boolean b)
   {
      interimbreak = b ;
      breakframe = df ;
   }

	// The no action breakpoint, if set, does not allow this event to
   // break normal processing.  The event nobreakpoint is propagated
   // to all event actions.

	void setNoBreakpoint(boolean b)
   {
      nobreakpoint = b ;
      breakpoint = false ;
      interimbreak = false ;

      // Propagate the nobreakpoint setting to all actions.

      if (actions == null) return ;
      for (int i = 0 ; i < actions.size() ; i++)
      {
         FKissAction a = (FKissAction) actions.elementAt(i) ;
         a.setNoBreakpoint(b) ;
      }
   }

	// The breakpoint pause, if set, will interrupt current execution when
   // the next action is processed.  The specified FKiSS Editor is invoked
   // if it is visible.  This is a static entry that is recognized across
   // all concurrent event handler activities.

	static void setBreakPause(FKissFrame df, boolean b)
   {
      breakpause = b ;
      breakframe = df ;
   }

	// Get the global breakpoint pause state.

	static boolean getBreakPause() { return breakpause ; }

	// Get the global breakpoint FKiSS Editor frame.

	static FKissFrame getBreakFrame() { return breakframe ; }

	// The current breakpoint, if set, indicates that this event is the
   // currently active breakpoint event.  This is used to ensure that
   // called subroutines are not traced on a step breakpoint.

	void setCurrentBreak(boolean b) { currentbreak = b ; }

	// Return the breakpoint state indicators.

	boolean isBreakStep() { return (breakenabled && stepbreak) ; }
	boolean isBreakEnter() { return (breakenabled && enterbreak) ; }
	boolean isBreakRunToEnd() { return (breakenabled && returnbreak) ; }
	boolean isBreakRunToSel() { return (breakenabled && runtobreak != null) ; }

	// The breakpoint state is set according to the supplied values.  The object
   // array contains the breakenabled, stepbreak, enterbreak, returnbreak,
   // timer break boolean values; the runtobreak object; and the topbreak event
   // that terminates breakpoint tracing.

	void setBreakState(Object [] buffer)
   {
      Object o = (buffer != null && buffer.length > 0) ? buffer[0] : null ;
      breakenabled = (o instanceof Boolean) ? ((Boolean) o).booleanValue() : false ;
      o = (buffer != null && buffer.length > 1) ? buffer[1] : null ;
      stepbreak = (o instanceof Boolean) ? ((Boolean) o).booleanValue() : false ;
      o = (buffer != null && buffer.length > 2) ? buffer[2] : null ;
      enterbreak = (o instanceof Boolean) ? ((Boolean) o).booleanValue() : false ;
      o = (buffer != null && buffer.length > 3) ? buffer[3] : null ;
      returnbreak = (o instanceof Boolean) ? ((Boolean) o).booleanValue() : false ;
      o = (buffer != null && buffer.length > 4) ? buffer[4] : null ;
      timerbreak = (o instanceof Boolean) ? ((Boolean) o).booleanValue() : false ;
      o = (buffer != null && buffer.length > 5) ? buffer[5] : null ;
      runtobreak = o ;
      o = (buffer != null && buffer.length > 6) ? buffer[6] : null ;
      topbreak = o ;
   }

	// The breakpoint entry state is returned.

	Object [] getBreakState()
   {
      Object [] o = new Object [7] ;
      o[0] = new Boolean(breakenabled) ;
      o[1] = new Boolean(stepbreak) ;
      o[2] = new Boolean(enterbreak) ;
      o[3] = new Boolean(returnbreak) ;
      o[4] = new Boolean(timerbreak) ;
      o[5] = runtobreak ;
      o[6] = topbreak ;
      return o ;
   }

	// The line number is the configuration file line number where this
	// event was declared.  It is used for diagnostic output messages.

	void setLine(int n) { line = n ; }
   
	// The parent object to which this event is associated.

	void setParentObject(KissObject o) { this.parent = o ; }

	// The temporary variable indicator is set by the Variable routine if
   // this event creates temporary variables during execution.

	void setTemporary(boolean b)
   {
      temporary = b ;
      if (temporary)
      {
         Hashtable variable = (Hashtable) variabledepth.get(new Long(depth)) ;
         if (variable == null)
         {
            variable = new Hashtable(220,0.8f) ;
            variabledepth.put(new Long(depth),variable) ;
         }
      }
      else
      {
         variabledepth.remove(new Long(depth)) ;
      }
   }

	// The nest level is the current nesting for if/else/endif statements.
	// The skip logic matches the action command to the skip level context.

	void setIfLevel(int level) { this.iflevel = level ; }

	// The elseif level is the current nesting for if/elseif/endif statements.
	// The elseif count is used to establist the endif nest level.

	void setElseIfLevel(int level) { this.elseiflevel = level ; }

	// Set the object comment text.

	void setComment(String s) { comment = s ; }

	// Set our confirm dialog wait state.

	void setConfirmWait(boolean b) { confirmwait = b ; }

	// Actions can be disabled if we are processing logical commands
	// and the logic condition failed.  As this event can be running
	// in multiple activities we must retain individual action controls
	// by thread name.  We retain the skip context (if, while, for) and
	// the current nest level.  The data buffer contains a boolean, context
   // string, and a level number.  The key is the current thread.

	void setSkipActions(Object thread, Object o)
	{
      if (thread == null) return ;
      if (o == null)
         key.remove(thread) ;
      else
		   key.put(thread,o) ;
   }

	// Set the thread action skip level.  This level determines
	// the nesting level that the action command references.

	void setSkipLevel(Object thread, int level)
	{
		Object [] o = (Object []) key.get(thread) ;
		if (o == null) return ;
      o[2] = new Integer(level) ;
	}

	// Alarms set through timer actions are initially disabled.  Alarms must
	// be enabled when this event terminates.  This method adds an alarm
	// reference or a set of alarm references to the alarm enable list.

	void setAlarmEnable(Vector v)
	{
		if (v == null) return ;
		if (v != alarmlist)
		{
			alarmlist.addAll(v) ;
			v.removeAllElements() ;
		}
	}

	void setAlarmEnable(Object a)
	{ 
      if (a != null) alarmlist.addElement(a) ; 
   }

	// Set the event start time.  This is the time the event begins execution
	// and it is used in timer actions to reduce the alarm delay to compensate
	// for the actual event processing time.

	void setStartTime(long t) { starttime = t ; }
	
   // Set the event repeating state.  This is true if this event is the
   // target of a 'repeat()' action statement.

	void setRepeating(boolean b) { repeating = b ; }
	
   // Set the repeat() statement iteration limit.  This is valid if this event
   // is the target of a 'repeat()' action statement.

	void setRepeatLimit(int n) { repeatlimit = n ; }

   // Clear the skip table.

   void clearSkip(Object o) { key.remove(o) ; }

   // Set the collision objects for a collision event.

   void setCollide(Object [] o) { collide = o ; }

   // Set the collision cel list for checkTouch processing.

   void setCollision1(Object [] o) { collision1 = o ; }
   void setCollision2(Object [] o) { collision2 = o ; }

   // Set the parameter visibility for checkTouch processing.

   void setVisible(boolean b, String s) 
   { 
      if (parameters == null) return ;
      for (int i = 0 ; i < parameters.size() ; i++)
      {
         String s1 = (String) parameters.elementAt(i) ;
         if (s1.charAt(0) == '\"')
         {
            int j = s1.lastIndexOf('\"') ;
            if (j > 0) s1 = s1.substring(1,j) ;
         }
         if (!s.equalsIgnoreCase(s1)) continue ;
         visibility.setElementAt(((b) ? Boolean.TRUE : Boolean.FALSE),i) ;
      }
   }


	// Object state reference methods
	// ------------------------------

	// Return the object identifier.

	Object getIdentifier() { return identifier ; }

	// Return the event source code line number.

	int getLine() { return line ; }
   
	// Return the object to which this event is associated.

	KissObject getParentObject() { return parent ; }

	// Return the recursion depth level.

	long getDepth() { return depth ; }

	// Set the recursion depth level.

	void setDepth(long n) { depth = n ; }

	// Return the current event if/else/endif skip nest level.

	int getIfLevel() { return iflevel ; }

	// Return the current event if/elseif/endif nest level.

	int getElseIfLevel() { return elseiflevel ; }

	// Return the object comment text.

	String getComment() { return comment ; }

	// The temporary variable indicator is set by the Variable routine if
   // this event creates temporary variables during execution.

	boolean getTemporary() { return temporary ; }

	// Return the local event variable table.

	Hashtable getVariableTable()
   {
      return (Hashtable) variabledepth.get(new Long(depth)) ;
   }

	// Return the event action state.  The event state is used for
	// collide, apart, in and out events and maintains the active object
	// collision state across mouse down and mouse up events.

	boolean getState() { return state ; }

	// Return the event breakpoint switch.  If a breakpoint is set then
	// the FKiSS Editor will be invoked when this event is fired.

	boolean getBreakpoint() { return breakpoint || interimbreak ; }

	// Return the interim breakpoint switch.  This is a one time breakpoint
   // that is specific to an FKiSS Editor frame.

	boolean getInterimBreakpoint()
   { return getInterimBreakpoint(breakframe) ; }

	boolean getInterimBreakpoint(FKissFrame f)
   { return (breakframe == f) ? interimbreak : false ; }

	// Return the no breakpoint switch.  If the no breakpoint switch is set
   // then this event cannot initiate a breakpoint interrupt.

	boolean getNoBreakpoint() { return nobreakpoint ; }

	// Return the current breakpoint indicator.  If we are breakpoint
	// processing and stopped at this command this value is true.

	boolean isCurrentBreak() { return currentbreak ; }

	// Return the current repeat target indicator.  If we are repeating
	// this event as a target of a repeat() action this value is true.

	boolean isRepeating() { return repeating ; }

	// Return the busy indicator.  This is set while the event is fired.

	boolean isBusy() { return busy ; }

	// Return the current parameter visibility for collision processing.  
   
	boolean isVisible() 
   { 
      for (int i = 0 ; i < visibility.size() ; i++)
         if (!((Boolean) visibility.elementAt(i)).booleanValue()) return false ;
      return true ;
   }

	// Return the current repeat iteration limit.  This is valid if we are 
	// repeating this event as a target of a repeat() action.

	int getRepeatLimit() { return repeatlimit ; }

	// Return the event start time.  The start time is the time when the
	// event is fired.  This is set when the event is selected from the
	// EventHandler queue.

	long getStartTime() { return starttime ; }

	// Return the event object creation time.

	long getCreateTime() { return createtime ; }

	// Return the event cummulative execution time.

	long getRunTime() { return runtime ; }

	// Return the event execution count.

	long getRunCount() { return invocations ; }

	// Return the total event action statement count.

	long getTotalActionCount() { return totalactions ; }

	// Return the total event action statement count.

	long getActionsProcessed() { return actionsprocessed ; }

	// Return our confirm dialog wait state.

	boolean getConfirmWait() { return confirmwait ; }

	// Return the thread action skip state.  This state is set through event
	// actions and is maintained in the thread control table keyed on our
	// current thread object.

	boolean getSkip(Object thread)
	{
		Object [] o = (Object []) key.get(thread) ;
		if (o == null) return false ;
		Boolean b = (Boolean) o[0] ;
		if (b == null) return false ;
		return b.booleanValue() ;
	}

	// Return the thread action skip context.  This context determines
	// the action command (if, while, for) that the skip references.

	String getSkipContext(Object thread)
	{
		Object [] o = (Object []) key.get(thread) ;
		if (o == null) return null ;
		return ((String) o[1]) ;
	}

	// Return the thread action skip object. We return a clone of the object.
   // The skip object contains three items.  A Boolean, for skip or no skip,
   // a String for the command context, and an Integer for the level.

	Object getSkipActions(Object thread)
	{
      if (thread == null) return null ;
		Object [] o = (Object []) key.get(thread) ;
      if (o == null) return null ;
      Object [] o1 = new Object[3] ;
		Boolean b = (Boolean) o[0] ;
      String s = (String) o[1] ;
		Integer n = (Integer) o[2] ;
		if (b == null) b = new Boolean(false) ;
      if (s == null) s = new String("") ;
      if (n == null) n = new Integer(0) ;
      o1[0] = new Boolean(b.booleanValue()) ;
      o1[1] = new String(s) ;
      o1[2] = new Integer(n.intValue()) ;
      return o1 ;
	}

	// Return the thread action skip level.  This level determines
	// the nesting level that the action command references.

	int getSkipLevel(Object thread)
	{
		Object [] o = (Object []) key.get(thread) ;
		if (o == null) return 0 ;
		if (!(o[2] instanceof Integer)) return 0 ;
		return ((Integer) o[2]).intValue() ;
	}

	// Return the event bounding box.  This is the rectangle that describes
   // the union of all action areas in the event.

	Rectangle getBoundingBox() { return box ; }

   // Return the pending alarm activation list.  This is referenced upon
   // completion of a gosub action, which is a recursive label event, so
   // that any pending alarms can be propagated to the main event.

   Vector getAlarmList() { return alarmlist ; }

	// Return the event parameter list.

	Vector getParameters() { return parameters ; }

	// Return the event return value.

	Object getReturnValue() { return returnvalue ; }

	// Return the event exception string.

	String getEventException() { return exception ; }

	// Return the event collision state table.

	Hashtable getCollisionState() { return eventstate ; }

	// Return the first event parameter.

	String getFirstParameter()
	{ return (parameters.size() > 0) ? (String) parameters.elementAt(0) : null ; }

	// Set the first event parameter.

	void setFirstParameter(String s)
	{ if (parameters.size() > 0) parameters.setElementAt(s,0) ; }

	// Return the second event parameter.

	String getSecondParameter()
	{ return (parameters.size() > 1) ? (String) parameters.elementAt(1) : null ; }

	// Set the first event parameter.

	void setSecondParameter(String s)
	{ if (parameters.size() > 1) parameters.setElementAt(s,1) ; }

	// Return the event action list.

	Enumeration getActions()
	{
		if (actions == null || actions.size() == 0) return null ;

		// Construct the enumeration object.  This lists all actions in the
		// event.

		return new Enumeration()
		{
			private int i = 0;
			public boolean hasMoreElements() { return (i < actions.size()) ; }
			public Object nextElement() throws NoSuchElementException
			{
				if (i >= actions.size()) throw new NoSuchElementException() ;
				return (actions.elementAt(i++)) ;
			}
		} ;
	}

   // Return the count of the number of actions in this event.

   int getActionCount() { return (actions == null) ? 0 : actions.size() ; }

   // Return the action list vector.

   Vector getActionList() { return actions ; }

   // Return an array of the event actions.

   Object [] getActionArray() { return (actions == null) ? null : actions.toArray() ; }

	// Return the current action being processed.

	FKissAction getCurrentAction() { return action ; }

   // Return the collision objects for a collision event.

   Object [] getCollide() { return collide ; }

   // Return the collision cel list for checkTouch processing.

   Object [] getCollision1() { return collision1 ; }
   Object [] getCollision2() { return collision2 ; }

   // Required KissObject method to write a representation of this object.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
   {
   	String s = toString() ;
      byte [] b = s.getBytes() ;
   	out.write(b) ;
      if (fw != null) fw.updateProgress(b.length) ;
      return b.length ;
   }



	// Event firing method.  Event actions may change the visibility
	// of cels and this may require that the base graphics image be
	// updated.  The update will occur if this method is called
	// with our current panel frame reference passed as an argument.
	// The thread argument is used to associate this event to a specific
	// event thread.  The event thread is used to distinguish between user
	// initiated events and animation Timer events during Alarm processing.

	Rectangle fireEvent(final PanelFrame panel, final Thread thread, final Object source)
	{
      // If we are modal, only process events from the modal source.  Events
      // without a source such as keyboard events or set initialization events
      // or label events are processed.  Alarm events are also processed.
      // A modal group object accepts events from any contained cel.

      busy = true ;
      firethread = thread ;
      firesource = source ;
      Object modal = EventHandler.getModal() ;
      if (modal != null && source != null && !(source instanceof Alarm))
      {
         Object o = source ;
         if (modal instanceof Group && o instanceof Cel) o = ((Cel) o).getGroup() ;
         if (modal instanceof Cel && o instanceof Group) modal = ((Cel) modal).getGroup() ;
         if (modal != o)
         {
            Toolkit.getDefaultToolkit().beep() ;
            busy = false ;
            return null ;
         }
      }
      
      // If we are an alarm with a delay time of 0 then our alarm must have 
      // been stopped while this event was queued in the EventHandler.  Do not
      // process the alarm. Also, if the alarm time was changed while in the
      // EventHandler also do not process the alarm.
      
      if ("alarm".equals(getIdentifier()))
      {
         Object o = evaluateParam((String) getFirstParameter()) ;
         if (o instanceof String) o = ((String) o).toUpperCase() ;
         Alarm alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,o) ;
         if (alarm == null) return null ;
         alarm.setTriggerTime(0) ;
//    This allows only 1 alarm event to fire on a single alarm.
         if (!OptionsDialog.getMultipleEvents() || alarm.getInterval() == 0) 
         {
            if (alarm.getInterval() != Integer.MAX_VALUE) 
               return null ; 
         }
 			alarm.setInterval(-1,thread) ;
         AlarmTimer timer = config.getTimer() ;
         if (timer != null) timer.removeAlarm(alarm) ;
      }

      // Initialize for processing the event.
      
		int i ;
      this.box = null ;
  		Rectangle box = null ;
      boolean loop = true ;
		if (!enabled) { busy = false ; return null ; }

		FKissEvent event = this ;
		createtime = Configuration.getTimestamp() ;
		starttime = System.currentTimeMillis() ;
//		starttime = System.nanoTime() ;  // Java 1.5
      currentthread = Thread.currentThread() ;
      actionsprocessed = 0 ;
      iflevel = 0 ;
      depth++ ;

		// Under normal circumstances we process only the action statements
		// for this event.  However, if we process a goto action then we need
		// to switch our attention to a new label event object.   This loop
		// normally executes once except for the case of a goto which will
		// cause the loop to repeat with a new event object.

      try
      {
   		while (loop && event != null && !terminate)
   		{
            // Write the event trace to the log file. Note, event, action, 
            // and variable traces set as 'nobreakpoint' in the FKiSS editor 
            // have a sentinal '*' inserted in the text string. These trace 
            // lines are not forwarded to the trace dialog although they will 
            // appear in the log file.
      
   			if (OptionsDialog.getDebugEvent() && (!getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
   			{
               String bp = (event.getNoBreakpoint()) ? "*" : " " ;
   				long time = System.currentTimeMillis() - createtime ;
               if (!("label".equals(identifier)))
                  System.out.println("[" + time + "]"+bp+"[" + currentthread.getName() + "] FKissEvent begin event " + event.getName()) ;
   			}

            // If we have a breakpoint set on this event suspend processing
            // and invoke the FKiSS Editor.  Note, label events (gosub) will 
            // not have pause breakpoints unless an explicit breakpoint is set  
            // for the label event.

            if (event.getBreakpoint() || (breakpause && OptionsDialog.getEventPause()) && !("label".equals(getIdentifier())))
               if (!event.getNoBreakpoint())
                  doBreakpoint(event,panel,box,false) ;
            if (terminate) break ;

   			// Perform the event actions.  We take all the actions for this
   			// event and construct a consolidated bounding box, then draw
     			// once within the total bounding box area. We use sequence numbers
            // to track actions for this event.  Loop statements take us to a
            // new action in the event.

   			loop = false ;
            int sequence = 0 ;
            Rectangle actionbox = null ;
    			Object [] a = event.getActionArray() ;
    			while (a != null && sequence < a.length)
   			{
               if (EventHandler.getStop()) terminate = true ;
    				action = (FKissAction) a[sequence++] ;
               if (terminate) break ;

               // If we have a breakpoint set on this action suspend processing
               // and invoke the FKiSS Editor.  We do not trace skipped code.
               // The code is synchronized to ensure that multiple activities
               // do not simultaneously recognize a break pause.

               if (!(event.getSkip(currentthread)))
               {
                  synchronized (queue)
                  {
                     if (breakenabled)
                     {
                        if (stepbreak && event == breakevent)
                           doBreakpoint(action,panel,actionbox,false) ;
                        else if (enterbreak)
                           doBreakpoint(action,panel,actionbox,false) ;
                        else if (runtobreak == action)
                           doBreakpoint(action,panel,actionbox,false) ;
                     }
                     else
                        if (action.getBreakpoint() || (breakpause && OptionsDialog.getActionPause()))
                           if (!action.getNoBreakpoint())
                              doBreakpoint(action,panel,actionbox,false) ;
                     if (terminate) break ;
                  }
               }

   				// Action exceptions may be 'goto' exceptions, 'loop' exceptions
               // or 'exitevent' exceptions.  Action exceptions interrupt the
               // normal logic flow.  Our action result is a four object item
               // consisting of the action bounding box, exception name,
               // exception return object, and count of statements processed.

   				Object [] result = action.doAction(panel,thread,currentthread,eventstate) ;
               actionbox = (result == null) ? null : (Rectangle) result[0] ;
               exception = (result == null) ? null : (String) result[1] ;
               actionsprocessed += (result == null) ? 0 : ((Long) result[3]).intValue() ;

               // If breakpointing and we returned from a label event we must
               // re-establish our break event for step processing.  If we
               // returned from a timer action and had requested a step
               // into the timer, an interim break will have been set on
               // the alarm event.  This break is valid if the timer will
               // fire.  It is invalid if the timer was set to a zero delay.

               if (breakenabled)
               {
                  if (breakevent == null) breakevent = event ;
                  if (timerbreak)
                  {
                     Alarm alarm = action.getAlarm() ;
                     // Step Into and alarm not fired
                     if (alarm == null && action.isTimerAction())
                        stepProcessing() ;
                     // No alarm or alarm scheduled
                     else if (alarm == null || alarm.getInterval() > 0)
                        resumeProcessing() ;
                     // Timer with zero interval
                     else
                     {
         					Vector v = alarm.getEvent("alarm") ;
                        Object ae = (v != null && v.size() > 0) ? v.elementAt(0) : null ;
                        if (ae instanceof FKissEvent)
                        {
                           ((FKissEvent) ae).setInterimBreakpoint(breakframe,false) ;
                           stepProcessing() ;
                        }
                     }
                  }
               }

               // On a goto exception we must clear the event skip table and
               // reset all for actions.  The new action event is invoked.

  					if ("goto".equals(exception))
               {
            		event.clearSkip(currentthread) ;
                  Vector actions = event.getActionList() ;
                  if (actions != null)
                  {
              			for (i = 0 ; i < actions.size() ; i++)
                     {
                        action = (FKissAction) actions.elementAt(i) ;
                        if ("for".equals(action.getIdentifier())) action.initfor() ;
                     }
                  }

                  // Rename any temp variables.  Variables created from the
                  // calling event must be transitioned to the new label event.

  						FKissEvent event2 = (FKissEvent) result[2] ;
                  if (temporary && config != null)
                  {
                     Variable variable = config.getVariable() ;
                     if (variable != null) variable.renameTemporary(event,event2) ;
                  }

                  // Switch to the new label event.

  						event = (FKissEvent) result[2] ;
                  if (event != null)
                  {
   						event.setStartTime(System.currentTimeMillis()) ;
//   						event.setStartTime(System.nanoTime()) ;  // Java 1.5
     						event.setIfLevel(0) ;
                     event.setElseIfLevel(0) ;
                     loop = true ;

                     // If breakpointing we must establish our break event
                     // for continued processing.

                     if (breakenabled)
                     {
                        breakevent = event ;
                        if (topbreak == null) topbreak = event ;
                     }
                  }
     					break ;
               }

               // On a loop exception we jump to the start loop statement.

               else if ("loop".equals(exception))
               {
                  Integer n = (Integer) result[2] ;
                  int savesequence = sequence ;
                  if (n != null) sequence = n.intValue() ;
                  if (sequence < 0 || sequence >= a.length)
                     sequence = savesequence ;
               }

               // On an overflow exception we queue any overflow events
               // and continue.

               else if ("overflow".equals(exception))
               {
                 	MainFrame main = Kisekae.getMainFrame() ;
                  if (main == null) break ;
                  Configuration config = main.getConfig() ;
                  if (config == null) break ;
                  EventHandler handler = config.getEventHandler() ;
                  if (handler == null) break ;
  	  					Vector evt = handler.getEvent("overflow") ;
                  if (evt != null) handler.queueEvents(evt,thread,null) ;
                  break ;
  	            }

               // On an exitevent exception we exit.

               else if ("exitevent".equals(exception))
               {
                  break ;
               }

               // On a terminate request we exit.

               else if ("terminate".equals(exception))
               {
                  terminate = true ;
                  break ;
               }

               // On an exitloop exception we exit without repeat.  The repeat
               // exception is cancelled on the gosub return.

               else if ("exitloop".equals(exception))
               {
                  break ;
               }

               // On a repeat exception we reprocess the current statement.

               else if ("repeat".equals(exception))
               {
                  sequence-- ;
               }

               // Construct a combined bounding box for all actions.

   				if (box == null) box = actionbox ;
   				if (actionbox != null) box = box.union(actionbox) ;
     			}
   		}
      }

      catch (StackOverflowError e)
      {
			key.remove(currentthread) ;
      	String s = "Event stack overflow at depth " + depth + "\nLine [" + line + "] " + toString() ;
         if (e.getMessage() == null) e = new StackOverflowError(s) ;
      	depth = 0 ;
         throw e ;
      }

      finally 
      { 
         if (terminate)
         {
            terminate = false ;
            busy = false ;
            return null ; 
         }
      }


      // Pick up any local return value.  Return values are found in a local
      // variable identified by the label event name.

      returnvalue = null ;
      Variable variable = (config != null) ? config.getVariable() : null ;
      Vector parameters = event.getParameters() ;
      String eventname = (parameters != null && parameters.size() > 0)
         ? (String) parameters.elementAt(0) : null ;
      if (eventname != null && variable != null)
         returnvalue = variable.getValue("@"+eventname,event) ;

      // Remove any temporary variables that were defined. Alarms are 
      // non-recursive so we take the current temporary variable set
      // and back it up one depth level so modified parameters can be 
      // referenced on the next alarm invocation.

      if (temporary && variable != null)
      {
         variable.removeTemporary(event) ;
         if ("alarm".equals(getIdentifier()))
         {
            depth++ ;
            Hashtable locals = getVariableTable() ;
            depth-- ;
            if (locals != null) variabledepth.put(new Long(depth),locals) ;
         }
      }


		// The event is finished.  Remove this event from the skip thread
		// control table as actions that cause code skips do not extend
		// beyond the code module. Label events should not update the display
      // directly because the gosub calling event is responsible for the
      // update.  The redraw method is thread safe so we do not need to
      // switch to the AWT thread to perform a panel frame update.

      depth-- ;
      if (event != null)
      {
   		event.clearSkip(currentthread) ;
         Vector actions = event.getActionList() ;
         if (actions != null)
         {
      		for (i = 0 ; i < actions.size() ; i++)
            {
               action = (FKissAction) actions.elementAt(i) ;
               if ("for".equals(action.getIdentifier())) action.initfor() ;
            }
         }
      }

      // Return from gosub processing immediately.  If we are seeking a
      // return breakpoint signal the label return.  No top level event
      // call can be a label event.

      if ("label".equals(identifier))
      {
         if (returnbreak && event == breakevent)
         {
            doBreakpoint(action,panel,box,false) ;
            breakevent = null ;
         }
         if (breakenabled && topbreak == event)
            doBreakpoint(action,panel,box,true) ;
         Dimension d = (panel != null) ? panel.getSize() : null ;
         if (d != null) box = new Rectangle(d) ;
         this.box = box ;
         return box ;
      }

      // If this was an alarm that terminated clear the alarm time.

      if ("alarm".equals(identifier))
      {
         Object o = getFirstParameter() ;
         if (variable != null) o = variable.getValue((String) o,this) ;
   		String value = (o != null) ? o.toString() : null ;
   		if (value != null) value = value.toUpperCase() ;
			Alarm alarm = (Alarm) Alarm.getByKey(Alarm.getKeyTable(),cid,value) ;
         if (alarm != null) alarm.setTime(0) ;
      }

      // Fire any object overlap or collision events if the object has been 
      // moved.  Objects are fired once. 

      Enumeration enum1 = (eventstate != null) ? eventstate.elements() : null ;
      while (enum1 != null && enum1.hasMoreElements())
      {
         boolean moved = false ;
         boolean mustfire = false ;
         Object o = enum1.nextElement() ;
         if (!(o instanceof Object [])) continue ;
         Object [] param = (Object []) o ;
         if (!(param[0] instanceof KissObject)) continue ;
         KissObject ko = (KissObject) param[0] ;
         if (!ko.isVisible() || panel == null) continue ;
         mustfire = ((Boolean) param[3]).booleanValue() ;
         Point initlocation = (Point) param[2] ;
         Point location = ko.getLocation() ;
         if (initlocation != null && location != null)
            if (initlocation.x != location.x || initlocation.y != location.y) moved = true ;
         if ((moved || mustfire) && !objectfired.contains(ko))
         {
            objectfired.addElement(ko) ;
            box = panel.fireCollisionEvents(ko,eventstate,thread,box) ;
         }
      }

      // Enable any alarms that may have been initiated by this event.
      // Synchronize on the alarm timer queue to prevent queue activity
      // during this update so that alarms fire in proper sequence.

      if (event != null)
      {
         alarmlist = event.getAlarmList() ;
         Collections.sort(alarmlist,new AlarmDeclarationOrder()) ;
         for (i = 0 ; i < alarmlist.size() ; i++)
         {
            if (alarmlist.elementAt(i) instanceof Alarm)
            {
               Alarm a = (Alarm) alarmlist.elementAt(i) ;
               a.setStartTime(event.getStartTime());
               a.setTriggerTime() ;
               a.enableAlarm() ;
            }
         }
         
         // In general we want to retain the same sequence in which alarms
         // were queued as sequential processing can be dependent on this
         // sequence. But PlayFKiSS fires alarms in the CNF alarm order 
         // for identical firing times? 
         
         AlarmTimer timer = config.getTimer() ;
         if (timer != null) timer.queueAlarm(alarmlist) ;
 			alarmlist.removeAllElements() ;
      }

      // Redraw the screen.   Perform the redraw on the AWT thread.

      objectfired = new Vector() ;
		if (panel != null) 
      {
         final Rectangle box1 = box ;
         Runnable runner = new Runnable()
         { public void run() { panel.redraw(box1) ; } } ;
         javax.swing.SwingUtilities.invokeLater(runner) ;
      }

      // Accumulate the event performance statistics.

      runtime += (System.currentTimeMillis() - starttime) ;
//    runtime = (System.nanoTime() - starttime) ;  // Java 1.5
      totalactions += actionsprocessed ;
      invocations++ ;

      // If we are breakpoint processing signal the final end of event.

      if (breakenabled)
         doBreakpoint(action,panel,box,true) ;
      
   	if (OptionsDialog.getDebugEvent() && (!getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
   	{
         String bp = (event.getNoBreakpoint()) ? "*" : " " ;
   		long time = System.currentTimeMillis() - createtime ;
   		System.out.println("[" + time + "]"+bp+"[" + currentthread.getName() + "] FKissEvent end event " + event.getName() + " processing time " + runtime + " ms") ;
      }
      if (terminate) 
      {
         terminate = false ;
         busy = false ;
         return null ;
      }
      busy = false ;
      this.box = box ;
      return box ;
	}


   // Suspend processing due to a breakpoint request.  The breakpoint object
   // can be an event or an action. The panel screen is updated to ensure
   // we have a current visual display.

   void doBreakpoint(Object o, PanelFrame panel, Rectangle box, boolean end)
   {
      // If we are breakpointing and running on the AWT thread we terminate to
      // release control and perform all future processing on a new internal
      // thread.  Break processing requires that we do not suspend the AWT
      // thread.  Note that sequential event processing is sometimes assumed
      // under the ATW thread and this is not possible while breakpointing.
      
		if (SwingUtilities.isEventDispatchThread())
      {
         Thread t = new Thread()
         { public void run() { fireEvent(panel,firethread,firesource) ; } } ;
         t.start() ;
         terminate = true ;
         return ;
		}

    	try
      {
         synchronized (queue)
         {
            AlarmTimer.suspendTimer(true) ;
            EventHandler.suspendEventHandler(true) ;
        		if (panel != null) panel.redraw(box) ;
            boolean initialbreak = breakenabled ;

            // Capture the breakpoint event.

            breakevent = null ;
            breakpause = false ;
            breakenabled = true ;
            interimbreak = false ;
            if (o instanceof FKissAction) breakevent = ((FKissAction) o).getEvent() ;
            else if (o instanceof FKissEvent) breakevent = (FKissEvent) o ;
            if (topbreak == null) topbreak = breakevent ;
            if (OptionsDialog.getDebugControl())
            {
      			System.out.println("FKissEvent: breakpoint " + getName()) ;
      			System.out.println("FKissEvent: breakpoint on object " + o) ;
            }

            // Establish the breakpoint FKiSS Editor frame.  Issue a breakpoint
            // call to this frame and suspend the event execution.  The FKiSS
            // editor will reactivate event processing through a resume
            // processing or step processing call.

            Object frame = debugframe.get(Thread.currentThread()) ;
            if (frame instanceof FKissFrame)
            {
               Rectangle drawbox = (initialbreak && panel != null)
                  ? new Rectangle(panel.getPanelSize()) : box ;
               ((FKissFrame) frame).doBreakpoint(this,o,drawbox,end) ;
            }
            else
            {
               if (breakframe == null || !breakframe.isVisible())
               {
                  frame = new FKissFrame(this,config,o) ;
                  debugframe.put(Thread.currentThread(),frame) ;
                  ((FKissFrame) frame).setVisible(true) ;
               }
               else
               {
                  debugframe.put(Thread.currentThread(),breakframe) ;
                  breakframe.doBreakpoint(this,o,box,end) ;
               }
            }
            queue.wait() ;
         }
      }

      // Watch for an interrupt as this signals thread termination.

      catch (InterruptedException e) { terminate = true ; }
   }


   // Resume normal processing after a breakpoint.  Execution continues to
   // completion unless a new breakpoint is recognized.

	void resumeProcessing()
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
     			System.out.println("FKissEvent: breakpoint resume " + getName()) ;
         EventHandler.resumeEventHandler(true) ;
         AlarmTimer.resumeTimer(true) ;
         debugframe.clear() ;
         timerbreak = false ;
         stepbreak = false ;
         enterbreak = false ;
         returnbreak = false ;
         runtobreak = null ;
         breakenabled = false ;
         breakevent = null ;
         topbreak = null ;
		   queue.notify() ;
      }
	}


   // Execute the next instruction and breakpoint.

	void stepProcessing()
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
   		  	System.out.println("FKissEvent: breakpoint single step processing " + getName()) ;
         timerbreak = false ;
         stepbreak = true ;
         enterbreak = false ;
         returnbreak = false ;
         runtobreak = null ;
		   queue.notify() ;
      }
	}


   // Step into the next instruction and breakpoint.

	void enterProcessing()
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
   		  	System.out.println("FKissEvent: breakpoint step into processing " + getName()) ;
         timerbreak = false ;
         stepbreak = false ;
         enterbreak = true ;
         returnbreak = false ;
         runtobreak = null ;
		   queue.notify() ;
      }
	}


   // Run to the module return and breakpoint.

	void returnProcessing()
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
   		  	System.out.println("FKissEvent: breakpoint run to return processing " + getName()) ;
         timerbreak = false ;
         stepbreak = false ;
         enterbreak = false ;
         returnbreak = true ;
         runtobreak = null ;
		   queue.notify() ;
      }
	}


   // Run to the specified object and breakpoint.

	void cursorProcessing(Object o)
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
         {
   		  	System.out.println("FKissEvent: breakpoint run to cursor processing " + getName()) ;
   		  	System.out.println("FKissEvent: breakpoint cursor object is " + o) ;
         }
         timerbreak = false ;
         stepbreak = false ;
         enterbreak = false ;
         returnbreak = false ;
         runtobreak = o ;
		   queue.notify() ;
      }
	}


   // Timer processing is used for a step into on a timer related command.
   // Execution continues to completion if the alarm was set, otherwise
   // normal step processing occurs.

	void timerProcessing()
	{
      synchronized (queue)
      {
			if (OptionsDialog.getDebugControl())
     			System.out.println("FKissEvent: breakpoint timer step into " + getName()) ;
         timerbreak = true ;
         stepbreak = false ;
         enterbreak = false ;
         returnbreak = false ;
         runtobreak = null ;
		   queue.notify() ;
      }
	}


	// The getName method returns a string representation of this event.
	// This is the action name and parameter list.

	String getName() { return toStringComment(false) ; }


   // Function to evaluate a KiSS object key parameter.  If the parameter
   // is a variable then this function returns the variable value, otherwise
   // it returns the actual parameter.

   private String evaluateParam(String s)
   {
      if (variable == null) return s ;
      Object o = variable.getValue(s,this) ;
      String value = (o != null) ? o.toString() : s ;
      return value ;
   }


	// The toString method returns a string representation of this event.
	// This is the action name, parameter list, and comment string.

	public String toString() { return toStringComment(true) ; }


	// The toStringComment method builds a string representation of this
	// event, optionally including the comment text.

	String toStringComment(boolean showcomment)
	{
      if (identifier == null) return "" ;
   	StringBuffer sb = new StringBuffer(identifier) ;
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
      Cel cel =  (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),s1.toUpperCase()) ;
      if (cel == null) return s ;
      if (!cel.isWritable()) return s ;
      s1 = cel.getWriteName() ;
      if (literal) s1 = "\"" + s1 + "\"" ;
      return s1 ;
   }


   // Required comparison method for the Comparable interface. Event names
   // are examined so that events can be ordered correctly in a list.
   //
   // The order is as follows:
   //	1. Event names are compared lexographically.
   // 2. String parameters are compared lexographically.
   // 3. Group parameters are compared numerically.
   // 4. Numeric parameters are compared numerically .

   public int compareTo(Object o)
   {
      if (identifier == null) return -1 ;
   	if (!(o instanceof FKissEvent)) return -1 ;
      FKissEvent kissevent = (FKissEvent) o ;
      Object eventidentifier = kissevent.getIdentifier() ;
      if (!(eventidentifier instanceof String)) return -1 ;
      if (!(identifier.equals(eventidentifier)))
      	return (identifier.compareTo((String) eventidentifier)) ;
      Vector objectparam = kissevent.getParameters() ;
      if (objectparam == null) return (parameters == null) ? 0 : 1 ;
      if (parameters == null) return -1 ;

      // Compare parameters.

      for (int i = 0 ; i < parameters.size() ; i++)
      {
			if (i >= objectparam.size()) return 1 ;
			String p = (String) parameters.elementAt(i) ;
			String q = (String) objectparam.elementAt(i) ;
			if (p.equals(q)) continue ;
			if (p.charAt(0) == '#' && q.charAt(0) == '#')
 				{p = p.substring(1) ; q = q.substring(1) ; }
			if (p.charAt(0) == '\"' || q.charAt(0) == '\"')
         	return (p.compareTo(q)) ;

         // Try a numeric comparison.

         try
         {
         	Integer n1 = new Integer(p) ;
         	Integer n2 = new Integer(q) ;
	         return (n1.compareTo(n2)) ;
         }
         catch ( Exception e) { return (p.compareTo(q)) ; }
      }
      return -1 ;
   }
}
