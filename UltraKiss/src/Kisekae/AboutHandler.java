package Kisekae;

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
* AboutHandler class
*
* Purpose:
*
* This class defines an interface to the Apple application menu.
* The Application Adaptor and other objects come from ui.jar that
* is released by Apple Systems with their Java implementation.
*
*/

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.Application;
import javax.swing.JFrame;

public class AboutHandler extends Application {

    public AboutHandler() {
        addApplicationListener(new AboutBoxHandler());
    }

    class AboutBoxHandler extends ApplicationAdapter {
       
         //Handle the About menu item
         public void handleAbout(ApplicationEvent event) {
            new AboutBox(new JFrame()).show();
            event.setHandled(true) ;
         }
        
         //Handle the Preferences menu item
         public void handlePreferences(ApplicationEvent event) {
            new OptionsDialog(new JFrame()).show();
            event.setHandled(true) ;
         }
         
         //Handle the Quit menu item
         public void handleQuit(ApplicationEvent event) {
            MainFrame mf = Kisekae.getMainFrame() ;
            if (mf != null) 
            {
               mf.exit() ;
               event.setHandled(true) ;
           }
         }    
    }
}

