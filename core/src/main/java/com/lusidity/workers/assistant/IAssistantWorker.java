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

package com.lusidity.workers.assistant;


import com.lusidity.domains.system.assistant.message.AssistantMessage;

public interface IAssistantWorker
{

	/**
	 * Send a message to the end of the queue.
	 *
	 * @param assistantMessage An AssistantMessage.
	 * @return true if the message was sent to the end of the queue.
	 */
	boolean recycle(Object assistantMessage);

	/**
	 * Stop the listener if it is stopped and then start the listener.
	 *
	 * @return
	 */
	boolean restart();

	/**
	 * The time to wait between messages being processed.
	 *
	 * @param delay         Time in milliseconds to wait.
	 * @param idleThreshold If the processor utilization exceeds the specified threshold, idle the processor until resources are freed up.
	 * @return true if the worker has started.
	 */
	boolean restart(int delay, int idleThreshold);

	/**
	 * The time to wait between messages being processed.
	 *
	 * @param delay         Time in milliseconds to wait.
	 * @param idleThreshold If the processor utilization exceeds the specified threshold, idle the processor until resources are freed up.
	 * @return true if the worker has started.
	 */
	boolean start(int delay, int idleThreshold);

	/**
	 * Stop the worker.
	 *
	 * @return true if the worker was stopped.
	 */
	boolean stop();

	boolean add(AssistantMessage assistantMessage);

// Getters and setters
	/**
	 * Is a message currently processing?
	 *
	 * @return true if a message is currently processing.
	 */
	boolean isProcessing();

	/**
	 * Is the worker listening?
	 *
	 * @return true if the worker is listening.
	 */
	boolean isStarted();

	void setStarted(boolean started);
}
