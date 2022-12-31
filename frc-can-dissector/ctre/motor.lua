-- CTRE Motor Control

local status2 = require("motor_status2")
local status4 = require("motor_status4")

local M = {}

function M.insert(t)
  status2.insert(t)
  status4.insert(t)

  -- for the conditionals below.
  M.can_frc_type = Field.new("can.frc.type")
  M.can_frc_mfr = Field.new("can.frc.mfr")
  M.can_frc_api_class = Field.new("can.frc.api_class")
  M.can_frc_api_index = Field.new("can.frc.api_index")
end

function M.dissect(buffer, pinfo, subtree)
  if M.can_frc_type()() == 2 then -- motor controller
    if M.can_frc_mfr()() == 4 then -- CTRE
      -- see LowLevel_TalonSrx.cs
      -- i'm not attempting to be comprehensive here.  :-)
      if M.can_frc_api_class()() == 0 then -- CONTROL
        if M.can_frc_api_index()() == 0 then -- CONTROL_1
        elseif M.can_frc_api_index()() == 1 then -- CONTROL_2
        elseif M.can_frc_api_index()() == 2 then -- CONTROL_3
        elseif M.can_frc_api_index()() == 4 then -- CONTROL_5, output
        elseif M.can_frc_api_index()() == 5 then -- CONTROL_6, motoin profile
        elseif M.can_frc_api_index()() == 6 then -- CONTROL_7, config
        end
      elseif M.can_frc_api_class()() == 5 then -- STATUS
        if M.can_frc_api_index()() == 0 then -- STATUS_1, 10ms
        elseif M.can_frc_api_index()() == 1 then -- STATUS_2, 20ms
          status2.dissect(buffer, pinfo, subtree)
        elseif M.can_frc_api_index()() == 2 then -- STATUS_3, 100ms
        elseif M.can_frc_api_index()() == 3 then -- STATUS_4, 100ms
          status4.dissect(buffer, pinfo, subtree)
        elseif M.can_frc_api_index()() == 4 then -- STATUS_5
        elseif M.can_frc_api_index()() == 5 then -- STATUS_6
        elseif M.can_frc_api_index()() == 6 then -- STATUS_7, 200ms
        elseif M.can_frc_api_index()() == 7 then -- STATUS_8, 100ms
        elseif M.can_frc_api_index()() == 8 then -- STATUS_9, 100ms
        elseif M.can_frc_api_index()() == 9 then -- STATUS_10, 100ms
        elseif M.can_frc_api_index()() == 10 then -- STATUS_11
        end
      elseif M.can_frc_api_class()() == 6 then -- PARAM
        if M.can_frc_api_index()() == 0 then -- PARAM_REQUEST
        elseif M.can_frc_api_index()() == 1 then -- PARAM_RESPONSE
        elseif M.can_frc_api_index()() == 2 then -- PARAM_SET
        end
      end
    end
  end
end

return M
