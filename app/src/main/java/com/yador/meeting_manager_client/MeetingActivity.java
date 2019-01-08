package com.yador.meeting_manager_client;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.yador.meeting_manager_client.model.FacilitatedMeeting;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;
import static com.yador.meeting_manager_client.Dependency.Dependencies.AUTHMODEL;

public class MeetingActivity extends AppCompatActivity {

    private Dependency dependency;
    private RecyclerView meetingsRecyclerView;
    private MeetingsAdapter meetingsAdapter;
    private MeetingsApi meetingsApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dependency = Dependency.getInstance();
        if(dependency.getDependency(AUTHMODEL) == null) {
            toLogin();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initRecyclerView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MeetingActivity.this, MeetingEditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        meetingsApi = (MeetingsApi) dependency.getDependency(MEETINGAPI);
        if(meetingsApi != null) {
            refreshMeetings();
        } else if(dependency.getDependency(AUTHMODEL) != null) {
            meetingsApi = dependency.createMeetingApi();
            refreshMeetings();
        }
    }

    private void initRecyclerView() {
        meetingsRecyclerView = findViewById(R.id.meetings_recycler_view);
        meetingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        meetingsAdapter = new MeetingsAdapter(MeetingActivity.this);
        meetingsRecyclerView.setAdapter(meetingsAdapter);
    }

    private void toLogin() {
        Intent intent = new Intent(MeetingActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void refreshMeetings() {
        Call<List<FacilitatedMeeting>> meetingCall = meetingsApi.getAllMeetings();
        meetingCall.enqueue(new Callback<List<FacilitatedMeeting>>() {
            @Override
            public void onResponse(@NonNull Call<List<FacilitatedMeeting>> call, @NonNull Response<List<FacilitatedMeeting>> response) {
                switch (response.code()) {
                    case 200:
                        meetingsAdapter.refreshMeetings(response.body());
                        break;
                    case 401:
                        toLogin();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<FacilitatedMeeting>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
            }
        });
    }
}
