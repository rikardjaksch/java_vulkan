package rendering;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import platform.Window;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static rendering.VulkanHelpers._CHECK_;

public class RenderBackend {
    private VkInstance instance = null;
    private GpuAdapter gpuAdapter = null;
    private RenderingDevice device = null;
    private long windowSurface;

    public RenderBackend(Window window) {
        createInstance();
        createSurface(window);
        enumerateGpuAdapters();
        createRenderDevice();
    }

    public void cleanup() {
        if (device != null) device.destroy();
        if (instance != null) vkDestroyInstance(instance, null);
    }

    private void createInstance() {
        try (MemoryStack stack = stackPush()) {
            VkApplicationInfo app_info = VkApplicationInfo.calloc(stack);

            app_info.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            app_info.pApplicationName(stack.UTF8Safe("Hello World"));
            app_info.applicationVersion(VK_MAKE_VERSION(1, 0,0));
            app_info.pEngineName(stack.UTF8Safe("veng"));
            app_info.engineVersion(VK_MAKE_VERSION(1, 0,0));
            app_info.apiVersion(VK.getInstanceVersionSupported());

            VkInstanceCreateInfo create_info = VkInstanceCreateInfo.calloc(stack);
            create_info.pApplicationInfo(app_info);
            create_info.ppEnabledExtensionNames(getRequiredInstanceExtensions());
            create_info.ppEnabledLayerNames(null);

            PointerBuffer instancePtr = stack.mallocPointer(1);

            _CHECK_(vkCreateInstance(create_info, null, instancePtr), "Creating instance.");
            instance = new VkInstance(instancePtr.get(0), create_info);
        }
    }

    private void createSurface(Window window) {
        try (MemoryStack stack = stackPush()) {
            LongBuffer surface_ptr = stack.longs(VK_NULL_HANDLE);

            if (glfwCreateWindowSurface(instance, window.getHandle(), null, surface_ptr) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create window surface");
            }

            windowSurface = surface_ptr.get(0);
        }
    }

    private PointerBuffer getRequiredInstanceExtensions() {
        PointerBuffer required_extensions = glfwGetRequiredInstanceExtensions();
        // TODO: Debug extensions if we have enabled them for the build
        return required_extensions;
    }

    private void enumerateGpuAdapters() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer device_count = stack.ints(0);

            vkEnumeratePhysicalDevices(instance, device_count, null);
            int num_devices = device_count.get(0);

            if (num_devices == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support.");
            }

            PointerBuffer physical_devices = stack.mallocPointer(num_devices);
            vkEnumeratePhysicalDevices(instance, device_count, physical_devices);

            for (int i = 0; i < num_devices; ++i) {
                GpuAdapter adapter = new GpuAdapter(new VkPhysicalDevice(physical_devices.get(i), instance), windowSurface);
                if (isDeviceSuitable(adapter)) {
                    gpuAdapter = adapter;
                    return;
                }
            }

            throw new RuntimeException("Failed to find a suitable GPU");
        }
    }

    private boolean isDeviceSuitable(GpuAdapter adapter) {
        QueueFamilyIndices indices = adapter.getFamilyIndices();
        return indices.isComplete();
    }

    private void createRenderDevice() {
        device = gpuAdapter.createDevice();
    }
}
