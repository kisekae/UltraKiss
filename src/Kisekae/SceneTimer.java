package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.6.1  (November 4, 2023)
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
* SceneTimer class
*
* Purpose:
*
* This class is a timer activity to free memory allocations for scene cels.  
* A scene cel is a cel located on a page that is not page 0 when the set option 
* PagesAreScenes is set.  For sets of this type cels other than page 0 cels are 
* not loaded into memory until such time as they are referenced for drawing.
*
*/


import java.util.Vector ;
import static java.lang.Thread.sleep;
import javax.swing.* ;


final class SceneTimer extends Thread
{
   static boolean enabled = true ;              // Timer enable state
   static boolean manualsuspend = false ;			// True, manual suspension
   static Vector workingset = new Vector() ;    // The set of loaded pages
   static Integer page = null ;						// Last page loaded
   static int workingsize = 3 ;                 // Number of pages in working set
   static int id = 0 ;                    		// SceneTimer identifier

   private Object suspendlock = new Object() ;	// Suspend lock
	private Thread thread = null ;					// The timer thread
	private Vector cels = null ; 	      			// Set of cels to monitor
   private boolean suspend = false ;    			// If true, suspend timer
   private boolean active = false ;					// If true, timer is active
   private boolean wait = false ;					// If true, timer is sleeping
   private long count = 0 ;							// Count of cels unloaded
   private int cycle = 0 ;                      // Garbage collection cycle


	// Constructor

	public SceneTimer()
   {
      id++ ;
      setName("SceneTimer-" + id) ;
   }


	// Class methods
   // -------------

   static void setEnabled(boolean b) { enabled = b ; }

   // Set the last page loaded during the PanelFrame draw.  We maintain a
   // working set of last loaded pages.  Some scene cels can reside on multiple 
   // scenes.

   static void setPage(Integer p) 
   { 
      page = p ; 
      int n = workingset.size() ;
      if (workingset.size() >= workingsize) 
         workingset.removeElementAt(n-1) ;
      workingset.insertElementAt(page,0) ;
   }


	// Timer methods
	// -------------

	// Method to stop the timer.

	void stopTimer()
   {
		if (thread != null)
      {
         if (OptionsDialog.getDebugControl())
			   System.out.println("Stop scene timer.") ;
   	   thread.interrupt() ;
      }
      cels = null ;
   }


	// Method to start the timer.

	void startTimer(Vector a)
   {
      if (a == null) return ;
      if (!OptionsDialog.getPagesAreScenes()) return ;
      cels = (Vector) a.clone() ;

      // Retain only cels that are on scenes.  These are cels that are
      // not specifically on page 0.

      for (int i = cels.size()-1 ; i >= 0 ; i--)
      {
         Object o = cels.elementAt(i) ;
         if (!(cels.elementAt(i) instanceof Cel))
            { cels.removeElementAt(i) ;  continue ; }
         if ((cels.elementAt(i) instanceof Video))
            { cels.removeElementAt(i) ;  continue ; }
         if ((cels.elementAt(i) instanceof JavaCel))
            { cels.removeElementAt(i) ;  continue ; }
         Cel cel = (Cel) o ;
         if (cel.isOnSpecificPage(0))
            { cels.removeElementAt(i) ;  continue ; }
      }

      // Start the scene unload activity if there are scenes.

      if (cels.size() == 0) cels = null ;
	   if (OptionsDialog.getDebugControl())
		   System.out.println("Start scene timer.") ;
      if (cels != null) start() ;
   }


	// Method to update the list of scene cels.  The timer must be
   // suspended before this routine is called.  If the thread is stopped
   // and cels exist it is started in a suspended mode.

	void updateCels(Vector a) { updateCels(a,true) ; }
	void updateCels(Vector a, boolean reset)
   {
      if (a == null) return ;
      if (!(suspend || thread == null)) return ;
      if (!OptionsDialog.getPagesAreScenes()) return ;
      Vector v = (Vector) a.clone() ;

      // Retain only objects that are on scenes.

      for (int i = v.size()-1 ; i >= 0 ; i--)
      {
         Object o = cels.elementAt(i) ;
         if (!(cels.elementAt(i) instanceof Cel))
            { cels.removeElementAt(i) ;  continue ; }
         if ((cels.elementAt(i) instanceof Video))
            { cels.removeElementAt(i) ;  continue ; }
         if ((cels.elementAt(i) instanceof JavaCel))
            { cels.removeElementAt(i) ;  continue ; }
         Cel cel = (Cel) o ;
         if (cel.isOnSpecificPage(0))
            { cels.removeElementAt(i) ;  continue ; }
      }

      // Add the new objects to the scene cel list. Objects
      // are added only if they do not currently exist.
      // The list is reset or reinitialized if necessary.

      if (cels == null || reset) cels = new Vector() ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (!cels.contains(o)) cels.addElement(o) ;
      }

