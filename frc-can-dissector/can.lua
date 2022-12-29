-- See https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html

device_types = {
  { 0, 0, "Broadcast Messages"},
  { 1, 1, "Robot Controller"},
  { 2, 2, "Motor Controller"},
  { 3, 3, "Relay Controller"},
  { 4, 4, "Gyro Sensor"},
  { 5, 5, "Accelerometer"},
  { 6, 6, "Ultrasonic Sensor"},
  { 7, 7, "Gear Tooth Sensor"},
  { 8, 8, "Power Distribution Module"},
  { 9, 9, "Pnuematics Controller"},
  {10,10, "Miscellaneous"},
  {11,11, "IO Breakout"},
  {12,30, "Reserved"},
  {31,31, "Firmware Update"}
}

manufacturers = {
  { 0,   0, "Broadcast" },
  { 1,   1, "NI" },
  { 2,   2, "Luminary Micro" },
  { 3,   3, "DEKA" },
  { 4,   4, "CTR Electronics" },
  { 5,   5, "REV Robotics" },
  { 6,   6, "Grapple" },
  { 7,   7, "MindSensors" },
  { 8,   8, "Team Use" },
  { 9,   9, "Kauai Labs" },
  {10,  10, "Copperforge" },
  {11,  11, "Playing With Fusion" },
  {12,  12, "Studica" },
  {13, 255, "Reserved" }
}

can_protocol = Proto("FRC_CAN", "FRC CAN Protocol")

-- see linux/can.h for these flags
eff_field = ProtoField.bool("eff_flag", "EFF flag", 32, nil, 0x80000000, "Extended Frame Format")
rtr_field = ProtoField.bool("rtr_flag", "RTR flag", 32, nil, 0x40000000, "Remote Frame")
err_field = ProtoField.bool("err_flag", "ERR flag", 32, nil, 0x20000000, "Error")

device_type_field = ProtoField.uint32("device_type", "Device Type", base.RANGE_STRING, device_types, 0x1f000000, "Device Type")
manufacturer_field = ProtoField.uint32("manufacturer", "Manufacturer", base.RANGE_STRING, manufacturers, 0x00ff0000, "Manufacturer")
api_class_field = ProtoField.uint32("api_class", "API Class", base.DEC, nil, 0x0000fc00, "API Class")
api_index_field = ProtoField.uint32("api_index", "API Index", base.DEC, nil, 0x000003c0, "API Index")
device_number_field = ProtoField.uint32("device_number", "Device Number", base.DEC, nil, 0x0000003f, "Device Number")

id_field = ProtoField.uint32("canid", "CAN ID", base.HEX, nil, 0x1fffffff, "CAN id")
length_field = ProtoField.uint8("data_length", "data length", base.DEC, nil, nil, "data length")
pad_field = ProtoField.bytes("padding", "padding", base.NONE, "padding")
data_field = ProtoField.bytes("datafield", "data field", base.NONE, "the data field")

can_protocol.fields = {
  eff_field,
  rtr_field,
  err_field,
  device_type_field,
  manufacturer_field,
  api_class_field,
  api_index_field,
  device_number_field,
  id_field,
  length_field,
  pad_field,
  data_field
}

-- Tvb buffer, see lua_module_Tvb.html
function can_protocol.dissector(buffer, pinfo, tree)
  length = buffer:len()
  if length == 0 then return end

  pinfo.cols.protocol = can_protocol.name
  pinfo.cols.info = buffer:bytes():tohex(true, " ")

  local subtree = tree:add(can_protocol, buffer(), "FRC CAN Protocol Data")
  subtree:add_le(id_field, buffer:range(0,4))

  subtree:add_le(eff_field, buffer:range(0,4)) -- should always be true!
  subtree:add_le(rtr_field, buffer:range(0,4))
  subtree:add_le(err_field, buffer:range(0,4))
  subtree:add_le(device_type_field, buffer:range(0,4))
  subtree:add_le(manufacturer_field, buffer:range(0,4))
  subtree:add_le(api_class_field, buffer:range(0,4))
  subtree:add_le(api_index_field, buffer:range(0,4))
  subtree:add_le(device_number_field, buffer:range(0,4))

  subtree:add(length_field, buffer:range(4,1))
  subtree:add(pad_field, buffer:range(5,3))
  subtree:add(data_field, buffer:range(8))
end

local wcap = DissectorTable.get("wtap_encap")
wcap:add(109, can_protocol)
