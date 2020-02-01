package org.programmatori.domotica.own.plugin.homekit;

import io.github.hapjava.HomekitAuthInfo;
import io.github.hapjava.HomekitServer;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.function.Consumer;

public class OWNAuthInfo implements HomekitAuthInfo {

    private final AuthState authState;
    Consumer<AuthState> callback;

    public OWNAuthInfo() throws InvalidAlgorithmParameterException {
        this(new AuthState("031-45-155", HomekitServer.generateMac(), HomekitServer.generateSalt(),
                HomekitServer.generateKey()));
    }

    public OWNAuthInfo(AuthState _authState) {
        authState = _authState;
        System.out.println("The PIN for pairing is " + authState.PIN);
    }

    @Override
    public String getPin() {
        return authState.PIN;
    }

    @Override
    public String getMac() {
        return authState.mac;
    }

    @Override
    public BigInteger getSalt() {
        return authState.salt;
    }

    @Override
    public byte[] getPrivateKey() {
        return authState.privateKey;
    }

    @Override
    public void createUser(String username, byte[] publicKey) {
        if (!authState.userKeyMap.containsKey(username)) {
            authState.userKeyMap.putIfAbsent(username, publicKey);
            System.out.println("Added pairing for " + username);
            notifyChange();
        } else {
            System.out.println("Already have a user for " + username);
        }
    }

    @Override
    public void removeUser(String username) {
        authState.userKeyMap.remove(username);
        System.out.println("Removed pairing for " + username);
        notifyChange();
    }

    @Override
    public byte[] getUserPublicKey(String username) {
        return authState.userKeyMap.get(username);
    }

    public void onChange(Consumer<AuthState> _callback) {
        callback = _callback;
        notifyChange();
    }

    private void notifyChange() {
        if (callback != null) {
            callback.accept(authState);
        }
    }
}
