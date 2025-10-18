package com.alaka_ala.florafilm.ui.util.coreMatrix;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MatrixManager {

    private static final String TAG = "MatrixManager";
    private static final String BINARY_NAME = "torrserver";
    private static final String UPDATE_BINARY_NAME = "torrserver_update";
    private static final String LOG_FILE_NAME = "torrserver.log";

    // Static initializer to configure libsu
    static {
        // Set flags for non-root shell
        Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_NON_ROOT_SHELL));
    }

    /**
     * Callback interface for download progress.
     */
    public interface DownloadCallback {
        void onProgress(int progress);
        void onComplete(boolean success, String message);
    }

    /**
     * Downloads the server binary with progress reporting.
     * @param context The application context.
     * @param callback The callback to report progress and completion.
     */
    public static void downloadServer(Context context, DownloadCallback callback) {
        new Thread(() -> {
            try {
                // 1. Get the correct download URL
                String arch = getArch();
                String downloadUrl = getDownloadUrlForArch(arch);
                if (downloadUrl == null) {
                    throw new Exception("Unsupported architecture: " + arch);
                }

                File binaryFile = new File(context.getFilesDir(), BINARY_NAME);
                File updateFile = new File(context.getFilesDir(), UPDATE_BINARY_NAME);

                // 2. Download to a temporary file
                Log.d(TAG, "Downloading from: " + downloadUrl);
                downloadFile(downloadUrl, updateFile, callback);
                Log.d(TAG, "Download complete.");

                // 3. Atomically replace the old binary
                if (binaryFile.exists() && !binaryFile.delete()) {
                    Log.w(TAG, "Could not delete old binary file.");
                }
                if (!updateFile.renameTo(binaryFile)) {
                    throw new Exception("Failed to rename update file.");
                }

                callback.onComplete(true, "Download successful");

            } catch (Exception e) {
                Log.e(TAG, "Failed during downloadServer", e);
                callback.onComplete(false, e.getMessage());
            }
        }).start();
    }

    /**
     * Starts the server process.
     * @param context The application context.
     */
    public static void startServer(Context context) {
        try {
            File binaryFile = new File(context.getFilesDir(), BINARY_NAME);
            File logFile = new File(context.getFilesDir(), LOG_FILE_NAME);

            if (!binaryFile.exists()) {
                throw new Exception("Binary file does not exist, cannot start process.");
            }

            if (!binaryFile.canExecute() && !binaryFile.setExecutable(true)) {
                throw new Exception("Failed to set executable permission.");
            }

            // Construct the shell command
            String command = String.format(
                    "export GODEBUG=madvdontneed=1; %s --port=8090 --path=%s --logpath=%s 1>>%s 2>&1 &",
                    binaryFile.getAbsolutePath(),
                    context.getFilesDir().getAbsolutePath(),
                    logFile.getAbsolutePath(),
                    logFile.getAbsolutePath()
            );

            Log.d(TAG, "Executing command: " + command);
            Shell.sh(command).exec();
            Log.d(TAG, "Server start command executed.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start server", e);
        }
    }


    public static void stopServer() {
        Log.d(TAG, "Executing command: killall -9 " + BINARY_NAME);
        Shell.sh("killall -9 " + BINARY_NAME).exec();
        Log.d(TAG, "Server stop command executed.");
    }

    public static boolean isServerDownloaded(Context context) {
        File binaryFile = new File(context.getFilesDir(), BINARY_NAME);
        return binaryFile.exists();
    }

    /**
     * Checks if the server process is running.
     * This is a simple check and might not be 100% reliable.
     */
    public static boolean isServerRunning() {
        Shell.Result result = Shell.sh("pidof " + BINARY_NAME).exec();
        return result.isSuccess() && !result.getOut().isEmpty();
    }

    private static String getDownloadUrlForArch(String arch) {
        switch (arch) {
            case "arm64": return "https://github.com/YouROK/TorrServer/releases/download/MatriX.136/TorrServer-android-arm64";
            case "arm7": return "https://github.com/YouROK/TorrServer/releases/download/MatriX.136/TorrServer-android-arm7";
            case "amd64": return "https://github.com/YouROK/TorrServer/releases/download/MatriX.136/TorrServer-android-amd64";
            case "386": return "https://github.com/YouROK/TorrServer/releases/download/MatriX.136/TorrServer-android-386";
            default: return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static String getArch() {
        String arch = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
                Build.SUPPORTED_ABIS[0] : Build.CPU_ABI;

        switch (arch) {
            case "arm64-v8a": return "arm64";
            case "armeabi-v7a": return "arm7";
            case "x86_64": return "amd64";
            case "x86": return "386";
            default: return arch;
        }
    }

    private static void downloadFile(String urlString, File destination, DownloadCallback callback) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        int fileLength = connection.getContentLength();

        try (InputStream input = connection.getInputStream();
             OutputStream output = new FileOutputStream(destination)) {
            byte[] data = new byte[8192];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                if (fileLength > 0) {
                    callback.onProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
        } finally {
            connection.disconnect();
        }
    }
}
