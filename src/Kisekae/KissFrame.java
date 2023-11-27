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
* KissFrame class
*
* Purpose:
*
* This class is an abstract class for all KiSS frames.
* 
* KiSS frames are used for independent program windows.
*
*/

import java.awt.* ;
import java.util.* ;
import javax.swing.* ;
import javax.swing.text.* ;

public abstract class KissFrame extends JFrame
{
	// Object attributes

	protected JFrame frame = null ;
   protected Dimension defaultsize = null ;
   private static Vector windows = new Vector() ;

	// Constructor

	public KissFrame() { super() ; frame = this ; }
	public KissFrame(String s) { super(s) ; frame = this ; }


	// Close method to dispose of this frame.

	public void close() 
   { 
		if (!SwingUtilities.isEventDispatchThread())
		{
			Runnable awt = new Runnable()
			{ public void run() { close() ; } } ;
			SwingUtilities.invokeLater(awt) ;
			return ;
		}
      
      windows.remove(this) ;
      if (Kisekae.isVolatileImage())
         KissPreferences.closePreferences(this) ;
      MainMenu mainmenu = null ;
      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe != null) mainmenu = mainframe.getMainMenu() ;
      if (mainmenu != null) mainmenu.updateRunState() ;                   
   }

   
   // Open method to set frame size and location preferences.
   
   public void open()
   {
      windows.add(this) ;
      if (Kisekae.isVolatileImage())
         KissPreferences.openPreferences(this) ;
      else
      {
         Dimension screenArea = Toolkit.getDefaultToolkit().getScreenSize() ;
         int wd = screenArea.width ;
         int hd = (int)(screenArea.height*0.95f) ;
         setIconImage(Kisekae.getIconImage()) ;
         setLocation(0,0) ;
         setSize(wd,hd) ;
      }
   }

	// Method to show an error dialog window.   This method contains a default
	// implementation to display an error message dialog box.

	void showError(String s) { showError(s,null) ; }
	void showError(String s, String highlite) 
	{
		if (frame == null) return ;
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
		JOptionPane.showMessageDialog(frame,s,"Error",JOptionPane.ERROR_MESSAGE) ;
	}


	// Method to show a standard dialog window.  These should be
	// overloaded if an implementation is required.

	void showFile(String s) { }
	void showText(String s) { }
	void showStatus(String s) { }
	void showWarning(String s) { }
	void showWarning(String s, String highlite) { }


	// Method to show a standard progress window.  These should be
	// overloaded if an implementation is required.

	void initProgress(int n) { }
	void updateProgress(int n) { }

   // Method to set our default window size.
   
   void setDefaultSize(Dimension d) { defaultsize = d ; }
   
   // Method to override the frame size to be our default size.
   
   public Dimension getDefaultSize()
   {
      if (defaultsize == null) return super.getSize() ;
      return defaultsize ;
   }
   
   // Method to return ourselves as the parent frame.
   
   JFrame getParentFrame() { return this ; }

	// Method to return our size as we are a KissFrame.

	Dimension getParentSize() { return getSize() ; } 
	Point getParentLocation() { return getLocation() ; } 

	// Method to center a window within our parent.  We
   // ensure that the window does not exceed the screen
   // limits.
   
   void center(Window w, Window parent)
   {
      Dimension s = w.getSize() ;
      Dimension ss = Toolkit.getDefaultToolkit().getScreenSize() ;
      Dimension d = (parent != null) ? parent.getSize() : ss ;
		Point p = (parent != null) ? parent.getLocation() : new Point(0,0) ;
		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
      int cx = p.x + x ;
      int cy = p.y + y ;
      if ((cx+s.width) > ss.width) cx = ss.width - (cx+s.width) ;
      if ((cy+s.height) > ss.height) cy = ss.height - (cy+s.height) ;
      if (cx < 0) cx = 0 ;
      if (cy < 0) cy = 0 ;
		w.setLocation(cx,cy) ;
   }


	// Methods to provide standard document functions.  These should be
	// overloaded if this frame handles text and an implementation 
   // is required.

	void setFocus() { requestFocus() ; }
	void setSelection(Point location, boolean up) { }
	void setSelection(int start, int end, boolean up) { }
	JTextComponent getTextComponent() { return null ; }
	Document getDocument() { return null ; }
   
   
   // Method to return our open window frame list.
   
   static Vector getWindowFrames() { return windows ; }
   
   
   // Method to apply image transformations for frames that manage images.  
   // These should be overloaded if an implementation is required.
   
   void applyTransformedImage(Image img) { }
   
   
   // Method to transfer a nocopy indicator from a URL download
   // to a frame menu.  To be overloaded by the frame.
   
   void setNoCopy(boolean b) { }
}
