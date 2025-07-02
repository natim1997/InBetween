package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeActivity : BaseActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb  = FirebaseFirestore.getInstance()

    // מי המשתמש שמוצג כרגע
    private lateinit var targetUserId: String
    private lateinit var targetPermission: String

    private var lastViewedTask: TaskItem? = null

    companion object {
        const val REQUEST_CHOOSE_ADD       = 3001
        const val REQUEST_VIEW_TASK        = 3005

        const val EXTRA_VIEW_USER   = "EXTRA_VIEW_USER"
        const val EXTRA_PERMISSION  = "EXTRA_PERMISSION"
        const val EXTRA_NEW_TASK    = "NEW_TASK"
    }

    private val allTasks     = mutableListOf<TaskItem>()
    private var selectedDate = LocalDate.now()
    private var weekStart    = selectedDate.with(DayOfWeek.SUNDAY)
    private val timeFmt      = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var adapter: TaskAdapter

    // UI
    private lateinit var rvDates      : RecyclerView
    private lateinit var rvTasks      : RecyclerView
    private lateinit var fabAddTask   : FloatingActionButton
    private lateinit var fabContacts  : FloatingActionButton
    private lateinit var btnPrevWeek  : ImageButton
    private lateinit var btnNextWeek  : ImageButton
    private lateinit var tvWeekLabel  : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // קבלת פרמטרים (ברירת מחדל: היומן שלי)
        targetUserId     = intent.getStringExtra(EXTRA_VIEW_USER)
            ?: firebaseAuth.currentUser!!.uid
        targetPermission = intent.getStringExtra(EXTRA_PERMISSION)
            ?: "edit"

        // bind views
        rvDates     = findViewById(R.id.rvDates)
        rvTasks     = findViewById(R.id.rvTasks)
        fabAddTask  = findViewById(R.id.fabAddTask)
        fabContacts = findViewById(R.id.fabContacts)
        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
        tvWeekLabel = findViewById(R.id.tvWeekLabel)

        // תצוגת תאריכים
        rvDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = DateAdapter(generateWeekDates(weekStart)) { date ->
            selectedDate = date
            refreshWeek()
        }

        // תצוגת משימות
        adapter = TaskAdapter { task ->
            lastViewedTask = task
            Intent(this, TaskDetailActivity::class.java).apply {
                putExtra(TaskDetailActivity.EXTRA_TASK, task)
                putExtra(TaskDetailActivity.EXTRA_DATE_CONTEXT, selectedDate.toString())
            }.also {
                startActivityForResult(it, REQUEST_VIEW_TASK)
            }
        }
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        // כפתור הוספה
        fabAddTask.setOnClickListener {
            // אם אין הרשאת עריכה לאפשרות הזו
            if (targetPermission != "edit") return@setOnClickListener
            startActivityForResult(
                Intent(this, ChooseActionActivity::class.java),
                REQUEST_CHOOSE_ADD
            )
        }

        // כפתור אנשי קשר
        fabContacts.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        btnPrevWeek.setOnClickListener {
            weekStart = weekStart.minusWeeks(1)
            refreshWeek()
        }
        btnNextWeek.setOnClickListener {
            weekStart = weekStart.plusWeeks(1)
            refreshWeek()
        }

        // הסתירו קוד לא רלוונטי:
        if (targetPermission != "edit") {
            fabAddTask.visibility = View.GONE
        }

        loadTasksFromFirestore()
        refreshWeek()
    }

    private fun loadTasksFromFirestore() {
        firestoreDb.collection("users")
            .document(targetUserId)
            .collection("tasks")
            .addSnapshotListener { snaps, error ->
                if (error != null || snaps == null) return@addSnapshotListener
                allTasks.clear()
                for (doc in snaps.documents) {
                    val dateStr = doc.getString("date")?.takeIf { it.isNotBlank() } ?: continue
                    val title   = doc.getString("title") ?: continue
                    val start   = doc.getString("startTime")?.takeIf { it.isNotBlank() } ?: continue
                    val end     = doc.getString("endTime")?.takeIf { it.isNotBlank() } ?: continue

                    val isDaily = doc.getBoolean("isDaily") ?: false
                    val isWeekly = doc.getBoolean("isWeekly") ?: false
                    val recEnd = doc.getString("recurrenceEndDate")
                        ?.takeIf { it.isNotBlank() }?.let(LocalDate::parse)
                    val excluded = (doc.get("excludedDates") as? List<String> ?: emptyList())
                        .mapNotNull { it.takeIf { it.isNotBlank() }?.let(LocalDate::parse) }
                        .toMutableList()

                    allTasks += TaskItem(
                        title = title,
                        date = LocalDate.parse(dateStr),
                        startTime = start,
                        endTime = end,
                        isDaily = isDaily,
                        isWeekly = isWeekly,
                        recurrenceEndDate = recEnd,
                        excludedDates = excluded
                    )
                }
                refreshWeek()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHOOSE_ADD && resultCode == Activity.RESULT_OK && data != null) {
            // אם חזרנו עם משימה חדשה
            (data.getSerializableExtra(EXTRA_NEW_TASK) as? TaskItem)?.let { task ->
                saveTaskToFirestore(task)
            }
        }
        // כל שאר התוצאות (למחיקה וכו') מטופלות ב־snapshotListener
    }

    private fun saveTaskToFirestore(task: TaskItem) {
        val tasksCol = firestoreDb
            .collection("users")
            .document(targetUserId)
            .collection("tasks")
        val docId = "${task.title}_${task.date}"
        val data = mutableMapOf<String, Any>(
            "title"     to task.title,
            "date"      to task.date.toString(),
            "startTime" to task.startTime,
            "endTime"   to task.endTime,
            "isDaily"   to task.isDaily,
            "isWeekly"  to task.isWeekly
        )
        task.recurrenceEndDate?.let { data["recurrenceEndDate"] = it.toString() }
        if (task.excludedDates.isNotEmpty())
            data["excludedDates"] = task.excludedDates.map { it.toString() }

        tasksCol.document(docId)
            .set(data)
            .addOnSuccessListener { /* יתווסף אוטומטית ל־allTasks דרך ה־listener */ }
            .addOnFailureListener { e -> Log.e("Home", "Save failed", e) }
    }

    private fun refreshWeek() {
        val dates = generateWeekDates(weekStart)
        (rvDates.adapter as DateAdapter).updateDates(dates)
        if (selectedDate !in dates) selectedDate = weekStart
        updateTasks()

        val monthEnglish = weekStart.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        tvWeekLabel.text = "Week of ${weekStart.dayOfMonth} $monthEnglish"
    }

    private fun updateTasks() {
        val list = allTasks.filter { t ->
            if (selectedDate in t.excludedDates) return@filter false
            when {
                t.date == selectedDate -> true
                t.isDaily && !selectedDate.isBefore(t.date)
                        && (t.recurrenceEndDate == null || !selectedDate.isAfter(t.recurrenceEndDate)) -> true
                t.isWeekly && !selectedDate.isBefore(t.date)
                        && (t.recurrenceEndDate == null || !selectedDate.isAfter(t.recurrenceEndDate))
                        && selectedDate.dayOfWeek == t.date.dayOfWeek -> true
                else -> false
            }
        }.sortedBy { LocalTime.parse(it.startTime, timeFmt) }

        adapter.submitList(list)
    }

    private fun generateWeekDates(center: LocalDate) =
        (0L..6L).map { center.with(DayOfWeek.SUNDAY).plusDays(it) }
}
