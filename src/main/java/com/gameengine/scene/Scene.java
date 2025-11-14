package com.gameengine.scene;

import com.gameengine.core.GameObject;
import com.gameengine.core.Component;
import com.gameengine.graphics.IRenderer;
// 移除具体游戏逻辑的import
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.min;

/**
 * 场景类，管理游戏对象和组件
 */
public class Scene {
    private String name;
    private List<GameObject> gameObjects;
    private List<GameObject> objectsToAdd;
    private List<GameObject> objectsToRemove;
    private boolean initialized;
    private float time;
    private ExecutorService executor;
    private long parallelTime = 0;
    private int frameCount = 0;
    // 移除未使用的组件索引

    public Scene(String name) {
        this.name = name;
        this.gameObjects = new ArrayList<>();
        this.objectsToAdd = new ArrayList<>();
        this.objectsToRemove = new ArrayList<>();
        this.initialized = false;
        this.time = 0.0f;
        executor = Executors.newCachedThreadPool();
    }
    
    /**
     * 初始化场景
     */
    public void initialize() {
        for (GameObject obj : gameObjects) {
            obj.initialize();
        }
        initialized = true;
    }
    
    /**
     * 更新场景
     */
    public void update(float deltaTime) {
        // 更新时间
        time += deltaTime;
        
        // 添加新对象
        for (GameObject obj : objectsToAdd) {
            gameObjects.add(obj);
            if (initialized) {
                obj.initialize();
            }
        }
        objectsToAdd.clear();
        
        // 移除标记的对象
        for (GameObject obj : objectsToRemove) {
            gameObjects.remove(obj);
        }
        objectsToRemove.clear();
        
        // 更新所有活跃的游戏对象（并行）
        long start = System.nanoTime();
        int sz = gameObjects.size();
        int batch = (sz + 7) / 8;
        Set<Future<?>> futures = new HashSet<>();
        for(int i = 0; i<batch; i++) {
            final int s = i*8;
            final int e = min((i+1)*8, sz);
            futures.add(executor.submit(() -> {
                for(int j = s; j<e; j++) {
                    GameObject obj = gameObjects.get(j);
                    if (obj.isActive()) obj.update(deltaTime);
                }
            }));
        }
        for(Future<?> f : futures) {
            try { f.get(); } catch (Exception e) {}
        }

        parallelTime += System.nanoTime() - start;

        gameObjects.removeIf(obj -> !obj.isActive());

        if (++frameCount >= 100) {
            System.out.printf("Objects: %d | Parallel update: %.2fms\n",
                sz, parallelTime / 1_000_000.0 / frameCount);
            parallelTime = frameCount = 0;
        }
    }
    
    /**
     * 渲染场景（顺序渲染，OpenGL要求在主线程）
     */
    public void render() {
        for (GameObject obj : gameObjects) {
            if (obj.isActive()) {
                obj.render();
            }
        }
    }


    public void onEnemyKilled() {
    }
    
    /**
     * 添加/删除 游戏对象到场景
     */
    public void addGameObject(GameObject gameObject) {
        objectsToAdd.add(gameObject);
    }

    public void removeGameObject(GameObject gameObject) {
        objectsToRemove.add(gameObject);
    }


    /**
     * 根据名称查找游戏对象
     */
    public GameObject findGameObjectByName(String name) {
        return gameObjects.stream()
            .filter(obj -> obj.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据组件类型查找游戏对象
     */
    public <T extends Component<T>> List<GameObject> findGameObjectsByComponent(Class<T> componentType) {
        return gameObjects.stream()
            .filter(obj -> obj.hasComponent(componentType))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有具有指定组件的游戏对象
     */
    public <T extends Component<T>> List<T> getComponents(Class<T> componentType) {
        return findGameObjectsByComponent(componentType).stream()
            .map(obj -> obj.getComponent(componentType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 清空场景
     */
    public void clear() {
        gameObjects.clear();
        objectsToAdd.clear();
        objectsToRemove.clear();
    }
    
    /**
     * 获取场景名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取所有游戏对象
     */
    public List<GameObject> getGameObjects() {
        return new ArrayList<>(gameObjects);
    }
    
    // 移除具体游戏逻辑，让子类实现
    
    /**
     * 获取渲染器（需要在子类中实现）
     */
    public IRenderer getRenderer() {
        return null; // 子类需要重写此方法
    }
    
    /**
     * 获取场景运行时间（秒）
     */
    public float getTime() {
        return time;
    }

    public int getEnemyCount() {
        return 0;
    }

    public void onEnemyLimitExceeded() { }
}