import chisel3._

class CoreUnitPort(implicit val conf:Config) extends Bundle {
  val romPort = new RomPort()
  val RramPort = new ReadRamPort()
  val WramPort = new WriteRamPort()

  val mainRegOut = Output(new RegisterFileOutPort)
  val load = Input(Bool())
}

class CoreUnit(implicit val conf:Config) extends Module {
  val io = IO(new CoreUnitPort())

  val ifUnit = Module(new IfUnit)
  val idwbUnit = Module(new IdWbUnit)
  val exUnit = Module(new ExUnit)
  val WmemUnit = Module(new WMemUnit)
  val RmemUnit = Module(new RMemUnit)

  val pIfIdReg = RegInit(0.U.asTypeOf(new IfUnitOut))
  val pIdExReg = RegInit(0.U.asTypeOf(new IdUnitOut))
  val pExRMemReg = RegInit(0.U.asTypeOf(new ExUnitOut))
  val pRMemWbReg = RegInit(0.U.asTypeOf(new RMemUnitOut))

  io.romPort := ifUnit.romPort

  io.WramPort := WmemUnit.writeramPort
  io.RramPort := RmemUnit.readramPort

  ifUnit.io.in.jump := exUnit.io.out.jump
  ifUnit.io.in.jumpAddress := exUnit.io.out.res
  ifUnit.io.enable := !idwbUnit.io.stole&&(!io.load)

  idwbUnit.io.idIn := ifUnit.io.out
  idwbUnit.io.wbIn := RmemUnit.io.wbout
  idwbUnit.io.exRegWriteIn := exUnit.io.out.wbOut.regfilewrite
  idwbUnit.io.exMemIn := exUnit.io.memOut
  idwbUnit.io.idFlush := exUnit.io.out.jump
  idwbUnit.io.idEnable := true.B&&(!io.load)

  exUnit.io.in := idwbUnit.io.exOut
  exUnit.io.memIn := idwbUnit.io.memOut
  exUnit.io.wbIn := idwbUnit.io.wbOut
  exUnit.io.flush := exUnit.io.out.jump
  exUnit.io.enable := true.B&&(!io.load)

  RmemUnit.io.addr := exUnit.io.out
  RmemUnit.io.in := exUnit.io.memOut
  RmemUnit.io.wbIn := exUnit.io.wbOut
  RmemUnit.io.enable := true.B&&(!io.load)
  RmemUnit.io.ramPort.readData := io.ramPort.readData
}