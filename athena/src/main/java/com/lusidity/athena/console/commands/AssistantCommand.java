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

package com.lusidity.athena.console.commands;

import com.lusidity.Environment;
import com.lusidity.console.Console;
import com.lusidity.console.commands.BaseCommand;
import com.lusidity.domains.acs.security.BasePrincipal;
import com.lusidity.domains.system.assistant.worker.BaseAssistantWorker;
import com.lusidity.framework.json.JsonData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Map;

@SuppressWarnings({"UnusedDeclaration", "MethodMayBeStatic"})
public
class AssistantCommand
    extends BaseCommand
{
    private static final String WORKER_OPTION = "worker";
    private static final String START_OPTION = "start";    
    private static final String RESTART_OPTION = "restart";
    private static final String STOP_OPTION = "stop";
    private static final String KILL_OPTION = "kill";
    private static final String DELAY_OPTION = "delay";
    private static final String IDLE_THRESHOLD_OPTION = "idleThreshold";
    private static final String HELP_OPTION = "help";

// -------------------------- OTHER METHODS --------------------------

    public AssistantCommand(){
        super();
    }

	@SuppressWarnings("IfStatementWithTooManyBranches")
    @Override
	public
	void execute(Console console, CommandLine commandLine)
		throws Exception
	{
        if(commandLine.hasOption(AssistantCommand.HELP_OPTION))
        {
            console.help(this.getName());
        }
        else {
            if (!Environment.getInstance().getWorkers().isEmpty()) {
                if (commandLine.hasOption(AssistantCommand.WORKER_OPTION)) {
                    String opt = commandLine.getOptionValue(AssistantCommand.WORKER_OPTION);
                    BaseAssistantWorker worker = Environment.getInstance().getWorker(opt);
                    if (null != worker) {
                        if (commandLine.hasOption(AssistantCommand.STOP_OPTION)) {
                            this.stop(worker, console);
                        }
                        else if (commandLine.hasOption(AssistantCommand.START_OPTION)) {
                            this.start(worker, commandLine, console);
                        }
                        else if (commandLine.hasOption(AssistantCommand.KILL_OPTION)) {
                            this.kill(worker, console);
                        }
                        else if (commandLine.hasOption(AssistantCommand.RESTART_OPTION)) {
                            this.restart(worker, commandLine, console);
                        }
                        else {
                            console.say("-stop, -start or -restart is required.");
                        }

                    } else {
                        console.say(String.format("A valid worker is required. (%s)", this.getAvailableWorkers()));
                    }
                } else {
                    console.help(this.getName());
                }
            } else {
                console.say("None of the workers are running, nor are they in a state that they can be started.");
            }
        }
	}

    private void restart(BaseAssistantWorker worker, CommandLine cmdLine, Console console) {
        this.stop(worker, console);
        if(!worker.isStarted())
        {
            this.start(worker, cmdLine, console);
        }
    }

    private void start(BaseAssistantWorker worker, CommandLine cmdLine, Console console) {
        this.sayBegin("Starting", worker, console);
        boolean started = worker.start(this.getDelay(worker, cmdLine), this.getThreshold(worker, cmdLine));
        if(started)
        {
            this.sayStarted(worker, console);
        }
        else
        {
            this.sayStartedFailed(worker, console);
        }
    }

    private void stop(BaseAssistantWorker worker, Console console) {
        this.sayBegin("Stopping", worker, console);
        console.say("It could take up to a minute to stop the worker.");
        boolean hasStopped = worker.stop();
        String stopped = hasStopped ? "has been" : "has not been";
        console.say(String.format("The worker %s stopped.", stopped));
    }

    private void kill(BaseAssistantWorker worker, Console console) {
        this.sayBegin("Killing", worker, console);
        console.say("It could take up to a minute to kill the worker.");
        boolean hasStopped = worker.kill();
        String stopped = hasStopped ? "has been" : "has not been";
        console.say(String.format("The worker %s has been killed.", stopped));
    }

    public void sayStarted(BaseAssistantWorker worker, Console console)
    {
        String started = String.format("The worker has been started. Delay: %d, Idle Threshold: %d", worker.getDelay(), worker.getIdleThreshold());
        console.say(started);
    }

    private void sayBegin(String cmd, BaseAssistantWorker worker, Console console) {
        console.say(String.format("%s %s...", cmd,  worker.getClass().getSimpleName()));
    }

    private void sayStartedFailed(BaseAssistantWorker worker, Console console) {
        String failed = String.format("Could not start %s worker.", worker.getClass().getSimpleName());
        console.say(failed);
    }

    public int getDelay(BaseAssistantWorker worker, CommandLine commandLine)
    {
        int result = worker.getDelay();

        if(commandLine.hasOption(AssistantCommand.DELAY_OPTION))
        {
            result = Integer.parseInt(commandLine.getOptionValue(AssistantCommand.DELAY_OPTION));

            if(result<0)
            {
                result= BaseAssistantWorker.DEFAULT_DELAY;
            }
        }

        return result;
    }

    public int getThreshold(BaseAssistantWorker worker, CommandLine commandLine)
    {
        int result = worker.getIdleThreshold();

        if(commandLine.hasOption(AssistantCommand.IDLE_THRESHOLD_OPTION))
        {
            result = Integer.parseInt(commandLine.getOptionValue(AssistantCommand.IDLE_THRESHOLD_OPTION));

            if(result<0)
            {
                result= BaseAssistantWorker.DEFAULT_IDLE_THRESHOLD;
            }
        }

        return result;
    }

	@Override
	public
	String getDescription()
	{
		return "Stop or restart an assistant worker.";
	}

	/**
	 * Get name for this command.
	 *
	 * @return Command name.
	 */
	@Override
	public
	String getName()
	{
		return "assistant";
	}

	/**
	 * Get command line options for this command.
	 *
	 * @return Apache Commons CLI Options.
	 */
	@Override
	public
	Options getOptions()
	{
        Options options=new Options();

        String description  = String.format("The worker to -stop, -start or -restart. Available workers are %s", this.getAvailableWorkers());

        options.addOption(AssistantCommand.WORKER_OPTION, true, description);
        options.addOption(AssistantCommand.STOP_OPTION, false, "Stop the worker.");
        options.addOption(AssistantCommand.START_OPTION, false, "Start the worker.");
        options.addOption(AssistantCommand.KILL_OPTION, false, "Kill the worker forcing it to stop.");
        options.addOption(AssistantCommand.RESTART_OPTION, false, "Either start a worker that is stopped or stop and start the worker.");

        String delayDescription = String.format("The amount of time in mills before processing the next message." +
                "  The default is %d milliseconds", BaseAssistantWorker.DEFAULT_DELAY);
        Option delayOption = new Option(AssistantCommand.DELAY_OPTION, true, delayDescription);
        delayOption.setType(Integer.class);
        delayOption.setRequired(false);
        options.addOption(delayOption);

        String thresholdDescription = String.format("Must be in the range of 0-100." +
                "  The default is %d", BaseAssistantWorker.DEFAULT_IDLE_THRESHOLD);
        Option thresholdOption = new Option(AssistantCommand.IDLE_THRESHOLD_OPTION, true, thresholdDescription);
        thresholdOption.setType(Integer.class);
        thresholdOption.setRequired(false);
        options.addOption(thresholdOption);

        return options;
    }

    @Override
    public boolean isWebEnabled() {
        return true;
    }

    @Override
    public JsonData execute(JsonData params, BasePrincipal principal) {
        JsonData result = JsonData.createObject();

        if (!Environment.getInstance().getWorkers().isEmpty()) {
            if (params.hasKey(AssistantCommand.WORKER_OPTION)) {
                String opt = params.getString(AssistantCommand.WORKER_OPTION);
                BaseAssistantWorker worker = Environment.getInstance().getWorker(opt);
                if (null != worker) {
                    if (params.hasKey(AssistantCommand.STOP_OPTION)) {
                        boolean b = worker.stop();
                        result.put("success", b);
                    } else if (params.hasKey(AssistantCommand.START_OPTION)) {
                        boolean b = worker.start(params.getInteger(AssistantCommand.DELAY_OPTION), params.getInteger(AssistantCommand.IDLE_THRESHOLD_OPTION));
                        result.put("success", b);
                    }
                    else if (params.hasKey(AssistantCommand.KILL_OPTION)) {
                        boolean b = worker.kill();
                        result.put("success", b);
                    }
                    else if (params.hasKey(AssistantCommand.RESTART_OPTION)) {
                        boolean b = worker.stop();
                        result.put("success", b);
                        if (b) {
                            b = worker.start(params.getInteger(AssistantCommand.DELAY_OPTION), params.getInteger(AssistantCommand.IDLE_THRESHOLD_OPTION));
                            result.put("success", b);
                        }
                    } else {
                        result.put("help", "-stop, -start or -restart is required.");
                    }

                } else {
                    result.put("error", String.format("A valid worker is required. (%s)", this.getAvailableWorkers()));
                }
            } else {
                result.put("error", "Invalid worker name.");
            }
        } else {
            result.put("error", "None of the workers are running, nor are they in a state that they can be started.");
        }
        return result;
    }

    public String getAvailableWorkers() {
        Map<String, BaseAssistantWorker> workers = Environment.getInstance().getWorkers();
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, BaseAssistantWorker> worker: workers.entrySet())
        {
            if(sb.length()>0)
            {
                sb.append(", ");
            }
            sb.append(worker.getKey());
        }
        return sb.toString();
    }
}
