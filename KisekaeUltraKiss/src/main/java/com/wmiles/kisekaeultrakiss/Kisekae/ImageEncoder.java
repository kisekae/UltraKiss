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



// ImageEncoder - abstract class for writing out an image
//
// Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.
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


// Abstract class for writing out an image.
// <P>
// A framework for classes that encode and write out an image in
// a particular file format.
// <P>
// This provides a simplified rendition of the ImageConsumer interface.
// It always delivers the pixels as ints in the RGBdefault color model.
// It always provides them in top-down left-right order.
// If you want more flexibility you can always implement ImageConsumer
// directly.
// <P>
// <A HREF="/resources/classes/Acme/JPM/Encoders/ImageEncoder.java">Fetch the software.</A><BR>
// <A HREF="/resources/classes/Acme.tar.gz">Fetch the entire Acme package.</A>
// <P>
// @see GifEncoder
// @see PpmEncoder
// @see Acme.JPM.Decoders.ImageDecoder

public abstract class ImageEncoder implements ImageConsumer
{
	protected FileWriter fw;
	protected OutputStream out;
	protected ImageProducer producer;

   protected Palette palette = null ;
   protected Point offset = null ;
   protected Color transparentcolor = null ;
   protected Color backgroundcolor = null ;
   protected int multipalette = 0 ;
   protected int transparentIndex = -1 ;

	private int width = -1;
	private int height = -1;
	private int hintflags = 0;
	private boolean started = false;
	private boolean encoding;
	private IOException iox;
	private Hashtable props = null;

	private static final ColorModel rgbModel = ColorModel.getRGBdefault();


	/// Constructor.
	// @param img The image to encode.
	// @param out The stream to write the bytes to.

	public ImageEncoder(FileWriter fw, Image img, OutputStream out) throws IOException
	{
		this(fw, img.getSource(), out);
	}

	/// Constructor.
	// @param producer The ImageProducer to encode.
	// @param out The stream to write the bytes to.

	public ImageEncoder(FileWriter fw, ImageProducer producer, OutputStream out) throws IOException
	{
		this.producer = producer ;
		this.out = out ;
      this.fw = fw ;
	}


	// Methods that subclasses implement.

	/// Subclasses implement this to initialize an encoding.

	abstract void encodeStart(int w, int h) throws IOException;

	/// Subclasses implement this to actually write out some bits.  They
	// are guaranteed to be delivered in top-down-left-right order.
	// One int per pixel, index is row * scansize + off + col,
	// RGBdefault (AARRGGBB) color model.

	abstract void encodePixels(int x, int y, int w, int h, int[] rgbPixels,
   	int off, int scansize) throws IOException;

	/// Subclasses implement this to finish an encoding.

	abstract void encodeDone() throws IOException;


	// Our own methods.

	/// Call this after initialization to get things going.

	public synchronized void encode() throws IOException
	{
		iox = null;
		encoding = true;
		producer.startProduction( this );
		while ( encoding )
      {
			try { wait(); }
			catch ( InterruptedException e ) {}
      }
		if ( iox != null )
			throw iox;
	}

	// Set the producer for a new image frame.
	
   void setProducer(ImageProducer producer)
   {
   	this.producer = producer ;
   }


	private boolean accumulate = false;
	private int[] accumulator;

	private void encodePixelsWrapper(int x, int y, int w, int h, int[] rgbPixels,
   	int off, int scansize) throws IOException
	{
		if ( ! started )
		{
			started = true;
			encodeStart( width, height );
			if ((hintflags & TOPDOWNLEFTRIGHT) == 0)
			{
				accumulate = true;
				accumulator = new int[width * height];
			}
		}
		if (accumulate)
			for (int row = 0; row < h; ++row)
				System.arraycopy(
					rgbPixels, row * scansize + off,
					accumulator, ( y + row ) * width + x,
					w );
		else
			encodePixels(x, y, w, h, rgbPixels, off, scansize);
	}

	private void encodeFinish() throws IOException
	{
		if (accumulate)
		{
			encodePixels(0, 0, width, height, accumulator, 0, width);
			accumulator = null;
			accumulate = false;
		}
	}

   private synchronized void stop()
	{
		encoding = false;
		notifyAll();
	}


   // Default method to return bytes written.  This should be overloaded
   // by a subclass.

   int getBytesWritten() { return 0 ; }


	// Methods from ImageConsumer.

	public void setDimensions(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	public void setProperties(Hashtable props)
	{
		this.props = props;
	}

	public void setOffset(Point p)
	{
		this.offset = p;
	}

	public Point getOffset()
	{
		return offset;
	}

	public Color getTransparentColor()
	{
		return transparentcolor;
	}
   
	public int getTransparentIndex()
	{
		return transparentIndex;
	}

	public void setColorModel(ColorModel model)
	{
		// Ignore.
	}

	public void setHints(int hintflags)
	{
		this.hintflags = hintflags;
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels,
		int off, int scansize)
	{
		int[] rgbPixels = new int[w];
		for ( int row = 0; row < h; ++row )
		{
			int rowOff = off + row * scansize;
			for (int col = 0; col < w; ++col)
				rgbPixels[col] = model.getRGB( pixels[rowOff + col] & 0xff );
			try
			{
				encodePixelsWrapper(x, y + row, w, 1, rgbPixels, 0, w);
			}
			catch ( IOException e )
			{
				iox = e;
				stop();
				return;
			}
		}
	}

	public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels,
		int off, int scansize)
	{
		if (model == rgbModel)
		{
			try
			{
				encodePixelsWrapper(x, y, w, h, pixels, off, scansize);
			}
			catch ( IOException e )
			{
				iox = e;
				stop();
				return;
			}
		}
		else
		{
			int[] rgbPixels = new int[w];
			for (int row = 0; row < h; ++row)
			{
				int rowOff = off + row * scansize;
				for (int col = 0; col < w; ++col)
					rgbPixels[col] = model.getRGB( pixels[rowOff + col] );
				try
				{
					encodePixelsWrapper(x, y + row, w, 1, rgbPixels, 0, w);
				}
				catch ( IOException e )
				{
					iox = e;
					stop();
					return;
				}
			}
		}
	}

	public void imageComplete(int status)
	{
		producer.removeConsumer(this);
		if (status == ImageConsumer.IMAGEABORTED)
			iox = new IOException( "image aborted" );
		else
		{
			try
			{
				encodeFinish();
				encodeDone();
			}
			catch ( IOException e )
			{
				iox = e;
			}
		}
		stop();
	}
}
