package com.gameengine.core;

import com.gameengine.graphics.Renderer;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;
import com.gameengine.util.Profiler;
import javax.swing.Timer;

/**
 * 游戏引擎
 */
public class GameEngine {
    private Renderer renderer;
    private InputManager inputManager;
    private Scene currentScene;
    private boolean running;
    private float targetFPS;
    private float deltaTime;
    private long lastTime;
    private String title;
    private Timer gameTimer;
    private float currentFPS;
    private int frameCount;
    private long fpsTimer;
    private Profiler profiler;

    public GameEngine(int width, int height, String title) {
        this.title = title;
        this.renderer = new Renderer(width, height, title);
        this.inputManager = InputManager.getInstance();
        this.running = false;
        this.targetFPS = 120.0f;
        this.deltaTime = 0.0f;
        this.lastTime = System.nanoTime();
        this.currentFPS = 0.0f;
        this.frameCount = 0;
        this.fpsTimer = System.currentTimeMillis();
        this.profiler = Profiler.getInstance();
    }
    
    /**
     * 初始化游戏引擎
     */
    public boolean initialize() {
        return true; // Swing渲染器不需要特殊初始化
    }
    
    /**
     * 运行游戏引擎
     */
    public void run() {
        if (!initialize()) {
            System.err.println("游戏引擎初始化失败");
            return;
        }
        
        running = true;
        
        // 初始化当前场景
        if (currentScene != null) {
            currentScene.initialize();
        }
        
        // 创建游戏循环定时器
        gameTimer = new Timer((int) (1000 / targetFPS), e -> {
            if (running) {
                update();
                render();
            }
        });
        
        gameTimer.start();
    }
    
    /**
     * 更新游戏逻辑
     */
    private void update() {
        profiler.begin("Frame");

        // 计算时间间隔
        profiler.begin("DeltaTime");
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000.0f; // 转换为秒
        lastTime = currentTime;
        profiler.end("DeltaTime");

        // 更新输入
        profiler.begin("Input");
        inputManager.update();
        profiler.end("Input");

        // 更新场景
        profiler.begin("SceneUpdate");
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
        profiler.end("SceneUpdate");

        // 处理事件
        profiler.begin("PollEvents");
        renderer.pollEvents();
        profiler.end("PollEvents");

        // 检查退出条件
        if (inputManager.isKeyPressed(27)) { // ESC键
            running = false;
            gameTimer.stop();
            renderer.cleanup();
        }

        // 检查窗口是否关闭
        if (renderer.shouldClose()) {
            running = false;
            gameTimer.stop();
        }

        profiler.end("Frame");
    }
    
    /**
     * 渲染游戏
     */
    private void render() {
        profiler.begin("Render");

        profiler.begin("BeginFrame");
        renderer.beginFrame();
        profiler.end("BeginFrame");

        // 渲染场景
        profiler.begin("SceneRender");
        if (currentScene != null) {
            currentScene.render();
        }
        profiler.end("SceneRender");

        profiler.begin("EndFrame");
        renderer.endFrame();
        profiler.end("EndFrame");

        profiler.end("Render");

        // 计算FPS
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - fpsTimer >= 1000) {
            currentFPS = frameCount / ((currentTime - fpsTimer) / 1000.0f);
            frameCount = 0;
            fpsTimer = currentTime;
        }

        // 结束帧分析
        profiler.endFrame();
    }
    
    /**
     * 设置当前场景
     */
    public void setScene(Scene scene) {
        this.currentScene = scene;
        if (scene != null && running) {
            scene.initialize();
        }
    }
    
    /**
     * 获取当前场景
     */
    public Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * 停止游戏引擎
     */
    public void stop() {
        running = false;
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        if (currentScene != null) {
            currentScene.clear();
        }
        renderer.cleanup();
    }
    
    /**
     * 获取渲染器
     */
    public Renderer getRenderer() {
        return renderer;
    }
    
    /**
     * 获取输入管理器
     */
    public InputManager getInputManager() {
        return inputManager;
    }
    
    /**
     * 获取时间间隔
     */
    public float getDeltaTime() {
        return deltaTime;
    }
    
    /**
     * 设置目标帧率
     */
    public void setTargetFPS(float fps) {
        this.targetFPS = fps;
        if (gameTimer != null) {
            gameTimer.setDelay((int) (1000 / fps));
        }
    }
    
    /**
     * 获取目标帧率
     */
    public float getTargetFPS() {
        return targetFPS;
    }
    
    /**
     * 检查引擎是否正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 获取当前FPS
     */
    public float getCurrentFPS() {
        return currentFPS;
    }

    /**
     * 获取性能分析器
     */
    public Profiler getProfiler() {
        return profiler;
    }
}
