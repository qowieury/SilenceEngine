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

package com.shc.silenceengine.audio.openal;

import com.shc.silenceengine.core.SilenceEngine;
import com.shc.silenceengine.math.Vector3;

import static com.shc.silenceengine.audio.AudioDevice.Constants.*;

/**
 * Represents an OpenAL Sound source.
 *
 * @author Sri Harsha Chilakapati
 */
public class ALSource
{
    private int     id;
    private boolean disposed;

    /**
     * Constructs a new OpenAL source.
     *
     * @throws ALException.OutOfMemory If there is no memory available.
     */
    public ALSource()
    {
        id = SilenceEngine.audio.alGenSources();
    }

    /**
     * Attaches an ALBuffer to this source. The buffer contains the audio samples that this source should play.
     *
     * @param buffer The ALBuffer containing the sound samples to be played.
     *
     * @throws ALException.InvalidValue If the buffer is already disposed.
     */
    public void attachBuffer(ALBuffer buffer)
    {
        setParameter(AL_BUFFER, buffer == null ? 0 : buffer.getID());
    }

    /**
     * Sets the value of a property in this source object.
     *
     * @param parameter The name of the parameter to be set
     * @param value     The value of the parameter to be set
     *
     * @throws ALException.InvalidEnum If the value is invalid for parameter.
     * @throws ALException             If this source is already disposed.
     */
    public void setParameter(int parameter, int value)
    {
        if (isDisposed())
            throw new ALException("ALSource is already disposed");

        SilenceEngine.audio.alSourcei(id, parameter, value);
        ALError.check();
    }

    /**
     * Sets the value of a property in this source object.
     *
     * @param parameter The name of the parameter to be set
     * @param value     The value of the parameter to be set
     *
     * @throws ALException.InvalidEnum If the value is invalid for parameter.
     * @throws ALException             If this source is already disposed.
     */
    public void setParameter(int parameter, boolean value)
    {
        setParameter(parameter, value ? AL_TRUE : AL_FALSE);
    }

    /**
     * Sets the value of a property in this source object.
     *
     * @param parameter The name of the parameter to be set
     * @param value     The value of the parameter to be set
     *
     * @throws ALException.InvalidEnum If the value is invalid for parameter.
     * @throws ALException             If this source is already disposed.
     */
    public void setParameter(int parameter, float value)
    {
        if (isDisposed())
            throw new ALException("ALSource is already disposed");

        SilenceEngine.audio.alSourcef(id, parameter, value);
        ALError.check();
    }

    /**
     * Sets the value of a property in this source object.
     *
     * @param parameter The name of the parameter to be set
     * @param value     The value of the parameter to be set
     *
     * @throws ALException.InvalidEnum If the value is invalid for parameter.
     * @throws ALException             If this source is already disposed.
     */
    public void setParameter(int parameter, Vector3 value)
    {
        setParameter(parameter, value.x, value.y, value.z);
    }

    /**
     * Sets the value of a property in this source object.
     *
     * @param parameter The name of the parameter to be set
     * @param value1    The first value of the parameter to be set
     * @param value2    The second value of the parameter to be set
     * @param value3    The third value of the parameter to be set
     *
     * @throws ALException.InvalidEnum If the values are invalid for parameter.
     * @throws ALException             If this source is already disposed.
     */
    public void setParameter(int parameter, float value1, float value2, float value3)
    {
        if (isDisposed())
            throw new ALException("ALSource is already disposed");

        SilenceEngine.audio.alSource3f(id, parameter, value1, value2, value3);
        ALError.check();
    }

    /**
     * Starts playing from this source.
     *
     * @throws ALException If the source is already disposed.
     */
    public void play()
    {
        if (isDisposed())
            throw new ALException("Cannot play a disposed ALSource");

        SilenceEngine.audio.alSourcePlay(id);
        ALError.check();
    }

    /**
     * Pauses the playback from this source.
     *
     * @throws ALException If the source is already disposed.
     */
    public void pause()
    {
        if (isDisposed())
            throw new ALException("Cannot pause a disposed ALSource");

        SilenceEngine.audio.alSourcePause(id);
        ALError.check();
    }

    /**
     * Stops the playback from this source.
     *
     * @throws ALException If the source is already disposed.
     */
    public void stop()
    {
        if (isDisposed())
            throw new ALException("Cannot stop a disposed ALSource");

        SilenceEngine.audio.alSourceStop(id);
        ALError.check();
    }

    /**
     * Rewinds this source, so that it will be seek to the starting.
     *
     * @throws ALException If the source is already disposed.
     */
    public void rewind()
    {
        if (isDisposed())
            throw new ALException("Cannot rewind a disposed ALSource");

        SilenceEngine.audio.alSourceRewind(id);
        ALError.check();
    }

    /**
     * Queries the current playback state of this ALSource.
     *
     * @return The playback state of this ALSource.
     *
     * @throws ALException.InvalidName If the source is already disposed.
     */
    public State getState()
    {
        int state = getParameter(AL_SOURCE_STATE);

        switch (state)
        {
            case AL_PLAYING:
                if (getParameter(AL_LOOPING) == AL_TRUE)
                    return State.LOOPING;
                else
                    return State.PLAYING;

            case AL_PAUSED:
                return State.PAUSED;
        }

        return State.STOPPED;
    }

    /**
     * Gets a property from this ALSource.
     *
     * @param parameter The parameter of the property to be returned.
     *
     * @return The value of the property with key parameter.
     *
     * @throws ALException.InvalidEnum      If the value is invalid for the parameter.
     * @throws ALException.InvalidName      If this source is already disposed.
     * @throws ALException.InvalidOperation If there is no context.
     * @throws ALException.InvalidValue     If the value is not an integer.
     */
    public int getParameter(int parameter)
    {
        int result = SilenceEngine.audio.alGetSourcei(id, parameter);
        ALError.check();

        return result;
    }

    /**
     * Disposes this source, releasing all of it's resources.
     *
     * @throws ALException If the source is already disposed.
     */
    public void dispose()
    {
        if (isDisposed())
            throw new ALException("Cannot Dispose an already disposed OpenAL Source");

        SilenceEngine.audio.alDeleteSources(id);
        ALError.check();

        disposed = true;
    }

    /**
     * @return The OpenAL ID for this ALSource instance.
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return True if this source is disposed, Else false.
     */
    public boolean isDisposed()
    {
        return disposed;
    }

    /**
     * The State of this source.
     */
    public enum State
    {
        PLAYING, STOPPED, PAUSED, LOOPING
    }
}
