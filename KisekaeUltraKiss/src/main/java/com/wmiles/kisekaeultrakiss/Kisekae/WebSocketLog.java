package com.wmiles.kisekaeultrakiss.Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      4.0.2  (April 4, 2025)
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



import java.io.*;
import java.util.Date ;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.concurrent.TimeUnit;


final public class WebSocketLog extends PrintStream
{
	private static String websocketlogname = "WebSocket.log" ;
   private static PrintWriter w = null;

	// Constructor
	
	public WebSocketLog(PrintStream ps) 
	{	
      super(ps) ; 
   }

   // Create and write session log record.
   
	public static void write(Date begindate, int sets, String lastset, String lastzip, String url, String logfilename) 
	{
      try 
      {
         if (sets == 0) return ;
         if (begindate == null) return ;
         
         w = new PrintWriter(new BufferedWriter
            (new FileWriter(websocketlogname, true)));

			Date enddate = new Date() ;
         long duration = getDateDiff(begindate,enddate,TimeUnit.MINUTES) ;
         File f = new File(lastzip) ;
         if (url!= null && url.startsWith("jar:") && url.contains("!")) 
            url = url.substring(url.indexOf('!')) ;
			String s = "Websocket session on " + begindate.toString() ;
         s += " for " + duration + " minutes, sets loaded " + sets ;
         if (lastset != null && lastset.length() > 0) 
            s += " last set was " + lastset + ", in archive " + f.getName() ;
         if (url != null && url.length() > 0)
            s += ", downloaded from " + url ;
         if (logfilename != null && logfilename.length() > 0)
            s += ", logfile " + logfilename ;
         w.println(s);
      }

      catch (IOException ex) 
      {
        PrintLn.println("WebsocketLog: " + ex.getMessage()) ;
      }

      finally 
      {
         if (w != null)  w.close();
      }
   }

   /**
    * Get a diff between two dates
    * @param date1 the oldest date
    * @param date2 the newest date
    * @param timeUnit the unit in which you want the diff
    * @return the diff value, in the provided unit
    */
   public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
       long diffInMillies = date2.getTime() - date1.getTime();
       return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
   }
}
