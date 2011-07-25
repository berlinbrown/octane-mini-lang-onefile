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
 * **********************************************
 * File : OctaneLangOneSourceFile.java
 * Date: 7/25/2011
 * bbrown
 * Contact: Berlin Brown <berlin dot brown at gmail.com>
 * 
 * What is this application used for? : Simple proof of concept programming language
 *           It is a simple stack based programming language built on top of Java.  It is 
 *           very basic, almost esoteric (in the of BrainF**k).
 *           
 *           OctaneMini, simple programming language in Java with one source file.  The entire implementation
 *           is contained in this file.   A suite of test cases are also provided in a separate class.
 * 
 *           The goal of this project is to implement a simple stack based language with as few operations
 *           as possible.  This is a turing complete proof of concept language.
 * 
 * keywords: java, simple, brainf**k, bf, simple, stack language, forth like, joy like
 * 
 * URLs:
 *   https://octane.googlecode.com/svn/trunk/projects/misc/java/OctaneMini/
 *   
 *   https://github.com/berlinbrown
 *   https://gist.github.com/berlinbrown
 *   http://twitter.com/#!/berlinbrowndev2
 *   http://code.google.com/p/jvmnotebook/
 *   http://code.google.com/p/octane/
 *   http://code.google.com/p/doingitwrongnotebook/
 *   
 * **********************************************
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
    private Hashtable<String, Object> variables = new Hashtable<String, Object>();
    
    private LangTypeList activeList = null;
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

    } // End of Method //

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
        } // End of the try - catch //
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
                } // End if - check string //
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
                        this.interpret(cpst, s);
                        if (s.size() > 0) {
                            currentDataStack.push(s.pop());
                        }                        
                    } else if (")".equals(token)) {

                        // Create a unique function name //
                        final int i = random.nextInt();
                        final int z = new Random().nextInt();
                        final String a = String.valueOf(System.currentTimeMillis());
                        final String b = String.valueOf(uniqid);
                        final String uniqueFunctionName = "FUNC-" + a + "-" + i + "-" + b + "-" + z;
                        currentDataStack.push(uniqueFunctionName);
                        currentDataStack.push(OP_ADD_FUNC_BLOCK);
                        this.functionCodeStack.put(uniqueFunctionName, new Stack<Object>());  

                    } else if ("range".equals(token)) {
                        final int sizeCreate = ((Double) currentDataStack.pop()).intValue();
                        final LangTypeList ll = new LangTypeList();
                        final List<Object> lst = ll.list;
                        for (int i = 0; i < sizeCreate; i++) {
                            lst.add(new Double(i));
                        }
                        currentDataStack.push(ll);
                        
                    } else if ("]".equals(token)) {
                        this.modeAddingToList = true;
                        this.activeList = new LangTypeList();
                        
                    } else if ("[".equals(token)) {
                        this.modeAddingToList = false;
                        Collections.reverse(this.activeList.list);
                        currentDataStack.push(this.activeList);
                        
                    } else if ("dup".equalsIgnoreCase(token)) {

                        final Object obj = currentDataStack.pop();
                        currentDataStack.push(obj);
                        currentDataStack.push(obj);                                                                
                        
                    } else if ("pop".equalsIgnoreCase(token)) {
                        currentDataStack.pop();
                                            
                    } else if ("swap".equalsIgnoreCase(token)) {
                        
                        final Object obj1 = currentDataStack.pop();
                        final Object obj2 = currentDataStack.pop();
                        currentDataStack.push(obj1);
                        currentDataStack.push(obj2);
                        
                    } else if ("sumstk".equalsIgnoreCase(token)) {
                        
                        double sum = 0;
                        for (final Object o: currentDataStack) {
                            if (o instanceof Number) {
                                sum += (Double) o;
                            }
                        }
                        currentDataStack.push(sum);
                        
                    } else if ("sum".equalsIgnoreCase(token)) {
                        
                        double sum = 0;
                        final LangTypeList lst = (LangTypeList) currentDataStack.pop();
                        for (final Object o: lst.list) {
                            if (o instanceof Number) {
                                sum += (Double) o;
                            }
                        }
                        currentDataStack.push(sum);
                        
                    } else if ("loop".equalsIgnoreCase(token)) {
                        
                        final String funcname = String.valueOf(currentCodeStack.pop());
                        final String funcid = this.functionCodeLookup.get(funcname);                                                
                                                                                                                        
                        final LangTypeList lst = (LangTypeList) currentDataStack.pop();
                        for (final Object o: lst.list) {
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(funcid));
                            final Stack<Object> stkForCall = new Stack<Object>();
                            stkForCall.push(o);
                            this.interpret(cpst, stkForCall);
                            if (stkForCall.size() > 0) {                                
                                currentDataStack.push(stkForCall.pop());
                            }
                        }                        
                        
                    } else if ("ifstk".equals(token)) {
                        
                        final boolean a = (Boolean) currentDataStack.pop();
                        final Object b = currentDataStack.pop();
                        final Object c = currentDataStack.pop();
                        if (a) {
                            currentDataStack.push(b);
                        } else {
                            currentDataStack.push(c);
                        }
                        
                    } else if ("callstk".equals(token)) {
                        
                        final String funcname = String.valueOf(currentCodeStack.pop());
                        final String funcid = this.functionCodeLookup.get(funcname);
                        final int sz = currentDataStack.size();
                        for (int i = 0; i < sz; i++) {                            
                            final Stack<Object> cpst = this.copy(this.functionCodeStack.get(funcid));
                            this.interpret(cpst, currentDataStack);                            
                        }
                        
                    } else if ("array-deprecatd-xxx".equalsIgnoreCase(token)) {
                                                
                        final int arraySizeCreate = Integer.parseInt(String.valueOf(currentDataStack.pop())); 
                        final String name = String.valueOf(currentCodeStack.pop());
                        if (verbose) {
                            System.out.println("* Creating array of size : "+ arraySizeCreate + " '" + name + "'");
                        }
                        this.variables.put(name, new int [arraySizeCreate]);
                                          
                    } else if ("ptrinc".equalsIgnoreCase(token)) {
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
                        } // End of if pointer checks.
                        
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
                        } // End of if pointer checks.
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
                        } // End of if pointer checks.
                        
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
                        } // End of if pointer checks.

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

                    } // End of if - handle tokens

                } // End if - else //

            } // End of if - else - not function code block

        } // End of While //
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
        } // End of the For //
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
        } // End of the For //
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
        } // End of the For //
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
        } // End of the For //
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
            } // End of the For //
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
            } // End of Switch //

        } // End of While //
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
        } // End of the if - else //
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
        } // End of the try - catch //        
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
        } // End of try - catch finally //       
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

} // End of the Class //

