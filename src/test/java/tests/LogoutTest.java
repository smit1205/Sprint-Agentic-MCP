package tests;

import base.BaseTest;
import helperUtils.DriverFactory;
import helperUtils.ExcelReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.LogoutPage;

import java.time.Duration;

public class LogoutTest extends BaseTest {

    private WebDriver driver;
    private WebDriverWait wait;

    // all locators used in this test
    private static final By LOGOUT_BTN   = By.cssSelector("button[data-testid='logout']");
    private static final By LOGIN_LINK   = By.xpath("//a[contains(@href,'login')] | //button[contains(normalize-space(),'Login')]");
    private static final By ERROR_TOAST  = By.xpath("//*[contains(@class,'alert') or contains(@class,'toast') or @role='alert']");
    private static final By OVERLAY_CLOSE = By.xpath(
            "//*[contains(@class,'close') or contains(@class,'dismiss')" +
                    " or contains(@aria-label,'close') or contains(@aria-label,'Close')]"
    );

    @BeforeMethod(alwaysRun = true)
    public void init() {

        driver = DriverFactory.getDriver();

        wait = new WebDriverWait(
                driver,
                Duration.ofSeconds(25)
        );
    }


    @DataProvider(name = "validLoginData")
    public Object[][] getValidData() {
        return new Object[][]{
                {"smitpidurkar12@gmail.com", "Smit@123"}
        };
    }

    @Test(dataProvider = "validLoginData")
    public void logoutTest(String email, String password) {

        LoginPage login = new LoginPage(driver);
        login.clickLoginLink();
        login.login(email, password);

        handleSuccessFlow();
    }


    private void handleSuccessFlow() {

        // 1. Wait for URL to leave login page
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("login")
        ));

        // 2. Confirm dashboard URL
        wait.until(ExpectedConditions.urlContains("notes/app"));

        // 3. Wait for page JS to fully settle
        waitForPageLoad();

        // 4. Dismiss any ad/overlay that could block the logout button
        dismissOverlayIfPresent();

        // 5. Confirm logout button is visible on dashboard
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_BTN));
        System.out.println(" Dashboard loaded — Logout button visible");

        // 6. Perform logout
        LogoutPage logout = new LogoutPage(driver);
        logout.clickLogout();
        //control shifts to LogoutPage


        // 7. Confirm redirected back to landing/login area
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("login"),
                ExpectedConditions.visibilityOfElementLocated(LOGIN_LINK)
        ));

        Assert.assertTrue(                                              //if condition true then pass else error message is displayed
                driver.getCurrentUrl().contains("notes/app"),
                "Logout failed — unexpected URL: " + driver.getCurrentUrl()
        );

        System.out.println("[PASS] Logout successful");
    }


    //  Empty fields flow - if the fields are empty
    private void handleEmptyFieldsFlow() {
        Assert.assertTrue(
                driver.getCurrentUrl().contains("login"),
                "Expected to remain on login page for empty credentials"
        );
        System.out.println("[PASS] Stayed on login page for empty fields");
    }

    // Invalid credentials flow - if credentials are invalid
    private void handleInvalidCredentialsFlow(String user) {
        // Google vignette handling
        if (driver.getCurrentUrl().contains("google_vignette")) {
            System.out.println("[INFO] Google vignette detected");
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("login"));
        }
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(ERROR_TOAST),
                    ExpectedConditions.urlContains("login")));
            Assert.assertTrue(
                    driver.getCurrentUrl().contains("login"),
                    "Expected to remain on login page for invalid credentials"
            );
            System.out.println("[PASS] Invalid login handled correctly");
        } catch (Exception e) {
            Assert.fail(
                    "Expected invalid login behaviour not observed for: "
                            + user + " | " + e.getMessage()
            );
        }
    }

    //  Helpers

    private void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete")
        );
    }

    private void dismissOverlayIfPresent() {
        try {
            WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(OVERLAY_CLOSE));
            closeBtn.click();
            System.out.println("[INFO] Overlay dismissed");
        } catch (Exception ignored) {
            // No overlay — perfectly fine
        }
    }
}