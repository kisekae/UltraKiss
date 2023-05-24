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

final class ColorSizePanel extends JPanel implements ActionListener
{
   private ImageFrame parent = null ;			// Reference to our parent dialog
   private String colorcount = null ;			// The default color count

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JLabel jLabel1 = new JLabel();
	private JTextField jTextField1 = new JTextField();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private JCheckBox jCheckBox1 = new JCheckBox();


	// Constructor

	public ColorSizePanel(ImageFrame frame, int n)
	{
		// Call the base class constructor to set up our dialog.

      parent = frame ;

      // Initialize the user interface.

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

      // Set listeners and show the dialog.

      setColorSize(n) ;
		OK.addActionListener(this);
		CANCEL.addActionListener(this);
      setVisible(true) ;
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		jPanel1.setLayout(gridBagLayout1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		OK.setText(Kisekae.getCaptions().getString("OkMessage"));
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));

		jLabel1.setText(Kisekae.getCaptions().getString("PaletteColorsText"));
		jTextField1.setPreferredSize(new Dimension(30, 21));
		jCheckBox1.setToolTipText("");
      jCheckBox1.setText(Kisekae.getCaptions().getString("QuickColorMatchText"));
      this.add(panel1);
		panel1.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel1.add(jCheckBox1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      jPanel1.add(jTextField1, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
 	 	Object source = evt.getSource() ;

      // An OK request closes the dialog.

		if (source == OK)
      {
         int m = getColorSize() ;
         if (parent != null) parent.changeColorCount(m,getQuick()) ;
         return ;
      }

      // A Cancel request closes the dialog.

		if (source == CANCEL)
      {
      	colorcount = null ;
         return ;
      }
   }


   // Return the color count field value.

	public int getColorSize()
   {
   	if (colorcount == null) return -1 ;
   	String textvalue = jTextField1.getText() ;
      if (textvalue == null) return -1 ;
      if (textvalue.length() == 0) return -1 ;
      try { return (Integer.parseInt(textvalue)) ; }
      catch (Exception e) { }
      return -1 ;
   }


   // Return the color count field value.

   public boolean getQuick()
   {
      return jCheckBox1.isSelected() ;
   }


   // Set the color count field value.

	public void setColorSize(int n)
   {
      colorcount = "" + n ;
   	jTextField1.setText(colorcount) ;
   }
}
