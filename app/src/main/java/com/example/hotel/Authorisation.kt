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
    private lateinit var loginOrEmail : EditText
    private lateinit var pass : EditText
    private lateinit var db: HotelDatabase
    private lateinit var pref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorisation)

        loginOrEmail = findViewById(R.id.emailOrLogin)
        pass = findViewById(R.id.password)
        db = DatabaseClient.getInstance(applicationContext)
        pref = getSharedPreferences("PREF", MODE_PRIVATE)

        if (pref.getBoolean("IsAdminLogged", false))
        {
            startActivity(Intent(this@Authorisation, MainActivityForAdm::class.java))
        }
        else if (pref.getBoolean("IsCaretakerLogged", false))
        {
            startActivity(Intent(this@Authorisation, MainActivityForCare::class.java))
        }
        else if (pref.getBoolean("IsHousemaidLogged", false))
        {
            startActivity(Intent(this@Authorisation, MainActivityForHouse::class.java))
        }
    }

    fun goToRegister(view: View) {
        val intent = Intent(this, Registration::class.java)
        startActivity(intent)
    }

    fun Login(view: View) {
        if (loginOrEmail.text.isNotEmpty() && pass.text.isNotEmpty())
        {
            loginUser(loginOrEmail.text.toString(), pass.text.toString())
        }
        else
            Toast.makeText(this@Authorisation, "Fill in the blanks", Toast.LENGTH_SHORT).show()
    }

    private fun loginUser(name: String, password: String): Boolean {
        var isAuthenticated = false
        GlobalScope.launch {
            val user = db.userDao().getUserByUsernameOrEmailAndPassword(name, password)
            isAuthenticated = user != null
            runOnUiThread {
                if (isAuthenticated) {
                    if (user!!.role == "Caretaker") {
                        pref.edit().putBoolean("IsAdminLogged", false).apply()
                        pref.edit().putBoolean("IsHousemaidLogged", false).apply()
                        pref.edit().putBoolean("IsCaretakerLogged", true).apply()
                        pref.edit().putString("Online", user.username).apply()
                        startActivity(Intent(this@Authorisation, MainActivityForCare::class.java))
                    }
                    else if (user.role == "Admin") {
                        pref.edit().putBoolean("IsAdminLogged", true).apply()
                        pref.edit().putBoolean("IsHousemaidLogged", false).apply()
                        pref.edit().putBoolean("IsCaretakerLogged", false).apply()
                        pref.edit().putString("Online", user.username).apply()
                        startActivity(Intent(this@Authorisation, MainActivityForAdm::class.java))
                    }
                    else if (user.role == "Housemaid") {
                        pref.edit().putBoolean("IsAdminLogged", false).apply()
                        pref.edit().putBoolean("IsHousemaidLogged", true).apply()
                        pref.edit().putBoolean("IsCaretakerLogged", false).apply()
                        pref.edit().putString("Online", user.username).apply()
                        startActivity(Intent(this@Authorisation, MainActivityForHouse::class.java))
                    }
                } else {
                    Toast.makeText(this@Authorisation, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return isAuthenticated
    }
}