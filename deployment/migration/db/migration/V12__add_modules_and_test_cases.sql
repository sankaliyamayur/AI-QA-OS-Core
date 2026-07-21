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

-- Insert Mock Data for Modules
INSERT INTO modules (id, name, description, requirement_path, total_test_cases, pass_rate, last_run) VALUES 
('admin-login', 'Admin Authentication Module', 'Contains test cases for the Admin portal login and authentication flows.', 'resources/user-stories/Login/US-001.md', 8, 85, '2026-07-17 09:15:00'),
('user-registration', 'User Registration Module', 'Handles test cases related to new user onboarding and validation.', 'resources/user-stories/Registration/US-002.md', 12, 100, '2026-07-16 14:30:00'),
('checkout-flow', 'E-Commerce Checkout Flow', 'Validates cart, shipping, and payment processing.', 'resources/user-stories/Checkout/US-003.md', 25, 92, '2026-07-17 11:22:00'),
('inventory-sync', 'Inventory Synchronization API', 'Validates the background sync process between warehouse and store.', 'resources/api-specs/Inventory/API-004.md', 5, 60, '2026-07-17 10:05:00')
ON CONFLICT DO NOTHING;

-- Insert Mock Data for Test Cases
INSERT INTO test_cases (id, module_id, name, description, feature, priority, status, browser, duration, build, last_run, device, environment, commit_hash, pipeline_id) VALUES
('TC-AL-001', 'admin-login', 'AC-001: Verify Admin Login with Valid Credentials', 'Given Admin is on Login page, When valid Email and Password are entered, Then Login should be successful.', 'Admin Login Process', 'High', 'Passed', 'Chrome', '8 sec', 'Bld-2026.07.17-01', '2026-07-17 09:10:00', 'Desktop - Windows 11', 'Staging', '1a0dd34', 'PL-GHA-993848'),
('TC-AL-002', 'admin-login', 'AC-002: Verify Login Failure with Invalid Email', 'Given Admin enters invalid Email, When Login button is clicked, Then login should fail.', 'Login Validation', 'High', 'Passed', 'Chrome', '7 sec', 'Bld-2026.07.17-01', '2026-07-17 09:11:00', 'Desktop - Windows 11', 'Staging', '1a0dd34', 'PL-GHA-993848'),
('TC-AL-003', 'admin-login', 'AC-003: Verify Login Failure with Invalid Password', 'Given Admin enters invalid Password, When Login button is clicked, Then login should fail.', 'Login Validation', 'High', 'Failed', 'Firefox', '32 sec', 'Bld-2026.07.17-01', '2026-07-17 09:14:00', 'Desktop - Ubuntu 22.04', 'Staging', '1a0dd34', 'PL-GHA-993848'),
('TC-AL-004', 'admin-login', 'AC-004: Verify Login Failure with both fields Invalid', 'Given both Email and Password are invalid, When Login button is clicked, Then login should fail.', 'Login Validation', 'High', 'Passed', 'Chrome', '5 sec', 'Bld-2026.07.17-01', '2026-07-17 09:12:00', 'Desktop - Windows 11', 'Staging', null, null)
ON CONFLICT DO NOTHING;
