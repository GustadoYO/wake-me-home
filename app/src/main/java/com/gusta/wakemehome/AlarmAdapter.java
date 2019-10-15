package com.gusta.wakemehome;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.utilities.UnitsUtils;

import java.util.List;

/**
 * {@link AlarmAdapter} exposes a list of alarms to a
 * {@link RecyclerView}
 */
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    // Constant for logging
    private static final String TAG = AlarmAdapter.class.getSimpleName();

    // Member variable to handle item clicks
    final private AlarmAdapterListeners mAlarmListeners;
    // Class variables for the List that holds task data and the Context
    private List<AlarmEntry> mAlarmEntries;
    private Context mContext;

    /**
     * Constructor for the AlarmAdapter that initializes the Context.
     *
     * @param context  the current Context
     * @param listener the ItemClickListener
     */
    AlarmAdapter(Context context, AlarmAdapterListeners listener) {
        mContext = context;
        mAlarmListeners = listener;
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
        final AlarmEntry alarmEntry = mAlarmEntries.get(position);
        String location = alarmEntry.getLocation();
        String radius =
                UnitsUtils.formatLength(mContext, alarmEntry.getRadius()) + " radius";
        boolean enabled = alarmEntry.isEnabled();
        String message = alarmEntry.getMessage();

        //Set values
        holder.locationElement.setText(location);
        holder.messageElement.setText(message);
        holder.enabledElement.setText(radius);
        holder.enabledElement.setChecked(enabled);
        holder.enabledElement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                Log.d(TAG, "switch change from " + !isChecked + " -> " + isChecked);
                // Save the added/updated alarm entity

                alarmEntry.setEnabled(isChecked);
                mAlarmListeners.onAlarmEnabledChangeListener(alarmEntry);
            }
        });
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
    public interface AlarmAdapterListeners {
        void onItemClickListener(int itemId);
        void onAlarmEnabledChangeListener(AlarmEntry alarm);
    }

    /**
     * Inner class for creating ViewHolders
     */
    public class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Class variables
        TextView locationElement;
        Switch enabledElement;
        TextView messageElement;

        /**
         * Constructor for the AlarmViewHolder.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        AlarmViewHolder(View itemView) {
            super(itemView);

            locationElement = itemView.findViewById(R.id.location);
            enabledElement = itemView.findViewById(R.id.enabled);
            messageElement = itemView.findViewById(R.id.message);

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
            mAlarmListeners.onItemClickListener(elementId);
        }
    }
}
