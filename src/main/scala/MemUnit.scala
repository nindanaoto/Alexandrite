import chisel3._
import chisel3.util.MuxLookup

class RMemUnitIn(implicit val conf:Config) extends Bundle {
  val data = UInt(conf.dataWidth.W)
  val st_type    = UInt(2.W)
  val ld_type    = UInt(3.W)
  val exres = Input(UInt(conf.dataWidth.W))

  val wbIn = Input(new WbUnitIn)

  override def cloneType: this.type = new RMemUnitIn()(conf).asInstanceOf[this.type]
}

class RMemUnitOut(implicit val conf:Config) extends Bundle {
  val wmemout = Output(new WMemUnitIn)
  val wbout = Output(new WbUnitIn)
}

class RMemUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new RMemUnitIn)

  val wmemout = Output(new WMemUnitIn)
  val wbout = Output(new WbUnitIn)
}

import Control._

class RMemUnit(implicit val conf:Config) extends Module {
  val io = IO(new RMemUnitPort())
  val readramPort = IO(new ReadRamPort())

  readramPort.addr := io.in.exres(conf.ramDataAddrWidth-1+conf.datainbyte,conf.datainbyte)

  io.wbout := io.in.wbIn
  val shifted = readramPort.readData >> io.in.exres(1,0)
  io.wbout.regfilewrite.writeData := MuxLookup(io.in.ld_type, pWbReg.regfilewrite.writeData, Seq(LD_LW -> readramPort.readData, LD_LH  -> shifted(15, 0).asSInt, LD_LB  -> shifted(7, 0).asSInt, LD_LHU -> shifted(15, 0).zext, LD_LBU -> shifted(7, 0).zext) )

  io.wmemout.data := io.in.data
  io.wmemout.st_type := io.in.st_type
  io.wmemout.exres := io.in.exres
}

class WMemUnitIn(implicit val conf:Config) extends Bundle {
  val data = UInt(conf.dataWidth.W)
  val st_type    = UInt(2.W)

  val exres = Input(UInt(conf.dataWidth.W))

  override def cloneType: this.type = new WMemUnitIn()(conf).asInstanceOf[this.type]
}

class WMemUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new WMemUnitIn)
}

class WMemUnit(implicit val conf:Config) extends Module {
  val io = IO(new WMemUnitPort())
  val writeramPort = IO(new WriteRamPort())

  writeramPort.writeData := MuxLookup(io.in.st_type,DontCare,Seq(ST_SW->io.in.data,ST_SH->(io.in.data(16,0)<<(16.U*io.in.exres(1))),ST_SB->(io.in.data(8,0)<<(8.U*io.in.exres(1,0)))))
  writeramPort.writeEnable := MuxLookup(io.in.st_type,DontCare,Seq(ST_SW->"b1111".U,ST_SH->Mux(io.in.exres(1),"b1100".U,"b0011".U),ST_SB->(1.U<<io.in.exres(1,0))))
}