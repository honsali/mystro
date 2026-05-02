package app.input;

import java.util.ArrayList;
import java.util.List;
import app.input.model.InputListBundle;

public final class ArgParser {



    public InputListBundle parse(String[] args) {
        List<String> subjectIds = new ArrayList<>();
        List<String> doctrineIds = new ArrayList<>();
        String mode = null;
        for (String arg : args) {
            if ("--subjects".equals(arg)) {
                mode = "subjects";
                continue;
            }
            if ("--doctrines".equals(arg)) {
                mode = "doctrines";
                continue;
            }
            if (arg.startsWith("--")) {
                mode = null;
                continue;
            }
            if ("subjects".equals(mode)) {
                subjectIds.add(arg);
            } else if ("doctrines".equals(mode)) {
                doctrineIds.add(arg);
            }
        }
        return new InputListBundle(subjectIds, doctrineIds);
    }


}
