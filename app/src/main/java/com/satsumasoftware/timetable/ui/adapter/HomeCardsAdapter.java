package com.satsumasoftware.timetable.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.ui.card.HomeCard;

import java.util.ArrayList;

public class HomeCardsAdapter extends RecyclerView.Adapter<HomeCardsAdapter.HomeCardViewHolder> {

    private Context mContext;
    private ArrayList<HomeCard> mCards;

    public HomeCardsAdapter(Context context, ArrayList<HomeCard> cards) {
        mContext = context;
        mCards = cards;
    }

    @Override
    public HomeCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_home, parent, false);
        return new HomeCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeCardViewHolder holder, int position) {
        HomeCard card = mCards.get(position);

        holder.mTitle.setText(card.getTitle());
        holder.mTitle.setBackgroundColor(ContextCompat.getColor(mContext, card.getColorRes()));

        card.loadContent(holder.mContainer);

        if (card.hasForwardAction()) {
            holder.mForwardText.setText(card.getForwardActionText());
            holder.mBottomBar.setOnClickListener(card.getBottomBarClickListener());
        } else {
            holder.mBottomBar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public class HomeCardViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle, mForwardText;
        FrameLayout mContainer;
        LinearLayout mBottomBar;

        HomeCardViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mContainer = (FrameLayout) itemView.findViewById(R.id.container);
            mBottomBar = (LinearLayout) itemView.findViewById(R.id.bottom_bar);
            mForwardText = (TextView) itemView.findViewById(R.id.forward);
        }
    }
}
