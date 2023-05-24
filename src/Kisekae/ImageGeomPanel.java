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
* ImageGeomDialog class
*
* Purpose:
*
* This class is maintains the image geometry transformation user interface.
* This dialog can be used to adjust geometry factors such as orientation,
* scaling and shearing.
*
* Transform parameters for rotation degrees, scale, and shear factor
* are entered through this dialog.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.awt.image.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

final class ImageGeomPanel extends JPanel implements ActionListener
{
   private final int SHEARMIN = -200 ;
   private final int SHEARMAX = 200 ;
   private final int ROTATEMIN = -360 ;
   private final int ROTATEMAX = 360 ;
   private final int SCALEMIN = 0 ;
   private final int SCALEMAX = 100 ;

	// Dialog attributes

   private ImageFrame parent = null ;           // Our parent frame
   private int pane = 0 ;                       // Active tab pane
	private int shear = 0 ;           		      // The shear value
	private int rotate = 0 ;           		      // The rotation value
	private double scale = 1.0 ;           		// The scale factor
   private int initshear = 0 ;
   private int initrotate = 0 ;
   private double initscale = 1.0 ;
   private boolean updated = false ;            // True if values changed

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new JPanel();
	private JPanel jPanel8 = new JPanel();
   private JTabbedPane tabpane = new JTabbedPane() ;
   private JButton OK = new JButton() ;
   private JButton CANCEL = new JButton() ;
	private BorderLayout borderLayout1 = new BorderLayout();


	// Register for tab pane selection events.

	ChangeListener tabListener = new ChangeListener()
   {
		public void stateChanged(ChangeEvent e)
		{
         if (parent == null) return ;
         parent.updateTransformedImage() ;
         parent.showStatus("") ;
		}
	} ;


	// Constructor

   public ImageGeomPanel(JDialog f)
   { init(f,0) ; }

   public ImageGeomPanel(JFrame f)
   { init(f,0) ; }
   
   public ImageGeomPanel(JDialog f, int type)
   { init(f,type) ; }

	public ImageGeomPanel(JFrame f, int type)
	{ init(f,type) ; }

