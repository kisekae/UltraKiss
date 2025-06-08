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



import java.io.*;
import java.util.Date ;
import javax.swing.JOptionPane ;


final public class LogFile extends PrintStream 
{
	private static String logfilename = "UltraKiss.log" ;
	private static String origlogfilename = "UltraKiss.log" ;
   private static boolean tracewrite = false ;
   private static int byteswritten = 0 ;
   private static boolean error = false ;
   private static int maxbytes = 10000000 ;     // 10 MB max file size

	static OutputStream logfile ;
	static PrintStream oldStdout ;
	static PrintStream oldStderr ;

	// Constructor
	
	public LogFile(PrintStream ps) 
	{	
      super(ps) ; 
   }


   // Logfile open state.

   public static boolean isOpen() { return (logfile != null) ; }

	
	// Start copying stdout and stderr to a file.
	// Create and open the logfile.


	public static void start()
	{
		try
		{
			oldStdout = System.out ;
			oldStderr = System.err ;
         
         // Set the logfile in the temp directory if the default fails.  This is 
         // to bypass permission settings for writes to the webstart directory.
         
         try
         {
            if (Kisekae.isWebswing()) throw new Exception("Webswing Log File") ;
            File f = new File(logfilename) ;
            logfile = new PrintStream(new BufferedOutputStream(new FileOutputStream(logfilename)));
            logfilename = f.getAbsolutePath() ;
         }
         catch (Exception ex)
         {
            int i = logfilename.indexOf('.') ;
            String prefix = logfilename.substring(0,i) ;
            String suffix = logfilename.substring(i) ;
            File f = File.createTempFile(prefix,suffix) ;
            logfilename = f.getAbsolutePath() ;
            logfile = new PrintStream(new BufferedOutputStream(new FileOutputStream(logfilename)));
//          f.deleteOnExit() ;
         }

			// Time and date stamp this file.

			Date today = new Date() ;
			String ls = System.getProperty("line.separator") ;
			String s = "Kisekae UltraKiss log file created on " + today.toString() ;
			logfile.write(s.getBytes()) ;
			if (ls != null) logfile.write(ls.getBytes()) ;
			s = "Host Operating System " + System.getProperty("os.name") ;
			logfile.write(s.getBytes()) ;
			if (ls != null) logfile.write(ls.getBytes()) ;
			s = "Run time Java Virtual Machine level " + System.getProperty("java.version") ;
         s += (is64Bit()) ? " (64-bit)" : " (32-bit)" ;
			logfile.write(s.getBytes()) ;
			if (ls != null) logfile.write(ls.getBytes()) ;       
         Runtime rt = Runtime.getRuntime();
         long maxMem = rt.maxMemory();
         s = "Maximum Java Memory: " + maxMem + " (" + maxMem/(1024*1024) + " MB)" ;
			logfile.write(s.getBytes()) ;
			if (ls != null) logfile.write(ls.getBytes()) ;         
         if (Kisekae.isWebswing()) 
         {
            s = "Webswing option is set." ;
   			logfile.write(s.getBytes()) ;
   			if (ls != null) logfile.write(ls.getBytes()) ;    
         }

			// Start redirecting the output.  If we are running in webswing 
         // redirection does not seem to work.  The log file is not written.

			System.setOut(new LogFile(System.out)) ;
			System.setErr(new LogFile(System.err)) ;
		}

		// Watch for exceptions.  The Security Manager may throw an exception.

		catch (SecurityException e)
		{
	      PrintLn.println("LogFile: Security exception opening logfile " + logfilename) ;
         PrintLn.println(e.getMessage()) ;
			try { if (logfile != null) logfile.close() ; }
         catch (Exception ex) { }
			logfile = null ;
		}

		catch (Exception e)
		{
	      PrintLn.println("LogFile: Exception opening logfile " + logfilename) ;
         PrintLn.println(e.getMessage()) ;
			try { if (logfile != null) logfile.close() ; }
         catch (IOException ex) { }
			logfile = null ;
		}
	}
   
   // Determine if JVM is 64 bit
   
   public static final boolean is64Bit() 
   {
      String model = System.getProperty("sun.arch.data.model",
                                      System.getProperty("com.ibm.vm.bitmode"));
      if (model != null) return "64".equals(model);
      
      String ARCH = System.getProperty("os.arch") ;
              
      if ("x86-64".equals(ARCH)
        || "ia64".equals(ARCH)
        || "ppc64".equals(ARCH) || "ppc64le".equals(ARCH)
        || "sparcv9".equals(ARCH)
        || "mips64".equals(ARCH) || "mips64el".equals(ARCH)
        || "amd64".equals(ARCH)
        || "aarch64".equals(ARCH)) 
        return true;
//      return Native.POINTER_SIZE == 8;
      return false ;
   }


	
	// Flush the standard output stream so that all data is written to the file.

