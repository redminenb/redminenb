
package com.kenai.redminenb.util.markup;

public class StringUtil {
    public static String escapeHTML(String input) {
        if(input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27")
                ;
        
    }
}
