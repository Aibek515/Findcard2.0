package kz.diploma.findcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_auth.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("users")

        if (auth.currentUser != null) {
            startActivity(Intent(this, SelectActivity::class.java))
            finish()
        }

        login_btn.setOnClickListener {
            val email = login_email.text.toString()
            val pass = login_password.text.toString()
            logInUser(email, pass)
        }
        signup_btn.setOnClickListener {
            val name = signup_name.text.toString()
            val phone = signup_phone.text.toString()
            val email = signup_email.text.toString()
            val pass = signup_password.text.toString()
            val passConf = signup_password_confirm.text.toString()
            signUpUser(name, phone, email, pass, passConf)
        }
        login_signup.setOnClickListener {
            auth_login.visibility = View.GONE
            auth_signup.visibility = View.VISIBLE
        }
        signup_login.setOnClickListener {
            auth_login.visibility = View.VISIBLE
            auth_signup.visibility = View.GONE
        }

        MaskedTextChangedListener.installOn(signup_phone, "{+7} [000] [000] [00] [00]")
    }

    override fun onBackPressed() {
        finish()
    }

    private fun logInUser(email: String, pass: String) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            if (isEmailValid(email)) {
                loader.visibility = View.VISIBLE
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, SelectActivity::class.java))
                        finish()
                    } else {
                        showToast("Неправильные данные")
                    }
                    loader.visibility = View.GONE
                }
            } else {
                showToast("Неправильный email")
            }
        } else {
            showToast(getString(R.string.fill_all_fields))
        }
    }

    private fun signUpUser(
        name: String,
        phone: String,
        email: String,
        pass: String,
        passConf: String
    ) {
        if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && passConf.isNotEmpty()) {
            if (passConf == pass) {
                if (isEmailValid(email)) {
                    loader.visibility = View.VISIBLE
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val userId = mDatabase.push().key
                                userId?.let {
                                    mDatabase.child(email.replace(".", "")).setValue(
                                        User(auth.currentUser?.uid, name, email, phone, 0
                                        )
                                    ).addOnSuccessListener {
                                        startActivity(Intent(this, SelectActivity::class.java))
                                        finish()
                                    }.addOnFailureListener {
                                        it.printStackTrace()
                                    }
                                }
                            } else {
                                showToast("Ошибка при регистрации. Повторите попытку")
                            }
                            loader.visibility = View.GONE
                        }
                } else {
                    showToast("Неправильный email")
                }
            } else {
                showToast("Подвердите пароль")
            }
        } else {
            showToast(getString(R.string.fill_all_fields))
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun isEmailValid(email: String?): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

}