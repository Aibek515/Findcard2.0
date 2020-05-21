package kz.diploma.findcard

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val context: Context,
    private val onItemClicked: (Card) -> Unit
): RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var cardList: List<Card> = emptyList()

    fun setData(list: List<Card>) {
        cardList = list
        notifyDataSetChanged()
    }

    class CardViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private var numberView: TextView = itemView.findViewById(R.id.item_card_number)
        private var holderView: TextView = itemView.findViewById(R.id.item_card_holder)
        private var shareView: AppCompatImageView = itemView.findViewById(R.id.item_share)

        fun bind(context: Context, card: Card) {
            val textForNumber = "Номер карты: " + card.number.substring(0, 2) + " ... " + card.number.substring(card.number.length-4, card.number.length)
            numberView.text = textForNumber
            val textForCardHolder = "Владелец: " + card.cardHolder
            holderView.text = textForCardHolder
            shareView.setOnClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, R.string.share)
                    putExtra(Intent.EXTRA_TEXT, "Данные найденной карты:\n" +
                            " - $textForNumber\n" +
                            " - $textForCardHolder\n"
                    )
                }
                context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share)))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CardViewHolder(inflater.inflate(R.layout.item_card, parent, false))
    }

    override fun getItemCount(): Int = cardList.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(context = context, card = cardList[position])
        holder.itemView.setOnClickListener { onItemClicked(cardList[position]) }
    }
}