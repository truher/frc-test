-- FRC CAN Dissector

-- See https://docs.wpilib.org/en/stable/docs/software/can-devices/can-addressing.html
-- See https://github.com/carlosgj/FRC-CAN-Wireshark

device_types = {
  { 0,  0, "Broadcast Messages"},
  { 1,  1, "Robot Controller"},
  { 2,  2, "Motor Controller"},
  { 3,  3, "Relay Controller"},
  { 4,  4, "Gyro Sensor"},
  { 5,  5, "Accelerometer"},
  { 6,  6, "Ultrasonic Sensor"},
  { 7,  7, "Gear Tooth Sensor"},
  { 8,  8, "Power Distribution Module"},
  { 9,  9, "Pneumatics Controller"},
  {10, 10, "Miscellaneous"},
  {11, 11, "IO Breakout"},
  {12, 30, "Reserved"},
  {31, 31, "Firmware Update"}
}

manufacturers = {
  { 0,   0, "Broadcast" },
  { 1,   1, "NI" },
  { 2,   2, "Luminary Micro" },
  { 3,   3, "DEKA" },
  { 4,   4, "CTR Electronics" },
  { 5,   5, "REV Robotics" },
  { 6,   6, "Grapple" },
  { 7,   7, "MindSensors" },
  { 8,   8, "Team Use" },
  { 9,   9, "Kauai Labs" },
  {10,  10, "Copperforge" },
  {11,  11, "Playing With Fusion" },
  {12,  12, "Studica" },
  {13, 255, "Reserved" }
}

can_protocol = Proto("FRC_CAN", "FRC CAN Protocol")

-- See linux/can.h for these flags
eff_field = ProtoField.bool("can.eff_flag", "EFF flag", 32, nil, 0x80000000) -- extended frame (29 bit)
rtr_field = ProtoField.bool("can.rtr_flag", "RTR flag", 32, nil, 0x40000000) -- remote frame
err_field = ProtoField.bool("can.err_flag", "ERR flag", 32, nil, 0x20000000) -- error

device_type_field = ProtoField.uint32("can.frc.type", "Device Type", base.RANGE_STRING, device_types, 0x1f000000)
manufacturer_field = ProtoField.uint32("can.frc.mfr", "Manufacturer", base.RANGE_STRING, manufacturers, 0x00ff0000)
api_class_field = ProtoField.uint32("can.frc.api_class", "API Class", base.DEC, nil, 0x0000fc00)
api_index_field = ProtoField.uint32("can.frc.api_index", "API Index", base.DEC, nil, 0x000003c0)
device_number_field = ProtoField.uint32("can.frc.device_number", "Device Number", base.DEC, nil, 0x0000003f)

id_field = ProtoField.uint32("can.id", "CAN ID", base.HEX, nil, 0x1fffffff)
length_field = ProtoField.uint8("can.data_length", "data length", base.DEC, nil, nil)
pad_field = ProtoField.bytes("can.padding", "padding", base.NONE, "padding")
data_field = ProtoField.bytes("can.datafield", "data field", base.NONE)



-- See https://github.com/wpilibsuite/allwpilib/pull/1081/files

-- CTRE PDP STATUS1
-- chan1_h8:8;
-- chan2_h6:6;
-- chan1_l2:2;
-- chan3_h4:4;
-- chan2_l4:4;
-- chan4_h2:2;
-- chan3_l6:6;
-- chan4_l8:8;
-- chan5_h8:8;
-- chan6_h6:6;
-- chan5_l2:2;
-- reserved4:4;
-- chan6_l4:4;

