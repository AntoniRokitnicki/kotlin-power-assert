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

package com.bnorm.power.delegate

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols

interface FunctionDelegate {
  val function: IrFunction

  fun buildCall(
    builder: IrBuilderWithScope,
    original: IrCall,
    arguments: List<IrExpression?>,
    message: IrExpression
  ): IrExpression

  fun IrBuilderWithScope.irCallCopy(
    overload: IrSimpleFunctionSymbol,
    original: IrCall,
    arguments: List<IrExpression?>,
    expression: IrExpression
  ): IrExpression {
    return irCall(overload, type = original.type).apply {
      dispatchReceiver = original.dispatchReceiver?.deepCopyWithSymbols(parent)
      extensionReceiver = original.extensionReceiver?.deepCopyWithSymbols(parent)
      for (i in 0 until original.typeArgumentsCount) {
        putTypeArgument(i, original.getTypeArgument(i))
      }
      for ((i, argument) in arguments.withIndex()) {
        putValueArgument(i, argument?.deepCopyWithSymbols(parent))
      }
      putValueArgument(arguments.size, expression)
    }
  }
}
