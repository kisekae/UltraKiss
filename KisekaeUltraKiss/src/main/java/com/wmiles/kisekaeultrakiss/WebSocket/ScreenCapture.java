/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wmiles.kisekaeultrakiss.WebSocket;

import com.wmiles.kisekaeultrakiss.Kisekae.Kisekae;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScreenCapture 
{
   private static Robot robot = null ;
   static Rectangle r = new Rectangle() ;
   
   public static BufferedImage captureScreenImage() throws AWTException, IOException 
   {
      if (robot == null) robot = new Robot() ;
      Rectangle screenRect = new Rectangle(Kisekae.getScreenSize());
      BufferedImage screenFullImage = robot.createScreenCapture(screenRect);
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