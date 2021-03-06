package com.github.grishberg.tests;

import com.github.grishberg.tests.common.RunnerLogger;

import java.util.Map;

/**
 * Provides data for test command execution.
 */
public class TestRunnerContext {
    private final InstrumentalPluginExtension instrumentalInfo;
    private final Environment environment;
    private final Map<String, String> screenshotRelation;
    private final RunnerLogger logger;

    public TestRunnerContext(InstrumentalPluginExtension instrumentalInfo,
                             Environment environment,
                             Map<String, String> screenshotRelation,
                             RunnerLogger logger) {
        this.instrumentalInfo = instrumentalInfo;
        this.environment = environment;
        this.screenshotRelation = screenshotRelation;
        this.logger = logger;
    }

    public InstrumentalPluginExtension getInstrumentalInfo() {
        return instrumentalInfo;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Map<String, String> getScreenshotRelation() {
        return screenshotRelation;
    }

    public RunnerLogger getLogger() {
        return logger;
    }
}
