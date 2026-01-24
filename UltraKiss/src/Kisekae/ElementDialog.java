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
import java.io.* ;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector ;
import java.util.Collections ;
import javax.swing.filechooser.FileFilter;

final class ElementDialog extends KissDialog
	implements ActionListener, WindowListener
{
	private JFrame parent = null ;			// Parent frame
	private String item = null ;           // The chosen element
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
   private JButton IMPORT = new JButton() ;
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();
	private FlowLayout flowLayout1 = new FlowLayout();
	private FlowLayout flowLayout2 = new FlowLayout();

   // Set up the event handlers.

	MouseListener mouseListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
            Object o = LIST.getSelectedValue() ;
            item = (o != null) ? o.toString() : null ;
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
      parent = frame ;

		// Populate the list with the vector contents.

		if (v != null) Collections.sort(v) ;
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
		IMPORT.addActionListener(this);
		LIST.addMouseListener(mouseListener);
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
		jLabel1.setText(Kisekae.getCaptions().getString("ElementDialogText"));
      IMPORT.setText(Kisekae.getCaptions().getString("ElementDialogImportText"));
      IMPORT.setToolTipText(Kisekae.getCaptions().getString("ToolTipImportText"));
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
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel2.add(jScrollPane1, BorderLayout.CENTER);
		jPanel3.add(jPanel2, BorderLayout.CENTER);
      jPanel4.add(IMPORT, null) ;
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
			Object o = LIST.getSelectedValue() ;
			item = (o != null) ? o.toString() : null ;
         ze = (o instanceof ArchiveEntry) ? (ArchiveEntry) o : null ;
      }

      if (source == CANCEL)
      {
         item = null ;
      }
      
      if (source == IMPORT)
      {
         item = null ;
         importFile() ;
      }

		// Close the window.

		flush() ;
		dispose() ;
	}

	// Return the selected item.

	public String getSelectedItem() { return item ; }

	// Return the selected file for import.

	public MemFile getSelectedFile() { return memfile ; }

	// Return the selected archive entry for import.

	public ArchiveEntry getSelectedEntry() { return ze ; }

	// Allow imports of new files.

	public void setAllowImport(boolean b) 
   { 
      IMPORT.setVisible(b) ; 
   }

	// Allow only imports of new files.

	public void setImportOnly(boolean b) 
   { 
      importonly = b ;
   }
   
   // Override the setVisible() method to manage the importonly option
   // for when the element selection list is not required.
   
   public void setVisible(boolean b)
   {
      if (!importonly)
         super.setVisible(b) ;
      else
         importFile() ;
   }


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
		IMPORT.removeActionListener(this);
		LIST.removeMouseListener(mouseListener);
		LIST.removeListSelectionListener(listListener);
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }

   // Required method for all KissDialog
   
   void setValues() 
   { 
      IMPORT.setVisible(false) ;
   }
   
   
   // Show a file dialog to choose a CNF file to import. This dialog is shown
   // if the user chooses to import a new CNF when looking at a list of
   // multiple configurations or if the importonly switch is set when this
   // ElementDialog is made visible.
   
   private MemFile importFile() 
   {
      JFileChooser jfd = new JFileChooser() ;
      jfd.setLocale(Kisekae.getCurrentLocale()) ;
      jfd.setDialogTitle(Kisekae.getCaptions().getString("ElementDialogCnfText")) ;
      jfd.setMultiSelectionEnabled(false) ;
         
      // Define the file filters. 
         
      cnffiles = new SimpleFilter(Kisekae.getCaptions().getString("CnfFilter"),ArchiveFile.getConfigurationExt());
      jfd.addChoosableFileFilter(cnffiles) ;
      jfd.setFileFilter(cnffiles) ;

      try
      {
         URL codebase = Kisekae.getBase() ;
         if (codebase == null) throw new SecurityException("unknown codebase") ;
         String directory = codebase.getFile() ;
         if (directory != null) jfd.setCurrentDirectory(new File(directory)) ;

      	// Center the dialog in the screen space.  The dialog is always set
         // to location (0,0) of the parent frame.  We center the dialog only
   		// if a parent frame was not specified.

         int fdoption = JFileChooser.CANCEL_OPTION ;
   		Dimension ds = new Dimension(500,400) ;
  			Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
  			int x = (ds.width < d.width) ? (d.width - ds.width) / 2 : 0 ;
  			int y = (ds.height < d.height) ? (d.height - ds.height) / 2 : 0 ;
     		Frame dialogframe = new Frame() ;
  			dialogframe.setLocation(x, y) ;
  			fdoption = jfd.showOpenDialog(dialogframe) ;

  			// See if we have a file to open.

         selected = jfd.getSelectedFile() ;
  			if ((selected == null) || (fdoption != JFileChooser.APPROVE_OPTION))
  	      {
  	      	jfd.setVisible(false) ;
  	         return null ;
  	      }
         
         // Read the file into memory.
       
         parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         PrintLn.println("Import configuration file: " + selected.getPath());
         int b = 0 ;
         InputStream is = new FileInputStream(selected) ;
         OutputStream os = new ByteArrayOutputStream((int) selected.length()) ;
         while ((b = is.read()) >= 0) { os.write(b) ; }
         is.close() ;
         os.close() ;

         // Create a memory byte array for this CNF file.

         if (os instanceof ByteArrayOutputStream)
         {
            ByteArrayOutputStream bos = (ByteArrayOutputStream) os ;
            memfile = new MemFile(selected.getPath(),bos.toByteArray()) ;
         }
      }     

      // Catch security exceptions.

      catch (SecurityException e)
      {
         PrintLn.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("SecurityException") + "\n" +
            Kisekae.getCaptions().getString("FileOpenSecurityMessage1"),
         	Kisekae.getCaptions().getString("SecurityException"),
            JOptionPane.ERROR_MESSAGE) ;
      }

		// Catch file import error exceptions.

		catch (IOException e)
		{
         PrintLn.println("KiSS file open exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(null, e.toString(),
         	Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Catch file error exceptions.

		catch (Exception e)
		{
         PrintLn.println("ElementDialog exception, " + e.toString()) ;
			JOptionPane.showMessageDialog(null, e.toString(),
         	Kisekae.getCaptions().getString("FileOpenException"),
            JOptionPane.ERROR_MESSAGE) ;
         e.printStackTrace() ;
		}
   
      // Return the entry to our memory file.
   
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      return memfile ;
   }   
}
