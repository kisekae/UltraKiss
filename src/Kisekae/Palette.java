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
* Palette class
*
* Purpose:
*
* This class encapsulates a palette object.  A palette defines a color
* mapping for an image.  Each pixel of an image file is an index entry
* into a palette.
*
* Kisekae provides support for multiple palette files and multipalette files.
* More than one palette file can exist in a data set.  Each image can have
* its own palette file.   Alternatively, different palettes can be stacked in
* a multipalette file.  Images can reference a multipalette by using a palette
* offset into a single palette file.
*
* Palette objects use both their identifier number and their file name as
* their access key.
*
*/

import java.io.* ;
import java.awt.image.* ;
import java.awt.Color ;
import java.awt.Point ;
import java.awt.Rectangle ;
import java.util.Date ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.StringTokenizer ;
import java.util.Vector ;
import java.util.zip.* ;

final class Palette extends KissObject
{
	// Class attributes.  Sized for 16 palette objects.

	static private Hashtable key = new Hashtable(20,0.8f) ;
   static private ColorModel dcm = /* bits,red,green,blue,alpha */
		new DirectColorModel(32,255<<16,255<<8,255<<0,255<<24) ;


	// Palette attributes

	private String encoding = null ;		// The KiSS encoding type
	private String comment = null ;		// The palette comment text
	private String importfile = null ;	// The name of the import text file
	private int bytes = 0 ;					// The file size in bytes
	private int colors = 0 ;				// The number of colors in one palette
	private int offset = 0 ;				// The offset to data in palette
	private int bits = 0 ;					// The number of bits per color
	private int groups = 0 ;				// The number of multipalettes
   private int usedcolors = -1 ;			// The count of colors used by the image
	private int byteswritten = 0 ;  		// The number of bytes written
	private int size = 0 ;					// The entries in the color arrays
	private int line = 0 ;					// The configuration file line
   private int transparency = 255 ;    // The palette transparency

	// Color model vectors

	private Hashtable cmkey = null ;		// Index into color models
	private byte [] red = null ;			// The red colors
	private byte [] blue = null ;			// The blue colors
	private byte [] green = null ;		// The green colors
	private byte [] alpha = null ;		// The transparency
	private byte [] editred = null ;		// The backup red colors
	private byte [] editblue = null ;	// The backup blue colors
	private byte [] editgreen = null ;	// The backup green colors
	private byte [] editalpha = null ;	// The backup transparency
	private byte [] encodered = null ;	// The red colors for output
	private byte [] encodeblue = null ;	// The blue colors for output
	private byte [] encodegreen = null ; // The green colors for output
	private byte [] encodealpha = null ; // The transparency for output
   private int [] background = null ;	// The index of the background color
   private int [] transparent = null ;	// The index of the transparent color
   private int [] editbackground = null ;	// The backup background color index
   private int [] edittransparent = null ;  // The backup transparent color index
   private int editsize = 0 ;				// The backup size
	private int editgroups = 0 ;			// The backup group count
	private int editbytes = 0 ;			// The backup byte length
	private int editbits = 0 ;				// The backup bit length
	private int editcolors = 0 ;			// The backup number of colors
	private int encodecolors = 0 ;		// The encoded colors

	// State attributes

	private boolean loaded = false ;		// True if data has been loaded
	private boolean frominclude = false ;	// True if data from include file
	private boolean copy = false ;		// True if file is a copy
	private Vector visible = null ;		// List of palette group visible


	// Constructor

	public Palette() { this(null,null,null) ; }
	public Palette(ArchiveFile zip, String file) { this(zip,file,null) ; }
	public Palette(ArchiveFile zip, String file, Configuration ref)
	{
      setUniqueID(new Integer(this.hashCode())) ;
		setZipFile(zip) ;
		this.ref = ref ;
		this.file = convertSeparator(file) ;
		cmkey = new Hashtable(256,0.8f) ;

		// Decode the palette type based upon the source file type.

		if (file == null)
			extension = ".kcf" ;
		else
		{
			int n = file.lastIndexOf('.') ;
			extension = (n < 0) ? "" : file.substring(n).toLowerCase() ;
		}
	}


	// Class methods
	// -------------

	static Hashtable getKeyTable() { return key ; }

	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// configuration identifier.

	static void clearTable(Object cid)
	{
      if (cid == null) cid = new String("Unknown") ;
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}

	// A convenience method to return a direct color model.

	static ColorModel getDirectColorModel() { return dcm ; }


	// Object state reference methods
	// ------------------------------

	// Method to return the active palette data.

	Object [] getPaletteData()
	{
		Object [] o = new Object [4] ;
		o[0] = alpha ;
		o[1] = red ;
		o[2] = green ;
		o[3] = blue ;
		return o ;
	}

	// Method to return the backup palette data.

	Object [] getEditPaletteData()
	{
		Object [] o = new Object [4] ;
		o[0] = editalpha ;
		o[1] = editred ;
		o[2] = editgreen ;
		o[3] = editblue ;
		return o ;
	}

	// Method to return the active palette background and transparency.

	Object [] getPaletteTransBack()
	{
		Object [] o = new Object [2] ;
		o[0] = transparent ;
		o[1] = background ;
		return o ;
	}

	// Method to return the active palette background and transparency.

	Object [] getPaletteEditTransBack()
	{
		Object [] o = new Object [2] ;
		o[0] = edittransparent ;
		o[1] = editbackground ;
		return o ;
	}

	// Method to return the multipalette data for a specified multipalette.

	Object [] getMultiPaletteData(Integer multipalette)
	{
		Object [] o = new Object [4] ;
		byte [] a = new byte [colors] ;
		byte [] r = new byte [colors] ;
		byte [] g = new byte [colors] ;
		byte [] b = new byte [colors] ;
		int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
		for (int i = 0 ; i < colors ; i++)
		{
			int n = (mp * colors) + i ;
			if (n > size || n < 0) break ;
			a[i] = alpha[n] ;
			r[i] = red[n] ;
			g[i] = green[n] ;
			b[i] = blue[n] ;
		}
		o[0] = a ;
		o[1] = r ;
		o[2] = g ;
		o[3] = b ;
		return o ;
	}

	// Method to return the palette file size.

	int getBytes() { return bytes ; }
	int getEditBytes() { return editbytes ; }

	// Method to return the number of multipalettes in this file.

	int getMultiPaletteCount() { return groups ; }
	int getEditMultiPaletteCount() { return editgroups ; }

	// Method to return the number of colors in this palette.

	int getColorCount() { return colors ; }
	int getEditColorCount() { return editcolors ; }

	// Method to return the number of bits defined for a color.

	int getBits() { return bits ; }
	int getEditBits() { return editbits ; }

	// Method to return the index of the background color in the palette.

	int getBackgroundIndex()
   { return (background == null) ? -1 : background[0] ; }
	int getEditBackgroundIndex()
   { return (editbackground == null) ? -1 : editbackground[0] ; }

	// Method to return the index of the transparent color in the palette.

