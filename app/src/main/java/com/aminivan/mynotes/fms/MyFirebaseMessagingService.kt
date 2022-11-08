package com.aminivan.mynotes.fms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.aminivan.mynotes.MainActivity
import com.aminivan.mynotes.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import okhttp3.internal.notify

const val channelId = "notification_channel"
const val channelName = "com.aminivan.mynotes"
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.getNotification() != null){
            generateNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }
    }

    fun getRemoteView(title: String,message: String):RemoteViews{
        val remoteView = RemoteViews("com.aminivan.mynotes",R.layout.notification)
        remoteView.setTextViewText(R.id.tvTitle,title)
        remoteView.setTextViewText(R.id.tvMessage,message)
        remoteView.setImageViewResource(R.id.iv_logo,R.drawable.pinned_notes)

        return remoteView
    }

    fun generateNotification(title: String, message: String){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.pinned_notes)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            val notificationChanel = NotificationChannel(channelId, channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChanel)
        }

        notificationManager.notify(0,builder.build())

    }
}