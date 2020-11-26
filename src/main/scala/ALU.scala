// riscv-mini

package mini

import chisel3._
import chisel3.util._

object ALUOpcode {
  val ALU_ADD    = 0.U(4.W)
  val ALU_SUB    = 1.U(4.W)
  val ALU_AND    = 2.U(4.W)
  val ALU_OR     = 3.U(4.W)
  val ALU_XOR    = 4.U(4.W)
  val ALU_SLT    = 5.U(4.W)
  val ALU_SLL    = 6.U(4.W)
  val ALU_SLTU   = 7.U(4.W)
  val ALU_SRL    = 8.U(4.W)
  val ALU_SRA    = 9.U(4.W)
  val ALU_COPY_A = 10.U(4.W)
  val ALU_COPY_B = 11.U(4.W)
  val ALU_XXX    = 15.U(4.W)
}

class ALUIo(implicit conf: Config) extends Bundle {
  val A = Input(UInt(conf.dataWidth.W))
  val B = Input(UInt(conf.dataWidth.W))
  val alu_op = Input(UInt(4.W))
  val out = Output(UInt(conf.dataWidth.W))
  val sum = Output(UInt(conf.dataWidth.W))
}

import ALU.ALUOpcode

class ALU(implicit conf: Config) extends Module {
  val io = IO(new ALUIo)
  val sum = io.A + Mux(io.alu_op(0), -io.B, io.B)
  val cmp = Mux(io.A(conf.dataWidth-1) === io.B(conf.dataWidth-1), sum(conf.dataWidth-1),
            Mux(io.alu_op(1), io.B(conf.dataWidth-1), io.A(conf.dataWidth-1)))
  val shamt  = io.B(4,0).asUInt
  val shin   = Mux(io.alu_op(3), io.A, Reverse(io.A))
  val shiftr = (Cat(io.alu_op(0) && shin(conf.dataWidth-1), shin).asSInt >> shamt)(conf.dataWidth-1, 0)
  val shiftl = Reverse(shiftr)

  val out = 
    Mux(io.alu_op === ALU_ADD || io.alu_op === ALU_SUB, sum,
    Mux(io.alu_op === ALU_SLT || io.alu_op === ALU_SLTU, cmp,
    Mux(io.alu_op === ALU_SRA || io.alu_op === ALU_SRL, shiftr,
    Mux(io.alu_op === ALU_SLL, shiftl,
    Mux(io.alu_op === ALU_AND, (io.A & io.B),
    Mux(io.alu_op === ALU_OR,  (io.A | io.B),
    Mux(io.alu_op === ALU_XOR, (io.A ^ io.B), 
    Mux(io.alu_op === ALU_COPY_A, io.A, io.B)))))))))

  io.out := out
  io.sum := sum
}