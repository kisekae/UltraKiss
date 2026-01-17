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
* PrintPreview Class
*
* Purpose:
*
* This object is a frame window to preview print pages.  It displays the
* component that will be printed.
*
*/

import java.awt.* ;
import java.awt.event.* ;
import java.awt.image.* ;
import java.util.* ;
import java.net.URL ;
import java.net.MalformedURLException ;
import java.awt.print.* ;
import javax.swing.* ;
import javax.swing.border.* ;
import javax.swing.event.* ;

final class PrintPreview extends KissFrame
	implements ActionListener, WindowListener
{
   protected int NO_SUCH_PAGE = Printable.NO_SUCH_PAGE ;
   protected int PAGE_EXISTS = Printable.PAGE_EXISTS ;

   protected JFrame me = null ;
   protected Printable target = null ;
   protected PreviewContainer preview = null ;
   protected PageFormat pageFormat = null ;
   protected PagePreview pp = null ;
   protected LoadThread loader = null ;
   protected JComboBox cbScale ;
   protected JButton wait ;
   protected Vector newcomponents = null ;
   protected Component [] comps = null ;
	private JButton RUNSTATE = null ;
   protected int wPage = 0 ;
   protected int hPage = 0 ;
   protected int scale = 0 ;
   protected int orientation ;
   protected int w ;
   protected int h ;

   

   // Constructor

   public PrintPreview(Printable t, int orientation)
   { this(t,Kisekae.getCaptions().getString("PrintPreviewTitle"),orientation) ; }

   public PrintPreview(Printable t, String title, int orientation)
   {
      super(title) ;
      me = this ;
      setSize(600,400) ;
      target = t ;
      this.orientation = orientation ;
     	setIconImage(Kisekae.getIconImage()) ;

      // Construct the user interface.

		URL iconfile = null ;
      JToolBar tb = new JToolBar() ;
		iconfile = Kisekae.getResource("Images/print.gif") ;
      JButton bt = new JButton(Kisekae.getCaptions().getString("PrintMessage")) ;
		if (iconfile != null) bt.setIcon(new ImageIcon(iconfile)) ;
      bt.addActionListener(this) ;
      bt.setAlignmentY(0.5f) ;
      bt.setMargin(new Insets(2,6,2,6)) ;
      tb.add(bt) ;
      tb.addSeparator() ;

      bt = new JButton(Kisekae.getCaptions().getString("PageSetupMessage")) ;
      bt.addActionListener(this) ;
      bt.setAlignmentY(0.5f) ;
      bt.setMargin(new Insets(2,6,2,6)) ;
      tb.add(bt) ;
      tb.addSeparator() ;

      String[] scales = { "10 %", "25 %", "50 %", "100 %" } ;
      cbScale = new JComboBox(scales) ;
      cbScale.addActionListener(this) ;
      cbScale.setMaximumSize(cbScale.getPreferredSize()) ;
      cbScale.setEditable(true) ;
      tb.add(cbScale) ;
      tb.addSeparator() ;

      bt = new JButton(Kisekae.getCaptions().getString("CloseMessage")) ;
      bt.addActionListener(this) ;
      bt.setAlignmentY(0.5f) ;
      bt.setMargin(new Insets(2,6,2,6)) ;
      tb.add(bt) ;

      // Create the run state indicator.

      RUNSTATE = new JButton() ;
		RUNSTATE.setMargin(new Insets(1,1,1,1)) ;
      RUNSTATE.setRequestFocusEnabled(false) ;
		RUNSTATE.setAlignmentY(0.5f) ;
		iconfile = Kisekae.getResource("Images/greenball.gif") ;
		if (iconfile != null) RUNSTATE.setIcon(new ImageIcon(iconfile)) ;
		iconfile = Kisekae.getResource("Images/redball.gif") ;
		if (iconfile != null) RUNSTATE.setDisabledIcon(new ImageIcon(iconfile)) ;
		tb.add(Box.createGlue()) ;
      tb.add(RUNSTATE,null) ;
      getContentPane().add(tb,BorderLayout.NORTH) ;

      // Establish the initial print page layout.

      preview = new PreviewContainer() ;
      newcomponents = new Vector() ;
      pageFormat = getPageFormat() ;
      pageFormat.setOrientation(orientation);
      wPage = (int) (pageFormat.getWidth()) ;
      hPage = (int) (pageFormat.getHeight()) ;
      if (wPage == 0 || hPage == 0)
      {
         PrintLn.printErr("PrintPreview: Unable to determine default page size") ;
         return;
      }

      // Establish the initial page preview scaling factor.

      wait = new JButton(Kisekae.getCaptions().getString("PreviewLoadingText")) ;
      JScrollPane ps = new JScrollPane(preview) ;
      int vspolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) vspolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ;
      int hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ;
      if (OptionsDialog.getAppleMac()) hspolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS ;
      ps.setVerticalScrollBarPolicy(vspolicy) ;
      ps.setHorizontalScrollBarPolicy(hspolicy) ;			
      getContentPane().add(ps,BorderLayout.CENTER) ;
      setDefaultCloseOperation(DISPOSE_ON_CLOSE) ;
      setVisible(true) ;
		RUNSTATE.setBorder(BorderFactory.createEmptyBorder(0,5,0,5)) ;

      // Initiate the load thread.   The component update must be run under
      // the event dispatching thread to be thread safe.

      cbScale.setSelectedIndex(0);
		addWindowListener(this) ;
   }


   // Action Listener interface for all print preview controls.

   public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource() ;
      String command = evt.getActionCommand() ;
      if (command == null) return ;

		// A Print request starts printing.

		if (command.equals(Kisekae.getCaptions().getString("PrintMessage")))
		{
			try
			{
				// Use default printer, with dialog.

				PrinterJob prnJob = PrinterJob.getPrinterJob() ;
            PageFormat pageformat = getPageFormat();
            pageformat.setOrientation(orientation);
				prnJob.setPrintable(target,pageformat) ;
            if (!prnJob.printDialog()) return ;
				Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
				prnJob.print();
				Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
			}

         // Watch for printer errors.

			catch (PrinterException ex)
			{
				PrintLn.printErr("Printing error: " + ex.toString()) ;
				ex.printStackTrace() ;
            JOptionPane.showMessageDialog(this,
               Kisekae.getCaptions().getString("PrinterError") + " - " +
               Kisekae.getCaptions().getString("PrintingTerminated")
               + "\n" + ex.toString(),
               Kisekae.getCaptions().getString("PrinterError"),
               JOptionPane.ERROR_MESSAGE) ;
			}

	      // Watch for memory faults.  If we run low on memory invoke
	      // the garbage collector and wait for it to run.

			catch (OutOfMemoryError e)
			{
				Runtime.getRuntime().gc() ;
	         try { Thread.currentThread().sleep(300) ; }
	         catch (InterruptedException ex) { }
				PrintLn.println("PrintPreview: Out of memory.") ;
            JOptionPane.showMessageDialog(this,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
               JOptionPane.ERROR_MESSAGE) ;
			}

         return ;
      }

      // A Close request terminates the print preview frame.

      if (command.equals(Kisekae.getCaptions().getString("CloseMessage")))
		{
         if (loader != null) loader.stopload() ;
         flush() ;
         dispose() ;
         return ;
      }

		// A Page Setup request establishes the print control page format.
      // We reload the preview components to reflect any page format changes.

      if (command.equals(Kisekae.getCaptions().getString("PageSetupMessage")))
		{
         int initorientation = orientation ;
         int initwidth = wPage ;
         int initheight = hPage ;
	      PrinterJob pj = PrinterJob.getPrinterJob() ;
	      pageFormat = pj.pageDialog(getPageFormat()) ;
         orientation = pageFormat.getOrientation() ;
	      wPage = (int) (pageFormat.getWidth()) ;
	      hPage = (int) (pageFormat.getHeight()) ;
	      w = (int) (wPage * scale / 100) ;
	      h = (int) (hPage * scale / 100) ;

         // If changed, reload.

         if (orientation != initorientation ||
             wPage != initwidth || hPage != initheight) reload() ;
         return ;
      }

      // A scale change request redraws the preview panes.  We rescale
      // the pages in a new thread to free the AWT thread.

      if (source == cbScale)
      {
			String str = cbScale.getSelectedItem().toString();
			if (str.endsWith("%")) str = str.substring(0, str.length()-1) ;
			str = str.trim() ;
			try { scale = Integer.parseInt(str) ; }
			catch (NumberFormatException ex) { return ; }
			w = (int) (wPage * scale / 100) ;
			h = (int) (hPage * scale / 100) ;
         reload() ;
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
	public void windowClosing(WindowEvent evt)
   { flush() ; dispose() ; }


	// A utility function to return our current page format for printing.

	private PageFormat getPageFormat()
   {
      if (pageFormat == null)
      {
         PrinterJob pj = PrinterJob.getPrinterJob() ;
         pageFormat = pj.defaultPage() ;
      }
      return pageFormat ;
   }


	// A utility function to reload our preview panel.  This initiate the
   // loader thread.

	private void reload()
   {
      comps = null ;
      if (preview == null) return ;
      if (preview.getComponentCount() > 0) preview.removeAll() ;
      newcomponents.removeAllElements() ;
		wait.setPreferredSize(new Dimension(w,h));
		preview.add(wait) ;
		validate() ;
		loader = new LoadThread() ;
		loader.start() ;
   }


   // A utility function to flush all memory objects.

   void flush()
   {
      if (loader != null) loader.stopload() ;
      if (target instanceof PanelFrame) ((PanelFrame) target).printClose() ;
      int n = (preview == null) ? 0 : preview.getComponentCount() ;
      if (newcomponents != null) newcomponents.removeAllElements() ;
   	if (n > 0) preview.removeAll() ;
      me = null ;
      pp = null ;
      comps = null ;
      target = null ;
      loader = null ;
      preview = null ;
      pageFormat = null ;
      newcomponents = null ;
		Runtime.getRuntime().gc() ;
   }


   // Inner class to describe our multipage preview panel.

   class PreviewContainer extends JPanel
   {
      protected int H_GAP = 16;
      protected int V_GAP = 10;

      public Dimension getPreferredSize()
      {
         int n = getComponentCount() ;
         if (n == 0) return new Dimension(H_GAP,V_GAP) ;
         Component comp = getComponent(0) ;
         Dimension dc = comp.getPreferredSize() ;
         int w = dc.width ;
         int h = dc.height ;

         Dimension dp = getParent().getSize() ;
         int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1) ;
         int nRow = n/nCol ;
         if (nRow*nCol < n) nRow++ ;

         int ww = nCol*(w+H_GAP) + H_GAP ;
         int hh = nRow*(h+V_GAP) + V_GAP ;
         Insets ins = getInsets() ;
         return new Dimension(ww+ins.left+ins.right, hh+ins.top+ins.bottom) ;
      }

      public Dimension getMaximumSize()
      { return getPreferredSize() ; }

      public Dimension getMinimumSize()
      { return getPreferredSize() ; }

      public void doLayout()
      {
         Insets ins = getInsets() ;
         int x = ins.left + H_GAP ;
         int y = ins.top + V_GAP ;

         int n = getComponentCount() ;
         if (n == 0) return ;
         Component comp = getComponent(0) ;
         Dimension dc = comp.getPreferredSize() ;
         int w = dc.width ;
         int h = dc.height ;

         Dimension dp = getParent().getSize() ;
         int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1) ;
         int nRow = n / nCol ;
         if (nRow * nCol < n) nRow++ ;

         int index = 0 ;
         for (int k = 0; k < nRow; k++)
         {
            for (int m = 0; m < nCol; m++)
            {
               if (index >= n) return ;
               comp = getComponent(index++) ;
               comp.setBounds(x, y, w, h) ;
               x += w+H_GAP ;
            }
            y += h+V_GAP ;
            x = ins.left + H_GAP ;
         }
      }
   }


   // Inner class to describe one page in the preview panel.  Each page
   // is constructed from a full sized source image that is scaled to
   // fit the print preview scale factor.

   class PagePreview extends JPanel
      implements MouseListener, Printable
   {
      protected int w ;                   // Page panel width
      protected int h ;                   // Page panel height
      protected Image img ;               // Scaled image
      protected int pagenumber ;          // Page number
      private JDialog window = null ;     // Full page display window
      private boolean nomouse = false ;   // If true, ignore mouse events

      public PagePreview(int w, int h, Image source, int p)
      {
         this.w = w ;
         this.h = h ;
         this.pagenumber = p ;
         img = createResizedCopy(source,w,h,true) ;
         setBackground(Color.white) ;
         setBorder(new MatteBorder(1,1,2,2,Color.black)) ;
      	setDoubleBuffered(false) ;
         String s1 = Kisekae.getCaptions().getString("ToolTipPage") ;
         int i1 = s1.indexOf('[') ;
         int j1 = s1.indexOf(']') ;
         if (i1 >= 0 && j1 > i1)
            s1 = s1.substring(0,i1) + (p+1) + s1.substring(j1+1) ;
         setToolTipText(s1);
   		addMouseListener(this) ;
      }

      public Dimension getPreferredSize()
      {
         Insets ins = getInsets() ;
         return new Dimension(w+ins.left+ins.right,h+ins.top+ins.bottom) ;
      }

      public Dimension getMaximumSize()
      { return getPreferredSize() ; }

      public Dimension getMinimumSize()
      { return getPreferredSize() ; }

      public void paint(Graphics g)
      {
         g.setColor(getBackground()) ;
         g.fillRect(0,0,getWidth(),getHeight()) ;
         g.drawImage(img,0,0,this) ;
         paintBorder(g) ;
      }

      public void setIgnoreMouse(boolean b) { nomouse = b ; }

      // Mouse listener events.

      public void mousePressed(MouseEvent e) { }
      public void mouseEntered(MouseEvent e)
      { if (!nomouse) Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) ; }
      public void mouseExited(MouseEvent e)
      { Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ; }
      public void mouseClicked(MouseEvent e) { }
      public void mouseReleased(MouseEvent e)
      {
         if (nomouse) return ;
			if (!SwingUtilities.isLeftMouseButton(e)) return ;
         if (e.getClickCount() > 1) return ;
         if (target == null) return ;
         if (pageFormat == null) return ;
         MainFrame mf = Kisekae.getMainFrame() ;
         if (mf == null) return ;
			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;

         // Render this page at full size.

         try
         {
            Image img = new BufferedImage(wPage,hPage,BufferedImage.TYPE_INT_RGB) ;
            Graphics g = img.getGraphics() ;
            g.setColor(Color.white) ;
            g.fillRect(0,0,wPage,hPage) ;
            int exists = target.print(g,pageFormat,pagenumber) ;
            g.dispose() ;
            if (exists == Printable.NO_SUCH_PAGE) return ;

            // Construct the dialog window.

            Dimension d = mf.getSize() ;
            int w = (int) pageFormat.getWidth() ;
            int h = (int) pageFormat.getHeight() ;
            if (d.height < h) w += 40 ;
            w = Math.min(w,d.width) ;
            h = Math.min(h,d.height) ;
            final PagePreview p = new PagePreview(wPage,hPage,img,pagenumber) ;
            p.setIgnoreMouse(true) ;

            // Add a Print Page button.

            JButton printpage = new JButton(Kisekae.getCaptions().getString("PrintPageMessage")) ;
            ActionListener printpagelistener = new ActionListener()
			   {
					public void actionPerformed(ActionEvent e)
               {
         			try
         			{
         				// Use default printer, without dialog.

         				PrinterJob prnJob = PrinterJob.getPrinterJob() ;
         				prnJob.setPrintable(p,pageFormat) ;
         				Kisekae.setCursor(window,Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
         				prnJob.print();
         			}

                  // Watch for printer errors.

         			catch (Exception ex)
         			{
         				PrintLn.printErr("Printing error: " + ex.toString()) ;
                      JOptionPane.showMessageDialog(me,
                         Kisekae.getCaptions().getString("PrinterError") + " - " +
                         Kisekae.getCaptions().getString("PrintingTerminated")
                         + "\n" + ex.toString(),
                         Kisekae.getCaptions().getString("PrinterError"),
                         JOptionPane.ERROR_MESSAGE) ;
         			}
                  finally
                  {
         				Kisekae.setCursor(window,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
                  }
               }
            } ;

            // Add a Close button.

            JButton closepage = new JButton(Kisekae.getCaptions().getString("CloseMessage")) ;
            ActionListener closepagelistener = new ActionListener()
			   {
					public void actionPerformed(ActionEvent e)
               {
                  if (target != null)
                  {
                     try { target.print(null,null,-1) ; }
                     catch (PrinterException ex) { }
                  }
                  if (window != null) window.dispose() ;
               }
            } ;

            // Create the single page preview.

            closepage.addActionListener(closepagelistener) ;
            printpage.addActionListener(printpagelistener) ;
            JPanel header = new JPanel() ;
            header.add(printpage) ;
            header.add(closepage) ;
            JPanel pagepanel = new JPanel() ;
            pagepanel.setLayout(new BorderLayout()) ;
            pagepanel.add(header,BorderLayout.NORTH) ;
            pagepanel.add(p,BorderLayout.CENTER) ;
            JScrollPane jsp = new JScrollPane(pagepanel) ;
            String s1 = Kisekae.getCaptions().getString("PreviewPageTitle") ;
            int i1 = s1.indexOf('[') ;
            int j1 = s1.indexOf(']') ;
            if (i1 >= 0 && j1 > i1)
               s1 = s1.substring(0,i1) + (pagenumber+1) + s1.substring(j1+1) ;
            window = new JDialog(me,s1) ;
            window.getContentPane().add(jsp) ;
            window.setSize(w,h) ;
            window.setVisible(true) ;
         }
         catch (Exception ex) { }
			Kisekae.setCursor(this,Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
      }


   	// Print interface method.  The image scaled to fit on the page
   	// and centered if required.  Note that we return PAGE_EXISTS if
      // we have a page but it is not visible and rendered.

   	public int print(Graphics g, PageFormat pageformat, int pageindex)
   	{
         if (img == null) return Printable.NO_SUCH_PAGE ;
   		if (pageindex != 0) return Printable.NO_SUCH_PAGE ;

         // Print the page image.

   		int dw = (int) (pageformat.getImageableWidth()) ;
   		int dh = (int) (pageformat.getImageableHeight()) ;
   		int dsw = img.getWidth(null) ;
   		int dsh = img.getHeight(null) ;

   		// Center the image on the page.

   		int dx = (dw - dsw) / 2 + (int) pageformat.getImageableX() ;
   		int dy = (dh - dsh) / 2 + (int) pageformat.getImageableY() ;
   		g.drawImage(img,dx,dy,dx+dsw,dy+dsh,0,0,dsw,dsh,null) ;
   		return Printable.PAGE_EXISTS ;
   	}

      
      // Create a BufferedImage of the desired size and draw the original 
      // image into it, scaling on the fly. Note that depending on whether 
      // your original image is opaque or non-opaque (that is, if it's 
      // translucent or transparent), you may need to create an image with 
      // an alpha channel. Image.getScaledInstance() is not recommended any 
      // more. The preferred approach is to create a new Image and paint to it, 
      // scaling on the fly, to take advantage of faster loops.

      BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha)
      {
          int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
          BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
          Graphics2D g = scaledBI.createGraphics();
          if (preserveAlpha) 
          {
              g.setComposite(AlphaComposite.Src);
          }
          g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
          g.dispose();
          return scaledBI;
      }
   }


   // Inner class to create a thread to load the preview panels.

   class LoadThread extends Thread
   {
      private boolean stop = false ;

      void stopload() { stop = true ; }

		public void run()
		{
	      int pageIndex = 0 ;
         Graphics g = null ;
         BufferedImage img = null ;
        	setPriority(Thread.MIN_PRIORITY) ;
         RUNSTATE.setEnabled(false) ;

	      try
	      {
            img = new BufferedImage(wPage,hPage,BufferedImage.TYPE_INT_RGB) ;
            g = img.getGraphics() ;

	         while (true)
	         {
               if (stop) break ;
	            g.setColor(Color.white) ;
	            g.fillRect(0,0,wPage,hPage) ;
               int exists = target.print(g,pageFormat,pageIndex) ;
	            if (exists == NO_SUCH_PAGE) { g.dispose() ; break ; }
               if (exists != PAGE_EXISTS) { g.dispose() ; continue ; }
	            pp = new PagePreview(w,h,img,pageIndex) ;
               if (newcomponents != null) newcomponents.add(pp) ;

               // Do the component update under the AWT thread.  We do not
               // know when this update will be performed so we select the
               // first PagePreview object from the component list.

               Runnable awt = new Runnable()
               {
                 	public void run()
                  {
                   	try
                     {
                        if (preview == null) return ;
                     	if (newcomponents == null || newcomponents.size() == 0) return ;
                     	PagePreview pp = (PagePreview) newcomponents.firstElement() ;
                        newcomponents.removeElementAt(0) ;
		                  preview.remove(wait);
				            preview.add(pp) ;
		                  preview.add(wait) ;
								preview.doLayout() ;
		                 	preview.getParent().getParent().validate() ;
                     }

					      // Watch for memory faults.  If we run low on memory invoke
					      // the garbage collector and wait for it to run.

							catch (OutOfMemoryError e)
							{
                     	flush() ;
								Runtime.getRuntime().gc() ;
					         try { Thread.currentThread().sleep(300) ; }
					         catch (InterruptedException ex) { }
								PrintLn.println("PrintPreview: Out of memory.") ;
                        JOptionPane.showMessageDialog(me,
                           Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
                           Kisekae.getCaptions().getString("ActionNotCompleted"),
                           Kisekae.getCaptions().getString("LowMemoryFault"),
                           JOptionPane.ERROR_MESSAGE) ;
							}
                  }
               } ;

               // Update the preview window.

	            pageIndex++ ;
					javax.swing.SwingUtilities.invokeLater(awt);
               Thread.currentThread().sleep(50) ;
	         }

            // Remove the last posted wait component.  Get the component
            // list so that rescaling operations can identify the proper
            // PagePreview objects to scale.

            Runnable awt = new Runnable()
            {
              	public void run()
               {
                 	try
                  {
                     if (preview == null) return ;
	                  preview.remove(wait);
							preview.doLayout() ;
	                 	preview.getParent().getParent().validate() ;
	                 	preview.getParent().getParent().repaint() ;
							comps = preview.getComponents() ;
                     RUNSTATE.setEnabled(true) ;
                  }

				      // Watch for memory faults.  If we run low on memory invoke
				      // the garbage collector and wait for it to run.

						catch (OutOfMemoryError e)
						{
                  	flush() ;
							Runtime.getRuntime().gc() ;
				         try { Thread.currentThread().sleep(300) ; }
				         catch (InterruptedException ex) { }
							PrintLn.println("PrintPreview: Out of memory.") ;
                     JOptionPane.showMessageDialog(me,
                        Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
                        Kisekae.getCaptions().getString("ActionNotCompleted"),
                        Kisekae.getCaptions().getString("LowMemoryFault"),
                        JOptionPane.ERROR_MESSAGE) ;
						}
               }
            } ;
				javax.swing.SwingUtilities.invokeLater(awt);
	      }

         catch (InterruptedException e) { }

         // Watch for printer errors.

	      catch (PrinterException e)
	      {
	         PrintLn.printErr("Printing error: " + e.toString()) ;
	         e.printStackTrace() ;
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("PrinterError") + " - " +
               Kisekae.getCaptions().getString("PrintingTerminated")
               + "\n" + e.toString(),
               Kisekae.getCaptions().getString("PrinterError"),
               JOptionPane.ERROR_MESSAGE) ;
	      }

	      // Watch for memory faults.  If we run low on memory invoke
	      // the garbage collector and wait for it to run.

			catch (OutOfMemoryError e)
			{
         	flush() ;
				Runtime.getRuntime().gc() ;
	         try { Thread.currentThread().sleep(300) ; }
	         catch (InterruptedException ex) { }
				PrintLn.println("PrintPreview: Out of memory.") ;
            JOptionPane.showMessageDialog(me,
               Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
               Kisekae.getCaptions().getString("ActionNotCompleted"),
               Kisekae.getCaptions().getString("LowMemoryFault"),
               JOptionPane.ERROR_MESSAGE) ;
			}

         // Clean up.

         finally
         {
            if (g != null) g.dispose() ;
            img = null ;
            g = null ;
         }
		}
   }
}
