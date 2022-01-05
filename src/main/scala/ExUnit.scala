import chisel3._
import chisel3.util.{BitPat, Cat}

class ExUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new ExUnitIn)
  val memIn = Input(new MemUnitIn)
  val wbIn = Input(new WbUnitIn)
  val enable = Input(Bool())
  val flush = Input(Bool())

  val out = new ExUnitOut

}

class ExUnitIn(implicit val conf:Config) extends Bundle {
  val aluIn = new ALUIn

  val bcIO = new BrCondIO

  override def cloneType: this.type = new ExUnitIn()(conf).asInstanceOf[this.type]
}

class ExUnitOut(implicit val conf:Config) extends Bundle {
  val res = Output(UInt(conf.dataWidth.W))
  val jump = Output(Bool())

  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitIn)

  override def cloneType: this.type = new ExUnitOut()(conf).asInstanceOf[this.type]
}

import Control.ST_XXX
import Control.LD_XXX
import Control.BR_XXX

class ExUnit(implicit val conf:Config) extends Module {
  val io = IO(new ExUnitPort)
  val alu = Module(new ALU)
  val brcond = Module(new BrCond)
  val pExReg = RegInit(0.U.asTypeOf(new ExUnitIn))
  val pBrReg = RegInit(0.U.asTypeOf(new BrCondIn))
  val pMemReg = RegInit(0.U.asTypeOf(new MemUnitIn))
  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))

  when(io.enable) {
    pExReg := io.in
    pMemReg := io.memIn
    pWbReg := io.wbIn
    when(io.flush){
      pMemReg.st_type := ST_XXX
      pMemReg.ld_type := LD_XXX
      pWbReg.finishFlag := false.B
      pWbReg.regfilewrite.writeEnable := false.B
      pExReg.bcIO.in.br_type := BR_XXX
    }
  }

  alu.io.in := pExReg.aluIn
  io.out.res := alu.io.out.out

  io.memOut := pMemReg

  io.wbOut := pWbReg
  io.wbOut.regfilewrite.writeData := io.out.res

  brcond.io.in := pBrReg
  io.out.jump := brcond.io.jump

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