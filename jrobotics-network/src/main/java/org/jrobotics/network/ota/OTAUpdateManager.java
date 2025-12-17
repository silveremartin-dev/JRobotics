/*
 * JRobotics v2 - World-Class General Purpose Robotics API
 * 
 * Copyright (c) 2025 Silvère Martin-Michiellot <silvere.martin@gmail.com>
 * Co-authored by Gemini AI Assistant
 * 
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */
package org.jrobotics.network.ota;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

/**
 * Over-The-Air (OTA) Update Manager for robot firmware.
 *
 * <p>
 * Manages firmware updates including version checking, downloading,
 * verification, and installation coordination.
 * </p>
 *
 * <p>
 * <b>Features:</b>
 * </p>
 * <ul>
 * <li>Version comparison and update detection</li>
 * <li>Secure download with checksum verification</li>
 * <li>Atomic update installation</li>
 * <li>Rollback support</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 * @version 2.0.0
 * @since 2.6.0
 */
public class OTAUpdateManager {

    private static final Logger logger = LoggerFactory.getLogger(OTAUpdateManager.class);

    private final String currentVersion;
    private final String updateServerUrl;
    private final Path firmwareDirectory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile UpdateStatus status = UpdateStatus.IDLE;
    private volatile double downloadProgress = 0;
    private UpdateListener listener;

    /**
     * Creates an OTA update manager.
     *
     * @param currentVersion    current firmware version
     * @param updateServerUrl   URL of the update server
     * @param firmwareDirectory local directory for firmware files
     */
    public OTAUpdateManager(String currentVersion, String updateServerUrl, Path firmwareDirectory) {
        this.currentVersion = currentVersion;
        this.updateServerUrl = updateServerUrl;
        this.firmwareDirectory = firmwareDirectory;
    }

    /**
     * Sets the update listener.
     *
     * @param listener callback for update events
     */
    public void setUpdateListener(UpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Checks for available updates.
     *
     * @return update info if available, null otherwise
     */
    public CompletableFuture<UpdateInfo> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                status = UpdateStatus.CHECKING;
                notifyListener("Checking for updates...");

                URL url = new URL(updateServerUrl + "/version.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    status = UpdateStatus.IDLE;
                    return null;
                }

                // Parse version info (simplified)
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Simple JSON parsing (in production, use Jackson)
                    String json = response.toString();
                    String version = extractJsonValue(json, "version");
                    String downloadUrl = extractJsonValue(json, "downloadUrl");
                    String checksum = extractJsonValue(json, "checksum");
                    String releaseNotes = extractJsonValue(json, "releaseNotes");

                    if (version != null && isNewerVersion(version, currentVersion)) {
                        UpdateInfo info = new UpdateInfo(version, downloadUrl, checksum, releaseNotes);
                        status = UpdateStatus.UPDATE_AVAILABLE;
                        notifyListener("Update available: " + version);
                        return info;
                    }
                }

