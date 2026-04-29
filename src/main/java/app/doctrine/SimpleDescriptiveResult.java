package app.doctrine;

import java.util.Map;

public final class SimpleDescriptiveResult implements DescriptiveResult {
    private final String doctrineId;
    private final Map<String, Object> data;

    public SimpleDescriptiveResult(String doctrineId, Map<String, Object> data) {
        this.doctrineId = doctrineId;
        this.data = data == null ? Map.of() : Map.copyOf(data);
    }

    @Override
    public String getDoctrineId() { return doctrineId; }

    @Override
    public Map<String, Object> getData() { return data; }
}
