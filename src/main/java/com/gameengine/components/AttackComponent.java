package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.math.Vector2;

/**
 * 攻击组件 - 管理近战攻击
 */
public class AttackComponent extends Component {
    private float attackRange;        // 攻击范围
    private float attackCooldown;     // 攻击冷却时间
    private float cooldownTimer;      // 当前冷却计时器
    private boolean isAttacking;      // 是否正在攻击
    private float attackDuration;     // 攻击持续时间
    private float attackTimer;        // 攻击动画计时器
    private int attackDamage;         // 攻击伤害
    private Vector2 attackDirection;  // 攻击方向

    public AttackComponent(float attackRange, float attackCooldown, int attackDamage) {
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.attackDamage = attackDamage;
        this.cooldownTimer = 0.0f;
        this.isAttacking = false;
        this.attackDuration = 0.2f; // 攻击动画持续0.2秒
        this.attackTimer = 0.0f;
        this.attackDirection = new Vector2(0, -1); // 默认向上
    }

    @Override
    public void initialize() {
        // AttackComponent doesn't need special initialization
    }

    @Override
    public void update(float deltaTime) {
        // 更新冷却计时器
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaTime;
            if (cooldownTimer < 0) {
                cooldownTimer = 0;
            }
        }

        // 更新攻击动画计时器
        if (isAttacking) {
            attackTimer -= deltaTime;
            if (attackTimer <= 0) {
                isAttacking = false;
                attackTimer = 0;
            }
        }
    }

    @Override
    public void render() {
        // AttackComponent doesn't render anything directly
        // Visual feedback is handled by the owner GameObject
    }

    /**
     * 尝试执行攻击
     * @param direction 攻击方向
     * @return 是否成功执行攻击
     */
    public boolean tryAttack(Vector2 direction) {
        if (cooldownTimer > 0 || isAttacking) {
            return false;
        }

        // 开始攻击
        isAttacking = true;
        attackTimer = attackDuration;
        cooldownTimer = attackCooldown;

        // 设置攻击方向
        if (direction.magnitude() > 0) {
            attackDirection = direction.normalize();
        }

        return true;
    }

    /**
     * 检查某个位置是否在攻击范围内
     * @param targetPosition 目标位置
     * @param attackerPosition 攻击者位置
     * @return 是否在攻击范围内
     */
    public boolean isInAttackRange(Vector2 targetPosition, Vector2 attackerPosition) {
        if (!isAttacking) {
            return false;
        }

        // 计算到目标的距离
        float distance = targetPosition.distance(attackerPosition);
        if (distance > attackRange) {
            return false;
        }

        // 检查目标是否在攻击方向的前方（使用点积）
        Vector2 toTarget = targetPosition.subtract(attackerPosition).normalize();
        float dotProduct = toTarget.dot(attackDirection);

        // 如果点积大于0.5，说明目标在攻击方向的前方（约60度范围内）
        return dotProduct > 0.5f;
    }

    /**
     * 获取攻击范围
     */
    public float getAttackRange() {
        return attackRange;
    }

    /**
     * 设置攻击范围
     */
    public void setAttackRange(float attackRange) {
        this.attackRange = attackRange;
    }

    /**
     * 获取攻击冷却时间
     */
    public float getAttackCooldown() {
        return attackCooldown;
    }

    /**
     * 设置攻击冷却时间
     */
    public void setAttackCooldown(float attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    /**
     * 是否正在攻击
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * 是否在冷却中
     */
    public boolean isOnCooldown() {
        return cooldownTimer > 0;
    }

    /**
     * 获取冷却进度 (0-1)
     */
    public float getCooldownProgress() {
        if (attackCooldown <= 0) return 1.0f;
        return 1.0f - (cooldownTimer / attackCooldown);
    }

    /**
     * 获取攻击动画进度 (0-1)
     */
    public float getAttackProgress() {
        if (attackDuration <= 0) return 0.0f;
        return 1.0f - (attackTimer / attackDuration);
    }

    /**
     * 获取攻击伤害
     */
    public int getAttackDamage() {
        return attackDamage;
    }

    /**
     * 设置攻击伤害
     */
    public void setAttackDamage(int attackDamage) {
        this.attackDamage = attackDamage;
    }

    /**
     * 获取攻击方向
     */
    public Vector2 getAttackDirection() {
        return attackDirection;
    }

    /**
     * 获取攻击持续时间
     */
    public float getAttackDuration() {
        return attackDuration;
    }

    /**
     * 设置攻击持续时间
     */
    public void setAttackDuration(float attackDuration) {
        this.attackDuration = attackDuration;
    }
}
