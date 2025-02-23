package io.github.kdesp73;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class EudiSdk {
    public enum Visibility {
        PUBLIC(0),
        ANONYMOUS_OPT(1),
        ANONYMOUS(2);

        private final int value;

        Visibility(int value) {
            this.value = value;
        }
    }

    public static class ConfigOptions {
        @SerializedName("required")
        private final Map<String, Boolean> options = new HashMap<>();
        public int visibility;

        public ConfigOptions() {
            // Initialize all options to false by default
            List<String> keys = Arrays.asList("AgeOver18", "HealthID", "IBAN", "Loyalty", "mDL",
                    "MSISDN", "PhotoId", "PID", "PowerOfRepresentation",
                    "PseudonymDeferred", "Reservation", "TaxNumber");
            for (String key : keys) {
                options.put(key, false);
            }
            this.visibility = Visibility.PUBLIC.value;
        }

        public boolean hasAtLeastOneTrue() {
            return options.values().stream().anyMatch(Boolean::booleanValue);
        }

        public void setOption(String key, boolean value) {
            if (options.containsKey(key)) {
                options.put(key, value);
            } else {
                throw new IllegalArgumentException("Invalid option: " + key);
            }
        }

        public void require(String... keys) {
            for(String key : keys)
                this.setOption(key, true);
        }

        public boolean getOption(String key) {
            return options.getOrDefault(key, false);
        }
    }

    private static boolean anonymousCompatibility(ConfigOptions config) {
        if (config.visibility == Visibility.PUBLIC.value) return true;

        return !(
            config.getOption("HealthID") ||
            config.getOption("IBAN") ||
            config.getOption("Loyalty") ||
            config.getOption("mDL") ||
            config.getOption("MSISDN") ||
            config.getOption("PhotoId") ||
            config.getOption("PID") ||
            config.getOption("TaxNumber")
        );
    }

    public static void Login(ConfigOptions config) {
        if (!config.hasAtLeastOneTrue()) {
            System.err.println("Please set at least one value to true");
            return;
        }
        if (!anonymousCompatibility(config)) {
            System.err.println("Only AgeOver18, PseudonymDeferred, PowerOfRepresentation and Reservation are compatible with anonymous visibility");
            return;
        }

        Networking.startServer();
        Networking.openAuthWindow();
        Networking.sendMessageWithRetry(config);
    }
}
