package kz.diploma.findcard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.activity_add_card.*

class AddCardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        setToolbar(add_toolbar)
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("cards")

        add_button.setOnClickListener {
            val number = add_number_edt.text.trim()
            val cardholder = add_cardholder_edt.text.trim()
            val date = add_date_edt.text.trim()
            if (number.isNotEmpty() && cardholder.isNotEmpty() && date.isNotEmpty()) {
                if (number.length == 19) {
                    if (date.length == 5) {
                        addCard(
                            Card(
                                number = number.toString(),
                                cardHolder = cardholder.toString(),
                                date = date.toString(),
                                founderId = auth.currentUser?.uid
                            )
                        )
                    } else {
                        showToast("Введите дату")
                    }
                } else {
                    showToast("Введите номер карты")
                }
            } else {
                showToast(getString(R.string.fill_all_fields))
            }
        }
        MaskedTextChangedListener.installOn(add_date_edt, "[00]{/}[00]")
        MaskedTextChangedListener.installOn(add_number_edt, "[0000] [0000] [0000] [0000]")
    }

    override fun onBackPressed() {
        finish()
    }

    private fun addCard(card: Card) {
        loader.visibility = View.VISIBLE
        val cardId = mDatabase.push().key
        cardId?.let {
            card.id = it
            mDatabase.child(it).setValue(card).addOnSuccessListener {
                loader.visibility = View.GONE
                finish()
            }.addOnFailureListener {
                it.printStackTrace()
                loader.visibility = View.GONE
                showToast(getString(R.string.error_message))
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun setToolbar(toolbar: Toolbar) {
        toolbar.apply {
            navigationIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_black_24dp)?.mutate()
                    ?.apply {
                        val headerColor = ContextCompat.getColor(applicationContext, R.color.black)
                        DrawableCompat.setTint(this, headerColor)
                    }
            setSupportActionBar(this)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.from_left_in, R.anim.from_right_out);
    }
}
