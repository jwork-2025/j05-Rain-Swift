#!/bin/bash
cd "$(dirname "$0")"
mkdir -p bin

# 构建LWJGL classpath
LWJGL_DIR="lib/lwjgl"
LWJGL_CP="$LWJGL_DIR/lwjgl-3.3.6.jar:$LWJGL_DIR/lwjgl-glfw-3.3.6.jar:$LWJGL_DIR/lwjgl-opengl-3.3.6.jar"
LWJGL_CP="$LWJGL_CP:$LWJGL_DIR/lwjgl-3.3.6-natives-macos-arm64.jar"
LWJGL_CP="$LWJGL_CP:$LWJGL_DIR/lwjgl-glfw-3.3.6-natives-macos-arm64.jar"
LWJGL_CP="$LWJGL_CP:$LWJGL_DIR/lwjgl-opengl-3.3.6-natives-macos-arm64.jar"

# 编译
javac -d bin -cp "$LWJGL_CP" -sourcepath src/main/java src/main/java/com/gameengine/example/GameExample.java

# 运行 (macOS需要-XstartOnFirstThread参数)
java -XstartOnFirstThread -cp "bin:$LWJGL_CP" com.gameengine.example.GameExample
