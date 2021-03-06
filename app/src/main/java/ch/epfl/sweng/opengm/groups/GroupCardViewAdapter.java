package ch.epfl.sweng.opengm.groups;

import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ch.epfl.sweng.opengm.R;
import ch.epfl.sweng.opengm.parse.PFGroup;

public class GroupCardViewAdapter extends RecyclerView.Adapter<GroupCardViewAdapter.GroupViewHolder>{


    List<PFGroup> groups;
    DisplayMetrics screenMetrics;

    GroupCardViewAdapter(List<PFGroup> groups, DisplayMetrics metrics) {
        this.groups = groups;
        this.screenMetrics = metrics;
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_card_layout, parent, false);
        GroupViewHolder gvh = new GroupViewHolder(v);
        return gvh;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        // square the card
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.cardView.getLayoutParams();
        layoutParams.width = (screenMetrics.widthPixels / 2) - 10;
        layoutParams.height = layoutParams.width;
        layoutParams.leftMargin = 5;
        holder.cardView.setLayoutParams(layoutParams);

        holder.cardView.setTag(position);
        holder.groupName.setText(groups.get(position).getName());

        holder.groupPhoto.setImageBitmap(groups.get(position).getSquarePicture());
        holder.itemView.setLongClickable(true);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView groupName;
        ImageView groupPhoto;

        GroupViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.group_card_view);
            groupName = (TextView) itemView.findViewById(R.id.group_name);
            groupPhoto = (ImageView) itemView.findViewById(R.id.group_photo);
        }

    }

}