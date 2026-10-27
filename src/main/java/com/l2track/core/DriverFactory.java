
package com.l2track.core;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driver =
            new ThreadLocal<>();

    public static void initDriver(
            String downloadDir, String browserName) {

        String browser = browserName == null
                ? "chrome"
                : browserName.trim().toLowerCase();

        try {
            Path downloadPath = Paths.get(downloadDir)
                    .toAbsolutePath().normalize();

            Files.createDirectories(downloadPath);

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory",
                    downloadPath.toString());
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);

            switch (browser) {

                case "chrome": {
                    WebDriverManager.chromedriver().setup();

                    ChromeOptions options = new ChromeOptions();
                    options.setExperimentalOption("prefs", prefs);
                    options.addArguments("--disable-notifications");

                    driver.set(new ChromeDriver(options));
                    break;
                }

                case "edge": {
                    WebDriverManager.edgedriver().setup();

                    EdgeOptions options = new EdgeOptions();
                    options.setExperimentalOption("prefs", prefs);
                    options.addArguments("--disable-notifications");

                    driver.set(new EdgeDriver(options));
                    break;
                }

                default:
                    throw new IllegalArgumentException(
                            "Unsupported browser: " + browser
                            + ". Use chrome or edge.");
            }

            System.out.println("Requested browser: " + browser);
            System.out.println("Driver class: "
                    + driver.get().getClass().getSimpleName());

        } catch (Exception e) {
            quitDriver();
            throw new RuntimeException(
                    "Unable to start browser: " + browser, e);
        }
    }

    public static WebDriver getDriver() {
        WebDriver currentDriver = driver.get();

        if (currentDriver == null) {
            throw new IllegalStateException(
                    "WebDriver is not initialized.");
        }

        return currentDriver;
    }

    public static void quitDriver() {
        WebDriver currentDriver = driver.get();

        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } finally {
                driver.remove();
            }
        }
    }
}
