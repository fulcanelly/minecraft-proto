package me.fulcanelly.proto;

import lombok.SneakyThrows;

public class ProtocolOut {
    
    Oncoming oncoming;

    ProtocolOut(Oncoming oncoming) {
        this.oncoming = oncoming;
    }
    
    void handshake(int state) {
        oncoming.varint(0x00);
        oncoming.varint(757);
        oncoming.string("lolcalhost");
        oncoming.shorti((short)25565);
        oncoming.varint(state);
    }

    void loginStart(String username) {
        oncoming.varint(0x00);
        oncoming.string(username);
    }


    @SneakyThrows
    void keelAlive(long id) {
        oncoming.varint(0x0F);
        oncoming.getVarInt().getOutputStream()
            .writeLong(id);
    }
}
