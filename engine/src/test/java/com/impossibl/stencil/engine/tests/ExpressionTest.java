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

public class ExpressionTest extends Tests {
  
  @Test
  public void testParen() {
    assertMatch("$3+(6*2);", "15");
    assertMatch("$(3+6)*2;", "18");
    assertMatch("$18/(3*3)/2;", "1");
  }

  @Test
  public void testAdd() {
    assertMatch("$3+3;", "6");
    assertMatch("$3+'3';", "33");
    assertMatch("$[3]+3;", "[3, 3]");
  }

  @Test
  public void testSubtract() {
    assertMatch("$3-2;", "1");
    assertMatch("$var x=3;$assign x-=2;$x;", "1");
  }

  @Test
  public void testMultiply() {
    assertMatch("$3*2;", "6");
    assertMatch("$var x=3;$assign x*=2;$x;", "6");
  }

  @Test
  public void testDivide() {
    assertMatch("$3/3;", "1");
    assertMatch("$var x=3;$assign x/=3;$x;", "1");
  }

  @Test
  public void testModuls() {
    assertMatch("$5%3;", "2");
    assertMatch("$var x=5;$assign x%=3;$x;", "2");
  }

  @Test
  public void testLeftShift() {
    assertMatch("$1<<2;", "4");
    assertMatch("$var x=1;$assign x<<=2;$x;", "4");
  }

  @Test
  public void testRightShift() {
    assertMatch("$4>>2;", "1");
    assertMatch("$var x=4;$assign x>>=2;$x;", "1");
  }
  
  @Test
  public void testBitAnd() {
    assertMatch("$90&165;", "0");
    assertMatch("$var x=90;$assign x&=165;$x;", "0");
  }

  @Test
  public void testBitOr() {
    assertMatch("$90|165;", "255");
    assertMatch("$var x=90;$assign x|=165;$x;", "255");
  }

  @Test
  public void testBitXor() {
    assertMatch("$90^90;", "0");
    assertMatch("$var x=90;$assign x^=90;$x;", "0");
  }

  @Test
  public void testLogicalAnd() {
    assertMatch("$true&&true;", "true");
    assertMatch("$true&&false;", "false");
  }

  @Test
  public void testLogicalOr() {
    assertMatch("$true||false;", "true");
    assertMatch("$false||false;", "false");
  }
  
  @Test
  public void testInstance() {
    assertMatch("$[] instanceof java.util.ArrayList;", "true");
    assertMatch("$[] isa java.util.ArrayList;", "true");
    assertMatch("$[] instanceof java.util.HashSet;", "false");
    assertMatch("$$import java.util.ArrayList;$[] instanceof ArrayList;", "true");
    assertMatch("$$import java.util.ArrayList as Coll;$[] instanceof Coll;", "true");
  }
  
  @Test
  public void testNegate() {
    assertMatch("$-90;", "-90");
  }

  @Test
  public void testAddate() {
    assertMatch("$+90;", "90");
  }

  @Test
  public void testInc() {
    assertMatch("$++90;", "91");
  }

  @Test
  public void testDec() {
    assertMatch("$--90;", "89");
  }

  @Test
  public void testBitNegate() {
    assertMatch("$~90;", "-91");
  }

  @Test
  public void testLogicalNegate() {
    assertMatch("$!true;", "false");
    assertMatch("$!false;", "true");
  }

  @Test
  public void testEquality() {
    assertMatch("$3==3;", "true");
    assertMatch("$3!=3;", "false");
    assertMatch("$3!=2;", "true");
    assertMatch("$3!=3;", "false");
    assertMatch("$3.0f==3;", "true");
    assertMatch("$3.0d==3;", "true");
    assertMatch("$3==3.0d;", "true");
    assertMatch("$3.0f==3.0d;", "true");
    assertMatch("$3.0d==3.0d;", "true");
    assertMatch("$3==3.0f;", "true");
    assertMatch("$3.0f==3.0f;", "true");
    assertMatch("$3.0d==3.0f;", "true");
    assertMatch("$3>2;", "true");
    assertMatch("$2<3;", "true");
    assertMatch("$3<2;", "false");
    assertMatch("$2>3;", "false");
    assertMatch("$3<=3;", "true");
    assertMatch("$3<=2;", "false");
    assertMatch("$3>=3;", "true");
    assertMatch("$3>=2;", "true");
    assertMatch("$'test'=='test';","true");
    assertMatch("$'test'!='tst';","true");
    assertMatch("$'test'==true;","true");
    assertMatch("$'test'==false;","false");
    assertMatch("$''==false;","true");
    assertMatch("$''==true;","false");
    assertMatch("$var x='Yo',y='Yo';$x==x;", "true");
    assertMatch("$var x='Yo',y='Yo';$x==y;", "true");
    assertMatch("$var x='Yo',y='Yo';$x===x;", "true");
    assertMatch("$var x='Yo',y='Yo';$x===y;", "false");
    assertMatch("$var x='Yo',y='Yo';$x!==x;", "false");
    assertMatch("$var x='Yo',y='Yo';$x!==y;", "true");
}

  @Test
  public void testTernary() {
    assertMatch("$'VALUE'?'TRUE':'FALSE';", "TRUE");
    assertMatch("$''?'TRUE':'FALSE';", "FALSE");
    assertMatch("$'VALUE'?:'FALSE';", "VALUE");
    assertMatch("$''?:'FALSE';", "FALSE");
    assertMatch("$null?:'FALSE';", "FALSE");
    assertMatch("$true?'TRUE';", "TRUE");
    assertMatch("$false?'TRUE';", "");
  }

  @Test
  public void testSafeNavigation() {
    assertMatch("$'STRING'?.invalidProperty.another;", "");
    assertMatch("$var x='STRING';$assign x?.invalidProperty?.another='WOW';", "");
  }
  
  @Test
  public void testIndexSelector() {
    assertMatch("$[1,2,'3'][0];","1");
    assertMatch("$[x=1,y=2,z='3']['x'];","1");
  }
  
  @Test
  public void testMemberIndexSelector() {
    assertMatch("$'\000test'.bytes[0];","0");
  }
  
  @Test
  public void testMemberSelector() {
    assertMatch("$[].class;","class java.util.ArrayList");
  }
  
  @Test
  public void testMethodCallSelector() {
    assertMatch("$[].toString();","[]");
  }
  
  @Test
  public void testCallSelector() {
    assertMatch("$func x() { return 'X'; };$x();", "X");
    assertMatch("$$import java.util.ArrayList as AL;$AL();", "[]");
  }
  
  @Test
  public void testListLiteral() {
    assertMatch("$[1,2,'3'];","[1, 2, 3]");
    assertMatch("$[];","[]");
  }

  @Test
  public void testMapLiteral() {
    assertMatch("$[x=1,y=2,z='3'];","{z=3, y=2, x=1}");
    assertMatch("$['...'=1,'4-5'=2,'xx!'='3'];","{...=1, 4-5=2, xx!=3}");
    assertMatch("$[=];","{}");
  }

  @Test
  public void testRangeLiteral() {
    assertMatch("$$import java.util.ArrayList;$ArrayList([1..6]);","[1, 2, 3, 4, 5]");
    assertMatch("$$import java.util.ArrayList;$ArrayList([1+0..3+3]);","[1, 2, 3, 4, 5]");
    assertMatch("$[..];","[]");
  }

}
