package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GoalsActivity : BaseActivity() {

    companion object {
        const val REQUEST_ADD_GOAL = 2001
        const val EXTRA_GOAL = "EXTRA_GOAL"
    }

    private val goals = mutableListOf<GoalItem>()
    private lateinit var rvGoals: RecyclerView
    private lateinit var fabAddGoal: FloatingActionButton
    private lateinit var adapter: GoalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        rvGoals = findViewById(R.id.rvGoals)
        fabAddGoal = findViewById(R.id.fabAddGoal)

        adapter = GoalsAdapter(
            items = goals,
            onEdit = { goal ->
                Intent(this, AddEditGoalActivity::class.java).also {
                    it.putExtra(AddEditGoalActivity.EXTRA_GOAL, goal)
                    startActivityForResult(it, REQUEST_ADD_GOAL)
                }
            },
            onDelete = { goal ->
                goals.remove(goal)
                adapter.notifyDataSetChanged()
            }
        )

        rvGoals.apply {
            layoutManager = LinearLayoutManager(this@GoalsActivity)
            adapter = this@GoalsActivity.adapter
        }

        fabAddGoal.setOnClickListener {
            Intent(this, AddEditGoalActivity::class.java).also {
                startActivityForResult(it, REQUEST_ADD_GOAL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_GOAL && resultCode == RESULT_OK) {
            (data?.getSerializableExtra(EXTRA_GOAL) as? GoalItem)?.let { goal ->
                val idx = goals.indexOfFirst { it.id == goal.id }
                if (idx >= 0) goals[idx] = goal
                else goals.add(goal)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
