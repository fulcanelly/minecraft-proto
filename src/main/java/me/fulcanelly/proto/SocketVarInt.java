package me.fulcanelly.proto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketVarInt extends VarInt {

    Socket socket;
    
    SocketVarInt(Socket socket) {
        this.socket = socket;
    }

    @Override 
    byte readByte() {
        try {
            return (byte)socket.getInputStream()
                .read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void writeByte(int b) {
        try {
            socket.getOutputStream().write(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    @Override
    DataOutputStream getOutputStream() {
        try {        
            return new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    DataInputStream getInputStream() {
        try {        
            return new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}
