package beanshellsidekick;


import beanshellsidekick.delegates.AssignmentDelegate;
import beanshellsidekick.delegates.SimpleNodeDelegate;
import beanshellsidekick.delegates.TypedVariableDeclarationDelegate;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import errorlist.*;

import java.io.StringReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.bsh.ParseException;
import org.gjt.sp.jedit.bsh.Parser;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import sidekick.SideKickCompletion;
import sidekick.SideKickCompletionPopup;
import sidekick.SideKickParsedData;
import sidekick.SideKickParser;
import sidekick.enhanced.*;


public class BeanShellSideKickParser extends SideKickParser {
    private static final Map<Buffer, CompletionBuilder> completionBuilders = new HashMap<>();


    private static final Pattern POSITION_PATTERN = Pattern.compile("(\\d+).*?(\\d+)");

    private boolean firstVariable;

    private SideKickCompletionPopup lastCompletionPopup;
    private BeanShellCompletion lastCompletion;


    public BeanShellSideKickParser() {
        super("beanshell");
    }

    @Override
    public SideKickParsedData parse(Buffer buffer,
                                    DefaultErrorSource errorSource) {
        BeanShellSideKickParsedData data = new BeanShellSideKickParsedData(buffer.getName());

        CompletionBuilder completionBuilder = completionBuilders.get(buffer);
        if ( null == completionBuilder ) {
            completionBuilder = new CompletionBuilder();
            completionBuilders.put(buffer, completionBuilder);
        }
        CompletionBuilder.CURRENT = completionBuilder;

        if ( buffer.isDirty() ) {
            return data;
        }

        String content = buffer.getText();
        Parser parser = new Parser(new StringReader(content));
        boolean firstImport = true;
        boolean firstMethod = true;
        boolean eof = false;
        firstVariable = true;
        ParseException lastBpe = null;
        while (!eof) {
            try {
                eof = parser.Line();
                if (eof) {
                    break;
                }
                Object node = parser.popNode();
                SimpleNodeDelegate simpleNodeDelegate = new SimpleNodeDelegate(node);
                int line = simpleNodeDelegate.getLineNumber() - 1;
                String nodeType = node.getClass().getSimpleName();
                String varName;
                String typeName;
                String nodeText;
                // TODO : this should be replaced by a factory
                switch (nodeType) {
                    case "BSHImportDeclaration":
                        if (firstImport) {
                            SourceAsset imports = new SourceAsset("imports", line, begin(line, buffer));
                            data.imports = new DefaultMutableTreeNode(imports);
                            data.root.add(data.imports);
                            firstImport = false;
                        }
                        String importText = simpleNodeDelegate.getText().replaceFirst("import", "");
                        importText = importText.replaceFirst(";", "");
                        importText = importText.replaceAll("\\s", "");
                        SourceAsset imp = new SourceAsset(importText, line, begin(line, buffer));
                        data.imports.add(new DefaultMutableTreeNode(imp));
                        completionBuilder.addImport(importText);
                        break;
                    case "BSHMethodDeclaration":
                        if (firstMethod) {
                            SourceAsset methods = new SourceAsset("methods", line, begin(line, buffer));
                            data.methods = new DefaultMutableTreeNode(methods);
                            data.root.add(data.methods);
                            firstMethod = false;
                        }
                        SourceAsset method = new SourceAsset(simpleNodeDelegate.getText().replaceFirst("\\{", ""), line, begin(line, buffer));
                        data.methods.add(new DefaultMutableTreeNode(method));
                        break;
                    case "BSHAssignment":
                        AssignmentDelegate assignmentDelegate = new AssignmentDelegate(node);
                        if ( !assignmentDelegate.isSimpleAssignment() ) {
                            continue;
                        }
                        varName = assignmentDelegate.getLhsVarName();
                        typeName = assignmentDelegate.getRhsType();
                        completionBuilder.addVariable(varName, typeName);
                        String[] parts = typeName.split("\\.");
                        typeName = parts[parts.length - 1];
                        nodeText = varName + " : " + typeName;
                        addVariable(buffer, nodeText, data, line);
                        break;
                    case "BSHTypedVariableDeclaration":
                        TypedVariableDeclarationDelegate variableDelegate = new TypedVariableDeclarationDelegate( node );
                        typeName = variableDelegate.getType();
                        for (String name : variableDelegate.getDeclarators()) {
                            completionBuilder.addVariable(name, typeName);
                            parts = typeName.split("\\.");
                            typeName = parts[parts.length - 1];
                            nodeText = name + " : " + typeName;
                            addVariable(buffer, nodeText, data, line);
                        }
                        break;
                    default:
                        continue;
                }
            } catch (ParseException bpe) {
                if ( lastBpe != null && lastBpe.toString().equals(bpe.toString()) ) {
                    break;
                }
                lastBpe = bpe;
                Matcher matcher = POSITION_PATTERN.matcher(bpe.getMessage());
                int line = 0;
                int column = 0;
                if (matcher.find()) {
                    line = Integer.valueOf(matcher.group(1)) - 1;
                    column = Integer.valueOf(matcher.group(2));
                }
                errorSource.addError(ErrorSource.ERROR, buffer.getPath(),
                        line, column - 1, column, bpe.getMessage());

            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return data;
    }

    @Override
    public boolean supportsCompletion() {
        return true;
    }

    @Override
    public boolean canCompleteAnywhere() {
        return false;
    }

    @Override
    public String getInstantCompletionTriggers() {
        return ".";
    }

    public SideKickCompletionPopup getCompletionPopup(View view,
                                                      int caretPosition, SideKickCompletion complete, boolean active)
    {
        lastCompletionPopup = new SideKickCompletionPopup(view, this, caretPosition,
                complete, active);
        lastCompletion.setCompletionPopup( lastCompletionPopup );
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                lastCompletionPopup.setSelectedIndex(0);
            }
        });
        return lastCompletionPopup;
    }

    @Override
    public SideKickCompletion complete(EditPane editPane, int caret) {

        List<String> completions;
        Buffer buffer = editPane.getBuffer();
        JEditTextArea textArea = editPane.getTextArea();
        CompletionBuilder completionBuilder = completionBuilders.get(buffer);
        if ( completionBuilder == null ) {
            completions = Collections.EMPTY_LIST;
        } else {
            // get word left of dot
            String identifier = getIdentifierBeforeDot(textArea, caret);
            completions = completionBuilder.build(identifier);
        }
        lastCompletion = new BeanShellCompletion(editPane.getView(),"", completions);
        return lastCompletion;
    }

    private void addVariable(Buffer buffer, String nodeText, BeanShellSideKickParsedData data, int line) {
        if (firstVariable) {
            SourceAsset variables = new SourceAsset("variables", line, begin(line, buffer));
            data.variables = new DefaultMutableTreeNode(variables);
            data.root.add(data.variables);
            firstVariable = false;
        }
        SourceAsset variable = new SourceAsset(nodeText, line, begin(line, buffer));
        data.variables.add(new DefaultMutableTreeNode(variable));
    }





    private String getIdentifierBeforeDot(JEditTextArea textArea, int caret) {
        if ( caret <= 0 )
            return "";

        int pos = caret;
        char c = textArea.getText(--pos, 1).charAt(0);
        StringBuilder identifier = new StringBuilder();
        boolean isFirstChar = true;
        while ( Character.isJavaIdentifierPart(c) || c == '.' ) {
            if ( isFirstChar && c == '.' ) {
                isFirstChar = false;
                c = textArea.getText(--pos, 1).charAt(0);
                continue;
            }
            isFirstChar = false;
            identifier.insert(0, c);
            c = textArea.getText(--pos, 1).charAt(0);
        }
        return identifier.toString();
    }

    Position begin(int line, Buffer buffer) {
        return buffer.createPosition(buffer.getLineStartOffset(line));
    }

    Position end(int line, Buffer buffer) {
        return buffer.createPosition(buffer.getLineEndOffset(line));
    }

}

