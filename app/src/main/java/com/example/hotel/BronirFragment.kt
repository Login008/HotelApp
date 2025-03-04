package com.example.hotel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BronirFragment : Fragment() {

    private val hotelViewModel: HotelViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NumberAdapter
    private lateinit var sendHousebt: Button
    private lateinit var sendRepairbt: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bronir, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hotelViewModel.autoFillRooms() // Автозаполняем номера
        hotelViewModel.getAllNumbers().observe(viewLifecycleOwner, Observer { numbers ->
            numbers?.let {
                adapter.updateNumbers(it)
            }
        })

        sendHousebt = view.findViewById(R.id.notifyAllCleaningButton)
        recyclerView = view.findViewById(R.id.roomList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NumberAdapter(
            numbers = emptyList(),
            onBookClick = { number -> bookNumber(number) },
            onUnbookClick = { number -> unbookNumber(number) },
            onCleaningRepairChanged = { number, needsCleaning, needsRepair ->
                updateCleaningRepairStatus(number, needsCleaning, needsRepair)
            }
        )

        sendHousebt.setOnClickListener {
            hotelViewModel.notifyCleaningStaff()

            // Делаем кнопку неактивной и меняем текст
            sendHousebt.isEnabled = false
            sendHousebt.text = "Уведомление отправлено"

            // Через 5 секунд снова активируем кнопку
            Handler(Looper.getMainLooper()).postDelayed({
                sendHousebt.isEnabled = true
                sendHousebt.text = "Уведомить горничных"
            }, 5000)
        }

        sendRepairbt = view.findViewById(R.id.notifyAllRepairButton)  // Кнопка для уведомлений завхозам

        sendRepairbt.setOnClickListener {
            hotelViewModel.notifyRepairStaff()

            // Делаем кнопку неактивной и меняем текст
            sendRepairbt.isEnabled = false
            sendRepairbt.text = "Уведомление отправлено"

            // Через 5 секунд снова активируем кнопку
            Handler(Looper.getMainLooper()).postDelayed({
                sendRepairbt.isEnabled = true
                sendRepairbt.text = "Уведомить завхозов"
            }, 5000)
        }

        recyclerView.adapter = adapter

        hotelViewModel.getAllNumbers().observe(viewLifecycleOwner, Observer { numbers ->
            numbers?.let {
                adapter.updateNumbers(it)
            }
        })
    }

    private fun bookNumber(number: Number) {
        hotelViewModel.bookNumber(number)
        Toast.makeText(requireContext(), "Номер ${number.number} забронирован", Toast.LENGTH_SHORT).show()
    }

    private fun unbookNumber(number: Number) {
        hotelViewModel.unbookNumber(number)
        Toast.makeText(requireContext(), "Бронь с номера ${number.number} снята", Toast.LENGTH_SHORT).show()
    }

    private fun updateCleaningRepairStatus(number: Number, needsCleaning: Boolean, needsRepair: Boolean) {
        val updatedNumber = number.copy(needsCleaning = needsCleaning, needsRepair = needsRepair)
        hotelViewModel.updateNumber(updatedNumber)
    }
}



