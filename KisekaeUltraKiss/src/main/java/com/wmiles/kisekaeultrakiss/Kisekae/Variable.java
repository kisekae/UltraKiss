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
* Variable class
*
* Purpose:
*
* This class maintains all global FKiSS variables declared during action
* event programming.  One instance of this class is created for each new
* configuration.
*
* Variables are referenced by name.  Variables prefixed with '$' are
* indirect references to the variable value.  For example, the value of
* variable '$X' is the value of variable 'X'.
*
* Variables prefixed with '@' are local references to the variable value.
* Local references are unique to the scheduling event.  For example, the
* value of variable '@X' is the value of local variable 'X' in the current
* event. Each event object maintains its own unique local variable table
* indexed by processing depth.
*
* Variables prefixed with '!' reference a named group of cels.  Named cel
* groups is an FKiss 4 extension that is used to apply an FKiSS function
* to a collection of cels.
*
*/

import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Vector ;

final class Variable
{
	// Class attributes.  Sized for 1000 variables.
	
	private Hashtable globalvariable = new Hashtable(1200,0.8f) ;


	// Variable set value.  Given a variable, set its value.
	// The value can be any object type.  The variable name must
	// be String type.
	
	void setValue(Object name, Object o, FKissEvent event)
	{
		if (!(name instanceof String)) return ;
		String v = (String) name ;
		if (v.length() == 0) return ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// Check for relative variable names.
		
		if (v.charAt(0) == '$')
		{
			name = getValue(v.substring(1),event) ;
         String s = (name != null) ? name.toString() : "" ;
         if (!OptionsDialog.getVariableCase()) s = s.toUpperCase() ;
			if (name != null) v = "$" + s ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
         if (variable == null)
            event = event ;
      }

		// Save the value using the absolute variable name.  Note, event, action, 
      // and variable traces set as 'nobreakpoint' in the FKiSS editor have a 
      // sentinal '*' inserted in the text string. These trace lines are not 
      // forwarded to the trace dialog although they will appear in the log file.

		if (o == null)
		{
			variable.remove(v) ;
			if (OptionsDialog.getDebugVariable() && (!event.getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
         {
            String bp = (event.getNoBreakpoint()) ? "*" : " " ;
				PrintLn.println("  > [" + Thread.currentThread().getName() + "]"+bp+"Variable " + v + " removed.") ;
         }
		}
		else
		{
			variable.put(v,o) ;
			if (OptionsDialog.getDebugVariable() && (!event.getNoBreakpoint() || OptionsDialog.getDebugDisabled()))
         {
            String bp = (event.getNoBreakpoint()) ? "*" : " " ;
				PrintLn.println("  > [" + Thread.currentThread().getName() + "]"+bp+"Variable " + v + " set to " + o.toString()) ;
         }
		}
	}


	// Variable get value.  Returns the object value of the variable.
	// The value will be an object type such as a String, for string variables,
	// or a numeric object or a vector type for named cel groups.
	
	Object getValue(String v, FKissEvent event)
	{ 
		if (v == null) return null ;
		if (v.length() == 0) return v ;
      Hashtable variable = globalvariable ;

		// If this is a string literal value, return it.

		if (v.charAt(0) == '\"')
		{
			int i = v.lastIndexOf('\"') ;
			if (i > 0) 
         {
            return v.substring(1,i) ;
         }
		}

		// If this is a character literal value, return it.

		if (v.charAt(0) == '\'')
		{
			int i = v.lastIndexOf('\'') ;
			if (i > 0)
         {
            String s = v.substring(1,i).toLowerCase() ;
            if ("\\n".equals(s)) return "" + '\n' ;
            if ("\\r".equals(s)) return "" + '\r' ;
            return v.substring(1,i) ;
         }
		}

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

      // If this is a (cel) group specifier, remove the group character.

      if (v.charAt(0) == '#' || v.charAt(0) == '!')
      	v = v.substring(1) ;
		
		// If this is a numeric literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
   		   try {	return Integer.valueOf(Integer.parseInt(v)) ; }
   		   catch (NumberFormatException e) { }
   		   try {	return Long.valueOf(Long.parseLong(v)) ; }
   		   catch (NumberFormatException e) { }
   		   try {	return Double.valueOf(Double.parseDouble(v)) ; }
   		   catch (NumberFormatException e) { }
         }
      }

		// Check for relative variable names.

		if (v.charAt(0) == '$')
		{
         String s = v.substring(1) ;
			Object name = getValue(s,event) ;
			if (name != null)
         {
            if (s.length() > 0 && s.charAt(0) == '#')
               v = "$#" + name.toString() ;
            else
               v = "$" + name.toString() ;
         }
         if (!OptionsDialog.getVariableCase()) v = v.toUpperCase() ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }

		// Otherwise return the variable value.

      if (variable == null) return null ;
		return variable.get(v) ;
	}


