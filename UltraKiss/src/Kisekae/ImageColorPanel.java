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
* ImageColorPanel class
*
* Purpose:
*
* This class is maintains the image color transformation user interface.
* This dialog can be used to adjust color factors such as brightness,
* contrast, gamma, red, green, and blue components.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.awt.image.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.util.Hashtable ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

final class ImageColorPanel extends JPanel implements ActionListener
{
   private final int BRIGHTNESSMIN = -100 ;
   private final int BRIGHTNESSMAX = 100 ;
   private final int CONTRASTMIN = -100 ;
   private final int CONTRASTMAX = 100 ;
   private final int GAMMAMIN = 0 ;
   private final int GAMMAMAX = 100 ;
   private final int REDMIN = -100 ;
   private final int REDMAX = 100 ;
   private final int GREENMIN = -100 ;
   private final int GREENMAX = 100 ;
   private final int BLUEMIN = -100 ;
   private final int BLUEMAX = 100 ;
   private final int ALPHAMIN = -100 ;
   private final int ALPHAMAX = 0 ;
	private final int INVERT = 12 ;
	private final int BRIGHTNESS = 13 ;
	private final int CONTRAST = 14 ;
	private final int GAMMA = 15 ;
	private final int POSTERIZE = 16 ;
	private final int RED = 17 ;
	private final int GREEN = 18 ;
	private final int BLUE = 19 ;
	private final int ALPHA = 20 ;

	// Dialog attributes

   private KissFrame parent = null ;            // Our parent frame
   private Image image = null ;                 // The image to transform
   private Image workimage = null ;             // The working image
   private Image baseimage = null ;             // The original image
   private ImagePreview preview = null ;        // The preview pane
   private RenderingHints hints = null ;        // Default rendering hints
   private int pane = 0 ;                       // Active tab pane
	private int brightness = 0 ;           		// The brightness value
	private int contrast = 0 ;           		   // The contrast value
	private double gamma = 1.0 ;           		// The gamma value
	private int red = 0 ;           		         // The red factor
	private int green = 0 ;           		      // The green factor
	private int blue = 0 ;           		      // The blue factor
	private int alpha = 0 ;           		      // The alpha factor
	private int initbrightness = 0 ;           	// The brightness value
	private int initcontrast = 0 ;           		// The contrast value
	private double initgamma = 1.0 ;           	// The gamma value
	private int initred = 0 ;           		   // The red factor
	private int initgreen = 0 ;           		   // The green factor
	private int initblue = 0 ;           		   // The blue factor
	private int initalpha = 0 ;           		   // The alpha factor
   private boolean updated = false ;            // True if changed
   
   // Transform arrays.
   
   private final int samples = 256 ;
	private final int max = samples - 1 ;
	private final float mid = max / 2.0f ;
   private byte [] ilut = new byte[samples] ;
   private byte [] clut = new byte[samples] ;
   private byte [] rlut = new byte[samples] ;
   private byte [] glut = new byte[samples] ;
   private byte [] blut = new byte[samples] ;
   private byte [] alut = new byte[samples] ;

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
	private JPanel jPanel9 = new JPanel();
   private JTabbedPane tabpane = new JTabbedPane() ;
   private JButton OK = new JButton() ;
   private JButton CANCEL = new JButton() ;
	private BorderLayout borderLayout1 = new BorderLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

