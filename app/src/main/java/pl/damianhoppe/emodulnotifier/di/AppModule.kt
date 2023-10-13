package pl.damianhoppe.emodulnotifier.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.damianhoppe.emodulnotifier.data.db.AppDatabase
import pl.damianhoppe.emodulnotifier.data.emodul.EmodulApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun emodulApi(): EmodulApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://emodul.eu/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(EmodulApi::class.java)
    }

    @Provides
    @Singleton
    fun appDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "db"
        ).build()
    }
}