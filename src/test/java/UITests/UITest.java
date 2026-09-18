package UITests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("UI tests")
@Feature("Student registration form")
@Owner("Dima")
class UITest {

    private WebDriver driver;
    private WebDriverWait wait;
    private ChromeDriverService driverService;

    @BeforeEach
    void openBrowser() throws IOException {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--window-size=1920,1080",
                "--disable-gpu",
                "--disable-notifications"
        );

        driverService = new ChromeDriverService.Builder()
                .usingAnyFreePort()
                .build();
        driverService.start();

        // RemoteWebDriver keeps this basic form test on the WebDriver HTTP
        // protocol and does not open an unnecessary DevTools WebSocket.
        driver = new RemoteWebDriver(driverService.getUrl(), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void closeBrowser() {
        try {
            if (driver != null) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment(
                        "Browser state",
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        ".png"
                );
            }
        } finally {
            if (driver != null) {
                driver.quit();
            }
            if (driverService != null) {
                driverService.stop();
            }
        }
    }

    @Test
    @DisplayName("Форма регистрации открывается, заполняется и отправляется")
    @Description("Проверяем отправку обязательных полей и содержимое итогового окна")
    @Severity(SeverityLevel.CRITICAL)
    void submitPracticeForm() {
        Allure.step("Открыть страницу с формой", () ->
                driver.get("https://demoqa.com/automation-practice-form")
        );

        Allure.step("Заполнить обязательные поля", () -> {
            type(By.id("firstName"), "Valera");
            type(By.id("lastName"), "Vonychi");
            type(By.id("userEmail"), "valera@example.com");
            clickWithJavaScript(By.cssSelector("label[for='gender-radio-1']"));
            type(By.id("userNumber"), "8888888888");
            type(By.id("currentAddress"), "Moscow");
        });

        Allure.step("Отправить форму", () ->
                clickWithJavaScript(By.id("submit"))
        );

        Allure.step("Проверить результат отправки", () -> {
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.className("modal-content")
            ));
            String result = modal.getText();

            assertTrue(result.contains("Thanks for submitting the form"),
                    "Окно успешной отправки не появилось");
            assertTrue(result.contains("Valera Vonychi"),
                    "Имя отсутствует в результате");
            assertTrue(result.contains("valera@example.com"),
                    "Email отсутствует в результате");
            assertTrue(result.contains("8888888888"),
                    "Телефон отсутствует в результате");
        });
    }

    private void type(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(value);
    }

    private void clickWithJavaScript(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }
}
