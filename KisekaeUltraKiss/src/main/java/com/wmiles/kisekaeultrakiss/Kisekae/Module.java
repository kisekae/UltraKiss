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
* Module class
*
* Purpose:
*
* This class is a container for programming code.  The module class
* represents an FKiSS Label object.  These objects store action
* commands in the action vector maintained by the KissObject super 
* class.  
* 
* Labels use their number as their access key.  
* 
*/

import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Vector ;
import java.io.IOException ;
import java.io.OutputStream ;

final class Module extends KissObject
{
	// Class attributes.  Sized for 512 label objects.
	
	static private Hashtable key = new Hashtable(512,1.0f) ;
	
	// Module attributes

	private Object cid = null ;			// The configuration ID
   private Vector parameters = null ;  // The module parameter list


	// Constructor

	public Module(Object cid, String id, Vector params)
	{
		this.cid = cid ;
      parameters = params ;
		setIdentifier(id) ;
      setUniqueID(Integer.valueOf(this.hashCode())) ;
		setKey(key,cid,id.toUpperCase()) ;
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
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}


	// Kiss object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }
}
