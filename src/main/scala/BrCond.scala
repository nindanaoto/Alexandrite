// riscv-mini

import chisel3._
import Control._

class BrCondIn(implicit val conf:Config) extends Bundle {
  val rs1 = Input(UInt(conf.dataWidth.W))
  val rs2 = Input(UInt(conf.dataWidth.W))
  val br_type = Input(UInt(3.W))
}

class BrCondIO(implicit val conf:Config) extends Bundle {
  val in = new BrCondIn
  val jump = Output(Bool())
}

class BrCond(implicit val conf:Config) extends Module {
  val io = IO(new BrCondIO)
  val diff = io.in.rs1 - io.in.rs2
  val neq  = diff.orR
  val eq   = !neq
  val isSameSign = io.in.rs1(conf.dataWidth-1) === io.in.rs2(conf.dataWidth-1)
  val lt   = Mux(isSameSign, diff(conf.dataWidth-1), io.in.rs1(conf.dataWidth-1))
  val ltu  = Mux(isSameSign, diff(conf.dataWidth-1), io.in.rs2(conf.dataWidth-1))
  val ge   = !lt
  val geu  = !ltu
  io.jump :=     
    ((io.in.br_type === BR_EQ) && eq) ||
    ((io.in.br_type === BR_NE) && neq) ||
    ((io.in.br_type === BR_LT) && lt) ||
    ((io.in.br_type === BR_GE) && ge) ||
    ((io.in.br_type === BR_LTU) && ltu) ||
    ((io.in.br_type === BR_GEU) && geu)
}