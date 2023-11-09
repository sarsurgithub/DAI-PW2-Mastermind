package heig.vd.dai;

import heig.vd.dai.client.Client;
import heig.vd.dai.server.Server;
import picocli.CommandLine;

public class Main {

    @CommandLine.Command(name = "mastermind", subcommands = {Server.class, Client.class})
    static private class Mastermind{}
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Mastermind()).execute(args);
        System.exit(exitCode);
    }
}