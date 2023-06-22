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
* GifGlobalPanel class
*
* Purpose:
*
* This class defines a display and edit panel for GIF file global attributes.
*
*/


import java.awt.* ;
import java.awt.image.* ;
import java.awt.event.* ;
import java.util.* ;
import javax.swing.* ;

final class GifGlobalPanel extends JPanel
	implements ActionListener
{
   private GifCel gif = null ;                  // GIF cel
   private ImageFrame parent = null ;           // Parent frame
   private Configuration config = null ;        // Current configuration

	// User interface objects

   private JLabel name = new JLabel();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private JLabel logicalscreen = new JLabel();
   private JTextField widthfield = new JTextField();
   private JTextField heightfield = new JTextField();
   private JLabel globalpalette = new JLabel();
   private JCheckBox globalcheck = new JCheckBox();
   private JButton editpalette = new JButton();
   private JLabel backgroundlabel = new JLabel();
   private JTextField bgindexfield = new JTextField();
   private JLabel looplabel = new JLabel();
   private JTextField loopfield = new JTextField();
   private JCheckBox loopinfinite = new JCheckBox();
	private ColorMenu setbackground = null ;
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
            if (setbackground == null) return ;
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
         if (e.getSource() == setbackground)
         {
            popup = setbackground.getPopupMenu() ;
            popup.addMouseListener(this) ;
            setbackground.setPopupMenuVisible(true) ;
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
         if (popup == null || setbackground == null) return ;
         hidePopup() ;
      }
   } ;


   // Constructor

   public GifGlobalPanel(ImageFrame f, Configuration c, GifCel cel)
   {
      parent = f ;
      gif = cel ;
      config = c ;
      init() ;
      doLayout() ;
      setPreferredSize(new Dimension(250,300)) ;
      addMouseListener(panelListener) ;
   }


   // Initialization.

   private void init()
   {
      name.setText(gif.getName());
      this.setLayout(gridBagLayout1);
      logicalscreen.setText("Screen (w,h):");
      widthfield.setText("" + gif.getSize().width);
      widthfield.setEnabled(false);
      widthfield.setPreferredSize(new Dimension(50,widthfield.getPreferredSize().height));
      heightfield.setText("" + gif.getSize().height);
      heightfield.setEnabled(false);
      heightfield.setPreferredSize(new Dimension(50,heightfield.getPreferredSize().height));
      globalpalette.setText("Global Palette:");
      globalcheck.setSelected(gif.hasGlobalPalette());
      globalcheck.setText(" (" + gif.getColorCount() + ")");
      globalcheck.addActionListener(this) ;
      editpalette.setText("Edit");
      editpalette.addActionListener(this) ;
      editpalette.setEnabled(gif.hasGlobalPalette());
      backgroundlabel.setText("Background:");
      bgindexfield.setText("" + gif.getBackgroundIndex());
      bgindexfield.setPreferredSize(new Dimension(50,bgindexfield.getPreferredSize().height));
      bgindexfield.addActionListener(this) ;
      looplabel.setText("Loop Count:");
      loopinfinite.setText("Infinite") ;
      loopinfinite.setSelected(gif.getLoopLimit() == 0);
      loopinfinite.addActionListener(this) ;
      loopfield.setText("" + gif.getLoopLimit());
      loopfield.setPreferredSize(new Dimension(50,loopfield.getPreferredSize().height));
      loopfield.addActionListener(this) ;
      setbackground = new ColorMenu("Change") ;
      setbackground.addMouseListener(popupListener) ;
      setbackground.addActionListener(this) ;
      setbackground.setEnabled(gif.hasGlobalPalette());
      updateColorMenu(gif.getPalette(),gif.getMultiPalette()) ;

      this.add(name, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
      this.add(logicalscreen, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(widthfield, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(heightfield, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(globalpalette, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(globalcheck, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(editpalette, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(backgroundlabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(bgindexfield, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
      this.add(setbackground, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
      this.add(looplabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(loopfield, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      this.add(loopinfinite, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
   }


   // Panel value update for a new image.

   void update(Image img)
   {
      if (img == null) return ;
      widthfield.setText("" + img.getWidth(null)) ;
      heightfield.setText("" + img.getHeight(null)) ;
      if (gif != null)
      {
         globalcheck.setText("(" + gif.getColorsUsed() + ")" );
         editpalette.setEnabled(gif.hasPalette());
         setbackground.setEnabled(gif.hasPalette());
         int t = gif.getBackgroundIndex() ;
         bgindexfield.setText("" + t);
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
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
            Object o = (!gif.isLocalPalette())
               ? (Object) gif.getGlobalPalette() : (Object) gif ;
				ColorFrame cf = new ColorFrame(config,o,new Integer(gif.getMultiPalette())) ;
            if (parent instanceof ActionListener)
				   cf.callback.addActionListener((ActionListener) parent);
//          parent.setViewPane(cf) ;
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				cf.show() ; 
            return ;
         }

         // Set or clear the global palette switch.  If the global palette
         // is cleared all image frames are changed to use a local palette.
         // If a global palette is set then the frame 0 palette becomes the
         // global palette.

         if (source == globalcheck)
         {
            Vector frames = gif.getFrames() ;
            if (frames == null || frames.size() < 1)
            {
               globalcheck.setSelected(!globalcheck.isSelected()) ;
               return ;
            }

            // Process removal of global palette.

            if (!globalcheck.isSelected())
            {
               Enumeration enum1 = frames.elements() ;
               while (enum1 != null && enum1.hasMoreElements())
               {
                  GifFrame frame = (GifFrame) enum1.nextElement() ;
                  frame.setLocalPalette(true) ;
               }
            }

            // Clear the global palette.

            Object [] globalpalette = gif.getGlobalPaletteArrays() ;
            globalpalette[0] = globalpalette[1] = globalpalette[2] = null ;

            // Process creation of new global palette.

            if (globalcheck.isSelected())
            {
               GifFrame frame = (GifFrame) frames.elementAt(0) ;
               frame.setLocalPalette(false) ;
            }

            // Update state.

            gif.setFrame(0) ;
            editpalette.setEnabled(gif.hasGlobalPalette()) ;
            setbackground.setEnabled(gif.hasGlobalPalette()) ;
            parent.updatePreview() ;
            gif.setUpdated(true) ;
            return ;
         }

         // Set the specific background color index.  Values that exceed
         // the current palette size effectively set no background color.

         if (source == bgindexfield)
         {
            int n = -1 ;
            String s = bgindexfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            int back = gif.getBackgroundIndex() ;
            if (n == back) return ;

            // Update the background color menu.

   			if (setbackground != null)
   				setbackground.setSelectedIndex(n) ;

            // Update the image background color.

            Palette p = gif.getPalette() ;
            if (p != null)
            {
               p.setBackgroundIndex(n) ;
               gif.setBackgroundIndex(n) ;
               gif.setBackgroundColor(p.getBackgroundColor(gif.getMultiPalette())) ;
               parent.updatePreview() ;
               gif.setUpdated(true) ;
            }
            return ;
         }

         // This action occurs if the background color is changed through the
         // background color menu popup.

         if (source == setbackground)
         {
            int n = setbackground.getColorIndex() ;
            bgindexfield.setText("" + n);
            Palette p = gif.getPalette() ;
            if (p != null)
            {
               p.setBackgroundIndex(n) ;
               gif.setBackgroundIndex(n) ;
               gif.setBackgroundColor(p.getBackgroundColor(gif.getMultiPalette())) ;
               parent.updatePreview() ;
               gif.setUpdated(true) ;
            }
         }

         // Set the infinite loop setting for animation.

         if (source == loopinfinite)
         {
            gif.setLoopLimit((loopinfinite.isSelected()) ? 0 : -1) ;
            loopfield.setText("" + gif.getLoopLimit());
            gif.setUpdated(true) ;
            return ;
         }

         // Set the maximum loop count.

         if (source == loopfield)
         {
            int n = 0 ;
            String s = loopfield.getText() ;
            if (s == null) return ;
            try { n = Integer.parseInt(s) ; }
            catch (Exception e) { return ; }
            gif.setLoopLimit(n) ;
            loopinfinite.setSelected(gif.getLoopLimit() == 0);
            gif.setUpdated(true) ;
            return ;
         }
      }

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
         if (parent != null) parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("GifGlobalPanel: Internal fault, action " + evt.getActionCommand()) ;
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

		if (setbackground != null)
			setbackground.setDimension(d) ;

		// Populate the background color selection menu.

		if (palette != null)
		{
			for (int i = 0 ; i < d.height*d.width ; i++)
			{
				Color c = palette.getColor(mp,i) ;
				if (setbackground != null) setbackground.setMenuColor(i,c) ;
			}
			if (setbackground != null)
				setbackground.setSelectedIndex(palette.getBackgroundIndex()) ;
		}
      validate() ;
	}


   // A function to hide the popup menu and apply any color selections.

   public void hidePopup()
   {
      if (popup == null || setbackground == null) return ;
      setbackground.setPopupMenuVisible(false) ;
      popup.removeMouseListener(popupListener) ;
   }
}

