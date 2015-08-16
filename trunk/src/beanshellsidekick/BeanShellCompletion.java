package beanshellsidekick;

import org.gjt.sp.jedit.View;
import sidekick.SideKickCompletion;

import java.util.List;


public class BeanShellCompletion extends SideKickCompletion {
    public BeanShellCompletion(View view, String text, List items) {
        super(view, text, items);
    }
}
