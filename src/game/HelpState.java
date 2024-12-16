package game;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

public class HelpState extends BaseAppState {
    private Application app;
    private Node guiNode;
    private Container helpForm;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();

        // 初始化 GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    private void initHelp() {
        helpForm = new Container();
        guiNode.attachChild(helpForm);

        Label label1 = new Label("Push all the boxes onto the glowing yellow target spots to win!");
        label1.setFontSize(40);
        helpForm.addChild(label1);

        Label label2 = new Label("Movement: Use WASD to move, QE to rotate the camera, and Space to push boxes. " +
                "You can also click the corresponding buttons on the interface.");
        label2.setFontSize(40);
        helpForm.addChild(label2);

        Label label3 = new Label("Restart: Press R to restart the current level.");
        label3.setFontSize(40);
        helpForm.addChild(label3);

        Label label4 = new Label("Undo: Press U to undo the last move.");
        label4.setFontSize(40);
        helpForm.addChild(label4);

        Label label5 = new Label("For more actions, press ESC or click the top-left corner to open the menu.");
        label5.setFontSize(40);
        helpForm.addChild(label5);

        // 设置位置
        helpForm.setLocalTranslation(100, app.getCamera().getHeight() - 100, 0);
    }

    @Override
    protected void onEnable() { initHelp(); }

    @Override
    protected void onDisable() {
        helpForm.detachAllChildren();
        helpForm.removeFromParent();
    }

    @Override
    protected void cleanup(Application app) {}
}