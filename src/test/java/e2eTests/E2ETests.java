package e2eTests;

import base.BaseTest;
import helperUtils.ApiClient;
import helperUtils.DriverFactory;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.NotesPage;

import java.time.Duration;

public class E2ETests extends BaseTest {

    private WebDriver driver;
    private NotesPage notesPage;
    private ApiClient apiClient;
    private WebDriverWait wait;

    private static final String EMAIL = "smitpidurkar12@gmail.com";
    private static final String PASSWORD = "Smit@123";

    private static final By ADD_NOTE_BTN =
            By.cssSelector("[data-testid='add-new-note']");

    private static final By OVERLAY =
            By.cssSelector(".toast, .modal, .loading, .spinner");

    @BeforeMethod
    public void e2eSetup() throws Exception {

        driver = DriverFactory.getDriver();

        System.out.println("Current URL = " + driver.getCurrentUrl());

        wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        LoginPage login = new LoginPage(driver);
        login.clickLoginLink();
        login.login(EMAIL, PASSWORD);

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("notes"),
                ExpectedConditions.presenceOfElementLocated(ADD_NOTE_BTN)
        ));

        notesPage = new NotesPage(driver);

        String token = ApiClient.login(EMAIL, PASSWORD);
        apiClient = new ApiClient(token);
    }

    // TC-E2E-01

    @Test(description = "Note created via UI should appear in API GET response")
    public void TC_E2E_01_NoteCreatedInUIAppearsInAPI() throws Exception {

        String title = "E2E01-" + System.currentTimeMillis();
        String description = "Created via UI";
        String category = "Work";

        notesPage.addNote(title, description, category);

        JSONObject apiNote = apiClient.getNoteByTitle(title);

        Assert.assertNotNull(
                apiNote,
                "Note not found in API after UI creation"
        );
    }

    // TC-E2E-02

    @Test(description = "UI and API data match")
    public void TC_E2E_02_UIAndAPINoteDataMatch() throws Exception {

        String title = "E2E02-" + System.currentTimeMillis();
        String description = "Match test";
        String category = "Personal";

        notesPage.addNote(title, description, category);

        JSONObject apiNote = apiClient.getNoteByTitle(title);

        Assert.assertNotNull(apiNote, "Note not found in API");

        Assert.assertEquals(apiNote.getString("title"), title);
        Assert.assertEquals(apiNote.getString("description"), description);
        Assert.assertEquals(
                apiNote.getString("category").toLowerCase(),
                category.toLowerCase()
        );
    }

    // TC-E2E-03

    @Test(description = "Deleted via API should disappear from UI")
    public void TC_E2E_03_NoteDeletedViaAPIDisappearsFromUI() throws Exception {

        String title = "E2E03-" + System.currentTimeMillis();
        String description = "Delete test";
        String category = "Home";

        notesPage.addNote(title, description, category);

        JSONObject apiNote = apiClient.getNoteByTitle(title);

        Assert.assertNotNull(apiNote);

        String noteId = apiNote.getString("id");

        int status = apiClient.deleteNoteById(noteId);

        Assert.assertEquals(status, 200);

        driver.navigate().refresh();

        wait.until(ExpectedConditions.presenceOfElementLocated(ADD_NOTE_BTN));

        try {
            wait.until(
                    ExpectedConditions.invisibilityOfElementLocated(OVERLAY)
            );
        } catch (Exception ignored) {
        }

        Assert.assertTrue(notesPage.isNoteAbsent(title));
    }

    // TC-E2E-04

    @Test(description = "UI ID matches API ID")
    public void TC_E2E_04_NoteIDMatchesBetweenUIAndAPI() throws Exception {

        String title = "E2E04-" + System.currentTimeMillis();
        String description = "ID match";
        String category = "Work";

        notesPage.addNote(title, description, category);

        JSONObject apiNote = apiClient.getNoteByTitle(title);

        Assert.assertNotNull(apiNote);

        String apiId = apiNote.getString("id");
        String uiId = notesPage.getNoteIdFromUI(title);

        Assert.assertNotNull(uiId);

        Assert.assertEquals(uiId, apiId);
    }
}