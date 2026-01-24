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



// PpmEncoder - write out an image as a PPM
//
// Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// Visit the ACME Labs Java page for up-to-date versions of this and other
// fine Java utilities: http://www.acme.com/java/


import java.util.* ;
import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;

/// Write out an image as a PPM.
// <P>
// Writes an image onto a specified OutputStream in the PPM file format.
// <P>
// <A HREF="/resources/classes/Acme/JPM/Encoders/PpmEncoder.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
// <P>
// @see ToPpm

public class PpmEncoder extends ImageEncoder
{
	private int bytes = 0 ;                   // bytes written
   private int lastbytes = 0 ;               // last number of bytes written
   private int transparency = 255 ;

   // Constructor from Image.

	public PpmEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
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


	void encodeStart( int width, int height ) throws IOException
	{
		putString( out, "P6\n" );
		putString( out, width + " " + height + "\n" );
		putString( out, "255\n" );
	}

	void putString( OutputStream out, String str ) throws IOException
	{
		byte[] buf = str.getBytes();
		out.write(buf) ;
      bytes += str.length() ;
	}

	void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize)
		throws IOException
	{
		try
		{
			byte[] ppmPixels = new byte[w * 3] ;
			for (int row = 0 ; row < h ; ++row)
			{
				int rowOff = off + row * scansize;
				for (int col = 0 ; col < w ; ++col)
				{
					int i = rowOff + col ;
					int j = col * 3 ;
					int rgb = rgbPixels[i] ;

	            // If our pixel is transparent replace it with the
	            // background color.
               
	            boolean isTransparent = ((rgb >>> 24) == 0);
	            if (isTransparent)
	            {
	               if (transparentIndex < 0)
	               {
	                  // First transparent color; remember it.
	                  transparentIndex = i;
	                  transparentcolor = new Color(rgb);
                  }
               }
					if ((rgb & 0xff000000) == 0)
						if (backgroundcolor != null ) rgb = backgroundcolor.getRGB() ;

					// Isolate the RGB components of the pixel.

					int r = ((rgb & 0xff0000 ) >> 16) ;
					int g = ((rgb & 0x00ff00 ) >> 8) ;
					int b = (rgb & 0x0000ff) ;

					// Sun Java does does not properly treat color (0,0,0) as
					// truly transparent so we adjust the color to (0,0,1).

					if (r == 0 && g == 0 && b == 1) b = 0 ;
					ppmPixels[j] = (byte) r ;
					ppmPixels[j+1] = (byte) g ;
					ppmPixels[j+2] = (byte) b ;
				}
            
				out.write(ppmPixels) ;
            bytes += ppmPixels.length ;
            if (fw != null) fw.updateProgress(bytes-lastbytes) ;
			}
		}

		// Watch for errors.

		catch (OutOfMemoryError e)
		{
			throw new IOException("PpmEncoder: insufficient memory") ;
		}
		catch (Exception e)
		{
			PrintLn.println("PpmEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal exception during PPM encoding") ;
		}
	}

	void encodeDone() throws IOException
	{
      producer = null ;
     	palette = null ;
      multipalette = 0 ;
      offset = null ;
	}


   // Function to return the number of bytes written.

	int getBytesWritten() { return bytes ; }
}
