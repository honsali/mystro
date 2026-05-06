package app.web.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import app.doctrine.Doctrine;
import app.input.DoctrineLoader;

class DescriptiveRequestMapperTest {

    private final DescriptiveRequestMapper mapper = new DescriptiveRequestMapper(new DoctrineLoader());

    @Test
    void resolveReturnsSubjectAndDoctrine() {
        DescriptiveRequest request = new DescriptiveRequest();
        request.setId("ilia");
        request.setBirthDate("1975-07-14");
        request.setBirthTime("22:55:00");
        request.setUtcOffset("+01:00");
        request.setLatitude(50.60600755996812);
        request.setLongitude(3.0333769552426793);
        request.setDoctrine("valens");

        DescriptiveRequestMapper.ResolvedBundle resolved = mapper.resolve(request);

        assertEquals("ilia", resolved.subject().getId());
        Doctrine doctrine = resolved.doctrine();
        assertEquals("valens", doctrine.getDoctrineInfo().getId());
        assertEquals("Valens", doctrine.getDoctrineInfo().getName());
    }

    @Test
    void resolveRejectsUnknownDoctrine() {
        DescriptiveRequest request = new DescriptiveRequest();
        request.setId("ilia");
        request.setBirthDate("1975-07-14");
        request.setBirthTime("22:55:00");
        request.setUtcOffset("+01:00");
        request.setLatitude(50.60600755996812);
        request.setLongitude(3.0333769552426793);
        request.setDoctrine("unknown");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.resolve(request));

        assertEquals("Unknown doctrine: unknown", ex.getMessage());
    }
}
