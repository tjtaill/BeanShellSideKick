package beanshellsidekick.delegates;


import org.gjt.sp.jedit.bsh.ParserConstants;

import java.lang.reflect.Field;

public class AssignmentDelegate extends SimpleNodeDelegate{
    private static Field operatorField;

    static {
        try {
            Class<?> assingmentType = Class.forName("org.gjt.sp.jedit.bsh.BSHAssignment");
            operatorField = assingmentType.getField("operator");
            operatorField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public AssignmentDelegate(Object delegate) {
        super(delegate);
    }

    public String getLhsVarName() {
        Object lhs = getChild(0);
        String nodeType = lhs.getClass().getSimpleName();
        if ( "BSHPrimaryExpression".equals( nodeType ) ) {
            PrimaryExpressionDelegate expressionDelegate = new PrimaryExpressionDelegate(lhs);
            return expressionDelegate.getName();
        } else {
            throw new IllegalStateException("Expected node type BSHPrimaryExpression got " + nodeType );
        }

    }

    public String getRhsType() {
        Object rhs = getChild(1);
        String nodeType = rhs.getClass().getSimpleName();
        if ( "BSHPrimaryExpression".equals( nodeType ) ) {
            PrimaryExpressionDelegate expressionDelegate = new PrimaryExpressionDelegate(rhs);
            return expressionDelegate.getName();
        } else {
            throw new IllegalStateException("Expected node type BSHPrimaryExpression got " + nodeType );
        }
    }

    public boolean isSimpleAssignment() {
        try {
            int operator = (Integer)operatorField.get(delegate);
            return operator == ParserConstants.ASSIGN;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
