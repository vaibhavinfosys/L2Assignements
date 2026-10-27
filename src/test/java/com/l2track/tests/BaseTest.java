
package com.l2track.tests;

import com.l2track.core.ConfigReader;
import com.l2track.core.DriverFactory;
import com.l2track.utils.ZipUtils;
import com.l2track.utils.ExcelUtils;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseTest {
	private static final Logger logger =
	        LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser) {
    	
    	logger.info("========== Test setup started ==========");
    	logger.info("Requested browser: {}", browser);

        try {
            String downloadDir = ConfigReader.get("download.dir");

            if (downloadDir == null || downloadDir.trim().isEmpty()) {
                throw new IllegalStateException(
                    "download.dir is missing in config.properties");
            }

            Path downloadPath = Paths.get(downloadDir)
                    .toAbsolutePath().normalize();

            Files.createDirectories(downloadPath);

            Path testDataDir =
                    Paths.get("src/test/resources/testdata");

            Files.createDirectories(testDataDir);

            ZipUtils.ensureZipContainsTestFiles(
                    testDataDir.resolve("sample.zip"), testDataDir);

            ExcelUtils.ensureSampleExcel(
                    testDataDir.resolve("testdata.xlsx"));

            DriverFactory.initDriver(
                    downloadPath.toString(), browser);

            driver = DriverFactory.getDriver();
            logger.info("Browser launched successfully: {}", browser);

            String baseUrl = ConfigReader.get("base.url");

            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                throw new IllegalStateException(
                    "base.url is missing in config.properties");
            }

            driver.manage().window().maximize();
            driver.get(baseUrl);
            logger.info("Navigated to URL: {}", baseUrl);

            System.out.println("Started browser: " + browser);

        } catch (Exception e) {
            DriverFactory.quitDriver();
            driver = null;
            logger.error("Test setup failed for browser: {}", browser, e);
            throw new RuntimeException(
                "Test setup failed for browser: " + browser, e);
            
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        try {
        	logger.info("Closing browser");
            DriverFactory.quitDriver();
        } finally {
            driver = null;
        }
    }
}
