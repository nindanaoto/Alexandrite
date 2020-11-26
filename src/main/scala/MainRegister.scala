import chisel3._

class MainRegInRead(implicit val conf:Config) extends Bundle {
  val rs1 = UInt(conf.regBit.W)
  val rs2 = UInt(conf.regBit.W)
}

class MainRegInWrite(implicit val conf:Config) extends Bundle {
  val rd = UInt(conf.regBit.W)
  val writeEnable = Bool()
  val writeData = UInt(conf.dataWidth.W)
}

class MainRegOut(implicit val conf:CAHPConfig) extends Bundle {
  val rs1Data = UInt(conf.dataWidth.W)
  val rs2Data = UInt(conf.dataWidth.W)
}

class MainRegPort(implicit val conf:CAHPConfig) extends Bundle {
  val inRead = Input(new MainRegInRead)
  val inWrite = Input(new MainRegInWrite)

  val out = Output(new MainRegOut)
}


class MainRegisterOutPort(implicit val conf:CAHPConfig) extends Bundle{
  val reg = Output(Vec(1<<conf.regBit,UInt(conf.dataWidth.W)))
}

class MainRegisterPort(implicit val conf:CAHPConfig) extends Bundle {
  val port = new MainRegPort
  val regOut = new MainRegisterOutPort

  val inst = Input(UInt(conf.instWidth.W))
  val instAddr = Input(UInt(conf.instAddrWidth.W))
}

class MainRegister(implicit val conf:Config) extends Module{
  val io = IO(new MainRegisterPort)

  val MainReg = Mem(1<<conf.regBit, UInt(conf.dataWidth.W))

  io.port.out.rs1Data := MainReg(io.port.inRead.rs1)
  io.port.out.rs2Data := MainReg(io.port.inRead.rs2)
  when(io.port.inWrite.writeEnable) {
    MainReg(io.port.inWrite.rd) := io.port.inWrite.writeData
    when(conf.debugWb.B) {
      printf("inst:0x%x addr:0x%x portA Reg x%d <= 0x%x\n", io.inst, io.instAddr, io.port.inWrite.rd, io.port.inWrite.writeData)
    }
  }.otherwise{
    //when(conf.debugWb.B){
    //  printf("inst:0x%x addr:0x%x\n", io.inst, io.instAddr)
    //}
  }

  io.regOut.reg := MainReg
}