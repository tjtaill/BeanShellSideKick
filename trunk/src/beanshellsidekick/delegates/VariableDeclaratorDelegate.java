package beanshellsidekick.delegates;


import java.lang.reflect.Field;

public class VariableDeclaratorDelegate extends SimpleNodeDelegate {

    private static Field name;

    static {
        try {
            Class type = Class.forName("org.gjt.sp.jedit.bsh.BSHVariableDeclarator");
            name = type.getDeclaredField("name");
            name.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VariableDeclaratorDelegate(Object delegate) {
        super(delegate);
    }

    public String getName() {
        try {
            return (String) name.get(delegate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
