llc -filetype=obj text.ll
clang text.o -o text
./text
echo $?