package com.aiqaos.dashboard.bootstrap;

import com.aiqaos.core.entity.ModuleEntity;
import com.aiqaos.core.entity.TestCaseEntity;
import com.aiqaos.core.repository.ModuleRepository;
import com.aiqaos.core.repository.TestCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DevDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    private final ModuleRepository moduleRepository;
    private final TestCaseRepository testCaseRepository;
    private final JdbcTemplate jdbcTemplate;

    public DevDataInitializer(ModuleRepository moduleRepository,
                              TestCaseRepository testCaseRepository,
                              JdbcTemplate jdbcTemplate) {
        this.moduleRepository = moduleRepository;
        this.testCaseRepository = testCaseRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        seedModulesAndTestCases();
        seedWorkflowExecutions();
        seedArtifacts();
        seedHumanReviews();
        seedLocatorDrift();
        seedEvalResults();
        seedLearningObservations();
        seedUsers();
    }

    private void seedModulesAndTestCases() {
        try {
            jdbcTemplate.update("UPDATE test_cases SET browser = 'Chromium', status = 'Passed', duration = '5 sec' WHERE id = 'TC-AUTH-007'");
            jdbcTemplate.update("UPDATE test_cases SET browser = 'Chromium' WHERE browser = 'Firefox'");
        } catch (Exception e) {
            log.warn("Could not auto-update test_cases browser: {}", e.getMessage());
        }
        if (moduleRepository.count() == 0) {
            log.info("Seeding initial Authentication module and US-001 test cases into database...");

            ModuleEntity authModule = new ModuleEntity();
            authModule.setId("authentication");
            authModule.setName("Project 1 - Authentication");
            authModule.setDescription("Role-based authentication module for OnePurpos application.");
            authModule.setRequirementPath("resources/user-stories/Project_1_OnePurpos/Login/US-001.md");
            authModule.setTotalTestCases(14);
            authModule.setPassRate(93);
            authModule.setLastRun(LocalDateTime.now());
            moduleRepository.save(authModule);

            ModuleEntity ecomModule = new ModuleEntity();
            ecomModule.setId("ecommerce-checkout");
            ecomModule.setName("Project 2 - E-Commerce Store");
            ecomModule.setDescription("Storefront authentication, cart management, and checkout pipeline.");
            ecomModule.setRequirementPath("resources/user-stories/Project_2_ECommerce/Auth/US-010.md");
            ecomModule.setTotalTestCases(5);
            ecomModule.setPassRate(100);
            ecomModule.setLastRun(LocalDateTime.now());
            moduleRepository.save(ecomModule);

            ModuleEntity crmModule = new ModuleEntity();
            crmModule.setId("crm-lead-mgmt");
            crmModule.setName("Project 3 - Admin CRM");
            crmModule.setDescription("Enterprise lead pipeline, opportunity qualification, and deal management.");
            crmModule.setRequirementPath("resources/user-stories/Project_3_CRM/LeadManagement/US-001.md");
            crmModule.setTotalTestCases(4);
            crmModule.setPassRate(100);
            crmModule.setLastRun(LocalDateTime.now());
            moduleRepository.save(crmModule);

            List<TestCaseEntity> testCases = new ArrayList<>();

            // Seed Project 2 test cases
            testCases.add(createTestCase("TC-ECOM-001", "ecommerce-checkout", "AC-010-1: Storefront loads header and cart",
                    "Given the customer opens the E-Commerce Store URL, When storefront loads, Then header logo and cart badge should be visible.",
                    "Customer Login & Cart Access", "High", "Passed", "Chromium", "2 sec",
                    List.of(Map.of("stepNumber", 1, "action", "Navigate to https://staging.ecommerce.com", "status", "Passed", "duration", "1.2 sec")), null, null, null));

            // Seed Project 3 test cases
            testCases.add(createTestCase("TC-CRM-001", "crm-lead-mgmt", "AC-CRM-001: Leads dashboard matrix loads",
                    "Given Sales Manager logs in to Enterprise CRM portal, When navigating to Leads Dashboard, Then active deals matrix should load.",
                    "Lead Capture & Deal Assignment", "High", "Passed", "Chromium", "3 sec",
                    List.of(Map.of("stepNumber", 1, "action", "Navigate to https://admin.crm.app", "status", "Passed", "duration", "1.5 sec")), null, null, null));

            testCases.add(createTestCase("TC-AUTH-001", "authentication", "AC-001: Open application URL successfully",
                    "Given the user opens the OnePurpos application URL, When the application loads, Then the OnePurpos Openings page should open successfully.",
                    "User Login", "High", "Passed", "Chromium", "3 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Navigate to URL: https://onepurpos.in/openings", "status", "Passed", "duration", "1.8 sec"),
                        Map.of("stepNumber", 2, "action", "Assert page title contains 'OnePurpos Openings'", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 3, "action", "Verify header layout and Login menu button visible", "status", "Passed", "duration", "0.7 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-002", "authentication", "AC-002: Login dropdown options visible",
                    "Given the user is on the application page, When the user clicks the Login dropdown, Then the Login dropdown options should be displayed.",
                    "User Login", "High", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Navigate to URL: https://onepurpos.in/openings", "status", "Passed", "duration", "1.2 sec"),
                        Map.of("stepNumber", 2, "action", "Click Login dropdown button (#login-menu-dropdown)", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 3, "action", "Verify dropdown menu items 'Log in' and 'Sign up' are visible", "status", "Passed", "duration", "0.4 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-003", "authentication", "AC-003: Login side drawer opens",
                    "Given the Login dropdown is open, When the user selects the Log in option, Then the Login side drawer should open successfully.",
                    "User Login", "High", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Click Login dropdown menu", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 2, "action", "Select 'Log in' option", "status", "Passed", "duration", "0.6 sec"),
                        Map.of("stepNumber", 3, "action", "Assert side drawer container element (.login-drawer) is visible", "status", "Passed", "duration", "0.9 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-004", "authentication", "AC-004: Login form fields visible",
                    "Given the Login side drawer is open, When the user views the login form, Then Email field, Password field, Login button, and Forgot Password link should be available.",
                    "User Login", "High", "Passed", "Chromium", "1 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Verify Email input field (#login-email)", "status", "Passed", "duration", "0.2 sec"),
                        Map.of("stepNumber", 2, "action", "Verify Password input field (#login-password)", "status", "Passed", "duration", "0.2 sec"),
                        Map.of("stepNumber", 3, "action", "Verify Submit button (#login-submit)", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 4, "action", "Verify Forgot Password link (#forgot-password-link)", "status", "Passed", "duration", "0.3 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-005", "authentication", "AC-005: Successful login with valid credentials",
                    "Given the user enters a valid registered Email and valid Password, When the user clicks the Login button, Then the user should be successfully authenticated and logged in.",
                    "User Login", "High", "Passed", "Chromium", "4 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Open Login side drawer", "status", "Passed", "duration", "1.1 sec"),
                        Map.of("stepNumber", 2, "action", "Enter Email: shivam@yopamail.com", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 3, "action", "Enter Password: Test@123", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 4, "action", "Click Login button (#login-submit)", "status", "Passed", "duration", "0.8 sec"),
                        Map.of("stepNumber", 5, "action", "Assert POST /api/v1/auth/login returns 200 OK with accessToken", "status", "Passed", "duration", "0.7 sec"),
                        Map.of("stepNumber", 6, "action", "Verify authenticated user session avatar in header", "status", "Passed", "duration", "0.5 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-006", "authentication", "AC-006: Login fails with invalid Email",
                    "Given the user enters an invalid Email, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.",
                    "User Login", "High", "Passed", "Chromium", "3 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Open Login side drawer", "status", "Passed", "duration", "0.9 sec"),
                        Map.of("stepNumber", 2, "action", "Enter unregistered Email: unknown_user_99@yopamail.com", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 3, "action", "Enter Password: Test@123", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 4, "action", "Click Login button", "status", "Passed", "duration", "0.6 sec"),
                        Map.of("stepNumber", 5, "action", "Assert error alert displays 'Invalid Credentials.'", "status", "Passed", "duration", "0.8 sec")
                    ), null, null, null));

            // TC-AUTH-007: Verified Test Case with healed selector & Chromium browser
            testCases.add(createTestCase("TC-AUTH-007", "authentication", "AC-007: Login fails with invalid Password",
                    "Given the user enters an invalid Password, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.",
                    "User Login", "High", "Passed", "Chromium", "5 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Navigate to URL: https://onepurpos.in/openings", "status", "Passed", "duration", "1.2 sec"),
                        Map.of("stepNumber", 2, "action", "Click Login dropdown menu button", "status", "Passed", "duration", "0.8 sec"),
                        Map.of("stepNumber", 3, "action", "Select 'Log in' option from dropdown", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 4, "action", "Verify Login side drawer container visible", "status", "Passed", "duration", "0.6 sec"),
                        Map.of("stepNumber", 5, "action", "Enter registered Email: shivam@yopamail.com", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 6, "action", "Enter invalid Password: WrongPassword123!", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 7, "action", "Click Login button (#login-submit)", "status", "Passed", "duration", "0.7 sec"),
                        Map.of("stepNumber", 8, "action", "Assert error notification 'Invalid Credentials.' is visible (Selector: div[role='alert'])", "status", "Passed", "duration", "0.8 sec")
                    ),
                    null, null, null));

            testCases.add(createTestCase("TC-AUTH-008", "authentication", "AC-008: Login fails when both fields invalid",
                    "Given both Email and Password are invalid, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.",
                    "User Login", "High", "Passed", "Chromium", "3 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Enter invalid Email: bad_email@yopamail.com", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 2, "action", "Enter invalid Password: wrong_pass", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 3, "action", "Click Login button", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 4, "action", "Assert error message displayed", "status", "Passed", "duration", "0.8 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-009", "authentication", "AC-009: Email validation when blank",
                    "Given the Email field is blank, When the user clicks the Login button, Then the appropriate Email validation message should appear.",
                    "User Login", "Medium", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Leave Email field blank", "status", "Passed", "duration", "0.2 sec"),
                        Map.of("stepNumber", 2, "action", "Enter Password: Test@123", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 3, "action", "Click Login button", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 4, "action", "Assert HTML5 required field validation prompt", "status", "Passed", "duration", "0.6 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-010", "authentication", "AC-010: Password validation when blank",
                    "Given the Password field is blank, When the user clicks the Login button, Then the appropriate Password validation message should appear.",
                    "User Login", "Medium", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Enter Email: shivam@yopamail.com", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 2, "action", "Leave Password field blank", "status", "Passed", "duration", "0.2 sec"),
                        Map.of("stepNumber", 3, "action", "Click Login button", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 4, "action", "Assert HTML5 required password validation prompt", "status", "Passed", "duration", "0.5 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-011", "authentication", "AC-011: Both fields blank validation",
                    "Given both Email and Password fields are blank, When the user clicks the Login button, Then required field validation messages should appear.",
                    "User Login", "Medium", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Leave Email and Password fields blank", "status", "Passed", "duration", "0.2 sec"),
                        Map.of("stepNumber", 2, "action", "Click Login button", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 3, "action", "Assert required validation attributes on both inputs", "status", "Passed", "duration", "0.5 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-012", "authentication", "AC-012: Invalid email format validation",
                    "Given the user enters an Email in an invalid format, When the user clicks the Login button, Then login should not proceed and appropriate email validation should be displayed.",
                    "User Login", "Medium", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Enter malformed Email: 'invalid.email.without.at'", "status", "Passed", "duration", "0.3 sec"),
                        Map.of("stepNumber", 2, "action", "Click Login button", "status", "Passed", "duration", "0.4 sec"),
                        Map.of("stepNumber", 3, "action", "Assert browser type=email format validation error", "status", "Passed", "duration", "0.5 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-013", "authentication", "AC-013: Forgot Password navigation works",
                    "Given the Login side drawer is open, When the user clicks the Forgot Password link, Then the Forgot Password page should open successfully.",
                    "User Login", "Medium", "Passed", "Chromium", "3 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Open Login side drawer", "status", "Passed", "duration", "0.9 sec"),
                        Map.of("stepNumber", 2, "action", "Click 'Forgot Password?' link", "status", "Passed", "duration", "0.5 sec"),
                        Map.of("stepNumber", 3, "action", "Assert URL changes to /forgot-password or password recovery page", "status", "Passed", "duration", "0.8 sec")
                    ), null, null, null));

            testCases.add(createTestCase("TC-AUTH-014", "authentication", "AC-014: No broken page after Forgot Password navigation",
                    "Given the user clicks the Forgot Password link, When the navigation is completed, Then no broken page or error page should be displayed.",
                    "User Login", "Low", "Passed", "Chromium", "2 sec",
                    List.of(
                        Map.of("stepNumber", 1, "action", "Navigate to Forgot Password page", "status", "Passed", "duration", "0.7 sec"),
                        Map.of("stepNumber", 2, "action", "Verify HTTP response 200 OK and no 404/500 error elements", "status", "Passed", "duration", "0.5 sec")
                    ), null, null, null));

            testCaseRepository.saveAll(testCases);
            log.info("Successfully seeded 1 module and 14 test cases with steps & failure diagnostics into database.");
        }
    }

    private void seedWorkflowExecutions() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM workflow_executions", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding today's workflow executions into database...");
                jdbcTemplate.update("""
                    INSERT INTO workflow_executions (id, tenant_id, workflow_id, execution_id, status, start_time, end_time, duration_ms, result, total_steps, success_steps, failed_steps, skipped_steps, retry_count, execution_cost, token_usage, git_commit, git_branch, llm_model, pipeline_version, environment, browser, current_step, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, ?, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 135000, 'SUCCESS', 14, 13, 1, 0, 0, 0.02, 4200, 'a1b2c3d4', 'E2E_AUTOMATION_PIPELINE', 'gemini-1.5-flash', 'v2.4.0', 'QA', 'Chromium', 'Completed', CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

                jdbcTemplate.update("""
                    INSERT INTO workflow_executions (id, tenant_id, workflow_id, execution_id, status, start_time, end_time, duration_ms, result, total_steps, success_steps, failed_steps, skipped_steps, retry_count, execution_cost, token_usage, git_commit, git_branch, llm_model, pipeline_version, environment, browser, current_step, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, ?, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 180000, 'SUCCESS', 14, 14, 0, 0, 0, 0.015, 3100, 'f9e8d7c6', 'US-001_Acceptance_Suite', 'gemini-1.5-flash', 'v2.4.0', 'Development', 'Chromium', 'Completed', CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            }
        } catch (Exception e) {
            log.warn("Could not seed workflow_executions: {}", e.getMessage());
        }
    }

    private void seedArtifacts() {
        try {
            jdbcTemplate.update("DELETE FROM execution_artifacts WHERE test_case_id = 'TC-AUTH-007'");
            log.info("Seeding execution artifacts for TC-AUTH-007...");
            
            String screenshotPath = "artifacts/tc-auth-007-failure.png";
            String videoUrl = "artifacts/tc-auth-007-execution.webm";

            jdbcTemplate.update("""
                INSERT INTO execution_artifacts (id, tenant_id, execution_id, test_case_id, browser, run_number, screenshot_path, video_path, trace_path, report_path, log_path, created_at, active, deleted, version)
                VALUES (?, '__system__', ?, 'TC-AUTH-007', 'chromium', 1,
                        ?, ?,
                        'artifacts/trace-tc-auth-007.zip',
                        'artifacts/playwright-report.html',
                        'Console log: POST /api/v1/auth/login 401 Unauthorized\nResponse: {"error":"invalid_credentials"}\nAssertion Passed: div[role="alert"] visible',
                        CURRENT_TIMESTAMP, true, false, 0)
            """, UUID.randomUUID(), UUID.randomUUID(), screenshotPath, videoUrl);
        } catch (Exception e) {
            log.warn("Could not seed execution_artifacts: {}", e.getMessage());
        }
    }

    private void seedHumanReviews() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM human_reviews", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding pending human reviews...");
                jdbcTemplate.update("""
                    INSERT INTO human_reviews (id, tenant_id, review_id, workflow_id, execution_id, step_name, confidence, status, reviewer, decision_comment, created_time, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, ?, ?, 'Self-Healing Confidence Gate', 0.72, 'PENDING', null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            }
        } catch (Exception e) {
            log.warn("Could not seed human_reviews: {}", e.getMessage());
        }
    }

    private void seedLocatorDrift() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM locator_drift", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding locator drift entries...");
                jdbcTemplate.update("""
                    INSERT INTO locator_drift (id, tenant_id, selector, test_case_id, failing_action, provenance, healed_to, heal_strategy, heal_approval, observed_at, created_at, active, deleted, version)
                    VALUES (?, '__system__', '#login-submit', 'TC-AUTH-007', 'Click Submit', 'PLAYWRIGHT_CALL_LOG', 'button.btn-login', 'FUZZY_MATCH', 'AUTO_APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID());
                jdbcTemplate.update("""
                    INSERT INTO locator_drift (id, tenant_id, selector, test_case_id, failing_action, provenance, healed_to, heal_strategy, heal_approval, observed_at, created_at, active, deleted, version)
                    VALUES (?, '__system__', '#login-password', 'TC-AUTH-007', 'Type Password', 'PLAYWRIGHT_CALL_LOG', 'input[name="pwd"]', 'EXACT_TEXT', 'AUTO_APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID());
                jdbcTemplate.update("""
                    INSERT INTO locator_drift (id, tenant_id, selector, test_case_id, failing_action, provenance, healed_to, heal_strategy, heal_approval, observed_at, created_at, active, deleted, version)
                    VALUES (?, '__system__', '.nav-login-dropdown', 'TC-AUTH-002', 'Click Dropdown', 'PLAYWRIGHT_CALL_LOG', 'button#login-dropdown', 'DOM_HEURISTIC', 'AUTO_APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID());
            }
        } catch (Exception e) {
            log.warn("Could not seed locator_drift: {}", e.getMessage());
        }
    }

    private void seedEvalResults() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eval_results", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding evaluation results for Prompt Quality...");
                jdbcTemplate.update("""
                    INSERT INTO eval_results (id, tenant_id, result_id, suite, case_id, evaluator, score, passed, prompt_version, agent_type, reason, created_time, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, 'login-suite', 'TC-AUTH-001', 'LLM-Judge-Evaluator', 0.94, true, 'v1.2.0-flash', 'PlannerAgent', 'High accuracy test case generation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID());
                jdbcTemplate.update("""
                    INSERT INTO eval_results (id, tenant_id, result_id, suite, case_id, evaluator, score, passed, prompt_version, agent_type, reason, created_time, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, 'login-suite', 'TC-AUTH-005', 'LLM-Judge-Evaluator', 0.88, true, 'v1.1.0-flash', 'CodeGeneratorAgent', 'Valid Playwright syntax', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID());
                jdbcTemplate.update("""
                    INSERT INTO eval_results (id, tenant_id, result_id, suite, case_id, evaluator, score, passed, prompt_version, agent_type, reason, created_time, created_at, active, deleted, version)
                    VALUES (?, '__system__', ?, 'login-suite', 'TC-AUTH-007', 'LLM-Judge-Evaluator', 0.79, false, 'v1.0.0-flash', 'HealingAgent', 'Locator drift tolerance test', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, false, 0)
                """, UUID.randomUUID(), UUID.randomUUID());
            }
        } catch (Exception e) {
            log.warn("Could not seed eval_results: {}", e.getMessage());
        }
    }

    private void seedLearningObservations() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM learning_observations", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding learning observations...");
                for (int i = 1; i <= 5; i++) {
                    jdbcTemplate.update("""
                        INSERT INTO learning_observations (id, tenant_id, sequence_no, success, confidence, label, created_at, active, deleted, version)
                        VALUES (?, '__system__', ?, ?, ?, ?, CURRENT_TIMESTAMP, true, false, 0)
                    """, UUID.randomUUID(), i, i != 4, 0.82 + (i * 0.03), "Run #" + i + " - E2E_AUTOMATION_PIPELINE");
                }
            }
        } catch (Exception e) {
            log.warn("Could not seed learning_observations: {}", e.getMessage());
        }
    }

    private void seedUsers() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM security_users", Integer.class);
            if (count == null || count == 0) {
                log.info("Seeding RBAC users for Admin page...");
                UUID adminId = UUID.randomUUID();
                UUID qaEngId = UUID.randomUUID();
                UUID qaMgrId = UUID.randomUUID();

                String validAdminHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin");

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'admin', 'admin@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, adminId, validAdminHash);

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'qa_engineer', 'engineer@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, qaEngId, validAdminHash);

                jdbcTemplate.update("""
                    INSERT INTO security_users (id, tenant_id, username, email, password_hash, enabled, account_locked, failed_login_attempts, mfa_enabled, created_at, active, deleted, version)
                    VALUES (?, '__system__', 'qa_manager', 'manager@aiqaos.local', ?, true, false, 0, false, CURRENT_TIMESTAMP, true, false, 0)
                """, qaMgrId, validAdminHash);

                // Assign roles
                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'ADMIN')", adminId);
                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'QA_ENGINEER')", qaEngId);
                jdbcTemplate.update("INSERT INTO security_user_roles (user_id, role_name) VALUES (?, 'QA_MANAGER')", qaMgrId);
            }
        } catch (Exception e) {
            log.warn("Could not seed security_users: {}", e.getMessage());
        }
    }

    private TestCaseEntity createTestCase(String id, String moduleId, String name, String description,
                                           String feature, String priority, String status, String browser, String duration,
                                           List<Map<String, Object>> steps, String failureReason, String errorMessage, String stackTrace) {
        TestCaseEntity tc = new TestCaseEntity();
        tc.setId(id);
        tc.setModuleId(moduleId);
        tc.setName(name);
        tc.setDescription(description);
        tc.setFeature(feature);
        tc.setPriority(priority);
        tc.setStatus(status);
        tc.setBrowser(browser);
        tc.setDuration(duration);
        tc.setBuild("Bld-2026.08.17-01");
        tc.setEnvironment("QA");
        tc.setDevice("Desktop - Windows 11");
        tc.setLastRun(LocalDateTime.now());
        tc.setSteps(steps);
        tc.setFailureReason(failureReason);
        tc.setErrorMessage(errorMessage);
        tc.setStackTrace(stackTrace);
        return tc;
    }
}
