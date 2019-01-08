package com.yador.meeting_manager_client;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yador.meeting_manager_client.model.AuthModel;
import com.yador.meeting_manager_client.model.Meeting;
import com.yador.meeting_manager_client.model.Person;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.AUTHMODEL;
import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;

public class MeetingDetailsActivity extends AppCompatActivity {

    private TextView titleText, descriptionText, fromText, toText;
    private Meeting meeting;
    private MeetingsApi meetingsApi;
    private AuthModel authModel;
    private Button personActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_details);

        Dependency dependency = Dependency.getInstance();

        titleText = findViewById(R.id.meeting_details_title_text);
        descriptionText = findViewById(R.id.meeting_description_text);
        fromText = findViewById(R.id.from_details_text);
        toText = findViewById(R.id.to_details_text);
        personActionButton = findViewById(R.id.person_action_btn);

        authModel = (AuthModel)dependency.getDependency(AUTHMODEL);

        meetingsApi = (MeetingsApi) dependency.getDependency(MEETINGAPI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMeeting(getIntent().getIntExtra("meeting_id", 0));
    }

    private void loadMeeting(int meeting_id) {
        Call<Meeting> meetingCall = meetingsApi.getMeetingsById(meeting_id);
        meetingCall.enqueue(new Callback<Meeting>() {
            @Override
            public void onResponse(@NonNull Call<Meeting> call, @NonNull Response<Meeting> response) {
                switch (response.code()) {
                    case 200:
                        meeting = response.body();
                        fillLayout();
                        break;
                    case 401:
                        toLogin();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                        onBackPressed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Meeting> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        });
    }

    private void toLogin() {
        Intent intent = new Intent(MeetingDetailsActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void fillLayout() {
        titleText.setText(meeting.getName());
        descriptionText.setText(meeting.getDescription());
        fromText.setText(DateUtils.formatDateTime(this,
                meeting.getStartDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        toText.setText(DateUtils.formatDateTime(this,
                meeting.getEndDate().getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        if(authModel.getUserId() == null) {
            setCreatePersonButton();
        } else if(isCurrentInParticipants()) {
            setDeclineMeetingButton();
        } else {
            setAcceptMeetingButton();
        }
    }

    private void setAcceptMeetingButton() {
        personActionButton.setText(R.string.person_action_btn_accept);
        personActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo accept request
            }
        });
    }

    private void setDeclineMeetingButton() {
        personActionButton.setText(R.string.person_action_btn_decline);
        personActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo decline request
            }
        });
    }

    private void setCreatePersonButton() {
        personActionButton.setText(R.string.person_action_btn_create_person);
        personActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingDetailsActivity.this, CreatePersonActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean isCurrentInParticipants() {
        for(Person participant:meeting.getParticipants()) {
            if(participant.getId() == authModel.getUserId()) return true;
        }
        return false;
    }

    public void editMeeting(View view) {
        Intent intent = new Intent(MeetingDetailsActivity.this, MeetingEditorActivity.class);
        intent.putExtra("meeting", new Gson().toJson(meeting));
        startActivity(intent);
    }

    public void deleteMeeting(View view) {
        meetingsApi.deleteMeetingById(meeting.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                switch (response.code()) {
                    case 200:
                        Toast.makeText(getApplicationContext(), R.string.SUCCESS, Toast.LENGTH_LONG).show();
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
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
                Log.e("fuck","networking again", t);
            }
        });
    }
}
