package com.gameengine.example;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplayScene extends Scene {
    private GameEngine engine;
    private IRenderer renderer;
    private InputManager inputManager;
    private List<String> replayFiles;
    private int selectedIndex = 0;

    public ReplayScene(GameEngine engine) {
        super("ReplayScene");
        this.engine = engine;
        this.renderer = engine.getRenderer();
        this.inputManager = InputManager.getInstance();
        this.replayFiles = new ArrayList<>();
        loadReplayFiles();
    }

    private void loadReplayFiles() {
        File dir = new File(".");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
            if (files != null) {
                for (File file : files) {
                    replayFiles.add(file.getName());
                }
            }
        }
        if (replayFiles.isEmpty()) {
            replayFiles.add("(无回放文件)");
        }
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (inputManager.isKeyJustPressed(265) || inputManager.isKeyJustPressed(87)) { // 上
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (inputManager.isKeyJustPressed(264) || inputManager.isKeyJustPressed(83)) { // 下
            selectedIndex = Math.min(replayFiles.size() - 1, selectedIndex + 1);
        } else if (inputManager.isKeyJustPressed(257) || inputManager.isKeyJustPressed(32)) { // 回车/空格
            if (!replayFiles.get(selectedIndex).equals("(无回放文件)")) {
                startReplay();
            }
        } else if (inputManager.isKeyJustPressed(256)) { // ESC
            engine.setScene(new MenuScene(engine, "MenuScene"));
        }
    }

    private void startReplay() {
        String filename = replayFiles.get(selectedIndex);
        inputManager.loadRecording(filename);
        Scene gameScene = new GameScene(engine);
        engine.setScene(gameScene);
        inputManager.startReplaying();
    }

    @Override
    public void render() {
        int width = renderer.getWidth();
        int height = renderer.getHeight();
        float centerX = width / 2.0f;

        renderer.drawRect(0, 0, width, height, 0.15f, 0.15f, 0.25f, 1.0f);

        String title = "选择回放";
        float titleWidth = title.length() * 24 * 0.6f;
        renderer.drawText(title, centerX - titleWidth / 2.0f, 80f, 1f, 1f, 1f, 1.0f, 24);

        float startY = 150f;
        for (int i = 0; i < replayFiles.size(); i++) {
            String filename = replayFiles.get(i);
            float textWidth = filename.length() * 16 * 0.6f;
            float textX = centerX - textWidth / 2.0f;
            float textY = startY + i * 40f;

            if (i == selectedIndex) {
                renderer.drawRect(textX - 10, textY - 8, textWidth + 20, 30, 0.5f, 0.4f, 0.2f, 0.8f);
                renderer.drawText(filename, textX, textY, 1f, 1f, 0.5f, 1.0f, 16);
            } else {
                renderer.drawText(filename, textX, textY, 0.8f, 0.8f, 0.8f, 1.0f, 16);
            }
        }

        String hint = "ARROWS: SELECT | ENTER: PLAY | ESC: BACK";
        float hintWidth = hint.length() * 14 * 0.6f;
        renderer.drawText(hint, centerX - hintWidth / 2.0f, height - 60, 0.6f, 0.6f, 0.6f, 1.0f, 14);
    }
}
