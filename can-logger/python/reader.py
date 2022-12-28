#!/usr/bin/python3
#
# Reader
#
# See dpkt/examples/print_packets.py
# See docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
#

import dpkt
import datetime
import struct

with open('canbus.pcap', 'rb') as f:
    for ts, buf in dkpt.pcap.Reader(f):
        print('Timestamp: ', str(datetime.datetime.utcfromtimestamp(ts)))
        # ... etc

