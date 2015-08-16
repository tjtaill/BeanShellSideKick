package beanshellsidekick;

import sidekick.SideKickParsedData;
import javax.swing.tree.DefaultMutableTreeNode;

public class BeanShellSideKickParsedData extends SideKickParsedData {

    public DefaultMutableTreeNode imports = null;
    public DefaultMutableTreeNode methods = null;
    public DefaultMutableTreeNode variables = null;
    
    /**
     * @param fileName The file name being parsed, used as the root of the
     * tree.
     */
    public BeanShellSideKickParsedData(String fileName) {
        super( fileName );
    }
}