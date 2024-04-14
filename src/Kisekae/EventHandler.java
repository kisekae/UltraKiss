package Kisekae ;

// Title:        Kisekae UltraKiss
// Version:      2.0
// Copyright:    Copyright (c) 2002-2005
// Company:      WSM Information Systems Inc.
// Description:  Kisekae Set System
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
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
* FKiSS EventHandler class
*
* Purpose:
*
* This class is a container class to process all defined FKiSS
* events.  It runs as a separate activity and processes all events
* that are placed on its input queue.
*
*/


import java.awt.Cursor ;
import java.util.Vector ;
import java.util.Enumeration ;
import java.util.Hashtable ;
import java.util.Comparator ;
import javax.swing.JOptionPane ;
import java.io.* ;
import java.util.Collections;

final class EventHandler extends KissObject
	implements Runnable
{
	// Class attributes

	private static String definedEvents[] = {

	 // FKiSS 1 events

	 "eventhandler",	// [0]  Start of event handler code
	 "alarm",			// [1]  Activates when an alarm fires
	 "begin",			// [2]  Triggers when data set is first viewed
	 "catch",			// [3]  Triggers on mouse down if object is movable
	 "col",				// [4]  Activates when the palette group is changed
	 "drop",				// [5]  Triggers on mouse up if object is movable
	 "end",				// [6]  Activates when a data set is closed
	 "fixcatch",		// [7]  Triggers on mouse down if object is fixed
	 "fixdrop",			// [8]  Triggers on mouse up if object is fixed
	 "initialize",		// [9]  Triggers when a data set is initialized
	 "never",			// [10] This event is never triggered
	 "nothing",			// [11] This event is never triggered
	 "press",			// [12] Triggers on mouse down regardless of fix
	 "release",			// [13] Triggers on mouse up regardless of fix
	 "set",				// [14] Activates when the page set is changed
	 "unfix",			// [15] Triggers when object fix value becomes zero

	 // FKiSS 2 events

	 "in",				// [16] Triggers when object bounding boxes overlap
	 "out",				// [17] When object bounding boxes no longer overlap
	 "stillin",			// [18] When object bounding boxes still overlap
	 "stillout",		// [19] When object bounding boxes still do not overlap
	 "version",			// [20] When viewer version greater than command version

	 // FKiSS 2.1 events

	 "apart",			// [21] Triggers when touching cels come apart
	 "collide",			// [22] Triggers when non-touching cels touch

	 // FKiSS 3 events

	 "label",			// [23] Program module grouping
	 "overflow",		// [24] Exception handler for errors

	 // FKiSS 4 events

	 "mousein",			// [25] Triggers when mouse enters cel or group
	 "mouseout",		// [26] Triggers when mouse leaves cel or group
	 "detached",		// [27] Signals object no longer attached to parent
	 "keypress",		// [28] Activates when the user presses a key
	 "keyrelease",		// [29] Activates when the user releases a key

	 // FKiSS 5 Kisekae extension events

	 "keytype",			// [30] Activates when the user types a key
	 "mediastart",		// [31] Fires when a media file starts
	 "mediastop",		// [32] Fires when a media file stops

	 // FKiSS 1 events

	 "unknown"			// [33] This event is never triggered
	} ;


	private static String definedActions[] = {

	 // FKiSS 1 and FKiSS 2 commands.

	 "altmap",			// [0]  Inverts an object or cel visibility
	 "changecol",		// [1]  Switches to a new color palette
	 "changeset",		// [2]  Switches to a new page set
	 "debug",			// [3]  Shows a message dialog box
	 "iffixed",			// [4]  Sets a timer if an object is fixed
	 "ifmapped",		// [5]  Sets a timer if a cel is visible
	 "ifmoved",			// [6]  Sets a timer if an object has ever moved
	 "ifnotfixed",		// [7]  Sets a timer if an object is not fixed
	 "ifnotmapped",	// [8]  Sets a timer if a cel is not visible
	 "ifnotmoved",		// [9]  Sets a timer if an object has never moved
	 "map",				// [10] Makes an object or cel visible
	 "move",				// [11] Moves an object in x and y dimension
	 "movebyx",			// [12] Moves an object in x dimension
	 "movebyy",			// [13] Moves an object in y dimension
	 "moverandx",		// [14] Moves an object by random amount in x dimension
	 "moverandy",		// [15] Moves an object by random amount in y dimension
	 "moveto",			// [16] Moves an object to an absolute (x,y) location
	 "movetorand",		// [17] Moves an object to a random location
	 "music",			// [18] Plays a MIDI file
	 "nop",				// [19] Does nothing
	 "notify",			// [20] Synonym for debug
	 "quit",				// [21] Terminates execution of the data set
	 "randomtimer",	// [22] Sets a timer to a random value
	 "setfix",			// [23] Sets the fix value for an object
	 "shell",			// [24] Invokes a shell program on the host
	 "sound",			// [25] Plays a WAV or AU sound file
	 "timer",			// [26] Sets a timer which will fire an alarm
	 "transparent",	// [27] Adjusts a cel or object transparency
	 "unmap",			// [28] Makes an object or cel invisible
	 "viewport",		// [29] Changes the viewport offset
	 "windowsize",		// [30] Changes the window size

	 // FKiSS 3 commands.

	 "goto",				// [31] Jump to label event
	 "gosub",			// [32] Subroutine call to label event
	 "gotorandom",		// [33] Random selection of label event
	 "gosubrandom",	// [34] Random selection of subroutine
	 "exitevent",		// [35] Exit the label module
	 "let",				// [36] Assignment statement
	 "add",				// [37] Addition statement
	 "sub",				// [38] Subtraction statement
	 "mul",				// [39] Multiplication statement
	 "div",				// [40] Integer division statement
	 "mod",				// [41] Modulus statement
	 "random",			// [42] Random number generator
	 "letobjectx",		// [43] X-coordinate of object
	 "letobjecty",		// [44] Y-coordinate of object
	 "letfix",			// [45] Flex value of object
	 "letmapped",		// [46] Visibility of object
	 "letset",			// [47] Current page set number
	 "letpal",			// [48] Current palette number
	 "letmousex",		// [49] Mouse x-coordinate
	 "letmousey",		// [50] Mouse y-coordinate
	 "letcatch",		// [51] Object currently selected
	 "letcollide",		// [52] True if identified cels are touching
	 "letinside",		// [53] True if identified objects overlap
	 "lettransparent",// [54] Cel transparency
	 "ifequal",			// [55] Logical if statement
	 "ifnotequal",		// [56] Logical not if statement
	 "ifgreaterthan",	// [57] Numeric if statement
	 "iflessthan",		// [58] Numeric if statement
	 "else",				// [59] If statement optional part
	 "endif",			// [60] If statement end
	 "ghost",			// [61] Makes cels ungrabbable

	 // FKiSS 5 Kisekae extension commands.

	 "letobjectw",		// [62] Width of object
	 "letobjecth",		// [63] Height of object
	 "while",			// [64] While statement
	 "endwhile",		// [65] While statement end
	 "showstatus",		// [66] Show status bar text
	 "letcel",			// [67] Cel currently selected
	 "letcomment",		// [68] Cel comment
	 "concat",			// [69] String concatenation
	 "substr",			// [70] String substring
	 "animate",			// [71] Sets a cel loop count
    "for",           // [72] Loop statement
    "next",          // [73] Loop terminator statement
	 "movie",         // [74] Shows a movie

	 // FKiSS 4 commands.

	 "letinitx",		// [75] Initial x ordinate of object
	 "letinity",		// [76] Initial y ordinate of object
	 "letwidth",		// [77] Width of object
	 "letheight",		// [78] Height of object
	 "letframe", 		// [79] Return current frame of object
	 "setframe", 		// [80] Set current frame of object
	 "attach", 			// [81] Attach an object to a parent
	 "detach", 			// [82] Detach an object from its parent
	 "glue", 			// [83] Attach an object but movements do not detach
	 "letchild",		// [84] Lowest numbered object attached
	 "letparent",		// [85] Number of parent object
	 "letsibling",		// [86] Next lowest numbered object attached
	 "letkey",			// [87] Index of first key pressed in list
	 "letkeymap", 		// [88] Binary key mapping for multiple key strokes

	 // FKiSS 5 Kisekae extension commands.

	 "letkeychar",	  			// [89] The last key typed character
	 "letkeycode",   			// [90] The last key typed virtual key code
	 "letkeymodifier", 		// [91] The last key typed modifiers
	 "letkeystring",   		// [92] The last key typed string name
    "mediaplayer",		 	// [93] Invoke the media player for a playlist

	 // FKiSS 4 elseif commands.

	 "elseifequal",	 		// [94] Else variant (equal)
	 "elseifnotequal", 		// [95] Else variant (notequal)
	 "elseifgreaterthan",	// [96] Else variant (greaterthan)
	 "elseiflessthan",		// [97] Else variant (lessthan)

	 // FKiSS 5 Kisekae extension commands.

    "clone",		 	      // [98] Create a new object
    "destroy",		 	      // [99] Destroy an object
    "letlevel",	 	      // [100] Return the object draw level
    "setlevel",		 	   // [101] Set the object draw level
    "indexof",		 	      // [102] Returns index of substring in string
    "replacestr",		 	   // [103] Replaces a substring in string

	 // FKiSS 4 movement restriction commands.

    "restrictx",		 	   // [104] Restrict object movement in X direction
    "restricty",		 	   // [105] Restrict object movement in Y direction

    // FKiSS 4 new proposed commands.

    "lettimer",		 	   // [106] Return the remaining time on a timer
    "setpal",		 	      // [107] Set a cel/object/cel palette group
    "letkcf",		 	      // [108] Return a cel palette file number
    "setkcf",		 	      // [109] Set a cel/object/cel new palette file

    // FKiSS 5 commands to interface with the viewer.

    "viewer",		 	      // [110] Viewer specific commands
    "strlen",		 	      // [111] Length of a string

    // FKiSS 5 commands to adjust component attributes.

    "getText",		 	      // [112] Get component text
    "setText",		 	      // [113] Set component text
    "getSelected",		 	// [114] Get component selection state
    "setSelected",		   // [115] Set component selection state
    "getValueAt",		 	   // [116] Get component list value
    "setValueAt",		 	   // [117] Set component list value
    "addItem",		 	      // [118] Add component item
    "removeItem",		 	   // [119] Remove component item
    "getSelectedIndex",		// [120] Get component value at index
    "setSelectedIndex",		// [121] Set component value at index
    "getSelectedValue",		// [122] Get component selected value
    "setSelectedValue",		// [123] Set component selection value
    "getSelectedItem",		// [124] Get component selected index
    "setSelectedItem",		// [125] Set component selected index
    "getIndexOf",		      // [126] Get index of list item
    "removeAll",	         // [127] Remove all list items
    "getItemCount",	      // [128] Get number of list items
    "getEnabled",	         // [129] Get the enable state of the component
    "setEnabled",	         // [130] Enable the component
    "getNextSelectedIndex", // [131] Get the next selected index
    "setAttributes",       // [132] Set the component attributes

    // FKiSS 5 commands for file access.

    "open",		 	         // [133] Open a file
    "read",		 	         // [134] Read a line
    "write", 		 	      // [135] Write a line
    "close", 		 	      // [136] Close a file
    "edit", 		 	      // [137] Edit a file

    // FKiSS 5 commands for environment access.

    "environment",		 	// [138] A generic system request
    "confirm", 		 	   // [139] Show a modal confirm dialog
    "setmodal", 		 	   // [140] Set the modal event object source
    "letmodal", 		 	   // [141] Return the modal object source
    "event", 		 	      // [142] Fire an existing event
    "paint", 		 	      // [143] Paint the current screen
    "wait", 		 	      // [144] Wait for a notify lock
    "signal", 		 	      // [145] Notify queued activities
    "sleep", 		 	      // [146] Sleep for a bit
    "mouseRelease", 		 	// [147] Force a mouse release
    "sqrt",		 	         // [148] Square root of a number

    // FKiSS 4 new proposed commands.

    "repeat",		 	      // [149] Repeat a gosub 'n' times
    "valuepool",		 	   // [150] Establish a local poperties pool
    "loadvalue",		 	   // [151] Load a property variable
    "savevalue",		 	   // [152] Remove a property variable
    "deletevalue",		   // [153] Set a property variable
    "exitloop",		 	   // [154] Exit the Repeat 

    // FKiSS 5 commands for environment access.

    "letmaxpage",          // [155] Get the maximum pages defined
    "letmaxcolor",         // [156] Get the maximum color sets defined
    "getAttributes",       // [157] Get the component attributes
    "math",                // [158] Math function library
    "format",              // [159] Decimal format function

    // FKiSS 5 additional commands for cloned objects.
  
    "letcloned",           // [160] Cloned object source
    "letaudio",            // [161] Last audio name started
    "setPage",             // [162] Set url page for text pane
	} ;

   // FKiSS event specification levels.

   private static byte [] eventfkisslevel = {
      1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
      2,2,2,2,2,
      2,2,
      3,3,
      4,4,4,4,4,
      5,5,5,
      1 } ;

   // FKiSS action specification levels.

   private static byte [] actionfkisslevel = {
      1,1,1,1,2,2,2,2,2,2,1,1,2,2,2,2,2,2,2,1,2,1,1,2,1,1,1,1,1,1,1,
      3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,
      5,5,5,5,5,5,5,5,5,5,5,5,5,
      4,4,4,4,4,4,4,4,4,4,4,4,4,4,
      5,5,5,5,5,
      4,4,4,4,
      5,5,5,5,5,5,
      4,4,
      4,4,4,4,
      5,5,
      5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
      5,5,5,5,5,
      5,5,5,5,5,5,5,5,5,5,5,
      4,4,4,4,4,4,
      5,5,5,5,5,
      5,5,5 } ;

   // FKiSS event mandatory number of parameters.

   private static int [] eventmandatoryparams = {
      0,1,0,1,1,1,0,1,1,0,0,0,1,1,1,1,
      2,2,2,2,1,
      2,2,
      1,0,
      1,1,1,1,1,
      1,1,1,
      0 } ;

   // FKiSS action mandatory number of parameters.

   private static int [] actionmandatoryparams = {
      1,1,1,1,3,3,3,3,3,3,1,3,3,3,3,3,3,1,1,0,1,0,3,2,1,1,2,2,1,2,2,
      1,1,3,3,0,1,3,3,3,3,3,3,2,2,2,2,1,1,1,1,1,3,3,2,2,2,2,2,0,0,1,
      2,2,1,1,1,1,1,2,3,2,3,1,1,
      2,2,2,2,2,2,2,1,2,2,2,2,2,2,
      1,1,1,1,1,
      2,2,2,2,
      2,1,2,2,3,4,
      3,3,
      2,2,2,2,
      1,2,
      2,2,2,2,3,3,2,2,2,2,2,2,2,2,3,1,2,2,2,2,2,
      3,4,3,2,3,
      1,2,0,1,1,0,1,1,1,0,2,
      3,1,2,2,1,0,
      1,1,2,2,6,
      2,1,2 } ;

   // FKiSS event valid parameter types by parameter position.
   // These are according to the fuzzy FKiSS specifications.
   // Bit 0 is object, bit 1 is cel name, bit 2 is variable, 
   // bit 3 is number, bit 4 is string, bit 5 is celgroup,
   // bit 6 is name, bit 7 is a variable declaration, bit 8
   // is generic '*', bit 9 is object literal.

   private static int [][] eventparamdkiss = {
      { 0,72,0,35,264,35,0,35,35,0,0,0,35,35,264,35,
      35,35,35,35,8,
      35,35,
      72,0,
      35,35,1,16,16,
      16,16,16,
      0 } 
      } ;

   private static int [][] eventparampkiss = {
      { 0,72,0,3,264,3,0,3,3,0,0,0,35,35,264,3,
      35,35,35,35,8,
      34,34,
      72,0,
      3,3,1,16,16,
      16,16,16,
      0 } 
      } ;
      
   private static int [][] eventparamukiss = {
      { 0,72,0,35,264,35,0,35,35,0,0,0,35,35,264,35,
      35,35,35,35,8,
      35,35,
      72,0,
      35,35,1,16,16,
      16,16,16,
      0 } 
      } ;

   // FKiSS action valid parameter types by parameter position.
   // These are are for DirectKiss.
   // Bit 0 is object, bit 1 is celname, bit 2 is variable, 
   // bit 3 is number, bit 4 is string, bit 5 is celgroup,
   // bit 6 is name, bit 7 is a variable declaration. For
   // example, if the first parameter of action 12 must be of 
   // object or cel name type, then actionparamtype[0][12] = 3.
   // Objects are #nnn or #vbl, celnames are "name.ext", 
   // variables begin with a letter, numbers begin with a 
   // digit or +/-, strings begin with a ", celgroups begin
   // with a !, and names begin with a letter. Celnames are
   // also strings, numbers are objects, and variables are
   // distinguished from names by the action command.

   private static int [][] actionparamdkiss = {
      { 7,12,12,16,5,35,5,5,35,5,39,5,5,5,5,5,5,5,16,0,16,0,76,5,16,16,76,39,39,12,12,
      76,76,12,12,0,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,12,12,12,12,0,0,7,
      132,132,4,4,20,132,132,132,132,23,132,4,20,
      132,132,132,132,132,32,5,5,5,132,132,132,132,132,
      132,132,132,132,20,
      12,12,12,12,
      132,5,132,7,132,132,
      5,5,
      132,39,132,39,
      20,4,
      132,6,132,6,132,6,20,6,132,6,132,6,132,6,132,6,132,132,6,132,6,
      132,132,132,132,132,
      132,132,7,132,20,12,4,4,12,0,132,
      76,16,132,16,16,0,
      132,132,132,132,132, 
      132,4,6 },
      
      { 0,0,0,0,76,76,76,76,76,76,0,12,5,5,12,12,12,0,0,0,16,0,12,12,0,0,12,12,0,12,12,
      0,0,76,76,0,12,12,12,12,12,12,12,5,5,5,39,0,6,0,0,0,39,39,39,12,12,12,12,0,0,12,
      5,5,28,0,20,0,6,28,20,12,12,0,12,
      5,5,5,5,32,12,5,0,5,5,5,5,20,20,
      0,0,0,0,12,
      12,12,12,12,
      5,0,7,12,20,20,
      12,12,
      76,12,39,12,
      20,20,
      6,28,6,28,6,12,28,28,6,12,6,28,6,28,6,0,6,6,28,6,20,
      20,20,20,20,20,
      20,20,0,0,20,0,12,0,0,0,12,
      12,0,16,28,0,0,
      0,0,6,20,12, 
      5,0,28 },
      
      { 0,0,0,0,12,12,12,12,12,12,0,12,12,12,12,12,12,0,0,0,0,0,12,0,0,0,0,0,0,0,0,
      0,0,76,76,0,0,12,12,12,12,12,12,0,0,0,0,0,0,0,0,0,39,39,0,0,0,0,0,0,0,0,
      5,5,20,0,20,0,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,20,20,
      12,12,
      0,0,0,0,
      540,0,
      0,0,0,0,12,28,0,0,0,0,0,0,0,0,28,0,0,0,0,0,12,
      20,12,20,20,20,
      20,20,0,0,20,0,0,0,0,0,0,
      132,0,0,0,0,0,
      0,0,20,12,12, 
      0,0,0 },
      
      { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,20,0,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,0,20,
      0,0,
      0,0,0,0,
      540,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      12,20,0,12,28,
      20,20,0,0,20,0,0,0,0,0,0,
      0,0,0,0,0,0,
      0,0,0,12,12, 
      0,0,0 }
      } ;
      

   // FKiSS action valid parameter types by parameter position.
   // These are are for PlayFKiss.
   // Bit 0 is object, bit 1 is celname, bit 2 is variable, 
   // bit 3 is number, bit 4 is string, bit 5 is celgroup,
   // bit 6 is name, bit 7 is a variable declaration. 
      
   private static int [][] actionparampkiss = {
      { 7,12,12,16,7,35,5,7,35,5,39,5,5,5,5,5,5,5,16,0,16,0,76,7,16,16,76,39,39,12,12,
      76,76,12,12,0,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,12,12,12,12,0,0,7,
      132,132,4,4,20,132,132,132,132,23,132,4,20,
      132,132,132,132,132,32,5,5,5,132,132,132,132,132,
      132,132,132,132,20,
      12,12,12,12,
      132,5,132,7,132,132,
      5,5,
      132,39,132,39,
      20,4,
      132,6,132,6,132,6,20,6,132,6,132,6,132,6,132,6,132,132,6,132,6,
      132,132,132,132,132,
      132,132,7,132,20,12,4,4,12,0,132,
      76,16,132,16,16,0,
      132,132,132,132,132,
      132,4,6 },
      
      { 0,0,0,0,76,76,76,76,76,76,0,12,5,5,12,12,12,0,0,0,16,0,12,12,0,0,12,12,0,12,12,
      0,0,76,76,0,12,12,12,12,12,12,12,5,5,5,39,0,6,0,0,0,39,39,39,12,12,12,12,0,0,12,
      5,5,28,0,20,0,6,20,28,12,12,0,12,
      5,5,5,5,32,12,5,0,5,5,5,5,20,20,
      0,0,0,0,12,
      12,12,12,12,
      5,0,7,12,20,20,
      12,12,
      76,12,7,12,
      20,20,
      6,28,6,28,6,12,28,28,6,12,6,28,6,28,6,0,6,6,28,6,20,
      20,20,20,20,20,
      20,20,0,0,20,0,12,0,0,0,12,
      12,0,16,28,0,0,
      0,0,6,20,12,
      5,0,28 },
      
      { 0,0,0,0,12,12,12,12,12,12,0,12,12,12,12,12,12,0,0,0,0,0,12,0,0,0,0,0,0,0,0,
      0,0,76,76,0,0,12,12,12,12,12,12,0,0,0,0,0,0,0,0,0,39,39,0,0,0,0,0,0,0,0,
      5,5,20,0,20,0,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,20,20,
      12,12,
      0,0,0,0,
      540,0,
      0,0,0,0,12,28,0,0,0,0,0,0,0,0,28,0,0,0,0,0,12,
      20,12,20,20,20,
      20,20,0,0,20,0,0,0,0,0,0,
      132,0,0,0,0,0,
      0,0,20,12,12,
      0,0,0 },
      
      { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,20,0,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,0,20,
      0,0,
      0,0,0,0,
      540,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      12,20,0,12,28,
      20,20,0,0,20,0,0,0,0,0,0,
      0,0,0,0,0,0,
      0,0,0,12,12,
      0,0,0 }
      } ;

   // FKiSS action valid parameter types by parameter position.
   // These are according to the UltraKiss specifications.
   // UltraKiss allows strings and local variable names
   // and celnames on many commands.
   // Bit 0 is object, bit 1 is celname, bit 2 is variable, 
   // bit 3 is number, bit 4 is string, bit 5 is celgroup,
   // bit 6 is name, bit 7 is a variable declaration, bit 8
   // is generic '*' value, bit 9 is object literal.

   private static int [][] actionparamukiss = {
      { 39,12,12,28,7,39,7,7,39,7,39,7,7,7,7,7,7,7,20,0,28,0,76,7,20,20,76,39,39,12,12,
      76,76,12,12,0,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,132,28,28,28,28,0,0,39,
      132,132,4,4,20,132,132,132,132,55,132,4,20,
      132,132,132,132,132,39,5,5,5,132,132,132,132,132,
      132,132,132,132,20,
      28,28,28,28,
      132,5,132,7,132,132,
      5,5,
      132,39,132,39,
      20,4,
      132,6,132,6,132,6,20,6,132,6,132,6,132,6,132,6,132,132,6,132,6,
      132,132,132,132,132,
      148,132,7,132,20,12,4,4,12,0,132,
      76,16,132,16,16,0,
      132,132,132,132,132,
      132,4,6 },
      
      { 0,0,0,28,76,76,76,76,76,76,0,12,7,7,12,12,12,0,12,0,28,0,12,12,0,12,12,12,0,12,12,
      28,28,76,76,0,540,12,12,12,12,12,12,7,7,7,39,0,6,0,0,39,39,39,39,28,28,28,28,0,0,12,
      5,5,28,0,20,39,6,28,20,12,12,0,12,
      7,7,5,5,39,12,5,0,5,5,5,5,20,20,
      0,0,0,0,12,
      28,28,28,28,
      5,0,7,12,20,20,
      12,12,
      76,12,39,12,
      20,20,
      6,28,6,28,6,12,28,28,6,12,6,28,6,28,6,0,6,6,28,6,20,
      20,20,20,20,20,
      20,28,0,0,20,0,12,0,0,0,12,
      12,0,16,28,0,0,
      0,0,6,20,12,
      5,0,28 },
      
      { 0,0,0,28,12,12,12,12,12,12,0,12,12,12,12,12,12,0,0,0,28,0,12,0,0,0,28,28,0,0,0,
      28,28,76,76,0,0,12,12,12,12,12,12,0,0,0,0,0,0,0,0,0,39,39,0,0,0,0,0,0,0,0,
      5,5,20,0,20,12,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,20,20,
      12,12,
      0,0,0,0,
      540,0,
      0,0,0,0,12,28,0,0,0,0,0,0,0,0,28,0,0,0,0,0,12,
      20,12,20,20,20,
      20,28,0,0,20,0,0,0,0,0,0,
      132,0,0,0,0,0,
      0,0,20,12,12,
      0,0,0 },
      
      { 0,0,0,28,28,28,28,28,28,28,0,24,24,24,24,24,24,0,0,0,28,0,0,0,0,0,28,0,0,0,0,
      28,28,28,28,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,20,0,0,28,12,0,12,0,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      0,0,0,0,12,
      0,0,0,0,
      0,0,0,0,12,20,
      0,0,
      0,0,0,0,
      540,0,
      0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
      12,28,0,12,28,
      20,20,0,0,20,0,0,0,0,0,0,
      0,0,0,0,0,0,
      0,0,0,12,12,
      0,0,0 }
      } ;


	// Object attributes

   private static int count = 0 ;                  // The handler count
   private static ThreadGroup threadgroup = null ;	// The event handler group
   private static Vector handlers = new Vector() ;	// The active handlers
	private static Vector queue = new Vector() ;    // The event queue
   private static final Object eventlock = new Object() ;  // The event lock
	private static PanelFrame panel = null ;        // The drawing window
	private static Object modal = null ;            // The modal event source
   private static Hashtable signatures = null ;    // The event signatures
	private static boolean suspend = false ;	      // True if we must suspend
	private static boolean stop = false ;	         // True if we must stop
   private static boolean manualsuspend = false ;	// True if manual suspend

	// Thread attributes

	private Thread thread = null ;	      	      // The active thread
   private String threadname = null ;					// The thread name
   private long firecount = 0 ;							// Count of events processed
	private boolean active = false ;	         	   // True if handler is active
   private boolean wait = false ;						// True if waiting
	private static long createtime = 0 ;			   // EventHandler creation time
   private static Hashtable stats = new Hashtable(1000) ; // Queue wait time stats


	// Constructor

   public EventHandler()
   {
      threadname = "EventHandler-" + count++ ;
      if (signatures == null)
      {
         signatures = new Hashtable() ;
         signatures.put("alarm","alarm(alarmname) ; Activates when an alarm fires on a timer statement") ;
         signatures.put("begin","begin() ; Triggers when data set is first viewed") ;
         signatures.put("catch","catch(target) ; Triggers on mouse down if object is movable") ;
         signatures.put("col","col(integer) ; Activates when the palette group is changed") ;
         signatures.put("drop","drop(target) ; Triggers on mouse up if object is movable") ;
         signatures.put("end","end() ; Activates when a data set is closed") ;
         signatures.put("fixcatch","fixcatch(target) ; Triggers on mouse down if object is fixed") ;
         signatures.put("fixdrop","fixdrop(target) ; Triggers on mouse up if object is fixed") ;
         signatures.put("initialize","initialize() ; Triggers when a data set is initialized") ;
         signatures.put("never","never() ; This event is never triggered") ;
         signatures.put("nothing","nothing() ; This event is never triggered") ;
         signatures.put("press","press(target) ; Triggers on mouse down regardless of fix") ;
         signatures.put("release","release(target) ; Triggers on mouse up regardless of fix") ;
         signatures.put("set","set(integer) ; Activates when the page set is changed") ;
         signatures.put("unfix","unfix(target) ; Triggers when object fix value becomes zero") ;
         signatures.put("in","in(target,target) ; Triggers when object bounding boxes overlap") ;
         signatures.put("out","out(target,target) ; Triggers when object bounding boxes no longer overlap") ;
         signatures.put("stillin","stillin(target,target) ; Triggers when object bounding boxes still overlap") ;
         signatures.put("stillout","stillout(target,target) ; Triggers when object bounding boxes still do not overlap") ;
         signatures.put("version","version(integer) ; Activates when viewer version greater than specified version") ;
         signatures.put("apart","apart(target,target) ; Triggers when touching cels come apart") ;
         signatures.put("collide","collide(target,target) ; Triggers when non-touching cels touch") ;
         signatures.put("label","label(identifier[, parameter1, parameter2, ...]) ; Begins a program label module specification") ;
         signatures.put("overflow","overflow() ; Exception handler for errors") ;
         signatures.put("mousein","mousein(target) ; Triggers when mouse enters cel or group") ;
         signatures.put("mouseout","mouseout(target) ; Triggers when mouse leaves cel or group" ) ;
         signatures.put("detached","detached(object) ; Triggers when object is no longer attached to parent") ;
         signatures.put("keypress","keypress(keyliteral) ; Activates when the user presses a key") ;
         signatures.put("keyrelease","keyrelease(keyliteral) ;	Activates when the user releases a key") ;
         signatures.put("keytype","keytype(keyliteral) ;	Activates when the user types a key") ;
         signatures.put("mediastart","mediastart(filename) ; Fires when a media file starts playing") ;
         signatures.put("mediastop","mediastop(filename) ; Fires when a media file stops playing") ;
         signatures.put("unknown", "unknown() ; This event is never triggered") ;
         signatures.put("altmap","altmap(target) ; Inverts an object or cel visibility") ;
         signatures.put("changecol","changecol(integer) ; Switches to a new color palette") ;
         signatures.put("changeset","changecol(integer) ; Switches to a new page set") ;
         signatures.put("debug","debug(string[,string,string,...]) ; Shows a debug trace message") ;
         signatures.put("iffixed","iffixed(target,alarmname,duration) ; Sets a timer if an object is fixed") ;
         signatures.put("ifmapped","ifmapped(target,alarmname,duration) ; Sets a timer if a cel is visible") ;
         signatures.put("ifmoved","ifmapped(target,alarmname,duration) ; Sets a timer if an object has ever moved") ;
         signatures.put("ifnotfixed","ifnotfixed(target,alarmname,duration) ; Sets a timer if an object is not fixed") ;
         signatures.put("ifnotmapped","ifnotmapped(target,alarmname,duration) ; Sets a timer if a cel is not visible") ;
         signatures.put("ifnotmoved","ifnotmoved(target,alarmname,duration) ; Sets a timer if an object has never moved") ;
         signatures.put("map","map(target) ; Makes an object or cel visible") ;
         signatures.put("move","move(target,integer,integer[,noconstrain]) ;  Moves an object or cel in x and y dimension") ;
         signatures.put("movebyx","movebyx(target,base,integer[,noconstrain]) ; Moves an object or cel in x dimension relative to base") ;
         signatures.put("movebyy","movebyy(target,base,integer[,noconstrain]) ; Moves an object or cel in y dimension relative to base") ;
         signatures.put("moverandx","moverandx(target,minimum,maximum[,noconstrain]) ; Moves an object or cel by random amount in x dimension") ;
         signatures.put("moverandy","moverandy(target,minimum,maximum[,noconstrain]) ; Moves an object or cel by random amount in y dimension") ;
         signatures.put("moveto","moverandy(target,integer,integer[,noconstrain]) ; Moves an object or cel to an absolute (x,y) location") ;
         signatures.put("movetorand","movetorand(target) ; Moves an object or cel to a random location") ;
         signatures.put("music","music(filename[,repeat]) ; Plays an audio file with optional repeat count") ;
         signatures.put("nop","nop() ; Does nothing") ;
         signatures.put("notify","notify (string[,string,string, ...]) ; Shows a message dialog box") ;
         signatures.put("quit","quit() ; Terminates execution of the data set") ;
         signatures.put("randomtimer","randomtimer (alarmname,delay,range[,operand1,operand2,operand3,...]) ; Sets a timer to a random value") ;
         signatures.put("setfix","setfix(target,integer) ; Sets the fix value for an object or cel") ;
         signatures.put("shell","shell(string) ; Invokes a shell program on the host") ;
         signatures.put("sound","sound(filename[,repeat]) ; Plays an audio file with optional repeat count") ;
         signatures.put("timer","timer (alarmname,delay[,operand1,operand2,operand3, ...]) ; Sets a timer which will fire the specified alarm") ;
         signatures.put("transparent","transparent (target,integer[,bound]) ; Adjusts a cel or object transparency") ;
         signatures.put("unmap","unmap(target) ; Makes an object or cel invisible") ;
         signatures.put("viewport","viewport(integer,integer) ; Changes the viewport offset in the X and Y dimension") ;
         signatures.put("windowsize","windowsize(width,height) ; Changes the window size by a relative width and height") ;
         signatures.put("goto","goto(labelname[,argument1,argument2, ...]) ; Jump to the specified label module") ;
         signatures.put("gosub","gosub(labelname[,argument1,argument2, ...]) ; Subroutine call to the specified label module") ;
         signatures.put("gotorandom","gotorandom(percent,labelname1,labelname2[,argument1,argument2, ...]) ; Random jump to label module") ;
         signatures.put("gosubrandom","gosubrandom(percent,labelname1,labelname2[,argument1,argument2, ...]) ;	Random selection of subroutine to call") ;
         signatures.put("exitevent","exitevent() ; Exit the label module") ;
         signatures.put("let","let(variable,value) ; Assignment statement to give a variable a value") ;
         signatures.put("add","add(variable,operand1,operand2) ; Addition statement assigns variable the sum of operands") ;
         signatures.put("sub","sub(variable,operand1,operand2) ; Subtraction statement subtracts operand2 from operand 1") ;
         signatures.put("mul","mul(variable,operand1,operand2) ; Multiplication statement") ; 
         signatures.put("div","div(variable,operand1,operand2) ; Integer division statement unless an operand is a real number") ;
         signatures.put("mod","mod(variable,operand1,operand2) ; Modulus statement yields integer division remainder") ;
         signatures.put("random","random(variable,operand1,operand2) ; Assigns a random number between operand1 and operand2") ;
         signatures.put("letobjectx","letobjectx(variable,target) ; Assigns variable the X-coordinate of an object or cel") ;
         signatures.put("letobjecty","letobjectx(variable,target) ; Assigns variable the Y-coordinate of an object or cel") ;
         signatures.put("letfix","letfix(variable,target) ;	Assigns variable with flex value of object or cel") ;
         signatures.put("letmapped","letmapped(variable,target) ;	Assigns variable a non-zero value if the object or cel is visibile") ;
         signatures.put("letset","letset(variable) ; Assigns variable the current page set number") ;
         signatures.put("letpal","letpal(variable[,celname]) ; Assigns variable the current palette number") ;
         signatures.put("letmousex","letmousex(variable) ; Assigns variable the current mouse x-coordinate") ;
         signatures.put("letmousey","letmousey(variable) ; Assigns variable the current mouse y-coordinate") ;
         signatures.put("letcatch","letcatch(variable) ; Assigns variable the object number currently selected") ;
         signatures.put("letcollide","letcollide(variable,target,target) ; Assigns variable non-zero if cels in target are touching") ;
         signatures.put("letinside","letinside(variable,target,target) ; Assigns variable non-zero if target bounding boxes overlap") ;
         signatures.put("lettransparent","lettransparent(variable,target) ; Assigns variable current target transparency") ;
         signatures.put("ifequal","ifequal(operand1,operand2) ; Logical if statement true if operands are equal") ;
         signatures.put("ifnotequal","ifnotequal(operand1,operand2) ; Logical if statement true if operands are not equal") ;
         signatures.put("ifgreaterthan","ifgreaterthan(operand1,operand2) ; Logical if statement true if operand1 is greater than operand2") ;
         signatures.put("iflessthan","iflessthan(operand1,operand2) ; Logical if statement true if operand1 is less than operand2") ;
         signatures.put("else","else() ; If statement optional part") ;
         signatures.put("endif","endif() ; If statement block end") ;
         signatures.put("ghost","ghost(target,integer) ;  Sets the target unresponsive to the mouse") ;
         signatures.put("letobjectw","letobjectw(variable,target) ; Assigns variable the width of an object or cel") ;
         signatures.put("letobjecth","letobjecth(variable,target) ; Assigns variable the height of an object or cel") ;
	 		signatures.put("while","while(operand1[,operand2[,condition]]) ; While statement loops while operand1 is non-zero") ;
         signatures.put("endwhile","endwhile (control) ; While statement end, control variable matches while operand1") ;
         signatures.put("showstatus","showstatus (string[,string,string,...]) ; Shows concatenated text in status bar") ;
         signatures.put("letcel","letcel(variable) ; Assigns variable the cel name currently selected") ;	
         signatures.put("letcomment","letcomment(variable,celname) ; Assigns variable the cel comment text in CNF") ;
         signatures.put("concat","concat(variable,operand1,operand2, ...) ; Assigns variable a concatenated string") ;
         signatures.put("substr","substr(variable,source,start[,end]) ; Assigns variable the substring in the source from start to end") ;
         signatures.put("animate","animate(target,period) ; Sets the target frames to animate at the specified period") ;
         signatures.put("for","for(control,start,end[,increment]) ; Loop statement iterates using the control variable") ;
         signatures.put("next","next(control) ; Loop terminator statement begins next iteration") ;
         signatures.put("movie","movie(filename[,repeat]) ; Plays a video file with optional repeat count") ; 
	      signatures.put("letinitx","letinitx(variable,target) ; Initial x ordinate of cel or object") ;
         signatures.put("letinity","letinity(variable,target) ; Initial y ordinate of cel or object") ;
         signatures.put("letwidth","letwidth(variable,target) ; Visible width of cel or object on current page") ;
         signatures.put("letheight","letheight(variable,target) ; Visible height of cel or object on current page") ;
         signatures.put("letframe","letframe(variable,target) ; Return current frame of cel group or object group") ;
         signatures.put("setframe","setframe(target,integer) ; Set current frame of cel group or object group") ;
         signatures.put("attach","attach(child,parent) ; Attach a child object to a parent object") ;
         signatures.put("detach","detach(child) ; Detaches an object from its parent object") ;
         signatures.put("glue","glue(child,parent) ; Glue a child object to a parent object such that movements do not detach") ;
         signatures.put("letchild","letchild(variable,parent) ; Returns the lowest numbered child object attached to parent object") ;
         signatures.put("letparent","letparent(variable,child) ; Returns the number of the child's parent object") ;
         signatures.put("letsibling","letsibling(variable,child) ; Returns the next lowest numbered child object attached to parent object") ;
         signatures.put("letkey","letkey(variable,keystring) ; Returns the index of pressed key in list, starting from 1") ;
         signatures.put("letkeymap","letkeymap(variable,keystring) ; Returns binary map of simultaneous key indexes of pressed keys") ;
         signatures.put("letkeychar","letkeychar(variable) ; Returns the last key pressed character representation") ; 
         signatures.put("letkeycode","letkeycode(variable) ; Returns the virtual key code for the last character pressed") ;
         signatures.put("letkeymodifier","letkeymodifier(variable) ; Returns the last key typed modifier characters (shift,alt,ctrl)") ;
         signatures.put("letkeystring","letkeystring(variable) ; Returns the last line of text that was typed") ;
         signatures.put("mediaplayer","mediaplayer(filename) ; Invokes the media player for the specified file") ;
         signatures.put("elseifequal","elseifequal(operand1,operand2) ; Logical if statement Else variant (equal)");
         signatures.put("elseifnotequal","elseifnotequal(operand1,operand2) ; Logical if statement Else variant (notequal)") ;
         signatures.put("elseifgreaterthan","elseifgreaterthan(operad1,operand2) ; Logical if statement Else variant (greaterthan)") ; 
         signatures.put("elseiflessthan","elseiflessthan(operand1,operand2) ; Logical if statement Else variant (lessthan)") ;
         signatures.put("clone","clone(variable,target) ; Returns the object number of a new cloned object") ;
         signatures.put("destroy","destroy(target) ; Destroys the target object") ;
         signatures.put("letlevel","letlevel(variable,target) ; Returns the draw level of the target cel or object") ;
         signatures.put("setlevel","setlevel(target,integer) ; Sets the target cel or object draw level") ;
         signatures.put("indexof","indexof(variable,string,substring) ; Returns index of substring in string") ;
         signatures.put("replacestr","replacestr(variable,string,substring,replacestr) ; Returns a new string after replacing a substring in string") ;
         signatures.put("restrictx","restrictx(target,minX,maxX) ; Restrict object movement in X direction") ;
         signatures.put("restricty","restricty(target,minY,maxY) ; Restrict object movement in Y direction") ;
         signatures.put("lettimer","lettimer(variable,alarmname) ; Returns the remaining time on the specified alarm") ;
         signatures.put("setpal","setpal(target,integer) ; Sets the target cel or group fixed palette group") ;
         signatures.put("letkcf","letkcf(variable,target) ; Returns the target cel palette file number") ;
         signatures.put("setkcf","setkcf(target,integer) ; Sets the target cel or group palette file") ;
         signatures.put("viewer","viewer(command,arg1,arg2,...) Viewer specific commands") ;
    		signatures.put("strlen","strlen(variable,string) ; Returns the length of a string") ;
         signatures.put("getText","getText(variable,component) ; Get component text") ;
         signatures.put("setText","setText(component,string) ; Set component text") ;
         signatures.put("getSelected","getSelected(variable,component) ; Get component selection state") ;
         signatures.put("setSelected","setSelected(component,boolean) ; Set component selection state") ;
         signatures.put("getValueAt","getValueAt(variable,component,index) ; Get component list value") ;
         signatures.put("setValueAt","setValueAt(component,index,string) Set component list value") ;
         signatures.put("addItem","addItem(component,string) ; Add component list or combobox item") ;
         signatures.put("removeItem","removeItem(component,string) ; Remove component list or combobox item") ;
         signatures.put("getSelectedIndex","getSelectedIndex(variable,component) ; Get index of selected list item") ;
         signatures.put("setSelectedIndex","setSelectedIndex(component,integer) ; Set index of selected list item") ;
         signatures.put("getSelectedValue","getSelectedValue(variable,component) ; Get component selected value") ;
         signatures.put("setSelectedValue","setSelectedValue(component,string) ; Set component selection value") ;
         signatures.put("getSelectedItem","getSelectedItem(variable,component) ; Get combobox selected item") ;
         signatures.put("setSelectedItem","setSelectedItem(component,string) ; Set combobox selected item") ;
         signatures.put("getIndexOf","getIndexOf(variable,component,string) ; Get index of string item in list") ;
         signatures.put("removeAll","removeAll(component) ; Remove all list items") ;
         signatures.put("getItemCount","getItemCount(variable,component) ; Get number of list items") ;
         signatures.put("getEnabled","getEnabled(variable,component) ; Get the enable state of the component") ;
         signatures.put("setEnabled","setEnabled(component,boolean) ; Enable the component") ;
         signatures.put("getNextSelectedIndex","getNextSelectedIndex(variable,component) ; Get the next selected index") ;
         signatures.put("setAttributes","setAttributes(component,string[,temporary]) ; Set the component attributes") ;
         signatures.put("open","open(status,filename,mode,[decode]) ; Open a file") ;
         signatures.put("read","read(status,filename,variable,line) ; Read a line in a file") ;
         signatures.put("write","write(status,filename,string) ; Write a line in a file") ;
         signatures.put("close","close(status,filename,[commit,encode]) ; Close a file with commit") ;
         signatures.put("edit","edit(status,filename,command,arg1,arg2,...) ; Edit a file") ;
         signatures.put("environment","environment(variable,command,[arg1,arg2,...]) ; A generic system request") ;
         signatures.put("confirm","confirm(variable,string1,string2,...) ; Show a modal confirm dialog") ;
         signatures.put("setmodal","setmodal([target]) ; Set the modal event object source") ;
         signatures.put("letmodal","letmodal(variable) ; Returns the modal object source") ;
         signatures.put("event","event(eventname,param1,param2,...) ; Fire an existing event") ;
         signatures.put("paint","paint([delay]) ; Paint the current screen after delay") ;
         signatures.put("wait","wait(target[,delay]) ; Wait for a notify lock on the target") ; 
         signatures.put("signal","signal(target) ; Notify queued activities waiting on the target") ;
         signatures.put("sleep","sleep(delay) ; Sleep for the specified number of milliseconds") ;
         signatures.put("mouseRelease","mouseRelease() ; Force a mouse release") ;
         signatures.put("sqrt","sqrt(variable,number) ; Calculates the square root of a number") ;
         signatures.put("repeat","repeat(labelname,count,iterator) ; Repeat a gosub 'count' times") ;
         signatures.put("valuepool","valuepool(poolname) ; Establish the default properties pool") ;
         signatures.put("loadvalue","loadvalue(variable,property) ; Load a [poolname.]property value") ; 
         signatures.put("savevalue","savevalue(property,value) ; Set a [poolname.]property variable") ;
         signatures.put("deletevalue","deletevalue(property) ; Remove a [poolname.]property variable") ;
         signatures.put("exitloop","exitloop() ; Exit the current Repeat loop") ;
         signatures.put("letmaxpage","letmaxpage(variable) ; Return the maximum pages defined") ;
         signatures.put("letmaxcolor","letmaxcolor(variable) ; Return the maximum color sets defined") ;
         signatures.put("getAttributes","getAttributes(variable,component[,string]) ; Get the component attributes") ;
         signatures.put("math","math(variable,function,operand1,operand2,...) ; Math function library") ;
         signatures.put("format","format(variable,number,maxfraction,minfraction,maxint,minint) ; Decimal format function") ;
         signatures.put("letcloned","format(variable,object) ; Returns the source objecy number if cloned") ;
         signatures.put("letaudio","format(variable) ; Returns the last sound name played") ;
         signatures.put("setPage","setPage(component,string) ; Set text pane component url") ;
      }
   }


	// Object state change methods
	// ---------------------------

	// Place a list of events on the event handler queue.  This method must
	// be synchronized to notify the thread activity.  Each event has an
   // associated execution thread and a source object.  Alarms are placed
   // through this list process.

	static void queueEvents(Vector v, Thread t, Object source)
	{
		if (v == null) return ;
      if (v.size() == 0) return ;

      synchronized (eventlock)
      {
         Collections.sort(v, new EventHandlerOrder()) ;
   		for (int i = 0 ; i < v.size() ; i++)
         {
            Object [] qentry = new Object[3]  ;
            qentry[0] = v.elementAt(i) ;
            qentry[1] = t ;
            qentry[2] = source ;
			   queue.addElement(qentry) ;
      		if (OptionsDialog.getDebugEvent())
            {
               Object o = qentry[0] ;
               if (o instanceof FKissEvent && !((FKissEvent) o).getNoBreakpoint())
               {
                  long time = System.currentTimeMillis() - Configuration.getTimestamp() ;  
                  FKissEvent evt = (FKissEvent) qentry[0] ;
                  o = evt.getParent() ;
                  Long n = (o instanceof Alarm) ? ((Alarm) o).getTriggeredTime() : 0 ;
                  if (n == 0)
                     n = n ;
                  if (n > 0) n -= Configuration.getTimestamp() ;
                  String s = (n != 0) ? " triggered time " + n.toString() : "" ;
                  System.out.println("[" + time + "] [" + Thread.currentThread().getName() + "] queue " + evt.getName() + " on EventHandler, source " + qentry[2] + " queue size " + queue.size() + s) ;
                  stats.put(qentry[0],new Long(System.currentTimeMillis())) ;  // Java 1.5
               }
            }
            if (!OptionsDialog.getMultipleEvents()) break ;
         }
         eventlock.notify() ;
      }
	}

	// Place a single event on the event handler queue.  This method must
	// be synchronized to notify the thread activity.  Each event has an
   // associated execution thread.

	static void queueEvent(FKissEvent event, Thread t, Object source)
	{
		if (event == null) return ;

      synchronized (eventlock)
      {
         Object [] qentry = new Object[3]  ;
         qentry[0] = event ;
         qentry[1] = t ;
         qentry[2] = source ;
		   queue.addElement(qentry) ;
     		if (OptionsDialog.getDebugEvent())
         {
            Object o = qentry[0] ;
            if (o instanceof FKissEvent && !((FKissEvent) o).getNoBreakpoint())
            {
               long time = System.currentTimeMillis() - Configuration.getTimestamp() ;  
               FKissEvent evt = (FKissEvent) qentry[0] ;
               o = evt.getParent() ;
               Long n = (o instanceof Alarm) ? ((Alarm) o).getTriggeredTime() : 0 ;
               if (n > 0) n -= Configuration.getTimestamp() ;
               String s = (n != 0) ? " triggered time " + n.toString() : "" ;
               System.out.println("[" + time + "] [" + Thread.currentThread().getName() + "] queue single " + evt.getName() + " on EventHandler, source " + qentry[2] + " queue size " + queue.size() + s) ;
               stats.put(qentry[0],new Long(System.currentTimeMillis())) ; // Java 1.5
            }
         }
     		eventlock.notify() ;
      }
	}


	// Remove an event from the event handler queue.  The event object is a
   // two element array that contains the FKissEvent and its invokation
   // Thread.

	static Object dequeueEvent()
	{
      Object o ;
      synchronized (eventlock)
      {
	   	if (queue.size() == 0) return null ;
         Collections.sort(queue, new EventHandlerOrder());
	   	o = queue.elementAt(0) ;
	   	queue.removeElement(o) ;
     		eventlock.notify() ;
      }
   	return o ;
	}


	// Set the panel frame for event actions that draw to the screen.

	static void setPanelFrame(PanelFrame f) { panel = f ; }


	// Set the modal object that is the valid source for events.

	static void setModal(Object k) { modal = k ; }



	// Object utility methods
	// ----------------------

	// Static method to determine if a string is a valid event name.

	static int getEventNameKey(String s)
	{
      if (s == null) return -1 ;
		for (int i = 0 ; i < definedEvents.length ; i++)
			if (s.equals(definedEvents[i])) return i ;
		return -1 ;
	}

	// Static method to determine if a string is a valid action name.

	static int getActionNameKey(String s)
	{
      if (s == null) return -1 ;
		for (int i = 0 ; i < definedActions.length ; i++)
			if (s.equals(definedActions[i])) return i ;
		return -1 ;
	}

	// Static method to determine an event FKiSS specification level.

	static int getEventFKissLevel(int n)
	{
      if (n < 0 || n >= eventfkisslevel.length) return -1 ;
      return (int) eventfkisslevel[n] ;
	}

	// Static method to determine an action FKiSS specification level.

	static int getActionFKissLevel(int n)
	{
      if (n < 0 || n >= actionfkisslevel.length) return -1 ;
      return (int) actionfkisslevel[n] ;
	}

	// Static method to determine mandatory number of event parameters.

	static int getMandatoryEventParams(int n)
	{
      if (n < 0 || n >= eventmandatoryparams.length) return -1 ;
      return (int) eventmandatoryparams[n] ;
	}

	// Static method to determine mandatory number of action parameters.

	static int getMandatoryActionParams(int n)
	{
      if (n < 0 || n >= actionmandatoryparams.length) return -1 ;
      return (int) actionmandatoryparams[n] ;
	}

	// Static method to determine an event valid parameter type.

	static int getEventParamType(int eventcode, int paramnum)
	{
      if (OptionsDialog.getCompatibilityMode() == null)
      {
         if (paramnum < 0 || paramnum >= eventparamukiss.length) return -1 ;
         if (eventcode < 0 || eventcode >= eventparamukiss[paramnum].length) return -1 ;
         return eventparamukiss[paramnum][eventcode] ;
      }
      else if ("DirectKiss".equals(OptionsDialog.getCompatibilityMode()))
      {
         if (paramnum < 0 || paramnum >= eventparamdkiss.length) return -1 ;
         if (eventcode < 0 || eventcode >= eventparamdkiss[paramnum].length) return -1 ;
         return eventparamdkiss[paramnum][eventcode] ;
      }
      else if ("PlayFKiss".equals(OptionsDialog.getCompatibilityMode()))
      {
         if (paramnum < 0 || paramnum >= eventparampkiss.length) return -1 ;
         if (eventcode < 0 || eventcode >= eventparampkiss[paramnum].length) return -1 ;
         return eventparampkiss[paramnum][eventcode] ;
      }
      else
      {
         if (paramnum < 0 || paramnum >= eventparamdkiss.length) return -1 ;
         if (eventcode < 0 || eventcode >= eventparamdkiss[paramnum].length) return -1 ;
         return eventparamdkiss[paramnum][eventcode] ;
      }
	}

	// Static method to determine an action valid parameter type.  As UltraKiss allows
   // unlimited parameters for some action commands, the last parameter type for an
   // action command is propagated for all remaining parameters.

	static int getActionParamType(int actioncode, int paramnum)
	{
      if (OptionsDialog.getCompatibilityMode() == null)
      {
         if (paramnum < 0 || paramnum >= actionparamukiss.length) return -1 ;
         if (actioncode < 0 || actioncode >= actionparamukiss[paramnum].length) return -1 ;
         return actionparamukiss[paramnum][actioncode] ;
      }
      else if ("DirectKiss".equals(OptionsDialog.getCompatibilityMode()))
      {
         if (paramnum < 0 || paramnum >= actionparamdkiss.length) return -1 ;
         if (actioncode < 0 || actioncode >= actionparamdkiss[paramnum].length) return -1 ;
         return actionparamdkiss[paramnum][actioncode] ;
      }
      else if ("PlayFKiss".equals(OptionsDialog.getCompatibilityMode()))
      {
         if (paramnum < 0 || paramnum >= actionparampkiss.length) return -1 ;
         if (actioncode < 0 || actioncode >= actionparampkiss[paramnum].length) return -1 ;
         return actionparampkiss[paramnum][actioncode] ;
      }
      else
      {
         if (paramnum < 0 || paramnum >= actionparamdkiss.length) return -1 ;
         if (actioncode < 0 || actioncode >= actionparamdkiss[paramnum].length) return -1 ;
         return actionparamdkiss[paramnum][actioncode] ;
      }
	}


	// Static method to determine if a string is prefixed by a valid action
   // name. If it is we return the action name, otherwise we return the
   // original string.  We also remove preceeding '@' signs.

	static String findPartialActionName(String s)
	{
      if (s == null) return s ;
      if (s.charAt(0) == '@') s = s.substring(1) ;
      String s1 = s.toLowerCase() ;
		for (int i = 0 ; i < definedActions.length ; i++)
			if (s1.startsWith(definedActions[i])) return definedActions[i] ;
		return s ;
	}

	// Static method to determine an event or action signature string.

	static String findSignature(String s)
	{
      if (signatures == null) return null ;
		Object o = signatures.get(s) ;
      if (o != null) return o.toString() ;
      return null ;
	}


   // Static method to determine if an action command is a timer command.
   // If it is we return the correct alarm parameter depending on the command.

   static String getTimerAlarmParam(FKissAction a)
   {
      if (a == null) return null ;
      int code = a.getCode() ;
      if (code == 4 || code == 7 || code == 5 || code == 8 || code == 6 || code == 9)
          return a.getSecondParameter() ;
      if (code == 22 || code == 26)
         return a.getFirstParameter() ;
      return null ;
   }


   // Static method to determine if an action command is an if command.
   // If commands should be paired with else() or endif() actions.

   static boolean isIfAction(FKissAction a)
   {
      if (a == null) return false ;
      int code = a.getCode() ;
      if (code == 55 || code == 56 || code == 57 || code == 58) return true ;
      return false ;
   }


   // Static method to determine if an action command is an endif command.

   static boolean isEndIfAction(FKissAction a)
   {
      if (a == null) return false ;
      int code = a.getCode() ;
      if (code == 60) return true ;
      return false ;
   }


   // Static method to determine if an action command is an iteration command.
   // These are 'for' and 'while' commands.

   static boolean isIterationAction(FKissAction a)
   {
      if (a == null) return false ;
      int code = a.getCode() ;
      if (code == 64 || code == 72) return true ;
      return false ;
   }


   // Static method to determine if an action command is an end of iteration
   // command.  These are 'next' and 'endwhile' commands.

   static boolean isEndIterationAction(FKissAction a)
   {
      if (a == null) return false ;
      int code = a.getCode() ;
      if (code == 65 || code == 73) return true ;
      return false ;
   }


   // Static methods to determine if an action command is an else or elseif
   // or endif command.

   static boolean isElseEndAction(FKissAction a)
   {
      if (a == null) return false ;
      int code = a.getCode() ;
		if (code == 94 || code == 95 || code == 96 || code == 97
			|| code == 59 || code == 60) return true ;
      return false ;
   }

   static boolean isElseEndAction(String s)
   {
      if (s == null) return false ;
      if (s.startsWith("else") || "endif".equals(s)) return true ;
      return false ;
   }


   // Static methods to determine if an event is a begin() or initialize()
   // event.

   static boolean isBeginningEvent(FKissEvent e)
   {
      if (e == null) return false ;
      int code = e.getCode() ;
		if (code == 2 || code == 9) return true ;
      return false ;
   }



	// Static method to directly fire a series of events without queuing them.
   // The event is associated with the indicated thread.  Note that multiple
   // events will always be fired for begin() and initialize() events.  These
   // are required so as to uniquely initialize each configuration.
   //
   // If multiple events are not permitted (MultipleEvents option is false)
   // then event definitions from a prior configuration replace event
   // declarations from an earlier configuration.  This provides the ability
   // to correct code by developing a wrapper set that loads an earlier set 
   // version through a viewer("menu","appendcnf") command.

	static void fireEvents(Vector v, PanelFrame f, Thread t, Object source)
	{
   	FKissEvent event = null ;
		if (v == null) return ;
      
      try
      {
			for (int i = 0 ; i < v.size() ; i++)
         {
         	event = (FKissEvent) v.elementAt(i) ;
            if (isBeginningEvent(event))
               System.out.println("EventHandler: firing event " + event + " actions " + event.getActionList().size());
				event.fireEvent(f,t,source) ;
            if (!OptionsDialog.getMultipleEvents() && !isBeginningEvent(event)) break ;            
         }
      }

      // Watch for memory faults.  If we run low on memory invoke
      // the garbage collector and wait for it to run.  Close the
      // configuration.

		catch (OutOfMemoryError e)
		{
         suspendEventHandler(true) ;
			Runtime.getRuntime().gc() ;
         try { Thread.currentThread().sleep(300) ; }
         catch (InterruptedException ex) { }
			System.out.println("EventHandler: Out of memory.") ;
         MainFrame mf = Kisekae.getMainFrame() ;
			mf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
         JOptionPane.showMessageDialog(mf,
            Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted"),
            Kisekae.getCaptions().getString("LowMemoryFault"),
            JOptionPane.ERROR_MESSAGE) ;
		   if (mf != null)
         {
            resumeEventHandler(true) ;
            mf.closeconfig() ;
         }
		}

      // Watch for stack overflow.

      catch (StackOverflowError e)
      {
			System.out.println("EventHandler: " + e.toString()) ;
         JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
            Kisekae.getCaptions().getString("StackOverflowFault") + " - " +
            Kisekae.getCaptions().getString("ActionNotCompleted") +
            "\n" + e.getMessage(),
            Kisekae.getCaptions().getString("StackOverflowFault"),
            JOptionPane.ERROR_MESSAGE) ;
         e.printStackTrace() ;
         if (panel != null) panel.showpage() ;
      }

		// Watch for internal faults.  Close the configuration.

		catch (Throwable e)
		{
         stopEventHandler() ;
         e.printStackTrace() ;
         FKissAction action = (event != null) ? event.getCurrentAction() : null ;
         String s1 = (event == null) ? "unknown" : event.toString() ;
         String s2 = (action == null) ? "unknown" : action.toString() ;
         int currentline = (action == null) ? 0 : action.getLine() ;
         String s = "EventHandler: Internal fault,  event " + s1 ;
	      System.out.println(s) ;
         String s3 = "Failing action " + s2 ;
         System.out.println(s3) ;
         s += "\n" + s3 ;
         s3 = "Configuration source line " + currentline ;
         System.out.println(s3) ;
         s3 = "Exception " + e.toString() ;
         System.out.println(s3) ;

         // Build the language sensitive display string.

         s = Kisekae.getCaptions().getString("InternalError")
             + " - " + Kisekae.getCaptions().getString("ActionNotCompleted")
             + "\n" + e.toString() ;
         s1 = (event == null)
             ? Kisekae.getCaptions().getString("UnknownValueText") : event.toString() ;
         s += "\n" + Kisekae.getCaptions().getString("EventText") + " " + s1 ;
         s2 = (action == null)
             ? Kisekae.getCaptions().getString("UnknownValueText") : action.toString() ;
         s += "\n" + Kisekae.getCaptions().getString("ActionText") + " " + s2 ;
         s += "\n" + Kisekae.getCaptions().getString("LineText") + " " + currentline ;

         // Catch the stack trace.

         try
         {
            File tf = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(tf) ;
            PrintStream ps = new PrintStream(os) ;
            e.printStackTrace(ps) ;
            os.close() ;
            FileReader is = new FileReader(tf) ;
            LineNumberReader lr = new LineNumberReader(is) ;
            s1 = lr.readLine() ;
            s1 = lr.readLine() ;
            int traceline = 0 ;
            while (s1 != null)
            {
               s += "\n" + s1.trim() ;
               s1 = lr.readLine() ;
               if (traceline++ > 5) break ;
            }
         }
         catch (EOFException eof) { }
         catch (Exception ex) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(Kisekae.getMainFrame(), s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         if (panel != null) panel.quit() ;
         panel = null ;
		}
	}


	// Static method to clear the queue.  This is used when we switch to a
	// new page and start a new animation sequence.

	static void clearEventQueue()
   { 
      synchronized (eventlock) 
      { 
         queue.removeAllElements() ; 
     		eventlock.notify() ;
      } 
   }



	// Event Handler methods
	// ---------------------

	// Start the handler.  This creates a new thread.  The thread
	// suspends itself and waits to be resumed by the timer activity
	// when a new event is placed on the queue.  All event handler
   // threads are placed within the same thread group.

	void startEventHandler()
	{
   	stop = false ;
  		createtime = Configuration.getTimestamp() ;
		if (OptionsDialog.getDebugControl())
			System.out.println("Start EventHandler.") ;
      if (threadgroup == null || threadgroup.isDestroyed())
      {
      	threadgroup = new ThreadGroup("EventHandler") ;
         threadgroup.setDaemon(true) ;
      }
		thread = new Thread(threadgroup,this) ;
 		thread.start() ;
      handlers.add(this) ;
	}


	// Static method to stop all event handler threads.  This will interrupt
   // all threads within the event handler group and release all resources.

	static void stopEventHandler()
	{
      int i = 0 ;
      stop = true ;
		if (OptionsDialog.getDebugControl())
			System.out.println("Stop EventHandler.") ;

      try
      {
         clearEventQueue() ;
         handlers.removeAllElements() ;
         synchronized (eventlock) { eventlock.notifyAll() ; }

         // Wait for the thread group destruction.

         while (i < 5 && threadgroup != null)
         {
   	      try { Thread.currentThread().sleep(100) ; }
   	      catch (InterruptedException e) { }
   	      if (threadgroup.isDestroyed()) break ;
            i++ ;
         }

         // Applets do not seem to destroy daemon threadgroups when all threads
         // terminate.

         if (i == 5 && threadgroup != null)
         {
            threadgroup.destroy() ;
   	      try { Thread.currentThread().sleep(100) ; }
   	      catch (InterruptedException e) { }
            if (threadgroup.isDestroyed()) i = 0 ;
         }
      }
      catch (Exception e)
      {
//         System.out.println("EventHandler: Thread termination " + e);
      }

      // Release critical references.

      threadgroup = null ;
//      if (i == 5)
//         System.out.println("EventHandler: Thread termination timeout.");
	}


   // Static method to return an enumeration of the queue contents.

   static Enumeration getQueue()
   { return (queue == null) ? null : queue.elements() ; }


   // Static method to count the number of events on the queue.

   static int getQueueSize() { return queue.size() ; }


   // Static method to return the event handler threads.

   static Vector getThreads() { return handlers ; }


   // Static method to return the modal event source.

   static Object getModal() { return modal ; }


   // Static method to return the modal state.

   static boolean isModal() { return (modal != null) ; }


   // Static method to return the global EventHandler active state.

   static boolean isActive() { return !(suspend || stop) ; }


   // Static method to return the global EventHandler stop state.

   static boolean getStop() { return stop ; }


   // Method to return this event handler name.

   String getName() { return threadname ; }


   // Function to return this EventHandler thread state.

   String getState()
   {
      if (suspend) return Kisekae.getCaptions().getString("AlarmSuspendState") ;
   	if (wait) return Kisekae.getCaptions().getString("AlarmWaitState") ;
      if (active) return Kisekae.getCaptions().getString("AlarmActiveState") ;
      return Kisekae.getCaptions().getString("AlarmStopState") ;
   }


   // Method to return the number of events fired by this thread.

   long getCount() { return firecount ; }


	// Static method to suspend the event handler.   Suspension causes all
   // threads in the event handler to enter the wait state.  One must be
   // notified to restart execution.  The suspend flag is global to all
   // instances of the event handler.

	static void suspendEventHandler(){ suspendEventHandler(false) ; }
	static void suspendEventHandler(boolean manual)
   {
		if (OptionsDialog.getDebugControl() && !manualsuspend)
			System.out.println("Suspend EventHandler. " + ((manual) ? "Manual" : "")) ;
   	if (manual) manualsuspend = true ;
   	suspend = true ;
   }


	// Static method to resume the handler activity.  This method is
   // synchronized on the queue object because it must notify an independent
   // thread activity.

	static void resumeEventHandler() {resumeEventHandler(false) ; }
	static void resumeEventHandler(boolean manual)
	{
   	if (manual) manualsuspend = false ;
      if (manualsuspend) return ;
      synchronized (eventlock)
      {
			if (OptionsDialog.getDebugControl())
				System.out.println("Resume EventHandler.") ;
   		suspend = false ;
		   eventlock.notify() ;
      }
	}


	// Kiss object abstract method implementation.

	int write(FileWriter fw, OutputStream out, String type) throws IOException
	{ return -1 ; }



	// Event Handler execution thread
	// ------------------------------

	public void run()
	{
   	active = true ;
      FKissEvent event = null ;
      Thread thread = null ;
      Object source = null ;
      Thread me = Thread.currentThread() ;
		me.setPriority(Thread.NORM_PRIORITY) ;
		me.setName(threadname) ;
		if (OptionsDialog.getDebugControl())
			System.out.println(me.getName() + " started.") ;

		// The handler suspends itself until it is notified.  Once
		// notified the handler performs all event actions on the queue
		// and then suspends itself again.

		try
      {
			while (!stop)
			{
         	try
            {
					synchronized (eventlock)
					{
						while ((queue.size() == 0 || suspend) && !stop)
						{
							wait = true ;
							eventlock.wait() ;
						}
					}

					// Fire the event.  We can wake up with an empty queue.
	            // We yield control after every event.

					wait = false ;
               if (stop) break ;
	 	 			Object qentry = EventHandler.dequeueEvent() ;
					if (qentry != null)
					{
						Object [] queueobject = (Object []) qentry ;
						event = (FKissEvent) queueobject[0] ;
						thread = (Thread) queueobject[1] ;
						source = (Object) queueobject[2] ;
                  if (OptionsDialog.getDebugEvent())
                  {
                     if (event != null && !event.getNoBreakpoint())
                     {
                        long time = System.currentTimeMillis() - Configuration.getTimestamp() ;  
                        Object o = stats.get(event) ;
                        long diff = (o instanceof Long) ? (System.currentTimeMillis() - ((Long) o).longValue()) : 0 ; // Java 1.5
                        System.out.println("[" + time + "] [" + Thread.currentThread().getName() + "] fire " + event.getName() + " for FKissEvent processing, wait on queue " + diff + " ms") ;
                        stats.remove(event) ;
                     }
                  }
						if (event != null) event.fireEvent(panel,thread,source) ;
	               firecount++ ;
	               me.yield() ;
		         }
            }

		      // Watch for memory faults.  If we run low on memory invoke
		      // the garbage collector and wait for it to run.  Close the
		      // configuration.

				catch (OutOfMemoryError e)
				{
		         suspendEventHandler(true) ;
					Runtime.getRuntime().gc() ;
		         try { Thread.currentThread().sleep(300) ; }
		         catch (InterruptedException ex) { }
					System.out.println("EventHandler: Out of memory.") ;
               MainFrame mf = Kisekae.getMainFrame() ;
     				mf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) ;
               JOptionPane.showMessageDialog(mf,
                  Kisekae.getCaptions().getString("LowMemoryFault") + " - " +
                  Kisekae.getCaptions().getString("ActionNotCompleted"),
                  Kisekae.getCaptions().getString("LowMemoryFault"),
                  JOptionPane.ERROR_MESSAGE) ;
     			   if (mf != null)
               {
                  resumeEventHandler(true) ;
                  mf.closeconfig() ;
               }
				}

		      // Watch for stack overflow.

		      catch (StackOverflowError e)
		      {
					System.out.println("EventHandler: " + e.toString()) ;
               JOptionPane.showMessageDialog(Kisekae.getMainFrame(),
                  Kisekae.getCaptions().getString("StackOverflowFault") + " - " +
                  Kisekae.getCaptions().getString("ActionNotCompleted") +
                  "\n" + e.getMessage(),
                  Kisekae.getCaptions().getString("StackOverflowFault"),
                  JOptionPane.ERROR_MESSAGE) ;
               e.printStackTrace() ;
		         if (panel != null) panel.showpage() ;
		      }
			}
      }

      // Watch for an interrupt as this signals thread termination.

      catch (InterruptedException e) { }

		// Watch for internal faults.  Close the configuration.

		catch (Throwable e)
		{
         stopEventHandler() ;
         e.printStackTrace() ;
         FKissAction action = (event != null) ? event.getCurrentAction() : null ;
         String s1 = (event == null) ? "unknown" : event.toString() ;
         String s2 = (action == null) ? "unknown" : action.toString() ;
         int currentline = (action == null) ? 0 : action.getLine() ;
         String s = "EventHandler: Internal fault,  event " + s1 ;
	      System.out.println(s) ;
         String s3 = "Failing action " + s2 ;
         System.out.println(s3) ;
         s += "\n" + s3 ;
         s3 = "Configuration source line " + currentline ;
         System.out.println(s3) ;
         s3 = "Exception " + e.toString() ;
         System.out.println(s3) ;

         // Build the language sensitive display string.

         s = Kisekae.getCaptions().getString("InternalError")
             + " - " + Kisekae.getCaptions().getString("ActionNotCompleted")
             + "\n" + e.toString() ;
         s1 = (event == null)
             ? Kisekae.getCaptions().getString("UnknownValueText") : event.toString() ;
         s += "\n" + Kisekae.getCaptions().getString("EventText") + " " + s1 ;
         s2 = (action == null)
             ? Kisekae.getCaptions().getString("UnknownValueText") : action.toString() ;
         s += "\n" + Kisekae.getCaptions().getString("ActionText") + " " + s2 ;
         s += "\n" + Kisekae.getCaptions().getString("LineText") + " " + currentline ;

         // Catch the stack trace.

         try
         {
            File tf = File.createTempFile("Kisekae","debug") ;
            OutputStream os = new FileOutputStream(tf) ;
            PrintStream ps = new PrintStream(os) ;
            e.printStackTrace(ps) ;
            os.close() ;
            FileReader is = new FileReader(tf) ;
            LineNumberReader lr = new LineNumberReader(is) ;
            s1 = lr.readLine() ;
            s1 = lr.readLine() ;
            int traceline = 0 ;
            while (s1 != null)
            {
               s += "\n" + s1.trim() ;
               s1 = lr.readLine() ;
               if (traceline++ > 5) break ;
            }
         }
         catch (EOFException eof) { }
         catch (Exception ex) { s += "\n" + "Stack trace unavailable." ; }

         JOptionPane.showMessageDialog(Kisekae.getMainFrame(), s,
            Kisekae.getCaptions().getString("InternalError"),
            JOptionPane.ERROR_MESSAGE) ;
         if (panel != null) panel.quit() ;
         panel = null ;
		}

      // Clear critical resource references.

      this.thread = null ;
      this.active = false ;
      this.wait = false ;
      firecount = 0 ;
		if (OptionsDialog.getDebugControl())
	      System.out.println(me.getName() + " terminated.");
	}
}
