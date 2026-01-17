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
* ImageSelection Class
*
* Purpose:
*
* This object is a transferable object for integrated support to transfer
* images between Java programs and native applications.
*
*/

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import javax.swing.*;

public class ImageSelection extends TransferHandler
    implements Transferable
{

   private static final DataFlavor flavors[] = { DataFlavor.imageFlavor };
   private Image image;

   public int getSourceActions(JComponent c)
   {
      return TransferHandler.COPY;
   }

   // canImport returns true if the component can support one of the data
   // flavors, false otherwise.

   public boolean canImport(JComponent comp, DataFlavor flavor[])
   {
      if (!(comp instanceof JLabel) || (comp instanceof AbstractButton))
      {
         return false;
      }
      for (int i=0, n=flavor.length; i<n; i++)
         if (flavor[i].equals(flavors[0])) return true;
      return false;
   }


   // This saves a reference to the data to be transferred, and returns the
   // TransferHandler (this). The component represents where the data is coming
   // from. This is the copy operation. The handler does the actual copy to
   // the clipboard at the appropriate time.

   public Transferable createTransferable(JComponent comp)
   {
      // Clear
      image = null;
      Icon icon = null;

      if (comp instanceof JLabel)
      {
        JLabel label = (JLabel)comp;
        icon = label.getIcon();
      }
      else if (comp instanceof AbstractButton)
      {
        AbstractButton button = (AbstractButton) comp;
        icon = button.getIcon();
      }
      if (icon instanceof ImageIcon)
      {
         image = ((ImageIcon)icon).getImage();
         return this;
      }
      return null;
   }

   // This returns true if the component supports getting one of the data
   // flavors from the Transferable object, and successfully gets it, false
   // otherwise. This is the paste operation. Again, the handler gets the
   // data from the clipboard, you just have to get it from the Transferable.

   public boolean importData(JComponent comp, Transferable t)
   {
      ImageIcon icon = null;
      try
      {
         if (t.isDataFlavorSupported(flavors[0]))
         {
            image = (Image) t.getTransferData(flavors[0]);
            icon = new ImageIcon(image);
         }
         if (comp instanceof JLabel)
         {
            JLabel label = (JLabel)comp;
            label.setIcon(icon);
            return true;
         }
         else if (comp instanceof AbstractButton)
         {
            AbstractButton button = (AbstractButton) comp;
            button.setIcon(icon);
            return true;
         }
      }
      catch (UnsupportedFlavorException ignored) { }
      catch (IOException ignored) {   }
      return false;
   }

   // Transferable
   public Object getTransferData(DataFlavor flavor)
   {
      if (isDataFlavorSupported(flavor)) return image;
      return null;
   }

   public DataFlavor[] getTransferDataFlavors() { return flavors; }

   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      return flavor.equals(flavors[0]);
   }
}

