package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import java.time.LocalDate
import androidx.appcompat.app.AppCompatActivity


class ChooseActionActivity : BaseActivity() {

    companion object {
        const val REQUEST_CHOOSE_ADD = HomeActivity.REQUEST_CHOOSE_ADD
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_action)

        findViewById<Button>(R.id.btnChooseAddTask).setOnClickListener {
            Intent(this, AddTaskActivity::class.java).also { intent ->
                intent.putExtra("SELECTED_DATE", LocalDate.now().toString())
                startActivityForResult(intent, REQUEST_CHOOSE_ADD)
            }
        }

        findViewById<Button>(R.id.btnChooseAddGoal).setOnClickListener {
            Intent(this, AddEditGoalActivity::class.java).also { intent ->
                startActivityForResult(intent, REQUEST_CHOOSE_ADD)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHOOSE_ADD && resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }
}
