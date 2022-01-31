package me.fulcanelly.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.management.RuntimeErrorException;

import dev.dewy.nbt.Nbt;
import lombok.SneakyThrows;



public class Main {

    Socket socket;

    int threshold = 0;
    boolean compressed = false;
   
    public static final Nbt NBT = new Nbt();

    @SneakyThrows
    void send(byte[] data) {
        if (compressed) {
            if (data.length < threshold) {
                var bytes = new ByteArrayOutputStream();
                var out = new Oncoming(
                    new ArrayVarInt(bytes, null));
                
                out.varint(0);
                out.getVarInt().getOutputStream()
                    .write(data);


                var outdata = bytes.toByteArray();

                new SocketVarInt(socket).writeVarInt(outdata.length);;
                socket.getOutputStream().write(outdata);
            } else {
                var deflater = new Deflater();
                throw new RuntimeException("not implemented");
            }
        } else {
            new SocketVarInt(socket).writeVarInt(data.length);;
            socket.getOutputStream().write(data);
        }
    }


    void dispatchPacket(Incoming data) {
        System.out.println("== got packet");

        if (compressed) {
            dispatchCompressed(data);
        } else {
            dispatchPurePacket(data);
        }

   
    }


    @SneakyThrows
    void readJoinGame(Incoming data) {
        var reader = data.getVarInt().getInputStream();
    
        System.out.println("entity id: " + reader.readInt());
        System.out.println("is hardcore: " + reader.readBoolean());
        System.out.println("Gamemode: " + reader.readUnsignedByte());
        System.out.println("Prev Gamemode: " + reader.readByte());

    
        var worlds = data.varint();
        for (int i = 0; i < worlds; i++) {
            System.out.println("name " + i + ": " + data.string());
        }

        //


        System.out.println("Codec: " + NBT.fromStream(reader));

        System.out.println("Dimension: " + NBT.fromStream(reader));
        System.out.println("Dimension Name: " + data.string());
        System.out.println("hash: " + Long.toHexString(reader.readLong()));
        System.out.println("Max Players: " + data.varint());
        System.out.println("View Distance: " + data.varint());

       // throw new RuntimeException("lazy");

        
  
    }

    void readPluginMessage(Incoming data) {
        var msg = data.string();
        System.out.println("channel: " + msg);

        if (msg.equals("minecraft:brand")) {
            System.out.println(data.string());
        } else {
            System.out.println(
                Arrays.toString(data.readRest())
                );
        }
    }

    void handleKeepAlive(Incoming data) {
        System.out.println("keep alive");

        var bytestream = new ByteArrayOutputStream();

        var protocol = new ProtocolOut(
            new Oncoming(new ArrayVarInt(bytestream, null))
        );


        protocol.keelAlive(2);
        send(bytestream.toByteArray());
    }


    void dispatchPurePacket(Incoming data) {
        var id = data.varint();
        System.out.println("   +   *   id is : " + Integer.toHexString(id));
        if (id == 0x03) {
    
            threshold = data.varint();
            System.out.println("set threshold: " + threshold);

            compressed = true;
        } else if (id == 0x02) {
           
            System.out.println("uuid:" + Arrays.toString(data.readNBytes(16)));

            System.out.println(data.string());
        } else if (id == 0x18) {
            readPluginMessage(data);
        } else if (id == 0x21) {
            handleKeepAlive(data);
        } else if (id == 0x26) {
            readJoinGame(data);
        } else if (id == 0x5A) {
            readChat(data);
        } else if (id == 0x1A) {
            throw new RuntimeException("disonnect");
        } else {
            System.out.println("unknown id " + Integer.toHexString(id));
        //  throw new RuntimeException("idk such packet: " + Integer.toHexString(id));
        }
    }

    void dispatchCompressed(Incoming data) {
        var dataLength = data.varint();
        if (dataLength == 0) {
            dispatchPurePacket(data);  
        } else {

            var result = new byte[dataLength];
            var decompr = new Inflater();
            decompr.setInput(data.readRest());

            try {
                decompr.inflate(result);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }

            var packetReader = new Incoming(
                new ArrayVarInt(null, new ByteArrayInputStream(result))
            );

            dispatchPurePacket(packetReader);

        }
    }

    void login(String player) {
        var bytestream = new ByteArrayOutputStream();

        var protocol = new ProtocolOut(
            new Oncoming(new ArrayVarInt(bytestream, null))
        );


        protocol.handshake(2);
        
        byte arr[];
        send(arr = bytestream.toByteArray());
           
        System.out.println(Arrays.toString(arr));
        
        bytestream.reset();
        protocol.loginStart(player);
        send(bytestream.toByteArray());


        var protin = new Incoming(
            new SocketVarInt(socket)
        );


        while (true) {
            readPacket(protin);
        }
    }

    byte[] readNBytes(int n) {
        try {
            return socket.getInputStream()
            .readNBytes(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    void readChat(Incoming protin) {
        System.out.println(
            "in: " + protin.string()
        );
    }
    
    void readPacket(Incoming protin) {
        int size = (int)protin.varint();

        var packetData = readNBytes(size);

        var packetReader = new Incoming(
            new ArrayVarInt(null, new ByteArrayInputStream(packetData))
        );
    
        dispatchPacket(packetReader);
    }

    @SneakyThrows
    void setup(String host, int port) {
        socket = new Socket(host, port);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("nea - not enough args");
        }
        
        var main = new Main();
        main.setup(args[0], Integer.valueOf(args[1]));
        main.login(args[2]);
    }
}
