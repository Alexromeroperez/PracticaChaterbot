package com.arp.practicachaterbot;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arp.practicachaterbot.chatterbotapi.ChatterBot;
import com.arp.practicachaterbot.chatterbotapi.ChatterBotFactory;
import com.arp.practicachaterbot.chatterbotapi.ChatterBotSession;
import com.arp.practicachaterbot.chatterbotapi.ChatterBotType;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private ChatterBotFactory factory;
    private ChatterBot bot1;
    private ChatterBotSession bot1session;
    private TextToSpeech tts;
    private LinearLayout ly;
    private String respuesta="";
    private final int CT1=1,CT2=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        ly=(LinearLayout)findViewById(R.id.linear);
        /*Se lanza si no tenemos el tts instalado*/
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CT1);

        /*Boton flotante que lanza la actividad para hablar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora");
                i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
                startActivityForResult(i, CT2);
            }
        });

        /*Crea la conexion con Chaterbot*/
        factory = new ChatterBotFactory();
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.getDefault());
        } else {
            Toast.makeText(this, "No puede reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CT1) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }else if(requestCode ==CT2){
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            userTexto(getCurrentFocus(),textos.get(0));
            Hilo h=new Hilo();
            h.execute(textos.get(0));
        }
    }

    /****************Paramos el recurso******************************/
    @Override
    protected void onStop() {
        super.onStop();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /*************Hilo que manda el texto y lo escribe ademas de leer el resultado****************/
    class Hilo extends AsyncTask<String, Void,String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                bot1session = bot1.createSession();
                respuesta=bot1session.think(params[0])+"\n";
            } catch (Exception e) {
            }
            return respuesta;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tts.speak(s,TextToSpeech.QUEUE_FLUSH, null);
            botTexto(getCurrentFocus(), s);
        }
    }

    /*******Añade un textview cuando hablas y cuando responde el bot *************/
    public void botTexto(View v,String texto){
        TextView tv=new TextView(this);
        tv.setText(texto);
        tv.setBackgroundColor(Color.GRAY);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ly.addView(tv, params);
    }

    public void userTexto(View v,String texto){
        TextView tv=new TextView(this);
        tv.setText(texto);
        tv.setGravity(Gravity.END);
        tv.setBackgroundColor(Color.GREEN);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ly.addView(tv, params);
    }
}
