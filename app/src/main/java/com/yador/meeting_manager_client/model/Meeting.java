package com.yador.meeting_manager_client.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

public class Meeting implements Serializable {
    private Integer id;
    private String name, description;
    private Date startDate, endDate;
    private MeetingPriority priority;
    private Set<Person> participants;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MeetingPriority getMeetingPriority() {
        return priority;
    }

    public void setMeetingPriority(MeetingPriority meetingPriority) {
        this.priority = meetingPriority;
    }

    public Set<Person> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Person> participants) {
        this.participants = participants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
