add_executable(litest sim4litest.cpp)
target_include_directories(litest
PUBLIC
${PROJECT_SOURCE_DIR}/thirdparties/ELFIO
)
verilate(litest SOURCES ../../CoreUnit.v THREADS 2 TRACE_FST)
file(COPY litest.S DESTINATION .)
file(COPY ../link.ld DESTINATION .)
add_custom_command(
  TARGET litest
  PRE_BUILD
  COMMAND riscv32-unknown-elf-gcc *.S -nostdlib -T link.ld
)