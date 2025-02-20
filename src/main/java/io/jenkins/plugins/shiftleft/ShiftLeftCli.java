package io.jenkins.plugins.shiftleft;

import hudson.remoting.RemoteOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jenkins.security.MasterToSlaveCallable;

public class ShiftLeftCli extends MasterToSlaveCallable<Void, IOException> {
    private final String authToken;
    private final String cliUrl;
    private final String cliSavePath;
    private final String[] cliCmd;
    private final RemoteOutputStream remoteLogger;

    public ShiftLeftCli(
            String authToken, String cliUrl, String cliSavePath, String[] cliCmd, RemoteOutputStream logger) {
        this.authToken = authToken;
        this.cliUrl = cliUrl;
        this.cliSavePath = cliSavePath;
        this.cliCmd = cliCmd;
        this.remoteLogger = logger;
    }

    @Override
    public Void call() throws IOException {
        PrintStream logger = new PrintStream(remoteLogger, true, StandardCharsets.UTF_8);
        // Set up the shift left cli
        ShiftLeftDownloader downloader = new ShiftLeftDownloader(logger);
        downloader.downloadAgent(cliUrl, cliSavePath, authToken);
        Path cliPath = Path.of(cliSavePath);
        downloader.setFilePermissions(cliPath);

        try {
            ProcessBuilder cli = new ProcessBuilder(cliCmd);
            Process cliProcess = cli.start();

            // Capture output and error streams
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(cliProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.println(line);
                    }
                } catch (IOException e) {
                    logger.println("[❌] Thread exception: " + e);
                }
            });

            // upwind-agent always writes to stderr
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(cliProcess.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.println(line);
                    }

                } catch (IOException e) {
                    logger.println("[❌] Thread exception: " + e);
                }
            });

            // Start threads to capture output and errors
            outputThread.start();
            errorThread.start();

            // Wait for the process to finish
            cliProcess.waitFor();

            if (cliProcess.exitValue() != 0) {
                logger.println("[❌] Shift Left CLI failed with exit code: " + cliProcess.exitValue());
            }

            // Wait for threads to finish
            outputThread.join();
            errorThread.join();
        } catch (IOException | InterruptedException e) {
            logger.println("[❌] Exception: " + e);
        } finally {
            Files.delete(cliPath);
        }

        return null;
    }
}
