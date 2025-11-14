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

/**
 * 游戏示例
 */
public class GameExample {
    public static void main(String[] args) {
        System.out.println("启动游戏引擎...");
        
        try {
            // 创建游戏引擎
            GameEngine engine = new GameEngine(800, 600,
                    "葫芦娃大战妖精");
            Scene menuScene = new MenuScene(engine,
                    "MenuScene");
            
            // 创建游戏场景
            Scene gameScene = new Scene("GameScene") {
                private IRenderer renderer;
                private Random random;
                private float time;
                private GameLogic gameLogic;
                private ScoreTable scoreTable;
                private FPSDisplay fpsDisplay;
                private boolean gameOver = false;
                
                @Override
                public void initialize() {
                    super.initialize();
                    this.renderer = engine.getRenderer();
                    this.random = new Random();
                    this.time = 0;
                    this.gameLogic = new GameLogic(this);

                    // 创建游戏对象
                    createPlayer();
                    createEnemies();
                    createDecorations();
                    createTable();
                    createFPSDisplay();
                    createProfileDisplay();
                    createHealthDisplay();
                }
                
                @Override
                public void update(float deltaTime) {
                    if (gameOver) return;

                    engine.getProfiler().begin("GameObjects");
                    super.update(deltaTime);
                    engine.getProfiler().end("GameObjects");

                    time += deltaTime;

                    // 使用游戏逻辑类处理游戏规则
                    engine.getProfiler().begin("PlayerInput");
                    gameLogic.handlePlayerInput();
                    engine.getProfiler().end("PlayerInput");

                    engine.getProfiler().begin("Physics");
                    gameLogic.updatePhysics();
                    engine.getProfiler().end("Physics");

                    engine.getProfiler().begin("Collisions");
                    gameLogic.checkCollisions();
                    engine.getProfiler().end("Collisions");

                    engine.getProfiler().begin("EnemyCount");
                    gameLogic.checkEnemyCount();
                    engine.getProfiler().end("EnemyCount");

                    // 生成新敌人
                    if (time > 2.0f) {
                        engine.getProfiler().begin("SpawnEnemy");
                        createEnemy();
                        scoreTable.updateEnemyCount(1);
                        engine.getProfiler().end("SpawnEnemy");
                        time = 0;
                    }
                }
                
                @Override
                public void render() {
                    // 绘制背景
                    renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);

                    // 渲染所有对象
                    super.render();

                    // 显示Game Over
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
                
                private void createTable() {
                    ScoreTable table = new ScoreTable();
                    this.scoreTable = table;
                    addGameObject(table);
                }

                private void createFPSDisplay() {
                    FPSDisplay display = new FPSDisplay();
                    this.fpsDisplay = display;
                    addGameObject(display);
                }

                private void createProfileDisplay() {
                    ProfileDisplay display = new ProfileDisplay();
                    addGameObject(display);
                }

                private void createHealthDisplay() {
                    HealthDisplay display = new HealthDisplay();
                    addGameObject(display);
                }

                final class ScoreTable extends GameObject {
                    private int score = 0;
                    private int enemyCount = 3;
                    public ScoreTable() {
                        super("ScoreTable");
                    }
                    @Override
                    public void update(float deltaTime) {
                        super.update(deltaTime);
                    }
                    @Override
                    public void render() {
                        renderer.drawText("Score: " + score + " EnemyCount: " + enemyCount, 8f, 28f, 1f, 1f, 1f, 1.0f, 20);
                    }
                    public void updateScore(int diff) {
                        score += diff;
                    }
                    public void updateEnemyCount(int diff) {
                        enemyCount += diff;
                    }
                }

                final class FPSDisplay extends GameObject {
                    public FPSDisplay() {
                        super("FPSDisplay");
                    }
                    @Override
                    public void update(float deltaTime) {
                        super.update(deltaTime);
                    }
                    @Override
                    public void render() {
                        float fps = engine.getCurrentFPS();
                        String fpsText = String.format("FPS: %.1f", fps);
                        renderer.drawText(fpsText, 700f, 28f, 0f, 1f, 0f, 1.0f, 20);
                    }
                }

                final class ProfileDisplay extends GameObject {
                    private boolean showDetailed = false;
                    private long lastToggleTime = 0;

                    public ProfileDisplay() {
                        super("ProfileDisplay");
                    }

                    @Override
                    public void update(float deltaTime) {
                        super.update(deltaTime);

                        // Press P to toggle detailed profiling display
                        if (engine.getInputManager().isKeyPressed(80)) { // P key
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastToggleTime > 300) {
                                showDetailed = !showDetailed;
                                lastToggleTime = currentTime;
                            }
                        }
                    }

                    @Override
                    public void render() {
                        if (!showDetailed) return;

                        // Draw semi-transparent background
                        renderer.drawRect(10f, 50f, 380f, 300f, 0f, 0f, 0f, 0.7f);

                        // Draw title
                        renderer.drawText("=== Performance Profile (Press P to toggle) ===", 20f, 70f, 1f, 1f, 0f, 1.0f, 14);

                        // Get profiling data
                        var stats = engine.getProfiler().getAllStats();

                        // Sort by average duration (descending)
                        var entries = new java.util.ArrayList<>(stats.entrySet());
                        entries.sort((a, b) -> Long.compare(b.getValue().avgDuration, a.getValue().avgDuration));

                        // Calculate total time
                        long totalTime = 0;
                        for (var entry : entries) {
                            totalTime += entry.getValue().avgDuration;
                        }

                        // Draw each section
                        float y = 90f;
                        int count = 0;
                        for (var entry : entries) {
                            if (count >= 12) break; // Limit to top 12 entries

                            String section = entry.getKey();
                            var data = entry.getValue();

                            double avgMs = data.getAvgMs();
                            double percentage = totalTime > 0 ? (data.avgDuration * 100.0 / totalTime) : 0;

                            // Color based on time consumption
                            float r = percentage > 20 ? 1f : (percentage > 10 ? 1f : 0.5f);
                            float g = percentage > 20 ? 0f : (percentage > 10 ? 0.5f : 1f);
                            float b = 0f;

                            String text = String.format("%-15s: %5.2fms (%4.1f%%)",
                                section.length() > 15 ? section.substring(0, 15) : section,
                                avgMs, percentage);

                            renderer.drawText(text, 20f, y, r, g, b, 1.0f, 12);
                            y += 18f;
                            count++;
                        }

                        // Draw total
                        y += 5f;
                        double totalMs = totalTime / 1_000_000.0;
                        renderer.drawText(String.format("Total: %.2fms", totalMs), 20f, y, 1f, 1f, 1f, 1.0f, 14);
                    }
                }

                final class HealthDisplay extends GameObject {
                    public HealthDisplay() {
                        super("HealthDisplay");
                    }

                    @Override
                    public void update(float deltaTime) {
                        super.update(deltaTime);
                    }

                    @Override
                    public void render() {
                        // Find the player and get their health using the outer class reference
                        List<GameObject> allObjects = getGameObjects();
                        GameObject player = null;
                        for (GameObject obj : allObjects) {
                            if (obj.getName().equals("Player")) {
                                player = obj;
                                break;
                            }
                        }
                        if (player == null) return;

                        HealthComponent health = player.getComponent(HealthComponent.class);
                        if (health == null) return;

                        int currentHealth = health.getCurrentHealth();
                        int maxHealth = health.getMaxHealth();

                        // Draw hearts in top-left corner
                        float startX = 8f;
                        float startY = 50f;
                        float heartSize = 20f;
                        float spacing = 25f;

                        for (int i = 0; i < maxHealth; i++) {
                            float x = startX + i * spacing;

                            if (i < currentHealth) {
                                // Full heart (red)
                                renderer.drawRect(x, startY, heartSize, heartSize, 1f, 0f, 0f, 1.0f);
                                // Inner highlight
                                renderer.drawRect(x + 5, startY + 5, 10, 10, 1f, 0.5f, 0.5f, 1.0f);
                            } else {
                                // Empty heart (dark gray outline)
                                renderer.drawRect(x, startY, heartSize, heartSize, 0.3f, 0.3f, 0.3f, 1.0f);
                                // Inner empty
                                renderer.drawRect(x + 2, startY + 2, heartSize - 4, heartSize - 4, 0.1f, 0.1f, 0.2f, 1.0f);
                            }
                        }

                        // Draw health text
                        renderer.drawText("Health:", startX, startY - 5f, 1f, 1f, 1f, 1.0f, 14);
                    }
                }

                public int getEnemyCount() {
                    return scoreTable.enemyCount;
                }
                
                private void createPlayer() {
                    // 创建葫芦娃 - 所有部位都在一个GameObject中
                    GameObject player = new GameObject("Player") {
                        private Vector2 basePosition;
                        private float facingDirection = 0f; // 0 = right, PI = left
                        private float flashTimer = 0f;

                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);

                            // Check if player is dead
                            HealthComponent health = getComponent(HealthComponent.class);
                            if (health != null && health.isDead()) {
                                gameOver = true;
                                return;
                            }

                            // Update facing direction based on horizontal movement
                            PhysicsComponent physics = getComponent(PhysicsComponent.class);
                            if (physics != null) {
                                Vector2 velocity = physics.getVelocity();
                                if (velocity.x < 0) facingDirection = -1f;
                                else if (velocity.x > 0) facingDirection = 1f;
                            }

                            updateComponents(deltaTime);

                            // 更新所有部位的位置
                            updateBodyParts();

                            // Update flash timer for invincibility visual
                            flashTimer += deltaTime;
                        }

                        @Override
                        public void render() {
                            // 渲染攻击特效
                            renderAttackEffect();
                            // 渲染所有部位
                            renderBodyParts();
                        }

                        private void updateBodyParts() {
                            TransformComponent transform = getComponent(TransformComponent.class);
                            if (transform != null) {
                                basePosition = transform.getPosition();
                            }
                        }

                        private void renderAttackEffect() {
                            if (basePosition == null) return;

                            AttackComponent attack = getComponent(AttackComponent.class);
                            if (attack == null || !attack.isAttacking()) return;

                            // 获取攻击方向和范围
                            Vector2 attackDir = attack.getAttackDirection();
                            float attackRange = attack.getAttackRange();
                            float progress = attack.getAttackProgress();

                            // 计算攻击弧的中心点（在玩家前方）
                            float centerX = basePosition.x + 20 + attackDir.x * 30;
                            float centerY = basePosition.y + 20 + attackDir.y * 30;

                            // 绘制攻击范围圆（半透明红色，随进度变化）
                            float alpha = 0.3f + progress * 0.4f; // 0.3 到 0.7
                            float size = attackRange * (0.5f + progress * 0.5f); // 范围随进度增长

                            // 绘制攻击圆
                            renderer.drawRect(
                                centerX - size/2,
                                centerY - size/2,
                                size,
                                size,
                                1.0f, 0.3f, 0.3f, alpha
                            );

                            // 绘制攻击方向指示线
                            float lineEndX = centerX + attackDir.x * attackRange;
                            float lineEndY = centerY + attackDir.y * attackRange;

                            // 用多个小矩形模拟线条
                            for (int i = 0; i < 5; i++) {
                                float t = i / 5.0f;
                                float x = centerX + (lineEndX - centerX) * t;
                                float y = centerY + (lineEndY - centerY) * t;
                                renderer.drawRect(x - 2, y - 2, 4, 4, 1.0f, 0.5f, 0.0f, 0.8f);
                            }
                        }

                        private void renderBodyParts() {
                            if (basePosition == null) return;

                            // Flash when invincible
                            HealthComponent health = getComponent(HealthComponent.class);
                            if (health != null && health.isInvincible()) {
                                // Flash every 0.1 seconds
                                if ((int)(flashTimer * 10) % 2 == 0) {
                                    return; // Skip rendering to create flash effect
                                }
                            }

                            if(facingDirection > 0){
                                renderer.drawImage("src/resource/cyan-right.png", basePosition.x, basePosition.y, 40, 40);
                            }else{
                                renderer.drawImage("src/resource/cyan-left.png", basePosition.x, basePosition.y, 40, 40);
                            }
                        }
                    };

                    // 添加变换组件
                    TransformComponent transform = player.addComponent(new TransformComponent(new Vector2(400, 300)));

                    // 添加物理组件
                    PhysicsComponent physics = player.addComponent(new PhysicsComponent(1.0f));
                    physics.setFriction(0.9f);

                    // 添加健康组件 - 5点生命值
                    HealthComponent health = player.addComponent(new HealthComponent(5));

                    // 添加攻击组件 - 攻击范围60, 冷却时间0.5秒, 伤害1
                    AttackComponent attack = player.addComponent(new AttackComponent(60.0f, 0.5f, 1));

                    addGameObject(player);
                }
                
