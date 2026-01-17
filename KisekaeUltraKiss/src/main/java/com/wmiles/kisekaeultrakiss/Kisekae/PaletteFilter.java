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
* PaletteFilter class
*
* Purpose:
*
* This class is used to create an image filter to apply a new color palette
* to cel images.  Palette images can use this filter to change image colors
* by creating a new IndexColorModel for the image.  The color model defines
* the palette colors and the color transparency.  Non-palette images can use
* this filter to adjust the image transparency.
* 
*/

import java.awt.* ;
import java.awt.image.* ;

final class PaletteFilter extends RGBImageFilter
{
	private ColorModel cm = null ; 					// New palette color model
	private ColorModel base = null ; 	  			// Old palette color model
   private IndexColorModel icm = null ;			// Base indexed color model
	private int transparency = 0 ;     				// Required transparency
   private int transparentcolor = -1 ;   			// Transparent color RGB
	private int components[] = new int[4] ;		// Color model components

   
	// Constructor.  Transparent colors of 0 are adjusted to 1.

	public PaletteFilter(ColorModel cm, ColorModel base, int transparency, Color c)
	{
		this.cm = cm ;
		this.base = base ;
		this.transparency = transparency ;
		this.transparentcolor = (c == null) ? -1 : (c.getRGB() & 0xffffff) ;
      if (this.transparency < 0) this.transparency = 0 ;
      if (this.transparency > 255) this.transparency = 255 ;

		// Sun Java does does not properly treat color (0,0,0) as
		// truly transparent so we adjust the color to (0,0,1).

		if (transparentcolor == 0) transparentcolor = 1 ;
      canFilterIndexColorModel = (cm instanceof IndexColorModel) ;
      if (base instanceof IndexColorModel) icm = (IndexColorModel) base ;
	}

	// Take a new color model and use this as our filter.  This establishes
   // a new palette for the image.  This method is invoked by the parent
   // class if canFilterIndexColorModel is true.

	public IndexColorModel filterIndexColorModel(IndexColorModel icm)
	{
      IndexColorModel c = icm ;
      if (cm instanceof IndexColorModel) c = (IndexColorModel) cm ;
      return c ;
   }

	// This is the required interface to filter a single pixel.
   // Any pixel that is fully transparent is returned without change.

	public int filterRGB(int x, int y, int rgb)
	{
      int p = (rgb & 0xffffff) ;
      int a = ((rgb & 0xff000000) >> 24) & 0xff ;
      if (p == transparentcolor) return p ;
      if (p == 0 && transparentcolor == 1) return p ;
		if (a == 0) return rgb ;
      if (transparency < a) a = transparency ;
      return ((a << 24) | p) ;
   }

   // This method overloads the filterRGBPixels method of the RGBImageFilter
   // class to filter a complete array of pixels all at once.  The alpha value
   // for each pixel is set to the filter transparency. This method is used by
   // direct color model images.  This includes JPG images and GIF images that
   // are represented as TYPE_INT_ARGB buffered images.

   public void filterRGBPixels
      (int x, int y, int w, int h, int[] pixels, int off, int scansize)
	{
		for (int index = off ; index < pixels.length ; index++)
      {
         int p = (pixels[index] & 0xffffff) ;
         int a = ((pixels[index] & 0xff000000) >> 24) & 0xff ;
			int r = (p & 0xff0000) >> 16 ;
			int g = (p & 0xff00) >> 8 ;
			int b = (p & 0xff) ;

         // If this filter has an index color model then we will translate
         // the pixel color from the old model to the new model.

         if (icm != null)
         {
				components[0] = r ;
				components[1] = g ;
				components[2] = b ;
				components[3] = a ;
				int pixel = icm.getDataElement(components,0) ;
				int rgb = cm.getRGB(pixel) ;
				p = (rgb & 0xffffff) ;

            // The following line does not change transparency for original
            // transparent pixels.  GIF images with transparency changes
            // adjusted transparent backgrounds.  The returned data element
            // has an updated transparency value and this results in the
            // background being shown with a new transparency.

	         if (a != 0) a = ((rgb & 0xff000000) >> 24) & 0xff ;
         }

         // Assign transparency.  Fully transparent pixels do not change.

         if (p == transparentcolor || (p == 0 && transparentcolor == 1))
            pixels[index] = p ;
			else if (a != 0)
         {
            if (transparency < a) a = transparency ;
            pixels[index] = ((a << 24) | p) ;
         }
      }

      // Return the new pixel colors to the image consumer.
      
      consumer.setPixels(x,y,w,h,ColorModel.getRGBdefault(),pixels,off,scansize);
   }


   // This method updates our IndexColorModel for a new palette.

   void setIndexColorModel(ColorModel cm)
   {
   	this.cm = cm ;
      canFilterIndexColorModel = (cm instanceof IndexColorModel) ;
   }


   // This method updates our filter transparency.

   void setTransparency(int t) { transparency = t ; }
}
