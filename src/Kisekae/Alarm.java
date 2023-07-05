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
* Alarm class
*
* Purpose:
*
* This class is an alarm object for the FKiSS animation.  An alarm
* maintains the time delays for animation events.
*
* Alarms use their number as their access key.
*
*/

import java.awt.event.* ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Vector ;
import java.io.IOException ;
import java.io.OutputStream ;
import javax.swing.Timer ;

final class Alarm extends KissObject implements Comparable
{
	// Class attributes.  Sized for 512 alarm objects.

	static private Hashtable key = new Hashtable(600,0.855f) ;

	// Alarm attributes

	private int delay = -1 ;				// The timer interval
	private long time = 0 ;					// The elapsed time
	private long starttime = 0 ;			// The time when the alarm is created
	private long triggertime = 0 ;		// The time to trigger the alarm 
   private Thread activator = null ;	// The user activation thread
   private boolean enabled = true ;		// The alarm enable flag
   private String source = null ;      // The scheduling FKiSS timer action
   private Timer timer = null ;        // The scheduling timer
   
	// Create an action listener for timer scheduling.  This is an alternate
   // method to using the AlarmTimer periodic polling process.
   
   ActionListener task = new ActionListener()
   {
      public void actionPerformed(ActionEvent evt)
      {
         Thread activator = AlarmTimer.getThread() ;
         Thread alarmactivator = getActivator() ;
         if (alarmactivator != null && "endevent".equals(alarmactivator.getName()))
            activator = alarmactivator ;
			setInterval(Integer.MAX_VALUE,activator) ;
			Vector v = getEvent("alarm") ;
			EventHandler.queueEvents(v,activator,getSource()) ;
         timer = null ;
      }
   } ;


	// Constructor

	public Alarm(Object cid, String id)
	{
		setID(cid) ;
		setIdentifier(id) ;
		setKey(key,cid,id.toUpperCase()) ;
		init() ;
	}


	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// configuration identifier.

	static void clearTable(Object cid)
	{
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}


	// Object state change methods
	// ---------------------------

	// Initialize the alarm.  A negative delay value stops the alarm
	// from firing, but not from being set.  A zero delay value stops
	// the alarm from firing and also stops all new timer updates from
   // modifying this alarm.  Such updates can come from the alarm timer.

	void init()
	{
		delay = -1 ;
		time = 0 ;
      activator = null ;
      enabled = true ;
	}


	// Set the alarm interval.
	//
	// We can have a situation where the actions for the alarm are
	// directly or indirectly self-referential and attempt to reset
	// this interval, yet an independent activity such as a mouse
	// event may have reset the alarm while the self-referential
	// actions were queued for timer processing.  If this happens
	// the self-referential alarm update should not proceed.
	//
	// Conversely, if we start an alarm through a mouse event then
	// any other timer that is initiated due to this activation must
	// also run.  Every alarm maintains an activator state that can
	// be propogated to other alarms during timer action initiation.

	synchronized void setInterval(int t, Thread thread)
	{
		// We allow this interval setting unless we were started through
		// a timer event and the alarm has been set to zero (stopped) by
      // a user event.
      
      if (delay == 0)
      {
         if (thread != null && thread.getName().startsWith("AlarmTimer"))
         {
            if (activator != null && !activator.getName().startsWith("EventHandler")) return ;
         }
      }
      
      // We do not allow this interval setting if the alarm has a delay
      // of Integer.MAX_VALUE as this implies the alarm was previously
      // scheduled and queued on the EventHandler. This is a timing
      // window that can occur if independent events try to schedule
      // the same alarm. Only one alarm instance should be active.
      
      if (delay == Integer.MAX_VALUE && t > 0) return ;

      // If we are setting this alarm we must disable it.  For the alarm
      // to fire it must be enabled.  It is the responsibility of the
      // initiating FKiSS event to enable the alarm when event processing
      // is complete.

      if (t > 0) enabled = false ;

		// Set our activation flag.  The activation setting is used by
      // all subordinate timer actions when the alarm event occurs.
      // When the alarm fires the alarm activation setting is retrieved
      // and used as the activator for any new timer actions that are
      // scheduled.  We do not set the activator if the alarm is being
      // expired (t < 0).  If we are setting the activator we set it to
      // the activator on our call parameter list, providing that the alarm
      // has not expired.

      if (t > 0) activator = (delay < 0) ? Thread.currentThread() : thread ;
      if (thread != null && "endevent".equals(thread.getName())) activator = thread ;
      if (t == 0) activator = null ;

      // Set the new timer delay.

 		delay = t ;
		time = 0 ;
	}


