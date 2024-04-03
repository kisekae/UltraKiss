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
* KissCel class
*
* Purpose:
*
* This class encapsulates a KiSS cel object.  A cel object contains the
* cel image, a position, and a group association.  The cel also
* knows how to draw itself.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.awt.Graphics2D ;
import java.util.Vector ;
import javax.swing.* ;

final class KissCel extends Cel
{

	// KiSS cel file attributes

	private int start = 0 ;						// Start of image data
	private int bits = 0 ;						// The bits in a pixel
	private int bytes = 0 ;						// The file size in bytes
	private String encoding = null ;			// The Kiss encoding type

   // Internal flag to capture missing palette faults

   private boolean nopalette = false ;		// True if no palette specified


	// Constructor

	public KissCel() { this(null,"",null) ; }
	public KissCel(ArchiveFile zip, String file, Configuration ref)
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

	String getEncoding() { return (encoding == null) ? "unknown" : encoding ; }

	// Return the associated palette.  This is referenced from the cel palette
   // integer identifier if the palette was defined when the configuration was
   // read. Otherwise we access the palette object that was defined for an
   // imported cel.  If no palette was specified we default to palette 0.

	Palette getPalette()
	{
      if (truecolor) return null ;
      Palette p = (isImported()) ? super.getPalette() : null ;
   	if (p == null) p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,pid) ;
      if (p == null) p = super.getPalette() ;
   	if (p == null) p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,new Integer(0)) ;
      return p ;
   }

   // Return the writable offset state.

   boolean isWriteableOffset() { return isUpdated() ; }
   static boolean getWriteableOffset() { return true ; }

	// Method to write our file contents to the specified output stream.
   // The encoder will identify the first transparent color in the image.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
      int bytes = 0 ;
		if (!isWritable()) return -1 ;
      ImageEncoder encoder = getEncoder(fw,out,type) ;
      if (encoder == null)	throw new IOException("unable to encode CEL file as " + type) ;
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
      // reference configuration.  The original cel is unloaded to release
      // memory resources.

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
               c.unload() ;
               copy = false ;
               if (!c.isFromInclude()) zip.addEntry(ze) ;
               zip.setUpdated(ze,c.isUpdated()) ;
	            return ;
            }
         }
		}
    
		// Nothing left now, but to read the new cel file.

		try
		{
         String includename = null ;
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
               // Try pathnames first and then just filenames.

              	ArchiveFile refzip = ref.getZipFile() ;
               if (refzip != null && !refzip.isOpen()) refzip.open() ;
               ze = (refzip != null) ? refzip.getEntry(getPath()) : null ;
               if (refzip != null && ze == null ) ze = refzip.getEntry(getPath(),true) ;
				}
			}
         
         // If we are still stuck, try just the filename.  If include files
         // were in use and a cel was edited and subsequently saved it will
         // be written to the top level archive.  This supercedes the included
         // file, but it may have been stored with directory path information.
         
			if (ze == null && includefiles != null && zip != null) 
            ze = zip.getEntry(file,true) ;         

         // If we have not yet found the file, check the INCLUDE list.

         if (ze == null)
         {
            ze = searchIncludeList(includefiles,name) ;
            if (ze != null)
            {
               zip = ze.getZipFile() ;
               includename = (zip != null) ? zip.getFileName() : null ;
               setFromInclude(true) ;
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
            
         // Watch for improperly formatted Cel files.
            
         if (bytes < 16)
            throw new KissException("is not a valid KiSS file.") ;

			// Decode the KiSS cel file format.

         String s1 = new String(b,0,4) ;
			if ("KiSS".equals(s1))
			{
				encoding = "KiSS version 1" ;
				size.width = fixByte(b[9],b[8]) ;
				size.height = fixByte(b[11],b[10]) ;
            Point initialoffset = getInitialOffset() ;
				offset.x = fixByte(b[13],b[12]) ;
				offset.y = fixByte(b[15],b[14]) ;
   			baseoffset.x = offset.x ;
   			baseoffset.y = offset.y ;
            offset.x += initialoffset.x ;
            offset.y += initialoffset.y ;
				bits = fixByte((byte) 0,b[5]) ;
				start = 32 ;
			}
			else
			{
				encoding = "KiSS version 0" ;
				size.width = fixByte(b[1],b[0]) ;
				size.height = fixByte(b[3],b[2]) ;
				offset.x = offset.y = 0 ;
            baseoffset.x = baseoffset.y = 0 ;
				bits = 4 ;
				start = 4 ;
            
            // Watch for improperly formatted Cel files.
            
            if (size.width > 2000 || size.height > 2000)
               throw new KissException("is not a valid KiSS cel.") ;
			}
         

			// Create the image object for this cel.  True color images
			// use an integer array to store the data, all other images
			// use a byte array.

			int pixel = 0 ;					// The buffer index
			byte cel[] = null ;				// The cel byte buffer
			int tcel[] = null ;				// The cel truecolor buffer
			int w = (bits == 4) ? ((size.width+1)/2*2) : size.width ;
			int h = size.height ;
			truecolor = (bits == 32) ;
			if (truecolor)
         	tcel = new int[w*h] ;
         else
         	cel = new byte[w*h] ;

			// Unpack bytes from image file.  4 bit pixels map to 16
			// color palettes.  8 bit pixels map to 256 color palettes.
			// 32 bit pixels are true color files which do not use a
			// palette.

			for (int i = start ; i < bytes ; )
			{
				if (!truecolor && bits == 4)
				{
					cel[pixel++] = (byte) ((b[i] >> 4) & 15) ;
					cel[pixel++] = (byte) (b[i++] & 15) ;
				}
				else if (!truecolor && bits == 8)
				{
					cel[pixel++] = b[i++] ;
				}
				else if (truecolor)
				{
					if ((i+3) >= bytes) break ;
					int b1 = b[i++] & 255 ;			// Blue
					int b2 = b[i++] & 255 ;			// Green
					int b3 = b[i++] & 255 ;			// Red
					int b4 = b[i++] & 255 ;			// Alpha
					tcel[pixel++] = (b4<<24) + (b3<<16) + (b2<<8) + b1 ;
				}
			}

			// Create a base color model and an image for this cel.  If the
         // palette does not exist then a direct color model is created.

			if (truecolor)
			{
				cm = basecm = Palette.getDirectColorModel() ;
				m = new MemoryImageSource(w,h,cm,tcel,0,w) ;
				image = Toolkit.getDefaultToolkit().createImage(m) ;
         
           // Kludge for Apple systems.
           // Convert the image to an ARGB buffered image.
/*         
           if (OptionsDialog.getAppleMac()) 
           {
               BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB_PRE) ;
               bi.setRGB(0,0,w,h,tcel,0,w) ;
               image = bi ;
            }
*/            
            if (transparency != 255) changeTransparency(0) ;
            pid = null ;
			}

         // Palette type cels use an index color model.  If no palette was
         // specified and this is a palette cel then we would default to
         // Palette file 0, which must be our initial palette.

			if (!truecolor)
			{
				Palette p = getPalette() ;
				if (p != null)
				{
			      Object mpid = getPaletteGroupID() ;
			      if (mpid instanceof Integer) multipalette = ((Integer) mpid).intValue() ;
					cm = basecm = p.createColorModel(transparency,multipalette) ;
					m = new MemoryImageSource(w,h,cm,cel,0,w) ;
	 				image = Toolkit.getDefaultToolkit().createImage(m) ;
               transparentcolor = p.getTransparentColor(multipalette) ;
   				backgroundcolor = p.getBackgroundColor(multipalette) ;
               setColorsUsed(p.getColorCount()) ;
               setPaletteID(p.getIdentifier()) ;
               if (getInitPaletteID() == null) setInitPaletteID(p.getIdentifier()) ;
               transparentindex = 0 ;
               background = 0 ;
         
              // Kludge for Apple systems.
              // Convert the image to an ARGB buffered image.
/*         
              if (OptionsDialog.getAppleMac()) 
              {
                  int [] rgbarray = new int[w*h] ;
                  BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB_PRE) ;
                  for (int i = 0 ; i < w*h ; i++) 
                  {
                     int j = ((int) cel[i]) & 0xff ;
                     rgbarray[i] = cm.getRGB(j) ; 
                  }
                  bi.setRGB(0,0,w,h,rgbarray,0,w) ;
                  image = bi ;
               }
*/
            }
			   else
            {
            	nopalette = true ;
   				throw new KissException("invalid palette") ;
            }
			}
         
         // If we are a 4 bit 16 color cel with an odd width then we need to
         // crop our image as it was constructed 1 pixel too wide.
         
         if (bits == 4 && size.width == w-1)
         {
            size.width = w = w-1 ;
            if (image instanceof BufferedImage)
               image = ((BufferedImage) image).getSubimage(0,0,w,h) ;
            else
            {
               ImageProducer ip = image.getSource() ;
               ip = new FilteredImageSource(ip, new CropImageFilter(0,0,w,h)) ;
               image = Toolkit.getDefaultToolkit().createImage(ip) ;
               MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
               tracker.addImage(image,0) ;
               try { tracker.waitForAll(500) ; }
               catch (InterruptedException e) { }
            }
         }

			// Check for a valid image.

			if (image == null) throw new KissException("unsupported graphics format") ;
         baseimage = image ;
         imagewidth = w ;
         imageheight = h ;
         loaded = true ;
		}

		// Watch for KiSS errors.

		catch (KissException e)
		{
			error = true ;
			String s = e.getMessage() ;
			if (s == null) s = e.toString() ;
			showError("KiSS Exception, Cel " + file + ", " + s) ;
		}

		// Watch for I/O errors.

		catch (IOException e)
		{
			error = true ;
			String s = e.getMessage() ;
			if (s == null) s = e.toString() ;
			showError("I/O Exception, Cel " + file + ", " + s) ;
		}

		// Watch for file size errors.

		catch (StringIndexOutOfBoundsException e)
		{
			error = true ;
			showError("Cel " + file + " is not a valid KiSS cel.") ;
         int calcsize = size.width * size.height ;
         if (bits == 4) calcsize = (calcsize + 1) / 2 ;
			System.out.println("Size Exception, Cel " + file
         	+ ", Width: " + size.width + ", Height: " + size.height
				+ ", Bits: " + bits + ", Expected size: " + calcsize
            + ", Actual size: " + (bytes-start)) ;
		}

		// Watch for file size errors.

		catch (ArrayIndexOutOfBoundsException e)
		{
			error = true ;
			showError("Cel " + file + " is not a valid KiSS cel.") ;
         int calcsize = size.width * size.height ;
         if (bits == 4) calcsize = (calcsize + 1) / 2 ;
			System.out.println("Size Exception, Cel " + file
         	+ ", Width: " + size.width + ", Height: " + size.height
				+ ", Bits: " + bits + ", Expected size: " + calcsize
            + ", Actual size: " + (bytes-start)) ;
		}

		// Watch for general KiSS exceptions.

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
      Point initialoffset = getInitialOffset() ;
      baseoffset = c.getBaseOffset() ;
      offset.x = baseoffset.x + initialoffset.x ;
      offset.y = baseoffset.y + initialoffset.y ;
		truecolor = c.isTruecolor() ;
      encoding = c.getEncoding() ;
		cm = basecm = c.getBaseColorModel() ;
      transparentcolor = c.getTransparentColor() ;
      backgroundcolor = c.getBackgroundColor() ;
      transparentindex = c.getTransparentIndex() ;
      background = c.getBackgroundIndex() ;
      setLastModified(c.lastModified()) ;
      setUpdated(c.isUpdated()) ;
      setFromInclude(c.isFromInclude()) ;

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

   	Palette p = getPalette() ;
      if (pid == null) pid = c.getPaletteID() ;
      if (initpid == null) initpid = c.getInitPaletteID() ;
      if (truecolor) pid = null ;
		if (p != null) setColorsUsed(p.getColorCount()) ;
		if (pid != null && pid.equals(c.getPaletteID()) &&
			transparency == c.getTransparency() &&
         mpid == c.getPaletteGroupID()) return ;
      if (pid == null && transparency == c.getInitTransparency()) return ;

		// Palette cels require a new color model.  Truecolor cels use the
      // same color model with a new transparency.

		if (!truecolor)
		{
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
      MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
      tracker.addImage(filteredimage,0) ;
      try { tracker.waitForAll(500) ; }
      catch (InterruptedException e) { }
		return ;
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

      // Draw the cel.  A selection box is drawn if the cel is selected.

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
            if ((clip.x+clip.width < cx+w) || (clip.y+clip.height < cy+h))
            {
               int cw = clip.x+clip.width-cx ;
               int ch = clip.y+clip.height-cy ;
               cw = Math.min(cw,img.getWidth(null)) ;
               ch = Math.min(ch,img.getHeight(null)) ;
               if (cw < 0) cw = 0 ;
               if (ch < 0) ch = 0 ;
               if (img instanceof BufferedImage)
               {
                  img = ((BufferedImage) img).getSubimage(0,0,cw,ch) ;
               }
               else
               {
                  CropImageFilter f = new CropImageFilter(0,0,cw,ch);
                  img = Toolkit.getDefaultToolkit().createImage(
                     new FilteredImageSource(img.getSource(),f)) ;
                  MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
                  tracker.addImage(img,0) ;
                  try { tracker.waitForAll(500) ; }
                  catch (InterruptedException e) { }
               }
            }
         }
         catch (Exception ae) { }
      }
