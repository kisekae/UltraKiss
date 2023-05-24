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



import java.awt.*;
import java.awt.event.* ;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.* ;
import java.util.* ;
import java.io.* ;

final class ZipFindDialog extends KissDialog
	implements ActionListener, WindowListener
{
	private static String initfind = "" ;		// Initial find text
   private int lastindex = -1 ;					// Index of last successful find

   // User interface objects

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel message = new JLabel();
	private JTextField searchtext = new JTextField();
	private JComboBox pathselect = new JComboBox();
	private JButton CANCEL = new JButton();
	private JButton FIND = new JButton();
	private JButton FINDALL = new JButton();

	// Create specialized listeners for events.

	KeyListener keyListener = new KeyListener()
   {
      public void keyReleased(KeyEvent e) { }
      public void keyTyped(KeyEvent e) { }
		public void keyPressed(KeyEvent e)
      {
      	if (e.getKeyChar() != KeyEvent.VK_ENTER) return ;
         FIND.doClick() ;
      }
	} ;


	// Constructor

	public ZipFindDialog(JFrame frame)
	{
		// Call the base class constructor to set up our dialog.
      // This is a non-modal dialog.

 		super(frame,Kisekae.getCaptions().getString("FindNameTitle"),false);

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
      setPathValues() ;
      pack() ;

		// Center the frame in the screen space.

		center(this) ;

      // Set listeners and show the dialog.

		FIND.addActionListener(this);
		FINDALL.addActionListener(this);
		CANCEL.addActionListener(this);
      searchtext.addKeyListener(keyListener) ;
 		addWindowListener(this);
      setVisible(true) ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton(FIND) ;
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
		jPanel1.setLayout(gridBagLayout1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jLabel1.setText(Kisekae.getCaptions().getString("FindPathText"));
		jLabel3.setText(Kisekae.getCaptions().getString("FindNameText"));
		searchtext.setMinimumSize(new Dimension(250, 21));
		searchtext.setPreferredSize(new Dimension(250, 21));
      searchtext.setText(initfind);
		FIND.setText(Kisekae.getCaptions().getString("FindMessage"));
		FINDALL.setText(Kisekae.getCaptions().getString("FindAllMessage"));
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		panel1.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel1.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
		message.setPreferredSize(new Dimension(250, 17));
		message.setHorizontalAlignment(SwingConstants.CENTER);

		pathselect.setMaximumRowCount(4);
		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel1.add(searchtext, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
		jPanel1.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
		jPanel1.add(pathselect, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
		jPanel1.add(message, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(FIND, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(FINDALL, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
 	 	Object source = evt.getSource() ;

		// A Find or Find Next searches the list of archive elements.

		if (source == FIND || source == FINDALL)
      {
			message.setText(""); ;
      	if (!(parent instanceof ZipManager)) return ;
         JTable table = ((ZipManager) parent).getTable() ;
         if (table == null) return ;
         TableModel model = table.getModel() ;
         if (model == null) return ;

	      // Establish the wildcard filter.

			File f = new File(searchtext.getText()) ;
			String fname = (f != null) ? f.getName() : "*.*" ;
			if (fname == null) fname = "*.*" ;
			StringTokenizer st = new StringTokenizer("?" + fname.toUpperCase(),".") ;
			String name = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
			name = name.substring(1) ;
			String ext = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
         String namemid = "" ;
         String extmid = "" ;

	      // Determine the wildcard position for name and extension.

			st = new StringTokenizer("?" + name,"*") ;
			String nameprefix = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
			nameprefix = nameprefix.substring(1) ;
			String namesuffix = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
         if (name.endsWith("*")) { namemid = namesuffix ; namesuffix = "" ; }
			st = new StringTokenizer("?" + ext,"*") ;
			String extprefix = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
			extprefix = extprefix.substring(1) ;
			String extsuffix = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
         if (ext.endsWith("*")) { extmid = extsuffix ; extsuffix = "" ; }

	      // Search the table entries.  Retain the indexes of the found entries.

         int found = 0 ;
         int [] index = new int[table.getRowCount()] ;
         if (source == FINDALL) lastindex = -1 ;
	      for (int i = lastindex+1 ; i < index.length ; i++)
	      {
         	Object o = model.getValueAt(i,0) ;
            if (!(o instanceof String)) continue ;
	        	f = new File((String) o) ;

            // Test for a matching path name.

            Object p = pathselect.getSelectedItem() ;
            String test = (p instanceof String) ? ((String) p).trim() : "" ;
            if (!("".equals(test)))
            {
	         	Object o2 = model.getValueAt(i,5) ;
		         String path = (o2 instanceof String) ? (String) o2 : "" ;
               if (!(path.equalsIgnoreCase(test))) continue ;
            }

	         // Test for a matching file name.

	         if (f != null)
	         {
		         fname = f.getName() ;
			      st = new StringTokenizer(fname.toUpperCase(),".") ;
			      name = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
					ext = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
		         if (!(name.startsWith(nameprefix))) continue ;
               if (!(name.indexOf(namemid) >= 0)) continue ;
		         if (!(name.endsWith(namesuffix))) continue ;
		         if (!(ext.startsWith(extprefix))) continue ;
               if (!(ext.indexOf(extmid) >= 0)) continue ;
		         if (!(ext.endsWith(extsuffix))) continue ;

	            // The name is valid.  Select this entry in the list
               // if we are just looking for one entry.

               lastindex = i ;
               index[found++] = i ;
               if (source == FIND)
               {
                  String s1 = Kisekae.getCaptions().getString("FoundFileText") ;
                  int i1 = s1.indexOf('[') ;
                  int j1 = s1.indexOf(']') ;
                  if (i1 >= 0 && j1 > i1)
                     s1 = s1.substring(0,i1) + f.getPath() + s1.substring(j1+1) ;
	               message.setText(s1) ;
	               table.setRowSelectionInterval(lastindex,lastindex) ;
			         ((ZipManager) parent).setViewRow(lastindex) ;
	               FIND.setText(Kisekae.getCaptions().getString("FindNextMessage")) ;
               	return ;
               }
            }
         }

         // Done.  Select all found entries.

         lastindex = -1 ;
         FIND.setText(Kisekae.getCaptions().getString("FindMessage")) ;
         String s1 = Kisekae.getCaptions().getString("FindResultText") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + found + s1.substring(j1+1) ;
         message.setText(s1) ;
         if (source == FINDALL && found > 0)
         {
            for (int i = 0 ; i < found ; i++)
            {
            	if (i == 0)
	               table.setRowSelectionInterval(index[i],index[i]) ;
               else
	               table.addRowSelectionInterval(index[i],index[i]) ;
            }
	         ((ZipManager) parent).setViewRow(index[0]) ;
         }
         return ;
      }

      // A Cancel request closes the dialog.

		if (source == CANCEL)
      {
         close() ;
         return ;
      }
   }


   // A function to set up valid path values in the path selection box.

   private void setPathValues()
   {
     	if (!(parent instanceof ZipManager)) return ;
      JTable table = ((ZipManager) parent).getTable() ;
      if (table == null) return ;
      TableModel model = table.getModel() ;
      if (model == null) return ;

      // Search the list entries for path names.  These are in field 5.
      // If we have a path name then we store it in a hash table to remove
      // duplicates.

      Hashtable paths = new Hashtable() ;
      for (int i = 0 ; i < table.getRowCount() ; i++)
      {
        	Object o = model.getValueAt(i,5) ;
         if (!(o instanceof String)) continue ;
         String path = (String) o ;
         if ("".equals(path)) continue ;
         paths.put(path.toUpperCase(),path) ;
      }

      // Populate the selection box with the known paths.
      // Ensure that a generic blank path exists.

      pathselect.addItem(" ");
      Collection c = paths.values() ;
      Iterator iter = c.iterator() ;
      while (iter.hasNext()) pathselect.addItem(iter.next()) ;
   }


   // Method to close this dialog.

   void close()
   {
		flush() ;
		dispose() ;
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { flush() ; dispose() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
      setVisible(false) ;
      initfind = searchtext.getText() ;
		FIND.removeActionListener(this);
		FINDALL.removeActionListener(this);
		CANCEL.removeActionListener(this);
      searchtext.removeKeyListener(keyListener) ;
 		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
   
   void setValues() {
   }
   
}
