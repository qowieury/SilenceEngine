/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Sri Harsha Chilakapati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.shc.silenceengine.backend.lwjgl.glfw;

import com.shc.silenceengine.backend.lwjgl.glfw.callbacks.*;
import com.shc.silenceengine.core.SilenceEngine;
import com.shc.silenceengine.graphics.Color;
import com.shc.silenceengine.graphics.Image;
import com.shc.silenceengine.io.FilePath;
import com.shc.silenceengine.math.Vector2;
import com.shc.silenceengine.math.Vector4;
import com.shc.silenceengine.utils.functional.SimpleCallback;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * <p> This class is an object oriented wrapper for GLFWwindow structure, which encapsulates both the window and the
 * context. For example, this creates a 640 by 480 windowed mode window.</p>
 *
 * <pre>
 *     Window window = new Window(640, 480, "My Title");
 * </pre>
 *
 * <p> There are several other overloads of the constructors allowing you to specify the size, title, monitor, a parent
 * window and the video mode. The window object is provided along with the input events, so the event handlers can tell
 * which window recieved the input.</p>
 *
 * <h2>Full screen windows</h2>
 *
 * <p> To create a full screen window, you need to specify which monitor the window should use. In most cases, the
 * user's primary monitor is a good choice. For more information about retrieving monitors, see {@link
 * Monitor#getMonitors()}.</p>
 *
 * <pre>
 *     Window window = new Window(640, 480, Monitor.getPrimaryMonitor());
 * </pre>
 *
 * <p> Full screen windows cover the entire display area of a monitor, have no border or decorations. You can also pass
 * a {@link VideoMode} to the constructor along with the monitor to change the resolution of the monitor.</p>
 *
 * <h2>Window hints</h2>
 *
 * <p> There are a number of hints that can be set before the creation of a window and context. Some affect the window
 * itself, others affect the framebuffer or context. These hints are set to their default values each time the library
 * is initialized with {@link GLFW3#init()}, can be set individually with {@link Window#setHint(int, boolean)} or {@link
 * Window#setHint(int, int)}, and reset all at once to their defaults with {@link Window#setDefaultHints()}.</p>
 *
 * <pre>
 *     Window.setHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
 *     Window.setHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
 *     Window.setHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, true);
 *     Window.setHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
 *
 *     Window coreProfileWindow = new Window(640, 480);
 * </pre>
 *
 * <p> Note that hints need to be set before the creation of the window and context you wish to have the specified
 * attributes.</p>
 *
 * <h2>Context object sharing</h2>
 *
 * <p> When creating a window and its OpenGL context, you can specify another window whose context the new one should
 * share its objects (textures, vertex and element buffers, etc.) with.</p>
 *
 * <pre>
 *     Window secondWindow = new Window(640, 480, "Second Window", firstWindow);
 * </pre>
 *
 * <p> Object sharing is implemented by the operating system and graphics driver. On platforms where it is possible to
 * choose which types of objects are shared, GLFW requests that all types are shared. A common exception to this is VAO
 * objects, they have to be recreated for every context.</p>
 *
 * @author Sri Harsha Chilakapati
 * @author lclc98
 * @see Monitor
 * @see VideoMode
 * @see Cursor
 */
public class Window
{
    // A HashMap to store the Window objects based on the handle of the window.
    // This is used to send the Window object to the input events because, say
    // long pointers are ugly in Java.
    private static Map<Long, Window> registeredWindows = new HashMap<>();

    private long handle;

    private Vector2 position;
    private Vector2 size;
    private Vector2 framebufferSize;

    private String  title;
    private Monitor monitor;

    /* Native GLFW Callbacks */
    private GLFWCharCallback            glfwCharCallback;
    private GLFWCharModsCallback        glfwCharModsCallback;
    private GLFWCursorEnterCallback     glfwCursorEnterCallback;
    private GLFWCursorPosCallback       glfwCursorPosCallback;
    private GLFWDropCallback            glfwDropCallback;
    private GLFWFramebufferSizeCallback glfwFramebufferSizeCallback;
    private GLFWKeyCallback             glfwKeyCallback;
    private GLFWMouseButtonCallback     glfwMouseButtonCallback;
    private GLFWScrollCallback          glfwScrollCallback;
    private GLFWWindowCloseCallback     glfwWindowCloseCallback;
    private GLFWWindowFocusCallback     glfwWindowFocusCallback;
    private GLFWWindowIconifyCallback   glfwWindowIconifyCallback;
    private GLFWWindowPosCallback       glfwWindowPosCallback;
    private GLFWWindowRefreshCallback   glfwWindowRefreshCallback;
    private GLFWWindowSizeCallback      glfwWindowSizeCallback;

    /* Overridden custom Callbacks for better interfacing with C++ code */
    private ICharacterCallback       characterCallback;
    private ICharacterModsCallback   characterModsCallback;
    private ICursorEnterCallback     cursorEnterCallback;
    private ICursorPositionCallback  cursorPositionCallback;
    private IDropCallback            dropCallback;
    private IFramebufferSizeCallback framebufferSizeCallback;
    private IKeyCallback             keyCallback;
    private IMouseButtonCallback     mouseButtonCallback;
    private IScrollCallback          scrollCallback;
    private IWindowCloseCallback     windowCloseCallback;
    private IWindowFocusCallback     windowFocusCallback;
    private IWindowIconifyCallback   windowIconifyCallback;
    private IWindowPositionCallback  windowPositionCallback;
    private IWindowRefreshCallback   windowRefreshCallback;
    private IWindowSizeCallback      windowSizeCallback;

    private GLCapabilities windowCapabilities;

    /**
     * Creates a window which is 800 by 600 in dimensions with the title "SilenceEngine Window".
     */
    public Window()
    {
        this(800, 600);
    }

    /**
     * Creates a windowed mode window with a specified width and height, in screen coordinates. The title will be the
     * default title which is "SilenceEngine Window".
     *
     * @param width  The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height The desired height, in screen coordinates, of the window. This must be greater than zero.
     */
    public Window(int width, int height)
    {
        this(width, height, (Monitor) null, null);
    }


    /**
     * Crates a window and its associated OpenGL context, by taking into account the resolution of the screen and the
     * monitor that the window should be made full screen on, and another window if you want to share the context of the
     * other window.
     *
     * @param width   The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height  The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param monitor The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     * @param share   The window whose context to share resources with, or <code>null</code> to not share resources.
     */
    public Window(int width, int height, Monitor monitor, Window share)
    {
        this(width, height, "SilenceEngine Window", monitor, share);
    }

