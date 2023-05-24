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
* State class
*
* Purpose:
*
* This class is a container that maintains the values of cel or group 
* state variables.  The state object has a unique identifier (typically a
* hashcode), and a variable array of size n.  Thus, the state is associated
* with a unique object.  An object wishing to establish a known state simply
* creates an instance of a State object and populates the variable array with
* its representative values.
* 
*/

import java.awt.Point ;
import java.util.Hashtable ;
import java.util.Vector ;
import java.util.Enumeration ;

final class State
{
	// Class attributes
	
	static private Hashtable key = new Hashtable(4098,0.9f) ;
	
	// Hashtable keys are compound entities that contain a reference
	// to a configuration.  Thus, multiple configurations can coexist 
	// in the static hash table.  When we clear a table we must remove
	// only those entities that are associated with the specified
	// file.  
	
	static void clearTable(Object cid) 
	{
		Enumeration e = key.keys() ;
		while (e.hasMoreElements())
		{
			String hashkey = (String) e.nextElement() ;
			if (hashkey.startsWith(cid.toString())) key.remove(hashkey) ;
		}
	}
	
	// Kiss object state attributes.  These are accessed using a
	// direct reference to this state object.

	private int elements = 0 ;	 					// The number of variables
	protected Object object = null ;				// The object owning this state
	protected Object identifier = null ;	 	// The state identifier name
	protected Object variable[] = null ;	 	// The state variables


	// Constructor

	public State(Object cid, Object o, Object id, int n)
	{
		object = o ;
		identifier = id ;
		variable = new Object[n] ;
		elements = n ;
		if (id == null) id = new String("Unknown") ;
		if (cid == null) cid = new String("Unknown") ;
		if (o != null)
		{
			String s = cid.toString() + " " + o.getClass().hashCode() + o.hashCode() + "-" + id.toString() ;
			key.remove(s) ;
			key.put(s,this) ;
		}
	}
	
	
	// Class methods
	// -------------

	// Return the object by key.
	
	static Object getByKey(Object cid, Object o, Object id) 
	{
		if (o == null) return null ;
		if (id == null) id = new String("Unknown") ;
		if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + o.getClass().hashCode() + o.hashCode() + "-" + id.toString() ;
		return key.get(hashkey) ;
	}

	// Remove an object by key.
	
	static Object removeByKey(Object cid, Object o, Object id)
	{
		if (o == null) return null ;
		if (id == null) id = new String("Unknown") ;
		if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + o.getClass().hashCode() + o.hashCode() + "-" + id.toString() ;
		o = key.remove(hashkey) ;
      return o ;
	}


	// Return the number of elements.

	int getSize() { return elements ; }
}
