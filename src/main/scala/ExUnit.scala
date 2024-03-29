import chisel3._
import chisel3.util.{BitPat, Cat}

class ExUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new ExUnitIn)
  val memIn = Input(new MemUnitIn)
  val wbIn = Input(new WbUnitIn)
  val enable = Input(Bool())
  val flush = Input(Bool())

  val PC4 = Input(UInt(conf.instAddrWidth.W))

  val out = new ExUnitOut
  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitIn)

}

class ExUnitIn(implicit val conf:Config) extends Bundle {
  val aluIn = new ALUIn

  val bcIn = new BrCondIn

  val wb_sel = UInt(2.W)
}

class ExUnitOut(implicit val conf:Config) extends Bundle {
  val res = Output(UInt(conf.dataWidth.W))
  val jump = Output(Bool())
}

import Control.ST_XXX
import Control.LD_XXX
import Control.BR_XXX
import Control.WB_PC4
import Control.WB_ALU

class ExUnit(implicit val conf:Config) extends Module {
  val io = IO(new ExUnitPort)
  val alu = Module(new ALU)
  val brcond = Module(new BrCond)
  val pExReg = RegInit(0.U.asTypeOf(new ExUnitIn))
  val pMemReg = RegInit(0.U.asTypeOf(new MemUnitIn))
  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))

  when(io.enable) {
    pExReg := io.in
    pMemReg := io.memIn
    pWbReg := io.wbIn
    when(io.flush){
      pMemReg.st_type := ST_XXX
      pMemReg.ld_type := LD_XXX
      pWbReg.regfilewrite.writeEnable := false.B
      pExReg.bcIn.br_type := BR_XXX
      pExReg.wb_sel := WB_ALU
    }
  }

  alu.io.in := pExReg.aluIn
  io.out.res := alu.io.out.out

  io.memOut := pMemReg
  io.memOut.exres := alu.io.out.sum

  io.wbOut := pWbReg
  io.wbOut.regfilewrite.writeData := Mux(pExReg.wb_sel===WB_PC4,io.PC4,io.out.res)

  io.out.jump := brcond.io.jump || (pExReg.wb_sel===WB_PC4)
  brcond.io.in := pExReg.bcIn

  when(conf.debugEx.B) {
    printf("[EX] opcode:0x%x\n", pExReg.aluIn.alu_op)
    printf("[EX] inA:0x%x\n", pExReg.aluIn.A)
    printf("[EX] inB:0x%x\n", pExReg.aluIn.B)
    printf("[EX] Res:0x%x\n", io.out.res)
    printf("[EX] Jump:%d\n", io.out.jump)
  }

  //when(io.out.jump){
  //  printf("JUMP addr:0x%x pcAdd:%d pc:0x%x pcImm:0x%x\n", io.out.jumpAddress, pExReg.bcIn.pcAdd, pExReg.bcIn.pc, pExReg.bcIn.pcImm)
  //}
}