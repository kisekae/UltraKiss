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



import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import java.util.* ;



// Class to construct a color selection dialog for foreground and background
// text colors.  This class is used in the ColorFrame tool.  It creates a grid
// of items each maintaining one color in the palette.  It handles mouse over
// events to highlight the selected grid item, and when an item is clicked it
// fires an action event.  After this object is created the various palette 
// colors need to be set.

class ColorMenu extends JMenu
{
	private Border unselectedBorder ;
	private Border selectedBorder ;
	private Border activeBorder ;
	private Hashtable panes ;
	private Vector panelist ;
	protected ColorPane selected ;
   
   
	// Constructor

	public ColorMenu(String name) { this(name,8,8) ; }

	public ColorMenu(String name, int rows, int cols)
	{
		super(name) ;
		unselectedBorder = new CompoundBorder(
			new MatteBorder(1, 1, 1, 1, getBackground()),
			new BevelBorder(BevelBorder.LOWERED,Color.white,Color.gray)) ;
		selectedBorder = new CompoundBorder(
			new MatteBorder(2,2,2,2,Color.red),
			new MatteBorder(1,1,1,1,getBackground())) ;
		activeBorder = new CompoundBorder(
			new MatteBorder(2, 2, 2, 2, Color.blue),
			new MatteBorder(1, 1, 1, 1, getBackground())) ;
		init(rows,cols) ;
	}

	// Initialization.