// These are the operations from the joy programming language:
// OctaneMini is not based on Joy but tries to implement some of the operations.
// You can use the code/documentation as a loose guide as to how some of the OctaneMini operations work.
// 
// http://www.kevinalbrecht.com/code/joy-mirror/joy.html
// http://en.wikipedia.org/wiki/Joy_(programming_language)
/*
 * {" truth value type", dummy_, "->  B",
 * "The logical type, or the type of truth values.\nIt has just two literals: true and false."
 * },
 * {" character type", dummy_, "->  C",
 * "The type of characters. Literals are written with a single quote.\nExamples:  'A  '7  ';  and so on. Unix style escapes are allowed."
 * },
 * {" integer type", dummy_, "->  I",
 * "The type of negative, zero or positive integers.\nLiterals are written in decimal notation. Examples:  -123   0   42."
 * },
 * {" set type", dummy_, "->  {...}",
 * "The type of sets of small non-negative integers.\nThe maximum is platform dependent, typically the range is 0..31.\nLiterals are written inside curly braces.\nExamples:  {}  {0}  {1 3 5}  {19 18 17}."
 * },
 * {" string type", dummy_, "->  \"...\" ",
 * "The type of strings of characters. Literals are written inside double quotes.\nExamples: \"\"  \"A\"  \"hello world\" \"123\".\nUnix style escapes are accepted."
 * },
 * {" list type", dummy_, "->  [...]",
 * "The type of lists f values of any type (including lists),\nor the type of quoted programs which may contain operators or combinators.\nLiterals of this type are written inside square brackets.\nExamples: []  [3 512 -7]  [john mary]  ['A 'C ['B]]  [dup *]."
 * }, 
 * {" float type", dummy_, "->  F",
 * "The type of floating-point numbers.\nLiterals of this type are written with embedded decimal points (like 1.2)\nand optional exponent specifiers (like 1.5E2)"
 * }, 
 * {" file type", dummy_, "->  FILE:",
 * "The type of references to open I/O streams,\ntypically but not necessarily files.\nThe only literals of this type are stdin, stdout, and stderr."
 * },
 * {"false", false_, "->  false", "Pushes the value false."},
 * {"true", true_, "->  true", "Pushes the value true."},
 * 
 * {"maxint", maxint_, "->  maxint",
 * "Pushes largest integer (platform dependent). Typically it is 32 bits."},
 * 
 * {"setsize", setsize_, "->  setsize",
 * "Pushes the maximum number of elements in a set (platform dependent).\nTypically it is 32, and set members are in the range 0..31."
 * },
 * 
 * {"stack", stack_, ".. X Y Z  ->  .. X Y Z [Z Y X ..]",
 * "Pushes the stack as a list."}, 
 * 
 * {"conts", conts_, "->  [[P] [Q] ..]",
 * "Pushes current continuations. Buggy, do not use."},
 * 
 * {"autoput", autoput_, "->  I",
 * "Pushes current value of flag  for automatic output, I = 0..2."},
 * 
 * {"undeferror", undeferror_, "->  I",
 * "Pushes current value of undefined-is-error flag."},
 * 
 * {"undefs", undefs_, "->",
 * "Push a list of all undefined symbols in the current symbol table."},
 * 
 * {"echo", echo_, "->  I", "Pushes value of echo flag, I = 0..3."},
 * 
 * {"clock", clock_, "->  I",
 * "Pushes the integer value of current CPU usage in hundreds of a second."},
 * 
 * {"time", time_, "->  I",
 * "Pushes the current time (in seconds since the Epoch)."},
 * 
 * {"rand", rand_, "  -> I", "I is a random integer."},
 * 
 * {"__memorymax", memorymax_, "->", "Pushes value of total size of memory."},
 * 
 * {"stdin", stdin_, "->  S", "Pushes the standard input stream."},
 * 
 * {"stdout", stdout_, "->  S", "Pushes the standard output stream."},
 * 
 * {"stderr", stderr_, "->  S", "Pushes the standard error stream."},
 * 
 * {"id", id_, "->",
 * "Identity function, does nothing.\nAny program of the form  P id Q  is equivalent to just  P Q."
 * },
 * 
 * {"dup", dup_, " X  ->   X X", "Pushes an extra copy of X onto stack."},
 * 
 * {"swap", swap_, " X Y  ->   Y X",
 * "Interchanges X and Y on top of the stack."},
 * 
 * {"rollup", rollup_, "X Y Z  ->  Z X Y", "Moves X and Y up, moves Z down"},
 * 
 * {"rolldown", rolldown_, "X Y Z  ->  Y Z X",
 * "Moves Y and Z down, moves X up"},
 * 
 * {"rotate", rotate_, "X Y Z  ->  Z Y X", "Interchanges X and Z"},
 * 
 * {"popd", popd_, "Y Z  ->  Z", "As if defined by:   popd  ==  [pop] dip "},
 * 
 * {"dupd", dupd_, "Y Z  ->  Y Y Z", "As if defined by:   dupd  ==  [dup] dip"},
 * 
 * {"swapd", swapd_, "X Y Z  ->  Y X Z",
 * "As if defined by:   swapd  ==  [swap] dip"},
 * 
 * {"rollupd", rollupd_, "X Y Z W  ->  Z X Y W",
 * "As if defined by:   rollupd  ==  [rollup] dip"},
 * 
 * {"rolldownd", rolldownd_, "X Y Z W  ->  Y Z X W",
 * "As if defined by:   rolldownd  ==  [rolldown] dip "},
 * 
 * {"rotated", rotated_, "X Y Z W  ->  Z Y X W",
 * "As if defined by:   rotated  ==  [rotate] dip"},
 * 
 * {"pop", pop_, " X  ->", "Removes X from top of the stack."},
 * 
 * {"choice", choice_, "B T F  ->  X", "If B is true, then X = T else X = F."},
 * 
 * {"or", or_, "X Y  ->  Z",
 * "Z is the union of sets X and Y, logical disjunction for truth values."},
 * 
 * {"xor", xor_, "X Y  ->  Z",
 * "Z is the symmetric difference of sets X and Y,\nlogical exclusive disjunction for truth values."
 * },
 * 
 * {"and", and_, "X Y  ->  Z",
 * "Z is the intersection of sets X and Y, logical conjunction for truth values."
 * },
 * 
 * {"not", not_, "X  ->  Y",
 * "Y is the complement of set X, logical negation for truth values."},
 * 
 * {"+", plus_, "M I  ->  N",
 * "Numeric N is the result of adding integer I to numeric M.\nAlso supports float."
 * },
 * 
 * {"-", minus_, "M I  ->  N",
 * "Numeric N is the result of subtracting integer I from numeric M.\nAlso supports float."
 * },
 * 
 * {"*", mul_, "I J  ->  K",
 * "Integer K is the product of integers I and J.  Also supports float."},
 * 
 * {"/", divide_, "I J  ->  K",
 * "Integer K is the (rounded) ratio of integers I and J.  Also supports float."
 * },
 * 
 * {"rem", rem_, "I J  ->  K",
 * "Integer K is the remainder of dividing I by J.  Also supports float."},
 * 
 * {"div", div_, "I J  ->  K L",
 * "Integers K and L are the quotient and remainder of dividing I by J."},
 * 
 * {"sign", sign_, "N1  ->  N2",
 * "Integer N2 is the sign (-1 or 0 or +1) of integer N1,\nor float N2 is the sign (-1.0 or 0.0 or 1.0) of float N1."
 * },
 * 
 * {"neg", neg_, "I  ->  J",
 * "Integer J is the negative of integer I.  Also supports float."},
 * 
 * {"ord", ord_, "C  ->  I",
 * "Integer I is the Ascii value of character C (or logical or integer)."},
 * 
 * {"chr", chr_, "I  ->  C",
 * "C is the character whose Ascii value is integer I (or logical or character)."
 * },
 * 
 * {"abs", abs_, "N1  ->  N2",
 * "Integer N2 is the absolute value (0,1,2..) of integer N1,\nor float N2 is the absolute value (0.0 ..) of float N1"
 * },
 * 
 * {"acos", acos_, "F  ->  G", "G is the arc cosine of F."},
 * 
 * {"asin", asin_, "F  ->  G", "G is the arc sine of F."},
 * 
 * {"atan", atan_, "F  ->  G", "G is the arc tangent of F."},
 * 
 * {"atan2", atan2_, "F G  ->  H", "H is the arc tangent of F / G."},
 * 
 * {"ceil", ceil_, "F  ->  G", "G is the float ceiling of F."},
 * 
 * {"cos", cos_, "F  ->  G", "G is the cosine of F."},
 * 
 * {"cosh", cosh_, "F  ->  G", "G is the hyperbolic cosine of F."},
 * 
 * {"exp", exp_, "F  ->  G",
 * "G is e (2.718281828...) raised to the Fth power."},
 * 
 * {"floor", floor_, "F  ->  G", "G is the floor of F."},
 * 
 * {"frexp", frexp_, "F  ->  G I",
 * "G is the mantissa and I is the exponent of F.\nUnless F = 0, 0.5 <= abs(G) < 1.0."
 * },
 * 
 * {"ldexp", ldexp_, "F I  -> G", "G is F times 2 to the Ith power."},
 * 
 * {"log", log_, "F  ->  G", "G is the natural logarithm of F."},
 * 
 * {"log10", log10_, "F  ->  G", "G is the common logarithm of F."},
 * 
 * {"modf", modf_, "F  ->  G H",
 * "G is the fractional part and H is the integer part\n(but expressed as a float) of F."
 * },
 * 
 * {"pow", pow_, "F G  ->  H", "H is F raised to the Gth power."},
 * 
 * {"sin", sin_, "F  ->  G", "G is the sine of F."},
 * 
 * {"sinh", sinh_, "F  ->  G", "G is the hyperbolic sine of F."},
 * 
 * {"sqrt", sqrt_, "F  ->  G", "G is the square root of F."},
 * 
 * {"tan", tan_, "F  ->  G", "G is the tangent of F."},
 * 
 * {"tanh", tanh_, "F  ->  G", "G is the hyperbolic tangent of F."},
 * 
 * {"trunc", trunc_, "F  ->  I",
 * "I is an integer equal to the float F truncated toward zero."},
 * 
 * {"strftime", strftime_, "T S1  ->  S2",
 * "Formats a list T in the format of localtime or gmtime\nusing string S1 and pushes the result S2."
 * },
 * 
 * {"strtol", strtol_, "S I  ->  J",
 * "String S is converted to the integer J using base I.\nIf I = 0, assumes base 10,\nbut leading \"0\" means base 8 and leading \"0x\" means base 16."
 * },
 * 
 * {"strtod", strtod_, "S  ->  R", "String S is converted to the float R."},
 * 
 * {"format", format_, "N C I J  ->  S",
 * "S is the formatted version of N in mode C\n('d or 'i = decimal, 'o = octal, 'x or\n'X = hex with lower or upper case letters)\nwith maximum width I and minimum width J."
 * },
 * 
 * {"srand", srand_, "I  ->  ", "Sets the random integer seed to integer I."},
 * 
 * {"pred", pred_, "M  ->  N", "Numeric N is the predecessor of numeric M."},
 * 
 * {"succ", succ_, "M  ->  N", "Numeric N is the successor of numeric M."},
 * 
 * {"max", max_, "N1 N2  ->  N",
 * "N is the maximum of numeric values N1 and N2.  Also supports float."},
 * 
 * {"min", min_, "N1 N2  ->  N",
 * "N is the minimum of numeric values N1 and N2.  Also supports float."},
 * 
 * {"fclose", fclose_, "S  ->  ",
 * "Stream S is closed and removed from the stack."},
 * 
 * {"feof", feof_, "S  ->  S B", "B is the end-of-file status of stream S."},
 * 
 * {"ferror", ferror_, "S  ->  S B", "B is the error status of stream S."},
 * 
 * {"fflush", fflush_, "S  ->  S",
 * "Flush stream S, forcing all buffered output to be written."},
 * 
 * {"fgetch", fgetch_, "S  ->  S C",
 * "C is the next available character from stream S."},
 * 
 * {"fgets", fgets_, "S  ->  S L",
 * "L is the next available line (as a string) from stream S."},
 * 
 * {"fopen", fopen_, "P M  ->  S",
 * "The file system object with pathname P is opened with mode M (r, w, a, etc.)\nand stream object S is pushed; if the open fails, file:NULL is pushed."
 * },
 * 
 * {"fread", fread_, "S I  ->  S L",
 * "I bytes are read from the current position of stream S\nand returned as a list of I integers."
 * },
 * 
 * {"fwrite", fwrite_, "S L  ->  S",
 * "A list of integers are written as bytes to the current position of stream S."
 * },
 * 
 * {"fremove", fremove_, "P  ->  B",
 * "The file system object with pathname P is removed from the file system.\n is a boolean indicating success or failure."
 * },
 * 
 * {"frename", frename_, "P1 P2  ->  B",
 * "The file system object with pathname P1 is renamed to P2.\nB is a boolean indicating success or failure."
 * },
 * 
 * {"fput", fput_, "S X  ->  S", "Writes X to stream S, pops X off stack."},
 * 
 * {"fputch", fputch_, "S C  ->  S",
 * "The character C is written to the current position of stream S."},
 * 
 * {"fputchars", fputchars_, "S \"abc..\"  ->  S",
 * "The string abc.. (no quotes) is written to the current position of stream S."
 * },
 * 
 * {"fputstring", fputchars_, "S \"abc..\"  ->  S",
 * "== fputchars, as a temporary alternative."},
 * 
 * {"fseek", fseek_, "S P W  ->  S",
 * "Stream S is repositioned to position P relative to whence-point W,\nwhere W = 0, 1, 2 for beginning, current position, end respectively."
 * },
 * 
 * {"ftell", ftell_, "S  ->  S I", "I is the current position of stream S."},
 * 
 * {"unstack", unstack_, "[X Y ..]  ->  ..Y X",
 * "The list [X Y ..] becomes the new stack."},
 * 
 * {"cons", cons_, "X A  ->  B",
 * "Aggregate B is A with a new member X (first member for sequences)."},
 * 
 * {"swons", swons_, "A X  ->  B",
 * "Aggregate B is A with a new member X (first member for sequences)."},
 * 
 * {"first", first_, "A  ->  F",
 * "F is the first member of the non-empty aggregate A."},
 * 
 * {"rest", rest_, "A  ->  R",
 * "R is the non-empty aggregate A with its first member removed."},
 * 
 * {"compare", compare_, "A B  ->  I",
 * "I (=-1,0,+1) is the comparison of aggregates A and B.\nThe values correspond to the predicates <=, =, >=."
 * },
 * 
 * {"at", at_, "A I  ->  X", "X (= A[I]) is the member of A at position I."},
 * 
 * {"of", of_, "I A  ->  X", "X (= A[I]) is the I-th member of aggregate A."},
 * 
 * {"size", size_, "A  ->  I",
 * "Integer I is the number of elements of aggregate A."},
 * 
 * {"opcase", opcase_, "X [..[X Xs]..]  ->  [Xs]",
 * "Indexing on type of X, returns the list [Xs]."},
 * 
 * {"case", case_, "X [..[X Y]..]  ->  Y i",
 * "Indexing on the value of X, execute the matching Y."},
 * 
 * {"uncons", uncons_, "A  ->  F R",
 * "F and R are the first and the rest of non-empty aggregate A."},
 * 
 * {"unswons", unswons_, "A  ->  R F",
 * "R and F are the rest and the first of non-empty aggregate A."},
 * 
 * {"drop", drop_, "A N  ->  B",
 * "Aggregate B is the result of deleting the first N elements of A."},
 * 
 * {"take", take_, "A N  ->  B",
 * "Aggregate B is the result of retaining just the first N elements of A."},
 * 
 * {"concat", concat_, "S T  ->  U",
 * "Sequence U is the concatenation of sequences S and T."},
 * 
 * {"enconcat", enconcat_, "X S T  ->  U",
 * "Sequence U is the concatenation of sequences S and T\nwith X inserted between S and T (== swapd cons concat)"
 * },
 * 
 * {"name", name_, "sym  ->  \"sym\"",
 * "For operators and combinators, the string \"sym\" is the name of item sym,\nfor literals sym the result string is its type."
 * },
 * {"intern", intern_, "\"sym\"  -> sym",
 * "Pushes the item whose name is \"sym\"."},
 * 
 * {"body", body_, "U  ->  [P]",
 * "Quotation [P] is the body of user-defined symbol U."},
 * {"null", null_, "X  ->  B", "Tests for empty aggregate X or zero numeric."},
 * {"small", small_, "X  ->  B",
 * "Tests whether aggregate X has 0 or 1 members, or numeric 0 or 1."},
 * 
 * {">=", geql_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X greater than or equal to Y.  Also supports float."
 * },
 * 
 * {">", greater_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X greater than Y.  Also supports float."
 * },
 * 
 * {"<=", leql_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X less than or equal to Y.  Also supports float."
 * },
 * 
 * {"<", less_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X less than Y.  Also supports float."
 * },
 * 
 * {"!=", neql_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X not equal to Y.  Also supports float."
 * },
 * 
 * {"=", eql_, "X Y  ->  B",
 * "Either both X and Y are numeric or both are strings or symbols.\nTests whether X equal to Y.  Also supports float."
 * },
 * {"equal", equal_, "T U  ->  B",
 * "(Recursively) tests whether trees T and U are identical."},
 * {"has", has_, "A X  ->  B", "Tests whether aggregate A has X as a member."},
 * {"in", in_, "X A  ->  B", "Tests whether X is a member of aggregate A."},
 * {"integer", integer_, "X  ->  B", "Tests whether X is an integer."},
 * 
 * {"char", char_, "X  ->  B", "Tests whether X is a character."},
 * 
 * {"logical", logical_, "X  ->  B", "Tests whether X is a logical."},
 * 
 * {"set", set_, "X  ->  B", "Tests whether X is a set."},
 * 
 * {"string", string_, "X  ->  B", "Tests whether X is a string."},
 * 
 * {"list", list_, "X  ->  B", "Tests whether X is a list."},
 * 
 * {"leaf", leaf_, "X  ->  B", "Tests whether X is not a list."},
 * 
 * {"user", user_, "X  ->  B", "Tests whether X is a user-defined symbol."},
 * 
 * {"float", float_, "R  ->  B", "Tests whether R is a float."},
 * 
 * {"file", file_, "F  ->  B", "Tests whether F is a file."},
 * 
 * {"i", i_, "[P]  ->  ...", "Executes P. So, [P] i  ==  P."},
 * 
 * {"x", x_, "[P]i  ->  ...",
 * "Executes P without popping [P]. So, [P] x  ==  [P] P."},
 * 
 * {"dip", dip_, "X [P]  ->  ... X", "Saves X, executes P, pushes X back."},
 * 
 * {"app1", app1_, "X [P]  ->  R",
 * "Executes P, pushes result R on stack without X."},
 * 
 * {"app11", app11_, "X Y [P]  ->  R", "Executes P, pushes result R on stack."},
 * 
 * {"app12", app12_, "X Y1 Y2 [P]  ->  R1 R2",
 * "Executes P twice, with Y1 and Y2, returns R1 and R2."},
 * 
 * {"construct", construct_, "[P] [[P1] [P2] ..]  ->  R1 R2 ..",
 * "Saves state of stack and then executes [P].\nThen executes each [Pi] to give Ri pushed onto saved stack."
 * },
 * 
 * {"nullary", nullary_, "[P]  ->  R",
 * "Executes P, which leaves R on top of the stack.\nNo matter how many parameters this consumes, none are removed from the stack."
 * },
 * 
 * {"unary", unary_, "X [P]  ->  R",
 * "Executes P, which leaves R on top of the stack.\nNo matter how many parameters this consumes,\nexactly one is removed from the stack."
 * },
 * 
 * {"unary2", unary2_, "X1 X2 [P]  ->  R1 R2",
 * "Executes P twice, with X1 and X2 on top of the stack.\nReturns the two values R1 and R2."
 * },
 * 
 * {"unary3", unary3_, "X1 X2 X3 [P]  ->  R1 R2 R3",
 * "Executes P three times, with Xi, returns Ri (i = 1..3)."},
 * 
 * {"unary4", unary4_, "X1 X2 X3 X4 [P]  ->  R1 R2 R3 R4",
 * "Executes P four times, with Xi, returns Ri (i = 1..4)."},
 * 
 * {"app2", unary2_, "X1 X2 [P]  ->  R1 R2", "Obsolescent.  == unary2"},
 * 
 * {"app3", unary3_, "X1 X2 X3 [P]  ->  R1 R2 R3", "Obsolescent.  == unary3"},
 * 
 * {"app4", unary4_, "X1 X2 X3 X4 [P]  ->  R1 R2 R3 R4",
 * "Obsolescent.  == unary4"},
 * 
 * {"binary", binary_, "X Y [P]  ->  R",
 * "Executes P, which leaves R on top of the stack.\nNo matter how many parameters this consumes,\nexactly two are removed from the stack."
 * },
 * 
 * {"ternary", ternary_, "X Y Z [P]  ->  R",
 * "Executes P, which leaves R on top of the stack.\nNo matter how many parameters this consumes,\nexactly three are removed from the stack."
 * },
 * 
 * {"cleave", cleave_, "X [P1] [P2]  ->  R1 R2",
 * "Executes P1 and P2, each with X on top, producing two results."},
 * 
 * {"branch", branch_, "B [T] [F]  ->  ...",
 * "If B is true, then executes T else executes F."},
 * 
 * {"ifte", ifte_, "[B] [T] [F]  ->  ...",
 * "Executes B. If that yields true, then executes T else executes F."},
 * 
 * {"ifinteger", ifinteger_, "X [T] [E]  ->  ...",
 * "If X is an integer, executes T else executes E."},
 * 
 * {"ifchar", ifchar_, "X [T] [E]  ->  ...",
 * "If X is a character, executes T else executes E."},
 * 
 * {"iflogical", iflogical_, "X [T] [E]  ->  ...",
 * "If X is a logical or truth value, executes T else executes E."},
 * 
 * {"ifset", ifset_, "X [T] [E]  ->  ...",
 * "If X is a set, executes T else executes E."},
 * 
 * {"ifstring", ifstring_, "X [T] [E]  ->  ...",
 * "If X is a string, executes T else executes E."},
 * 
 * {"iflist", iflist_, "X [T] [E]  ->  ...",
 * "If X is a list, executes T else executes E."},
 * 
 * {"iffloat", iffloat_, "X [T] [E]  ->  ...",
 * "If X is a float, executes T else executes E."},
 * 
 * {"iffile", iffile_, "X [T] [E]  ->  ...",
 * "If X is a file, executes T else executes E."},
 * 
 * {"cond", cond_, "[..[[Bi] Ti]..[D]]  ->  ...",
 * "Tries each Bi. If that yields true, then executes Ti and exits.\nIf no Bi yields true, executes default D."
 * },
 * 
 * {"while", while_, "[B] [D]  ->  ...",
 * "While executing B yields true executes D."},
 * 
 * {"linrec", linrec_, "[P] [T] [R1] [R2]  ->  ...",
 * "Executes P. If that yields true, executes T.\nElse executes R1, recurses, executes R2."
 * },
 * 
 * {"tailrec", tailrec_, "[P] [T] [R1]  ->  ...",
 * "Executes P. If that yields true, executes T.\nElse executes R1, recurses."},
 * 
 * {"binrec", binrec_, "[B] [T] [R1] [R2]  ->  ...",
 * "Executes P. If that yields true, executes T.\nElse uses R1 to produce two intermediates, recurses on both,\nthen executes R2 to combines their results."
 * },
 * 
 * {"genrec", genrec_, "[B] [T] [R1] [R2]  ->  ...",
 * "Executes B, if that yields true executes T.\nElse executes R1 and then [[B] [T] [R1] [R2] genrec] R2."
 * },
 * 
 * {"condnestrec", condnestrec_, "[ [C1] [C2] .. [D] ]  ->  ...",
 * "A generalisation of condlinrec. Each [Ci] is of the form [[B] [R1] [R2] .. [Rn]] and [D] is of the form [[R1] [R2] .. [Rn]]. Tries each B, or if all fail, takes the default [D]. For the case taken, executes each [Ri] but recurses between any two consecutive [Ri]. (n > 3 would be exceptional.)"
 * },
 * 
 * {"condlinrec", condlinrec_, "[ [C1] [C2] .. [D] ]  ->  ...",
 * "Each [Ci] is of the forms [[B] [T]] or [[B] [R1] [R2]].\nTries each B. If that yields true and there is just a [T], executes T and exit.\nIf there are [R1] and [R2], executes R1, recurses, executes R2.\nSubsequent case are ignored. If no B yields true, then [D] is used.\nIt is then of the forms [[T]] or [[R1] [R2]]. For the former, executes T.\nFor the latter executes R1, recurses, executes R2."
 * },
 * 
 * {"step", step_, "A  [P]  ->  ...",
 * "Sequentially putting members of aggregate A onto stack,\nexecutes P for each member of A."
 * },
 * 
 * {"fold", fold_, "A V0 [P]  ->  V",
 * "Starting with value V0, sequentially pushes members of aggregate A\nand combines with binary operator P to produce value V."
 * },
 * 
 * {"map", map_, "A [P]  ->  B",
 * "Executes P on each member of aggregate A,\ncollects results in sametype aggregate B."
 * },
 * 
 * {"times", times_, "N [P]  ->  ...", "N times executes P."},
 * 
 * {"infra", infra_, "L1 [P]  ->  L2",
 * "Using list L1 as stack, executes P and returns a new list L2.\nThe first element of L1 is used as the top of stack,\nand after execution of P the top of stack becomes the first element of L2."
 * },
 * 
 * {"primrec", primrec_, "X [I] [C]  ->  R",
 * "Executes I to obtain an initial value R0.\nFor integer X uses increasing positive integers to X, combines by C for new R.\nFor aggregate X uses successive members and combines by C for new R."
 * },
 * 
 * {"filter", filter_, "A [B]  ->  A1",
 * "Uses test B to filter aggregate A producing sametype aggregate A1."},
 * 
 * {"split", split_, "A [B]  ->  A1 A2",
 * "Uses test B to split aggregate A into sametype aggregates A1 and A2 ."},
 * 
 * {"some", some_, "A  [B]  ->  X",
 * "Applies test B to members of aggregate A, X = true if some pass."},
 * 
 * {"all", all_, "A [B]  ->  X",
 * "Applies test B to members of aggregate A, X = true if all pass."},
 * 
 * {"treestep", treestep_, "T [P]  ->  ...",
 * "Recursively traverses leaves of tree T, executes P for each leaf."},
 * 
 * {"treerec", treerec_, "T [O] [C]  ->  ...",
 * "T is a tree. If T is a leaf, executes O. Else executes [[O] [C] treerec] C."
 * },
 * 
 * {"treegenrec", treegenrec_, "T [O1] [O2] [C]  ->  ...",
 * "T is a tree. If T is a leaf, executes O1.\nElse executes O2 and then [[O1] [O2] [C] treegenrec] C."
 * },
 *
 * {"setecho", setecho_, "I ->",
 * "Sets value of echo flag for listing.\nI = 0: no echo, 1: echo, 2: with tab, 3: and linenumber."
 * },
 * {"system", system_, "\"command\"  ->",
 * "Escapes to shell, executes string \"command\".\nThe string may cause execution of another program.\nWhen that has finished, the process returns to Joy."
 * },
 * 
 * {"getenv", getenv_, "\"variable\"  ->  \"value\"",
 * "Retrieves the value of the environment variable \"variable\"."},
 * 
 * {"argv", argv_, "-> A",
 * "Creates an aggregate A containing the interpreter's command line arguments."
 * },
 * 
 * {"argc", argc_, "-> I",
 * "Pushes the number of command line arguments. This is quivalent to 'argv size'."
 * },
 * 
 * {"__memoryindex", memoryindex_, "->", "Pushes current value of memory."},
 * {"get", get_, "->  F",
 * "Reads a factor from input and pushes it onto stack."},
 * 
 * {"put", put_, "X  ->", "Writes X to output, pops X off stack."},
 * 
 * {"putch", putch_, "N  ->",
 * "N : numeric, writes character whose ASCII is N."},
 * 
 * {"putchars", putchars_, "\"abc..\"  ->", "Writes  abc.. (without quotes)"},
 * 
 * {"quit", quit_, "->", "Exit from Joy."}, 
 * 
 * End of joy operations 
 */
