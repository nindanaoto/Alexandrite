import chisel3._

class ReadRamPort(implicit val conf:Config) extends Bundle {
  val addr = Output(UInt(conf.ramAddrWidth.W))
  val readData = Input(UInt(conf.ramDataWidth.W))

  override def cloneType: this.type = new ReadRamPort()(conf).asInstanceOf[this.type]
}

class WriteRamPort(implicit val conf:Config) extends Bundle {

  val writeData = Output(UInt(conf.ramDataWidth.W))
  val writeEnable = Output(UInt((conf.ramDataWidth/8).W))

  override def cloneType: this.type = new WriteRamPort()(conf).asInstanceOf[this.type]
}

class RomPort(implicit val conf:Config) extends Bundle {
  val data = Input(UInt(conf.romDataWidth.W))

  val addr = Output(UInt(conf.romAddrWidth.W))

  override def cloneType: this.type = new RomPort()(conf).asInstanceOf[this.type]
}