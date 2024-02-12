package org.github.kshashov.navback;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TilesController {
    private final OsmService osmService;

    public TilesController(OsmService osmService) {
        this.osmService = osmService;
    }


    @PostMapping(value = "/tile", produces = MediaType.IMAGE_PNG_VALUE)
    byte[] getTile(@RequestBody OsmService.Window window) {
        return osmService.getTile(window, 25000);
    }
}
