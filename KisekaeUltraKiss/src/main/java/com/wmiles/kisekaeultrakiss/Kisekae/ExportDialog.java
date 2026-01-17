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
* ExportDialog class
*
* Purpose:
*
* This class defines an instance of a the Kisekae export image dialog.  This 
* dialog is used to select parameters for writing the current page images
* as a set of image files.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.awt.image.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.net.URL ;
import java.io.* ;
import java.util.* ;

final class ExportDialog extends KissDialog implements ActionListener, WindowListener
{
	private static String dirname = null ;       // Last accessed directory
	private static String imagetype = "PNG" ;    // Last accessed type
	private static boolean exportall = false ;   // Last accessed export type
   
   private JFrame parent = null ;	     	      // Directory name
   private String directory = null ;	     	   // Directory name
   private Configuration config = null ;
   private PageSet startpage = null ;
   private PanelFrame panel = null ;
   private ArchiveFile zip = null ;

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private FlowLayout flowLayout1 = new FlowLayout();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JButton CANCEL = new JButton();
	private JButton OK = new JButton();
   private JButton DirBtn = new JButton() ;
	private JTextField WriteDirectory = new JTextField();
	private JTextField ImagePrefix = new JTextField();
   private JComboBox ExportBox = new JComboBox(OptionsDialog.exporttypes) ;
	private JCheckBox Transparent = new JCheckBox();
	private JCheckBox TruecolorType = new JCheckBox();
	private JRadioButton ExportAll = new JRadioButton();
	private JRadioButton ExportCurrent = new JRadioButton();

   // Set up the event handlers.



	// Constructor

	public ExportDialog(JFrame frame, String title)
	{
		// Call the base class constructor to set up our frame

 		super(frame, title, true);
      if (frame instanceof MainFrame) config = ((MainFrame) frame).getConfig() ;
      if (frame instanceof MainFrame) panel = ((MainFrame) frame).getPanel() ;
      if (config != null) zip = config.getZipFile() ;
      parent = frame ;
      

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
      setValues() ;

		// Register for events.

		OK.addActionListener(this);
		CANCEL.addActionListener(this);
 		addWindowListener(this);
	}

   // User interface initialization.

