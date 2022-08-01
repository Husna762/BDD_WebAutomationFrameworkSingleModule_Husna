package configuration.common;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import configuration.reporting.ExtentManager;
import configuration.reporting.ExtentTestManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class WebTestBaseBrowserSetup {
    //Create a driver
    public static WebDriver driver;

    // Credential for Cloud Environments
    // Temp Email for BrowserStack: figowas697@tebyy.com and password : test1234
    // Temp Email for SauceLabs: figowas697@tebyy.com and userName: figowas697 password : Test1234@
    // public static final String BROWSERSTACK_USERNAME = System.getenv("BROWSERSTACK_USERNAME");
    public static final String BROWSERSTACK_USERNAME = "demoh_0BjgNJ";
    public static final String BROWSERSTACK_ACCESS_KEY = "xtGabrvKsRAqLSCts2q5";

    // public static final String BROWSERSTACK_ACCESS_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
    public static final String BROWSERSTACK_URL = "https://" + BROWSERSTACK_USERNAME + ":" + BROWSERSTACK_ACCESS_KEY + "@hub-cloud.browserstack.com/wd/hub";


    public static final String SAUCELABS_USERNAME = "figowas697";
    public static final String SAUCELABS_ACCESS_KEY = "aaa01031-8ce5-4d92-94f1-83d745ad18f0";
    public static final String SAUCELABS_URL = "https://" + SAUCELABS_USERNAME + ":" + SAUCELABS_ACCESS_KEY + "@ondemand.us-west-1.saucelabs.com:443/wd/hub";
    //URL url = new URL("https://xodale3453:*****ca08@ondemand.us-west-1.saucelabs.com:443/wd/hub");


    /**
     * **************************************************
     * ********** Start Of Reporting Utilities **********
     * **************************************************
     * **************************************************
     */
    //ExtentReport
    public static ExtentReports extent;
    public static ExtentTest logger;

    @BeforeSuite
    public void extentSetup(ITestContext context) {
        ExtentManager.setOutputDirectory(context);
        extent = ExtentManager.getInstance();
    }

    @BeforeMethod
    public void startExtent(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName().toLowerCase();
        ExtentTestManager.startTest(method.getName());
        ExtentTestManager.getTest().assignCategory(className);
    }

    protected String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @AfterMethod
    public void afterEachTestMethod(ITestResult result) throws Exception {
        ExtentTestManager.getTest().getTest().setStartedTime(getTime(result.getStartMillis()));
        ExtentTestManager.getTest().getTest().setEndedTime(getTime(result.getEndMillis()));
        for (String group : result.getMethod().getGroups()) {
            ExtentTestManager.getTest().assignCategory(group);
        }
        if (result.getStatus() == 1) {
            ExtentTestManager.getTest().log(LogStatus.PASS, "Test Passed");
        } else if (result.getStatus() == 2) {
            //logger.log(LogStatus.FAIL, "Test Case Failed is " + result.getName());
            // logger.log(LogStatus.FAIL, "Test Case Failed is " + result.getThrowable());
            ExtentTestManager.getTest().log(LogStatus.FAIL, getStackTrace(result.getThrowable()));
            //We do pass the path captured by this method in to the extent reports using "logger.addScreenCapture" method.
            String screenshotPath = captureScreenShotWithPath(driver, result.getName());
            //To add it in the extent report
            //   logger.log(LogStatus.FAIL, logger.addScreenCapture(screenshotPath));

//            if (result.getStatus() == ITestResult.FAILURE) {
//                captureScreenShotWithPath(driver, result.getName());
//                logger.log(LogStatus.FAIL, logger.addScreenCapture(screenshotPath));
//            }

        } else if (result.getStatus() == 3) {
            ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
        }
        ExtentTestManager.endTest();
        extent.flush();

        // driver.close();
        //driver.quit();
        // ending test
        //endTest(logger) : It ends the current test and prepares to create HTML report
        extent.endTest(logger);
    }

    @AfterSuite
    public void generateReport() {
        extent.close();
    }

    private Date getTime(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.getTime();
    }


    public static String convertToString(String st) {
        String splitString = "";
        splitString = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(st), ' ');
        return splitString;
    }










    // Configuration purpose
    @BeforeTest
    public void setUpAutomation() throws InterruptedException {
        System.out.println("***************** Automation Started *******************");
        Thread.sleep(2000);
    }


    //@AfterTest()
    @AfterMethod
    public void tearDownAutomation() {
        //  driver.close();
        if (driver != null) {
            driver.quit();
        }
        System.out.println("***************** Automation End *******************");
    }


    @Parameters({"useCloudEnv", "cloudEnvName", "os", "osVersion", "browserName", "browserVersion", "url"})
    @BeforeMethod
    public void setUp(@Optional("false") boolean useCloudEnv, @Optional("sauceLabs") String cloudEnvName, @Optional("OS X") String os, @Optional("Big Sure") String osVersion, @Optional("firefox") String browserName, @Optional("100") String browserVersion, @Optional("https://www.google.com") String url) throws MalformedURLException, InterruptedException {
        if (useCloudEnv) {
            if (cloudEnvName.equalsIgnoreCase("browserStack")) {
                getCloudDriver(cloudEnvName, os, osVersion, browserName, browserVersion);
            }
        } else {
            getLocalDriver(os, browserName);
        }

        Thread.sleep(2000);
//        //navigate to jCPenny.com
//        driver = new ChromeDriver();
        Thread.sleep(2000);
        driver.get(url);
        getLog("Browser : " + browserName);
        Thread.sleep(2000);
        getLog("Url : " + url);
        Thread.sleep(2000);
        driver.manage().window().maximize();
        Thread.sleep(2000);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().deleteAllCookies();


    }

    public static void getLog(final String message) {
        Reporter.log(message, true);
    }


