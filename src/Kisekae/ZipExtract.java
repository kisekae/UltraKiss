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
import java.util.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.* ;
import javax.help.* ;


final class ZipExtract extends KissDialog
	implements ActionListener, WindowListener
{
	private ImageIcon ICON_COMPUTER = null ;
	private ImageIcon ICON_DISK = null ;
	private ImageIcon ICON_FOLDER = null ;
	private ImageIcon ICON_EXPANDEDFOLDER = null ;

	// Dialog attributes

   private JDialog me = null ;						// Reference to ourselves
   private ArchiveFile zip = null ;					// Reference to archive file
   private Vector items = null ;						// Reference to selection items
   private URL iconfile = null ;						// URL of icon image
   private String directory = null ;				// Extract directory
   private int row = 0 ;                        // Selection row

   // Help set objects

	private static String helpset = "Help/ArchiveManager.hs" ;
	private HelpSet hs = null ;
	private HelpBroker hb = null ;

   // User interface objects.

	private JPanel panel1 = new JPanel();
   private JPanel jPanel1 = new JPanel();
   private JPanel jPanel2 = new JPanel();
   private JPanel jPanel3 = new JPanel();
   private JPanel jPanel4 = new JPanel();
   private JPanel jPanel5 = new JPanel();
   private JPanel jPanel6 = new JPanel();
   private JTree TREE = null ;
	private DefaultMutableTreeNode top = null ;
   private DefaultTreeModel treemodel = null ;
   private JScrollPane jScrollPane1 = new JScrollPane();
   private TitledBorder titledBorder1;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
   private JTextField extractdirectory = new JTextField();
   private JRadioButton selectedbutton = new JRadioButton();
   private JRadioButton allbutton = new JRadioButton();
   private JCheckBox overwrite = new JCheckBox();
   private JCheckBox skipolder = new JCheckBox();
   private JCheckBox usefoldernames = new JCheckBox();
   private JLabel jLabel2 = new JLabel();
   private JButton EXTRACT = new JButton();
   private JButton CANCEL = new JButton();
   private JButton HELP = new JButton();
   private JButton NEWDIRECTORY = new JButton();
   private GridLayout gridLayout1 = new GridLayout();
   private GridLayout gridLayout2 = new GridLayout();
   private GridLayout gridLayout3 = new GridLayout();
   private GridBagLayout gridBagLayout1 = new GridBagLayout();
   private GridBagLayout gridBagLayout2 = new GridBagLayout();
   private GridBagLayout gridBagLayout3 = new GridBagLayout();
   private GridBagLayout gridBagLayout4 = new GridBagLayout();

   // A tree selection event updates our extract directory text field.

	TreeSelectionListener treeListener = new TreeSelectionListener()
   {
      public void valueChanged(TreeSelectionEvent event)
      {
         DefaultMutableTreeNode node = getTreeNode(event.getPath()) ;
         FileNode fnode = getFileNode(node) ;
         if (fnode != null)
            extractdirectory.setText(fnode.getFile().getAbsolutePath()) ;
         else if (zip != null)
            extractdirectory.setText(zip.getDirectoryName()) ;
         else
            extractdirectory.setText("") ;
      }
	} ;



	// Constructor

	public ZipExtract(JFrame f, ArchiveFile zip, Vector items)
	{
   	super(f,Kisekae.getCaptions().getString("ArchiveManagerExtractTitle"),true) ;
      me = this ;
      init(zip,items) ;
   }

   private void init(ArchiveFile zip, Vector items)
   {
      this.zip = zip ;
      this.items = items ;
      String s1 = Kisekae.getCaptions().getString("ArchiveManagerExtractFromTitle") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1 && zip != null)
         s1 = s1.substring(0,i1) + zip.getName() + s1.substring(j1+1) ;
      if (zip != null) setTitle(s1) ;

      // Construct the user interface.

		try { jbInit(); pack(); }
      catch(Exception ex)
      {
         System.out.println("ZipExtract: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + ex.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }

      // Set the initial tree icon display values.

		iconfile = Kisekae.getResource("Images/computer.gif") ;
		ICON_COMPUTER = new ImageIcon(iconfile) ;
		iconfile = Kisekae.getResource("Images/disk.gif") ;
		ICON_DISK = new ImageIcon(iconfile) ;
		iconfile = Kisekae.getResource("Images/folder.gif") ;
		ICON_FOLDER = new ImageIcon(iconfile) ;
		iconfile = Kisekae.getResource("Images/expandedfolder.gif") ;
		ICON_EXPANDEDFOLDER = new ImageIcon(iconfile) ;
      setValues() ;

		// Center the frame in the panel space.

 		center(this) ;

		// Find the HelpSet file and create the HelpSet broker.

		try
		{
			ClassLoader loader = this.getClass().getClassLoader() ;
			URL hsURL = Kisekae.getResource(helpset) ;
			hs = new HelpSet(loader,hsURL) ;
			hb = hs.createHelpBroker() ;
		}
		catch (Throwable e)
		{
			System.out.println("HelpSet " + helpset + " " + e.getMessage()) ;
		}

		// Register for events.

		if (hb != null) HELP.addActionListener(new CSH.DisplayHelpFromSource(hb)) ;
      CANCEL.addActionListener(this);
      EXTRACT.addActionListener(this);
      NEWDIRECTORY.addActionListener(this);
      TREE.addTreeSelectionListener(treeListener) ;
      TREE.addTreeExpansionListener(new ExpansionListener()) ;
		addWindowListener(this);
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
      String s = Kisekae.getCaptions().getString("FilesBoxTitle") ;
      titledBorder1 = new TitledBorder(s);
      gridLayout1.setColumns(1);
      gridLayout1.setRows(2);
      gridLayout2.setColumns(1);
      gridLayout2.setRows(3);
      selectedbutton.setToolTipText(Kisekae.getCaptions().getString("ToolTipSelected"));
		selectedbutton.setText(Kisekae.getCaptions().getString("SelectedFilesText"));
      allbutton.setToolTipText(Kisekae.getCaptions().getString("ToolTipAll"));
		allbutton.setText(Kisekae.getCaptions().getString("AllFilesText"));
      overwrite.setToolTipText(Kisekae.getCaptions().getString("ToolTipOverwrite"));
      overwrite.setText(Kisekae.getCaptions().getString("OverwriteText"));
		overwrite.setSelected(true);
      skipolder.setToolTipText(Kisekae.getCaptions().getString("ToolTipOlder"));
		skipolder.setText(Kisekae.getCaptions().getString("SkipOlderText"));
      usefoldernames.setToolTipText(Kisekae.getCaptions().getString("ToolTipPath"));
		usefoldernames.setText(Kisekae.getCaptions().getString("UseFolderText"));
      usefoldernames.setSelected(true) ;
      panel1.setLayout(gridBagLayout4);
      jPanel1.setLayout(gridBagLayout1);
      jPanel2.setLayout(gridBagLayout2);
      jPanel3.setLayout(gridBagLayout3);
      jPanel4.setLayout(gridLayout1);
      jPanel4.setBorder(titledBorder1);
      jPanel5.setLayout(gridLayout2);
      jPanel6.setLayout(gridLayout3);
		gridLayout3.setColumns(1);
      gridLayout3.setRows(2);
      jLabel2.setText(Kisekae.getCaptions().getString("ExtractToText"));
      jScrollPane1.setMinimumSize(new Dimension(300, 235));
		jScrollPane1.setPreferredSize(new Dimension(300, 235));
		EXTRACT.setMnemonic('E');
		EXTRACT.setText(Kisekae.getCaptions().getString("ExtractMessage"));
		CANCEL.setMnemonic('C');
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		HELP.setMnemonic('H');
      HELP.setText(Kisekae.getCaptions().getString("HelpMessage"));
		NEWDIRECTORY.setMargin(new Insets(2, 5, 2, 5));
		NEWDIRECTORY.setMnemonic('N');
		NEWDIRECTORY.setText(Kisekae.getCaptions().getString("NewDirectoryText"));
      jPanel1.setBorder(eb1);
      jPanel2.setBorder(eb1);
      jPanel3.setBorder(eb1);
		jPanel5.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));

		getContentPane().add(panel1);
      panel1.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      jPanel1.add(jPanel5, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
      jPanel5.add(overwrite, null);
      jPanel5.add(skipolder, null);
      jPanel5.add(usefoldernames, null);
      jPanel1.add(jPanel4, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
      jPanel4.add(selectedbutton, null);
      jPanel4.add(allbutton, null);
      jPanel1.add(jPanel6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      panel1.add(jPanel2, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel2.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
      jPanel2.add(jScrollPane1, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 0, 0));
		jPanel2.add(extractdirectory, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
      panel1.add(jPanel3, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      jPanel3.add(EXTRACT, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(40, 0, 5, 0), 0, 0));
      jPanel3.add(CANCEL, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel3.add(HELP, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 5, 0), 0, 0));
      jPanel3.add(NEWDIRECTORY, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(100, 0, 5, 0), 0, 0));
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

		// An Extract invokes the file writer to extract the selected elements.

		if (source == EXTRACT)
		{
      	directory = extractdirectory.getText() ;
         if (directory == null) return ;
         File f = new File(directory) ;

         // Does the directory exist?
         // Do we have a valid directory?

         if (!f.exists() || !f.isDirectory())
         {
            JOptionPane.showMessageDialog(me,
               f.getAbsolutePath() + "\n" +
               Kisekae.getCaptions().getString("FDNoExistError"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.WARNING_MESSAGE) ;
            return ;
         }

			// Open the zip file.

			Vector contents = new Vector() ;
			FileOpen fileopen = zip.getFileOpen() ;
         if (fileopen == null) 
         {
            fileopen = new FileOpen(getParentFrame(),zip.pathname,"r") ;
            fileopen.setFileFilter("kissarchives") ;
            fileopen.setZipFile(zip) ;
         }
			fileopen.open() ;
			zip = fileopen.getZipFile() ;
			if (zip == null) return ;

         // Identify the archive elements to be extracted.  If we are
         // extracting selected elements we must identify the zip file
         // entry from the list name and path.

         if (selectedbutton.isSelected())
         {
          	for (int i = 0 ; i < items.size() ; i++)
            {
            	Object item = items.elementAt(i) ;
			      if (!(item instanceof String)) continue ;
					ArchiveEntry ze = zip.getEntry((String) item) ;
               if (ze == null) continue ;
               if (ze.getSize() <= 0) continue ;
	            ze.setSaveDir(usefoldernames.isSelected());
	            ze.setCheckDate(skipolder.isSelected());
               contents.addElement(ze) ;
            }
         }

			// If we are extracting all elements, each entry in the zip file
         // is an archive entry.

         if (allbutton.isSelected())
         {
         	Enumeration enum1 = zip.entries() ;
            while (enum1.hasMoreElements())
            {
            	ArchiveEntry ze = (ArchiveEntry) enum1.nextElement() ;
               if (ze == null) continue ;
               if (ze.getSize() <= 0) continue ;
	            ze.setSaveDir(usefoldernames.isSelected());
	            ze.setCheckDate(skipolder.isSelected());
               contents.addElement(ze) ;
            }
         }

         // Perform the file extraction.

			FileWriter fw = new FileWriter(getParentFrame(),zip,directory,contents) ; 
	      if (parent instanceof ActionListener)
				fw.callback.addActionListener((ActionListener) parent);
			Thread thread = new Thread(fw) ;
         if (parent instanceof ZipManager)
         {
            ((ZipManager) parent).setFileWriter(fw) ;
            ((ZipManager) parent).setBusy() ;
         }
        	close() ;
         dispose() ;
	      thread.start() ;
         return ;
		}

		// A CANCEL closes this dialog.

		if (source == CANCEL)
		{
        	close() ;
         dispose() ;
			return ;
		}

		// A New Folder creates a new directory for output.

		if (source == NEWDIRECTORY)
		{
      	ZipDirectory zd = new ZipDirectory(this,extractdirectory.getText()) ;
         zd.setVisible(true) ;
         String dir = zd.getDirectory() ;
         if (dir != null)
         {
            directory = dir ;
            extractdirectory.setText(directory) ;

	      	// Rebuild the top level directory tree.

            top.removeAllChildren() ;
		      TreeModel treemodel = TREE.getModel() ;
		      ((DefaultTreeModel) treemodel).reload() ;
		      DefaultMutableTreeNode node = null ;
		      File [] roots = File.listRoots() ;

            // Add the root file entries.

		      for (int i = 0 ; i < roots.length ; i++)
		      {
		      	node = new DefaultMutableTreeNode
		         	(new IconData(ICON_DISK,null,new FileNode(roots[i]))) ;
		         top.add(node) ;
		         node.add(new DefaultMutableTreeNode(new Boolean(true))) ;
				}

            // Expand to show our new directory.

            expandTree(directory) ;
         }
			return ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { scroll() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; dispose() ; }



   // Define a method to position the tree display to the correct item.
   // This only works if the dialog is visible.

   private void scroll() { TREE.scrollRowToVisible(row+5); }


	// Method to set the dialog field values.

	void setValues()
	{
	   ButtonGroup bg1 = new ButtonGroup() ;
      bg1.add(selectedbutton) ;
      bg1.add(allbutton) ;
	   ButtonGroup bg2 = new ButtonGroup() ;
      bg2.add(overwrite) ;
      bg2.add(skipolder) ;

      // Set the extract type based upon the input context.

      if (items == null || items.size() == 0)
      {
      	selectedbutton.setEnabled(false) ;
         allbutton.setSelected(true) ;
      }
      else
      	selectedbutton.setSelected(true);

      // Establish the initial directory tree.

      IconData idata = new IconData(ICON_COMPUTER,ICON_COMPUTER,"Computer") ;
      top = new DefaultMutableTreeNode(idata) ;
      treemodel = new DefaultTreeModel(top) ;
      TREE = new JTree(treemodel) ;
		TREE.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      TreeCellRenderer renderer = new IconCellRenderer() ;
      TREE.setCellRenderer(renderer);
      TREE.setShowsRootHandles(true);
      TREE.setEditable(false) ;
      jScrollPane1.getViewport().add(TREE, null);

      // Build the top level directory tree.

      DefaultMutableTreeNode node = null ;
      File [] roots = File.listRoots() ;
      for (int i = 0 ; i < roots.length ; i++)
      {
      	node = new DefaultMutableTreeNode
         	(new IconData(ICON_DISK,null,new FileNode(roots[i]))) ;
         top.add(node) ;
         node.add(new DefaultMutableTreeNode(new Boolean(true))) ;
		}

      // Expand the tree to the extract path.

      if (zip != null)
      {
      	directory = zip.getDirectoryName() ;
         extractdirectory.setText(directory) ;
	      expandTree(directory) ;
      }
   }


   // Utility function to return the last path node of the tree.

   private DefaultMutableTreeNode getTreeNode(TreePath path)
   {
      return (DefaultMutableTreeNode) path.getLastPathComponent() ;
   }


   // Utility function to get a file node from a tree node.

   private FileNode getFileNode(DefaultMutableTreeNode node)
   {
      if (node == null) return null ;
      Object o = node.getUserObject() ;
      if (o instanceof IconData)
      	o = ((IconData) o).getObject() ;
      if (o instanceof FileNode)
      	return (FileNode) o ;
      return null ;
   }


   // Utility function to expand the tree to the specified path.

   private void expandTree(String s)
   {
   	DefaultMutableTreeNode node = null ;
      Vector pathnames = new Vector() ;
      File f = new File(s) ;
      row = 0 ;

      // Split the path name up into specific directories.

      while (f != null)
      {
	      pathnames.insertElementAt(f,0) ;
         f = f.getParentFile() ;
      }

      // Expand the path. We walk down the tree progressively expanding
      // the directory paths until we reach the end of the extract path.

		TREE.expandRow(row) ;
      for (int i = 0 ; i < pathnames.size() ; i++)
      {
			f = (File) pathnames.elementAt(i) ;
         s = f.getPath().toUpperCase() ;
         TreePath path = null ;

         // Find the node that matches the current file path.

         for (int j = row+1 ; ; j++)
         {
         	path = TREE.getPathForRow(j) ;
         	if (path == null) break ;
            node = getTreeNode(path) ;
            String s1 = node.toString() ;
            if (s1.indexOf(File.separator) < 0)
            	s1 = File.separator + node.toString() ;
            if (s.endsWith(s1.toUpperCase())) break ;
         }

         // If we found the directory, expand the node.

         if (path == null) break ;
         FileNode filenode = getFileNode(node) ;
         if (filenode == null) break ;
         filenode.expand(node) ;
         TREE.expandPath(path) ;
         row = TREE.getRowForPath(path) ;
         TREE.setSelectionPath(path) ;
      }
   }

   // Establish the dialog close method.

   void close()
   {
      flush() ;
   }

   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
      parent = null ;
      items = null ;
      zip = null ;

      // Flush the dialog contents.

      setVisible(false) ;
      CANCEL.removeActionListener(this);
      EXTRACT.removeActionListener(this);
      NEWDIRECTORY.removeActionListener(this);
      TREE.removeTreeSelectionListener(treeListener) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }


   // Inner class to create a tree cell renderer.

   class IconCellRenderer extends JLabel
   	implements TreeCellRenderer
   {
   	private Color textSelectionColor ;
      private Color textNonSelectionColor ;
      private Color bkSelectionColor ;
      private Color bkNonSelectionColor ;
      private Color borderSelectionColor ;
      private boolean selected = false ;

      // Constructor

      public IconCellRenderer()
      {
      	super() ;
         textSelectionColor = UIManager.getColor("Tree.selectionForeground") ;
         textNonSelectionColor = UIManager.getColor("Tree.textForeground") ;
         bkSelectionColor = UIManager.getColor("Tree.selectionBackground") ;
         bkNonSelectionColor = UIManager.getColor("Tree.textBackground") ;
         borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor") ;
         setOpaque(false) ;
      }

      // TreeCellRenderer interface method.

      public Component getTreeCellRendererComponent
      	(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
          int row, boolean hasFocus)
      {
			DefaultMutableTreeNode node = null ;

      	if (value instanceof DefaultMutableTreeNode)
      		node = (DefaultMutableTreeNode) value ;
         Object o = (node == null) ? null : node.getUserObject() ;
         if (o != null) setText(o.toString()) ;

         // Are we in the midst of retrieving the directory data?

         if (o instanceof Boolean)
         	setText(Kisekae.getCaptions().getString("RetrieveDataText")) ;

         // Set the tree line icon depending on the expansion state.

         if (o instanceof IconData)
         {
         	IconData idata = (IconData) o ;
            if (expanded || sel)
            	setIcon(idata.getExpandedIcon()) ;
            else
            	setIcon(idata.getIcon()) ;
         }
         else
         	setIcon(null) ;

         // Establish tree line font and color.

//       setFont(tree.getFont()) ;
         setForeground(sel ? textSelectionColor : textNonSelectionColor) ;
         setBackground(sel ? bkSelectionColor : bkNonSelectionColor) ;
         selected = sel ;
         return this ;
      }

      // Method to paint the line.

      public void paintComponent(Graphics g)
      {
      	Color background = getBackground() ;
         Icon icon = getIcon() ;

         // Paint the background color.  The text is drawn by the base class.

         g.setColor(background) ;
         int offset = 0 ;
         if (icon != null && getText() != null)
         	offset = (icon.getIconWidth() + getIconTextGap()) ;
         g.fillRect(offset, 0, getWidth()-1-offset, getHeight()-1) ;

         // If the line is selected, paint the selection color.

         if (selected)
         {
         	g.setColor(borderSelectionColor) ;
            g.drawRect(offset, 0, getWidth()-1-offset, getHeight()-1) ;
         }
         super.paintComponent(g) ;
      }
   }


   // Inner class to encapsulate the closed and open icons with a data object.

   class IconData
   {
   	private Icon icon ;
      private Icon expandedicon ;
      private Object data ;

      // Constructor

      public IconData(Icon icon, Object data)
      {
      	this.icon = icon ;
         this.expandedicon = null ;
         this.data = data ;
      }

      public IconData(Icon icon, Icon expandedicon, Object data)
      {
      	this(icon,data) ;
         this.expandedicon = expandedicon ;
      }

      // Methods to return the object attributes.

      public Icon getIcon() { return icon ; }
      public Icon getExpandedIcon() { return expandedicon ; }
      public Object getObject() { return data ; }

      // Overload the object toString method to return our representation.

      public String toString() { return data.toString() ; }
   }


   // Inner class to define a tree file node.

   class FileNode
   {
   	private File file ;

      public FileNode(File f) { file = f ; }

      public File getFile() { return file ; }

      public String toString()
      { return (file.getName().length() > 0) ? file.getName() : file.getPath() ; }

      // This method expands a tree node by adding new nodes corresponding to
      // the subdirectory of the parent node.

      public boolean expand(DefaultMutableTreeNode parent)
      {
         DefaultMutableTreeNode flag = null ;
         try { flag = (DefaultMutableTreeNode) parent.getFirstChild() ; }
         catch (NoSuchElementException e) { flag = null ; }
         if (flag == null) return false ;
         Object o = flag.getUserObject() ;
         if (!(o instanceof Boolean)) return false ;
         parent.removeAllChildren() ;

         // Get list of files in the directory.

         File [] files = listFiles() ;
         if (files == null) return true ;

         // Create a new file node for each directory in the expansion.

         Vector v = new Vector() ;
         for (int i = 0 ; i < files.length ; i++)
         {
            File f = files[i] ;
            if (!(f.isDirectory())) continue ;
            FileNode newnode = new FileNode(f) ;

            // Add this file node to our list.  An insertion sort is done.
            // This keeps the directory names in sorted order.

            boolean added = false ;
            for (int j = 0 ; j < v.size() ; j++)
            {
               FileNode nd = (FileNode) v.elementAt(j) ;
               if (newnode.compareTo(nd) < 0)
               {
                  v.insertElementAt(newnode,j) ;
                  added = true ;
                  break ;
               }
            }
            if (!added) v.addElement(newnode) ;
         }

         // Add the nodes to the tree.  If there are subdirectories for this
         // node add a dummy boolean variable to signal that the node can be
         // expanded.

         for (int i = 0 ; i < v.size() ; i++)
         {
            FileNode nd = (FileNode) v.elementAt(i) ;
            IconData idata = new IconData(ICON_FOLDER,ICON_EXPANDEDFOLDER,nd) ;
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata) ;
            parent.add(node) ;
            if (nd.hasSubDirs())
               node.add(new DefaultMutableTreeNode(new Boolean(true)));
         }
         return true ;
      }


      // Utility function to determine if our current file node has
      // subdirectories.

      private boolean hasSubDirs()
      {
         File [] files = listFiles() ;
         if (files == null) return false ;
         for (int i = 0 ; i < files.length ; i++)
            if (files[i].isDirectory()) return true ;
         return false ;
      }


      // Utility function to compare file node names lexographically.

      private int compareTo(FileNode f)
      {
         return file.getName().compareToIgnoreCase(f.getFile().getName()) ;
      }


      // Utility function to return a list of files in the directory.

      private File [] listFiles()
      {
         if (file == null) return null ;
         try { return file.listFiles() ; }
         catch (Exception e)
         {
            JOptionPane.showMessageDialog(me,
               file.getAbsolutePath() + "\n" +
               Kisekae.getCaptions().getString("FileReadError"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
               JOptionPane.WARNING_MESSAGE) ;
            return null ;
         }
      }
   }


   // Inner class to define a tree expansion listener.  We must ensure that
   // the expansion is threaded and that the tree model is updated within
   // the event dispatching thread.

   class ExpansionListener implements TreeExpansionListener
   {
      public void treeExpanded(TreeExpansionEvent event)
      {
         final DefaultMutableTreeNode node = getTreeNode(event.getPath()) ;
         final FileNode fnode = getFileNode(node) ;

         Thread runner = new Thread()
         {
            public void run()
            {
               if (fnode != null && fnode.expand(node))
               {
                  Runnable runnable = new Runnable()
                  {
                     public void run() { treemodel.reload(node) ; }
                  } ;
                  SwingUtilities.invokeLater(runnable) ;
               }
            }
         } ;
         runner.start() ;
      }

      public void treeCollapsed(TreeExpansionEvent event) { }
   }
}
