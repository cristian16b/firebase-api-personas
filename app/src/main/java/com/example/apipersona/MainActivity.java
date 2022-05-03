package com.example.apipersona;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageButton botonBuscarTarjeta;
    private EditText dniEdit;
    private TextView numero;
    private TextView nombre_completo;
    private TextView nombres;
    private TextView apellido_paterno;
    private TextView apellido_materno;
    private TextView sexo;
    private TextView estado_civil;
    private TextView departamento;
    private TextView provincia;
    private TextView distrito;
    private TextView direccion;
    private TextView direccion_completa;
    private TextView ubigeo_reniec;
    private TextView ubigeo_sunat;
    private TextView ubigeo;
    private TextView codigo_verificacion;
    private TextView fecha_nacimiento;

    String imei;

    static final Integer PHONESTATS = 0x1;
    private final String TAG=MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://blog.nubecolectiva.com/como-obtener-el-imei-de-un-movil-en-android-studio/
        consultarPermiso(Manifest.permission.READ_PHONE_STATE, PHONESTATS);

        //Toast.makeText(this, "IMEI" +this.imei,Toast.LENGTH_LONG).show();

        //Log.i("IMEI",imei);

        //COMENTAR ESTAS VARIABLES DEL IMEI
        // IMEI NO EXISTENTE
        //imei = "111";
        //IMEI EXISTENTE
        imei = "2222";

        DatabaseReference dbCielo =
                FirebaseDatabase.getInstance().getReference()
                //        .child("prediccion-hoy")
                //        .child("cielo")
                        .child("usuarios")
                        .child(imei)
                ;

        dbCielo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null){
                    String valor = dataSnapshot.getValue().toString();
                    Log.i("IMEI",valor);
                } else {
                    Log.i("IMEI","cierro");
                    //cierro
                    finish();
                    finishAffinity();
                    System.exit(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("j", "Error!", databaseError.toException());
            }
        });


        botonBuscarTarjeta = (ImageButton) findViewById(R.id.botonBuscar);
        dniEdit = (EditText) findViewById(R.id.dniText);

        numero = (TextView) findViewById(R.id.numero);
        nombre_completo = (TextView) findViewById(R.id.nombre_completo);
        nombres = (TextView) findViewById(R.id.nombres);
        apellido_paterno = (TextView) findViewById(R.id.apellido_paterno);
        apellido_materno = (TextView) findViewById(R.id.apellido_materno);
        sexo = (TextView) findViewById(R.id.sexo);
        estado_civil =  (TextView) findViewById(R.id.estado_civil);
        departamento =  (TextView) findViewById(R.id.departamento);
        provincia = (TextView) findViewById(R.id.provincia);
        distrito = (TextView) findViewById(R.id.distrito);
        direccion = (TextView) findViewById(R.id.direccion);
        direccion_completa = (TextView) findViewById(R.id.direccion_completa);
        ubigeo_reniec= (TextView) findViewById(R.id.ubigeo_reniec);
        ubigeo_sunat = (TextView) findViewById(R.id.ubigeo_sunat);
        ubigeo = (TextView) findViewById(R.id.ubigeo);
        codigo_verificacion = (TextView) findViewById(R.id.codigo_verificacion);
        fecha_nacimiento = (TextView) findViewById(R.id.fecha_nacimiento);
    }

    // Con este método mostramos en un Toast con un mensaje que el usuario ha concedido los permisos a la aplicación
    private void consultarPermiso(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            imei = obtenerIMEI();
            Toast.makeText(this,permission + " El permiso a la aplicación esta concedido.", Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerIMEI() {
        final TelephonyManager telephonyManager= (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Hacemos la validación de métodos, ya que el método getDeviceId() ya no se admite para android Oreo en adelante, debemos usar el método getImei()
            return telephonyManager.getImei();
        }
        else {
            return telephonyManager.getDeviceId();
        }

    }

    public void buscarDatosTarjeta(View view) throws IOException {
        String numeroTarjetaTexto=this.dniEdit.getText().toString();

        if(numeroTarjetaTexto.length()== 0){
            Toast.makeText(this,"Debe ingresar el DNI",Toast.LENGTH_LONG).show();
            return;
        }
        else
        {
            try
            {
                Toast.makeText(this,"Buscando...",Toast.LENGTH_SHORT).show();
                blanquearDatosTarjeta();
                postHttpResponse(numeroTarjetaTexto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void postHttpResponse(String numeroIngresado) throws IOException {
        Log.i("CALL", numeroIngresado);
        String urlBase = "https://apiperu.dev/api/dni/";
        String urlSolicitud = urlBase + numeroIngresado + "?api_token=b5add658c50ac44fe9ff612016bf2f9102dc0fd8a3960ee14c4f9ee382847db8&fbclid=IwAR0EZPTSDvTf5LT4G4GxoZEWv48Z5TdGLC0YcdI3VQw6fXiNYeioGKEL30g";
        Request request = new Request.Builder()
                .url(urlSolicitud)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.i("ONFAILURE","");
                mostrarErrores("Fallo al conectarse");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.i("CALL", "llama");
                try
                {
                    //Log.i("codigoResp", String.valueOf(response.code()));

                    // Si la api retorna un 200
                    if(response.isSuccessful()){
                        String responseData = Objects.requireNonNull(response.body()).string();

                        JSONObject jsonObject = new JSONObject(responseData);
                        Log.i("ONRESPONSE",jsonObject.toString());
                        Log.i("CALL", jsonObject.toString());

                        mostrarDatosTarjeta(jsonObject);
                    }
                    else
                    {
                        switch(response.code()){
                            case 401: //unauthorized
                                mostrarErrores("Fallo al conectarse");
                                break;
                            case 403: //unauthorized
                                mostrarErrores("Fallo al conectarse");
                                break;
                            default:
                                mostrarErrores("Fallo al conectarse");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // muestro un mensaje de error que se le pasa como parametro.
    private void mostrarErrores(final String errorMensaje)
    {
        // Run view-related code back on the main thread

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),errorMensaje,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarDatosTarjeta(JSONObject jsonObject){
        try
        {
            //Log.i("CALL", String.valueOf(jsonObject));
            //Log.i("resultado", String.valueOf(jsonObject));
            /*
            final String nombreTarj = jsonObject.getString("scheme").toUpperCase();
            final String tipoTarj = jsonObject.getString("type").toUpperCase();

            JSONObject pais = jsonObject.getJSONObject("country");
            final String paisNombre = pais.getString("name").toUpperCase();
            final String paisMoneda = pais.getString("currency").toUpperCase();
             */
            /*
            Log.i("resultado",jsonObject.getString("type"));
            Log.i("resultado",pais.getString("name"));
            Log.i("resultado",pais.getString("currency"));
             */

            /*
            JSONObject banco = jsonObject.getJSONObject("bank");
            final String bancoNombre = banco.getString("name").toUpperCase();
            */
            // Log.i("resultado",banco.getString("name"));




            String status =  jsonObject.getString("success");
            //Log.i("CALL", "estatus"+status.equals("true"));
            //Toast.makeText(this,status,Toast.LENGTH_LONG).show();
           // Toast.makeText(this,status.equals("true")+"",Toast.LENGTH_LONG).show();
            //Log.i("CALL", data.toString());

            if(status.equals("true")){
                Log.i("CALL", "Entra en el true");
                JSONObject data =  jsonObject.getJSONObject("data");
                final String t_numero = data.getString("numero");
                final String t_nombre_completo = data.getString("nombre_completo");
                final String t_nombres = data.getString("nombres");
                final String t_apellido_paterno = data.getString("apellido_paterno");
                final String t_apellido_materno = data.getString("apellido_materno");
                final String t_codigo_verificacion = data.getString("codigo_verificacion");
                final String t_fecha_nacimiento = data.getString("fecha_nacimiento");
                final String t_sexo = data.getString("sexo");
                final String t_estado_civil = data.getString("estado_civil");
                final String t_departamento = data.getString("departamento");
                final String t_provincia = data.getString("provincia");
                final String t_distrito = data.getString("distrito");
                final String t_direccion = data.getString("direccion");
                final String t_direccion_completa = data.getString("direccion_completa");
                final String t_ubigeo_reniec = data.getString("ubigeo_reniec");
                final String t_ubigeo_sunat = data.getString("ubigeo_sunat");
               final String t_ubigeo = data.getString("ubigeo") + "";

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        numero.setText(t_numero);
                        nombre_completo.setText(t_nombre_completo);
                        nombres.setText(t_nombres);
                        apellido_paterno.setText(t_apellido_paterno);
                        apellido_materno.setText(t_apellido_materno);
                        codigo_verificacion.setText(t_codigo_verificacion);
                        fecha_nacimiento.setText(t_fecha_nacimiento);
                        sexo.setText(t_sexo);
                        estado_civil.setText(t_estado_civil);
                        departamento.setText(t_departamento);
                        provincia.setText(t_provincia);
                        departamento.setText(t_departamento);
                        provincia.setText(t_provincia);
                        distrito.setText(t_distrito);
                        direccion.setText(t_direccion);
                        direccion_completa.setText(t_direccion_completa);
                        ubigeo_reniec.setText(t_ubigeo_reniec);
                        ubigeo_sunat.setText(t_ubigeo_sunat);
                        ubigeo.setText(t_ubigeo);
                        Log.i("CALL", "FIN DEL SETEO");
                    }
                });
            }
            else {
                String mensaje = (String) jsonObject.getString("message");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void blanquearDatosTarjeta(){

        nombre_completo.setText("");
        numero.setText("");
        apellido_materno.setText("");
        nombres.setText("");
        apellido_paterno.setText("");
        apellido_materno.setText("");
        codigo_verificacion.setText("");
        fecha_nacimiento.setText("");
        sexo.setText("");
        estado_civil.setText("");
        departamento.setText("");
        provincia.setText("");
        distrito.setText("");
        direccion.setText("");
        direccion_completa.setText("");
        ubigeo_reniec.setText("");
        ubigeo_sunat.setText("");
        ubigeo.setText("");
    }

}