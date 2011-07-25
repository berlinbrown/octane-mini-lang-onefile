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

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Example test cases for OctaneMiniProof.
 * 
 * OctaneMini, simple programming language in Java with one source file.  The entire implementation
 * is contained in this file.   A suite of test cases are also provided in a separate class.
 * 
 * The goal of this project is to implement a simple stack based language with as few operations
 * as possible.  This is a turing complete proof of concept language.
 * 
 * (Development time: 5 hours - this is only a proof of concept implementation)
 * 
 * @author berlinberlin (berlin.brown at gmail.com)
 * @see OctaneLangOneSourceFileTest
 */
public class OctaneLangOneSourceFileTest extends TestCase {

    public void testAdd1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" + 1 1 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("2.0", lexer.toStringStack());
    }
    
    public void testAdd2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" + 100 11000 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("11100.0", lexer.toStringStack());
    }
    
    public void testAdd3() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                               
        .append(" lambda ( + + 1 1 + 1 1)  ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("4.0", lexer.toStringStack());
    }
    
    public void testSub1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" - 5 10 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("-5.0", lexer.toStringStack());
    }
    
    public void testSub3() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" - swap 5 10 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("5.0", lexer.toStringStack());
    }
    
    public void testSub2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" - 10 5 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("5.0", lexer.toStringStack());
    }
    
    public void testMult1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" * 10 5 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("50.0", lexer.toStringStack());
    }
        
    public void testLambda1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" lambda (+ 1 1)  ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("2.0", lexer.toStringStack());
    }
    
    public void testFunc1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" myf call , myf func (- 5 10) , ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("-5.0", lexer.toStringStack());
    }
    
    public void testFunc2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")
        .append(" myfunc call , myfunc func ( - ) 5 10 ")        
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("-5.0", lexer.toStringStack());
        
    }
    
    public void testPointer1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" ptr ptrval ptrplus , ptrval 'h' ptrset ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("0 105.0 104.0", lexer.toStringStack());
    }
    
    public void testPointer2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" ptrval , ptr , ptrdec ptr ptrdec , ptr , ptrwhile ( 100 ptrinc ) , ptrdec ptrdec , ptrinc ptrplus , ptrinc ptrplus ")        
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("1.0 0 1 2 100.0 100.0", lexer.toStringStack());
    }
    
    public void testIf1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" if ( false ) ( + 1 1 ) ( + 2 2 )   ")               
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("4.0", lexer.toStringStack());        
    }
    public void testIf2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" if (true) (+ 1 1) (+ 2 2)   ")               
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("2.0", lexer.toStringStack());        
    }
    
    public void testPointer3() {
        
        // Complex example, count down to 1000, for one pointer value, increment by two
        // then put the value back on the stack.
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")
        .append(" ptrval ptrinc , ptrwhile ( ptrdec ptrplus ptrplus ptrinc, ptrminus ) , ptrval 1000 ptrset ")               
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("2000.0 1000.0", lexer.toStringStack());
        
    }
    
    public void testIf4() {
        
        // Complex example, count down to 1000, for one pointer value, increment by two
        // then put the value back on the stack.
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("")                       
        .append(" if (")
        .append("     or")
        .append("       eql 0 , mod 88 3")
        .append("       eql 0 , mod 88 5")     
        .append(" ) ")
        .append(" ( 1 ) ")
        .append(" ( 2 ) ")        
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("2.0", lexer.toStringStack());
        
    }
    
    public void testOr2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append("or norem 4 2 norem 5 2")                 
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("true", lexer.toStringStack());
        
    }
    
    public void testIf3() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append(" ifstk , or norem 5 2 norem 5 2 , 1 0")                 
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("0.0", lexer.toStringStack());
        
    }
    
    public void testPtrVal2() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()        
        .append(" ifstk , or norem ptrval 2 norem ptrval 2 , 1 0 , ptrsetstk 4 ")
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("1.0 4.0", lexer.toStringStack());        
    }
    
    public void testPtrVal() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append(" ptrval ptrsetstk * dup 2 ")                        
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("4.0 4.0", lexer.toStringStack());       
    }
    
    public void testEulerSimple1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append(" x loop [ 1 2 3 4 5 ] , x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk ) ")                               
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("5.0 0.0 3.0 0.0 0.0", lexer.toStringStack());        
    }
    
    public void testEulerProblem1() {
        final OctaneLangOneSourceFile lexer = new OctaneLangOneSourceFile();
        lexer.setInput(new StringBuffer()
        .append(" sumstk , x loop range 1000, x func ( ifstk , or norem ptrval 3 norem ptrval 5 , ptrval 0 , ptrsetstk ) ")                               
        .toString());                       
        lexer.interpret();
        TestCase.assertEquals("233168.0", lexer.toStringStackTop());        
    }
    
    public static void main(final String [] args) {
        System.out.println("Running tests");
        if (args.length == 0) {
            // Run all of the tests
            junit.textui.TestRunner.run(OctaneLangOneSourceFileTest.class);
        } else {
            // Run only the named tests
            TestSuite suite = new TestSuite("Selected tests");
            for (int i = 0; i < args.length; i++) {
                final TestCase test = new OctaneLangOneSourceFileTest();
                test.setName(args[i]);
                suite.addTest(test);
            }
            junit.textui.TestRunner.run(suite);
        }
    }
    
} // End of the Class //
