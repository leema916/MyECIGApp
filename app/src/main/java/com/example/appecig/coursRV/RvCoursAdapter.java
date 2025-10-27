package com.example.appecig.coursRV;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appecig.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class  RvCoursAdapter extends RecyclerView.Adapter<RvCoursAdapter.ViewHolder> {
    Context context ;
    List<CoursData> coursDataList ;

    public RvCoursAdapter(Context context, List<CoursData> coursDataList) {
        this.context = context;
        this.coursDataList = coursDataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        MaterialCardView item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.coursTitle);
            item = itemView.findViewById(R.id.itemCard);

        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.txtTitle.setText(coursDataList.get(position).courseTitle);
        holder.item.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("courseTitle", coursDataList.get(position).courseTitle);

            Navigation.findNavController(holder.itemView)
                    .navigate(R.id.courItemFragment, bundle);
        });

    }

    @Override
    public int getItemCount() {
        return coursDataList.size();
    }


}
