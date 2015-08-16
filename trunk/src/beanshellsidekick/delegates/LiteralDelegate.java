package beanshellsidekick.delegates;

import org.gjt.sp.jedit.bsh.Primitive;

import java.lang.reflect.Field;

/**
 * Created by troy on 8/13/2015.
 */
public class LiteralDelegate extends SimpleNodeDelegate {
    static private Field valueField;

    static {
        try {
            Class type = Class.forName("org.gjt.sp.jedit.bsh.BSHLiteral");
            valueField = type.getField("value");
            valueField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public LiteralDelegate(Object delegate) {
        super(delegate);
    }


    public String getType() {
        try {
            Object value = valueField.get(delegate);
            if ( value instanceof Primitive ) {
                Primitive primitive = (Primitive) value;
                return primitive.getType().getName();
            } else {
                return value.getClass().getName();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
