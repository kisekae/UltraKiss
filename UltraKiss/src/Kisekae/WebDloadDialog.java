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



import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.text.* ;
import javax.swing.border.*;
import java.util.* ;
import java.net.* ;
import java.io.* ;



// Class to define a download dialog prompt for the Web catalogue frame.

class WebDloadDialog extends KissDialog
 	implements ActionListener, WindowListener
{
   private KissFrame parent = null ;			// Reference to our parent dialog
   protected boolean cancel = true ;			// True if dialog cancelled
   protected boolean showrun = true ;			// True if run allowed
   private URL url = null ;                  // URL to download

   // User interface objects

   private JPanel panel1 = new JPanel() ;
  	private JPanel jPanel1 = new JPanel();
  	private JPanel jPanel2 = new JPanel();
  	private JButton OK = new JButton();
  	private JButton CANCEL = new JButton();
  	private JLabel jLabel1 = new JLabel();
  	private JLabel jLabel2 = new JLabel();
  	private JLabel jLabel3 = new JLabel();
   protected JRadioButton open = new JRadioButton() ;
   protected JRadioButton save = new JRadioButton() ;
   private ButtonGroup bGroup1 = new ButtonGroup() ;
  	private FlowLayout flowLayout1 = new FlowLayout() ;
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private GridLayout gridLayout1 = new GridLayout();
   private JPanel jPanel3 = new JPanel();
   private JLabel warnstw = new JLabel();
   private JCheckBox showdialog = new JCheckBox();
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
   private Border cb2 = new CompoundBorder(eb2,cb1) ;


  	// Constructor

  	public WebDloadDialog(KissFrame frame, URL url, boolean showrun)
  	{
  		super(frame,Kisekae.getCaptions().getString("DownloadFileTitle"),true);
      parent = frame ;
      this.url = url ;
      this.showrun = showrun ;

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

  		// Center the frame in the screen space.

 		center(this) ;

      // Set listeners and show the dialog.

  		OK.addActionListener(this);
  		CANCEL.addActionListener(this);
  		addWindowListener(this);
      setValues() ;
  	}

   // User interface initialization.

  	void jbInit() throws Exception
  	{
  		jPanel1.setLayout(flowLayout1);
  		jPanel2.setLayout(gridBagLayout1);
  		jPanel2.setBorder(cb2);
  		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
  		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));

  		jLabel1.setText(Kisekae.getCaptions().getString("DownloadFileText1"));
      jLabel1.setHorizontalAlignment(JLabel.CENTER) ;
      String name = url.toString() ;
      name = name.replaceFirst("[\\#\\?].*$","") ;  // no query or ref
      jLabel2.setText(name) ;
      jLabel2.setHorizontalAlignment(JLabel.CENTER) ;
      jLabel3.setText(Kisekae.getCaptions().getString("DownloadFileText2"));
      jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
      open.setText(Kisekae.getCaptions().getString("DownloadFileOpenText")) ;
      open.setEnabled(showrun) ;
      save.setText(Kisekae.getCaptions().getString("DownloadFileSaveText")) ;
      jPanel3.setLayout(gridLayout1);
      gridLayout1.setColumns(1);
      gridLayout1.setRows(2);
      showdialog.setText(Kisekae.getCaptions().getString("DownloadFileNoShow"));
      warnstw.setText(Kisekae.getCaptions().getString("DownloadWarning"));
      warnstw.setForeground(Color.RED) ;
      bGroup1.add(open) ;
      bGroup1.add(save) ;
      open.setSelected(showrun) ;
      save.setSelected(!showrun) ;

  		getContentPane().add(panel1);
      panel1.setLayout(new BorderLayout()) ;
  		jPanel2.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
  		jPanel2.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 30));
  		jPanel2.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
      jPanel2.add(jPanel3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel3.add(save, null);
      jPanel3.add(open, null);
      jPanel2.add(warnstw, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
      jPanel2.add(showdialog, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 0, 0, 0), 0, 0));
  		panel1.add(jPanel2, BorderLayout.CENTER);
  	   jPanel1.add(Box.createGlue()) ;
  		jPanel1.add(OK, null);
      jPanel1.add(Box.createGlue()) ;
  		jPanel1.add(CANCEL, null);
      jPanel1.add(Box.createGlue()) ;
      panel1.add(jPanel1, BorderLayout.SOUTH) ;
   }

   // Copy protected if URL has a fragment #nocopy 

   void setValues() 
   { 
      String ref = "" ;
      parent.setNoCopy(false) ;
      if (url != null) ref = url.getRef() ;
      if ("nocopy".equals(ref))
      {
         save.setEnabled(false) ;
         parent.setNoCopy(true) ;
      }
   }

   // Return the dialog show switch.

   static boolean showDialog()
   { return OptionsDialog.getShowDLPrompt() ; }

   // set the dialog show switch.

   static void enableDialog(boolean b)
   {
      OptionsDialog.setShowDLPrompt(b) ;
   }


  	// The action method is used to process control events.

  	public void actionPerformed(ActionEvent evt)
  	{
  	 	Object source = evt.getSource() ;

      // An OK request closes the dialog.

  		if (source == OK)
      {
        	cancel = false ;
         enableDialog(!showdialog.isSelected()) ;
         close() ;
         return ;
      }

      // A Cancel request closes the dialog.

  		if (source == CANCEL)
      {
        	cancel = true ;
         close() ;
         return ;
      }
   }

   // Method to close this dialog.

   void close()
   {
  		flush() ;
  		dispose() ;
  	}


  	// Window Events

  	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
  	public void windowClosed(WindowEvent evt) { }
  	public void windowIconified(WindowEvent evt) { }
  	public void windowDeiconified(WindowEvent evt) { }
  	public void windowActivated(WindowEvent evt) { }
  	public void windowDeactivated(WindowEvent evt) { }
  	public void windowClosing(WindowEvent evt) { close() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      setVisible(false) ;
 		OK.removeActionListener(this);
  		CANCEL.removeActionListener(this);
  		removeWindowListener(this);
  		getContentPane().removeAll() ;
  		getContentPane().removeNotify() ;
   }
}
