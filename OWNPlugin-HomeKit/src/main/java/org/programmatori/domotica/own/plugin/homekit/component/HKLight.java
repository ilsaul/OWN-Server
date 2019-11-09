package org.programmatori.domotica.own.plugin.homekit.component;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.accessories.Lightbulb;
import org.programmatori.domotica.own.engine.emulator.component.Light;
import org.programmatori.domotica.own.plugin.homekit.HomeKit;
import org.programmatori.domotica.own.sdk.component.LightState;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.msg.What;
import org.programmatori.domotica.own.sdk.msg.Where;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class HKLight implements Lightbulb {
    private static final Logger logger = LoggerFactory.getLogger(HKLight.class);

    private HomekitCharacteristicChangeCallback subscribeCallback = null;
    private final int id;
    private LightState state;
    private HomeKit owner;

    public HKLight(int id, LightState state, HomeKit owner) {
        this.id = id;
        this.state = state;
        this.owner = owner;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return Who.LIGHT.getName() + " " + id;
    }

    @Override
    public void identify() {
        logger.info("Identifying {}", getLabel());
    }

    @Override
    public String getSerialNumber() {
        return "none";
    }

    @Override
    public String getModel() {
        return "Light Base";
    }

    @Override
    public String getManufacturer() {
        return "BTicino";
    }

    @Override
    public CompletableFuture<Boolean> getLightbulbPowerState() {
        return CompletableFuture.completedFuture(state.getValue() == 1);
    }

    @Override
    public CompletableFuture<Void> setLightbulbPowerState(boolean newState) throws Exception {
        //LightState nextState;
        if (newState) {
            state = LightState.ON;
        } else {
            state = LightState.OFF;
        }
        if (subscribeCallback != null) {
            subscribeCallback.changed();
        }
        owner.sendCommand(new SCSMsg(new org.programmatori.domotica.own.sdk.msg.Who("" + Who.LIGHT.getValue()),
                new Where("" + getId()), new What("" + state.getValue())));
        logger.info("The {} is now {}", getLabel(), state.getName());


        return CompletableFuture.completedFuture(null); // TODO: What mean??
    }

    @Override
    public void subscribeLightbulbPowerState(HomekitCharacteristicChangeCallback callback) {
        this.subscribeCallback = callback;

    }

    @Override
    public void unsubscribeLightbulbPowerState() {
        this.subscribeCallback = null;
    }

    public CompletableFuture<Void> setLightbulbPowerState(LightState newState) {
        state = newState;

        if (subscribeCallback != null) {
            subscribeCallback.changed();
        }
        logger.info("The {} is now {}", getLabel(), state.getName());

        return CompletableFuture.completedFuture(null); // TODO: What mean??
    }
}
