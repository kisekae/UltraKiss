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
* GifCel class
*
* Purpose:
*
* This class encapsulates a GIF cel object.  A cel object contains the
* cel image, a position, and a group association.  The cel also
* knows how to draw itself.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.util.Vector ;

final class GifCel extends Cel
{
	static private Component component = new Component() { } ;

	// GIF cel file attributes

   private Vector frames = null ;			// The GIF image frames
	private BufferedImage source = null ;  // The image prior to update
	private int start = 0 ;						// Start of image data
	private int bits = 0 ;						// The bits in a pixel
	private int bytes = 0 ;						// The file size in bytes

   // Animation frame attributes

   private int frame = 0 ;						// Index of current frame
   private int framedelay = 0 ;				// Time delay until next frame
   private long frametime = 0 ;				// Time elapsed for next frame
   private int framedispose = 0 ;			// Frame disposal setting

	// Global palette storage common to all frames.  This contains RGB
   // byte arrays that are created when the first frame is read.  All
   // other frames with a global palette reference these arrays.

   private Object [] globalpalette = new Object[3] ;


	// Constructor

	public GifCel(ArchiveFile zip, String file, Configuration ref)
	{
		setZipFile(zip) ;
		this.file = convertSeparator(file) ;
		this.ref = ref ;
		frames = new Vector() ;
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
   	if (!(frames == null || frames.size() == 0))
      {
			GifFrame cf = (GifFrame) frames.elementAt(0) ;
	   	String version = (cf == null) ? "" : cf.getVersion() ;
	   	return "GIF " + version + " LZW" ;
      }
      return super.getEncoding() ;
   }

	// Return the cel encoding version (87 or 89a).

	String getVersion()
   {
   	if (!(frames == null || frames.size() == 0))
      {
			GifFrame cf = (GifFrame) frames.elementAt(0) ;
	   	String version = (cf == null) ? "" : cf.getVersion() ;
	   	if (version != null) return version.toLowerCase() ;
      }
      return "" ;
   }

	// Return the cel frames.

	Vector getFrames() { return frames ; }

	// Add a new cel frames.

	void addFrame(GifFrame cf) { frames.addElement(cf) ; }

	// Return the global palette object.

	Object [] getGlobalPaletteArrays() { return globalpalette ; }

   // Return an indicator if the cel has a local palette.

   boolean isLocalPalette()
   {
   	if (frames == null) return true ;
      if (frame >= frames.size()) return true ;
		GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      return cf.isLocalColorTable() ;
   }

   // Return an indicator if the cel has a global palette.

   boolean hasGlobalPalette()
   {
   	return (globalpalette[0] != null) ;
   }


   // A function to construct a global palette object for this cel.

   Palette getGlobalPalette()
   {
      if (globalpalette[0] == null) return null ;
      byte [] red = (byte []) globalpalette[0] ;
      byte [] green = (byte []) globalpalette[1] ;
      byte [] blue = (byte []) globalpalette[2] ;
      int colors = red.length ;
      byte [] alpha = new byte[colors] ;
      for (int i = 0 ; i < colors ; i++) alpha[i] = (byte) 255 ;
      Palette p = new Palette() ;
      p.setPalette(alpha,red,green,blue,1,colors,background,transparentindex) ;
      p.setName(getName()) ;
      p.setInternal(true) ;
      return p ;
   }

   // Return the writable offset state.

   boolean isWriteableOffset() { return isUpdated() ; }
   static boolean getWriteableOffset() { return true ; }

	// Method to write the file contents to the specified output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
      int bytes = 0 ;
		if (!isWritable()) return -1 ;
      ImageEncoder encoder = getEncoder(fw,out,type) ;
      if (encoder == null)
      	throw new IOException("unable to encode GIF file as " + type) ;

      // If we are not encoding as a GIF file, do the encoding and exit.

      if (!(encoder instanceof GifEncoder))
      {
			encoder.encode() ;
	      bytes = encoder.getBytesWritten() ;
	      return bytes ;
      }
      
      // If we have no frames, encode this cel image.  
      
