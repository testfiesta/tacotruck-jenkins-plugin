package io.jenkins.plugins.tacotruck.service;

public class HttpServiceFactory {

    public enum Provider {
        TESTFIESTA("testfiesta"),
        TESTRAIL("testrail");

        private final String name;

        Provider(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Provider fromString(String name) {
            for (Provider provider : values()) {
                if (provider.name.equalsIgnoreCase(name)) {
                    return provider;
                }
            }
            throw new IllegalArgumentException("Unknown provider: " + name);
        }
    }

    public static BaseHttpService createService(Provider provider, String apiUrl, String credentialsId) {
        switch (provider) {
            case TESTFIESTA:
                return new TestfiestaHttpServiceImpl(apiUrl, credentialsId);
            case TESTRAIL:
                return new TestrailHttpServiceImpl(apiUrl, credentialsId);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }

    public static BaseHttpService createService(String providerName, String apiUrl, String credentialsId) {
        Provider provider = Provider.fromString(providerName);
        return createService(provider, apiUrl, credentialsId);
    }

    public static BaseHttpService createDefaultService(String apiUrl, String credentialsId) {
        return createService(Provider.TESTFIESTA, apiUrl, credentialsId);
    }
}
