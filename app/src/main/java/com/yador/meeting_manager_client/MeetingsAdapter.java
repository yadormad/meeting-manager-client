package com.yador.meeting_manager_client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.yador.meeting_manager_client.model.FacilitatedMeeting;

import java.util.ArrayList;
import java.util.List;

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.MeetingViewHolder> implements Filterable {

    private List<FacilitatedMeeting> filteredMeetings = new ArrayList<>();
    private List<FacilitatedMeeting> meetings = new ArrayList<>();
    private Context mContext;

    public MeetingsAdapter(Context mContext) {
        this.mContext = mContext;
    }

    class MeetingViewHolder extends RecyclerView.ViewHolder {

        private TextView meetingTitle, meetingDate, meetingPriority;
        ConstraintLayout mLayout;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            meetingTitle = itemView.findViewById(R.id.meeting_title_text);
            meetingDate = itemView.findViewById(R.id.meeting_date_text);
            meetingPriority = itemView.findViewById(R.id.meeting_priority_text);
            mLayout = itemView.findViewById(R.id.meeting_item_layout);
        }

        public void bind(FacilitatedMeeting facilitatedMeeting) {
            meetingTitle.setText(facilitatedMeeting.getName());
            meetingDate.setText(facilitatedMeeting.getStartDate().toString());
            meetingPriority.setText(facilitatedMeeting.getMeetingPriority());
        }
    }

    public void refreshMeetings(List<FacilitatedMeeting> freshMeetings) {
        if(!meetings.equals(freshMeetings)) {
            meetings = freshMeetings;
            filteredMeetings = freshMeetings;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.meeting_item, viewGroup, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MeetingViewHolder meetingViewHolder, int i) {
        meetingViewHolder.bind(filteredMeetings.get(i));
        meetingViewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MeetingDetailsActivity.class);
                intent.putExtra("meeting_id", filteredMeetings.get(meetingViewHolder.getAdapterPosition()).getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredMeetings.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredMeetings = meetings;
                } else {
                    List<FacilitatedMeeting> filteredList = new ArrayList<>();
                    for (FacilitatedMeeting row : meetings) {
                        if (row.getDescription().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    filteredMeetings = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredMeetings;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredMeetings = (ArrayList<FacilitatedMeeting>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
