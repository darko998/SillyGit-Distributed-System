package cli.command;

import app.AppConfig;

public class PushCommand implements CLICommand {

    @Override
    public String commandName() {
        return "push";
    }

    @Override
    public void execute(String args) {

        // todo

        AppConfig.chordState.acceptAllCommands();
    }
}