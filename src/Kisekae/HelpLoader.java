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
* HelpLoader class
*
* Purpose:
*
* This class encapsulates the Help subsystem access functions. We retain
* static references to the HelpSetBroker as repeated allocations cause a
* memory leak.
*
*/

import javax.help.* ;
import javax.swing.* ;
import java.awt.* ;
import java.util.Hashtable ;
import java.awt.event.* ;
import java.net.URL ;


final public class HelpLoader
{
   private static Hashtable helpsets = new Hashtable() ;
   private static Hashtable brokers = new Hashtable() ;
   
	private KissFrame parent = null ;
   private String helpset = null ;
   private String helpsection = null ;
	private HelpSet hs = null ;
	private HelpBroker hb = null ;
   private boolean loaded = false ;

	// Constructor

   public HelpLoader(KissFrame frame, String hsname, String section)
   {
   	parent = frame ;
      helpset = hsname ;
      helpsection = section ;

      // Load the help system.

		try
		{
         if (Kisekae.getLoadBase() == null) throw new Exception("codebase unavailable") ;
			ClassLoader loader = this.getClass().getClassLoader() ;
//       ClassLoader loader = Thread.currentThread().getContextClassLoader() ; // Trusted Library
			URL hsURL = Kisekae.getResource(helpset) ;
         if (hsURL == null) throw new Exception("helpset URL unavailable") ;
         hs = (HelpSet) helpsets.get(helpset) ;
			if (hs == null) hs = new HelpSet(loader,hsURL) ;
         if (hs == null) throw new Exception("helpset object unavailable") ;
         helpsets.put(helpset,hs) ;
         hb = (HelpSetBroker) brokers.get(helpset) ;
			if (hb == null) hb = new HelpSetBroker(hs,parent) ;
         if (hb == null) throw new Exception("helpset broker unavailable") ;
         brokers.put(helpset,hb) ;
         hb.setSize(parent.getSize());
         JRootPane rootpane = parent.getRootPane() ;
         hb.enableHelpKey(rootpane,helpsection,null) ;
		}
		catch (Throwable e)
		{
			PrintLn.println("HelpSet " + helpset + " " + e.getMessage()) ;
         return ;
		}

      loaded = true ;
   }

   // A function to add an action listener to a button.

   public void addActionListener(AbstractButton b)
   {
      if (!loaded) return ;
   	if (b == null) return ;
		b.addActionListener(new CSH.DisplayHelpFromSource(hb)) ;
   }

   // A function to return the load status.

   public boolean isLoaded() { return loaded ; }

   // A function to return the display status.

   public boolean isVisible() 
   { 
      if (!loaded) return false ;
      if (!(hb instanceof HelpSetBroker)) return false ;
      return ((HelpSetBroker) hb).isVisible() ;
   }

   // A function to set the size of the help window.

   public void setSize(Dimension d)
   {
      if (!loaded) return ;
      if (hb != null) hb.setSize(d) ;
   }

   // A function to set the location of the help window.

   void setLocation(Point p)
   {
      if (!loaded) return ;
      if (hb != null) hb.setLocation(p) ;
   }

   // A function to hide or show the navigational view.

   void setViewDisplayed(boolean b)
   {
      if (!loaded) return ;
      if (hb != null) hb.setViewDisplayed(b) ;
   }

   // A function to hide or show the help presentation to the user.

   void setDisplayed(boolean b)
   {
      if (!loaded) return ;
      if (hb != null) hb.setDisplayed(b) ;
   }

   // A function to set a window close action listener.

   void setActionListener(ActionListener a)
   {
      if (!loaded) return ;
      if (hb != null) ((HelpSetBroker) hb).setActionListener(a) ;
   }
   
   // A function to close the help window.
   
   void close()
   {
      if (!loaded) return ;
      if (hb == null) return ;
      ((HelpSetBroker) hb).setDisplayed(false);
      Window w = ((HelpSetBroker) hb).getWindow() ;
      if (w != null) w.dispose() ;
      ((HelpSetBroker) hb).doWindowClosed() ;
   }
   
   // Clear our static tables.
   
   public static void clearTables()
   {
      brokers.clear() ;
      helpsets.clear() ;
   }
}
