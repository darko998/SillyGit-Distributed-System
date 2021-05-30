package cli.command;

import app.AppConfig;
import app.ServentInfo;

public class SuccessorAndPredecessorInfo implements CLICommand {

	@Override
	public String commandName() {
		return "successor_info";
	}

	@Override
	public void execute(String args) {
		ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();

		AppConfig.timestampedStandardPrint("Predecessor: " + AppConfig.chordState.getPredecessor());

		int num = 0;
		for (ServentInfo serventInfo : successorTable) {
			AppConfig.timestampedStandardPrint("Successor " + num + ": " + serventInfo);
			num++;
		}

	}

}
