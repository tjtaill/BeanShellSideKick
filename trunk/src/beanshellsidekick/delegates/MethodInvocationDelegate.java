package beanshellsidekick.delegates;


import beanshellsidekick.CompletionBuilder;

import java.lang.reflect.Method;

public class MethodInvocationDelegate extends SimpleNodeDelegate {
    public MethodInvocationDelegate(Object delegate) {
        super(delegate);
    }

    public String getReturnType() {
        Object name = getChild(0);
        AmbiguousNameDelegate nameDelegate = new AmbiguousNameDelegate( name );
        String methodCall = nameDelegate.getName();
        int lastDot = methodCall.lastIndexOf('.');
        if ( lastDot == -1) {
            throw new IllegalStateException("Don't handle free method calls from static imports or beanhell methods");
        }
        String callTarget = methodCall.substring(0, lastDot);
        Class target = null;
        if ( callTarget.contains(".") || Character.isUpperCase( callTarget.charAt(0) ) ) {
            target = CompletionBuilder.CURRENT.resolveType(callTarget);
        } else {
            target = CompletionBuilder.CURRENT.resolveVar( callTarget );
        }
        String methodName = methodCall.substring(lastDot+1, methodCall.length());
        Object args = getChild(1);
        ArgumentsDelegate argsDelegate = new ArgumentsDelegate(args);
        Class[] argTypes = argsDelegate.getTypes();
        Method method = null;
        try {
            method = target.getMethod(methodName, argTypes);
        } catch (Exception e) {
            // crap did not find anything probably one of the argument need to be super type
            for( Method m : target.getMethods() ) {
                if ( m.getName().equals(methodName) ) {
                    method = m;
                    break; // found it
                }
            }
        }
        return method.getReturnType().getName();
    }
}
