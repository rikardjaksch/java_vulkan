package rendering.vulkan;

import org.lwjgl.vulkan.VkInstance;

import java.util.Set;

public class Instance {
    private VkInstance handle;

    public Instance() {
        handle = null;
    }

    public void initialize(Set<String> required_extensions) {
        if (valid()) {
            throw new RuntimeException("Instance is already initialized.")
        }

        // Create the instance here
    }

    public VkInstance handle() { return handle; }
    public boolean valid() { return handle != null; }

    /**
     * @return A set of names for all available extensions.
     */
    public static Set<String> enumerate_available_extensions() {
        return null;
    }
}