ctre_pdp_chan1_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan1_h8", "PDP Chan1_h8", base.DEC, nil, 0xff)
ctre_pdp_chan2_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan2_h6", "PDP Chan2_h6", base.DEC, nil, 0xfc)
ctre_pdp_chan1_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan1_l2", "PDP Chan1_l2", base.DEC, nil, 0x03)
ctre_pdp_chan3_h4_field = ProtoField.uint8("can.frc.ctre.pdp.chan3_h4", "PDP Chan3_h4", base.DEC, nil, 0xf0)
ctre_pdp_chan2_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan2_l4", "PDP Chan2_l4", base.DEC, nil, 0x0f)
ctre_pdp_chan4_h2_field = ProtoField.uint8("can.frc.ctre.pdp.chan4_h2", "PDP Chan4_h2", base.DEC, nil, 0xc0)
ctre_pdp_chan3_l6_field = ProtoField.uint8("can.frc.ctre.pdp.chan3_l6", "PDP Chan3_l6", base.DEC, nil, 0x3f)
ctre_pdp_chan4_l8_field = ProtoField.uint8("can.frc.ctre.pdp.chan4_l8", "PDP Chan4_l8", base.DEC, nil, 0xff)
ctre_pdp_chan5_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan5_h8", "PDP Chan5_h8", base.DEC, nil, 0xff)
ctre_pdp_chan6_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan6_h6", "PDP Chan6_h6", base.DEC, nil, 0xfc)
ctre_pdp_chan5_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan5_l2", "PDP Chan5_l2", base.DEC, nil, 0x03)
ctre_pdp_chan6_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan6_l4", "PDP Chan6_l4", base.DEC, nil, 0x0f)

-- CTRE PDP STATUS2
-- chan7_h8:8;
-- chan8_h6:6;
-- chan7_l2:2;
-- chan9_h4:4;
-- chan8_l4:4;
-- chan10_h2:2;
-- chan9_l6:6;
-- chan10_l8:8;
-- chan11_h8:8;
-- chan12_h6:6;
-- chan11_l2:2;
-- reserved4:4;
-- chan12_l4:4;

ctre_pdp_chan7_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan7_h8", "PDP Chan7_h8", base.DEC, nil, 0xff)
ctre_pdp_chan8_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan8_h6", "PDP Chan8_h6", base.DEC, nil, 0xfc)
ctre_pdp_chan7_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan7_l2", "PDP Chan7_l2", base.DEC, nil, 0x03)
ctre_pdp_chan9_h4_field = ProtoField.uint8("can.frc.ctre.pdp.chan9_h4", "PDP Chan9_h4", base.DEC, nil, 0xf0)
ctre_pdp_chan8_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan8_l4", "PDP Chan8_l4", base.DEC, nil, 0x0f)
ctre_pdp_chan10_h2_field = ProtoField.uint8("can.frc.ctre.pdp.chan10_h2", "PDP Chan10_h2", base.DEC, nil, 0xc0)
ctre_pdp_chan9_l6_field = ProtoField.uint8("can.frc.ctre.pdp.chan9_l6", "PDP Chan9_l6", base.DEC, nil, 0x3f)
ctre_pdp_chan10_l8_field = ProtoField.uint8("can.frc.ctre.pdp.chan10_l8", "PDP Chan10_l8", base.DEC, nil, 0xff)
ctre_pdp_chan11_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan11_h8", "PDP Chan11_h8", base.DEC, nil, 0xff)
ctre_pdp_chan12_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan12_h6", "PDP Chan12_h6", base.DEC, nil, 0xfc)
ctre_pdp_chan11_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan11_l2", "PDP Chan11_l2", base.DEC, nil, 0x03)
ctre_pdp_chan12_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan12_l4", "PDP Chan12_l4", base.DEC, nil, 0x0f)


-- CTRE PDP STATUS3
-- chan13_h8:8;
-- chan14_h6:6;
-- chan13_l2:2;
-- chan15_h4:4;
-- chan14_l4:4;
-- chan16_h2:2;
-- chan15_l6:6;
-- chan16_l8:8;
-- internalResBattery_mOhms:8;
-- busVoltage:8;
-- temp:8;

