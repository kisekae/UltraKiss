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
* AttributeDialog Class
*
* Purpose:
*
* This dialog is used to set component attribute values.
*
*/

import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.event.* ;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.*;
import java.net.URL ;
import java.net.MalformedURLException ;


class AttributeDialog extends KissDialog
	 implements ActionListener, WindowListener
{
	private Cel cel = null ;
   private Configuration config = null ;
   private Color defaultcolor = null ;
   private String baseattributes = null ;

   private JPanel panel1 = new JPanel();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private JPanel jPanel3 = new JPanel();
   private JPanel jPanel4 = new JPanel();
   private JPanel jPanel5 = new JPanel();
   private JPanel jPanel6 = new JPanel();
   private JPanel jPanel7 = new JPanel();
   private JPanel jPanel8 = new JPanel();
   private FlowLayout flowLayout1 = new FlowLayout();
   private BorderLayout borderLayout1 = new BorderLayout();
   private BorderLayout borderLayout2 = new BorderLayout();
   private BorderLayout borderLayout3 = new BorderLayout();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private GridBagLayout gridBagLayout2 = new GridBagLayout();
   private GridBagLayout gridBagLayout3 = new GridBagLayout();
   private GridBagLayout gridBagLayout4 = new GridBagLayout();
   private JButton OK = new JButton();
   private JButton CANCEL = new JButton();
   private JLabel jLabel2 = new JLabel();
   private JLabel jLabel3 = new JLabel();
   private JComboBox jComboBox1 = new JComboBox();
   private JComboBox jComboBox2 = new JComboBox();
   private JLabel jLabel4 = new JLabel();
   private JComboBox jComboBox3 = new JComboBox();
   private JLabel jLabel5 = new JLabel();
   private JComboBox jComboBox4 = new JComboBox();
   private JLabel jLabel6 = new JLabel();
   private JComboBox jComboBox5 = new JComboBox();
   private JLabel jLabel7 = new JLabel();
   private JComboBox jComboBox6 = new JComboBox();
   private JLabel jLabel8 = new JLabel();
   private JLabel jLabel9 = new JLabel();
   private JLabel jLabel10 = new JLabel();
   private JLabel jLabel11 = new JLabel();
   private JLabel jLabel12 = new JLabel();
   private JLabel jLabel13 = new JLabel();
   private JLabel jLabel14 = new JLabel();
   private JButton bcswath = new JButton();
   private JButton fcswath = new JButton();
   private JButton borderswath = new JButton();
   private JButton textinput = new JButton();
   private JComboBox jComboBox7 = new JComboBox();
   private JComboBox jComboBox8 = new JComboBox();
   private JLabel jLabel15 = new JLabel();
   private JComboBox jComboBox9 = new JComboBox();
   private JLabel jLabel16 = new JLabel();
   private JComboBox jComboBox10 = new JComboBox();
   private JLabel jLabel17 = new JLabel();
   private JTextField jTextField1 = new JTextField();
   private JTextField jTextField2 = new JTextField();
   private JTextField jTextField3 = new JTextField();
   private JComboBox jComboBox11 = new JComboBox();
   private JComboBox FONTS = new JComboBox();
   private JButton nobackcolor = new JButton();
   private JButton noforecolor = new JButton();
   private JButton nobordercolor = new JButton();
   private JLabel jLabel18 = new JLabel();
   private JComboBox jComboBox12 = new JComboBox();
   private JLabel jLabel19 = new JLabel();
   private JComboBox jComboBox13 = new JComboBox();
   private JLabel jLabel20 = new JLabel();
   private JComboBox jComboBox14 = new JComboBox();
   private JLabel jLabel21 = new JLabel();
   private JTextField jTextField4 = new JTextField();
   private JLabel jLabel22 = new JLabel();
   private JTextField jTextField5 = new JTextField();
   private JTextField jTextField6 = new JTextField();
   private JTextField jTextField7 = new JTextField();
   private JTextField jTextField8 = new JTextField();
   private JLabel jLabel23 = new JLabel();
   private JLabel jLabel24 = new JLabel();
   private JComboBox jComboBox15 = new JComboBox();
   private JLabel jLabel25 = new JLabel();
   private JTextField jTextField9 = new JTextField();
   private JLabel jLabel26 = new JLabel();
   private JCheckBox opaque = new JCheckBox();
   private JLabel jLabel27 = new JLabel();
  
   // State variables
   
   private Color initfc = null ;
   private Color initbc = null ;
   private Color initbdc = null ;

   
   
   ItemListener borderListener = new ItemListener()
   {
     	public void itemStateChanged(ItemEvent e)
      {
        	Object item = jComboBox14.getSelectedItem() ;
         setBorderValues(item) ;
      }
   } ;


   public AttributeDialog(JFrame f, Cel c, Configuration cf)
   { 
      super(f,null,false) ; 
      config = cf ;
      init(c) ; 
   }

   private void init(Cel c)
   {
		cel = c ;

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

      String s = Kisekae.getCaptions().getString("AttributeDialogTitle") ;
      s += (cel != null) ? " - " + cel.getName() : "";
		setTitle(s) ;
		setValues() ;
      pack() ;
      center(this) ;
      
		// Add action listeners.

      bcswath.addActionListener(this) ;
      fcswath.addActionListener(this) ;
      borderswath.addActionListener(this) ;
      nobackcolor.addActionListener(this) ;
      noforecolor.addActionListener(this) ;
      nobordercolor.addActionListener(this) ;
		textinput.addActionListener(this) ;
      jComboBox14.addItemListener(borderListener) ;
		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
   }

	// Set valid values.

	void setValues()
	{
      Vector v = new Vector() ;
      v.addElement("") ;
      v.addElement("true") ;
      v.addElement("false") ;
		Vector v2 = new Vector() ;
		v2.addElement("") ;
		v2.addElement("left") ;
		v2.addElement("right") ;
		v2.addElement("center") ;
		Vector v3 = new Vector() ;
		v3.addElement("") ;
		v3.addElement("bold") ;
		v3.addElement("italic") ;
		v3.addElement("plain") ;
		Vector v4 = new Vector() ;
		v4.addElement("") ;
		v4.addElement("line") ;
		v4.addElement("bevel") ;
		v4.addElement("etched") ;
		v4.addElement("matte") ;
		v4.addElement("titled") ;        // Will not work well as overlay
		Vector v5 = new Vector() ;
		v5.addElement("") ;
		v5.addElement("raised") ;
		v5.addElement("lowered") ;
      jComboBox1.setModel(new DefaultComboBoxModel(v)) ;
      jComboBox2.setModel(new DefaultComboBoxModel(v)) ;
      jComboBox3.setModel(new DefaultComboBoxModel(v2)) ;
      jComboBox4.setModel(new DefaultComboBoxModel(v)) ;
      jComboBox5.setModel(new DefaultComboBoxModel(v)) ;
      jComboBox6.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox7.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox8.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox9.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox10.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox11.setModel(new DefaultComboBoxModel(v3)) ;
		jComboBox12.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox13.setModel(new DefaultComboBoxModel(v)) ;
		jComboBox14.setModel(new DefaultComboBoxModel(v4)) ;
		jComboBox15.setModel(new DefaultComboBoxModel(v5)) ;
      nobackcolor.setText("Reset") ;
      noforecolor.setText("Reset") ;
      nobordercolor.setText("Reset") ;
      borderswath.setBackground(Color.black) ;

		// Create the font names selection box.

      String [] fontnames = { "Dialog", "Serif", "SansSerif", "Monospaced" } ;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment() ;
      try { fontnames = ge.getAvailableFontFamilyNames() ; }
      catch (Throwable e) { } 
      String [] fn  = new String[fontnames.length+1] ;
      for (int i = 0 ; i < fontnames.length ; i++) fn[i+1] = fontnames[i] ;
      fn[0] = "" ;
		FONTS.setModel(new DefaultComboBoxModel(fn)) ;
		FONTS.setEditable(true) ;
      
		// Identify the attributes.

		String s = (cel != null) ? cel.getAttributes() : "" ;
      baseattributes = s ;
		parseAttributes(s) ;
      
      // Enable or disable the valid attributes.

      boolean showtextinput = false ;
      String type = (cel instanceof JavaCel) ? ((JavaCel) cel).getType() : "" ;
      if ("button".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(false) ;   jComboBox2.setEnabled(false) ;
         jLabel3.setEnabled(false) ;   jComboBox1.setEnabled(false) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(false) ;   jComboBox4.setEnabled(false) ;
         jLabel6.setEnabled(false) ;   jComboBox5.setEnabled(false) ;
         jLabel7.setEnabled(false) ;   jComboBox6.setEnabled(false) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(false) ;  jComboBox9.setEnabled(false) ;
         jLabel16.setEnabled(true) ;   jComboBox10.setEnabled(true) ;
      }
      
      else if ("togglebutton".equals(type) || "radiobutton".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(false) ;   jComboBox2.setEnabled(false) ;
         jLabel3.setEnabled(false) ;   jComboBox1.setEnabled(false) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(false) ;   jComboBox4.setEnabled(false) ;
         jLabel6.setEnabled(false) ;   jComboBox5.setEnabled(false) ;
         jLabel7.setEnabled(false) ;   jComboBox6.setEnabled(false) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(true) ;   jComboBox9.setEnabled(true) ;
         jLabel16.setEnabled(true) ;   jComboBox10.setEnabled(true) ;
      }
      
      else if ("checkbox".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(false) ;   jComboBox2.setEnabled(false) ;
         jLabel3.setEnabled(false) ;   jComboBox1.setEnabled(false) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(false) ;   jComboBox4.setEnabled(false) ;
         jLabel6.setEnabled(false) ;   jComboBox5.setEnabled(false) ;
         jLabel7.setEnabled(false) ;   jComboBox6.setEnabled(false) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(true) ;   jComboBox9.setEnabled(true) ;
         jLabel16.setEnabled(true) ;   jComboBox10.setEnabled(true) ;
      }

      else if ("label".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(false) ;   jComboBox2.setEnabled(false) ;
         jLabel3.setEnabled(false) ;   jComboBox1.setEnabled(false) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(false) ;   jComboBox4.setEnabled(false) ;
         jLabel6.setEnabled(false) ;   jComboBox5.setEnabled(false) ;
         jLabel7.setEnabled(false) ;   jComboBox6.setEnabled(false) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(false) ;  jComboBox9.setEnabled(false) ;
         jLabel16.setEnabled(false) ;  jComboBox10.setEnabled(false) ;
         showtextinput = true ;
      }

      else if ("textarea".equals(type) || "textbox".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(true) ;    jComboBox2.setEnabled(true) ;
         jLabel3.setEnabled(true) ;    jComboBox1.setEnabled(true) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(true) ;    jComboBox4.setEnabled(true) ;
         jLabel6.setEnabled(true) ;    jComboBox5.setEnabled(true) ;
         jLabel7.setEnabled(true) ;    jComboBox6.setEnabled(true) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(false) ;  jComboBox9.setEnabled(false) ;
         jLabel16.setEnabled(false) ;  jComboBox10.setEnabled(false) ;
         showtextinput = true ;
      }

      else if ("textpane".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(true) ;    jComboBox2.setEnabled(true) ;
         jLabel3.setEnabled(true) ;    jComboBox1.setEnabled(true) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(true) ;    jComboBox4.setEnabled(true) ;
         jLabel6.setEnabled(true) ;    jComboBox5.setEnabled(true) ;
         jLabel7.setEnabled(true) ;    jComboBox6.setEnabled(true) ;
         jLabel13.setEnabled(false) ;  jComboBox7.setEnabled(false) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(false) ;  jComboBox9.setEnabled(false) ;
         jLabel16.setEnabled(false) ;  jComboBox10.setEnabled(false) ;
         showtextinput = true ;
      }

      else if ("list".equals(type))
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(true) ;    jComboBox2.setEnabled(true) ;
         jLabel3.setEnabled(true) ;    jComboBox1.setEnabled(true) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(true) ;    jComboBox4.setEnabled(true) ;
         jLabel6.setEnabled(true) ;    jComboBox5.setEnabled(true) ;
         jLabel7.setEnabled(true) ;    jComboBox6.setEnabled(true) ;
         jLabel13.setEnabled(true) ;   jComboBox7.setEnabled(true) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(false) ;  jComboBox9.setEnabled(false) ;
         jLabel16.setEnabled(false) ;  jComboBox10.setEnabled(false) ;
      }
      
      else
      {
         jLabel17.setEnabled(true) ;   jTextField1.setEnabled(true) ;
         jLabel2.setEnabled(true) ;    jComboBox2.setEnabled(true) ;
         jLabel3.setEnabled(true) ;    jComboBox1.setEnabled(true) ;
         jLabel4.setEnabled(true) ;    jComboBox3.setEnabled(true) ;
         jLabel5.setEnabled(true) ;    jComboBox4.setEnabled(true) ;
         jLabel6.setEnabled(true) ;    jComboBox5.setEnabled(true) ;
         jLabel7.setEnabled(true) ;    jComboBox6.setEnabled(true) ;
         jLabel13.setEnabled(true) ;   jComboBox7.setEnabled(true) ;
         jLabel14.setEnabled(true) ;   jComboBox8.setEnabled(true) ;
         jLabel15.setEnabled(true) ;   jComboBox9.setEnabled(true) ;
         jLabel16.setEnabled(true) ;   jComboBox10.setEnabled(true) ;
      }
      
      // Set the border context.
      
      Object o = jComboBox14.getSelectedItem() ;
      String s1 = (o != null) ? o.toString() : "" ;
      setBorderValues(s1) ;
      
		URL iconfile = Kisekae.getResource("Images/leaf.gif") ;
		if (iconfile != null) textinput.setIcon(new ImageIcon(iconfile)) ;
		textinput.setMargin(new Insets(1,1,1,1)) ;
      textinput.setVisible(showtextinput) ;
		textinput.setFocusable(false) ;
		textinput.setToolTipText("Enter text in a window") ;
		textinput.setAlignmentY(0.5f) ;
      
		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton(OK) ;
   }

	// Set border values on a context change.

	void setBorderValues(Object o)
	{
      jLabel22.setVisible(false) ;   jTextField4.setVisible(false) ;
      jLabel25.setVisible(false) ;   jComboBox15.setVisible(false) ;
      jLabel26.setVisible(false) ;   jTextField9.setVisible(false) ;
      if ("".equals(o.toString()))
         { jLabel22.setVisible(true) ; jTextField4.setVisible(true) ; }
      else if ("line".equals(o.toString()))
         { jLabel22.setVisible(true) ; jTextField4.setVisible(true) ; }
      else if ("bevel".equals(o.toString()))
         { jLabel25.setVisible(true) ; jComboBox15.setVisible(true) ; }
      else if ("titled".equals(o.toString()))
         { jLabel26.setVisible(true) ; jTextField9.setVisible(true) ; }
   }
 
   private void jbInit() throws Exception
   {
		Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
		Border eb2 = BorderFactory.createEmptyBorder(0,10,0,0) ;
		Border eb3 = BorderFactory.createEmptyBorder(20,10,10,10) ;
		Border eb4 = BorderFactory.createEmptyBorder(0,0,0,0) ;
      Border tb1 = new CompoundBorder(BorderFactory.createTitledBorder("Attributes"),eb1) ;
      Border tb2 = new CompoundBorder(BorderFactory.createTitledBorder("Fonts"),eb1) ;
      Border tb3 = new CompoundBorder(BorderFactory.createTitledBorder("Color"),eb1) ;
      Border tb4 = new CompoundBorder(BorderFactory.createTitledBorder("Border"),eb1) ;
      panel1.setLayout(borderLayout1);
      panel1.setBorder(eb1) ;
      
      OK.setToolTipText("");
      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      CANCEL.setToolTipText("");
      CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
      jPanel5.setBorder(eb3);
      jPanel5.setLayout(new BoxLayout(jPanel5,BoxLayout.X_AXIS));
      
      jPanel1.setLayout(gridBagLayout1);
      jPanel1.setBorder(tb1);
      jPanel2.setLayout(borderLayout2);
      jPanel2.setBorder(eb2);
      jPanel3.setLayout(gridBagLayout2);
      jPanel3.setBorder(tb2);
      jPanel4.setLayout(gridBagLayout3);
      jPanel4.setBorder(tb3);
      jPanel6.setLayout(gridBagLayout4);
      jPanel6.setBorder(tb4);
      jPanel7.setLayout(flowLayout1);
      jPanel7.setBorder(eb4);
      jPanel8.setLayout(borderLayout3);
      jLabel2.setText("Read:");
      jLabel2.setToolTipText("Sets the component to read-only");
      jLabel3.setText("Wrap:");
      jLabel3.setToolTipText("Enables word wrap for text");
      jLabel4.setText("Align:");
      jLabel4.setToolTipText("Aligns the text in the component");
      jLabel5.setText("No scroll:");
      jLabel5.setToolTipText("Enables or disables scrolling for the component");
      jLabel6.setText("Vertical scroll:");
      jLabel6.setToolTipText("Enables vertical scrolling for the component");
      jLabel7.setText("Horizontal scroll:");
      jLabel7.setToolTipText("Enables horizontal scrolling for the component");
      jLabel8.setText("Back color:");
      jLabel8.setToolTipText("Sets the background color for the component");
      jLabel9.setText("Fore color:");
      jLabel9.setToolTipText("Sets the foreground color for the component");
      jLabel10.setText("Font name:");
      jLabel10.setToolTipText("Specifies the text font for the component");
      jLabel11.setText("Font size:");
      jLabel11.setToolTipText("Specifies the text size for the component");
      jLabel12.setText("Font style:");
      jLabel12.setToolTipText("Specifies the text style for the component");
      jLabel13.setText("Multiple:");
      jLabel13.setToolTipText("Enables multiple selections for list components");
      jLabel14.setText("Disabled:");
      jLabel14.setToolTipText("Disables the component from input");
      jLabel15.setText("Selected:");
      jLabel15.setToolTipText("Sets a selected state for the component");
      jLabel16.setText("No margin:");
      jLabel16.setToolTipText("Enables drawing of the component margin");
      jLabel17.setText("Text:");
      jLabel17.setToolTipText("Sets the initial text for the component");
      jLabel18.setText("Controls:");
      jLabel18.setToolTipText("Shows player controls for the movie");
      jLabel19.setText("Repeat:");
      jLabel19.setToolTipText("Causes the movie to continuously repeat");
      jLabel20.setText("Border:");
      jLabel20.setToolTipText("Sets the border thickness around the movie");
      jLabel21.setText("Border:");
      jLabel21.setToolTipText("Sets the border type for the component");
      jLabel22.setText("Thickness:");
      jLabel22.setToolTipText("Sets the border thickness in pixels");
      jLabel23.setText("Insets:");
      jLabel23.setToolTipText("Sets the insets (Top,Left,Bottom,Right)");
      jLabel24.setText("Border color:");
      jLabel24.setToolTipText("Sets the color of the border");
      jLabel25.setText("Bevel Type:");
      jLabel25.setToolTipText("Sets the raised or lowered shape of the border");
      jLabel26.setText("Title:");
      jLabel26.setToolTipText("The title for the border");
      jLabel27.setText("Transparent background:");
      jLabel27.setToolTipText("Enable a transparent background");
      jTextField1.setOpaque(true);
      jTextField1.setPreferredSize(new Dimension(100, 21));
      jTextField1.setRequestFocusEnabled(true);
      jTextField1.setText(null);
      jTextField2.setOpaque(true);
      jTextField2.setPreferredSize(new Dimension(50, 21));
      jTextField2.setRequestFocusEnabled(true);
      jTextField2.setText(null);
      jTextField3.setPreferredSize(new Dimension(100, 21));
      jTextField3.setRequestFocusEnabled(true);
      jTextField3.setText(null);
      jTextField4.setPreferredSize(new Dimension(30, 21));
      jTextField4.setRequestFocusEnabled(true);
      jTextField4.setText(null);
      jTextField5.setPreferredSize(new Dimension(30, 21));
      jTextField5.setRequestFocusEnabled(true);
      jTextField5.setToolTipText("Top");
      jTextField5.setText(null);
      jTextField6.setPreferredSize(new Dimension(30, 21));
      jTextField6.setRequestFocusEnabled(true);
      jTextField6.setToolTipText("Left");
      jTextField6.setText(null);
      jTextField7.setPreferredSize(new Dimension(30, 21));
      jTextField7.setRequestFocusEnabled(true);
      jTextField7.setToolTipText("Bottom");
      jTextField7.setText(null);
      jTextField8.setPreferredSize(new Dimension(30, 21));
      jTextField8.setRequestFocusEnabled(true);
      jTextField8.setToolTipText("Right");
      jTextField8.setText(null);
      jTextField9.setPreferredSize(new Dimension(100, 21));
      jTextField9.setRequestFocusEnabled(true);
      jTextField9.setText(null);
      jComboBox2.setPreferredSize(new Dimension(100, 21));
      jComboBox1.setPreferredSize(new Dimension(100, 21));
      jComboBox3.setPreferredSize(new Dimension(100, 21));
      jComboBox4.setPreferredSize(new Dimension(100, 21));
      jComboBox5.setPreferredSize(new Dimension(100, 21));
      jComboBox6.setPreferredSize(new Dimension(100, 21));
      jComboBox7.setPreferredSize(new Dimension(100, 21));
      jComboBox8.setPreferredSize(new Dimension(100, 21));
      jComboBox9.setPreferredSize(new Dimension(100, 21));
      jComboBox10.setPreferredSize(new Dimension(100, 21));
      jComboBox11.setPreferredSize(new Dimension(100, 21));
      jComboBox14.setPreferredSize(new Dimension(100, 21));
      jComboBox15.setPreferredSize(new Dimension(100, 21));
      FONTS.setPreferredSize(new Dimension(150, 21));
      bcswath.setPreferredSize(new Dimension(20, 20));
      fcswath.setPreferredSize(new Dimension(20, 20));
      borderswath.setPreferredSize(new Dimension(20, 20));
      
      getContentPane().add(panel1);
      panel1.add(jPanel1, BorderLayout.WEST);
      
      if (cel instanceof JavaCel)
      {
         jPanel1.add(jPanel8,           new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
         jPanel8.add(jLabel17,BorderLayout.WEST) ;
         jPanel8.add(textinput,BorderLayout.EAST) ;
         jPanel1.add(jTextField1,       new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel2,            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox2,           new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel3,            new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox1,           new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel4,            new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox3,           new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel5,            new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox4,           new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel6,            new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox5,           new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel7,            new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox6,           new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel13,            new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox7,           new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel14,            new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox8,           new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel15,            new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox9,           new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel16,            new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox10,           new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      }
      
      if (cel instanceof Video)
      {
         jPanel1.add(jLabel18,           new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox12,       new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jLabel19,            new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
         jPanel1.add(jComboBox13,           new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      }

      // Font pane
      
      panel1.add(jPanel2, BorderLayout.EAST);
      jPanel2.add(jPanel3, BorderLayout.NORTH);
      jPanel3.add(jLabel10,         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel3.add(FONTS,       new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel3.add(jLabel11,        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel3.add(jTextField3,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel3.add(jLabel12,       new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel3.add(jComboBox11,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

      // Border pane
      
      jPanel2.add(jPanel6, BorderLayout.CENTER);
      jPanel6.add(jLabel21,         new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jComboBox14,       new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jLabel22,        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jTextField4,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jLabel25,        new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jComboBox15,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jLabel26,        new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jTextField9,  new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jLabel23,       new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(jPanel7,  new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
      jPanel7.add(jTextField5) ;
      jPanel7.add(jTextField6) ;
      jPanel7.add(jTextField7) ;
      jPanel7.add(jTextField8) ;
      jPanel6.add(jLabel24,     new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(borderswath,  new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel6.add(nobordercolor,  new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 30, 5, 5), 0, 0));
 
      // Color pane
      
      jPanel2.add(jPanel4, BorderLayout.SOUTH);
      jPanel4.add(jLabel8,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel4.add(bcswath,      new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel4.add(nobackcolor,  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 30, 5, 5), 0, 0));
      jPanel4.add(jLabel9,     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel4.add(fcswath,      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel4.add(noforecolor,  new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 30, 5, 5), 0, 0));
     jPanel4.add(jLabel27,      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel4.add(opaque,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 30, 5, 5), 0, 0));
      
      panel1.add(jPanel5,  BorderLayout.SOUTH);
      jPanel5.add(Box.createGlue()) ;
      jPanel5.add(OK, null);
      jPanel5.add(Box.createGlue()) ;
      jPanel5.add(CANCEL, null);
      jPanel5.add(Box.createGlue()) ;
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
				if (cel != null) 
               cel.setAttributes(buildAttributes()) ;
				if (config != null) 
               config.setUpdated(true) ;
            
				MainFrame mf = Kisekae.getMainFrame() ;
				PanelFrame pf = (mf != null) ? mf.getPanel() : null ;
				if (pf != null && cel != null) 
            {
               pf.createAttributeEdit(cel,baseattributes,cel.getAttributes()) ;
               pf.redraw(cel.getBoundingBox()) ;
            }
				dispose() ;
            return ;
			}

			// A CANCEL closes only this dialog and makes the parent visible.

			if (source == CANCEL)
			{
				dispose() ;
            return ;
			}

			// Background color change

			if (source == bcswath)
			{
            Color cc = JColorChooser.showDialog(getParentFrame(),
               "Background Color",bcswath.getBackground()) ;
            if (cc != null) 
            {
               bcswath.setBackground(cc) ;
               initbc = cc ;
            }
            return ;
			}

			// Foreground color change

			if (source == fcswath)
			{
            Color cc = JColorChooser.showDialog(getParentFrame(),
               "Foreground Color",fcswath.getBackground()) ;
            if (cc != null) 
            {
               fcswath.setBackground(cc) ;
               initfc = cc ;
            }
            return ;
			}

			// Border color change

			if (source == borderswath)
			{
            Color cc = JColorChooser.showDialog(getParentFrame(),
               "Border Color",fcswath.getBackground()) ;
            if (cc != null) 
            {
               borderswath.setBackground(cc) ;
               initbdc = cc ;
            }
            return ;
			}

			// Remove background color setting

			if (source == nobackcolor)
			{
            initbc = null ;
            bcswath.setBackground(null) ;
            if (cel != null) cel.setAttributes("-bc") ;
         }

			// Remove foreground color setting

			if (source == noforecolor)
			{
            initfc = null ;
            fcswath.setBackground(null) ;
            if (cel != null) cel.setAttributes("-fc") ;
         }

			// Remove border color setting

			if (source == nobordercolor)
			{
            initbdc = null ;
            borderswath.setBackground(Color.black) ;
            if (cel != null) cel.setAttributes("-bdc") ;
         }

			// Pop up a text input dialog

			if (source == textinput)
			{
            JScrollPane scroll = null ;
            JTextComponent input = new JTextField() ;
            Dimension d = new Dimension(cel.getSize()) ;
            if (!(cel instanceof JavaCel)) return ;

            // If we are setting TextArea attributes, configure our
            // input component as the current component is set.
            // Replace newlines with '\n' character sequences.
            
            Object [] o = ((JavaCel) cel).getComponent() ;
            if (o[1] instanceof JTextArea)
            {
               input = new JTextArea() ;
               String s = substitute(jTextField1.getText(), "\\n", "\n") ;
               ((JTextArea) input).setLineWrap(((JTextArea) o[1]).getLineWrap()) ;
               ((JTextArea) input).setWrapStyleWord(((JTextArea) o[1]).getWrapStyleWord()) ;
               ((JTextArea) input).setMargin(((JTextArea) o[1]).getMargin()) ;
               ((JTextArea) input).setText(s) ;
               scroll = new JScrollPane(input) ;
               Insets insets = scroll.getInsets() ;
               d.width += insets.left + insets.right ;
               d.height += insets.top + insets.bottom ;
            }
            else if (o[1] instanceof JEditorPane)
            {
               input = new JEditorPane() ;
               ((JEditorPane) input).setContentType("text/html");
               ((JEditorPane) input).setEditorKit(new WebHTMLEditor());
               String s = substitute(jTextField1.getText(), "\\n", "\n") ;
               ((JEditorPane) input).setMargin(((JEditorPane) o[1]).getMargin()) ;
               ((JEditorPane) input).setText(s) ;
               scroll = new JScrollPane(input) ;
               Insets insets = scroll.getInsets() ;
               d.width += insets.left + insets.right ;
               d.height += insets.top + insets.bottom ;
            }
            else
               input.setText(jTextField1.getText()) ;
            
            // Create a custom option pane to show the text.

            Object o1 = (scroll != null) ? (Object) scroll : (Object) input ;
            JOptionPane optionpane = new JOptionPane(o1,JOptionPane.PLAIN_MESSAGE,JOptionPane.OK_CANCEL_OPTION) ;
            String s = Kisekae.getCaptions().getString("AttributeDialogTitle") ;
            JDialog dlg = optionpane.createDialog(getParentFrame(),s) ;
            Insets insets = optionpane.getInsets() ;
            d.width += insets.left + insets.right ;
            d.width = Math.max(d.width,200) ;
            d.height += insets.top + insets.bottom + 60 ;
            dlg.setSize(d) ;
            dlg.show() ;
            
            // Get the return value. The option pane is modal.
            
            o1 = optionpane.getValue() ;
            if (!(o1 instanceof Integer)) return ;
            if (((Integer) o1).intValue() == JOptionPane.CANCEL_OPTION) return ;
            
            // Replace newlines with '\n' character sequences.

            if (input instanceof JTextArea)
            {
               s = substitute(input.getText(),"\n","\\n") ;
               jTextField1.setText(s) ;
            }
            else if (input instanceof JEditorPane)
            {
               s = substitute(input.getText(),"\n","\\n") ;
               jTextField1.setText(s) ;
            }
            else
            {
               jTextField1.setText(input.getText()) ;
            }
         }
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			System.out.println("AttributeDialog: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + e.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { dispose() ; }


	// Parse the component attribute string.

	private void parseAttributes(String s)
	{
		if (s == null) return ;
      String s1 = eraseLiterals(s) ;
		int i = 0 ;

		// Parse attributes.

		if ((i = s1.indexOf("wrap")) >= 0)         // word wrap
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox1.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("align=")) >= 0)        // text alignment
		{
			String s2 = s1.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 6) j = 6 ;
			s2 = s2.substring(6,j) ;
			jComboBox3.setSelectedItem(s2);
		}

		if ((i = s1.indexOf("noscroll")) >= 0)     // disable scrolling
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox4.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("read")) >= 0)         // enable read only
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox2.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("vsb")) >= 0)          // enable vertical scroll bars
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox5.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("hsb")) >= 0)          // enable horizontal scroll bars
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox6.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("bc=")) >= 0)           // set background color rgb
		{
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (onoff)
         {
            String s2 = s1.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            if (j < 3) j = 3 ;
            s2 = s2.substring(3,j) ;
            try
            {
               int rgb = Integer.parseInt(s2) ;
               initbc = new Color(rgb) ;
               bcswath.setBackground(initbc) ;
            }
            catch (Exception e) { }
         }
		}

		if ((i = s1.indexOf("fc=")) >= 0)           // set foreground color rgb
		{
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (onoff)
         {
            String s2 = s1.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            if (j < 3) j = 3 ;
            s2 = s2.substring(3,j) ;
            try
            {
               int rgb = Integer.parseInt(s2) ;
               initfc = new Color(rgb) ;
               fcswath.setBackground(initfc) ;
            }
            catch (Exception e) { }
         }
		}
      
		if ((i = s1.indexOf("trans")) >= 0)        // transparent background
		{
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         opaque.setSelected(onoff); 
		}

		if ((i = s1.indexOf("fontname=")) >= 0)    // set the font
		{
			String s2 = s.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
			s2 = s2.substring(9,j) ;
         s2 = Variable.getStringLiteralValue(s2) ;
			FONTS.setSelectedItem(s2);
		}

		if ((i = s1.indexOf("fontsize=")) >= 0)    // set the font size
		{
			String s2 = s1.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 9) j = 9 ;
			s2 = s2.substring(9,j) ;
			jTextField3.setText(s2);
		}

		if ((i = s1.indexOf("fontstyle=")) >= 0)    // set the font attributes
		{
			String s2 = s1.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 10) j = 10 ;
			s2 = s2.substring(10,j) ;
			jComboBox11.setSelectedItem(s2);
		}

		if ((i = s1.indexOf("multiple")) >= 0)    // set multiple selections
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox7.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("disabled")) >= 0)    // set disabled state
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox8.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("selected")) >= 0)    // set selected state
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox9.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("text=")) >= 0)        // set component text
		{
         String s2 = s.substring(i) ;
         int j = s2.indexOf("\",") ;
         if (j > 0 && s2.charAt(j) == '\"') j++ ;
         if (j < 0) j = s2.length() ;
         if (j < 5) j = 5 ;
			s2 = s2.substring(5,j) ;
			s2 = Variable.getStringLiteralValue(s2) ;
			jTextField1.setText(s2);
		}

		if ((i = s1.indexOf("nomargin")) >= 0)    // set no margin
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox10.setSelectedItem(onoff);
      }

      // Movie attributes
      
		if ((i = s1.indexOf("controls")) >= 0)    // set selected state
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox12.setSelectedItem(onoff);
      }

		if ((i = s1.indexOf("repeat")) >= 0)    // set selected state
      {
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         String onoff = (c == '-') ? "false" : "true" ;
			jComboBox13.setSelectedItem(onoff);
      }

      // Border attributes
      
		if ((i = s1.indexOf("border=")) >= 0)    // set border size
		{
			String s2 = s.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 7) j = 7 ;
			s2 = s2.substring(7,j) ;
			s2 = Variable.getStringLiteralValue(s2) ;
			jTextField4.setText(s2);
		}

		if ((i = s1.indexOf("insets=")) >= 0)    // set border insets
		{
			String s2 = s.substring(i) ;
         int j = s2.indexOf("\",") ;
         if (j > 0 && s2.charAt(j) == '\"') j++ ;
         if (j < 0) j = s2.length() ;
         if (j < 7) j = 7 ;
			s2 = s2.substring(7,j) ;
			s2 = Variable.getStringLiteralValue(s2) ;
         StringTokenizer st = new StringTokenizer(s2,", ") ;
         if (st.hasMoreTokens()) s2 = st.nextToken() ;
			jTextField5.setText(s2);
         if (st.hasMoreTokens()) s2 = st.nextToken() ;
			jTextField6.setText(s2);
         if (st.hasMoreTokens()) s2 = st.nextToken() ;
			jTextField7.setText(s2);
         if (st.hasMoreTokens()) s2 = st.nextToken() ;
			jTextField8.setText(s2);
		}

		if ((i = s1.indexOf("borderstyle=")) >= 0)    // set the border style
		{
			String s2 = s1.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 12) j = 12 ;
			s2 = s2.substring(12,j) ;
			jComboBox14.setSelectedItem(s2);
		}

		if ((i = s1.indexOf("bordertitle=")) >= 0)    // set the border title
		{
			String s2 = s.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 12) j = 12 ;
			s2 = s2.substring(12,j) ;
			s2 = Variable.getStringLiteralValue(s2) ;
			jTextField9.setText(s2);
		}

		if ((i = s1.indexOf("bevelstyle=")) >= 0)    // set the border bevel style
		{
			String s2 = s1.substring(i) ;
			int j = s2.indexOf(',') ;
			if (j < 0) j = s2.length() ;
         if (j < 11) j = 11 ;
			s2 = s2.substring(11,j) ;
			jComboBox15.setSelectedItem(s2);
		}

		if ((i = s1.indexOf("bdc=")) >= 0)           // set border color rgb
		{
         char c = (i > 0) ? s1.charAt(i-1) : ' ' ;
         boolean onoff = (c == '-') ? false : true ;
         if (onoff)
         {
            String s2 = s1.substring(i) ;
            int j = s2.indexOf(',') ;
            if (j < 0) j = s2.length() ;
            if (j < 4) j = 4 ;
            s2 = s2.substring(4,j) ;
            try
            {
               int rgb = Integer.parseInt(s2) ;
               initbdc = new Color(rgb) ;
               borderswath.setBackground(initbdc) ;
            }
            catch (Exception e) { }
         }
		}
	}



	// Generate a new component attribute string.

	private String buildAttributes()
	{
      String s ;
      Object o ;
		StringBuffer sb = new StringBuffer("") ;

      // Component attributes
      
      if (cel instanceof JavaCel)
      {
         s = jTextField1.getText() ;
         if (s != null) sb.append("text=\""+s+"\",") ;
         o = jComboBox3.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if (s.length() > 0) sb.append("align="+s+",") ;
         if (s.length() == 0) sb.append("-align,") ;
         o = jComboBox1.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("wrap,") ;
         if ("false".equals(s)) sb.append("-wrap,") ;
         o = jComboBox2.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("read,") ;
         if ("false".equals(s)) sb.append("-read,") ;
         o = jComboBox4.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("noscroll,") ;
         if ("false".equals(s)) sb.append("-noscroll,") ;
         o = jComboBox5.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("vsb,") ;
         if ("false".equals(s)) sb.append("-vsb,") ;
         o = jComboBox6.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("hsb,") ;
         if ("false".equals(s)) sb.append("-hsb,") ;
         o = jComboBox7.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("multiple,") ;
         if ("false".equals(s)) sb.append("-multiple,") ;
         o = jComboBox8.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("disabled,") ;
         if ("false".equals(s)) sb.append("-disabled,") ;
         o = jComboBox9.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("selected,") ;
         if ("false".equals(s)) sb.append("-selected,") ;
         o = jComboBox10.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("nomargin,") ;
         if ("false".equals(s)) sb.append("-nomargin,") ;
      }

      // Movie attributes
      
      if (cel instanceof Video)
      {
         o = jComboBox12.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("controls,") ;
         if ("false".equals(s)) sb.append("-controls,") ;
         o = jComboBox13.getSelectedItem() ;
         s = (o != null) ? o.toString() : "" ;
         if ("true".equals(s)) sb.append("repeat,") ;
         if ("false".equals(s)) sb.append("-repeat,") ;
//       s = jTextField2.getText() ;
//       if (s != null) sb.append("border=\""+s+"\",") ;
      }

      // Font attributes
      
		o = FONTS.getSelectedItem() ;
		s = (o != null) ? o.toString() : "" ;
		if (s.length() > 0) sb.append("fontname=\""+s+"\",") ;
		if (o == null) sb.append("-fontname,") ;
		s = jTextField3.getText() ;
		if (s != null && s.length() > 0) sb.append("fontsize="+s+",") ;
		o = jComboBox11.getSelectedItem() ;
		s = (o != null) ? o.toString() : "" ;
		if (s.length() > 0) sb.append("fontstyle="+s+",") ;
      
      // Color attributes
      
      Color c = bcswath.getBackground() ;
      if (initbc != null) sb.append("bc="+(c.getRGB()&0xffffff)+",") ;
      if (initbc == null) sb.append("-bc,") ;
      c = fcswath.getBackground() ;
      if (initfc != null) sb.append("fc="+(c.getRGB()&0xffffff)+",") ;
      if (initfc == null) sb.append("-fc,") ;
      c = borderswath.getBackground() ;
      if (initbdc != null) sb.append("bdc="+(c.getRGB()&0xffffff)+",") ;
      if (initbdc == null) sb.append("-bdc,") ;
      if (opaque.isSelected()) sb.append("trans,") ;

      // Border attributes

      s = jTextField4.getText() ;
      if (s != null && s.length() > 0) sb.append("border="+s+",") ;
      if (s != null && s.length() == 0) sb.append("-border,") ;
		o = jComboBox14.getSelectedItem() ;
		s = (o != null) ? o.toString() : "" ;
		if (s.length() > 0) sb.append("borderstyle="+s+",") ;
		if (s.length() == 0) sb.append("-borderstyle,") ;
      s = jTextField9.getText() ;
      if (s != null && s.length() > 0) sb.append("bordertitle=\""+s+"\",") ;
      if (s != null && s.length() == 0) sb.append("-bordertitle,") ;
		o = jComboBox15.getSelectedItem() ;
		s = (o != null) ? o.toString() : "" ;
		if (s.length() > 0) sb.append("bevelstyle="+s+",") ;
		if (s.length() == 0) sb.append("-bevelstyle,") ;
      String s1 = jTextField5.getText() ;
      String s2 = jTextField6.getText() ;
      String s3 = jTextField7.getText() ;
      String s4 = jTextField8.getText() ;
      if ((s1 != null && s1.length() > 0) || (s2 != null && s2.length() > 0)
         || (s3 != null && s3.length() > 0) || (s4 != null && s4.length() > 0)) 
      {
         if (s1 == null || s1.length() == 0) s1 = "0" ;
         if (s2 == null || s2.length() == 0) s2 = "0" ;
         if (s3 == null || s3.length() == 0) s3 = "0" ;
         if (s4 == null || s4.length() == 0) s4 = "0" ;
         sb.append("insets=\""+s1+","+s2+","+s3+","+s4+"\",") ;
      }
      else sb.append("-insets,") ;

      // Terminate the attribute string.
      
		s = sb.toString() ;
		int i = s.length() ;
		if (i > 1) s = s.substring(0,i-1) ;
		return s ;
	}
   
      
  // Function to erase all string literals in the comment.
   
   private String eraseLiterals(String s)
   {
      if (s == null) return null ;
      s = s.toLowerCase() ;
      StringBuffer sb = new StringBuffer(s) ;
      boolean inliteral = s.startsWith("\"") ;
      for (int i = 1 ; i < sb.length() ; i++)
      {
         if (sb.charAt(i) == '"' && sb.charAt(i-1) != '\\') 
            inliteral = !inliteral ;
         else if (inliteral && sb.charAt(i) != '\\') 
            sb.replace(i,i+1," ") ;
      }
      return sb.toString() ; 
   }
   
   
   // Function to substitute characters in a string.
   // Substitute s1 string with s2
   
   private String substitute(String s, String s1, String s2)
   {
      if (s == null) return "" ;
      if (s1 == null || s1.length() == 0) return s ;
      if (s2 == null) s2 = "" ;
      StringBuffer sb = new StringBuffer(s) ;
      int found = sb.indexOf(s1) ;
      while (found >= 0) 
      {
         sb.replace(found,found+s1.length(),s2) ;
         found = sb.indexOf(s1,found+s2.length()) ;
      }
      return sb.toString() ;
   }
}