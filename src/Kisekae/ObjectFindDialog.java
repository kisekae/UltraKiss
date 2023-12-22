package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      3.7  (December 15, 2023)
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
* ObjectFindDialog Class
*
* Purpose:
*
* This object is a find dialog to search the object tree to find a specific
* node or object, such as a cel or group or other entity.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.util.Enumeration ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.text.* ;
import javax.swing.tree.* ;


class ObjectFindDialog extends KissDialog
{
	private KissDialog owner = null ;				// Parent frame
	private JDialog me = null ;               // Ourselves

   // Search variables.

	private boolean searchUp = false ;
	private boolean findnext = false ;
   private JTree tree = null ;
   private DefaultMutableTreeNode top = null ;
   private Enumeration treescan = null ;
   
   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JTabbedPane jTabbedPane = new JTabbedPane();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel2a = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel6a = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private GridLayout gridLayout2 = new GridLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;

	private JButton FINDNEXT = new JButton();
	private JButton CLOSE = new JButton();
	private JLabel findlabel = new JLabel();
	private JLabel statuslabel = new JLabel();
   private JTextField findtext = new JTextField() ;
   private JCheckBox wholewords = new JCheckBox();
   private JCheckBox matchcase = new JCheckBox();
   private ButtonGroup searchgroup = new ButtonGroup() ;
	private JRadioButton searchup = new JRadioButton();
	private JRadioButton searchdown = new JRadioButton();


   // Constructor

