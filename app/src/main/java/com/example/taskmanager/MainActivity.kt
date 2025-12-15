package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanager.databinding.ActivityMainBinding
import com.example.taskmanager.ui.viewmodel.TaskViewModel
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel

    private val createTaskLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val title = data?.getStringExtra(CreateTaskActivity.EXTRA_TITLE)
            val description = data?.getStringExtra(CreateTaskActivity.EXTRA_DESCRIPTION)
            val priority = data?.getIntExtra(CreateTaskActivity.EXTRA_PRIORITY, 2)
            val dueDateMillis = data?.getLongExtra(CreateTaskActivity.EXTRA_DUE_DATE, -1L)
            val status = data?.getStringExtra(CreateTaskActivity.EXTRA_STATUS)

            if (title != null && status != null && dueDateMillis != null && priority != null) {
                val dueDate = if (dueDateMillis != -1L) Date(dueDateMillis) else null
                viewModel.addNewTask(title, description ?: "", priority, dueDate, status)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            createTaskLauncher.launch(intent)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_tasks -> {
                    selectedFragment = TasksFragment()
                }
                R.id.navigation_calendar -> {
                    selectedFragment = CalendarFragment()
                }
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_tasks
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}