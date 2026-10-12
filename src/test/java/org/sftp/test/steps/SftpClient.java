package org.sftp.test.steps;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SftpClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private SSHClient sshClient;
    private SFTPClient sftpClient;

    public SftpClient() {

        this.host = getRequiredProperty("SFTP_HOST");
        this.port = Integer.parseInt(
                System.getenv().getOrDefault("SFTP_PORT", "22")
        );
        this.username = getRequiredProperty("SFTP_USERNAME");
        this.password = getRequiredProperty("SFTP_PASSWORD");
    }

    public void connect() throws IOException {

        sshClient = new SSHClient();

        sshClient.loadKnownHosts();

        sshClient.connect(host, port);

        sshClient.authPassword(
                username,
                password
        );

        sftpClient = sshClient.newSFTPClient();
    }

    public void upload(
            Path localFile,
            String remoteDirectory,
            String remoteFileName) throws IOException {

        String remotePath =
                remoteDirectory + "/" + remoteFileName;

        sftpClient.put(
                localFile.toString(),
                remotePath
        );
    }

    public boolean exists(String remotePath) throws IOException {

        return sftpClient.statExistence(remotePath) != null;
    }

    public long getFileSize(String remotePath) throws IOException {

        return sftpClient.stat(remotePath).getSize();
    }

    public byte[] download(String remotePath) throws IOException {

        Path temporaryFile = Files.createTempFile(
                "sftp-test-",
                ".tmp"
        );

        try {

            sftpClient.get(
                    remotePath,
                    temporaryFile.toString()
            );

            return Files.readAllBytes(temporaryFile);

        } finally {

            Files.deleteIfExists(temporaryFile);
        }
    }

    public void delete(String remotePath) throws IOException {

        if (exists(remotePath)) {
            sftpClient.rm(remotePath);
        }
    }

    @Override
    public void close() throws IOException {

        if (sftpClient != null) {
            sftpClient.close();
        }

        if (sshClient != null) {
            sshClient.disconnect();
        }
    }

    private String getRequiredProperty(String name) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "La variable de entorno " + name + " no está configurada"
            );
        }

        return value;
    }
}