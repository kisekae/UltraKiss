#!/bin/bash

# Title:        Kisekae UltraKiss
# Version:      5.0  (December 31, 2025)
# Copyright:    Copyright (c) 2002-2026
# Author:       William Miles
# Description:  Kisekae Set System
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
#
#
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
#  This copyright notice and this permission notice shall be included in      %
#  all copies or substantial portions of UltraKiss.                           %
#                                                                             %
#  The software is provided "as is", without warranty of any kind, express or %
#  implied, including but not limited to the warranties of merchantability,   %
#  fitness for a particular purpose and noninfringement.  In no event shall   %
#  William Miles be liable for any claim, damages or other liability,         %
#  whether in an action of contract, tort or otherwise, arising from, out of  %
#  or in connection with Kisekae UltraKiss or the use of UltraKiss.           %
#                                                                             %
#  William Miles                                                              %
#  144 Oakmount Rd. S.W.                                                      %
#  Calgary, Alberta                                                           %
#  Canada  T2V 4X4                                                            %
#                                                                            %
#  w.miles@wmiles.com                                                         %
#                                                                             %
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

echo " "
echo "This is startUltraKiss.sh script version 1.1 2026/01/15"
echo "The current date and time is $(date)"
echo "Display argument (argument 1) is $1"
echo "Port argument (argument 2) is $2"
echo "SSL argument (argument 3) is $3"

SCRIPT_DIR="$(dirname "$(readlink -f "$0")")" 
Directory="$SCRIPT_DIR"
Application="KisekaeUltraKiss-5.0.jar"

echo "Home directory is $Directory"
cd $Directory

echo "Starting Xvfd and kwin_x11 if not running on display $1"
if xdpyinfo -display :$1 > /dev/null 2>&1
then
   echo "Xvfb is running on display $1" 
   XVFB_PID=$(pgrep -f "Xvfb :$1")
   KWIN_PID=$XVFB_PID
else
   Xvfb :$1 -screen 0 1280x1024x24 &
   XVFB_PID=$!
   if ps -p $XVFB_PID > /dev/null
   then
      echo "Xvfb started in background on display $1 with PID $XVFB_PID"
      sleep 2
      echo "Starting kwin_x11 on display $1"
      DISPLAY=:$1 kwin_x11 --replace > /dev/null 2>&1 &
      KWIN_PID=$!

      if ps -p $KWIN_PID > /dev/null
      then
         echo "kwin_x11 started in background on display $1 with PID $KWIN_PID"
      else 
         echo "kwin_x11 on display $1 failed to start."
         echo "Continuing without a display manager."
         unset KWIN_PID
      fi
   else
      echo "Xvfb on display $1 failed to start."
      exit 1
   fi
#  Wait until the process is up and running.
   sleep 2
fi

if [ -z "${KWIN_PID}" ];
then
    echo "Xvfb is now running in the background. Proceeding with other commands."
else
    echo "Xvfb and kwin_x11 are now running in the background. Proceeding with other commands."
fi

# Perform housekeeping on log files.
WEBSOCKET_FILE="$SCRIPT_DIR/WebSocket.log"
if [ -f "$WEBSOCKET_FILE" ];
then
   echo "Clean WebSocket log file $WEBSOCKET_FILE"
   tail -n 100 $WEBSOCKET_FILE > tmpfile
   mv tmpfile $WEBSOCKET_FILE
else
   echo "WebSocket log file $WEBSOCKET_FILE does not exist."
fi

echo "Starting $Application on display $1"
DISPLAY=:$1 java -XX:MaxRAMPercentage=50 -jar $Application "portal" "" "" "" "" "" "$2" "$3" > server$2.log 2>&1 &
ULTRAKISS_PID=$!

if ps -p $ULTRAKISS_PID > /dev/null
then
   echo "UltraKiss started in background on display $1 with PID $ULTRAKISS_PID" on port $2 
else
   echo "$Directory/$Application failed to start."
   exit 1
fi

# keep track of all concurrent running PID and port
# Record format: ULTRAKISS_PID, Port, KWIN_PID, XVFB_PID
echo "$ULTRAKISS_PID $2 $KWIN_PID" $XVFB_PID >> pid.txt
echo "UltraKiss PID $ULTRAKISS_PID is added to \"pid.txt\" file as a running process."
echo "KWIN_PID is $KWIN_PID and XVFB_PID is $XVFB_PID."

# Wait until the process is up and running.
sleep 2
echo "UltraKiss has now started on port $2. Proceeding with other commands."