    /**
     * <p> Creates a window and its associated OpenGL context. Most of the options controlling how the window and its
     * context should be created are specified with window hints.</p>
     *
     * <p> Successful creation does not change which context is current. Before you can use the newly created context,
     * you need to make it current.</p>
     *
     * <p> The created window, framebuffer and context may differ from what you requested, as not all parameters and
     * hints are hard constraints. This includes the size of the window, especially for full screen windows. You could
     * query them manually after the window is created.</p>
     *
     * <p> To create a full screen window, you need to specify the monitor the window will cover. If no monitor is
     * specified, windowed mode will be used. Unless you have a way for the user to choose a specific monitor, it is
     * recommended that you pick the primary monitor.</p>
     *
     * <p> For full screen windows, the specified size becomes the resolution of the window's desired video mode. As
     * long as a full screen window has input focus, the supported video mode most closely matching the desired video
     * mode is set for the specified monitor.</p>
     *
     * <p> By default, newly created windows use the placement recommended by the window system. To create the window at
     * a specific position, make it initially invisible using the <code>GLFW_VISIBLE</code> window hint, set its
     * position and then show it.</p>
     *
     * <p> If a full screen window has input focus, the screensaver is prohibited from starting.</p>
     *
     * <p> The swap interval is not set during window creation and the initial value may vary depending on driver
     * settings and defaults.</p>
     *
     * @param width   The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height  The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param title   The desired title string of the Window. This can also contain UTF-8 characters.
     * @param monitor The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     * @param share   The window whose context to share resources with, or <code>null</code> to not share resources.
     */
    public Window(int width, int height, String title, Monitor monitor, Window share)
    {
        this(width, height, title, monitor, (monitor == null ? Monitor.getPrimaryMonitor() : monitor).getVideoMode().getRefreshRate(), share);
    }

    /**
     * <p> Creates a window and its associated OpenGL context. Most of the options controlling how the window and its
     * context should be created are specified with window hints.</p>
     *
     * <p> Successful creation does not change which context is current. Before you can use the newly created context,
     * you need to make it current.</p>
     *
     * <p> The created window, framebuffer and context may differ from what you requested, as not all parameters and
     * hints are hard constraints. This includes the size of the window, especially for full screen windows. You could
     * query them manually after the window is created.</p>
     *
     * <p> To create a full screen window, you need to specify the monitor the window will cover. If no monitor is
     * specified, windowed mode will be used. Unless you have a way for the user to choose a specific monitor, it is
     * recommended that you pick the primary monitor.</p>
     *
     * <p> For full screen windows, the specified size becomes the resolution of the window's desired video mode. As
     * long as a full screen window has input focus, the supported video mode most closely matching the desired video
     * mode is set for the specified monitor.</p>
     *
     * <p> By default, newly created windows use the placement recommended by the window system. To create the window at
     * a specific position, make it initially invisible using the <code>GLFW_VISIBLE</code> window hint, set its
     * position and then show it.</p>
     *
     * <p> If a full screen window has input focus, the screensaver is prohibited from starting.</p>
     *
     * <p> The swap interval is not set during window creation and the initial value may vary depending on driver
     * settings and defaults.</p>
     *
     * @param width       The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height      The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param title       The desired title string of the Window. This can also contain UTF-8 characters.
     * @param monitor     The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     * @param refreshRate The refresh rate to be used for the window. This is ignored for windowed mode windows.
     * @param share       The window whose context to share resources with, or <code>null</code> to not share
     *                    resources.
     */
    public Window(int width, int height, String title, Monitor monitor, int refreshRate, Window share)
    {
        size = new Vector2(width, height);
        framebufferSize = new Vector2();

        position = new Vector2();
        this.title = title;
        this.monitor = monitor;

        setHint(GLFW_REFRESH_RATE, refreshRate);
        handle = glfwCreateWindow(width, height, title, monitor == null ? NULL : monitor.getHandle(), share == null ? NULL : share.getHandle());

        if (handle == NULL)
            throw new RuntimeException("Cannot create window");

        registeredWindows.put(handle, this);

        // Initialize the native callbacks
        initNativeCallbacks();
    }

    /**
     * This constructor creates a fullscreen mode window that fills the monitor. The resolution will be set to the
     * default resolution of the monitor supplied. If the monitor is supplied as <code>null</code>, then a windowed mode
     * window is created instead which is 800 by 600 in size.
     *
     * @param monitor The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     */
    public Window(Monitor monitor)
    {
        this(monitor != null ? monitor.getVideoMode().getWidth() : 800,
                monitor != null ? monitor.getVideoMode().getHeight() : 600, monitor);
    }

    /**
     * Creates a full screen window over a specified monitor. The width and height are the resolution of the monitor.
     *
     * @param width   The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height  The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param monitor The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     */
    public Window(int width, int height, Monitor monitor)
    {
        this(width, height, "SilenceEngine Window", monitor);
    }

    /**
     * Creates a full screen window over a specified monitor with a title. The width and height are the resolution of
     * the monitor. The title is used on the taskbar entry if the user minimised to the desktop.
     *
     * @param width   The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height  The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param title   The desired title string of the Window. This can also contain UTF-8 characters.
     * @param monitor The monitor to use for full screen mode, or <code>null</code> to use windowed mode.
     */
    public Window(int width, int height, String title, Monitor monitor)
    {
        this(width, height, title, monitor, null);
    }

    /**
     * Creates a windowed mode window that is 800 by 600 in size and shares it's context with another window. Everything
     * except the VAOs (Vertex Array Objects) are shared across the contexts.
     *
     * @param share The window who's context should be shared by the new window.
     */
    public Window(Window share)
    {
        this(800, 600, share);
    }

    /**
     * Creates a windowed mode window that has a given dimensions, and shares it's context with another window. The
     * title is the default title, "SilenceEngine window". Everything except the VAOs (Vertex Array Objects) are shared
     * across the context.
     *
     * @param width  The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param share  The window who's context should be shared by the new window.
     */
    public Window(int width, int height, Window share)
    {
        this(width, height, "SilenceEngine window", share);
    }

    /**
     * Creates a windowed mode window that has a given dimensions, a title, and shares it's context with another window.
     * Everything except the VAOs (Vertex Array Objects) are shared across the context.
     *
     * @param width  The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param title  The desired title string of the Window. This can also contain UTF-8 characters.
     * @param share  The window who's context should be shared by the new window.
     */
    public Window(int width, int height, String title, Window share)
    {
        this(width, height, title, null, share);
    }

    /**
     * Creates a windowed mode window that has a specified title. The dimensions are the default dimensions, 800 by
     * 600.
     *
     * @param title The desired title string of the Window. This can also contain UTF-8 characters.
     */
    public Window(String title)
    {
        this(800, 600, title);
    }

    /**
     * Creates a windowed mode window with the given dimensions and the title.
     *
     * @param width  The desired width, in screen coordinates, of the window. This must be greater than zero.
     * @param height The desired height, in screen coordinates, of the window. This must be greater than zero.
     * @param title  The desired title string of the Window. This can also contain UTF-8 characters.
     */
    public Window(int width, int height, String title)
    {
        this(width, height, title, (Monitor) null);
    }

    /**
     * Creates a fullscreen window on a monitor with a specific {@link VideoMode}.
     *
     * @param videoMode The VideoMode to use for the monitor.
     * @param monitor   The monitor that should be used for fullscreen.
     */
    public Window(VideoMode videoMode, Monitor monitor)
    {
        this(videoMode, "SilenceEngine Window", monitor);
    }

