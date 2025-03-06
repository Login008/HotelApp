package com.example.hotel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class RoomSpinnerAdapter(
    context: Context,
    private var roomNumbers: List<String>
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, roomNumbers) {

    init {
        // Устанавливаем стиль для выпадающего списка
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    // Метод для обновления данных в адаптере
    fun updateData(newRoomNumbers: List<String>) {
        roomNumbers = newRoomNumbers
        notifyDataSetChanged() // Уведомляем адаптер об изменении данных
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Используем стандартный layout для отображения текста в спиннере
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = roomNumbers[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = roomNumbers[position]
        return view
    }
}
