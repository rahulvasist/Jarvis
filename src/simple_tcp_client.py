import socket
import sys

if len(sys.argv) != 2:
    print "Error: format is\n%s [data]" % sys.argv[0]
    sys.exit(0);

message = sys.argv[1]

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Connect the socket to the port where the server is listening
server_address = ('192.168.0.20', 5000)
print >>sys.stderr, 'connecting to %s port %s' % server_address
sock.connect(server_address)

try:
    print >>sys.stderr, 'sending "%s"' % message
    sock.sendall(message)

    data = sock.recv(1024)
    print >>sys.stderr, 'received "%s"' % data

finally:
    print >>sys.stderr, 'closing socket'
    sock.close()

