/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Sri Harsha Chilakapati
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

package com.shc.silenceengine.core.glfw;

import com.shc.silenceengine.core.glfw.callbacks.IMonitorCallback;
import com.shc.silenceengine.math.Vector2;
import com.shc.silenceengine.utils.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWgammaramp;
import org.lwjgl.glfw.GLFWvidmode;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * <p> A monitor object represents a currently connected monitor and is represented as instance of the {@link Monitor}
 * class. Monitor objects cannot be created or destroyed by the application and retain their addresses until the
 * monitors they represent are disconnected or until the library is terminated.</p>
 *
 * <p> Each monitor has a current video mode, a list of supported video modes, a virtual position, a human-readable
 * name, an estimated physical size and a gamma ramp. One of the monitors is the primary monitor. The virtual position
 * of a monitor is in screen coordinates and, together with the current video mode, describes the viewports that the
 * connected monitors provide into the virtual desktop that spans them.</p>
 *
 * @author Sri Harsha Chilakapati
 * @see VideoMode
 * @see GammaRamp
 */
public class Monitor
{
    private static Map<Long, Monitor> registeredMonitors = new HashMap<>();

    private static Monitor       primary;
    private static List<Monitor> monitors;

    private static GLFWMonitorCallback glfwMonitorCallback;
    private static IMonitorCallback    monitorCallback;

    private long handle;

    private List<VideoMode> videoModes;

    /**
     * Constructs a wrapper Monitor object around the native pointer given by the {@link GLFW#glfwGetMonitors()} and
     * {@link GLFW#glfwGetPrimaryMonitor()} functions.
     *
     * @param handle The handle that points to the GLFWMonitor* of the native monitor type.
     */
    private Monitor(long handle)
    {
        this.handle = handle;
        registeredMonitors.put(handle, this);
    }

    /**
     * This function returns the primary monitor. This is usually the monitor where elements like the Windows task bar
     * or the OS X menu bar is located.
     *
     * @return The Monitor object which represents the primary monitor of the OS.
     */
    public static Monitor getPrimaryMonitor()
    {
        if (primary == null)
            primary = new Monitor(glfwGetPrimaryMonitor());

        return primary;
    }

    /**
     * This function sets the monitor configuration callback, or removes the currently set callback. This is called when
     * a monitor is connected to or disconnected from the system.
     *
     * @param callback The new callback, or <code>null</code> to remove the currently set callback.
     *
     * @return The previously set callback, or <code>null</code> if no callback was set or the library had not been
     * initialized.
     */
    public static IMonitorCallback setCallback(IMonitorCallback callback)
    {
        IMonitorCallback previousCallback = monitorCallback;

        if (callback == null)
            callback = (monitor, event) -> {
            };

        monitorCallback = callback;

        if (glfwMonitorCallback != null)
            glfwMonitorCallback.release();

        glfwMonitorCallback = GLFWMonitorCallback.create((monitor, event) ->
                        monitorCallback.invoke(registeredMonitors.get(monitor), event)
        );

        glfwSetMonitorCallback(glfwMonitorCallback);
        return previousCallback;
    }

    /**
     * This function returns a list of Monitor objects for all currently connected monitors. This list is unmodifiable.
     *
     * @return The list of all connected Monitor objects.
     */
    public static List<Monitor> getMonitors()
    {
        if (monitors == null)
        {
            monitors = new ArrayList<>();

            PointerBuffer buffer = glfwGetMonitors();

            while (buffer.hasRemaining())
                monitors.add(new Monitor(buffer.get()));

            monitors = Collections.unmodifiableList(monitors);
        }

        return monitors;
    }

    /**
     * This function returns a list of all video modes supported by the specified monitor. The returned list is sorted
     * in ascending order, first by color bit depth (the sum of all channel depths) and then by resolution area (the
     * product of width and height).
     *
     * @return The list of {@link VideoMode}s supported by this Monitor object. Note that this list is unmodifiable.
     */
    public List<VideoMode> getVideoModes()
    {
        if (videoModes == null)
        {
            videoModes = new ArrayList<>();

            GLFWvidmode.Buffer modes = glfwGetVideoModes(handle);

            for (int i = 0; i < modes.capacity(); i++)
            {
                modes.position(i);

                int width = modes.getWidth();
                int height = modes.getHeight();
                int redBits = modes.getRedBits();
                int greenBits = modes.getGreenBits();
                int blueBits = modes.getBlueBits();
                int refreshRate = modes.getRefreshRate();

                videoModes.add(new VideoMode(width, height, redBits, greenBits, blueBits, refreshRate));
            }

            videoModes = Collections.unmodifiableList(videoModes);
        }

        return videoModes;
    }

    /**
     * This function returns the current video mode of the specified monitor. If you have created a full screen window
     * for that monitor, the return value will depend on whether that window is iconified.
     *
     * @return The current {@link VideoMode} of this monitor.
     */
    public VideoMode getVideoMode()
    {
        GLFWvidmode mode = glfwGetVideoMode(handle);

        int width = mode.getWidth();
        int height = mode.getHeight();
        int redBits = mode.getRedBits();
        int greenBits = mode.getGreenBits();
        int blueBits = mode.getBlueBits();
        int refreshRate = mode.getRefreshRate();

        return new VideoMode(width, height, redBits, greenBits, blueBits, refreshRate);
    }

    /**
     * This method returns the native GLFWmonitor pointer as a Java long value. This is required by the native GLFW
     * functions that are wrapped in the {@link GLFW} class that take the pointer of the monitor to function.
     *
     * @return The native handle of this Monitor object.
     */
    public long getHandle()
    {
        return handle;
    }

