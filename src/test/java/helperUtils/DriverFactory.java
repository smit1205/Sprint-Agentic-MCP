package helperUtils;

import java.time.Duration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {


    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>(); // thread local is used for parallel testing
                                                                              //runs test in isolation
    public static WebDriver initializeBrowser(String browserName) {

        WebDriver webDriver;

        if (browserName.equalsIgnoreCase("chrome")) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-popup-blocking");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-features=InterestFeedContentSuggestions");
            options.addArguments("--disable-features=NotificationTriggers");

            webDriver = new ChromeDriver(options);
        } else if (browserName.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            webDriver = new FirefoxDriver(new FirefoxOptions());

        } else if (browserName.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            webDriver = new EdgeDriver(new EdgeOptions());

        } else {
            throw new RuntimeException("Invalid browser: " + browserName);
        }

        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigReader.getImplicitWaitfromConfig())
        );

        driver.set(webDriver); // binds to this thread thread1 - chromedriverA..
        return webDriver;
    }

    public static WebDriver getDriver() {
        return driver.get(); // returns this thread's driver
    }

    public static void exitBrowser() {
        WebDriver webDriver = driver.get();
        if (webDriver != null) {
            webDriver.quit();
            driver.remove();     //cleans thread slot Thread-1 runs Test A Thread-1 finishes Thread-1 reused for Test B
        }
    }
}