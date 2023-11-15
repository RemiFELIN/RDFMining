package fr.inria.corese.command;

import fr.inria.corese.command.programs.Convert;
import fr.inria.corese.command.programs.Sparql;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "Corese-command", version = App.version, mixinStandardHelpOptions = true, subcommands = {
        Convert.class, Sparql.class,
        // Profile.class, LDScript.class,
})

public final class App implements Runnable {

    public final static String version = "4.4.1";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Print usage
        CommandLine.usage(new App(), System.out);
    }
}