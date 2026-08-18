package com.tim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.tim.data.db.AppDatabase
import com.tim.data.repository.CatRepository
import com.tim.ui.CatViewModel
import com.tim.ui.HomeScreen
import com.tim.ui.theme.CatCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CatRepository(database.catDao(), database.catLogDao())
        val viewModelFactory = CatViewModel.Factory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[CatViewModel::class.java]

        setContent {
            CatCareTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
