package com.customwrld.atom.listeners;

import com.customwrld.pigeon.annotations.PayloadListener;
import com.customwrld.pigeon.listeners.Listener;

@PayloadListener
public class GamemodeListener implements Listener {

//    @PayloadHandler(requiredState = RequiredState.REQUEST)
//    public void onGamemodeRequest(GamemodeRequestPayload payload) {
//        String name = payload.getName();
//
//        Gamemode gamemode = Gamemode.getByName(name);
//
//        payload.sendResponse(new GamemodeRequestPayload(gamemode));
//    }
//
//    @PayloadHandler
//    public void onRegisterGamemode(GamemodeRegisterPayload payload) {
//        var gamemode = payload.getGamemode();
//        Gamemode.save(gamemode);
//    }

}