   private JLabel brightnessLabel = new JLabel("Brightness Percent:",SwingConstants.RIGHT) ;
   private JTextField brightnessText = new JTextField(5) ;
   private JSlider brightnessSlider = new JSlider() ;
   private JLabel contrastLabel = new JLabel("Contrast Percent:",SwingConstants.RIGHT) ;
   private JTextField contrastText = new JTextField(5) ;
   private JSlider contrastSlider = new JSlider() ;
   private JLabel gammaLabel = new JLabel("Gamma:",SwingConstants.RIGHT) ;
   private JTextField gammaText = new JTextField(5) ;
   private JSlider gammaSlider = new JSlider() ;
   private JLabel redLabel = new JLabel("Red Percent:",SwingConstants.RIGHT) ;
   private JTextField redText = new JTextField(5) ;
   private JSlider redSlider = new JSlider() ;
   private JLabel greenLabel = new JLabel("Green Percent:",SwingConstants.RIGHT) ;
   private JTextField greenText = new JTextField(5) ;
   private JSlider greenSlider = new JSlider() ;
   private JLabel blueLabel = new JLabel("Blue Percent:",SwingConstants.RIGHT) ;
   private JTextField blueText = new JTextField(5) ;
   private JSlider blueSlider = new JSlider() ;
   private JLabel alphaLabel = new JLabel("Alpha Percent:",SwingConstants.RIGHT) ;
   private JTextField alphaText = new JTextField(5) ;
   private JSlider alphaSlider = new JSlider() ;


	// Register for tab pane selection events.

	ChangeListener tabListener = new ChangeListener()
   {
		public void stateChanged(ChangeEvent e)
		{
         int oldpane = pane ;
         pane = tabpane.getSelectedIndex() ;
         if (pane >= 3 && oldpane < 3)
         {
            workimage = image ;
            brightness = contrast = 0 ;
            gamma = 1 ;
            brightnessSlider.setValue(0) ;
            contrastSlider.setValue(0) ;
            gammaSlider.setValue(10) ;
         }
         if (pane < 3)
         {
            workimage = image ;
            red = green = blue = alpha = 0 ;
            brightness = contrast = 0 ;
            gamma = 1 ;
            redSlider.setValue(0) ;
            greenSlider.setValue(0) ;
            blueSlider.setValue(0) ;
            alphaSlider.setValue(0) ;
            brightnessSlider.setValue(0) ;
            contrastSlider.setValue(0) ;
            gammaSlider.setValue(10) ;
      
            // Initialize our transform arrays
      
            for (int i = 0; i < samples ; i++)
            {
               rlut[i] = (byte) i ;
               glut[i] = (byte) i ;
               blut[i] = (byte) i ;
               alut[i] = (byte) i ;
            }
         }
   	}
	} ;


	// Constructor

   public ImageColorPanel(KissFrame f)
   { init(f,null,null,0) ; }
   
   public ImageColorPanel(KissFrame f, Image img, ImagePreview prev)
   { init(f,img,prev,0) ; }

	public ImageColorPanel(KissFrame f, Image img, ImagePreview prev, int tab)
	{ init(f,img,prev,tab) ; }

   private void init(KissFrame f, Image img, ImagePreview prev, int tab)
   {
      pane = tab ;
      parent = f ;
      preview = prev ;
      setImage(img) ;

      // Construct the user interface.

		try { jbInit() ; }
		catch(Exception ex)
		{ ex.printStackTrace(); }

      // Select the proper tab for viewing.

      if (pane > tabpane.getTabCount()) pane = 0 ;
      tabpane.setSelectedIndex(pane) ;
      tabpane.addChangeListener(tabListener) ;

      // Create our rendering hints.

      Object antialiasinghint = RenderingHints.VALUE_ANTIALIAS_DEFAULT ;
      Object renderinghint = RenderingHints.VALUE_RENDER_DEFAULT ;
      Object ditheringhint = RenderingHints.VALUE_DITHER_DEFAULT ;
      Object interpolationhint = RenderingHints.VALUE_INTERPOLATION_BILINEAR ;
      hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,antialiasinghint) ;
      hints.put(RenderingHints.KEY_RENDERING,renderinghint) ;
      hints.put(RenderingHints.KEY_DITHERING,ditheringhint) ;
      hints.put(RenderingHints.KEY_INTERPOLATION,interpolationhint) ;
      
      // Initialize our transform arrays
      
