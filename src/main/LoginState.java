package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.PasswordField;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.BaseStyles;

public class LoginState extends BaseAppState {
    private static final String USER_LIST_FILE = "archives/users.txt";

    private Application app;
    private Node guiNode;
    private InputManager inputManager;
    private Container loginForm;
    private String username = "", password = "";
    private TextField usernameField;
    private PasswordField passwordField;
    private Picture logInPicture, backPicture;
    private Label usernameLabel, passwordLabel;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();
        inputManager = app.getInputManager();

        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");
    }

    private void initGui() {
        loginForm = new Container();
        guiNode.attachChild(loginForm);

        usernameLabel=loginForm.addChild(new Label("Username:"));
        usernameLabel.setFontSize(40);
        usernameLabel.setColor(ColorRGBA.White);
        usernameField = loginForm.addChild(new TextField(username), 1);
        usernameField.setPreferredWidth(200);
        usernameField.setFontSize(40);

        passwordLabel=loginForm.addChild(new Label("Password:"));
        passwordLabel.setFontSize(40);
        passwordLabel.setColor(ColorRGBA.White);
        passwordField = loginForm.addChild(new PasswordField(password), 1);
        passwordField.setPreferredWidth(200);
        passwordField.setFontSize(40);

        logInPicture = new Picture("LogIn");
        logInPicture.setImage(app.getAssetManager(), "buttonlogin.png", true);
        logInPicture.setWidth(192);
        logInPicture.setHeight(50);
        logInPicture.setLocalTranslation(150, app.getCamera().getHeight() - 450, 0);
        guiNode.attachChild(logInPicture);

        backPicture = new Picture("Back");
        backPicture.setImage(app.getAssetManager(), "buttonback.png", true);
        backPicture.setWidth(161);
        backPicture.setHeight(50);
        backPicture.setLocalTranslation(150, app.getCamera().getHeight() - 600, 0);
        guiNode.attachChild(backPicture);

        // 设置窗口位置
        loginForm.setLocalTranslation(150, app.getCamera().getHeight() - 200, 0);
    }

    private void initInput() {
        app.getInputManager().addMapping("Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "Click");
    }

    private final ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Click") && isPressed) {
                // 获取鼠标点击位置
                float x = app.getInputManager().getCursorPosition().x;
                float y = app.getInputManager().getCursorPosition().y;

                // 检查点击位置是否在图片范围内
                if (Main.inPicture(logInPicture, x, y)) {
                    username = usernameField.getText().trim();
                    password = passwordField.getText();

                    if (authenticateUser(username, password)) {
                        getStateManager().attach(new AlertState(
                                "Login Successful",
                                "Welcome, " + username + "!"
                        ));
                        checkArchive(username); // 检查用户存档文件
                        checkStatus(username); // 检查用户状态文件

                        Main.username = username; // 设置当前用户名

                        getStateManager().detach(LoginState.this); // 移除当前状态
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
                } else if (Main.inPicture(backPicture, x, y)) {
                    getStateManager().detach(LoginState.this); // 移除当前状态
                    getStateManager().attach(new MainMenuState()); // 返回主菜单
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
                    String line = scanner.nextLine(), parts[] = line.split(" ");
                    if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("User list file not found!");
        }
        return false; // 用户验证失败
    }

    // 检查用户存档文件
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

    // 检查用户状态文件
    private void checkStatus(String username) {
        File file = new File("archives/" + username + "_status.txt");
        if (!file.exists()) {
            System.err.println("No status found for user " + username + ". A new status will be created.");
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file, true)) {
                    writer.write("0\n");
                    for (int i = 0; i < LevelSelectionState.LEVEL_COUNT; i++) writer.append(Main.UNSOLVED);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to write to status file of user " + username);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create status file for user " + username);
            }
        }
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        initInput(); // 初始化输入监听
    }

    @Override
    public void onDisable() {
        ((GuiControl) usernameField.getControl(GuiControl.class)).focusLost();
        ((GuiControl) passwordField.getControl(GuiControl.class)).focusLost();

        loginForm.detachAllChildren(); // 移除表单的所有子节点
        loginForm.removeFromParent(); // 将表单从 GUI 节点移除
        logInPicture.removeFromParent(); // 将登录按钮从 GUI 节点移除
        backPicture.removeFromParent(); // 将返回按钮从 GUI 节点移除

        inputManager.removeListener(actionListener);
        inputManager.clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}