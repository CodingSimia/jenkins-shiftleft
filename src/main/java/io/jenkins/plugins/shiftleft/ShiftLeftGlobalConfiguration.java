package io.jenkins.plugins.shiftleft;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

@Extension
public class ShiftLeftGlobalConfiguration extends GlobalConfiguration {

    private String dockerUser;
    private Secret dockerPassword;
    private String upwindClientId;
    private Secret upwindClientSecret;
    private String upwindUri;

    public ShiftLeftGlobalConfiguration() {
        load();
    }

    public String getUpwindClientId() {
        return upwindClientId;
    }

    @DataBoundSetter
    public void setUpwindClientId(String upwindClientId) {
        this.upwindClientId = upwindClientId;
        save();
    }

    public Secret getUpwindClientSecret() {
        return upwindClientSecret;
    }

    @DataBoundSetter
    public void setUpwindClientSecret(Secret upwindClientSecret) {
        this.upwindClientSecret = upwindClientSecret;
        save();
    }

    public FormValidation doCheckUpwindClientId(@QueryParameter String upwindClientId) {
        if (upwindClientId == null || upwindClientId.isEmpty()) {
            return FormValidation.error("Upwind ClientId is required.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckUpwindClientSecret(@QueryParameter Secret upwindClientSecret) {
        if (upwindClientSecret == null || upwindClientSecret.getPlainText().isEmpty()) {
            return FormValidation.error("Upwind Client Secret is required.");
        }
        return FormValidation.ok();
    }

    public FormValidation doCheckUpwindUri(@QueryParameter String upwindUri) {
        if (upwindUri == null || upwindUri.isEmpty()) {
            return FormValidation.error("Upwind Uri is required.");
        }
        return FormValidation.ok();
    }

    public static ShiftLeftGlobalConfiguration get() {
        return GlobalConfiguration.all().get(ShiftLeftGlobalConfiguration.class);
    }
}
