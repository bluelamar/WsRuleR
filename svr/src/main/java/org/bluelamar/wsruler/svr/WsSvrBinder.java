package org.bluelamar.wsruler.svr;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

class WsSvrBinder extends AbstractBinder {

    @Override
    protected void configure() {
    	bind(new WsSvrImpl()).to(WsSvrHandler.class);
    }
}
