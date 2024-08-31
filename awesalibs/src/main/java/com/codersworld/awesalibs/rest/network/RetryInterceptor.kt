package com.codersworld.awesalibs.rest.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class RetryInterceptor(private val maxAttempts: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        var response: Response
        response = chain.proceed(request)
        var retryCount = 0

        while (response.code != 200 && retryCount <= maxAttempts) {
            try {
                response = chain.proceed(request)
                if (response.code == 200) {
                    return response

                } else {
                    request = request.newBuilder().build()
                    response.close()
                    retryCount++
                }
            } catch (e: Exception) {
            }
        }
        return response
    }
}
