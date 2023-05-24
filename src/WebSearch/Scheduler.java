package WebSearch ;

// Title:        Kisekae UltraKiss
// Version:      2.0
// Copyright:    Copyright (c) 2002-2005
// Company:      WSM Information Systems Inc.
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
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
 *
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  This copyright notice and this permission notice shall be included in      %
%  all copies or substantial portions of UltraKiss.                           %
%                                                                             %
%  The software is provided "as is", without warranty of any kind, express or %
%  implied, including but not limited to the warranties of merchantability,   %
%  fitness for a particular purpose and noninfringement.  In no event shall   %
%  WSM Information Systems be liable for any claim, damages or other          %
%  liability, whether in an action of contract, tort or otherwise, arising    %
%  from, out of or in connection with UltraKiss or the use of UltraKiss       %
%                                                                             %
%  WSM Information Systems Inc.                                               %
%  144 Oakmount Rd. S.W.                                                      %
%  Calgary, Alberta                                                           %
%  Canada  T2V 4X4                                                            %
%                                                                             %
%  http://www.kisekaeworld.com                                                %
%                                                                             %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
*/



/**
* Scheduler class
*
* Purpose:
*
* This class is a container class to process all thread control for the
* WebSearch events.  It runs as a separate activity and processes all events
* that are placed on its input queue.
* 
* This class is part of a simple web browser for searching and downloading 
* KiSS sets from a host server.
*/


import java.util.Vector ;
import java.util.Enumeration ;
import javax.swing.JOptionPane ;
import javax.swing.Timer ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.awt.event.* ;
import Kisekae.OptionsDialog ;

