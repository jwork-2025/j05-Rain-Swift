package com.gameengine.core;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.HealthComponent;
import com.gameengine.components.AttackComponent;
import com.gameengine.graphics.Renderer;
import com.gameengine.core.GameObject;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.List;
import java.util.ArrayList;

/**
 * 游戏逻辑类，处理具体的游戏规则
 */
public class GameLogic {
    private Scene scene;
    private InputManager inputManager;
    private float lastShotTime;
    
    public GameLogic(Scene scene) {
        this.scene = scene;
        this.inputManager = InputManager.getInstance();
    }
    
    /**
     * 处理玩家输入
     */
    public void handlePlayerInput() {
        List<GameObject> players = scene.findGameObjectsByComponent(TransformComponent.class);
        if (players.isEmpty()) return;
        
        GameObject player = players.get(0);
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        
        if (transform == null || physics == null) return;
        
        Vector2 movement = new Vector2();

        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(265)) { // W或上箭头
            movement.y -= 1;
        }
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(264)) { // S或下箭头
            movement.y += 1;
        }
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(263)) { // A或左箭头
            movement.x -= 1;
        }
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(262)) { // D或右箭头
            movement.x += 1;
        }
        // 空格键 - 近战攻击
        if (inputManager.isKeyPressed(32)) {
            AttackComponent attack = player.getComponent(AttackComponent.class);
            if (attack != null) {
                // 确定攻击方向
                Vector2 attackDirection;
                if (movement.magnitude() > 0) {
                    // 如果正在移动，攻击移动方向
                    attackDirection = movement.normalize();
                } else {
                    // 如果静止，使用角色朝向
                    attackDirection = new Vector2(
                        (float)Math.sin(transform.getRotation()),
                        (float)(-Math.cos(transform.getRotation()))
                    );
                }

                // 尝试执行攻击
                boolean attacked = attack.tryAttack(attackDirection);
                if (attacked) {
                    System.out.println("玩家发动近战攻击！");
                }
            }
        }

        // 鼠标左键 - 远程攻击（射击子弹）
        if (inputManager.isMouseButtonPressed(0)) { // 0 = 左键
            float currentTime = this.scene.getTime();
            if(currentTime - this.lastShotTime < 0.5f) {
                // 冷却中
            } else {
                this.lastShotTime = currentTime;

                // 获取鼠标位置
                Vector2 mousePos = inputManager.getMousePosition();
                Vector2 playerPos = transform.getPosition();

                // 计算从玩家到鼠标的方向
                Vector2 bulletDirection = mousePos.subtract(playerPos);
                if (bulletDirection.magnitude() > 0) {
                    bulletDirection = bulletDirection.normalize();

                    // 创建子弹
                    GameObject bullet = new GameObject("Bullet");

                    // 子弹位置从玩家位置开始
                    TransformComponent transform_bullet = bullet.addComponent(new TransformComponent(playerPos));

                    PhysicsComponent physics_bullet = bullet.addComponent(new PhysicsComponent(0.1f));
                    physics_bullet.setFriction(1.0f);
                    physics_bullet.setVelocity(bulletDirection.multiply(400));

                    RenderComponent render = bullet.addComponent(
                        new RenderComponent(
                            RenderComponent.RenderType.CIRCLE,
                            new Vector2(6, 6),
                            new RenderComponent.Color(1.0f, 1.0f, 0.0f, 1.0f)
                        )
                    );
                    // 设置渲染器
                    render.setRenderer(scene.getRenderer());

                    this.scene.addGameObject(bullet);
                    System.out.println("玩家发射子弹！");
                }
            }
        }
        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(200);
            physics.setVelocity(movement);
            // 使角色"前方"为朝上，将atan2结果加90°（PI/2）
            transform.setRotation((float)Math.atan2(movement.y, movement.x) + (float)Math.PI / 2f);
        }
        
        // 边界检查
        Vector2 pos = transform.getPosition();
        if (pos.x < 0) pos.x = 0;
        if (pos.y < 0) pos.y = 0;
        if (pos.x > 800 - 20) pos.x = 800 - 20;
        if (pos.y > 600 - 20) pos.y = 600 - 20;
        transform.setPosition(pos);
    }
    
    /**
     * 更新物理系统
     */
    public void updatePhysics() {
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        for (PhysicsComponent physics : physicsComponents) {
            TransformComponent transform = physics.getOwner().getComponent(TransformComponent.class);
            if (transform != null) {
                Vector2 pos = transform.getPosition();
                Vector2 velocity = physics.getVelocity();
                
                // 只对非子弹对象应用边界反弹
                String name = physics.getOwner().getName();
                if (!name.equals("Bullet") && !name.equals("EnemyBullet")) {
                    if (pos.x <= 0 || pos.x >= 800 - 15) {
                        velocity.x = -velocity.x;
                        physics.setVelocity(velocity);
                    }
                    if (pos.y <= 0 || pos.y >= 600 - 15) {
                        velocity.y = -velocity.y;
                        physics.setVelocity(velocity);
                    }

                    // 确保在边界内
                    if (pos.x < 0) pos.x = 0;
                    if (pos.y < 0) pos.y = 0;
                    if (pos.x > 800 - 15) pos.x = 800 - 15;
                    if (pos.y > 600 - 15) pos.y = 600 - 15;
                    transform.setPosition(pos);
                } else {
                    // 子弹飞出屏幕时销毁
                    if (pos.x < -10 || pos.x > 810 || pos.y < -10 || pos.y > 610) {
                        physics.getOwner().destroy();
                    }
                }
            }
        }
    }
    
    /**
     * 检查碰撞
     */
    public void checkCollisions() {
        // 直接查找玩家对象
        List<GameObject> players = scene.findGameObjectsByComponent(TransformComponent.class);
        if (players.isEmpty()) return;

        GameObject player = players.get(0);
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        AttackComponent playerAttack = player.getComponent(AttackComponent.class);
        if (playerTransform == null) return;

        // 检查近战攻击是否击中敌人
        if (playerAttack != null && playerAttack.isAttacking()) {
            List<GameObject> enemiesToRemove = new ArrayList<>();

            for (GameObject obj : scene.getGameObjects()) {
                if (obj.getName().equals("Enemy")) {
                    TransformComponent enemyTransform = obj.getComponent(TransformComponent.class);
                    if (enemyTransform != null) {
                        // 检查敌人是否在攻击范围内
                        if (playerAttack.isInAttackRange(enemyTransform.getPosition(), playerTransform.getPosition())) {
                            enemiesToRemove.add(obj);
                            System.out.println("近战攻击击中敌人！");
                        }
                    }
                }
            }

            // 移除被击中的敌人
            for (GameObject enemy : enemiesToRemove) {
                scene.removeGameObject(enemy);
                scene.onEnemyKilled();
            }
        }

        // 检查玩家与敌人的碰撞
        for (GameObject obj : scene.getGameObjects()) {
            if (obj.getName().equals("Enemy")) {
                TransformComponent enemyTransform = obj.getComponent(TransformComponent.class);
                if (enemyTransform != null) {
                    float distance = playerTransform.getPosition().distance(enemyTransform.getPosition());
                    if (distance < 15) {
                        // 碰撞！玩家受伤
                        if (playerHealth != null) {
                            boolean damaged = playerHealth.takeDamage(1);
                            if (damaged) {
                                System.out.println("玩家受伤！剩余生命: " + playerHealth.getCurrentHealth());
                            }
                        } else {
                            // 如果没有健康组件，使用旧的即死逻辑
                            scene.onEnemyLimitExceeded();
                        }
                        break;
                    }
                }
            }
        }

        // 检查子弹与敌人的碰撞（保留子弹系统以备后用）
        for (GameObject bul : scene.getGameObjects()) {
            if (bul.getName().equals("Bullet")) {
                TransformComponent bulletTransform = bul.getComponent(TransformComponent.class);
                if (bulletTransform != null) {
                    for (GameObject obj : scene.getGameObjects()) {
                        if (obj.getName().equals("Enemy")) {
                            TransformComponent enemyTransform = obj.getComponent(TransformComponent.class);
                            if (enemyTransform != null) {
                                float distance = bulletTransform.getPosition().distance(enemyTransform.getPosition());
                                if (distance < 25) {
                                    this.scene.removeGameObject(bul);
                                    this.scene.removeGameObject(obj);
                                    this.scene.onEnemyKilled();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 检查敌人子弹与玩家的碰撞
        for (GameObject bul : scene.getGameObjects()) {
            if (bul.getName().equals("EnemyBullet")) {
                TransformComponent bulletTransform = bul.getComponent(TransformComponent.class);
                if (bulletTransform != null && playerTransform != null) {
                    float distance = bulletTransform.getPosition().distance(playerTransform.getPosition());
                    if (distance < 20) {
                        this.scene.removeGameObject(bul);
                        if (playerHealth != null) {
                            boolean damaged = playerHealth.takeDamage(1);
                            if (damaged) {
                                System.out.println("玩家被子弹击中！剩余生命: " + playerHealth.getCurrentHealth());
                            }
                        }
                        break;
                    }
                }
            }
        }

    }

    public void checkEnemyCount() {
        if (this.scene.getEnemyCount() > 100) {
            System.out.println("游戏结束");
            scene.onEnemyLimitExceeded();
        }
    }
}
