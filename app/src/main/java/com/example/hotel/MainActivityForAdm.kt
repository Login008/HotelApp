package com.example.hotel

import android.os.Bundle
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
                    replaceFragment(BronirFragment())
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
}