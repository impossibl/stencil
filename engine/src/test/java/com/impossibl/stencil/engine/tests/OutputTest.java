/**
 * Copyright (c) 2013, impossibl.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of  nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.impossibl.stencil.engine.tests;

import org.junit.Test;

import com.impossibl.stencil.engine.ParseException;
import com.impossibl.stencil.engine.UndefinedVariableException;

public class OutputTest extends Tests {

  @Test
  public void testExpressionOutput() {    
    assertMatch("$'######';", "######");
  }
  
  @Test
  public void testDeclarationOutput() {    
    assertMatch("$var x='######';$x;", "######");
  }
  
  @Test
  public void testAssignOutput() {    
    assertMatch("$var x=10;$assign x='######';$x;", "######");
  }
    
  @Test(expected=UndefinedVariableException.class)
  public void testInvalidAssignOutput() throws ParseException {
    eval("$assign x=0;");
  }

  @Test
  public void testForeachOutput() {
    
    assertMatch("$foreach x in [1,2,3,4,5] {$x;};", "12345");
    assertMatch("$foreach x,xit in [1,2,3,4,5] {$xit.index;,};", "0,1,2,3,4,");
    assertMatch("$foreach x,xit in [1,2,3,4,5] {$xit.count;,};", "1,2,3,4,5,");
    assertMatch("$foreach x,xit in [1,2,3,4,5] {$xit.even;,};", "false,true,false,true,false,");
    assertMatch("$foreach x,xit in [1,2,3,4,5] {$xit.odd;,};", "true,false,true,false,true,");
    assertMatch("$foreach x,xit in [1,2,3,4,5] {$xit.hasNext;,};", "true,true,true,true,false,");
    assertMatch("$foreach x in [] {SOME} else {NONE};", "NONE");
    
  }  

  @Test
  public void testWhileOutput() {
    
    assertMatch("$var x=0;$while x < 5 {$x;$assign x=++x;};", "01234");
    
  }
  
  @Test
  public void testBreakOutput() {
    assertMatch("$foreach x in [0,1,2] {INCLUDED$break;EXCLUDED};", "INCLUDED");
    assertMatch("$while true {INCLUDED$break;EXCLUDED};", "INCLUDED");
    assertMatch("$foreach x in [0,1,2] {$while true {INCLUDED$break;EXCLUDED};};", "INCLUDEDINCLUDEDINCLUDED");
  }

  @Test
  public void testContinueOutput() {
    assertMatch("$foreach x in [0,1,2] {INCLUDED$continue;EXCLUDED};", "INCLUDEDINCLUDEDINCLUDED");
    assertMatch("$var x=0;$while x < 3 {$assign x=++x;INCLUDED$continue;EXCLUDED};", "INCLUDEDINCLUDEDINCLUDED");
    assertMatch("$foreach y in [0,1,2] {$var x=0;$while x < 3 {$assign x=++x;INCLUDED$continue;EXCLUDED};STOP};", "INCLUDEDINCLUDEDINCLUDEDSTOPINCLUDEDINCLUDEDINCLUDEDSTOPINCLUDEDINCLUDEDINCLUDEDSTOP");
  }

  @Test
  public void testIfOutput() {
    
    assertMatch("$if true {TRUE} else {FALSE};", "TRUE");
    assertMatch("$if false {TRUE} else {FALSE};", "FALSE");
    
  }
  
  @Test
  public void testSwitchOutput() {
    
    assertMatch("$switch 'yes' case 'yes' {TRUE} case 'no' {FALSE} default {NONE};", "TRUE");
    assertMatch("$switch 'no' case 'yes' {TRUE} case 'no' {FALSE} default {NONE};", "FALSE");
    assertMatch("$switch 'i-dont-know' case 'yes' {TRUE} case 'no' {FALSE} default {NONE};", "NONE");
    
  }
  
  @Test
  public void testWithOutput() {
    
    assertMatch("$with [name='Robin',greeting='Hello'] {$greeting; $name;};", "Hello Robin");
    assertMatch("$with [] {$class.simpleName;};", "ArrayList");
    
  }
  
  @Test
  public void testIncludeOutput() {
    
    assertMatch("$include 'src/test/resources/include.stencil';", "##INCLUDED####INCLUDED##");
    assertMatch("$include 'src/test/resources/include.stencil' ('PARAM');", "##INCLUDED##PARAM##INCLUDED##");
    assertMatch("$include 'src/test/resources/include.stencil' {BLOCK};", "##INCLUDED##BLOCK##INCLUDED##");
    assertMatch("$include 'src/test/resources/include.stencil' ('PARAM') {BLOCK};", "##INCLUDED##PARAMBLOCK##INCLUDED##");
    
  }
  
  @Test
  public void testDoubleParenBlocks() {

    assertMatch("$raw() {{$ { { } $}{ } { { ${ { } } } ${ { $}};", "$ { { } $}{ } { { ${ { } } } ${ { $");
    assertMatch("$var x='VALUE';$raw() {{$ { { } $}{ } $$x;{ { ${ { } } } ${ { $}};", "$ { { } $}{ } VALUE{ { ${ { } } } ${ { $");
    
  }
  
}