    /**
     * Creates a fullscreen window on a monitor with a specific {@link VideoMode} and a title.
     *
     * @param videoMode The VideoMode to use for the monitor.
     * @param title     The desired title string of the Window. This can also contain UTF-8 characters.
     * @param monitor   The monitor that should be used for fullscreen.
     */
    public Window(VideoMode videoMode, String title, Monitor monitor)
    {
        this(videoMode, title, monitor, null);
    }

    /**
     * Creates a fullscreen window on a monitor with a specific {@link VideoMode}, a title, and also shares it's context
     * with an existing window. Everything except VAOs (Vertex Array Objects) are shared across contexts.
     *
     * @param videoMode The VideoMode to use for the monitor.
     * @param title     The desired title string of the Window. This can also contain UTF-8 characters.
     * @param monitor   The monitor that should be used for fullscreen.
     * @param share     The window who's context should be shared with the new window.
     */
    public Window(VideoMode videoMode, String title, Monitor monitor, Window share)
    {
        this(videoMode.getWidth(), videoMode.getHeight(), title, monitor, videoMode.getRefreshRate(), share);
    }

    /**
     * This method sets the window hints that would affect the next newly created window. The hints, once set, retain
     * their values until changed by a call to {@link Window#setHint(int, int)} or {@link Window#setDefaultHints()} or
     * this method. These are also cleared by terminating the GLFW library with {@link GLFW3#terminate()}.
     *
     * @param hint  The window hint to be set. These hints are integer constants provided in the {@link GLFW} class.
     * @param value The new value of the window hint.
     */
    public static void setHint(int hint, boolean value)
    {
        setHint(hint, value ? 1 : 0);
    }

    /**
     * This method sets the window hints that would affect the next newly created window. The hints, once set, retain
     * their values until changed by a call to {@link Window#setHint(int, boolean)} or {@link Window#setDefaultHints()}
     * or this method. These are also cleared by terminating the GLFW library with {@link GLFW3#terminate()}.
     *
     * @param hint  The window hint to be set. These hints are integer constants provided in the {@link GLFW} class.
     * @param value The new value of the window hint.
     */
    public static void setHint(int hint, int value)
    {
        glfwWindowHint(hint, value);
    }

    /**
     * This method resets all the window hints to their default values.
     */
    public static void setDefaultHints()
    {
        glfwDefaultWindowHints();
    }

    /**
     * This method returns the Window whose context is current on the thread that called this method.
     *
     * @return The window whose context is current in the current thread.
     */
    public static Window getCurrentContext()
    {
        return registeredWindows.get(glfwGetCurrentContext());
    }

    /**
     * Returns the native pointer of the window. This value can be casted to a <code>GLFWwindow*</code> in the native
     * code. This is useful if you want to use the native glfw functions from the {@link GLFW} class.
     *
     * @return The native handle that corresponds to this window.
     */
    public long getHandle()
    {
        return handle;
    }

    /**
     * This method initialises the native callbacks, which just translate the events into the custom written callback
     * interfaces. This is done to allow ease of development and more object oriented classes instead of long pointers.
     */
    private void initNativeCallbacks()
    {
        // Initialize overriden callbacks
        initCustomCallbacks();

        // Create the callback functions that re-post the events
        glfwCharCallback = GLFWCharCallback.create((window, codePoint) ->
                characterCallback.invoke(registeredWindows.get(window), codePoint));

        glfwCharModsCallback = GLFWCharModsCallback.create((window, codePoint, mods) ->
        {
            characterModsCallback.invoke(registeredWindows.get(window), codePoint, mods);
        });

        glfwCursorEnterCallback = GLFWCursorEnterCallback.create((window, entered) ->
                cursorEnterCallback.invoke(registeredWindows.get(window), entered));

        glfwCursorPosCallback = GLFWCursorPosCallback.create((win, xPos, yPos) ->
        {
            Window window = registeredWindows.get(win);

            cursorPositionCallback.invoke(window, xPos, yPos);
        });

        glfwDropCallback = GLFWDropCallback.create((window, count, names) ->
        {
            String[] strings = new String[count];

            for (int i = 0; i < count; i++)
                strings[i] = GLFWDropCallback.getName(names, i);

            dropCallback.invoke(registeredWindows.get(window), strings);
        });

        glfwFramebufferSizeCallback = GLFWFramebufferSizeCallback.create((window, width, height) ->
                framebufferSizeCallback.invoke(registeredWindows.get(window), width, height));

        glfwKeyCallback = GLFWKeyCallback.create((win, key, scanCode, action, mods) ->
        {
            Window window = registeredWindows.get(win);
            keyCallback.invoke(window, key, scanCode, action, mods);
        });

        glfwMouseButtonCallback = GLFWMouseButtonCallback.create((win, button, action, mods) ->
        {
            Window window = registeredWindows.get(win);
            mouseButtonCallback.invoke(window, button, action, mods);
        });

        glfwScrollCallback = GLFWScrollCallback.create((win, xOffset, yOffset) ->
        {
            Window window = registeredWindows.get(win);
            scrollCallback.invoke(window, xOffset, yOffset);
        });

        glfwWindowCloseCallback = GLFWWindowCloseCallback.create((window) ->
                windowCloseCallback.invoke(registeredWindows.get(window))
        );

        glfwWindowFocusCallback = GLFWWindowFocusCallback.create((window, focus) ->
                windowFocusCallback.invoke(registeredWindows.get(window), focus)
        );

        glfwWindowIconifyCallback = GLFWWindowIconifyCallback.create((window, iconify) ->
                windowIconifyCallback.invoke(registeredWindows.get(window), iconify)
        );

        glfwWindowPosCallback = GLFWWindowPosCallback.create((window, xPos, yPos) ->
                windowPositionCallback.invoke(registeredWindows.get(window), xPos, yPos)
        );

        glfwWindowRefreshCallback = GLFWWindowRefreshCallback.create((window) ->
                windowRefreshCallback.invoke(registeredWindows.get(window))
        );

        glfwWindowSizeCallback = GLFWWindowSizeCallback.create((window, width, height) ->
                windowSizeCallback.invoke(registeredWindows.get(window), width, height)
        );

        // Register native callbacks
        glfwSetCharCallback(handle, glfwCharCallback);
        glfwSetCharModsCallback(handle, glfwCharModsCallback);
        glfwSetCursorEnterCallback(handle, glfwCursorEnterCallback);
        glfwSetCursorPosCallback(handle, glfwCursorPosCallback);
        glfwSetDropCallback(handle, glfwDropCallback);
        glfwSetFramebufferSizeCallback(handle, glfwFramebufferSizeCallback);
        glfwSetKeyCallback(handle, glfwKeyCallback);
        glfwSetMouseButtonCallback(handle, glfwMouseButtonCallback);
        glfwSetScrollCallback(handle, glfwScrollCallback);
        glfwSetWindowCloseCallback(handle, glfwWindowCloseCallback);
        glfwSetWindowFocusCallback(handle, glfwWindowFocusCallback);
        glfwSetWindowIconifyCallback(handle, glfwWindowIconifyCallback);
        glfwSetWindowPosCallback(handle, glfwWindowPosCallback);
        glfwSetWindowRefreshCallback(handle, glfwWindowRefreshCallback);
        glfwSetWindowSizeCallback(handle, glfwWindowSizeCallback);
    }

