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

class ALUIn(implicit conf: Config) extends Bundle {
  val A = Input(UInt(conf.dataWidth.W))
  val B = Input(UInt(conf.dataWidth.W))
  val alu_op = Input(UInt(4.W))

  override def cloneType: this.type = new ALUin()(conf).asInstanceOf[this.type]
}

class ALUOut(implicit conf: Config) extends Bundle {
  val out = Output(UInt(conf.dataWidth.W))

  override def cloneType: this.type = new ALUout()(conf).asInstanceOf[this.type]
}

class ALUIo(implicit conf: Config) extends Bundle {
  val in = new ALUIn
  val out = new ALUOut
}

import ALU.ALUOpcode

class ALU(implicit conf: Config) extends Module {
  val io = IO(new ALUIo)
  val sum = io.in.A + Mux(io.in.alu_op(0), -io.in.B, io.in.B)
  val cmp = Mux(io.in.A(conf.dataWidth-1) === io.in.B(conf.dataWidth-1), sum(conf.dataWidth-1),
            Mux(io.in.alu_op(1), io.in.B(conf.dataWidth-1), io.in.A(conf.dataWidth-1)))
  val shamt  = io.in.B(4,0).asUInt
  val shin   = Mux(io.in.alu_op(3), io.in.A, Reverse(io.in.A))
  val shiftr = (Cat(io.in.alu_op(0) && shin(conf.dataWidth-1), shin).asSInt >> shamt)(conf.dataWidth-1, 0)
  val shiftl = Reverse(shiftr)

  val out = 
    Mux(io.in.alu_op === ALU_ADD || io.in.alu_op === ALU_SUB, sum,
    Mux(io.in.alu_op === ALU_SLT || io.in.alu_op === ALU_SLTU, cmp,
    Mux(io.in.alu_op === ALU_SRA || io.in.alu_op === ALU_SRL, shiftr,
    Mux(io.in.alu_op === ALU_SLL, shiftl,
    Mux(io.in.alu_op === ALU_AND, (io.in.A & io.in.B),
    Mux(io.in.alu_op === ALU_OR,  (io.in.A | io.in.B),
    Mux(io.in.alu_op === ALU_XOR, (io.in.A ^ io.in.B), 
    Mux(io.in.alu_op === ALU_COPY_A, io.in.A, io.in.B)))))))))

  io.out.out := out
}