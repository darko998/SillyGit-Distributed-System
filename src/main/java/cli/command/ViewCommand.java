package cli.command;

import app.AppConfig;

public class ViewCommand implements CLICommand {

    @Override
    public String commandName() {
        return "view";
    }

    @Override
    public void execute(String args) {

        // todo

        AppConfig.chordState.acceptAllCommands();
    }
}
