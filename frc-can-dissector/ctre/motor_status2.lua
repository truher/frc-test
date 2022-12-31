function currentA(x)
  return tostring(x) * 0.125
end

local M = {}

M.talon_current_h8_field = ProtoField.uint8("can.frc.ctre.talon.current_h8", "Talon Current_h8", base.DEC, nil, 0xff)
M.talon_current_l2_field = ProtoField.uint8("can.frc.ctre.talon.current_l2", "Talon Current_l2", base.DEC, nil, 0xc0)

function M.insert(t)
  table.insert(t, M.talon_current_h8_field)
  table.insert(t, M.talon_current_l2_field)

  M.talon_current_h8 = Field.new("can.frc.ctre.talon.current_h8")
  M.talon_current_l2 = Field.new("can.frc.ctre.talon.current_l2")
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add(M.talon_current_h8_field, buffer:range(10,1))
  subtree:add(M.talon_current_l2_field, buffer:range(9,1))
  subtree:add("Talon Current (A):", currentA(bit.bor(bit.lshift(M.talon_current_h8()(), 2), M.talon_current_l2()())))
end

return M
