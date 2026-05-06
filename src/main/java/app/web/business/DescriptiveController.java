package app.web.business;

import app.basic.BasicCalculator;
import app.chart.model.NatalChart;
import app.input.model.Subject;
import app.output.DescriptiveAstrologyReport;
import app.runtime.EngineVersion;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public final class DescriptiveController {

    private final BasicCalculator basicCalculator;
    private final DescriptiveRequestMapper requestMapper;
    private final EngineVersion engineVersion;

    public DescriptiveController(BasicCalculator basicCalculator,
                                 DescriptiveRequestMapper requestMapper,
                                 EngineVersion engineVersion) {
        this.basicCalculator = basicCalculator;
        this.requestMapper = requestMapper;
        this.engineVersion = engineVersion;
    }

    @PostMapping("/descriptive")
    public ResponseEntity<?> descriptive(@RequestBody(required = false) DescriptiveRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new ErrorResponse("Request body is required"));
        }

        DescriptiveRequestMapper.ResolvedBundle resolved;
        try {
            resolved = requestMapper.resolve(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new ErrorResponse(e.getMessage()));
        }

        Subject subject = resolved.subject();
        app.doctrine.Doctrine doctrine = resolved.doctrine();
        NatalChart natalChart = doctrine.calculateDescriptive(subject, basicCalculator);
        DescriptiveAstrologyReport report = new DescriptiveAstrologyReport(
                engineVersion.get(), subject, doctrine.getDoctrineInfo(), natalChart);

        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(report);
    }
}
