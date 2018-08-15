package com.example.ismael.downloadfilesweb;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadFilesWeb extends AppCompatActivity {

    private DownloadManager dManager;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        TextView direccion = (TextView) findViewById(R.id.direccion);
        outState.putString("direccion", direccion.getText().toString());
        TextView extension = (TextView) findViewById(R.id.extension);
        outState.putString("extension", extension.getText().toString());
        TextView contiene = (TextView) findViewById(R.id.filtro);
        outState.putString("contiene", contiene.getText().toString());
        Log.d("Salvado estado: ", "contiene=" + contiene.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        TextView direccion = (TextView) findViewById(R.id.direccion);
        if (savedInstanceState.getString("direccion") != null)
            direccion.setText(savedInstanceState.getString("direccion"));
        TextView extension = (TextView) findViewById(R.id.extension);
        if (savedInstanceState.getString("extension") != null)
            extension.setText(savedInstanceState.getString("extension"));
        TextView contiene = (TextView) findViewById(R.id.filtro);
        if (savedInstanceState.getString("contiene") != null)
            contiene.setText(savedInstanceState.getString("contiene"));
        Log.d("Restaurando estado: ", "contiene=" + savedInstanceState.getString("contiene"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_files_web);

        if (savedInstanceState != null) {
            TextView direccion = (TextView) findViewById(R.id.direccion);
            if (savedInstanceState.getString("direccion") != null)
                direccion.setText(savedInstanceState.getString("direccion"));
            TextView extension = (TextView) findViewById(R.id.extension);
            if (savedInstanceState.getString("extension") != null)
                extension.setText(savedInstanceState.getString("extension"));
            TextView contiene = (TextView) findViewById(R.id.filtro);
            if (savedInstanceState.getString("contiene") != null)
                contiene.setText(savedInstanceState.getString("contiene"));
            Log.d("Recuperando estado: ", "contiene=" + savedInstanceState.getString("contiene"));
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            ((TextView)findViewById(R.id.direccion)).setText(sharedPreferences.getString("direccion",getString(R.string.direccionDefecto)));
            ((TextView)findViewById(R.id.extension)).setText(sharedPreferences.getString("extension",getString(R.string.extDefecto)));
            ((TextView)findViewById(R.id.filtro)).setText(sharedPreferences.getString("filtro",getString(R.string.filtroDefecto)));
            Log.d("Recuperando estado: ", "Nulo");
        }
        dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        final TextView nombreRed = (TextView) findViewById(R.id.nombreRed);
        nombreRed.setText(getCurrentSsid(getBaseContext()));

        Button btnConsultar = (Button) findViewById(R.id.consultar);
        OnClickListener listenerConsultar = new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView nombreRed = (TextView) findViewById(R.id.nombreRed);
                nombreRed.setText(getCurrentSsid(getBaseContext()));
                if(!nombreRed.getText().equals(getString(R.string.noDatos))){
                    savePreferences("direccion", ((TextView) findViewById(R.id.direccion)).getText().toString());
                    savePreferences("extension", ((TextView)findViewById(R.id.extension)).getText().toString());
                    Button consultar = (Button) findViewById(R.id.consultar);consultar.setEnabled(false);
                    Button descargar = (Button) findViewById(R.id.descargar);descargar.setEnabled(false);
                    Button seleccionar = (Button) findViewById(R.id.filtrar);seleccionar.setEnabled(false);
                    Button invertir = (Button) findViewById(R.id.invertir);invertir.setEnabled(false);
                    ArrayAdapter<String> adaptador;
                    adaptador = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_multiple_choice, new ArrayList<String>());
                    ListView lista = (ListView) findViewById(R.id.lista);lista.setAdapter(adaptador);
                    lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    Toast.makeText(getBaseContext(), getString(R.string.consultandoPagina), Toast.LENGTH_SHORT).show();
                    EditText direccion = (EditText) findViewById(R.id.direccion);
                    EditText extension = (EditText) findViewById(R.id.extension);
                    TareaPagina tareaPagina = new TareaPagina();
                    tareaPagina.execute(direccion.getText().toString(), extension.getText().toString());
                } else {
                    Toast.makeText(getBaseContext(),getString(R.string.noDatos), Toast.LENGTH_SHORT).show();
                }
            }
        };
        btnConsultar.setOnClickListener(listenerConsultar);

        Button btnInvertir = (Button) findViewById(R.id.invertir);
        OnClickListener listenerInvertir = new OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView lista = (ListView) findViewById(R.id.lista);
                lista.setEnabled(false);
                int seleccionados = 0;
                for (int i=0;i<lista.getCount();i++){
                    lista.setItemChecked(i,!lista.isItemChecked(i));
                    if (lista.isItemChecked(i)) seleccionados++;
                }
                lista.setEnabled(true);
                Toast.makeText(getBaseContext(), seleccionados+" "+getString(R.string.seleccionados), Toast.LENGTH_SHORT).show();
            }
        };
        btnInvertir.setOnClickListener(listenerInvertir);

        Button btnSeleccionar = (Button) findViewById(R.id.filtrar);
        OnClickListener listenerSeleccionar = new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText contiene = (EditText) findViewById(R.id.filtro);
                savePreferences("filtro", contiene.getText().toString());
                contiene.setEnabled(false);
                ListView lista = (ListView) findViewById(R.id.lista);
                lista.setEnabled(false);
                int seleccionados = 0;
                for (int i=0;i<lista.getCount();i++){
                    if (lista.getItemAtPosition(i).toString().contains(contiene.getText().toString())) lista.setItemChecked(i, true);
                    if (lista.isItemChecked(i)) seleccionados++;
                }
                lista.setEnabled(true);
                contiene.setEnabled(true);
                Toast.makeText(getBaseContext(), seleccionados+" "+getString(R.string.seleccionados), Toast.LENGTH_SHORT).show();
            }
        };
        btnSeleccionar.setOnClickListener(listenerSeleccionar);

        Button btnDescargar = (Button) findViewById(R.id.descargar);
        OnClickListener listenerDescargar = new OnClickListener() {
            @Override
            public void onClick(View v) {
                descargaSeleccionados();
                Button descargar = (Button) findViewById(R.id.descargar);descargar.setEnabled(false);
                descargar.setEnabled(true);
            }
        };
        btnDescargar.setOnClickListener(listenerDescargar);

    }
    private void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor cursor = dManager.query(q);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Log.d("Error: ",cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)) +" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)) +" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                    //Toast.makeText(getBaseContext(),cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))+" "+ "descargado correctamente en"+" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)), Toast.LENGTH_SHORT).show();
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Log.d("Error: ",cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)) +" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)) +" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                    Toast.makeText(getBaseContext(), getString(R.string.error)+": ("+cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)) +" "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)) +") "+cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)), Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    protected void onResume(){
        super.onResume();
        IntentFilter intentFilter= new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }
    public void descargaSeleccionados(){
        String urlString;
        ListView lista = (ListView) findViewById(R.id.lista);
        String direccion = ((EditText)findViewById(R.id.direccion)).getText().toString();
        Toast.makeText(getBaseContext(), getString(R.string.descargando) +" "+ lista.getCheckedItemCount()+ " "+getString(R.string.archivosTrjExt)+" "+Environment.getExternalStorageState()+")", Toast.LENGTH_SHORT).show();
        for (int i=0;i<lista.getCount();i++){
            if (lista.isItemChecked(i)) {
                Log.d("url",lista.getItemAtPosition(i).toString().substring(0,5));
                if (lista.getItemAtPosition(i).toString().substring(0,5).equals("http:"))
                    urlString = lista.getItemAtPosition(i).toString();
                else
                    urlString = direccion +"/"+ lista.getItemAtPosition(i).toString();
                if (!urlString.equals("")) {
                    String fileName="";
                    try {
                        fileName = urlString.substring(urlString.lastIndexOf("/") + 1);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse((urlString)));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDescription(getString(R.string.descargando) + fileName + " " + urlString);
                        request.setTitle(fileName);
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            Log.d("Externa ", Environment.getExternalStorageState() +
                                    " " + Environment.getExternalStorageDirectory());
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        } else {
                            Log.d("No Externa ", Environment.getExternalStorageState() +
                                    " " + Environment.getExternalStorageDirectory());
                            request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,fileName);
                        }
                        Log.d("Encolando: ", fileName);
                        dManager.enqueue(request);
                        Log.d("Encolado: ",fileName);
                    } catch (Exception e) {
                        Toast.makeText(getBaseContext(), getString(R.string.error)+" " +fileName+": "+ e.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("Almacenamiento ", Environment.getRootDirectory().toString());
                        Log.d("Encolado Error: ", e.toString());
                        e.printStackTrace();
                    }

                }
            }
        }
    }
    class TareaPagina extends AsyncTask<String,Void,ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... url) {
            ArrayList<String> elementos=null;
            //Log.d("doInBackground :", url[0] + " , " + url[1]);
            try{
                String textoHtml;
                textoHtml = downloadUrl(url[0]);
                if (textoHtml != null && !textoHtml.isEmpty()) {
                    elementos = new ArrayList<>();
                    String extension = (url[1].isEmpty())?"[A-Za-z0-9áéíóúñÁÉÍÓÚÑ_?&=-]+":url[1];
                    Pattern pattern = Pattern.compile("(?i)([A-Za-z0-9áéíóúñÁÉÍÓÚÑ/_?&.:=-]+[.]"+extension+")",Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(textoHtml);
                    Log.d("Grupos:", String.valueOf(matcher.groupCount()));
                    while (matcher.find()) if (!elementos.contains(matcher.group(1))) elementos.add(matcher.group(1));
                    Log.d("Elementos:", String.valueOf(elementos.size()));
                }
            }catch(Exception e){
                Log.d("Coincidencias...",e.toString());
            }
            return elementos;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result==null) {
                Button descargar = (Button) findViewById(R.id.descargar);descargar.setEnabled(false);
                Button seleccionar = (Button) findViewById(R.id.filtrar);seleccionar.setEnabled(false);
                Button invertir = (Button) findViewById(R.id.invertir);invertir.setEnabled(false);
                Toast.makeText(getBaseContext(),getString(R.string.errorConsultar), Toast.LENGTH_SHORT).show();
            } else {
                if (result.size()>0) {
                    ArrayAdapter<String> adaptador;
                    adaptador = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_multiple_choice, result);
                    ListView lista = (ListView) findViewById(R.id.lista);lista.setAdapter(adaptador);
                    lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    Button seleccionar = (Button) findViewById(R.id.filtrar);seleccionar.setEnabled(true);
                    Button invertir = (Button) findViewById(R.id.invertir);invertir.setEnabled(true);
                    Button descargar = (Button) findViewById(R.id.descargar);descargar.setEnabled(true);
                } else {
                    Button descargar = (Button) findViewById(R.id.descargar);descargar.setEnabled(false);
                    Button seleccionar = (Button) findViewById(R.id.filtrar);seleccionar.setEnabled(false);
                    Button invertir = (Button) findViewById(R.id.invertir);invertir.setEnabled(false);
                }
                Toast.makeText(getBaseContext(), result.size() +" "+ getString(R.string.coincidenciasEncontradas), Toast.LENGTH_LONG).show();
            }
            Button consultar = (Button) findViewById(R.id.consultar);consultar.setEnabled(true);
        }
    }
    private String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        //NetworkInfo networkInfo = connManager.getNetworkInfo(connManager.getActiveNetwork());
        if (networkInfo !=null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            if (networkInfo.getType()==android.net.ConnectivityManager.TYPE_WIFI) {
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                    ssid = getString(R.string.wifi)+" ("+connectionInfo.getSSID()+")";
                }
            } else {ssid=getString(R.string.redMovil);}
        } else {ssid=getString(R.string.noDatos);}
        return ssid;
    }
    private String downloadUrl(String strUrl) throws IOException{
        String textoHtml;
        try{
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //responseCode = urlConnection.getResponseCode();
            urlConnection.connect();
            textoHtml = readStream(urlConnection.getInputStream());
        }catch(Exception e){
            Log.d("Descarga direccion", e.toString());
            textoHtml = null;
        }//finally{}
        //Log.w("Descargado "+((textoHtml==null)?"0":textoHtml.length()),(textoHtml==null)?"Nulo":textoHtml);
        return textoHtml;
    }
    private String readStream(InputStream entityResponse) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = entityResponse.read(buffer)) != -1) {
            //Log.d("InputStream write :", String.valueOf(length));
            baos.write(buffer, 0, length);
        }
        //Log.d("InputStream lenght:", String.valueOf(baos.toString("UTF-8").length()));
        return baos.toString("UTF-8");
    }
}
