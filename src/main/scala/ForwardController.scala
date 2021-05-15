import chisel3._

class ForwardControllerPort(implicit val conf:Config) extends Bundle {
  val rs = Input(UInt(conf.regBit.W))
  val data = Input(UInt(conf.dataWidth.W))
  val exWrite = Input(new RegFileWrite())
  val memWrite = Input(new RegFileWrite())
  val out = Output(UInt(conf.dataWidth.W))
}

class ForwardController(implicit val conf:Config) extends Module{
  val io = IO(new ForwardControllerPort)
  when(io.rs === io.exWrite.rd && io.exWrite.rd.orR && io.exWrite.writeEnable) {
    //Forward from EX
    io.out := io.exWrite.writeData
  }.elsewhen(io.rs === io.memWrite.rd && io.memWrite.rd.orR && io.memWrite.writeEnable) {
    //Forward from MEM
    io.out := io.memWrite.writeData
  }.otherwise{
    io.out := io.data
  }
}