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


/*
 * NewEventDialog.java
 *
 * Created on July 11, 2005, 6:14 PM
 */


import java.awt.* ;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.* ;

public class EventWizard extends KissDialog 
{
   private Configuration config ;
   private KissObject kiss ;
   private JLabel indextitle = new JLabel();
   private Object primaryobject ;
   private Object secondaryobject ;
   private String ls = "\n" ;
   
	protected CallbackButton callback = new CallbackButton(this,"EventWizard Callback") ;

   
   /** Creates new form NewEventDialog */
   
   public EventWizard(JFrame parent, Configuration config, KissObject k, boolean modal) {
      super(parent,"",modal);
      this.parent = parent ;
      this.config = config ;
      this.kiss = k ;
      initComponents();
      setValues() ;

  		// Set the default button for an enter key.

  		JRootPane rootpane = getRootPane()  ;
  		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;

		// Center the frame in the panel space.

		center(this) ;
   }
   
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   private void initComponents() {//GEN-BEGIN:initComponents
      jPanel4 = new javax.swing.JPanel();
      jPanel1 = new javax.swing.JPanel();
      jLabel1 = new javax.swing.JLabel();
      jComboBox1 = new javax.swing.JComboBox();
      jPanel2 = new javax.swing.JPanel();
      jLabel2 = new javax.swing.JLabel();
      primaryobjects = new javax.swing.JComboBox();
      jPanel3 = new javax.swing.JPanel();
      OK = new javax.swing.JButton();
      UNDO = new javax.swing.JButton();
      CANCEL = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTextArea1 = new javax.swing.JTextArea();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("FKiSS Event Wizard");
      jPanel4.setLayout(new java.awt.BorderLayout());

      jPanel4.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 10, 10)));
      jPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 10, 0)));
      jLabel1.setText("Event Type:");
      jLabel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 5)));
      jPanel1.add(jLabel1);

      jComboBox1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jComboBox1ActionPerformed(evt);
         }
      });

      jPanel1.add(jComboBox1);

      jPanel1.add(jPanel2);

      jLabel2.setText("Event Source:");
      jLabel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 10, 0, 5)));
      jPanel1.add(jLabel2);

      primaryobjects.setEnabled(false);
      primaryobjects.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            primaryobjectsItemStateChanged(evt);
         }
      });

      jPanel1.add(primaryobjects);

      jPanel4.add(jPanel1, java.awt.BorderLayout.NORTH);

      jPanel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 0, 0, 0)));
      OK.setText("OK");
      OK.setEnabled(false);
      OK.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            OKActionPerformed(evt);
         }
      });

      jPanel3.add(OK);

      UNDO.setText("Undo Event");
      UNDO.setEnabled(false);
      UNDO.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            UNDOActionPerformed(evt);
         }
      });

      jPanel3.add(UNDO);

      CANCEL.setText("Cancel");
      CANCEL.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            CANCELActionPerformed(evt);
         }
      });

      jPanel3.add(CANCEL);

      jPanel4.add(jPanel3, java.awt.BorderLayout.SOUTH);

      jTextArea1.setColumns(80);
      jTextArea1.setLineWrap(true);
      jTextArea1.setRows(20);
      jTextArea1.setTabSize(3);
      jTextArea1.setWrapStyleWord(true);
      jScrollPane1.setViewportView(jTextArea1);

      jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

      getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

      pack();
   }//GEN-END:initComponents

   private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
      if (!jComboBox1.isEnabled()) return ;
      int i = jComboBox1.getSelectedIndex() ;
      if (i < 0) return ;
      OK.setEnabled(true) ;
      UNDO.setEnabled(true) ;
      
      Object o = jComboBox1.getSelectedItem() ;
      if (o == null) return ;
      String s = o.toString().toLowerCase() ;
      if ("snap to".equals(s)) eventSnapTo() ;
      if ("press".equals(s)) eventPressRelease(s) ;
      if ("release".equals(s)) eventPressRelease(s) ;
      if ("catch".equals(s)) eventPressRelease(s) ;
      if ("drop".equals(s)) eventPressRelease(s) ;
      if ("fixcatch".equals(s)) eventPressRelease(s) ;
      if ("fixdrop".equals(s)) eventPressRelease(s) ;
      if ("unfix".equals(s)) eventPressRelease(s) ;
      if ("in".equals(s)) eventInOut(s) ;
      if ("out".equals(s)) eventInOut(s) ;
      if ("detached".equals(s)) eventPressRelease(s) ;
      if ("mousein".equals(s)) eventPressRelease(s) ;
      if ("mouseout".equals(s)) eventPressRelease(s) ;
      if ("stillin".equals(s)) eventInOut(s) ;
      if ("stillout".equals(s)) eventInOut(s) ;
      if ("collide".equals(s)) eventInOut(s) ;
      if ("apart".equals(s)) eventInOut(s) ;
   }//GEN-LAST:event_jComboBox1ActionPerformed

   private void primaryobjectsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_primaryobjectsItemStateChanged
      if (!primaryobjects.isEnabled()) return ;
      primaryobject = primaryobjects.getSelectedItem() ;
      String s1 = getTitle() ;
      if (s1 == null) s1 = "" ;
      int i = s1.indexOf(" - ") ;
      if (i >= 0) s1 = s1.substring(0,i) ;
      if (primaryobject != null) 
      {
         setTitle(s1 + " - " + primaryobject.toString()) ;
         if (primaryobject instanceof Cel)
         {
            s1 = getTitle() ;
            Object o = ((Cel) primaryobject).getGroup() ;
            if (o instanceof Group)
               setTitle(s1 + " (" + o + ")") ;
         }
      }
      
      // Set the event context.
      
      setEventContext() ;

   }//GEN-LAST:event_primaryobjectsItemStateChanged

   private void UNDOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UNDOActionPerformed
      primaryobjects.setSelectedItem(kiss) ;
      jComboBox1.setSelectedItem(null) ;
      UNDO.setEnabled(false) ;
      OK.setEnabled(false) ;
      setIntroText() ;
   }
   
   private void setIntroText()
   {
		jTextArea1 = new JTextArea() 
      {
         public String getToolTipText(MouseEvent e)
         {
            // If we are showing FKiSS signature help then identify the
            // event or action name within the FKiSS statement.
               
            try 
            { 
               Point pos = e.getPoint() ;
               int index = viewToModel(pos) ;
               int start = Utilities.getPreviousWord(jTextArea1,index) ;
               int end = Utilities.getWordEnd(jTextArea1,index) ;
               int line = getLineOfOffset(index) ; 
               int linestart = getLineStartOffset(line) ;
               int lineend = getLineEndOffset(line) ;
               String s = jTextArea1.getText(linestart, lineend-linestart) ;
               int i = s.indexOf(";@") ;
               if (i < 0) i = start ;
               int j = s.indexOf(";",i+1) ;
               if (j < 0) j = end ;
               if ((start-linestart) > i && (end-linestart) <= j)
               {
                  String word = getText(start,end-start) ; 
                  return EventHandler.findSignature(word) ; 
               }
            }
            catch (BadLocationException ble) { return null ; }
            return null ;
        }
      } ;
      ToolTipManager.sharedInstance().registerComponent(jTextArea1) ;
      jTextArea1.setColumns(80);
      jTextArea1.setLineWrap(true);
      jTextArea1.setRows(20);
      jTextArea1.setTabSize(3);
      jTextArea1.setWrapStyleWord(true);
      jScrollPane1.setViewportView(jTextArea1);
      
      String s = "Welcome to the UltraKiss Event Wizard.\n\n"
         + "This wizard helps generate simple FKiSS event code.\n\n"
         + "Step 1.  Choose an event source object from the list above.\n\n"
         + "Step 2.  Choose an event type for the source object from the list above.\n\n"
         + "Step 3.  If prompted, select the secondary event object from the pop-up list.\n\n"
         + "Step 4.  Review and edit the skeleton code generated for you.\n\n"
         + "Step 5.  Apply your changes to save the event." ;
      jTextArea1.setText(s) ;
   }//GEN-LAST:event_UNDOActionPerformed

   private void OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKActionPerformed
      int i = jComboBox1.getSelectedIndex() ;
      if (i >= 0) callback.doClick() ;
      flush() ;
		dispose() ;
      return ;
   }//GEN-LAST:event_OKActionPerformed

   private void CANCELActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CANCELActionPerformed
		flush() ;
      dispose() ;
		return ;
   }//GEN-LAST:event_CANCELActionPerformed
   
   
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton CANCEL;
   private javax.swing.JButton OK;
   private javax.swing.JButton UNDO;
   private javax.swing.JComboBox jComboBox1;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JPanel jPanel4;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTextArea jTextArea1;
   private javax.swing.JComboBox primaryobjects;
   // End of variables declaration//GEN-END:variables
   
   void setValues()
   {
      setIntroText() ;
      
      // Set primary object list for all entities.

      if (config == null) return ;
      Vector v = config.getGroups() ;
      primaryobjects.setEnabled(false) ;
      for (int i = 0 ; i < v.size() ; i++) 
         primaryobjects.addItem(v.elementAt(i)) ;
      v = config.getCels() ;
      for (int i = 0 ; i < v.size() ; i++) 
         primaryobjects.addItem(v.elementAt(i)) ;
      v = config.getCelGroups() ;
      for (int i = 0 ; i < v.size() ; i++) 
         primaryobjects.addItem(v.elementAt(i)) ;
      
      primaryobject = kiss ;
      primaryobjects.setSelectedItem(primaryobject) ;
      primaryobjects.setEnabled(true) ;
      
      // Set the event context.
      
      setEventContext() ;
      
      // Show the primary object in the dialog title.
      
      if (primaryobject != null)
      {
         String s1 = getTitle() ;
         if (s1.indexOf('-') < 0)
            setTitle(s1 + " - " + primaryobject.toString()) ;
         if (primaryobject instanceof Cel)
         {
            s1 = getTitle() ;
            Object o = ((Cel) primaryobject).getGroup() ;
            if (o instanceof Group)
               setTitle(s1 + " (" + o + ")") ;
         }
      }
   }

   
   // Set the event selection list based upon context.   
   
   private void setEventContext()
   {
      jComboBox1.setEnabled(false) ;
      jComboBox1.removeAllItems();

      if (!(primaryobject instanceof KissObject)) return ;
      
      if (((KissObject) primaryobject).isComponent())
      {
         jComboBox1.addItem("press") ;
         jComboBox1.addItem("release") ;
         jComboBox1.addItem("catch") ;
         jComboBox1.addItem("drop") ;
         jComboBox1.addItem("fixcatch") ;
         jComboBox1.addItem("fixdrop") ;
         jComboBox1.addItem("mousein") ;
         jComboBox1.addItem("mouseout") ;
      }
      else if (primaryobject instanceof Group)
      {
         jComboBox1.addItem("Snap To") ;
         jComboBox1.addItem("press") ;
         jComboBox1.addItem("release") ;
         jComboBox1.addItem("catch") ;
         jComboBox1.addItem("drop") ;
         jComboBox1.addItem("fixcatch") ;
         jComboBox1.addItem("fixdrop") ;
         jComboBox1.addItem("apart") ;
         jComboBox1.addItem("collide") ;
         jComboBox1.addItem("in") ;
         jComboBox1.addItem("out") ;
         jComboBox1.addItem("stillin") ;
         jComboBox1.addItem("stillout") ;
         jComboBox1.addItem("mousein") ;
         jComboBox1.addItem("mouseout") ;
         jComboBox1.addItem("detached") ;
         jComboBox1.addItem("unfix") ;
      }
      else if (primaryobject instanceof Cel)
      {
         jComboBox1.addItem("Snap To") ;
         jComboBox1.addItem("press") ;
         jComboBox1.addItem("release") ;
         jComboBox1.addItem("catch") ;
         jComboBox1.addItem("drop") ;
         jComboBox1.addItem("fixcatch") ;
         jComboBox1.addItem("fixdrop") ;
         jComboBox1.addItem("apart") ;
         jComboBox1.addItem("collide") ;
         jComboBox1.addItem("in") ;
         jComboBox1.addItem("out") ;
         jComboBox1.addItem("stillin") ;
         jComboBox1.addItem("stillout") ;
         jComboBox1.addItem("mousein") ;
         jComboBox1.addItem("mouseout") ;
         jComboBox1.addItem("unfix") ;
      }
      else if (primaryobject instanceof CelGroup)
      {
         jComboBox1.addItem("press") ;
         jComboBox1.addItem("release") ;
         jComboBox1.addItem("catch") ;
         jComboBox1.addItem("drop") ;
         jComboBox1.addItem("fixcatch") ;
         jComboBox1.addItem("fixdrop") ;
         jComboBox1.addItem("apart") ;
         jComboBox1.addItem("collide") ;
      }

     jComboBox1.setSelectedItem(null) ;
     jComboBox1.setEnabled(true) ;
   }
   
   
   // Event code generation for a single parameter event.
   
   private void eventPressRelease(String ss)
   {
      if (ss == null) return ;
      String s = ";@" + ss + "(" ;
      String encoding = Kisekae.getLanguageEncoding() ;
      if (primaryobject == null) 
         primaryobject = getObject("primary") ;
      if (primaryobject == null) return ;
      if (primaryobject instanceof Cel) 
         s += "\"" + ((Cel) primaryobject).getName() + "\"" ;
      if (primaryobject instanceof Group) 
         s += ((Group) primaryobject).getName() ;
      if (primaryobject instanceof CelGroup) 
         s += ((CelGroup) primaryobject).getName() ;
      s += ")" + ls ;
      s += ";@ notify(\"" + ss + " " + primaryobject + "\")" + ls ;
      jTextArea1.setText(s + ls) ; 
   }
   
   
   
   // Event code generation for a Snap-To event.
   
   private void eventSnapTo() 
   {
      if (config == null) return ;
      Vector v = config.getGroups() ;
      if (v == null || v.size() < 1) return ;
      if (!(primaryobject instanceof KissObject)) return ;
      secondaryobject = JOptionPane.showInputDialog(null,
         "Select the parent object",
         "Snap " + primaryobject + " To ...",
         JOptionPane.INFORMATION_MESSAGE,
         null,
         v.toArray(),
         null) ;
      if (!(secondaryobject instanceof KissObject)) return ;  
      KissObject g1 = (KissObject) primaryobject ;
      KissObject g2 = (KissObject) secondaryobject ;
      
      // Build the collision event text
      
      String s = ";@collide" ;
      if (g1 instanceof Group && g2 instanceof Group) s = ";@in" ;
      String encoding = Kisekae.getLanguageEncoding() ;
      String n1 = g1.getName() ;
      if (g1 instanceof Cel) n1 = '\"' + n1 + '\"' ;
      String n2 = g2.getName() ;
      if (g2 instanceof Cel) n2 = '\"' + n2 + '\"' ;
      s += "(" + n1 + "," + n2 + ")" + ls ;
      
      // Move parent group object if we specified cels.

      if (g1 instanceof Cel) 
      {
         Object o = ((Cel) g1).getGroup() ;
         if (!(o instanceof Group)) return ;
         g1 = (Group) o ;
      }
      if (g2 instanceof Cel)
      {
         Object o = ((Cel) g2).getGroup() ;
         if (!(o instanceof Group)) return ;
         g2 = (Group) o ;
      }
      
      // Determine the group positions.
      
      Point p1 = g1.getLocation() ;
      Point p2 = g2.getLocation() ;
      
      // Build the horizontal displacement text
      
      String s1 = ";@ movebyx(" ; 
      s1 += g1.getName() + ",";
      s1 += g2.getName() + ",";
      s1 += (p1.x - p2.x) + ")" + ls ;
      
      // Build the vertical displacement text
      
      String s2 = ";@ movebyy(" ; 
      s2 += g1.getName() + "," ;
      s2 += g2.getName() + ",";
      s2 += (p1.y - p2.y) + ")" + ls ;
      
      jTextArea1.setText(s + s1 + s2 + ls) ; 
   }
   
   
   
   // Event code generation for a two parameter event.
   
   private void eventInOut(String ss)
   {
      if (ss == null) return ;
      String s = ";@" + ss + "(" ;
      String encoding = Kisekae.getLanguageEncoding() ;
      if (primaryobject == null) 
         primaryobject = getObject("primary") ;
      if (primaryobject == null) return ;
      if (primaryobject instanceof Cel) 
         s += "\"" + ((Cel) primaryobject).getName() + "\"" ;
      if (primaryobject instanceof Group) 
         s += ((Group) primaryobject).getName() ;
      if (primaryobject instanceof CelGroup) 
         s += ((CelGroup) primaryobject).getName() ;
      
      s += "," ;
      if (secondaryobject == null) 
         secondaryobject = getObject("secondary") ;
      if (secondaryobject == null) return ;
      if (secondaryobject instanceof Cel) 
         s += "\"" + ((Cel) secondaryobject).getName() + "\"" ;
      if (secondaryobject instanceof Group) 
         s += ((Group) secondaryobject).getName() ;
      if (secondaryobject instanceof CelGroup) 
         s += ((CelGroup) secondaryobject).getName() ;
      s += ")" + ls ;
      s += ";@ notify(\"" + ss + " " + primaryobject + " and " + secondaryobject + "\")" + ls ;
      jTextArea1.setText(s + ls) ; 
   }
   
   
   // Build a selection dialog for an object.
   
   private Object getObject(String s)
   {
      Vector v = new Vector() ;
      Vector v1 = config.getGroups() ;
      if (v1 != null) v.addAll(v1) ;
      v1 = config.getCels() ;
      if (v1 != null) v.addAll(v1) ;
      v1 = config.getCelGroups() ;
      if (v1 != null) v.addAll(v1) ;
      Object o1 = jComboBox1.getSelectedItem() ;
      if (o1 == null) return null ;
      Object o = JOptionPane.showInputDialog(null,
         "Select the " + s + " object",
         o1.toString() + " " + s + " object",
         JOptionPane.INFORMATION_MESSAGE,
         null,
         v.toArray(),
         null) ;
      return o ;
   }
   
   public TextObject getTextObject()
   {
      DirEntry ze = new DirEntry("New Event") ;
      ze.setUserObject(new FKissEvent(null,config)) ;
      TextObject text = new TextObject(ze,jTextArea1,null) ;
      return text ;
  }
   
   public void setPrimaryObject(KissObject kiss) { primaryobject = kiss ; }
   
   public void setSecondaryObject(KissObject kiss) { secondaryobject = kiss ; }
   
   public Object getPrimaryObject() { return primaryobject ; }
   
   public Object getSecondaryObject() { return secondaryobject ; }

   
   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      if (parent instanceof ActionListener)
		   callback.removeActionListener((ActionListener) parent) ; 
      
      parent = null ;
      config = null ;
      kiss = null ;
      primaryobjects.removeAll() ;

      // Flush the dialog contents.

      setVisible(false) ;
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
}
