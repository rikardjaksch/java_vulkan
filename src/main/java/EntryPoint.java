import platform.Window;
import rendering.RenderBackend;

public class EntryPoint {
    public static void main(String[] args) {
        Window window = new Window();
        RenderBackend backend = new RenderBackend(window);

        while (!window.shouldClose()) {
            window.pumpEvents();
        }

        backend.cleanup();
        window.cleanup();
    }
}
