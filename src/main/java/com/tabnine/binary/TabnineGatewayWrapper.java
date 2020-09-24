package com.tabnine.binary;

public class TabnineGatewayWrapper {
    private static TabNineGateway INSTANCE = null;

    public static synchronized TabNineGateway getOrCreateInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TabNineGateway();

            INSTANCE.init();
        }

        return INSTANCE;
    }
}
