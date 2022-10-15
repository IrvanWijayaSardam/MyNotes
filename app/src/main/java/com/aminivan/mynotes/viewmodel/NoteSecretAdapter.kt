package com.aminivan.mynotes.viewmodel

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aminivan.mynotes.R
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.databinding.ItemNoteBinding
import com.aminivan.mynotes.helper.NoteDiffCallback


class NoteSecretAdapter(var listener: NoteSecretAdapter.OnAdapterListener) :
    RecyclerView.Adapter<NoteSecretAdapter.NoteSecretViewHolder>() {
    private lateinit var context : Context
    val listNotes = ArrayList<Note>()

    fun setListNotes(listNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(this.listNotes, listNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listNotes.clear()
        this.listNotes.addAll(listNotes)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class NoteSecretViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SuspiciousIndentation")
        fun bind(note: Note) {
            binding.dataNotes = note
            binding.ivDelete.setOnClickListener{
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.custom_dialog_delete)
                dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                val btnDeleteYes : Button = dialog.findViewById(R.id.btnDeleteYes)
                val btnDeleteNo : Button = dialog.findViewById(R.id.btnDeleteNo)

                btnDeleteYes.setOnClickListener(){
                    listener.onDelete(note)
                    dialog.dismiss()
                }
                btnDeleteNo.setOnClickListener(){
                    Toast.makeText(context, "No Clicked", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                dialog.show()
            }
            binding.ivUpdate.setOnClickListener{
                listener.onUpdate(note)
            }

            binding.cvItemNote.setOnClickListener {
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.custom_dialog_detail)
                dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                val judul : EditText = dialog.findViewById(R.id.edtDetailJudul)
                val catatan : EditText = dialog.findViewById(R.id.edtDetailCatatan)
                val ivCopy : ImageView = dialog.findViewById(R.id.ivCopy)
                judul.setText(note.title)
                catatan.setText(note.description)
                ivCopy.setOnClickListener {
                    val clipboardManager = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("text", note.description)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(itemView.context, "Text copied to clipboard", Toast.LENGTH_LONG).show()
                }
                dialog.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteSecretViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteSecretViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteSecretViewHolder, position: Int) {
        holder.bind(listNotes[position])
    }

    override fun getItemCount(): Int {
        return listNotes.size

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }
    interface OnAdapterListener {
        fun onDelete(note: Note)
        fun onUpdate(note: Note)
    }
}