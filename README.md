# Sokoban Monkey

这是 [lyx1311](https://github.com/lyx1311) 和 [SKYTRAMPPER](https://github.com/SKYTRAMPPER) 的 2024 秋 SUSTech CS109 的 project——小型 3D 推箱子游戏。本项目基于 [jMonkeyEngine 3 (jME3)](https://jmonkeyengine.org/) 及其 GUI 框架 [Lemur](https://github.com/jMonkeyEngine-Contributions/Lemur) 开发。

## 功能特性

- 经典的推箱子玩法：WASD 移动，QE 旋转镜头，Space 推箱子（或点击屏幕上的按钮）
- 视角切换：按 L 以飞起 / 降落
- 撤销（按 U）、重启、注册 / 登录用户、存读档
- A* 搜索以自动解决关卡
- 金币系统
- 参数设置
- ……

## 环境要求

- JDK 17 版本（**更高或更低版本都可能无法运行**）
- 支持 OpenGL 的显卡

## 快速开始

以下是 Windows 上运行项目的简单步骤：

1. **克隆仓库**  
   打开 [Git Bash](https://git-scm.com/downloads) 并运行以下命令以克隆项目代码：
   
   ```markdown
   git clone https://github.com/lyx1311/sokoban-3d.git
   cd sokoban-3d
   ```

2. **构建项目**  
   使用 Gradle 构建项目：
   
   ```bash
   gradlew.bat clean build
   ```

3. **运行游戏**  
   构建完成后，通过以下命令运行游戏：
   
   ```bash
   gradlew.bat run
   ```

4. **启动界面**  
   游戏启动后，您将进入主菜单，可以选择关卡并开始游戏。

## 项目结构

- `src/`：项目的主要代码，包括游戏逻辑和 3D 渲染。
- `assets/`：资源文件夹，包含模型、纹理和声音。
- `build.gradle`：Gradle 构建脚本，配置依赖和任务。

## 致谢

尤其感谢以下开源项目：

- [jMonkeyEngine 3](https://jmonkeyengine.org/)
- [Lemur](https://github.com/jMonkeyEngine-Contributions/Lemur)

以及任课的马老师、沈老师！（欢迎妮可的同学们报名该课程）

想了解 jME3？推荐阅读 [jME3 中文版教程](https://github.com/jmecn/tutorial-for-beginners) 并自行尝试！本项目只实现了其中一点小小小功能……

若对项目中任何地方有疑问，欢迎和我交流；若在学习 jME3 时遇到困难，推荐在 [jME 论坛](https://hub.jmonkeyengine.org/) 上直接发帖求助！

如果您喜欢这个项目，希望您为仓库点亮 ⭐ Star！

---

*本项目基于 [Creative Commons BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) 和 [The Star And Thank Author License](https://github.com/zTrix/sata-license) 进行许可，使用或修改时请注明作者并分享修改内容。*
