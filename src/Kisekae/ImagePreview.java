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
* ImagePreview class
*
* Purpose:
*
* This class constructs a panel in which a scaled image can be drawn and
* previewed.  The image can be magnified or reduced in size by clicking
* with the left and right mouse buttons.
*
* The image is scaled and centered to fit within the size of this panel.
*
*/


import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import java.awt.geom.AffineTransform ;
import javax.swing.*;
import java.net.URL ;
import java.net.MalformedURLException ;


class ImagePreview extends JPanel
	implements ActionListener, MouseListener, MouseMotionListener, ComponentListener
{
   private KissFrame parent = null ;      // The parent frame
	protected Image image = null ;			// The base image to preview
	protected Image scaledimage = null ;   // The scaled image to paint
	private Point pixel = null ;				// The selected pixel location
   private ImageScaleThread lazy = null ;	// Our lazy scaling thread
   private Dimension panelsize = null ;   // The panel display size
   private boolean loading = false ;  		// If true, image is being loaded
   private boolean scaling = false ;  		// If true, image is being scaled
   private boolean nomemory = false ;		// If true, out of memory
   private boolean showstate = true ;		// If true, show image load state

	// Image scaling factors.

	private Rectangle clip ;					// Preview image clip rectangle
	private Rectangle undoclip ;				// Initial drag clip rectangle
	private float scale ;						// Base image scale factor
	private float multiple ;					// Current scaling multiplier
	private int centerX, centerY ;			// Image centering coordinates
	private int x, y ;							// Paint coordinates, includes insets
	private int iw, ih ;							// Base image width and height
	private int sw, sh ;							// Scaled image width and height
   private int posX, posY ;					// Drag position coordinates
   private int transparency ;					// Image transparency, 255 is opague

	// Image selection box

	private Rectangle selectbox = null ;	// Mouse drag selection area
	private Rectangle drawbox = null ;		// Graphics drawing area

	// Toolbar interface objects

	private JToolBar toolbar = null ;
	private JToggleButton SELECT = null ;
	private JToggleButton CROP = null ;
	private JToggleButton MAGNIFY = null ;
	private JToggleButton POSITION = null ;
	private JToggleButton PICKUP = null ;
   
   // Status bar 
   
   private StatusBar statusbar = null ;

   // Cursors

   private Cursor selectcursor = null ;
   private Cursor positioncursor = null ;
   private Cursor magnifycursor = null ;
   private Cursor pickupcursor = null ;

	// Our color pickup callback button that other components can attach
	// listeners to.

	protected CallbackButton callback = new CallbackButton(this,"ImagePreview Callback") ;
	private boolean pickupcontrol = false ;
	private boolean pickupshift = false ;
   private boolean pickupmeta = false ;


	// Constructor

	public ImagePreview()
	{
   	URL url = null ;
   	Toolkit toolkit = Toolkit.getDefaultToolkit() ;
      transparency = 255 ;

      // Create default cursors.

      selectcursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) ;
      positioncursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) ;
      magnifycursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ;
      pickupcursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ;

      // Create the custom cursors.

      try
      {
         Dimension d = toolkit.getBestCursorSize(32,32) ;
         if (!(d == null || d.width == 0 || d.height == 0))
         {
            url = Kisekae.getResource("Images/magnifycursor.gif") ;
            ImageIcon icon = (url == null) ? null : new ImageIcon(url) ;
            Image img = (icon == null) ? null : icon.getImage() ;
            if (img != null) magnifycursor = toolkit.createCustomCursor(img,
              	new Point(13,13),"Magnify") ;
            url = Kisekae.getResource("Images/pickupcursor.gif") ;
            icon = (url == null) ? null : new ImageIcon(url) ;
            img = (icon == null) ? null : icon.getImage() ;
            if (img != null) pickupcursor = toolkit.createCustomCursor(img,
              	new Point(10,21),"Eyedropper") ;
         }
      }
      catch (Exception e)
      {
         PrintLn.println("ImagePreview: unable to create custom cursors.") ;
         e.printStackTrace() ;
      }

      // Add event listeners.

   	createToolBar() ;
		addMouseListener(this) ;
		addMouseMotionListener(this) ;
      addComponentListener(this) ;
      doLayout() ;
		validate() ;
	}

	public ImagePreview(Image i)
	{ this() ; setImage(i) ; }


	// Set the scaled preview image.  We must kill any active scaling threads.

	void setImage(Image img)
	{
      if (lazy != null && lazy.isAlive())
         { lazy.interrupt() ;  lazy = null ; }

      // Establish the new image parameters.

		image = img ;
    	scaledimage = img ;
      if (img == null) { repaint() ; return ; }
		iw = image.getWidth(null) ;
		ih = image.getHeight(null) ;
		clip = new Rectangle(0,0,iw,ih) ;

		// We display the whole image at scale multiple 1.  We will construct
      // a scaled image that is guaranteed to fit within our preview panel.
      // The image will not be scaled if it already fits, but it can be
      // reduced if necessary.  The scale value is the factor that the
      // base image must be scaled by to fit within the panel area.

		multiple = 1 ;
		Dimension d = panelsize ;
		if (d == null) d = getSize() ;
      d = new Dimension(d) ;
		Insets insets = getInsets() ;
		d.width -= (insets.left + insets.right) ;
		d.height -= (insets.top + insets.bottom) ;
		int minw = (iw < d.width) ? iw : d.width ;
		int minh = (ih < d.height) ? ih : d.height ;
		float scalew = ((float) minw) / iw ;
		float scaleh = ((float) minh) / ih ;
		scale = (scalew < scaleh) ? scalew : scaleh ;
		sw = (int) (iw * scale) ;
		sh = (int) (ih * scale) ;

		// Scale the image as a new lazy thread.
      
		loading = true ;
      showstate = true ;
 		drawImage() ;
	}


	// Get the base preview image.

	Image getImage() 
   { 
      if (image == null) return null ; 
      if (image instanceof BufferedImage) return image ;
      int w = image.getWidth(null) ;
      int h = image.getHeight(null) ;
      BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
      Graphics gc = img.getGraphics() ;
      gc.drawImage(image,0,0,null) ;
      gc.dispose() ;
      return img ;     
   }


	// Update the scaled preview image.  This method is used to replace
   // an image with a modified copy.  It retains the existing clip area
   // and magnification.

	void updateImage(Image img)
	{
      if (image == null)
      {
         setImage(img) ;
         return ;
      }

      // Interrupt any active scaling thread.

      if (lazy != null && lazy.isAlive())
         { lazy.interrupt() ;  lazy = null ; }

      // Establish the new image parameters.

		image = img ;
   	scaledimage = null ;
      if (image == null) { repaint() ; return ; }
		iw = image.getWidth(null) ;
		ih = image.getHeight(null) ;

		// Scale the image as a new lazy thread.

      if (scaleImage(multiple))
      {
   		loading = true ;
   		drawImage() ;
      }
      else
      {
         scaledimage = image ;
         repaint() ;
      }
   }


	// Create a tool bar for preview image control.

	private void createToolBar()
	{
		URL iconfile = null ;
		toolbar = new JToolBar() ;
		toolbar.setFloatable(false) ;
		SELECT = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/select.gif") ;
		if (iconfile != null) SELECT.setIcon(new ImageIcon(iconfile)) ;
		SELECT.setMargin(new Insets(1,1,1,1)) ;
		SELECT.setAlignmentY(0.5f) ;
		SELECT.addActionListener(this) ;
		SELECT.setToolTipText(Kisekae.getCaptions().getString("ToolTipPreviewSelect")) ;
		CROP = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/crop.gif") ;
		if (iconfile != null) CROP.setIcon(new ImageIcon(iconfile)) ;
		CROP.setMargin(new Insets(1,1,1,1)) ;
		CROP.setAlignmentY(0.5f) ;
		CROP.addActionListener(this) ;
		CROP.setToolTipText(Kisekae.getCaptions().getString("ToolTipPreviewCrop")) ;
		POSITION = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/hand.gif") ;
		if (iconfile != null) POSITION.setIcon(new ImageIcon(iconfile)) ;
		POSITION.setMargin(new Insets(1,1,1,1)) ;
		POSITION.setAlignmentY(0.5f) ;
		POSITION.addActionListener(this) ;
		POSITION.setToolTipText(Kisekae.getCaptions().getString("ToolTipPreviewPosition")) ;
		MAGNIFY = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/magnify.gif") ;
		if (iconfile != null) MAGNIFY.setIcon(new ImageIcon(iconfile)) ;
		MAGNIFY.setMargin(new Insets(1,1,1,1)) ;
		MAGNIFY.setAlignmentY(0.5f) ;
		MAGNIFY.addActionListener(this) ;
		MAGNIFY.setToolTipText(Kisekae.getCaptions().getString("ToolTipPreviewMagnify")) ;
		PICKUP = new JToggleButton() ;
		iconfile = Kisekae.getResource("Images/eyedropper.gif") ;
		if (iconfile != null) PICKUP.setIcon(new ImageIcon(iconfile)) ;
		PICKUP.setMargin(new Insets(1,1,1,1)) ;
		PICKUP.setAlignmentY(0.5f) ;
		PICKUP.addActionListener(this) ;
		PICKUP.setToolTipText(Kisekae.getCaptions().getString("ToolTipPreviewPickup")) ;
		ButtonGroup bg1 = new ButtonGroup() ;
		bg1.add(SELECT) ;
		bg1.add(CROP) ;
		bg1.add(POSITION) ;
		bg1.add(MAGNIFY) ;
		bg1.add(PICKUP) ;
		SELECT.setSelected(true) ;
		setCursor(selectcursor) ;
		toolbar.add(SELECT, null) ;
		toolbar.add(CROP, null) ;
		toolbar.add(POSITION, null) ;
		toolbar.add(MAGNIFY, null) ;
		toolbar.add(PICKUP, null) ;
	}


   // Method to return our current toolbar.

   JToolBar getToolBar() { return toolbar ; }
   
   
   // Method to set our status bar reference.
   
   void setStatusBar(StatusBar sb) { statusbar = sb ; }


   // Method to return our current pixel location.

   Point getPixel() { return pixel ; }

   
   // Method to return our current clip rectangle.

   Rectangle getClip() { return clip ; }

   
   // Method to return our current selection rectangle.  This is in 
   // original image coordinates.

   Rectangle getSelection() 
   { 
      if (clip == null) return null ;
      if (selectbox == null) return null ;
	   int ix = selectbox.x - x ;
	   int iy = selectbox.y - y ;
      ix = clip.x + (int) ((((float) ix) / sw) * clip.width) ;
      iy = clip.y + (int) ((((float) iy) / sh) * clip.height) ;
      int iw = (int) (selectbox.width * scale) ;
      int ih = (int) (selectbox.height * scale) ;
      return new Rectangle(ix,iy,iw,ih) ; 
   }
   

   // Method to clear our current selection rectangle.  

   public void clearSelection() { selectbox = null ; }
   

   // Method to set our required image transparency.  

   public void setTransparency(int n) { transparency = n ; }
   

   // Overload setSize() to retain our specified size.

   public void setSize(int w, int h)
   {
      super.setSize(w,h) ;
      panelsize = new Dimension(w,h) ;
   }

   public void setSize(Dimension d)
   {
      super.setSize(d) ;
      panelsize = d ;
   }


   // Method to return our callback button.

   AbstractButton getCallback() { return callback ; }


	// Methods to return our mouse modifier keys.

	boolean isMetaDown() { return pickupmeta ; }
	boolean isShiftDown() { return pickupshift ; }
	boolean isControlDown() { return pickupcontrol ; }


   // Method to set our show status option.  This controls if status information
   // sucj as loading or scaling text is shown in the preview window while image
   // processing is underway.

   void setShowState(boolean b) { showstate = b ; }


	// Image painting.

	public void paintComponent(Graphics g)
	{
		final int space = 2 ;      	// size of line segment space
		final int segment = 4 ;			// size of line segment

      try
      {
   		super.paintComponent(g) ;
   		Dimension d = panelsize ;
         if (d == null) d = getSize() ;
         d = new Dimension(d) ;
         Insets insets = getInsets() ;
         d.width -= (insets.left + insets.right) ;
         d.height -= (insets.top + insets.bottom) ;

         // Examine the image state.

			String s = null ;
			if (loading) s = Kisekae.getCaptions().getString("PreviewLoadingText") ;
			if (scaling) s = Kisekae.getCaptions().getString("PreviewScalingText") ;
         if (nomemory) s = Kisekae.getCaptions().getString("PreviewMemoryText") ;
			if (image == null) s = Kisekae.getCaptions().getString("PreviewNoImageText") ;
			if (s != null)
			{
				FontMetrics fm = getFontMetrics(getFont()) ;
				x = (d.width - fm.stringWidth(s)) / 2 + insets.left ;
				y = (d.height + fm.getAscent()) / 2 + insets.top ;
				if (showstate) g.drawString(s,x,y) ;
	         return ;
			}
      
         // Set the transparency.

         if (transparency < 255) 
         {
            float t = transparency / 255.0f ;
            if (t > 1) t = 1 ; else if (t < 0) t = 0 ;
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,t) ;
            if (g instanceof Graphics2D) ((Graphics2D) g).setComposite(ac) ;
         }

			// We have an image to paint.
	      // Draw the scaled image centered in the panel.

			x = (d.width - sw) / 2 + insets.left ;
			y = (d.height - sh) / 2 + insets.top ;
			if (x < insets.left) x = insets.left ;
			if (y < insets.top) y = insets.top ;
			if (scaledimage != null)
      		g.drawImage(scaledimage,x,y,null) ;

			// Draw the selection box.

			if (selectbox == null) return ;
         if (selectbox.width <= 0) return ;
         if (selectbox.height <= 0) return ;
			g.setColor(Color.black) ;
			g.setXORMode(Color.white) ;
			int colsegments = (selectbox.width - space) / (segment + space) ;
			int rowsegments = (selectbox.height - space) / (segment + space) ;

			// Draw the horizontal lines.

			int x1 = selectbox.x ;
			int y1 = selectbox.y ;
			int x2 = selectbox.x + selectbox.width - 1 ;
			int y2 = selectbox.y + selectbox.height - 1 ;
			for (int i = 0 ; i < colsegments ; i++)
			{
				x2 = x1 + segment ;
				g.drawLine(x1,y1,x2,y1) ;
				g.drawLine(x1,y2,x2,y2) ;
				x1 += (segment + space) ;
			}
			x2 = selectbox.x + selectbox.width - 1 ;
			g.drawLine(x1,y1,x2,y1) ;
			g.drawLine(x1,y2,x2,y2) ;

			// Draw the vertical lines.

			x1 = selectbox.x ;
			y1 = selectbox.y ;
			x2 = selectbox.x + selectbox.width - 1 ;
			y2 = selectbox.y + selectbox.height - 1 ;
			for (int i = 0 ; i < rowsegments ; i++)
			{
				y2 = y1 + segment ;
				g.drawLine(x1,y1,x1,y2) ;
				g.drawLine(x2,y1,x2,y2) ;
				y1 += (segment + space) ;
			}
			y2 = selectbox.y + selectbox.height - 1 ;
			g.drawLine(x1,y1,x1,y2) ;
			g.drawLine(x2,y1,x2,y2) ;
		}

      // Watch for memory faults.

		catch (OutOfMemoryError e)
		{
      	scaledimage = null ;
			Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
			PrintLn.println("ImagePreview: Out of memory.") ;
         nomemory = true ;
         showstate = true ;
		}

      // Watch for general exceptions.

      catch (Exception e)
      {
         image = null ;
         scaledimage = null ;
         Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
         PrintLn.println("ImagePreview: Exception " + e.toString()) ;
         e.printStackTrace() ;
         showstate = true ;
      }
	}


   // Image drawing.  Start a new lazy scale thread if necessary.
   // The repaint shows any status messages.  The lazy thread will
   // do a repaint() when it completes.

   synchronized void drawImage()
   {
   	if (lazy != null && lazy.isAlive()) lazy.restart() ;
   	if (lazy == null || !lazy.isAlive())
      {
      	lazy = new ImageScaleThread(image) ;
         lazy.start() ;
      }
	}
   
   
   // Status trace information.
   
   void showTrace()
   {
      if (statusbar == null) return ;
      String trace = "" ;
      if (selectbox != null) 
      {
         trace += "location [" +
         selectbox.x + "," + selectbox.y + "," +
         selectbox.width + "," + selectbox.height + "]" ;
      }
      statusbar.showStatus(trace) ;
   }


	// The action method implements the ActionListener interface to capture
	// events on the preview image toolbar.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

      if (source == SELECT)
      	setCursor(selectcursor) ;
      else if (source == CROP)
      	setCursor(selectcursor) ;
      else if (source == POSITION)
      {
      	undoclip = null ;
      	setCursor(positioncursor) ;
      }
      else if (source == MAGNIFY)
      	setCursor(magnifycursor) ;
      else if (source == PICKUP)
      {
      	setCursor(pickupcursor) ;
	      drawImage() ;
      }
	}


	// The action on a mouse down event is to adjust the image scale and
   // position based upon the current mouse coordinates and cursor type.

	public void mousePressed(MouseEvent e)
	{
		if (image == null) return ;
		selectbox = null ;
      posX = posY = 0 ;

		// Establish a new selection box if we are selecting an area.
		// On a right mouse click we restore the image to its original
		// size.  On a left mouse click we establish the start of our
		// drag box.

		if (SELECT.isSelected())
		{
			if (SwingUtilities.isRightMouseButton(e))
			{
				multiple = 1 ;
				clip = new Rectangle(0,0,iw,ih) ;
				sw = (int) (iw * scale) ;
				sh = (int) (ih * scale) ;
				scaling = true ;
				scaledimage = null ;
				drawImage() ;
			}
			else
         {
         	int px = e.getX() ;
            int py = e.getY() ;
            if (px < x) px = x ;
            if (py < y) py = y ;
				selectbox = new Rectangle(px,py,0,0) ;
         }
         showTrace() ;
			return ;
		}

		// Establish a new selection box if we are cropping an area.
		// On a right mouse click we clear any selection box.  On a left
      // mouse click we establish the start of our drag box.

		if (CROP.isSelected())
		{
			if (SwingUtilities.isRightMouseButton(e))
			{
            selectbox = null ;
				drawImage() ;
			}
			else
         {
           	int px = e.getX() ;
            int py = e.getY() ;
            if (px < x) px = x ;
            if (py < y) py = y ;
      		selectbox = new Rectangle(px,py,0,0) ;
         }
         showTrace() ;
			return ;
		}

		// For magnification we determine the new center image location.
		// The centering coordinates are computed relative to the original
		// image bounds.

		if (MAGNIFY.isSelected())
		{
      	int px = e.getX() ;
         int py = e.getY() ;
			if (px < x) return ;
			if (py < y) return ;
			if (px > x+sw) return ;
			if (py > y+sh) return ;

         // Calculate the image center position.  We first calculate the
         // appropriate fraction of the scaled width and height, then
         // apply this fraction to the clip width and height.

			float fx = (px - x) / (float) (sw) ;
			float fy = (py - y) / (float) (sh) ;
 			centerX = (int) (fx * clip.width + clip.x) ;
  			centerY = (int) (fy * clip.height + clip.y) ;

			// If right mouse button pressed, reduce the magnification.
			// If left mouse button pressed, increase the magnification.
         // If we have reduced to the minimum then we revert to the
         // original image.

         float interval = 1.66f ;
			if (SwingUtilities.isRightMouseButton(e)) multiple /= interval ; else multiple *= interval ;
         if (multiple < 1)
         {
         	multiple = 1 ;
				clip = new Rectangle(0,0,iw,ih) ;
				sw = (int) (iw * scale) ;
				sh = (int) (ih * scale) ;
			}

			// Repaint the scaled image.  The image is cropped to the
			// base clip area to minimize scaling memory requirements.

			scaleImage(multiple) ;
			scaling = true ;
			scaledimage = null ;
			drawImage() ;
         return ;
		}

      // If we are positioning the image, retain the initial mouse down
      // coordinates.  Whenever we position the image we can always restore
      // the initial state through a right mouse click.

      if (POSITION.isSelected())
      {
      	if (clip == null) return ;
         if (undoclip == null) undoclip = new Rectangle(clip) ;
      	int px = e.getX() ;
         int py = e.getY() ;
			if (px < x) return ;
			if (py < y) return ;
			if (px > x+sw) return ;
			if (py > y+sh) return ;
         posX = px ;
         posY = py ;
         return ;
      }
	}


	// The action on a mouse drag event is to adjust the drag selection box
   // size if we are selecting an area.  If we are positioning the image
   // we adjust the image clip position.

	public void mouseDragged(MouseEvent e)
	{
		if (image == null) return ;
		if (SwingUtilities.isRightMouseButton(e)) return ;

      // If we are in SELECT or CROP mode, update our select box.

		if (SELECT.isSelected() || CROP.isSelected())
      {
			if (selectbox == null) return ;
	      if (drawbox == null) drawbox = selectbox ;
         int px = e.getX() ;
         int py = e.getY() ;
         Insets insets = getInsets() ;
         if (px > sw+x) px = sw + x ;
         if (py > sh+y) py = sh + y ;
			selectbox.width = px - selectbox.x ;
			selectbox.height = py - selectbox.y ;
	      drawbox = drawbox.union(selectbox) ;
	      int x1 = drawbox.x - 1 ;
	      int y1 = drawbox.y - 1 ;
	      int x2 = drawbox.x + drawbox.width + 2 ;
	      int y2 = drawbox.y + drawbox.height + 2 ;
			repaint(x1,y1,x2,y2) ;
	      drawbox = selectbox ;
         showTrace() ;
         return ;
      }

      // If we are positioning the preview image we need to adjust the
      // image clip area and rescale the image.  Not very efficient.

      if (POSITION.isSelected())
      {
      	if (clip == null) return ;
      	int px = e.getX() ;
         int py = e.getY() ;
         int xoffset = posX - px ;
         int yoffset = posY - py ;
         clip.x += xoffset ;
         clip.y += yoffset ;
         if (clip.x < 0) clip.x = 0 ;
         if (clip.y < 0) clip.y = 0 ;
         if (clip.x + clip.width > iw) clip.x = iw - clip.width ;
         if (clip.y + clip.height > ih) clip.y = ih - clip.height ;
         posX = px ;
         posY = py ;
         drawImage() ;
      }
	}


	// The action on a mouse release event is to show the selection box
	// if we are selecting an area.  We pickup a pixel color if we are
   // in color selection mode.

	public void mouseReleased(MouseEvent e)
	{
		if (image == null) return ;

      // If we are in PICKUP mode we must identify the chosen location.
      // The pixel point coordinate is relative to the original image.

      if (PICKUP.isSelected())
      {
      	pixel = null ;
      	if (scaledimage == null) return ;
         pickupcontrol = e.isControlDown() ;
         pickupshift = e.isShiftDown() ;
         pickupmeta = SwingUtilities.isRightMouseButton(e) ;
	      int ix = e.getX() - x ;
	      int iy = e.getY() - y ;
         if (ix < 0 || iy < 0) return ;
         if (ix > sw || iy > sh) return ;
         ix = clip.x + (int) ((((float) ix) / sw) * clip.width) ;
         iy = clip.y + (int) ((((float) iy) / sh) * clip.height) ;
         pixel = new Point(ix,iy) ;
			callback.doClick() ;
         return ;
      }

      // If we are in SELECT mode we must display the selected area.

		if (SELECT.isSelected())
      {
			if (selectbox == null) return ;
      	if (scaledimage == null) return ;

			// Establish our final selection box.

			selectbox.width = e.getX() - selectbox.x ;
			selectbox.height = e.getY() - selectbox.y ;
	      if (selectbox.width <= 0 || selectbox.height <= 0)
	      {
	      	selectbox = null ;
	         drawbox = null ;
            showTrace() ;
	         return ;
	      }

			// Determine the new center image location.  The centering
         // coordinates are computed relative to the original image bounds.

         centerX = selectbox.x + (selectbox.width / 2) ;
         centerY = selectbox.y + (selectbox.height / 2) ;
			float fx = (centerX - x) / (float) (sw) ;
			float fy = (centerY - y) / (float) (sh) ;
			centerX = (int) (fx * clip.width + clip.x) ;
			centerY = (int) (fy * clip.height + clip.y) ;

         // Compute the required magnification for the selected area
         // to fill the panel.

   		Dimension d = panelsize ;
   		if (d == null) d = getSize() ;
         d = new Dimension(d) ;
			Insets insets = getInsets() ;
			d.width -= (insets.left + insets.right) ;
			d.height -= (insets.top + insets.bottom) ;
         float multw = (d.width * multiple) / selectbox.width ;
         float multh = (d.height * multiple) / selectbox.height ;
         multiple = Math.min(multw,multh) ;

			// Scale a new clipped image as a lazy thread.

         scaleImage(multiple) ;
			selectbox = null ;
	      drawbox = null ;
			scaling = true ;
			scaledimage = null ;
			drawImage() ;
         showTrace() ;
         return ;
      }

      // If we are in CROP mode we must display the selected area.

		if (CROP.isSelected())
      {
			if (selectbox == null) return ;
      	if (scaledimage == null) return ;

			// Establish our final selection box.

			selectbox.width = e.getX() - selectbox.x ;
			selectbox.height = e.getY() - selectbox.y ;
	      if (selectbox.width <= 0 || selectbox.height <= 0)
	      {
	      	selectbox = null ;
	         drawbox = null ;
	      }
         return ;
      }

      // If we are positioning the preview image we update the image at
      // its final location.  If the right mouse button was pressed the
      // image is restored to its starting point prior to the last drag.

      if (POSITION.isSelected())
      {
      	if (clip == null) return ;
      	int px = e.getX() ;
         int py = e.getY() ;
         int xoffset = posX - px ;
         int yoffset = posY - py ;
         clip.x += xoffset ;
         clip.y += yoffset ;

         // Restore our initial state?

         if (SwingUtilities.isRightMouseButton(e) && undoclip != null)
         	clip = new Rectangle(undoclip) ;

         // Ensure that the clip rectangle is sound.

         if (clip.x < 0) clip.x = 0 ;
         if (clip.y < 0) clip.y = 0 ;
         if (clip.x + clip.width > iw) clip.x = iw - clip.width ;
         if (clip.y + clip.height > ih) clip.y = ih - clip.height ;
         posX = posY = 0 ;
         drawImage() ;
         return ;
      }
	}


	// Utility function to calculate an image clip area for a magnified
	// image.  We compute the new image clip bounds.  We calculate
	// the correct clip bounds within the base image so that when
	// the clip is magnified to the required panel size it displays
   // at the proper scale.

	private boolean scaleImage(float multiple)
	{
      if (clip == null) return false ;
		Dimension d = panelsize ;
		if (d == null) d = getSize() ;
      d = new Dimension(d) ;
		Insets insets = getInsets() ;
		d.width -= (insets.left + insets.right) ;
		d.height -= (insets.top + insets.bottom) ;

      // Compute the actual width and height of the magnified image.

		sw = (int) ((iw * scale) * multiple) ;
		sh = (int) ((ih * scale) * multiple) ;

      // Ensure magnified image fits within our panel area.

		int minw = (sw < d.width) ? sw : d.width ;
		int minh = (sh < d.height) ? sh : d.height ;

      // Compute a scale factor that ensures the magnified image fits.

		float scalew = ((float) minw) / sw ;
		float scaleh = ((float) minh) / sh ;

      // Calculate the bounds of the magnified image area rectangle.

      if (centerX == 0) centerX = d.width / 2 ;
      if (centerY == 0) centerY = d.height / 2 ;      
		clip.width = (int) (iw * scalew) ;
		clip.height = (int) (ih * scaleh) ;
		clip.x = centerX - (clip.width / 2) ;
		clip.y = centerY - (clip.height / 2) ;

      // Ensure that the rectangle bounds are sound.

		if (clip.x+clip.width > iw) clip.x = iw - clip.width ;
		if (clip.y+clip.height > ih) clip.y = ih - clip.height ;
		if (clip.x < 0) clip.x = 0 ;
		if (clip.y < 0) clip.y = 0 ;

      // Set the width and height of the new scaled image.

		sw = (int) (sw * scalew) ;
		sh = (int) (sh * scaleh) ;
      return (sw != iw || sh != ih) ;
	}


   // Non-implemented mouse listener methods.

	public void mouseExited(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseClicked(MouseEvent e) { }
	public void mouseMoved(MouseEvent e) { }
   
   
   // Component listener events for resizing
   
   public void componentResized(ComponentEvent e) { setImage(image) ; }
   public void componentHidden(ComponentEvent e) {}
   public void componentMoved(ComponentEvent e) {}
   public void componentShown(ComponentEvent e) {}



   // Inner class to define the lazy image scaling thread.

	class ImageScaleThread extends Thread
   {
   	private boolean restart = true ;
      protected Object lock = new Object() ;
      private Image sourceimage = null ;
      
      public ImageScaleThread(Image image)
      {
         sourceimage = image ;
      }

      // Thread run code

     	public void run()
      {
        	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

         // Repeat the scaling if we are told to restart at any time during
         // this execution.

         while (restart)
         {
            restart = false ;
            if (clip == null) break ;
            if (image == null) break ;
            if (sw == 0 || sh == 0) break ;               

            // Do the scaling on the AWT thread.  
            
            Runnable awt = new Runnable()
            { public void run() { doScaling(sourceimage) ; } } ;
            SwingUtilities.invokeLater(awt) ;
         }               

         synchronized (lock)
         {
           try { lock.wait(); }
            catch (Exception e) { }
         }
         
         loading = false ;
         scaling = false ;
         Runnable awt = new Runnable()
         { public void run() { repaint() ; } } ;
         SwingUtilities.invokeLater(awt) ;
      }

      
      // Method to set the restart flag.

      void restart() { restart = true ; }

      
      // Function to do the image scaling.  This must run on the AWT thread.

      private void doScaling(Image image)
      {
        try
         {
            ImageProducer ip = image.getSource() ;
            ImageFilter filter = new CropImageFilter(clip.x,clip.y,clip.width,clip.height) ;
            ip = new FilteredImageSource(ip,filter) ;
            Image cropimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            scaledimage = createResizedCopy(cropimage,sw,sh,true) ;
//          scaledimage = cropimage.getScaledInstance(sw,sh,Image.SCALE_AREA_AVERAGING) ;

         // Not sure why the mediatracker is required, but without this the
         // image does not always display.

//          MediaTracker mt = new MediaTracker(Kisekae.getKisekae()) ;
//          mt.addImage(scaledimage,0) ;
//          mt.waitForAll(500) ; 
         }
     		catch (Exception e) 
         { 
            PrintLn.println("ImagePreview: lazy thread failure") ;
            e.printStackTrace();
         }
         
         synchronized (lock)
         {
            try { lock.notify(); }
            catch (Exception e) { }
         }
      }            
      
      
      // Create a BufferedImage of the desired size and draw the original 
      // image into it, scaling on the fly. Note that depending on whether 
      // your original image is opaque or non-opaque (that is, if it's 
      // translucent or transparent), you may need to create an image with 
      // an alpha channel.  Using Image.getScaledInstance() is not 
      // recommended. The preferred approach is to create a new Image 
      // and paint to it, scaling on the fly, to take advantage of faster loops.


      BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
      {
         int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
         BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
         Graphics2D g = scaledBI.createGraphics();
         if (preserveAlpha) 
         {
            g.setComposite(AlphaComposite.Src);
         }
         g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
         g.dispose();
         return scaledBI;
      }
   }
}
