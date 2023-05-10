package com.ibaevzz.hiddenshooting

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class Record : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for(i in appWidgetIds){
            update(context, appWidgetManager, i)
        }
    }

    private fun update(context: Context, manager: AppWidgetManager, id: Int){
        val rView = RemoteViews(context.packageName, R.layout.record)

        val intentFront = Intent(context, RecordService::class.java)
        intentFront.putExtra("camera", 3)
        val pIntentFront = PendingIntent.getForegroundService(context, 0, intentFront, PendingIntent.FLAG_IMMUTABLE)
        rView.setOnClickPendingIntent(R.id.front, pIntentFront)

        val intentBack = Intent(context, RecordService::class.java)
        intentBack.putExtra("camera", 2)
        val pIntentBack = PendingIntent.getForegroundService(context, 0, intentBack, PendingIntent.FLAG_IMMUTABLE)
        rView.setOnClickPendingIntent(R.id.back, pIntentBack)

        manager.updateAppWidget(id, rView)
    }
}