  		for (int i = 0; i < samples ; i++)
  		{
         rlut[i] = (byte) i ;
         glut[i] = (byte) i ;
         blut[i] = (byte) i ;
         alut[i] = (byte) i ;
      }
      
		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
 	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
		panel1.setLayout(gridBagLayout1);
		OK.setText(Kisekae.getCaptions().getString("ApplyMessage"));
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
      OK.setEnabled(false) ;
      CANCEL.setEnabled(false) ;

      jPanel1 = createBrightnessPanel() ;
      jPanel2 = createContrastPanel() ;
      jPanel3 = createGammaPanel() ;
 		jPanel4 = createRedPanel() ;
      jPanel5 = createGreenPanel() ;
      jPanel6 = createBluePanel() ;
      jPanel9 = createAlphaPanel() ;

      tabpane.add("Brightness",jPanel1) ;
      tabpane.add("Contrast",jPanel2) ;
      tabpane.add("Gamma",jPanel3) ;
      tabpane.add("Red",jPanel4) ;
      tabpane.add("Green",jPanel5) ;
      tabpane.add("Blue",jPanel6) ;
      tabpane.add("Alpha",jPanel9) ;
      jPanel7.add(tabpane) ;

		this.add(panel1);
		panel1.add(jPanel7, new GridBagConstraints(0, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 10), 0, 0));
		panel1.add(OK, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(20, 10, 0, 10), 0, 0));
		panel1.add(CANCEL, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 0, 0));
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

      try
      {
			// An OK applies the current values.

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


   // Create brightness tab pane.

   private JPanel createBrightnessPanel()
   {
      brightnessLabel = new JLabel("Brightness Percent:",SwingConstants.RIGHT) ;
      brightnessText = new JTextField(5) ;
      brightnessSlider = new JSlider() ;

      brightnessText.setText(Integer.toString(brightness)) ;
      brightnessText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               brightnessSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      brightnessSlider.setMinimum(BRIGHTNESSMIN) ;
      brightnessSlider.setMaximum(BRIGHTNESSMAX);
      brightnessSlider.setValue(brightness) ;
      brightnessSlider.setMajorTickSpacing(50) ;
      brightnessSlider.setMinorTickSpacing(10) ;
      brightnessSlider.setPaintTicks(true) ;
      brightnessSlider.setPaintLabels(true) ;
      brightnessSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = brightness ;
            brightness = ((JSlider) e.getSource()).getValue() ;
            changeBrightness(brightness);
            brightnessText.setText("" + brightness) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel brightnessPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      brightnessPanel.setLayout(gb);
      brightnessPanel.setBackground(Color.lightGray);
      brightnessPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(brightnessLabel,c);
      brightnessPanel.add(brightnessLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(brightnessText,c);
      brightnessPanel.add(brightnessText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(brightnessSlider,c);
      brightnessPanel.add(brightnessSlider);
      return brightnessPanel ;
   }


   // Create contrast tab pane.

   private JPanel createContrastPanel()
   {
      contrastLabel = new JLabel("Contrast Percent:",SwingConstants.RIGHT) ;
      contrastText = new JTextField(5) ;
      contrastSlider = new JSlider() ;

      contrastText.setText(Integer.toString(contrast)) ;
      contrastText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               contrastSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      contrastSlider.setMinimum(CONTRASTMIN) ;
      contrastSlider.setMaximum(CONTRASTMAX);
      contrastSlider.setValue(contrast) ;
      contrastSlider.setMajorTickSpacing(50) ;
      contrastSlider.setMinorTickSpacing(10) ;
      contrastSlider.setPaintTicks(true) ;
      contrastSlider.setPaintLabels(true) ;
      contrastSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = contrast ;
            contrast = ((JSlider) e.getSource()).getValue() ;
            changeContrast(contrast);
            contrastText.setText("" + contrast) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel contrastPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      contrastPanel.setLayout(gb);
      contrastPanel.setBackground(Color.lightGray);
      contrastPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(contrastLabel,c);
      contrastPanel.add(contrastLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(contrastText,c);
      contrastPanel.add(contrastText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(contrastSlider,c);
      contrastPanel.add(contrastSlider);
      return contrastPanel ;
   }

   // Create the gamma tab pane.

   private JPanel createGammaPanel()
   {
      gammaLabel = new JLabel("Gamma:",SwingConstants.RIGHT) ;
      gammaText = new JTextField(5) ;
      gammaSlider = new JSlider() ;

      gammaText.setText(Double.toString(gamma)) ;
      gammaText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               double value = (Double.valueOf(s)).doubleValue() ;
               gammaSlider.setValue((int) (value * 10)) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      gammaSlider.setMinimum(GAMMAMIN) ;
      gammaSlider.setMaximum(GAMMAMAX);
      gammaSlider.setValue((int) (gamma * 10)) ;
      gammaSlider.setMajorTickSpacing(20) ;
      gammaSlider.setMinorTickSpacing(5) ;
      gammaSlider.setPaintTicks(true) ;
      gammaSlider.setPaintLabels(true) ;
      gammaSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            double oldvalue = gamma ;
            gamma = (double) ((JSlider) e.getSource()).getValue() ;
            changeGamma(gamma/10.0) ;
            gammaText.setText(Double.toString(gamma/10.0)) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;
      
      // Specify our own labels for decimal values.
      
      Hashtable labels = gammaSlider.createStandardLabels(20) ;
      labels.put(new Integer(20), new JLabel("2.0")) ;
      labels.put(new Integer(40), new JLabel("4.0")) ;
      labels.put(new Integer(60), new JLabel("6.0")) ;
      labels.put(new Integer(80), new JLabel("8.0")) ;
      labels.put(new Integer(100), new JLabel("10.0")) ;
      gammaSlider.setLabelTable(labels) ;

      // Layout the panel.

      JPanel gammaPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      gammaPanel.setLayout(gb);
      gammaPanel.setBackground(Color.lightGray);
      gammaPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(gammaLabel,c);
      gammaPanel.add(gammaLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(gammaText,c);
      gammaPanel.add(gammaText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(gammaSlider,c);
      gammaPanel.add(gammaSlider);
      return gammaPanel ;
   }


   // Create red tab pane.

   private JPanel createRedPanel()
   {
      redLabel = new JLabel("Red Percent:",SwingConstants.RIGHT) ;
      redText = new JTextField(5) ;
      redSlider = new JSlider() ;

      redText.setText(Integer.toString(red)) ;
      redText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               redSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      redSlider.setMinimum(REDMIN) ;
      redSlider.setMaximum(REDMAX);
      redSlider.setValue(red) ;
      redSlider.setMajorTickSpacing(50) ;
      redSlider.setMinorTickSpacing(10) ;
      redSlider.setPaintTicks(true) ;
      redSlider.setPaintLabels(true) ;
      redSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = red ;
            red = ((JSlider) e.getSource()).getValue() ;
            changeRed(red);
            redText.setText("" + red) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel redPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      redPanel.setLayout(gb);
      redPanel.setBackground(Color.lightGray);
      redPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(redLabel,c);
      redPanel.add(redLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(redText,c);
      redPanel.add(redText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(redSlider,c);
      redPanel.add(redSlider);
      return redPanel ;
   }


   // Create green tab pane.

   private JPanel createGreenPanel()
   {
      greenLabel = new JLabel("Green Percent:",SwingConstants.RIGHT) ;
      greenText = new JTextField(5) ;
      greenSlider = new JSlider() ;

      greenText.setText(Integer.toString(green)) ;
      greenText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               greenSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      greenSlider.setMinimum(GREENMIN) ;
      greenSlider.setMaximum(GREENMAX);
      greenSlider.setValue(green) ;
      greenSlider.setMajorTickSpacing(50) ;
      greenSlider.setMinorTickSpacing(10) ;
      greenSlider.setPaintTicks(true) ;
      greenSlider.setPaintLabels(true) ;
      greenSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = green ;
            green = ((JSlider) e.getSource()).getValue() ;
            changeGreen(green);
            greenText.setText("" + green) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel greenPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      greenPanel.setLayout(gb);
      greenPanel.setBackground(Color.lightGray);
      greenPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(greenLabel,c);
      greenPanel.add(greenLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(greenText,c);
      greenPanel.add(greenText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(greenSlider,c);
      greenPanel.add(greenSlider);
      return greenPanel ;
   }


   // Create blue tab pane.

   private JPanel createBluePanel()
   {
      blueLabel = new JLabel("Blue Percent:",SwingConstants.RIGHT) ;
      blueText = new JTextField(5) ;
      blueSlider = new JSlider() ;

      blueText.setText(Integer.toString(blue)) ;
      blueText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               blueSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      blueSlider.setMinimum(BLUEMIN) ;
      blueSlider.setMaximum(BLUEMAX);
      blueSlider.setValue(blue) ;
      blueSlider.setMajorTickSpacing(50) ;
      blueSlider.setMinorTickSpacing(10) ;
      blueSlider.setPaintTicks(true) ;
      blueSlider.setPaintLabels(true) ;
      blueSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = blue ;
            blue = ((JSlider) e.getSource()).getValue() ;
            changeBlue(blue);
            blueText.setText("" + blue) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel bluePanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      bluePanel.setLayout(gb);
      bluePanel.setBackground(Color.lightGray);
      bluePanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(blueLabel,c);
      bluePanel.add(blueLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(blueText,c);
      bluePanel.add(blueText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(blueSlider,c);
      bluePanel.add(blueSlider);
      return bluePanel ;
   }


   // Create alpha tab pane.

   private JPanel createAlphaPanel()
   {
      alphaLabel = new JLabel("Alpha Percent:",SwingConstants.RIGHT) ;
      alphaText = new JTextField(5) ;
      alphaSlider = new JSlider() ;

      alphaText.setText(Integer.toString(alpha)) ;
      alphaText.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            try
            {
               String s = ((JTextField) e.getSource()).getText() ;
               int value = (Integer.valueOf(s)).intValue() ;
               alphaSlider.setValue(value) ;
            }
            catch (Exception ex) { }
         }
      } ) ;

      alphaSlider.setMinimum(ALPHAMIN) ;
      alphaSlider.setMaximum(ALPHAMAX);
      alphaSlider.setValue(alpha) ;
      alphaSlider.setMajorTickSpacing(50) ;
      alphaSlider.setMinorTickSpacing(10) ;
      alphaSlider.setPaintTicks(true) ;
      alphaSlider.setPaintLabels(true) ;
      alphaSlider.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            int oldvalue = alpha ;
            alpha = ((JSlider) e.getSource()).getValue() ;
            changeAlpha(alpha);
            alphaText.setText("" + alpha) ;
            updated = true ;
            OK.setEnabled(true) ;
            CANCEL.setEnabled(true) ;
         }
      } ) ;

      // Layout the panel.

      JPanel alphaPanel = new JPanel() ;
      GridBagLayout gb = new GridBagLayout() ;
      GridBagConstraints c = new GridBagConstraints();
      alphaPanel.setLayout(gb);
      alphaPanel.setBackground(Color.lightGray);
      alphaPanel.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(),""));

      c.weighty = 1.0 ; c.weightx = 1.0 ;
      c.insets = new Insets(0,5,5,5) ;
      gb.setConstraints(alphaLabel,c);
      alphaPanel.add(alphaLabel);
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.fill = GridBagConstraints.HORIZONTAL;
      gb.setConstraints(alphaText,c);
      alphaPanel.add(alphaText);
      c.gridx = 0 ; c.gridy = 1 ;
      gb.setConstraints(alphaSlider,c);
      alphaPanel.add(alphaSlider);
      return alphaPanel ;
   }


   // Utility functions to return the current values.

   boolean isChanged() { return updated ; }

   int getBrightness() { return brightness ; }

   int getContrast() { return contrast ; }

   double getGamma() { return gamma ; }

   int getRed() { return red ; }

   int getGreen() { return green ; }

   int getBlue() { return blue ; }

   int getAlpha() { return alpha ; }

   
   // Ensure our source is a 4 channel buffered image.

   void setImage(Image img) 
   { 
      baseimage = img ;
      int w = (img != null) ? img.getWidth(null) : 0 ;
      int h = (img != null) ? img.getHeight(null) : 0 ;
      image = makeBufferedImage(img,w,h,BufferedImage.TYPE_INT_ARGB) ;
      workimage = image ; 
   }

   
   // Enable our controls.

   public void setEnabled(boolean b) 
   { 
      tabpane.setEnabled(b) ;
      OK.setEnabled(b) ;
      CANCEL.setEnabled(b) ;
      brightnessSlider.setEnabled(b) ;
      contrastSlider.setEnabled(b) ;
      gammaSlider.setEnabled(b) ;
      redSlider.setEnabled(b) ;
      greenSlider.setEnabled(b) ;
      blueSlider.setEnabled(b) ;
      alphaSlider.setEnabled(b) ;
      brightnessText.setEnabled(b) ;
      contrastText.setEnabled(b) ;
      gammaText.setEnabled(b) ;
      redText.setEnabled(b) ;
      greenText.setEnabled(b) ;
      blueText.setEnabled(b) ;
      alphaText.setEnabled(b) ;
   }

   // Set the ImagePreview pane for displaying the image.

   void setPreview(ImagePreview p) 
   { 
      preview = p ;
   }

   // Set the Rendering Hints for the image quality.

   void setHints(RenderingHints h) 
   { 
      hints = h ;
   }

   // Set the tab on display (red, green, blue, brightness, contrast, gamma).

   void setPane(int pane) 
   { 
      if (tabpane == null) return ;
      if (pane > tabpane.getTabCount()) return ;
      tabpane.setSelectedIndex(pane) ; 
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeBrightness(int n)
   {
      image = createLookupTransform(workimage,BRIGHTNESS,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeContrast(int n)
   {
      image = createLookupTransform(workimage,CONTRAST,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeGamma(double n)
   {
      image = createLookupTransform(workimage,GAMMA,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeRed(double n)
   {
      image = createLookupTransform(workimage,RED,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeGreen(double n)
   {
      image = createLookupTransform(workimage,GREEN,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeBlue(double n)
   {
      image = createLookupTransform(workimage,BLUE,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // Utility functions to apply an image transformation relative to the
   // existing base image.  The transformation does not update the base image.

   private void changeAlpha(double n)
   {
      image = createLookupTransform(workimage,ALPHA,n) ;
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
   }


   // A function to perform a lookup transform on the specified image.
   // This returns a new image modified from the source ARGB image,

   private BufferedImage createLookupTransform(Image img, int type, double param)
   {
      BufferedImage image = null ;
      BufferedImage source = null ;
      byte [] lut = new byte[samples] ;
      byte [][] bandadjust = null ;
      if (!(img instanceof BufferedImage)) return null ;
      source = (BufferedImage) img ;
      if (source.getType() != BufferedImage.TYPE_INT_ARGB) return null ;

      // Create our convolution kernel.

      switch (type)
      {
         case INVERT:
            for (int i = 0 ; i < samples ; i++)
               lut[i] = (byte) (samples - i) ;
            break ;


         case POSTERIZE:
            for (int i = 0 ; i < samples ; i++)
               lut[i] = (byte) (i - (i % 32)) ;
            break ;


         // The amount of brightness adjustment is given as a percentage
         // between -100 and 100.  -100 will make the resulting image black,
         // 0 will leave it unchanged, 100 will make it white.

         case BRIGHTNESS:
            if (param < -100 || param > 100) break ;
      		for (int i = 0; i < samples ; i++)
      		{
        			if (param < 0)
      				lut[i] = (byte) ((float) i  * (100.0f + param) / 100.0f);
      			else
      				lut[i] = (byte) (i + (max - i) * param / 100.0f);
            }
            break ;


         // The amount of contrast adjustment is given as a percentage
         // between -100 and 100.  -100 will make the resulting image
         // gray, 0 will leave it unchanged, 100 will map it to the eight
         // corners of the color cube.

         case CONTRAST:
            if (param < -100 || param > 100) break ;
      		for (int i = 0 ; i < samples ; i++)
      		{
      			if (param < 0)
      			{
      				if (i < mid)
      					lut[i] = (byte) (i + (mid - i) * (-param) / 100.0f);
      				else
      					lut[i] = (byte) (mid + (i - mid) * (100.0f + param) / 100.0f);
      			}
      			else
      			{
      				if (i < mid)
      					lut[i] = (byte) (i * (100.0f - param) / 100.0f);
      				else
      					lut[i] = (byte) (i + (max - i) * param / 100.0f);
      			}
      		}
            break ;


         // Changes to image intensity are made by applying the formula
         // f(x) = MAX * (x / MAX) exp(1 / gamma)
         // Gamma values must be greater than 0 and less than 10.

         case GAMMA:
            if (param <= 0 || param > 10) break ;
            double m = max ;
      		double g = 1.0 / param ;
      		for (int i = 0 ; i < samples ; i++)
      		{

      			int result = (int) Math.round((m * Math.pow((i/m),g)));
      			if (result < 0) result = 0 ;
      			if (result > max) result = max ;
               lut[i] = (byte) result ;
      		}
            break ;


         // The amount of red adjustment is given as a percentage between
         // 0 and 100.  -100 removes all red from the image, 0 will leave
         // it unchanged, 100 will ensure that the red component is fully
         // saturated.

         case RED:
            if (param < -100 || param > 100) break ;
      		for (int i = 0; i < samples ; i++)
      		{
        			if (param < 0)
      				rlut[i] = (byte) ((float) i  * (100.0f + param) / 100.0f);
      			else
      				rlut[i] = (byte) (i + (max - i) * param / 100.0f);
            }
            bandadjust = new byte[][] { rlut, glut, blut, alut } ;
            break ;


         // The amount of green adjustment is given as a percentage between
         // 0 and 100.  -100 removes all green from the image, 0 will leave
         // it unchanged, 100 will ensure that the green component is fully
         // saturated.

         case GREEN:
            if (param < -100 || param > 100) break ;
      		for (int i = 0; i < samples ; i++)
      		{
        			if (param < 0)
      				glut[i] = (byte) ((float) i  * (100.0f + param) / 100.0f);
      			else
      				glut[i] = (byte) (i + (max - i) * param / 100.0f);
            }
            bandadjust = new byte[][] { rlut, glut, blut, alut } ;
            break ;


         // The amount of blue adjustment is given as a percentage between
         // 0 and 100.  -100 removes all blue from the image, 0 will leave
         // it unchanged, 100 will ensure that the blue component is fully
         // saturated.

         case BLUE:
            if (param < -100 || param > 100) break ;
      		for (int i = 0; i < samples ; i++)
      		{
        			if (param < 0)
      				blut[i] = (byte) ((float) i  * (100.0f + param) / 100.0f);
      			else
      				blut[i] = (byte) (i + (max - i) * param / 100.0f);
            }
            bandadjust = new byte[][] { rlut, glut, blut, alut } ;
            break ;


         // The amount of blue adjustment is given as a percentage between
         // 0 and 100.  -100 removes all blue from the image, 0 will leave
         // it unchanged, 100 will ensure that the blue component is fully
         // saturated.

         case ALPHA:
            if (param < -100 || param > 100) break ;
      		for (int i = 0; i < samples ; i++)
      		{
        			if (param < 0)
      				alut[i] = (byte) ((float) i  * (100.0f + param) / 100.0f);
      			else
      				alut[i] = (byte) (i + (max - i) * param / 100.0f);
            }
            bandadjust = new byte[][] { rlut, glut, blut, alut } ;
            break ;

         default:
            break ;
      }

      // Apply the transform.  Create a new transform image.

      try
      {
         ByteLookupTable blt = (bandadjust != null)
            ? new ByteLookupTable(0,bandadjust) : new ByteLookupTable(0,lut) ;
         LookupOp op = new LookupOp(blt,hints) ;
         image = op.filter(source,image) ;
      }
      catch (Exception e)
      {
         PrintLn.println("ImageColorPanel: transform exception " + e) ;
      }
      return image ;
   }


   // Utility function to ensure that an AWT image is ARGB or RGB.  

   private BufferedImage makeBufferedImage(Image img, int w, int h, int type)
   {
      if (w <= 0 || h <=0) return null ;
      BufferedImage image = new BufferedImage(w,h,type) ;

      // Set the background as transparent.
/*
		int [] rgbarray = new int[w*h] ;
		for (int i = 0 ; i < w*h ; i++) rgbarray[i] = 1 ;
		image.setRGB(0,0,w,h,rgbarray,0,w) ;
*/
      // Apply the required image.

      if (img == null) return image ;
      Graphics2D gc = image.createGraphics() ;
      gc.setComposite(AlphaComposite.Src);
      gc.drawImage(img,0,0,null) ;
      gc.dispose() ;
      return image ;
   }



   // The reset method reverts all updates to their initial values.

   void reset()
   {
      updated = false ;
      setImage(baseimage) ;
      
      // Initialize our transform arrays
      
  		for (int i = 0; i < samples ; i++)
  		{
         rlut[i] = (byte) i ;
         glut[i] = (byte) i ;
         blut[i] = (byte) i ;
         alut[i] = (byte) i ;
      }
      
      // Rebuild the interface
      
      int n = tabpane.getSelectedIndex() ;
      tabpane.removeAll() ;
      initbrightness = brightness = 0 ;
      initcontrast = contrast = 0 ;
      initgamma = gamma = 1.0 ;
      initred = red = 0 ;
      initgreen = green = 0 ;
      initblue = blue = 0 ;
      initalpha = alpha = 0 ;
      jPanel1 = createBrightnessPanel() ;
      jPanel2 = createContrastPanel() ;
      jPanel3 = createGammaPanel() ;
      jPanel4 = createRedPanel() ;
      jPanel5 = createGreenPanel() ;
      jPanel6 = createBluePanel() ;
      jPanel9 = createAlphaPanel() ;
      tabpane.add("Brightness",jPanel1) ;
      tabpane.add("Contrast",jPanel2) ;
      tabpane.add("Gamma",jPanel3) ;
      tabpane.add("Red",jPanel4) ;
      tabpane.add("Green",jPanel5) ;
      tabpane.add("Blue",jPanel6) ;
      tabpane.add("Alpha",jPanel9) ;
      tabpane.setSelectedIndex(n) ;
      
      // Show the base image
      
      if (preview == null) return ;
      preview.setShowState(false) ;
      preview.updateImage(image) ;
      preview.setShowState(true) ;
      OK.setEnabled(false) ;
      CANCEL.setEnabled(false) ;
   }


   // The apply method commits the change.  
   
   void apply()
   {
      updated = false ;
      baseimage = image ;
      if (parent instanceof ImageFrame)
      {
         ((ImageFrame) parent).setChanged(true) ;
         ((ImageFrame) parent).applyTransformedImage(image) ;
      }
      reset() ;
   }

   
   // Flush references.
   
   void flush()
   {
      parent = null ;
      image = null ;
      workimage = null ;
      baseimage = null ;
   }
}