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



import java.io.* ;
import java.awt.*;
import java.awt.event.* ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.tree.* ;

final class PaletteDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;						 	// Reference to ourselves
	private Cel cel = null ;  			      		 	// The cel object
	private Palette palette = null ;		  			 	// The palette to show
	private KissObject kiss = null ;						// The palette object
	private Configuration config = null ;  		 	// The current config id
	private Integer multipalette = null ;			 	// Current multipalette

   // User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new JPanel();
	private JPanel jPanel8 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private JButton VIEW = new JButton();
	private JButton LISTBTN = new JButton();
	private JButton EDIT = new JButton();
	private GridLayout gridLayout1 = new GridLayout();
	private GridLayout gridLayout2 = new GridLayout();
	private GridLayout gridLayout3 = new GridLayout();
	private GridLayout gridLayout4 = new GridLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JList LIST = null ;
 	private DefaultListModel listmodel = new DefaultListModel() ;
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
   private Border eb2 = BorderFactory.createEmptyBorder(0,5,0,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
   private Border eb5 = BorderFactory.createEmptyBorder(2,2,2,2) ;
	private JLabel heading = new JLabel();
	private JLabel groupslabel = new JLabel();
	private JLabel colorslabel = new JLabel();
	private JLabel bitslabel = new JLabel();
	private JLabel celslabel = new JLabel();
	private JLabel multilabel = new JLabel();
	private JLabel namelabel = new JLabel();
	private JLabel backgroundlabel = new JLabel();
	private JLabel transparentlabel = new JLabel();
	private JCheckBox visiblelabel = new JCheckBox();
	private JCheckBox loadedlabel = new JCheckBox();
	private JCheckBox copylabel = new JCheckBox();
	private JComboBox multiselect = new JComboBox();

   // Define a background color for disabled checkboxes that does not
   // grey the text.

   private Color nonFading = new Color (copylabel.getBackground().getRGB())
   {
      public Color darker() { return Color.black ; }
      public Color brighter() { return copylabel.getBackground() ; }
   };

	// Register for events.

	MouseListener mouseListListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
        	if (e.getClickCount() == 2)
         {
         	if (config == null) return ;
           	String s = (String) LIST.getSelectedValue() ;
            if (s == null || s.length() < 22) return ;
            s = s.substring(6,21).trim().toUpperCase() ;
				cel = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),s) ;
				if (cel == null) return ;
 				CelDialog cd = new CelDialog(me,cel,null,config) ;
				cd.show() ;
			}
		}
	} ;

   ItemListener multiListener = new ItemListener()
   {
     	public void itemStateChanged(ItemEvent e)
      {
        	Integer n = (Integer) multiselect.getSelectedItem() ;
         if (n == null) return ;
         multipalette = n ;
         setValues() ;
      }
   } ;

	ListSelectionListener listListener = new ListSelectionListener()
   {
		public void valueChanged(ListSelectionEvent e)
      { VIEW.setEnabled(true); }
	} ;


	// Constructor

	public PaletteDialog(JDialog f, KissObject o, Integer mp, Configuration c)
	{ super(f,null,false) ; init(o,mp,c) ; }

	public PaletteDialog(JFrame f, KissObject o, Integer mp, Configuration c)
	{ super(f,null,false) ; init(o,mp,c) ; }

	private void init(KissObject o, Integer mp, Configuration c)
   {
		me = this ;
		kiss = o ;
		config = c ;
		multipalette = mp ;

		// Different object types can have palettes.  Determine the palette
		// based upon the entry object.

		if (o instanceof Palette)
			palette = (Palette) o ;
		if (o instanceof Cel)
		{
			cel = (Cel) o ;
			palette = cel.getPalette() ;
		}

		// Set up the dialog title.

		Object id = palette.getIdentifier() ;
      String idname = (id == null) ? "" : id.toString() ;
      String title = Kisekae.getCaptions().getString("PaletteDialogTitle") ;
      if (palette.getIdentifier() != null) title += " " + palette.getIdentifier() ;
      if (palette.getName() != null) title += " " + palette.getName() ;
      if (palette.isInternal()) title += " [" + Kisekae.getCaptions().getString("InternalStateText") + "]" ;
      if (palette.isUpdated()) title += " [" + Kisekae.getCaptions().getString("UpdatedStateText") + "]" ;
		setTitle(title) ;

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
      setValues() ;

		// Center the frame in the panel space.

      Dimension d1 = getParentSize() ;
      Dimension d2 = getSize() ;
      if (d1.height > d2.height) d2.height = d1.height ;
      if (d1.width > d2.width) d2.width = d1.width ;
      setSize(d2) ;
 		center(this) ;

		// Set the initial values.  Populate the multipalette popdown selection
      // box with the possible options.

		for (int i = 0 ; i < palette.getMultiPaletteCount() ; i++)
        	multiselect.addItem(new Integer(i));
      if (multipalette != null)
      	multiselect.setSelectedItem(multipalette) ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		VIEW.addActionListener(this) ;
		LISTBTN.addActionListener(this) ;
		EDIT.addActionListener(this) ;
		LIST.addMouseListener(mouseListListener) ;
      LIST.addListSelectionListener(listListener) ;
      multiselect.addItemListener(multiListener) ;
      visiblelabel.addActionListener(this) ;
      loadedlabel.addActionListener(this) ;
      copylabel.addActionListener(this) ;
		addWindowListener(this);
	}


   // User interface initialization.

	void jbInit() throws Exception
	{
		panel1.setLayout(borderLayout1);
 		panel1.setPreferredSize(new Dimension(620, 410));
      OK.setText(Kisekae.getCaptions().getString("OkMessage"));
      OK.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyOKButton"));
      CANCEL.setText(Kisekae.getCaptions().getString("ReturnMessage"));
      CANCEL.setToolTipText(Kisekae.getCaptions().getString("ToolTipPropertyCancelButton"));
      CANCEL.setEnabled(parent instanceof KissDialog);
		VIEW.setEnabled(false);
      VIEW.setToolTipText(Kisekae.getCaptions().getString("ToolTipViewImageButton"));
      VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage"));
		LISTBTN.setText(Kisekae.getCaptions().getString("ListImagesMessage"));
      LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListImagesButton"));
		EDIT.setText(Kisekae.getCaptions().getString("EditPaletteMessage"));
      EDIT.setToolTipText(Kisekae.getCaptions().getString("ToolTipEditPaletteButton"));
      EDIT.setEnabled(!(getParentFrame() instanceof ColorFrame));
		LIST = new JList (listmodel);
		LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
      Border cb2 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("PaletteBoxText")),eb2) ;
      Border cb3 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("AttributesBoxText")),eb2) ;
      Border cb4 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("StateBoxText")),eb2) ;
      Border cb5 = new CompoundBorder(new TitledBorder(Kisekae.getCaptions().getString("PaletteContextBoxText")),eb2) ;
      Border cb6 = new CompoundBorder(BorderFactory.createRaisedBevelBorder(),eb5) ;
		gridLayout1.setColumns(16);
		gridLayout1.setHgap(1);
		gridLayout1.setRows(16);
		gridLayout1.setVgap(1);
		gridLayout2.setColumns(1);
		gridLayout2.setRows(4);
		gridLayout3.setColumns(1);
		gridLayout3.setRows(3);
		gridLayout4.setColumns(1);
		gridLayout4.setRows(4);

 		jPanel1.setBorder(eb1);
 		jPanel1.setLayout(gridBagLayout1);
		jPanel2.setBorder(eb1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3);
		jPanel3.setBorder(eb1);
		jPanel4.setBorder(cb2);
		jPanel4.setMinimumSize(new Dimension(132, 100));
		jPanel4.setPreferredSize(new Dimension(132, 100));
		jPanel4.setLayout(gridLayout2);
		jPanel5.setBorder(cb3);
		jPanel5.setMinimumSize(new Dimension(132, 100));
		jPanel5.setPreferredSize(new Dimension(132, 100));
		jPanel5.setLayout(gridLayout3);
		jPanel6.setBorder(cb4);
		jPanel6.setMinimumSize(new Dimension(132, 100));
		jPanel6.setPreferredSize(new Dimension(132, 100));
		jPanel6.setLayout(gridLayout4);
		jPanel7.setBorder(BorderFactory.createRaisedBevelBorder());
		jPanel7.setMinimumSize(new Dimension(100, 100));
		jPanel7.setPreferredSize(new Dimension(100, 100));
		jPanel7.setLayout(gridLayout1);
		jPanel8.setBorder(cb5);
		jPanel8.setLayout(gridBagLayout2);

      heading.setText(" ");
      heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		groupslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteNumber"));
		groupslabel.setText(Kisekae.getCaptions().getString("PaletteGroupsText"));
      copylabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipCopyState"));
      copylabel.setText(Kisekae.getCaptions().getString("CopyStateText"));
      copylabel.setEnabled(false);
      copylabel.setBackground(nonFading);
      loadedlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipLoadedState"));
      loadedlabel.setText(Kisekae.getCaptions().getString("LoadedStateText"));
      loadedlabel.setEnabled(false);
      loadedlabel.setBackground(nonFading);
      visiblelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipVisibleState"));
      visiblelabel.setText(Kisekae.getCaptions().getString("VisibleStateText"));
      visiblelabel.setBackground(nonFading);
		colorslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteColors"));
      colorslabel.setText(Kisekae.getCaptions().getString("PaletteColorsText"));
		bitslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteBits"));
		bitslabel.setText(Kisekae.getCaptions().getString("PaletteBitsText"));
      celslabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipImageCount"));
      celslabel.setText(Kisekae.getCaptions().getString("ImageCountText"));
      namelabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipName"));
      namelabel.setText(Kisekae.getCaptions().getString("NameText"));
 		backgroundlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteBackground"));
		backgroundlabel.setText(Kisekae.getCaptions().getString("PaletteBackgroundText"));
 		transparentlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteTransparent"));
		transparentlabel.setText(Kisekae.getCaptions().getString("PaletteTransparentText"));
		multilabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipPaletteGroup"));
		multilabel.setText(Kisekae.getCaptions().getString("PaletteGroupText"));
		multiselect.setMinimumSize(new Dimension(50, 24));
		multiselect.setPreferredSize(new Dimension(50, 24));
		multiselect.setMaximumRowCount(OptionsDialog.getMaxColorSet());

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel4.add(bitslabel, null);
		jPanel4.add(colorslabel, null);
		jPanel4.add(backgroundlabel, null);
		jPanel4.add(transparentlabel, null);
		jPanel1.add(jPanel5, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel5.add(celslabel, null);
		jPanel5.add(groupslabel, null);
		jPanel5.add(namelabel, null);
		jPanel1.add(jPanel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 0, 0), 0, 0));
		jPanel6.add(visiblelabel, null);
		jPanel6.add(loadedlabel, null);
		jPanel6.add(copylabel, null);
		jPanel1.add(jPanel7, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 0, 0));
		jPanel1.add(jPanel8, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jPanel8.add(multilabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		jPanel8.add(multiselect, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 8, 0), 0, 0));
		jPanel8.add(EDIT, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 8, 0), 0, 0));
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
		jPanel2.add(LISTBTN, null);
      jPanel2.add(Box.createGlue()) ;
 		jPanel2.add(VIEW, null);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(heading, BorderLayout.NORTH);
		jPanel3.add(jScrollPane1, BorderLayout.CENTER);
	}


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;
      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe == null) { close() ; return ; }

      // The user cannot change state variable values.

      try
      {
	      if (source instanceof JCheckBox)
	      {
				JCheckBox cb = (JCheckBox) source ;
            if (cb == visiblelabel)
            {
               int mp = (multipalette != null) ? multipalette.intValue() : 0 ;
               palette.setVisible(cb.isSelected(),mp) ;
               mainframe.updateMenu() ;
            }
            else
               cb.setSelected(!cb.isSelected()) ;
	         return ;
	      }

			// An OK closes the frame.

			if (source == OK)
			{
	      	close() ;
				return ;
			}

			// A CANCEL closes only this dialog makes the parent visible.

			if (source == CANCEL)
			{
	         if (parent instanceof KissDialog)
	         {
            	((KissDialog) parent).setValues() ;
               callback.doClick();
			      flush() ;
					dispose() ;
					parent = null ;
	         	getOwner().setVisible(true) ;
	         }
	         else
	         	close() ;
				return ;
			}

			// A List Images updates the list table to display the group cels.

         if (command.equals(Kisekae.getCaptions().getString("ListImagesMessage")))
			{
	         VIEW.setEnabled(false) ;
	         VIEW.setVisible(true) ;
	         LISTBTN.setText(Kisekae.getCaptions().getString("ListTextMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListTextButton"));
	         setValues() ;
				return ;
			}

			// A List Palette updates the list table to display the group cels.

         if (command.equals(Kisekae.getCaptions().getString("ListTextMessage")))
			{
	         VIEW.setVisible(false) ;
	         VIEW.setEnabled(false) ;
	         LISTBTN.setText(Kisekae.getCaptions().getString("ListImagesMessage")) ;
            LISTBTN.setToolTipText(Kisekae.getCaptions().getString("ToolTipListImagesButton"));
	         setValues() ;
				return ;
			}

			// A View Image brings up a Cel dialog.

         if (command.equals(Kisekae.getCaptions().getString("ViewImageMessage")))
			{
         	if (config == null) return ;
	        	String s = (String) LIST.getSelectedValue() ;
	         if (s == null || s.length() < 22) return ;
	         s = s.substring(6,21).trim() ;
	         String directory = palette.getDirectory() ;
	         File f = new File(directory,s) ;
	         s = f.getPath().toUpperCase() ;
				Cel c = (Cel) Cel.getByKey(Cel.getKeyTable(),config.getID(),s) ;
				if (c == null) return ;
				CelDialog cd = new CelDialog(me,c,null,config) ;
				cd.show() ;
				return ;
			}

	      // An Edit request invokes the Color Editor.  We set an edit
	      // callback to our PanelMenu as this is where we apply color
	      // changes to the active panel frame.

         if (command.equals(Kisekae.getCaptions().getString("EditPaletteMessage")))
	      {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				ColorFrame cf = new ColorFrame(config,kiss,multipalette) ;
	         cf.callback.addActionListener(this);
	         MainFrame mf = Kisekae.getMainFrame() ;
	         KissMenu menu = mf.getMenu() ;
	         if (menu instanceof ActionListener)
	           	cf.callback.addActionListener((ActionListener) menu);
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
				cf.show() ;
			}

			// An update request from the palette edit window has occured.
			// Our color preview panel will be updated.

			if ("ColorFrame Callback".equals(evt.getActionCommand()))
			{
	      	if (palette == null) return ;
		      if (jPanel7.getComponentCount() > 0) jPanel7.removeAll() ;
		      int mp = (multipalette != null) ? multipalette.intValue() : 0 ;
		      for (int i = 0 ; i < Math.min(256,palette.getColorCount()) ; i++)
		      {
		      	JPanel p = new JPanel() ;
		         p.setBackground(palette.getColor(mp,i)) ;
		         jPanel7.add(p) ;
		      }
		      jPanel7.revalidate();

				// Populate the multipalette popdown selection box with the
            // possible options.

	         multiselect.removeAllItems() ;
				for (int i = 0 ; i < palette.getMultiPaletteCount() ; i++)
		        	multiselect.addItem(new Integer(i));
		      if (multipalette != null)
		      	multiselect.setSelectedItem(multipalette) ;
	      }
      }

		// Watch for memory faults.  If we run low on memory invoke
		// the garbage collector and wait for it to run.

		catch (OutOfMemoryError e)
		{
			Runtime.getRuntime().gc() ;
			try { Thread.currentThread().sleep(300) ; }
			catch (InterruptedException ex) { }
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("PaletteDialog: Out of memory.") ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

		// Watch for internal faults during action events.

		catch (Throwable e)
		{
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			System.out.println("PaletteDialog: Internal fault, action " + evt.getActionCommand()) ;
			e.printStackTrace() ;
         JOptionPane.showMessageDialog(this,
            Kisekae.getCaptions().getString("InternalError") +
            "\n" + e.toString() + "\n" +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
		}
	}


	// Window Events

	public void windowOpened(WindowEvent evt)
   { if (CANCEL.isEnabled()) CANCEL.requestFocus() ; else OK.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { close() ; }


	// Method to set the dialog field values.

	void setValues()
	{
   	int celcount = 0 ;
      Vector cels = (config == null) ? null : config.getCels() ;
      int n = (cels == null) ? 0 : cels.size() ;
		for (int i = 0 ; i < n ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
			Object pid = c.getPaletteID() ;
			if (pid == null) continue ;
			if (pid.equals(palette.getIdentifier())) celcount++ ;
      }

      // Establish the field values.

      int mp = (multipalette != null) ? multipalette.intValue() : 0 ;
      n = palette.getBackgroundIndex() ;
      String s1 = (n < 0)
          ? Kisekae.getCaptions().getString("NoColorIndex") : "" + n ;
      n = palette.getTransparentIndex() ;
      String s2 = (n < 0)
          ? Kisekae.getCaptions().getString("NoColorIndex") : "" + n ;
      String pn = palette.getName() ;
      if (pn == null)
         pn = Kisekae.getCaptions().getString("UnknownValueText") ;
      String s3 = Kisekae.getCaptions().getString("NameText")
          + "  " + pn ;
		String s4 = Kisekae.getCaptions().getString("ImageCountText")
          + "  " + celcount ;
		String s5 = Kisekae.getCaptions().getString("PaletteGroupsText")
          + "  " + palette.getMultiPaletteCount() ;
		String s6 = Kisekae.getCaptions().getString("PaletteColorsText")
          + "  " + palette.getColorCount() ;
		String s7 = Kisekae.getCaptions().getString("PaletteBitsText")
          + "  " + palette.getBits() ;
		String s8 = Kisekae.getCaptions().getString("BackgroundIndexText")
          + "  " + s1 ;
		String s9 = Kisekae.getCaptions().getString("TransparentIndexText")
          + "  " + s2 ;
		namelabel.setText(s3) ;
		celslabel.setText(s4) ;
		groupslabel.setText(s5) ;
		colorslabel.setText(s6) ;
		bitslabel.setText(s7) ;
      backgroundlabel.setText(s8) ;
      transparentlabel.setText(s9) ;
      loadedlabel.setSelected(palette.isLoaded()) ;
      copylabel.setSelected(palette.isCopy()) ;
      visiblelabel.setSelected(palette.isVisible(mp)) ;

      // Examine the error state.

      if (palette.isError())
      {
         loadedlabel.setText(Kisekae.getCaptions().getString("ErrorStateText")) ;
         loadedlabel.setToolTipText(Kisekae.getCaptions().getString("ToolTipErrorState"));
         loadedlabel.setSelected(true) ;
      }

      // Set the palette color panel.

      if (jPanel7.getComponentCount() > 0) jPanel7.removeAll() ;
      for (int i = 0 ; i < Math.min(256,palette.getColorCount()) ; i++)
      {
      	JPanel p = new JPanel() ;
         p.setBackground(palette.getColor(mp,i)) ;
         jPanel7.add(p) ;
      }
      jPanel7.revalidate();

      // Set the user interface state where necessary.

      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListImagesMessage")))
      {
      	VIEW.setVisible(false) ;
         VIEW.setText(Kisekae.getCaptions().getString("ViewImageMessage"));
      	LISTBTN.setEnabled(true) ;
         showPalette() ;
      }
      if (LISTBTN.getText().equals(Kisekae.getCaptions().getString("ListTextMessage")))
      {
      	LISTBTN.setEnabled(true) ;
         showCels() ;
      }

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // A function to construct the palette text entries.

   private void showPalette()
   {
      heading.setText(Kisekae.getCaptions().getString("CharacteristicsHeadingText")) ;
   	Component c1 = jScrollPane1.getViewport().getView() ;
      if (c1 != null) jScrollPane1.getViewport().remove(c1) ;
		jScrollPane1.getViewport().add(LIST, null);
		jScrollPane1.setColumnHeaderView(null);
      listmodel.removeAllElements();
      String fn = Kisekae.getCaptions().getString("UnknownValueText") ;
      Object o = palette.getZipFile() ;
      if (o instanceof PkzFile) fn = ((PkzFile) o).getName() ;
      if (o instanceof LhaFile) fn = ((LhaFile) o).getName() ;
      if (o instanceof DirFile) fn = ((DirFile) o).getName() ;
      if (fn == null) fn = Kisekae.getCaptions().getString("UnknownValueText") ;
      String fntype = Kisekae.getCaptions().getString("ArchiveText") ;
      if (o instanceof DirFile) fntype = Kisekae.getCaptions().getString("DirectoryText") ;
      String pn = palette.getName() ;
      if (pn == null) pn = Kisekae.getCaptions().getString("UnknownValueText") ;
      listmodel.addElement(Kisekae.getCaptions().getString("NameText") + " " + pn) ;
		listmodel.addElement(fntype + " " + fn) ;
      listmodel.addElement(Kisekae.getCaptions().getString("BytesText") + " " + palette.getBytes()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("ColorBitsText") + " " + palette.getBits()) ;
		if (palette.isInternal())
         listmodel.addElement(Kisekae.getCaptions().getString("PaletteTypeText")
            + " " + Kisekae.getCaptions().getString("InternalStateText")) ;
      listmodel.addElement(Kisekae.getCaptions().getString("EncodingText") + " " + palette.getEncoding()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("ImportedPaletteText") + " " + palette.isImported()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("UpdatedPaletteText") + " " + palette.isUpdated()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("InternalPaletteText") + " " + palette.isInternal()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("BackgroundIndexText") + " " + palette.getBackgroundIndex()) ;
		listmodel.addElement(Kisekae.getCaptions().getString("TransparentIndexText") + " " + palette.getTransparentIndex()) ;
	}


   // A function to construct the cel list entries.

   private void showCels()
   {
      heading.setText(Kisekae.getCaptions().getString("ImageListHeadingText")) ;
		Component comp = jScrollPane1.getViewport().getView() ;
		if (comp != null) jScrollPane1.getViewport().remove(comp) ;
      int len = 60 ;
      format("clear","",0,len) ;
      format("center",Kisekae.getCaptions().getString("CelImageIDText"),0,5) ;
      format("center",Kisekae.getCaptions().getString("CelImageNameText"),6,15) ;
      format("left",Kisekae.getCaptions().getString("CelVisibleText"),22,9) ;
      format("center",Kisekae.getCaptions().getString("CelEventsText"),32,6) ;
      format("left",Kisekae.getCaptions().getString("CelCommentText"),39,20) ;
      String s = format(" ","",0,len) ;
      JLabel columns = new JLabel(s) ;
      columns.setFont(LIST.getFont());
		jScrollPane1.getViewport().add(LIST, null);
		jScrollPane1.setColumnHeaderView(columns);
      listmodel.removeAllElements();

		// Populate the display list with the cel vector contents.

      Vector cels = (config == null) ? null : config.getCels() ;
      int n = (cels == null) ? 0 : cels.size() ;
		for (int i = 0 ; i < n ; i++)
		{
			Cel c = (Cel) cels.elementAt(i) ;
         Palette p = c.getPalette() ;
			Object pid = (p != null) ? p.getIdentifier() : null ;
			if (pid == null) continue ;
			if (!pid.equals(palette.getIdentifier())) continue ;

			// Put cel in list.

			Rectangle r = c.getBoundingBox() ;
			String celEventCount = "" + c.getEventCount() ;
			String comment = c.getComment() ;
			boolean visible = c.isVisible() ;
         String visibletext = (visible)
             ? Kisekae.getCaptions().getString("BooleanTrueText")
             : Kisekae.getCaptions().getString("BooleanFalseText") ;
         len = 39 + comment.length() ;
         format("clear","",0,len) ;
         format("right",(c.getIdentifier()).toString(),0,4) ;
         format("left",c.getName(),6,15) ;
         format("left",visibletext,22,5) ;
         format("right",celEventCount,32,3) ;
         format("left",comment,39,comment.length()) ;
         s = format(" ","",0,len) ;
			listmodel.addElement(s) ;
		}
	}

   // We overload the KissDialog close method to release our preview image.

   void close()
   {
      flush() ;
      super.close() ;
   }

   // We release references to some of our critical objects.

   private void flush()
   {
   	me = null ;
      palette = null ;
      cel = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		VIEW.removeActionListener(this) ;
		LISTBTN.removeActionListener(this) ;
		LIST.removeMouseListener(mouseListListener) ;
      LIST.removeListSelectionListener(listListener) ;
      multiselect.removeItemListener(multiListener) ;
      visiblelabel.removeActionListener(this) ;
      loadedlabel.removeActionListener(this) ;
      copylabel.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }
}
