package com.yador.meeting_manager_client;

import com.google.gson.GsonBuilder;
import com.yador.meeting_manager_client.model.AuthModel;
import com.yador.meeting_manager_client.rest.LoginApi;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.io.IOException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Dependency {

    enum Dependencies {
        AUTHMODEL, LOGINAPI, MEETINGAPI, MODEL
    }

    public static final String BASEURL = "http://192.168.1.5:8080/";

    private static Dependency INSTANCE;
    private Map<Dependencies, Object> dependencies;

    public static Dependency getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Dependency();
        }
        return INSTANCE;
    }

    public Dependency() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setLenient()
                        .create()))
                .build();

        dependencies = new HashMap<>();
        dependencies.put(Dependencies.LOGINAPI, retrofit.create(LoginApi.class));
    }

    public Object getDependency(Dependencies d) {
        return dependencies.get(d);
    }

    public void putDependency(Dependencies key, Object value) {
        dependencies.put(key, value);
    }

    public MeetingsApi createMeetingApi() {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + ((AuthModel)Objects.requireNonNull(dependencies.get(Dependencies.AUTHMODEL))).getToken())
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(Dependency.BASEURL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setLenient()
                        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .create()))
                .build();
        return (MeetingsApi) dependencies.put(Dependencies.MEETINGAPI, retrofit.create(MeetingsApi.class));
    }
}
