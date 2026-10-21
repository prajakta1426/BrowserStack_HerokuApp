import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;

public class AutomateHeroku {
    WebDriver driver;
    WebDriverWait wait;

    public static final String USERNAME =System.getenv("bs_username");
    public static final String ACCESS_KEY =System.getenv("bs_token");
    public static final String BROWSERSTACK_URL ="https://" + USERNAME + ":" + ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";

    public void markTestStatus(String status,String reason,WebDriver driver){
        JavascriptExecutor jse= (JavascriptExecutor) driver;
        jse.executeScript("browserstack_executor: " + "{\"action\": \"setSessionStatus\",\"arguments\":{\"status\":\""
                + status + "\",\"reason\": \""+reason +"\"}}");
    }

    @Parameters({"os","osVersion","browserName","browserVersion","deviceName","realMobile"})
    @BeforeMethod
    public void setup(@Optional("") String os, @Optional("") String osVersion,
                      @Optional("") String browserName, @Optional("") String browserVersion,
                      @Optional("") String deviceName, @Optional("") String realMobile)
            throws InterruptedException, MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        HashMap<String, Object> bstackOptions = new HashMap<>();

        bstackOptions.put("os",os);
        bstackOptions.put("osVersion",osVersion);

        bstackOptions.put("projectName", "CCST BrowserStack Demo");
        bstackOptions.put("buildName", "TestNG Build 1.0");
        bstackOptions.put("sessionName","CCST BrowserStack Demo");
        bstackOptions.put("deviceName",deviceName);
        bstackOptions.put("realMobile",realMobile);

        capabilities.setCapability("bstack:options",bstackOptions);
        capabilities.setCapability("browserName",browserName);
        capabilities.setCapability("browserVersion",browserVersion);

        driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL),capabilities);
        wait=new WebDriverWait(driver, Duration.ofSeconds(10));
        //driver.manage().window().maximize();
        Thread.sleep(1000);
    }

    @Test
    public void AddAndRemoveElementsValidation()
    {
        try {
            driver.get("https://the-internet.herokuapp.com/add_remove_elements/");
                Thread.sleep(2000);
            WebElement addElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Add Element']")));
            addElement.click();
            WebElement removeElement = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='Delete']")));
            Assert.assertTrue(removeElement.isDisplayed());
            removeElement.click();
            boolean isGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//button[text()='Delete']")));
            Assert.assertTrue(isGone, "Delete button should be removed from the page");
                markTestStatus("Passed", "Add And Remove Elements Validation successful.",driver);
        } catch (Exception e) {
            markTestStatus("Failed", "Add And Remove Elements Validation failed."+e.getMessage(),driver);
        }
    }

    @Test
    public void KeyPressesValidation() {
        try{
            driver.get("https://the-internet.herokuapp.com/key_presses");
                Thread.sleep(2000);

            WebElement target = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("target")));
            target.sendKeys("A");

            WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
            Assert.assertTrue(result.getText().contains("You entered: A"));
            markTestStatus("Passed", "Key Presses Validation successful.",driver);
         }
        catch (Exception e) {
        markTestStatus("Failed", "Key Presses Validation failed."+e.getMessage(),driver);
    }
    }

    @Test
    public void DragAndDropValidation() {
        try{
            driver.get("https://the-internet.herokuapp.com/drag_and_drop");
            Thread.sleep(2000);

            WebElement source = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("column-a")));
            WebElement target = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("column-b")));

            new Actions(driver)
                    .dragAndDrop(source, target)
                    .perform();
            Thread.sleep(1000);

            WebElement sourceHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='column-a']/header")));
            WebElement targetHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='column-b']/header")));

            Assert.assertEquals(sourceHeader.getText(), "B", "Column A should contain the source after drag and drop");
            Assert.assertEquals(targetHeader.getText(), "A", "Column B should contain the target after drag and drop");
                markTestStatus("Passed", "Drag And Drop Validation successful.",driver);
        } catch (Exception e) {
            markTestStatus("Failed", "Drag And Drop Validation failed."+e.getMessage(),driver);
        }
    }

    @Test
    public void DropdownValidation() {
        try{
            driver.get("https://the-internet.herokuapp.com/dropdown");
            Thread.sleep(2000);

            WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("dropdown")));
            Select select = new Select(dropdown);
            select.selectByVisibleText("Option 2");

            WebElement selectedOption = select.getFirstSelectedOption();
            Assert.assertEquals(selectedOption.getText(), "Option 2", "Expected dropdown value to be Option 2");
            markTestStatus("Passed", "Dropdown Validation successful.",driver);
    } catch (Exception e) {
        markTestStatus("Failed", "Dropdown Validation failed."+e.getMessage(),driver);
    }
    }

    @AfterMethod
    public void teardown() {
        if (driver != null)
            driver.quit();
    }
}
