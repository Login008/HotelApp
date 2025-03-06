package com.example.hotel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DatabaseClient {
    private var instance: HotelDatabase? = null

    fun getInstance(context: Context): HotelDatabase {
        if (instance == null) {
            synchronized(HotelDatabase::class.java) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        HotelDatabase::class.java,
                        "hotel_database"
                    )
                        .build()
                }
            }
        }
        return instance!!
    }
}

@Database(entities = [User::class, Guest::class, Number::class], version = 1)
abstract class HotelDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun guestDao(): GuestDao
    abstract fun numberDao(): NumberDao
}


@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val role: String // Администратор, Горничная, Завхоз
)

@Entity(tableName = "guests")
data class Guest(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val roomNumber: String? = null // Добавляем поле для номера отеля
)

@Entity(tableName = "numbers")
data class Number(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val isBooked: Boolean,
    var needsCleaning: Boolean,
    var needsRepair: Boolean,
    val guestId: Long? // Ссылка на гостя, если номер занят
)

@Dao
interface UserDao {
    @Query("SELECT email FROM users WHERE role = 'Housemaid'")
    fun getCleaningStaffEmails(): List<String>

    @Query("SELECT email FROM users WHERE role = 'Caretaker'")  // Завхозы
    fun getManagersEmails(): List<String>

    @Insert
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)

    @Query("SELECT * FROM users WHERE (username = :name OR email = :name) AND password = :password LIMIT 1")
    fun getUserByUsernameOrEmailAndPassword(name: String, password: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): List<User?>

    @Query("SELECT * FROM users WHERE username = :username OR email = :email LIMIT 1")
    fun getUserByUsernameOrEmail(username: String, email: String): User?
}

@Dao
interface GuestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGuest(guest: Guest): Long // Возвращаем ID вставленного гостя

    @Delete
    fun deleteGuest(guest: Guest)

    @Query("SELECT * FROM guests")
    fun getAllGuests(): LiveData<List<Guest>>

    @Query("DELETE FROM guests WHERE id = :guestId")
    fun deleteGuestById(guestId: Long)
}

@Dao
interface NumberDao {
    @Query("SELECT * FROM numbers WHERE needsCleaning = 1")
    fun getNumbersNeedingCleaningList(): List<Number>

    @Query("SELECT * FROM numbers WHERE needsRepair = 1")
    fun getNumbersNeedingRepair(): List<Number>

    @Update
    fun updateNumber(number: Number)

    @Query("SELECT * FROM numbers")
    fun getAllNumbers(): LiveData<List<Number>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNumber(number: Number)
    @Query("SELECT * FROM numbers")
    fun getNumbersList(): List<Number> // Для проверки наличия номеров

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNumbers(numbers: List<Number>) // Вставка списка номеров

    @Query("SELECT number FROM numbers WHERE guestId = :guestId LIMIT 1")
    fun getRoomNumberForGuest(guestId: Long): LiveData<String?>
}

class HotelViewModel(application: Application) : AndroidViewModel(application) {

    private val numberDao: NumberDao = DatabaseClient.getInstance(application).numberDao()
    private val guestDao: GuestDao = DatabaseClient.getInstance(application).guestDao()



    fun addGuest(guest: Guest) {
        viewModelScope.launch(Dispatchers.IO) {
            val insertedGuestId = guestDao.insertGuest(guest) // Вставляем гостя и получаем его ID
            guest.id = insertedGuestId // Устанавливаем сгенерированный ID для объекта гостя
        }
    }

    fun updateNumber(number: Number) {
        viewModelScope.launch(Dispatchers.IO) {
            numberDao.updateNumber(number)
        }
    }


    fun deleteGuest(guest: Guest) {
        viewModelScope.launch(Dispatchers.IO) {
            guestDao.deleteGuest(guest)
        }
    }

    fun getGuestRoomNumber(guestId: Long): LiveData<String?> {
        return numberDao.getRoomNumberForGuest(guestId) // Получаем номер по гостю
    }

    fun getAllGuests(): LiveData<List<Guest>> {
        return guestDao.getAllGuests()
    }
    fun getAllNumbers(): LiveData<List<Number>> {
        return numberDao.getAllNumbers()
    }

