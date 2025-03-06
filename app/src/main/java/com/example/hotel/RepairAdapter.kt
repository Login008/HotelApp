package com.example.hotel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RepairAdapter(
    private var numbers: MutableList<Number>,
    private val onRepairChecked: (Number, Boolean) -> Unit
) : RecyclerView.Adapter<RepairAdapter.RepairViewHolder>() {

    val repairedNumbers = mutableListOf<Number>() // Список номеров, помеченных как отремонтированные

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_care, parent, false) // Используем item_care вместо item_house
        return RepairViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
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
    fun submitRepairResults() {
        // Обновляем только те номера, которые были помечены как отремонтированные
        repairedNumbers.forEach { number ->
            number.needsRepair = false
        }
        notifyDataSetChanged() // Обновляем UI
    }

    inner class RepairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomNumber: TextView = itemView.findViewById(R.id.roomNumber)
        private val repairCheckBox: CheckBox = itemView.findViewById(R.id.repairCheckBox)  // Изменено на repairCheckBox из item_care

        fun bind(number: Number) {
            roomNumber.text = "Номер ${number.number}"

            // Отключаем слушатель перед обновлением состояния
            repairCheckBox.setOnCheckedChangeListener(null)

            // Устанавливаем состояние чекбокса в зависимости от номера
            repairCheckBox.isChecked = !number.needsRepair  // По умолчанию чекбокс не активен, если номер не требует ремонта

            // Назначаем новый слушатель
            repairCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    repairedNumbers.add(number) // Помечаем номер как отремонтированный
                } else {
                    repairedNumbers.remove(number) // Убираем из списка, если чекбокс снят
                }
                onRepairChecked(number, isChecked)
            }
        }
    }
}
