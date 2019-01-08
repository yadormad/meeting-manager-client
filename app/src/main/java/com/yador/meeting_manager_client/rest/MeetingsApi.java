package com.yador.meeting_manager_client.rest;

import com.yador.meeting_manager_client.model.FacilitatedMeeting;
import com.yador.meeting_manager_client.model.Meeting;
import com.yador.meeting_manager_client.model.MeetingPriority;
import com.yador.meeting_manager_client.model.Person;
import com.yador.meeting_manager_client.model.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MeetingsApi {
    @GET("meetings/all")
    Call<List<FacilitatedMeeting>> getAllMeetings();

    @GET("meetings/detailed/{id}")
    Call<Meeting> getMeetingsById(@Path("id") int id);

    @GET("users/positions")
    Call<List<Position>> getAllPositions();

    @POST("users/person/create")
    Call<Integer> createPerson(@Body Person person);

    @PUT("meetings/{id}")
    Call<Void> editMeeting(@Body Meeting meeting, @Path("id") int id);

    @POST("meetings/")
    Call<Void> addMeeting(@Body Meeting meeting);

    @GET("meetings/priorities")
    Call<List<MeetingPriority>> getAllPriorities();

    @DELETE("meetings/{id}")
    Call<Void> deleteMeetingById(@Path("id") int id);
}
