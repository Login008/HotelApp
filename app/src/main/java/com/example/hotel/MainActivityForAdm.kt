package com.example.hotel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityForAdm : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for_adm)

        // Получаем BottomNavigationView из разметки
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)

        // Устанавливаем слушатель для выбора пунктов меню
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_guests -> {
                    // Заменяем фрагмент для Home
                    replaceFragment(GuestsFragment())
                    true
                }
                R.id.nav_bronirovanie -> {
                    // Заменяем фрагмент для Search
                    replaceFragment(BronirFragment())
                    true
                }
                else -> false
            }
        }

        // Устанавливаем фрагмент по умолчанию (например, HomeFragment)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_guests
        }
    }

    // Функция для замены фрагмента
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    fun onLogoutClicked(view: View) {
        // Очищаем SharedPreferences (или другие данные авторизации)
        val pref = getSharedPreferences("PREF", MODE_PRIVATE)
        pref.edit().clear().apply()

        // Переходим на экран авторизации
        val intent = Intent(this, Authorisation::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность
    }
}