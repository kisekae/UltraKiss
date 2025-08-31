package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      4.0  (January 5, 2025)
// Copyright:    Copyright (c) 2002-2025
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



import java.io.*;

final public class PrintLn 
{  
   private static boolean tracewrite = false ;
   
   // System.out.println() replacement to write to logfile if webswing.

   public static void println(String line) 
   {
      if (line == null) return ;

      if (Kisekae.isWebswing())
      {     
         try
         {
            OutputStream logfile = Kisekae.getLogFile() ;
            if (logfile != null)
            {
         		String ls = System.getProperty("line.separator") ;
         		if (ls != null) line += ls ;
         		logfile.write(line.getBytes()) ;
               
               // Trace output
               
               MainFrame mf = Kisekae.getMainFrame() ;
               FKissTrace trace = (mf != null) ? mf.getTrace() : null ;
               if (trace != null) 
               {
                  String s = new String(line) ;
                  for (int i = 0 ; i < s.length() ; i++)
                     if (!Character.isISOControl(s.charAt(i))) 
                        { tracewrite = true ; break ; }
                  if (s.indexOf("*[") >= 0) tracewrite = false ; 
                  if (s.indexOf(">*") >= 0) tracewrite = false ;
                  if (tracewrite) trace.write(s) ; 
               }
            }
         }
         catch (Exception e)
         {
            System.out.println("PrintLn: " + e.toString()) ; 
         }
      }
      else
         System.out.println(line) ;          
   }
   
   public static void print(String line) 
   {
      if (line == null) return ;

      if (Kisekae.isWebswing())
      {     
         try
         {
            OutputStream logfile = Kisekae.getLogFile() ;
            if (logfile != null)
            {
         		logfile.write(line.getBytes()) ;
            }
         }
         catch (Exception e)
         {
            System.out.println("PrintLn: " + e.toString()) ; 
         }
      }
      else
         System.out.print(line) ; 
   }
   
   public static void printErr(String line) 
   {
      if (line == null) return ;

      if (Kisekae.isWebswing())
      {     
         try
         {
            OutputStream logfile = Kisekae.getLogFile() ;
            if (logfile != null)
            {
         		String ls = System.getProperty("line.separator") ;
         		if (ls != null) line += ls ;
         		logfile.write(line.getBytes()) ;
            }
         }
         catch (Exception e)
         {
            System.err.println("PrintLn: " + e.toString()) ; 
         }
      }
      else
         System.err.println(line) ;          
   }
}
