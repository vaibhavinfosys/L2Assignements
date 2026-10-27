
package com.l2track.tests;

import com.l2track.pages.UploadPage;
import com.l2track.core.ConfigReader;
import com.l2track.dataproviders.ExcelDataProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUploadTest extends BaseTest {

    private static final Logger logger =
            LoggerFactory.getLogger(FileUploadTest.class);

    @Test(
        dataProvider = "uploadFiles",
        dataProviderClass = ExcelDataProvider.class,
        description = "Upload files driven by Excel test data"
    )
    public void testFileUpload(
            String relativePath,
            String expectedHeader) {

        logger.info("Starting file upload test");

        // Step 1: Validate Excel test data
        Assert.assertNotNull(
                relativePath,
                "File path from Excel must not be null"
        );

        Assert.assertFalse(
                relativePath.trim().isEmpty(),
                "File path from Excel must not be empty"
        );

        Assert.assertNotNull(
                expectedHeader,
                "Expected content from Excel must not be null"
        );

        // Step 2: Build the absolute file path
        String path = Paths.get(
                "src/test/resources",
                relativePath
        ).toAbsolutePath().normalize().toString();

        File file1 = new File(path);

        logger.info("File selected for upload: {}", file1.getName());

        // Step 3: Verify the file exists and is readable
        Assert.assertTrue(
                file1.exists(),
                "Test file does not exist: " + path
        );

        Assert.assertTrue(
                file1.isFile(),
                "Upload path must point to a file: " + path
        );

        Assert.assertTrue(
                file1.canRead(),
                "Test file is not readable: " + path
        );

        // Step 4: Validate the local file content
        try {
            String actualContent = Files.readString(
                    file1.toPath(),
                    StandardCharsets.UTF_8
            );

            Assert.assertTrue(
                    actualContent.contains(expectedHeader),
                    "Expected content was not found in: "
                            + file1.getName()
                            + ". Expected: " + expectedHeader
            );

        } catch (IOException e) {
            Assert.fail(
                    "Unable to read file: "
                            + file1.getAbsolutePath(),
                    e
            );
        }

        // Step 5: Read the application URL
        String baseUrl = ConfigReader.get("base.url");

        Assert.assertNotNull(
                baseUrl,
                "base.url is missing from config.properties"
        );

        // Step 6: Navigate to the upload page
        driver.get(baseUrl + "/upload");

        // Step 7: Upload the file
        UploadPage uploadPage = new UploadPage(driver);

        uploadPage.uploadFile(file1.getAbsolutePath());

        // Step 8: Get the uploaded filename
        String uploadedFileName =
                uploadPage.getUploadedFileName();

        // Step 9: Verify the upload result
        Assert.assertNotNull(
                uploadedFileName,
                "Uploaded filename was not displayed"
        );

        Assert.assertFalse(
                uploadedFileName.trim().isEmpty(),
                "Uploaded filename is empty"
        );

        // Step 10: Verify the correct filename
        Assert.assertTrue(
                uploadedFileName.contains(file1.getName()),
                "Expected filename: " + file1.getName()
                        + ", but displayed filename was: "
                        + uploadedFileName
        );

        logger.info(
                "File uploaded successfully: {}",
                file1.getName()
        );

        System.out.println(
                "PASSED: File uploaded and validated successfully: "
                        + file1.getName()
        );
    }
}
