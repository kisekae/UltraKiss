/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wmiles.kisekaeultrakiss.WebSocket;

/**
 *
 * @author william
 */
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyCodeMapper {

    private static final Map<String, Object> jsKeyToJavaKeyCodeMap = new HashMap<>();

    static {
        // Alphbetic lower case keys
        jsKeyToJavaKeyCodeMap.put("a", new KeyClass(KeyEvent.VK_A,false));
        jsKeyToJavaKeyCodeMap.put("b", new KeyClass(KeyEvent.VK_B,false));
        jsKeyToJavaKeyCodeMap.put("c", new KeyClass(KeyEvent.VK_C,false));
        jsKeyToJavaKeyCodeMap.put("d", new KeyClass(KeyEvent.VK_D,false));
        jsKeyToJavaKeyCodeMap.put("e", new KeyClass(KeyEvent.VK_E,false));
        jsKeyToJavaKeyCodeMap.put("f", new KeyClass(KeyEvent.VK_F,false));
        jsKeyToJavaKeyCodeMap.put("g", new KeyClass(KeyEvent.VK_G,false));
        jsKeyToJavaKeyCodeMap.put("h", new KeyClass(KeyEvent.VK_H,false));
        jsKeyToJavaKeyCodeMap.put("i", new KeyClass(KeyEvent.VK_I,false));
        jsKeyToJavaKeyCodeMap.put("j", new KeyClass(KeyEvent.VK_J,false));
        jsKeyToJavaKeyCodeMap.put("k", new KeyClass(KeyEvent.VK_K,false));
        jsKeyToJavaKeyCodeMap.put("l", new KeyClass(KeyEvent.VK_L,false));
        jsKeyToJavaKeyCodeMap.put("m", new KeyClass(KeyEvent.VK_M,false));
        jsKeyToJavaKeyCodeMap.put("n", new KeyClass(KeyEvent.VK_N,false));
        jsKeyToJavaKeyCodeMap.put("o", new KeyClass(KeyEvent.VK_O,false));
        jsKeyToJavaKeyCodeMap.put("p", new KeyClass(KeyEvent.VK_P,false));
        jsKeyToJavaKeyCodeMap.put("q", new KeyClass(KeyEvent.VK_Q,false));
        jsKeyToJavaKeyCodeMap.put("r", new KeyClass(KeyEvent.VK_R,false));
        jsKeyToJavaKeyCodeMap.put("s", new KeyClass(KeyEvent.VK_S,false));
        jsKeyToJavaKeyCodeMap.put("t", new KeyClass(KeyEvent.VK_T,false));
        jsKeyToJavaKeyCodeMap.put("u", new KeyClass(KeyEvent.VK_U,false));
        jsKeyToJavaKeyCodeMap.put("v", new KeyClass(KeyEvent.VK_V,false));
        jsKeyToJavaKeyCodeMap.put("w", new KeyClass(KeyEvent.VK_W,false));
        jsKeyToJavaKeyCodeMap.put("x", new KeyClass(KeyEvent.VK_X,false));
        jsKeyToJavaKeyCodeMap.put("y", new KeyClass(KeyEvent.VK_Y,false));
        jsKeyToJavaKeyCodeMap.put("z", new KeyClass(KeyEvent.VK_Z,false));
        // Alphbetic upper case keys
        jsKeyToJavaKeyCodeMap.put("A", new KeyClass(KeyEvent.VK_A,true));
        jsKeyToJavaKeyCodeMap.put("B", new KeyClass(KeyEvent.VK_B,true));
        jsKeyToJavaKeyCodeMap.put("C", new KeyClass(KeyEvent.VK_C,true));
        jsKeyToJavaKeyCodeMap.put("D", new KeyClass(KeyEvent.VK_D,true));
        jsKeyToJavaKeyCodeMap.put("E", new KeyClass(KeyEvent.VK_E,true));
        jsKeyToJavaKeyCodeMap.put("F", new KeyClass(KeyEvent.VK_F,true));
        jsKeyToJavaKeyCodeMap.put("G", new KeyClass(KeyEvent.VK_G,true));
        jsKeyToJavaKeyCodeMap.put("H", new KeyClass(KeyEvent.VK_H,true));
        jsKeyToJavaKeyCodeMap.put("I", new KeyClass(KeyEvent.VK_I,true));
        jsKeyToJavaKeyCodeMap.put("J", new KeyClass(KeyEvent.VK_J,true));
        jsKeyToJavaKeyCodeMap.put("K", new KeyClass(KeyEvent.VK_K,true));
        jsKeyToJavaKeyCodeMap.put("L", new KeyClass(KeyEvent.VK_L,true));
        jsKeyToJavaKeyCodeMap.put("M", new KeyClass(KeyEvent.VK_M,true));
        jsKeyToJavaKeyCodeMap.put("N", new KeyClass(KeyEvent.VK_N,true));
        jsKeyToJavaKeyCodeMap.put("O", new KeyClass(KeyEvent.VK_O,true));
        jsKeyToJavaKeyCodeMap.put("P", new KeyClass(KeyEvent.VK_P,true));
        jsKeyToJavaKeyCodeMap.put("Q", new KeyClass(KeyEvent.VK_Q,true));
        jsKeyToJavaKeyCodeMap.put("R", new KeyClass(KeyEvent.VK_R,true));
        jsKeyToJavaKeyCodeMap.put("S", new KeyClass(KeyEvent.VK_S,true));
        jsKeyToJavaKeyCodeMap.put("T", new KeyClass(KeyEvent.VK_T,true));
        jsKeyToJavaKeyCodeMap.put("U", new KeyClass(KeyEvent.VK_U,true));
        jsKeyToJavaKeyCodeMap.put("V", new KeyClass(KeyEvent.VK_V,true));
        jsKeyToJavaKeyCodeMap.put("W", new KeyClass(KeyEvent.VK_W,true));
        jsKeyToJavaKeyCodeMap.put("X", new KeyClass(KeyEvent.VK_X,true));
        jsKeyToJavaKeyCodeMap.put("Y", new KeyClass(KeyEvent.VK_Y,true));
        jsKeyToJavaKeyCodeMap.put("Z", new KeyClass(KeyEvent.VK_Z,true));
        // Digit keys
        jsKeyToJavaKeyCodeMap.put("0", new KeyClass(KeyEvent.VK_0,false));
        jsKeyToJavaKeyCodeMap.put("1", new KeyClass(KeyEvent.VK_1,false));
        jsKeyToJavaKeyCodeMap.put("2", new KeyClass(KeyEvent.VK_2,false));
        jsKeyToJavaKeyCodeMap.put("3", new KeyClass(KeyEvent.VK_3,false));
        jsKeyToJavaKeyCodeMap.put("4", new KeyClass(KeyEvent.VK_4,false));
        jsKeyToJavaKeyCodeMap.put("5", new KeyClass(KeyEvent.VK_5,false));
        jsKeyToJavaKeyCodeMap.put("6", new KeyClass(KeyEvent.VK_6,false));
        jsKeyToJavaKeyCodeMap.put("7", new KeyClass(KeyEvent.VK_7,false));
        jsKeyToJavaKeyCodeMap.put("8", new KeyClass(KeyEvent.VK_8,false));
        jsKeyToJavaKeyCodeMap.put("9", new KeyClass(KeyEvent.VK_9,false));
        // Special keys (examples)
        jsKeyToJavaKeyCodeMap.put("Enter", new KeyClass(KeyEvent.VK_ENTER,false));
        jsKeyToJavaKeyCodeMap.put("Escape", new KeyClass(KeyEvent.VK_ESCAPE,false));
        jsKeyToJavaKeyCodeMap.put("Space", new KeyClass(KeyEvent.VK_SPACE,false));
        jsKeyToJavaKeyCodeMap.put("ArrowUp", new KeyClass(KeyEvent.VK_UP,false));
        jsKeyToJavaKeyCodeMap.put("ArrowDown", new KeyClass(KeyEvent.VK_DOWN,false));
        jsKeyToJavaKeyCodeMap.put("ArrowLeft", new KeyClass(KeyEvent.VK_LEFT,false));
        jsKeyToJavaKeyCodeMap.put("ArrowRight", new KeyClass(KeyEvent.VK_RIGHT,false));
        jsKeyToJavaKeyCodeMap.put("Shift", new KeyClass(KeyEvent.VK_SHIFT,false));
        jsKeyToJavaKeyCodeMap.put("Control", new KeyClass(KeyEvent.VK_CONTROL,false));
        jsKeyToJavaKeyCodeMap.put("Alt", new KeyClass(KeyEvent.VK_ALT,false));
        jsKeyToJavaKeyCodeMap.put("Tab", new KeyClass(KeyEvent.VK_TAB,false));
        jsKeyToJavaKeyCodeMap.put("Backspace", new KeyClass(KeyEvent.VK_BACK_SPACE,false));
        jsKeyToJavaKeyCodeMap.put("Delete", new KeyClass(KeyEvent.VK_DELETE,false));
        jsKeyToJavaKeyCodeMap.put("Insert", new KeyClass(KeyEvent.VK_INSERT,false));
        jsKeyToJavaKeyCodeMap.put("Home", new KeyClass(KeyEvent.VK_HOME,false));
        jsKeyToJavaKeyCodeMap.put("End", new KeyClass(KeyEvent.VK_END,false));
        jsKeyToJavaKeyCodeMap.put("PageUp", new KeyClass(KeyEvent.VK_PAGE_UP,false));
        jsKeyToJavaKeyCodeMap.put("PageDown", new KeyClass(KeyEvent.VK_PAGE_DOWN,false));
        jsKeyToJavaKeyCodeMap.put("CapsLock", new KeyClass(KeyEvent.VK_CAPS_LOCK,false));
        jsKeyToJavaKeyCodeMap.put("NumLock", new KeyClass(KeyEvent.VK_NUM_LOCK,false));
        jsKeyToJavaKeyCodeMap.put("ScrollLock", new KeyClass(KeyEvent.VK_SCROLL_LOCK,false));
        // Function keys
        jsKeyToJavaKeyCodeMap.put("F1", new KeyClass(KeyEvent.VK_F1,false));
        jsKeyToJavaKeyCodeMap.put("F2", new KeyClass(KeyEvent.VK_F2,false));
        jsKeyToJavaKeyCodeMap.put("F3", new KeyClass(KeyEvent.VK_F3,false));
        jsKeyToJavaKeyCodeMap.put("F4", new KeyClass(KeyEvent.VK_F4,false));
        jsKeyToJavaKeyCodeMap.put("F5", new KeyClass(KeyEvent.VK_F5,false));
        jsKeyToJavaKeyCodeMap.put("F6", new KeyClass(KeyEvent.VK_F6,false));
        jsKeyToJavaKeyCodeMap.put("F7", new KeyClass(KeyEvent.VK_F7,false));
        jsKeyToJavaKeyCodeMap.put("F8", new KeyClass(KeyEvent.VK_F8,false));
        jsKeyToJavaKeyCodeMap.put("F9", new KeyClass(KeyEvent.VK_F9,false));
        jsKeyToJavaKeyCodeMap.put("F10", new KeyClass(KeyEvent.VK_F10,false));
        jsKeyToJavaKeyCodeMap.put("F11", new KeyClass(KeyEvent.VK_F11,false));
        jsKeyToJavaKeyCodeMap.put("F12", new KeyClass(KeyEvent.VK_F12,false));
        jsKeyToJavaKeyCodeMap.put("F13", new KeyClass(KeyEvent.VK_F13,false));
        jsKeyToJavaKeyCodeMap.put("F14", new KeyClass(KeyEvent.VK_F14,false));
        jsKeyToJavaKeyCodeMap.put("F15", new KeyClass(KeyEvent.VK_F15,false));
        jsKeyToJavaKeyCodeMap.put("F16", new KeyClass(KeyEvent.VK_F16,false));
        jsKeyToJavaKeyCodeMap.put("F17", new KeyClass(KeyEvent.VK_F17,false));
        jsKeyToJavaKeyCodeMap.put("F18", new KeyClass(KeyEvent.VK_F18,false));
        jsKeyToJavaKeyCodeMap.put("F19", new KeyClass(KeyEvent.VK_F19,false));
        jsKeyToJavaKeyCodeMap.put("F20", new KeyClass(KeyEvent.VK_F20,false));
        jsKeyToJavaKeyCodeMap.put("F21", new KeyClass(KeyEvent.VK_F21,false));
        jsKeyToJavaKeyCodeMap.put("F22", new KeyClass(KeyEvent.VK_F22,false));
        jsKeyToJavaKeyCodeMap.put("F23", new KeyClass(KeyEvent.VK_F23,false));
        jsKeyToJavaKeyCodeMap.put("F24", new KeyClass(KeyEvent.VK_F24,false));
        // Other keys
        jsKeyToJavaKeyCodeMap.put("`", new KeyClass(KeyEvent.VK_BACK_QUOTE,false));
        jsKeyToJavaKeyCodeMap.put("~", new KeyClass(KeyEvent.VK_BACK_QUOTE, true)) ;
        jsKeyToJavaKeyCodeMap.put("!", new KeyClass(KeyEvent.VK_EXCLAMATION_MARK,false));
        jsKeyToJavaKeyCodeMap.put("@", new KeyClass(KeyEvent.VK_AT,false));
        jsKeyToJavaKeyCodeMap.put("#", new KeyClass(KeyEvent.VK_NUMBER_SIGN,false));
        jsKeyToJavaKeyCodeMap.put("$", new KeyClass(KeyEvent.VK_DOLLAR,false));
        jsKeyToJavaKeyCodeMap.put("%", new KeyClass(KeyEvent.VK_5, true)) ;
        jsKeyToJavaKeyCodeMap.put("^", new KeyClass(KeyEvent.VK_CIRCUMFLEX,false));
        jsKeyToJavaKeyCodeMap.put("&", new KeyClass(KeyEvent.VK_AMPERSAND,false));
        jsKeyToJavaKeyCodeMap.put("*", new KeyClass(KeyEvent.VK_ASTERISK,false));
        jsKeyToJavaKeyCodeMap.put("(", new KeyClass(KeyEvent.VK_LEFT_PARENTHESIS,false));
        jsKeyToJavaKeyCodeMap.put(")", new KeyClass(KeyEvent.VK_RIGHT_PARENTHESIS,false));
        jsKeyToJavaKeyCodeMap.put("_", new KeyClass(KeyEvent.VK_UNDERSCORE,false));
        jsKeyToJavaKeyCodeMap.put("-", new KeyClass(KeyEvent.VK_MINUS,false));
        jsKeyToJavaKeyCodeMap.put("+", new KeyClass(KeyEvent.VK_PLUS,false));
        jsKeyToJavaKeyCodeMap.put("=", new KeyClass(KeyEvent.VK_EQUALS,false));
        jsKeyToJavaKeyCodeMap.put(":", new KeyClass(KeyEvent.VK_COLON,false));
        jsKeyToJavaKeyCodeMap.put(";", new KeyClass(KeyEvent.VK_SEMICOLON,false));
        jsKeyToJavaKeyCodeMap.put("\"", new KeyClass(KeyEvent.VK_QUOTEDBL,false));
        jsKeyToJavaKeyCodeMap.put("'", new KeyClass(KeyEvent.VK_QUOTE,false));
        jsKeyToJavaKeyCodeMap.put("?", new KeyClass(KeyEvent.VK_SLASH, true)) ;
        jsKeyToJavaKeyCodeMap.put("/", new KeyClass(KeyEvent.VK_SLASH,false));
        jsKeyToJavaKeyCodeMap.put("\\", new KeyClass(KeyEvent.VK_BACK_SLASH,false));
        jsKeyToJavaKeyCodeMap.put("|", new KeyClass(KeyEvent.VK_BACK_SLASH, true)) ;
        jsKeyToJavaKeyCodeMap.put("{", new KeyClass(KeyEvent.VK_BRACELEFT,false));
        jsKeyToJavaKeyCodeMap.put("}", new KeyClass(KeyEvent.VK_BRACERIGHT,false));
        jsKeyToJavaKeyCodeMap.put("[", new KeyClass(KeyEvent.VK_OPEN_BRACKET,false));
        jsKeyToJavaKeyCodeMap.put("]", new KeyClass(KeyEvent.VK_CLOSE_BRACKET,false));
        jsKeyToJavaKeyCodeMap.put("<", new KeyClass(KeyEvent.VK_COMMA, true)) ;
        jsKeyToJavaKeyCodeMap.put(">", new KeyClass(KeyEvent.VK_PERIOD, true)) ;
        jsKeyToJavaKeyCodeMap.put(",", new KeyClass(KeyEvent.VK_COMMA,false));
        jsKeyToJavaKeyCodeMap.put(".", new KeyClass(KeyEvent.VK_PERIOD,false));
     }

    public static Object getJavaKeyCode(String jsKeyName) 
    {
       if (jsKeyName == null) jsKeyName = "Space" ;
        return jsKeyToJavaKeyCodeMap.getOrDefault(jsKeyName, KeyEvent.VK_UNDEFINED);
    }

    // Class for characters that do not have defined VK_codes and require <shift>VK-key
    
    public static class KeyClass
    {
       public int key ;
       public boolean shift ;
       
       KeyClass(int key,boolean shift)
       {
          this.key = key ;
          this.shift = shift ;          
       }
    }
}