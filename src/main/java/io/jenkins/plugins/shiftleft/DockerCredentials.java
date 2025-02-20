package io.jenkins.plugins.shiftleft;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class DockerCredentials {
    private String dockerUsername;
    private Secret dockerPassword;

    @DataBoundConstructor
    public DockerCredentials(String dockerUsername, Secret dockerPassword) {
        this.dockerUsername = dockerUsername;
        this.dockerPassword = dockerPassword;
    }

    public String getDockerUsername() {
        return dockerUsername;
    }

    @DataBoundSetter
    public void setDockerUsername(String dockerUsername) {
        this.dockerUsername = dockerUsername;
    }

    public Secret getDockerPassword() {
        return dockerPassword;
    }

    @DataBoundSetter
    public void setDockerPassword(Secret dockerPassword) {
        this.dockerPassword = dockerPassword;
    }
}
