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
* BmpCel class
*
* Purpose:
*
* This class encapsulates a BMP cel object.  A cel object contains the
* cel image, a position, and a group association.  The cel also
* knows how to draw itself.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.util.Vector ;
import javax.swing.* ;

final class BmpCel extends Cel
{
   static private Component component = new Component() { } ;

	// Kiss cel file attributes

	private int start = 0 ;						// Start of image data
	private int bits = 0 ;						// The bits in a pixel
	private int bytes = 0 ;						// The file size in bytes


	private static final int BI_RGB = 0;
	private static final int BI_RLE8 = 1;
	private static final int BI_RLE4 = 2;

	// Image entries.

	private int bytesread = 0 ;				  	// number of bytes read
	private int width = -1, height = -1;      // image width and height
	private int bfsize = 0 ; 					  	// file size in bytes
	private int biBitCount = 0 ;				  	// bits per pixel
	private int biCompression = 0 ; 			 	// compression code

	// Palette entries.

	private byte [] alpha = null ;				// Palette alpha colors
	private byte [] red = null ;					// Palette red colors
	private byte [] green = null ;				// Palette green colors
	private byte [] blue = null ;					// Palette blue colors

   // Transparent color changes.

	private int palettetransindex = -1 ; 		// Palette transparent index
   private boolean changed = false ;    		// True if setTransparent


	// Image data.

	private int [] pixels = null ;				// pixel data
	private int bitshift = -1;


	// Constructor

	public BmpCel(ArchiveFile zip, String file, Configuration ref)
	{
		setZipFile(zip) ;
		this.file = convertSeparator(file) ;
		this.ref = ref ;
		int n = file.lastIndexOf('.') ;
		extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
	}

	// Return the cel file size.

	int getBytes() { return bytes ; }

	// Return the cel pixel bit size.

	int getPixelBits() { return bits ; }

	// Return the cel encoding.

	String getEncoding()
   {
   	if (biCompression == BI_RGB) return "BMP Uncompressed" ;
   	if (biCompression == BI_RLE8) return "BMP RLE_8" ;
   	if (biCompression == BI_RLE4) return "BMP RLE_4" ;
      return super.getEncoding() ;
   }

	// Method to write our file contents to the specified output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
      int bytes = 0 ;
		if (!isWritable()) return -1 ;
      ImageEncoder encoder = getEncoder(fw,out,type) ;
      if (encoder == null)
      	throw new IOException("unable to encode BMP file as " + type) ;
      
      if (!(encoder instanceof GifEncoder))
      {
   		encoder.encode() ;
         bytes = encoder.getBytesWritten() ;
         return bytes ;
      }
      
      try { encoder.encode() ; }
      catch (IOException e)
      {
         if ("Too many colors for a GIF image".equals(e.getMessage()))
         {
            Object [] reduced = this.dither(256) ;
            Image img = (Image) reduced[0] ;
            encoder = new GifEncoder(fw,img,null,0,null,
               transparentcolor,backgroundcolor,transparency,out) ;
            encoder.encode() ;
         }
         else throw e ;
      }
      bytes += encoder.getBytesWritten() ;
      return bytes ;
	}

	// Set the object update state.  This also sets the update state of
	// the configuration entry if we have adjusted the cel transparency.

	void setUpdated(boolean b)
	{
   	super.setUpdated(b) ;
      if (!changed) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf == null) return ;
      Configuration config = mf.getConfig() ;
      if (config != null) config.setUpdated(true) ;
   }

   // Monitor changes to the transparent color setting for update control.

   void setTransparentIndex(int t)
   {
   	super.setTransparentIndex(t) ;
      changed = true ;
   }

   // Return the writable offset state.

   boolean isWriteableOffset() { return false ; }
   static boolean getWriteableOffset() { return false ; }




	// Object loading methods
	// ----------------------

	// Load the cel file.  This method creates an input stream to
	// read the cel pixels from the compressed zip file.  New palette
   // objects can be created for certain types of cel files.

	void load(Vector includefiles)
	{
		InputStream is = null ;				// The data I/O stream
		MemoryImageSource m = null ;		// The cel memory source
      String name = getRelativeName() ;
      if (name != null) name = name.toUpperCase() ;
      scaledimage = null ;
      filteredimage = null ;

		// Load the file if another copy of the cel has not already been
		// loaded.  If we have previously read the file use the prior
		// image.  Set the archive entry to reference an object copy.

		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,name) ;
		if (c != null && c.isLoaded())
      {
      	loadCopy(c) ;
			if (zip != null) ze = zip.getEntry(file) ;
         if (ze != null) ze.setCopy(copy) ;
         return ;
      }

		// Load a reference copy if we are accessing the same zip file as our
      // reference configuration.  On new KiSS sets we may not yet have known
      // paths to the files.

