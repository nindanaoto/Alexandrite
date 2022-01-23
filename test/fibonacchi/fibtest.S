main:
	li x5, 5
	li x6, 1
	li x7, 0
	li x8, 1
	li x9, 0
.loop:
	mv x4, x8
	add x8, x8, x7
	mv x7, x4
	addi x6, x6, 1
	blt x6, x5, .loop
.end:
	li x9, 1