import chisel3._
import chisel3.util.Cat

class WbUnitIn(implicit val conf:Config) extends Bundle {
  val regfilewrite = new RegFileWrite
}

class IdWbUnitPort(implicit val conf:Config) extends Bundle {
  val idIn = Input(new IfUnitOut)
  val wbIn = Input(new WbUnitIn)
  val exRegWriteIn = Input(new RegFileWrite)

  val exOut = Output(new ExUnitIn)
  val memOut = Output(new MemUnitIn)
  val wbOut = Output(new WbUnitIn)

  val mainRegOut = Output(new RegisterFileOutPort)

  val stall = Output(Bool())

  val idEnable = Input(Bool())
  val idFlush = Input(Bool())
}

import Control.A_RS1
import Control.B_RS2
import Control.ST_XXX
import Control.LD_XXX
import Control.BR_XXX

class IdWbUnit(implicit val conf:Config) extends Module {
  val io = IO(new IdWbUnitPort)

  val pIdReg = RegInit(0.U.asTypeOf(new IfUnitOut))
  when(io.idEnable){
    when(io.idFlush) {
      pIdReg := 0.U.asTypeOf(new IfUnitOut)
    }.otherwise{
      when(io.stall){
        pIdReg := pIdReg
      }.otherwise {
        pIdReg := io.idIn
      }
    }
  }

  val decoder = Module(new InstructionDecoder())
  val immgen = Module(new ImmGen())
  val fwd1 = Module(new ForwardController())
  val fwd2 = Module(new ForwardController())
  val mainReg = Module(new RegFile())

  decoder.io.inst := pIdReg.inst

  immgen.io.inst := pIdReg.inst
  immgen.io.sel := decoder.io.imm_sel

  io.memOut.exres := DontCare
  io.memOut.st_type := decoder.io.st_type
  io.memOut.ld_type := decoder.io.ld_type
  io.memOut.data := fwd2.io.out

  io.wbOut.regfilewrite.rd := decoder.io.rd
  io.wbOut.regfilewrite.writeEnable := decoder.io.wb_en
  io.wbOut.regfilewrite.writeData := io.idIn.instAddr

  io.mainRegOut := mainReg.io.regOut

  mainReg.io.readport.rs1 := decoder.io.rs1
  mainReg.io.readport.rs2 := decoder.io.rs2
  mainReg.io.writeport := io.wbIn.regfilewrite

  fwd1.io.rs := decoder.io.rs1
  fwd1.io.data := mainReg.io.readport.rs1data
  fwd1.io.exWrite := io.exRegWriteIn
  fwd1.io.wbWrite := io.wbIn.regfilewrite

  fwd2.io.rs := decoder.io.rs2
  fwd2.io.data := mainReg.io.readport.rs2data
  fwd2.io.exWrite := io.exRegWriteIn
  fwd2.io.wbWrite := io.wbIn.regfilewrite

  io.exOut.aluIn.alu_op := decoder.io.alu_op
  io.exOut.aluIn.A := Mux(decoder.io.A_sel === A_RS1, fwd1.io.out, pIdReg.instAddr)
  io.exOut.aluIn.B := Mux(decoder.io.B_sel === B_RS2, fwd2.io.out, immgen.io.out)
  io.exOut.bcIn.rs1 := fwd1.io.out
  io.exOut.bcIn.rs2 := fwd2.io.out
  io.exOut.bcIn.br_type := decoder.io.br_type
  io.exOut.wb_sel := decoder.io.wb_sel

  io.stall := false.B
  when(((decoder.io.A_sel === A_RS1)&&(decoder.io.rs1 === io.exRegWriteIn.rd)) ||
       ((decoder.io.B_sel === B_RS2)&&(decoder.io.rs2 === io.exRegWriteIn.rd))){
    io.stall := RegNext((decoder.io.ld_type =/= LD_XXX)&&(~io.stall))
  }

  when(io.stall){
    io.memOut.st_type := ST_XXX
    io.memOut.ld_type := LD_XXX
    io.wbOut.regfilewrite.writeEnable := false.B
    io.exOut.bcIn.br_type := BR_XXX
  }

  when(conf.debugId.B){
    printf("[ID] instAddress:0x%x\n", pIdReg.instAddr)
    printf("[ID] inst:0x%x\n", pIdReg.inst)
    printf("[ID] Imm:0x%x\n", immgen.io.out)
    //printf("[ID] pc:0x%x\n", io.exOut.bcIn.pc)
    //printf("[ID] pcAdd:%d\n", io.exOut.bcIn.pcAdd)
    //printf("[ID] InAData:0x%x\n", fwd1.io.out)
    //printf("[ID] InBData:0x%x\n", fwd2.io.out)
    //printf("[ID] InASel:%d\n", decoder.io.inASel)
    //printf("[ID] InBSel:%d\n", decoder.io.inBSel)
    //printf("[ID] RegWrite:0x%x\n", decoder.io.wbOut.regWrite)
  }
}