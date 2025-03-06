package com.example.hotel

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hotel.databinding.FragmentGuestsBinding

class GuestsFragment : Fragment(R.layout.fragment_guests) {

    private lateinit var binding: FragmentGuestsBinding
    private lateinit var guestAdapter: GuestAdapter
    private lateinit var viewModel: HotelViewModel
    private var selectedGuest: Guest? = null // Выбранный гость

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGuestsBinding.bind(view)
        viewModel = ViewModelProvider(this)[HotelViewModel::class.java]

        binding.guestList.layoutManager = LinearLayoutManager(requireContext())

        // Создаём адаптер и передаем LifecycleOwner
        guestAdapter = GuestAdapter(mutableListOf(), ::onGuestSelected, viewModel, viewLifecycleOwner)
        binding.guestList.adapter = guestAdapter

        // Загружаем гостей при создании фрагмента
        loadGuests()

        // Настройка SearchView
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterGuests(newText) // Вызываем фильтрацию при каждом изменении текста
                return true
            }
        })

        // Кнопка добавления гостя
        binding.addGuestButton.setOnClickListener {
            showAddGuestDialog()
        }

        // Кнопка удаления гостя
        binding.deleteGuestButton.setOnClickListener {
            selectedGuest?.let {
                deleteGuest(it)
            } ?: Toast.makeText(requireContext(), "Выберите гостя для удаления", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGuests() {
        viewModel.getAllGuests().observe(viewLifecycleOwner) { guests ->
            Log.d("GuestsFragment", "Guests loaded from DB: ${guests.joinToString { it.name }}")
            guestAdapter.updateGuests(guests.toMutableList())
        }
    }

    private fun filterGuests(query: String?) {
        // Получаем текущий список гостей из ViewModel
        viewModel.getAllGuests().observe(viewLifecycleOwner) { allGuests ->

            // Отладочное сообщение: выводим всех гостей
            Log.d("GuestsFragment", "All guests: ${allGuests.joinToString { it.name }}")

            if (query.isNullOrEmpty()) {
                // Если запрос пустой, показываем всех гостей
                guestAdapter.updateGuests(allGuests.toMutableList())
                Log.d("GuestsFragment", "Query is empty, showing all guests")
            } else {
                // Приводим запрос к нижнему регистру и удаляем лишние пробелы
                val lowerCaseQuery = query.lowercase().trim()

                // Фильтруем гостей по имени, игнорируя регистр и пробелы
                val filteredGuests = allGuests.filter { guest ->
                    guest.name.lowercase().trim().contains(lowerCaseQuery)
                }.toMutableList()

                // Отладочное сообщение: выводим отфильтрованных гостей
                Log.d("GuestsFragment", "Filtered guests: ${filteredGuests.joinToString { it.name }}")

                // Обновляем адаптер с отфильтрованным списком
                guestAdapter.updateGuests(filteredGuests)
            }
        }
    }

    private fun onGuestSelected(guest: Guest) {
        selectedGuest = guest
        Toast.makeText(requireContext(), "Выбран гость: ${guest.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showAddGuestDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_guest_item, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.edit_name)
        val phoneInput = dialogView.findViewById<EditText>(R.id.edit_phone)
        val emailInput = dialogView.findViewById<EditText>(R.id.edit_email)
        val roomSpinner = dialogView.findViewById<Spinner>(R.id.spinner_room)

        // Загружаем свободные номера, которые не требуют уборки или ремонта
        viewModel.getAllNumbers().observe(viewLifecycleOwner) { numbers ->
            // Фильтруем номера: не забронированы, не требуют уборки и не требуют ремонта
            val availableRooms = numbers.filter { number ->
                !number.isBooked && !number.needsCleaning && !number.needsRepair
            }
            val roomNumbers = availableRooms.map { it.number } // Получаем номера как список строк

            // Если доступных номеров больше 0, обновляем адаптер
            if (roomNumbers.isNotEmpty()) {
                val adapter = roomSpinner.adapter as? RoomSpinnerAdapter
                if (adapter == null) {
                    // Если адаптер еще не был создан, создаем его
                    val newAdapter = RoomSpinnerAdapter(requireContext(), roomNumbers)
                    roomSpinner.adapter = newAdapter
                } else {
                    // Если адаптер уже существует, обновляем его данные
                    adapter.updateData(roomNumbers)
                }
            } else {
                Toast.makeText(requireContext(), "Нет свободных номеров", Toast.LENGTH_SHORT).show()
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить гостя")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = nameInput.text.toString().trim()
                val phone = phoneInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val selectedRoomNumber = roomSpinner.selectedItem?.toString() ?: ""

                // Проверка на пустые поля
                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || selectedRoomNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Проверка корректности email
                if (!isValidEmail(email)) {
                    Toast.makeText(requireContext(), "Строгий формат email: w@w.w", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Проверка корректности телефона
                if (!isValidPhone(phone)) {
                    Toast.makeText(requireContext(), "Телефон должен содержать не менее 10 цифр", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Если все проверки пройдены, добавляем гостя
                val newGuest = Guest(0, name, phone, email)

                // Добавляем нового гостя
                viewModel.addGuest(newGuest)

                // После добавления гостя, обновляем номера и перезагружаем спиннер
                viewModel.getAllNumbers().observe(viewLifecycleOwner) { numbers ->
                    // Фильтруем номера: не забронированы, не требуют уборки и не требуют ремонта
                    val availableRooms = numbers.filter { number ->
                        !number.isBooked && !number.needsCleaning && !number.needsRepair
                    }
                    val roomNumbers = availableRooms.map { it.number } // Маппируем на номера

                    if (roomNumbers.isNotEmpty()) {
                        val adapter = roomSpinner.adapter as? RoomSpinnerAdapter
                        adapter?.updateData(roomNumbers) // Обновляем данные в адаптере
                    }

                    val selectedRoom = numbers.find { it.number == selectedRoomNumber }
                    selectedRoom?.let { room ->
                        // Привязываем выбранный номер к гостю
                        val updatedRoom = room.copy(isBooked = true, guestId = newGuest.id)
                        viewModel.updateNumber(updatedRoom)

                        // Обновляем список гостей и номеров после добавления гостя и привязки номера
                        loadGuests() // Перезагружаем список гостей
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Проверка корректности email
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Проверка корректности телефона
    private fun isValidPhone(phone: String): Boolean {
        // Проверяем, что телефон содержит только цифры и имеет длину не менее 10 символов
        return phone.matches(Regex("\\d{10,}"))
    }

    private fun deleteGuest(guest: Guest) {
        // Сначала находим номер, который был занят этим гостем
        viewModel.getAllNumbers().observe(viewLifecycleOwner) { numbers ->
            val roomToRelease = numbers.find { it.guestId == guest.id }

            // Если номер найден, делаем его свободным
            roomToRelease?.let { room ->
                val updatedRoom = room.copy(isBooked = false, guestId = null)
                viewModel.updateNumber(updatedRoom) // Обновляем номер в базе данных
            }

            // Удаляем гостя
            viewModel.deleteGuest(guest) // Удаляем гостя из базы данных
            loadGuests() // Перезагружаем список гостей
        }
    }
}