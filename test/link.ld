OUTPUT_ARCH( "riscv" )

PHDRS
{
    rom PT_LOAD;
    ram PT_LOAD;
}

ENTRY(_start)

SECTIONS
{
  . = 0x00000000;
  .text : { *(.text) }:rom
  . = 0x00010018;
  .data : { *(.data) }:ram
  .rodata : { *(.rodata) } :ram
  .bss : { *(.bss) }:ram
}