package com.jedyny_cywilizowany.morseowa_latarka

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        txtInput=findViewById(R.id.textInput)
        activateButton=findViewById(R.id.button)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager!!.cameraIdList[0]

        val spinner=findViewById<Spinner>(R.id.spinner)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ){
                if ((running?.isActive)!=true) {
                    spinnerLastIndex = position
                    if (position>0) send(extraCommands[position])
                }
                else spinner.setSelection(spinnerLastIndex)
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        fun codeChar(lc:Char,uc:Char?,str:String)
        {
            val c = abToCode(str)
            coding[lc - Char.MIN_VALUE] = c
            if (uc!=null) coding[uc - Char.MIN_VALUE] = c
        }
        fun codeChar(lc:Char,str:String) {
            codeChar(lc, null, str)
        }
        codeChar('a','A',"ab")
        codeChar('b','B',"baaa")
        codeChar('c','C',"baba")
        codeChar('d','D',"baa")
        codeChar('e','E',"a")
        codeChar('f','F',"aaba")
        codeChar('g','G',"bba")
        codeChar('h','H',"aaaa")
        codeChar('i','I',"aa")
        codeChar('j','J',"abbb")
        codeChar('k','K',"bab")
        codeChar('l','L',"abaa")
        codeChar('m','M',"bb")
        codeChar('n','N',"ba")
        codeChar('o','O',"bbb")
        codeChar('p','P',"abba")
        codeChar('q','Q',"bbab")
        codeChar('r','R',"aba")
        codeChar('s','S',"aaa")
        codeChar('t','T',"b")
        codeChar('u','U',"aab")
        codeChar('v','V',"aaab")
        codeChar('w','W',"abb")
        codeChar('x','X',"baab")
        codeChar('y','Y',"babb")
        codeChar('z','Z',"bbaa")
        codeChar('ą','Ą',"abab")
        codeChar('ć','Ć',"babaa")
        codeChar('ę','Ę',"aabaa")
        codeChar('ł','Ł',"abaab")
        codeChar('ń','Ń',"bbabb")
        codeChar('ó','Ó',"bbba")
        codeChar('ś','Ś',"aaabaaa")
        codeChar('ż','Ż',"bbaaba")
        codeChar('ź','Ź',"bbaab")
        codeChar('0',"bbbbb")
        codeChar('1',"abbbb")
        codeChar('2',"aabbb")
        codeChar('3',"aaabb")
        codeChar('4',"aaaab")
        codeChar('5',"aaaaa")
        codeChar('6',"baaaa")
        codeChar('7',"bbaaa")
        codeChar('8',"bbbaa")
        codeChar('9',"bbbba")
        codeChar('.',"ababab")
        codeChar(',',"bbaabb")
        codeChar('\'',"abbbba")
        codeChar('\"',"abaaba")
        codeChar('_',"aabbab")
        codeChar(':',"bbbaaa")
        codeChar(';',"bababa")
        codeChar('?',"aabbaa")
        codeChar('!',"bababb")
        codeChar('-',"baaaab")
        codeChar('+',"ababa")
        codeChar('/','\\',"baaba")
        codeChar('(',"babba")
        codeChar(')',"babbab")
        codeChar('=',"baaab")
        codeChar('@',"abbaba")
        coding[' ' - Char.MIN_VALUE] = Array(4){false}
    }

    override fun onDestroy() {
        super.onDestroy()
        try
        {
            cameraManager?.setTorchMode(cameraId, false)
        }
        catch (e: Exception)
        {
        }
    }

    private val coding = Array<Array<Boolean>>(Char.MAX_VALUE-Char.MIN_VALUE){Array(0){false}}
    private var running: Job? = null
    private var txtInput: TextView? = null
    private var activateButton: Button? = null
    private var cameraManager: CameraManager? = null
    private var cameraId = ""
    private val startCode = abToCode("babab")
    private val stopCode =  abToCode("ababa")
    private val extraCommands = arrayOf(
        Array(0){false},
        abToCode("aaabbbaaa"),
        abToCode("aabbaa"),
        abToCode("aaaba"),
        abToCode("aaabaaabaaab"),
        abToCode("aaabab"),
        abToCode("bab"),
        abToCode("abaaa"),
        abToCode("abaab")
        )
    private var spinnerLastIndex: Int = 0
    fun buttonPress(v:View)
    {
        if ((running?.isActive)==true)
        {
            running?.cancel()
            activateButton!!.text="OK"
            findViewById<Spinner>(R.id.spinner).setSelection(0)
            cameraManager?.setTorchMode(cameraId, false)
        }
        else {
            val text = txtInput!!.text
            send(parseString(text.toString()))
        }
    }
    private fun send(code:Array<Boolean>)
    {
        activateButton!!.text="STOP"
        val context = this
        running = MainScope().launch {
            try
            {
                cameraManager?.setTorchMode(cameraId, false)
                delay(500)
                var lastSign=false
                for (sign in code) {
                    if (sign!=lastSign) {
                        cameraManager?.setTorchMode(cameraId, sign)
                        lastSign = sign
                    }
                    delay(200)
                }
                cameraManager?.setTorchMode(cameraId, false)
            }
            catch (e: CameraAccessException)
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Błąd")
                builder.setMessage(e.localizedMessage)
                    .setPositiveButton("Start") { _, _ ->}
                builder.create().show()
            }
            activateButton!!.text="OK"
            findViewById<Spinner>(R.id.spinner).setSelection(0)
        }
    }
    private fun parseString(str:String): Array<Boolean>
    {
        var c = List<Boolean>(0){false}
        for (ch in " "+str+" ") {
            val cc = coding[ch - Char.MIN_VALUE]
            c = c + cc
            if (cc.isNotEmpty()) c = c + Array<Boolean>(3){false}
        }
        return startCode + c.toTypedArray() + stopCode
    }
    private fun abToCode(str: String): Array<Boolean>
    {
        var c = List<Boolean>(0){false}
        for (ch in str) {
            if (ch=='a') c = c + true
            else if (ch=='b') c = c + Array<Boolean>(3){true}
            c = c + false
        }
        return c.dropLast(1).toTypedArray()
    }
}