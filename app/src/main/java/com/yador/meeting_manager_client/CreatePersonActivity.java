package com.yador.meeting_manager_client;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.yador.meeting_manager_client.model.AuthModel;
import com.yador.meeting_manager_client.model.Person;
import com.yador.meeting_manager_client.model.Position;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.AUTHMODEL;
import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;

public class CreatePersonActivity extends AppCompatActivity {

    private EditText fullNameEditText;
    private Spinner positionSpinner;
    private MeetingsApi meetingsApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_person);

        fullNameEditText = findViewById(R.id.fullname_field);
        positionSpinner = findViewById(R.id.position_spinner);
        meetingsApi = (MeetingsApi) Dependency.getInstance().getDependency(MEETINGAPI);

        loadAllPositions();
    }

    private void setSpinnerAdapter(List<Position> positionList) {
        positionSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.position_item, positionList));
    }

    private void loadAllPositions() {
        Call<List<Position>> positionsCall = meetingsApi.getAllPositions();
        positionsCall.enqueue(new Callback<List<Position>>() {
            @Override
            public void onResponse(@NonNull Call<List<Position>> call, @NonNull Response<List<Position>> response) {
                switch (response.code()) {
                    case 200:
                        setSpinnerAdapter(response.body());
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), R.string.incorrectLoginCreds, Toast.LENGTH_LONG).show();
                        toLogin();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Position>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
                Log.e("fuck","networking again", t);
            }
        });
    }

    private void toLogin() {
        Intent intent = new Intent(CreatePersonActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void submitCreate(View view) {
        Person createdPerson = new Person();
        createdPerson.setFullName(fullNameEditText.getText().toString());
        createdPerson.setPosition((Position) positionSpinner.getSelectedItem());

        meetingsApi.createPerson(createdPerson).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                switch (response.code()) {
                    case 200:
                        ((AuthModel)Dependency.getInstance().getDependency(AUTHMODEL)).setUserId(response.body());
                        onBackPressed();
                        break;
                    case 401:
                        Toast.makeText(getApplicationContext(), R.string.incorrectLoginCreds, Toast.LENGTH_LONG).show();
                        toLogin();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
                Log.e("fuck","networking again", t);
            }
        });
    }
}
