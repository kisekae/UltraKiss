package Seigen ;

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




import java.net.* ;
import java.util.* ;
import java.text.DateFormat ;

/**                     Kisekae UltraKiss Version 1.0
 *
 * This is the registration module for Kisekae UltraKiss.  This is the
 * default module shipped for free installations.  The registered versions
 * will use a new class that contains additional functions.  Introspection
 * is used by the main UltraKiss program to determine if authorized
 * features are available.
 *
 */


public final class Seigen extends Authenticator
{
   private static String copyright =            	// Copyright text
      "Kisekae UltraKiss V4.0 (c) 2002-2025 William Miles" ;

   private static Calendar warningdate = Calendar.getInstance() ;
   private static Calendar restrictdate = Calendar.getInstance() ;
   private static Calendar expiredate = Calendar.getInstance() ;

   private static String user = "" ;
   private static String password = "" ;
   private static String serialnumber = "0000000000" ;
   private static String registration = "0000000000" ;
   private static boolean debug = false ;


   // Utility functions to return copyright information.

   public static String getCopyright() { return copyright ; }


   // Constructor

   public Seigen() { debug = false ; }
   public Seigen(boolean b) { debug = b ; }


   // Utility functions to return authorization date information.

   public static Calendar getWarningDate()
   {
      warningdate.set(2099,Calendar.DECEMBER,31) ;
      return warningdate ;
   }

   public static Calendar getExpireDate()
   {
      expiredate.set(2099,Calendar.DECEMBER,31) ;
      return expiredate ;
   }

   public static Calendar getRestrictDate()
   {
      restrictdate.set(2099,Calendar.DECEMBER,31) ;
      return restrictdate ;
   }


   // Utility functions to return user information.

   public static String getUser() 
   { 
      if (user == "") user = System.getProperty("user.name");
      return b64encode(user) ; 
   }
   public static String getPassword() { return b64encode(password) ; }
   public static String getUnencodedUser() 
   { 
      if (user == "") user = System.getProperty("user.name");
      return user ; 
   }
   public static String getConnectionID() 
   { 
      if (user == null || user.length() == 0) return null ;
      return b64encode(user+':'+password) ; 
   }


   // Utility functions to set user information.

   public static void setUser(String s) { user = s ; }
   public static void setPassword(String s) { password = s ; }


   // Utility functions to return serial information.

   public static String getSerialNumber() { return serialnumber ; }
   public static String getRegistration() { return registration ; }


   // Authenticator implementation.

   public PasswordAuthentication getPasswordAuthentication()
   {
      return new PasswordAuthentication(user,password.toCharArray()) ;
   }

   // Utility functions.

   private static String b64encode(String s)
   {
      if (s == null || s.length() == 0) return "" ;
      try 
      { 
         byte[] encoded = Base64.getEncoder().encode(s.getBytes()) ; 
         return Arrays.toString(encoded) ;
      }
      catch (SecurityException e) { }
      return "" ;
   }
}

