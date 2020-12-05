import chisel3._

class ForwardController(implicit val conf:Config) extends Module{
  val io = IO(new Bundle{
    val rs = Input(UInt(conf.regBit.W))
    val data = Input(UInt(conf.dataWidth.W))
    val exWrite = Input(new RegFileWritee())
    val memWrite = Input(new RegFileWrite())
    val out = Output(UInt(conf.dataWidth.W))
  })
  when(io.rs === io.exWrite.rd && io.exWrite.writeEnable) {
    //Forward from EX
    io.out := io.exWrite.writeData
  }.elsewhen(io.rs === io.memWrite.rd && io.memWrite.writeEnable) {
    //Forward from MEM
    io.out := io.memWrite.writeData
  }.otherwise{
    io.out := io.data
  }
}