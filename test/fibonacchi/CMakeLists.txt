add_executable(fibtest sim4fibtest.cpp)
target_include_directories(fibtest
PUBLIC
${PROJECT_SOURCE_DIR}/thirdparties/ELFIO
)
verilate(fibtest SOURCES ../../CoreUnit.v THREADS 2 TRACE_FST)
file(COPY fibtest.S DESTINATION .)
file(COPY ../link.ld DESTINATION .)
add_custom_command(
  TARGET fibtest
  PRE_BUILD
  COMMAND riscv32-unknown-elf-gcc *.S -nostdlib -T link.ld
)