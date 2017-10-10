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

public class StatementTest extends Tests {
  
  @Test
  public void testExpressionStatement() {
    assertMatch("$$import java.util.ArrayList;$func add(list) { list.add('RES'); };$var list=ArrayList();$add(list);$list[0];", "RES");
  }

  @Test
  public void testReturnStatement() {
    assertMatch("$func ret() {return 'RES';};$ret();", "RES");
    assertMatch("$func src() {return 'SRC';};$func ret() {return src()+'RES';};$ret();", "SRCRES");
  }

  @Test
  public void testDeclarationStatement() {
    assertMatch("$func decl() {var x='RES';return x;};$decl();", "RES");
  }

  @Test
  public void testAssignmentStatement() {
    assertMatch("$func assigner() {var x=null;x='RES';return x;};$assigner();", "RES");
  }

  @Test
  public void testIfStatement() {
    assertMatch("$func test() { if true return 'TRUE'; else return 'FALSE';};$test();", "TRUE");
    assertMatch("$func test() { if false return 'TRUE'; else return 'FALSE';};$test();", "FALSE");
  }

  @Test
  public void testForeachStatement() {
    assertMatch("$func test() { var r=''; foreach x in [0,1,2] r+=x; return r;};$test();", "012");
    assertMatch("$func test() { var r=''; foreach x,it in [0,1,2] r+=it.index; return r;};$test();", "012");
    assertMatch("$func test() { var r=''; foreach x,it in [0,1,2] r+=it.count; return r;};$test();", "123");
    assertMatch("$func test() { var r=''; foreach x,it in [0,1,2] r+=it.hasNext; return r;};$test();", "truetruefalse");
    assertMatch("$func test() { var r=''; foreach x,it in [0,1,2] r+=it.even; return r;};$test();", "falsetruefalse");
    assertMatch("$func test() { var r=''; foreach x,it in [0,1,2] r+=it.odd; return r;};$test();", "truefalsetrue");
    assertMatch("$func test() { var r=''; foreach x,it in [] r+=it.index; return r;};$test();", "");
  }
  
  @Test
  public void testWhileStatement() {
    assertMatch("$func test() { var x=0, r=''; while x < 5 {r+=x; x+=1;} return r;};$test();", "01234");
  }

  @Test
  public void testBreakStatement() {
    assertMatch("$func test() { var r=''; foreach x in [0,1,2] {r+='INCLUDED';break;r+='EXCLUDED';}return r;};$test();", "INCLUDED");
    assertMatch("$func test() { var r=''; while true {r+='INCLUDED';break;r+='EXCLUDED';}return r;};$test();", "INCLUDED");
    assertMatch("$func test() { var r=''; foreach x in [0,1,2] while true {r+='INCLUDED';break;r+='EXCLUDED';}return r;};$test();", "INCLUDEDINCLUDEDINCLUDED");
  }

  @Test
  public void testContinueOutput() {
    assertMatch("$func test() { var r=''; foreach x in [0,1,2] {r+='INCLUDED';continue;r+='EXCLUDED';}return r;};$test();", "INCLUDEDINCLUDEDINCLUDED");
    assertMatch("$func test() { var r='',x=0; while x < 3 {x+=1;r+='INCLUDED';continue;r+='EXCLUDED';}return r;};$test();", "INCLUDEDINCLUDEDINCLUDED");
    assertMatch("$func test() { var r=''; foreach y in [0,1,2] {var x=0; while x < 3 {x+=1;r+='INCLUDED';continue;r+='EXCLUDED';}r+='STOP';}return r;};$test();", "INCLUDEDINCLUDEDINCLUDEDSTOPINCLUDEDINCLUDEDINCLUDEDSTOPINCLUDEDINCLUDEDINCLUDEDSTOP");
  }

  @Test
  public void testSwitchStatement() {
    
    assertMatch("$func test() { var r=''; switch 'yes' { case 'yes': r='TRUE'; case 'no': r='FALSE'; default: r='NONE'; } return r;};$test();", "TRUE");
    assertMatch("$func test() { var r=''; switch 'no' { case 'yes': r='TRUE'; case 'no': r='FALSE'; default: r='NONE'; } return r;};$test();", "FALSE");
    assertMatch("$func test() { var r=''; switch 'idk' { case 'yes': r='TRUE'; case 'no': r='FALSE'; default: r='NONE'; } return r;};$test();", "NONE");
    
  }
  
  @Test
  public void testWithStatement() {
    
    assertMatch("$func test() { var r=''; with [name='Robin',greeting='Hello'] { r=greeting+' '+name;} return r;};$test();", "Hello Robin");
    assertMatch("$func test() { var r=''; with [] {r=class.simpleName;} return r;};$test();", "ArrayList");
    
  }
  
}
