package dev.coolrequest.tool;

public class ToolFactory implements ToolPanelFactory {

    @Override
    public CoolToolPanel createToolPanel() {
        return new MainPanel();
    }
}