//		if (ref != null && zip != null && ref.isRestartable())
		if (ref != null && zip != null)
		{
      	ArchiveFile refzip = ref.getZipFile() ;
         String refpath = (refzip != null) ? refzip.getName() : null ;
         String zippath = zip.getName() ;
         if ((refpath == null && zippath == null) ||
         	(refpath != null && zippath != null && refpath.equals(zippath)))
         {
				c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),name) ;
            if (c == null) 
            {
               c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),"Import "+getName().toUpperCase()) ;
            }
				if (c != null && c.isLoaded())
	         {
	         	loadCopy(c) ;
               zip.addEntry(ze) ;
               zip.setUpdated(ze,c.isUpdated()) ;
	            return ;
            }
         }
		}

		// Nothing left now, but to read the new cel file.

		try
		{
			if (zip != null) ze = zip.getEntry(file) ;

			// Load a reference copy if it exists.

			if (ze == null)
			{
				if (ref != null)
				{
					c = (Cel) Cel.getByKey(Cel.getKeyTable(),ref.getID(),name) ;
					if (c != null && c.isLoaded())
               {
               	loadCopy(c) ;
                  copy = false ;
						return ;
               }

      			// Load an unloaded copy if it exists in the reference file.

              	ArchiveFile refzip = ref.getZipFile() ;
               if (refzip != null && !refzip.isOpen()) refzip.open() ;
               ze = (refzip != null) ? refzip.getEntry(getPath()) : null ;
				}
			}

         // If we have not yet found the file, check the INCLUDE list.

         String includename = null ;
         if (ze == null)
         {
            ze = searchIncludeList(includefiles,name) ;
            if (ze != null)
            {
               zip = ze.getZipFile() ;
               includename = (zip != null) ? zip.getFileName() : null ;
            }
         }

			// Determine the uncompressed file size.

			bytes = (ze == null) ? 0 : (int) ze.getSize() ;
         if (loader != null)
         {
            String s = Kisekae.getCaptions().getString("FileNameText") ;
            int i1 = s.indexOf('[') ;
            int j1 = s.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s = s.substring(0,i1) + file + s.substring(j1+1) ;
            loader.showFile(s + " [" + bytes + " bytes]" +
               ((includename != null) ? (" (" + includename + ")") : "")) ;
         }

			// Create the file input stream.

			is = (zip == null) ? null : zip.getInputStream(ze) ;
			if (is == null) throw new IOException("file not found") ;

			// Decode the BMP file format.  Note that BMP files may or may
         // not have palettes depending on the number of colors in the
         // file.

         readHeader(is) ;
         bits = biBitCount ;
         size.width = width ;
         size.height = height ;
         truecolor = (red == null) ;
			int w = size.width ;
			int h = size.height ;
         Point initialoffset = getInitialOffset() ;
         offset.x = initialoffset.x ;
         offset.y = initialoffset.y ;

			// Truecolor type bitmaps use a direct color model.

			if (truecolor)
			{
				int buffer[] = new int[w*h] ;
	         int rgbrow[] = new int[w] ;
	         for (int i = 0 ; i < h ; i++)
	         {
	         	readRow(is,i,rgbrow) ;
					System.arraycopy(rgbrow,0,buffer,i*w,w) ;
	         }
				cm = basecm = Palette.getDirectColorModel() ;
				m = new MemoryImageSource(w,h,cm,buffer,0,w) ;
				image = Toolkit.getDefaultToolkit().createImage(m) ;
            if (transparency != 255) changeTransparency(0) ;
            pid = null ;
			}

         // Palette type bitmaps require a palette with an index color model.

         if (!truecolor)
			{
				byte buffer[] = new byte[w*h] ;
	         byte rgbrow[] = new byte[w] ;
	         for (int i = 0 ; i < h ; i++)
	         {
	         	readRow(is,i,rgbrow) ;
					System.arraycopy(rgbrow,0,buffer,i*w,w) ;
				}

				// Create a palette for this bitmap.

            if (palette == null)
            {
   				palette = new Palette(zip,file) ;
   				palette.setInternal(true) ;
   				palette.setLine(getLine()) ;
               int [] trans = new int[1] ;
               trans[0] = palettetransindex ;
   				palette.setPalette(alpha,red,green,blue,1,red.length,null,trans) ;
   				palette.setIdentifier(this) ;
   				palette.setKey(palette.getKeyTable(),cid,palette.getIdentifier()) ;
   				palette.setKey(palette.getKeyTable(),cid,palette.getPath().toUpperCase()) ;
            }

            // Create an image.

  				cm = basecm = palette.createColorModel(transparency,multipalette) ;
  				if (cm == null) throw new KissException("invalid BMP palette") ;
				m = new MemoryImageSource(w,h,cm,buffer,0,w) ;
 				image = Toolkit.getDefaultToolkit().createImage(m) ;
				transparentcolor = palette.getTransparentColor(multipalette) ;
				backgroundcolor = palette.getBackgroundColor(multipalette) ;
            usedcolors = palette.getColorCount() ;
				pid = palette.getIdentifier() ;
			}

         // Convert the image to a buffered image.  This seems to clear up
         // a restart  problem where cel image copies, created on a restart,
         // fail to paint the first time they are drawn.  The problem has
         // been observed for JPG and GIF images.

         BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
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
         baseimage = image ;
         loaded = true ;
         imagewidth = w ;
         imageheight = h ;
		}

		// Watch for file size errors.

		catch (ArrayIndexOutOfBoundsException e)
		{
			error = true ;
			showError("Cel " + file + " is not a valid BMP cel.") ;
         int calcsize = size.width * size.height ;
         if (bits == 4) calcsize = (calcsize + 1) / 2 ;
			System.out.println("Size Exception, Cel " + file
         	+ ", Width: " + size.width + ", Height: " + size.height
				+ e.toString()) ;
		}

		// Watch for I/O errors.

		catch (IOException e)
		{
			error = true ;
         String s = e.getMessage() ;
         if (s == null) s = e.toString() ;
			showError("I/O Exception, Cel " + file + ", " + s) ;
			System.out.println(e.toString()) ;
		}

		// Watch for general Kiss exceptions.

		catch (Exception e)
		{
			error = true ;
         String s = e.getMessage() ;
         if (s == null) s = e.toString() ;
			showError("Exception, Cel " + file + ", " + s) ;
			e.printStackTrace() ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; is = null ; }
			catch (IOException e)
			{
				error = true ;
	         String s = e.getMessage() ;
	         if (s == null) s = e.toString() ;
				showError("I/O Exception, Cel " + file + ", " + s) ;
				e.printStackTrace() ;
			}
			if (error) image = null ;
		}
	}


	// Load a copy of the cel data from the specified cel.  The copy can
   // use a different palette file and have a different transparency.
   // The copy is unscaled.

	void loadCopy(Cel c)
	{
      if (c == null) return ;
      error = c.isError() ;
    	loaded = !error ;
		if (error) return ;
		image = baseimage = c.getBaseImage() ;
      scaledimage = null ;
      filteredimage = null ;
		if (image == null) return ;
      imagewidth = image.getWidth(null) ;
      imageheight = image.getHeight(null) ;

      // Set this cel's attributes from the cel copy.

      sf = 1.0f ;
      copy = true ;
      scaled = false ;
      scaledsize = null ;
      ze = c.getZipEntry() ;
		size = c.getBaseSize() ;
		bytes = c.getBytes() ;
		truecolor = c.isTruecolor() ;
      encoding = c.getEncoding() ;
		cm = basecm = c.getBaseColorModel() ;
      Point initialoffset = getInitialOffset() ;
      baseoffset = c.getBaseOffset() ;
      offset.x = baseoffset.x + initialoffset.x ;
      offset.y = baseoffset.y + initialoffset.y ;
      palette = c.getPalette() ;
      transparentcolor = c.getTransparentColor() ;
      setLastModified(c.lastModified()) ;
      setUpdated(c.isUpdated()) ;

      if (loader != null)
      {
         String s1 = Kisekae.getCaptions().getString("FileNameText") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + file + s1.substring(j1+1) ;
         loader.showFile(s1 + " [" + bytes + " bytes] (copy)") ;
      }

		// Get the required palette for this image copy.

      if (pid == null) pid = c.getPaletteID() ;
      if (truecolor) pid = null ;
		if (pid != null && pid.equals(c.getPaletteID()) &&
			transparency == c.getTransparency() &&
         mpid == c.getPaletteGroupID()) return ;

		// Palette cels require a new color model.  Truecolor cels use the
      // same color model with a new transparency.

		if (!truecolor)
		{
			Palette p = getPalette() ;
			if (p == null) return ;
	      if (mpid instanceof Integer) multipalette = ((Integer) mpid).intValue() ;
			cm = p.createColorModel(transparency,multipalette) ;
      }
      else
         cm = Palette.getDirectColorModel() ;

		// Establish the correct palette colors and transparency level.

		ImageProducer ip = image.getSource() ;
		ip = new FilteredImageSource(ip, new PaletteFilter(cm,basecm,transparency,transparentcolor)) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
      MediaTracker tracker = new MediaTracker(component) ;
      tracker.addImage(filteredimage,0) ;
      try { tracker.waitForAll(500) ; }
      catch (InterruptedException e) { }
		return ;
	}



	// Subclasses implement this to read in enough of the image stream
	// to figure out the width and height.  We decode the complete file
	// as the encoded scan lines is run from the bottom of the image to the
	// top.

	void readHeader( InputStream in ) throws IOException
	{
		// Read the BITMAPFILEHEADER structure.

		int bmheaderlocation = bytesread ;		// start of BITMAPFILEHEADER
		char c1 = (char) readByte(in) ;			// 'B'
		char c2 = (char) readByte(in) ;			// 'M'
		int bfSize = readDword(in) ; 				// size of file in bytes
		int r0 = readUint(in) ;         			// reserved (0)
		int r1 = readUint(in) ;             	// reserved (0)
		int bfOffBits = readDword(in) ;			// byte offset to bitmap data
		if ( c1 != 'B' || c2 != 'M' )
			throw new IOException( "invalid BMP file" ) ;

		// Read the BITMAPINFO structure.   This begins with a BITMAPINFOHEADER
		// structure.

		int bminfolocation = bytesread ;			// start of BITMAPINFO
		int biSize = readDword(in) ; 				// bytes in BITMAPINFO structure
		width = readLong(in) ;   					// width
		height = readLong(in) ;             	// height
		int biPlanes = readWord(in) ;				// number of planes for target (1)
		biBitCount = readWord(in) ;				// number of bits per pixel
		biCompression = readDword(in) ;			// compression code
		int biSizeImage = readDword(in) ;		// size of image in bytes
		int biXPelsPerMeter = readLong(in) ;	// horizontal resolution in pixels
		int biYPelsPerMeter = readLong(in) ; 	// vertical resolution in pixels
		int biClrUsed = readDword(in) ;			// number of color indexs used
		int biClrImportant = readDword(in) ;	// number of important color index

		// Locate the color table.

		int colortablelocation = bminfolocation + biSize ;
		while (bytesread < colortablelocation) { readByte(in) ; }
		int colortablesize = (1 << biBitCount) ;
		if (colortablesize > 256 || colortablesize < 0) colortablesize = 0 ;

		// Read the color table.  Identify the transparent color index
      // if the bitmap cel transparent color RGB was specified.

		if (colortablesize > 0)
		{
         palettetransindex = -1 ;
			alpha = new byte[colortablesize] ;
			red = new byte[colortablesize] ;
			green = new byte[colortablesize] ;
			blue = new byte[colortablesize] ;
			for (int i = 0 ; i < colortablesize ; i++)
			{
				blue[i] = (byte) readByte(in) ;
				green[i] = (byte) readByte(in) ;
				red[i] = (byte) readByte(in) ;
				alpha[i] = (byte) 255 ;
            int rgb = ((red[i] & 0xff) << 16) | ((green[i] & 0xff) << 8) | (blue[i] & 0xff) ;
            if (rgb == transparentindex) palettetransindex = i ;
				readByte(in) ;
			}
		}

		// Position ourselves to the actual bitmap data in the file.

		int bitmaplocation = bmheaderlocation + bfOffBits ;
		while (bytesread < bitmaplocation) { readByte(in) ; }
		pixels = new int[width*height] ;

		// Read the data and decode.

		int row = 0 ;
		int col = 0 ;
		while (row < height)
		{
			switch (biCompression)
			{
				// Uncompressed.  Scan lines are padded to 32 bits.

				case BI_RGB:
					pixels[row*width+col] = getPixel(biBitCount,in) ;
					col++ ;
					if (col >= width)
               {
               	int scanlinepad = (col * biBitCount) % 32 ;
                  if (scanlinepad > 0) scanlinepad = 32 - scanlinepad ;
                  while (scanlinepad > 0)
                  {
                    	int bitsread = 0 ;
                    	switch (biBitCount)
                     {
                       	case 1:
                          	readBit(in) ;
                           bitsread = 1 ;
                           break ;
                        case 4:
                          	readNibble(in) ;
                           bitsread = 4 ;
                           break ;
                        case 8:
                          	readByte(in) ;
                           bitsread = 8 ;
                           break ;
                        default:
                          	readByte(in) ;
                           bitsread = 8 ;
                           break ;
                     }
                     scanlinepad -= bitsread ;
                  }
               	col = 0 ;
                  row++ ;
               }
					break ;

				// Run length encoded.

				case BI_RLE4:
				case BI_RLE8:
					bitshift = -1 ;
					int mode = readByte(in) ;
					int n = readByte(in) ;

					// Absolute mode?  Each run of bytes must align on a 16 bit
               // boundary.

					if (mode == 0 && n > 2)
					{
						for (int i = 0 ; i < n ; i++)
						{
							pixels[row*width+col] = getCompressedPixel(biCompression,in) ;
							col++ ;
	  					}

                  // Align on 16 bit boundary.

                  int processed = (biCompression == BI_RLE4) ? (n+1)/2 : n ;
                  if ((processed % 2) != 0) readByte(in) ;
						break ;
					}

					// End of line control?

					if (mode == 0 && n == 0)
					{
						row++ ;
						col = 0 ;
						break ;
					}

					// End of bitmap?

					if (mode == 0 && n == 1)
					{
						row = height ;
						col = 0 ;
						break ;
					}

					// Delta offset into the bitmap?

					if (mode == 0 && n == 2)
					{
						int h = readByte(in) ;
						int v = readByte(in) ;
						row += v ;
						col += h ;
						break ;
					}

					// Encoded mode.

					for (int i = 0 ; i < mode ; i++)
					{
						pixels[row*width+col] = getCompressedPixel(biCompression,n) ;
						col++ ;
					}
					break ;

				default:
					throw new IOException( "invalid RLE encoding: " + biCompression) ;
			}
      }
	}


	// Subclasses implement this to read pixel data into the rgbRow
	// array, an int[width].  One int per pixel, no offsets or padding,
	// RGBdefault (AARRGGBB) color model

	void readRow( InputStream in, int row, int[] rgbRow ) throws IOException
	{
		for ( int col = 0; col < width ; col++ )
		{
			int topdownrow = (height - 1) - row ;
			if (topdownrow < 0)
				throw new IOException("BMP read row " + row + " > " + height) ;
			rgbRow[col] = pixels[topdownrow*width+col] ;
		}
	}

	void readRow( InputStream in, int row, byte[] rgbRow ) throws IOException
	{
		for ( int col = 0; col < width ; col++ )
		{
			int topdownrow = (height - 1) - row ;
			if (topdownrow < 0)
				throw new IOException("BMP read row " + row + " > " + height) ;
			rgbRow[col] = (byte) pixels[topdownrow*width+col] ;
		}
	}


	// Utility routine to read a byte.  Instead of returning -1 on
	// EOF, it throws an exception.

	private int readByte( InputStream in ) throws IOException
	{
		int b = in.read() ;
		if (b == -1) throw new EOFException("BMP EOF at " + bytesread) ;
		bytesread++ ;
		return b ;
	}


	// Utility routine to read a bit, packed eight to a byte, big-endian.

	private boolean readBit( InputStream in ) throws IOException
	{
		if ( bitshift == -1 )
		{
			bits = readByte( in ) ;
			bitshift = 7 ;
		}
		boolean bit = (((bits >> bitshift) & 1) != 0) ;
		--bitshift ;
		return bit ;
	}


	// Utility routine to read 4 bits, packed eight to a byte.

	private int readNibble( InputStream in ) throws IOException
	{
		if ( bitshift == -1 )
		{
			bits = readByte(in) ;
			bitshift = 7 ;
		}
		int nibble = (bits >> (bitshift+1-4)) & 0xf ;
      bitshift -= 4 ;
		return nibble ;
	}


	// Utility routine to read a character.

	private char readChar( InputStream in ) throws IOException
	{ return (char) readByte( in ); }


	// Utility routine to read a signed 16 bit integer.

	private int readInt( InputStream in ) throws IOException
	{
		int b1 = (readByte(in) & 0xFF) ;
		int b2 = readByte(in) ;
		return ((b2 << 8) + b1) ;
	}


	// Utility routine to read an unsigned 16 bit integer.

	private int readUint( InputStream in ) throws IOException
	{ return (readInt(in) & 0xFFFF) ; }


	// Utility routine to read a signed 32 bit integer.

	private int readLong( InputStream in ) throws IOException
	{
		int i1 = (readInt(in) & 0xFFFF) ;
		int i2 = readInt(in) ;
		return ((i2 << 16) + i1) ;
	}


	// Utility routine to read an unsigned 16 bit word.

	private int readWord( InputStream in ) throws IOException
	{ return readInt(in) ; }


	// Utility routine to read a 32 bit word.

	private int readDword( InputStream in ) throws IOException
	{ return readLong(in) ; }


	// Utility routine make an RGBdefault pixel from three color values.
   // Set the color as transparent if the bitmap had a transparent RGB.

	private int makeRgb( int r, int g, int b )
	{
   	r = r & 0xFF ;
   	g = g & 0xFF ;
   	b = b & 0xFF ;

		// Sun Java does does not properly treat color (0,0,0) as
		// truly transparent so we adjust the color to (0,0,1).

		int rgb = r << 16 | g << 8 | b ;
		int a = (rgb == transparentindex) ? 0 : 255 ;
		if (r == 0 && g == 0 && b == 0) b = 1 ;
		return (a << 24) | ( r << 16 ) | ( g << 8 ) | b ;
   }


	// Utility function to get a pixel value using RLE encoding.  The pixel
   // value is read directly from the input stream for absolute mode encoding.
   // All RLE encoded pixel values are indexes into the color table.

	private int getCompressedPixel(int rlemode,InputStream in) throws IOException
	{
		int b1 = 0 ;
		if (rlemode == BI_RLE4)
			b1 = readNibble(in) ;
		if (rlemode == BI_RLE8)
			b1 = readByte(in) ;
		return getPixel(b1) ;
	}


	// Utility function to get a pixel value using RLE encoding.  The pixel
   // value is replicated for the encoded mode of the encoding.

	private int getCompressedPixel(int rlemode, int n)  throws IOException
	{
		if (rlemode == BI_RLE4)
		{
			if ( bitshift == -1 )
			{
				bits = n ;
				bitshift = 7;
			}
			int pixel = getPixel((bits >> (bitshift+1-4)) & 0xF) ;
         bitshift -= 4 ;
         return pixel ;
		}

		if (rlemode == BI_RLE8)
			return getPixel(n) ;

		return 0 ;
	}

   // Function to return a pixel read from the input stream.  The bit count
   // determines the number of bits that define each pixel.  Monochrome,
   // 16 color or 256 color bitmap pixel values are all indexes into the
   // color table.  24 bit pixel values are truecolor and read as RGB color
   // bytes directly from the input stream.

	private int getPixel(int bits, InputStream in) throws IOException
	{
		switch (bits)
		{
			case 1:
				int index = (readBit(in)) ? 1 : 0 ;
				return getPixel(index) ;
			case 4:
				index = readNibble(in) ;
				return getPixel(index) ;
			case 8:
				index = readByte(in) ;
				return getPixel(index) ;
			case 24:
				int b = readByte(in) ;
				int g = readByte(in) ;
				int r = readByte(in) ;
				return makeRgb(r,g,b) ;
		}
		return 0 ;
	}

   // Function to return a pixel value given an index into the color table.

	private int getPixel(int index)
	{
		if (red == null) return 0 ;
		if (index < 0) return 0 ;
		if (index >= red.length) return 0 ;
      return index ;
	}



	// Object graphics methods
	// -----------------------

	// Draw the cel at its current position, constrained by the
	// defined bounding box.  We draw the cel only if is is visible
	// and intersects our drawing area.

	void draw(Graphics g, Rectangle box)
	{
   	if (error) return ;
		if (!visible) return ;
		Rectangle celBox = getBoundingBox() ;
		Rectangle r = (box == null) ? celBox : box.intersection(celBox) ;
		if (r.width < 0 || r.height < 0) return ;
      float scale = (scaled) ? sf : 1.0f ;

		// The cel intersects our drawing box.  Position to screen coordinates
      // if the cel has been scaled.

      int x = (int) (r.x * scale) ;
      int y = (int) (r.y * scale) ;
      int w = (int) Math.ceil(r.width * scale) ;
      int h = (int) Math.ceil(r.height * scale) ;
      int cx = (int) (celBox.x * scale) ;
      int cy = (int) (celBox.y * scale) ;

      // Draw the cel.

		Graphics gc = g.create(x,y,w,h) ;
		gc.translate(-x,-y) ;
      Image img = getImage() ;
      
      // Set the transparency.

      if (transparency < 255) 
      {
         float t = transparency / 255.0f ;
         if (t > 1) t = 1 ; else if (t < 0) t = 0 ;
         AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,t) ;
         if (gc instanceof Graphics2D) ((Graphics2D) gc).setComposite(ac) ;
      }
      
      // A workaround for Apple image distortion when cels exceed
      // the panel clip area.  Suspect this is due to a hardware
      // driver problem.
