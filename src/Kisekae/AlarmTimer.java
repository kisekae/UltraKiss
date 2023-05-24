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
* AlarmTimer class
*
* Purpose:
*
* This class is a timer activity.  It is used to activate alarms if
* the alarm delay time expires.  The timer activity polls the alarms
* once each period.  The overall event sensitivity depends on the
* period delay value.
*
*/


import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.util.NoSuchElementException ;


final class AlarmTimer extends Thread
{
   private static Object suspendlock = new Object() ;	// Suspend lock
   private static Object queuelock = new Object() ;	// Queue lock

   private static boolean enabled = true ;         // Timer enable state
   private static boolean manualsuspend = false ;  // True, manual suspension
   private static boolean suspend = false ;        // If true, suspend timer
   private static int id = 0 ;                     // AlarmTimer identifier

	private static Thread thread = null ;				// The timer thread
	private Vector alarms = null ; 	      		   // Set of alarms to monitor
   private boolean active = false ;					   // If true, timer is active
   private boolean wait = false ;					   // If true, timer is sleeping
   private long count = 0 ;							   // Count of alarms fired
   private long defaultperiod = 10 ;               // Sleep default time peroid
   private long overtime = 0 ;                     // Sleep period difference


	// Constructor

	public AlarmTimer()
   {
      id++ ;
      setName("AlarmTimer-" + id) ;
   }


	// Class methods
   // -------------

   static void setEnabled(boolean b) { enabled = b ; }


	// Timer methods
	// -------------

	// Method to stop the timer.

	void stopTimer()
   {
      if (thread != null)
      {
         if (OptionsDialog.getDebugControl())
			   System.out.println("Stop alarm timer.") ;
   	   thread.interrupt() ;
      }
      alarms = null ;
   }


	// Method to start the timer.

	void startTimer(Vector a)
   {
      if (!OptionsDialog.getTimerOn()) return ;
      alarms = a ;
      start() ;
   }


	// Method to suspend the timer.  If an event is active we
	// will wait for it to finish executing.

	static void suspendTimer() { suspendTimer(false) ; }
	static void suspendTimer(boolean manual)
   {
		if (OptionsDialog.getDebugControl() && !manualsuspend)
			System.out.println("Suspend alarm timer. " + ((manual) ? "Manual" : "")) ;
   	if (manual) manualsuspend = true ;
   	suspend = true ;
   }


	// Method to resume a suspended timer activity.

	static void resumeTimer() { resumeTimer(false) ; }
	static void resumeTimer(boolean manual)
	{
   	if (manual) manualsuspend = false ;
      if (manualsuspend) return ;
      synchronized (suspendlock)
      {
			if (OptionsDialog.getDebugControl())
				System.out.println("Resume alarm timer.") ;
   		suspend = false ;
		   suspendlock.notify() ;
      }
	}


   // Function to return the AlarmTimer state.

   String getAlarmState()
   {
      if (!enabled) return Kisekae.getCaptions().getString("AlarmDisableState") ;
      if (suspend) return Kisekae.getCaptions().getString("AlarmSuspendState") ;
   	if (wait) return Kisekae.getCaptions().getString("AlarmWaitState") ;
      if (active) return Kisekae.getCaptions().getString("AlarmActiveState") ;
      return Kisekae.getCaptions().getString("AlarmStopState") ;
   }


   // Method to return an enumeration of the queue contents.   This
   // enumeration returns only those alarms that are enabled and
   // eligible to be fired.

	Enumeration getQueue()
	{
		if (alarms == null) return null ;

		// Construct the enumeration object.  This lists all actions in the
		// event.

		return new Enumeration()
		{
			private int i = -1 ;
         private boolean elementfound = false ;

			public boolean hasMoreElements()
         {
         	elementfound = findNextElement() ;
         	return (elementfound) ;
         }

			public Object nextElement() throws NoSuchElementException
			{
         	if (!elementfound) elementfound = findNextElement() ;
				if (!elementfound) throw new NoSuchElementException() ;
            elementfound = false ;
				return (alarms.elementAt(i)) ;
			}

         private boolean findNextElement()
         {
         	int index ;
		   	if (alarms == null) return false ;
		      for (index = i+1 ; index < alarms.size() ; index++)
		      {
					Alarm alarm = (Alarm) alarms.elementAt(index) ;
		         if (!alarm.isEnabled()) continue ;
					int delay = alarm.getInterval() ;
					if (delay > 0) break ;
		      }
            i = index ;
            return (i < alarms.size()) ;
         }
		} ;
	}


   // Function to count the number of enabled alarms on the queue.

   int getQueueSize()
   {
   	int n = 0 ;
   	if (alarms == null) return 0 ;
      for (int i = 0 ; i < alarms.size() ; i++)
      {
			Alarm alarm = (Alarm) alarms.elementAt(i) ;
         if (!alarm.isEnabled()) continue ;
			int delay = alarm.getInterval() ;
			if (delay > 0) n++ ;
      }
      return n ;
   }


   // Method to reset all elements in the alarm queue.

	void resetQueue()
	{
		if (alarms == null) return ;
      for (int i = 0 ; i < alarms.size() ; i++)
      {
         Alarm a = (Alarm) alarms.elementAt(i) ;
         synchronized (a) { a.init() ; }
      }
   }

	// Place a single alarm on the monitor queue.

