import chisel3._
import chisel3.util.{BitPat, Cat}

class ExUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new ExUnitIn)
  val memIn = Input(new MemUnitIn)
  val wbIn = Input(new WbUnitIn)
  val enable = Input(Bool())
  val flush = Input(Bool())

  val out = new ExUnitOut
  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitIn)
}

class ExUnitIn(implicit val conf:Config) extends Bundle {
  val aluIn = new ALUPortIn

  val bcIn = new BranchControllerIn()
}

class BranchControllerIn(implicit val conf:Config) extends Bundle {
  val pcOpcode = UInt(3.W)
  val pc = UInt(16.W)
  val pcImm = UInt(16.W)
  val pcAdd = Bool()
}

class ExUnitOut(implicit val conf:Config) extends Bundle {
  val res = Output(UInt(conf.dataWidth.W))
  val jumpAddress = Output(UInt(conf.instAddrWidth.W))
  val jump = Output(Bool())

  override def cloneType: this.type = new ExUnitOut()(conf).asInstanceOf[this.type]
}

class ExUnit(implicit val conf:Config) extends Module {
  val io = IO(new ExUnitPort)
  val alu = Module(new ALU)
  val pExReg = RegInit(0.U.asTypeOf(new ExUnitIn))
  val pMemReg = RegInit(0.U.asTypeOf(new MemUnitIn))
  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))


  when(io.enable) {
    pExReg := io.in
    pMemReg := io.memIn
    pWbReg := io.wbIn
    when(io.flush){
      pMemReg.write := false.B
      pWbReg.finishFlag := false.B
      pWbReg.regWrite.writeEnable := false.B
      pExReg.bcIn.pcOpcode := 0.U
    }
  }

  alu.io.in := pExReg.aluIn
  io.out.res := alu.io.out.out

  io.memOut := pMemReg

  io.wbOut := pWbReg
  io.wbOut.regWrite.writeData := io.out.res

  when(pExReg.bcIn.pcAdd) {
    io.out.jumpAddress := pExReg.bcIn.pc + pExReg.bcIn.pcImm
  }.otherwise{
    io.out.jumpAddress := pExReg.bcIn.pcImm
  }

  val flagCarry = alu.io.out.flagCarry
  val flagOverflow = alu.io.out.flagOverflow
  val flagSign = alu.io.out.flagSign
  val flagZero = alu.io.out.flagZero

  io.out.jump := false.B
  when(pExReg.bcIn.pcOpcode === 1.U){
    io.out.jump := flagZero
  }.elsewhen(pExReg.bcIn.pcOpcode === 2.U){
    io.out.jump := flagCarry
  }.elsewhen(pExReg.bcIn.pcOpcode === 3.U){
    io.out.jump := flagCarry||flagZero
  }.elsewhen(pExReg.bcIn.pcOpcode === 4.U){
    io.out.jump := true.B
  }.elsewhen(pExReg.bcIn.pcOpcode === 5.U){
    io.out.jump := !flagZero
  }.elsewhen(pExReg.bcIn.pcOpcode === 6.U){
    io.out.jump := flagSign != flagOverflow
  }.elsewhen(pExReg.bcIn.pcOpcode === 7.U){
    io.out.jump := (flagSign != flagOverflow)||flagZero
  }

  when(conf.debugEx.B) {
    printf("[EX] opcode:0x%x\n", pExReg.aluIn.opcode)
    printf("[EX] inA:0x%x\n", pExReg.aluIn.inA)
    printf("[EX] inB:0x%x\n", pExReg.aluIn.inB)
    printf("[EX] Res:0x%x\n", io.out.res)
    printf("[EX] Jump:%d\n", io.out.jump)
    printf("[EX] JumpAddress:0x%x\n", io.out.jumpAddress)
  }

  //when(io.out.jump){
  //  printf("JUMP addr:0x%x pcAdd:%d pc:0x%x pcImm:0x%x\n", io.out.jumpAddress, pExReg.bcIn.pcAdd, pExReg.bcIn.pc, pExReg.bcIn.pcImm)
  //}
}