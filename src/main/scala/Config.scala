
case class Config() {
    var debugIf = true
    var debugId = true
    var debugEx = true
    var debugMem = true
    var debugWb = true

    var test = false
    var testRom:Seq[BigInt] = Seq(BigInt(0))
    var testRam:Seq[BigInt] = Seq(BigInt(0))

    val instWidth = 32
    val dataWidth = 32
    val regBit = 5

    //IF Unit
    val romAddrWidth = 7
    val romDataWidth = dataWidth

    val instAddrWidth = romAddrWidth+2

    var ramAddrWidth = 8
    val ramDataWidth = dataWidth

    val dataAddrWidth = ramAddrWidth+1
}