	public ObjectFindDialog(JDialog parent, JTree tree)
	{
		super(parent,Kisekae.getCaptions().getString("FindTitle"),false) ;
      this.tree = tree ;
		me = this ;

		// Construct the user interface.

 		try { jbInit() ; pack() ; }
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

		// Center the frame in the panel space.

 		center(this) ;

		// Register for events.  Find Next action.

		ActionListener findAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
            statuslabel.setText("") ;
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            if (top == null) top = (DefaultMutableTreeNode) model.getRoot();
            DefaultMutableTreeNode node = find(top,findnext,findtext.getText()) ;
            if (node == null) 
            {
               warning(Kisekae.getCaptions().getString("NoFindResultText"),true) ;
               findnext = false ;
            }
            else 
            {
               TreePath path = new TreePath(((DefaultMutableTreeNode) node).getPath()); 
               tree.setSelectionPath(path);
               tree.scrollPathToVisible(path);        
               findnext = true ;
            }
            top = node ;
				me.requestFocus() ;
         }
		} ;
      FINDNEXT.addActionListener(findAction) ;

		// Register for events.  Close action.

      ActionListener closeAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false) ;
            findnext = false ;
			}
		} ;
		CLOSE.addActionListener(closeAction) ;
		CLOSE.setDefaultCapable(true) ;

		// Register for events.  Window activate and deactivate action.

		WindowListener flst = new WindowAdapter()
      {
			public void windowOpened(WindowEvent e)
         { windowActivated(e) ; }

			public void windowActivated(WindowEvent e)
         {
            validate() ;
				if (jTabbedPane.getSelectedIndex() == 0)
					findtext.grabFocus(); 
			}

			public void windowClosed(WindowEvent e)
			{ findnext = false ; }
         
         public void windowGainedFocus(WindowEvent e) 
         { windowActivated(e) ; }

		} ;
		addWindowListener(flst) ;
      

      // Initialize and show dialog. A request to show a negative
      // index disables replacement and shows a find only dialog.

      statuslabel.setHorizontalAlignment(SwingConstants.CENTER);
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1) ;
      panel1.setPreferredSize(new Dimension(400,400));
 		jTabbedPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      String s = Kisekae.getCaptions().getString("OptionsBoxTitle") ;
      Border cb2 = new CompoundBorder(new TitledBorder(s),eb2) ;
      Border cb3 = new CompoundBorder(new TitledBorder(s),eb2) ;
		gridLayout1.setColumns(2) ;
		gridLayout1.setRows(2) ;
		gridLayout2.setColumns(2) ;
		gridLayout2.setRows(2) ;
		jPanel1.setBorder(eb1) ;
		jPanel2.setBorder(cb2) ;
		jPanel2a.setBorder(eb1) ;
		jPanel3.setBorder(eb1) ;
		jPanel4.setBorder(eb1) ;
		jPanel5.setBorder(eb1) ;
		jPanel6.setBorder(cb3) ;
		jPanel6a.setBorder(eb1) ;
		jPanel1.setLayout(gridBagLayout1) ;
		jPanel2.setLayout(gridLayout1) ;
		jPanel2a.setLayout(borderLayout3) ;
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS)) ;
		jPanel4.setLayout(gridBagLayout2) ;
		jPanel5.setLayout(new BoxLayout(jPanel5,BoxLayout.X_AXIS)) ;
		jPanel6.setLayout(gridLayout2) ;
		jPanel6a.setLayout(borderLayout4) ;
		findlabel.setText(Kisekae.getCaptions().getString("TextToFindText")) ;
		wholewords.setText(Kisekae.getCaptions().getString("WholeWordsText")) ;
		matchcase.setText(Kisekae.getCaptions().getString("CaseSensitiveText")) ;
		searchup.setText(Kisekae.getCaptions().getString("SearchUpText")) ;
		searchdown.setText(Kisekae.getCaptions().getString("SearchDownText")) ;
		FINDNEXT.setText(Kisekae.getCaptions().getString("FindNextMessage")) ;
		CLOSE.setText(Kisekae.getCaptions().getString("CloseMessage")) ;

       //add search direction listener
		searchdown.setSelected(true) ;
		searchgroup.add(searchup) ;
		searchgroup.add(searchdown) ;
      searchup.addActionListener(new ActionListener() 
      { public void actionPerformed(ActionEvent e) { searchUp = true ; } });
      searchdown.addActionListener(new ActionListener() 
      { public void actionPerformed(ActionEvent e) { searchUp = false ; } });
      
		getContentPane().add(panel1);
		panel1.add(jTabbedPane, BorderLayout.CENTER) ;
		jTabbedPane.add(jPanel1, Kisekae.getCaptions().getString("MenuEditFind")) ;
		jPanel1.add(findlabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0)) ;
		jPanel1.add(findtext, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 190, 0)) ;
		jPanel1.add(jPanel2, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
		jPanel2.add(wholewords, null) ;
		jPanel2.add(searchup, null) ;
		jPanel2.add(matchcase, null) ;
		jPanel2.add(searchdown, null) ;
		jPanel1.add(jPanel2a, new GridBagConstraints(0, 2, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
		jPanel2a.add(statuslabel, BorderLayout.CENTER) ;
		jPanel1.add(jPanel3, new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
		jPanel3.add(Box.createGlue()) ;
		jPanel3.add(FINDNEXT, null) ;
		jPanel3.add(Box.createGlue()) ;
		jPanel3.add(CLOSE, null) ;
		jPanel3.add(Box.createGlue()) ;
  	}
   
   
   // Find a node in the tree.
   
   private DefaultMutableTreeNode find(DefaultMutableTreeNode root, boolean next, String s) 
   {
      if (s == null) return null ;
      if (root == null) return null ;
      boolean wwords = wholewords.isSelected() ;
      boolean casematch = matchcase.isSelected() ;
      if (!findnext) 
      {
         treescan = root.preorderEnumeration() ;
         if (searchUp) treescan = root.postorderEnumeration() ;
      }

      // Scan the tree.  Skip the first node. 
      
      boolean found = false ;
      while (treescan.hasMoreElements()) 
      {
         Object node = treescan.nextElement() ;
         String matchtext1 = node.toString() ;
         if (!wwords && matchtext1.toLowerCase().indexOf(s.toLowerCase()) >= 0) matchtext1 = s ;
         if (!casematch && matchtext1.equalsIgnoreCase(s)) found = true ;
         if (casematch && matchtext1.equals(s)) found = true ;
         if (found) return (DefaultMutableTreeNode) node ;
      }
      return null ;
   }
   
   
   // A utility function to display a message dialog.

	private void warning(String message)
   { warning(message, false) ; }
   
	private void warning(String message, boolean status)
   {
      if (status)
      {
         if (jTabbedPane.getSelectedIndex() == 0)
            statuslabel.setText(message) ;
      }
      else
      {
   		JOptionPane.showMessageDialog(null, message,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.INFORMATION_MESSAGE);
      }
	}
   
   void setValues() { }
}

