// riscv-mini

package mini

import chisel3._

class RegFileRead(implicit val conf:Config) extends Bundle{
  val raddr1 = Input(UInt(conf.regBit.W))
  val raddr2 = Input(UInt(conf.regBit.W))
  val rdata1 = Output(UInt(conf.dataWidth.W))
  val rdata2 = Output(UInt(conf.dataWidth.W)))
}


class RegFileWrite(implicit val conf:Config) extends Bundle{
  val writeEnable = Input(Bool())
  val rd          = Input(UInt(conf.regBit.W))
  val writeData   = Input(UInt(conf.dataWidth.W))
}

class RegisterFileOutPort(implicit val conf:Config) extends Bundle{
  val reg = Output(Vec(1<<conf.regBit,UInt(conf.dataWidth.W)))
}

class RegFileIO(implicit val conf:Config)  extends Bundle {
  val readport = new RegFileRead
  val writeport = new RegFileWrite
  val regOut = new RegisterFileOutPort
}

class RegFile(implicit val conf:Config) extends Module with CoreParams {
  val io = IO(new RegFileIO)
  val regs = Mem(1<<conf.regBit, UInt(conf.dataWidth.W))
  io.rdata1 := Mux(io.raddr1.orR, regs(io.raddr1), 0.U)
  io.rdata2 := Mux(io.raddr2.orR, regs(io.raddr2), 0.U)
  when(io.wen & io.rd.orR) {
    regs(io.rd) := io.wdata
  }

  io.regOut := regs
}