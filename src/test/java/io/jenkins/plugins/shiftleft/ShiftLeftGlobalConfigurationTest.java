package io.jenkins.plugins.shiftleft;

import static org.junit.Assert.assertEquals;

import hudson.util.Secret;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.DataBoundSetter;

public class ShiftLeftGlobalConfigurationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private ShiftLeftGlobalConfiguration config;
    private final Map<String, Object> generatedValues = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Before
    public void setUp() {
        config = new ShiftLeftGlobalConfiguration();
    }

    @Test
    public void testAllSettersAndGettersWithRandomValues() throws Exception {
        Class<?> clazz = ShiftLeftGlobalConfiguration.class;

        // Iterate through all declared methods to find setters
        for (Method method : clazz.getDeclaredMethods()) {
            if (isSetter(method)) {
                // Generate a random value for the parameter type of the setter
                Class<?> parameterType = method.getParameterTypes()[0];
                Object randomValue = generateRandomValue(parameterType);

                // Save the generated value for later comparison
                String fieldName = method.getName().substring(3); // Remove "set" prefix
                generatedValues.put(fieldName, randomValue);

                // Invoke the setter with the random value
                method.invoke(config, randomValue);
            }
        }

        // Verify that each getter returns the value we set via the setter
        for (Map.Entry<String, Object> entry : generatedValues.entrySet()) {
            String fieldName = entry.getKey();
            Object expectedValue = entry.getValue();

            // Construct the getter name
            String getterName = "get" + fieldName;
            Method getterMethod = clazz.getMethod(getterName);

            // Invoke the getter and assert that the value matches
            Object actualValue = getterMethod.invoke(config);
            assertEquals("Field " + fieldName + " did not match expected value.", expectedValue, actualValue);
        }
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterCount() == 1
                && method.isAnnotationPresent(DataBoundSetter.class);
    }

    private Object generateRandomValue(Class<?> type) {
        if (type == String.class) {
            return randomString(10);
        } else if (type == Secret.class) {
            return Secret.fromString(randomString(10));
        }
        throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }

    private String randomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}
