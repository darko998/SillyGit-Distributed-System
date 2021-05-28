package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.pinger.Pinger;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	private Pinger pinger;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener, Pinger pinger) {
		this.parser = parser;
		this.listener = listener;
		this.pinger = pinger;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
		pinger.stop();
	}

}
