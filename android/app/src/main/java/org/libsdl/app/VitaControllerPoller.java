package org.libsdl.app;

// SDLControllerManager.pollInputDevices is package-private since SDL 3.4, so the emulator reaches it from here
public final class VitaControllerPoller {
    private VitaControllerPoller() {
    }

    public static void pollInputDevices() {
        SDLControllerManager.pollInputDevices();
    }
}
