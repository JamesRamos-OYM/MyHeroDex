package com.example.proyectofinal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class SolicitarFragment extends Fragment {

    EditText edtBuscar;
    ImageButton btnBuscar;
    TextView txtResultados;
    RecyclerView recyclerView;
    SuperHeroAdapter adapter;
    List<SuperHero> lista = new ArrayList<>();

    public SolicitarFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicitar, container, false);

        // Inicializar vistas
        edtBuscar = view.findViewById(R.id.edt_buscar);
        btnBuscar = view.findViewById(R.id.btn_buscar);
        txtResultados = view.findViewById(R.id.txt_resultados);
        recyclerView = view.findViewById(R.id.recycler_heroes);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SuperHeroAdapter(lista);
        recyclerView.setAdapter(adapter);

        // Listener para búsqueda mientras escribe
        edtBuscar.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) { // Buscar solo si hay al menos 3 caracteres
                    cargarDesdeApi(s.toString());
                } else if (s.length() == 0) {
                    // Limpiar resultados si no hay texto
                    lista.clear();
                    adapter.notifyDataSetChanged();
                    txtResultados.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        // Listener para el botón de búsqueda
        btnBuscar.setOnClickListener(v -> {
            String busqueda = edtBuscar.getText().toString().trim();
            if (!busqueda.isEmpty()) {
                cargarDesdeApi(busqueda);
            } else {
                Toast.makeText(getContext(), "Escribe el nombre de un superhéroe", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void cargarDesdeApi(String filtro) {
        String ts = String.valueOf(System.currentTimeMillis());
        String publicKey = "6a65fc4fc78ff716b65cf680e6eb78c1";
        String privateKey = "ddb4a05b944c359c21acf4dac2ea0ccd57142a38";
        String hash = generateHash(ts, privateKey, publicKey);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelService service = retrofit.create(MarvelService.class);
        Call<JsonObject> call = service.getHeroes(publicKey, ts, hash, filtro);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lista.clear();
                    JsonArray results = response.body()
                            .getAsJsonObject("data")
                            .getAsJsonArray("results");

                    for (JsonElement e : results) {
                        JsonObject obj = e.getAsJsonObject();

                        // Obtener datos básicos
                        String nombre = obj.get("name").getAsString();
                        int id = obj.get("id").getAsInt();

                        // Obtener descripción
                        String descripcion = "";
                        if (obj.has("description") && !obj.get("description").isJsonNull()) {
                            descripcion = obj.get("description").getAsString();
                        }
                        if (descripcion.isEmpty()) {
                            descripcion = "No hay descripción disponible para este superhéroe.";
                        }

                        // Obtener imagen
                        JsonObject thumb = obj.getAsJsonObject("thumbnail");
                        String img = thumb.get("path").getAsString() + "." + thumb.get("extension").getAsString();

                        // Reemplazar http por https para mejor seguridad
                        if (img.startsWith("http:")) {
                            img = img.replace("http:", "https:");
                        }

                        // Crear objeto SuperHero con toda la información
                        lista.add(new SuperHero(nombre, img, descripcion, id));
                    }

                    adapter.notifyDataSetChanged();

                    // Mostrar texto de resultados
                    if (lista.size() > 0) {
                        txtResultados.setText("Encontrados: " + lista.size() + " superhéroes");
                        txtResultados.setVisibility(View.VISIBLE);
                    } else {
                        txtResultados.setText("No se encontraron superhéroes");
                        txtResultados.setVisibility(View.VISIBLE);
                    }

                } else {
                    Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
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
        @GET("v1/public/characters")
        Call<JsonObject> getHeroes(
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("nameStartsWith") String name
        );

        // Nuevo método para obtener detalles específicos
        @GET("v1/public/characters/{characterId}/comics")
        Call<JsonObject> getCharacterComics(
                @retrofit2.http.Path("characterId") int characterId,
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );

        @GET("v1/public/characters/{characterId}/series")
        Call<JsonObject> getCharacterSeries(
                @retrofit2.http.Path("characterId") int characterId,
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );
    }
}