    /**
     * This method generates a 256-element gamma ramp from the specified exponent and then calls {@link
     * Monitor#setGammaRamp(GammaRamp)} with it. The value must be a finite number greater than zero.
     *
     * @param gamma The desired exponent.
     */
    public void setGamma(float gamma)
    {
        glfwSetGamma(handle, gamma);
    }

    /**
     * This method returns the current gamma ramp of the specified monitor.
     *
     * @return The current GammaRamp, or <code>null</code> if an error occurred.
     */
    public GammaRamp getGammaRamp()
    {
        GLFWgammaramp gammaRamp = glfwGetGammaRamp(handle);

        if (gammaRamp.address() == 0)
            return null;

        int byteBufferSize = gammaRamp.getSize() * Short.BYTES;

        ShortBuffer rBuffer = gammaRamp.getRed(byteBufferSize).asShortBuffer();
        ShortBuffer gBuffer = gammaRamp.getGreen(byteBufferSize).asShortBuffer();
        ShortBuffer bBuffer = gammaRamp.getBlue(byteBufferSize).asShortBuffer();

        short[] red = new short[gammaRamp.getSize()];
        short[] green = new short[gammaRamp.getSize()];
        short[] blue = new short[gammaRamp.getSize()];

        int i = 0;
        while (rBuffer.hasRemaining())
            red[i++] = rBuffer.get();

        i = 0;
        while (gBuffer.hasRemaining())
            green[i++] = gBuffer.get();

        i = 0;
        while (bBuffer.hasRemaining())
            blue[i++] = bBuffer.get();

        return new GammaRamp(red, green, blue);
    }

    /**
     * This method sets the current gamma ramp for the specified monitor. The original gamma ramp for that monitor is
     * saved by GLFW the first time this function is called and is restored by {@link GLFW3#terminate()}.
     *
     * @param gammaRamp The {@link GammaRamp} to use.
     */
    public void setGammaRamp(GammaRamp gammaRamp)
    {
        GLFWgammaramp ramp = GLFWgammaramp.malloc();

        ByteBuffer rBuffer = BufferUtils.createByteBuffer(gammaRamp.getSize() * Short.BYTES);
        ByteBuffer gBuffer = BufferUtils.createByteBuffer(gammaRamp.getSize() * Short.BYTES);
        ByteBuffer bBuffer = BufferUtils.createByteBuffer(gammaRamp.getSize() * Short.BYTES);

        rBuffer.asShortBuffer().put(gammaRamp.getRed()).flip();
        gBuffer.asShortBuffer().put(gammaRamp.getGreen()).flip();
        bBuffer.asShortBuffer().put(gammaRamp.getBlue()).flip();

        ramp.setRed(rBuffer);
        ramp.setGreen(gBuffer);
        ramp.setBlue(bBuffer);
        ramp.setSize(gammaRamp.getSize());

        glfwSetGammaRamp(handle, ramp);

        BufferUtils.freeBuffer(rBuffer);
        BufferUtils.freeBuffer(gBuffer);
        BufferUtils.freeBuffer(bBuffer);

        ramp.free();
    }

    /**
     * <p> This method returns the size, in millimetres, of the display area of the specified monitor.</p>
     *
     * <p> Some systems do not provide accurate monitor size information, either because the monitor EDID data is
     * incorrect or because the driver does not report it accurately.</p>
     *
     * <p> If an error occurs, the size components will be set to zero.</p>
     *
     * @return The physical size of this monitor in millimeters as a Vector2. The x-component is the width and the
     * y-component is the height of the monitor.
     */
    public Vector2 getPhysicalSize()
    {
        IntBuffer sizeBuffer = org.lwjgl.BufferUtils.createIntBuffer(2);

        long addressWidth = BufferUtils.memAddress(sizeBuffer);
        long addressHeight = addressWidth + Integer.BYTES;
        nglfwGetMonitorPhysicalSize(handle, addressWidth, addressHeight);

        Vector2 size = new Vector2(sizeBuffer.get(0), sizeBuffer.get(1));
        BufferUtils.freeBuffer(sizeBuffer);

        return size;
    }

    /**
     * This method returns the position, in screen coordinates, of the upper-left corner of the specified monitor. If
     * there is any error, the position is set to zero, or the origin.
     *
     * @return The virtual position of this monitor.
     */
    public Vector2 getVirtualPosition()
    {
        IntBuffer posBuffer = BufferUtils.createIntBuffer(2);

        long posX = BufferUtils.memAddress(posBuffer);
        long posY = posX + Integer.BYTES;

        nglfwGetMonitorPos(handle, posX, posY);
        Vector2 position = new Vector2(posBuffer.get(0), posBuffer.get(1));

        BufferUtils.freeBuffer(posBuffer);
        return position;
    }

    @Override
    public int hashCode()
    {
        return (int) (handle ^ (handle >>> 32));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Monitor monitor = (Monitor) o;

        return handle == monitor.handle;
    }

    @Override
    public String toString()
    {
        return "Monitor{" +
               "handle=" + handle + ", " +
               "name=\"" + getName() + "\", " +
               "primary=" + isPrimary() +
               '}';
    }

    /**
     * This method returns a human-readable name, encoded as UTF-8, of the specified monitor. The name typically
     * reflects the make and model of the monitor and is not guaranteed to be unique among the connected monitors.
     *
     * @return The name of this monitor object.
     */
    public String getName()
    {
        return glfwGetMonitorName(handle);
    }

    /**
     * This method is used to check if this monitor object is the primary monitor of the OS or not.
     *
     * @return True if this monitor is primary, else False.
     */
    public boolean isPrimary()
    {
        return getMonitors().get(0).equals(this);
    }
}
