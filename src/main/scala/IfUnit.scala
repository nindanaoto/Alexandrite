import chisel3._
import chisel3.util.Cat

class IfUnitIn(implicit val conf:Config) extends Bundle {
  val jumpAddress = Input(UInt(conf.instAddrWidth.W))
  val jump = Input(Bool())

  override def cloneType: this.type = new IfUnitIn()(conf).asInstanceOf[this.type]
}

class IfUnitOut(implicit val conf:Config) extends Bundle {
  val inst = Output(UInt(conf.instWidth.W))
  val instAddr = Output(UInt(conf.instAddrWidth.W))

  override def cloneType: this.type = new IfUnitOut()(conf).asInstanceOf[this.type]
}

class IfUnitPort(implicit val conf:Config) extends Bundle {
  val in = new IfUnitIn
  val out = new IfUnitOut

  val stall = Input(Bool())
}

class IfUnit(implicit val conf: Config) extends Module {
  val io = IO(new Bundle{
    val ifPort = new IfUnitPort
    val romPort = Flipped(new RomPort)
  })

  val pc = RegInit(0.U(conf.instAddrWidth.W))
  io.ifPort.out.instAddr := pc
  io.ifPort.out.inst := 0.U

  io.romPort.addr := pc(conf.instAddrWidth-1, 2)
  io.ifPort.out.inst := io.romPort.data

  when(!io.ifPort.stall){
    // ProgramCounter
    when(io.ifPort.in.jump) {
      pc := io.ifPort.in.jumpAddress
    }.otherwise {
      pc := pc + (conf.instWidth/8).U
    }
  }

  when(conf.debugIf.B){
    printf("\n[IF] instAddress:0x%x\n", io.ifPort.out.instAddr)
    printf("[IF] inst:0x%x\n", io.ifPort.out.inst)
    printf("[IF] jump:%d\n", io.ifPort.in.jump)
    printf("[IF] JumpAddress:0x%x\n", io.ifPort.in.jumpAddress)
  }
}