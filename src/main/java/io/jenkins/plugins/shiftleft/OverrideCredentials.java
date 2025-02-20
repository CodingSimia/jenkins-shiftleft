package io.jenkins.plugins.shiftleft;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class OverrideCredentials {
    private String upwindClientId;
    private Secret upwindClientSecret;

    @DataBoundConstructor
    public OverrideCredentials(String upwindClientId, Secret upwindClientSecret) {
        this.upwindClientId = upwindClientId;
        this.upwindClientSecret = upwindClientSecret;
    }

    public String getUpwindClientId() {
        return upwindClientId;
    }

    @DataBoundSetter
    public void setUpwindClientId(String upwindClientId) {
        this.upwindClientId = upwindClientId;
    }

    public Secret getUpwindClientSecret() {
        return upwindClientSecret;
    }

    @DataBoundSetter
    public void setUpwindClientSecret(Secret upwindClientSecret) {
        this.upwindClientSecret = upwindClientSecret;
    }
}
