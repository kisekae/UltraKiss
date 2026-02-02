// Title:        Kisekae UltraKiss
// Version:      5.0 (December 25, 2025)
// Copyright:    Copyright (c) 2002-2025
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

package com.wmiles.kisekaeultrakiss.WebSocket;

/**
 *
 * @author william
 */
import java.util.HashMap;
import java.util.Map;

public class CursorNameMapper {

    private static final Map<String, String> javaCursorTojsStyleMap = new HashMap<>();

    static {
        // Alphbetic lower case keys
        javaCursorTojsStyleMap.put("Default Cursor", "default");
        javaCursorTojsStyleMap.put("Crosshair Cursor", "crosshair");
        javaCursorTojsStyleMap.put("Text Cursor", "text");
        javaCursorTojsStyleMap.put("Wait Cursor", "wait");
        javaCursorTojsStyleMap.put("SW Resize Cursor", "sw-resize");
        javaCursorTojsStyleMap.put("SE Resize Cursor", "se-resize");
        javaCursorTojsStyleMap.put("NW Resize Cursor", "nw-resize");
        javaCursorTojsStyleMap.put("NE Resize Cursor", "ne-resize");
        javaCursorTojsStyleMap.put("N Resize Cursor", "n-resize");
        javaCursorTojsStyleMap.put("S Resize Cursor", "s-resize");
        javaCursorTojsStyleMap.put("W Resize Cursor", "w-resize");
        javaCursorTojsStyleMap.put("E Resize Cursor", "e-resize");
        javaCursorTojsStyleMap.put("Hand Cursor", "pointer");
        javaCursorTojsStyleMap.put("Move Cursor", "move");
     }

    public static String getJavascriptCursorStyleCode(String javaCursorName) 
    {
        return javaCursorTojsStyleMap.getOrDefault(javaCursorName, "default");
    }
}