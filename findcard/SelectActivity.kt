package kz.diploma.findcard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_select.*

class SelectActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("users")

        select_found_card.setOnClickListener {
            val intent = Intent(this, AddCardActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
        }
        select_search_card.setOnClickListener {
            val intent = Intent(this, CardListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.from_left_in, R.anim.from_right_out);
        }
    }

    override fun onResume() {
        super.onResume()
        mDatabase.addValueEventListener(listener)
        select_logout.setOnClickListener {
            mDatabase.removeEventListener(listener)
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        mDatabase.removeEventListener(listener)
    }

    override fun onBackPressed() {
        finish()
    }

    val listener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            p0.toException().printStackTrace()
            main_loader.visibility = View.GONE
            Toast.makeText(baseContext, R.string.error_message, Toast.LENGTH_SHORT).show()
        }

        override fun onDataChange(p0: DataSnapshot) {
            p0.children.mapNotNull {
                val post = it.getValue<User>(User::class.java)
                if (post?.id == auth.currentUser?.uid) {
                    main_name.text = "Ваше имя: ${post?.name}"
                    main_profit.text = "Ваш баланс: ${post?.profit}"
                    main_loader.visibility = View.GONE
                    main_profit.visibility = View.VISIBLE
                    main_name.visibility = View.VISIBLE
                    return
                }
                main_loader.visibility = View.GONE
            }
        }
    }

}
