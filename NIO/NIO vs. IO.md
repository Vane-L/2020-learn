### NIO vs. IO
Stream Oriented vs. Buffer Oriented
- IO is stream oriented, where NIO is buffer oriented. 
- Java IO being stream oriented means that you read one or more bytes at a time, from a stream. 
    - What you do with the read bytes is up to you. They are not cached anywhere. 
    - Furthermore, you cannot move forth and back in the data in a stream. 
- Java NIO can read data into a buffer from which it is later processed. You can move forth and back in the buffer as you need to. 
    - This gives you a bit more flexibility during processing. 
    - And, you need to make sure that when reading more data into the buffer, you do not overwrite data in the buffer you have not yet processed.