package com.example.hotel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HouseAdapter(
    private var numbers: MutableList<Number>,
    private val onCleaningChecked: (Number, Boolean) -> Unit
) : RecyclerView.Adapter<HouseAdapter.HouseViewHolder>() {

    val cleanedNumbers = mutableListOf<Number>() // Список номеров, помеченных как убранные

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house, parent, false)
        return HouseViewHolder(view)
    }

    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val number = numbers[position]
        holder.bind(number)
    }

    override fun getItemCount(): Int = numbers.size

    fun updateNumbers(newNumbers: List<Number>) {
        numbers.clear()
        numbers.addAll(newNumbers)
        notifyDataSetChanged()
    }

    // Добавим функцию для отправки результатов и обновления базы данных
    fun submitCleaningResults() {
        // Обновляем только те номера, которые были помечены как убранные
        cleanedNumbers.forEach { number ->
            number.needsCleaning = false
        }
        notifyDataSetChanged() // Обновляем UI
    }

    inner class HouseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomNumber: TextView = itemView.findViewById(R.id.roomNumber)
        private val cleaningCheckBox: CheckBox = itemView.findViewById(R.id.cleaningCheckBox)

        fun bind(number: Number) {
            roomNumber.text = "Номер ${number.number}"

            // Отключаем слушатель перед обновлением состояния
            cleaningCheckBox.setOnCheckedChangeListener(null)
            cleaningCheckBox.isChecked = !number.needsCleaning

            // Назначаем новый слушатель
            cleaningCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    cleanedNumbers.add(number) // Помечаем номер как убранный
                } else {
                    cleanedNumbers.remove(number) // Убираем из списка, если чекбокс снят
                }
                onCleaningChecked(number, isChecked)
            }
        }
    }
}