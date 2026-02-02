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

package com.wmiles.kisekaeultrakiss.WebSocket;

/**
 *
 * @author william
 */
import com.wmiles.kisekaeultrakiss.Kisekae.Audio;
import com.wmiles.kisekaeultrakiss.Kisekae.ColorFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.ImageFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.Kisekae;
import com.wmiles.kisekaeultrakiss.Kisekae.KissFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.MainFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.MediaFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.OptionsDialog;
import com.wmiles.kisekaeultrakiss.Kisekae.TextFrame;
import com.wmiles.kisekaeultrakiss.Kisekae.PrintLn;
import com.wmiles.kisekaeultrakiss.Kisekae.ZipManager;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(autoDemand = true)
public class JettyWebSocketEndpoint 
{
   private static int id = 0 ;
   private static long timeout = 5*60*1000 ;   // 5 minute session timeout
   private static long timeoutstart = 0 ;      // Start timeout time
   private boolean warningsent = false ;       // timeout warning sent
   private static int busytimeout = 300 ;      // partial binary send timeout  
   private static int busywait = 10 ;          // sleep time while waiting
   private long createtime = 0 ;
   private long time = 0 ;
	private Session session ;                   // set on websocket open
   private Thread processThread ;              // sends screen image to client
   private Thread timeoutThread ;              // watches for no activity
	private static ArrayList<JettyWebSocketEndpoint> sessions = new ArrayList<JettyWebSocketEndpoint>();
   private final Map<Session, FileUploadClass> fosMap = Collections.synchronizedMap(new HashMap<>());
   private final Queue<String> queue = new LinkedList<>();
   private final byte[] imageheader = {0} ;    // binary type for image transfer
   private final byte[] fileheader = {1} ;     // binary type for file transfer
   private final byte[] soundheader = {2} ;    // binary type for audio transfer
   private boolean socketBusy = false ;   // Set true if file transfer to client
   private boolean bufferBusy = false ;   // Set true if partial buffer is sent
   private boolean audioIsPlaying = false ;   // Set true audio sent and no stop

   
   /*
   * WebSocket functions
   */
   
   @OnWebSocketOpen
   public void onOpen(Session session) 
   {
   	this.session = session;
    	sessions.add(this);   
      JettyServer server = Kisekae.getServer() ;
      createtime = server.getCreateTime() ;
		time = System.currentTimeMillis() - createtime ;
      System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: connect to " + session.getRemoteSocketAddress());
      processThread = new ProcessThread() ;
      processThread.setName("Screen Capture " + (id++)) ;
      processThread.start() ;      
      timeoutThread = new TimeoutThread() ;
      timeoutThread.setName("Timeout Thread") ;
      timeoutThread.start() ;      
   }