//Mahmud Vai

    public WebDriver getLocalDriver(String os, String browserName) throws InterruptedException {
        if (browserName.equalsIgnoreCase("chrome")) {
            if (os.equalsIgnoreCase("OS X")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/BrowserDriver/Mac/chromedriver");
            } else if (os.equalsIgnoreCase("windows")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/BrowserDriver/Windows/chromedriver.exe");
            }
            Thread.sleep(2000);
//            Navigate to JCpPenny.com
            driver = new ChromeDriver();
        } else if (browserName.equalsIgnoreCase("chrome-options")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-notifications");
            options.addArguments("--incognito");
            options.addArguments("--start-maximized");
            ChromeOptions capability = new ChromeOptions();
            capability.setCapability(ChromeOptions.CAPABILITY, options);
            if (os.equalsIgnoreCase("OS X")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/BrowserDriver/Mac/chromedriver");
            } else if (os.equalsIgnoreCase("windows")) {
                System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/BrowserDriver/Windows/chromedriver.exe");
            }
            driver = new ChromeDriver(options);
        } else if (browserName.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            //  options.setHeadless(true);
            options.addArguments("--start-maximized");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--private");
            FirefoxOptions capability = new FirefoxOptions();
            capability.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
            if (os.equalsIgnoreCase("OS X")) {
                System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + "/BrowserDriver/Mac/geckodriver");
            } else if (os.equalsIgnoreCase("windows")) {
                System.setProperty("webdriver.gecko.driver", System.getProperty("user.dir") + "/BrowserDriver/Windows/geckodriver.exe");
            }
            driver = new FirefoxDriver();
        } else if (browserName.equalsIgnoreCase("ie")) {
            if (os.equalsIgnoreCase("windows")) {
                System.setProperty("webdriver.ie.driver", System.getProperty("user.dir") + "/BrowserDriver/Windows/IEDriverServer.exe");
            }
            driver = new InternetExplorerDriver();
        } else if (browserName.equalsIgnoreCase("safari")) {
            if (os.equalsIgnoreCase("OS X")) {
                System.setProperty("webdriver.safari.driver", System.getProperty("user.dir") + "/BrowserDriver/Mac/safaridriver");
            }
        }

        return driver;
    }


    // https://automate.browserstack.com/dashboard/v2/quick-start/integrate-test-suite-step#integrate-your-test-suite-with-browserstack
    // https://app.saucelabs.com/platform-configurator
    public WebDriver getCloudDriver(String envName, String os, String osVersion, String browserName, String browserVersion) throws MalformedURLException {
        // Add the following capabilities to your test script
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", browserName);
        capabilities.setCapability("browserVersion", browserVersion);
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put("os", os);
        options.put("osVersion", osVersion);
        if (envName.equalsIgnoreCase("browserStack")) {
            // capabilities.setCapability("resolution", "1024x786");
            capabilities.setCapability("bstack:options", options);
            driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), capabilities);
        } else if (envName.equalsIgnoreCase("sauceLabs")) {
            capabilities.setCapability("sauce:options", options);
            driver = new RemoteWebDriver(new URL(SAUCELABS_URL), capabilities);
        }
        return driver;
    }

    public static String captureScreenShotWithPath(WebDriver driver, String screenShotName) {
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
        File file = takesScreenshot.getScreenshotAs(OutputType.FILE);
        String fileName = System.getProperty("user.dir") + "/ScreenShots/" + screenShotName + "_" + dateName + ".png";
        try {
            FileUtils.copyFile(file, new File(fileName));
            getLog("ScreenShot Captured");
        } catch (IOException e) {
            getLog("Exception while taking ScreenShot " + e.getMessage());
        }
        return fileName;
    }


    public void waitFor(int seconds) throws InterruptedException {
        Thread.sleep(1000 * seconds);
    }



}