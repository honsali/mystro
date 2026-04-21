package app.common.io;

import java.util.ArrayList;
import java.util.List;

public record ApplicationArguments(List<String> names) {
    public static ApplicationArguments parse(String[] args) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--names" -> {
                    i++;
                    while (i < args.length && !args[i].startsWith("--")) {
                        names.add(args[i]);
                        i++;
                    }
                    i--;
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        return new ApplicationArguments(List.copyOf(names));
    }

    public boolean hasNames() {
        return !names.isEmpty();
    }
}
