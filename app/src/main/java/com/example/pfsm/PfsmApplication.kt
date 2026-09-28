package com.example.pfsm

import android.app.Application
import com.example.pfsm.data.AppContainer
import com.example.pfsm.data.AppDataContainer

class PFSMApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}