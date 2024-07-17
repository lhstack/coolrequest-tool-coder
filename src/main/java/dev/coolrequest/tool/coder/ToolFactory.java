package dev.coolrequest.tool.coder;

import dev.coolrequest.tool.CoolToolPanel;
import dev.coolrequest.tool.ToolPanelFactory;

public class ToolFactory implements ToolPanelFactory {

    @Override
    public CoolToolPanel createToolPanel() {
        return new MainPanel();
    }
}
