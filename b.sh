llc -filetype=obj out.ll
clang out.o -o prog
./prog
echo $?