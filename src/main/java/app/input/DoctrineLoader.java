package app.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import app.doctrine.Doctrine;
import app.doctrine.impl.dorotheus.DorotheusDoctrine;
import app.doctrine.impl.ptolemy.PtolemyDoctrine;
import app.doctrine.impl.valens.ValensDoctrine;
import app.input.model.InputListBundle;
import app.output.Logger;

public final class DoctrineLoader {
    private final Map<String, Doctrine> doctrines = new LinkedHashMap<>();

    public DoctrineLoader() {
        register(new DorotheusDoctrine());
        register(new PtolemyDoctrine());
        register(new ValensDoctrine());
    }

    public void register(Doctrine doctrine) {
        doctrines.put(doctrine.getId(), doctrine);
    }

    public Optional<Doctrine> find(String id) {
        return Optional.ofNullable(doctrines.get(id));
    }


    public void load(InputListBundle input) {
        List<Doctrine> doctrines = new ArrayList<>();
        for (String doctrineId : input.getDoctrineIds()) {
            Optional<Doctrine> doctrine = find(doctrineId);
            if (doctrine.isPresent()) {
                doctrines.add(doctrine.get());
            } else {
                Logger.instance.error("doctrine", "Skipping unknown doctrine: " + doctrineId);
            }
        }

        if (doctrines.isEmpty()) {
            Logger.instance.error("doctrine", "No doctrines requested. Use --doctrines <id> [...]");
        }

        input.setDoctrines(doctrines);
    }
}
