package kz.diploma.findcard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.*
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.layout_confirm_bottom_sheet.*
import java.lang.Exception


class ConfirmationBottomFragment : BottomSheetDialogFragment() {

    private lateinit var mDatabase: DatabaseReference
    private var id: String? = null
    private var cardNumber: String? = null
    private var userr: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        id = arguments?.getString(ID)
        cardNumber = arguments?.getString(CARD_NUMBER)
        mDatabase = FirebaseDatabase.getInstance().getReference("users")
        return inflater.inflate(R.layout.layout_confirm_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        confirmation_check.setOnClickListener {
            val cardNumEdtTxt = confirmation_card_number.text
            if (cardNumEdtTxt.length == 19) {
                if (confirmation_card_number.text.toString() == cardNumber) {
                    loader.visibility = View.VISIBLE
                    mDatabase.addValueEventListener(listener)
                } else {
                    Toast.makeText(context, R.string.it_is_not_years, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            }
        }
        confirmation_thanks.setOnClickListener {
            userr?.let {
                loader.visibility = View.VISIBLE
                mDatabase.addValueEventListener(listener2)
                mDatabase.child(it.email.replace(".", "")).child("profit").setValue(it.profit + 1)
            }
        }
        MaskedTextChangedListener.installOn(confirmation_card_number, "[0000] [0000] [0000] [0000]")
    }

    override fun onStop() {
        super.onStop()
        try {
            mDatabase.removeEventListener(listener)
            mDatabase.removeEventListener(listener2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.mapNotNull {
                val post = it.getValue<User>(User::class.java)
                if (post?.id == id) {
                    userr = post
                    confirmation_name?.text = "${getString(R.string.found_card)} ${post?.name}"
                    confirmation_phone?.text = post?.phone
                    confirmation_email?.text = post?.email
                    confirmation_phone?.setOnClickListener { sendToPhone(post) }
                    confirmation_email?.setOnClickListener { sendToEmail(post) }

                    loader.visibility = View.GONE
                    lay1.visibility = View.GONE
                    lay2.visibility = View.VISIBLE
                    return
                }
            }
            loader.visibility = View.GONE
            Toast.makeText(context, R.string.it_is_not_years, Toast.LENGTH_SHORT).show()
        }

        override fun onCancelled(err: DatabaseError) {
            loader.visibility = View.GONE
            Toast.makeText(context, R.string.error_message, Toast.LENGTH_SHORT).show()
            err.toException().printStackTrace()
        }
    }

    private val listener2 = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            loader.visibility = View.GONE
            Toast.makeText(context, R.string.thanks_received, Toast.LENGTH_SHORT).show()
            this@ConfirmationBottomFragment.dismiss()
        }

        override fun onCancelled(err: DatabaseError) {
            loader.visibility = View.GONE
            Toast.makeText(context, R.string.error_message, Toast.LENGTH_SHORT).show()
            err.toException().printStackTrace()
            this@ConfirmationBottomFragment.dismiss()
        }
    }

    private fun sendToPhone(post: User?) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", post?.phone, null))
        startActivity(intent)
    }

    private fun sendToEmail(post: User?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", post?.email, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.email_subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)} $cardNumber")
        startActivity(Intent.createChooser(emailIntent, getString(R.string.write)))
    }

    companion object {
        private const val ID = "ID"
        private const val CARD_NUMBER = "CARD"

        fun newInstance(id: String, number: String) =
            ConfirmationBottomFragment().apply {
                arguments = Bundle().apply {
                    putString(ID, id)
                    putString(CARD_NUMBER, number)
                }
            }
    }
}