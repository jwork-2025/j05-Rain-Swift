package com.gameengine.components;

import com.gameengine.core.Component;

/**
 * 健康组件 - 管理游戏对象的生命值
 */
public class HealthComponent extends Component {
    private int maxHealth;
    private int currentHealth;
    private boolean invincible;
    private float invincibilityTimer;
    private float invincibilityDuration;
    private boolean isDead;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.invincible = false;
        this.invincibilityTimer = 0.0f;
        this.invincibilityDuration = 1.0f; // 1秒无敌时间
        this.isDead = false;
    }

    @Override
    public void initialize() {
        // HealthComponent doesn't need special initialization
    }

    @Override
    public void update(float deltaTime) {
        // 更新无敌时间
        if (invincible) {
            invincibilityTimer -= deltaTime;
            if (invincibilityTimer <= 0) {
                invincible = false;
                invincibilityTimer = 0;
            }
        }
    }

    @Override
    public void render() {
        // HealthComponent doesn't render anything
    }

    /**
     * 受到伤害
     * @param damage 伤害值
     * @return 是否成功造成伤害（无敌时返回false）
     */
    public boolean takeDamage(int damage) {
        if (invincible || isDead) {
            return false;
        }

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
        } else {
            // 受伤后进入无敌状态
            invincible = true;
            invincibilityTimer = invincibilityDuration;
        }

        return true;
    }

    /**
     * 恢复生命值
     * @param amount 恢复量
     */
    public void heal(int amount) {
        if (isDead) return;

        currentHealth += amount;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    /**
     * 获取当前生命值
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * 获取最大生命值
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * 设置最大生命值
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    /**
     * 是否死亡
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * 是否处于无敌状态
     */
    public boolean isInvincible() {
        return invincible;
    }

    /**
     * 获取生命值百分比
     */
    public float getHealthPercentage() {
        return (float) currentHealth / maxHealth;
    }

    /**
     * 设置无敌状态
     */
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
        if (!invincible) {
            invincibilityTimer = 0;
        }
    }

    /**
     * 设置无敌持续时间
     */
    public void setInvincibilityDuration(float duration) {
        this.invincibilityDuration = duration;
    }

    /**
     * 复活
     */
    public void revive() {
        currentHealth = maxHealth;
        isDead = false;
        invincible = false;
        invincibilityTimer = 0;
    }
}
