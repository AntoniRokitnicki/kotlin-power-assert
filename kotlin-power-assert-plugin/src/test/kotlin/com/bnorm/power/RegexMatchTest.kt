/*
 * Copyright (C) 2021 Brian Norman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bnorm.power

import org.junit.Test
import kotlin.test.assertEquals

class RegexMatchTest {
  @Test
  fun `regex matches`() {
    val actual = executeMainAssertion("""assert("Hello, World".matches("[A-Za-z]+".toRegex()))""")
    assertEquals(
      """
      Assertion failed
      assert("Hello, World".matches("[A-Za-z]+".toRegex()))
                            |                   |
                            |                   [A-Za-z]+
                            false
      """.trimIndent(),
      actual
    )
  }

  @Test
  fun `infix regex matches`() {
    val actual = executeMainAssertion("""assert("Hello, World" matches "[A-Za-z]+".toRegex())""")
    assertEquals(
      """
      Assertion failed
      assert("Hello, World" matches "[A-Za-z]+".toRegex())
                            |                   |
                            |                   [A-Za-z]+
                            false
      """.trimIndent(),
      actual
    )
  }
}
