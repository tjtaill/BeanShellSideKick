package beanshellsidekick.delegates;


public class PrimaryExpressionDelegate extends SimpleNodeDelegate {



    public PrimaryExpressionDelegate(Object delegate) {
        super(delegate);
    }

    public String getName() {
        try {
            Object node = getChild(0);
            String nodeType = node.getClass().getSimpleName();
            switch(nodeType) {
                case "BSHAmbiguousName":
                    AmbiguousNameDelegate nameDelegate = new AmbiguousNameDelegate( node );
                    return nameDelegate.getName();
                case "BSHAllocationExpression":
                    AllocationExpressionDelegate allocationDelegate = new AllocationExpressionDelegate(node);
                    return allocationDelegate.getType();
                case "BSHLiteral":
                    LiteralDelegate literalDelegate = new LiteralDelegate(node);
                    return literalDelegate.getType();
                case "BSHMethodInvocation":
                    MethodInvocationDelegate invocationDelegate = new MethodInvocationDelegate(node);
                    return invocationDelegate.getReturnType();
                default:
                    throw new IllegalStateException("Unexpected Node Type "  + nodeType );
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
