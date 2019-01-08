package com.yador.meeting_manager_client.model;

import java.util.Date;
import java.util.Objects;

public class FacilitatedMeeting {

    private Integer id;
    private String name;
    private Date startDate;
    private String meetingPriority;
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacilitatedMeeting that = (FacilitatedMeeting) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(meetingPriority, that.meetingPriority);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, startDate, meetingPriority);
    }
}
