package org.example.campaigntask.controller.campaign;

import jakarta.validation.Valid;
import org.example.campaigntask.dto.campaign.CampaignResponse;
import org.example.campaigntask.dto.campaign.CreateCampaignRequest;
import org.example.campaigntask.dto.campaign.UpdateCampaignRequest;
import org.example.campaigntask.service.campaign.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {
    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping()
    public ResponseEntity<List<CampaignResponse>> getAll() {
        return ResponseEntity.ok().body(campaignService.getAll());
    }

    @PostMapping()
    public ResponseEntity<CampaignResponse> createCampaign(@RequestBody @Valid CreateCampaignRequest createCampaignRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(createCampaignRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> updateCampaign(@PathVariable Long id, @RequestBody @Valid UpdateCampaignRequest updateCampaignRequest) {
        return ResponseEntity.status(200).body(campaignService.update(id, updateCampaignRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}