-- FRC manufacturer in CAN ID
--
-- See https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html

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

local M = {}

M.manufacturer_field = ProtoField.uint32("can.frc.mfr", "Manufacturer", base.RANGE_STRING, manufacturers, 0x00ff0000)

function M.insert(t)
  table.insert(t, M.manufacturer_field)
  M.can_frc_mfr = Field.new("can.frc.mfr")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add_le(M.manufacturer_field, buffer:range(0,4))
  --subtree:add("can_frc_mfr:", M.can_frc_mfr()())
end


return M
