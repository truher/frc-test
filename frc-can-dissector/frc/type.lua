-- FRC type in CAN ID
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

local M = {}

M.device_type_field = ProtoField.uint32("can.frc.type", "Device Type", base.RANGE_STRING, device_types, 0x1f000000)

function M.insert(t)
  table.insert(t, M.device_type_field)
  M.can_frc_type = Field.new("can.frc.type")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add_le(M.device_type_field, buffer:range(0,4))
  --subtree:add("can_frc_type:", M.can_frc_type()())
end

return M
