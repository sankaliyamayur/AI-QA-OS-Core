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

# Page Object Model for Home Page (Products Page)
# File: pages/home_page.py
class HomePage:
    def __init__(self, page: Page):
        self.page = page
        self.product_header = page.locator(".title")
        self.cart_icon = page.locator(".shopping_cart_link")

    def is_on_homepage(self):
        return self.product_header.text_content() == "Products"

    def get_product_header_text(self):
        return self.product_header.text_content()

    def add_item_to_cart(self, item_name):
        self.page.locator(f"div.inventory_item_name:has-text('{item_name}') + div .btn_primary").click()

    def go_to_cart(self):
        self.cart_icon.click()

# Test Script for TC-001: User Login with Valid Credentials
# File: tests/test_login.py
def test_valid_login(page: Page):
    login_page = LoginPage(page)
    home_page = HomePage(page)

    login_page.navigate()
    login_page.login("standard_user", "secret_sauce")

    expect(page).to_have_url("https://www.saucedemo.com/inventory.html")
    expect(home_page.product_header).to_be_visible()
    expect(home_page.product_header).to_have_text("Products")

