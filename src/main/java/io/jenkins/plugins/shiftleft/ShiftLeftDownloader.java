package io.jenkins.plugins.shiftleft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class ShiftLeftDownloader {
    private final PrintStream logger;

    public ShiftLeftDownloader(PrintStream logger) {
        this.logger = logger;
    }

    public void downloadAgent(String uri, String savePath, String authToken) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) new URL(uri).openConnection();
        httpConn.setRequestProperty("Authorization", "Bearer " + authToken);
        httpConn.setRequestMethod("GET");
        try (InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(savePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            logger.println("[❌] Error downloading ShiftLeft CLI: " + e);
        } finally {
            httpConn.disconnect();
        }
    }

    public void setFilePermissions(Path agentPath) throws IOException {
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-xr-x");
        Files.setPosixFilePermissions(agentPath, permissions);
    }
}
