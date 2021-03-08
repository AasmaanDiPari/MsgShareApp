package com.example.msgshareapp

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.msgshareapp.R.layout.new_msg
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.new_msg.*
import kotlinx.android.synthetic.main.user_row_newmsg.view.*


class NewMessageActivity:AppCompatActivity() {
    companion object {
        val user_key = "User_Key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(new_msg)

        supportActionBar?.title = "Select User"
        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("New message", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val user_item = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra(user_key, user_item.user)
                    startActivity(intent)
                    finish()
                }

                recycler_view.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }
}
        class UserItem(val user: User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_row_newmsg.text = user.user_name
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.imageView_row_newmsg)

        }

        override fun getLayout(): Int {
            return R.layout.user_row_newmsg
        }
    }
        @Parcelize
        class User(val uid:String, val user_name:String, val profileImageUrl:String):Parcelable {
        constructor():this("","","")}

