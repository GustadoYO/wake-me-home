package com.gusta.wakemehome.utilities;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.material.snackbar.Snackbar;
import com.gusta.wakemehome.MainActivity;
import com.gusta.wakemehome.R;

public final class NotificationUtils {

    //================//
    // PUBLIC METHODS //
    //================//

    /**
     * Show a text message to the user.
     *
     * @param context The context asking to show the message.
     * @param text    The message text.
     */
    public static void notifyUser(Context context, String text) {
        notifyUser(context, text, null, null, null, null);
    }

    /**
     * Show a text message to the user.
     *
     * @param activity           The activity to show the message on.
     * @param mainTextStringId   The id for the string resource for the message's main text.
     * @param actionTextStringId The id for the string resource for the text of the action item.
     * @param listener           The listener associated with the Snackbar action.
     */
    public static void notifyUser(Activity activity, int mainTextStringId, int actionTextStringId,
                                  View.OnClickListener listener) {
        notifyUser(activity, activity.getString(mainTextStringId), null,
                activity.getString(actionTextStringId), listener, null);
    }

    /**
     * Show a text message to the user.
     *
     * @param service       The service asking to show the message.
     * @param mainText      The message's main text.
     * @param secondaryText The message's secondary text.
     * @param intent        Intent to be called when message is pressed.
     */
    public static void notifyUser(Service service, String mainText, String secondaryText,
                                  Intent intent) {
        notifyUser(service, mainText, secondaryText, null, null, intent);
    }

    /**
     * Show a text message to the user.
     *
     * @param context               The context asking to show the message.
     * @param mainTextStringId      The id for the string resource for the message's main text.
     * @param secondaryTextStringId The id for the string resource for the message's secondary text.
     * @param actionTextStringId    The id for the string resource for the text of the action item.
     * @param intent                Intent to be called when message is pressed.
     */
    public static void notifyUser(final Context context, int mainTextStringId,
                                  int secondaryTextStringId, int actionTextStringId,
                                  final Intent intent) {
        notifyUser(context, context.getString(mainTextStringId),
                context.getString(secondaryTextStringId), context.getString(actionTextStringId),
                null, intent);
    }

    /**
     * Show a text message to the user.
     *
     * @param context       The context asking to show the message.
     * @param mainText      The message's main text.
     * @param secondaryText The message's secondary text.
     * @param actionText    The text of the action item.
     * @param intent        Intent to be called when message is pressed.
     */
    public static void notifyUser(final Context context, String mainText, String secondaryText,
                                  String actionText, final Intent intent) {
        notifyUser(context, mainText, secondaryText, actionText, null, intent);
    }

    //=================//
    // PRIVATE METHODS //
    //=================//

    /**
     * Show a text message to the user.
     *
     * @param context       The context asking to show the message.
     * @param mainText      The message's main text.
     * @param secondaryText The message's secondary text.
     * @param actionText    The text of the action item.
     * @param listener      The listener associated with the Snackbar action.
     * @param intent        Intent to be called when message is pressed.
     */
    private static void notifyUser(final Context context, String mainText, String secondaryText,
                                   String actionText, View.OnClickListener listener,
                                   final Intent intent) {

        // If the context is an activity, the message can be shown on the screen (with a snackbar)
        if (context instanceof Activity) {
            if (listener == null && intent != null) listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(intent);
                }
            };
            showSnackbar((Activity) context, mainText, actionText, listener);
        }
        // If the context is not an activity, we have to use a notification so notify the user
        else sendNotification(context, mainText, secondaryText, intent);
    }

    /**
     * Constructs and displays a notification.
     * @param context       Context used to use various Utility methods
     * @param contentTitle  Notification's title
     * @param contentText   Notification's text
     * @param intent        Intent to be called when notification is pressed
     */
    private static void sendNotification(Context context, String contentTitle, String contentText,
                                         Intent intent) {
        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(Constants.CHANNEL_ID, name,
                            NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(intent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, Constants.CHANNEL_ID);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(notificationPendingIntent);

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(Constants.CHANNEL_ID); // Channel ID
        }

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Shows a {@link Snackbar}.
     * @param activity      The activity used to show the Snackbar.
     * @param mainText      The id for the string resource for the Snackbar text.
     * @param actionText    The text of the action item.
     * @param listener      The listener associated with the Snackbar action.
     */
    private static void showSnackbar(final Activity activity, String mainText, String actionText,
                                     final View.OnClickListener listener) {
        // Show the message to the user
        View container = activity.findViewById(android.R.id.content);
        if (container != null) {
            Snackbar snackbar = Snackbar.make(container, mainText, Snackbar.LENGTH_INDEFINITE);
            if (actionText != null && listener != null)
                snackbar.setAction(actionText, listener);
            snackbar.show();
        }
    }

}
