import pytest
from playwright.sync_api import Page, expect
from pages.login_page import LoginPage
from pages.home_page import HomePage
from pages.cart_page import CartPage
from pages.checkout_page import CheckoutPage

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
        self.inventory_items = page.locator(".inventory_item_name")

    def is_on_homepage(self):
        return self.product_header.text_content() == "Products"

    def get_product_header_text(self):
        return self.product_header.text_content()

    def add_item_to_cart(self, item_name):
        self.page.locator(f"div.inventory_item_name:has-text('{item_name}') + div .btn_primary").click()

    def go_to_cart(self):
        self.cart_icon.click()

    def get_all_product_names(self):
        return self.inventory_items.all_text_contents()

# Page Object Model for Cart Page
# File: pages/cart_page.py
class CartPage:
    def __init__(self, page: Page):
        self.page = page
        self.cart_items = page.locator(".cart_item")
        self.checkout_button = page.locator("#checkout")

    def get_item_names_in_cart(self):
        return self.page.locator(".inventory_item_name").all_text_contents()

    def go_to_checkout(self):
        self.checkout_button.click()

# Page Object Model for Checkout Page
# File: pages/checkout_page.py
class CheckoutPage:
    def __init__(self, page: Page):
        self.page = page
        self.first_name_input = page.locator("#first-name")
        self.last_name_input = page.locator("#last-name")
        self.zip_code_input = page.locator("#postal-code")
        self.continue_button = page.locator("#continue")
        self.finish_button = page.locator("#finish")
        self.checkout_complete_header = page.locator(".complete-header")

    def fill_your_information(self, first_name, last_name, zip_code):
        self.first_name_input.fill(first_name)
        self.last_name_input.fill(last_name)
        self.zip_code_input.fill(zip_code)
        self.continue_button.click()

    def finish_checkout(self):
        self.finish_button.click()

    def get_checkout_complete_message(self):
        return self.checkout_complete_header.text_content()

# Test Script for TC-005: Checkout Process
# File: tests/test_checkout.py
def test_checkout_process(page: Page):
    login_page = LoginPage(page)
    home_page = HomePage(page)
    cart_page = CartPage(page)
    checkout_page = CheckoutPage(page)

    login_page.navigate()
    login_page.login("standard_user", "secret_sauce")

    home_page.add_item_to_cart("Sauce Labs Backpack")
    home_page.go_to_cart()

    cart_page.go_to_checkout()
    expect(page).to_have_url("https://www.saucedemo.com/checkout-step-one.html")

    checkout_page.fill_your_information("John", "Doe", "12345")
    expect(page).to_have_url("https://www.saucedemo.com/checkout-step-two.html")

    checkout_page.finish_checkout()
    expect(page).to_have_url("https://www.saucedemo.com/checkout-complete.html")
    expect(checkout_page.checkout_complete_header).to_be_visible()
    expect(checkout_page.checkout_complete_header).to_have_text("THANK YOU FOR YOUR ORDER")

