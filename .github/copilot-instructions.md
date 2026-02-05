# Copilot instructions for octane-mini-lang-onefile ✅

Purpose
- Short, focused notes to help an AI coding agent get productive quickly on this repo.

Big picture / architecture 🔧
- Single-file interpreter: primary implementation is `src/org/berlin/octane/lexer1/OctaneLangOneSourceFile.java` (entire language in one file).
- Test coverage lives in `test/org/berlin/octane/lexer1/OctaneLangOneSourceFileTest.java` (JUnit 3 style).
- Maven-based project with a non-standard layout: source files under `src` and tests under `test` (see `pom.xml` where `sourceDirectory` and `testSourceDirectory` are overridden).

Quick start: build & test ▶️
- Build jar: `mvn -q package`
- Run tests (the POM sets surefire `skipTests=true` by default):
  - Preferred: `mvn -DskipTests=false -q test` to run the test suite via Maven
  - Or run the test class directly: `mvn -Dexec.mainClass="org.berlin.octane.lexer1.OctaneLangOneSourceFileTest" exec:java`
- Run the interpreter on the example file: `mvn -Dexec.mainClass="org.berlin.octane.lexer1.OctaneLangOneSourceFile" exec:java -Dexec.args="main.octane"`

Project-specific conventions & gotchas ⚠️
- Java compatibility is old: `java.source.version` and `target` are set to 1.5 in `pom.xml`. Avoid modern language features that break compilation under 1.5.
- Tests use **JUnit 3** (extends `TestCase` and contains a `main` runner). Add new tests following this style for consistency.
- The POM config uses `src`/`test` instead of Maven standard `src/main/java`—IDE import settings might need to be adjusted.
- The interpreter uses doubles for numeric values. Tests assert string forms like `"2.0"` (see `toStringStack()`), so beware formatting/regression when changing numeric behavior.

Code patterns to know (how to change behavior) 🔁
- Tokenization: `nextToken()` produces tokens; `scanInteger()` returns a `double`, `scanString()` returns `LangTypeString`, `scanChar()` returns `LangTypeChar`.
- Execution core: `interpret()` pushes tokens to `codeStack` and then `interpret(Stack, Stack)` walks tokens, using many `if/else` branches keyed on token `String` values (e.g., `+`, `lambda`, `if`, `ptr`, ...).
  - To add a new word/operator: add handling inside the `else if (lastValue instanceof String)` branch in `interpret()` and add a focused unit test in `OctaneLangOneSourceFileTest`.
- Functions & blocks: anonymous blocks are created with `)` which generates a unique function id and `OP_ADD_FUNC_BLOCK`/`OP_SET_FUNC_BLOCK` control block collection; named functions map via `functionCodeLookup`.
- Lists/quotations: `[` and `]` control list mode (`LangTypeList`) and the interpreter toggles `modeAddingToList` and `activeList`.
- Data vs code stacks: `dataStack` holds runtime values, `codeStack` holds words for interpretation. Use `printStack()` / `printCodeStack()` for debugging.

Examples from this repo (use when writing tests or fixes) 💡
- Arithmetic test: `.setInput(" + 1 1 ")` → `interpret()` → expect `"2.0"` via `toStringStack()` (see `testAdd1`).
- Pointer example: `"ptr ptrval ptrplus , ptrval 'h' ptrset"` (see `testPointer1`).

Debugging tips 🔍
- Toggle runtime verbose using the `verbosedebug` runtime token in source code to get interpret-time prints.
- Use `printStack()` and `printCodeStack()` helper methods (they're already in the main class).
- Keep unit tests small and deterministic; many tests rely on exact top-of-stack or full stack string outputs.

Safety & maintenance notes 🛠️
- Avoid refactors that change token string literals or numeric formatting unless tests are updated accordingly.
- If adding major features, add tests that exercise both the lexer (`scan*` methods) and the interpreter behavior.

If anything is unclear or you'd like the file to include more examples (e.g., how to add a new operator step-by-step), please tell me which section to expand. ✨