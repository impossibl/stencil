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

public class MacroTest extends Tests {

  @Test
  public void testBlockReplaceMode() {
    assertMatch("$macro test() [a] {$a {###AAA###};};$test replace a {###BBB###};", "###BBB###");
    assertMatch("$macro test() [a] {$a {###AAA###};};$test a {###BBB###};", "###BBB###");
  }
  
  @Test
  public void testBlockAfterMode() {
    assertMatch("$macro test() [a] {$a {###AAA###};};$test after a {###BBB###};", "###AAA######BBB###");
  }

  @Test
  public void testBlockBeforeMode() {
    assertMatch("$macro test() [a] {$a {###AAA###};};$test before a {###BBB###};", "###BBB######AAA###");
  }

  @Test
  public void testDefaultParameter() {
    assertMatch("$macro test(a='Hello',b='World!') {$a; $b;};$test();", "Hello World!");
  }
  
  @Test
  public void testUnboundBlock() {
    assertMatch("$macro test() [a,b] {$a;$b;};$test() a {###AAA###};", "###AAA###");
  }
  
  @Test
  public void testDefaultBlock() {
    assertMatch("$macro test() [a,b] {$a;$b {###BBB###};};$test() a {###AAA###};", "###AAA######BBB###");
  }
  
  @Test
  public void testAllParameter() {
    assertMatch("$macro test(*a) {$a;};$test(a=1,b=2);","{a=1, b=2}");
  }
  
  @Test
  public void testUnnamedBlock() {
    assertMatch("$macro test() [+a] {$a;};$test {UNNAMED};","UNNAMED");
  }
  
  @Test
  public void testAllBlock() {
    assertMatch("$macro test() [*a] {$a;};$test a {AAA} b {BBB};","{b=BBB, a=AAA}");
  }
  
  @Test
  public void testAllAndUnnamedBlock() {
    assertMatch("$macro test() [+a,*b] {$a;$b;};$test {UNNAMED} a {AAA} b {BBB};","UNNAMED{b=BBB, a=AAA}");
  }
  
  @Test
  public void testRestOfBlocks() {
    assertMatch("$macro test() [a,+b,*c] {$c;};$test {UNNAMED} a {AAA} b {BBB} c {CCC};","{b=BBB, c=CCC}");
  }
  
}
