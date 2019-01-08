package com.yador.meeting_manager_client;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yador.meeting_manager_client.model.AuthModel;
import com.yador.meeting_manager_client.rest.LoginApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.LOGINAPI;
import static com.yador.meeting_manager_client.Dependency.Dependencies.AUTHMODEL;

public class LoginActivity extends AppCompatActivity {

    private LoginApi loginApi;
    private Dependency dependency;

    private EditText loginEditField, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dependency = Dependency.getInstance();
        loginApi = (LoginApi) dependency.getDependency(LOGINAPI);

        setContentView(R.layout.activity_login);

        loginEditField = findViewById(R.id.login_field);
        passwordEditText = findViewById(R.id.pass_field);
    }

    public void login(View view) {
        Call<AuthModel> loginCall = loginApi.login(loginEditField.getText().toString(), passwordEditText.getText().toString());
        loginCall.enqueue(new Callback<AuthModel>() {
            @Override
            public void onResponse(@NonNull Call<AuthModel> call, @NonNull Response<AuthModel> response) {
                switch (response.code()) {
                    case 200:
                        dependency.putDependency(AUTHMODEL, response.body());
                        dependency.createMeetingApi();
                        onBackPressed();
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), R.string.incorrectLoginCreds, Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthModel> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
                Log.e("fuck","networking again", t);
            }
        });
    }


    public void goToRegistration(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        LoginActivity.this.finish();
    }
}
