.section .text
.global _start
_start:
	li sp,  0x00010008 # Initial Stack Pointer
	lw sp, 0(sp)
	lw a0, 0(sp)	# argc
	addi a1, sp, 4	# argv

	jal ra, main
	li x6, 0x00010004
	li x7, 1
	sw x7, 0(x6) # finflag MMIO