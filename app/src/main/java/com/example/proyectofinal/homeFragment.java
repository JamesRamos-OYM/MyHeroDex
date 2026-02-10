package com.example.proyectofinal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.security.MessageDigest;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class homeFragment extends Fragment {

    // Views
    TextView txtTotalHeroes, txtTotalComics, txtSolicitudes;
    TextView txtHeroName, txtHeroDescription;
    ImageView imgHeroDestacado;
    CardView cardBuscar, cardComics, cardHeroDestacado;
    Button btnExplorar;

    // API
    private static final String PUBLIC_KEY = "6a65fc4fc78ff716b65cf680e6eb78c1";
    private static final String PRIVATE_KEY = "ddb4a05b944c359c21acf4dac2ea0ccd57142a38";

    public homeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializar vistas
        initViews(view);

        // Configurar listeners
        setupListeners();

        // Cargar datos iniciales
        cargarDatosIniciales();

        return view;
    }

    private void initViews(View view) {
        // Estadísticas
        txtTotalHeroes = view.findViewById(R.id.txt_total_heroes);
        txtTotalComics = view.findViewById(R.id.txt_total_comics);
        txtSolicitudes = view.findViewById(R.id.txt_solicitudes);

        // Héroe destacado
        txtHeroName = view.findViewById(R.id.txt_hero_name);
        txtHeroDescription = view.findViewById(R.id.txt_hero_description);
        imgHeroDestacado = view.findViewById(R.id.img_hero_destacado);
        cardHeroDestacado = view.findViewById(R.id.card_hero_destacado);

        // Botones y tarjetas
        cardBuscar = view.findViewById(R.id.card_buscar);
        cardComics = view.findViewById(R.id.card_comics);
        btnExplorar = view.findViewById(R.id.btn_explorar);
    }

    private void setupListeners() {
        // Navegación a búsqueda
        cardBuscar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navegarASolicitar();
            }
        });

        // Navegación a comics
        cardComics.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navegarAComics();
            }
        });

        // Botón explorar - navegar a personajes
        btnExplorar.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navegarASolicitar();
            }
        });

        // Al hacer clic en el héroe destacado, buscar uno nuevo
        cardHeroDestacado.setOnClickListener(v -> {
            cargarHeroeDestacado();
            Toast.makeText(getContext(), "🎲 Nuevo héroe cargado", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarDatosIniciales() {
        // Cargar estadísticas
        cargarEstadisticasHeroes();
        cargarEstadisticasComics();
        cargarSolicitudesGuardadas();

        // Cargar héroe destacado
        cargarHeroeDestacado();
    }

    private void cargarEstadisticasHeroes() {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = generateHash(ts, PRIVATE_KEY, PUBLIC_KEY);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelStatsService service = retrofit.create(MarvelStatsService.class);
        Call<JsonObject> call = service.getCharactersStats(PUBLIC_KEY, ts, hash, 1);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    int total = data.get("total").getAsInt();
                    txtTotalHeroes.setText(String.valueOf(total));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                txtTotalHeroes.setText("1562"); // Fallback
                Log.e("HOME", "Error cargando estadísticas de héroes: " + t.getMessage());
            }
        });
    }

    private void cargarEstadisticasComics() {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = generateHash(ts, PRIVATE_KEY, PUBLIC_KEY);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelStatsService service = retrofit.create(MarvelStatsService.class);
        Call<JsonObject> call = service.getComicsStats(PUBLIC_KEY, ts, hash, 1);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body().getAsJsonObject("data");
                    int total = data.get("total").getAsInt();
                    txtTotalComics.setText(String.valueOf(total));
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                txtTotalComics.setText("50000+"); // Fallback
                Log.e("HOME", "Error cargando estadísticas de comics: " + t.getMessage());
            }
        });
    }

    private void cargarSolicitudesGuardadas() {
        try {
            SharedPreferences prefs = requireActivity().getSharedPreferences("solicitudes", getContext().MODE_PRIVATE);
            Set<String> solicitudes = prefs.getStringSet("comics_solicitados", null);
            int count = solicitudes != null ? solicitudes.size() : 0;
            txtSolicitudes.setText(String.valueOf(count));
        } catch (Exception e) {
            txtSolicitudes.setText("0");
        }
    }

    private void cargarHeroeDestacado() {
        String ts = String.valueOf(System.currentTimeMillis());
        String hash = generateHash(ts, PRIVATE_KEY, PUBLIC_KEY);

        // Generar offset aleatorio para obtener diferentes héroes
        Random random = new Random();
        int offset = random.nextInt(1000); // Hasta 1000 héroes

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelStatsService service = retrofit.create(MarvelStatsService.class);
        Call<JsonObject> call = service.getRandomCharacter(PUBLIC_KEY, ts, hash, 1, offset);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonArray results = response.body()
                            .getAsJsonObject("data")
                            .getAsJsonArray("results");

                    if (results.size() > 0) {
                        JsonObject hero = results.get(0).getAsJsonObject();
                        String nombre = hero.get("name").getAsString();
                        String descripcion = hero.get("description").getAsString();

                        // Si no hay descripción, usar una genérica
                        if (descripcion.isEmpty()) {
                            descripcion = "Un poderoso héroe del universo Marvel con habilidades extraordinarias.";
                        }

                        JsonObject thumbnail = hero.getAsJsonObject("thumbnail");
                        String imageUrl = thumbnail.get("path").getAsString() + "." + thumbnail.get("extension").getAsString();

                        // Cambiar a https
                        if (imageUrl.startsWith("http:")) {
                            imageUrl = imageUrl.replace("http:", "https:");
                        }

                        // Actualizar UI
                        txtHeroName.setText(nombre);
                        txtHeroDescription.setText(descripcion);

                        // Cargar imagen con Glide
                        if (getContext() != null) {
                            Glide.with(getContext())
                                    .load(imageUrl)
                                    .placeholder(R.color.image_placeholder)
                                    .error(R.color.image_placeholder)
                                    .into(imgHeroDestacado);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // Héroe de fallback
                txtHeroName.setText("Spider-Man");
                txtHeroDescription.setText("Tu amigable vecino Spider-Man, protector de Nueva York con poderes arácnidos.");
                Log.e("HOME", "Error cargando héroe destacado: " + t.getMessage());
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

    // Interface para las llamadas a la API
    interface MarvelStatsService {
        @GET("v1/public/characters")
        Call<JsonObject> getCharactersStats(
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );

        @GET("v1/public/comics")
        Call<JsonObject> getComicsStats(
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );

        @GET("v1/public/characters")
        Call<JsonObject> getRandomCharacter(
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit,
                @Query("offset") int offset
        );
    }
}