package beanshellsidekick.delegates;

import java.lang.reflect.Field;

public class AmbiguousNameDelegate extends SimpleNodeDelegate {
    
    private static Field textField;
    
    static {
        try {
            Class type = Class.forName("org.gjt.sp.jedit.bsh.BSHAmbiguousName");
            textField = type.getField("text");
            textField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public AmbiguousNameDelegate(Object delegate) {
        super(delegate);
    }

    public String getName() {
        try {
            return (String)textField.get(delegate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
