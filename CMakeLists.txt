project(alexandrite-test CXX ASM)
cmake_minimum_required(VERSION 3.16)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "-march=native -O2")

find_package(verilator)

add_subdirectory(test)