	public static void flushcontents() throws IOException
	{
		if (logfile != null) logfile.flush() ;
	}


   // Clear our file contents.

   static void clearcontents()
   { stop() ; byteswritten = 0 ; start() ; }


	// Return our log file name.

	static String getLogFileName() { return logfilename ; }
	static String getOrigLogFileName() { return origlogfilename ; }


	// Return our log file.

	static OutputStream getLogFile() { return logfile ; }


	// Set our trace indicator.

	static void setTraceWrite(boolean b) { tracewrite = b ; }


	// Restores the original settings.
	
	public static void stop() 
	{
		if (logfile == null) return ;
		try
		{
			Date today = new Date() ;
			PrintLn.println("Kisekae UltraKiss log file closed on " + today.toString()) ;
			System.setOut(oldStdout) ;
			System.setErr(oldStderr) ;
			try { logfile.close() ; }
         catch (SecurityException e) { }
		}
		catch (Exception e)
		{
			PrintLn.println("LogFile: Exception closing log file " + logfilename) ;
			e.printStackTrace() ;
		}
      finally
      {
         logfile = null ;
      }
	}


	// Delete the log file.
	
	public static void delete() 
	{
		if (logfile == null) return ;
      try
      {
         stop() ;
         File f = new File(logfilename) ;
         f.delete() ;
         byteswritten = 0 ;
      }

		// Watch for exceptions.  The Security Manager may throw an exception.

		catch (SecurityException e)
		{
			PrintLn.println("LogFile: Security exception deleting logfile " + logfilename) ;
			PrintLn.println(e.getMessage()) ;
		}
      
      catch (Exception e)
		{
			PrintLn.println("LogFile: Exception deleting log file " + logfilename) ;
			e.printStackTrace() ;
		}
   }

	
	// PrintStream override.
	
	public void write(int b) 
	{
		if (logfile == null) return ;
      
		try
		{
         if (byteswritten <= maxbytes)       
         {
   			logfile.write(b) ;
            if (!Kisekae.inApplet()) super.write(b);
            byteswritten++ ;
         }
         else
         {
   			if (!error) 
            {
               String s = "\nLogFile: Exceed maximum log file size " + logfilename ;
               logfile.write(s.getBytes()) ;
            }
            error = true ;
         }         
		}
		catch (Exception e)
		{
         stop() ;
			PrintLn.println("LogFile: Exception writing log file " + logfilename) ;
			e.printStackTrace() ;
  			setError();
		}

      // Write to trace dialog if visible.

      if (!tracewrite) return ;
      MainFrame mf = Kisekae.getMainFrame() ;
      FKissTrace trace = (mf != null) ? mf.getTrace() : null ;
      if (trace == null) return ;
      byte [] b1 = new byte [1] ;
      b1[0] = (byte) (b & 0xff) ;
      String s = new String(b1) ;
      trace.write(s) ; 
	}
	
	
    // PrintStream override.
	
    public void write(byte buf[], int off, int len) 
	 {
      // Write to trace dialog if visible. Note, event, action, and variable 
      // traces set as 'nobreakpoint' in the FKiSS editor have a sentinal '*'
      // inserted in the text string. These trace lines are not forwarded to
      // the trace dialog although they will appear in the log file. A text
      // line with a printable character in it must be processed to begin
      // tracing.

      MainFrame mf = Kisekae.getMainFrame() ;
      FKissTrace trace = (mf != null) ? mf.getTrace() : null ;
      if (trace != null) 
      {
         String s = new String(buf,off,len) ;
         for (int i = 0 ; i < s.length() ; i++)
            if (!Character.isISOControl(s.charAt(i))) 
               { tracewrite = true ; break ; }
         if (s.indexOf("*[") >= 0) tracewrite = false ; 
         if (s.indexOf(">*") >= 0) tracewrite = false ;
         if (tracewrite) trace.write(s) ; 
      }
      
      // Write to log file.  Note, to minimize debug output we do not write 
      // FKiSS disabled traces unless the appropriate debug option is set.
      
		if (logfile == null) return ;
      if (trace != null && !tracewrite && !OptionsDialog.getDebugDisabled()) return ;
      
		try
		{
         if (byteswritten <= maxbytes)       
         {
            logfile.write(buf, off, len) ;
            if (!Kisekae.inApplet()) super.write(buf, off, len) ;
            byteswritten += len ;
         }
         else
         {
   			if (!error) 
            {
               String s = "\nLogFile: Exceed maximum log file size " + logfilename ;
               logfile.write(s.getBytes()) ;
            }
            error = true ;
         }         
		}
		catch (Exception e)
		{
         stop() ;
			PrintLn.println("LogFile: Exception writing log file " + logfilename) ;
			e.printStackTrace() ;
  			setError();
		}
	}
}
