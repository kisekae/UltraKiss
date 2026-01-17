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
echo "This is ultrakiss.sh script version 1.1 2026/01/15"
echo "The current date and time is $(date)"

Command=$1
Host=$2
Port=$3
SSLPort=$4
Name=$5
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")" 

if [ "$Host" = "" ]; then
  SERVER_NAME=$(hostname)
  if [ "$SERVER_NAME" = "bronzeart.ca" ]; then
    Host="wmiles.com"
  else
    Host="127.0.0.1"
  fi
fi
if [ "$Port" = "" ]; then
  Port="8080"
fi
if [ "$SSLPort" = "" ]; then
  SSLPort="8443"
fi
if [ "$Name" = "" ]; then
  Name="KisekaeUltraKissServlet-1.0.jar"
fi
echo "Script command argument (argument 1) is: \"$Command\""
echo "This script is running in directory: \"$SCRIPT_DIR\""

# The 'start' command starts the KisekaeUltraKissServlet-1.0.jar program
# to listen on ports 8080 and 8443 for incoming requests to run UltraKiss.

ExitCode=0
if [ "$Command" = "start" ]; then
  # start the servlet on port 8080
  echo "The host argument (argument 2) is: \"$Host\""
  echo "The port argument (argument 3) is: \"$Port\""
  echo "The SSL port argument (argument 4) is: \"$SSLPort\""
  echo "Java program name argument (argument 5) is: \"$Name\""
  PIDS=$(pgrep -f "$Name")
  if [[ -n "$PIDS" ]]; then
    echo "$Name is currently running."
    ExitCode=1 
  else
    java -jar $Name "$Host" "$Port" "$SSLPort" "$SCRIPT_DIR" &
    SERVLETPID=$!
    echo "$Name started on PID $SERVLETPID and is now listening on port $Port and $SSLPort."
  fi

# The 'stop' command terminates the KisekaeUltraKissServlet-1.0.jar program.
# Listening on ports 8080 and 8443 is terminated.  All active sessions of 
# UltraKiss are also terminated.

elif [ "$Command" = "stop" ]; then
  # stop the servlet on port 8080
  echo "Java program name argument (argument 5) is: \"$Name\""
  echo "Searching for $Name running as an active process ..."
  PIDS=$(pgrep -f "$Name")
  if [[ -n "$PIDS" ]]; then
    echo "Found PIDs: $PIDS"
    # Iterate over all found PIDs (pgrep outputs PIDs separated by newlines or spaces)
    for PID in $PIDS; do
      echo "Stopping PID: $PID"
      # Example: kill the process
      kill "$PID"
    done
    echo "Stopping all active UltraKiss sessions."
    PID_FILE="pid.txt"
    if [ -e $PID_FILE ]; then
      # Read the file line by line even if no terminating newline
      while IFS= read -r line || [[ -n "$line" ]]; 
      do
        echo "read pid.txt line $line"
        # Remove leading/trailing whitespace and ensure it's a number
        FIRST_TOKEN=$(echo "$line" | awk '{print $1}')
        echo "kill $FIRST_TOKEN"
        if ! [[ "$FIRST_TOKEN" =~ ^[0-9]+$ ]]; then
            if [ -n "$FIRST_TOKEN" ]; then # Only show error for non-empty lines
                echo "Warning: Invalid PID '$FIRST_TOKEN' in file, skipping."
            fi
            continue
        fi  
        kill $FIRST_TOKEN
      done < "$PID_FILE"
    fi
    rm -f "pid.txt"
  else
    echo "No matching processes found for $Name."
    ExitCode=1
  fi

# The 'status' command determines if the KisekaeUltraKissServlet-1.0.jar program
# is running. The number of currently active UltraKiss sessions is also reported.

elif [ "$Command" = "status" ]; then
  # show the running status of the servlet
  echo "Java program name argument (argument 5) is: \"$Name\""
  PIDS=$(pgrep -f "$Name")
  if [[ -n "$PIDS" ]]; then
    echo "$Name is running on process ID $PIDS"

    PID_FILE="pid.txt"
    if [ -e $PID_FILE ]; then
      linecount=0

      # Read the file line by line
      while IFS= read -r line || [[ -n "$line" ]]; 
      do
        # Remove leading/trailing whitespace and ensure it's a number
        read -r pid rest <<< "$line"
        if ! [[ "$pid" =~ ^[0-9]+$ ]]; then
            if [ -n "$pid" ]; then # Only show error for non-empty lines
                echo "Warning: Invalid PID '$pid' in file, skipping."
            fi
            continue
        fi

        # Check if the process is running
        # We use ps --pid "$pid" and check its exit status ($?)
        # Redirecting output to /dev/null keeps the script's output clean
        if ps --pid "$pid" > /dev/null; then
            read -r pidport _ <<< "$rest"
            echo "UltraKiss PID $pid is running on port $pidport."
            ((linecount++))
        else
            echo "UltraKiss PID $pid: NOT RUNNING."
        fi
      done < "$PID_FILE"
      echo "$linecount sessions of UltraKiss are currently active."
    else
      echo "No sessions of UltraKiss are currently active."
    fi
  else
    echo "No matching processes found. $Name is not running."
    ExitCode=1
  fi

# The 'ports' command lists the process IDs of currently active UltraKiss
# sessions.  These sessions are reported from the "pid.txt" file.

elif [ "$Command" = "ports" ]; then
  # list the UltraKiss ports in use
  echo "Looking for file \"pid.txt\" in directory $SCRIPT_DIR"
  if [ -e "pid.txt" ]; then
    echo "PID  Port"
    cat "pid.txt"
  else
    echo "File \"pid.txt\" not found in directory $SCRIPT_DIR"
    ExitCode=1
  fi

# Unknown commands.

else
  echo "Unknown command: \"$Command\""
  echo "Valid commands are: start, stop, status, ports"
  ExitCode=1
fi

echo "ultrakiss.sh terminates."
exit $ExitCode




