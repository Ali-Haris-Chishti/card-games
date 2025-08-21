package com.ahccode.server;

import com.ahccode.server.ui.GameServerManagerCLI;
import com.ahccode.server.ui.GameServerManagerUI;

public class Main {
    public static void main(String[] args) {
        switch (parseMode(args)) {
            case "gui" -> GameServerManagerUI.main(args);
            case "cli" -> GameServerManagerCLI.main(args);
            case null, default -> throw new IllegalArgumentException("Unknown arguments");
        }
    }

    public static String parseMode(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-m") || args[i].equals("--mode")) {
                return args[++i];
            }
        }
        return null;
    }

}