require './varnum'
require 'socket'
require 'zlib'

class ArrayVarNum < Struct.new(:bytes)

    include VarNum  
    
    def read() = self.bytes.shift
    
    def write(x) = self.bytes << x 
    
end


class SocketVarNumReader < Struct.new(:sock)
    
    include VarNum

    def read() = sock.recv(1)
            .bytes
            .first

end


module S end
class << S 
    
    # string :: Text -> [Byte]
    def string(text)
        varnum(text.bytes.size) + text.bytes
    end

    # varnum :: Integer -> [Byte]
    def varnum(x)
        num = ArrayVarNum.new []
        num.write_var_num(x)
        num.bytes
    end

    # short :: Integer -> [Byte]
    def short(num)
        [num].pack("I").bytes.take(2).reverse
    end

end

class Conn < Struct.new :sock
    def send_packet_list(id, data)
        data = S.varnum(id) + data.flatten
        data = S.varnum(data.size) + data
        sock.send(data.pack('C*'), 0)
    end
end

def keep_alive(conn)
    conn.send_packet_list(0x0F, [
        # TODO
    ])
end

def handshake(conn)
    conn.send_packet_list 0x00, [
        S.varnum(757),
        S.string("lol"),
        S.short(25565),
        S.varnum(2)
    ]
end

def login_start(conn)
    conn.send_packet_list 0x00, [
        S.string("fulcanelly"),
    ]
end


module R 
    class << self
        #  VarNum :: Conn -> IO Integer
        def varnum(conn)
            SocketVarNumReader.new(conn.sock).read_var_num()
        end
    
    end
end

module RA
    class << self
        # varnum :: IOref [Byte] -> IO Integer
        def varnum(arr) = 
            ArrayVarNum.new(arr).read_var_num()        
        
        # string :: IOref [Byte] -> IO String
        def string(arr) = arr.shift(varnum(arr))
            .pack('c*').force_encoding('UTF-8')
    
    end
end

class StatePackedReader

    attr_accessor :compress
    attr_accessor :conn

    def initialize(conn) 
        self.compress = false
        self.conn = conn
    end


    def send_packet 
        if self.compress then 
            throw "not implemented suck a fuck pidaer"
        else

        end
    end

    def dispatch_income_packet(data)
        id = RA.varnum(data)


        puts "+ id is #{id}"
        case id 
        when 0x03
            self.compress = true
            puts "enabled compression"
        when 0x02
            puts "UUID: #{data.shift 16}"
            puts "username: #{RA.string data}"
        when 0x21
            puts "keeping alive"

        else 
            puts "unknown packet"
        end

    end

    def recv_packet 
        size = R.varnum(conn)

        data = conn.sock.recv(size).bytes
        puts "data: #{data.take(200)}"
        puts "packed size: #{size}"

        return data
    end

    def read_packet()
        puts "\n\n\n"

        unless compress
            data = recv_packet()
            dispatch_income_packet(data)
        else 
            puts "compressed read"
            read_compressed()
        end
    end

    def read_compressed() 
        fullsize = R.varnum(conn)
        puts "fullsize #{fullsize}"

        return if fullsize == 0 

        data = conn.sock.read(fullsize).bytes
        csize = RA.varnum data 
        puts "compressed size #{csize}"

        if csize == 0 then 
            dispatch_income_packet(data)
        else 
            data = Zlib::Inflate.inflate(data.pack("C*")).bytes 
            dispatch_income_packet(data)
        end

    end

end


conn = Conn.new(TCPSocket.new("82.130.36.101", 25565)) 
#conn = Conn.new(TCPSocket.new("127.0.0.1", 65535)) 

handshake conn
login_start conn
reader = StatePackedReader.new conn
loop do 
    reader.read_packet()
end
  