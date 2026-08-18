CREATE TABLE IF NOT EXISTS modules (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    requirement_path VARCHAR(500),
    total_test_cases INT DEFAULT 0,
    pass_rate INT DEFAULT 0,
    last_run TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS test_cases (
    id VARCHAR(50) PRIMARY KEY,
    module_id VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    feature VARCHAR(255),
    priority VARCHAR(50),
    status VARCHAR(50),
    browser VARCHAR(50),
    duration VARCHAR(50),
    build VARCHAR(100),
    last_run TIMESTAMP,
    device VARCHAR(100),
    environment VARCHAR(100),
    commit_hash VARCHAR(100),
    pipeline_id VARCHAR(100),
    failure_reason TEXT,
    error_message TEXT,
    stack_trace TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (module_id) REFERENCES modules(id)
);

-- Real modules seeded from actual user stories only.
-- Module hierarchy: Role (User/Admin) → Section (story title) → Test Cases
-- New modules are auto-created by the pipeline when a new user story is executed.

-- Authentication module: seeded from US-001.md (User Login story)
INSERT INTO modules (id, name, description, requirement_path, total_test_cases, pass_rate, last_run) VALUES
('authentication', 'Authentication', 'Role-based authentication module. Contains all login and access control user stories.', 'resources/user-stories/Login/US-001.md', 14, 85, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Test cases for the Authentication → User Login section (from US-001.md Acceptance Criteria)
INSERT INTO test_cases (id, module_id, name, description, feature, priority, status, browser, duration, build, last_run, device, environment, commit_hash, pipeline_id) VALUES
('TC-AUTH-001', 'authentication', 'AC-001: Open application URL successfully', 'Given the user opens the OnePurpos application URL, When the application loads, Then the OnePurpos Openings page should open successfully.', 'User Login', 'High', 'Passed', 'Chromium', '3 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-002', 'authentication', 'AC-002: Login dropdown options visible', 'Given the user is on the application page, When the user clicks the Login dropdown, Then the Login dropdown options should be displayed.', 'User Login', 'High', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-003', 'authentication', 'AC-003: Login side drawer opens', 'Given the Login dropdown is open, When the user selects the Log in option, Then the Login side drawer should open successfully.', 'User Login', 'High', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-004', 'authentication', 'AC-004: Login form fields visible', 'Given the Login side drawer is open, When the user views the login form, Then Email field, Password field, Login button, and Forgot Password link should be available.', 'User Login', 'High', 'Passed', 'Chromium', '1 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-005', 'authentication', 'AC-005: Successful login with valid credentials', 'Given the user enters a valid registered Email and valid Password, When the user clicks the Login button, Then the user should be successfully authenticated and logged in.', 'User Login', 'High', 'Passed', 'Chromium', '4 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-006', 'authentication', 'AC-006: Login fails with invalid Email', 'Given the user enters an invalid Email, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.', 'User Login', 'High', 'Passed', 'Chromium', '3 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-007', 'authentication', 'AC-007: Login fails with invalid Password', 'Given the user enters an invalid Password, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.', 'User Login', 'High', 'Failed', 'Firefox', '28 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Ubuntu 22.04', 'QA', null, null),
('TC-AUTH-008', 'authentication', 'AC-008: Login fails when both fields invalid', 'Given both Email and Password are invalid, When the user clicks the Login button, Then login should fail and an appropriate error message should be displayed.', 'User Login', 'High', 'Passed', 'Chromium', '3 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-009', 'authentication', 'AC-009: Email validation when blank', 'Given the Email field is blank, When the user clicks the Login button, Then the appropriate Email validation message should appear.', 'User Login', 'Medium', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-010', 'authentication', 'AC-010: Password validation when blank', 'Given the Password field is blank, When the user clicks the Login button, Then the appropriate Password validation message should appear.', 'User Login', 'Medium', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-011', 'authentication', 'AC-011: Both fields blank validation', 'Given both Email and Password fields are blank, When the user clicks the Login button, Then required field validation messages should appear.', 'User Login', 'Medium', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-012', 'authentication', 'AC-012: Invalid email format validation', 'Given the user enters an Email in an invalid format, When the user clicks the Login button, Then login should not proceed and appropriate email validation should be displayed.', 'User Login', 'Medium', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-013', 'authentication', 'AC-013: Forgot Password navigation works', 'Given the Login side drawer is open, When the user clicks the Forgot Password link, Then the Forgot Password page should open successfully.', 'User Login', 'Medium', 'Passed', 'Chromium', '3 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null),
('TC-AUTH-014', 'authentication', 'AC-014: No broken page after Forgot Password navigation', 'Given the user clicks the Forgot Password link, When the navigation is completed, Then no broken page or error page should be displayed.', 'User Login', 'Low', 'Passed', 'Chromium', '2 sec', 'Bld-2026.08.14-01', CURRENT_TIMESTAMP, 'Desktop - Windows 11', 'QA', null, null)
ON CONFLICT DO NOTHING;
