package com.github.grishberg.tests;

import com.github.grishberg.tests.commands.DeviceCommand;
import com.github.grishberg.tests.commands.DeviceCommandProvider;
import com.github.grishberg.tests.commands.InstrumentalTestCommand;
import com.github.grishberg.tests.planner.InstrumentalTestPlanProvider;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides one command for all instrumental tests for device.
 */
public class AllTestsInOneScopeCommandProvider implements DeviceCommandProvider {
    private final Project project;
    private final InstrumentationInfo instrumentationInfo;
    private final InstrumentationArgsProvider argsProvider;

    public AllTestsInOneScopeCommandProvider(Project project,
                                             InstrumentationInfo instrumentalInfo,
                                             InstrumentationArgsProvider argsProvider) {
        this.project = project;
        this.instrumentationInfo = instrumentalInfo;
        this.argsProvider = argsProvider;
    }

    @Override
    public DeviceCommand[] provideDeviceCommands(DeviceWrapper device,
                                                 InstrumentalTestPlanProvider testPlanProvider) {
        List<DeviceCommand> commands = new ArrayList<>();
        Map<String, String> instrumentalArgs = argsProvider.provideInstrumentationArgs(device);
        project.getLogger().debug("[AITR] device={}, args={}",
                device.toString(), instrumentalArgs);

            commands.add(new InstrumentalTestCommand(project,
                    instrumentationInfo,
                    instrumentalArgs));
        return commands.toArray(new DeviceCommand[commands.size()]);
    }
}