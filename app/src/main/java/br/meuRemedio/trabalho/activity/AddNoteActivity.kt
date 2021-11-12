package br.meuRemedio.trabalho.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import br.meuRemedio.trabalho.R
import br.meuRemedio.trabalho.R.id.Remedio
import br.meuRemedio.trabalho.database.DBManager
import br.meuRemedio.trabalho.databinding.ActivityAddBinding
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {

    private var id = 0
    private lateinit var binding: ActivityAddBinding

    @SuppressLint("SimpleDateFormat")
    var formatter: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_add)
        findViewById<TimePicker>(R.id.timePicker1).setIs24HourView(true);
        findViewById<NumberPicker>(R.id.number_picker).minValue = 0;
        findViewById<NumberPicker>(R.id.number_picker).maxValue = 100;
        findViewById<NumberPicker>(R.id.number_picker).value = 100;
        findViewById<CheckBox>(R.id.checkBox).setOnCheckedChangeListener { _, isChecked ->
            findViewById<EditText>(R.id.Dias).isEnabled = !isChecked
            findViewById<TextView>(R.id.Dias).text = ""
        }

        findViewById<Button>(R.id.buttonSalvar)
            .setOnClickListener(editNoteListener)

        try {
            if (intent.extras != null) {
                supportActionBar!!.title = "Atualizar Remédio"
                id = intent.getIntExtra("ID", 0)
                findViewById<TextView>(Remedio).text = intent.getStringExtra("NAME")

                if(intent.getStringExtra("DIAS") == "INDETERMINADO"){
                    findViewById<CheckBox>(R.id.checkBox).isChecked = true;
                }else{
                    findViewById<TextView>(R.id.Dias).text = intent.getStringExtra("DIAS")
                }

                findViewById<TimePicker>(R.id.timePicker1).hour = intent.getStringExtra("DATAHORA")!!
                    .split(":")[0].toInt()
                findViewById<TimePicker>(R.id.timePicker1).minute = intent.getStringExtra("DATAHORA")!!
                    .split(":")[1].toInt()
                findViewById<TextView>(R.id.dosagem).text = intent.getStringExtra("DOSAGEM")
                intent.getStringExtra("VOLUME")?.let {
                    findViewById<NumberPicker>(R.id.number_picker).value = it.toInt()
                }

                findViewById<Button>(R.id.buttonSalvar).text = "Atualizar"
            } else {
                supportActionBar!!.title = "Adicionar Remédio"
                findViewById<Button>(R.id.buttonSalvar).text = "Adicionar"
                findViewById<Button>(R.id.buttonSalvar)
                    .setOnClickListener(addNoteListener)
            }
        } catch (e: RuntimeException) {
        }
    }

    /**Columns**/
    private val addNoteListener = View.OnClickListener {
        val dbManager = DBManager(this)
        val values = ContentValues()
        values.put("NAME", findViewById<TextView>(Remedio).text.toString())

        if(findViewById<CheckBox>(R.id.checkBox).isChecked){
            values.put("DIAS", "INDETERMINADO")
        }else{
            values.put("DIAS", findViewById<TextView>(R.id.Dias).text.toString())
        }

        values.put("DATAHORA", findViewById<TimePicker>(R.id.timePicker1).hour.toString() + ":" + findViewById<TimePicker>(R.id.timePicker1).minute.toString())
        values.put("DOSAGEM", findViewById<TextView>(R.id.dosagem).text.toString())
        values.put("VOLUME", findViewById<NumberPicker>(R.id.number_picker).value.toString())
        values.put("PERIODICIDADE", "")

        try {
            dbManager.insert(values)
            finish()
            Toast.makeText(this, "Remédio adicionado com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Problemas ao adicionar remédio!", Toast.LENGTH_SHORT).show()
        }
    }

    private val editNoteListener = View.OnClickListener {
        val dbManager = DBManager(this)
        val values = ContentValues()
        values.put("NAME", findViewById<TextView>(Remedio).text.toString())

        if(findViewById<CheckBox>(R.id.checkBox).isChecked){
            values.put("DIAS", "INDETERMINADO")
        }else{
            values.put("DIAS", findViewById<TextView>(R.id.Dias).text.toString())
        }

        values.put("DATAHORA", findViewById<TimePicker>(R.id.timePicker1).hour.toString() + ":" + findViewById<TimePicker>(R.id.timePicker1).minute.toString())
        values.put("DOSAGEM", findViewById<TextView>(R.id.dosagem).text.toString())
        values.put("VOLUME", findViewById<NumberPicker>(R.id.number_picker).value.toString())
        try {
            val selectionArgs = arrayOf(id.toString())
            dbManager.update(values, "ID = ?", selectionArgs)
            finish()
            Toast.makeText(this, "Remédio Atualizado!", Toast.LENGTH_SHORT).show()
        } catch (e: RuntimeException) {
            Toast.makeText(this, "Problemas ao adicionar remédio!", Toast.LENGTH_SHORT).show()
        }
    }
}