### FileChannel
- A FileChannel cannot be set into non-blocking mode. It always runs in **blocking mode**.
```java
RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
FileChannel inChannel = aFile.getChannel();
// read data
ByteBuffer readBuf = ByteBuffer.allocate(48);
// The int returned how many bytes were written into the Buffer. If -1 is returned, the end-of-file is reached.
int bytesRead = inChannel.read(readBuf);
// write data
String newData = "New String to write to file..." + System.currentTimeMillis();
ByteBuffer writeBuf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
while(buf.hasRemaining()) {
    channel.write(writeBuf);
}
// closing channel
channel.close();  
```

### SocketChannel
- A SocketChannel that is _connected to_ a **TCP** network socket.
```java
SocketChannel socketChannel = SocketChannel.open();
socketChannel.connect(new InetSocketAddress("http://example.com", 80));
// read data
ByteBuffer buf = ByteBuffer.allocate(48);
int bytesRead = socketChannel.read(buf);
// write data
String newData = "New String to write to file..." + System.currentTimeMillis();
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
while(buf.hasRemaining()) {
    channel.write(buf);
}
socketChannel.close();   

/**Non-blocking Mode**/
SocketChannel socketChannel = SocketChannel.open();
socketChannel.configureBlocking(false);
socketChannel.connect(new InetSocketAddress("http://example.com", 80));
while(! socketChannel.finishConnect() ){
    //wait, or do something else...    
} 
```
### ServerSocketChannel
- A ServerSocketChannel that can _listen for_ incoming **TCP** connections.
```java
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
    //do something with socketChannel...
}
serverSocketChannel.close();

/**Non-blocking Mode**/
ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
serverSocketChannel.socket().bind(new InetSocketAddress(9999));
serverSocketChannel.configureBlocking(false);
while(true){
    SocketChannel socketChannel = serverSocketChannel.accept();
    if(socketChannel != null){
        //do something with socketChannel...
    }
}
```
### DatagramChannel
- A DatagramChannel that can send and receive **UDP** packets.
```java
DatagramChannel channel = DatagramChannel.open();
channel.socket().bind(new InetSocketAddress(9999));
// receive data
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
channel.receive(buf);
// send data
String newData = "New String to write to file..."+ System.currentTimeMillis();
ByteBuffer buf = ByteBuffer.allocate(48);
buf.clear();
buf.put(newData.getBytes());
buf.flip();
int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));
```

[NIO Server](https://github.com/jjenkov/java-nio-server)

