package beanshellsidekick.delegates;

import java.lang.reflect.Method;

public class SimpleNodeDelegate {
    protected Object delegate;
    private static Method getTextMethod;
    private static Method getLineNumberMethod;
    private static Method getChildMethod;
    private static Method jjtGetNumChildrenMethod;

    static {
        try {
            Class<?> simpleNodeClass = Class.forName("org.gjt.sp.jedit.bsh.SimpleNode");
            getTextMethod = simpleNodeClass.getMethod("getText", new Class[] {});
            getTextMethod.setAccessible(true);
            getLineNumberMethod = simpleNodeClass.getMethod("getLineNumber", new Class[] {});
            getLineNumberMethod.setAccessible( true );
            getChildMethod = simpleNodeClass.getMethod("getChild", new Class[]{int.class});
            getChildMethod.setAccessible(true);
            jjtGetNumChildrenMethod = simpleNodeClass.getMethod("jjtGetNumChildren", new Class[]{});
            jjtGetNumChildrenMethod.setAccessible(true);
        } catch(Throwable t) {
            throw new RuntimeException( t );
        }
    }

    public SimpleNodeDelegate(Object delegate) {
        this.delegate = delegate;
    }

    public String getText() {
        try {
            return (String)getTextMethod.invoke(delegate, new Object[]{});
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public int getLineNumber() {
        try {
            return (Integer)getLineNumberMethod.invoke(delegate, new Object[]{});
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Object getChild(int i) {
        try {
            return getChildMethod.invoke(delegate, new Object[]{ i });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumChildren() {
        try {
            return (Integer)jjtGetNumChildrenMethod.invoke(delegate, new Object[]{});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
