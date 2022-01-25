import chisel3._
import chisel3.util.MuxLookup

import Control._

class MemUnitIn(implicit val conf:Config) extends Bundle {
  val data = UInt(conf.dataWidth.W)
  val st_type    = UInt(2.W)
  val ld_type    = UInt(3.W)
  val exres = UInt(conf.dataWidth.W)
}

class MemUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new MemUnitIn)
  val wbIn = Input(new WbUnitIn)
  val wbout = Output(new WbUnitIn)
}

class MemUnit(implicit val conf:Config) extends Module {
  val io = IO(new Bundle{
    val memPort = new MemUnitPort()
    val ramPort = Flipped(new RamPort)
  })

  val pMemReg = RegInit(0.U.asTypeOf(new MemUnitIn))
  pMemReg := io.memPort.in
  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))
  pWbReg := io.memPort.wbIn

  io.ramPort.addr := pMemReg.exres

  io.ramPort.writeData := MuxLookup(pMemReg.st_type,DontCare,Seq(ST_SW->pMemReg.data,ST_SH->(pMemReg.data(16,0)<<(16.U*pMemReg.exres(1))),ST_SB->(pMemReg.data(8,0)<<(8.U*pMemReg.exres(1,0)))))
  io.ramPort.writeEnable := MuxLookup(pMemReg.st_type,DontCare,Seq(ST_SW->"b1111".U,ST_SH->Mux(pMemReg.exres(1),"b1100".U,"b0011".U),ST_SB->(1.U<<pMemReg.exres(1,0))))

  io.memPort.wbout := pWbReg
  val shifted = io.ramPort.readData >> (pMemReg.exres(1,0)*8.U)
  io.memPort.wbout.regfilewrite.writeData := MuxLookup(pMemReg.ld_type, pWbReg.regfilewrite.writeData.asSInt, Seq(LD_LW -> io.ramPort.readData.asSInt, LD_LH  -> shifted(15, 0).asSInt, LD_LB  -> shifted(7, 0).asSInt, LD_LHU -> shifted(15, 0).zext, LD_LBU -> shifted(7, 0).zext) ).asUInt
}