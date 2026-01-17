package com.wmiles.kisekaeultrakiss.Kisekae ;

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
* TipsBox class
*
* Purpose:
*
* This class defines an instance of the Kisekae application Help Tips.
* This shows a random tip from the Tips data file.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.* ;
import java.util.ResourceBundle ;
import javax.swing.text.html.* ;
import java.util.Vector;


final class TipsBox extends KissDialog 
  implements ActionListener, WindowListener, ItemListener, ListSelectionListener
{
   static private int tipindex = 0 ;
   
   private Frame frame = null ;
   private URL iconfile = null ;
   private ImageIcon imageicon = null ;

   // User interface objects.

   private JPanel panel1 = new JPanel();
   private JLabel iconlabel = new JLabel();
   private JLabel tiptitle = new JLabel();
   private JLabel indextitle = new JLabel();
   private JButton OK = new JButton();
   private JButton NEXT = new JButton();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private JPanel jPanel3 = new JPanel();
   private JPanel jPanel4 = new JPanel();
   private BorderLayout borderLayout1 = new BorderLayout();
   private BorderLayout borderLayout2 = new BorderLayout();
   private BorderLayout borderLayout3 = new BorderLayout();
   private JScrollPane jScrollPane1 = new JScrollPane();
   private JScrollPane jScrollPane2 = new JScrollPane();
   private JTextPane text = new JTextPane();
   private JList list = new JList();
   private JCheckBox showfirst = new JCheckBox();

   // Constructor

   public TipsBox()
   { this(null, Kisekae.getCaptions().getString("TipsBoxTitle"), true) ; }

   public TipsBox(JFrame frame)
   { this(frame, Kisekae.getCaptions().getString("TipsBoxTitle"), true) ; }
   
   public TipsBox(JFrame frame, String title, boolean modal)
   {
      super(frame, title, modal) ;
      this.frame = frame ;
      init() ;
   }

   // Dialog initialization.

   private void init()
   {
      imageicon = Kisekae.getImageIcon() ;

      // Construct the user interface.

      try { jbInit(); pack() ; }
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
      
      // Initialize the tip.

      setValues() ;
      setTip(tipindex) ;
      list.setSelectedIndex(tipindex) ;
      list.addListSelectionListener(this) ;
      list.ensureIndexIsVisible(tipindex) ;
      list.setFont(new Font("SansSerif",Font.PLAIN,12)) ;
      setSize(new Dimension(620,450)) ;
      doLayout() ;

		// Center the frame in the panel space.

		center(this) ;

      // Register for events.

      OK.addActionListener(this) ;
      NEXT.addActionListener(this) ;
      addWindowListener(this) ;
   }


   // User interface initialization.

   void jbInit() throws Exception
   {
      panel1.setLayout(new BorderLayout());
      panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      iconlabel.setBorder(BorderFactory.createLoweredBevelBorder());
      iconlabel.setPreferredSize(new Dimension(100, 100));
      iconlabel.setHorizontalAlignment(JLabel.CENTER) ;
      iconlabel.setIcon(imageicon);
      showfirst.setText(Kisekae.getCaptions().getString("TipsShowFirst")) ;
      showfirst.setSelected(OptionsDialog.getShowTips()) ;
      showfirst.setHorizontalAlignment(SwingConstants.CENTER);
      showfirst.addItemListener(this) ;
      jPanel1.setLayout(borderLayout1);
      jPanel1.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
      jPanel1.add(iconlabel,BorderLayout.NORTH) ;
      jPanel1.add(showfirst,BorderLayout.SOUTH) ;
      
      jScrollPane2.getViewport().add(list, null);
      indextitle = new JLabel("Tips Index") ;
      indextitle.setHorizontalAlignment(SwingConstants.CENTER);
      indextitle.setFont(list.getFont());
      jScrollPane2.setColumnHeaderView(indextitle);
      jPanel3.setLayout(borderLayout3);
      jPanel3.setBorder(BorderFactory.createEmptyBorder(0,0,0,10));
      jPanel3.add(jPanel1,BorderLayout.NORTH) ;
      jPanel3.add(jScrollPane2,BorderLayout.CENTER) ;
      
      text.setEditorKit(new HTMLEditorKit()) ;
      text.setDocument(new HTMLDocument()) ;
      text.setEditable(false) ;
      jScrollPane1.getViewport().add(text, null);
      tiptitle = new JLabel("") ;
      tiptitle.setHorizontalAlignment(SwingConstants.CENTER);
      tiptitle.setFont(list.getFont());
      jScrollPane1.setColumnHeaderView(tiptitle);
      
      panel1.add(jPanel3,BorderLayout.WEST) ;
      panel1.add(jScrollPane1,BorderLayout.CENTER) ;
      
      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      NEXT.setText(Kisekae.getCaptions().getString("TipsNextTip"));
      jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
      jPanel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(NEXT, null);
      jPanel2.add(Box.createGlue()) ;
      panel1.add(jPanel2,BorderLayout.SOUTH) ;
      
      getContentPane().add(panel1);
   } 

   
   
   // Initialization.
   
   void setValues()
   {
      try 
      {
         if (frame != null) 
            Kisekae.setCursor(frame,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         ClassLoader cl = getClass().getClassLoader() ;
         URL tipsindex = Kisekae.getResource(Kisekae.getTipsIndex()) ;
         if (tipsindex == null) return ;
 
         // Read the tips index.
         
         InputStream in = tipsindex.openStream() ;
         InputStreamReader r = new InputStreamReader(in,Kisekae.getLanguageEncoding()) ;
         LineNumberReader lnr = new LineNumberReader(r) ;

         Vector v = new Vector() ;
         while (true)
         {
            String s = lnr.readLine() ;
            if (s == null) break ;
            v.addElement(s) ;
         }
 
         lnr.close() ;
         list.setListData(v) ;
      }
      catch (Exception e) 
      {
         PrintLn.println("TipsBox: setValue " + e.toString()) ;
      }
      finally
      {
         if (frame != null) 
            Kisekae.setCursor(frame,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
   }

   
   // Set the specified tip.
   
   private void setTip(int tipnum)
   {
      try
      {
         if (frame != null) 
            Kisekae.setCursor(frame,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         String s = Kisekae.getTipsApp() ;
         int m = s.indexOf('.') ;
         s = s.substring(0,m) + tipnum + s.substring(m) ;
         URL tips = Kisekae.getResource(s) ;
         try 
         { 
            text.setPage(tips) ; 
            tipindex = tipnum ;
            tiptitle.setText("Tip Number " + tipnum) ;
         }
         catch (Exception e) 
         { 
            text.setText("<p align='center'>"+ s + " not found." + "</p>") ;
         }
      }
      catch (Exception e) 
      {
         PrintLn.println("TipsBox: setTip " + e.toString()) ;
      }
      finally
      {
         if (frame != null) 
            Kisekae.setCursor(frame,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }
   }


   // The action method is used to process control events.

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource() ;

      // An OK closes the frame.

      if (source == OK)
      {
         setVisible(false) ;
         return ;
      }

      // A Next Tip displays the next entry document.

      if (source == NEXT)
      {
         int n = list.getSelectedIndex() + 1 ;
         if (n >= list.getModel().getSize()) n = 0 ; 
         list.ensureIndexIsVisible(n) ;
         list.setSelectedIndex(n) ;
         return ;
      }
   }



   // The item method is used to process checkbox events.

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getItemSelectable() ;
      
      if (source == showfirst)
         OptionsDialog.setShowTips(showfirst.isSelected()) ;
   }



   // The valueChanged method is used to process list events.

   public void valueChanged(ListSelectionEvent evt)
   {
      Object source = evt.getSource() ;
      
      if (source == list)
         setTip(list.getSelectedIndex()) ;
   }




   // Window Events

   public void windowOpened(WindowEvent evt) { }
   public void windowClosed(WindowEvent evt) { }
   public void windowIconified(WindowEvent evt) { }
   public void windowDeiconified(WindowEvent evt) { }
   public void windowActivated(WindowEvent evt) { list.requestFocus() ; }
   public void windowDeactivated(WindowEvent evt) { }
   public void windowClosing(WindowEvent evt)
   {
      frame = null ;
      setVisible(false) ;
   }
}
