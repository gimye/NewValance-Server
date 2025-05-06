package capston.new_valance.controller;

import capston.new_valance.dto.req.VideoMetadataRequest;
import capston.new_valance.dto.res.VideoMetadataResponse;
import capston.new_valance.service.VideoMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class VideoMetadataController {

    private final VideoMetadataService videoMetadataService;

    @PostMapping("/metadata")
    public ResponseEntity<VideoMetadataResponse> uploadMetadata(@RequestBody VideoMetadataRequest request) {
        VideoMetadataResponse response = videoMetadataService.saveMetadata(request);
        return ResponseEntity.ok(response);
    }
}
