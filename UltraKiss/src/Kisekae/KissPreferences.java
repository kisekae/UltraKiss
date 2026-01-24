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
* KissPreferences class
*
* Purpose:
*
* This class is worker class to save and restore the Preferences object.
* This object was introduced with Java 1.4 and we cannot use it on 
* earlier systems, as installed on Mac OS X.
* 
*/

import java.awt.* ;
import javax.swing.* ;
import java.util.prefs.* ;

final public class KissPreferences
{

	// Constructor

	public KissPreferences() { }


	// Close method to dispose of this frame. We update the preferences
   // unless the Tutorial help is on display.

	static public void closePreferences(JFrame frame) 
   {  
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null)
      {
         MainMenu menu = mf.getMainMenu() ;
         if (menu != null && menu.isTutorialDisplayed()) return ;
      }
      
      // Update the frame location preferences.
      
      try
      {
         Preferences prefs = Preferences.userNodeForPackage(frame.getClass()) ;
         String s = frame.getClass().getName() ;
         Point p = frame.getLocation() ;
         Dimension d = frame.getSize() ;
         prefs.putInt(s + " x", p.x) ;
         prefs.putInt(s + " y", p.y) ;
         prefs.putInt(s + " w", d.width) ;
         prefs.putInt(s + " h", d.height) ;
         prefs.flush() ; 
      }
      catch (Exception e) { }
   }

   
   // Open method to set frame size and location preferences.
   
   static public void openPreferences(JFrame frame)
   {
      Dimension screenArea = Toolkit.getDefaultToolkit().getScreenSize() ;
      int xd = 0 ;
      int yd = 0 ;
      int wd = screenArea.width ;
		int hd = (int)(screenArea.height*0.95f) ;
      
      try
      {
         Preferences prefs = Preferences.userNodeForPackage(frame.getClass()) ;
         String s = frame.getClass().getName() ;
         int x = prefs.getInt(s + " x", xd) ;
         int y = prefs.getInt(s + " y", yd) ;
         int w = prefs.getInt(s + " w", wd) ;
         int h = prefs.getInt(s + " h", hd) ;
         if (x < 0 || x >= screenArea.width-10) 
            { x = xd ; y = yd ; w = wd ; h = hd ; }
         if (y < 0 || y >= screenArea.height-10) 
            { x = xd ; y = yd ; w = wd ; h = hd ; }
         if (w < 10 || w > wd) 
            { x = xd ; y = yd ; w = wd ; h = hd ; }
         if (h < 10 || h > hd) 
            { x = xd ; y = yd ; w = wd ; h = hd ; }
         if (!OptionsDialog.getRetainWindowSize()) 
            { x = xd ; y = yd ; w = wd ; h = hd ; }
         xd = x ; yd = y ;
         wd = w ; hd = h ;
      }
      catch (Exception e) { }
      
      // Retain our default size.
      
      if (frame instanceof KissFrame)
         ((KissFrame) frame).setDefaultSize(new Dimension(wd,hd)) ;
      
      // Use the default frame size and location unless the Tutorial help is
      // on display.  In this case, use the current MainFrame size and location.
      
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null)
      {
         MainMenu menu = mf.getMainMenu() ;
         if (menu != null && menu.isTutorialDisplayed())
         {
            Point p = mf.getLocation() ;
            Dimension d = mf.getSize() ;
            xd = p.x ;
            yd = p.y ;
            wd = d.width ;
            hd = d.height ;
         }
      }
      
      frame.setIconImage(Kisekae.getIconImage()) ;
      frame.setLocation(xd,yd) ;
      frame.setSize(wd,hd) ;
    }
}