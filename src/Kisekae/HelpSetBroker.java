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
* HelpSetBroker class
*
* Purpose:
*
* This class extends the DefaultHelpBroker to customize our help system
* icon display.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import javax.help.* ;
import javax.swing.* ;


final class HelpSetBroker extends DefaultHelpBroker
{
   public WindowListener wl = new WindowAdapter() {
      public void windowClosing(WindowEvent evt) 
      { doWindowClosed() ; }
   } ;
   
   static ActionListener al = null ;
   static boolean visible = false ;
   
	// Constructor

   public HelpSetBroker() { super() ; }

   public HelpSetBroker(HelpSet hs) { super(hs) ; }

   public HelpSetBroker(HelpSet hs, Frame parent)
   {
   	super(hs) ;
      try
      {
         initPresentation() ;
         WindowPresentation pres = super.getWindowPresentation();
         Window frame = pres.getHelpWindow();
         if (frame == null) return ;
         frame.setIconImage(parent.getIconImage());
         frame.addWindowListener(wl); 
      }
      catch (Throwable e)
      {
         System.out.println("HelpSetBroker: Exception " + e.toString()) ;
         e.printStackTrace() ;
      }
   }
   
   private void doWindowClosed()
   {
      visible = false ;
      if (al == null) return ;
      ActionEvent evt = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"HelpWindowClosed") ;
      al.actionPerformed(evt) ;
   }
   
   public void setActionListener(ActionListener a) { al = a ; }
   
   public boolean isVisible() { return visible ; }
   
   public void setDisplayed(boolean b)
   {
      super.setDisplayed(b) ;
      visible = b ;
      WindowPresentation pres = super.getWindowPresentation();
      Window frame = pres.getHelpWindow();
      if (frame != null) frame.toFront();
   }
}
