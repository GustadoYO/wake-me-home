package com.gusta.wakemehome;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link AlarmAdapter} exposes a list of weather forecasts to a
 * {@link android.support.v7.widget.RecyclerView}
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmAdapterViewHolder> {

    private String[] mAlarmsData;
    private final AlarmAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface AlarmAdapterOnClickHandler {
        void onClick(String alarmData);
    }

    /**
     * Creates a AlarmAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public AlarmAdapter(AlarmAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class AlarmAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mAlarmTextView;

        public AlarmAdapterViewHolder(View view) {
            super(view);
            mAlarmTextView = (TextView) view.findViewById(R.id.tv_alarm_data);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String alarmData = mAlarmsData[adapterPosition];
            mClickHandler.onClick(alarmData);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new AlarmAdapterViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public AlarmAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.alarm_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new AlarmAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the alarm
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param alarmAdapterViewHolder The ViewHolder which should be updated to represent the
     *                               contents of the item at the given position in the data set.
     * @param position               The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AlarmAdapterViewHolder alarmAdapterViewHolder, int position) {
        String thisAlarm = mAlarmsData[position];
        alarmAdapterViewHolder.mAlarmTextView.setText(thisAlarm);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our alarm list
     */
    @Override
    public int getItemCount() {
        if (null == mAlarmsData) return 0;
        return mAlarmsData.length;
    }

    /**
     * This method is used to set the alarm on an AlarmAdapter if we've already
     * created one. This is handy when we get new data but don't want to create a
     * new AlarmAdapter to display it.
     *
     * @param alarmsData The new alarm data to be displayed.
     */
    public void setAlarmsData(String[] alarmsData) {
        this.mAlarmsData = alarmsData;
        notifyDataSetChanged();
    }
}
