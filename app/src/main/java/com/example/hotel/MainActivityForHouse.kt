package com.example.hotel

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivityForHouse : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HouseAdapter
    private lateinit var submitAllButton: Button
    private lateinit var hotelViewModel: HotelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for_house)

        hotelViewModel = ViewModelProvider(this)[HotelViewModel::class.java]

        recyclerView = findViewById(R.id.cleaningList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HouseAdapter(mutableListOf()) { number, isCleaned ->
            // Состояние изменяется в адаптере, но не в базе данных сразу
            updateCleaningStatus(number, isCleaned)
        }

        recyclerView.adapter = adapter

        submitAllButton = findViewById(R.id.submitAllButton)
        submitAllButton.setOnClickListener {
            submitCleaningResults() // Отправляем результаты только по кнопке
        }

        hotelViewModel.getAllNumbers().observe(this) { numbers ->
            val numbersNeedingCleaning = numbers.filter { it.needsCleaning }
            adapter.updateNumbers(numbersNeedingCleaning)
        }
    }

    private fun updateCleaningStatus(number: Number, isCleaned: Boolean) {
        // Статус меняется в адаптере, но база данных не обновляется тут
        // Это делаем позже при нажатии на кнопку "Отправить результаты"
    }

    private fun submitCleaningResults() {
        // После нажатия на кнопку, обновляем статус в базе данных
        adapter.submitCleaningResults()

        // Обновляем базу данных для номеров, которые были помечены как убранные
        hotelViewModel.updateNumbersInDatabase(adapter.cleanedNumbers)

        Toast.makeText(this, "Результаты уборки отправлены!", Toast.LENGTH_SHORT).show()
    }
}
