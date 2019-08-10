package com.aperez.owltabs.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aperez.owltabs.R
import com.aperez.owltabs.model.Fixture
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageViewModel : ViewModel() {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val fixtures: MutableLiveData<FixturesListView> = MutableLiveData()

    sealed class FixturesListView {
        object Loading : FixturesListView()
        data class Success(val fixtures: List<Fixture>) : FixturesListView()
        data class Error(val message: String) : FixturesListView()
    }

    fun loadData(context: Context, title: String?) {
        viewModelScope.launch {
            fixtures.value = FixturesListView.Loading
            val response = getFixtures(context, title)
            if (response.isNotEmpty()) {
                fixtures.value = FixturesListView.Success(response)
            } else {
                fixtures.value = FixturesListView.Error("No Fixtures available")
            }
        }
    }

    private suspend fun getFixtures(context: Context, title: String?) = withContext(Dispatchers.Default) {
        val type = Types.newParameterizedType(List::class.java, Fixture::class.java)
        val adapter = moshi.adapter<List<Fixture>>(type)

        val text = if (title == context.getString(R.string.fixtures)) {
            context.resources.openRawResource(R.raw.fixtures)
                .bufferedReader().use { it.readText() }
        } else {
            context.resources.openRawResource(R.raw.results)
                .bufferedReader().use { it.readText() }
        }

        return@withContext adapter.fromJson(text) as List<Fixture>
    }


    fun fixturesView(): LiveData<FixturesListView> {
        return fixtures
    }
}