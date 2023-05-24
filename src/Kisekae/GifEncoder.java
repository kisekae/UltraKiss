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


// GifEncoder - write out an image as a GIF
//
// Transparency handling and variable bit size courtesy of Jack Palevich.
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


public class GifEncoder extends ImageEncoder
{
   private boolean interlace = false ;
	private GifFrame frame = null ;
   private int frameindex = 0 ;
   private int bytes = 0 ;                      // bytes written
   private int lastbytes = 0 ;                  // last bytes written
   private boolean hasExtension = false ;
   private int transparency = 255 ;

   private int width, height ;
   private int[][] rgbPixels ;
   private IntHashtable colorHash ;


   // Constructor from Image.

	public GifEncoder(FileWriter fw, Image img, Palette palette, int multipalette, Point offset,
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

   // Constructor from Image with interlace setting.

   public GifEncoder(FileWriter fw, Image img, OutputStream out, boolean interlace) throws IOException
   {
      super(fw, img, out);
      this.interlace = interlace;
   }

   // Constructor for an animated GIF frame.

   public GifEncoder(FileWriter fw, GifFrame frame, OutputStream out) throws IOException
   {
      super(fw, frame.getImage(), out) ;
      this.frame = frame ;
      frameindex = frame.getFrame() ;
   }


   // Method to initialize the encoder.

   void encodeStart(int width, int height) throws IOException
	{
		try
		{
			this.width = width;
			this.height = height;
			rgbPixels = new int[height][width];
		}
		catch (OutOfMemoryError e)
		{
			throw new IOException("GifEncoder: insufficient memory") ;
		}
	}


   // Method to capture the pixels during encoding.

   void encodePixels(int x, int y, int w, int h, int[] rgbPixels, int off, int scansize )
      throws IOException
   {
      // Save the pixels.
      for ( int row = 0; row < h; ++row )
         System.arraycopy(rgbPixels, row*scansize+off, this.rgbPixels[y+row], x, w);
   }


   // Method to terminate the encoding.  This is where all the work is
   // performed.  A palette is constructed by identifying all the colors in
   // the image.  Each pixel is mapped to a color table index.

   void encodeDone() throws IOException
   {
      colorHash = new IntHashtable() ;
      boolean globalcolortable = true ;
      int backgroundIndex = 0 ;
      int transparentRgb = -1 ;
      int loop = -1 ;
      int index = 0 ;

      // We must construct a palette for this image and convert pixel
      // colors into color indexes.  If we are encoding a GifFrame or
      // a palette then we use the known palette colors.  If we are
      // encoding an arbitrary image then the palette must be constructed
      // from the image pixels.

      try
      {
         int colors = 0 ;
      	byte [] red = null ;
      	byte [] green = null ;
      	byte [] blue = null ;
         transparentIndex = -1 ;

         // If we have a palette, use the palette.

	      if (palette != null)
	      {
	         transparentIndex = palette.getTransparentIndex() ;
	         backgroundIndex = palette.getBackgroundIndex() ;
	         hasExtension = (transparentIndex >= 0) ;
            colors = palette.getColorCount() ;
            Object [] data = palette.getMultiPaletteData(new Integer(multipalette)) ;
	      	red = (byte []) data[1] ;
	      	green = (byte []) data[2] ;
	      	blue = (byte []) data[3] ;
         }

         // If we have a frame, use the frame palette if it exists.

	      if (frame != null)
	      {
            int [] trans = frame.getTransparentIndex() ;
            int [] back = frame.getBackgroundIndex() ;
	         transparentIndex = (trans != null) ? trans[0] : -1 ;
	         backgroundIndex = (back != null) ? back[0] : -1 ;
	         globalcolortable = !frame.isLocalColorTable() ;
	         hasExtension = (frame.hasExtension()) || (transparentIndex >= 0) ;
            colors = frame.getColorCount() ;
	         loop = frame.getLoopCount() ;
	      	red = frame.getRed() ;
	      	green = frame.getGreen() ;
	      	blue = frame.getBlue() ;
            if (red == null || green == null || blue == null) colors = 0 ;
         }

         // Build the color hash table for this known palette.  All palette
			// colors are set to fully opaque except for the transparent color.
			// The table is constructed for multipalette 0.

			for (int i = 0 ; i < colors ; i++)
         {
         	int r = (i < red.length) ? red[i] & 0xFF : 0 ;
            int g = (i < green.length) ? green[i] & 0xFF : 0 ;
            int b = (i < blue.length) ? blue[i] & 0xFF : 0 ;
            boolean isTransparent = (i == transparentIndex) ;
            int a = (isTransparent) ? 0 : 0xFF ;
         	int rgb = (a << 24) | (r << 16) | (g << 8) | b ;
            if (isTransparent) transparentRgb = rgb ;
           	EncoderHashitem item = new EncoderHashitem(rgb,0,index,isTransparent) ;
            colorHash.put(rgb,item);
            index++ ;
	      }

	      // Now, scan the image to translate the image pixel colors into
	      // color palette indexes.  Image colors that do not exist in the
	      // palette are added to the palette.

         if (transparentIndex < 0 && transparentcolor != null)
            transparentRgb = transparentcolor.getRGB() ;
	      for ( int row = 0; row < height; ++row )
	      {
	         for ( int col = 0; col < width; ++col )
	         {
	            int rgb = rgbPixels[row][col];
	            boolean isTransparent = ((rgb >>> 24) == 0);
               if (transparentIndex < 0 && transparentcolor != null)
                  if ((transparentRgb & 0xFFFFFF) == (rgb & 0xFFFFFF)) 
                     isTransparent = true ;
                  
	            if (isTransparent)
	            {
	               if (transparentIndex < 0)
	               {
	                  // First transparent color; remember it.
	                  transparentIndex = index;
                     transparentcolor = new Color(rgb) ;
	                  transparentRgb = rgb;
	                  hasExtension = true ;
                  }
	               else if (rgb != transparentRgb)
	               {
	                  // A second transparent color; replace it with
	                  // the first one as one fully transparent color
                     // is the same as another.
	                  rgbPixels[row][col] = rgb = transparentRgb;
	               }
	            }
	            else
	            	rgb |= 0xFF000000 ;

	            // We have a pixel.  Find the color in our hash table.  If it
	            // does not exist, create a new color entry in the hash table.
	            // If it does exist, count the number of times this color is used.
	            // Note that pixels with color (0,0,1) are actually color (0,0,0).

	            if ((rgb & 0xffffff) == 1) rgb-- ;
	            EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb);
	            if (item == null)
	            {
	               if (index >= 256)
							throw new KissException("Too many colors for a GIF image") ;

	               // If we have a frame with a palette this pixel should exist.

						if ((frame != null && colors > 0) || palette != null)
	               {
	               	int a = (rgb & 0xff000000) >> 24 ;
	               	int r = (rgb & 0xff0000) >> 16 ;
	               	int g = (rgb & 0xff00) >> 8 ;
	               	int b = (rgb & 0xff) ;
                     if (!Kisekae.isBatch()) throw new KissException(
                        "frame " + frameindex + " at [" + row + "," + col + 
                        "] image color (" + r + "," + g + "," + b + 
                        ") not in palette") ;
	               }

	               // If we do not have a frame then remember this color.

	               item = new EncoderHashitem(rgb,1,index,isTransparent) ;
	               colorHash.put(rgb,item);
	               ++index;
	            }
	            else
	               ++item.count;
	         }
	      }

	      // Figure out how many bits to use to represent the colors.
         // GIF color arrays must be of size 2, 4, 16, or 256 colors.

	      int logColors;
	      if ( index <= 2 )
	         logColors = 1;
	      else if ( index <= 4 )
	         logColors = 2;
	      else if ( index <= 16 )
	         logColors = 4;
	      else
	         logColors = 8;

	      // Turn the colors into color vectors.  The color vectors represent
	      // the local or global palette written to the encoded file.

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

	      // Encode the GIF file.

	      GIFEncode( out, width, height, interlace, backgroundIndex, transparentIndex,
	         globalcolortable, loop, logColors, reds, grns, blus ) ;
         if (fw != null) fw.updateProgress(bytes-lastbytes) ;
      }

		// Watch for encoding errors.

		catch (OutOfMemoryError e)
		{
			throw new IOException("GifEncoder: insufficient memory") ;
		}
		catch (KissException e)
		{
			System.out.println("GifEncoder: exception during encoding, " + e.getMessage()) ;
			throw new IOException(e.getMessage()) ;
		}
		catch (Exception e)
		{
			System.out.println("GifEncoder: exception during encoding") ;
			e.printStackTrace() ;
			throw new IOException("Internal exception during GIF encoding") ;
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

   byte GetPixel(int x, int y) throws IOException
   {
   	int rgb = rgbPixels[y][x] ;
      if ((rgb >>> 24) != 0) rgb |= 0xFF000000 ;
		if ((rgb & 0xffffff) == 1) rgb-- ;
		EncoderHashitem item = (EncoderHashitem) colorHash.get(rgb);
      if (item == null)
			throw new IOException("GIF color not found, RGB ("
         	+ ((rgb & 0xFF0000) >> 16) + "," + ((rgb & 0xFF00) >> 8)
            + "," + (rgb & 0xFF) + ")");
		return (byte) item.index;
   }


   // Function to return the number of bytes written.

	int getBytesWritten() { return bytes ; }


   // ---------------------------------------------------------------------
   // The GIF encoding routine.
   // Adapted from ppmtogif, which is based on GIFENCOD by David
   // Rowley <mgardi@watdscu.waterloo.edu>.  Lempel-Zim compression
   // based on "compress".

   int Width, Height;
   boolean Interlace;
   int curx, cury;
   int CountDown;
   int Pass = 0;
   int LeftOfs, TopOfs;

   void GIFEncode( OutputStream outs, int Width, int Height, boolean Interlace,
   		int Background, int Transparent, boolean global, int loop, int BitsPerPixel,
         byte[] Red, byte[] Green, byte[] Blue )
      throws IOException
   {
      byte B;
      int ColorMapSize;
      int InitCodeSize;
      int disposal = 0 ;
      int delay = 0 ;
      int xoffset = (offset != null) ? offset.x : 0 ;
      int yoffset = (offset != null) ? offset.y : 0 ;
      int i;

      this.Width = width = Width;
      this.Height = height = Height;
      this.Interlace = Interlace;
      ColorMapSize = 1 << BitsPerPixel;
      LeftOfs = TopOfs = 0;

      // Establish our attributes if we are saving an animated frame.

      if (frame != null)
      {
			if (offset == null) xoffset = frame.getBaseOffset().x ;
			if (offset == null) yoffset = frame.getBaseOffset().y ;
			width = frame.getSize().width ;
         height = frame.getSize().height ;
         disposal = frame.getDisposal() ;
         delay = frame.getDelay() / 10 ;
      }

      // Calculate number of bits we are expecting.

      if (width > Width) width = Width ;
      if (height > Height) height = Height ;
      if (LeftOfs > Width) LeftOfs = Width-1 ;
      if (TopOfs > Height) TopOfs = Height-1 ;
      CountDown = width * height;

      // Indicate which pass we are on (if interlace)

      Pass = 0;

      // The initial code size

      if ( BitsPerPixel <= 1 )
         InitCodeSize = 2;
      else
         InitCodeSize = BitsPerPixel;

      // Set up the current x and y position

      curx = LeftOfs;
      cury = TopOfs;

      if (frameindex == 0)
      {
	      // Write the Magic header
	      writeString( outs, "GIF89a" );

	      // Write out the screen width and height
	      Putword( Width, outs );
	      Putword( Height, outs );

	      // Indicate if there is a global colour map
	      B = (byte) ((global) ? 0x80 : 0) ;
	      // OR in the resolution
	      B |= (byte) ( ( 8 - 1 ) << 4 );
	      // Not sorted
	      // OR in the Bits per Pixel
	      B |= (byte) ( ( BitsPerPixel - 1 ) );

	      // Write it out
	      Putbyte( B, outs );

	      // Write out the Background colour
	      Putbyte( (byte) Background, outs );

	      // Pixel aspect ratio - 1:1.
	      //Putbyte( (byte) 49, outs );
	      // Java's GIF reader currently has a bug, if the aspect ratio byte is
	      // not zero it throws an ImageFormatException.  It doesn't know that
	      // 49 means a 1:1 aspect ratio.  Well, whatever, zero works with all
	      // the other decoders I've tried so it probably doesn't hurt.
	      Putbyte( (byte) 0, outs );

	      // Write out the Global Colour Map
         if (global)
         {
		      for ( i = 0; i < ColorMapSize; ++i )
		      {
		         Putbyte( Red[i], outs );
		         Putbyte( Green[i], outs );
		         Putbyte( Blue[i], outs );
            }
			}

         // Write the loop application extension.
         if (loop >= 0)
         {
         	Putbyte( (byte) 0x21, outs) ;
            Putbyte( (byte) 0xFF, outs) ;
            Putbyte( (byte) 11, outs) ;
            writeString(outs,"NETSCAPE2.0") ;
            Putbyte( (byte) 3, outs) ;
            Putbyte( (byte) 1, outs) ;
            Putword( loop, outs) ;
            Putbyte( (byte) 0, outs) ;
         }
      }

      // Write out extension for transparent colour index, if necessary.
      if (hasExtension)
      {
         Putbyte( (byte) '!', outs );
         Putbyte( (byte) 0xf9, outs );
         Putbyte( (byte) 4, outs );
         int packedfields = (disposal << 2) ;
         if (Transparent != -1) packedfields |= 1 ;
         Putbyte( (byte) packedfields, outs );
         Putword( delay, outs );
         Putbyte( (byte) ((Transparent < 0) ? 0 : Transparent), outs );
         Putbyte( (byte) 0, outs );
      }

      // Write an Image separator
      Putbyte( (byte) ',', outs );

      // Write the Image header
      Putword( xoffset, outs );
      Putword( yoffset, outs );
      Putword( width, outs );
      Putword( height, outs );

		// Indicate if there is a local colour map
      B = (byte) ((!global) ? 0x80 : 0) ;
      // OR in the interlace bit
      if (Interlace) B |= (byte) 0x40 ;
      // Not sorted
      // OR in the Bits per Pixel
      if (!global) B |= (byte) ((BitsPerPixel - 1));

      // Write it out
      Putbyte( B, outs );

      // Write out the Local Colour Map
      if (!global)
      {
	      for ( i = 0; i < ColorMapSize; ++i )
	      {
	         Putbyte( Red[i], outs );
	         Putbyte( Green[i], outs );
	         Putbyte( Blue[i], outs );
         }
		}

      // Write out the initial code size
      Putbyte( (byte) InitCodeSize, outs );

      // Go and actually compress the data
      compress( InitCodeSize+1, outs );

      // Write out a Zero-length packet (to end the series)
      Putbyte( (byte) 0, outs );

      // Write the GIF file terminator
      if (frame == null)
	      Putbyte( (byte) ';', out );
   }


   // Bump the 'curx' and 'cury' to point to the next pixel
   void BumpPixel()
   {
      // Bump the current X position
      ++curx;

      // If we are at the end of a scan line, set curx back to the beginning
      // If we are interlaced, bump the cury to the appropriate spot,
      // otherwise, just increment it.
      if (curx == width + LeftOfs)
      {
         curx = LeftOfs;

         if (!Interlace)
            ++cury;
         else
         {
            switch( Pass )
            {
            case 0:
               cury += 8;
               if ( cury >= height + TopOfs)
               {
                  ++Pass;
                  cury = TopOfs + 4;
               }
               break;

            case 1:
               cury += 8;
               if ( cury >= height  + TopOfs)
               {
                  ++Pass;
                  cury = TopOfs + 2;
               }
               break;

            case 2:
               cury += 4;
               if ( cury >= height  + TopOfs)
               {
                  ++Pass;
                  cury = TopOfs + 1;
               }
               break;

            case 3:
               cury += 2;
               break;
            }
         }
      }
   }

   static final int EOF = -1;

   // Return the next pixel from the image
   int GIFNextPixel() throws IOException
   {
      byte r;

      if (CountDown == 0) return EOF;
      --CountDown;
      r = GetPixel(curx,cury);
      BumpPixel();
      return r & 0xff;
   }

   // Write out a word to the GIF file
   void Putword( int w, OutputStream outs ) throws IOException
	{
      Putbyte( (byte) ( w & 0xff ), outs );
      Putbyte( (byte) ( ( w >> 8 ) & 0xff ), outs );
   }

   // Write out a byte to the GIF file
   void Putbyte( byte b, OutputStream outs ) throws IOException
   { outs.write( b ) ; bytes++ ; }

   // Write a string to the GIF file.
   void writeString( OutputStream out, String str ) throws IOException
   {
      byte[] buf = str.getBytes();
      out.write( buf ) ;
      bytes += buf.length ;
   }



   // ------------------------------------------------------------------
	// GIFCOMPR.C       - GIF Image compression routines
	//
	// Lempel-Ziv compression based on 'compress'.  GIF modifications by
	// David Rowley (mgardi@watdcsu.waterloo.edu)

	// General DEFINEs

	static final int BITS = 12;
	static final int HSIZE = 5003;		// 80% occupancy

	// GIF Image compression - modified 'compress'
	//
	// Based on: compress.c - File compression ala IEEE Computer, June 1984.
	//
	// By Authors:  Spencer W. Thomas      (decvax!harpo!utah-cs!utah-gr!thomas)
	//              Jim McKie              (decvax!mcvax!jim)
	//              Steve Davies           (decvax!vax135!petsd!peora!srd)
	//              Ken Turkowski          (decvax!decwrl!turtlevax!ken)
	//              James A. Woods         (decvax!ihnp4!ames!jaw)
	//              Joe Orost              (decvax!vax135!petsd!joe)

	int n_bits;							// number of bits/code
	int maxbits = BITS;				// user settable max # bits/code
	int maxcode;						// maximum code, given n_bits
	int maxmaxcode = 1 << BITS; 	// should NEVER generate this code

	final int MAXCODE( int n_bits )
	{
		return ( 1 << n_bits ) - 1;
	}

	int[] htab = new int[HSIZE];
	int[] codetab = new int[HSIZE];

	int hsize = HSIZE;				// for dynamic table sizing

	int free_ent = 0;					// first unused entry

	// block compression parameters -- after all codes are used up,
	// and compression rate changes, start over.
	boolean clear_flg = false;

	// Algorithm:  use open addressing double hashing (no chaining) on the
	// prefix code / next character combination.  We do a variant of Knuth's
	// algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
	// secondary probe.  Here, the modular division first probe is gives way
	// to a faster exclusive-or manipulation.  Also do block compression with
	// an adaptive reset, whereby the code table is cleared when the compression
	// ratio decreases, but after the table fills.  The variable-length output
	// codes are re-sized at this point, and a special CLEAR code is generated
	// for the decompressor.  Late addition:  construct the table according to
	// file size for noticeable speed improvement on small files.  Please direct
	// questions about this implementation to ames!jaw.

	int g_init_bits;

	int ClearCode;
	int EOFCode;

	void compress( int init_bits, OutputStream outs ) throws IOException
	{
		int fcode;
		int i /* = 0 */;
		int c;
		int ent;
		int disp;
		int hsize_reg;
		int hshift;

		// Set up the globals:  g_init_bits - initial number of bits
		g_init_bits = init_bits;

		// Set up the necessary values
		clear_flg = false;
		n_bits = g_init_bits;
		maxcode = MAXCODE( n_bits );

		ClearCode = 1 << ( init_bits - 1 );
		EOFCode = ClearCode + 1;
		free_ent = ClearCode + 2;

		char_init();

		ent = GIFNextPixel();

		hshift = 0;
		for ( fcode = hsize; fcode < 65536; fcode *= 2 )
			++hshift;
		hshift = 8 - hshift;			// set hash code range bound

		hsize_reg = hsize;
		cl_hash( hsize_reg );	// clear hash table

		output( ClearCode, outs );

		outer_loop:
		while ( (c = GIFNextPixel()) != EOF )
		{
			fcode = ( c << maxbits ) + ent;
			i = ( c << hshift ) ^ ent;		// xor hashing

			if ( htab[i] == fcode )
			{
				ent = codetab[i];
				continue;
			}
			else if ( htab[i] >= 0 )	// non-empty slot
			{
				disp = hsize_reg - i;	// secondary hash (after G. Knott)
				if ( i == 0 )
					disp = 1;
				do
				{
					if ( (i -= disp) < 0 )
						i += hsize_reg;

					if ( htab[i] == fcode )
					{
						ent = codetab[i];
						continue outer_loop;
					}
				}
				while ( htab[i] >= 0 );
			}
			output( ent, outs );
			ent = c;
			if ( free_ent < maxmaxcode )
			{
				codetab[i] = free_ent++;	// code -> hashtable
				htab[i] = fcode;
			}
			else
				cl_block( outs );
		}
		// Put out the final code.
		output( ent, outs );
		output( EOFCode, outs );
	}

	// output
	//
	// Output the given code.
	// Inputs:
	//      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
	//              that n_bits =< wordsize - 1.
	// Outputs:
	//      Outputs code to the file.
	// Assumptions:
	//      Chars are 8 bits long.
	// Algorithm:
	//      Maintain a BITS character long buffer (so that 8 codes will
	// fit in it exactly).  Use the VAX insv instruction to insert each
	// code in turn.  When the buffer fills up empty it and start over.


	int cur_accum = 0;
	int cur_bits = 0;
	int masks[] = { 0x0000, 0x0001, 0x0003, 0x0007, 0x000F,
		    0x001F, 0x003F, 0x007F, 0x00FF,
		    0x01FF, 0x03FF, 0x07FF, 0x0FFF,
		    0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF };

	void output( int code, OutputStream outs ) throws IOException
	{
		cur_accum &= masks[cur_bits];

		if ( cur_bits > 0 )
			cur_accum |= ( code << cur_bits );
		else
			cur_accum = code;

		cur_bits += n_bits;

		while ( cur_bits >= 8 )
		{
			char_out( (byte) ( cur_accum & 0xff ), outs );
			cur_accum >>= 8;
			cur_bits -= 8;
		}

		// If the next entry is going to be too big for the code size,
		// then increase it, if possible.
		if ( free_ent > maxcode || clear_flg )
		{
			if ( clear_flg )
			{
				maxcode = MAXCODE(n_bits = g_init_bits);
				clear_flg = false;
			}
			else
			{
				++n_bits;
				if ( n_bits == maxbits )
					maxcode = maxmaxcode;
				else
					maxcode = MAXCODE(n_bits);
			}
		}

		if ( code == EOFCode )
		{
			// At EOF, write the rest of the buffer.
			while ( cur_bits > 0 )
			{
				char_out( (byte) ( cur_accum & 0xff ), outs );
				cur_accum >>= 8;
				cur_bits -= 8;
			}

			flush_char( outs );
		}
      
      // Track the output display
      
      int progress = bytes - lastbytes ;
      if (fw != null && progress > 1024) 
      {
         fw.updateProgress(bytes-lastbytes) ;
         lastbytes = bytes ;
      }
	}

	// Clear out the hash table

	// table clear for block compress
	void cl_block( OutputStream outs ) throws IOException
	{
		cl_hash( hsize );
		free_ent = ClearCode + 2;
		clear_flg = true;

		output( ClearCode, outs );
	}

	// reset code table
	void cl_hash( int hsize )
	{
		for ( int i = 0; i < hsize; ++i )
			htab[i] = -1;
	}

	// GIF Specific routines

	// Number of characters so far in this 'packet'
	int a_count;

	// Set up the 'byte output' routine
	void char_init()
	{
		a_count = 0;
	}

	// Define the storage for the packet accumulator
	byte[] accum = new byte[256];

	// Add a character to the end of the current packet, and if it is 254
	// characters, flush the packet to disk.
	void char_out( byte c, OutputStream outs ) throws IOException
	{
		accum[a_count++] = c;
		if ( a_count >= 254 )
			flush_char( outs );
	}

	// Flush the packet to disk, and reset the accumulator
	void flush_char( OutputStream outs ) throws IOException
	{
		if ( a_count > 0 )
		{
			outs.write( a_count );
			outs.write( accum, 0, a_count );
         bytes += (a_count + 1) ;
			a_count = 0;
		}
	}
}
