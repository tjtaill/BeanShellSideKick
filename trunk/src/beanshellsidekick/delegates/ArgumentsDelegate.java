package beanshellsidekick.delegates;


import beanshellsidekick.CompletionBuilder;

public class ArgumentsDelegate extends SimpleNodeDelegate {

    public ArgumentsDelegate(Object delegate) {
        super(delegate);
    }

    public Class[] getTypes() {
        Class[] types = new Class[ getNumChildren() ];
        String typeName = null;
        Class type = null;
        for(int i = 0; i < types.length; i++ ) {
            Object child = getChild(i);
            String childType = child.getClass().getSimpleName();
            switch(childType) {
                case "BSHPrimaryExpression":
                    PrimaryExpressionDelegate expressionDelegate = new PrimaryExpressionDelegate(child);
                    typeName = expressionDelegate.getName();
                    break;
                default:
                    throw new IllegalStateException("Unexpected nod type " + childType);
            }
            type = CompletionBuilder.CURRENT.resolveType(typeName);
            types[i] = type;
        }
        return types;
    }
}
