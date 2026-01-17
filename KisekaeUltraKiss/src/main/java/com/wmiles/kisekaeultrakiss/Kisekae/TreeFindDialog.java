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
* TreeFindDialog Class
*
* Purpose:
*
* This object is a find dialog for our general event tree lists.
* It is a non-modal dialog that can be used to locate text in a tree
* by searching all tree nodes.
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



class TreeFindDialog extends KissDialog
{
	private KissFrame owner = null ;				// Parent frame
	private JDialog me = null ;               // Ourselves
   private JTree TREE = null ;               // Tree to search
   private DefaultMutableTreeNode node = null ; // Node to test
	private Document docFind;                 // Control data
	private Document docReplace;
	private ButtonModel modelWord;
	private ButtonModel modelCase;
	private ButtonModel modelUp;
	private ButtonModel modelDown;
	private boolean findreplace = false ;     // dialog type

   // Search variables.

	private int pos = 0 ;
	private int searchIndex = -1 ;
   private Point textlocation = new Point(-1,-1) ;
	private boolean searchUp = false ;
	private String searchData ;
   private DefaultMutableTreeNode selectednode = null ; // Selected TREE node
	private DefaultMutableTreeNode top = null ;

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
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private GridBagLayout gridBagLayout4 = new GridBagLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private GridLayout gridLayout2 = new GridLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;

	private JButton FINDNEXT = new JButton();
	private JButton CLOSE = new JButton();
	private JButton CLOSE1 = new JButton();
	private JButton REPLACE = new JButton();
	private JButton REPLACEALL = new JButton();
	private JLabel findlabel = new JLabel();
	private JLabel findlabel1 = new JLabel();
	private JLabel replacelabel = new JLabel();
	private JLabel statuslabel = new JLabel();
	private JLabel statuslabel1 = new JLabel();
   private JTextField findtext = new JTextField() ;
   private JTextField findtext1 = new JTextField() ;
   private JTextField replacetext = new JTextField() ;

   private JCheckBox wholewords = new JCheckBox();
   private JCheckBox matchcase = new JCheckBox();
   private ButtonGroup searchgroup = new ButtonGroup() ;
	private JRadioButton searchup = new JRadioButton();
	private JRadioButton searchdown = new JRadioButton();
   private JCheckBox wholewords1 = new JCheckBox();
   private JCheckBox matchcase1 = new JCheckBox();
   private ButtonGroup searchgroup1 = new ButtonGroup() ;
	private JRadioButton searchup1 = new JRadioButton();
	private JRadioButton searchdown1 = new JRadioButton();



   // Constructor

