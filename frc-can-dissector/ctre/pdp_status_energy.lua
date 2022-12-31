-- CTRE PDP STATUS_ENERGY

local M = {}

M.TmeasMs_likelywillbe20ms__field = ProtoField.uint8("can.frc.ctre.pdp.TmeasMs_likelywillbe20ms_", "PDP TmeasMs", base.DEC, nil, 0xff)
M.TotalCurrent_125mAperunit_h8_field = ProtoField.uint8("can.frc.ctre.pdp.TotalCurrent_125mAperunit_h8", "PDP Total Current h8", base.DEC, nil, 0xff)
M.Power_125mWperunit_h4_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_h4", "PDP Power h4", base.DEC, nil, 0xf0)
M.TotalCurrent_125mAperunit_l4_field = ProtoField.uint8("can.frc.ctre.pdp.TotalCurrent_125mAperunit_l4", "PDP Total Current l4", base.DEC, nil, 0x0f)
M.Power_125mWperunit_m8_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_m8", "PDP Power m8", base.DEC, nil, 0xff)
M.Energy_125mWPerUnitXTmeas_h4_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_h4", "PDP Energy h4", base.DEC, nil, 0xf0)
M.Power_125mWperunit_l4_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_l4", "PDP Power l4", base.DEC, nil, 0x0f)
M.Energy_125mWPerUnitXTmeas_mh8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_mh8", "PDP Energy mh8", base.DEC, nil, 0xff)
M.Energy_125mWPerUnitXTmeas_ml8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_ml8", "PDP Energy ml8", base.DEC, nil, 0xff)
M.Energy_125mWPerUnitXTmeas_l8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_l8", "PDP Energy l8", base.DEC, nil, 0xff)

function M.insert(t)
  table.insert(t, M.TmeasMs_likelywillbe20ms__field)
  table.insert(t, M.TotalCurrent_125mAperunit_h8_field)
  table.insert(t, M.Power_125mWperunit_h4_field)
  table.insert(t, M.TotalCurrent_125mAperunit_l4_field)
  table.insert(t, M.Power_125mWperunit_m8_field)
  table.insert(t, M.Energy_125mWPerUnitXTmeas_h4_field)
  table.insert(t, M.Power_125mWperunit_l4_field)
  table.insert(t, M.Energy_125mWPerUnitXTmeas_mh8_field)
  table.insert(t, M.Energy_125mWPerUnitXTmeas_ml8_field)
  table.insert(t, M.Energy_125mWPerUnitXTmeas_l8_field)

  M.TmeasMs_likelywillbe20ms_ = Field.new("can.frc.ctre.pdp.TmeasMs_likelywillbe20ms_")
  M.TotalCurrent_125mAperunit_h8 = Field.new("can.frc.ctre.pdp.TotalCurrent_125mAperunit_h8")
  M.Power_125mWperunit_h4 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_h4")
  M.TotalCurrent_125mAperunit_l4 = Field.new("can.frc.ctre.pdp.TotalCurrent_125mAperunit_l4")
  M.Power_125mWperunit_m8 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_m8")
  M.Energy_125mWPerUnitXTmeas_h4 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_h4")
  M.Power_125mWperunit_l4 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_l4")
  M.Energy_125mWPerUnitXTmeas_mh8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_mh8")
  M.Energy_125mWPerUnitXTmeas_ml8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_ml8")
  M.Energy_125mWPerUnitXTmeas_l8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_l8")
end

function M.dissect(buffer, pinfo, subtree)
  -- raw
  subtree:add(M.TmeasMs_likelywillbe20ms__field, buffer:range(8,1))
  subtree:add(M.TotalCurrent_125mAperunit_h8_field, buffer:range(9,1))
  subtree:add(M.Power_125mWperunit_h4_field, buffer:range(10,1))
  subtree:add(M.TotalCurrent_125mAperunit_l4_field, buffer:range(10,1))
  subtree:add(M.Power_125mWperunit_m8_field, buffer:range(11,1))
  subtree:add(M.Energy_125mWPerUnitXTmeas_h4_field, buffer:range(12,1))
  subtree:add(M.Power_125mWperunit_l4_field, buffer:range(12,1))
  subtree:add(M.Energy_125mWPerUnitXTmeas_mh8_field, buffer:range(13,1))
  subtree:add(M.Energy_125mWPerUnitXTmeas_ml8_field, buffer:range(14,1))
  subtree:add(M.Energy_125mWPerUnitXTmeas_l8_field, buffer:range(15,1))

  -- cooked
  raw = M.Energy_125mWPerUnitXTmeas_h4()()
  raw = bit.lshift(raw, 8)
  raw = bit.bor(raw, M.Energy_125mWPerUnitXTmeas_mh8()())
  raw = bit.lshift(raw, 8)
  raw = bit.bor(raw, M.Energy_125mWPerUnitXTmeas_ml8()())
  raw = bit.lshift(raw, 8)
  raw = bit.bor(raw, M.Energy_125mWPerUnitXTmeas_l8()())
  energyJoules = 0.125 * raw
  energyJoules = energyJoules * 0.001
  energyJoules = energyJoules * M.TmeasMs_likelywillbe20ms_()()
  subtree:add("total_energy (J):", energyJoules)

  raw = M.TotalCurrent_125mAperunit_h8()()
  raw = bit.lshift(raw, 4)
  raw = bit.bor(raw, M.TotalCurrent_125mAperunit_l4()())
  currentAmps = 0.125 * raw
  subtree:add("total_current (A):", currentAmps)

  raw = M.Power_125mWperunit_h4()()
  raw = bit.lshift(raw, 8)
  raw = bit.bor(raw, M.Power_125mWperunit_m8()())
  raw = bit.lshift(raw, 8)
  raw = bit.bor(raw, M.Power_125mWperunit_l4()())
  powerWatts = 0.125 * raw
  subtree:add("total_Power (W):", powerWatts)
end


return M
