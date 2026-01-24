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
* RegisterDialog Class
*
* Purpose:
*
* This dialog is used to set the UltraKiss user identifier and password.
* These values are used for any URL download that requires authentication
* to a web server.
*
*/

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.* ;
import javax.swing.event.* ;


public class RegisterDialog extends KissDialog
   implements ActionListener
{
   private JPanel panel1 = new JPanel();
   private BorderLayout borderLayout1 = new BorderLayout();
   private JLabel jLabel1 = new JLabel();
   private JPanel jPanel1 = new JPanel();
   private JLabel jLabel2 = new JLabel();
   private JTextField userid = new JTextField();
   private JLabel jLabel3 = new JLabel();
   private JLabel jLabel4 = new JLabel();
   private JLabel jLabel5 = new JLabel();
   private JLabel jLabel6 = new JLabel();
   private JTextField website = new JTextField();
   private JPanel jPanel3 = new JPanel();
   private JPasswordField password = new JPasswordField();
   private JPanel jPanel2 = new JPanel();
   private JButton CANCEL = new JButton();
   private JButton OK = new JButton();
   private Border border1;
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private GridLayout gridLayout1 = new GridLayout(2,1);

   public RegisterDialog(JFrame frame, String title, boolean modal)
   {
      super(frame, title, modal);
      
      try
      { jbInit(); pack(); }
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

		// Center the frame in the panel space.

 		center(this) ;

		// Set initial values.

		userid.setText("") ;
      userid.setCaretPosition(0) ;
		password.setText("");
      password.setCaretPosition(0) ;
      website.setText(OptionsDialog.getKissWeb()) ;
      website.setCaretPosition(0) ;

		// Add listeners.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
   }

   public RegisterDialog()
   {
      this(null, "", false);
   }

   private void jbInit() throws Exception
   {
      border1 = BorderFactory.createEmptyBorder(10,10,10,10);
      panel1.setLayout(borderLayout1);
      jLabel1.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel1.setText(Kisekae.getCaptions().getString("UserRegistrationText"));
      jLabel5.setText(Kisekae.getCaptions().getString("WebBrowserTitle")) ;
      jLabel5.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));
      jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
      jLabel6.setText(Kisekae.getCaptions().getString("RegisterTitle2")) ;
      jLabel6.setBorder(BorderFactory.createEmptyBorder(5,0,10,0));
      jLabel6.setHorizontalAlignment(SwingConstants.CENTER);
      jPanel3.setLayout(gridLayout1) ;
//    jPanel3.add(jLabel1,null);
      jPanel3.add(jLabel5,null);
      jPanel3.add(jLabel6,null);
      jPanel1.setLayout(gridBagLayout1);
      jLabel2.setToolTipText("");
      jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel2.setText(Kisekae.getCaptions().getString("UserIDText")+":");
      jLabel3.setToolTipText("");
      jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel3.setText(Kisekae.getCaptions().getString("UserPasswordText")+":");
      jLabel4.setToolTipText("");
      jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
      jLabel4.setText(Kisekae.getCaptions().getString("KissWebText"));
      userid.setMinimumSize(new Dimension(100, 21));
      userid.setPreferredSize(new Dimension(100, 21));
      userid.setRequestFocusEnabled(true);
      userid.setToolTipText("");
      userid.setText(Kisekae.getUser());
      userid.setHorizontalAlignment(SwingConstants.LEFT);
      password.setMinimumSize(new Dimension(100, 21));
      password.setPreferredSize(new Dimension(100, 21));
      password.setToolTipText("");
      password.setText(Kisekae.getPassword());
      password.setHorizontalAlignment(SwingConstants.LEFT);
		website.setPreferredSize(new Dimension(200, 21));
      panel1.setPreferredSize(new Dimension(300, 250));
      CANCEL.setToolTipText("");
//		CANCEL.setPreferredSize(new Dimension(85, 27));
      CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
      OK.setToolTipText("");
//		OK.setPreferredSize(new Dimension(85, 27));
      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      jPanel2.setBorder(border1);
      getContentPane().add(panel1);
      panel1.add(jPanel3, BorderLayout.NORTH);
      panel1.add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(jLabel2,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
      jPanel1.add(userid,     new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      jPanel1.add(jLabel3,    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
      jPanel1.add(password,    new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      jPanel1.add(jLabel4,    new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
      jPanel1.add(website,    new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
      this.getContentPane().add(jPanel2, BorderLayout.SOUTH);
      jPanel2.add(OK, null);
      jPanel2.add(CANCEL, null);

  		// Set the default button for an enter key.

  		JRootPane rootpane = getRootPane()  ;
  		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{

			// An OK closes the dialog and applies the changes.

			if (source == OK)
			{
				Kisekae.setUser(userid.getText());
				char [] pw = password.getPassword() ;
				String s = new String(pw) ;
				Kisekae.setPassword(s);
            OptionsDialog.setKissWeb(website.getText()) ;
				dispose() ;
				return ;
			}

			// A CANCEL closes this dialog.

			if (source == CANCEL)
			{
				dispose() ;
				return ;
			}
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			PrintLn.println("RegisterDialog: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + e.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}
   
   void setValues() {
   }
   
}