	private void jbInit() throws Exception
	{
		Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
		Border eb2 = BorderFactory.createEmptyBorder(10,0,0,0) ;
		Border eb3 = BorderFactory.createEmptyBorder(0,0,0,0) ;
      Border tb1 = new CompoundBorder(BorderFactory.createTitledBorder(Kisekae.getCaptions().getString("OptionsBoxTitle")),eb1) ;
		URL iconfile = Kisekae.getResource("Images/folder.gif") ;
      Icon folderIcon = (iconfile != null) ? new ImageIcon(iconfile) : null ;
		panel1.setLayout(borderLayout1);
		jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
		jPanel1.setBorder(eb1);
		jPanel2.setLayout(gridBagLayout1);
		jPanel2.setBorder(eb1);
		jPanel3.setLayout(gridBagLayout2);
		jPanel3.setBorder(tb1);
		jLabel1.setBorder(eb2);
		jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jLabel1.setText(Kisekae.getCaptions().getString("ExportDialogText"));
		jLabel2.setText(Kisekae.getCaptions().getString("ExportDirectory"));
		jLabel3.setText(Kisekae.getCaptions().getString("ExportPrefix"));
		jLabel4.setText(Kisekae.getCaptions().getString("MenuViewExportCel"));
      jLabel4.setToolTipText(Kisekae.getCaptions().getString("ToolTipExportCel"));
  		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
  		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      Transparent.setText(Kisekae.getCaptions().getString("ExportTransparent")) ;
      TruecolorType.setText(Kisekae.getCaptions().getString("ExportTruecolor")) ;
      ExportAll.setText(Kisekae.getCaptions().getString("ExportAll")) ;
      ExportCurrent.setText(Kisekae.getCaptions().getString("ExportCurrent")) ;

      String s = (zip != null) ? zip.getFileName() : "" ;
      if (s.indexOf('.') > 0) s = s.substring(0,s.indexOf('.')) ;
      ImagePrefix.setText("snapshot-") ;
		ImagePrefix.setPreferredSize(new Dimension(100, 21));
      if (directory == null) directory = dirname ;
      if (directory == null) directory = FileOpen.getDirectory() ;
      if (directory == null) directory = (zip != null) ? zip.getDirectoryName() : "" ;
      WriteDirectory.setText(directory) ;
      WriteDirectory.setCaretPosition(0) ;
		WriteDirectory.setPreferredSize(new Dimension(200, 21));
      DirBtn.setIcon(folderIcon) ;
      DirBtn.setBorder(eb3) ;
		DirBtn.setToolTipText(Kisekae.getCaptions().getString("ChooseDirectoryText"));
		DirBtn.addActionListener(this);
      ButtonGroup bg = new ButtonGroup() ;
      bg.add(ExportAll) ;
      bg.add(ExportCurrent) ;
		ExportBox.addActionListener(this);
		ExportAll.addActionListener(this);
		ExportCurrent.addActionListener(this);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.SOUTH);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(OK, null);
      jPanel1.add(Box.createGlue()) ;
      jPanel1.add(CANCEL, null);
      jPanel1.add(Box.createGlue()) ;
		panel1.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel2.add(ImagePrefix, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel2.add(WriteDirectory, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(DirBtn, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel2.add(jLabel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel2.add(ExportBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2.add(jPanel3, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 0, 0, 0), 0, 0));
		jPanel3.add(Transparent, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel3.add(TruecolorType, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
		jPanel3.add(ExportAll, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel3.add(ExportCurrent, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel1.add(jLabel1, BorderLayout.NORTH);
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

		// An OK closes the frame

      if (source == OK)
      {
         Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         saveimage() ;
         Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         close() ;
         return ;
      }

      if (source == CANCEL)
      {
         close() ;
         return ;
      }
         
      if (source == DirBtn)
      {
         JFileChooser fc = new JFileChooser() ;
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY) ;
         if (fc.showDialog(getParentFrame(),"OK") == JFileChooser.APPROVE_OPTION)
         {
            File f = fc.getSelectedFile() ;
            String s = (f != null) ? f.getPath() : null ;
            WriteDirectory.setText((f != null) ? f.getPath() : "") ;
            WriteDirectory.setCaretPosition(0) ;
            dirname = WriteDirectory.getText() ;
         }
         return ;
      }
      
      if (source == ExportBox)
      {
         Object o = ExportBox.getSelectedItem() ;
         if (o == null) return ;
         String s = o.toString() ;
         imagetype = s ;
         
         if ("bmp".equalsIgnoreCase(s))
         {
            Transparent.setEnabled(false) ;
            Transparent.setSelected(false) ;
            TruecolorType.setEnabled(true) ;
            TruecolorType.setSelected(true) ;
         }
         else if ("cel".equalsIgnoreCase(s))
         {
            Transparent.setEnabled(true) ;
            Transparent.setSelected(true) ;
            TruecolorType.setEnabled(false) ;
            TruecolorType.setSelected(true) ;
         }
         else if ("gif".equalsIgnoreCase(s))
         {
            Transparent.setEnabled(true) ;
            Transparent.setSelected(true) ;
            TruecolorType.setEnabled(false) ;
            TruecolorType.setSelected(false) ;
         }
         else if ("jpg".equalsIgnoreCase(s))
         {
            Transparent.setEnabled(false) ;
            Transparent.setSelected(false) ;
            TruecolorType.setEnabled(false) ;
            TruecolorType.setSelected(true) ;
         }
         else if ("png".equalsIgnoreCase(s))
         {
            Transparent.setEnabled(true) ;
            Transparent.setSelected(true) ;
            TruecolorType.setEnabled(true) ;
            TruecolorType.setSelected(true) ;
         }
         else
         {
            Transparent.setEnabled(false) ;
            Transparent.setSelected(false) ;
            TruecolorType.setEnabled(false) ;
            TruecolorType.setSelected(true) ;
         }
         return ;
      }
      
      if (source == ExportAll)
      {
         exportall = ExportAll.isSelected() ;
      }
      if (source == ExportCurrent)
      {
         exportall = !ExportCurrent.isSelected() ;
      }
	}


	// Utility function to write the page images.  

	private void saveimage()
	{
      Vector contents = new Vector() ;
      String prefix = ImagePrefix.getText() ;
      Object o = ExportBox.getSelectedItem() ;
      String type = (o != null) ? o.toString() : null ;
		if (type == null) return ;
      if (panel == null) return ;

      // Each page image is saved as a cel of the specified type.
      
      try
      {
         PageSet startpage = panel.getPage() ;
         String directory = WriteDirectory.getText() ;
         ArchiveFile zip = new DirFile(null,directory) ;
         
         while (true)
         {
            type = type.toLowerCase() ;
            PageSet page = panel.getPage() ;
            Object p = (page != null) ? page.getIdentifier() : null ;
            int n = (p instanceof Integer) ? ((Integer) p).intValue() : -1 ;
            if (n < 0) return ;
            
            // Process visible pages only

            if (page.isVisible())
            {
               Image img = panel.getImage() ;
               if (img == null) return ;
               int w = img.getWidth(null) ;
               int h = img.getHeight(null) ;
               BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ; 
               Graphics g = bi.getGraphics() ;
               g.drawImage(img,0,0,null) ;
               g.dispose() ;
               img = bi ;
               
               // Create filename and cel
               
               String filename = prefix + n + "." + type;
               ArchiveEntry ze = new DirEntry(directory,filename,zip) ;
               Cel c = Cel.createCel(zip,filename,null) ;
               if (c == null) return ;
         
               // Set image background.

               Color color = panel.getBackground() ;
               c.setImage(img) ;
               c.setBackgroundColor(color) ;
         
               // Set transparent color.

               if (Transparent.isSelected())
               {
                  c.setTransparentColor(color) ;
                  img = c.getImage() ;
               }
         
               // Dither image if required.

               if (!TruecolorType.isSelected())
               {
                  Object [] pkt = c.dither(255) ;
                  c.setPalette((Palette) pkt[1]) ;
                  img = (Image) pkt[0] ;
               }

               // Setup the cel for writing.  

               c.setImage(img) ;
               c.setZipEntry(ze) ;
               c.setLoaded(true) ;
               c.setUpdated(true) ;
               contents.addElement(c) ;
            }
            
            // Next page

            if (ExportCurrent.isSelected()) break ;
            panel.initpage((++n < config.getPageCount()) ? n : 0) ;
            panel.showpage() ;
            if (panel.getPage() == startpage) break ;
         }
         
         // Write all pages
         
         FileWriter fw = new FileWriter(parent,zip,contents) ;
         Thread thread = new Thread(fw) ;
         thread.start() ;
      }
      catch (IOException e)
      {
         PrintLn.println("ExportDialog: " + e) ;
      }
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { flush() ; dispose() ; }


	// Utility function to close the dialog.  

	void close()
   {
      flush() ;
      dispose() ;
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
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   void setValues() 
   { 
      OK.setEnabled(panel != null) ;
		ExportBox.setSelectedItem(imagetype);
      ExportAll.setSelected(exportall) ;
      ExportCurrent.setSelected(!exportall) ;
   }
}
