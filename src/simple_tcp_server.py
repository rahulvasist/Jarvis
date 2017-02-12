import socket
import sys

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('0.0.0.0', 5000)
print 'connecting to %s port %s' % server_address
sock.bind(server_address)

sock.listen(1)

while True:
    # Wait for a connection
    print 'waiting for a connection'
    connection, client_address = sock.accept()

    try:
        print 'connection from', client_address

        # Receive the data in small chunks and retransmit it
        data = connection.recv(16)
        print 'received "%s"' % data
        if data == 'E' or data == 'D':
            connection.sendall('ACK')
        else:
            connection.sendall('NACK')
            
    finally:
        # Clean up the connection
        connection.close()
