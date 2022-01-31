package me.fulcanelly.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ArrayVarInt extends VarInt {

    ByteArrayOutputStream ostream;
    ByteArrayInputStream istream;
    

    ByteArrayOutputStream getOstream() { 
        return ostream;
    } 


    ByteArrayInputStream getIstream() { 
        return istream;
    } 

    ArrayVarInt(ByteArrayOutputStream ostream, ByteArrayInputStream istream) {
        this.ostream = ostream;
        this.istream = istream;
    }

    @Override
    DataOutputStream getOutputStream() {
        return new DataOutputStream(ostream);
    }

    @Override
    DataInputStream getInputStream() {
        return new DataInputStream(istream);      
    }

    @Override
    byte readByte() {
        return (byte) istream.read();
    }

    @Override
    void writeByte(int b) {
        ostream.write(b);        
    }
    
}
