package beanshellsidekick.delegates;


import java.lang.reflect.Method;

public class PrimitiveTypeDelegate extends SimpleNodeDelegate {
    private static Method getTypeMethod;

    static {
        try {
            Class type = Class.forName("org.gjt.sp.jedit.bsh.BSHPrimitiveType");
            getTypeMethod = type.getMethod("getType", new Class[]{});
            getTypeMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PrimitiveTypeDelegate(Object delegate) {
        super(delegate);
    }

    public String getType() {
        try {
            Class type = (Class)getTypeMethod.invoke(delegate, new Object[]{});
            return type.getName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
