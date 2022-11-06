/*
 *   Copyright 2022 BASS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("unused")

package com.bass.easySerial.extend

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull


/**
 * Create by BASS
 * on 2022/1/11 10:13.
 */

//tryCatch的内联函数
internal suspend inline fun tryCatchSuspend(crossinline func: suspend () -> Unit) {
    try {
        func()
    } catch (e: Exception) {
        logE(e)
    }
}

/**
 * tryCatch的内联函数带有返回值
 * @param catchValue 捕获异常后的返回值
 */
internal suspend inline fun <T> tryCatchSuspend(
    catchValue: T,
    crossinline func: suspend () -> T
): T {
    return try {
        func()
    } catch (e: Exception) {
        logE(e)
        catchValue
    }
}

//tryCatch的内联函数
internal inline fun tryCatch(crossinline func: () -> Unit) {
    try {
        func()
    } catch (e: Exception) {
        logE(e)
    }
}

/**
 * tryCatch的内联函数带有返回值
 * @param catchValue 捕获异常后的返回值
 */
internal inline fun <T> tryCatch(catchValue: T, crossinline func: () -> T): T {
    return try {
        func()
    } catch (e: Exception) {
        logE(e)
        catchValue
    }
}

//操作超时设定
internal suspend inline fun <T> blockWithTimeoutOrNull(timeOut: Long, crossinline func: () -> T?) =
    withTimeoutOrNull(timeOut) {
        suspendCancellableCoroutine<T?> {
            it.resumeWith(Result.success(func()))
        }
    }
