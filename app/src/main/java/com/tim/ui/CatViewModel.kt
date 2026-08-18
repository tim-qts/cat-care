package com.tim.ui

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tim.data.model.CatDailyLogEntity
import com.tim.data.model.CatEntity
import com.tim.data.repository.CatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CatViewModel(private val repository: CatRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())

    // Currently selected date for the daily checklist
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Currently selected cat ID for trends view
    private val _selectedCatIdForTrends = MutableStateFlow<Int?>(null)
    val selectedCatIdForTrends: StateFlow<Int?> = _selectedCatIdForTrends.asStateFlow()

    // List of all cats
    val cats: StateFlow<List<CatEntity>> = repository.allCats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logs for the currently selected date
    val logsForSelectedDate: StateFlow<List<CatDailyLogEntity>> = _selectedDate
        .flatMapLatest { dateStr ->
            repository.getLogsForDate(dateStr)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Logs for the selected cat in trends view
    val logsForSelectedCat: StateFlow<List<CatDailyLogEntity>> = _selectedCatIdForTrends
        .flatMapLatest { catId ->
            if (catId != null && catId > 0) {
                repository.getLogsForCat(catId)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dialog & UI states
    val isAddCatDialogOpen = MutableStateFlow(false)
    val isCsvExportDialogOpen = MutableStateFlow(false)
    val catToDelete = MutableStateFlow<CatEntity?>(null)
    val catToEdit = MutableStateFlow<CatEntity?>(null)
    val generatedCsvText = MutableStateFlow<String?>(null)

    // User feedback message
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        // Automatically select the first cat for trends when cats load if none selected
        viewModelScope.launch {
            cats.collect { catList ->
                if (_selectedCatIdForTrends.value == null && catList.isNotEmpty()) {
                    _selectedCatIdForTrends.value = catList.first().id
                }
            }
        }
    }

    fun setSelectedDate(calendar: Calendar) {
        _selectedDate.value = dateFormat.format(calendar.time)
    }

    fun changeDateByDays(days: Int) {
        try {
            val date = dateFormat.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectCatForTrends(catId: Int) {
        _selectedCatIdForTrends.value = catId
    }

    fun toggleTask(cat: CatEntity, taskType: CatRepository.TaskType) {
        viewModelScope.launch {
            repository.toggleTask(cat.id, _selectedDate.value, taskType)
            val taskName = when (taskType) {
                CatRepository.TaskType.MORNING_MEAL -> "Morning Meal 🥣"
                CatRepository.TaskType.EVENING_MEAL -> "Evening Meal 🍲"
                CatRepository.TaskType.POOP -> "Poop Log 💩"
                CatRepository.TaskType.MEDICATION -> "Daily Medication 💊"
            }
            _snackbarEvent.emit("Updated $taskName for ${cat.name}!")
        }
    }

    fun updateNotes(catId: Int, notes: String) {
        viewModelScope.launch {
            repository.updateDailyNotes(catId, _selectedDate.value, notes)
        }
    }

    fun addCat(
        name: String,
        breed: String,
        colorHex: String,
        avatarEmoji: String,
        requiresMedication: Boolean = false,
        medicationNotes: String = ""
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newCatId = repository.addCat(
                name = name.trim(),
                breed = breed.trim(),
                colorHex = colorHex,
                avatarEmoji = avatarEmoji,
                requiresMedication = requiresMedication,
                medicationNotes = if (requiresMedication) medicationNotes.trim() else ""
            )
            isAddCatDialogOpen.value = false
            _snackbarEvent.emit("Welcome ${name.trim()} to the family! 🐾")
            if (_selectedCatIdForTrends.value == null) {
                _selectedCatIdForTrends.value = newCatId.toInt()
            }
        }
    }

    fun openEditCatDialog(cat: CatEntity) {
        catToEdit.value = cat
    }

    fun closeEditCatDialog() {
        catToEdit.value = null
    }

    fun updateCat(updatedCat: CatEntity) {
        viewModelScope.launch {
            repository.updateCat(updatedCat)
            catToEdit.value = null
            _snackbarEvent.emit("Updated details for ${updatedCat.name}! 🐾")
        }
    }

    fun reorderCats(reorderedList: List<CatEntity>) {
        viewModelScope.launch {
            repository.reorderCats(reorderedList)
        }
    }

    fun moveCatUp(index: Int) {
        val list = cats.value.toMutableList()
        if (index > 0 && index < list.size) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            reorderCats(list)
        }
    }

    fun moveCatDown(index: Int) {
        val list = cats.value.toMutableList()
        if (index >= 0 && index < list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            reorderCats(list)
        }
    }

    fun confirmDeleteCat(cat: CatEntity) {
        catToDelete.value = cat
    }

    fun deleteCatConfirmed() {
        val cat = catToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteCat(cat.id)
            catToDelete.value = null
            _snackbarEvent.emit("${cat.name} was removed.")
            if (_selectedCatIdForTrends.value == cat.id) {
                _selectedCatIdForTrends.value = cats.value.firstOrNull { it.id != cat.id }?.id
            }
        }
    }

    fun prepareCsvExport(catId: Int? = null) {
        viewModelScope.launch {
            val csvContent = repository.exportToCsv(catId)
            generatedCsvText.value = csvContent
            isCsvExportDialogOpen.value = true
        }
    }

    fun shareCsvFile(context: Context, csvContent: String) {
        try {
            val fileName = "cat_care_logs_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Cat Care Tracker CSV Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Export CSV Logs").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            viewModelScope.launch {
                _snackbarEvent.emit("Error generating CSV file: ${e.localizedMessage}")
            }
        }
    }

    class Factory(private val repository: CatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CatViewModel(repository) as T
        }
    }
}
