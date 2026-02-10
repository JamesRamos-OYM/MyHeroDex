package com.example.proyectofinal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class RegistroActivity extends AppCompatActivity {

    EditText edtNombre, edtCorreo, edtContrasena, edtFecha;
    Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        edtNombre = findViewById(R.id.edt_nombre);
        edtCorreo = findViewById(R.id.edt_correo);
        edtContrasena = findViewById(R.id.edt_contrasena);
        edtFecha = findViewById(R.id.edt_fecha);
        btnRegistrar = findViewById(R.id.btn_registrar);

        edtFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String fecha = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                edtFecha.setText(fecha);
            }, year, month, day);
            datePickerDialog.show();
        });

        btnRegistrar.setOnClickListener(v -> {
            String nombre = edtNombre.getText().toString().trim();
            String correo = edtCorreo.getText().toString().trim();
            String contrasena = edtContrasena.getText().toString().trim();
            String fecha = edtFecha.getText().toString().trim();

            boolean valido = true;

            if (nombre.isEmpty()) {
                edtNombre.setError("Ingrese su nombre");
                valido = false;
            }

            if (correo.isEmpty() || !correo.contains("@")) {
                edtCorreo.setError("Ingrese un correo válido");
                valido = false;
            }

            if (contrasena.isEmpty() || contrasena.length() < 4) {
                edtContrasena.setError("Mínimo 4 caracteres");
                valido = false;
            }

            if (fecha.isEmpty()) {
                edtFecha.setError("Seleccione su fecha de nacimiento");
                valido = false;
            }

            if (valido) {
                realizarRegistro(nombre, correo, contrasena, fecha);
            }
        });
    }

    private void realizarRegistro(String nombre, String correo, String contrasena, String fecha) {

        String url = "http://192.168.10.23/marvel_proyecto/final.php?action=register";


        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("nombre", nombre);
            jsonBody.put("correo", correo);
            jsonBody.put("contrasena", contrasena);
            jsonBody.put("fecha_nacimiento", fecha);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // Volver al login
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
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