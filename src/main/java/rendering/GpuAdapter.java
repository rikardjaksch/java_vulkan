package rendering;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static rendering.VulkanHelpers._CHECK_;
import static rendering.VulkanHelpers.asPointerBuffer;

class GpuAdapter {
    private VkPhysicalDevice gpuDevice;
    private long surface;
    private QueueFamilyIndices familyIndices;

    private static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME)
            .collect(toSet());

    GpuAdapter(VkPhysicalDevice device, long surface) {
        gpuDevice = device;
        this.surface = surface;
        findQueueFamilies();
    }

    QueueFamilyIndices getFamilyIndices() {
        return familyIndices;
    }

    RenderingDevice createDevice() {
        try (MemoryStack stack = stackPush()) {
            VkDeviceQueueCreateInfo.Buffer queue_create_infos =
                    VkDeviceQueueCreateInfo.calloc(1, stack)
                    .sType$Default()
                    .queueFamilyIndex(familyIndices.graphicsFamily)
                    .pQueuePriorities(stack.floats(1.0f));

            VkPhysicalDeviceFeatures device_features = VkPhysicalDeviceFeatures.calloc(stack);

            VkDeviceCreateInfo create_info = VkDeviceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pQueueCreateInfos(queue_create_infos)
                    .pEnabledFeatures(device_features)
                    .ppEnabledExtensionNames(asPointerBuffer(DEVICE_EXTENSIONS));

            PointerBuffer device_ptr = stack.pointers(VK_NULL_HANDLE);
            _CHECK_(vkCreateDevice(gpuDevice, create_info, null, device_ptr), "Creating logical device");

            return new RenderingDevice(new VkDevice(device_ptr.get(0), gpuDevice, create_info), familyIndices);
        }
    }

    private void findQueueFamilies() {
        familyIndices = new QueueFamilyIndices();

        try(MemoryStack stack = stackPush()) {
            IntBuffer queue_family_count = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(gpuDevice, queue_family_count, null);

            VkQueueFamilyProperties.Buffer queue_families = VkQueueFamilyProperties.malloc(queue_family_count.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(gpuDevice, queue_family_count, queue_families);

            IntBuffer present_supported = stack.ints(VK_FALSE);

            for (int i = 0; i < queue_families.capacity(); ++i) {
                VkQueueFamilyProperties props = queue_families.get(i);

                if ((props.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    familyIndices.graphicsFamily = i;
                }

                vkGetPhysicalDeviceSurfaceSupportKHR(gpuDevice, i, surface, present_supported);
                if(present_supported.get(0) == VK_TRUE) {
                    familyIndices.presentFamily = i;
                }

                if (familyIndices.isComplete()) {
                    break;
                }
            }
        }
    }
}
