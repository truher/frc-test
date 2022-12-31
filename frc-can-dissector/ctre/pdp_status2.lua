-- CTRE PDP Status2

function currentA(x)
  return tostring(x) * 0.125
end

local M = {}

-- CTRE PDP STATUS2
M.chan7_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan7_h8", "PDP Chan7_h8", base.DEC, nil, 0xff)
M.chan8_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan8_h6", "PDP Chan8_h6", base.DEC, nil, 0xfc)
M.chan7_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan7_l2", "PDP Chan7_l2", base.DEC, nil, 0x03)
M.chan9_h4_field = ProtoField.uint8("can.frc.ctre.pdp.chan9_h4", "PDP Chan9_h4", base.DEC, nil, 0xf0)
M.chan8_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan8_l4", "PDP Chan8_l4", base.DEC, nil, 0x0f)
M.chan10_h2_field = ProtoField.uint8("can.frc.ctre.pdp.chan10_h2", "PDP Chan10_h2", base.DEC, nil, 0xc0)
M.chan9_l6_field = ProtoField.uint8("can.frc.ctre.pdp.chan9_l6", "PDP Chan9_l6", base.DEC, nil, 0x3f)
M.chan10_l8_field = ProtoField.uint8("can.frc.ctre.pdp.chan10_l8", "PDP Chan10_l8", base.DEC, nil, 0xff)
M.chan11_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan11_h8", "PDP Chan11_h8", base.DEC, nil, 0xff)
M.chan12_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan12_h6", "PDP Chan12_h6", base.DEC, nil, 0xfc)
M.chan11_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan11_l2", "PDP Chan11_l2", base.DEC, nil, 0x03)
M.chan12_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan12_l4", "PDP Chan12_l4", base.DEC, nil, 0x0f)

function M.insert(t)
  table.insert(t, M.chan7_h8_field)
  table.insert(t, M.chan8_h6_field)
  table.insert(t, M.chan7_l2_field)
  table.insert(t, M.chan9_h4_field)
  table.insert(t, M.chan8_l4_field)
  table.insert(t, M.chan10_h2_field)
  table.insert(t, M.chan9_l6_field)
  table.insert(t, M.chan10_l8_field)
  table.insert(t, M.chan11_h8_field)
  table.insert(t, M.chan12_h6_field)
  table.insert(t, M.chan11_l2_field)
  table.insert(t, M.chan12_l4_field)


  M.chan7_h8 = Field.new("can.frc.ctre.pdp.chan7_h8")
  M.chan8_h6 = Field.new("can.frc.ctre.pdp.chan8_h6")
  M.chan7_l2 = Field.new("can.frc.ctre.pdp.chan7_l2")
  M.chan9_h4 = Field.new("can.frc.ctre.pdp.chan9_h4")
  M.chan8_l4 = Field.new("can.frc.ctre.pdp.chan8_l4")
  M.chan10_h2 = Field.new("can.frc.ctre.pdp.chan10_h2")
  M.chan9_l6 = Field.new("can.frc.ctre.pdp.chan9_l6")
  M.chan10_l8 = Field.new("can.frc.ctre.pdp.chan10_l8")
  M.chan11_h8 = Field.new("can.frc.ctre.pdp.chan11_h8")
  M.chan12_h6 = Field.new("can.frc.ctre.pdp.chan12_h6")
  M.chan11_l2 = Field.new("can.frc.ctre.pdp.chan11_l2")
  M.chan12_l4 = Field.new("can.frc.ctre.pdp.chan12_l4")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add(M.chan7_h8_field, buffer:range(8,1))
  subtree:add(M.chan8_h6_field, buffer:range(9,1))
  subtree:add(M.chan7_l2_field, buffer:range(9,1))
  subtree:add(M.chan9_h4_field, buffer:range(10,1))
  subtree:add(M.chan8_l4_field, buffer:range(10,1))
  subtree:add(M.chan10_h2_field, buffer:range(11,1))
  subtree:add(M.chan9_l6_field, buffer:range(11,1))
  subtree:add(M.chan10_l8_field, buffer:range(12,1))
  subtree:add(M.chan11_h8_field, buffer:range(13,1))
  subtree:add(M.chan12_h6_field, buffer:range(14,1))
  subtree:add(M.chan11_l2_field, buffer:range(14,1))
  subtree:add(M.chan12_l4_field, buffer:range(15,1))

  subtree:add("current_channel_7 (A):", currentA(bit.bor(bit.lshift(M.chan7_h8()(), 2), M.chan7_l2()())))
  subtree:add("current_channel_8 (A):", currentA(bit.bor(bit.lshift(M.chan8_h6()(), 4), M.chan8_l4()())))
  subtree:add("current_channel_9 (A):", currentA(bit.bor(bit.lshift(M.chan9_h4()(), 6), M.chan9_l6()())))
  subtree:add("current_channel_10 (A):", currentA(bit.bor(bit.lshift(M.chan10_h2()(), 8), M.chan10_l8()())))
  subtree:add("current_channel_11 (A):", currentA(bit.bor(bit.lshift(M.chan11_h8()(), 2), M.chan11_l2()())))
  subtree:add("current_channel_12 (A):", currentA(bit.bor(bit.lshift(M.chan12_h6()(), 4), M.chan12_l4()())))
end

return M
