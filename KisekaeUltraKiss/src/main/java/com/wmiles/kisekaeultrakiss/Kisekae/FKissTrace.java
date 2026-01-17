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
* FKissTrace class
*
* Purpose:
*
* This class defines a dialog that displays print messages in a scrollable 
* TextArea.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.text.* ;
import javax.swing.border.*;


final public class FKissTrace extends JPanel
   implements ActionListener
{
   private JFrame parent = null ;
   private String title = null ;

   // User interface objects.

   private JButton OK = new JButton();
   private JButton CLEAR = new JButton();
   private JToggleButton PAUSE = new JToggleButton();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private JScrollPane scroll = new JScrollPane();
   private JLabel titlelabel = new JLabel();
   private JTextArea text = new JTextArea();
   private BorderLayout borderLayout1 = new BorderLayout() ;

   // Constructor

   public FKissTrace()
   { this(null, Kisekae.getCaptions().getString("FKissTraceTitle")) ; }

   public FKissTrace(JFrame frame)
   { this(frame, Kisekae.getCaptions().getString("FKissTraceTitle")) ; }
   
   public FKissTrace(JFrame frame, String s)
   {
      parent = frame ;
      title = s;
      init() ;
   }

   
   // Panel initialization.  Set a scrollable textarea for our trace window.
   // Size the trace frame to the full width of the mainframe, 

   private void init()
   {
      text = new JTextArea() ;
      scroll = new JScrollPane(text) ;
      text.setRows(5) ;
      text.setEditable(false) ;

      // Construct the user interface.

      try { jbInit(); }
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

      // Register for events.

      reset() ;
      OK.addActionListener(this) ;
      CLEAR.addActionListener(this) ;
      PAUSE.addActionListener(this) ;
   }


   // User interface initialization.

   void jbInit() throws Exception
   {
      this.setLayout(borderLayout1);
      this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      jPanel1.setLayout(new BorderLayout());
      jPanel2.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
      titlelabel.setText((title != null) ? title : "") ;
      OK.setText(Kisekae.getCaptions().getString("CloseMessage"));
      CLEAR.setText(Kisekae.getCaptions().getString("LogClearMessage"));
      PAUSE.setText(Kisekae.getCaptions().getString("BreakpointTitle"));

		this.add(titlelabel, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(scroll,BorderLayout.CENTER) ;
 		this.add(jPanel2, BorderLayout.SOUTH);
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(CLEAR, null);
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(PAUSE, null);
      jPanel2.add(Box.createGlue()) ;
   }
   
   
   // Reset the trace for a set restart.
   
   void reset() 
   { 
      text.setText(Kisekae.getCopyright() + "\n") ; 
      updateRunState() ;
   }
   
   
   // Update the breakpoint state button.
   
   void updateRunState() 
   { 
      PAUSE.setSelected(FKissEvent.getBreakPause()) ;
   }


   // The required override to set dialog values.
   
   void setValues() { }


   // Appends the specified string to the text area.
   
   void write(String s) 
   { 
      text.append(s) ; 
      Document doc = text.getDocument() ;
      Position pos = doc.getEndPosition() ;
      int offset = pos.getOffset() - 1 ;
      if (offset < 0) offset = 0 ;
      text.setCaretPosition(offset) ;
   }


   // The action method is used to process control events.

   public void actionPerformed(ActionEvent evt)

   {
      Object source = evt.getSource() ;

      // An OK sets the trace window invisible. 

      if (source == OK)
      {
         setVisible(false) ;
         if (!(parent instanceof MainFrame)) return ;
         MainFrame mf = (MainFrame) parent ;
         if (mf == null) return ;
         MainMenu menu =  mf.getMainMenu() ;
         if (menu == null) return ;
         menu.tracefkiss.setSelected(false) ;
         mf.validate() ;
         mf.centerpanel() ;
         return ;
      }

      // A CLEAR request erases the text.

      if (source == CLEAR)
      {
         reset() ;
         return ;
      }

      // A BREAKPOINT request invokes the FKiSS Editor on the next action.

      if (source == PAUSE)
      {
         boolean b = PAUSE.isSelected() ;
         OptionsDialog.setEventPause(false) ;
         OptionsDialog.setActionPause(b) ;
         FKissEvent.setBreakPause(FKissEvent.getBreakFrame(),b) ;
         if (!(parent instanceof MainFrame)) return ;
         ToolBar toolbar = ((MainFrame) parent).getToolBar() ;
         if (toolbar != null) toolbar.updateRunState() ;
         return ;
      }
   }
}


