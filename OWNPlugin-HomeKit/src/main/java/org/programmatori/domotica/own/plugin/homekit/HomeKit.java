package org.programmatori.domotica.own.plugin.homekit;

import io.github.hapjava.HomekitAccessory;
import io.github.hapjava.HomekitRoot;
import io.github.hapjava.HomekitServer;
import org.programmatori.domotica.own.plugin.homekit.component.HKBlind;
import org.programmatori.domotica.own.plugin.homekit.component.HKLight;
import org.programmatori.domotica.own.sdk.component.BlindState;
import org.programmatori.domotica.own.sdk.component.LightState;
import org.programmatori.domotica.own.sdk.component.Who;
import org.programmatori.domotica.own.sdk.msg.MessageFormatException;
import org.programmatori.domotica.own.sdk.msg.SCSMsg;
import org.programmatori.domotica.own.sdk.server.engine.EngineManager;
import org.programmatori.domotica.own.sdk.server.engine.PlugIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.HashMap;

/**
 * Connect This server to a Health Kit of Apple
 *
 * @author Moreno Cattaneo
 * @since 07/11/2019
 */
public class HomeKit implements PlugIn {
    private static final Logger logger = LoggerFactory.getLogger(HomeKit.class);
    private static final int PORT = 9123;

    private int pauseStart = 5000;
    private int pauseUnit = 5000;
    private EngineManager engine;
    private java.util.Map<Integer, HomekitAccessory> accessories;
    private HomekitRoot bridge;

    public HomeKit(EngineManager engine) {
        this.engine = engine;
        accessories = new HashMap<>();
    }

    @Override
    public void start() {
        logger.trace("Start Start");
        try {
            OWNAuthInfo mockAuth = new OWNAuthInfo();

            InetAddress localAddress = InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, 3, 115});
            HomekitServer homeKit = new HomekitServer(localAddress, PORT);

            bridge = homeKit.createBridge(mockAuth, "Test Bridge", "TestBridge, Inc.", "G6", "111abe234");
            logger.trace("End Start");

            //bridge.addAccessory(new OWNSwitch());

            run();

        } catch (InvalidAlgorithmParameterException | IOException e) {
            logger.error("Error in start", e);
        }
    }

    private void run() {
        try {
            Thread.sleep(pauseStart);
        } catch (InterruptedException e) {
            logger.error("error sleep start", e);
            Thread.currentThread().interrupt();
        }
        prepareLight();

        try {
            Thread.sleep(pauseUnit);
        } catch (InterruptedException e) {
            logger.error("error sleep light", e);
            Thread.currentThread().interrupt();
        }
        prepareBlind();

        try {
            Thread.sleep(pauseUnit);
        } catch (InterruptedException e) {
            logger.error("error sleep blind", e);
            Thread.currentThread().interrupt();
        }

        if (accessories.size() == 0) {
            logger.info("Bus Empty");
        }

        bridge.start();
    }

    private void prepareLight() {
        try {
            SCSMsg msg = new SCSMsg("*#1*0##");

            engine.sendCommand(msg, this);
        } catch (MessageFormatException e) {
            logger.error("Error prepare lights", e);
        }
    }

    private void prepareBlind() {
        try {
            SCSMsg msg = new SCSMsg("*#2*0##");

            engine.sendCommand(msg, this);
        } catch (MessageFormatException e) {
            logger.error("Error prepare blinds", e);
        }
    }

    @Override
    public void receiveMsg(SCSMsg msg) {
        int who = msg.getWho().getMain();

        if (who == Who.LIGHT.getValue()) {
            LightState state = LightState.createByValue(msg.getWhat().getMain());
            HKLight light = null;

            if (accessories.containsKey(msg.getWhere().getAddress())) {
                light = (HKLight) accessories.get(msg.getWhere().getAddress());
                try {
                    light.setLightbulbPowerState(state);
                } catch (Exception e) {
                    logger.error("Error in set state", e);
                }
            } else {
                light = new HKLight(msg.getWhere().getAddress(), state, this);
                accessories.put(msg.getWhere().getAddress(), light);
            }

        } else if (who == Who.BLIND.getValue()) {
            BlindState state = BlindState.createByValue(msg.getWhat().getMain());
            HKBlind blind = null;

            if (accessories.containsKey(msg.getWhere().getAddress())) {
                blind = (HKBlind) accessories.get(msg.getWhere().getAddress());
                try {
                    blind.setTargetPosition(state);
                } catch (Exception e) {
                    logger.error("Error in set state", e);
                }
            } else {
                blind = new HKBlind(msg.getWhere().getAddress(), state);
                //accessories.put(msg.getWhere().getAddress(), blind);
            }

        } else {
            throw new RuntimeException("Unknown element");
        }
    }

    @Override
    public long getId() {
        return -1;
    }

    public void sendCommand(SCSMsg msg) {
        engine.sendCommand(msg, this);
    }
}
