package helperUtils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHelper {

    public static String captureScreenshot(WebDriver driver, String testName) {

        String timestamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());  //makes every screenshot unique

        String path =
                System.getProperty("user.dir")
                        + "/screenshots/"
                        + testName + "_"
                        + timestamp + ".png";

        File src =
                ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.FILE);

        try {
            FileUtils.copyFile(src, new File(path));        //Copies temporary screenshot file to screenshots/
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
}