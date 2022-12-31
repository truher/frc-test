-- CTRE Talon Status4

function talonTemp(x)
  return tostring(x) * 0.645161290322581 - 50
end

local M = {}

M.talon_temp_field = ProtoField.uint8("can.frc.ctre.talon.temp", "Talon Temp", base.DEC, nil, 0xff)
M.talon_voltage_field = ProtoField.uint8("can.frc.ctre.talon.voltage", "Talon Voltage", base.DEC, nil, 0xff)

function M.insert(t)
  table.insert(t, M.talon_temp_field)
  table.insert(t, M.talon_voltage_field)
  M.talon_temp = Field.new("can.frc.ctre.talon.temp")
  M.talon_voltage = Field.new("can.frc.ctre.talon.voltage")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add(M.talon_temp_field, buffer:range(10,1))
  subtree:add(M.talon_voltage_field, buffer:range(9,1))
  subtree:add("Talon Temp (?):", talonTemp(M.talon_temp()()))
  subtree:add("Talon Voltage (V):", voltage(M.talon_voltage()()))
end

return M
