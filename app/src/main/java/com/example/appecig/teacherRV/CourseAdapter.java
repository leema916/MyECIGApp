package com.example.appecig.teacherRV;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appecig.CourseDetailsActivity;
import com.example.appecig.R;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courseList;
    private Context context;

    public CourseAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_item_teacher, parent, false);
        return new CourseViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course currentCourse = courseList.get(position);
        holder.courseNameTextView.setText("Course Name: " + currentCourse.getName());
        holder.courseNotesTextView.setText("Course Notes: " + currentCourse.getNotes());

        holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, CourseDetailsActivity.class);
                intent.putExtra("course", currentCourse);
                context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView courseNameTextView;
        TextView courseNotesTextView;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameTextView = itemView.findViewById(R.id.course_name);
            courseNotesTextView = itemView.findViewById(R.id.course_notes);
        }
    }
}