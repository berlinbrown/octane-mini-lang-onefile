/**
 * Copyright (c) 2006-2010 Berlin Brown. All Rights Reserved
 *
 * http://www.opensource.org/licenses/bsd-license.php

 * All rights reserved.

 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the Botnode.com (Berlin Brown) nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * OctaneMini is a tiny stack-based programming language implemented as a
 * single Java source file. The entire interpreter is contained here, and
 * tests live in OctaneLangOneSourceFileTest.
 */
package org.berlin.octane.lexer1;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * OctaneMini, simple programming language in Java with one source file.  The entire implementation
 * is contained in this file.   A suite of test cases are also provided in a separate class.
 * 
 * The goal of this project is to implement a simple stack based language with as few operations
 * as possible.  This is a turing complete proof of concept language.
 * 
 * (Development time: 5 hours - this is only a proof of concept implementation)
 * 
 * Example source:
 * <pre>
 *  
 * # This line will add two numbers 100 and 101, 
 * # 201 is pushed on the data stack
 * # 101 is read in first and then 100.
 *  
 * + 100 101
 * 
 * # Lambda call, execute the block of code
 * 
 * lambda ( + 1 1 )  
 * </pre>
 * 
 * @author berlinberlin (berlin.brown at gmail.com)
 * @see OctaneLangOneSourceFileTest
 */
public class OctaneLangOneSourceFile {

    public static final String APP = "OctaneMini";
    public static final String VERSION = "0.0.1";
    
    /**
     * Check for this file in the current working directory.
     */
    public static final String DEFAULT_CWD_SRC_FILE = "main.octane";
    
    public static final char EOF = (char) -1;
    public static final int EOF_TYPE = 1;
    
    private String input;

    /**
     * Byte position pointer during lexing. 
     */
    private int p = 0;
    
    /**
     * Active character used during lexing.
     */
    private char c;

    /**
     * Code stack tokens.
     */
    private Stack<Object> codeStack = new Stack<Object>();
   
    /**
     * Data stack tokens.
     */
    private Stack<Object> dataStack = new Stack<Object>();

    /**
     * Block of code by name. Some blocks are anonymous.
     */
    private Hashtable<String, Stack<Object>> functionCodeStack = new Hashtable<String, Stack<Object>>();
    private Hashtable<String, String> functionCodeLookup = new Hashtable<String, String>();
    /** Variable store for named values. */
    private Hashtable<String, Object> variables = new Hashtable<String, Object>();
    
    /** Current list under construction when list mode is active. */
    private LangTypeList activeList = null;
    /** When true, tokens are added to the active list instead of executed. */
    private boolean modeAddingToList = false;
    
    /**
     * For pointer/array operations.
     */
    private int pointer = 0;    
    /**
     * Memory block for pointer operations.
     */
    private Object activeObjectArray = new int [3000];
    
    private Random random = new Random(System.currentTimeMillis());
    /** Used to generate unique anonymous function identifiers. */
    private static final int uniqid = new Random(System.currentTimeMillis()).nextInt();
    
    /**
     * Verbose output, this is controlled by the code stack.
     */
    private boolean verbose = false;
    
    /**
     * Operations.
     */
    public static final String OP_ADD_FUNC_BLOCK = "ADDFUNC";
    public static final String OP_SET_FUNC_BLOCK = "SETFUNC";

    public static final String OP_EXIT = "exit"; 
    public static final String OP_QUIT = "quit";
    public static final String OP_COMMA_ID = ",";                                
    public static final String OP_IDENTITY = "id";                        
    public static final String OP_TRUE = "true";
    public static final String OP_FALSE = "false";
        
    /**
     * Main entry and starting point for the application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        new OctaneLangOneSourceFile().run(args);
    }

    /**
     * Run with source as a string object.
     * 
     * @param codeSource
     */
    public void run(final String codeSource) {        
        this.input = codeSource;        
        try {
            if (this.verbose) {
                System.out.println("Running : " + APP + "-" + VERSION);
            }
            this.interpret();
        } catch (Exception e) {
            System.out.println(">>>> Error during interpret <<<<");
            e.printStackTrace();
            this.printStack();
            this.printCodeStack();
        }
    }
    
    
    /**
     * Consume tokens and respond to the tokens, interpret.
     */
    public void interpret() {
        this.consume();
        do {
            final Object token = this.nextToken();
            this.codeStack.push(token);
        } while (this.c != EOF);
        this.interpret(this.codeStack, this.dataStack);
    }

