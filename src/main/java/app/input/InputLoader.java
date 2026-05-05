package app.input;

import java.io.IOException;
import app.input.model.InputListBundle;
import app.output.Logger;

public final class InputLoader {



    public InputListBundle load(String[] args) throws IOException {
        InputListBundle input = (new ArgParser()).parse(args);
        (new SubjectListParser()).parse(input);
        (new DoctrineLoader()).load(input);


        if (Logger.instance.hasErrors()) {
            throw new IllegalArgumentException("Input validation failed. See output/run-logger.json");
        }
        return input;
    }



}
