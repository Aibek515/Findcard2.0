package kz.diploma.findcard

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_card_list.*


class CardListActivity : AppCompatActivity() {

    private var list: List<Card> = emptyList()
    private lateinit var adapter: CardAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var confirmationBottomFragment: ConfirmationBottomFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list)
        setToolbar(list_toolbar)
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().getReference("cards")

        adapter = CardAdapter(
            context = this,
            onItemClicked = {
                if (it.founderId != auth.currentUser?.uid)
                    showAlertDialog(it)
                else
                    deleteCard(it)
            }
        )

        list_search_edt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { filterList(s.trim().toString()) }
            }
        })
        list_recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        mDatabase.addValueEventListener(listListener)
    }

    override fun onStop() {
        super.onStop()
        mDatabase.removeEventListener(listListener)
    }

    override fun onBackPressed() {
        finish()
    }

    private val listListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            p0.toException().printStackTrace()
        }

        override fun onDataChange(p0: DataSnapshot) {
            list = if (p0.value != null) {
                val values: HashMap<String, HashMap<String, String>> =
                    p0.value as HashMap<String, HashMap<String, String>>
                values.values.map {
                    Card(
                        it.getValue("id"),
                        it.getValue("number"),
                        it.getValue("cardHolder"),
                        it.getValue("date"),
                        it.getValue("founderId")
                    )
                }
            } else {
                emptyList()
            }
            adapter.setData(list)
            list_loader.visibility = View.GONE
            if (list.isNullOrEmpty()) {
                list_empty.visibility = View.VISIBLE
            } else {
                list_recycler.visibility = View.VISIBLE
            }
        }
    }

    private fun deleteCard(card: Card) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Эту карту добавили вы")
        builder.setMessage("Вы хотите удалить? ")
        builder.setPositiveButton("Да") { dialog, _ ->
            card.id?.let {
                mDatabase.child(it).removeValue()
            }
            dialog.cancel()
        }
        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    @SuppressLint("DefaultLocale")
    private fun filterList(key: String) {
        var filtered = list.filter {
            it.cardHolder.toLowerCase().contains(key.toLowerCase())
                    || it.number.toLowerCase().substring(0, 2).contains(key.toLowerCase())
                    || it.number.toLowerCase().substring(it.number.length-4, it.number.length).contains(key.toLowerCase())
        }
        if (filtered.isEmpty()) filtered = list
        adapter.setData(filtered)
    }

    private fun showAlertDialog(card: Card) {
        card.founderId?.let {
            confirmationBottomFragment = ConfirmationBottomFragment.newInstance(it, card.number)
            confirmationBottomFragment.show(supportFragmentManager, "CONF_TAG")
        }
    }

    private fun setToolbar(toolbar: Toolbar) {
        toolbar.apply {
            navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_black_24dp)?.mutate()?.apply {
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
        overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out)
    }
}
