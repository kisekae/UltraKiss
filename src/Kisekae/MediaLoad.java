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
* MediaLoad class
*
* Purpose:
*
* This class defines a loader thread used to load audio or video media files
* for the Media Player.
* 
*/

import java.io.* ;
import java.util.* ;
import java.awt.* ;
import java.awt.event.* ;
import javax.swing.JButton ;



final class MediaLoad extends Thread
{
	private KissObject ko = null ;						// The abstract media
   private KissFrame parent = null ;               // The parent frame
   private ArchiveFile zip = null ;                // The media zip file
   private boolean opened = false ;                // True if zip file opened

	// Our update callback button that other components can attach
	// listeners to.  The callback is fired when the load is complete.

	protected CallbackButton callback = new CallbackButton(this,"MediaLoad Callback") ;


	// Constructor

   public MediaLoad(KissFrame f, KissObject kiss)
   { parent = f ; ko = kiss ; }

   // Return our load object.

   KissObject getKissObject() { return ko ; }



	// Method to load a media object.   The load is performed asynchronously.
	// The media playback is started after being loaded.

	public void run()
	{
      if (OptionsDialog.getDebugControl())
         System.out.println("MediaLoad: loading " + ko) ;

      // Ensure the file is open.

      try
      {
         zip = (ko != null) ? ko.getZipFile() : null ;
         if (zip != null && !zip.isOpen())
         {
            zip.open() ;
            zip.connect() ;
            opened = true ;
         }

         // Load the media file.

   		if (ko instanceof Audio)
   		{
   			Audio a = (Audio) ko ;
            a.setLoader(parent) ;
            a.setType("media") ;
   			a.load() ;
           	a.init() ;
         }

   		if (ko instanceof Video)
         {
            Video v = (Video) ko ;
            v.setLoader(parent) ;
            v.load() ;
            v.init() ;
         }

         // Close the archive file.

         if (opened && zip != null)
         {
            zip.disconnect() ;
            zip.close() ;
         }

         // Signal the load is complete.
         
         callback.doClick() ;
   	}

      catch (IOException e)
      {
         System.out.println("MediaLoad: IOException loading " + ko + " file " + zip) ;
         e.printStackTrace() ;
      }
   }
}
