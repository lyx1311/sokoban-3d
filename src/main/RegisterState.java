package main;

import com.jme3.app.state.BaseAppState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.PasswordField;
import com.simsilica.lemur.TextField;
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
    private Container registerForm;
    private String username = "", password = "", confirmPassword = "";

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
        registerForm = new Container();
        guiNode.attachChild(registerForm);

        registerForm.addChild(new Label("Username:")).setFontSize(24);
        TextField usernameField = registerForm.addChild(new TextField(username));

        registerForm.addChild(new Label("Password:")).setFontSize(24);
        PasswordField passwordField = registerForm.addChild(new PasswordField(password));

        registerForm.addChild(new Label("Confirm Password:")).setFontSize(24);
        PasswordField confirmPasswordField = registerForm.addChild(new PasswordField(confirmPassword));

        Button registerButton = registerForm.addChild(new Button("Register"));
        registerButton.setFontSize(24);
        Button backButton = registerForm.addChild(new Button("Back"));
        backButton.setFontSize(24);

        // 事件绑定
        registerButton.addClickCommands(source -> {
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
                try {
                    saveUser(username, password);
                    getStateManager().attach(new AlertState(
                            "Registration Successful",
                            "User registered successfully!"
                    ));
                    createUserArchive(username);

                    Main.username = username; // 设置当前用户名

                    getStateManager().detach(this);
                    getStateManager().attach(new LevelSelectionState()); // 切换到关卡选择界面
                    cleanup(); // 清理资源
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            onDisable();
            onEnable();
        });
        backButton.addClickCommands(source -> {
            getStateManager().detach(this);
            getStateManager().attach(new MainMenuState()); // 返回主菜单
            cleanup(); // 清理资源
        });

        // 设置窗口位置
        registerForm.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);
    }

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
                    if (parts.length > 0 && parts[0].equals(username)) {
                        return true;
                    }
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
    private void saveUser(String username, String password) throws IOException {
        try (FileWriter writer = new FileWriter(USER_LIST_FILE, true)) {
            writer.write(username + " " + password + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 创建用户存档文件
    private void createUserArchive(String username) throws IOException {
        File archive = new File(ARCHIVE_FILE_PATH + username + "_archive.txt");
        if (!archive.getParentFile().exists()) archive.getParentFile().mkdirs();
        if (!archive.exists()) {
            archive.createNewFile();
        } else {
            System.err.println("Archive file of " + username + " already exists!");
        }
    }

    @Override
    public void onEnable() {
        initGui(); // 初始化 GUI
        guiNode.attachChild(registerForm); // 将表单添加到 GUI 节点
    }

    @Override
    public void onDisable() {
        for (Spatial child : registerForm.getChildren()) {
            if (child instanceof Button) ((Button) child).setEnabled(false); // 禁用按钮
        }
         registerForm.detachAllChildren(); // 移除表单的所有子节点
         registerForm.removeFromParent(); // 将表单从 GUI 节点移除
    }

    @Override
    protected void cleanup(Application app) {}
}