package com.example.proyectofinal;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class DetalleActivity extends AppCompatActivity {

    private TextView txtNombre;
    private TextView txtDescripcion;
    private ImageView imgHero;
    private TextView txtComics;
    private TextView txtSeries;
    private Button btnVolver;

    private String publicKey = "6a65fc4fc78ff716b65cf680e6eb78c1";
    private String privateKey = "ddb4a05b944c359c21acf4dac2ea0ccd57142a38";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // Inicializar vistas
        txtNombre = findViewById(R.id.txt_nombre_hero_detalle);
        txtDescripcion = findViewById(R.id.txt_descripcion_hero_detalle);
        imgHero = findViewById(R.id.img_hero_detalle);
        txtComics = findViewById(R.id.txt_comics_hero_detalle);
        txtSeries = findViewById(R.id.txt_series_hero_detalle);
        btnVolver = findViewById(R.id.btn_volver);

        // Obtener datos del intent
        String nombre = getIntent().getStringExtra("nombre");
        String imagenUrl = getIntent().getStringExtra("imagen");
        String descripcion = getIntent().getStringExtra("descripcion");
        int heroId = getIntent().getIntExtra("id", 0);

        // Setear datos básicos
        txtNombre.setText(nombre);
        txtDescripcion.setText(descripcion != null && !descripcion.isEmpty() ?
                descripcion : "No hay descripción disponible");

        // Cargar imagen
        Picasso.get()
                .load(imagenUrl)
                .placeholder(R.drawable.ic_hero_placeholder)
                .error(R.drawable.ic_hero_error)
                .into(imgHero);

        // Cargar información adicional si tenemos el ID
        if (heroId > 0) {
            cargarComics(heroId);
            cargarSeries(heroId);
        } else {
            txtComics.setText("No se pudo cargar la información de comics");
            txtSeries.setText("No se pudo cargar la información de series");
        }

        // Listener para botón volver
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarComics(int heroId) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = generateHash(ts, privateKey, publicKey);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelService service = retrofit.create(MarvelService.class);
        Call<JsonObject> call = service.getCharacterComics(heroId, publicKey, ts, hash, 5);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject data = response.body().getAsJsonObject("data");
                        JsonArray results = data.getAsJsonArray("results");

                        StringBuilder comics = new StringBuilder();
                        for (JsonElement element : results) {
                            JsonObject comic = element.getAsJsonObject();
                            String title = comic.get("title").getAsString();
                            comics.append("• ").append(title).append("\n");
                        }

                        if (comics.length() > 0) {
                            txtComics.setText(comics.toString());
                        } else {
                            txtComics.setText("No hay comics disponibles");
                        }

                    } catch (Exception e) {
                        Log.e("DetalleActivity", "Error parsing comics: " + e.getMessage());
                        txtComics.setText("Error al cargar comics");
                    }
                } else {
                    txtComics.setText("No se pudieron cargar los comics");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("DetalleActivity", "Error loading comics: " + t.getMessage());
                txtComics.setText("Error de conexión al cargar comics");
            }
        });
    }

    private void cargarSeries(int heroId) {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = generateHash(ts, privateKey, publicKey);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelService service = retrofit.create(MarvelService.class);
        Call<JsonObject> call = service.getCharacterSeries(heroId, publicKey, ts, hash, 5);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonObject data = response.body().getAsJsonObject("data");
                        JsonArray results = data.getAsJsonArray("results");

                        StringBuilder series = new StringBuilder();
                        for (JsonElement element : results) {
                            JsonObject serie = element.getAsJsonObject();
                            String title = serie.get("title").getAsString();
                            series.append("• ").append(title).append("\n");
                        }

                        if (series.length() > 0) {
                            txtSeries.setText(series.toString());
                        } else {
                            txtSeries.setText("No hay series disponibles");
                        }

                    } catch (Exception e) {
                        Log.e("DetalleActivity", "Error parsing series: " + e.getMessage());
                        txtSeries.setText("Error al cargar series");
                    }
                } else {
                    txtSeries.setText("No se pudieron cargar las series");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("DetalleActivity", "Error loading series: " + t.getMessage());
                txtSeries.setText("Error de conexión al cargar series");
            }
        });
    }

    private String generateHash(String ts, String privateKey, String publicKey) {
        try {
            String value = ts + privateKey + publicKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(value.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    interface MarvelService {
        @GET("v1/public/characters/{characterId}/comics")
        Call<JsonObject> getCharacterComics(
                @Path("characterId") int characterId,
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );

        @GET("v1/public/characters/{characterId}/series")
        Call<JsonObject> getCharacterSeries(
                @Path("characterId") int characterId,
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );
    }
}
