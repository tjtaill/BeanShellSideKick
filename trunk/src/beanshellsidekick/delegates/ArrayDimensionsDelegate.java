package beanshellsidekick.delegates;

import java.lang.reflect.Field;

/**
 * Created by ttaillefer on 8/14/2015.
 */
public class ArrayDimensionsDelegate extends SimpleNodeDelegate {
    private static Field numDefinedDimsField;
    private static Field numUndefinedDimsField;

    static {
        try {
            Class type = Class.forName("org.gjt.sp.jedit.bsh.BSHArrayDimensions");
            numDefinedDimsField = type.getField("numDefinedDims");
            numDefinedDimsField.setAccessible(true);
            numUndefinedDimsField = type.getField("numUndefinedDims");
            numUndefinedDimsField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public ArrayDimensionsDelegate(Object delegate) {
        super(delegate);
    }

    public int getDims() {
        try {
            int undefined = (Integer)numUndefinedDimsField.get(delegate);
            int defined = (Integer)numDefinedDimsField.get(delegate);
            return undefined + defined;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
