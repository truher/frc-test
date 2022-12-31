-- FRC-defined fields in CAN ID
--
-- See https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html

device_types = {
  { 0,  0, "Broadcast Messages"},
  { 1,  1, "Robot Controller"},
  { 2,  2, "Motor Controller"},
  { 3,  3, "Relay Controller"},
  { 4,  4, "Gyro Sensor"},
  { 5,  5, "Accelerometer"},
  { 6,  6, "Ultrasonic Sensor"},
  { 7,  7, "Gear Tooth Sensor"},
  { 8,  8, "Power Distribution Module"},
  { 9,  9, "Pneumatics Controller"},
  {10, 10, "Miscellaneous"},
  {11, 11, "IO Breakout"},
  {12, 30, "Reserved"},
  {31, 31, "Firmware Update"}
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

broadcast_api_indexes = {
  [0] = "Disable",
  [1] = "System Halt",
  [2] = "System Reset",
  [3] = "Device Assign",
  [4] = "Device Query",
  [5] = "Heartbeat",
  [6] = "Sync",
  [7] = "Update",
  [8] = "Firmware Version",
  [9] = "Enumerate",
  [10] = "System Resume"
}

local M = {}

M.device_type_field = ProtoField.uint32("can.frc.type", "Device Type", base.RANGE_STRING, device_types, 0x1f000000)
M.manufacturer_field = ProtoField.uint32("can.frc.mfr", "Manufacturer", base.RANGE_STRING, manufacturers, 0x00ff0000)
M.api_class_field = ProtoField.uint32("can.frc.api_class", "API Class", base.DEC, nil, 0x0000fc00)
M.api_index_field = ProtoField.uint32("can.frc.api_index", "API Index", base.DEC, nil, 0x000003c0)
M.api_index_broadcast_field = ProtoField.uint32("can.frc.api_index", "API Index", base.DEC, broadcast_api_indexes, 0x000003c0)
M.device_number_field = ProtoField.uint32("can.frc.device_number", "Device Number", base.DEC, nil, 0x0000003f)

function M.insert(t)
  table.insert(t, M.device_type_field)
  table.insert(t, M.manufacturer_field)
  table.insert(t, M.api_class_field)
  table.insert(t, M.api_index_field)
  table.insert(t, M.api_index_broadcast_field)
  table.insert(t, M.device_number_field)
  M.can_frc_type = Field.new("can.frc.type")
  M.can_frc_mfr = Field.new("can.frc.mfr")
  M.can_frc_api_class = Field.new("can.frc.api_class")
  M.can_frc_api_index = Field.new("can.frc.api_index")
end

function M.dissect(buffer, pinfo, subtree)
  -- these fields are always present
  subtree:add_le(M.device_type_field, buffer:range(0,4))
  subtree:add_le(M.manufacturer_field, buffer:range(0,4))
  subtree:add_le(M.api_class_field, buffer:range(0,4))
  subtree:add_le(M.api_index_field, buffer:range(0,4))
  subtree:add_le(M.device_number_field, buffer:range(0,4))

  if M.can_frc_type()() == 0 then -- broadcast
    if M.can_frc_mfr()() == 0 then -- broadcast
      if M.can_frc_api_class()() == 0 then -- broadcast
        subtree:add_le(M.api_index_broadcast_field, buffer:range(0,4))
      end
    end
  end

  --subtree:add("can_frc_type:", M.can_frc_type()())
  --subtree:add("can_frc_mfr:", M.can_frc_mfr()())

end

return M
