-- CTRE PDP Status3

function currentA(x)
  return tostring(x) * 0.125
end

function voltage(x)
  return tostring(x) * 0.05 + 4.0
end

function temperatureC(x)
  return tostring(x) * 1.03250836957542 - 67.8564500484966
end

local M = {}

M.chan13_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan13_h8", "PDP Chan13_h8", base.DEC, nil, 0xff)
M.chan14_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan14_h6", "PDP Chan14_h6", base.DEC, nil, 0xfc)
M.chan13_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan13_l2", "PDP Chan13_l2", base.DEC, nil, 0x03)
M.chan15_h4_field = ProtoField.uint8("can.frc.ctre.pdp.chan15_h4", "PDP Chan15_h4", base.DEC, nil, 0xf0)
M.chan14_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan14_l4", "PDP Chan14_l4", base.DEC, nil, 0x0f)
M.chan16_h2_field = ProtoField.uint8("can.frc.ctre.pdp.chan16_h2", "PDP Chan16_h2", base.DEC, nil, 0xc0)
M.chan15_l6_field = ProtoField.uint8("can.frc.ctre.pdp.chan15_l6", "PDP Chan15_l6", base.DEC, nil, 0x3f)
M.chan16_l8_field = ProtoField.uint8("can.frc.ctre.pdp.chan16_l8", "PDP Chan16_l8", base.DEC, nil, 0xff)
M.internalres_field = ProtoField.uint8("can.frc.ctre.pdp.internalres", "PDP Internal Res", base.DEC, nil, 0xff)
M.bus_voltage_field = ProtoField.uint8("can.frc.ctre.pdp.voltage", "PDP Voltage", base.DEC, nil, 0xff)
M.temp_field = ProtoField.uint8("can.frc.ctre.pdp.temp", "PDP Temperature", base.DEC, nil, 0xff)

function M.insert(t)
  table.insert(t, M.chan13_h8_field)
  table.insert(t, M.chan14_h6_field)
  table.insert(t, M.chan13_l2_field)
  table.insert(t, M.chan15_h4_field)
  table.insert(t, M.chan14_l4_field)
  table.insert(t, M.chan16_h2_field)
  table.insert(t, M.chan15_l6_field)
  table.insert(t, M.chan16_l8_field)
  table.insert(t, M.internalres_field)
  table.insert(t, M.bus_voltage_field)
  table.insert(t, M.temp_field)

  M.chan13_h8 = Field.new("can.frc.ctre.pdp.chan13_h8")
  M.chan14_h6 = Field.new("can.frc.ctre.pdp.chan14_h6")
  M.chan13_l2 = Field.new("can.frc.ctre.pdp.chan13_l2")
  M.chan15_h4 = Field.new("can.frc.ctre.pdp.chan15_h4")
  M.chan14_l4 = Field.new("can.frc.ctre.pdp.chan14_l4")
  M.chan16_h2 = Field.new("can.frc.ctre.pdp.chan16_h2")
  M.chan15_l6 = Field.new("can.frc.ctre.pdp.chan15_l6")
  M.chan16_l8 = Field.new("can.frc.ctre.pdp.chan16_l8")
  M.internalres = Field.new("can.frc.ctre.pdp.internalres")
  M.voltage = Field.new("can.frc.ctre.pdp.voltage")
  M.temp = Field.new("can.frc.ctre.pdp.temp")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add(M.chan13_h8_field, buffer:range(8,1))
  subtree:add(M.chan14_h6_field, buffer:range(9,1))
  subtree:add(M.chan13_l2_field, buffer:range(9,1))
  subtree:add(M.chan15_h4_field, buffer:range(10,1))
  subtree:add(M.chan14_l4_field, buffer:range(10,1))
  subtree:add(M.chan16_h2_field, buffer:range(11,1))
  subtree:add(M.chan15_l6_field, buffer:range(11,1))
  subtree:add(M.chan16_l8_field, buffer:range(12,1))
  subtree:add(M.internalres_field, buffer:range(13,1))
  subtree:add(M.bus_voltage_field, buffer:range(14,1))
  subtree:add(M.temp_field, buffer:range(15,1))

  subtree:add("current_channel_13 (A):", currentA(bit.bor(bit.lshift(M.chan13_h8()(), 2), M.chan13_l2()())))
  subtree:add("current_channel_14 (A):", currentA(bit.bor(bit.lshift(M.chan14_h6()(), 4), M.chan14_l4()())))
  subtree:add("current_channel_15 (A):", currentA(bit.bor(bit.lshift(M.chan15_h4()(), 6), M.chan15_l6()())))
  subtree:add("current_channel_16 (A):", currentA(bit.bor(bit.lshift(M.chan16_h2()(), 8), M.chan16_l8()())))
  subtree:add("internal_resistance (mOhm):", M.internalres()())
  subtree:add("bus_voltage (V):", voltage(M.voltage()()))
  subtree:add("temperature (C):", temperatureC(M.temp()()))
end

return M
