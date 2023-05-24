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
import javax.swing.border.*;
import java.io.* ;

final class ZipDirectory extends KissDialog
	implements ActionListener, WindowListener
{
	private String directory = null ;         // The full directory path name
   private boolean success = false ;         // True if directory created

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private JLabel currentfolder = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JTextField newdirectory = new JTextField();
	private JPanel jPanel2 = new JPanel();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();


	// Constructor

	public ZipDirectory(JDialog frame, String dir)
	{
		// Call the base class constructor to set up our frame

 		super(frame,Kisekae.getCaptions().getString("ArchiveManagerDirectoryTitle"),true);
      directory = dir ;

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
      currentfolder.setText(directory);

		// Center the frame in the panel space

 		center(this) ;

		// Register for events.

		OK.addActionListener(this);
		CANCEL.addActionListener(this);
		newdirectory.addActionListener(this);
 		addWindowListener(this);
	}

   // User interface initialization.

	void jbInit() throws Exception
	{
		jPanel1.setLayout(gridBagLayout1);
		jLabel1.setText(Kisekae.getCaptions().getString("CurrentFolderText"));
		jLabel3.setText(Kisekae.getCaptions().getString("FolderNameText"));
		newdirectory.setPreferredSize(new Dimension(250, 21));
		jPanel2.setLayout(gridBagLayout2);
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
		currentfolder.setPreferredSize(new Dimension(0, 17));
		getContentPane().add(panel1);
		panel1.add(jPanel1, null);
		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(currentfolder, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel1.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 0, 5, 0), 0, 0));
		jPanel1.add(newdirectory, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel1.add(jPanel2, null);
		jPanel2.add(CANCEL, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
		jPanel2.add(OK, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

		// An OK closes the frame

		if (source == OK || source == newdirectory)
      {
         if (newdirectory.getText() == null) return ;
         File f = new File(directory,newdirectory.getText()) ;

         // Does the directory exist?

         if (f.exists())
         {
            JOptionPane.showMessageDialog(getParentFrame(),
               f.getAbsolutePath() + "\n" +
               Kisekae.getCaptions().getString("FDExistsError"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.WARNING_MESSAGE) ;
            return ;
         }

         // Do we have a valid directory?

         try { success = f.mkdirs() ; }
         catch (Exception e)
         {
            JOptionPane.showMessageDialog(getParentFrame(),
               f.getAbsolutePath() + "\n" + e.getMessage(),
               Kisekae.getCaptions().getString("FileOpenException"),
               JOptionPane.ERROR_MESSAGE) ;
            return ;
         }

         // Were we successful?

         if (!success)
         {
            JOptionPane.showMessageDialog(getParentFrame(),
               f.getAbsolutePath() + "\n" +
               Kisekae.getCaptions().getString("FileCreateError"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.WARNING_MESSAGE) ;
            return ;
         }

         // Done.  Close the dialog.

         directory = f.getAbsolutePath() ;
         close() ;
         return ;
      }

		if (source == CANCEL)
      {
      	directory = currentfolder.getText() ;
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

   // Method to return the selected directory.

   public String getDirectory() { return directory ; }


	// Window Events

	public void windowOpened(WindowEvent evt) { }
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
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   void setValues() {
   }
   
}

