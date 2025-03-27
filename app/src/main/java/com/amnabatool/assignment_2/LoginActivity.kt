package com.amnabatool.assignment_2
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.assignment_1.R


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerText=findViewById<TextView>(R.id.registerText)

        loginButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        registerText.setOnClickListener{
            val intent=Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}
