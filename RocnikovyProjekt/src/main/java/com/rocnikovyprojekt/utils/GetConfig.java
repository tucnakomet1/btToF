package com.rocnikovyprojekt.utils;

public class GetConfig {
    public static String[] config;

    public static void getConfig() {
        config = createConfig();
    }

    /**
     * Check if the configuration is set.
     * PROZATIM JE ZKOUSENO JEN PRO [0]
     */
    private static String[] createConfig() {
        String[] config = new String[4];

        if (!GetCOM.CheckForPorts())
            GetCOM.getCOM();

        if (GetCOM.CheckForPorts()) {
            String[] com = GetCOM.getPorts()[0];
            config[0] = com[0];
            config[1] = com[1];
            config[2] = com[2];
        }

        return config;
    }

}
