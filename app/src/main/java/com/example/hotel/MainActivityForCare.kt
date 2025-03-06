package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivityForCare : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RepairAdapter
    private lateinit var submitAllButton: Button
    private lateinit var hotelViewModel: HotelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for_care)

        hotelViewModel = ViewModelProvider(this)[HotelViewModel::class.java]

        recyclerView = findViewById(R.id.repairList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RepairAdapter(mutableListOf()) { number, isRepaired ->
            // Состояние изменяется в адаптере, но не в базе данных сразу
            updateRepairStatus(number, isRepaired)
        }

        recyclerView.adapter = adapter

        submitAllButton = findViewById(R.id.submitAllButton1)
        submitAllButton.setOnClickListener {
            submitRepairResults() // Отправляем результаты только по кнопке
        }

        hotelViewModel.getAllNumbers().observe(this) { numbers ->
            val numbersNeedingRepair = numbers.filter { it.needsRepair }
            adapter.updateNumbers(numbersNeedingRepair) // Обновляем адаптер с отфильтрованными номерами
        }
    }

    private fun updateRepairStatus(number: Number, isRepaired: Boolean) {
        // Статус меняется в адаптере, но база данных не обновляется тут
        // Это делаем позже при нажатии на кнопку "Отправить результаты"
    }

    private fun submitRepairResults() {
        // После нажатия на кнопку, обновляем статус в базе данных
        adapter.submitRepairResults()

        // Обновляем базу данных для номеров, которые были помечены как отремонтированные
        hotelViewModel.updateRepairStatusInDatabase(adapter.repairedNumbers)

        Toast.makeText(this, "Результаты ремонта отправлены!", Toast.LENGTH_SHORT).show()
    }

    fun onLogoutClicked(view: View) {
        // Очищаем SharedPreferences (или другие данные авторизации)
        val pref = getSharedPreferences("PREF", MODE_PRIVATE)
        pref.edit().clear().apply()

        // Переходим на экран авторизации
        val intent = Intent(this, Authorisation::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность
    }
}