final class Scheduler
	implements Runnable
{

	// Object attributes

   private static int count = 0 ;                  // The handler count
   private static ThreadGroup threadgroup = null ;	// The event handler group
   private static Vector handlers = new Vector() ;	// The active handlers
	private static Vector queue = new Vector() ;    // The event queue
	private static Vector processed = new Vector() ;  // The processed queue
   private static Object eventlock = new Object() ;  // The event lock
	private static boolean suspend = false ;	      // True if we must suspend
	private static boolean stop = false ;	         // True if we must stop
   private static boolean manualsuspend = false ;	// True if manual suspend

	// Thread attributes

	private Thread thread = null ;	      	      // The active thread
   private String threadname = null ;					// The thread name
   private long firecount = 0 ;							// Count of events processed
	private boolean active = false ;	         	   // True if handler is active
   private boolean wait = false ;						// True if waiting
	private static long createtime = 0 ;			   // EventHandler creation time


	// Constructor

   public Scheduler()
   {
      threadname = "Scheduler-" + count++ ;
   }
	

	// Object state change methods
	// ---------------------------

	// Place a single event on the event handler queue.  This method must
	// be synchronized to notify the thread activity.  Each event has an
   // associated execution thread.

	static void queueEvent(GetLinks event, ThreadGroup t)
	{
		if (event == null) return ;

      synchronized (eventlock)
      {
         // Check that we are not already processed.

         for (int i = 0 ; i < processed.size() ; i++)
         {
            GetLinks gl = (GetLinks) processed.elementAt(i) ;
            String s = gl.getLocation() ;
            if (s == null) continue ;
            if (s.equalsIgnoreCase(event.getLocation())) return ;
         }

         // Check that we are not already queued.

         for (int i = 0 ; i < queue.size() ; i++)
         {
            Object [] queueobject = (Object []) queue.elementAt(i) ;
            if (queueobject == null) continue ;
            GetLinks gl = (GetLinks) queueobject[0] ;
            if (gl == null) continue ;
            String s = gl.getLocation() ;
            if (s == null) continue ;
            if (s.equalsIgnoreCase(event.getLocation())) return ;
         }

         // Queue the event.

         Object [] qentry = new Object[2]  ;
         qentry[0] = event ;
         qentry[1] = t ;
		   queue.addElement(qentry) ;
   		eventlock.notify() ;
      }
	}


	// Remove an event from the event handler queue.

	static Object dequeueEvent()
	{
      synchronized (eventlock)
      {
	   	if (queue.size() == 0) return null ;
	   	Object o = queue.elementAt(0) ;
	   	queue.removeElement(o) ;
	   	return o ;
      }
	}

	
	
	// Object utility methods
	// ----------------------


	// Static method to clear the queue.  This is used when we switch to a
	// new page and start a new animation sequence.

	static void clearEventQueue()
   { synchronized (eventlock) { queue.removeAllElements() ; } }
	
	
	
	// Event Handler methods
	// ---------------------
	
	// Start the handler.  This creates a new thread.  The thread
	// suspends itself and waits to be resumed by the timer activity
	// when a new event is placed on the queue.  All event handler
   // threads are placed within the same thread group.
	
	void startScheduler()
	{
   	stop = false ;
      if (OptionsDialog.getDebugSearch())
         System.out.println("Start Scheduler.") ;
      if (threadgroup == null || threadgroup.isDestroyed())
      {
      	threadgroup = new ThreadGroup("Scheduler") ;
         threadgroup.setDaemon(true) ;
      }
		thread = new Thread(threadgroup,this) ;
 		thread.start() ;
	}

	
	// Static method to stop all event handler threads.  This will interrupt
   // all threads within the event handler group and release all resources.
	
	static void stopScheduler()
	{
      stop = true ;
      if (OptionsDialog.getDebugSearch())
         System.out.println("Stop Scheduler.") ;
      clearEventQueue() ;
      if (threadgroup == null || threadgroup.isDestroyed()) return ;
      threadgroup.interrupt() ;

      // Wait for the thread group destruction.

      int i = 0 ;
      while (i < 5)
      {
	      try { Thread.currentThread().sleep(200) ; }
	      catch (InterruptedException e) { }
	      if (threadgroup.isDestroyed()) break ;
         i++ ;
      }

      // Release critical references.

      threadgroup = null ;
      if (i == 5)
         System.out.println("Scheduler: Thread termination timeout.");
	}


   // Static method to return an enumeration of the queue contents.

   static Enumeration getQueue()
   { return (queue == null) ? null : queue.elements() ; }


   // Static method to count the number of events on the queue.

   static int getQueueSize() { return queue.size() ; }


   // Static method to reset the scheduler to initial state.

   static void reset() 
   { 
      stopScheduler() ;
      queue = new Vector() ;
      processed = new Vector() ;
      suspend = false ;	      
      stop = false ;	         
      manualsuspend = false ;	
   }


   // Static method to return the event handler threads.

   static Vector getThreads() { return handlers ; }


   // Static method to return the global EventHandler active state.

   static boolean isActive() { return !(suspend || stop) ; }


   // Method to return this event handler name.

   String getName() { return threadname ; }


   // Function to return this EventHandler thread state.

   String getState()
   {
      if (suspend) return "Suspended" ;
   	if (wait) return "Waiting" ;
      if (active) return "Active" ;
      return "Stopped" ;
   }


   // Method to return the number of events fired by this thread.

   long getCount() { return firecount ; }

	
	// Static method to suspend the event handler.   Suspension causes all
   // threads in the event handler to enter the wait state.  One must be
   // notified to restart execution.  The suspend flag is global to all
   // instances of the event handler.

	static void suspendScheduler(){ suspendScheduler(false) ; }
	static void suspendScheduler(boolean manual)
   {
   	if (manual) manualsuspend = true ;
   	suspend = true ;
   }


	// Static method to resume the handler activity.  This method is
   // synchronized on the queue object because it must notify an independent
   // thread activity.

	static void resumeScheduler() {resumeScheduler(false) ; }
	static void resumeScheduler(boolean manual)
	{
   	if (manual) manualsuspend = false ;
      if (manualsuspend) return ;
      synchronized (eventlock)
      {
   		suspend = false ;
		   eventlock.notify() ;
      }
	}

	
	
	// Event Handler execution thread
	// ------------------------------
	
	public void run() 
	{
   	active = true ;
      Thread thread = null ;
      Thread me = Thread.currentThread() ;
		me.setPriority(Thread.MIN_PRIORITY) ;
		me.setName(threadname) ;
      if (OptionsDialog.getDebugSearch())
         System.out.println(me.getName() + " started.") ;

		// The handler suspends itself until it is notified.  Once
		// notified the handler performs all event actions on the queue
		// and then suspends itself again.

		try
      {
			while (!stop)
			{
         	try
            {
					synchronized (eventlock)
					{
						while (queue.size() == 0 || suspend)
						{
							wait = true ;
							eventlock.wait() ;
						}
					}

					// Fire the event.  We can wake up with an empty queue.
	            // We yield control after every event.

					wait = false ;
	 	 			Object qentry = Scheduler.dequeueEvent() ;
					if (qentry != null)
					{
						Object [] queueobject = (Object []) qentry ;
						GetLinks event = (GetLinks) queueobject[0] ;
						ThreadGroup threadgroup = (ThreadGroup) queueobject[1] ;
                  int n = threadgroup.activeCount() ;
                  if (n < 3)
                  {
                     processed.addElement(event) ;
                     Thread newthread = new Thread(threadgroup,event) ;
                     newthread.start() ;
   	               firecount++ ;
                  }
                  else
                  {
                     ActionListener listen = new ActionListener()
                     {
                        public void actionPerformed(ActionEvent e)
                        { resumeScheduler() ; }
                     } ;

                     suspendScheduler() ;
                     queue.insertElementAt(qentry,0) ;
                     Timer t = new Timer(500,listen) ;
                     t.setRepeats(false) ;
                     t.start() ;
                  }
	               me.yield() ;
               }
            }

		      // Watch for memory faults.  If we run low on memory invoke
		      // the garbage collector and wait for it to run.  Close the
		      // configuration.

				catch (OutOfMemoryError e)
				{
		         suspendScheduler(true) ;
					Runtime.getRuntime().gc() ;
		         try { Thread.currentThread().sleep(300) ; }
		         catch (InterruptedException ex) { }
					System.out.println("Scheduler: Out of memory.") ;
				}
			}
      }

      // Watch for an interrupt as this signals thread termination.

      catch (InterruptedException e) { }

      // Watch for internal faults.  Close the configuration.

		catch (Throwable e)
		{
         stopScheduler() ;
			System.out.println("Scheduler: Throwable " + e) ;
		}

      // Clear critical resource references.

      this.thread = null ;
      this.active = false ;
      this.wait = false ;
      firecount = 0 ;
      if (OptionsDialog.getDebugSearch())
         System.out.println(me.getName() + " terminated.");
	}
}