    /**
     * This method initialises the custom callbacks, which just sit here and doesn't do anything. This is nothing but
     * simply setting them to null.
     */
    private void initCustomCallbacks()
    {
        // The default callbacks does nothing
        setCharacterCallback(null);
        setCharacterModsCallback(null);
        setCursorEnterCallback(null);
        setCursorPositionCallback(null);
        setDropCallback(null);
        setFramebufferSizeCallback(null);
        setKeyCallback(null);
        setMouseButtonCallback(null);
        setScrollCallback(null);
        setCloseCallback(null);
        setFocusCallback(null);
        setIconifyCallback(null);
        setPositionCallback(null);
        setRefreshCallback(null);
        setSizeCallback(null);
    }

    /**
     * <p> This method returns the last state reported for the specified key to the specified window. The returned state
     * is one of <code>GLFW_PRESS</code> or <code>GLFW_RELEASE</code>. The higher-level action <code>GLFW_REPEAT</code>
     * is only reported to the key callback.</p>
     *
     * <p> If the <code>GLFW_STICKY_KEYS</code> input mode is enabled, this function returns <code>GLFW_PRESS</code> the
     * first time you call it for a key that was pressed, even if that key has already been released.</p>
     *
     * <p> The key methods deal with physical keys, with key tokens named after their use on the standard US keyboard
     * layout. If you want to input text, use the Unicode character callback instead.</p>
     *
     * <p> The modifier key bit masks are not key tokens and cannot be used with this method.</p>
     *
     * @param key The desired keyboard key. <code>GLFW_KEY_UNKNOWN</code> is not a valid key for this method.
     *
     * @return One of <code>GLFW_PRESS</code> or <code>GLFW_RELEASE</code>.
     */
    public int getKey(int key)
    {
        return glfwGetKey(handle, key);
    }

    /**
     * <p> This method returns the last state reported for the specified mouse button to the specified window. The
     * returned state is one of <code>GLFW_PRESS</code> or <code>GLFW_RELEASE</code>.</p>
     *
     * <p> If the <code>GLFW_STICKY_MOUSE_BUTTONS</code> input mode is enabled, this method <code>GLFW_PRESS</code> the
     * first time you call it for a mouse button that was pressed, even if that mouse button has already been released.
     * </p>
     *
     * @param button The desired mouse button.
     *
     * @return One of <code>GLFW_PRESS</code> or <code>GLFW_RELEASE</code>.
     *
     * @see Window#setInputMode(int, int)
     */
    public int getMouseButton(int button)
    {
        return glfwGetMouseButton(handle, button);
    }

    /**
     * <p> This method returns the position of the cursor, in screen coordinates, relative to the upper-left corner of
     * the client area of the specified window.</p>
     *
     * <p> If the cursor is disabled (with <code>GLFW_CURSOR_DISABLED</code>) then the cursor position is unbounded and
     * limited only by the minimum and maximum values of a double.</p>
     *
     * @return The current position of the mouse cursor in screen coordinates, as a {@link Vector2} object.
     */
    public Vector2 getCursorPos()
    {
        DoubleBuffer pos = BufferUtils.createDoubleBuffer(2);
        nglfwGetCursorPos(handle, memAddress(pos), memAddress(pos) + Double.BYTES);

        return new Vector2((float) pos.get(0), (float) pos.get(1));
    }

    /**
     * <p> This method sets the position, in screen coordinates, of the cursor relative to the upper-left corner of the
     * client area of the window. The window must have input focus. If the window does not have input focus when this
     * method is called, it fails silently.</p>
     *
     * <p> <b>Do not use this method</b> to implement things like camera controls. GLFW already provides the
     * <code>GLFW_CURSOR_DISABLED</code> cursor mode that hides the cursor, transparently re-centers it and provides
     * unconstrained cursor motion. See {@link Window#setInputMode(int, int)} for more information.</p>
     *
     * <p> If the cursor mode is <code>GLFW_CURSOR_DISABLED</code> then the cursor position is unconstrained and limited
     * only by the minimum and maximum values of a double.</p>
     *
     * @param pos The desired position of the cursor as a Vector2 object.
     */
    public void setCursorPos(Vector2 pos)
    {
        setCursorPos(pos.x, pos.y);
    }

    /**
     * <p> This method sets the position, in screen coordinates, of the cursor relative to the upper-left corner of the
     * client area of the window. The window must have input focus. If the window does not have input focus when this
     * method is called, it fails silently.</p>
     *
     * <p> <b>Do not use this method</b> to implement things like camera controls. GLFW already provides the
     * <code>GLFW_CURSOR_DISABLED</code> cursor mode that hides the cursor, transparently re-centers it and provides
     * unconstrained cursor motion. See {@link Window#setInputMode(int, int)} for more information.</p>
     *
     * <p> If the cursor mode is <code>GLFW_CURSOR_DISABLED</code> then the cursor position is unconstrained and limited
     * only by the minimum and maximum values of a double.</p>
     *
     * @param xPos The desired x-coordinate, relative to the left edge of the client area.
     * @param yPos The desired y-coordinate, relative to the top edge of the client area.
     */
    public void setCursorPos(double xPos, double yPos)
    {
        glfwSetCursorPos(handle, xPos, yPos);
    }

    /**
     * <p> This method sets an input mode option for this window. The mode must be one of <code>GLFW_CURSOR</code>,
     * <code>GLFW_STICKY_KEYS</code> or <code>GLFW_STICKY_MOUSE_BUTTONS</code>.</p>
     *
     * <p> If the mode is GLFW_CURSOR, the value must be one of the following cursor modes:</p>
     *
     * <ul> <li><code>GLFW_CURSOR_NORMAL</code> makes the cursor visible and behaving normally.</li>
     * <li><code>GLFW_CURSOR_HIDDEN</code> makes the cursor invisible when it is over the client area of the window but
     * does not restrict the cursor from leaving.</li> <li><code>GLFW_CURSOR_DISABLED</code> hides and grabs the cursor,
     * providing virtual and unlimited cursor movement. This is useful for implementing for example 3D camera
     * controls.</li> </ul>
     *
     * <p> If the mode is <code>GLFW_STICKY_KEYS</code>, the value must be either <code>GL_TRUE</code> to enable sticky
     * keys, or <code>GL_FALSE</code> to disable it. If sticky keys are enabled, a key press will ensure that {@link
     * Window#getKey(int)} returns <code>GLFW_PRESS</code> the next time it is called even if the key had been released
     * before the call. This is useful when you are only interested in whether keys have been pressed but not when or in
     * which order.</p>
     *
     * <p> If the mode is <code>GLFW_STICKY_MOUSE_BUTTONS</code>, the value must be either <code>GL_TRUE</code> to
     * enable sticky mouse buttons, or <code>GL_FALSE</code> to disable it. If sticky mouse buttons are enabled, a mouse
     * button press will ensure that {@link Window#getMouseButton(int)} returns <code>GLFW_PRESS</code> the next time it
     * is called even if the mouse button had been released before the call. This is useful when you are only interested
     * in whether mouse buttons have been pressed but not when or in which order.</p>
     *
     * @param mode  One of <code>GLFW_CURSOR</code>, <code>GLFW_STICKY_KEYS</code> or <code>GLFW_STICKY_MOUSE_BUTTONS</code>.
     * @param value The new value of the specified input mode.
     */
    public void setInputMode(int mode, int value)
    {
        glfwSetInputMode(handle, mode, value);
    }

