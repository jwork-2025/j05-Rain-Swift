package com.gameengine.example;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameEngine;
import com.gameengine.core.GameLogic;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import java.util.List;
import java.util.Random;

public class GameScene extends Scene {
    private GameEngine engine;
    private IRenderer renderer;
    private Random random;
    private float time;
    private GameLogic gameLogic;
    private ScoreTable scoreTable;
    private boolean gameOver = false;

    public GameScene(GameEngine engine) {
        super("GameScene");
        this.engine = engine;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.random = new Random();
        this.time = 0;
        this.gameLogic = new GameLogic(this);
        createPlayer();
        createEnemies();
        createTable();
    }

    @Override
    public void update(float deltaTime) {
        if (gameOver) return;
        super.update(deltaTime);
        time += deltaTime;
        gameLogic.handlePlayerInput();
        gameLogic.updatePhysics();
        gameLogic.checkCollisions();
        gameLogic.checkEnemyCount();
        if (time > 2.0f) {
            createEnemy();
            scoreTable.updateEnemyCount(1);
            time = 0;
        }
    }

    @Override
    public void render() {
        renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);
        super.render();
        if (gameOver) {
            renderer.drawText("GAME OVER", 300f, 300f, 1f, 0f, 0f, 1.0f, 48);
        }
    }

    @Override
    public void onEnemyKilled() {
        if (scoreTable != null) {
            scoreTable.updateScore(1);
            scoreTable.updateEnemyCount(-1);
        }
    }

    @Override
    public IRenderer getRenderer() {
        return renderer;
    }

    public int getEnemyCount() {
        return scoreTable.enemyCount;
    }

    @Override
    public void onEnemyLimitExceeded() {
        gameOver = true;
    }

    private void createTable() {
        scoreTable = new ScoreTable();
        addGameObject(scoreTable);
    }

    private void createPlayer() {
        GameObject player = new GameObject("Player") {
            private Vector2 basePosition;
            private float facingDirection = 0f;

            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                HealthComponent health = getComponent(HealthComponent.class);
                if (health != null && health.isDead()) {
                    gameOver = true;
                    return;
                }
                PhysicsComponent physics = getComponent(PhysicsComponent.class);
                if (physics != null) {
                    Vector2 velocity = physics.getVelocity();
                    if (velocity.x < 0) facingDirection = -1f;
                    else if (velocity.x > 0) facingDirection = 1f;
                }
                updateComponents(deltaTime);
                TransformComponent transform = getComponent(TransformComponent.class);
                if (transform != null) basePosition = transform.getPosition();
            }

            @Override
            public void render() {
                if (basePosition == null) return;

                // 渲染攻击特效
                AttackComponent attack = getComponent(AttackComponent.class);
                if (attack != null && attack.isAttacking()) {
                    Vector2 attackDir = attack.getAttackDirection();
                    float attackRange = attack.getAttackRange();
                    float progress = attack.getAttackProgress();
                    float centerX = basePosition.x + 20 + attackDir.x * 30;
                    float centerY = basePosition.y + 20 + attackDir.y * 30;
                    float alpha = 0.3f + progress * 0.4f;
                    float size = attackRange * (0.5f + progress * 0.5f);
                    renderer.drawRect(centerX - size/2, centerY - size/2, size, size, 1.0f, 0.3f, 0.3f, alpha);
                    float lineEndX = centerX + attackDir.x * attackRange;
                    float lineEndY = centerY + attackDir.y * attackRange;
                    for (int i = 0; i < 5; i++) {
                        float t = i / 5.0f;
                        float x = centerX + (lineEndX - centerX) * t;
                        float y = centerY + (lineEndY - centerY) * t;
                        renderer.drawRect(x - 2, y - 2, 4, 4, 1.0f, 0.5f, 0.0f, 0.8f);
                    }
                }

                // 渲染玩家图片
                if (facingDirection > 0) {
                    renderer.drawImage("src/resource/cyan-right.png", basePosition.x, basePosition.y, 40, 40);
                } else {
                    renderer.drawImage("src/resource/cyan-left.png", basePosition.x, basePosition.y, 40, 40);
                }

                // 渲染生命值
                HealthComponent health = getComponent(HealthComponent.class);
                if (health != null) {
                    int currentHealth = health.getCurrentHealth();
                    int maxHealth = health.getMaxHealth();
                    float startX = 10f;
                    float startY = 60f;
                    float heartSize = 18f;
                    float spacing = 22f;
                    renderer.drawText("Health:", startX, startY - 20f, 1f, 1f, 1f, 1.0f, 14);
                    for (int i = 0; i < maxHealth; i++) {
                        float x = startX + i * spacing;
                        if (i < currentHealth) {
                            renderer.drawRect(x, startY, heartSize, heartSize, 1f, 0f, 0f, 1.0f);
                            renderer.drawRect(x + 4, startY + 4, 10, 10, 1f, 0.5f, 0.5f, 1.0f);
                        } else {
                            renderer.drawRect(x, startY, heartSize, heartSize, 0.3f, 0.3f, 0.3f, 1.0f);
                            renderer.drawRect(x + 2, startY + 2, heartSize - 4, heartSize - 4, 0.1f, 0.1f, 0.2f, 1.0f);
                        }
                    }
                }
            }
        };
        player.addComponent(new TransformComponent(new Vector2(400, 300)));
        PhysicsComponent physics = player.addComponent(new PhysicsComponent(1.0f));
        physics.setFriction(0.9f);
        player.addComponent(new HealthComponent(5));
        player.addComponent(new AttackComponent(60.0f, 0.5f, 1));
        addGameObject(player);
    }

    private void createEnemies() {
        createSnakeEnemy();
        createMinionEnemy();
        createScorpionEnemy();
    }

    private void createEnemy() {
        int enemyType = random.nextInt(3);
        switch (enemyType) {
            case 0: createSnakeEnemy(); break;
            case 1: createMinionEnemy(); break;
            case 2: createScorpionEnemy(); break;
        }
    }

    private void createSnakeEnemy() {
        GameObject enemy = new GameObject("Enemy") {
            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                GameObject player = findGameObjectByName("Player");
                if (player != null) {
                    TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                    TransformComponent enemyTransform = getComponent(TransformComponent.class);
                    PhysicsComponent physics = getComponent(PhysicsComponent.class);
                    RenderComponent render = getComponent(RenderComponent.class);
                    if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                        Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                        physics.applyForce(direction.multiply(100f));
                        render.setImagePath(direction.x > 0 ? "src/resource/snake-right.png" : "src/resource/snake-left.png");
                    }
                }
                updateComponents(deltaTime);
            }

            @Override
            public void render() {
                renderComponents();
            }
        };
        Vector2 position = new Vector2(random.nextFloat() * 800, random.nextFloat() * 600);
        enemy.addComponent(new TransformComponent(position));
        RenderComponent render = enemy.addComponent(new RenderComponent("src/resource/snake-left.png", new Vector2(40, 40)));
        render.setRenderer(renderer);
        PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.5f));
        physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 100, (random.nextFloat() - 0.5f) * 100));
        physics.setFriction(0.98f);
        addGameObject(enemy);
    }

    private void createMinionEnemy() {
        GameObject enemy = new GameObject("Enemy") {
            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                GameObject player = findGameObjectByName("Player");
                if (player != null) {
                    TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                    TransformComponent enemyTransform = getComponent(TransformComponent.class);
                    PhysicsComponent physics = getComponent(PhysicsComponent.class);
                    RenderComponent render = getComponent(RenderComponent.class);
                    if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                        Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                        physics.applyForce(direction.multiply(180f));
                        render.setImagePath(direction.x > 0 ? "src/resource/minion-right.png" : "src/resource/minion-left.png");
                    }
                }
                updateComponents(deltaTime);
            }

            @Override
            public void render() {
                renderComponents();
            }
        };
        Vector2 position = new Vector2(random.nextFloat() * 800, random.nextFloat() * 600);
        enemy.addComponent(new TransformComponent(position));
        RenderComponent render = enemy.addComponent(new RenderComponent("src/resource/minion-left.png", new Vector2(35, 35)));
        render.setRenderer(renderer);
        PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.3f));
        physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 150, (random.nextFloat() - 0.5f) * 150));
        physics.setFriction(0.95f);
        addGameObject(enemy);
    }

    private void createScorpionEnemy() {
        GameObject enemy = new GameObject("Enemy") {
            @Override
            public void update(float deltaTime) {
                super.update(deltaTime);
                GameObject player = findGameObjectByName("Player");
                if (player != null) {
                    TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                    TransformComponent enemyTransform = getComponent(TransformComponent.class);
                    PhysicsComponent physics = getComponent(PhysicsComponent.class);
                    RenderComponent render = getComponent(RenderComponent.class);
                    if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                        Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                        physics.applyForce(direction.multiply(60f));
                        render.setImagePath(direction.x > 0 ? "src/resource/Scorpion-right.png" : "src/resource/Scorpion-left.png");
                    }
                }
                updateComponents(deltaTime);
            }

            @Override
            public void render() {
                renderComponents();
            }
        };
        Vector2 position = new Vector2(random.nextFloat() * 800, random.nextFloat() * 600);
        enemy.addComponent(new TransformComponent(position));
        RenderComponent render = enemy.addComponent(new RenderComponent("src/resource/Scorpion-left.png", new Vector2(45, 45)));
        render.setRenderer(renderer);
        PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.8f));
        physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 60, (random.nextFloat() - 0.5f) * 60));
        physics.setFriction(0.99f);
        addGameObject(enemy);
    }

    final class ScoreTable extends GameObject {
        int score = 0;
        int enemyCount = 3;

        public ScoreTable() {
            super("ScoreTable");
        }

        @Override
        public void render() {
            renderer.drawText("Score: " + score + "  Enemies: " + enemyCount, 10f, 10f, 1f, 1f, 1f, 1.0f, 16);
            float fps = engine.getCurrentFPS();
            String fpsText = String.format("FPS: %.0f", fps);
            renderer.drawText(fpsText, 720f, 10f, 0f, 1f, 0f, 1.0f, 16);
        }

        public void updateScore(int diff) {
            score += diff;
        }

        public void updateEnemyCount(int diff) {
            enemyCount += diff;
        }
    }
}
