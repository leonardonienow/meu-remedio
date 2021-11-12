package br.meuRemedio.trabalho.activity


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import br.meuRemedio.trabalho.R
import br.meuRemedio.trabalho.database.DBManager
import br.meuRemedio.trabalho.databinding.ActivityMainBinding
import br.meuRemedio.trabalho.entity.Remedio
import java.util.*
import kotlinx.coroutines.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.ln


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var noteList = ArrayList<Remedio>()
    private var mSharedPref: SharedPreferences? = null
    private val projection = arrayOf("ID", "NAME", "DIAS", "DATAHORA", "DOSAGEM", "VOLUME", "PERIODICIDADE")
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_15sec)

        setContentView(R.layout.activity_main)
        mSharedPref = this.getSharedPreferences("My_Data", Context.MODE_PRIVATE)
        loadSharedPref()
        initAlarm(){
            Handler(Looper.getMainLooper()).post {
                try {
                for (remedio: Remedio in noteList) {
                    Log.i("remedio.nodePeriodicidade", remedio.nodePeriodicidade )

                    val rightNow = Calendar.getInstance()
                    val currentHourIn24Format = rightNow[Calendar.HOUR_OF_DAY]
                    val currentMinuteIn24Format = rightNow[Calendar.MINUTE]
                    var currentDay = rightNow[Calendar.DAY_OF_MONTH]
                    var dias: ArrayList<String> = ArrayList()

                    if(remedio.nodePeriodicidade.isNotEmpty()){
                        dias = ArrayList(remedio.nodePeriodicidade.substring(0, remedio.nodePeriodicidade.length - 1).split("|"))
                        val qtdeDias = dias.count()
                        var ultimoDia = dias.last()

                        var ultimoDiaInt = 0;

                        if(ultimoDia == null){
                            ultimoDiaInt = 0
                        }else{
                            ultimoDiaInt = ultimoDia.toInt()
                        }

                        if(ultimoDiaInt < currentDay && remedio.nodeDias == "INDETERMINADO")
                        {
                            if("$currentHourIn24Format:$currentMinuteIn24Format" == remedio.nodeDataHora) {
                                showAlarmDialog(remedio)
                            }
                        }else if(ultimoDiaInt < currentDay && remedio.nodeDias.toInt() <= qtdeDias){
                            if("$currentHourIn24Format:$currentMinuteIn24Format" == remedio.nodeDataHora) {
                                showAlarmDialog(remedio)
                            }
                        }
                    }else if("$currentHourIn24Format:$currentMinuteIn24Format" == remedio.nodeDataHora) {
                         showAlarmDialog(remedio)
                    }
                }
                } catch (e: Exception) {
                    Log.i("ERROR", e.toString())
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadSharedPref()
    }

    private fun loadSharedPref() {
        "%".loadQueryNewest()
    }

    @SuppressLint("Range")
    private fun String.loadQueryNewest() {
        val dbManager = DBManager(this@MainActivity)
        val selectionArgs = arrayOf(this)
        val cursor = dbManager.query(projection,
            "NAME like ?", selectionArgs, "NAME")
        noteList.clear()
        if (cursor!!.moveToLast()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val name = cursor.getString(cursor.getColumnIndex("NAME"))
                val dias = cursor.getString(cursor.getColumnIndex("DIAS"))
                val dataHora = cursor.getString(cursor.getColumnIndex("DATAHORA"))
                val dosagem = cursor.getString(cursor.getColumnIndex("DOSAGEM"))
                val volume = cursor.getString(cursor.getColumnIndex("VOLUME"))
                val periodicidade = cursor.getString(cursor.getColumnIndex("PERIODICIDADE"))
                noteList.add(Remedio(id, name, dias, dataHora, dosagem, volume, periodicidade))
            } while (cursor.moveToPrevious())
        }
        val notesAdapter = MyNotesAdapter( noteList)
        findViewById<ListView>(R.id.notesLV).adapter = notesAdapter
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.subtitle = "Total ${notesAdapter.count} remédio(s)..."
        }
    }

    @SuppressLint("Range")
    private fun String.loadQueryAscending() {
        val dbManager = DBManager(this@MainActivity)
        val selectionArgs = arrayOf(this)
        val cursor = dbManager.query(projection,
            "NAME like ?", selectionArgs, "NAME")
        noteList.clear()
        if (cursor!!.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val name = cursor.getString(cursor.getColumnIndex("NAME"))
                val dias = cursor.getString(cursor.getColumnIndex("DIAS"))
                val dataHora = cursor.getString(cursor.getColumnIndex("DATAHORA"))
                val dosagem = cursor.getString(cursor.getColumnIndex("DOSAGEM"))
                val volume = cursor.getString(cursor.getColumnIndex("VOLUME"))
                val periodicidade = cursor.getString(cursor.getColumnIndex("PERIODICIDADE"))
                noteList.add(Remedio(id, name, dias, dataHora, dosagem, volume, periodicidade))
            } while (cursor.moveToNext())

        }
        val notesAdapter = MyNotesAdapter(noteList)
        findViewById<ListView>(R.id.notesLV).adapter = notesAdapter
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.subtitle = "Total ${notesAdapter.count} remédio(s)..."
        }
    }

    @SuppressLint("Range")
    private fun loadSearchQuery(query: String) {

        val dbManager = DBManager(this)
        val projection = arrayOf("*")
        val selectionArgs = arrayOf(query)
        val string = "Last update: "
        val cursor = dbManager.query(projection,
            "NAME like ?", selectionArgs, "NAME")

        noteList.clear()
        if (cursor!!.moveToLast()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex("ID"))
                val name = cursor.getString(cursor.getColumnIndex("NAME"))
                val dias = cursor.getString(cursor.getColumnIndex("DIAS"))
                val dataHora = cursor.getString(cursor.getColumnIndex("DATAHORA"))
                val dosagem = cursor.getString(cursor.getColumnIndex("DOSAGEM"))
                val volume = cursor.getString(cursor.getColumnIndex("VOLUME"))
                val periodicidade = cursor.getString(cursor.getColumnIndex("PERIODICIDADE"))
                noteList.add(Remedio(id, name, dias, dataHora, dosagem, volume, periodicidade))
            } while (cursor.moveToPrevious())
        }
        val notesAdapter = MyNotesAdapter(noteList)
        findViewById<ListView>(R.id.notesLV).adapter = notesAdapter
        val mActionBar = supportActionBar
        if (mActionBar != null) {
            mActionBar.subtitle = "Total ${notesAdapter.count} remédio(s)..."
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val sv: SearchView = menu!!.findItem(R.id.app_bar_search).actionView as SearchView
        sv.maxWidth = 800
        val sm = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(sm.getSearchableInfo(componentName))
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                "%$query%".loadQueryAscending()
                if (noteList.size == 0) loadSearchQuery("%$query%")
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                "%$query%".loadQueryAscending()
                if (noteList.size == 0) loadSearchQuery("%$query%")
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_addNote -> {
                startActivity(Intent(this, AddNoteActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    inner class MyNotesAdapter
        (private val listNotes: ArrayList<Remedio>) : BaseAdapter() {

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val rowView = layoutInflater.inflate(R.layout.activity_row, parent, false)
            val note = listNotes[position]

            rowView.findViewById<TextView>(R.id.titleH).text = note.nodeDataHora
            rowView.findViewById<TextView>(R.id.titleTV).text = "Remédio: ${note.nodeName}"
            rowView.findViewById<TextView>(R.id.titleD).text ="Dias: ${note.nodeDias }"
            rowView.findViewById<TextView>(R.id.descrTV).text ="Dosagem: ${note.nodeDosagem}"

            rowView.findViewById<AppCompatImageButton>(R.id.deleteBtn)
                .setOnClickListener{showDeleteDialog(note)}
            rowView.findViewById<AppCompatImageButton>(R.id.editBtn)
                .setOnClickListener{updateNoteAction(note)}

            return rowView
        }

        override fun getItem(position: Int): Any {
            return listNotes[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listNotes.size
        }
    }

    private fun updateNoteAction(note: Remedio) {
        val intent = Intent(this, AddNoteActivity::class.java)
        intent.putExtra("ID", note.nodeID)
        intent.putExtra("DATAHORA", note.nodeDataHora)
        intent.putExtra("DIAS", note.nodeDias)
        intent.putExtra("DOSAGEM", note.nodeDosagem)
        intent.putExtra("VOLUME", note.nodeVolume)
        intent.putExtra("NAME", note.nodeName)

        startActivity(intent)
    }

    private fun showDeleteDialog(note: Remedio) {
        val dbManager = DBManager(this)
        val selectionArgs = arrayOf(note.nodeID.toString())
        val removeNoteDialogListener = DialogInterface.OnClickListener { _, i ->
            when(i){
                AlertDialog.BUTTON_POSITIVE ->{
                    dbManager.delete("ID = ?", selectionArgs)
                    "%".loadQueryAscending()
                    Toast.makeText(this,
                        "Remédio removido!", Toast.LENGTH_SHORT).show()}
                AlertDialog.BUTTON_NEGATIVE ->{}
            }
        }
        val mBuilder = AlertDialog.Builder(this).apply {
            setTitle("${getString(R.string.del_dlg_note_ttl)} ${note.nodeName}!")
            setIcon(R.drawable.ic_action_alert)
            setMessage(getString(R.string.del_dlg_note_msg))
            setPositiveButton(getString(R.string.del_dlg_ok_btn),  removeNoteDialogListener)
            setNegativeButton(getString(R.string.del_dlg_ccl_btn), removeNoteDialogListener)
        }
        mBuilder.create().show()
    }

    private fun showAlarmDialog(note: Remedio) {
        val volume = (1 - (ln(100.00 - note.nodeVolume.toFloat()) / ln(100.00))).toFloat();
        mediaPlayer?.setVolume(volume, volume);
        mediaPlayer?.start()
        val rightNow = Calendar.getInstance()
        val dayOfMonth = rightNow[Calendar.DAY_OF_MONTH]
        val dbManager = DBManager(this)
        val selectionArgs = arrayOf(note.nodeID.toString())
        val values = ContentValues()
        values.put("PERIODICIDADE", note.nodePeriodicidade + dayOfMonth + "|")
        dbManager.update(values, "ID = ?", selectionArgs)

        val removeNoteDialogListener = DialogInterface.OnClickListener { _, i ->
            when(i){
                AlertDialog.BUTTON_POSITIVE ->{
                    mediaPlayer?.pause()
                    loadSharedPref();
                }
            }
        }
        val mBuilder = AlertDialog.Builder(this).apply {
            setTitle("Tomar: ${note.nodeName}")
            setIcon(R.drawable.ic_action_alert)
            setMessage("Dosagem: ${note.nodeDosagem}")
            setPositiveButton("OK",  removeNoteDialogListener)
        }
        mBuilder.create().show()
    }

    private fun initAlarm( handler: () -> Unit) = GlobalScope.launch {
        while (true) {
            delay(5000)
            handler()
        }
    }
}