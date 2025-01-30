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
* StatusBar Class
*
* Purpose:
*
* This object encapsulates the main program status bar.  It is a
* panel that resides in the main frame window.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import javax.swing.* ;
import javax.swing.border.* ;



final class StatusBar extends JPanel
{
	// Class attributes

   static private int period = 5000 ;        // Update period
	private boolean statusBarOn = false ;     // Status bar view state
   private Thread thread = null ;            // Periodic mem update

   // User interface components

   private BorderLayout borderLayout1 = new BorderLayout();
   private JLabel statuslabel = new JLabel();
   private JLabel memlabel = new JLabel();


	// Constructor

	public StatusBar(Frame parent)
	{
      try { jbInit() ; }
      catch(Exception e)
      { e.printStackTrace() ; }
	}


   // User interface initialization.

   private void jbInit() throws Exception
   {
      this.setLayout(borderLayout1);
      Border b1 = BorderFactory.createLoweredBevelBorder() ;
      Border b2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
      Border b3 = BorderFactory.createCompoundBorder(b1,b2) ;
      statuslabel.setBorder(b3);
      memlabel.setBorder(b3);
      memlabel.setText("Mem:       0K");
      memlabel.addMouseListener(new java.awt.event.MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         { memlabel_mouseClicked(e); }
      });
      this.add(statuslabel, BorderLayout.CENTER);
      this.add(memlabel, BorderLayout.EAST);
      if (OptionsDialog.getAppleMac()) 
         setBackground(Color.LIGHT_GRAY) ;
  }

   
   public boolean getState() { return statusBarOn ; }
   

	// Method to display a status message.  This must run on the AWT thread.

	void showStatus(String s)
	{
      if (!statusBarOn) return ;
      if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { showStatus(s) ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }
      
		statuslabel.setText(new String(s)) ;
		repaint() ;
	}


	// Method to update our memory display.  This must run on the AWT thread.

	private void showMem()
	{
      if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable runner = new Runnable()
			{ public void run() { showMem() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }

      long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ;
		memlabel.setText("Mem: " + (int) (mem/1024) + "K") ;
		repaint() ;
	}


   // Method to set the status bar on.
   // If the bar is on, start the memory indicator update thread.

   void setStatusBar(boolean state)
   {
      statusBarOn = state ;
      if (state && thread == null)
      {
         thread = new Thread()
         {
            public void run()
            {
            	Thread.currentThread().setPriority(Thread.MIN_PRIORITY+1);
               while (true)
               {
                  try
                  {
                     showMem() ;
                     sleep(period) ;
                  }
                  catch (InterruptedException e) { break ; }
               }
            }
         } ;
         thread.start() ;
      }

      // If the bar is off, stop the memory bar update thread.

      if (!state)
      {
         if (thread != null) thread.interrupt() ;
         thread = null ;
      }
   }

   // Update memory if user clicks on memory label.   Note that this
   // event can be held if a garbage collection is currently in progress.

   void memlabel_mouseClicked(MouseEvent e) 
   { 
      if (thread == null)
         setStatusBar(true) ;
      else
         showMem() ; 
   }
}








