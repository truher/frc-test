-- CTRE PDP
--
-- PDP measurements are 10-bit, so most of the work here
-- is in stitching together 2, 4, 6, and 8 bit pieces, to
-- avoid spanning bytes in the payload.
--
-- See https://github.com/wpilibsuite/allwpilib/pull/1081/files
-- See https://github.com/CrossTheRoadElec/deprecated-HERO-SDK/blob/master/CTRE/LowLevel_Pcm.cs

local status1 = require("pdp_status1")
local status2 = require("pdp_status2")
local status3 = require("pdp_status3")
local status_energy = require("pdp_status_energy")
local control1 = require("pdp_control1")

local M = {}

function M.insert(t)
  status1.insert(t)
  status2.insert(t)
  status3.insert(t)
  status_energy.insert(t)
  control1.insert(t)

  -- for the conditionals below.
  M.can_frc_type = Field.new("can.frc.type")
  M.can_frc_mfr = Field.new("can.frc.mfr")
  M.can_frc_api_class = Field.new("can.frc.api_class")
  M.can_frc_api_index = Field.new("can.frc.api_index")
end

function M.dissect(buffer, pinfo, subtree)
  if M.can_frc_type()() == 8 then -- PDP
    if M.can_frc_mfr()() == 4 then -- CTRE
      if M.can_frc_api_class()() == 5 then -- STATUS
        if M.can_frc_api_index()() == 0 then
          status1.dissect(buffer, pinfo, subtree)
        elseif M.can_frc_api_index()() == 1 then
          status2.dissect(buffer, pinfo, subtree)
        elseif M.can_frc_api_index()() == 2 then
          status3.dissect(buffer, pinfo, subtree)
        elseif M.can_frc_api_index()() == 13 then
          status_energy.dissect(buffer, pinfo, subtree)
        end
      elseif M.can_frc_api_class()() == 7 then -- CONTROL
        if M.can_frc_api_index()() == 0 then
          control1.dissect(buffer, pinfo, subtree)
        end
      end
    end
  end
end

return M
