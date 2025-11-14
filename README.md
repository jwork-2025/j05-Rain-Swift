[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/iHSjCEgj)
# J05

本版本采用 LWJGL + OpenGL 实现纯 GPU 渲染，窗口与输入基于 GLFW，文本渲染通过 AWT 字体离屏生成纹理后在 OpenGL 中批量绘制。


## 核心类型与概念

- **Scene（场景）**：一组 `GameObject` 的容器，负责生命周期（`initialize/update/render/clear`）与场景间切换。示例：`MenuScene`, `GameScene`, `ReplayScene`。
- **GameObject（游戏对象）**：由多个 `Component` 组成的实体，管理自身更新与渲染委托。支持自定义 `render()`（如玩家外观组合）。
- **Component（组件）**：面向数据/单体行为的可组合单元，例如：
  - `TransformComponent`：位置/旋转/缩放（本项目主要使用位置与尺寸）
  - `PhysicsComponent`：速度/摩擦/运动学数据（行为由 `PhysicsSystem` 统一处理）
  - `RenderComponent`：基础形状绘制（矩形/圆等，颜色与尺寸）
- **System（系统）**：面向“过程”的批处理逻辑，跨对象统一执行。例如 `PhysicsSystem` 负责所有带 `PhysicsComponent` 的对象物理更新。并行物理计算通过 `ExecutorService` 线程池实现，按批处理提升多核利用。
- **IRenderer/GPURenderer**：渲染后端抽象与 LWJGL 实现，负责窗口/上下文/绘制 API 封装，文本纹理缓存与绘制。
- **EntityFactory**：常用外观/组合的建造器（如 Player、AI 外观），便于游戏与回放共享同一套“预制”。


## 游戏录制/回放机制

### 实现方式
- **InputManager 录制**：基于输入事件的序列化录制，记录所有键盘/鼠标事件及时间戳
  - 录制模式：捕获 `KEY_PRESSED/KEY_RELEASED/MOUSE_MOVED/MOUSE_PRESSED/MOUSE_RELEASED` 事件
  - 存储格式：Java 序列化（`.dat` 文件），包含事件类型、时间戳、按键码、鼠标位置等
  - 鼠标移动采样：50ms 间隔采样，避免过多事件

### 回放功能
- **精确时间回放**：按录制时间戳精确重放输入事件
- **回放控制**：
  - 暂停/恢复：`pauseReplay()` / `resumeReplay()`
  - 变速播放：`setReplaySpeed(0.1x - 5.0x)`
  - 进度查询：`getReplayProgress()` / `getCurrentReplayTime()`
- **状态管理**：回放时自动清空输入状态，结束后恢复正常模式

### 回放选择界面
- **ReplayScene**：独立的回放文件选择场景
  - 自动扫描项目根目录的 `.dat` 回放文件
  - 键盘/鼠标选择回放文件
  - 按 ESC 返回主菜单
- **MenuScene 集成**：主菜单新增 "REPLAY" 选项，进入回放选择界面


## 渲染系统优化

### GPURenderer 修复
- **OpenGL 状态管理**：修复纹理状态混乱导致的渲染失败
  - `drawRect/drawCircle/drawLine`：显式禁用纹理，启用混合模式
  - `drawText/drawImage`：正确启用纹理和混合
  - 每个绘制方法独立管理 OpenGL 状态，避免状态污染

### GameScene 渲染完善
- **玩家渲染**：添加完整的渲染逻辑
  - 攻击特效：半透明红色方块 + 橙色方向指示点
  - 生命值显示：红色心形图标（左上角）
  - 玩家图片：根据朝向显示不同图片
- **UI 布局优化**：
  - 分数统计：左上角 (10, 10)，字体 16px
  - FPS 显示：右上角 (720, 10)，绿色，字体 16px
  - 生命值标签："Health:" (10, 40)，字体 14px
  - 生命值图标：(10, 60)，心形大小 18px，间距 22px


## 菜单界面优化

### MenuScene 布局调整
- **标题**：背景框更紧凑（50px 高度）
- **按钮**：高度 40px，间距 60px，适配 3 个选项
- **文字宽度计算**：使用 `fontSize * 0.6f` 公式，更准确的居中对齐
- **提示文字**：距底部 60px


## 编译与运行

1) 下载 LWJGL 依赖与原生库（按平台自动处理）

```bash
./download_lwjgl.sh
```

2) 编译并启动（脚本会自动编译 src/main/java 下所有源码并运行）

```bash
./run.sh
```


## 作业要求

- 参考本仓库代码，完善你自己的游戏：
 
- 为你的游戏设计并实现“存档与回放”功能：
  - 存档：定义存储抽象（文件/网络/内存均可），录制关键帧 + 输入/事件
  - 回放：读取存档，恢复对象状态并插值渲染，保证外观与行为可见且稳定

提示：请尽量保持模块解耦（渲染/输入/逻辑/存储）。

**重要提醒：尽量手写代码，不依赖自动生成，考试会考！**