package com.example.hotel

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.hotel.databinding.ItemGuestBinding

class GuestAdapter(
    var guests: MutableList<Guest>,
    private val onGuestSelected: (Guest) -> Unit,
    private val hotelViewModel: HotelViewModel, // Добавляем ViewModel
    private val lifecycleOwner: LifecycleOwner // Передаем LifecycleOwner
) : RecyclerView.Adapter<GuestAdapter.GuestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val binding = ItemGuestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val guest = guests[position]
        holder.bind(guest)
    }

    override fun getItemCount(): Int = guests.size

    // Метод для обновления списка гостей
    fun updateGuests(newGuests: List<Guest>) {
        guests.clear() // Очищаем старый список
        guests.addAll(newGuests) // Добавляем новые данные
        notifyDataSetChanged() // Уведомляем адаптер об изменении данных
        Log.d("GuestAdapter", "Adapter updated with ${newGuests.size} guests")
    }

    inner class GuestViewHolder(private val binding: ItemGuestBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(guest: Guest) {
            binding.guestName.text = guest.name
            binding.guestPhone.text = guest.phone
            binding.guestEmail.text = guest.email

            // Получаем номер для гостя
            hotelViewModel.getGuestRoomNumber(guest.id).observe(lifecycleOwner) { roomNumber ->
                // Отображаем номер комнаты, если он есть, иначе выводим "Нет номера"
                binding.guestRoom.text = roomNumber ?: "Нет номера"
            }

            binding.root.setOnClickListener {
                onGuestSelected(guest)
            }
        }
    }
}



