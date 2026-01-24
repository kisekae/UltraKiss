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
* UserMenu class
*
* Purpose:
*
* This class encapsulates the menu bar for a user defined KiSS menu.
*
*/

import java.util.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;


final class UserMenu extends KissMenu
   implements ActionListener
{
	private FileOpen fd = null ;							// Our file open list

	// HelpSet declarations

	private String helpset = null ;
	private String helpsection = null ;
   private HelpLoader helper = null ;
   private JMenu helpmenu = null ;
   private JMenuItem help = new JMenuItem("Contents") ;
   private JMenuItem about = null ;



	// Constructor

	public UserMenu (MainFrame frame)
	{
		parent = frame ;
		mb = new JMenuBar() ;
	}


   // Method to add a new menu to the menu bar.  All menu items in
   // the specified group object are added to the new menu.

   void addMenu(String s, Group g)
   {
      if (s == null || s.length() == 0) return ;
      JMenu menu = new JMenu(s) ;
      mb.add(menu) ;
		String s1 = System.getProperty("java.version") ;
		int rm = (s1.indexOf("1.2") == 0) ? 2 : 26 ;
		Insets insets = new Insets(2,2,2,rm) ;
		menu.setMargin(insets) ;

      // Construct our help menu.

      if ("Help".equals(s))
      {
         helpmenu = menu ;
         if (helper != null)
         {
      		helpmenu.add(help) ;
      		helpmenu.addSeparator() ;
         }
      }

      // Define user menu items.

      if (g == null) return ;
      Vector cels = g.getCels() ;
      for (int i = 0 ; i < cels.size() ; i++)
      {
         Cel c = (Cel) cels.elementAt(i) ;
         if (!(c instanceof JavaCel)) continue ;
         Object [] pkt = ((JavaCel) c).getComponent() ;
         Component comp = (Component) pkt[0] ;
         if (comp instanceof JLabel)
         {
            s1 = ((JLabel) comp).getText() ;
            if ("menuSeparator".equalsIgnoreCase(s1)) menu.addSeparator() ;
         }
         else if (comp instanceof JMenuItem)
         {
            JMenuItem item = (JMenuItem) comp ;
            menu.add(item) ;
         }
      }

      // Append our about item to any help menu.

      if ("Help".equals(s))
      {
   		helpmenu.add((about = new JMenuItem("About UltraKiss..."))) ;
   		about.addActionListener(this);
      }
   }


	// Method to remove all entries from our menu.  This is required to
   // release references when a configuration is closed.

	void clearMenu()
	{
		if (!SwingUtilities.isEventDispatchThread())
      {
			Runnable runner = new Runnable()
			{ public void run() { clearMenu() ; } } ;
			javax.swing.SwingUtilities.invokeLater(runner) ;
         return ;
      }
      mb.removeAll() ;
   }


	// Implementation of the required KissMenu abstract method.  The FileOpen
   // object contains the list of CNF file entries in the compressed file.
   // The FileOpen dialog is retained when the configuration is loaded so
   // that it can be referenced if a new configuration is selected from the
   // file.

	FileOpen getFileOpen() { return null ; }

   // Implementation of the required KissMenu abstract method to restore the
   // fileopen reference.  Restoration must occur if a configuration load fails.

   void setFileOpen(FileOpen f) { fd = f ; }

	// Implementation of required KissMenu abstract method.
	
	JMenu getHelpMenu() { return helpmenu ; }

   // Implementation of the required menu item update.

   void update() {  }

   // Find the HelpSet file and create the HelpSet broker.

   void setHelpSet(String helpset, String helpsection)
   {
		if (Kisekae.isHelpInstalled() && helper == null)
		{
			helper = new HelpLoader(parent,helpset,helpsection) ;
			helper.addActionListener(help) ;
   		help.setEnabled(helper != null && helper.isLoaded()) ;
		}
   }

   // A utility function to open a loaded file according to its context.
   // Configuration elements initialize the main frame.

   void openContext(FileOpen fd, ArchiveEntry ze)
   {
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf == null) return ;
      MainMenu menu = mf.getMainMenu() ;
      if (menu == null) return ;
      menu.openContext(fd,ze);
   }



	// The action method is used to process control menu events.
	// This method is required as part of the ActionListener interface.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

      try
      {

   		// The Help About request brings up the About dialog window.

   		if (about == source)
   		{
   			if (parent != null) parent.getAboutDialog().show() ;
   			return ;
   		}
		}

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
			PrintLn.println("UserMenu: Out of memory.") ;
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			JOptionPane.showMessageDialog(parent,
				"Insufficient memory.  Action not completed.",
				"Low Memory Fault", JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			EventHandler.stopEventHandler() ;
			PrintLn.println("UserMenu: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			JOptionPane.showMessageDialog(parent,
            "Internal fault.  Data set will be closed." + "\n" + e.toString(),
            "Internal Fault", JOptionPane.ERROR_MESSAGE) ;
			parent.closeconfig() ;
		}
  	}
}