   private void init(Object f, int type)
   {
      pane = type ;
      if (f instanceof ImageFrame) parent = (ImageFrame) f ;

      // Construct the user interface.

		try { jbInit() ; }
		catch(Exception ex)
		{ ex.printStackTrace(); }

      // Select the proper tab for viewing.

      if (pane > tabpane.getTabCount()) pane = 0 ;
      tabpane.setSelectedIndex(pane) ;
      tabpane.addChangeListener(tabListener) ;

		// Center the frame in the panel space.

		Dimension s = getSize() ;
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
		int x = (s.width < d.width) ? (d.width - s.width) / 2 : 0 ;
		int y = (s.height < d.height) ? (d.height - s.height) / 2 : 0 ;
//		setLocation(x,y) ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
 	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		OK.setText(Kisekae.getCaptions().getString("ApplyMessage"));
      OK.setEnabled(false) ;
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));

 		jPanel4 = createRotatePanel() ;
      jPanel5 = createShearPanel() ;
      jPanel6 = createScalePanel() ;

      tabpane.add("Rotate",jPanel4) ;
      tabpane.add("Shear",jPanel5) ;
      tabpane.add("Scale",jPanel6) ;
      jPanel7.add(tabpane) ;

		this.add(panel1);
		panel1.add(jPanel7, BorderLayout.CENTER);
		panel1.add(jPanel8, BorderLayout.SOUTH);
  	   jPanel8.add(Box.createGlue()) ;
      jPanel8.add(OK, null) ;
      jPanel8.add(Box.createGlue()) ;
		jPanel8.add(CANCEL, null) ;
      jPanel8.add(Box.createGlue()) ;
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

      try
      {
			// An OK closes the frame.

			if (source == OK)
			{
            apply() ;
				return ;
			}

			// A CANCEL closes only this dialog and makes the parent visible.

			if (source == CANCEL)
			{
            reset() ;
				return ;
			}
      }
      catch (Exception e) { }
   }


   // Create rotate tab pane.

   private JPanel createRotatePanel()
   {
      final JLabel rotateLabel = new JLabel("Degrees:",SwingConstants.RIGHT) ;
      final JTextField rotateText = new JTextField(5) ;
      final JSlider rotateSlider = new JSlider() ;

      rotateText.setText(Integer.toString(rotate)) ;
      rotateText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               rotateSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      rotateSlider.setMinimum(ROTATEMIN) ;
      rotateSlider.setMaximum(ROTATEMAX);
      rotateSlider.setValue(rotate) ;
      rotateSlider.setMajorTickSpacing(120) ;
      rotateSlider.setMinorTickSpacing(12) ;
      rotateSlider.setPaintTicks(true) ;
      rotateSlider.setPaintLabels(true) ;
      rotateSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = rotate ;
            rotate = ((JSlider) e.getSource()).getValue() ;
            if (parent != null) parent.changeRotation((rotate/180.0) * Math.PI);
            rotateText.setText("" + rotate) ;
            updated = true ;
            OK.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel rotatePanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      rotatePanel.setLayout(gb);
      rotatePanel.setBackground(Color.lightGray);
      rotatePanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),"Rotate"));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(rotateLabel,c);
      rotatePanel.add(rotateLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(rotateText,c);
      rotatePanel.add(rotateText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(rotateSlider,c);
      rotatePanel.add(rotateSlider);
      return rotatePanel ;
   }

   // Create the shear tab pane.

   private JPanel createShearPanel()
   {
      final JLabel shearLabel = new JLabel("Shear Factor:",SwingConstants.RIGHT) ;
      final JTextField shearText = new JTextField(5) ;
      final JSlider shearSlider = new JSlider() ;

      shearText.setText(Integer.toString(shear)) ;
      shearText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               double value = (Double.valueOf(s)).doubleValue();
               shearSlider.setValue((int) (value * 100)) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      shearSlider.setMinimum(SHEARMIN) ;
      shearSlider.setMaximum(SHEARMAX);
      shearSlider.setValue(shear) ;
      shearSlider.setMajorTickSpacing(100) ;
      shearSlider.setMinorTickSpacing(25) ;
      shearSlider.setPaintTicks(true) ;
      shearSlider.setPaintLabels(true) ;
      shearSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = shear ;
            shear = ((JSlider) e.getSource()).getValue() ;
            if (parent != null) parent.changeShear(shear/100.0);
            shearText.setText(Double.toString(shear/100.0)) ;
            updated = true ;
            OK.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel shearPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      shearPanel.setLayout(gb);
      shearPanel.setBackground(Color.lightGray);
      shearPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),"Shear"));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(shearLabel,c);
      shearPanel.add(shearLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(shearText,c);
      shearPanel.add(shearText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(shearSlider,c);
      shearPanel.add(shearSlider);
      return shearPanel ;
   }

   // Create the scale tab pane.

   private JPanel createScalePanel()
   {
      final JLabel scaleLabel = new JLabel("Factor:",SwingConstants.RIGHT) ;
      final JTextField scaleText = new JTextField(5) ;
      final JSlider scaleSlider = new JSlider() ;

      scaleText.setText(Double.toString(scale)) ;
      scaleText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               double value = (Double.valueOf(s)).doubleValue() ;
               scaleSlider.setValue((int) (value * 10)) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      scaleSlider.setMinimum(SCALEMIN) ;
      scaleSlider.setMaximum(SCALEMAX);
      scaleSlider.setValue((int) (scale * 10)) ;
      scaleSlider.setMajorTickSpacing(20) ;
      scaleSlider.setMinorTickSpacing(5) ;
      scaleSlider.setPaintTicks(true) ;
      scaleSlider.setPaintLabels(true) ;
      
      scaleSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            double oldvalue = scale ;
            scale = (double) ((JSlider) e.getSource()).getValue() ;
            if (parent != null) parent.changeScale(scale/10.0) ;
            scaleText.setText(Double.toString(scale/10.0)) ;
            updated = true ;
            OK.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel scalePanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      scalePanel.setLayout(gb);
      scalePanel.setBackground(Color.lightGray);
      scalePanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),"Scale"));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(scaleLabel,c);
      scalePanel.add(scaleLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(scaleText,c);
      scalePanel.add(scaleText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(scaleSlider,c);
      scalePanel.add(scaleSlider);
      return scalePanel ;
   }


   // Utility functions to return the current values.

   boolean isChanged() { return updated ; }

   int getRotation() { return rotate ; }

   int getShear() { return shear ; }

   double getScale() { return scale ; }
   
   void setPane(int pane) 
   { 
      if (tabpane == null) return ;
      if (pane > tabpane.getTabCount()) return ;
      tabpane.setSelectedIndex(pane) ; 
      if (parent == null) return ;
      parent.updateTransformedImage() ;
      parent.showStatus("") ;
   }


   // The reset method reverts all updates to their initial values.

   void reset()
   {
      updated = false ;
      OK.setEnabled(false) ;
      if (parent == null) return ;

      rotate = 0 ;
      jPanel4 = createRotatePanel() ;
      tabpane.remove(0) ;
      tabpane.insertTab("Rotate", null, jPanel4, null, 0) ;
      parent.changeRotation(0);

      shear = 0 ;
      jPanel5 = createShearPanel() ;
      tabpane.remove(1) ;
      tabpane.insertTab("Shear", null, jPanel5, null, 1) ;
      parent.changeShear(0);

      scale = 1.0 ;
      jPanel6 = createScalePanel() ;
      tabpane.remove(2) ;
      tabpane.insertTab("Scale", null, jPanel6, null, 2) ;
      parent.changeScale(1.0);
      
      parent.showStatus("") ;
   }


   // The apply method commits the change.  An undo will revert the change.

   void apply()
   {
      if (parent == null) return ;
      
      ImageFrame saveparent = parent ;
      if (updated) parent.applyTransformedImage() ;
      parent = null ;
      int n = tabpane.getSelectedIndex() ;
      tabpane.removeAll() ;
      initrotate = rotate = 0 ;
      initshear = shear = 0 ;
      initscale = scale = 1.0 ;
      jPanel4 = createRotatePanel() ;
      jPanel5 = createShearPanel() ;
      jPanel6 = createScalePanel() ;
      tabpane.add("Rotate",jPanel4) ;
      tabpane.add("Shear",jPanel5) ;
      tabpane.add("Scale",jPanel6) ;
      tabpane.setSelectedIndex(n) ;
      parent = saveparent ;
      parent.showStatus("") ;
      updated = false ;
      OK.setEnabled(false) ;
   }

}