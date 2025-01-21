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
* GifTimer class
*
* Purpose:
*
* This class is a timer activity to perform GIF animations.  It is used to
* update GIF image frames if the frame delay time expires.  The timer activity
* polls the animated GIF files once each period.  The polling period is set
* to 1/100 of a second according to the GIF specification.
*
*/


import java.util.Vector ;
import java.util.Enumeration ;
import java.util.NoSuchElementException ;
import java.awt.Rectangle ;
import javax.swing.* ;


final class GifTimer extends Thread
{
   static boolean enabled = true ;              // Timer enable state
   static boolean manualsuspend = false ;			// True, manual suspension
   static PanelFrame panel = null ;					// Our active panel frame
   static int id = 0 ;                    		// GifTimer identifier

   private Object suspendlock = new Object() ;	// Suspend lock
	private Thread thread = null ;					// The timer thread
	private Vector gifs = null ; 	      			// Set of GIFs to monitor
   private Rectangle box = null ;					// The drawing bounding box
   private boolean suspend = false ;    			// If true, suspend timer
   private boolean active = false ;					// If true, timer is active
   private boolean wait = false ;					// If true, timer is sleeping
   private long count = 0 ;							// Count of redraws performed


	// Constructor

	public GifTimer()
   {
      id++ ;
      setName("GifTimer-" + id) ;
   }


	// Class methods
   // -------------

   static void setEnabled(boolean b) { enabled = b ; }

   static void setPanelFrame(PanelFrame p) { panel = p ; }


	// Timer methods
	// -------------

	// Method to stop the timer.

	void stopTimer()
   {
		if (thread != null)
      {
         if (OptionsDialog.getDebugControl())
			   PrintLn.println("Stop animation timer.") ;
   	   thread.interrupt() ;
      }
      gifs = null ;
   }


	// Method to start the timer.

	void startTimer(Vector a)
   {
      if (a == null) return ;
      if (!OptionsDialog.getAnimateOn()) return ;
      gifs = (Vector) a.clone() ;

      // Retain only cels and objects that can animate.

      for (int i = gifs.size()-1 ; i >= 0 ; i--)
      {
         Object o = gifs.elementAt(i) ;
         if (!(gifs.elementAt(i) instanceof KissObject))
            { gifs.removeElementAt(i) ;  continue ; }
         KissObject cel = (KissObject) o ;
         if (cel.getFrameCount() <= 1)
            { gifs.removeElementAt(i) ;  continue ; }
         if (!cel.getAnimate())
            { gifs.removeElementAt(i) ;  continue ; }
      }

      // Start animator if there are cels to animate.

      if (gifs.size() == 0) gifs = null ;
	   if (OptionsDialog.getDebugControl())
		   PrintLn.println("Start animation timer.") ;
      start() ;
   }


	// Method to update the list of animated cels.  The timer must be
   // suspended before this routine is called.  If the thread is stopped
   // and cels exist it is started in a suspended mode.

	void updateCels(Vector a) { updateCels(a,true) ; }
	void updateCels(Vector a, boolean reset)
   {
      if (a == null) return ;
      if (!(suspend || thread == null)) return ;
      if (!OptionsDialog.getAnimateOn()) return ;
      Vector v = (Vector) a.clone() ;

      // Retain only objects with more than one frame.

      for (int i = v.size()-1 ; i >= 0 ; i--)
      {
         Object o = v.elementAt(i) ;
         if (!(v.elementAt(i) instanceof KissObject))
            { v.removeElementAt(i) ;  continue ; }
         KissObject cel = (KissObject) o ;
         if (cel.getFrameCount() <= 1)
            { v.removeElementAt(i) ;  continue ; }
      }

      // Add the new objects to the animation list. Objects
      // are added only if they do not currently exist.
      // The list is reset or reinitialized if necessary.

      if (gifs == null || reset) gifs = new Vector() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (!gifs.contains(o)) gifs.addElement(o) ;
      }

