package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText edtUsuario, edtClave;
    Button btnLogin, btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = getSharedPreferences("datos", MODE_PRIVATE);
        if (prefs.getBoolean("logueado", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        edtUsuario = findViewById(R.id.edt_usuario);
        edtClave = findViewById(R.id.edt_clave);
        btnLogin = findViewById(R.id.btn_login);
        btnRegistrarse = findViewById(R.id.btn_registrarse);

        btnLogin.setOnClickListener(v -> {
            String correo = edtUsuario.getText().toString().trim();
            String contrasena = edtClave.getText().toString().trim();

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            realizarLogin(correo, contrasena);
        });

        btnRegistrarse.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });
    }

    private void realizarLogin(String correo, String contrasena) {


        String url = "http://192.168.10.23/marvel_proyecto/final.php?action=login";


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("correo", correo);
            jsonBody.put("contrasena", contrasena);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject usuario = response.getJSONObject("usuario");

                            // Guardar en SharedPreferences
                            SharedPreferences.Editor editor = getSharedPreferences("datos", MODE_PRIVATE).edit();
                            editor.putString("usuario", usuario.getString("correo"));
                            editor.putString("nombre", usuario.getString("nombre"));
                            editor.putString("correo", usuario.getString("correo"));
                            editor.putString("fecha_nacimiento", usuario.optString("fecha_nacimiento", ""));
                            editor.putBoolean("logueado", true);
                            editor.apply();

                            Toast.makeText(this, "Bienvenido " + usuario.getString("nombre"), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error de formato en respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
}
