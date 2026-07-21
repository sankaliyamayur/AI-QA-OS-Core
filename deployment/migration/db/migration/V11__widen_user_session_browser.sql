-- V11: Widen security_user_sessions.browser so real browsers can log in.
--
-- AuthenticationController stores the raw User-Agent header in this column.
-- It was VARCHAR(100), but current Chrome/Firefox/Edge user agents are ~130
-- characters, so the session insert at the end of a SUCCESSFUL login failed
-- with a value-too-long error. The login surfaced as HTTP 403, which the UI
-- renders as "Invalid username or password." -- the password was never wrong.
--
-- Only clients with short user agents (curl, PowerShell) could log in.
-- 255 matches the entity default and the sibling device/location columns.

ALTER TABLE security_user_sessions
    ALTER COLUMN browser TYPE VARCHAR(255);