                private void createEnemies() {
                    // 创建初始敌人：1个蛇，1个小兵，1个蝎子
                    createSnakeEnemy();
                    createMinionEnemy();
                    createScorpionEnemy();
                }

                @Override
                public void onEnemyLimitExceeded() {
                    gameOver = true;
                }

                // 随机创建一个敌人（用于游戏过程中生成）
                private void createEnemy() {
                    int enemyType = random.nextInt(3);
                    switch (enemyType) {
                        case 0:
                            createSnakeEnemy();
                            break;
                        case 1:
                            createMinionEnemy();
                            break;
                        case 2:
                            createScorpionEnemy();
                            break;
                    }
                }

                // 创建蛇敌人 - 平衡型
                private void  createSnakeEnemy() {
                    final Scene scene = this;
                    GameObject enemy = new GameObject("Enemy") {
                        private float shootTimer = 0f;

                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);

                            // Track player
                            GameObject player = scene.findGameObjectByName("Player");
                            if (player != null) {
                                TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                                TransformComponent enemyTransform = getComponent(TransformComponent.class);
                                PhysicsComponent physics = getComponent(PhysicsComponent.class);
                                RenderComponent render = getComponent(RenderComponent.class);

                                if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                                    Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                                    physics.applyForce(direction.multiply(100f)); // 中等速度

                                    // Update image based on direction
                                    String imagePath = (direction.x > 0 ? "src/resource/snake-right.png" : "src/resource/snake-left.png");
                                    render.setImagePath(imagePath);

                                    // Shoot bullet every 3 seconds
                                    shootTimer += deltaTime;
                                    if (shootTimer >= 3.0f) {
                                        shootTimer = 0f;

                                        // Create bullet
                                        GameObject bullet = new GameObject("EnemyBullet");
                                        bullet.addComponent(new TransformComponent(enemyTransform.getPosition()));

                                        PhysicsComponent bulletPhysics = bullet.addComponent(new PhysicsComponent(0.1f));
                                        bulletPhysics.setFriction(1.0f);
                                        bulletPhysics.setVelocity(direction.multiply(300));

                                        RenderComponent bulletRender = bullet.addComponent(
                                            new RenderComponent(
                                                RenderComponent.RenderType.CIRCLE,
                                                new Vector2(6, 6),
                                                new RenderComponent.Color(1.0f, 0.0f, 0.0f, 1.0f)
                                            )
                                        );
                                        bulletRender.setRenderer(renderer);

                                        scene.addGameObject(bullet);
                                    }
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
                    RenderComponent render_component = enemy.addComponent(new RenderComponent("src/resource/snake-left.png", new Vector2(40, 40)));
                    render_component.setRenderer(renderer);
                    PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.5f));
                    physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 100, (random.nextFloat() - 0.5f) * 100));
                    physics.setFriction(0.98f);

                    addGameObject(enemy);
                }

                // 创建小兵敌人 - 快速但脆弱
                private void createMinionEnemy() {
                    final Scene scene = this;
                    GameObject enemy = new GameObject("Enemy") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);

                            GameObject player = scene.findGameObjectByName("Player");
                            if (player != null) {
                                TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                                TransformComponent enemyTransform = getComponent(TransformComponent.class);
                                PhysicsComponent physics = getComponent(PhysicsComponent.class);
                                RenderComponent render = getComponent(RenderComponent.class);

                                if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                                    Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                                    physics.applyForce(direction.multiply(180f)); // 快速！

                                    String imagePath = (direction.x > 0 ? "src/resource/minion-right.png" : "src/resource/minion-left.png");
                                    render.setImagePath(imagePath);
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
                    RenderComponent render_component = enemy.addComponent(new RenderComponent("src/resource/minion-left.png", new Vector2(35, 35))); // 稍小
                    render_component.setRenderer(renderer);
                    PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.3f)); // 轻量
                    physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 150, (random.nextFloat() - 0.5f) * 150));
                    physics.setFriction(0.95f); // 低摩擦，更灵活

                    addGameObject(enemy);
                }

                // 创建蝎子敌人 - 缓慢但坚韧
                private void createScorpionEnemy() {
                    final Scene scene = this;
                    GameObject enemy = new GameObject("Enemy") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);

                            GameObject player = scene.findGameObjectByName("Player");
                            if (player != null) {
                                TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                                TransformComponent enemyTransform = getComponent(TransformComponent.class);
                                PhysicsComponent physics = getComponent(PhysicsComponent.class);
                                RenderComponent render = getComponent(RenderComponent.class);

                                if (playerTransform != null && enemyTransform != null && physics != null && render != null) {
                                    Vector2 direction = playerTransform.getPosition().subtract(enemyTransform.getPosition()).normalize();
                                    physics.applyForce(direction.multiply(60f)); // 缓慢

                                    String imagePath = (direction.x > 0 ? "src/resource/Scorpion-right.png" : "src/resource/Scorpion-left.png");
                                    render.setImagePath(imagePath);
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
                    RenderComponent render_component = enemy.addComponent(new RenderComponent("src/resource/Scorpion-left.png", new Vector2(45, 45))); // 稍大
                    render_component.setRenderer(renderer);
                    PhysicsComponent physics = enemy.addComponent(new PhysicsComponent(0.8f)); // 重量级
                    physics.setVelocity(new Vector2((random.nextFloat() - 0.5f) * 60, (random.nextFloat() - 0.5f) * 60));
                    physics.setFriction(0.99f); // 高摩擦，移动缓慢

                    addGameObject(enemy);
                }
                
                private void createDecorations() {
                    for (int i = 0; i < 5; i++) {
                        createDecoration();
                    }
                }
                
                private void createDecoration() {
                    GameObject decoration = new GameObject("Decoration") {
                        @Override
                        public void update(float deltaTime) {
                            super.update(deltaTime);
                            updateComponents(deltaTime);
                        }
                        
                        @Override
                        public void render() {
                            renderComponents();
                        }
                    };
                    
                    // 随机位置
                    Vector2 position = new Vector2(
                        random.nextFloat() * 800,
                        random.nextFloat() * 600
                    );
                    
                    // 添加变换组件
                    TransformComponent transform = decoration.addComponent(new TransformComponent(position));
                    
                    // 添加渲染组件
                    RenderComponent render = decoration.addComponent(new RenderComponent(
                        RenderComponent.RenderType.CIRCLE,
                        new Vector2(5, 5),
                        new RenderComponent.Color(0.5f, 0.5f, 1.0f, 0.8f)
                    ));
                    render.setRenderer(renderer);
                    
                    addGameObject(decoration);
                }
            };
            
            // 设置场景
            engine.setScene(menuScene);

            // 运行游戏
            engine.run();
            
        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("游戏结束");
    }
}