	// Set the timer count time.  This time is set by the Alarm timer every
   // tick of the timer.  The alarm fires when the time equals or exceeds
   // the delay.  The alarm timer takes the alarm event associated with
   // this KissObject and queues it to the event handler for processing.

	void setTime(long t) 
   { 
      time = t ; 
      if (time == 0) setStartTime(0) ;
   }


	// Set the time when the alarm is created.  This time is used to compute
   // the actual time at which the alarm is to be triggered.  This sequences
   // the alarms for proper order.

	void setStartTime(long t) { starttime = t ; }


	// Set the minimum time when the alarm is to be triggered.  This is the 
   // start time plus the delay time.  

	void setTriggerTime() { triggertime = starttime + delay ; }
	void setTriggerTime(long t) { triggertime = t ; }


	// Set the alarm activation source.  This is the FKiSS action command
   // string that scheduled this alarm.

	void setSource(String s) { source = s ; }


   // Enable the alarm.   When a timer event action is processed the alarms
   // are initially disabled.  The alarm should not be fired until the
   // enabling event has terminated.

   void enableAlarm()
   {
   	enabled = true ;
      
      // If the timer is active set a new delay.  Using Java Timer is an 
      // alternate method to scheduling alarms instead of using the polled 
      // AlarmTimer.  When using this method the AlarmTimer must be modified 
      // to not put the alarm on the EventHandler queue.

      if (!OptionsDialog.getTimerOn())
      {
         if (timer != null) timer.stop() ;
         timer = null ;
      
         // Only one alarm instance should be active.
      
         if (delay > 0 && delay != Integer.MAX_VALUE)
         {
            timer = new Timer(delay,task) ;
            timer.setRepeats(false) ;
            timer.start() ;
         }
      }
   }


	// Object state reference methods
	// ------------------------------

	// Return the alarm delay time.

	int getInterval() { return delay ; }

	// Return the alarm timer time.

	long getTime() { return time ; }
   
	// Return the time the alarm was created.  This is the event start time.

	long getStartTime() { return starttime ; }
   
	// Return the time at which the alarm was triggered.

	long getTriggerTime() { return triggertime ; }
	long getTriggeredTime() { return triggertime - time ; }

	// Return the forced timer activation setting.

	Thread getActivator() { return activator ; }

	// Return the Java Timer.

	Timer getTimer() { return timer ; }

	// Return the alarm delay value.

	synchronized boolean isStopped() { return (delay == 0) ; }

	// Return the alarm enable flag.

	boolean isEnabled() { return enabled ; }

	// Return the alarm scheduling source command.

	String getSource() { return source ; }


	// Object state retention methods
	// ------------------------------

	// Save the current alarm state.  The state is given an identifier
	// for later reference in case it must be restored.

	void saveState(Object cid, Object id)
	{
		State sv = new State(cid,this,id,2) ;
		sv.variable[0] = new Integer(delay) ;
		sv.variable[1] = new Long(time) ;
	}

	// Restore the required alarm state.

	void restoreState(Object cid, Object id)
	{
		State sv = (State) State.getByKey(cid,this,id) ;
		if (sv != null)
		{
			delay = ((Integer) sv.variable[0]).intValue() ;
			time = ((Long) sv.variable[1]).longValue() ;
		}
	}

	// KiSS object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }
 
   
   // Comparable interface to sort alarms on their timer delay value.
   // This is used by the EventHandler to schedule alarms in proper
   // sequence within the timer period setting.  
   
   public int compareTo(Alarm a)
   {
      return (getTriggeredTime() > a.getTriggeredTime()) ? 1 : -1 ;
   }
}
