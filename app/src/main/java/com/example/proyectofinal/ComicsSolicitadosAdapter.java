package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComicsSolicitadosAdapter extends RecyclerView.Adapter<ComicsSolicitadosAdapter.ComicViewHolder> {

    private List<ComicsFragment.Comic> comics;
    private OnComicClickListener listener;

    public interface OnComicClickListener {
        void onEliminarClick(ComicsFragment.Comic comic);
        void onVerDetallesClick(ComicsFragment.Comic comic);
    }

    public ComicsSolicitadosAdapter(List<ComicsFragment.Comic> comics, OnComicClickListener listener) {
        this.comics = comics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comic_solicitado, parent, false);
        return new ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        ComicsFragment.Comic comic = comics.get(position);
        holder.bind(comic, listener);
    }

    @Override
    public int getItemCount() {
        return comics.size();
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreComic;
        TextView txtIdComic;
        Button btnEliminar;
        Button btnVerDetalles;

        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreComic = itemView.findViewById(R.id.txt_nombre_comic);
            txtIdComic = itemView.findViewById(R.id.txt_id_comic);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_comic);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_detalles);
        }

        public void bind(ComicsFragment.Comic comic, OnComicClickListener listener) {
            txtNombreComic.setText(comic.getNombre());
            txtIdComic.setText("ID: " + comic.getId());

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(comic);
                }
            });

            btnVerDetalles.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVerDetallesClick(comic);
                }
            });
        }
    }
}