    /**
     * <p> This method sets the cursor image to be used when the cursor is over the client area of the specified window.
     * The set cursor will only be visible when the cursor mode of the window is <code>GLFW_CURSOR_NORMAL</code>.</p>
     *
     * <p> On some platforms, the set cursor may not be visible unless the window also has input focus.</p>
     *
     * @param cursor The cursor to set, or <code>null</code> to switch back to the default arrow cursor.
     */
    public void setCursor(Cursor cursor)
    {
        glfwSetCursor(handle, cursor == null ? NULL : cursor.getHandle());
    }

    /**
     * <p> This method makes the OpenGL context of this window current on the calling thread. A context can only be made
     * current on a single thread at a time and each thread can have only a single current context at a time.</p>
     *
     * <p> By default, making a context non-current implicitly forces a pipeline flush. On machines that support
     * <code>GL_KHR_context_flush_control</code>, you can control whether a context performs this flush by setting the
     * <code>GLFW_CONTEXT_RELEASE_BEHAVIOR</code> window hint.</p>
     */
    public void makeCurrent()
    {
        glfwMakeContextCurrent(handle);

        if (windowCapabilities == null)
            windowCapabilities = GL.createCapabilities();
        else
            GL.setCapabilities(windowCapabilities);
    }

    public void setMonitor(Monitor monitor, VideoMode videoMode)
    {
        this.monitor = monitor;

        if (videoMode == null)
        {
            Vector2 size = getSize();
            videoMode = new VideoMode((int) size.x, (int) size.y, 32, 32, 32, 32);
        }

        glfwSetWindowMonitor(handle, monitor == null ? NULL : monitor.getHandle(), 0, 0, videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate());
    }

    /**
     * This method swaps the front and back buffers of the specified window. If the swap interval is greater than zero,
     * the GPU driver waits the specified number of screen updates before swapping the buffers.
     */
    public void swapBuffers()
    {
        glfwSwapBuffers(handle);
    }

    /**
     * This method returns the value of the close flag of this window.
     *
     * @return The close flag of this window.
     */
    public boolean shouldClose()
    {
        return glfwWindowShouldClose(handle);
    }

    public void maximize()
    {
        glfwMaximizeWindow(handle);
    }

    /**
     * Sets the icon for the window.
     *
     * @param image The image to be used as an icon.
     */
    public void setIcon(Image image)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        GLFWImage.Buffer glfwImages = GLFWImage.calloc(1);
        GLFWImage glfwImage = glfwImages.get(0);
        glfwImage.width(width);
        glfwImage.height(height);

        ByteBuffer data = BufferUtils.createByteBuffer(width * height * 4);

        Color color = Color.REUSABLE_STACK.pop();

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                image.getPixel(x, y, color);

                float r = (color.r * 255f);
                float g = (color.g * 255f);
                float b = (color.b * 255f);
                float a = (color.a * 255f);

