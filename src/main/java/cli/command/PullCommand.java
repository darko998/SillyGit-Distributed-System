package cli.command;

import app.AppConfig;

public class PullCommand implements CLICommand {

    @Override
    public String commandName() {
        return "pull";
    }

    @Override
    public void execute(String args) {

        // todo

        AppConfig.chordState.acceptAllCommands();
    }
}
