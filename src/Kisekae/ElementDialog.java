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
* ElementDialog class
*
* Purpose:
*
* This class defines an instance of a general Kisekae data set element
* selection dialog.  This dialog is a list dialog used to select a specific
* object from a KiSS data set.  The candidate list objects are specified when
* this dialog is constructed.  The name of the selected item is returned
* through a getSelectedItem method call.
*
*/

import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.Vector ;
import java.util.Collections ;

final class ElementDialog extends KissDialog
	implements ActionListener, WindowListener
{
	private String item ;

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JLabel jLabel1 = new JLabel();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList LIST = new JList();
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();
	private FlowLayout flowLayout1 = new FlowLayout();

   // Set up the event handlers.

	MouseListener mouseListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
           	item = LIST.getSelectedValue().toString() ;
            flush() ;
         	dispose() ;
         }
		}
	} ;

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      {
        	OK.setEnabled(true) ;
		}
	} ;


	// Constructor

	public ElementDialog(JFrame frame, String title, Vector v)
	{
		// Call the base class constructor to set up our frame

 		super(frame, title, true);
		if (v != null) Collections.sort(v) ;

		// Populate the list with the vector contents.

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
		LIST.addMouseListener(mouseListener);
 		addWindowListener(this);
	}

   // User interface initialization.

	private void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel2.setLayout(borderLayout3);
		jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jLabel1.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText(Kisekae.getCaptions().getString("ElementDialogText"));
  		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
  		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.SOUTH);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(OK, null);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(CANCEL, null);
      jPanel1.add(Box.createGlue()) ;
		panel1.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jScrollPane1, BorderLayout.CENTER);
		panel1.add(jLabel1, BorderLayout.NORTH);
		jScrollPane1.getViewport().add(LIST, null);
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

		// An OK closes the frame

      if (source == OK)
      {
			Object o = LIST.getSelectedValue() ;
			if (o == null) return ;
			item = o.toString() ;
      }

      if (source == CANCEL)
      {
         item = null ;
      }

		// Close the window.

		flush() ;
		dispose() ;
	}

	// Return the selected item

	public String getSelectedItem() { return item ; }


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { flush() ; dispose() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      setVisible(false) ;
		OK.removeActionListener(this);
		CANCEL.removeActionListener(this);
		LIST.removeMouseListener(mouseListener);
		LIST.removeListSelectionListener(listListener);
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   void setValues() { }
}