   // Text messages
   @OnWebSocketMessage
   public void onMessage(String message) 
   {
     	if (session == null) return ;
		time = System.currentTimeMillis() - createtime ;
      
      if (!(message.startsWith("mouse") || message.startsWith("cursor")))
      {
         if (OptionsDialog.getDebugWebSocket() || (message.startsWith("file") 
                 || message.startsWith("close") || message.startsWith("screen")
                 || message.startsWith("retransmit") || message.startsWith("notify")))
         {
            System.out.println("[" + time + "] "+"Message receive: " + message);
         }
      }
      
      // Resource image url  "image /Image/name.png"
      if (message.startsWith("image")) 
      {
   		try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 1) return ;
            URL imageUrl = Kisekae.getResource(tokens[1]) ;
            if (imageUrl != null)
            {
               BufferedImage bi = ImageIO.read(imageUrl);
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               ImageIO.write(bi, "png", out);
               ByteBuffer byteBuffer = ByteBuffer.wrap(addHeader(imageheader,out.toByteArray()));
               session.sendBinary(byteBuffer, Callback.NOOP);
               out.close();
               byteBuffer.clear();
            }
   		} 
         catch (IOException e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: image sendBinary exception: " + e.getMessage());   
  				e.printStackTrace();
			}
     	}
        
      // robot full screen capture
      if (message.equals("capture")) 
      {
			try 
         {
				BufferedImage bi = ScreenCapture.captureScreenImage() ;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ImageIO.write(bi, "png", out);
				ByteBuffer byteBuffer = ByteBuffer.wrap(addHeader(imageheader,out.toByteArray()));
				session.sendBinary(byteBuffer, Callback.NOOP);
				out.close();
				byteBuffer.clear();
			} 
         catch (Exception e) 
         {
            if (e instanceof AWTException)
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: screen capture exception: " + e.getMessage());   
            else
 					e.printStackTrace();
			}
      }    
            
      // request client close websocket "close reason"
      if (message.startsWith("close")) 
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 1) return ;
            if ("window".equals(tokens[1]))
            {
               MainFrame mainframe = Kisekae.getMainFrame() ;
               if (mainframe != null) 
               {
                  Runnable runner = new Runnable()
                  { public void run() { mainframe.close() ; } } ;
                  javax.swing.SwingUtilities.invokeLater(runner) ; 
               }
               else
               {
                  closeWebSocket() ;
                  exit(1) ;
               }
            }
            if ("close".equals(tokens[0]))
            {
               send(message) ;            // ask client to close socket
            }
			} 
         catch (Exception e) 
         {
            if (e instanceof AWTException)
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: close exception: " + e.getMessage());   
            else
 					e.printStackTrace();
			}
      }
            
      // Kisekae screen dimension  "screen width height"
      if (message.startsWith("screen")) 
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 2) return ;
            int width = Integer.parseInt(tokens[1]) ;
            int height = Integer.parseInt(tokens[2]) ;
            Kisekae.setScreenSize(new Dimension(width,height)) ;
            Kisekae.setClientScreen();
			} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: screen dimensions exception: " + e.getMessage());   
        	}
      }    
 
      // robot mouse movements  "mousemove X Y"
      if (message.startsWith("mousemove"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 2) return ;
            int X = Integer.parseInt(tokens[1]) ;
            int Y = Integer.parseInt(tokens[2]) ;
            ScreenCapture.mouseMove(X,Y);
            setTimeoutStart() ;
			} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: mouse move exception: " + e.getMessage());   
         }
      }
 
      // robot mouse movements  "mouseup X Y button"
      if (message.startsWith("mouseup"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 2) return ;
            int X = Integer.parseInt(tokens[1]) ;
            int Y = Integer.parseInt(tokens[2]) ;
            int button = Integer.parseInt(tokens[3]) ;
            ScreenCapture.mouseUp(X,Y,button);
            setTimeoutStart() ;
			} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: mouse up exception: " + e.getMessage());   
         }
      }
 
      // robot mouse movements  "mousedown X Y button"
      if (message.startsWith("mousedown"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 3) return ;
            int X = Integer.parseInt(tokens[1]) ;
            int Y = Integer.parseInt(tokens[2]) ;
            int button = Integer.parseInt(tokens[3]) ;
            ScreenCapture.mouseDown(X,Y,button);
            setTimeoutStart() ;
   		} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: mouse down exception: " + e.getMessage());   
         }
      }
 
      // robot mouse movements  "mousewheel X Y mode ctrl"
      if (message.startsWith("mousewheel"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 4) return ;
            int X = Integer.parseInt(tokens[1]) ;
            int Y = Integer.parseInt(tokens[2]) ;
            int mode = Integer.parseInt(tokens[3]) ;
            boolean ctrl = Boolean.parseBoolean(tokens[4]) ;
            ScreenCapture.mouseWheel(X,Y,mode,ctrl);
            setTimeoutStart() ;
			} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: mouse wheel exception: " + e.getMessage());   
         }
      }
    
      // robot mouse movements  "keypress keycode"
      if (message.startsWith("keypress"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 1) return ;
            Object o = KeyCodeMapper.getJavaKeyCode(tokens[1]) ;
            if (!(o instanceof KeyCodeMapper.KeyClass)) 
               throw new Exception(tokens[1] + "is Undefined key") ;
            if (((KeyCodeMapper.KeyClass) o).shift)
            {
               ScreenCapture.keyPress(KeyEvent.VK_SHIFT) ;
               ScreenCapture.keyPress(((KeyCodeMapper.KeyClass) o).key);
            }
            else
               ScreenCapture.keyPress(((KeyCodeMapper.KeyClass) o).key);
            setTimeoutStart() ;
   		} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: key press exception: " + e.getMessage());   
         }
      }
    
      // robot mouse movements  "keyrelease keycode"
      if (message.startsWith("keyrelease"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            Object o = KeyCodeMapper.getJavaKeyCode(tokens[1]) ;
            if (tokens.length < 1) return ;
            if (!(o instanceof KeyCodeMapper.KeyClass)) 
               throw new Exception(tokens[1] + "is Undefined key") ;
            if (((KeyCodeMapper.KeyClass) o).shift)
            {
               ScreenCapture.keyRelease(((KeyCodeMapper.KeyClass) o).key);
               ScreenCapture.keyRelease(KeyEvent.VK_SHIFT) ;
            }
            else
               ScreenCapture.keyRelease(((KeyCodeMapper.KeyClass) o).key);
            setTimeoutStart() ;
			} 
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: key release exception: " + e.getMessage());   
         }
      }
       
      // fileupload filename size source
      if (message.startsWith("fileupload"))
      {
			try 
         {
            MainFrame mf = Kisekae.getMainFrame() ;
            String [] tokens = message.split(" ") ;
            if ("fileupload".equals(tokens[0]))
            {
               if (tokens.length < 1) return ;
               String filename = tokens[1] ;
               int n = filename.lastIndexOf('.') ;
               String ext = (n > 0) ? filename.substring(n) : "" ;
               File f = File.createTempFile("UltraKiss-", ext) ;
               FileOutputStream os = new FileOutputStream(f) ;
               fosMap.put(session, new FileUploadClass(f,os)) ;               
               if (OptionsDialog.getDebugWebSocket())
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload to server start, " + f.getPath()) ;
               if (mf != null) mf.showStatus("WebSocket downloading file " + filename + " from client browser.") ;
            }
            if ("fileuploadend".equals(tokens[0]))
            {
               if (tokens.length < 3) return ;
               String filename = tokens[1] ;   
               FileUploadClass attributes = fosMap.get(session);
               if (OptionsDialog.getDebugWebSocket())
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload to server end, " + filename) ;
               if (mf != null) mf.showStatus(null) ;
               if (attributes == null) throw new Exception("No file attributes for session " + session) ;
               File f = attributes.file ;    
               FileOutputStream os = attributes.os ;
               os.close() ;
               
               // Rename temporary file to original file name.
               
               String parent = f.getParent() ;
               File rename = new File(parent,filename) ;
               boolean b = f.renameTo(rename) ;
               if (b) attributes.file = rename ;
               if (OptionsDialog.getDebugWebSocket())
               {
                  if (b) System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: " + f.getPath() + " successfully renamed to " + attributes.file.getPath()) ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload success, " + attributes.file.getPath()) ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload size, " + tokens[2]) ;
               }
         
               // Open file in UltraKiss.  Run this on the EDT thread.

               int source = Integer.parseInt(tokens[3]) ;
               String editimport = (tokens.length > 4) ? tokens[4] : "" ;
               KissFrame frame = KissFrame.getFrameByIdentifier(source) ;
               String frameclass = (frame != null) ? frame.getClass().getSimpleName() : "Unknown" ;
               if (OptionsDialog.getDebugWebSocket())
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload parent frame is " + frameclass + " (" + editimport + ")") ;
               
        			Runnable awt = new Runnable()
        			{ 
                  public void run() 
                  { 
                     if ("MainFrame".equals(frameclass))
                     {
                        if ("import".equals(editimport))
                           Kisekae.importfile(attributes.file.getPath()) ; 
                        else if ("importpalette".equals(editimport))
                           Kisekae.importcelpalette(attributes.file.getPath()) ; 
                        else
                           Kisekae.loadfile(attributes.file.getPath()) ; 
                     }
                     if ("TextFrame".equals(frameclass))
                        ((TextFrame) frame).loadfile(attributes.file.getPath()) ; 
                     if ("ImageFrame".equals(frameclass))
                        ((ImageFrame) frame).loadfile(attributes.file.getPath()) ; 
                     if ("ColorFrame".equals(frameclass))
                        ((ColorFrame) frame).loadfile(attributes.file.getPath()) ; 
                     if ("ZipManager".equals(frameclass))
                        ((ZipManager) frame).loadfile(attributes.file.getPath()) ; 
                     if ("MediaFrame".equals(frameclass))
                        ((MediaFrame) frame).loadfile(attributes.file.getPath()) ; 
                  } 
               } ;
        			SwingUtilities.invokeLater(awt) ;
            }
            if ("fileuploadlogfileend".equals(tokens[0]))
            {
               if (tokens.length < 1) return ;
               String filename = tokens[1] ;   
               FileUploadClass attributes = fosMap.get(session);
               if (OptionsDialog.getDebugWebSocket())
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: upload logfile to server end, " + filename) ;
               if (mf != null) mf.showStatus(null) ;
               if (attributes == null) throw new Exception("No logfile attributes for session " + session) ;
               File f = attributes.file ;    
               FileOutputStream os = attributes.os ;
               os.close() ;
               if (OptionsDialog.getDebugWebSocket())
               {
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: logfile upload success, " + f.getPath()) ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: logfile upload size, " + tokens[2]) ;
               }
         
               // Save the log file to the server.

               File destination = new File(filename) ;
               if (destination.exists()) destination.delete() ;
               destination.createNewFile() ;
               InputStream in = new FileInputStream(f) ;
               OutputStream out = new FileOutputStream(destination) ;
               byte[] buffer = new byte[1024] ;
               int length;
               while ((length = in.read(buffer)) > 0) {
                  out.write(buffer, 0, length);
               }                  
               if (OptionsDialog.getDebugWebSocket())
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: logfile " + filename + " successfully saved.");
            }
         }
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: file upload exception: " + e.getMessage());   
         }
      }
       
      // audiostop id filename
      if (message.startsWith("audiostop"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 2) return ;
            String filename = tokens[2] ;
            int source = Integer.parseInt(tokens[1]) ;
            Audio audio = Audio.getAudioByIdentifier(source) ;
            String audioclass = (audio != null) ? audio.getClass().getSimpleName() : "Unknown" ;
            String name = (audio != null) ? audio.getPublicName() : null ;
            name = (name != null) ? removeSpaces(name,"_") : "Unknown" ;
            if (OptionsDialog.getDebugWebSocket())
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: audio stop, parent class is " + audioclass + " for " + name) ;
               
     			Runnable awt = new Runnable()
     			{ 
               public void run() 
               { 
                  // fire an event so audio listeners recognizes the stop.
                  if (audio != null) audio.sendStopEvent() ;
                  audioIsPlaying = false ;
               } 
            } ;
     			SwingUtilities.invokeLater(awt) ;
         }
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: audio stop exception: " + e.getMessage());   
         }
      }
      
       
      // retransmit id filename
      if (message.startsWith("retransmit"))
      {
			try 
         {
            String [] tokens = message.split(" ") ;
            if (tokens.length < 2) return ;
            String filename = tokens[2] ;
            int source = Integer.parseInt(tokens[1]) ;
            Audio audio = Audio.getAudioByIdentifier(source) ;
            if (audio == null) 
            {
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: audio retransmit ignored, + cannot identify object for " + filename) ;
               return ;
            }
            
            String audioclass = audio.getClass().getSimpleName() ;
            String name = audio.getPublicName() ;
            name = (name != null) ? removeSpaces(name,"_") : "Unknown" ;
            if (OptionsDialog.getDebugWebSocket())
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: audio retransmit, parent class is " + audioclass + " for " + name) ;
            
            int size = audio.getBytes() ;
            send("audioopen " + source + " " + name + " " + size) ;
            sendAudio(audio.getInputStream(),name,size) ;
         }
         catch (Exception e) 
         {
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: retransmit exception: " + e.getMessage());   
         }
         
      }     
   }
   
   /*
    * We receive binary data when the client is sending a file.  These files
    * are typically LZH or ZIP files or TXT or other images intended to be
    * opened by UltraKiss.  The data is written to a temporary file created
    * on the 'fileupload filename size source' text message.  The file output
    * stream is stored in an attribute object for this temporary file.
    */
   
   @OnWebSocketMessage
   public void onWebSocketBinary(ByteBuffer message, Callback callback)
   {
		time = System.currentTimeMillis() - createtime ;
      try
      {
         // Handle binary chunks
         byte[] data = new byte[message.remaining()];
         message.get(data);
         if (OptionsDialog.getDebugWebSocket())
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: Received binary data length: " + data.length);
         FileUploadClass attributes = fosMap.get(session);
         if (attributes != null) 
         {
            // Write the received bytes to the file
            FileOutputStream os = attributes.os ;
            os.write(data, 0, data.length);
         }
         callback.succeed() ;
      }
      catch (Exception e) 
      {
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: receive binary message exception " + e.getMessage());   
         callback.fail(e);
      }
    }

   
   
   @OnWebSocketClose
   public void onClose(Session session, int statusCode, String reason) 
   {
      sessions.remove(this);
      System.out.println("JettyWebSocketEndpoint: OnClose statusCode = " + statusCode + ", reason = " + reason + ", sessions = " + sessions.size());
      if (processThread != null) processThread.interrupt() ;
      if (timeoutThread != null) timeoutThread.interrupt() ;
      try { Thread.currentThread().sleep(200) ; } catch (InterruptedException e) { }
      Kisekae.exit() ;
   }

   @OnWebSocketError
   public void onError(Throwable t) 
   {
		time = System.currentTimeMillis() - createtime ;
      System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: OnError exception " + t.getMessage());
      onClose(null,0,t.getMessage()) ;
   }

   
   /* 
   * Miscellaneous functions
   */

	public static ArrayList<JettyWebSocketEndpoint> getAllSessions() 
   {
		return sessions;
	}

   // Images are sent to the client as binary data.   
   
	public void sendImage(byte[] data) 
   {
		if (session == null)	return;		
		time = System.currentTimeMillis() - createtime ;
		try 
      {        	
			ByteBuffer byteBuffer = ByteBuffer.wrap(addHeader(imageheader,data));
			session.sendBinary(byteBuffer, Callback.NOOP);
         byteBuffer.clear();
      } 
      catch (Exception e) 
      { 
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendImage exception " + e.getMessage());   
         e.printStackTrace(); 
      }
   }

   // Messages are sent to the client as text data.   
   
	public void send(String message) 
   {
		if (session == null) return;	
		time = System.currentTimeMillis() - createtime ;
   
      if (javax.swing.SwingUtilities.isEventDispatchThread())
      {
      	Thread thread = new Thread()
			{ 
            public void run() 
            { 
               setName("SendText " + message) ;
               send(message) ; 
            } 
         } ;
   		thread.start() ;
         return ;
      }
      
		try 
      {  
         while (socketBusy)
         {
            try { Thread.currentThread().sleep(busywait) ; }
            catch (InterruptedException e) { }
         }
         
         socketBusy = true ;
         if (!(message.startsWith("mouse") || message.startsWith("cursor")))
            System.out.println("[" + time + "] "+"Message send: " + message);
         session.sendText(message, new Callback() {
            @Override
            public void succeed() {
               // Handle success (optional)
               socketBusy = false ;
            }
                
            @Override
            public void fail(Throwable cause) {
               // Handle failure (e.g., client disconnected)
        	   	time = System.currentTimeMillis() - createtime ;
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendText failed: " + cause.getMessage());
               socketBusy = false ;
               session.close(); // Consider closing the session on failure
            }
         });
      } 
      catch (Exception e) 
      { 
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendText exception " + e.getMessage());   
         e.printStackTrace(); 
      }
      finally
      {
         socketBusy = false ;         
      }
   }
   
 
   // File data is sent to the client as binary data.  This sends the complete
   // file in chunks which is then reassembled into one message by the client.
   
	public void sendFile(File file) 
   {
		if (session == null)	return;		
      if (file == null) return ;
     
      if (javax.swing.SwingUtilities.isEventDispatchThread())
      {
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendFile "+file.getName()+" on EDT, starting new thread for websocket transfer.") ;   
      	Thread thread = new Thread()
			{ 
            public void run() 
            { 
               setName("SendFile " + file.getName()) ;
               sendFile(file) ; 
            } 
         } ;
   		thread.start() ;
         return ;
      }
      
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.showStatus("WebSocket uploading file " + file.getName() + " to client browser.") ;
                         
		try 
      {  
         while (socketBusy)
         {
            try { Thread.currentThread().sleep(busywait) ; }
            catch (InterruptedException e) { }
         }
         
         socketBusy = true ;
         FileInputStream fis = new FileInputStream(file) ;
         byte[] buffer = new byte[1024*32]; // 32KB buffer
         long totalBytes = file.length() ;
         boolean first = true ;
         int bytesRead;
         int chunks = 0 ;
         int offset = 0 ;
         ByteBuffer data ;
         
         while ((bytesRead = fis.read(buffer)) != -1) {
            // Create a ByteBuffer for the current chunk
            if (first)
               data = ByteBuffer.wrap(addHeader(fileheader,buffer), 0, bytesRead+fileheader.length);
            else 
               data = ByteBuffer.wrap(buffer, 0, bytesRead);
            first = false ;
            chunks++ ;
            offset += bytesRead ;
            final int bytesSent = bytesRead ;
            boolean last = (offset >= totalBytes) ;
            
            // Send the binary data (use sendBytes for blocking or async with callback)
            bufferBusy = true ;
            session.sendPartialBinary(data, last, new Callback() {
               @Override
               public void succeed() {
                  // Handle success (optional)
        				time = System.currentTimeMillis() - createtime ;
                  if (OptionsDialog.getDebugWebSocket())
                     System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: Sent file binary data length: " + bytesSent);
                  if (last) socketBusy = false ;
                  bufferBusy = false ;
               }
                
               @Override
               public void fail(Throwable cause) {
                  // Handle failure (e.g., client disconnected)
        				time = System.currentTimeMillis() - createtime ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: File chunk send failed: " + cause.getMessage());
                  socketBusy = false ;
                  bufferBusy = false ;
                  session.close(); // Consider closing the session on failure
               }
            });
                  
            // Wait for the partial buffer to complete sending.  This is to handle
            // backpressure so we don't send faster than the network can handle.
            // Wait for asynchronous delivery to complete.  Failure to do this
            // can cause clicks and other odd things with sound playback.

            int waittime = 0 ;
            while (bufferBusy)
            {
               try { Thread.currentThread().sleep(busywait) ; }
               catch (InterruptedException e) { }
               waittime += busywait ;
               if (waittime > busytimeout)
               {
        				time = System.currentTimeMillis() - createtime ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: send file buffer " + chunks + ", timeout exceeds " + busytimeout +"ms, continuing.");
                  break ;
               }
            }
				data.clear();            
         }
         
         setTimeoutStart() ;
         send("filesaveend " + removeSpaces(file.getName(),"_") + " " + offset) ;
      }
      catch (FileNotFoundException e)
      {
         socketBusy = false ;
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendFile exception " + e.getMessage()) ;            
      }
      catch (Exception e) 
      { 
         socketBusy = false ;
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendFile exception " + e.getMessage()) ;   
         e.printStackTrace() ; 
      }
      finally
      {
         socketBusy = false ;         
         if (mf != null) mf.showStatus(null) ;
      }
   }
   
 
   // File data is sent to the client as binary data.  This sends the complete
   // file in chunks which is then reassembled into one message by the client.
   // Audio is not played on the EDT thread so a websocket transfer is safe.
  
	public void sendAudio(InputStream stream, String name, int length) 
   {
		if (session == null)	return ;		
      if (stream == null) return ;
      
      if (javax.swing.SwingUtilities.isEventDispatchThread())
      {
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendAudio "+name+" on EDT, starting new thread for websocket transfer.") ;   
      	Thread thread = new Thread()
			{ 
            public void run() 
            { 
               setName("SendAudio " + name) ;
               sendAudio(stream,name,length) ; 
            } 
         } ;
   		thread.start() ;
         return ;
      }
      
      MainFrame mf = Kisekae.getMainFrame() ;
      if (mf != null) mf.showStatus("WebSocket uploading audio " + name + " to client browser.") ;                         
      
		try 
      {  
         while (socketBusy)
         {
            try { Thread.currentThread().sleep(busywait) ; }
            catch (InterruptedException e) { }
         }
         
         socketBusy = true ;
         byte[] buffer = new byte[1024*32]; // 32KB buffer
         long totalBytes = length ;
         boolean first = true ;
         int bytesRead;
         int chunks = 0 ;
         int offset = 0 ;
         ByteBuffer data ;
         
         while ((bytesRead = stream.read(buffer)) != -1) {
            // Create a ByteBuffer for the current chunk
            if (first)
               data = ByteBuffer.wrap(addHeader(soundheader,buffer), 0, bytesRead+soundheader.length);
            else 
               data = ByteBuffer.wrap(buffer, 0, bytesRead);
            first = false ;
            chunks++ ;
            offset += bytesRead ;
            final int bytesSent = bytesRead ;
            final int chunk = chunks ;
            boolean last = (offset >= totalBytes) ;
            
            // Send the binary data (use sendBytes for blocking or async with callback)
            bufferBusy = true ;
  				long starttime = System.currentTimeMillis()  - createtime ;
            session.sendPartialBinary(data, last, new Callback() {
               @Override
               public void succeed() {
                  // Handle success (optional)
        				time = System.currentTimeMillis() - createtime ;
                  if (OptionsDialog.getDebugWebSocket())
                     System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: Sent audio buffer data length: " + bytesSent + " chunk " + chunk + " time = " + (time-starttime));
                  if (last) socketBusy = false ;
                  bufferBusy = false ;
               }
                
               @Override
               public void fail(Throwable cause) {
                  // Handle failure (e.g., client disconnected)
        				time = System.currentTimeMillis() - createtime ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: Audio chunk send failed: " + cause.getMessage() + " chunk " + chunk + " time = " + (time-starttime));
                  socketBusy = false ;
                  bufferBusy = false ;
                  session.close(); // Consider closing the session on failure
               }
            });
                  
            // Wait for the partial buffer to complete sending.  This is to handle
            // backpressure so we don't send faster than the network can handle.
            // Wait for asynchronous delivery to complete.  Failure to do this
            // can cause clicks and other odd things with sound playback.

            int waittime = 0 ;
            while (bufferBusy)
            {
               try { Thread.currentThread().sleep(busywait) ; }
               catch (InterruptedException e) { }
               waittime += busywait ;
               if (waittime > busytimeout)
               {
        				time = System.currentTimeMillis() - createtime ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: Sent audio buffer " + chunk + ", timeout exceeds " + busytimeout +"ms, continuing.");
                  break ;
               }
            }
				data.clear();        
         }

         // Did we send MIDI data?
         
         setTimeoutStart() ;
         audioIsPlaying = true ;
         String lowercasename = name.toLowerCase() ;
         if (lowercasename.endsWith(".mid") || lowercasename.endsWith(".midi"))
            send("audioendmidi " + removeSpaces(name,"_") + " " + offset + " " + chunks) ;
         else   
            send("audioend " + removeSpaces(name,"_") + " " + offset + " " + chunks) ;
      }
      catch (FileNotFoundException e)
      {
         socketBusy = false ;
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendAudio exception " + e.getMessage()) ;            
      }
      catch (Exception e) 
      { 
         socketBusy = false ;
			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: sendAudio exception " + e.getMessage()) ;   
         e.printStackTrace() ; 
      }
      finally
      {
         socketBusy = false ;         
         if (mf != null) mf.showStatus(null) ;
      }
   }

   
   // Binary messages have a header to identify if image, file, or sound data.
   // 0 = image, 1 = file, 2 = sound.
   
   byte[] addHeader(byte[] header, byte[] message) 
   {
      byte[] combinedbuffer = new byte[header.length+message.length] ;
      System.arraycopy(header,0,combinedbuffer,0,header.length) ;
      System.arraycopy(message,0,combinedbuffer,header.length,message.length) ;
      return combinedbuffer ;
   }
   
   // On a server socket close request send a close image to the client and a 
   // close request to the client.  Server socket close requests occur when 
   // UltraKiss is terminated.
   
   public void closeWebSocket()
   {  
      if (processThread != null) processThread.interrupt() ;
      if (timeoutThread != null) timeoutThread.interrupt() ;
      if (!session.isOpen()) return ;
      try { Thread.currentThread().sleep(200) ; } catch (InterruptedException e) { }
      onMessage("image Images/KisekaeUltraKiss.png") ;
      try { Thread.currentThread().sleep(300) ; } catch (InterruptedException e) { }
      send("sendconsolelog") ;   // request console log file
      try { Thread.currentThread().sleep(500) ; } catch (InterruptedException e) { }
      onMessage("close UltraKiss terminated.") ;
   }
   
   
   public static boolean isSessionOpen()
   {
      return (!sessions.isEmpty()) ;
   }
   
   public static void setTimeoutStart()
   {
      timeoutstart = new Date().getTime() ;      
   }
   
   public static String removeSpaces(String source, String sub)
   {
      if (source == null) return null ;
      return source.replaceAll(" ", sub) ;
   }
 
   
   /**
    * Inner class to animate screen captures.  This runs many times a second
    * to animate the client display.  
    */

   class ProcessThread extends Thread 
   {
      int delay = 100 ;  // screen capture period
//      int delay = 5000 ;  // screen capture period
      int iterations = 0 ;
      long totaltime = 0 ;
      long maxtime = 0 ;
      long mintime = 0 ;
      long time = 0 ;
      boolean error = false ;
      int errors = 0 ;
      
      @Override
      public void run()
      {
         // Wait a bit on startup.
         try { Thread.currentThread().sleep(500) ; }
         catch (InterruptedException e) { }
         
  			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: " + this.getName() + " starts."); 
         
         if (Kisekae.getMainFrame() == null)
         {
            int waittime = 0 ;
            int maxconnection = Kisekae.getMaxConnectionTime() ;
     			time = System.currentTimeMillis() - createtime ;
            System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: MainFrame is not yet available, waiting ..."); 
            while (Kisekae.getMainFrame() == null)
            {
               try { Thread.currentThread().sleep(delay) ; }
               catch (InterruptedException e) 
               { 
                  error = true ;
                  break ; 
               }   
               waittime += delay ;
               if (waittime > maxconnection)
               {
           			time = System.currentTimeMillis() - createtime ;
                  System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: MainFrame is not created, screen capture unable to continue ..."); 
                  error = true ;
                  break ;
               }
            }
            if (Kisekae.getMainFrame() != null)
            {
        			time = System.currentTimeMillis() - createtime ;
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: MainFrame established, continuing with screen capture ...");                
            }
         }
                                     
         while (session != null && !error)
         {
				try 
            {
               iterations++ ;
      			time = System.currentTimeMillis() - createtime ;
               long starttime = System.currentTimeMillis() ;
					BufferedImage bi = ScreenCapture.captureScreenImage() ;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					ImageIO.write(bi, "png", out);
					ByteBuffer byteBuffer = ByteBuffer.wrap(addHeader(imageheader,out.toByteArray()));
               
               if (!socketBusy) 
               {
                  socketBusy = true ;
                  session.sendBinary(byteBuffer, new Callback() {
                     @Override
                     public void succeed() {
                        // Handle success (optional)
                        socketBusy = false ;
                     }
                     @Override
                     public void fail(Throwable cause) {
                        // Failed to send screen capture to client, cause=DataFrame before fin==true                                   
                        errors++ ;
                        socketBusy = false ;
                        String reason = cause.getMessage() ;
                        if (reason == null) reason = "Unknown" ;
                        System.out.println("[" + time + "] "+"Failed to send screen capture to client, cause=" + reason);
                        System.out.println("[" + time + "] "+"Session is open: " + session.isOpen());
                        error = !session.isOpen() ; 
                     }
                  });
               }
               
					out.close();
					byteBuffer.clear();
               long endtime = System.currentTimeMillis() ;
               long time = endtime - starttime ;
               if (time > maxtime || maxtime == 0) maxtime = time ;
               if (time < mintime || mintime == 0) mintime = time ;
               totaltime += time ;
               sleep(delay) ;
				} 
            catch (InterruptedException e) 
            { break ; }               
            catch (AWTException e) 
            { 
      			time = System.currentTimeMillis() - createtime ;
               System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: screen capture exception " + e.getMessage()); 
            }
            catch (Exception e) 
            { error = true ; e.printStackTrace(); }
         }
         
         timeoutThread.interrupt() ;
         long averagetime = (iterations > 0) ? totaltime / iterations : 0 ;
         System.out.println("JettyWebSocketEndpoint: screen capture stops, average capture time="+averagetime+"ms");
         System.out.println("JettyWebSocketEndpoint: screen capture maxtime="+maxtime+"ms, mintime="+mintime+"ms");
         
         if (error)
         {
            String reason = Kisekae.getCaptions().getString("WebSocketException") + ", " +
               Kisekae.getCaptions().getString("SessionClosed") ;
            System.out.println("JettyWebSocketEndpoint: error reason=" + reason);
            Kisekae.exit() ;
         }
      }
   }
   
   /**
    * Inner class to maintain file upload attributes.  When we upload files
    * from the browser client to UltraKiss the data is stored in a temporary
    * file on the server.  When we receive the binary data we locate this
    * attribute object by the session key in order to get the output stream 
    * to write the data.
    */
   
   class FileUploadClass
   {
       public File file ;
       public FileOutputStream os ;
       
       FileUploadClass(File file, FileOutputStream os)
       {
          this.file = file ;
          this.os = os ;          
       }      
   }
 
   
   /**
    * Inner class to watch for an inactivity timeout.  Perform an orderly
    * shutdown on a timeout.
    */

   class TimeoutThread extends Thread 
   {
      private long lastduration = 0;

      @Override
      public void run()
      {     
  			time = System.currentTimeMillis() - createtime ;
         System.out.println("[" + time + "] "+"JettyWebSocketEndpoint: " + this.getName() + " starts."); 

         try 
         {
            while (true)
            {
               long timeoutend = new Date().getTime() ;
               if (timeoutstart == 0) timeoutstart = timeoutend ;
               long duration = timeoutend - timeoutstart ;
               
               // If duration is increasing we may timeout.
               // If duration drops we had movement.
               
               if (duration < lastduration)
               {
                  if (warningsent) 
                  {
                     System.out.println("JettyWebSocketEndpoint: timeout cancelled.");
                     send("timeoutcancel") ;
                     timeout = timeout - (30 * 1000) ;  // restore timeout 
                  }
                  warningsent = false ;
               }
               lastduration = duration ;
               
               // If duration exceeds timeout value, show a warning prompt.
               
               if (duration > timeout  && !audioIsPlaying) 
               {
                  if (warningsent)
                  {
                     // request that client close the connection
                     send("timeout") ;
                     System.out.println("JettyWebSocketEndpoint: timeout UltraKiss terminated.");
                     MainFrame mainframe = Kisekae.getMainFrame() ;
                     if (mainframe != null) 
                     {
                 			Runnable runner = new Runnable()
                 			{ public void run() { mainframe.close() ; } } ;
                        javax.swing.SwingUtilities.invokeLater(runner) ; 
                     }
                     else
                     {
                        closeWebSocket() ;
                        exit(1) ;
                     }
                     return ;
                  }
                  System.out.println("JettyWebSocketEndpoint: sending timeout warning.");
                  send("timeoutalert") ;
                  warningsent = true ;
                  timeout = timeout + (30 * 1000) ;  // add 30 more seconds
               }
               sleep(1000) ; 
            }
         }
         catch (InterruptedException e) { }  
      }      
   }
}