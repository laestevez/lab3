lab3:
	javac *.java

tests:
	java lab3 sum_10.asm sum_10.script
	diff -w -B output.out sum_10.output
	java lab3 lab3_fib.asm lab3_fib.script
	diff -w -B output.out lab3_fib.output
	java lab3 lab3_test3.asm lab3_test3.script
	diff -w -B output.out lab3_test3.output
