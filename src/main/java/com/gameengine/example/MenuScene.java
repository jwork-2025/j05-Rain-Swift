package com.gameengine.example;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

public class MenuScene extends Scene {
    public enum MenuOption {
        START_GAME,
        REPLAY,
        EXIT
    }

    private IRenderer renderer;
    private InputManager inputManager;
    private GameEngine engine;
    private int selectedIndex;
    private MenuOption[] options;
    private boolean selectionMade;
    private MenuOption selectedOption;

    public MenuScene(GameEngine engine, String name) {
        super(name);
        this.engine = engine;
        this.renderer = engine.getRenderer();
        this.inputManager = InputManager.getInstance();
        this.selectedIndex = 0;
        this.options = new MenuOption[]{MenuOption.START_GAME, MenuOption.REPLAY, MenuOption.EXIT};
        this.selectionMade = false;
        this.selectedOption = null;
    }

    @Override
    public void initialize() {
        super.initialize();
        selectedIndex = 0;
        selectionMade = false;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        handleMenuSelection();
        if (selectionMade) {
            processSelection();
        }
    }

    private void handleMenuSelection() {
        if (inputManager.isKeyJustPressed(265) || inputManager.isKeyJustPressed(87)) { // 上箭头或W
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        } else if (inputManager.isKeyJustPressed(264) || inputManager.isKeyJustPressed(83)) { // 下箭头或S
            selectedIndex = (selectedIndex + 1) % options.length;
        } else if (inputManager.isKeyJustPressed(257) || inputManager.isKeyJustPressed(32)) { // 回车或空格
            selectionMade = true;
            selectedOption = options[selectedIndex];
            if (selectedOption == MenuOption.EXIT) {
                engine.stop();
                System.exit(0);
            }
        }

        Vector2 mousePos = inputManager.getMousePosition();
        if (inputManager.isMouseButtonJustPressed(0)) {
            float centerY = renderer.getHeight() / 2.0f;
            for (int i = 0; i < options.length; i++) {
                float buttonY = centerY - 20.0f + i * 60.0f;
                if (mousePos.y >= buttonY - 20 && mousePos.y <= buttonY + 20) {
                    selectedIndex = i;
                    selectionMade = true;
                    selectedOption = options[i];
                    if (selectedOption == MenuOption.EXIT) {
                        engine.stop();
                        System.exit(0);
                    }
                    break;
                }
            }
        }
    }

    private void processSelection() {
        if (selectedOption == MenuOption.START_GAME) {
            Scene gameScene = new GameScene(engine);
            engine.setScene(gameScene);
        } else if (selectedOption == MenuOption.REPLAY) {
            Scene replayScene = new ReplayScene(engine);
            engine.setScene(replayScene);
        }
    }

    @Override
    public void render() {
        if (renderer == null) return;

        int width = renderer.getWidth();
        int height = renderer.getHeight();

        renderer.drawRect(0, 0, width, height, 0.25f, 0.25f, 0.35f, 1.0f);
        super.render();
        renderMainMenu();
    }

    private void renderMainMenu() {
        if (renderer == null) return;

        int width = renderer.getWidth();
        int height = renderer.getHeight();
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        String title = "葫芦娃大战妖精";
        float titleWidth = title.length() * 24 * 0.6f;
        float titleX = centerX - titleWidth / 2.0f;
        float titleY = 100.0f;

        renderer.drawRect(centerX - titleWidth / 2.0f - 15, titleY - 10, titleWidth + 30, 50, 0.4f, 0.4f, 0.5f, 0.8f);
        renderer.drawText(title, titleX, titleY, 1.0f, 1.0f, 1.0f, 1.0f, 24);

        for (int i = 0; i < options.length; i++) {
            String text;
            if (options[i] == MenuOption.START_GAME) text = "START GAME";
            else if (options[i] == MenuOption.REPLAY) text = "REPLAY";
            else text = "EXIT";
            float textWidth = text.length() * 20 * 0.6f;
            float textX = centerX - textWidth / 2.0f;
            float textY = centerY - 20.0f + i * 60.0f;

            if (i == selectedIndex) {
                renderer.drawRect(textX - 15, textY - 10, textWidth + 30, 40, 0.6f, 0.5f, 0.2f, 0.9f);
                renderer.drawText(text, textX, textY, 1.0f, 1.0f, 0.5f, 1.0f, 20);
            } else {
                renderer.drawRect(textX - 15, textY - 10, textWidth + 30, 40, 0.2f, 0.2f, 0.3f, 0.5f);
                renderer.drawText(text, textX, textY, 0.95f, 0.95f, 0.95f, 1.0f, 20);
            }
        }

        String hint = "USE ARROWS OR MOUSE TO SELECT, ENTER TO CONFIRM";
        float hintWidth = hint.length() * 14 * 0.6f;
        renderer.drawText(hint, centerX - hintWidth / 2.0f, height - 60, 0.6f, 0.6f, 0.6f, 1.0f, 14);
    }
}
