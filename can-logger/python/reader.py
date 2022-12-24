#!/usr/bin/python3

# see
# https://github.com/kbandla/dpkt/blob/master/examples/print_packets.py

import dpkt
import datetime

with open('canbus.pcap', 'rb') as f:
    for ts, buf in dkpt.pcap.Reader(f):
        print('Timestamp: ', str(datetime.datetime.utcfromtimestamp(ts)))
        # ... etc
