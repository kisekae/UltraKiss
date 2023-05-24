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
* BmpEncoder - write out an image as a BMP file.
*
* This function performs RLE_8 encoding.
*
*/


import java.util.* ;
import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;


public class BmpEncoder extends ImageEncoder
{
	private static final int BI_RGB = 0;
	private static final int BI_RLE8 = 1;
	private static final int BI_RLE4 = 2;

	private int bytes = 0 ;                   // bytes written
   private int lastbytes = 0 ;               // last number of bytes written
   private int width, height ;
   private int[][] rgbPixels ;
	private IntHashtable colorHash ;

	private int bfsize = 0 ; 					  	// file size in bytes
	private int biBitCount = 0 ;				  	// bits per pixel
	private int biCompression = 0 ; 			 	// compression code
   
   private int transparency = 255 ; 


   // Constructor from Image.

	public BmpEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
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
   }


   // Method to initialize the encoder.

   void encodeStart( int width, int height ) throws IOException
	{
		try
		{
			this.width = width ;
			this.height = height ;
			rgbPixels = new int[height][width] ;
		}
		catch (OutOfMemoryError e)
		{
			throw new IOException("BmpEncoder: insufficient memory") ;
		}
	}


   // Method to capture the pixels during encoding.

   void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize )
      throws IOException
   {
      for ( int row = 0; row < h; ++row )
         System.arraycopy(rgbPixels, row*scansize+off, this.rgbPixels[y+row], x, w);
   }


   // Method to terminate the encoding.  This is where all the work is
   // performed.  A palette is constructed by identifying all the colors in
   // the image.  Each pixel is mapped to a color table index.

   void encodeDone() throws IOException
   {
      int index = 0 ;
      int logColors = 0 ;
      colorHash = new IntHashtable() ;

      // We must construct a palette for this image and convert pixel
      // colors into color indexes.  If there are too many colors for
      // a palette then we use 24 bit encoding.

		try
      {
         int colors = 0 ;
      	byte [] red = null ;
      	byte [] green = null ;
      	byte [] blue = null ;
         transparentIndex = -1 ;

         // If we have a palette, use the palette and specified multipalette.

	      if (palette != null)
	      {
            colors = palette.getColorCount() ;
            Integer n = new Integer(multipalette) ;
            Object [] data = palette.getMultiPaletteData(n) ;
	      	red = (byte []) data[1] ;
	      	green = (byte []) data[2] ;
	      	blue = (byte []) data[3] ;
         }

         // Build the color hash table for this known palette.  All palette
			// colors are set to fully opaque except for the transparent color.

			for (int i = 0 ; i < colors ; i++)
         {
         	int r = red[i] & 0xFF ;
            int g = green[i] & 0xFF ;
            int b = blue[i] & 0xFF ;
            int a = 0xFF ;
         	int rgb = (a << 24) | (r << 16) | (g << 8) | b ;
           	EncoderHashitem item = new EncoderHashitem(rgb,0,index,false) ;
            colorHash.put(rgb,item);
            index++ ;
	      }

			// Scan the image to translate the image pixel colors into indexes
			// into the color palette.  Image colors that do not exist in the
			// palette are added to the palette.  All BMP palette colors are
         // opaque.

			for (int row = 0 ; row < height ; ++row)
			{
				for (int col = 0 ; col < width ; ++col)
				{
					int rgb = rgbPixels[row][col] ;

	            // If our pixel is transparent replace it with the
	            // background color.
               
	            boolean isTransparent = ((rgb >>> 24) == 0);
	            if (isTransparent)
	            {
	               if (transparentIndex < 0)
	               {
	                  // First transparent color; remember it.
	                  transparentIndex = index;
	                  transparentcolor = new Color(rgb);
                  }
               }
	            if ((rgb & 0xff000000) == 0)
	               if (backgroundcolor != null ) rgb = backgroundcolor.getRGB() ;

					// We have a pixel.  Find the color in our hash table.  If it
					// does not exist, create a new color entry in the hash table.
					// If it does exist, count the number of times this color is used.
					// Note that pixels with color (0,0,1) are actually color (0,0,0).

					rgb |= 0xFF000000 ;
					if ((rgb & 0xffffff) == 1) rgb-- ;
					EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb);
					if (item == null)
	            {
	               if (index >= 256)
                  {
                  	col = width ;
                     row = height ;
                     index = 1 << 24 ;
                     break ;
                  }
	               item = new EncoderHashitem(rgb,1,index,false) ;
	               colorHash.put(rgb,item);
	               ++index;
	            }
	            else
	               ++item.count;
	         }
	      }

	      // Figure out how many bits to use to represent the colors.
         // If logColors is 0 then we are truecolor.

	      if (index <= 2)
				logColors = 1 ;
			else if (index <= 16)
	         logColors = 4 ;
	      else if (index <= 256)
	         logColors = 8 ;

	      // Turn the colors into color vectors.  The color vectors represent
			// the global palette that is written to the encoded file.

	      int mapSize = 1 << logColors;
	      byte[] reds = new byte[mapSize];
	      byte[] grns = new byte[mapSize];
	      byte[] blus = new byte[mapSize];
			for (Enumeration e = colorHash.elements() ; e.hasMoreElements() ; )
	      {
				EncoderHashitem item = (EncoderHashitem) e.nextElement() ;
				if (item.index >= mapSize) continue ;
	         reds[item.index] = (byte) ((item.rgb >> 16) & 0xff) ;
	         grns[item.index] = (byte) ((item.rgb >> 8) & 0xff) ;
	         blus[item.index] = (byte) (item.rgb & 0xff) ;
	      }

	      // Encode the BMP file.

			BMPEncode(out, width, height, logColors, reds, grns, blus);
         if (fw != null) fw.updateProgress(bytes-lastbytes) ;
      }

      // Watch for encoding errors.

		catch (OutOfMemoryError e)
		{
			throw new IOException("BmpEncoder: insufficient memory") ;
		}
		catch (Exception e)
      {
			System.out.println("BmpEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal exception during BMP encoding") ;
		}

      // Clean up on exit.

      finally
      {
      	rgbPixels = null ;
         colorHash = null ;
         producer = null ;
      	palette = null ;
         multipalette = 0 ;
         offset = null ;
      }
   }


   // A utility function to return the color index of an image pixel.
   // Note that pixels with color (0,0,1) are actually color (0,0,0).
   // All BMP colors are opaque.

   byte getPixel(int x, int y) throws IOException
   {
		int rgb = rgbPixels[y][x] ;

      // If our pixel is transparent replace it with the
      // background color.

      if ((rgb & 0xff000000) == 0)
         if (backgroundcolor != null ) rgb = backgroundcolor.getRGB() ;

		// SUN Java does not treat color 0,0,0 as truly transparent so the
		// image color model was adjusted and color 0,0,0 was changed to 0,0,1.

		rgb |= 0xFF000000 ;
		if ((rgb & 0xffffff) == 1) rgb-- ;
		EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb);
		if (item == null)
      {
			throw new IOException("BMP color not found, RGB ("
         	+ ((rgb & 0xFF0000) >> 16) + "," + ((rgb & 0xFF00) >> 8)
            + "," + (rgb & 0xFF) + ")");
      }
		return (byte) item.index;
   }


   // Function to return the number of bytes written.

	int getBytesWritten() { return bytes ; }


	

   // ---------------------------------------------------------------------
   // The BMP encoding routine.   This writes the bitmap file header,
   // the bitmap info header, the color table, and the image data.

	void BMPEncode(OutputStream out, int Width, int Height, int BitsPerPixel,
   		byte[] Red, byte[] Green, byte[] Blue)
      throws IOException
   {
		int width, height ;							// image width and height
		int bfSize = 0 ; 								// size of file in bytes
		int bfOffBits = 0 ;							// byte offset to bitmap data
		int biSize = 40  ;					 		// bytes in BITMAPINFO structure
		int biPlanes = 1 ;							// number of planes for target (1)
		int biBitCount = 0 ;							// number of bits per pixel
		int biCompression = 0 ;						// compression code
		int biSizeImage = 0 ;						// size of image in bytes
		int biXPelsPerMeter = 0 ;					// horizontal resolution in pixels
		int biYPelsPerMeter = 0 ; 					// vertical resolution in pixels
		int biClrUsed = 0 ;							// number of color indexs used
		int biClrImportant = 0 ;					// number of important color index

      // Calculate number of bits we are expecting.

		width = Width ;
		height = Height ;
		biBitCount = (BitsPerPixel == 0) ? 24 : BitsPerPixel ;
		biCompression = (BitsPerPixel == 0) ? BI_RGB : BI_RLE8 ;
		biClrUsed = (BitsPerPixel == 0) ? 0 : 1 << BitsPerPixel ;
		biClrImportant = (BitsPerPixel == 0) ? 0 : Red.length ;

		// Compress the image data.  We compress the data so that we can
		// calculate the final output file size.

		ByteArrayOutputStream b = new ByteArrayOutputStream() ;
		compress(b,biCompression) ;
		bfOffBits = 14 + biSize + (biClrUsed * 4) ;
		bfSize = bytes + bfOffBits ;
		biSizeImage = bytes ;
		bytes = 0 ;
		b.close() ;

		// Write the BITMAPFILEHEADER structure.

		putString(out, "BM") ;                 // magic string
		putDword(out,bfSize) ;  					// size of file in bytes
		putInt(out,0) ;   							// reserved (0)
		putInt(out,0) ;                        // reserved (0)
		putDword(out,bfOffBits) ;              // byte offset to bitmap data

		// Write the BITMAPINFO structure.

		putDword(out,biSize) ; 						// bytes in BITMAPINFO structure
		putLong(out,width) ;   						// width
		putLong(out,height) ; 	            	// height
		putWord(out,biPlanes) ;						// number of planes for target (1)
		putWord(out,biBitCount) ;					// number of bits per pixel
		putDword(out,biCompression) ;				// compression code
		putDword(out,biSizeImage) ;				// size of image in bytes
		putLong(out,biXPelsPerMeter) ;			// horizontal resolution in pixels
		putLong(out,biYPelsPerMeter) ; 			// vertical resolution in pixels
		putDword(out,biClrUsed) ;					// number of color indexs used
		putDword(out,biClrImportant) ;			// number of important color index

		// Write the color table.

		if (biClrUsed > 0)
		{
			for (int i = 0 ; i < Red.length ; i++)
			{
				putByte(out,Blue[i]) ;
				putByte(out,Green[i]) ;
				putByte(out,Red[i]) ;
				putByte(out,(byte) 0) ;
			}
			for (int i = Red.length ; i < biClrUsed ; i++)
			{
				putByte(out,(byte) 0) ;
				putByte(out,(byte) 0) ;
				putByte(out,(byte) 0) ;
				putByte(out,(byte) 0) ;
         }
		}

		// Write the image data.

		byte [] data = b.toByteArray() ;
		for (int i = 0 ; i < data.length ; i++) 
      {
          putByte(out,data[i]) ;
          int progress = bytes - lastbytes ;
          if (fw != null && progress > 1024) 
          {
             fw.updateProgress(bytes-lastbytes) ;
             lastbytes = bytes ;
         }
      }
	}


   // Write out a byte.

   void putByte(OutputStream out, byte b) throws IOException
   { out.write(b) ; bytes++ ; }


   // Write out a string.

   void putString(OutputStream out, String str) throws IOException
   {
      byte[] buf = str.getBytes();
      out.write(buf) ;
      bytes += buf.length ;
   }


	// Write out an integer.

	void putInt(OutputStream out, int w) throws IOException
	{
		putByte(out, (byte) (w & 0xff));
		putByte(out, (byte) ((w >> 8) & 0xff));
	}


	// Write out a word.

	void putWord(OutputStream out, int w) throws IOException
	{ putInt(out,w); }


	// Utility routine to write an unsigned 16 bit integer.

	void putUint(OutputStream out, int w) throws IOException
	{ putInt(out, (w & 0xFFFF)) ; }


	// Utility routine to write a signed 32 bit integer.

	void putLong(OutputStream out, long w) throws IOException
	{
		int i1 = (int) (w & 0xFFFF) ;
		int i2 = (int) (w >> 16) ;
      putInt(out,i1) ;
      putInt(out,i2) ;
	}


	// Utility routine to write an unsigned 32 bit word.

	void putDword(OutputStream out, long w) throws IOException
	{ putLong(out,w) ; }



	// -----------------------------------------------------------------
	// This function performs RLE_8 compression for a scan line.

	void compress(OutputStream out, int compress) throws IOException
	{
		// Scan the image and encode each row.   Note that all pixels are
		// opaque and those with color (0,0,1) are actually color (0,0,0).

		for (int row = height-1 ; row >= 0 ; row--)
		{
			if (compress == BI_RGB)
			{
				writeUncompressed(out,row,width) ;
				padScanLine(out,width) ;
			}
			else if (compress == BI_RLE8)
			{
				writeCompressed(out,row,width) ;
				putByte(out,(byte) 0) ;
            if (row > 0)
					putByte(out,(byte) 0) ;
            else
					putByte(out,(byte) 1) ;
			}
		}
	}


	// Write uncompressed BGR pixels.

	void writeUncompressed(OutputStream out, int row, int width) throws IOException
	{
		for (int col = 0 ; col < width ; col++)
		{
			int rgb = rgbPixels[row][col] ;

	      // If our pixel is transparent replace it with the
	      // background color.

	      if ((rgb & 0xff000000) == 0)
	         if (backgroundcolor != null ) rgb = backgroundcolor.getRGB() ;

         // Pixels with value (0,0,1) are really (0,0,0)
         
			rgb |= 0xFF000000 ;
			if ((rgb & 0xffffff) == 1) rgb-- ;

			// If uncompressed write the pixel colors.

			byte b = (byte) (rgb & 0xff) ;
			byte g = (byte) ((rgb >> 8) & 0xff) ;
			byte r = (byte) ((rgb >> 16) & 0xff) ;
			putByte(out,b) ;
			putByte(out,g) ;
			putByte(out,r) ;
		}
	}

	// Pad the output line to 32 bits.

	void padScanLine(OutputStream out, int width) throws IOException
	{
     	int scanlinepad = (width * 24) % 32 ;
      if (scanlinepad > 0) scanlinepad = 32 - scanlinepad ;
		int padbytes = scanlinepad / 8 ;
		for (int i = 0 ; i < padbytes ; i++) putByte(out,(byte) 0) ;
	}


	// Write compressed BGR pixels.

	void writeCompressed(OutputStream out, int row, int width) throws IOException
	{
		int startscan = 0 ;

		while (startscan < width)
		{
			int n = consecutive(row,startscan,width) ;
			if (n > 0)
			{
				putByte(out,(byte) (n+1)) ;
				putByte(out, getPixel(startscan,row)) ;
				startscan += (n + 1) ;
			}

			// No consecutive bytes.  Encode absolute.  Each encoding
			// must end on a 16 bit boundary.

			else
			{
				n = absolute(row,startscan,width) ;
				switch (n)
				{
					case 0:
						startscan = width ;
						break ;

					case 1:
						putByte(out,(byte) n) ;
						putByte(out, getPixel(startscan,row)) ;
						startscan += n ;
						break ;

					case 2:
						putByte(out,(byte) 1) ;
						putByte(out, getPixel(startscan,row)) ;
						putByte(out,(byte) 1) ;
						putByte(out, getPixel(startscan+1,row)) ;
						startscan += n ;
						break ;
						
					default:
						putByte(out,(byte) 0) ;
						putByte(out,(byte) n) ;
						for (int i = 0 ; i < n ; i++)
							putByte(out, getPixel(startscan+i,row)) ;
						if (n % 2 != 0) putByte(out,(byte) 0) ;
						startscan += n ;
						break ;
				}
			}
		}
	}


	// A function to count the number of consecutive pixels in a row.
	// A return value of 0 implies there are no consecutive pixels.
	// A return value N greater than zero implies N+1 consecutive pixels.

	private int consecutive(int row, int col, int width)
	{
		if (col >= width) return 0 ;
		int startvalue = rgbPixels[row][col] ;
		int count = 0 ;
		col++ ;
		while (col < width && rgbPixels[row][col] == startvalue)
		{
			count++ ;
			col++ ;
         if (count == 253) break ;
		}
		return count ;
	}


	// A function to count the number of non-consecutive pixels in a row.
	// A return value of 0 implies we are at the end of the row.
	// A return value N greater than zero implies N non-consecutive pixels.

	private int absolute(int row, int col, int width)
	{
		if (col >= width) return 0 ;
		int startvalue = rgbPixels[row][col] ;
		int count = 1 ;
		col++ ;
		while (col < width && rgbPixels[row][col] != startvalue)
		{
			startvalue = rgbPixels[row][col] ;
			count++ ;
			col++ ;
         if (count == 254) break ;
		}
      if (col < width) return count - 1 ;
		return count ;
	}



	// An inner class to define the mapping between a pixel RGB color and a
   // palette index.

	class EncoderHashitem
	{
		public int rgb;
		public int count;
		public int index;
		public boolean isTransparent;

		public EncoderHashitem( int rgb, int count, int index, boolean isTransparent )
		{
			this.rgb = rgb;
			this.count = count;
			this.index = index;
			this.isTransparent = isTransparent;
		}
   }
}