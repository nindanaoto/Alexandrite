import chisel3._
import chisel3.util.Cat

class WbUnitIn(implicit val conf:Config) extends Bundle {
  val regfilewrite = new RegFileWrite
  val finishFlag = Bool()

  override def cloneType: this.type = new WbUnitIn()(conf).asInstanceOf[this.type]
}

class IdWbUnitPort(implicit val conf:Config) extends Bundle {
  val idIn = Input(new IfUnitOut)
  val wbIn = Input(new WbUnitIn)
  val exRegWriteIn = Input(new RegFileWrite)
  val exMemIn = Input(new MemUnitIn)

  val exOut = Output(new ExUnitIn)
  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitIn)

  val mainRegOut = Output(new RegisterFileOutPort)

  val stole = Output(Bool())

  val idEnable = Input(Bool())
  val idFlush = Input(Bool())
}

import Control.A_RS1
import Control.B_RS2
import Control.ST_XXX

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

  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))
  pWbReg := io.wbIn

  val decoder = Module(new InstructionDecoder())
  val immgen = Module(new ImmGen())
  val fwd1 = Module(new ForwardController())
  val fwd2 = Module(new ForwardController())
  val mainReg = Module(new RegFile())

  decoder.io.inst := pIdReg.inst

  io.memOut.st_type := decoder.io.st_type
  io.memOut.ld_type := decoder.io.ld_type
  io.memOut.data := fwd2.io.out

  io.wbOut.regfilewrite.rd := decoder.io.rd
  io.mainRegOut := mainReg.io.regOut

  mainReg.io.readport.rs1 := decoder.io.rs1
  mainReg.io.readport.rs2 := decoder.io.rs2
  mainReg.io.writeport := pWbReg.regfilewrite

  fwd1.io.rs := decoder.io.rs1
  fwd1.io.data := mainReg.io.readport.rs1data
  fwd1.io.exWrite := io.exRegWriteIn
  fwd1.io.wbWrite := io.memRegWriteIn

  fwd2.io.rs := decoder.io.rs2
  fwd2.io.data := mainReg.io.readport.rs2data
  fwd2.io.exWrite := io.exRegWriteIn
  fwd2.io.memWrite := io.memRegWriteIn

  io.exOut.aluIn.alu_op := decoder.io.alu_op
  io.exOut.aluIn.A := Mux(decoder.io.A_sel === A_RS1, fwd1.io.out, pIdReg.instAddr)
  io.exOut.aluIn.B := Mux(decoder.io.B_sel === B_RS2, fwd2.io.out, immgen.io.out)


  io.stole := false.B
  when((decoder.io.rs1 === io.exRegWriteIn.rd) ||
       (decoder.io.rs2 === io.exRegWriteIn.rd)){
    io.stole := io.exMemIn.st_type != ST_XXX
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