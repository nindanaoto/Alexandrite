import chisel3._

class RamPort(implicit val conf:Config) extends Bundle {
  val addr = Input(UInt(conf.ramAddrWidth.W))
  val readData = Output(UInt(conf.ramDataWidth.W))
  val writeData = Input(UInt(conf.ramDataWidth.W))
  val writeEnable = Input(UInt((conf.ramDataWidth/8).W))
}

class RomPort(implicit val conf:Config) extends Bundle {
  val data = Output(UInt(conf.romDataWidth.W))

  val addr = Input(UInt(conf.romAddrWidth.W))
}