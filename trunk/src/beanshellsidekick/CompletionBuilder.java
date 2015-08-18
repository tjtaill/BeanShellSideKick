package beanshellsidekick;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class CompletionBuilder {
    public static CompletionBuilder CURRENT;
    private Set<String> imports = new LinkedHashSet<String>() {{
        add("java.lang");
        add("java.util");
        add("java.io");
        add("java.net");
        add("java.awt");
        add("javax.swing");
        add("javax.swing.event");
        add("java.awt.event");
        add("org.gjt.sp.jedit");
        add("org.gjt.sp.jedit.buffer");
        add("org.gjt.sp.jedit.textarea");
        add("org.gjt.sp.jedit.search");
        add("org.gjt.sp.jedit.io");
        add("org.gjt.sp.jedit.print");
        add("org.gjt.sp.util");
        add("org.gjt.sp.jedit.browser");
        add("org.gjt.sp.jedit.gui");
        add("org.gjt.sp.jedit.help");
        add("org.gjt.sp.jedit.msg");
        add("org.gjt.sp.jedit.options");
        add("org.gjt.sp.jedit.pluginmgr");
        add("org.gjt.sp.jedit.syntax");
    }};

    private Map<String, Class<?>> varToTypeCache = new HashMap<String, Class<?>>() {{
        try {
            put("buffer", Class.forName("org.gjt.sp.jedit.Buffer"));
            put("view", Class.forName("org.gjt.sp.jedit.View"));
            put("editPane", Class.forName("org.gjt.sp.jedit.EditPane"));
            put("textArea", Class.forName("org.gjt.sp.jedit.textarea.JEditTextArea"));
            put("wm", Class.forName("org.gjt.sp.jedit.gui.DockableWindowManager"));
            put("scriptPath", Class.forName("java.lang.String"));
            put("$_", Class.forName("java.lang.String"));
            put("$_e", Class.forName("java.lang.Exception"));
            put("bsh.args", Array.newInstance(String.class, 1).getClass() );
            put("bsh.cwd", String.class);
            put("bsh.show", boolean.class);
            put("bsh.interactive", boolean.class);
            put("bsh.evalOnly", boolean.class);
            put("this.variables", Array.newInstance(String.class, 1).getClass() );
            put("this.methods", Array.newInstance(String.class, 1).getClass() );

                   /*
bsh.shared - A special static space which is shared across all interpreter instances. Normally each bsh.Interpreter instance is entirely independent; having its own unique global namespace and settings. bsh.shared is implemented as a static namespace in the bsh.Interpreter class. It was added primarily to support communication among instances for the GUI desktop.
bsh.console - If BeanShell is running in its GUI desktop mode, this variable holds a reference to the current interpreter's console, if it has one.
bsh.appletcontext - If BeanShell is running inside an Applet, the current applet context, if one exists.
this.interpreter - A bsh.Interpreter reference to the currently executing BeanShell Interpreter object.
this.namespace - A bsh.NameSpace reference to the BeanShell NameSpace object of the current method context. See "Advanced Topics".
this.caller - A bsh.This reference to the calling BeanShell method context. See "Variables and Scope Modifiers".
this.callstack - An array of bsh.NameSpace references representing the "call stack" up to the current method context. See "Advanced Topics".
         */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }};

    private Map<String, List<String>> completionCache = new HashMap<String, List<String>>() {{
        try {
            put("jEdit", buildStaticCompletions(Class.forName("org.gjt.sp.jedit.jEdit")));
            put("bsh", new ArrayList<String>() {{
                add("appletcontext");
                add("args");
                add("console");
                add("cwd");
                add("evalOnly");
                add("interactive");
                add("show");
                add("shared");
            }} );
            put("this", new ArrayList<String>() {{
                add("caller");
                add("callstack");
                add("interpreter");
                add("interpreter");
                add("variables");

            }} );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }};

    public void addImport(String imp) {
        // TODO : hanlde static imports
        String[] parts = imp.split("\\.");
        String lastPart = parts[parts.length - 1];
        if (lastPart.equals("*")) { // wildcard import
            StringBuilder pkg = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                pkg.append(parts[i]);
                if (i < parts.length - 2) {
                    pkg.append('.');
                }
            }
            imports.add(pkg.toString());
        } else { // else class import
            imports.add(imp);
        }

    }

    private Class primitiveType(String type) {
        switch (type) {
            case "int":
                return int.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "float":
                return float.class;
            case "long":
                return long.class;
            case "double":
                return double.class;
            case "byte":
                return byte.class;
            default:
                throw new IllegalArgumentException("Unexpected type string " + type);
        }
    }

    public Class resolveType(String type) {
        Class fullType = null;
        try {
            // if this succeeds either we where given a fully qualified type
            fullType = Class.forName(type);
        } catch (ClassNotFoundException cnfe) {
            if (Character.isLowerCase(type.charAt(0))) {
                fullType = primitiveType(type);
            } else { // unqualified type resolve with imports
                for (String imp : imports) {
                    String qualifiedType = null;
                    try {
                        int lastDot = imp.lastIndexOf('.');
                        if (Character.isLowerCase(imp.charAt(lastDot + 1))) { // wildcard
                            qualifiedType = imp + "." + type;
                        } else { // class import
                            String[] parts = imp.split("\\.");
                            String lastPart = parts[parts.length - 1];
                            if (!lastPart.equals(fullType))
                                continue;
                            qualifiedType = imp;
                        }
                        fullType = Class.forName(qualifiedType);
                        break;
                    } catch (ClassNotFoundException cnfe2) {
                        continue;
                    }
                }
            }
        }
        return fullType;
    }

    public void addVariable(String varName, String varType) {
        String baseType = null;
        Class type = null;
        boolean isArray = false;
        if (varType.contains("[]")) {
            baseType = varType.split("\\[", 2)[0];
            isArray = true;
        } else {
            baseType = varType;
        }

        type = resolveType(baseType);

        if (isArray) {
            Object arrayInstance = Array.newInstance(type, 1);
            type = arrayInstance.getClass();
        }

        varToTypeCache.put(varName, type);

    }

    private List<String> buildInstanceCompletions(Class<?> type) {
        Set<String> uniqueCompletions = new HashSet<>();
        for (Method method : type.getMethods()) {
            uniqueCompletions.add(buildMethodCompletion(method));
        }

        for (Field field : type.getFields()) {
            uniqueCompletions.add(field.getName());
        }
        ArrayList<String> completions = new ArrayList<>(uniqueCompletions);
        Collections.sort(completions);
        return completions;
    }


    private static String buildMethodCompletion(Method method) {
        String suffix = method.getParameterTypes().length > 0 ? "(" : "()";
        return method.getName() + suffix;
    }

    private static List<String> buildStaticCompletions(Class<?> type) {
        Set<String> uniqueCompletions = new HashSet<>();
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()))
                uniqueCompletions.add(buildMethodCompletion(method));
        }

        for (Field field : type.getFields()) {
            if (Modifier.isStatic(field.getModifiers()))
                uniqueCompletions.add(field.getName());
        }
        ArrayList<String> completions = new ArrayList<>(uniqueCompletions);
        Collections.sort(completions);
        return completions;
    }


    public List<String> build(String identifierBeforeDot) {
        List<String> completions = completionCache.get(identifierBeforeDot);
        if (completions != null)
            return completions;
        if (Character.isUpperCase(identifierBeforeDot.charAt(0))) { // static completion
            completions = buildStaticCompletions(resolveType(identifierBeforeDot));
            completionCache.put(identifierBeforeDot, completions);
        } else { // instance completion
            Class<?> type = varToTypeCache.get(identifierBeforeDot);
            if (type == null || type.isPrimitive()) {
                return Collections.EMPTY_LIST;
            } else if (type.isArray()) { // array completion
                /*
                 optimized array lookup all arrays support same methods
                 so store in completion cache as array
                  */
                completions = completionCache.get("array");
                if (completions == null) {
                    completions = buildInstanceCompletions(type);
                    completions.add("length");
                    Collections.sort(completions);
                    completionCache.put("array", completions);
                }
            } else { // object completion
                completions = buildInstanceCompletions(type);
                completionCache.put(identifierBeforeDot, completions);
            }
        }
        return completions;
    }

    public Class resolveVar(String var) {
        return varToTypeCache.get(var);
    }
}