/*      
      if (OptionsDialog.getAppleMac())
      {
         try
         {
            Rectangle clip = gc.getClipBounds() ;
            BufferedImage bi = (BufferedImage) img ;
            if ((clip.x+clip.width < cx+w) || (clip.y+clip.height < cy+h))
            {
               int cw = clip.x+clip.width-cx ;
               int ch = clip.y+clip.height-cy ;
               cw = Math.min(cw,bi.getWidth()) ;
               ch = Math.min(ch,bi.getHeight()) ;
               if (cw < 0) cw = 0 ;
               if (ch < 0) ch = 0 ;
               img = bi.getSubimage(0,0,cw,ch) ;
            }
         }
         catch (Exception ae) { }
      }
*/      
      if (img != null) gc.drawImage(img,cx,cy,null) ;
		gc.dispose() ;
	}


	// Method to add a filter to the cel image for multipalette changes.
	// The base image was initially constructed for a multipalette of
	// zero. If we change the image to reference a different multipalette
	// then this will cause the cel to be drawn in a different color.
   // A color filter is created only if necessary.  Note that truecolor
   // cels cannot change their palette.

	void changePalette(Integer newmultipalette)
	{
		if (error) return ;
		if (truecolor) return ;
		if (palette == null) return ;
		int mp = (newmultipalette == null) ? 0 : newmultipalette.intValue() ;
      int n = palette.getMultiPaletteCount() - 1 ;
		if (mp > n) mp = n ;
      if (mp < 0) return ;

		// Update our internal palette arrays to match the palette object.

		Object [] o = palette.getMultiPaletteData(new Integer(mp)) ;
		alpha = (byte []) o[0] ;
		red = (byte []) o[1] ;
		green = (byte []) o[2] ;
		blue = (byte []) o[3] ;

      // Update our transparent index and background index to match
      // the palette object.

		background = palette.getBackgroundIndex() ;
		transparentindex = palette.getTransparentIndex() ;

		// Change the palette.  Make the change only if the requested
		// color model actually exists in the cel palette file.

		multipalette = mp ;
		ColorModel newcm = palette.createColorModel(transparency,multipalette) ;
		if (newcm != cm)
		{
			cm = newcm ;
         Image img = getScaledImage() ;
         if (img == null) img = getBaseImage() ;
         if (img == null) return ;

			// Construct an image filter.

         transparentcolor = palette.getTransparentColor(mp) ;
			ImageProducer base = img.getSource() ;
			PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor) ;
			ImageProducer ip = new FilteredImageSource(base,pf);
			filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
         MediaTracker tracker = new MediaTracker(component) ;
         tracker.addImage(filteredimage,0) ;
         try { tracker.waitForAll(500) ; }
         catch (InterruptedException e) { }
      }
	}


	// Method to change the cel transparency.  The change is relative
	// to the current transparency.

	void changeTransparency(int t, boolean bound, boolean ambiguous)
	{
		if (error) return ;
		if (image == null) return ;

      // Limit transparency between 0 and 255 if this is a bound change.

      int adjust = t ;
      if (bound)
      {
         if (transparency < 0) transparency = 0 ;
         if (transparency > 255) transparency = 255 ;
   		int n1 = 255 - transparency ;
         int n2 = n1 + t ;
         if (n2 < 0) n2 = 0 ;
         if (n2 > 255) n2 = 255 ;
         adjust = n2 - n1 ;
      }

		// Update the cel transparency value.

		int kisstransparency = 255 - transparency ;
      kisstransparency += adjust ;
		setTransparency(255 - kisstransparency,(OptionsDialog.getAllAmbiguous() && ambiguous),this) ;

		// Identify the image to filter.
/*
		filteredimage = null ;
      Image img = getScaledImage() ;
      if (img == null) img = getBaseImage() ;
      if (img == null) return ;

		// Palette cels require a new color model.  Truecolor cels use the
      // same color model with a new transparency.

		if (!truecolor)
		{
			if (palette == null) return ;
			cm = palette.createColorModel(transparency,multipalette) ;
		}

		// Construct an image filter.

//		img.flush() ;
		PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor) ;
		ImageProducer ip = new FilteredImageSource(img.getSource(),pf) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
      MediaTracker tracker = new MediaTracker(component) ;
      tracker.addImage(filteredimage,0) ;
      try { tracker.waitForAll(500) ; }
      catch (InterruptedException e) { }
 */
	}


	// Function to display a syntax error message.

	void showError(String s)
	{
   	errormessage = s ;
   	int line = getLine() ;
		if (line > 0) s = "Line [" + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
		else System.out.println(s) ;
	}
}