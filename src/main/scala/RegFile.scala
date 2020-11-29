// riscv-mini

package mini

import chisel3._

class RegisterFileOutPort(implicit val conf:CAHPConfig) extends Bundle{
  val reg = Output(Vec(1<<conf.regBit,UInt(conf.dataWidth.W)))
}

class RegFileIO(implicit val conf:Config)  extends CoreBundle()(p) {
  val raddr1 = Input(UInt(conf.regBit.W))
  val raddr2 = Input(UInt(conf.regBit.W))
  val rdata1 = Output(UInt(conf.dataWidth.W))
  val rdata2 = Output(UInt(conf.dataWidth.W))
  val wen    = Input(Bool())
  val waddr  = Input(UInt(conf.regBit.W))
  val wdata  = Input(UInt(conf.dataWidth.W))

  val regOut = Output(RegisterFileOutPort)
}

class RegFile(implicit val conf:Config) extends Module with CoreParams {
  val io = IO(new RegFileIO)
  val regs = Mem(1<<conf.regBit, UInt(conf.dataWidth.W))
  io.rdata1 := Mux(io.raddr1.orR, regs(io.raddr1), 0.U)
  io.rdata2 := Mux(io.raddr2.orR, regs(io.raddr2), 0.U)
  when(io.wen & io.waddr.orR) {
    regs(io.waddr) := io.wdata
  }

  io.regOut := regs
}