	void queueAlarm(Alarm alarm)
	{
      synchronized (queuelock)
      {
         if (alarm == null) return ;
         if (alarms == null) return ;
         if (!alarms.contains(alarm)) alarms.add(alarm) ;
      }
	}

	// Place a list of alarms on the monitor queue.

	void queueAlarm(Vector v)
	{
      synchronized (queuelock)
      {
         if (v == null) return ;
         if (alarms == null) return ;
         for (int i = 0 ; i < v.size() ; i++)
            queueAlarm((Alarm) v.elementAt(i)) ;
         
         // In general we want to retain the same sequence in which alarms
         // were queued as sequential processing can be dependent on this
         // sequence. But PlayFKiSS fires alarms in the CNF alarm order 
         // for identical firing times? 
         
         if ("PlayFKiss".equals(OptionsDialog.getCompatibilityMode()))
            Collections.sort(alarms,new AlarmDeclarationOrder()) ;
      }
	}

	// Remove an alarm from the monitor queue.

	void removeAlarm(Alarm alarm)
	{
      synchronized (queuelock)
      {
         if (alarm == null) return ;
         if (alarms == null) return ;
         alarms.remove(alarm) ;
      }
	}


   // Method to return the number of alarms fired.

   long getCount() { return count ; }
   
   
   
   // Method to return the timer thread
   
   static Thread getThread() { return thread ; }


   // Method to return the alarms queue. We need to synchronize on this
   // queue when enabling queued alarms to ensure that alarms execute in 
   // the sequence in which they were queued.

   Vector getAlarmQueue() { return alarms ; }


	// The timer thread code.  This code runs until the activity is
	// terminated or suspended.  A default period of 50 msec is used
   // if an invalid period option is set.

	public void run()
	{
		thread = Thread.currentThread() ;
		thread.setName("AlarmTimer-" + id) ;
		if (OptionsDialog.getDebugControl())
			System.out.println(thread.getName() + " started.") ;
//      thread.setPriority(Thread.MAX_PRIORITY) ;
      long period = OptionsDialog.getTimerPeriod() ;
      if (period < 0) period = defaultperiod ;
      defaultperiod = period ;

		// Run the timer loop until this activity is terminated.
		// As this activity can update the alarm interval concurrently
		// with an alarm timer action, this is a critical section of
		// code.

		while (true)
		{
         try
         {
         	if (alarms == null) break ;

            // Suspend timer execution if requested.

				synchronized (suspendlock)
				{
					while (suspend)
					{
						active = false ;
              		if (OptionsDialog.getDebugControl())
                     System.out.println(thread.getName() + " suspended.") ;
						suspendlock.wait() ;
              		if (OptionsDialog.getDebugControl())
                     System.out.println(thread.getName() + " resumed.") ;
					}
   			}

            // Do nothing if the timer is disabled.

            if (!OptionsDialog.getTimerOn()) continue ;

   			// Lock the alarm to stop simultaneous updates.

            synchronized (queuelock)
            {
               active = true ;
               if (alarms == null) break ;
               for (int i = 0 ; i < alarms.size() ; i++)
               {
      				Alarm alarm = (Alarm) alarms.elementAt(i) ;
      				synchronized (alarm)
      				{
                     if (!alarm.isEnabled()) continue ;
      					int delay = alarm.getInterval() ;
      					if (delay < 0) continue ;
      					if (delay == 0)
                     {
      						alarm.setInterval(-1,thread) ;
                        continue ;
                     }

      					// Alarm is active.  Fire the alarm when time equals
      					// or exceeds the delay.

      					long time = alarm.getTime() ;
      					time = time + period ;
      					alarm.setTime(time) ;

      					// Now we have a consistent delay and time.  If the
      					// time exceeds the delay, fire the alarm event.
      					// The timer activity will clear the alarm delay
      					// to terminate this alarm.

      					if (time >= delay)
      					{
                        Thread activator = thread ;
                        Thread alarmactivator = alarm.getActivator() ;
                        if (alarmactivator != null && "endevent".equals(alarmactivator.getName()))
                           activator = alarmactivator ;
      						alarm.setInterval(Integer.MAX_VALUE,activator) ;
      						Vector v = alarm.getEvent("alarm") ;
                        // Do this only if alarms are not scheduled by a Timer 
      						EventHandler.queueEvents(v,activator,alarm) ;
                        count++ ;
      					}
                  }
   				}
               
               // Remove alarms which are inactive.
               
               for (int i = 0 ; i < alarms.size() ; i++)
               {
      				Alarm alarm = (Alarm) alarms.elementAt(i) ;
     					int delay = alarm.getInterval() ;
                  if (delay < 0) removeAlarm(alarm) ;
               }
            }

     			// Sleep a bit now that we have initiated all alarms in this
            // time cycle.  We terminate on receipt of an interrupt.

            period = defaultperiod - overtime ;
            if (period <= 0) period = 1 ;
            long starttime = System.currentTimeMillis() ; 
            wait = true ;
            sleep(period) ;
            wait = false ;
            long endtime = System.currentTimeMillis() ; 
            long diff = endtime - starttime ;
            if (diff <= 0) diff = period = defaultperiod ;
            overtime = diff - period ; 
            period = defaultperiod ;
			}
			catch (InterruptedException e) { break ; }
		}

		if (OptionsDialog.getDebugControl())
	      System.out.println(thread.getName() + " terminated.");
      thread = null ;
      active = false ;
      suspend = false ;
      wait = false ;
	}
}
