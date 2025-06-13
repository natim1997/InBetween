package com.example.inbetween


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeActivity : BaseActivity() {

    companion object {
        private const val REQUEST_ADD_TASK = 1001
    }

    private lateinit var rvDates   : RecyclerView
    private lateinit var rvTasks   : RecyclerView
    private lateinit var fabAddTask: FloatingActionButton

    // המאגר של כל המשימות
    private val allTasks = mutableListOf<TaskItem>()

    // התאריך הנבחר כרגע
    private var selectedDate: LocalDate = LocalDate.now()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvDates    = findViewById(R.id.rvDates)
        rvTasks    = findViewById(R.id.rvTasks)
        fabAddTask = findViewById(R.id.fabAddTask)

        // 1) הגדרת הקרוסלה של התאריכים
        rvDates.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = DateAdapter(generateWeekDates(selectedDate)) { date ->
            selectedDate = date
            updateTasks()
        }

        // 2) הגדרת RecyclerView של המשימות
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = TaskAdapter()

        // 3) כפתור להוספת משימה
        fabAddTask.setOnClickListener {
            Intent(this, AddTaskActivity::class.java).also { intent ->
                intent.putExtra("SELECTED_DATE", selectedDate.toString())
                startActivityForResult(intent, REQUEST_ADD_TASK)
            }
        }

        // הצגת המשימות של היום הנבחר
        updateTasks()
    }

    // מסנן וממיין את המשימות לפי השעה ומעדכן את ה־RecyclerView
    private fun updateTasks() {
        val tasksForDay = allTasks
            .filter  { it.date == selectedDate }
            .sortedBy { LocalTime.parse(it.startTime, timeFormatter) }
        (rvTasks.adapter as TaskAdapter).submitList(tasksForDay)
    }

    // קבלת משימה חדשה מ־AddTaskActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_TASK && resultCode == RESULT_OK) {
            val newTask = data
                ?.getSerializableExtra("NEW_TASK") as? TaskItem
            newTask?.let {
                allTasks.add(it)
                updateTasks()
            }
        }
    }

    // מחזיר 7 תאריכים, מיומי ראשון עד שבת סביב 'center'
    private fun generateWeekDates(center: LocalDate): List<LocalDate> {
        val sunday = center.with(DayOfWeek.SUNDAY)
        return (0L..6L).map { sunday.plusDays(it) }
    }
}
