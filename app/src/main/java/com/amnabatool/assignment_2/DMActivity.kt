package com.amnabatool.assignment_2
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amnabatool.assignment_2.R


class DMActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dmAdapter: DMAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_4)

        // Navigate to HomeActivity
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.dmRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Set Linear Layout
        recyclerView.setHasFixedSize(true) // Optimize performance

        val dmList = getDMList() // Fetch list of DMs
        dmAdapter = DMAdapter(dmList)
        recyclerView.adapter = dmAdapter
    }

    private fun getDMList(): List<DMUser> {
        return listOf(
            DMUser("Henry Benjamin", R.drawable.user_profile10),
            DMUser("Emily James", R.drawable.user_profile8),
            DMUser("Lily Thomas", R.drawable.user_profile11),
            DMUser("Christopher", R.drawable.user_profile12),
            DMUser("Amy Wesley", R.drawable.user_profile13),
            DMUser("Laura Ryan", R.drawable.user_profile14)
        )
    }
}
