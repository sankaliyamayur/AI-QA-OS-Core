package com.aiqaos.dashboard.api;

import com.aiqaos.dashboard.dto.ArtifactDTO;
import java.util.List;
import java.util.Optional;

public interface DashboardApiService {

    /**
     * Returns the latest artifact metadata for the given test case ID.
     * Returns empty if no Playwright artifacts have been captured yet.
     */
    Optional<ArtifactDTO> getArtifactsForTestCase(String testCaseId);

    /**
     * Returns all historical artifact records for a test case, ordered by run number.
     * Enables the execution history timeline view in the dashboard.
     */
    List<ArtifactDTO.RunEntry> getArtifactHistory(String testCaseId);
}