ctre_pdp_chan13_h8_field = ProtoField.uint8("can.frc.ctre.pdp.chan13_h8", "PDP Chan13_h8", base.DEC, nil, 0xff)
ctre_pdp_chan14_h6_field = ProtoField.uint8("can.frc.ctre.pdp.chan14_h6", "PDP Chan14_h6", base.DEC, nil, 0xfc)
ctre_pdp_chan13_l2_field = ProtoField.uint8("can.frc.ctre.pdp.chan13_l2", "PDP Chan13_l2", base.DEC, nil, 0x03)
ctre_pdp_chan15_h4_field = ProtoField.uint8("can.frc.ctre.pdp.chan15_h4", "PDP Chan15_h4", base.DEC, nil, 0xf0)
ctre_pdp_chan14_l4_field = ProtoField.uint8("can.frc.ctre.pdp.chan14_l4", "PDP Chan14_l4", base.DEC, nil, 0x0f)
ctre_pdp_chan16_h2_field = ProtoField.uint8("can.frc.ctre.pdp.chan16_h2", "PDP Chan16_h2", base.DEC, nil, 0xc0)
ctre_pdp_chan15_l6_field = ProtoField.uint8("can.frc.ctre.pdp.chan15_l6", "PDP Chan15_l6", base.DEC, nil, 0x3f)
ctre_pdp_chan16_l8_field = ProtoField.uint8("can.frc.ctre.pdp.chan16_l8", "PDP Chan16_l8", base.DEC, nil, 0xff)

ctre_pdp_internalres_field = ProtoField.uint8("can.frc.ctre.pdp.internalres", "PDP Internal Res", base.DEC, nil, 0xff)
ctre_pdp_bus_voltage_field = ProtoField.uint8("can.frc.ctre.pdp.voltage", "PDP Voltage", base.DEC, nil, 0xff)
ctre_pdp_temp_field = ProtoField.uint8("can.frc.ctre.pdp.temp", "PDP Temperature", base.DEC, nil, 0xff)

-- CTRE PDP STATUS_ENERGY

-- TmeasMs_likelywillbe20ms_:8;
-- TotalCurrent_125mAperunit_h8:8;
-- Power_125mWperunit_h4:4;
-- TotalCurrent_125mAperunit_l4:4;
-- Power_125mWperunit_m8:8;
-- Energy_125mWPerUnitXTmeas_h4:4;
-- Power_125mWperunit_l4:4;
-- Energy_125mWPerUnitXTmeas_mh8:8;
-- Energy_125mWPerUnitXTmeas_ml8:8;
-- Energy_125mWPerUnitXTmeas_l8:8;

ctre_pdp_TmeasMs_likelywillbe20ms__field = ProtoField.uint8("can.frc.ctre.pdp.TmeasMs_likelywillbe20ms_", "PDP TmeasMs", base.DEC, nil, 0xff)
ctre_pdp_TotalCurrent_125mAperunit_h8_field = ProtoField.uint8("can.frc.ctre.pdp.TotalCurrent_125mAperunit_h8", "PDP Total Current h8", base.DEC, nil, 0xff)
ctre_pdp_Power_125mWperunit_h4_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_h4", "PDP Power h4", base.DEC, nil, 0xf0)
ctre_pdp_TotalCurrent_125mAperunit_l4_field = ProtoField.uint8("can.frc.ctre.pdp.TotalCurrent_125mAperunit_l4", "PDP Total Current l4", base.DEC, nil, 0x0f)
ctre_pdp_Power_125mWperunit_m8_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_m8", "PDP Power m8", base.DEC, nil, 0xff)
ctre_pdp_Energy_125mWPerUnitXTmeas_h4_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_h4", "PDP Energy h4", base.DEC, nil, 0xf0)
ctre_pdp_Power_125mWperunit_l4_field = ProtoField.uint8("can.frc.ctre.pdp.Power_125mWperunit_l4", "PDP Power l4", base.DEC, nil, 0x0f)
ctre_pdp_Energy_125mWPerUnitXTmeas_mh8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_mh8", "PDP Energy mh8", base.DEC, nil, 0xff)
ctre_pdp_Energy_125mWPerUnitXTmeas_ml8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_ml8", "PDP Energy ml8", base.DEC, nil, 0xff)
ctre_pdp_Energy_125mWPerUnitXTmeas_l8_field = ProtoField.uint8("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_l8", "PDP Energy l8", base.DEC, nil, 0xff)

