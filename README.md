# OctaneMini

OctaneMini is a simple proof-of-concept stack-based programming language built on top of Java. The entire interpreter is contained in a single source file, with a separate JUnit 3 test class for coverage. The goal is to keep the core operations small while remaining Turing complete.

## What’s in this repo
- Single-file interpreter: `src/org/berlin/octane/lexer1/OctaneLangOneSourceFile.java`
- Tests: `test/org/berlin/octane/lexer1/OctaneLangOneSourceFileTest.java`
- Example program: `main.octane`

## Example programs

```
# This line will add two numbers 100 and 101,
# 201 is pushed on the data stack
# 101 is read in first and then 100.
+ 100 101

# Lambda call, execute the block of code
lambda ( + 1 1 )

# Other source

data + 1 1

data + 100 11000

data lambda ( + + 1 1 + 1 1)

data - swap 5 10

data lambda (+ 1 1)

data myf call , myf func (- 5 10) ,

data myfunc call , myfunc func ( - ) 5 10

data ptr ptrval ptrplus , ptrval 'h' ptrset

data ptrval , ptr , ptrdec ptr ptrdec , ptr , ptrwhile ( 100 ptrinc ) , ptrdec ptrdec , ptrinc ptrplus , ptrinc ptrplus

data if ( false ) ( + 1 1 ) ( + 2 2 )

data if (true) (+ 1 1) (+ 2 2)

data ptrval ptrinc , ptrwhile ( ptrdec ptrplus ptrplus ptrinc, ptrminus ) , ptrval 1000 ptrset

data ifstk , or norem 5 2 norem 5 2 , 1 0

data x loop [ 1 2 3 4 5 ] , x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk )

data sumstk , x loop range 10, x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk )
```

## Keywords
java, simple, stack language, forth-like, joy-like

## References
- http://en.wikipedia.org/wiki/Joy_(programming_language)
- http://www.kevinalbrecht.com/code/joy-mirror/index.html
- http://en.wikipedia.org/wiki/Forth_(programming_language)