	// Variable get type.  Returns the type of the variable.
	// 0 = unknown, 1 = integer, 2 = long, 3 = double, 4 = string
	
	int getType(String v, FKissEvent event)
	{ 
		if (v == null) return 0 ;
		if (v.length() == 0) return 0 ;
      Hashtable variable = globalvariable ;

		// If this is a string literal value, return it.

		if (v.charAt(0) == '\"') return 4 ;
      
		// If this is a character literal value, return it.

		if (v.charAt(0) == '\'') return 4 ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

      // If this is a (cel) group specifier, remove the group character.

      if (v.charAt(0) == '#' || v.charAt(0) == '!')
      	v = v.substring(1) ;
		
		// If this is a numeric literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
   		   try {	Integer.parseInt(v) ; return 1 ; }
   		   catch (NumberFormatException e) { }
   		   try {	Long.parseLong(v) ; return 2 ; }
   		   catch (NumberFormatException e) { }
   		   try {	Double.parseDouble(v) ; return 3 ; }
   		   catch (NumberFormatException e) { }
         }
      }

		// Check for relative variable names.

		if (v.charAt(0) == '$')
		{
         String s = v.substring(1) ;
			Object name = getValue(s,event) ;
			if (name != null)
         {
            if (s.length() > 0 && s.charAt(0) == '#')
               v = "$#" + name.toString() ;
            else
               v = "$" + name.toString() ;
         }
         if (!OptionsDialog.getVariableCase()) v = v.toUpperCase() ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }

		// Otherwise return the variable type.

      if (variable == null) return 0 ;
		Object o = variable.get(v) ;
      if (o instanceof Integer) return 1 ;
      if (o instanceof Long) return 2 ;
      if (o instanceof Double) return 3 ;
      if (o instanceof String) return 4 ;
      return 0 ;
	}

	// Variable get type for conversion.  Checks string type for numeric
   // conversion.  0 = unknown, 1 = integer, 2 = long, 3 = double, 4 = string
	
	int getConvertType(String v, FKissEvent event)
	{ 
      int n = getType(v,event) ;
      if (n != 4) return n ;
      Object o = getValue(v,event) ;
      if (o == null) return n ;
      v = o.toString().trim() ;
		
		// If this is a numeric string, return the number type.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
   		   try {	Integer.parseInt(v) ; return 1 ; }
   		   catch (NumberFormatException e) { }
   		   try {	Long.parseLong(v) ; return 2 ; }
   		   catch (NumberFormatException e) { }
   		   try {	Double.parseDouble(v) ; return 3 ; }
   		   catch (NumberFormatException e) { }
         }
      }
      return n ;
   }


	// Integer set value.  Given a variable, set its integer value.  Integer
   // values are saved as Integer objects, thus a subsequent getValue for
   // this variable will return an Integer object.

	void setIntValue(String v, int n, FKissEvent event)
	{ 
		if (v == null || v.length() == 0) return ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// Check for relative variable names.
		
		if (v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
         String s = (name != null) ? name.toString() : "" ;
         if (!OptionsDialog.getVariableCase()) s = s.toUpperCase() ;
			if (name != null) v = "$" + s ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }

		// Save the value using the absolute variable name.  Note, event, action, 
      // and variable traces set as 'nobreakpoint' in the FKiSS editor have a 
      // sentinal '*' inserted in the text string. These trace lines are not 
      // forwarded to the trace dialog although they will appear in the log file.
		
		variable.put(v,Integer.valueOf(n)) ;
		if (OptionsDialog.getDebugVariable() && (!event.getNoBreakpoint() || OptionsDialog.getDebugDisabled())) 
      {
         String bp = (event.getNoBreakpoint()) ? "*" : " " ;
			PrintLn.println("  > [" + Thread.currentThread().getName() + "]"+bp+"Variable " + v + " set to " + n) ;
      }
	}


	// Long set value.  Given a variable, set its long value.  Long
   // values are saved as Long objects, thus a subsequent getValue for
   // this variable will return an Long object.

	void setLongValue(String v, long n, FKissEvent event)
	{ 
		if (v == null || v.length() == 0) return ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// Check for relative variable names.
		
		if (v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
         String s = (name != null) ? name.toString() : "" ;
         if (!OptionsDialog.getVariableCase()) s = s.toUpperCase() ;
			if (name != null) v = "$" + s ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }

		// Save the value using the absolute variable name.
		
		variable.put(v,Long.valueOf(n)) ;
		if (OptionsDialog.getDebugVariable() && (!event.getNoBreakpoint() || OptionsDialog.getDebugDisabled())) 
      {
         String bp = (event.getNoBreakpoint()) ? "*" : " " ;
			PrintLn.println("  > [" + Thread.currentThread().getName() + "]"+bp+"Variable " + v + " set to " + n) ;
      }
	}


	// Double set value.  Given a variable, set its double value.  Double
   // values are saved as Double objects, thus a subsequent getValue for
   // this variable will return an Double object.

	void setDoubleValue(String v, double n, FKissEvent event)
	{ 
		if (v == null || v.length() == 0) return ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// Check for relative variable names.
		
		if (v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
         String s = (name != null) ? name.toString() : "" ;
         if (!OptionsDialog.getVariableCase()) s = s.toUpperCase() ;
			if (name != null) v = "$" + s ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }

		// Save the value using the absolute variable name.
		
		variable.put(v,Double.valueOf(n)) ;
		if (OptionsDialog.getDebugVariable() && (!event.getNoBreakpoint() || OptionsDialog.getDebugDisabled())) 
      {
         String bp = (event.getNoBreakpoint()) ? "*" : " " ;
			PrintLn.println("  > [" + Thread.currentThread().getName() + "]"+bp+"Variable " + v + " set to " + n) ;
      }
	}


	// Integer variable get value.  Returns the integer value of the variable.
	// Invalid integer values return zero.
	
	int getIntValue(String v, FKissEvent event)
	{
		if (v == null) return 0 ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// If this is a literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
      		try {	return (Integer.parseInt(v)) ; }
      		catch (NumberFormatException e) { }
         }
      }
		
		// Check for relative variable names.
		
		if (v.length() > 0 && v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
			if (name != null) v = "$" + name.toString() ;
         if (!OptionsDialog.getVariableCase()) v = v.toUpperCase() ;
		}

      // Check for local variable names.

      if (v.length() > 0 && v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }
		
		// Reference the integer value using the absolute variable name.

      if (variable == null) return 0 ;
		Object o = variable.get(v) ;
      if (o == null) return 0 ;
		if (o instanceof Integer) return ((Integer) o).intValue() ;
		if (o instanceof Long) return ((Long) o).intValue() ;
		if (o instanceof Double) return ((Double) o).intValue() ;
		try {	return (Integer.parseInt(o.toString().trim())) ;	}
		catch (NumberFormatException e) { }
      return 0 ;
	}


	// Long variable get value.  Returns the long value of the variable.
	// Invalid long values return zero.
	
	long getLongValue(String v, FKissEvent event)
	{
		if (v == null) return 0 ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// If this is a literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
      		try {	return (Long.parseLong(v)) ; }
      		catch (NumberFormatException e) { }
         }
      }
		
		// Check for relative variable names.
		
		if (v.length() > 0 && v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
			if (name != null) v = "$" + name.toString() ;
         if (!OptionsDialog.getVariableCase()) v = v.toUpperCase() ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }
		
		// Reference the long value using the absolute variable name.

      if (variable == null) return 0 ;
		Object o = variable.get(v) ;
      if (o == null) return 0 ;
		if (o instanceof Integer) return ((Integer) o).longValue() ;
		if (o instanceof Long) return ((Long) o).longValue() ;
		if (o instanceof Double) return ((Double) o).longValue() ;
		try {	return (Long.parseLong(o.toString().trim())) ;	}
		catch (NumberFormatException e) { }
      return 0 ;
	}


	// Double variable get value.  Returns the double value of the variable.
	// Invalid double values return zero.
	
	double getDoubleValue(String v, FKissEvent event)
	{
		if (v == null) return 0 ;
      Hashtable variable = globalvariable ;

      // Adjust for case sensitive variables.

      if (!OptionsDialog.getVariableCase())
      	v = v.toUpperCase() ;

		// If this is a literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
      		try {	return (Double.parseDouble(v)) ; }
      		catch (NumberFormatException e) { }
         }
      }
		
		// Check for relative variable names.
		
		if (v.length() > 0 && v.charAt(0) == '$')
		{
			Object name = getValue(v.substring(1),event) ;
			if (name != null) v = "$" + name.toString() ;
         if (!OptionsDialog.getVariableCase()) v = v.toUpperCase() ;
		}

      // Check for local variable names.

      if (v.charAt(0) == '@' && event != null)
      {
         event.setTemporary(true) ;
         variable = event.getVariableTable() ;
      }
		
		// Reference the double value using the absolute variable name.

      if (variable == null) return 0 ;
		Object o = variable.get(v) ;
      if (o == null) return 0 ;
		if (o instanceof Integer) return ((Integer) o).doubleValue() ;
		if (o instanceof Long) return ((Long) o).doubleValue() ;
		if (o instanceof Double) return ((Double) o).doubleValue() ;
		try {	return (Double.parseDouble(o.toString().trim())) ;	}
		catch (NumberFormatException e) { }
      return 0 ;
	}


   // Function to remove temporary variables from the hashtable.
   // Key values for temporary variables are unique by event, call depth,
   // and scheduling thread.

   void removeTemporary(FKissEvent event)
   {
   	if (event == null) return ;
      event.setTemporary(false) ;
   }


   // Function to rename temporary variables from one event to another.
   // This is required on a goto switch to a new label event.

   void renameTemporary(FKissEvent event1, FKissEvent event2)
   {
   	if (event1 == null || event2 == null) return ;
      if (!event1.getTemporary()) return ;
      Hashtable variable1 = event1.getVariableTable() ;
      event1.setTemporary(false) ;
      event2.setTemporary(true) ;
      Hashtable variable2 = event2.getVariableTable() ;
      Enumeration enum1 = variable1.keys() ;
      while (enum1.hasMoreElements())
      {
         Object o1 = enum1.nextElement() ;
         Object o2 = variable1.get(o1) ;
         variable2.put(o1,o2) ;
      }
   }


   // Function to eliminate quotes around a string literal.

   static String getStringLiteralValue(String v)
   {
   	if (v == null) return null ;
		if (v.length() == 0) return v ;
		if (v.charAt(0) == '\"')
		{
			int i = v.lastIndexOf('\"') ;
			if (i > 0) return v.substring(1,i) ;
		}
      return v ;
   }


	// Determine if the variable is literal.  Undefined or zero length
   // variable names return true.

	static boolean isLiteral(String v)
	{ 
		if (v == null) return true ;
		if (v.length() == 0) return true ;

		// If this is a string literal value, return it.

		if (v.charAt(0) == '\"') return true ;

      // If this is a (cel) group specifier, remove the group character.

      if (v.charAt(0) == '#' || v.charAt(0) == '!')
      	v = v.substring(1) ;
		
		// If this is a numeric literal value, return it.

      if (v.length() > 0)
      {
         char c = v.charAt(0) ;
         if (c == '-' || Character.isDigit(c))
         {
   		   try {	Integer.parseInt(v) ; return true ; }
   		   catch (NumberFormatException e) { }
   		   try {	Long.parseLong(v) ; return true ; }
   		   catch (NumberFormatException e) { }
   		   try {	Double.parseDouble(v) ; return true ; }
   		   catch (NumberFormatException e) { }
         }
      }
      return false ;
	}


	// Hashtable reset.  The variable table should be cleared when
	// a new configuration is activated.
	
	void clear() { globalvariable.clear() ; }

	// Return the number of variables defined.

	int getSize() { return globalvariable.size() ; }

   // Return an enumeration of all the variable keys.

   Enumeration getVariables() { return globalvariable.keys() ; }


   // The toString method returns a string representation of this object.

   public String toString() { return "Variable Values [" + getSize() + "]" ; }
}
