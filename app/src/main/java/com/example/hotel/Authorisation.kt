package com.example.hotel

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Authorisation : AppCompatActivity() {
    private lateinit var loginOrEmail: EditText
    private lateinit var pass: EditText
    private lateinit var db: HotelDatabase
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorisation)

        loginOrEmail = findViewById(R.id.emailOrLogin)
        pass = findViewById(R.id.password)
        db = DatabaseClient.getInstance(applicationContext)
        pref = getSharedPreferences("PREF", MODE_PRIVATE)

        // Проверяем, авторизован ли пользователь
        checkIfUserIsLoggedIn()
    }

    private fun checkIfUserIsLoggedIn() {
        val isAdminLogged = pref.getBoolean("IsAdminLogged", false)
        val isCaretakerLogged = pref.getBoolean("IsCaretakerLogged", false)
        val isHousemaidLogged = pref.getBoolean("IsHousemaidLogged", false)

        if (isAdminLogged) {
            startActivity(Intent(this@Authorisation, MainActivityForAdm::class.java))
            finish() // Закрываем текущую активность, чтобы пользователь не мог вернуться назад
        } else if (isCaretakerLogged) {
            startActivity(Intent(this@Authorisation, MainActivityForCare::class.java))
            finish()
        } else if (isHousemaidLogged) {
            startActivity(Intent(this@Authorisation, MainActivityForHouse::class.java))
            finish()
        }
    }

    fun goToRegister(view: View) {
        val intent = Intent(this, Registration::class.java)
        startActivity(intent)
    }

    fun Login(view: View) {
        if (loginOrEmail.text.isNotEmpty() && pass.text.isNotEmpty()) {
            loginUser(loginOrEmail.text.toString(), pass.text.toString())
        } else {
            Toast.makeText(this@Authorisation, "Заполните пустые поля", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(name: String, password: String): Boolean {
        var isAuthenticated = false
        GlobalScope.launch {
            val user = db.userDao().getUserByUsernameOrEmailAndPassword(name, password)
            isAuthenticated = user != null
            runOnUiThread {
                if (isAuthenticated) {
                    when (user!!.role) {
                        "Caretaker" -> {
                            pref.edit().putBoolean("IsAdminLogged", false).apply()
                            pref.edit().putBoolean("IsHousemaidLogged", false).apply()
                            pref.edit().putBoolean("IsCaretakerLogged", true).apply()
                            startActivity(Intent(this@Authorisation, MainActivityForCare::class.java))
                        }
                        "Admin" -> {
                            pref.edit().putBoolean("IsAdminLogged", true).apply()
                            pref.edit().putBoolean("IsHousemaidLogged", false).apply()
                            pref.edit().putBoolean("IsCaretakerLogged", false).apply()
                            startActivity(Intent(this@Authorisation, MainActivityForAdm::class.java))
                        }
                        "Housemaid" -> {
                            pref.edit().putBoolean("IsAdminLogged", false).apply()
                            pref.edit().putBoolean("IsHousemaidLogged", true).apply()
                            pref.edit().putBoolean("IsCaretakerLogged", false).apply()
                            startActivity(Intent(this@Authorisation, MainActivityForHouse::class.java))
                        }
                    }
                    finish() // Закрываем текущую активность после успешной авторизации
                } else {
                    Toast.makeText(this@Authorisation, "Неправильный логин/email или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return isAuthenticated
    }
}