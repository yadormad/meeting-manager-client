package com.yador.meeting_manager_client;

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

public class RegisterActivity extends AppCompatActivity {

    private LoginApi loginApi;

    private Dependency dependency;

    private EditText loginEditField, passwordEditText, repeatPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dependency = Dependency.getInstance();
        loginApi = (LoginApi) dependency.getDependency(LOGINAPI);

        setContentView(R.layout.activity_register);

        loginEditField = findViewById(R.id.register_login_field);
        passwordEditText = findViewById(R.id.register_pass_field);
        repeatPasswordEditText = findViewById(R.id.register_repeat_password_field);
    }

    public void register(View view) {
        if(!verifyPasswords()) return;
        Call<AuthModel> registerCall = loginApi.register(loginEditField.getText().toString(), passwordEditText.getText().toString());
        registerCall.enqueue(new Callback<AuthModel>() {
            @Override
            public void onResponse(@NonNull Call<AuthModel> call, @NonNull Response<AuthModel> response) {
                switch (response.code()) {
                    case 200:
                        dependency.putDependency(AUTHMODEL, response.body());
                        dependency.createMeetingApi();
                        onBackPressed();
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

    private boolean verifyPasswords() {
        if(passwordEditText.getText().toString().equals(repeatPasswordEditText.getText().toString())) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.passwords_not_matching), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
