// Title:        Kisekae UltraKiss
// Version:      5.0 (December 25, 2025)
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


// This ScreenCapture class is the interface into the Java Robot system.
// It is used for all transfer of data from the robot into the application.

package com.wmiles.kisekaeultrakiss.WebSocket;

import com.wmiles.kisekaeultrakiss.Kisekae.Kisekae;
import com.wmiles.kisekaeultrakiss.Kisekae.OptionsDialog;
import com.wmiles.kisekaeultrakiss.Kisekae.PrintLn;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCapture 
{
   private static Robot robot = null ;
   static Rectangle screenRect = new Rectangle() ;
   
   public static BufferedImage captureScreenImage() throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      Rectangle r = new Rectangle(Kisekae.getScreenSize());
      if (OptionsDialog.getDebugWebSocket() && (r.width != screenRect.width || r.height != screenRect.height))
         PrintLn.println("ScreenCapture: new screen size " + r.width + " " + r.height);
      screenRect = r ;
      BufferedImage screenFullImage = robot.createScreenCapture(r);
      return screenFullImage;
   }
   
   public static void mouseMove(int X, int Y) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      robot.mouseMove(X, Y) ;
   }
   
   public static void mouseUp(int X, int Y, int button) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      if (button == 1) robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
      if (button == 2) robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
      if (button == 3) robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
   }
   
   public static void mouseDown(int X, int Y, int button) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      if (button == 1) robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
      if (button == 2) robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
      if (button == 3) robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
   }
   
   public static void mouseWheel(int X, int Y, int mode, boolean ctrl) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      Y = (Y < 0) ? -1 : 1 ;           // scrolling sensitivity
      robot.mouseWheel(Y);
   }
   
   public static void keyPress(int key) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      robot.keyPress(key);
   }
   
   public static void keyRelease(int key) throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      robot.keyRelease(key);
   }}