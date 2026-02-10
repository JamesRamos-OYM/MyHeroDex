package com.example.proyectofinal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComicDetailsManager {

    private static final String PUBLIC_KEY = "6a65fc4fc78ff716b65cf680e6eb78c1";
    private static final String PRIVATE_KEY = "ddb4a05b944c359c21acf4dac2ea0ccd57142a38";
    private static final String BASE_URL = "https://gateway.marvel.com/v1/public/comics/";

    private Context context;
    private RequestQueue requestQueue;

    public ComicDetailsManager(Context context) {
        this.context = context;

        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void mostrarDetallesComic(ComicsFragment.Comic comic) {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comic_detalles, null);

        // Referencias a las vistas
        TextView txtTitulo = dialogView.findViewById(R.id.txt_titulo_detalle);
        TextView txtId = dialogView.findViewById(R.id.txt_id_detalle);
        TextView txtDescripcion = dialogView.findViewById(R.id.txt_descripcion_detalle);
        TextView txtFecha = dialogView.findViewById(R.id.txt_fecha_detalle);
        TextView txtPaginas = dialogView.findViewById(R.id.txt_paginas_detalle);
        TextView txtPrecio = dialogView.findViewById(R.id.txt_precio_detalle);
        ImageView imgComic = dialogView.findViewById(R.id.img_comic_detalle);

        // Mostrar información básica inmediatamente
        txtTitulo.setText(comic.getNombre());
        txtId.setText("ID: " + comic.getId());

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Detalles del Comic")
                .setPositiveButton("Cerrar", null)
                .create();

        dialog.show();

        // Cargar detalles adicionales de la API
        cargarDetallesAPI(comic.getId(), txtDescripcion, txtFecha, txtPaginas, txtPrecio, imgComic);
    }

    private void cargarDetallesAPI(int comicId, TextView descripcion, TextView fecha,
                                   TextView paginas, TextView precio, ImageView imagen) {

        // Generar timestamp y hash para la autenticación
        long timestamp = System.currentTimeMillis() / 1000;
        String hash = generarHash(timestamp, PRIVATE_KEY, PUBLIC_KEY);

        // Construir URL de la API
        String url = BASE_URL + comicId +
                "?ts=" + timestamp +
                "&apikey=" + PUBLIC_KEY +
                "&hash=" + hash;

        // Crear y enviar la petición
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        procesarRespuestaAPI(response, descripcion, fecha, paginas, precio, imagen);
                    } catch (JSONException e) {
                        Log.e("ComicDetails", "Error al procesar respuesta: " + e.getMessage());
                        mostrarError(descripcion);
                    }
                },
                error -> {
                    Log.e("ComicDetails", "Error en petición API: " + error.getMessage());
                    mostrarError(descripcion);
                });

        requestQueue.add(request);
    }

    private void procesarRespuestaAPI(JSONObject response, TextView descripcion, TextView fecha,
                                      TextView paginas, TextView precio, ImageView imagen) throws JSONException {

        JSONObject data = response.getJSONObject("data");
        JSONArray results = data.getJSONArray("results");

        if (results.length() > 0) {
            JSONObject comic = results.getJSONObject(0);

            // Descripción
            String desc = comic.optString("description", "");
            if (desc.isEmpty()) {
                descripcion.setText("No hay descripción disponible para este comic.");
            } else {
                descripcion.setText(desc);
            }

            // Páginas
            int pageCount = comic.optInt("pageCount", 0);
            if (pageCount > 0) {
                paginas.setText("Páginas: " + pageCount);
            } else {
                paginas.setText("Páginas: No disponible");
            }

            // Fecha de publicación
            JSONArray dates = comic.optJSONArray("dates");
            if (dates != null && dates.length() > 0) {
                for (int i = 0; i < dates.length(); i++) {
                    JSONObject dateObj = dates.getJSONObject(i);
                    if ("onsaleDate".equals(dateObj.getString("type"))) {
                        String dateStr = dateObj.getString("date");
                        fecha.setText("Fecha: " + formatearFecha(dateStr));
                        break;
                    }
                }
            } else {
                fecha.setText("Fecha: No disponible");
            }

            // Precio
            JSONArray prices = comic.optJSONArray("prices");
            if (prices != null && prices.length() > 0) {
                JSONObject priceObj = prices.getJSONObject(0);
                double priceValue = priceObj.getDouble("price");
                if (priceValue > 0) {
                    precio.setText("Precio: $" + String.format("%.2f", priceValue));
                } else {
                    precio.setText("Precio: No disponible");
                }
            } else {
                precio.setText("Precio: No disponible");
            }

            // Imagen
            JSONObject thumbnail = comic.optJSONObject("thumbnail");
            if (thumbnail != null) {
                String imagePath = thumbnail.getString("path");
                String imageExtension = thumbnail.getString("extension");
                String imageUrl = imagePath + "." + imageExtension;

                // Usar una imagen de mejor calidad
                imageUrl = imageUrl.replace("/standard_medium", "/portrait_uncanny");

                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.color.image_placeholder)
                        .error(R.color.image_placeholder)
                        .into(imagen);
            }
        }
    }

    private void mostrarError(TextView descripcion) {
        descripcion.setText("Error al cargar los detalles del comic. Verifica tu conexión a internet.");
    }

    private String formatearFecha(String fechaISO) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(fechaISO);
            return outputFormat.format(date);
        } catch (Exception e) {
            return fechaISO.substring(0, 10); // Retornar solo la fecha sin la hora
        }
    }

    private String generarHash(long timestamp, String privateKey, String publicKey) {
        try {
            String input = timestamp + privateKey + publicKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("ComicDetails", "Error generando hash: " + e.getMessage());
            return "";
        }
    }
}