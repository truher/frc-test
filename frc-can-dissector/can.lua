-- FRC CAN Dissector

local socketcan = require("can.socketcan")
local frc = require("frc.frc")
local pdp = require("ctre.pdp")
local motor = require("ctre.motor")

can_protocol = Proto("FRC_CAN", "FRC CAN Protocol")
can_protocol.fields = {}

socketcan.insert(can_protocol.fields)
frc.insert(can_protocol.fields)
pdp.insert(can_protocol.fields)
motor.insert(can_protocol.fields)

function can_protocol.dissector(buffer, pinfo, tree)
  length = buffer:len()
  if length == 0 then return end

  pinfo.cols.protocol = can_protocol.name
  pinfo.cols.info = buffer:bytes():tohex(true, " ")

  local subtree = tree:add(can_protocol, buffer(), "FRC CAN Protocol Data")
  socketcan.dissect(buffer, pinfo, subtree)
  frc.dissect(buffer, pinfo, subtree)
  if frc.can_frc_type()() == 2 then -- motor controller
    if frc.can_frc_mfr()() == 4 then -- CTRE
      motor.dissect(buffer, pinfo, subtree)
    end
  elseif frc.can_frc_type()() == 8 then -- PDP
    if frc.can_frc_mfr()() == 4 then -- CTRE
      pdp.dissect(buffer, pinfo, subtree)
    end
  end
end

local wcap = DissectorTable.get("wtap_encap")
wcap:add(109, can_protocol)
