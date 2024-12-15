package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.PasswordField;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.style.BaseStyles;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class LoginState extends BaseAppState {
    private static final String USER_LIST_FILE = "archives/users.txt";

    private Application app;
    private Node guiNode;
    private Container loginForm;
    private String username = "", password = "";
    private Picture lIPicture;
    private Picture BPicture;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();

        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    private void initGui() {
        loginForm = new Container();
        guiNode.attachChild(loginForm);

        loginForm.addChild(new Label("Username:")).setFontSize(40);
        TextField usernameField = loginForm.addChild(new TextField(username), 1);
        usernameField.setPreferredWidth(200);
        usernameField.setFontSize(40);

        loginForm.addChild(new Label("Password:")).setFontSize(40);
        PasswordField passwordField = loginForm.addChild(new PasswordField(password), 1);
        passwordField.setPreferredWidth(200);
        passwordField.setFontSize(40);

        lIPicture = new Picture("LogIn");
        lIPicture.setImage(app.getAssetManager(), "buttonlogin.png", true);
        lIPicture.setWidth(404);
        lIPicture.setHeight(200);
        lIPicture.setLocalTranslation(230, app.getCamera().getHeight() - 500, 0);
        guiNode.attachChild(lIPicture);

        BPicture = new Picture("LogIn");
        BPicture.setImage(app.getAssetManager(), "buttonback.png", true);
        BPicture.setWidth(404);
        BPicture.setHeight(200);
        BPicture.setLocalTranslation(230, app.getCamera().getHeight() - 650, 0);
        guiNode.attachChild(BPicture);


        /*Button loginButton = loginForm.addChild(new Button("Log In"));
        loginButton.setFontSize(40);
        Button backButton = loginForm.addChild(new Button("Back"));
        backButton.setFontSize(40);*/

        // 事件绑定
        /*loginButton.addClickCommands(source -> {
            username = usernameField.getText().trim();
            password = passwordField.getText();

            if (authenticateUser(username, password)) {
                getStateManager().attach(new AlertState(
                        "Login Successful",
                        "Welcome, " + username + "!"
                ));
                checkArchive(username); // 检查用户存档

                Main.username = username; // 设置当前用户名

                getStateManager().detach(this);
                getStateManager().attach(new LevelSelectionState()); // 切换到关卡选择界面
                cleanup(); // 清理资源
            } else {
                getStateManager().attach(new AlertState(
                        "Login Failed",
                        "Invalid username or password!"
                ));
            }

            onDisable();
            onEnable();
        });
        backButton.addClickCommands(source -> {
            getStateManager().detach(this);
            getStateManager().attach(new MainMenuState()); // 返回主菜单
            cleanup(); // 清理资源
        });*/

        // 设置窗口位置
        loginForm.setLocalTranslation(250, app.getCamera().getHeight() -200, 0);
    }
    private final ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && !isPressed) {
                // 获取鼠标点击位置
                float x = app.getInputManager().getCursorPosition().x;
                float y = app.getInputManager().getCursorPosition().y;

                // 检查点击位置是否在图片范围内
                if (Main.inPicture(lIPicture, x, y)) {
                    getStateManager().detach(LoginState.this); // 移除当前状态
                    getStateManager().attach(new LoginState()); // 切换到登
                } else if (Main.inPicture(BPicture, x, y)) {
                    getStateManager().detach(LoginState.this);
                    getStateManager().attach(new RegisterState());
                }
            }
        }
    };
    // 验证用户凭证
    private boolean authenticateUser(String username, String password) {
        File file = new File(USER_LIST_FILE);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(" ");
                    if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("User list file not found!");
        }
        return false; // 用户验证失败
    }

    // 检查用户存档
    private void checkArchive(String username) {
        File file = new File("archives/" + username + "_archive.txt");
        if (!file.exists()) {
            System.err.println("No archive found for user " + username + ". A new archive will be created.");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        guiNode.attachChild(loginForm); // 将表单添加到 GUI 节点
    }

    @Override
    public void onDisable() {
        for (Spatial child : loginForm.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
        loginForm.detachAllChildren(); // 移除表单的所有子节点
        loginForm.removeFromParent(); // 将表单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}