can_protocol.fields = {
  eff_field,
  rtr_field,
  err_field,
  device_type_field,
  manufacturer_field,
  api_class_field,
  api_index_field,
  device_number_field,
  id_field,
  length_field,
  pad_field,
  data_field,

  ctre_pdp_chan1_h8_field,
  ctre_pdp_chan2_h6_field,
  ctre_pdp_chan1_l2_field,
  ctre_pdp_chan3_h4_field,
  ctre_pdp_chan2_l4_field,
  ctre_pdp_chan4_h2_field,
  ctre_pdp_chan3_l6_field,
  ctre_pdp_chan4_l8_field,
  ctre_pdp_chan5_h8_field,
  ctre_pdp_chan6_h6_field,
  ctre_pdp_chan5_l2_field,
  ctre_pdp_chan6_l4_field,

  ctre_pdp_chan7_h8_field,
  ctre_pdp_chan8_h6_field,
  ctre_pdp_chan7_l2_field,
  ctre_pdp_chan9_h4_field,
  ctre_pdp_chan8_l4_field,
  ctre_pdp_chan10_h2_field,
  ctre_pdp_chan9_l6_field,
  ctre_pdp_chan10_l8_field,
  ctre_pdp_chan11_h8_field,
  ctre_pdp_chan12_h6_field,
  ctre_pdp_chan11_l2_field,
  ctre_pdp_chan12_l4_field,

  ctre_pdp_chan13_h8_field,
  ctre_pdp_chan14_h6_field,
  ctre_pdp_chan13_l2_field,
  ctre_pdp_chan15_h4_field,
  ctre_pdp_chan14_l4_field,
  ctre_pdp_chan16_h2_field,
  ctre_pdp_chan15_l6_field,
  ctre_pdp_chan16_l8_field,
  ctre_pdp_internalres_field,
  ctre_pdp_bus_voltage_field,
  ctre_pdp_temp_field,

  ctre_pdp_TmeasMs_likelywillbe20ms__field,
  ctre_pdp_TotalCurrent_125mAperunit_h8_field,
  ctre_pdp_Power_125mWperunit_h4_field,
  ctre_pdp_TotalCurrent_125mAperunit_l4_field,
  ctre_pdp_Power_125mWperunit_m8_field,
  ctre_pdp_Energy_125mWPerUnitXTmeas_h4_field,
  ctre_pdp_Power_125mWperunit_l4_field,
  ctre_pdp_Energy_125mWPerUnitXTmeas_mh8_field,
  ctre_pdp_Energy_125mWPerUnitXTmeas_ml8_field,
  ctre_pdp_Energy_125mWPerUnitXTmeas_l8_field


}

-- fields for the parsed values
can_frc_type = Field.new("can.frc.type")
can_frc_mfr = Field.new("can.frc.mfr")
can_frc_api_class = Field.new("can.frc.api_class")
can_frc_api_index = Field.new("can.frc.api_index")

can_frc_ctre_pdp_chan1_h8 = Field.new("can.frc.ctre.pdp.chan1_h8")
can_frc_ctre_pdp_chan2_h6 = Field.new("can.frc.ctre.pdp.chan2_h6")
can_frc_ctre_pdp_chan1_l2 = Field.new("can.frc.ctre.pdp.chan1_l2")
can_frc_ctre_pdp_chan3_h4 = Field.new("can.frc.ctre.pdp.chan3_h4")
can_frc_ctre_pdp_chan2_l4 = Field.new("can.frc.ctre.pdp.chan2_l4")
can_frc_ctre_pdp_chan4_h2 = Field.new("can.frc.ctre.pdp.chan4_h2")
can_frc_ctre_pdp_chan3_l6 = Field.new("can.frc.ctre.pdp.chan3_l6")
can_frc_ctre_pdp_chan4_l8 = Field.new("can.frc.ctre.pdp.chan4_l8")
can_frc_ctre_pdp_chan5_h8 = Field.new("can.frc.ctre.pdp.chan5_h8")
can_frc_ctre_pdp_chan6_h6 = Field.new("can.frc.ctre.pdp.chan6_h6")
can_frc_ctre_pdp_chan5_l2 = Field.new("can.frc.ctre.pdp.chan5_l2")
can_frc_ctre_pdp_chan6_l4 = Field.new("can.frc.ctre.pdp.chan6_l4")


