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
* GroupGlobalPanel class
*
* Purpose:
*
* This class defines a display and edit panel for group object attributes.
*
*/


import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;
import javax.swing.* ;

final class GroupGlobalPanel extends JPanel
	implements ActionListener
{
   private Group group = null ;                 // Group object
   private ImageFrame parent = null ;           // Parent image frame
   private Configuration config = null ;        // Current configuration

	// User interface objects

   private JLabel name = new JLabel();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private JLabel sizelabel = new JLabel();
   private JTextField widthfield = new JTextField();
   private JTextField heightfield = new JTextField();
   private JLabel offsetlabel = new JLabel();
   private JTextField xoffsetfield = new JTextField();
   private JTextField yoffsetfield = new JTextField();


	// Create specialized listeners for events.




   // Constructor

   public GroupGlobalPanel(ImageFrame mf, Configuration c, Group g)
   {
      group = g ;
      parent = mf ;
      config = c ;
      init() ;
      doLayout() ;
      setPreferredSize(new Dimension(250,300)) ;
   }


   // Initialize.

   private void init()
   {
      this.setLayout(gridBagLayout1);
      name.setText("Object #" + group.getIdentifier());
      sizelabel.setText("Size (w,h):");
      widthfield.setText("" + group.getSize().width);
      widthfield.setPreferredSize(new Dimension(50,widthfield.getPreferredSize().height));
      widthfield.setEnabled(false);
      heightfield.setText("" + group.getSize().height);
      heightfield.setPreferredSize(new Dimension(50,heightfield.getPreferredSize().height));
      heightfield.setEnabled(false);
      offsetlabel.setText("Offset (x,y):");
      xoffsetfield.setText("" + group.getOffset().x);
      xoffsetfield.setPreferredSize(new Dimension(50,xoffsetfield.getPreferredSize().height));
      xoffsetfield.addActionListener(this);
      yoffsetfield.setText("" + group.getOffset().y);
      yoffsetfield.setPreferredSize(new Dimension(50,yoffsetfield.getPreferredSize().height));
      yoffsetfield.addActionListener(this);

      this.add(name, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
      this.add(sizelabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(widthfield, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(heightfield, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(offsetlabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(xoffsetfield, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(yoffsetfield, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
   }


   // Panel value update for a new image.

   void update(Image img)
   {
      if (img == null) return ;
      widthfield.setText("" + img.getWidth(null)) ;
      heightfield.setText("" + img.getHeight(null)) ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
         // Change the object X offset.

         if (source == xoffsetfield)
         {
            int n = 0 ;
            String s = xoffsetfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            Point offset = group.getOffset() ;
            offset.x = n ;
            group.setOffset(offset) ;
            parent.updatePreview() ;
            group.setUpdated(true) ;
            return ;
         }

         // Change the object Y offset.

         if (source == yoffsetfield)
         {
            int n = 0 ;
            String s = yoffsetfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            Point offset = group.getOffset() ;
            offset.y = n ;
            group.setOffset(offset) ;
            parent.updatePreview() ;
            group.setUpdated(true) ;
            return ;
         }
      }

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			PrintLn.println("GroupGlobalPanel: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
			JOptionPane.showMessageDialog(parent,
				"Internal fault.  Action not completed." + "\n" + e.toString(),
				"Internal Fault", JOptionPane.ERROR_MESSAGE) ;
		}
   }
}
