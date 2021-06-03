package cli;

import app.AppConfig;
import app.Cancellable;
import cli.command.*;
import servent.SimpleServentListener;
import servent.pinger.Pinger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A simple CLI parser. Each command has a name and arbitrary arguments.
 * 
 * Currently supported commands:
 * 
 * <ul>
 * <li><code>info</code> - prints information about the current node</li>
 * <li><code>pause [ms]</code> - pauses exection given number of ms - useful when scripting</li>
 * <li><code>ping [id]</code> - sends a PING message to node [id] </li>
 * <li><code>broadcast [text]</code> - broadcasts the given text to all nodes</li>
 * <li><code>causal_broadcast [text]</code> - causally broadcasts the given text to all nodes</li>
 * <li><code>print_causal</code> - prints all received causal broadcast messages</li>
 * <li><code>stop</code> - stops the servent and program finishes</li>
 * </ul>
 * 
 * @author bmilojkovic
 *
 */
public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;

	private final List<CLICommand> commandList;
	private final List<CLICommand> conflictCommandList;
	
	public CLIParser(SimpleServentListener listener, Pinger pinger) {
		this.commandList = new ArrayList<>();
		this.conflictCommandList = new ArrayList<>();

		commandList.add(new InfoCommand());
		commandList.add(new PauseCommand());
		commandList.add(new SuccessorAndPredecessorInfo());
		commandList.add(new DHTGetCommand());
		commandList.add(new DHTPutCommand());
		commandList.add(new StopCommand(this, listener, pinger));
		commandList.add(new AddFileCommand());
		commandList.add(new CommitCommand());
		commandList.add(new PushCommand());
		commandList.add(new PullCommand());
		commandList.add(new ViewCommand());

		conflictCommandList.add(new PushCommand());
		conflictCommandList.add(new PullCommand());
		conflictCommandList.add(new ViewCommand());

	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		
		while (working) {

			String commandLine = sc.nextLine();

			int spacePos = commandLine.indexOf(" ");

			String commandName = null;
			String commandArgs = null;
			if (spacePos != -1) {
				commandName = commandLine.substring(0, spacePos);
				commandArgs = commandLine.substring(spacePos + 1, commandLine.length());
			} else {
				commandName = commandLine;
			}

			boolean found = false;

			if(AppConfig.chordState.amIAcceptAllCommands()) {
				for (CLICommand cliCommand : commandList) {
					if (cliCommand.commandName().equals(commandName)) {
						cliCommand.execute(commandArgs);
						found = true;
						break;
					}
				}

				if (!found) {
					AppConfig.timestampedErrorPrint("Unknown command: " + commandName);
				}
			} else {
				for (CLICommand cliCommand : conflictCommandList) {
					if (cliCommand.commandName().equals(commandName)) {
						cliCommand.execute(commandArgs);
						found = true;
						break;
					}
				}

				if (!found) {
					AppConfig.timestampedErrorPrint("Unknown command: " + commandName + "! Enter 'view', 'push' or 'pull' to resolve conflict!");
				}
			}

		}
		
		sc.close();
	}
	
	@Override
	public void stop() {
		this.working = false;
	}

}
