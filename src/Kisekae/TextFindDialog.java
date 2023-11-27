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
* TextFindDialog Class
*
* Purpose:
*
* This object is a find/replace dialog for our general text file editor.
* It is a non-modal dialog that can be used to locate text in a document
* and optionally replace the text contents.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.text.* ;


class TextFindDialog extends KissDialog
{
	private KissFrame owner = null ;				// Parent frame
	private JDialog me = null ;               // Ourselves
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
   private int startcaret = 0 ;

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


	// Create specialized listeners for events.

	ChangeListener tablistener = new ChangeListener()
	{
		public void stateChanged(ChangeEvent e)
		{
         statuslabel.setText("") ;
         statuslabel1.setText("") ;
			if (jTabbedPane == null) return ;
			int n = jTabbedPane.getSelectedIndex() ;
			JRootPane rootpane = getRootPane()  ;
			rootpane.setDefaultButton((n == 0) ? FINDNEXT : REPLACE) ;
		}
	} ;


   // Constructor

	public TextFindDialog(KissFrame parent, int index)
	{
		super(parent,Kisekae.getCaptions().getString("FindReplaceTitle"),false) ;
      if (index < 0) setTitle(Kisekae.getCaptions().getString("FindTitle")) ;
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

		// Register for events.  Replace action.

		ActionListener replaceAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
            owner.setFocus() ;
            statuslabel1.setText("") ;
            int n = findNext(true) ;
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
		REPLACE.addActionListener(replaceAction) ;

		// Register for events.  Replace All action.

		ActionListener replaceAllAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
            statuslabel1.setText("") ;
				int result = replaceAll() ;
				if (result < 0) return;
            String s1 = Kisekae.getCaptions().getString("ReplaceResultText") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + result + s1.substring(j1+1) ;
            warning(s1,true) ;
			}
		} ;
		REPLACEALL.addActionListener(replaceAllAction) ;

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
      

      // Initialize and show dialog. A request to show a negative
      // index disables replacement and shows a find only dialog.

      statuslabel.setHorizontalAlignment(SwingConstants.CENTER);
      statuslabel1.setHorizontalAlignment(SwingConstants.CENTER);
		jTabbedPane.addChangeListener(tablistener) ;
		setSelectedIndex(index) ;
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
		CLOSE.setText(Kisekae.getCaptions().getString("CloseMessage")) ;
		CLOSE1.setText(Kisekae.getCaptions().getString("CloseMessage")) ;
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
      if (!findreplace) return ;
      
		jTabbedPane.add(jPanel4, Kisekae.getCaptions().getString("MenuEditReplace")) ;
		jPanel4.add(findlabel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0)) ;
		jPanel4.add(findtext1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 190, 0)) ;
		jPanel4.add(replacelabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
				,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0)) ;
		jPanel4.add(replacetext, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 190, 0)) ;
		jPanel4.add(jPanel6, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
      jPanel6.add(wholewords1,null) ;
      jPanel6.add(searchup1,null) ;
      jPanel6.add(matchcase1,null) ;
      jPanel6.add(searchdown1,null) ;
		jPanel4.add(jPanel6a, new GridBagConstraints(0, 3, 3, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
		jPanel6a.add(statuslabel1, BorderLayout.CENTER) ;
		jPanel4.add(jPanel5, new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 0, 0), 0, 0)) ;
		jPanel5.add(Box.createGlue()) ;
		jPanel5.add(REPLACE, null) ;
		jPanel5.add(Box.createGlue()) ;
		jPanel5.add(REPLACEALL, null) ;
		jPanel5.add(Box.createGlue()) ;
		jPanel5.add(CLOSE1, null) ;
		jPanel5.add(Box.createGlue()) ;
	}


   // Method to set the required tab and show the dialog.

	public void setSelectedIndex(int index)
   {
		searchIndex = -1 ;
      if (index < 0) index = 0 ;
		jTabbedPane.setSelectedIndex(index) ;
		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((index == 0) ? FINDNEXT : REPLACE) ;
		setVisible(true) ;
	}


	// Method to return the selected text location.

	public Point getSelected() { return textlocation ; }


   // Method to search the document for the required text.
   // Return values:  -1 signals an error
   // 0 signals the target text cannot be found
   // 1 signifies that a find or replace was successful.

	private int findNext(boolean doReplace)
	{
		String key = "" ;
      Document doc = owner.getDocument() ;
      JTextComponent monitor = owner.getTextComponent() ;
      if (doc == null) return -1 ;
      if (monitor == null) return -1 ;
      
      if (doReplace) monitor.setCaretPosition(startcaret) ;
		int caret = monitor.getCaretPosition();
      startcaret = caret ;
      boolean directionchange = false ;

		// Watch for up/down direction changes.

		if (modelUp.isSelected() != searchUp)
      {
			searchUp = modelUp.isSelected() ;
			searchIndex = -1 ;
         directionchange = true ;
		}

      // If searchindex is -1 then the text to be searched through must
      // be re-established.  The portion of text to be searched is the
      // text from the beginning of the document to the caret position
      // if we are searching up, or the text from the caret position to
      // the end of the document if we are searching down.

		if (searchIndex == -1)
      {
         // Retain our document position as our search index.

			searchIndex = caret ;
			try
         {
				if (searchUp)
            {
					searchData = doc.getText(0, caret) ;
               pos = caret ;
            }
				else
            {
					searchData = doc.getText(caret, doc.getLength()-caret) ;
               pos = 0 ;
            }
			}
			catch (BadLocationException ex)
         {
				warning(ex.toString()) ;
				return -1 ;
			}
		}

      // Get the target text.

		key = "" ;
		try { key = docFind.getText(0, docFind.getLength()) ; }
		catch (BadLocationException ex) {}
		if (key.length() == 0)
      {
			warning(Kisekae.getCaptions().getString("FindQueryText"),true) ;
			return -1 ;
		}

      // If we are case insensitive, convert both the target text and the
      // search data to lower case.

		if (!modelCase.isSelected())
      {
			searchData =  searchData.toLowerCase() ;
			key = key.toLowerCase() ;
		}

      // Get the replacement text.

		String replacement = "" ;
		if (doReplace)
      {
			try
         { replacement = docReplace.getText(0, docReplace.getLength()); }
         catch (BadLocationException ex) { }
		}

      // Perform the search.  We use the string search functions to find
      // the target text in the search data string.

		int xStart = -1;
		int xFinish = -1;
		while (true)
		{
			if (searchUp)
				xStart = searchData.lastIndexOf(key, pos-1) ;
			else
				xStart = searchData.indexOf(key, pos) ;
			if (xStart < 0)
         {
				warning(Kisekae.getCaptions().getString("NoFindResultText"),true) ;
            textlocation = new Point(-1,-1) ;
				return 0 ;
			}

         // If we have changed direction and located the previous
         // selection in the document then we need to search for the
         // next occurance of the target text.

			xFinish = xStart + key.length();
         if (directionchange)
         {
          	if (searchUp)
            {
            	if (textlocation.x == xStart && textlocation.y == xFinish)
               	if (xStart > 0)
                  	{ pos = xStart - 1 ; continue ; }
            }
            else
            {
            	if (textlocation.x == xStart+searchIndex && textlocation.y == xFinish+searchIndex)
               	if (xFinish < searchData.length())
                  	{ pos = xFinish + 1 ; continue ; }
            }
         }

         // If we are looking for words then we must check if the found text
         // is a word.  This requires checking the character immediately
         // preceding and following the found text.

			if (modelWord.isSelected())
         {
				boolean s1 = xStart > 0 ;
				boolean b1 = s1 && !Character.isLetterOrDigit(searchData.charAt(xStart-1)) ;
            b1 = (b1 || !s1) ;
				boolean s2 = xFinish < searchData.length() ;
				boolean b2 = s2 && !Character.isLetterOrDigit(searchData.charAt(xFinish)) ;
            b2 = (b2 || !s2) ;

				if (!(b1 && b2))    // Not a whole word
				{
					if (searchUp && s1)    // Can continue up
					{
						pos = xStart ;
						continue ;
					}
					if (!searchUp && s2)    // Can continue down
					{
						pos = xFinish ;
						continue ;
					}

					// Found, but not a whole word, and we cannot continue.

					warning(Kisekae.getCaptions().getString("NoFindResultText"),true) ;
               textlocation = new Point(-1,-1) ;
					return 0 ;
				}
			}

         // We have a selection.  Terminate the loop.

			break;
		}

      // Retain our position for the next search.

      if (searchUp) pos = xStart ; else pos = xFinish ;

      // Turn our search data relative indexes into document relative indexes.
      // We have found our text and this should be inclusive in the document.

     	if (!searchUp)
      {
			xStart += searchIndex ;
			xFinish += searchIndex ;
		}

      // Make the replacement and highlight the selected text.

      if (doReplace)
      {
         owner.setSelection(xStart,xFinish,searchUp) ;
         monitor.replaceSelection(replacement) ;
         owner.setSelection(xStart,xStart+replacement.length(),searchUp) ;
         searchIndex = -1 ;
      }
      else
      {
         owner.setSelection(xStart,xFinish,searchUp) ;
      }
 
      // Retain the location of the found text.

      textlocation = new Point(xStart,xFinish) ;
		return 1;
	}


   // Method to replace all entries in the required text.
   // Return values:  -1 signals an error

	private int replaceAll()
	{
      Document doc = owner.getDocument() ;
      JTextComponent monitor = owner.getTextComponent() ;
		if (doc == null) return -1 ;
      if (monitor == null) return -1 ;
      monitor.setCaretPosition(startcaret) ;
		int caret = monitor.getCaretPosition();
      startcaret = caret ;
      StringBuffer sb = new StringBuffer() ;
      String baseData = null ;

      // The text to be searched through must be established.  The portion of
      // text to be searched is the text from the beginning of the document to
      // the caret position if we are searching up, or the text from the caret
      // position to the end of the document if we are searching down.

      pos = 0 ;
		searchIndex = caret ;
		searchUp = modelUp.isSelected() ;
		try
      {
			baseData = (searchUp) ? doc.getText(0,caret)
			   : doc.getText(caret,doc.getLength()-caret) ;
      }
		catch (BadLocationException ex)
      {
			warning(ex.toString()) ;
			return -1 ;
		}

      // Get the target text.

		String key = "" ;
		try { key = docFind.getText(0, docFind.getLength()) ; }
		catch (BadLocationException ex) {}
		if (key.length() == 0)
      {
			warning(Kisekae.getCaptions().getString("FindQueryText"),true) ;
			return -1 ;
		}

      // If we are case insensitive, convert both the target text and the
      // search data to lower case.

      searchData = baseData ;
		if (!modelCase.isSelected())
      {
			searchData = searchData.toLowerCase() ;
			key = key.toLowerCase() ;
		}

      // Get the replacement text.

		String replacement = "" ;
		try { replacement = docReplace.getText(0, docReplace.getLength()); }
      catch (BadLocationException ex) { }

      // Perform the search.  We use the string search functions to find
      // the target text in the search data string.  We construct a buffer
      // that contains all original and replaced text.

      int counter = 0 ;
      int lastpos = pos ;
		while (true)
		{
			int xStart = searchData.indexOf(key,pos) ;
			int xFinish = xStart + key.length();
			if (xStart < 0) break ;

         // If we are looking for words then we must check if the found text
         // is a word.  This requires checking the character immediately
         // preceding and following the found text.

			if (modelWord.isSelected())
         {
				boolean s1 = xStart > 0 ;
				boolean b1 = s1 && !Character.isLetterOrDigit(searchData.charAt(xStart-1)) ;
            b1 = (b1 || !s1) ;
				boolean s2 = xFinish < searchData.length() ;
				boolean b2 = s2 && !Character.isLetterOrDigit(searchData.charAt(xFinish)) ;
            b2 = (b2 || !s2) ;

				if (!(b1 && b2))    // Not a whole word
				{
					if (s2)          // Can continue
					{
						pos = xFinish ;
						continue ;
					}
				}
			}

         // Make the replacement.

         sb.append(baseData.substring(lastpos,xStart)) ;
         sb.append(replacement) ;
         pos = xFinish ;
         lastpos = pos ;
         counter++ ;
      }

      // All done.  Replace document text.

      if (counter > 0)
      {
			try
         {
            sb.append(baseData.substring(lastpos,baseData.length()-1)) ;
				if (searchUp)
            {
               doc.remove(0,caret) ;
               doc.insertString(0,sb.toString(),null) ;
            }
				else
            {
					doc.remove(caret,doc.getLength()-caret) ;
               doc.insertString(caret,sb.toString(),null) ;
            }
			}
			catch (BadLocationException ex)
         {
				warning(ex.toString()) ;
				return -1 ;
			}
      }

      // Return the number of items replaced.
      
      monitor.setCaretPosition(startcaret);
      textlocation = new Point(startcaret,startcaret) ;
  		return counter ;
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
   
   void setValues() { }
}

