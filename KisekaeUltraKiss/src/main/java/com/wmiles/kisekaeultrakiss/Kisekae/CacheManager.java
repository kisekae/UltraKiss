package com.wmiles.kisekaeultrakiss.Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.7.3  (June 8, 2024)
// Copyright:    Copyright (c) 2002-2024
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
* CacheManager class
*
* Purpose:
*
* This class manages the cache directory for individual cache file deletion.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.io.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

final class CacheManager extends KissDialog
	implements ActionListener, WindowListener
{
	private JFrame parent = null ;			// Parent frame
	private Object [] items = null ;       // The chosen elements
   private File selected = null ;         // The selected import file
   private MemFile memfile = null ;       // The memory file contents
   private ArchiveEntry ze = null ;       // The selected archive entry
   private FileFilter cnffiles = null ;   // FileChooser filter for CNF
   private boolean allowimport = false ;  // if true show import button
   private boolean importonly = false ;   // if true only show import dialog

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JLabel jLabel1 = new JLabel();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList LIST = new JList();
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();
	private FlowLayout flowLayout1 = new FlowLayout();
	private FlowLayout flowLayout2 = new FlowLayout();

   // Set up the event handlers.

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
        	OK.setEnabled(true) ;
		}
	} ;


	// Constructor

	public CacheManager(JFrame frame, String title, File [] v)
	{
		// Call the base class constructor to set up our frame

 		super(frame, title, true);
      parent = frame ;

		// Populate the list with the array contents.

      LIST = new JList(v) ;
      LIST.addListSelectionListener(listListener) ;
		OK.setEnabled(false) ;

      // Initialize the user interface.

		try { jbInit(); pack(); }
      catch(Exception ex)
      {
         ex.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + ex.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }

		// Center the frame in the panel space

		center(this) ;

		// Register for events.

		OK.addActionListener(this);
		CANCEL.addActionListener(this);
 		addWindowListener(this);
      
      setValues() ;
	}

   // User interface initialization.

	private void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel2.setLayout(borderLayout2);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(borderLayout3);
		jPanel4.setLayout(flowLayout1);
		jLabel1.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText(Kisekae.getCaptions().getString("ElementMultipleDialogText"));
  		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
  		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      LIST.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.SOUTH);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(OK, null);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(CANCEL, null);
      jPanel1.add(Box.createGlue()) ;
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel2.add(jScrollPane1, BorderLayout.CENTER);
		jPanel3.add(jPanel2, BorderLayout.CENTER);
      jPanel3.add(jPanel4, BorderLayout.SOUTH);
		panel1.add(jLabel1, BorderLayout.NORTH);
		jScrollPane1.getViewport().add(LIST, null);
	}


	// The action method is used to process control events.
   // Any action closes the frame.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

      if (source == OK)
      {
			items = LIST.getSelectedValues() ;
         deleteSelectedItems() ;
      }

      if (source == CANCEL)
      {
         items = null ;
      }
      
		// Close the window.

		flush() ;
		dispose() ;
	}

	// Return the selected items.

	public Object [] getSelectedItems() { return items ; }


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { flush() ; dispose() ; }
   
   
   private void deleteSelectedItems()
   {
      if (items == null) return ;
      int m = 0 ;
      int n = items.length ;
      for (int i = 0 ; i < n ; i++)
      {
         Object o = items[i] ;
         if (!(o instanceof File)) continue ;
         File f = (File) o ;
         boolean b = f.delete() ;
         if (b) m++ ;
      }
                  
      PrintLn.println("Cache is cleared, " + m + " files deleted out of " + n + " selected.") ;
      String s = Kisekae.getCaptions().getString("OptionsDialogCacheClearText2") + m ;
		JOptionPane.showMessageDialog(getParentFrame(), s,
         Kisekae.getCaptions().getString("OptionsDialogCacheClearTitle"),
         JOptionPane.INFORMATION_MESSAGE) ;
   }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      setVisible(false) ;
		OK.removeActionListener(this);
		CANCEL.removeActionListener(this);
		LIST.removeListSelectionListener(listListener);
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }

   // Required method for all KissDialog
   
   void setValues() { }
   
}
