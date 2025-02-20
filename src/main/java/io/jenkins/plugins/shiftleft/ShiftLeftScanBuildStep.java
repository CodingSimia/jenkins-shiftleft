package io.jenkins.plugins.shiftleft;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import hudson.util.Secret;
import java.io.*;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class ShiftLeftScanBuildStep extends hudson.tasks.Builder implements SimpleBuildStep {

    private static final String SHIFTLEFT_CLI_URL_TEMPLATE = "https://releases.%s/shiftleft/stable/shiftleft";

    private Boolean pullDockerImage;
    private DockerCredentials dockerCredentials;
    private String dockerImage;
    private OverrideCredentials overrideCredentials;
    private String upwindUri;
    private String commit;
    private String branch;
    private String repo;

    @DataBoundConstructor
    public ShiftLeftScanBuildStep(
            String dockerImage,
            OverrideCredentials overrideCredentials,
            String upwindUri,
            Boolean pullDockerImage,
            DockerCredentials dockerCredentials,
            String commit,
            String branch,
            String repo) {
        this.dockerImage = dockerImage;
        this.overrideCredentials = overrideCredentials;
        this.upwindUri = upwindUri;
        this.pullDockerImage = pullDockerImage;
        this.dockerCredentials = dockerCredentials;
        this.commit = commit;
        this.branch = branch;
        this.repo = repo;
    }

    // region public properties
    public DockerCredentials getDockerCredentials() {
        return dockerCredentials;
    }

    @DataBoundSetter
    public void setDockerCredentials(DockerCredentials dockerCredentials) {
        this.dockerCredentials = dockerCredentials;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    @DataBoundSetter
    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public Boolean getPullDockerImage() {
        return pullDockerImage;
    }

    @DataBoundSetter
    public void setPullDockerImage(Boolean pullDockerImage) {
        this.pullDockerImage = pullDockerImage;
    }

    public OverrideCredentials getOverrideCredentials() {
        return overrideCredentials;
    }

    @DataBoundSetter
    public void setOverrideCredentials(OverrideCredentials overrideCredentials) {
        this.overrideCredentials = overrideCredentials;
    }

    public String getUpwindUri() {
        return upwindUri;
    }

    @DataBoundSetter
    public void setUpwindUri(String upwindUri) {
        this.upwindUri = upwindUri;
    }

    public String getCommit() {
        return commit;
    }

    @DataBoundSetter
    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getBranch() {
        return branch;
    }

    @DataBoundSetter
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRepo() {
        return repo;
    }

    @DataBoundSetter
    public void setRepo(String repo) {
        this.repo = repo;
    }

    // endregion

    @Override
    public void perform(
            @Nonnull Run<?, ?> run,
            @Nonnull hudson.FilePath workspace,
            @Nonnull hudson.Launcher launcher,
            @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        // Fetch global config
        ShiftLeftGlobalConfiguration globalConfig = ShiftLeftGlobalConfiguration.get();

        // Fetch env vars
        EnvVars env = run.getEnvironment(listener);

        // Use overridden values if they are set; otherwise, fall back to global config
        // Use overridden values if they are set; otherwise, fall back to global config
        String upwindClientIdToUse = (overrideCredentials != null
                        && overrideCredentials.getUpwindClientId() != null
                        && !overrideCredentials.getUpwindClientId().isEmpty())
                ? overrideCredentials.getUpwindClientId()
                : globalConfig.getUpwindClientId();
        Secret upwindClientSecretToUse = (overrideCredentials != null
                        && overrideCredentials.getUpwindClientSecret() != null
                        && !overrideCredentials
                                .getUpwindClientSecret()
                                .getPlainText()
                                .isEmpty())
                ? overrideCredentials.getUpwindClientSecret()
                : globalConfig.getUpwindClientSecret();
        String upwindUriToUse = (upwindUri != null && !upwindUri.isEmpty()) ? upwindUri : "upwind.io";
        String commitToUse = (commit != null && !commit.isEmpty()) ? commit : env.getOrDefault("GIT_COMMIT", "unknown");
        String branchToUse = (branch != null && !branch.isEmpty()) ? branch : env.getOrDefault("GIT_BRANCH", "unknown");
        String repoToUse = (repo != null && !repo.isEmpty()) ? repo : env.getOrDefault("GIT_URL", "unknown");
        Boolean pullDockerImageToUse = pullDockerImage != null ? pullDockerImage : false;
        String dockerUserToUse = (dockerCredentials != null
                        && dockerCredentials.getDockerUsername() != null
                        && !dockerCredentials.getDockerUsername().isEmpty())
                ? dockerCredentials.getDockerUsername()
                : "";
        Secret dockerPasswordToUse = (dockerCredentials != null
                        && dockerCredentials.getDockerPassword() != null
                        && dockerCredentials.getDockerPassword().getPlainText().isEmpty())
                ? dockerCredentials.getDockerPassword()
                : Secret.fromString("");

        if (upwindClientIdToUse == null || upwindClientIdToUse.isEmpty()) {
            logger.println("[❌] Upwind Client ID is not set");
        }

        if (upwindClientSecretToUse == null
                || upwindClientSecretToUse.getPlainText().isEmpty()) {
            logger.println("[❌] Upwind Client Secret is not set");
        }

        if (dockerImage == null || dockerImage.isEmpty()) {
            logger.println("[❌] Docker Image is not set");
        }

        if ((upwindClientIdToUse == null || upwindClientIdToUse.isEmpty())
                || (upwindClientSecretToUse == null
                        || upwindClientSecretToUse.getPlainText().isEmpty())
                || (dockerImage == null || dockerImage.isEmpty())) {
            return;
        }

        String initiatorToUse = "non-user";
        Cause.UserIdCause userIdCause = run.getCause(Cause.UserIdCause.class);
        if (userIdCause != null) {
            String userId = userIdCause.getUserId();
            String userName = userIdCause.getUserName();
            initiatorToUse = String.format("%s (%s)", userName, userId);
        }

        // Log values used for this build
        logger.println("[🏄] Shift Left scan starting...");
        logger.println("     - Initiator:    " + initiatorToUse);
        logger.println("     - Docker Image: " + dockerImage);
        logger.println("     - Commit:       " + commitToUse);
        logger.println("     - Branch:       " + branchToUse);
        logger.println("     - Repo:         " + repoToUse);

        // Auth
        AuthHelper authHelper = new AuthHelper(logger);
        String authToken = authHelper.getAuthToken(upwindUriToUse, upwindClientIdToUse, upwindClientSecretToUse);
        if (authToken == null || authToken.isEmpty()) {
            logger.println("[❌] Can't authenticate with Upwind.  Please check your client id and secret.");
            return;
        }

        String cliSavePath = workspace + "/shiftleft";
        String cliUrl = String.format(SHIFTLEFT_CLI_URL_TEMPLATE, upwindUriToUse);

        Path cliPath = Path.of(cliSavePath);
        String[] cliCmd = {
            cliPath.toString(),
            "image",
            "--source=JENKINS",
            String.format("--initiator=%s", initiatorToUse),
            String.format("--docker-pull=%s", pullDockerImageToUse),
            String.format("--docker-user=%s", dockerUserToUse),
            String.format("--docker-password=%s", dockerPasswordToUse.getPlainText()),
            String.format("--docker-image=%s", dockerImage),
            String.format("--repository=%s", repoToUse),
            String.format("--branch=%s", branchToUse),
            String.format("--commit-sha=%s", commitToUse),
            String.format("--upwind-client-id=%s", upwindClientIdToUse),
            String.format("--upwind-client-secret=%s", upwindClientSecretToUse.getPlainText()),
            String.format("--upwind-uri=%s", upwindUriToUse)
        };

        // Perform scan on the jenkins agent
        VirtualChannel channel = launcher.getChannel();
        if (channel != null) {
            logger.println("[⏳] Scanning...");
            try {
                channel.call(new ShiftLeftCli(
                        authToken, cliUrl, cliSavePath, cliCmd, new RemoteOutputStream(listener.getLogger())));
                logger.println("[✅] Scanning complete");
            } catch (Exception e) {
                logger.println("[❌] Error scanning: " + e.getMessage());
            }
        }
    }

    @Extension
    @Symbol("shiftLeftScan")
    public static final class DescriptorImpl extends hudson.tasks.BuildStepDescriptor<hudson.tasks.Builder> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "Shift Left Scan";
        }

        @Override
        public boolean isApplicable(Class<? extends hudson.model.AbstractProject> aClass) {
            return true;
        }

        public FormValidation doCheckDockerImage(@QueryParameter String dockerImage) {
            if (dockerImage == null || dockerImage.isEmpty()) {
                return FormValidation.error("Docker image is required.");
            }
            return FormValidation.ok();
        }
    }
}
