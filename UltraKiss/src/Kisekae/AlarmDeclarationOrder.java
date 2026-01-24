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
* AlarmDeclarationOrder class
*
* Purpose:
*
* This class compares Alarm objects to determine their declaration order
* in the CNF.  Apparently PlayFKiSS fires identical time alarms in this
* order, probably because it scanned the alarm list to find alarms that
* needed to be fired.
*
* This comparator is used by FKissEvent to properly sequence event initiated
* alarms before they are put on the AlarmTimer queue.
*
*/


import java.util.* ;

final class AlarmDeclarationOrder implements Comparator
{
   private boolean ascending ;
   private Vector alarmlist = null ;

   public AlarmDeclarationOrder() { this(true) ; }
   public AlarmDeclarationOrder(boolean ascending)
   {
      this.ascending = ascending ;
      MainFrame mf = Kisekae.getMainFrame() ;
      Configuration config = (mf != null) ? mf.getConfig() : null ;
      alarmlist = (config !=  null) ? config.getAlarms() : null ;
   }
 
   public int compare(Object o1, Object o2)
   {
      // compare trigger times based on the event firing time and delays
   
      if (!(o1 instanceof Alarm)) return 0 ;
      if (!(o2 instanceof Alarm)) return 0 ;
      long n1 = ((Alarm) o1).getTriggeredTime() ;
      long n2 = ((Alarm) o2).getTriggeredTime() ;
      if (n1 < n2) return (ascending) ? -1 : 1 ;
      if (n1 > n2) return (ascending) ? 1 : -1 ;
      
      // for equivalent delays in the same event compare on the alarm
      // declaration order in the CNF.  
      
      if (alarmlist == null) return 0 ;
      n1 = alarmlist.indexOf(o1) ;
      n2 = alarmlist.indexOf(o2) ;
      if (n1 < n2) return (ascending) ? -1 : 1 ;
      if (n1 > n2) return (ascending) ? 1 : -1 ;
      return 0 ;
   }
}
