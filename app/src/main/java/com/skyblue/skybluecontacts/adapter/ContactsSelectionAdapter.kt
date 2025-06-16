package com.skyblue.skybluecontacts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.R
import com.skyblue.skybluecontacts.databinding.ItemContactsSelectionBinding
import com.skyblue.skybluecontacts.model.ContactsSelection

class ContactsSelectionAdapter(
    private var contacts: List<ContactsSelection>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ContactsSelectionAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactsSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactsSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val context = holder.itemView.context

        with(holder.binding) {
            contactName.text = contact.firstName
            contactPhone.text = contact.phoneNumber

            val selectedColor = ContextCompat.getColor(context, R.color.textColor)
            val defaultColor = ContextCompat.getColor(context, R.color.default_text_color)
            val bgColor = ContextCompat.getColor(
                context,
                if (contact.isSelected) R.color.selected_background_color else R.color.backgroundColor
            )

            contactLayout.setBackgroundColor(bgColor)
//            contactName.setTextColor(if (contact.isSelected) selectedColor else defaultColor)
//            contactPhone.setTextColor(if (contact.isSelected) selectedColor else defaultColor)

            contactLayout.setOnClickListener {
                onItemClick(position)

            }
        }
    }

    override fun getItemCount(): Int = contacts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<ContactsSelection>) {
        contacts = newList
        notifyDataSetChanged()
    }
}
