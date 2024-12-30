/*
 * SPDX-FileCopyrightText: 2024 MDP43140
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.mdp43140.superfreeze
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.lang.ref.WeakReference
//import io.mdp43140.superfreeze.CommonFunctions
class NotificationService: NotificationListenerService(){
	override fun onStartCommand(
		intent: Intent?,
		flags: Int,
		startId: Int,
	): Int = Service.START_STICKY
	override fun onCreate(){
		super.onCreate()
	}
	override fun onNotificationPosted(sbn: StatusBarNotification){
		if (isNotificationMediaSession(sbn)){
			@Suppress("DEPRECATION") // '@Deprecated(...) @Nullable() fun get(p0: String!): Any?' is deprecated. Deprecated in Java.
			val token = sbn.notification.extras[Notification.EXTRA_MEDIA_SESSION] as MediaSession.Token
			synchronized(mediaPlaybackApps){
				if (MediaController(this,token).playbackState?.state != PlaybackState.STATE_PLAYING)
					mediaPlaybackApps.remove(sbn.packageName)
				else if (!mediaPlaybackApps.contains(sbn.packageName))
					mediaPlaybackApps.add(sbn.packageName)
			}
		}
		else if (isNotificationPersistent(sbn)){
			synchronized(persistNotificationApps){
				if (!persistNotificationApps.contains(sbn.packageName))
					persistNotificationApps.add(sbn.packageName)
			}
		}
	}
	override fun onNotificationRemoved(sbn: StatusBarNotification){
		if (isNotificationMediaSession(sbn)){
			synchronized(mediaPlaybackApps){
				mediaPlaybackApps.remove(sbn.packageName)
			}
		}
		else if (isNotificationPersistent(sbn)){
			synchronized(persistNotificationApps){
				persistNotificationApps.remove(sbn.packageName)
			}
		}
	}
	fun isNotificationMediaSession(sbn: StatusBarNotification): Boolean {
		@Suppress("DEPRECATION") // '@Deprecated(...) @Nullable() fun get(p0: String!): Any?' is deprecated. Deprecated in Java.
		return sbn.notification.category in listOf(Notification.CATEGORY_TRANSPORT,Notification.CATEGORY_SERVICE) &&
					 sbn.notification.extras[Notification.EXTRA_MEDIA_SESSION] is MediaSession.Token
	}
	fun isNotificationPersistent(sbn: StatusBarNotification): Boolean {
		@Suppress("DEPRECATION") // '@Deprecated(...) @Nullable() fun get(p0: String!): Any?' is deprecated. Deprecated in Java.
		return CommonFunctions.isFlagSet(sbn.notification.flags,Notification.FLAG_ONGOING_EVENT) ||
					 CommonFunctions.isFlagSet(sbn.notification.flags,Notification.FLAG_NO_CLEAR)
	}
	override fun onListenerConnected() {
		super.onListenerConnected()
		instance = WeakReference(this)
	}
	override fun onListenerDisconnected() {
		super.onListenerDisconnected()
		instance = null
	}
	companion object {
		private var instance: WeakReference<NotificationService>? = null
		internal fun getInstance(): NotificationService? = instance?.get()
		val mediaPlaybackApps: MutableSet<String> = HashSet()
		val persistNotificationApps: MutableSet<String> = HashSet()
	}
}