      // Start the thread if necessary.

      if (gifs.size() == 0) gifs = null ;
      if (thread == null)
      {
         suspend = true ;
         new Thread(this).start() ;
      }
   }


	// Method to suspend the timer.  If an event is active we
	// will wait for it to finish executing.

	void suspendTimer() { suspendTimer(false) ; }
	void suspendTimer(boolean manual)
   {
      if (thread == null) return ;
		if (OptionsDialog.getDebugControl() && !manualsuspend)
			PrintLn.println("Suspend animation timer. " + ((manual) ? "Manual" : "")) ;
   	if (manual) manualsuspend = true ;
   	suspend = true ;
   }


	// Method to resume a suspended timer activity.

	void resumeTimer() { resumeTimer(false) ; }
	void resumeTimer(boolean manual)
	{
      if (thread == null) return ;
   	if (manual) manualsuspend = false ;
      if (manualsuspend) return ;
      synchronized (suspendlock)
      {
			if (OptionsDialog.getDebugControl())
				PrintLn.println("Resume animation timer." + ((manual) ? "Manual" : "")) ;
   		suspend = false ;
		   suspendlock.notify() ;
      }
	}


   // Function to return the GifTimer state.

   String getTimerState()
   {
      if (!enabled) return Kisekae.getCaptions().getString("AlarmDisableState") ;
      if (suspend) return Kisekae.getCaptions().getString("AlarmSuspendState") ;
      if (wait) return Kisekae.getCaptions().getString("AlarmWaitState") ;
      if (active) return Kisekae.getCaptions().getString("AlarmActiveState") ;
      return Kisekae.getCaptions().getString("AlarmStopState") ;
   }

   // Function to return the GifTimer state.

   boolean isSuspended() { return suspend ; }
   boolean isActive() { return active ; }


   // Method to return an enumeration of the queue contents.   This
   // enumeration returns only those cels on the queue that are
   // contextually eligible for animation.

	Enumeration getQueue()
	{
		if (gifs == null) return null ;

		// Construct the enumeration object.  This lists all actions in the
		// event.

		return new Enumeration()
		{
			private int i = -1 ;
         private boolean elementfound = false ;
	      PageSet p = (panel == null) ? null : panel.getPage() ;
	      Integer page = (p == null) ? null : (Integer) p.getIdentifier() ;

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
				return (gifs.elementAt(i)) ;
			}

         private boolean findNextElement()
         {
         	int index ;
		   	if (gifs == null) return false ;
		      for (index = i+1 ; index < gifs.size() ; index++)
		      {
					Object c = gifs.elementAt(index) ;
					if (!(c instanceof KissObject)) continue ;
					KissObject cel = (KissObject) c ;
					if (cel.getFrameCount() <= 1) continue ;
					if (!cel.isOnPage(page)) continue ;
		         if (!cel.isVisible()) continue ;
					if (cel.getInterval() == 0) continue ;
					break ;
		      }
            i = index ;
            return (i < gifs.size()) ;
         }
		} ;
	}


   // Function to count the number of visible animated GIF cels on the
   // queue for the contextually specified page.

   int getQueueSize()
   {
   	int n = 0 ;
   	if (gifs == null) return 0 ;
      PageSet p = (panel == null) ? null : panel.getPage() ;
      Integer page = (p == null) ? null : (Integer) p.getIdentifier() ;
      for (int i = 0 ; i < gifs.size() ; i++)
      {
			Object c = gifs.elementAt(i) ;
			if (!(c instanceof KissObject)) continue ;
			KissObject cel = (KissObject) c ;
			if (cel.getFrameCount() <= 1) continue ;
			if (!cel.isOnPage(page)) continue ;
         if (!cel.isVisible()) continue ;
         if (cel.getInterval() == 0) continue ;
			n++ ;
      }
      return n ;
   }


   // Method to return an indicator if the specified object is on
   // the animation queue.

	boolean isQueued(Object o)
	{
		if (gifs == null) return false ;
      return gifs.contains(o) ;
   }


   // Method to return the number of animations fired.

   long getCount() { return count ; }



	// The timer thread code.  This code runs until the activity
	// is terminated or suspended.

	public void run()
	{
      int period = OptionsDialog.getGifPeriod() ;
		thread = Thread.currentThread() ;
		thread.setName("GifTimer-" + id) ;
		if (OptionsDialog.getDebugControl())
			PrintLn.println(thread.getName() + " started.") ;

		// Run the timer loop until this activity is terminated.

		while (true)
		{
         try
         {
         	if (gifs == null) break ;
   			for (int i = 0 ; i < gifs.size() ; i++)
   			{
            	// Suspend timer execution if requested.  We restart after
               // a suspension because the GIF vector may have changed.

					synchronized (suspendlock)
					{
                  boolean restart = false ;
						while (suspend)
						{
							active = false ;
               		if (OptionsDialog.getDebugControl())
                        PrintLn.println(thread.getName() + " suspended.") ;
							suspendlock.wait() ;
               		if (OptionsDialog.getDebugControl())
                        PrintLn.println(thread.getName() + " resumed.") ;
                     restart = true ;
						}
                  if (restart) break ;
   				}

   				// Access the object to determine the animation period.
               // We animate only visible cels on the current page.

               active = true ;
               if (!enabled) continue ;
               Object o = gifs.elementAt(i) ;
               if (!(o instanceof KissObject)) continue ;
					KissObject c = (KissObject) o ;
               if (!c.getAnimate()) continue ;
					if (c.getFrameCount() <= 1) continue ;
		         if (!c.isVisible()) continue ;
					PageSet p = (panel == null) ? null : panel.getPage() ;
			      Integer page = (p == null) ? null : (Integer) p.getIdentifier() ;
					if (!c.isOnPage(page)) continue ;
               int maxloop = c.getLoopLimit() ;
               int loop = c.getLoopCount() ;
               if (maxloop < 0 && loop > 0) continue ;
               if (maxloop > 0 && loop >= maxloop) continue ;
  					int delay = c.getInterval() ;
  					long time = c.getTime() ;
  					time = time + period ;
  					c.setTime(time) ;

  					// If the time exceeds the delay, perform the animation.
  					// The cel will switch to the next frame and set a new
               // animation delay value for this frame.  We construct
               // a combined bounding box for all actions.

  					if (time >= delay)
  					{
               	c.setTime(0) ;
               	try { c.setNextFrame() ; }
                  catch (Exception e) { PrintLn.println(e.getMessage()) ; }
						Rectangle r = c.getBoundingBox() ;
						if (box == null) box = r ;
						if (r != null) box = box.union(r) ;
   				}
            }

     			// Sleep a bit now that we have initiated all animations in this
            // time cycle.  We terminate on receipt of an interrupt.

				if (panel != null && box != null)
            {
            	panel.redraw(box) ;
               count++ ;
            }
            box = null ;
            wait = true ;
	     		sleep(period) ;
            wait = false ;
			}
			catch (InterruptedException e) { break ; }

	      // Watch for memory faults.  If we run low on memory suspend
	      // the animation thread.

			catch (OutOfMemoryError e)
			{
            suspendTimer(true) ;
				Runtime.getRuntime().gc() ;
	         try { Thread.currentThread().sleep(300) ; }
	         catch (InterruptedException ex) { }
				PrintLn.println("GifTimer: Out of memory.") ;
				JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
	           	"Insufficient memory.  Animator thread is suspended.",
	           	"Low Memory Fault",
	            JOptionPane.ERROR_MESSAGE) ;
			}
		}

      // Shut down.

		if (OptionsDialog.getDebugControl())
	      PrintLn.println(thread.getName() + " terminated.");
      thread = null ;
      panel = null ;
      gifs = null ;
      active = false ;
      suspend = false ;
      wait = false ;
	}
}