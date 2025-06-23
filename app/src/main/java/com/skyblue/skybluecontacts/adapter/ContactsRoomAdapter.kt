package com.skyblue.skybluecontacts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.ContactsDiffCallback
import com.skyblue.skybluecontacts.databinding.ItemContactBinding
import com.skyblue.skybluecontacts.model.ContactsRoom
import com.skyblue.skybluecontacts.model.Options

class ContactsRoomAdapter(private var contacts: MutableList<ContactsRoom>,
                         private val onClick: (Options) -> Unit
                         ) : RecyclerView.Adapter<ContactViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    private var expandedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(contacts[position])

         holder.itemView.setOnClickListener {
             // Temp solution for expand and close. again expand same item or id
             if (expandedPosition == selectedPosition){
                 holder.binding.optionsLayout.visibility = View.GONE
                 expandedPosition = null
             }else{
                 holder.binding.optionsLayout.visibility = View.VISIBLE
                 expandedPosition = selectedPosition
             }
             //---
                val previousPosition = selectedPosition
                selectedPosition = position

                if (previousPosition != selectedPosition) {
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                }
            }

        if (position == selectedPosition) {
            holder.binding.optionsLayout.visibility = View.VISIBLE
            expandedPosition = selectedPosition // Temp solution for expand and close. again expand same item or id(line only
        } else {
            holder.binding.optionsLayout.visibility = View.GONE
        }
        val contact = contacts[position]
        holder.binding.callNow.setOnClickListener {

            val options = Options("call",
                holder.itemView,
                contact = contact)
            onClick(options)
        }

        holder.binding.messageNow.setOnClickListener {
            val options = Options("message",
                holder.itemView,
                contact = contact)
            onClick(options)
        }

        holder.binding.whatsappNow.setOnClickListener {
            val options = Options("whatsapp",
                holder.itemView,
                contact = contact)
            onClick(options)
        }

        holder.itemView.setOnLongClickListener {
            val options = Options("delete",
                holder.itemView,
                contact = contact)
            onClick(options)
            true
        }
    }

    override fun getItemCount() = contacts.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContacts: List<ContactsRoom>) {
        contacts = newContacts.toMutableList()
        notifyDataSetChanged()
    }

    fun updateList(newList: List<ContactsRoom>) {
        val diffCallback = ContactsDiffCallback(contacts, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        contacts.clear()
        contacts.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun removeItem(contact: ContactsRoom) {
        val newList = contacts.toMutableList()
        newList.remove(contact)
        updateList(newList)
    }
}

class ContactViewHolder(val binding: ItemContactBinding) :
    RecyclerView.ViewHolder(binding.root){
        fun bind(contact: ContactsRoom) {
            binding.name.text = contact.firstName
            binding.phoneNumber.text = contact.phoneNumber
        }
    }