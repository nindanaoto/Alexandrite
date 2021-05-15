import chisel3._

class CoreUnitPort(implicit val conf:Config) extends Bundle {
  val romPort = new RomPort()
  val ramPort = new RamPort()

  val mainRegOut = Output(new RegisterFileOutPort)
  val finishFlag = Output(Bool())
  val load = Input(Bool())
}

class CoreUnit(implicit val conf:Config) extends Module {
  val io = IO(new CoreUnitPort())

  val ifUnit = Module(new IfUnit)
  val idwbUnit = Module(new IdWbUnit)
  val exUnit = Module(new ExUnit)
  val memUnit = Module(new MemUnit)

  io.finishFlag := memUnit.io.out.finishFlag
  io.romPort.addr := ifUnit.io.out.romAddr
  io.mainRegOut := idwbUnit.io.mainRegOut
  ifUnit.io.in.romData := io.romPort.data

  io.ramPort.addr := memUnit.io.ramPort.addr
  io.ramPort.writeData := memUnit.io.ramPort.writeData
  io.ramPort.writeEnable := memUnit.io.ramPort.writeEnable

  ifUnit.io.in.jump := exUnit.io.out.jump
  ifUnit.io.in.jumpAddress := exUnit.io.out.res
  ifUnit.io.enable := !idwbUnit.io.stole&&(!io.load)

  idwbUnit.io.idIn := ifUnit.io.out
  idwbUnit.io.wbIn := memUnit.io.out
  idwbUnit.io.exRegWriteIn := exUnit.io.wbOut.regfilewrite
  idwbUnit.io.memRegWriteIn := memUnit.io.out.regfilewrite
  idwbUnit.io.exMemIn := exUnit.io.memOut
  idwbUnit.io.idFlush := exUnit.io.out.jump
  idwbUnit.io.idEnable := true.B&&(!io.load)

  exUnit.io.in := idwbUnit.io.exOut
  exUnit.io.memIn := idwbUnit.io.memOut
  exUnit.io.wbIn := idwbUnit.io.wbOut
  exUnit.io.flush := exUnit.io.out.jump
  exUnit.io.enable := true.B&&(!io.load)

  memUnit.io.addr := exUnit.io.out
  memUnit.io.in := exUnit.io.memOut
  memUnit.io.wbIn := exUnit.io.wbOut
  memUnit.io.enable := true.B&&(!io.load)
  memUnit.io.ramPort.readData := io.ramPort.readData
}