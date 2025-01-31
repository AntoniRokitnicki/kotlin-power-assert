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

import org.jetbrains.kotlin.name.FqName
import org.junit.Test

class AssertLibraryTest {
  @Test
  fun `test assertTrue transformation`() {
    assertMessage(
      """
      import kotlin.test.assertTrue
      
      fun main() {
        assertTrue(1 != 1)
      }""",
      """
      Assertion failed
      assertTrue(1 != 1)
                   |
                   false
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertTrue")))
    )
  }

  @Test
  fun `test assertTrue transformation with message`() {
    assertMessage(
      """
      import kotlin.test.assertTrue
      
      fun main() {
        assertTrue(1 != 1, "Message:")
      }""",
      """
      Message:
      assertTrue(1 != 1, "Message:")
                   |
                   false
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertTrue")))
    )
  }

  @Test
  fun `test assertFalse transformation`() {
    assertMessage(
      """
      import kotlin.test.assertFalse
      
      fun main() {
        assertFalse(1 == 1)
      }""",
      """
      Assertion failed
      assertFalse(1 == 1)
                    |
                    true
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertFalse")))
    )
  }

  @Test
  fun `test assertFalse transformation with message`() {
    assertMessage(
      """
      import kotlin.test.assertFalse
      
      fun main() {
        assertFalse(1 == 1, "Message:")
      }""",
      """
      Message:
      assertFalse(1 == 1, "Message:")
                    |
                    true
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertFalse")))
    )
  }

  @Test
  fun `test assertEquals transformation`() {
    assertMessage(
      """
      import kotlin.test.assertEquals
      
      fun main() {
        val greeting = "Hello"
        val name = "World"
        assertEquals(greeting, name)
      }""",
      """
      assertEquals(greeting, name)
                   |         |
                   |         World
                   Hello expected:<[Hello]> but was:<[World]>
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertEquals")))
    )
  }

  @Test
  fun `test assertEquals transformation with message`() {
    assertMessage(
      """
      import kotlin.test.assertEquals
      
      fun main() {
        val greeting = "Hello"
        val name = "World"
        assertEquals(greeting, name, "Message:")
      }""",
      """
      Message:
      assertEquals(greeting, name, "Message:")
                   |         |
                   |         World
                   Hello expected:<[Hello]> but was:<[World]>
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertEquals")))
    )
  }

  @Test
  fun `test assertNotNull transformation`() {
    assertMessage(
      """
      import kotlin.test.assertNotNull
      
      fun main() {
        val name: String? = null
        assertNotNull(name)
      }""",
      """
      assertNotNull(name)
                    |
                    null
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertNotNull")))
    )
  }

  @Test
  fun `test assertNotNull transformation with message`() {
    assertMessage(
      """
      import kotlin.test.assertNotNull
      
      fun main() {
        val name: String? = null
        assertNotNull(name, "Message:")
      }""",
      """
      Message:
      assertNotNull(name, "Message:")
                    |
                    null
      """.trimIndent(),
      PowerAssertComponentRegistrar(setOf(FqName("kotlin.test.assertNotNull")))
    )
  }
}
