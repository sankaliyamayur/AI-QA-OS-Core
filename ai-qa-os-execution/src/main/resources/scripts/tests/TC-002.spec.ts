import pytest
from playwright.sync_api import Page, expect
from pages.login_page import LoginPage

# Page Object Model for Login Page
# File: pages/login_page.py
class LoginPage:
    def __init__(self, page: Page):
        self.page = page
        self.username_input = page.locator("#user-name")
        self.password_input = page.locator("#password")
        self.login_button = page.locator("#login-button")
        self.error_message_container = page.locator("[data-test='error']")

    def navigate(self):
        self.page.goto("https://www.saucedemo.com/")

    def login(self, username, password):
        self.username_input.fill(username)
        self.password_input.fill(password)
        self.login_button.click()

    def get_error_message(self):
        return self.error_message_container.text_content()

# Test Script for TC-002: User Login with Invalid Credentials
# File: tests/test_login.py
def test_invalid_login(page: Page):
    login_page = LoginPage(page)

    login_page.navigate()
    login_page.login("invalid_user", "wrong_password")

    expect(login_page.error_message_container).to_be_visible()
    expect(login_page.error_message_container).to_have_text("Epic sadface: Username and password do not match any user in this service")
    expect(page).to_have_url("https://www.saucedemo.com/")

