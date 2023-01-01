#!/usr/bin/python3
#
# Reader
#
# See dpkt/examples/print_packets.py
# See docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
#

#import bitstring # does not allow unaligned little-endian ints
#import bitstruct # allows unaligned little-endian ints
import datetime
import dpkt
import struct
import sys

# 3 bit flags
# 29-bit id
# 8-bit length
# 24-bit padding
# 64-bit data
FMT = bitstruct.compile('<u29>u1u1u1u8p24>r64<')

def main(infile):
    with open(infile, 'rb') as f:
        r = dpkt.pcap.Reader(f)
        print('===================')
        print('FILE HEADER')
        print('Network (should be 190): ', r.datalink())
        for ts, buf in r:
            print('===================')
            print('Timestamp: ', str(datetime.datetime.utcfromtimestamp(ts)))
            print('buffer: ', buf)
            #record = bitstring.BitArray(buf)
            print('Buffer length: ', len(buf)) # should be 16
            #print(str(record))
            # note the order here is the inverse of the struct order
            #eff, rtr, err, canid, lng, data = record.unpack(
            #  'bool, bool, bool, uint29, uint8, pad24, bits:64'
            #)
            eff, rtr, err, canid, lng, data = FMT.unpack(buf)
            print('EFF: ', eff)
            print('RTR: ', rtr)
            print('ERR: ', err)
            print('CAN id: ', hex(canid))
            print('Length (should be 8): ', lng)
            print('Data: ', data)

if __name__ == "__main__":
    infile = sys.argv[1]
    main(infile)
