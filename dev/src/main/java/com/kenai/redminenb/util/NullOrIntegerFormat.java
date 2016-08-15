
package com.kenai.redminenb.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class NullOrIntegerFormat extends Format  {

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if(obj != null) {
            if(obj instanceof Number) {
                toAppendTo.append(((Number) obj).intValue());
            } else {
                throw new IllegalArgumentException("Only integers are understood");
            }
        }
        return toAppendTo;
    }

    @Override
    public Object parseObject(String source, ParsePosition pos) {
        if(source.trim().isEmpty()) {
            pos.setIndex(1);
            return null;
        } else {
            try {
                int result = Integer.parseInt(source);
                pos.setIndex(source.length());
                return result;
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }


}
