import chisel3._
import chisel3.util.Cat

class WbUnitPort(implicit val conf:Config) extends Bundle {
  val regfilewrite = new RegFileWrite
  val finishFlag = Bool()

  override def cloneType: this.type = new WbUnitPort()(conf).asInstanceOf[this.type]
}

class IdWbUnitPort(implicit val conf:Config) extends Bundle {
  val idIn = Input(new IfUnitOut)
  val wbIn = Input(new WbUnitPort)
  val exRegWriteIn = Input(new MainRegInWrite)
  val memRegWriteIn = Input(new MainRegInWrite)
  val exMemIn = Input(new MemUnitIn)

  val exOut = Output(new ExUnitIn)
  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitPort)

  val mainRegOut = Output(new MainRegisterOutPort)

  val stole = Output(Bool())

  val idEnable = Input(Bool())
  val idFlush = Input(Bool())
}

class IdWbUnit(implicit val conf:Config) extends Module {
  val io = IO(new IdWbUnitPort)

  val pIdReg = RegInit(0.U.asTypeOf(new IfUnitOut))
  when(io.idEnable){
    when(io.idFlush) {
      pIdReg := 0.U.asTypeOf(new IfUnitOut)
    }.otherwise{
      when(io.stole){
        pIdReg := pIdReg
      }.otherwise {
        pIdReg := io.idIn
      }
    }
  }.otherwise{
    pIdReg := pIdReg
  }

  val decoder = Module(new InstructionDecoder())
  val immgen = Module(new ImmGen())
  val fwd1 = Module(new ForwardController())
  val fwd2 = Module(new ForwardController())
  val mainReg = Module(new MainRegister())

  decoder.io.inst := pIdReg.inst

  io.exOut := decoder.io.exOut
  io.exOut.bcIn.pc := pIdReg.instAddr

  io.memOut := decoder.io.memOut
  io.memOut.data := fwd2.io.out

  io.wbOut.regfilewrite.waddr := decoder.io.rd
  io.mainRegOut := mainReg.io.regOut

  mainReg.io.port.inRead := decoder.io.regRead
  mainReg.io.port.inWrite := io.wbIn.regWrite

  fwd1.io.rs := decoder.io.regRead.rs1
  fwd1.io.data := mainReg.io.port.out.rs1Data
  fwd1.io.exWrite := io.exRegWriteIn
  fwd1.io.memWrite := io.memRegWriteIn

  fwd2.io.rs := decoder.io.regRead.rs2
  fwd2.io.data := mainReg.io.port.out.rs2Data
  fwd2.io.exWrite := io.exRegWriteIn
  fwd2.io.memWrite := io.memRegWriteIn

  when(decoder.io.pcImmSel){
    io.exOut.bcIn.pcImm := decoder.io.pcImm
    io.exOut.bcIn.pcAdd := true.B
  }.otherwise{
    io.exOut.bcIn.pcImm := fwd1.io.out
    io.exOut.bcIn.pcAdd := false.B
  }

  alu.io.A := Mux(decoder.io.inASel === A_RS1, fwd1.io.out1, pIdReg.instAddr)
  alu.io.B := Mux(decoder.io.inBSel === B_RS2, fwd2.io.out, immgen.io.out)
  alu.io.alu_op := decoder.io.alu_op

  io.stole := false.B
  when((decoder.io.regRead.rs1 === io.exRegWriteIn.rd) ||
       (decoder.io.regRead.rs2 === io.exRegWriteIn.rd)){
    io.stole := io.exMemIn.read
  }

  when(io.stole){
    io.memOut.write := false.B
    io.memOut.read := false.B
    io.wbOut.regWrite.writeEnable := false.B
    io.wbOut.inst := 0.U
    io.wbOut.instAddr := 0.U
    io.wbOut.finishFlag := false.B
    io.exOut.bcIn.pcOpcode := 0.U
  }

  when(conf.debugId.B){
    printf("[ID] instAddress:0x%x\n", pIdReg.instAddr)
    printf("[ID] inst:0x%x\n", pIdReg.inst)
    printf("[ID] Imm:0x%x\n", decoder.io.imm)
    //printf("[ID] pc:0x%x\n", io.exOut.bcIn.pc)
    //printf("[ID] pcAdd:%d\n", io.exOut.bcIn.pcAdd)
    //printf("[ID] InAData:0x%x\n", fwd1.io.out)
    //printf("[ID] InBData:0x%x\n", fwd2.io.out)
    //printf("[ID] InASel:%d\n", decoder.io.inASel)
    //printf("[ID] InBSel:%d\n", decoder.io.inBSel)
    //printf("[ID] RegWrite:0x%x\n", decoder.io.wbOut.regWrite)
  }
}