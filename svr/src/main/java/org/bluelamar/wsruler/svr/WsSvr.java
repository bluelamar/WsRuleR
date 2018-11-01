package org.bluelamar.wsruler.svr;

import org.glassfish.jersey.server.ResourceConfig;

class WsSvr extends ResourceConfig {
    public WsSvr() {
		registerClasses(WsSvrResources.class);
		register(new WsSvrBinder());
    }
}
