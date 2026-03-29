package com.sevis.app.di

import com.sevis.app.data.remote.AuthApiService
import com.sevis.app.data.remote.BillingApiService
import com.sevis.app.data.remote.InventoryApiService
import com.sevis.app.data.remote.JobCardApiService
import com.sevis.app.data.remote.OrdersApiService
import com.sevis.app.data.remote.PartApiService
import com.sevis.app.data.remote.StockApiService
import com.sevis.app.data.remote.UserApiService
import com.sevis.app.data.remote.createHttpClient
import com.sevis.app.data.repository.AuthRepository
import com.sevis.app.data.repository.BillingRepository
import com.sevis.app.data.repository.InventoryRepository
import com.sevis.app.data.repository.JobCardRepository
import com.sevis.app.data.repository.OrdersRepository
import com.sevis.app.data.repository.PartRepository
import com.sevis.app.data.repository.StockRepository
import com.sevis.app.data.repository.UserRepository
import com.sevis.app.presentation.viewmodel.AuthViewModel
import com.sevis.app.presentation.viewmodel.BillingViewModel
import com.sevis.app.presentation.viewmodel.InventoryViewModel
import com.sevis.app.presentation.viewmodel.JobCardViewModel
import com.sevis.app.presentation.viewmodel.OrdersViewModel
import com.sevis.app.presentation.viewmodel.PartsViewModel
import com.sevis.app.presentation.viewmodel.StockViewModel
import com.sevis.app.presentation.viewmodel.UserViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
}

val apiModule = module {
    single { AuthApiService(get()) }
    single { UserApiService(get()) }
    single { InventoryApiService(get()) }
    single { BillingApiService(get()) }
    single { OrdersApiService(get()) }
    single { PartApiService(get()) }
    single { StockApiService(get()) }
    single { JobCardApiService(get()) }
}

val repositoryModule = module {
    single { AuthRepository(get()) }
    single { UserRepository(get()) }
    single { InventoryRepository(get()) }
    single { BillingRepository(get()) }
    single { OrdersRepository(get()) }
    single { PartRepository(get()) }
    single { StockRepository(get()) }
    single { JobCardRepository(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { UserViewModel(get()) }
    viewModel { InventoryViewModel(get()) }
    viewModel { BillingViewModel(get()) }
    viewModel { OrdersViewModel(get()) }
    viewModel { PartsViewModel(get()) }
    viewModel { StockViewModel(get()) }
    viewModel { JobCardViewModel(get()) }
}

val appModules = listOf(networkModule, apiModule, repositoryModule, viewModelModule)