      // Start the thread if necessary.

      if (cels.size() == 0) cels = null ;
      if (thread == null && cels != null)
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
			System.out.println("Suspend scene timer. " + ((manual) ? "Manual" : "")) ;
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
				System.out.println("Resume scene timer." + ((manual) ? "Manual" : "")) ;
   		suspend = false ;
		   suspendlock.notify() ;
      }
	}


   // Function to return the SceneTimer state.

   String getTimerState()
   {
      if (!enabled) return Kisekae.getCaptions().getString("AlarmDisableState") ;
      if (suspend) return Kisekae.getCaptions().getString("AlarmSuspendState") ;
      if (wait) return Kisekae.getCaptions().getString("AlarmWaitState") ;
      if (active) return Kisekae.getCaptions().getString("AlarmActiveState") ;
      return Kisekae.getCaptions().getString("AlarmStopState") ;
   }

   // Function to return the SceneTimer state.

   boolean isSuspended() { return suspend ; }
   boolean isActive() { return active ; }


   // Method to return the number of cels unloaded.

   long getCount() { return count ; }


   // Method to return the number of scene cels that we examine.

   long getQueueSize() { return (cels != null) ? cels.size() : 0 ; }



	// The timer thread code.  This code runs until the activity
	// is terminated or suspended.

	public void run()
	{
      int period = OptionsDialog.getScenePeriod() ;
		thread = Thread.currentThread() ;
		thread.setName("SceneTimer-" + id) ;
		if (OptionsDialog.getDebugControl())
			System.out.println(thread.getName() + " started.") ;

		// Run the timer loop until this activity is terminated.  Each time
      // through the loop we identify the current scene on display.

		while (true)
		{
         MainFrame mf = Kisekae.getMainFrame() ;
         PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
         PageSet ps = (pf != null) ? pf.getPage() : null ;
         Object cp = (ps != null) ? ps.getIdentifier() : null ;
         Integer scene = (cp instanceof Integer) ? (Integer) cp : null ;

         try
         {
         	if (cels == null) break ;              
   			for (int i = 0 ; i < cels.size() ; i++)
   			{
            	// Suspend timer execution if requested.  We restart after
               // a suspension because the cel vector is allowed to change.

					synchronized (suspendlock)
					{
                  boolean restart = false ;
						while (suspend)
						{
							active = false ;
               		if (OptionsDialog.getDebugControl())
                        System.out.println(thread.getName() + " suspended.") ;
							suspendlock.wait() ;
               		if (OptionsDialog.getDebugControl())
                        System.out.println(thread.getName() + " resumed.") ;
                     restart = true ;
						}
                  if (restart) break ;
   				}

   				// Access the object to determine the page load state.
               // Cels specifically on page 0 are not scene cels and should
               // not be in the cel list.  Cels in the working set of pages  
               // loaded to satisfy a PanelFrame draw are not unloaded.
               // Cels on the currently active scene are not unloaded.

               active = true ;
               if (!enabled) continue ;
               Object o = cels.elementAt(i) ;
               if (!(o instanceof Cel)) continue ;
					Cel c = (Cel) o ;
               if (!c.isLoaded()) continue ;
               if (c.isOnAllPage()) continue ;
               if (c.isOnSpecificPage(0)) continue ;             
               if (c.isOnSpecificPage(scene)) continue ; 
               
               boolean b = false ;
               for (int j = 0; j < workingset.size(); j++)
               {
                  Integer page = (Integer) workingset.elementAt(j) ;
                  if (c.isOnSpecificPage(page)) { b = true; break ; }
               }
               if (b) continue ;
               
               if (!OptionsDialog.getCacheImage())
               {
                  c.unload() ;
                  count++ ;
                  cycle++ ;
               }
            }

     			// Sleep a bit now that we have unloaded all scene cels in this
            // time cycle.  We terminate on receipt of an interrupt.

            if (cycle > 100) 
            {
               Runtime.getRuntime().gc() ;
               cycle = 0 ;
            }
            wait = true ;
	     		sleep(period) ;
            wait = false ;
			}
			catch (InterruptedException e) { break ; }

	      // Watch for memory faults.  If we run low on memory suspend
	      // the thread.

			catch (OutOfMemoryError e)
			{
            suspendTimer(true) ;
				Runtime.getRuntime().gc() ;
	         try { Thread.currentThread().sleep(300) ; }
	         catch (InterruptedException ex) { }
				System.out.println("SceneTimer: Out of memory.") ;
				JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
	           	"Insufficient memory.  Scene thread is suspended.",
	           	"Low Memory Fault",
	            JOptionPane.ERROR_MESSAGE) ;
			}
		}

      // Shut down.

		if (OptionsDialog.getDebugControl())
	      System.out.println(thread.getName() + " terminated.");
      thread = null ;
      cels = null ;
      active = false ;
      suspend = false ;
      wait = false ;
	}
}