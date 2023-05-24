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
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Collections ;
import java.util.StringTokenizer ;
import java.util.NoSuchElementException ;
import java.io.File ;
import java.awt.dnd.* ;
import java.awt.datatransfer.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.undo.* ;


final class LayerDialog extends KissDialog
	implements ActionListener, WindowListener, ListSelectionListener
{
	// Dialog attributes

   private JDialog me = null ;								// Reference to ourselves
	private Vector cels = null ;       				 		// The configuration cels
   private Configuration config = null ;					// Our current context
   private DefaultListModel sourceModel = null ;
   private Vector initialstate = null ;
   private ListDragSource ds = null ;
   private ListDropTarget dt = null ;
	private UndoManager undomanager = null ;
   private ImagePreview preview = null ;        // Our panel preview pane
   private Dimension previewsize = null ;       // Size of preview pane

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
	private JButton UNDO = new JButton();
	private JButton REDO = new JButton();
	private JButton UNSELECT = new JButton();
	private JTextField Layer = new JTextField();
	private JList LIST = null ;
	private JScrollPane scrollPane = new JScrollPane();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private GridLayout gridLayout2 = new GridLayout();
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private Border eb2 = BorderFactory.createEmptyBorder(5,5,5,5) ;
   private Border eb3 = BorderFactory.createEmptyBorder(0,0,5,0) ;
   private Border eb4 = BorderFactory.createEmptyBorder(0,10,10,10) ;
	private Border cb1 = new CompoundBorder(eb2,BorderFactory.createRaisedBevelBorder()) ;
	private JLabel heading = new JLabel();
	private JLabel layerhdg = new JLabel();


	// Constructor

   public LayerDialog(JDialog f, Configuration c)
   { super(f,null,false) ; init(c) ; }

	public LayerDialog(JFrame f, Configuration c)
	{ super(f,null,false) ; init(c) ; }

   private void init(Configuration cfg)
   {
      me = this ;
      config = cfg ;
      String title = Kisekae.getCaptions().getString("LayerDialogTitle") ;
      setTitle(title) ;

      // Invert the configuration cel list so that lower index duplicate level
      // cels appear at the top of the working list.

      Vector cels = (config == null) ? null : (Vector) config.getCels() ;
      Vector sortedcels = new Vector() ;
      for (int i = cels.size()-1 ; i >= 0 ; i--)
      	sortedcels.addElement(cels.elementAt(i)) ;

      // Sort the working list by z-level.  The sort retains original order
      // for duplicate values.

      Collections.sort(sortedcels,new LevelComparator()) ;
      LIST = new ListAutoScroll() ;
      LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION) ;
		sourceModel = new DefaultListModel() ;
      int len = 55 ;
      format("clear","",0,len) ;
      format("left",Kisekae.getCaptions().getString("LayerFieldLayer"),0,5) ;
      format("left",Kisekae.getCaptions().getString("LayerFieldObject"),6,8) ;
      format("center",Kisekae.getCaptions().getString("LayerFieldImage"),15,15) ;
      format("center",Kisekae.getCaptions().getString("LayerFieldPalette"),31,7) ;
      format("center",Kisekae.getCaptions().getString("LayerFieldPages"),39,15) ;
      String s = format(" ","",0,len) ;
      JLabel columns = new JLabel(s) ;
      columns.setFont(LIST.getFont());

      // Create the draggable list entries.  We format only active cels
      // that are not internal.

      for (int i = 0 ; i < sortedcels.size() ; i++)
      {
      	Cel c = (Cel) sortedcels.elementAt(i) ;
         s = c.formatCel(config) ;
         if (s == null) continue ;
         StringTokenizer st = new StringTokenizer(s,"\t") ;
			format("clear","",0,len) ;
			format("right",c.getLevel(),0,4) ;
         try
         {
   			format("left",st.nextElement(),6,8) ;
            String relativename = (String) st.nextElement() ;
            int n = relativename.lastIndexOf(File.separator) ;
            if (n > 0) relativename = relativename.substring(n+1) ;
   			format("left",relativename,15,15) ;
   			format("left",st.nextElement(),31,7) ;
            format("left",st.nextElement(),39,15) ;
         }
         catch (NoSuchElementException e) { }
			s = format(" ","",0,len) ;
			sourceModel.addElement(new ListEntry(c.getIdentifier(),c.getLevel(),s)) ;
      }

      // Create the scrollable dialog initial state list.

		sourceModel.addElement(new ListEntry(" ")) ;
      initialstate = new Vector() ;
      for (int i = 0 ; i < sourceModel.size() ; i++)
      {
        	ListEntry le = (ListEntry) sourceModel.getElementAt(i) ;
         Object [] layeredit = new Object [2] ;
         layeredit[0] = le.getObject() ;
         layeredit[1] = le.getLevel() ;
         initialstate.addElement(layeredit) ;
      }

      // Create the scrollable dialog drag and drop targets.

      undomanager = new UndoManager() ;
		LIST.setModel(sourceModel) ;
      LIST.setCellRenderer(new ListEntry());
		scrollPane = new JScrollPane(LIST) ;
		scrollPane.setColumnHeaderView(columns) ;
      ds = new ListDragSource(LIST,DnDConstants.ACTION_MOVE) ;
      dt = new ListDropTarget(LIST,ds,undomanager) ;
      ds.callback.addActionListener(this) ;

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

      // Set interface values.

      setValues() ;

		// Center the frame in the panel space.

 		center(this) ;

		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		UNDO.addActionListener(this) ;
		REDO.addActionListener(this) ;
		UNSELECT.addActionListener(this) ;
      LIST.addListSelectionListener(this) ;
      Layer.addActionListener(this) ;
		addWindowListener(this);
   }



   // User interface initialization.

	void jbInit() throws Exception
	{
		OK.setText(Kisekae.getCaptions().getString("OkMessage")) ;
      OK.setEnabled(false);
		UNDO.setText(Kisekae.getCaptions().getString("MenuEditUndo")) ;
		REDO.setText(Kisekae.getCaptions().getString("MenuEditRedo")) ;
      UNSELECT.setText(Kisekae.getCaptions().getString("UnselectMessage")) ;
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage")) ;
      layerhdg.setText(Kisekae.getCaptions().getString("LayerLabelText"));
		Layer.setPreferredSize(new Dimension(30, 21));

		panel1.setLayout(borderLayout1);
		jPanel1.setBorder(eb1);
 		jPanel1.setLayout(new BoxLayout(jPanel1,BoxLayout.X_AXIS));
		jPanel2.setBorder(eb1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setLayout(borderLayout3);
		jPanel3.setBorder(eb4);
		jPanel4.setLayout(gridLayout1);
		gridLayout1.setColumns(1);
		gridLayout1.setRows(2);
      preview = new ImagePreview() ;
		jPanel7.setLayout(new BorderLayout()) ;
      jPanel7.setBorder(cb1) ;
      jPanel7.add(preview,BorderLayout.CENTER) ;
      previewsize = new Dimension(200,200) ;
      preview.setSize(previewsize) ;
      preview.setPreferredSize(previewsize) ;
      preview.setShowState(false) ;
		jPanel8.setLayout(gridLayout2) ;
		gridLayout2.setColumns(2);
		gridLayout2.setRows(1);
      jPanel8.add(layerhdg) ;
      jPanel8.add(Layer) ;

      heading.setText(" ");
      heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);

		getContentPane().add(panel1);
		panel1.add(jPanel4, BorderLayout.SOUTH) ;
      jPanel4.add(jPanel1, null) ;
  	   jPanel1.add(Box.createGlue()) ;
		jPanel1.add(UNDO, null);
  	   jPanel1.add(Box.createGlue()) ;
		jPanel1.add(REDO, null);
  	   jPanel1.add(Box.createGlue()) ;
		jPanel1.add(UNSELECT, null);
      jPanel1.add(Box.createGlue()) ;
		jPanel1.add(jPanel8, null);
      jPanel1.add(Box.createGlue()) ;
      jPanel4.add(jPanel2, null) ;
      jPanel2.add(Box.createGlue()) ;
 		jPanel2.add(OK, null);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(heading, BorderLayout.NORTH);
		jPanel3.add(scrollPane, BorderLayout.CENTER);
      panel1.add(jPanel7, BorderLayout.EAST) ;

  		// Set the default button for an enter key.

  		JRootPane rootpane = getRootPane()  ;
  		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
	}


   void setValues()
   {
      UNDO.setEnabled(undomanager.canUndo()) ;
      REDO.setEnabled(undomanager.canRedo()) ;
      UNSELECT.setEnabled(!LIST.isSelectionEmpty()) ;
      OK.setEnabled(OK.isEnabled() || undomanager.canUndo()) ;
      layerhdg.setEnabled(!LIST.isSelectionEmpty()) ;
      Layer.setEnabled(!LIST.isSelectionEmpty()) ;
      MainFrame mainframe = Kisekae.getMainFrame() ;
      PanelFrame panel = (mainframe != null) ? mainframe.getPanel() : null ;
      Image image = (panel != null) ? panel.getImage() : null ;
      Color bg = (panel != null) ? panel.getBackground() : null ;
      if (bg != null) preview.setBackground(bg) ;
      preview.updateImage(image) ;
   }


	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)
	{
      Object source = evt.getSource() ;

      try
      {
         // OK performs the update and exists.  This is an undoable edit
         // in the panel frame.

	      if (source == OK)
	      {
		      Vector finalstate = new Vector() ;
	         for (int i = 0 ; i < sourceModel.size() ; i++)
	         {
	         	ListEntry le = (ListEntry) sourceModel.getElementAt(i) ;
		         Object [] layeredit = new Object [2] ;
		         layeredit[0] = le.getObject() ;
		         layeredit[1] = new Integer(i) ;
	            finalstate.addElement(layeredit) ;
	         }

            // Accept and apply the update.

      		if (config != null) config.setUpdated(true) ;
            MainFrame mainframe = Kisekae.getMainFrame() ;
            if (mainframe != null)
   	      	mainframe.adjustLayer(initialstate,finalstate) ;
            close() ;
            dispose() ;
            return ;
         }

         // Unselect the current active list selection.

         if (source == UNSELECT)
         {
         	ds.clearSelection() ;
            return ;
         }

	      // Perform undo operation.

	      if (source == UNDO)
	      {
         	if (undomanager.canUndo()) undomanager.undo() ;
            apply() ;
            setValues() ;
            return ;
	      }

	      // Perform redo operation.

	      if (source == REDO)
	      {
         	if (undomanager.canRedo()) undomanager.redo() ;
            apply() ;
            setValues() ;
            return ;
	      }

         // Cancel operation.  Revert to the initial state.

	      if (source == CANCEL)
	      {
	      	cancel() ;
         	close() ;
            dispose() ;
            return ;
	      }

         // Layer set level operation.

	      if (source == Layer)
	      {
            Layer.requestFocus() ;
            if (LIST.isSelectionEmpty()) return ;
            Object selected = LIST.getSelectedValue() ;
            if (!(selected instanceof ListEntry)) return ;

            // Validate the input text.

            Integer n = null ;
            String s = Layer.getText() ;
            if (s == null) return ;
            try { n = new Integer(s) ; }
            catch (NumberFormatException e) { }
            if (n == null) return ;

            // Move the selected entry and apply the update.

            int n1 = sourceModel.indexOf(selected) ;
            Object draglocator = (n1 == 0) ? null : sourceModel.getElementAt(n1-1) ;
            sourceModel.removeElement(selected) ;
            int i = findListLevel(n) ;
            sourceModel.insertElementAt(selected,i) ;
            int n2 = sourceModel.indexOf(selected) ;
            Object droplocator = (n2 == 0) ? null : sourceModel.getElementAt(n2-1) ;
            LIST.setSelectedValue(selected,true) ;

            // Create an undo object.

            UndoableLayerEdit le = new UndoableLayerEdit(selected,draglocator,droplocator,n) ;
   			UndoableEditEvent uevt = new UndoableEditEvent(this,le) ;
				if (undomanager != null) undomanager.undoableEditHappened(uevt) ;
            ((ListEntry) selected).setLevel(n) ;
            apply() ;
            setValues() ;
            return ;
	      }

         // Undo callback from drag target list.  This indicates that a
         // list item was removed.  We apply the update.  As there is no
         // initial state provided during the update an undoable edit is
         // not created.

			if ("Drag Callback".equals(evt.getActionCommand()))
	      {
            apply() ;
            setValues() ;
            return ;
	      }
      }

      // Watch for errors.

      catch (Exception e)
      {
         System.out.println("LayerDialog: Exception " + e.toString());
         e.printStackTrace() ;
  			JOptionPane.showMessageDialog(me,
            e.toString(),
            Kisekae.getCaptions().getString("LayeringFault"),
  				JOptionPane.ERROR_MESSAGE) ;
      }
   }


	// Window Events

	public void windowOpened(WindowEvent evt) { CANCEL.requestFocus() ; }
	public void windowClosed(WindowEvent evt) { }
	public void windowIconified(WindowEvent evt) { }
	public void windowDeiconified(WindowEvent evt) { }
	public void windowActivated(WindowEvent evt) { }
	public void windowDeactivated(WindowEvent evt) { }
	public void windowClosing(WindowEvent evt) { cancel() ; close() ; }


   // We overload the KissDialog close method to release our storage.

   void close()
   {
   	flush() ;
      super.close() ;
   }


   // A utility function to cancel any updates.

   private void cancel()
   {
      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe != null) mainframe.adjustLayer(null,initialstate) ;
   }


   // A utility function to apply any updates.

   private void apply()
   {
      Vector finalstate = new Vector() ;
      for (int i = 0 ; i < sourceModel.size() ; i++)
      {
         ListEntry le = (ListEntry) sourceModel.getElementAt(i) ;
         Object [] layeredit = new Object [2] ;
         layeredit[0] = le.getObject() ;
         layeredit[1] = new Integer(i) ;
         finalstate.addElement(layeredit) ;
      }

      // Apply the update for visual feedback.

      MainFrame mainframe = Kisekae.getMainFrame() ;
      if (mainframe != null)
         mainframe.adjustLayer(null,finalstate) ;
   }


   // A utility function to find a list entry with a specific level.

   private int findListLevel(Integer n)
   {
      int i ;
      for (i = 0 ; i < sourceModel.size() ; i++)
      {
         Object o = sourceModel.elementAt(i) ;
         if (!(o instanceof ListEntry)) continue ;
         ListEntry entry = (ListEntry) o ;
         Integer level = entry.getLevel() ;
         if (level == null) continue ;
         if (level.intValue() >= n.intValue()) break ;
      }

      // Bound the returned value to the list size.

      if (i >= sourceModel.size()) i = sourceModel.size() - 1 ;
      if (i < 0) i = 0 ;
      return i ;
   }


   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing with this dialog window.

   private void flush()
   {
   	me = null ;
      cels = null ;
      config = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		UNDO.removeActionListener(this) ;
		REDO.removeActionListener(this) ;
		UNSELECT.removeActionListener(this) ;
      LIST.removeListSelectionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }


   // ListSelectionListener interface methods.

   public void valueChanged(ListSelectionEvent e)
   {
      if (LIST.getValueIsAdjusting()) return ;
      setValues() ;
   }


	// Inner class to construct an undoable edit operation.
	// These edits occur if we move an object.  The drag locator
   // and drop locator are the list objects after which the
   // moving edit object is to be inserted.

	class UndoableLayerEdit extends AbstractUndoableEdit
   {
      private Object dropobject = null ;
      private Object draglocator = null ;
      private Object droplocator = null ;
      private Integer olddroplevel = null ;
      private Integer newdroplevel = null ;


		// Constructor for cel layer adjustment changes

		public UndoableLayerEdit(Object dropobject, Object drag, Object drop, Integer n)
      {
      	this.dropobject = dropobject ;
         this.draglocator = drag ;
         this.droplocator = drop ;
         this.newdroplevel = n ;
         if (dropobject instanceof ListEntry)
            olddroplevel = ((ListEntry) dropobject).getLevel() ;
      }

      // Return the undo/redo menu name

      public String getPresentationName()
      { return Kisekae.getCaptions().getString("UndoLayerName") ;  }

		// Undo a change.

      public void undo()
      {
			super.undo() ;
         try
         {
         	DefaultListModel model = (DefaultListModel) LIST.getModel() ;
            for (int i = 0 ; i < model.size() ; i++)
            {
               if (!model.elementAt(i).toString().equals(dropobject.toString()))
                  continue ;
               model.removeElementAt(i) ;
               break ;
            }
            int n = (draglocator == null) ? -1 : model.indexOf(draglocator) ;
         	model.add(n+1,dropobject) ;
            LIST.setSelectedValue(dropobject,true) ;
            if (dropobject instanceof ListEntry)
               ((ListEntry) dropobject).setLevel(olddroplevel) ;
         }
         catch (ArrayIndexOutOfBoundsException e) { }
		}

		// Redo a change.

      public void redo()
      {
			super.redo() ;
         try
         {
         	DefaultListModel model = (DefaultListModel) LIST.getModel() ;
            for (int i = 0 ; i < model.size() ; i++)
            {
               if (!model.elementAt(i).toString().equals(dropobject.toString()))
                  continue ;
               model.removeElementAt(i) ;
               break ;
            }
            int n = (droplocator == null) ? -1 : model.indexOf(droplocator) ;
         	model.add(n+1,dropobject) ;
            LIST.setSelectedValue(dropobject,true) ;
            if (dropobject instanceof ListEntry && droplocator instanceof ListEntry)
               ((ListEntry) dropobject).setLevel(newdroplevel) ;
         }
         catch (ArrayIndexOutOfBoundsException e) { }
		}
	}
}
