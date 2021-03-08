package com.example.msgshareapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.msgshareapp.R.layout.chat_log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_log.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class ChatLogActivity:AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(chat_log)
        recycler_view_chat_log.adapter = adapter

        toUser=intent.getParcelableExtra<User>(NewMessageActivity.user_key)
        supportActionBar?.title = toUser?.user_name

        //setDummyData()
        ListenForMessages()
        button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()


        }
    }

    private fun ListenForMessages() {

        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

                val chatMessage: ChatMessage? = p0.getValue(ChatMessage::class.java)
                if (chatMessage == null) return
                else {
                    Log.d(TAG, chatMessage.text)
                    if(chatMessage.FromId==FirebaseAuth.getInstance().uid){

                       val currentUser=LatestMsgActivity.currentUser
                        if(currentUser==null){
                            Log.d(TAG,"currentUser null")
                            return
                        }
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))

                    }
                    else{

                         if(toUser==null){
                             Log.d(TAG,"touser null")
                         }
                        else{
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))}
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }


            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    class ChatMessage(val id: String, val text: String, val FromId: String,val toId:String,val timestamp: Long) {
        constructor() : this("", "", "", "",-1)
    }

    private fun performSendMessage() {


        val text = editText_chat_log.text!!.toString()
        val FromId = FirebaseAuth.getInstance().uid
        val user=intent.getParcelableExtra<User>(NewMessageActivity.user_key)
        val toId =user?.uid
        Log.d(TAG, "1")

        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        Log.d(TAG, "2")
        if (FromId == null) {
            Log.d(TAG,"is null")
            return
        }
        if (toId == null) {
            Log.d(TAG," to is null")
            return
        }

        else {
            val chat_message =
                ChatMessage(reference.key!!, text, FromId,toId,System.currentTimeMillis() / 1000)
            reference.setValue(chat_message).addOnSuccessListener {
                Log.d(TAG, "Saved message:${reference.key}")
            }


        }
    }}


    class ChatFromItem(val text: String,val user: User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.text_view_chat_row_from.text = text

            val uri=user.profileImageUrl
            val TargetImageView=viewHolder.itemView.imageButton_chat_row_from
            Picasso.get().load(uri).into(TargetImageView)

        }

        override fun getLayout(): Int {
            return R.layout.chat_from_row

        }
    }

    class ChatToItem(val text: String,val user:User) : Item<ViewHolder>() {
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.text_view_chat_row_to.text = this.text
            val uri=user.profileImageUrl
            val TargetImageView=viewHolder.itemView.imageButton_chat_row_to
            Picasso.get().load(uri).into(TargetImageView)


        }


        override fun getLayout(): Int {
            return R.layout.chat_to_row

        }
    }
