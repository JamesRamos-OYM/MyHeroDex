package com.example.proyectofinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class configFragment extends Fragment {
    TextView txtNombre, txtCorreo, txtFechaNacimiento;
    Button btnCerrarSesion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);

        txtNombre = view.findViewById(R.id.txt_nombre_config);
        txtCorreo = view.findViewById(R.id.txt_correo_config);
        txtFechaNacimiento = view.findViewById(R.id.txt_fecha_config);
        btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion);

        // Cargar datos del usuario logueado
        SharedPreferences prefs = requireActivity().getSharedPreferences("datos", getContext().MODE_PRIVATE);

        String nombre = prefs.getString("nombre", "Usuario");
        String correo = prefs.getString("correo", "");
        String fecha = prefs.getString("fecha_nacimiento", "No disponible");

        txtNombre.setText("Nombre: " + nombre);
        txtCorreo.setText("Correo: " + correo);
        txtFechaNacimiento.setText("Fecha de nacimiento: " + fecha);

        btnCerrarSesion.setOnClickListener(v -> {
            // Limpiar datos de sesión
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("logueado", false);
            editor.remove("usuario");
            editor.remove("nombre");
            editor.remove("correo");
            editor.remove("fecha_nacimiento");
            editor.apply();

            // Ir a login
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}