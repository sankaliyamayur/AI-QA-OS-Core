package com.aiqaos.healing.approval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * HEAL-2: in-memory reference {@link HealingApprovalStore} — the default, and the store the approval
 * lifecycle is unit-tested against. When the AI-2-backed durable store is added (FI-HEAL2-A) it takes
 * precedence via {@code @Primary}/property gating.
 */
@Component
public class InMemoryHealingApprovalStore implements HealingApprovalStore {

    private final Map<String, HealingApprovalRequest> byId = new ConcurrentHashMap<>();

    @Override
    public void save(HealingApprovalRequest request) {
        byId.put(request.getHealingId(), request);
    }

    @Override
    public Optional<HealingApprovalRequest> find(String healingId) {
        return Optional.ofNullable(byId.get(healingId));
    }

    @Override
    public List<HealingApprovalRequest> pending() {
        List<HealingApprovalRequest> out = new ArrayList<>();
        for (HealingApprovalRequest r : byId.values()) {
            if (r.getStatus() == HealingApprovalStatus.PENDING_APPROVAL) {
                out.add(r);
            }
        }
        return out;
    }
}
