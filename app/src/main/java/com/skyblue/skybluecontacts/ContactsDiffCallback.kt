package com.skyblue.skybluecontacts

import androidx.recyclerview.widget.DiffUtil
import com.skyblue.skybluecontacts.model.ContactsRoom

class ContactsDiffCallback(
    private val oldList: List<ContactsRoom>,
    private val newList: List<ContactsRoom>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Replace with your unique ID check
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
