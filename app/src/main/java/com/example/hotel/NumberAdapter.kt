package com.example.hotel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NumberAdapter(
    private var numbers: List<Number>,
    private val onBookClick: (Number) -> Unit,
    private val onUnbookClick: (Number) -> Unit,
    private val onCleaningRepairChanged: (Number, Boolean, Boolean) -> Unit
) : RecyclerView.Adapter<NumberAdapter.NumberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_number, parent, false)
        return NumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
        val number = numbers[position]
        holder.bind(number)
    }

    override fun getItemCount(): Int = numbers.size

    fun updateNumbers(newNumbers: List<Number>) {
        numbers = newNumbers
        notifyDataSetChanged()
    }

    inner class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val roomNumber: TextView = itemView.findViewById(R.id.roomNumber)
        private val bookingStatus: TextView = itemView.findViewById(R.id.bookingStatus)
        private val bookButton: Button = itemView.findViewById(R.id.bookButton)
        private val unbookButton: Button = itemView.findViewById(R.id.unbookButton)
        private val cleaningCheckBox: CheckBox = itemView.findViewById(R.id.cleaningRepairCheckBox)
        private val repairCheckBox: CheckBox = itemView.findViewById(R.id.cleaningRepairCheckBox1)

        fun bind(number: Number) {
            roomNumber.text = "Номер ${number.number}"
            bookingStatus.text = if (number.isBooked) "Статус: Занят" else "Статус: Свободен"

            // Видимость кнопок бронирования
            bookButton.visibility = if (number.isBooked) View.GONE else View.VISIBLE
            unbookButton.visibility = if (number.isBooked) View.VISIBLE else View.GONE

            // Блокировка чекбоксов для забронированных номеров
            cleaningCheckBox.isEnabled = !number.isBooked
            repairCheckBox.isEnabled = !number.isBooked

            // Установка чекбоксов
            cleaningCheckBox.isChecked = number.needsCleaning
            repairCheckBox.isChecked = number.needsRepair

            // Блокируем кнопку бронирования, если хотя бы один чекбокс отмечен
            val isAnyCheckBoxChecked = cleaningCheckBox.isChecked || repairCheckBox.isChecked
            bookButton.isEnabled = !isAnyCheckBoxChecked

            // Обработчики нажатий
            bookButton.setOnClickListener { onBookClick(number) }
            unbookButton.setOnClickListener { onUnbookClick(number) }

            cleaningCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onCleaningRepairChanged(number, isChecked, repairCheckBox.isChecked)
            }

            repairCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onCleaningRepairChanged(number, cleaningCheckBox.isChecked, isChecked)
            }
        }
    }
}