      if (frames.size() == 0)
      {
         try
         {
            encoder = new GifEncoder(fw,getImage(),null,0,null,
               transparentcolor,backgroundcolor,transparency,out) ;
            encoder.encode() ;
            bytes += encoder.getBytesWritten() ;
         }
         catch (IOException e)
         {
            if ("Too many colors for a GIF image".equals(e.getMessage()))
            {
               Object [] reduced = this.dither(256) ;
               Image img = (Image) reduced[0] ;
               encoder = new GifEncoder(fw,img,null,0,null,
                  transparentcolor,backgroundcolor,transparency,out) ;
               encoder.encode() ;
               bytes += encoder.getBytesWritten() ;
            }
            else throw e ;
         }
      }
      
      // Otherwise, write each frame separately.  The GIF encoder writes 
      // the file header blocks for frame 0 and then all successive frames.

      else
      {
         Point offset = encoder.getOffset() ;
         for (int i = 0 ; i < frames.size() ; i++)
         {
            try
            {
               GifFrame cf = (GifFrame) frames.elementAt(i) ;
               Image img = cf.getImage() ;
               encoder = new GifEncoder(fw,img,out,cf) ;
               if (i == 0) encoder.setOffset(offset) ;
               encoder.encode() ;
               bytes += encoder.getBytesWritten() ;
            }
            catch (IOException e)
            {
               if ("Too many colors for a GIF image".equals(e.getMessage()))
               {
                  Object [] reduced = this.dither(256) ;
                  Image img = (Image) reduced[0] ;
                  encoder = new GifEncoder(fw,img,null,0,null,
                     transparentcolor,backgroundcolor,transparency,out) ;
                  encoder.encode() ;
                  bytes += encoder.getBytesWritten() ;
               }
               else throw e ;
            }
         }
      }

      // Write the GIF file terminator.

