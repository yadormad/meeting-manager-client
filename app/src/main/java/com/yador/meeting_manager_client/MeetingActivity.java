package com.yador.meeting_manager_client;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.yador.meeting_manager_client.model.FacilitatedMeeting;
import com.yador.meeting_manager_client.model.Meeting;
import com.yador.meeting_manager_client.rest.MeetingsApi;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yador.meeting_manager_client.Dependency.Dependencies.MEETINGAPI;
import static com.yador.meeting_manager_client.Dependency.Dependencies.AUTHMODEL;
import static com.yador.meeting_manager_client.Dependency.Dependencies.MODEL;

public class MeetingActivity extends AppCompatActivity {

    private Dependency dependency;
    private MeetingsAdapter meetingsAdapter;
    private MeetingsApi meetingsApi;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Meeting> allDetailedMeetings;
    private SimpleDateFormat csvDateFormatter;

    public final static String BROADCAST_ACTION = "com.meetings.broadcastreceiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dependency = Dependency.getInstance();
        if(dependency.getDependency(AUTHMODEL) == null) {
            toLogin();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMeetings();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        csvDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

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

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                meetingsAdapter.refreshMeetings((List<FacilitatedMeeting>) dependency.getDependency(MODEL));
            }
        };
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        Intent pushIntent = new Intent(this, MeetingsService.class);
        this.startService(pushIntent);

        SearchView meetingSearchView = findViewById(R.id.meeting_search_view);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        meetingSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        meetingSearchView.setSubmitButtonEnabled(false);
        meetingSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                meetingsAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meetings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.exportMeetings:
                exportAllMeetings();
                break;
        }
        return true;
    }

    private void exportAllMeetings() {
        meetingsApi.getAllDetailedMeetings().enqueue(new Callback<List<Meeting>>() {
            @Override
            public void onResponse(@NonNull Call<List<Meeting>> call, @NonNull Response<List<Meeting>> response) {
                switch (response.code()) {
                    case 200:
                        allDetailedMeetings = response.body();
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            startActivityForResult(intent, 9999);
                        break;
                    case 401:
                        toLogin();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), R.string.serverError, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Meeting>> call, @NonNull Throwable t) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 9999:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    writeMeetingsIntoFile(uri);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
    }

    private void writeMeetingsIntoFile(Uri treeUri) {
        String fileName = "meetings.csv";
        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        DocumentFile pickedDir = Objects.requireNonNull(DocumentFile.fromTreeUri(this, treeUri));
        DocumentFile meetingsFile = pickedDir.findFile(fileName) != null ? pickedDir.findFile(fileName) : pickedDir.createFile("text/csv", fileName);
        try {
            assert meetingsFile != null;
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContentResolver().openOutputStream(meetingsFile.getUri()));

            CSVWriter writer = new CSVWriter(outputStreamWriter);
            String[] csvLine = {"id", "title", "description", "from", "to", "priority"};
            writer.writeNext(csvLine);

            for(Meeting meeting:allDetailedMeetings) {
                csvLine = new String[]{String.valueOf(meeting.getId()), meeting.getName(), meeting.getDescription(), csvDateFormatter.format(meeting.getStartDate()), csvDateFormatter.format(meeting.getEndDate()), meeting.getMeetingPriority().getValue()};
                writer.writeNext(csvLine);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        RecyclerView meetingsRecyclerView = findViewById(R.id.meetings_recycler_view);
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
                        dependency.putDependency(MODEL, response.body());
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
