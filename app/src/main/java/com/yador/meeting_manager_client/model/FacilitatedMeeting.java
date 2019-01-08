package com.yador.meeting_manager_client.model;

import java.util.Date;

public class FacilitatedMeeting {

    private Integer id;
    private String name;
    private Date startDate;
    private String meetingPriority;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getMeetingPriority() {
        return meetingPriority;
    }

    public void setMeetingPriority(String meetingPriority) {
        this.meetingPriority = meetingPriority;
    }
}
