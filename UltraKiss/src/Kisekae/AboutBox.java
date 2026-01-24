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
* AboutBox class
*
* Purpose:
*
* This class defines an instance of the Kisekae application About dialog.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.IOException ;
import java.util.ResourceBundle ;
import javax.media.Manager ;
import java.util.Calendar;
import java.text.DateFormat;
import java.util.Vector;


final public class AboutBox extends KissDialog
   implements ActionListener, WindowListener
{
   private static String portal = "HTML/License.html" ;

   private URL iconfile = null ;
   private ImageIcon imageicon = null ;
   private ImageIcon sailoricon = null ;
   private String javaversion = null ;
   private String mediaversion = null ;
   private String helpversion = null ;
   private String imagingversion = null ;
   private String jlayerversion = null ;
   private JDialog ld = null ;

   // User interface objects.

   private JPanel panel1 = new JPanel();
   private JLabel productlabel = new JLabel();
   private JLabel versionlabel = new JLabel();
   private JLabel copyrightlabel = new JLabel();
   private JLabel ownerlabel = new JLabel();
   private JLabel addresslabel1 = new JLabel();
   private JLabel addresslabel2 = new JLabel();
   private JLabel addresslabel3 = new JLabel();
   private JLabel iconlabel = new JLabel();
   private JLabel javalabel = new JLabel();
   private JLabel medialabel = new JLabel();
   private JLabel helplabel = new JLabel();
   private JLabel imaginglabel = new JLabel();
   private JLabel jlayerlabel = new JLabel();
   private JLabel dummylabel = new JLabel();
   private JLabel builddate = new JLabel();
   private JLabel expiredate = new JLabel();
   private JLabel website = new JLabel();
   private JButton OK = new JButton();
   private JButton LICENSE = new JButton();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private JScrollPane jScrollPane1 = new JScrollPane();
   private JList jList1 = new JList();

   // Constructor

   public AboutBox()
   { this(null, Kisekae.getCaptions().getString("AboutBoxTitle"), true) ; }

   public AboutBox(JFrame frame)
   { this(frame, Kisekae.getCaptions().getString("AboutBoxTitle"), true) ; }
   
   public AboutBox(JFrame frame, String title, boolean modal)
   {
      super(frame, title, modal) ;
      init() ;
   }

   // Dialog initialization.

   private void init()
   {
      setResizable(false) ;
      imageicon = Kisekae.getImageIcon() ;
      Package media = Package.getPackage("javax.media") ;
      Package help = Package.getPackage("javax.help") ;
      Package imaging = Package.getPackage("javax.media.jai") ;
      Package jlayer = Package.getPackage("javazoom.jl.decoder") ;
      String version = System.getProperty("java.version") ;
      javaversion = (version == null) ? "Unknown" : version ;
      version = (media == null) ? null : Manager.getVersion() ;
      mediaversion = (version == null) ? "Unknown" : version ;
      version = (help == null) ? null : help.getImplementationVersion() ;
      helpversion = (version == null) ? "Unknown" : version ;
      version = (imaging == null) ? null : imaging.getImplementationVersion() ;
      imagingversion = (version == null) ? "Unknown" : version ;
      version = (jlayer == null) ? null : "1.0" ;
      jlayerversion = (version == null) ? "Unknown" : version ;

      // Set contributors.

      Vector contrib = new Vector() ;
      contrib.addElement("Calzane (Holland)") ;
      contrib.addElement("The Invisible Phan (Canada)") ;
      contrib.addElement("Yoshakai Yav (Japan)") ;
      contrib.addElement("MIO.H (Japan)") ;
      contrib.addElement("Bruce Grubb (USA)") ;
      contrib.addElement("Laurent Sebilleau (France)") ;
      contrib.addElement("Dominatrix (Spain)") ;
      contrib.addElement("The Owl (Romania)") ;
      jList1 = new JList(contrib) ;

      // Construct the user interface.

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

      // Register for events.

      OK.addActionListener(this) ;
      LICENSE.addActionListener(this) ;
      addWindowListener(this);
      
      // Open a browser on the website link click.
      
      website.addMouseListener(new MouseAdapter()  
      {  
         public void mouseClicked(MouseEvent e)  
         {  
            try { BrowserControl.displayURL(Kisekae.getWebSite()) ; }
            catch (Exception ex) { }
         }  
      });
   }


   // User interface initialization.

   void jbInit() throws Exception
   {
      panel1.setLayout(gridBagLayout1);
      panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.Y_AXIS));
      jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
      productlabel.setText("");
      Font f = versionlabel.getFont() ;
      f = f.deriveFont(16.0f) ;
      versionlabel.setFont(f);
      versionlabel.setText(Kisekae.getReleaseLevel());
      versionlabel.setForeground(Color.red);
      versionlabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
      builddate.setText("Build date: " + Kisekae.getBuildDate()) ;
      builddate.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      expiredate.setText("Expire date: " + Kisekae.getExpireDate());
      expiredate.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
      copyrightlabel.setText(Kisekae.getCopyrightDate());
      copyrightlabel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
