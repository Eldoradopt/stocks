package com.example.stocks.di

import androidx.room.Room
import com.example.stocks.data.local.datastore.UserPreferencesRepository
import com.example.stocks.data.local.db.AppDatabase
import com.example.stocks.data.remote.coflnet.CoflnetApiService
import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.data.remote.mojang.MojangApiService
import com.example.stocks.data.repository.AccountRepository
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.ui.detail.ItemDetailViewModel
import com.example.stocks.ui.portfolio.PortfolioViewModel
import com.example.stocks.ui.search.SearchViewModel
import com.example.stocks.ui.watchlist.WatchlistViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
            .build()
    }

    // Hypixel API
    single<HypixelApiService> {
        Retrofit.Builder()
            .baseUrl("https://api.hypixel.net/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(HypixelApiService::class.java)
    }

    // Mojang API
    single<MojangApiService> {
        Retrofit.Builder()
            .baseUrl("https://api.mojang.com/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(MojangApiService::class.java)
    }

    // Coflnet API
    single<CoflnetApiService> {
        Retrofit.Builder()
            .baseUrl("https://sky.coflnet.com/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(CoflnetApiService::class.java)
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "skyblock_portfolio.db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    single { get<AppDatabase>().holdingDao() }
    single { get<AppDatabase>().watchlistDao() }
    single { get<AppDatabase>().transactionDao() }
    single { UserPreferencesRepository(androidContext()) }
}

val repositoryModule = module {
    single { BazaarRepository(get(), get()) }
    single { PortfolioRepository(get(), get(), get(), get()) }
    single { AccountRepository(get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { PortfolioViewModel(get(), get(), get()) }
    viewModel { WatchlistViewModel(get(), get()) }
    viewModel { (tag: String) -> ItemDetailViewModel(tag, get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
}

val appModules = listOf(networkModule, databaseModule, repositoryModule, viewModelModule)
