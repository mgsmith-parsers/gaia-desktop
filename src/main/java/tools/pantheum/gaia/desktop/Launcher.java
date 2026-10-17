package tools.pantheum.gaia.desktop;

/**
 * {@code Main-Class} of the packaged jar — both the default layout (classes plus
 * {@code lib/}) and the {@code -Pfat} one.
 *
 * <p>In either, the JavaFX classes sit on the class path (the unnamed module) rather than the
 * module path, and the JavaFX launcher refuses to start when the jar's main class is itself an
 * {@link javafx.application.Application} subclass in that layout ("JavaFX runtime components
 * are missing"). Going through a plain class that calls {@code launch} sidesteps the check —
 * the toolkit still starts normally.
 *
 * <p>{@code mvn javafx:run} puts JavaFX on the module path and launches
 * {@link GaiaDesktopApp} directly, so this shim is only for the packaged jar.
 */
public final class Launcher {

    private Launcher() {}

    public static void main(String[] args) {
        GaiaDesktopApp.main(args);
    }
}
