package app;

import java.util.List;
import app.astroseek.AstroSeekService;
import app.common.Config;
import app.common.Logger;
import app.common.io.ApplicationArguments;
import app.common.io.NativeListLoader;
import app.mystro.MystroService;
import app.validator.ValidatorService;

public final class App {

    public static void main(String[] args) throws Exception {
        Logger.getInstance().clear();

        ApplicationArguments cli = ApplicationArguments.parse(args);
        List<String> requestedNames = cli.hasNames() ? cli.names() : NativeListLoader.loadNames(Config.NATIVE_LIST_PATH);

        (new MystroService()).generate(requestedNames);
        (new AstroSeekService()).generate(requestedNames);
        (new ValidatorService()).compare(requestedNames);

        Logger.getInstance().printErrors();
    }

    private App() {}

}
