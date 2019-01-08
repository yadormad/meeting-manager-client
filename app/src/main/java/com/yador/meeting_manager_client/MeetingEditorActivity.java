package com.yador.meeting_manager_client;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yador.meeting_manager_client.model.Meeting;
import com.yador.meeting_manager_client.model.MeetingPriority;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;

public class MeetingEditorActivity extends AppCompatActivity {

    private EditText titleEdit, descriptionEdit;
    private TextView fromText, toText;
    private Spinner prioritySpinner;
    private MeetingsApi meetingsApi;
    private Calendar dateAndTime = Calendar.getInstance();
    private Meeting meeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);

        titleEdit = findViewById(R.id.meeting_editor_title_edit);
        descriptionEdit = findViewById(R.id.meeting_editor_description_edit);
        fromText = findViewById(R.id.meeting_editor_from_date_text);
        toText = findViewById(R.id.meeting_editor_to_date_text);
        prioritySpinner = findViewById(R.id.priority_spinner);

        meeting = new Gson().fromJson(getIntent().getStringExtra("meeting"), Meeting.class);

        if(meeting == null) {
            meeting = new Meeting();
            setFromDateTime(new Date(dateAndTime.getTimeInMillis()));
            setToDateTime(new Date(dateAndTime.getTimeInMillis()));
        } else {
            setFromDateTime(meeting.getStartDate());
            setToDateTime(meeting.getEndDate());
            titleEdit.setText(meeting.getName());
            descriptionEdit.setText(meeting.getDescription());
        }

        meetingsApi = (MeetingsApi) Dependency.getInstance().getDependency(MEETINGAPI);

        loadAllPriorities();
    }

    private void loadAllPriorities() {
        meetingsApi.getAllPriorities().enqueue(new Callback<List<MeetingPriority>>() {
            @Override
            public void onResponse(@NonNull Call<List<MeetingPriority>> call, @NonNull Response<List<MeetingPriority>> response) {
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
            public void onFailure(@NonNull Call<List<MeetingPriority>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.checkYourConnection, Toast.LENGTH_LONG).show();
                Log.e("fuck","networking again", t);
            }
        });
    }

    private void setSpinnerAdapter(List<MeetingPriority> body) {
        ArrayAdapter<MeetingPriority> priorityArrayAdapter = new ArrayAdapter<>(this, R.layout.priority_item, body);
        prioritySpinner.setAdapter(priorityArrayAdapter);
        if(meeting.getMeetingPriority() != null) {
            prioritySpinner.setSelection(priorityArrayAdapter.getPosition(meeting.getMeetingPriority()));
        }
    }

    private void toLogin() {
        Intent intent = new Intent(MeetingEditorActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    // отображаем диалоговое окно для выбора даты
    public void showFromDateDialog(View v) {
        new DatePickerDialog(MeetingEditorActivity.this, fromDateListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void showFromTimeDialog() {
        new TimePickerDialog(MeetingEditorActivity.this, fromTimeListener,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    public void showToDateDialog(View v) {
        new DatePickerDialog(MeetingEditorActivity.this, toDateListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void showToTimeDialog() {
        new TimePickerDialog(MeetingEditorActivity.this, toTimeListener,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }

    private void setFromDateTime(Date date) {
        fromText.setText(DateUtils.formatDateTime(this,
                date.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        meeting.setStartDate(date);
    }

    private void setToDateTime(Date date) {
        toText.setText(DateUtils.formatDateTime(this,
                date.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
        meeting.setEndDate(date);
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener fromTimeListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            setFromDateTime(dateAndTime.getTime());
        }
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showFromTimeDialog();
        }
    };

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener toTimeListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            setToDateTime(dateAndTime.getTime());
        }
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener toDateListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showToTimeDialog();
        }
    };

    public void onSubmitPressed(View view) {
        meeting.setDescription(descriptionEdit.getText().toString());
        meeting.setName(titleEdit.getText().toString());
        meeting.setMeetingPriority((MeetingPriority) prioritySpinner.getSelectedItem());

        Call<Void> meetingEditorCall = meeting.getId() == null ? meetingsApi.addMeeting(meeting) : meetingsApi.editMeeting(meeting, meeting.getId());
        meetingEditorCall.enqueue(new Callback<Void>() {
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
