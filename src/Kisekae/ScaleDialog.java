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



import java.awt.* ;
import java.awt.event.* ;
import java.util.Vector ;
import javax.swing.* ;


// Class to define the image scaling dialog.

class ScaleDialog extends KissDialog
	implements WindowListener, ActionListener, Runnable
{
   private JDialog me = null ;            // Reference to ourselves
  	private Thread thread = null ;			// The scaling thread
   private Vector cels = null ;           // The cels to scale
   private float sf = 1.0f ;              // The current scale factor
   private int progress = 0 ;             // Our progress
   private boolean cancel = false ;    	// True if scaling cancelled

   // Control definitions

	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JPanel jPanel1 = new JPanel();
 	private JPanel jPanel1a = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JLabel Status = new JLabel();
	private JLabel FileName = new JLabel();
	private JProgressBar Progress = new JProgressBar();
	private JButton CANCEL = new JButton();

	// Our callback button that other components can attach listeners to.

	protected CallbackButton callback = new CallbackButton(this,"ImageScale Callback") ;


	// Constructor.

	public ScaleDialog(JFrame parent, Vector cels, float scale)
	{
      super(parent,"",true) ;
      this.me = this ;
     	this.parent = parent ;
      this.cels = cels ;
      this.sf = scale ;
      if (cels == null) this.cels = new Vector() ;
      setTitle(Kisekae.getCaptions().getString("ScaleDialogTitle")) ;

		// Set the frame characteristics.

		try { jbInit(); }
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

		// Center the dialog in the screen space.

 		setSize(500,150) ;
 		center(this) ;

		// Setup to catch window events in this frame.

		addWindowListener(this) ;
      CANCEL.addActionListener(this) ;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;
      setVisible(true) ;
	}


   // User interface initialization.

	private void jbInit() throws Exception
	{
		Status.setBorder(BorderFactory.createEmptyBorder(0,10,10,0));
		Status.setPreferredSize(new Dimension(150, 20));
      String s = Kisekae.getCaptions().getString("ScaleFactorText") ;
      int i1 = s.indexOf('[') ;
      int j1 = s.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s = s.substring(0,i1+1) + sf + s.substring(j1) ;
      Status.setText(s) ;
		FileName.setBorder(BorderFactory.createEmptyBorder(0,0,10,10));
		FileName.setPreferredSize(new Dimension(300, 20));
		FileName.setHorizontalAlignment(SwingConstants.CENTER);
//		CANCEL.setPreferredSize(new Dimension(85, 27));
		CANCEL.setText(Kisekae.getCaptions().getString("CancelMessage"));
		Progress.setPreferredSize(new Dimension(300, 16));

		jPanel1.setLayout(borderLayout2);
 		jPanel1a.setLayout(borderLayout3);
		jPanel1a.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		jPanel3.setLayout(new BoxLayout(jPanel3,BoxLayout.X_AXIS));
		jPanel3.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		this.getContentPane().setLayout(borderLayout1);
		this.getContentPane().add(jPanel1, BorderLayout.NORTH);
		jPanel1.add(jPanel1a, BorderLayout.NORTH);
		jPanel1a.add(Status, BorderLayout.WEST);
		jPanel1a.add(FileName, BorderLayout.EAST);
		jPanel1a.add(Progress, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel3, BorderLayout.SOUTH);
      jPanel3.add(Box.createGlue()) ;
      jPanel3.add(CANCEL, null);
      jPanel3.add(Box.createGlue()) ;
	}


	// Action Events.

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;

		// A Cancel request closes the dialog.

		if (source == CANCEL)
		{
         cancel = true ;
      	flush() ;
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
	public void windowClosing(WindowEvent evt) { cancel = true ; flush() ; }


   // Return the cancel indicator.

   public boolean isCancelled() { return cancel ; }

	// Return the scale factor.

   float getScaleFactor() { return sf ; }

	// Method to intialize the progress bar display.

	void initProgress(int max)
	{
		progress = 0 ;
		Progress.setValue(0) ;
		Progress.setMaximum(max) ;
	}

	// Method to update the progress bar display.

	void updateProgress(Cel c)
	{
		progress += 1 ;
		Progress.setValue(progress) ;
      if (c == null) return ;
      String s = c.getName() + " [" + c.getBytes() + " bytes]" ;
      String s1 = Kisekae.getCaptions().getString("FileNameText") ;
      int i1 = s1.indexOf('[') ;
      int j1 = s1.indexOf(']') ;
      if (i1 >= 0 && j1 > i1)
         s1 = s1.substring(0,i1) + s + s1.substring(j1+1) ;
      FileName.setText(s1);
	}

   // We release references to some of our critical objects.  We do this
   // so that the garbage collector can potentially reclaim the data set
   // objects when the data set is closed, even if a problem occurs while
   // disposing the dialog window.

   private void flush()
   {
   	me = null ;
   	parent = null ;
      thread = null ;
      cels = null ;

      // Flush the dialog contents.

      setVisible(false) ;
		CANCEL.removeActionListener(this) ;
		removeWindowListener(this);
		getContentPane().removeAll() ;
		getContentPane().removeNotify() ;
   }


  	// The scaling code.  This code runs as a separate thread.

  	public void run()
  	{
  		thread = Thread.currentThread() ;
  		thread.setName("ImageScale") ;
  		thread.setPriority(Thread.MIN_PRIORITY) ;
      me.setModal(true) ;
      initProgress(cels.size()) ;

      // Scale every cel.

      try
      {
         for (int i = 0 ; i < cels.size() ; i++)
         {
         	if (cancel) break ;
            Cel cel = (Cel) cels.elementAt(i) ;
            updateProgress(cel);
         	cel.scaleImage(sf) ;
         }
      }

      // Watch for scaling faults.  We want to catch all errors in this
      // thread so we can recover from any failure.

		catch (Throwable e)
		{
      	cancel = true ;
			PrintLn.println("ScaleDialog: " + e.toString()) ;
			Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }

         // Show a message dialog.

			JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
           	e.toString(),
            Kisekae.getCaptions().getString("ScalingFault"),
            JOptionPane.ERROR_MESSAGE) ;
		}

      // Restore all the cels if scaling was cancelled.

      if (cancel)
      {
      	sf = 1.0f ;
	      for (int i = 0 ; i < cels.size() ; i++)
         {
	         try  { ((Cel) cels.elementAt(i)).scaleImage(0.0f) ; }
	         catch (Throwable e) { }
			}

			// Set the user interface to show that we are not scaled.

         if (parent instanceof MainFrame)
         	((MainFrame) parent).setFitScreen(false) ;

         // Invoke the garbage collector.

			Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
      }

      // Perform any callbacks that are required.

		me.setModal(false) ;
		flush() ;
      dispose() ;
      callback.doClick() ;
   }
   
   void setValues() { }
}
