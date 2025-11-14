package com.gameengine.input;

import com.gameengine.math.Vector2;

import java.util.*;
import java.io.*;

/**
 * 输入管理器，处理键盘和鼠标输入
 */
public class InputManager {
    private static InputManager instance;
    private Set<Integer> pressedKeys;
    private Set<Integer> justPressedKeys;
    private Map<Integer, Boolean> keyStates;
    private Vector2 mousePosition;
    private boolean[] mouseButtons;
    private boolean[] mouseButtonsJustPressed;
    private long startTime;

    public enum InputMode {
        NORMAL, RECORDING, REPLAYING
    }
    private InputMode currentMode;

    private enum EventType {
        MOUSE_MOVED, MOUSE_PRESSED, MOUSE_RELEASED, KEY_PRESSED, KEY_RELEASED
    }

    private static class InputEvent implements Serializable {
        long timestamp;
        EventType type;
        int keyCode;
        int button;
        Vector2 position;

        InputEvent(EventType type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    private List<InputEvent> events;
    private int replayIndex;
    private long replayStartTime;
    private long lastMouseMoveTime;
    private float replaySpeed = 1.0f;
    private boolean replayPaused = false;
    private long pauseStartTime = 0;
    private long totalPausedTime = 0;
    
    private InputManager() {
        pressedKeys = new HashSet<>();
        justPressedKeys = new HashSet<>();
        keyStates = new HashMap<>();
        mousePosition = new Vector2();
        mouseButtons = new boolean[3]; // 左键、右键、中键
        mouseButtonsJustPressed = new boolean[3];
        startTime = System.currentTimeMillis();
        events = new ArrayList<InputEvent>();
        currentMode = InputMode.NORMAL;
    }
    
    public static InputManager getInstance() {
        if (instance == null) {
            instance = new InputManager();
        }
        return instance;
    }
    
    /**
     * 更新输入状态
     */
    public void update() {
        justPressedKeys.clear();
        for (int i = 0; i < mouseButtonsJustPressed.length; i++) {
            mouseButtonsJustPressed[i] = false;
        }
    }
    
    /**
     * 处理键盘按下事件
     */
    public void onKeyPressed(int keyCode) {
        if (currentMode == InputMode.RECORDING) {
            InputEvent event = new InputEvent(EventType.KEY_PRESSED, System.currentTimeMillis() - startTime);
            event.keyCode = keyCode;
            events.add(event);
        }
        if (!pressedKeys.contains(keyCode)) {
            justPressedKeys.add(keyCode);
        }
        pressedKeys.add(keyCode);
        keyStates.put(keyCode, true);
    }

    /**
     * 处理键盘释放事件
     */
    public void onKeyReleased(int keyCode) {
        if (currentMode == InputMode.RECORDING) {
            InputEvent event = new InputEvent(EventType.KEY_RELEASED, System.currentTimeMillis() - startTime);
            event.keyCode = keyCode;
            events.add(event);
        }
        pressedKeys.remove(keyCode);
        keyStates.put(keyCode, false);
    }

    /**
     * 处理鼠标移动事件
     */
    public void onMouseMoved(float x, float y) {
        if (currentMode == InputMode.RECORDING) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMouseMoveTime > 50) {
                InputEvent event = new InputEvent(EventType.MOUSE_MOVED, currentTime - startTime);
                event.position = new Vector2(x, y);
                events.add(event);
                lastMouseMoveTime = currentTime;
            }
        }
        mousePosition.x = x;
        mousePosition.y = y;
    }

    /**
     * 处理鼠标按下事件
     */
    public void onMousePressed(int button) {
        if (button >= 0 && button < mouseButtons.length) {
            if (currentMode == InputMode.RECORDING) {
                InputEvent event = new InputEvent(EventType.MOUSE_PRESSED, System.currentTimeMillis() - startTime);
                event.button = button;
                events.add(event);
            }
            if (!mouseButtons[button]) {
                mouseButtonsJustPressed[button] = true;
            }
            mouseButtons[button] = true;
        }
    }

    /**
     * 处理鼠标释放事件
     */
    public void onMouseReleased(int button) {
        if (button >= 0 && button < mouseButtons.length) {
            if (currentMode == InputMode.RECORDING) {
                InputEvent event = new InputEvent(EventType.MOUSE_RELEASED, System.currentTimeMillis() - startTime);
                event.button = button;
                events.add(event);
            }
            mouseButtons[button] = false;
        }
    }
    
