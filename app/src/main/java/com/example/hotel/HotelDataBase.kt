package com.example.hotel

import android.app.Application
import android.content.Context
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
import kotlinx.coroutines.launch

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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String
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
    fun insertGuest(guest: Guest)

    @Query("SELECT * FROM guests")
    fun getAllGuests(): LiveData<List<Guest>>
}

@Dao
interface NumberDao {
    @Query("SELECT * FROM numbers WHERE needsCleaning = 1")
    fun getNumbersNeedingCleaning(): LiveData<List<Number>>

    @Query("SELECT * FROM numbers WHERE needsRepair = 1")
    fun getNumbersNeedingRepair(): LiveData<List<Number>>

    @Update
    fun updateNumber(number: Number)
}

class HotelViewModel(application: Application) : AndroidViewModel(application) {
    private val numberDao: NumberDao = DatabaseClient.getInstance(application).numberDao()
    private val guestDao: GuestDao = DatabaseClient.getInstance(application).guestDao()

    private val _numberUpdated = MutableLiveData<Boolean>()
    val numberUpdated: LiveData<Boolean> get() = _numberUpdated

    fun markNumberCleaned(number: Number) {
        viewModelScope.launch {
            val updatedNumber = number.copy(needsCleaning = false)
            numberDao.updateNumber(updatedNumber)
            _numberUpdated.postValue(true)
        }
    }
}

