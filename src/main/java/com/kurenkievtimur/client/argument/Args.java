package com.kurenkievtimur.client.argument;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Args {
    @Parameter(names = {"--type", "-t"}, description = "type request")
    private String type;
    @Parameter(names = {"--key", "-k"}, description = "key")
    private String key;
    @Parameter(names = {"--value", "-v"}, description = "value")
    private String value;
    @Parameter(names = {"--input", "-in"}, description = "input file")
    private String input;

    private Args() {
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getInput() {
        return input;
    }

    public static Args parseArguments(String[] args) {
        Args arguments = new Args();

        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        return arguments;
    }
}