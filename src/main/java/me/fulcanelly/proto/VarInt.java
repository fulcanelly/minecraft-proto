package me.fulcanelly.proto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class VarInt {
    
    abstract DataOutputStream getOutputStream();
    
    abstract DataInputStream getInputStream();
    abstract byte readByte();

    abstract void writeByte(int b);

    public void test() {
        System.out.println("i have read: " + readByte());
        writeVarInt(25565);
    }

    public int readVarInt() {
        int value = 0;
        int length = 0;
        byte currentByte;
    
        while (true) {
            currentByte = readByte();
            value |= (currentByte & 0x7F) << (length * 7);
            
            length += 1;
            if (length > 5) {
                throw new RuntimeException("VarInt is too big");
            }
    
            if ((currentByte & 0x80) != 0x80) {
                break;
            }
        }
        return value;
    }

    public long readVarLong() {
        long value = 0;
        int length = 0;
        byte currentByte;
    
        while (true) {
            currentByte = readByte();
            value |= (currentByte & 0x7F) << (length * 7);
            
            length += 1;
            if (length > 10) {
                throw new RuntimeException("VarLong is too big");
            }
    
            if ((currentByte & 0x80) != 0x80) {
                break;
            }
        }
        return value;
    }

    public void writeVarInt(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
              writeByte(value);
              return;
            }
    
            writeByte((value & 0x7F) | 0x80);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    public void writeVarLong(long value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
              writeByte((byte)value);
              return;
            }
    
            writeByte((byte)((value & 0x7F) | 0x80));
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

}