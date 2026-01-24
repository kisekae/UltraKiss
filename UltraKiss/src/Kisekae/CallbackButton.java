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
* CallbackButton class
*
* Purpose:
*
* This is an extension of a JButton to retain the owner frame attribute
* for a programmable callback.
*
*/


import javax.swing.* ;
import java.util.Vector ;
import java.awt.event.ActionListener ;

final class CallbackButton extends JButton
{
	private Object data = null ;
	private Object parent = null ;
   private Vector listeners = new Vector() ;

   public CallbackButton(Object f, String text)
   {
      super(text) ;
      parent = f ;
   }
   
   // Accessors and mutators to callback data.

   public Object getParentObject() { return parent ; }
   public Object getDataObject() { return data ; }
   public void setDataObject(Object o) { data = o ; }
   

   // Overloaded action listener methods to allow for removal of all
   // registered listeners.

   public void removeActionListener(ActionListener l)
   {
      if (l == null)
      {
         for (int i = 0 ; i < listeners.size() ; i++)
         {
            ActionListener al = (ActionListener) listeners.elementAt(i) ;
            super.removeActionListener(al) ;
         }
         listeners = new Vector() ;
         return ;
      }
      super.removeActionListener(l) ;
      listeners.removeElement(l) ;
   }

   public void addActionListener(ActionListener l)
   {
      super.addActionListener(l) ;
      listeners.addElement(l) ;
   }
   
   public String toString()
   {
      return "CallbackButton["+ this.hashCode() + "] " + getText() + " listeners = " + listeners ;
   }
}

