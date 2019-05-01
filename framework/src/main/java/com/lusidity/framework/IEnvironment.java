/*
 * Copyright 2018 lusidity inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.lusidity.framework;

import com.lusidity.framework.system.logging.LoggerX;
import org.reflections.Reflections;

import java.util.HashMap;

public interface IEnvironment {
    String getName();

    Reflections getReflections();

    RunLevel getRunLevel();

    @SuppressWarnings("UnusedDeclaration")
    void setRunLevel(RunLevel runLevel);

    LoggerX getLogger();

    @SuppressWarnings("UnusedDeclaration")
    boolean isDebugging();

    @SuppressWarnings("UnusedDeclaration")
    boolean isSandbox();

    String getAppProperty(String key);

    HashMap<String, String> getAppProperties();

    @SuppressWarnings("UnusedDeclaration")
    boolean isRunning();

    @SuppressWarnings("UnusedDeclaration")
    boolean isStopped();

    @SuppressWarnings("UnusedDeclaration")
    void registerModules(String classPath);

    void stop();

    @SuppressWarnings("UnusedDeclaration")
    void finest(String format, Object... parameters);

    void finer(String format, Object... parameters);

    void fine(String format, Object... parameters);

    void info(String format, Object... parameters);

    void warning(Exception e);

    void warning(String format, Object... parameters);

    void severe(Exception e);

    void severe(String format, Object... parameters);

    void stupid(Exception e);

    void notImplemented();

    /**
     * Run level (like in UNIX-like operating systems).
     */
    public
    enum RunLevel
    {
        Stop,
        Console,
        Server
    }
}
