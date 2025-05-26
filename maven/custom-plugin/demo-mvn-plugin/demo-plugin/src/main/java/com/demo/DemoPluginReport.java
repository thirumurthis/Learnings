package com.demo;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.util.Locale;

@Mojo(name="report", defaultPhase = LifecyclePhase.SITE, requiresProject = true)
public class DemoPluginReport extends AbstractMavenReport {

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {

    }

    @Override
    public String getOutputName() {
        return "Demo plugin";
    }

    @Override
    public String getName(Locale locale) {
        return "Demo plugin";
    }

    @Override
    public String getDescription(Locale locale) {
        return "Demo plugin report";
    }
}
