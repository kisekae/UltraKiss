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
* GifLocalPanel class
*
* Purpose:
*
* This class defines a display and edit panel for GIF file local attributes.
*
*/


import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;
import javax.swing.* ;

final class GifLocalPanel extends JPanel
	implements ActionListener
{
   private GifCel gif = null ;                  // GIF cel
   private GifFrame frame = null ;              // GIF cel frame
   private int framenumber = 0 ;                // GIF frame number
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
   private JLabel localpalette = new JLabel();
   private JCheckBox localcheck = new JCheckBox();
   private JButton editpalette = new JButton();
   private JLabel transparentlabel = new JLabel();
   private JTextField trindexfield = new JTextField();
   private JLabel delaylabel = new JLabel();
   private JTextField delayfield = new JTextField();
   private JCheckBox delaywait = new JCheckBox();
   private JLabel removelabel = new JLabel();
   private JComboBox removecombo = new JComboBox();
	private ColorMenu settransparent = null ;
   private JPopupMenu popup = null ;


	// Create specialized listeners for events.

   // The popup listener is used to show and hide the menu popup item for
   // transparency color selections.  We show the popup on a press and
   // hide it when the mouse moves out of the menu or this panel.

	MouseListener popupListener = new MouseListener()
   {
      private Rectangle popupbounds = null ;

      public void mouseReleased(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e)
      {
         if (e.getSource() == popup)
         {
            if (popupbounds == null) return ;
            if (settransparent == null) return ;
            Point p = new Point(e.getX(),e.getY()) ;
			   Component source = (Component) e.getSource() ;
			   SwingUtilities.convertPointToScreen(p,source) ;
            int x1 = popupbounds.x ;
            int x2 = x1 + popupbounds.width ;
            int y1 = popupbounds.y ;
            int y2 = y1 + popupbounds.height ;
            if (p.x <= x1 || p.x >= x2 || p.y <= y1 || p.y >= y2)
               hidePopup() ;
         }
      }
      public void mousePressed(MouseEvent e)
      {
         if (e.getSource() == settransparent)
         {
            popup = settransparent.getPopupMenu() ;
            popup.addMouseListener(this) ;
            settransparent.setPopupMenuVisible(true) ;
            popupbounds = popup.getBounds() ;
            Point location = popup.getLocationOnScreen() ;
            popupbounds.x = location.x ;
            popupbounds.y = location.y ;
         }
      }
   } ;

	MouseListener panelListener = new MouseListener()
   {
      public void mousePressed(MouseEvent e) { }
      public void mouseReleased(MouseEvent e) { }
      public void mouseClicked(MouseEvent e) { }
      public void mouseEntered(MouseEvent e) { }
      public void mouseExited(MouseEvent e)
      {
         if (popup == null || settransparent == null) return ;
         hidePopup() ;
      }
   } ;



   // Constructor

   public GifLocalPanel(ImageFrame mf, Configuration c, GifCel cel, GifFrame f, int n)
   {
      gif = cel ;
      frame = f ;
      framenumber = n ;
      parent = mf ;
      config = c ;
      init() ;
      doLayout() ;
      setPreferredSize(new Dimension(250,300)) ;
      addMouseListener(panelListener) ;
   }


   // Initialize.

   private void init()
   {
      this.setLayout(gridBagLayout1);
      name.setText("Frame " + framenumber);
      sizelabel.setText("Size (w,h):");
      widthfield.setText("" + frame.getSize().width);
      widthfield.setEnabled(false) ;
      widthfield.setPreferredSize(new Dimension(50,widthfield.getPreferredSize().height));
      heightfield.setText("" + frame.getSize().height);
      heightfield.setEnabled(false) ;
      heightfield.setPreferredSize(new Dimension(50,heightfield.getPreferredSize().height));
      offsetlabel.setText("Offset (x,y):");
      xoffsetfield.setText("" + frame.getOffset().x);
      xoffsetfield.setPreferredSize(new Dimension(50,xoffsetfield.getPreferredSize().height));
      xoffsetfield.addActionListener(this);
      yoffsetfield.setText("" + frame.getOffset().y);
      yoffsetfield.setPreferredSize(new Dimension(50,yoffsetfield.getPreferredSize().height));
      yoffsetfield.addActionListener(this);
      localpalette.setText("Local Palette:");
      localcheck.setSelected(frame.isLocalColorTable());
      localcheck.setText(" (" + frame.getColorCount() + ")");
      localcheck.addActionListener(this);
      editpalette.setText("Edit");
      editpalette.addActionListener(this) ;
      editpalette.setEnabled(localcheck.isSelected());
      transparentlabel.setText("Transparent:");
      int [] trans = frame.getTransparentIndex() ;
      int t = (trans == null) ? -1 : trans[0] ;
      trindexfield.setText("" + t);
      trindexfield.setPreferredSize(new Dimension(50,trindexfield.getPreferredSize().height));
      trindexfield.addActionListener(this);
      delaylabel.setText("Delay:");
      delayfield.setText("" + frame.getDelay());
      delayfield.setPreferredSize(new Dimension(50,delayfield.getPreferredSize().height));
      delayfield.addActionListener(this);
      delaywait.setText("Wait");
      delaywait.setSelected(frame.isUserInput());
      delaywait.addActionListener(this);
      removelabel.setText("Remove By:");
      removecombo.addItem("Do nothing") ;
      removecombo.addItem("Do not remove") ;
      removecombo.addItem("To background") ;
      removecombo.addItem("To prior state") ;
      removecombo.setSelectedIndex(frame.getDisposal()) ;
      removecombo.addActionListener(this) ;
      settransparent = new ColorMenu("Change") ;
      settransparent.addMouseListener(popupListener) ;
      settransparent.addActionListener(this);
      updateColorMenu(frame.getPalette(),gif.getMultiPalette()) ;

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
      this.add(localpalette, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(localcheck, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(editpalette, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(transparentlabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(trindexfield, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(settransparent, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(delaylabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(delayfield, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(delaywait, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(removelabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(removecombo, new GridBagConstraints(1, 6, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
   }


   // Panel value update for a new image.

   void update(Image img)
   {
      if (img == null) return ;
      widthfield.setText("" + img.getWidth(null)) ;
      heightfield.setText("" + img.getHeight(null)) ;
      if (gif != null)
      {
         xoffsetfield.setText("" + gif.getOffset().x) ;
         yoffsetfield.setText("" + gif.getOffset().y) ;
         localcheck.setText("(" + gif.getColorsUsed() + ")" );
         editpalette.setEnabled(localcheck.isSelected()) ;
         settransparent.setEnabled(gif.hasPalette());
         int t = gif.getTransparentIndex() ;
         trindexfield.setText("" + t);
         updateColorMenu(gif.getPalette(),gif.getMultiPalette()) ;
      }
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		try
		{
         // Edit color palette.  A callback is registered for a ColorFrame
         // event if the palette is changed.

         if (source == editpalette)
         {
				if (gif == null) return ;
            parent.setChanged(false) ;
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				ColorFrame cf = new ColorFrame(config,gif,Integer.valueOf(gif.getMultiPalette()),framenumber) ;
            if (parent instanceof ActionListener)
				   cf.callback.addActionListener((ActionListener) parent);
				Kisekae.setCursor(parent,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				cf.show() ;
            return ;
         }

         // Convert the palette to or from a local state.  A new local palette
         // is a copy of the current global palette.

         if (source == localcheck)
         {
            editpalette.setEnabled(localcheck.isSelected()) ;
            frame.setLocalPalette(localcheck.isSelected()) ;
            gif.setFrame(framenumber);
            updateColorMenu(frame.getPalette(),gif.getMultiPalette()) ;
            parent.updatePreview() ;
            gif.setUpdated(true) ;
            return ;
         }

         // Set the 'wait for user input' setting for the frame disposal.
         // This feature is not used in UltraKiss.

         if (source == delaywait)
         {
            frame.setUserInput(delaywait.isSelected()) ;
            gif.setUpdated(true) ;
            return ;
         }

         // This action occurs if the transparent color is changed through the
         // transparent color menu popup.

         if (source == settransparent)
         {
            int n = settransparent.getColorIndex() ;
            trindexfield.setText("" + n);

            // Update the image transparent color.

            Palette p = gif.getPalette() ;
            if (p != null)
            {
               p.setTransparentIndex(n) ;
               frame.setTransparentIndex(n) ;
               frame.setTransparentColor(p.getTransparentColor(gif.getMultiPalette())) ;
               parent.updatePreview() ;
               gif.setUpdated(true) ;
            }
            return ;
         }

         // Change the animation frame removal code. This code specifies
         // how the current frame is to be replaced when the next frame
         // is shown in the animation sequence.

         if (source == removecombo)
         {
            int n = removecombo.getSelectedIndex() ;
            int disposal = frame.getDisposal() ;
            if (n != disposal)
            {
               frame.setDisposal(n) ;
               gif.setUpdated(true) ;
            }
            return ;
         }

         // Set the specific transparent color index.  Values that exceed
         // the current palette size effectively set no transparent color.

         if (source == trindexfield)
         {
            int n = -1 ;
            String s = trindexfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            int [] trans = frame.getTransparentIndex() ;
            if (trans != null && n == trans[0]) return ;

            // Update the transparent color menu.

   			if (settransparent != null)
   				settransparent.setSelectedIndex(n) ;

            // Update the image transparent color.

            Palette p = gif.getPalette() ;
            if (p != null)
            {
               p.setTransparentIndex(n) ;
               frame.setTransparentIndex(n) ;
               frame.setTransparentColor(p.getTransparentColor(gif.getMultiPalette())) ;
               parent.updatePreview() ;
               gif.setUpdated(true) ;
            }
            return ;
         }

         // Set the frame animation delay.  This value is in milliseconds.

         if (source == delayfield)
         {
            int n = 0 ;
            String s = delayfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            frame.setDelay(n) ;
            gif.setUpdated(true) ;
            return ;
         }

         // Change the frame X offset.  This repositions the frame in the
         // animated image but does not change the image size.

         if (source == xoffsetfield)
         {
            int n = 0 ;
            String s = xoffsetfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            Point offset = frame.getOffset() ;
            offset.x = n ;
            frame.setOffset(offset) ;
            parent.updatePreview() ;
            gif.setUpdated(true) ;
            return ;
         }

         // Change the frame Y offset.  This repositions the frame in the
         // animated image but does not change the image size.

         if (source == yoffsetfield)
         {
            int n = 0 ;
            String s = yoffsetfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            Point offset = frame.getOffset() ;
            offset.y = n ;
            frame.setOffset(offset) ;
            parent.updatePreview() ;
            gif.setUpdated(true) ;
            return ;
         }
      }

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
			PrintLn.println("GifLocalPanel: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
			JOptionPane.showMessageDialog(parent,
				"Internal fault.  Action not completed." + "\n" + e.toString(),
				"Internal Fault", JOptionPane.ERROR_MESSAGE) ;
		}
   }


	// A utility function to update the color selection menu to agree with
	// the current palette.

	private void updateColorMenu(Palette palette, int mp)
	{
      int cols = 16 ;
      int colors = (palette == null) ? 0 : palette.getColorCount() ;
      int rows = colors / cols + ((colors % cols == 0) ? 0 : 1) ;
      if (colors < cols) cols = colors ;
		Dimension d = new Dimension(cols,rows) ;

		// Resize the transparency and background color selection menus.

		if (settransparent != null)
			settransparent.setDimension(d) ;

		// Populate the background color selection menu.

		if (palette != null)
		{
			for (int i = 0 ; i < d.height*d.width ; i++)
			{
				Color c = palette.getColor(mp,i) ;
				if (settransparent != null) settransparent.setMenuColor(i,c) ;
			}
			if (settransparent != null)
				settransparent.setSelectedIndex(palette.getTransparentIndex()) ;
		}
      validate() ;
	}


   // A function to hide the popup menu and apply any color selections.

   public void hidePopup()
   {
      if (popup == null || settransparent == null) return ;
      settransparent.setPopupMenuVisible(false) ;
      popup.removeMouseListener(popupListener) ;
   }
}
