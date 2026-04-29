package app.doctrine;

import java.util.Map;

public interface DescriptiveResult {
    String getDoctrineId();

    Map<String, Object> getData();
}
