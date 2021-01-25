import chisel3._

class RamPort(implicit val conf:Config) extends Bundle {
  val readData = Input(UInt(conf.ramDataWidth.W))

  val addr = Output(UInt(conf.ramAddrWidth.W))
  val writeData = Output(UInt(conf.ramDataWidth.W))
  val writeEnable = Output(UInt(conf.ramDataWidth.W/8))

  override def cloneType: this.type = new RamPort()(conf).asInstanceOf[this.type]
}

class RomPort(implicit val conf:Config) extends Bundle {
  val data = Input(UInt(conf.romDataWidth.W))

  val addr = Output(UInt(conf.romAddrWidth.W))

  override def cloneType: this.type = new RomPort()(conf).asInstanceOf[this.type]
}