    /**
     * Interpret the code stack, visit each token and execute an operation.
     * This method contains a large set of conditional blocks for each operation. 
     * 
     * @param currentCodeStack
     */
    public void interpret(final Stack<Object> currentCodeStack, final Stack<Object> currentDataStack) {

        while (!currentCodeStack.isEmpty()) {

            final Object lastValue = currentCodeStack.pop();
            Object lastArg1 = -1;
            Object lastArg2 = -1;
            Object newValueForStack = -1;
            String lastFuncName = null;
            Object lastDataStack = null;

            // Check for functions on data stack //
            if (currentDataStack.size() > 0 && currentDataStack.peek() instanceof String) {
                lastDataStack = currentDataStack.peek();
            }
            if ("[".equals(String.valueOf(lastValue))) {
                this.modeAddingToList = false;
            }
            if ((lastDataStack != null) && OP_ADD_FUNC_BLOCK.equalsIgnoreCase(String.valueOf(lastDataStack))) {

                final String a = String.valueOf(currentDataStack.pop());
                final String b = String.valueOf(currentDataStack.pop());
                lastFuncName = b;

                // Add this value to the code stack //
                this.functionCodeStack.get(lastFuncName).push(lastValue);
                currentDataStack.push(b);
                currentDataStack.push(a);

                if (lastValue instanceof String) {
                    if ("(".equals(lastValue)) {
                        this.functionCodeStack.get(lastFuncName).pop();
                        Collections.reverse(this.functionCodeStack.get(lastFuncName));                        
                        currentDataStack.pop();
                        currentDataStack.pop();
                        currentDataStack.push(lastFuncName);
                        currentDataStack.push(OP_SET_FUNC_BLOCK);
                    }
                }
            } else if (this.modeAddingToList) {                              
                this.activeList.list.add(lastValue);
            } else {

                // Normal operation //
                if (lastValue instanceof Number) {
                    currentDataStack.push(lastValue);

                } else if (lastValue instanceof LangTypeChar) {
                    currentDataStack.push(lastValue);
                    
                } else if (lastValue instanceof LangTypeString) {
                    currentDataStack.push(lastValue);
                    
                } else if (lastValue instanceof String) {

                    final String token = String.valueOf(lastValue);
                    if (OP_EXIT.equalsIgnoreCase(token) || OP_QUIT.equalsIgnoreCase(token)) {

                        System.out.println("!!!");
                        System.out.println("!!! Exiting - output of stack at exit:");
                        this.printStack();
                        this.printCodeStack();
                        System.exit(0);

                    } else if (OP_COMMA_ID.equals(token)) {                        
                        // Token does nothing, used to breakup code block //
                        
                    } else if (OP_IDENTITY.equals(token)) {                        
                        // Token does nothing, used to breakup code block //
                    
                    } else if (OP_TRUE.equalsIgnoreCase(token)) {
                        currentDataStack.push(true);
                        
                    } else if (OP_FALSE.equalsIgnoreCase(token)) {
                        currentDataStack.push(false);
                        
                    } else if ("+".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = (Double) lastArg1 + (Double) lastArg2;
                        currentDataStack.push(newValueForStack);

                    } else if ("-".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = (Double) lastArg1 - (Double) lastArg2;
                        currentDataStack.push(newValueForStack);

                    } else if ("*".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = (Double) lastArg1 * (Double) lastArg2;
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("/".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = (Double) lastArg1 / (Double) lastArg2;
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("inc".equals(token)) {

                        lastArg1 = currentDataStack.pop();                        
                        newValueForStack = (Double) lastArg1 + 1.0;
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("dec".equals(token)) {

                        lastArg1 = currentDataStack.pop();                        
                        newValueForStack = (Double) lastArg1 - 1.0;
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("mod".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = (Double) lastArg1 % (Double) lastArg2;
                        currentDataStack.push(newValueForStack);

                    } else if ("norem".equals(token)) {

                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        final Double d = ((Double) lastArg1 % (Double) lastArg2);
                        newValueForStack = d.intValue() == 0;
                        currentDataStack.push(newValueForStack);

                        
                    } else if ("eql".equals(token)) {
                        
                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        newValueForStack = lastArg1.equals(lastArg2);
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("or".equals(token)) {
                        
                        final boolean a = (Boolean) currentDataStack.pop();
                        final boolean b = (Boolean) currentDataStack.pop();
                        newValueForStack = a || b; 
                        currentDataStack.push(newValueForStack);
                        
                    } else if ("lambda".equals(token)) {                        
                        // Pull 2 args, last op and then the function name
                        // Execute the block of code
                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();                        
                        final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                        this.interpret(cpst, currentDataStack);                                                
                        
                    } else if ("lambdasav".equals(token)) {                        
                        // Pull 2 args, last op and then the function name
                        // Execute the block of code
                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();                        
                        final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                        final Stack<Object> s = this.copy(currentDataStack); 
                        this.interpret(cpst, s);
                        if (s.size() > 0) {
                            currentDataStack.push(s.pop());
                        }                                                                       
                    } else if ("if".equals(token)) {
                        // Pull 2 args, last op and then the function name
                        // Execute the block of code
                        {
                            lastArg1 = currentDataStack.pop();
                            lastArg2 = currentDataStack.pop();                                                        
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                            this.interpret(cpst, currentDataStack);
                        }
                        // top of the stack should be a boolean
                        final boolean iftrue = (Boolean)currentDataStack.pop();
                        if (iftrue) {
                            lastArg1 = currentDataStack.pop();
                            lastArg2 = currentDataStack.pop();
                            currentDataStack.pop();
                            currentDataStack.pop();
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                            this.interpret(cpst, currentDataStack);                            
                        } else {
                            currentDataStack.pop();
                            currentDataStack.pop();
                            lastArg1 = currentDataStack.pop();
                            lastArg2 = currentDataStack.pop();
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                            this.interpret(cpst, currentDataStack);                            
                        }
                        if (verbose) {
                            System.out.println("* DONE-" + lastArg2);
                        }
                        
                    } else if ("func".equals(token)) {
                        
                        // Pull 2 args, last op and then the function name
                        // Execute the block of code
                        lastArg1 = currentDataStack.pop();
                        lastArg2 = currentDataStack.pop();
                        final Object lastArg3 = currentCodeStack.pop();
                        this.functionCodeLookup.put(String.valueOf(lastArg3), String.valueOf(lastArg2));                        
                        if (verbose) {
                            System.out.println();
                            System.out.println("* Attempt to store block - " + lastArg2 + " as '" + lastArg3 + "'");                        
                            System.out.println("* DONE-" + lastArg2);
                        }
                        
                    } else if ("call".equals(token)) {
                        
                        final String funcname = String.valueOf(currentCodeStack.pop());
                        final String funcid = this.functionCodeLookup.get(funcname);
                        if (verbose) {
                            System.out.println("* Running function by id : " + funcid);
                        }
                        final Stack<Object> cpst = this.copy(this.functionCodeStack.get(funcid));
                        this.interpret(cpst, currentDataStack);                        
                        
                    } else if ("callsav".equals(token)) {
                        
                        // Call but operate on its own stack
                        // pop the last value on to the current data stack
                        final String funcname = String.valueOf(currentCodeStack.pop());
                        final String funcid = this.functionCodeLookup.get(funcname);                        
                        final Stack<Object> cpst = this.copy(this.functionCodeStack.get(funcid));
                        final Stack<Object> s = this.copy(currentDataStack); 
                        this.pointer++;
                        
                    } else if ("ptrdec".equalsIgnoreCase(token)) {
                        this.pointer--;
                        
                    } else if ("ptr".equalsIgnoreCase(token)) {
                        // Put the pointer value on the stack (not the value at the pointer)
                        currentDataStack.push(this.pointer);
                        
                    } else if ("ptrval".equalsIgnoreCase(token)) {
                        // Put value at the pointer on the stack
                        final int [] active = (int [])this.activeObjectArray;
                        currentDataStack.push((double) active[this.pointer]);
                        
                    } else if ("ptrload".equalsIgnoreCase(token)) {
                        
                        final int i = ((Double) currentCodeStack.pop()).intValue();
                        this.pointer = i;
                        
                    } else if ("ptrsetstk".equalsIgnoreCase(token)) {
                        
                        // Pop the value off the data stack and storing
                        final Object o = currentDataStack.peek();
                        final int i = ((Double) o).intValue();
                        final int [] active = (int [])this.activeObjectArray;
                        active[this.pointer] = i;                        
                        
                    } else if ("ptrset".equalsIgnoreCase(token)) {
                        
                        if (verbose && this.pointer < 0) {                            
                            System.out.println("WARN: pointer is less than zero");
                        } else {
                            final int [] active = (int [])this.activeObjectArray;
                            if (verbose && this.pointer >= active.length) {
                                System.out.println("WARN: pointer is larger than memory allocated");
                            } else {
                                final Object o = currentCodeStack.pop();
                                if (o instanceof LangTypeChar) {                                    
                                    final LangTypeChar c = (LangTypeChar) o;
                                    active[this.pointer] = c.toString().charAt(1);
                                } else if (o instanceof Number) {
                                    final int i = ((Double) o).intValue();
                                    active[this.pointer] = i;
                                }
                            }                            
                        }
                        
                    } else if ("ptrplus".equalsIgnoreCase(token)) {
                        if (verbose && this.pointer < 0) {
                            System.out.println("WARN: pointer is less than zero");
                        } else {
                            final int [] active = (int [])this.activeObjectArray;
                            if (verbose && this.pointer >= active.length) {
                                System.out.println("WARN: pointer is larger than memory allocated");
                            } else {
                                active[this.pointer]++;
                            }                            
                        }
                    } else if ("ptrminus".equalsIgnoreCase(token)) {
                        if (verbose && this.pointer < 0) {
                            System.out.println("WARN: pointer is less than zero");
                        } else {
                            final int [] active = (int [])this.activeObjectArray;
                            if (verbose && this.pointer >= active.length) {
                                System.out.println("WARN: pointer is larger than memory allocated");
                            } else {
                                active[this.pointer]--;
                            }                            
                        }
                        
                    } else if ("ptrputc".equalsIgnoreCase(token)) {
                        if (verbose && this.pointer < 0) {
                            System.out.println("WARN: pointer is less than zero");
                        } else {
                            final int [] active = (int [])this.activeObjectArray;
                            if (verbose && this.pointer >= active.length) {
                                System.out.println("WARN: pointer is larger than memory allocated");
                            } else {
                                // Check for ascii printable //
                                if (active[this.pointer] >= 32 && active[this.pointer] <= 126) {
                                    System.out.print(Character.valueOf((char)active[this.pointer]));
                                }
                            }                            
                        }

                    } else if ("ptrwhile".equals(token)) {
                        
                        // Pull 2 args, last op and then the function name
                        // Execute the block of code   
                        int [] active = (int [])this.activeObjectArray;
                        int ptr = active[this.pointer];
                        
                        lastArg1 = currentDataStack.pop();                        
                        lastArg2 = currentDataStack.pop();
                        if (verbose) {
                            System.out.println();
                            System.out.println("* Attempt to execute block - " + lastArg2);
                        }
                        while (ptr != 0) {                                                       
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(String.valueOf(lastArg2)));
                            this.interpret(cpst, currentDataStack);
                            active = (int [])this.activeObjectArray;
                            ptr = active[this.pointer];                            
                        }
                        if (verbose) {
                            System.out.println("* DONE-" + lastArg2);
                        }
                        
                    } else if ("ptrdebug".equals(token)) {
                        
                        int [] active = (int [])this.activeObjectArray;
                        int ptr = active[this.pointer];
                        if (verbose) {
                            System.out.println("* Value of Pointer (Address) : " + this.pointer);
                            System.out.println("* Value at Pointer : " + ptr);
                        }
                        
                    } else if ("verbosedebug".equals(token)) {
                        
                        this.verbose = !this.verbose;
                        System.out.println("* Verbose setting modified : now = " + this.verbose);
                        
                    } else if ("data".equalsIgnoreCase(token)) {
                        this.printStack();

                    } else if ("code".equalsIgnoreCase(token)) {
                        this.printCodeStack();

                    }

                }

            }

        }
    }

    /**
     * Print the data stack.
     */
    public void printStack() {
        System.out.println();
        System.out.println(">>> Data Stack <<<");
        int i = 0;
        final List<Object> s = new ArrayList<Object>(this.dataStack);
        Collections.reverse(s);
        for (final Object obj : s) {
            System.out.println("  -> index-" + (i + 1) + ":  " + obj + " " + (i == 0 ? "(top)" : ""));
            i++;
        }
    }
    
    /**
     * Return a string representation of the data stack.
     * 
     * @return
     */
    public String toStringStack() {
        final List<Object> s = new ArrayList<Object>(this.dataStack);
        Collections.reverse(s);
        final StringBuffer buf = new StringBuffer();
        for (final Object obj : s) {
            buf.append(obj);
            buf.append(" ");                      
        }
        return buf.toString().trim();
    }
    
    /**
     * Return a string representation of the top of the stack.
     * 
     * @return
     */
    public String toStringStackTop() {
        final List<Object> s = new ArrayList<Object>(this.dataStack);
        Collections.reverse(s);
        final StringBuffer buf = new StringBuffer();        
        for (final Object obj : s) {
            buf.append(obj);
            break;
        }
        return buf.toString().trim();
    }

    /**
     * Copy stack.
     * 
     * @param s
     * @return
     */
    public Stack<Object> copy(final Stack<Object> s) {
        final Stack<Object> z = new Stack<Object>();
        for (final Object o: s) {
            z.push(o);
        }
        return z;
    }
    
    /**
     * Output the code stack to console.
     * The code stack contains words for execution.
     */
    public void printCodeStack() {
        System.out.println();
        System.out.println(">>> Code Stack <<<");
        int i = 0;
        final List<Object> s = new ArrayList<Object>(this.codeStack);
        Collections.reverse(s);
        for (final Object obj : s) {
            System.out.println("  -> index-" + (i + 1) + ":  " + obj + " " + (i == 0 ? "(top)" : ""));
            i++;
        }
    }

    /**
     * Output the function stack to the console.
     * The function stack contains a code stack mapped to a function name.
     */
    public void printFunctionStack() {
        System.out.println();
        for (final String keyFunctionName : this.functionCodeStack.keySet()) {
            final Stack<Object> st = this.functionCodeStack.get(keyFunctionName);
            System.out.println("Function : " + keyFunctionName);
            System.out.println("  >>> Function-Stack <<<");
            int i = 0;
            final List<Object> s = new ArrayList<Object>(st);
            Collections.reverse(s);
            for (final Object obj : s) {
                System.out.println("    -> index-" + (i + 1) + ":  " + obj + " " + (i == 0 ? "(top)" : ""));
                i++;
            }
            System.out.println("  End of Function Stack - " + keyFunctionName);
        }
    }

    /**
     * Scan for next token.
     * 
     * Used with lexing on the input code string data.
     * 
     * @return Token
     */
    public Object nextToken() {
        while (c() != EOF) {
            switch (c()) {
            case ' ':
            case '\t':
            case '\n':
            case '\r':
                consumeWhitespace();
                continue;
            case '#':
                consumeComment();
                continue;
            case '(':
                consume();
                return "(";
            case ')':
                consume();
                return ")";                
            case '[':
                consume();
                return "[";
            case ']':
                consume();
                return "]";                
            case ',':
                consume();
                return ",";
            case '"':
                return this.scanString();
            case '\'':
                return this.scanChar();  
            case '+':
                consume();
                return "+";                                        
            case '-':
                consume();
                return "-";                
            case '*':
                consume();
                return "*";            
            case '/':
                consume();
                return "/";
            case '\0':
                return EOF;
            default:
                if (this.isDigit()) {
                    return scanInteger();
                } else if (this.isLetter()) {
                    return scanWord();
                } 
                final int code = (int) c();
                throw new Error("invalid character: {" + c() + "} code:" + code);
            }

        }
        return EOF;
    }

    /**
     * Scan for a token name/integer.
     * Used with lexing on the input code string data.
     * 
     * @return Token
     */
    public LangTypeString scanString() {
        final StringBuilder buf = new StringBuilder();
        consume();
        do {
            buf.append(c());
            consume();
        } while (c != '"');
        consume();    
        return new LangTypeString(buf.toString());
    }
    
    /**
     * Scan for a token name/integer.
     * Used with lexing on the input code string data.
     *  
     * @return Token
     */
    public LangTypeChar scanChar() {
        final StringBuilder buf = new StringBuilder();
        consume();
        do {
            buf.append(c());
            consume();
        } while (c != '\'');
        consume();        
        return new LangTypeChar(buf.toString());
    }
    
    /**
     * Scan for a token name/integer.
     * Used with lexing on the input code string data.
     * 
     * @return Token
     */
    public double scanInteger() {
        final StringBuilder buf = new StringBuilder();
        do {
            buf.append(c());
            consume();
        } while (isDigit());
        final double data = Double.parseDouble(buf.toString());
        return data;
    }

    /**
     * Scan for a token name/integer.
     * Used with lexing on the input code string data.
     * 
     * @return Token
     */
    public String scanWord() {
        final StringBuilder buf = new StringBuilder();
        do {
            buf.append(c());
            consume();
        } while (isLetter());
        return buf.toString();
    }

    /**
     * Check if character is a letter.
     * Used with lexing on the input code string data.
     * 
     * @return boolean
     */
    public boolean isLetter() {
        return (c() >= 'a') && (c() <= 'z') || (c() >= 'A') && (c() <= 'Z');
    }

    /**
     * Check if character is a digit.
     * Used with lexing on the input code string data.
     * 
     * @return boolean
     */
    public boolean isDigit() {
        return (c() >= '0') && (c() <= '9');
    }

    /**
     * Consume and detect whitespace.
     * Used with lexing on the input code string data.
     */
    public void consumeWhitespace() {
        while (c() == ' ' || c() == '\t' || c() == '\n' || c() == '\r') {
            consume();
        }
    }    
    /**
     * Iterate and consume until a newline is encountered
     */
    public void consumeComment() {        
        do {           
            consume();
        } while (c != '\n' && c != '\r');
    }

    /**
     * Consume character.
     * Used with lexing on the input code string data.
     */
    public void consume() {
        if (p >= input.length()) {
            c = EOF;
        } else {
            c = input.charAt(p);
        }
        p++;
    }

    /**
     * Simple String type.
     */
    public static interface LangType {
        
    }
    /**
     * Basic string type.     
     */
    public class LangTypeString implements LangType {
        private final String data;
        public LangTypeString(final String d) {
            this.data = d;
        }
        public String toString() {
            return "'" + data + "'";
        }
    }
    /**
     * A simple character type.
     */
    public class LangTypeChar implements LangType {
        private final String data;
        public LangTypeChar(final String d) {
            this.data = d;
        }
        public String toString() {
            return "'" + data + "'";
        }
    }
    
    /**
     * A simple List/Vector type.
     */
    public class LangTypeList implements LangType {
        private final List<Object> list;
        public LangTypeList() {
            this.list = new ArrayList<Object>();
        }
        public String toString() {
            return "#<" + list + ">";
        }
    }
    
    /**
     * Run interpret with bufferedreader.
     * 
     * @param reader
     */
    public void run(final BufferedReader reader) {       
        try {        
            String data = "";
            final StringBuffer buf = new StringBuffer();
            do {
                data = reader.readLine();
                if (data != null) {                    
                    buf.append(data).append('\n');
                }
            } while(data != null);
            this.run(buf.toString());
        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Invalid input source data at run interpreter");
        }
    }
        
    /**
     * Run against default source file or read the application arguments.
     * @param args
     */
    public void run(final String [] args) {
        
        File f = null;
        FileInputStream stream = null;        
        if (args.length == 0) {
            System.out.println("Running without arguments, no input filename found");
            final File f1 = new File(DEFAULT_CWD_SRC_FILE);            
            if (!f1.exists()) {
                // Write a basic source file
                System.out.println("Writing example source file - " + f1);
                this.writeExampleSourceFile(f1);
            }
            f = f1;
        } else if (args.length == 1) {
            final String filename = args[0];
            f = new File(filename);
        } else {
            throw new IllegalStateException("Invalid input file parameters, could not load source file");
        } // End if - args length zero //
        
        if (f == null) {
            throw new IllegalStateException("Invalid input file parameters, could not load source file");
        }
        try {
            stream = new FileInputStream(f);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            this.run(reader);        
        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Invalid read source file : " + e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Write a source file with example code.
     * 
     * @param f
     */
    protected void writeExampleSourceFile(final File f) {        
        final String src = new StringBuffer()
        .append("############\n")
        .append("# OctaneMini Example Source\n")
        .append("############\n")
        .append("\n\n")
        .append("exit")
        .append("\n\n")                       
        .append(" data + 1 1 ").append("\n\n")
        
        .append("\n\n data + 100 11000 ")
        .append("\n\n data lambda ( + + 1 1 + 1 1)  ")
        .append("\n\n data - swap 5 10 ")
        .append("\n\n data lambda (+ 1 1)  ")
        .append("\n\n data myf call , myf func (- 5 10) , ")
        .append("\n\n data myfunc call , myfunc func ( - ) 5 10 ") 
        .append("\n\n data ptr ptrval ptrplus , ptrval 'h' ptrset ")
        .append("\n\n data ptrval , ptr , ptrdec ptr ptrdec , ptr , ptrwhile ( 100 ptrinc ) , ptrdec ptrdec , ptrinc ptrplus , ptrinc ptrplus ")
        .append("\n\n data if ( false ) ( + 1 1 ) ( + 2 2 )   ")
        .append("\n\n data if (true) (+ 1 1) (+ 2 2)   ")
        .append("\n\n data ptrval ptrinc , ptrwhile ( ptrdec ptrplus ptrplus ptrinc, ptrminus ) , ptrval 1000 ptrset ")
        .append("\n\n data ifstk , or norem 5 2 norem 5 2 , 1 0")
        .append("\n\n data x loop [ 1 2 3 4 5 ] , x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk ) ")
        .append("\n\n data sumstk , x loop range 10, x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk ) ")
        
        .toString();           
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(f);
            final BufferedOutputStream buf = new BufferedOutputStream(stream);
            final PrintWriter writer = new PrintWriter(buf);
            writer.println(src);
            writer.flush();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }            
        }
    }
        
    /**
     * Return the current character for lexing.
     * 
     * @return
     */
    public char c() {       
        return c;
    }

    /**
     * Set the active character for lexing.
     * 
     * @param c
     */
    public void setc(final char c) {
        this.c = c;
    }

    /**
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(String input) {
        this.input = input;
    }   
    
    /**
     * Return the code stack.  
     * @return the codeStack
     */
    public Stack<Object> getCodeStack() {
        return codeStack;
    }

    /**
     * Return the datastack.  The data stack contains words and number values that
     * have been pushed on the stack.
     * 
     * @return the dataStack
     */
    public Stack<Object> getDataStack() {
        return dataStack;
    }

    /**
     * @return the functionCodeStack
     */
    public Hashtable<String, Stack<Object>> getFunctionCodeStack() {
        return functionCodeStack;
    }

    /**
     * @return the functionCodeLookup
     */
    public Hashtable<String, String> getFunctionCodeLookup() {
        return functionCodeLookup;
    }

    /**
     * @return the activeObject
     */
    public Object getActiveObject() {
        return activeObjectArray;
    }
}
