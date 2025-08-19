package com.skyfatelabs.soulpaperconnections

import android.app.Application
import androidx.room.Room
import com.skyfatelabs.soulpaperconnections.data.AppDatabase
import com.skyfatelabs.soulpaperconnections.data.AppointmentsRepository
import com.skyfatelabs.soulpaperconnections.data.ChatRepository
import com.skyfatelabs.soulpaperconnections.data.TicketsRepository
import com.skyfatelabs.soulpaperconnections.notifications.AlarmCenter
import com.skyfatelabs.soulpaperconnections.notifications.Notifications
import com.skyfatelabs.soulpaperconnections.settings.SettingsRepository
//import com.stripe.android.PaymentConfiguration

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1) Init DI graph (Room + repositories)
        Graph.provide(this)

        // 2) Init Stripe (needed for PaymentSheet UI)
        // Add STRIPE_PUBLISHABLE_KEY to local.properties and expose via build.gradle as BuildConfig field.
       //PaymentConfiguration.init(
        //    applicationContext,
        //    BuildConfig.STRIPE_PUBLISHABLE_KEY
        //)

        // 3) Prepare notifications/alarms
        Notifications.ensureChannel(this)
        AlarmCenter.init(this)
    }
}

object Graph {
    lateinit var db: AppDatabase
        private set
    lateinit var chatRepo: ChatRepository
        private set
    lateinit var apptRepo: AppointmentsRepository
        private set
    lateinit var settingsRepo: SettingsRepository
        private set
    lateinit var ticketsRepo: TicketsRepository
        private set

    fun provide(app: Application) {
        db = Room.databaseBuilder(app, AppDatabase::class.java, "soulpaper.db")
            // Dev-friendly: wipes DB when schema version changes.
            // Replace with .addMigrations(...) for production.
            .fallbackToDestructiveMigration()
            .build()

        chatRepo = ChatRepository(db.chatDao())
        apptRepo = AppointmentsRepository(db.appointmentDao())
        settingsRepo = SettingsRepository(app)
        ticketsRepo = TicketsRepository(db.ticketDao())
    }
}
