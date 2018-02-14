# view_packets.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# Outputs all recieved packet info.

import DataPacket, XMeshBase
import sys

if(len(sys.argv) < 2):
	print "Usage: view_packets.py <serial_port>"
	sys.exit(0)

xmb = XMeshBase.XMeshBase(sys.argv[1], verbose=True)

try:
	while True:
		pack = xmb.get_packet()
		
		if(pack == False):
			continue

		print "recieved packet" + str(pack.__dict__)
except:
	xmb.kill()