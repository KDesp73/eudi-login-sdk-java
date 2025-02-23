package io.github.kdesp73;

import java.util.*;

public class EUDIAuth {
    private static final String AUTH_URL = "http://localhost:8080";

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
        public boolean AgeOver18;
        public boolean HealthID;
        public boolean IBAN;
        public boolean Loyalty;
        public boolean mDL;
        public boolean MSISDN;
        public boolean PhotoId;
        public boolean PID;
        public boolean PowerOfRepresentation;
        public boolean PseudonymDeferred;
        public boolean Reservation;
        public boolean TaxNumber;
        public Visibility visibility;

        public boolean hasAtLeastOneTrue() {
            return HealthID || IBAN || Loyalty || mDL || MSISDN || PhotoId || PID || TaxNumber;
        }
    }

    private static boolean anonymousCompatibility(ConfigOptions config) {
        if (config.visibility == Visibility.PUBLIC) return true;

        return !(
            config.HealthID ||
            config.IBAN ||
            config.Loyalty ||
            config.mDL ||
            config.MSISDN ||
            config.PhotoId ||
            config.PID ||
            config.TaxNumber
        );
    }

    public static void EUDILogin(ConfigOptions config, String target) {
        if (target == null) {
            target = "http://localhost";
        }

        if (!config.hasAtLeastOneTrue()) {
            System.err.println("Please set at least one value to true");
            return;
        }
        if (!anonymousCompatibility(config)) {
            System.err.println("Only AgeOver18, PseudonymDeferred, PowerOfRepresentation and Reservation are compatible with anonymous visibility");
            return;
        }

        // Simulate authentication window opening
        System.out.println("Opening authentication window at: " + AUTH_URL);

        // Simulated message passing (would be handled in a real application with HTTP communication)
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("site", "http://localhost");
        messageData.put("data", config);

        System.out.println("Sending authentication data...");

        // Simulate authentication response
        String response = "User Data: {\"attestations\":[]}";
        System.out.println(response);
    }
}