	int getTransparentIndex()
   { return (transparent == null) ? -1 : transparent[0] ; }
	int getEditTransparentIndex()
   { return (edittransparent == null) ? -1 : edittransparent[0] ; }

	// Method to return the transparency of the palette.

	int getTransparency() { return transparency ; }

	// Method to return the count of colors used by the associated image.
   // This attribute is valid only for internal palettes associated with
   // one image.

	int getUsedColors() { return usedcolors ; }

	// Return the palette encoding.

	String getEncoding() { return (encoding == null) ? "unknown" : encoding ; }

	// Method to set the index of the background color in the palette.

	void setBackgroundIndex(int b)
   {
      if (background == null) background = new int[1] ;
   	background[0] = b ;
      setBackgroundIndex(background) ;
	}

	// Method to set the index of the background color in the palette.

	void setBackgroundIndex(int [] b)
   {
   	background = b ;
		invalidate() ;
      setUpdated(true) ;
	}

	// Method to set the index of the background color in the palette.

	void setBackgroundColor(Color c)
   {
      setBackgroundIndex(getColorIndex(c)) ;
	}

	// Method to set the index of the transparent color in the palette.

	void setTransparentIndex(int t)
   {
      if (transparent == null) transparent = new int[1] ;
   	transparent[0] = t ;
      setTransparentIndex(transparent) ;
	}

	// Method to set the index of the transparent color in the palette.

	void setTransparentIndex(int [] t)
   {
   	transparent = t ;
		invalidate() ;
		setUpdated(true) ;
	}

	// Method to set the index of the transparent color in the palette.

	void setTransparentColor(Color c)
   {
      setTransparentIndex(getColorIndex(c)) ;
	}

	// Method to set the transparency of the palette.

	void setTransparency(int n) { transparency = n ; }

	// The configuration file line number showing where this object was
	// first declared is used for diagnostic output messages.

	void setLine(int l) { if (line == 0) line = l ; }

   // Set the loaded state.  

   void setLoaded(boolean b) { loaded = b ; }

   // Indicate if loaded from an include file.  

   void setFromInclude(boolean b) { frominclude = b ; }

	// Set the number of colors actually used in the image that references
   // this palette.  This attribute is only valid for internal palettes that
   // are associated with a single image.

	void setUsedColors(int n) { usedcolors = n ; }

	// Set the palette group visibility attribute.  Invisible multipalettes
	// are disabled on the palette group toolbar.

	void setVisible(boolean b, int mp)
   {
      if (visible == null) visible = new Vector() ;
      int size = visible.size() ;
      if (mp >= size) visible.setSize(mp+1) ;
   	visible.setElementAt(new Boolean(b),mp) ;
   }

	// Return the palette image copy indicator.

	boolean isCopy() { return copy ; }

	// Return an indication if the data has been loaded.

	boolean isLoaded() { return (loaded || copy) ; }

   // Return an indication if data was loaded from an include file.

   boolean isFromInclude() { return frominclude ; }

	// Return an indication if the multipalette is visible.

	boolean isVisible(int mp)
   {
      if (mp < 0) return false ;
      if (visible == null) return true ;
      if (mp >= visible.size()) return true ;
      Object o = visible.elementAt(mp) ;
      if (o instanceof Boolean) return ((Boolean) o).booleanValue() ;
      return true ;
   }

	// Return a writable file indicator.

	boolean isWritable() { return (ArchiveFile.hasPalette(extension)) ; }

