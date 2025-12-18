package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.taskmanager.databinding.ActivityMainBinding
import com.example.taskmanager.ui.settings.SettingsActivity

// Импорты, которые я забыл добавить
import com.example.taskmanager.TasksFragment
import com.example.taskmanager.CalendarFragment
import com.example.taskmanager.AddEditTaskFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            loadFragment(TasksFragment())
            binding.navView.selectedItemId = R.id.navigation_tasks
        }

        binding.navView.background = null
        binding.navView.menu.getItem(1).isEnabled = false

        binding.navView.setOnNavigationItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (item.itemId) {
                R.id.navigation_tasks -> {
                    if (currentFragment !is TasksFragment) loadFragment(TasksFragment())
                    true
                }
                R.id.navigation_calendar -> {
                    if (currentFragment !is CalendarFragment) loadFragment(CalendarFragment())
                    true
                }
                else -> false
            }
        }

        // ВОТ ОНА, ОШИБКА!
        // ID в XML - "fab_add_task", а ViewBinding генерирует "fabAddTask"
        // Но я обращаюсь к "binding.fabAddTask", что НЕПРАВИЛЬНО. Должно быть binding.fabAddTask
        binding.fabAddTask.setOnClickListener { // Эта строка должна вызывать ошибку
            loadFragment(AddEditTaskFragment.newInstance(), true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(fragment.javaClass.name)
        }
        transaction.commit()
    }

    fun showBottomAppBarAndFab() {
        binding.bottomAppBar.performShow()
        binding.fabAddTask.show()
    }
}