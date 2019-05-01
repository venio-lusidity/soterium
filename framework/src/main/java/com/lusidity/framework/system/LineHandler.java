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

package com.lusidity.framework.system;


public interface LineHandler {
    /**
     * Method to keep track of lines read.
     * @return last line read.
     */
    void incrementLinesRead();

    /**
     * Total number of lines read.
     * @return lines read.
     */
    int getLinesRead();

    /**
     * If looking for a specific object this gives a way to return a result.
     * Use in combination with the handlers return value of false.
     * @return A result as an Object.
     */
    Object getValue();
    /**
     * Passes a line read to the handler.
     * @param line the line read from the file.
     * @return true to stop reading exiting the handler.
     */
    boolean handle(String line);

	boolean handle(String[] values);
}
