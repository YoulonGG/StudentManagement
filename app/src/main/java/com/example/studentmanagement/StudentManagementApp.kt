package com.example.studentmanagement

import android.app.Application
import com.example.studentmanagement.data.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */


class StudentManagementApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StudentManagementApp)
            modules(appModule)
        }
    }
}