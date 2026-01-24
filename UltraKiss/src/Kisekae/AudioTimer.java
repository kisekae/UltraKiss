package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.7  (December 6, 2023)
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
* AudioTimer class
*
* Purpose:
*
* This class is a timer activity to free memory allocations for audio clips.  
* An audio clip is not closed when stopped.  This can leave input streams open
* and other internal resources assigned.  We invoke this activity to close
* unused audio objects on a periodic basis.
*
*/


import java.util.Vector ;
import static java.lang.Thread.sleep;
import javax.swing.* ;


final class AudioTimer extends Thread
{
   static boolean enabled = true ;              // Timer enable state
   static boolean manualsuspend = false ;			// True, manual suspension
   static int id = 0 ;                    		// SceneTimer identifier

   private Object suspendlock = new Object() ;	// Suspend lock
	private Thread thread = null ;					// The timer thread
	private Vector audio = null ; 	      		// Set of audio objects to monitor
   private boolean suspend = false ;    			// If true, suspend timer
   private boolean active = false ;					// If true, timer is active
   private boolean wait = false ;					// If true, timer is sleeping
   private long count = 0 ;							// Count of audio objects unloaded


	// Constructor

	public AudioTimer()
   {
      id++ ;
      setName("AudioTimer-" + id) ;
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
			   PrintLn.println("Stop audio timer.") ;
   	   thread.interrupt() ;
      }
      audio = null ;
   }


	// Method to start the timer.

	void startTimer(Vector a)
   {
      if (a == null) return ;
      audio = (Vector) a.clone() ;

      // Start the audio unload activity if there are audio objects.

      if (audio.size() == 0) audio = null ;
	   if (OptionsDialog.getDebugControl())
		   PrintLn.println("Start audio timer.") ;
      if (audio != null) start() ;
   }


	// Method to update the list of audio objects.  The timer must be
   // suspended before this routine is called.  If the thread is stopped
   // and audio objects exist it is started in a suspended mode.

	void updateAudio(Audio a) 
   { 
      Vector v = new Vector() ;
      if (a != null) v.addElement(a);
      updateAudio(v,false) ; 
   }
	void updateAudio(Vector a) { updateAudio(a,true) ; }
	void updateAudio(Vector a, boolean reset)
   {
      if (a == null) return ;
      if (!(suspend || thread == null)) return ;
      Vector v = (Vector) a.clone() ;

      // Add the new objects to the scene cel list. Objects
      // are added only if they do not currently exist.
      // The list is reset or reinitialized if necessary.

      if (audio == null || reset) 
         audio = v ;
      else
      {
         for (int i = 0 ; i < v.size() ; i++)
         {
            Object o = v.elementAt(i) ;
            if (!audio.contains(o)) audio.addElement(o) ;
         }
      }

      // Start the thread if necessary.

      if (audio.size() == 0) audio = null ;
      if (thread == null && audio != null)
      {
         suspend = true ;
         new Thread(this).start() ;
         MainFrame mf = Kisekae.getMainFrame() ;
         Configuration config = (mf != null) ? mf.getConfig() : null ;
         if (config != null) config.setAudioTimer(this) ;
   		if (OptionsDialog.getDebugControl() && !manualsuspend)
   			PrintLn.println("Started audio timer through update. ") ;
      }
      else
      {
   		if (OptionsDialog.getDebugControl() && !manualsuspend)
   			PrintLn.println("Update audio timer. " + ((reset) ? "(Reset queue)" : "")) ;
         
      }
   }


	// Method to suspend the timer.  If an event is active we
	// will wait for it to finish executing.

	void suspendTimer() { suspendTimer(false) ; }
	void suspendTimer(boolean manual)
   {
      if (thread == null) return ;
		if (OptionsDialog.getDebugControl() && !manualsuspend)
			PrintLn.println("Suspend audio timer. " + ((manual) ? "Manual" : "")) ;
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
				PrintLn.println("Resume audio timer. " + ((manual) ? "Manual" : "")) ;
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


   // Method to return the number of audio objects closed.

   long getCount() { return count ; }


   // Method to return the number of scene cels that we examine.

   long getQueueSize() { return (audio != null) ? audio.size() : 0 ; }



	// The timer thread code.  This code runs until the activity
	// is terminated or suspended.

	public void run()
	{
      int period = OptionsDialog.getAudioPeriod() ;
		thread = Thread.currentThread() ;
		thread.setName("AudioTimer-" + id) ;
		if (OptionsDialog.getDebugControl())
			PrintLn.println(thread.getName() + " started.") ;

		// Run the timer loop until this activity is terminated.  

		while (true)
		{
         try
         {
         	if (audio == null) break ;              
            long time = System.currentTimeMillis() - Configuration.getTimestamp() ;
   			for (int i = 0 ; i < audio.size() ; i++)
   			{
            	// Suspend timer execution if requested.  We restart after
               // a suspension because the audio vector is allowed to change.

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

   				// Access the object to determine the last time it was played.
               // If it was recently played it is not closed.

               active = true ;
               if (!enabled) continue ;
               Object o = audio.elementAt(i) ;
               if (!(o instanceof Audio)) continue ;
					Audio a = (Audio) o ;
               if (a.isError()) continue ;
               if (!a.isOpen()) continue ;
               if (a.isStarted()) continue ;
               
               if (!OptionsDialog.getCacheAudio())
               {
                  long stoptime = a.getStopTime() ;
                  if (stoptime == 0) continue ;
                  if (time - stoptime < period) continue ;
                  if (OptionsDialog.getDebugSound())
                     PrintLn.println("AudioTimer: close " + a.getWriteName()) ;                            
                  a.close() ;                  
                  count++ ;
               }
            }

     			// Sleep a bit now that we have closed all audio objects in this
            // time cycle.  We terminate on receipt of an interrupt.

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
				PrintLn.println("AudioTimer: Out of memory.") ;
				JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
	           	"Insufficient memory.  Audio timer thread is suspended.",
	           	"Low Memory Fault",
	            JOptionPane.ERROR_MESSAGE) ;
			}
		}

      // Shut down.

		if (OptionsDialog.getDebugControl())
	      PrintLn.println(thread.getName() + " terminated.");
      thread = null ;
      audio = null ;
      active = false ;
      suspend = false ;
      wait = false ;
	}
}
