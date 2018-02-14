# send_command.py
# Alex Scarpantoni, 2015
# alex@ascarp.com
# Sends an acknowledged command to WW.

import DataPacket, XMeshBase
import sys, struct

if(len(sys.argv) < 5):
	print "Usage: send_command.py <serial_port> <command> <param> <mote>"
	sys.exit(0)

xmb = XMeshBase.XMeshBase(sys.argv[1], verbose=True, require_ack=True)

pack = DataPacket.DataPacket()
pack.application_id = 51
pack.am_type = DataPacket.AM_DATAACK2NODE
pack.am_group = 125
pack.source_address = 0
pack.origin_address = 0
pack.destination = int(sys.argv[4])
pack.payload = struct.pack("<HBH", 0xABCD, int(sys.argv[2]), int(sys.argv[3]))

xmb.send_packet(pack)

try:
	while True:
		pass
except:
	xmb.kill()