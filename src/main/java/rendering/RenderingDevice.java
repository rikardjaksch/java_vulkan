package rendering;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class RenderingDevice {
    private VkPhysicalDevice physical_device;
    private VkDevice device;
    private VkQueue graphicsQueue;

    public RenderingDevice(VkDevice device, QueueFamilyIndices familyIndices) {
        this.device = device;

        try (MemoryStack stack = stackPush()) {
            PointerBuffer graphics_queue = stack.pointers(VK_NULL_HANDLE);
            vkGetDeviceQueue(device, familyIndices.graphicsFamily, 0, graphics_queue);
            graphicsQueue = new VkQueue(graphics_queue.get(0), device);
        }
    }

    public void destroy() {
        vkDestroyDevice(device, null);
    }
}
