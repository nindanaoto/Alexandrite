import chisel3._

class CoreUnitPort(implicit val conf:Config) extends Bundle {
  val romPort = Flipped(new RomPort())
  val ramPort = Flipped(new RamPort())

  val mainRegOut = Output(new RegisterFileOutPort)
  val load = Input(Bool())
}

class CoreUnit(implicit val conf:Config) extends Module {
  val io = IO(new CoreUnitPort())

  val ifUnit = Module(new IfUnit)
  val idwbUnit = Module(new IdWbUnit)
  val exUnit = Module(new ExUnit)
  val memUnit = Module(new MemUnit)

  io.romPort <> ifUnit.io.romPort
  io.ramPort <> memUnit.io.ramPort

  ifUnit.io.ifPort.in.jump := exUnit.io.out.jump
  ifUnit.io.ifPort.in.jumpAddress := exUnit.io.out.res
  ifUnit.io.ifPort.stall := idwbUnit.io.stall||io.load

  idwbUnit.io.idIn := ifUnit.io.ifPort.out
  idwbUnit.io.wbIn := memUnit.io.memPort.wbout
  idwbUnit.io.exRegWriteIn := exUnit.io.wbOut.regfilewrite
  idwbUnit.io.idFlush := exUnit.io.out.jump
  idwbUnit.io.idEnable := true.B&&(!io.load)
  io.mainRegOut := idwbUnit.io.mainRegOut

  exUnit.io.in := idwbUnit.io.exOut
  exUnit.io.memIn := idwbUnit.io.memOut
  exUnit.io.wbIn := idwbUnit.io.wbOut
  exUnit.io.flush := exUnit.io.out.jump
  exUnit.io.enable := true.B&&(!io.load)

  memUnit.io.memPort.in := exUnit.io.memOut
  memUnit.io.memPort.wbIn := exUnit.io.wbOut
}

object CoreUnitTop extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(new CoreUnit()(Config()))
}