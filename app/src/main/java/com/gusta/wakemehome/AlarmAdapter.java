package com.gusta.wakemehome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.utilities.WakeMeHomeUnitsUtils;

import java.util.List;

/**
 * {@link AlarmAdapter} exposes a list of alarms to a
 * {@link RecyclerView}
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    // Class variables for the List that holds alarm data and the Context
    private List<AlarmEntry> mAlarmEntries;
    private Context mContext;

    /**
     * Constructor for the AlarmAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    AlarmAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent   The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (which ours doesn't) you
     *                 can use this viewType integer to provide a different layout. See
     *                 {@link RecyclerView.Adapter#getItemViewType(int)}
     *                 for more details.
     * @return A new AlarmAdapterViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the alarm_list_item to a view
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.alarm_list_item, parent, false);

        return new AlarmViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the alarm
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the
     *                 contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        // Determine the values of the wanted data
        AlarmEntry taskEntry = mAlarmEntries.get(position);
        String location = taskEntry.getLocation();
        String radius =
                WakeMeHomeUnitsUtils.formatLength(mContext, taskEntry.getRadius()) + " radius";
        boolean enabled = taskEntry.isEnabled();
        String message = taskEntry.getMessage();

        //Set values
        holder.locationView.setText(location);
        holder.enabledView.setText(radius);
        holder.enabledView.setChecked(enabled);
        holder.messageView.setText(message);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our alarm list
     */
    @Override
    public int getItemCount() {
        if (null == mAlarmEntries) return 0;
        return mAlarmEntries.size();
    }

    List<AlarmEntry> getAlarms() {
        return mAlarmEntries;
    }

    /**
     * This method is used to set the alarm on an AlarmAdapter if we've already
     * created one. This is handy when we get new data but don't want to create a
     * new AlarmAdapter to display it.
     *
     * @param alarmEntries The new alarm data to be displayed.
     */
    void setAlarms(List<AlarmEntry> alarmEntries) {
        mAlarmEntries = alarmEntries;
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    /**
     * Inner class for creating ViewHolders
     */
    public class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables
        TextView locationView;
        Switch enabledView;
        TextView messageView;

        /**
         * Constructor for the AlarmViewHolder.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        AlarmViewHolder(View itemView) {
            super(itemView);

            locationView = itemView.findViewById(R.id.location);
            enabledView = itemView.findViewById(R.id.enabled);
            messageView = itemView.findViewById(R.id.message);

            itemView.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param view The View that was clicked
         */
        @Override
        public void onClick(View view) {
            int elementId = mAlarmEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
