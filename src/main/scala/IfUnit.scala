import chisel3._
import chisel3.util.Cat

class IfUnitIn(implicit val conf:Config) extends Bundle {
  val romData = UInt(conf.romDataWidth.W)
  val jumpAddress = UInt(conf.instAddrWidth.W)
  val jump = Bool()

  override def cloneType: this.type = new IfUnitIn()(conf).asInstanceOf[this.type]
}

class IfUnitOut(implicit val conf:Config) extends Bundle {
  val romAddr = UInt(conf.romAddrWidth.W)
  val inst = UInt(conf.instWidth.W)
  val instAddr = UInt(conf.instAddrWidth.W)
  val stole = Bool()

  override def cloneType: this.type = new IfUnitOut()(conf).asInstanceOf[this.type]
}

class IfUnitPort(implicit val conf:Config) extends Bundle {
  val in = Input(new IfUnitIn)
  val out = Output(new IfUnitOut)

  val enable = Input(Bool())
}

class IfUnit(implicit val conf: Config) extends Module {
  val io = IO(new IfUnitPort)
  val romPort = IO(new RomPort)

  val pc = RegInit(0.U(conf.instAddrWidth.W))

  val stole = Wire(Bool())
  val inst = Wire(UInt(conf.instWidth.W))
  inst := DontCare


  when(io.enable){

    // ProgramCounter
    when(!stole) {
      when(io.in.jump) {
        pc := io.in.jumpAddress
      }.otherwise {
        pc := pc + (conf.instWidth/8).U
      }
    }.otherwise{
      pc := pc
    }

    stole := false.B
    romPort.addr := pc(conf.instAddrWidth-1, 2)
    inst := romPort.romData
    stole := false.B
  }.otherwise{
      pc := pc
  }
  io.out.instAddr := pc
  io.out.stole := stole
  when(stole){
    io.out.inst := 0.U
  }.otherwise{
    io.out.inst := inst
  }

  when(conf.debugIf.B){
    printf("\n[IF] instAddress:0x%x\n", io.out.instAddr)
    printf("[IF] inst:0x%x\n", io.out.inst)
    printf("[IF] jump:%d\n", io.in.jump)
    printf("[IF] JumpAddress:0x%x\n", io.in.jumpAddress)
  }
}