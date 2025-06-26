package com.skyblue.skybluecontacts.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skyblue.skybluecontacts.TrashDiffCallback
import com.skyblue.skybluecontacts.databinding.ItemListTrashBinding
import com.skyblue.skybluecontacts.model.OptionsTrash
import com.skyblue.skybluecontacts.model.TrashContact

class TrashAdapter(private var contacts: MutableList<TrashContact>,
                   private val onClick: (OptionsTrash) -> Unit ) :
    RecyclerView.Adapter<TrashAdapter.ContactViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemListTrashBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.name.text = contact.firstName
        holder.binding.phoneNumber.text = contact.phoneNumber

        holder.binding.restore.setOnClickListener {
            val options = OptionsTrash("restore",
                holder.itemView,
                trashContact = contact)
            onClick(options)
        }

        holder.itemView.setOnLongClickListener{
            val options = OptionsTrash("delete",
                holder.itemView,
                trashContact = contact)
            onClick(options)
            true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newContacts: MutableList<TrashContact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    fun removeItem(trashContact: TrashContact){
        val newList = contacts.toMutableList()
        newList.remove(trashContact)
        updateList(newList)
    }

    private fun updateList(newList: List<TrashContact>) {
        val diffCallback = TrashDiffCallback(contacts, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        contacts.clear()
        contacts.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ContactViewHolder(val binding: ItemListTrashBinding) :
        RecyclerView.ViewHolder(binding.root)
}