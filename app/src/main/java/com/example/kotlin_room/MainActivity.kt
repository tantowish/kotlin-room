package com.example.kotlin_room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.kotlin_room.database.Note
import com.example.kotlin_room.database.NoteDao
import com.example.kotlin_room.database.NoteRoomDatabase
import com.example.kotlin_room.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mNotesDao: NoteDao
    private lateinit var executorService: ExecutorService
    private var updateId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        executorService = Executors.newSingleThreadExecutor()
        val db = NoteRoomDatabase.getDatabase(this)
        mNotesDao = db!!.noteDao()!!
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding){
            btnAdd.setOnClickListener(
                View.OnClickListener{
                    insert(
                        Note(
                            title = etTitle.text.toString(),
                            description = etDesc.text.toString(),
                            date = etDate.text.toString()
                        )
                    )
                    resetData()
                }
            )

            btnUpdate.setOnClickListener {
                update(
                    Note(
                        id = updateId,
                        title = etTitle.getText().toString(),
                        description = etDesc.getText().toString(),
                        date = etDate.getText().toString()
                    )
                )
                updateId = 0
                resetData()
            }

            lvNote.setOnItemClickListener{ adapterView, view, position, id->
                val item = adapterView.adapter.getItem(position) as Note
                updateId = item.id
                etTitle.setText(item.title)
                etDesc.setText(item.description)
                etDate.setText(item.date)
            }

            lvNote.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { adapterView, _, i, _ ->
                    val item = adapterView.adapter.getItem(i) as Note
                    delete(item)
                    true
                }
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun resetData(){
        with(binding){
            etTitle.setText("")
            etDesc.setText("")
            etDate.setText("")
        }
    }

    private fun getAllNotes(){
        mNotesDao.allNotes.observe(this){
            notes->
            val adapter: ArrayAdapter<Note> = ArrayAdapter<Note>(
                this,
                android.R.layout.simple_list_item_1, notes
            )
            binding.lvNote.adapter = adapter
        }
    }

    private fun insert(note: Note){
        executorService.execute{mNotesDao.insert(note)}
    }

    private fun update(note: Note){
        executorService.execute{mNotesDao.update(note)}
    }

    private fun delete(note: Note){
        executorService.execute{mNotesDao.delete(note)}
    }
}