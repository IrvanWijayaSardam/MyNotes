package com.aminivan.mynotes.viewmodel

import android.R.attr.label
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aminivan.mynotes.R
import com.aminivan.mynotes.database.Note
import com.aminivan.mynotes.databinding.ItemNoteBinding
import com.aminivan.mynotes.helper.DateHelper
import com.aminivan.mynotes.helper.NoteDiffCallback
import com.aminivan.mynotes.helper.SwipeToDeleteCallback


class NoteAdapter(var listener : OnAdapterListener) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private lateinit var context : Context
    val listNotes = ArrayList<Note>()

    fun setListNotes(listNotes: List<Note>) {
        val diffCallback = NoteDiffCallback(this.listNotes, listNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.listNotes.clear()
        this.listNotes.addAll(listNotes)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)


    }
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(listNotes[position])
    }
    override fun getItemCount(): Int {
        return listNotes.size
    }
    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

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
                            Toast.makeText(context, "Yes Clicked , data ${binding.dataNotes}", Toast.LENGTH_SHORT).show()
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
                val dialog = Dialog(context)
                dialog.setContentView(R.layout.custom_dialog)
                dialog.getWindow()!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                val tvInputCustomDialog : TextView = dialog.findViewById(R.id.tvInputCustomDialog)
                val judul : EditText = dialog.findViewById(R.id.edtJudul)
                val catatan : EditText = dialog.findViewById(R.id.edtCatatan)
                val submit : Button = dialog.findViewById(R.id.btnSubmit)

                tvInputCustomDialog.setText("Update Notes")
                submit.setText("Update")
                judul.setText(note.title)
                catatan.setText(note.description)

                submit.setOnClickListener(){
                    note.let { note ->
                        note?.title = judul.text.toString()
                        note?.description = catatan.text.toString()
                        note?.date = DateHelper.getCurrentDate()
                    }
                    listener.onUpdate(note)
                    dialog.dismiss()
                }
                dialog.show()
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
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        context = recyclerView.context
    }
    interface OnAdapterListener {
        fun onDelete(note: Note)
        fun onUpdate(note: Note)
    }

}