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
* JpgEncoder class
*
* Purpose:
*
* This class encodes a JPEG cel object.  This class is used to translate an
* image to a JPG file format.
* 
*/

import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;


public class JpgEncoder extends ImageEncoder
{
	private int bytes = 0 ;                   // bytes written
   private int inbytes = 0 ;                 // bytes processed
   private int lastbytes = 0 ;               // last number of bytes processed
   private int width = 0 ;
   private int height = 0 ;
   private int[][] rgbPixels = null ;
   private int transparency ;


   // Constructor from Image.

	public JpgEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
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
			throw new IOException("JpgEncoder: insufficient memory") ;
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

         // Set the encoding quality.
         
         saveAsJPEG(image,OptionsDialog.getJPEGQuality(),out) ;

         bytes = (int) ((width * height) * 0.85) ;
         if (fw != null) fw.updateProgress(bytes-lastbytes) ;
      }

      catch (Exception e)
		{
			System.out.println("JpgEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal exception during JPG encoding") ;
		}

      // Clean up on exit.

      finally
      {
         producer = null ;
      	palette = null ;
         rgbPixels = null ;
         multipalette = 0 ;
         offset = null ;
         transparentcolor = null ;
         backgroundcolor = null ;
      }
   }


   // Method to create a JPEG buffered image from an image.  JPEG does
   // not support transparency.  We must convert to a 3 channel RGB image.

	private BufferedImage makeBufferedImage(int [][] pixels)
	{
		if (pixels == null) return null ;
      int [] pixdata = new int[width*height] ;
      for (int x = width; x-- > 0; )
         for (int y = height; y-- > 0; )
             pixdata[y * width + x] = pixels[y][x] & 0xffffff ;
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB) ;
      bi.setRGB(0,0,width,height,pixdata,0,width) ;
   	return bi ;
	}
        
    public static void saveAsJPEG(BufferedImage image_to_save, float JPEGcompression, OutputStream fos) throws IOException {
 
    //useful documentation at http://docs.oracle.com/javase/7/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html
    //useful example program at http://johnbokma.com/java/obtaining-image-metadata.html to output JPEG data
 
    //old jpeg class
    //com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder = com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(fos);
    //com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);
 
    // Image writer
    JPEGImageWriter imageWriter = (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpeg").next();
    ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
    imageWriter.setOutput(ios);
 
    //and metadata
    IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image_to_save), null);
 
    if(JPEGcompression>=0 && JPEGcompression<=1f){
 
        //old compression
        //jpegEncodeParam.setQuality(JPEGcompression,false);
 
        // new Compression
        JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
        jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(JPEGcompression);
 
    }
 
    //old write and clean
    //jpegEncoder.encode(image_to_save, jpegEncodeParam);
 
    //new Write and clean up
    imageWriter.write(imageMetaData, new IIOImage(image_to_save, null, null), null);
    ios.close();
    imageWriter.dispose();
 
}      
  
}
