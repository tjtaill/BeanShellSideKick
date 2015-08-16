package beanshellsidekick.delegates;


public class AllocationExpressionDelegate extends SimpleNodeDelegate {

    public AllocationExpressionDelegate(Object delegate) {
        super(delegate);
    }

    public String getType() {
        try {
            Object node = getChild(0);
            int arrayDims = 0;
            Object array = getChild(1);
            String nodeType = array.getClass().getSimpleName();
            switch (nodeType) {
                case "BSHArrayDimensions":
                    ArrayDimensionsDelegate arrayDelegate = new ArrayDimensionsDelegate(array);
                    arrayDims = arrayDelegate.getDims();
                    break;
                case "BSHArguments":
                    // don't need these but this state is ok
                    break;
                default:
                    throw new IllegalStateException("Unexpected node type " + nodeType);
            }

            nodeType =  node.getClass().getSimpleName();
            StringBuilder type = new StringBuilder();
            switch(nodeType) {
                case "BSHAmbiguousName":
                    AmbiguousNameDelegate nameDelegate = new AmbiguousNameDelegate(node);
                    type.append( nameDelegate.getName() );
                    break;
                case "BSHPrimitiveType":
                    PrimitiveTypeDelegate typeDelegate = new PrimitiveTypeDelegate(node);
                    type.append( typeDelegate.getType() );
                    break;
                default:
                    throw new IllegalStateException("Unexpected node type " + nodeType );
            }
            for(int i = 0; i < arrayDims; i++) {
                type.append("[]");
            }
            return type.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
