package main;

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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RegisterState extends BaseAppState {
    private static final String USER_LIST_FILE = "archives/users.txt";
    private static final String ARCHIVE_FILE_PATH = "archives/";
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MAX_USERNAME_LENGTH = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 16;

    private Application app;
    private Node guiNode;
    private InputManager inputManager;
    private Container registerForm;
    private String username = "", password = "", confirmPassword = "";
    private TextField usernameField;
    private PasswordField passwordField, confirmPasswordField;
    private Picture registerPicture, backPicture;
    private Label usernameLabel, passwordLabel, confirmPasswordLabel;

    @Override
    protected void initialize(Application app) {
        this.app = app;

        if (!(app instanceof SimpleApplication)) {
            throw new IllegalArgumentException("Application must be an instance of SimpleApplication.");
        }
        guiNode = ((SimpleApplication) app).getGuiNode();
        inputManager = app.getInputManager();
    }

    private void initGui() {
        // 初始化 Lemur GUI
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        registerForm = new Container();
        guiNode.attachChild(registerForm);

        usernameLabel = registerForm.addChild(new Label("Username:"));
        usernameLabel.setFontSize(40);
        usernameLabel.setColor(ColorRGBA.White);
        usernameField = registerForm.addChild(new TextField(username), 1);
        usernameField.setPreferredWidth(200);
        usernameField.setFontSize(40);

        passwordLabel = registerForm.addChild(new Label("Password:"));
        passwordLabel.setFontSize(40);
        passwordLabel.setColor(ColorRGBA.White);
        passwordField = registerForm.addChild(new PasswordField(password), 1);
        passwordField.setPreferredWidth(200);
        passwordField.setFontSize(40);

        confirmPasswordLabel = registerForm.addChild(new Label("Confirm Password:"));
        confirmPasswordLabel.setFontSize(40);
        confirmPasswordLabel.setColor(ColorRGBA.White);
        confirmPasswordField = registerForm.addChild(new PasswordField(confirmPassword), 1);
        confirmPasswordField.setPreferredWidth(200);
        confirmPasswordField.setFontSize(40);

        registerPicture = new Picture("Register");
        registerPicture.setImage(app.getAssetManager(), "buttonregister.png", true);
        registerPicture.setWidth(239);
        registerPicture.setHeight(50);
        registerPicture.setLocalTranslation(150, app.getCamera().getHeight() - 500, 0);
        guiNode.attachChild(registerPicture);

        backPicture = new Picture("Back");
        backPicture.setImage(app.getAssetManager(), "buttonback.png", true);
        backPicture.setWidth(161);
        backPicture.setHeight(50);
        backPicture.setLocalTranslation(150, app.getCamera().getHeight() - 650, 0);
        guiNode.attachChild(backPicture);

        // 设置窗口位置
        registerForm.setLocalTranslation(150, app.getCamera().getHeight() - 200, 0);
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
                if (Main.inPicture(registerPicture, x, y)) {
                    Main.playClickSound();
                    username = usernameField.getText().trim();
                    password = passwordField.getText();
                    confirmPassword = confirmPasswordField.getText();

                    if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Username must be between " + MIN_USERNAME_LENGTH + " and " +
                                        MAX_USERNAME_LENGTH + " characters!"
                        ));
                    } else if (!isUsernameValid(username)) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Username must start with a letter or underscore, and only " +
                                        "contain letters, digits, and underscores."
                        ));
                    } else if (isUserExists(username) || username.equals("Visitor")) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Username already exists!"
                        ));
                    } else if (!password.equals(confirmPassword)) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Passwords do not match!"
                        ));
                    } else if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Password must be between " + MIN_PASSWORD_LENGTH + " and " +
                                        MAX_PASSWORD_LENGTH + " characters!"
                        ));
                    } else if (!isPasswordValid(password)) {
                        getStateManager().attach(new AlertState(
                                "Registration Failed",
                                "Password must contain at least two of the following: " +
                                        "lowercase letters, uppercase letters, digits, and special characters."
                        ));
                    } else {
                        saveUser(username, password);
                        getStateManager().attach(new AlertState(
                                "Registration Successful",
                                "User registered successfully!"
                        ));
                        createArchive(username);
                        createStatus(username);

                        Main.username = username; // 设置当前用户名

                        getStateManager().detach(RegisterState.this);
                        getStateManager().attach(new LevelSelectionState()); // 切换到关卡选择界面
                        cleanup(); // 清理资源
                    }

                    onDisable();
                    onEnable();
                } else if (Main.inPicture(backPicture, x, y)) {
                    Main.playClickSound();
                    getStateManager().detach(RegisterState.this);
                    getStateManager().attach(new MainMenuState()); // 返回主菜单
                }
            }
        }
    };

    // 检查用户名是否符合要求
    public boolean isUsernameValid(String username) {
        // 用户名必须以字母或下划线开头，并只包含字母、数字、下划线
        return username.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    // 检查用户是否已存在
    private boolean isUserExists(String username) {
        File file = new File(USER_LIST_FILE);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(" ");
                    if (parts.length == 2 && parts[0].equals(username)) return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 检查密码是否符合要求
    public boolean isPasswordValid(String password) {
        // 定义允许的字符集
        String allowedSpecialChars = "@|\\$!%\\*\\?&\\.,:;\"'/<>\\[\\]{}\\(\\)\\-_+=^#~";
        String validCharsPattern = "[a-zA-Z\\d" + allowedSpecialChars + "]+";

        // 检查密码是否只包含有效字符
        if (!password.matches(validCharsPattern)) return false;

        // 计算满足的类型数量，至少要有两种类型
        int typeCount = 0;
        if (password.matches(".*[a-z].*")) typeCount++;
        if (password.matches(".*[A-Z].*")) typeCount++;
        if (password.matches(".*\\d.*")) typeCount++;
        if (password.matches(".*[" + allowedSpecialChars + "].*")) typeCount++;

        return typeCount >= 2;
    }

    // 保存新用户信息到用户列表文件
    private void saveUser(String username, String password) {
        try (FileWriter writer = new FileWriter(USER_LIST_FILE, true)) {
            writer.write(username + " " + password + System.lineSeparator());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write to user list file!");
        }
    }

    // 创建用户存档文件
    private void createArchive(String username) {
        File archive = new File(ARCHIVE_FILE_PATH + username + "_archive.txt");
        if (!archive.getParentFile().exists()) archive.getParentFile().mkdirs();
        if (!archive.exists()) {
            try {
                archive.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create archive file for user " + username);
            }
        } else {
            System.err.println("Archive file of " + username + " already exists!");
        }
    }

    // 创建用户状态文件
    private void createStatus(String username) {
        File status = new File(ARCHIVE_FILE_PATH + username + "_status.txt");
        if (!status.getParentFile().exists()) status.getParentFile().mkdirs();
        if (!status.exists()) {
            try {
                status.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to create status file for user " + username);
            }
            try (FileWriter writer = new FileWriter(status, true)) {
                writer.write("0\n");
                for (int i = 0; i < LevelSelectionState.LEVEL_COUNT; i++) writer.append(Main.UNSOLVED);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to write to status file of user " + username);
            }
        } else {
            System.err.println("Status file of " + username + " already exists!");
        }
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        initInput(); // 初始化输入监听
    }

    @Override
    public void onDisable() {
        usernameField.getControl(GuiControl.class).focusLost();
        passwordField.getControl(GuiControl.class).focusLost();
        confirmPasswordField.getControl(GuiControl.class).focusLost();

        registerForm.detachAllChildren(); // 移除表单的所有子节点
        registerForm.removeFromParent(); // 将表单从 GUI 节点移除
        registerPicture.removeFromParent();
        backPicture.removeFromParent();

        inputManager.removeListener(actionListener);
        inputManager.clearMappings();
    }

    @Override
    protected void cleanup(Application app) {}
}