can_frc_ctre_pdp_chan7_h8 = Field.new("can.frc.ctre.pdp.chan7_h8")
can_frc_ctre_pdp_chan8_h6 = Field.new("can.frc.ctre.pdp.chan8_h6")
can_frc_ctre_pdp_chan7_l2 = Field.new("can.frc.ctre.pdp.chan7_l2")
can_frc_ctre_pdp_chan9_h4 = Field.new("can.frc.ctre.pdp.chan9_h4")
can_frc_ctre_pdp_chan8_l4 = Field.new("can.frc.ctre.pdp.chan8_l4")
can_frc_ctre_pdp_chan10_h2 = Field.new("can.frc.ctre.pdp.chan10_h2")
can_frc_ctre_pdp_chan9_l6 = Field.new("can.frc.ctre.pdp.chan9_l6")
can_frc_ctre_pdp_chan10_l8 = Field.new("can.frc.ctre.pdp.chan10_l8")
can_frc_ctre_pdp_chan11_h8 = Field.new("can.frc.ctre.pdp.chan11_h8")
can_frc_ctre_pdp_chan12_h6 = Field.new("can.frc.ctre.pdp.chan12_h6")
can_frc_ctre_pdp_chan11_l2 = Field.new("can.frc.ctre.pdp.chan11_l2")
can_frc_ctre_pdp_chan12_l4 = Field.new("can.frc.ctre.pdp.chan12_l4")




can_frc_ctre_pdp_chan13_h8 = Field.new("can.frc.ctre.pdp.chan13_h8")
can_frc_ctre_pdp_chan14_h6 = Field.new("can.frc.ctre.pdp.chan14_h6")
can_frc_ctre_pdp_chan13_l2 = Field.new("can.frc.ctre.pdp.chan13_l2")
can_frc_ctre_pdp_chan15_h4 = Field.new("can.frc.ctre.pdp.chan15_h4")
can_frc_ctre_pdp_chan14_l4 = Field.new("can.frc.ctre.pdp.chan14_l4")
can_frc_ctre_pdp_chan16_h2 = Field.new("can.frc.ctre.pdp.chan16_h2")
can_frc_ctre_pdp_chan15_l6 = Field.new("can.frc.ctre.pdp.chan15_l6")
can_frc_ctre_pdp_chan16_l8 = Field.new("can.frc.ctre.pdp.chan16_l8")
can_frc_ctre_pdp_internalres = Field.new("can.frc.ctre.pdp.internalres")
can_frc_ctre_pdp_voltage = Field.new("can.frc.ctre.pdp.voltage")
can_frc_ctre_pdp_temp = Field.new("can.frc.ctre.pdp.temp")

can_frc_ctre_pdp_TmeasMs_likelywillbe20ms_ = Field.new("can.frc.ctre.pdp.TmeasMs_likelywillbe20ms_")
can_frc_ctre_pdp_TotalCurrent_125mAperunit_h8 = Field.new("can.frc.ctre.pdp.TotalCurrent_125mAperunit_h8")
can_frc_ctre_pdp_Power_125mWperunit_h4 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_h4")
can_frc_ctre_pdp_TotalCurrent_125mAperunit_l4 = Field.new("can.frc.ctre.pdp.TotalCurrent_125mAperunit_l4")
can_frc_ctre_pdp_Power_125mWperunit_m8 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_m8")
can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_h4 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_h4")
can_frc_ctre_pdp_Power_125mWperunit_l4 = Field.new("can.frc.ctre.pdp.Power_125mWperunit_l4")
can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_mh8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_mh8")
can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_ml8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_ml8")
can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_l8 = Field.new("can.frc.ctre.pdp.Energy_125mWPerUnitXTmeas_l8")

