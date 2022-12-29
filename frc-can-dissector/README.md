# FRC CAN Dissector

This is a Lua plugin for Wireshark that decodes FRC CAN messages.

The pcap files are produced by a Teensy-based logger, which writes
packets with pcap headers but without the "linux cooked capture" headers
produced by SocketCAN.

```
https://github.com/truher/frc-test/tree/main/can-logger
```

To use it, put the lua file in your Wireshark plugin directory, which for me
is:

```
  $HOME/.local/lib/wireshark/plugins
```

See also Carlos's FRC CAN dissector written in C:

```
  https://github.com/carlosgj/FRC-CAN-Wireshark
```
