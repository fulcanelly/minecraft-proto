package me.fulcanelly.proto;

import java.io.IOException;

public class Incoming {
    VarInt varInt;
    
    Incoming(VarInt varInt) {
        this.varInt = varInt;
    }

    VarInt getVarInt() {
        return varInt;
    }
    
    byte[] readNBytes(int n) {
        try {
            return varInt.getInputStream().readNBytes(n);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    byte[] readRest() {
        try {
            return varInt.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    long varlong() {
        return varInt.readVarLong();
    }
    
    int varint() {
        return varInt.readVarInt();
    }

    String string() {
        var size = varint();
        try {
            return new String(varInt.getInputStream().readNBytes(size));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