                data.put((byte) r)
                        .put((byte) g)
                        .put((byte) b)
                        .put((byte) a);
            }
        }

        Color.REUSABLE_STACK.push(color);

        data.flip();
        glfwImage.pixels(data);

        glfwSetWindowIcon(handle, glfwImages);

        glfwImages.free();
    }

    /**
     * Sets the icon for the window.
     *
     * @param imagePath The image to be used as an icon.
     * @param success   The callback to be called upon successful icon set.
     */
    public void setIcon(FilePath imagePath, SimpleCallback success)
    {
        SilenceEngine.io.getImageReader().readImage(imagePath, image ->
        {
            setIcon(image);
            success.invoke();
        });
    }

    /**
     * This method sets the value of the close flag of this window. This can be used to override the user's attempt to
     * close the window, or to signal that it should be closed.
     *
     * @param value The new value.
     */
    public void setShouldClose(boolean value)
    {
        glfwSetWindowShouldClose(handle, value);
    }

    /**
     * <p> This method iconifies (minimizes) this window if it was previously restored. If the window is already
     * iconified, this method does nothing.</p>
     *
     * <p> If this window is a full screen window, the original monitor resolution is restored until the window is
     * restored again.</p>
     */
    public void iconify()
    {
        glfwIconifyWindow(handle);
    }

    /**
     * <p> This method restores this window if it was previously iconified (minimized). If the window is already
     * restored, this method does nothing.</p>
     *
     * <p> If this window is a full screen window, the resolution chosen for the window is restored on the selected
     * monitor.</p>
     */
    public void restore()
    {
        glfwRestoreWindow(handle);
    }

    /**
     * This method hides this window if it was previously visible. If the window is already hidden or is in full screen
     * mode, this method does nothing.
     */
    public void hide()
    {
        glfwHideWindow(handle);
    }

    /**
     * This method makes this window visible if it was previously hidden. If the window is already visible or is in full
     * screen mode, this method does nothing.
     */
    public void show()
    {
        glfwShowWindow(handle);
    }

    /**
     * This method destroys this window and its context. On calling this method, no further callbacks will be called for
     * this window. If the context of this window is current on the main thread, it is detached before being destroyed.
     * The context of this window must not be current on any other thread when this function is called.
     */
    public void destroy()
    {
        releaseNativeCallbacks();
        glfwDestroyWindow(handle);
    }

    /**
     * This method releases the native callbacks, preventing the occurrence of segmentation fault errors from the JNI
     * code, which usually occurs due to the function address not being cleared from the native code. Releasing these
     * callbacks erases the function address, so it can't be called again.
     */
    private void releaseNativeCallbacks()
    {
        glfwCharCallback.free();
        glfwCharModsCallback.free();
        glfwCursorEnterCallback.free();
        glfwCursorPosCallback.free();
        glfwDropCallback.free();
        glfwFramebufferSizeCallback.free();
        glfwKeyCallback.free();
        glfwMouseButtonCallback.free();
        glfwScrollCallback.free();
        glfwWindowCloseCallback.free();
        glfwWindowFocusCallback.free();
        glfwWindowIconifyCallback.free();
        glfwWindowPosCallback.free();
        glfwWindowRefreshCallback.free();
        glfwWindowSizeCallback.free();
    }

    /**
     * This method retrieves the position, in screen coordinates, of the upper-left corner of the client area of this
     * window. If any error occurs, the position is set to zero.
     *
     * @return The position of the upper-left corner of the client area of this window, as a Vector2.
     */
    public Vector2 getPosition()
    {
        IntBuffer pos = BufferUtils.createIntBuffer(2);
        nglfwGetWindowPos(handle, memAddress(pos), memAddress(pos) + Integer.BYTES);

        return position.set(pos.get(0), pos.get(1));
    }

    /**
     * <p> This method sets the position, in screen coordinates, of the upper-left corner of the client area of this
     * windowed mode window. If the window is a full screen window, this function does nothing.</p>
     *
     * <p> <b>Do not use this method</b> to move an already visible window unless you have very good reasons for doing
     * so, as it will confuse and annoy the user.</p>
     *
     * <p> The window manager may put limits on what positions are allowed. GLFW cannot and should not override these
     * limits.</p>
     *
     * @param position The new position of this window.
     */
    public void setPosition(Vector2 position)
    {
        this.position.set(position);
        glfwSetWindowPos(handle, (int) position.x, (int) position.y);
    }

    /**
     * <p> This method sets the position, in screen coordinates, of the upper-left corner of the client area of this
     * windowed mode window. If the window is a full screen window, this function does nothing.</p>
     *
     * <p> <b>Do not use this method</b> to move an already visible window unless you have very good reasons for doing
     * so, as it will confuse and annoy the user.</p>
     *
     * <p> The window manager may put limits on what positions are allowed. GLFW cannot and should not override these
     * limits.</p>
     *
     * @param x The x-coordinate of the upper-left corner of the client area.
     * @param y The y-coordinate of the upper-left corner of the client area.
     */
    public void setPosition(float x, float y)
    {
        setPosition(position.set(x, y));
    }

    /**
     * This method retrieves the size, in screen coordinates, of the client area of this window. If you wish to retrieve
     * the size of the framebuffer of the window in pixels, see {@link Window#getFramebufferSize()}.
     *
     * @return The size of the client area of this window, in screen coordinates, as a Vector2.
     */
    public Vector2 getSize()
    {
        IntBuffer size = BufferUtils.createIntBuffer(2);
        nglfwGetWindowSize(handle, memAddress(size), memAddress(size) + Integer.BYTES);

        return this.size.set(size.get(0), size.get(1));
    }

    /**
     * <p> This method sets the size, in screen coordinates, of the client area of this window.</p>
     *
     * <p> For full screen windows, this method selects and switches to the resolution closest to the specified size,
     * without affecting the window's context. As the context is unaffected, the bit depths of the framebuffer remain
     * unchanged.</p>
     *
     * <p> The window manager may put limits on what sizes are allowed. GLFW cannot and should not override these
     * limits.</p>
     *
     * @param size The new size of the client area of this window, in screen coordinates, as a Vector2.
     */
    public void setSize(Vector2 size)
    {
        this.size.set(size);
        glfwSetWindowSize(handle, (int) size.x, (int) size.y);
    }

    /**
     * <p> This method sets the size, in screen coordinates, of the client area of this window.</p>
     *
     * <p> For full screen windows, this method selects and switches to the resolution closest to the specified size,
     * without affecting the window's context. As the context is unaffected, the bit depths of the framebuffer remain
     * unchanged.</p>
     *
     * <p> The window manager may put limits on what sizes are allowed. GLFW cannot and should not override these
     * limits.</p>
     *
     * @param width  The desired width of the specified window.
     * @param height The desired height of the specified window.
     */
    public void setSize(float width, float height)
    {
        setSize(size.set(width, height));
    }

    /**
     * This method retrieves the size, in pixels, of the framebuffer of this window. If you wish to retrieve the size of
     * the window in screen coordinates, see {@link Window#getSize()}.
     *
     * @return The size of the window framebuffer, in pixels, as a Vector2.
     */
    public Vector2 getFramebufferSize()
    {
        IntBuffer size = BufferUtils.createIntBuffer(2);
        nglfwGetFramebufferSize(handle, memAddress(size), memAddress(size) + Integer.BYTES);

        return framebufferSize.set(size.get(0), size.get(1));
    }

    /**
     * This method resizes the window such that the framebuffer is fixed to the size specified. The difference to the
     * <code>setSize()</code> methods is that this method accepts the input in pixels, while those methods accept the
     * input in screen coordinates.
     *
     * @param fbSize The size of the framebuffer in pixels.
     */
    public void setFramebufferSize(Vector2 fbSize)
    {
        Vector2 temp = Vector2.REUSABLE_STACK.pop();
        Vector2 framebufferSize = getFramebufferSize();
        Vector2 size = getSize();

        temp.set(size).scale(1 / framebufferSize.x, 1 / framebufferSize.y);

        this.framebufferSize.set(fbSize);
        size.set(fbSize).scale(temp.x, temp.y);

        setSize(size);

        Vector2.REUSABLE_STACK.push(temp);
    }

    /**
     * This method resizes the window such that the framebuffer is fixed to the size specified. The difference to the
     * <code>setSize()</code> methods is that this method accepts the input in pixels, while those methods accept the
     * input in screen coordinates.
     *
     * @param width  The width of the window framebuffer, in pixels.
     * @param height The height of the window framebuffer, in pixels.
     */
    public void setFramebufferSize(float width, float height)
    {
        Vector2 temp = Vector2.REUSABLE_STACK.pop();
        setFramebufferSize(temp.set(width, height));
        Vector2.REUSABLE_STACK.push(temp);
    }

    public String getClipboardString()
    {
        return glfwGetClipboardString(handle);
    }

    public void setClipboardString(String string)
    {
        glfwSetClipboardString(handle, string);
    }

    /**
     * <p> This method retrieves the size, in screen coordinates, of each edge of the frame of this window. This size
     * includes the title bar, if the window has one. The size of the frame may vary depending on the window-related
     * hints used to create it.</p>
     *
     * <p> Because this method retrieves the size of each window frame edge and not the offset along a particular
     * coordinate axis, the retrieved values will always be zero or positive.</p>
     *
     * @return The size of the frame as a Vector4. The x-component is the size of the left edge, y-component is the top
     * edge, z-component is the right edge and the last w-component is the size of the bottom edge.
     */
    public Vector4 getFrameSize()
    {
        IntBuffer size = BufferUtils.createIntBuffer(4);

        long left = memAddress(size);
        long top = left + Integer.BYTES;
        long right = top + Integer.BYTES;
        long bottom = right + Integer.BYTES;

        nglfwGetWindowFrameSize(handle, left, top, right, bottom);

        return new Vector4(size.get(0), size.get(1), size.get(2), size.get(3));
    }

    /**
     * This methods returns the monitor that this window is fullscreen upon. If this window is in windowed mode, then
     * the monitor will be <code>null</code>.
     *
     * @return The monitor used by this window.
     */
    public Monitor getMonitor()
    {
        return monitor;
    }

    public void setMonitor(Monitor monitor)
    {
        setMonitor(monitor, monitor == null ? null : monitor.getVideoMode());
    }

    /**
     * This method returns the title of this window.
     *
     * @return The title of this window, encoded as UTF-8 string.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * This method sets the title of the window, which is a string encoding in UTF-8.
     *
     * @param title The new title of this window.
     */
    public void setTitle(String title)
    {
        this.title = title;

        glfwSetWindowTitle(handle, title);
    }

    /**
     * This method returns the value of an attribute of the specified window or its OpenGL context.
     *
     * @param attribute The window attribute whose value to return.
     *
     * @return The value of the attribute, or zero if an error occurred.
     */
    public int getAttribute(int attribute)
    {
        return glfwGetWindowAttrib(handle, attribute);
    }

    /**
     * Returns the currently set {@link ICharacterCallback} of this window object.
     *
     * @return The currently set character callback, or <code>null</code> if there is none.
     */
    public ICharacterCallback getCharacterCallback()
    {
        return characterCallback;
    }

    /**
     * This method sets a {@link ICharacterCallback} with this window, allowing you to listen to character input events
     * that happened on this window.
     *
     * @param callback The callback instance which will be invoked when a character input event occurs.
     *
     * @return The previously set callback, or <code>null</code> if there is no previously set callback.
     */
    public ICharacterCallback setCharacterCallback(ICharacterCallback callback)
    {
        ICharacterCallback previousCallback = characterCallback;

        if (callback == null)
            callback = (window, codePoint) ->
            {
            };

        this.characterCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link ICharacterModsCallback} for this window. If there is no callback
     * registered, this method returns <code>null</code>.
     *
     * @return The currently set character mods callback on this window.
     */
    public ICharacterModsCallback getCharacterModsCallback()
    {
        return characterModsCallback;
    }

    /**
     * This method sets the {@link ICharacterModsCallback} used by this window to notify input events. This method also
     * returns the previously set callback for you.
     *
     * @param callback The new {@link ICharacterModsCallback} to be used.
     *
     * @return The previously used callback, or <code>null</code> if there is none.
     */
    public ICharacterModsCallback setCharacterModsCallback(ICharacterModsCallback callback)
    {
        ICharacterModsCallback previousCallback = characterModsCallback;

        if (callback == null)
            callback = (window, cp, mods) ->
            {
            };

        this.characterModsCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link ICursorEnterCallback} on this window. If there is no callback, then
     * this method returns <code>null</code>.
     *
     * @return The currently set {@link ICursorEnterCallback}, or <code>null</code> if there is none.
     */
    public ICursorEnterCallback getCursorEnterCallback()
    {
        return cursorEnterCallback;
    }

    /**
     * This method sets the {@link ICursorEnterCallback} on this window. Also, it returns the previously set callback.
     *
     * @param callback The new {@link ICursorEnterCallback} to be used, or <code>null</code> to clear it.
     *
     * @return The previously used callback, or <code>null</code> if there is none.
     */
    public ICursorEnterCallback setCursorEnterCallback(ICursorEnterCallback callback)
    {
        ICursorEnterCallback previousCallback = cursorEnterCallback;

        if (callback == null)
            callback = (window, entered) ->
            {
            };

        this.cursorEnterCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link ICursorPositionCallback} on this window.
     *
     * @return The currently set callback, or <code>null</code> if there is none set.
     */
    public ICursorPositionCallback getCursorPositionCallback()
    {
        return cursorPositionCallback;
    }

    /**
     * This method sets the {@link ICursorPositionCallback} to be used by this window. If you want to clear the existing
     * callback, pass <code>null</code> as the argument.
     *
     * @param callback The new {@link ICursorPositionCallback} to be used.
     *
     * @return The previously set callback, or <code>null</code> if there is none set.
     */
    public ICursorPositionCallback setCursorPositionCallback(ICursorPositionCallback callback)
    {
        ICursorPositionCallback previousCallback = cursorPositionCallback;

        if (callback == null)
            callback = (window, x, y) ->
            {
            };

        this.cursorPositionCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link IDropCallback} on this window. If there is no callback set, then
     * <code>null</code> is returned to you.
     *
     * @return The currently set callback, or <code>null</code> if there is none set.
     */
    public IDropCallback getDropCallback()
    {
        return dropCallback;
    }

    /**
     * This method sets a {@link IDropCallback} to be used by this Window. To clear the current callback, just pass
     * <code>null</code> to the argument.
     *
     * @param callback The new callback to be used by this window, or <code>null</code> to clear the existing one.
     *
     * @return The previously used callback, or <code>null</code> if there is none set.
     */
    public IDropCallback setDropCallback(IDropCallback callback)
    {
        IDropCallback previousCallback = dropCallback;

        if (callback == null)
            callback = (window, paths) ->
            {
            };

        this.dropCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently used {@link IFramebufferSizeCallback} by this window. If there is no existing
     * callback, then this method returns <code>null</code> to you.
     *
     * @return The current callback, or <code>null</code> if none is set previously.
     */
    public IFramebufferSizeCallback getFramebufferSizeCallback()
    {
        return framebufferSizeCallback;
    }

    /**
     * This method sets the {@link IFramebufferSizeCallback} used by this window. If you want to clear the existing
     * callback, simply pass <code>null</code> as the value for the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing one.
     *
     * @return The previously used callback, or <code>null</code> if none exists before.
     */
    public IFramebufferSizeCallback setFramebufferSizeCallback(IFramebufferSizeCallback callback)
    {
        IFramebufferSizeCallback previousCallback = framebufferSizeCallback;

        if (callback == null)
            callback = (window, w, h) ->
            {
            };

        this.framebufferSizeCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently used {@link IKeyCallback} of this window. If there is no callback set before,
     * this method returns <code>null</code> to you.
     *
     * @return The currently used key callback of this window.
     */
    public IKeyCallback getKeyCallback()
    {
        return keyCallback;
    }

    /**
     * This method sets the {@link IKeyCallback} used by this window object. If you want to clear the existing one, just
     * pass <code>null</code> as the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing one.
     *
     * @return The previously used key callback, or <code>null</code> if there is nothing previously set.
     */
    public IKeyCallback setKeyCallback(IKeyCallback callback)
    {
        IKeyCallback previousCallback = keyCallback;

        if (callback == null)
            callback = (win, key, sc, act, mods) ->
            {
            };

        this.keyCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link IMouseButtonCallback} of this Window object. If there is nothing set
     * before, then this method returns <code>null</code> to you.
     *
     * @return The currently set mouse button callback, or <code>null</code> if there is nothing set previously.
     */
    public IMouseButtonCallback getMouseButtonCallback()
    {
        return mouseButtonCallback;
    }

    /**
     * This method sets the {@link IMouseButtonCallback} used by this window object. If you want to clear the existing
     * callback, simply pass <code>null</code> as the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> if you want to clear it.
     *
     * @return The previously set mouse button callback, or <code>null</code> if there is nothing set previously.
     */
    public IMouseButtonCallback setMouseButtonCallback(IMouseButtonCallback callback)
    {
        IMouseButtonCallback previousCallback = mouseButtonCallback;

        if (callback == null)
            callback = (win, b, act, mods) ->
            {
            };

        this.mouseButtonCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the {@link IScrollCallback} used by this window object. If there is no callback registered,
     * then this method returns <code>null</code>.
     *
     * @return The current scroll callback, or <code>null</code> if there is nothing set previously.
     */
    public IScrollCallback getScrollCallback()
    {
        return scrollCallback;
    }

    /**
     * This method sets the {@link IScrollCallback} used by this window object. If you want to clear the existing
     * callback, simply pass <code>null</code> to the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> if you want to clear existing callback.
     *
     * @return The previously used scroll callback, or <code>null</code> if there isn't any.
     */
    public IScrollCallback setScrollCallback(IScrollCallback callback)
    {
        IScrollCallback previousCallback = scrollCallback;

        if (callback == null)
            callback = (win, x, y) ->
            {
            };

        this.scrollCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the current {@link IWindowCloseCallback} of this window object. If there is no existing
     * callback, this method returns <code>null</code> as the return value.
     *
     * @return The current window close callback, or <code>null</code> if there is nothing previously.
     */
    public IWindowCloseCallback getCloseCallback()
    {
        return windowCloseCallback;
    }

    /**
     * This method sets the {@link IWindowCloseCallback} used by this window object. If you want to clear the existing
     * callback, then simply pass <code>null</code> to the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> if you want to clear the existing one.
     *
     * @return The previously set close callback, or <code>null</code> if nothing is set previously.
     */
    public IWindowCloseCallback setCloseCallback(IWindowCloseCallback callback)
    {
        IWindowCloseCallback previousCallback = windowCloseCallback;

        if (callback == null)
            callback = (win) ->
            {
            };

        this.windowCloseCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link IWindowFocusCallback} of this window object. If there is no existing
     * callback, then <code>null</code> is returned to you.
     *
     * @return The currently set focus callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowFocusCallback getFocusCallback()
    {
        return windowFocusCallback;
    }

    /**
     * This method sets the {@link IWindowFocusCallback} used by this window object. If you want to clear out the
     * existing callback, then simply pass <code>null</code> as the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing one.
     *
     * @return The previously used focus callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowFocusCallback setFocusCallback(IWindowFocusCallback callback)
    {
        IWindowFocusCallback previousCallback = windowFocusCallback;

        if (callback == null)
            callback = (win, focused) ->
            {
            };

        this.windowFocusCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the {@link IWindowIconifyCallback} used by this window object. If there is no existing
     * callback, then <code>null</code> is returned.
     *
     * @return The currently used iconify callback, or <code>null</code> if there is none set previously.
     */
    public IWindowIconifyCallback getIconifyCallback()
    {
        return windowIconifyCallback;
    }

    /**
     * This method sets the {@link IWindowIconifyCallback} used by this window object. If you want to clear the existing
     * callback, simply pass <code>null</code> as the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing callback.
     *
     * @return The previously used iconify callback, or <code>null</code> if there is none set previously.
     */
    public IWindowIconifyCallback setIconifyCallback(IWindowIconifyCallback callback)
    {
        IWindowIconifyCallback previousCallback = windowIconifyCallback;

        if (callback == null)
            callback = (win, ic) ->
            {
            };

        this.windowIconifyCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link IWindowPositionCallback} of this window object. If there is nothing
     * set previously, then this method returns <code>null</code>.
     *
     * @return The current window position callback, or <code>null</code> if there is nothing previously set.
     */
    public IWindowPositionCallback getPositionCallback()
    {
        return windowPositionCallback;
    }

    /**
     * This method sets the {@link IWindowPositionCallback} of this window object. If you want to clear the existing
     * callback, then simply pass <code>null</code> to the value of the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing one.
     *
     * @return The previously used callback, or <code>null</code> if there is nothing previously set.
     */
    public IWindowPositionCallback setPositionCallback(IWindowPositionCallback callback)
    {
        IWindowPositionCallback previousCallback = windowPositionCallback;

        if (callback == null)
            callback = (win, x, y) ->
            {
            };

        this.windowPositionCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently set {@link IWindowRefreshCallback} on this window object. If there is no
     * callback previously set, this method will return <code>null</code> to you.
     *
     * @return The currently set window refresh callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowRefreshCallback getRefreshCallback()
    {
        return windowRefreshCallback;
    }

    /**
     * This method sets the {@link IWindowRefreshCallback} on this window object. If you want to clear out the existing
     * callback, then simply pass <code>null</code> as the value to the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing callback.
     *
     * @return The previously used callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowRefreshCallback setRefreshCallback(IWindowRefreshCallback callback)
    {
        IWindowRefreshCallback previousCallback = windowRefreshCallback;

        if (callback == null)
            callback = (win) ->
            {
            };

        this.windowRefreshCallback = callback;

        return previousCallback;
    }

    /**
     * This method returns the currently used {@link IWindowSizeCallback} of this window object. If there is no existing
     * callback, this method returns <code>null</code>.
     *
     * @return The currently set window size callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowSizeCallback getSizeCallback()
    {
        return windowSizeCallback;
    }

    /**
     * This method sets the {@link IWindowSizeCallback} used by this window object. If you want to clear the existing
     * callback, simply pass <code>null</code> as the argument.
     *
     * @param callback The new callback to be used, or <code>null</code> to clear the existing one.
     *
     * @return The previously used callback, or <code>null</code> if there is nothing set previously.
     */
    public IWindowSizeCallback setSizeCallback(IWindowSizeCallback callback)
    {
        IWindowSizeCallback previousCallback = windowSizeCallback;

        if (callback == null)
            callback = (win, w, h) ->
            {
            };

        this.windowSizeCallback = callback;

        return previousCallback;
    }

    /**
     * This methods returns the current monitor that this window is on. If the window is over multiple screen it will
     * pick the one that has more of the window on the monitor. In the case of being equal it will pick the monitor that
     * is called first.
     *
     * @return The monitor that the window is on.
     */
    public Monitor getCurrentMonitor()
    {
        int bestoverlap = 0;
        Monitor currentMonitor = null;

        int wx = (int) getPosition().x;
        int wy = (int) getPosition().y;

        int ww = (int) getSize().x;
        int wh = (int) getSize().y;

        for (Monitor monitor : Monitor.getMonitors())
        {
            Vector2 vPos = monitor.getVirtualPosition();
            VideoMode vMode = monitor.getVideoMode();
            int mx = (int) vPos.x;
            int my = (int) vPos.y;
            int mw = vMode.getWidth();
            int mh = vMode.getHeight();

            int overlap = Math.max(0, Math.min(wx + ww, mx + mw) - Math.max(wx, mx)) * Math.max(0, Math.min(wy + wh, my + mh) - Math.max(wy, my));

            if (bestoverlap < overlap)
            {
                bestoverlap = overlap;
                currentMonitor = monitor;
            }
        }

        return currentMonitor;
    }

    /**
     * Returns the GL capabilities instance of this window. If the window haven't been made current even once, then this
     * method returns {@code null} to the caller.
     *
     * @return The GLCapabilities instance.
     */
    public GLCapabilities getCapabilities()
    {
        return windowCapabilities;
    }
}
