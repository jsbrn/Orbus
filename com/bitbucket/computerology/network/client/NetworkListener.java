package com.bitbucket.computerology.network.client;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.gui.MainMenu;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.network.Packet.*;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class NetworkListener extends Listener {
    
    @Override
    public void connected(Connection c) {
        JoinRequest jr = new JoinRequest();
        jr.update_id = SlickInitializer.UPDATE_ID;
        c.sendTCP(jr);
    }

    @Override
    public void disconnected(Connection c) {
        MainMenu.enter();
        MainMenu.CONNECT_BUTTON.enable();
    }

    @Override
    public void received(Connection c, Object o) {
        MPClient.PACKETS_RECIEVED++;
        if (!MPClient.PACKET_NAMES.contains(o.getClass().toString())) {
            MPClient.PACKET_NAMES.add(o.getClass().toString());
            MPClient.PACKET_COUNTS.add(1);
        } else {
            int index = MPClient.PACKET_NAMES.indexOf(o.getClass().toString());
            int count = MPClient.PACKET_COUNTS.get(index);
            MPClient.PACKET_COUNTS.set(index, count+1);
        }
        MPClient.processPacket(c, o, GameScreen.getStateBasedGame().getCurrentStateID()
            == SlickInitializer.GAME_SCREEN);
    }
    
}