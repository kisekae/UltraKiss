/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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