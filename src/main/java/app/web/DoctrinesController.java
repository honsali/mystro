package app.web;

import app.input.DoctrineLoader;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public final class DoctrinesController {

    private final DoctrineLoader doctrineLoader;

    public DoctrinesController(DoctrineLoader doctrineLoader) {
        this.doctrineLoader = doctrineLoader;
    }

    @GetMapping("/doctrines")
    public DoctrinesResponse list() {
        List<DoctrineInfo> infos = doctrineLoader.list().stream()
                .map(DoctrineInfo::new)
                .toList();
        return new DoctrinesResponse(infos);
    }
}
