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
* KissObject class
*
* Purpose:
*
* This class is an abstract class for all KiSS objects.  It defines
* common methods and attributes for use by every KiSS object.
*
* KiSS objects such as cels, groups, palettes and page sets extend
* this class.  Each object must provide specific implementations for
* the common methods.
*
* Each KissObject has an event table indexed by the event name.
* Given an object, we can find an event associated with this object.
* Accessing the table by name returns a Vector of all events with
* the identified name associated with this object.
*
*/


import java.awt.* ;
import java.util.Vector ;
import java.util.Hashtable ;
import java.util.Enumeration ;
import java.util.Collection ;
import java.util.Collections ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.File ;
import java.net.URL ;
import java.net.MalformedURLException ;


abstract class KissObject
	implements Comparable, Cloneable
{
	static KissFrame loader = null ;			// The parent frame for messages

	// Object attributes

	private Object identifier = null ;		// The object identifier
   private Object clone = null ;		      // The original clone source
	private Hashtable evtkey = null ;		// Event locator index
	private int eventcount = 0 ;     		// Event count for this object
	private boolean actionEvent = false ;	// True if event runs on user action
	private boolean internal = false ;		// True if object generated internal
	private boolean pasted = false ;       // True if object was edit pasted
	private boolean exported = false ;     // True if object was exported
	private Boolean updated = null ;			// True if object was updated

	// Attributes inherited by objects that are KissObjects

	protected Object cid = null ;				// The object configuration id
	protected Object uniqueid = null ;		// The object unique identifier
	protected Configuration ref = null ;	// The configuration reference
	protected ArchiveFile zip = null ;		// The archive for the parent file
	protected ArchiveEntry ze = null ;		// The archive entry for the object
	protected String file = null ;			// The name of the parent file
	protected String relname = null ;		// The relative name of the object
	protected String extension = null ;		// The file extension
	protected String comment = null ;		// The full object comment text
	protected Vector leadcomment = null ;	// The list of leading comment text
	protected Vector trailcomment = null ;	// The list of trailing comment text
	protected String basecomment = null ;	// The comment excluding % controls
	protected String encoding = null ;		// The object encoding method
   protected String errormessage = null ;	// The error message
	protected long updatetime = 0 ;        // The time/date of last update
	protected boolean error = false ;		// True, the object is in error
	protected int line = 0 ;               // The CNF line number of this object

	// Inherited attributes for the object parent/child relation

	protected KissObject parent = null ; 	// The parent object
	protected Vector children = null ;		// Any child objects
	protected boolean glued = false ;		// True if no detach on move
   protected Point restrictx = null ;     // The movement min, max x
   protected Point restricty = null ;     // The movement min, max y
   private Point initrestrictx = null ;   // The original movement min, max x
   private Point initrestricty = null ;   // The original movement min, max y


	// Constructor

	public KissObject()
	{
		evtkey = new Hashtable(100,0.8f) ;	// Plan on 80 events per object
      updated = new Boolean(false) ;		// Object for cloned copies
      leadcomment = new Vector() ;			// Leading object comments
      trailcomment = new Vector() ;			// Trailing object comments
	}


	// Methods
	// -------

	// Set the object identity.

	void setIdentifier(Object id) { identifier = id ; }

	// Method to set the reference to our parent zip file object.

	void setZipFile(ArchiveFile af) { zip = af ; }

	// Method to set the reference to our zip entry object.

	void setZipEntry(ArchiveEntry ae) { ze = ae ; }

	// Method to set the relative name of our object.

	void setRelativeName(String s) { relname = s ; }


	// Set the object by key.  We can have duplicates.  If more than
	// one object is referenced by the same key value then we build a
	// list of objects and attach the list to the key.

	void setKey(Hashtable key, Object cid, Object id)
	{
		if (key == null || id == null) return ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.remove(hashkey) ;
		if (o == null)
		{
			key.put(hashkey,this) ;
			return ;
		}

		// We have a duplicate.  Create or update the duplicate list.
      // We will not add ourselves to the list if we are already attached
      // to this key.

		if (o instanceof Vector)
      {
      	Vector v = (Vector) o ;
			if (!(v.contains(this))) v.addElement(this) ;
      }
		else if (this != o)
		{
			Vector v = new Vector() ;
			v.addElement(o) ;
			v.addElement(this) ;
			o = v ;
		}

		// Store the duplicate list by key.

		key.put(hashkey,o) ;
	}


	// Remove the object key.  This will remove the key value from the
   // hashtable.  This deletes all references to the object and all
   // duplicates.

	static void removeKey(Hashtable key, Object cid, Object id)
	{
		if (key == null || id == null) return ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.remove(hashkey) ;
	}


	// Remove the object.  This will remove the object from the hashtable.
   // This deletes only this reference to the object.

	static void removeObject(Hashtable key, Object cid, Object id, Object object)
	{
		if (key == null || id == null || object == null) return ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.get(hashkey) ;
      if (o == object)
         key.remove(hashkey) ;
      else if (o instanceof Vector)
      {
         Vector v = (Vector) o ;
         if (v.contains(object)) v.removeElement(object) ;
         if (v.size() == 0) key.remove(hashkey) ;
         if (v.size() == 1) key.put(hashkey,v.elementAt(0)) ;
      }
	}


	// Return the next object by key.  The first object will have
	// been referenced through a static getByKey call.  Remaining
	// duplicate objects are now located using individual object
	// instances. This method returns the next object following this
	// object in the duplicate list.

	Object getNextByKey(Hashtable key, Object cid, Object id)
	{
		if (key == null || id == null) return null ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.get(hashkey) ;
		if (!(o instanceof Vector)) return null ;

		// We have a duplicate list attached to this key.  Find
		// ourselves in the list.

		int i = 0 ;
		Vector v = (Vector) o ;
		for (i = 0 ; i < v.size() ; i++)
		{
			o = v.elementAt(i) ;
			if (o == this) break ;
		}

		// Return the next element in the list, if it exists.

		o = ((i+1) < v.size()) ? v.elementAt(i+1) : null ;
		return o ;
	}


	// Add a new event to this object.  The event object will not be added
   // if it already exists in the object's event list.  Events are indexed
   // by the event name and each index is a list of events.

	void addEvent(FKissEvent e)
	{
      if (e == null) return ;
      e.setParentObject(this);
      Object identifier = e.getIdentifier() ;
      if (identifier == null) return ;
		Vector events = (Vector) evtkey.get(identifier) ;
		if (events == null) events = new Vector() ;
      if (events.contains(e)) return ;
		events.addElement(e) ;
      evtkey.put(identifier,events) ;
      eventcount++ ;

		// Identify user press action event types.

		String name = (identifier instanceof String) ? (String) identifier : "" ;
		if ("press".equals(name) || "catch".equals(name) ||
			"fixcatch".equals(name) || "release".equals(name) ||
			"drop".equals(name) || "fixdrop".equals(name))
			actionEvent = true ;
	}


	// Add a set of events to this object.

	void addEvent(Vector v)
	{
		if (v == null) return ;
		for (int i = 0 ; i < v.size() ; i++)
		{
			Object o = v.elementAt(i) ;
			if (!(o instanceof FKissEvent)) continue ;
			addEvent((FKissEvent) o) ;
		}
	}


	// Add a new, named event to this object.  The event object will not be
   // added if it already exists in the object's event list as defined by
   // the event name and parameter list.

	void addNamedEvent(FKissEvent e)
	{
      if (e == null) return ;
      e.setParentObject(this);
      Object identifier = e.getIdentifier() ;
      if (identifier == null) return ;
		Vector events = (Vector) evtkey.get(identifier) ;
		if (events == null) events = new Vector() ;
      for (int i = 0 ; i < events.size() ; i++)
      {
         FKissEvent evt = (FKissEvent) events.elementAt(i) ;
         String s = evt.toString() ;
         if (s.equals(e.toString())) return ;
      }
		events.addElement(e) ;
      evtkey.put(identifier,events) ;
      eventcount++ ;

		// Identify user press action event types.

		String name = (identifier instanceof String) ? (String) identifier : "" ;
		if ("press".equals(name) || "catch".equals(name) ||
			"fixcatch".equals(name) || "release".equals(name) ||
			"drop".equals(name) || "fixdrop".equals(name))
			actionEvent = true ;
	}


	// Add a set of named events to this object.

	void addNamedEvent(Vector v)
	{
		if (v == null) return ;
		for (int i = 0 ; i < v.size() ; i++)
		{
			Object o = v.elementAt(i) ;
			if (!(o instanceof FKissEvent)) continue ;
			addNamedEvent((FKissEvent) o) ;
		}
	}


	// Remove an event from this object.  The event is identified by its
   // object reference.

	void removeEvent(FKissEvent e)
	{
      if (e == null) return ;
      Object identifier = e.getIdentifier() ;
      if (identifier == null) return ;
		Vector events = (Vector) evtkey.get(identifier) ;
		if (events == null) return ;
		events.removeElement(e) ;
		if (events.size() == 0) evtkey.remove(identifier) ;
      e.setParentObject(null);
      eventcount-- ;
	}


   // Add leading text.

   void addLeadComment(Collection c) { if (c != null) leadcomment.addAll(c) ; }
   Vector getLeadComment() { return leadcomment ; }

   // Add trailing text.

   void addTrailComment(String s) { if (s != null) trailcomment.addElement(s) ; }
   void addTrailComment(Collection c) { if (c != null) trailcomment.addAll(c) ; }

   // Return the trailing text set or the first string.

   Vector getTrailComment() { return trailcomment ; }
   String getFirstTrailComment()
   {
      if (trailcomment == null || trailcomment.size() == 0) return null ;
      return (String) trailcomment.elementAt(0) ;
   }


   // Method to write the object contents to the specified output stream.
   // This should be overridden for writable objects that inherit from this
   // class.

	abstract int write(FileWriter fw, OutputStream out, String type) throws IOException ;


   // Method to export the object contents to the specified output stream.
   // Cel files are exported as truecolor images.  Removed 2007/12/27 as
   // export can be used to convert set to cel type for other viewers and
   // existing palette cels should not be changed.  Corrected 2008/08/14
   // as non-cel images must still be converted to truecolor cel.

	int export(FileWriter fw, OutputStream out, String type) throws IOException 
   { 
      Palette p = null ;
      if (this instanceof Cel && ".cel".equalsIgnoreCase(type) && !(this instanceof KissCel))
      {
         p = ((Cel) this).getPalette() ;
         ((Cel) this).setPalette(null) ;
      }
      int n = write(fw,out,type) ; 
      if (this instanceof Cel && ".cel".equalsIgnoreCase(type) && !(this instanceof KissCel))
      {
         ((Cel) this).setPalette(p) ;
      }
      return n ;
   }


   // Methods to initialize the object.

   void load() { load(null) ; }
	void load(Vector includefiles) { } ;


   // Method to release object storage.

   void flush() 
   { 
      evtkey = null ;
      ref = null ;
      zip = null ;
      ze = null ;
      clone = null ;
      parent = null ;
      children = null ;
      trailcomment = null ;
      leadcomment = null ;
   }


   // Search for an element entry in a set of INCLUDE files.  The include
   // list will either contain File objects for unreferenced entries or
   // ArchiveFiles for referenced entries.  If an include file does not
   // exist it is removed from the include list.

   ArchiveEntry searchIncludeList(Vector v, String name)
   {
      if (v == null) return null ;
      for (int n = v.size()-1 ; n >= 0 ; n--)
      {
         Object include = v.elementAt(n) ;

         // File object.

         if (include instanceof File)
         {
            File f = (File) include ;
            if (!f.isFile())
            {
               System.out.println("INCLUDE file " + f.getPath() + " does not exist.") ;
               v.removeElementAt(n) ;
               continue ;
            }

            // Isolate the file name.

            String pathname = f.getPath() ;
            String filename = f.getName() ;
  			   int i = filename.lastIndexOf(".") ;
  			   String extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;

  			   // Construct a URL entry for the file.

            URL zipFileURL = null ;
     		   URL codebase = Kisekae.getBase() ;
            if (codebase == null) return null ;
  			   String protocol = codebase.getProtocol() ;
  			   String host = codebase.getHost() ;
  			   int port = codebase.getPort() ;
  			   try { zipFileURL = new URL(protocol,host,port,pathname) ; }
            catch (MalformedURLException e)
            {
               System.out.println("INCLUDE file " + pathname + " is an invalid name.") ;
               v.removeElementAt(n) ;
               continue ;
            }

  			   // Regretfully, we must treat each file type separately as Java's
  			   // zip file class does not appear designed for subclassing.

            try
            {
               ArchiveFile zip = null ;
    			   if (".zip".equals(extension))
             	   zip = new PkzFile(null,zipFileURL.getFile()) ;
               else if (".gzip".equals(extension))
             	   zip = new PkzFile(null,zipFileURL.getFile()) ;
     			   else if (".jar".equals(extension))
              	   zip = new PkzFile(null,zipFileURL.getFile()) ;
     			   else if (".lzh".equals(extension))
              	   zip = new LhaFile(null,zipFileURL.getFile()) ;
     	         if (zip == null) continue ;
        		   System.out.println("Open INCLUDE file " + zipFileURL.toExternalForm()) ;
               v.setElementAt(zip,n) ;
               include = zip ;
            }
            catch (IOException e)
            {
        		   System.out.println("Exception: Open INCLUDE file " + e) ;
            }
         }

         // MemFile objects were created if include files were downloaded in
         // a secure environment.

         else if (include instanceof MemFile)
         {
            try
            {
               ArchiveFile zip = null ;
               String filename = ((MemFile) include).getFileName() ;
     			   int i = filename.lastIndexOf(".") ;
     			   String extension = (i < 0) ? "" : filename.substring(i).toLowerCase() ;
    			   if (".zip".equals(extension))
             	   zip = new PkzFile(null,filename,(MemFile) include) ;
               else if (".gzip".equals(extension))
             	   zip = new PkzFile(null,filename,(MemFile) include) ;
     			   else if (".jar".equals(extension))
              	   zip = new PkzFile(null,filename,(MemFile) include) ;
     			   else if (".lzh".equals(extension))
              	   zip = new LhaFile(null,filename,(MemFile) include) ;
     	         if (zip == null) continue ;
        		   System.out.println("Open INCLUDE memory file " + name) ;
               v.setElementAt(zip,n) ;
               include = zip ;
            }
            catch (IOException e)
            {
        		   System.out.println("Exception: Open INCLUDE memory file " + e) ;
            }
         }

  		   // Archive file object.

         if (include instanceof ArchiveFile)
         {
            ArchiveEntry ze = ((ArchiveFile) include).getEntry(name,true) ;
            if (ze != null) return ze ;
         }
      }
      return null ;
   }


	// Object attribute reference methods
	// ----------------------------------

	// Return the object identifier.

	Object getIdentifier() { return identifier ; }

	// Return the object clone source.

	Object getClone() { return clone ; }

	// Return the configuration identifier.

	Object getID() { return cid ; }

	// Return the unique identifier.

	Object getUniqueID() { return uniqueid ; }

	// Method to return the object unqualified file name.

	String getName()
   {
   	if (file == null) return null ;
      File f = new File(file) ;
      return f.getName() ;
   }

	// Method to return the relative file name.  This is the name relative
   // to the configuration directory and includes any subordinate directory
   // paths.

	String getRelativeName()
   {
   	if (relname == null) return getName() ;
      return relname ;
   }

	// Method to return the name of the file as written.  Cels
   // can be encoded and written in a different format.  This 
   // method should be overridden during write if the CNF 
   // needs to reference the new format on next load.

	String getWriteName() { return getRelativeName() ; }

	// Method to return the object fully qualified file name.

	String getPath() { return (file == null) ? "" : file ; }

	// Method to return the object parent directory name.

	String getDirectory()
   {
   	if (file == null) return null ;
      return (new File(file)).getParent() ;
   }

	// Method to return the file type.

	String getExtension() { return (extension == null) ? "" : extension ; }

	// Return the object comment text.

	String getComment() { return (comment == null) ? "" : comment ; }

	// Return the base object comment text.

	String getBaseComment() { return (basecomment == null) ? "" : basecomment ; }

	// Return the object encoding.

	String getEncoding() { return (encoding == null) ? "unknown" : encoding ; }

	// Return the object error message.

	String getErrorMessage() { return (errormessage == null) ? "" : errormessage ; }

	// Method to return a reference to our parent zip file object.

	ArchiveFile getZipFile() { return zip ; }

	// Method to return a reference to our zip entry object.

	ArchiveEntry getZipEntry() { return ze ; }

	// Return the requested event. This is a list of all events by identifier.

	Vector getEvent(Object o) { return (o != null) ? (Vector) evtkey.get(o) : null ; }

	// Return an enumeration of the events.

	Enumeration getEvents() { return evtkey.elements() ; }

	// Return a collection of the events.

	Collection getEventValues() { return evtkey.values() ; }

	// Return a count of the events.

	int getEventCount() { return eventcount ; }

	// Return the number of bytes in the object.

	int getBytes() { return 0 ; }

	// Return the object transparency value.

	int getTransparency() { return 0 ; }

	// Return the object last modified time.

	long lastModified() { return updatetime ; }

	// Return the object parent object.

	KissObject getParent() { return parent ; }

	// Return the object child object.

	Vector getChildren() { return children ; }

	// Get the movement X restriction.

	Point getRestrictX() { return restrictx ; }

	// Get the movement Y restriction.

	Point getRestrictY() { return restricty ; }

	// Get the initial movement X restriction.

	Point getInitialRestrictX() { return initrestrictx ; }

	// Get the initial movement Y restriction.

	Point getInitialRestrictY() { return initrestricty ; }

   // Get a contained cel object.

   Cel getCel(int i) { return null ; }

   // Get the number of contained cels.

   int getCelCount() { return 0 ; }

	// Return the object configuration line number.

	int getLine() { return line ; }

   // Get a contained cel indicator.

   boolean containsCel(Cel c) { return false ; }

	// Get the palette identifier object for all cels in the group.

	Object getPaletteID() { return null ; }
   
   // Get the visible cel count on the specified page.

	int getVisibleCelCount(Integer page) { return 0 ; }



	// Object attribute settings methods
	// ---------------------------------

	// Set the configuration ID for this object.

	void setID(Object id) { cid = id ; }

	// Set the unique ID for this object.

	void setUniqueID(Object id) { uniqueid = id ; }

	// Method to set the parent file name.

	void setName(String name)
	{
		file = convertSeparator(name) ;
		int i = file.indexOf('.') ;
		if (i >= 0) extension = file.substring(i) ;
		if (extension != null) extension = extension.toLowerCase() ;
	}

	// Set the object comment text.

	void setComment(String s) { comment = s ; }

	// Set the object comment text.

	void setBaseComment(String s) { basecomment = s ; }

	// Set the object loader frame.

	static void setLoader(KissFrame f) { loader = f ; }

	// Set the configuration load reference.

	void setReference(Configuration c) { ref = c ; }

	// Set the object update state.  This also sets the update state of
	// the associated archive entry and the possibly outdated entry
   // in the archive file contents.

	void setUpdated(boolean b)
	{
		updated = new Boolean(b) ;
		if (ze != null) ze.setUpdated(b) ;
		if (zip != null) zip.setUpdated(ze,b) ;
	}

	// Set the object internal generation state. Internal objects are
   // created through edit cut operations if the object is removed from
   // the configuration. These objects are never written, however the
   // object must remain available for an edit undo. Internal objects
   // are flushed if the configuration is restarted.

	void setInternal(boolean b) { internal = b ; }

	// Set the object edit paste state.  This is set if the object
   // has recently been pasted. We use this to highlight pasted
   // objects by flashing the selection box.  The state should be 
   // cleared when the selection box is first drawn.

	void setPasted(boolean b) { pasted = b ; }

	// Set the object imported state. Imported objects are never
   // unloaded as they must remain available until they are
   // saved. Note that the import state is maintained in the
   // archive entry and is not an attribute of this KiSS object.

	void setImported(boolean b) { if (ze != null) ze.setImported(b) ; }

	// Set the object exported state. This indicates that the object 
   // has been exported but ensures that it is not unloaded until the 
   // set is restarted. On restart, the object will be loaded as a
   // standard cel.

	void setExported(boolean b) { exported = b ; }

	// Set the object cloned state. Cloned objects are created
   // through the FKiSS clone() command. These objects are not
   // written when the configuration is saved.

	void setClone(Object o) { clone = o ; }

	// Set the object last modified time.

	void setLastModified(long t) { updatetime = t ; }

	// Set the attachment glue setting.

	void setGlue(boolean b) { glued = b ; }

	// Set the parent object.

	void setParent(KissObject o) { parent = o ; }

	// Set the movement X restriction.

	void setRestrictX(Point p)
   {
      if (p == null || p.x > p.y) restrictx = null ; else restrictx = p ;
      initrestrictx = (restrictx == null) ? null : new Point(restrictx) ;
      if (hasChildren()) updateMoveRestrictions(getChildren()) ;
      if (parent != null) parent.updateMoveRestrictions(parent.getChildren()) ;
   }

	// Set the movement Y restriction.

	void setRestrictY(Point p)
   {
      if (p == null || p.x > p.y) restricty = null ; else restricty = p ;
      initrestricty = (restricty == null) ? null : new Point(restricty) ;
      if (hasChildren()) updateMoveRestrictions(getChildren()) ;
      if (parent != null) parent.updateMoveRestrictions(parent.getChildren()) ;
   }



	// Object status reference indicators
	// ----------------------------------

	// Return the object action event indicator.

	boolean hasActionEvent() { return actionEvent ; }

	// Return the object error indicator.

	boolean isError() { return error ; }

	// Return the object update state.

	boolean isUpdated() { return (ze != null) ? ze.isUpdated() : updated.booleanValue() ; }

	// Return the object recently pasted signal.

	boolean isPasted() {return pasted ; }

	// Return true if the object is internally generated.

	boolean isInternal() { return internal ; }

	// Return true if the object is attached.

	boolean isAttached() { return (hasParent() || hasChildren()) ; }

	// Return true if the object is cloned.

	boolean isCloned() { return (clone != null) ; }

	// Return a default writable indicator.

	boolean isWritable() { return false ; }

	// Return the attachment glue indicator.

	boolean isGlued() { return glued ; }

	// Return an indicator if this object has a parent object.

	boolean hasParent() { return (parent != null) ; }

	// Return an indicator if this object has child objects

 	boolean hasChildren() { return (children != null && children.size() > 0) ; }

	// Return an indicator if this object was imported

 	boolean isImported() { return (ze != null && ze.isImported()) ; }

	// Return an indicator if this object is being exported

 	boolean isExported() { return exported ; }
   
 	// Return the configuration load reference.

	Configuration getReference() { return ref ; }
  
   // Return an indicator if the object contains a component.
   
   boolean isComponent()
   {
      for (int i = 0 ; i < getCelCount() ; i++)
         if (getCel(i) instanceof JavaCel) return true ;
      return false ;
   }



	// Class methods
	// -------------

	// Return the object by key.

	static Object getByKey(Hashtable key, Object cid, Object id)
	{
		if (key == null || id == null) return null ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.get(hashkey) ;

      // Check for alternate file separator if no find.

      if (o == null)
      {
         if (hashkey.indexOf('/') >= 0)
         {
            hashkey.replace('/','\\') ;
            o = key.get(hashkey) ;
         }
         else if (hashkey.indexOf('\\') >= 0)
         {  
            hashkey.replace('\\','/') ;
            o = key.get(hashkey) ;
         }  
      }
		if (!(o instanceof Vector)) return o ;

		// We have a duplicate list attached to this key.  In general everything
      // should be of the same type, but if we were importing alternate image 
      // cels we could have an imported cel on a duplicate list along with a 
      // reference to a previously cut cel.  In this case we do not want to 
      // return internal objects on the list.

		Vector v = (Vector) o ;
		if (v.size() == 0) return null ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         o = v.elementAt(i) ;
         if (!(o instanceof KissObject)) continue ;
         if (((KissObject) o).isInternal()) continue ;
         return o ;
      }
		return null ;
	}


	// Return if the object has duplicate keys.  Note that internal
   // objects are not considered duplicated.

	static boolean hasDuplicateKey(Hashtable key, Object cid, Object id)
	{
		if (key == null || id == null) return false ;
      if (cid == null) cid = new String("Unknown") ;
		String hashkey = cid.toString() + " " + id.toString() ;
		Object o = key.get(hashkey) ;
		if (!(o instanceof Vector)) return false ;

      // Check the duplicate list for internal elements.

      int n = 0 ;
      Vector v = (Vector) o ;
      boolean duplicated = false ;
      for (int i = 0 ; i < v.size() ; i++)
      {
         o = v.elementAt(i) ;
         if (!(o instanceof KissObject)) continue ;
         if (((KissObject) o).isInternal()) continue ;
         if (n > 0) return true ;
         n++ ;
      }
      return false ;
   }


	// Method to convert signed bytes to an integer number.

	static int fixByte(byte b1, byte b2)
	{
		int n1 = (b1 << 8) ;
		int n2 = (b2 < 0) ? 256 + b2 : b2 ;
		return n1 + n2 ;
	}


   // A function to convert file separator characters.

   static String convertSeparator(String s)
   {
      if (s == null) return null ;
      s = s.replace('/',File.separatorChar) ;
      s = s.replace('\\',File.separatorChar) ;
      return s ;
   }



	// Common methods for graphic objects
	// ----------------------------------

	// Return an indication if the object is visible.

	boolean isVisible() { return false ; }

	// Return an indication if the object is on the specified page.

	boolean isOnPage(Integer p) { return false ; }

	// Return the object bounding box.  This is the area that needs to
   // be drawn when the object is repainted.

	Rectangle getBoundingBox() { return new Rectangle() ; }

	// Return the visible object bounding box.  This is the area that needs to
   // be checked for boundardy conditions.

	Rectangle getVisibleBoundingBox(Integer page) { return new Rectangle() ; }

	// Return the object bounding box inclusive of all attached children.

	Rectangle getAttachedBoundingBox() { return new Rectangle() ; }

	// Return the base object bounding box.  This is the bounding box but
   // excludes any moving placement offset.

	Rectangle getBaseBoundingBox() { return new Rectangle() ; }

	// Return the consolidated object bounding box for all ambuguous cels.

	Rectangle getAllBoundingBox() { return getBoundingBox() ; }

	// Return the base object offset.  This is the original offset established
   // before any offset changes have occured.

	Point getBaseOffset() { return new Point(0,0) ; }

	// Return the object location.  This point does not include the
   // object offset.

	Point getLocation() { return new Point() ; }

   // Return the initial object location on the specified page.

	Point getInitialLocation(PageSet page) { return new Point() ; }

	// Return the location bounding box.  This is a rectangle positioned
   // at the object location that will include the drawing bounding box.
   // It is equivalent to the drawing bounding box for objects with a (0,0)
   // offset.

	Rectangle getLocationBox()
	{
   	Rectangle box = getBoundingBox() ;
      Point location = getLocation() ;
      return box.union(new Rectangle(location)) ;
	}

	// Return the object drawing level.

	Integer getLevel() { return new Integer(0) ; }

	// Return the object offset.

	Point getOffset() { return new Point() ; }
 
   // Animation enabled specific methods.

   boolean getAnimate() { return false ; }
 
   // Animation frame delay.

   int getInterval() { return 0 ; }
 
   // Animation frame time consumed.

   long getTime() { return 0 ; }

   void setTime(long t) { }

	// Return the object image frame.

	int getFrame() { return 0 ; }
	int getFrame(Cel c) { return 0 ; }

   // Return the number of frames in the cel.  For ambiguous cels this
   // is the number of duplicate cels.

   int getFrameCount() { return 0 ; }

   // Set the next frame image.  For ambiguous cels this maps the next
   // cel and unmaps all others.  We cycle from frame 0.

   synchronized void setNextFrame() 
   {
      int n = getFrame() ;
      if (n < 0) return ;
      int max = getFrameCount() ;
      n = (n < max-1) ? n + 1 : 0 ;
      setFrame(n) ;
   }

   // Return the maximum animation loop count.

   int getLoopLimit() { return 0 ; }

   // Return the actual animation loop count.

   int getLoopCount() { return 0 ; }

	// Set the object movement offset.

	void setPlacement(int x, int y) { }
	void setPlacement(int x, int y, boolean b) { }

	// Set the indicator that the movement offset was set by restrictions.
   // We can force clear this restriction, or only clear it if the object
   // no longer meets the restriction bounds.

	void setRestrictedPlacement(int x, int y) { }
	void clearRestrictedPlacement() { }

	// Obtain the indicator that drag movement offset was constrained by 
   // restrictions. If this is true standard drag movement offsets
   // cannot be set.

	boolean hasRestrictedPlacement() { return false ; } 

	// Adjust the object transparency by a relative amount.

	void changeTransparency(int t) { changeTransparency(t,false,false) ; }
	void changeTransparency(int t, boolean bounded, boolean ambiguous) { }

	// Set the object visibility.

	void setVisible(boolean b) { }
	void setVisible(boolean b, boolean all, KissObject c) { setVisible(b) ; }

	// Set the object ghost flag.

	void setGhost(boolean b) { }
	void setGhost(boolean b, boolean all, KissObject c) { setGhost(b) ; }

	// Set the object animation flag.

	void setAnimate(int n) { }
	void setAnimate(int n, boolean all, KissObject c) {setAnimate(n) ; }
   
	// Set the repeat flag for this media file.

	void setRepeat(int n) { }

	// Set the object image frame.

	void setFrame(int n) { }

	// Set the object draw level.

	void setLevel(Integer n) { }

	// Set the line number where it was declared in the CNF.

	void setLine(int n) { line = n ; }

	// Invert the object visibility.

	void altVisible() { }
	void altVisible(boolean all, KissObject c) {altVisible() ; }

	// Update the object location to reflect the object placement.

	void drop() { }

   // Draw a selection box around the graphic object. Objects can be
   // marked for special display.

	void drawSelected(Graphics g, float sf, int wx, int wy, boolean marked, boolean flicker) { }

	// Method to fix the multipalette group.

	void fixPaletteGroup(Integer n) { }

	// Set the palette identifier value.

	void changePaletteID(Integer n) { }



	// Methods to maintain the object parent/child relation
	// ----------------------------------------------------


	void attach(KissObject o)
	{
		if (o == null) return ;
		KissObject kiss = o.getParent() ;
		if (kiss == this) return ;
		if (kiss != null) o.detach() ;
		o.setParent(this) ;
		if (children == null) children = new Vector() ;
		if (!children.contains(o)) children.add(o) ;
		Collections.sort(children) ;
      updateMoveRestrictions(o.getLocation(),o.getRestrictX(),o.getRestrictY()) ;
	}

	void detach()
	{
		if (parent == null) return ;
		Vector v = parent.getChildren() ;
		if (v == null) return ;
		v.remove(this) ;
      parent.updateMoveRestrictions(v) ;
		glued = false ;
		parent = null ;
      v = getChildren() ;
      updateMoveRestrictions(v) ;

		// Trigger any detached events for this object.

		Vector evt = getEvent("detached") ;
		if (evt != null)
			EventHandler.queueEvents(evt,Thread.currentThread(),this) ;
	}

	KissObject getFirstChild()
	{
		if (children == null) return null ;
		if (children.size() == 0) return null ;
		return (KissObject) children.elementAt(0) ;
	}

	KissObject getNextChild()
	{
		if (parent == null) return null ;
		Vector v = parent.getChildren() ;
		if (v == null) return null ;
		int n = v.indexOf(this) ;
		if (n < 0) return null ;
		if (n+1 >= v.size()) return null ;
		return (KissObject) v.elementAt(n+1) ;
	}


   // A function to reset this object to its initial attachment state.

   void reset()
   {
      glued = false ;
      parent = null ;
      children = null ;
      restrictx = restricty = null ;
      initrestrictx = initrestricty = null ;
   }


   // Method to update the object movement restrictions.  This update
   // sets the restriction values to the intersection of the current
   // restrictions and the new specified restrictions that apply to
   // the given location.

   void updateMoveRestrictions(Point location, Point x, Point y)
   {
      Point loc = getLocation() ;

      // Horizontal restrictions.  We compute the delta displacements that
      // the apply to the given location and then ensure that this object
      // cannot move such that the location point is outside the restriction
      // limits.

      if (x != null)
      {
         int deltaX1 = location.x - x.x ;
         int deltaX2 = x.y - location.x ;
         int x1 = loc.x - deltaX1 ;
         int x2 = loc.x + deltaX2 ;
         if (restrictx == null) restrictx = new Point(x1,x2) ;
         restrictx.x = Math.max(x1,restrictx.x) ;
         restrictx.y = Math.min(x2,restrictx.y) ;
      }

      // Vertical restrictions.

      if (y != null)
      {
         int deltaY1 = location.y - y.x ;
         int deltaY2 = y.y - location.y ;
         int y1 = loc.y - deltaY1 ;
         int y2 = loc.y + deltaY2 ;
         if (restricty == null) restricty = new Point(y1,y2) ;
         restricty.x = Math.max(y1,restricty.x) ;
         restricty.y = Math.min(y2,restricty.y) ;
      }

      // If this object has a parent we must update the parent restrictions.

      if (parent != null)
         parent.updateMoveRestrictions(parent.getChildren()) ;
   }


   // Method to update the object movement restrictions.  This update
   // sets the restriction values to the intersection of the current
   // restrictions and any new restrictions within the vector of
   // associated KissObjects.

   void updateMoveRestrictions(Vector v)
   {
      restrictx = (initrestrictx == null) ? null : new Point(initrestrictx) ;
      restricty = (initrestricty == null) ? null : new Point(initrestricty) ;
      if (v == null || v.size() == 0)
      {
         if (parent == null) return ;
         parent.updateMoveRestrictions(parent.getChildren()) ;
         return ;
      }

      // Recompute movement restrictions based upon the restrictions
      // in the specified list of objects.

      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (!(o instanceof KissObject)) continue ;
         KissObject kiss = (KissObject) o ;
         updateMoveRestrictions(kiss.getLocation(),kiss.getRestrictX(),kiss.getRestrictY()) ;
      }
   }


   // Method to rebuild the object movement restrictions.  This traverses
   // the hierarchy tree to find the leaf objects and rebuilds movement 
   // restrictions for these subordinate objects. Leaf nodes have their 
   // movement restrictions set to their initial values.

   void rebuildMoveRestrictions()
   {
      Vector v = getChildren() ;
      if (v != null)
      {
         for (int i = 0 ; i < v.size() ; i++)
         {
            Object o = v.elementAt(i) ;
            if (!(o instanceof KissObject)) continue ;
            KissObject kiss = (KissObject) o ;
            kiss.rebuildMoveRestrictions() ;
         }
      }
      else
         updateMoveRestrictions(getLocation(),getRestrictX(),getRestrictY()) ;
   }


	// Function to find the child object that is restricting movement.
   // The object is identified based upon the specified direction, which
   // is minimum or maximum X, minimum or maximum Y.  This function
   // returns the first attached object in the attachment hierarchy
   // that is unfixed and not glued.

	static KissObject getRestrictionObject(KissObject k,
      boolean isX1, boolean isX2, boolean isY1, boolean isY2)
	{
      if (k == null) return null ;
      if (!k.hasChildren()) return null ;
      Object [] packet = null ;

      // Check for restrictions on the children.

      if (isX1 || isX2)
         packet = getRestrictionXObject(k,k.getRestrictX(),k.getLocation(),isX1,isX2) ;
      if (packet == null && (isY1 || isY2))
         packet = getRestrictionYObject(k,k.getRestrictY(),k.getLocation(),isY1,isY2) ;

      // Return the child if found.

      if (packet == null) return null ;
      KissObject ko = (KissObject) packet[0] ;
      Boolean b = (Boolean) packet[1] ;
      if (b.booleanValue()) return ko ;
      return null ;
   }

   // When finding restriction objects we test for equality with the specified
   // restriction point. This equality will be satisfied if the object attachment
   // chain has had restrictions set through the updateMoveRestrictions() method.
   
	private static Object [] getRestrictionXObject(KissObject k,
      Point rx, Point location, boolean isX1, boolean isX2)
   {
      Vector v = k.getChildren() ;
      if (v == null) return null ;
      if (rx == null) return null ;

      // Find the restricting child object.  This is a recursive search.

      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (!(o instanceof Group)) continue ;
         Group g = (Group) o ;
         Point flex = g.getFlex() ;
         Point gx = g.getInitialRestrictX() ;

         // If we have an initial restriction this could be the restricting
         // child.  To be the restricting child it must be unglued and unfixed.

         if (gx != null)
         {
            Point gloc = g.getLocation() ;
            int x1 = gx.x - (gloc.x - location.x) ;
            int x2 = gx.y - (gloc.x - location.x) ;
            if ((isX1 && x1 == rx.x) || (isX2 && x2 == rx.y))
            {
               Object [] packet = new Object [2] ;
               packet[0] = g ;
               packet[1] = new Boolean(!g.isGlued() && 
                  (!OptionsDialog.getDetachFix() || (flex == null || flex.y == 0))) ;
               packet[1] = new Boolean(true) ;
               return packet ;
            }
         }

         // Recursively check for this child's restricting children.
         // If we find a child that is not fixed or glued, return this child.
         // If the restricting child is fixed or glued but this object is
         // not, return this object.

         Object [] packet = getRestrictionXObject(g,rx,location,isX1,isX2) ;
         if (packet == null) continue ;
         Boolean b = (Boolean) packet[1] ;
         if (b.booleanValue()) return packet ;
         if (!g.isGlued() && (flex == null || flex.y == 0))
         {
            packet[0] = g ;
            packet[1] = new Boolean(true) ;
         }
         return packet ;
      }
      return null ;
   }

	private static Object [] getRestrictionYObject(KissObject k, Point ry,
      Point location, boolean isY1, boolean isY2)
   {
      Vector v = k.getChildren() ;
      if (v == null) return null ;
      if (ry == null) return null ;

      // Find the restricting child object.  This is a recursive search.

      for (int i = 0 ; i < v.size() ; i++)
      {
         Object o = v.elementAt(i) ;
         if (!(o instanceof Group)) continue ;
         Group g = (Group) o ;
         Point flex = g.getFlex() ;
         Point gy = g.getInitialRestrictY() ;

         // If we have an initial restriction this could be the restricting
         // child.

         if (gy != null)
         {
            Point gloc = g.getLocation() ;
            int y1 = gy.x - (gloc.y - location.y) ;
            int y2 = gy.y - (gloc.y - location.y) ;
            if ((isY1 && y1 == ry.x) || (isY2 && y2 == ry.y))
            {
               Object [] packet = new Object [2] ;
               packet[0] = g ;
               packet[1] = new Boolean(!g.isGlued() && 
                  (!OptionsDialog.getDetachFix() || (flex == null || flex.y == 0))) ;
               packet[1] = new Boolean(true) ;
               return packet ;
            }
         }

         // Recursively check for this child's restricting children.
         // If we find a child that is not fixed or glued, return this child.
         // If the restricting child is fixed or glued but this object is
         // not, return this object.

         Object [] packet = getRestrictionYObject(g,ry,location,isY1,isY2) ;
         if (packet == null) continue ;
         Boolean b = (Boolean) packet[1] ;
         if (b.booleanValue()) return packet ;
         if (!g.isGlued() && (flex == null || flex.y == 0))
         {
            packet[0] = g ;
            packet[1] = new Boolean(true) ;
         }
         return packet ;
      }
      return null ;
   }





	// Utility methods for class mantenance
	// ------------------------------------


	// The toString method returns a string representation of this object.
   // This is the class name concatenated with the object identifier.

   public String toString()
   {
      Object o = getIdentifier() ;
      String s = (o == null) ? "" : o.toString() ;
      String classname = this.getClass().getName() ;
      String packagename = Kisekae.getPackageName() ;
      int n = (packagename != null) ? packagename.length() + 1 : 0 ;
      if (classname.startsWith(packagename)) classname = classname.substring(n) ;
      if (s.length() == 0) return classname ;
		return classname + " " + s ;
   }


   // Required comparison method for the Comparable interface.  Numeric
   // comparisons are made if we are being compared to another instance of
   // our class and our identifiers are integers.  Otherwise, string
   // comparisons are performed.

   public int compareTo(Object o)
   {
   	Integer n1 = null ;
      Integer n2 = null ;
      boolean numeric = true ;

   	if (!(o instanceof KissObject)) return -1 ;
   	if (o.getClass().equals(this.getClass()))
      {
         Object o1 = getIdentifier() ;
      	Object o2 = ((KissObject) o).getIdentifier() ;

	      // Use an Integer sort if applicable.  This maintains a
	      // numeric sort for object name comparisons.

	      try { n1 = new Integer(o1.toString()) ; }
	      catch (NumberFormatException e) { numeric = false ; }
	      try { n2 = new Integer(o2.toString()) ; }
	      catch (NumberFormatException e) { numeric = false ; }
      	if (numeric) return (n1.compareTo(n2)) ;
      }
      return (toString().compareTo(o.toString())) ;
   }

	// Shallow clone.  This creates a new object where all object references
   // are the same references as found in the original object.

   public Object clone()
   {
	   try { return super.clone() ; }
      catch (CloneNotSupportedException e)
      {
      	System.out.println("KissObject: Clone failure, " + this.toString()) ;
      	e.printStackTrace();
         return null ;
      }
   }
}