	// Method to write the palette data in the correct format to the
   // specified output stream.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{
		if (error) return -1 ;
		if (!isWritable()) return 0 ;
      createEncodeArrays() ;
		if (type == null) type = extension ;
      if (type != null) type = type.toLowerCase() ;
		if (".pal".equals(type)) return exporttext(fw,out) ;
		if (".kcf".equals(type))return encode(fw,out) ;
     	throw new IOException("unable to encode KiSS palette file as " + type) ;
	}

	// Method to export the palette data in text format to the
   // specified output stream.

	int export(FileWriter fw, OutputStream out, String type) throws IOException
	{
		if (error) return -1 ;
		if (!isWritable()) return 0 ;
      createEncodeArrays() ;
		return exporttext(fw,out) ;
	}


   // Perform any transparent color location adjustment.  A KCF must have
   // the transparent color at index 0. The encoding arrays are set by
   // the KissEncoder when an image is written, and if they have been set
   // this routine does nothing.

   private void createEncodeArrays()
   {
      // If the encoding arrays have been set previously, then exit.  This
      // can happen if a cel is saved from a different image format and the
      // palette was rebuilt through dithering.
      
      if (encodered != null) return ;

      // If we have a transparent color, ensure that it is at index 0.

      if (transparent != null && transparent[0] > 0)
      {
         encodecolors = colors ;
         encodered = new byte[encodecolors*groups] ;
         encodegreen = new byte[encodecolors*groups] ;
         encodeblue = new byte[encodecolors*groups] ;
         encodealpha = new byte[encodecolors*groups] ;
         for (int i = 0 ; i < encodecolors*groups ; i++)
         {
            encodered[i] = red[i] ;
            encodegreen[i] = green[i] ;
            encodeblue[i] = blue[i] ;
            encodealpha[i] = alpha[i] ;
         }

         // Swap the transparent color in the encoding arrays.

         for (int j = 0 ; j < groups ; j++)
         {
            int offset = j * groups ;
            int trans = transparent[0] + offset ;
            byte temp = encodered[offset] ; encodered[offset] = encodered[trans] ; encodered[trans] = temp ;
            temp = encodegreen[offset] ; encodegreen[offset] = encodegreen[trans] ; encodegreen[trans] = temp ;
            temp = encodeblue[offset] ; encodeblue[offset] = encodeblue[trans] ; encodeblue[trans] = temp ;
         }
      }

      // Palettes with no transparency must reserve index 0 if possible.

      else if ((transparent == null || transparent[0] < 0)
               && (colors < 256 && colors > 1))
      {
         encodecolors = colors + 1 ;
         encodered = new byte[encodecolors*groups] ;
         encodegreen = new byte[encodecolors*groups] ;
         encodeblue = new byte[encodecolors*groups] ;
         encodealpha = new byte[encodecolors*groups] ;

         // Reserve space for the transparent color in the encoding arrays.

         for (int j = 0 ; j < groups ; j++)
         {
            int offset = j * colors ;
            byte [] temp = new byte[encodecolors] ;
            for (int i = encodecolors-1 ; i > 0 ; i--) temp[i] = red[offset+i-1] ;
            for (int i = offset ; i < offset+encodecolors ; i++) encodered[i] = temp[i-offset] ;
            temp = new byte[encodecolors] ;
            for (int i = encodecolors-1 ; i > 0 ; i--) temp[i] = green[offset+i-1] ;
            for (int i = offset ; i < offset+encodecolors ; i++) encodegreen[i] = temp[i-offset] ;
            temp = new byte[encodecolors] ;
            for (int i = encodecolors-1 ; i > 0 ; i--) temp[i] = blue[offset+i-1] ;
            for (int i = offset ; i < offset+encodecolors ; i++) encodeblue[i] = temp[i-offset] ;
         }
      }

      // For valid transparency or if we cannot transform the arrays, use
      // the current palette as the default.

      else
      {
         encodecolors = colors ;
         encodered = red ;
         encodegreen = green ;
         encodeblue = blue ;
         encodealpha = alpha ;
      }
   }


   // Set the palette output encoding arrays.  A KCF must have the
   // transparent color at index 0. The KissEncoder establishes the
   // appropriate transformation during image output.

   void setEncodeArrays(byte [] red, byte[] green, byte [] blue)
   {
      encodecolors = (red == null) ? 0 : red.length ;
      encodered = red ;
      encodegreen = green ;
      encodeblue = blue ;
      encodealpha = (red == null) ? null : new byte[encodecolors] ;
   }


   // Palette file Edit control methods
   // ---------------------------------

	// Method to capture a backup copy of the palette before editing.

	void startEdits()
	{
		editalpha = (alpha != null) ? (byte []) alpha.clone() : null ;
		editred = (red != null) ? (byte []) red.clone() : null ;
		editblue = (blue != null) ? (byte []) blue.clone() : null ;
		editgreen = (green != null) ? (byte []) green.clone() : null ;
      editbackground = (background != null) ? (int []) background.clone() : null ;
      edittransparent = (transparent != null) ? (int []) transparent.clone() : null ;
      editsize = size ;
		editgroups = groups ;
		editbytes = bytes ;
		editbits = bits ;
		editcolors = colors ;
	}

	// Method to release the backup copy of the palette after editing.

	void stopEdits()
	{
   	editalpha = null ;
		editred = null ;
		editblue = null ;
		editgreen = null ;
      editbackground = null ;
      edittransparent = null ;
      editsize = 0 ;
      editgroups = 0 ;
		editbytes = 0 ;
		editbits = 0 ;
		editcolors = 0 ;
	}

	// Method to restore the backup copy of the palette during edits.
   // Note that all color models for this palette are invalidated.

	void undoEdits()
	{
		invalidate() ;
		if (editalpha != null) alpha = (byte []) editalpha.clone() ;
		if (editred != null) red = (byte []) editred.clone() ;
		if (editblue != null) blue = (byte []) editblue.clone() ;
		if (editgreen != null) green = (byte []) editgreen.clone() ;
      if (editred != null)
      {
	      size = editsize ;
	      groups = editgroups ;
			bytes = editbytes ;
			bits = editbits ;
			colors = editcolors ;
         background = editbackground ;
         transparent = edittransparent ;
      }
	}



	// Object loading methods
	// ----------------------

	// Method to read a KiSS KCF palette file.  This method decodes the
	// structure of this object's KCF file in compressed form.

	void load(Vector includefiles)
	{
		if (isLoaded()) return ;
		InputStream is = null ;
		byte [] b = null ;
      String name = getRelativeName() ;
      if (name != null) name = name.toUpperCase() ;

		// Load the file if another copy of the palette has not already been
		// loaded.  If we have previously read the file use the prior
		// palette.  Set the archive entry to reference an object copy.

		Palette p = (Palette) Palette.getByKey(Palette.getKeyTable(),cid,name) ;
		if (p != null && p.isLoaded())
      {
      	loadCopy(p) ;
         copy = true ;
			if (zip != null) ze = zip.getEntry(file) ;
         if (ze != null) ze.setCopy(copy) ;
         return ;
      }

		// Load a reference copy if we are accessing the same zip file as our
      // reference configuration.  On new data sets we may not yet have known
      // paths to the files.

		if (ref != null && zip != null)
		{
      	ArchiveFile refzip = ref.getZipFile() ;
         String refpath = (refzip != null) ? refzip.getName() : null ;
         String zippath = zip.getName() ;
         if ((refpath == null && zippath == null) ||
         	(refpath != null && zippath != null && refpath.equals(zippath)))
         {
				p = (Palette) Palette.getByKey(Palette.getKeyTable(),ref.getID(),name) ;
				if (p != null && p.isLoaded())
	         {
	         	loadCopy(p) ;
               if (!p.isFromInclude()) zip.addEntry(ze) ;
               zip.setUpdated(ze,p.isUpdated()) ;
	            return ;
            }
         }
		}

		// Read the new palette file.

		try
		{
			if (!isLoaded())
			{
				if (zip != null) ze = zip.getEntry(file) ;

				// Load a reference copy if it exists.

				if (ze == null)
				{
					if (ref != null)
					{
						p = (Palette) Palette.getByKey(Palette.getKeyTable(),ref.getID(),name) ;
						if (p != null && p.isLoaded())
                  {
                     loadCopy(p) ;
                     return ;
                  }

         			// Load an unloaded copy if it exists in the reference file.

                 	ArchiveFile refzip = ref.getZipFile() ;
                  if (refzip != null && !refzip.isOpen()) refzip.open() ;
                  ze = (refzip != null) ? refzip.getEntry(getPath()) : null ;
					}
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
               setFromInclude(true) ;
            }
         }

			// Determine the uncompressed file size and load the file.
         // The file may be a .KCF compressed file or a .PAL text file.

			if (!isLoaded() && ".kcf".equals(extension))
			{
				bytes = (ze == null) ? 0 : (int) ze.getSize() ;
				if (loader != null)
            {
               String s = Kisekae.getCaptions().getString("FileNameText") ;
               int i1 = s.indexOf('[') ;
               int j1 = s.indexOf(']') ;
               if (i1 >= 0 && j1 > i1)
                  s = s.substring(0, i1) + file + s.substring(j1 + 1) ;
               loader.showFile(s + " [" + bytes + " bytes]" +
                  ((includename != null) ? (" (" + includename + ")") : "")) ;
            }

				// Create the file input stream.

				is = (zip == null) ? null : zip.getInputStream(ze) ;
				if (is == null) throw new IOException("file not found") ;

				// Read and decode the entire contents.

				int n = 0, len = 0 ;
				b = new byte[bytes] ;
				while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;
            decode(b) ;
			}

         // We may be loading a .PAL text file.

			if (!isLoaded()) loadText(ze) ;

			// Check for a valid palette load.

			if (red == null || green == null || blue == null)
				throw new KissException("Invalid palette format, encoding="+encoding+
                    " colors="+colors+" groups="+groups+" bits="+bits+" offset="+offset) ;
		}

		// Watch for file size errors.

		catch (StringIndexOutOfBoundsException ex)
		{
			error = true ;
			showError("Palette " + file + " is not valid, string exception.") ;
			PrintLn.println("Palette String Exception, " + file + " size = " + bytes) ;
		}

		// Watch for file size errors.

		catch (ArrayIndexOutOfBoundsException ex)
		{
			error = true ;
			showError("Palette " + file + " is not valid, size exception.") ;
			PrintLn.println("Palette Size Exception, " + file + " size = " + bytes) ;
		}

		// Watch for I/O errors

		catch (IOException ex)
		{
			error = true ;
			showError("I/O Exception, Palette " + file + ", " + ex.getMessage()) ;
		}

		// Watch for general KiSS exceptions.

		catch (Exception e)
		{
			error = true ;
			showError("Exception, Palette " + file + ", " + e.getMessage()) ;
         e.printStackTrace() ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; }
			catch (IOException ex)
			{
				error = true ;
				showError("I/O Exception, Palette " + file + ", " + ex.getMessage()) ;
            ex.printStackTrace() ;
			}
			is = null ;
		}
	}


	// Load a new copy of the palette data from the specified palette.
	// This method retains the necessary attributes of the original palette.

	private void loadCopy(Palette p)
	{
		if (p == null) return ;
		error = p.isError() ;
   	loaded = !error ;
      if (error) return ;
      ze = p.getZipEntry() ;
		Object [] o = p.getPaletteData() ;
      alpha = (byte []) o[0] ;
      red = (byte []) o[1] ;
      green = (byte []) o[2] ;
      blue = (byte []) o[3] ;
      size = alpha.length ;
		bytes = p.getBytes() ;
      colors = p.getColorCount() ;
      bits = p.getBits() ;
      o = p.getPaletteTransBack() ;
      groups = p.getMultiPaletteCount() ;
      background = (int []) o[1] ;
      transparent = (int []) o[0] ;
      setLastModified(p.lastModified()) ;
      setFromInclude(p.isFromInclude()) ;
		if (loader != null)
      {
         String s1 = Kisekae.getCaptions().getString("FileNameText") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + file + s1.substring(j1+1) ;
         loader.showFile(s1 + " [" + bytes + " bytes] (copy)") ;
      }
	}


	// Method to load a KiSS palette file into this object.  This method reads
	// the structure of a PAL file in text form and constructs the palette.
	// See the export method that writes text palettes.   This load is
	// destructive.  All color models for the palette are invalidated.

	void loadText(ArchiveEntry ze)
	{
   	String s = null ;
		StringTokenizer st = null ;
		String ls = System.getProperty("line.separator") ;

		InputStream is = null ;
		byte [] b = null ;
		alpha = null ;
      size = 0 ;
      bits = 0 ;
      groups = 0 ;
      colors = 0 ;
      line = 0 ;

		// Read the new palette file.

		try
		{
			if (ze == null) return ;
			is = ze.getInputStream() ;
			importfile = ze.getName() ;
			bytes = (int) ze.getSize() ;
			if (is == null) throw new IOException("null input stream") ;

			// Read the entire contents.

			int n = 0, len = 0 ;
			b = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;
			is.close() ;

			// Open the memory copy for syntax processing.

			encoding = "ASCII" ;
			is = new ByteArrayInputStream(b) ;
			InputStreamReader isr = new InputStreamReader(is) ;
			BufferedReader f = new BufferedReader(isr) ;
			invalidate() ;

         // Create new background and transparent index objects.

         background = new int[1] ;
         transparent = new int[1] ;
         background[0] = -1 ;
         transparent[0] = -1 ;

			// Decode the text file.  Read one line at a time.

			while (true)
			{
				s = f.readLine() ;
				if (s == null) break ;
            line++ ;
				s = trim(s) ;

				// Decode the line type.

				st = new StringTokenizer(s," " + ls) ;
				if (!st.hasMoreTokens()) continue ;
				String token = st.nextToken() ;
				if (".Kisekae".equals(token)) continue ;
				else if (".palettes".equals(token)) groups = getNumber(st) ;
				else if (".colors".equals(token)) colors = getNumber(st) ;
				else if (".bits".equals(token)) bits = getNumber(st) ;
				else if (".background".equals(token)) background[0] = getNumber(st) ;
				else if (".transparent".equals(token)) transparent[0] = getNumber(st) ;

				// Decode the multipalette data.

				else if (".palette".equals(token))
				{
					if (alpha == null)
					{
						size = colors * groups ;
						alpha = new byte[size] ;
						red = new byte[size] ;
						green = new byte[size] ;
						blue = new byte[size] ;
					}

               // Determine the multipalette to load.

					int mp = getNumber(st) ;
					int offset = mp * colors ;

               // Parse the data.  Four numbers per line.  (ARGB)

					for (int i = 0 ; i < colors ; i++)
					{
						s = f.readLine() ;
						if (s == null) break ;
		            line++ ;
						s = trim(s) ;
						st = new StringTokenizer(s," " + ls) ;
						alpha[offset] = (byte) getNumber(st) ;
						red[offset] = (byte) getNumber(st) ;
						green[offset] = (byte) getNumber(st) ;
						blue[offset] = (byte) getNumber(st) ;
						offset++ ;
					}
				}

				// Unknown data.

				else if (s.length() > 0)
					throw new Exception("Invalid file format" + "\n" + s) ;
			}

			loaded = true ;
		}

		// Watch for I/O errors.

		catch (IOException ex)
		{
			error = true ;
			showError("I/O Exception, Palette " + importfile + "\n" + ex.getMessage()) ;
		}

		// Watch for file size errors.

		catch (ArrayIndexOutOfBoundsException ex)
		{
			error = true ;
			showError("Palette " + importfile + " is not valid, size exception.") ;
			PrintLn.println("Palette Size Exception, " + importfile + " size = " + bytes) ;
			PrintLn.println(ex.getMessage()) ;
		}

		// Watch for numeric parse errors.

		catch (NumberFormatException ex)
		{
			error = true ;
			showError("Palette " + importfile + " is not valid, number format exception.") ;
			PrintLn.println("Palette NumberFormatException, " + importfile + " " + s) ;
			PrintLn.println(ex.getMessage()) ;
		}

		// Watch for general KiSS exceptions.

		catch (Exception ex)
		{
			error = true ;
			showError("Exception, Palette " + importfile + "\n" + ex.getMessage()) ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; }
			catch (IOException ex)
			{
				error = true ;
				showError("I/O Exception, Palette " + importfile + "\n" + ex.getMessage()) ;
			}
			is = null ;
		}
	}


   // A function to import a new KCF or PAL file.  This action replaces
   // the current palette file data.

   void importPalette(ArchiveEntry newze)
   {
   	if (newze == null) return ;
      InputStream is = null ;
		byte [] b = null ;
		alpha = null ;
      size = 0 ;
      bits = 0 ;
      groups = 0 ;
      colors = 0 ;
      line = 0 ;

      // Importing a .pal file.

   	importfile = newze.getName() ;
		int n = (newze == null) ? -1 : importfile.lastIndexOf('.') ;
		String newzeext = (n < 0) ? "" : importfile.substring(n).toLowerCase() ;
      if (".pal".equals(newzeext))
      {
      	loadText(newze) ;
         return ;
      }

      // Decode .kcf file

		try
		{
			is = newze.getInputStream() ;
			bytes = (int) newze.getSize() ;
			if (is == null) throw new IOException("null input stream") ;

			// Read the entire contents.

			n = 0 ;
         int len = 0 ;
			b = new byte[bytes] ;
			while (n < bytes && (len = is.read(b,n,bytes-n)) >= 0) n += len ;
			is.close() ;
			invalidate() ;
	      decode(b) ;
      }

		// Watch for I/O errors.

		catch (IOException ex)
		{
			error = true ;
			showError("I/O Exception, Palette " + importfile + "\n" + ex.getMessage()) ;
		}

		// Close the file on termination.

		finally
		{
			try { if (is != null) is.close() ; }
			catch (IOException ex)
			{
				error = true ;
				showError("I/O Exception, Palette " + importfile + "\n" + ex.getMessage()) ;
			}
			is = null ;
		}
   }


   // Function to decode a compressed KCF file.
	// Decode the file format.  Although it is not encouraged, it is
   // possible to save a .PAL text palette file as a .KCF file.  If
   // this happens the .KCF file will not be compressed and must be
   // restored from the text encoding.

   private void decode(byte [] b)
   {
		if (".Kisekae".equals(new String(b,0,8)))
		{
			encoding = "ASCII" ;
			bits = 0 ;
		}
		else if ("KiSS".equals(new String(b,0,4)))
		{
			encoding = "KiSS version 1" ;
			colors = fixByte((byte) 0,b[8]) ;
			groups = fixByte((byte) 0,b[10]) ;
			bits = fixByte((byte) 0,b[5]) ;
			offset = 32 ;
		}
		else
		{
			encoding = "KiSS version 0" ;
			colors = 16 ;
			groups = 10 ;
			bits = 12 ;
			offset = 0 ;
		}

		// Convert the palette from a packed 12 bit representation.
		// These palettes can contain a maximum of 4096 colors.

		if (bits == 12)
		{
			size = (b.length - offset) / 2 ;
			if (colors == 0 && groups == 0)
         	{ colors = size ; groups = 1 ; }
         else if (colors == 0 && groups != 0)
            { colors = size / groups ; }
			else if (colors != 0 && groups == 0)
         	{ groups = size / colors ; }
			alpha = new byte[size] ;
			red = new byte[size] ;
			green = new byte[size] ;
			blue = new byte[size] ;
			int i = offset ;
			int j = 0 ;

         // RGB colors with 4 bits are shifted left to create color bytes
         // that span the complete color range from 0 to 256.

			while (j < size)
			{
           	alpha[j] = (byte) 255 ;
				red[j] = (byte) (((b[i] >>> 4) & 0xf) << 4) ;
				blue[j] = (byte) ((b[i] & 0xf) << 4) ;
				green[j] = (byte) ((b[i+1] & 0xf) << 4) ;
				i = i + 2 ;
				j = j + 1 ;
			}
		}

		// Calculate colors in 16M palette.  It should be 256.

		if (bits == 24)
		{
			size = (b.length - offset) / 3 ;
			if (colors == 0 && groups == 0)
         	{ colors = size ; groups = 1 ; }
         else if (colors == 0 && groups != 0)
            { colors = size / groups ; }
			else if (colors != 0 && groups == 0)
         	{ groups = size / colors ; }
			alpha = new byte[size] ;
			red = new byte[size] ;
			green = new byte[size] ;
			blue = new byte[size] ;
			int i = offset ;
			int j = 0 ;

			while (j < size)
			{
           	alpha[j] = (byte) 255 ;
				red[j] = b[i] ;
				green[j] = b[i+1] ;
				blue[j] = b[i+2] ;
				i = i + 3 ;
				j = j + 1 ;
			}
		}

      background = new int[1] ;
		transparent = new int[1] ;
      background[0] = 0 ;
      transparent[0] = 0 ;
      if (bits != 0) loaded = true ;
	}


	// Method to merge a KiSS palette with this object.  This method appends
   // the palette definitions to the current palette as new multipalettes.
   // A merge will expand the colors in each multipalette to the maximum
   // number of colors required.  Current color models for the palette can
   // be invalidated.  This function is destructive.  The merge palette
   // and this palette can be modified.

	void merge(Palette mergepalette)
	{
   	if (mergepalette == null) return ;
      int mergecolors = mergepalette.getColorCount() ;
      if (mergecolors > colors) setColorCount(mergecolors) ;
      if (mergecolors < colors) mergepalette.setColorCount(colors) ;

      // Append all new multipalettes to this palette.

      int n = mergepalette.getMultiPaletteCount() ;
     	Object [] mp = mergepalette.getPaletteData() ;
      byte [] alpha = (byte []) mp[0] ;
      byte [] red = (byte []) mp[1] ;
      byte [] green = (byte []) mp[2] ;
      byte [] blue = (byte []) mp[3] ;
      addMultiPalettes(n,groups,alpha,red,green,blue) ;
   }



	// Object update methods
	// ---------------------

	// Method to set the palette definition to the specified color
	// arrays.  Note that all color models for this palette will be
	// invalidated.

	void setPalette(byte [] a, byte [] r, byte [] g, byte [] b)
	{
      setPalette(a,r,g,b,1,((r == null) ? 0 : r.length),-1,-1) ;
   }

	// Method to set the palette definition to the specified RGB color
	// arrays.  This also establishes the palette transparent index if
   // there is a fully transparent color in the arrays.

   void setPalette(int [] rgb)
   {
      int m = rgb.length ;
      int ti = -1 ;
      byte [] a = new byte[m] ;
      byte [] r = new byte[m] ;
      byte [] g = new byte[m] ;
      byte [] b = new byte[m] ;
      for (int i = 0 ; i < m ; i++)
      {
         int c = rgb[i] ;
         a[i] = (byte) ((c & 0xff000000) >> 24) ;
         r[i] = (byte) ((c & 0xff0000) >> 16) ;
         g[i] = (byte) ((c & 0xff00) >> 8) ;
         b[i] = (byte) (c & 0xff) ;
         if (a[i] == 0) ti = i ;
      }
      setPalette(a,r,g,b) ;
      setTransparentIndex(ti) ;
   }

	// Method to set the palette definition to the specified multipalette color
	// arrays, with known background and transparency indexes.

	void setPalette(byte [] a, byte [] r, byte [] g, byte [] b,
		int mp, int c, int back, int trans)
	{
      if (background == null) background = new int[1] ;
      if (transparent == null) transparent = new int[1] ;
      background[0] = back ;
      transparent[0] = trans ;
      setPalette(a,r,g,b,mp,c,background,transparent) ;
   }

   // Set color values plus number of multipalettes and background and
	// transparency indexes.  We write 24 bit palettes to retain all color
	// information.

	void setPalette(byte [] a, byte [] r, byte [] g, byte [] b,
		int mp, int c, int [] back, int [] trans)
	{
		error = false ;
		invalidate() ;
		groups = mp ;
		colors = c ;
		bits = 24 ;
		size = colors * groups ;
		background = back ;
		transparent = trans ;
		bytes = 32 + groups * ((bits == 12) ? (colors * 2) : (colors * 3)) ;
		alpha = a ;
		red = r ;
		green = g ;
		blue = b ;
      loaded = true ;
	}

	// Method to set the active palette data.

	void setPaletteData(Object [] o)
	{
		alpha = (byte []) o[0] ;
		red = (byte []) o[1] ;
		green = (byte []) o[2] ;
		blue = (byte []) o[3] ;
      size = (red == null) ? 0 : red.length ;
	}


	// Method to add new multipalette definitions to the specified color
	// arrays in the correct position.  The new multipalettes are constructed
	// to be the same size as all other multipalettes.  Note that all color
	// models for this palette will be invalidated.

	void addMultiPalette(Integer mp)
   { addMultiPalettes(1,mp.intValue(),null,null,null,null) ; }

	void addMultiPalette(byte [] a, byte [] r, byte [] g, byte [] b)
   { addMultiPalettes(1,groups,a,r,g,b) ; }

	void addMultiPalettes(int n, int mp, byte [] a, byte [] r, byte [] g, byte [] b)
	{
		invalidate() ;
		groups += n ;
		if (groups < 0) groups = 0 ;
		size = groups * colors ;
		int split = mp * colors ;
		int newdata = n * colors ;
		byte [] newalpha = new byte [size] ;
		byte [] newred = new byte [size] ;
		byte [] newgreen = new byte [size] ;
		byte [] newblue = new byte [size] ;
		if (a == null) a = new byte [colors] ;
		if (r == null) r = new byte [colors] ;
		if (g == null) g = new byte [colors] ;
		if (b == null) b = new byte [colors] ;

		// Copy the old palette colors to the new palette, followed by the
		// new colors.  We first copy the initial multipalettes, then the
		// new data, then the remaining multipalettes.

		for (int i = 0 ; i < size ; i++)
		{
			if (i < split)
				newalpha[i] = alpha[i] ;
			else if (i-split < newdata)
				newalpha[i] = (i-split < a.length) ? a[i-split] : 0 ;
			else if (i-newdata < alpha.length)
				newalpha[i] = alpha[i-newdata] ;
			if (i < split)
				newred[i] = red[i] ;
			else if (i-split < newdata)
				newred[i] = (i-split < r.length) ? r[i-split] : 0 ;
			else if (i-newdata < red.length)
				newred[i] = red[i-newdata] ;
			if (i < split)
				newgreen[i] = green[i] ;
			else if (i-split < newdata)
				newgreen[i] = (i-split < g.length) ? g[i-split] : 0 ;
			else if (i-newdata < green.length)
				newgreen[i] = green[i-newdata] ;
			if (i < split)
				newblue[i] = blue[i] ;
			else if (i-split < newdata)
				newblue[i] = (i-split < b.length) ? b[i-split] : 0 ;
			else if (i-newdata < blue.length)
				newblue[i] = blue[i-newdata] ;
		}

      // Update our color arrays.

		alpha = newalpha ;
		red = newred ;
		green = newgreen ;
		blue = newblue ;
		bytes = 32 + groups * ((bits == 12) ? (colors * 2) : (colors * 3)) ;
      setUpdated(true) ;
	}

	// Method to set a specific multipalette data arrays to the specified values.

	void setMultiPalette(byte [] a, byte [] r, byte [] g, byte [] b, int mp)
	{
		invalidate() ;
		size = groups * colors ;
		int split = mp * colors ;

		// Copy the new colors to the palette.

      try
      {
         for (int i = 0 ; i < colors ; i++)
         {
            int index = split + i ;
            alpha[index] = (i < a.length) ? a[i] : 0 ;
            red[index] = (i < r.length) ? r[i] : 0 ;
            green[index] = (i < g.length) ? g[i] : 0 ;
            blue[index] = (i < b.length) ? b[i] : 0 ;
         }
      }
      catch (ArrayIndexOutOfBoundsException e) { }

      // Update our color arrays.

		bytes = 32 + groups * ((bits == 12) ? (colors * 2) : (colors * 3)) ;
      setUpdated(true) ;
   }


	// Method to delete multipalette definitions from the specified color
	// arrays.  Note that all color models for this palette will be invalidated.

	void deleteMultiPalette(Integer mp)
	{ deleteMultiPalettes(1,mp.intValue()) ; }

	void deleteMultiPalettes(int n, int mp)
	{
		invalidate() ;
		groups -= n ;
		if (groups < 0) groups = 0 ;
		size = groups * colors ;
		int split = mp * colors ;
		byte [] newalpha = new byte [size] ;
		byte [] newred = new byte [size] ;
		byte [] newgreen = new byte [size] ;
		byte [] newblue = new byte [size] ;

      // Copy the old palette colors to the new palette.  We first copy the
      // initial multipalettes, then the remaining multipalettes after the
      // deleted multipalette.

		for (int i = 0 ; i < size ; i++)
		{
			if (i < split)
				newalpha[i] = alpha[i] ;
			else
				newalpha[i] = alpha[i+n*colors] ;
			if (i < split)
				newred[i] = red[i] ;
			else
				newred[i] = red[i+n*colors] ;
			if (i < split)
				newgreen[i] = green[i] ;
			else
				newgreen[i] = green[i+n*colors] ;
			if (i < split)
				newblue[i] = blue[i] ;
			else
				newblue[i] = blue[i+n*colors] ;
		}

      // Update our color arrays.

		alpha = newalpha ;
		red = newred ;
		green = newgreen ;
		blue = newblue ;
		bytes = 32 + groups * ((bits == 12) ? (colors * 2) : (colors * 3)) ;
      setUpdated(true) ;
	}


   // Method to set the palette color count.  If the count is greater than
   // the current size then black colors are appended to each multipalette.
   // If the count is smaller than the current size then colors at the end
   // of the palette are dropped.  Note that all color models for this
   // palette will be invalidated.

   void setColorCount(int newcolors)
   {
   	if (newcolors < 0) return ;
   	if (newcolors > 256) newcolors = 256 ;
   	int n = groups * newcolors ;
		byte [] newalpha = new byte [n] ;
		byte [] newred = new byte [n] ;
		byte [] newgreen = new byte [n] ;
		byte [] newblue = new byte [n] ;

		// Copy the current palette data for each multipalette.

		for (int i = 0 ; i < groups ; i++)
		{
			int oldmpstart = i * colors ;
			int newmpstart = i * newcolors ;
			for (int j = 0 ; j < colors ; j++)
			{
				if (j >= newcolors) break ;
				newalpha[j+newmpstart] = alpha[j+oldmpstart] ;
				newred[j+newmpstart] = red[j+oldmpstart] ;
				newgreen[j+newmpstart] = green[j+oldmpstart] ;
				newblue[j+newmpstart] = blue[j+oldmpstart] ;
			}
		}

      // Update our color arrays.

      invalidate() ;
		alpha = newalpha ;
		red = newred ;
		green = newgreen ;
		blue = newblue ;
      colors = newcolors ;
      size = n ;
		bytes = 32 + groups * ((bits == 12) ? (colors * 2) : (colors * 3)) ;
      setUpdated(true) ;
   }


	// Object output methods
	// ----------------------

	// Method to write a KiSS KCF palette file.  This method encodes the
	// structure of a KCF file and writes the file to an output stream.
   // Transparent colors must be at index 0.

	private int encode(FileWriter fw, OutputStream out) throws IOException
	{
		byteswritten = 0 ;
		if (error) return 0 ;
      if (encodered == null) return 0 ;
      if (encodegreen == null) return 0 ;
      if (encodeblue == null) return 0 ;

      // Write the Magic header
		putString(out,"KiSS");

      // Write the Palette file indicator
		putByte(out, (byte) 0x10) ;

		// Write bits per color (12 or 24)
		putByte(out, (byte) (bits & 0xff)) ;

      // Reserved word
		putWord(out,0) ;

      // Number of colors in one palette group
		putWord(out,encodecolors) ;

      // Number of palette groups
		putWord(out,groups) ;

      // Reserved space
		putWord(out,0) ;
		putWord(out,0) ;
		for (int i = 0 ; i < 16 ; i++) putByte(out, (byte) 0) ;

		// Go and actually write the data
		compress(fw,out,bits) ;
      encodered = null ;
      encodeblue = null ;
      encodegreen = null ;
      encodealpha = null ;
      return byteswritten ;
   }


   // Function to compress the palette colors and write them to the
	// output stream.  This function ensures that the palette transparent
   // index is written to index entry 0.  Cel encoding must also ensure
   // that any image transparent color is associated with palette index 0.

	private void compress(FileWriter fw, OutputStream out, int bits) throws IOException
	{
      int lastbyteswritten = 0 ;
      
      // Access all colors in all multipalettes.

      try
      {
			for (int index = 0 ; index < encodecolors*groups ; index++)
	      {
	      	if (bits == 12)
	         {
	 	        	byte r = (byte) ((encodered[index] >> 4) & 0xf) ;
	         	byte g = (byte) ((encodegreen[index] >> 4) & 0xf) ;
					byte b = (byte) ((encodeblue[index] >> 4) & 0xf) ;
	            byte b1 = (byte) ((r * (1 << 4)) + b) ;

               // Write the palette entry.

					putByte(out,b1) ;
					putByte(out,g) ;
	         }

	      	if (bits == 24)
	         {
  					putByte(out,encodered[index]) ;
  					putByte(out,encodegreen[index]) ;
  					putByte(out,encodeblue[index]) ;
            }
            
            if (fw != null) fw.updateProgress(byteswritten - lastbyteswritten) ;
            lastbyteswritten = byteswritten ;
         }
      }

      // Watch for palette size problems.

      catch (ArrayIndexOutOfBoundsException e)
      {
         e.printStackTrace() ;
      	throw new IOException("Palette size fault, colors = "
         	+ colors + ", palette groups = " + groups
            + ", actual size = " + size + ", encodecolors = " 
            + encodecolors + ", encodesize = " + encodered.length) ;
      }
	}


	// Method to export a KiSS palette file.  This method writes the
	// structure of a KCF file in text form to an output stream.

	private int exporttext(FileWriter fw, OutputStream out) throws IOException
	{
		byteswritten = 0 ;
      int lastbyteswritten = 0 ;
		if (error) return 0 ;
      if (encodered == null) return 0 ;
      if (encodegreen == null) return 0 ;
      if (encodeblue == null) return 0 ;
      if (encodealpha == null) return 0 ;

		Date today = new Date() ;
		String ls = System.getProperty("line.separator") ;
		putString(out,".Kisekae UltraKiss palette exported on " + today.toString() + ls) ;
		putString(out,".palettes " + groups + ls) ;
		putString(out,".colors " + encodecolors + ls) ;
		putString(out,".bits " + bits + ls) ;
		putString(out,".background " + getBackgroundIndex() + ls) ;
		putString(out,".transparent " + getTransparentIndex() + ls) ;
      if (fw != null) fw.updateProgress(byteswritten - lastbyteswritten) ;
      lastbyteswritten = byteswritten ;

		// Write each multipalette.

		for (int i = 0 ; i < groups ; i++)
		{
			putString(out," " + ls) ;
			putString(out,".palette " + i + " " + ls) ;
			int mpstart = i * encodecolors ;
			for (int j = 0 ; j < encodecolors ; j++)
			{
				int a = encodealpha[j+mpstart] & 0xff ;
				int r = encodered[j+mpstart] & 0xff ;
				int g = encodegreen[j+mpstart] & 0xff ;
				int b = encodeblue[j+mpstart] & 0xff ;
				putString(out,a + " ") ;
				putString(out,r + " ") ;
				putString(out,g + " ") ;
				putString(out,b + " " + ls) ;
			}
         if (fw != null) fw.updateProgress(byteswritten - lastbyteswritten) ;
         lastbyteswritten = byteswritten ;
		}

      // Clean up.

      encodered = null ;
      encodeblue = null ;
      encodegreen = null ;
      encodealpha = null ;
      if (fw != null) fw.updateProgress(byteswritten - lastbyteswritten) ;
		return byteswritten ;
	}


   // A method to write a string to an output stream.

	private void putString(OutputStream out, String str) throws IOException
   {
      byte[] buf = str.getBytes();
		out.write(buf) ;
      byteswritten += buf.length ;
   }


   // Function to write out a word to the Palette file in Little Endian mode.

	private void putWord(OutputStream out, int w) throws IOException
	{
		putByte(out, (byte) (w & 0xff));
		putByte(out, (byte) (( w >> 8 ) & 0xff));
   }


   // Function to write out a byte to the Palette file.

	private void putByte(OutputStream out, byte b) throws IOException
	{ out.write(b) ; byteswritten++ ; }


	// Function to get a number from a string token.  This function
   // throws a KissException if there is a numeric parsing error.

	private int getNumber(StringTokenizer st) throws NumberFormatException
	{
		Integer n ;
		if (!st.hasMoreTokens())
      	throw new NumberFormatException("NumberFormatException: No number") ;
		String token = st.nextToken() ;
		try { n = new Integer(token) ; }
      catch (NumberFormatException e)
      { throw new NumberFormatException("NumberFormatException: " + token) ; }
		return n.intValue() ;
	}


	// Color utility methods
	// ---------------------

	// Method to set a color in the palette.  Note that all color models
   // for the multipalette will be invalidated on any color change. Cels
   // that reference an invalid color model must be re-established through
   // a color change request.

	void setColor(Integer multipalette, int colorindex, Color color)
	{
		if (error) return ;
      if (color == null) return ;
		int mp = (multipalette == null) ? 0 : multipalette.intValue() ;
		int i = (mp * colors) + colorindex ;
		if (i >= size || i < 0) return ;
		alpha[i] = (byte) color.getAlpha() ;
		red[i] = (byte) color.getRed() ;
		blue[i] = (byte) color.getBlue() ;
		green[i] = (byte) color.getGreen() ;
		invalidate(mp) ;
		setUpdated(true) ;
		return  ;
	}


	// Method to return a color, given a color index relative to a multipalette.
   // If the multipalette number exceeds the number of multipalettes in the
   // file, then we return the color relative to multipalette 0.  If the
   // color index exceeds the total number of colors in the file across all
   // multipalettes then we return the color at index 0.  The color returned
   // is opaque unless the request specified with alpha.

   Color getColor(int mp, int colorindex)
   { return getColor(mp,colorindex,false) ; }

	Color getColor(int mp, int colorindex, boolean withalpha)
	{
		if (error) return Color.black ;
		int i = (mp * colors) + colorindex ;
		if (i >= size) i = colorindex ;
		if (i >= size || i < 0) i = 0 ;
      try
      {
   		int a = ((int) alpha[i]) & 0xff ;
         int r = ((int) red[i]) & 0xff ;
         int g = ((int) green[i]) & 0xff ;
   		int b = ((int) blue[i]) & 0xff ;
         if (!withalpha) a = 255 ;
   		return new Color(r,g,b,a) ;
      }
      catch (Exception e) { }
      return Color.black ;
	}


	// Method to return a color index, given a color.

	int getColorIndex(Color c)
   { return getColorIndex(c,0) ; }

	int getColorIndex(Color c, int mp)
	{
		if (error) return -1 ;
      try
      {
         int rgb = c.getRGB() ;
         byte r = (byte) ((rgb & 0xff0000) >> 16) ;
         byte g = (byte) ((rgb & 0xff00) >> 8) ;
         byte b = (byte) ((rgb & 0xff) >> 0) ;
         for (int i = 0 ; i < colors ; i++)
         {
            int j = mp * colors + i ;
   		   if (r == red[j] && g == green[j] && b == blue[j]) return i ;
         }
      }
      catch (Exception e) { }
      return -1 ;
	}


   // Method to return the background color for the specified multipalette.
   // The returned color is transparent if the background color index equals
   // the transparent color index, otherwise it is opaque.

   Color getBackgroundColor(int multipalette)
   {
      if (background == null) return null ;
		if (background[0] < 0 || background[0] >= colors) return null ;
      Color c = getColor(multipalette,background[0]) ;
      if (transparent != null && background[0] == transparent[0])
      	c = new Color(c.getRed(),c.getGreen(),c.getBlue(),0) ;
      return c ;
   }


   // Method to return the transparent color for the specified multipalette.
   // Note that the returned color is opaque.

   Color getTransparentColor(int multipalette)
   {
      if (transparent == null) return null ;
   	if (transparent[0] < 0 || transparent[0] >= colors) return null ;
      return getColor(multipalette,transparent[0]) ;
   }


	// Method to return a fully opaque color model for the specified
	// multipalette.

	ColorModel getColorModel(int multipalette)
	{ return createColorModel(255,multipalette) ; }


	// Method to return a known color model for the specified transparency
   // and multipalette.

	ColorModel getColorModel(int transparency, int multipalette)
	{
		Integer key = new Integer((transparency<<8)+(multipalette)) ;
		ColorModel cm = (ColorModel) cmkey.get(key) ;
		return cm ;
   }


	// Method to create a new index color model for this palette.  Pixels
   // at the palette transparent index are transparent.  Images with identical
	// pixel size, transparency requirements and palette offset all use the
   // same IndexColorModel object.

	ColorModel createColorModel(int transparency, int multipalette)
	{
      transparency = 255 ;
      // Note: With the implementation of the AlphaComposite feature
      // to expedite transparency drawing, all color models are now
      // fully opague. 
      if (transparency < 0) transparency = 0 ;
      if (transparency > 255) transparency = 255 ;
      if (multipalette >= groups) multipalette = groups - 1 ;
      if (multipalette < 0) multipalette = 0 ;
		Integer key = new Integer((transparency<<8)+(multipalette)) ;
		ColorModel cm = (ColorModel) cmkey.get(key) ;
		if (cm != null) return cm ;

		// We could have a multipalette request.  We create new 256
		// color models because this seems to be the only size that
		// works consistently across various implementations and
		// different machines.

		byte r[] = new byte[256] ;
		byte g[] = new byte[256] ;
		byte b[] = new byte[256] ;
		byte a[] = new byte[256] ;
		int maxcolors = (colors > a.length) ? a.length : colors ;
		for (int i = 0 ; i < maxcolors ; i++)
		{
			if (i >= a.length) break ;
			int mp = multipalette*maxcolors + i ;
			if (mp < red.length)
			{
				r[i] = red[mp] ;
				g[i] = green[mp] ;
				b[i] = blue[mp] ;
				a[i] = (byte) transparency ;

				// Sun Java does does not properly treat color (0,0,0) as
				// truly transparent so we adjust the color to (0,0,1).

				if (r[i] == 0 && g[i] == 0 && b[i] == 0) b[i] = 1 ;
			}
		}

		// Set the transparent color if it exists and create a new color model.

      int trans = -1 ;
		if (transparent != null && transparent[0] >= 0 && transparent[0] < a.length)
      {
         trans = transparent[0] ;
         a[transparent[0]] = 0 ;
      }
		cm = new IndexColorModel(8,256,r,g,b,a) ;
		cmkey.put(key,cm) ;
		return cm ;
	}


	// Method to invalidate all color models for a given multipalette.
	// Invalidation removes all transparency variants of the color model
	// from our color model key table.  Invalidation always occurs if any
	// palette color changes.  If a color model has been invalidated then
	// all cels referencing the color model should be re-established
	// through a cel color change request before they are used.

	void invalidate() { invalidate(-1) ; }
	void invalidate(int multipalette)
	{
		Enumeration e = cmkey.keys() ;
		while (e.hasMoreElements())
		{
			Integer key = (Integer) e.nextElement() ;
			int mp = key.intValue() & 255 ;
			if (multipalette < 0 || mp == multipalette)
         	cmkey.remove(key) ;
		}
	}


	// Function to display a syntax error message.

	private void showError(String s)
	{
   	errormessage = s ;
		if (line > 0) s = "Line [" + line + "] " + s ;
		if (loader != null) loader.showError(s) ;
		else PrintLn.println(s) ;
	}


	// Function to trim leading spaces from a string.

	private static String trim(String s)
	{
		int i = 0 ;
      int length = s.length() ;
		while (i < length)
      {
      	char c = s.charAt(i) ;
         if (Character.isWhitespace(c) || Character.isISOControl(c)) i++ ;
         else break ;
      }
      if (i >= length) return "" ;
		return s.substring(i) ;
	}


   // The toString method returns a string representation of this object.
   // This is the class name concatenated with the object identifier.

   public String toString()
   {
   	if (getIdentifier() instanceof Integer)
      {
         String s = super.toString() + " " + getName() ;
         if (isInternal()) s += " " + this.hashCode() ;
   		return s ;
      }
      else
      	return super.toString() ;
   }
}
