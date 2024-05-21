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
import java.util.zip.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.io.* ;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.* ;
import javax.swing.table.* ;
import javax.swing.plaf.basic.BasicFileChooserUI ;
import javax.help.* ;


final class ZipAdd extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;						// Reference to ourselves
   private ArchiveFile zip = null ;					// Reference to archive file
   private Object [] items = null ;					// Reference to selection items
   private String directory = null ;				// Extract directory

   // Help set objects

	private static String helpset = "Help/ArchiveManager.hs" ;
	private HelpSet hs = null ;
	private HelpBroker hb = null ;

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
	private JFileChooser chooser = new JFileChooser();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JComboBox action = new JComboBox();
	private JComboBox compression = new JComboBox();
	private JCheckBox recursefolders = new JCheckBox();
	private JCheckBox savefolderinfo = new JCheckBox();
	private JCheckBox savepathinfo = new JCheckBox();
	private GridLayout gridLayout1 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JButton HELP = new JButton();



	// Constructor

	public ZipAdd(JFrame f, ArchiveFile zip)
	{
   	super(f,Kisekae.getCaptions().getString("ArchiveManagerAddTitle"),true) ;
      me = this ;
      init(zip) ;
   }

   private void init(ArchiveFile zip)
   {
      this.zip = zip ;
      String s1 = Kisekae.getCaptions().getString("ArchiveManagerAddToTitle") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1 && zip != null)
         s1 = s1.substring(0,i1) + zip.getName() + s1.substring(j1+1) ;
      if (zip != null) setTitle(s1) ;

      // Construct the user interface.

		try { jbInit(); pack(); }
      catch(Exception ex)
      {
         System.out.println("ZipAdd: jbInit constructor " + ex.toString()) ;
         ex.printStackTrace();
         JOptionPane.showMessageDialog(null,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + ex.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         return ;
      }

      // Set the initial display values.

	   ButtonGroup bg = new ButtonGroup() ;
      bg.add(savefolderinfo) ;
      bg.add(savepathinfo) ;
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

		addWindowListener(this);
		if (hb != null) HELP.addActionListener(new CSH.DisplayHelpFromSource(hb)) ;
      attachlistener(chooser,this) ;
      chooser.addActionListener(this) ;
   }


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
      String s = Kisekae.getCaptions().getString("FolderBoxTitle") ;
      Border cb2 = new CompoundBorder(new TitledBorder(s),eb2) ;
		jLabel1.setText(Kisekae.getCaptions().getString("AddActionText"));
		jPanel1.setLayout(gridBagLayout1);
		jLabel2.setText(Kisekae.getCaptions().getString("AddCompressionText"));
		jPanel2.setBorder(cb2);
		jPanel2.setLayout(gridLayout1);
		recursefolders.setToolTipText(Kisekae.getCaptions().getString("ToolTipRecurse"));
		recursefolders.setText(Kisekae.getCaptions().getString("RecurseFoldersText"));
		savefolderinfo.setToolTipText(Kisekae.getCaptions().getString("ToolTipAbsolute"));
		savefolderinfo.setText(Kisekae.getCaptions().getString("AbsolutePathText"));
		savepathinfo.setToolTipText(Kisekae.getCaptions().getString("ToolTipRelative"));
		savepathinfo.setText(Kisekae.getCaptions().getString("RelativePathText"));
		gridLayout1.setColumns(1);
		gridLayout1.setRows(3);
		chooser.setPreferredSize(new Dimension(500, 300));
		chooser.setMinimumSize(new Dimension(400, 300));
		chooser.setMultiSelectionEnabled(true);
      chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		action.setMaximumRowCount(3);
		compression.setMaximumRowCount(3);
		HELP.setMinimumSize(new Dimension(73, 27));
		HELP.setPreferredSize(new Dimension(73, 27));
		HELP.setToolTipText(Kisekae.getCaptions().getString("ToolTipHelp"));
		HELP.setMnemonic('H');
		HELP.setText(Kisekae.getCaptions().getString("MenuHelp"));

		getContentPane().add(panel1);
		panel1.add(chooser, BorderLayout.CENTER);
		panel1.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(compression, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 10, 0), 0, 0));
		jPanel1.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 0), 0, 0));
		jPanel1.add(action, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 5, 0), 0, 0));
		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanel2, new GridBagConstraints(1, 1, 1, 3, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 104, 21, 15), 0, 0));
		jPanel2.add(recursefolders, null);
		jPanel2.add(savefolderinfo, null);
		jPanel2.add(savepathinfo, null);
		jPanel1.add(HELP, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 5, 10), 0, 0));
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

		// A CANCEL closes this dialog.

		if (command.equals(JFileChooser.CANCEL_SELECTION))
		{
        	close() ;
         dispose() ;
			return ;
		}

		// An ADD invokes the file writer to add the selected elements.

		if (command.equals(Kisekae.getCaptions().getString("AddMessage")))
		{
         if (((ZipManager) parent).isBusy()) return ;
			Object [] files = getSelectedFiles(chooser) ;
			boolean selected = (!(files == null || files.length == 0)) ;
			if (!selected) files = getVisibleFiles(chooser) ;
			if (files == null || files.length == 0) return ;

			// Did we select files or directories?  If we selected either
			// then we should not apply the file name filter.

			if (selected)
			{
         	selected = false ;
				for (int i = 0 ; i < files.length ; i++)
				{
					File f = (File) files[i] ;
					if (f.isFile()) { selected = true ; break ; }
					if (f.isDirectory()) { selected = true ; break ; }
				}
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

         // Convert the selected files to ArchiveEntries.  Set the
         // save directory flag to retain directory path information.

			contents.addAll(zip.getContents()) ;
			for (int i = 0 ; i < contents.size() ; i++)
				((ArchiveEntry) contents.elementAt(i)).setSaveDir(true) ;
			if (!addFiles(contents,files,selected,chooser))
			{
				JOptionPane.showMessageDialog(this,
					Kisekae.getCaptions().getString("NoFilesSelectedText"),
               Kisekae.getCaptions().getString("OptionsDialogWarningTitle"),
					JOptionPane.INFORMATION_MESSAGE) ;
				return ;
			}

         // Perform the file addition.

			FileWriter fw = new FileWriter(getParentFrame(),zip,contents,true) ;
	      if (parent instanceof ActionListener)
				fw.callback.addActionListener((ActionListener) parent);
			Thread thread = new Thread(fw) ;
         if (parent instanceof ZipManager)
         {
            ((ZipManager) parent).setBusy() ;
            ((ZipManager) parent).setFileWriter(fw) ;
         }
        	close() ;
         dispose() ;
         thread.start() ;
			return ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt) { }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; dispose() ; }


	// Method to set the dialog field values.

	void setValues()
	{
   	File f = new File(zip.getDirectoryName()) ;
      String s1 = Kisekae.getCaptions().getString("ArchiveManagerAddContentTitle") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + zip.getName() + s1.substring(j1+1) ;
      chooser.setSelectedFile(new File("*.*")) ;
		chooser.setCurrentDirectory(f) ;
		chooser.setDialogTitle(s1) ;
      chooser.setMultiSelectionEnabled(true) ;
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY) ;
      chooser.setApproveButtonText(Kisekae.getCaptions().getString("AddMessage")) ;
      chooser.setApproveButtonToolTipText(Kisekae.getCaptions().getString("ToolTipAdd")) ;
      chooser.setApproveButtonMnemonic('A') ;

      // Create filters.

		String [] allfilter = { "" } ;
		AddFilter filter0 = new AddFilter("All Files (*.*)", allfilter) ;
		String [] textfilter = { ".txt",".doc",".rtf",".log",".cnf",".lst" } ;
		AddFilter filter1 = new AddFilter("Text Files (*.txt,*.doc,*.rtf,*.log,*.cnf,*.lst)", textfilter) ;
		String [] imagefilter = { ".cel",".gif",".jpg",".bmp",".ppm",".pgm",".pbm",".png"} ;
      AddFilter filter2 = new AddFilter("Image Files (*.cel,*.gif,*.jpg,*.bmp,*.ppm,*.png)", imagefilter) ;
      String [] palettefilter = { ".kcf",".pal" } ;
		AddFilter filter3 = new AddFilter("Palette Files (*.kcf,*.pal)", palettefilter) ;
		chooser.addChoosableFileFilter(filter0) ;
		chooser.addChoosableFileFilter(filter1) ;
		chooser.addChoosableFileFilter(filter2) ;
		chooser.addChoosableFileFilter(filter3) ;
		chooser.setFileFilter(filter0) ;
		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter()) ;

		// Create options.

      action.addItem(Kisekae.getCaptions().getString("AddReplaceText"));
      action.addItem(Kisekae.getCaptions().getString("AddRefreshText"));
      compression.addItem(Kisekae.getCaptions().getString("NormalCompressionText"));
      compression.addItem(Kisekae.getCaptions().getString("NoCompressionText"));
   }


   // Establish the dialog close method.

	void close() { flush() ; }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
		setVisible(false) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }


   // A utility function to search for abstract buttons in a container
   // and attach a listener to them.  This function is used for the
   // JFileChooser component to attach a listener to the accept and
   // cancel buttons.

   private void attachlistener(Container c, ActionListener l)
   {
   	Component [] comps = c.getComponents() ;
	   if (comps == null) return ;
     	for (int i = 0 ; i < comps.length ; i++)
      {
        	Component comp = comps[i] ;
         if (comp instanceof JPanel)
         	attachlistener((Container) comp, l) ;
         if (comp instanceof AbstractButton)
           	((AbstractButton) comp).addActionListener(l) ;
      }
   }


   // A utility function to determine the files selected in a
	// JFileChooser object.

	private Object [] getSelectedFiles(JFileChooser chooser)
   {
  		Object list = getJList(chooser) ;
  		if (list == null) return null ;
  		if (list instanceof JList) return ((JList) list).getSelectedValues() ;
      if (!(list instanceof JTable)) return null ;
      JTable table = (JTable) list ;
      int [] rows = table.getSelectedRows() ;
      if (rows.length == 0) return null ;
      TableModel model = table.getModel() ;
      Object [] values = new Object[rows.length] ;
      for (int i = 0 ; i < rows.length ; i++)
         values[i] = model.getValueAt(rows[i],0) ;
      return values ;
   }


	// A utility function to determine the files visible in a
	// JFileChooser object.

	private Object [] getVisibleFiles(JFileChooser chooser)
   {
  		Vector v = new Vector() ;
		Object list = getJList(chooser) ;
      if (list == null) return null ;
      if (list instanceof JList)
      {
   		ListModel model = ((JList) list).getModel() ;
   		if (model == null) return null ;
   		for (int i = 0 ; i < model.getSize() ; i++)
   			v.addElement(model.getElementAt(i));
      }
      if (list instanceof JTable)
      {
   		TableModel model = ((JTable) list).getModel() ;
   		if (model == null) return null ;
         int rows = model.getRowCount() ;
         int cols = model.getColumnCount() ;
   		for (int j = 0 ; j < cols ; j++)
            for (int i = 0 ; i < rows ; i++)
   			   v.addElement(model.getValueAt(i,j));
      }
  		return v.toArray() ;
   }


	// A utility function to find the JList or JTable item in a JFileChooser.

	private Object getJList(Container chooser)
	{
		Object list = null ;
      Component [] components = chooser.getComponents() ;
      if (components == null) return null ;
      for (int i = 0 ; i < components.length ; i++)
      {
         if (!(components[i] instanceof Container)) continue ;
			Container c = (Container) components[i] ;
			if (c instanceof JList)
			{
				list = c ;
				break ;
			}
			if (c instanceof JTable)
			{
				list = c ;
				break ;
			}
        	list = getJList(c) ;
         if (list != null) break ;
		}
		return list ;
	}


	// A utility function to find the JTextField item in a JFileChooser object.
	// The JFileChooser getSelectedFile method does not work properly.

	private File getFile(JFileChooser chooser)
	{
      try
      {  // Java 1.3 case
   		JTextField text = null ;
   		Container c1 = (Container) chooser.getComponent(5) ;
         if (c1 == null) return null ;
   		Container c2 = (Container) c1.getComponent(3) ;
   		while (c2 != null)
         {
   			Container c = (Container) c2.getComponent(0) ;
   			if (c instanceof JTextField)
   			{
   				text = (JTextField) c ;
   				break ;
   			}
   			c2 = c ;
   		}
         if (text != null)
         {
            String s = text.getText() ;
      		return (s == null) ? null : new File(s) ;
         }
      }
      catch (Exception e) { }
      return chooser.getSelectedFile() ;
	}


   // A utility function to add new ArchiveEntries for all File objects in
   // our file selection to our contents vector.  This function is recursive
   // for File directories.  If a duplicate entry by pathname already exists
	// in the contents vector then it is replaced with the new entry.  The
	// function returns true if an archive entry is added to our contents
	// vector.  The file name wildcard filter will not be used if files were
	// specifically selected.

	private boolean addFiles(Vector contents, Object [] files,
		boolean selected, JFileChooser chooser)
	{
		boolean added = false ;
		if (files == null) return false ;

      // Establish the wildcard filter.

		File f = (!selected) ? getFile(chooser) : null ;
		String fname = (f != null) ? f.getName() : "*.*" ;
		if (fname == null) fname = "*.*" ;
		StringTokenizer st = new StringTokenizer("?" + fname.toUpperCase(),".") ;
		String name = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
		name = name.substring(1) ;
		String ext = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;

      // Determine the wildcard position for name and extension.

		st = new StringTokenizer("?" + name,"*") ;
		String nameprefix = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
		nameprefix = nameprefix.substring(1) ;
		String namesuffix = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
		st = new StringTokenizer("?" + ext,"*") ;
		String extprefix = (st.hasMoreElements()) ? (String) st.nextElement() : "?" ;
		extprefix = extprefix.substring(1) ;
		String extsuffix = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;

      // Filter the files.

      for (int i = 0 ; i < files.length ; i++)
      {
        	if (!(files[i] instanceof File)) continue ;
        	f = (File) files[i] ;

         // For a file we must construct a new archive entry.

         if (f.isFile())
         {
	         fname = f.getName() ;
		      st = new StringTokenizer(fname.toUpperCase(),".") ;
		      name = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
				ext = (st.hasMoreElements()) ? (String) st.nextElement() : "" ;
	         if (!(name.startsWith(nameprefix))) continue ;
	         if (!(name.endsWith(namesuffix))) continue ;
	         if (!(ext.startsWith(extprefix))) continue ;
	         if (!(ext.endsWith(extsuffix))) continue ;

            // File is valid.  Construct an archive entry.

				ArchiveEntry ae = new DirEntry(f.getParent(),f.getName(),null) ;
            ae.setSaveDir(savefolderinfo.isSelected()) ;
            ae.setSaveRelDir(savepathinfo.isSelected()) ;

            // Set the required compression method.

            int method = 0 ;
            switch (compression.getSelectedIndex())
            {
           	case 0:     	// Compress
					if (zip instanceof PkzFile) method = ZipEntry.DEFLATED ;
               if (zip instanceof LhaFile) method = LhaEntry.LH5 ;
               if (zip instanceof DirFile) method = DirEntry.COMPRESSED ;
               break ;

            case 1:			// No compress
               if (zip instanceof PkzFile) method = ZipEntry.STORED ;
               if (zip instanceof LhaFile) method = LhaEntry.LH0 ;
               if (zip instanceof DirFile) method = DirEntry.UNCOMPRESSED ;
               break ;

            default: 		// Default to compress
               if (zip instanceof PkzFile) method = ZipEntry.DEFLATED ;
               if (zip instanceof LhaFile) method = LhaEntry.LH5 ;
               if (zip instanceof DirFile) method = DirEntry.COMPRESSED ;
               break ;
            }

            // Set the required date and time check.

            switch (action.getSelectedIndex())
            {
           	case 0:     	// Add and Replace
               ae.setCheckDate(false); ;
               break ;

            case 1:			// Add and Refresh
               ae.setCheckDate(true); ;
               break ;

            default: 		// Default to Add and Replace
               ae.setCheckDate(false); ;
               break ;
            }

            ae.setMethod(method) ;
            ae = removeDuplicate(contents,ae) ;
				if (ae != null) { contents.add(ae) ;  added = true ; }
         }

         // For a directory we will recurse if necessary.   The current
         // filechooser filter will apply, as will any wildcards.

         if (f.isDirectory() && recursefolders.isSelected())
         {
				FileFilter filter = (FileFilter) chooser.getFileFilter() ;
				File [] list = f.listFiles(filter) ;
				added |= addFiles(contents,list,selected,chooser) ;
         }
		}

		// Return true if we selected and added a file.

		return added ;
   }


   // A utility function to search for a duplicate entry in the contents
   // vector and remove it if found.

   private ArchiveEntry removeDuplicate(Vector contents, ArchiveEntry ae)
   {
   	String entrypath = ae.getPath() ;
      String entryname = ae.getName() ;
      if (entryname == null) return null ;

      // Check all contents entries for a duplicate.   If we are checking
      // dates and our archive entry is older than the contents entry then
      // this routine will not delete the duplicate entry and it will return
      // null.

      for (int i = contents.size() ; i > 0 ; i--)
      {
      	ArchiveEntry a = (ArchiveEntry) contents.elementAt(i-1) ;
         String path = a.getPath() ;
         String name = a.getName() ;
         if (name == null) continue ;

         // If retaining directory information, remove all contents
         // entries of the same name that do not have directory information,
         // or duplicate entries that have the same full path.

	      if (ae.getSaveDir())
         {
         	if (name.equalsIgnoreCase(path))
            {
            	if (entryname.equalsIgnoreCase(name))
               {
		         	if (!ae.getCheckDate() || !checkdate(ae,a))
                  	contents.removeElementAt(i-1) ;
                  else return null ;
               }
            }
           	else if (entrypath.equalsIgnoreCase(path))
            {
	         	if (!ae.getCheckDate() || !checkdate(ae,a))
               	contents.removeElementAt(i-1) ;
               else return null ;
            }
         }

         // If not retaining directory information, remove all contents
         // entries of the same name regardless of their path.

         else
         {
         	if (entryname.equalsIgnoreCase(name))
            {
	         	if (!ae.getCheckDate() || !checkdate(ae,a))
               	contents.removeElementAt(i-1) ;
               else return null ;
            }
         }
      }

      // Return the archive entry for insertion into the contents vector.

      return ae ;
   }


   // Function to check the creation time and date of two archive elements.
   // This function returns true if A is older than B.

   private boolean checkdate(ArchiveEntry a, ArchiveEntry b)
   {
   	long ta = a.getTime() ;
      long tb = b.getTime() ;
      return (ta < tb) ;
   }


   // Inner class to create a file filter for various file types.

   class AddFilter extends javax.swing.filechooser.FileFilter
   	implements FileFilter
   {
   	private String description = null ;
      private String [] extension = null ;

      AddFilter(String desc, String [] ext)
      {
      	description = desc ;
         extension = ext ;
      }

      public String getDescription() { return description ; }

      public boolean accept(File f)
      {
      	if (f == null) return false ;
         if (f.isDirectory()) return true ;
         if (extension == null) return false ;

         String name = f.getName().toLowerCase() ;
         for (int i = 0 ; i < extension.length ; i++)
         	if (name.endsWith(extension[i])) return true ;
         return false ;
      }
   }
}
