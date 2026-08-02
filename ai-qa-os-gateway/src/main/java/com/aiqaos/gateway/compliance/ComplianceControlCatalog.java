package com.aiqaos.gateway.compliance;

import static com.aiqaos.gateway.compliance.ComplianceFramework.GDPR;
import static com.aiqaos.gateway.compliance.ComplianceFramework.ISO_27001;
import static com.aiqaos.gateway.compliance.ComplianceFramework.SOC2;
import static com.aiqaos.gateway.compliance.ControlStatus.NOT_IMPLEMENTED;
import static com.aiqaos.gateway.compliance.ControlStatus.PARTIAL;
import static com.aiqaos.gateway.compliance.ControlStatus.SATISFIED;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * GOV-2 (ADR-081): the curated compliance control catalog — the platform's <b>implemented</b>
 * capabilities mapped to SOC 2 / ISO 27001 / GDPR requirements, with an <b>honest</b> status. A
 * capability is {@code SATISFIED} only where it genuinely ships; gaps are recorded {@code PARTIAL} /
 * {@code NOT_IMPLEMENTED} rather than overstated. This is self-attestation, not a certification.
 */
@Component
public class ComplianceControlCatalog {

    private final List<ComplianceControl> controls = List.of(
            // ── SOC 2 (Trust Services Criteria) ──────────────────────────────────────────────
            new ComplianceControl("SOC2-CC6.1", SOC2, "CC6.1", "Logical access controls",
                    "SEC-1 JWT auth enforcement (deny-by-default)", SATISFIED, "SEC-1 / SecurityConfig"),
            new ComplianceControl("SOC2-CC6.2", SOC2, "CC6.2", "User registration & authorization",
                    "ENT-4 admin user provisioning + role assignment", SATISFIED, "ENT-4 / ADR-067"),
            new ComplianceControl("SOC2-CC6.3", SOC2, "CC6.3", "Role-based access enforcement",
                    "ENT-4 role-derived authorities (hasRole)", SATISFIED, "FI-ENT4-C / ADR-066"),
            new ComplianceControl("SOC2-CC6.6", SOC2, "CC6.6", "Credential & secret management",
                    "SEC-2 externalized secrets (env/secret store)", SATISFIED, "SEC-2"),
            new ComplianceControl("SOC2-CC6.7", SOC2, "CC6.7", "Transmission integrity/encryption",
                    "SEC-4 transport/CSP hardening; service mTLS pending", PARTIAL, "SEC-4; FI-SEC6-B pending"),
            new ComplianceControl("SOC2-CC7.1", SOC2, "CC7.1", "System & data integrity",
                    "SEC-6 HMAC artifact signing (tamper-evidence)", SATISFIED, "SEC-6 / ADR-076"),
            new ComplianceControl("SOC2-CC7.2", SOC2, "CC7.2", "Monitoring & audit logging",
                    "GOV-1 AI audit trail + SecurityAuditLogger", SATISFIED, "GOV-1"),
            new ComplianceControl("SOC2-CC8.1", SOC2, "CC8.1", "Change management",
                    "GOV-4 model/prompt version registry with rollback", SATISFIED, "GOV-4"),
            new ComplianceControl("SOC2-A1.2", SOC2, "A1.2", "Backup & availability",
                    "ENT-5 backup CronJobs (authored; cluster validation pending)", PARTIAL, "ENT-5 / ADR-074"),

            // ── ISO/IEC 27001 (Annex A) ──────────────────────────────────────────────────────
            new ComplianceControl("ISO-A9.4", ISO_27001, "A.9.4", "System & application access control",
                    "SEC-1 JWT auth enforcement", SATISFIED, "SEC-1"),
            new ComplianceControl("ISO-A9.2", ISO_27001, "A.9.2", "User access management",
                    "ENT-4 RBAC admin (create/disable/assign)", SATISFIED, "ENT-4"),
            new ComplianceControl("ISO-A12.4", ISO_27001, "A.12.4", "Logging & monitoring",
                    "GOV-1 audit trail", SATISFIED, "GOV-1"),
            new ComplianceControl("ISO-A12.3", ISO_27001, "A.12.3", "Backup",
                    "ENT-5 backup CronJobs (authored)", PARTIAL, "ENT-5 / ADR-074"),
            new ComplianceControl("ISO-A14.2", ISO_27001, "A.14.2", "Security in development (integrity)",
                    "SEC-6 artifact signing", SATISFIED, "SEC-6 / ADR-076"),
            new ComplianceControl("ISO-A13.1", ISO_27001, "A.13.1", "Network security (in transit)",
                    "service mTLS not yet implemented", NOT_IMPLEMENTED, "FI-SEC6-B (infra)"),

            // ── GDPR ─────────────────────────────────────────────────────────────────────────
            new ComplianceControl("GDPR-Art32", GDPR, "Art.32", "Security of processing (isolation)",
                    "ENT-1 tenant isolation + SEC-2 secrets", SATISFIED, "ENT-1"),
            new ComplianceControl("GDPR-Art30", GDPR, "Art.30", "Records of processing (audit)",
                    "GOV-1 audit trail", SATISFIED, "GOV-1"),
            new ComplianceControl("GDPR-Art25", GDPR, "Art.25", "Data protection by design",
                    "ENT-1 tenancy + SEC-1 auth (secure-by-default)", SATISFIED, "ENT-1 / SEC-1"),
            new ComplianceControl("GDPR-Art17", GDPR, "Art.17", "Right to erasure (DSAR)",
                    "no data-subject erasure workflow", NOT_IMPLEMENTED, "future FI"),
            new ComplianceControl("GDPR-Art20", GDPR, "Art.20", "Data portability (export)",
                    "no data-subject export workflow", NOT_IMPLEMENTED, "future FI"));

    /** The curated control catalog (immutable). */
    public List<ComplianceControl> controls() {
        return controls;
    }
}
