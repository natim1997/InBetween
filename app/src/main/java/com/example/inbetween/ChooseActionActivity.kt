package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import java.time.LocalDate

class ChooseActionActivity : BaseActivity() {

    companion object {
        const val REQUEST_ADD_TASK = HomeActivity.REQUEST_CHOOSE_ADD
        const val REQUEST_ADD_GOAL = 2001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_action)

        findViewById<Button>(R.id.btnChooseAddTask).setOnClickListener {
            Intent(this, AddTaskActivity::class.java).also { intent ->
                intent.putExtra("SELECTED_DATE", LocalDate.now().toString())
                startActivityForResult(intent, REQUEST_ADD_TASK)
            }
        }

        findViewById<Button>(R.id.btnChooseAddGoal).setOnClickListener {
            Intent(this, AddEditGoalActivity::class.java).also { intent ->
                startActivityForResult(intent, REQUEST_ADD_GOAL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_ADD_TASK -> {
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            REQUEST_ADD_GOAL -> {
                val goal = data.getSerializableExtra(AddEditGoalActivity.EXTRA_GOAL) as? GoalItem
                goal?.let {
                    Intent().apply {
                        putExtra(HomeActivity.EXTRA_GOAL, it)
                    }.also { out ->
                        setResult(Activity.RESULT_OK, out)
                        finish()
                    }
                }
            }
        }
    }
}
