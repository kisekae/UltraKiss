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
* KissEncoder class
*
* Purpose:
*
* This class encodes a KiSS cel object.  This class is used to translate an
* image to a CEL file format.  The translation is performed by taking image
* pixel RGB values and associating the pixel color with an index in the image
* palette.  The translated file may not equal the original file if the same
* color is duplicated in the palette and referenced through different pixel
* values in the image.
* 
*/


import java.util.* ;
import java.io.* ;
import java.awt.Point ;
import java.awt.Color ;
import java.awt.Image ;
import java.awt.image.* ;


public class KissEncoder extends ImageEncoder
{
   static final int EOF = -1;
   private int[][] rgbPixels = null ;
   private IntHashtable colorHash = null ;
   private int width, height ;
   private int curx, cury ;
   private int Xoffset, Yoffset ;
   private int CountDown ;
   private int bytes = 0 ;                      // bytes written
   private int inbytes = 0 ;                    // bytes processed
   private int lastbytes = 0 ;                  // last bytes processed
   private int transparency = 255 ;


   // Constructor from Image.

	public KissEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
         Color transparentcolor, Color backgroundcolor, int transparency, OutputStream out)
   	throws IOException
   {
   	super(fw, img, out) ;
      this.palette = palette ;
      this.multipalette = multipalette ;
      this.offset = offset ;
      this.transparentcolor = transparentcolor ;
      this.backgroundcolor = backgroundcolor ;
      this.transparency = transparency ;
      Xoffset = offset.x ;
      Yoffset = offset.y ;
   }


	// Required ImageEncoder method to start encoding.  This routine constructs
   // a memory array that represents all pixels of the image.

   void encodeStart(int width, int height) throws IOException
	{
		try
		{
			this.width = width ;
			this.height = height ;
			rgbPixels = new int[height][width] ;
		}
		catch (OutOfMemoryError e)
		{
			throw new IOException("KissEncoder: insufficient memory") ;
		}
   }


   // Required ImageEncoder method to encode a set of pixels.  This routine
   // loads the image pixels into the memory array.

   void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize )
      throws IOException
   {
      for ( int row = 0; row < h; ++row )
			System.arraycopy(rgbPixels, row*scansize+off, this.rgbPixels[y+row], x, w);
	}


	// Required ImageEncoder method to terminate encoding.  This is where the
   // work is performed.  The pixels, which are RGB encoded, are mapped to
   // the image palette to determine the correct index for the RGB color.

   void encodeDone() throws IOException
	{
		colorHash = new IntHashtable() ;
      boolean transparent = true ;
		int logColors = 0 ;
		int index = 0 ;

		// Put all the palette colors into a hash table.  Colors are indexed
		// by their RGB values.  Transparency does not apply.

		try
		{
			int colors = 0 ;
			byte [] red = null ;
			byte [] green = null ;
			byte [] blue = null ;

			// If we have a palette we will use the palette.  If not, we generate
         // the palette from the image and will not use palette index 0, as
         // this is reserved for KCF transparency.

			if (palette != null)
			{
				colors = palette.getColorCount() ;
				Object [] data = palette.getMultiPaletteData(new Integer(multipalette)) ;
				red = (byte []) data[1] ;
				green = (byte []) data[2] ;
				blue = (byte []) data[3] ;

            // Ensure that palette transparent color is at index 0.  Cel
            // encodings sets all transparent pixels to index 0.  If the
            // palette is written as a KCF the encoding will perform a similar
            // translation.

            if (palette.getTransparentIndex() > 0)
            {
               int trans = palette.getTransparentIndex() ;
               byte temp = red[0] ; red[0] = red[trans] ; red[trans] = temp ;
               temp = green[0] ; green[0] = green[trans] ; green[trans] = temp ;
               temp = blue[0] ; blue[0] = blue[trans] ; blue[trans] = temp ;
            }

            // Dither a non-transparent 256 color palette to 255 colors to
            // ensure we have room for the unused transparent color.  This
            // is one of the design flaws of KiSS palettes, in that color 0
            // must always be transparent.

            if (palette.getTransparentIndex() < 0 && colors == 256)
            {
               int pixels[][] = new int[width][height] ;
               for (int x = width; x-- > 0; )
                  for (int y = height; y-- > 0; )
                      pixels[x][y] = rgbPixels[y][x] ;
               int reduced[] = Quantize.quantizeImage(pixels,255) ;
               for (int x = width; x-- > 0; )
                  for (int y = height; y-- > 0; )
                      rgbPixels[y][x] = reduced[pixels[x][y]] ;

               // Create new palette colors after dithering.

               colors = reduced.length ;
               for (int i = 0 ; i < colors ; i++)
               {
                  if (i >= red.length) break ;
                  int rgb = reduced[i] ;
                  red[i] = (byte) ((rgb & 0xff0000) >> 16) ;
                  green[i] = (byte) ((rgb & 0xff00) >> 8) ;
                  blue[i] = (byte) ((rgb & 0xff) >> 0) ;
               }
            }

            // For non-transparent images with less than 256 colors
            // ensure that color index 0 is vacant.

            if (palette.getTransparentIndex() < 0 && colors <= 256)
            {
               colors++ ;
               byte [] temp = new byte[colors] ;
               for (int i = colors-1 ; i > 0 ; i--) temp[i] = red[i-1] ;
               red = temp ; red[0] = 0 ;
               temp = new byte[colors] ;
               for (int i = colors-1 ; i > 0 ; i--) temp[i] = green[i-1] ;
               green = temp ; green[0] = 0 ;
               temp = new byte[colors] ;
               for (int i = colors-1 ; i > 0 ; i--) temp[i] = blue[i-1] ;
               blue = temp ; blue[0] = 0 ;
               transparent = false ;
            }
			}

			// Build the color hash table for this known palette.  All palette
			// colors are set to fully opaque.  If duplicate colors exist in
         // the palette the smallest index is retained.

			for (int i = 0 ; i < colors ; i++)
			{
				int r = red[i] & 0xff ;
				int g = green[i] & 0xff ;
				int b = blue[i] & 0xff ;
				int rgb = (r << 16) | (g << 8) | b ;
				rgb |= 0xff000000 ;
				EncoderHashitem item = new EncoderHashitem(rgb,0,index,false) ;
				if (index > 0 || transparent) colorHash.put(rgb,item);
				index++ ;
			}

         // Reserve palette index 0 for KCF transparency.

         if (index == 0) index++ ;

	      // Now, scan the image to translate the image pixel colors into
			// color palette indexes.  Image colors that do not exist in the
			// palette are added to the palette.

 			for ( int row = 0; row < height; ++row )
  			{
  	         for ( int col = 0; col < width; ++col )
  				{
  					int rgb = rgbPixels[row][col];
  					int a = (rgb & 0xff000000) >> 24 ;
  					int r = (rgb & 0xff0000) >> 16 ;
  					int g = (rgb & 0xff00) >> 8 ;
  					int b = (rgb & 0xff) ;

  					// SUN Java does not treat color 0,0,0 as truly transparent so the
  					// image color model was adjusted and color 0,0,0 was changed to 0,0,1.
  			      // If the color is 0,0,1 and we failed to find it then we should
  					// look for color 0,0,0.

  					if ((rgb & 0xffffff) == 1) rgb-- ;

               // Not sure why this is necessary.  For some reason
               // when a GIF is saved as a CEL the image a transparent color
               // of rgb (0,0,1) exists  in the image and this not the palette
               // transparent rgb.

               if (a == 0 && transparentcolor != null)
                	rgb = transparentcolor.getRGB() ;
  					rgb |= 0xff000000 ;

  	            // We have a pixel.  Find the color in our hash table.  If it
  					// does not exist, create a new color entry in the hash table.
  					// If it does exist, count the number of times this color is used.
  	            // Note that pixels with color (0,0,1) are actually color (0,0,0).

  					EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb);
               if (item == null && (rgb & 0xffffff) == 0)
                  item = (EncoderHashitem) colorHash.get(rgb+1);
  					if (item == null)
  					{
  						// If we have a palette this pixel should exist.  If not, 
                  // we recover by writing a true color cel.

           			if (palette != null)
  	               {
                     String s = "CEL pixel at [" + row + "," + col +
  								"] image color (" + r + "," + g + "," + b + ") not in palette" ;
                     System.out.println("KissEncoder: " + s) ;
                     palette = null ;
  						}

  						// Remember this color.  If it is transparent it must be at
                  // index 0.

  						item = new EncoderHashitem(rgb,1,((a == 0) ? 0 : index),false) ;
  						if (index < 256) colorHash.put(rgb,item) ;
  						++index;
  					}
  					else
  	               ++item.count;
  				}
  			}

			// Figure out how many bits to use to represent a pixel.

         colors = index ;
			if (colors <= 16)
				logColors = 4 ;
			else if (colors <= 256)
				logColors = 8 ;
			else logColors = 32 ;
			if (palette == null)
            logColors = 32 ;
         else if (palette.isInternal())
            palette.setUsedColors(colors) ;

	      // Do the encoding.  This function retrieves pixels from the image
	      // and writes the associated pixel color index to the output stream.

	      celEncode(out, width, height, logColors);
         if (fw != null) fw.updateProgress(inbytes-lastbytes) ;

         // Retain any palette transformations in the Palette.

         if (palette != null) palette.setEncodeArrays(red,green,blue) ;
         if (transparent) transparentIndex = 0 ;
		}

		// Watch for encoding errors.

		catch (OutOfMemoryError e)
		{
			throw new IOException("KissEncoder: insufficient memory") ;
		}
		catch (Exception e)
		{
			System.out.println("KissEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal fault during CEL encoding") ;
		}

		// Clean up on exit.

		finally
		{
			rgbPixels = null ;
			colorHash = null ;
			palette = null ;
			producer = null ;
			multipalette = 0 ;
         offset = null ;
      }
	}


	// The method to perform the CEL file encoding.  This method writes
	// the cel header information followed by the cel data information.

	void celEncode(OutputStream outs, int w, int h, int BitsPerPixel)
		throws IOException
	{
		int i;

		// Write the Magic header
		writeString(outs, "KiSS");

		// Write the CEL file indicator
		Putbyte((byte) 0x20,outs) ;

		// Write bits per pixel (4 or 8)
		Putbyte((byte) (BitsPerPixel & 0xff),outs) ;

		// Reserved word
      Putword(0,outs) ;

      // Width and height
      Putword(w,outs) ;
      Putword(h,outs) ;

      // X and Y offset
      Putword(Xoffset,outs) ;
      Putword(Yoffset,outs) ;

      // Reserved space
      for (i = 0 ; i < 16 ; i++) Putbyte((byte) 0,outs) ;

      // Go and actually write the data
      compress(BitsPerPixel,w,h,outs) ;
   }


   // Function to compress the image pixels and write them to the
   // output stream.

	private void compress(int bits, int w, int h, OutputStream outs)
      throws IOException
	{
		int c = 0 ;
      curx = 0;
		cury = 0;
		int pixel = 0 ;
		CountDown = w * h;

      // Access all pixels

		try
		{
			while (true)
			{
				c = celNextPixel(bits,w,h) ;
				pixel++ ;
				if (bits == 4)
				{
					int c2 = celNextPixel(bits,w,h) ;
					if (c2 == EOF) c2 = 0 ;
					c = c << 4 | (c2 & 0xf) ;
					pixel++ ;
				}
				if (bits <= 8)
					Putbyte( (byte) (c & 0xff ),outs ) ;
				else
				{
					byte a = (byte) ((c >> 24) & 0xff) ;
					byte r = (byte) ((c >> 16) & 0xff) ;
					byte g = (byte) ((c >> 8) & 0xff) ;
					byte b = (byte) ((c) & 0xff) ;
					Putbyte(b,outs) ;
					Putbyte(g,outs) ;
					Putbyte(r,outs) ;
					Putbyte(a,outs) ;
				}
			}
		}
		catch (EOFException e) { }
	}


   // Function to return the next pixel from the image.  If we are
   // encoding 16 color cels and our width is not even, we return
   // a zero pixel at the end of the line.

	private int celNextPixel(int bits, int w, int h) throws IOException
   {
   	int r = 0 ;
		if (CountDown == 0) throw new EOFException() ;
      if (bits <= 8) r = getPixel(curx,cury) ;
      else if (curx < width && cury < height) r = rgbPixels[cury][curx] ;

      // Point to next pixel.

      ++curx;
      if (bits == 4 && curx == w && ((w & 0x1) == 1)) return r ;
      CountDown-- ;
            
      // Track the progress
            
      int progress = ++inbytes - lastbytes ;
      if (fw != null && progress > 1024) 
      {
          fw.updateProgress(progress) ;
          lastbytes = inbytes ;
      }
      
      if (curx >= w )
      {
         curx = 0;
         ++cury;
      }

      // Return the pixel or word.

      return r ;
   }


   // Function to return the pixel at the (x,y) location of the image.  We
   // use the RGB values as the color index and transparency do not apply.

   private byte getPixel(int x, int y) throws IOException
   {
   	if (x < 0 || x >= width) return 0 ;
   	if (y < 0 || y >= height) return 0 ;
		int rgb = rgbPixels[y][x] ;
		int a = (rgb & 0xff000000) >> 24 ;
		int r = (rgb & 0xff0000) >> 16 ;
		int g = (rgb & 0xff00) >> 8 ;
		int b = (rgb & 0xff) ;

		// SUN Java does not treat color 0,0,0 as truly transparent so the
		// image color model was adjusted and color 0,0,0 was changed to 0,0,1.
      // If the color is 0,0,1 and we failed to find it then we should
		// look for color 0,0,0.

		if ((rgb & 0xffffff) == 1) rgb-- ;

		// Not sure why this is necessary.  For some reason
		// when a GIF is saved as a CEL the image transparent color
		// has rgb (0,0,1) and not the palette transparent rgb.

		if (a == 0 && transparentcolor != null)
			rgb = transparentcolor.getRGB() ;
		rgb |= 0xff000000 ;
		EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb) ;
      if (item == null && (rgb & 0xffffff) == 0)
         item = (EncoderHashitem) colorHash.get(rgb+1);

		if (item == null)
		{
			throw new IOException("color (" + r + "," + g + "," + b + ") not in palette" );
      }

      // Fully transparent pixels in the image must use cel palette index 0.

      if (a == 0) return 0 ;
      return (byte) (item.index & 0xff) ;
   }


   // Function to write a string to the output stream.

   private void writeString(OutputStream out, String str) throws IOException
   {
      byte[] buf = str.getBytes() ;
      out.write(buf) ;
      bytes += buf.length ;
   }


   // Function to write out a word to the CEL file in Little Endian mode.

   private void Putword(int w, OutputStream outs) throws IOException
	{
      Putbyte((byte) (w & 0xff ),outs);
      Putbyte((byte) (( w >> 8 ) & 0xff ),outs);
   }


   // Function to write out a byte to the CEL file.

   void Putbyte(byte b, OutputStream outs) throws IOException
   { outs.write(b) ; bytes++ ; }


   // Function to return the number of bytes written.

   int getBytesWritten() { return bytes ; }
}
