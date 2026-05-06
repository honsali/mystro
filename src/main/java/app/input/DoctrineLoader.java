package app.input;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import app.doctrine.Doctrine;
import app.doctrine.impl.dorotheus.DorotheusDoctrine;
import app.doctrine.impl.ptolemy.PtolemyDoctrine;
import app.doctrine.impl.valens.ValensDoctrine;

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

    public List<Doctrine> list() {
        return List.copyOf(doctrines.values());
    }
}
