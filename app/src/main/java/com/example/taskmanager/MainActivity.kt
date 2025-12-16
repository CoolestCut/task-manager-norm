package com.example.taskmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.taskmanager.databinding.ActivityMainBinding

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

        binding.fabAddTask.setOnClickListener {
            loadFragment(AddEditTaskFragment.newInstance(), true)
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