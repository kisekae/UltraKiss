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
* VariableDialog class
*
* Purpose:
*
* This class defines an instance of the Kisekae variable dialog.  This dialog
* shows all variables defined for the active configuration.  It is an instance
* of a KissDialog.
*
* The dialog display is updated on a periodic basis.
*
*/

import java.awt.*;
import java.awt.event.* ;
import java.util.* ;
import java.text.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import javax.swing.border.* ;
import javax.swing.table.* ;




final class VariableDialog extends KissDialog
	implements ActionListener, WindowListener
{
	// Dialog attributes

   private JDialog me = null ;					  		// Reference to ourselves
   private Configuration config = null ;  	  		// The current configuration
	private Variable variable = null ;    		  		// The variable object
	private VariableTableData tabledata = null ;		// The table data object
   private int sortcolumn = 0 ; 					  		// The column to sort
	private boolean ascending = true ;			  		// The sort direction

	// Timer update attributes

	private Thread thread = null ;     					// Periodic mem update
	private int period = 1000 ;        					// Update period
   private boolean stop = false ;						// Thread stop flag

	// User interface objects.

	private JPanel panel1 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JButton OK = new JButton();
	private JButton CANCEL = new JButton();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane();
   private Border eb1 = BorderFactory.createEmptyBorder(10,10,10,10) ;
	private Border eb3 = BorderFactory.createEmptyBorder(0,0,0,0) ;
	private JLabel heading = new JLabel();
	private JTable jTable1 = new JTable();


	// Register for events.

   MouseListener columnListener = new MouseAdapter()
   {
		public void mouseClicked(MouseEvent e)
      {
      	if (jTable1 == null) return ;
         if (tabledata == null) return ;
        	TableColumnModel columnmodel = jTable1.getColumnModel() ;
         int columnindex = columnmodel.getColumnIndexAtX(e.getX()) ;
         int modelindex = columnmodel.getColumn(columnindex).getModelIndex() ;

         if (modelindex < 0) return ;
         if (sortcolumn == modelindex)
         	ascending = !ascending ;
         else
           	sortcolumn = modelindex ;

         for (int i = 0 ; i < tabledata.columns.length ; i++)
         {
           	TableColumn column = columnmodel.getColumn(i) ;
            column.setHeaderValue(tabledata.getColumnName(column.getModelIndex())) ;
         }

         jTable1.getTableHeader().repaint() ;

         Vector v = tabledata.getData() ;
         Collections.sort(v,new SortComparator(modelindex,ascending)) ;
         jTable1.tableChanged(new TableModelEvent(tabledata)) ;
         jTable1.repaint() ;
      }
   } ;


	// Constructor

   public VariableDialog(JDialog f, Configuration c)
   { super(f,null,false) ; init(c) ; }

	public VariableDialog(JFrame f, Configuration c)
	{ super(f,null,false) ; init(c) ; }

   private void init(Configuration c)
   {
		me = this ;
		config = c ;
      variable = (c != null) ? c.getVariable() : null ;
      String title = Kisekae.getCaptions().getString("VariableDialogTitle")  ;
      if (c.getName() != null) title += " - " + c.getName() ;
      setTitle(title) ;

		// Construct the user interface.  Values must be set first otherwise
      // the mouse listener is not recognized.

		setValues() ;
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

		// Center the frame in the panel space.

		doLayout() ;
      Dimension d1 = getParentSize() ;
      Dimension d2 = getSize() ;
      if (d1.height > d2.height) d2.height = d1.height ;
      if (d1.width > d2.width) d2.width = d1.width ;
      setSize(d2) ;
 		center(this) ;
      
		// Register for events.

		OK.addActionListener(this) ;
		CANCEL.addActionListener(this) ;
		addWindowListener(this) ;

		// Start the periodic update thread.

		thread = new Thread()
		{
			public void run()
			{
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				try
				{
					while (!stop)
					{
						if (tabledata != null) tabledata.setDefaultData() ;
						if (jTable1 != null) jTable1.revalidate() ;
						if (me != null) me.repaint() ;
						sleep(period) ;
					}
				}
				catch (InterruptedException e) { }
			}
		} ;
		thread.start() ;
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
		Border cb1 = new CompoundBorder(BorderFactory.createEtchedBorder(),eb1) ;
		jPanel1.setBorder(eb1);
 		jPanel1.setLayout(borderLayout2);
		jPanel2.setBorder(eb1);
 		jPanel2.setLayout(new BoxLayout(jPanel2,BoxLayout.X_AXIS));
		jPanel3.setBorder(eb1);
		jPanel3.setLayout(borderLayout3);
      heading.setText(Kisekae.getCaptions().getString("VariableHeadingText"));
		heading.setBorder(eb3);
		heading.setHorizontalAlignment(SwingConstants.CENTER);

		getContentPane().add(panel1);
		panel1.add(jPanel1, BorderLayout.NORTH);
      jPanel1.add(heading, BorderLayout.CENTER);
		panel1.add(jPanel2, BorderLayout.SOUTH);
  	   jPanel2.add(Box.createGlue()) ;
      jPanel2.add(OK, null);
      jPanel2.add(Box.createGlue()) ;
		jPanel2.add(CANCEL, null);
      jPanel2.add(Box.createGlue()) ;
		panel1.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jTable1, null);
	}



	// The action method is used to process control events.

	public void actionPerformed(ActionEvent evt)

	{
      Object source = evt.getSource() ;

      // The user cannot change state variable values.

      if (source instanceof JCheckBox)
      {
			JCheckBox cb = (JCheckBox) source ;
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
   	if (jTable1 == null) return ;
   	tabledata = new VariableTableData(variable) ;
		jTable1.setAutoCreateColumnsFromModel(false) ;
      jTable1.setRowSelectionAllowed(false) ;
      jTable1.setColumnSelectionAllowed(false) ;
		jTable1.setModel(tabledata) ;

		// Add new table columns.

		for (int i = 0 ; i < 2 ; i++)
		{
      	DefaultTableCellRenderer r = new DefaultTableCellRenderer() ;
         r.setHorizontalAlignment(tabledata.columns[i].alignment) ;
         TableColumn column = new TableColumn(i,tabledata.columns[i].width,r,null) ;
         jTable1.addColumn(column) ;
      }

      JTableHeader header = jTable1.getTableHeader() ;
		header.setUpdateTableInRealTime(true) ;
		header.addMouseListener(columnListener) ;

		// Set the default button for an enter key.

		JRootPane rootpane = getRootPane()  ;
		rootpane.setDefaultButton((CANCEL.isEnabled()) ? CANCEL : OK) ;
   }


   // We overload the KissDialog close method.

   void close()
   {
   	stop = true ;
		flush() ;
		super.close() ;
	}


	// We release references to some of our critical objects.  We do this
	// so that the garbage collector can potentially reclaim the data set
	// objects when the data set is closed, even if a problem occurs while
	// disposing the dialog window.

	private void flush()
	{
		me = null ;
		config = null ;
		variable = null ;
		thread = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		OK.removeActionListener(this) ;
		CANCEL.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }


   // Inner classes to define the table data characteristics.

   class VariableData
   {
   	public String name ;
      public String value ;

      public VariableData(String name, String value)
      {
      	this.name = name ;
         this.value = value ;
      }
   }


   class ColumnData
   {
   	protected String title ;
      protected int width ;
      protected int alignment ;

      public ColumnData(String title, int width, int alignment)
      {
      	this.title = title ;
         this.width = width ;
         this.alignment = alignment ;
      }
   }


   class VariableTableData extends AbstractTableModel
   {
   	final public ColumnData columns[] = {
      	new ColumnData(Kisekae.getCaptions().getString("VariableNameText"),100,JLabel.LEFT),
         new ColumnData(Kisekae.getCaptions().getString("VariableValueText"),350,JLabel.LEFT) } ;

      private Vector vector = null ;
      private Variable variable = null ;

      public VariableTableData(Variable variable)
      {
      	this.variable = variable ;
      	vector = new Vector() ;
         setDefaultData() ;
      }

      public void setDefaultData()
      {
      	vector.removeAllElements() ;
         if (variable == null) return ;

         Enumeration enum1 = variable.getVariables() ;
         while (enum1.hasMoreElements())
         {
         	Object key = enum1.nextElement() ;
            if (!(key instanceof String)) continue ;
            Object value = variable.getValue((String) key,null) ;
            String keyvalue = (value == null)
                ? Kisekae.getCaptions().getString("UnknownValueText")
                : value.toString() ;
            vector.addElement(new VariableData((String) key, keyvalue)) ;
         }

         Collections.sort(vector,new SortComparator(sortcolumn,ascending)) ;
		}

      public int getRowCount() { return (vector == null) ? 0 : vector.size() ; }

      public int getColumnCount() { return columns.length ; }

      public String getColumnName(int c)
      {
      	String s = columns[c].title ;
         if (c == sortcolumn)
         	s += (ascending) ? " >>" : " <<" ;
      	return s ;
      }

      public boolean isCellEditable(int row, int col) { return false ; }

      public Object getValueAt(int row, int col)
      {
      	if (row < 0 || row > getRowCount()) return " " ;
         VariableData rowvalue = (VariableData) vector.elementAt(row) ;
         switch (col)
         {
         	case 0: return rowvalue.name ;
            case 1: return rowvalue.value ;
         }
         return " " ;
      }

      public String getTitle() { return Kisekae.getCaptions().getString("VariableValuesText") ; }

      public Vector getData() { return vector ; }
   }


   class SortComparator implements Comparator
   {
   	private int column ;
      private boolean ascending ;

      public SortComparator(int column, boolean ascending)
      {
      	this.column = column ;
         this.ascending = ascending ;
      }

      public int compare(Object o1, Object o2)
      {
      	if (!(o1 instanceof VariableData)) return 0 ;
         if (!(o2 instanceof VariableData)) return 0 ;
         VariableData vd1 = (VariableData) o1 ;
         VariableData vd2 = (VariableData) o2 ;
         int result = 0 ;

         switch (column)
         {
         	case 0:
            	result = vd1.name.compareTo(vd2.name) ;
               break ;

            case 1:
            	result = vd1.value.compareTo(vd2.value) ;
               break ;
         }

         if (!ascending) result = -result ;
         return result ;
      }
   }
}