package beanshellsidekick.delegates;


import java.lang.reflect.Method;
import java.util.ArrayList;

public class TypedVariableDeclarationDelegate extends SimpleNodeDelegate {

    private static Method getTypeNodeMethod;
    private static Method getDeclaratorsMethod;

    static {
        Class type;
        try {
            type = Class.forName("org.gjt.sp.jedit.bsh.BSHTypedVariableDeclaration");
            getTypeNodeMethod = type.getDeclaredMethod("getTypeNode", new Class[]{});
            getTypeNodeMethod.setAccessible(true);
            getDeclaratorsMethod = type.getDeclaredMethod("getDeclarators", new Class[]{});
            getDeclaratorsMethod.setAccessible(true);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }



    public TypedVariableDeclarationDelegate(Object delegate) {
        super(delegate);
    }

    public String getType() {
        Class type;
        try {
            Object typeNode = getTypeNodeMethod.invoke(delegate, new Object[]{});
            TypeDelegate typeDelegate = new TypeDelegate(typeNode);
            return typeDelegate.getType();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<String> getDeclarators() {
        ArrayList<String> names = new ArrayList<>();
        try {
            Object[] variableNodes = (Object[])getDeclaratorsMethod.invoke(delegate, new Object[]{});
            for( Object variableNode : variableNodes ) {
                VariableDeclaratorDelegate variableDeclaration = new VariableDeclaratorDelegate(variableNode);
                names.add( variableDeclaration.getName() );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return names;
    }


}