*/     
      if (img != null) gc.drawImage(img,cx,cy,null) ;
		gc.dispose() ;
	}


	// Method to change the cel palette file.

	void changePaletteID(Integer n)
   {
   	Palette p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,n) ;
   	if (p == null) return ;
      pid = n ;
      changePalette(new Integer(multipalette)) ;
   }


	// Method to add a filter to the cel image for multipalette changes.
	// The base image was initially constructed for a multipalette of
	// zero. If we change the image to reference a different multipalette
	// then this will cause the cel to be drawn in a different color.
   // A color filter is created only if necessary.  Note that truecolor
   // cels cannot change their palette and cels with a specific multipalette
   // cannot change to a new multipalette.

	void changePalette(Integer newmultipalette)
	{
		if (error) return ;
		if (truecolor) return ;
		Palette p = getPalette() ;
		if (p == null) return ;
      Object mpid = getPaletteGroupID() ;
      if (mpid != null && !mpid.equals(newmultipalette)) return ;
		int mp = (newmultipalette == null) ? 0 : newmultipalette.intValue() ;
      int n = p.getMultiPaletteCount() - 1 ;
		if (mp > n) mp = n ;
      if (mp < 0) return ;

		// Change the palette.  Make the change only if the requested
		// color model actually exists in the cel palette file.

		multipalette = mp ;
		ColorModel newcm = p.createColorModel(transparency,multipalette) ;
		if (newcm != cm)
		{
			cm = newcm ;
         Image img = getScaledImage() ;
         if (img == null) img = getBaseImage() ;
         if (img == null) return ;

			// Construct an image filter.

         transparentcolor = p.getTransparentColor(multipalette) ;
			ImageProducer base = img.getSource() ;
			ImageProducer ip = new FilteredImageSource(base,
         	new PaletteFilter(cm,basecm,transparency,transparentcolor));
			filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
         MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
         tracker.addImage(filteredimage,0) ;
         try { tracker.waitForAll(500) ; }
         catch (InterruptedException e) { }
		}
	}


	// Method to change the cel transparency.  The change is relative
	// to the current transparency.  We create a new color model for
	// palette cels.  The relative change uses the KiSS model of transparency.
   // Positive values make the object more transparent.  Negative values make
   // the object more opaque.

	void changeTransparency(int t, boolean bounded, boolean ambiguous)
	{
		if (error) return ;

      // Limit transparency between 0 and 255 if this is a bounded change.

      int adjust = t ;
      if (bounded)
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
		if (image == null) return ;

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
			Palette p = getPalette() ;
			if (p == null) return ;
			cm = p.createColorModel(transparency,multipalette) ;
      }

		// Construct an image filter.
      
		PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor) ;
		ImageProducer ip = new FilteredImageSource(img.getSource(),pf) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
      MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
      tracker.addImage(filteredimage,0) ;
      try { tracker.waitForAll(500) ; }
      catch (InterruptedException e) { }
/*
		// This is approximately twice as fast.

      int w = img.getWidth(null) ;
      int h = img.getHeight(null) ;
      BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB) ;
      AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC,transparency/255.0f) ;
      Graphics2D gc = (Graphics2D) bi.getGraphics() ;
      gc.setComposite(ac) ;
      gc.drawImage(img,0,0,null) ;
      filteredimage = bi ;
*/
	}


	// Function to display a syntax error message.

	void showError(String s)
	{
   	errormessage = s ;
   	int line = getLine() ;
		if (line > 0) s = "[Line " + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
		else if (!nopalette) System.out.println(s) ;
	}
}
