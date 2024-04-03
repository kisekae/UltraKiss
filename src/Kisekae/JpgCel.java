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
* JpgCel class
*
* Purpose:
*
* This class encapsulates a JPG cel object.  A cel object contains the
* cel image, a position, and a group association.  The cel also
* knows how to draw itself.
*
*/


import java.io.* ;
import java.awt.* ;
import java.awt.image.* ;
import java.util.Vector ;
import javax.swing.* ;

final class JpgCel extends Cel
{

	// Kiss cel file attributes

	private int start = 0 ;						// Start of image data
	private int bits = 0 ;						// The bits in a pixel
	private int bytes = 0 ;						// The file size in bytes


	// Constructor

	public JpgCel(ArchiveFile zip, String file, Configuration ref)
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

	String getEncoding() { return "JPEG Encoding" ; }

	// Method to write our file contents to the specified output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
      int bytes = 0 ;
		if (!isWritable()) return -1 ;
      ImageEncoder encoder = getEncoder(fw,out,type) ;
      if (encoder == null)
      	throw new IOException("unable to encode JPG file as " + type) ;
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
      // reference configuration.  On new data sets we may not yet have known
      // paths to the files.  The original cel is unloaded to release memory.

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

			// Decode the JPG file format.
			// Note that jpeg files are truecolor images.
         // We need to wait for the load to determine the image size.

			ImageIcon imageicon = new ImageIcon(b) ;
			image = imageicon.getImage() ;
			if (imageicon.getImageLoadStatus() != MediaTracker.COMPLETE)
				throw new KissException("Load failure") ;
			size.width = image.getWidth(null) ;
			size.height = image.getHeight(null) ;
         Point initialoffset = getInitialOffset() ;
         offset.x = initialoffset.x ;
         offset.y = initialoffset.y ;
			cm = basecm = Palette.getDirectColorModel() ;
			truecolor = true ;
			pid = null ;
			bits = 24 ;

         // Convert the image to a buffered image.  This seems to clear up
         // a restart  problem where cel image copies, created on a restart,
         // fail to paint the first time they are drawn.  The problem has
         // been observed for JPG and GIF images.

         int w = size.width ;
         int h = size.height ;
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

         // Apply any transparent color choice if specified.

         if (transparentindex >= 0 || transparency != 255)
         {
         	transparentcolor = new Color(transparentindex) ;
            changeTransparency(0) ;
         }

         loaded = true ;
         imagewidth = w ;
         imageheight = h ;
		}

		// Watch for I/O errors.

		catch (IOException e)
		{
			error = true ;
         String s = e.getMessage() ;
         if (s == null) s = e.toString() ;
			showError("I/O Exception, Cel " + file + ", " + s) ;
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
      Point initialoffset = getInitialOffset() ;
      baseoffset = c.getBaseOffset() ;
      offset.x = baseoffset.x + initialoffset.x ;
      offset.y = baseoffset.y + initialoffset.y ;
		cm = basecm = c.getBaseColorModel() ;
      transparentcolor = c.getTransparentColor() ;
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

		// Establish the correct transparency level.

      if (pid == null) pid = c.getPaletteID() ;
      if (truecolor) pid = null ;
      if (transparentindex >= 0 || transparency != 255)
      {
        	transparentcolor = new Color(transparentindex) ;
         changeTransparency(0) ;
      }
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
		if (kisstransparency > 255) kisstransparency = 255 ;
		if (kisstransparency < 0) kisstransparency = 0 ;
		setTransparency(255 - kisstransparency,(OptionsDialog.getAllAmbiguous() && ambiguous),this) ;

		// Identify the image to filter.
/*
		filteredimage = null ;
      Image img = getScaledImage() ;
      if (img == null) img = getBaseImage() ;
      if (img == null) return ;

		// Construct an image filter.

		PaletteFilter pf = new PaletteFilter(cm,basecm,transparency,transparentcolor) ;
		ImageProducer ip = new FilteredImageSource(img.getSource(),pf) ;
		filteredimage = Toolkit.getDefaultToolkit().createImage(ip) ;
      MediaTracker tracker = new MediaTracker(Kisekae.getKisekae()) ;
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
		if (line > 0) s = "[Line " + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
		else System.out.println(s) ;
	}
}
