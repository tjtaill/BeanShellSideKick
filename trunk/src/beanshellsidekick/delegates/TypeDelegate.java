package beanshellsidekick.delegates;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TypeDelegate extends SimpleNodeDelegate {
    private static Field typeField;
    private static Field arrayDimsField;
    private static Method getTypeNodeMethod;

    static {
        try {
            Class<?> bshType = Class.forName("org.gjt.sp.jedit.bsh.BSHType");
            getTypeNodeMethod =  bshType.getDeclaredMethod("getTypeNode", new Class[]{});
            getTypeNodeMethod.setAccessible(true);
            typeField = bshType.getDeclaredField("type");
            typeField.setAccessible(true);
            arrayDimsField = bshType.getDeclaredField("arrayDims");
            arrayDimsField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TypeDelegate(Object delegate) {
        super(delegate);
    }

    public String getType() {
        StringBuilder typeText = new StringBuilder();
        try {
            Class type = (Class)typeField.get(delegate);
            if ( null != type ) {
                return type.getName();
            }
            Object node = getTypeNodeMethod.invoke(delegate, new Object[]{});
            String nodeType = node.getClass().getSimpleName();
            switch(nodeType) {
                case "BSHAmbiguousName":
                    AmbiguousNameDelegate nameDelegate = new AmbiguousNameDelegate(node);
                    typeText.append(nameDelegate.getName());
                    break;
                case "BSHPrimitiveType":
                    PrimitiveTypeDelegate primitiveDelegate = new PrimitiveTypeDelegate(node);
                    typeText.append(primitiveDelegate.getType());
                    break;
                default:
                    break;
            }
            int arrayDims = (Integer)arrayDimsField.get(delegate);
            for (int i = 0; i < arrayDims; i++) {
                typeText.append("[]");
            }
            return typeText.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    

}
