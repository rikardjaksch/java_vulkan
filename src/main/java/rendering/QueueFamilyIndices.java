package rendering;

class QueueFamilyIndices {
    Integer graphicsFamily;
    Integer presentFamily;

    boolean isComplete() {
        return graphicsFamily != null && presentFamily != null;
    }
}
