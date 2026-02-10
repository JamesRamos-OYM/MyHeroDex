package com.example.proyectofinal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class ComicsFragment extends Fragment implements ComicsSolicitadosAdapter.OnComicClickListener {

    Spinner spinnerComics;
    Button btnSolicitar;
    RecyclerView recyclerComicsSolicitados;
    TextView txtComicsSolicitados;

    List<Comic> listaComics = new ArrayList<>();
    List<Comic> comicsSolicitados = new ArrayList<>();
    ArrayAdapter<Comic> adapter;
    ComicsSolicitadosAdapter adapterSolicitados;


    private ComicDetailsManager detailsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comics, container, false);


        detailsManager = new ComicDetailsManager(getContext());


        spinnerComics = view.findViewById(R.id.spinner_comics);
        btnSolicitar = view.findViewById(R.id.btn_solicitar_comic);
        recyclerComicsSolicitados = view.findViewById(R.id.recycler_comics_solicitados);
        txtComicsSolicitados = view.findViewById(R.id.txt_comics_solicitados);


        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, listaComics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerComics.setAdapter(adapter);


        recyclerComicsSolicitados.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterSolicitados = new ComicsSolicitadosAdapter(comicsSolicitados, this);
        recyclerComicsSolicitados.setAdapter(adapterSolicitados);


        cargarComicsDesdeApi();
        cargarComicsSolicitados();

        btnSolicitar.setOnClickListener(v -> {
            if (spinnerComics.getSelectedItem() != null) {
                Comic comicSeleccionado = (Comic) spinnerComics.getSelectedItem();

                if (yaEstaSolicitado(comicSeleccionado)) {
                    Toast.makeText(getContext(), "Este comic ya está en tu lista de solicitados", Toast.LENGTH_SHORT).show();
                    return;
                }

                guardarSolicitud(comicSeleccionado);
                cargarComicsSolicitados();

                Toast.makeText(getContext(), "Comic solicitado: " + comicSeleccionado.getNombre(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Por favor selecciona un comic", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }


    @Override
    public void onEliminarClick(Comic comic) {
        eliminarComicSolicitado(comic);
    }

    @Override
    public void onVerDetallesClick(Comic comic) {
        detailsManager.mostrarDetallesComic(comic);
    }

    private boolean yaEstaSolicitado(Comic comic) {
        for (Comic solicitado : comicsSolicitados) {
            if (solicitado.getId() == comic.getId()) {
                return true;
            }
        }
        return false;
    }

    private void cargarComicsSolicitados() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("solicitudes", getContext().MODE_PRIVATE);
        Set<String> solicitudes = prefs.getStringSet("comics_solicitados", new HashSet<>());

        comicsSolicitados.clear();
        for (String solicitud : solicitudes) {
            String[] partes = solicitud.split("\\|");
            if (partes.length == 2) {
                int id = Integer.parseInt(partes[0]);
                String nombre = partes[1];
                comicsSolicitados.add(new Comic(id, nombre));
            }
        }


        if (adapterSolicitados != null) {
            adapterSolicitados.notifyDataSetChanged();
        }


        if (comicsSolicitados.isEmpty()) {
            txtComicsSolicitados.setVisibility(View.GONE);
            recyclerComicsSolicitados.setVisibility(View.GONE);
        } else {
            txtComicsSolicitados.setVisibility(View.VISIBLE);
            recyclerComicsSolicitados.setVisibility(View.VISIBLE);
            txtComicsSolicitados.setText("Mis Comics Solicitados (" + comicsSolicitados.size() + ")");
        }
    }

    private void eliminarComicSolicitado(Comic comic) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("solicitudes", getContext().MODE_PRIVATE);
        Set<String> solicitudes = prefs.getStringSet("comics_solicitados", new HashSet<>());

        Set<String> nuevasSolicitudes = new HashSet<>(solicitudes);
        nuevasSolicitudes.remove(comic.getId() + "|" + comic.getNombre());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("comics_solicitados", nuevasSolicitudes);
        editor.apply();


        cargarComicsSolicitados();

        Toast.makeText(getContext(), "Comic eliminado de solicitados", Toast.LENGTH_SHORT).show();
    }

    private void guardarSolicitud(Comic comic) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("solicitudes", getContext().MODE_PRIVATE);
        Set<String> solicitudes = prefs.getStringSet("comics_solicitados", new HashSet<>());

        Set<String> nuevasSolicitudes = new HashSet<>(solicitudes);
        nuevasSolicitudes.add(comic.getId() + "|" + comic.getNombre());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("comics_solicitados", nuevasSolicitudes);
        editor.apply();

        Log.d("SOLICITUD", "Comic guardado: " + comic.getNombre());
    }

    private void cargarComicsDesdeApi() {
        String ts = String.valueOf(System.currentTimeMillis());
        String publicKey = "6a65fc4fc78ff716b65cf680e6eb78c1";
        String privateKey = "ddb4a05b944c359c21acf4dac2ea0ccd57142a38";
        String hash = generateHash(ts, privateKey, publicKey);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.marvel.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarvelComicsService service = retrofit.create(MarvelComicsService.class);
        Call<JsonObject> call = service.getComics(publicKey, ts, hash, 20);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaComics.clear();
                    JsonArray results = response.body()
                            .getAsJsonObject("data")
                            .getAsJsonArray("results");

                    for (JsonElement e : results) {
                        JsonObject obj = e.getAsJsonObject();
                        String nombre = obj.get("title").getAsString();
                        int id = obj.get("id").getAsInt();
                        listaComics.add(new Comic(id, nombre));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("API", "Error cargando comics: " + t.getMessage());
                Toast.makeText(getContext(), "Error cargando comics", Toast.LENGTH_SHORT).show();
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

    interface MarvelComicsService {
        @GET("v1/public/comics")
        Call<JsonObject> getComics(
                @Query("apikey") String apikey,
                @Query("ts") String timestamp,
                @Query("hash") String hash,
                @Query("limit") int limit
        );
    }

    public static class Comic {
        private int id;
        private String nombre;

        public Comic(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }

        @Override
        public String toString() {
            return nombre;
        }
    }
}