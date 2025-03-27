package com.amnabatool.assignment_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment_1.R


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginText=findViewById<TextView>(R.id.loginText)

        registerButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        loginText.setOnClickListener{
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}
