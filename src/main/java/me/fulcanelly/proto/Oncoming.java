package me.fulcanelly.proto;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Oncoming {
    VarInt varInt;
    

    VarInt getVarInt() {
        return varInt;
    }
    
    Oncoming(VarInt varInt) {
        this.varInt = varInt;
    }

    void varint(int i) {
        varInt.writeVarInt(i);
    }

    void shorti(short i) {
        try {
            varInt.getOutputStream().writeShort(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void string(String str) {
        var bytes = str.getBytes();
        varint(bytes.length);
        try {
            varInt.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