	private void init(int rows, int cols)
	{
      if (rows == 0 && cols == 0) return ;
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5)) ;
		p.setLayout(new GridLayout(rows,cols)) ;
		panes = new Hashtable() ;
		panelist = new Vector() ;
		selected = null ;
		removeAll() ;

		// Populate the default menu colors.    Note that the hashtable keys are
		// all opaque colors.

		int n = rows * cols ;
		for (int i = 0 ; i < n ; i++)
		{
			Color c = getDefaultColor(i,n) ;
			ColorPane pn = new ColorPane(c,i) ;
			p.add(pn) ;
			panes.put(c,pn) ;
			panelist.addElement(pn);
		}
		add(p);
   }

   // Function to return a color based upon an index value.

   static Color getDefaultColor(int index, int n)
   {
   	switch (index)
      {
			case 0: return Color.getHSBColor(0f,0f,1.0f) ;
			case 1: return Color.getHSBColor(0f,0f,(6.f/7)) ;
			case 2: return Color.getHSBColor(0f,0f,(5.f/7)) ;
			case 3: return Color.getHSBColor(0f,0f,(4.f/7)) ;
			case 4: return Color.getHSBColor(0f,0f,(3.f/7)) ;
			case 5: return Color.getHSBColor(0f,0f,(2.f/7)) ;
			case 6: return Color.getHSBColor(0f,0f,(1.f/7)) ;
			case 7: return Color.getHSBColor(0f,0f,0f) ;
			case 8: return Color.red ;
	      case 9: return Color.pink ;
	      case 10: return Color.orange ;
	      case 11: return Color.yellow ;
	      case 12: return Color.green ;
	      case 13: return Color.cyan ;
	      case 14: return Color.blue ;
	      case 15: return Color.magenta ;
	      default: return Color.getHSBColor((index-16)*(1.0f/n),1.0f,1.0f) ;
      }
   }

	// Set the grid dimensions.

	public void setDimension(Dimension d)
	{ init(d.height,d.width) ;	}

	// Set the selected color.  Note that the hashtable keys are opague colors.

	public void setSelectedColor(Color c)
	{
		if (c == null)
      {
			if (selected != null) selected.setSelected(false) ;
      	selected = null ;
         return ;
      }
		int rgb = c.getRGB() ;
		rgb = rgb & 0x00ffffff ;
		c = new Color(rgb) ;
		Object obj = panes.get(c) ;
		if (obj == null) return ;
		if (selected != null) selected.setSelected(false) ;
		selected = (ColorPane) obj ;
		selected.setSelected(true) ;
	}

	// Set the selected color.

	public void setSelectedIndex(int index)
	{
		if (index < 0 || index >= panelist.size())
      {
			if (selected != null) selected.setSelected(false) ;
      	selected = null ;
         return ;
      }
		Object obj = panelist.elementAt(index) ;
		if (!(obj instanceof ColorPane)) return ;
		if (selected != null) selected.setSelected(false) ;
		selected = (ColorPane) obj ;
		selected.setSelected(true) ;
	}

	// Set the color of the specified index.   Note that the hashtable keys are
	// opaque colors.

	public void setMenuColor(int index, Color c)
	{
		if (c == null) return ;
		if (index >= panelist.size()) return ;
		ColorPane obj = (ColorPane) panelist.elementAt(index) ;
		panes.remove(obj.getColor()) ;
		obj.setColor(c) ;
		panes.put(c,obj) ;
	}

	// Return the selected color.

	public Color getColor()
	{
		if (selected == null) return null ;
		return selected.getColor() ;
	}

	// Return the selected color pane.

	ColorPane getSelected() { return selected ; }

	// Return the selected color index.

	public int getColorIndex()
	{
		if (selected == null) return -1 ;
		return selected.getIndex() ;
	}

	// Fire the ColorMenu action event.

	public void doSelection()
	{
		fireActionPerformed(new ActionEvent(this,
			ActionEvent.ACTION_PERFORMED, getActionCommand())) ;
	}


	// Inner ColorMenu class to construct a color pane for one defined
	// color.  This pane captures mouse over events and recognizes
   // click actions if a pane is selected by the user.

	class ColorPane extends JPanel implements MouseListener
	{
		private Color c;
      private int index ;
		private boolean isselected;

		// Constructor

		public ColorPane(Color c, int index)
		{
			this.c = c ;
         this.index = index ;
			setBorder(unselectedBorder) ;
			if (c != null)
			{
				setBackground(c) ;
				String msg = Kisekae.getCaptions().getString("ColorIndexText")
               + " " + index + " RGB [" + c.getRed() + ","
            	+ c.getGreen() + "," + c.getBlue() + "]" ;
				setToolTipText(msg) ;
			}
			addMouseListener(this) ;
		}

		public Color getColor() { return c ; }

      public int getIndex() { return index ; }

		public void setColor(Color c)
		{
			this.c = c ;
			if (c == null) return ;
			setBackground(c) ;
			String msg = Kisekae.getCaptions().getString("ColorIndexText")
             + index + " RGB [" + c.getRed() + ","
         	+ c.getGreen() + "," + c.getBlue() + "]" ;
			setToolTipText(msg) ;
		}

		public Dimension getPreferredSize() { return new Dimension(15,15) ; }
		public Dimension getMaximumSize() { return getPreferredSize() ; }
		public Dimension getMinimumSize() { return getPreferredSize() ; }

		public void setSelected(boolean selected)
		{
			isselected = selected ;
			if (selected)
				setBorder(selectedBorder) ;
			else
				setBorder(unselectedBorder) ;
		}

		public boolean isSelected() { return isselected ; }

		public void mousePressed(MouseEvent e) { }

      // mouseReleased() does not seem to fire?
		public void mouseReleased(MouseEvent e) { }

		public void mouseClicked(MouseEvent e)
		{
// 		MenuSelectionManager.defaultManager().clearSelectedPath() ;
			setBorder(unselectedBorder) ;
      	if (SwingUtilities.isRightMouseButton(e))
         {
            if (getSelected() == this)
					setSelectedColor(null) ;
            else
            	return ;
         }
			else
         {
				if (selected != null) selected.setSelected(false) ;
				setSelected(true) ;
            selected = this ;
         }
			doSelection() ;
		}

		public void mouseEntered(MouseEvent e)
		{ setBorder(activeBorder); }

		public void mouseExited(MouseEvent e)
		{ setBorder(isselected ? selectedBorder : unselectedBorder) ; }
	}
}
