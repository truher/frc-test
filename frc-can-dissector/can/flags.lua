-- CAN ID flags
--
-- See linux/can.h

local M = {}

M.eff_field = ProtoField.bool("can.eff_flag", "EFF flag", 32, nil, 0x80000000) -- extended frame (29 bit)
M.rtr_field = ProtoField.bool("can.rtr_flag", "RTR flag", 32, nil, 0x40000000) -- remote frame
M.err_field = ProtoField.bool("can.err_flag", "ERR flag", 32, nil, 0x20000000) -- error

function M.insert(t)
  table.insert(t, M.eff_field)
  table.insert(t, M.rtr_field)
  table.insert(t, M.err_field)
end

function M.dissect(buffer, pinfo, subtree)
  subtree:add_le(M.eff_field, buffer:range(0,4)) -- should always be true!
  subtree:add_le(M.rtr_field, buffer:range(0,4))
  subtree:add_le(M.err_field, buffer:range(0,4))
end

return M