	public TreeFindDialog(KissFrame parent, int index)
	{
		super(parent,Kisekae.getCaptions().getString("FindTitle"),false) ;
      findreplace = (index >= 0) ;
		owner = parent ;
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

		// Establish control models.

		docFind =  findtext.getDocument() ;
		docReplace = replacetext.getDocument() ;
		modelWord = wholewords.getModel() ;
		modelUp = searchup.getModel() ;
		modelCase = matchcase.getModel() ;
		modelDown = searchdown.getModel() ;
		searchup1.setModel(modelUp) ;
		searchdown1.setModel(modelDown) ;
		matchcase1.setModel(modelCase) ;
		wholewords1.setModel(modelWord) ;
		findtext1.setDocument(docFind) ;

		// Register for events.  Find Next action.

		ActionListener findAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
            owner.setFocus() ;
            statuslabel.setText("") ;
            int n = findNext(false) ;
            if (n == 0 && !searchUp) 
            {
               owner.setSelection(0,0,searchUp) ;
               JTextComponent monitor = owner.getTextComponent() ;
               if (monitor != null) 
               {
                  monitor.setCaretPosition(0) ;
                  monitor.setEditable(true) ;
               }
            }
				me.requestFocus() ;
         }
		} ;
      FINDNEXT.addActionListener(findAction) ;

		// Register for events.  Close action.

      ActionListener closeAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
            owner.setSelection(textlocation,searchUp) ; 
				setVisible(false) ;
			}
		} ;
		CLOSE.addActionListener(closeAction) ;
		CLOSE.setDefaultCapable(true) ;
		CLOSE1.addActionListener(closeAction) ;
		CLOSE1.setDefaultCapable(true) ;

		// Register for events.  Window activate and deactivate action.

		WindowListener flst = new WindowAdapter()
      {
			public void windowOpened(WindowEvent e)
         { windowActivated(e) ; }

			public void windowActivated(WindowEvent e)
         {
            validate() ;
				searchIndex = -1 ;
				if (jTabbedPane.getSelectedIndex() == 0)
					findtext.grabFocus(); 
				else
					findtext1.grabFocus();
			}

			public void windowDeactivated(WindowEvent e)
			{ searchData = null ; }
         
         public void windowGainedFocus(WindowEvent e) 
         { windowActivated(e) ; }

		} ;
		addWindowListener(flst) ;
      
      // Set required values.
      
      statuslabel.setHorizontalAlignment(SwingConstants.CENTER);
      statuslabel1.setHorizontalAlignment(SwingConstants.CENTER);
      setValues() ;
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1) ;
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
		jPanel1.setLayout(gridBagLayout1) ;
		jPanel2.setLayout(gridLayout1) ;
		jPanel2a.setLayout(borderLayout3) ;
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS)) ;
		jPanel4.setLayout(gridBagLayout2) ;
		jPanel5.setLayout(new BoxLayout(jPanel5,BoxLayout.X_AXIS)) ;
		jPanel6.setLayout(gridLayout2) ;
		findlabel.setText(Kisekae.getCaptions().getString("TextToFindText")) ;
		findlabel1.setText(Kisekae.getCaptions().getString("TextToFindText")) ;
		replacelabel.setText(Kisekae.getCaptions().getString("ReplaceWithText")) ;
		wholewords.setText(Kisekae.getCaptions().getString("WholeWordsText")) ;
		matchcase.setText(Kisekae.getCaptions().getString("CaseSensitiveText")) ;
		searchup.setText(Kisekae.getCaptions().getString("SearchUpText")) ;
		searchdown.setSelected(true) ;
		searchdown.setText(Kisekae.getCaptions().getString("SearchDownText")) ;
		wholewords1.setText(Kisekae.getCaptions().getString("WholeWordsText")) ;
		matchcase1.setText(Kisekae.getCaptions().getString("CaseSensitiveText")) ;
		searchup1.setText(Kisekae.getCaptions().getString("SearchUpText")) ;
		searchdown1.setSelected(true) ;
		searchdown1.setText(Kisekae.getCaptions().getString("SearchDownText")) ;
		FINDNEXT.setText(Kisekae.getCaptions().getString("FindNextMessage")) ;
		CLOSE.setText(Kisekae.getCaptions().getString("CancelMessage")) ;
		CLOSE1.setText(Kisekae.getCaptions().getString("CancelMessage")) ;
		REPLACE.setText(Kisekae.getCaptions().getString("ReplaceMessage")) ;
		REPLACEALL.setText(Kisekae.getCaptions().getString("ReplaceAllMessage")) ;

		searchgroup.add(searchup) ;
		searchgroup.add(searchdown) ;
		searchgroup1.add(searchup1) ;
		searchgroup1.add(searchdown1) ;

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


	// Method to return the selected text location.

	public Point getSelected() { return textlocation ; }


   // Method to search the document for the required text.
   // Return values:  -1 signals an error
   // 0 signals the target text cannot be found
   // 1 signifies that a find or replace was successful.

	private int findNext(boolean doReplace)
	{
      if (top == null)
      {
         top = (DefaultMutableTreeNode) TREE.getModel().getRoot() ;
         TREE.setSelectionPath(new TreePath(top.getPath())) ;
         node = top ;
      }
   
      // Search for next occurance.
      
      node = searchNode(findtext.getText()) ;
      if (node == null) 
      {
         top = null ;
			warning(Kisekae.getCaptions().getString("NoFindResultText"),true) ;
         return 0 ;
      }
      
      // Expand found node.

      TreePath path = new TreePath(node.getPath()) ;
      TREE.setSelectionPath(path);
      TREE.expandPath(path) ;
      TREE.scrollPathToVisible(path) ;

      // Continue search from next node.
      
      node = node.getNextNode() ;
      return 1 ;
	}

   
   // Method to traverse the tree based on the last node found.

   private DefaultMutableTreeNode searchNode(String nodeStr) 
   { 
      if (nodeStr == null || "".equals(nodeStr)) return null ;
      
      if (modelWord.isSelected())
         nodeStr = " " + nodeStr.trim() + " " ;
         
      while (node != null) 
      {
         if (modelUp.isSelected())
            node = node.getPreviousNode() ;
         else
            node = node.getNextNode() ;
         if (node == null) break ;
         
         Object o = node.getUserObject() ;
         if (o != null)
         {   
            String s1 = o.toString() ;
            String s2 = nodeStr ;
            if (!modelCase.isSelected())
            {
               s1 = s1.toUpperCase() ;
               s2 = s2.toUpperCase() ;
            }        
            if (s1.contains(s2)) return node;
         }
      }
      return null;
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
			else
            statuslabel1.setText(message) ;
      }
      else
      {
   		JOptionPane.showMessageDialog(null, message,
            Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
            JOptionPane.INFORMATION_MESSAGE);
      }
	}
   
   void setValues() 
   { 
      TREE = owner.getTree() ;
   }
}

