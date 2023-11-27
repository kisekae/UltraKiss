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
* PngEncoder class
*
* Purpose:
*
* This class encodes a PNG cel object.  This class is used to translate an
* image to a PNG file format.
*
*/

import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.util.Iterator ;
import javax.imageio.* ;
import javax.imageio.stream.* ;


public class PngEncoder extends ImageEncoder
{
	private int bytes = 0 ;                   // bytes written
   private int lastbytes = 0 ;               // last number of bytes written
   private int width = 0 ;
   private int height = 0 ;
   private int[][] rgbPixels = null ;
   private int transparency = 255 ;


   // Constructor from Image.

	public PngEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
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


   // Function to return the number of bytes written.

   int getBytesWritten() { return bytes ; }


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
			throw new IOException("PngEncoder: insufficient memory") ;
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


   // Method to invoke the encoder.  We assume a 66% compression factor
   // to estimate the output file size.

   public void encodeDone() throws IOException
   {
    	try
      {
         BufferedImage image = makeBufferedImage(rgbPixels) ;

//       IIOImage iioimage = new IIOImage(image,null,null) ;
//       Iterator writers = ImageIO.getImageWritersByFormatName("png");
//       ImageWriter writer = (ImageWriter) writers.next();
//       ImageOutputStream ios = ImageIO.createImageOutputStream(out);
//       writer.setOutput(ios);
//       ImageWriteParam wp = writer.getDefaultWriteParam() ;
//       wp.setTiling(width,height,offset.x,offset.y) ;
//       writer.prepareWriteSequence(null);
//       writer.writeToSequence(iioimage,wp) ;
//       writer.endWriteSequence() ;

         ImageIO.write(image,"png",out) ;
         bytes = (int) ((width * height) * 0.66) ;
         if (fw != null) fw.updateProgress(bytes-lastbytes) ;
      }
		catch (OutOfMemoryError e)
		{
			throw new IOException("PngEncoder: insufficient memory") ;
		}
		catch (Exception e)
		{
			System.out.println("PngEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal exception during PNG encoding") ;
		}

      // Clean up on exit.

      finally
      {
         producer = null ;
      	palette = null ;
         rgbPixels = null ;
         multipalette = 0 ;
         offset = null ;
      }
   }


   // Method to create a PNG buffered image from an image.  PNG supports
   // transparency.

	private BufferedImage makeBufferedImage(int [][] pixels)
	{
      int alpha = -1 ;
      int size = width * height ;
		if (pixels == null) return null ;
      int [] pixdata = new int[size] ;
      for (int x = width; x-- > 0; )
         for (int y = height; y-- > 0; )
         {
             pixdata[y * width + x] = pixels[y][x] ;
             int a = (pixels[y][x] >> 24) ;
             if (alpha == -1) alpha = a ;
             else if (alpha != a) alpha = -2 ;
         }

      // Truecolor images return ARGB buffered images.  If our source image is 
      // a palette type PNG then we may have variable pixel transparency.  If 
      // this is the case alpha will be -2 and we will write this as truecolor, 
      // otherwise alpha will be the actual image constant value.  We use the 
      // image alpha if we are encoding with an opaque transparency, otherwise 
      // we use the specified transparency.

      if (alpha > 0 && transparency == 255) transparency = alpha ; 
      ColorModel cm = (palette != null) ? palette.createColorModel(transparency,0) : null ;
      if (!(cm instanceof IndexColorModel) || alpha < 0)
      {
         BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB) ;
         bi.setRGB(0,0,width,height,pixdata,0,width) ;
         return bi ;
      }

      // Palette images return INDEXED buffered images.

      byte [] ppixels = new byte [width*height] ;
      IndexColorModel icm = (IndexColorModel) cm ;
      for (int i = 0 ; i < pixdata.length ; i++)
      {
         int rgb = pixdata[i] ;
         rgb = (transparency << 24) | (rgb & 0xffffff) ;
         Object pixel = icm.getDataElements(rgb,null) ;
         ppixels[i] = ((byte[]) pixel)[0] ;
      }

      // Create an indexed image from the palette pixels.

      DataBufferByte db = new DataBufferByte(ppixels,ppixels.length) ;
      WritableRaster wr = Raster.createPackedRaster(db,width,height,8,null) ;
      BufferedImage bi = new BufferedImage(icm,wr,false,null) ;
      return bi ;
	}
}
