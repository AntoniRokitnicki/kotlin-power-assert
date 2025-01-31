/*
 * Copyright (C) 2020 Brian Norman
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

package com.bnorm.power.diagram

import com.bnorm.power.irString
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

fun IrBuilderWithScope.irDiagramString(
  file: IrFile,
  fileSource: String,
  prefix: IrExpression? = null,
  original: IrExpression,
  variables: List<IrTemporaryVariable>
): IrExpression {

  val originalInfo = file.info(original)
  val callIndent = originalInfo.startColumnNumber

  val stackValues = variables.map { it.toValueDisplay(fileSource, callIndent, file, originalInfo) }

  val valuesByRow = stackValues.groupBy { it.row }
  val rows = fileSource.substring(original)
    .replace("\n" + " ".repeat(callIndent), "\n") // Remove additional indentation
    .split("\n")

  return irConcat().apply {
    if (prefix != null) addArgument(prefix)

    for ((row, rowSource) in rows.withIndex()) {
      val rowValues = valuesByRow[row]?.let { values -> values.sortedBy { it.indent } } ?: emptyList()
      val indentations = rowValues.map { it.indent }

      addArgument(
        irString {
          if (row != 0 || prefix != null) appendLine()
          append(rowSource)
          if (indentations.isNotEmpty()) {
            appendLine()
            var last = -1
            for (i in indentations) {
              if (i > last) indent(i - last - 1).append("|")
              last = i
            }
          }
        }
      )

      for (tmp in rowValues.asReversed()) {
        addArgument(
          irString {
            appendLine()
            var last = -1
            for (i in indentations) {
              if (i == tmp.indent) break
              if (i > last) indent(i - last - 1).append("|")
              last = i
            }
            indent(tmp.indent - last - 1)
          }
        )
        addArgument(irGet(tmp.value))
      }
    }
  }
}

private data class ValueDisplay(
  val value: IrVariable,
  val indent: Int,
  val row: Int,
  val source: String
)

private fun IrTemporaryVariable.toValueDisplay(
  fileSource: String,
  callIndent: Int,
  file: IrFile,
  originalInfo: SourceRangeInfo
): ValueDisplay {
  val source = fileSource.substring(original)
    .replace("\n" + " ".repeat(callIndent), "\n") // Remove additional indentation

  val info = file.info(original)
  var indent = info.startColumnNumber - callIndent
  var row = info.startLineNumber - originalInfo.startLineNumber

  val columnOffset = findDisplayOffset(original, source)

  val prefix = source.substring(0, columnOffset)
  val rowShift = prefix.count { it == '\n' }
  if (rowShift == 0) {
    indent += columnOffset
  } else {
    row += rowShift
    indent = columnOffset - (prefix.lastIndexOf('\n') + 1)
  }

  return ValueDisplay(temporary, indent, row, source)
}

/**
 * Responsible for determining the diagram display offset of the expression
 * beginning from the startOffset of the expression.
 *
 * Equality:
 * ```
 * number == 42
 * | <- startOffset
 *        | <- display offset: 7
 * ```
 *
 * Arithmetic:
 * ```
 * i + 2
 * | <- startOffset
 *   | <- display offset: 2
 * ```
 *
 * Infix:
 * ```
 * 1 shl 2
 * | <- startOffset
 *   | <- display offset: 2
 * ```
 *
 * Standard:
 * ```
 * 1.shl(2)
 *   | <- startOffset
 *   | <- display offset: 0
 * ```
 */
private fun findDisplayOffset(
  expression: IrExpression,
  source: String
): Int {
  if (expression !is IrMemberAccessExpression<*>) return 0

  if (expression.origin == IrStatementOrigin.EXCLEQ || expression.origin == IrStatementOrigin.EXCLEQEQ) {
    // special case to handle `value != null`
    return source.indexOf("!=")
  }

  val owner = expression.symbol.owner
  if (owner !is IrSimpleFunction) return 0

  if (owner.isInfix || owner.isOperator || owner.origin == IrBuiltIns.BUILTIN_OPERATOR) {
    // Ignore single value operators
    val singleReceiver = (expression.dispatchReceiver != null) xor (expression.extensionReceiver != null)
    if (singleReceiver && expression.valueArgumentsCount == 0) return 0

    // Start after the dispatcher or first argument
    val receiver = expression.dispatchReceiver
      ?: expression.extensionReceiver
      ?: expression.getValueArgument(0).takeIf { owner.origin == IrBuiltIns.BUILTIN_OPERATOR }
      ?: return 0
    var offset = receiver.endOffset - expression.startOffset
    if (receiver is IrConst<*> && receiver.kind == IrConstKind.String) offset++ // String constants don't include the quote
    if (offset < 0 || offset >= source.length) return 0 // infix function called using non-infix syntax

    // Continue until there is a non-whitespace character
    while (source[offset].isWhitespace()) {
      offset++
      if (offset >= source.length) return 0
    }
    return offset
  }

  return 0
}

fun String.substring(expression: IrElement) = substring(expression.startOffset, expression.endOffset)
fun IrFile.info(expression: IrElement) = fileEntry.getSourceRangeInfo(expression.startOffset, expression.endOffset)

fun StringBuilder.indent(indentation: Int): StringBuilder {
  repeat(indentation) { append(" ") }
  return this
}