    /**
     * 检查按键是否被按下
     */
    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }
    
    /**
     * 检查按键是否刚刚被按下（只在这一帧为true）
     */
    public boolean isKeyJustPressed(int keyCode) {
        return justPressedKeys.contains(keyCode);
    }
    
    /**
     * 检查鼠标按键是否被按下
     */
    public boolean isMouseButtonPressed(int button) {
        if (button >= 0 && button < mouseButtons.length) {
            return mouseButtons[button];
        }
        return false;
    }
    
    /**
     * 检查鼠标按键是否刚刚被按下
     */
    public boolean isMouseButtonJustPressed(int button) {
        if (button >= 0 && button < mouseButtons.length) {
            return mouseButtonsJustPressed[button];
        }
        return false;
    }
    
    /**
     * 获取鼠标位置
     */
    public Vector2 getMousePosition() {
        return new Vector2(mousePosition);
    }
    
    /**
     * 获取鼠标X坐标
     */
    public float getMouseX() {
        return mousePosition.x;
    }
    
    /**
     * 获取鼠标Y坐标
     */
    public float getMouseY() {
        return mousePosition.y;
    }

    public void setMode(InputMode mode) {
        this.currentMode = mode;
    }

    public InputMode getMode() {
        return currentMode;
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        events.clear();
        startTime = System.currentTimeMillis();
        lastMouseMoveTime = 0;
        currentMode = InputMode.RECORDING;
        System.out.println("开始录制输入...");
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        currentMode = InputMode.NORMAL;
        System.out.println("停止录制，共录制 " + events.size() + " 个事件");
    }

    /**
     * 开始回放
     */
    public void startReplaying() {
        if (events.isEmpty()) {
            System.out.println("没有可回放的录制数据");
            return;
        }
        replayIndex = 0;
        replayStartTime = System.currentTimeMillis();
        replaySpeed = 1.0f;
        replayPaused = false;
        totalPausedTime = 0;
        currentMode = InputMode.REPLAYING;
        clearInputState();
        System.out.println("开始回放，共 " + events.size() + " 个事件");
    }

    /**
     * 停止回放
     */
    public void stopReplaying() {
        currentMode = InputMode.NORMAL;
        clearInputState();
        System.out.println("停止回放");
    }

    /**
     * 更新回放状态
     */
    public void updateReplay() {
        if (currentMode != InputMode.REPLAYING || replayIndex >= events.size()) {
            if (currentMode == InputMode.REPLAYING && replayIndex >= events.size()) {
                stopReplaying();
            }
            return;
        }

        if (replayPaused) return;

        long currentReplayTime = (long)((System.currentTimeMillis() - replayStartTime - totalPausedTime) * replaySpeed);

        while (replayIndex < events.size()) {
            InputEvent event = events.get(replayIndex);
            if (event.timestamp > currentReplayTime) {
                break;
            }

            applyEvent(event);
            replayIndex++;
        }
    }

    /**
     * 暂停回放
     */
    public void pauseReplay() {
        if (currentMode == InputMode.REPLAYING && !replayPaused) {
            replayPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }

    /**
     * 恢复回放
     */
    public void resumeReplay() {
        if (currentMode == InputMode.REPLAYING && replayPaused) {
            replayPaused = false;
            totalPausedTime += System.currentTimeMillis() - pauseStartTime;
        }
    }

    /**
     * 设置回放速度
     */
    public void setReplaySpeed(float speed) {
        this.replaySpeed = Math.max(0.1f, Math.min(speed, 5.0f));
    }

    /**
     * 获取回放速度
     */
    public float getReplaySpeed() {
        return replaySpeed;
    }

    /**
     * 应用录制的事件
     */
    private void applyEvent(InputEvent event) {
        switch (event.type) {
            case KEY_PRESSED:
                if (!pressedKeys.contains(event.keyCode)) {
                    justPressedKeys.add(event.keyCode);
                }
                pressedKeys.add(event.keyCode);
                keyStates.put(event.keyCode, true);
                break;
            case KEY_RELEASED:
                pressedKeys.remove(event.keyCode);
                keyStates.put(event.keyCode, false);
                break;
            case MOUSE_PRESSED:
                if (event.button >= 0 && event.button < mouseButtons.length) {
                    if (!mouseButtons[event.button]) {
                        mouseButtonsJustPressed[event.button] = true;
                    }
                    mouseButtons[event.button] = true;
                }
                break;
            case MOUSE_RELEASED:
                if (event.button >= 0 && event.button < mouseButtons.length) {
                    mouseButtons[event.button] = false;
                }
                break;
            case MOUSE_MOVED:
                if (event.position != null) {
                    mousePosition.x = event.position.x;
                    mousePosition.y = event.position.y;
                }
                break;
        }
    }

    /**
     * 清空输入状态
     */
    private void clearInputState() {
        pressedKeys.clear();
        justPressedKeys.clear();
        keyStates.clear();
        for (int i = 0; i < mouseButtons.length; i++) {
            mouseButtons[i] = false;
            mouseButtonsJustPressed[i] = false;
        }
    }

    /**
     * 保存录制到文件
     */
    public void saveRecording(String filepath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath))) {
            oos.writeObject(events);
            System.out.println("录制已保存到: " + filepath);
        } catch (IOException e) {
            System.err.println("保存录制失败: " + e.getMessage());
        }
    }

    /**
     * 从文件加载录制
     */
    @SuppressWarnings("unchecked")
    public void loadRecording(String filepath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            events = (List<InputEvent>) ois.readObject();
            System.out.println("录制已加载: " + filepath + "，共 " + events.size() + " 个事件");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("加载录制失败: " + e.getMessage());
        }
    }

    /**
     * 获取回放进度 (0.0 - 1.0)
     */
    public float getReplayProgress() {
        if (events.isEmpty()) return 0.0f;
        return (float) replayIndex / events.size();
    }

    /**
     * 获取录制总时长（毫秒）
     */
    public long getRecordingDuration() {
        if (events.isEmpty()) return 0;
        return events.get(events.size() - 1).timestamp;
    }

    /**
     * 获取当前回放时间（毫秒）
     */
    public long getCurrentReplayTime() {
        if (currentMode != InputMode.REPLAYING || replayIndex >= events.size()) return 0;
        return (long)((System.currentTimeMillis() - replayStartTime - totalPausedTime) * replaySpeed);
    }

    /**
     * 检查是否正在回放
     */
    public boolean isReplaying() {
        return currentMode == InputMode.REPLAYING;
    }

    /**
     * 检查是否正在录制
     */
    public boolean isRecording() {
        return currentMode == InputMode.RECORDING;
    }

    /**
     * 检查回放是否暂停
     */
    public boolean isReplayPaused() {
        return replayPaused;
    }

    /**
     * 获取录制的事件数量
     */
    public int getEventCount() {
        return events.size();
    }
}
