package com.gameengine.graphics;

public class RendererFactory {
    public static IRenderer createRenderer(RenderBackend backend, int width, int height, String title) {
        switch (backend) {
            case GPU:
                return new GPURenderer(width, height, title);
            case SWING:
                return new Renderer(width, height, title);
            default:
                throw new IllegalArgumentException("Unknown render backend: " + backend);
        }
    }
}
