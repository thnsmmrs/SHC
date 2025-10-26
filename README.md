# Self Hosted Compiler HackIO 2025

A custom low-level programming language, originally built in Java with the goal of compiling itself as a fully self-hosted compiler.

## Usage

Compile:

```bash
javac Main.java
```

Usage:
```bash
java Main file.shc
```

## Compilation Process

Source code (SHC) -> Scanner -> Parser -> Compiler

### Source Code

SHC is similar to C, but pointers work a bit different.

In C, the following snippets work differently:
```
int *x = 5;
```

```
int *x;
*x = 5;
```

This distinction can be difficult for new programmers to get accustomed to.
In contrast, SHC treats these the same.
```
// declare an int:
x: ^int; 
// set the value to 5:
x = 5;
```

In fact, `x = 5` in SHC will always be valid.

This also removes the distinction between `&` and `*` that C has, as both are replaced by the single `^` operator.

Additionally, types are declared after the function or variable, similar to other modern languages like Go, Rust, or Kotlin.

Example full program:
```
fun main(): int {
  len: int;
  len = 100;
  buf: ^char;
  ^buf = malloc(len + 1);
  val: ^char;
  ^val = ^buf;

  while (^val < ^buf + len) {
    val = 'A';
    ^val = ^val + 1;
  }
  val = 0; // terminate string

  puts(^buf);
  free(^buf);

  return 0;
}
```

See [shc.bnf](./shc.bnf) for the full grammar.

### Scanner

The scanner tokenizes the input according to the enum in [SHC.java](./SHC.java), so that it can then be fed into the parser.

### Parser

The parser builds an Abstract Syntax Tree (AST) to be used by the compiler.

The AST was also used with a PrettyPrinter to help with debugging.

### Compiler

The compiler uses th AST to generate C code, using 8 bytes for ints and 1 byte for chars.

