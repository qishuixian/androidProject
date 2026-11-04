package com.example.myapplication.day24

import com.google.gson.JsonParseException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal data class RequestProblem(val message: String, val retryable: Boolean)

internal fun classifyRequestError(error: Exception): RequestProblem = when (error) {
    is CancellationException -> throw error
    is HttpException -> when (error.code()) {
        401, 403 -> RequestProblem("认证或权限不足，请检查登录和访问权限", false)
        404 -> RequestProblem("资源不存在，请检查请求地址", false)
        429 -> RequestProblem("请求过于频繁，请稍后手动重试", true)
        in 500..599 -> RequestProblem("服务暂时异常，请稍后重试", true)
        else -> RequestProblem("HTTP ${error.code()}：请求未成功，请检查参数", false)
    }
    is SocketTimeoutException -> RequestProblem("请求超时，请稍后重试", true)
    is UnknownHostException -> RequestProblem("无法解析服务地址，请检查网络和域名", true)
    is JsonParseException -> RequestProblem("响应格式不符合预期，需要检查接口数据", false)
    is IOException -> RequestProblem("连接或数据传输失败，请检查网络后重试", true)
    else -> RequestProblem("发生未预期错误，需要检查程序日志", false)
}