    fun bookNumber(number: Number) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNumber = number.copy(isBooked = true)
            numberDao.updateNumber(updatedNumber)
        }
    }

    fun unbookNumber(number: Number) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedNumber = number.copy(isBooked = false)
            numberDao.updateNumber(updatedNumber)
        }
    }

    fun updateNumbersInDatabase(numbers: List<Number>) {
        // Обновляем все помеченные номера как убранные в базе данных
        viewModelScope.launch(Dispatchers.IO) {
            numbers.forEach { number ->
                val updatedNumber = number.copy(needsCleaning = false)
                numberDao.updateNumber(updatedNumber)
            }
        }
    }

    fun updateRepairStatusInDatabase(numbers: List<Number>) {
        viewModelScope.launch(Dispatchers.IO) {
            numbers.forEach { number ->
                val updatedNumber = number.copy(needsRepair = false) // Снимаем метку о необходимости ремонта
                numberDao.updateNumber(updatedNumber)
            }
        }
    }

    fun deleteGuestById(guestId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            guestDao.deleteGuestById(guestId)
        }
    }

    fun autoFillRooms() {
        viewModelScope.launch {
            val existingNumbers = withContext(Dispatchers.IO) { numberDao.getNumbersList() } // Запрос в фоновом потоке
            if (existingNumbers.isEmpty()) {
                val defaultNumbers = (101..120).map { roomNumber ->
                    Number(
                        number = roomNumber.toString(),
                        isBooked = false,
                        needsCleaning = false,
                        needsRepair = false,
                        guestId = null
                    )
                }
                withContext(Dispatchers.IO) { numberDao.insertNumbers(defaultNumbers) }
                Log.d("HotelViewModel", "Добавлены номера: ${defaultNumbers.size}")
            } else {
                Log.d("HotelViewModel", "Номера уже есть в БД: ${existingNumbers.size}")
            }
        }
    }

    fun notifyCleaningStaff() {
        viewModelScope.launch(Dispatchers.IO) {
            val userDao = DatabaseClient.getInstance(getApplication()).userDao()
            val numberDao = DatabaseClient.getInstance(getApplication()).numberDao()

            // Получаем список адресов уборщиков и номеров, нуждающихся в уборке
            val cleaningStaffEmails = userDao.getCleaningStaffEmails()
            val numbersNeedingCleaning = numberDao.getNumbersNeedingCleaningList() // предполагаем, что это List, а не LiveData

            if (cleaningStaffEmails.isNotEmpty()) {
                if (numbersNeedingCleaning.isNotEmpty()) {
                    val subject = "Необходима уборка номеров"
                    val roomList = numbersNeedingCleaning.joinToString { it.number }
                    val body = "Пожалуйста, проведите уборку в следующих номерах: $roomList"

                    for (email in cleaningStaffEmails) {
                        MailSender.sendEmail(email, subject, body)
                    }
                } else {
                    Log.e("Нет номеров", "Нет номеров")
                }
            } else {
                Log.e("Нет адресов", "Нет адресов")
            }
        }
    }

    private val userDao: UserDao = DatabaseClient.getInstance(application).userDao()

    fun notifyRepairStaff() {
        viewModelScope.launch(Dispatchers.IO) {
            val managersEmails = userDao.getManagersEmails()  // Получаем email завхозов
            val numbersNeedingRepair = numberDao.getNumbersNeedingRepair()  // Получаем список номеров, требующих ремонта

            if (managersEmails.isNotEmpty()) {
                if (numbersNeedingRepair.isNotEmpty()) {
                    val subject = "Необходим ремонт номеров"
                    val roomList = numbersNeedingRepair.joinToString { it.number }
                    val body = "Пожалуйста, обратите внимание на следующие номера, которые требуют ремонта: $roomList"

                    for (email in managersEmails) {
                        MailSender.sendEmail(email, subject, body)
                    }
                } else {
                    Log.e("Нет номеров", "Нет номеров для ремонта")
                }
            } else {
                Log.e("Нет адресов", "Нет адресов завхозов")
            }
        }
    }
}


