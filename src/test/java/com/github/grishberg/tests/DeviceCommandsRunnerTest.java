package com.github.grishberg.tests;

import com.github.grishberg.tests.commands.DeviceCommandResult;
import com.github.grishberg.tests.commands.DeviceRunnerCommand;
import com.github.grishberg.tests.commands.DeviceRunnerCommandProvider;
import com.github.grishberg.tests.commands.ExecuteCommandException;
import com.github.grishberg.tests.common.RunnerLogger;
import com.github.grishberg.tests.exceptions.ProcessCrashedException;
import com.github.grishberg.tests.planner.InstrumentalTestPlanProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by grishberg on 27.03.18.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceCommandsRunnerTest {
    @Mock
    InstrumentalTestPlanProvider planProvider;
    @Mock
    DeviceRunnerCommandProvider commandProvider;
    @Mock
    Environment environment;
    @Mock
    RunnerLogger logger;
    @Mock
    ConnectedDeviceWrapper deviceWrapper;
    @Mock
    DeviceRunnerCommand command;
    @Mock
    DeviceCommandResult result;
    @Mock
    TestRunnerContext context;
    private List<DeviceRunnerCommand> commands;
    private ConnectedDeviceWrapper[] devices;
    private DeviceCommandsRunner runner;

    @Before
    public void setUp() throws Exception {
        commands = new ArrayList<>();
        commands.add(command);
        when(context.getLogger()).thenReturn(logger);
        when(context.getEnvironment()).thenReturn(environment);
        when(commandProvider.provideCommandsForDevice(deviceWrapper, planProvider, environment))
                .thenReturn(commands);
        when(command.execute(deviceWrapper, context)).thenReturn(result);
        devices = new ConnectedDeviceWrapper[1];
        devices[0] = deviceWrapper;
        runner = new DeviceCommandsRunner(planProvider, commandProvider);
    }

    @Test
    public void runCommands() throws Exception {
        Assert.assertTrue(runner.runCommands(devices, context));
        verify(command).execute(deviceWrapper, context);
    }

    @Test
    public void runCommandsReturnFalseWhenHasFailedTests() throws Exception {
        when(result.isFailed()).thenReturn(true);
        Assert.assertFalse(runner.runCommands(devices, context));
        verify(command).execute(deviceWrapper, context);
    }

    @Test(expected = ExecuteCommandException.class)
    public void logErrorWhenException() throws Exception {
        ExecuteCommandException exception = new ExecuteCommandException("Exception", new Throwable());
        when(command.execute(deviceWrapper, context))
                .thenThrow(exception);
        runner.runCommands(devices, context);
        verify(logger).e("DCR", "Execute command exception:", exception);
    }

    @Test(expected = ExecuteCommandException.class)
    public void throwExecuteCommandExceptionWhenOtherException() throws Exception {
        NullPointerException exception = new NullPointerException();
        when(command.execute(deviceWrapper, context))
                .thenThrow(exception);
        runner.runCommands(devices, context);
        verify(logger).e("DCR", "Execute command exception:", exception);
    }

    @Test(expected = ProcessCrashedException.class)
    public void throwProcessCrashedExceptionWhenProcessCrashed() throws Exception {
        ProcessCrashedException exception = new ProcessCrashedException("test process crashed");
        when(command.execute(deviceWrapper, context))
                .thenThrow(exception);
        runner.runCommands(devices, context);
        verify(logger).e("DCR", "Execute command exception:", exception);
    }
}