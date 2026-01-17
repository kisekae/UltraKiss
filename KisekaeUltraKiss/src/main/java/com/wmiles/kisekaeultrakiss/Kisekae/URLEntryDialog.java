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



import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.ResourceBundle ;
import javax.swing.* ;
import javax.swing.text.* ;


// Class to define the URL name entry dialog.  This is a modal dialog.

class URLEntryDialog extends KissDialog
	implements WindowListener, ActionListener
{
	private static Vector history = new Vector() ;

   // I18N attributes

   ResourceBundle captions = Kisekae.getCaptions() ;

   private JDialog me = null ;            // Reference to ourselves
   private String selected = null ;			// URL entry name

   // Control definitions

	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel3 = new JPanel();
   private JComboBox jCombo1 = new JComboBox() ;
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();


	// Constructor.

	public URLEntryDialog(JFrame parent)
	{
      super(parent,"",true) ;
      this.me = this ;
      setTitle(captions.getString("OpenURLTitle")) ;

      // Initialize the static history element.

      if (history.size() == 0)
      {
         history.addElement("http://") ;
      }

		// Set the frame characteristics.

		try { jbInit(); }
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

		// Center the dialog in the screen space.

 		setSize(500,150) ;
 		center(this) ;
      jCombo1.setSelectedIndex(0) ;

		// Setup to catch window events in this frame.

		addWindowListener(this) ;
      CANCEL.addActionListener(this) ;
      OK.addActionListener(this) ;
      
      JTextComponent editor = (JTextComponent) jCombo1.getEditor().getEditorComponent();
      editor.addKeyListener(new KeyAdapter() 
      {
         public void keyReleased(KeyEvent evt) 
         {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER)
               OK.doClick();
         }
      });
      
      setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;
      setVisible(true) ;
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
//		OK.setPreferredSize(new Dimension(85, 27));
		OK.setText(captions.getString("OkMessage"));
//		CANCEL.setPreferredSize(new Dimension(85, 27));
		CANCEL.setText(captions.getString("CancelMessage"));
      jCombo1 = new JComboBox(history) ;
      jCombo1.setEditable(true) ;

		jPanel1.setLayout(borderLayout2);
		jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		this.getContentPane().setLayout(borderLayout1);
		this.getContentPane().add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jCombo1, BorderLayout.NORTH);
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(OK, null);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(CANCEL, null);
      jPanel3.add(Box.createGlue()) ;
	}


	// Action Events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		// A OK request accepts the entry.

		if (source == OK)
		{
      	Object o = jCombo1.getSelectedItem() ;
         if (o instanceof String)
         {
         	selected = (String) o ;
            history.removeElement(selected) ;
            history.insertElementAt(selected,0) ;
         }
      	flush() ;
         dispose() ;
         return ;
		}

		// A Cancel request closes the dialog.

		if (source == CANCEL)
		{
      	selected = null ;
      	flush() ;
         dispose() ;
         return ;
		}
   }

   // Return selected URL

   String getSelected() { return selected ; }


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { selected = null ; flush() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
   	parent = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		jCombo1.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   void setValues() { }
}
