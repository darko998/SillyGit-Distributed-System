package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.pinger.Pinger;

public class QuitCommand implements CLICommand {

    private CLIParser parser;
    private SimpleServentListener listener;
    private Pinger pinger;

    public QuitCommand(CLIParser parser, SimpleServentListener listener, Pinger pinger) {
        this.parser = parser;
        this.listener = listener;
        this.pinger = pinger;
    }

    @Override
    public String commandName() {
        return "quit";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Quit...");
        parser.stop();
        listener.stop();
        pinger.stop();
    }
}
