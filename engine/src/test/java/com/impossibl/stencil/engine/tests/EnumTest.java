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

import com.google.common.collect.ImmutableMap;
import com.impossibl.stencil.engine.ParseException;

public class EnumTest extends Tests {
  
  public enum Tester {
    AnEnumVal
  }
  
  public enum Tester2 {
    AnEnumVal
  }
  
  public class NonString {
    public String toString() {
      return "AnEnumVal";
    }
  }

  @Test
  public void testStringCompare() throws ParseException {
    assertMatch("$$(val);$val=='AnEnumVal'?'MATCHED';", ImmutableMap.<String,Object>of("val", Tester.AnEnumVal), "MATCHED");
  }

  @Test
  public void testDifferentEnumCompare() throws ParseException {
    assertMatch("$$(val1,val2);$val1==val2?'MATCHED';", ImmutableMap.<String,Object>of("val1", Tester.AnEnumVal, "val2", Tester2.AnEnumVal), "MATCHED");
  }

  @Test
  public void testNonStringCompare() throws ParseException {
    assertMatch("$$(val1,val2);$val1==val2?'MATCHED';", ImmutableMap.<String,Object>of("val1", Tester.AnEnumVal, "val2", new NonString()), "MATCHED");
  }

}
