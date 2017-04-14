package co.timetableapp.ui.events;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.model.Color;
import co.timetableapp.model.Event;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private Context mContext;
    private ArrayList<String> mHeaders;
    private ArrayList<Event> mEvents;

    public EventsAdapter(Context context, ArrayList<String> headers, ArrayList<Event> events) {
        mContext = context;
        mHeaders = headers;
        mEvents = events;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View header = LayoutInflater.from(parent.getContext()).inflate(R.layout.subheader, parent, false);
                return new HeaderViewHolder(header);
            case VIEW_TYPE_ITEM:
                View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_general, parent, false);
                return new EventViewHolder(item);
            default:
                throw new IllegalArgumentException("invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                setupHeaderLayout(headerViewHolder, position);
                break;
            case VIEW_TYPE_ITEM:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                setupItemLayout(eventViewHolder, position);
                break;
        }
    }

    private void setupHeaderLayout(HeaderViewHolder holder, int position) {
        String text = mHeaders.get(position);
        holder.mText.setText(text);
    }

    private void setupItemLayout(EventViewHolder holder, int position) {
        Event event = mEvents.get(position);

        holder.mTitle.setText(event.getTitle());

        boolean isOneDay = event.getStartTime().toLocalDate()
                .equals(event.getEndTime().toLocalDate());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM uu");

        StringBuilder dateBuilder = new StringBuilder();
        dateBuilder.append(event.getStartTime().format(dateFormatter));
        if (!isOneDay) {
            dateBuilder.append(" - ")
                    .append(event.getEndTime().format(dateFormatter));
        }

        String timesText = event.getStartTime().toLocalTime().toString() + " - " +
                event.getEndTime().toLocalTime().toString();

        holder.mDates.setText(dateBuilder.toString());
        holder.mTimes.setText(timesText);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mEvents.get(position) == null ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView mText;

        HeaderViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.text);
        }
    }

    private class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitle, mDates, mTimes;

        EventViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // TODO: don't use hardcoded Color(19) - use Kotlin sealed class or constants
            itemView.findViewById(R.id.color).setBackgroundColor(
                    ContextCompat.getColor(mContext, new Color(19).getPrimaryColorResId(mContext)));

            mTitle = (TextView) itemView.findViewById(R.id.text1);
            ((TextView) itemView.findViewById(R.id.text2)).setText("");
            mDates = (TextView) itemView.findViewById(R.id.text3);
            mTimes = (TextView) itemView.findViewById(R.id.text4);
        }

        @Override
        public void onClick(View view) {
            if (mOnEntryClickListener != null) {
                mOnEntryClickListener.onEntryClick(view, getLayoutPosition());
            }
        }
    }

    private OnEntryClickListener mOnEntryClickListener;

    public interface OnEntryClickListener {
        void onEntryClick(View view, int position);
    }

    public void setOnEntryClickListener(OnEntryClickListener onEntryClickListener) {
        mOnEntryClickListener = onEntryClickListener;
    }

}
