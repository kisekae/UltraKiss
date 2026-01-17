package com.wmiles.kisekaeultrakiss.Kisekae ;

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
* KissMenu class
*
* Purpose:
*
* This class is an abstract class for all Kiss menus.  It defines
* common methods and attributes for use within every menu.
*
* Different menus can be used depending on the situation context.
* Each menu should extend this class to ensure that common functions
* are maintained.
*
*/

import java.awt.* ;
import javax.swing.* ;


abstract class KissMenu
{
	// Object attributes

	protected MainFrame parent = null ;			// Reference to our main frame
	protected JMenuBar mb = null ;				// The menu bar
   protected int accelerator = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ;


	// This method returns the current FileOpen dialog object.
	// This dialog object contains the list of CNF file entries
	// in the compressed file.  The FileOpen dialog must be retained
	// when the configuration is loaded so that it can be referenced
	// if a new configuration is selected from the file.

	abstract FileOpen getFileOpen() ;

   // This method sets the menu FileOpen object.

	abstract void setFileOpen(FileOpen fd) ;

	// Return a reference to the help menu.

	abstract JMenu getHelpMenu() ;

   // Update the menu item state.

   abstract void update() ;

	// Return a reference to the actual menu bar object.

	JMenuBar getMenuBar() { return mb ; }


	// Toolbar and Menu shared event action methods
	// --------------------------------------------

	void eventNew(int type) { }
	void eventOpen() { }
	void eventUrl() { }
	void eventPortal() { }
	void eventWeb() { }
	void eventSelect() { }
	void eventClose() { }
	void eventCut() { }
	void eventCopy() { }
	void eventPaste() { }
	void eventMagnify(int n) { }
	void eventSound(boolean b) { }
	void eventMovie(boolean b) { }
	void eventSave(int n) { }
	void eventPage(int p) { }
	void eventColor(int c) { }
	void openContext(FileOpen fd, ArchiveEntry ze) { }
   void updateRunState() { }
}