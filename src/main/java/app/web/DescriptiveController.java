package app.web;

import app.output.DescriptiveAstrologyReport;
import app.output.Logger;
import app.runtime.DescriptiveReportService;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public final class DescriptiveController {

    private final DescriptiveReportService service;
    private final DescriptiveRequestMapper requestMapper;

    public DescriptiveController(DescriptiveReportService service, DescriptiveRequestMapper requestMapper) {
        this.service = service;
        this.requestMapper = requestMapper;
    }

    @PostMapping("/descriptive")
    public ResponseEntity<?> descriptive(@RequestBody(required = false) DescriptiveRequest request) throws Exception {
        if (request == null) {
            return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noStore())
                    .body(new ErrorResponse("Request body is required"));
        }

        DescriptiveRequestMapper.ResolvedBundle resolved;
        try {
            resolved = requestMapper.resolve(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noStore())
                    .body(new ErrorResponse(e.getMessage()));
        }

        DescriptiveAstrologyReport report = Logger.instance.runIsolated(() ->
                service.generateDescriptiveReports(resolved.bundle()).get(0)
        );

        String subjectId = report.getSubject().getId();
        String doctrineId = report.getDoctrine().getId();
        String filename = subjectId + "-" + doctrineId + "-descriptive.json";

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(new DescriptiveResponse(report, filename));
    }
}
