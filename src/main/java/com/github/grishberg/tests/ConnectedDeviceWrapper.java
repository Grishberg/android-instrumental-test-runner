package com.github.grishberg.tests;

import com.android.ddmlib.*;
import com.android.utils.ILogger;
import com.github.grishberg.tests.commands.ExecuteCommandException;
import com.github.grishberg.tests.exceptions.PullCoverageException;
import org.gradle.internal.impldep.org.apache.maven.wagon.CommandExecutionException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Wraps {@link IDevice} interface.
 */
public class ConnectedDeviceWrapper implements IShellEnabledDevice, DeviceShellExecuter {
    public static final String COVERAGE_FILE_NAME = "coverage.ec";
    private final IDevice device;
    private String name;

    public ConnectedDeviceWrapper(IDevice device) {
        this.device = device;
    }

    @Override
    public void executeShellCommand(String command,
                                    IShellOutputReceiver receiver,
                                    long maxTimeToOutputResponse,
                                    TimeUnit maxTimeUnits) throws TimeoutException,
            AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits);
    }

    @Override
    public Future<String> getSystemProperty(String name) {
        return device.getSystemProperty(name);
    }

    @Override
    public String getName() {
        if (name == null) {
            name = device.getAvdName();
        }
        return name;
    }

    public IDevice getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return "ConnectedDeviceWrapper{" +
                "sn=" + device.getSerialNumber() +
                ", isOnline=" + device.isOnline() +
                ", name='" + getName() + '\'' +
                '}';
    }

    @Override
    public void pullFile(String temporaryCoverageCopy, String path) throws ExecuteCommandException {
        try {
            device.pullFile(temporaryCoverageCopy, path);
        } catch (Exception e) {
            throw new ExecuteCommandException("pullFile exception:", e);
        }
    }

    public boolean isEmulator() {
        return device.isEmulator();
    }

    public String getSerialNumber() {
        return device.getSerialNumber();
    }

    public void installPackage(String absolutePath, boolean reinstall, String extraArgument)
            throws InstallException {
        device.installPackage(absolutePath, reinstall, extraArgument);
    }

    /**
     * Pulls coverage file from device.
     *
     * @param instrumentationInfo plugin extension with instrumentation info.
     * @param coverageFilePrefix  prefix for generating coverage on local dir.
     * @param coverageFile        full path to coverage file on target device.
     * @param outCoverageDir      local dir, where coverage file will be copied.
     * @param logger              logger.
     * @throws PullCoverageException
     */
    public void pullCoverageFile(InstrumentalPluginExtension instrumentationInfo,
                                 String coverageFilePrefix,
                                 String coverageFile,
                                 File outCoverageDir,
                                 final ILogger logger) throws PullCoverageException {
        MultiLineReceiver outputReceiver = new MultilineLoggerReceiver(logger);

        logger.verbose("ConnectedDeviceWrapper '%s': fetching coverage data from %s",
                getName(), coverageFile);
        try {
            String temporaryCoverageCopy = "/data/local/tmp/" + instrumentationInfo.getApplicationId()
                    + "." + COVERAGE_FILE_NAME;
            executeShellCommand("run-as " + instrumentationInfo.getApplicationId() +
                            " cat " + coverageFile + " | cat > " + temporaryCoverageCopy,
                    outputReceiver,
                    2L, TimeUnit.MINUTES);
            pullFile(temporaryCoverageCopy,
                    (new File(outCoverageDir, coverageFilePrefix + "-" + COVERAGE_FILE_NAME))
                            .getPath());
            executeShellCommand("rm " + temporaryCoverageCopy, outputReceiver, 30L,
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new PullCoverageException(e);
        }
    }

    @Override
    public void executeShellCommand(String command, IShellOutputReceiver receiver,
                                    long maxTimeout, long maxTimeToOutputResponse,
                                    TimeUnit maxTimeUnits) throws TimeoutException,
            AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver, maxTimeout, maxTimeToOutputResponse, maxTimeUnits);
    }

    /**
     * Execute adb shell command on device.
     *
     * @param command adb shell command for execution.
     * @throws CommandExecutionException
     */
    public void executeShellCommand(String command) throws ExecuteCommandException {
        try {
            executeShellCommand(command, new CollectingOutputReceiver(), 5L, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new ExecuteCommandException("executeShellCommand exception:", e);
        }
    }

    @Override
    public String executeShellCommandAndReturnOutput(String command) throws ExecuteCommandException {
        try {
            CollectingOutputReceiver receiver = new CollectingOutputReceiver();
            executeShellCommand(command, receiver, 5L, TimeUnit.MINUTES);
            return receiver.getOutput();
        } catch (Exception e) {
            throw new ExecuteCommandException("executeShellCommand exception:", e);
        }
    }

    private class MultilineLoggerReceiver extends MultiLineReceiver {
        private final ILogger logger;

        MultilineLoggerReceiver(ILogger logger) {
            this.logger = logger;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                logger.verbose(line);
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
