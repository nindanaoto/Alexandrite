// riscv-mini

import chisel3._

class RegFileRead(implicit val conf:Config) extends Bundle{
  val rs1 = Input(UInt(conf.regBit.W))
  val rs2 = Input(UInt(conf.regBit.W))
  val rs1data = Output(UInt(conf.dataWidth.W))
  val rs2data = Output(UInt(conf.dataWidth.W))
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

class RegFile(implicit val conf:Config) extends Module {
  val io = IO(new RegFileIO)
  val regs = Mem(1<<conf.regBit, UInt(conf.dataWidth.W))
  io.rs1data := Mux(io.rs1.orR, regs(io.rs1), 0.U)
  io.rs2data := Mux(io.rs2.orR, regs(io.rs2), 0.U)
  when(io.wen & io.rd.orR) {
    regs(io.rd) := io.wdata
  }

  io.regOut := regs
}