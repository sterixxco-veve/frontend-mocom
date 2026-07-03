package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.repositories.AssignmentRepository
import com.example.myapplication.data.repositories.AttendanceRepository
import com.example.myapplication.data.repositories.DefaultAssignmentRepository
import com.example.myapplication.data.repositories.DefaultAttendanceRepository
import com.example.myapplication.data.repositories.ScheduleRepository
import com.example.myapplication.data.repositories.DefaultScheduleRepository
import com.example.myapplication.data.repositories.UserRepository
import com.example.myapplication.data.repositories.DefaultUserRepository // 💡 Pastikan kamu mengimpor kelas repositori user kamu
import com.example.myapplication.data.sources.local.RoomDataSource
import com.example.myapplication.data.sources.local.database.AppDatabase
import com.example.myapplication.data.sources.remote.RetrofitDataSource
import com.example.myapplication.data.sources.remote.WebService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {

    lateinit var scheduleRepository: ScheduleRepository
    lateinit var userRepository: UserRepository
    lateinit var attendanceRepository: AttendanceRepository
    lateinit var assignmentRepository: AssignmentRepository

    override fun onCreate() {
        super.onCreate()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(RetrofitClient.BASE_URL + "/")
            .build()

        val retrofitService = retrofit.create(WebService::class.java)

        scheduleRepository = DefaultScheduleRepository(
            RoomDataSource(AppDatabase.getInstance(baseContext)),
            RetrofitDataSource(retrofitService)
        )

        userRepository = DefaultUserRepository(
            RoomDataSource(AppDatabase.getInstance(baseContext)), // Jika repositori user juga butuh room lokal
            RetrofitDataSource(retrofitService)                   // Menyuplai Retrofit DataSource ke repositori user
        )

        attendanceRepository = DefaultAttendanceRepository(
            RoomDataSource(AppDatabase.getInstance(baseContext)),
            RetrofitDataSource(retrofitService)
        )

        assignmentRepository =
            DefaultAssignmentRepository(
                RoomDataSource(AppDatabase.getInstance(baseContext)),
                RetrofitDataSource(retrofitService)
            )
    }
}