# Alexandrite
This is a small RV32Iu processor designed to be used as a virtual processor like [cahp-ruby](https://github.com/virtualsecureplatform/cahp-ruby).

This porcessor is based on [riscv-mini](https://github.com/ucb-bar/riscv-mini). Some unnecessry functionality like CSR is omitted to reduce a gate count and introduced more pipeline registers to increase its IPC.
