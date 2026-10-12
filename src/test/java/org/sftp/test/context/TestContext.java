package org.sftp.test.context;

import java.nio.file.Path;

public class TestContext {

    private Path inputFile;

    public Path getInputFile() {
        return inputFile;
    }

    public void setInputFile(Path inputFile) {
        this.inputFile = inputFile;
    }
}