                status = UpdateStatus.IDLE;
                notifyListener("No updates available");
                return null;

            } catch (IOException e) {
                logger.error("Failed to check for updates", e);
                status = UpdateStatus.ERROR;
                return null;
            }
        }, executor);
    }

    /**
     * Downloads and installs an update.
     *
     * @param updateInfo the update to install
     * @return future that completes when installation is done
     */
    public CompletableFuture<Boolean> downloadAndInstall(UpdateInfo updateInfo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Download
                status = UpdateStatus.DOWNLOADING;
                notifyListener("Downloading update " + updateInfo.getVersion());

                Path downloadPath = firmwareDirectory.resolve("update-" + updateInfo.getVersion() + ".bin");
                boolean downloaded = downloadFile(updateInfo.getDownloadUrl(), downloadPath);

                if (!downloaded) {
                    status = UpdateStatus.ERROR;
                    return false;
                }

                // Verify checksum
                status = UpdateStatus.VERIFYING;
                notifyListener("Verifying download...");

                if (!verifyChecksum(downloadPath, updateInfo.getChecksum())) {
                    logger.error("Checksum verification failed");
                    Files.deleteIfExists(downloadPath);
                    status = UpdateStatus.ERROR;
                    return false;
                }

                // Install
                status = UpdateStatus.INSTALLING;
                notifyListener("Installing update...");

                // Backup current firmware
                Path currentFirmware = firmwareDirectory.resolve("current.bin");
                Path backupFirmware = firmwareDirectory.resolve("backup.bin");
                if (Files.exists(currentFirmware)) {
                    Files.copy(currentFirmware, backupFirmware, StandardCopyOption.REPLACE_EXISTING);
                }

                // Install new firmware
                Files.move(downloadPath, currentFirmware, StandardCopyOption.REPLACE_EXISTING);

                status = UpdateStatus.COMPLETE;
                notifyListener("Update installed: " + updateInfo.getVersion());
                return true;

            } catch (IOException e) {
                logger.error("Update installation failed", e);
                status = UpdateStatus.ERROR;
                return false;
            }
        }, executor);
    }

    /**
     * Rolls back to the previous firmware version.
     *
     * @return true if rollback successful
     */
    public boolean rollback() {
        try {
            Path currentFirmware = firmwareDirectory.resolve("current.bin");
            Path backupFirmware = firmwareDirectory.resolve("backup.bin");

            if (!Files.exists(backupFirmware)) {
                logger.warn("No backup firmware available for rollback");
                return false;
            }

            Files.move(backupFirmware, currentFirmware, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Rollback completed");
            return true;

        } catch (IOException e) {
            logger.error("Rollback failed", e);
            return false;
        }
    }

    private boolean downloadFile(String urlStr, Path destination) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int contentLength = conn.getContentLength();

        try (InputStream in = conn.getInputStream();
                OutputStream out = Files.newOutputStream(destination)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalRead += bytesRead;

                if (contentLength > 0) {
                    downloadProgress = (double) totalRead / contentLength;
                }
            }
        }

        downloadProgress = 1.0;
        return true;
    }

    private boolean verifyChecksum(Path file, String expectedChecksum) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equalsIgnoreCase(expectedChecksum);

        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Checksum verification error", e);
            return false;
        }
    }

    private boolean isNewerVersion(String remote, String local) {
        // Simple semantic version comparison
        String[] remoteParts = remote.split("\\.");
        String[] localParts = local.split("\\.");

        for (int i = 0; i < Math.min(remoteParts.length, localParts.length); i++) {
            try {
                int r = Integer.parseInt(remoteParts[i]);
                int l = Integer.parseInt(localParts[i]);
                if (r > l)
                    return true;
                if (r < l)
                    return false;
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                int cmp = remoteParts[i].compareTo(localParts[i]);
                if (cmp != 0)
                    return cmp > 0;
            }
        }

        return remoteParts.length > localParts.length;
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1)
            return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1)
            return null;
        return json.substring(start, end);
    }

    private void notifyListener(String message) {
        if (listener != null) {
            listener.onStatusChange(status, message);
        }
    }

    public UpdateStatus getStatus() {
        return status;
    }

    public double getDownloadProgress() {
        return downloadProgress;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Update status enumeration.
     */
    public enum UpdateStatus {
        IDLE, CHECKING, UPDATE_AVAILABLE, DOWNLOADING, VERIFYING, INSTALLING, COMPLETE, ERROR
    }

    /**
     * Update information.
     */
    public static class UpdateInfo {
        private final String version;
        private final String downloadUrl;
        private final String checksum;
        private final String releaseNotes;

        public UpdateInfo(String version, String downloadUrl, String checksum, String releaseNotes) {
            this.version = version;
            this.downloadUrl = downloadUrl;
            this.checksum = checksum;
            this.releaseNotes = releaseNotes;
        }

        public String getVersion() {
            return version;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getChecksum() {
            return checksum;
        }

        public String getReleaseNotes() {
            return releaseNotes;
        }
    }

    /**
     * Listener for update events.
     */
    public interface UpdateListener {
        void onStatusChange(UpdateStatus status, String message);
    }
}
