[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/iHSjCEgj)
# J05

## 本作业添加的功能

**演示视频**：https://www.bilibili.com/video/BV16crWBNEYW/

### 游戏录制/回放机制

#### 实现方式
- **InputManager 录制**：基于输入事件的序列化录制，记录所有键盘/鼠标事件及时间戳
  - 录制模式：捕获 `KEY_PRESSED/KEY_RELEASED/MOUSE_MOVED/MOUSE_PRESSED/MOUSE_RELEASED` 事件
  - 存储格式：Java 序列化（`.dat` 文件），包含事件类型、时间戳、按键码、鼠标位置等
  - 鼠标移动采样：50ms 间隔采样，避免过多事件

#### 回放功能
- **精确时间回放**：按录制时间戳精确重放输入事件
- **回放控制**：
  - 暂停/恢复：`pauseReplay()` / `resumeReplay()`
  - 变速播放：`setReplaySpeed(0.1x - 5.0x)`
  - 进度查询：`getReplayProgress()` / `getCurrentReplayTime()`
- **状态管理**：回放时自动清空输入状态，结束后恢复正常模式

#### 回放选择界面
- **ReplayScene**：独立的回放文件选择场景
  - 自动扫描项目根目录的 `.dat` 回放文件
  - 键盘/鼠标选择回放文件
  - 按 ESC 返回主菜单
- **MenuScene 集成**：主菜单新增 "REPLAY" 选项，进入回放选择界面


### 渲染系统优化

#### GPURenderer 修复
- **OpenGL 状态管理**：修复纹理状态混乱导致的渲染失败
  - `drawRect/drawCircle/drawLine`：显式禁用纹理，启用混合模式
  - `drawText/drawImage`：正确启用纹理和混合
  - 每个绘制方法独立管理 OpenGL 状态，避免状态污染

#### GameScene 渲染完善
- **玩家渲染**：添加完整的渲染逻辑
  - 攻击特效：半透明红色方块 + 橙色方向指示点
  - 生命值显示：红色心形图标（左上角）
  - 玩家图片：根据朝向显示不同图片
- **UI 布局优化**：
  - 分数统计：左上角 (10, 10)，字体 16px
  - FPS 显示：右上角 (720, 10)，绿色，字体 16px
  - 生命值标签："Health:" (10, 40)，字体 14px
  - 生命值图标：(10, 60)，心形大小 18px，间距 22px