      out.write((byte) ';') ;
      setFrame(frame) ;
      return bytes + 1 ;
	}


   // Animation specific methods.

   int getInterval() { return framedelay ; }

   void setInterval(int t) { framedelay = t ; }

   long getTime() { return frametime ; }

   void setTime(long t) { frametime = t ; }

   int getDisposal() { return framedispose ; }

   int getFrame() { return frame ; }

   int getFrameCount() { return (frames == null) ? 0 : frames.size() ; }

	// Return the number of colors in the palette.

	int getColorCount()
   {
      if (palette == null) return 0 ;
      return palette.getColorCount() ;
   }

	// Set the cel visibility flag.  We overload the superclass method
   // to ensure that any visiblity change resets the GIF cel to frame 0
   // and resets the animation loop counter.

	void setVisible(boolean b)
	{
		super.setVisible(b) ;
   	loop = 0 ;
      setTime(0) ;
		setFrame(0) ;
	}

   // Set the cel background index.  Propagate the change to all frames.

   void setBackgroundIndex(int n)
   {
      super.setBackgroundIndex(n) ;
      if (frames == null) return ;
      for (int i = 0 ; i < frames.size() ; i++)
      {
         GifFrame cf = (GifFrame) frames.elementAt(i) ;
         cf.setBackgroundIndex(n) ;
      }
   }

   // Set the image.  Propagate to the current frame.

   void setImage(Image img)
   {
      super.setImage(img) ;
      if (frames == null) return ;
      if (frame < 0 || frame >= frames.size()) return ;
      GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      cf.setImage(img);
   }

   // Set the palette.  Establishes the global palette arrays.

   void setPalette(Palette p)
   {
      super.setPalette(p) ;
      if (p == null) return ;
      Object [] palettedata = p.getPaletteData() ;
      globalpalette[0] = palettedata[1] ;  // red
      globalpalette[1] = palettedata[2] ;  // green
      globalpalette[2] = palettedata[3] ;  // blue
      if (frames == null) return ;
      
      // Propagate to all frames using the global palette.
      
      for (int i = 0 ; i < frames.size() ; i++)
      {
         GifFrame cf = (GifFrame) frames.elementAt(i) ;
         if (!cf.isLocalColorTable()) 
            cf.setLocalPalette(false) ;
      }
   }

	// Set the cel loop count.

   void setLoopLimit(int n)
   {
      super.setLoopLimit(n) ;
      if (frames == null) return ;
      for (int i = 0 ; i < frames.size() ; i++)
      {
         GifFrame cf = (GifFrame) frames.elementAt(i) ;
         cf.setLoopLimit(n) ;
      }
   }

	// Get the cel requested transparent color for cel types that maintain
   // transparency.  This is an index for cels with palettes or an RGB value
   // for truecolor cels.

	int getTransparentIndex()
   {
      if (frames == null) return transparentindex ;
      if (frame < 0 || frame >= frames.size()) return transparentindex ;
      GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      int [] trans = cf.getTransparentIndex() ;
      if (trans == null) return transparentindex ;
      return trans[0] ;
   }

	// Set the cel transparent index.  Propagate the change to only the
   // current frame if this cel has a local palette, otherwise propagate to
   // all global frames.

	void setTransparentIndex(int n)
	{
      super.setTransparentIndex(n) ;
      if (frames == null) return ;
      if (frame < 0 || frame >= frames.size()) return ;
      GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      if (cf.isLocalColorTable())
         cf.setTransparentIndex(n) ;
      else
      {
         for (int i = 0 ; i < frames.size() ; i++)
         {
            cf = (GifFrame) frames.elementAt(i) ;
            if (!cf.isLocalColorTable())
               cf.setTransparentIndex(n) ;
         }
      }
	}

   // Set the cel offset.  This is the relative point from the logical
	// location where the cel is drawn.  The baseoffset for the cel is
	// set for frame 0 when the cel is first loaded.

	void setOffset(int x, int y)
	{
		super.setOffset(x,y) ;
		if (frames == null) return ;
		for (int i = 0 ; i < frames.size() ; i++)
		{
			GifFrame frame = (GifFrame) frames.elementAt(i) ;
			Point frameoffset = frame.getBaseOffset() ;
			int x1 = x + (frameoffset.x - baseoffset.x) ;
			int y1 = y + (frameoffset.y - baseoffset.y) ;
			frame.setOffset(x1,y1) ;
		}
		setFrame(frame) ;
	}


   // The setNextFrame method updates the cel image to correctly display
   // the next frame in the animation sequence.  It applies the image
   // update to the current image.
   //
   // The setFrame method sets the cel image to the specified frame.  The
   // isnext setting indicates that this is a setNextFrame state.

	synchronized void setNextFrame()
   { setFrame(frame+1,true) ; }

	synchronized void setFrame(int n)
   {
   	setFrame(0,false) ;
      for (int i = 1 ; i <= n ; i++) setNextFrame() ;
   }

   // The frame argument n is the frame required.  This routine does nothing
   // if the GIF image has only one frame (GIF87).  This minimizes memory
   // requirements as we use a basic Java AWT image type and do not convert
   // to a buffered image.

	synchronized void setFrame(int n, boolean isnext)
   {
   	if (frames == null) return ;
   	if (frames.size() <= 1) return ;

      // We have a multi-frame GIF image.  It is redrawn to set up the
      // required frame.

      if (n < 0) n = 0 ;
      frame = (isnext) ? n-1 : n ;
      if (frame < 0) frame = 0 ;
		if (frame >= frames.size()) frame = 0 ;
		GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      if (cf.isError()) return ;
      float scale = (scaled) ? sf : 1.0f ;

      // Get our current image.

     	Graphics g = null ;
		Image img = getImage() ;
     	Dimension s = getSize() ;
     	int iw = (int) (s.width * scale) ;
      int ih = (int) (s.height * scale) ;
      if (img == null) return ;

      // Obtain a graphics context for our current image.  We create
      // a writable buffered image the first time we update a frame.

      if (img instanceof BufferedImage)
        	g = img.getGraphics() ;
      else
		{
			BufferedImage bufferedimage =
            new BufferedImage(iw,ih,BufferedImage.TYPE_INT_ARGB) ;
         g = bufferedimage.getGraphics() ;
         g.drawImage(img,0,0,null) ;
         filteredimage = null ;
         if (scaled)
         	img = scaledimage = bufferedimage ;
         else
         	img = image = bufferedimage ;
      }

      // If we are initializing frame 0, clear the image background.  The
      // cleared image is transparent.  Note that ARGB values of 0 are not
      // truly transparent (Java bug).

		if (!isnext && frame == 0)
      {
			int [] rgbarray = new int[iw*ih] ;
         Color c = backgroundcolor ;
         if (c == null) c = Color.black ;
         int rgb = c.getRGB() & 0xFFFFFF ;
         if (rgb == 0) rgb = 1 ;
			for (int i = 0 ; i < iw*ih ; i++) rgbarray[i] = rgb ;
			((BufferedImage) img).setRGB(0,0,iw,ih,rgbarray,0,iw) ;
      }

      // Obtain the size of the current frame.

      Dimension fs = cf.getSize() ;
		Point fp = cf.getBaseOffset() ;
      int x = (int) (fp.x * scale) ;
      int y = (int) (fp.y * scale) ;
      int w = (int) (fs.width * scale) ;
		int h = (int) (fs.height * scale) ;

      // The frame disposal setting indicates how to process the current
      // image prior to showing the next animation frame. If we restore
      // the background our default painting behaviour will draw the panel
      // background before drawing the cel.  Dispose of the pixels by making
      // them transparent.  Ensure that we do not exceed the image bounds.

      if (framedispose == 2)
      {
         int fx = x ;
         int fy = y ;
         if (fx < 0) fx = 0 ;
         if (fy < 0) fy = 0 ;
         int fw = (w+fx > iw) ? iw - fx : w ;
			int fh = (h+fy > ih) ? ih - fy : h ;
			if (fw < 0) fw = 0 ;
			if (fh < 0) fh = 0 ;
			int [] rgbarray = new int[fw*fh] ;
         Color c = backgroundcolor ;
         if (c == null) c = Color.black ;
         int rgb = c.getRGB() & 0xFFFFFF ;
         if (rgb == 0) rgb = 1 ;
         for (int i = 0 ; i < fw*fh ; i++) rgbarray[i] = 1 ;
         ((BufferedImage) img).setRGB(fx,fy,fw,fh,rgbarray,0,fw) ;
      }

      // The disposal setting requests that the image be restored to
      // its state prior to the frame being rendered.

		if (framedispose == 3)
      {
       	if (source != null) g.drawImage(source,0,0,null) ;
      }

      // Draw the next frame on top of the current image.  This behaviour
      // maps to a disposal value of 0 or 1, which leaves the graphic in
      // place.  We also set the animation parameters for the current frame.

      if (isnext) frame++ ;
      if (frame >= frames.size()) { loop++ ; frame = 0 ; }
		cf = (GifFrame) frames.elementAt(frame) ;
      if (cf.isError()) return ;
     	Image frameimage = cf.getImage() ;
      if (frameimage == null) return ;
      framedelay = cf.getDelay() ;
      framedispose = cf.getDisposal() ;

      // Obtain the size of the next frame.

      fs = cf.getSize() ;
		fp = cf.getBaseOffset() ;
      x = (int) (fp.x * scale) ;
      y = (int) (fp.y * scale) ;
      w = (int) (fs.width * scale) ;
      h = (int) (fs.height * scale) ;

      // Retain a copy of the source image if we must restore this on dispose.

      if (isnext && framedispose == 3)
      {
         source = new BufferedImage(iw,ih,BufferedImage.TYPE_INT_ARGB) ;
         Graphics g2 = source.getGraphics() ;
         g2.drawImage(img,0,0,null) ;
         g2.dispose() ;
      }

		// Update the cel image with the new frame.  The frame is drawn
		// using frame relative offset from the base cel offset.

		Point p1 = getOffset() ;
		Point p2 = cf.getOffset() ;
		x = (int) ((p2.x - p1.x) * sf) ;
		y = (int) ((p2.y - p1.y) * sf) ;
		g.setClip(x,y,w,h);
		g.drawImage(frameimage,x,y,null) ;
		g.dispose() ;

		// Set the GIF palette to the new frame palette information.   Note that
      // the palette colors, transparent and background values are object arrays.
      // Palette color changes, transparency and background changes update
      // these arrays and thus update the GIF cel frame definition.

      if (palette != null)
      {
			palette.setPalette(cf.getAlpha(),cf.getRed(),cf.getGreen(),cf.getBlue(),
				cf.getMultiPaletteCount(),cf.getColorCount(),cf.getBackgroundIndex(),
	         cf.getTransparentIndex()) ;
			transparentcolor = palette.getTransparentColor(multipalette) ;
			backgroundcolor = palette.getBackgroundColor(multipalette) ;
         cm = cf.getColorModel() ;
      }
   }


   // Reset the cel to its initial animation state.

   void reset()
   {
      super.reset() ;
   	loop = 0 ;
      setTime(0) ;
		setFrame(0) ;
	}


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

      String copyname = name ;
		Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,copyname) ;
      if (c == null)
      {
         copyname = getPath().toUpperCase() ;
         c = (Cel) Cel.getByKey(Cel.getKeyTable(),cid,copyname) ;
      }
      if (c != null && !c.isLoaded() && Cel.hasDuplicateKey(Cel.getKeyTable(),cid,copyname))
         while (c != null && !c.isLoaded()) c = (Cel) c.getNextByKey(Cel.getKeyTable(),cid,copyname) ;
		if (c != null && c.isLoaded())
      {
      	loadCopy(c) ;
			if (zip != null) ze = zip.getEntry(file) ;
         if (ze != null) ze.setCopy(copy) ;
         return ;
      }

		// Load a reference copy if we are accessing the same zip file as our
      // reference configuration.  On new data sets we may not yet have known
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
               copy = false ;
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

			// Read the entire contents.

			int n = 0, len = 0 ;
			byte b[] = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;

			// Decode the GIF file format.  Note that GIF files have palettes.
         // We construct one palette object for each new GIF file.  Each
         // frame of a multiframe GIF image is created as a separate frame
			// image.  The palette is updated each time we switch to a new frame.

         int frame = 0 ;
			frames = new Vector() ;
         while (true)
         {
				GifFrame cf = new GifFrame(this) ;
				cf.loadFrame(b,frame++,globalpalette) ;
            if (cf.isError()) break ;
            frames.addElement(cf) ;
         }

         // Set the loaded characteristics.

         try { loadFrame(0) ; }
         catch (KissException e) { error = true ; }
         if (image != null)
         {
            imagewidth = image.getWidth(null) ;
            imageheight = image.getHeight(null) ;
         }

         animate = (frames.size() > 1) ;
			loaded = true ;
		}

		// Watch for I/O errors.

		catch (IOException e)
		{
			error = true ;
         String s = e.getMessage() ;
         if (s == null) s = e.toString() ;
			showError("I/O Exception: " + s + "\n" + file) ;
//			e.printStackTrace() ;
		}

		// Watch for general exceptions.

		catch (Exception e)
		{
			error = true ;
         String s = e.getMessage() ;
         if (s == null) s = e.toString() ;
			showError("Exception: " + s + "\n" + file) ;
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
   			showError("I/O Exception: " + s + "," + file) ;
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

      // GIF images must be unique for each cel as frames update this image.

     	Dimension s = c.getBaseSize() ;
     	int iw = s.width ;
      int ih = s.height ;
		BufferedImage bufferedimage =
         new BufferedImage(iw,ih,BufferedImage.TYPE_INT_ARGB) ;
      Graphics g = bufferedimage.getGraphics() ;
      g.drawImage(image,0,0,null) ;
     	image = baseimage = bufferedimage ;
      g.dispose() ;

      // If we are copying a GIF cel, establish the frame attributes.
      // We clone frames to create unique copies for offsets.

      if (c instanceof GifCel)
      {
         GifCel gc = (GifCel) c ;
         Vector v = gc.getFrames() ;
         frames = (v != null) ? new Vector() : null ;
         if (v != null)
            for (int i = 0 ; i < v.size() ; i++)
               frames.addElement(((GifFrame) v.elementAt(i)).clone()) ;
         framedelay = gc.getInterval() ;
	      framedispose = gc.getDisposal() ;
         backgroundcolor = gc.getBackgroundColor() ;
         baseoffset = gc.getBaseOffset() ;
			maxloop = gc.getLoopLimit() ;
			palette = gc.getPalette() ;
      }

      // Set this cel's attributes from the cel copy.

      sf = 1.0f ;
      copy = true ;
      scaled = false ;
      scaledsize = null ;
      ze = c.getZipEntry() ;
		size = c.getBaseSize() ;
		bytes = c.getBytes() ;
      Point initialoffset = getInitialOffset() ;
      baseoffset = c.getBaseOffset() ;
      offset.x = baseoffset.x + initialoffset.x ;
      offset.y = baseoffset.y + initialoffset.y ;
		truecolor = c.isTruecolor() ;
      encoding = c.getEncoding() ;
      animate = c.getAnimate() ;
		cm = basecm = c.getBaseColorModel() ;
      transparentcolor = c.getTransparentColor() ;
      backgroundcolor = c.getBackgroundColor() ;
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

		// Get the required transparency for this image copy.

		palette = c.getPalette() ;
      if (pid == null) pid = c.getPaletteID() ;
      if (truecolor) pid = null ;
		if (transparency == c.getTransparency()) return ;
      changeTransparency(0) ;
		return ;
	}

   // Unload the cel file.  This releases our image allocation.

	void unload()
   {
      frame = 0 ;
		frames = new Vector() ;
      super.unload() ;
   }



   // A function to initialize this GIF cel to the specified frame.
   // Confirm that we have at least one frame.  The cel offset is set
   // to the frame base offset plus any initial offset that exists.

   void loadFrame(int n) throws KissException
   {
      frame = n ;
      if (frames.size() <= n) throw new KissException("Invalid GIF file, no frame " + frame) ;
      Point initialoffset = getInitialOffset() ;
   	GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      image = cf.getImage() ;
      size.width = cf.getScreenSize().width ;
      size.height = cf.getScreenSize().height ;
		offset.x = cf.getOffset().x ;
		offset.y = cf.getOffset().y ;
		baseoffset.x = offset.x ;
		baseoffset.y = offset.y ;
      offset.x += initialoffset.x ;
      offset.y += initialoffset.y ;
		framedelay = cf.getDelay() ;
      framedispose = cf.getDisposal() ;
      maxloop = cf.getLoopCount() ;
   	bits = 8 ;

		// Construct the GIF palette information if we have a known palette.

      if (cf.getAlpha() != null)
      {
   		palette = new Palette(zip,file) ;
   		palette.setInternal(true) ;
   		palette.setLine(getLine()) ;
   		palette.setPalette(cf.getAlpha(),cf.getRed(),cf.getGreen(),cf.getBlue(),
   			1,cf.getAlpha().length,cf.getBackgroundIndex(),cf.getTransparentIndex()) ;
   		palette.setIdentifier(this) ;
   		palette.setKey(palette.getKeyTable(),cid,palette.getIdentifier()) ;
   		palette.setKey(palette.getKeyTable(),cid,palette.getPath().toUpperCase()) ;
   		cm = basecm = palette.createColorModel(transparency,multipalette) ;
   		if (cm == null) throw new KissException("Invalid GIF file, no palette") ;
   		transparentcolor = palette.getTransparentColor(multipalette) ;
   		backgroundcolor = palette.getBackgroundColor(multipalette) ;
         setColorsUsed(palette.getColorCount()) ;
   		pid = palette.getIdentifier() ;
      }

      // Convert the image to a buffered image.  This seems to clear up
      // a restart problem where cel image copies fail to paint the first
      // time they are drawn.  The problem has been observed for JPG and
      // GIF images.

/*
      int w = size.width ;
      int h = size.height ;
      BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
      Color tc = transparentcolor ;
      if (tc == null) tc = Color.black ;
      int rgb = tc.getRGB() & 0xFFFFFF ;
      int [] rgbarray = new int[w*h] ;
      for (int i = 0 ; i < w*h ; i++) rgbarray[i] = rgb ;
      bi.setRGB(0,0,w,h,rgbarray,0,w) ;
		Graphics g = bi.createGraphics() ;
		g.drawImage(image,0,0,null) ;
		g.dispose() ;
		image = bi ;
*/
   }


   // Function to scale the base image.  Note that scaling destroys any
   // color or transparency filtering previously applied to the image.
   // This method overloads the Cel scaleImage method to ensure that
   // all frames of a GIF cel are scaled.

   void scaleImage(float scale)
   	throws KissException
   {
      super.scaleImage(scale) ;
      if (frames == null) return ;

      // Scale all image frames in this cel.

      for (int i = 0 ; i < frames.size() ; i++)
      {
			GifFrame cf = (GifFrame) frames.elementAt(i) ;
         cf.scaleImage(scale) ;
      }

      // Establish our main cel image.

      setFrame(0) ;
   }



	// Object graphics methods
	// -----------------------

	// Draw the cel at its current position, constrained by the
	// defined bounding box.  We draw the cel only if is is visible
	// and intersects our drawing area.  This method is synchronized
   // to coordinate with the frame update method.

	synchronized void draw(Graphics g, Rectangle box)
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

	void changePalette(Integer newpalette)
	{
		if (error) return ;
		if (truecolor) return ;
		if (palette == null) return ;
   	if (frames == null || frames.size() == 0) return ;
      if (frame < 0 || frame >= frames.size()) return ;
		int mp = (newpalette == null) ? 0 : newpalette.intValue() ;
      int n = palette.getMultiPaletteCount() - 1 ;
		if (mp > n) mp = n ;
      if (mp < 0) return ;
		multipalette = mp ;

      // A palette change must be made to the cel frame. Changes to the global
      // color table must be applied to all cel frames that use this table.

		GifFrame cf = (GifFrame) frames.elementAt(frame) ;
      if (cf.isLocalColorTable())
      	cf.changePalette(palette,transparency,multipalette) ;
      else
      {
      	for (int i = 0 ; i < frames.size() ; i++)
         {
         	cf = (GifFrame) frames.elementAt(i) ;
            if (cf.isLocalColorTable()) continue ;
            cf.changePalette(palette,transparency,multipalette) ;
         }
      }

      // If we have only one frame, then use the frame image.

   	if (frames.size() == 1)
      {
         Image img = cf.getImage() ;
         if (scaled) scaledimage = img ;
         else if (filteredimage != null) filteredimage = img ;
         else image = img ;
         cm = cf.getColorModel() ;
      }

      // Draw all frames.

		setFrame(frame) ;
	}


	// Method to change the cel transparency.  The change is relative
	// to the current transparency.

	void changeTransparency(int t, boolean bound, boolean ambiguous)
	{
		if (error) return ;
		if (image == null) return ;
      if (palette == null) return ;
   	if (frames == null || frames.size() == 0) return ;

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

      // A transparency change must be made to all cel frames.
/*
     	for (int i = 0 ; i < frames.size() ; i++)
      {
        	GifFrame cf = (GifFrame) frames.elementAt(i) ;
         cf.setTransparency(transparency) ;
         cf.changeTransparency(0) ;
      }

      // If we have only one frame, then use the frame image.

   	if (frames.size() == 1)
      {
        	GifFrame cf = (GifFrame) frames.elementAt(0) ;
         Image img = cf.getImage() ;
         if (scaled) scaledimage = img ;
         else if (filteredimage != null) filteredimage = img ;
         else image = img ;
      }
*/
      // Draw all frames.

		setFrame(0) ;
	}


	// Function to display a syntax error message.

	void showError(String s)
	{
   	errormessage = s ;
   	int line = getLine() ;
		if (line > 0) s = "[Line " + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
		else System.out.println(s) ;
	} 
   
   
	// GifCel clone.  We need to clone the frames as the cel clone
   // is a shallow clone.  Each frame can be modified in the cloned
   // copy and we don't want to modify the original frame.

   public Object clone()
   {
       Cel c = (Cel) super.clone() ;
       if (frames == null) return c ;
       Vector clonedframes = new Vector() ;
       for (int i= 0; i< frames.size(); i++)
       {
          GifFrame cf = (GifFrame) frames.elementAt(i) ;
          clonedframes.addElement(cf.clone()) ;
       }
       frames = clonedframes ;
       return c ;
   }

}