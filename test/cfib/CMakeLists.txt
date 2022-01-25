add_executable(cfibtest sim4cfibtest.cpp)
target_include_directories(cfibtest
PUBLIC
${PROJECT_SOURCE_DIR}/thirdparties/ELFIO
)
verilate(cfibtest SOURCES ../../CoreUnit.v THREADS 2 TRACE_FST)
file(COPY cfibtest.c DESTINATION .)
file(COPY ../link.ld DESTINATION .)
file(COPY ../util.S DESTINATION .)
add_custom_command(
  TARGET cfibtest
  PRE_BUILD
  COMMAND riscv32-unknown-elf-gcc -O2 util.S *.c -nostdlib -T link.ld
)