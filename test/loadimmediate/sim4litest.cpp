#include <bits/stdint-uintn.h>
#include <verilated.h>
#include <verilated_fst_c.h>
#include <VCoreUnit.h>
#include <array>
#include <cstdint>
#include <iostream>
#include <elfio/elfio.hpp>

constexpr int romsize = 1<<12;
constexpr int ramsize = 1<<10;
constexpr int romword = romsize*8/32;
constexpr int ramword = ramsize*8/32;
std::array<uint32_t,romword> rom = {};
std::array<uint32_t,ramword> ram = {};

void clock(VCoreUnit *dut, VerilatedFstC* tfp){
  static uint time_counter = 0;
  static uint prev_io_ramPort_addr = 0;
  static uint prev_io_ramPort_writeData = 0;
  static uint prev_io_ramPort_writeEnable = 0;
  for(int i = 0; i<2; i++){
    dut->io_romPort_data = rom[dut->io_romPort_addr];
    dut->io_ramPort_readData = ram[dut->io_ramPort_addr];
    ram[prev_io_ramPort_addr] = prev_io_ramPort_writeEnable?prev_io_ramPort_writeData:ram[prev_io_ramPort_addr];
    if(i!=0){
      prev_io_ramPort_addr = dut->io_ramPort_addr;
      prev_io_ramPort_writeData = dut->io_ramPort_writeData;
      prev_io_ramPort_writeEnable = dut->io_ramPort_writeEnable;
    }
    dut->eval();
    tfp->dump(1000*time_counter);
    time_counter++;
    dut->clock = !dut->clock;
  }
}

int main(int argc, char** argv) {

  ELFIO::elfio reader;
  reader.load("a.out");
  {
    int index = 0;
    const ELFIO::section* psec = reader.sections[".text"];
    const char* p = psec->get_data();
    while(index<psec->get_size()){
      ((char*) &rom[0])[index] = p[index];
      index++;
    }
    std::cout<<index<<std::endl;
  }
  std::cout<<rom[0]<<std::endl;

  Verilated::commandArgs(argc, argv);
  VCoreUnit *dut = new VCoreUnit();

	Verilated::traceEverOn(true);
	VerilatedFstC* tfp = new VerilatedFstC;
	dut->trace(tfp, 100);  // Trace 100 levels of hierarchy
	tfp->open("simx.fst");

    // Format
  dut->reset = 1;
  dut->clock = 0;

  // Reset
  clock(dut,tfp);

  //Release reset
  dut->reset = 0;

  clock(dut,tfp);
  clock(dut,tfp);
  clock(dut,tfp);
  clock(dut,tfp);
  clock(dut,tfp);
  clock(dut,tfp);
  clock(dut,tfp);

  dut->final();
  tfp->close();

  std::cout<<(dut->io_mainRegOut_reg_8)<<std::endl;
}