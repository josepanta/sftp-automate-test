package org.sftp.test.sftp;

import org.sftp.test.context.TestContext;
import org.sftp.test.steps.SftpClient;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SftpSteps {

    private final TestContext context;

    private SftpClient sftpClient;

    public SftpSteps(TestContext context) {
        this.context = context;
    }

    @Before
    public void beforeScenario() throws IOException {

        sftpClient = new SftpClient();

        sftpClient.connect();
    }

    @After
    public void afterScenario() throws IOException {

        if (sftpClient != null) {
            sftpClient.close();
        }
    }

    @Given("tengo el archivo de entrada {string}")
    public void tengoElArchivoDeEntrada(String file) {

        Path path = Path.of(
                "src",
                "test",
                "resources",
                "test-data",
                file
        );

        assertThat(path)
                .exists()
                .isRegularFile();

        context.setInputFile(path);
    }

    @When("dejo el archivo en la ruta SFTP de entrada {string}")
    public void dejoElArchivoEnLaRutaSftpDeEntrada(
            String remoteDirectory) throws IOException {

        Path inputFile = context.getInputFile();

        String fileName =
                inputFile.getFileName().toString();

        sftpClient.upload(
                inputFile,
                remoteDirectory,
                fileName
        );
    }

    @Then("espero que aparezca el archivo {string} en la ruta SFTP {string}")
    public void esperoQueAparezcaElArchivo(
            String fileName,
            String remoteDirectory) {

        String remotePath =
                remoteDirectory + "/" + fileName;

        await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> {

                    try {
                        return sftpClient.exists(remotePath);
                    } catch (IOException e) {
                        return false;
                    }
                });
    }

    @Then("el archivo de salida debe coincidir con {string}")
    public void elArchivoDebeCoincidir(
            String expectedFile) throws IOException {

        String expectedPath =
                "src/test/resources/test-data/" + expectedFile;

        Path expected =
                Path.of(expectedPath);

        assertThat(expected)
                .exists()
                .isRegularFile();

        String inputFileName =
                context.getInputFile()
                        .getFileName()
                        .toString();

        String outputFileName =
                inputFileName.replace(
                        ".txt",
                        "_response.txt"
                );

        String remoteOutput =
                "/output/" + outputFileName;

        byte[] actual =
                sftpClient.download(remoteOutput);

        byte[] expectedContent =
                java.nio.file.Files.readAllBytes(expected);

        assertThat(actual)
                .isEqualTo(expectedContent);
    }
}