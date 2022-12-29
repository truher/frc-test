can_protocol = Proto("FRC_CAN", "FRC CAN Protocol")
can_protocol.fields = {}

garbage_field = ProtoField.int32("blarg1", "blarg2", base.DEC)

function can_protocol.dissector(buffer, pinfo, tree)
  length = buffer:len()
  if length == 0 then return end
  pinfo.cols.protocol = can_protocol.name
  local subtree = tree:add(can_protocol, buffer(), "FRC CAN Protocol Data")
  subtree:add_le(garbage_field, 2)
end

local wcap = DissectorTable.get("wtap_encap")
wcap:add(109, can_protocol)
