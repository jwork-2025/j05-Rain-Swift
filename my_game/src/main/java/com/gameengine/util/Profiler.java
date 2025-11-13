package com.gameengine.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能分析器 - 用于测量游戏各部分的时间消耗
 */
public class Profiler {
    private static Profiler instance;

    // 当前帧的计时数据
    private Map<String, Long> startTimes;
    private Map<String, Long> durations;

    // 历史数据（用于计算平均值）
    private Map<String, List<Long>> history;
    private int historySize = 60; // 保存60帧的历史数据

    // 统计数据
    private Map<String, ProfileData> stats;

    private boolean enabled = true;

    private Profiler() {
        this.startTimes = new ConcurrentHashMap<>();
        this.durations = new ConcurrentHashMap<>();
        this.history = new ConcurrentHashMap<>();
        this.stats = new ConcurrentHashMap<>();
    }

    public static Profiler getInstance() {
        if (instance == null) {
            instance = new Profiler();
        }
        return instance;
    }

    /**
     * 开始计时一个区域
     */
    public void begin(String section) {
        if (!enabled) return;
        startTimes.put(section, System.nanoTime());
    }

    /**
     * 结束计时一个区域
     */
    public void end(String section) {
        if (!enabled) return;

        Long startTime = startTimes.get(section);
        if (startTime == null) {
            System.err.println("Profiler: end() called without begin() for section: " + section);
            return;
        }

        long duration = System.nanoTime() - startTime;
        durations.put(section, duration);

        // 添加到历史记录
        history.computeIfAbsent(section, k -> new ArrayList<>()).add(duration);

        // 限制历史记录大小
        List<Long> hist = history.get(section);
        if (hist.size() > historySize) {
            hist.remove(0);
        }

        startTimes.remove(section);
    }

    /**
     * 结束当前帧，计算统计数据
     */
    public void endFrame() {
        if (!enabled) return;

        // 计算每个区域的统计数据
        for (Map.Entry<String, Long> entry : durations.entrySet()) {
            String section = entry.getKey();
            long duration = entry.getValue();

            ProfileData data = stats.computeIfAbsent(section, k -> new ProfileData());
            data.lastDuration = duration;

            // 计算平均值
            List<Long> hist = history.get(section);
            if (hist != null && !hist.isEmpty()) {
                long sum = 0;
                long max = Long.MIN_VALUE;
                long min = Long.MAX_VALUE;

                for (long d : hist) {
                    sum += d;
                    max = Math.max(max, d);
                    min = Math.min(min, d);
                }

                data.avgDuration = sum / hist.size();
                data.maxDuration = max;
                data.minDuration = min;
            }
        }

        durations.clear();
    }

    /**
     * 获取某个区域的统计数据
     */
    public ProfileData getStats(String section) {
        return stats.get(section);
    }

    /**
     * 获取所有统计数据
     */
    public Map<String, ProfileData> getAllStats() {
        return new HashMap<>(stats);
    }

    /**
     * 获取格式化的报告
     */
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Performance Profile ===\n");

        // 按平均时间排序
        List<Map.Entry<String, ProfileData>> entries = new ArrayList<>(stats.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue().avgDuration, a.getValue().avgDuration));

        long totalTime = 0;
        for (Map.Entry<String, ProfileData> entry : entries) {
            totalTime += entry.getValue().avgDuration;
        }

        for (Map.Entry<String, ProfileData> entry : entries) {
            String section = entry.getKey();
            ProfileData data = entry.getValue();

            double avgMs = data.avgDuration / 1_000_000.0;
            double lastMs = data.lastDuration / 1_000_000.0;
            double maxMs = data.maxDuration / 1_000_000.0;
            double minMs = data.minDuration / 1_000_000.0;
            double percentage = totalTime > 0 ? (data.avgDuration * 100.0 / totalTime) : 0;

            sb.append(String.format("%-20s: %.3fms (%.1f%%) [last: %.3fms, min: %.3fms, max: %.3fms]\n",
                section, avgMs, percentage, lastMs, minMs, maxMs));
        }

        double totalMs = totalTime / 1_000_000.0;
        sb.append(String.format("\nTotal: %.3fms\n", totalMs));

        return sb.toString();
    }

    /**
     * 清空所有数据
     */
    public void reset() {
        startTimes.clear();
        durations.clear();
        history.clear();
        stats.clear();
    }

    /**
     * 启用/禁用分析器
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 性能数据类
     */
    public static class ProfileData {
        public long lastDuration;  // 最近一次的时间（纳秒）
        public long avgDuration;   // 平均时间（纳秒）
        public long maxDuration;   // 最大时间（纳秒）
        public long minDuration;   // 最小时间（纳秒）

        public double getLastMs() {
            return lastDuration / 1_000_000.0;
        }

        public double getAvgMs() {
            return avgDuration / 1_000_000.0;
        }

        public double getMaxMs() {
            return maxDuration / 1_000_000.0;
        }

        public double getMinMs() {
            return minDuration / 1_000_000.0;
        }
    }
}
