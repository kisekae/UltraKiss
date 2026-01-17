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
* GifFrame class
*
* Purpose:
*
* This class defines one frame of a GIF cel object.  GIF files may contain
* many image frames.  Each frame can have its own palette and display offset
* relative to the logical screen.
*
* Animated GIFs display each frame in sequence.  Animation support is
* implemented by updating the cel image as required to display the correct
* frame.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import javax.swing.* ;

final class GifFrame
   implements Cloneable
{

	// Cel image attributes

   private GifCel cel = null ;				// The parent file cel
	private Image image = null ;				// The frame base image
	private Image filteredimage = null ;	// The frame filtered image
	private Image scaledimage = null ;		// The frame scaled image
	private Dimension size = null ;			// The frame dimensions
	private Dimension scaledsize = null ; 	// The frame scaled dimensions
	private Point offset = null ;				// The frame offset
   private ColorModel basecm = null	;		// The color model for base image
   private ColorModel cm = null	;		   // The active color model
   private Color transparentcolor = null ; // The transparent color
   private int frame = 0 ;						// The image frame number
   private float sf = 1.0f ;					// The image scale factor
	private String version = null ;			// The GIF file version
	private int multipalettes = 1 ;			// The multipalette count
   private int transparency = 255 ;       // The cel transparency

   // Frame state

   private boolean error = false ;   		// If true, frame in error
   private boolean gce = false ;				// If true, then graphic extension
   private boolean scaled = false ;			// If true, frame is scaled

   // GIF file attributes

  	private int gctflag = 0 ;    				// Global color table flag
   private int gctsize = 0 ;					// Global color table size
   private int gctsort = 0 ;					// Global color table sorted
   private int gctbits = 0 ;					// Global color resolution bits
   private int [] bgcolor = null ;			// Background color index
   private int base = 0 ;						// Base address for block
 	private int lctflag = 0 ;    				// Local color table flag
   private int lctsize = 0 ;					// Local color table size
   private int lctsort = 0 ;					// Local color table sorted
   private int interlace = 0 ;				// Image interlaced
   private int screenwidth = 0 ;	  			// Logical screen width
   private int screenheight = 0 ;	  		// Logical screen height
	private int width = 0 ;	  					// Image width
   private int height = 0 ;					// Image height
   private int xoffset = 0 ;					// Image x offset
   private int yoffset = 0 ;					// Image y offset
   private int transflag = 0 ;				// Transparent color flag
   private int disposal = 0 ;					// Disposal method
   private int userinput = 0 ;				// User input flag
   private int delay = 0 ;						// Animation period in 1/100 seconds
   private int [] transparent = null ;		// Transparent color index
   private int lzwcodesize = 0 ;				// LZW minimum code size
   private int loop = -1 ;						// Maximum loop count

   // GIF palette definitions

   private byte [] globalred = null ;		// Global palette red colors
   private byte [] globalgreen = null ;	// Global palette green colors
   private byte [] globalblue = null ;		// Global palette blue colors
   private byte [] localred = null ;		// Local palette red colors
   private byte [] localgreen = null ;		// Local palette green colors
   private byte [] localblue = null ;		// Local palette blue colors



	// Constructor

	public GifFrame(GifCel c)
	{
		size = new Dimension(0,0) ;
		offset = new Point(0,0) ;
      transparency = c.getTransparency() ;
      cel = c ;
	}



	// Object state reference methods
	// ------------------------------


	// Return a reference to the parent cel.

	GifCel getCel() { return cel ; }

   // Return a reference to the frame image.

 	Image getImage()
   {
      Image img = (scaled) ? scaledimage : ((filteredimage != null) ? filteredimage : image) ;
      return img ;
   }

	// Return a reference to the frame color model.

	ColorModel getColorModel() { return cm ; }

	// Return a reference to the frame base image.

	Image getBaseImage() { return image ; }

	// Return a reference to the frame scaled image.

	Image getScaledImage() { return scaledimage ; }

	// Return our frame number.

	int getFrame() { return frame ; }

	// Return the frame image size.

	Dimension getSize() { return (scaled) ? scaledsize : size ; }

	// Return the logical screen size.

	Dimension getScreenSize() { return new Dimension(screenwidth,screenheight); }

	// Return the frame image offset.

	Point getOffset() { return (offset == null) ? new Point(0,0) : offset ; }

	// Return the original frame image offset.

	Point getBaseOffset() { return new Point(xoffset,yoffset) ; }

	// Return the frame animation delay time in milliseconds.

	int getDelay() { return delay * 10 ; }

	// Return the frame disposal setting.

	int getDisposal() { return disposal ; }

	// Return the animation loop count.

	int getLoopCount() { return loop ; }

	// Return the frame background color index.

	int [] getBackgroundIndex() { return bgcolor ; }

	// Return the frame transparent color index.

	int [] getTransparentIndex()
   {
      if (transflag == 1) return transparent ;
      int [] trans = new int[1] ;
      trans[0] = -1 ;
      return trans ;
   }

	// Return the frame transparent color.

	Color getTransparentColor() { return transparentcolor ; }

	// Return the number of multipalettes in the palette.

	int getMultiPaletteCount() { return multipalettes ; }

	// Return the number of colors in the palette.

	int getColorCount()
   {
   	byte [] colorarray = getRed() ;
      if (colorarray == null) return 0 ;
      return colorarray.length / multipalettes ;
   }

	// Return the GIF file version.

	String getVersion() { return (version == null) ? "unknown" : version ; }

   // Return the default frame transparency.

	byte [] getAlpha()
	{
   	byte [] colorarray = getRed() ;
      if (colorarray == null) return null ;
      int n = colorarray.length ;
      byte [] a = new byte [n] ;
      for (int i = 0 ; i < n ; i++) a[i] = (byte) transparency ;
      if (transflag != 0 && transparent != null && transparent[0] < n && transparent[0] >= 0)
         a[transparent[0]] = 0 ;
      return a ;
   }

   // Return the frame palette red colors.

   byte [] getRed()
	{ return (lctflag != 0) ? localred : globalred ; }

   // Return the frame palette green colors.

   byte [] getGreen()
   { return (lctflag != 0) ? localgreen : globalgreen ; }

   // Return the frame palette blue colors.

	byte [] getBlue()
   { return (lctflag != 0) ? localblue : globalblue ; }

   // Identify if we are using the global or a local palette.

   boolean isLocalColorTable() { return (lctflag != 0) ; }

   // Identify if we are waiting on user input.

   boolean isUserInput() { return (userinput != 0) ; }

   // Identify if the frame is in error.

   boolean isError() { return error ; }

   // Identify if this frame has a graphics control extension.

   boolean hasExtension() { return gce ; }

   // Set the frame error state.

	void setError(boolean b) { error = b ; }

   // Set the frame image.

   void setImage(Image img)
   {
      image = img ;
      filteredimage = null ;
      scaledimage = null ;
      screenwidth = width = (image == null) ? 0 : image.getWidth(null) ;
      screenheight = height = (image == null) ? 0 : image.getHeight(null) ;
		size.width = width ;
		size.height = height ;
   }

   // Set the frame base color model.

	void setBaseColorModel(ColorModel cm) { basecm = cm ; }

	// Set the frame image offset.

	void setOffset(Point p) { offset = p ; } ;
	void setOffset(int x, int y) { offset.x = x ; offset.y = y ; }

	// Set the frame transparency.

	void setTransparency(int n) { transparency = n ; }

	// Set the frame transparent index.

	void setTransparentIndex(int n)
   {
      transflag = (n < 0) ? 0 : 1 ;
      transparent = new int[1] ;
      transparent[0] = n ;
   }

	// Set the frame transparent color.

	void setTransparentColor(Color c) { transparentcolor = c ; }

	// Set the frame background index.  This is defined by the parent GIF cel.

	void setBackgroundIndex(int n)
   {
      bgcolor = new int[1] ;
      bgcolor[0] = n ;
   }

	// Set the frame maximum loop limit.

	void setLoopLimit(int n) { loop = n ; }

	// Set the frame disposal code.

	void setDisposal(int n) { disposal = n ; }

	// Set the user input flag.

	void setUserInput(boolean b) { userinput = (b) ? 1 : 0 ; }

	// Set the frame loop count.

	void setLoopCount(int n) { loop = n ; }

	// Set the frame delay.

	void setDelay(int n) { delay = n / 10 ; }

	// Set a reference to the parent cel.  This is required when the
   // frame is cloned for a cel copy.

	void setCel(GifCel c) { cel = c ; }


	// Set or clear the local palette copy from the global palette.
   // If we already have a local palette and we are converting to a
   // local palette then the current local palette is not replaced.
   // If we are converting to a global palette then the existing local
   // palette is replaced.

	void setLocalPalette(boolean b)
   {
      if (b && lctflag != 0) return ;

      // We can switch state.  Access the global palette.

      Object [] globalpalette = cel.getGlobalPaletteArrays() ;
      globalred = (byte []) globalpalette[0] ;
      globalgreen = (byte []) globalpalette[1] ;
      globalblue = (byte []) globalpalette[2] ;
      gctsize = (globalred != null) ? globalred.length : 0 ;
      gctflag = (b) ? 0 : 1 ;
      lctflag = (b) ? 1 : 0 ;
      lctsize = (b) ? gctsize : 0 ;
      lctsort = (b) ? gctsort : 0 ;

      // Copy the global color table if we are converting to local.

      if (lctflag != 0)
      {
         localred = new byte[lctsize] ;
         localgreen = new byte[lctsize] ;
         localblue = new byte[lctsize] ;
         for (int i = 0 ; i < lctsize ; i++)
         {
            localred[i] = globalred[i] ;
            localgreen[i] = globalgreen[i] ;
            localblue[i] = globalblue[i] ;
         }
      }

      // Establish the global arrays if we are converting to global.

      else
      {
         if (globalred == null)
         {
            globalpalette[0] = globalred = localred ;
            globalpalette[1] = globalgreen = localgreen ;
            globalpalette[2] = globalblue = localblue ;
         }
         gctsize = (globalred != null) ? globalred.length : 0 ;
         localred = null ;
         localgreen = null ;
         localblue = null ;
      }

      // Update the image.

      changeTransparency(0) ;
   }




	// Object loading methods
	// ----------------------

	// Load the GIF frame.  This method decodes the cel pixels from the
   // GIF file buffer for the specified image frame.  The global palette
   // color vectors will be created if they do not exist.

	void loadFrame(byte [] b, int frame, Object [] globalpalette)
	{
		int descriptoroffset = 0 ;
      int dataoffset = 0 ;
      int framecount = 0 ;
      boolean spandata = false ;
      boolean newframe = true ;
      transparent = new int[1] ;
      bgcolor = new int[1] ;
      transparent[0] = -1 ;
      bgcolor[0] = -1 ;

      try
      {
	      // Check the GIF signature block.

			if (b == null || b.length < 4 || !("GIF".equals(new String(b,0,3))))
           	throw new KissException("Invalid GIF file signature") ;
	      version = new String(b,3,3) ;
         this.frame = frame ;

			// Decode the logical screen descriptor.  This block can optionally
         // contain a Global Color Table.

	      gctflag = (b[10] & 255) >> 7 ;
	      gctsize = 1 << ((b[10] & 7) + 1) ;
         gctsort = (b[10] & 0x8) >> 3 ;
         gctbits = ((b[10] & 0x70) >> 4) + 1 ;
	      bgcolor[0] = fixByte((byte) 0,b[11]) ;
         screenwidth = fixByte(b[7],b[6]) ;
         screenheight = fixByte(b[9],b[8]) ;
         base += (13 + 3*((gctflag == 0) ? 0 : gctsize)) ;

			// Decode the global color table.  If the global table exists
			// we use it, otherwise we create it.

         if (globalpalette != null)
         {
	         globalred = (byte []) globalpalette[0] ;
	         globalgreen = (byte []) globalpalette[1] ;
	         globalblue = (byte []) globalpalette[2] ;
         }

			if (gctflag != 0 && globalred == null)
			{
				globalpalette[0] = globalred = new byte[gctsize] ;
				globalpalette[1] = globalgreen = new byte[gctsize] ;
				globalpalette[2] = globalblue = new byte[gctsize] ;
				int i = 13 ;
				int j = 0 ;

				while (j < gctsize)
				{
					globalred[j] = b[i] ;
					globalgreen[j] = b[i+1] ;
					globalblue[j] = b[i+2] ;
					i = i + 3 ;
					j = j + 1 ;
				}
     		}

         // Find the Image Descriptor block.  This block is a graphics
         // rendering block, optionally preceded by one or more control
         // blocks such as the Graphics Control Extension, and may be
         // optionally followed by a Local Color Table.

         while (base < b.length)
         {
         	if (framecount < 0) break ;
	         int block = (b[base] & 255) ;

            // If we are spanning the image data, simply skip the blocks.

            if (spandata)
            {
            	if (block == 0) { spandata = false ; newframe = true ; }
               base += block + 1 ;
               continue ;
            }

            // If we have located the necessary frame, exit.  If we are
				// beginning the search for a new frame, initialize as required.

            if (framecount > frame) break ;
            if (newframe) { gce = false ; newframe = false ; }

            // Now we are currently seeking control blocks.

	         switch (block)
	         {

				// Zero length block.

            case 0x00:
            	base += 1 ;
               break ;

            // File terminator.

            case 0x3B:
            	base += 1 ;
            	framecount = -1 ;
               break ;

            // Image Descriptor.  This block describes the image and can
            // optionally contain a Local Color Table.

				case 0x2C:
               xoffset = fixByte(b[base+2],b[base+1]) ;
               yoffset = fixByte(b[base+4],b[base+3]) ;
               width = fixByte(b[base+6],b[base+5]) ;
               height = fixByte(b[base+8],b[base+7]) ;
					lctflag = (b[base+9] & 255) >> 7 ;
					lctsize = 1 << ((b[base+9] & 7) + 1) ;
					lctsort = (b[base+9] & 0x20) >> 5 ;
               interlace = (b[base+9] & 127) >> 6 ;

					// Decode the color table.  Accept a maximum of 256 colors.

					if (lctflag != 0)
					{
						localred = new byte[lctsize] ;
						localgreen = new byte[lctsize] ;
						localblue = new byte[lctsize] ;
						int i = base + 10 ;
						int j = 0 ;

						while (j < lctsize)
						{
							localred[j] = b[i] ;
							localgreen[j] = b[i+1] ;
							localblue[j] = b[i+2] ;
							i = i + 3 ;
							j = j + 1 ;
						}
         		}

               // Retain the frame descriptor addresses.

               descriptoroffset = base ;
		         base += (10 + 3*((lctflag == 0) ? 0 : lctsize)) ;
               dataoffset = base ;
               framecount++ ;
               spandata = true ;

               // Pick up the LZW minimum code size value.

					lzwcodesize = b[base] ;
               base += 1 ;
               break ;

            // Extension block.  These blocks are control blocks that can
            // precede the Image Descriptor.

            case 0x21:
            	int type = (b[base+1] & 255) ;
               switch (type)
					{

               // Graphic Control Extension.

               case 0xF9:
               	base += 2 ;
                  transflag = (b[base+1] & 0x1) ;
                  disposal = (((b[base+1]) & 0x1C) >> 2) ;
                  userinput = (((b[base+1]) & 0x2) >> 1) ;
                  delay = fixByte(b[base+3],b[base+2]) ;
                  transparent[0] = (b[base+4] & 255) ;
                  base += (b[base] & 255) + 1 ;
                  gce = true ;
                  break ;

               // Comment Extension.

               case 0xFE:
               	base += 2 ;
                  while (b[base] != 0) base += (b[base] & 255) + 1 ;
                  base += 1 ;
                  break ;

					// Plain Text Extension.

               case 0x01:
               	base += 2 ;
                  base += (b[base] & 255) + 1 ;
                  while (b[base] != 0) base += (b[base] & 255) + 1 ;
                  base += 1 ;
                  break ;

               // Application Extension.   Watch for the loop count block.

               case 0xFF:
               	base += 2 ;
                  if ("NETSCAPE2.0".equals(new String(b,base+1,11)))
                  {
	                  base += (b[base] & 255) + 1 ;
                     if (b[base+1] == 1) loop = fixByte(b[base+3],b[base+2]) ;
                  }
                  base += (b[base] & 255) + 1 ;
                  while (b[base] != 0) base += (b[base] & 255) + 1 ;
                  base += 1 ;
                  break ;

               default:
               	throw new KissException("Unknown GIF extension block " + type) ;
               }
   	         break ;

            default:
         		throw new KissException("Unknown GIF block " + block) ;
            }
         }

			// Did we locate the required frame?

         if (framecount <= frame)
         {
         	error = true ;
            return ;
         }

	      // Construct our frame image.  The dataoffset points to the
         // start of the image data in the buffer.  The base address
			// points to the location after the zero sized block terminator.

	      int index = 0 ;
	      int length = 13 + (((gctflag == 0) ? 0 : gctsize)*3)
         	+ 10 + (((lctflag == 0) ? 0 : lctsize)*3)
            + (base-dataoffset) + 1
            + ((gce) ? 8 : 0) ;
	      byte [] gif = new byte[length] ;

	      // Gif header and logical screen descriptor.   Set the image
         // screenwidth and screenheight as the frame width and height.

	      for (int i = 0 ; i < 13 ; i++)
	      	gif[index++] = b[i] ;
         gif[6] = (byte) (width & 0xff) ;
         gif[7] = (byte) ((width >> 8) & 0xff) ;
         gif[8] = (byte) (height & 0xff) ;
         gif[9] = (byte) ((height >> 8) & 0xff) ;

	      // Follow with the global color table.

	      for (int i = 0 ; i < ((gctflag == 0) ? 0 : gctsize) ; i++)
	      {
	      	gif[index++] = globalred[i] ;
	      	gif[index++] = globalgreen[i] ;
	      	gif[index++] = globalblue[i] ;
	      }

			// Insert a graphics control extension if required.

         if (gce)
         {
         	gif[index++] = (byte) 0x21 ;
            gif[index++] = (byte) 0xF9 ;
            gif[index++] = 4 ;
            gif[index++] = (byte) transflag ;
            gif[index++] = 0 ;
            gif[index++] = 0 ;
				gif[index++] = (byte) transparent[0] ;
            gif[index++] = 0 ;
         }

 	      // Next comes the image descriptor.  We clear the image offset values
         // because we maintain these ourselves.  Also, Java image scaling
         // seems to fail when the offsets are non-zero.

      	gif[index++] = b[descriptoroffset+0] ;
	      for (int i = 1 ; i < 5 ; i++)
	      	gif[index++] = 0 ;
	      for (int i = 5 ; i < 10 ; i++)
	      	gif[index++] = b[descriptoroffset+i] ;

	      // Follow with the local color table.

	      for (int i = 0 ; i < ((lctflag == 0) ? 0 : lctsize) ; i++)
	      {
	      	gif[index++] = localred[i] ;
	      	gif[index++] = localgreen[i] ;
	      	gif[index++] = localblue[i] ;
	      }

			// Now for the image data.

	      for (int i = 0 ; i < base-dataoffset ; i++)
	      	gif[index++] = b[dataoffset+i] ;

	      // Lastly, the image terminator.

	      gif[index++] = (byte) 0x3B ;

	      // Convert the frame to an image.

			ImageIcon imageicon = new ImageIcon(gif) ;
			image = imageicon.getImage() ;
			if (imageicon.getImageLoadStatus() != MediaTracker.COMPLETE)
				throw new KissException("GIF image frame load failure") ;
			size.width = width ;
			size.height = height ;
         offset.x = xoffset ;
         offset.y = yoffset ;

         // Convert the image to a buffered image.  This seems to clear up
         // a restart problem where cel image copies, created on a restart,
         // fail to paint the first time they are drawn.  The problem has
         // been observed for JPG and GIF images.
/*         
         if (OptionsDialog.getAppleMac())
         {
            int w = size.width ;
            int h = size.height ;
            BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB_PRE) ;
            Color tc = transparentcolor ;
            if (tc == null) tc = Color.black ;
            int rgb = tc.getRGB() & 0xFFFFFF ;
            int [] rgbarray = new int[w*h] ;
            for (int i = 0 ; i < w*h ; i++) rgbarray[i] = rgb ;
            bi.setRGB(0,0,w,h,rgbarray,0,w) ;
            Graphics2D g2 = bi.createGraphics() ;
            g2.drawImage(image,null,null) ;
            g2.dispose() ;
            image = bi ;
         }
*/
         // We must create a base color model for this image.  This is used
         // to translate colors for new palette filters.

         int cmtrans = transparency ;
         if (cmtrans < 0) cmtrans = 0 ;
         if (cmtrans > 255) cmtrans = 255 ;
			byte rd[] = new byte[256] ;
			byte gn[] = new byte[256] ;
			byte bl[] = new byte[256] ;
			byte al[] = new byte[256] ;
         byte red[] = getRed() ;
         byte green[] = getGreen() ;
         byte blue[] = getBlue() ;
			for (int i = 0 ; i < 256 ; i++)
			{
         	if (i >= red.length) break ;
         	if (i >= green.length) break ;
         	if (i >= blue.length) break ;
				rd[i] = red[i] ;
				gn[i] = green[i] ;
				bl[i] = blue[i] ;
				al[i] = (byte) cmtrans ;
			}

			// Set the transparent color if it exists and create a color model.

			if (transparent != null && transparent[0] < al.length && transparent[0] >= 0)
         {
            al[transparent[0]] = 0 ;
            int r1 = (int) (rd[transparent[0]] & 0xff) ;
            int g1 = (int) (gn[transparent[0]] & 0xff) ;
            int b1 = (int) (bl[transparent[0]] & 0xff) ;
            transparentcolor = new Color(r1,g1,b1) ;
         }
			cm = basecm = new IndexColorModel(8,256,rd,gn,bl,al) ;

         // Apply any initial transparency filters to the image.

         if (transparency != 255)
         {
            PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor);
        		ImageProducer ip = new FilteredImageSource(image.getSource(),pf) ;
        		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
            MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
            tracker.addImage(filteredimage,0) ;
            try { tracker.waitForAll(500) ; }
            catch (InterruptedException e) { }
         }
		}

		// Watch for KiSS exceptions.

		catch (KissException e)
		{
			error = true ;
         String s = e.getMessage() ;
			if (s == null) s = e.toString() ;
			PrintLn.println("GIF Exception, Cel " + cel.getPath()
         	+ " frame " + frame + ", " + s) ;
		}

		// Watch for general exceptions.

		catch (Exception e)
		{
			error = true ;
         String s = e.getMessage() ;
			if (s == null) s = e.toString() ;
			PrintLn.println("Exception, Cel " + cel.getPath()
         	+ " frame " + frame + ", " + s) ;
			e.printStackTrace() ;
		}
	}

   // Unload the gif frame.  This releases our image allocation.

   void unload()
   {
      scaled = false ;
      if (image != null) image.flush();
      if (filteredimage != null) filteredimage.flush();
      if (scaledimage != null) scaledimage.flush();
      image = filteredimage = scaledimage = null ;
      if (OptionsDialog.getDebugLoad())
         PrintLn.println("Unload: " + toString());
      cel = null ;
   }


   // A function to construct a palette object for this frame.  Usually
   // we alter the parent GifCel palette each time the frame is changed,
   // however we may have temporary need for a frame palette object.

   Palette getPalette()
   {
      Palette p = new Palette() ;
      p.setInternal(true) ;
      p.setPalette(getAlpha(),getRed(),getGreen(),getBlue(),getMultiPaletteCount(),
         getColorCount(),getBackgroundIndex(),getTransparentIndex()) ;
      return p ;
   }


   // Method to change the palette for this frame image.  This updates our
   // color vectors and creates a filtered image to apply the new colors to
   // our image.

	void changePalette(Palette p, int transparency, int mp)
	{
		if (p == null) return ;
		if (!p.isUpdated()) return ;

      // Update our palette arrays to match the palette object.

		Object [] o = p.getPaletteData() ;
		if (lctflag != 0)
		{
			localred = (byte []) o[1] ;
			localgreen = (byte []) o[2] ;
			localblue = (byte []) o[3] ;
         lctsize = localred.length ;
		}
		else
		{
			globalred = (byte []) o[1] ;
			globalgreen = (byte []) o[2] ;
			globalblue = (byte []) o[3] ;
         gctsize = globalred.length ;
		}

      // Update our background index to match the palette object.

		multipalettes = p.getMultiPaletteCount() ;
      if (bgcolor == null) bgcolor = new int[1] ;
      bgcolor[0] = p.getBackgroundIndex() ;

		// Change the image.  Make the change only if the requested
		// color model actually exists in the cel palette file.

      filteredimage = null ;
		cm = p.createColorModel(transparency,mp) ;
      Image img = getScaledImage() ;
      if (img == null) img = getBaseImage() ;
      if (img == null) return ;

		// Construct an image filter.

		ImageProducer base = img.getSource() ;
      PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor);
		ImageProducer ip = new FilteredImageSource(base,pf) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;

      // Not sure why we require the media tracker, but cases exist where
      // the image does not display.

		MediaTracker mt = new MediaTracker(Kisekae.getKisekae()) ;
		mt.addImage(filteredimage,frame) ;
		try { mt.waitForAll(500) ; }
		catch (InterruptedException e) { }
   }


	// Method to change the cel frame transparency.  The change is relative
	// to the current transparency.

	void changeTransparency(int t)
	{
		if (error) return ;

		// Identify the image to filter.

		filteredimage = null ;
      Image img = getScaledImage() ;
      if (img == null) img = getBaseImage() ;
      if (img == null) return ;

		// Update the frame transparency value.

		int kisstransparency = 255 - transparency ;
      kisstransparency += t ;
		transparency = 255 - kisstransparency ;
      int cmtrans = transparency ;
      if (cmtrans < 0) cmtrans = 0 ;
      if (cmtrans > 255) cmtrans = 255 ;

		// We create new 256 color models because this seems to be the only
      // size that works consistently across various implementations and
		// different machines.

		byte r[] = new byte[256] ;
		byte g[] = new byte[256] ;
		byte b[] = new byte[256] ;
		byte a[] = new byte[256] ;
      byte red[] = getRed() ;
      byte green[] = getGreen() ;
      byte blue[] = getBlue() ;
		for (int i = 0 ; i < 256 ; i++)
		{
        	if (i >= red.length) break ;
        	if (i >= green.length) break ;
        	if (i >= blue.length) break ;
			r[i] = red[i] ;
			g[i] = green[i] ;
			b[i] = blue[i] ;
			a[i] = (byte) cmtrans ;

			// Sun Java does does not properly treat color (0,0,0) as
			// truly transparent so we adjust the color to (0,0,1).

			if (r[i] == 0 && g[i] == 0 && b[i] == 0) b[i] = 1 ;
		}

		// Set the transparent color if it exists and create a new color model.

		if (transparent != null && transparent[0] >= 0 && transparent[0] < a.length)
         a[transparent[0]] = 0 ;
		cm = new IndexColorModel(8,256,r,g,b,a) ;

		// Construct an image filter.

		ImageProducer base = img.getSource() ;
      PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor);
      ImageProducer ip = new FilteredImageSource(base,pf) ;
      filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;

      // Not sure why the mediatracker is required, but without this the
      // image does not always display.

		MediaTracker mt = new MediaTracker(Kisekae.getKisekae()) ;
		mt.addImage(filteredimage,0) ;
		try { mt.waitForAll(500) ; }
		catch (InterruptedException e) { }
	}


   // Method to scale the frame image.

   void scaleImage(float scale)
   	throws KissException
   {
   	if (image == null) return ;
      scaled = (scale != 1.0f) ;

      // Request to eliminate scaling?

      if (scale == 0.0f || !scaled)
      {
	      scaledimage = null ;
         scaled = false ;
         sf = 1.0f ;
      }

      // If we have a scaled image at this size, use it.

      if (!scaled) return ;
      float delta = scale - sf ;
      if (delta < 0) delta = -delta ;
      if (scaledimage != null && delta < 0.01f) return ;

      // Determine the new scaled image size.

      int w = size.width ;
      int h = size.height ;
		int sw = (int) Math.ceil(w * scale) ;
      int sh = (int) Math.ceil(h * scale) ;

      // Convert to a buffered image.  Watch for errors.

      try
      {
	      BufferedImage bufferedimage = new BufferedImage
	        	(w,h,BufferedImage.TYPE_INT_ARGB) ;
	      Graphics g = bufferedimage.getGraphics() ;
         Image img = (filteredimage != null) ? filteredimage : image ;
	      g.drawImage(img,0,0,null) ;
	      g.dispose() ;

	      // Scale the image.

	     	scaledimage = bufferedimage.getScaledInstance(sw,sh,Image.SCALE_REPLICATE) ;

	      // Establish the new scaled image size.  Note that this is not the
	      // size of the scaled image.  It is the adjusted base size of the
	      // image that results from the scaling and should be equal or close
	      // to the original image size.

	      sf = scale ;
	      sw = (int) (sw / sf) ;
	      sh = (int) (sh / sf) ;
	      scaledsize = new Dimension(sw,sh) ;
		}
      catch (OutOfMemoryError e)
      { throw new KissException("Out of memory, Cel " + cel.getPath()) ; }
   }


	// Method to convert signed bytes to an integer number.

	static int fixByte(byte b1, byte b2)
	{
		int n1 = (b1 << 8) ;
		int n2 = (b2 < 0) ? 256 + b2 : b2 ;
		return n1 + n2 ;
	}


   // Method to return a string representation of ourselves.

   public String toString()
   {
   	if (cel == null) return "" ;
   	return (cel.toString() + " (Frame " + frame + ")") ;
   }

	// Shallow clone.  This creates a new object where required object
   // references are duplicated as found in the original object.

   public Object clone()
   {
	   try
      {
         GifFrame gf = (GifFrame) super.clone() ;
         gf.setOffset(new Point(getOffset())) ;
         return gf ;
      }
      catch (CloneNotSupportedException e)
      {
      	PrintLn.println("GifFrame: Clone failure") ;
      	e.printStackTrace();
         return null ;
      }
   }
}
