package platform;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long windowHandle;

    private static final int WIDTH = 1028;
    private static final int HEIGHT = 720;

    public Window() {
        if (!glfwInit()) {
            throw new RuntimeException("Could not initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        windowHandle = glfwCreateWindow(WIDTH, HEIGHT, "Title Of Window", NULL, NULL);

        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create GLFW Window");
        }
    }

    public long getHandle() {
        return windowHandle;
    }

    public void cleanup() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void pumpEvents() {
        glfwPollEvents();
    }
}
