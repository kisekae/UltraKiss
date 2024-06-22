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
* NotifyDialog class
*
* Purpose:
*
* This class defines an instance of a general notification dialog that is
* shown on a notify FKiSS action command.  Images or text can be shown.
* If an image is shown this takes priority over text.  Text is scrollable.
* Text with newlines encoded as two character '\n' are properly interpreted.
*
*/

import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.Vector ;
import java.util.Collections ;
import javax.swing.JViewport;

final class NotifyDialog extends KissDialog
   implements ActionListener, WindowListener
{
   private JFrame parent = null ;
   private Image image = null ;
   private String hyperlink = null ;
   private int state = 0 ;
   private int confirm = 0 ;

   // User interface objects

   private JPanel panel1 = new JPanel();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private BorderLayout borderLayout1 = new BorderLayout();
   private BorderLayout borderLayout3 = new BorderLayout();
   private JScrollPane jScrollPane1 = new JScrollPane();
   private JTextArea TEXT = new JTextArea();
   private JLabel LABEL = new JLabel();
   private JButton OK = new JButton();
   private JButton YES = new JButton();
   private JButton NO = new JButton();
   private JButton CANCEL = new JButton();
   private FlowLayout flowLayout1 = new FlowLayout();


   // Constructor.  

   public NotifyDialog(JFrame frame, String title, String text, Image img, boolean conf)
   { this(frame,title,text,img,(conf) ? 1 : 0,true) ; }
   
   public NotifyDialog(JFrame frame, String title, String text, Image img, int conf, boolean modal)
   {
      // Call the base class constructor to set up our frame.
      
      super(frame, title, modal);
      parent = frame ;
      confirm = conf ;
      image = img ;
      hyperlink = text ;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;

      // Initialize the user interface.

      try { jbInit(); pack(); }
      catch(Exception ex)
      {
         ex.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + ex.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }

      // Put the text into the TEXT object.

      int i = 0 ;
      if (text != null)
      {
         int j = text.indexOf("\\n",i) ;
         StringBuffer sb = new StringBuffer("") ;
         while (j >= 0)
         {
            sb.append(text.substring(i,j)) ;
            sb.append('\n') ;
            i = j + 2 ;
            j = text.indexOf("\\n",i) ;
        }
         sb.append(text.substring(i)) ;
         TEXT.setText(sb.toString()) ;
         TEXT.setLineWrap(true) ;
         TEXT.setWrapStyleWord(true) ;
         TEXT.setCaretPosition(0);
         TEXT.setEditable(false) ;
      }

      // Center the frame in the panel space

      if (image == null)
         setSize(new Dimension(300,200)) ;
      else
         doLayout() ;
 		center(this) ;

      OK.addActionListener(this);
      YES.addActionListener(this);
      NO.addActionListener(this);
      CANCEL.addActionListener(this);
      addWindowListener(this);
   }

   // User interface initialization.

   private void jbInit() throws Exception
   {
      panel1.setLayout(borderLayout1);
      jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
      jPanel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      jPanel2.setLayout(borderLayout3);
      jPanel2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      YES.setText(Kisekae.getCaptions().getString("YesMessage"));
      NO.setText(Kisekae.getCaptions().getString("NoMessage"));
      CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));

      getContentPane().add(panel1);
      panel1.add(jPanel1, BorderLayout.SOUTH);
      jPanel1.add(Box.createGlue()) ;
      if (confirm == 0)
      {
         jPanel1.add(OK, null);
         jPanel1.add(Box.createGlue()) ;
      }
      else if (confirm == 1)
      {
         jPanel1.add(YES, null);
         jPanel1.add(Box.createGlue()) ;
         jPanel1.add(NO, null);
         jPanel1.add(Box.createGlue()) ;
      }
      else if (confirm == 2)
      {
         jPanel1.add(CANCEL, null);
         jPanel1.add(Box.createGlue()) ;
      }
      panel1.add(jPanel2, BorderLayout.CENTER);
      jPanel2.add(jScrollPane1, BorderLayout.CENTER);
      if (image != null)
      {
         LABEL.setIcon(new ImageIcon(image));
         jScrollPane1.getViewport().add(LABEL, null);
         MouseListener listener = new HyperlinkListener() ;
         LABEL.addMouseListener(listener);            
      }
      else
         jScrollPane1.getViewport().add(TEXT, null);
   }


   // The action method is used to process control events.

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource() ;

      // An OK closes the frame

      if (OK == source) { close() ; }
      if (YES == source) { state = 1 ; close() ; }
      if (NO == source) { close() ; }
      if (CANCEL == source) 
      { 
         state = 2 ; 
         if (parent instanceof WebFrame)
         {
            if (OptionsDialog.getDebugControl())
               System.out.println("NotifyDialog: cancel WebFrame load archive.") ;
            ActionEvent event = new ActionEvent(CANCEL,0,"NotifyDialog Cancel") ;
            ((WebFrame) parent).actionPerformed(event) ;
         }
         close() ; 
      }
   }


   // Close the dialog.  We must repaint the panel frame.

   void close()
   {
      setVisible(false) ;
      redraw() ;
      dispose() ;
   }


   // Return our confirm value.

   int getConfirmValue() { return state ; }


   // Redraw the panel frame.

   private void redraw()
   {
      if (parent == null) return ;
      if (parent instanceof MainFrame)
      {
         PanelFrame panel = ((MainFrame) parent).getPanel() ;
         if (panel != null) panel.releaseMouse() ;
      }
      getParentFrame().validate() ;
      getParentFrame().repaint() ;
   }


   // Window Events

   public void windowOpened(WindowEvent evt) { }
   public void windowClosed(WindowEvent evt) { }
   public void windowIconified(WindowEvent evt) { }
   public void windowDeiconified(WindowEvent evt) { }
   public void windowActivated(WindowEvent evt) { }
   public void windowDeactivated(WindowEvent evt) { }
   public void windowClosing(WindowEvent evt) { close() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      parent = null ;
      TEXT = null ;
      setVisible(false) ;
      OK.removeActionListener(this);
      YES.removeActionListener(this);
      NO.removeActionListener(this);
      removeWindowListener(this);
      getContentPane().removeAll() ;
      getContentPane().removeNotify() ;
   }
   
   void setValues() { }


   // Mouse listener for hyperlinks on images in notify dialog.
   // FKiSS syntax:  notify(cel,string)
   
   class HyperlinkListener extends MouseAdapter 
   {
      private Cursor defaultcursor = 
         Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ;
      private Cursor presscursor = 
         Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) ;
 
      public void mouseClicked(MouseEvent event) 
      { 
         if (hyperlink == null || "".equals(hyperlink)) return ;
         String s = hyperlink.toLowerCase() ;
         try
         {
            if (s.startsWith("http:") || s.startsWith("https:"))
            {
               BrowserControl browser = new BrowserControl() ;
               browser.displayURL(s) ;
            }
         }
         catch (Exception e)
         {
            System.out.println(e) ;
         }
      }
      
      public void mouseEntered(MouseEvent e) 
      { 
         if (hyperlink == null || "".equals(hyperlink)) return ;
         setCursor(presscursor) ; 
      }
      
      public void mouseExited(MouseEvent e) 
      { 
         if (hyperlink == null || "".equals(hyperlink)) return ;
         setCursor(defaultcursor) ; 
      }
   }
}