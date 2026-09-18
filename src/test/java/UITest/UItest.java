package UITest;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Epic("UI_test")
@Feature("Main")
@Owner("Dima")
public class UItest {

    public WebDriver driver;
    public WebDriverWait wait;

    @Test
    @DisplayName("Страница активешин практис форм открывается и заполняется")
    @Description("Проверяем заголовок страницы и наличие текста")
    @Severity(SeverityLevel.CRITICAL)
    public void setUp() throws InterruptedException {

        // Настройка ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        options.addArguments("--window-size=1920,1080", "--disable-gpu", "--disable-notifications");
        // options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get("https://demoqa.com/automation-practice-form");

        // Явное ожидание: 20 секунд
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Ждём появления формы на странице
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userForm")));

//Name
        WebElement firstname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName")));
        firstname.sendKeys("Valera");
        WebElement lastname = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lastName")));
        lastname.sendKeys("Vonychi");

//Email
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userEmail")));
        email.sendKeys("dfgsgsg@sgsdgas.com");

//Gender(radio) — оставляем только один, иначе перекрывают друг друга
        WebElement genderMale = wait.until(ExpectedConditions.elementToBeClickable(By.id("gender-radio-1")));
        genderMale.click();

//Mobile
        WebElement phoneNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userNumber")));
        phoneNumber.sendKeys("8888888888");

//DateOfBirth
        WebElement date = wait.until(ExpectedConditions.elementToBeClickable(By.id("dateOfBirthInput")));
        date.click();
        WebElement dateMonth = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("react-datepicker__month-select")));
        Select selectMonth = new Select(dateMonth);
        selectMonth.selectByVisibleText("May");
        WebElement dateYear = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("react-datepicker__year-select")));
        Select selectYear = new Select(dateYear);
        selectYear.selectByVisibleText("2026");
        WebElement dateDay = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'react-datepicker__day--020')]")));
        dateDay.click();

//Subjects
        WebElement subjects = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("subjectsInput")));
        subjects.sendKeys("Maths");
        subjects.sendKeys(Keys.ENTER);

//Hobbies — оставляем один
        WebElement sports = wait.until(ExpectedConditions.elementToBeClickable(By.id("hobbies-checkbox-1")));
        sports.click();

//Picture
        WebElement picture = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("uploadPicture")));
        picture.sendKeys("C:/Users/grigo/OneDrive/Изображения/Screenshots/Снимок экрана 2026-01-09 153805.png");

//Current Address
        WebElement address = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currentAddress")));
        address.sendKeys("dsafgaqegxrfh2T2dsfsaf");

//StateAndCity
        WebElement state0 = wait.until(ExpectedConditions.elementToBeClickable(By.id("state")));
        state0.click();
        WebElement ncr = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-select-3-option-0")));
        ncr.click();

        WebElement city00 = wait.until(ExpectedConditions.elementToBeClickable(By.id("city")));
        city00.click();
        WebElement delhi = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-select-4-option-0")));
        delhi.click();

//Submit
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
        submit.click();
    }
}