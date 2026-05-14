package com.example.contapp.network;

import com.example.contapp.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException{
        Request.Builder requestBuilder = chain.request().newBuilder();

        String token = sessionManager.fetchAuthToken();

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Token " + token);
        }

        return chain.proceed(requestBuilder.build());
    }
}
