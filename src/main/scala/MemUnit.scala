import chisel3._
import chisel3.util.Cat

class MemUnitIn(implicit val conf:Config) extends Bundle {
  val data = UInt(conf.dataWidth.W)
  val st_type    = UInt(2.W)
  val ld_type    = UInt(3.W)

  override def cloneType: this.type = new MemUnitIn()(conf).asInstanceOf[this.type]
}

class MemUnitPort(implicit val conf:Config) extends Bundle {
  val addr = Input(new ExUnitOut)
  val in = Input(new MemUnitIn)
  val wbIn = Input(new WbUnitIn)
  val ramPort = new RamPort
  val out = Output(new WbUnitIn)

  val enable = Input(Bool())
}

class MemUnit(implicit val conf:Config) extends Module {
  val io = IO(new MemUnitPort())

  val pExInReg = RegInit(0.U.asTypeOf(new ExUnitOut))
  val pMemReg = RegInit(0.U.asTypeOf(new MemUnitIn))
  val pWbReg = RegInit(0.U.asTypeOf(new WbUnitIn))

  when(io.enable){
    pMemReg := io.in
    pWbReg := io.wbIn
    pExInReg := io.addr
  }

  io.ramPort.addr := pExInReg.res(conf.ramDataAddrWidth-1+2,2)
  io.ramPort.writeData := pMemReg.data
  io.ramPort.writeEnable := MuxLookup(pMemReg.st_type,DontCare,Seq(ST_SW->"0b1111",ST_SH->Mux(pExInReg.res(1),"0b1100","0b0011"),ST_SB->(1<<pExInReg.res(1,0))))

  io.out := pWbReg
  val shifted = io.ramPort.readData >> pExInReg.res(1,0)
  io.out.regfilewrite.writeData := MuxLookup(ld_type, io.ramPort.readData, Seq(LD_LH  -> shifted(15, 0).asSInt, LD_LB  -> shifted(7, 0).asSInt, LD_LHU -> shifted(15, 0).zext, LD_LBU -> shifted(7, 0).zext) )
}