//    ownerlabel.setText("William Miles");
      addresslabel1.setText("144 Oakmount Rd. SW");
      addresslabel2.setText("Calgary, Alberta");
      addresslabel3.setText("Canada T2V 4X4");
      javalabel.setText("Java VM Version " + javaversion);
      helplabel.setText("Java Help Version " + helpversion);
      medialabel.setText("Java Media Framework " + mediaversion);
      imaginglabel.setText("Java Advanced Imaging " + imagingversion);
      jlayerlabel.setText("JLayer MP3 Version " + jlayerversion);
      iconlabel.setBorder(BorderFactory.createLoweredBevelBorder());
      iconlabel.setPreferredSize(new Dimension(100, 100));
      iconlabel.setHorizontalAlignment(JLabel.CENTER) ;
      iconlabel.setIcon(imageicon);
      website.setText(String.format("<html><u>%s</u></html>",Kisekae.getWebSite())) ;
      website.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
      website.setForeground(Color.BLUE.darker()) ;
      website.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ;
      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      LICENSE.setText(Kisekae.getCaptions().getString("AboutBoxLicense"));

      getContentPane().add(panel1);
      panel1.add(jPanel1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(25, 13, 0, 43), 8, 47));
      jPanel1.add(productlabel, null);
      jPanel1.add(versionlabel, null);
      jPanel1.add(copyrightlabel, null);
//    jPanel1.add(ownerlabel, null);
      jPanel1.add(addresslabel1, null);
      jPanel1.add(addresslabel2, null);
      jPanel1.add(addresslabel3, null);
      jPanel1.add(builddate, null);
      jPanel1.add(expiredate, null);
      jPanel1.add(website, null);
      if ("".equals(Kisekae.getRegistration())) jPanel1.add(expiredate, null);
      panel1.add(jPanel2, new GridBagConstraints(0, 6, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 10, 0), 0, 0));
      panel1.add(iconlabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 29, 0, 0), 0, 0));
      panel1.add(javalabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 29, 0, 0), 30, 10));
      panel1.add(helplabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 29, 0, 0), 30, 10));
      panel1.add(jlayerlabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 29, 0, 0), 30, 10));
      panel1.add(medialabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 29, 0, 0), 30, 10));
//    panel1.add(imaginglabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
//          ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 29, 0, 0), 30, 10));
      panel1.add(jScrollPane1,  new GridBagConstraints(1, 1, 1, 4, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jScrollPane1.getViewport().add(jList1, null);
      JLabel columns = new JLabel("International Contributors") ;
      columns.setHorizontalAlignment(SwingConstants.CENTER);
      columns.setFont(jList1.getFont());
      jScrollPane1.setColumnHeaderView(columns);
      jList1.setVisibleRowCount(4) ;
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
      jPanel2.add(LICENSE, null);
      jPanel2.add(Box.createGlue()) ;
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

      // A View License displays the license document on the full screen.

      if (source == LICENSE)
      {
         try
         {
            URL webpage = null ;
            ld = new JDialog(getParentFrame(),Kisekae.getCaptions().getString("AboutBoxLicense"),true) ;
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize() ;
            d = new Dimension((int) (d.width*1.00),(int) (d.height*0.95)) ;
            ld.setSize(d) ;
            ld.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            webpage = Kisekae.getResource(portal) ;
            if (webpage == null) webpage = new URL(Kisekae.getLoadBase() + portal) ;
            JEditorPane license = new JEditorPane(webpage) ;
            JScrollPane scroll = new JScrollPane(license) ;
            license.setEditable(false) ;
            ld.getContentPane().add(scroll,BorderLayout.CENTER) ;

            // Add an Accept button.

            JButton accept = new JButton(Kisekae.getCaptions().getString("AcceptMessage")) ;
            ActionListener acceptlistener = new ActionListener()
            {
               public void actionPerformed(ActionEvent e)
               { ld.dispose() ; }
            } ;
            accept.addActionListener(acceptlistener) ;
            JPanel footer = new JPanel() ;
            footer.add(accept) ;
            ld.getContentPane().add(footer,BorderLayout.SOUTH) ;
            ld.setVisible(true) ;
         }
         catch ( Exception e)
         {
            PrintLn.println("AboutBox: license agreement exception " + e) ;
         }
         return ;
      }

      // A click on the website label invokes the browser.

      if (source == website)
      {
         
      }
   }


   // Window Events

   public void windowOpened(WindowEvent evt) { }
   public void windowClosed(WindowEvent evt) { }
   public void windowIconified(WindowEvent evt) { }
   public void windowDeiconified(WindowEvent evt) { }
   public void windowDeactivated(WindowEvent evt) { }
 
   // Center the dialog in the parent space.
   
   public void windowActivated(WindowEvent evt)  { center(this) ; }
   
   // When dialog is closed, hide.
   
   public void windowClosing(WindowEvent evt)
   {
      ld = null ;
      setVisible(false) ;
   }
   
   void setValues() { }
   
}
