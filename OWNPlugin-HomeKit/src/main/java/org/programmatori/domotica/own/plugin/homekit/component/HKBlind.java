package org.programmatori.domotica.own.plugin.homekit.component;

import io.github.hapjava.HomekitCharacteristicChangeCallback;
import io.github.hapjava.accessories.BasicWindowCovering;
import io.github.hapjava.accessories.Lightbulb;
import io.github.hapjava.accessories.properties.WindowCoveringPositionState;
import org.programmatori.domotica.own.engine.emulator.component.Blind;
import org.programmatori.domotica.own.engine.emulator.component.Light;
import org.programmatori.domotica.own.sdk.component.BlindState;
import org.programmatori.domotica.own.sdk.component.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class HKBlind implements BasicWindowCovering {
    private static final Logger logger = LoggerFactory.getLogger(HKBlind.class);

    private HomekitCharacteristicChangeCallback subscribeStateCallback = null;
    private HomekitCharacteristicChangeCallback subscribeTargetCallback = null;
    private HomekitCharacteristicChangeCallback subscribeCurrentCallback = null;
    private final int id;
    private BlindState state;



    public HKBlind(int id, BlindState state) {
        this.id = id;
        this.state = state;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return Who.BLIND.getName() + " "+ id;
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
        return Who.BLIND.getName();
    }

    @Override
    public String getManufacturer() {
        return "BTicino";
    }

    @Override
    public CompletableFuture<Integer> getCurrentPosition() {
        return null;
    }

    @Override
    public CompletableFuture<Integer> getTargetPosition() {
        return null;
    }

    @Override
    public CompletableFuture<WindowCoveringPositionState> getPositionState() {
        return null;
    }

    @Override
    public CompletableFuture<Void> setTargetPosition(int position) throws Exception {

        return setTargetPosition(BlindState.STOP);
    }

    @Override
    public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
        this.subscribeCurrentCallback = callback;
    }

    @Override
    public void unsubscribeCurrentPosition() {
        this.subscribeCurrentCallback = null;
    }

    @Override
    public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
        this.subscribeTargetCallback = callback;
    }

    @Override
    public void unsubscribeTargetPosition() {
        this.subscribeTargetCallback = null;
    }

    @Override
    public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
        this.subscribeStateCallback = callback;
    }

    @Override
    public void unsubscribePositionState() {
        this.subscribeStateCallback = null;
    }

    public CompletableFuture<Void> setTargetPosition(BlindState newState) {
        state = newState;

        if (subscribeStateCallback != null) {
            subscribeStateCallback.changed();
        }
        if (subscribeTargetCallback != null) {
            subscribeTargetCallback.changed();
        }
        if (subscribeCurrentCallback != null) {
            subscribeCurrentCallback.changed();
        }
        logger.info("The {} is now {}", getLabel(), state.getName());

        return CompletableFuture.completedFuture(null); // TODO: What mean??
    }
}
