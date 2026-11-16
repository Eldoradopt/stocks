package com.example.stocks.di

import androidx.room.Room
import com.example.stocks.data.local.db.AppDatabase
import com.example.stocks.data.remote.coflnet.CoflnetApiService
import com.example.stocks.data.remote.hypixel.HypixelApiService
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

    // Hypixel API (no auth required for Bazaar)
    single<HypixelApiService> {
        Retrofit.Builder()
            .baseUrl("https://api.hypixel.net/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(HypixelApiService::class.java)
    }

    // Coflnet API (historical data)
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
        ).build()
    }

    single { get<AppDatabase>().holdingDao() }
    single { get<AppDatabase>().watchlistDao() }
}

val repositoryModule = module {
    single { BazaarRepository(get(), get()) }
    single { PortfolioRepository(get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { PortfolioViewModel(get()) }
    viewModel { WatchlistViewModel(get()) }
    viewModel { (tag: String) -> ItemDetailViewModel(tag, get(), get()) }
    viewModel { SearchViewModel(get()) }
}

val appModules = listOf(networkModule, databaseModule, repositoryModule, viewModelModule)
