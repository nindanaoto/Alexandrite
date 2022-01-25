#include <bits/stdint-uintn.h>
#include <verilated.h>
#include <verilated_fst_c.h>
#include <VCoreUnit.h>
#include <array>
#include <cstdint>
#include <iostream>
#include <elfio/elfio.hpp>
#include <vector>
#include <string.h>

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
    dut->io_ramPort_readData = ram[dut->io_ramPort_addr/4];
    ram[prev_io_ramPort_addr/4] = prev_io_ramPort_writeEnable?prev_io_ramPort_writeData:ram[prev_io_ramPort_addr/4];
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

  {
    ELFIO::elfio reader;
    reader.load("a.out");
    int index = 0;
    const ELFIO::segment* psec = reader.segments[0];
    const char* p = psec->get_data();
    std::cout<<psec->get_file_size()<<std::endl;
    while(index<psec->get_file_size()){
      ((char*) &rom[0])[index] = p[index];
      index++;
    }
  }

  {
    std::vector<uint32_t> pointer2argv(argc+1);
    int index = ramsize-1;
    ((char*) &ram[0])[index] = 0;
    pointer2argv[argc] = index;
    for(int i = argc-1; i >= 0; i--){
      index--;
      ((char*) &ram[0])[index] = 0;
      for(int j = strlen(argv[i]) -1; j >= 0; j--){
        index--;
        ((char*) &ram[0])[index] = argv[i][j];
      }
      pointer2argv[i] = index;
    }
    index -= index %4;
    index /= 4;
    for(int i = argc; i >= 0; i--){
      index--;
      ram[index] = pointer2argv[i];
    }
    index--;
    ram[index] = argc;
    ram[2] = index*4;
  }

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

  for(int i=0; i< 100; i++ ){
    clock(dut,tfp);
    std::cout<<i<<std::endl;
    if(ram[1]) break;
  }

  dut->final();
  tfp->close();

  std::cout<<(dut->io_mainRegOut_reg_10)<<std::endl;
}