-- See https://github.com/CrossTheRoadElec/deprecated-HERO-SDK/blob/master/CTRE/LowLevel_Pcm.cs
-- note the delicious use of tostring() here; lua coerces the string to a number
function voltage(x)
  return tostring(x) * 0.05 + 4.0
end

-- See https://github.com/wpilibsuite/allwpilib/pull/1081/files
function temperatureC(x)
  return tostring(x) * 1.03250836957542 - 67.8564500484966
end

-- See https://github.com/wpilibsuite/allwpilib/pull/1081/files
function currentA(x)
  return tostring(x) * 0.125
end

-- Tvb buffer, see lua_module_Tvb.html
function can_protocol.dissector(buffer, pinfo, tree)
  length = buffer:len()
  if length == 0 then return end

  pinfo.cols.protocol = can_protocol.name
  pinfo.cols.info = buffer:bytes():tohex(true, " ")

  local subtree = tree:add(can_protocol, buffer(), "FRC CAN Protocol Data")
  subtree:add_le(id_field, buffer:range(0,4))

  subtree:add_le(eff_field, buffer:range(0,4)) -- should always be true!
  subtree:add_le(rtr_field, buffer:range(0,4))
  subtree:add_le(err_field, buffer:range(0,4))
  subtree:add_le(device_type_field, buffer:range(0,4))
  subtree:add_le(manufacturer_field, buffer:range(0,4))
  subtree:add_le(api_class_field, buffer:range(0,4))
  subtree:add_le(api_index_field, buffer:range(0,4))
  subtree:add_le(device_number_field, buffer:range(0,4))

  subtree:add(length_field, buffer:range(4,1))
  subtree:add(pad_field, buffer:range(5,3))
  subtree:add(data_field, buffer:range(8))


  subtree:add("can_frc_type:", can_frc_type()())
  subtree:add("can_frc_mfr:", can_frc_mfr()())
  if can_frc_type()() == 8 then -- PDP
    if can_frc_mfr()() == 4 then -- CTRE
      if can_frc_api_class()() == 5 then -- STATUS
        if can_frc_api_index()() == 0 then -- PDP_API_STATUS1

          -- raw
          subtree:add(ctre_pdp_chan1_h8_field, buffer:range(8,1))
          subtree:add(ctre_pdp_chan2_h6_field, buffer:range(9,1))
          subtree:add(ctre_pdp_chan1_l2_field, buffer:range(9,1))
          subtree:add(ctre_pdp_chan3_h4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_chan2_l4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_chan4_h2_field, buffer:range(11,1))
          subtree:add(ctre_pdp_chan3_l6_field, buffer:range(11,1))
          subtree:add(ctre_pdp_chan4_l8_field, buffer:range(12,1))
          subtree:add(ctre_pdp_chan5_h8_field, buffer:range(13,1))
          subtree:add(ctre_pdp_chan6_h6_field, buffer:range(14,1))
          subtree:add(ctre_pdp_chan5_l2_field, buffer:range(14,1))
          subtree:add(ctre_pdp_chan6_l4_field, buffer:range(15,1))

          -- cooked
          subtree:add("can_frc_ctre_pdp_current_channel_1 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan1_h8()(), 2), can_frc_ctre_pdp_chan1_l2()())))
          subtree:add("can_frc_ctre_pdp_current_channel_2 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan2_h6()(), 4), can_frc_ctre_pdp_chan2_l4()())))
          subtree:add("can_frc_ctre_pdp_current_channel_3 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan3_h4()(), 6), can_frc_ctre_pdp_chan3_l6()())))
          subtree:add("can_frc_ctre_pdp_current_channel_4 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan4_h2()(), 8), can_frc_ctre_pdp_chan4_l8()())))
          subtree:add("can_frc_ctre_pdp_current_channel_5 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan5_h8()(), 2), can_frc_ctre_pdp_chan5_l2()())))
          subtree:add("can_frc_ctre_pdp_current_channel_6 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan6_h6()(), 4), can_frc_ctre_pdp_chan6_l4()())))

        elseif can_frc_api_index()() == 1 then -- PDP_API_STATUS2

          -- raw
          subtree:add(ctre_pdp_chan7_h8_field, buffer:range(8,1))
          subtree:add(ctre_pdp_chan8_h6_field, buffer:range(9,1))
          subtree:add(ctre_pdp_chan7_l2_field, buffer:range(9,1))
          subtree:add(ctre_pdp_chan9_h4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_chan8_l4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_chan10_h2_field, buffer:range(11,1))
          subtree:add(ctre_pdp_chan9_l6_field, buffer:range(11,1))
          subtree:add(ctre_pdp_chan10_l8_field, buffer:range(12,1))
          subtree:add(ctre_pdp_chan11_h8_field, buffer:range(13,1))
          subtree:add(ctre_pdp_chan12_h6_field, buffer:range(14,1))
          subtree:add(ctre_pdp_chan11_l2_field, buffer:range(14,1))
          subtree:add(ctre_pdp_chan12_l4_field, buffer:range(15,1))

          -- cooked
          subtree:add("can_frc_ctre_pdp_current_channel_7 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan7_h8()(), 2), can_frc_ctre_pdp_chan7_l2()())))
          subtree:add("can_frc_ctre_pdp_current_channel_8 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan8_h6()(), 4), can_frc_ctre_pdp_chan8_l4()())))
          subtree:add("can_frc_ctre_pdp_current_channel_9 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan9_h4()(), 6), can_frc_ctre_pdp_chan9_l6()())))
          subtree:add("can_frc_ctre_pdp_current_channel_10 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan10_h2()(), 8), can_frc_ctre_pdp_chan10_l8()())))
          subtree:add("can_frc_ctre_pdp_current_channel_11 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan11_h8()(), 2), can_frc_ctre_pdp_chan11_l2()())))
          subtree:add("can_frc_ctre_pdp_current_channel_12 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan12_h6()(), 4), can_frc_ctre_pdp_chan12_l4()())))

        elseif can_frc_api_index()() == 2 then -- PDP_API_STATUS3

          -- raw
          subtree:add(ctre_pdp_chan13_h8_field, buffer:range(8,1))    -- ff000000 00000000
          subtree:add(ctre_pdp_chan14_h6_field, buffer:range(9,1))    -- 00fc0000 00000000
          subtree:add(ctre_pdp_chan13_l2_field, buffer:range(9,1))    -- 00030000 00000000
          subtree:add(ctre_pdp_chan15_h4_field, buffer:range(10,1))   -- 0000f000 00000000
          subtree:add(ctre_pdp_chan14_l4_field, buffer:range(10,1))   -- 00000f00 00000000
          subtree:add(ctre_pdp_chan16_h2_field, buffer:range(11,1))   -- 000000c0 00000000
          subtree:add(ctre_pdp_chan15_l6_field, buffer:range(11,1))   -- 0000003f 00000000
          subtree:add(ctre_pdp_chan16_l8_field, buffer:range(12,1))   -- 00000000 ff000000
          subtree:add(ctre_pdp_internalres_field, buffer:range(13,1)) -- 00000000 00ff0000
          subtree:add(ctre_pdp_bus_voltage_field, buffer:range(14,1)) -- 00000000 0000ff00
          subtree:add(ctre_pdp_temp_field, buffer:range(15,1))        -- 00000000 000000ff

          -- cooked
          subtree:add("can_frc_ctre_pdp_current_channel_13 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan13_h8()(), 2), can_frc_ctre_pdp_chan13_l2()())))
          subtree:add("can_frc_ctre_pdp_current_channel_14 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan14_h6()(), 4), can_frc_ctre_pdp_chan14_l4()())))
          subtree:add("can_frc_ctre_pdp_current_channel_15 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan15_h4()(), 6), can_frc_ctre_pdp_chan15_l6()())))
          subtree:add("can_frc_ctre_pdp_current_channel_16 (A):", 
            currentA(bit.bor(bit.lshift(can_frc_ctre_pdp_chan16_h2()(), 8), can_frc_ctre_pdp_chan16_l8()())))
          subtree:add("can_frc_ctre_pdp_internal_resistance (mOhm):", can_frc_ctre_pdp_internalres()())
          subtree:add("can_frc_ctre_pdp_bus_voltage (V):", voltage(can_frc_ctre_pdp_voltage()()))
          subtree:add("can_frc_ctre_pdp_temperature (C):", temperatureC(can_frc_ctre_pdp_temp()()))

        elseif can_frc_api_index()() == 13 then -- PDP_API_STATUS_ENERGY

          -- raw
          subtree:add(ctre_pdp_TmeasMs_likelywillbe20ms__field, buffer:range(8,1))
          subtree:add(ctre_pdp_TotalCurrent_125mAperunit_h8_field, buffer:range(9,1))
          subtree:add(ctre_pdp_Power_125mWperunit_h4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_TotalCurrent_125mAperunit_l4_field, buffer:range(10,1))
          subtree:add(ctre_pdp_Power_125mWperunit_m8_field, buffer:range(11,1))
          subtree:add(ctre_pdp_Energy_125mWPerUnitXTmeas_h4_field, buffer:range(12,1))
          subtree:add(ctre_pdp_Power_125mWperunit_l4_field, buffer:range(12,1))
          subtree:add(ctre_pdp_Energy_125mWPerUnitXTmeas_mh8_field, buffer:range(13,1))
          subtree:add(ctre_pdp_Energy_125mWPerUnitXTmeas_ml8_field, buffer:range(14,1))
          subtree:add(ctre_pdp_Energy_125mWPerUnitXTmeas_l8_field, buffer:range(15,1))

          -- cooked
          raw = can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_h4()()
          raw = bit.lshift(raw, 8)
          raw = bit.bor(raw, can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_mh8()())
          raw = bit.lshift(raw, 8)
          raw = bit.bor(raw, can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_ml8()())
          raw = bit.lshift(raw, 8)
          raw = bit.bor(raw, can_frc_ctre_pdp_Energy_125mWPerUnitXTmeas_l8()())
          energyJoules = 0.125 * raw
          energyJoules = energyJoules * 0.001
          energyJoules = energyJoules * can_frc_ctre_pdp_TmeasMs_likelywillbe20ms_()()
          subtree:add("can_frc_ctre_pdp_total_energy (J):", energyJoules)

          raw = can_frc_ctre_pdp_TotalCurrent_125mAperunit_h8()()
          raw = bit.lshift(raw, 4)
          raw = bit.bor(raw, can_frc_ctre_pdp_TotalCurrent_125mAperunit_l4()())
          currentAmps = 0.125 * raw
          subtree:add("can_frc_ctre_pdp_total_current (A):", currentAmps)

          raw = can_frc_ctre_pdp_Power_125mWperunit_h4()()
          raw = bit.lshift(raw, 8)
          raw = bit.bor(raw, can_frc_ctre_pdp_Power_125mWperunit_m8()())
          raw = bit.lshift(raw, 8)
          raw = bit.bor(raw, can_frc_ctre_pdp_Power_125mWperunit_l4()())
          powerWatts = 0.125 * raw
          subtree:add("can_frc_ctre_pdp_total_Power (W):", powerWatts)

        end
      elseif can_frc_api_class()() == 7 then -- CONTROL
        if can_frc_api_index()() == 0 then -- CONTROL_1
        end
      end
    end
  end

end

local wcap = DissectorTable.get("wtap_encap")
wcap:add(109, can_protocol)
